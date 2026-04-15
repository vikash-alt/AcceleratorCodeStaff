/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class CustomerAccountSummaryPAPL {

    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();
    AadharVault av = new AadharVault();

    public String executeCustomerAccountSummary(IFormReference ifr, String ProcessInstanceId) {

        Log.consoleLog(ifr, "Entered into executeCustomerAccountSummary...");

        String apiName = "customerAccountSummary";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {
            JSONObject obj = new JSONObject();

//        String ErrorCode = "";
//        String ErrorMessage = "";
            String mobileNumber = pcm.getMobileNumber(ifr);//Modified by Ahmed on 16-07-2024
            Log.consoleLog(ifr, "Inside CustomerAccountSummary: executeCustomerAccountSummary mobilenumber=====>"+mobileNumber);
            String customerId = getCustomerIDFromPAPLMaster(ifr, mobileNumber);
            Log.consoleLog(ifr, "customerId==>"+customerId);
            String Count = getCountCustomerData(ifr);

            Log.consoleLog(ifr, "Count==>" + Count);
            if (Integer.parseInt(Count) == 0) {
                Log.consoleLog(ifr, "Customer Account Summary Execution is in progress...");
//                Date currentDate = new Date();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//                String formattedDate = dateFormat.format(currentDate);
                //mobileNumber = pcm.getMobileNumber(ifr);
                String panNumber = "";

                String BankCode = pcm.getConstantValue(ifr, "CBS360V2STAFF", "BANKCODE");
                String Channel = pcm.getConstantValue(ifr, "CBS360V2STAFF", "CHANNEL");
                String UserId = pcm.getConstantValue(ifr, "CBS360V2STAFF", "USERID");
                String TBranch = pcm.getConstantValue(ifr, "CBS360V2STAFF", "TransactionBranch");

                request = "{\n" 
                        + "    \"input\": {\n"
                        + "        \"SessionContext\": {\n"
                        + "            \"SupervisorContext\": {\n"
                        + "                \"PrimaryPassword\": \"\",\n"
                        + "                \"UserId\": \"\"\n"
                        + "            },\n"
                        + "            \"BankCode\": \"" + BankCode + "\",\n"
                        + "            \"Channel\": \"" + Channel + "\",\n"
                        + "            \"ExternalBatchNumber\": \"\",\n"
                        + "            \"ExternalReferenceNo\": \""+ cm.getCBSExternalReferenceNo() +"\",\n"
                        + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
                        + "            \"LocalDateTimeText\": \"\",\n"
                        + "            \"OriginalReferenceNo\": \"\",\n"
                        + "            \"OverridenWarnings\": \"\",\n"
                        + "            \"PostingDateText\": \"\",\n"
                        + "            \"ServiceCode\": \"\",\n"
                        + "            \"SessionTicket\": \"\",\n"
                        + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
                        + "            \"UserId\": \"" + UserId + "\",\n"
                        + "            \"UserReferenceNumber\": \"\",\n"
                        + "            \"ValueDateText\": \"\"\n"
                        + "        },\n"
                        + "        \"ExtUniqueRefId\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                        + "        \"CustomerId\": \"" + customerId + "\",\n"
                        + "        \"MobileNo\":\""+ mobileNumber + "\",\n"
                        + "        \"AccountNumber\": \"\",\n"
                        + "        \"DebitCard\":\"\",\n"
                        + "        \"PanNo\":\"\",\n" 
                        + "        \"AadhaarNo\" : \"\",\n"
                        + "        \"PassportNo\": \"\"\n"
					
                        + "    }\n"
                        + "}";

                response = cm.getWebServiceResponse(ifr, apiName, request);
                Log.consoleLog(ifr, "Response===>" + response);
                String CustomerID = "";

                String aadharVaultRefNo = "";
                if (!response.equalsIgnoreCase("{}")) {
                    JSONParser parser = new JSONParser();
                    JSONObject OutputJSON = (JSONObject) parser.parse(response);
                    JSONObject resultObj = new JSONObject(OutputJSON);

                    String body = resultObj.get("body").toString();
                    JSONObject bodyJSON = (JSONObject) parser.parse(body);
                    JSONObject bodyObj = new JSONObject(bodyJSON);

                    String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                    if (CheckError.equalsIgnoreCase("true")) {

                        String city = "", state = "", zipCode = "";

                        String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
                        JSONObject CustomerAccountSummaryResponseJSON = (JSONObject) parser.parse(CustomerAccountSummaryResponse);
                        JSONObject CustomerAccountSummaryResponseJSONObj = new JSONObject(CustomerAccountSummaryResponseJSON);
                        CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
                        Log.consoleLog(ifr, "CustomerID==>" + CustomerID);

                        //Added by Ahmed
                        String salaryAccountNo = pcm.getSalaryAccountNo(ifr, "PAPL", CustomerID);
                        Log.consoleLog(ifr, "salaryAccountNo===>" + salaryAccountNo);
                        //Ended by Ahmed

                        String currentStatus = "";
                        String branchDtls = "";

                        String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber").toString();

                        String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
                        String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
                        String bCode = "";
                        try {
                            if (homeBranch.contains("-")) {
                                String[] BRSet = homeBranch.split("-");
                                //  branchCode = BRSet[0];
                            }
                        } catch (Exception e) {
                            Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
                        }
                        String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
                        String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
                        String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
                        String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
                        String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
                        String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
                        String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice").toString();
                        String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
                        String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();

//                        aadharVaultRefNo = av.pushData(ifr, aadharNo);
//                        Log.consoleLog(ifr, "aadharVaultRefNo==>" + aadharVaultRefNo);
//                        if (aadharVaultRefNo.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                            return RLOS_Constants.ERROR;
//                        }
                        String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName").toString();
                        String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName").toString();
                        String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
                        panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
                        String phoneNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
                        String mobile_Number = mobileNumber;
                        //String mobile_Number = String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
                        String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
                        String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
                        String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
                        String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
                        String CustomerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
                        String MaritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
                        String ProfessionCategory = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();

                        // String PermCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
                        //String PermState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
                        //String PermZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
                        String CASADetailsDTO = CustomerAccountSummaryResponseJSONObj.get("CASADetailsDTO").toString();
                        Log.consoleLog(ifr, "CASADetailsDTO==>" + CASADetailsDTO);
                        String AcyAmount = "";
                        JSONArray CASADetailsDTOJSONJSON = (JSONArray) parser.parse(CASADetailsDTO);
                        for (int i = 0; i < CASADetailsDTOJSONJSON.size(); i++) {
                            System.out.println("CASADetailsDTOResponseObj==>" + CASADetailsDTOJSONJSON.get(i));
                            String InputString = CASADetailsDTOJSONJSON.get(i).toString();
                            JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);
                            JSONObject InputStringResponseJSONJSONObj = new JSONObject(InputStringResponseJSON);
                            JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
                            String AccountId = AtrresultObj.get("AccountId").toString();

                            Log.consoleLog(ifr, "salaryAccountNo===>" + salaryAccountNo);
                            Log.consoleLog(ifr, "AccountId=========>" + AccountId);
                            if (salaryAccountNo.trim().equalsIgnoreCase(AccountId.trim())) {

                                AcyAmount = AtrresultObj.get("AcyAmount").toString();
                                Log.consoleLog(ifr, "AcyAmount===>" + AcyAmount);

                                bCode = AtrresultObj.get("branchCode").toString().trim();
                                Log.consoleLog(ifr, "bCode=======>" + bCode);

                            }

                        }

                        //================Added by Ahmed on 04-04-2024 for BranchCode Logic=================
                        Log.consoleLog(ifr, "#BranchCode Found from the System==>" + bCode);

                        //Commented by Ahmed on 08-08-2024 for PermCity,State,ZipCode
//                        if (!bCode.equalsIgnoreCase("")) {
//
//                            if (bCode.length() == 4) {
//                                bCode = "0" + bCode;//Appending Zero in the master for 4 digit branchcode
//                            }
//
//                            String custParamsDataQry = ConfProperty.getCommonPropertyValue("PAPLBranchCodeQuery");
//                            custParamsDataQry = custParamsDataQry.replaceAll("#BRANCHCODE#", bCode);
//                            Log.consoleLog(ifr, "custParamsDataQry==>" + custParamsDataQry);
//                            //String custParamsDataQry = "SELECT CITY,STATE,ZIPCODE FROM LOS_M_BRANCH WHERE BRANCHCODE='" + bCode + "' AND ROWNUM=1";
//                            List< List< String>> Result = ifr.getDataFromDB(custParamsDataQry);
//                            Log.consoleLog(ifr, "#Result===>" + Result.toString());
//                            if (!Result.isEmpty()) {
//                                city = Result.get(0).get(0);
//                                state = Result.get(0).get(1);
//                                zipCode = Result.get(0).get(2);
//                            }
//                            Log.consoleLog(ifr, "City====>" + city);
//                            Log.consoleLog(ifr, "State===>" + state);
//                            Log.consoleLog(ifr, "Zip=====>" + zipCode);
//                        }
                        //Added by Ahmed  Ahmed on 08-08-2024 for PermCity,State,ZipCode
                        city = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
                        state = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
                        zipCode = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();

                        //================Ended by Ahmed on 04-04-2024 for BranchCode Logic=================
                        String CustomerSummaryInsertQuery = "INSERT INTO LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
                                + "(EMAILID,CUSTOMERID,HOMEBRANCH,ACCOUNTID,CURRENTSTATUS,BRANCHDTLS,BRANCHCODE,"
                                + "PERMADDRESS1,PERMADDRESS2,PERMADDRESS3,DATEOFBIRTH,SERVICINGBRANCH,MAILCITY,PHONENUMBEROFFICE,"
                                + "CUSTOMERFLAG,AADHARNO,CUSTOMERMIDDLENAME,CUSTOMERLASTNAME,PANNUMBER,PHONENUMBER,MOBILENUMBER"
                                + ",MAILADDRESS1,MAILADDRESS2,MAILADDRESS3,FATHERNAME,WINAME,"
                                + "CUSTOMERFIRSTNAME,GENDER,MARITALSTATUS,PROFESSIONCATEGORY,ACYAMOUNT,"
                                + "PERMCITY,PERMSTATE,PERMZIP) "
                                + "VALUES ( '" + emailID + "','" + CustomerID + "',"
                                + "'" + homeBranch + "','" + accountIdString + "',"
                                + "'" + currentStatus + "','" + branchDtls + "',"
                                + "'" + bCode + "','" + permAddress1 + "',"
                                + "'" + permAddress2 + "','" + permAddress3 + "',"
                                + "'" + dateOfBirth + "','" + servicingBranch + "',"
                                + "'" + mailCity + "','" + phoneNumberOffice + "',"
                                + "'" + customerFlag + "','" + aadharVaultRefNo + "',"
                                + "'" + customerMiddleName + "','" + customerLastName + "',"
                                + "'" + panNumber + "','" + phoneNumber + "','" + mobile_Number + "',"
                                + "'" + mailAddress1 + "','" + mailAddress2 + "','" + mailAddress3 + "',"
                                + "'" + fatherName + "','" + ProcessInstanceId + "',"
                                + "'" + CustomerFirstName + "','" + CustomerSex + "','" + MaritalStatus + "',"
                                + "'" + ProfessionCategory + "','" + AcyAmount + "',"
                                + "'" + city + "','" + state + "','" + zipCode + "')";
                        Log.consoleLog(ifr, "CustomerSummaryInsertQuery==>" + CustomerSummaryInsertQuery);
                        ifr.saveDataInDB(CustomerSummaryInsertQuery);
                        addAdditionalData(ifr, CustomerID, ProcessInstanceId);
                        Log.consoleLog(ifr, "Data Inserted into CustomerSummaryInsertQuery");
                        // }
                        Log.consoleLog(ifr, "Data Inserted into ProcessInstanceId" + ProcessInstanceId);

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

                cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                        apiErrorCode, apiErrorMessage, apiStatus);

                if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                    obj.put("aadharNo", aadharVaultRefNo);
                    obj.put("apiStatus", RLOS_Constants.SUCCESS);
                    return obj.toString();
                }

//                String APIStatus = "";
//                String APIStatusSend = "";
//                if (ErrorMessage.equalsIgnoreCase("")) {
//                    APIStatus = "SUCCESS";
//                    APIStatusSend = RLOS_Constants.SUCCESS;
//                } else {
//                    APIStatus = "FAIL";
//                    APIStatusSend = RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//                }
//                cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                        ErrorCode, ErrorMessage, APIStatus);
//
//                if (APIStatusSend.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                    obj.put("aadharNo", aadharVaultRefNo);
//                    obj.put("apiStatus", RLOS_Constants.SUCCESS);
//                    return obj.toString();
//                } else {
//                    return APIStatusSend;
//                }
            } else {//Added by Ahmed on 14-06-2024 for PAPL multiple times recurisve Aadhar Issue
                Log.consoleLog(ifr, "Customer Account Summary Executed already!!");
//                String aadharNumber = av.getAadharNoFromVault(ifr, "PAPL", "");
//                obj.put("aadharNo", aadharNumber);
//                obj.put("apiStatus", RLOS_Constants.SUCCESS);
//                return obj.toString();
//                
                HashMap<String, String> customerdetails = new HashMap<>();
                customerdetails.put("MobileNumber", mobileNumber);
                customerdetails.put("customerId", customerId);

                CustomerAccountSummary cas = new CustomerAccountSummary();
                String aadharNumber = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
                obj.put("aadharNo", aadharNumber);
                obj.put("apiStatus", RLOS_Constants.SUCCESS);
                return obj.toString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        } finally {

        }
        return RLOS_Constants.ERROR;
    }

    public String getCountCustomerData(IFormReference ifr) {
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery = "SELECT COUNT(*) FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
        List<List<String>> Result1 = cf.mExecuteQuery(ifr, CountQuery, "CountQuery:");
        String Count = "0";//Modified by Ahmed on 14-06-2024 for parsing purpose
        if (!Result1.isEmpty()) {
            Count = Result1.get(0).get(0);
        }
        return Count;
    }

    private void addAdditionalData(IFormReference ifr, String CustomerID, String ProcessInstanceId) {

        //Modified by Ahmed for getting queryReadingFromProp
        // String getData = "select NETSALARY from los_mst_papl where customer_id= '" + CustomerID + "' and rownum=1";
        String getData = ConfProperty.getQueryScript("PAPL_UPDATEDGDTLSQRY").replace("#CUSTOMER_ID#", CustomerID);

        Log.consoleLog(ifr, "#addAdditionalData===>" + "addAdditionalData");
        Log.consoleLog(ifr, " addAdditionalData Query==>" + getData);
        Log.consoleLog(ifr, " ProcessInstanceId ProcessInstanceId==>" + ProcessInstanceId);
        List< List< String>> output = ifr.getDataFromDB(getData);
        String grossSalary = "";

        if (output.size() > 0) {
            grossSalary = output.get(0).get(0);
        }

        if (!grossSalary.isEmpty()) {
            try {
                // Convert the string to a numeric value (assuming it's a decimal number)
                double grossSalaryValue = Double.parseDouble(grossSalary);

                // Perform the division
                double result = grossSalaryValue;

                // Round off the result to 2 decimal places
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                String roundedResult_grossSalary = decimalFormat.format(result);// field

                double roundedGrossSalary = Double.parseDouble(roundedResult_grossSalary);

                System.out.println("Rounded Result: " + roundedGrossSalary);
                Log.consoleLog(ifr, "roundedGrossSalary===>" + roundedGrossSalary);

                double NTH_Amount = roundedGrossSalary * 0.25;
                double max_NTH_Amount = Math.max(NTH_Amount, 10000);
                String final_NTH_Amount = Double.toString(max_NTH_Amount); ///field
                System.out.println(final_NTH_Amount);
                Log.consoleLog(ifr, "final_NTH_Amount===>" + final_NTH_Amount);
                double NTH_percentage = (max_NTH_Amount / roundedGrossSalary) * 100;

                String final_NTH_percentage = Double.toString(NTH_percentage); ///field
                Log.consoleLog(ifr, "final_NTH_percentage===>" + final_NTH_percentage);

                String qryUpdateNetSalDtls = ConfProperty.getQueryScript("PAPL_UPDATENETSALDTLSQRY")
                        .replace("#GROSSSALARY#", roundedResult_grossSalary)
                        .replace("#NTHAMOUNT#", final_NTH_Amount)
                        .replace("#NTHPERCENTAGE#", final_NTH_percentage)
                        .replace("#WINAME#", ProcessInstanceId);
                Log.consoleLog(ifr, "qryUpdateNetSalDtls" + qryUpdateNetSalDtls);
                ifr.saveDataInDB(qryUpdateNetSalDtls);

//                String ACC_SUMMARY_Update = "UPDATE LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
                //                        + " SET "
                //                        + " GROSSSALARY =  '" + roundedResult_grossSalary + "', "
                //                        + " NTHAMOUNT = '" + final_NTH_Amount + "',"
                //                        + " NTHPERCENTAGE = '" + final_NTH_percentage + "' "
                //                        + " WHERE "
                //                        + " WINAME = '" + ProcessInstanceId + "' ";
                //                Log.consoleLog(ifr, "Update Query in ACC_SUMMARY_Update" + ACC_SUMMARY_Update);
                //                ifr.saveDataInDB(ACC_SUMMARY_Update);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric format for GrossSalary");
                Log.consoleLog(ifr, "Exception===>" + e);

            }
        }

    }

    public String getCustomerIDFromPAPLMaster(IFormReference ifr, String mobNumber) {
        //String query = "SELECT CUSTOMER_ID FROM LOS_MST_PAPL WHERE MOBILE_NO='" + mobNumber + "' AND ROWNUM=1 ";
       // String query = ConfProperty.getQueryScript("PAPL_GETCUSTIDQRY").replace("#MOBILE_NO#", mobNumber);
       String mob="";
       if(mobNumber.length()==12){
           mob=mobNumber.substring(2);
       }
       else if(mobNumber.length()==10){
           mobNumber="91"+mobNumber;
       }
       String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String query="select CUSTOMERID from los_trn_customersummary where  WINAME='"+processInstanceId+"'";
       
        Log.consoleLog(ifr, "getCustomerIDFromPAPLMaster===>"+query);
        //List<List<String>> list = cf.mExecuteQuery(ifr, query, "getCustomerIDFromPAPLMaster:");
        List<List<String>> list=ifr.getDataFromDB(query);
        if (!list.isEmpty()) {
            return list.get(0).get(0);
        }
        return "";
    }

}
