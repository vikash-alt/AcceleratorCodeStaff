package com.newgen.dlp.homeLoan;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author skalidindi
 */
public class PensionApi {
     APICommonMethods cm = new APICommonMethods();
    public String pension(IFormReference ifr,String customerId){
       String apiName = "pension";
       String apiErrorMessage="";
       String apiErrorCode = "";
       String apiStatus="";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String response="";
        String request="";
        JSONObject pensionData=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try{
            request = "{\n" + "        \"Request\": \"" + customerId + "\"\n" + "}";
            response = cm.getWebServiceResponse(ifr, apiName, request);
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
//                JSONObject resultObj = (JSONObject) parser.parse(response);
                JSONObject getBody = (JSONObject) parser.parse(response);

             // First level body
             JSONObject body1 = (JSONObject) getBody.get("body");

             // Second level body
             JSONObject body2 = (JSONObject) body1.get("body");

             // Get status
             JSONObject status = (JSONObject) body2.get("status");
             String getStatusCode = status.get("statusCode").toString();
             String getStatusDesc = status.get("statusDesc").toString();

             if(getStatusCode.equalsIgnoreCase("no success")
                && getStatusDesc.equalsIgnoreCase("data not found")) {
                 return RLOS_Constants.ERROR + "," + AccelatorStaffConstant.PENSION_API;
             }

             // Get data array
             JSONArray jsonDataArray = (JSONArray) body2.get("data");
             Log.consoleLog(ifr, "jsonDataArray: " + jsonDataArray);
                Log.consoleLog(ifr, "jsonDataArray" +jsonDataArray);
                int len=jsonDataArray.size();
                Log.consoleLog(ifr, "length" +len);
                 for(int i=0;i<len && i<1;i++){
                   JSONObject  tempJsonObj=(JSONObject)  jsonDataArray.get(i);
                   pensionData.put("Name",tempJsonObj.get("Exstaff_name"));
                   Log.consoleLog(ifr, "pensionData" +pensionData);
                   pensionData.put("empId", tempJsonObj.get("Exstaff_employee_number"));
                   pensionData.put("ppoNumber", tempJsonObj.get("ppo_number"));
                   pensionData.put("designation",tempJsonObj.get("Exstaff_designation"));
                   pensionData.put("dateOfRetirement", tempJsonObj.get("Exstaff_date_of_retirement"));
                   pensionData.put("age", tempJsonObj.get("age"));
                   pensionData.put("dateOfBirth",tempJsonObj.get("date_of_birth"));
                   pensionData.put("pan", tempJsonObj.get("pan"));
                   pensionData.put("aadharNo", tempJsonObj.get("aadhar_id"));
                   pensionData.put("pensionType",tempJsonObj.get("pension_type"));
                   pensionData.put("accNumber",tempJsonObj.get("account_number"));
                   pensionData.put("penMonth",tempJsonObj.get("pen_month"));
                   pensionData.put("empId", tempJsonObj.get("Exstaff_employee_number"));
                   pensionData.put("ppoNumber", tempJsonObj.get("ppo_number"));
                   pensionData.put("penMonth",tempJsonObj.get("pen_month"));
                   pensionData.put("grossPension",tempJsonObj.get("gross_pension"));
                   pensionData.put("netPension", tempJsonObj.get("net_pension"));
                   pensionData.put("Warning","NoError");
                 }
                 return pensionData.toString();
            }else{
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
            }
            if (apiErrorMessage.equalsIgnoreCase("")) {
                apiStatus = RLOS_Constants.SUCCESS;
            } else {
                apiStatus = RLOS_Constants.ERROR;
            }
             if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }else{
                  JSONObject jsonObject = new JSONObject();
                    return jsonObject.toJSONString();
              }
        }catch(Exception e){
            
        }
    finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }
    
}
