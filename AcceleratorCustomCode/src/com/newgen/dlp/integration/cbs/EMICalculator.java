/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;

/**
 *
 * @author ahmed.zindha
 */
public class EMICalculator {

    //  CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    //CCMCommonMethods apic = new CCMCommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String getEmiCalculatorInstallment(IFormReference ifr, String ProcessInstanceId,
            String LoanAmount, String Tenure, String ROI, String FrameSection) {
        Log.consoleLog(ifr, "ExecuteCBS_EmiCalculatorInstallmentAPI : ");

        String apiName = "EMICalculator";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

            String EMIAmount = "";

            String BankCode = pcm.getConstantValue(ifr, "CBSEMICALC", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSEMICALC", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CBSEMICALC", "USERID");
            String TBranch = pcm.getConstantValue(ifr, "CBSEMICALC", "TBRANCH");
            String CALCMODE = pcm.getConstantValue(ifr, "CBSEMICALC", "CALCMODE");

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                \"UserId\": \"\"\n"
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
                    + "            \"UserReferenceNumber\": \"\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"LoanAmount\": \"" + LoanAmount + "\",\n"
                    + "        \"InterestRate\": \"" + ROI + "\",\n"
                    + "        \"CalculatorMode\": \"" + CALCMODE + "\",\n"
                    + "        \"TermYears\": \"\",\n"
                    + "        \"TermMonths\": \"" + Tenure + "\",\n"
                    + "        \"InstallmentAmount\": \"\",\n"
                    + "        \"ExtUniqueRefId\": \"" + cm.getCBSExternalReferenceNo() + "\"\n"
                    + "    }\n"
                    + "}";

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            //System.out.println("Text==>" + Text);
//            String ErrorCode = "";
//            String ErrorMessage = "";
            String RoundedAmount = "";
            if (!response.equalsIgnoreCase("{}")) {

                JSONParser parser = new JSONParser();
                JSONObject TextJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(TextJSON);

                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body :  " + body);

                JSONObject bodyJSON = (JSONObject) parser.parse(body);
                JSONObject bodyJSONObj = new JSONObject(bodyJSON);
                JSONObject bodyObj = new JSONObject(bodyJSON);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                if (CheckError.equalsIgnoreCase("true")) {
                    String LoanCalculatorResponse = bodyJSONObj.get("LoanCalculatorResponse").toString();
                    Log.consoleLog(ifr, "LoanCalculatorResponse :  " + LoanCalculatorResponse);

                    JSONObject LoanCalculatorResponseJSON = (JSONObject) parser.parse(LoanCalculatorResponse);
                    JSONObject LoanCalculatorResponseJSONObj = new JSONObject(LoanCalculatorResponseJSON);

                    EMIAmount = LoanCalculatorResponseJSONObj.get("InstallmentAmount").toString();
                    Log.consoleLog(ifr, "EMIAmount :  " + EMIAmount);
                    RoundedAmount = pcm.mAccRoundOffvalue(ifr, EMIAmount);
                    Log.consoleLog(ifr, "Rounded EMIAmount :  " + RoundedAmount);

                } else {
                    String[] ErrorData = CheckError.split("#");
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

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                if (RoundedAmount.equalsIgnoreCase("")) {
                    RoundedAmount = "0";
                }

                if ((!RoundedAmount.equalsIgnoreCase("0")) || (!RoundedAmount.equalsIgnoreCase("0.0"))) {

                    String Query = "INSERT INTO LOS_CBS_EMICALC_HISTORY(PID,SCREEN_NAME,LOAN_AMOUNT,"
                            + "LOAN_TENURE,LOAN_ROI,LOAN_EMI) "
                            + "VALUES('" + ProcessInstanceId + "',"
                            + "'" + FrameSection + "',"
                            + "'" + LoanAmount + "',"
                            + "'" + Tenure + "',"
                            + "'" + ROI + "',"
                            + "'" + RoundedAmount + "')";
                    Log.consoleLog(ifr, "INSERT Query :  " + Query);
                    ifr.saveDataInDB(Query);

                }
                return RoundedAmount;
            }

//            String APIName = "CBS_EMICalculator";
//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
//
//            if (APIStatus.equalsIgnoreCase("FAIL")) {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, APIName, ErrorCode);
//            } else {
//                if (RoundedAmount.equalsIgnoreCase("")) {
//                    RoundedAmount = "0";
//                }
//
//                if ((!RoundedAmount.equalsIgnoreCase("0")) || (!RoundedAmount.equalsIgnoreCase("0.0"))) {
//
//                    String Query = "INSERT INTO LOS_CBS_EMICALC_HISTORY(PID,SCREEN_NAME,LOAN_AMOUNT,"
//                            + "LOAN_TENURE,LOAN_ROI,LOAN_EMI) "
//                            + "VALUES('" + ProcessInstanceId + "',"
//                            + "'" + FrameSection + "',"
//                            + "'" + LoanAmount + "',"
//                            + "'" + Tenure + "',"
//                            + "'" + ROI + "',"
//                            + "'" + RoundedAmount + "')";
//                    Log.consoleLog(ifr, "INSERT Query :  " + Query);
//                    ifr.saveDataInDB(Query);
//
//                }
//            }
//
//            return RoundedAmount;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return "0";

    }
}
