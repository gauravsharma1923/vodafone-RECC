package recc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class getPackType extends HttpServlet {

    static HashMap<String, String> map = new HashMap<String, String>();

    public void readConfiguration() {
        try {

            ServletContext cntxt = getServletContext();
            InputStream ins = cntxt.getResourceAsStream("/WEB-INF/RECCConfig.txt");
            String line;
            String[] strArr = new String[2];
            if (ins == null) {
                System.out.println("Could not read Properties file");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(ins));
                while ((line = br.readLine()) != null) {
                    strArr = line.split("=");
                    map.put(strArr[0], strArr[1]);
                }
                br.close();
            }
        } catch (Exception ex) {
            System.out.println("Could not read Properties file");
            ex.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        String strReqType = "";
        String strMsisdn = "";
        String circleId = "";
        String strConnType = "";
        String strFlag = "";
        String strSessionId = "";
        String strMessage = "";
        String url = "";
        String strData = "";
        String[] strArr = new String[5];
        String username = "";
        String password = "";
        String strIP = "";
        String strPort = "";
        String strSO_TimeOut = "";
        String strCON_TimeOut = "";
        long uniqueNumber = 0L;
        String strBL_Packs = "";
        try {
            out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            strMsisdn = request.getParameter("msisdn");
            strMsisdn = strMsisdn.trim();
            circleId = request.getParameter("circleId");
            circleId = circleId.trim();
            strConnType = request.getParameter("connType");
            strConnType = strConnType.trim();
            strSessionId = request.getParameter("sessionId");
            strSessionId = strSessionId.trim();

            if (map.isEmpty()) {
                readConfiguration();
            }

            strData = map.get(circleId);
            strCON_TimeOut = map.get("CONNECTION_TIMEOUT");
            strSO_TimeOut = map.get("SO_TIMEOUT");
            strBL_Packs = map.get("BL_PACKS");
            strArr = strData.split("~");
            strFlag = strArr[0];
            username = strArr[1];
            password = strArr[2];
            strIP = strArr[3];
            strPort = strArr[4];

            uniqueNumber = System.currentTimeMillis();
            Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strConnType, "Request");

            url = "http://" + strIP + ":" + strPort + "/services/TisService";
            System.out.println("Node ::" + strFlag + " Url ::" + url);
            Client1 cl = null;
            CustomerDetails objCustomerDetails = null;
            if (strConnType.equalsIgnoreCase("O")) {
                try {
                    cl = new Client1(url, username, password, strCON_TimeOut, strSO_TimeOut);
                    objCustomerDetails = cl.getPackType(strFlag, strMsisdn, strReqType, circleId, strSessionId);
                    String errorMsg = objCustomerDetails.getErrorMsg();
                    if (errorMsg.equals("no_data")) {
                        out.print("msg=no_data");
                    } else if (errorMsg.equals("exception")) {
                        out.print("msg=exception");
                    } else if (errorMsg.equals("success")) {
                        String strPackName = "";
                        ArrayList<CustomerDetailsBean> arrCustomerDetailsBean = objCustomerDetails.getListCustomerDetails();
                        for (CustomerDetailsBean objCustomerDetailsBean : arrCustomerDetailsBean) {
                            strPackName = objCustomerDetailsBean.getPackName();
                            String functionalName = objCustomerDetailsBean.getFunctionalName();
                            if ((strPackName != null) && !(strPackName.endsWith("Usage")) && !(strPackName.endsWith("Usage1"))) {
                                if (!isCheckBlackListedPacks(strPackName, strBL_Packs)) {
                                    if (functionalName.intern() == "RPP") {
                                        strMessage = strMessage + strPackName + "|";
                                    } else {
                                        char thirdDigit = strPackName.charAt(2);
                                        char fourthDigit = strPackName.charAt(3);
                                        if (thirdDigit == 'W' && fourthDigit == 'F') {
                                        } else {
                                            String type = "";
                                            int start_point = strPackName.indexOf("G") - 1;
                                            int end_point = strPackName.indexOf("G") + 1;
                                            if (start_point <= 0 || end_point <= 0) {
                                            } else {
                                                type = strPackName.substring(start_point, end_point);
                                            }
                                            strMessage = strMessage + strPackName + "#" + type + "|";
                                        }
                                    }
                                }
                            }
                        }
                        if (strMessage == "") {
                            strMessage = "no_data";
                        }

                        out.println("msg=" + strMessage);
                        Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strConnType + "," + strMessage.trim() + "," + (System.currentTimeMillis() - uniqueNumber), "Response");

                    }
                } catch (Exception e) {
                    Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strConnType + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Error");
                    e.printStackTrace();
                    System.out.println("EXCEPTION ::" + e.getMessage());
                    out.println("msg=exception");

                } finally {
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION ::" + e.getMessage());
            out.println("msg=error");

        } finally {
            out.flush();
            out.close();
        }
    }

    public boolean isCheckBlackListedPacks(String strPackName, String strBL_Packs) {
        String[] arrBLPacks = null;
        boolean booleanFlag = false;
        if (strBL_Packs != null) {
            arrBLPacks = strBL_Packs.split("~");
            for (int i = 0; i < arrBLPacks.length; i++) {
                if (strPackName != null && arrBLPacks[i].equalsIgnoreCase(strPackName)) {
                    booleanFlag = true;
                }
            }
        }
        return booleanFlag;
    }
}
