/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.json.simple.parser.ParseException;

/**
 *
 * @author prakashkaliyamoorthy
 */
public class AccountMiniStatement {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();

    public String getAccountMiniStatementDetails(IFormReference ifr, String journeyType) throws ParseException {

        Log.consoleLog(ifr, "#getAccountMiniStatementDetails starting...");
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "AccountMiniStatement";
        String serviceName = "CBS_" + apiName;
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {
//        String errorCode = "";
//        String errorMessage = "";
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);

            String bankCode = pcm.getConstantValue(ifr, "ACCMINISTMNT", "BANKCODE");
            String channel = pcm.getConstantValue(ifr, "ACCMINISTMNT", "CHANNEL");
            String userId = pcm.getConstantValue(ifr, "ACCMINISTMNT", "USERID");
            String serviceCode = pcm.getConstantValue(ifr, "ACCMINISTMNT", "SERVICECODE");//173
            String pageNo = pcm.getConstantValue(ifr, "ACCMINISTMNT", "PAGENO");//1
            String pageSize = pcm.getConstantValue(ifr, "ACCMINISTMNT", "PAGESIZE");//
            String tBranch = cm.GetHomeBranchCode(ifr, processInstanceId, journeyType);

            //===============Added by Ahmed on 25-06-2024====================
            int minusYear = Integer.parseInt(ConfProperty.getCommonPropertyValue("fromDateFormat"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate today = LocalDate.now();
            LocalDate oneYearAgo = today.minusYears(minusYear);
            String toDate = today.format(formatter);
            String fromDate = oneYearAgo.format(formatter);
            Log.consoleLog(ifr, "fromDate===>" + fromDate);
            Log.consoleLog(ifr, "toDate=====>" + toDate);
            String accountNumber = pcm.getAccountNumber(ifr, journeyType);

            request = "{\n"
                    + "    \"bankCode\": \"" + bankCode + "\",\n"
                    + "    \"channel\": \"" + channel + "\",\n"
                    + "    \"externalBatchNumber\": \"0\",\n"
                    + "    \"externalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "    \"serviceCode\": \"" + serviceCode + "\",\n"
                    + "    \"transactionBranch\": \"" + tBranch + "\",\n"
                    + "    \"userId\": \"" + userId + "\",\n"
                    + "    \"accountNumber\": \"" + accountNumber + "\",\n"
                    + "    \"fromDate\": \"" + fromDate + "\",\n"//20230206
                    + "    \"toDate\": \"" + toDate + "\",\n"//20230206
                    + "    \"pageNumber\": \"" + pageNo + "\",\n"
                    + "    \"pageSize\": \"" + pageSize + "\"\n"
                    + "}";

            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
            } else {
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
            }

            if (apiErrorMessage.equalsIgnoreCase("")) {
                apiStatus = RLOS_Constants.SUCCESS;
            } else {
                apiStatus = RLOS_Constants.ERROR;
            }
//            //==============================================================
//            Log.consoleLog(ifr, "CaptureRequestResponse Calling ");
//            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//                    errorCode, errorMessage, apiStatus);
//            //==============================================================
//
//            

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                return response;
            } else {
                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getAccountMiniStatementDetails===>" + e);
            Log.errorLog(ifr, "Exception/getAccountMiniStatementDetails===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }

        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
    }
}
