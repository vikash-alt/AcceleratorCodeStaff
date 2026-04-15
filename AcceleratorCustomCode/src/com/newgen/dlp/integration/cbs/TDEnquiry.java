/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class TDEnquiry {

    // CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    // CCMCommonMethods apic = new CCMCommonMethods();

    public String getFDAccountEnquiryForOD(IFormReference ifr, String CustomerId) {

        String apiName = "FDAccountEnquiryOD";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {
            //   String APIName = "CBS_ODEnquiry";
//            String ErrorCode = "";
//            String ErrorMessage = "";
//            Log.consoleLog(ifr, "#getFDAccountEnquiryForOD Starting....");
//            String APIStatusSend = "";

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String ProdCode = pcm.getConstantValue(ifr, "CBSLADFDENQ", "PRCODE");
            String BankCode = pcm.getConstantValue(ifr, "CBSLADFDENQ", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSLADFDENQ", "CHANNEL");
            String S_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
            String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
            String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "LAD");

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"" + S_UserId + "\"\n"
                    + "            },\n"
                    + "            \"BankCode\": \"" + BankCode + "\",\n"
                    + "            \"Channel\": \"" + Channel + "\",\n"
                    + "            \"ExternalBatchNumber\": \"\",\n"
                    + "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "            \"LocalDateTimeText\": \"\",\n"
                    + "            \"OriginalReferenceNo\": \"\",\n"
                    + "            \"OverridenWarnings\": \"\",\n"
                    + "            \"PostingDateText\": \"20230519\",\n"
                    + "            \"ServiceCode\": \"\",\n"
                    + "            \"SessionTicket\": \"\",\n"
                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "            \"UserId\": \"" + UserId + "\",\n"
                    + "            \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"CustomerId\": \"" + CustomerId + "\",\n"
                    + "        \"oDProdCod\": \"" + ProdCode + "\"\n"
                    + "    }\n"
                    + "}";
            Log.consoleLog(ifr, "request sent to API" + request);

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                org.json.simple.JSONObject OutputJSON = (org.json.simple.JSONObject) parser.parse(response);
                org.json.simple.JSONObject resultObj = new org.json.simple.JSONObject(OutputJSON);
                String body = resultObj.get("body").toString();
                JSONObject bodyJSON = (JSONObject) parser.parse(body);
                JSONObject bodyObj = new JSONObject(bodyJSON);
                String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + checkError);
                if (checkError.equalsIgnoreCase("true")) {
                    String responseCode = resultObj.get("responseCode").toString();
                    Log.consoleLog(ifr, "responseCode :  " + responseCode);
                    String TDAcctInquiryResponse = bodyJSON.get("EligibleTDForODResponse").toString();
                    JSONObject TDAcctInquiryResponseJSON = (JSONObject) parser.parse(TDAcctInquiryResponse);
                    JSONObject TDAcctInquiryResponseJSONObj = new JSONObject(TDAcctInquiryResponseJSON);
                    String DepositDetails = TDAcctInquiryResponseJSONObj.get("TDAccountDetails").toString();

                    JSONArray DepositDetailsJSON = new JSONArray();
                    Object TDAccountDetails = TDAcctInquiryResponseJSONObj.get("TDAccountDetails");
                    if (TDAccountDetails instanceof JSONArray) {
                        DepositDetailsJSON = (JSONArray) parser.parse(DepositDetails);
                    } else {
                        DepositDetailsJSON.add(TDAccountDetails);
                    }
                    ifr.clearTable("P_LAD_IDC_FD1");
                    JSONArray jsonArr = new JSONArray();
                    if (DepositDetailsJSON.isEmpty()) {
                        pcm.returnErrorcustmessage(ifr, "Eligible deposits are not available");
                    } else {
                        for (int i = 0; i < DepositDetailsJSON.size(); i++) {
                            String individualDepsositDetails = DepositDetailsJSON.get(i).toString();
                            JSONObject individualDepsositDetailsJSON = (JSONObject) parser.parse(individualDepsositDetails);
                            JSONObject individualDepsositDetailsJSONObject = new JSONObject(individualDepsositDetailsJSON);
                            String FDNo = individualDepsositDetailsJSONObject.get("TDAccountNo").toString().trim();
                            String depositAmount = individualDepsositDetailsJSONObject.get("BalPrincipal").toString();
                            //float depositValue = Float.parseFloat(depositAmount);
                            //float EligibleValue = depositValue / 100 * 75;
                            //String eligibilityAmount = String.valueOf(EligibleValue);
                            String eligibilityAmount = individualDepsositDetailsJSONObject.get("MaximumEligibleAmount").toString();
                            Log.consoleLog(ifr, "eligibilityAmount :  " + eligibilityAmount);
                            String depositNumber = individualDepsositDetailsJSONObject.get("DepositNo").toString();;

                            String startDate = individualDepsositDetailsJSONObject.get("DepositDate").toString();
                            LocalDate date = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
                            startDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                            String MaturityDate = individualDepsositDetailsJSONObject.get("MaturityDate").toString();
                            LocalDate date1 = LocalDate.parse(MaturityDate, DateTimeFormatter.ISO_LOCAL_DATE);
                            MaturityDate = date1.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                            String tdno = individualDepsositDetailsJSONObject.get("DepositNo").toString().trim();
                            String ODRoi = individualDepsositDetailsJSONObject.get("ODRoi").toString();
                            String TDRoi = individualDepsositDetailsJSONObject.get("TDRoi").toString();
                            JSONObject jsonobj = new JSONObject();
                            jsonobj.put("Term Deposit No", FDNo);
                            // Added by prakash 30-03-2024  for round of 100
                            int depositAmount_conv = Integer.parseInt(depositAmount);
                            int roundedDepositAmount = Math.round(depositAmount_conv / 100) * 100;
                            depositAmount = Integer.toString(roundedDepositAmount);

                            int eligibilityAmount_conv = Integer.parseInt(eligibilityAmount);
                            int roundedEligibilityAmount = Math.round(eligibilityAmount_conv / 100) * 100;
                            eligibilityAmount = Integer.toString(roundedEligibilityAmount);
                            // end 
                            Log.consoleLog(ifr, "depositAmount :  " + depositAmount);
                            jsonobj.put("Deposit Amount", depositAmount);
                            jsonobj.put("Eligible Amount", eligibilityAmount);
                            jsonobj.put("Start Date", startDate);
                            jsonobj.put("Maturity Date", MaturityDate);
                            jsonobj.put("Rate of Interest", ODRoi);
                            jsonobj.put("Deposit No", tdno);
                            jsonobj.put("TD Rate of Interest", TDRoi);
                            jsonArr.add(jsonobj);
                        }

                        // Sort JSON array based on depositAmount in descending order
                        jsonArr.sort(new Comparator<JSONObject>() {
                            @Override
                            public int compare(JSONObject o1, JSONObject o2) {
                                // Get depositAmount values from JSON objects
                                int depositAmount1 = Integer.parseInt(o1.get("Deposit Amount").toString());
                                int depositAmount2 = Integer.parseInt(o2.get("Deposit Amount").toString());
                                // Compare and return in descending order
                                return depositAmount2 - depositAmount1;
                            }
                        });

                        Log.consoleLog(ifr, "jsonArr :  " + jsonArr.toString());
                        ifr.addDataToGrid("P_LAD_IDC_FD1", jsonArr);
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

            return apiStatus;

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//                APIStatusSend = RLOS_Constants.SUCCESS;
//            } else {
//                APIStatus = "FAIL";
//                APIStatusSend = RLOS_Constants.ERROR;
//            }
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
            Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
//        if (APIStatusSend.contains(RLOS_Constants.ERROR) || APIStatusSend.equalsIgnoreCase("")) {
//            return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//        }
        //Modified by Ahmed on 11-07-2024 for displaying the actual error message without data massaging
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;
        //return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);

    }
}
