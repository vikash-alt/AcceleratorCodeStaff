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
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.properties.ConfProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class CIM09 {

    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();

    public String updateCustMisValue(IFormReference ifr, String CustomerID,
            String codMisCustCode1, String codMisCustCode2, String codMisCustCode3, String codMisCustCode4,
            String codMisCustCode5, String codMisCustCode6, String codMisCustCode7, String codMisCustCode8,
            String codMisCustCode9, String codMisCustCode10) {
        Log.consoleLog(ifr, "Entered into CustomerInformationMaster...");

        String apiName = "CIM09";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {
//        String ErrorCode = "";
//        String ErrorMessage = "";
//        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

            String BankCode = pcm.getConstantValue(ifr, "CIM09", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CIM09", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CIM09", "USERID");
            String UserId_S = pcm.getConstantValue(ifr, "CIM09", "S_USERID");
            String TBranch = pcm.getConstantValue(ifr, "CIM09", "TBRANCH");
            String datSuperannuation = pcm.getDateSuperannuationCIM09(ifr);
            //Modified by Aravindh For Date Superannuation Updation

            request = "{\n"
                    + "    \"input\": {\n"
                    + "        \"SessionContext\": {\n"
                    + "            \"SupervisorContext\": {\n"
                    + "                \"PrimaryPassword\": \"\",\n"
                    + "                   \"UserId\": \"" + UserId_S + "\"\n"
                    + "            },\n"
                    + "           \"BankCode\": \"" + BankCode + "\",\n"
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
                    + "          \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "             \"UserId\": \"" + UserId + "\",\n"
                    + "            \"UserReferenceNumber\": \"\",\n"
                    + "            \"ValueDateText\": \"\"\n"
                    + "        },\n"
                    + "        \"codCustId\": \"" + CustomerID + "\",\n"
                    + "            \"corpIdenNo\": \"\",\n"
                    + "            \"businessType\": \"\",\n"
                    + "            \"datSuperannuation\": \"" + datSuperannuation + "\",\n"
                    + "        \"codMisCustCode1\": \"" + codMisCustCode1 + "\",\n"
                    + "        \"codMisCustCode2\": \"" + codMisCustCode2 + "\",\n"
                    + "        \"codMisCustCode3\": \"" + codMisCustCode3 + "\",\n"
                    + "        \"codMisCustCode4\": \"" + codMisCustCode4 + "\",\n"
                    + "        \"codMisCustCode5\": \"" + codMisCustCode5 + "\",\n"
                    + "        \"codMisCustCode6\": \"" + codMisCustCode6 + "\",\n"
                    + "        \"codMisCustCode7\": \"" + codMisCustCode7 + "\",\n"
                    + "        \"codMisCustCode8\": \"" + codMisCustCode8 + "\",\n"
                    + "        \"codMisCustCode9\": \"" + codMisCustCode9 + "\",\n"
                    + "        \"codMisCustCode10\": \"" + codMisCustCode10 + "\",\n"
                    + "        \"codMisCompCode1\": \"NA\"\n"
                    + "    }\n"
                    + "}";

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(response);
                // JSONObject resultObj = new JSONObject(OutputJSON);
                String body = resultObj.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser.parse(body);
                String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
                if (CheckError.equalsIgnoreCase("true")) {
                    String CustMISMntResponse = bodyObj.get("CustMISMntResponse").toString();
                    JSONObject CustMISMntResponseJSON = (JSONObject) parser.parse(CustMISMntResponse);
                    String CodCustId = cf.getJsonValue(CustMISMntResponseJSON, "CodCustId");
                    Log.consoleLog(ifr, "CodCustId : " + CodCustId);
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
                return apiStatus;
            }

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = RLOS_Constants.SUCCESS;
//            } else {
//                APIStatus = "FAIL";
//            }
//            Log.consoleLog(ifr, "CaptureRequestResponse Calling ");
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//            if (!(APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            return "";
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception/CBS_Demographic===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Demographic===>" + e);

        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
    }

    public String updateMIS(IFormReference ifr) {
        String CustomerId = pcm.getCustomerIDCB(ifr, "B");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String codMisCustCode1 = "";
        String codMisCustCode2 = "";
        String codMisCustCode3 = "";
        String codMisCustCode4 = "";
        String codMisCustCode5 = "";
        String codMisCustCode6 = "";
        String codMisCustCode7 = "";
        String codMisCustCode8 = "";
        String codMisCustCode9 = "";
        String codMisCustCode10 = "";
        //  String codMisCustCode11 ="";
        String fkey = bpcc.Fkey(ifr, "B");
        String query = ConfProperty.getQueryScript("MISCODEUPDATEQUERY").replaceAll("#PID#", ProcessInstanceId).replaceAll("#FKEY#", fkey);
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "MISCODE:");
        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                codMisCustCode1 = result.get(0).get(0);
                codMisCustCode2 = result.get(0).get(1);
                codMisCustCode3 = result.get(0).get(2);
                codMisCustCode4 = result.get(0).get(3);
                codMisCustCode5 = result.get(0).get(4);
                codMisCustCode6 = result.get(0).get(5);
                codMisCustCode7 = result.get(0).get(6);
                codMisCustCode8 = result.get(0).get(7);
                codMisCustCode9 = result.get(0).get(8);
                codMisCustCode10 = result.get(0).get(9);
                //  codMisCustCode11 = result.get(i).get(9);

            }
        } else {
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
        CIM09 cim09 = new CIM09();
        String response = cim09.updateCustMisValue(ifr, CustomerId,
                codMisCustCode1, codMisCustCode2, codMisCustCode3, codMisCustCode4, codMisCustCode5,
                codMisCustCode6, codMisCustCode7, codMisCustCode8, codMisCustCode9, codMisCustCode10);
        Log.consoleLog(ifr, "response==>" + response);
        if (response.contains(RLOS_Constants.ERROR)) {
            Log.consoleLog(ifr, "inside error condition CustMisStatus LAD");
            //  return pcm.returnErrorAPIThroughExecute(ifr);
            return RLOS_Constants.ERROR;
        } else {
            return RLOS_Constants.SUCCESS;
        }
        // return "";
    }

}
