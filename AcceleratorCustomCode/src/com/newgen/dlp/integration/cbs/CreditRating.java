/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class CreditRating {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String inquireInternalCreditRating(IFormReference ifr, String customerId, String loanAccountNumber, String journeyType) {
        Log.consoleLog(ifr, "Entered into InquireInternalCreditRating...");

        String apiName = "InquireInternalCreditRating";
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

            String bankCode = pcm.getConstantValue(ifr, "CBSINQCR", "BANKCODE");
            String channel = pcm.getConstantValue(ifr, "CBSINQCR", "CHANNEL");
            String userId = pcm.getConstantValue(ifr, "CBSINQCR", "USERID");
            String suserId = pcm.getConstantValue(ifr, "CBSINQCR", "S_USERID");
            String tBranch = cm.GetHomeBranchCode(ifr, processInstanceId, journeyType);

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"AccountNo\": \"" + loanAccountNumber + "\",\n"
                    + "        \"CustomerId\": \"" + customerId + "\",\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"BankCode\": \"" + bankCode + "\",\n"
                    + "            \"Channel\": \"" + channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"\",\n"
                    + "            \"ServiceCode\": \"\",\n"
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"" + suserId + "\"\n"
                    + "            },\n"
                    + "            \"TransactionBranch\": \"" + tBranch + "\",\n"
                    + "            \"UserId\": \"" + userId + "\",\n"
                    + "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        }\n"
                    + "    }\n"
                    + "}";

            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {

                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(response);
                String Status = resultObj.get("Status").toString();
                Log.consoleLog(ifr, "Status==>" + Status);
                JSONObject statusObj = (JSONObject) parser.parse(Status);
                apiErrorCode = statusObj.get("ErrorCode").toString();
                if ((apiErrorCode.equalsIgnoreCase("0")) || (apiErrorCode.equalsIgnoreCase(""))) {
                    String dateofCreditRating = resultObj.get("DateofCreditRating").toString();
                    Log.consoleLog(ifr, "dateofCreditRating==>" + dateofCreditRating);
                    apiStatus = RLOS_Constants.SUCCESS;
                } else {
                    apiStatus = RLOS_Constants.ERROR;
                }

            } else {
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
                apiStatus = RLOS_Constants.ERROR;
            }

//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    apiErrorCode, apiErrorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");
            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/inquireInternalCreditRating===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

    public String maintainInternalCreditRating(IFormReference ifr, String customerId, String journeyType, JSONObject objCredMain) {
        Log.consoleLog(ifr, "Entered into maintainInternalCreditRating...");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "MaintainInternalCreditRating";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
            String formattedDate = dateFormat.format(currentDate);

            String bankCode = pcm.getConstantValue(ifr, "CBSINQCR", "BANKCODE");
            String channel = pcm.getConstantValue(ifr, "CBSINQCR", "CHANNEL");
            String userId = pcm.getConstantValue(ifr, "CBSINQCR", "USERID");
            String suserId = pcm.getConstantValue(ifr, "CBSINQCR", "S_USERID");
            String tBranch = cm.GetHomeBranchCode(ifr, processInstanceId, journeyType);

            String action = objCredMain.get("Action").toString();//A
            String accountNo = objCredMain.get("AccountNo").toString();//loanaccount
            String internalRatingSrl = objCredMain.get("InternalRatingSrl").toString();//1
            String ratingModel = objCredMain.get("RatingModel").toString();//16
            String internalCreditGrade = objCredMain.get("InternalCreditGrade").toString();//as per CRG
            String dateOfCreditRating = objCredMain.get("DateOfCreditRating").toString();//Sanctiondate //yyyymmdd
            String yearOfABS = objCredMain.get("YearOfABS").toString();//
            String isThisRatingDowngrade = objCredMain.get("IsThisRatingDowngrade").toString();//3
            String dateOfSigningBalanceSheet = objCredMain.get("DateOfSigningBalanceSheet").toString();//Sanctiondate
            String riskCategory = objCredMain.get("RiskCategory").toString();//as per CRG
            String dateOfCreditExpiry = objCredMain.get("DateOfCreditExpiry").toString();//Calculate 15 month
           
            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"" + suserId + "\"\n"
                    + "            },\n"
                    + "            \"BankCode\": \"" + bankCode + "\",\n"
                    + "            \"Channel\": \"" + channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"\",\n"
                    + "            \"ServiceCode\": \"\",\n"
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"TransactionBranch\": \"" + tBranch + "\",\n"
                    + "            \"UserId\": \"" + userId + "\",\n"
                    + "            \"UserReferenceNumber\": \"" + formattedDate + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"Action\": \"" + action + "\",\n"
                    + "        \"CustomerId\": \"" + customerId + "\",\n"
                    + "        \"AccountNo\": \"" + accountNo + "\",\n"
                    + "        \"InternalRatingSrl\": \"" + internalRatingSrl + "\",\n"
                    + "        \"RatingModel\": \"" + ratingModel + "\",\n"
                    + "        \"InternalCreditGrade\": \"" + internalCreditGrade + "\",\n"
                    + "        \"DateOfCreditRating\": \"" + dateOfCreditRating + "\",\n"
                    + "        \"YearOfABS\": \"" + yearOfABS + "\",\n"
                    + "        \"IsThisRatingDowngrade\": \"" + isThisRatingDowngrade + "\",\n"
                    + "        \"DateOfSigningBalanceSheet\": \"" + dateOfSigningBalanceSheet + "\",\n"
                    + "        \"RiskCategory\": \"" + riskCategory + "\",\n"
                    + "        \"DateOfCreditExpiry\": \"" + dateOfCreditExpiry + "\"\n"
                    + "    }\n"
                    + "}";

            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {

                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(response);
                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body==>" + body);

                JSONObject bodyObj = (JSONObject) parser.parse(body);
                String checkError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + checkError);
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

//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    errorCode, errorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");
//
//            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                return RLOS_Constants.SUCCESS;
//            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/MaintainInternalCreditRating===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

}
