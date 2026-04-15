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
public class SSAOrchestrator {

    String perfiosServiceType = "SSA";
    String productType = "RLOS";
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods cm = new APICommonMethods();

    public String triggerSSAOrchestrator(IFormReference ifr) {
        Log.consoleLog(ifr, "SSAOrchestrator:triggerSSAOrchestrator-> triggering SSA Orchestrator");

        try {

            String perfSSAEnable = ConfProperty.getCommonPropertyValue("PERFIOS_SSAENABLE");
            perfSSAEnable = (perfSSAEnable != null) ? perfSSAEnable : "NO";
            Log.consoleLog(ifr, "perfBSAEnable?====>" + perfSSAEnable);

            if (perfSSAEnable.equalsIgnoreCase("YES")) {
                String criterion = "'GENERATED','COMPLETED'";
                String transactionStatus = cm.checkPerfiosCompletedTRNStatus(ifr, perfiosServiceType, criterion);

                if (Integer.parseInt(transactionStatus) == 0) {
                    String initatedStatus = initiateTransaction(ifr);
                    if (!initatedStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                        String uploadStatus = uploadDocuments(ifr);
                        if (!uploadStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                            String processStatus = processStatement(ifr);
                            if (!processStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                String reportGenStatus = reportGeneration(ifr);
                                if (!reportGenStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
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

        String apiName = "SSAInitiateTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String inCriteria = "'INITIATED','UPLOADED','PROCESSED','GENERATED'";
            String notInCriteria = "'COMPLETED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);

            if (Integer.parseInt(inflowStatus) == 0) {
                String callBackSSAURL = pcm.getConstantValue(ifr, "PERFIOS", "SSACALLBACKURL");

                String request = "{\n"
                        + "  \"payload\": {\n"
                        + "    \"processingType\": \"SALARY_SLIP\",\n"
                        + "    \"transactionCompleteCallbackUrl\": \"" + callBackSSAURL + "\",\n"
                        + "    \"txnId\": \"" + cm.getClientTransUniqueId(ifr) + "\",\n"
                        + "    \"uploadingScannedStatements\": \"true\"\n"
                        + "  }\n"
                        + "}";
                Log.consoleLog(ifr, "request===>" + request);
                String response = cm.getWebServiceResponse(ifr, apiName, request);
                Log.consoleLog(ifr, "response===>" + response);

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
                Log.consoleLog(ifr, "Transaction already in flow SSA.");
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in /initiateTransactionSSA::" + e);
        }

        return RLOS_Constants.ERROR;

    }

    private String uploadDocuments(IFormReference ifr) {

        String apiName = "SSAUploadTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String inCriteria = "'INITIATED'";
            String notInCriteria = "'UPLOADED','PROCESSED','GENERATED'";
            String inflowStatus = cm.checkCurrentPerfiosTRNStatus(ifr, processInstanceId, perfiosServiceType, inCriteria, notInCriteria);
            if (Integer.parseInt(inflowStatus) > 0) {

                ArrayList fileLocation = new ArrayList();
                String filepath = ConfProperty.getIntegrationValue("SSATempPath");
                String perfTransId = cm.getPerfiosTransId(ifr, processInstanceId, perfiosServiceType);
                String ssaUploadURL = ConfProperty.getIntegrationValue(apiName);
                ssaUploadURL = ssaUploadURL.replace("#{transactionId}#", perfTransId);

                String docIndexes = cm.getDocumentIndexFromWorkItem(ifr, "Salary");
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

                Log.consoleLog(ifr, "fileLocation" + fileLocation.size());
                Log.consoleLog(ifr, "fileLocation" + fileLocation.toString());

                // List<String> documentFilePaths = fileLocation;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (int i = 0; i < fileLocation.size(); i++) {
                    Log.consoleLog(ifr, "i==>" + i);

                    String uploadFilePath = fileLocation.get(i).toString();

                    builder.addFormDataPart("documentType", "pdf"); // Example: documentType1, documentType2, ...
                    builder.addFormDataPart("file", uploadFilePath,
                            RequestBody.create(MediaType.parse("application/octet-stream"), new File(uploadFilePath)));
                }

                Log.consoleLog(ifr, "#After addFormDataPart#");

                RequestBody body = builder.build();
                Request request = new Request.Builder()
                        .url(ssaUploadURL)
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
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    private String processStatement(IFormReference ifr) {
        String apiName = "SSAProcessTransaction";
        String serviceName = "Perfios_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {
            String inCriteria = "'UPLOADED'";
            String notInCriteria = "'INITIATED','PROCESSED','GENERATED'";
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

                String ssaProcessURL = ConfProperty.getIntegrationValue(apiName);
                ssaProcessURL = ssaProcessURL.replace("#{transactionId}#", perfTransId);

                String request = "{\n"
                        + "  \"payload\": {\n"
                        + "    \"fileId\": \"" + perfFileId + "\",\n"
                        + "    \"password\": \"\"\n"
                        + "  }\n"
                        + "}";

                Log.consoleLog(ifr, "request===>" + request);
                String response = cm.getWebServiceIntegrationResponse(ifr, processInstanceId, request, ssaProcessURL);
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

    private String reportGeneration(IFormReference ifr) {
        String apiName = "SSAReportGeneration";
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

                String ssaReportGenURL = ConfProperty.getIntegrationValue(apiName);
                ssaReportGenURL = ssaReportGenURL.replace("#{transactionId}#", perfTransId);
                String response = cm.getWebServiceIntegrationResponse(ifr, processInstanceId, "", ssaReportGenURL);
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
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;
    }

}
