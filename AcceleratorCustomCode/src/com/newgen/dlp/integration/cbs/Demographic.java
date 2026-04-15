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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class Demographic {

    CommonFunctionality cf = new CommonFunctionality();
    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();

    public String getDemographic(IFormReference ifr, String ProcessInstanceId, String CustomerID) {

        String apiName = "Demographic";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        // String APIName = "CBS_Demographic";
//        String ErrorCode = "";
//        String ErrorMessage = "";
        try {
            Log.consoleLog(ifr, "Entered into ExecuteCBS_Demographic...");
            String Dataset = "";
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
            int Age = 0;
            int Years = 0;
            String WriteOffDate = "NA";
            String writeOffPresent = "NA";
            String CustomerCategory = "";
            String misCodeTagValue = "";
            String DateOfCustOpen = "";
            String strFlgCustType = "";

            String BankCode = pcm.getConstantValue(ifr, "CBSDGRAPH", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSDGRAPH", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CBSDGRAPH", "USERID");
            String TBranch = pcm.getConstantValue(ifr, "CBSDGRAPH", "TBRANCH");

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
                    + "            \"ExternalReferenceNo\": \"\",\n"
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
                    + "        \"CustomerID\": \"" + CustomerID + "\"\n"
                    + "    }\n"
                    + "}";

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(OutputJSON);

                String body = resultObj.get("body").toString();
                JSONObject bodyJSON = (JSONObject) parser.parse(body);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyJSON);
                if (CheckError.equalsIgnoreCase("true")) {
                    String CustDemographInqResponse = bodyJSON.get("CustDemographInqResponse").toString();
                    JSONObject CustDemographInqResponseJSON = (JSONObject) parser.parse(CustDemographInqResponse);

                    CustomerCategory = cf.getJsonValue(CustDemographInqResponseJSON, "CustomerCategory");
                    Log.consoleLog(ifr, "CustomerCategory : " + CustomerCategory);
                    String DateOfBirth = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfBirth");
                    Log.consoleLog(ifr, "DateOfBirth===>" + DateOfBirth);

                    DateOfCustOpen = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfCustOpen");
                    Log.consoleLog(ifr, "DateOfCustOpen===>" + DateOfCustOpen);
                    WriteOffDate = cf.getJsonValue(CustDemographInqResponseJSON, "WriteOffDate");

                    //Added by Ahmed on 15-02-2024 from prod output response..
                    if (WriteOffDate.equalsIgnoreCase("{}")) {
                        WriteOffDate = "";
                    }

                    if (!WriteOffDate.equalsIgnoreCase("")) {
                        writeOffPresent = "Yes";
                    } else {
                        WriteOffDate = "01-01-1800";//Passing the currentdate to Writeoff
                        Log.consoleLog(ifr, "Writeoff is Empty!");
                    }

                    Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);

//                    Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);
//                    if (!(WriteOffDate.equalsIgnoreCase(""))) {
//                        JSONObject WriteOffDateJSON = (JSONObject) parser.parse(WriteOffDate);
//                        Log.consoleLog(ifr, "WriteOffDateJSON.size()===>" + WriteOffDateJSON.size());
//
//                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
//                        String formattedDate1 = dateFormat1.format(currentDate);
//                        if (!WriteOffDateJSON.isEmpty()) {
//                            writeOffPresent = "Yes";
//                            WriteOffDate = formattedDate1;
//                        } else {
//                            WriteOffDate = formattedDate1;
//                        }
//                    }
                    LocalDate dob = LocalDate.parse(DateOfBirth);
                    Age = calculateAge(dob);
                    Log.consoleLog(ifr, "Age===>" + Age);

                    LocalDate dateofcustopen = LocalDate.parse(DateOfCustOpen);
                    Years = calculateAge(dateofcustopen);
                    Log.consoleLog(ifr, "Years===>" + Years);

                    try {
                        Log.consoleLog(ifr, "DemoGraphic API");
                        String ApplicantCategory = cf.getJsonValue(CustDemographInqResponseJSON, "ApplicantCategory");
                        String VoterID = cf.getJsonValue(CustDemographInqResponseJSON, "VoterID");
                        String PassPortNo = cf.getJsonValue(CustDemographInqResponseJSON, "PassPortNo");
                        String DrivingLicense = cf.getJsonValue(CustDemographInqResponseJSON, "DrivingLicense");
                        String Landline = cf.getJsonValue(CustDemographInqResponseJSON, "Landline");

                        String Query = "select CASTE from los_m_caste where "
                                + "CBSCASTECODE='" + ApplicantCategory + "' and rownum=1";
                        Log.consoleLog(ifr, "Query==>" + Query);
                        List< List< String>> Result = ifr.getDataFromDB(Query);
                        Log.consoleLog(ifr, "#Result===>" + Result.toString());

                        Log.consoleLog(ifr, "#ApplicantCategory" + ApplicantCategory);
                        strFlgCustType = cf.getJsonValue(CustDemographInqResponseJSON, "FlgCustType");
                        if (Result.size() > 0) {
                            ApplicantCategory = Result.get(0).get(0);
                        }

//Modified by Ahmed ob 26-07-2024 for queryReadingfromProp
//                        String DG_Update = "UPDATE LOS_T_CUSTOMER_ACCOUNT_SUMMARY "
//                                + " SET "
//                                + " DG_APPLICANTCATEGORY =  '" + ApplicantCategory + "', "
//                                + " DG_VOTERID = '" + VoterID + "',"
//                                + " DG_PASSPORTNO = '" + PassPortNo + "',"
//                                + " DG_DRIVINGLICENSE = '" + DrivingLicense + "',"
//                                + " DG_LANDLINE = '" + Landline + "' "
//                                + " WHERE"
//                                + " CUSTOMERID = '" + CustomerID + "' ";
                        String DG_Update = ConfProperty.getQueryScript("PAPL_UPDATEDGDTLSQRY")
                                .replace("#DG_APPLICANTCATEGORY#", ApplicantCategory)
                                .replace("#DG_VOTERID#", VoterID)
                                .replace("#DG_PASSPORTNO#", PassPortNo)
                                .replace("#DG_DRIVINGLICENSE#", DrivingLicense)
                                .replace("#DG_LANDLINE#", Landline)
                                .replace("#CUSTOMERID#", CustomerID);
                        Log.consoleLog(ifr, "Update Query in Demographic" + DG_Update);
                        ifr.saveDataInDB(DG_Update);
                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in Demographic API new column adding " + e);
                    }

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

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                JSONObject obj = new JSONObject();
                obj.put("DOB", WriteOffDate);
                obj.put("Age", Age);
                obj.put("Years", Years);
                obj.put("writeOffPresent", writeOffPresent);
                obj.put("CustomerCategory", CustomerCategory);
                obj.put("misCodeTagValue", misCodeTagValue);
                String strCustMisStatus = getCustMISStatus(ifr, response, CustomerID);

                Log.consoleLog(ifr, "strCustMisStatus" + strCustMisStatus);
                obj.put("CustMisStatus", strCustMisStatus);
                obj.put("DateOfCustOpen", DateOfCustOpen);
                obj.put("FlgCustType", strFlgCustType);
                return obj.toJSONString();

            }

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//            Log.consoleLog(ifr, "CaptureRequestResponse Calling ");
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//            if (!(ErrorMessage.equalsIgnoreCase(""))) {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            Log.consoleLog(ifr, " CustMisStatus:::: ");
//            JSONObject obj = new JSONObject();
//            obj.put("DOB", WriteOffDate);
//            obj.put("Age", Age);
//            obj.put("Years", Years);
//            obj.put("writeOffPresent", writeOffPresent);
//            obj.put("CustomerCategory", CustomerCategory);
//            obj.put("misCodeTagValue", misCodeTagValue);
//            String strCustMisStatus = getCustMISStatus(ifr, response);
//            Log.consoleLog(ifr, "strCustMisStatus" + strCustMisStatus);
//            obj.put("CustMisStatus", strCustMisStatus);
//            obj.put("DateOfCustOpen", DateOfCustOpen);
//            return obj.toJSONString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_Demographic===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Demographic===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
    }

    public static int calculateAge(LocalDate dob) {
        LocalDate curDate = LocalDate.now();
        if ((dob != null) && (curDate != null)) {
            return Period.between(dob, curDate).getYears();
        } else {
            return 0;
        }
    }
// Added by prakash 03-02-2024 for checking Customer MIS Status 

    private String getCustMISStatus(IFormReference ifr, String Response, String CustomerID) {
        String custMisStatus = "Incomplete"; // Default value
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONParser parser = new JSONParser();
            JSONObject outputJSON = (JSONObject) parser.parse(Response);
            JSONObject bodyJSON = (JSONObject) outputJSON.get("body");
            JSONObject custDemographInqResponse = (JSONObject) bodyJSON.get("CustDemographInqResponse");

            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            Log.consoleLog(ifr, "Triggered CustomerID :: " + CustomerID);
            String BorrowerCustId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "BorrowerCustId From Basic Info Table :: " + BorrowerCustId);

            String Product = pcm.getProductCode(ifr);
            String subProduct = pcm.getSubProductCode(ifr);
 
            //added by logaraj on 12-08-2024 for f_key insert for no mis data customers
            if ((Product.equalsIgnoreCase("PL") && subProduct.equalsIgnoreCase("STP-CB")) || loan_selected.equalsIgnoreCase("Vehicle Loan") || (Product.equalsIgnoreCase("PL") && subProduct.equalsIgnoreCase("STP-CP"))) {
                String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTMIS").replaceAll("#PID#", PID);
                Log.consoleLog(ifr, "MISDATAQuery query::" + IndexQuery);
                List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
                String count = dataResult.get(0).get(0);
                Log.consoleLog(ifr, "count::" + count);
                String f_key = pcm.Fkey(ifr, "B");
                if (Integer.parseInt(count) == 0) {
                    String dataSavingINMISDataQuery = ConfProperty.getQueryScript("IndexInsertmisData").
                            replaceAll("#ProcessInstanceId#", PID)
                            .replaceAll("#InsertionOrderID#", "S_LOS_NL_MIS_DATA.nextval").
                            replaceAll("#f_key#", f_key);
                    Log.consoleLog(ifr, "insert dataSavingINMISData:::" + dataSavingINMISDataQuery);
                    ifr.saveDataInDB(dataSavingINMISDataQuery);
                    Log.consoleLog(ifr, "after insert dataSavingINMISData:::");

                }
            }

            // Checking if the tags "CustMisCode", "CustMisCode1" to "CustMisCode10" are available
            for (int i = 1; i <= 10; i++) {
                String misCodeTag = "CustMisCode" + i;

                Log.consoleLog(ifr, "misCodeTag::::" + misCodeTag);
                if (!custDemographInqResponse.containsKey(misCodeTag)) {
                    Log.consoleLog(ifr, " CustMisCode Tag not available :::: " + misCodeTag);
                    if (loan_selected.equalsIgnoreCase("Loan Against Deposit") || loan_selected.equalsIgnoreCase("Pre-Approved Personal Loan") || loan_selected.equalsIgnoreCase("Canara Budget")) {
                        Log.consoleLog(ifr, " Returned custMis Status ==> " + custMisStatus);
                        return custMisStatus; // Return MIS-Incomplete if any tag is missing
                    }
                }

                String misCodeTagValue = cf.getJsonValue(custDemographInqResponse, misCodeTag);
                Log.consoleLog(ifr, "CustMisCode Tag values ::::::: " + misCodeTagValue);

                if (misCodeTagValue.isEmpty() || misCodeTagValue.equals("{}")) {
                    Log.consoleLog(ifr, " CustMisCode Tag values are not available :::: " + misCodeTag);
                    if (loan_selected.equalsIgnoreCase("Loan Against Deposit") || loan_selected.equalsIgnoreCase("Pre-Approved Personal Loan")) {
                        Log.consoleLog(ifr, " Returned custMis Status ==> " + custMisStatus);
                        return custMisStatus;
                    }
                }

//                Commented by Aravindh on 31/07/24
//                String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
//                List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
//                String loan_selected = loanSelected.get(0).get(0);
//                Log.consoleLog(ifr, "loan type==>" + loan_selected);
                //Pension subproduct added by keerthana
                if ((Product.equalsIgnoreCase("PL") && subProduct.equalsIgnoreCase("STP-CB")) || loan_selected.equalsIgnoreCase("Vehicle Loan") || (Product.equalsIgnoreCase("PL") && subProduct.equalsIgnoreCase("STP-CP"))) {
                    //Modified By Aravindh for Updating only the borrower MIS
                    Log.consoleLog(ifr, " CustomerID = " + CustomerID + " , Basic Info Table BorrowerCustId = " + BorrowerCustId);
                    if (CustomerID.equalsIgnoreCase(BorrowerCustId)) {
                        Log.consoleLog(ifr, "Inside If : Borrower CustomerID is Matched ");
                        //Modified by Aravindh on 19-06-24 for inserting miscode instead of miscodedesc
                        String queryMISData = "SELECT misclass, miscodedesc, miscode FROM los_mst_miscode WHERE miscode='" + misCodeTagValue + "'";
                        List<List<String>> resultMISData = ifr.getDataFromDB(queryMISData);
                        for (List<String> row : resultMISData) {
                            String misclass = row.get(0);
                            // Replace spaces and dashes with underscores
                            misclass = misclass.replaceAll("[\\s-]+", "_");
                            String miscodedesc = row.get(1);
                            String misCodeValue = row.get(2);

                            // Insert into LOS_NL_MIS_DATA table for each retrieved row
                            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTMIS").replaceAll("#PID#", PID);
                            Log.consoleLog(ifr, "MISDATAQuery query::" + IndexQuery);
                            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
                            String count = dataResult.get(0).get(0);
                            Log.consoleLog(ifr, "count::" + count);
                            String f_key = pcm.Fkey(ifr, "B");
                            if (Integer.parseInt(count) == 0) {
                                String dataSavingINMISDataQuery = ConfProperty.getQueryScript("insertmisDataCB").
                                        replaceAll("#ProcessInstanceId#", PID)
                                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_MIS_DATA.nextval").
                                        replaceAll("#misclass#", misclass).
                                        replaceAll("#miscodedesc#", misCodeValue).replaceAll("#f_key#", f_key);
                                Log.consoleLog(ifr, "insert dataSavingINMISData:::" + dataSavingINMISDataQuery);
                                ifr.saveDataInDB(dataSavingINMISDataQuery);

                                Log.consoleLog(ifr, "Inserted miscodedesc: " + misCodeValue + " with misclass: " + misclass);
                                Log.consoleLog(ifr, "misclass.." + misclass);
                                Log.consoleLog(ifr, "miscodedesc.." + miscodedesc);
                                Log.consoleLog(ifr, "misCodeValue.." + misCodeValue);
                            } else {
                                String dataSavingINMISDataQuery = ConfProperty.getQueryScript("updatemisData").
                                        replaceAll("#ProcessInstanceId#", PID)
                                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_MIS_DATA.nextval").
                                        replaceAll("#misclass#", misclass).
                                        replaceAll("#miscodedesc#", misCodeValue).replaceAll("#f_key#", f_key);
                                Log.consoleLog(ifr, "update dataSavingINMISDataQuery :::" + dataSavingINMISDataQuery);
                                ifr.saveDataInDB(dataSavingINMISDataQuery);
                                Log.consoleLog(ifr, "Inserted miscodedesc: " + misCodeValue + " with misclass: " + misclass);
                                Log.consoleLog(ifr, "misclass.." + misclass);
                                Log.consoleLog(ifr, "miscodedesc.." + miscodedesc);
                                Log.consoleLog(ifr, "misCodeValue.." + misCodeValue);
                            }
                        }
                    }
                }
            }
            custMisStatus = "Complete";
            Log.consoleLog(ifr, "custMisStatus " + custMisStatus);
            return custMisStatus;
        } catch (NumberFormatException | ParseException e) {
            Log.consoleLog(ifr, "Exception/CBS_Demographic===>in getCustMISStatus " + e);
            Log.errorLog(ifr, "Exception/CBS_Demographic===>in getCustMISStatus" + e);
            return RLOS_Constants.ERROR;
        }
    }

    public String getDemographicSHL(IFormReference ifr, String ProcessInstanceId, String CustomerID) {

        String apiName = "Demographic";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        // String APIName = "CBS_Demographic";
//        String ErrorCode = "";
//        String ErrorMessage = "";
        try {
            Log.consoleLog(ifr, "Entered into ExecuteCBS_Demographic...");
            String Dataset = "";
//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
            int Age = 0;
            int Years = 0;
            String WriteOffDate = "NA";
            String writeOffPresent = "NA";
            String CustomerCategory = "";
            String misCodeTagValue = "";
            String DateOfCustOpen = "";
            String strFlgCustType = "";
            String CustMisCode1 = "";
            String CustMisCode2 = "";
            String CustMisCode3 = "";
            String CustMisCode4 = "";
            String CustMisCode5 = "";
            String CustMisCode6 = "";
            String CustMisCode7 = "";
            String CustMisCode8 = "";
            String CustMisCode9 = "";
            String CustMisCode10 = "";


            String BankCode = pcm.getConstantValue(ifr, "CBSDGRAPH", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSDGRAPH", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CBSDGRAPH", "USERID");
            String TBranch = pcm.getConstantValue(ifr, "CBSDGRAPH", "TBRANCH");

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
                    + "            \"ExternalReferenceNo\": \"\",\n"
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
                    + "        \"CustomerID\": \"" + CustomerID + "\"\n"
                    + "    }\n"
                    + "}";

            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                JSONObject resultObj = new JSONObject(OutputJSON);

                String body = resultObj.get("body").toString();
                JSONObject bodyJSON = (JSONObject) parser.parse(body);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyJSON);
                if (CheckError.equalsIgnoreCase("true")) {
                    String CustDemographInqResponse = bodyJSON.get("CustDemographInqResponse").toString();
                    JSONObject CustDemographInqResponseJSON = (JSONObject) parser.parse(CustDemographInqResponse);

                    CustomerCategory = cf.getJsonValue(CustDemographInqResponseJSON, "CustomerCategory");
                    Log.consoleLog(ifr, "CustomerCategory : " + CustomerCategory);
                    String DateOfBirth = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfBirth");
                    Log.consoleLog(ifr, "DateOfBirth===>" + DateOfBirth);

                    DateOfCustOpen = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfCustOpen");
                    Log.consoleLog(ifr, "DateOfCustOpen===>" + DateOfCustOpen);
                    WriteOffDate = cf.getJsonValue(CustDemographInqResponseJSON, "WriteOffDate");

                    //Added by Ahmed on 15-02-2024 from prod output response..
                    if (WriteOffDate.equalsIgnoreCase("{}")) {
                        WriteOffDate = "";
                    }

                    if (!WriteOffDate.equalsIgnoreCase("")) {
                        writeOffPresent = "Yes";
                    } else {
                        WriteOffDate = "01-01-1800";//Passing the currentdate to Writeoff
                        Log.consoleLog(ifr, "Writeoff is Empty!");
                    }

                    Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);

//                    Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);
//                    if (!(WriteOffDate.equalsIgnoreCase(""))) {
//                        JSONObject WriteOffDateJSON = (JSONObject) parser.parse(WriteOffDate);
//                        Log.consoleLog(ifr, "WriteOffDateJSON.size()===>" + WriteOffDateJSON.size());
//
//                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
//                        String formattedDate1 = dateFormat1.format(currentDate);
//                        if (!WriteOffDateJSON.isEmpty()) {
//                            writeOffPresent = "Yes";
//                            WriteOffDate = formattedDate1;
//                        } else {
//                            WriteOffDate = formattedDate1;
//                        }
//                    }
                    LocalDate dob = LocalDate.parse(DateOfBirth);
                    Age = calculateAge(dob);
                    Log.consoleLog(ifr, "Age===>" + Age);

                    LocalDate dateofcustopen = LocalDate.parse(DateOfCustOpen);
                    Years = calculateAge(dateofcustopen);
                    Log.consoleLog(ifr, "Years===>" + Years);

                    try {
                        Log.consoleLog(ifr, "DemoGraphic API");
                        String ApplicantCategory = cf.getJsonValue(CustDemographInqResponseJSON, "ApplicantCategory");
                        String VoterID = cf.getJsonValue(CustDemographInqResponseJSON, "VoterID");
                        String PassPortNo = cf.getJsonValue(CustDemographInqResponseJSON, "PassPortNo");
                        String DrivingLicense = cf.getJsonValue(CustDemographInqResponseJSON, "DrivingLicense");
                        String Landline = cf.getJsonValue(CustDemographInqResponseJSON, "Landline");

                        String Query = "select CASTE from los_m_caste where "
                                + "CBSCASTECODE='" + ApplicantCategory + "' and rownum=1";
                        Log.consoleLog(ifr, "Query==>" + Query);
                        List< List< String>> Result = ifr.getDataFromDB(Query);
                        Log.consoleLog(ifr, "#Result===>" + Result.toString());

                        Log.consoleLog(ifr, "#ApplicantCategory" + ApplicantCategory);
                        strFlgCustType = cf.getJsonValue(CustDemographInqResponseJSON, "FlgCustType");
                        if (Result.size() > 0) {
                            ApplicantCategory = Result.get(0).get(0);
                        }

                        String DG_Update = ConfProperty.getQueryScript("PAPL_UPDATEDGDTLSQRY")
                                .replace("#DG_APPLICANTCATEGORY#", ApplicantCategory)
                                .replace("#DG_VOTERID#", VoterID)
                                .replace("#DG_PASSPORTNO#", PassPortNo)
                                .replace("#DG_DRIVINGLICENSE#", DrivingLicense)
                                .replace("#DG_LANDLINE#", Landline)
                                .replace("#CUSTOMERID#", CustomerID);
                        Log.consoleLog(ifr, "Update Query in Demographic" + DG_Update);
                        ifr.saveDataInDB(DG_Update);

                        CustMisCode1 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode1");
                        CustMisCode2 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode2");
                        CustMisCode3 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode3");
                        CustMisCode4 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode4");
                        CustMisCode5 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode5");
                        CustMisCode6 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode6");
                        CustMisCode7 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode7");
                        CustMisCode8 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode8");
                        CustMisCode9 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode9");
                        CustMisCode10 = cf.getJsonValue(CustDemographInqResponseJSON, "CustMisCode10");

                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in Demographic API new column adding " + e);
                    }

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
                apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;
            }

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {

                JSONObject obj = new JSONObject();
                obj.put("DOB", WriteOffDate);
                obj.put("Age", Age);
                obj.put("Years", Years);
                obj.put("writeOffPresent", writeOffPresent);
                obj.put("CustomerCategory", CustomerCategory);
                obj.put("misCodeTagValue", misCodeTagValue);
                obj.put("CustMisCode1",CustMisCode1);
                obj.put("CustMisCode2",CustMisCode2);
                obj.put("CustMisCode3",CustMisCode3);
                obj.put("CustMisCode4",CustMisCode4);
                obj.put("CustMisCode5",CustMisCode5);
                obj.put("CustMisCode6",CustMisCode6);
                obj.put("CustMisCode7",CustMisCode7);
                obj.put("CustMisCode8",CustMisCode8);
                obj.put("CustMisCode9",CustMisCode9);
                obj.put("CustMisCode10",CustMisCode10);
                String strCustMisStatus = getCustMISStatus(ifr, response, CustomerID);
                obj.put("CustMisStatus", strCustMisStatus);
                // not required for home laon
//                try {
//                    String f_key = pcm.Fkey(ifr, "B");
//                    String strCustMisStatus = getCustMISStatus(ifr, response, CustomerID);
//                    Log.consoleLog(ifr, "strCustMisStatus" + strCustMisStatus);
//                    obj.put("CustMisStatus", strCustMisStatus);
//                } catch (Exception e) {
//                    Log.consoleLog(ifr, "FKey not found" + e);
//                }

//                String strCustMisStatus = getCustMISStatus(ifr, response, CustomerID);
//
//                Log.consoleLog(ifr, "strCustMisStatus" + strCustMisStatus);
//                obj.put("CustMisStatus", strCustMisStatus);
                obj.put("DateOfCustOpen", DateOfCustOpen);
                obj.put("FlgCustType", strFlgCustType);
                return obj.toJSONString();

            } else {
                return apiStatus;
            }

//            String APIStatus = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//            Log.consoleLog(ifr, "CaptureRequestResponse Calling ");
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//            if (!(ErrorMessage.equalsIgnoreCase(""))) {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//            Log.consoleLog(ifr, " CustMisStatus:::: ");
//            JSONObject obj = new JSONObject();
//            obj.put("DOB", WriteOffDate);
//            obj.put("Age", Age);
//            obj.put("Years", Years);
//            obj.put("writeOffPresent", writeOffPresent);
//            obj.put("CustomerCategory", CustomerCategory);
//            obj.put("misCodeTagValue", misCodeTagValue);
//            String strCustMisStatus = getCustMISStatus(ifr, response);
//            Log.consoleLog(ifr, "strCustMisStatus" + strCustMisStatus);
//            obj.put("CustMisStatus", strCustMisStatus);
//            obj.put("DateOfCustOpen", DateOfCustOpen);
//            return obj.toJSONString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CBS_Demographic===>" + e);
            Log.errorLog(ifr, "Exception/CBS_Demographic===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apiErrorMessage;
    }

}
