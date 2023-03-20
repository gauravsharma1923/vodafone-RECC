/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */
package recc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;
/**
 *  *
 *   * @author CH-E00793
 *    */
public class getRedPlan extends HttpServlet {

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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String strConnType = "";
        String strMsisdn = "";
        String circleId = "";
        String strSessionId = "";
        String strMessage = "";
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
            circleId = request.getParameter("circleId");
            circleId = circleId.trim();
            strSessionId = request.getParameter("sessionId");
            strSessionId = strSessionId.trim();
            strConnType = request.getParameter("connType");
            strConnType = strConnType.trim();

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

            Client1 cl = new Client1(url, username, password, strCON_TimeOut, strSO_TimeOut);
            CustomerDetails objCustomerDetails = cl.getBottomUpHierarchyPostpaidRed(strFlag, strMsisdn, circleId, strSessionId);
            strMessage = objCustomerDetails.getErrorMsg();
            ArrayList<CustomerDetailsBean> listCustomerDetailsBean = objCustomerDetails.getListCustomerDetails();
            if (!listCustomerDetailsBean.isEmpty()) {
                strMessage=strMessage+"|";
                for (CustomerDetailsBean objCustomerDetailsBean : listCustomerDetailsBean) {
                    strMessage = strMessage + objCustomerDetailsBean.getCustomerId() + "~" + objCustomerDetailsBean.getFunctionalName() + "~" + objCustomerDetailsBean.getPackName() + "~" + objCustomerDetailsBean.getTotalQuota() + "~" + objCustomerDetailsBean.getBalance() + "~" + getValidity(objCustomerDetailsBean.getExpiryDate())+ "|";
                }
            }
            Log.getRequestResponseReccStatusLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strConnType+ "," + strMessage + "," + (System.currentTimeMillis() - uniqueNumber), "Response");
            out.println("code=" + strMessage); 
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Log.getRequestResponseReccStatusLog(strFlag, strSessionId + "," + strMsisdn + "," + circleId + "," + strConnType + "," + e.getMessage() + "," + (System.currentTimeMillis() - uniqueNumber), "Response");
            out.println("code=exception");
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
 *      * Handles the HTTP <code>GET</code> method.
 *           *
 *                * @param request servlet request
 *                     * @param response servlet response
 *                          * @throws ServletException if a servlet-specific error occurs
 *                               * @throws IOException if an I/O error occurs
 *                                    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
 *      * Handles the HTTP <code>POST</code> method.
 *           *
 *                * @param request servlet request
 *                     * @param response servlet response
 *                          * @throws ServletException if a servlet-specific error occurs
 *                               * @throws IOException if an I/O error occurs
 *                                    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
 *      * Returns a short description of the servlet.
 *           *
 *                * @return a String containing servlet description
 *                     */
    @Override
    public String getServletInfo() {
        return "Short description";
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
/*    public static String getValidity(long l) {
        Date date = new Date(l);
        DateFormat df = new SimpleDateFormat("dd-MMM-yy");
        return (df.format(date));
    }*/

 public static String getValidity(Long l) {
        String strDate = "";
        DateFormat df = new SimpleDateFormat("dd-MMM-yy");;
        Date receivedDate = new Date(l);
        Calendar calCurrentDate = Calendar.getInstance();
        Calendar calReceivedDate = Calendar.getInstance();
        calReceivedDate.setTime(receivedDate);
        calReceivedDate.add(Calendar.MONTH, -1);
        boolean sameYear = calCurrentDate.get(Calendar.YEAR) == calReceivedDate.get(Calendar.YEAR);
        boolean sameMonth = calCurrentDate.get(Calendar.MONTH) == calReceivedDate.get(Calendar.MONTH);
        boolean sameDay = calCurrentDate.get(Calendar.DAY_OF_MONTH) == calReceivedDate.get(Calendar.DAY_OF_MONTH);

//System.out.println("---receivedDate"+receivedDate+",calReceivedDate="+calReceivedDate +",calCurrentDate="+calCurrentDate);
        if (sameDay && sameMonth && sameYear) {
            Date changeDate = calReceivedDate.getTime();
            strDate = df.format(changeDate);
            System.out.println("change date : "+df.format(changeDate));
        } else {
            strDate = df.format(receivedDate);
            System.out.println("received date : "+df.format(receivedDate));
        }
        return strDate;
    }




}
