/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.portalAcceleratorCode;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import static com.newgen.iforms.properties.Log.errorLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.util.HashMap;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ishwaryamathiazhagan
 */
public class BRMSRules {

    public static final String ERROR = "ERROR";
    public static final String STATUS = "Status";
    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods apicm = new APICommonMethods();

    public String executeBRMSRule(IFormReference ifr, String ruleName, String Data) throws JSONException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            String query = "SELECT URL,IP_PARAMS,REQUEST,OP_TAG FROM LOS_M_BRMS_SERVICES WHERE RULENAME = '" + ruleName + "'";
            List<List<String>> data = cf.mExecuteQuery(ifr, query, "Execute BRMS Rule Query->");
            for (int i = 0; i < data.size(); i++) {
                List<String> row = data.get(i);
                String url = row.get(0);
                String ipParams = row.get(1);
                String req = row.get(2);
                String opTag = row.get(3);
                jobj = fetchInputAndExecute(ifr, url, ipParams, req, opTag, ruleName, Data);
            }
        } catch (JSONException Ex) {
            jobj.put(STATUS, ERROR);
            Log.consoleLog(ifr, "Inside the Execute BRMS Method Exception raises  " + Ex);
            Log.errorLog(ifr, "Inside the Execute BRMS Method Exception raises  " + Ex);
        }
        Log.consoleLog(ifr, "Final jobj.. " + jobj.toString());
        if (ruleName.equalsIgnoreCase("knock_of_CB_Rule")) {
            JSONParser parser = new JSONParser();
            JSONObject BRMSOutputJSON = (JSONObject) parser.parse(jobj.toString());
            JSONObject BRMSOutputJSONObj = new JSONObject(BRMSOutputJSON);

            String Output = BRMSOutputJSONObj.get("Output").toString();
            Log.consoleLog(ifr, "Output==>" + Output);

            JSONObject OutputJSON = (JSONObject) parser.parse(Output);
            JSONObject OutputJSONObj = new JSONObject(OutputJSON);

            String total_knockoff_cb = OutputJSONObj.get("total_knockoff_cb").toString();
            Log.consoleLog(ifr, "total_knockoff_cb==>" + total_knockoff_cb);

            JSONObject knockoff_opJSON = (JSONObject) parser.parse(total_knockoff_cb);
            JSONObject knockoff_opJSONObj = new JSONObject(knockoff_opJSON);

            String KnockoffOutputValue = knockoff_opJSONObj.get("total_knockoff_cb_op").toString();
            Log.consoleLog(ifr, "KnockoffOutputValue==>" + KnockoffOutputValue);
            if (KnockoffOutputValue.equalsIgnoreCase("Proceed")) {
                return "Proceed";
            } else {
                return "NA";
            }

        }
        if (ruleName.equalsIgnoreCase("knock_of_CP_Rule")) {
            JSONParser parser = new JSONParser();
            JSONObject BRMSOutputJSON = (JSONObject) parser.parse(jobj.toString());
            JSONObject BRMSOutputJSONObj = new JSONObject(BRMSOutputJSON);

            String Output = BRMSOutputJSONObj.get("Output").toString();
            Log.consoleLog(ifr, "Output==>" + Output);

            JSONObject OutputJSON = (JSONObject) parser.parse(Output);
            JSONObject OutputJSONObj = new JSONObject(OutputJSON);

            String total_knockoff_cp = OutputJSONObj.get("total_knockoff_cp").toString();
            Log.consoleLog(ifr, "total_knockoff_cp==>" + total_knockoff_cp);

            JSONObject knockoff_opJSON = (JSONObject) parser.parse(total_knockoff_cp);
            JSONObject knockoff_opJSONObj = new JSONObject(knockoff_opJSON);

            String KnockoffOutputValue = knockoff_opJSONObj.get("total_knockoff_cp_op").toString();
            Log.consoleLog(ifr, "KnockoffOutputValue==>" + KnockoffOutputValue);
            if (KnockoffOutputValue.equalsIgnoreCase("Proceed")) {
                return "Proceed";
            } else {
                return "NA";
            }

        }
        if (ruleName.equalsIgnoreCase("ELIGIBILITY_CB")) {
            WDGeneralData data = ifr.getObjGeneralData();
            String ProcessInstanceId = data.getM_strProcessInstanceId();

            JSONParser parser = new JSONParser();
            JSONObject BRMSOutputJSON = (JSONObject) parser.parse(jobj.toString());
            JSONObject BRMSOutputJSONObj = new JSONObject(BRMSOutputJSON);

            String Output = BRMSOutputJSONObj.get("Output").toString();
            Log.consoleLog(ifr, "Output==>" + Output);

            JSONObject OutputJSON = (JSONObject) parser.parse(Output);
            JSONObject OutputJSONObj = new JSONObject(OutputJSON);

            String validcheck = OutputJSONObj.get("validcheck").toString();
            Log.consoleLog(ifr, "validcheck==>" + validcheck);

            JSONObject validcheckJSON = (JSONObject) parser.parse(validcheck);
            JSONObject validcheckJSONObj = new JSONObject(validcheckJSON);

            String validcheckop = validcheckJSONObj.get("validcheck1op").toString();
            Log.consoleLog(ifr, "validcheck1op==>" + validcheckop);

            String Query4 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
                    + "SET VALID_CHECK='" + validcheckop + "' WHERE PID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query4===>" + Query4);
            ifr.saveDataInDB(Query4);
            if (validcheckop.equalsIgnoreCase("Eligible")) {
                return "Eligible";
            } else {
                return "Not Eligible";
            }
        }
        return "";
    }

    public JSONObject fetchInputAndExecute(IFormReference ifr, String url, String ipParams, String req, String opTag, String ruleName, String Data) throws JSONException {
        Log.consoleLog(ifr, "ENTERED ExecuteBRMSRule1");
        JSONObject jobj = new JSONObject();
        if (url != null && ipParams != null && req != null && opTag != null) {
            String[] p = ipParams.split(",");
            List<String> v = fetchParamsData(ifr, ruleName, Data);
            jobj = callBRMS(ifr, url, p, req, opTag, ruleName, v);
        } else {
            jobj.put(STATUS, ERROR);
            errorLog(ifr, "Data missing in LOS_M_BRMS_SERVICES table");
        }
        return jobj;

    }

    public JSONObject callBRMS(IFormReference ifr, String url, String[] p, String req, String opTag, String ruleName, List<String> v) {
        Log.consoleLog(ifr, "ENTERED ExecuteBRMSRule2");
        JSONObject jobj = new JSONObject();
        try {
            if (p.length == v.size()) {
                for (int j = 0; j < p.length; j++) {
                    if (v.get(j).contains(",")) {
                        String[] InputVal = v.get(j).split(",");
                        for (String InputVal1 : InputVal) {
                            req = req.replaceAll("#" + p[j] + "#", InputVal1);
                        }
                    } else {
                        req = req.replaceAll("#" + p[j] + "#", v.get(j));
                    }
                }
                //Log.consoleLog(ifr, "op tag" + opTag);
                String[] o = opTag.split(",");
                //Log.consoleLog(ifr, "string array" + o);
                for (int k = 0; k < o.length; k++) {
                    req = req.replaceAll("#" + o[k] + "#", "0");
                }
                Log.consoleLog(ifr, "Input XML Request : \n " + req);
                String resp = "";
                if (url.contains("brmsrest")) {
                    resp = new PortalCommonMethods().executeRestBRMS(ifr, req, url);
                    Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);
                    if (resp != null && !resp.equals("")) {
                        JSONObject json = (JSONObject) new JSONParser().parse(resp);
                        jobj = json;
                    } else {
                        jobj.put(STATUS, ERROR);
                        errorLog(ifr, "Response is empty");
                    }
                } else {
                    Log.consoleLog(ifr, "Invalid URL : " + url);
                }
            } else {
                jobj.put(STATUS, ERROR);
                Log.consoleLog(ifr, "Mismatch in inputParamKeys and inputParamValues count...Length of p=" + p.length
                        + ", Length of v=" + v.size());
                Log.errorLog(ifr, "Mismatch in inputParamKeys and inputParamValues count...Length of p=" + p.length
                        + ", Length of v=" + v.size());
            }
        } catch (Exception e) {
            jobj.put(STATUS, ERROR);
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return jobj;
    }

    public List<String> fetchParamsData(IFormReference ifr, String ruleName, String Data) throws org.json.JSONException {
        Log.consoleLog(ifr, "Entered fetchParamsData... ");
        List<String> inputVal = new ArrayList<>();
        if (ruleName.equalsIgnoreCase("CIBIL")) {
            inputVal = fetchCIBILInput(ifr, Data);
        } else if (ruleName.equalsIgnoreCase("EXPERIAN")) {
            inputVal = fetchEXPERIANInput(ifr);
        } else if (ruleName.equalsIgnoreCase("knock_of_CB_Rule")) {
            inputVal = fetchKYCValidationinput(ifr);
        }
        return inputVal;
    }

    public List<String> fetchCIBILInput(IFormReference ifr, String Data) throws org.json.JSONException {
        List<String> inputVal = null;
        try {
            // int cibil_input = 700;
            String product = "60";

            inputVal = new ArrayList<>();
            inputVal.add(String.valueOf(Data));
            inputVal.add(product);
        } catch (Exception Ex) {
            errorLog(ifr, "Error in fetchCIBILInput " + Ex);
        }
        return inputVal;
    }

    public List<String> fetchEXPERIANInput(IFormReference ifr) throws org.json.JSONException {
        List<String> inputVal = null;
        try {
            String experian_input = "";
            String product = "PAPL";
            String cibil_input = "";

            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInsatnceId = Data.getM_strProcessInstanceId();
            String experian = "select EXP_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK "
                    + "WHERE PROCESSINSTANCEID='" + ProcessInsatnceId + "'";
            Log.consoleLog(ifr, "experian==> " + experian);
            List<List<String>> list4 = ifr.getDataFromDB(experian);
            experian_input = list4.get(0).get(0);
            String cibil = "select TRANS_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK "
                    + "WHERE PROCESSINSTANCEID='" + ProcessInsatnceId + "'";
            Log.consoleLog(ifr, "cibil==> " + cibil);
            List<List<String>> list5 = ifr.getDataFromDB(cibil);
            cibil_input = list5.get(0).get(0);

            inputVal = new ArrayList<>();
            inputVal.add(product);
            inputVal.add(experian_input);
            inputVal.add(cibil_input);

        } catch (Exception Ex) {
            errorLog(ifr, "Error in fetchEXPERIANInput " + Ex);
        }

        return inputVal;
    }

    public List<String> fetchKYCValidationinput(IFormReference ifr) throws org.json.JSONException {

        List<String> inputVal = null;
        try {
            Log.consoleLog(ifr, "inside fetchKYCValidationinput  : ");
            String aadhar_ip = "Yes";
            String pan_ip = "Yes";
            String cb_writeoffdate_ip = "18-12-2023";
            String cb_writeoffhistfromapi_ip = "No";
            String cb_npavalfromapi_ip = "No";
            String cb_exis_papl_ip = "No";
            int cb_exispl_canbud_ip = 2;
            String cb_product_ip = "CB";
            String exis_stp_ip = "No";
            String staffchk_ip = "No";
            String nre_ip = "No";
            String nro_a = "No";
            String salariedacc_ip = "Salaried";

            inputVal = new ArrayList<>();
            inputVal.add(aadhar_ip);
            inputVal.add(pan_ip);
            inputVal.add(cb_writeoffdate_ip);
            inputVal.add(cb_writeoffhistfromapi_ip);
            inputVal.add(cb_npavalfromapi_ip);
            inputVal.add(cb_exis_papl_ip);
            inputVal.add(String.valueOf(cb_exispl_canbud_ip));
            inputVal.add(cb_product_ip);
            inputVal.add(exis_stp_ip);
            inputVal.add(staffchk_ip);
            inputVal.add(nre_ip);
            inputVal.add(nro_a);
            inputVal.add(salariedacc_ip);

        } catch (Exception Ex) {
            errorLog(ifr, "Error in fetchKYCValidationinput " + Ex);
        }

        return inputVal;
    }

    public JSONObject executeLOSBRMSRule(IFormReference ifr, String ruleName, String paramsData, String outputTag) {
        Log.consoleLog(ifr, "Inside executeLOSBRMSRule::ruleName:" + ruleName + " ::paramsData:" + paramsData);
        JSONObject jobj = new JSONObject();
        jobj.put("status", RLOS_Constants.SUCCESS);
        try {
            String query = ConfProperty.getQueryScript(RLOS_Constants.EXECUTEBRMSRULE).replaceAll("#ruleName#", ruleName);
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
                    String resp = new PortalCommonMethods().executeRestBRMS(ifr, req, url);
                    Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);

                    //Added by Ahmed for BRMS Request & Response capturing
                    //apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", "");
                    //Ended by Ahmed for BRMS Request & Response capturing
                    if (resp != null && !resp.equals("")) {
                        JSONParser parser = new JSONParser();
                        JSONObject BRMSOutputJSON = (JSONObject) parser.parse(resp);
                        JSONObject BRMSOutputJSONObj = new JSONObject(BRMSOutputJSON);
                        String Output = BRMSOutputJSONObj.get("Output").toString();
                        Log.consoleLog(ifr, "Output==>" + Output);
                        JSONObject OutputJSON = (JSONObject) parser.parse(Output);
                        String[] tags = opTag.split(",");
                        String cibilscore_op = cf.getJsonValue(OutputJSON, tags[0]);
                        JSONObject cibilscore_opJSON = (JSONObject) parser.parse(cibilscore_op);

                        for (int j = 1; j < tags.length; j++) {
                            jobj.put(tags[j], cf.getJsonValue(cibilscore_opJSON, tags[j]));
                        }
                        Log.consoleLog(ifr, "jobj in executeLOSBRMSRule :: " + jobj);
                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        String parsedOutput = getBRMSOutput(ifr, jobj, outputTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + parsedOutput);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return jobj;
                    } else {
                        Log.errorLog(ifr, "Response is empty");
                        jobj.put("status", RLOS_Constants.ERROR);

                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        String parsedOutput = getBRMSOutput(ifr, jobj, outputTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + parsedOutput);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return jobj;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in executeBRMSRule:" + e);
            Log.errorLog(ifr, "Exception in executeBRMSRule:" + e);
            jobj.put("status", RLOS_Constants.ERROR);
        }
        return jobj;
    }
//14-05-2024 Commented by monesh for duplicate coding 
/*    public JSONObject getExecuteBRMSRule(IFormReference ifr, String ruleName, String paramsData, String outputTag) {
        Log.consoleLog(ifr, "Inside executeBRMSRule::ruleName:" + ruleName + " ::paramsData:" + paramsData);
        JSONObject jobj = new JSONObject();
        jobj.put("status", RLOS_Constants.SUCCESS);
        try {
            String query = ConfProperty.getQueryScript(RLOS_Constants.EXECUTEBRMSRULE).replaceAll("#ruleName#", ruleName);
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
                    String resp = new PortalCommonMethods().executeRestBRMS(ifr, req, url);
                    Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);

                    //Added by Ahmed for BRMS Request & Response capturing
                    //apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", "");
                    //Ended by Ahmed for BRMS Request & Response capturing
                    if (resp != null && !resp.equals("")) {
                        JSONParser parser = new JSONParser();
                        JSONObject BRMSOutputJSON = (JSONObject) parser.parse(resp);
                        jobj = new JSONObject(BRMSOutputJSON);
                        Log.consoleLog(ifr, "The response Obj BRMSOutputJSONObj " + jobj);

                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        String parsedOutput = getBRMSOutput(ifr, jobj, outputTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + parsedOutput);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return jobj;
                    } else {
                        Log.errorLog(ifr, "Response is empty");
                        jobj.put("status", RLOS_Constants.ERROR);

                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        String parsedOutput = getBRMSOutput(ifr, jobj, outputTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + parsedOutput);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return jobj;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in executeBRMSRule:" + e);
            Log.errorLog(ifr, "Exception in executeBRMSRule:" + e);
            jobj.put("status", RLOS_Constants.ERROR);
        }
        return jobj;
    }
     */
    //Added by AHmed on 23-04-2024
    public String getBRMSOutput(IFormReference ifr, JSONObject result, String outputTag) {

        Log.consoleLog(ifr, "#getBRMSOutput...");
        Log.consoleLog(ifr, "cf.getJsonValue(result, \"status\"==>" + cf.getJsonValue(result, "status"));
        Log.consoleLog(ifr, "outputTag===>" + outputTag);

        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String cibilOutput = cf.getJsonValue(result, outputTag);
            Log.consoleLog(ifr, cibilOutput);
            return cibilOutput;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    public HashMap<String, Object> getExecuteBRMSRule(IFormReference ifr, String ruleName, String paramsData) {
        Log.consoleLog(ifr, "Inside executeBRMSRule::ruleName:" + ruleName + " ::paramsData:" + paramsData);
        JSONObject jobj = new JSONObject();
        jobj.put("status", RLOS_Constants.SUCCESS);
        HashMap<String, Object> map = new HashMap<>();
        try {
            String query = ConfProperty.getQueryScript(RLOS_Constants.EXECUTEBRMSRULE).replaceAll("#ruleName#", ruleName);
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
                    String resp = new PortalCommonMethods().executeRestBRMS(ifr, req, url);
                    Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);

                    if (resp != null && !resp.equals("")) {
                        JSONParser parser = new JSONParser();
                        JSONObject BRMSOutputJSON = (JSONObject) parser.parse(resp);
                        jobj = new JSONObject(BRMSOutputJSON);
                        Log.consoleLog(ifr, "The response Obj BRMSOutputJSONObj " + jobj);

                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        // 
                        String parsedOutput = getBRMSOutput(ifr, jobj, opTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + map);
                        map = brmsResultprase(ifr, jobj.toString(), opTag);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return map;
                    } else {
                        Log.errorLog(ifr, "Response is empty");
                        map.put("status", RLOS_Constants.ERROR);

                        //================Added by Ahmed for BRMS Request & Response capturing===================
                        String parsedOutput = getBRMSOutput(ifr, jobj, opTag);
                        Log.consoleLog(ifr, "parsedOutput==>" + map);
                        apicm.captureBRMSRuleHistory(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), ruleName, req, resp, "", "", parsedOutput);
                        //================Ended by Ahmed for BRMS Request & Response capturing====================

                        return map;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in executeBRMSRule:" + e);
            Log.errorLog(ifr, "Exception in executeBRMSRule:" + e);
            //jobj.put("status", RLOS_Constants.ERROR);
            map.put("status", RLOS_Constants.ERROR);
            return map;
        }
        return map;

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
}
