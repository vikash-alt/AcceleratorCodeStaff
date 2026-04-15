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
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.staffHL.CRGGenerator;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class ConsumerAPI {

    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    
    public String getConsumerCIBILScore2(IFormReference ifr, String ProductType,
            String enquiryAmount, String aadharNo, String applicantType) throws ParseException { // Added by Vikash Mehta

        Log.consoleLog(ifr, "#getConsumerCIBILScore starting...");

        String apiName = "CIBIL";
        Log.consoleLog(ifr, "apiName==>" + apiName);
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String Request = "";
        String Response = "";
        String insertionOrderId = "";

        try {

            String paymentHistoryCombined = "";
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDateTime now = LocalDateTime.now();
            String MonitoringDate = dtf.format(now);
            Log.consoleLog(ifr, "MonitoringDate==>" + MonitoringDate);

            //  String MobileNumber = pcm.getMobileNumber(ifr);
            String MobileNumber = "";

            //String enquiryAmount = "";
            String firstName = "";
            String lastName = "";
            String birthDate = "";
            String gender = "";
            String gendercode = "";
            String ParsedDate = "";
            String PAN = "";
            String scoreCardAccTypes = "";
            String overAllAccountTypes = "";
            String overAllcreditFacilityStatus = "";
            String overALLownershipIndicator = "";
            String scoreCardAccStatus = "";
            String scoreCardAccType = "";
            String scoreCardAccStatu = "";
            String scoreCardownershipIndicator = "";
            String scoreCardOwnerShip = "";
            String overALLdateOpened = "";
            String OwnerShipIndicator = "";
            //  String settledHistory = "";
            String pHist6monthsChk = "No";
            //String SrcPaymentHistory = "", PaymentHistory = "", NPA_INP = "", GUARANTORNPA_INP = "", WRITEOFF_INP = "", GUARANTORWRITEOFFSETTLEDHIST_INP = "";
            String SrcPaymentHistory = "", PaymentHistory = "", WRITEOFF_INP = "";
            String AddressLine1 = "", AddressLine2 = "", AddressLine3 = "", City = "",
                    State = "", Pincode = "", StateCode = "";

            String Query1 = "";
            String Query2 = "";
            String customerId = "";
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
                MobileNumber = MobileNumber.substring(MobileNumber.length() - 10);
            }
            CustomerAccountSummary cas = new CustomerAccountSummary();
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", MobileNumber);
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

            Log.consoleLog(ifr, "Query1==>" + Query1);

            List< List< String>> Result = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());
            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
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

            PAN = tempPan;
            birthDate = tempDateOfBirth;
            gender = tempGender;

            if (!birthDate.equalsIgnoreCase("")) {
                DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date = sdf.parse(birthDate);
                ParsedDate = new SimpleDateFormat("ddMMyyyy").format(date);
                Log.consoleLog(ifr, "ParsedDate======>" + ParsedDate);
            }

            if (gender.equalsIgnoreCase("Male")) {
                gendercode = "1";
            } else if (gender.equalsIgnoreCase("Female")) {
                gendercode = "2";
            } else {
                gendercode = "3";
            }

            Log.consoleLog(ifr, "firstName===>" + firstName);
            Log.consoleLog(ifr, "lastName====>" + lastName);
            Log.consoleLog(ifr, "PAN=========>" + PAN);
            Log.consoleLog(ifr, "birthDate===>" + birthDate);
            Log.consoleLog(ifr, "gender======>" + gender);
            Log.consoleLog(ifr, "gendercode==>" + gendercode);

            Log.consoleLog(ifr, "Query2==>" + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (Result1.size() > 0) {
                AddressLine1 = Result1.get(0).get(0);
                AddressLine2 = Result1.get(0).get(1);
                AddressLine3 = Result1.get(0).get(2);
                City = Result1.get(0).get(3);
                State = Result1.get(0).get(4);
                Pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "AddressLine1====>" + AddressLine1);
            Log.consoleLog(ifr, "AddressLine2====>" + AddressLine2);
            Log.consoleLog(ifr, "AddressLine3====>" + AddressLine3);
            Log.consoleLog(ifr, "City============>" + City);
            Log.consoleLog(ifr, "State===========>" + State);
            Log.consoleLog(ifr, "Pincode=========>" + Pincode);

            //Need to take from Statecode master
            //StateCode = "33";
            //PA_Pincode = "629002";
            //Added by Ahmed Alireza on 12-02-2024 for picking statecode from Master
            String Query3 = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + State + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "Query3==>" + Query3);
            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
            Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
            if (Result3.size() > 0) {
                StateCode = Result3.get(0).get(0);
            }
            //Ended by Ahmed Alireza on 12-02-2024 for picking statecode from Master

            if (StateCode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "StateCode founds to be empty for " + State + " in LOS_MST_STATE table.");
                return RLOS_Constants.ERROR + "StateCode not found";
            }
            if (AddressLine1.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "AddressLine1 not found");
                return RLOS_Constants.ERROR + "AddressLine1 not found";
            }
            if (Pincode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "Pincode not found");
                return RLOS_Constants.ERROR + "Pincode not found";
            }

            switch (enquiryAmount.length()) {
                case 4:
                    enquiryAmount = "00000" + enquiryAmount;
                    break;
                case 5:
                    enquiryAmount = "0000" + enquiryAmount;
                    break;
                case 6:
                    enquiryAmount = "000" + enquiryAmount;
                    break;
                case 7:
                    enquiryAmount = "00" + enquiryAmount;
                    break;
                case 8:
                    enquiryAmount = "0" + enquiryAmount;
                    break;
                default:
                    break;
            }

            String cicReportGenReq = pcm.getConstantValue(ifr, "TRANSUNION", "CICREPORTGENREQ");//Added by Ahmed on 02-05-2024
            String serviceCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SERVICECODE");
            String memberRefNo = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_MEMREFNO");
            String enquiryMemberUserId = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_USERID");
            String enquiryPassword = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_PWD");
            String version = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_VERSION");//12
            String enquiryPurpose = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_ENQPURPOSE");//05
            String headerType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_HEADERTYPE");//TUEF
            String gstStateCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_GSTSTATECODE");//01
            String scoreType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SCORETYPE");//08
            String outputFormat = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_OPFORMAT");//03
            String responseSize = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_RESSIZE");//1
            String ioMedia = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_IOMEDIA");//CC
            String authenticationMethod = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_AUTHMODE");//L

            String productCode = "HL";
            Log.consoleLog(ifr, "productCode=======>" + productCode);
            String npaResult = "";

            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "ACCTTYPE");
            Log.consoleLog(ifr, "excludedEMIAccnts=>" + excludedEMIAccnts);
            String excludedNPAAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "NPAACCTTYPE");
            Log.consoleLog(ifr, "excludedNPAAccnts=>" + excludedNPAAccnts);
            String excludedDPDAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "DPDACCTTYPE");
            Log.consoleLog(ifr, "excludedDPDAccnts=>" + excludedDPDAccnts);
            String excludedWROAccnts = pcm.getParamConfig2(ifr, productCode,"TRANSUNIONCONF", "WROACCTTYPE");
            Log.consoleLog(ifr, "excludedWROAccnts=>" + excludedWROAccnts);
            String excludedOwners = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "OWNERTYPE");
            Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);

            scoreCardAccTypes = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILACCTTYPE");
            Log.consoleLog(ifr, "scoreCardAccTypes=>" + scoreCardAccTypes);
            scoreCardAccStatus = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILACCTTYPESTATUS");
            Log.consoleLog(ifr, "scoreCardAccStatus=>" + scoreCardAccStatus);//SrcPaymentHistory
            scoreCardownershipIndicator = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILOWNERSHIPINDICATOR");
            Log.consoleLog(ifr, "scoreCardownershipIndicator=>" + scoreCardownershipIndicator);
            SrcPaymentHistory = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILSRCPAYMENTHISTORY");
            Log.consoleLog(ifr, "SrcPaymentHistory=>" + SrcPaymentHistory);
            String npaPaymentHistoryTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPAPAYMENTHIST");//SUB,DBT,LSS
            String npaSuitFiledTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPASUITFILED");//00
            String dpdMonthCalcTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DPDMONTHCALC");//6
            String dpdValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DAYSPASTDUE");//31
            String creditFacilityValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "CRDFACILITY");//31
            String pHistory6MonthCheckTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "PHIST6MONTHSCHK");

            String aadharSnippet = "";

            aadharNo = tempAadhar;
            Log.consoleLog(ifr, "Aadhar inside fetching Cibil score:: "+aadharNo);
            if (aadharNo.contains(RLOS_Constants.ERROR)) {
                return pcm.returnCustomErrorMessage(ifr, aadharNo);
            }
            if ((!aadharNo.equalsIgnoreCase("")) && (!aadharNo.equalsIgnoreCase("{}"))) {

                aadharSnippet = ",{\n"
                        + "                \"index\": \"I02\",\n"
                        + "                \"idNumber\": \"" + aadharNo + "\",\n"
                        + "                \"idType\": \"06\"\n"
                        + "       }\n";

            }
            Log.consoleLog(ifr, "Addhar Snippit inside cibil socre generation:: "+ aadharSnippet);

            Request = "{\n"
                    + "    \"serviceCode\": \"" + serviceCode + "\",\n"
                    + "    \"monitoringDate\": \"" + MonitoringDate + "\",\n"
                    + "    \"consumerInputSubject\": {\n"
                    + "        \"tuefHeader\": {\n"
                    + "            \"headerType\": \"" + headerType + "\",\n"
                    + "            \"version\": \"" + version + "\",\n"
                    + "            \"memberRefNo\": \"" + memberRefNo + "\",\n"
                    + "            \"gstStateCode\": \"" + gstStateCode + "\",\n"
                    + "            \"enquiryMemberUserId\": \"" + enquiryMemberUserId + "\",\n"
                    + "            \"enquiryPassword\": \"" + enquiryPassword + "\",\n"
                    + "            \"enquiryPurpose\": \"" + enquiryPurpose + "\",\n"
                    + "            \"enquiryAmount\": \"" + enquiryAmount + "\",\n"
                    + "            \"scoreType\": \"" + scoreType + "\",\n"
                    + "            \"outputFormat\": \"" + outputFormat + "\",\n"
                    + "            \"responseSize\": \"" + responseSize + "\",\n"
                    + "            \"ioMedia\": \"" + ioMedia + "\",\n"
                    + "            \"authenticationMethod\": \"" + authenticationMethod + "\"\n"
                    + "        },\n"
                    + "        \"names\": [\n"
                    + "            {\n"
                    + "                \"index\": \"N01\",\n"
                    + "                \"firstName\": \"" + firstName + "\",\n"
                    + "                \"lastName\": \"" + lastName + "\",\n"
                    + "                \"birthDate\": \"" + ParsedDate + "\",\n"
                    + "                \"gender\": \"" + gendercode + "\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"ids\": [\n"
                    + "            {\n"
                    + "                \"index\": \"I01\",\n"
                    + "                \"idNumber\": \"" + PAN + "\",\n"
                    + "                \"idType\": \"01\"\n"
                    + "            }\n"
                    + aadharSnippet
                    + "        ],\n"
                    + "        \"telephones\": [\n"
                    + "            {\n"
                    + "                \"index\": \"T01\",\n"
                    + "                \"telephoneNumber\": \"" + MobileNumber + "\",\n"
                    + "                \"telephoneType\": \"01\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"addresses\": [\n"
                    + "            {\n"
                    + "                \"index\": \"A01\",\n"
                    + "                \"line1\": \"" + AddressLine1 + "\",\n"
                    + "                \"line2\": \"" + AddressLine2 + "\",\n"
                    + "                \"line3\": \"" + AddressLine3 + "\",\n"
                    + "                \"line4\": \"" + City + "\",\n"
                    + "                \"line5\": \"\",\n"
                    + "                \"stateCode\": \"" + StateCode + "\",\n"//shoudl get from master
                    + "                \"pinCode\": \"" + Pincode + "\",\n"
                    + "                \"addressCategory\": \"04\",\n"
                    + "                \"residenceCode\": \"01\"\n"
                    + "           }\n"
                    + "        ],\n"
                    + "        \"enquiryAccounts\": [\n"
                    + "            {\n"
                    + "                \"index\": \"I01\",\n"
                    + "                \"accountNumber\": \"\"\n"
                    + "            }\n"
                    + "        ]\n"
                    + "    }\n"
                    + "}";

            Log.consoleLog(ifr, "Requerst before calling cibil:: "+Request);
            Response = cm.getWebServiceResponse(ifr, apiName, Request);
            Log.consoleLog(ifr, "Response===> CONSUMERAPI****" + Response);

//              cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "", applicantType);
            if (!Response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(Response);

                String body = resultObj.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser.parse(body);

                String ControlData = bodyObj.get("controlData").toString();
                Log.consoleLog(ifr, "ControlData==>" + ControlData);

                JSONObject ControlDataObj = (JSONObject) parser.parse(ControlData);
                String ControlDataStatus = ControlDataObj.get("success").toString();

                Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);

                if (ControlDataStatus.equalsIgnoreCase("true")) {

                    Log.consoleLog(ifr, "cicReportGenReq==>" + cicReportGenReq);
                    if (cicReportGenReq.equalsIgnoreCase("Y")) {
                        if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
                            String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
                            String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Consumer", encodedB64, "NGREPORTTOOL_TRANSUNION");
                            Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
                            cm.updateCICReportStatus(ifr, "CIBIL", generateReportStatus, applicantType);//Added by Ahmed on 26-07-2024 for Status Updation of CIC Report
                        }
                    }

                    String BureauScore = "";
                    int cibilScore = 0;
                    String totalAccounts = "0";
                    String Scores = "";
                    String accounts = "";
                    String type= "NoCIC";
                    String consumerCreditData = cf.getJsonValue(bodyObj, "consumerCreditData");
                    
                    
                
                    JSONArray consumerCreditDataJSON = (JSONArray) parser.parse(consumerCreditData);
                    if (!consumerCreditDataJSON.isEmpty()) {
                        for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
                            String InputString = consumerCreditDataJSON.get(i).toString();
                            JSONObject consumerCreditDataJSONObj = (JSONObject) parser.parse(InputString);
                            Scores = consumerCreditDataJSONObj.get("scores").toString();
                            Log.consoleLog(ifr, "Scores=" + Scores);

                            try {
                                if (consumerCreditDataJSONObj.containsKey("accounts")) {
                                	type="";
                                    accounts = cf.getJsonValue(consumerCreditDataJSONObj, "accounts");
                                    //  consumerCreditDataJSONObj.get("accounts").toString();
                                    Log.consoleLog(ifr, "account=" + accounts);
                                }
                            } catch (Exception e) {
                                Log.consoleLog(ifr, "Exception/accounts=" + e);
                            }

                        }
                    }

                    Log.consoleLog(ifr, "Scores==>" + Scores);
                    JSONArray ScoresJSON = (JSONArray) parser.parse(Scores);

                    if (!ScoresJSON.isEmpty()) {
                        for (int i = 0; i < ScoresJSON.size(); i++) {
                            String InputString = ScoresJSON.get(i).toString();
                            JSONObject ScoresJSONObj = (JSONObject) parser.parse(InputString);
                            String transCIBILScore = ScoresJSONObj.get("score").toString();
                            Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
                            cibilScore = Integer.parseInt(transCIBILScore);
                            Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
                            BureauScore = String.valueOf(cibilScore);
                            Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
                            //added by prakash 02-03-2024 for BureauScore set default as 0 if 000-1

                            if ((BureauScore.equalsIgnoreCase("000-1"))) {
                                String cbilimmuneAccept = ConfProperty.getCommonPropertyValue("CICImmune_Accept");
                                Log.consoleLog(ifr, "cbilimmuneAccept==>" + cbilimmuneAccept);
                                //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

                                if (cbilimmuneAccept.equalsIgnoreCase("YES")) {
                                    BureauScore = "0";
                                    transCIBILScore="0";
                                }

                            }
                            Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
                        }
                    }
                    if ((Integer.parseInt(BureauScore) > 200)
                            || (Integer.parseInt(BureauScore) < 100)) {
                        String consumerSummaryData = cf.getJsonValue(bodyObj, "consumerSummaryData");
                        JSONObject consumerSummaryDataObj = (JSONObject) parser.parse(consumerSummaryData);
                        String accountSummary = cf.getJsonValue(consumerSummaryDataObj, "accountSummary");
                        JSONObject accountSummaryObj = (JSONObject) parser.parse(accountSummary);

                        totalAccounts = cf.getJsonValue(accountSummaryObj, "totalAccounts");
                        Log.consoleLog(ifr, "totalAccounts==>" + totalAccounts);

                    }
                    int suitFiledcount = 0;

                    int dpdCount = 0;//dayspastdue

                    int totalEmiAmnt = 0;
                    int totalNonEmiCount = 0;
                    String consolidatedtwelveMonthData = "";
                    Log.consoleLog(ifr, "accounts==>" + accounts);
                    int count6MonthData = 0;
                    int total_NPA_INP = 0;
                    int totalNPA = 0;

                    String paymentHistoryClob = "000";
                    String dateReportedStr = "";
                    String typeofcredit = "";
                    Map<String, String> npaResultCheck = new HashMap<>();
                    Map<String, String> wrosettResultCheck = new HashMap<>();

                    //Commented by Ahmed on 06-09-2024 after for MoneshKumar
                    if ((!accounts.equalsIgnoreCase(""))) {
                        //if (!accounts.equalsIgnoreCase("")) {
                        JSONArray accountsJSON = (JSONArray) parser.parse(accounts);

                        if (!accountsJSON.isEmpty()) {

                            String query5 = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
                            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query5, "Execute query for fetching loan selected ");
                            String loan_selected = loanSelected.get(0).get(0);
                            Log.consoleLog(ifr, "loan type==>%%" + loan_selected);

                            for (int i = 0; i < accountsJSON.size(); i++) {

                                String InputString = accountsJSON.get(i).toString();
                                JSONObject accountsJSONObj = (JSONObject) parser.parse(InputString);
                                dateReportedStr = accountsJSONObj.get("dateReported").toString();
                                Log.consoleLog(ifr, "conssumerAPI:CIBILScore->dateReported: " + dateReportedStr);
                                Log.consoleLog(ifr, "overAllAccountTypes" + overAllAccountTypes);
                                Log.consoleLog(ifr, "overAllcreditFacilityStatus" + overAllcreditFacilityStatus);

                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();
                                    String paymentStartDate = accountsJSONObj.get("paymentStartDate").toString();

                                    Log.consoleLog(ifr, "Payment_History_Profile" + payHistory);
                                    // added by vandana start

                                    Log.consoleLog(ifr, "Payment_History_Profile_Data_Cibil" + payHistory);

                                    String payment = payHistory.replace("?", "0")
                                            .replace("S", "0").replace("N", "0")
                                            .replace("B", "0").replace("D", "0")
                                            .replace("U", "0").replace("T", "0")
                                            .replace("M", "0").replace("L", "0").replace("X", "0")
                                            .replace("M", "0").replace("L", "0").replace("X", "0")
                                            .replace("M", "0").replace("L", "0").replace("X", "0").replace("T", "0");

                                    Log.consoleLog(ifr, "Payment_History_Profile" + payment);

                                    // Starts Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK
                                    if (loan_selected.equalsIgnoreCase("Canara Pension")
                                            || loan_selected.equalsIgnoreCase("Canara Budget")
                                            || loan_selected.equalsIgnoreCase("VEHICLE LOAN")
                                            && !paymentStartDate.equalsIgnoreCase("")) {
                                        payment = validatePaymentHistory_CB(ifr, paymentStartDate, payment, "DPD");
                                        npaResult = validatePaymentHistory_CB(ifr, dateReportedStr, payment, "NPA");
                                        Log.consoleLog(ifr, "npaResult==>$$" + npaResult);

                                        if (!npaResult.equals("")) {

                                            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                                            Log.consoleLog(ifr, "dateReportedStr==>$$" + dateReportedStr);
                                            Date dateReported = sdf.parse(dateReportedStr);
                                            Calendar cal = Calendar.getInstance();
                                            cal.setTime(dateReported);
                                            cal.add(Calendar.MONTH, 60);
                                            Date sixtyMonthsDate = cal.getTime();
                                            Log.consoleLog(ifr, "sixtyMonthsDate==>$$" + sixtyMonthsDate);
                                            Date today = new Date();

                                            if (today.before(sixtyMonthsDate)) {
                                                // NPA_INP = "Yes";
                                                total_NPA_INP += Integer.parseInt(npaResult);

                                                Log.consoleLog(ifr, "dateReported is within 60 months from today::" + sixtyMonthsDate);
                                            } else {
                                                totalNPA += Integer.parseInt(npaResult);
                                                Log.consoleLog(ifr, "dateReported is older than 60 months.:NPA_INP is No:" + sixtyMonthsDate);
                                            }

                                        }

                                        consolidatedtwelveMonthData = consolidatedtwelveMonthData + payment;

                                        Log.consoleLog(ifr, "consolidatedtwelveMonthData==>$$" + consolidatedtwelveMonthData);

                                    } else {     // end  Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK

                                        //-----------------
                                        if (payment.length() < 3) {
                                            Log.consoleLog(ifr, "Insufficient data to extract payment history.");
                                            //return "";//Modified by Ahmed on 18-07-2024
                                        } else {
                                            int numberOfMonths = payment.length() / 3;
                                            Log.consoleLog(ifr, "Payment_History_Profile numberOfMonths" + numberOfMonths);

                                            for (int j = 0; j < numberOfMonths; j++) {
                                                String twelveMonthData = payment.substring(j * 3, (j * 3) + 3);
                                                consolidatedtwelveMonthData = consolidatedtwelveMonthData + twelveMonthData;
                                            }
                                        }
                                    }
                                    Log.consoleLog(ifr, "consolidatedtwelveMonthData" + consolidatedtwelveMonthData);
                                    paymentHistoryCombined = paymentHistoryCombined + payHistory;

                                    String[] substrings = splitString(payHistory, 3);
                                    if (substrings.length >= 6) {
                                        for (int j = 0; j < 6; j++) {
                                            if (!substrings[j].equalsIgnoreCase("XXX")) {
                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, substrings[j]);
                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
                                                if (sixMonthchkdata) {
                                                    count6MonthData++;
                                                }
                                            }
                                        }
                                    } else {
                                        for (String pHistory : substrings) {
                                            if (!pHistory.equalsIgnoreCase("XXX")) {
                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, pHistory);
                                                Log.consoleLog(ifr, "pHistory/less than 6 months available==>" + pHistory);
                                                if (sixMonthchkdata) {
                                                    count6MonthData++;
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    Log.consoleLog(ifr, "paymentHistory tag not available.");
                                }
                                if (count6MonthData > 0) {
                                    pHist6monthsChk = "Yes";
                                }
                                Log.consoleLog(ifr, "pHist6monthsChk  " + pHist6monthsChk);
                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();

                                    //       String firstSixMonths = paymentHistory.substring(0, 18); // Assuming each month is 3 digits, first 6 months are 18 characters
                                    // Check if first 6 months contain SUB, DBT, or LSS
                                    //    PaymentHistory = PaymentHistory + firstSixMonths;
                                }

                                String creditFacilityStatus = "";
                                String paymentHistory = "";
                                String suitFiled = "";
                                String accountType = "";
                                String dateClosed = "";
                                String currentBalance = "";
                                String ownershipIndicator = "";
                                String dateOpened = "";

                                if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus").equalsIgnoreCase(""))) {
                                    creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + creditFacilityStatus;
                                    Log.consoleLog(ifr, "overAllcreditFacilityStatus==>" + overAllcreditFacilityStatus);
                                } else {
                                    Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
                                    accountType = accountsJSONObj.get("accountType").toString();
                                    overAllAccountTypes = overAllAccountTypes + accountType;
                                    Log.consoleLog(ifr, "accountType==>" + accountType);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "ownershipIndicator").equalsIgnoreCase(""))) {
                                    ownershipIndicator = accountsJSONObj.get("ownershipIndicator").toString();
                                    overALLownershipIndicator = overALLownershipIndicator + ownershipIndicator;
                                    Log.consoleLog(ifr, "ownershipIndicator==>" + ownershipIndicator);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
                                    dateOpened = accountsJSONObj.get("dateOpened").toString();
                                    overALLdateOpened = overALLdateOpened + dateOpened;
                                    Log.consoleLog(ifr, "dateOpened==>:::===>$$$" + dateOpened);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!overAllAccountTypes.equalsIgnoreCase("")) {
                                    overAllAccountTypes = overAllAccountTypes + ",";
                                }

                                if (!overAllcreditFacilityStatus.equalsIgnoreCase("")) {
                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + ",";
                                }
                                if (!overALLownershipIndicator.equalsIgnoreCase("")) {
                                    overALLownershipIndicator = overALLownershipIndicator + ",";
                                }
                                if (!overALLdateOpened.equalsIgnoreCase("")) {
                                    overALLdateOpened = overALLdateOpened + ",";
                                    Log.consoleLog(ifr, "overALLdateOpened==>$$$" + overALLdateOpened);

                                }
                                if (!PaymentHistory.equalsIgnoreCase("")) {
                                    PaymentHistory = PaymentHistory + ",";//overALLdateOpened
                                }

                                if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
                                    currentBalance = accountsJSONObj.get("currentBalance").toString();
                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);

                                    if (currentBalance.equalsIgnoreCase("")) {
                                        currentBalance = "0";
                                    }

                                } else {
                                    Log.consoleLog(ifr, "currentBalance tag not available.");
                                }

                                if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
                                    dateClosed = accountsJSONObj.get("dateClosed").toString();
                                    Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
                                } else {
                                    Log.consoleLog(ifr, "dateClosed tag not available.");
                                }

                                Log.consoleLog(ifr, "emiStatus==>&&&" + ownershipIndicator);
                                boolean emiStatus = checkEMIAccFilterStatus(ifr,
                                        accountType, currentBalance, dateClosed,
                                        excludedEMIAccnts,
                                        ownershipIndicator, excludedOwners);
                                Log.consoleLog(ifr, "emiStatus==>" + emiStatus);

                                //For NPA
                                boolean npaStatuscheck = checkKnockOffAccFilterStatusNPA(ifr, accountType,
                                        excludedNPAAccnts);
                                Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:npaStatuscheck==>" + npaStatuscheck);
                                if (npaStatuscheck) {

                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                        String paymentHistoryNPA = accountsJSONObj.get("paymentHistory").toString();
                                        Log.consoleLog(ifr, "paymentHistoryNPA==>" + paymentHistoryNPA);

                                        Map<String, String> NPACheck = NPACheckfromCibil(ifr, paymentHistoryNPA,
                                                dateReportedStr, ownershipIndicator, npaPaymentHistoryTags);
                                        Log.consoleLog(ifr, "NPACheck==>" + NPACheck);

                                        // Merge results: keep "Yes" if it has already been found
                                        for (Map.Entry<String, String> entry : NPACheck.entrySet()) {
                                            String key = entry.getKey();
                                            String value = entry.getValue();

                                            // If already "Yes", retain it; otherwise update
                                            String existing = npaResultCheck.getOrDefault(key, "No");
                                            npaResultCheck.put(key, existing.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("Yes") ? "Yes" : "No");
                                        }

                                        Log.consoleLog(ifr, "npaResultCheck==>" + npaResultCheck);
                                    } else {
                                        Log.consoleLog(ifr, "paymentHistory is empty==>" + paymentHistory);
                                    }

                                    if (!(cf.getJsonValue(accountsJSONObj, "suitFiled").equalsIgnoreCase(""))) {
                                        suitFiled = accountsJSONObj.get("suitFiled").toString();
                                        Log.consoleLog(ifr, "suitFiled==>" + suitFiled);
                                    } else {
                                        Log.consoleLog(ifr, "suitFiled tag not available.");
                                    }

                                    //Suit Filed should not be 00 and it should not be empty- As per Naveen`s Mail
                                    if (!npaSuitFiledTags.contains(suitFiled) && (!suitFiled.equalsIgnoreCase(""))) {
                                        suitFiledcount++;
                                    }
                                }

                                //For DPD
                                boolean dpdStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedDPDAccnts, dateClosed);
                                Log.consoleLog(ifr, "dpdStatus==>" + dpdStatus);

                                if (dpdStatus) {
                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                        paymentHistory = accountsJSONObj.get("paymentHistory").toString();
                                        Log.consoleLog(ifr, "paymentHistory==>" + paymentHistory);

                                        String[] substrings = splitString(paymentHistory, 3);

                                        int dpdmonthcounttag = 1;
                                        for (String pHistoryTag : substrings) {
                                            Log.consoleLog(ifr, "pHistoryTag " + pHistoryTag);
                                            if (dpdmonthcounttag <= Integer.parseInt(dpdMonthCalcTags)) {
                                                if (!pHistoryTag.equalsIgnoreCase("XXX")) {

                                                    try {
                                                        if (Integer.parseInt(pHistoryTag) >= Integer.parseInt(dpdValueTags)) {
                                                            dpdCount++;
                                                        }
                                                    } catch (Exception e) {
                                                        Log.consoleLog(ifr, "Exception where pHistoryTag is an alphaber and not numeric. So here DPDCount is skipped");
                                                    }

                                                }
                                                dpdmonthcounttag++;
                                            }
                                            Log.consoleLog(ifr, "dpdfirst6monthcounttag " + dpdmonthcounttag);
                                        }

                                    } else {
                                        Log.consoleLog(ifr, "paymentHistory tag not available.");
                                    }

                                } else {
                                    Log.consoleLog(ifr, "DPD Account Type Excluded. Please check config master. Looping next..");
                                }

                                //for Writeoff Setttled Restructred
                                boolean WroSetResStatus = checkKnockOffAccFilterStatus_WSR(ifr, accountType, excludedWROAccnts);
                                Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:WroSetResStatus==>" + WroSetResStatus);
                                if (WroSetResStatus) {
                                    if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus")
                                            .equalsIgnoreCase(""))) {
                                        creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:creditFacilityStatus==>" + creditFacilityStatus);
                                        Map<String, String> WroSetResCheck = WroSetResCheckfromCibil(ifr,
                                                creditFacilityStatus, dateReportedStr,
                                                ownershipIndicator);
                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:WroSetResCheck==>" + WroSetResCheck);
                                        // Merge logic to retain "Yes" once it's found
                                        for (Map.Entry<String, String> entry : WroSetResCheck.entrySet()) {
                                            String key = entry.getKey();
                                            String value = entry.getValue();
                                            String existing = wrosettResultCheck.getOrDefault(key, "No");
                                            wrosettResultCheck.put(key, existing.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("Yes") ? "Yes" : "No");
                                        }

                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:wrosettResultCheck==>" + wrosettResultCheck);

                                    } else {
                                        Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
                                    }

                                } else {
                                    Log.consoleLog(ifr, "WRO Account Type Excluded. Please check config master. Looping next..");
                                }

                                //For EMI Block
                                //Modified by Ahmed on 03-06-2024 for performing Total Non EMI cals
                                if (emiStatus) {
                                    String emiAmount = "0";
                                    if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
                                        emiAmount = accountsJSONObj.get("emiAmount").toString();
                                        Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
                                        if (!emiAmount.equalsIgnoreCase("")) {
                                            totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
                                        } else {
                                            Log.consoleLog(ifr, "emiAmount tag available but empty");
                                            if (Integer.parseInt(currentBalance) > 0) {
                                                totalNonEmiCount++;
                                            }
                                        }
                                    } else {
                                        Log.consoleLog(ifr, "emiAmount tag not available.");

                                        if (Integer.parseInt(currentBalance) > 0) {
                                            totalNonEmiCount++;
                                        }
                                    }
                                } else {
                                    Log.consoleLog(ifr, "EMI Account Type Excluded. "
                                            + "Please check config master. Looping next..");
                                }
                            }//End of Accounts Loop                           

                        }

                    }

                    String DPD = "0";
                    //dpd
                    if (dpdCount > 0) {
                        DPD = String.valueOf(dpdCount);
                    }

                    Log.consoleLog(ifr, "DPD==>" + DPD);
                    String NPA = "No";//field name--> CICNPACHECK //npa borrower
                    String NPAG = "No";////field name--> GUARANTORNPAINP //npa GUARANTOR
                    String npaWroSettSixtyI = "No";////field name--> SRNPAINP //npa 60 check borrower
                    String NPASixtyG = "No";////field name--> NPACOUNT_CB //npa 60 check 
                    Log.consoleLog(ifr, "npaResultCheck check==>" + npaResultCheck);

                    NPA = npaResultCheck.get("NPAI");//field name--> CICNPACHECK //npa borrower
                    NPAG = npaResultCheck.get("NPAG");////field name--> GUARANTORNPAINP //npa GUARANTOR
                    NPASixtyG = npaResultCheck.get("NPASixtyMonthG");////field name--> NPACOUNT_CB //npa 60 check 

                    String wro = "No";////field name--> WRITEOFF
                    String Sett = "No";////field name--> SETTLEDHISTORY
                    String res = "No";
                    String wrosettresG = "No";////field name--> GUARANTORWRITEOFFSETTLEDHIST
                    String wroSettRessixtyG = "No";
                    Log.consoleLog(ifr, "wrosettResultCheck check==>" + wrosettResultCheck);

                    wro = wrosettResultCheck.getOrDefault("wroI", "No");////field name--> WRITEOFF
                    Sett = wrosettResultCheck.getOrDefault("settI", "No");
                    res = wrosettResultCheck.getOrDefault("resI", "No");
                    wrosettresG = wrosettResultCheck.getOrDefault("wrosettresG", "No");
                    wroSettRessixtyG = wrosettResultCheck.getOrDefault("wrosettresSixtyG", "No");

                    if ("Yes".equalsIgnoreCase(npaResultCheck.getOrDefault("NPASixtyMonthI", "No"))
                            || "Yes".equalsIgnoreCase(wrosettResultCheck.getOrDefault("wrosettresSixtyI", "No"))) {
                        npaWroSettSixtyI = "Yes";
                    }
                    Log.consoleLog(ifr, "BureauScore==>" + BureauScore);
                    Log.consoleLog(ifr, "paymentHistoryCombined==>" + consolidatedtwelveMonthData);
                    Log.consoleLog(ifr, "NPA Results => NPAI: " + NPA + ", NPAG: " + NPAG + ", NPASixtyMonthI: " + npaWroSettSixtyI + ", NPASixtyMonthG: " + NPASixtyG
                            + " | WRO/SETT/RES Results => wroI: " + wro + ", settI: " + Sett + ", resI: " + res
                            + ", wrosettresG: " + wrosettresG + ", wrosettresSixtyI: " + npaWroSettSixtyI + ", wrosettresSixtyG: " + wroSettRessixtyG);

                    Log.consoleLog(ifr, "dpdCount " + dpdCount);
                    Log.consoleLog(ifr, "totalNonEmiCount==>" + totalNonEmiCount);
                    Log.consoleLog(ifr, "overAllAccountTypes  " + overAllAccountTypes);
                    Log.consoleLog(ifr, "overAllAccountStatus  " + overAllcreditFacilityStatus);
                    Log.consoleLog(ifr, "overALLownershipIndicator  " + overALLownershipIndicator);

                    //If-Else Condition Removed for PAPL / Other Journerys until Impact is derived.
                    String newToCredit = "Yes";
                    String lapExists = "No";//Need to derive the count as per the Factors
                    if (Integer.parseInt(totalAccounts) > 0) {
                        newToCredit = "No";
                    }

                    String qryCICDataUpdate1 = "";
                    String qryCICDataUpdate2 = "";
                    if(type.equalsIgnoreCase("NoCIC"))
                    {
                    	totalEmiAmnt=Integer.parseInt("0");
                    }
                    qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,"
                            + " EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,"
                            + " PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
                            + " GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,NPACOUNT_CB,APPLICANT_UID) "
                            + "values('" + ProcessInstanceId + "','" + BureauScore + "','" + NPA + "','" + DPD + "','"
                            + "" + wro + "','CB','" + totalEmiAmnt + "'," + paymentHistoryClob + ",'" + applicantType
                            + "','" + totalNonEmiCount + "','" + Sett + "','" + npaWroSettSixtyI + "','"
                            + NPAG + "','" + wrosettresG + "',SYSDATE,'"
                            + NPASixtyG + "','" + insertionOrderId + "')";
                    Log.consoleLog(ifr, "qryCICDataUpdate1:" + qryCICDataUpdate1);
                    ifr.saveDataInDB(qryCICDataUpdate1);
                    // } else {
//                    qryCICDataUpdate2 = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
//                            + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
//                            + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
//                            + "CIC_SCORE,NPA_STATUS,DPD,WRITEOFF_STATUS,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
//                            + "VALUES('" + ProcessInstanceId + "',"
//                            + "'','" + MobileNumber + "','" + productCode + "','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderId + "',\n"
//                            + "'EXT','EX','','" + lapExists + "','" + BureauScore + "',"
//                            + "'" + NPA + "','" + DPD + "','" + wro + "','" + totalEmiAmnt + "','" + newToCredit + "',SYSDATE,SYSDATE)";
                    // }
//                    Log.consoleLog(ifr, "qryCICDataUpdate2:" + qryCICDataUpdate2);
//                    ifr.saveDataInDB(qryCICDataUpdate2);
//                    if (!productCode.equalsIgnoreCase("MF")) {
//                        CRGGenerator crg = new CRGGenerator();
//                        Log.consoleLog(ifr, "@@@ConsumerAPI:getConsumerCIBILScore==>insertionOrderId@@@" + insertionOrderId);
//                        crg.crgGenCibil(ifr, bodyObj, ProcessInstanceId, apiName, applicantType, type,insertionOrderId);
//                        //cm.generateTransunionReport(ifr, ProcessInstanceId, "CONSUMER", Response);
//                    }
                    return BureauScore;
                } else {
                    return RLOS_Constants.ERROR;
                }

            } else {
                Response = "No response from Server.";
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getConsumerCIBILScore==>" + e);
        } finally {
            /*cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);*/
            Log.consoleLog(ifr, "finally==>bLOCKfinally");
            if (!Request.isEmpty()) {
                cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "", (!insertionOrderId.equalsIgnoreCase("") ? insertionOrderId : applicantType));
            }

        }

        return RLOS_Constants.ERROR;

    }
    
//    public String getConsumerCIBILScore2(IFormReference ifr, String ProductType,
//            String enquiryAmount, String aadharNo, String applicantType) throws ParseException {
//
//        Log.consoleLog(ifr, "#getConsumerCIBILScore starting...");
//
//        String apiName = "CIBIL";
//        Log.consoleLog(ifr, "apiName==>" + apiName);
//        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String Request = "";
//        String Response = "";
//        String insertionOrderId = "";
//
//        try {
//
//            String paymentHistoryCombined = "";
//            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
//            LocalDateTime now = LocalDateTime.now();
//            String MonitoringDate = dtf.format(now);
//            Log.consoleLog(ifr, "MonitoringDate==>" + MonitoringDate);
//
//            //  String MobileNumber = pcm.getMobileNumber(ifr);
//            String MobileNumber = "";
//
//            //String enquiryAmount = "";
//            String firstName = "";
//            String lastName = "";
//            String birthDate = "";
//            String gender = "";
//            String gendercode = "";
//            String ParsedDate = "";
//            String PAN = "";
//            String scoreCardAccTypes = "";
//            String overAllAccountTypes = "";
//            String overAllcreditFacilityStatus = "";
//            String overALLownershipIndicator = "";
//            String scoreCardAccStatus = "";
//            String scoreCardAccType = "";
//            String scoreCardAccStatu = "";
//            String scoreCardownershipIndicator = "";
//            String scoreCardOwnerShip = "";
//            String overALLdateOpened = "";
//            String OwnerShipIndicator = "";
//            //  String settledHistory = "";
//            String pHist6monthsChk = "No";
//            //String SrcPaymentHistory = "", PaymentHistory = "", NPA_INP = "", GUARANTORNPA_INP = "", WRITEOFF_INP = "", GUARANTORWRITEOFFSETTLEDHIST_INP = "";
//            String SrcPaymentHistory = "", PaymentHistory = "", WRITEOFF_INP = "";
//            String AddressLine1 = "", AddressLine2 = "", AddressLine3 = "", City = "",
//                    State = "", Pincode = "", StateCode = "";
//
//            String Query1 = "";
//            String Query2 = "";
//            String customerId = "";
//
//            Query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,customerid "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//
//            MobileNumber = "";
//
//            String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" + ProcessInstanceId + "'";
//            List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
//            if (!list.isEmpty()) {
//                MobileNumber =  list.get(0).get(0);
//            }
//
//            Log.consoleLog(ifr,"Mobile number after fetching in cibil check:: +"+MobileNumber);
//
//            Log.consoleLog(ifr, "Query1==>" + Query1);
//
//            List< List< String>> Result = ifr.getDataFromDB(Query1);
//            Log.consoleLog(ifr, "#Result===>" + Result.toString());
//            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
//            Log.consoleLog(ifr, "#Result pattern===>" + pattern);
//            if (Result.size() > 0) {
//                firstName = Result.get(0).get(0).replaceAll("[ .]", "");
//                //firstName = Result.get(0).get(0).trim().replaceAll(".", "");
//                lastName = Result.get(0).get(1).replaceAll("[ .]", "");
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
//
//                PAN = Result.get(0).get(2);
//                birthDate = Result.get(0).get(3);
//                gender = Result.get(0).get(4);
//
//                customerId = Result.get(0).get(5);//Added by Ahmed on 31-07-2024
//            }
//
//            if (!birthDate.equalsIgnoreCase("")) {
//                DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
//                Date date = sdf.parse(birthDate);
//                ParsedDate = new SimpleDateFormat("ddMMyyyy").format(date);
//                Log.consoleLog(ifr, "ParsedDate======>" + ParsedDate);
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
//            Log.consoleLog(ifr, "PAN=========>" + PAN);
//            Log.consoleLog(ifr, "birthDate===>" + birthDate);
//            Log.consoleLog(ifr, "gender======>" + gender);
//            Log.consoleLog(ifr, "gendercode==>" + gendercode);
//
//            Log.consoleLog(ifr, "Query2==>" + Query2);
//            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
//            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
//
//            if (Result1.size() > 0) {
//                AddressLine1 = Result1.get(0).get(0);
//                AddressLine2 = Result1.get(0).get(1);
//                AddressLine3 = Result1.get(0).get(2);
//                City = Result1.get(0).get(3);
//                State = Result1.get(0).get(4);
//                Pincode = Result1.get(0).get(5);
//            }
//
//            Log.consoleLog(ifr, "AddressLine1====>" + AddressLine1);
//            Log.consoleLog(ifr, "AddressLine2====>" + AddressLine2);
//            Log.consoleLog(ifr, "AddressLine3====>" + AddressLine3);
//            Log.consoleLog(ifr, "City============>" + City);
//            Log.consoleLog(ifr, "State===========>" + State);
//            Log.consoleLog(ifr, "Pincode=========>" + Pincode);
//
//            //Need to take from Statecode master
//            //StateCode = "33";
//            //PA_Pincode = "629002";
//            //Added by Ahmed Alireza on 12-02-2024 for picking statecode from Master
//            String Query3 = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
//                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + State + "')) AND ROWNUM=1";
//            Log.consoleLog(ifr, "Query3==>" + Query3);
//            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
//            Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
//            if (Result3.size() > 0) {
//                StateCode = Result3.get(0).get(0);
//            }
//            //Ended by Ahmed Alireza on 12-02-2024 for picking statecode from Master
//
//            if (StateCode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "StateCode founds to be empty for " + State + " in LOS_MST_STATE table.");
//                return RLOS_Constants.ERROR + "StateCode not found";
//            }
//            if (AddressLine1.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "AddressLine1 not found");
//                return RLOS_Constants.ERROR + "AddressLine1 not found";
//            }
//            if (Pincode.equalsIgnoreCase("")) {
//                Log.consoleLog(ifr, "Pincode not found");
//                return RLOS_Constants.ERROR + "Pincode not found";
//            }
//
//            switch (enquiryAmount.length()) {
//                case 4:
//                    enquiryAmount = "00000" + enquiryAmount;
//                    break;
//                case 5:
//                    enquiryAmount = "0000" + enquiryAmount;
//                    break;
//                case 6:
//                    enquiryAmount = "000" + enquiryAmount;
//                    break;
//                case 7:
//                    enquiryAmount = "00" + enquiryAmount;
//                    break;
//                case 8:
//                    enquiryAmount = "0" + enquiryAmount;
//                    break;
//                default:
//                    break;
//            }
//
//            String cicReportGenReq = pcm.getConstantValue(ifr, "TRANSUNION", "CICREPORTGENREQ");//Added by Ahmed on 02-05-2024
//            String serviceCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SERVICECODE");
//            String memberRefNo = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_MEMREFNO");
//            String enquiryMemberUserId = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_USERID");
//            String enquiryPassword = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_PWD");
//            String version = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_VERSION");//12
//            String enquiryPurpose = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_ENQPURPOSE");//05
//            String headerType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_HEADERTYPE");//TUEF
//            String gstStateCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_GSTSTATECODE");//01
//            String scoreType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SCORETYPE");//08
//            String outputFormat = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_OPFORMAT");//03
//            String responseSize = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_RESSIZE");//1
//            String ioMedia = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_IOMEDIA");//CC
//            String authenticationMethod = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_AUTHMODE");//L
//
//            //Added by Ahmed on 14-02-2024 for excluding accountTypes.
//            // //Commented by Ahmed on 28-06-2024 for taking dynamic prod & sub prod code from master
////            String productCode = "";
////            String subProductCode = "";
////            if (ProductType.equalsIgnoreCase("PAPL")) {
////                productCode = "PL";
////                subProductCode = "STP-PAPL";
////            } else if (ProductType.equalsIgnoreCase("CB")) {
////                productCode = "PL";
////                subProductCode = "STP-CB";
////            }
////            Log.consoleLog(ifr, "productCode=======>" + productCode);
////            Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
//            String productCode = "HL";
//            Log.consoleLog(ifr, "productCode=======>" + productCode);
//            String npaResult = "";
//
//            //Added by Ahmed on 19-02-2024 for independant calculation of NPA,DPD,WRO.
//            //Discussed with Naveen and post than implementation has been put into picture.
//            String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "ACCTTYPE");
//            Log.consoleLog(ifr, "excludedEMIAccnts=>" + excludedEMIAccnts);
//            String excludedNPAAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "NPAACCTTYPE");
//            Log.consoleLog(ifr, "excludedNPAAccnts=>" + excludedNPAAccnts);
//            String excludedDPDAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "DPDACCTTYPE");
//            Log.consoleLog(ifr, "excludedDPDAccnts=>" + excludedDPDAccnts);
//            String excludedWROAccnts = pcm.getParamConfig2(ifr, productCode,"TRANSUNIONCONF", "WROACCTTYPE");
//            Log.consoleLog(ifr, "excludedWROAccnts=>" + excludedWROAccnts);
//            String excludedOwners = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "OWNERTYPE");
//            Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);
//
//            scoreCardAccTypes = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILACCTTYPE");
//            Log.consoleLog(ifr, "scoreCardAccTypes=>" + scoreCardAccTypes);
//            scoreCardAccStatus = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILACCTTYPESTATUS");
//            Log.consoleLog(ifr, "scoreCardAccStatus=>" + scoreCardAccStatus);//SrcPaymentHistory
//            scoreCardownershipIndicator = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILOWNERSHIPINDICATOR");
//            Log.consoleLog(ifr, "scoreCardownershipIndicator=>" + scoreCardownershipIndicator);
//            SrcPaymentHistory = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "CIBILSRCPAYMENTHISTORY");
//            Log.consoleLog(ifr, "SrcPaymentHistory=>" + SrcPaymentHistory);
//            String npaPaymentHistoryTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPAPAYMENTHIST");//SUB,DBT,LSS
//            String npaSuitFiledTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPASUITFILED");//00
//            String dpdMonthCalcTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DPDMONTHCALC");//6
//            String dpdValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DAYSPASTDUE");//31
//            String creditFacilityValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "CRDFACILITY");//31
//            String pHistory6MonthCheckTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "PHIST6MONTHSCHK");
//
//            //Aadhar No Snippet added by Ahmed for dual core check process- Implemented after discussing with Trinath on 07-02-2024
//            String aadharSnippet = "";
//            //Added by Ahmed for getting AadharNo via AadharVault on 12-07-2024
//            //  String mobileNumber = pcm.getMobileNumber(ifr);
//            CustomerAccountSummary cas = new CustomerAccountSummary();
//            HashMap<String, String> customerdetails = new HashMap<>();
//            customerdetails.put("MobileNumber", MobileNumber);
//            customerdetails.put("customerId", customerId);//Added by Ahmed on 31-07-2024
//            aadharNo = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
//            Log.consoleLog(ifr, "Aadhar inside fetching Cibil score:: "+aadharNo);
//            if (aadharNo.contains(RLOS_Constants.ERROR)) {
//                return pcm.returnCustomErrorMessage(ifr, aadharNo);
//            }
//            if ((!aadharNo.equalsIgnoreCase("")) && (!aadharNo.equalsIgnoreCase("{}"))) {
//
//                aadharSnippet = ",{\n"
//                        + "                \"index\": \"I02\",\n"
//                        + "                \"idNumber\": \"" + aadharNo + "\",\n"
//                        + "                \"idType\": \"06\"\n"
//                        + "       }\n";
//
//            }
//            Log.consoleLog(ifr, "Addhar Snippit inside cibil socre generation:: "+ aadharSnippet);
//
//            Request = "{\n"
//                    + "    \"serviceCode\": \"" + serviceCode + "\",\n"
//                    + "    \"monitoringDate\": \"" + MonitoringDate + "\",\n"
//                    + "    \"consumerInputSubject\": {\n"
//                    + "        \"tuefHeader\": {\n"
//                    + "            \"headerType\": \"" + headerType + "\",\n"
//                    + "            \"version\": \"" + version + "\",\n"
//                    + "            \"memberRefNo\": \"" + memberRefNo + "\",\n"
//                    + "            \"gstStateCode\": \"" + gstStateCode + "\",\n"
//                    + "            \"enquiryMemberUserId\": \"" + enquiryMemberUserId + "\",\n"
//                    + "            \"enquiryPassword\": \"" + enquiryPassword + "\",\n"
//                    + "            \"enquiryPurpose\": \"" + enquiryPurpose + "\",\n"
//                    + "            \"enquiryAmount\": \"" + enquiryAmount + "\",\n"
//                    + "            \"scoreType\": \"" + scoreType + "\",\n"
//                    + "            \"outputFormat\": \"" + outputFormat + "\",\n"
//                    + "            \"responseSize\": \"" + responseSize + "\",\n"
//                    + "            \"ioMedia\": \"" + ioMedia + "\",\n"
//                    + "            \"authenticationMethod\": \"" + authenticationMethod + "\"\n"
//                    + "        },\n"
//                    + "        \"names\": [\n"
//                    + "            {\n"
//                    + "                \"index\": \"N01\",\n"
//                    + "                \"firstName\": \"" + firstName + "\",\n"
//                    + "                \"lastName\": \"" + lastName + "\",\n"
//                    + "                \"birthDate\": \"" + ParsedDate + "\",\n"
//                    + "                \"gender\": \"" + gendercode + "\"\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"ids\": [\n"
//                    + "            {\n"
//                    + "                \"index\": \"I01\",\n"
//                    + "                \"idNumber\": \"" + PAN + "\",\n"
//                    + "                \"idType\": \"01\"\n"
//                    + "            }\n"
//                    + aadharSnippet
//                    + "        ],\n"
//                    + "        \"telephones\": [\n"
//                    + "            {\n"
//                    + "                \"index\": \"T01\",\n"
//                    + "                \"telephoneNumber\": \"" + MobileNumber + "\",\n"
//                    + "                \"telephoneType\": \"01\"\n"
//                    + "            }\n"
//                    + "        ],\n"
//                    + "        \"addresses\": [\n"
//                    + "            {\n"
//                    + "                \"index\": \"A01\",\n"
//                    + "                \"line1\": \"" + AddressLine1 + "\",\n"
//                    + "                \"line2\": \"" + AddressLine2 + "\",\n"
//                    + "                \"line3\": \"" + AddressLine3 + "\",\n"
//                    + "                \"line4\": \"" + City + "\",\n"
//                    + "                \"line5\": \"\",\n"
//                    + "                \"stateCode\": \"" + StateCode + "\",\n"//shoudl get from master
//                    + "                \"pinCode\": \"" + Pincode + "\",\n"
//                    + "                \"addressCategory\": \"04\",\n"
//                    + "                \"residenceCode\": \"01\"\n"
//                    + "           }\n"
//                    + "        ],\n"
//                    + "        \"enquiryAccounts\": [\n"
//                    + "            {\n"
//                    + "                \"index\": \"I01\",\n"
//                    + "                \"accountNumber\": \"\"\n"
//                    + "            }\n"
//                    + "        ]\n"
//                    + "    }\n"
//                    + "}";
//
//            Log.consoleLog(ifr, "Requerst before calling cibil:: "+Request);
//            Response = cm.getWebServiceResponse(ifr, apiName, Request);
//            Log.consoleLog(ifr, "Response===> CONSUMERAPI****" + Response);
//
//              cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "", applicantType);
//            if (!Response.equalsIgnoreCase("{}")) {
//                JSONParser parser = new JSONParser();
//                JSONObject resultObj = (JSONObject) parser.parse(Response);
//
//                String body = resultObj.get("body").toString();
//                JSONObject bodyObj = (JSONObject) parser.parse(body);
//
//                String ControlData = bodyObj.get("controlData").toString();
//                Log.consoleLog(ifr, "ControlData==>" + ControlData);
//
//                JSONObject ControlDataObj = (JSONObject) parser.parse(ControlData);
//                String ControlDataStatus = ControlDataObj.get("success").toString();
//
//                Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);
//
//                if (ControlDataStatus.equalsIgnoreCase("true")) {
//
//                    Log.consoleLog(ifr, "cicReportGenReq==>" + cicReportGenReq);
//                    if (cicReportGenReq.equalsIgnoreCase("Y")) {
//                        if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
//                            String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
//                            String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Consumer", encodedB64, "NGREPORTTOOL_TRANSUNION");
//                            Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
//                            cm.updateCICReportStatus(ifr, "CIBIL", generateReportStatus, applicantType);//Added by Ahmed on 26-07-2024 for Status Updation of CIC Report
//                        }
//                    }
//
//                    String BureauScore = "";
//                    int cibilScore = 0;
//                    String totalAccounts = "0";
//                    String Scores = "";
//                    String accounts = "";
//                    String type= "NoCIC";
//                    String consumerCreditData = cf.getJsonValue(bodyObj, "consumerCreditData");
//                    
//                    
//                
//                    JSONArray consumerCreditDataJSON = (JSONArray) parser.parse(consumerCreditData);
//                    if (!consumerCreditDataJSON.isEmpty()) {
//                        for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
//                            String InputString = consumerCreditDataJSON.get(i).toString();
//                            JSONObject consumerCreditDataJSONObj = (JSONObject) parser.parse(InputString);
//                            Scores = consumerCreditDataJSONObj.get("scores").toString();
//                            Log.consoleLog(ifr, "Scores=" + Scores);
//
//                            try {
//                                if (consumerCreditDataJSONObj.containsKey("accounts")) {
//                                	type="";
//                                    accounts = cf.getJsonValue(consumerCreditDataJSONObj, "accounts");
//                                    //  consumerCreditDataJSONObj.get("accounts").toString();
//                                    Log.consoleLog(ifr, "account=" + accounts);
//                                }
//                            } catch (Exception e) {
//                                Log.consoleLog(ifr, "Exception/accounts=" + e);
//                            }
//
//                        }
//                    }
//
//                    Log.consoleLog(ifr, "Scores==>" + Scores);
//                    JSONArray ScoresJSON = (JSONArray) parser.parse(Scores);
//
//                    if (!ScoresJSON.isEmpty()) {
//                        for (int i = 0; i < ScoresJSON.size(); i++) {
//                            String InputString = ScoresJSON.get(i).toString();
//                            JSONObject ScoresJSONObj = (JSONObject) parser.parse(InputString);
//                            String transCIBILScore = ScoresJSONObj.get("score").toString();
//                            Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
//                            cibilScore = Integer.parseInt(transCIBILScore);
//                            Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
//                            BureauScore = String.valueOf(cibilScore);
//                            Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
//                            //added by prakash 02-03-2024 for BureauScore set default as 0 if 000-1
//
//                            if ((BureauScore.equalsIgnoreCase("000-1"))) {
//                                String cbilimmuneAccept = ConfProperty.getCommonPropertyValue("CICImmune_Accept");
//                                Log.consoleLog(ifr, "cbilimmuneAccept==>" + cbilimmuneAccept);
//                                //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
//
//                                if (cbilimmuneAccept.equalsIgnoreCase("YES")) {
//                                    BureauScore = "0";
//                                    transCIBILScore="0";
//                                }
//
//                            }
//                            Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
//                        }
//                    }
//                    if ((Integer.parseInt(BureauScore) > 200)
//                            || (Integer.parseInt(BureauScore) < 100)) {
//                        String consumerSummaryData = cf.getJsonValue(bodyObj, "consumerSummaryData");
//                        JSONObject consumerSummaryDataObj = (JSONObject) parser.parse(consumerSummaryData);
//                        String accountSummary = cf.getJsonValue(consumerSummaryDataObj, "accountSummary");
//                        JSONObject accountSummaryObj = (JSONObject) parser.parse(accountSummary);
//
//                        totalAccounts = cf.getJsonValue(accountSummaryObj, "totalAccounts");
//                        Log.consoleLog(ifr, "totalAccounts==>" + totalAccounts);
//
//                    }
//                    int suitFiledcount = 0;
//
//                    int dpdCount = 0;//dayspastdue
//
//                    int totalEmiAmnt = 0;
//                    int totalNonEmiCount = 0;
//                    String consolidatedtwelveMonthData = "";
//                    Log.consoleLog(ifr, "accounts==>" + accounts);
//                    int count6MonthData = 0;
//                    int total_NPA_INP = 0;
//                    int totalNPA = 0;
//
//                    String paymentHistoryClob = "000";
//                    String dateReportedStr = "";
//                    String typeofcredit = "";
//                    Map<String, String> npaResultCheck = new HashMap<>();
//                    Map<String, String> wrosettResultCheck = new HashMap<>();
//
//                    //Commented by Ahmed on 06-09-2024 after for MoneshKumar
//                    if ((!accounts.equalsIgnoreCase(""))) {
//                        //if (!accounts.equalsIgnoreCase("")) {
//                        JSONArray accountsJSON = (JSONArray) parser.parse(accounts);
//
//                        if (!accountsJSON.isEmpty()) {
//
//                            String query5 = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
//                            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query5, "Execute query for fetching loan selected ");
//                            String loan_selected = loanSelected.get(0).get(0);
//                            Log.consoleLog(ifr, "loan type==>%%" + loan_selected);
//
//                            for (int i = 0; i < accountsJSON.size(); i++) {
//
//                                String InputString = accountsJSON.get(i).toString();
//                                JSONObject accountsJSONObj = (JSONObject) parser.parse(InputString);
//                                dateReportedStr = accountsJSONObj.get("dateReported").toString();
//                                Log.consoleLog(ifr, "conssumerAPI:CIBILScore->dateReported: " + dateReportedStr);
//                                Log.consoleLog(ifr, "overAllAccountTypes" + overAllAccountTypes);
//                                Log.consoleLog(ifr, "overAllcreditFacilityStatus" + overAllcreditFacilityStatus);
//
//                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
//                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();
//                                    String paymentStartDate = accountsJSONObj.get("paymentStartDate").toString();
//
//                                    Log.consoleLog(ifr, "Payment_History_Profile" + payHistory);
//                                    // added by vandana start
//
//                                    Log.consoleLog(ifr, "Payment_History_Profile_Data_Cibil" + payHistory);
//
//                                    String payment = payHistory.replace("?", "0")
//                                            .replace("S", "0").replace("N", "0")
//                                            .replace("B", "0").replace("D", "0")
//                                            .replace("U", "0").replace("T", "0")
//                                            .replace("M", "0").replace("L", "0").replace("X", "0")
//                                            .replace("M", "0").replace("L", "0").replace("X", "0")
//                                            .replace("M", "0").replace("L", "0").replace("X", "0").replace("T", "0");
//
//                                    Log.consoleLog(ifr, "Payment_History_Profile" + payment);
//
//                                    // Starts Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK
//                                    if (loan_selected.equalsIgnoreCase("Canara Pension")
//                                            || loan_selected.equalsIgnoreCase("Canara Budget")
//                                            || loan_selected.equalsIgnoreCase("VEHICLE LOAN")
//                                            && !paymentStartDate.equalsIgnoreCase("")) {
//                                        payment = validatePaymentHistory_CB(ifr, paymentStartDate, payment, "DPD");
//                                        npaResult = validatePaymentHistory_CB(ifr, dateReportedStr, payment, "NPA");
//                                        Log.consoleLog(ifr, "npaResult==>$$" + npaResult);
//
//                                        if (!npaResult.equals("")) {
//
//                                            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
//                                            Log.consoleLog(ifr, "dateReportedStr==>$$" + dateReportedStr);
//                                            Date dateReported = sdf.parse(dateReportedStr);
//                                            Calendar cal = Calendar.getInstance();
//                                            cal.setTime(dateReported);
//                                            cal.add(Calendar.MONTH, 60);
//                                            Date sixtyMonthsDate = cal.getTime();
//                                            Log.consoleLog(ifr, "sixtyMonthsDate==>$$" + sixtyMonthsDate);
//                                            Date today = new Date();
//
//                                            if (today.before(sixtyMonthsDate)) {
//                                                // NPA_INP = "Yes";
//                                                total_NPA_INP += Integer.parseInt(npaResult);
//
//                                                Log.consoleLog(ifr, "dateReported is within 60 months from today::" + sixtyMonthsDate);
//                                            } else {
//                                                totalNPA += Integer.parseInt(npaResult);
//                                                Log.consoleLog(ifr, "dateReported is older than 60 months.:NPA_INP is No:" + sixtyMonthsDate);
//                                            }
//
//                                        }
//
//                                        consolidatedtwelveMonthData = consolidatedtwelveMonthData + payment;
//
//                                        Log.consoleLog(ifr, "consolidatedtwelveMonthData==>$$" + consolidatedtwelveMonthData);
//
//                                    } else {     // end  Added by prakash on 20-09-2024 for adding payment history in IBPS_BUREUCHECK
//
//                                        //-----------------
//                                        if (payment.length() < 3) {
//                                            Log.consoleLog(ifr, "Insufficient data to extract payment history.");
//                                            //return "";//Modified by Ahmed on 18-07-2024
//                                        } else {
//                                            int numberOfMonths = payment.length() / 3;
//                                            Log.consoleLog(ifr, "Payment_History_Profile numberOfMonths" + numberOfMonths);
//
//                                            for (int j = 0; j < numberOfMonths; j++) {
//                                                String twelveMonthData = payment.substring(j * 3, (j * 3) + 3);
//                                                consolidatedtwelveMonthData = consolidatedtwelveMonthData + twelveMonthData;
//                                            }
//                                        }
//                                    }
//                                    Log.consoleLog(ifr, "consolidatedtwelveMonthData" + consolidatedtwelveMonthData);
//                                    paymentHistoryCombined = paymentHistoryCombined + payHistory;
//
//                                    String[] substrings = splitString(payHistory, 3);
//                                    if (substrings.length >= 6) {
//                                        for (int j = 0; j < 6; j++) {
//                                            if (!substrings[j].equalsIgnoreCase("XXX")) {
//                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
//                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, substrings[j]);
//                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
//                                                if (sixMonthchkdata) {
//                                                    count6MonthData++;
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        for (String pHistory : substrings) {
//                                            if (!pHistory.equalsIgnoreCase("XXX")) {
//                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, pHistory);
//                                                Log.consoleLog(ifr, "pHistory/less than 6 months available==>" + pHistory);
//                                                if (sixMonthchkdata) {
//                                                    count6MonthData++;
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                } else {
//                                    Log.consoleLog(ifr, "paymentHistory tag not available.");
//                                }
//                                if (count6MonthData > 0) {
//                                    pHist6monthsChk = "Yes";
//                                }
//                                Log.consoleLog(ifr, "pHist6monthsChk  " + pHist6monthsChk);
//                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
//                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();
//
//                                    //       String firstSixMonths = paymentHistory.substring(0, 18); // Assuming each month is 3 digits, first 6 months are 18 characters
//                                    // Check if first 6 months contain SUB, DBT, or LSS
//                                    //    PaymentHistory = PaymentHistory + firstSixMonths;
//                                }
//
//                                String creditFacilityStatus = "";
//                                String paymentHistory = "";
//                                String suitFiled = "";
//                                String accountType = "";
//                                String dateClosed = "";
//                                String currentBalance = "";
//                                String ownershipIndicator = "";
//                                String dateOpened = "";
//
//                                if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus").equalsIgnoreCase(""))) {
//                                    creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
//                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + creditFacilityStatus;
//                                    Log.consoleLog(ifr, "overAllcreditFacilityStatus==>" + overAllcreditFacilityStatus);
//                                } else {
//                                    Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
//                                }
//                                if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
//                                    accountType = accountsJSONObj.get("accountType").toString();
//                                    overAllAccountTypes = overAllAccountTypes + accountType;
//                                    Log.consoleLog(ifr, "accountType==>" + accountType);
//                                } else {
//                                    Log.consoleLog(ifr, "accountType tag not available.");
//                                }
//                                if (!(cf.getJsonValue(accountsJSONObj, "ownershipIndicator").equalsIgnoreCase(""))) {
//                                    ownershipIndicator = accountsJSONObj.get("ownershipIndicator").toString();
//                                    overALLownershipIndicator = overALLownershipIndicator + ownershipIndicator;
//                                    Log.consoleLog(ifr, "ownershipIndicator==>" + ownershipIndicator);
//                                } else {
//                                    Log.consoleLog(ifr, "accountType tag not available.");
//                                }
//                                if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
//                                    dateOpened = accountsJSONObj.get("dateOpened").toString();
//                                    overALLdateOpened = overALLdateOpened + dateOpened;
//                                    Log.consoleLog(ifr, "dateOpened==>:::===>$$$" + dateOpened);
//                                } else {
//                                    Log.consoleLog(ifr, "accountType tag not available.");
//                                }
//                                if (!overAllAccountTypes.equalsIgnoreCase("")) {
//                                    overAllAccountTypes = overAllAccountTypes + ",";
//                                }
//
//                                if (!overAllcreditFacilityStatus.equalsIgnoreCase("")) {
//                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + ",";
//                                }
//                                if (!overALLownershipIndicator.equalsIgnoreCase("")) {
//                                    overALLownershipIndicator = overALLownershipIndicator + ",";
//                                }
//                                if (!overALLdateOpened.equalsIgnoreCase("")) {
//                                    overALLdateOpened = overALLdateOpened + ",";
//                                    Log.consoleLog(ifr, "overALLdateOpened==>$$$" + overALLdateOpened);
//
//                                }
//                                if (!PaymentHistory.equalsIgnoreCase("")) {
//                                    PaymentHistory = PaymentHistory + ",";//overALLdateOpened
//                                }
//
//                                if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
//                                    currentBalance = accountsJSONObj.get("currentBalance").toString();
//                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//
//                                    if (currentBalance.equalsIgnoreCase("")) {
//                                        currentBalance = "0";
//                                    }
//
//                                } else {
//                                    Log.consoleLog(ifr, "currentBalance tag not available.");
//                                }
//
//                                if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
//                                    dateClosed = accountsJSONObj.get("dateClosed").toString();
//                                    Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
//                                } else {
//                                    Log.consoleLog(ifr, "dateClosed tag not available.");
//                                }
//
//                                Log.consoleLog(ifr, "emiStatus==>&&&" + ownershipIndicator);
//                                boolean emiStatus = checkEMIAccFilterStatus(ifr,
//                                        accountType, currentBalance, dateClosed,
//                                        excludedEMIAccnts,
//                                        ownershipIndicator, excludedOwners);
//                                Log.consoleLog(ifr, "emiStatus==>" + emiStatus);
//
//                                //For NPA
//                                boolean npaStatuscheck = checkKnockOffAccFilterStatusNPA(ifr, accountType,
//                                        excludedNPAAccnts);
//                                Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:npaStatuscheck==>" + npaStatuscheck);
//                                if (npaStatuscheck) {
//
//                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
//                                        String paymentHistoryNPA = accountsJSONObj.get("paymentHistory").toString();
//                                        Log.consoleLog(ifr, "paymentHistoryNPA==>" + paymentHistoryNPA);
//
//                                        Map<String, String> NPACheck = NPACheckfromCibil(ifr, paymentHistoryNPA,
//                                                dateReportedStr, ownershipIndicator, npaPaymentHistoryTags);
//                                        Log.consoleLog(ifr, "NPACheck==>" + NPACheck);
//
//                                        // Merge results: keep "Yes" if it has already been found
//                                        for (Map.Entry<String, String> entry : NPACheck.entrySet()) {
//                                            String key = entry.getKey();
//                                            String value = entry.getValue();
//
//                                            // If already "Yes", retain it; otherwise update
//                                            String existing = npaResultCheck.getOrDefault(key, "No");
//                                            npaResultCheck.put(key, existing.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("Yes") ? "Yes" : "No");
//                                        }
//
//                                        Log.consoleLog(ifr, "npaResultCheck==>" + npaResultCheck);
//                                    } else {
//                                        Log.consoleLog(ifr, "paymentHistory is empty==>" + paymentHistory);
//                                    }
//
//                                    if (!(cf.getJsonValue(accountsJSONObj, "suitFiled").equalsIgnoreCase(""))) {
//                                        suitFiled = accountsJSONObj.get("suitFiled").toString();
//                                        Log.consoleLog(ifr, "suitFiled==>" + suitFiled);
//                                    } else {
//                                        Log.consoleLog(ifr, "suitFiled tag not available.");
//                                    }
//
//                                    //Suit Filed should not be 00 and it should not be empty- As per Naveen`s Mail
//                                    if (!npaSuitFiledTags.contains(suitFiled) && (!suitFiled.equalsIgnoreCase(""))) {
//                                        suitFiledcount++;
//                                    }
//                                }
//
//                                //For DPD
//                                boolean dpdStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedDPDAccnts, dateClosed);
//                                Log.consoleLog(ifr, "dpdStatus==>" + dpdStatus);
//
//                                if (dpdStatus) {
//                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
//                                        paymentHistory = accountsJSONObj.get("paymentHistory").toString();
//                                        Log.consoleLog(ifr, "paymentHistory==>" + paymentHistory);
//
//                                        String[] substrings = splitString(paymentHistory, 3);
//
//                                        int dpdmonthcounttag = 1;
//                                        for (String pHistoryTag : substrings) {
//                                            Log.consoleLog(ifr, "pHistoryTag " + pHistoryTag);
//                                            if (dpdmonthcounttag <= Integer.parseInt(dpdMonthCalcTags)) {
//                                                if (!pHistoryTag.equalsIgnoreCase("XXX")) {
//
//                                                    try {
//                                                        if (Integer.parseInt(pHistoryTag) >= Integer.parseInt(dpdValueTags)) {
//                                                            dpdCount++;
//                                                        }
//                                                    } catch (Exception e) {
//                                                        Log.consoleLog(ifr, "Exception where pHistoryTag is an alphaber and not numeric. So here DPDCount is skipped");
//                                                    }
//
//                                                }
//                                                dpdmonthcounttag++;
//                                            }
//                                            Log.consoleLog(ifr, "dpdfirst6monthcounttag " + dpdmonthcounttag);
//                                        }
//
//                                    } else {
//                                        Log.consoleLog(ifr, "paymentHistory tag not available.");
//                                    }
//
//                                } else {
//                                    Log.consoleLog(ifr, "DPD Account Type Excluded. Please check config master. Looping next..");
//                                }
//
//                                //for Writeoff Setttled Restructred
//                                boolean WroSetResStatus = checkKnockOffAccFilterStatus_WSR(ifr, accountType, excludedWROAccnts);
//                                Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:WroSetResStatus==>" + WroSetResStatus);
//                                if (WroSetResStatus) {
//                                    if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus")
//                                            .equalsIgnoreCase(""))) {
//                                        creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
//                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:creditFacilityStatus==>" + creditFacilityStatus);
//                                        Map<String, String> WroSetResCheck = WroSetResCheckfromCibil(ifr,
//                                                creditFacilityStatus, dateReportedStr,
//                                                ownershipIndicator);
//                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:WroSetResCheck==>" + WroSetResCheck);
//                                        // Merge logic to retain "Yes" once it's found
//                                        for (Map.Entry<String, String> entry : WroSetResCheck.entrySet()) {
//                                            String key = entry.getKey();
//                                            String value = entry.getValue();
//                                            String existing = wrosettResultCheck.getOrDefault(key, "No");
//                                            wrosettResultCheck.put(key, existing.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("Yes") ? "Yes" : "No");
//                                        }
//
//                                        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:wrosettResultCheck==>" + wrosettResultCheck);
//
//                                    } else {
//                                        Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
//                                    }
//
//                                } else {
//                                    Log.consoleLog(ifr, "WRO Account Type Excluded. Please check config master. Looping next..");
//                                }
//
//                                //For EMI Block
//                                //Modified by Ahmed on 03-06-2024 for performing Total Non EMI cals
//                                if (emiStatus) {
//                                    String emiAmount = "0";
//                                    if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
//                                        emiAmount = accountsJSONObj.get("emiAmount").toString();
//                                        Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
//                                        if (!emiAmount.equalsIgnoreCase("")) {
//                                            totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
//                                        } else {
//                                            Log.consoleLog(ifr, "emiAmount tag available but empty");
//                                            if (Integer.parseInt(currentBalance) > 0) {
//                                                totalNonEmiCount++;
//                                            }
//                                        }
//                                    } else {
//                                        Log.consoleLog(ifr, "emiAmount tag not available.");
//
//                                        if (Integer.parseInt(currentBalance) > 0) {
//                                            totalNonEmiCount++;
//                                        }
//                                    }
//                                } else {
//                                    Log.consoleLog(ifr, "EMI Account Type Excluded. "
//                                            + "Please check config master. Looping next..");
//                                }
//                            }//End of Accounts Loop                           
//
//                        }
//
//                    }
//
//                    String DPD = "0";
//                    //dpd
//                    if (dpdCount > 0) {
//                        DPD = String.valueOf(dpdCount);
//                    }
//
//                    Log.consoleLog(ifr, "DPD==>" + DPD);
//                    String NPA = "No";//field name--> CICNPACHECK //npa borrower
//                    String NPAG = "No";////field name--> GUARANTORNPAINP //npa GUARANTOR
//                    String npaWroSettSixtyI = "No";////field name--> SRNPAINP //npa 60 check borrower
//                    String NPASixtyG = "No";////field name--> NPACOUNT_CB //npa 60 check 
//                    Log.consoleLog(ifr, "npaResultCheck check==>" + npaResultCheck);
//
//                    NPA = npaResultCheck.get("NPAI");//field name--> CICNPACHECK //npa borrower
//                    NPAG = npaResultCheck.get("NPAG");////field name--> GUARANTORNPAINP //npa GUARANTOR
//                    NPASixtyG = npaResultCheck.get("NPASixtyMonthG");////field name--> NPACOUNT_CB //npa 60 check 
//
//                    String wro = "No";////field name--> WRITEOFF
//                    String Sett = "No";////field name--> SETTLEDHISTORY
//                    String res = "No";
//                    String wrosettresG = "No";////field name--> GUARANTORWRITEOFFSETTLEDHIST
//                    String wroSettRessixtyG = "No";
//                    Log.consoleLog(ifr, "wrosettResultCheck check==>" + wrosettResultCheck);
//
//                    wro = wrosettResultCheck.getOrDefault("wroI", "No");////field name--> WRITEOFF
//                    Sett = wrosettResultCheck.getOrDefault("settI", "No");
//                    res = wrosettResultCheck.getOrDefault("resI", "No");
//                    wrosettresG = wrosettResultCheck.getOrDefault("wrosettresG", "No");
//                    wroSettRessixtyG = wrosettResultCheck.getOrDefault("wrosettresSixtyG", "No");
//
//                    if ("Yes".equalsIgnoreCase(npaResultCheck.getOrDefault("NPASixtyMonthI", "No"))
//                            || "Yes".equalsIgnoreCase(wrosettResultCheck.getOrDefault("wrosettresSixtyI", "No"))) {
//                        npaWroSettSixtyI = "Yes";
//                    }
//                    Log.consoleLog(ifr, "BureauScore==>" + BureauScore);
//                    Log.consoleLog(ifr, "paymentHistoryCombined==>" + consolidatedtwelveMonthData);
//                    Log.consoleLog(ifr, "NPA Results => NPAI: " + NPA + ", NPAG: " + NPAG + ", NPASixtyMonthI: " + npaWroSettSixtyI + ", NPASixtyMonthG: " + NPASixtyG
//                            + " | WRO/SETT/RES Results => wroI: " + wro + ", settI: " + Sett + ", resI: " + res
//                            + ", wrosettresG: " + wrosettresG + ", wrosettresSixtyI: " + npaWroSettSixtyI + ", wrosettresSixtyG: " + wroSettRessixtyG);
//
//                    Log.consoleLog(ifr, "dpdCount " + dpdCount);
//                    Log.consoleLog(ifr, "totalNonEmiCount==>" + totalNonEmiCount);
//                    Log.consoleLog(ifr, "overAllAccountTypes  " + overAllAccountTypes);
//                    Log.consoleLog(ifr, "overAllAccountStatus  " + overAllcreditFacilityStatus);
//                    Log.consoleLog(ifr, "overALLownershipIndicator  " + overALLownershipIndicator);
//
//                    //If-Else Condition Removed for PAPL / Other Journerys until Impact is derived.
//                    String newToCredit = "Yes";
//                    String lapExists = "No";//Need to derive the count as per the Factors
//                    if (Integer.parseInt(totalAccounts) > 0) {
//                        newToCredit = "No";
//                    }
//
//                    String qryCICDataUpdate1 = "";
//                    String qryCICDataUpdate2 = "";
//                    if(type.equalsIgnoreCase("NoCIC"))
//                    {
//                    	totalEmiAmnt=Integer.parseInt("0");
//                    }
//                    qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,"
//                            + " EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,"
//                            + " PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
//                            + " GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,NPACOUNT_CB,APPLICANT_UID) "
//                            + "values('" + ProcessInstanceId + "','" + BureauScore + "','" + NPA + "','" + DPD + "','"
//                            + "" + wro + "','CB','" + totalEmiAmnt + "'," + paymentHistoryClob + ",'" + applicantType
//                            + "','" + totalNonEmiCount + "','" + Sett + "','" + npaWroSettSixtyI + "','"
//                            + NPAG + "','" + wrosettresG + "',SYSDATE,'"
//                            + NPASixtyG + "','" + insertionOrderId + "')";
//                    Log.consoleLog(ifr, "qryCICDataUpdate1:" + qryCICDataUpdate1);
//                    ifr.saveDataInDB(qryCICDataUpdate1);
//                    // } else {
////                    qryCICDataUpdate2 = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
////                            + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
////                            + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
////                            + "CIC_SCORE,NPA_STATUS,DPD,WRITEOFF_STATUS,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
////                            + "VALUES('" + ProcessInstanceId + "',"
////                            + "'','" + MobileNumber + "','" + productCode + "','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderId + "',\n"
////                            + "'EXT','EX','','" + lapExists + "','" + BureauScore + "',"
////                            + "'" + NPA + "','" + DPD + "','" + wro + "','" + totalEmiAmnt + "','" + newToCredit + "',SYSDATE,SYSDATE)";
//                    // }
////                    Log.consoleLog(ifr, "qryCICDataUpdate2:" + qryCICDataUpdate2);
////                    ifr.saveDataInDB(qryCICDataUpdate2);
////                    if (!productCode.equalsIgnoreCase("MF")) {
////                        CRGGenerator crg = new CRGGenerator();
////                        Log.consoleLog(ifr, "@@@ConsumerAPI:getConsumerCIBILScore==>insertionOrderId@@@" + insertionOrderId);
////                        crg.crgGenCibil(ifr, bodyObj, ProcessInstanceId, apiName, applicantType, type,insertionOrderId);
////                        //cm.generateTransunionReport(ifr, ProcessInstanceId, "CONSUMER", Response);
////                    }
//                    return BureauScore;
//                } else {
//                    return RLOS_Constants.ERROR;
//                }
//
//            } else {
//                Response = "No response from Server.";
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception/getConsumerCIBILScore==>" + e);
//        } finally {
//            /*cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response,
//                    apiErrorCode, apiErrorMessage, apiStatus);*/
//            Log.consoleLog(ifr, "finally==>bLOCKfinally");
//            if (!Request.isEmpty()) {
//                cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "", (!insertionOrderId.equalsIgnoreCase("") ? insertionOrderId : applicantType));
//            }
//
//        }
//
//        return RLOS_Constants.ERROR;
//
//    }
    
    public String validatePaymentHistory_CB(IFormReference ifr, String paymentStartDateStr,
            String payment, String type) {
        try {
            String monthsFromConstant = "";
            String NPAcheck = "";
            int countLSS = 0;
            int countSUM = 0;
            int countDBT = 0;
            int totalCount = 0; // To hold the total count
            int MONTHS = 0;
            Log.consoleLog(ifr, " Replace payment==>" + payment);
            payment = payment.replaceAll("[a-zA-Z]", "0");
            Log.consoleLog(ifr, "payment==>" + payment);
            if (type.equalsIgnoreCase("DPD")) {
                monthsFromConstant = pcm.getConstantValue(ifr, "TRANSUNION", "MONNTH_DPD");//12
                MONTHS = Integer.parseInt(monthsFromConstant);
            } 

            long monthsDifferenceResult = 0;
            Log.consoleLog(ifr, "validatePaymentHistory:::" + paymentStartDateStr);

            SimpleDateFormat inputFormatter = new SimpleDateFormat("ddMMyyyy");
            SimpleDateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");

            Date paymentStartDate = inputFormatter.parse(paymentStartDateStr);
            String formattedPaymentStartDate = outputFormatter.format(paymentStartDate);
            Log.consoleLog(ifr, "Formatted Payment Start Date: " + formattedPaymentStartDate);

            Date currentDate = new Date();
            String formattedCurrentDate = outputFormatter.format(currentDate);
            Log.consoleLog(ifr, "Current Date: " + formattedCurrentDate);

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(paymentStartDate);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);

            int yearsDifference = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
            int monthsDifference = yearsDifference * 12 + currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
            Log.consoleLog(ifr, "Difference in months: " + monthsDifference);
            Log.consoleLog(ifr, "MONTHS: " + MONTHS);
            String sixtymonthNPA = "";
            if (monthsDifference <= MONTHS) {
                if (monthsDifference == MONTHS) {
                    monthsDifference = MONTHS;
                } else {
                    monthsDifference = MONTHS - monthsDifference;
                    Log.consoleLog(ifr, "monthsDifference-: " + MONTHS + "::" + monthsDifference);

                }
                monthsDifferenceResult = (long) (monthsDifference * 3);
                Log.consoleLog(ifr, "Result after multiplication by 3: " + monthsDifferenceResult);
            } else if (monthsDifference > MONTHS && type.equalsIgnoreCase("NPA")) {
                if (monthsDifference == MONTHS) {
                    monthsDifference = MONTHS;
                } else {
                    //monthsDifference = MONTHS - monthsDifference;
                    monthsDifference = monthsDifference - MONTHS;
                    Log.consoleLog(ifr, "monthsDifference-: " + MONTHS + "::" + monthsDifference);

                }

                monthsDifferenceResult = (long) (monthsDifference * 3);
                Log.consoleLog(ifr, "Result after multiplication by 3: " + monthsDifferenceResult);
                sixtymonthNPA = "Yes";
            } else {
                Log.consoleLog(ifr, "No multiplication as difference is more than 12 months.");
                payment = "000";
                return payment;
            }
            if (monthsDifference * 3 > payment.length()) {
                Log.consoleLog(ifr, "ConsumerAPI--> same payment");
                //payment = payment;
            } else {

                payment = payment.substring(0, (int) monthsDifferenceResult);
            }

            if (type.equalsIgnoreCase("NPA")) {
                // Count occurrences of LSS, SUM, and DBT
                countLSS = countOccurrences(payment, "LSS");
                countSUM = countOccurrences(payment, "SUB");
                countDBT = countOccurrences(payment, "DBT");

                // Calculate the total count
                totalCount = countLSS + countSUM + countDBT;

                // Combine the counts and total count into a single string to return
                payment = "LSS: " + countLSS + ", SUM: " + countSUM + ", DBT: " + countDBT + ", Total: " + totalCount;
                Log.consoleLog(ifr, "validatePaymentHistory==> LSS, SUM, DBT count: " + payment);
                //payment = String.valueOf(totalCount) + sixtymonthNPA;

                payment = String.valueOf(totalCount);

            }

            Log.consoleLog(ifr, "validatePaymentHistory==>" + payment);

        } catch (Exception e) {
            Log.consoleLog(ifr, "validatePaymentHistory==> for ConsumerAPI Exception::" + e);
        }
        return payment;
    }

	private boolean checkEMIAccFilterStatus(IFormReference ifr,
            String accountType, String currentBalance,
            String dateClosed, String excludedAccnts, String ownerType, String excludedOwners) {

        Log.consoleLog(ifr, "#checkEMIAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);
        Log.consoleLog(ifr, "ownerType=======>" + ownerType);
        Log.consoleLog(ifr, "excludedOwners==>" + excludedOwners);
        Log.consoleLog(ifr, "excludedAccnts==>" + excludedAccnts);
        Log.consoleLog(ifr, "ownerType==>&&&" + ownerType);

        String[] excludedAccounts = excludedAccnts.split(",");

        String[] excludedOwnerTypes = excludedOwners.split(",");
        for (String typeOfOwner : excludedOwnerTypes) {
            if (typeOfOwner.equals(ownerType)) {
                Log.consoleLog(ifr, "excludedOwnerTypes===> " + typeOfOwner);
                return false;
            }
        }

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "excludedAccounts===> CB" + accnt);
                return false;
            }
        }
//        if (excludedAccnts.contains(accountType)) {
//            return false;
//        }

        boolean isValidDateFormat = false;
        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "ddMMyyyy";

            isValidDateFormat = isValidDateFormat(dateClosed, dateFormat);
            Log.consoleLog(ifr, "IsValidDateFormat" + isValidDateFormat);

        }

        if (((currentBalance.equalsIgnoreCase("0")) && (!currentBalance.equalsIgnoreCase("")))
                || (isValidDateFormat == true)) {
            return false;
        }

        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }
	
	private int countOccurrences(String text, String substring) {
	    int count = 0;
	    int index = 0;

	    while ((index = text.indexOf(substring, index)) != -1) {
	        count++;
	        index += substring.length(); // Move past the found substring
	    }

	    return count;
	}
	 
    
    private boolean checkKnockOffAccFilterStatusNPA(IFormReference ifr, String accountType,
            String excludedAccnts) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatusNPA started...");
        Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:accountType=====>" + accountType);

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

        String[] excludedAccntsSplitter = excludedAccnts.split(",");
        for (String exAccount : excludedAccntsSplitter) {
            Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:exAccount====>" + exAccount);
            Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:accountType==>" + accountType);
            if (exAccount.equalsIgnoreCase(accountType)) {
                Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:exAccoun mactching with AccountType");
                return false;
            }
        }

        Log.consoleLog(ifr, "checkKnockOffAccFilterStatusNPA:Filter Condition not satisfied..");
        return true;

    }
     
    
    private Map<String, String> WroSetResCheckfromCibil(IFormReference ifr, String creditFacilityStatus,
            String dateReportedStr, String ownershipIndicator) {

        Log.consoleLog(ifr, "WroSetResCheckfromCibil :: Inside Method");

        int wroI = 0;
        int wrosettresG = 0;
        int settI = 0;
        int resI = 0;
        String wrosettresSixtyI = "No";
        String wrosettresSixtyG = "No";

        try {
            Log.consoleLog(ifr, "WroSetResCheckfromCibil :: creditFacilityStatus: " + creditFacilityStatus);
            Log.consoleLog(ifr, "WroSetResCheckfromCibil :: dateReportedStr: " + dateReportedStr);
            Log.consoleLog(ifr, "WroSetResCheckfromCibil :: ownershipIndicator: " + ownershipIndicator);

            String monthsToCheckStr = pcm.getParamValue(ifr, "TRANSUNIONCONF", "MONTHSTOCHECK");
            String ownershipI = pcm.getParamValue(ifr, "TRANSUNIONCONF", "OwnerShipIndicatorI");
            String ownershipG = pcm.getParamValue(ifr, "TRANSUNIONCONF", "OwnerShipIndicatorG");

            int monthsToCheck = Integer.parseInt(monthsToCheckStr);
            Log.consoleLog(ifr, "WroSetResCheckfromCibil :: monthsToCheck: " + monthsToCheck);

            boolean isOwnershipI = ownershipIndicator.equalsIgnoreCase(ownershipI);
            boolean isOwnershipG = ownershipIndicator.equalsIgnoreCase(ownershipG);

            if (!creditFacilityStatus.isEmpty()) {
                switch (creditFacilityStatus) {
                    case "02": // WRO
                        if (isOwnershipI) {
                            wroI++;
                        } else if (isOwnershipG) {
                            wrosettresG++;
                        }
                        break;
                    case "03": // Settlement
                        if (isOwnershipI) {
                            settI++;
                        } else if (isOwnershipG) {
                            wrosettresG++;
                        }
                        break;
                    case "00": // Restructured
                        if (isOwnershipI) {
                            resI++;
                        } else if (isOwnershipG) {
                            wrosettresG++;
                        }
                        break;
                    default:
                        break;
                }

                if (!dateReportedStr.isEmpty()
                        && Arrays.asList("00", "02", "03").contains(creditFacilityStatus)) {

                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                    Date reportedDate = sdf.parse(dateReportedStr);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(reportedDate);
                    cal.add(Calendar.MONTH, monthsToCheck);  // adds 60 months (or whatever configured)
                    Date validTillDate = cal.getTime();

                    Log.consoleLog(ifr, "WroSetResCheckfromCibil :: validTillDate: " + validTillDate);

                    if (new Date().before(validTillDate)) {
                        if (isOwnershipI) {
                            wrosettresSixtyI = "Yes";
                        } else if (isOwnershipG) {
                            wrosettresSixtyG = "Yes";
                        }
                    }
                }

            } else {
                Log.consoleLog(ifr, "WroSetResCheckfromCibil :: creditFacilityStatus is empty");
            }

        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "WroSetResCheckfromCibil :: Exception: " + e);
        } catch (java.text.ParseException ex) {
            Logger.getLogger(ConsumerAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        Map<String, String> result = new HashMap<>();
        result.put("wroI", wroI > 0 ? "Yes" : "No");
        result.put("settI", settI > 0 ? "Yes" : "No");
        result.put("resI", resI > 0 ? "Yes" : "No");
        result.put("wrosettresG", wrosettresG > 0 ? "Yes" : "No");
        result.put("wrosettresSixtyI", wrosettresSixtyI);
        result.put("wrosettresSixtyG", wrosettresSixtyG);

        Log.consoleLog(ifr, "WroSetResCheckfromCibil :: Final Counts => " + result);

        return result;
    }
    
    private boolean checkKnockOffAccFilterStatus_WSR(IFormReference ifr, String accountType,
            String excludedAccnts) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatus_WSR started...");
        Log.consoleLog(ifr, "checkKnockOffAccFilterStatus_WSR:accountType=====>" + accountType);

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

        String[] excludedAccntsSplitter = excludedAccnts.split(",");
        for (String exAccount : excludedAccntsSplitter) {
            Log.consoleLog(ifr, "exAccount====>" + exAccount);
            Log.consoleLog(ifr, "accountType==>" + accountType);
            if (exAccount.equalsIgnoreCase(accountType)) {
                Log.consoleLog(ifr, "exAccoun mactching with AccountType");
                return false;
            }
        }

        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }
    private Map<String, String> NPACheckfromCibil(IFormReference ifr, String PaymentHistory, String dateReportedStr,
            String OwnerShipIndicator, String npaPaymentHistoryTags) throws java.text.ParseException {

        int npaCountI = 0, npaCountG = 0;
        String npaSixtyMonthI = "No", npaSixtyMonthG = "No";

        try {
            Log.consoleLog(ifr, "NPACheckfromCibil:: Started");
            Log.consoleLog(ifr, "NPACheckfromCibil:: PaymentHistory:: " + PaymentHistory);
            Log.consoleLog(ifr, "NPACheckfromCibil:: dateReportedStr:: " + dateReportedStr);
            Log.consoleLog(ifr, "NPACheckfromCibil:: OwnerShipIndicator:: " + OwnerShipIndicator);
            Log.consoleLog(ifr, "NPACheckfromCibil:: npaPaymentHistoryTags:: " + npaPaymentHistoryTags);

            String Monthstocheck = pcm.getParamValue(ifr, "TRANSUNIONCONF", "MONTHSTOCHECK");  // 60
            String OwnerShipIndicatorI = pcm.getParamValue(ifr, "TRANSUNIONCONF", "OwnerShipIndicatorI"); // 1
            String OwnerShipIndicatorG = pcm.getParamValue(ifr, "TRANSUNIONCONF", "OwnerShipIndicatorG"); // 3
            int Monthstocheckint = Integer.parseInt(Monthstocheck);

            Log.consoleLog(ifr, "NPACheckfromCibil:: Monthstocheck:: " + Monthstocheck);
            Log.consoleLog(ifr, "NPACheckfromCibil:: Monthstocheckint:: " + Monthstocheckint);
            Log.consoleLog(ifr, "NPACheckfromCibil:: OwnerShipIndicatorI:: " + OwnerShipIndicatorI);
            Log.consoleLog(ifr, "NPACheckfromCibil:: OwnerShipIndicatorG:: " + OwnerShipIndicatorG);

            // Payment history check
            if (!PaymentHistory.isEmpty()) {
                String[] substrings = splitString(PaymentHistory, 3);
                Log.consoleLog(ifr, "NPACheckfromCibil:: Substrings:: " + Arrays.toString(substrings));

                for (String pHistory : substrings) {
                    Log.consoleLog(ifr, "NPACheckfromCibil:: Checking pHistory:: " + pHistory);

                    if (npaPaymentHistoryTags.contains(pHistory)) {
                        if (OwnerShipIndicator.equalsIgnoreCase(OwnerShipIndicatorI)) {
                            npaCountI++;
                            Log.consoleLog(ifr, "NPACheckfromCibil:: NPA found for Individual");
                        } else if (OwnerShipIndicator.equalsIgnoreCase(OwnerShipIndicatorG)) {
                            npaCountG++;
                            Log.consoleLog(ifr, "NPACheckfromCibil:: NPA found for Guarantor");
                        } else {
                            Log.consoleLog(ifr, "NPACheckfromCibil:: OwnershipIndicator not relevant");
                        }

                        // 60 month check based on reported date
                        if (!dateReportedStr.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                            Date reportedDate = sdf.parse(dateReportedStr);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(reportedDate);
                            cal.add(Calendar.MONTH, Monthstocheckint);  // Add the months to the reported date
                            Date sixtyMonthsDate = cal.getTime();

                            Date today = new Date();

                            Log.consoleLog(ifr, "NPACheckfromCibil:: SixtyMonthsDate:: " + sixtyMonthsDate);

                            // Compare if today is within the 60 months from the reported date
                            if (today.before(sixtyMonthsDate)) {
                                if (OwnerShipIndicator.equalsIgnoreCase(OwnerShipIndicatorI)) {
                                    npaSixtyMonthI = "Yes"; // Return "Yes" if within 60 months
                                    Log.consoleLog(ifr, "NPACheckfromCibil:: dateReported within 60 months for Individual");
                                } else if (OwnerShipIndicator.equalsIgnoreCase(OwnerShipIndicatorG)) {
                                    npaSixtyMonthG = "Yes"; // Return "Yes" if within 60 months
                                    Log.consoleLog(ifr, "NPACheckfromCibil:: dateReported within 60 months for Guarantor");
                                } else {
                                    Log.consoleLog(ifr, "NPACheckfromCibil:: OwnershipIndicator not relevant for 60 month check");
                                }
                            } else {
                                Log.consoleLog(ifr, "NPACheckfromCibil:: dateReported older than 60 months");
                            }
                        }
                    }
                }

            } else {
                Log.consoleLog(ifr, "NPACheckfromCibil:: PaymentHistory is empty");
            }

        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "NPACheckfromCibil:: Exception:: " + e);
        }

        Map<String, String> result = new HashMap<>();
        result.put("NPAI", npaCountI > 0 ? "Yes" : "No");
        result.put("NPAG", npaCountG > 0 ? "Yes" : "No");
        result.put("NPASixtyMonthI", npaSixtyMonthI);  // Return "Yes" or "No"
        result.put("NPASixtyMonthG", npaSixtyMonthG);  // Return "Yes" or "No"
        Log.consoleLog(ifr, "NPACheckfromCibil :: Final Counts => " + result);
        return result;
    }


    public String getConsumerCIBILScore(IFormReference ifr, String ProductType,
            String enquiryAmount, String aadharNo, String applicantType) throws ParseException {

        Log.consoleLog(ifr, "#getConsumerCIBILScore starting...");

        String apiName = "CIBIL";
        Log.consoleLog(ifr, "apiName==>" + apiName);

        try {

            String paymentHistoryCombined = "";
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDateTime now = LocalDateTime.now();
            String MonitoringDate = dtf.format(now);
            Log.consoleLog(ifr, "MonitoringDate==>" + MonitoringDate);

            String MobileNumber = pcm.getMobileNumber(ifr);

            //String enquiryAmount = "";
            String firstName = "";
            String lastName = "";
            String birthDate = "";
            String gender = "";
            String gendercode = "";
            String ParsedDate = "";
            String PAN = "";
            String scoreCardAccTypes = "";
            String overAllAccountTypes = "";
            String overAllcreditFacilityStatus = "";
            String overALLownershipIndicator = "";
            String scoreCardAccStatus = "";
            String scoreCardAccType = "";
            String scoreCardAccStatu = "";
            String scoreCardownershipIndicator = "";
            String scoreCardOwnerShip = "";
            String overALLdateOpened = "";
            String OwnerShipIndicator = "";
            //  String settledHistory = "";
            String pHist6monthsChk = "No";
            //String SrcPaymentHistory = "", PaymentHistory = "", NPA_INP = "", GUARANTORNPA_INP = "", WRITEOFF_INP = "", GUARANTORWRITEOFFSETTLEDHIST_INP = "";
            String SrcPaymentHistory = "", PaymentHistory = "", WRITEOFF_INP = "";
            String AddressLine1 = "", AddressLine2 = "", AddressLine3 = "", City = "",
                    State = "", Pincode = "", StateCode = "";

            String Query1 = "";
            String Query2 = "";
            String customerId = "";

            if (ProductType.equalsIgnoreCase("PAPL")) {
                //Modified by Ahmed on 31-07-2024 for CustomerId Combination logic for CAS API
//                Query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER "
//                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

                Query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,customerid "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

                Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

            } else {
//Modified by Ahmed on 31-07-2024 for CustomerId Combination logic  for CAS API
//                Query1 = "select a.firstname,a.lastname,b.kyc_no,to_char(a.dob,'dd-MM-YYYY')dob,a.gender from los_l_basic_info_i a, LOS_NL_KYC b where a.f_key in (\n"
//                        + "select f_key from los_nl_basic_info where PID = '" + ProcessInstanceId + "'\n"
//                        + ") and a.f_key=b.f_key";
//                Query1 = "select a.firstname,a.lastname,b.kyc_no,to_char(a.dob,'dd-MM-YYYY')dob,a.gender from los_l_basic_info_i a, LOS_NL_KYC b,\n"
//                        + "los_nl_basic_info c where a.f_key=b.f_key\n"
//                        + "and a.f_key=c.f_key and c.pid='" + ProcessInstanceId + "' "
//                        + "and c.applicanttype='" + applicantType + "'";
                Query1 = "select a.firstname,a.lastname,b.kyc_no,to_char(a.dob,'dd-MM-YYYY')dob,a.gender,c.customerid from los_l_basic_info_i a, LOS_NL_KYC b,\n"
                        + "los_nl_basic_info c where a.f_key=b.f_key\n"
                        + "and a.f_key=c.f_key and c.pid='" + ProcessInstanceId + "' "
                        + "and c.applicanttype='" + applicantType + "'";

                Query2 = "select line1,line2,line3,city_town_village,state,pincode "
                        + "from LOS_NL_Address where F_KEY =(select F_KEY from los_nl_basic_info "
                        + " where PID ='" + ProcessInstanceId + "' and applicanttype='" + applicantType + "')\n"
                        + " and addresstype='P'";

            }

            Log.consoleLog(ifr, "Query1==>" + Query1);

            List< List< String>> Result = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());

            if (Result.size() > 0) {
                firstName = Result.get(0).get(0);
                lastName = Result.get(0).get(1);

                if (lastName.equalsIgnoreCase("{}")) {
                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
                }
                if (lastName.equalsIgnoreCase("{")) {
                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
                }
                if (lastName.equalsIgnoreCase("}")) {
                    lastName = "";//Handled w..r.t Production usecase on 13/02/2024
                }

                PAN = Result.get(0).get(2);
                birthDate = Result.get(0).get(3);
                gender = Result.get(0).get(4);

                customerId = Result.get(0).get(5);//Added by Ahmed on 31-07-2024
            }

            if (!birthDate.equalsIgnoreCase("")) {
                DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date = sdf.parse(birthDate);
                ParsedDate = new SimpleDateFormat("ddMMyyyy").format(date);
                Log.consoleLog(ifr, "ParsedDate======>" + ParsedDate);
            }

            if (gender.equalsIgnoreCase("Male")) {
                gendercode = "1";
            } else if (gender.equalsIgnoreCase("Female")) {
                gendercode = "2";
            } else {
                gendercode = "3";
            }

            Log.consoleLog(ifr, "firstName===>" + firstName);
            Log.consoleLog(ifr, "lastName====>" + lastName);
            Log.consoleLog(ifr, "PAN=========>" + PAN);
            Log.consoleLog(ifr, "birthDate===>" + birthDate);
            Log.consoleLog(ifr, "gender======>" + gender);
            Log.consoleLog(ifr, "gendercode==>" + gendercode);

            Log.consoleLog(ifr, "Query2==>" + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());

            if (Result1.size() > 0) {
                AddressLine1 = Result1.get(0).get(0);
                AddressLine2 = Result1.get(0).get(1);
                AddressLine3 = Result1.get(0).get(2);
                City = Result1.get(0).get(3);
                State = Result1.get(0).get(4);
                Pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "AddressLine1====>" + AddressLine1);
            Log.consoleLog(ifr, "AddressLine2====>" + AddressLine2);
            Log.consoleLog(ifr, "AddressLine3====>" + AddressLine3);
            Log.consoleLog(ifr, "City============>" + City);
            Log.consoleLog(ifr, "State===========>" + State);
            Log.consoleLog(ifr, "Pincode=========>" + Pincode);

            //Need to take from Statecode master
            //StateCode = "33";
            //PA_Pincode = "629002";
            //Added by Ahmed Alireza on 12-02-2024 for picking statecode from Master
            String Query3 = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + State + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "Query3==>" + Query3);
            List< List< String>> Result3 = ifr.getDataFromDB(Query3);
            Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
            if (Result3.size() > 0) {
                StateCode = Result3.get(0).get(0);
            }
            //Ended by Ahmed Alireza on 12-02-2024 for picking statecode from Master

            if (StateCode.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "StateCode founds to be empty for " + State + " in LOS_MST_STATE table.");
                return RLOS_Constants.ERROR;
            }

            switch (enquiryAmount.length()) {
                case 4:
                    enquiryAmount = "00000" + enquiryAmount;
                    break;
                case 5:
                    enquiryAmount = "0000" + enquiryAmount;
                    break;
                case 6:
                    enquiryAmount = "000" + enquiryAmount;
                    break;
                case 7:
                    enquiryAmount = "00" + enquiryAmount;
                    break;
                case 8:
                    enquiryAmount = "0" + enquiryAmount;
                    break;
                default:
                    break;
            }

            String cicReportGenReq = pcm.getConstantValue(ifr, "TRANSUNION", "CICREPORTGENREQ");//Added by Ahmed on 02-05-2024
            String serviceCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SERVICECODE");
            String memberRefNo = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_MEMREFNO");
            String enquiryMemberUserId = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_USERID");
            String enquiryPassword = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_PWD");
            String version = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_VERSION");//12
            String enquiryPurpose = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_ENQPURPOSE");//05
            String headerType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_HEADERTYPE");//TUEF
            String gstStateCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_GSTSTATECODE");//01
            String scoreType = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_SCORETYPE");//08
            String outputFormat = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_OPFORMAT");//03
            String responseSize = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_RESSIZE");//1
            String ioMedia = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_IOMEDIA");//CC
            String authenticationMethod = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_AUTHMODE");//L

            //Added by Ahmed on 14-02-2024 for excluding accountTypes.
            // //Commented by Ahmed on 28-06-2024 for taking dynamic prod & sub prod code from master
//            String productCode = "";
//            String subProductCode = "";
//            if (ProductType.equalsIgnoreCase("PAPL")) {
//                productCode = "PL";
//                subProductCode = "STP-PAPL";
//            } else if (ProductType.equalsIgnoreCase("CB")) {
//                productCode = "PL";
//                subProductCode = "STP-CB";
//            }
//            Log.consoleLog(ifr, "productCode=======>" + productCode);
//            Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
            String productCode = pcm.getProductCode(ifr);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "productCode=======>" + productCode);
            Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

            //Added by Ahmed on 19-02-2024 for independant calculation of NPA,DPD,WRO.
            //Discussed with Naveen and post than implementation has been put into picture.
            String excludedEMIAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "ACCTTYPE");
            Log.consoleLog(ifr, "excludedEMIAccnts=>" + excludedEMIAccnts);
            String excludedNPAAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "NPAACCTTYPE");
            Log.consoleLog(ifr, "excludedNPAAccnts=>" + excludedNPAAccnts);
            String excludedDPDAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "DPDACCTTYPE");
            Log.consoleLog(ifr, "excludedDPDAccnts=>" + excludedDPDAccnts);
            String excludedWROAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "WROACCTTYPE");
            Log.consoleLog(ifr, "excludedWROAccnts=>" + excludedWROAccnts);
            scoreCardAccTypes = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "CIBILACCTTYPE");
            Log.consoleLog(ifr, "scoreCardAccTypes=>" + scoreCardAccTypes);
            scoreCardAccStatus = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "CIBILACCTTYPESTATUS");
            Log.consoleLog(ifr, "scoreCardAccStatus=>" + scoreCardAccStatus);//SrcPaymentHistory
            scoreCardownershipIndicator = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "CIBILOWNERSHIPINDICATOR");
            Log.consoleLog(ifr, "scoreCardownershipIndicator=>" + scoreCardownershipIndicator);
            SrcPaymentHistory = pcm.getParamConfig(ifr, productCode, subProductCode, "TRANSUNIONCONF", "CIBILSRCPAYMENTHISTORY");
            Log.consoleLog(ifr, "SrcPaymentHistory=>" + SrcPaymentHistory);
            String npaPaymentHistoryTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPAPAYMENTHIST");//SUB,DBT,LSS
            String npaSuitFiledTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "NPASUITFILED");//00
            String dpdMonthCalcTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DPDMONTHCALC");//6
            String dpdValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "DAYSPASTDUE");//31
            String creditFacilityValueTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "CRDFACILITY");//31
            String pHistory6MonthCheckTags = pcm.getParamValue(ifr, "TRANSUNIONCONF", "PHIST6MONTHSCHK");

            //Aadhar No Snippet added by Ahmed for dual core check process- Implemented after discussing with Trinath on 07-02-2024
            String aadharSnippet = "";
            //Added by Ahmed for getting AadharNo via AadharVault on 12-07-2024
            String mobileNumber = pcm.getMobileNumber(ifr);
            CustomerAccountSummary cas = new CustomerAccountSummary();
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", mobileNumber);
            customerdetails.put("customerId", customerId);//Added by Ahmed on 31-07-2024
            aadharNo = cas.getAadharCustomerAccountSummary(ifr, customerdetails);

            if ((!aadharNo.equalsIgnoreCase("")) && (!aadharNo.equalsIgnoreCase("{}"))) {

                aadharSnippet = ",{\n"
                        + "                \"index\": \"I02\",\n"
                        + "                \"idNumber\": \"" + aadharNo + "\",\n"
                        + "                \"idType\": \"06\"\n"
                        + "       }\n";

            }

            String Request = "{\n"
                    + "    \"serviceCode\": \"" + serviceCode + "\",\n"
                    + "    \"monitoringDate\": \"" + MonitoringDate + "\",\n"
                    + "    \"consumerInputSubject\": {\n"
                    + "        \"tuefHeader\": {\n"
                    + "            \"headerType\": \"" + headerType + "\",\n"
                    + "            \"version\": \"" + version + "\",\n"
                    + "            \"memberRefNo\": \"" + memberRefNo + "\",\n"
                    + "            \"gstStateCode\": \"" + gstStateCode + "\",\n"
                    + "            \"enquiryMemberUserId\": \"" + enquiryMemberUserId + "\",\n"
                    + "            \"enquiryPassword\": \"" + enquiryPassword + "\",\n"
                    + "            \"enquiryPurpose\": \"" + enquiryPurpose + "\",\n"
                    + "            \"enquiryAmount\": \"" + enquiryAmount + "\",\n"
                    + "            \"scoreType\": \"" + scoreType + "\",\n"
                    + "            \"outputFormat\": \"" + outputFormat + "\",\n"
                    + "            \"responseSize\": \"" + responseSize + "\",\n"
                    + "            \"ioMedia\": \"" + ioMedia + "\",\n"
                    + "            \"authenticationMethod\": \"" + authenticationMethod + "\"\n"
                    + "        },\n"
                    + "        \"names\": [\n"
                    + "            {\n"
                    + "                \"index\": \"N01\",\n"
                    + "                \"firstName\": \"" + firstName + "\",\n"
                    + "                \"lastName\": \"" + lastName + "\",\n"
                    + "                \"birthDate\": \"" + ParsedDate + "\",\n"
                    + "                \"gender\": \"" + gendercode + "\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"ids\": [\n"
                    + "            {\n"
                    + "                \"index\": \"I01\",\n"
                    + "                \"idNumber\": \"" + PAN + "\",\n"
                    + "                \"idType\": \"01\"\n"
                    + "            }\n"
                    + aadharSnippet
                    + "        ],\n"
                    + "        \"telephones\": [\n"
                    + "            {\n"
                    + "                \"index\": \"T01\",\n"
                    + "                \"telephoneNumber\": \"" + MobileNumber + "\",\n"
                    + "                \"telephoneType\": \"01\"\n"
                    + "            }\n"
                    + "        ],\n"
                    + "        \"addresses\": [\n"
                    + "            {\n"
                    + "                \"index\": \"A01\",\n"
                    + "                \"line1\": \"" + AddressLine1 + "\",\n"
                    + "                \"line2\": \"" + AddressLine2 + "\",\n"
                    + "                \"line3\": \"" + AddressLine3 + "\",\n"
                    + "                \"line4\": \"" + City + "\",\n"
                    + "                \"line5\": \"\",\n"
                    + "                \"stateCode\": \"" + StateCode + "\",\n"//shoudl get from master
                    + "                \"pinCode\": \"" + Pincode + "\",\n"
                    + "                \"addressCategory\": \"04\",\n"
                    + "                \"residenceCode\": \"01\"\n"
                    + "           }\n"
                    + "        ],\n"
                    + "        \"enquiryAccounts\": [\n"
                    + "            {\n"
                    + "                \"index\": \"I01\",\n"
                    + "                \"accountNumber\": \"\"\n"
                    + "            }\n"
                    + "        ]\n"
                    + "    }\n"
                    + "}";

            String Response = cm.getWebServiceResponse(ifr, apiName, Request);
            Log.consoleLog(ifr, "Response===>" + Response);

            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "", applicantType);

            if (!Response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(Response);

                String body = resultObj.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser.parse(body);

                String ControlData = bodyObj.get("controlData").toString();
                Log.consoleLog(ifr, "ControlData==>" + ControlData);

                JSONObject ControlDataObj = (JSONObject) parser.parse(ControlData);
                String ControlDataStatus = ControlDataObj.get("success").toString();

                Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);

                if (ControlDataStatus.equalsIgnoreCase("true")) {

                    Log.consoleLog(ifr, "cicReportGenReq==>" + cicReportGenReq);
                    if (cicReportGenReq.equalsIgnoreCase("Y")) {
                        if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
                            String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
                            String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Consumer", encodedB64, "NGREPORTTOOL_TRANSUNION");
                            Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
                            cm.updateCICReportStatus(ifr, "CIBIL", generateReportStatus, applicantType);//Added by Ahmed on 26-07-2024 for Status Updation of CIC Report
                        }
                    }

                    String BureauScore = "";
                    int cibilScore = 0;

                    String Scores = "";
                    String accounts = "";
                    String consumerCreditData = bodyObj.get("consumerCreditData").toString();
                    // System.out.println("consumerCreditData==>" + consumerCreditData);
                    JSONArray consumerCreditDataJSON = (JSONArray) parser.parse(consumerCreditData);
                    if (!consumerCreditDataJSON.isEmpty()) {
                        for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
                            String InputString = consumerCreditDataJSON.get(i).toString();
                            JSONObject consumerCreditDataJSONObj = (JSONObject) parser.parse(InputString);
                            Scores = consumerCreditDataJSONObj.get("scores").toString();
                            Log.consoleLog(ifr, "Scores=" + Scores);

                            try {
                                accounts = consumerCreditDataJSONObj.get("accounts").toString();
                                Log.consoleLog(ifr, "account=" + accounts);
                            } catch (Exception e) {
                                Log.consoleLog(ifr, "Exception/accounts=" + e);
                            }

                        }
                    }

                    Log.consoleLog(ifr, "Scores==>" + Scores);
                    JSONArray ScoresJSON = (JSONArray) parser.parse(Scores);

                    if (!ScoresJSON.isEmpty()) {
                        for (int i = 0; i < ScoresJSON.size(); i++) {
                            String InputString = ScoresJSON.get(i).toString();
                            JSONObject ScoresJSONObj = (JSONObject) parser.parse(InputString);
                            String transCIBILScore = ScoresJSONObj.get("score").toString();
                            Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
                            cibilScore = Integer.parseInt(transCIBILScore);
                            Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
                            BureauScore = String.valueOf(cibilScore);
                            Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
                            //added by prakash 02-03-2024 for BureauScore set default as 0 if 000-1

                            if ((BureauScore.equalsIgnoreCase("000-1"))) {
                                String cbilimmuneAccept = ConfProperty.getCommonPropertyValue("CICImmune_Accept");
                                Log.consoleLog(ifr, "cbilimmuneAccept==>" + cbilimmuneAccept);
                                //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

                                if (cbilimmuneAccept.equalsIgnoreCase("YES")) {
                                    BureauScore = "-1";
                                } else {
                                    BureauScore = "0";
                                }

                            }
                            Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
                        }
                    }

                    int suitFiledcount = 0;
                    int npaCount = 0;
                    int dpdCount = 0;
                    int crdCount = 0;
                    int totalEmiAmnt = 0;
                    int totalNonEmiCount = 0;
                    String consolidatedtwelveMonthData = "";
                    Log.consoleLog(ifr, "accounts==>" + accounts);
                    int count6MonthData = 0;

                    //Vandana Code variable
                    String settledHistory = "No";
                    String NPA_INP = "No";
                    String GUARANTORNPA_INP = "No";
                    String GUARANTORWRITEOFFSETTLEDHIST_INP = "No";
                    String paymentHistoryClob = "000";

                    if (!accounts.equalsIgnoreCase("")) {
                        JSONArray accountsJSON = (JSONArray) parser.parse(accounts);

                        if (!accountsJSON.isEmpty()) {

                            //For NPA KnockOff Check
                            for (int i = 0; i < accountsJSON.size(); i++) {

                                String InputString = accountsJSON.get(i).toString();
                                JSONObject accountsJSONObj = (JSONObject) parser.parse(InputString);

                                Log.consoleLog(ifr, "overAllAccountTypes" + overAllAccountTypes);
                                Log.consoleLog(ifr, "overAllcreditFacilityStatus" + overAllcreditFacilityStatus);

                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();
                                    Log.consoleLog(ifr, "Payment_History_Profile" + payHistory);
                                    // added by vandana start

                                    Log.consoleLog(ifr, "Payment_History_Profile_Data_Cibil" + payHistory);
                                    String payment = payHistory.replace("?", "0")
                                            .replace("S", "0").replace("N", "0")
                                            .replace("B", "0").replace("D", "0")
                                            .replace("M", "0").replace("L", "0").replace("X", "0");
                                    Log.consoleLog(ifr, "Payment_History_Profile" + payment);

                                    //-----------------
                                    if (payment.length() < 3) {
                                        Log.consoleLog(ifr, "Insufficient data to extract payment history.");
                                        //return "";//Modified by Ahmed on 18-07-2024
                                    } else {
                                        int numberOfMonths = payment.length() / 3;
                                        Log.consoleLog(ifr, "Payment_History_Profile numberOfMonths" + numberOfMonths);

                                        for (int j = 0; j < numberOfMonths; j++) {
                                            String twelveMonthData = payment.substring(j * 3, (j * 3) + 3);
                                            consolidatedtwelveMonthData = consolidatedtwelveMonthData + twelveMonthData;
                                        }
                                    }
                                    Log.consoleLog(ifr, "consolidatedtwelveMonthData" + consolidatedtwelveMonthData);
                                    paymentHistoryCombined = paymentHistoryCombined + payHistory;

                                    String[] substrings = splitString(payHistory, 3);
                                    if (substrings.length >= 6) {
                                        for (int j = 0; j < 6; j++) {
                                            if (!substrings[j].equalsIgnoreCase("XXX")) {
                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, substrings[j]);
                                                Log.consoleLog(ifr, "pHistory/first 6 months available==>" + substrings[j]);
                                                if (sixMonthchkdata) {
                                                    count6MonthData++;
                                                }
                                            }
                                        }
                                    } else {
                                        for (String pHistory : substrings) {
                                            if (!pHistory.equalsIgnoreCase("XXX")) {
                                                boolean sixMonthchkdata = checkpHistory6MonthData(ifr, pHistory6MonthCheckTags, pHistory);
                                                Log.consoleLog(ifr, "pHistory/less than 6 months available==>" + pHistory);
                                                if (sixMonthchkdata) {
                                                    count6MonthData++;
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    Log.consoleLog(ifr, "paymentHistory tag not available.");
                                }
                                if (count6MonthData > 0) {
                                    pHist6monthsChk = "Yes";
                                }
                                Log.consoleLog(ifr, "pHist6monthsChk  " + pHist6monthsChk);
                                if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                    String payHistory = accountsJSONObj.get("paymentHistory").toString();

                                    //       String firstSixMonths = paymentHistory.substring(0, 18); // Assuming each month is 3 digits, first 6 months are 18 characters
                                    // Check if first 6 months contain SUB, DBT, or LSS
                                    //    PaymentHistory = PaymentHistory + firstSixMonths;
                                }

                                String creditFacilityStatus = "";
                                String paymentHistory = "";
                                String suitFiled = "";
                                String accountType = "";
                                String dateClosed = "";
                                String currentBalance = "";
                                String ownershipIndicator = "";
                                String dateOpened = "";

                                if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus").equalsIgnoreCase(""))) {
                                    creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + creditFacilityStatus;
                                    Log.consoleLog(ifr, "overAllcreditFacilityStatus==>" + overAllcreditFacilityStatus);
                                } else {
                                    Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
                                    accountType = accountsJSONObj.get("accountType").toString();
                                    overAllAccountTypes = overAllAccountTypes + accountType;
                                    Log.consoleLog(ifr, "accountType==>" + accountType);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "ownershipIndicator").equalsIgnoreCase(""))) {
                                    ownershipIndicator = accountsJSONObj.get("ownershipIndicator").toString();
                                    overALLownershipIndicator = overALLownershipIndicator + ownershipIndicator;
                                    Log.consoleLog(ifr, "ownershipIndicator==>" + ownershipIndicator);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
                                    dateOpened = accountsJSONObj.get("dateOpened").toString();
                                    overALLdateOpened = overALLdateOpened + dateOpened;
                                    Log.consoleLog(ifr, "dateOpened==>" + dateOpened);
                                } else {
                                    Log.consoleLog(ifr, "accountType tag not available.");
                                }
                                if (!overAllAccountTypes.equalsIgnoreCase("")) {
                                    overAllAccountTypes = overAllAccountTypes + ",";
                                }

                                if (!overAllcreditFacilityStatus.equalsIgnoreCase("")) {
                                    overAllcreditFacilityStatus = overAllcreditFacilityStatus + ",";
                                }
                                if (!overALLownershipIndicator.equalsIgnoreCase("")) {
                                    overALLownershipIndicator = overALLownershipIndicator + ",";
                                }
                                if (!overALLdateOpened.equalsIgnoreCase("")) {
                                    overALLdateOpened = overALLdateOpened + ",";
                                }
                                if (!PaymentHistory.equalsIgnoreCase("")) {
                                    PaymentHistory = PaymentHistory + ",";//overALLdateOpened
                                }

                                if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
                                    currentBalance = accountsJSONObj.get("currentBalance").toString();
                                    Log.consoleLog(ifr, "currentBalance==>" + currentBalance);

                                    if (currentBalance.equalsIgnoreCase("")) {
                                        currentBalance = "0";
                                    }

                                } else {
                                    Log.consoleLog(ifr, "currentBalance tag not available.");
                                }

                                if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
                                    dateClosed = accountsJSONObj.get("dateClosed").toString();
                                    Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
                                } else {
                                    Log.consoleLog(ifr, "dateClosed tag not available.");
                                }

                                boolean emiStatus = checkEMIAccFilterStatus(ifr, accountType, currentBalance, dateClosed, excludedEMIAccnts);
                                Log.consoleLog(ifr, "emiStatus==>" + emiStatus);

                                boolean npaStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedNPAAccnts, dateClosed);
                                Log.consoleLog(ifr, "npaStatus==>" + npaStatus);

                                boolean dpdStatus = checkKnockOffAccFilterStatus(ifr, accountType, excludedDPDAccnts, dateClosed);
                                Log.consoleLog(ifr, "dpdStatus==>" + dpdStatus);

                                boolean wroStatus = checkKnockOffAccFilterStatus_WRO(ifr, accountType, excludedWROAccnts, dateClosed);
                                Log.consoleLog(ifr, "wroStatus==>" + wroStatus);

                                //For NPA
                                if (npaStatus) {
                                    //Handled for empty and excluded accounts
                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                        paymentHistory = accountsJSONObj.get("paymentHistory").toString();
                                        Log.consoleLog(ifr, "paymentHistory==>" + paymentHistory);

                                        String[] substrings = splitString(paymentHistory, 3);
                                        // Check if SUB, DBT, or LSS is present
                                        for (String pHistory : substrings) {
                                            if (npaPaymentHistoryTags.contains(pHistory)) {
                                                npaCount++;
                                            }
                                        }
                                    } else {
                                        Log.consoleLog(ifr, "paymentHistory tag not available.");
                                    }

                                    if (!(cf.getJsonValue(accountsJSONObj, "suitFiled").equalsIgnoreCase(""))) {
                                        suitFiled = accountsJSONObj.get("suitFiled").toString();
                                        Log.consoleLog(ifr, "suitFiled==>" + suitFiled);
                                    } else {
                                        Log.consoleLog(ifr, "suitFiled tag not available.");
                                    }

                                    //Suit Filed should not be 00 and it should not be empty- As per Naveen`s Mail
                                    if (!npaSuitFiledTags.contains(suitFiled) && (!suitFiled.equalsIgnoreCase(""))) {
                                        suitFiledcount++;
                                    }

                                } else {
                                    Log.consoleLog(ifr, "NPA Account Type Excluded. Please check config master. Looping next..");
                                }

                                //For DPD
                                if (dpdStatus) {
                                    if (!(cf.getJsonValue(accountsJSONObj, "paymentHistory").equalsIgnoreCase(""))) {
                                        paymentHistory = accountsJSONObj.get("paymentHistory").toString();
                                        Log.consoleLog(ifr, "paymentHistory==>" + paymentHistory);

                                        String[] substrings = splitString(paymentHistory, 3);

                                        int dpdmonthcounttag = 1;
                                        for (String pHistoryTag : substrings) {
                                            Log.consoleLog(ifr, "pHistoryTag " + pHistoryTag);
                                            if (dpdmonthcounttag <= Integer.parseInt(dpdMonthCalcTags)) {
                                                if (!pHistoryTag.equalsIgnoreCase("XXX")) {

                                                    try {
                                                        if (Integer.parseInt(pHistoryTag) >= Integer.parseInt(dpdValueTags)) {
                                                            dpdCount++;
                                                        }
                                                    } catch (Exception e) {
                                                        Log.consoleLog(ifr, "Exception where pHistoryTag is an alphaber and not numeric. So here DPDCount is skipped");
                                                    }

                                                }
                                                dpdmonthcounttag++;
                                            }
                                            Log.consoleLog(ifr, "dpdfirst6monthcounttag " + dpdmonthcounttag);
                                        }

                                    } else {
                                        Log.consoleLog(ifr, "paymentHistory tag not available.");
                                    }

                                } else {
                                    Log.consoleLog(ifr, "DPD Account Type Excluded. Please check config master. Looping next..");
                                }

                                //For Written Off
                                if (wroStatus) {
                                    if (!(cf.getJsonValue(accountsJSONObj, "creditFacilityStatus").equalsIgnoreCase(""))) {
                                        creditFacilityStatus = accountsJSONObj.get("creditFacilityStatus").toString();
                                        Log.consoleLog(ifr, "creditFacilityStatus==>" + creditFacilityStatus);
                                    } else {
                                        Log.consoleLog(ifr, "creditFacilityStatus tag not available.");
                                    }

                                    if ((creditFacilityValueTags.contains(creditFacilityStatus))
                                            && (!creditFacilityStatus.equalsIgnoreCase(""))) {
                                        crdCount++;
                                    }
                                } else {
                                    Log.consoleLog(ifr, "WRO Account Type Excluded. Please check config master. Looping next..");
                                }

                                //For EMI Block
                                //Modified by Ahmed on 03-06-2024 for performing Total Non EMI cals
                                if (emiStatus) {
                                    String emiAmount = "0";
                                    if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
                                        emiAmount = accountsJSONObj.get("emiAmount").toString();
                                        Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
                                        if (!emiAmount.equalsIgnoreCase("")) {
                                            totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
                                        } else {
                                            Log.consoleLog(ifr, "emiAmount tag available but empty");
                                            if (Integer.parseInt(currentBalance) > 0) {
                                                totalNonEmiCount++;
                                            }
                                        }
                                    } else {
                                        Log.consoleLog(ifr, "emiAmount tag not available.");

                                        if (Integer.parseInt(currentBalance) > 0) {
                                            totalNonEmiCount++;
                                        }
                                    }
                                } else {
                                    Log.consoleLog(ifr, "EMI Account Type Excluded. Please check config master. Looping next..");
                                }

                            }//End of Accounts Loop

                            //Vandana code moved here by Ahmed on 07-08-2024
                            if (overALLownershipIndicator.equalsIgnoreCase("3")) {
                                OwnerShipIndicator = "Yes";
                            } else {
                                OwnerShipIndicator = "No";
                            }
                            Log.consoleLog(ifr, "OwnerShipIndicator  " + OwnerShipIndicator);

                            Log.consoleLog(ifr, "count6MonthData  " + count6MonthData);

                            String scoreCardstatus = checScoreCardAccountTypeStatus(ifr, overAllAccountTypes, scoreCardAccTypes,
                                    overAllcreditFacilityStatus, scoreCardAccStatus, overALLownershipIndicator, scoreCardownershipIndicator);

                            Log.consoleLog(ifr, "scoreCardstatus  " + scoreCardstatus);

                            String[] statuses = scoreCardstatus.split("-");

                            // Ensure that we have exactly two parts
                            if (statuses.length == 3) {
                                scoreCardAccType = statuses[0].trim(); // Trim to remove any leading/trailing whitespace
                                scoreCardAccStatu = statuses[1].trim();  // Trim to remove any leading/trailing whitespace
                                scoreCardOwnerShip = statuses[2].trim();

                                // Now you can use yesStatus and noStatus as needed
                                Log.consoleLog(ifr, "CIBILscoreCardAccType  " + scoreCardAccType);
                                Log.consoleLog(ifr, "CIBILscoreCardAccStatu  " + scoreCardAccStatu);
                                Log.consoleLog(ifr, "CIBILscoreCardOwnerShip  " + scoreCardOwnerShip);

                            } else {
                                // Handle unexpected format if needed
                                System.out.println("Unexpected format: " + scoreCardstatus);
                            }

                            String DateOpen = checScoreCardDateOpend(ifr, overALLdateOpened);
                            Log.consoleLog(ifr, "DateOpen  " + DateOpen);
                            paymentHistoryClob = cm.getClobSnippetData(ifr, consolidatedtwelveMonthData);
                            // ADDED BY VANDANA
                            if (scoreCardAccStatu.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & scoreCardOwnerShip.equalsIgnoreCase("Yes")) {
                                settledHistory = "Yes";
                            }

                            if (pHist6monthsChk.equalsIgnoreCase("Yes") & scoreCardOwnerShip.equalsIgnoreCase("Yes")) {
                                NPA_INP = "Yes";
                            }

                            if (pHist6monthsChk.equalsIgnoreCase("Yes") & OwnerShipIndicator.equalsIgnoreCase("Yes")) {
                                GUARANTORNPA_INP = "Yes";
                            }

                            if (scoreCardAccStatu.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & OwnerShipIndicator.equalsIgnoreCase("Yes")) {
                                GUARANTORWRITEOFFSETTLEDHIST_INP = "Yes";
                            }

                        }

                    } else {
                        Log.consoleLog(ifr, "Account Information not found in CIBIL Resposne.");
                    }

                    Log.consoleLog(ifr, "dpdCount " + dpdCount);
                    Log.consoleLog(ifr, "suitFiledcount " + suitFiledcount);
                    Log.consoleLog(ifr, "npaCount " + npaCount);
                    Log.consoleLog(ifr, "writeoff " + crdCount);
                    Log.consoleLog(ifr, "totalNonEmiCount==>" + totalNonEmiCount);
                    Log.consoleLog(ifr, "overAllAccountTypes  " + overAllAccountTypes);
                    Log.consoleLog(ifr, "overAllAccountStatus  " + overAllcreditFacilityStatus);
                    Log.consoleLog(ifr, "overALLownershipIndicator  " + overALLownershipIndicator);

//                    String DateOpen = checScoreCardDateOpend(ifr, overALLdateOpened);
//                    Log.consoleLog(ifr, "DateOpen  " + DateOpen);
//                        String srcPaymenyHistory = CheckPaymenyHistory(ifr, PaymentHistory, SrcPaymentHistory);
//                        Log.consoleLog(ifr, "srcPaymenyHistory  " + srcPaymenyHistory);
                    String NPA = "NA";
                    String DPD = "0";
                    String WRO = "NA";

                    if ((npaCount > 0) || (suitFiledcount > 0)) {
                        NPA = "Yes";
                    }

                    if (dpdCount > 0) {
                        DPD = String.valueOf(dpdCount);
                    }

                    if (crdCount > 0) {
                        WRO = "Yes";
                    }

                    Log.consoleLog(ifr, "NPA==>" + NPA);
                    Log.consoleLog(ifr, "DPD==>" + DPD);
                    Log.consoleLog(ifr, "WRO==>" + WRO);
                    Log.consoleLog(ifr, "BureauScore==>" + BureauScore);
                    Log.consoleLog(ifr, "paymentHistoryCombined==>" + consolidatedtwelveMonthData);

//                    String paymentHistoryClob = cm.getClobSnippetData(ifr, consolidatedtwelveMonthData);
//                    // ADDED BY VANDANA
//                    if (scoreCardAccStatu.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & scoreCardOwnerShip.equalsIgnoreCase("Yes")) {
//                        settledHistory = "Yes";
//                    } else {
//                        settledHistory = "No";
//                    }
//                    if (pHist6monthsChk.equalsIgnoreCase("Yes") & scoreCardOwnerShip.equalsIgnoreCase("Yes")) {
//                        NPA_INP = "Yes";
//                    } else {
//                        NPA_INP = "No";
//                    }
//
//                    if (pHist6monthsChk.equalsIgnoreCase("Yes") & OwnerShipIndicator.equalsIgnoreCase("Yes")) {
//                        GUARANTORNPA_INP = "Yes";
//                    } else {
//                        GUARANTORNPA_INP = "No";
//                    }
//
//                    if (scoreCardAccStatu.equalsIgnoreCase("Yes") & DateOpen.equalsIgnoreCase("Yes") & OwnerShipIndicator.equalsIgnoreCase("Yes")) {
//                        GUARANTORWRITEOFFSETTLEDHIST_INP = "Yes";
//                    } else {
//                        GUARANTORWRITEOFFSETTLEDHIST_INP = "No";
//                    }
                    Log.consoleLog(ifr, "CIBILsettledHistory==>" + settledHistory);
                    Log.consoleLog(ifr, "CIBILNPA_INP==>" + NPA_INP);
                    Log.consoleLog(ifr, "CIBILGUARANTORNPA_INP==>" + GUARANTORNPA_INP);
                    Log.consoleLog(ifr, "CIBILGUARANTORNPA_INP==>" + GUARANTORWRITEOFFSETTLEDHIST_INP);
//                    String query = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST) "
//                            + "values('" + ProcessInstanceId + "','" + BureauScore + "','" + NPA + "','" + DPD + "','" + WRO + "','CB','" + totalEmiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEmiCount + "','" + settledHistory + "','" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "')";
//                    Log.consoleLog(ifr, "Insert query:" + query);

                    String query = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF,BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED) "
                            + "values('" + ProcessInstanceId + "','" + BureauScore + "','" + NPA + "','" + DPD + "','" + WRO + "','CB','" + totalEmiAmnt + "'," + paymentHistoryClob + ",'" + applicantType + "','" + totalNonEmiCount + "','" + settledHistory + "','" + NPA_INP + "','" + GUARANTORNPA_INP + "','" + GUARANTORWRITEOFFSETTLEDHIST_INP + "',SYSDATE)";
                    Log.consoleLog(ifr, "Insert query:" + query);

                    ifr.saveDataInDB(query);

                    //cm.generateTransunionReport(ifr, ProcessInstanceId, "CONSUMER", Response);
                    return BureauScore;
                } else {
                    return RLOS_Constants.ERROR;
                }

            } else {
                Response = "No response from Server.";
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getConsumerCIBILScore==>" + e);
        }

        return RLOS_Constants.ERROR;

    }

    public static String[] splitString(String input, int length) {
        int numOfSubstrings = (int) Math.ceil((double) input.length() / length);
        String[] substrings = new String[numOfSubstrings];

        int index = 0;
        for (int i = 0; i < input.length(); i += length) {
            substrings[index] = input.substring(i, Math.min(i + length, input.length()));
            index++;
        }

        return substrings;
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

    private boolean checkEMIAccFilterStatus(IFormReference ifr, String accountType, String currentBalance,
            String dateClosed, String excludedAccnts) {

        Log.consoleLog(ifr, "#checkEMIAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);
        String[] excludedAccounts = excludedAccnts.split(",");

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "excludedAccounts===> CB" + accnt);
                return false;
            }
        }
//        if (excludedAccnts.contains(accountType)) {
//            return false;
//        }

        boolean isValidDateFormat = false;
        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "ddMMyyyy";

            isValidDateFormat = isValidDateFormat(dateClosed, dateFormat);
            Log.consoleLog(ifr, "IsValidDateFormat" + isValidDateFormat);

        }

        if (((currentBalance.equalsIgnoreCase("0")) && (!currentBalance.equalsIgnoreCase("")))
                || (isValidDateFormat == true)) {
            return false;
        }

        Log.consoleLog(ifr, "Filter Condition not satisfied..");
        return true;

    }

    private boolean checkKnockOffAccFilterStatus(IFormReference ifr, String accountType,
            String excludedAccnts, String dateClosed) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

//        if (excludedAccnts.contains(accountType)) {
//            return false;
//        }
        String[] excludedAccntsSplitter = excludedAccnts.split(",");
        for (String exAccount : excludedAccntsSplitter) {
            Log.consoleLog(ifr, "exAccount====>" + exAccount);
            Log.consoleLog(ifr, "accountType==>" + accountType);
            if (exAccount.equalsIgnoreCase(accountType)) {
                Log.consoleLog(ifr, "exAccoun mactching with AccountType");
                return false;
            }
        }

        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "ddMMyyyy";

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

    // added by vandana
    private String checScoreCardAccountTypeStatus(IFormReference ifr,
            String accountType, String srcaccountType,
            String creditFacilityStatus, String srcAccStatus, String ownershipIndicator, String srcownershipIndicator) {

        int brmsAccountTypeCount = 0;
        int brmsAccountTypeStatusCount = 0;
        int brmsownershipIndicatorCount = 0;

        String brmsAccountType = "No";
        String brmsAccountTypeStatus = "No";
        String brmsownershipIndicator = "No";

        Log.consoleLog(ifr, "#checScoreCardAccountTypeStatus started...");
        Log.consoleLog(ifr, "CibilaccountType=====>" + accountType);
        Log.consoleLog(ifr, "creditFacilityStatus===>" + creditFacilityStatus);
        Log.consoleLog(ifr, "brmsownershipIndicator===>" + brmsownershipIndicator);
        String[] includedAccounts = srcaccountType.split(",");
        String[] includedAccStatusArray = srcAccStatus.split(",");
        String[] includeownershipIndicator = srcownershipIndicator.split(",");

        String[] cibilAccounts = accountType.split(",");
        String[] cibilAccStatus = creditFacilityStatus.split(",");
        String[] cibilOwnershipIndicator = ownershipIndicator.split("");
        for (String cibilAccnt : cibilAccounts) {
            Log.consoleLog(ifr, "cibilAccnt=====>" + cibilAccnt);
            //
            for (String scoreCardAccnt : includedAccounts) {
                Log.consoleLog(ifr, "scoreCardAccnt=====>" + scoreCardAccnt);
                //
                if (scoreCardAccnt.equals(cibilAccnt)) {
                    //
                    Log.consoleLog(ifr, "scoreCardAccnt=====>" + scoreCardAccnt);
                    brmsAccountTypeCount++;
                }
            }
        }

        for (String cibilAccntStatus : cibilAccStatus) {
            Log.consoleLog(ifr, "cibilAccntStatus=====>" + cibilAccntStatus);
            for (String scoreCardAccntStatus : includedAccStatusArray) {
                Log.consoleLog(ifr, "scoreCardAccntStatus=====>" + scoreCardAccntStatus);
                if (scoreCardAccntStatus.equals(cibilAccntStatus)) {
                    Log.consoleLog(ifr, "scoreCardAccntStatus=====>" + scoreCardAccntStatus);
                    brmsAccountTypeStatusCount++;
                }
            }
        }
        for (String cibilownershipIndicator : cibilOwnershipIndicator) {
            Log.consoleLog(ifr, "cibilAccntStatus=====>" + cibilownershipIndicator);
            for (String scoreownershipIndicator : includeownershipIndicator) {
                Log.consoleLog(ifr, "scoreownershipIndicator=====>" + scoreownershipIndicator);
                if (scoreownershipIndicator.equals(cibilownershipIndicator)) {
                    Log.consoleLog(ifr, "scoreownershipIndicator=====>" + scoreownershipIndicator);
                    brmsAccountTypeStatusCount++;
                }
            }
        }

        if (brmsAccountTypeCount > 0) {
            brmsAccountType = "Yes";
        }

        if (brmsownershipIndicatorCount > 0) {
            brmsAccountTypeStatus = "Yes";
        }
        if (brmsAccountTypeStatusCount > 0) {
            brmsownershipIndicator = "Yes";
        }

        Log.consoleLog(ifr, "brmsAccountType=========>" + brmsAccountType);
        Log.consoleLog(ifr, "brmscreditFacilityStatus===>" + brmsAccountTypeStatus);

        return brmsAccountType + "-" + brmsAccountTypeStatus + "-" + brmsownershipIndicator;

    }

    private String checScoreCardDateOpend(IFormReference ifr,
            String dateopen) {
        LocalDate currentDate = LocalDate.now();
        String dateOpen = "No";
        int brmsdateOpenCount = 0;
        // Formatter for parsing DDMMYYYY format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

        String[] cibildateopen = dateopen.split(",");

        for (String dateOpened : cibildateopen) {
            LocalDate dateOpenedParsed = LocalDate.parse(dateOpened, formatter);

            // Calculate the difference in years
            long monthsDifference = ChronoUnit.MONTHS.between(dateOpenedParsed.withDayOfMonth(1), currentDate.withDayOfMonth(1));
            Log.consoleLog(ifr, "yearsDifference=========>" + monthsDifference);
            // Compare with 3 years
            if (monthsDifference <= 60) {
                Log.consoleLog(ifr, "monthsDifference=========>" + monthsDifference);
                // Compare with 3 years

            } else {
                brmsdateOpenCount++;
                Log.consoleLog(ifr, "monthsDifference=========>" + monthsDifference);
                // Compare with 3 years
            }
        }
        if (brmsdateOpenCount < 0) {
            dateOpen = "Yes";
            Log.consoleLog(ifr, "IFdateOpen=========>" + dateOpen);
            // Compare with 3 years
        }

        return dateOpen;
    }

    private static boolean checkpHistory6MonthData(IFormReference ifr, String pHistoryConfigData, String pHistoryMonthData) {
        String[] pHistoryMonthDataSplitter = pHistoryConfigData.split(",");

        for (String pHistoryConfigs : pHistoryMonthDataSplitter) {

            Log.consoleLog(ifr, "pHistoryConfigs=====>" + pHistoryConfigs);
            Log.consoleLog(ifr, "pHistoryMonthData===>" + pHistoryMonthData);

            if (pHistoryConfigs.equalsIgnoreCase(pHistoryMonthData)) {
                return true;
            }

        }
        return false;
    }

    private static boolean isDateWithinSpecifiedMonths(String date, int nofMonthsToCheck) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDate givenDate = LocalDate.parse(date, formatter);
        LocalDate currentDate = LocalDate.now();
        LocalDate monthsAgo = currentDate.minusMonths(nofMonthsToCheck);
        return !givenDate.isBefore(monthsAgo) && !givenDate.isAfter(currentDate);
    }

    private boolean checkKnockOffAccFilterStatus_WRO(IFormReference ifr, String accountType,
            String excludedAccnts, String dateClosed) {

        Log.consoleLog(ifr, "#checkKnockOffAccFilterStatus started...");
        Log.consoleLog(ifr, "accountType=====>" + accountType);
        Log.consoleLog(ifr, "dateClosed======>" + dateClosed);

        if (accountType.equalsIgnoreCase("")) {
            return false;
        }

        String[] excludedAccntsSplitter = excludedAccnts.split(",");
        for (String exAccount : excludedAccntsSplitter) {
            Log.consoleLog(ifr, "exAccount====>" + exAccount);
            Log.consoleLog(ifr, "accountType==>" + accountType);
            if (exAccount.equalsIgnoreCase(accountType)) {
                Log.consoleLog(ifr, "exAccoun mactching with AccountType");
                return false;
            }
        }

        if (!dateClosed.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
            String dateFormat = "ddMMyyyy";

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

}
