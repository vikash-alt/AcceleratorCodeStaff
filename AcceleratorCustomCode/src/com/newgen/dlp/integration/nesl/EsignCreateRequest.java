/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.nesl;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class EsignCreateRequest {

    CommonFunctionality cf = new CommonFunctionality();
    EsignCommonMethods cm = new EsignCommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods apicm = new APICommonMethods();
    ESignGetURL eURL = new ESignGetURL();

    //Function modifed for NESL (Mutliple Applicants Handling)
    String createNESLRequest(IFormReference ifr, String FileList, String DocPath,
            String NESLMode, String ProductType, String loanType) throws ParseException {
    	String loanAmount= "";
    	String tenure ="";

        Log.consoleLog(ifr, "Inside CreateNESLRequest.....NESLMode:" + NESLMode + ":ProductType:" + ProductType);

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        //  String ProcessInstanceId = "LOS-00000000000003470";
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        try {

            String CountResult = "";
            String CountQuery = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS "
                    + "WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
                    + "AND NESL_MODE='" + NESLMode + "'";
            Log.consoleLog(ifr, "CountQuery==>" + CountQuery);

            List< List< String>> Result1 = ifr.getDataFromDB(CountQuery);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (!Result1.isEmpty()) {
                CountResult = Result1.get(0).get(0);
            }
            if (CountResult.equalsIgnoreCase("")) {
                CountResult = "0";
            }

            Log.consoleLog(ifr, "CountResult==>" + CountResult);

            if (Integer.parseInt(CountResult) == 0) {

                //============Added on 16-02-2024 by Ahmed=================//
                String articleCode = "";
                String stampDutyAmnt = "";
                String eStampFlag = "N";
                Double loanAmt =0.0;
                Double odUtilized =0.0;
                String schemeID ="";
                String stateCode ="";
                
              
				if (ProductType.contains("VL")) {
					schemeID = pcm.mGetSchemeIDFORVL(ifr, ProcessInstanceId);
					stateCode = pcm.getStateCode(ifr, ProductType, ProcessInstanceId);
					if (stateCode.equalsIgnoreCase("")) {
						Log.consoleLog(ifr, "StateCode founds to be empty for in LOS_MST_STATE table.");
						return RLOS_Constants.ERROR;
					}
					if (NESLMode.equalsIgnoreCase("eStamping")) {
						eStampFlag = "Y";

						String Query = "SELECT STAMPCHARGES  from SLOS_TRN_LOANSUMMARY WHERE WINAME='"
								+ ProcessInstanceId + "'";
						List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
						if (!Output3.isEmpty()) {
							stampDutyAmnt = Output3.get(0).get(0);
						}
						String prob = "";
						String lapsDocNum = "";
						String probationQuery = "SELECT probation from slos_staff_trn where winame='"
								+ ProcessInstanceId + "' ";
						Log.consoleLog(ifr, "probation query===>" + probationQuery);
						List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
						if (!queryRes.isEmpty()) {
							prob = queryRes.get(0).get(0);
						}
						if (prob.equalsIgnoreCase("No")) {
							lapsDocNum = "NF 825";
						} else {
							lapsDocNum = "NF 1088";
						}
						String query = "SELECT DIGI_ARTICLE_CODE,STAMP_AMT FROM SLOS_MST_STATEFEECHARGES "
								+ "WHERE SCHEMEID='" + schemeID + "'\n" + "AND ISACTIVE='Y' " + "AND STATE_CODE='"
								+ stateCode + "' AND  LAPS_DOC_NUM='"+lapsDocNum+"'";
						Log.consoleLog(ifr, "query=====>" + query);
						List<List<String>> Result2 = ifr.getDataFromDB(query);
						Log.consoleLog(ifr, "#Result2===>" + Result2.toString());
						for (int i = 0; i < Result2.size(); i++) {
							articleCode = Result2.get(i).get(0);
							// stampDutyAmnt = Result2.get(i).get(1);
						}

						Log.consoleLog(ifr, "articleCode=====>" + articleCode);
						Log.consoleLog(ifr, "stampDutyAmnt===>" + stampDutyAmnt);

						if ((articleCode.equalsIgnoreCase("")) || (stampDutyAmnt.equalsIgnoreCase(""))) {
							Log.consoleLog(ifr, "Article Code/StampDuty amount seems to be empty!!");
							return RLOS_Constants.ERROR;
						}
					} else {
						Log.consoleLog(ifr, "Only ESigning is Configured");
					}

				} else {
					schemeID = pcm.mGetSchemeID(ifr, ProcessInstanceId);
					stateCode = pcm.getStateCode(ifr, ProductType, ProcessInstanceId);

					String queryForOdUtilized = "SELECT LOAN_AMOUNT, OD_UTILIZED FROM SLOS_STAFF_TRN WHERE WINAME='"
							+ ProcessInstanceId + "'";
					Log.consoleLog(ifr, "queryForOdUtilized" + queryForOdUtilized);
					List<List<String>> resForOdUtilized = ifr.getDataFromDB(queryForOdUtilized);
					Log.consoleLog(ifr, "resForOdUtilized===>" + resForOdUtilized);
					if (!resForOdUtilized.isEmpty()) {
						loanAmt = Double.parseDouble(resForOdUtilized.get(0).get(0));
						odUtilized = Double.parseDouble(resForOdUtilized.get(0).get(1));
					}

					if (stateCode.equalsIgnoreCase("")) {
						Log.consoleLog(ifr, "StateCode founds to be empty for in LOS_MST_STATE table.");
						return RLOS_Constants.ERROR;
					}

					if (loanType.contains("Renewal")) {
						Log.consoleLog(ifr, "Inside condition to remove object for stamp amt====>");
						eStampFlag = "N";
						NESLMode = "eSign";
					}
					if (NESLMode.equalsIgnoreCase("eStamping")) {
						eStampFlag = "Y";

						String Query = "SELECT STAMPCHARGES  from SLOS_TRN_LOANSUMMARY WHERE WINAME='"
								+ ProcessInstanceId + "'";
						List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
						if (!Output3.isEmpty()) {
							stampDutyAmnt = Output3.get(0).get(0);
						}

						String query = "SELECT DIGI_ARTICLE_CODE,STAMP_AMT FROM SLOS_MST_STATEFEECHARGES "
								+ "WHERE SCHEMEID='" + schemeID + "'\n" + "AND ISACTIVE='Y' " + "AND STATE_CODE='"
								+ stateCode + "'";
						Log.consoleLog(ifr, "query=====>" + query);
						List<List<String>> Result2 = ifr.getDataFromDB(query);
						Log.consoleLog(ifr, "#Result2===>" + Result2.toString());
						for (int i = 0; i < Result2.size(); i++) {
							articleCode = Result2.get(i).get(0);
							// stampDutyAmnt = Result2.get(i).get(1);
						}

						Log.consoleLog(ifr, "articleCode=====>" + articleCode);
						Log.consoleLog(ifr, "stampDutyAmnt===>" + stampDutyAmnt);

						if ((articleCode.equalsIgnoreCase("")) || (stampDutyAmnt.equalsIgnoreCase(""))) {
							Log.consoleLog(ifr, "Article Code/StampDuty amount seems to be empty!!");
							return RLOS_Constants.ERROR;
						}
					} else {
						Log.consoleLog(ifr, "Only ESigning is Configured");
					}

				}
                String[] App_F_Key = null;

                JSONObject loanInfoDetails = cm.getLoanInformation(ifr, ProductType, ProcessInstanceId);
                if (loanInfoDetails.isEmpty()) {
                    Log.consoleLog(ifr, "No Loan information details present. Please check Application Journey");
                    return RLOS_Constants.ERROR;
                }

                String DocumentCountSnippet = cm.getDocDetailsCount(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "DocumentCountSnippet========>" + DocumentCountSnippet);
                if (DocumentCountSnippet.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                String resultCount = cm.getNoofApplicants(ifr, ProductType, ProcessInstanceId);
                Log.consoleLog(ifr, "resultCount===>" + resultCount);
                if (Integer.parseInt(resultCount) == 0) {
                    Log.consoleLog(ifr, "#No data available to process");
                    return RLOS_Constants.ERROR;
                }

//                if (!ProductType.equalsIgnoreCase("PAPL")) {
//                    String F_Key = cm.getApplicantUniqueKeyParams(ifr, ProcessInstanceId);
//                    App_F_Key = F_Key.split(",");
//                }

                String eStampDetailSnippet = "";
                String participantDetailSnippet = "";
                String documentDetailSnippet = "";
                String eSignDetailSnippet = "";

                for (int i = 0; i < Integer.parseInt(resultCount); i++) {
                    int partyId = i + 1;

                    if (!eStampDetailSnippet.equalsIgnoreCase("")) {
                        eStampDetailSnippet = eStampDetailSnippet + ",";
                    }

                    if (!participantDetailSnippet.equalsIgnoreCase("")) {
                        participantDetailSnippet = participantDetailSnippet + ",";
                    }

                    if (!documentDetailSnippet.equalsIgnoreCase("")) {
                        documentDetailSnippet = documentDetailSnippet + ",";
                    }

                    if (!eSignDetailSnippet.equalsIgnoreCase("")) {
                        eSignDetailSnippet = eSignDetailSnippet + ",";
                    }

//                    String query1 = "";
//                    // String query2 = "";//Added by Ahmed on 12-06-2024 for AadharVault
//                    if (ProductType.equalsIgnoreCase("PAPL")) {
//                        query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERMIDDLENAME,CUSTOMERLASTNAME,PANNUMBER,MOBILENUMBER,EMAILID,DATEOFBIRTH,GENDER,CUSTOMERID "
//                                + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
////                        //Added by Ahmed on 12-06-2024 for fetching Aadhar Number from AadharVault
////                        query2 = "SELECT AADHARNO FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
////                                + "WHERE WINAME='" + ProcessInstanceId + "' AND ROWNUM=1";
//                    } else {
////                        query1 = "select a.firstname,a.middlename,a.lastname,b.kyc_no,a.mobileno,a.emailid,to_char(a.dob,'dd-MM-YYYY')dob,a.gender from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
////                                + "select f_key from los_nl_basic_info where PID = '" + ProcessInstanceId + "'\n"
////                                + ") and a.f_key=b.f_key and a.f_key='" + App_F_Key[i] + "'";
//
//                        query1 = "select a.firstname,a.middlename,a.lastname,b.kyc_no,a.mobileno,a.emailid,to_char(a.dob,'dd-MM-YYYY')dob,a.gender,c.customerid\n"
//                                + " from los_l_basic_info_i a, LOS_NL_KYC b,los_nl_basic_info c  \n"
//                                + " where a.f_key=b.f_key and a.f_key='" + App_F_Key[i] + "' and c.pid='" + ProcessInstanceId + "' and a.f_key=c.f_key";
//
////                        //Added by Ahmed on 12-06-2024 for fetching Aadhar Number from AadharVault
////                        query2 = "select b.kyc_no from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
////                                + "select f_key from los_nl_basic_info where PID = '" + ProcessInstanceId + "'\n"
////                                + ") and a.f_key=b.f_key and a.f_key='" + App_F_Key[i] + "' AND b.kyc_id='AA'";
//                    }
//
//                    Log.consoleLog(ifr, "query1=====>" + query1);
//                    List< List< String>> Result3 = ifr.getDataFromDB(query1);
//                    Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
//                    //Added by Ahmed on 12-06-2024 for fetching Aadhar Number from AadharVault
//                    Log.consoleLog(ifr, "query2=====>" + query2);
//                    List< List< String>> query2Result = ifr.getDataFromDB(query2);
//                    Log.consoleLog(ifr, "#query2Result===>" + query2Result.toString());
//                    String aadharNumber = "";
//                    if (query2Result.isEmpty()) {
//                        Log.consoleLog(ifr, "No Aadhar RefNo found");
//                        return RLOS_Constants.ERROR;
//                    } else {
////                        String aadharRefKeyNo = query2Result.get(0).get(0);
////                        Log.consoleLog(ifr, "aadharRefKeyNo===>" + aadharRefKeyNo);
////                        AadharVault av = new AadharVault();
////                        aadharNumber = av.getData(ifr, aadharRefKeyNo);
////                        Log.consoleLog(ifr, "aadharNumber==>" + aadharNumber);
////                        if (aadharNumber.contains(RLOS_Constants.ERROR)) {
////                            return RLOS_Constants.ERROR;
////                        }
//
//                       
//
//                    }
                    //===Added by Ahmed on 31-07-2024 for getting CustomerId logic for Combination Included.
//                    String customerId = "";
//                    if (ProductType.equalsIgnoreCase("PAPL")) {
//                        customerId = pcm.getCustomerIDPAPL(ifr);
//                    } else {
//                        customerId = pcm.getCustomerIDForOtherProducts(ifr);
//                    }
                    //JSONObject applicantObj = cm.getApplicantInformation(ifr, Result3, "");
                    //Modiifed by Ahmed on 19-08-2024 for adding Customer Id
                    String query1 = "";
                   /* if (ProductType.equalsIgnoreCase("PAPL")) {
                        query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERMIDDLENAME,CUSTOMERLASTNAME,PANNUMBER,MOBILENUMBER,EMAILID,DATEOFBIRTH,GENDER,CUSTOMERID "
                                + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
                    } else {
                        query1 = "select a.firstname,a.middlename,a.lastname,b.kyc_no,a.mobileno,a.emailid,to_char(a.dob,'dd-MM-YYYY')dob,a.gender,c.customerid\n"
                                + " from los_l_basic_info_i a, LOS_NL_KYC b,los_nl_basic_info c  \n"
                                + " where a.f_key=b.f_key and a.f_key='" + App_F_Key[i] + "' and c.pid='" + ProcessInstanceId + "' and a.f_key=c.f_key";
                    }
                   */
                   query1="SELECT CUSTOMERFIRSTNAME,CUSTOMERMIDDLENAME,CUSTOMERLASTNAME,PANNUMBER," +
                   "MOBILENUMBER,EMAILID,to_char(DATEOFBIRTH,'dd-MM-YYYY')dob,CUSTOMERSEX,CUSTOMERID " +
                   "FROM LOS_TRN_CUSTOMERSUMMARY where winame='"+ProcessInstanceId+"'";
                    Log.consoleLog(ifr, "query1=====>" + query1);
                    List< List< String>> Result3 = ifr.getDataFromDB(query1);
                    Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
                      
                    // need to check for aadhar number
                    JSONObject applicantObj = cm.getApplicantInformation(ifr, Result3);
                    if (applicantObj.isEmpty()) {
                        Log.consoleLog(ifr, "No Applicant Info present. Please check Application Journey");
                        return RLOS_Constants.ERROR;
                    }
                    String permZIP="";
                    String panno="";
                    String query2="SELECT PERMZIP,PANNUMBER FROM LOS_TRN_CUSTOMERSUMMARY where winame='"+ProcessInstanceId+"'";
                    List<List<String>> query2Zip = ifr.getDataFromDB(query2);
                    Log.consoleLog(ifr, "Permanent ZIP====>" + query2Zip);
                    if (!query2Zip.isEmpty()) {
            			permZIP = query2Zip.get(0).get(0);
            			Log.consoleLog(ifr, "permZIP====>" + permZIP);
            			panno = query2Zip.get(0).get(1);
            		}
                    if (eStampFlag.equalsIgnoreCase("Y") && (stateCode.equalsIgnoreCase("WB") || stateCode.equalsIgnoreCase("MP"))) {
                        String eStampSnippet = cm.getStampDetailsSnippetForWBAndMP(ifr, Result3, articleCode, stampDutyAmnt, applicantObj, loanInfoDetails,permZIP,panno);
                        eStampDetailSnippet = eStampDetailSnippet + eStampSnippet;
                    }
                   
                    else if(eStampFlag.equalsIgnoreCase("Y")) {
                        String eStampSnippet = cm.getStampDetailsSnippet(ifr, Result3, articleCode, stampDutyAmnt, applicantObj, loanInfoDetails);
                        eStampDetailSnippet = eStampDetailSnippet + eStampSnippet;
                    }
                   

                    String participantSnippet = cm.getParticipantSnippet(ifr, Result3, DocumentCountSnippet, applicantObj, partyId);
                    participantDetailSnippet = participantDetailSnippet + participantSnippet;

                    String documentDataSnippet = cm.getDocBase64Content(ifr, FileList, DocPath, partyId);
                    documentDetailSnippet = documentDetailSnippet + documentDataSnippet;

                    String eSignCordinatesSnippet = cm.getSignCoordinate(ifr, FileList, DocPath, partyId, ProcessInstanceId);
                    eSignDetailSnippet = eSignDetailSnippet + eSignCordinatesSnippet;

                    Result3.clear();
                    applicantObj.clear();

                }

                Date currentDate1 = new Date();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("ddMMyyHHmmssSSS");
                String ReferenceId = dateFormat1.format(currentDate1);

                String LoanNo = ProcessInstanceId;
                String[] DataSplitter = LoanNo.split("-");
                String WIRefNo = cm.removeZero(DataSplitter[1]);
                System.out.println("WIRefNo==>" + WIRefNo);
                String NESLRefNo = "NESL_STF" + WIRefNo;
                String SancRefNo = "SANC_STF" + WIRefNo;
                Log.consoleLog(ifr, "NESLRefNo=========>" + NESLRefNo);
                Log.consoleLog(ifr, "SancRefNo=========>" + SancRefNo);

                String Request = "";

                //String TxnId = "NESL_" + ReferenceId; Commented by Ahmed on 27-06-2024 for TnxId Logic Change
                String TxnId = "NESL_STF_" + WIRefNo;
                Request = "{\n"
                        + "    \"loan\": {\n"
                        + "        \"loanno\": \"" + NESLRefNo + "\",\n"
                        + "        \"snctnno\": \"" + SancRefNo + "\",\n"
                        + "        \"regType\": \"1\",\n"
                        + "        \"signFlag\": \"1\",\n"
                        + "        \"txnID\": \"" + TxnId + "\",\n"
                        + "        \"loanDocFlag\": \"N\",\n"
                        + "        \"estampFlag\": \"" + eStampFlag + "\",\n"
                        + "        \"state\": \"" + stateCode + "\",\n"
                        + "        \"f2f\": \"Y\",\n"
                        + "        \"estampdtls\": [\n"
                        + eStampDetailSnippet
                        + "        ],\n"
                        + "        \"prtcptentty\": [\n"
                        + participantDetailSnippet
                        + "        ],\n"
                        + "        \"loandtls\": {\n"
                        + "            \"chgamt\": \"0\",\n"
                        + "            \"crdtsubtyp\": \"credit facility\",\n"
                        + "            \"currofsanc\": \"INR\",\n"
                        + "            \"dtofsnctn\": \"" + loanInfoDetails.get("DateofSanction") + "\",\n"
                        + "            \"fcltynm\": \"Personal Loan\",\n"
                        + "            \"fundtyp\": \"Funded\",\n"
                        + "            \"isacctclosed\": \"no\",\n"
                        + "            \"ntrofcrdt\": \"financial\",\n"
                        + "            \"rtofint\": \"" + loanInfoDetails.get("roi") + "\",\n"
                        + "            \"snctnamt\": \"" + loanInfoDetails.get("loanAmount") + "\",\n"
                        + "            \"emiamt\": \"" + loanInfoDetails.get("emi") + "\",\n"
                        + "            \"tenure\": \"" + loanInfoDetails.get("Tenure") + "\",\n"
                        + "            \"toutstndamt\": \"0.00\"\n"
                        + "        },\n"
                        + "        \"documentdtls\": [\n"
                        + documentDetailSnippet
                        + "        ],\n"
                        + "         \"eSignCordinates\": [\n"
                        + eSignDetailSnippet
                        + "        ]\n"
                        + "    }\n"
                        + "}";

                Log.consoleLog(ifr, "CreateNESLSigningRequest Ended..");

                HashMap requestHeader = new HashMap<>();
               // requestHeader.put("workitemid", ProcessInstanceId);//Added by Ahmed on 23-07-2024 including WI on API Headers
                requestHeader.put("journey", "staff");//Adding Header based upon Multi Client Id/Client Secret w.r.t Product Wise
                String Response = cf.CallWebService(ifr, "NESL_InitiateRequest", Request, "", requestHeader);

                //  apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                if (!Response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(Response);
                    JSONObject resultObj = new JSONObject(OutputJSON);

                    String status = resultObj.get("status").toString();

                   // apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                    Log.consoleLog(ifr, "status==>" + status);
                    if (status.equalsIgnoreCase("SUCCESS")) {
                    	apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                        String transactionId = resultObj.get("transactionId").toString();
                        Log.consoleLog(ifr, "transactionId==>" + transactionId);

                        String Query1 = "INSERT INTO LOS_INTEGRATION_NESL_STATUS(PROCESSINSTANCEID,LOAN_ACC_NO,NESL_TRANS_ID,REQ_STATUS,NESL_MODE) "
                                + "VALUES ('" + ProcessInstanceId + "','" + NESLRefNo + "','" + TxnId + "','N','" + NESLMode + "')";
                        Log.consoleLog(ifr, "Query1==>" + Query1);
                        ifr.saveDataInDB(Query1);
                        
                        
                        String Query = "SELECT LOAN_AMOUNT,TENURE_MONTHS from SLOS_STAFF_TRN WHERE WINAME='"
        						+ ProcessInstanceId + "'";
        				List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
        				if (Output3.size() > 0) {
        					loanAmount = Output3.get(0).get(0);
        					tenure = Output3.get(0).get(1);
        				}
                        
        				 LocalDate today = LocalDate.now();
        				 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        				 String todayDate = today.format(formatter);
        				  
        				 String mobileNumber = "";
        				 String appStatus="";
        				 
        				 String queryForR = "select MOBILENUMBER, APPLICATION_STATUS from LOS_WIREFERENCE_TABLE WHERE WINAME= '"+ProcessInstanceId+"'";
						Log.consoleLog(ifr, "queryForR : " + queryForR);
						List<List<String>> queryForResumeD = ifr.getDataFromDB(queryForR);
						Log.consoleLog(ifr, "queryForResumeD : " + queryForResumeD);
						if(queryForResumeD.isEmpty())
						{
							mobileNumber = queryForResumeD.get(0).get(0);
							appStatus = queryForResumeD.get(0).get(1);
						}
                        
                        String Query3 = "INSERT INTO SLOS_NESL_LOAN_PASSED_STATUS(WINAME,LOAN_AMOUNT,TENURE,NESLDATE) "
                                + "SELECT '" + ProcessInstanceId + "','" + loanAmount + "','" + tenure + "','"+todayDate+"' FROM dual WHERE NOT EXISTS (SELECT 1 FROM SLOS_NESL_LOAN_PASSED_STATUS WHERE WINAME ='"+ProcessInstanceId+"')";
                        Log.consoleLog(ifr, "Query3==>" + Query3);
                        ifr.saveDataInDB(Query3);
                        

                        String Query2 = "INSERT INTO LOS_NESL_DOC(PID,TXNID) VALUES ('" + ProcessInstanceId + "','" + TxnId + "')";
                        Log.consoleLog(ifr, "Query2==>" + Query2);
                        ifr.saveDataInDB(Query2);

                        String neslMode = pcm.getNESLModeQuery(ifr, "STAFF");
                        if (neslMode.equalsIgnoreCase("Y")) {
                            JSONObject eSignURLObj = new JSONObject();
                            eSignURLObj.put("ProcessInstanceId", ProcessInstanceId);
                            eSignURLObj.put("NESLRefNo", NESLRefNo);
                            eSignURLObj.put("TxnId", TxnId);

                            String eSignURLStatus = eURL.getEsignURL(ifr, eSignURLObj);
                            Log.consoleLog(ifr, "eSignURLStatus==>" + eSignURLStatus);
                            if (eSignURLStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                                return RLOS_Constants.SUCCESS;
                            } else {
                                return RLOS_Constants.ERROR;
                            }
                        }	
                        else {
                            return RLOS_Constants.SUCCESS;
                        }

                        //return RLOS_Constants.SUCCESS;
                        //Below is for NESL F2F. Commented on 29/02/2024 after discussing with Ashwin sir
                    } else {
                        // apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                        //Added by Ahmed on 27-06-2024 for handling Dup. Transaction Details
                        String respStatusMessage = resultObj.get("respStatusMessage").toString();
                        Log.consoleLog(ifr, "respStatusMessage==>" + respStatusMessage);
                        if (respStatusMessage.contains("Duplicate")) {
                        	String count = "0";
                        	 String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                                     + "PROCESSINSTANCEID='" + ProcessInstanceId + "'";
                             Log.consoleLog(ifr, "Query1==>" + Query1);
                             List<List<String>> Result2 = ifr.getDataFromDB(Query1);
                             Log.consoleLog(ifr, "#Result1===>" + Result2.toString());
                             if (Result2 != null && !Result2.isEmpty() && !Result2.get(0).isEmpty()) {
                                 count = Result2.get(0).get(0);
                             }
                        	if(Integer.parseInt(count) ==0) {
                        	   String Query2 = "INSERT INTO LOS_INTEGRATION_NESL_STATUS(PROCESSINSTANCEID,LOAN_ACC_NO,NESL_TRANS_ID,REQ_STATUS,NESL_MODE) "
                                       + "VALUES ('" + ProcessInstanceId + "','" + NESLRefNo + "','" + TxnId + "','N','" + NESLMode + "')";
                               Log.consoleLog(ifr, "Query1==>" + Query1);
                               ifr.saveDataInDB(Query2);
                        	}
                               JSONObject eSignURLObj = new JSONObject();
                               eSignURLObj.put("ProcessInstanceId", ProcessInstanceId);
                               eSignURLObj.put("NESLRefNo", NESLRefNo);
                               eSignURLObj.put("TxnId", TxnId);
                               
                               String eSignURLStatus = eURL.getEsignURL(ifr, eSignURLObj);
                        	
                            Log.consoleLog(ifr, "#Duplicate Transaction Details");
                            apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", "Duplicate Transaction Details");
                            return RLOS_Constants.SUCCESS;
                        }
                        apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                        return RLOS_Constants.ERROR;
                    }
                } else {
                    apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, "No response from Server", "", "", "FAIL");
                }

                Log.consoleLog(ifr, "Response==>" + Response);

            } else {
                Log.consoleLog(ifr, "NESL Request already is in progress..");
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            Log.errorLog(ifr, "Exception==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    /*
    String createNESLRequest_eStamping(IFormReference ifr, String FileList, String DocPath,
            String NESLMode, String ProductType) throws ParseException {
        Log.consoleLog(ifr, "Inside CreateNESLRequest.....NESLMode:" + NESLMode + ":ProductType:" + ProductType);
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        try {

            String CountResult = "";
            String CountQuery = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS "
                    + "WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
                    + "AND NESL_MODE='" + NESLMode + "'";
            Log.consoleLog(ifr, "CountQuery==>" + CountQuery);

            List< List< String>> Result1 = ifr.getDataFromDB(CountQuery);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (Result1.size() > 0) {
                CountResult = Result1.get(0).get(0);
            }
            if (CountResult.equalsIgnoreCase("")) {
                CountResult = "0";
            }

            Log.consoleLog(ifr, "CountResult==>" + CountResult);

            if (Integer.parseInt(CountResult) == 0) {

                //============Added on 16-02-2024 by Ahmed=================//
                String articleCode = "";
                String stampDutyAmnt = "";
                String schemeID = pcm.mGetSchemeID(ifr, ProcessInstanceId);
                String stateCode = pcm.getStateCode(ifr, ProductType, ProcessInstanceId);

                if (stateCode.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "StateCode founds to be empty for in LOS_MST_STATE table.");
                    return RLOS_Constants.ERROR;
                }

                String query = "SELECT DIGI_ARTICLE_CODE,STAMP_AMT FROM LOS_MST_STATEFEECHARGES "
                        + "WHERE SCHEMEID='" + schemeID + "'\n"
                        + "AND ISACTIVE='Y' "
                        + "AND STATE_CODE='" + stateCode + "'";
                Log.consoleLog(ifr, "query=====>" + query);
                List< List< String>> Result2 = ifr.getDataFromDB(query);
                Log.consoleLog(ifr, "#Result2===>" + Result2.toString());

                if (Result2.size() > 0) {
                    articleCode = Result2.get(0).get(0);
                    stampDutyAmnt = Result2.get(0).get(1);
                }

                Log.consoleLog(ifr, "articleCode=====>" + articleCode);
                Log.consoleLog(ifr, "stampDutyAmnt===>" + stampDutyAmnt);

                if ((articleCode.equalsIgnoreCase("")) || (stampDutyAmnt.equalsIgnoreCase(""))) {
                    Log.consoleLog(ifr, "Article Code/StampDuty amount seems to be empty!!");
                    return RLOS_Constants.ERROR;
                }

                //============Ended on 16-02-2024 by Ahmed=================//
                String signatoryAadhar = "";
                String signatoryGender = "M";//Deafult
                String customerFullName = "";
                String customerFirstName = "";
                String customerLastName = "";
                String panNumber = "";
                String mobileNumber = "";
                String emailId = "";
                String loanAmount = "";
                String Tenure = "";
                String roi = "";
                String emi = "";
                String aadharNum = "";
                String dateOfBirth = "";
                String gender = "";

                String query1 = "";
                if (ProductType.equalsIgnoreCase("PAPL")) {
                    query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,MOBILENUMBER,EMAILID,DATEOFBIRTH,GENDER "
                            + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
                } else {
                    query1 = "select a.firstname,a.lastname,b.kyc_no,a.mobileno,a.emailid,to_char(a.dob,'dd-MM-YYYY')dob,a.gender from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
                            + "select f_key from los_nl_basic_info where PID = '" + ProcessInstanceId + "'\n"
                            + ") and a.f_key=b.f_key";
                }

                Log.consoleLog(ifr, "query1==>" + query1);
                List< List< String>> Result3 = ifr.getDataFromDB(query1);
                Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
                if (Result3.size() > 0) {
                    customerFirstName = Result3.get(0).get(0);
                    customerLastName = Result3.get(0).get(1);
                    customerFullName = customerFirstName.replace("{}", "") + " " + customerLastName.replace("{}", "");
                    panNumber = Result3.get(0).get(2);
                    mobileNumber = Result3.get(0).get(3);

                    if (mobileNumber.length() > 10) {
                        mobileNumber = mobileNumber.substring(2, 12);
                        Log.consoleLog(ifr, "MobileNumber==>" + mobileNumber);
                    }
                    emailId = Result3.get(0).get(4);
                    dateOfBirth = Result3.get(0).get(5);
                    gender = Result3.get(0).get(6);

                    if (gender.equalsIgnoreCase("MALE")) {
                        signatoryGender = "M";
                    } else if (gender.equalsIgnoreCase("FEMALE")) {
                        signatoryGender = "F";
                    } else {
                        signatoryGender = "M";//Transgender is not supporting by NESL
                    }
                }

                Log.consoleLog(ifr, "customerFullName==>" + customerFullName);
                Log.consoleLog(ifr, "panNumber=========>" + panNumber);
                Log.consoleLog(ifr, "mobileNumber======>" + mobileNumber);
                Log.consoleLog(ifr, "emailId===========>" + emailId);
                Log.consoleLog(ifr, "dateOfBirth=======>" + dateOfBirth);
                Log.consoleLog(ifr, "gender============>" + gender);

                HashMap<String, String> customerdetails = new HashMap<>();
                customerdetails.put("MobileNumber", mobileNumber);
                aadharNum = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
                Log.consoleLog(ifr, "aadharNum==>" + aadharNum);
                signatoryAadhar = aadharNum;
                Log.consoleLog(ifr, "signatoryAadhar/getCustomerAccountSummary==>" + signatoryAadhar);
                signatoryAadhar = signatoryAadhar.substring(8, 12);
                Log.consoleLog(ifr, "signatoryAadhar==>" + signatoryAadhar);

                String DOB = "";
                try {
                    DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
                    DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = originalFormat.parse(dateOfBirth);
                    DOB = targetFormat.format(date);
                    Log.consoleLog(ifr, "DOB==>" + DOB);
                } catch (Exception e) {
                    Log.consoleLog(ifr, "Exception DOB==>" + e);
                }

                String query2 = "";
                if (ProductType.equalsIgnoreCase("PAPL")) {
                    query2 = "select loanamount,tenure,rateofinterest,emi from "
                            + "los_tran_papl_finaleligibility WHERE WINAME='" + ProcessInstanceId + "'";
                } else if (ProductType.equalsIgnoreCase("Pension")) {
                    query2 = "SELECT LOAN_AMT,TENURE,ROI,EMI from "
                            + "LOS_PEN_FINAL_ELIGIBILITY WHERE PID='" + ProcessInstanceId + "'";
                } else {
                    query2 = "SELECT LOANAMOUNT,Tenure,RATEOFINTEREST,EMI from "
                            + "LOS_TRN_FINALELIGIBILITY WHERE WINAME='" + ProcessInstanceId + "'";
                }
                Log.consoleLog(ifr, "#query2===>" + query2);
                List< List< String>> Result4 = ifr.getDataFromDB(query2);
                Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
                if (Result4.size() > 0) {
                    loanAmount = Result4.get(0).get(0);
                    Tenure = Result4.get(0).get(1);
                    roi = Result4.get(0).get(2);
                    emi = Result4.get(0).get(3);
                }

                Log.consoleLog(ifr, "LoanAmount==>" + loanAmount);
                Log.consoleLog(ifr, "Tenure======>" + Tenure);
                Log.consoleLog(ifr, "ROI=========>" + roi);
                Log.consoleLog(ifr, "EMI=========>" + emi);

                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String DateofSanction = dateFormat.format(currentDate);

                String DocumentDataSnippet = cm.getDocBase64Content(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "DocumentDataSnippet=========>" + DocumentDataSnippet);

                String eSignCordinatesSnippet = cm.getSignCoordinate(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "eSignCordinatesSnippet=========>" + eSignCordinatesSnippet);
                if (eSignCordinatesSnippet.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                String DocumentCountSnippet = cm.getDocDetailsCount(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "DocumentCountSnippet=========>" + DocumentCountSnippet);
                if (DocumentCountSnippet.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                Date currentDate1 = new Date();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("ddMMyyHHmmssSSS");
                String ReferenceId = dateFormat1.format(currentDate1);

                String LoanNo = ProcessInstanceId;
                String[] DataSplitter = LoanNo.split("-");
                String WIRefNo = cm.removeZero(DataSplitter[1]);
                System.out.println("WIRefNo==>" + WIRefNo);
                String NESLRefNo = "NESL_" + WIRefNo;
                String SancRefNo = "SANC_" + WIRefNo;
                Log.consoleLog(ifr, "NESLRefNo=========>" + NESLRefNo);
                Log.consoleLog(ifr, "SancRefNo=========>" + SancRefNo);

                String Request = "";

                String TxnId = "NESL_" + ReferenceId;
                Request = "{\n"
                        + "    \"loan\": {\n"
                        + "        \"loanno\": \"" + NESLRefNo + "\",\n"
                        + "        \"snctnno\": \"" + SancRefNo + "\",\n"
                        + "        \"regType\": \"1\",\n"
                        + "        \"signFlag\": \"1\",\n"
                        + "        \"txnID\": \"" + TxnId + "\",\n"
                        + "        \"loanDocFlag\": \"N\",\n"
                        + "        \"estampFlag\": \"Y\",\n"
                        + "        \"state\": \"" + stateCode + "\",\n"
                        + "        \"f2f\": \"Y\",\n"
                        + "        \"estampdtls\": [\n"
                        + "            {\n"
                        + "                \"firstparty\": \"" + customerFirstName + "\",\n"
                        + "                \"considerationPrice\": \"" + loanAmount + "\",\n"
                        + "                \"stampDutyAmount\": \"" + stampDutyAmnt + "\",\n"
                        + "                \"stampdutyPaidby\": \"Canara Bank\",\n"
                        + "                \"secondparty\": \"Canara Bank\",\n"
                        + "                \"documentID\": \"1\",\n"
                        + "                \"articleCode\": \"" + articleCode + "\",\n"
                        + "                \"descriptionofDocument\": \"Personal loan\"\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"prtcptentty\": [\n"
                        + "            {\n"
                        + "                \"prtcptenttyId\": \"1\",\n"
                        + "                \"altemlid\": \"\",\n"
                        + "                \"altmobno\": \"" + mobileNumber + "\",\n"
                        + "                \"cntrprtyaddr\": \"\",\n"
                        + "                \"cntrprtycntmobno\": \"" + mobileNumber + "\",\n"
                        + "                \"cntrprtycntnm\": \"FIRE\",\n"
                        + "                \"comaddr\": \"\",\n"
                        + "                \"doi\": \"" + DOB + "\",\n"
                        + "                \"lglcnstn\": \"Resident Individual\",\n"
                        + "                \"emlid\": \"" + emailId.replace("{}", "") + "\",\n"
                        + "                \"fulnm\": \"" + customerFirstName + "\",\n"
                        + "                \"panno\": \"" + panNumber + "\",\n"
                        + "                \"partytyp\": \"Indian Entity\",\n"
                        + "                \"pin\": \"\",\n"
                        + "                \"regoffpin\": \"\",\n"
                        + "                \"reltocntrct\": \"debtor\",\n"
                        + "                \"ovdtype\": \"\",\n"
                        + "                \"ovdid\": \"\",\n"
                        + "                \"signatoryAadhar\": \"" + signatoryAadhar + "\",\n"
                        + "                \"signatoryGender\": \"" + signatoryGender + "\",\n"
                        + "                \"documentID\": [\n"
                        + DocumentCountSnippet
                        + "                ],\n"
                        + "                \"seqno\": 1\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"loandtls\": {\n"
                        + "            \"chgamt\": \"0\",\n"
                        + "            \"crdtsubtyp\": \"credit facility\",\n"
                        + "            \"currofsanc\": \"INR\",\n"
                        + "            \"dtofsnctn\": \"" + DateofSanction + "\",\n"
                        + "            \"fcltynm\": \"Personal Loan\",\n"
                        + "            \"fundtyp\": \"Funded\",\n"
                        + "            \"isacctclosed\": \"no\",\n"
                        + "            \"ntrofcrdt\": \"financial\",\n"
                        + "            \"rtofint\": \"" + roi + "\",\n"
                        + "            \"snctnamt\": \"" + loanAmount + "\",\n"
                        + "            \"emiamt\": \"" + emi + "\",\n"
                        + "            \"tenure\": \"" + Tenure + "\",\n"
                        + "            \"toutstndamt\": \"0.00\"\n"
                        + "        },\n"
                        + "        \"documentdtls\": [\n"
                        + DocumentDataSnippet
                        + "        ],\n"
                        + "         \"eSignCordinates\": [\n"
                        + eSignCordinatesSnippet
                        + "        ]\n"
                        + "    }\n"
                        + "}";

                Log.consoleLog(ifr, "CreateNESLSigningRequest Ended..");

                HashMap requestHeader = new HashMap<>();
                String Response = cf.CallWebService(ifr, "NESL_InitiateRequest", Request, "", requestHeader);

                //  apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                if (!Response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(Response);
                    JSONObject resultObj = new JSONObject(OutputJSON);

                    String status = resultObj.get("status").toString();

                    apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                    Log.consoleLog(ifr, "status==>" + status);
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        String transactionId = resultObj.get("transactionId").toString();
                        Log.consoleLog(ifr, "transactionId==>" + transactionId);

                        String Query1 = "INSERT INTO LOS_INTEGRATION_NESL_STATUS(PROCESSINSTANCEID,LOAN_ACC_NO,NESL_TRANS_ID,REQ_STATUS,NESL_MODE) "
                                + "VALUES ('" + ProcessInstanceId + "','" + NESLRefNo + "','" + TxnId + "','N','" + NESLMode + "')";
                        Log.consoleLog(ifr, "Query1==>" + Query1);
                        ifr.saveDataInDB(Query1);

                        String Query2 = "INSERT INTO LOS_NESL_DOC(PID,TXNID) VALUES ('" + ProcessInstanceId + "','" + TxnId + "')";
                        Log.consoleLog(ifr, "Query2==>" + Query2);
                        ifr.saveDataInDB(Query2);
                        return RLOS_Constants.SUCCESS;
                    } else {
                        // apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                        return RLOS_Constants.ERROR;
                    }
                } else {
                    apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, "No response from Server", "", "", "FAIL");
                }

                Log.consoleLog(ifr, "Response==>" + Response);

            } else {
                Log.consoleLog(ifr, "NESL Request already is in progress..");
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            Log.errorLog(ifr, "Exception==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    String createNESLRequest_eSigning(IFormReference ifr, String FileList, String DocPath,
            String NESLMode, String ProductType) throws ParseException {
        Log.consoleLog(ifr, "Inside CreateNESLRequest.....NESLMode:" + NESLMode + ":ProductType:" + ProductType);
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        try {

            String CountResult = "";
            String CountQuery = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS "
                    + "WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
                    + "AND NESL_MODE='" + NESLMode + "'";
            Log.consoleLog(ifr, "CountQuery==>" + CountQuery);

            List< List< String>> Result1 = ifr.getDataFromDB(CountQuery);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (Result1.size() > 0) {
                CountResult = Result1.get(0).get(0);
            }
            if (CountResult.equalsIgnoreCase("")) {
                CountResult = "0";
            }

            Log.consoleLog(ifr, "CountResult==>" + CountResult);

            if (Integer.parseInt(CountResult) == 0) {

                String stateCode = pcm.getStateCode(ifr, ProductType, ProcessInstanceId);

                String signatoryAadhar = "";
                String signatoryGender = "M";//Deafult
                String customerFullName = "";
                String customerFirstName = "";
                String customerLastName = "";
                String panNumber = "";
                String mobileNumber = "";
                String emailId = "";
                String loanAmount = "";
                String Tenure = "";
                String roi = "";
                String emi = "";
                String aadharNum = "";
                String dateOfBirth = "";
                String gender = "";

                String query1 = "";
                if (ProductType.equalsIgnoreCase("PAPL")) {
                    query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,MOBILENUMBER,EMAILID,DATEOFBIRTH,GENDER "
                            + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
                } else {
                    query1 = "select a.firstname,a.lastname,b.kyc_no,a.mobileno,a.emailid,to_char(a.dob,'dd-MM-YYYY')dob,a.gender from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
                            + "select f_key from los_nl_basic_info where PID = '" + ProcessInstanceId + "'\n"
                            + ") and a.f_key=b.f_key";
                }

                Log.consoleLog(ifr, "query1==>" + query1);
                List< List< String>> Result3 = ifr.getDataFromDB(query1);
                Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
                if (Result3.size() > 0) {
                    customerFirstName = Result3.get(0).get(0);
                    customerLastName = Result3.get(0).get(1);
                    customerFullName = customerFirstName.replace("{}", "") + " " + customerLastName.replace("{}", "");
                    panNumber = Result3.get(0).get(2);
                    mobileNumber = Result3.get(0).get(3);

                    if (mobileNumber.length() > 10) {
                        mobileNumber = mobileNumber.substring(2, 12);
                        Log.consoleLog(ifr, "MobileNumber==>" + mobileNumber);
                    }
                    emailId = Result3.get(0).get(4);
                    dateOfBirth = Result3.get(0).get(5);
                    gender = Result3.get(0).get(6);

                    if (gender.equalsIgnoreCase("MALE")) {
                        signatoryGender = "M";
                    } else if (gender.equalsIgnoreCase("FEMALE")) {
                        signatoryGender = "F";
                    } else {
                        signatoryGender = "M";//Transgender is not supporting by NESL
                    }
                }

                Log.consoleLog(ifr, "customerFullName==>" + customerFullName);
                Log.consoleLog(ifr, "panNumber=========>" + panNumber);
                Log.consoleLog(ifr, "mobileNumber======>" + mobileNumber);
                Log.consoleLog(ifr, "emailId===========>" + emailId);
                Log.consoleLog(ifr, "dateOfBirth=======>" + dateOfBirth);
                Log.consoleLog(ifr, "gender============>" + gender);

                HashMap<String, String> customerdetails = new HashMap<>();
                customerdetails.put("MobileNumber", mobileNumber);
                aadharNum = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
                Log.consoleLog(ifr, "aadharNum==>" + aadharNum);
                signatoryAadhar = aadharNum;
                Log.consoleLog(ifr, "signatoryAadhar/getCustomerAccountSummary==>" + signatoryAadhar);
                signatoryAadhar = signatoryAadhar.substring(8, 12);
                Log.consoleLog(ifr, "signatoryAadhar==>" + signatoryAadhar);

                String DOB = "";
                try {
                    DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
                    DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = originalFormat.parse(dateOfBirth);
                    DOB = targetFormat.format(date);
                    Log.consoleLog(ifr, "DOB==>" + DOB);
                } catch (Exception e) {
                    Log.consoleLog(ifr, "Exception DOB==>" + e);
                }

                String query2 = "";
                if (ProductType.equalsIgnoreCase("PAPL")) {
                    query2 = "select loanamount,tenure,rateofinterest,emi from "
                            + "los_tran_papl_finaleligibility WHERE WINAME='" + ProcessInstanceId + "'";
                } else if (ProductType.equalsIgnoreCase("Pension")) {
                    query2 = "SELECT LOAN_AMT,TENURE,ROI,EMI from "
                            + "LOS_PEN_FINAL_ELIGIBILITY WHERE PID='" + ProcessInstanceId + "'";
                } else {
                    query2 = "SELECT LOANAMOUNT,Tenure,RATEOFINTEREST,EMI from "
                            + "LOS_TRN_FINALELIGIBILITY WHERE WINAME='" + ProcessInstanceId + "'";
                }
                Log.consoleLog(ifr, "#query2===>" + query2);
                List< List< String>> Result4 = ifr.getDataFromDB(query2);
                Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
                if (Result4.size() > 0) {
                    loanAmount = Result4.get(0).get(0);
                    Tenure = Result4.get(0).get(1);
                    roi = Result4.get(0).get(2);
                    emi = Result4.get(0).get(3);
                }

                Log.consoleLog(ifr, "LoanAmount==>" + loanAmount);
                Log.consoleLog(ifr, "Tenure======>" + Tenure);
                Log.consoleLog(ifr, "ROI=========>" + roi);
                Log.consoleLog(ifr, "EMI=========>" + emi);

                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String DateofSanction = dateFormat.format(currentDate);

                String DocumentDataSnippet = cm.getDocBase64Content(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "DocumentDataSnippet=========>" + DocumentDataSnippet);

                String eSignCordinatesSnippet = cm.getSignCoordinate(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "eSignCordinatesSnippet=========>" + eSignCordinatesSnippet);
                if (eSignCordinatesSnippet.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                String DocumentCountSnippet = cm.getDocDetailsCount(ifr, FileList, DocPath);
                Log.consoleLog(ifr, "DocumentCountSnippet=========>" + DocumentCountSnippet);
                if (DocumentCountSnippet.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                Date currentDate1 = new Date();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("ddMMyyHHmmssSSS");
                String ReferenceId = dateFormat1.format(currentDate1);

                String LoanNo = ProcessInstanceId;
                String[] DataSplitter = LoanNo.split("-");
                String WIRefNo = cm.removeZero(DataSplitter[1]);
                System.out.println("WIRefNo==>" + WIRefNo);
                String NESLRefNo = "NESL_" + WIRefNo;
                String SancRefNo = "SANC_" + WIRefNo;
                Log.consoleLog(ifr, "NESLRefNo=========>" + NESLRefNo);
                Log.consoleLog(ifr, "SancRefNo=========>" + SancRefNo);

                String Request = "";

                String TxnId = "NESL_" + ReferenceId;
                Request = "{\n"
                        + "    \"loan\": {\n"
                        + "        \"loanno\": \"" + NESLRefNo + "\",\n"
                        + "        \"snctnno\": \"" + SancRefNo + "\",\n"
                        + "        \"regType\": \"1\",\n"
                        + "        \"signFlag\": \"1\",\n"
                        + "        \"txnID\": \"" + TxnId + "\",\n"
                        + "        \"loanDocFlag\": \"N\",\n"
                        + "        \"estampFlag\": \"N\",\n"
                        + "        \"state\": \"" + stateCode + "\",\n"
                        + "        \"f2f\": \"Y\",\n"
                        + "        \"estampdtls\": [\n"
                        + "        ],\n"
                        + "        \"prtcptentty\": [\n"
                        + "            {\n"
                        + "                \"prtcptenttyId\": \"1\",\n"
                        + "                \"altemlid\": \"\",\n"
                        + "                \"altmobno\": \"" + mobileNumber + "\",\n"
                        + "                \"cntrprtyaddr\": \"\",\n"
                        + "                \"cntrprtycntmobno\": \"" + mobileNumber + "\",\n"
                        + "                \"cntrprtycntnm\": \"FIRE\",\n"
                        + "                \"comaddr\": \"\",\n"
                        + "                \"doi\": \"" + DOB + "\",\n"
                        + "                \"lglcnstn\": \"Resident Individual\",\n"
                        + "                \"emlid\": \"" + emailId.replace("{}", "") + "\",\n"
                        + "                \"fulnm\": \"" + customerFirstName + "\",\n"
                        + "                \"panno\": \"" + panNumber + "\",\n"
                        + "                \"partytyp\": \"Indian Entity\",\n"
                        + "                \"pin\": \"\",\n"
                        + "                \"regoffpin\": \"\",\n"
                        + "                \"reltocntrct\": \"debtor\",\n"
                        + "                \"ovdtype\": \"\",\n"
                        + "                \"ovdid\": \"\",\n"
                        + "                \"signatoryAadhar\": \"" + signatoryAadhar + "\",\n"
                        + "                \"signatoryGender\": \"" + signatoryGender + "\",\n"
                        + "                \"documentID\": [\n"
                        + DocumentCountSnippet
                        + "                ],\n"
                        + "                \"seqno\": 1\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"loandtls\": {\n"
                        + "            \"chgamt\": \"0\",\n"
                        + "            \"crdtsubtyp\": \"credit facility\",\n"
                        + "            \"currofsanc\": \"INR\",\n"
                        + "            \"dtofsnctn\": \"" + DateofSanction + "\",\n"
                        + "            \"fcltynm\": \"Personal Loan\",\n"
                        + "            \"fundtyp\": \"Funded\",\n"
                        + "            \"isacctclosed\": \"no\",\n"
                        + "            \"ntrofcrdt\": \"financial\",\n"
                        + "            \"rtofint\": \"" + roi + "\",\n"
                        + "            \"snctnamt\": \"" + loanAmount + "\",\n"
                        + "            \"emiamt\": \"" + emi + "\",\n"
                        + "            \"tenure\": \"" + Tenure + "\",\n"
                        + "            \"toutstndamt\": \"0.00\"\n"
                        + "        },\n"
                        + "        \"documentdtls\": [\n"
                        + DocumentDataSnippet
                        + "        ],\n"
                        + "         \"eSignCordinates\": [\n"
                        + eSignCordinatesSnippet
                        + "        ]\n"
                        + "    }\n"
                        + "}";

                Log.consoleLog(ifr, "CreateNESLSigningRequest Ended..");

                HashMap requestHeader = new HashMap<>();
                String Response = cf.CallWebService(ifr, "NESL_InitiateRequest", Request, "", requestHeader);

                //  apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                if (!Response.equalsIgnoreCase("")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(Response);
                    JSONObject resultObj = new JSONObject(OutputJSON);

                    String status = resultObj.get("status").toString();

                    apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                    Log.consoleLog(ifr, "status==>" + status);
                    if (status.equalsIgnoreCase("SUCCESS")) {
                        String transactionId = resultObj.get("transactionId").toString();
                        Log.consoleLog(ifr, "transactionId==>" + transactionId);

                        String Query1 = "INSERT INTO LOS_INTEGRATION_NESL_STATUS(PROCESSINSTANCEID,LOAN_ACC_NO,NESL_TRANS_ID,REQ_STATUS,NESL_MODE) "
                                + "VALUES ('" + ProcessInstanceId + "','" + NESLRefNo + "','" + TxnId + "','N','" + NESLMode + "')";
                        Log.consoleLog(ifr, "Query1==>" + Query1);
                        ifr.saveDataInDB(Query1);

                        String Query2 = "INSERT INTO LOS_NESL_DOC(PID,TXNID) VALUES ('" + ProcessInstanceId + "','" + TxnId + "')";
                        Log.consoleLog(ifr, "Query2==>" + Query2);
                        ifr.saveDataInDB(Query2);
                        return RLOS_Constants.SUCCESS;
                    } else {
                        // apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, Response, "", "", status);
                        return RLOS_Constants.ERROR;
                    }
                } else {
                    apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, "NESL_CreateRequest", Request, "No response from Server", "", "", "FAIL");
                }

                Log.consoleLog(ifr, "Response==>" + Response);

            } else {
                Log.consoleLog(ifr, "NESL Request already is in progress..");
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            Log.errorLog(ifr, "Exception==>" + e);
        }
        return RLOS_Constants.ERROR;
    }
     */
}
