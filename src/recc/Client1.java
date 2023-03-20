package recc;

import org.apache.axis2.client.*;
import org.apache.axis2.addressing.*;
import com.nsn.ossbss.charge_once.wsdl.entity.tis.xsd._1.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.Constants;
import org.apache.axis2.recc.TisServiceStub;
import org.apache.axis2.recc.TisException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Client1 {

    TisServiceStub stub = null;
    ServiceClient sClient = null;
    SOAP11Factory factory = null;
    long uniqueNumber = 0L;

    public Client1(String Url, String username, String password, String strCON_TimeOut, String strSO_TimeOut) {
        try {
            stub = new TisServiceStub(Url);
            sClient = stub._getServiceClient();
            factory = new SOAP11Factory();
            OMNamespace SecurityElementNamespace = factory.createOMNamespace("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "wsse");

            OMElement usernameTokenEl = factory.createOMElement("UsernameToken", SecurityElementNamespace);
            OMElement usernameEl = factory.createOMElement("Username", SecurityElementNamespace);
            OMElement passwordEl = factory.createOMElement("Password", SecurityElementNamespace);
            OMAttribute attr = factory.createOMAttribute("Type", null, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
            usernameEl.setText(username);
            passwordEl.setText(password);
            passwordEl.addAttribute(attr);
            usernameTokenEl.addChild(usernameEl);
            usernameTokenEl.addChild(passwordEl);
            SOAPHeaderBlockImpl block = new SOAP11HeaderBlockImpl("Security", SecurityElementNamespace, factory);
            block.setMustUnderstand(true);
            block.addChild(usernameTokenEl);
            sClient.addHeader(block);
            Options options = sClient.getOptions();
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
            options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, AddressingConstants.Submission.WSA_NAMESPACE);

            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer.parseInt(strCON_TimeOut));
            options.setProperty(HTTPConstants.SO_TIMEOUT, Integer.parseInt(strSO_TimeOut));
            sClient.setOptions(options);
            options.setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Boolean.TRUE);
        } catch (Exception ex) {
            System.out.println("Exception in connection ::" + ex.getMessage());
        }
    }

    public ArrayList<String> getActiveDataPacks(String strFlag, String Msisdn, String strRType, String circleId, String strSessionId) {
        ArrayList<String> packNames = new ArrayList<String>();
        String value = "no_data";
        String errMessage = "";
        String Packname = "NOW";
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");

            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);

            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");

            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");

            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
            //System.out.println("Request ::"+req);
            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            //System.out.println("Response ::"+response);
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    ParameterList objParameterList = objOperation.getParameterList();
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        String packName = "", customerId = "";
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && !(packName.equals("MANDONERAT") || packName.equals("MANDWFRAT"))) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        packNames.add(packName);
                                        value = value + packName + ",";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + value + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");

        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errMessage = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errMessage + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            packNames.add("exception");
        } catch (Exception e) {
            e.printStackTrace();
            /*String errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());
                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());
                }
            }*/
            errMessage = e.getMessage();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            packNames.add("exception");
        } finally {
            close_connection();
        }
        return packNames;
    }

    public CustomerDetails getBottomUpHierarchyPrepaid(String strFlag, String Msisdn, String strRType, String circleId, String strSessionId) {
        String errorMsg = "no_data";
        String Packname = "NOW";
        ArrayList<CustomerDetailsBean> arrResult = new ArrayList<CustomerDetailsBean>();
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");
            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);
            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");
            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");
            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            //System.out.println("Response ::"+response);
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                String customerId = "", packName = "", functionalName = "";
                long endDate = 0l, balance = 0l;
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        functionalName = objOperation.getModifier();
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && !(packName.equals("MANDONERAT") || packName.equals("MANDWFRAT"))) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        LongParameter[] arrLongParameter = objParameterList.getLongParameterArray();
                                        for (LongParameter objLongParameter : arrLongParameter) {
                                            //if (objLongParameter.getName().equals("s_NextPeriodAct")) {
                                            if (objLongParameter.getName().equals("s_ActivationEndTime")) {
                                                endDate = objLongParameter.longValue();
                                            }
                                        }
                                        StructParameter[] arrStructParameter = objParameterList.getStructParameterArray();
                                        for (StructParameter objStructParameter : arrStructParameter) {
                                            if (objStructParameter.getName().equals("s_PeriodicBonus_FU")) {
                                                balance = objStructParameter.getLongParameterArray(0).getLongValue();
                                            }
                                        }
                                        //System.out.println("customerId : " + Msisdn + " PackName : " + packName + " balance : " + balance + " TotalQuota : " + 0 + " FunctionalName : " + functionalName);
                                        arrResult.add(new CustomerDetailsBean(customerId, functionalName, packName, 0, balance, endDate));
                                        errorMsg = "success";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
            return new CustomerDetails(errorMsg, arrResult);
        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errorMsg = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errorMsg + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } catch (Exception e) {
            e.printStackTrace();
            /*errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());
                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());
                }
            }*/
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } finally {
            close_connection();
        }
    }
   
    public CustomerDetails getBottomUpHierarchyPostpaid(String strFlag, String Msisdn, String strRType, String circleId, String strSessionId) {
        String errorMsg = "no_data";
        String Packname = "NOW";
        ArrayList<CustomerDetailsBean> arrResult = new ArrayList<CustomerDetailsBean>();
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");
            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);
            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");
            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");
            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
//	    System.out.println("request:"+req);
            CommandResponseDataDocument response = stub.executeCommand(req);
//	    System.out.println("response:"+response);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        String customerId = "", packName = "";
                        long endDate = 0l, balance = 0l;
                        int totalQuota=0;
                        String functionalName = objOperation.getModifier();
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && !(packName.equals("MANDONERAT") || packName.equals("MANDWFRAT"))) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        IntParameter[] arrIntParameter = objParameterList.getIntParameterArray();
                                        for (IntParameter objIntParameter : arrIntParameter) {
                                            if (objIntParameter.getName().equals("s_FreeOfChargeModifyTasks")) {
                                                totalQuota = objIntParameter.getIntValue();
                                            }
                                        }
                                        LongParameter[] arrLongParameter = objParameterList.getLongParameterArray();
                                        for (LongParameter objLongParameter : arrLongParameter) {
                                            if (objLongParameter.getName().equals("s_NextPeriodAct")) {
                                                endDate = objLongParameter.longValue();
                                            }
                                        }
                                        StructParameter[] arrStructParameter = objParameterList.getStructParameterArray();
                                        for (StructParameter objStructParameter : arrStructParameter) {
                                            if (objStructParameter.getName().equals("s_PeriodicBonus_FU")) {
                                                balance = objStructParameter.getLongParameterArray(0).getLongValue();
                                            }
                                        }
                                       //System.out.println("customerId : " + Msisdn + " PackName : " + packName + " balance : " + balance + " TotalQuota : " + totalQuota + " FunctionalName : " + functionalName);
                                        arrResult.add(new CustomerDetailsBean(customerId, functionalName, packName, totalQuota, balance, endDate));
                                        errorMsg = "success";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
            return new CustomerDetails(errorMsg, arrResult);
        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errorMsg = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errorMsg + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } catch (Exception e) {
            e.printStackTrace();
            /*errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());
                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());
                }
            }*/
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } finally {
            close_connection();
        }
    }




 public CustomerDetails getBottomUpHierarchyPostpaidRed(String strFlag, String Msisdn, String circleId, String strSessionId) {
        String identity = "";
        String errorMsg = "no_data";
        String Packname = "NOW";
        ArrayList<CustomerDetailsBean> arrResult = new ArrayList<CustomerDetailsBean>();
        CustomerDetails objCustomerDetails = null;
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");
            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);
            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");
            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");
            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();

            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage") || objOperation.getModifier().equals("RPP_s_limitSupervision")) {
                        String customerId = "", packName = "";
                        long endDate = 0l, balance = 0l;
                        int totalQuota = 0;
                        String functionalName = objOperation.getModifier();
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId != Msisdn && packName.startsWith("FP")) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        IntParameter[] arrIntParameter = objParameterList.getIntParameterArray();
                                        for (IntParameter objIntParameter : arrIntParameter) {
                                            if (objIntParameter.getName().equals("s_FreeOfChargeModifyTasks")) {
                                                totalQuota = objIntParameter.getIntValue();
                                            }
                                        }
		                        StructParameter[] arrStructParameter = objParameterList.getStructParameterArray();
                                        for (StructParameter objStructParameter : arrStructParameter) {
                                            if (objStructParameter.getName().equals("s_PeriodicBonus_FU")) {
                                                balance = objStructParameter.getLongParameterArray(0).getLongValue();
                                            }
                                        }
                                        LongParameter[] arrLongParameter = objParameterList.getLongParameterArray();
                                        for (LongParameter objLongParameter : arrLongParameter) {
                                            if (objLongParameter.getName().equals("s_NextPeriodAct")) {
                                                endDate = objLongParameter.longValue();
                                            }
                                        }

                                        arrResult.add(new CustomerDetailsBean(customerId, functionalName, packName, totalQuota, balance, endDate));
                                        errorMsg = "success";
                                    }
                                }
                            }
                        } else if (customerId.equals(Msisdn) && functionalName.equals("RPP_s_limitSupervision")) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        IntParameter[] arrIntParameter = objParameterList.getIntParameterArray();
                                        for (IntParameter objIntParameter : arrIntParameter) {
                                            if (objIntParameter.getName().equals("s_FreeOfChargeModifyTasks")) {
                                                totalQuota = objIntParameter.getIntValue();
                                            }
                                        }
                                        StructParameter[] arrStructParameter = objParameterList.getStructParameterArray();
                                        for (StructParameter objStructParameter : arrStructParameter) {
                                            if (objStructParameter.getName().equals("s_PeriodicBonus_FU")) {
                                                balance = objStructParameter.getLongParameterArray(0).getLongValue();
                                            }
                                        }
				 arrResult.add(new CustomerDetailsBean(customerId, functionalName, packName, totalQuota, balance, endDate));
                                        errorMsg = "success";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            objCustomerDetails = new CustomerDetails(errorMsg, arrResult);
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
            return objCustomerDetails;
        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errorMsg = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errorMsg + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception",null);
        } catch (Exception e) {
            e.printStackTrace();
	 Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } finally {
            close_connection();
        }
    }



    public String getSharedPackDetails(String strFlag, String Msisdn, String strRType, String circleId, String strSessionId) {
        String value = "";
        String errMessage = "";
        String Packname = "NOW";
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");
            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);
            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");
            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");
            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");

            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();

            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                String customerId = "", packName = "";
                long balance = 0l;
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP")) {
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && !(packName.equals("MANDONERAT") || packName.equals("MANDWFRAT"))) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        StructParameter[] arrStructParameter = objParameterList.getStructParameterArray();
                                        for (StructParameter objStructParameter : arrStructParameter) {
                                            if (objStructParameter.getName().equals("s_PeriodicBonus_FU")) {
                                                balance = objStructParameter.getLongParameterArray(0).getLongValue();
                                            }
                                        }
                                        //System.out.println("Msisdn : " + Msisdn + " PackName : " + packName + " balance : " + balance);
                                        value = value + packName + "~" + balance + "#";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
            if (value == "") {
                value = "no_data";
            } else {
                value = value.substring(0, value.length() - 1);
            }
            //System.out.println("value :: " + value);
            return value;
        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errMessage = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errMessage + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return "exception";

        } catch (Exception e) {
            e.printStackTrace();
            /*String errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());

                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());

                }
            }*/
            errMessage = e.getMessage();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return "exception";
        } finally {
            close_connection();
        }
    }

    public String getDataPackValidity(String strFlag, String Msisdn, String circleId, String packName, String strSessionId) {
        String endDate = "unsubscribed";
        String errMessage = "";
        String Packname = "NOW";
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");

            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);

            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");

            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");

            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");

            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();

            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        String customerId = "", stPackName = "";
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                stPackName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && stPackName.equals(packName)) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        LongParameter[] arrLongParameter = objParameterList.getLongParameterArray();
                                        for (LongParameter objLongParameter : arrLongParameter) {
                                            if (objLongParameter.getName().equals("s_ActivationEndTime")) {
                                                java.util.Date dateTime = new Date(objLongParameter.getLongValue());
                                                endDate = dateTime.toLocaleString();
                                            }
                                        }
                                    } else {
                                        endDate = "unsubscribed";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + endDate + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
        } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errMessage = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errMessage + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            endDate = "exception";
        } catch (Exception e) {
            e.printStackTrace();
            /*String errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());
                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());
                }
            }*/
            errMessage = e.getMessage();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            endDate = "exception";
        } finally {
            close_connection();
        }
        return endDate;
    }

    public String getDataPackStatus(String strFlag, String Msisdn, String circleId, String packName, String strSessionId) {
        String value = "unsubscribed";
        String errMessage = "";
        String Packname = "NOW";
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");

            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);

            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");

            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");

            uniqueNumber = System.currentTimeMillis();
            Log.getInReccStatusLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
            //System.out.println("Request : "+req.toString());
            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            //System.out.println("Response : "+res.toString());
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        String customerId = "", stPackName = "";
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                stPackName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && stPackName.equals(packName)) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                        value = "active";
                                    } else {
                                        //System.out.println("ak");
                                        value = "unsubscribed";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccStatusLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + value + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
        } catch (TisException e) {
            value = "exception";
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            if(errInfo[0].getCode().trim().intern()=="13423"){
              value = "unsubscribed";
            }
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errMessage = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccStatusLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errMessage + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
        } catch (Exception e) {
            e.printStackTrace();
            /*String errorMsg = e.getMessage();
            if (errorMsg.equalsIgnoreCase("Connection refused") || errorMsg.equalsIgnoreCase("Read timed out") || errorMsg.equalsIgnoreCase("connect timed out") || errorMsg.equalsIgnoreCase("The host did not accept the connection within timeout of 2000 ms")) {
                System.out.println("Exception ::" + errorMsg);
                if (circleId.equals("0003") || circleId.equals("0004") || circleId.equals("0006") || circleId.equals("0010") || circleId.equals("0012") || circleId.equals("0013") || circleId.equals("0022") || circleId.equals("0023")) {
                    SingleToneAcess.getObject().setDelhiFlag(false);
                    SingleToneAcess.getObject().setDelhiError(e.getMessage());
                }
                if (circleId.equals("0005") || circleId.equals("0009") || circleId.equals("0014") || circleId.equals("0021")) {
                    SingleToneAcess.getObject().setMumbaiFlag(false);
                    SingleToneAcess.getObject().setMumbaiError(e.getMessage());
                }
                if (circleId.equals("0008") || circleId.equals("0017") || circleId.equals("0018") || circleId.equals("0019") || circleId.equals("0011") || circleId.equals("0020")) {
                    SingleToneAcess.getObject().setKolkataFlag(false);
                    SingleToneAcess.getObject().setKolkataError(e.getMessage());
                }
                if (circleId.equals("0002") || circleId.equals("0015") || circleId.equals("0001") || circleId.equals("0007") || circleId.equals("0016")) {
                    SingleToneAcess.getObject().setChennaiFlag(false);
                    SingleToneAcess.getObject().setChennaiError(e.getMessage());
                }
            }*/
            errMessage = e.getMessage();
            Log.getInReccStatusLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            value = "exception";
        } finally {
            close_connection();
        }
        return value;
    }
    
     public CustomerDetails getPackType(String strFlag, String Msisdn, String reqType, String circleId, String strSessionId) {
         ArrayList<CustomerDetailsBean> arrResult = new ArrayList<CustomerDetailsBean>();
        String value = "";
        String errorMsg = "no_data";
        String Packname = "NOW";
        try {
            CommandRequestDataDocument req = CommandRequestDataDocument.Factory.newInstance();
            CommandRequestDataDocument.CommandRequestData data = req.addNewCommandRequestData();
            Environment environment = data.addNewEnvironment();
            NameValuePair domain = environment.addNewParameter();
            domain.setName("ApplicationDomain");
            domain.setValue("CAO_LDM_00");
            NameValuePair namespace = environment.addNewParameter();
            namespace.setName("DefaultOperationNamespace");
            namespace.setValue("GMF");
            Command command = data.addNewCommand();
            Transaction trans = command.addNewTransaction();
            Operation operation = trans.addNewOperation();
            operation.setModifier("Customer");
            operation.setName("ReadRatePrefetchData");

            ParameterList cust = operation.addNewParameterList();
            StringParameter id = cust.insertNewStringParameter(0);
            id.setName("CustomerId");
            id.setStringValue(Msisdn);

            SymbolicParameter id1 = cust.insertNewSymbolicParameter(0);
            id1.setName("SelectionDate");
            id1.setStringValue(Packname);
            id1.setNamespace("@");

            BooleanParameter id2 = cust.insertNewBooleanParameter(0);
            id2.setName("IgnoreTerminatedProfiles");
            id2.setBooleanValue(true);
            id2.setNamespace("@");

            uniqueNumber = System.currentTimeMillis();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + Packname, "InReq");
            //System.out.println("Request       ::" + req);
            CommandResponseDataDocument response = stub.executeCommand(req);
            CommandResponseDataDocument.CommandResponseData res = response.getCommandResponseData();
            //System.out.println("Response      ::" + res);
            CommandResult commandResult = res.getCommandResult();
            OperationResult arrOperationResult[] = commandResult.getTransactionResult().getOperationResultArray();
            for (OperationResult objOperationResult : arrOperationResult) {
                Operation arrOperation[] = objOperationResult.getOperationArray();
                for (Operation objOperation : arrOperation) {
                    if (objOperation.getModifier().equals("RPP") || objOperation.getModifier().equals("RPP_s_QoSOnTotalUsage")) {
                        String customerId = "", packName = "", functionalName = "";
                        functionalName = objOperation.getModifier();
                        ParameterList objParameterList = objOperation.getParameterList();
                        StringParameter[] arrStringParameter = objParameterList.getStringParameterArray();
                        for (StringParameter objStringParameter : arrStringParameter) {
                            if (objStringParameter.getName().equals("CustomerId")) {
                                customerId = objStringParameter.getStringValue();
                            }
                            if (objStringParameter.getName().equals("s_PackageId")) {
                                packName = objStringParameter.getStringValue();
                            }
                        }
                        if (customerId.equals(Msisdn) && !(packName.equals("MANDONERAT") || packName.equals("MANDWFRAT"))) {
                            BooleanParameter[] arrBooleanParameter = objParameterList.getBooleanParameterArray();
                            for (BooleanParameter objBooleanParameter : arrBooleanParameter) {
                                if (objBooleanParameter.getName().equals("s_Active")) {
                                    if (objBooleanParameter.getBooleanValue()) {
                                         arrResult.add(new CustomerDetailsBean(customerId, functionalName, packName, 0, 0, 0));
                                        errorMsg = "success";
                                        value = value + packName + ",";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + value + "," + (System.currentTimeMillis() - uniqueNumber), "InRes");
            return new CustomerDetails(errorMsg, arrResult);
            } catch (TisException e) {
            FaultMessageDocument doc = e.getFaultMessage();
            FaultMessageDocument.FaultMessage msg = doc.getFaultMessage();
            ErrorInfo errInfo[] = msg.getErrorInfoArray();
            System.out.println(errInfo[0].getCode() + " " + errInfo[0].getDetail() + " " + errInfo[0].getText());
            errorMsg = "<Error Code>" + errInfo[0].getCode() + "<Error Detail> " + errInfo[0].getDetail() + "<Error Text> " + errInfo[0].getText();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + errorMsg + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.getInReccLogs(strFlag, strSessionId + "," + Msisdn + "," + circleId + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "InErr");
            return new CustomerDetails("exception", null);
        } finally {
            close_connection();
        }
    }

    public void close_connection() {
        try {
            sClient.cleanupTransport();
            sClient.cleanup();
            stub.cleanup();
        } catch (Exception ex) {
            System.out.println("error in service client ::" + ex.getMessage());
        }
    }
}
