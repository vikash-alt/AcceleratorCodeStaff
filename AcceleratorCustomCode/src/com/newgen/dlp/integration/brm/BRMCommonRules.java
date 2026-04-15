/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.brm;

import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author monesh.kumar
 */
public class BRMCommonRules {

    BRMSRules jsonBRMSCall = new BRMSRules();
    CommonFunctionality cf = new CommonFunctionality();
    CommonMethods cm=new CommonMethods();

    public String checkCICScore(IFormReference ifr, String productCode, String subProductCode, String bureauType, String applicanttype) {
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String cbScore = "";
        String exScore = "";
        if (bureauType.equalsIgnoreCase("CB")) {
            String cbQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + processInstanceId + "' and BUREAUTYPE='CB' and APPLICANT_TYPE='" + applicanttype + "' ";
            List<List<String>> cbResult = cf.mExecuteQuery(ifr, cbQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (cbResult.size() > 0) {
                cbScore = cbResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

        } else {
            String cbQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + processInstanceId + "' and BUREAUTYPE='CB' and APPLICANT_TYPE='" + applicanttype + "'  ";
            List<List<String>> cbResult = cf.mExecuteQuery(ifr, cbQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (cbResult.size() > 0) {
                cbScore = cbResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

            String exQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + processInstanceId + "' and BUREAUTYPE='EX' and APPLICANT_TYPE='" + applicanttype + "' ";
            List<List<String>> exResult = cf.mExecuteQuery(ifr, exQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (exResult.size() > 0) {
                exScore = exResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

        }

        String decision;
        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

        if (!cbScore.equalsIgnoreCase("I")) {
            if (bureauType.equalsIgnoreCase("CB")) {
                String knockInPramas = productCode + "," + subProductCode + "," + cbScore;
                decision = callBRMRule(ifr, "CIBILSCORE", knockInPramas, "cibilscrop");
            } else {
                String knockInPramas = productCode + "," + subProductCode + "," + cbScore + "," + exScore;
                decision = callBRMRule(ifr, "EXPERIANSCORE", knockInPramas, "experianscrop");
            }
        } else {
            Log.consoleLog(ifr, "BRMCommonRules:checkCICScore->Customer is CIC Immune: " + cbScore);
            decision = "Approve";
        }
        Log.consoleLog(ifr, "decision==>" + decision);
        if (decision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            return decision;
        }
    }
//Added by Ahmed on 12-06-2024 for BRMS New Rule change

    public String checkCICEligibility(IFormReference ifr, String productCode, String subProductCode) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String returnresponse = getMinBureauScore(ifr);
//        if (returnresponse.contains(RLOS_Constants.ERROR)) {
//            return RLOS_Constants.ERROR;
//        }
//        String[] bSplitter = returnresponse.split("-");
//        String bureauType = bSplitter[0];
//        String bureauScore = bSplitter[1];

        CommonMethods cm = new CommonMethods();
        String returnresponse = cm.getMaxTotalEMIAmount(ifr);
        if (returnresponse.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        }
        String[] bSplitter = returnresponse.split("-");
        String bureauType = bSplitter[0];
        //   String totalEMIAmount = bSplitter[1];

        Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Bureau Type: " + bureauType);
        // Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Bureau Scoreon: " + bureauScore);

        String CountQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,TOTNONEMICOUNT FROM "
                + "LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
                + "and BUREAUTYPE='" + bureauType + "'";

        List<List<String>> Result = cf.mExecuteQuery(ifr, CountQuery, "Count Query:");
        String CICScore;
        String CICNPACHECK;
        String CICOVERDUE;
        String WRITEOFF;
        String TOTNONEMICOUNT;

        if (Result.size() > 0) {
            CICScore = Result.get(0).get(0);
            CICNPACHECK = Result.get(0).get(1);
            CICOVERDUE = Result.get(0).get(2);
            WRITEOFF = Result.get(0).get(3);
            TOTNONEMICOUNT = Result.get(0).get(4);
        } else {
            return RLOS_Constants.ERROR;
        }
        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
        String npaDecision = null;
        if (!CICScore.equalsIgnoreCase("I")) {
            String knockInPramas = CICOVERDUE + "," + CICNPACHECK + "," + productCode + "," + subProductCode + "," + WRITEOFF + "," + TOTNONEMICOUNT;
            npaDecision = callBRMRule(ifr, "KNOCKOFFCIC_ELIGIBILITY", knockInPramas, "totalknockoff_output");
        } else {
            Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Customer is CIC Immune: " + CICScore);
            npaDecision = "Approve";
        }

        if (npaDecision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else if ((npaDecision.equalsIgnoreCase("Approve"))) {
            return "Approve";
        } else {
            return "Reject";
        }

    }

//Commented by Ahmed on 12-06-2024 for BRMS New Rule change
//    public String checkCICEligibility(IFormReference ifr, String productCode, String subProductCode) {
//
//        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
////        String returnresponse = getMinBureauScore(ifr);
////        if (returnresponse.contains(RLOS_Constants.ERROR)) {
////            return RLOS_Constants.ERROR;
////        }
////        String[] bSplitter = returnresponse.split("-");
////        String bureauType = bSplitter[0];
////        String bureauScore = bSplitter[1];
//
//        CommonMethods cm = new CommonMethods();
//        String returnresponse = cm.getMaxTotalEMIAmount(ifr);
//        if (returnresponse.contains(RLOS_Constants.ERROR)) {
//            return RLOS_Constants.ERROR;
//        }
//        String[] bSplitter = returnresponse.split("-");
//        String bureauType = bSplitter[0];
//        //   String totalEMIAmount = bSplitter[1];
//
//        Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Bureau Type: " + bureauType);
//        // Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Bureau Scoreon: " + bureauScore);
//
//        String CountQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF FROM "
//                + "LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
//                + "and BUREAUTYPE='" + bureauType + "'";
//
//        List<List<String>> Result = cf.mExecuteQuery(ifr, CountQuery, "Count Query:");
//        String CICScore;
//        String CICNPACHECK;
//        String CICOVERDUE;
//        String WRITEOFF;
//
//        if (Result.size() > 0) {
//            CICScore = Result.get(0).get(0);
//            CICNPACHECK = Result.get(0).get(1);
//            CICOVERDUE = Result.get(0).get(2);
//            WRITEOFF = Result.get(0).get(3);
//        } else {
//            return RLOS_Constants.ERROR;
//        }
//        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
//        String npaDecision = null;
//        if (!CICScore.equalsIgnoreCase("I")) {
//            String knockInPramas = CICOVERDUE + "," + CICNPACHECK + "," + productCode + "," + subProductCode + "," + WRITEOFF;
//            npaDecision = callBRMRule(ifr, "KNOCKOFF_CIC_ELIGIBILITY", knockInPramas, "totalknockoff_output");
//        } else {
//            Log.consoleLog(ifr, "BRMCommonRules:checkCICEligibility->Customer is CIC Immune: " + CICScore);
//            npaDecision = "Approve";
//        }
//
//        if (npaDecision.contains(RLOS_Constants.ERROR)) {
//            return RLOS_Constants.ERROR;
//        } else if ((npaDecision.equalsIgnoreCase("Approve"))) {
//            return "Approve";
//        } else {
//            return "Reject";
//        }
//
//    }
    public String callBRMRule(IFormReference ifr, String RuleName, String values, String ValueTag) {
        JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
        Log.consoleLog(ifr, "BRMS Result:" + result);
        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String cibilOutput = cf.getJsonValue(result, ValueTag);
            Log.consoleLog(ifr, cibilOutput);
            return cibilOutput;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    private String getMinBureauScore(IFormReference ifr) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String bureauChkQuery = "SELECT EXP_CBSCORE, bureautype\n"
                + "FROM LOS_CAN_IBPS_BUREAUCHECK\n"
                + "WHERE processinstanceid = '" + ProcessInstanceId + "'\n"
                + "AND EXP_CBSCORE = (\n"
                + "    SELECT MIN(EXP_CBSCORE)\n"
                + "    FROM LOS_CAN_IBPS_BUREAUCHECK\n"
                + "    WHERE processinstanceid = '" + ProcessInstanceId + "'\n"
                + ")";
        Log.consoleLog(ifr, "bureauChkQuery:" + bureauChkQuery);
        //  List<List<String>> bureauChk = cf.mExecuteQuery(ifr, bureauChkQuery, "bureauChkQuery:");
        List< List< String>> Result = ifr.getDataFromDB(bureauChkQuery);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (Result.size() > 0) {
            String bureauScore = Result.get(0).get(0);
            String bureauType = Result.get(0).get(1);
            return bureauType + "-" + bureauScore;
        }
        //L

        return RLOS_Constants.ERROR;
    }

    public HashMap<String, Object> brmsResultprase(IFormReference ifr, String brmsout, String brmsouttags) {
        HashMap<String, Object> map = new HashMap<>();
        try {

            Log.consoleLog(ifr, "POST NOT WORKED");
            JSONParser parser = new JSONParser();
            JSONObject BRMSOutputJSON = (JSONObject) parser.parse(brmsout);
            JSONObject outputJSON = (JSONObject) BRMSOutputJSON.get("Output");
            String[] parentKeys = brmsouttags.split(",");
            Log.consoleLog(ifr, "POST NOT WORKED");
            for (int i = 0; i < parentKeys.length; i += 2) {
                String parentKey = parentKeys[i];
                String outKey = parentKeys[i + 1];
                JSONObject parentObject = (JSONObject) outputJSON.get(parentKey);
                Object value = parentObject.get(outKey);
                Log.consoleLog(ifr, "POST Key: " + outKey + ", Value: " + value);
                map.put(outKey, value);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "es " + e.getMessage());
        }
        return map;
    }

	public HashMap<String, Object> getExecuteBRMSrule(IFormReference ifr, String ruleName, String paramsData) {
    Log.consoleLog(ifr, "Inside executeBRMSRule::ruleName:" + ruleName + " ::paramsData:" + paramsData);
    JSONObject jobj = new JSONObject();
    jobj.put("status", RLOS_Constants.SUCCESS);
    HashMap<String, Object> map = new HashMap<>();
    try {
        String query = ConfProperty.getQueryScript(RLOS_Constants.EXECUTEBRMSRULE).replaceAll("#ruleName#", ruleName);
        Log.consoleLog(ifr, "Inside the executeBRMSRule try block::"+ruleName);
        List<List<String>> data = cf.mExecuteQuery(ifr, query, "BRMSRULE " + ruleName);
        for (int i = 0; i < data.size(); i++) {
            String url = ConfProperty.getCommonPropertyValue("BRMSServerIpPort") + data.get(i).get(0);
            Log.consoleLog(ifr, "url:" + url);
            String ipParams = data.get(i).get(1);
            String req = data.get(i).get(2);
            String opTag = data.get(i).get(3);
            if (url == null || url.trim().equals("") || ipParams == null || ipParams.trim().equals("")
                    || req == null || req.trim().equals("")) {
                jobj.put("status", RLOS_Constants.ERROR);
                return jobj;
            } else {
                String[] p = ipParams.split(",");
                String[] v = paramsData.split(",");
                for (int j = 0; j < p.length; j++) {
                    req = req.replaceAll("#" + p[j] + "#", v[j]);
                }
                Log.consoleLog(ifr, "::Final request for BRMS " + ruleName + " is ::" + req);
                Log.consoleLog(ifr, "Final requers URL for BRMS:: "+url);
                String resp = executeRestBRMS(ifr, req, url);
                Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);
                if (resp != null && !resp.equals("")) {
                    JSONParser parser = new JSONParser();
                    JSONObject BRMSOutputJSON = (JSONObject) parser.parse(resp);
                    jobj = new JSONObject(BRMSOutputJSON);
                    Log.consoleLog(ifr, "The response Obj BRMSOutputJSONObj " + jobj);

                    String parsedOutput = getBRMSOutput(ifr, jobj, opTag);
                    Log.consoleLog(ifr, "parsedOutput==>" + map);
                    map = brmsResultprase(ifr, jobj.toString(), opTag);
                    captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);

                    return map;
                } else {
                    Log.errorLog(ifr, "Response is empty");
                    map.put("Status", RLOS_Constants.ERROR);

                    //================Added by Ahmed for BRMS Request & Response capturing===================
                    String parsedOutput = getBRMSOutput(ifr, jobj, opTag);
                    Log.consoleLog(ifr, "parsedOutput==>" + map);
                    captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                    //================Ended by Ahmed for BRMS Request & Response capturing====================

                    return map;
                }
            }
        }

        map.put("Status", RLOS_Constants.ERROR);
        return map;
    } catch (Exception e) {
        Log.consoleLog(ifr, "Exception in executeBRMSRule:" + e);
        Log.errorLog(ifr, "Exception in executeBRMSRule:" + e);
        //jobj.put("status", RLOS_Constants.ERROR);
        map.put("Status", RLOS_Constants.ERROR);
        return map;
    }

}

	public String getBRMSOutput(IFormReference ifr, JSONObject result, String outputTag) {

	    Log.consoleLog(ifr, "#getBRMSOutput...");
	    Log.consoleLog(ifr, "cf.getJsonValue(result, \"status\"==>" + cf.getJsonValue(result, "status"));
	    Log.consoleLog(ifr, "outputTag===>" + outputTag);

	    if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
	        String cibilOutput = cf.getJsonValue(result, outputTag);
	        Log.consoleLog(ifr, cibilOutput);
	        return cibilOutput;
	    } else {
	        Log.consoleLog(ifr, "Error:" + RLOS_Constants.ERROR);
	        return "ERROR";
	    }
	}
	
	public String executeRestBRMS(IFormReference ifr, String req, String url) throws IOException {
	    final String POST_PARAMS = req;
	    String ResponseValue = null;
	    Log.consoleLog(ifr, "\"executeRestBRMS POST_PARAMS\" : " + POST_PARAMS);

	    HttpURLConnection httpConn = null;
	    OutputStream os = null;
	    try {
	        URL obj = new URL(url);
	        httpConn = (HttpURLConnection) obj.openConnection();
	        httpConn.setRequestMethod("POST");
	        httpConn.setRequestProperty("Content-Type", "application/json");
	        httpConn.setDoOutput(true);
	        os = httpConn.getOutputStream();
	        os.write(POST_PARAMS.getBytes());
	        os.flush();
	        os.close();
	        int responseCode = httpConn.getResponseCode();
	        Log.consoleLog(ifr, "After HTTP Connection in POST");
	        Log.consoleLog(ifr, "POST Response Code" + responseCode);
	        Log.consoleLog(ifr, "POST Response Message " + httpConn.getResponseMessage());
	        if (responseCode == HttpURLConnection.HTTP_OK) { //success
	            BufferedReader in = new BufferedReader(new InputStreamReader(
	                    httpConn.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();
	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();
	            Log.consoleLog(ifr, "After HTTP Connection" + response.toString());
	            ResponseValue = response.toString();
	        } else {
	            Log.consoleLog(ifr, "POST NOT WORKED");
	        }
	    } catch (Exception ex) {
	        Log.errorLog(ifr, "Error in executeRestBRMS finally1 : " + ex);
	    } finally {
	        try {
	            if (os != null) {
	                os.close();
	            }
	        } catch (Exception ex) {
	            Log.errorLog(ifr, "Error in executeRestBRMS finally1 : " + ex);
	        } finally {
	            try {
	                if (httpConn != null) {
	                    httpConn.disconnect();
	                }
	            } catch (Exception exc) {
	                Log.errorLog(ifr, "Error in executeRestBRMS finally2 : " + exc);
	            }
	        }
	    }
	    return ResponseValue;
	}
	 
	public String captureBRMSRuleHistory(IFormReference ifr, String WorkItemName, String RuleName, String Request,
			String Response, String ErrorCode, String ErrorMessage, String APIStatus) {
		IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
		HttpServletRequest req = iFormAPIHandler.getRequest();
		HttpSession session = req.getSession();
		try {
			Log.consoleLog(ifr, "#captureBRMSRuleHistory Starting...");

//======================================================================//
			String mobileNumber = "";
			String PRODUCT_TYPE = (String) session.getAttribute("Portal_LOAN_SELECTED");
			Log.consoleLog(ifr, "productType==>" + PRODUCT_TYPE);
			mobileNumber = ifr.getValue("Portal_T_MobileNumber").toString();

			String TRANSACTION_ID = WorkItemName;
			String REQUEST = cm.getClobSnippetData(ifr, Request);
			String RESPONSE = cm.getClobSnippetData(ifr, Response);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String CurrentDateTime = dtf.format(now);
			Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);

			try {
				String Query = "INSERT INTO LOS_HIS_BRMSRULES "
						+ "(PRODUCT_TYPE,RULE_NAME,ENTRYDATETIME,PROCESSINSTANCEID,REQUEST,"
						+ "RESPONSE,RESPONSE_CODE,BRMS_OUTPUT) " + "VALUES('" + PRODUCT_TYPE + "'," + "'" + RuleName
						+ "'," + "'" + CurrentDateTime + "'," + "'" + TRANSACTION_ID + "'," + "" + REQUEST + "," + ""
						+ RESPONSE + "," + "'" + ErrorCode.replace("'", "") + "'," + "'" + APIStatus + "')";

				Log.consoleLog(ifr, "Query==>" + Query);
				ifr.saveDataInDB(Query);

			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception==>" + e);
			}
//======================================================================//
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception==>" + e);
		}
		return "";
	}

	 
	 
}
