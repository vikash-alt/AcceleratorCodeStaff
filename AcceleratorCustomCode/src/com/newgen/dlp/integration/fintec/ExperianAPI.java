/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.fintec;

import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.dlp.integration.common.APICommonMethods;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class ExperianAPI {

    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    APICommonMethods cm = new APICommonMethods();
    
    public String getExperianCIBILScore2(IFormReference ifr, String ProcessInstanceId,
            String AadharNo, String productType, String loanAmount, String applicantType) throws ParseException {// Added by Vikash Mehta
    	Log.consoleLog(ifr, "#### Started getExperianCIBILScore getExperianCIBILScore#### " );
        String apiName = "EXPERIAN";
        String insertionOrderid = "";
        Log.consoleLog(ifr, "apiName==>" + apiName);
        String aadharNo = "";
        String MobileNumber="";
        String cleanedMobileNumber="";
 
        String request = "";
        String responseBody = "";
        try {

            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
                    addressLine3 = "", city = "", state = "", pincode = "", gender = "", gendercode = "", stateCode = "", mobileNo = "";

            String Query1 = "";
            String Query2 = "";
            String paymentHistoryCombined = "";
            String suitFiledWillfulDefaultWrittenOffStatus = "";
            String AccountHoldertypeCode = "", AccountHoldertypeCodes = "", WRITEOFF_INP = "", ScoreCardAssetClarification = "";

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
                cleanedMobileNumber = MobileNumber.substring(MobileNumber.length() - 10);
            }
            CustomerAccountSummary cas = new CustomerAccountSummary();
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", cleanedMobileNumber);
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

            Query2 = "select a.LINE1,a.LINE2,a.LINE3,a.CITY_TOWN_VILLAGE,a.STATE,a.PINCODE from LOS_NL_ADDRESS a join los_nl_basic_info b on a.f_key=b.f_key and b.pid='"+ProcessInstanceId+"' and b.insertionorderid='"+finalIntOrderID+"' and b.applicanttype in ('CB', 'G')  and a.ADDRESSTYPE='P'";

            Log.consoleLog(ifr,"Mobile number final:: +"+MobileNumber);

            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
            //"^[A-Za-z ]+$";
            Log.consoleLog(ifr, "#Result pattern===>" + pattern);

            firstName = tempFirstName.replaceAll("[ .]", "");
            lastName = tempLastName.replaceAll("[ .]", "");

            if (lastName.equalsIgnoreCase("{}")) {
                lastName = "";
            }
            if (lastName.equalsIgnoreCase("{")) {
                lastName = "";
            }
            if (lastName.equalsIgnoreCase("}")) {
                lastName = "";
            }

            panNumber = tempPan;
            dateofBirth = tempDateOfBirth;
            gender = tempGender;
            mobileNo = MobileNumber;
            aadharNo=tempAadhar;

            if (gender.equalsIgnoreCase("MALE")) {
                gendercode = "1";
            } else if (gender.equalsIgnoreCase("FEMALE")) {
                gendercode = "2";
            } else {
                gendercode = "3";
            }

            Log.consoleLog(ifr, "firstName===>" + firstName);
            Log.consoleLog(ifr, "lastName====>" + lastName);
            Log.consoleLog(ifr, "PAN=========>" + panNumber);
            Log.consoleLog(ifr, "birthDate===>" + dateofBirth);
            Log.consoleLog(ifr, "gender======>" + gender);

            Log.consoleLog(ifr, "Query2==>" + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (Result1.size() > 0) {
                addressLine1 = Result1.get(0).get(0);
                addressLine2 = Result1.get(0).get(1);
                addressLine3 = Result1.get(0).get(2);
                city = Result1.get(0).get(3);
                state = Result1.get(0).get(4);
                pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "AddressLine1====>" + addressLine1);
            Log.consoleLog(ifr, "AddressLine2====>" + addressLine2);
            Log.consoleLog(ifr, "AddressLine3====>" + addressLine3);
            Log.consoleLog(ifr, "City============>" + city);
            Log.consoleLog(ifr, "State===========>" + state);
            Log.consoleLog(ifr, "Pincode=========>" + pincode);

            String Query3 = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "Query3==>" + Query3);
            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
            Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
            if (Result3.size() > 0) {
                stateCode = Result3.get(0).get(0);
            }

            if (stateCode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "StateCode founds to be empty for " + state + " in LOS_MST_STATE table.");
                return RLOS_Constants.ERROR + "StateCode not found";
            }
            if (addressLine1.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "addressLine1 not found");
                return RLOS_Constants.ERROR + "addressLine1 not found";
            }
            if (pincode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "pincode not found");
                return RLOS_Constants.ERROR + "pincode not found";
            }

            String DOB="";
            if(!dateofBirth.equalsIgnoreCase("")) {
            	DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
            	DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
            	Date date = originalFormat.parse(dateofBirth);
            	DOB = targetFormat.format(date);
            	Log.consoleLog(ifr, "DOB=======>" + DOB);
            }

            String LoanNo = ProcessInstanceId;
            String[] DataSplitter = LoanNo.split("-");
            String FTReferenceNumber = DataSplitter[0] + DataSplitter[1];
            Log.consoleLog(ifr, "FTReferenceNumber=======>" + FTReferenceNumber);

            String Username = pcm.getConstantValue(ifr, "EXPERIAN", "USERNAME");
            String Password = pcm.getConstantValue(ifr, "EXPERIAN", "PASSWORD");

            String enquiryReason = pcm.getConstantValue(ifr, "EXPERIAN", "ENQ_PURPOSE");//14
            String financePurpose = pcm.getConstantValue(ifr, "EXPERIAN", "FI_PURPOSE");//48
            String durationOfAgreement = pcm.getConstantValue(ifr, "EXPERIAN", "DUROFAGMNT");//180
            String scoreFlag = pcm.getConstantValue(ifr, "EXPERIAN", "SCOREFLAG");//1
            String psvFlag = pcm.getConstantValue(ifr, "EXPERIAN", "PSVFLAG");//Y
            String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");//Y
            String months_papl = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_PAPL");//Y
          
            String settledStatus = pcm.getConstantValue(ifr, "EXPERIAN", "SETTLED_STATUS");
            
             AccountHoldertypeCode="";

            String wriitenofffStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");
            String restructureStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");


            String productCode = "HL";
            Log.consoleLog(ifr, "productCode=======>" + productCode);
            String assetClassification_No = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_NO");
            String assetClassification_Yes = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_YES");
            String dpddays = pcm.getConstantValue(ifr, "EXPERIAN", "DPD_DAYS");


            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "EMIACCTTYPE");
            Log.consoleLog(ifr, "excludedEMIAccnts========>" + excludedEMIAccnts);
            String excludedEMIAccntStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "EMIACCTTYPESTATUS");
            Log.consoleLog(ifr, "excludedEMIAccntStatus===>" + excludedEMIAccntStatus);

            String excludedNPAAccnts = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "NPAACCTTYPE");
            Log.consoleLog(ifr, "excludedNPAAccnts========>" + excludedNPAAccnts);
            String excludedNPAAccntStatus = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "NPAACCTTYPESTATUS");
            Log.consoleLog(ifr, "excludedNPAAccntStatus===>" + excludedNPAAccntStatus);
            String excludedDPDAccnts = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "DPDACCTTYPE");
            Log.consoleLog(ifr, "excludedDPDAccnts========>" + excludedDPDAccnts);
            String excludedDPDAccntStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "DPDACCTTYPESTATUS");
            Log.consoleLog(ifr, "excludedDPDAccntStatus===>" + excludedDPDAccntStatus);
            String excludedWROAccnts = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "WROACCTTYPE");
            Log.consoleLog(ifr, "excludedWROAccnts========>" + excludedWROAccnts);
            String excludedWROAccntStatus = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "WROACCTTYPESTATUS");
            Log.consoleLog(ifr, "excludedWROAccntStatus===>" + excludedWROAccntStatus);
            String excludedOwners = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "OWNERTYPE");
            Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);

            String scoreCardAccTypes = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "SRCACCTTYPE");
            Log.consoleLog(ifr, "excludedWROAccnts========>" + excludedWROAccnts);
            String scoreCardAccStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "SRCCCTTYPESTATUS");
            Log.consoleLog(ifr, "excludedWROAccntStatus===>" + excludedWROAccntStatus);//scoreCardAssetClarification
            String scoreCardAssetClarification = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "ASSETCLARIFICATION");
            Log.consoleLog(ifr, "scoreCardAssetClarification===>" + scoreCardAssetClarification);

            request = "{\n"
                    + "  \"INProfileRequest\": {\n"
                    + "    \"Identification\": {\n"
                    + "      \"XMLUser\": \"" + Username + "\",\n"//Need to configure in INI
                    + "      \"XMLPassword\": \"" + Password + "\"\n"//Need to configure in INI
                    + "    },\n"
                    + "    \"Application\": {\n"
                    + "      \"FTReferenceNumber\": \"" + FTReferenceNumber + "\",\n"
                    + "      \"CustomerReferenceID\": \"" + customerId + "\",\n"
                    + "      \"EnquiryReason\": " + enquiryReason + ",\n"
                    + "      \"FinancePurpose\": " + financePurpose + ",\n"
                    + "      \"AmountFinanced\": " + loanAmount + ",\n"
                    + "      \"DurationOfAgreement\": " + durationOfAgreement + ",\n"
                    + "      \"ScoreFlag\": " + scoreFlag + ",\n"
                    + "      \"PSVFlag\": \"" + psvFlag + "\"\n"
                    + "    },\n"
                    + "    \"Applicant\": {\n"
                    + "      \"Surname\": \"" + lastName + "\",\n"
                    + "      \"FirstName\": \"" + firstName + "\",\n"
                    + "      \"MiddleName1\": \"\",\n"
                    + "      \"MiddleName2\": \"\",\n"
                    + "      \"MiddleName3\": \"\",\n"
                    + "      \"GenderCode\": \"" + gendercode + "\",\n"
                    + "      \"IncomeTaxPAN\": \"" + panNumber + "\",\n"
                    + "      \"PANIssueDate\": \"\",\n"
                    + "      \"PANExpirationDate\": \"\",\n"
                    + "      \"PassportNumber\": \"\",\n"
                    + "      \"PassportIssueDate\": \"\",\n"
                    + "      \"PassportExpirationDate\": \"\",\n"
                    + "      \"VoterIdentityCard\": \"\",\n"
                    + "      \"VoterIDIssueDate\": \"\",\n"
                    + "      \"VoterIDExpirationDate\": \"\",\n"
                    + "      \"DriverLicenseNumber\": \"\",\n"
                    + "      \"DriverLicenseIssueDate\": \"\",\n"
                    + "      \"DriverLicenseExpirationDate\": \"\",\n"
                    + "      \"RationCardNumber\": \"\",\n"
                    + "      \"RationCardIssueDate\": \"\",\n"
                    + "      \"RationCardExpirationDate\": \"\",\n"
                    + "      \"UniversalIDNumber\": \"" + aadharNo + "\",\n"
                    + "      \"UniversalIDIssueDate\": \"\",\n"
                    + "      \"UniversalIDExpirationDate\": \"\",\n"
                    + "      \"DateOfBirth\": \"" + DOB + "\",\n"
                    + "      \"STDPhoneNumber\": \"\",\n"
                    + "      \"PhoneNumber\": \"\",\n"
                    + "      \"TelephoneExtension\": \"\",\n"
                    + "      \"TelephoneType\": 1,\n"
                    + "      \"MobilePhone\": \"" + mobileNo + "\",\n"
                    + "      \"EMailId\": \"\"\n"
                    + "    },\n"
                    + "    \"Details\": {\n"
                    + "      \"Income\": \"\",\n"
                    + "      \"MaritalStatus\": 2,\n"
                    + "      \"EmployStatus\": \"S\",\n"
                    + "      \"TimeWithEmploy\": \"\",\n"
                    + "      \"NumberOfMajorCreditCardHeld\": 0\n"
                    + "    },\n"
                    + "    \"Address\": {\n"
                    + "      \"FlatNoPlotNoHouseNo\": \"" + addressLine1 + "\",\n"
                    + "      \"BldgNoSocietyName\": \"" + addressLine2 + "\",\n"
                    + "      \"RoadNoNameAreaLocality\": \"" + addressLine3 + "\",\n"
                    + "      \"City\": \"" + city + "\",\n"
                    + "      \"Landmark\": \"\",\n"
                    + "      \"State\": \"" + stateCode + "\",\n"
                    + "      \"PinCode\": \"" + pincode + "\"\n"
                    + "    },\n"
                    + "    \"AdditionalAddressFlag\": {\n"
                    + "      \"Flag\": \"Y\"\n"
                    + "    },\n"
                    + "    \"AdditionalAddress\": {\n"
                    + "      \"FlatNoPlotNoHouseNo\": \"" + addressLine1 + "\",\n"
                    + "      \"BldgNoSocietyName\": \"" + addressLine2 + "\",\n"
                    + "      \"RoadNoNameAreaLocality\": \"" + addressLine3 + "\",\n"
                    + "      \"City\": \"" + city + "\",\n"
                    + "      \"Landmark\": \"\",\n"
                    + "      \"State\": \"" + stateCode + "\",\n"
                    + "      \"PinCode\": \"" + pincode + "\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "  \n"
                    + "}";
            Log.consoleLog(ifr, "Request Body===============>" + responseBody);
            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===============>" + responseBody);
            Log.consoleLog(ifr, "####  getExperianCIBILScore Response #### " );

            Log.consoleLog(ifr, "EXPERIAN API RESPONSE==>" + responseBody);
            //Modified by Ahmed on 28-06-2024 for redundancy as well as ApplicantType Hanling
//            cm.CaptureExperianRequestResponse(ifr, ProcessInstanceId, "Experian_API",
//                    request, responseBody, "", "", "");

            //        cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Experian_API", request, responseBody, "", "", "", applicantType);
            if (!responseBody.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
                JSONObject resultObj = new JSONObject(OutputJSON);
                
                

                Log.consoleLog(ifr, "resultObj==>" + resultObj);

                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body==>" + body);
                JSONObject bodyJSON = (JSONObject) parser.parse(body);
                JSONObject bodyObj = new JSONObject(bodyJSON);

                String INProfileResponseData = bodyObj.get("INProfileResponse").toString();
                Log.consoleLog(ifr, "INProfileResponseData==>" + INProfileResponseData);

                JSONObject INProfileResponseDataJSON = (JSONObject) parser.parse(INProfileResponseData);
                JSONObject INProfileResponseDataJSONObj = new JSONObject(INProfileResponseDataJSON);

                String Score = INProfileResponseDataJSONObj.get("SCORE").toString();
                Log.consoleLog(ifr, "SCORE===>" + Score);

                //==============Added by Ahmed on 13-05-2024 for CIC Report Generation of Experian===========
                String cicReportGenReq = pcm.getConstantValue(ifr, "EXPERIAN", "CICREPORTGENREQ");//Added by Ahmed on 02-05-2024
                Log.consoleLog(ifr, "cicReportGenReq==>" + cicReportGenReq);
                if (cicReportGenReq.equalsIgnoreCase("Y")) {
                    String encodedString = Base64.getEncoder().encodeToString(body.getBytes());
                    Log.consoleLog(ifr, "Encoded string: " + encodedString);
                    String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Experian", encodedString, "NGREPORTTOOL_EXPERIAN");
                    Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
                    cm.updateCICReportStatus(ifr, "Experian", generateReportStatus, applicantType);//Added by Ahmed on 26-07-2024 for Status Updation of CIC Report
                }
                //==============Added by Ahmed on 13-05-2024 for CIC Report Generation of Experian===========

                JSONObject ScoreDataJSON = (JSONObject) parser.parse(Score);
                JSONObject ScoreDataJSONObj = new JSONObject(ScoreDataJSON);

                String BureauScore = ScoreDataJSONObj.get("BureauScore").toString();
                String type = "";
                Log.consoleLog(ifr, "BureauScore===>" + BureauScore);
                //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
                if ((BureauScore.equalsIgnoreCase("000-1"))) {
                    String cbilimmuneAccept = ConfProperty.getCommonPropertyValue("CICImmune_Accept");
                    Log.consoleLog(ifr, "cbilimmuneAccept==>" + cbilimmuneAccept);

                    if (cbilimmuneAccept.equalsIgnoreCase("YES")) {
                        BureauScore = "-1";
                    } else {
                        BureauScore = "0";
                    }

                }
               
              
                String overAllAccountTypes = "";
                String overAllAccountStatus = "";
                String overAllOpendate = "";
                String overAllSuitFiledWillfulDefaultWrittenOffStatus = "";
                int totalNonEMICount = 0;
                String NPA = "No";
                String dpd = "0";
                String writeOFF = "No";
                String consolidated_emiAmnt = "";
                String totaldaysPastDue = "";
                double totalSum = 0;
                double finalscheduledMonthlyPaymentAmount = 0;
                String Outstanding_Balance_All = "";
                String Payment_History_Profile_Data = "";
                ArrayList<String> emiAmountTagValues = new ArrayList<>();
                String consolidatedtwelveMonthData = "";
                String scoreCardAccType = "";
                String scoreCardAccStatu = "";
                String overALlAsset_Classification = "";

                String settledHistory = "No";
                String NPA_INP = "No";
                String GUARANTORNPA_INP = "No";
                String GUARANTORWRITEOFFSETTLEDHIST_INP = "No";
                String paymentHistoryClob = "000";

                if (!(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account").equalsIgnoreCase(""))) {

                    String query = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
                    List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
                    String loan_selected = loanSelected.get(0).get(0);
                    Log.consoleLog(ifr, "loan type==>%%" + loan_selected);
                    JSONObject CAIS_Account = (JSONObject) parser.parse(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account"));

                    JSONObject CAIS_SummaryJSON = (JSONObject) parser.parse(cf.getJsonValue(CAIS_Account, "CAIS_Summary"));
                    Log.consoleLog(ifr, "CAIS_Summary===>  " + CAIS_SummaryJSON);
                    Log.consoleLog(ifr, "CAIS_Summary===>  " + CAIS_SummaryJSON);
                    JSONObject CAIS_SummaryJSONJSONObj = new JSONObject(CAIS_SummaryJSON);
                    JSONObject Total_Outstanding_BalanceJSON = (JSONObject) parser.parse(cf.getJsonValue(CAIS_SummaryJSONJSONObj, "Total_Outstanding_Balance"));
                    Log.consoleLog(ifr, "Total_Outstanding_BalanceJSON===>  " + Total_Outstanding_BalanceJSON);
                    JSONObject Total_Outstanding_BalanceJSONObj = new JSONObject(Total_Outstanding_BalanceJSON);
                    Outstanding_Balance_All = Total_Outstanding_BalanceJSONObj.get("Outstanding_Balance_All").toString();
                    Log.consoleLog(ifr, "Outstanding_Balance_All===>  " + Outstanding_Balance_All);
                    JSONArray CAIS_Account_DETAILS = new JSONArray();

                    //Vandana Code variable
                    //Modified by Ahmed on 06-09-2024 for MoneshKumar`s code
                    //Modified by Monesh  on 26-09-2024 for 
                    String accountDetails = cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").toString();
                    if ((!accountDetails.equalsIgnoreCase(""))
                            && (Integer.parseInt(BureauScore) > 10)) {
                        //if (!(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").equalsIgnoreCase(""))) {
                        //  String CAIS_Accountdetails=cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS");
                        Object CAISAccountdetails = CAIS_Account.get("CAIS_Account_DETAILS");
                        Log.consoleLog(ifr, "CAIS_AccountS_DETAILS " + cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
                        if (CAISAccountdetails instanceof JSONArray) {
                            CAIS_Account_DETAILS = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
                            Log.consoleLog(ifr, "Test " + CAIS_Account_DETAILS.toString());
                        } else {

                            CAIS_Account_DETAILS.add(CAISAccountdetails);
                            Log.consoleLog(ifr, "Test add" + CAIS_Account_DETAILS.toString());
                        }
                        Log.consoleLog(ifr, "CAIS_Account_DETAILS " + Outstanding_Balance_All);

                        for (int i = 0; i < CAIS_Account_DETAILS.size(); i++) {

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
                            JSONObject CAIS_Account_DETAILSObj = (JSONObject) CAIS_Account_DETAILS.get(i);
                            String Payment_History_Profile = cf.getJsonValue(CAIS_Account_DETAILSObj, "Payment_History_Profile");
                            Log.consoleLog(ifr, "Payment_History_Profile" + Payment_History_Profile);
                            // added by vandana start

                            Log.consoleLog(ifr, "Payment_History_Profile_Data" + Payment_History_Profile);
                            String payment = Payment_History_Profile.replace("?", "0")
                                    .replace("S", "0").replace("N", "0")
                                    .replace("B", "0").replace("D", "0")
                                    .replace("M", "0").replace("L", "0");
                            Log.consoleLog(ifr, "Payment_History_Profile" + payment);

                            //-----------------
                            if (payment.length() < 3) {
                                Log.consoleLog(ifr, "Insufficient data to extract payment history.");
                                //return "";//Modified by Ahmed on 18-07-2024
                            } else {
                                // Calculate the maximum number of complete months that can be extracted
                                int numberOfMonths = payment.length() / 3;
                                Log.consoleLog(ifr, "Payment_History_Profile numberOfMonths" + numberOfMonths);
                                // Extract payment status for each complete month
                                StringBuilder sb = new StringBuilder();

                                for (int j = 0; j < numberOfMonths; j++) {
                                    // Each month's status is represented by 3 characters
                                    String twelveMonthData = payment.substring(j * 3, (j * 3) + 3);
                                    // sb.append("Month ").append(j + 1).append(": ").append(Payment_History_Profile_Data).append("\n");
                                    // consolidatedtwelveMonthData = consolidatedtwelveMonthData + twelveMonthData;
                                } // end

                            }

                            // Log.consoleLog(ifr, "consolidatedtwelveMonthData" + consolidatedtwelveMonthData);
                            paymentHistoryCombined = Payment_History_Profile_Data + payment;
                            Log.consoleLog(ifr, "paymentHistoryCombined" + Payment_History_Profile_Data);
                            String accountType = cf.getJsonValue(CAIS_Account_DETAILSObj, "Account_Type");
                            String accountStatus = cf.getJsonValue(CAIS_Account_DETAILSObj, "Account_Status");
                            String openDate = cf.getJsonValue(CAIS_Account_DETAILSObj, "Open_Date");
                            String closedDate = cf.getJsonValue(CAIS_Account_DETAILSObj, "Date_Closed");//Added by Ahmed on 16-07-2024
                            overAllAccountTypes = overAllAccountTypes + accountType;
                            overAllAccountStatus = overAllAccountStatus + accountStatus;
                            String ownerType = cf.getJsonValue(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");//Added by Ahmed on 26-09-2024

                            overAllOpendate = overAllOpendate + openDate;
                            Log.consoleLog(ifr, "overAllOpendate" + overAllOpendate);//SuitFiledWillfulDefaultWrittenOffStatus
                            String SuitFiledWillfulDefaultWrittenOffStatus = cf.getJsonValue(CAIS_Account_DETAILSObj, "SuitFiledWillfulDefaultWrittenOffStatus");
                            overAllSuitFiledWillfulDefaultWrittenOffStatus = overAllSuitFiledWillfulDefaultWrittenOffStatus + SuitFiledWillfulDefaultWrittenOffStatus;
                            if (overAllSuitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("00")) {
                                suitFiledWillfulDefaultWrittenOffStatus = "No";
                            } else {
                                suitFiledWillfulDefaultWrittenOffStatus = "Yes";
                            }
                            Log.consoleLog(ifr, "suitFiledWillfulDefaultWrittenOffStatus==>" + suitFiledWillfulDefaultWrittenOffStatus);
                            // vandana
                            Log.consoleLog(ifr, "suitFiledWillfulDefaultWrittenOffStatus==>####" + cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));

                            if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History").equalsIgnoreCase(""))) {
                                JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
                                for (int j = 0; j < CAIS_Account_History.size(); j++) {
                                    Log.consoleLog(ifr, "ExperianAPI==>::expDate");
                                    JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
                                    String Asset_Classification = cf.getJsonValue(CAIS_Account_HistoryObj, "Asset_Classification");
                                    overALlAsset_Classification = Asset_Classification + Asset_Classification;
                                    String daysPastDue = (String) CAIS_Account_HistoryObj.get("Days_Past_Due");
                                    Log.consoleLog(ifr, "CAIS_Account_HistoryObj*****)" + CAIS_Account_HistoryObj.get("Year").toString());

                                    if (CAIS_Account_HistoryObj.get("Year") != "") {

                                        int expYear = Integer.parseInt(CAIS_Account_HistoryObj.get("Year").toString());

                                        int expMonth = Integer.parseInt(CAIS_Account_HistoryObj.get("Month").toString()); // if Month is String, otherwise cast to Integer

                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate");
                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expYear);
                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expMonth);

                                        String expDateStr = String.format("01/%02d/%d", expMonth, expYear);
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                                        Date expDate = dateFormatter.parse(expDateStr);

                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expDate);
                                        // Starts Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK                                       
                                        int totalmonths = 0;
                                        if (loan_selected.equalsIgnoreCase("Canara Pension")
                                                || loan_selected.equalsIgnoreCase("Canara Budget")
                                                || loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                                            totalmonths = Integer.parseInt(months);
                                            daysPastDue = validatePaymentHistory_EX(ifr, expDate, daysPastDue, totalmonths);

                                            totaldaysPastDue = totaldaysPastDue + daysPastDue;

                                        } else {
                                            totalmonths = Integer.parseInt(months_papl);
                                            daysPastDue = validatePaymentHistory_EX(ifr, expDate, daysPastDue, totalmonths);

                                            totaldaysPastDue = totaldaysPastDue + daysPastDue;

                                        }

                                    }
                                    Log.consoleLog(ifr, "ExperianAPI==>::expDate" + totaldaysPastDue);
                                    // End

                                }
                                if (!overALlAsset_Classification.equalsIgnoreCase("")) {
                                    overALlAsset_Classification = overALlAsset_Classification + ",";//overALLdateOpened
                                }
                            }
                            Log.consoleLog(ifr, "overALlAsset_Classification==>" + overALlAsset_Classification);
                            boolean npaStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedNPAAccnts, accountStatus, excludedNPAAccntStatus, closedDate);
                            Log.consoleLog(ifr, "npaStatus==>" + npaStatus);

                            boolean dpdStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedDPDAccnts, accountStatus, excludedDPDAccntStatus, closedDate);
                            Log.consoleLog(ifr, "dpdStatus==>" + dpdStatus);

                            boolean wroStatus = checkKnockOffAccFilterStatus_WRO(ifr, accountType, excludedWROAccnts, accountStatus, excludedWROAccntStatus, closedDate);
                            Log.consoleLog(ifr, "wroStatus==>" + wroStatus);

                           
//Modified by AHmed on 26-09-2024 for Ownership Indicator Filtering
                            boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
                                    excludedEMIAccnts, accountStatus,
                                    excludedEMIAccntStatus, closedDate, ownerType, excludedOwners);
                            Log.consoleLog(ifr, "emiStatus==>@@@" + emiStatus);

                            if (dpdStatus) {
                            	try
                            	{
                                if (payment.length() > 6) {
                                    payment = payment.substring(0, 6);
                                }
                                Log.consoleLog(ifr, payment);
                                int k = 0;
                                for (int l = 0; l < payment.length(); l++) {
                                    int val = Integer.parseInt(payment.substring(k, l + 1));
                                    Log.consoleLog(ifr, "@@@@@payment"+payment);
                                    k++;
                                    if (val > 0) {
                                        dpd = String.valueOf(val);
                                        Log.consoleLog(ifr, "@@@@@found");
                                        break;
                                    }
                                }
                                
                            }catch(Exception e)
                            {
                            	Log.consoleLog(ifr, "@@@@@ dpdStatus Exception"+e);
                            }  
                                
                                
                                
                                
                            } else {
                                Log.consoleLog(ifr, "DPD Account Type Excluded. Please check config master. Looping next..");
                            }

                            if (wroStatus) {
                            	
                            	try
                            	{
                                    Log.consoleLog(ifr, "inside wroStatus .."+wroStatus);
                                    Log.consoleLog(ifr, "inside wroStatus .. CAIS_Account_DETAILSObj"+CAIS_Account_DETAILSObj);

                            	String written_off_Settled_Status=CAIS_Account_DETAILSObj.get("Written_off_Settled_Status").toString();
                            	String accountHoldertype= cf.getJsonValue
                            			(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");;
                            	 
                            	 
                            	 
                         // String ownerType = cf.getJsonValue(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");
                            	// written off  settled status
                            	if(settledStatus.contains(written_off_Settled_Status))
                            	{
                            		 Log.consoleLog(ifr, "@@@@inside settledStatus .."+settledStatus);
                            		if (!(cf.getJsonValue(CAIS_Account_DETAILSObj,
                                   		   "CAIS_Account_History").equalsIgnoreCase(""))) {
                                           JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
                                           for (int j = 0; j < CAIS_Account_History.size(); j++) {
                                               JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
                                               
                                               String Year = cf.getJsonValue(CAIS_Account_HistoryObj, "Year");
                                               String Month = cf.getJsonValue(CAIS_Account_HistoryObj, "Month");
                                               String dayPastDue = cf.getJsonValue(CAIS_Account_HistoryObj, "Days_Past_Due");
                                         
                            		
                            		}
                                       } 
                            	}
                            	
                            	}catch(Exception e)
                            	{
                        Log.consoleLog(ifr, "Exception wroStatus.."+e);

                            	}
                            	
                            	 
                            	
                                 
                            	
                            }

                            if (npaStatus) {
                            	
                            	try
                            	{
                            	
                                if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History")
                                		.equalsIgnoreCase(""))) {
                                    JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
                                    for (int j = 0; j < CAIS_Account_History.size(); j++) {
                                        JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
                                       
                                    }
                                }
                                
                            }catch(Exception e)
                            {
                            	 Log.consoleLog(ifr, " Exception NPA::::: nparesult====>***"+e); 
                            }

                            if (emiStatus) {
                                if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "Advanced_Account_History").equalsIgnoreCase(""))) {
                                    JSONArray Advanced_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "Advanced_Account_History"));

                                    int count = 0;
                                    for (int j = 0; j < Advanced_Account_History.size(); j++) {
                                        Log.consoleLog(ifr, "count/OutsideideEMIValueNULL Loop===>" + count);

                                        JSONObject Advanced_Account_HistoryObj = (JSONObject) Advanced_Account_History.get(j);

                                        //Modified by Ahmed on 03-06-2024 for performing Total Non EMI cals
                                        if (!cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount").equalsIgnoreCase("")) {
                                            Log.consoleLog(ifr, "EMI_Amount tag available");
                                            String EMI_Amount = cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount");
                                            Log.consoleLog(ifr, "EMI_Amount===>" + EMI_Amount);

                                            
                                            if (count == 0) {
                                                emiAmountTagValues.add(EMI_Amount);

                                                if (EMI_Amount.equalsIgnoreCase("")) {
                                                    Log.consoleLog(ifr, "EMI_Amount tag available but empty");
                                                    String currentBalance = cf.getJsonValue(Advanced_Account_HistoryObj, "Current_Balance");
                                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
                                                    if (currentBalance.equalsIgnoreCase("")) {
                                                        currentBalance = "0";
                                                    }

                                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
                                                    if (Integer.parseInt(currentBalance) > 0) {
                                                        totalNonEMICount++;
                                                    }

                                                    Log.consoleLog(ifr, "totalNonEMICount=>" + totalNonEMICount);
                                                }

                                                count++;
                                            }
                                        } else {
                                            Log.consoleLog(ifr, "EMI_Amount tag not available");

                                            if (count == 0) {
                                                String currentBalance = cf.getJsonValue(Advanced_Account_HistoryObj, "Current_Balance");
                                                Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
                                                if (currentBalance.equalsIgnoreCase("")) {
                                                    currentBalance = "0";
                                                }

                                                Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
                                                if (Integer.parseInt(currentBalance) > 0) {
                                                    totalNonEMICount++;
                                                }

                                                Log.consoleLog(ifr, "totalNonEMICount=>" + totalNonEMICount);
                                                count++;
                                            }

                                        }

                                        //Commented by Ahmed on 03-06-2024 for performing Total Non EMI cals
//                                        String EMI_Amount = cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount");
//                                        Log.consoleLog(ifr, "EMI_Amount===>" + EMI_Amount);
//                                        Log.consoleLog(ifr, "consolidated_emiAmnt  " + consolidated_emiAmnt);
//                                        if ((!EMI_Amount.equalsIgnoreCase(""))
//                                                && (EMI_Amount != null)) {
//
//                                            Log.consoleLog(ifr, "count/InsideEMIValueNULL Loop===>" + count);
//                                            if (count == 0) {
//                                                emiAmountTagValues.add(EMI_Amount);
//                                                count++;
//                                            }
//                                        }
                                    }
                                }
                            }

                        }

                        //Vandana code restructured here..
                        Log.consoleLog(ifr, "overAllAccountTypes  " + overAllAccountTypes);
                        Log.consoleLog(ifr, "overAllAccountStatus  " + overAllAccountStatus);
                        String scoreCardstatus = checScoreCardAccountTypeStatus(ifr, overAllAccountTypes, scoreCardAccTypes,
                                overAllAccountStatus, scoreCardAccStatus, overALlAsset_Classification, scoreCardAssetClarification);
                        Log.consoleLog(ifr, "scoreCardstatus  " + scoreCardstatus);
                        String[] statuses = scoreCardstatus.split("-");

                        // Ensure that we have exactly two parts
                        if (statuses.length == 3) {
                            scoreCardAccType = statuses[0].trim(); // Trim to remove any leading/trailing whitespace
                            scoreCardAccStatu = statuses[1].trim();
                            ScoreCardAssetClarification = statuses[2].trim();

                            // Now you can use yesStatus and noStatus as needed
                            Log.consoleLog(ifr, "scoreCardAccType  " + scoreCardAccType);
                            Log.consoleLog(ifr, "scoreCardAccStatu  " + scoreCardAccStatu);
                            Log.consoleLog(ifr, "ScoreCardAssetClarification  " + ScoreCardAssetClarification);
                        } else {
                            // Handle unexpected format if needed
                            System.out.println("Unexpected format: " + scoreCardstatus);
                        }
                        Log.consoleLog(ifr, "overAllOpendate" + overAllOpendate);//CheckPaymenyHistory
                        String DateOpen = checScoreCardDateOpend(ifr, overAllOpendate);
                        Log.consoleLog(ifr, "DateOpen  " + DateOpen);

                        paymentHistoryClob = cm.getClobSnippetData(ifr, totaldaysPastDue);
                        // ADDED BY VANDANA
                        if (overAllAccountTypes.equalsIgnoreCase("1")
                                || overAllAccountTypes.equalsIgnoreCase("4")) {
                            AccountHoldertypeCode = "Yes";
                        } else {
                            AccountHoldertypeCode = "No";
                        }

                        if (scoreCardAccStatu.equalsIgnoreCase("Yes") & AccountHoldertypeCode.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & suitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("Yes")) {
                            settledHistory = "Yes";
                        }
                        Log.consoleLog(ifr, "EXPERIANsettledHistory" + settledHistory);

                        if (ScoreCardAssetClarification.equalsIgnoreCase("Yes") & AccountHoldertypeCode.equalsIgnoreCase("Yes")) {
                            NPA_INP = "Yes";
                        }
                        if (ScoreCardAssetClarification.equalsIgnoreCase("Yes") & AccountHoldertypeCodes.equalsIgnoreCase("Yes")) {
                            GUARANTORNPA_INP = "Yes";
                        }

                        if (scoreCardAccStatu.equalsIgnoreCase("Yes") & AccountHoldertypeCodes.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & suitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("Yes")) {
                            GUARANTORWRITEOFFSETTLEDHIST_INP = "Yes";
                        }

                        //----
                    }

                } else {
                    consolidated_emiAmnt = "0";
                    Outstanding_Balance_All = "No data Found";

                }

                Log.consoleLog(ifr, "emiAmountTagValues  " + emiAmountTagValues.toString());
                Log.consoleLog(ifr, "emiAmountTagValues  " + emiAmountTagValues.size());

                if (!emiAmountTagValues.isEmpty()) {
                    HashSet<String> hashSet = new HashSet<>(emiAmountTagValues);
                    ArrayList<String> distinctArrayList = new ArrayList<>(hashSet);

                    Log.consoleLog(ifr, "Distinct elements==>" + distinctArrayList.toString());
                    for (String element : distinctArrayList) {
                        Log.consoleLog(ifr, "Distinct EMI Value from a Single Block==>" + element);
                        if ((!element.equalsIgnoreCase(""))
                                && (element != null)) {
                            double scheduledMonthlyPaymentAmount = Double.parseDouble(element);
                            totalSum = scheduledMonthlyPaymentAmount;
                            double roundedValue = Math.round(scheduledMonthlyPaymentAmount * 100.0) / 100.0;
                            finalscheduledMonthlyPaymentAmount += roundedValue;
                        }
                    }
                }
                String formattedValue = String.format(Locale.US, "%.2f",
                        finalscheduledMonthlyPaymentAmount);
                consolidated_emiAmnt = formattedValue;

                Log.consoleLog(ifr, "@@@@EMI Consolidated Amount==>" + finalscheduledMonthlyPaymentAmount);

                Log.consoleLog(ifr, "settledHistory:" + settledHistory);
                Log.consoleLog(ifr, "@@@@@NPA_INP:" + NPA_INP);
                Log.consoleLog(ifr, "GUARANTORNPA_INP:" + GUARANTORNPA_INP);
                Log.consoleLog(ifr, "####GUARANTORWRITEOFFSETTLEDHIST_INP: budget Pension Vehicle " + GUARANTORWRITEOFFSETTLEDHIST_INP);
                Log.consoleLog(ifr, "#####GUARANTORWRITEOFFSETTLEDHIST_INP: nparesult nparesult nparesult " );

//               
                String lapExists = "No";//Need to derive this data
                String newToCredit = "No";//Need to derive this data

                String qryCICDataUpdate1 = "",lessThan5year = "No",npaMoreThan5year = "No",qryCICDataUpdate2 = "";
                
                String restructured="",writteOff="",settled="",wrmoreThan5year="",accountHolder="";
                
                
               
                
//                    if ((ProductType.equalsIgnoreCase("PAPL"))
//                            || (ProductType.equalsIgnoreCase("LAD"))) {
                qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,"
                		+ "APPLICANT_UID,RESTRUCTURED,SETTLED,NPAISFIVEYEARS,WRISFIVEYEARS) "
                        + "values('" + ProcessInstanceId + "','" + BureauScore + "',"
                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "','EX',"
                        + "'" + consolidated_emiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEMICount + "','" + settledHistory + "',"
                        + "'" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "',SYSDATE,"
                        + "'"+insertionOrderid+"','" + restructured + "','" + settled + "','" + npaMoreThan5year 
                        + "','" + wrmoreThan5year + "')";
                Log.consoleLog(ifr, "Insert query: LOS_TRN_CREDITHISTORY" + qryCICDataUpdate1);
                ifr.saveDataInDB(qryCICDataUpdate1);
                // } else {
//                qryCICDataUpdate2 = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
//                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
//                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
//                        + "CIC_SCORE,NPA_STATUS,DPD,WRITEOFF_STATUS,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
//                        + "VALUES('" + ProcessInstanceId + "',"
//                        + "'','" + mobileNo + "','" + productCode + "','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','"+insertionOrderid+"',\n"
//                        + "'EXT','EX','CNS','" + lapExists + "','" + BureauScore + "',"
//                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "','" + consolidated_emiAmnt + "','" + newToCredit + "',SYSDATE,SYSDATE)";
                // }
                
                Log.consoleLog(ifr, "qryCICDataUpdate2: EXPERIAN" + qryCICDataUpdate2);
                ifr.saveDataInDB(qryCICDataUpdate2);

                    Log.consoleLog(ifr, "productType==>PAPL");
                    String ACC_SUMMARY_Update = "UPDATE LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
                            + " SET "
                            + " EXP_CBSCORE = '" + BureauScore + "',  "
                            + " MONTHLYDEDUCTION = '" + consolidated_emiAmnt + "',  "
                            + " TOTALOUTSTANDINGLIABILITY = '" + Outstanding_Balance_All + "'  "
                            + " WHERE "
                            + " WINAME = '" + ProcessInstanceId + "' ";

                    Log.consoleLog(ifr, "Update Query in ACC_SUMMARY_Update" + ACC_SUMMARY_Update);
                    ifr.saveDataInDB(ACC_SUMMARY_Update);
//                if (!productCode.equalsIgnoreCase("MF")) {
//                    Log.consoleLog(ifr, "#### Started CRG Validation#### ");
//                    crg.crgGenExperian(ifr, resultObj, ProcessInstanceId, apiName, applicantType, type,insertionOrderid);
//                    Log.consoleLog(ifr, "#### Ended  CRG Validation #### ");
//                }

                return BureauScore;
            } else {
            	
            	type="NoCIC";
            	   Log.consoleLog(ifr, "####Inside ELse ####  query: NOCIC" + type);
            	consolidated_emiAmnt="0";
            	 String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,"
                 		+ "APPLICANT_UID,RESTRUCTURED,SETTLED,NPAISFIVEYEARS,WRISFIVEYEARS) "
                         + "values('" + ProcessInstanceId + "','" + BureauScore + "',"
                         + "'" + NPA + "','" + dpd + "','" + writeOFF + "','EX',"
                         + "'" + consolidated_emiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEMICount + "','" + settledHistory + "',"
                         + "'" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "',SYSDATE,"
                         + "'"+insertionOrderid+"','" + "" + "','" + "" + "','" + "" 
                         + "','" + "" + "')";
                 Log.consoleLog(ifr, "####Insert query:#### NOCIC" + qryCICDataUpdate1);
                 ifr.saveDataInDB(qryCICDataUpdate1);
//                 if (!productCode.equalsIgnoreCase("MF")) {
//                    Log.consoleLog(ifr, "####Started CRG Validation #### " );
//                    crg.crgGenExperian(ifr,resultObj,ProcessInstanceId,apiName,applicantType,type,insertionOrderid);
//                    Log.consoleLog(ifr, "####Ended#####  CRG Validation " );
//                 }
                 return BureauScore;
            }
               
                
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Experian Exception===>  " + e);
            Log.errorLog(ifr, "Experian Exception===>  " + e);
        } finally {
            /*cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);*/
            if (!request.isEmpty()) {
                cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Experian_API", request, responseBody, "", "", "", (!insertionOrderid.equalsIgnoreCase("") ? insertionOrderid : applicantType));
            }

        }
        return RLOS_Constants.ERROR;
    }

//    public String getExperianCIBILScore2(IFormReference ifr, String ProcessInstanceId,
//            String AadharNo, String productType, String loanAmount, String applicantType) throws ParseException {
//    	Log.consoleLog(ifr, "#### Started getExperianCIBILScore getExperianCIBILScore#### " );
//        String apiName = "EXPERIAN";
//        String insertionOrderid = "";
//        Log.consoleLog(ifr, "apiName==>" + apiName);
//        String aadharNo = AadharNo;
// 
//        String request = "";
//        String responseBody = "";
//        try {
//
//            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
//                    addressLine3 = "", city = "", state = "", pincode = "", gender = "", gendercode = "", stateCode = "", mobileNo = "";
//
//            String Query1 = "";
//            String Query2 = "";
//            String paymentHistoryCombined = "";
//            String suitFiledWillfulDefaultWrittenOffStatus = "";
//            String AccountHoldertypeCode = "", AccountHoldertypeCodes = "", WRITEOFF_INP = "", ScoreCardAssetClarification = "";
//            Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            Log.consoleLog(ifr, "Query1==>" + Query1);
//            List< List< String>> Result = ifr.getDataFromDB(Query1);
//            Log.consoleLog(ifr, "#Result===>" + Result.toString());
//            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
//            //"^[A-Za-z ]+$";
//            Log.consoleLog(ifr, "#Result pattern===>" + pattern);
//            if (Result.size() > 0) {
//                customerId = Result.get(0).get(0);
//                firstName = Result.get(0).get(1).replaceAll("[ .]", "");
//                lastName = Result.get(0).get(2).replaceAll("[ .]", "");
//
//                if (lastName.equalsIgnoreCase("{}")) {
//                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
//                }
//                if (lastName.equalsIgnoreCase("{")) {
//                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
//                }
//                if (lastName.equalsIgnoreCase("}")) {
//                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
//                }
//                //Added by shashi to handle for borrower NI case
//                String queryNI = "select ENTITYTYPE from los_nl_basic_info where PID='" + ProcessInstanceId + "' and APPLICANTTYPE='B' ";
//                List<List<String>> ExequeryNI = cf.mExecuteQuery(ifr, queryNI, "AcceleratorActivityManager:ImplLoadPartyGrid -> Execute query for fetching Entitytype::");
//                String entitytype ="";
//                if(ExequeryNI.size()>0) {
//                	  entitytype = ExequeryNI.get(0).get(0);
//                }
//                Log.consoleLog(ifr, "entitytype==>" + entitytype);
//                if(!(applicantType.equalsIgnoreCase("B") && entitytype.equalsIgnoreCase("NI"))) {
//                	if (firstName.length() < 2) {
//                		Log.consoleLog(ifr, "#firstName===>" +firstName);
//                		return RLOS_Constants.ERROR + "Name is less than 1 character ";
//                	} else if (!Pattern.matches(pattern, firstName)) {
//                		return RLOS_Constants.ERROR + "Name contains invalid special characters";
//                	}
//                }
//                panNumber = Result.get(0).get(3);
//                dateofBirth = Result.get(0).get(4);
//                gender = Result.get(0).get(5);
//                mobileNo = Result.get(0).get(6);
//            }
//
//            if (gender.equalsIgnoreCase("Male")) {
//                gendercode = "1";
//            } else if (gender.equalsIgnoreCase("Female")) {
//                gendercode = "2";
//            } else {
//                gendercode = "3";
//            }
//
//            Log.consoleLog(ifr, "firstName===>" + firstName);
//            Log.consoleLog(ifr, "lastName====>" + lastName);
//            Log.consoleLog(ifr, "PAN=========>" + panNumber);
//            Log.consoleLog(ifr, "birthDate===>" + dateofBirth);
//            Log.consoleLog(ifr, "gender======>" + gender);
//
//            Log.consoleLog(ifr, "Query2==>" + Query2);
//            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
//            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
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
//            Log.consoleLog(ifr, "AddressLine1====>" + addressLine1);
//            Log.consoleLog(ifr, "AddressLine2====>" + addressLine2);
//            Log.consoleLog(ifr, "AddressLine3====>" + addressLine3);
//            Log.consoleLog(ifr, "City============>" + city);
//            Log.consoleLog(ifr, "State===========>" + state);
//            Log.consoleLog(ifr, "Pincode=========>" + pincode);
//
//            //Added by Ahmed Alireza on 12-02-2024 for picking statecode from Master
//            String Query3 = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
//                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
//            Log.consoleLog(ifr, "Query3==>" + Query3);
//            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
//            Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
//            if (Result3.size() > 0) {
//                stateCode = Result3.get(0).get(0);
//            }
//
//            if (stateCode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "StateCode founds to be empty for " + state + " in LOS_MST_STATE table.");
//                return RLOS_Constants.ERROR + "StateCode not found";
//            }
//            if (addressLine1.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "addressLine1 not found");
//                return RLOS_Constants.ERROR + "addressLine1 not found";
//            }
//            if (pincode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "pincode not found");
//                return RLOS_Constants.ERROR + "pincode not found";
//            }
//
//            // String mobileNumber = pcm.getMobileNumber(ifr);
//            String DOB="";
//            if(!dateofBirth.equalsIgnoreCase("")) {
//            	DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
//            	DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
//            	Date date = originalFormat.parse(dateofBirth);
//            	DOB = targetFormat.format(date);
//            	Log.consoleLog(ifr, "DOB=======>" + DOB);
//            }
//
//            //Ended by Ahmed Alireza on 12-02-2024 for picking statecode from Master
//            String LoanNo = ProcessInstanceId;
//            String[] DataSplitter = LoanNo.split("-");
//            String FTReferenceNumber = DataSplitter[0] + DataSplitter[1];
//            Log.consoleLog(ifr, "FTReferenceNumber=======>" + FTReferenceNumber);
//
//            String Username = pcm.getConstantValue(ifr, "EXPERIAN", "USERNAME");
//            String Password = pcm.getConstantValue(ifr, "EXPERIAN", "PASSWORD");
//
//            String enquiryReason = pcm.getConstantValue(ifr, "EXPERIAN", "ENQ_PURPOSE");//14
//            String financePurpose = pcm.getConstantValue(ifr, "EXPERIAN", "FI_PURPOSE");//48
//            String durationOfAgreement = pcm.getConstantValue(ifr, "EXPERIAN", "DUROFAGMNT");//180
//            String scoreFlag = pcm.getConstantValue(ifr, "EXPERIAN", "SCOREFLAG");//1
//            String psvFlag = pcm.getConstantValue(ifr, "EXPERIAN", "PSVFLAG");//Y
//            String months = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS");//Y
//            String months_papl = pcm.getConstantValue(ifr, "EXPERIAN", "MONTHS_PAPL");//Y
//          
//            String settledStatus = pcm.getConstantValue(ifr, "EXPERIAN", "SETTLED_STATUS");
//            
//             AccountHoldertypeCode="";
//
//            String wriitenofffStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");
//            String restructureStatus = pcm.getConstantValue(ifr, "EXPERIAN", "WRITTEN-OFF_STATUS");
//
//
//            
//            //String exclusiveAccnts = pcm.getConstantValue(ifr, "EXPERIAN", "ACCTYPES");//1
//            //String accountStatus = pcm.getConstantValue(ifr, "EXPERIAN", "CLOSEDACCTCODES");//Y
//            //Commented.- To discuss with BA`s whethere the below logic applies to journery specific
//            //Commented by Ahmed on 24-06-2024 for taking dynamic prod & sub prod code from master
////            String productCode = "";
////            String subProductCode = "";
////            if (productType.equalsIgnoreCase("PAPL")) {
////                productCode = "PL";
////                subProductCode = "STP-PAPL";
////            } else if (productType.equalsIgnoreCase("CB")) {
////                productCode = "PL";
////                subProductCode = "STP-CB";
////            }
////            
//            String productCode = "HL";
//            Log.consoleLog(ifr, "productCode=======>" + productCode);
//            String assetClassification_No = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_NO");
//            String assetClassification_Yes = pcm.getConstantValue(ifr, "EXPERIAN", "ASSETCLASSIFICATION_NPA_YES");
//            String dpddays = pcm.getConstantValue(ifr, "EXPERIAN", "DPD_DAYS");
//
//
//            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "EMIACCTTYPE");
//            Log.consoleLog(ifr, "excludedEMIAccnts========>" + excludedEMIAccnts);
//            String excludedEMIAccntStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "EMIACCTTYPESTATUS");
//            Log.consoleLog(ifr, "excludedEMIAccntStatus===>" + excludedEMIAccntStatus);
//
//            String excludedNPAAccnts = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "NPAACCTTYPE");
//            Log.consoleLog(ifr, "excludedNPAAccnts========>" + excludedNPAAccnts);
//            String excludedNPAAccntStatus = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "NPAACCTTYPESTATUS");
//            Log.consoleLog(ifr, "excludedNPAAccntStatus===>" + excludedNPAAccntStatus);
//            String excludedDPDAccnts = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "DPDACCTTYPE");
//            Log.consoleLog(ifr, "excludedDPDAccnts========>" + excludedDPDAccnts);
//            String excludedDPDAccntStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "DPDACCTTYPESTATUS");
//            Log.consoleLog(ifr, "excludedDPDAccntStatus===>" + excludedDPDAccntStatus);
//            String excludedWROAccnts = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "WROACCTTYPE");
//            Log.consoleLog(ifr, "excludedWROAccnts========>" + excludedWROAccnts);
//            String excludedWROAccntStatus = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "WROACCTTYPESTATUS");
//            Log.consoleLog(ifr, "excludedWROAccntStatus===>" + excludedWROAccntStatus);
//            String excludedOwners = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "OWNERTYPE");
//            Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);
//
//            String scoreCardAccTypes = pcm.getParamConfig2(ifr, productCode,  "EXPERIANCONF", "SRCACCTTYPE");
//            Log.consoleLog(ifr, "excludedWROAccnts========>" + excludedWROAccnts);
//            String scoreCardAccStatus = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "SRCCCTTYPESTATUS");
//            Log.consoleLog(ifr, "excludedWROAccntStatus===>" + excludedWROAccntStatus);//scoreCardAssetClarification
//            String scoreCardAssetClarification = pcm.getParamConfig2(ifr, productCode, "EXPERIANCONF", "ASSETCLARIFICATION");
//            Log.consoleLog(ifr, "scoreCardAssetClarification===>" + scoreCardAssetClarification);
//
//            //Added by Ahmed for getting AadharNo via AadharVault on 12-07-2024
//            // String mobileNumber = pcm.getMobileNumber(ifr);
//            CustomerAccountSummary cas = new CustomerAccountSummary();
//            HashMap<String, String> customerdetails = new HashMap<>();
//            customerdetails.put("MobileNumber", mobileNo);
//            customerdetails.put("customerId", customerId);//Added by Ahmed on 31-07-2024
//            aadharNo = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
//            if (aadharNo.contains(RLOS_Constants.ERROR)) {
//                return pcm.returnCustomErrorMessage(ifr, aadharNo);
//            }
//            request = "{\n"
//                    + "  \"INProfileRequest\": {\n"
//                    + "    \"Identification\": {\n"
//                    + "      \"XMLUser\": \"" + Username + "\",\n"//Need to configure in INI
//                    + "      \"XMLPassword\": \"" + Password + "\"\n"//Need to configure in INI
//                    + "    },\n"
//                    + "    \"Application\": {\n"
//                    + "      \"FTReferenceNumber\": \"" + FTReferenceNumber + "\",\n"
//                    + "      \"CustomerReferenceID\": \"" + customerId + "\",\n"
//                    + "      \"EnquiryReason\": " + enquiryReason + ",\n"
//                    + "      \"FinancePurpose\": " + financePurpose + ",\n"
//                    + "      \"AmountFinanced\": " + loanAmount + ",\n"
//                    + "      \"DurationOfAgreement\": " + durationOfAgreement + ",\n"
//                    + "      \"ScoreFlag\": " + scoreFlag + ",\n"
//                    + "      \"PSVFlag\": \"" + psvFlag + "\"\n"
//                    + "    },\n"
//                    + "    \"Applicant\": {\n"
//                    + "      \"Surname\": \"" + lastName + "\",\n"
//                    + "      \"FirstName\": \"" + firstName + "\",\n"
//                    + "      \"MiddleName1\": \"\",\n"
//                    + "      \"MiddleName2\": \"\",\n"
//                    + "      \"MiddleName3\": \"\",\n"
//                    + "      \"GenderCode\": \"" + gendercode + "\",\n"
//                    + "      \"IncomeTaxPAN\": \"" + panNumber + "\",\n"
//                    + "      \"PANIssueDate\": \"\",\n"
//                    + "      \"PANExpirationDate\": \"\",\n"
//                    + "      \"PassportNumber\": \"\",\n"
//                    + "      \"PassportIssueDate\": \"\",\n"
//                    + "      \"PassportExpirationDate\": \"\",\n"
//                    + "      \"VoterIdentityCard\": \"\",\n"
//                    + "      \"VoterIDIssueDate\": \"\",\n"
//                    + "      \"VoterIDExpirationDate\": \"\",\n"
//                    + "      \"DriverLicenseNumber\": \"\",\n"
//                    + "      \"DriverLicenseIssueDate\": \"\",\n"
//                    + "      \"DriverLicenseExpirationDate\": \"\",\n"
//                    + "      \"RationCardNumber\": \"\",\n"
//                    + "      \"RationCardIssueDate\": \"\",\n"
//                    + "      \"RationCardExpirationDate\": \"\",\n"
//                    + "      \"UniversalIDNumber\": \"" + aadharNo + "\",\n"
//                    + "      \"UniversalIDIssueDate\": \"\",\n"
//                    + "      \"UniversalIDExpirationDate\": \"\",\n"
//                    + "      \"DateOfBirth\": \"" + DOB + "\",\n"
//                    + "      \"STDPhoneNumber\": \"\",\n"
//                    + "      \"PhoneNumber\": \"\",\n"
//                    + "      \"TelephoneExtension\": \"\",\n"
//                    + "      \"TelephoneType\": 1,\n"
//                    + "      \"MobilePhone\": \"" + mobileNo + "\",\n"
//                    + "      \"EMailId\": \"\"\n"
//                    + "    },\n"
//                    + "    \"Details\": {\n"
//                    + "      \"Income\": \"\",\n"
//                    + "      \"MaritalStatus\": 2,\n"
//                    + "      \"EmployStatus\": \"S\",\n"
//                    + "      \"TimeWithEmploy\": \"\",\n"
//                    + "      \"NumberOfMajorCreditCardHeld\": 0\n"
//                    + "    },\n"
//                    + "    \"Address\": {\n"
//                    + "      \"FlatNoPlotNoHouseNo\": \"" + addressLine1 + "\",\n"
//                    + "      \"BldgNoSocietyName\": \"" + addressLine2 + "\",\n"
//                    + "      \"RoadNoNameAreaLocality\": \"" + addressLine3 + "\",\n"
//                    + "      \"City\": \"" + city + "\",\n"
//                    + "      \"Landmark\": \"\",\n"
//                    + "      \"State\": \"" + stateCode + "\",\n"
//                    + "      \"PinCode\": \"" + pincode + "\"\n"
//                    + "    },\n"
//                    + "    \"AdditionalAddressFlag\": {\n"
//                    + "      \"Flag\": \"Y\"\n"
//                    + "    },\n"
//                    + "    \"AdditionalAddress\": {\n"
//                    + "      \"FlatNoPlotNoHouseNo\": \"" + addressLine1 + "\",\n"
//                    + "      \"BldgNoSocietyName\": \"" + addressLine2 + "\",\n"
//                    + "      \"RoadNoNameAreaLocality\": \"" + addressLine3 + "\",\n"
//                    + "      \"City\": \"" + city + "\",\n"
//                    + "      \"Landmark\": \"\",\n"
//                    + "      \"State\": \"" + stateCode + "\",\n"
//                    + "      \"PinCode\": \"" + pincode + "\"\n"
//                    + "    }\n"
//                    + "  }\n"
//                    + "  \n"
//                    + "}";
//            Log.consoleLog(ifr, "Request Body===============>" + responseBody);
//            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
//            Log.consoleLog(ifr, "Response===============>" + responseBody);
//            Log.consoleLog(ifr, "####  getExperianCIBILScore Response #### " );
//
//            Log.consoleLog(ifr, "EXPERIAN API RESPONSE==>" + responseBody);
//            //Modified by Ahmed on 28-06-2024 for redundancy as well as ApplicantType Hanling
////            cm.CaptureExperianRequestResponse(ifr, ProcessInstanceId, "Experian_API",
////                    request, responseBody, "", "", "");
//
//            //        cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Experian_API", request, responseBody, "", "", "", applicantType);
//            if (!responseBody.equalsIgnoreCase("{}")) {
//                JSONParser parser = new JSONParser();
//                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
//                JSONObject resultObj = new JSONObject(OutputJSON);
//                
//                
//
//                Log.consoleLog(ifr, "resultObj==>" + resultObj);
//
//                String body = resultObj.get("body").toString();
//                Log.consoleLog(ifr, "body==>" + body);
//                JSONObject bodyJSON = (JSONObject) parser.parse(body);
//                JSONObject bodyObj = new JSONObject(bodyJSON);
//
//                String INProfileResponseData = bodyObj.get("INProfileResponse").toString();
//                Log.consoleLog(ifr, "INProfileResponseData==>" + INProfileResponseData);
//
//                JSONObject INProfileResponseDataJSON = (JSONObject) parser.parse(INProfileResponseData);
//                JSONObject INProfileResponseDataJSONObj = new JSONObject(INProfileResponseDataJSON);
//
//                String Score = INProfileResponseDataJSONObj.get("SCORE").toString();
//                Log.consoleLog(ifr, "SCORE===>" + Score);
//
//                //==============Added by Ahmed on 13-05-2024 for CIC Report Generation of Experian===========
//                String cicReportGenReq = pcm.getConstantValue(ifr, "EXPERIAN", "CICREPORTGENREQ");//Added by Ahmed on 02-05-2024
//                Log.consoleLog(ifr, "cicReportGenReq==>" + cicReportGenReq);
//                if (cicReportGenReq.equalsIgnoreCase("Y")) {
//                    String encodedString = Base64.getEncoder().encodeToString(body.getBytes());
//                    Log.consoleLog(ifr, "Encoded string: " + encodedString);
//                    String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Experian", encodedString, "NGREPORTTOOL_EXPERIAN");
//                    Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
//                    cm.updateCICReportStatus(ifr, "Experian", generateReportStatus, applicantType);//Added by Ahmed on 26-07-2024 for Status Updation of CIC Report
//                }
//                //==============Added by Ahmed on 13-05-2024 for CIC Report Generation of Experian===========
//
//                JSONObject ScoreDataJSON = (JSONObject) parser.parse(Score);
//                JSONObject ScoreDataJSONObj = new JSONObject(ScoreDataJSON);
//
//                String BureauScore = ScoreDataJSONObj.get("BureauScore").toString();
//                String type = "";
//                Log.consoleLog(ifr, "BureauScore===>" + BureauScore);
//                //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
//                if ((BureauScore.equalsIgnoreCase("000-1"))) {
//                    String cbilimmuneAccept = ConfProperty.getCommonPropertyValue("CICImmune_Accept");
//                    Log.consoleLog(ifr, "cbilimmuneAccept==>" + cbilimmuneAccept);
//
//                    if (cbilimmuneAccept.equalsIgnoreCase("YES")) {
//                        BureauScore = "-1";
//                    } else {
//                        BureauScore = "0";
//                    }
//
//                }
//               
//              
//                String overAllAccountTypes = "";
//                String overAllAccountStatus = "";
//                String overAllOpendate = "";
//                String overAllSuitFiledWillfulDefaultWrittenOffStatus = "";
//                int totalNonEMICount = 0;
//                String NPA = "No";
//                String dpd = "0";
//                String writeOFF = "No";
//                String consolidated_emiAmnt = "";
//                String totaldaysPastDue = "";
//                double totalSum = 0;
//                double finalscheduledMonthlyPaymentAmount = 0;
//                String Outstanding_Balance_All = "";
//                String Payment_History_Profile_Data = "";
//                ArrayList<String> emiAmountTagValues = new ArrayList<>();
//                String consolidatedtwelveMonthData = "";
//                String scoreCardAccType = "";
//                String scoreCardAccStatu = "";
//                String overALlAsset_Classification = "";
//
//                String settledHistory = "No";
//                String NPA_INP = "No";
//                String GUARANTORNPA_INP = "No";
//                String GUARANTORWRITEOFFSETTLEDHIST_INP = "No";
//                String paymentHistoryClob = "000";
//
//                if (!(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account").equalsIgnoreCase(""))) {
//
//                    String query = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
//                    List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
//                    String loan_selected = loanSelected.get(0).get(0);
//                    Log.consoleLog(ifr, "loan type==>%%" + loan_selected);
//                    JSONObject CAIS_Account = (JSONObject) parser.parse(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account"));
//
//                    JSONObject CAIS_SummaryJSON = (JSONObject) parser.parse(cf.getJsonValue(CAIS_Account, "CAIS_Summary"));
//                    Log.consoleLog(ifr, "CAIS_Summary===>  " + CAIS_SummaryJSON);
//                    Log.consoleLog(ifr, "CAIS_Summary===>  " + CAIS_SummaryJSON);
//                    JSONObject CAIS_SummaryJSONJSONObj = new JSONObject(CAIS_SummaryJSON);
//                    JSONObject Total_Outstanding_BalanceJSON = (JSONObject) parser.parse(cf.getJsonValue(CAIS_SummaryJSONJSONObj, "Total_Outstanding_Balance"));
//                    Log.consoleLog(ifr, "Total_Outstanding_BalanceJSON===>  " + Total_Outstanding_BalanceJSON);
//                    JSONObject Total_Outstanding_BalanceJSONObj = new JSONObject(Total_Outstanding_BalanceJSON);
//                    Outstanding_Balance_All = Total_Outstanding_BalanceJSONObj.get("Outstanding_Balance_All").toString();
//                    Log.consoleLog(ifr, "Outstanding_Balance_All===>  " + Outstanding_Balance_All);
//                    JSONArray CAIS_Account_DETAILS = new JSONArray();
//
//                    //Vandana Code variable
//                    //Modified by Ahmed on 06-09-2024 for MoneshKumar`s code
//                    //Modified by Monesh  on 26-09-2024 for 
//                    String accountDetails = cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").toString();
//                    if ((!accountDetails.equalsIgnoreCase(""))
//                            && (Integer.parseInt(BureauScore) > 10)) {
//                        //if (!(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").equalsIgnoreCase(""))) {
//                        //  String CAIS_Accountdetails=cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS");
//                        Object CAISAccountdetails = CAIS_Account.get("CAIS_Account_DETAILS");
//                        Log.consoleLog(ifr, "CAIS_AccountS_DETAILS " + cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
//                        if (CAISAccountdetails instanceof JSONArray) {
//                            CAIS_Account_DETAILS = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
//                            Log.consoleLog(ifr, "Test " + CAIS_Account_DETAILS.toString());
//                        } else {
//
//                            CAIS_Account_DETAILS.add(CAISAccountdetails);
//                            Log.consoleLog(ifr, "Test add" + CAIS_Account_DETAILS.toString());
//                        }
//                        Log.consoleLog(ifr, "CAIS_Account_DETAILS " + Outstanding_Balance_All);
//
//                        for (int i = 0; i < CAIS_Account_DETAILS.size(); i++) {
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
//                            JSONObject CAIS_Account_DETAILSObj = (JSONObject) CAIS_Account_DETAILS.get(i);
//                            String Payment_History_Profile = cf.getJsonValue(CAIS_Account_DETAILSObj, "Payment_History_Profile");
//                            Log.consoleLog(ifr, "Payment_History_Profile" + Payment_History_Profile);
//                            // added by vandana start
//
//                            Log.consoleLog(ifr, "Payment_History_Profile_Data" + Payment_History_Profile);
//                            String payment = Payment_History_Profile.replace("?", "0")
//                                    .replace("S", "0").replace("N", "0")
//                                    .replace("B", "0").replace("D", "0")
//                                    .replace("M", "0").replace("L", "0");
//                            Log.consoleLog(ifr, "Payment_History_Profile" + payment);
//
//                            //-----------------
//                            if (payment.length() < 3) {
//                                Log.consoleLog(ifr, "Insufficient data to extract payment history.");
//                                //return "";//Modified by Ahmed on 18-07-2024
//                            } else {
//                                // Calculate the maximum number of complete months that can be extracted
//                                int numberOfMonths = payment.length() / 3;
//                                Log.consoleLog(ifr, "Payment_History_Profile numberOfMonths" + numberOfMonths);
//                                // Extract payment status for each complete month
//                                StringBuilder sb = new StringBuilder();
//
//                                for (int j = 0; j < numberOfMonths; j++) {
//                                    // Each month's status is represented by 3 characters
//                                    String twelveMonthData = payment.substring(j * 3, (j * 3) + 3);
//                                    // sb.append("Month ").append(j + 1).append(": ").append(Payment_History_Profile_Data).append("\n");
//                                    // consolidatedtwelveMonthData = consolidatedtwelveMonthData + twelveMonthData;
//                                } // end
//
//                            }
//
//                            // Log.consoleLog(ifr, "consolidatedtwelveMonthData" + consolidatedtwelveMonthData);
//                            paymentHistoryCombined = Payment_History_Profile_Data + payment;
//                            Log.consoleLog(ifr, "paymentHistoryCombined" + Payment_History_Profile_Data);
//                            String accountType = cf.getJsonValue(CAIS_Account_DETAILSObj, "Account_Type");
//                            String accountStatus = cf.getJsonValue(CAIS_Account_DETAILSObj, "Account_Status");
//                            String openDate = cf.getJsonValue(CAIS_Account_DETAILSObj, "Open_Date");
//                            String closedDate = cf.getJsonValue(CAIS_Account_DETAILSObj, "Date_Closed");//Added by Ahmed on 16-07-2024
//                            overAllAccountTypes = overAllAccountTypes + accountType;
//                            overAllAccountStatus = overAllAccountStatus + accountStatus;
//                            String ownerType = cf.getJsonValue(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");//Added by Ahmed on 26-09-2024
//
//                            overAllOpendate = overAllOpendate + openDate;
//                            Log.consoleLog(ifr, "overAllOpendate" + overAllOpendate);//SuitFiledWillfulDefaultWrittenOffStatus
//                            String SuitFiledWillfulDefaultWrittenOffStatus = cf.getJsonValue(CAIS_Account_DETAILSObj, "SuitFiledWillfulDefaultWrittenOffStatus");
//                            overAllSuitFiledWillfulDefaultWrittenOffStatus = overAllSuitFiledWillfulDefaultWrittenOffStatus + SuitFiledWillfulDefaultWrittenOffStatus;
//                            if (overAllSuitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("00")) {
//                                suitFiledWillfulDefaultWrittenOffStatus = "No";
//                            } else {
//                                suitFiledWillfulDefaultWrittenOffStatus = "Yes";
//                            }
//                            Log.consoleLog(ifr, "suitFiledWillfulDefaultWrittenOffStatus==>" + suitFiledWillfulDefaultWrittenOffStatus);
//                            // vandana
//                            Log.consoleLog(ifr, "suitFiledWillfulDefaultWrittenOffStatus==>####" + cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
//
//                            if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History").equalsIgnoreCase(""))) {
//                                JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
//                                for (int j = 0; j < CAIS_Account_History.size(); j++) {
//                                    Log.consoleLog(ifr, "ExperianAPI==>::expDate");
//                                    JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
//                                    String Asset_Classification = cf.getJsonValue(CAIS_Account_HistoryObj, "Asset_Classification");
//                                    overALlAsset_Classification = Asset_Classification + Asset_Classification;
//                                    String daysPastDue = (String) CAIS_Account_HistoryObj.get("Days_Past_Due");
//                                    Log.consoleLog(ifr, "CAIS_Account_HistoryObj*****)" + CAIS_Account_HistoryObj.get("Year").toString());
//
//                                    if (CAIS_Account_HistoryObj.get("Year") != "") {
//
//                                        int expYear = Integer.parseInt(CAIS_Account_HistoryObj.get("Year").toString());
//
//                                        int expMonth = Integer.parseInt(CAIS_Account_HistoryObj.get("Month").toString()); // if Month is String, otherwise cast to Integer
//
//                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate");
//                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expYear);
//                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expMonth);
//
//                                        String expDateStr = String.format("01/%02d/%d", expMonth, expYear);
//                                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
//                                        Date expDate = dateFormatter.parse(expDateStr);
//
//                                        Log.consoleLog(ifr, "ExperianAPI==>::expDate" + expDate);
//                                        // Starts Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK                                       
//                                        int totalmonths = 0;
//                                        if (loan_selected.equalsIgnoreCase("Canara Pension")
//                                                || loan_selected.equalsIgnoreCase("Canara Budget")
//                                                || loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
//                                            totalmonths = Integer.parseInt(months);
//                                            daysPastDue = validatePaymentHistory_EX(ifr, expDate, daysPastDue, totalmonths);
//
//                                            totaldaysPastDue = totaldaysPastDue + daysPastDue;
//
//                                        } else {
//                                            totalmonths = Integer.parseInt(months_papl);
//                                            daysPastDue = validatePaymentHistory_EX(ifr, expDate, daysPastDue, totalmonths);
//
//                                            totaldaysPastDue = totaldaysPastDue + daysPastDue;
//
//                                        }
//
//                                    }
//                                    Log.consoleLog(ifr, "ExperianAPI==>::expDate" + totaldaysPastDue);
//                                    // End
//
//                                }
//                                if (!overALlAsset_Classification.equalsIgnoreCase("")) {
//                                    overALlAsset_Classification = overALlAsset_Classification + ",";//overALLdateOpened
//                                }
//                            }
//                            Log.consoleLog(ifr, "overALlAsset_Classification==>" + overALlAsset_Classification);
//                            boolean npaStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedNPAAccnts, accountStatus, excludedNPAAccntStatus, closedDate);
//                            Log.consoleLog(ifr, "npaStatus==>" + npaStatus);
//
//                            boolean dpdStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedDPDAccnts, accountStatus, excludedDPDAccntStatus, closedDate);
//                            Log.consoleLog(ifr, "dpdStatus==>" + dpdStatus);
//
//                            boolean wroStatus = checkKnockOffAccFilterStatus_WRO(ifr, accountType, excludedWROAccnts, accountStatus, excludedWROAccntStatus, closedDate);
//                            Log.consoleLog(ifr, "wroStatus==>" + wroStatus);
//
//                           
////Modified by AHmed on 26-09-2024 for Ownership Indicator Filtering
//                            boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
//                                    excludedEMIAccnts, accountStatus,
//                                    excludedEMIAccntStatus, closedDate, ownerType, excludedOwners);
//                            Log.consoleLog(ifr, "emiStatus==>@@@" + emiStatus);
//
//                            if (dpdStatus) {
//                            	try
//                            	{
//                                if (payment.length() > 6) {
//                                    payment = payment.substring(0, 6);
//                                }
//                                Log.consoleLog(ifr, payment);
//                                int k = 0;
//                                for (int l = 0; l < payment.length(); l++) {
//                                    int val = Integer.parseInt(payment.substring(k, l + 1));
//                                    Log.consoleLog(ifr, "@@@@@payment"+payment);
//                                    k++;
//                                    if (val > 0) {
//                                        dpd = String.valueOf(val);
//                                        Log.consoleLog(ifr, "@@@@@found");
//                                        break;
//                                    }
//                                }
//                                
//                            }catch(Exception e)
//                            {
//                            	Log.consoleLog(ifr, "@@@@@ dpdStatus Exception"+e);
//                            }  
//                                
//                                
//                                
//                                
//                            } else {
//                                Log.consoleLog(ifr, "DPD Account Type Excluded. Please check config master. Looping next..");
//                            }
//
//                            if (wroStatus) {
//                            	
//                            	try
//                            	{
//                                    Log.consoleLog(ifr, "inside wroStatus .."+wroStatus);
//                                    Log.consoleLog(ifr, "inside wroStatus .. CAIS_Account_DETAILSObj"+CAIS_Account_DETAILSObj);
//
//                            	String written_off_Settled_Status=CAIS_Account_DETAILSObj.get("Written_off_Settled_Status").toString();
//                            	String accountHoldertype= cf.getJsonValue
//                            			(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");;
//                            	 
//                            	 
//                            	 
//                         // String ownerType = cf.getJsonValue(CAIS_Account_DETAILSObj, "AccountHoldertypeCode");
//                            	// written off  settled status
//                            	if(settledStatus.contains(written_off_Settled_Status))
//                            	{
//                            		 Log.consoleLog(ifr, "@@@@inside settledStatus .."+settledStatus);
//                            		if (!(cf.getJsonValue(CAIS_Account_DETAILSObj,
//                                   		   "CAIS_Account_History").equalsIgnoreCase(""))) {
//                                           JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
//                                           for (int j = 0; j < CAIS_Account_History.size(); j++) {
//                                               JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
//                                               
//                                               String Year = cf.getJsonValue(CAIS_Account_HistoryObj, "Year");
//                                               String Month = cf.getJsonValue(CAIS_Account_HistoryObj, "Month");
//                                               String dayPastDue = cf.getJsonValue(CAIS_Account_HistoryObj, "Days_Past_Due");
//                                         
//                            		
//                            		}
//                                       } 
//                            	}
//                            	
//                            	}catch(Exception e)
//                            	{
//                        Log.consoleLog(ifr, "Exception wroStatus.."+e);
//
//                            	}
//                            	
//                            	 
//                            	
//                                 
//                            	
//                            }
//
//                            if (npaStatus) {
//                            	
//                            	try
//                            	{
//                            	
//                                if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History")
//                                		.equalsIgnoreCase(""))) {
//                                    JSONArray CAIS_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "CAIS_Account_History"));
//                                    for (int j = 0; j < CAIS_Account_History.size(); j++) {
//                                        JSONObject CAIS_Account_HistoryObj = (JSONObject) CAIS_Account_History.get(j);
//                                       
//                                    }
//                                }
//                                
//                            }catch(Exception e)
//                            {
//                            	 Log.consoleLog(ifr, " Exception NPA::::: nparesult====>***"+e); 
//                            }
//
//                            if (emiStatus) {
//                                if (!(cf.getJsonValue(CAIS_Account_DETAILSObj, "Advanced_Account_History").equalsIgnoreCase(""))) {
//                                    JSONArray Advanced_Account_History = (JSONArray) parser.parse(cf.getJsonValue(CAIS_Account_DETAILSObj, "Advanced_Account_History"));
//
//                                    int count = 0;
//                                    for (int j = 0; j < Advanced_Account_History.size(); j++) {
//                                        Log.consoleLog(ifr, "count/OutsideideEMIValueNULL Loop===>" + count);
//
//                                        JSONObject Advanced_Account_HistoryObj = (JSONObject) Advanced_Account_History.get(j);
//
//                                        //Modified by Ahmed on 03-06-2024 for performing Total Non EMI cals
//                                        if (!cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount").equalsIgnoreCase("")) {
//                                            Log.consoleLog(ifr, "EMI_Amount tag available");
//                                            String EMI_Amount = cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount");
//                                            Log.consoleLog(ifr, "EMI_Amount===>" + EMI_Amount);
//
//                                            
//                                            if (count == 0) {
//                                                emiAmountTagValues.add(EMI_Amount);
//
//                                                if (EMI_Amount.equalsIgnoreCase("")) {
//                                                    Log.consoleLog(ifr, "EMI_Amount tag available but empty");
//                                                    String currentBalance = cf.getJsonValue(Advanced_Account_HistoryObj, "Current_Balance");
//                                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//                                                    if (currentBalance.equalsIgnoreCase("")) {
//                                                        currentBalance = "0";
//                                                    }
//
//                                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//                                                    if (Integer.parseInt(currentBalance) > 0) {
//                                                        totalNonEMICount++;
//                                                    }
//
//                                                    Log.consoleLog(ifr, "totalNonEMICount=>" + totalNonEMICount);
//                                                }
//
//                                                count++;
//                                            }
//                                        } else {
//                                            Log.consoleLog(ifr, "EMI_Amount tag not available");
//
//                                            if (count == 0) {
//                                                String currentBalance = cf.getJsonValue(Advanced_Account_HistoryObj, "Current_Balance");
//                                                Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//                                                if (currentBalance.equalsIgnoreCase("")) {
//                                                    currentBalance = "0";
//                                                }
//
//                                                Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//                                                if (Integer.parseInt(currentBalance) > 0) {
//                                                    totalNonEMICount++;
//                                                }
//
//                                                Log.consoleLog(ifr, "totalNonEMICount=>" + totalNonEMICount);
//                                                count++;
//                                            }
//
//                                        }
//
//                                        //Commented by Ahmed on 03-06-2024 for performing Total Non EMI cals
////                                        String EMI_Amount = cf.getJsonValue(Advanced_Account_HistoryObj, "EMI_Amount");
////                                        Log.consoleLog(ifr, "EMI_Amount===>" + EMI_Amount);
////                                        Log.consoleLog(ifr, "consolidated_emiAmnt  " + consolidated_emiAmnt);
////                                        if ((!EMI_Amount.equalsIgnoreCase(""))
////                                                && (EMI_Amount != null)) {
////
////                                            Log.consoleLog(ifr, "count/InsideEMIValueNULL Loop===>" + count);
////                                            if (count == 0) {
////                                                emiAmountTagValues.add(EMI_Amount);
////                                                count++;
////                                            }
////                                        }
//                                    }
//                                }
//                            }
//
//                        }
//
//                        //Vandana code restructured here..
//                        Log.consoleLog(ifr, "overAllAccountTypes  " + overAllAccountTypes);
//                        Log.consoleLog(ifr, "overAllAccountStatus  " + overAllAccountStatus);
//                        String scoreCardstatus = checScoreCardAccountTypeStatus(ifr, overAllAccountTypes, scoreCardAccTypes,
//                                overAllAccountStatus, scoreCardAccStatus, overALlAsset_Classification, scoreCardAssetClarification);
//                        Log.consoleLog(ifr, "scoreCardstatus  " + scoreCardstatus);
//                        String[] statuses = scoreCardstatus.split("-");
//
//                        // Ensure that we have exactly two parts
//                        if (statuses.length == 3) {
//                            scoreCardAccType = statuses[0].trim(); // Trim to remove any leading/trailing whitespace
//                            scoreCardAccStatu = statuses[1].trim();
//                            ScoreCardAssetClarification = statuses[2].trim();
//
//                            // Now you can use yesStatus and noStatus as needed
//                            Log.consoleLog(ifr, "scoreCardAccType  " + scoreCardAccType);
//                            Log.consoleLog(ifr, "scoreCardAccStatu  " + scoreCardAccStatu);
//                            Log.consoleLog(ifr, "ScoreCardAssetClarification  " + ScoreCardAssetClarification);
//                        } else {
//                            // Handle unexpected format if needed
//                            System.out.println("Unexpected format: " + scoreCardstatus);
//                        }
//                        Log.consoleLog(ifr, "overAllOpendate" + overAllOpendate);//CheckPaymenyHistory
//                        String DateOpen = checScoreCardDateOpend(ifr, overAllOpendate);
//                        Log.consoleLog(ifr, "DateOpen  " + DateOpen);
//
//                        paymentHistoryClob = cm.getClobSnippetData(ifr, totaldaysPastDue);
//                        // ADDED BY VANDANA
//                        if (overAllAccountTypes.equalsIgnoreCase("1")
//                                || overAllAccountTypes.equalsIgnoreCase("4")) {
//                            AccountHoldertypeCode = "Yes";
//                        } else {
//                            AccountHoldertypeCode = "No";
//                        }
//
//                        if (scoreCardAccStatu.equalsIgnoreCase("Yes") & AccountHoldertypeCode.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & suitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("Yes")) {
//                            settledHistory = "Yes";
//                        }
//                        Log.consoleLog(ifr, "EXPERIANsettledHistory" + settledHistory);
//
//                        if (ScoreCardAssetClarification.equalsIgnoreCase("Yes") & AccountHoldertypeCode.equalsIgnoreCase("Yes")) {
//                            NPA_INP = "Yes";
//                        }
//                        if (ScoreCardAssetClarification.equalsIgnoreCase("Yes") & AccountHoldertypeCodes.equalsIgnoreCase("Yes")) {
//                            GUARANTORNPA_INP = "Yes";
//                        }
//
//                        if (scoreCardAccStatu.equalsIgnoreCase("Yes") & AccountHoldertypeCodes.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & suitFiledWillfulDefaultWrittenOffStatus.equalsIgnoreCase("Yes")) {
//                            GUARANTORWRITEOFFSETTLEDHIST_INP = "Yes";
//                        }
//
//                        //----
//                    }
//
//                } else {
//                    consolidated_emiAmnt = "0";
//                    Outstanding_Balance_All = "No data Found";
//
//                }
//
//                Log.consoleLog(ifr, "emiAmountTagValues  " + emiAmountTagValues.toString());
//                Log.consoleLog(ifr, "emiAmountTagValues  " + emiAmountTagValues.size());
//
//                if (!emiAmountTagValues.isEmpty()) {
//                    HashSet<String> hashSet = new HashSet<>(emiAmountTagValues);
//                    ArrayList<String> distinctArrayList = new ArrayList<>(hashSet);
//
//                    Log.consoleLog(ifr, "Distinct elements==>" + distinctArrayList.toString());
//                    for (String element : distinctArrayList) {
//                        Log.consoleLog(ifr, "Distinct EMI Value from a Single Block==>" + element);
//                        if ((!element.equalsIgnoreCase(""))
//                                && (element != null)) {
//                            double scheduledMonthlyPaymentAmount = Double.parseDouble(element);
//                            totalSum = scheduledMonthlyPaymentAmount;
//                            double roundedValue = Math.round(scheduledMonthlyPaymentAmount * 100.0) / 100.0;
//                            finalscheduledMonthlyPaymentAmount += roundedValue;
//                        }
//                    }
//                }
//                String formattedValue = String.format(Locale.US, "%.2f",
//                        finalscheduledMonthlyPaymentAmount);
//                consolidated_emiAmnt = formattedValue;
//
//                Log.consoleLog(ifr, "@@@@EMI Consolidated Amount==>" + finalscheduledMonthlyPaymentAmount);
//
//                Log.consoleLog(ifr, "settledHistory:" + settledHistory);
//                Log.consoleLog(ifr, "@@@@@NPA_INP:" + NPA_INP);
//                Log.consoleLog(ifr, "GUARANTORNPA_INP:" + GUARANTORNPA_INP);
//                Log.consoleLog(ifr, "####GUARANTORWRITEOFFSETTLEDHIST_INP: budget Pension Vehicle " + GUARANTORWRITEOFFSETTLEDHIST_INP);
//                Log.consoleLog(ifr, "#####GUARANTORWRITEOFFSETTLEDHIST_INP: nparesult nparesult nparesult " );
//
////               
//                String lapExists = "No";//Need to derive this data
//                String newToCredit = "No";//Need to derive this data
//
//                String qryCICDataUpdate1 = "",lessThan5year = "No",npaMoreThan5year = "No",qryCICDataUpdate2 = "";
//                
//                String restructured="",writteOff="",settled="",wrmoreThan5year="",accountHolder="";
//                
//                
//               
//                
////                    if ((ProductType.equalsIgnoreCase("PAPL"))
////                            || (ProductType.equalsIgnoreCase("LAD"))) {
//                qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,"
//                		+ "APPLICANT_UID,RESTRUCTURED,SETTLED,NPAISFIVEYEARS,WRISFIVEYEARS) "
//                        + "values('" + ProcessInstanceId + "','" + BureauScore + "',"
//                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "','EX',"
//                        + "'" + consolidated_emiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEMICount + "','" + settledHistory + "',"
//                        + "'" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "',SYSDATE,"
//                        + "'"+insertionOrderid+"','" + restructured + "','" + settled + "','" + npaMoreThan5year 
//                        + "','" + wrmoreThan5year + "')";
//                Log.consoleLog(ifr, "Insert query: LOS_TRN_CREDITHISTORY" + qryCICDataUpdate1);
//                ifr.saveDataInDB(qryCICDataUpdate1);
//                // } else {
////                qryCICDataUpdate2 = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
////                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
////                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
////                        + "CIC_SCORE,NPA_STATUS,DPD,WRITEOFF_STATUS,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
////                        + "VALUES('" + ProcessInstanceId + "',"
////                        + "'','" + mobileNo + "','" + productCode + "','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','"+insertionOrderid+"',\n"
////                        + "'EXT','EX','CNS','" + lapExists + "','" + BureauScore + "',"
////                        + "'" + NPA + "','" + dpd + "','" + writeOFF + "','" + consolidated_emiAmnt + "','" + newToCredit + "',SYSDATE,SYSDATE)";
//                // }
//                
//                Log.consoleLog(ifr, "qryCICDataUpdate2: EXPERIAN" + qryCICDataUpdate2);
//                ifr.saveDataInDB(qryCICDataUpdate2);
//
//                    Log.consoleLog(ifr, "productType==>PAPL");
//                    String ACC_SUMMARY_Update = "UPDATE LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                            + " SET "
//                            + " EXP_CBSCORE = '" + BureauScore + "',  "
//                            + " MONTHLYDEDUCTION = '" + consolidated_emiAmnt + "',  "
//                            + " TOTALOUTSTANDINGLIABILITY = '" + Outstanding_Balance_All + "'  "
//                            + " WHERE "
//                            + " WINAME = '" + ProcessInstanceId + "' ";
//
//                    Log.consoleLog(ifr, "Update Query in ACC_SUMMARY_Update" + ACC_SUMMARY_Update);
//                    ifr.saveDataInDB(ACC_SUMMARY_Update);
////                if (!productCode.equalsIgnoreCase("MF")) {
////                    Log.consoleLog(ifr, "#### Started CRG Validation#### ");
////                    crg.crgGenExperian(ifr, resultObj, ProcessInstanceId, apiName, applicantType, type,insertionOrderid);
////                    Log.consoleLog(ifr, "#### Ended  CRG Validation #### ");
////                }
//
//                return BureauScore;
//            } else {
//            	
//            	type="NoCIC";
//            	   Log.consoleLog(ifr, "####Inside ELse ####  query: NOCIC" + type);
//            	consolidated_emiAmnt="0";
//            	 String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,"
//                 		+ "APPLICANT_UID,RESTRUCTURED,SETTLED,NPAISFIVEYEARS,WRISFIVEYEARS) "
//                         + "values('" + ProcessInstanceId + "','" + BureauScore + "',"
//                         + "'" + NPA + "','" + dpd + "','" + writeOFF + "','EX',"
//                         + "'" + consolidated_emiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEMICount + "','" + settledHistory + "',"
//                         + "'" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "',SYSDATE,"
//                         + "'"+insertionOrderid+"','" + "" + "','" + "" + "','" + "" 
//                         + "','" + "" + "')";
//                 Log.consoleLog(ifr, "####Insert query:#### NOCIC" + qryCICDataUpdate1);
//                 ifr.saveDataInDB(qryCICDataUpdate1);
////                 if (!productCode.equalsIgnoreCase("MF")) {
////                    Log.consoleLog(ifr, "####Started CRG Validation #### " );
////                    crg.crgGenExperian(ifr,resultObj,ProcessInstanceId,apiName,applicantType,type,insertionOrderid);
////                    Log.consoleLog(ifr, "####Ended#####  CRG Validation " );
////                 }
//                 return BureauScore;
//            }
//                
//                
//                
//                
//              
//               
//                
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Experian Exception===>  " + e);
//            Log.errorLog(ifr, "Experian Exception===>  " + e);
//        } finally {
//            /*cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
//                    apiErrorCode, apiErrorMessage, apiStatus);*/
//            if (!request.isEmpty()) {
//                cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Experian_API", request, responseBody, "", "", "", (!insertionOrderid.equalsIgnoreCase("") ? insertionOrderid : applicantType));
//            }
//
//        }
//        return RLOS_Constants.ERROR;
//    }

    public String validatePaymentHistory_EX(
            IFormReference ifr, Date expDate, String daysPastDue, int totalmonths) {
        try {

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date(); // Current date
            Log.consoleLog(ifr, "ExperianAPI==>::currentDate: " + dateFormatter.format(currentDate));

            Log.consoleLog(ifr, "ExperianAPI==>::expDate: " + dateFormatter.format(expDate));

            Calendar expCalendar = Calendar.getInstance();
            expCalendar.setTime(expDate);

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);

            int yearDifference = currentCalendar.get(Calendar.YEAR) - expCalendar.get(Calendar.YEAR);
            int monthDifference = currentCalendar.get(Calendar.MONTH) - expCalendar.get(Calendar.MONTH);

            int totalMonthDifference = (yearDifference * 12) + monthDifference;

            Log.consoleLog(ifr, "ExperianAPI==>::Month Difference: " + totalMonthDifference);

            Log.consoleLog(ifr, "ExperianAPI==>::daysPastDue: " + daysPastDue);

            if (totalMonthDifference <= totalmonths) {

                if (daysPastDue.equalsIgnoreCase("")) {
                    daysPastDue = "0";

                }
                daysPastDue = String.format("%03d", Integer.parseInt(daysPastDue));

            }

        } catch (Exception e) {
            //e.printStackTrace();
            Log.consoleLog(ifr, "Error in calculating date difference.");
            // daysPastDue = "Error";
        }
        Log.consoleLog(ifr, "ExperianAPI==>::daysPastDue: " + daysPastDue);

        return daysPastDue;
    }

	private boolean checkEMIKnockOffAccFilterStatus(IFormReference ifr, String accountType,
        String excludedAccnts,
        String accountStatus, String excludedAccStatus,
        String dateClosed, String ownerType, String excludedOwners) {

    Log.consoleLog(ifr, "#checkEMIKnockOffAccFilterStatus started...");
    Log.consoleLog(ifr, "accountType=====>" + accountType);
    Log.consoleLog(ifr, "accountStatus===>" + accountStatus);
    Log.consoleLog(ifr, "dateClosed===>" + dateClosed);
    Log.consoleLog(ifr, "ownerType===>" + ownerType);
    Log.consoleLog(ifr, "excludedOwners===>" + excludedOwners);

    String[] excludedOwnerTypes = excludedOwners.split(",");
    for (String typeOfOwner : excludedOwnerTypes) {
        if (typeOfOwner.equals(ownerType)) {
            Log.consoleLog(ifr, "excludedOwnerTypes===> " + typeOfOwner);
            return false;
        }
    }

    if (!dateClosed.equalsIgnoreCase("")) {
        return false;
    }

    String[] excludedAccounts = excludedAccnts.split(",");
    String[] excludedAccStatusArray = excludedAccStatus.split(",");

    for (String accnt : excludedAccounts) {
        if (accnt.equals(accountType)) {
            Log.consoleLog(ifr, "excludedAccounts===>" + accnt);
            return false;
        }
    }
    for (String accntStatus : excludedAccStatusArray) {
        if (accntStatus.equals(accountStatus)) {
            Log.consoleLog(ifr, "excludedAccStatusArray===>" + accntStatus);
            return false;
        }
    }
    Log.consoleLog(ifr, "Filter Condition not satisfied..");
    return true;

}

	private boolean checkKnockOffAccFilterStatus(IFormReference ifr, String accountType,
            String excludedAccnts,
            String accountStatus, String excludedAccStatus, String dateClosed) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "accountStatus===>" + accountStatus);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);

        String[] excludedAccounts = excludedAccnts.split(",");
        String[] excludedAccStatusArray = excludedAccStatus.split(",");

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "excludedAccounts===>" + accnt);
                return false;
            }
        }
        for (String accntStatus : excludedAccStatusArray) {
            if (accntStatus.equals(accountStatus)) {
                Log.consoleLog(ifr, "excludedAccStatusArray===>" + accntStatus);
                return false;
            }
        }

        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "yyyyMMdd";

            boolean isValidDateFormat = isValidDateFormat(dateClosed, dateFormat);
            Log.consoleLog(ifr, "IsValidDateFormat" + isValidDateFormat);

            if (isValidDateFormat) {
                boolean isWithinSpecifiedMonths = isDateWithinSpecifiedMonths(dateClosed, 6);
                if (isWithinSpecifiedMonths) {
                    Log.consoleLog(ifr, "The date " + dateClosed + " is within the last 6 months from the current date.");
                } else {
                    Log.consoleLog(ifr, "The date " + dateClosed + " is not within the last 6 months from the current date.");
                    return false;
                }
            }
        }

        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }

    private String checScoreCardAccountTypeStatus(IFormReference ifr,
            String accountType, String srcaccountType,
            String accountStatus, String srcAccStatus, String assetclassification, String srcassetclassification) {

        int brmsAccountTypeCount = 0;
        int brmsAccountTypeStatusCount = 0;
        int brmsassetclassificationCount = 0;

        String brmsAccountType = "No";
        String brmsAccountTypeStatus = "No";
        String brmsAssetclassification = "No";

        Log.consoleLog(ifr, "#checScoreCardAccountTypeStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "accountStatus===>" + accountStatus);
        Log.consoleLog(ifr, "brmsAssetclassification===>" + brmsAssetclassification);
        String[] includedAccounts = srcaccountType.split(",");
        String[] includedAccStatusArray = srcAccStatus.split(",");
        String[] includedassetclassification = srcassetclassification.split(",");

        String[] experianAccounts = accountType.split(",");
        String[] experianAccStatus = accountStatus.split(",");
        String[] experianassetclassification = assetclassification.split(",");

        for (String expAccnt : experianAccounts) {
            for (String scoreCardAccnt : includedAccounts) {
                if (scoreCardAccnt.equals(expAccnt)) {
                    brmsAccountTypeCount++;
                }
            }
        }

        for (String experianAssetclassification : experianassetclassification) {
            for (String scoreCardAssetclassification : includedassetclassification) {
                if (scoreCardAssetclassification.equals(experianAssetclassification)) {
                    brmsAccountTypeStatusCount++;
                }
            }
        }

        for (String expAccntStatus : experianAccStatus) {
            for (String scoreCardAccntStatus : includedAccStatusArray) {
                if (scoreCardAccntStatus.equals(expAccntStatus)) {
                    brmsassetclassificationCount++;
                }
            }
        }

        if (brmsAccountTypeCount > 0) {
            brmsAccountType = "Yes";
        }

        if (brmsAccountTypeStatusCount > 0) {
            brmsAccountTypeStatus = "Yes";
        }

        if (brmsassetclassificationCount > 0) {
            brmsAssetclassification = "Yes";
        }

        Log.consoleLog(ifr, "brmsAccountType=========>" + brmsAccountType);
        Log.consoleLog(ifr, "brmsAccountTypeStatus===>" + brmsAccountTypeStatus);

        return brmsAccountType + "-" + brmsAccountTypeStatus + "-" + brmsAssetclassification;

    }

//    private String checScoreCardDateOpend(IFormReference ifr,
//            String dateopen) {
//        LocalDate currentDate = LocalDate.now();
//        String dateOpen = "No";
//        int brmsdateOpenCount = 0;
//        // Formatter for parsing DDMMYYYY format
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//        String[] cibildateopen = dateopen.split(",");
//        // Iterate through each dateOpened and compare with current date
//        for (String dateOpened : cibildateopen) {
//            LocalDate dateOpenedParsed = LocalDate.parse(dateOpened, formatter);
//
//            // Calculate the difference in years
//            long monthsDifference = ChronoUnit.MONTHS.between(dateOpenedParsed.withDayOfMonth(1), currentDate.withDayOfMonth(1));
//            Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
//            // Compare with 3 years
//            if (monthsDifference <= 60) {
//                Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
//                // Compare with 3 years
//            } else {
//                brmsdateOpenCount++;
//                Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
//                // Compare with 3 years
//            }
//        }
//        if (brmsdateOpenCount < 0) {
//            dateOpen = "Yes";
//            Log.consoleLog(ifr, "IFdateOpen=========>" + dateOpen);
//            // Compare with 3 years
//        } else {
//            dateOpen = "No";
//            Log.consoleLog(ifr, "ELSEdateOpen=========>" + dateOpen);
//            // Compare with 3 years
//        }
//
//        return dateOpen;
//    }
    //Added by Ahmed on 17-07-2024 for checkEMIKnockOffAccFilterStatus
    private boolean checkEMIKnockOffAccFilterStatus(IFormReference ifr, String accountType,
            String excludedAccnts,
            String accountStatus, String excludedAccStatus, String dateClosed) {

        Log.consoleLog(ifr, "#checkEMIKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "accountStatus===>" + accountStatus);
        Log.consoleLog(ifr, "dateClosed===>" + dateClosed);

        if (!dateClosed.equalsIgnoreCase("")) {
            return false;
        }

        String[] excludedAccounts = excludedAccnts.split(",");
        String[] excludedAccStatusArray = excludedAccStatus.split(",");

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "excludedAccounts===>" + accnt);
                return false;
            }
        }
        for (String accntStatus : excludedAccStatusArray) {
            if (accntStatus.equals(accountStatus)) {
                Log.consoleLog(ifr, "excludedAccStatusArray===>" + accntStatus);
                return false;
            }
        }
        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }

    private static boolean isValidDateFormat(String dateString, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false); // This makes the SimpleDateFormat object strict about the format

        try {
            Date date = sdf.parse(dateString);
            // If parsing is successful and the date string matches the format, return true
            return dateString.equals(sdf.format(date));
        } catch (Exception e) {
            // Parsing failed, return false
            return false;
        }
    }

    private static boolean isDateWithinSpecifiedMonths(String date, int nofMonthsToCheck) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate givenDate = LocalDate.parse(date, formatter);
        LocalDate currentDate = LocalDate.now();
        LocalDate monthsAgo = currentDate.minusMonths(nofMonthsToCheck);
        return !givenDate.isBefore(monthsAgo) && !givenDate.isAfter(currentDate);
    }

    private boolean checkKnockOffAccFilterStatus_WRO(IFormReference ifr, String accountType,
            String excludedAccnts,
            String accountStatus, String excludedAccStatus, String dateClosed) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatus_WRO started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "accountStatus===>" + accountStatus);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);

        String[] excludedAccounts = excludedAccnts.split(",");
        String[] excludedAccStatusArray = excludedAccStatus.split(",");

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "excludedAccounts===>" + accnt);
                return false;
            }
        }
        for (String accntStatus : excludedAccStatusArray) {
            if (accntStatus.equals(accountStatus)) {
                Log.consoleLog(ifr, "excludedAccStatusArray===>" + accntStatus);
                return false;
            }
        }

        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "yyyyMMdd";

            boolean isValidDateFormat = isValidDateFormat(dateClosed, dateFormat);
            Log.consoleLog(ifr, "IsValidDateFormat" + isValidDateFormat);

            if (isValidDateFormat) {
                boolean isWithinSpecifiedMonths = isDateWithinSpecifiedMonths(dateClosed, 36);
                if (isWithinSpecifiedMonths) {
                    Log.consoleLog(ifr, "The date " + dateClosed + " is within the last 36 months from the current date.");
                } else {
                    Log.consoleLog(ifr, "The date " + dateClosed + " is not within the last 36 months from the current date.");
                    return false;
                }
            }
        }

        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }

    private String checScoreCardDateOpend(IFormReference ifr, String dateopen) {
        LocalDate currentDate = LocalDate.now();
        String dateOpen = "No";
        int brmsdateOpenCount = 0;
        // Formatter for parsing DDMMYYYY format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String[] cibildateopen = dateopen.split(",");
        // Iterate through each dateOpened and compare with current date
        for (String dateOpened : cibildateopen) {
            Log.consoleLog(ifr, "dateOpened=========>" + dateOpened);
            LocalDate dateOpenedParsed;
            if (!dateOpened.equals("0")) {
                Log.consoleLog(ifr, "dateOpened if=========>" + dateOpened);
                dateOpenedParsed = LocalDate.parse(dateOpened, formatter);
            } else {
                Log.consoleLog(ifr, "dateOpened else=========>" + dateOpened);
                String mockDate = "18000101";
                dateOpenedParsed = LocalDate.parse(mockDate, formatter);
            }

            // Calculate the difference in years
            long monthsDifference = ChronoUnit.MONTHS.between(dateOpenedParsed.withDayOfMonth(1), currentDate.withDayOfMonth(1));
            Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
            // Compare with 3 years
            if (monthsDifference <= 60) {
                Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
                // Compare with 3 years
            } else {
                brmsdateOpenCount++;
                Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
                // Compare with 3 years
            }
        }
        if (brmsdateOpenCount < 0) {
            dateOpen = "Yes";
            Log.consoleLog(ifr, "IFdateOpen=========>" + dateOpen);
            // Compare with 3 years
        } else {
            dateOpen = "No";
            Log.consoleLog(ifr, "ELSEdateOpen=========>" + dateOpen);
            // Compare with 3 years
        }

        return dateOpen;
    }
}
