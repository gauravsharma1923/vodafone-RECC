package recc;
import javax.servlet.http.*;
import java.io.*;
public class UpdateFlag extends HttpServlet
{
	protected void doGet(HttpServletRequest request,HttpServletResponse response)
	{
		try{
		PrintWriter out=response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		String strFlag = request.getParameter("Circle");
            if (strFlag != null && strFlag.equalsIgnoreCase("Delhi")) {
                SingleToneAcess.getObject().setDelhiFlagByJsp(true);

                out.println("Now Flag is : " + SingleToneAcess.getObject().isDelhiFlag());
                out.println("Successfully update flag Delhi");
                return;
            }
            if (strFlag != null && strFlag.equalsIgnoreCase("Kolkata")) {
                SingleToneAcess.getObject().setKolkataFlagByJsp(true);
                out.println("Now Flag is : " + SingleToneAcess.getObject().isKolkataFlag());
                out.println("Successfully update flag Kolkata");
                return;
            }
            if (strFlag != null && strFlag.equalsIgnoreCase("Mumbai")) {
                out.println("Now Flag is : " + SingleToneAcess.getObject().isMumbaiFlag());

                SingleToneAcess.getObject().setMumbaiFlagByJsp(true);
                out.println("Successfully update flag Mumbai");
                return;
            }
            if (strFlag != null && strFlag.equalsIgnoreCase("Chennai")) {
                out.println("Now Flag is : " + SingleToneAcess.getObject().isChennaiFlag());

                SingleToneAcess.getObject().setChennaiFlagByJsp(true);
                out.println("Successfully update flag Chennai");
                return;
            }
            out.println("Worng Parameter");
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}


	}
}
