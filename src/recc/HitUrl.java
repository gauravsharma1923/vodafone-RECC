/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recc;

//import com.cellebrum.vodafone.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HitUrl {

        public String urlResponse(String strUrl) {
        String strOutPut = "", strUrlBeforEncoding = "", strInputLine = "";
        BufferedReader in = null;

        try 
	{
            strUrl = strUrl.replaceAll(" ", "%20");
            strUrlBeforEncoding = strUrl;
            System.out.println("Going to Hit URL ================" + strUrl);
            URL url = new URL(strUrl);
            URLConnection urlconn = url.openConnection();
            urlconn.setConnectTimeout(5000);
            in = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
            while ((strInputLine = in.readLine()) != null) {
                strOutPut += strInputLine;
            }
            strOutPut = strOutPut.trim();
        } 
	catch (Exception e) 
	{
            e.printStackTrace();
            return "";
        }
        System.out.println("strOutPut ================" + strOutPut);
        return strOutPut;
    }

    public static void main(String args[]) 
    {
	HitUrl obj=new HitUrl();	
        String strResp=obj.urlResponse("http://localhost:8485/axis/Index.jsp");
	System.out.println("Response ::"+strResp);

    }
}
