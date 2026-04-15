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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class AadharVault {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String pushData(IFormReference ifr, String aadharNumber) {
        Log.consoleLog(ifr, "Entered into pushData...");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "AadharVaultPushData";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {

            String refKey = "";

            String userName = pcm.getConstantValue(ifr, "CBSVAULT", "USERNAME");
            String apiKey = pcm.getConstantValue(ifr, "CBSVAULT", "APIKEY");

            request = "{\n"
                    + "    \"USER_NAME\": \"" + userName + "\",\n"
                    + "    \"API_KEY\": \"" + apiKey + "\",\n"
                    + "    \"uid\": \"" + aadharNumber + "\",\n"
                    + "    \"additionalData1\": \"Additional Data\",\n"
                    + "    \"uidToken\": \"" + aadharNumber + "\"\n"
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
                    String vaultData = bodyObj.get("vaultData").toString();
                    JSONObject vaultDataObj = (JSONObject) parser.parse(vaultData);
                    refKey = vaultDataObj.get("refKey").toString();
                    Log.consoleLog(ifr, "refKey=>" + refKey);
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

//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    apiErrorCode, apiErrorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");
            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return refKey;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/pushData===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

    public String getDataByReferenceKey(IFormReference ifr, String refKey) {
        Log.consoleLog(ifr, "Entered into getDataByReferenceKey...");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "AadharVaultGetData";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        String aadharNo = "";

        try {

            String userName = pcm.getConstantValue(ifr, "CBSVAULT", "USERNAME");
            String apiKey = pcm.getConstantValue(ifr, "CBSVAULT", "APIKEY");

            request = "{\n"
                    + "    \"USER_NAME\": \"" + userName + "\",\n"
                    + "    \"API_KEY\": \"" + apiKey + "\",\n"
                    + "    \"refKey\": \"" + refKey + "\"\n"
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
                    String vaultData = bodyObj.get("vaultData").toString();
                    JSONObject vaultDataObj = (JSONObject) parser.parse(vaultData);
                    aadharNo = vaultDataObj.get("uid").toString();
                    Log.consoleLog(ifr, "aadharNo=>" + aadharNo);
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
            
//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    errorCode, errorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return aadharNo;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/pushData===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

//    //Added by Ahmed on 14-06-2024 for fetching Aadhar Number from AadharVault
//    public String getAadharNoFromVault(IFormReference ifr, String productType, String Fkey) {
//        Log.consoleLog(ifr, "#getAadharNoFromVault started");
//
//        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String qryAadharRefNo = "";
//        if (productType.equalsIgnoreCase("PAPL")) {
//            qryAadharRefNo = "SELECT AADHARNO FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                    + "WHERE WINAME='" + processInstanceId + "' AND ROWNUM=1";
//        } else {
//
//            qryAadharRefNo = "select b.kyc_no from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
//                    + "select f_key from los_nl_basic_info where PID = '" + processInstanceId + "'\n"
//                    + ") and a.f_key=b.f_key and a.f_key='" + Fkey + "' AND b.kyc_id='AA'";
//        }
//
//        Log.consoleLog(ifr, "qryAadharRefNo=====>" + qryAadharRefNo);
//        List< List< String>> qryAadharRefNoResult = ifr.getDataFromDB(qryAadharRefNo);
//        Log.consoleLog(ifr, "#qryAadharRefNoResult===>" + qryAadharRefNoResult.toString());
//        String aadharNumber = "";
//        if (qryAadharRefNoResult.isEmpty()) {
//            Log.consoleLog(ifr, "No Aadhar RefNo found");
//            return RLOS_Constants.ERROR;
//        } else {
//            String aadharRefKeyNo = qryAadharRefNoResult.get(0).get(0);
//            Log.consoleLog(ifr, "aadharRefKeyNo===>" + aadharRefKeyNo);
//            AadharVault av = new AadharVault();
//            aadharNumber = av.getDataByReferenceKey(ifr, aadharRefKeyNo);
//            Log.consoleLog(ifr, "aadharNumber==>" + aadharNumber);
//            if (aadharNumber.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            }
//        }
//        return aadharNumber;
//
//    }
}
