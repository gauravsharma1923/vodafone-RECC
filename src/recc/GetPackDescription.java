/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */
package recc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.*;
import java.sql.DriverManager;
import java.util.List;
import java.util.Iterator;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

public class GetPackDescription {
	String strDB_URL;
	String strMAX_DB_CONN;
	String strPASS_URL;
	String strSid;
	static Connection con = null;
	static BasicDataSource ds = null;

	public GetPackDescription(String strDB_URL, String strMAX_DB_CONN,
			String strPASS_URL,String strSid) {
		this.strDB_URL = strDB_URL;
		this.strMAX_DB_CONN = strMAX_DB_CONN;
		this.strPASS_URL = strPASS_URL;
		this.strSid = strSid;
	}

	public BasicDataSource getDbSource() {
		try {
			if (ds == null) {
				String dbResp = sendGet();
				if (dbResp != "-1") {
					ds = new BasicDataSource();
					String dbArray[] = dbResp.split("\\|");
					ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
					ds.setUsername(dbArray[1]);// "ussdmis");
					ds.setPassword(dbArray[2]);// "ussdmis");
					ds.setUrl("jdbc:oracle:thin:@" + strDB_URL);// "jdbc:oracle:thin:@ussd-scan1.vodafone.in:1526/USSD");
					ds.setMaxActive(Integer.parseInt(strMAX_DB_CONN));
					//ds.setMaxActive(10);
					ds.setMaxIdle(5);
					ds.setMaxWait(5);
					System.out.println("Db url==>jdbc:oracle:thin:@"+strDB_URL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}

	public synchronized Connection getConnection() {
		try {

			con = getDbSource().getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	public synchronized void closeDbConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public String getPackDescription(String circleid, String packName) {
		String packDescription = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			con = getConnection();
			
			pstmt = con
					.prepareStatement("select simplifiedname from reccPackDesc where circleid=? and reccpackname=? ");
			pstmt.setQueryTimeout(2);
			pstmt.setString(1, circleid);
			pstmt.setString(2, packName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				packDescription = rs.getString(1);
			}
			//System.out.println("select simplifiedname from reccPackDesc where circleid="+circleid+" and reccpackname="+packName+",output:packDescription="+packDescription);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				
				if (rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
				if(con != null)
					closeDbConnection(con);
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return packDescription;
	}

	public String sendGet() {
		String response = "-1";

		System.out.println("Hit Url to get Db username and password: "+strPASS_URL +"="+strSid);
		try {
			URL obj = new URL(strPASS_URL + "=" + strSid);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setConnectTimeout(2000);
			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response = inputLine;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
