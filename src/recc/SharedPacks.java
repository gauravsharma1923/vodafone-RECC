package recc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SharedPacks extends HttpServlet {

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
        String strPackName = "";
        String circleId = "";
        String strConnType = "";
        String strSessionId = "";
        String strMessage = "";
        String strPack = "";
        String url = "";
        String strData = "";
        String[] strArr = null;
        String strBL_Packs = null;
        String username = "";
        String password = "";
        String strIP = "";
        String strPort = "";
        String strSO_TimeOut = "";
        String strCON_TimeOut = "";
        String strFlag = "";
        long uniqueNumber = 0L;
        try {
            out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            strMsisdn = request.getParameter("msisdn");
            strMsisdn = strMsisdn.trim();
            strReqType = request.getParameter("reqType");
            strReqType = strReqType.trim();
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
            Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strReqType + "," + strConnType, "Request");

            url = "http://" + strIP + ":" + strPort + "/services/TisService";
            System.out.println("Node ::" + strFlag + " Url ::" + url);

            if (strConnType.equalsIgnoreCase("R")) {
                Client1 cl = new Client1(url, username, password, strCON_TimeOut, strSO_TimeOut);
                try {
                    String resMessage = cl.getSharedPackDetails(strFlag, strMsisdn, strReqType, circleId, strSessionId);
                    if (resMessage.equals("no_data")) {
                        out.print("msg=no_data");
                        strMessage = "No Data";
                        strPack = strMessage;
                    } else if (resMessage.equals("exception")) {
                        out.print("msg=exception");
                        strMessage = "exception";
                        strPack = strMessage;
                    } else {
                        int j = 0;
                        String[] strResult = resMessage.split("#");
                        for (int i = 0; i < strResult.length; i++) {
                            String[] strAtributes = strResult[i].split("~");
                            strPackName = strAtributes[0];
                            if (!isCheckBlackListedPacks(strPackName, strBL_Packs)) {
                                j = j + 1;
                                char secondDigit = strPackName.charAt(1);
                                char thirdDigit = strPackName.charAt(2);
                                char fourthDigit = strPackName.charAt(3);
                                String pack = "";
                                String type = "";

                                int duration = strPackName.lastIndexOf("D");
                                if (duration > 0) {
                                    pack = "duration";
                                } else {
                                    pack = "volume";
                                }
                                
                                if (secondDigit == 'I' && thirdDigit == 'W' && fourthDigit == 'F') {

                                } else if (pack.equals("volume")) {

                                } else if ((strPackName.startsWith("BOOST") || strPackName.startsWith("BOOS")) && pack.equals("duration")) {

                                } else {
                                    int start_point = strPackName.indexOf("G") - 1;
                                    int end_point = strPackName.indexOf("G") + 1;
                                    if (start_point <= 0 || end_point <= 0) {
                                    } else {
                                        type = strPackName.substring(start_point, end_point);
                                    }
                                    long balance = Long.parseLong(strAtributes[1]);
                                    String desc = getShortDescription(strPackName);
                                    strMessage = strMessage + strPackName + "," + balance + "," + type + "," + desc + "#";
                                }
                            }
                        }
                        if (strMessage == "") {
                            strMessage = "no_data";
                        }
                        out.println("msg=" + strMessage);
                        Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strReqType + "," + strConnType + "," + strPack + "," + strMessage.trim() + "," + (System.currentTimeMillis() - uniqueNumber), "Response");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("EXCEPTION ::" + e.getMessage());
                    out.println("exception");
                    Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strReqType + "," + strConnType + "," + strPack + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Error");
                } finally {
                    out.flush();
                    out.close();
                    cl.close_connection();
                }
            } else if (strConnType.equalsIgnoreCase("O")) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION ::" + e.getMessage());
            out.println("msg=error");
            Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strReqType + "," + strConnType + "," + strPackName + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Error");
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

    public String getShortDescription(String strPackName) {
        String strMessage = "";
        char secondDigit = strPackName.charAt(1);
        char last_digit = strPackName.charAt(strPackName.length() - 1);
        char second_last = strPackName.charAt(strPackName.length() - 2);
        String conn_type = "";
        int start_point = 0, end_point = 0;
        int data = 0;
        int start_index = 0;
        int end_index = 0;
        if (secondDigit == 'I') {
            conn_type = "Internet";
            start_point = 2;
            end_point = strPackName.indexOf("G") + 1;
        } else if (secondDigit == 'B') {
            conn_type = "Broadband";
            start_point = strPackName.indexOf("B") + 1;
            end_point = strPackName.indexOf("G") + 1;
        } else if (secondDigit == 'D') {
            conn_type = "Internet";
            start_point = strPackName.indexOf("I") + 1;
            end_point = strPackName.indexOf("G") + 1;
        }
        String mobile_internet = strPackName.substring(start_point, end_point);
        start_index = strPackName.lastIndexOf("D") + 1;

        if ((last_digit == 'G') && ((strPackName.charAt(strPackName.lastIndexOf("D") + 2)) == 'P')) {
            String value = (strPackName.substring(strPackName.lastIndexOf("D") + 1, strPackName.lastIndexOf("P")));
            start_index = strPackName.lastIndexOf("P") + 1;
            end_index = strPackName.length() - 1;
            value = value + "." + (strPackName.substring(start_index, end_index)) + "5" + last_digit + "B";
            strMessage = "Mobile " + conn_type + " " + value + "(" + mobile_internet + ")";
        } else if ((second_last == 'H') && (last_digit == 'M')) {
            end_index = strPackName.length() - 2;
            data = Integer.parseInt(strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + data * 100 + "" + last_digit + "B" + "(" + mobile_internet + ")";

        } else if ((second_last == 'K') && (last_digit == 'M')) {
            end_index = strPackName.length() - 2;
            data = Integer.parseInt(strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + data * 1000 + "" + last_digit + "B" + "(" + mobile_internet + ")";
        } else if ((second_last == 'M') && (last_digit == 'B')) {
            end_index = strPackName.length() - 2;
            data = Integer.parseInt(strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + data + "" + "MB" + "(" + mobile_internet + ")";

        } else if ((second_last == 'G') && (last_digit == 'B')) {
            end_index = strPackName.length() - 2;
            data = Integer.parseInt(strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + data + "" + "GB" + "(" + mobile_internet + ")";

        } else if ((last_digit == 'G') || (last_digit == 'M')) {
            end_index = strPackName.length() - 1;
            data = Integer.parseInt(strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + data + "" + last_digit + "B" + "(" + mobile_internet + ")";
        } else {
            end_index = strPackName.length();
            String value = (strPackName.substring(start_index, end_index));
            strMessage = "Mobile " + conn_type + " " + value + " " + "(" + mobile_internet + ")";
        }
        return strMessage;
    }
}
