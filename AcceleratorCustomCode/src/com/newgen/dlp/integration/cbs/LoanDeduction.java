/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
*
* @author ranshaw
*/
public class LoanDeduction {

    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();
    

    public String getLoanDeductionDetails(IFormReference ifr, String ProcessInstanceId,
            String LoanAccountNumber, String SanctionAmount, String ProductType) {

        Log.consoleLog(ifr, "Entered into Execute_CBSLoanDeduction...");

        String apiName = "LoanDeduction";
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
            String DisbursementMode = "2";

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
            String formattedDate = dateFormat.format(currentDate);

            String BankCode = pcm.getConstantValue(ifr, "CBSLOANDED", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSLOANDED", "CHANNEL");
            String S_UserId = pcm.getConstantValue(ifr, "CBSLOANDED", "SC_USERID");
            String UserId = pcm.getConstantValue(ifr, "CBSLOANDED", "USERID");

            //Modified by Ahmed on 01-07-2024 for Budget
            String flgTenorElongReduction = "";
            String rateRegime = "";
            if (ProductType.equalsIgnoreCase("PAPL")) {
                flgTenorElongReduction = pcm.getConstantValue(ifr, "CBSLOANDED", "TENORREDFLAG");//Newly added on 29/03/2024
                rateRegime = pcm.getConstantValue(ifr, "CBSLOANDED", "RATEREGIME");//Newly added on 29/03/2024
            } else {
                flgTenorElongReduction = pcm.getTenureElongation(ifr);
                rateRegime = pcm.getTypeOfInterestRegime(ifr);
            }

            String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

            request = "{\n"
                    + "  \"input\": {\n"
                    + "    \"AccountID\": \"" + LoanAccountNumber.trim() + "\",\n"
                    + "    \"AmtMargin\": \"\",\n"
                    + "    \"CodDealer\": \"\",\n"
                    + "    \"CodMarginacctno\": \"\",\n"
                    + "    \"DisbursementAmount\": \"" + SanctionAmount + "\",\n"
                    + "    \"DisbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
                    + "    \"DisbursementMode\": \"" + DisbursementMode + "\",\n"
                    + "    \"ExposureRefNum\": \"\",\n"
                    + "    \"FlgTenorElongReduction\": \"" + flgTenorElongReduction + "\",\n"
                    + "    \"LineNumber\": \"\",\n"
                    + "    \"RateRegime\": \"" + rateRegime + "\",\n"
                    + "    \"SessionContext\": {\n"
                    + "      \"BankCode\": \"" + BankCode + "\",\n"
                    + "      \"Channel\": \"" + Channel + "\",\n"
                    + "      \"ExternalBatchNumber\": \"\",\n"
                    + "      \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
                    + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "      \"LocalDateTimeText\": \"\",\n"
                    + "      \"OriginalReferenceNo\": \"\",\n"
                    + "      \"OverridenWarnings\": \"\",\n"
                    + "      \"PostingDateText\": \"\",\n"
                    + "      \"ServiceCode\": \"\",\n"
                    + "      \"SessionTicket\": \"\",\n"
                    + "      \"SupervisorContext\": {\n"
                    + "        \"PrimaryPassword\": \"\",\n"
                    + "        \"UserId\": \"" + S_UserId + "\"\n"
                    + "      },\n"
                    + "      \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "      \"UserId\": \"" + UserId + "\",\n"
                    + "      \"UserReferenceNumber\": \"" + formattedDate + "\",\n"
                    + "      \"ValueDateText\": \"\"\n"
                    + "    },\n"
                    + "    \"districtName\": \"\",\n"
                    + "    \"stateName\": \"\"\n"
                    + "  }\n"
                    + "}";

            Log.consoleLog(ifr, "inside  LoanDeductionAPI CALL : ");

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                //System.out.println("Text==>" + Text);
                JSONParser parser = new JSONParser();
                JSONObject TextJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(TextJSON);

                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body :  " + body);

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
                apiStatus = "SUCCESS";
            } else {
                apiStatus = "FAIL";
            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return RLOS_Constants.SUCCESS;
            }

//            //     try {
//            //   String APIName = "CBS_LoanDeduction";
//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  loanDedDetails" + e.getMessage());

        } finally {
            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }

        //Modified by Ahmed on 11-07-2024 for displaying the actual error message without data massaging
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;
        //return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);

    }
    
    public String getLoanDeductionDetailsForHRMS(IFormReference ifr, String ProcessInstanceId,
            String LoanAccountNumber, String SanctionAmount, String ProductType) {

        Log.consoleLog(ifr, "Entered into Execute_CBSLoanDeduction...");

        String apiName = "LoanDeduction";
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
            String DisbursementMode = "2";

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
            String formattedDate = dateFormat.format(currentDate);

            String BankCode = pcm.getConstantValue(ifr, "CBSLOANDED", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSLOANDED", "CHANNEL");
            String S_UserId = pcm.getConstantValue(ifr, "CBSLOANDED", "SC_USERID");
            String UserId = pcm.getConstantValue(ifr, "CBSLOANDED", "USERID");
            
            String ioimorperiod = pcm.getConstantValue(ifr, "STAFFDPN", "IOIMORPERIOD");

            
            String rateRegime = "L";
           // String ioimorperiod = "0";
            

            String tBranch="";
            String selectedBranchCode="";
             String queryBranchCode="select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n" +
         			" from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='"+processInstanceId+"'";
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

            request = "{\n"
                    + "  \"input\": {\n"
                    + "    \"AccountID\": \"" + LoanAccountNumber.trim() + "\",\n"
                    + "    \"AmtMargin\": \"\",\n"
                    + "    \"CodDealer\": \"\",\n"
                    + "    \"CodMarginacctno\": \"\",\n"
                    + "    \"DisbursementAmount\": \"" + SanctionAmount + "\",\n"
                    + "    \"DisbursementDate\": \"" + pcm.getCurrentAPIDate(ifr) + "\",\n"
                    + "    \"DisbursementMode\": \"" + DisbursementMode + "\",\n"
                    + "    \"ExposureRefNum\": \"\",\n"
                    + "    \"FlgTenorElongReduction\": \"\",\n"
                    + "    \"LineNumber\": \"\",\n"
                    + "    \"RateRegime\": \"" + rateRegime + "\",\n"
                    + "    \"SessionContext\": {\n"
                    + "      \"BankCode\": \"" + BankCode + "\",\n"
                    + "      \"Channel\": \"" + Channel + "\",\n"
                    + "      \"ExternalBatchNumber\": \"\",\n"
                    + "      \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
                    + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "      \"LocalDateTimeText\": \"\",\n"
                    + "      \"OriginalReferenceNo\": \"\",\n"
                    + "      \"OverridenWarnings\": \"\",\n"
                    + "      \"PostingDateText\": \"\",\n"
                    + "      \"ServiceCode\": \"\",\n"
                    + "      \"SessionTicket\": \"\",\n"
                    + "      \"SupervisorContext\": {\n"
                    + "        \"PrimaryPassword\": \"\",\n"
                    + "        \"UserId\": \"" + S_UserId + "\"\n"
                    + "      },\n"
                    + "      \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
                    + "      \"UserId\": \"" + UserId + "\",\n"
                    + "      \"UserReferenceNumber\": \"" + formattedDate + "\",\n"
                    + "      \"ValueDateText\": \"\"\n"
                    + "    },\n"
                    + "    \"districtName\": \"\",\n"
                    + "    \"stateName\": \"\",\n"
                    + "    \"salaryGenDateFlag\": \"\",\n"
                    + "    \"salaryDayDatePart\": \"\",\n"
                    + "    \"preferredRepayDatePart\": \"\",\n"
                    + "    \"ioimorperiod\": \""+ioimorperiod + "\"\n"
                    + "  }\n"
                    + "}";

            Log.consoleLog(ifr, "inside  LoanDeductionAPI CALL : ");

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                //System.out.println("Text==>" + Text);
                JSONParser parser = new JSONParser();
                JSONObject TextJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(TextJSON);

                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body :  " + body);

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
                apiStatus = "SUCCESS";
            } else {
                apiStatus = "FAIL";
            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return RLOS_Constants.SUCCESS;
            }


        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  loanDedDetails" + e.getMessage());

        } finally {
            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }

       
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;


    }

    public String getLoanDeductionDetailsForRetail(IFormReference ifr, String ProcessInstanceId,
            String LoanAccountNumber, String SanctionAmount) {
		  APICommonMethods cm = new APICommonMethods();
		  Log.consoleLog(ifr, "Entered into Execute_CBSLoanDeduction...");
		        String apiName = "LoanDeductionRetail";
		        String serviceName = "CBS_" + apiName;
		        String DisbursementMode = "2";
                        String status = "";
                        
		        Date currentDate = new Date();
		        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		        String formattedDate = dateFormat.format(currentDate);

		        String BankCode = cm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");
		        String Channel = cm.getConstantValue(ifr, "AGRIDISB", "CHANNEL");
		        String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
		        String Sc_UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
		       String flgTenorElongReduction = cm.getConstantValue(ifr, "CBSLOANDED", "TENORREDFLAG");//Newly added on 29/03/2024
		        String rateRegime = cm.getConstantValue(ifr, "CBSLOANDED", "RATEREGIME");//Newly added on 29/03/2024

		        String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "GOLD");
				String selectedBranchCode = "";

				selectedBranchCode = ifr.getValue("BRANCHCODEVL").toString();
				String randomnumber = pcm.generateRandomNumber();
				
		        String postingDate = cm.getCurrentAPIDate(ifr);
                        String districtANdStateQuery = "select PERMCITY, PERMSTATE from LOS_TRN_CUSTOMERSUMMARY where winame ='"+ProcessInstanceId+"'";
                        Log.consoleLog(ifr, "districtANdStatequery==>" + districtANdStateQuery);
                        List<List<String>> districtAndStateList = ifr.getDataFromDB(districtANdStateQuery);
                        String districtName = "";
                        String stateName ="";
//                       if (districtAndStateList.size() > 0) {
//                          districtName = districtAndStateList.get(0).get(0);
//                          stateName = districtAndStateList.get(0).get(1);
//                        }
                        Log.consoleLog(ifr, "districtAndStateList==>" + districtAndStateList);
		        String request = "{\n"
		                + "  \"input\": {\n"
		                + "    \"districtName\": \"\",\n"
		                + "    \"stateName\": \"\",\n"
		                + "    \"AccountID\": \"" + LoanAccountNumber.trim() + "\",\n"
		                + "    \"AmtMargin\": \"\",\n"
		                + "    \"CodDealer\": \"\",\n"
		                + "    \"CodMarginacctno\": \"\",\n"
		                + "    \"DisbursementAmount\": \"" + SanctionAmount + "\",\n" // 1st Year limit 
		                + "    \"DisbursementDate\": \"" + cm.getCurrentAPIDate(ifr) + "\",\n"
		                + "    \"DisbursementMode\": \"" + DisbursementMode + "\",\n"
		                + "    \"ExposureRefNum\": \"\",\n"
		                + "    \"FlgTenorElongReduction\": \"" + flgTenorElongReduction + "\",\n"
		                + "    \"LineNumber\": \"\",\n"
		                + "    \"RateRegime\": \"" + rateRegime + "\",\n"
		                + "    \"SessionContext\": {\n"
		                + "      \"BankCode\": \"" + BankCode + "\",\n"
		                + "      \"Channel\": \"" + Channel + "\",\n"
		                + "      \"ExternalBatchNumber\": \"\",\n"
		                + "      \"ExternalReferenceNo\": \"" + formattedDate +randomnumber+ "\",\n"
		                + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
		                + "      \"LocalDateTimeText\": \" \",\n"
		                + "      \"OriginalReferenceNo\": \"\",\n"
		                + "      \"OverridenWarnings\": \"\",\n"
		                + "      \"PostingDateText\": \"\",\n"
		                + "      \"ServiceCode\": \"\",\n"
		                + "      \"SessionTicket\": \"\",\n"
		                + "      \"SupervisorContext\": {\n"
		                + "        \"PrimaryPassword\": \"\",\n"
		                + "        \"UserId\": \"" + Sc_UserId + "\"\n"
		                + "      },\n"
		                + "      \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
		                + "      \"UserId\": \"" + UserId + "\",\n"
		                + "      \"UserReferenceNumber\": \"" + formattedDate +randomnumber+ "\",\n"
		                + "      \"ValueDateText\": \"\"\n"
		                + "    },\n"
                                + "    \"salaryGenDateFlag\": \"\",\n" 
                                + "    \"salaryDayDatePart\": \"\",\n" 
                                + "    \"preferredRepayDatePart\": \"\",\n" 
                                + "    \"refPreReleaseAudNo\": \"\",\n" 
                                + "    \"ioimorperiod\": \"0\""
		                + "  }\n"
		                + "}";


		        Log.consoleLog(ifr, "inside  LoanDeductionAPI CALL : ");
		        try {
		            HashMap<String, String> requestHeader = new HashMap<>();

		            Log.consoleLog(ifr, "Request====>" + request);
		            String response = cm.getWebServiceResponse(ifr, apiName, request);
		            Log.consoleLog(ifr, "Response===>" + response);

		            String ErrorCode = "";
		            String ErrorMessage = "";
		            if (!response.equalsIgnoreCase("{}")) {
		                //System.out.println("Text==>" + Text);
		                JSONParser parser = new JSONParser();
		                JSONObject TextJSON = (JSONObject) parser.parse(response);
		                JSONObject resultObj = new JSONObject(TextJSON);

		                String body = resultObj.get("body").toString();
		                Log.consoleLog(ifr, "body :  " + body);

		                JSONObject bodyJSON = (JSONObject) parser.parse(body);
		                JSONObject bodyJSONObj = new JSONObject(bodyJSON);
		                JSONObject bodyObj = new JSONObject(bodyJSONObj);

		                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
		                if (CheckError.equalsIgnoreCase("true")) {
		                    Log.consoleLog(ifr, "Insie CheckError.equalsIgnoreCase(\"true\")");
		                } else {
		                    String[] ErrorData = CheckError.split("#");
		                    ErrorCode = ErrorData[0];
		                    ErrorMessage = ErrorData[1];
		                }
		            } else {
		                response = "No response from the server.";
		                ErrorMessage = "No response from the CBS server.";
		            }

		            //     String APIName = "CBS_LoanDeduction";
		            String APIStatus = "";
		            if (ErrorMessage.equalsIgnoreCase("")) {
		                APIStatus = RLOS_Constants.SUCCESS;
                                status = RLOS_Constants.SUCCESS;
		            } else {
		                APIStatus = "FAIL";
                                status = ErrorMessage;
		            }

		            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
		                    ErrorCode, ErrorMessage, APIStatus);

		            

		        } catch (Exception e) {
		            Log.consoleLog(ifr, "Exception in  loanDedDetails" + e.getMessage());
                            status= e.getMessage();
		        }

		        return status;
	}


}
