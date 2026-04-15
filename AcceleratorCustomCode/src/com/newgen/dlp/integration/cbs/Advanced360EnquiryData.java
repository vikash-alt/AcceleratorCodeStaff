/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class Advanced360EnquiryData {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    

    public String executeAdvanced360Inquiry(IFormReference ifr, String ProcessInstanceId,
            String CustomerID, String ProductType) {

        Log.consoleLog(ifr, "===========#executeAdvanced360Inquiry========================");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "Advanced360API";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
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
            String ProductCode = "";
            String loanExists = "NA";
            String ClassificationFound = "NA";
            String ClassificationValue = "";
            int count = 0;
            BigDecimal total = new BigDecimal("0");

            String BankCode = pcm.getConstantValue(ifr, "CBSAD360", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSAD360", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CBSAD360", "USERID");
            String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, ProductType);

            String paplProductCode = pcm.getParamValue(ifr, "LOANEXISTCHECK", "PL-STP-PAPL");
            String cbProductCode = pcm.getParamValue(ifr, "LOANEXISTCHECK", "PL-STP-CB");
            String cbProductRltnshp = pcm.getParamValue(ifr, "LOANEXISTRELATION", "PL-STP-CB");

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"" + UserId + "\"\n"
                    + "            },\n"
                    + "            \"BankCode\": \"" + BankCode + "\",\n"
                    + "            \"Channel\": \"" + Channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"\",\n"
                    + "            \"ServiceCode\": \"\",\n"
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "            \"UserId\": \"" + UserId + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"customerId\": \"" + CustomerID + "\",\n"
                    + "        \"accountStatus\": \"NONCLOSED\",\n"
                    + "        \"accountModule\": \"ALL\"\n"
                    + "    }\n"
                    + "}";

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            String finalRes = "";
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(OutputJSON);

                String body = resultObj.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser.parse(body);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + CheckError);
                if (CheckError.equalsIgnoreCase("true")) {

                    String AdvanceCustomerSearchResponse = bodyObj.get("AdvanceCustomerSearchResponse").toString();
                    JSONObject AdvanceCustomerSearchResponseObj = (JSONObject) parser.parse(AdvanceCustomerSearchResponse);
                    //  JSONObject AdvanceCustomerSearchResponseObj = new JSONObject(AdvanceCustomerSearchResponseJSON);
                    Log.consoleLog(ifr, "AdvanceCustomerSearchResponseObj===>" + AdvanceCustomerSearchResponseObj.toJSONString());
                    String XfaceCustomerAccountDetailsDTO = AdvanceCustomerSearchResponseObj.get("XfaceCustomerAccountDetailsDTO").toString();

                    JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser.parse(XfaceCustomerAccountDetailsDTO);
                    // JSONObject XfaceCustomerAccountDetailsDTOObj = new JSONObject(XfaceCustomerAccountDetailsDTOJSON);

                    String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
                    //  System.out.println("AccountDetails==>" + AccountDetails);
                    Log.consoleLog(ifr, "AccountDetails===>" + AccountDetails);
                    JSONArray AccountDetailsJSON = (JSONArray) parser.parse(AccountDetails);
                    //  System.out.println("AccountDetailsJSON==>" + AccountDetailsJSON.size());
                    // Modified by Prakash on 08/02/2024, following orchestration Approch
                    /* org.json.JSONArray resArr = getCustomJsonArray(ifr, Response, "body,AdvanceCustomerSearchResponse,XfaceCustomerAccountDetailsDTO,AccountDetails");
                    Log.consoleLog(ifr, "resArr:" + resArr);
                    org.json.JSONArray jsonfilter = getCustomJsonFilter(ifr, "PSR");
                    Log.consoleLog(ifr, "jsonfilter:" + jsonfilter);
                    finalRes = filterJsonData(ifr, resArr, jsonfilter, "AccountId,BranchCode");*/
                    finalRes = getSavingBankAccNumber(ifr, response);
                    Log.consoleLog(ifr, "finalRes:" + finalRes);

                    PortalCommonMethods pcm = new PortalCommonMethods();
                    if (!AccountDetailsJSON.isEmpty()) {

                        int canaraBudjetCount = 0;
                        int paplCount = 0;

                        for (int i = 0; i < AccountDetailsJSON.size(); i++) {
                            // System.out.println("AccountDetailsJSON==>" + AccountDetailsJSON.get(i));
                            String InputString = AccountDetailsJSON.get(i).toString();

                            JSONObject InputStringResponseJSONJSONObj = (JSONObject) parser.parse(InputString);
                            //   JSONObject InputStringResponseJSONJSONObj = new JSONObject(InputStringResponseJSON);
                            Log.consoleLog(ifr, "InputStringResponseJSONJSONObj===>" + InputStringResponseJSONJSONObj.toString());
                            //  JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
                            String PrCode = InputStringResponseJSONJSONObj.get("ProductCode").toString();
                            String Classification = InputStringResponseJSONJSONObj.get("Classification").toString();
                            String totalLcyAmount = InputStringResponseJSONJSONObj.get("TotalLcyAmount").toString();
                            String DatAcctOpen = InputStringResponseJSONJSONObj.get("DatAcctOpen").toString();
                            String AvailableBalanace = InputStringResponseJSONJSONObj.get("AvailableBalanace").toString();
                            String customerRelationship = InputStringResponseJSONJSONObj.get("CustomerRelationship").toString();

                            ClassificationValue = Classification;

                            //Modified by Ahmed on 24-06-2024 for queryReading from PropertyFile
//                            try {
//                                String Ad_360_Update = "UPDATE LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                                        + " SET "
//                                        + " AD_360_DATACCTOPEN = '" + DatAcctOpen + "' "
//                                        + " WHERE "
//                                        + " CUSTOMERID = '" + CustomerID + "' ";
//
//                                Log.consoleLog(ifr, "Update Query in Adavance 360 " + Ad_360_Update);
//                                ifr.saveDataInDB(Ad_360_Update);
//                            } catch (Exception e) {
//                                Log.consoleLog(ifr, "Exception in AD360 API new column adding " + e);
//                            }
                            String queryUpdateOpenDt = ConfProperty.getQueryScript("PAPL_UPDATECBSOPENDATEQRY")
                                    .replace("#AD_360_DATACCTOPEN#", DatAcctOpen)
                                    .replace("#CUSTOMERID#", CustomerID);
                            Log.consoleLog(ifr, "Advanced360EnquiryData:executeAdvanced360Inquiry:queryUpdateOpenDt->" + queryUpdateOpenDt);
                            ifr.saveDataInDB(queryUpdateOpenDt);
                            
                            
                            

                            Log.consoleLog(ifr, "PrCode===>" + PrCode);
                            Log.consoleLog(ifr, "Classification===>" + Classification);
                            Log.consoleLog(ifr, "totalLcyAmount===>" + totalLcyAmount);
                            Log.consoleLog(ifr, "AvailableBalanace===>" + AvailableBalanace);
//                            if (PrCode.equalsIgnoreCase("60")) {
//                                PAPLExist = "Yes";
//                                total = total.add(pcm.mCheckBigDecimalValue(ifr, totalLcyAmount));
//                            }
//                            if (PrCode.equalsIgnoreCase("626")) {
//                                count++;
//                                ProductCode = PrCode;
//                                total = total.add(pcm.mCheckBigDecimalValue(ifr, totalLcyAmount));
//                            }

                            Log.consoleLog(ifr, "paplProductCode===>" + paplProductCode);
                            Log.consoleLog(ifr, "PrCode===>" + PrCode);
                            Log.consoleLog(ifr, "cbProductCode===>" + cbProductCode);

                            if (paplProductCode.equalsIgnoreCase(PrCode)) {
                                paplCount++;
                            }

                            //   if (cbProductCode.equalsIgnoreCase(PrCode)) {
                            if ((cbProductCode.equalsIgnoreCase(PrCode))
                                    && (checkCustomerRelationShipStatus(ifr, customerRelationship, cbProductRltnshp))) {
                                canaraBudjetCount++;
                                count++;
                                ProductCode = PrCode;
                                total = total.add(pcm.mCheckBigDecimalValue(ifr, AvailableBalanace));
                            }

                            if (Classification.equalsIgnoreCase("SUSPENDED")) {
                                ClassificationFound = Classification;
                            }

                            Log.consoleLog(ifr, "Brk1");
                        }

                        Log.consoleLog(ifr, "TotalExposures=====>" + total);
                        Log.consoleLog(ifr, "paplCount==========>" + paplCount);
                        Log.consoleLog(ifr, "canaraBudjetCount==>" + canaraBudjetCount);
                        if ((paplCount > 0) || (canaraBudjetCount > 1)) {
                            loanExists = "Yes";
                        }
                        Log.consoleLog(ifr, "loanExists========>" + loanExists);

                    }
                } else {
                    String[] ErrorData = CheckError.split("#");
                    apiErrorCode = ErrorData[0];
                    apiErrorMessage = ErrorData[1];
                }

            } else {
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
            }

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, response,
//                    ErrorCode, ErrorMessage, APIStatus);
//
//            if (!ErrorMessage.equalsIgnoreCase("")) {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            
            if (apiErrorMessage.equalsIgnoreCase("")) {
                apiStatus = RLOS_Constants.SUCCESS;
            } else {
                apiStatus = RLOS_Constants.ERROR;
            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            } else {
                JSONObject obj = new JSONObject();
                obj.put("ProductCode", ProductCode);
                obj.put("count", String.valueOf(count));
                obj.put("PAPLExist", loanExists);
                obj.put("Classification", ClassificationFound);
                obj.put("totalExposure", String.valueOf(total));
                obj.put("ClassifactionValue", ClassificationValue);
                obj.put("AccountDetails", finalRes);
                return obj.toJSONString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiry===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiry===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, apiName, apiErrorCode);
    }

    public String getSavingBankAccNumber(IFormReference ifr, String jsonRes) {

        Log.consoleLog(ifr, "#getSavingBankAccNumber starting..");

        org.json.JSONArray resArr = null, jsonfilter = null, finalRes = null;

        try {

            Log.consoleLog(ifr, "Advanced360EnquiryData:getSavingsBankAccNumber -> JSON Response:" + jsonRes);

            resArr = getCustomJsonArray(ifr, jsonRes, "body,AdvanceCustomerSearchResponse,XfaceCustomerAccountDetailsDTO,AccountDetails");

            jsonfilter = getCustomJsonFilter(ifr, "PSR");

            if (resArr != null) {
                finalRes = filterJsonData(ifr, resArr, jsonfilter, "AccountId,BranchCode,DatAcctOpen,AcyAmount,ProductCode");
            }
        } catch (Exception e) {

            Log.consoleLog(ifr, "[Exception]Advanced360EnquiryData:getSavingBankAccNumber===>" + e);

            Log.errorLog(ifr, "[Exception]getSavingBankAccNumber:getSavingBankAccNumber===>" + e);

        }

        if (finalRes != null) {
            return finalRes.toString();
        } else {
            return "";
        }

    }

    public static org.json.JSONArray getCustomJsonArray(IFormReference ifr, String res, String jsonKey) {
        String[] jsonKeys = null;
        org.json.JSONArray resArr = null;
        try {
            org.json.JSONObject resObj = new org.json.JSONObject(res);
            jsonKeys = jsonKey.trim().split(",");
            Log.consoleLog(ifr, "jsonKeys:" + jsonKeys);
            for (int i = 0; i < jsonKeys.length; i++) {
                if (resObj != null && resObj.has(jsonKeys[i])) {
                    if (i == jsonKeys.length - 1) {
                        resArr = (org.json.JSONArray) resObj.get(jsonKeys[i]);
                    } else {
                        resObj = resObj.getJSONObject(jsonKeys[i]);
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return resArr;
    }

// modified by Kathir & Keerthana for pension LC changes on 06/08/2024
    public static org.json.JSONArray filterJsonData(IFormReference ifr, org.json.JSONArray resArr, org.json.JSONArray jsonFilter, String outputKeys) {
        org.json.JSONArray outJsonArr = new org.json.JSONArray();
        try {
            for (int i = 0; i < resArr.length(); i++) {
                org.json.JSONObject resObj = resArr.getJSONObject(i);
                boolean matchFlag = true;
                for (int j = 0; j < jsonFilter.length(); j++) {
                    org.json.JSONObject jsonObj = jsonFilter.getJSONObject(j);
                    String key = (String) jsonObj.keys().next();
                    String val = (String) jsonObj.getString(key);
                    String[] valArr = val.toUpperCase().trim().split(",");
                    String resVal = resObj.getString(key).toUpperCase();
                    if (!Arrays.asList(valArr).contains(resVal)) {
                        matchFlag = false;
                    }
                }
                Log.consoleLog(ifr, "jsonFilter log3 " + matchFlag);
                if (matchFlag == true) {
                    String[] outArr = outputKeys.trim().split(",");
                    org.json.JSONObject outJsonObj = new org.json.JSONObject();
                    for (String outArr1 : outArr) {
                        outJsonObj.put(outArr1, resObj.get(outArr1));
                    }
                    outJsonArr.put(outJsonObj);
                }
            }

        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception:" + ex);
            Log.errorLog(ifr, "Exception:" + ex);
        }
        return outJsonArr;
    }

    public org.json.JSONArray getCustomJsonFilter(IFormReference ifr, String filterType) {
        org.json.JSONArray jsonArr = null;
        try {
            String strCustomerRelationship = "";
            String strCurrentStatusp = "";
            String strProductCode = "";
            switch (filterType) {
                case "PSR":
                    //PSR : ProductCode,  Status and Relationship
                    strCustomerRelationship = pcm.getParamValue(ifr, "CASACHECK", "SBACCTRELATION");
                    strCurrentStatusp = pcm.getParamValue(ifr, "CASACHECK", "SBACCTSTATUS");
                    strProductCode = pcm.getParamValue(ifr, "CASACHECK", "SBACCTPRODCODE");

                    jsonArr = new org.json.JSONArray("[{\"CustomerRelationship\":\"" + strCustomerRelationship + "\"},"
                            + "{\"CurrentStatus\":\"" + strCurrentStatusp + "\"},"
                            + "{\"ProductCode\":\"" + strProductCode + "\"}]");
                    break;
                case "PC":
                    strProductCode = pcm.getParamValue(ifr, "CASACHECK", "SBACCTSTATUS");
                    //ProductCode
                    jsonArr = new org.json.JSONArray("[{\"ProductCode\":\"" + strProductCode + "\"}]");
                    break;
                case "RS":
                    //RS: Relationship
                    strCustomerRelationship = pcm.getParamValue(ifr, "CASACHECK", "SBACCTPRODCODE");
                    jsonArr = new org.json.JSONArray("[{\"CustomerRelationship\":\"" + strCustomerRelationship + "\"}]");
                    break;
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception:" + ex);
            Log.errorLog(ifr, "Exception:" + ex);
        }
        return jsonArr;
    }

    public boolean checkCustomerRelationShipStatus(IFormReference ifr, String inputCustRel, String custRelDataFromDB) {

        int count = 0;
        if (!custRelDataFromDB.contains(",")) {
            custRelDataFromDB = custRelDataFromDB + ",";
        }

        Log.consoleLog(ifr, "cbProductRltnshp==>" + custRelDataFromDB);
        String[] cbProductRltnshpArr = custRelDataFromDB.split(",");
        for (String custRel : cbProductRltnshpArr) {
            Log.consoleLog(ifr, "custRel==>" + custRel);
            if (custRel.equalsIgnoreCase(inputCustRel)) {
                count++;
            }
        }

        Log.consoleLog(ifr, "count==>" + count);
        if (count > 0) {
            Log.consoleLog(ifr, "count/true==>" + count);
            return true;
        }
        Log.consoleLog(ifr, "count/false==>" + count);
        return false;
    }

	  public String filterDisbursementAccount(IFormReference ifr, String ProcessInstanceId,
            String CustomerID, String ProductType) {
        Log.consoleLog(ifr, "Entered into Execute_Advanced360Inquiry...::: filterDisbursementAccount");
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
        String formattedDate = dateFormat.format(currentDate);
        //String ProductCode = "";
        //String PAPLExist = "NA";
        // String ClassificationFound = "NA";
        // String ClassificationValue = "";
        int count = 0;
        ArrayList eligibleAccountNumber = new ArrayList();
        BigDecimal total = new BigDecimal("0");
        String finalAccountRes = "";
        try {

            String bankCode = pcm.getConstantValue(ifr, "CBSAD360V2", "BANKCODE");
            String channel = pcm.getConstantValue(ifr, "CBSAD360V2", "CHANNEL");
            String userId = pcm.getConstantValue(ifr, "CBSAD360V2", "USERID");
            String tBranch = pcm.getConstantValue(ifr, "CBSAD360V2", "BCODE");
            String accountModule=pcm.getConstantValue(ifr, "CBDLP", "ACCTMODULE");
            Log.consoleLog(ifr, "tBranch:::::" + tBranch);
           // String apiName = "Advanced360API";
            String apiName = "Advanced360Inquiryv2";
            String serviceName = "CBS_" + apiName;
            Log.consoleLog(ifr, "apiName==>" + apiName + " || " + " in filterDisbursementAccount serviceName==>" + serviceName);
            String randomnumber=pcm.generateRandomNumber(ifr);
            
            String AdvancedApiStatus = "SELECT COUNT(1) FROM LOS_INTEGRATION_REQRES WHERE API_NAME IN ('" + serviceName + "') AND TRANSACTION_ID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' and API_STATUS!='FAIL'";
      		
      		List<List<String>> AdvancedCount = cf.mExecuteQuery(ifr, AdvancedApiStatus, "Checking Count Of AdvancedCount");
      		Log.consoleLog(ifr, "Integer.parseInt(AdvancedCount.get(0).get(0))====>" + Integer.parseInt(AdvancedCount.get(0).get(0)));
            
            String request = "{\n"
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
                    + "            \"ExternalReferenceNo\": \"" + formattedDate+randomnumber + "\",\n"
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
                    + "        \"accountModule\": \""+accountModule+"\"\n"
                    + "}";
            HashMap<String, String> requestHeader = new HashMap<>();
            
            Log.consoleLog(ifr, "Request====>" + request);
            
            String response="";
			
            if (Integer.parseInt(AdvancedCount.get(0).get(0))>0) {
			    String advancedquery="SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME IN "+ "('" + serviceName + "') AND TRANSACTION_ID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' and API_STATUS!='FAIL' order by ENTRYDATETIME desc FETCH FIRST 1 ROWS ONLY";
				List<List<String>> advancedqueryres = cf.mExecuteQuery(ifr, advancedquery, "Execute query for fetching saved data from GLOS_INTEGRATION_REQRES");
			    response = advancedqueryres.get(0).get(0);
			    }
			else
			{
			   response = cm.getWebServiceResponse(ifr, apiName, request);
			}
            
           Log.consoleLog(ifr, "Response===>" + response);

            String ErrorCode = "";
            String ErrorMessage = "";
            org.json.JSONArray finalRes = new org.json.JSONArray();
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(OutputJSON);

                String body = resultObj.get("body").toString();
                JSONObject bodyJSON = (JSONObject) parser.parse(body);
                JSONObject bodyObj = new JSONObject(bodyJSON);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + CheckError);
                if (CheckError.equalsIgnoreCase("true")) {

                    //String AdvanceCustomerSearchResponse = bodyObj.get("AdvanceCustomerSearchResponse").toString();
                    //JSONObject AdvanceCustomerSearchResponseJSON = (JSONObject) parser.parse(AdvanceCustomerSearchResponse);
                    //JSONObject AdvanceCustomerSearchResponseObj = new JSONObject(AdvanceCustomerSearchResponseJSON);
                    // Log.consoleLog(ifr, "AdvanceCustomerSearchResponseObj===>" + AdvanceCustomerSearchResponseObj.toJSONString());
                    String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
                    JSONObject XfaceCustomerAccountDetailsDTOJSON = (JSONObject) parser.parse(XfaceCustomerAccountDetailsDTO);
                    JSONObject XfaceCustomerAccountDetailsDTOObj = new JSONObject(XfaceCustomerAccountDetailsDTOJSON);

                    String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
                    //  System.out.println("AccountDetails==>" + AccountDetails);
                    Log.consoleLog(ifr, "AccountDetails===>" + AccountDetails);
                    JSONArray AccountDetailsJSON = (JSONArray) parser.parse(AccountDetails);

                    if (!AccountDetailsJSON.isEmpty()) {
                        for (int i = 0; i < AccountDetailsJSON.size(); i++) {
                            // System.out.println("AccountDetailsJSON==>" + AccountDetailsJSON.get(i));
                            String InputString = AccountDetailsJSON.get(i).toString();

                            JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);
                            JSONObject InputStringResponseJSONJSONObj = new JSONObject(InputStringResponseJSON);
                            Log.consoleLog(ifr, "InputStringResponseJSONJSONObj===>" + InputStringResponseJSONJSONObj.toString());
                            //  JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
                            String productCode = InputStringResponseJSONJSONObj.get("ProductCode").toString();
                            String currentStatus = InputStringResponseJSONJSONObj.get("CurrentStatus").toString();
                            String customerRelationship = InputStringResponseJSONJSONObj.get("CustomerRelationship").toString();
                            //String DatAcctOpen = InputStringResponseJSONJSONObj.get("DatAcctOpen").toString();

                            Log.consoleLog(ifr, "productCode===>" + productCode);
                            Log.consoleLog(ifr, "currentStatus===>" + currentStatus);
                            Log.consoleLog(ifr, "customerRelationship===>" + customerRelationship);

                            // ArrayList eligibleAccountNumber = new ArrayList();
//                            if ((customerRelationship.equalsIgnoreCase("SOW") || customerRelationship.equalsIgnoreCase("JOO") || customerRelationship.equalsIgnoreCase("JOF"))
//                                    && (currentStatus.equalsIgnoreCase("6") || currentStatus.equalsIgnoreCase("8")) && (productCode.equalsIgnoreCase("101")
//                                    || productCode.equalsIgnoreCase("108") || productCode.equalsIgnoreCase("110") || productCode.equalsIgnoreCase("149") || productCode.equalsIgnoreCase("150")
//                                    || productCode.equalsIgnoreCase("148") || productCode.equalsIgnoreCase("145") || productCode.equalsIgnoreCase("144") || productCode.equalsIgnoreCase("146")
//                                    || productCode.equalsIgnoreCase("147") || productCode.equalsIgnoreCase("132") || productCode.equalsIgnoreCase("136"))) {
//
//                                eligibleAccountNumber.add(String.valueOf(InputStringResponseJSONJSONObj.get("AccountId")));
//                            }
                            //return eligibleAccountNumber.toString();
                            //  Log.consoleLog(ifr, "Brk1");
                            // Added by Subham on 27-05-2024 for getting account number
                        }
                        finalAccountRes = getSavingBankAccNumber(ifr, response,ProductType);
                        Log.consoleLog(ifr, "finalRes:" + finalAccountRes);

                    }
                    //return eligibleAccountNumber.toString();
                    return finalAccountRes;
                } else {
                    String[] ErrorData = CheckError.split("#");
                    ErrorCode = ErrorData[0];
                    ErrorMessage = ErrorData[1];
                }

            } else {
                response = "No response from the server.";
                ErrorMessage = "FAIL";
            }

            String APIName = "CBS_Advanced360Inquiry";
            String APIStatus = "";
            if (ErrorMessage.equalsIgnoreCase("")) {
                APIStatus = "SUCCESS";
            } else {
                APIStatus = "FAIL";
            }
            if(Integer.parseInt(AdvancedCount.get(0).get(0))==0)
            {	
            cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, request, response,
                    ErrorCode, ErrorMessage, APIStatus);
            }
            if (!(ErrorMessage.equalsIgnoreCase(""))) {
                return RLOS_Constants.ERROR;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiry===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiry===>" + e);
        }
        return RLOS_Constants.ERROR;
    }

	  public String getSavingBankAccNumber(IFormReference ifr, String jsonRes,String ProductType) {

	        Log.consoleLog(ifr, "#getSavingBankAccNumber starting..");

	        org.json.JSONArray resArr = null, jsonfilter = null, finalRes = null;

	        try {

	            Log.consoleLog(ifr, "Advanced360EnquiryData:getSavingsBankAccNumber -> JSON Response:" + jsonRes);

	            resArr = getCustomJsonArray(ifr, jsonRes, "body,AdvanceCustomerSearchResponse,XfaceCustomerAccountDetailsDTO,AccountDetails");

	            jsonfilter = getCustomJsonFilter(ifr, "PSR",ProductType);

	            if (resArr != null) {
	                finalRes = filterJsonData(ifr, resArr, jsonfilter, "AccountId,BranchCode,DatAcctOpen,AcyAmount,ProductCode");
	            }
	        } catch (Exception e) {

	            Log.consoleLog(ifr, "[Exception]Advanced360EnquiryData:getSavingBankAccNumber===>" + e);

	            Log.errorLog(ifr, "[Exception]getSavingBankAccNumber:getSavingBankAccNumber===>" + e);

	        }

	        if (finalRes != null) {
	            return finalRes.toString();
	        } else {
	            return "";
	        }

	    }

	  public org.json.JSONArray getCustomJsonFilter(IFormReference ifr, String filterType,String ProductType) {
	        org.json.JSONArray jsonArr = null;
	        try {
	        	Log.consoleLog(ifr, "getCustomJsonFilter start GOLD" +ProductType);
	            String strCustomerRelationship = "";
	            String strCurrentStatusp = "";
	            String strProductCode = "";
	            switch (filterType) {
	                case "PSR":
	                	if(ProductType.equalsIgnoreCase("KCC") || ProductType.equalsIgnoreCase("ALDRI")) {
	                    //PSR : ProductCode,  Status and Relationship
	                    strCustomerRelationship = pcm.getParamValue(ifr, "AGRICASACHECK", "SBACCTRELATION");
	                    strCurrentStatusp = pcm.getParamValue(ifr, "AGRICASACHECK", "SBACCTSTATUS");
	                    strProductCode = pcm.getParamValue(ifr, "AGRICASACHECK", "SBACCTPRODCODE");
	                	}
	                	else if(ProductType.equalsIgnoreCase("GOLD"))
	                	{
	                		Log.consoleLog(ifr, "getCustomJsonFilter inside GOLD" +ProductType);
	                		 strCustomerRelationship = pcm.getParamValue(ifr, "GOLDCASACHECK", "SBACCTRELATION");
	                         strCurrentStatusp = pcm.getParamValue(ifr, "GOLDCASACHECK", "SBACCTSTATUS");
	                         strProductCode = pcm.getParamValue(ifr, "GOLDCASACHECK", "SBACCTPRODCODE");
	                	}
	                    jsonArr = new org.json.JSONArray("[{\"CustomerRelationship\":\"" + strCustomerRelationship + "\"},"
	                            + "{\"CurrentStatus\":\"" + strCurrentStatusp + "\"},"
	                            + "{\"ProductCode\":\"" + strProductCode + "\"}]");
	                    break;
	                case "PC":
	                    strProductCode = pcm.getParamValue(ifr, "AGRICASACHECK", "SBACCTSTATUS");
	                    //ProductCode
	                    jsonArr = new org.json.JSONArray("[{\"ProductCode\":\"" + strProductCode + "\"}]");
	                    break;
	                case "RS":
	                    //RS: Relationship
	                    strCustomerRelationship = pcm.getParamValue(ifr, "AGRICASACHECK", "SBACCTPRODCODE");
	                    jsonArr = new org.json.JSONArray("[{\"CustomerRelationship\":\"" + strCustomerRelationship + "\"}]");
	                    break;
	            }
	        } catch (Exception ex) {
	            Log.consoleLog(ifr, "Exception:" + ex);
	            Log.errorLog(ifr, "Exception:" + ex);
	        }
	        return jsonArr;
	    }


}
