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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class Advanced360EnquiryDatav2 {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String executeCBSAdvanced360Inquiryv2(IFormReference ifr, String ProcessInstanceId,
            String CustomerID, String ProductType, String mobileNumber, String CallFrom) {
        Log.consoleLog(ifr, "Entered into ExecuteCBSAdvanced360Inquiryv2...");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "Advanced360Inquiryv2";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        String loanAccOpenDte = "";

        try {
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

            String bankCode = pcm.getConstantValue(ifr, "CBSAD360V2", "BANKCODE");
            String channel = pcm.getConstantValue(ifr, "CBSAD360V2", "CHANNEL");
            String userId = pcm.getConstantValue(ifr, "CBSAD360V2", "USERID");
            String tBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

            String loanSelectedProductCode = pcm.getProductCode(ifr);
            String loanSelectedSubProductCode = pcm.getProductCode(ifr);
            String paramKey = loanSelectedProductCode + "-" + loanSelectedSubProductCode;
            Log.consoleLog(ifr, "paramKey==========>" + paramKey);

            String paplProductCode = pcm.getParamValue(ifr, "LOANEXISTCHECK", "PL-STP-PAPL");
            String lapExistsProductCode = pcm.getParamValue(ifr, "LOANEXISTCHECK", paramKey);
            String salExistsProductCode = pcm.getParamValue(ifr, "CASACHECKCB", paramKey);
            Log.consoleLog(ifr, "lapExistsCode==========>" + lapExistsProductCode);
            Log.consoleLog(ifr, "salExistsProductCode===>" + salExistsProductCode);//Added by Ahmed on 28-06-2024 for checking

            request = "{\n"
                    + "            \"Operation\": \"advancedCustomer360viewEnquiry(SessionContext,long,String,String)\",\n"
                    + "            \"Service\": \"LOSManagerService\",\n"
                    + "            \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "            \"PrimaryPassword\": \"\",\n"
                    + "            \"UserId\": \"" + userId + "\"\n"
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
                    + "            \"UserReferenceNumber\": \"\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"customerId\": \"" + CustomerID + "\",\n"
                    + "        \"accountStatus\": \"NONCLOSED\",\n"
                    + "        \"accountModule\": \"ALL\"\n"
                    + "}";

            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            int canaraBudgetProdCount = 0;
            int paplCount = 0;
            int classificationCount = 0;
            int salExistsCount = 0;
            int smeExistsCount = 0;
            String loanAcctOpenDatae = "";
            String classificationFound = "NA";
            String paplExists = "No";
            String canaraBudgetExists = "No";
            String salExists = "No";
            String smaExists = "0";

            if (!response.equalsIgnoreCase("{}")) {

                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(response);
                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body==>" + body);

                JSONObject bodyObj = (JSONObject) parser.parse(body);
                String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + checkError);

                if (checkError.equalsIgnoreCase("true")) {

                    String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
                    Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
                    JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser.parse(XfaceCustomerAccountDetailsDTO);
                    String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
                    Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
                    JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

                    if (CallFrom.equalsIgnoreCase("FetchNTHRuleInput")) {
                        return AccountDetailsObj.toString();
                    }

                    if (!AccountDetailsObj.isEmpty()) {
                        for (int i = 0; i < AccountDetailsObj.size(); i++) {

                            Log.consoleLog(ifr, "AccountDetailsObj==>" + AccountDetailsObj.get(i));
                            String inputJSON = AccountDetailsObj.get(i).toString();
                            JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
                            String moduleCode = inputJSONObj.get("ModuleCode").toString();
                            Log.consoleLog(ifr, "ModuleCode==============>" + moduleCode);
                            String productName = inputJSONObj.get("ProductName").toString();
                            Log.consoleLog(ifr, "ProductName=============>" + productName);
                            String classification = inputJSONObj.get("Classification").toString();
                            Log.consoleLog(ifr, "classification==========>" + classification);
                            String productCode = inputJSONObj.get("ProductCode").toString();
                            Log.consoleLog(ifr, "ProductCode=============>" + productCode);
                            String Sma2Count12Months = inputJSONObj.get("Sma2Count12Months").toString();
                            Log.consoleLog(ifr, "Sma2Count12Months=============>" + Sma2Count12Months);
                            loanAcctOpenDatae = inputJSONObj.get("DatAcctOpen").toString();
                            Log.consoleLog(ifr, "DatAcctOpen==========>" + loanAcctOpenDatae);

                            if (lapExistsProductCode.equalsIgnoreCase(productCode)) {
                                canaraBudgetProdCount++;
                            }

                            if (paplProductCode.equalsIgnoreCase(productCode)) {
                                paplCount++;
                            }

                            if (classification.equalsIgnoreCase("SUSPENDED") || classification.equalsIgnoreCase("DOUBTFUL")
                                    || classification.equalsIgnoreCase("LOSS")) {
                                classificationCount++;
                            }

                            if (!Sma2Count12Months.equalsIgnoreCase("0")) {
                                smeExistsCount++;
                            }

                            boolean salExistFlag = checkSalaryAccoutExists(ifr, productCode, salExistsProductCode);
                            if (salExistFlag) {
                                salExistsCount++;
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

            if (canaraBudgetProdCount > 0) {
                canaraBudgetExists = "Yes";
            }

            if (paplCount > 0) {
                paplExists = "Yes";
            }

            if (salExistsCount > 0) {
                salExists = "Yes";
            }

            if (smeExistsCount > 0) {
                smaExists = "1";
            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            } else {
                JSONObject obj = new JSONObject();
                obj.put("apiStatus", apiStatus);
                obj.put("canaraBudgetExists", canaraBudgetExists);
                obj.put("ProductCode", loanSelectedProductCode);
                obj.put("count", String.valueOf(canaraBudgetProdCount));
                obj.put("PAPLExist", paplExists);
                obj.put("Classification", classificationFound);
                obj.put("salExists", salExists);
                obj.put("smaExists", smaExists);
                obj.put("loanAcctOpenDatae", loanAcctOpenDatae);
                Log.consoleLog(ifr, "obj==>" + obj.toString());
                return obj.toJSONString();
            }
//
//            JSONObject obj = new JSONObject();
//            // if (canaraBudgetProdCount > 0) {
//            obj.put("apiStatus", apiStatus);
//            obj.put("canaraBudgetExists", canaraBudgetExists);
//            obj.put("ProductCode", loanSelectedProductCode);
//            obj.put("count", String.valueOf(canaraBudgetProdCount));
//            obj.put("PAPLExist", paplExists);
//            obj.put("Classification", classificationFound);
//            obj.put("salExists", salExists);
//            obj.put("smaExists", smaExists);
//            //   }

//            Log.consoleLog(ifr, "captureRequestResponse Calling...");
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
//                    errorCode, errorMessage, apiStatus);
//            Log.consoleLog(ifr, "captureRequestResponse Ended....");
//            Log.consoleLog(ifr, "obj==>" + obj.toString());
//            return obj.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

    private boolean checkSalaryAccoutExists(IFormReference ifr, String productCode, String salExistsProductCode) {
        Log.consoleLog(ifr, "#checkSalaryAccoutExists...");
        Log.consoleLog(ifr, "productCode:" + productCode);
        Log.consoleLog(ifr, "salExistsProductCode:" + salExistsProductCode);

        String[] salExistsProductCodes = salExistsProductCode.split(",");

        for (String accnt : salExistsProductCodes) {
            if (accnt.equals(productCode)) {
                Log.consoleLog(ifr, "#Salary Account found.");
                return true;
            }
        }

        return false;

    }
}
