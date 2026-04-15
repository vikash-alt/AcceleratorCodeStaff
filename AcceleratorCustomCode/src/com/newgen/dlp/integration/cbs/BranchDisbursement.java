/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class BranchDisbursement {

	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String updateBranchDisbursement(IFormReference ifr, String ProcessInstanceId, String LoanAccountNumber,
			String SBAccountNumber, String LoanAmount, String ProductType) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "BranchDisbursement";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
//        String ErrorCode = "";
//        String ErrorMessage = "";

			Log.consoleLog(ifr, "Entered into CBS_BranchDisbursement...");
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

			String BankCode = pcm.getConstantValue(ifr, "CBSBRDISB", "BANKCODE");
			Log.consoleLog(ifr, "BankCode===>" + BankCode);
			String Channel = pcm.getConstantValue(ifr, "CBSBRDISB", "CHANNEL");
			Log.consoleLog(ifr, "Channel===>" + Channel);
			String SC_UserId = pcm.getConstantValue(ifr, "CBSBRDISB", "SC_USERID");
			Log.consoleLog(ifr, "SC_UserId===>" + SC_UserId);
			String UserId = pcm.getConstantValue(ifr, "CBSBRDISB", "USERID");
			Log.consoleLog(ifr, "UserId===>" + UserId);
			// String TBranch = pcm.getConstantValue(ifr, "CBSPAPLLOANACC", "TBRANCH");
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);
			Log.consoleLog(ifr, "TBranch===>" + TBranch);
			String DMODE = pcm.getConstantValue(ifr, "CBSBRDISB", "DMODE");
			Log.consoleLog(ifr, "DMODE===>" + DMODE);

			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + SC_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo()
					+ "\",\n" + "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"accountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"disbursementMode\": \"" + DMODE + "\",\n"
					+ "        \"fromAccountNo\": \"" + SBAccountNumber + "\",\n"
					+ "        \"beneficiaryName\": \"\",\n" + "        \"beneficiaryAdd1\": \"\",\n"
					+ "        \"beneficiaryAdd2\": \"\",\n" + "        \"beneficiaryAdd3\": \"\",\n"
					+ "        \"benefPassportICNo\": \"\",\n" + "        \"chequeDate\": \"\",\n"
					+ "        \"instrumentNumber\": \"\",\n" + "        \"amount\": \"" + LoanAmount + "\",\n"
					+ "        \"bankCode\": \"\",\n" + "        \"narrative\": \"BranchDisbursement\",\n"
					+ "        \"userReferenceNumber\": \"626\"\n" + "    }\n" + "}";
			Log.consoleLog(ifr, "request===>" + Request);
			// HashMap<String, String> requestHeader = new HashMap<>();

			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			if (!Response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(Response);
				// JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);
				// JSONObject bodyObj = new JSONObject(bodyJSON);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					Date d = new Date();
					SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
					String curDate = sd1.format(d);
					String query = null;
					if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
						if (ProductType.equalsIgnoreCase("PAPL")) {

							// Modified by Ahmed on 24-07-2024 for queryReadingfrom Property
//                            query = "update LOS_T_IBPS_LOAN_DETAILS set DISBDate='" + curDate
//                                    + "' where WIName='" + ProcessInstanceId + "'";
//                            Log.consoleLog(ifr, "Query bdisbursement PAPL:" + query);
//                            
							query = ConfProperty.getQueryScript("PAPL_UPDATEDISBDATEQRY").replace("#DISBDATE#", curDate)
									.replace("#WINAME#", ProcessInstanceId);
							Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:query->" + query);

						} else {
							// Modified by Ahmed on 24-07-2024 for queryReadingfrom Property
//                            query = "update los_trn_loandetails set DISB_DATE='" + curDate
//                                    + "' where PID='" + ProcessInstanceId + "'";
//                            Log.consoleLog(ifr, "Query bdisbursement NOT PAPL:" + query);
							query = ConfProperty.getQueryScript("UPDATEDISBDATEQRY").replace("#DISBDATE#", curDate)
									.replace("#WINAME#", ProcessInstanceId);
							Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:query->" + query);

						}
					} else {
						query = ConfProperty.getQueryScript("UPDATEDISBDETAILSQUERY")
								.replaceAll("#WINAME#", ProcessInstanceId).replaceAll("#DISBDate#", curDate);
					}
					int count = ifr.saveDataInDB(query);
					Log.consoleLog(ifr, "count:" + count);
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
			} else {
				return apiStatus;
			}
//            String APIStatus = "";
//            String APIStatusSend = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = RLOS_Constants.SUCCESS;
//                APIStatusSend = RLOS_Constants.SUCCESS;
//            } else {
//                APIStatus = "FAIL";
//                APIStatusSend = RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
//            return APIStatusSend;
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
	}

	public String updateBranchDisbursementForHRMS(IFormReference ifr, String ProcessInstanceId,
			String LoanAccountNumber, String SBAccountNumber, String LoanAmount, String ProductType) {

		Log.consoleLog(ifr, "Inside updateBranchDisbursementForHRMS SBACCNUMBER==>" + SBAccountNumber);

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "BranchDisbursement";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {

			Log.consoleLog(ifr, "Entered into CBS_BranchDisbursement...");

			String BankCode = pcm.getConstantValue(ifr, "CBSBRDISB", "BANKCODE");
			Log.consoleLog(ifr, "BankCode===>" + BankCode);
			String Channel = pcm.getConstantValue(ifr, "CBSBRDISB", "CHANNEL");
			Log.consoleLog(ifr, "Channel===>" + Channel);
//			String SC_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
//			Log.consoleLog(ifr, "SC_UserId===>" + SC_UserId);
//			String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
//			Log.consoleLog(ifr, "UserId===>" + UserId);
			
			String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
			Log.consoleLog(ifr, "UserId===>" + UserId);
			String SC_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
			Log.consoleLog(ifr, "SC_UserId===>" + SC_UserId);
			
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
			String DMODE = pcm.getConstantValue(ifr, "CBSBRDISB", "DMODE");
			Log.consoleLog(ifr, "DMODE===>" + DMODE);

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + SC_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo()
					+ "\",\n" + "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"accountId\": \""
					+ LoanAccountNumber + "\",\n" + "        \"disbursementMode\": \"" + DMODE + "\",\n"
					+ "        \"fromAccountNo\": \"" + SBAccountNumber + "\",\n"
					+ "        \"beneficiaryName\": \"\",\n" + "        \"beneficiaryAdd1\": \"\",\n"
					+ "        \"beneficiaryAdd2\": \"\",\n" + "        \"beneficiaryAdd3\": \"\",\n"
					+ "        \"benefPassportICNo\": \"\",\n" + "        \"chequeDate\": \"\",\n"
					+ "        \"instrumentNumber\": \"\",\n" + "        \"amount\": \"" + LoanAmount + "\",\n"
					+ "        \"bankCode\": \"\",\n" + "        \"narrative\": \"BranchDisbursement\",\n"
					+ "        \"userReferenceNumber\": \"701\"\n" + "    }\n" + "}";
			Log.consoleLog(ifr, "request===>" + request);

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);

				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					Date d = new Date();
					SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
					String curDate = sd1.format(d);
					String query = null;
					query = "UPDATE SLOS_TRN_LOANDETAILS SET DISB_DATE='" + curDate + "' WHERE PID='"
							+ ProcessInstanceId + "'";
					Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:query->" + query);

					int count = ifr.saveDataInDB(query);
					Log.consoleLog(ifr, "count:" + count);
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
			} else {
				return apiStatus;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

	public String updateBranchDisbursementForGoldRetail(IFormReference ifr, String ProcessInstanceId, 
            String LoanAccountNumber, String SBAccountNumber, String LoanAmount, String ProductType) {
        Log.consoleLog(ifr, "Entered into CBS_BranchDisbursement...");
        String apiName = "BranchDisbursementRetail";
        String serviceName = "CBS_" + apiName;
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
        String formattedDate = dateFormat.format(currentDate);
        try {
            	
        	 String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
		        String Channel = cm.getConstantValue(ifr, "AGRIDISB", "CHANNEL");
		        String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
		        String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
     		
            Log.consoleLog(ifr, "BankCode===>" + BankCode);
        
            
            Log.consoleLog(ifr, "Channel===>" + Channel);
         
            Log.consoleLog(ifr, "UserId===>" + UserId);
     
           String TBranch = ifr.getValue("BRANCHCODEVL").toString();
         
            Log.consoleLog(ifr, "SC_UserId===>" + Sc_UserId);
            String DMODE="2";
            //String DMODE = cm.getConstantValue(ifr, "CBSBRDISB", "DMODE");
            Log.consoleLog(ifr, "DMODE===>" + DMODE);

            String branchDisbursement = "BranchDisbursement";
            String randomnumber=pcm.generateRandomNumber(ifr);
            Log.consoleLog(ifr, "randomnumber===>" + randomnumber);

            String request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"" + Sc_UserId + "\"\n"
                    + "            },\n"
                    + "            \"BankCode\": \"" + BankCode + "\",\n"
                    + "            \"Channel\": \"" + Channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"" + formattedDate+randomnumber + "\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"\",\n"
                    + "            \"ServiceCode\": \"\",\n" // no need to pass
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "            \"UserId\": \"" + UserId + "\",\n"
                    + "            \"UserReferenceNumber\": \"" + formattedDate+randomnumber + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"accountId\": \"" + LoanAccountNumber + "\",\n"
                    + "        \"disbursementMode\": \"" + DMODE + "\",\n"
                    + "        \"fromAccountNo\": \"" + SBAccountNumber + "\",\n"
                    + "        \"beneficiaryName\": \"\",\n"
                    + "        \"beneficiaryAdd1\": \"\",\n"
                    + "        \"beneficiaryAdd2\": \"\",\n"
                    + "        \"beneficiaryAdd3\": \"\",\n"
                    + "        \"benefPassportICNo\": \"\",\n"
                    + "        \"chequeDate\": \"\",\n"
                    + "        \"instrumentNumber\": \"\",\n"
                    + "        \"amount\": \"" + LoanAmount + "\",\n"
                    + "        \"bankCode\": \"\",\n"
                    + "        \"narrative\": \"" + branchDisbursement + "\",\n"
                     + "        \"SCDebitAccount\": \"" + SBAccountNumber + "\",\n"
                    + "        \"userReferenceNumber\":\"" + formattedDate+randomnumber + "\"\n"
                    + "    }\n"
                    + "}";
            Log.consoleLog(ifr, "request===>" + request);
            HashMap<String, String> requestHeader = new HashMap<>();


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
                if (CheckError.equalsIgnoreCase("true")) {
                    ifr.setValue("Q_BranchTypeCode", "Disbursed"); 
                    Log.consoleLog(ifr, "============>Disbursed status updated successfully");
                } else {
                    String[] ErrorData = CheckError.split("#");
                    ErrorCode = ErrorData[0];
                    ErrorMessage = ErrorData[1];
                }

            } else {
                response = "No response from the server.";
                ErrorMessage = "No response from the CBS server.";
            }

            String APIName = "CBS_BranchDisbursement";
            String APIStatus = "";
            String APIStatusSend = "";
            if (ErrorMessage.equalsIgnoreCase("")) {
                APIStatus = RLOS_Constants.SUCCESS;
                APIStatusSend = RLOS_Constants.SUCCESS;
            } else {
                APIStatus = "FAIL";
                APIStatusSend = ErrorMessage;
            }
            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
                    ErrorCode, ErrorMessage, APIStatus);
            return APIStatusSend;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return e.getMessage();
        }
    }

}
