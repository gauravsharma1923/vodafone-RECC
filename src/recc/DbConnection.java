/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recc;

/**
 *
 * @author ch-e00246
 */
import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {

    public DbConnection() {
    }
  
    public static synchronized Connection getConnection() {
        Connection objConnection = null;
        try {
                Class.forName("com.mysql.jdbc.Driver");
                objConnection = DriverManager.getConnection("jdbc:mysql://10.210.9.32:5122/vodafoneussd", "vfadmin", "pwd9apr!3");
                System.out.println("----------------------------------------------------------------");
                System.out.println("-----------------Connected with  DataBase  ---------------------");
                System.out.println("----------------------------------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objConnection;
    }
}
