/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.common;

import com.newgen.dlp.commonobjects.api.MockDataLoader;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import static com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods.getDocumentInBase64Formate;
import com.newgen.iforms.properties.ConfProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.newgen.iforms.properties.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author ahmed.zindha
 */
public class APICommonMethods {

    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    MockDataLoader apimr = new MockDataLoader();

    public String GetHomeBranchCodeSHL(IFormReference ifr) {
        Log.consoleLog(ifr, "GetHomeBranchCodeSHL Started...");
        String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String zipAndNameBranchQ = "select TRIM(BRANCH_CODE), UPPER(TRIM(BRANCH_NAME)) FROM SLOS_STAFF_HOME_TRN where WINAME='"+pid+"'";
            Log.consoleLog(ifr, "zipAndNameBranchQuery==>" + zipAndNameBranchQ);
            List<List<String>> fetchedBranchQ = cf.mExecuteQuery(ifr, zipAndNameBranchQ, "Fetching zipcode and branch name to get actual branch code");
            String zipCode = "";
            String BRName = "";
            String BRCode = "";

            if (fetchedBranchQ.size() > 0) {
                zipCode = fetchedBranchQ.get(0).get(0);
                BRName = fetchedBranchQ.get(0).get(1);
            }
            Log.consoleLog(ifr, "zipCode==>" + zipCode);
            Log.consoleLog(ifr, "BRName==>" + BRName);

            String branchCodeQ = "SELECT TRIM(BRANCHCODE) FROM LOS_M_BRANCH WHERE TRIM(ZIPCODE)='"+zipCode+"' AND UPPER(TRIM(BRANCHNAME))='"+BRName+"'";
            Log.consoleLog(ifr, "branchCodeQuery==>" + branchCodeQ);
            List<List<String>> fetchedBranchCode = cf.mExecuteQuery(ifr, branchCodeQ, "Fetching actual branch code");

            if (fetchedBranchCode.size() > 0) {
                BRCode = fetchedBranchCode.get(0).get(0);
            }
            Log.consoleLog(ifr, "BRCode==>" + BRCode);


            if (BRCode.equalsIgnoreCase("")) {
                BRCode = "100";
            }

            return BRCode;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/GetHomeBranchCode=>..." + e);
        }

        return "";

    }
    
    public String getClobSnippetData(IFormReference ifr, String inputString) {

        try {

            if (inputString == null) {
                return "TO_CLOB('')";
            } else {

                Log.consoleLog(ifr, "Data.length()=>" + inputString.length());
                // String inputString = "Your input string goes here...";
                int maxLength = 4000;
                String Segment = "";
                if (inputString.length() > maxLength) {
                    int numChunks = (int) Math.ceil((double) inputString.length() / maxLength);
                    String[] chunks = new String[numChunks];

                    for (int i = 0; i < numChunks; i++) {
                        int startIndex = i * maxLength;
                        int endIndex = Math.min((i + 1) * maxLength, inputString.length());
                        chunks[i] = inputString.substring(startIndex, endIndex);
                    }

                    // Process or print the chunks as needed
                    for (String chunk : chunks) {
                        if (!Segment.equalsIgnoreCase("")) {
                            Segment = Segment + "||";
                        }

                        //   System.out.println(chunk.length() + "==>Chunk: " + chunk);
                        Segment = Segment + "TO_CLOB('" + chunk.replace("'", "") + "')";

                    }
                } else {
                    System.out.println("Input string is not greater than 4000 characters.");
                    Segment = Segment + "TO_CLOB('" + inputString.replace("'", "") + "')";
                }

                //System.out.println("Segment==>" + Segment);
                return Segment;

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }

        ///throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        return "";
        ///throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String CaptureRequestResponse(IFormReference ifr, String WorkItemName,
            String APIName, String Request, String Response, String ErrorCode,
            String ErrorMessage, String APIStatus) {
        try {
            //======================================================================//
            String PRODUCT_TYPE = "RETAIL";
            String TRANSACTION_ID = WorkItemName;
            String REQUEST = getClobSnippetData(ifr, Request);
            String RESPONSE = getClobSnippetData(ifr, Response);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String CurrentDateTime = dtf.format(now);
            Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);

            try {

                //For Individual WorkItem Reference
                String DelQuery1 = "DELETE FROM LOS_INTEGRATION_CBS_STATUS "
                        + "WHERE TRANSACTION_ID='" + TRANSACTION_ID + "' "
                        + "AND API_NAME='" + APIName + "'";
                Log.consoleLog(ifr, "DelQuery1==>" + DelQuery1);
                ifr.saveDataInDB(DelQuery1);

//                String Query1 = "INSERT INTO LOS_INTEGRATION_CBS_STATUS "
//                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
//                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
//                        + "VALUES('" + PRODUCT_TYPE + "',"
//                        + "'" + APIName + "',"
//                        + "'" + CurrentDateTime + "',"
//                        + "'" + TRANSACTION_ID + "',"
//                        + "" + REQUEST + ","
//                        + "" + RESPONSE + ","
//                        + "'" + ErrorCode + "',"
//                        + "'" + ErrorMessage + "',"
//                        + "'" + APIStatus + "')";
               /* String Query1 = "INSERT INTO LOS_INTEGRATION_CBS_STATUS "
                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
                        + "VALUES('" + PRODUCT_TYPE + "',"
                        + "'" + APIName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + TRANSACTION_ID + "',"
                        + "'',"
                        + "'',"
                        + "'" + ErrorCode + "',"
                        + "'" + ErrorMessage + "',"
                        + "'" + APIStatus + "')";*/
                
                String Query1 = "INSERT INTO LOS_INTEGRATION_CBS_STATUS "
                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
                        + "VALUES('" + PRODUCT_TYPE + "',"
                        + "'" + APIName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + TRANSACTION_ID + "',"
                        + "'',"
                        + "'',"
                        + "'" + ErrorCode + "',"
                        + "'" + ErrorMessage + "',"
                        + "'" + APIStatus + "')";
                
                Log.consoleLog(ifr, "Query1==>" + Query1);
                ifr.saveDataInDB(Query1);

                String Query = "INSERT INTO LOS_INTEGRATION_REQRES "
                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
                        + "VALUES('" + PRODUCT_TYPE + "',"
                        + "'" + APIName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + TRANSACTION_ID + "',"
                        + "" + REQUEST + ","
                        + "" + RESPONSE + ","
                        + "'" + ErrorCode.replace("'", "") + "',"
                        + "'" + ErrorMessage.replace("'", "") + "',"
                        + "'" + APIStatus + "')";

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

    public String CaptureNESLRequestResponse(IFormReference ifr, String WorkItemName,
            String APIName, String Request, String Response, String ErrorCode,
            String ErrorMessage, String APIStatus) {
        try {
            //======================================================================//
            String PRODUCT_TYPE = "RETAIL";
            String TRANSACTION_ID = WorkItemName;
//            String REQUEST = Request.replace("\"", "");
//            String RESPONSE = Response.replace("\"", "");
            String REQUEST = getClobSnippetData(ifr, Request);
            String RESPONSE = getClobSnippetData(ifr, Response);
            
			if (Response.equalsIgnoreCase("No response from Server")) {
				Response = "No response from Server";
				APIStatus = "FAIL";
			} 
			else if (Response.equalsIgnoreCase("Check NESL/Fintech whether it is down")) {
				Response = "Check NESL/Fintech whether it is down";
				APIStatus = "FAIL";
			} 
			else if (APIStatus != null && APIStatus.toLowerCase().contains("duplicate transaction")) {
			    ErrorMessage = APIStatus;   // keeps "Duplicate Transaction Details"
			    APIStatus = "FAIL";
			}
			else if (!RESPONSE.equalsIgnoreCase("{}")) {
				APIStatus = "SUCCESS";
			} else {
				// Response = "No response from Server";
				APIStatus = "FAIL";
			}
			
		
            
//            if (APIStatus.equalsIgnoreCase("FAIL")) {
//            	Response = "No response from Server";
//                APIStatus = "FAIL";
//            } else {
//            	 APIStatus = "SUCCESS";
//            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String CurrentDateTime = dtf.format(now);
            Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);

            try {
                String Query = "INSERT INTO LOS_INTEGRATION_REQRES "
                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
                        + "VALUES('" + PRODUCT_TYPE + "',"
                        + "'" + APIName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + TRANSACTION_ID + "',"
                        + "" + REQUEST + ","
                        + "" + RESPONSE + ","
                        + "'" + ErrorCode.replace("'", "") + "',"
                        + "'" + ErrorMessage.replace("'", "") + "',"
                        + "'" + APIStatus + "')";

                //Log.consoleLog(ifr, "Query==>" + Query);
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

    //Commented by Ahmed for reduncany 
//    public String CaptureExperianRequestResponse(IFormReference ifr, String WorkItemName,
//            String APIName, String Request, String Response, String ErrorCode,
//            String ErrorMessage, String APIStatus) {
//
//        Log.consoleLog(ifr, "#CaptureExperianRequestResponse...");
//
//        try {
//            //======================================================================//
//            String PRODUCT_TYPE = "RETAIL";
//            String TRANSACTION_ID = WorkItemName;
//            String REQUEST = getClobSnippetData(ifr, Request);
//            String RESPONSE = getClobSnippetData(ifr, Response);
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//            String CurrentDateTime = dtf.format(now);
//            Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);
//
//            try {
//                String Query = "INSERT INTO LOS_INTEGRATION_REQRES "
//                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
//                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
//                        + "VALUES('" + PRODUCT_TYPE + "',"
//                        + "'" + APIName + "',"
//                        + "'" + CurrentDateTime + "',"
//                        + "'" + TRANSACTION_ID + "',"
//                        + "" + REQUEST + ","
//                        + "" + RESPONSE + ","
//                        + "'" + ErrorCode.replace("'", "") + "',"
//                        + "'" + ErrorMessage.replace("'", "") + "',"
//                        + "'" + APIStatus + "')";
//
//                Log.consoleLog(ifr, "Query==>" + Query);
//                ifr.saveDataInDB(Query);
//
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception==>" + e);
//            }
//            //======================================================================//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception==>" + e);
//        }
//        return "";
//    }
//    public String CaptureTransunionRequestResponse(IFormReference ifr, String WorkItemName,
//            String APIName, String Request, String Response, String ErrorCode,
//            String ErrorMessage, String APIStatus) {
//        try {
//            //======================================================================//
//            String PRODUCT_TYPE = "RETAIL";
//            String TRANSACTION_ID = WorkItemName;
//            String REQUEST = getClobSnippetData(ifr, Request);
//            String RESPONSE = getClobSnippetData(ifr, Response);
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//            String CurrentDateTime = dtf.format(now);
//            Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);
//
//            try {
//                String Query = "INSERT INTO LOS_INTEGRATION_REQRES "
//                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
//                        + "RESPONSE,ERR_CODE,ERR_MSG,API_STATUS) "
//                        + "VALUES('" + PRODUCT_TYPE + "',"
//                        + "'" + APIName + "',"
//                        + "'" + CurrentDateTime + "',"
//                        + "'" + TRANSACTION_ID + "',"
//                        + "" + REQUEST + ","
//                        + "" + RESPONSE + ","
//                        + "'" + ErrorCode.replace("'", "") + "',"
//                        + "'" + ErrorMessage.replace("'", "") + "',"
//                        + "'" + APIStatus + "')";
//
//                //Log.consoleLog(ifr, "Query==>" + Query);
//                ifr.saveDataInDB(Query);
//
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception==>" + e);
//            }
//            //======================================================================//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception==>" + e);
//        }
//        return "";
//    }
    public String captureBRMSRuleHistory(IFormReference ifr, String WorkItemName,
            String RuleName, String Request, String Response, String ErrorCode,
            String ErrorMessage, String APIStatus) {
        try {
            Log.consoleLog(ifr, "#captureBRMSRuleHistory Starting...");

            //======================================================================//
            String PRODUCT_TYPE = "RETAIL";
            String TRANSACTION_ID = WorkItemName;
            String REQUEST = getClobSnippetData(ifr, Request);
            String RESPONSE = getClobSnippetData(ifr, Response);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String CurrentDateTime = dtf.format(now);
            Log.consoleLog(ifr, "CurrentDateTime==>" + CurrentDateTime);

            try {
                String Query = "INSERT INTO LOS_HIS_BRMSRULES "
                        + "(PRODUCT_TYPE,RULE_NAME,ENTRYDATETIME,PROCESSINSTANCEID,REQUEST,"
                        + "RESPONSE,RESPONSE_CODE,BRMS_OUTPUT) "
                        + "VALUES('" + PRODUCT_TYPE + "',"
                        + "'" + RuleName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + TRANSACTION_ID + "',"
                        + "" + REQUEST + ","
                        + "" + RESPONSE + ","
                        + "'" + ErrorCode.replace("'", "") + "',"
                        + "'" + APIStatus + "')";

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

    public String GetAPIErrorResponse(IFormReference ifr, String WorkItemName, JSONObject bodyObj) {
        Log.consoleLog(ifr, "#GetAPIErrorResponse..." + WorkItemName);
        try {
            JSONParser parser = new JSONParser();
            if (bodyObj.containsKey("Exception")) {
                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->In body obj..." + WorkItemName);
                String Exception = bodyObj.get("Exception").toString();
                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->Exception: " + Exception);

                JSONObject ExceptionJSON = (JSONObject) parser.parse(Exception);
                JSONObject ExceptionObj = new JSONObject(ExceptionJSON);

                String errorCode = "";
                String errorMessage = "";

                String reasonCode = "";
                String reasonMsg = "";

                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->Inside else");
                errorMessage = ExceptionObj.get("ErrorMessage").toString();
                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->ErrorMessage in else block: " + errorMessage);
                //Modified by Ahmed on 09-07-2024 for Get API Error Response:                
                //errorCode = ExceptionObj.get("ErrorCode").toString();
                if (!(cf.getJsonValue(ExceptionObj, "ErrorCode").equalsIgnoreCase(""))) {
                    errorCode = ExceptionObj.get("ErrorCode").toString();
                } else {
                    errorCode = ExceptionObj.get("errorCode").toString();
                }

                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->ErrorCode in else block: " + errorCode);
                Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->ErrorMessage: " + errorCode);
                if (errorMessage.equalsIgnoreCase("No data exists for this customer")) {
                    return errorCode + "#No data exists for this customer";
                }

                if (errorCode.equalsIgnoreCase("{}")){
                    String Reason = ExceptionObj.get("Reason").toString();
                    JSONObject ReasonJSONObj = (JSONObject) parser.parse(Reason);
                    Log.consoleLog(ifr, "APICommonMethods:GetAPIErrorResponse->ReasonJSONObj: " + ReasonJSONObj.size());
                    if (!ReasonJSONObj.isEmpty()) {
                        for (int i = 0; i < ReasonJSONObj.size() - 1; i++) {
                        	reasonMsg = ReasonJSONObj.get("Message").toString();
                            reasonCode = ReasonJSONObj.get("Code").toString();
                            
                        }

                        return reasonCode + "#" + reasonMsg;
                    }
                } else {
                    return errorCode + "#" + errorMessage;
                }
            }

            Log.consoleLog(ifr, "No Error Codes found on this API");
            return "true";
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            Log.errorLog(ifr, "Exception==>" + e);
        }
        return "false";
    }

//    Commented by Ahmed on 24-06-2024 for changing the pattern of ErrorResponse
//    public String GetAPIErrorResponse(IFormReference ifr, String WorkItemName, JSONObject bodyObj) {
//        Log.consoleLog(ifr, "#GetAPIErrorResponse..." + WorkItemName);
//        try {
//            JSONParser parser = new JSONParser();
//            if (bodyObj.containsKey("Exception")) {
//                Log.consoleLog(ifr, "#GetAPIErrorResponse1..." + WorkItemName);
//                String Exception = bodyObj.get("Exception").toString();
//                Log.consoleLog(ifr, "Exception==>" + Exception);
//
//                JSONObject ExceptionJSON = (JSONObject) parser.parse(Exception);
//                JSONObject ExceptionObj = new JSONObject(ExceptionJSON);
//
//                String Reason = ExceptionObj.get("Reason").toString();
//                Log.consoleLog(ifr, "Reason==>" + Reason);
//                JSONObject ReasonJSON = (JSONObject) parser.parse(Reason);
//                JSONObject ReasonJSONObj = new JSONObject(ReasonJSON);
//                Log.consoleLog(ifr, "ReasonJSONObj==>" + ReasonJSONObj.size());
//                String ErrorMessage = "";
//                String ErrorCode = "";
//                if (!ReasonJSONObj.isEmpty()) {
//                    for (int i = 0; i < ReasonJSONObj.size() - 1; i++) {
//                        ErrorMessage = ReasonJSONObj.get("Message").toString();
//                        ErrorCode = ReasonJSONObj.get("Code").toString();
//                    }
//                } else {
//                    ErrorMessage = ExceptionObj.get("ErrorMessage").toString();
//                    ErrorCode = ExceptionObj.get("ErrorCode").toString();
//                }
//                Log.consoleLog(ifr, "ErrorMessage==>" + ErrorMessage);
//                Log.consoleLog(ifr, "ErrorCode=====>" + ErrorCode);
//                return ErrorCode + "#" + ErrorMessage;
//            } else {
//                  System.out.println("No Error Codes found on this API");
//                Log.consoleLog(ifr, "No Error Codes found on this API");
//                return "true";
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception==>" + e);
//            Log.errorLog(ifr, "Exception==>" + e);
//        }
//        return "false";
//
//    }
    //Function Name modified by Ahmed on 01-07-2024 form Get_CBSExecutionStatus-->executeCBSStatus
    //public String Get_CBSExecutionStatus(IFormReference ifr, String ProcessInsatnceId, String APIName) {
    public String executeCBSStatus(IFormReference ifr, String ProcessInsatnceId, String apiNames) {
        Log.consoleLog(ifr, "executeCBSStatus Started...");
        try {
            String noofAPIs = "1";
            if (apiNames.contains(",")) {
                String[] APIList = apiNames.split(",");
                noofAPIs = String.valueOf(APIList.length);
            }
            Log.consoleLog(ifr, "NoofAPIs==>" + noofAPIs);
            //Modified by Ahmed on 04-07-2024 for handling ERROR/FAIL in APIStatus
//            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE API_NAME IN "
//                    + "(" + APIName + ") AND TRANSACTION_ID='" + ProcessInsatnceId + "' and API_STATUS!='FAIL'";

            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE API_NAME IN "
                    + "(" + apiNames + ") AND TRANSACTION_ID='" + ProcessInsatnceId + "' "
                    + "and API_STATUS IN ('SUCCESS')";

            List<List<String>> CountOutput1 = cf.mExecuteQuery(ifr, Query1, "Checking Count");
            if (CountOutput1.get(0).get(0).equalsIgnoreCase(noofAPIs)) {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  Get_CBSExecutionStatus" + e.getMessage());
            Log.errorLog(ifr, "Exception in  Get_CBSExecutionStatus" + e.getMessage());
            return "error, Database error occured please try after sometime";
        }
        return "FAIL";
    }

    public String GetHomeBranchCode(IFormReference ifr, String ProcessInstanceId, String ProductType) {
        Log.consoleLog(ifr, "GetHomeBranchCode Started...");
        Log.consoleLog(ifr, "ProductType=====>" + ProductType);
        try {
            //Modified by Ahmed on 26-07-2024  for queryReadingFromProp
            String Query = "";
//            if (ProductType.equalsIgnoreCase("PAPL")) {
//                Query = "SELECT BRANCHCODE FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                        + "WHERE WINAME='" + ProcessInstanceId + "' AND ROWNUM=1";
//            } else {
////                Query = "select BRANCHCODE from LOS_L_SOURCINGINFO where "
////                        + "PID='" + ProcessInstanceId + "' AND ROWNUM=1";
//                Query = "select BRANCHCODE from LOS_NL_CASA_ASSET_VAL where pid='" + ProcessInstanceId + "'";
//
//            }

            if (ProductType.equalsIgnoreCase("PAPL")) {

//                Query = "SELECT BRANCHCODE FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                        + "WHERE WINAME='" + ProcessInstanceId + "' AND ROWNUM=1";
                Query = ConfProperty.getQueryScript("PAPL_GETHOMEBRCODEQRY")
                        .replace("#WINAME#", ProcessInstanceId);

            } else if (ProductType.equalsIgnoreCase("LAD")) {//Modified by Ahmed on 23-07-2024
//                Query = "select BRANCHCODE from LOS_L_SOURCINGINFO where "
//                        + "PID='" + ProcessInstanceId + "' AND ROWNUM=1";
                Query = ConfProperty.getQueryScript("LAD_GETHOMEBRCODEQRY")
                        .replace("#WINAME#", ProcessInstanceId);

            } else {
                //  Query = "select BRANCHCODE from LOS_NL_CASA_ASSET_VAL where pid='" + ProcessInstanceId + "'";
                Query = ConfProperty.getQueryScript("GETHOMEBRCODEQRY")
                        .replace("#WINAME#", ProcessInstanceId);
            }

            Log.consoleLog(ifr, "Query==>" + Query);
            List Result = ifr.getDataFromDB(Query);
            String BRCode = Result.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "BRCode==>" + BRCode);

            if (BRCode.equalsIgnoreCase("")) {
                BRCode = "100";
            }

            return BRCode;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/GetHomeBranchCode=>..." + e);
        }

        return "";

    }

    public JSONObject getDownloadParams(IFormReference ifr, String Response) {
        try {
            JSONObject jobj = new JSONObject();

            JSONParser parser = new JSONParser();
            JSONObject resultObj = (JSONObject) parser.parse(Response);

            String NGOGetDocumentBDOResponse = resultObj.get("NGOGetDocumentBDOResponse").toString();
            JSONObject NGOGetDocumentBDOResponseObj = (JSONObject) parser.parse(NGOGetDocumentBDOResponse);
            Log.consoleLog(ifr, "NGOGetDocumentBDOResponse==>" + NGOGetDocumentBDOResponse);

            String docContent = NGOGetDocumentBDOResponseObj.get("docContent").toString();
            String documentName = NGOGetDocumentBDOResponseObj.get("documentName").toString();
            String createdByAppName = NGOGetDocumentBDOResponseObj.get("createdByAppName").toString();
            String statusCode = NGOGetDocumentBDOResponseObj.get("statusCode").toString();

            Log.consoleLog(ifr, "docContent==>" + docContent);
            Log.consoleLog(ifr, "documentName==>" + documentName);
            Log.consoleLog(ifr, "createdByAppName==>" + createdByAppName);
            Log.consoleLog(ifr, "statusCode==>" + statusCode);

            jobj.put("docContent", docContent);
            jobj.put("documentName", documentName);
            jobj.put("createdByAppName", createdByAppName);
            jobj.put("statusCode", statusCode);

            Log.consoleLog(ifr, "jobj==>" + jobj.toString());

            return jobj;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getDownloadParams=>..." + e);
        }

        return null;
    }

    public String getAPIParams(IFormReference ifr, String apiName) {

        Log.consoleLog(ifr, "#getAPIParams Started....APIName==>" + apiName);

        try {
            String productParams = pcm.mGetProductParams(ifr);
            Log.consoleLog(ifr, "productParams=>" + productParams);

            if ((!productParams.equalsIgnoreCase(""))
                    || (!productParams.contains("#"))) {

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getAPIParams=>..." + e);
        }

        return "";
    }

    public String getWebServiceResponse(IFormReference ifr, String apiName, String request) {
    	 
        HashMap<String, String> requestHeader = new HashMap<>();
        //Added by Ahmed on 23-07-2024 for passing WorkItemNumber on API Headers
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        requestHeader.put("workitemid", processInstanceId);
 
        String response = "";
        Log.consoleLog(ifr, "Request===>" + request);
         // Specify the path to the properties file
        //Modified by Ahmed on 03/05/2024 to handle null pointer experession.
        String chkMockFlg = ConfProperty.getIntegrationValue("MOCKFLG_" + apiName);
        Log.consoleLog(ifr, "chkMockFlg===>" + chkMockFlg);
        chkMockFlg = (chkMockFlg != null) ? chkMockFlg : "";
        Log.consoleLog(ifr, "chkMockFlg===>" + chkMockFlg);
        if (chkMockFlg.equalsIgnoreCase("Y")) {
            response = apimr.readMockResponse(ifr, apiName);
        } else {
            response = cf.CallWebService(ifr, apiName, request, "", requestHeader);
        }
        Log.consoleLog(ifr, "Response===>" + response);
        return response;
    }
//    public String getWebServiceResponse(IFormReference ifr, String apiName, String request) {
//
//        HashMap<String, String> requestHeader = new HashMap<>();
//        String response = "";
//        Log.consoleLog(ifr, "Request===>" + request);
//        if (ConfProperty.getIntegrationValue("MOCKFLG_" + apiName).equalsIgnoreCase("Y")) {
//            response = apimr.readMockResponse(ifr, apiName);
//        } else {
//            response = cf.CallWebService(ifr, apiName, request, "", requestHeader);
//        }
//        Log.consoleLog(ifr, "Response===>" + response);
//        return response;
//    }
    public String generateReport(IFormReference ifr, String processInstanceId,
            String reportType, String base64Content, String apiService) {
        Log.consoleLog(ifr, "#generateReport starting..");
        try {

            String clientId = ConfProperty.getIntegrationValue("NGREPORTTOOL_CLIENTID");
            String clientSecret = ConfProperty.getIntegrationValue("NGREPORTTOOL_CLIENTSECRET");
            String sessionId = ifr.getObjGeneralData().getM_strDMSSessionId();

            String request = "{\n"
                    + "    \"pid\": \"" + processInstanceId + "\",\n"
                    + "    \"reportType\": \"" + reportType + "\",\n"
                    + "    \"encodedstring\": \"" + base64Content + "\"\n"
                    + "}";

            HashMap<String, String> requestHeader = new HashMap<>();
            requestHeader.put("Content-Type", "application/json");
            requestHeader.put("clientId", clientId);
            requestHeader.put("clientSecret", clientSecret);
            requestHeader.put("sessionId", sessionId);//Added by Ahmed on 22/05/2024 for redirecting the sessionId from one War to another War
            Log.consoleLog(ifr, "request====>" + request);
            String response = cf.CallWebService(ifr, apiService, request, "", requestHeader);
            Log.consoleLog(ifr, "response===>" + response);
            return response;

        } catch (Exception e) {
            Log.consoleLog(ifr, "#generateReport/Exception==>" + e);
        }
        return "";

    }

    //Added by Ahmed on 24-06-2024 for getting SelectedBranchCode
    public String getHomeBranchCode(IFormReference ifr, String processInstanceId, String journeyType, String accountNumber) {
        Log.consoleLog(ifr, "getHomeBranchCode Started...");
        Log.consoleLog(ifr, "journeyType=====>" + journeyType);

        String branchCode = "";
        try {
            String queryBranchCode = "";
            if (journeyType.equalsIgnoreCase("PAPL")) {

//                queryBranchCode = "SELECT BRANCHCODE FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                        + "WHERE WINAME='" + processInstanceId + "' AND ROWNUM=1";
                queryBranchCode = ConfProperty.getQueryScript("PAPL_GETHOMEBRCODEQRY")
                        .replace("#WINAME#", processInstanceId);
                Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
                List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
                if (!result.isEmpty()) {
                    branchCode = result.get(0).get(0);
                }
            } else {

                //queryBranchCode = "select BRANCHCODE from LOS_NL_CASA_ASSET_VAL where pid='" + processInstanceId + "'";
                queryBranchCode = ConfProperty.getQueryScript("GETHOMEBRCODEQRY")
                        .replace("#WINAME#", processInstanceId);
                Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
                List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
                if (!result.isEmpty()) {
                    branchCode = result.get(0).get(0);
                }
                // branchCode = pcm.getSelectedBranchCode(ifr, accountNumber);/Commented by Ahmed on 01-07-2024 due to BrCode saving in Table
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getHomeBranchCode=>..." + e);
        }
        Log.consoleLog(ifr, "branchCode=====>" + branchCode);
        return branchCode;
    }

    public String getClientTransUniqueId(IFormReference ifr) {
        try {

            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String clientTransId = processInstanceId.replace("-", "");
            return clientTransId;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getClientTransUniqueId=>..." + e);
        }
        return RLOS_Constants.ERROR;
    }

    public String getWebServiceIntegrationResponse(IFormReference ifr, String processInstanceId, String request, String integrationURL) {

        try {

            StringBuilder response = new StringBuilder();
            Log.consoleLog(ifr, "request==>" + request);

            URL url = new URL(integrationURL);
            Log.consoleLog(ifr, "url==>" + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.consoleLog(ifr, "conn==>" + conn);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            Log.consoleLog(ifr, "conn.getOutputStream()" + conn.getOutputStream());

            try ( OutputStream os = conn.getOutputStream()) {
                byte[] input = request.getBytes("UTF-8");
                os.write(input, 0, input.length);
                Log.consoleLog(ifr, "Input==>" + os.toString());
            }

            try ( BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            conn.disconnect();
            Log.consoleLog(ifr, "Output==>" + response.toString());

            return response.toString();

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/GetIntegrationResponse===>" + e);
        }

        return "";

    }

    public String checkPerfiosCompletedTRNStatus(IFormReference ifr, String serviceType, String criteria) {

        try {
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String queryGetStatus = "SELECT COUNT(*) FROM LOS_TRN_PERF_INTEGRATION_DETAILS "
                    + "WHERE PROCESSINSTANCEID='" + processInstanceId + "' AND "
                    + "PERFIOS_SERVICETYPE='" + serviceType + "' "
                    + "AND TRANSACTION_STATUS IN (" + criteria + ")";
            Log.consoleLog(ifr, "queryGetStatus===>" + queryGetStatus);
            List< List< String>> Result = ifr.getDataFromDB(queryGetStatus);
            Log.consoleLog(ifr, "Result===>" + Result.toString());
            String completedTRNStatusCount = "0";
            if (!Result.isEmpty()) {
                completedTRNStatusCount = Result.get(0).get(0);
            }
            return completedTRNStatusCount;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkPerfiosCompletedTRNStatus==>" + e);
        }

        return RLOS_Constants.ERROR;

    }

    public String checkCurrentPerfiosTRNStatus(IFormReference ifr, String processInstanceId,
            String serviceType, String inCriterion, String NotInCriterion) {

        try {
            String queryGetStatus = "SELECT COUNT(*) FROM LOS_TRN_PERF_INTEGRATION_DETAILS "
                    + "WHERE PROCESSINSTANCEID='" + processInstanceId + "' AND "
                    + "PERFIOS_SERVICETYPE='" + serviceType + "' "
                    + "AND TRANSACTION_STATUS IN (" + inCriterion + ") "
                    + "AND TRANSACTION_STATUS NOT IN (" + NotInCriterion + ")";
            Log.consoleLog(ifr, "queryGetStatus===>" + queryGetStatus);
            List< List< String>> Result = ifr.getDataFromDB(queryGetStatus);
            Log.consoleLog(ifr, "Result===>" + Result.toString());
            String transactionCount = "0";
            if (!Result.isEmpty()) {
                transactionCount = Result.get(0).get(0);
            }
            return transactionCount;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/checkTransactionStatus==>" + e);
        }

        return RLOS_Constants.ERROR;

    }

    public String getPerfiosTransId(IFormReference ifr, String processInstanceId, String perfiosServiceType) {

        String query = "SELECT PERFIOS_TRANSACTIONID FROM LOS_TRN_PERF_INTEGRATION_DETAILS "
                + "WHERE PROCESSINSTANCEID='" + processInstanceId + "' "
                + "AND  PERFIOS_SERVICETYPE='" + perfiosServiceType + "' AND ROWNUM=1";

        String perfTransId = "";
        List< List< String>> Result = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        if (!Result.isEmpty()) {
            perfTransId = Result.get(0).get(0);
        }
        return perfTransId;

    }

    public String getNoofDocumentsInWorkItem(IFormReference ifr, String documentName) {
        Log.consoleLog(ifr, "==============getNoofDocumentsInWorkItem===================");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String queryGetNoofDocs = "SELECT COUNT(*) FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN "
                + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN "
                + "(SELECT FOLDERINDEX FROM PDBFOLDER WHERE NAME='" + processInstanceId + "')) "
                + "AND NAME LIKE '%" + documentName + "%'";

        String count = "0";
        List< List< String>> Result = ifr.getDataFromDB(queryGetNoofDocs);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        if (!Result.isEmpty()) {
            count = Result.get(0).get(0);
        }
        return count;

    }

    public String getDocumentIndexFromWorkItem(IFormReference ifr, String documentName) {
        Log.consoleLog(ifr, "==============getDocumentIndexFromWorkItem===================");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String queryDocIndexes = "SELECT DOCUMENTINDEX FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN "
                + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN "
                + "(SELECT FOLDERINDEX FROM PDBFOLDER WHERE NAME='" + processInstanceId + "')) "
                + "AND NAME LIKE '%" + documentName + "%'";

        Log.consoleLog(ifr, "queryDocIndexes===>" + queryDocIndexes);
        List< List< String>> Result = ifr.getDataFromDB(queryDocIndexes);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        String docIndexes = "";
        if (!Result.isEmpty()) {

            for (int i = 0; i < Result.size(); i++) {
                if (!docIndexes.equalsIgnoreCase("")) {
                    docIndexes = docIndexes + ",";
                }
                String docIndex = Result.get(i).get(0);
                docIndexes = docIndexes + docIndex;
            }

        }
        return docIndexes;
    }

    public String downloadDocFromWorkItem(IFormReference ifr,
            String processInstanceId, String documentIndex, String filePath) {

        Log.consoleLog(ifr, "============downloadDocFromWorkItem started==============");
        try {

            String url = pcm.getConstantValue(ifr, "ODDOWNLOAD", "URL");

            String jsonin = "{\n" + "    \"NGOGetDocumentBDO\": {\n" + "        \"cabinetName\": \""
                    + ifr.getCabinetName() + "\",\n" + "        \"siteId\": \"1\",\n" + "        \"volumeId\": \"1\",\n"
                    + "        \"userName\": \"\",\n" + "        \"userPassword\": \"\",\n" + "        \"userDBId\": \""
                    + ifr.getObjGeneralData().getM_strDMSSessionId() + "\",\n" + "        \"locale\": \"en_US\",\n"
                    + "        \"passAlgoType\": \"\",\n" + "        \"encrFlag\": \"\",\n" + "        \"docIndex\": \""
                    + documentIndex + "\"\n" + "    }\n" + "}";

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonin);
            Log.consoleLog(ifr, "jsonObject :- " + jsonObject.toString());
            String response = getDocumentInBase64Formate(ifr, jsonObject, url, processInstanceId);
            Log.consoleLog(ifr, "OD get Document response " + response);
            JSONObject resultObj = (JSONObject) parser.parse(response);

            String NGOGetDocumentBDOResponse = resultObj.get("NGOGetDocumentBDOResponse").toString();
            JSONObject NGOGetDocumentBDOResponseObj = (JSONObject) parser.parse(NGOGetDocumentBDOResponse);
            Log.consoleLog(ifr, "NGOGetDocumentBDOResponse==>" + NGOGetDocumentBDOResponse);

            String docContent = NGOGetDocumentBDOResponseObj.get("docContent").toString();
            String documentName = NGOGetDocumentBDOResponseObj.get("documentName").toString();
            String createdByAppName = NGOGetDocumentBDOResponseObj.get("createdByAppName").toString();
            String statusCode = NGOGetDocumentBDOResponseObj.get("statusCode").toString();

            Log.consoleLog(ifr, "docContent==>" + docContent);
            Log.consoleLog(ifr, "documentName==>" + documentName);
            Log.consoleLog(ifr, "createdByAppName==>" + createdByAppName);
            Log.consoleLog(ifr, "statusCode==>" + statusCode);

            byte[] decodedBytes = Base64.getDecoder().decode(docContent);
            String outputFile = System.getProperty("user.dir") + File.separator + filePath;

            File outDir = new File(outputFile);
            if (!outDir.isDirectory()) {
                outDir.mkdir();
            }

            outputFile = outputFile + File.separator + documentName + ".pdf";

            try ( FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(decodedBytes);
                Log.consoleLog(ifr, "File saved: " + outputFile);
                return outputFile;
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception/File saving=>" + e);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/downloadDocFromWorkItem=>" + e);
        }

        return RLOS_Constants.ERROR;
    }

    public String bypassPerfiosTransaction(IFormReference ifr, String perfServType) {
        Log.consoleLog(ifr, "============bypassPerfiosTransaction started==============");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String queryBypassTrn = "INSERT INTO LOS_TRN_PERF_INTEGRATION_DETAILS  (PRODUCT,PROCESSINSTANCEID,PERFIOS_TRANSACTIONID,ENTRYDATETIME,TRANSACTION_STATUS,PERFIOS_SERVICETYPE) \n"
                + "VALUES ('RLOS','" + processInstanceId + "','" + perfServType + "_BYPASSED',SYSDATE,'COMPLETED','" + perfServType + "')";
        Log.consoleLog(ifr, "queryBypassTrn==>" + queryBypassTrn);
        ifr.saveDataInDB(queryBypassTrn);
        return RLOS_Constants.SUCCESS;
    }

    //Added by Ahmed on 28-06-2024 for adding applicantType on req_res
    public String captureCICRequestResponse(IFormReference ifr, String processInstanceId,
            String apiName, String request, String response, String errorCode,
            String errorMessage, String apiStatus, String applicantType) {
        try {
            Log.consoleLog(ifr, "#captureCICRequestResponse started..");

            String productType = "RETAIL";
            String clobRequest = getClobSnippetData(ifr, request);
            String clobResponse = getClobSnippetData(ifr, response);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String CurrentDateTime = dtf.format(now);

            try {
                String Query = "INSERT INTO LOS_INTEGRATION_REQRES "
                        + "(PRODUCT_TYPE,API_NAME,ENTRYDATETIME,TRANSACTION_ID,REQUEST,"
                        + "RESPONSE,ERR_CODE,ERR_MSG,APPLICANT_TYPE,API_STATUS) "
                        + "VALUES('" + productType + "',"
                        + "'" + apiName + "',"
                        + "'" + CurrentDateTime + "',"
                        + "'" + processInstanceId + "',"
                        + "" + clobRequest + ","
                        + "" + clobResponse + ","
                        + "'" + errorCode + "',"
                        + "'" + errorMessage + "',"
                        + "'" + applicantType + "',"
                        + "'" + apiStatus + "')";
                ifr.saveDataInDB(Query);
                Log.consoleLog(ifr, "#captureCICRequestResponse Ended");
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception==>" + e);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }
        return "";
    }

    public String getCBSProductCode(IFormReference ifr) {

        String cbsProductCode = "";
        String schemeCode = getSchemeCode(ifr);
        String queryCbsProductCode = "select cbsproductcode from los_m_product_rlos  where schemeid='" + schemeCode + "'";
        Log.consoleLog(ifr, "APICommonMethods:getCBSProductCode->queryCbsProductCode:: ==> " + queryCbsProductCode);
        try {
            List<List<String>> queryResult = ifr.getDataFromDB(queryCbsProductCode);
            if (queryResult.size() > 0) {
                cbsProductCode = queryResult.get(0).get(0);
            }
            Log.consoleLog(ifr, "APICommonMethods:getCBSProductCode->CBSProductCode: " + cbsProductCode);
        } catch (Exception e) {
            Log.consoleLog(ifr, "APICommonMethods:getCBSProductCode->Exception block: " + e);
        }
        return cbsProductCode;
    }

    public String getSchemeCode(IFormReference ifr) {

        String schemeCode = "";
        String workItemNo = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "APICommonMethods:getSchemeCode->workItemNo: " + workItemNo);

        String querySchemeCode = "select r.schemeid from los_m_product_rlos r, alos_ext_table e "
                + "where r.productcode= e.producttype and r.subproductcode=e.subproducttype "
                + "and r.purposecode = NVL(e.purposecode,'NA') and r.variantcode = NVL(e.variantcode,'NA') "
                + "and r.isactive='Y' "
                + "and e.pid='" + workItemNo + "'";
        Log.consoleLog(ifr, "APICommonMethods:getSchemeCode->querySchemeCode:: ==> " + querySchemeCode);
        try {
            List<List<String>> queryResult = ifr.getDataFromDB(querySchemeCode);
            Log.consoleLog(ifr, "APICommonMethods:getSchemeCode->queryResult.size():: ==> " + queryResult.size());
            if (queryResult.size() > 0) {
                schemeCode = queryResult.get(0).get(0);
            }
            Log.consoleLog(ifr, "APICommonMethods:getCBSProductCode->SchemeCode: " + schemeCode);
        } catch (Exception e) {
            Log.consoleLog(ifr, "APICommonMethods:getSchemeCode->Exception block: " + e);
        }
        return schemeCode;
    }

    //Added by Ahmed on 26-07-2024 for CIC Report Updation Status
    public void updateCICReportStatus(IFormReference ifr, String cicType, String generateReportStatus, String appType) {
        Log.consoleLog(ifr, "APICommonMethods:updateCICReportStatus->Start: ");
        try {
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            if (!generateReportStatus.equalsIgnoreCase(null)
                    && !generateReportStatus.equalsIgnoreCase("")
                    && !generateReportStatus.equalsIgnoreCase("{}")) {

                JSONParser cicparser = new JSONParser();
                JSONObject cicresultObj = (JSONObject) cicparser.parse(generateReportStatus);
                String documentType = cicresultObj.get("documentType").toString();
                String uploadStatus = cicresultObj.get("uploadStatus").toString();
                String documentId = cicresultObj.get("documentId").toString();
                String documentName = cicresultObj.get("documentName").toString();
                String documentSize = cicresultObj.get("documentSize").toString();
                String message = cicresultObj.get("message").toString();
                String documentIndex = cicresultObj.get("documentIndex").toString();
                String status = cicresultObj.get("status").toString();

                Log.consoleLog(ifr, "uploadStatus==>" + uploadStatus);
                Log.consoleLog(ifr, "documentIndex==>" + documentIndex);
                Log.consoleLog(ifr, "documentId=====>" + documentId);
                Log.consoleLog(ifr, "documentName===>" + documentName);
                Log.consoleLog(ifr, "documentType===>" + documentType);
                Log.consoleLog(ifr, "documentSize===>" + documentSize);
                Log.consoleLog(ifr, "status===>" + status);

                String applicantType = "";
                if (appType.equalsIgnoreCase("B")) {
                    applicantType = "Borrower";
                } else {
                    applicantType = "CoBorrower";
                }

                String docName = applicantType + "_" + cicType + "_Report";

                String queryCICDocStatusInsert = "INSERT INTO LOS_NL_GENERATE_DOCUMENT (INSERTIONORDERID,PID,DOCUMENTINDEX,DOCUMENTNAME,GENERATEDBY,GENERATEDDATE,DMSNAME,DOCUMENTSTATUS,DOCSTAGENAME) \n"
                        + "VALUES (LOS_NL_GENERATE_DOCUMENT_seq.NEXTVAL,'" + processInstanceId + "','" + documentIndex + "','" + docName + "','SYSTEM',SYSDATE,'" + documentName + "','Generated','PORTAL')";
                Log.consoleLog(ifr, "queryCICDocStatusInsert===>" + queryCICDocStatusInsert);
                cf.mExecuteQuery(ifr, queryCICDocStatusInsert, "queryCICDocStatusInsert");

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "APICommonMethods:updateCICReportStatus->Exception block: " + e);
        }

    }

    public String getCBSExternalReferenceNo() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
        String formattedDate = dateFormat.format(currentDate);
        return formattedDate;
    }
 public String returnErrorForStaff(IFormReference ifr, String response) {
        CommonFunctionality cf = new CommonFunctionality();
        JSONObject message = new JSONObject();
        message.put("showMessage", cf.showMessage(ifr, "", "error", response));
        return message.toString();
    }

 public String getConstantValue(IFormReference ifr, String constType, String constName) {
     String query = "SELECT CONSTVALUE FROM LOS_MST_CONSTANTS WHERE CONSTTYPE='" + constType + "' AND CONSTNAME='" + constName + "'";
     Log.consoleLog(ifr, "query" + query);
     List<List<String>> result = ifr.getDataFromDB(query);
     if (result.size() > 0 && result.get(0).get(0) != null) {
         Log.consoleLog(ifr, "result" + result.get(0).get(0));
         return result.get(0).get(0);
     }
     Log.consoleLog(ifr, "result" + result.get(0).get(0));
     return "0";
 }

 public String getCurrentAPIDate(IFormReference ifr) {
     Date d = new Date();
     SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
     String APIDate = sd.format(d);
     Log.consoleLog(ifr, "APIDate:" + APIDate);
     return APIDate;
 }

 public String datNextDueforCollater(IFormReference ifr, String sancDate) {
     Date currentDate = new Date();
     String APIDate = "";
     Log.consoleLog(ifr, "getDisbursementDate:::sancDate " + sancDate);

     try {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(currentDate);
         calendar.add(Calendar.MONTH, 12);
         calendar.add(Calendar.DATE, -1); // Subtracting one day
         Date newDate = calendar.getTime();
         SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
         APIDate = sd.format(newDate);
         Log.consoleLog(ifr, "APIDate:" + APIDate);
     } catch (Exception e) {
         Log.consoleLog(ifr, "Exception " + e);
     }

     return APIDate;

 }

 public String Get_CBSExecutionStatus(IFormReference ifr, String ProcessInsatnceId, String APIName) {
     Log.consoleLog(ifr, "Get_CBSExecutionStatus Started...");
     try {
         String NoofAPIs = "1";
         if (APIName.contains(",")) {
             String[] APIList = APIName.split(",");
             NoofAPIs = String.valueOf(APIList.length);
         }
         Log.consoleLog(ifr, "NoofAPIs==>" + NoofAPIs);
         String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE API_NAME IN "
                 + "(" + APIName + ") AND TRANSACTION_ID='" + ProcessInsatnceId + "' and API_STATUS!='FAIL'";
         
         List<List<String>> CountOutput1 = cf.mExecuteQuery(ifr, Query1, "Checking Count");
         if (CountOutput1.get(0).get(0).equalsIgnoreCase(NoofAPIs)) {
             return RLOS_Constants.SUCCESS;
         }
     } catch (Exception e) {
         Log.consoleLog(ifr, "Exception in  Get_CBSExecutionStatus" + e.getMessage());
         Log.errorLog(ifr, "Exception in  Get_CBSExecutionStatus" + e.getMessage());
     }
     return "FAIL";
 }

 public String getConfigValue(IFormReference ifr, String productCode, String subProductCode, String paramType, String paramName) {
     String query = "select PARAMVALUE from los_mst_configs where "
             + "productcode='" + productCode + "' AND PARAMTYPE='" + paramType + "' AND PARAMNAME='" + paramName + "'";
     List<List<String>> result = ifr.getDataFromDB(query);
     if (result.size() > 0 && result.get(0).get(0) != null) {
         return result.get(0).get(0);
     }
     return "0";
 }

 public String getDisbAccountNoGold(IFormReference ifr) {
     String accNoData = "SELECT DISBURSE_TO FROM SLOS_STAFF_JEWELLERY_DETAILS "
             + "WHERE WiNAME='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
     List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, accNoData, "accNoData:");
     if (loanAccNo.size() > 0) {
         return loanAccNo.get(0).get(0);
     }
     return "";
 }
}
