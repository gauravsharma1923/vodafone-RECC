/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author ch-e00793
 */
public class getReccStatus extends HttpServlet {

    static HashMap<String, String> map = new HashMap<String, String>();
    // static HashMap<String, Client> mapObj = new HashMap<String, Client>();

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
        String strUrl = "";
        String strSessionId = "";
        String circleId = "";
        String strMsisdn = "";
        String strPackName = "";
        String resMessage = "";
        String strFlag = "";
        String strData = "";
        String[] strArr = new String[5];
        String username = "";
        String password = "";
        String strIP = "";
        String strPort = "";
        String strSO_TimeOut = "";
        String strCON_TimeOut = "";
        long uniqueNumber = 0L;
       try {
            out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            strMsisdn = request.getParameter("msisdn");
            strMsisdn = strMsisdn.trim();
            strPackName = request.getParameter("packName");
            strPackName = strPackName.trim();
            circleId = request.getParameter("circleId");
            circleId = circleId.trim();
            strSessionId = request.getParameter("sessionId");
            strSessionId = strSessionId.trim();

            if (map.isEmpty()) {
                readConfiguration();
            }
            strData = map.get(circleId);
            strCON_TimeOut = map.get("CONNECTION_TIMEOUT");
            strSO_TimeOut = map.get("SO_TIMEOUT");
            strArr = strData.split("~");
            strFlag = strArr[0];
            username = strArr[1];
            password = strArr[2];
            strIP = strArr[3];
            strPort = strArr[4];

            uniqueNumber = System.currentTimeMillis();
            Log.getRequestResponseReccStatusLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strPackName, "Request");

            strUrl = "http://" + strIP + ":" + strPort + "/services/TisService";

            System.out.println("Node ::" + strFlag + " Url ::" + strUrl);

            Client1 cl = new Client1(strUrl, username, password, strCON_TimeOut, strSO_TimeOut);
            resMessage = cl.getDataPackStatus(strFlag, strMsisdn, circleId, strPackName, strSessionId);
            out.println("msg=" + resMessage);
        } catch (Exception e) {
            e.printStackTrace();
            resMessage = e.getMessage();
            Log.getRequestResponseReccStatusLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strPackName + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Error");
            System.out.println("EXCEPTION  ::" + e.getMessage());
            out.println("msg=error");
        } finally {
            out.flush();
            out.close();
        }
        Log.getRequestResponseReccStatusLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strPackName + "," + resMessage + "," + (System.currentTimeMillis() - uniqueNumber), "Response");
    }
}

