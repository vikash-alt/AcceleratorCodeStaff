/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.portalAcceleratorCode;

import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author vinay.s
 */
public class APIIntegrationPortal {

    CommonFunctionality cf = new CommonFunctionality();

    public String emailNotificationAPI(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "emailNotificationAPI");
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("bcc", new String[]{"string"});
        jsonRequest.put("cc", new String[]{"string"});
        jsonRequest.put("fileContent", "string");
        jsonRequest.put("fileName", "string");
        jsonRequest.put("msgBody", "string");
        jsonRequest.put("subject", "string");
        jsonRequest.put("toEmail", "string");
        String jsonString = jsonRequest.toString();
        try {
            String ServiceName = "EMAILNOTIFICATION";
            HashMap<String, String> requestHeader = new HashMap<String, String>();
            String response = cf.CallWebService(ifr, ServiceName, jsonString, "", requestHeader);
            Log.consoleLog(ifr, "response :  " + response);
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response);
            String responseMessage = cf.getJsonValue(obj, "message : ");
            Log.consoleLog(ifr, "responseMessage : " + responseMessage);
            String responseCode = cf.getJsonValue(obj, "responseCode");
            Log.consoleLog(ifr, "responseCode " + responseCode);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  emailNotificationAPI" + e);
        }

        return "";
    }

    public String smsNotificationAPI(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "smsNotificationAPI : ");
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("dest", "string");
        jsonRequest.put("msg", "string");
        jsonRequest.put("pwd", "string");
        jsonRequest.put("uname", "string");
        String jsonString = jsonRequest.toString();
        try {
            String ServiceName = "SMSNOTIFICATION";
            HashMap<String, String> requestHeader = new HashMap<String, String>();
            String response = cf.CallWebService(ifr, ServiceName, jsonString, "", requestHeader);
            Log.consoleLog(ifr, "response :  " + response);
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response);
            String responseMessage = cf.getJsonValue(obj, "message : ");
            Log.consoleLog(ifr, "responseMessage : " + responseMessage);
            String responseCode = cf.getJsonValue(obj, "responseCode");
            Log.consoleLog(ifr, "responseCode " + responseCode);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  smsNotificationAPI" + e);
        }

        return "";
    }

    public String emiCalculatorInstallmentAPI(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "emiCalculatorInstallmentAPI : ");
        JSONObject requestJson = new JSONObject();

        JSONObject inputJson = new JSONObject();
        inputJson.put("CalculatorMode", "string");
        inputJson.put("ExtUniqueRefId", "string");
        inputJson.put("InstallmentAmount", "string");
        inputJson.put("InterestRate", "string");
        inputJson.put("LoanAmount", "string");

        JSONObject sessionContextJson = new JSONObject();
        sessionContextJson.put("BankCode", "string");
        sessionContextJson.put("Channel", "string");
        sessionContextJson.put("ExternalBatchNumber", "string");
        sessionContextJson.put("ExternalReferenceNo", "string");
        sessionContextJson.put("ExternalSystemAuditTrailNumber", "string");
        sessionContextJson.put("LocalDateTimeText", "string");
        sessionContextJson.put("OriginalReferenceNo", "string");
        sessionContextJson.put("OverridenWarnings", "string");
        sessionContextJson.put("PostingDateText", "string");
        sessionContextJson.put("ServiceCode", "string");
        sessionContextJson.put("SessionTicket", "string");

        JSONObject supervisorContextJson = new JSONObject();
        supervisorContextJson.put("PrimaryPassword", "string");
        supervisorContextJson.put("UserId", "string");

        sessionContextJson.put("SupervisorContext", supervisorContextJson);

        sessionContextJson.put("TransactionBranch", "string");
        sessionContextJson.put("UserId", "string");
        sessionContextJson.put("UserReferenceNumber", "string");
        sessionContextJson.put("ValueDateText", "string");

        inputJson.put("SessionContext", sessionContextJson);

        inputJson.put("TermMonths", "string");
        inputJson.put("TermYears", "string");

        requestJson.put("input", inputJson);

        String jsonString = requestJson.toString();

        try {
            String ServiceName = "EMICalculator";
            HashMap<String, String> requestHeader = new HashMap<String, String>();
            String response = cf.CallWebService(ifr, ServiceName, jsonString, "", requestHeader);
            Log.consoleLog(ifr, "response :  " + response);
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response);
            String responseMessage = cf.getJsonValue(obj, "message : ");
            Log.consoleLog(ifr, "responseMessage : " + responseMessage);
            String responseCode = cf.getJsonValue(obj, "responseCode");
            Log.consoleLog(ifr, "responseCode " + responseCode);
            String insertQuery = "insert name ";
            Log.consoleLog(ifr, "insertQuery " + insertQuery);
            int result = ifr.saveDataInDB(insertQuery);
            Log.consoleLog(ifr, "result " + result);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  emiCalculatorInstallmentAPI" + e);
        }

        return "";
    }

    /*public static void main(String[] args) {
        JSONObject requestJson = new JSONObject();

        JSONObject inputJson = new JSONObject();
        inputJson.put("CalculatorMode", "string");
        inputJson.put("ExtUniqueRefId", "string");
        inputJson.put("InstallmentAmount", "string");
        inputJson.put("InterestRate", "string");
        inputJson.put("LoanAmount", "string");

        JSONObject sessionContextJson = new JSONObject();
        sessionContextJson.put("BankCode", "string");
        sessionContextJson.put("Channel", "string");
        sessionContextJson.put("ExternalBatchNumber", "string");
        sessionContextJson.put("ExternalReferenceNo", "string");
        sessionContextJson.put("ExternalSystemAuditTrailNumber", "string");
        sessionContextJson.put("LocalDateTimeText", "string");
        sessionContextJson.put("OriginalReferenceNo", "string");
        sessionContextJson.put("OverridenWarnings", "string");
        sessionContextJson.put("PostingDateText", "string");
        sessionContextJson.put("ServiceCode", "string");
        sessionContextJson.put("SessionTicket", "string");

        JSONObject supervisorContextJson = new JSONObject();
        supervisorContextJson.put("PrimaryPassword", "string");
        supervisorContextJson.put("UserId", "string");

        sessionContextJson.put("SupervisorContext", supervisorContextJson);

        sessionContextJson.put("TransactionBranch", "string");
        sessionContextJson.put("UserId", "string");
        sessionContextJson.put("UserReferenceNumber", "string");
        sessionContextJson.put("ValueDateText", "string");

        inputJson.put("SessionContext", sessionContextJson);

        inputJson.put("TermMonths", "string");
        inputJson.put("TermYears", "string");

        requestJson.put("input", inputJson);

        System.out.println(requestJson + "11");
    }*/
}
