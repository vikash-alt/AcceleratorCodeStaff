package com.newgen.iforms.hrms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;

import com.newgen.dlp.commonobjects.util.PropertyReader;
import com.newgen.dlp.integration.common.APICommonMethods;

import java.util.Optional;

/**
 *
 * @author sanparmar
 */
public class EmailAndSms {

    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();

    public void triggerEmail(IFormReference ifr,
            String triggerStage, String triggerSubject, String triggerBody) {
        try {

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = "";
            String mobileNumber = getMobileNumber(ifr);

            String emailId = getCurrentEmailId(ifr, mobileNumber);

            if (!emailId.equalsIgnoreCase("")) {
                sendEmail(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), emailId, triggerStage, triggerSubject, triggerBody);
            } else {
                Log.consoleLog(ifr, "Email ID is blank Gold");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Inside triggerCCMAPIs Gold" + e);
        }
        Log.consoleLog(ifr, "#triggerCCMAPIs Gold Ended....");
    }

    public String getCurrentEmailId(IFormReference ifr, String MobileNo) {

        String eMailId = "";
        try {
            String Query = "";

            Query = "select emailid from alos_trn_customersummary where mobilenumber='" + MobileNo + "'";

            List< List< String>> Result = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());
            if (Result.size() > 0) {
                eMailId = Result.get(0).get(0);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getCurrentEmailId==>" + e);
        }
        return eMailId;

    }

    public String sendEmail(IFormReference ifr, String ProcessInstanceId, String emailId, String emailStage, String emailSubject, String emailBody) {
        Log.consoleLog(ifr, "Entered into Gold sendEmail...");
        Log.consoleLog(ifr, "sendEmail/EmailStage==>" + emailStage);
        Log.consoleLog(ifr, "sendEmail/emailSubject==>" + emailSubject);
        Log.consoleLog(ifr, "sendEmail/emailBody==>" + emailBody);
//        String emailEnabled = PropertyReader.getInstance().getProperty("EMAILENABLE");
//        if (emailEnabled.equalsIgnoreCase("YES")) {
            String APIName = "CBS_SendEmail_" + emailStage;
            String ErrorCode = "";
            String ErrorMessage = "";
            String APIStatus = "FAIL";
            String fileContent = "";
            String filename = "";
            try {
                Log.consoleLog(ifr, "#EmailId===>" + emailId);
                Log.consoleLog(ifr, "#EmailBody===>" + emailBody);
                String Request = "{\n    \"toEmail\": \"" + emailId + "\",\n    \"subject\": \"" + emailSubject + "\",\n    \"msgBody\": \"" + emailBody + "\",\n    \"fileName\": \"" + filename + "\",\n    \"fileContent\": \"" + fileContent + "\"\n}";
                HashMap<String, String> requestHeader = new HashMap<>();
                Log.consoleLog(ifr, "sendEmail/Request==>" + Request);
                Log.consoleLog(ifr, "sendEmail/requestHeader==>" + requestHeader);
                String Response = this.cf.CallWebService(ifr, "EMAILNOTIFICATION", Request, "", requestHeader);
                Log.consoleLog(ifr, "Response===>" + Response);
                if (!Response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(Response);
                    JSONObject resultObj = new JSONObject((Map) OutputJSON);
                    String body = resultObj.get("body").toString();
                    JSONObject bodyJSON = (JSONObject) parser.parse(body);
                    JSONObject bodyObj = new JSONObject((Map) bodyJSON);
                    String CheckError = this.cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                    Log.consoleLog(ifr, "CheckError===>" + CheckError);
                    if (!CheckError.equalsIgnoreCase("true")) {
                        String[] ErrorData = CheckError.split("#");
                        ErrorCode = ErrorData[0];
                        ErrorMessage = ErrorData[1];
                    }
                    APIStatus = "";
                    String APIStatusSend = "";
                    if (ErrorMessage.equalsIgnoreCase("")) {
                        APIStatus = "SUCCESS";
                        APIStatusSend = "SUCCESS";
                    } else {
                        APIStatus = "FAIL";
                        APIStatusSend = "ERROR";
                    }
                } else {
                    Response = "No response from server.";
                }
                this.cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception/CBS_SendEmail===>" + e);
                Log.errorLog(ifr, "Exception/CBS_SendEmail===>" + e);
            }
//        } else {
//            Log.consoleLog(ifr, "#EMAIL IS DISABLED For Gold.");
//        }
        return "";
    }

    public String sendSMS(IFormReference ifr, String ProcessInstanceId, String mobileno, String smsStage, String smsContent) {
        Log.consoleLog(ifr, "Entered into sendSMS Gold...");
        Log.consoleLog(ifr, "smsStage Gold==>" + smsStage);
        Log.consoleLog(ifr, "mobileno Gold==>" + mobileno);
        Log.consoleLog(ifr, "mobileno Gold==>" + smsContent);
//        String smsEnabled = PropertyReader.getInstance().getProperty("SMSENABLE");
//        if (smsEnabled.equalsIgnoreCase("YES")) {
            String APIName = "CBS_SMS_" + smsStage;
            String ErrorCode = "";
            String ErrorMessage = "";
            String APIStatus = "FAIL";
            try {
                String EmailBodyQry = "SELECT BODY FROM CAN_MST_EMAIL_HEADERS WHERE STAGE='" + smsStage + "'";
                List<List<String>> Result = ifr.getDataFromDB(EmailBodyQry);
                Log.consoleLog(ifr, "#Result===>" + Result.toString());
                String EmailBody = "";
                if (Result.size() > 0) {
                    EmailBody = ((List<String>) Result.get(0)).get(0);
                }
                Log.consoleLog(ifr, "EmailBody Before..." + EmailBody);
                JSONObject encryptData = new JSONObject();
                if (mobileno.length() == 10) {
                    encryptData.put("dest", "91" + mobileno);
                } else {
                    encryptData.put("dest", mobileno);
                }

                encryptData.put("msg", smsContent);
                encryptData.put("uname", PropertyReader.getInstance().getProperty("SMSUName"));
                encryptData.put("pwd", PropertyReader.getInstance().getProperty("SMSPwd"));
                encryptData.put("intl", "0");
                encryptData.put("prty", "1");
                String serviceName = "SendOTP";
                String reqest = encryptData.toString();
                HashMap<String, String> requestHeader = new HashMap<>();
                Log.consoleLog(ifr, "sendSMS/reqest==>" + reqest);
                Log.consoleLog(ifr, "sendSMS/requestHeader==>" + requestHeader);
                String Response = this.cf.CallWebService(ifr, serviceName, reqest, "", requestHeader);
                Log.consoleLog(ifr, "SMS OTP Response : " + Response);
                Log.consoleLog(ifr, "Response===>" + Response);
                if (!Response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(Response);
                    JSONObject resultObj = new JSONObject((Map) OutputJSON);
                    String body = resultObj.get("body").toString();
                    JSONObject bodyJSON = (JSONObject) parser.parse(body);
                    JSONObject bodyObj = new JSONObject((Map) bodyJSON);
                    String CheckError = this.cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                    Log.consoleLog(ifr, "CheckError===>" + CheckError);
                    if (!CheckError.equalsIgnoreCase("true")) {
                        String[] ErrorData = CheckError.split("#");
                        ErrorCode = ErrorData[0];
                        ErrorMessage = ErrorData[1];
                    }
                    APIStatus = "";
                    String APIStatusSend = "";
                    if (ErrorMessage.equalsIgnoreCase("")) {
                        APIStatus = "SUCCESS";
                        APIStatusSend = "SUCCESS";
                    } else {
                        APIStatus = "FAIL";
                        APIStatusSend = "ERROR";
                    }
                } else {
                    Response = "No response from server.";
                }
                this.cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, reqest, Response, ErrorCode, ErrorMessage, APIStatus);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception/papl_SendSMS===>" + e);
                Log.errorLog(ifr, "Exception/papl_SendSMS===>" + e);
            }
//        } else {
//            Log.consoleLog(ifr, "#SMS IS DISABLED For Gold.");
//        }
        return "";
    }

    public String sendWhatsAppMsg(IFormReference ifr, String ProcessInstanceId, String mobileno, String ParamValue1, String ParamValue2, String ProductType, String EmailStage) {
        Log.consoleLog(ifr, "Entered into sendWhatsAppMsg Gold...");
        Log.consoleLog(ifr, "EmailStage==>" + EmailStage);
        String whatsAppEnabled = PropertyReader.getInstance().getProperty("WHATSAPPENABLE");
        if (whatsAppEnabled.equalsIgnoreCase("YES")) {
            String APIName = "WhatsApp_" + EmailStage;
            String ErrorCode = "";
            String ErrorMessage = "";
            String APIStatus = "FAIL";
            try {
                String EmailBodyQry = "SELECT BODY FROM CAN_MST_EMAIL_HEADERS WHERE STAGE='" + EmailStage + "'";
                List<List<String>> Result = ifr.getDataFromDB(EmailBodyQry);
                Log.consoleLog(ifr, "#Result===>" + Result.toString());
                String EmailBody = "";
                if (Result.size() > 0) {
                    EmailBody = ((List<String>) Result.get(0)).get(0);
                }
                if (EmailStage.equalsIgnoreCase("1001")) {
                    EmailBody = EmailBody.replace("{#Product#}", ProductType);
                    EmailBody = EmailBody.replace("{#OTP#}", ParamValue1);
                }
                HashMap<String, String> requestHeader = new HashMap<>();
                String reqest = "{\n  \"auth_scheme\": \"plain\",\n  \"format\": \"json\",\n  \"message\": \"" + EmailBody + "\",\n  \"message_type\": \"DATA_TEXT\",\n  \"method\": \"SendMessage\",\n  \"phone_number\": \"" + mobileno + "\",\n  \"v\": \"1.1\"\n}";
                String Response = this.cf.CallWebService(ifr, "WhatsApp", reqest, "", requestHeader);
                Log.consoleLog(ifr, "WhatsApp Response : " + Response);
                if (!Response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject resultObj = (JSONObject) parser.parse(Response);
                    String responseCode = resultObj.get("responseCode").toString();
                    if (responseCode.equalsIgnoreCase("200")) {
                        APIStatus = "SUCCESS";
                    } else {
                        APIStatus = "FAIL";
                    }
                } else {
                    APIStatus = "FAIL";
                    Response = "No response from server.";
                }
                this.cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, reqest, Response, ErrorCode, ErrorMessage, APIStatus);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception/sendWhatsAppMsg===>" + e);
                Log.errorLog(ifr, "Exception/sendWhatsAppMsg===>" + e);
            }
        } else {
            Log.consoleLog(ifr, "#WHATSAPP IS DISABLED Gold.");
        }
        return "";
    }
    public String getMobileNumber(IFormReference ifr) {
        try {
            String mobileNumber = "";
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" + PID + "' ";
            List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
            if (!list.isEmpty()) {
                return list.get(0).get(0).length() == 10 ? "91" + list.get(0).get(0) : list.get(0).get(0);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in getMobileNumber::" + e);
            Log.errorLog(ifr, "Exception in getMobileNumber::" + e);
        }
        return "";
    }

}
