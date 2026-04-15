/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.nesl;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class ESignGetURL {

	CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods apicm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();

	public String getEsignURL(IFormReference ifr, JSONObject eSignURLObj) {

		Log.consoleLog(ifr, "#getEsignURL Started.....");
		String serviceName = "NESL_GetESignURL";
		try {

			String request = "{\n" + "  \"loanno\": \"" + eSignURLObj.get("NESLRefNo").toString() + "\",\n"
					+ "  \"transId\": \"" + eSignURLObj.get("TxnId").toString() + "\"\n" + "}";
			Log.consoleLog(ifr, "Request==>" + request);

			HashMap requestHeader = new HashMap<>();
			requestHeader.put("journey", "staff");
			String Response = cf.CallWebService(ifr, serviceName, request, "", requestHeader);
			Log.consoleLog(ifr, "Response==>" + Response);
			String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			try {
				apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, serviceName, request, Response, "", "", "");
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception ==>" + e);
			}
			if (!Response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(Response);
				String esignLink = resultObj.get("esignLink").toString();
				Log.consoleLog(ifr, "esignLink==>" + esignLink);
                
				String bodyParams = "Please check your SMS/Email for ESIGN link for eStamping & eSigning"+ esignLink;
				String subjectParams = "SMS/Email for ESIGN link";
				String fileName = "";
				String fileContent = "";
             	pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "STAFF", "26", bodyParams, subjectParams, fileName,
						fileContent);
				
				String query = "UPDATE LOS_INTEGRATION_NESL_STATUS " + "SET ESIGNLINK='" + esignLink + "' "
						+ "WHERE PROCESSINSTANCEID='" + eSignURLObj.get("ProcessInstanceId").toString() + "'";
				Log.consoleLog(ifr, "query=====>" + query);
				ifr.saveDataInDB(query);
				return RLOS_Constants.SUCCESS;
			} else {
				apicm.CaptureNESLRequestResponse(ifr, ProcessInstanceId, serviceName, request, "Check NESL/Fintech whether it is down", "", "", "");
				Log.consoleLog(ifr, "#Check NESL/Fintech whether it is down.");
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getEsignURL=====>" + e);
		}

		return RLOS_Constants.ERROR;

	}

}
