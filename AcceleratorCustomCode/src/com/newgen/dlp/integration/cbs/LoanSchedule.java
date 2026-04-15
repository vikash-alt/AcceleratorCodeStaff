/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
*
* @author ranshaw
*/
public class LoanSchedule {

	CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String computeLoanSchedule(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SanctionedAmount, String ProductType) {

		String apiName = "ComputeLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		try {
			// String APIName = "CBS_ComputeLoanSchedule";
//        String ErrorCode = "";
//        String ErrorMessage = "";

			Log.consoleLog(ifr, "Entered into computeLoanSchedule...");
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "USERID");

			String debitTypeDeductionAmount = pcm.getConstantValue(ifr, "CBSLOANSCH", "DTDAMOUNT");
			String holidayPeriod = pcm.getConstantValue(ifr, "CBSLOANSCH", "HOLIDAYPERIOD");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

			// Modified by Ahmed on 01-07-2024 for Budget
			// String rateRegime = pcm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			String rateRegime = "";
			if (ProductType.equalsIgnoreCase("PAPL")) {
				rateRegime = pcm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			} else {
				rateRegime = pcm.getTypeOfInterestRegime(ifr);
			}

			// Commented by Ahmed on 24-06-2024 for taking dynamic prod & sub prod code from
			// master
//            String productCode = "";
//            String subProductCode = "";
//            if (ProductType.equalsIgnoreCase("PAPL")) {
//                productCode = "PL";
//                subProductCode = "STP-PAPL";
//            } else if (ProductType.equalsIgnoreCase("BUDGET")) {
//                productCode = "PL";
//                subProductCode = "STP-CB";
//            }
			String productCode = pcm.getProductCode(ifr);
			String subProductCode = pcm.getSubProductCode(ifr);
			Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
			String ScheduleCode = "";
			if (ProductType.equalsIgnoreCase("PAPL")) {
				ScheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");
			} else {
				ScheduleCode = pcm.getScheduleCode(ifr);
			}
			request = "{\n" + "  \"input\": {\n" + "    \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + S_UserId + "\"\n"
					+ "      },\n" + "      \"TransactionBranch\": \"" + TBranch + "\",\n" + "      \"UserId\": \""
					+ UserId + "\",\n" + "      \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ValueDateText\": \"\"\n" + "    },\n" + "    \"accountId\": \"" + LoanAccountNumber
					+ "\",\n" + "    \"debitTypeDeductionAmount\": \"" + debitTypeDeductionAmount + "\",\n"
					+ "    \"definitionDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
					+ "    \"disbursementAmount\": \"" + SanctionedAmount + "\",\n" + "    \"holidayPeriod\": \""
					+ holidayPeriod + "\",\n" + "    \"preferredRepayDatePart\": \"\",\n"
					+ "    \"salaryDayDatePart\": \"\",\n" + "    \"salaryGenDateFlag\": \"\",\n"
					+ "    \"scheduleTypeCode\": \"" + ScheduleCode + "\"\n" + "  }\n" + "}";

//            String Request = "{\n"
//                    + "    \"input\": {\n"
//                    + "        \"SessionContext\": {\n"
//                    + "            \"SupervisorContext\": {\n"
//                    + "                \"PrimaryPassword\": \"\",\n"
//                    + "                \"UserId\": \"" + S_UserId + "\"\n"
//                    + "            },\n"
//                    + "            \"BankCode\": \"" + BankCode + "\",\n"
//                    + "            \"Channel\": \"" + Channel + "\",\n"
//                    + "            \"ExternalBatchNumber\": \"\",\n"
//                    + "            \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
//                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
//                    + "            \"LocalDateTimeText\": \"\",\n"
//                    + "            \"OriginalReferenceNo\": \"\",\n"
//                    + "            \"OverridenWarnings\": \"\",\n"
//                    + "            \"PostingDateText\": \"\",\n"
//                    + "            \"ServiceCode\": \"\",\n"
//                    + "            \"SessionTicket\": \"\",\n"
//                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
//                    + "            \"UserId\": \"" + UserId + "\",\n"
//                    + "            \"UserReferenceNumber\": \"" + formattedDate + "\",\n"
//                    + "            \"ValueDateText\": \"\"\n"
//                    + "        },\n"
//                    + "        \"accountId\": \"" + LoanAccountNumber + "\",\n"
//                    + "        \"scheduleTypeCode\": \"" + ScheduleCode + "\",\n"
//                    + "        \"definitionDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
//                    + "        \"disbursementAmount\": \"" + SanctionedAmount + "\",\n"
//                    + "        \"debitTypeDeductionAmount\": \"0\",\n"
//                    + "        \"holidayPeriod\": \"0\"\n"
//                    + "    }\n"
//                    + "}";
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			String SessionId = "";

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);
				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);

				if (checkError.equalsIgnoreCase("true")) {
					String LoanDisbursementComputeScheduleResponse = bodyObj
							.get("LoanDisbursementComputeScheduleResponse").toString();
					JSONObject LoanDisbursementComputeScheduleResponseJSON = (JSONObject) parser
							.parse(LoanDisbursementComputeScheduleResponse);
					JSONObject LoanDisbursementComputeScheduleResponseJSONObj = new JSONObject(
							LoanDisbursementComputeScheduleResponseJSON);

					SessionId = LoanDisbursementComputeScheduleResponseJSONObj.get("SessionId").toString();
					Log.consoleLog(ifr, "SessionId==>" + SessionId);
				} else {
					String[] ErrorData = checkError.split("#");
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
				return SessionId;
			}

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//                SessionId = RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
//            return SessionId;
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception computeLoanSchedule==>" + e);
			Log.errorLog(ifr, "Exception computeLoanSchedule==>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String computeLoanScheduleForHRMS(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SanctionedAmount, String ProductType) {

		String apiName = "ComputeLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		try {

			Log.consoleLog(ifr, "Entered into computeLoanSchedule...");

			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");

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

			String debitTypeDeductionAmount = "0";
			String rateRegime = "L";
			String holidayPeriod = "0";

			String productCode = pcm.getProductCode(ifr);
			String subProductCode = pcm.getSubProductCode(ifr);
			Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
			String ScheduleCode = "2001";

			request = "{\n" + "  \"input\": {\n" + "    \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + S_UserId + "\"\n"
					+ "      },\n" + "      \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "      \"UserId\": \""
					+ UserId + "\",\n" + "      \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "      \"ValueDateText\": \"\"\n" + "    },\n" + "    \"accountId\": \"" + LoanAccountNumber
					+ "\",\n" + "    \"debitTypeDeductionAmount\": \"" + debitTypeDeductionAmount + "\",\n"
					+ "    \"definitionDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
					+ "    \"disbursementAmount\": \"" + SanctionedAmount + "\",\n" + "    \"holidayPeriod\": \""
					+ holidayPeriod + "\",\n" + "    \"preferredRepayDatePart\": \"\",\n"
					+ "    \"salaryDayDatePart\": \"\",\n" + "    \"salaryGenDateFlag\": \"\",\n"
					+ "    \"scheduleTypeCode\": \"" + ScheduleCode + "\"\n" + "  }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			String SessionId = "";

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);
				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);

				if (checkError.equalsIgnoreCase("true")) {
					String LoanDisbursementComputeScheduleResponse = bodyObj
							.get("LoanDisbursementComputeScheduleResponse").toString();
					JSONObject LoanDisbursementComputeScheduleResponseJSON = (JSONObject) parser
							.parse(LoanDisbursementComputeScheduleResponse);
					JSONObject LoanDisbursementComputeScheduleResponseJSONObj = new JSONObject(
							LoanDisbursementComputeScheduleResponseJSON);

					SessionId = LoanDisbursementComputeScheduleResponseJSONObj.get("SessionId").toString();
					Log.consoleLog(ifr, "SessionId==>" + SessionId);
				} else {
					String[] ErrorData = checkError.split("#");
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
				return SessionId;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception computeLoanSchedule==>" + e);
			Log.errorLog(ifr, "Exception computeLoanSchedule==>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}

		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String generateLoanSchedule(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId, String ProductType) {

		String apiName = "GenerateLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		try {

//        // String APIName = "CBS_GenerateLoanSchedule";
//        String ErrorCode = "";
//        String ErrorMessage = "";
			Log.consoleLog(ifr, "Entered into Execute_CBS_GenerateLoanSchedule...");

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "USERID");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + S_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"OriginalReferenceNo\": \"\",\n" + "            \"OverridenWarnings\": \"\",\n"
					+ "            \"PostingDateText\": \"\",\n" + "            \"ServiceCode\": \"\",\n"
					+ "            \"SessionTicket\": \"\",\n" + "            \"TransactionBranch\": \"" + TBranch
					+ "\",\n" + "            \"UserId\": \"" + UserId + "\",\n"
					+ "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"accountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"sessionId\": \"" + SessionId + "\",\n"
					+ "        \"disbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\"\n" + "    }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			// System.out.println("Text==>" + Text);
			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String InstallmentDate = "";
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (checkError.equalsIgnoreCase("true")) {
					String termloanRes = bodyJSON.get("TermLoanGenerateScheduleResponse").toString();
					JSONObject TermLoanGenerateScheduleResponse = (JSONObject) parser.parse(termloanRes);

					String RepaymentRecordsDTO = TermLoanGenerateScheduleResponse.get("RepaymentRecordsDTO").toString();
					JSONArray RepaymentRecordsDTOObj = (JSONArray) parser.parse(RepaymentRecordsDTO);
					for (int i = 0; i < RepaymentRecordsDTOObj.size(); i++) {
						JSONObject repayObj = (JSONObject) RepaymentRecordsDTOObj.get(i);
						String InstallmentCounter = cf.getJsonValue(repayObj, "InstallmentCounter");
						if (InstallmentCounter.equalsIgnoreCase("1")) {
							InstallmentDate = cf.getJsonValue(repayObj, "InstallmentDate");

							// Added by Sravani fr Installment Amount...on 09-1-2024
							String InstallmentAmount = cf.getJsonValue(repayObj, "InstallmentAmount");

							Log.consoleLog(ifr, "InstallmentDate:" + InstallmentDate);
							SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
							SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
							Date emiDate = sd.parse(InstallmentDate);
							String InstallmentDateForm = sd1.format(emiDate);
							String query = null;
							// Added by Sravani fr Installment Amount...on 09-1-2024
							if (ifr.getActivityName().equalsIgnoreCase("portal")) // /Added by Monesh on 11-06-2024 for
																					// back office
							{
								if (ProductType.equalsIgnoreCase("PAPL")) {

									// Modidied by Ahmed on 24-07-2024 for queryReading from Prop file
//                                    query = "update LOS_T_IBPS_LOAN_DETAILS "
//                                            + "set EMIDate='" + InstallmentDateForm + "',"
//                                            + "INSTALLMENTAMOUNT='" + InstallmentAmount + "' "
//                                            + " where WIName='" + ProcessInstanceId + "'";
//                                    Log.consoleLog(ifr, "query:" + query);
//                                    // String query = "update LOS_T_IBPS_LOAN_DETAILS set EMIDate='" + InstallmentDateForm
//                                    //         + "' where WIName='" + ProcessInstanceId + "'";
//                                    Log.consoleLog(ifr, "Query:" + query);
//                                    int count = ifr.saveDataInDB(query);
//                                    Log.consoleLog(ifr, "count:" + count);
									query = ConfProperty.getQueryScript("PAPL_UPDATEMIDTLSQRY")
											.replace("#EMIDATE#", InstallmentDateForm)
											.replace("#INSTALLMENTAMOUNT#", InstallmentAmount)
											.replace("#WINAME#", ProcessInstanceId);
									Log.consoleLog(ifr, "LoanSchedule:generateLoanSchedule:query->" + query);
									ifr.saveDataInDB(query);
								} else {
									// Modidied by Ahmed on 24-07-2024 for queryReading from Prop file
//                                    query = "update LOS_TRN_LOANDETAILS "
//                                            + "set EMI_STARTDATE='" + InstallmentDateForm + "',"
//                                            + "EMI_AMOUNT='" + InstallmentAmount + "' "
//                                            + " where PID='" + ProcessInstanceId + "'";
//                                    Log.consoleLog(ifr, "query:" + query);
//                                    int count = ifr.saveDataInDB(query);
//                                    Log.consoleLog(ifr, "count:" + count);

									query = ConfProperty.getQueryScript("UPDATEMIDTLSQRY")
											.replace("#EMI_STARTDATE#", InstallmentDateForm)
											.replace("#EMI_AMOUNT#", InstallmentAmount)
											.replace("#WINAME#", ProcessInstanceId);
									Log.consoleLog(ifr, "LoanSchedule:generateLoanSchedule:query->" + query);
									ifr.saveDataInDB(query);
								}
							} else {
								query = ConfProperty.getQueryScript("UPDATEEMIDETAILSQUERY")
										.replaceAll("#WINAME#", ProcessInstanceId)
										.replaceAll("#EMI_STARTDATE#", InstallmentDateForm)
										.replaceAll("#EMI_AMOUNT#", InstallmentAmount);
								cf.mExecuteQuery(ifr, query, "Update EMI details  ");
								Log.consoleLog(ifr,
										"LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + query);
							}
						}
					}
				} else {
					String[] ErrorData = checkError.split("#");
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
				return RLOS_Constants.SUCCESS;
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
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String generateLoanScheduleForHRMS(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId, String ProductType) {

		String apiName = "GenerateLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String SessId= "0";
		try {

			Log.consoleLog(ifr, "Entered into Execute_CBS_GenerateLoanSchedule...");

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
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

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + S_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"OriginalReferenceNo\": \"\",\n" + "            \"OverridenWarnings\": \"\",\n"
					+ "            \"PostingDateText\": \"\",\n" + "            \"ServiceCode\": \"\",\n"
					+ "            \"SessionTicket\": \"\",\n" + "            \"TransactionBranch\": \"" + selectedBranchCode
					+ "\",\n" + "            \"UserId\": \"" + UserId + "\",\n"
					+ "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"accountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"sessionId\": \"" + SessId + "\",\n"
					+ "        \"disbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\"\n" + "    }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			// System.out.println("Text==>" + Text);
			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String InstallmentDate = "";
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (checkError.equalsIgnoreCase("true")) {
					String termloanRes = bodyJSON.get("TermLoanGenerateScheduleResponse").toString();
					JSONObject TermLoanGenerateScheduleResponse = (JSONObject) parser.parse(termloanRes);

					String RepaymentRecordsDTO = TermLoanGenerateScheduleResponse.get("RepaymentRecordsDTO").toString();
					JSONArray RepaymentRecordsDTOObj = (JSONArray) parser.parse(RepaymentRecordsDTO);
					for (int i = 0; i < RepaymentRecordsDTOObj.size(); i++) {
						JSONObject repayObj = (JSONObject) RepaymentRecordsDTOObj.get(i);
						String InstallmentCounter = cf.getJsonValue(repayObj, "InstallmentCounter");
						if (InstallmentCounter.equalsIgnoreCase("1")) {
							InstallmentDate = cf.getJsonValue(repayObj, "InstallmentDate");

							// Added by Sravani fr Installment Amount...on 09-1-2024
							String InstallmentAmount = cf.getJsonValue(repayObj, "InstallmentAmount");

							Log.consoleLog(ifr, "InstallmentDate:" + InstallmentDate);
							SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
							SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
							Date emiDate = sd.parse(InstallmentDate);
							String InstallmentDateForm = sd1.format(emiDate);
							String query = null;
							// Added by Sravani fr Installment Amount...on 09-1-2024

							query = "update SLOS_TRN_LOANDETAILS " + "set EMI_STARTDATE='" + InstallmentDateForm + "',"
									+ "EMI_AMOUNT='" + InstallmentAmount + "' " + " where PID='" + ProcessInstanceId
									+ "'";
							Log.consoleLog(ifr, "query:" + query);
							int count = ifr.saveDataInDB(query);
							Log.consoleLog(ifr, "count:" + count);

							Log.consoleLog(ifr, "LoanSchedule:generateLoanSchedule:query->" + query);
							ifr.saveDataInDB(query);

						}
					}
				} else {
					String[] ErrorData = checkError.split("#");
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
				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}

		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String updateLoanSchedule(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId, String SanctionedAmount, String ProductType) {
		// Date currentDate = new Date();

		Log.consoleLog(ifr, "Entered into Execute_CBSSaveLoanSchedule...");

		String apiName = "SaveLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			// String APIName = "CBS_SaveLoanSchedule";
//            String ErrorCode = "";
//            String ErrorMessage = "";
			// Commented by Ahmed on 24-06-2024 for taking dynamic prod & sub prod code from
			// master
//            String productCode = "";
//            String subProductCode = "";
//            if (ProductType.equalsIgnoreCase("PAPL")) {
//                productCode = "PL";
//                subProductCode = "STP-PAPL";
//            } else if (ProductType.equalsIgnoreCase("BUDGET")) {
//                productCode = "PL";
//                subProductCode = "STP-CB";
//            }
			String productCode = pcm.getProductCode(ifr);
			String subProductCode = pcm.getSubProductCode(ifr);
			Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

			String ScheduleCode = "";
			if (ProductType.equalsIgnoreCase("PAPL")) {
				ScheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");
			} else {
				ScheduleCode = pcm.getScheduleCode(ifr);
			}
			// pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE",
			// "SCODE");

			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "USERID");

			// Modified by Ahmed on 01-07-2024 for Budget
			// String rateRegime = pcm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			String rateRegime = "";
			if (ProductType.equalsIgnoreCase("PAPL")) {
				rateRegime = pcm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			} else {
				rateRegime = pcm.getTypeOfInterestRegime(ifr);
			}

			// String TBranch = pcm.getConstantValue(ifr, "CBSPAPLLOANACC", "TBRANCH");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

			// Modified by Ahmed on 20-06-2024 for SaveSchedule Request change from Bank
			request = "{\n" + "    \"input\": {\n" + "    \"SessionContext\": {\n"
					+ "        \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + S_UserId + "\"\n" + "        },\n" + "        \"BankCode\": \""
					+ BankCode + "\",\n" + "        \"Channel\": \"" + Channel + "\",\n"
					+ "        \"ExternalBatchNumber\": \"\",\n" + "        \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "        \"LocalDateTimeText\": \"\",\n" + "        \"OriginalReferenceNo\": \"\",\n"
					+ "        \"OverridenWarnings\": \"\",\n" + "        \"PostingDateText\": \"\",\n"
					+ "        \"SessionTicket\": \"\",\n" + "        \"TransactionBranch\": \"" + TBranch + "\",\n"
					+ "        \"UserId\": \"LAPSMAKER\",\n" + "        \"UserReferenceNumber\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"ValueDateText\": \"\"\n" + "    },\n"
					+ "    \"AccountId\": \"" + LoanAccountNumber + "\",\n" + "    \"SessionId\": \"0\",\n"
					+ "    \"DisbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
					+ "    \"ReasonCode\": \"9101\",\n" + "    \"ReasonDescription\": \"First Disbursement\",\n"
					+ "    \"DisbursementTransactionAmount\": \"" + SanctionedAmount + "\",\n"
					+ "    \"ScheduleCode\": \"" + ScheduleCode + "\",\n" + "    \"ArrearsToBeCapitalized\": \"0\",\n"
					+ "    \"DeductTypeDeductionAmount\": \"0\",\n" + "    \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "    \"DisbursementMode\": \"2\",\n" + "     \"XfaceLoanDeductionDetailsDTO\": {\n"
					+ "         \"AccountCurrencyCode\": \"104\",\n"
					+ "         \"ConversionRateAccountCurrency\": \"1\",\n"
					+ "         \"ConversionRateTransactionCurrency\": \"1\",\n" + "         \"CtrSrlDednNo\": \"\",\n"
					+ "         \"DeductionAmountInAccountCurrency\": \"\",\n"
					+ "         \"DeductionAmountInLocalCurrency\": \"\",\n"
					+ "         \"DeductionAmountInTransactionCurrency\": \"\",\n"
					+ "         \"DeductionCode\": \"\",\n" + "         \"DeductionCurrencyCode\": \"104\",\n"
					+ "         \"DeductionMode\": \"1\",\n" + "         \"DeductionType\": \"0\",\n"
					+ "         \"FlgDueOn\": \"1\",\n" + "         \"IsSCDeductionAmortised\": \"false\",\n"
					+ "         \"IsWaived\": \"false\",\n" + "         \"LocalCurrencyCode\": \"104\",\n"
					+ "         \"ServiceChargeName\": \"\",\n" + "         \"SrlNumber\": \"1\",\n"
					+ "         \"TransactionCurrencyCode\": \"104\"\n" + "        },\n"
					+ "        \"FlgMCLRMovement\": \"\"\n" + "    }\n" + "}";

//            String Request = "{\n"
//                    + "    \"input\": {\n"
//                    + "        \"SessionContext\": {\n"
//                    + "            \"SupervisorContext\": {\n"
//                    + "                \"PrimaryPassword\": \"\",\n"
//                    + "                \"UserId\": \"" + S_UserId + "\"\n"
//                    + "            },\n"
//                    + "            \"BankCode\": \"" + BankCode + "\",\n"
//                    + "            \"Channel\": \"" + Channel + "\",\n"
//                    + "            \"ExternalBatchNumber\": \"\",\n"
//                    + "            \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
//                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
//                    + "            \"LocalDateTimeText\": \"\",\n"
//                    + "            \"OriginalReferenceNo\": \"\",\n"
//                    + "            \"OverridenWarnings\": \"\",\n"
//                    + "            \"PostingDateText\": \"\",\n"
//                    + "            \"SessionTicket\": \"\",\n"
//                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
//                    + "            \"UserId\": \"" + UserId + "\",\n"
//                    + "            \"UserReferenceNumber\": \"" + formattedDate + "\",\n"
//                    + "            \"ValueDateText\": \"\"\n"
//                    + "        },\n"
//                    + "        \"AccountId\": \"" + LoanAccountNumber + "\",\n"
//                    + "        \"SessionId\": \"0\",\n"
//                    + "        \"DisbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
//                    + "        \"ReasonCode\": \"9101\",\n"
//                    + "        \"ReasonDescription\": \"First Disbursement\",\n"
//                    + "        \"DisbursementTransactionAmount\": \"" + SanctionedAmount + "\",\n"
//                    + "        \"ScheduleCode\": \"" + ScheduleCode + "\",\n"
//                    + "        \"ArrearsToBeCapitalized\": \"0\",\n"
//                    + "        \"DeductTypeDeductionAmount\": \"0\",\n"
//                    + "        \"RateRegime\": \"" + rateRegime + "\",\n"
//                    + "        \"DisbursementMode\": \"2\",\n"
//                    + "        \"XfaceLoanDeductionDetailsDTO\": {},\n"
//                    + "        \"FlgMCLRMovement\": \"\"\n"
//                    + "    }\n"
//                    + "}";
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (checkError.equalsIgnoreCase("true")) {

				} else {
					String[] ErrorData = checkError.split("#");
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
//                //  String APIName = "CBS_SaveLoanSchedule";
//                String APIStatus = "";
//                if (ErrorMessage.equalsIgnoreCase("")) {
//                    APIStatus = "SUCCESS";
//                } else {
//                    APIStatus = "FAIL";
//                }
//
//                cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
//                        ErrorCode, ErrorMessage, APIStatus);
//
//                if (APIStatus.equalsIgnoreCase("FAIL")) {
//                    return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//                } else {
//                    return RLOS_Constants.SUCCESS;
//                }
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String updateLoanScheduleForHRMS(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId, String SanctionedAmount, String ProductType) {
		// Date currentDate = new Date();

		Log.consoleLog(ifr, "Entered into Execute_CBSSaveLoanSchedule...");

		String apiName = "SaveLoanSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
			String productCode = pcm.getProductCode(ifr);
			String subProductCode = pcm.getSubProductCode(ifr);
			Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

			String ScheduleCode = "2001";
			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "USERID");

			String rateRegime = "L";

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

			String extUniqueRefId = generateUniqueID(17);

			request = "{\n" + "    \"input\": {\n" + "    \"SessionContext\": {\n"
					+ "        \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + S_UserId + "\"\n" + "        },\n" + "        \"BankCode\": \""
					+ BankCode + "\",\n" + "        \"Channel\": \"" + Channel + "\",\n"
					+ "        \"ExternalBatchNumber\": \"\",\n" + "        \"ExternalReferenceNo\": \""
					+ extUniqueRefId + "\",\n" + "        \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "        \"LocalDateTimeText\": \"\",\n" + "        \"OriginalReferenceNo\": \"\",\n"
					+ "        \"OverridenWarnings\": \"\",\n" + "        \"PostingDateText\": \"\",\n"
					+ "        \"SessionTicket\": \"\",\n" + "        \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
					+ "        \"UserId\": \"" + UserId + "\",\n" + "        \"UserReferenceNumber\": \""
					+ extUniqueRefId + "\",\n" + "        \"ValueDateText\": \"\"\n" + "    },\n"
					+ "    \"AccountId\": \"" + LoanAccountNumber + "\",\n" + "    \"SessionId\": \"0\",\n"
					+ "    \"DisbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
					+ "    \"ReasonCode\": \"9101\",\n" + "    \"ReasonDescription\": \"First Disbursement\",\n"
					+ "    \"DisbursementTransactionAmount\": \"" + SanctionedAmount + "\",\n"
					+ "    \"ScheduleCode\": \"" + ScheduleCode + "\",\n" + "    \"ArrearsToBeCapitalized\": \"0\",\n"
					+ "    \"DeductTypeDeductionAmount\": \"0\",\n" + "    \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "    \"DisbursementMode\": \"2\",\n" + "     \"XfaceLoanDeductionDetailsDTO\": [{\n"
					+ "         \"AccountCurrencyCode\": \"104\",\n"
					+ "         \"ConversionRateAccountCurrency\": \"1\",\n"
					+ "         \"ConversionRateTransactionCurrency\": \"1\",\n" + "         \"CtrSrlDednNo\": \"\",\n"
					+ "         \"DeductionAmountInAccountCurrency\": \"\",\n"
					+ "         \"DeductionAmountInLocalCurrency\": \"\",\n"
					+ "         \"DeductionAmountInTransactionCurrency\": \"\",\n"
					+ "         \"DeductionCode\": \"\",\n" + "         \"DeductionCurrencyCode\": \"104\",\n"
					+ "         \"DeductionMode\": \"1\",\n" + "         \"DeductionType\": \"0\",\n"
					+ "         \"FlgDueOn\": \"1\",\n" + "         \"IsSCDeductionAmortised\": \"false\",\n"
					+ "         \"IsWaived\": \"false\",\n" + "         \"LocalCurrencyCode\": \"104\",\n"
					+ "         \"ServiceChargeName\": \"\",\n" + "         \"SrlNumber\": \"1\",\n"
					+ "         \"TransactionCurrencyCode\": \"104\"\n" + "        }],\n"
					+ "        \"FlgMCLRMovement\": \"\"\n" + "    }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (checkError.equalsIgnoreCase("true")) {

				} else {
					String[] ErrorData = checkError.split("#");
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
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}

		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public static String generateUniqueID(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(10)); // Appending digits from 0 to 9
		}

		return sb.toString();
	}

	public String repaymentLoanSchedule(IFormReference ifr, String ProcessInstanceId, String accountId,
			String SessionId, String sanctionedAmount, String ProductType) {

		Log.consoleLog(ifr, "Entered into repaymentLoanSchedule...");

		String apiName = "RepaymentSchedule";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

//        String ErrorCode = "";
//        String ErrorMessage = "";
		try {
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			// String APIName = "CBS_SaveLoanSchedule";
			// Commented by Ahmed on 24-06-2024 for taking dynamic prod & sub prod code from
			// master
//            String productCode = "";
//            String subProductCode = "";
//            if (ProductType.equalsIgnoreCase("PAPL")) {
//                productCode = "PL";
//                subProductCode = "STP-PAPL";
//            } else if (ProductType.equalsIgnoreCase("BUDGET")) {
//                productCode = "PL";
//                subProductCode = "STP-CB";
//            }
			String productCode = pcm.getProductCode(ifr);
			String subProductCode = pcm.getSubProductCode(ifr);
			Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

			String scheduleCode = "";
			// pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE",
			// "SCODE");
			if (ProductType.equalsIgnoreCase("PAPL")) {
				scheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");
			} else {
				scheduleCode = pcm.getScheduleCode(ifr);
			}
			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANSCH", "USERID");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

			request = "{\n" + "            \"input\":{\n" + "               \"SessionContext\":{\n"
					+ "                  \"SupervisorContext\":{ \n"
					+ "                     \"PrimaryPassword\":\"\",\n" + "                     \"UserId\":\""
					+ S_UserId + "\" \n" + "                  },\n" + "                  \"BankCode\":\"" + BankCode
					+ "\",\n" + "                  \"Channel\":\"" + Channel + "\",\n"
					+ "                  \"ExternalBatchNumber\":\"\", \n"
					+ "                  \"ExternalReferenceNo\":\"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "                  \"ExternalSystemAuditTrailNumber\":\"\",\n"
					+ "                  \"LocalDateTimeText\":\"\", \n"
					+ "                  \"OriginalReferenceNo\":\"\",\n"
					+ "                  \"OverridenWarnings\":\"\",\n"
					+ "                  \"PostingDateText\":\"\",\n" + "                  \"SessionTicket\":\"\",\n"
					+ "                  \"TransactionBranch\":\"" + TBranch + "\", \n"
					+ "                  \"UserId\":\"" + UserId + "\", \n"
					+ "                  \"UserReferenceNumber\":\"" + cm.getCBSExternalReferenceNo() + "\", \n"
					+ "                  \"ValueDateText\":\"\"\n" + "               },\n"
					+ "               \"AccountId\":\"" + accountId + "\",\n" + "               \"SessionId\":\"0\", \n"
					+ "               \"DisbursementDate\":\"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
					+ "               \"ReasonCode\":\"9101\", \n"
					+ "               \"ReasonDescription\":\"First Disbursement\",\n"
					+ "               \"DisbursementTransactionAmount\":\"" + sanctionedAmount + "\", \n"
					+ "               \"ScheduleCode\":\"" + scheduleCode + "\", \n"
					+ "               \"ArrearsToBeCapitalized\":\"0\",\n"
					+ "               \"DeductTypeDeductionAmount\":\"500\",\n"
					+ "               \"DisbursementMode\":\"2\",\n"
					+ "               \"XfaceLoanDeductionDetailsDTO\":{},\n"
					+ "               \"FlgMCLRMovement\":\"\"\n" + "            }\n" + "         }";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {

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
//                //  String APIName = "CBS_SaveLoanSchedule";
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
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String computeLoanScheduleForRetail(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SanctionedAmount, String productCode) {
		Log.consoleLog(ifr, "Entered into computeLoanSchedule...");
		String apiName = "ComputeLoanScheduleRetail";
		String serviceName = "CBS_" + apiName;
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String formattedDate = dateFormat.format(currentDate);
		String randomnumber=pcm.generateRandomNumber();
          
                String ErrorCode = "";
                String ErrorMessage = "";
                String SessionId = "";
		try {


		
			String subProductCode = "GOLD";

			String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
			String Channel = cm.getConstantValue(ifr, "AGRIDISB", "CHANNEL");
			String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
			String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
			 String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "GOLD");
				String selectedBranchCode = "";

//				String querySelectedBranchCode = "select disbursement_brancode from glos_l_loansummary where winame='"
//						+ ProcessInstanceId + "' ";
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
			String ScheduleCode = cm.getConfigValue(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");
			String rateRegime = "L";
			// String rateRegime = cm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			String debitTypeDeductionAmount = "0";
			// String debitTypeDeductionAmount = cm.getConstantValue(ifr, "CBSLOANSCH",
			// "DTDAMOUNT");
			String holidayPeriod = "0";
			// String holidayPeriod = cm.getConstantValue(ifr, "CBSLOANSCH",
			// "HOLIDAYPERIOD");

			String scheduleTypeCode = "";
			String scheduleCodequery = "select distinct(COD_SCHED_TYPE)  from ALOS_GOLD_SCHED_TYPES where cod_prod='"
					+ productCode + "'";
			List<List<String>> queryRes = ifr.getDataFromDB(scheduleCodequery);
			if(queryRes.size() > 0){
                            scheduleTypeCode = queryRes.get(0).get(0);
                        }
                        
                       
			String disbursementMode = "2";

			String request = "{\n" + "  \"input\": {\n" + "    \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "    \"SessionContext\": {\n" + "      \"BankCode\": \"" + BankCode + "\",\n"
					+ "      \"Channel\": \"" + Channel + "\",\n" + "      \"ExternalBatchNumber\": \"\",\n"
					+ "      \"ExternalReferenceNo\": \"" + formattedDate +randomnumber+ "\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" // need to check if it exception
					+ "      \"ServiceCode\": \"\",\n" + "      \"SessionTicket\": \"\",\n"
					+ "      \"SupervisorContext\": {\n" + "        \"PrimaryPassword\": \"\",\n"
					+ "        \"UserId\": \"" + Sc_UserId + "\"\n" + "      },\n" + "      \"TransactionBranch\": \""
					+ selectedBranchCode + "\",\n" + "      \"UserId\": \"" + UserId + "\",\n"
					+ "      \"UserReferenceNumber\": \"" + formattedDate +randomnumber+ "\",\n" + "      \"ValueDateText\": \"\"\n"
					+ "    },\n" + "    \"accountId\": \"" + LoanAccountNumber + "\",\n"
					+ "    \"debitTypeDeductionAmount\": \"" + debitTypeDeductionAmount + "\",\n"
					+ "    \"definitionDate\": \"" + cm.getCurrentAPIDate(ifr) + "\",\n"
					+ "    \"disbursementAmount\": \"" + SanctionedAmount + "\",\n" + "    \"HolidayPeriod\": \""
					+ holidayPeriod + "\",\n" + "    \"disbursementMode\":\"" + disbursementMode + "\",\n"
					+ "    \"preferredRepayDatePart\": \"\",\n" + "    \"salaryDayDatePart\": \"\",\n"
					+ "    \"salaryGenDateFlag\": \"\",\n" + "    \"scheduleTypeCode\":\"" + scheduleTypeCode + "\"\n" // need
																														// to
																														// configure
					+ "  }\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			HashMap<String, String> requestHeader = new HashMap<>();

			Log.consoleLog(ifr, "Request====>" + request);
			String response = cm.getWebServiceResponse(ifr, apiName, request);
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
					String LoanDisbursementComputeScheduleResponse = bodyObj.get("LoanDisbursementComputeScheduleResponse").toString();
                    JSONObject LoanDisbursementComputeScheduleResponseJSON = (JSONObject) parser.parse(LoanDisbursementComputeScheduleResponse);
                    JSONObject LoanDisbursementComputeScheduleResponseJSONObj = new JSONObject(LoanDisbursementComputeScheduleResponseJSON);

                    SessionId = LoanDisbursementComputeScheduleResponseJSONObj.get("SessionId").toString();
                    Log.consoleLog(ifr, "SessionId==>" + SessionId);
				} else {
					String[] ErrorData = CheckError.split("#");
					ErrorCode = ErrorData[0];
					ErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				ErrorMessage = "No response from the CBS server.";
			}

			// String APIName = "CBS_ComputeLoanSchedule";
			String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
			}
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception computeLoanSchedule==>" + e);
			Log.errorLog(ifr, "Exception computeLoanSchedule==>" + e);
                        ErrorMessage = e.getMessage();
		}
		return SessionId+"#"+ErrorMessage;
	}

	public String generateLoanScheduleForRetail(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId) {
		// Date currentDate = new Date();

		Log.consoleLog(ifr, "Entered into Execute_CBS_GenerateLoanSchedule...");
                String status = "";
		String apiName = "LoanScheduleRetail";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String formattedDate = dateFormat.format(currentDate);
		String randomnumber=pcm.generateRandomNumber();

		try {

			 String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
		        String Channel = cm.getConstantValue(ifr, "AGRIDISB", "CHANNEL");
		        String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
		        String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
		        String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "GOLD");
				String selectedBranchCode = "";
				selectedBranchCode = ifr.getValue("BRANCHCODEVL").toString();

			String request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + Sc_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalReferenceNo\": \"" + formattedDate +randomnumber+ "\",\n"
					+ "            \"OriginalReferenceNo\": \"\",\n" + "            \"OverridenWarnings\": \"\",\n"
					+ "            \"PostingDateText\": \"\",\n" + "            \"ServiceCode\": \"\",\n"
					+ "            \"SessionTicket\": \"\",\n" + "            \"TransactionBranch\": \"" + selectedBranchCode
					+ "\",\n" + "            \"UserId\": \"" + UserId + "\",\n"
					+ "            \"UserReferenceNumber\": \"" + formattedDate +randomnumber+ "\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"accountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"sessionId\": \"" + SessionId + "\",\n"
					+ "        \"disbursementDate\": \"" + cm.getCurrentAPIDate(ifr) + "\"\n" + "    }\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);

			HashMap<String, String> requestHeader = new HashMap<>();

			Log.consoleLog(ifr, "Request====>" + request);
			String response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			// System.out.println("Text==>" + Text);
			String ErrorCode = "";
			String ErrorMessage = "";

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String InstallmentDate = "";
				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String termloanRes = bodyJSON.get("TermLoanGenerateScheduleResponse").toString();
					JSONObject TermLoanGenerateScheduleResponse = (JSONObject) parser.parse(termloanRes);

					String RepaymentRecordsDTO = TermLoanGenerateScheduleResponse.get("RepaymentRecordsDTO").toString();
						JSONObject repayObj;
						try {
                                                    repayObj = (JSONObject) parser.parse(RepaymentRecordsDTO);
						} catch(Exception e){
                                                    JSONArray repayArr = (JSONArray) parser.parse(RepaymentRecordsDTO);	
                                                    // If it fails, try parsing as JSONArray
                                                    repayObj = (JSONObject) repayArr.get(0);
						}
						String InstallmentCounter = cf.getJsonValue(repayObj, "InstallmentCounter");
						if (InstallmentCounter.equalsIgnoreCase("1")) {
							InstallmentDate = cf.getJsonValue(repayObj, "InstallmentDate");

							// Added by Sravani fr Installment Amount...on 09-1-2024
							String InstallmentAmount = cf.getJsonValue(repayObj, "InstallmentAmount");

							Log.consoleLog(ifr, "InstallmentDate:" + InstallmentDate);
							SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
							SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
							Date emiDate = sd.parse(InstallmentDate);
							String InstallmentDateForm = sd1.format(emiDate);
							String query = null;

							query = "update SLOS_TRN_LOANDETAILS " + "set EMI_STARTDATE='" + InstallmentDateForm + "',"
									+ "EMI_AMOUNT='" + InstallmentAmount + "' " + " where PID='" + ProcessInstanceId + "'";
							Log.consoleLog(ifr, "query:" + query);
							int count = ifr.saveDataInDB(query);
							Log.consoleLog(ifr, "count:" + count);

						}

				} else {
					String[] ErrorData = CheckError.split("#");
					ErrorCode = ErrorData[0];
					ErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				ErrorMessage = "No response from the CBS server.";
			}

			try {
				String APIStatus = "";
				if (ErrorMessage.equalsIgnoreCase("")) {
					APIStatus = "SUCCESS";
                                        status = RLOS_Constants.SUCCESS;
				} else {
					APIStatus = "FAIL";
                                        status = ErrorMessage;
				}

				cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, ErrorCode, ErrorMessage,
						APIStatus);
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                                status = e.getMessage();
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception" + e);
                         status = e.getMessage();
		}
		return status;

	}

	public String updateLoanScheduleForRetail(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SessionId, String SanctionedAmount, String productCode) {
		// Date currentDate = new Date();

		Log.consoleLog(ifr, "Entered into Execute_CBSSaveLoanSchedule...");
		String apiName = "SaveLoanScheduleRetail";
		String serviceName = "CBS_" + apiName;
		String APIStatus = "";
		String status = "";
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String formattedDate = dateFormat.format(currentDate);
		String randomnumber=pcm.generateRandomNumber();

		try {

			String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
			String Channel = cm.getConstantValue(ifr, "CBSLOANSCH", "CHANNEL");
			String UserId = cm.getConstantValue(ifr, "CBDLP", "GOLDCBSUSER");
			String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
			 String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "GOLD");
				String selectedBranchCode = "";

//				String querySelectedBranchCode = "select disbursement_brancode from glos_l_loansummary where winame='"
//						+ ProcessInstanceId + "' ";
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
			String rateRegime = cm.getConstantValue(ifr, "CBSLOANSCH", "RATEREGIME");
			String ReasonCode = "9101";
			String FirstDisbursement = "First Disbursement";
			String ArrearsToBeCapitalized = "0";
			String DeductTypeDeductionAmount = "0";
			String DisbursementMode = "2";
			
                        
                        String scheduleTypeCode = "";
			String scheduleCodequery = "select distinct(COD_SCHED_TYPE)  from ALOS_GOLD_SCHED_TYPES where cod_prod='"
					+ productCode + "'";
			List<List<String>> queryRes = ifr.getDataFromDB(scheduleCodequery);
			if(queryRes.size() > 0){
                            scheduleTypeCode = queryRes.get(0).get(0);
                        }

			
			String request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + Sc_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + formattedDate +randomnumber+ "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"SessionTicket\": \"\",\n" + "            \"TransactionBranch\": \"" + selectedBranchCode
					+ "\",\n" + "            \"UserId\": \"" + UserId + "\",\n"
					+ "            \"UserReferenceNumber\": \"" + formattedDate +randomnumber+ "\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"AccountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"SessionId\": \"" + SessionId + "\",\n" //
					+ "        \"DisbursementDate\": \"" + cm.getCurrentAPIDate(ifr) + "\",\n"
					+ "        \"ReasonCode\": \"" + ReasonCode + "\",\n" // need configure
					+ "        \"ReasonDescription\": \"" + FirstDisbursement + "\",\n"
					+ "        \"DisbursementTransactionAmount\": \"" + SanctionedAmount + "\",\n"
					+ "        \"ScheduleCode\": \"" + scheduleTypeCode + "\",\n" + "        \"ArrearsToBeCapitalized\":\""
					+ ArrearsToBeCapitalized + "\",\n" + "        \"DeductTypeDeductionAmount\":\""
					+ DeductTypeDeductionAmount + "\",\n" + "        \"RateRegime\": \"" + rateRegime + "\",\n"
					+ "        \"DisbursementMode\":  \"" + DisbursementMode + "\",\n" // configure
					+ "        \"XfaceLoanDeductionDetailsDTO\": [\n" +
                                        "            {\n" +
                                        "                \"AccountCurrencyCode\": \"104\",\n" +
                                        "                \"ConversionRateAccountCurrency\": \"1\",\n" +
                                        "                \"ConversionRateTransactionCurrency\": \"1\",\n" +
                                        "                \"CtrSrlDednNo\": \"\",\n" +
                                        "                \"DeductionAmountInAccountCurrency\": \"\",\n" +
                                        "                \"DeductionAmountInLocalCurrency\": \"\",\n" +
                                        "                \"DeductionAmountInTransactionCurrency\": \"\",\n" +
                                        "                \"DeductionCode\": \"\",\n" +
                                        "                \"DeductionCurrencyCode\": \"104\",\n" +
                                        "                \"DeductionMode\": \"1\",\n" +
                                        "                \"DeductionType\": \"0\",\n" +
                                        "                \"FlgDueOn\": \"1\",\n" +
                                        "                \"IsSCDeductionAmortised\": \"false\",\n" +
                                        "                \"IsWaived\": \"false\",\n" +
                                        "                \"LocalCurrencyCode\": \"104\",\n" +
                                        "                \"ServiceChargeName\": \"\",\n" +
                                        "                \"SrlNumber\": \"1\",\n" +
                                        "                \"TransactionCurrencyCode\": \"\"\n" +
                                        "            },\n" +
                                        "            {\n" +
                                        "                \"AccountCurrencyCode\": \"104\",\n" +
                                        "                \"ConversionRateAccountCurrency\": \"1\",\n" +
                                        "                \"ConversionRateTransactionCurrency\": \"1\",\n" +
                                        "                \"CtrSrlDednNo\": \"\",\n" +
                                        "                \"DeductionAmountInAccountCurrency\": \"\",\n" +
                                        "                \"DeductionAmountInLocalCurrency\": \"\",\n" +
                                        "                \"DeductionAmountInTransactionCurrency\": \"\",\n" +
                                        "                \"DeductionCode\": \"\",\n" +
                                        "                \"DeductionCurrencyCode\": \"104\",\n" +
                                        "                \"DeductionMode\": \"1\",\n" +
                                        "                \"DeductionType\": \"0\",\n" +
                                        "                \"FlgDueOn\": \"1\",\n" +
                                        "                \"IsSCDeductionAmortised\": \"false\",\n" +
                                        "                \"IsWaived\": \"false\",\n" +
                                        "                \"LocalCurrencyCode\": \"104\",\n" +
                                        "                \"ServiceChargeName\": \"\",\n" +
                                        "                \"SrlNumber\": \"1\",\n" +
                                        "                \"TransactionCurrencyCode\": \"\"\n" +
                                        "            }\n" +
                                        "          ],"
					+ "        \"FlgMCLRMovement\": \"\"\n" + "    }\n" + "}";

			// XfaceLoanDeductionDetailsDTO Tag Name
			Log.consoleLog(ifr, "Request====>" + request);
			HashMap<String, String> requestHeader = new HashMap<>();
//             String Response = "";
//              if (ConfProperty.getIntegrationValue("MOCKFLG_SaveLoanSchedule").equalsIgnoreCase("Y")) {
//                 Response=apmr.readMockResponse(ifr,"SaveLoanSchedule");
//                 Log.consoleLog(ifr, "MockResponse:::::::"+Response);
//             }
//                else {
//                    Log.consoleLog(ifr, "calling SaveLoanSchedule  webservice");
//            Response = cf.CallWebService(ifr, "SaveLoanSchedule", Request, "", requestHeader);
//                }
//
//            Log.consoleLog(ifr, "Response===>" + Response);

//added by subham on 16/05/2024 for calling API common method 
			Log.consoleLog(ifr, "Request====>" + request);
			String response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			String ErrorCode = "";
			String ErrorMessage = "";
			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);

				if (!CheckError.equalsIgnoreCase("true")) {
					String[] ErrorData = CheckError.split("#");
					ErrorCode = ErrorData[0];
					ErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				ErrorMessage = "FAIL";
			}

			try {


				if (ErrorMessage.equalsIgnoreCase("")) {
					APIStatus = "SUCCESS";
					status = RLOS_Constants.SUCCESS;
				} else {
					APIStatus = "FAIL";
					status = RLOS_Constants.ERROR;
				}

				cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, ErrorCode, ErrorMessage,
						APIStatus);
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                                status = RLOS_Constants.ERROR;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                        status = RLOS_Constants.ERROR;
		}
		Log.consoleLog(ifr, "LoanSchedule::APIStatus" + APIStatus);
		return status;

	}


}
