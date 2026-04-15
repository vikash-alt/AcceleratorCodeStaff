/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.perfios;

import com.newgen.dlp.integration.cbs.AccountMiniStatement;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class BSAOrchestrator {

    String perfiosServiceType = "BSA";
    String productType = "RLOS";
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String triggerBSAOrchestrator(IFormReference ifr) {
        Log.consoleLog(ifr, "BSAOrchestrator:triggerBSAOrchestrator-> triggering BSA Orchestrator");

        try {

            String perfBSAEnable = ConfProperty.getCommonPropertyValue("PERFIOS_BSAENABLE");
            perfBSAEnable = (perfBSAEnable != null) ? perfBSAEnable : "NO";
            Log.consoleLog(ifr, "perfBSAEnable?====>" + perfBSAEnable);

            if (perfBSAEnable.equalsIgnoreCase("YES")) {

                String criterion = "'PROCESSED','COMPLETED'";
                String transactionStatus = cm.checkPerfiosCompletedTRNStatus(ifr, perfiosServiceType, criterion);

                if (Integer.parseInt(transactionStatus) == 0) {
                    String accMiniStatementResponse = getAccountMiniStatementResponse(ifr);
                    if (accMiniStatementResponse.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                        String initatedStatus = initateTransaction(ifr);
                        if (!initatedStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                            String uploadStatus = uploadDocuments(ifr);
                            if (!uploadStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                String processStatus = processStatement(ifr);
                                if (!processStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                    return RLOS_Constants.SUCCESS;
                                }
                            }
                        }
                    }
                } else {
                    return RLOS_Constants.SUCCESS;
                }

            } else {
                cm.bypassPerfiosTransaction(ifr, perfiosServiceType);
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String getAccountMiniStatementResponse(IFormReference ifr) {
        try {
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String query = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE "
                    + "TRANSACTION_ID='" + processInstanceId + "' "
                    + "AND API_NAME='CBS_AccountMiniStatement' "
                    + "AND API_STATUS='SUCCESS'";
            List< List< String>> Result = ifr.getDataFromDB(query);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());
            String responseFromDB = "";
            if (!Result.isEmpty()) {
                responseFromDB = Result.get(0).get(0);
            }

            if (responseFromDB.equalsIgnoreCase("")) {
                AccountMiniStatement objAMS = new AccountMiniStatement();
                String response = objAMS.getAccountMiniStatementDetails(ifr, "BUDGET");
                if (response.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                String fileCreatedStatus = createJSONFile(ifr, response);
                if (fileCreatedStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

            } else {
                String fileCreatedStatus = createJSONFile(ifr, responseFromDB);
                if (fileCreatedStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }
            }
            return RLOS_Constants.SUCCESS;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }
        return RLOS_Constants.ERROR;

    }

    private String initateTransaction(IFormReference ifr) {

        try {

            String apiName = "BSAInitiateTransaction";
            String serviceName = "Perfios_" + apiName;
            Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String inCriteria = "'INITIATED','UPLOADED','PROCESSED'";
            String notInCriteria = "'COMPLETED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);
            if (Integer.parseInt(inflowStatus) == 0) {
                String callBSABackURL = pcm.getConstantValue(ifr, "PERFIOS", "BSACALLBACKURL");

                String request = "{\n"
                        + "  \"payload\": {\n"
                        + "    \"processingType\": \"STATEMENT\",\n"
                        + "    \"transactionCompleteCallbackUrl\": \"" + callBSABackURL + "\",\n"
                        + "    \"txnId\": \"" + cm.getClientTransUniqueId(ifr) + "\",\n"
                        + "    \"uploadingScannedStatements\": \"true\"\n"
                        + "  }\n"
                        + "}";
                Log.consoleLog(ifr, "request===>" + request);
                String response = cm.getWebServiceResponse(ifr, apiName, request);
                Log.consoleLog(ifr, "Response===>" + response);

                if (!response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject resultObj = (JSONObject) parser.parse(response);
                    String body = resultObj.get("body").toString();
                    JSONObject bodyObj = (JSONObject) parser.parse(body);
                    String transaction = bodyObj.get("transaction").toString();
                    JSONObject transactionObj = (JSONObject) parser.parse(transaction);
                    String perfiosTransactionId = transactionObj.get("perfiosTransactionId").toString();
                    Log.consoleLog(ifr, "perfiosTransactionId==>" + perfiosTransactionId);

                    String queryInsertPerfId = "INSERT INTO LOS_TRN_PERF_INTEGRATION_DETAILS (PRODUCT,PROCESSINSTANCEID,PERFIOS_TRANSACTIONID,CLIENT_TRANSACTIONID,ENTRYDATETIME,TRANSACTION_STATUS,PERFIOS_SERVICETYPE) \n"
                            + "VALUES ('" + productType + "','" + processInstanceId + "','" + perfiosTransactionId + "','" + cm.getClientTransUniqueId(ifr) + "',SYSDATE,'INITIATED','" + perfiosServiceType + "')";
                    Log.consoleLog(ifr, "queryInsertPerfId==>" + queryInsertPerfId);
                    ifr.saveDataInDB(queryInsertPerfId);

                    return RLOS_Constants.SUCCESS;
                }
            } else {
                Log.consoleLog(ifr, "Transaction already in flow.");
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/initateTransaction==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String uploadDocuments(IFormReference ifr) {

        String apiName = "BSAUploadTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String inCriteria = "'INITIATED'";
            String notInCriteria = "'UPLOADED','PROCESSED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);
            if (Integer.parseInt(inflowStatus) > 0) {

                Log.consoleLog(ifr, "#Uploading Documents to Perfios");

                String BSATempPath = ConfProperty.getIntegrationValue("BSATempPath");
                Log.consoleLog(ifr, "BSATempPath==?" + BSATempPath);
                String uploadFilePath = System.getProperty("user.dir") + File.separator + BSATempPath + File.separator + processInstanceId + File.separator + processInstanceId + ".json";

                String perfTransId = cm.getPerfiosTransId(ifr, processInstanceId, perfiosServiceType);
                Log.consoleLog(ifr, "perfTransId===>" + perfTransId);
                String bsaUploadURL = ConfProperty.getIntegrationValue(apiName);
                bsaUploadURL = bsaUploadURL.replace("#{transactionId}#", perfTransId);
                Log.consoleLog(ifr, "bsaUploadURL===>" + bsaUploadURL);

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("documentType", "json")
                        .addFormDataPart("file", uploadFilePath,
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(uploadFilePath)))
                        .build();
                Request request = new Request.Builder()
                        .url(bsaUploadURL)
                        .method("POST", body)
                        .addHeader("accept", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String outputResponse = response.body().string();

                Log.consoleLog(ifr, "outputResponse===>" + outputResponse);
                if (!outputResponse.equalsIgnoreCase("")) {

                    JSONParser parser = new JSONParser();
                    JSONObject resultObj = (JSONObject) parser.parse(outputResponse);
                    String sBody = resultObj.get("body").toString();
                    JSONObject sBodyObj = (JSONObject) parser.parse(sBody);
                    Log.consoleLog(ifr, "body==>" + body);
                    String sfile = sBodyObj.get("file").toString();
                    JSONObject sfileObj = (JSONObject) parser.parse(sfile);
                    String fileId = sfileObj.get("fileId").toString();
                    Log.consoleLog(ifr, "fileId==>" + fileId);

                    String queryUpdateTRN = "UPDATE LOS_TRN_PERF_INTEGRATION_DETAILS "
                            + "SET TRANSACTION_STATUS='UPLOADED',"
                            + "FILEID='" + fileId + "' "
                            + "WHERE PERFIOS_TRANSACTIONID='" + perfTransId + "' "
                            + "AND PROCESSINSTANCEID='" + processInstanceId + "' "
                            + "AND  PERFIOS_SERVICETYPE='" + perfiosServiceType + "'";
                    Log.consoleLog(ifr, "queryUpdateTRN==>" + queryUpdateTRN);
                    ifr.saveDataInDB(queryUpdateTRN);

                    return RLOS_Constants.SUCCESS;
                }

            } else {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String processStatement(IFormReference ifr) {
        String apiName = "BSAProcessTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {
            String inCriteria = "'UPLOADED'";
            String notInCriteria = "'INITIATED','PROCESSED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);
            if (Integer.parseInt(inflowStatus) > 0) {
                String queryGetDetails = "SELECT PERFIOS_TRANSACTIONID,FILEID FROM LOS_TRN_PERF_INTEGRATION_DETAILS  "
                        + "WHERE PROCESSINSTANCEID='" + processInstanceId + "'  AND  PERFIOS_SERVICETYPE='" + perfiosServiceType + "'";
                List< List< String>> Result = ifr.getDataFromDB(queryGetDetails);
                Log.consoleLog(ifr, "Result===>" + Result.toString());

                String perfTransId = "";
                String perfFileId = "";
                if (!Result.isEmpty()) {
                    perfTransId = Result.get(0).get(0);
                    perfFileId = Result.get(0).get(1);
                }

                String bsaProcessURL = ConfProperty.getIntegrationValue(apiName);
                bsaProcessURL = bsaProcessURL.replace("#{transactionId}#", perfTransId);

                String request = "{\n"
                        + "  \"payload\": {\n"
                        + "    \"fileId\": \"" + perfFileId + "\",\n"
                        + "    \"institutionId\": \"11\",\n"
                        + "    \"password\": \"\"\n"
                        + "  }\n"
                        + "}";

                Log.consoleLog(ifr, "request===>" + request);
                String response = cm.getWebServiceIntegrationResponse(ifr, processInstanceId, request, bsaProcessURL);
                Log.consoleLog(ifr, "Response===>" + response);

                if (!response.equalsIgnoreCase("{}")) {

                    String queryUpdateTRN = "UPDATE LOS_TRN_PERF_INTEGRATION_DETAILS "
                            + "SET TRANSACTION_STATUS='PROCESSED'"
                            + "WHERE PERFIOS_TRANSACTIONID='" + perfTransId + "' "
                            + "AND PROCESSINSTANCEID='" + processInstanceId + "' "
                            + "AND  PERFIOS_SERVICETYPE='" + perfiosServiceType + "'";
                    Log.consoleLog(ifr, "queryUpdateTRN==>" + queryUpdateTRN);
                    ifr.saveDataInDB(queryUpdateTRN);

                    return RLOS_Constants.SUCCESS;
                }
            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String createJSONFile(IFormReference ifr, String response) {

        try {
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String filepath = ConfProperty.getIntegrationValue("BSATempPath");
            filepath = filepath + File.separator + processInstanceId;

            File directory = new File(filepath);
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            String opFilePath = filepath + File.separator + processInstanceId + ".json";
            Log.consoleLog(ifr, "File path==>" + opFilePath);
            File file = new File(opFilePath);
            if (!file.exists()) {
                FileWriter fWriter = new FileWriter(opFilePath);
                fWriter.write(response);
                fWriter.close();
            } else {
                return RLOS_Constants.SUCCESS;
            }
            return RLOS_Constants.SUCCESS;

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;

    }

}
