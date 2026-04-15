/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.perfios;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.io.File;
import java.util.ArrayList;
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
public class ITROrchestrator {

    String perfiosServiceType = "ITR";
    String productType = "RLOS";
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods cm = new APICommonMethods();

    public String triggerITROrchestrator(IFormReference ifr) {
        Log.consoleLog(ifr, "ITROrchestrator:triggerBSAOrchestrator-> triggering ITR Orchestrator");

        try {

            String perfITREnable = ConfProperty.getCommonPropertyValue("PERFIOS_ITRENABLE");
            perfITREnable = (perfITREnable != null) ? perfITREnable : "NO";
            Log.consoleLog(ifr, "perfBSAEnable?====>" + perfITREnable);

            if (perfITREnable.equalsIgnoreCase("YES")) {

                String criterion = "'PROCESSED','COMPLETED'";
                String transactionStatus = cm.checkPerfiosCompletedTRNStatus(ifr, perfiosServiceType, criterion);

                if (Integer.parseInt(transactionStatus) == 0) {
                    String initatedStatus = initiateTransaction(ifr);
                    if (!initatedStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                        String uploadStatus = uploadDocuments(ifr);
                        if (!uploadStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                            String processStatus = processTransaction(ifr);
                            if (!processStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                String completeStatus = completeTransaction(ifr);
                                if (!completeStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
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

    public String initiateTransaction(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside initiateTransaction");

        String apiName = "ITRInitiateTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String panNumber = pcm.getPANNumber(ifr);//cbs
            String inCriteria = "'INITIATED','UPLOADED','PROCESSED'";
            String notInCriteria = "'COMPLETED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);

            if (Integer.parseInt(inflowStatus) == 0) {
                String callITRBackURL = pcm.getConstantValue(ifr, "PERFIOS", "ITRCALLBACKURL");

                String request = "{\n"
                        + "    \"additionalParams\": null,\n"
                        + "    \"clientTransactionId\": \"" + cm.getClientTransUniqueId(ifr) + "\",\n"
                        + "    \"optionalConfigParams\": {\n"
                        + "        \"acceptancePolicyEnabled\": null,\n"
                        + "        \"form16YearsList\": [\n"
                        + "            null\n"
                        + "        ],\n"
                        + "        \"form26asYearsList\": [\n"
                        + "            null\n"
                        + "        ],\n"
                        + "        \"itrYearsList\": [\n"
                        + "            null\n"
                        + "        ],\n"
                        + "        \"itrvYearsList\": [\n"
                        + "            null\n"
                        + "        ],\n"
                        + "        \"latestYearFilesRequired\": null,\n"
                        + "        \"mandatoryDocumentTypes\": [\n"
                        + "            null\n"
                        + "        ],\n"
                        + "        \"minAllowedYearsForForm16\": null,\n"
                        + "        \"minAllowedYearsForForm26as\": null,\n"
                        + "        \"minAllowedYearsForItr\": null,\n"
                        + "        \"minAllowedYearsForItrv\": null\n"
                        + "    },\n"
                        + "    \"pan\": \"" + panNumber + "\",\n"
                        + "    \"redirectionUrl\": \"https://google.com/\",\n"
                        + "    \"scanned\": \"false\",\n"
                        + "    \"transactionCompleteUrl\": \"" + callITRBackURL + "\",\n"
                        + "    \"type\": \"IncomeTaxStatementUpload\"\n"
                        + "}\n"
                        + " ";
                Log.consoleLog(ifr, "request===>" + request);
                String response = cm.getWebServiceResponse(ifr, apiName, request);
                Log.consoleLog(ifr, "response===>" + response);

                if (!response.equalsIgnoreCase("{}")) {

                    JSONParser parser = new JSONParser();
                    JSONObject resultObj = (JSONObject) parser.parse(response);
                    String body = resultObj.get("body").toString();
                    JSONObject bodyObj = (JSONObject) parser.parse(body);
                    String perfiosTransactionId = bodyObj.get("transactionId").toString();
                    Log.consoleLog(ifr, "perfiosTransactionId==>" + perfiosTransactionId);

                    String queryInsertPerfId = "INSERT INTO LOS_TRN_PERF_INTEGRATION_DETAILS (PRODUCT,PROCESSINSTANCEID,PERFIOS_TRANSACTIONID,CLIENT_TRANSACTIONID,ENTRYDATETIME,TRANSACTION_STATUS,PERFIOS_SERVICETYPE) \n"
                            + "VALUES ('" + productType + "','" + processInstanceId + "','" + perfiosTransactionId + "','" + cm.getClientTransUniqueId(ifr) + "',SYSDATE,'INITIATED','" + perfiosServiceType + "')";
                    Log.consoleLog(ifr, "queryInsertPerfId==>" + queryInsertPerfId);
                    ifr.saveDataInDB(queryInsertPerfId);

                    return RLOS_Constants.SUCCESS;

                }

            } else {
                Log.consoleLog(ifr, "Transaction already in flow ITR.");
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in /initiateTransaction" + e);
        }

        return RLOS_Constants.ERROR;

    }

    private String uploadDocuments(IFormReference ifr) {

        String apiName = "ITRUploadTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String inCriteria = "'INITIATED'";
            String notInCriteria = "'UPLOADED','PROCESSED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);
            if (Integer.parseInt(inflowStatus) > 0) {

                ArrayList fileLocation = new ArrayList();
                String filepath = ConfProperty.getIntegrationValue("ITRTempPath");
                String perfTransId = cm.getPerfiosTransId(ifr, processInstanceId, perfiosServiceType);
                String itrUploadURL = ConfProperty.getIntegrationValue(apiName);
                itrUploadURL = itrUploadURL.replace("#{transactionId}#", perfTransId);
                Log.consoleLog(ifr, "itrUploadURL==>" + itrUploadURL);

                String docIndexes = cm.getDocumentIndexFromWorkItem(ifr, "ITR");
                Log.consoleLog(ifr, "docIndexes==>" + docIndexes);
                if (docIndexes.equalsIgnoreCase("")) {
                    return RLOS_Constants.ERROR;
                } else {

                    if (!docIndexes.contains(",")) {
                        docIndexes = docIndexes + ",";//Splittingbased on Comma
                    }

                    String[] docIndxSplitted = docIndexes.split(",");
                    for (String documentIndex : docIndxSplitted) {
                        Log.consoleLog(ifr, "Downloading DocIndex: " + documentIndex);

                        String filePath = cm.downloadDocFromWorkItem(ifr, processInstanceId, documentIndex, filepath);
                        if (filePath.contains(RLOS_Constants.ERROR)) {
                            return RLOS_Constants.ERROR;
                        }
                        fileLocation.add(filePath);
                    }

                }

                // List<String> documentFilePaths = fileLocation;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (int i = 0; i < fileLocation.size(); i++) {
                    String uploadFilePath = fileLocation.get(i).toString();

                    builder.addFormDataPart("documentType", "pdf");
                    builder.addFormDataPart("file", uploadFilePath,
                            RequestBody.create(MediaType.parse("application/octet-stream"), new File(uploadFilePath)));
                }

                RequestBody body = builder.build();

                Request request = new Request.Builder()
                        .url(itrUploadURL)
                        .method("POST", body)
                        .addHeader("accept", "application/json")
                        .build();

                Response response = client.newCall(request).execute();

                String outputResponse = response.body().string();
                Log.consoleLog(ifr, "outputResponse==>" + outputResponse);

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
            Log.consoleLog(ifr, "Exception/uploadDocuments==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String processTransaction(IFormReference ifr) {
        String apiName = "ITRProcessTransaction";
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

                String itrProcessURL = ConfProperty.getIntegrationValue(apiName);
                itrProcessURL = itrProcessURL.replace("#{transactionId}#", perfTransId);
                itrProcessURL = itrProcessURL.replace("#{fileId}#", perfFileId);

                Log.consoleLog(ifr, "request===>" + "");
                String response = cm.getWebServiceIntegrationResponse(ifr, processInstanceId, "", itrProcessURL);
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
            Log.consoleLog(ifr, "Exception/processTransaction==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String completeTransaction(IFormReference ifr) {
        String apiName = "ITRCompleteTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {
            String inCriteria = "'PROCESSED'";
            String notInCriteria = "'INITIATED','UPLOADED','GENERATED'";
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

                String itrCompleteURL = ConfProperty.getIntegrationValue(apiName);
                itrCompleteURL = itrCompleteURL.replace("#{transactionId}#", perfTransId);
                String response = cm.getWebServiceIntegrationResponse(ifr, processInstanceId, "", itrCompleteURL);
                Log.consoleLog(ifr, "Response===>" + response);

                if (!response.equalsIgnoreCase("")) {

                    String queryUpdateTRN = "UPDATE LOS_TRN_PERF_INTEGRATION_DETAILS "
                            + "SET TRANSACTION_STATUS='GENERATED'"
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
            Log.consoleLog(ifr, "Exception/completeTransaction==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

}
