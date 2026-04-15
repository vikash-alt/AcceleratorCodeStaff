/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import com.newgen.dlp.integration.common.APICommonMethods;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class FundTransfer {

	// CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String prefundTransfer(IFormReference ifr) {
	    String apiName = "FundTransfer";
	    String serviceName = "CBS_" + apiName;
	    Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
	    String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	    String apiStatus = "";
	    String apiErrorCode = "";
	    String apiErrorMessage = "";
	    String request = "";
	    String response = "";
	    try {
	        Log.consoleLog(ifr, "Entered into Execute_CBSFundTransfer...");

	        String BankCode = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "BANKCODE");
	        String Channel = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "CHANNEL");
	        String S_UserId = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "SC_USERID");
	        String UserId = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "USERID");
	        String TBranch = "";

	        JSONArray getTotalCharges = null;

	        getTotalCharges = pcm.getpredisbTotalChargesSHL(ifr);

	        JSONObject requestMain = new JSONObject();
	        JSONObject input = new JSONObject();
	        JSONObject SessionContext = new JSONObject();

	        JSONObject SupervisorContext = new JSONObject();
	        SupervisorContext.put("PrimaryPassword", "");
	        SupervisorContext.put("UserId", S_UserId);
	        SessionContext.put("SupervisorContext", SupervisorContext);

	        SessionContext.put("BankCode", BankCode);
	        SessionContext.put("Channel", Channel);
	        SessionContext.put("ExternalBatchNumber", "");
	        SessionContext.put("ExternalReferenceNo", cm.getCBSExternalReferenceNo());
	        SessionContext.put("ExternalSystemAuditTrailNumber", "");
	        SessionContext.put("LocalDateTimeText", "");
	        SessionContext.put("OriginalReferenceNo", "");
	        SessionContext.put("OverridenWarnings", "129");
	        SessionContext.put("PostingDateText", "");
	        SessionContext.put("ServiceCode", "");
	        SessionContext.put("SessionTicket", "");
	        SessionContext.put("TransactionBranch", TBranch);
	        SessionContext.put("UserId", UserId);
	        SessionContext.put("UserReferenceNumber", cm.getCBSExternalReferenceNo());
	        SessionContext.put("ValueDateText", "");

	        input.put("SessionContext", SessionContext);
	        input.put("MultipleFundsTransferRequestDTO", getTotalCharges);
	        input.put("DebitTransactionCount", "1");
	        input.put("CreditTransactionCount", getTotalCharges.size() - 1);
	        input.put("makerId", "");
	        input.put("checkerId", "");
	        requestMain.put("input", input);

	        request = requestMain.toString();
	        response = cm.getWebServiceResponse(ifr, apiName, request);
	        Log.consoleLog(ifr, "Response===>" + response);

	        if (!response.equalsIgnoreCase("{}")) {
	            JSONParser parser = new JSONParser();
	            JSONObject TextJSON = (JSONObject) parser.parse(response);
	            JSONObject resultObj = new JSONObject(TextJSON);

	            String body = resultObj.get("body").toString();
	            JSONObject bodyJSON = (JSONObject) parser.parse(body);
	            JSONObject bodyJSONObj = new JSONObject(bodyJSON);
	            JSONObject bodyObj = new JSONObject(bodyJSONObj);

	            String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
	            if (CheckError.equalsIgnoreCase("true")) {

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
	    } catch (Exception e) {
	        Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
	    } finally {
	        Log.consoleLog(ifr, "&&&&inside finally /CaptureRequestResponse" + apiStatus);
	        cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, apiErrorCode,
	                apiErrorMessage, apiStatus);
	    }
	    return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

}
