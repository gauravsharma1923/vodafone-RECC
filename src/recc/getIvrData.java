package recc;

import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

public class getIvrData extends HttpServlet {
	static HashMap<String, String> map = new HashMap<String, String>();
	static HashMap<String, String> channel = new HashMap<String, String>();
	static HashMap<String, String> use = new HashMap<String, String>();

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
		{
			PrintWriter out = null;
			String strMessage = "";
			String url = "";
			String strReqType = "";
			String strMsisdn = "";
			String circleId = "";
			String strConnType = "";
			String strSessionId = "";
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
			String strDB_URL = "";
			String strMAX_DB_CONN = "";
			String strPASS_URL = "";
			String strSid = "";
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

				if (channel.isEmpty() || use.isEmpty()) {
					Hash objHash = new Hash();
					channel = objHash.channel();
					use = objHash.use();
				}

				strData = map.get(circleId);
				strCON_TimeOut = map.get("CONNECTION_TIMEOUT");
				strSO_TimeOut = map.get("SO_TIMEOUT");
				strDB_URL = map.get("DB_URL");
				strMAX_DB_CONN = map.get("MAX_DB_CONN");
				strPASS_URL = map.get("PASS_URL");
				strBL_Packs = map.get("BL_PACKS");
				strSid = map.get("DB_SID");
				strArr = strData.split("~");
				strFlag = strArr[0];
				username = strArr[1];
				password = strArr[2];
				strIP = strArr[3];
				strPort = strArr[4];

				uniqueNumber = System.currentTimeMillis();
				Log.getRequestResponseReccLog(strFlag,
						strSessionId + ",IvrData," + strMsisdn + "," + circleId + ","
								+ strReqType + "," + strConnType, "Request");

				url = "http://" + strIP + ":" + strPort + "/services/TisService";
				System.out.println("IvrData-->Circle ::" + strFlag + ", Url ::" + url);

				if (strConnType.equalsIgnoreCase("R")) {
					if (strReqType.equals("100")) {
						String strPack = "";
						Client1 cl = new Client1(url, username, password, strCON_TimeOut,
								strSO_TimeOut);
						try {
							ArrayList<String> resMessage = cl.getActiveDataPacks(strFlag,
									strMsisdn, strReqType, circleId, strSessionId);
							if (resMessage.isEmpty()) {
								out.print("msg=no_data");
								strMessage = "No Data";
								strPack = strMessage;
							} else if (resMessage.get(0).equals("exception")) {
								out.print("msg=exception");
								strMessage = "exception";
								strPack = strMessage;
							} else {
								int j = 0;
								for (int i = 0; i < resMessage.size(); i++) {
									try {
										strPack = strPack + resMessage.get(i) + ",";
										String strPackName = resMessage.get(i);
										if (!isCheckBlackListedPacks(strPackName, strBL_Packs)) {
											GetPackDescription objGetPackDescription = new GetPackDescription(
													strDB_URL, strMAX_DB_CONN, strPASS_URL, strSid);
											String packDesc = objGetPackDescription
													.getPackDescription(circleId, strPackName);
											j = j + 1;
											if (packDesc == "") {

												if (strPackName.startsWith("RDn")
														|| strPackName.startsWith("RD")) {
													strPackName.replace("RDn", "R");
													strPackName.replace("RD", "R");
												} else if (strPackName.startsWith("SDn")
														|| strPackName.startsWith("SD")) {
													strPackName.replace("SDn", "S");
													strPackName.replace("SD", "S");
												}

												Character first = strPackName.charAt(0);
												if (channel.containsKey(first.toString())) {
													Character second = strPackName.charAt(1);
													int temp;
													String packType = "";
													String validity = "";
													String rental = strPackName.substring(
															strPackName.lastIndexOf("R") + 1,
															strPackName.lastIndexOf("D"));
													String data = "";
													if (use.containsKey(second.toString())) // I(mobile
													// internet),B(Mobile
													// broadband),P(Shared
													// parent),C(shared
													// child)
													{
														if (strPackName.contains("V")) {
															temp = strPackName.lastIndexOf("V") - 2;
															packType = strPackName.substring(temp,
																	strPackName.lastIndexOf("V"));
														}

														if (use.containsKey(packType)) // 2g,3g,wifi
														{
															validity = strPackName.substring(
																	strPackName.indexOf("V") + 1,
																	strPackName.lastIndexOf("R"));
															/*
															 * System.out.println("validity======= from RECC-->"
															 * + validity);
															 */
															if (validity != null
																	|| (!validity.equalsIgnoreCase("")))// validity
															// check
															{
																if (rental != null
																		|| (!rental.equalsIgnoreCase("")))// rental
																// check
																{

																	if (strPackName.contains("T"))// data
																	// check
																	{
																		strPackName = strPackName.substring(
																				strPackName.lastIndexOf("T"),
																				strPackName.length());
																		strPackName = strPackName.replace("T", "D");
																		data = packDesc(strPackName);
																		strMessage = strMessage
																				+ (j + "." + use.get(second.toString())
																						+ " " + data + " Minutes ("
																						+ use.get(packType) + ") .~");
																	}

																	else if (strPackName.contains("D"))// data
																	// check
																	{
																		data = packDesc(strPackName);
																		strMessage = strMessage
																				+ (j + "." + use.get(second.toString())
																						+ " " + data + " ("
																						+ use.get(packType) + ") .~");

																	}
																}

															} else// volume add
															// on packs
															{
																data = packDesc(strPackName);
																strMessage = strMessage
																		+ (j + "." + use.get(second.toString())
																				+ " " + data + " (" + use.get(packType) + ") .~");
															}
														} else// rat products
														{
															data = packDesc(strPackName);
															strMessage = strMessage
																	+ (j + "." + use.get(second.toString()) + " "
																			+ data + ".~");
														}
													} else if (strPackName.startsWith("BOOST"))// boost
													// packs
													{

														data = packDesc(strPackName);
														strMessage = strMessage
																+ (j + ".Mobile Internet " + data
																		+ "(Booster Pack)" + ".~");
													}
												}
											}

											else {
												strMessage += j
														+ "."
														+ objGetPackDescription.getPackDescription(
																circleId, strPackName) + ".~ ";
											}

										}
									}

									catch (Exception e) {
										e.printStackTrace();
										System.out.println("EXCEPTION ::" + e.getMessage());
										Log.getRequestResponseReccLog(strFlag, strSessionId + ","
												+ strMsisdn + "," + circleId + "," + strReqType + ","
												+ strConnType + "," + strPack + "," + e.getMessage()
												+ "," + (System.currentTimeMillis() - uniqueNumber),
												"Error");
									}
								}
								if (strMessage == "") {
									strMessage = "no_data";
								}
								out.println("msg=" + strMessage);
								Log.getRequestResponseReccLog(strFlag, strSessionId + ","
										+ strMsisdn + "," + circleId + "," + strReqType + ","
										+ strConnType + "," + strPack + "," + strMessage.trim()
										+ "," + (System.currentTimeMillis() - uniqueNumber),
										"Response");

							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("EXCEPTION ::" + e.getMessage());
							out.println("msg=exception");
							Log.getRequestResponseReccLog(strFlag, strSessionId + ","
									+ strMsisdn + "," + circleId + "," + strReqType + ","
									+ strConnType + "," + strPack + "," + e.getMessage() + ","
									+ (System.currentTimeMillis() - uniqueNumber), "Error");
						} finally {
							out.flush();
							out.close();
							cl.close_connection();
						}
					}

					if (strReqType.equals("101")) {
						Client1 cl = new Client1(url, username, password, strCON_TimeOut,
								strSO_TimeOut);
						CustomerDetails objCustomerDetails = null;
						String strPack = "";
						try {
							objCustomerDetails = cl.getBottomUpHierarchyPrepaid(strFlag,
									strMsisdn, strReqType, circleId, strSessionId);
							String errorMsg = objCustomerDetails.getErrorMsg();
							if (errorMsg.equals("no_data")) {
								out.print("msg=no_data");
								strMessage = "No Data";
								strPack = strMessage;
							} else if (errorMsg.equals("exception")) {
								out.print("msg=exception");
								strMessage = "exception";
								strPack = strMessage;

							} else if (errorMsg.equals("success")) {
								int j = 0;
								String strPackName = "";
								ArrayList<CustomerDetailsBean> arrCustomerDetailsBean = objCustomerDetails
										.getListCustomerDetails();
								for (CustomerDetailsBean objCustomerDetailsBean : arrCustomerDetailsBean) {
									try {
										strPackName = objCustomerDetailsBean.getPackName();

										if (!isCheckBlackListedPacks(strPackName, strBL_Packs)) {
											j = j + 1;

											String validity = getValidity(objCustomerDetailsBean
													.getExpiryDate());

											long balance = objCustomerDetailsBean.getBalance();
											String functionalName = objCustomerDetailsBean
													.getFunctionalName();
											strPack = strPack + strPackName + ",";

											boolean QoS = false;
											if (functionalName.equals("RPP_s_QoSOnTotalUsage")) {
												QoS = true;
											} else {
												QoS = false;
											}
											GetPackDescription objGetPackDescription = new GetPackDescription(
													strDB_URL, strMAX_DB_CONN, strPASS_URL, strSid);

											String packDescription = strPack(strPackName, circleId,
													objGetPackDescription);
											System.out.println("Packname==>"+strPackName+",Circle==>"+circleId+",PackDescription==>"+packDescription);
											if (strPackName.startsWith("RDn")
													|| strPackName.startsWith("RD")) {
												strPackName.replace("RDn", "R");
												strPackName.replace("RD", "R");
											} else if (strPackName.startsWith("SDn")
													|| strPackName.startsWith("SD")) {
												strPackName.replace("SDn", "S");
												strPackName.replace("SD", "S");
											}

											Character first = strPackName.charAt(0);
											if (channel.containsKey(first.toString())) {
												Character second = strPackName.charAt(1);
												int temp;
												String packType = "", rental = "";
												String tempValidity = "";
												double accBalance;
												try {
													rental = strPackName.substring(
															strPackName.lastIndexOf("R") + 1,
															strPackName.lastIndexOf("D"));
												} catch (StringIndexOutOfBoundsException ex) {
													rental = strPackName.substring(
															strPackName.lastIndexOf("R") + 1,
															strPackName.lastIndexOf("T"));
												}
												String data = "";
												if (use.containsKey(second.toString())) // I(mobile
												// internet),B(Mobile
												// broadband),P(Shared
												// parent),C(shared
												// child)
												{
													if (strPackName.contains("V")) {
														temp = strPackName.lastIndexOf("V") - 2;
														packType = strPackName.substring(temp,
																strPackName.lastIndexOf("V"));
													}

													if (use.containsKey(packType)) // 2g,3g,wifi
													{
														tempValidity = strPackName.substring(
																strPackName.indexOf("V") + 1,
																strPackName.lastIndexOf("R"));
														if (tempValidity != null
																|| (!tempValidity.equalsIgnoreCase("")))// validity
														// check
														{
															if (rental != null
																	|| (!rental.equalsIgnoreCase("")))// rental
															// check
															{

																if (strPackName.contains("T"))// data
																// check
																{
																	// strPackName=strPackName.substring(strPackName.lastIndexOf("T"),strPackName.length());
																	// strPackName=strPackName.replace("T","D");
																	if (strPackName.contains("N")) {

																		// Data$PackName$Pack
																		// Desc$Total
																		// Data$Data
																		// Consumed$Pack
																		// Expiry
																		if (strPackName.contains("UL")) {
																			System.out
																					.println("*"
																							+ strPackName
																							+ ":"
																							+ getBalanceOfDuration(balance / 1024)
																							+ ":"
																							+ validity
																							+ ":"
																							+ fetchMrp(strPackName));

																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^unlimited^ "
																							+ getBalanceOfDuration(balance / 1024)
																							+ "^" + validity + "^" + fetchMrp(strPackName));
																		} else {
																			accBalance = getBalanceOfDuration(balance);
																			// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																			// /
																			// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																			// +
																			// validity
																			// +
																			// ".~ ");
																			String arr[] = usuage(strPackName,
																					accBalance).split("~");
																			// System.out.println("testtt"+arr[1]);
																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "^"
																							+ arr[0]
																							+ "^"
																							+ getBalanceOfDuration(balance / 1024)
																							+ "MB^ " + validity + "^" + fetchMrp(strPackName));
																			
																		}
																	} else if (strPackName.contains("UL")) {
																		

																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^unlimited"
																						+ "^"
																						+ getBalanceOfDuration(balance / 1024)
																						+ "^ " + validity + "^" + fetchMrp(strPackName));

																	} else if (strPackName.contains("D")) {
																		accBalance = getBalanceOfDuration(balance);
																		// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB"+"."+use.get(packType)+" Pack will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																		String arr[] = usuage(strPackName,
																				accBalance).split("~");

																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^"
																						+ arr[0]
																						+ "^ "
																						+ getBalanceOfDuration(balance / 1024)
																						+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																	} else {
																		String remaining_bal = getBalanceOfVolume(balance);
																		// strMessage=strMessage+(j+".Hi, You have "+remaining_bal+" time left for free data usage in your Current "+use.get(packType)+" Hourly Pack .~ ");

																		// String
																		// arr[]=usuage(strPackName,accBalance).split("~");

																		strMessage = strMessage
																				+ ("$" + strPackName + "^"
																						+ packDescription + "^Hourly Pack"
																						+ "^" + remaining_bal + "^ "
																						+ validity + "^" + fetchMrp(strPackName));
																	}

																} else if (strPackName.contains("C"))// data
																// check
																{
																	if (strPackName.contains("N")) {
																		if (strPackName.contains("UL")) {
																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "^unlimited"
																							+ "^"
																							+ getBalanceOfDuration(balance / 1024)
																							+ "^ " + validity + "^" + fetchMrp(strPackName));

																		} else {
																			accBalance = getBalanceOfDuration(balance);
																			// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																			// /
																			// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																			// +
																			// validity
																			// +
																			// ".~ ");
																			String arr[] = usuage(strPackName,
																					accBalance).split("~");

																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "^"
																							+ arr[0]
																							+ "^ "
																							+ getBalanceOfDuration(balance / 1024)
																							+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																		}
																	} else if (strPackName.contains("UL")) {
																		// strMessage=strMessage+(j+".Hi, You have consumed "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB from your "+use.get(packType)+" Unlimited Pack.It will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																		// String
																		// arr[]=usuage(strPackName,accBalance).split("~");

																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "$Unlimiited Pack"
																						+ "^"
																						+ getBalanceOfDuration(balance / 1024)
																						+ "^ " + validity + "^" + fetchMrp(strPackName));

																	}

																	else {
																		accBalance = getBalanceOfDuration(balance);
																		String arr[] = usuage(strPackName,
																				accBalance).split("~");

																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^"
																						+ arr[0]
																						+ "^ "
																						+ getBalanceOfDuration(balance / 1024)
																						+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																		// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB"+"."+use.get(packType)+" Pack will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																	}

																} else if (strPackName.contains("DB"))// data
																// check
																{
																	if (strPackName.contains("N")) {
																		if (strPackName.contains("UL")) {
																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "$Unlimiited Pack"
																							+ "^"
																							+ getBalanceOfDuration(balance / 1024)
																							+ "^ " + validity + "^" + fetchMrp(strPackName));
																		} else {
																			accBalance = getBalanceOfDuration(balance);
																			// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																			// /
																			// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																			// +
																			// validity
																			// +
																			// ".~ ");
																			String arr[] = usuage(strPackName,
																					accBalance).split("~");

																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "^"
																							+ arr[0]
																							+ "^ "
																							+ getBalanceOfDuration(balance / 1024)
																							+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																		}
																	} else if (strPackName.contains("UL")) {

																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "Unlimiited Pack"
																						+ "^"
																						+ getBalanceOfDuration(balance / 1024)
																						+ "^ " + validity + "^" + fetchMrp(strPackName));

																		// strMessage=strMessage+(j+".Hi, You have consumed "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB from your "+use.get(packType)+" Unlimited Pack.It will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																	}

																	else {
																		accBalance = getBalanceOfDuration(balance);
																		String arr[] = usuage(strPackName,
																				accBalance).split("~");
																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^"
																						+ arr[0]
																						+ "^"
																						+ getBalanceOfDuration(balance / 1024)
																						+ "MB ^" + validity + "^" + fetchMrp(strPackName));

																		// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB"+" "+use.get(packType)+" Pack will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																	}
																} else if (strPackName.contains("D"))// data
																// check
																{
																	/*
																	 * if (strPackName . contains( "P")) { String
																	 * ch= strPackName . subString ( strPackName .
																	 * lastIndexOf ("D")+1, strPackName .
																	 * lastIndexOf ("P")-1); String p_last_ch =
																	 * strPackName . subString ( strPackName .
																	 * lastIndexOf ("P"), strPackName .
																	 * lastIndexOf ("P")+1); }
																	 */
																	if (strPackName.contains("N")) {
																		if (strPackName.contains("UL")) {
																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "$Unlimiited Pack"
																							+ "^"
																							+ getBalanceOfDuration(balance / 1024)
																							+ "^ " + validity + "^" + fetchMrp(strPackName));
																		} else {
																			accBalance = getBalanceOfDuration(balance);
																			String arr[] = usuage(strPackName,
																					accBalance).split("~");
																			strMessage = strMessage
																					+ ("$"
																							+ strPackName
																							+ "^"
																							+ packDescription
																							+ "^"
																							+ arr[0]
																							+ "^ "
																							+ getBalanceOfDuration(balance / 1024)
																							+ "MB ^" + validity + "^" + fetchMrp(strPackName));

																			// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																			// /
																			// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																			// +
																			// validity
																			// +
																			// ".~ ");
																		}
																	} else if (strPackName.contains("UL")) {
																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^Unlimiited Pack"
																						+ "^"
																						+ getBalanceOfDuration(balance / 1024)
																						+ "^ " + validity + "^" + fetchMrp(strPackName));

																		// strMessage=strMessage+(j+".Hi, You have consumed "+getBalanceOfDuration(balance
																		// /
																		// 1024)+"MB from your "+use.get(packType)+" Unlimited Pack.It will expire on "
																		// +
																		// validity
																		// +
																		// ".~ ");
																	}

																	else {
																		accBalance = getBalanceOfDuration(balance);
																		

																		String arr[] = usuage(strPackName,
																				accBalance).split("~");
																		strMessage = strMessage
																				+ ("$"
																						+ strPackName
																						+ "^"
																						+ packDescription
																						+ "^"
																						+ arr[0]
																						+ "^ "
																						+ getBalanceOfDuration(balance / 1024)
																						+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																		/*
																		 * System.out
																		 * .println("welcometest####packname:" +
																		 * strPackName + "packDescription:" +
																		 * packDescription + "arr[0]:" + arr[0] +
																		 * "getBalanceOfDuration:" +
																		 * getBalanceOfDuration(balance / 1024) +
																		 * "validityvalidity:" + validity +
																		 * "fetchMrp:" + fetchMrp(strPackName) +
																		 * "strMessage:" + strMessage);
																		 */

																	}
																}
															}

														}

														else// volume add on
														// packs
														{
															if (strPackName.contains("N")) {
																if (strPackName.contains("UL")) {

																	strMessage = strMessage
																			+ ("$"
																					+ strPackName
																					+ "^"
																					+ packDescription
																					+ "^Unlimiited Pack"
																					+ "^"
																					+ getBalanceOfDuration(balance / 1024)
																					+ "^ " + validity + "^" + fetchMrp(strPackName));
																} else {
																	accBalance = getBalanceOfDuration(balance);
																	String arr[] = usuage(strPackName, accBalance)
																			.split("~");
																	strMessage = strMessage
																			+ ("$"
																					+ strPackName
																					+ "^"
																					+ packDescription
																					+ "^"
																					+ arr[0]
																					+ "^ "
																					+ getBalanceOfDuration(balance / 1024)
																					+ "MB ^" + validity + "^" + fetchMrp(strPackName));

																	// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																	// /
																	// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																	// +
																	// validity
																	// + ".~ ");
																}
															} else if (strPackName.contains("UL")) {

																strMessage = strMessage
																		+ ("$" + strPackName + "^"
																				+ packDescription + "^Unlimiited Pack"
																				+ "^"
																				+ getBalanceOfDuration(balance / 1024)
																				+ "^ " + validity + "^" + fetchMrp(strPackName));
																// strMessage=strMessage+(j+".Hi, You have consumed "+getBalanceOfDuration(balance
																// /
																// 1024)+"MB from your "+use.get(packType)+" Unlimited Pack.It will expire on "
																// + validity +
																// ".~ ");
															}

															else {
																accBalance = getBalanceOfDuration(balance);
																String arr[] = usuage(strPackName, accBalance)
																		.split("~");

																strMessage = strMessage
																		+ ("$" + strPackName + "^"
																				+ packDescription + "^" + arr[0] + "^ "
																				+ getBalanceOfDuration(balance / 1024)
																				+ "MB ^" + validity + "^" + fetchMrp(strPackName));

																// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																// /
																// 1024)+"MB"+"."+use.get(packType)+" Pack will expire on "
																// + validity +
																// ".~ ");
															}
														}
													} else// rat products
													{
														if (strPackName.contains("N")) {
															if (strPackName.contains("UL")) {
																strMessage = strMessage
																		+ ("$" + strPackName + "^"
																				+ packDescription + "^Unlimiited Pack"
																				+ "^"
																				+ getBalanceOfDuration(balance / 1024)
																				+ "^ " + validity + "^" + fetchMrp(strPackName));
															} else {
																accBalance = getBalanceOfDuration(balance);

																String arr[] = usuage(strPackName, accBalance)
																		.split("~");
																strMessage = strMessage
																		+ ("$" + strPackName + "^"
																				+ packDescription + "^" + arr[0] + "^ "
																				+ getBalanceOfDuration(balance / 1024)
																				+ "MB ^" + validity + "^" + fetchMrp(strPackName));
																// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
																// /
																// 1024)+"MB"+"."+use.get(packType)+" Night Pack will expire on "
																// + validity +
																// ".~ ");
															}
														} else if (strPackName.contains("UL")) {
															strMessage = strMessage
																	+ ("$" + strPackName + "^" + packDescription
																			+ "^Unlimiited Pack" + "^"
																			+ getBalanceOfDuration(balance / 1024)
																			+ "^ " + validity + "^" + fetchMrp(strPackName));
															// strMessage=strMessage+(j+".Hi, You have consumed "+getBalanceOfDuration(balance
															// /
															// 1024)+"MB from your "+use.get(packType)+" Unlimited Pack.It will expire on "
															// + validity +
															// ".~ ");
														}

														else /*
																	 * else for R/N after validity e.g. U after
																	 * Vaidity
																	 */
														{
															accBalance = getBalanceOfDuration(balance);
															String arr[] = usuage(strPackName, accBalance)
																	.split("~");
															strMessage = strMessage
																	+ ("$" + strPackName + "^" + packDescription
																			+ "^" + arr[0] + "^ "
																			+ getBalanceOfDuration(balance / 1024)
																			+ "MB ^" + validity + "^" + fetchMrp(strPackName));

															// strMessage=strMessage+(j+".Hi, Your data Usage is "+usuage(strPackName,accBalance)+" & Balance is "+getBalanceOfDuration(balance
															// /
															// 1024)+"MB"+"."+use.get(packType)+" Pack will expire on "
															// + validity +
															// ".~ ");
														}

													}
												} else if (strPackName.startsWith("BOOST"))// boost
												// packs
												{
													accBalance = getBalanceOfDuration(balance);

													String arr[] = usuage(strPackName, accBalance).split(
															"~");
													strMessage = strMessage
															+ ("$" + strPackName + "^" + packDescription
																	+ "^" + arr[0] + "^ "
																	+ getBalanceOfDuration(balance / 1024)
																	+ "MB ^" + validity + "^" + fetchMrp(strPackName));

													// strMessage=strMessage+(j+".Hi, Your data balance is "+getBalanceOfDuration(balance
													// /
													// 1024)+"MB"+" for Booster Pack will expire on "
													// + validity + ".~ ");
												}
											}

										}

									} catch (Exception e) {
										e.printStackTrace();
										System.out.println("EXCEPTION ::" + e.getMessage());
										Log.getRequestResponseReccLog(strFlag, strSessionId
												+ ",IvrData," + strMsisdn + "," + circleId + ","
												+ strReqType + "," + strConnType + "," + strPack + ","
												+ e.getMessage() + ","
												+ (System.currentTimeMillis() - uniqueNumber), "Error");
									}
								}
								if (strMessage == "") {
									strMessage = "no_data";
								}
								out.println("msg=" + strMessage);
								Log.getRequestResponseReccLog(strFlag, strSessionId
										+ ",IvrData," + strMsisdn + "," + circleId + ","
										+ strReqType + "," + strConnType + "," + strPack + ","
										+ strMessage.trim() + ","
										+ (System.currentTimeMillis() - uniqueNumber), "Response");
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("EXCEPTION ::" + e.getMessage());
							out.println("exception");
							Log.getRequestResponseReccLog(strFlag, strSessionId + ",IvrData,"
									+ strMsisdn + "," + circleId + "," + strReqType + ","
									+ strConnType + "," + strPack + "," + e.getMessage() + ","
									+ (System.currentTimeMillis() - uniqueNumber), "Error");
						} finally {
							out.flush();
							out.close();
							cl.close_connection();
						}
					}
				} else if (strConnType.equalsIgnoreCase("O")) {
					if (strReqType.equals("100")) {
						String res = "";
						CustomerDetails objCustomerDetails = null;
						Client1 cl = new Client1(url, username, password, strCON_TimeOut,
								strSO_TimeOut);
						String strPack = "";
						boolean bFLAG = false;
						try {
							objCustomerDetails = cl.getBottomUpHierarchyPostpaid(strFlag,
									strMsisdn, strReqType, circleId, strSessionId);
							String resMessage = objCustomerDetails.getErrorMsg();
							if (resMessage.equals("no_data")) {
								out.print("msg=no_data");
								strMessage = "No Data";
								strPack = strMessage;
							} else if (resMessage.equals("exception")) {
								out.print("msg=exception");
								strMessage = "exception";
								strPack = strMessage;
							} else if (resMessage.equals("success")) {
								int j = 0;
								ArrayList<CustomerDetailsBean> arrCustomerDetailsBean = objCustomerDetails
										.getListCustomerDetails();
								Collections.reverse(arrCustomerDetailsBean);
								for (CustomerDetailsBean objCustomerDetailsBean : arrCustomerDetailsBean) {
									try {
										String strPackName = objCustomerDetailsBean.getPackName();
										if (!(strPackName.endsWith("Usage"))
												&& !(strPackName.endsWith("Usage1"))) {
											if (!isCheckBlackListedPacks(strPackName, strBL_Packs)) {
												j = j + 1;
												int start_point = 0, end_point = 0, start_index = 0;
												String mobile_internet = "", conn_type = "", lastDigit = "";
												double balance = 0.0;
												String validity = getValidityPostpaid(objCustomerDetailsBean
														.getExpiryDate());
												long lBalance = objCustomerDetailsBean.getBalance();
												int totalQuota = objCustomerDetailsBean.getTotalQuota();
												System.out.println("balance ---- " + validity + ","
														+ lBalance + "," + totalQuota);
												String functionalName = objCustomerDetailsBean
														.getFunctionalName();
												strPack = strPack + strPackName + ",";

												char firstDigit = strPackName.charAt(0);
												char secondDigit = strPackName.charAt(1);
												char thirdDigit = strPackName.charAt(2);
												char fourthDigit = strPackName.charAt(3);
												if (secondDigit == 'I') {
													conn_type = "Mobile Internet";
													start_point = strPackName.indexOf("G") - 1;
													end_point = strPackName.indexOf("G") + 1;
												} else if (secondDigit == 'B') {
													conn_type = "Mobile Broadband";
													start_point = strPackName.indexOf("G") - 1;
													end_point = strPackName.indexOf("G") + 1;
												} else if (secondDigit == 'D') {
													conn_type = "Double Dhamaal";
													start_point = strPackName.indexOf("G") - 1;
													end_point = strPackName.indexOf("G") + 1;
												} else if (firstDigit == 'B' && secondDigit == 'O') {
													conn_type = "Mobile Internet";
													start_point = strPackName.indexOf("G") - 1;
													end_point = strPackName.indexOf("G") + 1;
												} else if (firstDigit == 'F' || firstDigit == 'D') {
													conn_type = "Mobile Internet";
													start_point = strPackName.indexOf("G") - 1;
													end_point = strPackName.indexOf("G") + 1;
												}
												if (start_point < 0 || end_point < 0) {
													start_point = 0;
													end_point = 0;
												}
												mobile_internet = strPackName.substring(start_point,
														end_point);
												boolean isNumeric = false, flag = false;
												String rental = "";
												for (int pos = end_point; pos < strPackName.length(); pos++) {
													char FirstChar = strPackName.charAt(pos);
													isNumeric = Character.isDigit(FirstChar);
													if (isNumeric == true) {
														flag = true;
														rental = rental + FirstChar;
													} else if (FirstChar == 'P' && flag == true) {
														rental = rental + ".";
													} else if (isNumeric == false) {
														if (flag == true) {
															start_index = pos;
															break;
														} else {
															continue;
														}
													}
												}
												char last_digit = strPackName.charAt(strPackName
														.length() - 1);
												char second_last = strPackName.charAt(strPackName
														.length() - 2);
												lastDigit = strPackName.substring(start_index,
														strPackName.length());
												strMessage = j + "." + conn_type + " "
														+ mobile_internet + " " + rental;
												balance = getBalanceOfDuration(lBalance / 1024);
												if (secondDigit == 'I' && thirdDigit == 'W'
														&& fourthDigit == 'F') {
													if ((second_last == 'U' && last_digit == 'L')
															|| (second_last == 'F' && last_digit == 'U')) {
														strMessage = j
																+ "."
																+ "You have consumed "
																+ balance
																+ " MB (approx) from your unlimited WiFi Internet pack."
																+ "~ ";
														res = res + strMessage;
													} else {
														strMessage = j + "." + conn_type + " "
																+ "Wifi Balance" + " " + balance + "MB" + ".~ ";
														res = res + strMessage;
													}
												} else if (secondDigit == 'D') {
													strMessage = strMessage + " " + lastDigit
															+ " Balance " + balance + "MB" + ".~ ";
													res = res + strMessage;
												} else if (firstDigit == 'F' || firstDigit == 'D') {
													strMessage = j + ".Balance in your " + conn_type
															+ " pack is " + balance + "MB" + " valid till "
															+ validity + ".~ ";
													res = res + strMessage;
												} else if (functionalName
														.equals("RPP_s_QoSOnTotalUsage")) {
													String strPackType = "";
													if (strPackName.startsWith("BOS")) {
														strPackType = balance + " MB(aprox) from your "
																+ mobile_internet + " Booster pack";
													} else {
														strPackType = balance + " MB(aprox) from your "
																+ mobile_internet + " Unlimited Internet Pack";
													}
													if (bFLAG) {
														String[] arr = new String[2];
														arr = res.split("till");
														res = arr[0] + "and " + strPackType + " till"
																+ arr[1];
														strMessage = "";
													} else {
														strMessage = j + "." + "You have consumed "
																+ strPackType
																+ " till date within this bill cycle." + "~ ";
													}
													bFLAG = true;
													res = res + strMessage;
												} else {
													strMessage = strMessage + " Balance " + balance
															+ "MB" + ".~ ";
													res = res + strMessage;
												}
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
										System.out.println("EXCEPTION ::" + e.getMessage());
										Log.getRequestResponseReccLog(strFlag, strSessionId + ","
												+ strMsisdn + "," + circleId + "," + strReqType + ","
												+ strConnType + "," + strPack + "," + e.getMessage()
												+ "," + (System.currentTimeMillis() - uniqueNumber),
												"Error");
									}
								}
								if (res == "") {
									res = "no_data";
								}

								out.println("msg=" + res);
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("EXCEPTION ::" + e.getMessage());
							out.println("exception");
							Log.getRequestResponseReccLog(strFlag, strSessionId + ","
									+ strMsisdn + "," + circleId + "," + strReqType + ","
									+ strPack + "," + strConnType + "," + e.getMessage() + ","
									+ (System.currentTimeMillis() - uniqueNumber), "Error");
						} finally {
							out.flush();
							out.close();
						}
						Log.getRequestResponseReccLog(strFlag, strSessionId + ","
								+ strMsisdn + "," + circleId + "," + strReqType + ","
								+ strConnType + "," + strPack + "," + res.trim() + ","
								+ (System.currentTimeMillis() - uniqueNumber), "Response");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("EXCEPTION ::" + e.getMessage());
				out.println("msg=error");
				Log.getRequestResponseReccLog(
						strFlag,
						strSessionId + "," + strMsisdn + "," + circleId + "," + strReqType
								+ "," + strConnType + "," + e.getMessage() + ","
								+ (System.currentTimeMillis() - uniqueNumber), "Error");
			} finally {
				out.flush();
				out.close();
			}
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

	public double getConsumedBalancePAYG(String[] strArr) {
		double balance = 0.0;
		for (int i = strArr.length - 1; i >= 0; i--) {
			String[] strAtributes = strArr[i].split("~");
			String strPackName = strAtributes[0];
			if (strPackName.endsWith("Usage")) {
				long bl = Long.parseLong(strAtributes[2]);
				balance = getBalanceOfDuration(bl / 1024);
			}
		}
		return balance;
	}

	public double getBalanceOfDuration(long lb) {
		double balance = Double.parseDouble(String.valueOf(lb));
		balance = balance / 1024;
		DecimalFormat decFormat = new DecimalFormat(".00");
		balance = Double.valueOf(decFormat.format(balance));
		return balance;
	}

	public String getBalanceOfVolume(Long lb) {
		int value = Integer.parseInt(String.valueOf(lb));
		int seconds = value % 60;
		int minutes = value / 60;
		int hour = minutes / 60;
		minutes = minutes % 60;
		String remaining_bal = hour + ":" + minutes + ":" + seconds;
		return remaining_bal;
	}

	public String getValidity(long longDate) {
		Date date = new Date(longDate);
		return date.toLocaleString();
	}

	public String getValidityPostpaid(long longDate) {
		longDate = longDate - 1000;
		Date date = new Date(longDate);
		return date.toLocaleString();
	}

	public double getMinutes(long balance) {
		double value = Double.parseDouble(String.valueOf(balance));
		System.out.println(value);
		double minutes = value / 60;
		DecimalFormat decFormat = new DecimalFormat(".00");
		minutes = Double.valueOf(decFormat.format(minutes));
		return minutes;
	}

	public String getConnType(String strPackName) {
		String conn_type = "";
		char secondDigit = strPackName.charAt(1);
		if (secondDigit == 'I') {
			conn_type = "Internet";
		} else if (secondDigit == 'B') {
			conn_type = "Broadband";
		} else if (secondDigit == 'D') {
			conn_type = "Internet";
		}
		return conn_type;
	}

	public String getTotalQuota(int totalQuota) {
		String str = "";
		if (totalQuota != 0) {
			double d = Double.parseDouble(String.valueOf(totalQuota));
			DecimalFormat decFormat = new DecimalFormat("0.00");
			str = String.valueOf(decFormat.format(d / 1024 / 1024));
			int intPart = Integer.parseInt(str.split("\\.")[0]);
			int decimalPart = Integer.parseInt(str.split("\\.")[1]);
			if (decimalPart > 0) {
				if (intPart > 0) {
					DecimalFormat decFormat1 = new DecimalFormat("0.0");
					str = String.valueOf(decFormat1.format(d / 1024 / 1024));
					str = str + "GB";
				} else {
					str = String.valueOf(totalQuota / 1024);
					str = str + "MB";
				}
			} else {
				str = intPart + "GB";
			}
		} else {
			str = "0GB";
		}
		return str;
	}

	public double getBalanceifNotZero(long lb) {
		double balance = 0.0;
		if (lb > 0) {
			balance = Double.parseDouble(String.valueOf(lb));
			balance = balance / 1024;
			if (balance <= 10) {
				balance = 0.0;
			} else {
				balance = balance / 1024;
				DecimalFormat decFormat = new DecimalFormat(".00");
				balance = Double.valueOf(decFormat.format(balance));
			}
		}
		return balance;
	}

	public static String packDesc(String strPackName) {
		String data = "";

		if (strPackName.contains("AO"))// add on packs
		{
			strPackName = strPackName.substring(0, strPackName.length() - 2);
		}

		else if (strPackName.contains("GT"))// rat based product
		{
			strPackName = strPackName.substring(0, strPackName.length() - 3);
		}

		else if (strPackName.contains("P") && strPackName.contains("G"))// rat
		// based
		// product
		{
			String data1 = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("P"));
			String data2 = strPackName.substring(strPackName.lastIndexOf("P") + 1,
					strPackName.lastIndexOf("P") + 2);
			data = data1 + "." + data2;

		}

		if (strPackName.endsWith("HM"))// MB CHECK
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("H"));
			data = data + "MB";
		} else if (strPackName.endsWith("KM"))// mb check
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("K"));
			data = data + "MB";

		} else if (strPackName.endsWith("M"))// MB CHECK
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("M"));
			data = data + "MB";
		} else if (strPackName.endsWith("G"))// gb check
		{
			if (strPackName.contains("P") && strPackName.contains("G"))// rat
			// based
			// product
			{
				String data1 = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("P"));
				String data2 = strPackName.substring(strPackName.lastIndexOf("P") + 1,
						strPackName.lastIndexOf("P") + 2);
				data = data1 + "." + data2 + "GB";

			} else {
				data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("G"));
				data = data + "GB";
			}
		} else if (strPackName.endsWith("UL"))// ulimted check
		{
			data = "Unlimted";
		} else// any data check
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("B") + 1);
		}
		return data;
	}

	public static String usuage(String strPackName, double balance) {
		String data = "";
		double packvalue = 0l;
		double temp;
		String usuage;

		if (strPackName.contains("AO"))// add on packs
		{
			strPackName = strPackName.substring(0, strPackName.length() - 2);
		}

		else if (strPackName.contains("GT"))// rat based product
		{
			strPackName = strPackName.substring(0, strPackName.length() - 3);
		}

		else if (strPackName.contains("P") && strPackName.contains("G"))// rat
		// based
		// product
		{
			String data1 = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("P"));
			String data2 = strPackName.substring(strPackName.lastIndexOf("P") + 1,
					strPackName.lastIndexOf("P") + 2);
			data = data1 + "." + data2;
			double d = Double.parseDouble(data);
			packvalue = (d) * 1024 * 1024;
		}

		if (strPackName.endsWith("HM"))// MB CHECK
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("H"));
			packvalue = Integer.parseInt(data) * 1024;
		} else if (strPackName.endsWith("KM"))// mb check
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("K"));
			packvalue = Integer.parseInt(data) * 1024;
		} else if (strPackName.endsWith("M"))// MB CHECK
		{
			data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
					strPackName.lastIndexOf("M"));
			packvalue = Integer.parseInt(data) * 1024;

		} else if (strPackName.endsWith("G"))// gb check
		{
			if (strPackName.contains("P"))// rat based product
			{
				//System.out.println("===   " + strPackName);
				String data1 = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("P"));
				String data2 = strPackName.substring(strPackName.lastIndexOf("P") + 1,
						strPackName.lastIndexOf("P") + 2);
				data = data1 + "." + data2;
				double d = Double.parseDouble(data);
				packvalue = (d) * 1024 * 1024;
			} else {

				data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("G"));
				packvalue = Integer.parseInt(data) * 1024 * 1024;
			}
		} else if (strPackName.endsWith("UL"))// ulimted check
		{
			data = "Unlimted";
		} else// any data check
		{
			if (strPackName.endsWith("MB")) {
				data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("M"));
				packvalue = Integer.parseInt(data) * 1024;
			} else if (strPackName.endsWith("GB")) {
				data = strPackName.substring(strPackName.lastIndexOf("D") + 1,
						strPackName.lastIndexOf("G"));
				packvalue = Integer.parseInt(data) * 1024 * 1024;
			}

		}
		if (packvalue > balance) {
			temp = packvalue - balance;
			temp = temp / 1024;
		} else if (packvalue == balance) {
			temp = 0;
			// temp=temp/1024;
		} else {
			temp = balance;
			temp = temp / 1024;
		}
		temp = Math.round(temp * 100.0) / 100.0;
		usuage = temp + "" + "MB";

		return usuage + "~" + packvalue;
	}

	public String strPack(String strPackName, String circleId,
			GetPackDescription objGetPackDescription){
		String strMessage = "";
		try {

			String packDesc = objGetPackDescription.getPackDescription(circleId,
					strPackName);

			if (packDesc == "") {

				if (strPackName.startsWith("RDn") || strPackName.startsWith("RD")) {
					strPackName.replace("RDn", "R");
					strPackName.replace("RD", "R");
				} else if (strPackName.startsWith("SDn")
						|| strPackName.startsWith("SD")) {
					strPackName.replace("SDn", "S");
					strPackName.replace("SD", "S");
				}

				Character first = strPackName.charAt(0);

				if (channel.containsKey(first.toString())) {
					Character second = strPackName.charAt(1);
					int temp;
					String packType = "";
					String validity = "";
					
					String rental = strPackName.substring(
							strPackName.lastIndexOf("R") + 1, strPackName.lastIndexOf("D"));
					
					//System.out.println("pack name="+strPackName+",rental="+rental);
					String data = "";
					if (use.containsKey(second.toString())) // I(mobile
					{
						if (strPackName.contains("V")) {
							temp = strPackName.lastIndexOf("V") - 2;
							packType = strPackName.substring(temp,
									strPackName.lastIndexOf("V"));
						}
						if (use.containsKey(packType)) // 2g,3g,wifi
						{
							validity = strPackName.substring(strPackName.indexOf("V") + 1,
									strPackName.lastIndexOf("R"));
							if (validity != null || (!validity.equalsIgnoreCase("")))// validity
							// check
							{
								if (rental != null || (!rental.equalsIgnoreCase("")))// rental
								// check
								{

									if (strPackName.contains("T"))// data check
									{
										strPackName = strPackName.substring(
												strPackName.lastIndexOf("T"), strPackName.length());
										strPackName = strPackName.replace("T", "D");
										data = packDesc(strPackName);

										strMessage = strMessage
												+ (use.get(second.toString()) + " " + data
														+ " Minutes (" + use.get(packType) + ") .~");
										System.out.println("String pack name contains T ="
												+ strMessage + ",data=" + data);
									}

									else if (strPackName.contains("D"))// data
									// check
									{
										data = packDesc(strPackName);
										strMessage = strMessage
												+ (use.get(second.toString()) + " " + data + " ("
														+ use.get(packType) + ") ");
									}
								}

							} else// volume add on packs
							{
								data = packDesc(strPackName);
								strMessage = strMessage
										+ (use.get(second.toString()) + " " + data + " ("
												+ use.get(packType) + ") ");
							}
						} else// rat products
						{
							data = packDesc(strPackName);
							strMessage = strMessage
									+ (use.get(second.toString()) + " " + data + "");
						}
					} else if (strPackName.startsWith("BOOST"))// boost packs
					{

						data = packDesc(strPackName);
						strMessage = strMessage
								+ ("Mobile Internet " + data + "(Booster Pack)" + "");
					}
				}
			}

			else {
				strMessage += objGetPackDescription.getPackDescription(circleId,
						strPackName) + "";

			}

		} catch (Exception e ) {
			System.out.println(e.getMessage());
			strMessage = "no Data found";
		}

		return strMessage;
	}

	public static String fetchMrp(String packname) {
		String mrp = "";
		try {
			mrp = packname.substring(packname.lastIndexOf("R") + 1,
					packname.lastIndexOf("D"));
		} catch (Exception e) {
			mrp = "0";

		}
		return mrp;
	}

}
