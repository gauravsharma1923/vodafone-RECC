/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recc;

import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author ch-e00246
 */
public class DbOperation {

    public void  upFilureHitCircleWise(String strCircle) {
        Connection conn = DbConnection.getConnection();
        try {
            if (strCircle != null && !strCircle.trim().equals("")) {
                Statement stmt = conn.createStatement();
		System.out.println("Inside DbOperation");
                String strQuery = "insert into vodafoneussd.tbl_failure_circle_hit(Circle,dateTime) values ('" + strCircle + "', now())";
                stmt.executeUpdate(strQuery);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
