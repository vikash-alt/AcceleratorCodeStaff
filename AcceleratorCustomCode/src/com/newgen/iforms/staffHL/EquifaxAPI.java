package com.newgen.iforms.staffHL;

import com.newgen.dlp.integration.cbs.AadharVault;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author a.janani
 */
public class EquifaxAPI {

    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods cm = new APICommonMethods();
    CRGGenerator crg= new CRGGenerator();
    public String getEquifaxCIBILScore(IFormReference ifr, String ProcessInstanceId, String aadharNo,
            String productType, String loanAmount, String applicantType)
            throws ParseException {

        String apiName = "EQUIFAX";
        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->apiName: " + apiName);
        
        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productType: " + productType);
        String request = "";
        String responseBody = "";
        double PastDueAmount = 0;
        String TotalPastDue = "";
        String NoOfPastDueAccounts = "";
        String PaymentStatus = "";
        String NoOfWriteOffs = "";
        String score = "";
        String EMAILID = "";
        String insertionOrderid = "";
        int totalNonEMICount = 0;
        int consolidated_emiAmnt = 0;
        String overAllAccountTypes = "";
        String overAllAccountStatus = "";
        String overAllOpendate = "";
        String overAllSuitFiledWillfulDefaultWrittenOffStatus = "";
        String totaldaysPastDue = "";
        String NPA = "No";
        String dpd = "0";
        String writeOFF = "No";
        String settledHistory = "No";
        String newToCredit = "No";
        try {

            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
                    formattedDate = "", addressLine3 = "", city = "", state = "", pincode = "",
                    gender = "", gendercode = "", stateCode = "", mobileNo = "",panNo="",votterId="";

            String Query1 = "";
            String Query2 = "";
          
           
            if (productType.equalsIgnoreCase("PAPL")) {
                Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

                Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
                Log.consoleLog(ifr, "Query2 " + Query2);

            } else {
                if (applicantType.contains("~")) {
                    insertionOrderid = applicantType.split("~")[1];
                    applicantType = applicantType.split("~")[0];
                    Query1 = "select a.customerid, b.firstname,b.lastname,c.kyc_Id,c.kyc_no,to_char(b.date_of_birth_hl,'dd-MM-YYYY')dob,b.gender,b.mobileno,b.EMAILID from los_nl_basic_info a ,\n"
                            + "los_l_basic_info_i b, LOS_NL_KYC c \n"
                            + "where a.f_key = b.f_key and b.f_key=c.f_key "
                            + "and a.pid='" + ProcessInstanceId + "' and a.insertionorderid='" + insertionOrderid + "'";

                    Query2 = "select line1,line2,line3,city_town_village,state,pincode "
                            + "from LOS_NL_Address where F_KEY =(select F_KEY from los_nl_basic_info "
                            + " where PID ='" + ProcessInstanceId + "' and insertionorderid='" + insertionOrderid + "')\n"
                            + " and addresstype='P'";
                    Log.consoleLog(ifr, "Query2 " + Query2);

                } 

            }
            panNumber = "";
    	    String voterId = "";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxScore->Query1: " + Query1);
            List< List< String>> Result = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result: " + Result.toString());
            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
            //"^[A-Za-z ]+$";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result pattern: " + pattern);
            if (Result.size() > 0) {
                Log.consoleLog(ifr, "Processing customer data for PID: LOS-00000000000012081");
                
                // Basic customer information
                customerId = Result.get(0).get(0);
                Log.consoleLog(ifr, "Customer ID: " + customerId);
                
                firstName = Result.get(0).get(1).replaceAll("[ .]", "");
                lastName = Result.get(0).get(2).replaceAll("[ .]", "");
                Log.consoleLog(ifr, "Raw Name: " + Result.get(0).get(1) + " " + Result.get(0).get(2));
                Log.consoleLog(ifr, "Processed Name: " + firstName + " " + lastName);

               
                if (lastName.equalsIgnoreCase("{}") || 
                    lastName.equalsIgnoreCase("{") || 
                    lastName.equalsIgnoreCase("}")) {
                    Log.consoleLog(ifr, "Cleaning invalid lastName characters");
                    lastName = "";
                }
                Log.consoleLog(ifr, "Processed lastName123: " + lastName);
                Log.consoleLog(ifr, "Processed lastName: " + !Pattern.matches(pattern, firstName));
                
                if (firstName.length() < 2) {
                    Log.consoleLog(ifr, "Validation failed - First name too short: " + firstName);
                    return RLOS_Constants.ERROR + "Name is less than 1 character ";
                } 
                
                
                else if (!Pattern.matches(pattern, firstName)) {
                    Log.consoleLog(ifr, "Validation failed - Invalid characters in name: " + firstName);
                    return RLOS_Constants.ERROR + "Name contains invalid special characters";
                }
                
                // Existing fields
                dateofBirth = Result.get(0).get(5);
                gender = Result.get(0).get(6);
                mobileNo = Result.get(0).get(7);
                EMAILID = Result.get(0).get(8);
                
          
                Log.consoleLog(ifr, "Extracted DOB: " + dateofBirth + ", Gender: " + gender + ", Mobile: " + mobileNo);
                
                // Handle empty email
                if (EMAILID == null || EMAILID.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "Empty email detected, setting default");
                    EMAILID = "abcd@gmail.com";
                }
                Log.consoleLog(ifr, "Final Email: " + EMAILID);
                
                // Initialize both ID types
                panNumber = "";
                 voterId = "";
                Log.consoleLog(ifr, "Starting KYC processing for " + Result.size() + " records");
                
                // Process all KYC records
                for (int i = 0; i < Result.size(); i++) {
                    List<String> row = Result.get(i);
                    String kycType = row.get(3);
                    String kycValue = row.get(4);
                    Log.consoleLog(ifr, "Processing row " + i + " - KYC Type: " + kycType + ", Value: " + kycValue);
                    
                    if (kycValue != null && !kycValue.isEmpty()) {
                        if ("TAXID".equalsIgnoreCase(kycType)) {
                            panNumber = kycValue.trim();
                            Log.consoleLog(ifr, "Found PAN Number: " + panNumber);
                        } 
                        else if ("VID".equalsIgnoreCase(kycType)) {
                            voterId = kycValue.trim();
                            Log.consoleLog(ifr, "Found Voter ID: " + voterId);
                        }
                        
                        else if ("AA".equalsIgnoreCase(kycType)) {
                        	aadharNo = kycValue.trim();
                            Log.consoleLog(ifr, "Found aadharNo Ref No: " + aadharNo);
                            AadharVault objAV = new AadharVault();
                            aadharNo = objAV.getDataByReferenceKey(ifr, aadharNo);
                        }
                        else if (panNumber.isEmpty() && kycValue.matches("[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}")) {
                            panNumber = kycValue.trim();
                            Log.consoleLog(ifr, "Auto-detected PAN format: " + panNumber);
                        }
                    } else {
                        Log.consoleLog(ifr, "Empty KYC value in row " + i);
                    }
                }
                
                // Set empty strings if not found
                if (panNumber == null || panNumber.isEmpty()) {
                    Log.consoleLog(ifr, "No PAN number found in KYC records");
                    panNumber = "";
                }
                if (voterId == null || voterId.isEmpty()) {
                    Log.consoleLog(ifr, "No Voter ID found in KYC records");
                    voterId = "";
                }
                
                Log.consoleLog(ifr, "Final KYC Results - PAN: " + panNumber + ", Voter ID: " + voterId);
                Log.consoleLog(ifr, "Customer data processing completed successfully");
            } else {
                Log.consoleLog(ifr, "No records found for the given query parameters");
            }
            
            
            
           
            
            String inputDate = dateofBirth;
            if (gender.equalsIgnoreCase("Male")) {
                gendercode = "M";
            } else if (gender.equalsIgnoreCase("Female")) {
                gendercode = "F";
            } else {
                gendercode = "T";
            }

            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->firstName: " + firstName);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->lastName: " + lastName);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PAN: " + panNumber);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->birthDate: " + dateofBirth);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->gender: " + gender);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);

            if (mobileNo.startsWith("91")) {
                mobileNo = mobileNo.substring(2); // Remove 91
                if (mobileNo.length() == 12 && mobileNo.startsWith("91")) {
                    mobileNo = mobileNo.substring(2);
                }
            }
            if (mobileNo.length() > 10) {
                mobileNo = mobileNo.substring(mobileNo.length() - 10);
            }

            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query2: " + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result1: " + Result1.toString());

            if (Result1.size() > 0) {
                addressLine1 = Result1.get(0).get(0);
                addressLine2 = Result1.get(0).get(1);
                addressLine3 = Result1.get(0).get(2);
                city = Result1.get(0).get(3);
                state = Result1.get(0).get(4);
                pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine1: " + addressLine1);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine2: " + addressLine2);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine3: " + addressLine3);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->City: " + city);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->State: " + state);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Pincode: " + pincode);

            String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query3: " + Query3);
            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result3: " + Result3.toString());
            if (Result3.size() > 0) {
                stateCode = Result3.get(0).get(0);
            }

            if (stateCode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->StateCode founds to be empty for " + state + " in LOS_MST_STATE table.");
                return RLOS_Constants.ERROR + "StateCode not found";
            }
            if (addressLine1.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->addressLine1 not found");
                return RLOS_Constants.ERROR + "addressLine1 not found";
            }
            if (pincode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->pincode not found");
                return RLOS_Constants.ERROR + "pincode not found";
            }
          
            try {
                if (!dateofBirth.equalsIgnoreCase("")) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
                    formattedDate = date.format(outputFormatter);
                }
            } catch (Exception e) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception in date formatting===>" + e);
            }

            String DOB = formattedDate;

           

            String productCode = pcm.getProductCode(ifr);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productCode: " + productCode);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->subProductCode: " + subProductCode);

            String excludedEMIAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "EQUIFAXCONF", "EMIACCTTYPE");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);

            String excludedOwners = pcm.getParamConfig(ifr, productCode, subProductCode, "EQUIFAXCONF", "OWNERTYPE");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwners: " + excludedOwners);

            String excludedOwnersGI = pcm.getParamConfig(ifr, productCode, subProductCode, "EQUIFAXCONF", "OWNERTYPEGI");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwnersGI: " + excludedOwnersGI);
            String loanSelected = pcm.getLoanSelected(ifr);
            String InquiryPurpose="";
            if(loanSelected.equalsIgnoreCase("Canara Budget") 
            		|| loanSelected.equalsIgnoreCase("Canara Pension")
            		|| loanSelected.equalsIgnoreCase("Vehicle Loan") ||
            		productType.equalsIgnoreCase("PAPL") ||
            		loanSelected.equalsIgnoreCase("PAPL") )
            {
            	InquiryPurpose="Personal Loan";
            	InquiryPurpose=pcm.getConstantValue(ifr, "EQUIFAXAPI", InquiryPurpose);
            }
            subProductCode = pcm.getSubProductCode(ifr);
            
            if(subProductCode.equalsIgnoreCase("CANTWO"))
            {
            	loanSelected="Two-wheeler Loan";
            	InquiryPurpose=pcm.getConstantValue(ifr, "EQUIFAXAPI", loanSelected);
            }
            if(subProductCode.equalsIgnoreCase("CANFOUR"))
            {
            	loanSelected="Auto Loan";
            	InquiryPurpose=pcm.getConstantValue(ifr, "EQUIFAXAPI", loanSelected);
            }
            
            if(panNumber==null ||  panNumber.equals(""))
            {
            	panNumber="";
            	Log.consoleLog(ifr,"inside if Valid PAN for processing: " + panNumber);
            }
            else if ( panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {
              
            	//panNumber=panNumber;
           	 Log.consoleLog(ifr,"Valid PAN for processing: " + panNumber);
           }
            else
            {
            	 Log.consoleLog(ifr,"Valid PAN for processing: " + "PAN number Not Listed ");
            	 return RLOS_Constants.ERROR;
            }
            request = "{\n"
                    + "\"RequestBody\": {\n"
                    + "        \"InquiryPurpose\": \"" + InquiryPurpose +  "\",\n"
                    + "        \"TransactionAmount\": \"" + loanAmount + "\",\n"
                    + "        \"FirstName\": \"" + firstName + "\",\n"
                    + "        \"MiddleName\": \"\",\n"
                    + "        \"LastName\": \"" + lastName + "\",\n"
                    + "        \"InquiryAddresses\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"AddressLine1\": \"" + addressLine1 + "\",\n"
                    + "                \"City\": \"" + city + "\",\n"
                    + "                \"State\": \"" + stateCode + "\",\n"
                    + "                \"AddressType\": [\n"
                    + "                    \"H\"\n"
                    + "                ],\n"
                    + "                \"Postal\": \"" + pincode + "\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"InquiryPhones\": [\n"
                    + "           \n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"Number\": \"" + mobileNo + "\",\n"
                    + "                \"PhoneType\": [\n"
                    + "                    \"M\"\n"
                    + "                ]\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"EmailAddresses\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\", \n"
                    + "                \"Email\": \"" + EMAILID + "\",	\n"
                    + "                \"EmailType\": [\n"
                    + "                    \"P\"\n"
                    + "                ]\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"IDDetails\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"IDValue\": \"" + aadharNo + "\",\n"
                    + "                \"IDType\": \"M\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"2\",\n"
                    + "                \"IDValue\": \"" + panNumber + "\",\n"
                    + "                \"IDType\": \"T\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"3\",\n"
                    + "               \"IDValue\": \"" + voterId + "\",\n"
                    + "                \"IDType\": \"V\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"4\",\n"
                    + "                \"IDValue\": \"\",\n"
                    + "                \"IDType\": \"D\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            }\n"
                  
                    + "        ],\n"
                    + "        \"DOB\": \"" + DOB + "\",\n"
                    + "        \"Gender\": \"" + gendercode + "\" \n"
                    + "    },\n"
                    + "    \"Score\": [\n"
                    + "        {\n"
                    + "            \"Type\": \"ERS\",\n"
                    + "            \"Version\": \"4.5\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->***EQUIFAX API request==>" + request);
            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->EQUIFAX API RESPONSE==>" + responseBody);

            if (!responseBody.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
                JSONObject resultObj = new JSONObject(OutputJSON);
                Log.consoleLog(ifr, "resultObj==>" + resultObj);
                String stringBody = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body==>" + stringBody);
                JSONObject body = (JSONObject) OutputJSON.get("body");
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->body: " + body.toString());
                JSONObject CCRResponse = (JSONObject) body.get("CCRResponse");
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CCRResponse: " + CCRResponse.toString());
                JSONArray CIRReportDataLst = (JSONArray) CCRResponse.get("CIRReportDataLst");
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.toString());
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.size());
                
                   if (CIRReportDataLst.size() > 0) {
                    JSONObject firstReportData = (JSONObject) CIRReportDataLst.get(0);
                    
                    if (firstReportData.containsKey("Error")) {
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->firstReportData: " + firstReportData);
                        JSONObject errorObj = (JSONObject) firstReportData.get("Error");
                        String errorCode = errorObj.get("ErrorCode").toString();
                        String errorDesc = errorObj.get("ErrorDesc").toString();
                        
                        Log.consoleLog(ifr, "EquifaxAPI: ErrorCode - " + errorCode);
                        Log.consoleLog(ifr, "EquifaxAPI: ErrorDesc - " + errorDesc);
                        
                        if(errorDesc.equalsIgnoreCase("Consumer not found in bureau"))
                        {
                        	score="-1";
                        }
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorCode: " + errorCode);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorDesc: " + errorDesc);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->score: " + score);
                    }
                    
                   
                }
                
                for (int CIRReportDataLstCount = 0; CIRReportDataLstCount < CIRReportDataLst
                        .size(); CIRReportDataLstCount++) {
                    JSONObject CIRReportDataVal = (JSONObject) CIRReportDataLst.get(CIRReportDataLstCount);
               if(CIRReportDataVal.containsKey("CIRReportData"))
               {
                    JSONObject CIRReportData = (JSONObject) CIRReportDataVal.get("CIRReportData");
                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->CIRReportData: " + CIRReportData.toString());

                    JSONArray ScoreDetails = (JSONArray) CIRReportData.get("ScoreDetails");
                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->ScoreDetails: " + ScoreDetails.toString());

                    for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
                        JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);

                        if (ScoreDetailsObj.containsKey("Value") && ScoreDetailsObj.get("Value") != null) {
                        	score = ScoreDetailsObj.get("Value").toString();
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Value: " + score);
                        }
                    }
                    JSONObject RetailAccountsSummary = (JSONObject) CIRReportData.get("RetailAccountsSummary");
                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountsSummary: " + RetailAccountsSummary.toString());

                    if (RetailAccountsSummary.containsKey("NoOfWriteOffs")
                            && RetailAccountsSummary.get("NoOfWriteOffs") != null) {
                        NoOfWriteOffs = RetailAccountsSummary.get("NoOfWriteOffs").toString();
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfWriteOffs: " + NoOfWriteOffs.toString());
                    }
                    if (RetailAccountsSummary.containsKey("TotalPastDue")
                            && RetailAccountsSummary.get("TotalPastDue") != null) {
                        TotalPastDue = RetailAccountsSummary.get("TotalPastDue").toString();
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->TotalPastDue: " + TotalPastDue.toString());
                    }

                    if (RetailAccountsSummary.containsKey("NoOfPastDueAccounts")
                            && RetailAccountsSummary.get("NoOfPastDueAccounts") != null) {
                        NoOfPastDueAccounts = RetailAccountsSummary.get("NoOfPastDueAccounts").toString();
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfPastDueAccounts: " + NoOfPastDueAccounts.toString());
                    }
                    JSONArray RetailAccountDetails = (JSONArray) CIRReportData.get("RetailAccountDetails");
                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountDetails: " + RetailAccountDetails.toString());

                    for (int RetailAccountDetailsCount = 0; RetailAccountDetailsCount < RetailAccountDetails
                            .size(); RetailAccountDetailsCount++) {

                        if (!overAllAccountTypes.equalsIgnoreCase("")) {
                            overAllAccountTypes = overAllAccountTypes + ",";
                        }

                        if (!overAllAccountStatus.equalsIgnoreCase("")) {
                            overAllAccountStatus = overAllAccountStatus + ",";
                        }
                        if (!overAllOpendate.equalsIgnoreCase("")) {
                            overAllOpendate = overAllOpendate + ",";
                        }
                        if (!overAllSuitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("")) {
                            overAllSuitFiledWillfulDefaultWrittenOffStatus = overAllSuitFiledWillfulDefaultWrittenOffStatus + ",";
                        }

                        JSONObject RetailAccountDetailsObj = (JSONObject) RetailAccountDetails
                                .get(RetailAccountDetailsCount);

                        String accountType = cf.getJsonValue(RetailAccountDetailsObj, "AccountType");
                        String accountStatus = cf.getJsonValue(RetailAccountDetailsObj, "AccountStatus");
                        String openDate = cf.getJsonValue(RetailAccountDetailsObj, "DateOpened");
                       
                        String OwnershipType = cf.getJsonValue(RetailAccountDetailsObj, "OwnershipType");
                        String Open = cf.getJsonValue(RetailAccountDetailsObj, "Open");
                        String InstallmentAmount = cf.getJsonValue(RetailAccountDetailsObj, "InstallmentAmount");
                        overAllAccountTypes = overAllAccountTypes + accountType;
                        overAllAccountStatus = overAllAccountStatus + accountStatus;
                        overAllOpendate = overAllOpendate + openDate;
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->overAllOpendate: " + overAllOpendate);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->accountType: " + accountType);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->openDate: " + openDate);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->OwnershipType: " + OwnershipType);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Open: " + Open);

                        if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                            JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                            for (int j = 0; j < History48Months.size(); j++) {
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->expDate");
                                JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);

                                String daysPastDue = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PaymentStatus: " + PaymentStatus);
                                String key = cf.getJsonValue(History48MonthsObj, "key");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                YearMonth currentYearMonth = YearMonth.now();
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                YearMonth twelveMonthsAgo = currentYearMonth.minusMonths(12);
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->twelveMonthsAgo: " + twelveMonthsAgo);
                                // Check if the input date is within the last 12 months
                                if (!inputYearMonth.isBefore(twelveMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 12 months: ");
                                    totaldaysPastDue = totaldaysPastDue + daysPastDue;
                                }
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->totaldaysPastDue: " + totaldaysPastDue);

                            }

                        }

                        boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
                                excludedEMIAccnts, OwnershipType, excludedOwners, Open);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->emiStatus: " + emiStatus);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
                        if (emiStatus) {
                            if (!InstallmentAmount.isEmpty()) {
                                int totalEMI = Integer.parseInt(InstallmentAmount);
                                if (totalEMI < 0) {
                                    totalNonEMICount++;
                                }
                                consolidated_emiAmnt += totalEMI;
                            }
                        }
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);
                        boolean npaStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->npaStatus: " + npaStatus);

                        if (npaStatus) {
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                            if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + History48Months);
                                for (int j = 0; j < History48Months.size(); j++) {
                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                    String Asset_Classification = cf.getJsonValue(History48MonthsObj, "AssetClassificationStatus");
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Asset_Classification: " + Asset_Classification);
                                    String key = cf.getJsonValue(History48MonthsObj, "key");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                    YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                    YearMonth currentYearMonth = YearMonth.now();
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                    YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                    // Check if the input date is within the last 60 months
                                    if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
                                        if ((Asset_Classification.equalsIgnoreCase("SUB")
                                                || Asset_Classification.equalsIgnoreCase("DBT")
                                                || Asset_Classification.equalsIgnoreCase("LOS"))) {
                                            NPA = "Yes";
                                        }
                                    }
                                }
                            }
                        }

                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NPA: " + NPA);
                        boolean setteledHistoryStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->setteledHistoryStatus: " + setteledHistoryStatus);

                        if (setteledHistoryStatus) {
                            if ("Settled".equalsIgnoreCase(accountStatus) || "Closed Account".equalsIgnoreCase(accountStatus)) {
                                if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                    JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                    for (int j = 0; j < History48Months.size(); j++) {
                                        JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                        String paymentStatus = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->paymentStatus: " + paymentStatus);
                                        String key = cf.getJsonValue(History48MonthsObj, "key");
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                        YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                        YearMonth currentYearMonth = YearMonth.now();
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                        YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                        // Check if the input date is within the last 60 months
                                        if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
                                            if (paymentStatus.equalsIgnoreCase("SET")) {
                                                settledHistory = "Yes";
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->settledHistory: " + settledHistory);

                        boolean wroStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->wroStatus: " + wroStatus);

                        if (wroStatus) {
                            if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                for (int j = 0; j < History48Months.size(); j++) {
                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                    String key = cf.getJsonValue(History48MonthsObj, "key");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                    YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                    YearMonth currentYearMonth = YearMonth.now();
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                    YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                    String WriteOffAmount = "0";
                                    if (RetailAccountDetailsObj.containsKey("WriteOffAmount")
                                            && RetailAccountDetailsObj.get("WriteOffAmount") != null) {
                                        WriteOffAmount = cf.getJsonValue(RetailAccountDetailsObj, "WriteOffAmount");
                                    }
                                    if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth) && Integer.parseInt(WriteOffAmount) > 0) {
                                        writeOFF = "Yes";
                                    }
                                }
                            }
                        }
                    }
               }else
               {
            	   
            	    totaldaysPastDue = "0";
                    NPA = "No";
                    dpd = "0";
                    writeOFF = "No";
                    settledHistory = "No";
                    newToCredit = "Yes"; 
               }

                }
              
                String lapExists = "No";//Need to derive this data
                String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
                        + "CICNPACHECK,CICOVERDUE,WRITEOFF,"
                        + "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
                        + "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
                        + "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
                        + "values('" + ProcessInstanceId + "','" + score + "',"
                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "',"
                        + "'EF',"
                        + "'" + consolidated_emiAmnt + "',"
                        + "'" + totaldaysPastDue + "',"
                        + "'" + applicantType + "',"
                        + "'" + totalNonEMICount + "','" + settledHistory + "','"
                        + NPA + "','" + NPA + "','" + settledHistory + "',"
                        + "SYSDATE,'" + insertionOrderid + "')";
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query: " + qryCICDataUpdate1);
                ifr.saveDataInDB(qryCICDataUpdate1);
                String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
                        + "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
                        + "VALUES('" + ProcessInstanceId + "',"
                        + "'','" + mobileNo + "','VL','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
                        + "'EXT','EF','CNS','" + lapExists + "','" + score + "',"
                     
                        + "'" + PastDueAmount + "','" + newToCredit + "',SYSDATE,SYSDATE)";
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
                int queryResult = ifr.saveDataInDB(query);
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->queryResult: " + queryResult);
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->insertionOrderid: " + insertionOrderid);
                crg.crgGenEquifax( ifr,  responseBody,  ProcessInstanceId,  apiName,
                         applicantType,insertionOrderid);
                return score;
            } else {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->No response from Server..");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
            Log.errorLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.consoleLog(ifr, "Exception StackTrace:::" + errors);

        } finally {
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Capture Equifax REquest and Response Calling!");
            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Equifax_API", request, responseBody, "", "", "",
                    applicantType);
        }
        return RLOS_Constants.ERROR;
    }

    public static String removeSpecialCharacters(String inputString) {
        return inputString.replaceAll("[^A-Za-z0-9 ]", " ");
    }

    private boolean checkEMIKnockOffAccFilterStatus(IFormReference ifr, String accountType,
            String excludedAccnts, String ownerType, String excludedOwners, String open) {

        Log.consoleLog(ifr, "#checkEMIKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "ownerType===>" + ownerType);
        Log.consoleLog(ifr, "excludedOwners===>" + excludedOwners);

        String loanAccount = "SELECT LOANCODE from los_m_loan_type where LOANTYPE=upper('" + accountType + "')";
        List<List<String>> Results = cf.mExecuteQuery(ifr, loanAccount, "Execute query for fetching loanAccount ");
        if (!Results.isEmpty()) {
            accountType = Results.get(0).get(0);
            Log.consoleLog(ifr, "accountType " + accountType);
        }
        if ("Individual".equalsIgnoreCase(ownerType)) {
            ownerType = "1";
        } else if ("Authorized User".equalsIgnoreCase(ownerType)) {
            ownerType = "2";
        } else if ("Guarantor".equalsIgnoreCase(ownerType)) {
            ownerType = "3";
        } else if ("Joint Account".equalsIgnoreCase(ownerType)) {
            ownerType = "4";
        }

        String[] excludedOwnerTypes = excludedOwners.split(",");
        for (String typeOfOwner : excludedOwnerTypes) {
            if (typeOfOwner.equals(ownerType)) {
                Log.consoleLog(ifr, "excludedOwnerTypes===> " + typeOfOwner);
                return false;
            }
        }

        if ("Yes".equalsIgnoreCase(open)) {
            Log.consoleLog(ifr, "open status ===>" + open);
            String[] excludedAccounts = excludedAccnts.split(",");
            for (String accnt : excludedAccounts) {
                if (accnt.equals(accountType)) {
                    Log.consoleLog(ifr, "excludedAccounts===>" + accnt);
                    return false;
                }
            }
            return true;
        } else {
            Log.consoleLog(ifr, "open status ===>" + open);
            return false;
        }
    }

    private boolean checkKnockOffOwnershipStatus(IFormReference ifr,
            String ownerType, String excludedOwners) {

        Log.consoleLog(ifr, "#checkEMIKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "ownerType===>" + ownerType);
        Log.consoleLog(ifr, "excludedOwners===>" + excludedOwners);
        if ("Individual".equalsIgnoreCase(ownerType)) {
            ownerType = "1";
        } else if ("Authorized User".equalsIgnoreCase(ownerType)) {
            ownerType = "2";
        } else if ("Guarantor".equalsIgnoreCase(ownerType)) {
            ownerType = "3";
        } else if ("Joint Account".equalsIgnoreCase(ownerType)) {
            ownerType = "4";
        }
        Log.consoleLog(ifr, "ownerType===> " + ownerType);
        String[] excludedOwnerTypes = excludedOwners.split(",");
        for (String typeOfOwner : excludedOwnerTypes) {
            Log.consoleLog(ifr, "typeOfOwner===> " + typeOfOwner);
            if (typeOfOwner.equals(ownerType)) {
                Log.consoleLog(ifr, "excludedOwnerTypes===> " + typeOfOwner);
                return false;
            }
        }
        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;
    }
    
    public String getEquifaxCIBILScore2(IFormReference ifr, String ProcessInstanceId, String aadharNo,
            String productType, String loanAmount, String applicantType)
            throws ParseException {//Added by Vikash Mehta

        String apiName = "EQUIFAX";
        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->apiName: " + apiName);

        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productType: " + productType);
        String request = "";
        String responseBody = "";
        double PastDueAmount = 0;
        String TotalPastDue = "";
        String NoOfPastDueAccounts = "";
        String PaymentStatus = "";
        String NoOfWriteOffs = "";
        String score = "";
        String EMAILID = "";
        String insertionOrderid = "";
        int totalNonEMICount = 0;
        int consolidated_emiAmnt = 0;
        String overAllAccountTypes = "";
        String overAllAccountStatus = "";
        String overAllOpendate = "";
        String overAllSuitFiledWillfulDefaultWrittenOffStatus = "";
        String totaldaysPastDue = "";
        String NPA = "No";
        String dpd = "0";
        String writeOFF = "No";
        String settledHistory = "No";
        String newToCredit = "No";
        boolean isNTC = false;
        try {

            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
                    formattedDate = "", addressLine3 = "", city = "", state = "", pincode = "",
                    gender = "", gendercode = "", stateCode = "", mobileNo = "", panNo = "", votterId = "";

            String Query1 = "";
            String Query2 = "";
            String MobileNumber="";
            String cleanedMobileNo="";

            String intertionOrderIDArray[] = applicantType.split("~");
            Log.consoleLog(ifr, "intertionOrderIDArray:: "+intertionOrderIDArray);

            String finalIntOrderID = intertionOrderIDArray[1].trim();
            Log.consoleLog(ifr, "finalIntOrderID:: "+finalIntOrderID);

            String CASKeysQuery = "select a.mobileno, b.customerid from LOS_L_BASIC_INFO_I a join los_nl_basic_info b on a.f_key=b.f_key and b.pid='"+ProcessInstanceId+"' and b.insertionorderid='"+finalIntOrderID+"' and b.applicanttype in ('CB', 'G')";
            Log.consoleLog(ifr, "CASKeysQuery:: "+CASKeysQuery);
            List<List<String>> CASKeysQueryRes = cf.mExecuteQuery(ifr, CASKeysQuery, "Get mobile number and customer Id of CB:");
            Log.consoleLog(ifr, "CASKeysQueryRes:: "+CASKeysQueryRes);
            if (!CASKeysQueryRes.isEmpty()) {
                MobileNumber =  CASKeysQueryRes.get(0).get(0);
                Log.consoleLog(ifr, "MobileNumber CASKeysQueryRes:: "+MobileNumber);
                customerId =  CASKeysQueryRes.get(0).get(1);
                Log.consoleLog(ifr, "customerId CASKeysQueryRes:: "+customerId);
            }
            if (MobileNumber != null && MobileNumber.length() > 10) {
                cleanedMobileNo = MobileNumber.substring(MobileNumber.length() - 10);
            }
            CustomerAccountSummary cas = new CustomerAccountSummary();
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", cleanedMobileNo);
            customerdetails.put("customerId", customerId);
            Log.consoleLog(ifr, "customerdetails:: "+customerdetails);
            String CASResponse = cas.getCustomerAccountSummary(ifr, customerdetails);
            if(CASResponse.equalsIgnoreCase(RLOS_Constants.ERROR)){
                return "Fetching Customer Account Summary Unsuccessful";
            }
            Log.consoleLog(ifr, "CASResponse:: "+CASResponse);
            JSONParser jparser = new JSONParser();
            JSONObject CASObject = (JSONObject) jparser.parse(CASResponse);
            String tempFirstName = CASObject.get("CustomerFirstName").toString();
            Log.consoleLog(ifr, "tempFirstName:: "+tempFirstName);
            String tempLastName = CASObject.get("CustomerLastName").toString();
            Log.consoleLog(ifr, "tempLastName:: "+tempLastName);
            String tempPan = CASObject.get("PanNumber").toString();
            Log.consoleLog(ifr, "tempPan:: "+tempPan);
            String tempDateOfBirth = CASObject.get("DateOfBirth").toString();
            Log.consoleLog(ifr, "tempDateOfBirth:: "+tempDateOfBirth);
            String tempGender = CASObject.get("CustomerSex").toString();
            Log.consoleLog(ifr, "tempGender:: "+tempGender);
            String tempAadhar = CASObject.get("AadharNo").toString();
            Log.consoleLog(ifr, "tempAadhar:: "+tempAadhar);
            String tempEmailID = CASObject.get("emailID").toString();
            Log.consoleLog(ifr, "tempEmailID:: "+tempEmailID);

            Query2 = "select a.LINE1,a.LINE2,a.LINE3,a.CITY_TOWN_VILLAGE,a.STATE,a.PINCODE from LOS_NL_ADDRESS a join los_nl_basic_info b on a.f_key=b.f_key and b.pid='"+ProcessInstanceId+"' and b.insertionorderid='"+finalIntOrderID+"' and b.applicanttype in ('CB', 'G')  and a.ADDRESSTYPE='P'";

            String voterId = "";
            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
            //"^[A-Za-z ]+$";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result pattern: " + pattern);

            Log.consoleLog(ifr, "Customer ID: " + customerId);

            firstName = tempFirstName.replaceAll("[ .]", "");
            lastName = tempLastName.replaceAll("[ .]", "");
            Log.consoleLog(ifr, "Raw Names: " + firstName + " " + lastName);

            if (lastName.equalsIgnoreCase("{}")
                    || lastName.equalsIgnoreCase("{")
                    || lastName.equalsIgnoreCase("}")) {
                Log.consoleLog(ifr, "Cleaning invalid lastName characters");
                lastName = "";
            }
            Log.consoleLog(ifr, "Processed lastName123: " + lastName);
            Log.consoleLog(ifr, "Processed lastName: " + !Pattern.matches(pattern, firstName));

            if (firstName.length() < 2) {
                Log.consoleLog(ifr, "Validation failed - First name too short: " + firstName);
                return RLOS_Constants.ERROR + "Name is less than 1 character ";
            } else if (!Pattern.matches(pattern, firstName)) {
                Log.consoleLog(ifr, "Validation failed - Invalid characters in name: " + firstName);
                return RLOS_Constants.ERROR + "Name contains invalid special characters";
            }

            // Existing fields
            dateofBirth = tempDateOfBirth;
            gender = tempGender;
            mobileNo = cleanedMobileNo;
            EMAILID = tempEmailID;

            Log.consoleLog(ifr, "Extracted DOB: " + dateofBirth + ", Gender: " + gender + ", Mobile: " + mobileNo);

            // Handle empty email
            if (EMAILID == null || EMAILID.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "Empty email detected, setting default");
                EMAILID = "abcd@gmail.com";
            }
            Log.consoleLog(ifr, "Final Email: " + EMAILID);

            panNumber = tempPan;
            aadharNo=tempAadhar;
            voterId = "";

            // Set empty strings if not found
            if (panNumber == null || panNumber.isEmpty()) {
                Log.consoleLog(ifr, "No PAN number found in KYC records");
                panNumber = "";
            }
            if (voterId == null || voterId.isEmpty()) {
                Log.consoleLog(ifr, "No Voter ID found in KYC records");
                voterId = "";
            }

            Log.consoleLog(ifr, "Final KYC Results - PAN: " + panNumber + ", Voter ID: " + voterId);
            Log.consoleLog(ifr, "Customer data processing completed successfully");
            String inputDate = dateofBirth;
            if (gender.equalsIgnoreCase("Male")) {
                gendercode = "M";
            } else if (gender.equalsIgnoreCase("Female")) {
                gendercode = "F";
            } else {
                gendercode = "T";
            }

            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->firstName: " + firstName);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->lastName: " + lastName);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PAN: " + panNumber);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->birthDate: " + dateofBirth);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->gender: " + gender);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);


            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query2: " + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result1: " + Result1.toString());

            if (Result1.size() > 0) {
                addressLine1 = Result1.get(0).get(0);
                addressLine2 = Result1.get(0).get(1);
                addressLine3 = Result1.get(0).get(2);
                city = Result1.get(0).get(3);
                state = Result1.get(0).get(4);
                pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine1: " + addressLine1);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine2: " + addressLine2);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine3: " + addressLine3);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->City: " + city);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->State: " + state);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Pincode: " + pincode);

            String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query3: " + Query3);
            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result3: " + Result3.toString());
            if (Result3.size() > 0) {
                stateCode = Result3.get(0).get(0);
            }

            if (stateCode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->StateCode founds to be empty for " + state + " in LOS_MST_STATE table.");
                return RLOS_Constants.ERROR + "StateCode not found";
            }
            if (addressLine1.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->addressLine1 not found");
                return RLOS_Constants.ERROR + "addressLine1 not found";
            }
            if (pincode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->pincode not found");
                return RLOS_Constants.ERROR + "pincode not found";
            }

            try {
                if (!dateofBirth.equalsIgnoreCase("")) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
                    formattedDate = date.format(outputFormatter);
                }
            } catch (Exception e) {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception in date formatting===>" + e);
            }

            String DOB = formattedDate;

            String productCode = "VL";
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productCode: " + productCode);

            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "EQUIFAXCONF", "EMIACCTTYPE");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);

            String excludedOwners = pcm.getParamConfig2(ifr, productCode, "EQUIFAXCONF", "OWNERTYPE");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwners: " + excludedOwners);

            String excludedOwnersGI = pcm.getParamConfig2(ifr, productCode,"EQUIFAXCONF", "OWNERTYPEGI");
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwnersGI: " + excludedOwnersGI);
           
            String InquiryPurpose = "";
//
            
            InquiryPurpose = pcm.getConstantValue(ifr, "EQUIFAXAPI", "Housing Loan");
            if(InquiryPurpose.isEmpty()){
                 return RLOS_Constants.ERROR;
            }
            if (panNumber == null || panNumber.equals("")) {
                panNumber = "";
                Log.consoleLog(ifr, "inside if Valid PAN for processing: " + panNumber);
            } else if (panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {

                panNumber = panNumber;
                Log.consoleLog(ifr, "Valid PAN for processing: " + panNumber);
            } else {
                Log.consoleLog(ifr, "Valid PAN for processing: " + "PAN number Not Listed ");
                return RLOS_Constants.ERROR;
            }

            String eqcustomerId = pcm.getConstantValue(ifr, "EQUIFAXAPI", "CustomerId");
            String userId = pcm.getConstantValue(ifr, "EQUIFAXAPI", "UserId");
            String password = pcm.getConstantValue(ifr, "EQUIFAXAPI", "Password");
            String memberNumber = pcm.getConstantValue(ifr, "EQUIFAXAPI", "MemberNumber");
            String securityCode = pcm.getConstantValue(ifr, "EQUIFAXAPI", "SecurityCode");
            String custRefField = pcm.getConstantValue(ifr, "EQUIFAXAPI", "CustRefField");
            String productVersion = pcm.getConstantValue(ifr, "EQUIFAXAPI", "ProductVersion");
            String eqproductCode = pcm.getConstantValue(ifr, "EQUIFAXAPI", "ProductCode");

            //InquiryPurpose=pcm.getConstantValue(ifr, "EQUIFAXAPI", InquiryPurpose);
            request = "{\n"
                    + "    \"RequestHeader\": {\n"
                    + "        \"CustomerId\": \"" + eqcustomerId + "\",\n"
                    + "        \"UserId\": \"" + userId + "\",\n"
                    + "        \"Password\": \"" + password + "\",\n"
                    + "        \"MemberNumber\": \"" + memberNumber + "\",\n"
                    + "        \"SecurityCode\": \"" + securityCode + "\",\n"
                    + "        \"CustRefField\": \"" + custRefField + "\",\n"
                    + "        \"ProductVersion\": \"" + productVersion + "\",\n"
                    + "        \"ProductCode\": [\n"
                    + "            \"" + eqproductCode + "\"\n"
                    + "        ]\n"
                    + "    },\n"
                    + "    \"RequestBody\": {\n"
                    + "        \"CustomFields\": [\n"
                    + "            {\n"
                    + "                \"key\": \"EmbeddedPdf\",\n"
                    + "                \"value\": \"Y\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"InquiryPurpose\": \"" + InquiryPurpose + "\",\n"
                    + "        \"TransactionAmount\": \"" + loanAmount + "\",\n"
                    + "        \"FirstName\": \"" + firstName + "\",\n"
                    + "        \"MiddleName\": \"\",\n"
                    + "        \"LastName\": \"" + lastName + "\",\n"
                    + "        \"InquiryAddresses\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"AddressLine1\": \"" + addressLine1 + "\",\n"
                    + "                \"City\": \"" + city + "\",\n"
                    + "                \"State\": \"" + stateCode + "\",\n"
                    + "                \"AddressType\": [\n"
                    + "                    \"H\"\n"
                    + "                ],\n"
                    + "                \"Postal\": \"" + pincode + "\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"InquiryPhones\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"Number\": \"" + mobileNo + "\",\n"
                    + "                \"PhoneType\": [\n"
                    + "                    \"M\"\n"
                    + "                ]\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"EmailAddresses\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"Email\": \"" + EMAILID + "\",\n"
                    + "                \"EmailType\": [\n"
                    + "                    \"P\"\n"
                    + "                ]\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"IDDetails\": [\n"
                    + "            {\n"
                    + "                \"seq\": \"1\",\n"
                    + "                \"IDValue\": \"" + aadharNo + "\",\n"
                    + "                \"IDType\": \"M\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"2\",\n"
                    + "                \"IDValue\": \"" + panNumber + "\",\n"
                    + "                \"IDType\": \"T\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"3\",\n"
                    + "                \"IDValue\": \"" + voterId + "\",\n"
                    + "                \"IDType\": \"V\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"4\",\n"
                    + "                \"IDValue\": \"\",\n"
                    + "                \"IDType\": \"D\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "                \"seq\": \"5\",\n"
                    + "                \"IDValue\": \"\",\n"
                    + "                \"IDType\": \"M\",\n"
                    + "                \"Source\": \"Inquiry\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"DOB\": \"" + DOB + "\",\n"
                    + "        \"Gender\": \"" + gendercode + "\"\n"
                    + "    },\n"
                    + "    \"Score\": [\n"
                    + "        {\n"
                    + "            \"Type\": \"ERS\",\n"
                    + "            \"Version\": \"4.5\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->***EQUIFAX API request==>" + request);
            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->EQUIFAX API RESPONSE==>" + responseBody);

            if (!responseBody.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
                JSONObject resultObj = new JSONObject(OutputJSON);
                Log.consoleLog(ifr, "resultObj==>" + resultObj);
                String stringBody = resultObj.get("body").toString();

                Log.consoleLog(ifr, "body==>" + stringBody);
                JSONObject body = (JSONObject) OutputJSON.get("body");

                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->body: " + body.toString());
                JSONObject CCRResponse = (JSONObject) body.get("CCRResponse");
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CCRResponse: " + CCRResponse.toString());
                JSONArray CIRReportDataLst = (JSONArray) CCRResponse.get("CIRReportDataLst");
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.toString());
                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.size());

//                String cicReportGenReq = pcm.getConstantValue(ifr, "HIGHMARK", "CICREPORTGENREQ");
//                if (cicReportGenReq.equalsIgnoreCase("Y")) {
//                    String encodedB64 = body.get("EncodedPdf").toString();
//                    String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "EQUIFAX", encodedB64, "NGREPORTTOOL_EQUIFAX");
//                    Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
//                    cm.updateCICReportStatus(ifr, "EQUIFAX", generateReportStatus, applicantType);
//                }

                if (CIRReportDataLst.size() > 0) {
                    JSONObject firstReportData = (JSONObject) CIRReportDataLst.get(0);

                    if (firstReportData.containsKey("Error")) {
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->firstReportData: " + firstReportData);
                        JSONObject errorObj = (JSONObject) firstReportData.get("Error");
                        String errorCode = errorObj.get("ErrorCode").toString();
                        String errorDesc = errorObj.get("ErrorDesc").toString();

                        Log.consoleLog(ifr, "EquifaxAPI: ErrorCode - " + errorCode);
                        Log.consoleLog(ifr, "EquifaxAPI: ErrorDesc - " + errorDesc);

                        if (errorDesc.equalsIgnoreCase("Consumer not found in bureau")) {
                            score = "0";
                            isNTC = true;
                        }
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorCode: " + errorCode);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorDesc: " + errorDesc);
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->score: " + score);
                    }

                }

                for (int CIRReportDataLstCount = 0; CIRReportDataLstCount < CIRReportDataLst
                        .size(); CIRReportDataLstCount++) {
                    JSONObject CIRReportDataVal = (JSONObject) CIRReportDataLst.get(CIRReportDataLstCount);
                    if (CIRReportDataVal.containsKey("CIRReportData")) {
                        JSONObject CIRReportData = (JSONObject) CIRReportDataVal.get("CIRReportData");
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->CIRReportData: " + CIRReportData.toString());

                        JSONArray ScoreDetails = (JSONArray) CIRReportData.get("ScoreDetails");
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->ScoreDetails: " + ScoreDetails.toString());

                        for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
                            JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);

                            if (ScoreDetailsObj.containsKey("Value") && ScoreDetailsObj.get("Value") != null) {
                                score = ScoreDetailsObj.get("Value").toString();

                                if ("-1".equalsIgnoreCase(score)) {
                                    isNTC = true;
                                    score = "0";
                                }
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Value: " + score);
                            }
                        }
                        JSONObject RetailAccountsSummary = (JSONObject) CIRReportData.get("RetailAccountsSummary");
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountsSummary: " + RetailAccountsSummary.toString());

                        if (RetailAccountsSummary.containsKey("NoOfWriteOffs")
                                && RetailAccountsSummary.get("NoOfWriteOffs") != null) {
                            NoOfWriteOffs = RetailAccountsSummary.get("NoOfWriteOffs").toString();
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfWriteOffs: " + NoOfWriteOffs.toString());
                        }
                        if (RetailAccountsSummary.containsKey("TotalPastDue")
                                && RetailAccountsSummary.get("TotalPastDue") != null) {
                            TotalPastDue = RetailAccountsSummary.get("TotalPastDue").toString();
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->TotalPastDue: " + TotalPastDue.toString());
                        }

                        if (RetailAccountsSummary.containsKey("NoOfPastDueAccounts")
                                && RetailAccountsSummary.get("NoOfPastDueAccounts") != null) {
                            NoOfPastDueAccounts = RetailAccountsSummary.get("NoOfPastDueAccounts").toString();
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfPastDueAccounts: " + NoOfPastDueAccounts.toString());
                        }
                        JSONArray RetailAccountDetails = (JSONArray) CIRReportData.get("RetailAccountDetails");
                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountDetails: " + RetailAccountDetails.toString());

                        for (int RetailAccountDetailsCount = 0; RetailAccountDetailsCount < RetailAccountDetails
                                .size(); RetailAccountDetailsCount++) {

                            if (!overAllAccountTypes.equalsIgnoreCase("")) {
                                overAllAccountTypes = overAllAccountTypes + ",";
                            }

                            if (!overAllAccountStatus.equalsIgnoreCase("")) {
                                overAllAccountStatus = overAllAccountStatus + ",";
                            }
                            if (!overAllOpendate.equalsIgnoreCase("")) {
                                overAllOpendate = overAllOpendate + ",";
                            }
                            if (!overAllSuitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("")) {
                                overAllSuitFiledWillfulDefaultWrittenOffStatus = overAllSuitFiledWillfulDefaultWrittenOffStatus + ",";
                            }

                            JSONObject RetailAccountDetailsObj = (JSONObject) RetailAccountDetails
                                    .get(RetailAccountDetailsCount);

                            String accountType = cf.getJsonValue(RetailAccountDetailsObj, "AccountType");
                            String accountStatus = cf.getJsonValue(RetailAccountDetailsObj, "AccountStatus");
                            String openDate = cf.getJsonValue(RetailAccountDetailsObj, "DateOpened");

                            String OwnershipType = cf.getJsonValue(RetailAccountDetailsObj, "OwnershipType");
                            String Open = cf.getJsonValue(RetailAccountDetailsObj, "Open");
                            String InstallmentAmount = cf.getJsonValue(RetailAccountDetailsObj, "InstallmentAmount");
                            overAllAccountTypes = overAllAccountTypes + accountType;
                            overAllAccountStatus = overAllAccountStatus + accountStatus;
                            overAllOpendate = overAllOpendate + openDate;
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->overAllOpendate: " + overAllOpendate);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->accountType: " + accountType);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->openDate: " + openDate);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->OwnershipType: " + OwnershipType);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Open: " + Open);

                            if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                for (int j = 0; j < History48Months.size(); j++) {
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->expDate");
                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);

                                    String daysPastDue = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PaymentStatus: " + PaymentStatus);
                                    String key = cf.getJsonValue(History48MonthsObj, "key");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                    YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                    YearMonth currentYearMonth = YearMonth.now();
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                    YearMonth twelveMonthsAgo = currentYearMonth.minusMonths(12);
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->twelveMonthsAgo: " + twelveMonthsAgo);
                                    // Check if the input date is within the last 12 months
                                    if (!inputYearMonth.isBefore(twelveMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 12 months: ");
                                        totaldaysPastDue = totaldaysPastDue + daysPastDue;
                                    }
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->totaldaysPastDue: " + totaldaysPastDue);

                                }

                            }

                            boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
                                    excludedEMIAccnts, OwnershipType, excludedOwners, Open);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->emiStatus: " + emiStatus);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
                            if (emiStatus) {
                                if (!InstallmentAmount.isEmpty()) {
                                    int totalEMI = Integer.parseInt(InstallmentAmount);
                                    if (totalEMI < 0) {
                                        totalNonEMICount++;
                                    }
                                    consolidated_emiAmnt += totalEMI;
                                }
                            }
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);
                            boolean npaStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->npaStatus: " + npaStatus);

                            if (npaStatus) {
                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                    JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + History48Months);
                                    for (int j = 0; j < History48Months.size(); j++) {
                                        JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                        String Asset_Classification = cf.getJsonValue(History48MonthsObj, "AssetClassificationStatus");
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Asset_Classification: " + Asset_Classification);
                                        String key = cf.getJsonValue(History48MonthsObj, "key");
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                        YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                        YearMonth currentYearMonth = YearMonth.now();
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                        YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                        // Check if the input date is within the last 60 months
                                        if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
                                            if ((Asset_Classification.equalsIgnoreCase("SUB")
                                                    || Asset_Classification.equalsIgnoreCase("DBT")
                                                    || Asset_Classification.equalsIgnoreCase("LOS"))) {
                                                NPA = "Yes";
                                            }
                                        }
                                    }
                                }
                            }

                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NPA: " + NPA);
                            boolean setteledHistoryStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->setteledHistoryStatus: " + setteledHistoryStatus);

                            if (setteledHistoryStatus) {
                                if ("Settled".equalsIgnoreCase(accountStatus) || "Closed Account".equalsIgnoreCase(accountStatus)) {
                                    if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                        JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                        for (int j = 0; j < History48Months.size(); j++) {
                                            JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                            String paymentStatus = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->paymentStatus: " + paymentStatus);
                                            String key = cf.getJsonValue(History48MonthsObj, "key");
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                            YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                            YearMonth currentYearMonth = YearMonth.now();
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                            YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                            // Check if the input date is within the last 60 months
                                            if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
                                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
                                                if (paymentStatus.equalsIgnoreCase("SET")) {
                                                    settledHistory = "Yes";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->settledHistory: " + settledHistory);

                            boolean wroStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->wroStatus: " + wroStatus);

                            if (wroStatus) {
                                if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
                                    JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
                                    for (int j = 0; j < History48Months.size(); j++) {
                                        JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
                                        String key = cf.getJsonValue(History48MonthsObj, "key");
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
                                        YearMonth inputYearMonth = YearMonth.parse(key, formatter);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
                                        YearMonth currentYearMonth = YearMonth.now();
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
                                        YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
                                        String WriteOffAmount = "0";
                                        if (RetailAccountDetailsObj.containsKey("WriteOffAmount")
                                                && RetailAccountDetailsObj.get("WriteOffAmount") != null) {
                                            WriteOffAmount = cf.getJsonValue(RetailAccountDetailsObj, "WriteOffAmount");
                                        }
                                        if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth) && Integer.parseInt(WriteOffAmount) > 0) {
                                            writeOFF = "Yes";
                                        }
                                    }
                                }
                            }
                        }
                    } else {

                        totaldaysPastDue = "0";
                        NPA = "No";
                        dpd = "0";
                        writeOFF = "No";
                        settledHistory = "No";
                        newToCredit = "Yes";
                    }

                }

                String lapExists = "No";//Need to derive this data
                String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
                        + "CICNPACHECK,CICOVERDUE,WRITEOFF,"
                        + "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
                        + "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
                        + "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
                        + "values('" + ProcessInstanceId + "','" + score + "',"
                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "',"
                        + "'EF',"
                        + "'" + consolidated_emiAmnt + "',"
                        + "'" + totaldaysPastDue + "',"
                        + "'" + applicantType + "',"
                        + "'" + totalNonEMICount + "','" + settledHistory + "','"
                        + NPA + "','" + NPA + "','" + settledHistory + "',"
                        + "SYSDATE,'" + insertionOrderid + "')";
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query: " + qryCICDataUpdate1);
                ifr.saveDataInDB(qryCICDataUpdate1);
//                String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
//                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
//                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
//                        + "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
//                        + "VALUES('" + ProcessInstanceId + "',"
//                        + "'','" + mobileNo + "','"+productCode+"','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
//                        + "'EXT','EF','CNS','" + lapExists + "','" + score + "',"
//                        + "'" + PastDueAmount + "','" + newToCredit + "',SYSDATE,SYSDATE)";
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
//                int queryResult = ifr.saveDataInDB(query);
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->queryResult: " + queryResult);
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->insertionOrderid: " + insertionOrderid);
//                crg.crgGenEquifax(ifr, responseBody, ProcessInstanceId, apiName,
//                        applicantType, insertionOrderid, isNTC);
                return score;
            } else {
                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->No response from Server..");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
            Log.errorLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.consoleLog(ifr, "Exception StackTrace:::" + errors);

        } finally {
            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Capture Equifax REquest and Response Calling!");
            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Equifax_API", request, responseBody, "", "", "",
                    applicantType);
        }
        return RLOS_Constants.ERROR;
    }

//    public String getEquifaxCIBILScore2(IFormReference ifr, String ProcessInstanceId, String aadharNo,
//            String productType, String loanAmount, String applicantType)
//            throws ParseException {
//
//        String apiName = "EQUIFAX";
//        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->apiName: " + apiName);
//
//        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productType: " + productType);
//        String request = "";
//        String responseBody = "";
//        double PastDueAmount = 0;
//        String TotalPastDue = "";
//        String NoOfPastDueAccounts = "";
//        String PaymentStatus = "";
//        String NoOfWriteOffs = "";
//        String score = "";
//        String EMAILID = "";
//        String insertionOrderid = "";
//        int totalNonEMICount = 0;
//        int consolidated_emiAmnt = 0;
//        String overAllAccountTypes = "";
//        String overAllAccountStatus = "";
//        String overAllOpendate = "";
//        String overAllSuitFiledWillfulDefaultWrittenOffStatus = "";
//        String totaldaysPastDue = "";
//        String NPA = "No";
//        String dpd = "0";
//        String writeOFF = "No";
//        String settledHistory = "No";
//        String newToCredit = "No";
//        boolean isNTC = false;
//        try {
//
//            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
//                    formattedDate = "", addressLine3 = "", city = "", state = "", pincode = "",
//                    gender = "", gendercode = "", stateCode = "", mobileNo = "", panNo = "", votterId = "";
//
//            String Query1 = "";
//            String Query2 = "";
//
//            Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER,EMAILID "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            panNumber = "";
//            String voterId = "";
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxScore->Query1: " + Query1);
//            List< List< String>> Result = ifr.getDataFromDB(Query1);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result: " + Result.toString());
//            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
//            //"^[A-Za-z ]+$";
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result pattern: " + pattern);
//            if (Result.size() > 0) {
//                Log.consoleLog(ifr, "Processing customer data for PID: LOS-00000000000012081");
//
//                // Basic customer information
//                customerId = Result.get(0).get(0);
//                Log.consoleLog(ifr, "Customer ID: " + customerId);
//
//                firstName = Result.get(0).get(1).replaceAll("[ .]", "");
//                lastName = Result.get(0).get(2).replaceAll("[ .]", "");
//                Log.consoleLog(ifr, "Raw Name: " + Result.get(0).get(1) + " " + Result.get(0).get(2));
//                Log.consoleLog(ifr, "Processed Name: " + firstName + " " + lastName);
//
//                if (lastName.equalsIgnoreCase("{}")
//                        || lastName.equalsIgnoreCase("{")
//                        || lastName.equalsIgnoreCase("}")) {
//                    Log.consoleLog(ifr, "Cleaning invalid lastName characters");
//                    lastName = "";
//                }
//                Log.consoleLog(ifr, "Processed lastName123: " + lastName);
//                Log.consoleLog(ifr, "Processed lastName: " + !Pattern.matches(pattern, firstName));
//
//                if (firstName.length() < 2) {
//                    Log.consoleLog(ifr, "Validation failed - First name too short: " + firstName);
//                    return RLOS_Constants.ERROR + "Name is less than 1 character ";
//                } else if (!Pattern.matches(pattern, firstName)) {
//                    Log.consoleLog(ifr, "Validation failed - Invalid characters in name: " + firstName);
//                    return RLOS_Constants.ERROR + "Name contains invalid special characters";
//                }
//
//                // Existing fields
//                dateofBirth = Result.get(0).get(4);
//                gender = Result.get(0).get(5);
//                mobileNo = Result.get(0).get(6);
//                EMAILID = Result.get(0).get(7);
//
//                Log.consoleLog(ifr, "Extracted DOB: " + dateofBirth + ", Gender: " + gender + ", Mobile: " + mobileNo);
//
//                // Handle empty email
//                if (EMAILID == null || EMAILID.equalsIgnoreCase("")) {
//                    Log.consoleLog(ifr, "Empty email detected, setting default");
//                    EMAILID = "abcd@gmail.com";
//                }
//                Log.consoleLog(ifr, "Final Email: " + EMAILID);
//
//                // Initialize both ID types
//                panNumber = "";
//                voterId = "";
//                Log.consoleLog(ifr, "Starting KYC processing for " + Result.size() + " records");
//
//                // Process all KYC records
//                for (int i = 0; i < Result.size(); i++) {
//                    List<String> row = Result.get(i);
//                    String kycType = row.get(3);
//                    String kycValue = row.get(4);
//                    Log.consoleLog(ifr, "Processing row " + i + " - KYC Type: " + kycType + ", Value: " + kycValue);
//
//                    if (kycValue != null && !kycValue.isEmpty()) {
//                        if ("TAXID".equalsIgnoreCase(kycType)) {
//                            panNumber = kycValue.trim();
//                            Log.consoleLog(ifr, "Found PAN Number: " + panNumber);
//                        } else if ("VID".equalsIgnoreCase(kycType)) {
//                            voterId = kycValue.trim();
//                            Log.consoleLog(ifr, "Found Voter ID: " + voterId);
//                        } else if ("AA".equalsIgnoreCase(kycType)) {
//                            aadharNo = kycValue.trim();
//                            Log.consoleLog(ifr, "Found aadharNo Ref No: " + aadharNo);
//                            AadharVault objAV = new AadharVault();
//                            aadharNo = objAV.getDataByReferenceKey(ifr, aadharNo);
//                        } else if (panNumber.isEmpty() && kycValue.matches("[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}")) {
//                            panNumber = kycValue.trim();
//                            Log.consoleLog(ifr, "Auto-detected PAN format: " + panNumber);
//                        }
//                    } else {
//                        Log.consoleLog(ifr, "Empty KYC value in row " + i);
//                    }
//                }
//
//                // Set empty strings if not found
//                if (panNumber == null || panNumber.isEmpty()) {
//                    Log.consoleLog(ifr, "No PAN number found in KYC records");
//                    panNumber = "";
//                }
//                if (voterId == null || voterId.isEmpty()) {
//                    Log.consoleLog(ifr, "No Voter ID found in KYC records");
//                    voterId = "";
//                }
//
//                Log.consoleLog(ifr, "Final KYC Results - PAN: " + panNumber + ", Voter ID: " + voterId);
//                Log.consoleLog(ifr, "Customer data processing completed successfully");
//            } else {
//                Log.consoleLog(ifr, "No records found for the given query parameters");
//            }
//
//            String inputDate = dateofBirth;
//            if (gender.equalsIgnoreCase("Male")) {
//                gendercode = "M";
//            } else if (gender.equalsIgnoreCase("Female")) {
//                gendercode = "F";
//            } else {
//                gendercode = "T";
//            }
//
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->firstName: " + firstName);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->lastName: " + lastName);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PAN: " + panNumber);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->birthDate: " + dateofBirth);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->gender: " + gender);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);
//
//            if (mobileNo.startsWith("91")) {
//                mobileNo = mobileNo.substring(2); // Remove 91
//                if (mobileNo.length() == 12 && mobileNo.startsWith("91")) {
//                    mobileNo = mobileNo.substring(2);
//                }
//            }
//            if (mobileNo.length() > 10) {
//                mobileNo = mobileNo.substring(mobileNo.length() - 10);
//            }
//
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->mobileNo: " + mobileNo);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query2: " + Query2);
//            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result1: " + Result1.toString());
//
//            if (Result1.size() > 0) {
//                addressLine1 = Result1.get(0).get(0);
//                addressLine2 = Result1.get(0).get(1);
//                addressLine3 = Result1.get(0).get(2);
//                city = Result1.get(0).get(3);
//                state = Result1.get(0).get(4);
//                pincode = Result1.get(0).get(5);
//            }
//
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine1: " + addressLine1);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine2: " + addressLine2);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->AddressLine3: " + addressLine3);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->City: " + city);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->State: " + state);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Pincode: " + pincode);
//
//            String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
//                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Query3: " + Query3);
//            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Result3: " + Result3.toString());
//            if (Result3.size() > 0) {
//                stateCode = Result3.get(0).get(0);
//            }
//
//            if (stateCode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->StateCode founds to be empty for " + state + " in LOS_MST_STATE table.");
//                return RLOS_Constants.ERROR + "StateCode not found";
//            }
//            if (addressLine1.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->addressLine1 not found");
//                return RLOS_Constants.ERROR + "addressLine1 not found";
//            }
//            if (pincode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->pincode not found");
//                return RLOS_Constants.ERROR + "pincode not found";
//            }
//
//            try {
//                if (!dateofBirth.equalsIgnoreCase("")) {
//                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//                    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
//                    formattedDate = date.format(outputFormatter);
//                }
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception in date formatting===>" + e);
//            }
//
//            String DOB = formattedDate;
//
//            String productCode = "VL";
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->productCode: " + productCode);
//
//            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "EQUIFAXCONF", "EMIACCTTYPE");
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);
//
//            String excludedOwners = pcm.getParamConfig2(ifr, productCode, "EQUIFAXCONF", "OWNERTYPE");
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwners: " + excludedOwners);
//
//            String excludedOwnersGI = pcm.getParamConfig2(ifr, productCode,"EQUIFAXCONF", "OWNERTYPEGI");
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->excludedOwnersGI: " + excludedOwnersGI);
//           
//            String InquiryPurpose = "";
////
//            
//            InquiryPurpose = pcm.getConstantValue(ifr, "EQUIFAXAPI", "Housing Loan");
//            if(InquiryPurpose.isEmpty()){
//                 return RLOS_Constants.ERROR;
//            }
//            if (panNumber == null || panNumber.equals("")) {
//                panNumber = "";
//                Log.consoleLog(ifr, "inside if Valid PAN for processing: " + panNumber);
//            } else if (panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {
//
//                panNumber = panNumber;
//                Log.consoleLog(ifr, "Valid PAN for processing: " + panNumber);
//            } else {
//                Log.consoleLog(ifr, "Valid PAN for processing: " + "PAN number Not Listed ");
//                return RLOS_Constants.ERROR;
//            }
//
//            String eqcustomerId = pcm.getConstantValue(ifr, "EQUIFAXAPI", "CustomerId");
//            String userId = pcm.getConstantValue(ifr, "EQUIFAXAPI", "UserId");
//            String password = pcm.getConstantValue(ifr, "EQUIFAXAPI", "Password");
//            String memberNumber = pcm.getConstantValue(ifr, "EQUIFAXAPI", "MemberNumber");
//            String securityCode = pcm.getConstantValue(ifr, "EQUIFAXAPI", "SecurityCode");
//            String custRefField = pcm.getConstantValue(ifr, "EQUIFAXAPI", "CustRefField");
//            String productVersion = pcm.getConstantValue(ifr, "EQUIFAXAPI", "ProductVersion");
//            String eqproductCode = pcm.getConstantValue(ifr, "EQUIFAXAPI", "ProductCode");
//
//            //InquiryPurpose=pcm.getConstantValue(ifr, "EQUIFAXAPI", InquiryPurpose);
//            request = "{\n"
//                    + "    \"RequestHeader\": {\n"
//                    + "        \"CustomerId\": \"" + eqcustomerId + "\",\n"
//                    + "        \"UserId\": \"" + userId + "\",\n"
//                    + "        \"Password\": \"" + password + "\",\n"
//                    + "        \"MemberNumber\": \"" + memberNumber + "\",\n"
//                    + "        \"SecurityCode\": \"" + securityCode + "\",\n"
//                    + "        \"CustRefField\": \"" + custRefField + "\",\n"
//                    + "        \"ProductVersion\": \"" + productVersion + "\",\n"
//                    + "        \"ProductCode\": [\n"
//                    + "            \"" + eqproductCode + "\"\n"
//                    + "        ]\n"
//                    + "    },\n"
//                    + "    \"RequestBody\": {\n"
//                    + "        \"CustomFields\": [\n"
//                    + "            {\n"
//                    + "                \"key\": \"EmbeddedPdf\",\n"
//                    + "                \"value\": \"Y\"\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"InquiryPurpose\": \"" + InquiryPurpose + "\",\n"
//                    + "        \"TransactionAmount\": \"" + loanAmount + "\",\n"
//                    + "        \"FirstName\": \"" + firstName + "\",\n"
//                    + "        \"MiddleName\": \"\",\n"
//                    + "        \"LastName\": \"" + lastName + "\",\n"
//                    + "        \"InquiryAddresses\": [\n"
//                    + "            {\n"
//                    + "                \"seq\": \"1\",\n"
//                    + "                \"AddressLine1\": \"" + addressLine1 + "\",\n"
//                    + "                \"City\": \"" + city + "\",\n"
//                    + "                \"State\": \"" + stateCode + "\",\n"
//                    + "                \"AddressType\": [\n"
//                    + "                    \"H\"\n"
//                    + "                ],\n"
//                    + "                \"Postal\": \"" + pincode + "\"\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"InquiryPhones\": [\n"
//                    + "            {\n"
//                    + "                \"seq\": \"1\",\n"
//                    + "                \"Number\": \"" + mobileNo + "\",\n"
//                    + "                \"PhoneType\": [\n"
//                    + "                    \"M\"\n"
//                    + "                ]\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"EmailAddresses\": [\n"
//                    + "            {\n"
//                    + "                \"seq\": \"1\",\n"
//                    + "                \"Email\": \"" + EMAILID + "\",\n"
//                    + "                \"EmailType\": [\n"
//                    + "                    \"P\"\n"
//                    + "                ]\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"IDDetails\": [\n"
//                    + "            {\n"
//                    + "                \"seq\": \"1\",\n"
//                    + "                \"IDValue\": \"" + aadharNo + "\",\n"
//                    + "                \"IDType\": \"M\",\n"
//                    + "                \"Source\": \"Inquiry\"\n"
//                    + "            },\n"
//                    + "            {\n"
//                    + "                \"seq\": \"2\",\n"
//                    + "                \"IDValue\": \"" + panNumber + "\",\n"
//                    + "                \"IDType\": \"T\",\n"
//                    + "                \"Source\": \"Inquiry\"\n"
//                    + "            },\n"
//                    + "            {\n"
//                    + "                \"seq\": \"3\",\n"
//                    + "                \"IDValue\": \"" + voterId + "\",\n"
//                    + "                \"IDType\": \"V\",\n"
//                    + "                \"Source\": \"Inquiry\"\n"
//                    + "            },\n"
//                    + "            {\n"
//                    + "                \"seq\": \"4\",\n"
//                    + "                \"IDValue\": \"\",\n"
//                    + "                \"IDType\": \"D\",\n"
//                    + "                \"Source\": \"Inquiry\"\n"
//                    + "            },\n"
//                    + "            {\n"
//                    + "                \"seq\": \"5\",\n"
//                    + "                \"IDValue\": \"\",\n"
//                    + "                \"IDType\": \"M\",\n"
//                    + "                \"Source\": \"Inquiry\"\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"DOB\": \"" + DOB + "\",\n"
//                    + "        \"Gender\": \"" + gendercode + "\"\n"
//                    + "    },\n"
//                    + "    \"Score\": [\n"
//                    + "        {\n"
//                    + "            \"Type\": \"ERS\",\n"
//                    + "            \"Version\": \"4.5\"\n"
//                    + "        }\n"
//                    + "    ]\n"
//                    + "}";
//
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->***EQUIFAX API request==>" + request);
//            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifax->EQUIFAX API RESPONSE==>" + responseBody);
//
//            if (!responseBody.equalsIgnoreCase("{}")) {
//                JSONParser parser = new JSONParser();
//                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
//                JSONObject resultObj = new JSONObject(OutputJSON);
//                Log.consoleLog(ifr, "resultObj==>" + resultObj);
//                String stringBody = resultObj.get("body").toString();
//
//                Log.consoleLog(ifr, "body==>" + stringBody);
//                JSONObject body = (JSONObject) OutputJSON.get("body");
//
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->body: " + body.toString());
//                JSONObject CCRResponse = (JSONObject) body.get("CCRResponse");
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CCRResponse: " + CCRResponse.toString());
//                JSONArray CIRReportDataLst = (JSONArray) CCRResponse.get("CIRReportDataLst");
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.toString());
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifax->CIRReportDataLst: " + CIRReportDataLst.size());
//
////                String cicReportGenReq = pcm.getConstantValue(ifr, "HIGHMARK", "CICREPORTGENREQ");
////                if (cicReportGenReq.equalsIgnoreCase("Y")) {
////                    String encodedB64 = body.get("EncodedPdf").toString();
////                    String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "EQUIFAX", encodedB64, "NGREPORTTOOL_EQUIFAX");
////                    Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
////                    cm.updateCICReportStatus(ifr, "EQUIFAX", generateReportStatus, applicantType);
////                }
//
//                if (CIRReportDataLst.size() > 0) {
//                    JSONObject firstReportData = (JSONObject) CIRReportDataLst.get(0);
//
//                    if (firstReportData.containsKey("Error")) {
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->firstReportData: " + firstReportData);
//                        JSONObject errorObj = (JSONObject) firstReportData.get("Error");
//                        String errorCode = errorObj.get("ErrorCode").toString();
//                        String errorDesc = errorObj.get("ErrorDesc").toString();
//
//                        Log.consoleLog(ifr, "EquifaxAPI: ErrorCode - " + errorCode);
//                        Log.consoleLog(ifr, "EquifaxAPI: ErrorDesc - " + errorDesc);
//
//                        if (errorDesc.equalsIgnoreCase("Consumer not found in bureau")) {
//                            score = "0";
//                            isNTC = true;
//                        }
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorCode: " + errorCode);
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->errorDesc: " + errorDesc);
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifax->score: " + score);
//                    }
//
//                }
//
//                for (int CIRReportDataLstCount = 0; CIRReportDataLstCount < CIRReportDataLst
//                        .size(); CIRReportDataLstCount++) {
//                    JSONObject CIRReportDataVal = (JSONObject) CIRReportDataLst.get(CIRReportDataLstCount);
//                    if (CIRReportDataVal.containsKey("CIRReportData")) {
//                        JSONObject CIRReportData = (JSONObject) CIRReportDataVal.get("CIRReportData");
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->CIRReportData: " + CIRReportData.toString());
//
//                        JSONArray ScoreDetails = (JSONArray) CIRReportData.get("ScoreDetails");
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->ScoreDetails: " + ScoreDetails.toString());
//
//                        for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
//                            JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);
//
//                            if (ScoreDetailsObj.containsKey("Value") && ScoreDetailsObj.get("Value") != null) {
//                                score = ScoreDetailsObj.get("Value").toString();
//
//                                if ("-1".equalsIgnoreCase(score)) {
//                                    isNTC = true;
//                                    score = "0";
//                                }
//                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Value: " + score);
//                            }
//                        }
//                        JSONObject RetailAccountsSummary = (JSONObject) CIRReportData.get("RetailAccountsSummary");
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountsSummary: " + RetailAccountsSummary.toString());
//
//                        if (RetailAccountsSummary.containsKey("NoOfWriteOffs")
//                                && RetailAccountsSummary.get("NoOfWriteOffs") != null) {
//                            NoOfWriteOffs = RetailAccountsSummary.get("NoOfWriteOffs").toString();
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfWriteOffs: " + NoOfWriteOffs.toString());
//                        }
//                        if (RetailAccountsSummary.containsKey("TotalPastDue")
//                                && RetailAccountsSummary.get("TotalPastDue") != null) {
//                            TotalPastDue = RetailAccountsSummary.get("TotalPastDue").toString();
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->TotalPastDue: " + TotalPastDue.toString());
//                        }
//
//                        if (RetailAccountsSummary.containsKey("NoOfPastDueAccounts")
//                                && RetailAccountsSummary.get("NoOfPastDueAccounts") != null) {
//                            NoOfPastDueAccounts = RetailAccountsSummary.get("NoOfPastDueAccounts").toString();
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NoOfPastDueAccounts: " + NoOfPastDueAccounts.toString());
//                        }
//                        JSONArray RetailAccountDetails = (JSONArray) CIRReportData.get("RetailAccountDetails");
//                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->RetailAccountDetails: " + RetailAccountDetails.toString());
//
//                        for (int RetailAccountDetailsCount = 0; RetailAccountDetailsCount < RetailAccountDetails
//                                .size(); RetailAccountDetailsCount++) {
//
//                            if (!overAllAccountTypes.equalsIgnoreCase("")) {
//                                overAllAccountTypes = overAllAccountTypes + ",";
//                            }
//
//                            if (!overAllAccountStatus.equalsIgnoreCase("")) {
//                                overAllAccountStatus = overAllAccountStatus + ",";
//                            }
//                            if (!overAllOpendate.equalsIgnoreCase("")) {
//                                overAllOpendate = overAllOpendate + ",";
//                            }
//                            if (!overAllSuitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("")) {
//                                overAllSuitFiledWillfulDefaultWrittenOffStatus = overAllSuitFiledWillfulDefaultWrittenOffStatus + ",";
//                            }
//
//                            JSONObject RetailAccountDetailsObj = (JSONObject) RetailAccountDetails
//                                    .get(RetailAccountDetailsCount);
//
//                            String accountType = cf.getJsonValue(RetailAccountDetailsObj, "AccountType");
//                            String accountStatus = cf.getJsonValue(RetailAccountDetailsObj, "AccountStatus");
//                            String openDate = cf.getJsonValue(RetailAccountDetailsObj, "DateOpened");
//
//                            String OwnershipType = cf.getJsonValue(RetailAccountDetailsObj, "OwnershipType");
//                            String Open = cf.getJsonValue(RetailAccountDetailsObj, "Open");
//                            String InstallmentAmount = cf.getJsonValue(RetailAccountDetailsObj, "InstallmentAmount");
//                            overAllAccountTypes = overAllAccountTypes + accountType;
//                            overAllAccountStatus = overAllAccountStatus + accountStatus;
//                            overAllOpendate = overAllOpendate + openDate;
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->overAllOpendate: " + overAllOpendate);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->accountType: " + accountType);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->openDate: " + openDate);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->OwnershipType: " + OwnershipType);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Open: " + Open);
//
//                            if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
//                                JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
//                                for (int j = 0; j < History48Months.size(); j++) {
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->expDate");
//                                    JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
//
//                                    String daysPastDue = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->PaymentStatus: " + PaymentStatus);
//                                    String key = cf.getJsonValue(History48MonthsObj, "key");
//                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
//                                    YearMonth inputYearMonth = YearMonth.parse(key, formatter);
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
//                                    YearMonth currentYearMonth = YearMonth.now();
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
//                                    YearMonth twelveMonthsAgo = currentYearMonth.minusMonths(12);
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->twelveMonthsAgo: " + twelveMonthsAgo);
//                                    // Check if the input date is within the last 12 months
//                                    if (!inputYearMonth.isBefore(twelveMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 12 months: ");
//                                        totaldaysPastDue = totaldaysPastDue + daysPastDue;
//                                    }
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->totaldaysPastDue: " + totaldaysPastDue);
//
//                                }
//
//                            }
//
//                            boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
//                                    excludedEMIAccnts, OwnershipType, excludedOwners, Open);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->emiStatus: " + emiStatus);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->InstallmentAmount: " + InstallmentAmount);
//                            if (emiStatus) {
//                                if (!InstallmentAmount.isEmpty()) {
//                                    int totalEMI = Integer.parseInt(InstallmentAmount);
//                                    if (totalEMI < 0) {
//                                        totalNonEMICount++;
//                                    }
//                                    consolidated_emiAmnt += totalEMI;
//                                }
//                            }
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);
//                            boolean npaStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->npaStatus: " + npaStatus);
//
//                            if (npaStatus) {
//                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
//                                if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
//                                    JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
//                                    Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->History48Months: " + History48Months);
//                                    for (int j = 0; j < History48Months.size(); j++) {
//                                        JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
//                                        String Asset_Classification = cf.getJsonValue(History48MonthsObj, "AssetClassificationStatus");
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Asset_Classification: " + Asset_Classification);
//                                        String key = cf.getJsonValue(History48MonthsObj, "key");
//                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
//                                        YearMonth inputYearMonth = YearMonth.parse(key, formatter);
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
//                                        YearMonth currentYearMonth = YearMonth.now();
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
//                                        YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
//                                        // Check if the input date is within the last 60 months
//                                        if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
//                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
//                                            if ((Asset_Classification.equalsIgnoreCase("SUB")
//                                                    || Asset_Classification.equalsIgnoreCase("DBT")
//                                                    || Asset_Classification.equalsIgnoreCase("LOS"))) {
//                                                NPA = "Yes";
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->NPA: " + NPA);
//                            boolean setteledHistoryStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->setteledHistoryStatus: " + setteledHistoryStatus);
//
//                            if (setteledHistoryStatus) {
//                                if ("Settled".equalsIgnoreCase(accountStatus) || "Closed Account".equalsIgnoreCase(accountStatus)) {
//                                    if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
//                                        JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
//                                        for (int j = 0; j < History48Months.size(); j++) {
//                                            JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
//                                            String paymentStatus = cf.getJsonValue(History48MonthsObj, "PaymentStatus");
//                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->paymentStatus: " + paymentStatus);
//                                            String key = cf.getJsonValue(History48MonthsObj, "key");
//                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
//                                            YearMonth inputYearMonth = YearMonth.parse(key, formatter);
//                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
//                                            YearMonth currentYearMonth = YearMonth.now();
//                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
//                                            YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
//                                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
//                                            // Check if the input date is within the last 60 months
//                                            if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth)) {
//                                                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->key is less than 60 months: ");
//                                                if (paymentStatus.equalsIgnoreCase("SET")) {
//                                                    settledHistory = "Yes";
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->settledHistory: " + settledHistory);
//
//                            boolean wroStatus = checkKnockOffOwnershipStatus(ifr, OwnershipType, excludedOwnersGI);
//                            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->wroStatus: " + wroStatus);
//
//                            if (wroStatus) {
//                                if (!(cf.getJsonValue(RetailAccountDetailsObj, "History48Months").equalsIgnoreCase(""))) {
//                                    JSONArray History48Months = (JSONArray) parser.parse(cf.getJsonValue(RetailAccountDetailsObj, "History48Months"));
//                                    for (int j = 0; j < History48Months.size(); j++) {
//                                        JSONObject History48MonthsObj = (JSONObject) History48Months.get(j);
//                                        String key = cf.getJsonValue(History48MonthsObj, "key");
//                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yy");
//                                        YearMonth inputYearMonth = YearMonth.parse(key, formatter);
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->inputYearMonth: " + inputYearMonth);
//                                        YearMonth currentYearMonth = YearMonth.now();
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->currentYearMonth: " + currentYearMonth);
//                                        YearMonth sixtyMonthsAgo = currentYearMonth.minusMonths(60);
//                                        Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->sixtyMonthsAgo: " + sixtyMonthsAgo);
//                                        String WriteOffAmount = "0";
//                                        if (RetailAccountDetailsObj.containsKey("WriteOffAmount")
//                                                && RetailAccountDetailsObj.get("WriteOffAmount") != null) {
//                                            WriteOffAmount = cf.getJsonValue(RetailAccountDetailsObj, "WriteOffAmount");
//                                        }
//                                        if (!inputYearMonth.isBefore(sixtyMonthsAgo) && !inputYearMonth.isAfter(currentYearMonth) && Integer.parseInt(WriteOffAmount) > 0) {
//                                            writeOFF = "Yes";
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//
//                        totaldaysPastDue = "0";
//                        NPA = "No";
//                        dpd = "0";
//                        writeOFF = "No";
//                        settledHistory = "No";
//                        newToCredit = "Yes";
//                    }
//
//                }
//
//                String lapExists = "No";//Need to derive this data
//                String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
//                        + "CICNPACHECK,CICOVERDUE,WRITEOFF,"
//                        + "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
//                        + "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
//                        + "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
//                        + "values('" + ProcessInstanceId + "','" + score + "',"
//                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "',"
//                        + "'EF',"
//                        + "'" + consolidated_emiAmnt + "',"
//                        + "'" + totaldaysPastDue + "',"
//                        + "'" + applicantType + "',"
//                        + "'" + totalNonEMICount + "','" + settledHistory + "','"
//                        + NPA + "','" + NPA + "','" + settledHistory + "',"
//                        + "SYSDATE,'" + insertionOrderid + "')";
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query: " + qryCICDataUpdate1);
//                ifr.saveDataInDB(qryCICDataUpdate1);
////                String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
////                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
////                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
////                        + "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
////                        + "VALUES('" + ProcessInstanceId + "',"
////                        + "'','" + mobileNo + "','"+productCode+"','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
////                        + "'EXT','EF','CNS','" + lapExists + "','" + score + "',"
////                        + "'" + PastDueAmount + "','" + newToCredit + "',SYSDATE,SYSDATE)";
////                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
////                int queryResult = ifr.saveDataInDB(query);
////                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->queryResult: " + queryResult);
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->insertionOrderid: " + insertionOrderid);
////                crg.crgGenEquifax(ifr, responseBody, ProcessInstanceId, apiName,
////                        applicantType, insertionOrderid, isNTC);
//                return score;
//            } else {
//                Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->No response from Server..");
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
//            Log.errorLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Exception/EquiFax: " + e);
//            StringWriter errors = new StringWriter();
//            e.printStackTrace(new PrintWriter(errors));
//            Log.consoleLog(ifr, "Exception StackTrace:::" + errors);
//
//        } finally {
//            Log.consoleLog(ifr, "EquifaxAPI:getEquifaxCIBILScore->Capture Equifax REquest and Response Calling!");
//            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Equifax_API", request, responseBody, "", "", "",
//                    applicantType);
//        }
//        return RLOS_Constants.ERROR;
//    }
   
}
