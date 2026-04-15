/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
*
* @author ranshaw
*/
public class DisbursementEnquiry {

	CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String updateCBSDisbursementEnquiry(IFormReference ifr, String ProcessInsatnceId, String LoanAccountNumber,
			String BranchDPCode, String ProductType) throws ParseException {

		String apiName = "DisbursementEnquiry";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
			// String APIName = "CBS_DisbursementEnquiry";
//            String ErrorCode = "";
//            String ErrorMessage = "";

			Log.consoleLog(ifr, "Entered into Execute_CBSDisbursementEnquiry...");
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

			String BankCode = pcm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSDISBENQ", "CHANNEL");
			String Sc_UserId = pcm.getConstantValue(ifr, "CBSDISBENQ", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSDISBENQ", "USERID");
			// String TBranch = pcm.getConstantValue(ifr, "CBSPAPLLOANACC", "TBRANCH");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);
			request = "{\n" + "  \"input\": {\n" + "    \"AccountId\": \"" + LoanAccountNumber + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + Sc_UserId + "\"\n"
					+ "      },\n" + "      \"TransactionBranch\": \"" + TBranch + "\",\n" + "      \"UserId\": \""
					+ UserId + "\",\n" + "      \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ValueDateText\": \"\"\n" + "    }\n" + "  }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String Query1 = "";
					Log.consoleLog(ifr, "getActivityName===>" + ifr.getActivityName());
					Log.consoleLog(ifr, "ProductType=======>" + ProductType);

					if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
						// Commeneted by Ahmed on 13-06-2024 for wrong param passing
						// if (ifr.getActivityId().equalsIgnoreCase("portal")) {

						if (ProductType.equalsIgnoreCase("PAPL")) {

							// Modified by Ahmed on 24-07-2024
//                            Query1 = "UPDATE LOS_T_IBPS_LOAN_DETAILS "
//                                    + "SET DISB_AMOUNT='0' "
//                                    + "WHERE WINAME='" + ProcessInsatnceId + "'";
//                            Log.consoleLog(ifr, "Query1===>" + Query1);
//                            ifr.saveDataInDB(Query1);
							String qryUpdateDisbAmnt = ConfProperty.getQueryScript("PAPL_UPDATEDISBAMTQRY")
									.replace("#DISB_AMOUNT#", "0").replace("#WINAME#", ProcessInstanceId);
							Log.consoleLog(ifr, "DisbursementEnquiry:updateCBSDisbursementEnquiry:qryUpdateDisbAmnt->"
									+ qryUpdateDisbAmnt);
							ifr.saveDataInDB(qryUpdateDisbAmnt);

						} else {

//                            Query1 = "UPDATE LOS_TRN_LOANDETAILS "
//                                    + "SET DISB_AMOUNT='0' "
//                                    + "WHERE PID ='" + ProcessInsatnceId + "'";
//                            Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
//                      
							String qryUpdateDisbAmnt = ConfProperty.getQueryScript("UPDATEDISBAMTQRY")
									.replace("#DISB_AMOUNT#", "0").replace("#WINAME#", ProcessInstanceId);
							Log.consoleLog(ifr, "DisbursementEnquiry:updateCBSDisbursementEnquiry:qryUpdateDisbAmnt->"
									+ qryUpdateDisbAmnt);
							ifr.saveDataInDB(qryUpdateDisbAmnt);
						}
					} else {
						Query1 = ConfProperty.getQueryScript("INSERTLOANACCOUNTDETAILS").replaceAll("#WINAME#",
								ProcessInstanceId);
						cf.mExecuteQuery(ifr, Query1, "Update disbur details  ");
						Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);

					}

				} else {
					String[] ErrorData = CheckError.split("#");
					apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				apiErrorMessage = "FAIL";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			}

//            try {
//
//                String APIStatus = "";
//                if (ErrorMessage.equalsIgnoreCase("")) {
//                    APIStatus = "SUCCESS";
//                } else {
//                    APIStatus = "FAIL";
//                }
//
//                cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                        ErrorCode, ErrorMessage, APIStatus);
//
//                if (APIStatus.equalsIgnoreCase("FAIL")) {
//                    return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//                } else {
//                    return RLOS_Constants.SUCCESS;
//                }
//
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
	}

	public String updateCBSDisbursementEnquiryForHRMS(IFormReference ifr, String ProcessInsatnceId,
			String LoanAccountNumber, String BranchDPCode, String ProductType) throws ParseException {

		String apiName = "DisbursementEnquiry";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {

			Log.consoleLog(ifr, "Entered into Execute_CBSDisbursementEnquiry...");

			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

			String BankCode = pcm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSBRDISB", "CHANNEL");
			String Sc_UserId = pcm.getConstantValue(ifr, "CBSDISBENQ", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSDISBENQ", "USERID");
			String tBranch = "";
			String selectedBranchCode = "";
			String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
					+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
			Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
			List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
			if (!result.isEmpty()) {
				tBranch = result.get(0).get(0);
			}

			String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='"
					+ processInstanceId + "' ";
			Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
			List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
			if (!branchCodeResult.isEmpty()) {
				selectedBranchCode = branchCodeResult.get(0).get(0);
			}
			if (branchCodeResult.isEmpty()) {
				selectedBranchCode = tBranch;
			}

			request = "{\n" + "  \"input\": {\n" + "    \"AccountId\": \"" + LoanAccountNumber + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + Sc_UserId + "\"\n"
					+ "      },\n" + "      \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "      \"UserId\": \""
					+ UserId + "\",\n" + "      \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ValueDateText\": \"\"\n" + "    }\n" + "  }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String Query1 = "";
					Log.consoleLog(ifr, "getActivityName===>" + ifr.getActivityName());
					Log.consoleLog(ifr, "ProductType=======>" + ProductType);

					Query1 = "UPDATE SLOS_TRN_LOANDETAILS " + "SET DISB_AMOUNT='0' " + "WHERE PID ='"
							+ ProcessInsatnceId + "'";
					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
					int count = ifr.saveDataInDB(Query1);
					Log.consoleLog(ifr, "count:" + count);
//                      

				} else {
					String[] ErrorData = CheckError.split("#");
					apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				apiErrorMessage = "FAIL";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

	public String updateCBSDisbursementEnquiryForRetail(IFormReference ifr, String ProcessInsatnceId,
			String LoanAccountNumber) throws ParseException {

		Log.consoleLog(ifr, "Entered into Execute_CBSDisbursementEnquiry...");

		String status = "";
		String apiName = "DisbursementEnquiryRetail";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String formattedDate = dateFormat.format(currentDate);

		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

		try {
			String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
			String Channel = cm.getConstantValue(ifr, "CBSDISBENQ", "CHANNEL");
			String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
			String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");

			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInsatnceId, "GOLD");
			String selectedBranchCode = "";

//				String querySelectedBranchCode = "select disbursement_brancode from glos_l_loansummary where winame='"
//						+ ProcessInsatnceId + "' ";
//				Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
//				List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
//				if (branchCodeResult.size() > 0) {
//					selectedBranchCode = branchCodeResult.get(0).get(0);
//					
//				}
//				if (!Optional.ofNullable(selectedBranchCode).isPresent()) {
//					selectedBranchCode = TBranch;
//				}

			selectedBranchCode = ifr.getValue("BRANCHCODEVL").toString();
			String randomnumber = pcm.generateRandomNumber();

			String postingDate = cm.getCurrentAPIDate(ifr);

			String request = "{\n" + "  \"input\": {\n" + "    \"AccountId\": \"" + LoanAccountNumber + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + formattedDate + randomnumber + "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"" + postingDate + "\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + Sc_UserId + "\"\n"
					+ "      },\n" + "      \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
					+ "      \"UserId\": \"" + UserId + "\",\n" + "      \"UserReferenceNumber\": \"" + formattedDate
					+ randomnumber + "\",\n" + "      \"ValueDateText\": \"\"\n" + "    }\n" + "  }\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			String response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);
			String ErrorCode = "";
			String ErrorMessage = "";

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (!CheckError.equalsIgnoreCase("true")) {
					String[] ErrorData = CheckError.split("#");
					ErrorCode = ErrorData[0];
					ErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				ErrorMessage = "No response from the CBS server.";
			}

			try {
				// String APIName = "CBS_DisbursementEnquiry";
				String APIStatus = "";
				if (ErrorMessage.equalsIgnoreCase("")) {
					APIStatus = "SUCCESS";
					status = RLOS_Constants.SUCCESS;
				} else {
					APIStatus = "FAIL";
					status = ErrorMessage;
				}

				cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, ErrorCode,
						ErrorMessage, APIStatus);
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
				status = e.getMessage();
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
			status = e.getMessage();

		}
		return status;
	}
}
