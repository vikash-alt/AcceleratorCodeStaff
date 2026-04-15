package com.newgen.iforms.staffHL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;

public class CRGGenerator {

    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonMethods objcm = new CommonMethods();
    CommonFunctionality cf = new CommonFunctionality();

    public void crgGenExperian(IFormReference ifr, JSONObject resultObj, String processInstanceId, String apiName,
            String applicantType, String type, String insertionOrderid) {

        Log.consoleLog(ifr, "insertionOrderid Query:==>#### insertionOrderid" + insertionOrderid);
//    	String deleteQuery = "DELETE FROM LOS_TRN_CREDITHISTORY_DETAILS WHERE PID = '" + processInstanceId
//                + "' AND API_NAME = '" + apiName
//                + "' AND APPLICANTTYPE = '" + applicantType + "'"
//                + "' AND APPLICANT_UID = '" + insertionOrderid + "'";

        String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
                replaceAll("#processInstanceId#", processInstanceId).replaceAll("#apiName#", apiName).
                replaceAll("#applicantType#", applicantType).replaceAll("insertionOrderid", insertionOrderid);
        Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
        ifr.saveDataInDB(trndelete);

        String settledStatus = pcm.getConstantValue(ifr, "EXPERIAN", "SETTLED_STATUS");
        String wriitenofffStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");
        String restructureStatus = pcm.getConstantValue(ifr, "EXPERIAN", "RESTRUCTURED");
        String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");// Y
        String assetClassification_No = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_NO");
        String assetClassification_Yes = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_YES");
        String dpddays = pcm.getConstantValue(ifr, "EXPERIAN", "DPD_DAYS");
        String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
        String emiAmount = "0";
        String currentBalance = "0";
        String dpdExfinal = "";
        int noOfAccountBlock = 0;
        List<LinkedHashMap<String, String>> finalMap = new ArrayList<>();

        if (!type.equalsIgnoreCase("NoCIC")) {
            try {
                JSONParser parser = new JSONParser();
                String body = resultObj.get("body").toString();
                JSONObject bodyJSON = (JSONObject) parser.parse(body);
                JSONObject bodyObj = new JSONObject(bodyJSON);
                String INProfileResponseData = bodyObj.get("INProfileResponse").toString();
                JSONObject INProfileResponseDataJSON = (JSONObject) parser.parse(INProfileResponseData);
                JSONObject INProfileResponseDataJSONObj = new JSONObject(INProfileResponseDataJSON);
                JSONObject CAIS_Account = (JSONObject) INProfileResponseDataJSONObj.get("CAIS_Account");
                Object CAISAccountdetails = CAIS_Account.get("CAIS_Account_DETAILS");
                JSONArray CAIS_Account_DETAILS = null;

                if (CAISAccountdetails instanceof JSONArray) {
                    CAIS_Account_DETAILS = (JSONArray) CAISAccountdetails;
                }

                for (int i = 0; i < CAIS_Account_DETAILS.size(); i++) {
                    JSONObject CAIS_Account_DETAILSObj = (JSONObject) CAIS_Account_DETAILS.get(i);

                    JSONArray Advanced_Account_History = (JSONArray) parser
                            .parse(CAIS_Account_DETAILSObj.get("Advanced_Account_History").toString());
                    LinkedHashMap<String, String> inputTags = new LinkedHashMap<>();
                    LinkedHashMap<String, String> npaResult = new LinkedHashMap<>();

                    String Payment_History_Profile = CAIS_Account_DETAILSObj.get("Payment_History_Profile").toString();
                    String open_Date = CAIS_Account_DETAILSObj.get("Open_Date").toString();
                    String Date_Closed = CAIS_Account_DETAILSObj.get("Date_Closed").toString();
                    String payment = Payment_History_Profile.replace("?", "0").replace("S", "0").replace("N", "0")
                            .replace("B", "0").replace("D", "0").replace("M", "0").replace("L", "0");

                    String written_off_Settled_Status = CAIS_Account_DETAILSObj.get("Written_off_Settled_Status")
                            .toString();
                    String dateOfAddition = CAIS_Account_DETAILSObj.get("WriteOffStatusDate").toString();
                    if (dateOfAddition.equals("")) {
                        dateOfAddition = CAIS_Account_DETAILSObj.get("DateOfAddition").toString();
                    }

                    String accountHoldertype = CAIS_Account_DETAILSObj.get("AccountHoldertypeCode").toString();
                    String account_Number = CAIS_Account_DETAILSObj.get("Account_Number").toString();
                    String account_Type = CAIS_Account_DETAILSObj.get("Account_Type").toString();

                    inputTags.put("DateOfAddition", dateOfAddition);
                    inputTags.put("account_Number", account_Number);
                    inputTags.put("Account_Type", account_Type);
                    inputTags.put("open_Date", open_Date);
                    inputTags.put("Date_Closed", Date_Closed);
                    inputTags.put("dateOfAddition", dateOfAddition);

                    inputTags.put("applicantType", applicantType);

                    JSONArray CAIS_Account_History = (JSONArray) parser
                            .parse(CAIS_Account_DETAILSObj.get("CAIS_Account_History").toString());

                    npaResult = checkwritteOff(inputTags, months, written_off_Settled_Status, settledStatus,
                            wriitenofffStatus, restructureStatus, accountHoldertype, npaResult, apiName, ifr);

                    JSONObject latestHistoryObj = null;
                    int latestYear = 0;
                    int latestMonth = 0;

                    for (Object historyObj : Advanced_Account_History) {
                        JSONObject hist = (JSONObject) historyObj;
                        String yearStr = (String) hist.get("Year");
                        String monthStr = (String) hist.get("Month");
                        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : 0;
                        int month = (monthStr != null && !monthStr.isEmpty()) ? Integer.parseInt(monthStr) : 0;

                        if ((year > latestYear) || (year == latestYear && month > latestMonth)) {
                            latestYear = year;
                            latestMonth = month;
                            latestHistoryObj = hist;
                        }
                    }

                    if (latestHistoryObj != null) {
                        String creditLimitAmount = "";
                        creditLimitAmount = latestHistoryObj.get("Credit_Limit_Amount").toString();

                        emiAmount = latestHistoryObj.get("EMI_Amount").toString();

                        currentBalance = latestHistoryObj.get("Current_Balance").toString();
                        npaResult.put("creditLimitAmount", creditLimitAmount);
                        npaResult.put("emiAmount", emiAmount);
                        npaResult.put("currentBalance", currentBalance);
                    }

                    String writteOff = npaResult.get("writteOff");
                    String settled = npaResult.get("settled");
                    String restructred = npaResult.get("restructured");

                    if ("Yes".equalsIgnoreCase(writteOff) || "Yes".equalsIgnoreCase(settled)
                            || "Yes".equalsIgnoreCase(restructred)) {
                        npaResult.put("npa", "No");

                        npaResult.put("moreThan5yearNpa", "No");
                        finalMap.add(npaResult);
                        continue;
                    }
                    String dpdvalues = "";
                    for (int j = 0; j < CAIS_Account_History.size(); j++) {
                        noOfAccountBlock++;
                        JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
                        inputTags.put("Year", CAIS_Account_HistoryObj.get("Year").toString());
                        inputTags.put("Month", CAIS_Account_HistoryObj.get("Month").toString());
                        inputTags.put("dayPastDue", CAIS_Account_HistoryObj.get("Days_Past_Due").toString());
                        inputTags.put("Asset_Classification",
                                CAIS_Account_HistoryObj.get("Asset_Classification").toString());

                        npaResult = checkNpa(inputTags, assetClassification_No, assetClassification_Yes, months, dpddays,
                                npaResult, ifr);

                        inputTags.put("months",
                                months_DPD);
                        dpdvalues = dpdvalues + check12MonthDPD_EX(inputTags, ifr, npaResult);
                        Log.consoleLog(ifr, "crgGenExperian==>DPD Values ." + dpdvalues);
                        npaResult.put("applicant_uid", insertionOrderid);
                        Log.consoleLog(ifr, "crgGenExperian==>DPD insertionOrderid===> ." + insertionOrderid);
                        if ("Yes".equalsIgnoreCase(npaResult.get("npa"))) {
                            finalMap.add(npaResult);
                            break;
                        }
                    }
                    npaResult.put("dpdvalues",
                            dpdvalues);
                    Log.consoleLog(ifr, "crgGenExperian==>DPD Values ." + npaResult.get("dpdvalues"));
                    if (!finalMap.contains(npaResult)) {
                        finalMap.add(npaResult);
                    }
                }
                Log.consoleLog(ifr, "crgGenExperian==>calling formToJson.");

            } catch (Exception e) {
                Log.consoleLog(ifr, "crgGenExperian==>::::===>" + e);

            }
        } else {
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

            map.put("settledMorethan5Year", "No");
            map.put("settled", "No");
            map.put("currentBalance", "No");
            map.put("dpdvalues", "0");
            map.put("wroffMorethan5Year", "No");
            map.put("dateOfAddition", "No");
            map.put("accountNumber", "No");
            map.put("AccountType", "No");
            map.put("creditLimitAmount", "No");
            map.put("DateClosed", "No");
            map.put("emiAmount", "No");
            map.put("applicantType", applicantType);
            map.put("reStructuredMorethan5Year", "No");
            map.put("restructured", "No");
            map.put("moreThan5yearNpa", "No");
            map.put("openDate", "No");
            map.put("AccountHolder", "No");
            map.put("npa", "No");
            map.put("writteOff", "No");
            map.put("applicant_uid", insertionOrderid);
            finalMap.add(map);

        }
        Log.consoleLog(ifr, "crgGenExperian==>calling finalMapfinalMap." + finalMap);
        formToJson(finalMap, apiName, ifr, processInstanceId, applicantType);
        updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderid);

    }

    public void crgGenCibil(IFormReference ifr, JSONObject bodyObj, String processInstanceId, String apiName,
            String applicantType, String type, String insertionOrderId) {
        String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
        Log.consoleLog(ifr, "Delete Query:==> insertionOrderId" + insertionOrderId);

//        String deleteQuery = "DELETE FROM LOS_TRN_CREDITHISTORY_DETAILS WHERE PID = '" + processInstanceId
//                + "' AND API_NAME = '" + apiName
//                  + "' AND APPLICANTTYPE = '" + applicantType + "'";
//        Log.consoleLog(ifr, "Delete Query:==>" + deleteQuery);
//        ifr.saveDataInDB(deleteQuery);
        String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
                replaceAll("#processInstanceId#", processInstanceId).replaceAll("#apiName#", apiName).
                replaceAll("#applicantType#", applicantType).replaceAll("insertionOrderid", insertionOrderId);

        Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
        ifr.saveDataInDB(trndelete);
        //ifr.saveDataInDB(deleteQuery);

        String settledStatus = pcm.getConstantValue(ifr, "EXPERIAN", "SETTLED_STATUS");
        String months = pcm.getConstantValue(ifr, "CIC", "NPAMONTHS");// Y
        String monthsFromConstant = pcm.getConstantValue(ifr, "TRANSUNION", "MONNTH_DPD");
        String wriitenofffStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");
        String restructureStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");

        LinkedHashMap<String, String> npaResult_CB = new LinkedHashMap<>();
        // LinkedHashMap<String, String> npaResult = new LinkedHashMap<>();
        LinkedHashMap<String, String> inputTags_CB = new LinkedHashMap<>();
        List<LinkedHashMap<String, String>> finalMap_CB = new ArrayList<>();
        String dpdfinal = "";
        try {
            JSONParser parser = new JSONParser();
            String consumerCreditData = bodyObj.get("consumerCreditData").toString();
            JSONArray consumerCreditDataJSON = (JSONArray) parser.parse(consumerCreditData);
            if (!type.equalsIgnoreCase("NoCIC")) {

                for (int i = 0; i < consumerCreditDataJSON.size(); i++) {

                    String InputData = consumerCreditDataJSON.get(i).toString();
                    JSONObject consumerCreditDataJSONObj = (JSONObject) parser.parse(InputData);

                    JSONArray accountsArray = (JSONArray) consumerCreditDataJSONObj.get("accounts");

                    if (accountsArray != null) {
                        for (int j = 0; j < accountsArray.size(); j++) {
                            JSONObject account = (JSONObject) accountsArray.get(j);
                            LinkedHashMap<String, String> inputTags = new LinkedHashMap<>();
                            LinkedHashMap<String, String> npaResult = new LinkedHashMap<>();

                            String paymentStartDate = (String) account.get("paymentStartDate");
                            String paymentEndDate = (String) account.get("paymentEndDate");
                            String paymentHistory = (String) account.get("paymentHistory");
                            String ownershipIndicator = (String) String.valueOf(account.get("ownershipIndicator"));
                            String accountType = (String) account.get("accountType");
                            String creditFacilityStatus = (String) String.valueOf(account.get("creditFacilityStatus"));
                            String highCreditAmount = (String) String.valueOf(account.get("highCreditAmount"));
                            String accountNumber = (String) String.valueOf(account.get("accountNumber"));

                            String currentBalance = (String) String.valueOf(account.get("currentBalance"));

                            if (paymentStartDate != null && paymentEndDate != null) {
                                inputTags.put("paymentStartDate", paymentStartDate);
                                inputTags.put("paymentEndDate", paymentEndDate);
                                inputTags.put("paymentHistory", paymentHistory);
                                inputTags.put("ownershipIndicator", ownershipIndicator);
                                inputTags.put("creditFacilityStatus", creditFacilityStatus);
                                inputTags.put("accountType", accountType);
                                inputTags.put("monthsFromConstant", monthsFromConstant);
                                inputTags.put("creditLimitAmount", highCreditAmount);
                                inputTags.put("accountNumber", accountNumber);

                                inputTags.put("applicantType", applicantType);
                                inputTags.put("currentBalance", currentBalance);

                                npaResult = checkNpa_CB(inputTags, npaResult, ifr, months);

                                npaResult = checkwritteOff_CB(inputTags, months, creditFacilityStatus, settledStatus,
                                        wriitenofffStatus, restructureStatus, npaResult, apiName, ifr);
                                inputTags.put("months", months_DPD);
                                npaResult = check12MonthDPDCB(inputTags, ifr, npaResult);

                                npaResult.put("applicant_uid", insertionOrderId);
                                finalMap_CB.add(npaResult);

                            } else {

                                Log.consoleLog(ifr, "crgGenCibil==>Missing paymentStartDate or paymentEndDate in account.");
                            }
                        }

                    }

                }

            } else {

                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

                map.put("settledMorethan5Year", "No");
                map.put("applicant_uid", insertionOrderId);
                map.put("settled", "No");
                map.put("accountType", "No");
                map.put("paymentHistory", "0");
                map.put("dpdvalues", "000");
                map.put("wroffMorethan5Year", "No");
                map.put("dateOfAddition", "No");
                map.put("accountNumber", "No");
                map.put("creditFacilityStatus", "No");
                map.put("paymentEndDate", "No");
                map.put("npaMonth", "No");
                map.put("moreThan5YearNpa", "No");
                map.put("applicantType", applicantType);
                map.put("reStructuredMorethan5Year", "No");
                map.put("restructured", "No");
                map.put("monthDifference", "No");
                map.put("npa", "No");
                map.put("AccountHolder", "No");
                map.put("writteOff", "No");

                finalMap_CB.add(map);

            }
            Log.consoleLog(ifr, "crgGenCibil==>calling formToJson. CIBILL()()()" + finalMap_CB);

            formToJson(finalMap_CB, apiName, ifr, processInstanceId, applicantType);
            updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderId);

            // System.out.println("npaResult_CB finalMap_CB: " + finalMap_CB);
        } catch (Exception e) {
            Log.consoleLog(ifr, "crgGenCibil==>::::===>" + e);

        }

    }

    public void crgGenHighMark(IFormReference ifr, String responseBody, String processInstanceId, String apiName,
            String applicantType, String insertionOrderid) {

        String installmentAmount = "";

        String writtenOffSettledStatus = "";

        if (!responseBody.equalsIgnoreCase("{}")) {
            List<LinkedHashMap<String, String>> finalMap_HM = new ArrayList<>();

            String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
            String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");
            Log.consoleLog(ifr, "apiName apiName:==>####" + apiName);
            Log.consoleLog(ifr, "applicantType applicantType:==>####" + applicantType);
            Log.consoleLog(ifr, "applicantType applicantType:==>####" + insertionOrderid);
            try {

//                String deleteQuery = "DELETE FROM LOS_TRN_CREDITHISTORY_DETAILS WHERE PID = '" + processInstanceId
//                        + "' AND API_NAME = '" + apiName
//                        + "' AND APPLICANTTYPE = '" + applicantType + "'"
//                        + "' AND APPLICANT_UID = '" + insertionOrderid + "'";
//                Log.consoleLog(ifr, "Delete Query:==>####" + deleteQuery);
//                ifr.saveDataInDB(deleteQuery);
                String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
                        replaceAll("#processInstanceId#", processInstanceId).replaceAll("#apiName#", apiName).
                        replaceAll("#applicantType#", applicantType).replaceAll("insertionOrderid", insertionOrderid);

                Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
                ifr.saveDataInDB(trndelete);

                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);

                JSONObject body = (JSONObject) OutputJSON.get("body");
                JSONObject INDV_REPORT_FILE = (JSONObject) body.get("INDV-REPORT-FILE");
                JSONArray INDV_REPORTS = (JSONArray) INDV_REPORT_FILE.get("INDV-REPORTS");

                for (int i = 0; i < INDV_REPORTS.size(); i++) {
                    JSONObject INDVReportDataVal = (JSONObject) INDV_REPORTS.get(i);
                    JSONObject INDV_REPORT = (JSONObject) INDVReportDataVal.get("INDV-REPORT");
                    JSONArray responses = (JSONArray) INDV_REPORT.get("RESPONSES");

                    for (Object responseObj : responses) {
                        // Create a new map for each account
                        LinkedHashMap<String, String> highmarkDPD = new LinkedHashMap<String, String>();

                        JSONObject response = (JSONObject) responseObj;
                        JSONObject loanDetails = (JSONObject) response.get("LOAN-DETAILS");
                        if (loanDetails != null) {
                            String ownershipInd = loanDetails.get("OWNERSHIP-IND").toString();
                            String accountNumber = loanDetails.get("ACCT-NUMBER").toString();
                            String accType = loanDetails.get("ACCT-TYPE").toString();
                            accType = pcm.getConstantValue(ifr, "HIGHMARK", accType);
                            String accountStatus = loanDetails.get("ACCOUNT-STATUS").toString();

                            if (loanDetails.containsKey("INSTALLMENT-AMT")
                                    && loanDetails.get("INSTALLMENT-AMT") != null) {
                                installmentAmount = loanDetails.get("INSTALLMENT-AMT").toString();
                            }

                            String paymentHistory = loanDetails.get("COMBINED-PAYMENT-HISTORY").toString();
                            String suitfield = "";
                            if (loanDetails.containsKey("SUIT-FILED_WILFUL-DEFAULT")) {
                                suitfield = loanDetails.get("SUIT-FILED_WILFUL-DEFAULT").toString();
                            }
                            if ("Individual".equalsIgnoreCase(ownershipInd)
                                    || "Joint".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD = processPaymentHistory(paymentHistory, highmarkDPD, months_DPD, ifr);
                                highmarkDPD = processNpaFromPaymentHistory(paymentHistory, highmarkDPD, months, ifr);
                                if (loanDetails.containsKey("WRITTEN-OFF_SETTLED-STATUS")) {
                                    String dateReported = loanDetails.get("DATE-REPORTED").toString();
                                    writtenOffSettledStatus = loanDetails.get("WRITTEN-OFF_SETTLED-STATUS").toString();
                                    highmarkDPD = processwriteOffFromaccountStatus(dateReported, accountStatus,
                                            writtenOffSettledStatus, highmarkDPD, months, ifr);
                                }
                            }

                            // Set AccountHolder value
                            if ("Individual".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "1");
                            } else if ("Authorized User".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "2");
                            } else if ("Guarantor".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "3");
                            } else if ("Joint".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "4");
                            } else {
                                highmarkDPD.put("AccountHolder", "5");
                            }

                            highmarkDPD.put("accountType", accType);
                            highmarkDPD.put("accountNumber", accountNumber);
                            highmarkDPD.put("suitfield", suitfield);

                            // Add to final map for each account
                            Log.consoleLog(ifr, "highmarkDPD==>highmarkDPD" + highmarkDPD);
                            highmarkDPD.put("applicantType", applicantType);
                            highmarkDPD.put("applicant_uid", insertionOrderid);
                            formToJson_highMark(highmarkDPD, apiName, ifr, processInstanceId, applicantType);
                            highmarkDPD.clear();

                        }
                    }
                }

                Log.consoleLog(ifr, "finalMap_HM==>finalMap_HM" + finalMap_HM);

                updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderid);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void crgGenEquifax(IFormReference ifr, String responseBody, String processInstanceId, String apiName,
            String applicantType, String insertionOrderid) {

        String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
                replaceAll("#processInstanceId#", processInstanceId).replaceAll("#apiName#", apiName).
                replaceAll("#applicantType#", applicantType).replaceAll("$$$$insertionOrderid$$$$", insertionOrderid);

        Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
        ifr.saveDataInDB(trndelete);

        String score = "";
        String dpdpaymentHistory = "";
        String OwnershipType = "";
        String accountNumber = "";
        String accountType = "";
        String accountStatus = "";

        if (!responseBody.equalsIgnoreCase("{}")) {

            try {
                String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
                String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
                JSONObject body = (JSONObject) OutputJSON.get("body");

                JSONObject CCRResponse = (JSONObject) body.get("CCRResponse");

                JSONArray CIRReportDataLst = (JSONArray) CCRResponse.get("CIRReportDataLst");

                if (CIRReportDataLst.size() > 0) {
                    JSONObject firstReportData = (JSONObject) CIRReportDataLst.get(0);

                    if (firstReportData.containsKey("Error")) {

                        JSONObject errorObj = (JSONObject) firstReportData.get("Error");
                        String errorCode = errorObj.get("ErrorCode").toString();
                        String errorDesc = errorObj.get("ErrorDesc").toString();

                        if (errorDesc.equalsIgnoreCase("Consumer not found in bureau")) {
                            score = "-1";
                        }

                    }

                    for (int CIRReportDataLstCount = 0; CIRReportDataLstCount < CIRReportDataLst
                            .size(); CIRReportDataLstCount++) {
                        JSONObject CIRReportDataVal = (JSONObject) CIRReportDataLst.get(CIRReportDataLstCount);
                        if (CIRReportDataVal.containsKey("CIRReportData")) {
                            JSONObject CIRReportData = (JSONObject) CIRReportDataVal.get("CIRReportData");
                            JSONArray ScoreDetails = (JSONArray) CIRReportData.get("ScoreDetails");
                            JSONArray RetailAccountDetails = (JSONArray) CIRReportData.get("RetailAccountDetails");

                            for (int i = 0; i < RetailAccountDetails.size(); i++) {

                                JSONObject RetailAccountDetailsObj = (JSONObject) RetailAccountDetails.get(i);
                                String history48Months = RetailAccountDetailsObj.get("History48Months").toString();
                                JSONArray History48Months = (JSONArray) parser.parse(history48Months);
                                LinkedHashMap<String, String> dpdMap = new LinkedHashMap<String, String>();
                                if (dpdMap.containsKey("dpdvalues")) {
                                    Log.consoleLog(ifr, "Equifaxapi.containsKey:::===>" + dpdMap);
                                    if (dpdMap.get("dpdvalues") != null) {
                                        dpdMap.remove("dpdvalues");
                                        dpdpaymentHistory = "";
                                        Log.consoleLog(ifr, "Equifaxapi.remove:::===>" + dpdMap);
                                    }
                                }
                                for (int j = 0; j < History48Months.size(); j++) {
                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                    LinkedHashMap<String, String> input = new LinkedHashMap<String, String>();

                                    String key = History48MonthsObj.get("key").toString();
                                    String paymentHistory = History48MonthsObj.get("PaymentStatus").toString();
                                    OwnershipType = RetailAccountDetailsObj.get("OwnershipType").toString();
                                    accountNumber = RetailAccountDetailsObj.get("AccountNumber").toString();
                                    accountType = RetailAccountDetailsObj.get("AccountType").toString();
                                    accountType = pcm.getConstantValue(ifr, "EQUIFAXAPI", accountType);
                                    accountStatus = RetailAccountDetailsObj.get("AccountStatus").toString();
                                    String classificationStatus = "";
                                    if (History48MonthsObj.get("AssetClassificationStatus") != null) {
                                        classificationStatus = History48MonthsObj.get("AssetClassificationStatus")
                                                .toString().trim();
                                    }

                                    if ("*".equals(classificationStatus) || classificationStatus.isEmpty()) {
                                        classificationStatus = "STD";
                                    }
                                    input.put("AssetClassificationStatus", classificationStatus);
                                    input.put("paymentHistory", paymentHistory);
                                    input.put("dpdYear", key);
                                    input.put("accountStatus", accountStatus);

                                    if (OwnershipType.equalsIgnoreCase("Individual")
                                            || OwnershipType.equalsIgnoreCase("Joint")) {
                                        dpdpaymentHistory += processPaymentStatusEquifaxapi(input, dpdMap, months_DPD, ifr);
                                        dpdMap = processNPAStatusEquifaxapi(input, dpdMap, months, ifr);
                                        dpdMap = processWRITEOFFStatusEquifaxapi(input, dpdMap, months, ifr);

                                    }
                                    dpdMap.put("dpdvalues", dpdpaymentHistory);
                                }

                                dpdpaymentHistory = "";
                                dpdMap.put("AccountHolder", OwnershipType);
                                dpdMap.put("accountNumber", accountNumber);
                                dpdMap.put("accountType", accountType);
                                dpdMap.put("applicantType", applicantType);
                                dpdMap.put("applicant_uid", insertionOrderid);

                                formToJson_highMark(dpdMap, apiName, ifr, processInstanceId, applicantType);

                            }

                        }
                    }

                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    public LinkedHashMap<String, String> checkNpa(Map<String, String> inputTags, String assetClassification_No,
            String assetClassification_Yes, String months, String dpddays, LinkedHashMap<String, String> npaResult,
            IFormReference ifr) {
        try {
            Log.consoleLog(ifr, " Started checkNpa==>");
            String npa = "No";

            String moreThan5Year = "No";

            if (!inputTags.get("Asset_Classification").equals("?")) {
                Calendar current = Calendar.getInstance();
                int currentYear = current.get(Calendar.YEAR);
                int currentMonth = current.get(Calendar.MONTH) + 1;

                String dpdStr = inputTags.containsKey("dayPastDue") && inputTags.get("dayPastDue") != null
                        && !inputTags.get("dayPastDue").trim().isEmpty() ? inputTags.get("dayPastDue") : "0";
                int dpd = Integer.parseInt(dpdStr);

                int dpdCount = Integer.parseInt(dpddays);

                String yearStr = inputTags.containsKey("Year") && inputTags.get("Year") != null
                        && !inputTags.get("Year").trim().isEmpty() ? inputTags.get("Year") : "0";
                int year = Integer.parseInt(yearStr);

                String monthStr = inputTags.containsKey("Month") && inputTags.get("Month") != null
                        && !inputTags.get("Month").trim().isEmpty() ? inputTags.get("Month") : "0";
                int month = Integer.parseInt(monthStr);

                // System.out.println("Exception in checkNpa: " + yearStr);
                int monthsDiff = (currentYear - year) * 12 + (currentMonth - month);
                int monthsFromConstant = Integer.parseInt(months);

                if (assetClassification_Yes.contains(inputTags.get("Asset_Classification"))) {

                    if (monthsDiff <= monthsFromConstant) {
                        npa = "Yes";
                    } else {
                        moreThan5Year = "Yes";
                    }
                }
            }

            npaResult.put("npa", npa);

            npaResult.put("moreThan5yearNpa", moreThan5Year);

        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception checkNpa==>" + e);
        }
        return npaResult;
    }

    public LinkedHashMap<String, String> checkwritteOff(Map<String, String> inputTags, String months,
            String written_off_Settled_Status, String wfsettledStatus, String wriitenofffStatus,
            String restructureStatus, String accountHoldertype, LinkedHashMap<String, String> npaResult, String apiname,
            IFormReference ifr) {

        try {

            Log.consoleLog(ifr, " checkwritteOff checkwritteOff==>" + "Started");
            Calendar currentDate = Calendar.getInstance();
            int currentYear = currentDate.get(Calendar.YEAR);
            int currentMonth = currentDate.get(Calendar.MONTH) + 1;

            String WritteOff = "No";
            String settled = "No";
            String restructred = "No";

            String settledMorethan5Year = "No";
            String wroffMorethan5Year = "No";
            String reStructuredMorethan5Year = "No";

            int monthsFromConstant = 0;
            try {
                monthsFromConstant = Integer.parseInt(months);
            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Invalid value for 'months': " + months);
                return npaResult;
            }

            int monthsDiff = 0;
            String dateOfAdditionStr = inputTags.get("DateOfAddition");

            if (dateOfAdditionStr != null && dateOfAdditionStr.length() >= 6) {
                try {
                    int addYear = Integer.parseInt(dateOfAdditionStr.substring(0, 4));
                    int addMonth = Integer.parseInt(dateOfAdditionStr.substring(4, 6));

                    monthsDiff = (currentYear - addYear) * 12 + (currentMonth - addMonth);
                } catch (Exception e) {
                    Log.consoleLog(ifr, "Invalid DateOfAddition format: " + dateOfAdditionStr);
                }
            }

            if (written_off_Settled_Status != null && !written_off_Settled_Status.isEmpty()) {
                if (wfsettledStatus != null && wfsettledStatus.contains(written_off_Settled_Status)) {
                    settled = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        settledMorethan5Year = "Yes";
                        settled = "No";
                    }
                } else if (wriitenofffStatus != null && wriitenofffStatus.contains(written_off_Settled_Status)) {
                    WritteOff = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        wroffMorethan5Year = "Yes";
                        WritteOff = "No";
                    }
                } else if (restructureStatus != null && restructureStatus.contains(written_off_Settled_Status)) {
                    restructred = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        reStructuredMorethan5Year = "Yes";
                        restructred = "No";
                    }
                }
            }

            npaResult.put("applicantType", inputTags.get("applicantType"));
            npaResult.put("writteOff", WritteOff);
            npaResult.put("wroffMorethan5Year", wroffMorethan5Year);
            npaResult.put("settled", settled);
            npaResult.put("settledMorethan5Year", settledMorethan5Year);
            npaResult.put("restructured", restructred);
            npaResult.put("reStructuredMorethan5Year", reStructuredMorethan5Year);
            npaResult.put("dateOfAddition", dateOfAdditionStr);
            npaResult.put("accountNumber", inputTags.get("account_Number"));
            npaResult.put("AccountType", inputTags.get("Account_Type"));
            npaResult.put("openDate", inputTags.get("open_Date"));
            npaResult.put("DateClosed", inputTags.get("Date_Closed"));
            npaResult.put("AccountHolder", accountHoldertype);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in checkwritteOff==> " + e);
        }

        return npaResult;
    }

    public int getMonthDifference(String start, String end, IFormReference ifr) {
        try {

            Log.consoleLog(ifr, " Started getMonthDifference==>");
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);

            if (startDate.after(endDate)) {
                // Swap dates
                Date temp = startDate;
                startDate = endDate;
                endDate = temp;
            }

            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            startCal.setTime(startDate);
            endCal.setTime(endDate);

            int yearDiff = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
            int monthDiff = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);

            return yearDiff * 12 + monthDiff;

        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception getMonthDifference==>" + e);
            return 0;
        }
    }

    public JSONObject formToJson(List<LinkedHashMap<String, String>> finalMap, String apiName, IFormReference ifr,
            String processInstanceId, String applicantType) {
        JSONArray jsonOutput = new JSONArray();
        LinkedHashMap<String, String> insertMap = new LinkedHashMap();
        for (int i = 0; i < finalMap.size(); i++) {
            LinkedHashMap<String, String> map = finalMap.get(i);
            JSONObject jsonObject = new JSONObject();
            JSONObject blockData = new JSONObject();
            String blockName = "noOfAccountBlock" + (i + 1);

            for (Map.Entry<String, String> entry : map.entrySet()) {
                blockData.put(entry.getKey(), entry.getValue());
                insertMap.put(entry.getKey(), entry.getValue());
            }

            jsonObject.put(blockName, blockData);
            jsonOutput.add(jsonObject);
            insertAllAccountBlocks(insertMap, apiName, processInstanceId, ifr, applicantType);

        }

        Log.consoleLog(ifr, "finalOutput:::CRG::==>" + jsonOutput);

        JSONObject finalOutput = new JSONObject();
        finalOutput.put(apiName, jsonOutput);
        Log.consoleLog(ifr, "finalOutput:::CRG::==>" + finalOutput);

        return finalOutput;
    }

    public LinkedHashMap<String, String> checkwritteOff_CB(Map<String, String> inputTags, String months,
            String written_off_Settled_Status, String wfsettledStatus, String wriitenofffStatus,
            String restructureStatus, LinkedHashMap<String, String> npaResult, String apiname, IFormReference ifr) {

        Log.consoleLog(ifr, "Started checkwritteOff_CB:::CRG::==>");

        try {
            String WritteOff = "No";
            String settled = "No";
            String restructured = "No";

            String settledMorethan5Year = "No";
            String wroffMorethan5Year = "No";
            String reStructuredMorethan5Year = "No";
            int monthsFromConstant = 0;
            try {
                monthsFromConstant = Integer.parseInt(months);
            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Invalid months input: " + months);
                return npaResult;
            }

            int monthsDiff = 0;
            String paymentStartDate = inputTags.get("paymentStartDate");

            if (paymentStartDate != null && paymentStartDate.length() == 8) {
                try {
                    int day = Integer.parseInt(paymentStartDate.substring(0, 2));
                    int month = Integer.parseInt(paymentStartDate.substring(2, 4)) - 1;
                    int year = Integer.parseInt(paymentStartDate.substring(4, 8));

                    Calendar startDate = Calendar.getInstance();
                    startDate.set(Calendar.YEAR, year);
                    startDate.set(Calendar.MONTH, month);
                    startDate.set(Calendar.DAY_OF_MONTH, day);

                    Calendar currentDate = Calendar.getInstance();

                    int diffYear = currentDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
                    int diffMonth = currentDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);

                    monthsDiff = diffYear * 12 + diffMonth;

                    if (currentDate.get(Calendar.DAY_OF_MONTH) < startDate.get(Calendar.DAY_OF_MONTH)) {
                        monthsDiff--;
                    }

                } catch (Exception e) {
                    Log.consoleLog(ifr, "Invalid paymentStartDate format: " + paymentStartDate);
                }
            }

            if (written_off_Settled_Status != null && !written_off_Settled_Status.isEmpty()) {
                if (wfsettledStatus != null && wfsettledStatus.contains(written_off_Settled_Status)) {
                    settled = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        settledMorethan5Year = "Yes";
                    }
                } else if (wriitenofffStatus != null && wriitenofffStatus.contains(written_off_Settled_Status)) {
                    WritteOff = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        wroffMorethan5Year = "Yes";
                    }
                } else if (restructureStatus != null && restructureStatus.contains(written_off_Settled_Status)) {
                    restructured = "Yes";
                    if (monthsDiff > monthsFromConstant) {
                        reStructuredMorethan5Year = "Yes";
                    }
                }
            }

            npaResult.put("dateOfAddition", inputTags.get("paymentStartDate"));
            npaResult.put("paymentHistory", inputTags.get("paymentHistory"));
            npaResult.put("AccountHolder", inputTags.get("ownershipIndicator"));
            npaResult.put("creditFacilityStatus", inputTags.get("creditFacilityStatus"));
            npaResult.put("accountType", inputTags.get("accountType"));
            npaResult.put("applicantType", inputTags.get("applicantType"));

            npaResult.put("writteOff", WritteOff);
            npaResult.put("wroffMorethan5Year", wroffMorethan5Year);
            npaResult.put("settled", settled);
            npaResult.put("settledMorethan5Year", settledMorethan5Year);
            npaResult.put("restructured", restructured);
            npaResult.put("reStructuredMorethan5Year", reStructuredMorethan5Year);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in checkwritteOff_CB:::CRG::==> " + e);
        }

        return npaResult;
    }

    public void insertAllAccountBlocks(LinkedHashMap<String, String> insertMap, String apiName,
            String processInstanceId, IFormReference ifr, String applicantType) {
        try {

            StringBuilder columns = new StringBuilder("PID, API_NAME");
            StringBuilder values = new StringBuilder("'" + processInstanceId + "', '" + apiName + "'");

            for (Map.Entry<String, String> entry : insertMap.entrySet()) {
                columns.append(", ").append(entry.getKey());
                values.append(", '").append(entry.getValue()).append("'");
            }

            String insertQuery = "INSERT INTO LOS_TRN_CREDITHISTORY_DETAILS (" + columns.toString() + ") VALUES ("
                    + values.toString() + ")";
            Log.consoleLog(ifr, "Insert Query:==>" + insertQuery);
            ifr.saveDataInDB(insertQuery);

            // "" need to change UniqueId
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:::::==> insertAllAccountBlocks " + e);
        }
    }

    public LinkedHashMap check12MonthDPDCB(LinkedHashMap<String, String> inputTags, IFormReference ifr,
            LinkedHashMap<String, String> npaResult) {
        Log.consoleLog(ifr, "inputTags= " + inputTags);

        try {

            String paymentStartDateStr = inputTags.get("paymentStartDate");
            String paymentHistory = inputTags.get("paymentHistory");

            try {
                String monthsFromConstant = inputTags.get("months");

                int MONTHS = 0;

                if (paymentHistory.contains("STD")) {
                    paymentHistory = paymentHistory.replace("STD", "000");
                }
                if (paymentHistory.contains("SUB")) {
                    paymentHistory = paymentHistory.replace("SUB", "091");
                }
                if (paymentHistory.contains("DBT")) {
                    paymentHistory = paymentHistory.replace("DBT", "091");
                }
                if (paymentHistory.contains("LSS")) {
                    paymentHistory = paymentHistory.replace("LSS", "091");
                }

                paymentHistory = paymentHistory.replaceAll("[a-zA-Z]", "0");

                Log.consoleLog(ifr, ":::::==> check12MonthDPDCB" + paymentHistory);
                MONTHS = Integer.parseInt(monthsFromConstant);

                long monthsDifferenceResult = 0;

                SimpleDateFormat inputFormatter = new SimpleDateFormat("ddMMyyyy");

                Date paymentStartDate_str = inputFormatter.parse(paymentStartDateStr);

                Date currentDate = new Date();
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(paymentStartDate_str);
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);

                int yearsDifference = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                int monthsDifference = yearsDifference * 12 + currentCalendar.get(Calendar.MONTH)
                        - startCalendar.get(Calendar.MONTH);
                Log.consoleLog(ifr, ":::::==> yearsDifference" + yearsDifference);
                Log.consoleLog(ifr, ":::::==> monthsDifference" + monthsDifference);
                Log.consoleLog(ifr, ":::::==> MONTHS" + MONTHS);
                Log.consoleLog(ifr, ":::::==> paymentHistory" + paymentHistory);

                if (monthsDifference <= MONTHS) {
                    Log.consoleLog(ifr, ":::::==> inside if  monthsDifference" + monthsDifference);

                    if (monthsDifference == MONTHS) {
                        monthsDifference = MONTHS;

                        Log.consoleLog(ifr, ":::::==> inside if  monthsDifference" + monthsDifference);
                    } else {
                        monthsDifference = MONTHS - monthsDifference;
                        Log.consoleLog(ifr, ":::::==> inside else  monthsDifference" + monthsDifference);

                    }
                    monthsDifferenceResult = (long) (monthsDifference * 3);

                } else {

                    paymentHistory = "";

                }
                if (monthsDifference * 3 > paymentHistory.length()) {

                    // payment = payment;
                } else {

                    paymentHistory = paymentHistory.substring(0, (int) monthsDifferenceResult);
                }

            } catch (Exception e) {

            }
            Log.consoleLog(ifr, ":::::==> inside paymentHistory  paymentHistory" + paymentHistory);
            npaResult.put("dpdvalues", paymentHistory);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:::::==> dpdvalues CB " + e);
        }
        return npaResult;

    }

    public String check12MonthDPD_EX(LinkedHashMap<String, String> inputTags, IFormReference ifr,
            LinkedHashMap<String, String> npaResult) {

        Log.consoleLog(ifr, "Inside check12MonthDPD_EX ==> " + inputTags);

        String daysPastDue = inputTags.get("dayPastDue");
        Log.consoleLog(ifr, "Original daysPastDue: " + daysPastDue);

        try {
            String expYear = inputTags.get("Year");
            String expMonth = inputTags.get("Month");

            Log.consoleLog(ifr, "Input Year: " + expYear);
            Log.consoleLog(ifr, "Input Month: " + expMonth);

            if (expYear == null || expMonth == null || expYear.trim().isEmpty() || expMonth.trim().isEmpty()) {
                Log.consoleLog(ifr, "Year or Month is missing.");
                throw new IllegalArgumentException("Month or Year is missing from inputTags.");
            }
            int year = Integer.parseInt(expYear.trim());
            int month = Integer.parseInt(expMonth.trim());
            Log.consoleLog(ifr, "Parsed Year: " + year);
            Log.consoleLog(ifr, "Parsed Month: " + month);

            Calendar expCalendar = Calendar.getInstance();
            expCalendar.set(Calendar.YEAR, year);
            expCalendar.set(Calendar.MONTH, month - 1); // 0-based in Java
            expCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Log.consoleLog(ifr, "Calendar set with expected date: " + expCalendar.getTime());

            Calendar currentCalendar = Calendar.getInstance();
            Log.consoleLog(ifr, "Current date: " + currentCalendar.getTime());

            int monthsDiff = (currentCalendar.get(Calendar.YEAR) - expCalendar.get(Calendar.YEAR)) * 12
                    + (currentCalendar.get(Calendar.MONTH) - expCalendar.get(Calendar.MONTH));
            Log.consoleLog(ifr, "Calculated months difference: " + monthsDiff);
            int allowedMonths = Integer.parseInt(inputTags.get("months").trim());

            if (monthsDiff > allowedMonths) {
                Log.consoleLog(ifr, "Data is older than 12 months. Returning empty DPD.");
                return "";
            }

            if (daysPastDue == null || daysPastDue.trim().isEmpty()) {
                Log.consoleLog(ifr, "daysPastDue is null or empty. Returning empty.");
                return "";
            }

            daysPastDue = daysPastDue.trim().toUpperCase();
            Log.consoleLog(ifr, "Trimmed & uppercased daysPastDue: " + daysPastDue);

            if (daysPastDue.contains("B") || daysPastDue.contains("D")
                    || daysPastDue.contains("M") || daysPastDue.contains("L")) {
                daysPastDue = "091";
                Log.consoleLog(ifr, "DPD contains B/D/M/L. Set to 091.");
            }

            try {
                daysPastDue = String.format("%03d", Integer.parseInt(daysPastDue));
                Log.consoleLog(ifr, "Formatted numeric daysPastDue: " + daysPastDue);
            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Invalid numeric DPD format. Setting to empty.");
                daysPastDue = "";
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in check12MonthDPD_EX: " + e.getMessage());
            daysPastDue = "";
        }

        Log.consoleLog(ifr, "Final returned daysPastDue: " + daysPastDue);
        return daysPastDue;
    }

    public String updateCreditHistory(IFormReference ifr, String Apiname, String Pid, String ApplicantCode, String UniqueId) {

        String bureautype = "";
        String strGWoffSdHist = "No";
        String strGnpainp = "No";
        String strsrnpainp = "No";
        String strSethis = "No";
        String cicnpacheck = "No";
        String writeoff = "No";
        String strpayhiscomb = "000";

        if (Apiname.equalsIgnoreCase("EXPERIAN")) {
            bureautype = "EX";
        } else if (Apiname.equalsIgnoreCase("CIBIL")) {
            bureautype = "CB";
        } else if (Apiname.equalsIgnoreCase("HighMark")) {
            bureautype = "HM";
        } else if (Apiname.equalsIgnoreCase("EQUIFAX")) {
            bureautype = "EQ";
        }
        strGWoffSdHist = cicvalues(ifr, Apiname, Pid, "WRITEOFFSETTLE", ApplicantCode, UniqueId);
        strGnpainp = cicvalues(ifr, Apiname, Pid, "GNPAINP", ApplicantCode, UniqueId);
        strsrnpainp = cicvalues(ifr, Apiname, Pid, "SRNPAINP", ApplicantCode, UniqueId);
        strSethis = cicvalues(ifr, Apiname, Pid, "SETHIS", ApplicantCode, UniqueId);
        cicnpacheck = cicvalues(ifr, Apiname, Pid, "CICNPACHECK", ApplicantCode, UniqueId);
        writeoff = cicvalues(ifr, Apiname, Pid, "WRITEOFF", ApplicantCode, UniqueId);
        List<List<String>> Result = cf.mExecuteQuery(ifr,
                ConfProperty.getQueryScript("PAYMENTHISCOM").replace("#PROCESSINSTANCEID#", Pid)
                        .replace("#APPLICANT_TYPE#", ApplicantCode).replace("#BUREAUTYPE#", Apiname).
                        replace("APPLICANT_UID", UniqueId),
                "Execute query for fetching strpayhiscomb ");

        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (!Result.isEmpty()) {
            strpayhiscomb = Result.get(0).get(0);
        }
        Log.consoleLog(ifr, "CRGGenerator:updateCICData->writeoff:: " + writeoff + "strpayhiscomb::" + strpayhiscomb
                + "strGWoffSdHist::" + strGWoffSdHist + "bureautype::" + bureautype + "strGnpainp::" + strGnpainp
                + "strsrnpainp::" + strsrnpainp + "strSethis::" + strSethis + "cicnpacheck::" + cicnpacheck);

        String updatequery = ConfProperty.getQueryScript("UPDTCICDATA").replace("#PROCESSINSTANCEID#", Pid)
                .replace("#GUARANTORWRITEOFFSETTLEDHIST#", strGWoffSdHist).replace("#GUARANTORNPAINP#", strGnpainp)
                .replace("#SRNPAINP#", strsrnpainp).replace("#SETTLEDHISTORY#", strSethis)
                .replace("#CICNPACHECK#", cicnpacheck).replace("#WRITEOFF#", writeoff)
                .replace("#PAYHISTORYCOMBINED#", strpayhiscomb).replace("#APPLICANT_TYPE#", ApplicantCode)
                .replace("#BUREAUTYPE#", bureautype);

        Log.consoleLog(ifr, "CRGGenerator:updateCICData->updatequeryforCICTables: " + updatequery);
        cf.mExecuteQuery(ifr, updatequery, "updatequery for credit history");
        return "";
    }

    public String cicvalues(IFormReference ifr, String bureautype, String Pid, String outputvalue,
            String ApplicantCode, String UniqueId) {
        List<List<String>> Result = cf.mExecuteQuery(ifr,
                ConfProperty.getQueryScript(outputvalue).replace("#PROCESSINSTANCEID#", Pid)
                        .replace("#APPLICANT_TYPE#", ApplicantCode).replace("#BUREAUTYPE#", bureautype).replaceAll("#APPLICANT_UID#", UniqueId),
                "Execute query for fetching CIC " + outputvalue);

        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (!Result.isEmpty()) {
            if (Integer.parseInt(Result.get(0).get(0)) > 0) {
                outputvalue = "Yes";
            } else {
                outputvalue = "No";
            }
        }
        return outputvalue;
    }

    public String requestloanAmount(IFormReference ifr) {
        String requestloanamt = "0";
        List<List<String>> Result = cf.mExecuteQuery(ifr,
                ConfProperty.getQueryScript("RECOMMEDEDLOANAMT")
                        .replace("#PROCESSINSTANCEID#", ifr.getObjGeneralData().getM_strProcessInstanceId()),
                "Execute query for fetching RECOMMEDEDLOANAMT loan amount");
        Log.consoleLog(ifr, "#RECOMMEDEDLOANAMT Result===>" + Result.toString());
        if (!Result.isEmpty() && Result.get(0).get(0) != null && !Result.get(0).get(0).trim().equals("")) {
            requestloanamt = Result.get(0).get(0);
        } else {

            Result = cf.mExecuteQuery(ifr,
                    ConfProperty.getQueryScript("REQUESTEDLOANAMT")
                            .replace("#PROCESSINSTANCEID#", ifr.getObjGeneralData().getM_strProcessInstanceId()),
                    "Execute query for fetching requested loan amount");

            Log.consoleLog(ifr, "#REQUESTEDLOANAMT Result===>" + Result.toString());

            if (!Result.isEmpty() && Result.get(0).get(0) != null && !Result.get(0).get(0).trim().equals("")) {
                requestloanamt = Result.get(0).get(0);
            }
        }

        return requestloanamt;
    }

    public String[] bureauTypes(IFormReference ifr, String processInstanceId, String applicantCode) {
        String additionalCondition = "";
        String ciccheckamount = pcm.getParamValue(ifr, "BUREAURULE", pcm.mGetProductCode(ifr));

        if (applicantCode.contains("~")) {
            additionalCondition = " AND APPLICANT_TYPE = '" + applicantCode.split("~")[1] + "'";
            applicantCode = applicantCode.split("~")[0];
        }
        // Use the correct script for bureau types or build it if needed
        String cbTypeQuery = ConfProperty.getQueryScript("BUREAUTYPEQUERY")
                .replace("#PID#", processInstanceId)
                .replace("#APPLICANTCODE#", applicantCode) + additionalCondition;

        List<List<String>> list = cf.mExecuteQuery(ifr, cbTypeQuery, "Query to get bureauTypes");
        List<String> bureauTypesList = new ArrayList<String>();

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                bureauTypesList.add(list.get(i).get(0));
            }
        } else {
            // Handle fallback for Portal and Branch Maker
            String activity = ifr.getActivityName();
            if ("Portal".equalsIgnoreCase(activity) || "Branch Maker".equalsIgnoreCase(activity)) {
                try {
                    int requestedLoanAmt = Integer.parseInt(requestloanAmount(ifr));
                    int cicCheckAmt = Integer.parseInt(ciccheckamount);

                    if (requestedLoanAmt > cicCheckAmt) {
                        bureauTypesList.add("EX");
                    }
                } catch (NumberFormatException e) {
                    Log.consoleLog(ifr, "Error parsing loan amounts: " + e.getMessage());
                }
                bureauTypesList.add("CB");
            }
        }

        return bureauTypesList.toArray(new String[0]);
    }

    public Map<String, List<String>> calculateOverDue(IFormReference ifr, String ProcessInstanceId, String applicantCode) {

        Log.consoleLog(ifr, "calculateOverDue  Started===> ");
        Map<String, List<String>> bureauStatsMap = new HashMap<>();
        String[] whereCICValues = ConfProperty.getCommonPropertyValue("WHERECICVALUES").split(",");
        BudgetPortalCustomCode bcc = new BudgetPortalCustomCode();
        String additionalCondition = "";
        if (applicantCode.contains("~")) {
            // additionalCondition = " AND " + whereCICValues[0] + "= '" + applicantCode.split("~")[1]  + "'";

            //additionalCondition = " AND APPLICANT_UID = '" + applicantCode.split("~")[1] + "'";
            additionalCondition = " AND " + whereCICValues[0] + "= '" + applicantCode.split("~")[1] + "'";
            applicantCode = applicantCode.split("~")[0];

        }
        Log.consoleLog(ifr, "outside if calculateOverDue  ===> ");
        String[] bureauTypes = bureauTypes(ifr, ProcessInstanceId, applicantCode);
        for (String bureau : bureauTypes) {
            try {
                String query = ConfProperty.getQueryScript("BUREAUPAYMENTHIS")
                        .replace("#PID#", ProcessInstanceId)
                        .replace("#APPLICANTCODE#", applicantCode).replace("#BUREAUTYPE#", bureau) + additionalCondition;

                List<List<String>> result = cf.mExecuteQuery(ifr, query, "paymenthistory_" + bureau);
                String paymentHistory = (!result.isEmpty() && !result.get(0).isEmpty()) ? result.get(0).get(0) : "0";

                Log.consoleLog(ifr, "PaymentHistory (" + bureau + "): " + paymentHistory);

                String cleaned = paymentHistory.replaceAll("[A-Za-z*]", "");
                String[] parts = cleaned.split("(?<=\\G.{3})"); // split every 3 chars
                Log.consoleLog(ifr, "cleaned (" + cleaned + "): parts " + parts.length);
                int maxValue = Integer.MIN_VALUE;
                Map<Integer, Integer> countMap = new HashMap<>();

                for (String part1 : parts) {
                    String part = part1.trim();
                    try {
                        int value = Integer.parseInt(part);
                        Integer count = countMap.get(value);
                        countMap.put(value, (count == null) ? 1 : count + 1);
                        if (value > maxValue) {
                            maxValue = value;
                        }
                    } catch (NumberFormatException e) {
                        Log.consoleLog(ifr, "Invalid number in " + bureau + ": " + part);
                    }
                }

                String rangeKey = bcc.getRangeKey(maxValue);
                int countInRange = bcc.getCountInRange(countMap, rangeKey);

                List<String> values = new ArrayList<>();
                values.add(String.valueOf(maxValue));
                values.add(String.valueOf(countInRange));

                bureauStatsMap.put(bureau, values);

            } catch (Exception e) {
                Log.consoleLog(ifr, "Error processing bureau " + bureau + ": " + e.getMessage());

            }
        }

        return bureauStatsMap;
    }

    public Map<String, String> getMaxOverdue(IFormReference ifr, Map<String, List<String>> bureauStatsMap) {
        String overduedays_inp = "0";
        String overduedaysupto = "0";
        Map<String, String> cicoverdue = new HashMap<>();
        try {
            String maxBureau = null;
            int maxOverdue = Integer.MIN_VALUE;

            for (Map.Entry<String, List<String>> entry : bureauStatsMap.entrySet()) {
                String bureauType = entry.getKey();
                List<String> stats = entry.getValue();

                int overdueValue = Integer.parseInt(stats.get(0));
                int countValue = Integer.parseInt(stats.get(1));

                Log.consoleLog(ifr, "BureauType: " + bureauType + " Overdue: " + overdueValue + " Count: " + countValue);

                if (overdueValue > maxOverdue) {
                    maxOverdue = overdueValue;
                    maxBureau = bureauType;
                    overduedays_inp = String.valueOf(overdueValue);
                    overduedaysupto = String.valueOf(countValue);
                }
            }

            Log.consoleLog(ifr, "Selected Bureau: " + maxBureau + ", OverdueDays: " + overduedays_inp + ", Upto: " + overduedaysupto);
            cicoverdue.put("overduedaysupto", overduedaysupto);

            cicoverdue.put("overduedaysinp", overduedays_inp);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error parsing overdue values: " + e.getMessage());
        }
        Log.consoleLog(ifr, "cicoverdue:=> " + cicoverdue);

        return cicoverdue;
    }

    public Map<String, String> getCICHistory(IFormReference ifr, String applicanttype) {
        Map<String, String> cichistory = new HashMap<>();
        try {
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Map<String, List<String>> bureauStatsMap = calculateOverDue(ifr, ProcessInstanceId, applicanttype);
            //String result = getMaxOverdue(ifr, bureauStatsMap);
            cichistory = getMaxOverdue(ifr, bureauStatsMap);
            Log.consoleLog(ifr, "overduedays_inp Form CIC :::: overduedaysinp " + cichistory);
            LinkedHashMap<String, String> BureauDataResponseBorrowerNPAWriteoff = getMaxNpaWriteoff(ifr, applicanttype);

            cichistory.put("bscore", BureauDataResponseBorrowerNPAWriteoff.get("bscore"));
            cichistory.put("monthlydeduction", BureauDataResponseBorrowerNPAWriteoff.get("monthlydeduction"));
            cichistory.put("guarantorsettledhist_inp", BureauDataResponseBorrowerNPAWriteoff.get("guarantorsettledhist_inp"));
            cichistory.put("guarantornpa_inp", BureauDataResponseBorrowerNPAWriteoff.get("guarantornpa_inp"));
            cichistory.put("guarantorwriteoff", BureauDataResponseBorrowerNPAWriteoff.get("guarantorwriteoff"));
            cichistory.put("nparestmonths_inpo", BureauDataResponseBorrowerNPAWriteoff.get("nparestmonths_inpo"));
            cichistory.put("settledhist_inp", BureauDataResponseBorrowerNPAWriteoff.get("settledhist_inp"));
            cichistory.put("npa_inp", BureauDataResponseBorrowerNPAWriteoff.get("npa_inp"));
            cichistory.put("writeOff", BureauDataResponseBorrowerNPAWriteoff.get("writeOff"));

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error parsing overdue values: " + e.getMessage());
            //  return RLOS_Constants.ERROR;
        }
        return cichistory;
    }

    public LinkedHashMap<String, String> getMaxNpaWriteoff(IFormReference ifr, String ApplicantType) {
        Log.consoleLog(ifr, "#getMaxCICScoreDatas===>");
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();
        try {
            String[] whereCICValues = ConfProperty.getCommonPropertyValue("WHERECICVALUES").split(",");

            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String additionalCondition = "";
            if (ApplicantType.contains("~")) {
                additionalCondition = " AND " + whereCICValues[0] + "= '" + ApplicantType.split("~")[1] + "'";
                ApplicantType = ApplicantType.split("~")[0];
                Log.consoleLog(ifr, "#getMaxCICScoreDatas===> inside if " + ApplicantType);

            }
            String bureauTypeCondition = "";
            String[] bureauTypes = bureauTypes(ifr, processInstanceId, ApplicantType);
            if (bureauTypes.length > 0) {
                StringBuilder typeBuilder = new StringBuilder(" AND " + whereCICValues[1] + " IN (");
                for (int i = 0; i < bureauTypes.length; i++) {
                    typeBuilder.append("'").append(bureauTypes[i]).append("'");
                    if (i < bureauTypes.length - 1) {
                        typeBuilder.append(",");
                    }
                }
                typeBuilder.append(")");
                bureauTypeCondition = typeBuilder.toString();
            }
            Log.consoleLog(ifr, "ApplicantType===>" + ApplicantType);
            Log.consoleLog(ifr, "additionalCondition===>" + additionalCondition);
            String qry = ConfProperty.getQueryScript("GETCICHISTORY").replace("#PROCESSINSTANCEID#", processInstanceId).replace("#APPLICANT_TYPE#", ApplicantType) + additionalCondition + " " + bureauTypeCondition;
            List<List<String>> Result = cf.mExecuteQuery(ifr, qry, "Execute query for fetching ");

            Log.consoleLog(ifr, "#Result===>" + Result.toString());

            if (!Result.isEmpty()) {

                resultMap.put("bscore", Result.get(0).get(6));
                resultMap.put("monthlydeduction", Result.get(0).get(7));
                resultMap.put("guarantorsettledhist_inp", Result.get(0).get(0));
                resultMap.put("guarantornpa_inp", Result.get(0).get(1));
                resultMap.put("guarantorwriteoff", Result.get(0).get(0));
                resultMap.put("nparestmonths_inpo", Result.get(0).get(2));
                resultMap.put("settledhist_inp", Result.get(0).get(3));
                resultMap.put("npa_inp", Result.get(0).get(4));
                resultMap.put("writeOff", Result.get(0).get(5));

                Log.consoleLog(ifr, "#resultMapCIC ===>" + resultMap);
                return resultMap;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "getMaxNpaWriteoff:: Exception==>: " + e);
            //  return RLOS_Constants.ERROR;
        }
        return resultMap;

    }

    public LinkedHashMap<String, String> checkNpa_CB(LinkedHashMap<String, String> inputTags_CB,
            LinkedHashMap<String, String> npaResult_CB, IFormReference ifr, String months) {
        Log.consoleLog(ifr, "Started checkNpa_CB input tags #####==>" + inputTags_CB);

        int monthDifference = getMonthDifference(inputTags_CB.get("paymentStartDate"),
                inputTags_CB.get("paymentEndDate"), ifr);
        Log.consoleLog(ifr, "monthDifference:***** " + monthDifference);

        try {
            String npa = "No";
            String monthYear = "";
            String moreThan5YearCB = "No";
            String paymentHistory = inputTags_CB.get("paymentHistory");

            int charLimit = Math.abs(monthDifference * 3);
            Log.consoleLog(ifr, "charLimit:***** " + charLimit);
            Log.consoleLog(ifr, "paymentHistory.length():***** " + paymentHistory.length());

            if (paymentHistory != null && paymentHistory.length() > charLimit) {
                paymentHistory = paymentHistory.substring(0, charLimit);
            }
            npaResult_CB.put("paymentHistory", paymentHistory);
            Log.consoleLog(ifr, "Payment History after trim: " + paymentHistory);

            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(inputTags_CB.get("paymentStartDate")));

            int historyLength = paymentHistory != null ? paymentHistory.length() : 0;
            int count = historyLength / 3;

            boolean isNpaFound = false;

            for (int i = 0; i < count; i++) {
                int start = i * 3;
                int end = start + 3;
                if (end > paymentHistory.length()) {
                    break;
                }
                String chunk = paymentHistory.substring(start, end);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yyyy");
                monthYear = monthFormat.format(cal.getTime()).toUpperCase();

                if (chunk.contains("SUB") || chunk.contains("LSS") || chunk.contains("DBT")) {
                    npa = "Yes";
                    isNpaFound = true;
                    break;
                }

                cal.add(Calendar.MONTH, -1);
            }

            if (!isNpaFound) {
                npa = "No";
            }

            if (isNpaFound) {
                Log.consoleLog(ifr, "inside isNpaFound : " + isNpaFound);
                SimpleDateFormat sdfStart = new SimpleDateFormat("ddMMyyyy");
                Date startDate = sdfStart.parse(inputTags_CB.get("paymentStartDate"));
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);

                Calendar sixtyMonthsAgo = Calendar.getInstance();
                int month = Integer.parseInt(months);
                sixtyMonthsAgo.add(Calendar.MONTH, -month);

                Log.consoleLog(ifr, "Payment Start Date: " + startCal.getTime());
                Log.consoleLog(ifr, "Date 60 months ago: " + sixtyMonthsAgo.getTime());

                if (startCal.before(sixtyMonthsAgo)) {
                    moreThan5YearCB = "Yes";
                    npa = "No";
                    Log.consoleLog(ifr, "moreThan5YearCB = Yes");
                } else {
                    moreThan5YearCB = "No";
                    Log.consoleLog(ifr, "moreThan5YearCB = No");
                }
            }

            npaResult_CB.put("npa", npa);
            npaResult_CB.put("npaMonth", monthYear);
            npaResult_CB.put("moreThan5YearNpa", moreThan5YearCB);

            npaResult_CB.put("AccountHolder", inputTags_CB.get("ownershipIndicator"));
            npaResult_CB.put("accountType", inputTags_CB.get("accountType"));
            npaResult_CB.put("monthDifference", String.valueOf(monthDifference));
            npaResult_CB.put("dateOfAddition", inputTags_CB.get("paymentStartDate"));
            npaResult_CB.put("paymentEndDate", inputTags_CB.get("paymentEndDate"));
            npaResult_CB.put("applicantType", inputTags_CB.get("applicantType"));
            npaResult_CB.put("accountNumber", inputTags_CB.get("accountNumber"));

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in checkNpa_CB ==> " + e);
        }
        return npaResult_CB;
    }

    public LinkedHashMap<String, String> processwriteOffFromaccountStatus(String dateReported, String accountStatus,
            String writtenOffSettledStatus, LinkedHashMap<String, String> highmarkDPD, String months, IFormReference ifr) {

        LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();

        Log.consoleLog(ifr, "Starting processwriteOffFromaccountStatus");

        try {
            Log.consoleLog(ifr, "Input values - dateReported: " + dateReported + ", accountStatus: " + accountStatus
                    + ", writtenOffSettledStatus: " + writtenOffSettledStatus);

            if (accountStatus == null || writtenOffSettledStatus == null || dateReported == null) {
                Log.consoleLog(ifr, "One or more input values are null, returning empty resultMap");
                return resultMap;
            }

            // Constants
            //String wStatusFromConstant = "Written-off,Written Off and Account,Account Purchased and Written Off,Written Off and Account Sold";
            // String settledStatusFromConstant = "Account Purchased and Settled,Auctioned & Settled,Auctioned and Settled,Settled,Post (WO) Settled,Repossessed & Settled,Repossessed and Settled";
            // String restructuredStatusFromConstant = "Restructured";
            String settledStatusFromConstant = pcm.getConstantValue(ifr, "HIGHMARK", "SETTLED_STATUS");
            String wStatusFromConstant = pcm.getConstantValue(ifr, "HIGHMARK", "WRITTEN-OFF_STATUS");
            String restructuredStatusFromConstant = pcm.getConstantValue(ifr, "HIGHMARK", "RESTRUCTURED");

            Log.consoleLog(ifr, "Defined status constants");

            // Default values
            String writtenOff = "No";
            String settled = "No";
            String restructured = "No";
            String wroffMorethan5Year = "No";
            String settledMorethan5Year = "No";
            String reStructuredMorethan5Year = "No";

            // Date parsing
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date reportedDate = null;
            try {
                reportedDate = sdf.parse(dateReported);
                Log.consoleLog(ifr, "Parsed reported date: " + reportedDate);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Invalid date format for dateReported: " + dateReported);
                return resultMap;
            }

            Calendar reportedCal = Calendar.getInstance();
            reportedCal.setTime(reportedDate);

            Calendar sixtyMonthsAgo = Calendar.getInstance();
            sixtyMonthsAgo.add(Calendar.MONTH, -Integer.parseInt(months));

            boolean isMoreThan5Years = reportedCal.before(sixtyMonthsAgo);
            Log.consoleLog(ifr, "Is more than 5 years old: " + isMoreThan5Years);

            // Check statuses and assign flags
            if (containsIgnoreCase(wStatusFromConstant, writtenOffSettledStatus)) {
                Log.consoleLog(ifr, "Matched written-off status");
                if (isMoreThan5Years) {
                    wroffMorethan5Year = "Yes";
                } else {
                    writtenOff = "Yes";
                }
            } else if (containsIgnoreCase(settledStatusFromConstant, writtenOffSettledStatus)) {
                Log.consoleLog(ifr, "Matched settled status");
                if (isMoreThan5Years) {
                    settledMorethan5Year = "Yes";
                } else {
                    settled = "Yes";
                }
            } else if (containsIgnoreCase(restructuredStatusFromConstant, writtenOffSettledStatus)) {
                Log.consoleLog(ifr, "Matched restructured status");
                if (isMoreThan5Years) {
                    reStructuredMorethan5Year = "Yes";
                } else {
                    restructured = "Yes";
                }
            }

            // Populate result
            resultMap.put("writteOff", writtenOff);
            resultMap.put("settled", settled);
            resultMap.put("restructured", restructured);
            resultMap.put("wroffMorethan5Year", wroffMorethan5Year);
            resultMap.put("settledMorethan5Year", settledMorethan5Year);
            resultMap.put("reStructuredMorethan5Year", reStructuredMorethan5Year);

            Log.consoleLog(ifr, "Populated resultMap: " + resultMap.toString());

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error in processwriteOffFromaccountStatus: " + e);
        }

        Log.consoleLog(ifr, "Finished processwriteOffFromaccountStatus");

        return resultMap;
    }

    public LinkedHashMap<String, String>
            processPaymentHistory(String paymentHistory, LinkedHashMap<String, String> highmarkDPD,
                    String months_DPD, IFormReference ifr) {

        if (highmarkDPD.containsKey("dpdvalues")) {
            Log.consoleLog(ifr, "highmarkDPD.containsKey:::===>" + highmarkDPD);
            if (highmarkDPD.get("dpdvalues") != null) {
                highmarkDPD.remove("dpdvalues");
                Log.consoleLog(ifr, "highmarkDPD.remove:::===>" + highmarkDPD);
            }
        }

        String combinedHistory = "";
        int count = 0;

        try {
            String[] entries = paymentHistory.split("\\|");

            Calendar today = Calendar.getInstance();
            Calendar twelveMonthsAgo = (Calendar) today.clone();
            twelveMonthsAgo.add(Calendar.MONTH, -12); // make 12 is the constant

            Map<String, Integer> monthMap = getMonthMap();

            for (int i = 0; i < entries.length; i++) {
                String entry = entries[i];
                String[] monthYearValue = entry.split(":");
                if (monthYearValue.length < 2) {
                    continue;
                }

                String monthStr = monthYearValue[0];
                String[] yearAndValue = monthYearValue[1].split(",");
                if (yearAndValue.length < 2) {
                    continue;
                }

                int year = 0;
                int month = 0;

                try {
                    year = Integer.parseInt(yearAndValue[0]);
                    if (monthMap.containsKey(monthStr)) {
                        month = monthMap.get(monthStr);
                    } else {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }

                Calendar entryDate = Calendar.getInstance();
                entryDate.set(Calendar.YEAR, year);
                entryDate.set(Calendar.MONTH, month - 1); // 0-based
                entryDate.set(Calendar.DAY_OF_MONTH, 1);

                boolean isWithin12Months = !entryDate.before(twelveMonthsAgo) && !entryDate.after(today);

                if (isWithin12Months) {
                    String[] paymentParts = yearAndValue[1].split("/");
                    if (paymentParts.length > 0) {
                        combinedHistory += paymentParts[0];
                        count++;
                    }
                    //highmarkDPD.put("dpdCount", String.valueOf(count));
                    // combinedHistory = combinedHistory.replaceAll("[^a-zA-Z0-9]", "0");
                    combinedHistory = combinedHistory.replaceAll("(?i)XXX|DDD", "000");
                    Log.consoleLog(ifr, "combinedHistory$$===>" + combinedHistory);
                    highmarkDPD.put("dpdvalues", combinedHistory);

                }
            }
        } catch (Exception e) {
            System.out.println("Error while processing payment history: " + e.getMessage());
        }

        return highmarkDPD;

    }

    public Map<String, Integer> getMonthMap() {
        Map<String, Integer> monthMap = new HashMap<String, Integer>();
        monthMap.put("Jan", 1);
        monthMap.put("Feb", 2);
        monthMap.put("Mar", 3);
        monthMap.put("Apr", 4);
        monthMap.put("May", 5);
        monthMap.put("Jun", 6);
        monthMap.put("Jul", 7);
        monthMap.put("Aug", 8);
        monthMap.put("Sep", 9);
        monthMap.put("Oct", 10);
        monthMap.put("Nov", 11);
        monthMap.put("Dec", 12);
        return monthMap;
    }

    // dpd end ----------------------------
    // start NPA
    public LinkedHashMap<String, String> processNpaFromPaymentHistory(String paymentHistory, LinkedHashMap<String, String> highmarkDPD, String months, IFormReference ifr) {

        String lessThan5YearNpa = "No";
        String moreThan5YearNpa = "No";
        String npaMonth = "";

        Log.consoleLog(ifr, "Starting processNpaFromPaymentHistory");

        try {
            if (paymentHistory == null || paymentHistory.trim().isEmpty()) {
                Log.consoleLog(ifr, "Payment history is null or empty, returning unmodified highmarkDPD");
                return highmarkDPD;
            }

            Log.consoleLog(ifr, "Splitting payment history: " + paymentHistory);
            String[] entries = paymentHistory.split("\\|");

            Calendar today = Calendar.getInstance();
            Calendar sixtyMonthsAgo = (Calendar) today.clone();
            sixtyMonthsAgo.add(Calendar.MONTH, -Integer.parseInt(months));
            Log.consoleLog(ifr, "Sixty months ago date: " + sixtyMonthsAgo.getTime());

            Map<String, Integer> monthMap = getMonthMap();
            Log.consoleLog(ifr, "Month map retrieved");

            for (int i = 0; i < entries.length; i++) {
                String entry = entries[i];
                Log.consoleLog(ifr, "Processing entry: " + entry);

                String[] monthYearValue = entry.split(":");
                if (monthYearValue.length < 2) {
                    Log.consoleLog(ifr, "Skipping malformed entry: " + entry);
                    continue;
                }

                String monthStr = monthYearValue[0].trim();
                String[] yearAndValue = monthYearValue[1].split(",");
                if (yearAndValue.length < 2) {
                    Log.consoleLog(ifr, "Skipping incomplete year/value data: " + monthYearValue[1]);
                    continue;
                }

                int year = 0;
                int month = 0;
                try {
                    year = Integer.parseInt(yearAndValue[0].trim());
                    if (monthMap.containsKey(monthStr)) {
                        month = monthMap.get(monthStr);
                    } else {
                        Log.consoleLog(ifr, "Invalid month string: " + monthStr + ", skipping");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    Log.consoleLog(ifr, "Invalid year format: " + yearAndValue[0] + ", skipping");
                    continue;
                }

                Calendar entryDate = Calendar.getInstance();
                entryDate.set(Calendar.YEAR, year);
                entryDate.set(Calendar.MONTH, month - 1);
                entryDate.set(Calendar.DAY_OF_MONTH, 1);
                Log.consoleLog(ifr, "Constructed entry date: " + entryDate.getTime());

                String[] paymentParts = yearAndValue[1].split("/");
                if (paymentParts.length > 1) {
                    String status = paymentParts[1].trim().toUpperCase();
                    Log.consoleLog(ifr, "Payment status found: " + status);

                    if ("LOS".equals(status) || "DBT".equals(status) || "SUB".equals(status)) {
                        Log.consoleLog(ifr, "Matched NPA status: " + status);

                        // Find month name from month number
                        String monthName = "";
                        for (Map.Entry<String, Integer> e : monthMap.entrySet()) {
                            if (e.getValue() == month) {
                                monthName = e.getKey();
                                break;
                            }
                        }

                        npaMonth = monthName + "-" + year;
                        Log.consoleLog(ifr, "Detected NPA month: " + npaMonth);

                        if (!entryDate.before(sixtyMonthsAgo)) {
                            lessThan5YearNpa = "Yes";
                            Log.consoleLog(ifr, "NPA is less than 5 years old");
                        } else {
                            moreThan5YearNpa = "Yes";
                            Log.consoleLog(ifr, "NPA is more than 5 years old");
                        }

                        break;
                    }
                }
            }

            highmarkDPD.put("npa", lessThan5YearNpa);
            highmarkDPD.put("moreThan5YearNpa", moreThan5YearNpa);
            highmarkDPD.put("npaMonth", npaMonth);

            Log.consoleLog(ifr, "Final highmarkDPD after NPA processing: " + highmarkDPD.toString());

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error while processing NPA from payment history: " + e);
        }

        Log.consoleLog(ifr, "Finished processNpaFromPaymentHistory");

        return highmarkDPD;
    }

    public boolean containsIgnoreCase(String list, String value) {
        if (list == null || value == null) {
            return false;
        }

        String[] parts = list.split(",");
        String target = value.replaceAll("\\s+", "").toUpperCase();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].replaceAll("\\s+", "").toUpperCase();
            if (part.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public JSONObject formToJson_highMark(LinkedHashMap<String, String> inputMap, String apiName, IFormReference ifr,
            String processInstanceId, String applicantType) {
        JSONArray jsonOutput = new JSONArray();
        LinkedHashMap<String, String> insertMap = new LinkedHashMap();
        JSONObject jsonObject = new JSONObject();
        JSONObject blockData = new JSONObject();
        String blockName = "noOfAccountBlock1";

        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            blockData.put(entry.getKey(), entry.getValue());
            insertMap.put(entry.getKey(), entry.getValue());
        }

        jsonObject.put(blockName, blockData);
        jsonOutput.add(jsonObject);
        insertAllAccountBlocks(insertMap, apiName, processInstanceId, ifr, applicantType);

        Log.consoleLog(ifr, "finalOutput:::CRG HighMark::==>" + jsonOutput);

        JSONObject finalOutput = new JSONObject();
        finalOutput.put(apiName, jsonOutput);
        Log.consoleLog(ifr, "finalOutput:::CRG HighMark::==>" + finalOutput);

        return finalOutput;
    }

    public String processPaymentStatusEquifaxapi(LinkedHashMap<String, String> inputMap, LinkedHashMap<String, String> dpdMap, String months_DPD, IFormReference ifr) {
        Log.consoleLog(ifr, "finalOutput:::EQUIFAXAPI::==> processPaymentStatus");

        String yearKey = inputMap.get("dpdYear");
        String paymentStatus = inputMap.get("paymentHistory");

        Log.consoleLog(ifr, "@@@@finalOutput:::EQUIFAXAPI::==> paymentStatus: " + paymentStatus);

        if (yearKey == null || paymentStatus == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-yy");
            sdf.setLenient(false);
            Date entryDate = sdf.parse(yearKey);

            Calendar cal = Calendar.getInstance();
            int months = Integer.parseInt(months_DPD);
            cal.add(Calendar.MONTH, -months);
            Date cutoffDate = cal.getTime();

            Log.consoleLog(ifr, "Parsed entryDate: " + entryDate + ", Cutoff (months ago): " + cutoffDate);

            if (entryDate.before(cutoffDate)) {
                return "";
            }

            boolean isNumeric = paymentStatus.matches("\\d+");

            if (isNumeric) {
                return paymentStatus;
            } else {
                return pcm.getConstantValue(ifr, "EQUIFAXAPI", paymentStatus);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in processPaymentStatus: " + e);
        }

        return "";
    }

    public LinkedHashMap<String, String> processNPAStatusEquifaxapi(LinkedHashMap<String, String> inputMap,
            LinkedHashMap<String, String> Equifaxapi, String months, IFormReference ifr) {

        Log.consoleLog(ifr, "Starting processNPAStatusEquifaxapi with inputMap: " + inputMap);

        String classificationStatus = "";
        String lessThan5YearNpa = "No";
        String moreThan5YearNpa = "No";
        String npaMonth = "";
        if (inputMap.get("AssetClassificationStatus") != null) {
            classificationStatus = inputMap.get("AssetClassificationStatus").toString().trim();
        }

        Log.consoleLog(ifr, "Initial classificationStatus: " + classificationStatus);

        // Default "*" or empty to "STD"
        if ("*".equals(classificationStatus) || classificationStatus.isEmpty()) {
            classificationStatus = "STD";
            Log.consoleLog(ifr, "classificationStatus set to STD due to default condition");
        }

        String classificationDateStr = inputMap.get("dpdYear"); // Expected format: MM-yy
        if (classificationDateStr == null || classificationDateStr.trim().isEmpty()) {
            Log.consoleLog(ifr, "classificationDateStr is null or empty, returning highmarkDPD as is");
            return Equifaxapi;
        }

        Date classificationDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-yy");
            sdf.setLenient(false);
            classificationDate = sdf.parse(classificationDateStr);
            Log.consoleLog(ifr, "Parsed classificationDate: " + classificationDate);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception while parsing classificationDateStr: " + e.getMessage());
            return Equifaxapi;
        }

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();

        cal.add(Calendar.MONTH, -Integer.parseInt(months));
        Date sixtyMonthsAgo = cal.getTime();

        Log.consoleLog(ifr, "Today: " + today + ", Sixty months ago: " + sixtyMonthsAgo);

        boolean isWithinLast60Months = classificationDate.after(sixtyMonthsAgo);
        boolean isMoreThan60Months = classificationDate.before(sixtyMonthsAgo);

        if (!isWithinLast60Months && !isMoreThan60Months) {
            Log.consoleLog(ifr, "classificationDate is neither within last 60 months nor more than 60 months, returning highmarkDPD");
            return Equifaxapi;
        }

        String npaIndicators = "SUB, DBT, LOSS, SMA, SMA, SMA, SMA";
        boolean isNPA = false;
        String[] indicators = npaIndicators.split(", ");
        for (int i = 0; i < indicators.length; i++) {
            if (classificationStatus.toUpperCase().contains(indicators[i])) {
                isNPA = true;
                Log.consoleLog(ifr, "classificationStatus contains NPA indicator: " + indicators[i]);
                break;
            }
        }

        if (!isNPA) {
            Log.consoleLog(ifr, "classificationStatus does not contain any NPA indicators, returning highmarkDPD");
            return Equifaxapi;
        }

        if (isWithinLast60Months) {
            lessThan5YearNpa = "Yes";
            Log.consoleLog(ifr, "NPA classified as less than 5 years");
        } else if (isMoreThan60Months) {
            moreThan5YearNpa = "Yes";
            Log.consoleLog(ifr, "NPA classified as more than 5 years");
        }
        npaMonth = classificationDateStr;

        Equifaxapi.put("npaMonth", npaMonth);
        Equifaxapi.put("npa", lessThan5YearNpa);
        Equifaxapi.put("moreThan5YearNpa", moreThan5YearNpa);

        Log.consoleLog(ifr, "Returning highmarkDPD: " + Equifaxapi);
        return Equifaxapi;
    }

    public LinkedHashMap<String, String> processWRITEOFFStatusEquifaxapi(LinkedHashMap<String, String> inputMap,
            LinkedHashMap<String, String> Equifaxapi, String months, IFormReference ifr) {

        Log.consoleLog(ifr, "Starting processWRITEOFFStatusEquifaxapi with inputMap: " + inputMap);

        String accountStatus = "", statusDateStr = "";
        String writtenOff = "No";
        String wroffMorethan5Year = "No";
        String settled = "No";
        String settledMorethan5Year = "No";
        String restructured = "No";
        String restructuredMorethan5Year = "No";

        String writeOffIndicators = "WOF,PWOS";
        String settledIndicators = "SET";
        String restructredIndicators = "RES";

        if (inputMap.get("accountStatus") != null) {
            accountStatus = inputMap.get("accountStatus").toString().trim().toUpperCase();
        }

        Log.consoleLog(ifr, "accountStatus: " + accountStatus);

        if ("*".equals(accountStatus) || accountStatus.isEmpty()) {
            Log.consoleLog(ifr, "accountStatus is * or empty, returning Equifaxapi as is");
            return Equifaxapi;
        }

        statusDateStr = inputMap.get("dpdYear"); // Expected format: MM-yy
        if (statusDateStr == null || statusDateStr.trim().isEmpty()) {
            Log.consoleLog(ifr, "statusDateStr is null or empty, returning Equifaxapi as is");
            return Equifaxapi;
        }

        Date statusDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-yy");
            sdf.setLenient(false);
            statusDate = sdf.parse(statusDateStr);
            Log.consoleLog(ifr, "Parsed statusDate: " + statusDate);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception while parsing statusDateStr: " + e.getMessage());
            return Equifaxapi;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -Integer.parseInt(months));
        Date sixtyMonthsAgo = cal.getTime();

        Log.consoleLog(ifr, "Sixty months ago date: " + sixtyMonthsAgo);

        boolean isWithinLast60Months = statusDate.after(sixtyMonthsAgo);
        boolean isMoreThan60Months = statusDate.before(sixtyMonthsAgo);

        if (!isWithinLast60Months && !isMoreThan60Months) {
            Log.consoleLog(ifr, "statusDate is neither within last 60 months nor more than 60 months, returning Equifaxapi");
            return Equifaxapi;
        }
        String[] writeOffs = writeOffIndicators.split(",");
        for (String woff : writeOffs) {
            if (accountStatus.equalsIgnoreCase(woff)) {
                if (isWithinLast60Months) {
                    writtenOff = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches writeOff indicator within last 60 months: " + woff);
                } else if (isMoreThan60Months) {
                    wroffMorethan5Year = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches writeOff indicator more than 60 months ago: " + woff);
                }
                break;
            }
        }

        String[] setts = settledIndicators.split(",");
        for (String sett : setts) {
            if (accountStatus.equalsIgnoreCase(sett)) {
                if (isWithinLast60Months) {
                    settled = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches settled indicator within last 60 months: " + sett);
                } else if (isMoreThan60Months) {
                    settledMorethan5Year = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches settled indicator more than 60 months ago: " + sett);
                }
                break;
            }
        }

        String[] restru = restructredIndicators.split(",");
        for (String res : restru) {
            if (accountStatus.equalsIgnoreCase(res)) {
                if (isWithinLast60Months) {
                    restructured = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches restructured indicator within last 60 months: " + res);
                } else if (isMoreThan60Months) {
                    restructuredMorethan5Year = "Yes";
                    Log.consoleLog(ifr, "AccountStatus matches restructured indicator more than 60 months ago: " + res);
                }
                break;
            }
        }

        Equifaxapi.put("writteOff", writtenOff);
        Equifaxapi.put("wroffMorethan5Year", wroffMorethan5Year);
        Equifaxapi.put("settled", settled);
        Equifaxapi.put("settledMorethan5Year", settledMorethan5Year);
        Equifaxapi.put("restructured", restructured);
        Equifaxapi.put("reStructuredMorethan5Year", restructuredMorethan5Year);
        Equifaxapi.put("npaMonth", statusDateStr);

        Log.consoleLog(ifr, "Returning Equifaxapi map: " + Equifaxapi);
        return Equifaxapi;
    }

	public void crgGenEquifax(IFormReference ifr, String responseBody, String processInstanceId, String apiName,
        String applicantType, String insertionOrderid, boolean isNTC) {

    Log.consoleLog(ifr, "crgGenEquifax::::::===>" + "crgGenEquifax");
    String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
            replaceAll("#processInstanceId#", processInstanceId).replaceAll("#API_NAME#", apiName).
            replaceAll("#applicantType#", applicantType).replaceAll("#APPLICANT_UID#", insertionOrderid);
    Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
    ifr.saveDataInDB(trndelete);

    String AccountHolder = "";
    String score = "";
    String dpdpaymentHistory = "";
    String OwnershipType = "";
    String accountNumber = "";
    String accountType = "";
    String accountStatus = "";

    if (!responseBody.equalsIgnoreCase("{}")) {

        try {

            String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
            String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");
            String eqNpaConstant = pcm.getConstantValue(ifr, "EQUIFAXAPI", "NPA");
            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
            JSONObject body = (JSONObject) OutputJSON.get("body");

            JSONObject CCRResponse = (JSONObject) body.get("CCRResponse");

            JSONArray CIRReportDataLst = (JSONArray) CCRResponse.get("CIRReportDataLst");
            if (!isNTC) {

                if (CIRReportDataLst.size() > 0) {
                    JSONObject firstReportData = (JSONObject) CIRReportDataLst.get(0);

                    if (firstReportData.containsKey("Error")) {

                        JSONObject errorObj = (JSONObject) firstReportData.get("Error");
                        String errorCode = errorObj.get("ErrorCode").toString();
                        String errorDesc = errorObj.get("ErrorDesc").toString();

                        if (errorDesc.equalsIgnoreCase("Consumer not found in bureau")) {
                            score = "0";
                        }

                    }

                    for (int CIRReportDataLstCount = 0; CIRReportDataLstCount < CIRReportDataLst
                            .size(); CIRReportDataLstCount++) {
                        JSONObject CIRReportDataVal = (JSONObject) CIRReportDataLst.get(CIRReportDataLstCount);
                        if (CIRReportDataVal.containsKey("CIRReportData")) {
                            JSONObject CIRReportData = (JSONObject) CIRReportDataVal.get("CIRReportData");
                            JSONArray ScoreDetails = (JSONArray) CIRReportData.get("ScoreDetails");
                            JSONArray RetailAccountDetails = (JSONArray) CIRReportData.get("RetailAccountDetails");

                            for (int i = 0; i < RetailAccountDetails.size(); i++) {

                                JSONObject RetailAccountDetailsObj = (JSONObject) RetailAccountDetails.get(i);
                                String history48Months = RetailAccountDetailsObj.get("History48Months").toString();
                                JSONArray History48Months = (JSONArray) parser.parse(history48Months);
                                LinkedHashMap<String, String> dpdMap = new LinkedHashMap<String, String>();
                                if (dpdMap.containsKey("dpdvalues")) {
                                    Log.consoleLog(ifr, "Equifaxapi.containsKey:::===>" + dpdMap);
                                    if (dpdMap.get("dpdvalues") != null) {
                                        dpdMap.remove("dpdvalues");
                                        dpdpaymentHistory = "";
                                        Log.consoleLog(ifr, "Equifaxapi.remove:::===>" + dpdMap);
                                    }
                                }
                                for (int j = 0; j < History48Months.size(); j++) {
                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                    LinkedHashMap<String, String> input = new LinkedHashMap<String, String>();

                                    String key = History48MonthsObj.get("key").toString();
                                    String paymentHistory = History48MonthsObj.get("DaysPastDue").toString();
                                    OwnershipType = RetailAccountDetailsObj.get("OwnershipType").toString();
                                    accountNumber = RetailAccountDetailsObj.get("AccountNumber").toString();
                                    accountType = RetailAccountDetailsObj.get("AccountType").toString();
                                    accountType = pcm.getConstantValue(ifr, "EQUIFAXAPI", accountType);
                                    accountStatus = RetailAccountDetailsObj.get("AccountStatus").toString();
                                    String classificationStatus = "";
                                    if (History48MonthsObj.get("AssetClassificationStatus") != null) {
                                        classificationStatus = History48MonthsObj.get("AssetClassificationStatus")
                                                .toString().trim();
                                    }

                                    if ("*".equals(classificationStatus) || classificationStatus.isEmpty()) {
                                        classificationStatus = "STD";
                                    }
                                    input.put("AssetClassificationStatus", classificationStatus);
                                    input.put("paymentHistory", paymentHistory);
                                    input.put("dpdYear", key);
                                    input.put("accountStatus", accountStatus);

                                    /*
                 * if (OwnershipType.equalsIgnoreCase("Individual") ||
                 * OwnershipType.equalsIgnoreCase("Joint") ||
                 * OwnershipType.equalsIgnoreCase("Guarantor")) { dpdpaymentHistory +=
                 * processPaymentStatusEquifaxapi(input, dpdMap,months_DPD,ifr); dpdMap =
                 * processNPAStatusEquifaxapi(input, dpdMap,months,ifr,eqNpaConstant);
                 * dpdMap=processWRITEOFFStatusEquifaxapi(input, dpdMap,months,ifr);
                 * 
                 * }
                                     */
                                    if (OwnershipType.equalsIgnoreCase("Individual")
                                            || OwnershipType.equalsIgnoreCase("Joint")
                                            || OwnershipType.equalsIgnoreCase("Guarantor")) {

                                        // Set AccountHolder based on OwnershipType
                                        if (OwnershipType.equalsIgnoreCase("Individual")) {
                                            AccountHolder = "1";
                                        } else if (OwnershipType.equalsIgnoreCase("Joint")) {
                                            AccountHolder = "4";
                                        } else if (OwnershipType.equalsIgnoreCase("Guarantor")) {
                                            AccountHolder = "3";
                                        }

                                        // Proceed with processing
                                        dpdpaymentHistory += processPaymentStatusEquifaxapi(input, dpdMap, months_DPD, ifr);
                                        dpdMap = processNPAStatusEquifaxapi(input, dpdMap, months, ifr, eqNpaConstant);
                                        dpdMap = processWRITEOFFStatusEquifaxapi(input, dpdMap, months, ifr);
                                    }

                                    dpdMap.put("dpdvalues", dpdpaymentHistory);
                                }

                                dpdpaymentHistory = "";
                                dpdMap.put("AccountHolder", AccountHolder);
                                dpdMap.put("accountNumber", accountNumber);
                                dpdMap.put("accountType", accountType);
                                dpdMap.put("applicantType", applicantType);
                                dpdMap.put("applicant_uid", insertionOrderid);

                                formToJson_highMark(dpdMap, apiName, ifr, processInstanceId, applicantType);

                            }

                        }
                    }

                }
            } else {
                LinkedHashMap<String, String> dpdMap = new LinkedHashMap<String, String>();
                dpdMap.put("dpdvalues", "000");
                dpdMap.put("AccountHolder", "No");
                dpdMap.put("accountNumber", "No");
                dpdMap.put("accountType", "No");
                dpdMap.put("applicantType", applicantType);
                dpdMap.put("applicant_uid", insertionOrderid);

                dpdMap.put("npa", "No");
                dpdMap.put("moreThan5YearNpa", "No");
                dpdMap.put("writteOff", "No");
                dpdMap.put("wroffMorethan5Year", "No");
                dpdMap.put("settled", "No");
                dpdMap.put("settledMorethan5Year", "No");
                dpdMap.put("restructured", "No");
                dpdMap.put("reStructuredMorethan5Year", "No");
                dpdMap.put("npaMonth", "no");
                formToJson_highMark(dpdMap, apiName, ifr, processInstanceId, applicantType);

            }
            updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderid);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

	public LinkedHashMap<String, String> processNPAStatusEquifaxapi(
	        LinkedHashMap<String, String> inputMap,
	        LinkedHashMap<String, String> Equifaxapi,
	        String months,
	        IFormReference ifr,
	        String eqNpaConstant) {

	    Log.consoleLog(ifr, "Starting processNPAStatusEquifaxapi with inputMap: " + inputMap);

	    String classificationStatus = inputMap.getOrDefault("AssetClassificationStatus", "").trim();
	    if ("*".equals(classificationStatus) || classificationStatus.isEmpty()) {
	        classificationStatus = "STD"; // Default to STD
	        Log.consoleLog(ifr, "classificationStatus set to STD due to default condition");
	    }

	    String classificationDateStr = inputMap.get("dpdYear"); // Expected format: MM-yy
	    if (classificationDateStr == null || classificationDateStr.trim().isEmpty()) {
	        Log.consoleLog(ifr, "classificationDateStr is null or empty, returning as-is");
	        return Equifaxapi;
	    }

	    Date classificationDate;
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("MM-yy");
	        sdf.setLenient(false);
	        classificationDate = sdf.parse(classificationDateStr);
	        Log.consoleLog(ifr, "Parsed classificationDate: " + classificationDate);
	    } catch (Exception e) {
	        Log.consoleLog(ifr, "Failed to parse classificationDateStr: " + e.getMessage());
	        return Equifaxapi;
	    }

	    // Step 1: Check if classificationStatus is NPA
	    boolean isNPA = false;
	    String[] indicators = eqNpaConstant.split(",\\s*"); // Split by comma + optional space
	    for (String indicator : indicators) {
	        if (classificationStatus.toUpperCase().contains(indicator)) {
	            isNPA = true;
	            Log.consoleLog(ifr, "classificationStatus contains NPA indicator: " + indicator);
	            break;
	        }
	    }

	    // Step 2: If not NPA, return with no changes
	    if (!isNPA) {
	        Log.consoleLog(ifr, "classificationStatus does not contain NPA indicator. Skipping date check.");
	        Equifaxapi.put("npaMonth", classificationDateStr);
	        Equifaxapi.put("npa", "No");
	        Equifaxapi.put("moreThan5YearNpa", "No");
	        return Equifaxapi;
	    }

	    // Step 3: Compare classification date to 60 months ago
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.MONTH, -Integer.parseInt(months));
	    Date sixtyMonthsAgo = cal.getTime();

	    String lessThan5YearNpa = "No";
	    String moreThan5YearNpa = "No";

	    if (classificationDate.after(sixtyMonthsAgo)) {
	        lessThan5YearNpa = "Yes";
	        moreThan5YearNpa = "No";
	        Log.consoleLog(ifr, "NPA classified as within last 5 years");
	    } else if (classificationDate.before(sixtyMonthsAgo)) {
	        lessThan5YearNpa = "No";
	        moreThan5YearNpa = "Yes";
	        Log.consoleLog(ifr, "NPA classified as more than 5 years ago");
	    } else {
	        // Edge case: exact boundary date or undefined
	        lessThan5YearNpa = "No";
	        moreThan5YearNpa = "No";
	        Log.consoleLog(ifr, "NPA classificationDate does not fit into date window logic.");
	    }

	    // Step 4: Put values into output map
	    Equifaxapi.put("npaMonth", classificationDateStr);
	    Equifaxapi.put("npa", lessThan5YearNpa);
	    Equifaxapi.put("moreThan5YearNpa", moreThan5YearNpa);

	    Log.consoleLog(ifr, "Returning updated Equifaxapi map: " + Equifaxapi);
	    return Equifaxapi;
	}

	public void crgGenHighMark(IFormReference ifr, String responseBody, String processInstanceId, String apiName,
        String applicantType, String insertionOrderid, boolean noCIC) {

    String installmentAmount = "";

    String writtenOffSettledStatus = "";

    if (!noCIC) {
        if (!responseBody.equalsIgnoreCase("{}")) {
            List<LinkedHashMap<String, String>> finalMap_HM = new ArrayList<>();

            String months_DPD = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_DPD");
            String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");
            Log.consoleLog(ifr, "apiName apiName:==>####" + apiName);
            Log.consoleLog(ifr, "applicantType applicantType:==>####" + applicantType);
            Log.consoleLog(ifr, "applicantType applicantType:==>####" + insertionOrderid);
            try {

                //                String deleteQuery = "DELETE FROM LOS_TRN_CREDITHISTORY_DETAILS WHERE PID = '" + processInstanceId
                //                        + "' AND API_NAME = '" + apiName
                //                        + "' AND APPLICANTTYPE = '" + applicantType + "'"
                //                        + "' AND APPLICANT_UID = '" + insertionOrderid + "'";
                //                Log.consoleLog(ifr, "Delete Query:==>####" + deleteQuery);
                //                ifr.saveDataInDB(deleteQuery);
                String trndelete = ConfProperty.getQueryScript("DELETETRNCREDITHISTORY").
                        replaceAll("#processInstanceId#", processInstanceId).replaceAll("#API_NAME#", apiName).
                        replaceAll("#applicantType#", applicantType).replaceAll("#APPLICANT_UID#", insertionOrderid);
                Log.consoleLog(ifr, "Delete Query:==>####" + trndelete);
                ifr.saveDataInDB(trndelete);

                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);

                JSONObject body = (JSONObject) OutputJSON.get("body");
                JSONObject INDV_REPORT_FILE = (JSONObject) body.get("INDV-REPORT-FILE");
                JSONArray INDV_REPORTS = (JSONArray) INDV_REPORT_FILE.get("INDV-REPORTS");

                for (int i = 0; i < INDV_REPORTS.size(); i++) {
                    JSONObject INDVReportDataVal = (JSONObject) INDV_REPORTS.get(i);
                    JSONObject INDV_REPORT = (JSONObject) INDVReportDataVal.get("INDV-REPORT");
                    JSONArray responses = (JSONArray) INDV_REPORT.get("RESPONSES");

                    for (Object responseObj : responses) {
                        // Create a new map for each account
                        LinkedHashMap<String, String> highmarkDPD = new LinkedHashMap<String, String>();

                        JSONObject response = (JSONObject) responseObj;
                        JSONObject loanDetails = (JSONObject) response.get("LOAN-DETAILS");
                        if (loanDetails != null) {
                            String ownershipInd = loanDetails.get("OWNERSHIP-IND").toString();
                            String accountNumber = loanDetails.get("ACCT-NUMBER").toString();
                            String accType = loanDetails.get("ACCT-TYPE").toString();
                            accType = pcm.getConstantValue(ifr, "HIGHMARK", accType);
                            String accountStatus = loanDetails.get("ACCOUNT-STATUS").toString();

                            if (loanDetails.containsKey("INSTALLMENT-AMT")
                                    && loanDetails.get("INSTALLMENT-AMT") != null) {
                                installmentAmount = loanDetails.get("INSTALLMENT-AMT").toString();
                            }

                            String paymentHistory = loanDetails.get("COMBINED-PAYMENT-HISTORY").toString();
                            String suitfield = "";
                            if (loanDetails.containsKey("SUIT-FILED_WILFUL-DEFAULT")) {
                                suitfield = loanDetails.get("SUIT-FILED_WILFUL-DEFAULT").toString();
                            }
                            if ("Individual".equalsIgnoreCase(ownershipInd)
                                    || "Joint".equalsIgnoreCase(ownershipInd) || "Guarantor".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD = processPaymentHistory(paymentHistory, highmarkDPD, months_DPD, ifr);
                                highmarkDPD = processNpaFromPaymentHistory(paymentHistory, highmarkDPD, months, ifr);
                                if (loanDetails.containsKey("WRITTEN-OFF_SETTLED-STATUS")) {
                                    String dateReported = loanDetails.get("DATE-REPORTED").toString();
                                    writtenOffSettledStatus = loanDetails.get("WRITTEN-OFF_SETTLED-STATUS").toString();
                                    highmarkDPD = processwriteOffFromaccountStatus(dateReported, accountStatus,
                                            writtenOffSettledStatus, highmarkDPD, months, ifr);
                                }
                            }

                            // Set AccountHolder value
                            if ("Individual".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "1");
                            } else if ("Authorized User".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "2");
                            } else if ("Guarantor".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "3");
                            } else if ("Joint".equalsIgnoreCase(ownershipInd)) {
                                highmarkDPD.put("AccountHolder", "4");
                            } else {
                                highmarkDPD.put("AccountHolder", "5");
                            }

                            highmarkDPD.put("accountType", accType);
                            highmarkDPD.put("accountNumber", accountNumber);
                            highmarkDPD.put("suitfield", suitfield);

                            // Add to final map for each account
                            Log.consoleLog(ifr, "highmarkDPD==>highmarkDPD" + highmarkDPD);
                            highmarkDPD.put("applicantType", applicantType);
                            highmarkDPD.put("applicant_uid", insertionOrderid);
                            formToJson_highMark(highmarkDPD, apiName, ifr, processInstanceId, applicantType);
                            highmarkDPD.clear();

                        }
                    }
                }

                Log.consoleLog(ifr, "finalMap_HM==>finalMap_HM" + finalMap_HM);

                updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderid);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    } else {
        LinkedHashMap<String, String> highmarkDPD = new LinkedHashMap<String, String>();
        highmarkDPD.put("applicantType", applicantType);
        highmarkDPD.put("applicant_uid", insertionOrderid);
        highmarkDPD.put("accountType", "");
        highmarkDPD.put("accountNumber", "");
        highmarkDPD.put("suitfield", "");
        highmarkDPD.put("AccountHolder", "");
        highmarkDPD.put("dpdvalues", "000");
        highmarkDPD.put("npa", "No");
        highmarkDPD.put("moreThan5YearNpa", "No");
        highmarkDPD.put("npaMonth", "No");
        highmarkDPD.put("writteOff", "No");
        highmarkDPD.put("settled", "No");
        highmarkDPD.put("restructured", "No");
        highmarkDPD.put("wroffMorethan5Year", "No");
        highmarkDPD.put("settledMorethan5Year", "No");
        highmarkDPD.put("reStructuredMorethan5Year", "No");
        formToJson_highMark(highmarkDPD, apiName, ifr, processInstanceId, applicantType);
        updateCreditHistory(ifr, apiName, processInstanceId, applicantType, insertionOrderid);
    }

}
}

