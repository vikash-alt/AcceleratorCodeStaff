package com.newgen.dlp.integration.cbs;

import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class HRMS {

    APICommonMethods cm = new APICommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();

    public String getHRMSDetails(IFormReference ifr, String empId) throws ParseException {
        String apiName = "HRMS";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        String name = "";
        String designation = "";
        String branchDPCode = "";
        String branch = "";
        String gross = "";
        String totalDeduction = "";
        String nth = "";
        String salaryAccount = "";
        String dateOfJoining = "";
        String probation = "";
        String basic = "";
        String specialAll = "";
        String da = "";
        String hra = "";
        String cca = "";
        String lall = "";
        String others = "";
        String it = "";
        String dcps = "";
        String lic = "";
        String fa = "";
        String quarDed = "";
        String furnitureDed = "";
        String swfLoan = "";
        String spf = "";
        String spfLoan = "";
        String otherDed = "";
        String dofRetire = "";
        String specall = "";
        String netSal = "";
        // newcode by shanmukhavarma
        String irStatus = "";
        String exServicesMan = "";
        String status = "";
        String monthOfSal="";
        String monthName ="";
        try {
            request = "{\n" + "        \"EMPLID\": \"" + empId + "\"\n" + "}";
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
//                JSONObject resultObj = (JSONObject) parser.parse(response);
                JSONObject getBody = (JSONObject) parser.parse(response);
                Log.consoleLog(ifr, "getBody===>" + getBody);
                JSONObject resultObj = (JSONObject) getBody.get("body");
                Log.consoleLog(ifr, "resultObj===>" + resultObj);
                
                JSONObject bodyObj = new JSONObject(resultObj);
                String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
                if (CheckError.equalsIgnoreCase("true")) {
                String knockOfRules = hrmsKnockOffRule(ifr, resultObj);
                if (!knockOfRules.equalsIgnoreCase("Knock of Success")) {
                	 Log.consoleLog(ifr, "Inside knockOfRules===>");
                    return knockOfRules;
                }
                if (Optional.ofNullable(resultObj.get("PROBATION")).isPresent()
                        && !Optional.ofNullable(resultObj.get("PROBATION")).isEmpty()) {
                    probation = resultObj.get("PROBATION").toString();
                    Log.consoleLog(ifr, "PROBATION==>" + probation);
                }
                
                if (Optional.ofNullable(resultObj.get("MONTH_OF_SALARY")).isPresent()
                        && !Optional.ofNullable(resultObj.get("MONTH_OF_SALARY")).isEmpty()) {
                    monthOfSal = resultObj.get("MONTH_OF_SALARY").toString();
                    Log.consoleLog(ifr, "MONTH_OF_SALARY==>" + monthOfSal);
                    String[] months = new DateFormatSymbols().getMonths();
                    int monthIndex = Integer.parseInt(monthOfSal) - 1;  
                   monthName = months[monthIndex]; 
                }
                
                if (Optional.ofNullable(resultObj.get("NAME")).isPresent()
                        && !Optional.ofNullable(resultObj.get("NAME")).isEmpty()) {
                    name = resultObj.get("NAME").toString();
                    Log.consoleLog(ifr, "name==>" + name);
                }
                if (Optional.ofNullable(resultObj.get("DESIGNATION")).isPresent()
                        && !Optional.ofNullable(resultObj.get("DESIGNATION")).isEmpty()) {
                    designation = resultObj.get("DESIGNATION").toString();
                    Log.consoleLog(ifr, "designation==>" + designation);
                }
                if (Optional.ofNullable(resultObj.get("BRANCH_DPCODE")).isPresent()
                        && !Optional.ofNullable(resultObj.get("BRANCH_DPCODE")).isEmpty()) {
                    branchDPCode = resultObj.get("BRANCH_DPCODE").toString();
                    Log.consoleLog(ifr, "branchDPCode==>" + branchDPCode);
                }
                
                int branchDPCodeLength = branchDPCode.length();
          		for (int i = 0; i < 5 - branchDPCodeLength; i++) {
          			branchDPCode = "0" + branchDPCode;
          			Log.consoleLog(ifr, "branchDPCode==>" + branchDPCode);
          		}
                
                if (Optional.ofNullable(resultObj.get("BRANCH")).isPresent()
                        && !Optional.ofNullable(resultObj.get("BRANCH")).isEmpty()) {
                    branch = resultObj.get("BRANCH").toString();
                    Log.consoleLog(ifr, "branch==>" + branch);
                }
                if (Optional.ofNullable(resultObj.get("GROSS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("GROSS")).isEmpty()) {
                    gross = resultObj.get("GROSS").toString();
                    Log.consoleLog(ifr, "gross==>" + gross);
                }
                if (Optional.ofNullable(resultObj.get("TOTAL_DED")).isPresent()
                        && !Optional.ofNullable(resultObj.get("TOTAL_DED")).isEmpty()) {
                    totalDeduction = resultObj.get("TOTAL_DED").toString();
                    Log.consoleLog(ifr, "totalDeduction==>" + totalDeduction);
                }
                nth = String.valueOf(0.25 * Double.parseDouble(gross));
                Log.consoleLog(ifr, "nth==>" + nth);

                if (Optional.ofNullable(resultObj.get("OTHER_DED")).isPresent()
                        && !Optional.ofNullable(resultObj.get("OTHER_DED")).isEmpty()) {
                    otherDed = resultObj.get("OTHER_DED").toString();
                    Log.consoleLog(ifr, "OTHER_DED==>" + otherDed);
                }

                if (Optional.ofNullable(resultObj.get("SALARY_ACCOUNT")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SALARY_ACCOUNT")).isEmpty()) {
                    salaryAccount = resultObj.get("SALARY_ACCOUNT").toString();
                    Log.consoleLog(ifr, "salaryAccount==>" + salaryAccount);
                }
                if (Optional.ofNullable(resultObj.get("DATE_OF_JOINING")).isPresent()
                        && !Optional.ofNullable(resultObj.get("DATE_OF_JOINING")).isEmpty()) {
                    dateOfJoining = resultObj.get("DATE_OF_JOINING").toString();
                    Log.consoleLog(ifr, "salaryAccount==>" + salaryAccount);
                }
                if (Optional.ofNullable(resultObj.get("BASIC")).isPresent()
                        && !Optional.ofNullable(resultObj.get("BASIC")).isEmpty()) {
                    basic = resultObj.get("BASIC").toString();
                    Log.consoleLog(ifr, "basic==>" + basic);
                }
                if (Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isEmpty()) {
                    specialAll = resultObj.get("SPECIAL_ALL").toString();
                    Log.consoleLog(ifr, "specialAll==>" + specialAll);
                }
                if (Optional.ofNullable(resultObj.get("DA")).isPresent()
                        && !Optional.ofNullable(resultObj.get("DA")).isEmpty()) {
                    da = resultObj.get("DA").toString();
                    Log.consoleLog(ifr, "da==>" + da);
                }
                if (Optional.ofNullable(resultObj.get("HRA")).isPresent()
                        && !Optional.ofNullable(resultObj.get("HRA")).isEmpty()) {
                    hra = resultObj.get("HRA").toString();
                    Log.consoleLog(ifr, "hra==>" + hra);
                }
                if (Optional.ofNullable(resultObj.get("CCA")).isPresent()
                        && !Optional.ofNullable(resultObj.get("CCA")).isEmpty()) {
                    cca = resultObj.get("CCA").toString();
                    Log.consoleLog(ifr, "cca==>" + cca);
                }
                if (Optional.ofNullable(resultObj.get("LEARN_ALL")).isPresent()
                        && !Optional.ofNullable(resultObj.get("LEARN_ALL")).isEmpty()) {
                    lall = resultObj.get("LEARN_ALL").toString();
                    Log.consoleLog(ifr, "lall==>" + lall);
                }
                if (Optional.ofNullable(resultObj.get("OTHERS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("OTHERS")).isEmpty()) {
                    others = resultObj.get("OTHERS").toString();
                    Log.consoleLog(ifr, "others==>" + others);
                }
                if (Optional.ofNullable(resultObj.get("IT")).isPresent()
                        && !Optional.ofNullable(resultObj.get("IT")).isEmpty()) {
                    it = resultObj.get("IT").toString();
                    Log.consoleLog(ifr, "it==>" + it);
                }
                if (Optional.ofNullable(resultObj.get("DCPS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("DCPS")).isEmpty()) {
                    dcps = resultObj.get("DCPS").toString();
                    Log.consoleLog(ifr, "dcps==>" + dcps);
                }
                if (Optional.ofNullable(resultObj.get("LIC")).isPresent()
                        && !Optional.ofNullable(resultObj.get("LIC")).isEmpty()) {
                    lic = resultObj.get("LIC").toString();
                    Log.consoleLog(ifr, "lic==>" + lic);
                }
                if (Optional.ofNullable(resultObj.get("FA")).isPresent()
                        && !Optional.ofNullable(resultObj.get("FA")).isEmpty()) {
                    fa = resultObj.get("FA").toString();
                    Log.consoleLog(ifr, "fa==>" + fa);
                }
                if (Optional.ofNullable(resultObj.get("QUARTERS_DED")).isPresent()
                        && !Optional.ofNullable(resultObj.get("QUARTERS_DED")).isEmpty()) {
                    quarDed = resultObj.get("QUARTERS_DED").toString();
                    Log.consoleLog(ifr, "quarDed==>" + quarDed);
                }
                if (Optional.ofNullable(resultObj.get("FURNITURE_DED")).isPresent()
                        && !Optional.ofNullable(resultObj.get("FURNITURE_DED")).isEmpty()) {
                    furnitureDed = resultObj.get("FURNITURE_DED").toString();
                    Log.consoleLog(ifr, "quarDed==>" + quarDed);
                }
                if (Optional.ofNullable(resultObj.get("SWF_LOAN")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SWF_LOAN")).isEmpty()) {
                    swfLoan = resultObj.get("SWF_LOAN").toString();
                    Log.consoleLog(ifr, "swfLoan==>" + swfLoan);
                }
                if (Optional.ofNullable(resultObj.get("SPF")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SPF")).isEmpty()) {
                    spf = resultObj.get("SPF").toString();
                    Log.consoleLog(ifr, "spf==>" + spf);
                }
                if (Optional.ofNullable(resultObj.get("SPF_LOAN")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SPF_LOAN")).isEmpty()) {
                    spfLoan = resultObj.get("SPF_LOAN").toString();
                    Log.consoleLog(ifr, "spfLoan==>" + spfLoan);
                }
                if (Optional.ofNullable(resultObj.get("DATE_OF_RETIREMENT")).isPresent()
                        && !Optional.ofNullable(resultObj.get("DATE_OF_RETIREMENT")).isEmpty()) {
                    dofRetire = resultObj.get("DATE_OF_RETIREMENT").toString();
                    Log.consoleLog(ifr, "dofRetire==>" + dofRetire);
                }
                if (Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isPresent()
                        && !Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isEmpty()) {
                    specall = resultObj.get("SPECIAL_ALL").toString();
                    Log.consoleLog(ifr, "specall==>" + specall);
                }
                if (Optional.ofNullable(resultObj.get("NET_SALARY")).isPresent()
                        && !Optional.ofNullable(resultObj.get("NET_SALARY")).isEmpty()) {
                    netSal = resultObj.get("NET_SALARY").toString();
                    Log.consoleLog(ifr, "netSal==>" + netSal);
                }

                if (Optional.ofNullable(resultObj.get("TOTAL_DED")).isPresent()
                        && !Optional.ofNullable(resultObj.get("TOTAL_DED")).isEmpty()) {
                    ifr.setValue("TOTAL_DED", resultObj.get("TOTAL_DED").toString());
                } else {
                    ifr.setValue("TOTAL_DED", "0.0");
                }

                // newcode phases2
                if (Optional.ofNullable(resultObj.get("IR_STATUS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("IR_STATUS")).isEmpty()) {
                    irStatus = resultObj.get("IR_STATUS").toString();
                    irStatus = irStatus.trim().equalsIgnoreCase("N") || irStatus.trim().equalsIgnoreCase("No") ? "No" : "Yes";
                    Log.consoleLog(ifr, "netSal==>" + netSal);
                }

                if (Optional.ofNullable(resultObj.get("EX_SERVICEMEN")).isPresent()
                        && !Optional.ofNullable(resultObj.get("EX_SERVICEMEN")).isEmpty()) {
                    exServicesMan = resultObj.get("EX_SERVICEMEN").toString();
                    Log.consoleLog(ifr, "exServicesMan==>" + exServicesMan);
                }

                if (Optional.ofNullable(resultObj.get("STATUS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("STATUS")).isEmpty()) {
                    status = resultObj.get("STATUS").toString();
                    Log.consoleLog(ifr, "STATUS==>" + status);
                }

                String updateQuery = "update SLOS_TRN_LOANSUMMARY set DA='" + da + "', HRA='" + hra + "',CCA='" + cca
                        + "'," + "LEARNING_ALLOWANCE='" + lall + "',INCOME_TAX='" + it + "',DEFINED_PENSION='" + dcps
                        + "',lic='" + lic + "',FESTIVAL_ADVANCE_RECOVERY='" + fa + "',QUARTERS_RENT_RECOVERY='"
                        + quarDed + "'," + " FURNITURE_RECOVERY='" + furnitureDed + "',SWF_LOAN_RECOVERY='" + swfLoan
                        + "',PROVIDENT_FUND='" + spf + "',PF_LOAN_RECOVERY='" + spfLoan + "', DP_CODE='" + branchDPCode
                        + "', OTHER_DED='" + otherDed + "',DATE_OF_RETIREMENT='" + dofRetire + "',BASIC_PAY='" + basic
                        + "',SPECIAL_ALLOWANCE='" + specall + "',NET_SALARY='" + netSal + "',MONTHOFSAL='"+monthName+"',BRANCH='" + branch
                        + "' where winame='" + processInstanceId + "'";
                ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "updateQuery SLOS_TRN_LOANSUMMARY==> " + updateQuery);
                
                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // Parse and format
                String formattedJoiningDate = LocalDate.parse(dateOfJoining, inputFormat).format(outputFormat);
                String formattedRetireDate = LocalDate.parse(dofRetire, inputFormat).format(outputFormat);
                Log.consoleLog(ifr, "dateOfJoining : " + formattedJoiningDate);
                Log.consoleLog(ifr, "dateOfRetirement : " + formattedRetireDate);
                ifr.setValue("Date_of_joining", formattedJoiningDate);
                ifr.setValue("Date_of_Retirement", formattedRetireDate);
                
                String insertQuery = "insert into SLOS_STAFF_TRN(winame,PROBATION,TOTAL_DED,DATE_OF_JOINING,CURRENT_BRANCH,DATE_OF_RETIREMENT,SB_ACCOUNT_NUMBER,EMPLOYEE_STATUS) values('" + processInstanceId + "','"
        				+ probation + "','" + totalDeduction + "','"
        				+ formattedJoiningDate + "','" + branch + "','"+formattedRetireDate+"','"+salaryAccount+"','"+status+"')";

        		Log.consoleLog(ifr, "insertQuery===> : " + insertQuery);
        		this.cf.mExecuteQuery(ifr, insertQuery, "INSERT into SLOS_STAFF_TRN");
                }
                else {
                    String[] ErrorData = CheckError.split("#");
                    apiErrorCode = ErrorData[0];
                    apiErrorMessage = ErrorData[1];
                }
            } else {
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
            }
            if (apiErrorMessage.equalsIgnoreCase("")) {
                apiStatus = "SUCCESS";
            } else {
                apiStatus = "ERROR";
            }
//
//            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                return RLOS_Constants.SUCCESS;
//            }
            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
            	return RLOS_Constants.ERROR + ":" + apiErrorMessage;
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("NAME", name);
                jsonObject.put("DESIGNATION", designation);
                jsonObject.put("BRANCHDPCODE", branchDPCode);
                //jsonObject.put("BRANCH", branch);
                jsonObject.put("GROSS", gross);
                jsonObject.put("TOTALDED", totalDeduction);
                jsonObject.put("NTH", nth);
                jsonObject.put("SALARYACCOUNT", salaryAccount);
                jsonObject.put("DATEOFJOINING", dateOfJoining);
                jsonObject.put("PROBATION", probation);
                jsonObject.put("branch", branch);
                ifr.setValue("IR_Status", irStatus);
                ifr.setValue("Ex_Serviceman", exServicesMan);
                ifr.setValue("Employee_Status", status);
                ifr.setValue("working_branch_DP_code", branchDPCode);

                //  IR_Status
//Ex_Serviceman
//Employee_Status
//working_branch_DP_code
//				jsonObject.put("LOANACCNO", loanAccNum);
//				jsonObject.put("LOANAMOUNT", loanAmount);
                return jsonObject.toJSONString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_HRMS===>" + e);
            Log.errorLog(ifr, "Exception/CBS_HRMS===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;

    }

    private String hrmsKnockOffRule(IFormReference ifr, JSONObject resultObj) {
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String mobileNumber = ifr.getValue("Portal_T_MobileNumber").toString();
        String panNumFrmHrm = resultObj.get("PAN").toString();
        Log.consoleLog(ifr, "mobileNumber hrms===>" + mobileNumber);
        String panNumber = "";
        String status = "Active";
        String query = "SELECT PANNUMBER FROM LOS_TRN_CUSTOMERSUMMARY WHERE WINAME='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        Log.consoleLog(ifr, "query for hrms knock off rules===>" + query);
        List<List<String>> responseForPan = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "responseForPan===>" + responseForPan);
        Log.consoleLog(ifr, "resultObj===>" + resultObj);
        Log.consoleLog(ifr, "panNumFrmHrm===>" + panNumFrmHrm);
        Log.consoleLog(ifr, "panNumber===>" + panNumber);
        for (List<String> panNumFrmDb : responseForPan) {
        	 Log.consoleLog(ifr, "Inside panNumFrmDb===>");
            panNumber = panNumFrmDb.get(0);
            Log.consoleLog(ifr, "panNumber===>" +panNumber);
        }
        if (resultObj.size() == 0) {
        	 Log.consoleLog(ifr, "Inside resultObj===>");
            return AccelatorStaffConstant.EMPTY_RESPONSE_MESSAGE;
        }
        if (!compare(panNumber, panNumFrmHrm)) {
        	 Log.consoleLog(ifr, "Inside compare between panNumber and panNumFrmHrm===>");
            return AccelatorStaffConstant.PAN_ERROR_MESSAGE;
        }
        if (!compare("Active", resultObj.get("STATUS").toString())) {
        	Log.consoleLog(ifr, "STATUS==>");
            return AccelatorStaffConstant.STATUS_ACTIVE_MESSAGE;
        }
       // if (compare("Y", resultObj.get("IR_STATUS").toString())) {
         //   return AccelatorStaffConstant.IR_STATUS_MESSAGE;
        //}

        boolean res1 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter1);
        boolean res2 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter2);
        boolean res3 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter3);
        String format = "";
        if (res1) {
        	Log.consoleLog(ifr, "inside res1==>");
            format = "MM-dd-yyyy";
        } else if (res2) {
        	Log.consoleLog(ifr, "inside res2==>");
            format = "yyyy-MM-dd";
        } else {
        	Log.consoleLog(ifr, "inside res3==>");
            format = "dd-MM-yyyy";
        }
        //if (!isEligibleForLoan(resultObj.get("DATE_OF_RETIREMENT").toString(), LocalDate.now(), format)) {
           // return AccelatorStaffConstant.RETIRING_STAFF_MESSAGE;
       // }
        /*  if (compare("Yes", resultObj.get("EX_SERVICEMEN").toString())) {
            return AccelatorStaffConstant.EX_SERVICEMAN_MESSAGE;
        }*/
        return "Knock of Success";
    }

    public boolean compare(String value1, String value2) {
        return Optional.ofNullable(value1).orElse("").trim().equals(Optional.ofNullable(value2).orElse("").trim());
    }

    public boolean isEligibleForLoan(String retirementDateStr, LocalDate loanApplicationDate, String formate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formate);

        LocalDate retirementDate = LocalDate.parse(retirementDateStr, formatter);

        long monthsUntilRetirement = ChronoUnit.MONTHS.between(loanApplicationDate, retirementDate);
        return monthsUntilRetirement > 12;
    }

    private boolean isDateInFormat(String dateStr, DateTimeFormatter formatter) {
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String getHRMSDetailsKnockOff(IFormReference ifr, String empId) throws ParseException {
        String apiName = "HRMS";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {
            request = "{\n" + "        \"EMPLID\": \"" + empId + "\"\n" + "}";
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
//                JSONObject resultObj = (JSONObject) parser.parse(response);
                JSONObject getBody = (JSONObject) parser.parse(response);
                Log.consoleLog(ifr, "getBody===>" + getBody);
                JSONObject resultObj = (JSONObject) getBody.get("body");
                Log.consoleLog(ifr, "resultObj===>" + resultObj);
                
                if (Optional.ofNullable(resultObj.get("EX_SERVICEMEN")).isPresent()
                        && !Optional.ofNullable(resultObj.get("EX_SERVICEMEN")).isEmpty()) {
                  String  exServicesMan = resultObj.get("EX_SERVICEMEN").toString();
                    Log.consoleLog(ifr, "exServicesMan==>" + exServicesMan);
                    if(exServicesMan.trim().equalsIgnoreCase("Yes") || exServicesMan.trim().equalsIgnoreCase("Y") ){
                        return AccelatorStaffConstant.EX_SERVICESMEN_ERROR_MESSAGE;
                    }
                }
                if (Optional.ofNullable(resultObj.get("IR_STATUS")).isPresent()
                        && !Optional.ofNullable(resultObj.get("IR_STATUS")).isEmpty()
                        && compare("Y", resultObj.get("IR_STATUS").toString())) {

                    return AccelatorStaffConstant.IR_STATUS_MESSAGE;

                }
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                boolean res1 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter1);
                boolean res2 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter2);
                boolean res3 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter3);
                String format = "";
                if (res1) {
                    format = "MM-dd-yyyy";
                } else if (res2) {
                    format = "yyyy-MM-dd";
                } else {
                    format = "dd-MM-yyyy";
                }
                if (!isEligibleForLoan(resultObj.get("DATE_OF_RETIREMENT").toString(), LocalDate.now(), format)) {
                    return AccelatorStaffConstant.RETIRING_STAFF_MESSAGE;
                }

                // newcode phases2
            } else {
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
            } else {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_HRMS===>" + e);
            Log.errorLog(ifr, "Exception/CBS_HRMS===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;

    }
    public String getHrmsDetailsVL(IFormReference ifr,String empId, boolean b, String loanType){
        String apiName = "HRMS";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        String name = "";
        String designation = "";
        String presentWorkingBranchDPCode = "";
        String branch = "";
        String gross = "";
        String totalDeduction = "";
        String nth = "";
        String salaryAccount = "";
        String dateOfJoining = "";
        String probation = "";
        String gender="";
        String dateOfRetirement="";
        String netSalary="";
       String irStatus="";
       String warning="";
       String exServicesMen="";
       String status="";
       String basic = "";
       String specialAll = "";
       String da = "";
       String hra = "";
       String cca = "";
       String lall = "";
       String others = "";
       String it = "";
       String dcps = "";
       String lic = "";
       String fa = "";
       String quarDed = "";
       String furnitureDed = "";
       String swfLoan = "";
       String spf = "";
       String spfLoan = "";
       String otherDed = "";
       String dofRetire = "";
       String specall = "";
       String netSal = "";
       String exServicesMan = "";
       String branchDPCode="";
       
       
        try {
            request = "{\n" + "        \"EMPLID\": \"" + empId + "\"\n" + "}";
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
//                JSONObject resultObj = (JSONObject) parser.parse(response);
                JSONObject getBody = (JSONObject) parser.parse(response);
              JSONObject resultObj = (JSONObject) getBody.get("body");
              Log.consoleLog(ifr, "resultObj===>" + resultObj);
              
              JSONObject bodyObj = new JSONObject(resultObj);
              String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
              if (CheckError.equalsIgnoreCase("true")) {
                
              if(b) {
              String knockOfRules = hrmsKnockOffRuleVL(ifr, resultObj);
              if (!knockOfRules.equalsIgnoreCase("success") && !knockOfRules.contains("WARNING")) {
              	Log.consoleLog(ifr, "inside Warning===>");
                  return knockOfRules;
              }
              warning=knockOfRules.contains("WARNING")?knockOfRules:"NoWarning";
              Log.consoleLog(ifr, "inside WARNING===>" );
             }
              if (Optional.ofNullable(resultObj.get("BRANCH")).isPresent()
                      && !Optional.ofNullable(resultObj.get("BRANCH")).isEmpty()) {
                  branch = resultObj.get("BRANCH").toString();
                  Log.consoleLog(ifr, "branch==>" + branch);
              }
//              if (Optional.ofNullable(resultObj.get("GROSS")).isPresent()
//                      && !Optional.ofNullable(resultObj.get("GROSS")).isEmpty()) {
//                  gross = resultObj.get("GROSS").toString();
//                  Log.consoleLog(ifr, "gross==>" + gross);
//              }
              if (Optional.ofNullable(resultObj.get("TOTAL_DED")).isPresent()
                      && !Optional.ofNullable(resultObj.get("TOTAL_DED")).isEmpty()) {
                  totalDeduction = resultObj.get("TOTAL_DED").toString();
                  Log.consoleLog(ifr, "totalDeduction==>" + totalDeduction);
              }
//              nth = String.valueOf(0.25 * Double.parseDouble(gross));
//              Log.consoleLog(ifr, "nth==>" + nth);

              if (Optional.ofNullable(resultObj.get("OTHER_DED")).isPresent()
                      && !Optional.ofNullable(resultObj.get("OTHER_DED")).isEmpty()) {
                  otherDed = resultObj.get("OTHER_DED").toString();
                  Log.consoleLog(ifr, "OTHER_DED==>" + otherDed);
              }
//
//              if (Optional.ofNullable(resultObj.get("SALARY_ACCOUNT")).isPresent()
//                      && !Optional.ofNullable(resultObj.get("SALARY_ACCOUNT")).isEmpty()) {
//                  salaryAccount = resultObj.get("SALARY_ACCOUNT").toString();
//                  Log.consoleLog(ifr, "salaryAccount==>" + salaryAccount);
//              }
//              if (Optional.ofNullable(resultObj.get("DATE_OF_JOINING")).isPresent()
//                      && !Optional.ofNullable(resultObj.get("DATE_OF_JOINING")).isEmpty()) {
//                  dateOfJoining = resultObj.get("DATE_OF_JOINING").toString();
//                  Log.consoleLog(ifr, "salaryAccount==>" + salaryAccount);
//              }
              if (Optional.ofNullable(resultObj.get("BASIC")).isPresent()
                      && !Optional.ofNullable(resultObj.get("BASIC")).isEmpty()) {
                  basic = resultObj.get("BASIC").toString();
                  Log.consoleLog(ifr, "basic==>" + basic);
              }
              if (Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isPresent()
                      && !Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isEmpty()) {
                  specialAll = resultObj.get("SPECIAL_ALL").toString();
                  Log.consoleLog(ifr, "specialAll==>" + specialAll);
              }
              if (Optional.ofNullable(resultObj.get("DA")).isPresent()
                      && !Optional.ofNullable(resultObj.get("DA")).isEmpty()) {
                  da = resultObj.get("DA").toString();
                  Log.consoleLog(ifr, "da==>" + da);
              }
              if (Optional.ofNullable(resultObj.get("HRA")).isPresent()
                      && !Optional.ofNullable(resultObj.get("HRA")).isEmpty()) {
                  hra = resultObj.get("HRA").toString();
                  Log.consoleLog(ifr, "hra==>" + hra);
              }
              if (Optional.ofNullable(resultObj.get("CCA")).isPresent()
                      && !Optional.ofNullable(resultObj.get("CCA")).isEmpty()) {
                  cca = resultObj.get("CCA").toString();
                  Log.consoleLog(ifr, "cca==>" + cca);
              }
              if (Optional.ofNullable(resultObj.get("LEARN_ALL")).isPresent()
                      && !Optional.ofNullable(resultObj.get("LEARN_ALL")).isEmpty()) {
                  lall = resultObj.get("LEARN_ALL").toString();
                  Log.consoleLog(ifr, "lall==>" + lall);
              }
              if (Optional.ofNullable(resultObj.get("OTHERS")).isPresent()
                      && !Optional.ofNullable(resultObj.get("OTHERS")).isEmpty()) {
                  others = resultObj.get("OTHERS").toString();
                  Log.consoleLog(ifr, "others==>" + others);
              }
              if (Optional.ofNullable(resultObj.get("IT")).isPresent()
                      && !Optional.ofNullable(resultObj.get("IT")).isEmpty()) {
                  it = resultObj.get("IT").toString();
                  Log.consoleLog(ifr, "it==>" + it);
              }
              if (Optional.ofNullable(resultObj.get("DCPS")).isPresent()
                      && !Optional.ofNullable(resultObj.get("DCPS")).isEmpty()) {
                  dcps = resultObj.get("DCPS").toString();
                  Log.consoleLog(ifr, "dcps==>" + dcps);
              }
              if (Optional.ofNullable(resultObj.get("LIC")).isPresent()
                      && !Optional.ofNullable(resultObj.get("LIC")).isEmpty()) {
                  lic = resultObj.get("LIC").toString();
                  Log.consoleLog(ifr, "lic==>" + lic);
              }
              if (Optional.ofNullable(resultObj.get("FA")).isPresent()
                      && !Optional.ofNullable(resultObj.get("FA")).isEmpty()) {
                  fa = resultObj.get("FA").toString();
                  Log.consoleLog(ifr, "fa==>" + fa);
              }
              if (Optional.ofNullable(resultObj.get("QUARTERS_DED")).isPresent()
                      && !Optional.ofNullable(resultObj.get("QUARTERS_DED")).isEmpty()) {
                  quarDed = resultObj.get("QUARTERS_DED").toString();
                  Log.consoleLog(ifr, "quarDed==>" + quarDed);
              }
              if (Optional.ofNullable(resultObj.get("FURNITURE_DED")).isPresent()
                      && !Optional.ofNullable(resultObj.get("FURNITURE_DED")).isEmpty()) {
                  furnitureDed = resultObj.get("FURNITURE_DED").toString();
                  Log.consoleLog(ifr, "quarDed==>" + quarDed);
              }
              if (Optional.ofNullable(resultObj.get("SWF_LOAN")).isPresent()
                      && !Optional.ofNullable(resultObj.get("SWF_LOAN")).isEmpty()) {
                  swfLoan = resultObj.get("SWF_LOAN").toString();
                  Log.consoleLog(ifr, "swfLoan==>" + swfLoan);
              }
              if (Optional.ofNullable(resultObj.get("SPF")).isPresent()
                      && !Optional.ofNullable(resultObj.get("SPF")).isEmpty()) {
                  spf = resultObj.get("SPF").toString();
                  Log.consoleLog(ifr, "spf==>" + spf);
              }
              if (Optional.ofNullable(resultObj.get("SPF_LOAN")).isPresent()
                      && !Optional.ofNullable(resultObj.get("SPF_LOAN")).isEmpty()) {
                  spfLoan = resultObj.get("SPF_LOAN").toString();
                  Log.consoleLog(ifr, "spfLoan==>" + spfLoan);
              }
//              if (Optional.ofNullable(resultObj.get("DATE_OF_RETIREMENT")).isPresent()
//                      && !Optional.ofNullable(resultObj.get("DATE_OF_RETIREMENT")).isEmpty()) {
//                  dofRetire = resultObj.get("DATE_OF_RETIREMENT").toString();
//                  Log.consoleLog(ifr, "dofRetire==>" + dofRetire);
//              }
              if (Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isPresent()
                      && !Optional.ofNullable(resultObj.get("SPECIAL_ALL")).isEmpty()) {
                  specall = resultObj.get("SPECIAL_ALL").toString();
                  Log.consoleLog(ifr, "specall==>" + specall);
              }
              if (Optional.ofNullable(resultObj.get("NET_SALARY")).isPresent()
                      && !Optional.ofNullable(resultObj.get("NET_SALARY")).isEmpty()) {
                  netSal = resultObj.get("NET_SALARY").toString();
                  Log.consoleLog(ifr, "netSal==>" + netSal);
              }

              if (Optional.ofNullable(resultObj.get("TOTAL_DED")).isPresent()
                      && !Optional.ofNullable(resultObj.get("TOTAL_DED")).isEmpty()) {
                  ifr.setValue("TOTAL_DED", resultObj.get("TOTAL_DED").toString());
              } else {
                  ifr.setValue("TOTAL_DED", "0.0");
              }
              String monthOfSal="";
              String monthName ="";
              if (Optional.ofNullable(resultObj.get("MONTH_OF_SALARY")).isPresent()
                      && !Optional.ofNullable(resultObj.get("MONTH_OF_SALARY")).isEmpty()) {
                  monthOfSal = resultObj.get("MONTH_OF_SALARY").toString();
                  Log.consoleLog(ifr, "MONTH_OF_SALARY==>" + monthOfSal);
                  String[] months = new DateFormatSymbols().getMonths();
                  int monthIndex = Integer.parseInt(monthOfSal) - 1;  
                 monthName = months[monthIndex]; 
              }
              
             Validator valid= new KnockOffValidator("");
             Log.consoleLog(ifr, "valid===>" +valid);
             probation= valid.getValue(ifr, resultObj, "PROBATION", "");
             Log.consoleLog(ifr, "probation===>" +probation);
             designation=valid.getValue(ifr, resultObj, "DESIGNATION", "");
             Log.consoleLog(ifr, "designation===>" +designation);
             name=valid.getValue(ifr, resultObj, "NAME", "");
             ifr.setValue("Q_FinalAuthorityDesignation", name);
             Log.consoleLog(ifr, "name===>" +name);
             dateOfJoining=valid.getValue(ifr, resultObj, "DATE_OF_JOINING", "");
             Log.consoleLog(ifr, "dateOfJoining===>" +dateOfJoining);
             dateOfRetirement=valid.getValue(ifr, resultObj, "DATE_OF_RETIREMENT", "");
             Log.consoleLog(ifr, "dateOfRetirement===>" +dateOfRetirement);
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
             LocalDate dateJoining = LocalDate.parse(dateOfJoining, formatter);
             LocalDate currentDate = LocalDate.now();
             int yearsDifference = Period.between(dateJoining, currentDate).getYears();
             Log.consoleLog(ifr, "yearsDifference===>" +yearsDifference);
             irStatus=valid.getValue(ifr, resultObj, "IR_STATUS", ""); 
             Log.consoleLog(ifr, "irStatus===>" +irStatus);
             exServicesMen=valid.getValue(ifr, resultObj, "EX_SERVICEMEN", "");
             Log.consoleLog(ifr, "exServicesMen===>" +exServicesMen);
             status=valid.getValue(ifr, resultObj, "STATUS", "");
             Log.consoleLog(ifr, "status===>" +status);
             presentWorkingBranchDPCode=valid.getValue(ifr, resultObj, "BRANCH_DPCODE", "");
             ifr.setValue("Q_ProcessingBranchCode", presentWorkingBranchDPCode);
             Log.consoleLog(ifr, "presentWorkingBranchDPCode===>" +presentWorkingBranchDPCode);
             salaryAccount=valid.getValue(ifr, resultObj, "SALARY_ACCOUNT", "");
             Log.consoleLog(ifr, "salaryAccount===>" +salaryAccount);
             gross=valid.getValue(ifr, resultObj, "GROSS", "0.0") ;
             Log.consoleLog(ifr, "gross===>" +gross);
            // netSalary=valid.getValue(ifr, resultObj, "NET_SALARY", "");
              totalDeduction= valid.getValue(ifr, resultObj,"TOTAL_DED", "0.0");
              Log.consoleLog(ifr, "totalDeduction===>" +totalDeduction);
//             netSalary= String.valueOf(Double.parseDouble(gross)- Double.parseDouble(totalDeduction));
//             Log.consoleLog(ifr, "netSalary===>" +netSalary)
              
              
              int branchDPCodeLength = presentWorkingBranchDPCode.length();
      		for (int i = 0; i < 5 - branchDPCodeLength; i++) {
      			branchDPCode = "0" + presentWorkingBranchDPCode;
      			Log.consoleLog(ifr, "branchDPCode==>" + branchDPCode);
      		}
      		String currCity = "";
      		String branchName = "";
      		String stateCode = "";
      		String currentAccountHCity = "SELECT CITY, BRANCHNAME,STATECODE FROM LOS_M_BRANCH where BRANCHCODE='"
      				+ branchDPCode + "'";

      		List<List<String>> listcurrentAccountHCity = ifr.getDataFromDB(currentAccountHCity);
      		Log.consoleLog(ifr, "Log of City====>" + listcurrentAccountHCity);
      		if (!listcurrentAccountHCity.isEmpty()) {
      			currCity = listcurrentAccountHCity.get(0).get(0);
      			branchName = listcurrentAccountHCity.get(0).get(1);
      			stateCode = listcurrentAccountHCity.get(0).get(2);
      			Log.consoleLog(ifr, "currCity====>" + currCity);
      		}
			if (b) {
			    String insertQuery = "insert into SLOS_STAFF_TRN(winame,PROBATION,TOTAL_DED,CURRENT_BRANCH,SB_ACCOUNT_NUMBER,LOAN_TYPE,EMPLOYEE_STATUS) values('" + processInstanceId + "','"
        				+ probation + "','" + totalDeduction + "','" + branch + "','"+salaryAccount+"','"+loanType+"','"+status+"')";

        		Log.consoleLog(ifr, "insertQuery===> : " + insertQuery);
        		this.cf.mExecuteQuery(ifr, insertQuery, "INSERT into SLOS_STAFF_TRN");
				
				String QueryCity = "UPDATE SLOS_TRN_LOANSUMMARY SET CITY= '" + currCity + "' WHERE WINAME= '"
						+ processInstanceId + "'";

				Log.consoleLog(ifr, "QueryCity====>" + QueryCity);
				ifr.saveDataInDB(QueryCity);

				String updateQuery = "update SLOS_TRN_LOANSUMMARY set DA='" + da + "', HRA='" + hra + "',CCA='" + cca
						+ "'," + "LEARNING_ALLOWANCE='" + lall + "',INCOME_TAX='" + it + "',DEFINED_PENSION='" + dcps
						+ "',lic='" + lic + "',FESTIVAL_ADVANCE_RECOVERY='" + fa + "',QUARTERS_RENT_RECOVERY='"
						+ quarDed + "'," + " FURNITURE_RECOVERY='" + furnitureDed + "',SWF_LOAN_RECOVERY='" + swfLoan
						+ "',PROVIDENT_FUND='" + spf + "',PF_LOAN_RECOVERY='" + spfLoan + "', DP_CODE='"
						+ branchDPCode + "', OTHER_DED='" + otherDed + "',DATE_OF_RETIREMENT='"
						+ dateOfRetirement + "',BASIC_PAY='" + basic + "',SPECIAL_ALLOWANCE='" + specall
						+ "',NET_SALARY='" + netSal + "',MONTHOFSAL='"+monthName+"',BRANCH='" + branch + "' where winame='" + processInstanceId
						+ "'";
				ifr.saveDataInDB(updateQuery);
				Log.consoleLog(ifr, "updateQuery SLOS_TRN_LOANSUMMARY==> " + updateQuery);
			}
              ifr.setValue("Net_Salary", netSalary);
              Log.consoleLog(ifr, "Net_Salary==> " + netSalary);
               ifr.setStyle("Net_Salary", "disable", "true");
               ifr.setValue("StaffName", name);
               Log.consoleLog(ifr, "StaffName==> " + name);
               ifr.setStyle("StaffName", "disable", "true");
               ifr.setValue("StaffDesignation", designation);
               Log.consoleLog(ifr, "StaffDesignation==> " + designation);
               ifr.setStyle("StaffDesignation", "disable", "true");
               ifr.setValue("Staff_Gender", gender);
               Log.consoleLog(ifr, "Staff_Gender==> " + gender);
               ifr.setStyle("Staff_Gender", "disable", "true");
               ifr.setValue("Date_of_joining", dateOfJoining);
               Log.consoleLog(ifr, "Date_of_joining==> " + dateOfJoining);
               ifr.setStyle("Date_of_joining", "disable", "true");
               ifr.setValue("Date_of_Retirement", dateOfRetirement); 
               Log.consoleLog(ifr, "Date_of_Retirement==> " + dateOfRetirement);
               ifr.setStyle("Date_of_Retirement", "disable", "true");
               ifr.setValue("Years_Of_Service", String.valueOf(yearsDifference));
               Log.consoleLog(ifr, "Years_Of_Service==> " + String.valueOf(yearsDifference));;
               ifr.setStyle("Years_Of_Service", "disable", "true");
               ifr.setValue("IR_Status", irStatus);
               Log.consoleLog(ifr, "IR_Status==> " + irStatus);;
               ifr.setStyle("IR_Status", "disable", "true");
               ifr.setValue("Ex_Serviceman", exServicesMen);
               Log.consoleLog(ifr, "Ex_Serviceman==> " + exServicesMen);
               ifr.setStyle("Ex_Serviceman", "disable", "true");
               ifr.setValue("Employee_Status", status);
               Log.consoleLog(ifr, "Employee_Status==> " + status);
               ifr.setStyle("Employee_Status", "disable", "true");
               ifr.setValue("working_branch_DP_code", presentWorkingBranchDPCode);
               Log.consoleLog(ifr, "working_branch_DP_code==> " + presentWorkingBranchDPCode);
               ifr.setStyle("working_branch_DP_code", "disable", "true");
               ifr.setValue("Salary_Account_Number", salaryAccount);
               Log.consoleLog(ifr, "Salary_Account_Number==> " + salaryAccount);
               ifr.setStyle("Salary_Account_Number", "disable", "true");
               ifr.setValue("Gross_Salary", gross);
               Log.consoleLog(ifr, "Gross_Salary==> " + gross);
               ifr.setValue("TOTAL_DED", totalDeduction);
               Log.consoleLog(ifr, "TOTAL_DED==> " + totalDeduction);
               ifr.setStyle("TOTAL_DED", "disable", "true");
               ifr.setStyle("Gross_Salary", "disable", "true");
               String irfRefNo=valid.getValue(ifr,resultObj,"IR_REF_NO",""); 
               Log.consoleLog(ifr, "irfRefNo==> " + irfRefNo);
               ifr.setStyle("IR_Ref_Num", "disable", "true");
               
              }
              else {
                  String[] ErrorData = CheckError.split("#");
                  apiErrorCode = ErrorData[0];
                  apiErrorMessage = ErrorData[1];
              }
          } else {
              response = "No response from the server.";
              apiErrorMessage = "FAIL";
          }
          if (apiErrorMessage.equalsIgnoreCase("")) {
              apiStatus = "SUCCESS";
          } else {
              apiStatus = "ERROR";
          }
//
//          if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//              return RLOS_Constants.SUCCESS;
//          }
//            } else {
//                response = "No response from the server.";
//                apiErrorMessage = "FAIL";
//            }
//            if (apiErrorMessage.equalsIgnoreCase("")) {
//                apiStatus = RLOS_Constants.SUCCESS;
//            } else {
//                apiStatus = RLOS_Constants.ERROR;
//            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
            	return RLOS_Constants.ERROR + ":" +apiErrorMessage;
            } else {
                JSONObject jsonObject = new JSONObject();
                //jsonObject.put("NAME", name);
                jsonObject.put("Designation", designation);
                Log.consoleLog(ifr, "Designation==> " + designation);
                jsonObject.put("PresentWorkingBranchDPCode", presentWorkingBranchDPCode);
                Log.consoleLog(ifr, "PresentWorkingBranchDPCode==> " + presentWorkingBranchDPCode);
                jsonObject.put("Probation", probation);
                Log.consoleLog(ifr, "Probation==> " + probation);
                jsonObject.put("Warning", warning);
                Log.consoleLog(ifr, "Warning==> " + warning);
                jsonObject.put("Name", name);
                Log.consoleLog(ifr, "Name==> " + name);
                jsonObject.put("DateOfJoining",dateOfJoining);
                Log.consoleLog(ifr, "DateOfJoining==> " + dateOfJoining);
                jsonObject.put("DateOfRetirement",dateOfRetirement);
                Log.consoleLog(ifr, "DateOfRetirement==> " + dateOfRetirement);
                jsonObject.put("IrStatus",irStatus);
                Log.consoleLog(ifr, "IrStatus==> " + irStatus);
                jsonObject.put("ExServicesMen",exServicesMen);
                Log.consoleLog(ifr, "ExServicesMen==> " + exServicesMen);
                jsonObject.put("Status",status);
                Log.consoleLog(ifr, "Status==> " + status);
                jsonObject.put("SalaryAccount",salaryAccount);
                Log.consoleLog(ifr, "SalaryAccount==> " + salaryAccount);
                jsonObject.put("Gross",gross);
                Log.consoleLog(ifr, "Gross==> " + gross);
                jsonObject.put("NetSalary",netSalary);
                Log.consoleLog(ifr, "NetSalary==> " + netSalary);
                jsonObject.put("TotalDeduction",totalDeduction);
                Log.consoleLog(ifr, "TotalDeduction==> " + totalDeduction);
                
                
                jsonObject.put("Gender",gender);
                Log.consoleLog(ifr, "Gender==> " + gender);
               // jsonObject.put("DateOfBirth",totalDeduction);
                
                
               // jsonObject.put("branch", branch);
               
              //  ifr.setValue("working_branch_DP_code", branchDPCode);

                //  IR_Status
//Ex_Serviceman
//Employee_Status
//working_branch_DP_code
//				jsonObject.put("LOANACCNO", loanAccNum);
//				jsonObject.put("LOANAMOUNT", loanAmount);
                return jsonObject.toJSONString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_HRMS===>" + e);
            Log.errorLog(ifr, "Exception/CBS_HRMS===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" +apiErrorMessage;

    }

    private String hrmsKnockOffRuleVL(IFormReference ifr, JSONObject resultObj) {
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String mobileNumber = ifr.getValue("Portal_T_MobileNumber").toString();
        String panNumFrmHrm = resultObj.get("PAN").toString();
        Log.consoleLog(ifr, "mobileNumber hrms===>" + mobileNumber);
        String panNumber = "";
        String status = "Active";
        String query = "SELECT PANNUMBER FROM LOS_TRN_CUSTOMERSUMMARY WHERE WINAME='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        Log.consoleLog(ifr, "query for hrms knock off rules===>" + query);
        List<List<String>> responseForPan = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "responseForPan===>" + responseForPan);
        Log.consoleLog(ifr, "resultObj===>" + resultObj);
        Log.consoleLog(ifr, "panNumFrmHrm===>" + panNumFrmHrm);
        Log.consoleLog(ifr, "panNumber===>" + panNumber);
        for (List<String> panNumFrmDb : responseForPan) {
        	 Log.consoleLog(ifr, "panNumFrmDb inside ===>");
            panNumber = panNumFrmDb.get(0);
            Log.consoleLog(ifr, "panNumber ===>" +panNumber);
        }
        if (resultObj.size() == 0) {
        	 Log.consoleLog(ifr, "resultObj ===>" );
            return AccelatorStaffConstant.EMPTY_RESPONSE_MESSAGE;
        }
        if (!compare(panNumber, panNumFrmHrm)) {
        	 Log.consoleLog(ifr, "panNumber compare panNumFrmHrm ===>" );
            return AccelatorStaffConstant.PAN_ERROR_MESSAGE;
        }
        if (!compare("Active", resultObj.get("STATUS").toString())) {
        	 Log.consoleLog(ifr, "STATUS===>" );
            return AccelatorStaffConstant.STATUS_ACTIVE_MESSAGE;
        }
       

        boolean res1 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter1);
        boolean res2 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter2);
        boolean res3 = isDateInFormat(resultObj.get("DATE_OF_RETIREMENT").toString(), formatter3);
        String format = "";
        if (res1) {
        	Log.consoleLog(ifr, "res1===>" );
            format = "MM-dd-yyyy";
        } else if (res2) {
        	Log.consoleLog(ifr, "res2===>" );
            format = "yyyy-MM-dd";
        } else {
        	Log.consoleLog(ifr, "res3===>" );
            format = "dd-MM-yyyy";
        }
        if (!isEligibleForLoan(resultObj.get("DATE_OF_RETIREMENT").toString(), LocalDate.now(), format)) {
        	  Log.consoleLog(ifr, "DATE_OF_RETIREMENT===>");
            return AccelatorStaffConstant.RETIRING_STAFF_MESSAGE;
       }
         if (compare("Y", resultObj.get("IR_STATUS").toString())) {
        	 Log.consoleLog(ifr, "IR_STATUS===>");
            return  "WARNING : " + AccelatorStaffConstant.IR_STATUS_MESSAGE;
        }
        /*  if (compare("Yes", resultObj.get("EX_SERVICEMEN").toString())) {
            return AccelatorStaffConstant.EX_SERVICEMAN_MESSAGE;
        }*/
        return "SUCCESS";
    }
}
