/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class TFLEnquiry {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String executeTFLEnquiry(IFormReference ifr, String processInstanceId, String journeyType, String accountNo) {

        Log.consoleLog(ifr, "Entered into executeTFLEnquiry...");

        String apiName = "TFLEnquiry";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {
//        String errorCode = "";
//        String errorMessage = "";
//        String apiStatus = "";
            String TFLAmtUtilized = "";

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
            String bankCode = pcm.getConstantValue(ifr, "CBSTFLENQ", "BANKCODE");//15
            String channel = pcm.getConstantValue(ifr, "CBSTFLENQ", "CHANNEL");//IFE
            String userId = pcm.getConstantValue(ifr, "CBSTFLENQ", "USERID");//MOBILE01
            String tBranch = cm.GetHomeBranchCode(ifr, processInstanceId, journeyType);

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"Operation\": \"specialPackageTFLEnquiry\",\n"
                    + "        \"Service\": \"XfaceExtSpecialPackageTFLEnquirySPIWrapper\",\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"\"\n"
                    + "            },\n"
                    + "            \"BankCode\": \"" + bankCode + "\",\n"
                    + "            \"Channel\": \"" + channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"\",\n"
                    + "            \"ServiceCode\": \"\",\n"
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"TransactionBranch\": \"" + tBranch + "\",\n"
                    + "            \"UserId\": \"" + userId + "\",\n"
                    + "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"AccountNo\": \"" + accountNo + "\"\n"
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

                    String XfaceExtSpecialTflAccountLimitDTO = bodyObj.get("XfaceExtSpecialTflAccountLimitDTO").toString();
                    JSONObject XfaceExtSpecialTflAccountLimitDTOObj = (JSONObject) parser.parse(XfaceExtSpecialTflAccountLimitDTO);
                    TFLAmtUtilized = XfaceExtSpecialTflAccountLimitDTOObj.get("TFLAmtUtilized").toString();
                    Log.consoleLog(ifr, "TFLAmtUtilized=>" + TFLAmtUtilized);

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

            JSONObject jobj = new JSONObject();
            jobj.put("apiStatus", apiStatus);
            jobj.put("TFLAmtUtilized", TFLAmtUtilized);
            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return jobj.toString();
            }

//            if (errorMessage.equalsIgnoreCase("")) {
//                apiStatus = RLOS_Constants.SUCCESS;
//            } else {
//                apiStatus = RLOS_Constants.ERROR;
//            }
//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    errorCode, errorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/executeTFLEnquiry===>" + e);
            Log.errorLog(ifr, "Exception/executeTFLEnquiry===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        //Modified by Ahmed on 11-07-2024 for displaying the actual error message without data massaging
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;
        //return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);

    }

}
