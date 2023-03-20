package recc;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

/**
 *
 * @author ch-e00793
 */
public class Log {

    //private static final String strLogDir = "/home/Logs/RECC_111/";

    private synchronized static void writeLog(String strLogDir,String strLogFolder,String logString,String strNode,String fileName) {
        String strFileName = "";
        String strDateDir = "";
        Calendar objCalendarRightNow = Calendar.getInstance();
        int intMonth = objCalendarRightNow.get(Calendar.MONTH) + 1;
        int intDate = objCalendarRightNow.get(Calendar.DATE);
        int intHour = objCalendarRightNow.get(Calendar.HOUR_OF_DAY);
        int intMinute = objCalendarRightNow.get(Calendar.MINUTE);
        int intSecond = objCalendarRightNow.get(Calendar.SECOND);
        String strYear = "" + objCalendarRightNow.get(Calendar.YEAR);
        strDateDir = strLogDir + strLogFolder + "/" + intDate + "-" + intMonth + "-" + strYear;
        createDateDir(strDateDir+"/"+strNode);
        strFileName = strDateDir +"/"+ strNode+"/" + fileName + ".log";
        try {
            FileWriter out = new FileWriter(strFileName, true);
            String strLogString = " " + intHour + ":" + intMinute + ":" + intSecond + "," + logString + "\n";
            out.write(strLogString);
            out.close();
        } catch (Exception ex) {
            System.out.println("System.out.println:" + ex.toString());
            ex.printStackTrace();
        }
    }

    private synchronized static void createDateDir(String dateDir) {
        try {
            String folderName = dateDir;
            new File(folderName).mkdirs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void getInReccLogs(String strNode,String logString,String fileName) {
        writeLog("/home/ussdaps/ussd_log/RECC_111/" ,"InLogs",logString,strNode,fileName);
    }
    public static void getRequestResponseReccLog(String strNode,String logString,String fileName) {
        writeLog("/home/ussdaps/ussd_log/RECC_111/","RequestResponseLog",logString,strNode,fileName);
    }
    public static void getInReccStatusLogs(String strNode,String logString,String fileName) {
        writeLog("/home/ussdaps/ussd_log/RECC_444/" ,"InLogs",logString,strNode,fileName);
    }
    public static void getRequestResponseReccStatusLog(String strNode,String logString,String fileName) {
        writeLog("/home/ussdaps/ussd_log/RECC_444/","RequestResponseLog",logString,strNode,fileName);
    }
}

