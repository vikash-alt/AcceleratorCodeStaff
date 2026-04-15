/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;

/**
 *
 * @author monesh.kumar
 */
public class PensionDetails {

    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods cm = new APICommonMethods();
    Advanced360EnquiryData AED =new Advanced360EnquiryData();

    /* public String getPensionExternalDetails(IFormReference ifr, String custid, String branchcode) {
        checkdataexits(ifr,custid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("customerId", custid);
        String response = "";
        String request = jsonObject.toString();
        String responseCode = "";
        HashMap<String, String> requestHeader = new HashMap<>();
        try {
            response = cf.CallWebService(ifr, "CUSTPENSION", request, "", requestHeader);

            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            JSONObject resultObj = new JSONObject(OutputJSON);

            String body = resultObj.get("body").toString();
            Object obj = parser.parse(body);
            // JSONObject bodyObj = new JSONObject(bodyJSON);
            responseCode = resultObj.get("responseCode").toString();
            Log.consoleLog(ifr, "response" + responseCode);
            JSONArray pensionDetailsJSON = new JSONArray();
            System.out.println("response" + obj.toString());
            if (responseCode.equalsIgnoreCase("200")) {
                Object pensionDetails = obj;
                if (obj instanceof JSONArray) {
                    pensionDetailsJSON = (JSONArray) obj;
                    pensionDetailsJSON.add(pensionDetails);
                } else {
                    pensionDetailsJSON.add(pensionDetails);
                }
                for (int i = 0; i < 1; i++) {
                    String individualpensionDetails = pensionDetailsJSON.get(i).toString();
                    JSONObject individualpensDetailsJSON = (JSONObject) parser.parse(individualpensionDetails);
                    JSONObject individualpenDetailsJSONObject = new JSONObject(individualpensDetailsJSON);
                    String strPPO_UNIQUE_ID = individualpenDetailsJSONObject.get("PPO_UNIQUE_ID").toString().trim();
                    String strPPO_NUMBER = individualpenDetailsJSONObject.get("PPO_NUMBER").toString().trim();
                    String strACCOUNT_NUMBER = individualpenDetailsJSONObject.get("ACCOUNT_NUMBER").toString().trim();
                    String strNAME = individualpenDetailsJSONObject.get("NAME").toString().trim();
                    String strDEPARTMENT = individualpenDetailsJSONObject.get("CATEGORY").toString().trim();
                    String strCATEGORY = individualpenDetailsJSONObject.get("DEPARTMENT").toString().trim();
                    String strAGE = individualpenDetailsJSONObject.get("AGE").toString().trim();
                    String strDATE_OF_BIRTH = individualpenDetailsJSONObject.get("DATE_OF_BIRTH").toString().trim();
                    String strDATE_OF_RETIREMENT = individualpenDetailsJSONObject.get("DATE_OF_RETIREMENT").toString().trim();
                    String strFAMILY_PENSIONER_NAME = individualpenDetailsJSONObject.get("FAMILY_PENSIONER_NAME").toString().trim();
                    String strDOB_FAMILY_PENSIONER = individualpenDetailsJSONObject.get("DOB_FAMILY_PENSIONER").toString().trim();
                    String strPENSION_TYPE = individualpenDetailsJSONObject.get("PENSION_TYPE").toString().trim();
                    String strRELATIONSHIP = individualpenDetailsJSONObject.get("RELATIONSHIP").toString().trim();
                    String strPAN = individualpenDetailsJSONObject.get("PAN").toString().trim();
                    String strAADHAR_ID = individualpenDetailsJSONObject.get("AADHAR_ID").toString().trim();
                    String strMOBILE_NUMBER = individualpenDetailsJSONObject.get("MOBILE_NUMBER").toString().trim();
                    String strPEN_MNTH = individualpenDetailsJSONObject.get("PEN_MNTH").toString().trim();
                    String strNET_PENSION = individualpenDetailsJSONObject.get("NET_PENSION").toString().trim();
                    String strGROSS_PENSION = individualpenDetailsJSONObject.get("GROSS_PENSION").toString().trim();
                    String insterquery = ConfProperty.getQueryScript("PORTALDATAPENSIONINSERT");
                    insterquery = insterquery.replaceAll("#strPPO_UNIQUE_ID#", strPPO_UNIQUE_ID)
                            .replaceAll("#strPPO_NUMBER#", strPPO_NUMBER)
                            .replaceAll("#strACCOUNT_NUMBER#", strACCOUNT_NUMBER)
                            .replaceAll("#strPENSION_TYPE#", strPENSION_TYPE)
                            .replaceAll("#strNAME#", strNAME)
                            .replaceAll("#strCATEGORY#", strCATEGORY)
                            .replaceAll("#strDEPARTMENT#", strDEPARTMENT)
                            .replaceAll("#strAGE#", strAGE)
                            .replaceAll("#strDATE_OF_BIRTH#", strDATE_OF_BIRTH)
                            .replaceAll("#strDATE_OF_RETIREMENT#", strDATE_OF_RETIREMENT)
                            .replaceAll("#strFAMILY_PENSIONER_NAME#", strFAMILY_PENSIONER_NAME)
                            .replaceAll("#strDOB_FAMILY_PENSIONER#", strDOB_FAMILY_PENSIONER)
                            .replaceAll("#strPAN#", strPAN)
                            .replaceAll("#strRELATIONSHIP#", strRELATIONSHIP)
                            .replaceAll("#strAADHAR_ID#", strAADHAR_ID)
                            .replaceAll("#strMOBILE_NUMBER#", strMOBILE_NUMBER)
                            .replaceAll("#strPEN_MNTH#", strPEN_MNTH)
                            .replaceAll("#strGROSS_PENSION#", strGROSS_PENSION)
                            .replaceAll("#strNET_PENSION#", strNET_PENSION)
                            .replaceAll("#strCUSTOMER_ID#", custid)
                            .replaceAll("#strBRANCHDP_CODE#", branchcode);
                    Log.consoleLog(ifr, "Insert query  " + insterquery);
                    cf.mExecuteQuery(ifr, insterquery, "PORTALINSERDATA Query:");
                }
                return RLOS_Constants.SUCCESS;
            } else {
                return RLOS_Constants.ERROR;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            //return RLOS_Constants.ERROR;
        }

        return RLOS_Constants.ERROR;
    }

     */
    public String getPensionExternalDetails(IFormReference ifr, String custid, String branchcode) {

        //Added by Ahmed on 21-06-2024 for pension MockData Loader
        Log.consoleLog(ifr, "===========#getPensionExternalDetails========================");
        String apiName = "CustomerPension";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {
            checkdataexits(ifr, custid);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("customerId", custid);

            request = jsonObject.toString();
            String responseCode = "";

            //HashMap<String, String> requestHeader = new HashMap<>();
            // response = cf.CallWebService(ifr, "CUSTPENSION", request, "", requestHeader);
            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(OutputJSON);
                String body = resultObj.get("body").toString();
                Object obj = parser.parse(body);
                responseCode = resultObj.get("responseCode").toString();
                Log.consoleLog(ifr, "response" + responseCode);
                Log.consoleLog(ifr, "responsev from PAI " + response);
                org.json.JSONArray resArr = null, jsonfilter = null, finalRes = null;
                String[] arrPension = ConfProperty.getIntegrationValue("PENSIONOUTTAGS").split(",");
                resArr = AED.getCustomJsonArray(ifr, response, ConfProperty.getIntegrationValue("PENSIONSEARCHTAGS"));
                jsonfilter = getCustomJsonFilter(ifr, "PENDET");
                finalRes = AED.filterJsonData(ifr, resArr, jsonfilter, ConfProperty.getIntegrationValue("PENSIONOUTTAGS"));
                Log.consoleLog(ifr, "responsev from PAI " + finalRes);
                if (responseCode.equalsIgnoreCase("200")) {
                    if (finalRes.isEmpty()) {

                        return "no data found ";

                    } else {
                        String individualpensionDetails = finalRes.get(0).toString();
                        JSONObject individualpensDetailsJSON = (JSONObject) parser.parse(individualpensionDetails);
                        JSONObject individualpenDetailsJSONObject = new JSONObject(individualpensDetailsJSON);
                        String strPPO_UNIQUE_ID = individualpenDetailsJSONObject.get(arrPension[0]).toString().trim();
                        Log.consoleLog(ifr, "strPPO_UNIQUE_ID" + strPPO_UNIQUE_ID);
                        String strPPO_NUMBER = individualpenDetailsJSONObject.get(arrPension[1]).toString().trim();
                        Log.consoleLog(ifr, "strPPO_NUMBER" + strPPO_NUMBER);
                        String strACCOUNT_NUMBER = individualpenDetailsJSONObject.get(arrPension[2]).toString().trim();
                        Log.consoleLog(ifr, "strPPO_NUMBER" + strPPO_NUMBER);
                        String strPENSION_TYPE = individualpenDetailsJSONObject.get(arrPension[3]).toString().trim();
                        Log.consoleLog(ifr, "strPENSION_TYPE" + strPENSION_TYPE);
                        String strNAME = individualpenDetailsJSONObject.get(arrPension[4]).toString().trim();
                        Log.consoleLog(ifr, "strNAME" + strNAME);
                        String strCATEGORY = individualpenDetailsJSONObject.get(arrPension[5]).toString().trim();
                        Log.consoleLog(ifr, "strNAME" + strCATEGORY);
                        String strDEPARTMENT = individualpenDetailsJSONObject.get(arrPension[6]).toString().trim();
                        Log.consoleLog(ifr, "strDEPARTMENT" + strDEPARTMENT);
                        String strAGE = individualpenDetailsJSONObject.get(arrPension[7]).toString().trim();
                        Log.consoleLog(ifr, "strAGE" + strAGE);
                        String strDATE_OF_BIRTH = individualpenDetailsJSONObject.get(arrPension[8]).toString().trim();
                        Log.consoleLog(ifr, "strDATE_OF_BIRTH" + strDATE_OF_BIRTH);
                        String strDATE_OF_RETIREMENT = individualpenDetailsJSONObject.get(arrPension[9]).toString().trim();
                        Log.consoleLog(ifr, "strDATE_OF_RETIREMENT" + strDATE_OF_RETIREMENT);
                        String strFAMILY_PENSIONER_NAME = individualpenDetailsJSONObject.get(arrPension[10]).toString().trim();
                        Log.consoleLog(ifr, "strFAMILY_PENSIONER_NAME" + strFAMILY_PENSIONER_NAME);
                        String strDOB_FAMILY_PENSIONER = individualpenDetailsJSONObject.get(arrPension[11]).toString().trim();
                        Log.consoleLog(ifr, "strDOB_FAMILY_PENSIONER" + strDOB_FAMILY_PENSIONER);
                        String strRELATIONSHIP = individualpenDetailsJSONObject.get(arrPension[12]).toString().trim();
                        Log.consoleLog(ifr, "strRELATIONSHIP" + strRELATIONSHIP);
                        String strPAN = individualpenDetailsJSONObject.get(arrPension[13]).toString().trim();
                        Log.consoleLog(ifr, "strPAN" + strPAN);
                        String strAADHAR_ID = individualpenDetailsJSONObject.get(arrPension[14]).toString().trim();
                        Log.consoleLog(ifr, "strAADHAR_ID" + strAADHAR_ID);
                        String strMOBILE_NUMBER = individualpenDetailsJSONObject.get(arrPension[15]).toString().trim();
                        if (strMOBILE_NUMBER.length() > 10) {
                            strMOBILE_NUMBER = strMOBILE_NUMBER.substring(2, 12);
                        }
                        Log.consoleLog(ifr, "strMOBILE_NUMBER" + strMOBILE_NUMBER.trim());
                        String strPEN_MNTH = individualpenDetailsJSONObject.get(arrPension[16]).toString().trim();
                        Log.consoleLog(ifr, "strPEN_MNTH" + strPEN_MNTH);
                        String strNET_PENSION = individualpenDetailsJSONObject.get(arrPension[17]).toString().trim();
                        Log.consoleLog(ifr, "strNET_PENSION" + strNET_PENSION);
                        String strGROSS_PENSION = individualpenDetailsJSONObject.get(arrPension[18]).toString().trim();
                        Log.consoleLog(ifr, "strGROSS_PENSION" + strGROSS_PENSION);
                        String insterquery = ConfProperty.getQueryScript("PORTALDATAPENSIONINSERT");
                        Log.consoleLog(ifr, "insterquery::>" + insterquery);
                        insterquery = insterquery.replaceAll("#strPPO_UNIQUE_ID#", strPPO_UNIQUE_ID).replaceAll("#strPPO_NUMBER#", strPPO_NUMBER).replaceAll("#strACCOUNT_NUMBER#", strACCOUNT_NUMBER).replaceAll("#strPENSION_TYPE#", strPENSION_TYPE).replaceAll("#strNAME#", strNAME).replaceAll("#strCATEGORY#", strCATEGORY).replaceAll("#strDEPARTMENT#", strDEPARTMENT).replaceAll("#strAGE#", strAGE).replaceAll("#strDATE_OF_BIRTH#", strDATE_OF_BIRTH).replaceAll("#strDATE_OF_RETIREMENT#", strDATE_OF_RETIREMENT).replaceAll("#strFAMILY_PENSIONER_NAME#", strFAMILY_PENSIONER_NAME).replaceAll("#strDOB_FAMILY_PENSIONER#", strDOB_FAMILY_PENSIONER).replaceAll("#strPAN#", strPAN).replaceAll("#strRELATIONSHIP#", strRELATIONSHIP).replaceAll("#strAADHAR_ID#", strAADHAR_ID).replaceAll("#strMOBILE_NUMBER#", strMOBILE_NUMBER).replaceAll("#strPEN_MNTH#", strPEN_MNTH).replaceAll("#strGROSS_PENSION#", strGROSS_PENSION).replaceAll("#strNET_PENSION#", strNET_PENSION).replaceAll("#strCUSTOMER_ID#", custid).replaceAll("#strBRANCHDP_CODE#", branchcode);
                        Log.consoleLog(ifr, "Insert query  " + insterquery);
                        cf.mExecuteQuery(ifr, insterquery, "PORTALINSERDATA Query:");
                        //return RLOS_Constants.SUCCESS;
                    }
                }

                //  return "SUCCESS";
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
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        } finally {
            cm.CaptureRequestResponse(ifr, "", serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;

        //  return RLOS_Constants.ERROR;
        //return RLOS_Constants.ERROR;
    }

    public void checkdataexits(IFormReference ifr, String custid) {
        String strquery = "select customer_id from los_mst_pension where customer_id='" + custid + "'";

        List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "Check Pension Data  Query:");
        if (list1.size() > 0) {
            String strdelepen = "Delete from los_mst_pension where customer_id='" + custid + "'";
            cf.mExecuteQuery(ifr, strdelepen, "Delete Pension Data  Query:");
        } else {
            Log.consoleLog(ifr, "No data found inserting  data :");
        }

    }
    public org.json.JSONArray getCustomJsonFilter(IFormReference ifr, String filterType) {
        org.json.JSONArray jsonArr = null;
        try {
            DateTimeFormatter formatter;
            LocalDate currentDate;
            String lastMonthString;
            switch (filterType) {
                case "PENDET":
                    formatter = DateTimeFormatter.ofPattern(ConfProperty.getIntegrationValue("Datepattern"));
                    currentDate = LocalDate.now().minusMonths(Integer.parseInt(ConfProperty.getIntegrationValue("PASTMONTH"))).withDayOfMonth(1);
                    lastMonthString = currentDate.format(formatter);
                    Log.consoleLog(ifr, "lastMonthString:" + lastMonthString);
                    Log.consoleLog(ifr, "[{\"" + ConfProperty.getIntegrationValue("PENSIONSEARCHKEYS") + "\":\"" + lastMonthString + "\"}]");
                    jsonArr = new org.json.JSONArray("[{\"" + ConfProperty.getIntegrationValue("PENSIONSEARCHKEYS") + "\":\"" + lastMonthString + "\"}]");
                    Log.consoleLog(ifr, "lastMonthString:jsonArr:" + jsonArr);
                    break;
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception:" + ex);
            Log.errorLog(ifr, "Exception:" + ex);
        }
        return jsonArr;
    }
}
