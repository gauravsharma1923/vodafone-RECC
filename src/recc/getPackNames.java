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

public class getPackNames extends HttpServlet {

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
        String strFlag = "";
        String strSessionId = "";
        String strMessage = "";
        String url = "";
        String strData = "";
        String[] strArr = new String[5];
        String strBL_Packs = "";
        String username = "";
        String password = "";
        String strIP = "";
        String strPort = "";
        String strSO_TimeOut = "";
        String strCON_TimeOut = "";
        long uniqueNumber = 0L;
        Client1 cl = null;
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
            Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strReqType + "," + strMsisdn + "," + circleId + "," + strConnType, "Request");

            url = "http://" + strIP + ":" + strPort + "/services/TisService";
            System.out.println("Node ::" + strFlag + " Url ::" + url);

            if (strReqType.equals("201")) {
                cl = new Client1(url, username, password, strCON_TimeOut, strSO_TimeOut);
                ArrayList<String> resMessage = cl.getActiveDataPacks(strFlag, strMsisdn, strReqType, circleId, strSessionId);
                if (resMessage.isEmpty()) {
                    out.print("msg=no_data");
                } else if (resMessage.get(0).equals("exception")) {
                    out.print("msg=exception");
                } else {
                    for (int i = 0; i < resMessage.size(); i++) {
                        strPackName = resMessage.get(i);
                        if (!isCheckBlackListedPacks(strPackName,strBL_Packs)) {
                            if (!(strPackName.endsWith("Usage")) && !(strPackName.endsWith("Usage1"))){
                                   strMessage = strMessage + strPackName + ",";
                            } 
                        }
                    }
                    if (strMessage == "") {
                        strMessage = "no_data";
                    } else {
                        strMessage = strMessage.substring(0, strMessage.length() - 1);
                    }
                    out.println("msg=" + strMessage);
                    Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strReqType + "," + strMsisdn + "," + circleId + "," + strConnType + "," + strMessage.trim() + "," + (System.currentTimeMillis() - uniqueNumber), "Response");
                }
            }
        } catch (Exception e) {
            Log.getRequestResponseReccLog(strFlag, strSessionId + "," + strReqType + "," + strMsisdn + "," + circleId + "," + strConnType + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Error");
            e.printStackTrace();
            System.out.println("EXCEPTION ::" + e.getMessage());
            out.println("msg=exception");
        } finally {
            out.flush();
            out.close();
            cl.close_connection();
        }
    }

    public boolean isCheckBlackListedPacks(String strPackName, String strBL_Packs) {
        String[] arrBLPacks = null;
        boolean booleanFlag = false;
        if (strBL_Packs != null) {
            arrBLPacks = strBL_Packs.split("~");
            for (int i = 0; i
                    < arrBLPacks.length; i++) {
                if (strPackName != null && arrBLPacks[i].equalsIgnoreCase(strPackName)) {
                    booleanFlag = true;
                }
            }
        }
        return booleanFlag;
    }
}

