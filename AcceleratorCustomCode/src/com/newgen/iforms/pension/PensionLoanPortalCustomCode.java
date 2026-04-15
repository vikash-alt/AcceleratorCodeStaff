/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.pension;

import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.util.List;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import com.newgen.iforms.constants.RLOS_Constants;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.portalAcceleratorCode.PortalCustomCode;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import org.json.simple.JSONArray;
import com.newgen.dlp.integration.cbs.TFLEnquiry; //instaod api adding
import com.newgen.dlp.integration.common.APIPreprocessor;

import java.time.temporal.ChronoUnit;
import org.json.simple.parser.ParseException;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.exception.ExceptionUtils; ////instaod api adding

/**
 *
 * @author subham.bhakat
 */
public class PensionLoanPortalCustomCode {

    //  CommonMethods cm = new CommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
//    BRMSRules pbc;
    //   BRMSRules jsonBRMSCall = new BRMSRules();
    Advanced360EnquiryDatav2 objCbs360 = new Advanced360EnquiryDatav2();
    BRMSRules jsonBRMSCall = new BRMSRules();
    CommonMethods objcm = new CommonMethods();
    CustomerAccountSummary cbcas = new CustomerAccountSummary();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
    // LADPortalCustomCode lc = new LADPortalCustomCode();
    BRMCommonRules objbcr = new BRMCommonRules();
    //   LoanEligibilityCheck lec = new LoanEligibilityCheck();
    TFLEnquiry instaApiData = new TFLEnquiry(); //instaod api adding
    APIPreprocessor objPreprocess = new APIPreprocessor();
    String SliderFlag = "";
    String ValidateChkFlag = "";

    public String PL_fromJSSampleFunction1(IFormReference ifr, String control, String event, String value) {

        //1 - Create SendOTP Request. Call Common method for this
        //2 - Create Connection and Call -  Common Function
        //3 - Parse Response - Common Function
        //4 - Write Business Logic. If needed decompose it in to other class and method
        //Called only on onload of any form, onload of a form need to put switch case in AcceleratorBaseCode class so that execution come to this classes this method.
        return "";
    }

    public String PL_fromActivityManagerTableSampleFunction2(IFormReference ifr, String control, String event, String value) {

        //1 - Create SendOTP Request. Call Common method for this
        //2 - Create Connection and Call -  Common Function
        //3 - Parse Response - Common Function
        //4 - Write Business Logic. If needed decompose it in to other class and method
        //called onclick or some other event where we have a scope to put custom control id in iform, need to insert a row in LOS_ACTIVITY_MANAGER_RLOS to make the execution come directly to this calsses this method/
        //dont need to route from the js file 
        return "";
    }

    //modified by keerthana on 12/07/2024 for co-obligant BO knockoff &CBEX Score check 
    public String mImpPensionOnClickDocumentUploadKnockoff(IFormReference ifr, String ApplicantType) {

        JSONObject message_err = new JSONObject();

        String knockoffDecision = "";

        try {

            Log.consoleLog(ifr, " Entered into mImpPensionOnClickDocumentUpload Pension CBS_CustomerAccountSummary API Call:::");
            String processInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
            String cust_id = "";
            String mobno = "";
            String custid = "";
            String prodCode = "";
            String pCode = "";
            String NRI = "";
            String NRO = "";
            String Classification = "";
            String Aadhar = "";
            String Pan = "";
            String writeOffPresent = "";
            String DOB = "";
            String Aadhar_val = "";
            String Pan_val = "";
            String ProductType = "Pension";
            String CustTypeFlag = "";
            String loanAcctOpen = "";
            String Age = "";
            Log.consoleLog(ifr, " Pension CBS_CustomerAccountSummary API Call:::");
//            String CoObligDatasQuery = ConfProperty.getQueryScript("QUERYFORCOOBLIGDATAS").replaceAll("#ProcessInstanceId#", processInsanceId);
//            List<List<String>> CoDatas = cf.mExecuteQuery(ifr, CoObligDatasQuery, "Execute query for fetching Cooblig mob.no and custid from nl basicinfo and occupationinfo table");
//            if (CoDatas.size() > 0) {
            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                custid = pcm.getCustomerIDCB(ifr, ApplicantType);
                Log.consoleLog(ifr, "CustomerId==>" + custid);
                String f_key = bpcc.Fkey(ifr, ApplicantType);
                Log.consoleLog(ifr, "f_key==>" + f_key);
                mobno = " SELECT MOBILENO FROM LOS_L_BASIC_INFO_I where f_key='" + f_key + "'";
                List<List<String>> mobileno = ifr.getDataFromDB(mobno);
                if (!mobno.isEmpty()) {
                    mobno = mobileno.get(0).get(0);;
                }
                Log.consoleLog(ifr, "MobileNo==>" + mobno);

            } else if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                mobno = ifr.getValue("P_CP_OD_MOBILE_NUMBER").toString();
                custid = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
            }
//            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobno);
            Log.consoleLog(ifr, ".CUSTOMERID" + custid);
            HashMap<String, String> map = new HashMap<>();
            Log.consoleLog(ifr, "Pension loan map value==>" + map);
            map.clear();
            map.put("MobileNumber", mobno);
            map.put("CustomerId", custid);
            Log.consoleLog(ifr, "Pension.Map" + map);
            String cbsresp = objCustSummary.getCustomerAccountSummary(ifr, map);
            JSONParser jp = new JSONParser();
            JSONObject cbsrespobj;
            cbsrespobj = (JSONObject) jp.parse(cbsresp);
            cust_id = cbsrespobj.get("CustomerID").toString();
            Log.consoleLog(ifr, "check for co-obligant acceptence Pension");
            Aadhar = cbsrespobj.get("AadharNo").toString();
            Log.consoleLog(ifr, "AadharNo value:" + Aadhar);
            if (Aadhar != null) {

                Aadhar_val = "Yes";

            } else {

                Aadhar_val = "No";

            }

            Log.consoleLog(ifr, "AadharNo value:" + Aadhar_val);

            Pan = cbsrespobj.get("PanNumber").toString();

            Log.consoleLog(ifr, "PanNumber value:" + Pan);

            if (Pan != null) {

                Pan_val = "Yes";

            } else {

                Pan_val = "No";

            }

            Log.consoleLog(ifr, "PanNumber value:" + Pan_val);
            NRI = cbsrespobj.get("NRI").toString();

            Log.consoleLog(ifr, "NRI value:" + NRI);
            if (NRI.equalsIgnoreCase("N") || NRI.equalsIgnoreCase("NO") || NRI.equalsIgnoreCase("No")) {
                NRI = "No";
            }
            NRO = NRI;
            String SMACode = "";
            Log.consoleLog(ifr, "calling 360 api");
            String response360 = objCbs360.executeCBSAdvanced360Inquiryv2(ifr, processInsanceId, custid, "Pension", "", "");
            Log.consoleLog(ifr, "response==>" + response360);
            if (response360.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition 360API Pension");
                return RLOS_Constants.ERROR;
            } else {
                Log.consoleLog(ifr, "inside non-error condition 360API Pension");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj360 = (JSONObject) jsonparser.parse(response360);
                Log.consoleLog(ifr, obj360.toString());
                Classification = obj360.get("Classification").toString();
                Log.consoleLog(ifr, "Classification Value" + Classification);
                Classification = Classification.equalsIgnoreCase("NA") ? "No" : "Yes";
                SMACode = obj360.get("smaExists").toString();
                Log.consoleLog(ifr, "SMACode Value" + SMACode);
                loanAcctOpen = obj360.get("loanAcctOpenDatae").toString();
                Log.consoleLog(ifr, "loanAcctOpenDatae Value" + loanAcctOpen);
                pCode = obj360.get("ProductCode").toString();
                Log.consoleLog(ifr, "ProductCode Value" + pCode);
            }
            String IndvalCk = "";
            Log.consoleLog(ifr, "calling Demographic api");

            Demographic objDemographic = new Demographic();

            String GetDemoGraphicData = objDemographic.getDemographic(ifr, processInsanceId, cust_id);

            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);

            if (GetDemoGraphicData.equalsIgnoreCase(RLOS_Constants.ERROR)) {

                Log.consoleLog(ifr, "inside error condition Demographic Pension");

                return RLOS_Constants.ERROR;

            } else {

                Log.consoleLog(ifr, "inside non-error condition Demographic Pension");

                JSONParser jsonparser = new JSONParser();
                JSONObject obj1Demo = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj1Demo.toString());
                DOB = obj1Demo.get("DOB").toString();
                Log.consoleLog(ifr, "DOB Value" + DOB);
                CustTypeFlag = obj1Demo.get("FlgCustType").toString();
                Log.consoleLog(ifr, "FlagCustType Value" + CustTypeFlag);
                Age = obj1Demo.get("Age").toString();
                Log.consoleLog(ifr, "Age Value" + Age);
            }
            if (CustTypeFlag.equalsIgnoreCase("Z4") || CustTypeFlag.equalsIgnoreCase("Z8") || CustTypeFlag.equalsIgnoreCase("R4") || CustTypeFlag.equalsIgnoreCase("Z5") || CustTypeFlag.equalsIgnoreCase("Z6") || CustTypeFlag.equalsIgnoreCase("R") || CustTypeFlag.equalsIgnoreCase("S2") || CustTypeFlag.equalsIgnoreCase("Z3")) {
                IndvalCk = "Yes";
            } else {
                IndvalCk = "No";
            }
            int monthsDiff = differenceInMonths(ifr, loanAcctOpen);
            Log.consoleLog(ifr, " sinceDateMonthDiff value after calculating the monthdifference" + monthsDiff);
            String loanAcctMonthDiffs = Integer.toString(monthsDiff);
            Log.consoleLog(ifr, "Month Difference calculation:" + loanAcctMonthDiffs);
            String exStaff = "";
            if (CustTypeFlag.equalsIgnoreCase("Z4") || CustTypeFlag.equalsIgnoreCase("Z8") || CustTypeFlag.equalsIgnoreCase("R4")) {
                exStaff = "Yes";
            } else if (CustTypeFlag.equalsIgnoreCase("Z5") || CustTypeFlag.equalsIgnoreCase("Z6") || CustTypeFlag.equalsIgnoreCase("R") || CustTypeFlag.equalsIgnoreCase("S2") || CustTypeFlag.equalsIgnoreCase("Z3")) {
                exStaff = "No";
            } else {
                Log.consoleLog(ifr, "CustTypeFlag ==> " + CustTypeFlag);
                return RLOS_Constants.ERROR;
            }
            String ProductCode = "CP";
            Log.consoleLog(ifr, " Aadhar:: " + Aadhar_val + " ,Pan:: " + Pan_val + " ,WriteOffDate:: " + DOB + " ,Classification:: " + Classification + " ,ProductCode:: " + ProductCode
                    + " ,NRO:: " + NRO + " ,Nri:: " + NRI + " ,indchk::" + IndvalCk + ",CustomerAge::" + Age
                    + " ,loanaccopen:: " + monthsDiff + " ,productcode_ExistingLoan_ip::" + pCode
                    + " ,Sma2Count::" + SMACode
                    + ",ExstaffValue ::" + exStaff + "'");
            String knockoffInParams = Aadhar_val + "," + Pan_val + "," + DOB + "," + Classification + "," + "CP" + "," + NRI + "," + NRI + "," + IndvalCk + "," + Age + "," + loanAcctMonthDiffs + "," + pCode + "," + SMACode + "," + exStaff;
            Log.consoleLog(ifr, "KnockoffInParams " + knockoffInParams);
            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                Log.consoleLog(ifr, "KnockoffInParams::checkKnockOffLCPension calling " + knockoffInParams);
                knockoffDecision = checkKnockOffLCPension(ifr, "CP_Knockoff", knockoffInParams, "total_knockoff_cp_op");
            } else if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                Log.consoleLog(ifr, "KnockoffInParams::checkKnockOff calling " + knockoffInParams);
                knockoffDecision = checkKnockOff(ifr, "CP_Knockoff", knockoffInParams, "total_knockoff_cp_op");
            }
            Log.consoleLog(ifr, "KnockoffDecision fetched" + knockoffDecision);
            Log.consoleLog(ifr, "KnockoffDecision case check" + knockoffDecision.equalsIgnoreCase("Approve"));
        } catch (Exception e) {

            Log.consoleLog(ifr, "Exception:" + e);

            Log.errorLog(ifr, "Exception:" + e);

        }

        return knockoffDecision;

    }

    //modified by keerthana on 03/07/2024 to remove the time from datae of retirement
    public String autoPopulatePensionOccupationDetails(IFormReference ifr, String control, String event, String value) {

        Log.consoleLog(ifr, "inside autoPopulatePensionOccupationDetails  : ");
        JSONObject obj1 = new JSONObject();
        Log.consoleLog(ifr, "StepName : " + value);
        pcm.stepNameUpdate(ifr, value);
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CustomerId = pcm.getCustomerIDCB(ifr, "B");
        Log.consoleLog(ifr, "CustomerId Query : " + CustomerId);
        String query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
        Log.consoleLog(ifr, "MOBILENUMBER Query : " + query);
        List list = ifr.getDataFromDB(query);
        String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
        Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);
        try {
            Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);
            String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "ADDRESS QUERY : " + queryResadd);
            String queryPeradd = ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "PERMANENTADDRESS QUERY : " + queryPeradd);

            List<List<String>> resultResadd = ifr.getDataFromDB(queryResadd);
            List<List<String>> resultPeradd = ifr.getDataFromDB(queryPeradd);
            if (resultResadd.size() > 0) {
                String comAddressLine1 = resultResadd.get(0).get(0);
                String comAddressLine2 = resultResadd.get(0).get(1);
                String comAddressLine3 = resultResadd.get(0).get(2);
                String EMAILID = resultResadd.get(0).get(3);
                String state = resultResadd.get(0).get(4);
                String country = resultResadd.get(0).get(5);
                String pincode = resultResadd.get(0).get(6);
                String strmobile = resultResadd.get(0).get(7);
                ifr.setValue("P_CP_EMAIL", EMAILID);
                ifr.setValue("P_CP_OCCINFO_MOBILENO", strmobile);
                ifr.setValue("P_CP_COMMADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + "," + state + "," + country + "," + pincode);
            }
            if (resultPeradd.size() > 0) {
                String addressLine1 = resultPeradd.get(0).get(0);
                String addressLine2 = resultPeradd.get(0).get(1);
                String addressLine3 = resultPeradd.get(0).get(2);
                String pincode = resultPeradd.get(0).get(3);
                ifr.setValue("P_CP_PERMANENT_ADDRESS", addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + pincode);
            }
            String grosspension = "";
            String netpension = "";
            String dateStringWithTime = "";
            String query1 = ConfProperty.getQueryScript("PORTALOCCUPDATA1").replaceAll("#MOBILE_NUMBER#", MOBILENUMBER);
            Log.consoleLog(ifr, "Query for OccupationNonEditValues " + query1);
            List<List<String>> executeData = ifr.getDataFromDB(query1);;
            if (!executeData.isEmpty()) {

                Log.consoleLog(ifr, "Query for OccupationNonEditValues " + executeData.toString());
                //   ifr.setValue("P_CP_OCCUPATION", executeData.get(0).get(0));
                ifr.setValue("P_CP_CATEGORY", executeData.get(0).get(1));
                grosspension = executeData.get(0).get(3);
                netpension = executeData.get(0).get(2);
                ifr.setValue("P_CP_NET_PENSION", executeData.get(0).get(3));
                ifr.setValue("P_CP_GROSS_PENSION", executeData.get(0).get(2));
                ifr.setValue("P_CP_OD_SalaryAccount", executeData.get(0).get(5));
                dateStringWithTime = executeData.get(0).get(6);
            }

            Log.consoleLog(ifr, "P_CP_OD_DATEOFSUPERANNUATION dateStringWithTime " + dateStringWithTime);
            ifr.setValue("P_CP_OD_DATEOFSUPERANNUATION", dateStringWithTime);//
            Log.consoleLog(ifr, "Grosspension val@@ " + grosspension);

            if ((!grosspension.equalsIgnoreCase("")) && (!netpension.equalsIgnoreCase(""))) {

                int gpen = Integer.parseInt(grosspension);
                int npen = Integer.parseInt(netpension);
                int ded = gpen - npen;
                String DeductionPen = String.valueOf(ded);
                Log.consoleLog(ifr, "DeductionPen Applicant..:" + DeductionPen);
                ifr.setValue("P_CP_OD_DeductionFromPension", DeductionPen);
            }
            // added by logaraj for field changes on 05/07/2024 starts
            ifr.setValue("P_CP_OD_PENSIONDEDUCTION", executeData.get(0).get(7));
            ifr.setStyle("P_CP_OD_PENSIONDEDUCTION", "disable", "true");
            // added by logaraj for field changes on 05/07/2024 ends
            String Profile = ConfProperty.getCommonPropertyValue("OCCUPATIONTYPE");
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR OCCCUPATION " + Profile);
            ifr.addItemInCombo("P_CP_OD_PROFILE", Profile); //Added by monesh on 11/07/2024
            ifr.setValue("P_CP_OD_PROFILE", Profile);

            String FirstNameCB = "", MiddleNameCB = "", LastNameCB = "";
            String FullNameCBQuery = ConfProperty.getQueryScript("getFullNameCBQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "mobileData query : " + FullNameCBQuery);
            List<List<String>> FullNameCBList = ifr.getDataFromDB(FullNameCBQuery);
            if (!FullNameCBList.isEmpty()) {
                FirstNameCB = FullNameCBList.get(0).get(0);
                MiddleNameCB = FullNameCBList.get(0).get(1);
                LastNameCB = FullNameCBList.get(0).get(2);
                if (!MiddleNameCB.isEmpty()) {
                    MiddleNameCB = MiddleNameCB.replace("null", "");
                }
                if (!LastNameCB.isEmpty()) {
                    LastNameCB = LastNameCB.replace("null", "");
                }
                String Fullname = FirstNameCB + " " + MiddleNameCB + " " + LastNameCB;
                ifr.setValue("P_PAPL_CUSTOMERNAME1", Fullname.replace("  ", " "));
            }
            //modified by keerthana for years and months difference on 20/06/2024 starts
            String custstartdate = "";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = "";
            if (loanSelected.size() > 0) {
                loan_selected = loanSelected.get(0).get(0);
            }
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                String query2 = ConfProperty.getQueryScript("DATAFROMPORTALTEMP").replaceAll("#MOBILE_NUMBER#", MOBILENUMBER).replaceAll("#LOANSELECTED#", loan_selected);
                Log.consoleLog(ifr, "Query for OccupationNonEditValues loan selected value" + query2);
                List<List<String>> executecustdateData = ifr.getDataFromDB(query2);;
                if (!executecustdateData.isEmpty()) {
                    custstartdate = executecustdateData.get(0).get(0);
                }

                Log.consoleLog(ifr, " sinceDateMonthDiff value after calculating the monthdifference" + custstartdate);
                String differenceInYearsAndMonths = calculateDifferenceInYearsAndMonths(ifr, custstartdate);
                Log.consoleLog(ifr, " sinceDateYearDiff value after calculating the Yeardifference" + differenceInYearsAndMonths);
                String[] parts = differenceInYearsAndMonths.split(",");
                if (parts.length >= 2) {
                    ifr.setValue("P_CP_RELATIONSHIP", parts[0]);
                    ifr.setValue("P_CP_OD_RelationshipWithCanara_InMonths", parts[1]);
                } else {
                    Log.consoleLog(ifr, "Invalid format or no difference calculated.");
                }
            }
            //modified by keerthana for years and months difference on 20/06/2024 starts
            Demographic objDemographic = new Demographic();
            String GetDemoGraphicData = objDemographic.getDemographic(ifr, ProcessInsanceId, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition Demographic Pension");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition Demographic Pension");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj.toString());
                String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
                Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return "";

    }

    public String onLoadPensionStatus(IFormReference ifr) {//Corrected
        Log.consoleLog(ifr, "onLoadPensionStatus : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            Demographic objDemographic = new Demographic();
            String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition Demographic pension");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition Demographic pension");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj.toString());
                String CustMisStatus = obj.get("CustMisStatus").toString();
                //  CustMisStatus="Incomplete";
                Log.consoleLog(ifr, "CustMisStatus Value" + CustMisStatus);
                if (CustMisStatus.equalsIgnoreCase("Incomplete")) {
                    ifr.setStyle("P_CP_OD_Details", "visible", "true");

                    ifr.setStyle("P_CP_OD_D_Minority_Status", "visible", "true");
                    ifr.setStyle("P_CP_OD_D_Category", "visible", "true");
//                    String codMisCustCode1 = ifr.getValue("P_CP_OD_D_Minority_Status").toString();
//                    String codMisCustCode2 = ifr.getValue("P_CP_OD_D_Category").toString();
//                    if (codMisCustCode1.equalsIgnoreCase("")) {
//                        return pcm.returnErrorThroughExecute(ifr, "Please Select Minority Status!");
//                    }
//                    if (codMisCustCode2.equalsIgnoreCase("")) {
//                        return pcm.returnErrorThroughExecute(ifr, "Please Select Caste!");
//                    }

                } else {
                    ifr.setStyle("P_CP_OD_Details", "visible", "false");
                    ifr.setStyle("P_CP_OD_D_Minority_Status", "visible", "false");
                    ifr.setStyle("P_CP_OD_D_Category", "visible", "false");
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onNextClickODADMStatus");
        }
        return "";
    }

    public void autoPopulate_BranchMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "Entered into autopopulate_BM ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("PORTALPENBASICINFO1").replaceAll("#PID#", ProcessInstanceId);;
        List<List<String>> data = cf.mExecuteQuery(ifr, query, "Execute autopopulate_BM Query->");
        if (data.size() > 0) {
            String branchcode = data.get(0).get(0);
            String exst_cust = data.get(0).get(1);
            String fullName = data.get(0).get(2);
            String FN = data.get(0).get(3);
            String MN = data.get(0).get(4);
            String LN = data.get(0).get(5);
            String DOB = data.get(0).get(6);
            String fatherName = data.get(0).get(7);
            String mob_no = data.get(0).get(8);
            String emailId = data.get(0).get(9);
            String address1 = data.get(0).get(10);
            String address2 = data.get(0).get(11);
            String address3 = data.get(0).get(12);
            ifr.setValue("QNL_BASIC_INFO_FullName_label", fullName);
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName", FN);
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", FN);
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName", LN);
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", mob_no);
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", emailId);
            ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line1", address1);
            ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line2", address2);
            ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line3", address3);
        }

    }

    //autopopulate loanslider ////modified by keerthana for pension Inprinciple autopopulation on 16/06/2024 and 12/07/2024
    public String autoPopulatePrincipleApprovalDetailsPension(IFormReference ifr, String control, String event, String value) {
        String sliderValue = "";
        try {

            pcm.stepNameUpdate(ifr, value);
            String customername = "";
            Log.consoleLog(ifr, "Entered into autoPopulatePrincipleApprovalDetailsPension ");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInsanceId);
            String customerdata_Query = ConfProperty.getQueryScript("getCustomerDataQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List<List<String>> list3 = cf.mExecuteQuery(ifr, customerdata_Query, "customerdata_Query:");
            if (list3.size() > 0) {
                customername = list3.get(0).get(0);
            }
            Log.consoleLog(ifr, "Customer Name : " + customername);
            ifr.setValue("P_CP_L_CUSNAME", "Congratulations! " + customername);
            String LoanAmount_data = "";
            List<List<String>> pension_data = null;
            String data1 = ConfProperty.getQueryScript("getLInPrincipleAmountQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "pension query : " + data1);
            pension_data = ifr.getDataFromDB(data1);
            Log.consoleLog(ifr, "pension pension_data : " + pension_data);
            if (!pension_data.isEmpty()) {
                LoanAmount_data = pension_data.get(0).get(0);
            }
            Log.consoleLog(ifr, "pension getAmountQuery 1234 : " + LoanAmount_data);
            String roiData = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", ProcessInsanceId);
            Log.consoleLog(ifr, "pension roiData query : " + roiData);
            List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
            String roi = "";
            if (!loanROI.isEmpty()) {
                roi = loanROI.get(0).get(2);
            }
            Log.consoleLog(ifr, "roi : " + roi);

            String custAge = "";
            String mobile_no = "";
            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobile_no = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobile_no);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
            String strquery = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobile_no);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "Check Pension temp Data  Query:");
            if (!list1.isEmpty()) {
                custAge = list1.get(0).get(1);
                Log.consoleLog(ifr, "custAge value  : " + custAge);

            }
            String Variant = "";
            String tenure = "";
            String schemeId = "";
            String schemeID = ConfProperty.getQueryScript("SchemeVariantFetchQuery").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> schemeList = cf.mExecuteQuery(ifr, schemeID, "PenAgeschemeIdQuery:");
            if (schemeList.size() > 0) {
                schemeId = schemeList.get(0).get(0);
                Variant = schemeList.get(0).get(1);
                Log.consoleLog(ifr, "schemeId==>" + schemeId);
            }
            String dataStp = ConfProperty.getQueryScript("PenTenMaxAmtQuery").replaceAll("#SCHEMEID#", schemeId).replaceAll("#AGE#", custAge);
            List<List<String>> stplist = cf.mExecuteQuery(ifr, dataStp, "PenTenMaxAmtQuery:");
            if (stplist.size() > 0) {
                tenure = stplist.get(0).get(0);
                Log.consoleLog(ifr, "maxTen==>" + tenure);
            }
            //   String Tenure = tenure + " " + "Months";
            ifr.setValue("P_CP_PA_LOAN_AMOUNT", LoanAmount_data);
            ifr.setValue("P_CP_PA_RATE_OF_INTEREST", roi);
            ifr.setValue("P_CP_PA_TENURE", tenure);
            String loanAmount = "-" + LoanAmount_data;
            BigDecimal rate = new BigDecimal(roi);
            int tenureval = Integer.parseInt(tenure);
            BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenureval);
            String emi = emicalc.toString();
            Log.consoleLog(ifr, "emi : " + emi);
            ifr.setValue("P_CP_PA_EMI_AMT", ("₹ " + emi));
            String loanAmount1 = "";
            String tenure1 = "";
            String query1 = ConfProperty.getQueryScript("PORTALSLIDERCHECK").replaceAll("#WINAME#", ProcessInsanceId);
            Log.consoleLog(ifr, " query1 : " + query1);
            List<List<String>> list = cf.mExecuteQuery(ifr, query1, "Execute Slider Value check  ");
            Log.consoleLog(ifr, "list : " + list);
            if (list.size() > 0) {
                String Slquery = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#", ProcessInsanceId);
                Log.consoleLog(ifr, " query : " + query);
                List<List<String>> Sl = cf.mExecuteQuery(ifr, Slquery, "Execute Slider Set value on  load ");
                //   ifr.getDataFromDB(Slquery);
                //Log.consoleLog(ifr, "Execute Slider " + loanTenure);
                loanAmount1 = Sl.get(0).get(0);
                tenure1 = Sl.get(0).get(1);
                sliderValue = loanAmount1 + "," + tenure1;
//                ifr.setValue("Principle_Approval_Pension_CustomControl1", Sl.get(0).get(0));
//                ifr.setValue("Principle_Approval_Pension_CustomControl2", Sl.get(0).get(1));
            } else {

                loanAmount1 = ifr.getValue("P_CP_PA_LOAN_AMOUNT").toString();
                tenure1 = tenure;
                sliderValue = loanAmount1 + "," + tenure1;

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return sliderValue;

    }

    //modified by keerthanaR for phase-2 pension development
    //for knock-off rule and eligibility calculation
    public String mPensionOnClickOccupationDetails(IFormReference ifr, String control, String event, String value) {

        JSONObject message_err = new JSONObject();

        try {
            if (ValidateChkFlag.equalsIgnoreCase("Yes")) {
                Log.consoleLog(ifr, " Entered into mPensionOnClickOccupationDetails Pension CBS_CustomerAccountSummary API Call:::");
                String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String schemeIDPension = pcm.getSchemeID(ifr, ProcessInsanceId);
                Log.consoleLog(ifr, "schemeIDPension ==>" + schemeIDPension);

                String updateQuery = ConfProperty.getQueryScript("SchemeUpdate").replaceAll("#WINAME#", ProcessInsanceId).replaceAll("#schemeId#", schemeIDPension);
                Log.consoleLog(ifr, " updateQuery : " + updateQuery);
                ifr.saveDataInDB(updateQuery);

                CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
                String mob_id = "";
                String cust_id = "";
                String ext_cust = "";
                String mobno = "";
                String custid = "";
                String prodCode = "";
                String pCode = "";
                String minor = "";
                String NRI = "";
                String NRO = "";
                //  String Classification = "";
                String Age = "";
                String Aadhar = "";
                String Pan = "";
                String writeOffPresent = "";
                String DOB = "";
                String message = "";
                String Aadhar_val = "";
                String Pan_val = "";
                String ProductType = "Pension";
                String finalEligibleAmount = "";
                double netpension = 0;
                double obligations = 0;
                BigDecimal emiperlac;
                double net_income = 0;
                double grosspensionip = 0;
                double tak_homPension = 0;
                String maxamt_stp = "";
                String customerId1 = "";
                String mobileNumber1 = "";
                String CustTypeFlag = "";
                String DateOfBirth = "";
                String loanAcctOpen = "";
                List<List<String>> pension_data = null;
                List<List<String>> pension_stp = null;
                Log.consoleLog(ifr, " Pension CBS_CustomerAccountSummary API Call:::");

                String COMobNo = ifr.getValue("P_CP_OD_MOBILE_NUMBER").toString();
                Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobno);

                String COCustID = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
                Log.consoleLog(ifr, ".CUSTOMERID" + custid);
                String StrMessage = bpcc.coapplicantcheck(ifr, COCustID, COMobNo);
                if (StrMessage.equalsIgnoreCase("Error")) {
                    ifr.setValue("P_CP_OD_MOBILE_NUMBER", "");
                    ifr.setValue("P_CP_OD_CUSTOMER_ID", "");
                    Log.consoleLog(ifr, "before coapplicantcheck ");
                    ifr.setStyle("P_CB_OD_MOBILE_NUMBER", "disable", "false");
                    ifr.setStyle("P_CB_OD_CUSTOMER_ID", "disable", "false");
                    Log.consoleLog(ifr, "before coapplicantcheck ");
                    //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error", "Kindly enter different co-obligant customer number"));
                    message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter different co-obligant customer number"));
                    return message_err.toString();
                }
                HashMap<String, String> map = new HashMap<>();
                Log.consoleLog(ifr, "Pension loan map value==>" + map);
                //   String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String query1 = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
                Log.consoleLog(ifr, "MOBILENUMBER Query : " + query1);
                List list = ifr.getDataFromDB(query1);
                String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
                Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);
                String query2 = ConfProperty.getQueryScript("PENSIONCUSTID").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
                Log.consoleLog(ifr, "CUSTOMERID Query : " + query2);
                List listI = ifr.getDataFromDB(query2);
                String CUSTOMERID = listI.toString().replace("[", "").replace("]", "");
                Log.consoleLog(ifr, "CUSTOMERID : " + CUSTOMERID);
                map.put("MobileNumber", MOBILENUMBER);
                map.put("CustomerId", CUSTOMERID);
                String brresponse = objCustSummary.getCustomerAccountSummary(ifr, map);
                JSONParser jp = new JSONParser();
                //JSONObject msg = new JSONObject();
                JSONObject brobj;
                brobj = (JSONObject) jp.parse(brresponse);
                Log.consoleLog(ifr, "CBS call for existing customer pension " + brobj);
                String exsCus = ifr.getValue("P_CP_OD_EXISTING_CUSTOMER").toString();
                if (exsCus.equalsIgnoreCase("Yes")) {
                    exsCus = "Y";
                } else {
                    exsCus = "N";
                }
                Log.consoleLog(ifr, "CBS call for existing customer pension @ exsCus : " + exsCus);
                mobno = ifr.getValue("P_CP_OD_MOBILE_NUMBER").toString();
                Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobno);
                custid = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
                Log.consoleLog(ifr, ".CUSTOMERID" + custid);
                map.clear();
                map.put("MobileNumber", mobno);
                map.put("CustomerId", custid);
                Log.consoleLog(ifr, "Pension.Map" + map);
                String cobrresponse2 = objCustSummary.getCustomerAccountSummary(ifr, map);
                JSONParser jp2 = new JSONParser();
                JSONObject cobrobj2;
                cobrobj2 = (JSONObject) jp2.parse(cobrresponse2);
                ext_cust = cobrobj2.get("CustomerFlag").toString();
                Log.consoleLog(ifr, "CBS call for existing customer pension " + cobrobj2);
                //added by keerthana for borrower on 05/07/2024 
                String MobileNo = pcm.getMobileNumber(ifr);
                Log.consoleLog(ifr, "MobileNo==>" + MobileNo);
                String response = cbcas.getCustomerAccountParams_CB(ifr, MobileNo);//Modified on 17-1-24
                Log.consoleLog(ifr, "response/getCustomerAccountParams_CB===>" + response);
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
                //added by keerthana for borrower on 05/07/2024
                if ((!ext_cust.equalsIgnoreCase("Y")) || ext_cust.isEmpty()) {

                    Log.consoleLog(ifr, "Co obligant validation failed");
                    message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message_err.put("eflag", "false");//Hard Stop
                    return message_err.toString();
                } else {
                    Log.consoleLog(ifr, "CBS call for existing customer pension @ ext_cust: " + ext_cust);
                    mob_id = brobj.get("mobile_Number").toString();
                    Log.consoleLog(ifr, "CBS call for existing customer pension @ applicant mob_id: " + mob_id);
                    cust_id = brobj.get("CustomerID").toString();
                    Log.consoleLog(ifr, "CBS call for existing customer pension @ applicant CustomerID: " + cust_id);
                    mobileNumber1 = cobrobj2.get("mobile_Number").toString();
                    Log.consoleLog(ifr, "CBS call for existing customer pension @ co-oblig mobileNumber1: " + mobileNumber1);
                    customerId1 = cobrobj2.get("CustomerID").toString();
                    Log.consoleLog(ifr, "CBS call for existing customer pension @ co-oblig customerId1: " + customerId1);
                    if (exsCus.equalsIgnoreCase("YES") || exsCus.equalsIgnoreCase("Y")) {
                        Log.consoleLog(ifr, "checking for mobile number " + mob_id + ":" + mobileNumber1);
                        Log.consoleLog(ifr, "checking for customer number " + cust_id + ":" + customerId1);
                        String ApplicantType = "CB";
                        mobileNumber1 = mobno;
                        Log.consoleLog(ifr, "Call for co-obligant Pension");
                        //new PortalCustomCode().saveDataInPartyDetailGrid(ifr, ApplicantType, mobileNumber1 + "~" + customerId1);
                        Log.consoleLog(ifr, "inside non-error condition cbs Pension");
                        Aadhar = brobj.get("AadharNo").toString();
                        Log.consoleLog(ifr, "For bureau check:::");
                        String productCode = pcm.mGetProductCode(ifr);
                        Log.consoleLog(ifr, "ProductCode:" + productCode);
                        String subProductCode = pcm.mGetSubProductCode(ifr);
                        Log.consoleLog(ifr, "ProductCode:" + productCode);
                        //added by keerthana for cibilbureau check and datasaving on 05/07/2024 starts
                        //added by keerthana for cibilbureau check and datasaving on 05/07/2024 ends
                        //account details update on casa table.
                        addacctdetailsCP(ifr);
//                    Log.consoleLog(ifr, "score check passed moving on to knockoff check");
//                    String Familpension = "";
//                    String strPensionQr = ConfProperty.getQueryScript("PORTALFAMILYPENSIONERDATA").replaceAll("#CUSTOMER_ID#", cust_id);
//                    //  "select TOTEMIAMOUNT from LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInsanceId + "'";
//                    Log.consoleLog(ifr, "pension obligation_Query===>" + strPensionQr);
//                    List<List<String>> strPensionrs = cf.mExecuteQuery(ifr, strPensionQr, "strPensionQr:");
//                    if (strPensionrs.size() > 0) {
//                        Familpension = strPensionrs.get(0).get(0);
//                    }
//                    Log.consoleLog(ifr, "AadharNo value:" + Aadhar);
//                    if (Aadhar != null) {
//                        Aadhar_val = "Yes";
//                    } else {
//                        Aadhar_val = "No";
//                    }
//                    Log.consoleLog(ifr, "AadharNo value:" + Aadhar_val);
//                    Pan = brobj.get("PanNumber").toString();
//                    Log.consoleLog(ifr, "PanNumber value:" + Pan);
//                    if (Pan != null) {
//                        Pan_val = "Yes";
//                    } else {
//                        Pan_val = "No";
//                    }
//                    Log.consoleLog(ifr, "PanNumber value:" + Pan_val);
//                    Log.consoleLog(ifr, "inside non-error condition cbs Pension");
//                    prodCode = brobj.get("productCode").toString();
//                    Log.consoleLog(ifr, "productCode value:" + prodCode);
//
//                    Log.consoleLog(ifr, "inside non-error condition cbs Pension");
//                    NRI = brobj.get("NRI").toString();
//                    Log.consoleLog(ifr, "NRI value before handling:" + NRI);
//                    if (NRI.equalsIgnoreCase("N") || NRI.equalsIgnoreCase("No")) {
//                        NRI = "No";
//                        Log.consoleLog(ifr, "NRI value after handling:" + NRI);
//                    }
//                    NRO = NRI;
//                    //Modified by keerthana for co-obligant knockoff check on 11/07/2024
//                    String Classification = "";
//                    String SMACode = "";
//                    Log.consoleLog(ifr, "calling 360 api");
//                    String response360 = objCbs360.executeCBSAdvanced360Inquiryv2(ifr, ProcessInsanceId, custid, "Pension", "", "");
//                    Log.consoleLog(ifr, "response==>" + response360);
//                    if (response360.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                        Log.consoleLog(ifr, "inside error condition 360API Pension");
//                        return RLOS_Constants.ERROR;
//                    } else {
//                        Log.consoleLog(ifr, "inside non-error condition 360API Pension");
//                        JSONParser jsonparser = new JSONParser();
//                        JSONObject obj360 = (JSONObject) jsonparser.parse(response360);
//                        Log.consoleLog(ifr, obj360.toString());
//                        Classification = obj360.get("Classification").toString();
//                        Log.consoleLog(ifr, "Classification Value" + Classification);
//                        Classification = Classification.equalsIgnoreCase("NA") ? "No" : "Yes";
//                        SMACode = obj360.get("smaExists").toString();
//                        Log.consoleLog(ifr, "SMACode Value" + SMACode);
//                        loanAcctOpen = obj360.get("loanAcctOpenDatae").toString();
//                        Log.consoleLog(ifr, "loanAcctOpenDatae Value" + loanAcctOpen);
//                        pCode = obj360.get("ProductCode").toString();
//                        Log.consoleLog(ifr, "ProductCode Value" + pCode);
//                    }
//                    
//                    
//                    //HARDCODING FOR TESTING PURPOSE by keerthana
//                    //Classification = "No";
//                    //SMACode = "0";
//                    //HARDCODING FOR TESTING PURPOSE by keerthana
//                    String IndvalCk = "";
//                    Log.consoleLog(ifr, "calling Demographic api");
//                    Demographic objDemographic = new Demographic();
//                    String GetDemoGraphicData = objDemographic.getDemographic(ifr, ProcessInsanceId, cust_id);
//                    Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
//                    if (GetDemoGraphicData.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                        Log.consoleLog(ifr, "inside error condition Demographic Pension");
//                        return RLOS_Constants.ERROR;
//                    } else {
//                        Log.consoleLog(ifr, "inside non-error condition Demographic Pension");
//                        JSONParser jsonparser = new JSONParser();
//                        JSONObject obj1Demo = (JSONObject) jsonparser.parse(GetDemoGraphicData);
//                        Log.consoleLog(ifr, obj1Demo.toString());
////                        writeOffPresent = obj1Demo.get("writeOffPresent").toString();
////                        Log.consoleLog(ifr, "writeOffPresent Value" + writeOffPresent);
//                        DOB = obj1Demo.get("DOB").toString();
//                        Log.consoleLog(ifr, "DOB Value" + DOB);
////                        writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
//                        CustTypeFlag = obj1Demo.get("FlgCustType").toString();
//                        Log.consoleLog(ifr, "FlagCustType Value" + CustTypeFlag);
//                        //IndvalCk = CustTypeFlag.equalsIgnoreCase("") ? "Yes" : "No";
//                        Age = obj1Demo.get("Age").toString();
//                        Log.consoleLog(ifr, "Age Value" + Age);
//                    }
//                    if (CustTypeFlag.equalsIgnoreCase("Z4") || CustTypeFlag.equalsIgnoreCase("Z8") || CustTypeFlag.equalsIgnoreCase("R4") || CustTypeFlag.equalsIgnoreCase("Z5") || CustTypeFlag.equalsIgnoreCase("Z6") || CustTypeFlag.equalsIgnoreCase("R") || CustTypeFlag.equalsIgnoreCase("S2") || CustTypeFlag.equalsIgnoreCase("Z3")) {
//                        IndvalCk = "Yes";
//                    } else {
//                        IndvalCk = "No";
//                    }
//                    
//                    int monthsDiff = differenceInMonths(ifr, loanAcctOpen);
//                    Log.consoleLog(ifr, " sinceDateMonthDiff value after calculating the monthdifference" + monthsDiff);
//                    String loanAcctMonthDiffs = Integer.toString(monthsDiff);
//                    Log.consoleLog(ifr, "Month Difference calculation:" + loanAcctMonthDiffs);
//                    
//                    String exStaff = "";
//                    if (CustTypeFlag.equalsIgnoreCase("Z4") || CustTypeFlag.equalsIgnoreCase("Z8") || CustTypeFlag.equalsIgnoreCase("R4")) {
//                        exStaff = "Yes";
//                    }else if (CustTypeFlag.equalsIgnoreCase("Z5") || CustTypeFlag.equalsIgnoreCase("Z6") ||CustTypeFlag.equalsIgnoreCase("R") || CustTypeFlag.equalsIgnoreCase("S2") || CustTypeFlag.equalsIgnoreCase("Z3")) {
//                        exStaff = "No";
//                    }
//                     Log.consoleLog(ifr, " Aadhar:: " + Aadhar_val + " ,Pan:: " + Pan_val + " ,WriteOffDate:: " + DOB + " ,Classification:: " + Classification + " ,ProductCode:: " + productCode
//                            + " ,NRO:: " + NRO + " ,Nri:: " + NRI + " ,indchk::" + IndvalCk + ",CustomerAge::" + Age
//                            + " ,loanaccopen:: " + monthsDiff + " ,productcode_ExistingLoan_ip::" + pCode
//                            + " ,Sma2Count::" + SMACode
//                            + ",ExstaffValue ::" + exStaff + "'");
//                    //pCode = "607";
//                    String knockoffInParams = Aadhar_val + "," + Pan_val + "," + DOB + "," + Classification + "," + "CP" + "," + NRI + "," + NRI + "," + IndvalCk + "," + Age + "," + loanAcctMonthDiffs + "," + pCode + "," + SMACode + "," + exStaff;
//                    //"," + monthDiffs +
//                    Log.consoleLog(ifr, "KnockoffInParams " + knockoffInParams);
//                    String knockoffDecision = checkKnockOff(ifr, "CP_Knockoff", knockoffInParams, "total_knockoff_cp_op");
//                    Log.consoleLog(ifr, "KnockoffDecision fetched" + knockoffDecision);
//                    Log.consoleLog(ifr, "KnockoffDecision case check" + knockoffDecision.equalsIgnoreCase("Approve"));
//                  //Modified by keerthana for co-obligant knockoff check on 11/07/2024

                        //  if (knockoffDecision.toUpperCase().equalsIgnoreCase("Approve")) {
                        String least_score = "select distinct(BUREAUTYPE), EXP_CBSCORE from los_can_ibps_bureaucheck where PROCESSINSTANCEID='" + ProcessInsanceId + "'";
                        List<List<String>> least_scoreList = cf.mExecuteQuery(ifr, least_score, "Execute query for fetching least score data");
                        String bureau_Type = "";
                        if (least_scoreList.size() > 1) {
                            if (Integer.parseInt(least_scoreList.get(0).get(1)) < Integer.parseInt(least_scoreList.get(1).get(1))) {
                                bureau_Type = least_scoreList.get(0).get(0);
                                Log.consoleLog(ifr, " bureau_Type:::: " + bureau_Type);
                            } else {
                                bureau_Type = least_scoreList.get(1).get(0);
                                Log.consoleLog(ifr, " bureau_Type:::: " + bureau_Type);
                            }

                        }
                        //getEMIBasedOnScore=SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK WHERE BUREAUTYPE ='#BT#' AND PROCESSINSTANCEID ='#ProcessInstanceId#'

                        String loanTenure = "";
                        String reqAmount = "";
                        String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
                        List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
                        if (list4.size() > 0) {
                            reqAmount = list4.get(0).get(0);
                            loanTenure = list4.get(0).get(1);
                        }
                        Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);

//                        String TotalEmiQuery = ConfProperty.getQueryScript("getEMIBasedOnScore").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#BT#", bureau_Type);
//                        List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");
//                        Log.consoleLog(ifr, "cibiloblig TotalEmi list::" + TotalEmi);
//                        String cibiloblig = "";
//
//                        if (!TotalEmi.isEmpty()) {
//
//                            cibiloblig = TotalEmi.get(0).get(0).toString();
//
//                            Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
//
//                        } else {
//
//                            cibiloblig = "0.00";
//
//                        }
                        String BureauDataResponseBorrower = objcm.getMaxTotalEMIAmountCICDatas(ifr, "B");

                        String[] bSplitter = BureauDataResponseBorrower.split("-");

                        String bureauTypeB = bSplitter[0];

                        String ApplicantTypeB = bSplitter[1];

                        String cibiloblig = bSplitter[2];

                        //BigDecimal oblig = new BigDecimal(cibiloblig);
                        obligations = Double.parseDouble(cibiloblig);
                        String flag = "";
                        String custAge = "";
                        String mobile_no = "";
                        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
                        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
                        String loan_selected = loanSelected.get(0).get(0);
                        Log.consoleLog(ifr, "loan type==>" + loan_selected);
                        String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
                        List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
                        if (MobileDataList.size() > 0) {
                            mobile_no = MobileDataList.get(0).get(0);
                            Log.consoleLog(ifr, "MobileNo==>" + mobile_no);
                        }
                        Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
                        String strquery = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobile_no);
                        List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "Check Pension temp Data  Query:");
                        if (!list1.isEmpty()) {
                            custAge = list1.get(0).get(1);
                            Log.consoleLog(ifr, "custAge value  : " + custAge);

                        }
                        String Variant = "";
                        String tenure = "";
                        String schemeId = "";
                        String schemeID = ConfProperty.getQueryScript("SchemeVariantFetchQuery").replaceAll("#PID#", ProcessInsanceId);
                        List<List<String>> schemeList = cf.mExecuteQuery(ifr, schemeID, "PenAgeschemeIdQuery:");
                        if (schemeList.size() > 0) {
                            schemeId = schemeList.get(0).get(0);
                            Variant = schemeList.get(0).get(1);
                            Log.consoleLog(ifr, "schemeId==>" + schemeId);
                        }
                        String dataStp = ConfProperty.getQueryScript("PenTenMaxAmtQuery").replaceAll("#SCHEMEID#", schemeId).replaceAll("#AGE#", custAge);
                        List<List<String>> stplist = cf.mExecuteQuery(ifr, dataStp, "PenTenMaxAmtQuery:");
                        if (stplist.size() > 0) {
                            tenure = stplist.get(0).get(0);
                            Log.consoleLog(ifr, "maxTen==>" + tenure);
                            maxamt_stp = stplist.get(0).get(1);
                            Log.consoleLog(ifr, "maxAmt==>" + maxamt_stp);
                        }
                        Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
                        //Modified by Keerthana for Age and Eligibility Calculation on 01/07/2024
                        pension_stp = ifr.getDataFromDB(dataStp);
                        //  BigDecimal lacAmount = new BigDecimal(100000);
                        String roiData = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", ProcessInsanceId);
                        Log.consoleLog(ifr, "pension roiData query : " + roiData);
                        List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
                        String roi = "";
                        if (!loanROI.isEmpty()) {
                            roi = loanROI.get(0).get(2);
                        }
//                        String strAge = "";
//                        if (strPensionrs.size() > 0) {
//                            strAge = strPensionrs.get(0).get(1);
//                        }
                        //Modified by Keerthana for Age and Eligibility Calculation on 01/07/2024                
//                        List<List<String>> maxstp = null;
//                        String maxstpData = ConfProperty.getQueryScript("PENSIONMAXLOANAMT").replaceAll("#schemeID#", schemeID);
//                        maxstp = ifr.getDataFromDB(maxstpData);
//                        if (!maxstp.isEmpty()) {
//                            maxamt_stp = maxstp.get(0).get(0);
//                        }
                        double maxstpAmt = Double.parseDouble(maxamt_stp);
                        BigDecimal emiperlc = pcm.calculatePMT(ifr, new BigDecimal(roi), Integer.parseInt(tenure));
                        //pcm.calculateEMI(ifr, lacAmount, bigDecimal, Integer.parseInt(String.valueOf(tenure)));
                        String grosspension = ifr.getValue("P_CP_GROSS_PENSION").toString();
                        String deductionPension = ifr.getValue("P_CP_OD_PENSIONDEDUCTION").toString();//P_CP_OD_PENSIONDEDUCTION
                        double prodspeccapping = maxstpAmt;
                        HashMap hm = new HashMap();
                        hm.put("cibiloblig", cibiloblig);
                        hm.put("tenure", tenure);
                        hm.put("roi", roi);
                        hm.put("loancap", maxamt_stp);
                        hm.put("reqAmount", reqAmount);
                        hm.put("deductionmonth", deductionPension);
                        hm.put("grosssalary", grosspension);

                        String finaleligibility = null;
                        try {
                            finaleligibility = getAmountForFinalEligibilityCPDataSaveBO(ifr, hm);
                            Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);
                            //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
                            if (finaleligibility.contains(RLOS_Constants.ERROR)) {
                                return pcm.returnError(ifr);
                            } else if (finaleligibility.contains("showMessage")) {
                                return finaleligibility;
                            }
                            String query11 = "SELECT * FROM LOS_L_FINAL_ELIGIBILITY WHERE PID = '" + ProcessInsanceId + "'";
                            Log.consoleLog(ifr, "query11===>" + query11);
                            List<List<String>> result = cf.mExecuteQuery(ifr, query11, "Query for checking LOS_L_FINAL_ELIGIBILITY datas available or not");
                            Log.consoleLog(ifr, "query11 result===>" + result.size());
                            if (result.size() == 0) {
                                String Query2 = ConfProperty.getQueryScript("insertQueryforPIDinFinalEligibility").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#finalelig#", finaleligibility);
                                Log.consoleLog(ifr, "Query1===>" + Query2);
                                ifr.saveDataInDB(Query2);
                            }
                            String Query3 = ConfProperty.getQueryScript("updateQueryforPrincipleamtinFinalEligibility").replaceAll("#finalelig#", finaleligibility).replaceAll("#ProcessInstanceId#", ProcessInsanceId);
                            Log.consoleLog(ifr, "Query3===>" + Query3);
                            ifr.saveDataInDB(Query3);
                        } catch (Exception ex) {
                            Log.errorLog(ifr, "Exception:" + ex);
                        }
                        Log.consoleLog(ifr, "inside if loanAmount query : ");

//                    } else {
//
//                        Log.consoleLog(ifr, "knockoffDecision fail" + knockoffDecision);
//                        message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
//                        message_err.put("eflag", "false");
//                        return message_err.toString();
//
//                    }
                        Log.consoleLog(ifr, "Before B Data Saving");
                        savePansionOccuapationBorrower(ifr, control, event, value);
                        Log.consoleLog(ifr, "Before CB Data Saving");
                        savePansionOccuapationCoBorrower(ifr, control, event, value);
                        Log.consoleLog(ifr, "After CB Data Saving");
                        return "";
                    }
                }
                Log.consoleLog(ifr, "Co-applicant is not an existing customer");
                message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
                message_err.put("eflag", "false");
                return message_err.toString();
            } else {
                Log.consoleLog(ifr, "Please click the validate button to validate the co-obligant details");
                message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly click the validate button to validate the Co-Obligant details."));
                message_err.put("eflag", "false");
                return message_err.toString();
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception:" + e);
        }

        return pcm.returnError(ifr);
    }

    public String checkKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);
        IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
        HttpServletRequest req = iFormAPIHandler.getRequest();
        HttpSession session = req.getSession();
        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("total_knockoff_cp_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);

        try {
            Log.consoleLog(ifr, "inside checkKnockOff Pension:::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId:::" + ProcessInstanceId);
            String[] brmsKeys = {
                "cp_kyc_op", "cp_writeoffhistfromapi_op", "cp_npavalfromapi_op", "nrichk_op", "cp_indcheck_op", "cp_extperloan_op", "cp_sma_op",
                "cp_entryage_op", "total_knockoff_cp_op"
            };

            String[] ruleNames = {
                "KYC Validation CB", "WRITEOFF HISTORY CB", "NPA CHECK CB", "NRI CHECK CB", "INDIVIDUAL CHECK CB",
                "EXISTING LOAN CB", "SMA CHECK CB", "ACCOUNT ELIGIBILITY CB", "KNOCK-OFF RULES CB"
            };
            List<String> brmsOutputs = new ArrayList<>();
            for (String key : brmsKeys) {
                String value = (String) objm.get(key);
                if (value != null && value.contains("#" + key + "#")) {
                    value = "NA";
                }
                brmsOutputs.add(value == null ? "NA" : value);
                Log.consoleLog(ifr, key + ":::" + value);
            }

            Log.consoleLog(ifr, "brmsOutputs:::" + brmsOutputs);

//            String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
//                    + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
//                    + "WHERE c.PID = '" + ProcessInstanceId + "' "
//                    + "and (c.ApplicantType='CB')";
//            List<List<String>> resultData = cf.mExecuteQuery(ifr, Query1, "Query1");
//            Log.consoleLog(ifr, "resultData::" + resultData.toString());
            String query11 = "select * from LOS_NL_K_KNOCKOFFRULES where pid = '" + ProcessInstanceId + "' and PARTYTYPE = 'CO-BORROWER'";
            List<List<String>> result = cf.mExecuteQuery(ifr, query11, "Query for checking LOS_NL_K_KNOCKOFFRULES datas available or not");
            Log.consoleLog(ifr, "result.size()==>" + result.size());
            if (result.size() >= 1) {
                String query12 = "delete from LOS_NL_K_KNOCKOFFRULES where pid = '" + ProcessInstanceId + "' and PARTYTYPE = 'CO-BORROWER'";
                result = cf.mExecuteQuery(ifr, query12, "Query for deleting the LOS_NL_K_KNOCKOFFRULES datas available inside table");
            }
            query11 = "select * from LOS_KNOCKOFFRULES_PARAMETERS where pid = '" + ProcessInstanceId + "' and RULENAME LIKE '%CB'";
            result = cf.mExecuteQuery(ifr, query11, "Query for checking LOS_KNOCKOFFRULES_PARAMETERS datas available or not");
            if (result.size() > 1) {
                String query12 = "delete from LOS_KNOCKOFFRULES_PARAMETERS where pid = '" + ProcessInstanceId + "' and RULENAME LIKE '%CB'";
                result = cf.mExecuteQuery(ifr, query12, "Query for deleting the LOS_KNOCKOFFRULES_PARAMETERS datas available inside table");
            }
            String count = "";
            String knockOfGrid = "SELECT count(*) from LOS_NL_K_KNOCKOFFRULES WHERE PID = '" + ProcessInstanceId + "' ";
            List<List<String>> resultData1 = cf.mExecuteQuery(ifr, knockOfGrid, "knockOfGrid");
            if (!resultData1.isEmpty()) {
                count = resultData1.get(0).get(0);
            }
            Log.consoleLog(ifr, "ALV_KnockOffRules==>" + count);
            if (Integer.parseInt(count) == 1) {
                JSONArray arr = new JSONArray();

                // Track if borrower and co-borrower rows have been added
                boolean coBorrowerAdded = false;

                JSONObject re = new JSONObject();
                JSONArray childJsonArray = new JSONArray();
                String dataSavingQuery = "INSERT INTO LOS_NL_K_KNOCKOFFRULES "
                        + "(PARTYTYPE,RULENAME,OUTPUT,PID) "
                        + "VALUES('CO-BORROWER', 'KNOCK-OFF RULES CB', '" + totalGrade + "','" + ProcessInstanceId + "')";
                Log.consoleLog(ifr, "dataSavingQuery: " + dataSavingQuery);
                ifr.saveDataInDB(dataSavingQuery);

                for (int i = 0; i < ruleNames.length && i < brmsOutputs.size(); i++) {
                    String dataSavingQuery_child = "INSERT INTO LOS_KNOCKOFFRULES_PARAMETERS "
                            + "(RULENAME,OUTPUT,PID)"
                            + "VALUES('" + ruleNames[i] + "', '" + brmsOutputs.get(i) + "', '" + ProcessInstanceId + "' )";
                    Log.consoleLog(ifr, "dataSavingQuery: " + dataSavingQuery_child);
                    ifr.saveDataInDB(dataSavingQuery_child);
                }
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception checkKnockOff : " + ex);
            Log.errorLog(ifr, "Exception checkKnockOff : " + ex);
        }

        Log.consoleLog(ifr, "totalGrade RETURN" + totalGrade);
        return totalGrade;
    }

    public String checkFinalEligibility(IFormReference ifr, String RuleName, String values, String ValueTag) {
        try {
            JSONObject result = executeLOSBRMSRule(ifr, RuleName, values);
            if (cf.getJsonValue(result, "Status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                String Output = result.get("Output").toString();
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(Output);
                String eligibilityCP = OutputJSON.get("pension_opparams").toString();
                JSONObject eligibilityCPJson = (JSONObject) parser.parse(eligibilityCP);
                String eligibilityCPOP = cf.getJsonValue(eligibilityCPJson, ValueTag);
                Log.consoleLog(ifr, "eligibilityCPOP value:" + eligibilityCPOP);
                return eligibilityCPOP;

            } else {
                Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
                return "ERROR";
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception occured" + e);
        }
        return "";
    }

    public String mImpOnClickFianlEligibility(IFormReference ifr, String control, String event, String value) {
        try {
            Log.consoleLog(ifr, "inside mImpOnClickFianlEligibility ::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String checkbox1 = ifr.getValue("P_CP_FE_AADHAR_DOC").toString();
            String checkbox2 = ifr.getValue("P_CP_FE_SIGN").toString();
            if (checkbox1.equalsIgnoreCase("true") && (checkbox2.equalsIgnoreCase("true"))) {
                String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", ProcessInstanceId).replaceAll("#checkbox2#", checkbox2);
                Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
                ifr.saveDataInDB(queryCheckbox);
            } else {
                return pcm.returnErrorThroughExecute(ifr, "Kindly Select Both Disclaimer and Consent!");
            }

            String LoanAmount = ifr.getValue("P_CP_FE_LOAN_AMOUNT").toString();

            String Tenure = ifr.getValue("P_CP_FE_TENURE").toString();
            Ammortization AMR = new Ammortization();
            String returnMessageFromAmmortization = AMR.ExecuteCBS_Ammortization(ifr, ProcessInstanceId, LoanAmount, Tenure, "BUDGET");
            Log.consoleLog(ifr, "Ammortization Response from CBS=>" + returnMessageFromAmmortization);
            if (returnMessageFromAmmortization.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }

            String returnMessageFromDocGen = mGenerateDoc(ifr);
            if (returnMessageFromDocGen.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, "Document Generation Failed ");
            }

            EsignIntegrationChannel NESL = new EsignIntegrationChannel();
            //String returnMessageFromNESL = NESL.redirectNESLRequest(ifr, "eStamping", "Pension");
            JSONParser jp = new JSONParser();
//            if (returnMessageFromNESL.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                return pcm.returnErrorHold(ifr);
//            } else if (returnMessageFromNESL.contains("showMessage")) {
//                JSONObject obj = (JSONObject) jp.parse(returnMessageFromNESL);
//                obj.put("eflag", "false");
//                return obj.toString();
//            }
            EsignCommonMethods NESLChk = new EsignCommonMethods();
            JSONObject messagereturn = new JSONObject();
            String Status = NESLChk.checkNESLWorkflowStatus(ifr);
            Log.consoleLog(ifr, "Status from NESLChk :" + Status);
            Log.consoleLog(ifr, "Status==>" + Status);
            if (!(Status.equalsIgnoreCase(""))) {
                messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", Status));
                return messagereturn.toString();
            }
            return messagereturn.toString();

        } catch (Exception e) {

            Log.consoleLog(ifr, "mImpOnClickFianlEligibility" + e);
            Log.errorLog(ifr, "mImpOnClickFianlEligibility" + e);

        }

        return "";

    }

    public String mGenerateDoc(IFormReference ifr) {

        try {

            objcm.generatedoc(ifr, "KFS", "PENSION");

            objcm.generatedoc(ifr, "LoanAggrement", "PENSION");

            objcm.generatedoc(ifr, "SanctionLetter", "PENSION");

            objcm.generatedoc(ifr, "RepaymentLetter", "PENSION");

            //Added by Hemanth 29-03-2024 for document list
            objcm.generatedoc(ifr, "LoanApplication", "PENSION");

            objcm.generatedoc(ifr, "ProcessNote", "PENSION");
            return RLOS_Constants.SUCCESS;

        } catch (Exception e) {

            Log.consoleLog(ifr, "mGenerateDoc" + e);

            Log.errorLog(ifr, "mGenerateDoc" + e);

        }

        return RLOS_Constants.ERROR;

    }

    public JSONObject executeLOSBRMSRule(IFormReference ifr, String ruleName, String paramsData) {
        Log.consoleLog(ifr, "Inside executeBRMSRule::ruleName:" + ruleName + " ::paramsData:" + paramsData);
        JSONObject jobj = new JSONObject();
        jobj.put("status", RLOS_Constants.SUCCESS);
        try {
            String query = ConfProperty.getQueryScript(RLOS_Constants.EXECUTEBRMSRULE).replaceAll("#ruleName#", ruleName);
            Log.consoleLog(ifr, "Inside Pension EXECUTEBRMSRULE:" + query);
            List<List<String>> data = cf.mExecuteQuery(ifr, query, "BRMSRULE " + ruleName);
            for (int i = 0; i < data.size(); i++) {
                String url = ConfProperty.getCommonPropertyValue("BRMSServerIpPort") + data.get(i).get(0);
                Log.consoleLog(ifr, "url:" + url);
                String ipParams = data.get(i).get(1);
                String req = data.get(i).get(2);
                String opTag = data.get(i).get(3);
                if (url == null || url.trim().equals("") || ipParams == null || ipParams.trim().equals("")
                        || req == null || req.trim().equals("")) {
                    jobj.put("status", RLOS_Constants.ERROR);
                    return jobj;
                } else {
                    String[] p = ipParams.split(",");
                    String[] v = paramsData.split(",");
                    for (int j = 0; j < p.length; j++) {
                        req = req.replaceAll("#" + p[j] + "#", v[j]);
                    }
                    Log.consoleLog(ifr, "::Final request for BRMS " + ruleName + " is ::" + req);
                    String resp = new PortalCommonMethods().executeRestBRMS(ifr, req, url);
                    Log.consoleLog(ifr, "Final response for executeRestBRMS " + ruleName + " is :: " + resp);
                    if (resp != null && !resp.equals("")) {
                        JSONParser parser = new JSONParser();
                        JSONObject BRMSOutputJSON = (JSONObject) parser.parse(resp);
                        jobj = new JSONObject(BRMSOutputJSON);
                        Log.consoleLog(ifr, "The response Obj BRMSOutputJSONObj " + jobj);
                        return jobj;
                    } else {
                        Log.errorLog(ifr, "Response is empty");
                        jobj.put("status", RLOS_Constants.ERROR);
                        return jobj;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in executeBRMSRule:" + e);
            Log.errorLog(ifr, "Exception in executeBRMSRule:" + e);
            jobj.put("status", RLOS_Constants.ERROR);
        }
        return jobj;
    }

    public String autoPopulateFinalEligibilityDataCP(IFormReference ifr, String control, String event, String value) {
        String sliderValue = "";
        try {
            Log.consoleLog(ifr, "inside  autoPopulateFinalEligibilityDataCP");
            pcm.setGetPortalStepName(ifr, value);
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID .." + ProcessInsanceId);
            String queryCheckBox = ConfProperty.getQueryScript("PORTALCHECKBOXEE").replaceAll("#WINAME#", ProcessInsanceId);
            Log.consoleLog(ifr, "queryCheckBox : " + queryCheckBox);
            List<List<String>> listbox = ifr.getDataFromDB(queryCheckBox);
            if (!listbox.isEmpty()) {
                String checkBox = listbox.get(0).get(0);
                if (checkBox.equalsIgnoreCase("true")) {
                    ifr.setValue("P_CP_FE_AADHAR_DOC", "true");
                    ifr.setValue("P_CP_FE_SIGN", "true");
                }
            }
            /* String Query1 = ConfProperty.getQueryScript("PortalFindFinalEligibiltyCB").replaceAll("#PID#", ProcessInsanceId);
            Log.consoleLog(ifr, "Query1.." + Query1);
            List<List<String>> result = ifr.getDataFromDB(Query1);
            String tenure1 = "";
            String loanAmount1 = "";
            if (result.size() > 0) {
                loanAmount1 = result.get(0).get(0);
                Log.consoleLog(ifr, "Loanamount .." + loanAmount1);
                tenure1 = result.get(0).get(1);
                Log.consoleLog(ifr, "tenure1 .." + tenure1);
                sliderValue = loanAmount1 + "," + tenure1;
            }*/
            String tenure = "";
            String strTenureQuery = ConfProperty.getQueryScript("PENSIONQUERY").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> tenurere = cf.mExecuteQuery(ifr, strTenureQuery, "strTenureQuery execution in final eligibity ");
            if (!tenurere.isEmpty()) {
                tenure = tenurere.get(0).get(0);
                //tenure = loanAmount.get(0).get(1);

            }

            String data = ConfProperty.getQueryScript("PENLINFINALELIGTYDATA").replaceAll("#PID#", ProcessInsanceId);
            Log.consoleLog(ifr, "data.." + data);
            List<List<String>> loanAmount = ifr.getDataFromDB(data);
            Log.consoleLog(ifr, "loanAmount : " + loanAmount);
            String amount = "";

            String roi = "";
            if (!loanAmount.isEmpty()) {
                amount = loanAmount.get(0).get(0);
                //tenure = loanAmount.get(0).get(1);
                roi = loanAmount.get(0).get(2);
            }
            ifr.setValue("P_CP_FE_LOAN_AMOUNT", amount);
            ifr.setValue("P_CP_FE_TENURE", tenure);
            ifr.setValue("P_CP_FE_ROI", roi + "%");
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            //String roiData = ConfProperty.getQueryScript("PENSIONTOTALROI").replaceAll("#schemeID#", schemeID);
            //List<List<String>> loanROI = ifr.getDataFromDB(roiData);
            //List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");

            EMICalculator API = new EMICalculator();
            String emi = API.getEmiCalculatorInstallment(ifr, ProcessInsanceId, amount, tenure, roi, "Pension");
            ifr.setValue("P_CP_FE_EMI", ("? " + emi));
            String processingFee = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode='CHR30'");
            String CICCharges = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR31','CHR32')");
            String esignAndStamp = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR34','CHR35')");
            long processingFeeRound = Math.round(Double.parseDouble(processingFee));
            ifr.setValue("P_CP_FE_PROC_FEES", ("? " + String.valueOf(processingFeeRound)));
            ifr.setValue("P_CP_FE_CIC_FEES", ("? " + String.valueOf(CICCharges)));
            ifr.setValue("P_CP_FE_E_SIGN_STAMP", ("? " + String.valueOf(esignAndStamp)));
            ifr.setValue("P_CP_FE_PROCEESING_FEES", ("? " + String.valueOf(processingFeeRound)));
            Log.consoleLog(ifr, " before calling emicalculator api the values are  amount" + amount + " tenure " + tenure + " roi " + roi);
            Double OtherFee = (Double.parseDouble(processingFee) * 0.3);
            long OtherFeesRounded = Math.round(OtherFee);
            ifr.setValue("P_CP_FE_OTHER_FEES", ("? " + String.valueOf(OtherFeesRounded)));
            String Query2 = ConfProperty.getQueryScript("INSERTTRNFINALQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#amount#", amount).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi).replaceAll("#EMIAmoumt#", emi).replaceAll("#processingFeeRound#", processingFee);
            Log.consoleLog(ifr, "Query2==>" + Query2);
            ifr.saveDataInDB(Query2);
            //String Query3 = ConfProperty.getQueryScript("INSERTPROPOSEDQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            //Log.consoleLog(ifr, "Query3==>" + Query3);
            //ifr.saveDataInDB(Query3);
            String Query = ConfProperty.getQueryScript("UPDATEFINALELIPENQUERY").replaceAll("#PID#", ProcessInsanceId).replaceAll("#emi#", emi).replaceAll("#processingFee#", processingFee).replaceAll("#LoanAmount#", amount).replaceAll("#CICFee#", CICCharges).replaceAll("#signStamp#", esignAndStamp);
            Log.consoleLog(ifr, "Query :" + Query);
            ifr.saveDataInDB(Query);
            return sliderValue;

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateFinalEligibilityData CanaraPension " + e);
        }
        return "";
    }

    public String autoPopulateRecieveMoneyDataCPension(IFormReference ifr, String control, String event, String value) {
        try {
            Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyDataCPension  : ");
            pcm.setGetPortalStepName(ifr, value);
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String data = ConfProperty.getQueryScript("PENRECEIMONEY").replaceAll("#PID#", ProcessInsanceId);
            Log.consoleLog(ifr, "loanAmount query : " + data);
            List<List<String>> list = cf.mExecuteQuery(ifr, data, "autoPopulateRecieveMoneyDataCPension-->PENRECEIMONEY ");
            String amount = "";
            String tenure = "";
            String Roi = "";
            if (!list.isEmpty()) {
                amount = list.get(0).get(0);
                tenure = list.get(0).get(1);
                Roi = list.get(0).get(2);
            }
            ifr.setValue("P_CP_RM_LOAN_AMOUNT", amount);
            ifr.setValue("P_CP_NET_DISBURSEMENT_NO", amount);
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            ifr.setValue("P_CP_RM_ROI", Roi + " %");
            ifr.setValue("P_CP_PA_TENURE", tenure);
            ifr.setValue("P_CP_RM_TENURE", tenure + " Months");
            String accNoData = ConfProperty.getQueryScript("PORTALACCOUNTID").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> loanAccNo = ifr.getDataFromDB(accNoData);
            String accNo = "";
            if (!loanAccNo.isEmpty()) {
                accNo = loanAccNo.get(0).get(0);
            }
            String processingFee = "";
            String emi = "";
            ifr.setValue("P_CP_SB_ACCOUNT_NO", accNo);
            ifr.setValue("P_CP_REPAYMENT_NO", accNo);
            String Query = ConfProperty.getQueryScript("SELECTFINALELIPENQUERY").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> resultset = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "Query is " + Query);
            if (!resultset.isEmpty()) {
                processingFee = resultset.get(0).get(0).toString().replace("?", "");
                emi = resultset.get(0).get(3).toString().replace("?", "");
                String cicFee = resultset.get(0).get(1).toString().replace("?", "");
                String Esign = resultset.get(0).get(2).toString().replace("?", "");
                ifr.setValue("P_CP_RM_PROCESSINGFEE", processingFee);
                ifr.setValue("P_CP_RM_EMI", "? " + emi);
                ifr.setValue("P_CP_RM_CICFEES", cicFee);
                ifr.setValue("P_CP_RM_SIGN_STAMP", Esign);
            }

            /*String processingFee = pcm.getCalulatedProcessingFeeCB(ifr, amount);
            try {
                double processingFEES = Double.parseDouble(processingFee);
                double otherFee = processingFEES * 0.1;
                double feesAndCharges = processingFEES + otherFee;
                double feesAndChargesRound = Math.round(feesAndCharges);
                String feesAndChargesRoundStr = String.valueOf(feesAndChargesRound);
                ifr.setValue("P_CP_RM_PROCESSINGFEE", feesAndChargesRoundStr);
            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Error parsing processing fee: " + e.getMessage());
            }

            String[] emiDetails = pcm.getCalulatedLoanDetails(ifr, amount, tenure, Roi);
            String emi = emiDetails[1];
            double emiRupeeRounded = 0;
            try {
                emiRupeeRounded = Math.round(Double.parseDouble(emi));
                ifr.setValue("P_CP_RM_EMI", "₹ " + emiRupeeRounded);

            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Error parsing EMI: " + e.getMessage());
            }
            String Query = ConfProperty.getQueryScript("PORTALESIGNCICDATA").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> resultset = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "Query is " + Query);
            if (!resultset.isEmpty()) {
                String cicFee = resultset.get(0).get(0).toString().replace("₹", "");
                String Esign = resultset.get(0).get(1).toString().replace("₹", "");
                ifr.setValue("P_CP_RM_CICFEES", cicFee);
                ifr.setValue("P_CP_RM_SIGN_STAMP", Esign);

            }*/
 /* String Query1 = ConfProperty.getQueryScript("DELETEFINALELQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "Query1==>" + Query1);
            ifr.saveDataInDB(Query1);

            String Query2 = ConfProperty.getQueryScript("INSERTTRNFINALQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#amount#", amount).replaceAll("#tenure#", tenure).replaceAll("#roi#", Roi).replaceAll("#EMIAmoumt#", emi).replaceAll("#processingFeeRound#", processingFee);
            Log.consoleLog(ifr, "Query2==>" + Query2);
            ifr.saveDataInDB(Query2);*/
            //String Query3 = ConfProperty.getQueryScript("INSERTPROPOSEDQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            //Log.consoleLog(ifr, "Query3==>" + Query3);
            //ifr.saveDataInDB(Query);
            Log.consoleLog(ifr, "#Documents generated..NESL is going to trigger.");
            EsignIntegrationChannel NESL = new EsignIntegrationChannel();
           // String returnMessageFromNESL = NESL.redirectNESLRequest(ifr, "Pension", "eStamping");
//            JSONParser jp = new JSONParser();
//            if (returnMessageFromNESL.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                return pcm.returnErrorHold(ifr);
//            } else if (returnMessageFromNESL.contains("showMessage")) {
//                JSONObject obj = (JSONObject) jp.parse(returnMessageFromNESL);
//                obj.put("eflag", "false");
//                return obj.toString();
//            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
        }

        return "";
    }

    public String mAccSetSliderValueCP(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside mAccSetSliderValue : ");
        Log.consoleLog(ifr, "value : " + value);
        JSONObject message = new JSONObject();
        SliderFlag = "Yes";
        try {
            String splitValue[] = value.split(",");
            String loanAmount = splitValue[0];
            Log.consoleLog(ifr, "loanAmount : " + loanAmount);
            String tenure = splitValue[1];
            Log.consoleLog(ifr, "tenure : " + tenure);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID : " + PID);
            String query = ConfProperty.getQueryScript("PORTALSLIDERCHECK").replaceAll("#WINAME#", PID);
            Log.consoleLog(ifr, " query : " + query);
            List<List<String>> list = ifr.getDataFromDB(query);
            Log.consoleLog(ifr, "list : " + list);
            if (list.size() == 0) {
                String instQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUEINSERT").replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount).replaceAll("#Tenure#", tenure);
                Log.consoleLog(ifr, " instQuery : " + instQuery);
                ifr.saveDataInDB(instQuery);
            } else {
                String updateQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUPDATE").replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount).replaceAll("#Tenure#", tenure);
                Log.consoleLog(ifr, " updateQuery : " + updateQuery);
                ifr.saveDataInDB(updateQuery);
            }
            //vishal
            String queryProposedLoanData = "select * from LOS_NL_PROPOSED_FACILITY  Where Pid='" + PID + "'";
            List<List<String>> resultqueryProposedLoanData = ifr.getDataFromDB(queryProposedLoanData);
            if (resultqueryProposedLoanData.size() > 0) {
                String updatequeryProposedLoanData = "update LOS_NL_PROPOSED_FACILITY set REQLOANAMT='" + loanAmount + "' ,TENURE='" + tenure + "' where PID='" + PID + "'";
                Log.consoleLog(ifr, " updateQuery : " + updatequeryProposedLoanData);
                ifr.saveDataInDB(updatequeryProposedLoanData);
            }//added by keerthana for pension change on 19/07/2024
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId::Inside slider:::" + CustomerId);
            String MobileNo = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "MobileNo::Inside slider::::" + MobileNo);
            String response = cbcas.getCustomerAccountParams_CB(ifr, MobileNo);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB::::::>" + response);
            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode::Inside slider:::" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode::Inside slider:::" + subProductCode);
            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String BureauDataResponseBorrower = objcm.getMaxTotalEMIAmountCICDatas(ifr, "B");
            String[] bSplitter = BureauDataResponseBorrower.split("-");
            String bureauTypeB = bSplitter[0];
            double obligations = 0;
            String ApplicantTypeB = bSplitter[1];
            String maxamt_stp = "";
            String cibiloblig = bSplitter[2];
            obligations = Double.parseDouble(cibiloblig);
            String flag = "";
            String custAge = "";
            String mobile_no = "";
            String query7 = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query7, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", PID);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobile_no = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobile_no);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
            String strquery = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobile_no);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "Check Pension temp Data  Query:");
            if (!list1.isEmpty()) {
                custAge = list1.get(0).get(1);
                Log.consoleLog(ifr, "custAge value  : " + custAge);
            }
            String Variant = "";
            String schemeId = "";
            String schemeID = ConfProperty.getQueryScript("SchemeVariantFetchQuery").replaceAll("#PID#", PID);
            List<List<String>> schemeList = cf.mExecuteQuery(ifr, schemeID, "PenAgeschemeIdQuery:");
            if (schemeList.size() > 0) {
                schemeId = schemeList.get(0).get(0);
                Variant = schemeList.get(0).get(1);
                Log.consoleLog(ifr, "schemeId==>" + schemeId);
            }
            String dataStp = ConfProperty.getQueryScript("PenTenMaxAmtQuery").replaceAll("#SCHEMEID#", schemeId).replaceAll("#AGE#", custAge);
            List<List<String>> stplist = cf.mExecuteQuery(ifr, dataStp, "PenTenMaxAmtQuery:");
            if (stplist.size() > 0) {
                maxamt_stp = stplist.get(0).get(1);
                Log.consoleLog(ifr, "maxAmt==>" + maxamt_stp);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
            String roiData = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", PID);
            Log.consoleLog(ifr, "pension roiData query : " + roiData);
            List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
            String roi = "";
            if (!loanROI.isEmpty()) {
                roi = loanROI.get(0).get(2);
            }
            double maxstpAmt = Double.parseDouble(maxamt_stp);
            BigDecimal emiperlc = pcm.calculatePMT(ifr, new BigDecimal(roi), Integer.parseInt(tenure));
            String grosspension = ifr.getValue("P_CP_GROSS_PENSION").toString();
            String deductionPension = ifr.getValue("P_CP_OD_PENSIONDEDUCTION").toString();//P_CP_OD_PENSIONDEDUCTION
            double prodspeccapping = maxstpAmt;
            HashMap hm = new HashMap();
            hm.put("cibiloblig", cibiloblig);
            hm.put("tenure", tenure);
            hm.put("roi", roi);
            hm.put("loancap", maxamt_stp);
            hm.put("reqAmount", loanAmount);
            hm.put("deductionmonth", deductionPension);
            hm.put("grosssalary", grosspension);
            String finaleligibility = null;
            try {
                finaleligibility = getAmountForFinalEligibilityCPDataSaveBO(ifr, hm);
                Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);
                if (finaleligibility.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                } else if (finaleligibility.contains("showMessage")) {
                    return finaleligibility;
                }
            } catch (Exception ex) {
                Log.errorLog(ifr, "Exception:" + ex);
            }

            ifr.setValue("P_CP_PA_LOAN_AMOUNT", loanAmount);
            ifr.setValue("P_CP_PA_TENURE", tenure);

        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception in mAccSetSliderValue method : " + e);
        }
        return "";
    }

    public String mAccClickSendOTPRecieveMoneyCP(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyCP");
        JSONObject messagereturn = new JSONObject();
        try {

            //Added by Ahmed for NESL Showstopper
            EsignCommonMethods NESLCM = new EsignCommonMethods();
            String Status = NESLCM.checkNESLWorkflowStatus(ifr);
            Log.consoleLog(ifr, "Status==>" + Status);
            if (!(Status.equalsIgnoreCase(""))) {
                messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", Status));
                return messagereturn.toString();
            }
            String mobileno = pcm.getCurrentWiMobileNumber(ifr);
            Log.consoleLog(ifr, "Inside mobile.." + mobileno);
            String currentDate = cf.getCurrentDateTime(ifr);
            Log.consoleLog(ifr, "Inside currentDate.." + currentDate);
            String randomnum = pcm.generateRandomNumber(ifr);
            Log.consoleLog(ifr, randomnum);
            String query = ConfProperty.getQueryScript("PCOUNTOTPQuery").replaceAll("#mobileno#", mobileno);
            List<List<String>> mobilecount = cf.mExecuteQuery(ifr, query, "Count For Mobile No:");

            String otpCheck = ConfProperty.getQueryScript("OTPCHECKENABLE");
            if (otpCheck.equalsIgnoreCase("NO")) {
                randomnum = ConfProperty.getQueryScript("OTPDEFAULT");
                Log.consoleLog(ifr, "otpCheck No : " + otpCheck);
            }
            if ((mobilecount.get(0).get(0).equalsIgnoreCase("0"))) {
                query = ConfProperty.getQueryScript("PINSERTOTPQuery").replaceAll("#mobileno#", mobileno).replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
            } else {
                query = ConfProperty.getQueryScript("PUPDATEOTPQuery").replaceAll("#mobileno#", mobileno).replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
            }
            Log.consoleLog(ifr, "query:" + query);
            Log.consoleLog(ifr, query);
            int result = ifr.saveDataInDB(query);
            Log.consoleLog(ifr, "result:" + result);
            if (result > 0) {
                if (otpCheck.equalsIgnoreCase("YES")) {
                    Log.consoleLog(ifr, "otpCheck Yes : " + otpCheck);
                    JSONObject encryptData = new JSONObject();
                    encryptData.put("dest", "91" + mobileno);
                    encryptData.put("msg", "OTP is " + randomnum + " for login into Corporate Account. This OTP is valid for the duration of 10 minutes. Do not share this OTP with anyone for security reasons. \nCanara Bank");
                    encryptData.put("uname", ConfProperty.getQueryScript("SMSUName"));
                    encryptData.put("pwd", ConfProperty.getQueryScript("SMSPwd"));
                    encryptData.put("intl", "0");
                    encryptData.put("prty", "1");
                    String serviceName = "SendOTP";
                    String reqest = encryptData.toString();
                    HashMap<String, String> requestHeader = new HashMap<>();
                    String response = cf.CallWebService(ifr, serviceName, reqest, "", requestHeader);
                    Log.consoleLog(ifr, "SMS OTP Response : " + response);
                }
            }

            ifr.setStyle("P_CP_RM_OTP_RESEND", "visible", "false");
            ifr.setStyle("P_CP_ENTEROTP", "visible", "true");
            ifr.setStyle("P_CP_RM_VALIDATE", "visible", "true");
            ifr.setStyle("Portal_L_Timer_Level", "visible", "true");
            ifr.setStyle("Portal_L_Timer", "visible", "true");
            messagereturn.put("clearOTPTypeField", "P_CP_ENTEROTP");
            messagereturn.put("retValue", "optRMPension");
            return messagereturn.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mAccClickSendOTPRecieveMoneyCP : " + e);
            Log.errorLog(ifr, "Exception mAccClickSendOTPRecieveMoneyCP : " + e);
        }
        return "";
    }

    public String mAccValidateOTPRecieveMoneyCP(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyCP  : ");
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
        String formattedDate = dateFormat.format(currentDate);
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        JSONObject re = new JSONObject();
        String enterOTP = ifr.getValue("P_CP_ENTEROTP").toString();
        Log.consoleLog(ifr, "enterOTP ; " + enterOTP);
        if (enterOTP.equalsIgnoreCase("")) {
            re.put("showMessage", cf.showMessage(ifr, "P_CP_RM_VALIDATE", "error", "Kindly Enter OTP"));
            return re.toString();
        }
        String sendOTP = "";
        String mobileNumber = pcm.getCurrentWiMobileNumber(ifr);
        String query = ConfProperty.getQueryScript("POTPQuery").replaceAll("#mobileNumber#", mobileNumber);
        Log.consoleLog(ifr, "query:" + query);
        List<List<String>> OTPD = cf.mExecuteQuery(ifr, query, "Query for Pension OTP Check:");
        if (!OTPD.isEmpty()) {
            sendOTP = OTPD.get(0).get(0);
            Log.consoleLog(ifr, "sendOTP==>" + sendOTP);
        }
        if (enterOTP.equalsIgnoreCase(sendOTP)) {
            try {
                Log.consoleLog(ifr, "execLoanAccCreateAndDisbure is calling.....");
                //PensionDisbursementScreen pds = new PensionDisbursementScreen();//Commemted by Ahmed on 01-07-2024 for APIPreprocessor Validations
                //String LoanDisbStatus = pds.CBSPensionFinalScreenValidation(ifr, PID);//Commemted by Ahmed on 01-07-2024 for APIPreprocessor Validations
                //String LoanDisbStatus = pds.CBSFinalScreenValidation(ifr, PID);//Commemted by Ahmed on 02-07-2024 for APIPreprocessor Validations
                String LoanDisbStatus = execLoanAccCreateAndDisbure(ifr, PID);//Modified by Ahmed on 02-07-2024 for APIPreprocessor Validations

                if (LoanDisbStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                Log.consoleLog(ifr, "before setPortalDataReceiveMoneyCP:");
                setPortalDataReceiveMoneyCP(ifr, control, event, value);
                Log.consoleLog(ifr, "after setPortalDataReceiveMoneyCP:");
                re.put("NavigationNextClick", "true");
            } catch (Exception e) {
                Log.consoleLog(ifr, "Excpetion:" + e);
                Log.errorLog(ifr, "Excpetion:" + e);
            }
        } else {

            re.put("showMessage", cf.showMessage(ifr, "P_CP_RM_VALIDATE", "error", "Kindly Enter Correct OTP"));
            re.put("clearOTPTypeField", "P_CP_ENTEROTP");
            return re.toString();
        }
        return re.toString();
    }

    public String setPortalDataReceiveMoneyCP(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside setPortalDataReceiveMoneyCP ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String LoanAmount = ifr.getValue("P_CP_RM_LOAN_AMOUNT").toString();
            String tenure = ifr.getValue("P_CP_RM_TENURE").toString();
            String roi1 = ifr.getValue("P_CP_RM_ROI").toString();
            String ROI[] = roi1.split("%");
            String roi = ROI[0];
            String emi1 = ifr.getValue("P_CP_RM_EMI").toString();
            String EMI[] = emi1.split(" ");
            String emi = EMI[1];
            String processingFee1 = ifr.getValue("P_CP_RM_PROCESSINGFEE").toString();
            String ProFee[] = processingFee1.split("\\?");
            String processingFee = ProFee[1];
            String CICFee = ifr.getValue("P_CP_RM_CICFEES").toString();
            String signStamp = ifr.getValue("P_CP_RM_SIGN_STAMP").toString();
            String SbAccNo = ifr.getValue("P_CP_SB_ACCOUNT_NO").toString();
            String repaymentAccNo = ifr.getValue("P_CP_REPAYMENT_NO").toString();
            String NetDisbrusmentAmnt = ifr.getValue("P_CP_NET_DISBURSEMENT_NO").toString();
            String Query = ConfProperty.getQueryScript("UPDATERECEIVEMONEYQUERY").replaceAll("#PID#", PID).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi).replaceAll("#emi#", emi).replaceAll("#processingFee#", processingFee).replaceAll("#LoanAmount#", LoanAmount).replaceAll("#CICFee#", CICFee).replaceAll("#signStamp#", signStamp).replaceAll("#SbAccNo#", SbAccNo).replaceAll("#repaymentAccNo#", repaymentAccNo).replaceAll("#NetDisbrusmentAmnt#", NetDisbrusmentAmnt);
            Log.consoleLog(ifr, "Query :" + Query);
            ifr.saveDataInDB(Query);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception .." + e);
        }
        return "";
    }

    public void autoPopulateFinalScreenDataCPension(IFormReference ifr, String control, String event, String value) {
        try {
            Log.consoleLog(ifr, "inside autoPopulateFinalScreenDataCPension  : ");
            pcm.setGetPortalStepName(ifr, value);
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String FinalLoanDetailsCP = ConfProperty.getQueryScript("getFinalScreenData").replaceAll("#PID#", ProcessInsanceId);;
            Log.consoleLog(ifr, "FinalLoanDetailsCP query : " + FinalLoanDetailsCP);
            List<List<String>> FinalLoanDetailsList = ifr.getDataFromDB(FinalLoanDetailsCP);
            Log.consoleLog(ifr, "FinalLoanDetailsList CP : " + FinalLoanDetailsList);
            String APPLICATION_REFNO = "";
            String FinalLoanAccNo = "";
            String FinalSanctionAmt = "";
            String EMIDATE = "";
            if (!FinalLoanDetailsList.isEmpty()) {
                APPLICATION_REFNO = FinalLoanDetailsList.get(0).get(0);
                FinalLoanAccNo = FinalLoanDetailsList.get(0).get(1);
                FinalSanctionAmt = FinalLoanDetailsList.get(0).get(2);
                EMIDATE = FinalLoanDetailsList.get(0).get(3);
            }

            if (APPLICATION_REFNO.equalsIgnoreCase("") || APPLICATION_REFNO.equalsIgnoreCase("null")) {
                APPLICATION_REFNO = ifr.getValue("P_PAPL_REFERENCENO1").toString();
                Log.consoleLog(ifr, "ApplicationRefNo Updated : " + APPLICATION_REFNO);
            }
            ifr.setValue("P_CP_FINAL_REF_NO", APPLICATION_REFNO);
            ifr.setValue("P_CP_FS_LOAN_ACC_NO", FinalLoanAccNo);
            ifr.setValue("P_CP_FS_SANCTIONED_LOAN", FinalSanctionAmt);
            ifr.setValue("P_CP_FINAL_EMIDATE", EMIDATE);

            //current Time 
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            ifr.setValue("P_CP_FINAL_DATEOFDISBURSEMENT", dtf.format(now));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateFinalScreenDataCPension " + e);
        }
    }

    public void setGetPortalStepNamePension(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside  documentUploadPension");
        pcm.stepNameUpdate(ifr, value);
    }

    public String mImpPensionOnClickDocumentUpload(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String strConsentquery = ConfProperty.getQueryScript("PORTALCONSENTSTATUS").replaceAll("#PID#", PID);
        List<List<String>> strConsentrs = cf.mExecuteQuery(ifr, strConsentquery, "mImpPensionOnClickDocumentUpload ::: Execute query for consentreceived Received ");
        Log.consoleLog(ifr, " Inside mImpPensionOnClickDocumentUpload" + strConsentquery);
        String strConsent = "";

        if (strConsentrs.size() > 0) {

            strConsent = strConsentrs.get(0).get(0);
            Log.consoleLog(ifr, "mImpPensionOnClickDocumentUpload" + strConsent);

            if (strConsent.equalsIgnoreCase("Accepted")) {

                JSONObject message_err = new JSONObject();
//                String knockoffDecision = mImpPensionOnClickDocumentUploadKnockoff(ifr);
//
//                if (knockoffDecision.toUpperCase().equalsIgnoreCase("PROCEED")) {
//
//                    Log.consoleLog(ifr, "pension document knockoffDecision Passed Successfully:::");
//
//                } else {
//
//                    Log.consoleLog(ifr, "pension document knockoffDecision fail" + knockoffDecision);
//
//                    message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
//
//                    message_err.put("eflag", "false");
//
//                    return message_err.toString();
//
//                }
                return RLOS_Constants.SUCCESS;
            }
            return RLOS_Constants.SUCCESS;
        } else {
            return pcm.returnErrorAPIThroughExecute(ifr);
        }

    }

    public void populateOccDetails(IFormReference ifr, String Control, String Event, String JSdata) {

        Log.consoleLog(ifr, "Inside populateOccDetails");

        WDGeneralData Data = ifr.getObjGeneralData();

        String ProcessInsanceId = Data.getM_strProcessInstanceId();

        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInsanceId);

        // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInsanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);

        try {

            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");

            String loan_selected = "";

            if (loanSelected.size() > 0) {

                loan_selected = loanSelected.get(0).get(0);

            }

            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            if (loan_selected.equalsIgnoreCase("Canara Pension")) {

                String queryData = ConfProperty.getQueryScript("PORTALPENSIONOCCUPATIONDET").replaceAll("#PID#", ProcessInsanceId);

                Log.consoleLog(ifr, "datas   : " + queryData);

                List<List<String>> dataQuery = ifr.getDataFromDB(queryData);

                Log.consoleLog(ifr, "dataQuery Size: " + dataQuery.size());

                Log.consoleLog(ifr, "dataQuery for inserting pension datas in los_nl_occupation_info 1 : " + dataQuery);

                String netPension = "";

                String grossPension = "";

                String profile = "";

                String category = "";

                String purpose = "";

                String relation = "";

                if (!dataQuery.isEmpty()) {

                    netPension = dataQuery.get(0).get(0);

                    Log.consoleLog(ifr, "Net Pension" + netPension);

                    grossPension = dataQuery.get(0).get(1);

                    Log.consoleLog(ifr, "Gross Pension" + grossPension);

                    profile = dataQuery.get(0).get(2);

                    category = dataQuery.get(0).get(3);

                    purpose = dataQuery.get(0).get(4);

                    relation = dataQuery.get(0).get(5);

                }

                Integer deduction = Integer.parseInt(grossPension) - Integer.parseInt(netPension);

                String deductionMonthly = deduction.toString();

                String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO1").replaceAll("#PID#", ProcessInsanceId);

                Log.consoleLog(ifr, "OcuppationInfoDetails1 query for pension::" + IndexQuery1);

                List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);

                String f_key = dataResult1.get(0).get(0);

                // String applicantData = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b \n"
                //   + "inner JOIN LOS_NL_BASIC_INFO c   ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + ProcessInsanceId + "' AND  c.ApplicantType ='B'";
                String applicantData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInsanceId);

                List<List<String>> data = cf.mExecuteQuery(ifr, applicantData, "Execute query for fetching customer data");

                String party_type = "";

                if (!data.isEmpty()) {

                    party_type = data.get(0).get(0);

                    Log.consoleLog(ifr, "Party Type==>" + party_type);

                }

                // String countQuery = "select * from los_nl_occupation_info where PID='" + ProcessInsanceId + "'";
                String countQuery = ConfProperty.getQueryScript("los_nl_occupation_info_count").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
                List<List<String>> dataCount = ifr.getDataFromDB(countQuery);

                int RowSize = dataCount.size();

                Log.consoleLog(ifr, "Row count" + RowSize);

                if (RowSize <= 0) {

                    Log.consoleLog(ifr, "Row count for pension" + RowSize);

                    // String insertQuery = "INSERT INTO los_nl_occupation_info "
                    //  + "(PID,NET_PENSION,GROSS_PENSION,DEDUCTIONMONTH,APPLICANTTYPE,F_KEY,OCCUPATIONTYPE,CATEGORY,PURPOSE,RELATIONSHIPCANARA)\n"
                    // + "VALUES ('" + ProcessInsanceId + "','" + netPension + "','" + grossPension + "','" + deductionMonthly + "','" + party_type + "','" + f_key + "','" + profile + "','" + category + "','" + purpose + "','" + relation + "')";
                    //  String insertQuery = ConfProperty.getQueryScript("OccInfoInsert").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#netPension#", netPension).replaceAll("#grossPension#", grossPension).replaceAll("#deductionMonthly#", deductionMonthly).replaceAll("#party_type#", party_type).replaceAll("#f_key#", f_key).replaceAll("#profile#", profile).replaceAll("#category#", category).replaceAll("#purpose#", purpose).replaceAll("#relation#", relation);
                    // Log.consoleLog(ifr, "insert query for pension===>" + insertQuery);
                    String insertQuery = ConfProperty.getQueryScript("OccInfoUpdate").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#netPension#", netPension).replaceAll("#grossPension#", grossPension).replaceAll("#deductionMonthly#", deductionMonthly).replaceAll("#party_type#", party_type).replaceAll("#f_key#", f_key).replaceAll("#profile#", profile).replaceAll("#category#", category).replaceAll("#purpose#", purpose).replaceAll("#relation#", relation);
                    Log.consoleLog(ifr, "insert query for pension===>" + insertQuery);
                    ifr.saveDataInDB(insertQuery);
                }

            }

        } catch (Exception ex) {

            Log.consoleLog(ifr, "Exception IN populateOccDetails " + ex);

        }

    }

    public void autoPupulateBueroConsentFromPortalPension(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            Log.consoleLog(ifr, "Inside autoPupulateBueroConsentFromPortal Pension::");

            // String bueroConsentTableQuery = "select PID from LOS_NL_BUREAU_CONSENT where PID='" + ProcessInstanceId + "'";
            String bueroConsentTableQuery = ConfProperty.getQueryScript("bueroConsentTableQueryPen").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

            Log.consoleLog(ifr, "bueroTableQuery ::" + bueroConsentTableQuery);
            List<List<String>> bueroConsentTableData = ifr.getDataFromDB(bueroConsentTableQuery);
            if (bueroConsentTableData.size() == 0) {
                // String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='CB' and PID='" + ProcessInstanceId + "'";
                String borrowerQuery = ConfProperty.getQueryScript("borrowerQueryPen").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                Log.consoleLog(ifr, "borrowerQuery for consent::" + borrowerQuery);
                List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                Log.consoleLog(ifr, "borrowerQueryData for consent::" + borrowerQueryData);
                if (borrowerQueryData.size() > 0) {
                    Log.consoleLog(ifr, "appType:: " + borrowerQueryData.get(0).get(0));
                    objcm.mInsertBureauConsent(ifr, ProcessInstanceId, borrowerQueryData.get(0).get(0));
                    String checkINConsentGridQuery = "Select * from LOS_NL_BUREAU_CONSENT where PID='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "insert dataSavingINConsentGridQuery for consent" + checkINConsentGridQuery);
                    List<List<String>> checkbrowwerdata = cf.mExecuteQuery(ifr, checkINConsentGridQuery, "Part check ");
                    Log.consoleLog(ifr, "Size check:::" + checkbrowwerdata.size());
                    String dataSavingINConsentGridQuery = "";

                    if (checkbrowwerdata.size() > 0) {
                        Log.consoleLog(ifr, "inside checkbrowwerdata greater than 0");
                        String deleteINConsentGridQuery = "delete from LOS_NL_BUREAU_CONSENT where PID='" + ProcessInstanceId + "'";
                        cf.mExecuteQuery(ifr, deleteINConsentGridQuery, "Delete query");
                        dataSavingINConsentGridQuery = "insert into LOS_NL_BUREAU_CONSENT (PID,InsertionOrderID,PartyType,Methodology,ConsentReceived) values('" + ProcessInstanceId + "',S_LOS_NL_BUREAU_CONSENT.nextval,'" + borrowerQueryData.get(0).get(0) + "','P','Initiated')";
                    } else {
                        Log.consoleLog(ifr, "inside checkbrowwerdata less than 0");
                        dataSavingINConsentGridQuery = "insert into LOS_NL_BUREAU_CONSENT (PID,InsertionOrderID,PartyType,Methodology,ConsentReceived) values('" + ProcessInstanceId + "',S_LOS_NL_BUREAU_CONSENT.nextval,'" + borrowerQueryData.get(0).get(0) + "','P','Initiated')";
                    }
                    Log.consoleLog(ifr, "insert dataSavingINConsentGridQuery for consent" + dataSavingINConsentGridQuery);

                    /*
                    Email em = new Email();
                    String emailId = pcm.getCurrentEmailId(ifr, "Canara Pension", "CB");
                    Log.consoleLog(ifr, "autoPupulateBueroConsentFromPortal :emailId::" + emailId);
                    em.sendEmail(ifr, ProcessInstanceId, emailId, "", "CB", "RETAIL", "6");//Need to add template in table CAN_MST_EMAIL_HEADERS
                  
                     */
                    ifr.saveDataInDB(dataSavingINConsentGridQuery);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in autoPupulateBueroConsentFromPortal::" + e);
            Log.errorLog(ifr, "Exception in  autoPupulateBueroConsentFromPortal::" + e);
        }
    }

    public void popluateDocumentsUploadCP(IFormReference ifr) {
        Log.consoleLog(ifr, "inside the popluateDocumentsUploadCP");
        try {
            String Coobligant = " ";
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
            int rowCount = docResultData.size();
            Log.consoleLog(ifr, "PensionLoanPortalCustomCode:popluateDocumentsUploadCP->rowCount::" + rowCount);
            if (rowCount == 0) {
                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b  inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID = '" + PID + "' and (c.ApplicantType='B' or c.ApplicantType='CB')";
                Log.consoleLog(ifr, "Query1 data::" + Query1);
                List<List<String>> resultData = ifr.getDataFromDB(Query1);
                Log.consoleLog(ifr, "resultData::" + resultData.toString());
                String applicantName[] = new String[resultData.size()];
                if (resultData.size() > 0) {
                    for (int i = 0; i < resultData.size(); i++) {
                        applicantName[i] = resultData.get(i).get(0);
                        Log.consoleLog(ifr, "applicantName is " + applicantName);
                    }
                }
                for (int i = 0; i < applicantName.length; i++) {
                    Log.consoleLog(ifr, "Inside second for loop::");
                    String applicantNameArrayData = applicantName[i];
                    String applicantType = "";
                    if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                        applicantType = "C";
                    } else if (applicantNameArrayData.contains("Borrower")) {
                        applicantType = "B";
                    }
                    Log.consoleLog(ifr, "applicantType value1::" + applicantType);
                    String query = ConfProperty.getQueryScript("PORTALDOUMENTQUERY").replaceAll("#applicablefor#", applicantType);
                    Log.consoleLog(ifr, "finalquery value1::" + query);
                    List<List<String>> result = ifr.getDataFromDB(query);
                    Log.consoleLog(ifr, "result is.." + result);
                    int resultsize = result.size();
                    Log.consoleLog(ifr, "size is " + resultsize);
                    if (resultsize > 0) {
                        JSONArray arr = new JSONArray();
                        for (int j = 0; j < result.size(); j++) {
                            JSONObject re = new JSONObject();

                            if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                                re.put("Mandatory", result.get(j).get(1));
                                re.put("Document Type ", result.get(j).get(0) + Coobligant);
                                re.put("Applicant Type", applicantName[i]);
                                re.put("DMSName", result.get(j).get(2));
                                arr.add(re);
//                            re.put("Applicant Type", applicantName[i]);
//                            arr.add(re);
                            } else {
                                re.put("Document Type ", result.get(j).get(0));
                                re.put("Mandatory", result.get(j).get(1));
                                re.put("Applicant Type", applicantName[i]);
                                re.put("DMSName", result.get(j).get(2));
                                arr.add(re);
                            }
                        }
                        Log.consoleLog(ifr, "Document grid  json array::" + arr);
                        ifr.addDataToGrid("CP_UPLOAD_DOCUMENT", arr);
                    }
                }
            }
            ifr.setColumnDisable("CP_UPLOAD_DOCUMENT", "1", true);
            ifr.setColumnDisable("CP_UPLOAD_DOCUMENT", "2", true);
            ifr.setColumnDisable("CP_UPLOAD_DOCUMENT", "4", true);
            ifr.setColumnDisable("CP_UPLOAD_DOCUMENT", "3", true);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the popluateDocumentsUploadCb" + e);
        }
    }

    public void incomeGridFieldVisibility(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside incomeGridFieldVisibility Pensionn==>");
        try {

            WDGeneralData Data = ifr.getObjGeneralData();

            String ProcessInstanceId = Data.getM_strProcessInstanceId();

            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");

            String loan_selected = loanSelected.get(0).get(0);

            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                String[] FieldVisibleFalse = new String[]{"QA_FI_PI_MINCOME_srcIncCombo",
                    "QA_FI_PI_MINCOME_occCombo",
                    "QA_FI_PI_MINCOME_occTxt",
                    "QA_FI_PI_MINCOME_GrossAvgAmt",
                    "QA_FI_PI_MINCOME_NetAvgAmt",
                    "QA_FI_PI_MINCOME_Description",
                    "table601_table620"};

                String[] FieldVisibleTrue = new String[]{"QA_FI_PI_MINCOME_GrossAmt",
                    "QA_FI_PI_MINCOME_NetAmount",
                    "QA_FI_PI_MINCOME_IncSource",
                    "QA_FI_PI_MINCOME_CustType",
                    "QA_FI_PI_MINCOME_Deduction"};

                for (int i = 0; i < FieldVisibleFalse.length; i++) {
                    ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                }

                for (int i = 0; i < FieldVisibleTrue.length; i++) {
                    ifr.setStyle(FieldVisibleTrue[i], "visible", "true");
                    ifr.setStyle(FieldVisibleTrue[i], "disable", "true");

                }
                ifr.setStyle("QA_FI_PI_MINCOME_GrossAmt", "disable", "false");
                ifr.setStyle("QA_FI_PI_MINCOME_NetAmount", "disable", "false");
                ifr.setStyle("QA_FI_PI_MINCOME_IncSource", "disable", "false");
                ifr.setStyle("QA_FI_PI_MINCOME_ConsiderEligibilty", "disable", "false");

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in incomeGridFieldVisibility::" + e);
        }
    }

    //modified by keerthana on 12/07/2024 for co-obligant BO knockoff &CBEX Score check 
    public String pensionCoBoEligibility(IFormReference ifr, String Control, String Event, String value) {
        try {
            Log.consoleLog(ifr, "inside pensionCoBoEligibility");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONArray LV_BCGrid = ifr.getDataFromGrid("ALV_BUREAU_CONSENT");
            String consentFlag = "";
            for (int i = 0; i < LV_BCGrid.size(); i++) {
                JSONObject LV_BCGridData = (JSONObject) LV_BCGrid.get(i);
                Log.consoleLog(ifr, "LV_BCGridData:: " + LV_BCGridData.toString());
                String appType = (String) LV_BCGridData.get("QNL_BUREAU_CONSENT_PartyType");
                Log.consoleLog(ifr, "appType:: " + appType);
                String selectQuery = ConfProperty.getQueryScript("BureauConsent").replaceAll("#PID#", PID).replaceAll("#PARTYTYPE#", appType);
                Log.consoleLog(ifr, "selectQuery::" + selectQuery);
                List<List<String>> bureauList = ifr.getDataFromDB(selectQuery);
                if (bureauList.size() > 0) {
                    Log.consoleLog(ifr, "Inside if bureau list");
                    consentFlag = bureauList.get(0).get(0);
                    Log.consoleLog(ifr, "consentFlag:::" + consentFlag);
                }

                if (consentFlag.equalsIgnoreCase("Y")) {
                    JSONObject message_err = new JSONObject();
//                        Log.consoleLog(ifr, "pension document knockoffDecision Passed Successfully:::");
//                        JSONArray gridResultSet = new JSONArray();
//                        String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
//                        org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
//                        Log.consoleLog(ifr,
//                                "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);
//
//                        String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
//                        Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);
//
//                        JSONObject formDetailsJson = new JSONObject();
//                        formDetailsJson.put("Name", coBorrowerName);
//                        formDetailsJson.put("Eligibility Status", "Eligible");
//
//                        gridResultSet.add(formDetailsJson);
//                        ifr.clearTable("CB_Eligibility_Grid");
//                        ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
//                        Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());
//
//                    } else if (knockoffDecision.toUpperCase().equalsIgnoreCase("REJECT")) {
//                        Log.consoleLog(ifr, " Pension knockoffDecision fail " + knockoffDecision);
//                        message_err.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error",
//                                "Co-Boorrower is not eligible for the selected digital loan journey."));
//                        message_err.put("eflag", "false");
//                        JSONArray gridResultSet = new JSONArray();
//                        String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
//                        org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
//                        Log.consoleLog(ifr,
//                                "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);
//                        String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
//                        Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);
//
//                        JSONObject formDetailsJson = new JSONObject();
//                        formDetailsJson.put("Name", coBorrowerName);
//                        formDetailsJson.put("Eligibility Status", "Not Eligible");
//
//                        gridResultSet.add(formDetailsJson);
//                        ifr.clearTable("CB_Eligibility_Grid");
//                        ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
//                        Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());
//                        ifr.setStyle("Btn_Refresh", "disable", "true");
//                        return message_err.toString();
                    String mobno = "";
                    String custid = "";
                    String Aadhar = "";
                    String CoObligDatasQuery = ConfProperty.getQueryScript("QUERYFORCOOBLIGDATAS").replaceAll("#ProcessInstanceId#", PID);
                    List<List<String>> CoDatas = cf.mExecuteQuery(ifr, CoObligDatasQuery, "Execute query for fetching Cooblig mob.no and custid from nl basicinfo and occupationinfo table");
                    if (CoDatas.size() > 0) {
                        mobno = CoDatas.get(0).get(0);
                        custid = CoDatas.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobno);
                    Log.consoleLog(ifr, ".CUSTOMERID" + custid);
                    HashMap<String, String> map = new HashMap<>();
                    Log.consoleLog(ifr, "Pension loan map value==>" + map);
                    map.clear();
                    map.put("MobileNumber", mobno);
                    map.put("CustomerId", custid);
                    Log.consoleLog(ifr, "Pension.Map" + map);
                    String cbsresp = cbcas.getCustomerAccountSummary(ifr, map);
                    JSONParser jp = new JSONParser();
                    JSONObject cbsrespobj;
                    cbsrespobj = (JSONObject) jp.parse(cbsresp);
                    Log.consoleLog(ifr, "check for co-obligant acceptence Pension");
                    Aadhar = cbsrespobj.get("AadharNo").toString();
                    Log.consoleLog(ifr, "AadharNo value:" + Aadhar);
                    String productCode = pcm.mGetProductCode(ifr);
                    Log.consoleLog(ifr, "ProductCode:" + productCode);
                    String subProductCode = pcm.mGetSubProductCode(ifr);
                    Log.consoleLog(ifr, "ProductCode:" + productCode);
                    String cb1 = bpcc.mCallBureau(ifr, "CB", Aadhar, "CB");
                    if (cb1.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    }
                    String decisionCBCibil = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "CB");
                    Log.consoleLog(ifr, "decisionCBCibil::" + decisionCBCibil);
                    if (decisionCBCibil.contains(RLOS_Constants.ERROR)) {
                        return RLOS_Constants.ERROR;
                    } else if (decisionCBCibil.equalsIgnoreCase("Approve")) {
                        Log.consoleLog(ifr, "CIBIL Passed Successfully:::");
                        String Experrian = bpcc.mCallBureau(ifr, "EX", Aadhar, "CB");
                        if (Experrian.contains(RLOS_Constants.ERROR)) {
                            return pcm.returnError(ifr);
                        }
                        String decisionCBExperian = objbcr.checkCICScore(ifr, productCode, subProductCode, "EX", "CB");
                        Log.consoleLog(ifr, "decisionCBExperian::" + decisionCBExperian);
                        if (decisionCBExperian.contains(RLOS_Constants.ERROR)) {
                            return pcm.returnError(ifr);
                        } else if (decisionCBExperian.equalsIgnoreCase("Approve")) {
                            bpcc.populatecicScore(ifr, "CB");
                        }
                    }

                } else if (consentFlag.equalsIgnoreCase("N")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage",
                            cf.showMessage(ifr, "Btn_Refresh", "error", "Co borrower consent Rejected"));
                    Log.consoleLog(ifr, " CP CB rejected");
                    // message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
                    // "Thank you for choosing Canara Bank. You are not eligible for the selected
                    // digital loan journey, as per scheme guidelines of the Bank."));
                    message.put("eflag", "false");
//
//                    JSONArray gridResultSet = new JSONArray();
//                    String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
//                    org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
//                    Log.consoleLog(ifr, "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);
//
//                    String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
//                    Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);
//
//                    JSONObject formDetailsJson = new JSONObject();
//                    formDetailsJson.put("Name", coBorrowerName);
//                    formDetailsJson.put("Eligibility Status", "Not Eligible");
//
//                    gridResultSet.add(formDetailsJson);
//                    ifr.clearTable("CB_Eligibility_Grid");
//                    ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
//                    Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());
                    ifr.setStyle("Btn_Refresh", "disable", "true");
                    return message.toString();
                } else if (consentFlag.equalsIgnoreCase("I")) {
                    JSONObject message = new JSONObject();
                    Log.consoleLog(ifr, " CP CB Consent Not received");
                    message.put("showMessage",
                            cf.showMessage(ifr, "Btn_Refresh", "error", "Co borrower consent Not Received"));
                    message.put("eflag", "false");
//                    JSONArray gridResultSet = new JSONArray();
//                    String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
//                    org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
//                    Log.consoleLog(ifr, "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);
//
//                    String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
//                    Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);
//
//                    JSONObject formDetailsJson = new JSONObject();
//                    formDetailsJson.put("Name", coBorrowerName);
//                    formDetailsJson.put("Eligibility Status", "Consent Pending");
//
//                    gridResultSet.add(formDetailsJson);
//                    ifr.clearTable("CB_Eligibility_Grid");
//                    ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
//                    Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());

                    //ifr.addItemInCombo("DecisionValue", "Submit", "S");
                    return message.toString();
                }

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in pensionCoBoEligibility::" + e);
        }

        return "";
    }

    public static String fetchDataFromGrid(IFormReference ifr, String gridId) {
        JSONArray jsonArray = ifr.getDataFromGrid(gridId);
        return jsonArray.toString();
    }

    //added by keerthanaR for phase-2 pension development
    //To autopopulate the data
    public String autoPopulateLoanDetailsDataCP(IFormReference ifr, String control, String event, String value) {

        String currentStep = pcm.setGetPortalStepName(ifr, value);
        Log.consoleLog(ifr, "currentStep CP::::: ");
        try {
            Log.consoleLog(ifr, "inside try block::::autoPopulateLoanDetailsDataCP::::: ");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            String queryPeradd = ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "Pension PortalCustomCode:autoPopulateLoanDetailsDataCP-> CA:" + queryResadd);
            Log.consoleLog(ifr, "Pension PortalCustomCode:autoPopulateLoanDetailsDataCP-> PA:" + queryPeradd);
            List<List<String>> resultResadd = ifr.getDataFromDB(queryResadd);
            List<List<String>> resultPeradd = ifr.getDataFromDB(queryPeradd);
            if (resultResadd.size() > 0) {
                String comAddressLine1 = resultResadd.get(0).get(0);
                String comAddressLine2 = resultResadd.get(0).get(1);
                String comAddressLine3 = resultResadd.get(0).get(2);
                String EMAILID = resultResadd.get(0).get(3);
                String state = resultResadd.get(0).get(4);
                String country = resultResadd.get(0).get(5);
                String pincode = resultResadd.get(0).get(6);
                String strmobile = resultResadd.get(0).get(7);
                ifr.setValue("P_CP_LD_EMAILID", EMAILID);
                ifr.setValue("P_CP_LD_MOBILENO", strmobile);
                ifr.setValue("P_CP_LP_COMMUNICATION_ADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + "," + state + "," + country + "," + pincode);
            }
            if (resultPeradd.size() > 0) {
                String addressLine1 = resultPeradd.get(0).get(0);
                String addressLine2 = resultPeradd.get(0).get(1);
                String addressLine3 = resultPeradd.get(0).get(2);
                String pincode = resultPeradd.get(0).get(3);
                ifr.setValue("P_CP_LD_PERMANENT_ADDRESS", addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + pincode);
            }

            String FirstNameCB = "", MiddleNameCB = "", LastNameCB = "";
            //String FullNameCBQuery = "SELECT TITLE||'. '||regexp_replace(a.FIRSTNAME,'{}'),regexp_replace(a.MIDDLENAME,'{}'),regexp_replace(a.LASTNAME,'{}') FROM LOS_L_BASIC_INFO_I a INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY WHERE b.CUSTOMERFLAG = 'Y' AND b.PID ='" + ProcessInsanceId + "' and b.ApplicantType='B'";
            String FullNameCBQuery = ConfProperty.getQueryScript("getFullNameCBQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "mobileData query : " + FullNameCBQuery);
            List<List<String>> FullNameCBList = ifr.getDataFromDB(FullNameCBQuery);
            if (!FullNameCBList.isEmpty()) {
                FirstNameCB = FullNameCBList.get(0).get(0);
                MiddleNameCB = FullNameCBList.get(0).get(1);
                LastNameCB = FullNameCBList.get(0).get(2);
                if (!MiddleNameCB.isEmpty()) {
                    MiddleNameCB = MiddleNameCB.replace("null", "");
                }
                if (!LastNameCB.isEmpty()) {
                    LastNameCB = LastNameCB.replace("null", "");
                }
                String Fullname = FirstNameCB + " " + MiddleNameCB + " " + LastNameCB;
                ifr.setValue("P_PAPL_CUSTOMERNAME1", Fullname.replace("  ", " "));
            }
            String Product = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataCP:getProductCode::" + Product);
            String ProductName = "";
            String ProductNameQuery = "SELECT ProductName FROM LOS_M_Product WHERE ProductCode='" + Product + "'";
            List<List<String>> ProductNameList = ifr.getDataFromDB(ProductNameQuery);
            if (!ProductNameList.isEmpty()) {
                ProductName = ProductNameList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Product Name::autoPopulateLoanDetailsDataCP:::" + ProductName);
            ifr.setValue("P_CP_LD_Product", ProductName);
            ifr.setStyle("P_CP_LD_Product", "disable", "true");
            String subProduct = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataCP:getSubProductCode::" + subProduct);
            String subProductName = "";
            if (subProduct.equalsIgnoreCase("STP-CP")) {
                subProductName = "Canara Pension";
            }
            Log.consoleLog(ifr, "subProductName::autoPopulateLoanDetailsDataCP:::" + subProductName);
            ifr.setValue("P_CP_LD_SubProduct", subProductName);
            ifr.setStyle("P_CP_LD_SubProduct", "disable", "true");
            String roiID = pcm.mGetRoiID(ifr);
            Log.consoleLog(ifr, "roiID::autoPopulateLoanDetailsDataCP::" + roiID);
//            String ROI = roiID + "%";
//            ifr.setValue("P_CP_LD_Total_ROI", ROI);
//            ifr.setStyle("P_CP_LD_Total_ROI", "disable", "true");
            ifr.setValue("P_CP_LD_FRP", "0.75%");
            ifr.setStyle("P_CP_LD_FRP", "disable", "true");
            String RLLR = "";
            String RLLRQuery = "select final_rllr from LOS_MST_RLLR where base_type='RLLR'";
            List<List<String>> resultRLLRQuery = ifr.getDataFromDB(RLLRQuery);
            if (!resultRLLRQuery.isEmpty()) {
                RLLR = resultRLLRQuery.get(0).get(0);
            }
            Log.consoleLog(ifr, "RLLR::autoPopulateLoanDetailsDataCP:::" + RLLR);
            ifr.setValue("P_CP_LD_RLLR", RLLR);
            ifr.setStyle("P_CP_LD_RLLR", "disable", "true");

            ifr.setValue("P_CP_LD_Purpose", "");
            ifr.clearCombo("P_CP_LD_Purpose");
            String purposeType = ifr.getValue("P_CP_LD_Purpose").toString();
            String indchk = "";
            String grossmultiple = "";
            String mobileNo = "";
            String queryV = "";
            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobileNo = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobileNo);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobileNo);
            String indChkData_Query = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobileNo);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, indChkData_Query, "indChkData_Query:");
            if (list1.size() > 0) {
                indchk = list1.get(0).get(0);
            }
            if (indchk.equalsIgnoreCase("R4") || indchk.equalsIgnoreCase("Z4") || indchk.equalsIgnoreCase("Z8")) {
                queryV = "SELECT PURPOSENAME,PURPOSECODE FROM LOS_M_PURPOSE WHERE purposecode ='PERS' OR purposecode ='DOM' OR purposecode ='OTH' OR purposecode ='MED' OR purposecode ='IBA' AND isactive='Y'";
            } else {
                queryV = "SELECT PURPOSENAME,PURPOSECODE FROM LOS_M_PURPOSE WHERE purposecode ='PERS' OR purposecode ='DOM' OR purposecode ='OTH' OR purposecode ='MED'  AND isactive='Y'";
            }
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load pension purpose type");
            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                String value1 = list.get(i).get(1);
                Log.consoleLog(ifr, "label ::  " + label);
                ifr.addItemInCombo("P_CP_LD_Purpose", label, value1);
            }

            //saving data in knockoff-grid starts
            String mobileNumber = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "mobileNumber::autoPopulateLoanDetailsDataCB:::" + mobileNumber);
            String PARTY_TYPE = "";
            String RULE_NAME = "";
            String OUTPUT = "";
            String knockoffTemprules = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYPEN").replaceAll("#mobileNumber#", mobileNumber);
            Log.consoleLog(ifr, "Customersummary value  : " + knockoffTemprules);
            List<List<String>> dbdata = cf.mExecuteQuery(ifr, knockoffTemprules, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules:Pension:");
            Log.consoleLog(ifr, "dbdata value  : " + dbdata);
            if (!dbdata.isEmpty()) {
                PARTY_TYPE = dbdata.get(0).get(0);
                RULE_NAME = dbdata.get(0).get(1);
                OUTPUT = dbdata.get(0).get(2);
            }
            String knockoffRules = ConfProperty.getQueryScript("KNOCKOFFCOUNT").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List<List<String>> knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            Log.consoleLog(ifr, "knockoffRules value  : " + knockoffRulesdata);
            String Fkey = bpcc.Fkey(ifr, "B");
            if (knockoffRulesdata.isEmpty()) {
                String dataSavingGridQuery = ConfProperty.getQueryScript("KNOCKOFFINSERT").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#PARTY_TYPE#", PARTY_TYPE)
                        .replaceAll("#RULE_NAME#", RULE_NAME).replaceAll("#OUTPUT#", OUTPUT).replaceAll("#f_key#", Fkey).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextVal");
                Log.consoleLog(ifr, "dataSavingGridQuery::::" + dataSavingGridQuery);
                ifr.saveDataInDB(dataSavingGridQuery);
            }
            knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            String knockoffTemprules1 = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYPEN1").replaceAll("#mobileNumber#", mobileNumber);
            Log.consoleLog(ifr, "knockoffTemprules1 value  : " + knockoffTemprules1);
            List<List<String>> dataSaveknockoffGridData = cf.mExecuteQuery(ifr, knockoffTemprules1, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules:Pension:");
            Log.consoleLog(ifr, "dbdata value  : " + dataSaveknockoffGridData);
            if (!dataSaveknockoffGridData.isEmpty()) {
                String RULENAME[] = new String[dataSaveknockoffGridData.size()];
                String OUT_PUT[] = new String[dataSaveknockoffGridData.size()];
                String f_key = knockoffRulesdata.get(0).get(5);
                Log.consoleLog(ifr, "f_key: " + f_key);
                for (int i = 0; i < dataSaveknockoffGridData.size(); i++) {
                    Log.consoleLog(ifr, "Inside dataSaveknockoffGridData RULENAME::" + dataSaveknockoffGridData.get(i).get(0));
                    RULENAME[i] = dataSaveknockoffGridData.get(i).get(0);
                    Log.consoleLog(ifr, "Inside dataSaveknockoffGridData:: OUT_PUT" + dataSaveknockoffGridData.get(i).get(1));
                    OUT_PUT[i] = dataSaveknockoffGridData.get(i).get(1);
                    Log.consoleLog(ifr, "RULENAME:::OUT_PUT::" + RULENAME[i] + " " + OUT_PUT[i]);
                }
                String knockoffTable1 = ConfProperty.getQueryScript("KNOCKOFFCOUNT1").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
                Log.consoleLog(ifr, "knockoffTable1: " + knockoffTable1);
                List<List<String>> knockoffTableDATA1 = cf.mExecuteQuery(ifr, knockoffTable1, "knockoffTable1:");
                Log.consoleLog(ifr, "knockoffTableDATA1:::" + knockoffTableDATA1);
                if (knockoffTableDATA1.isEmpty()) {
                    for (int i = 0; i < RULENAME.length; i++) {
                        String dataSavingQuery1 = ConfProperty.getQueryScript("KNOCKOFFINSERT1").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#f_key#", f_key)
                                .replaceAll("#RULENAME#", RULENAME[i]).replaceAll("#OUT_PUT#", OUT_PUT[i]).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextVal");
                        Log.consoleLog(ifr, "dataSavingQuery1: " + dataSavingQuery1);
                        ifr.saveDataInDB(dataSavingQuery1);
                    }
                }
            }
            //saving data in knockoff-grid ends

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside autoPopulateLoanDetailsDataCP::::" + e);
            Log.errorLog(ifr, "Exception inside autoPopulateLoanDetailsDataCP::::" + e);
            return pcm.returnError(ifr);
        }
        return currentStep;
    }

    //added by keerthanaR for phase-2 pension development
    //next validation of loan details (calling CIBIL & Experian)
    //modified by keerthana for reqloanAmt minmax validation
    public String mImpOnClickLoanDetailsDataCP(IFormReference ifr, String control, String event, String value) {
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "inside try block::::mImpOnClickLoanDetailsDataCP::::: ");
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId::mImpOnClickLoanDetailsDataCP:::" + CustomerId);
            String MobileNo = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "MobileNo:::mImpOnClickLoanDetailsDataCP:::" + MobileNo);
            String response = cbcas.getCustomerAccountParams_CB(ifr, MobileNo);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB:::mImpOnClickLoanDetailsDataCP:::>" + response);
            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataCP::" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataCP::" + subProductCode);
            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
            String PANNUMBER = OutputJSON.get("PanNumber").toString();
            String DateofBirth = OutputJSON.get("DateofBirth").toString();
            Log.consoleLog(ifr, "AADHARNUMBER::mImpOnClickLoanDetailsDataCP::" + AADHARNUMBER);
            Log.consoleLog(ifr, "PANNUMBER::mImpOnClickLoanDetailsDataCP::" + PANNUMBER);
            Log.consoleLog(ifr, "DateofBirth::mImpOnClickLoanDetailsDataCP::" + DateofBirth);
            String ReqLoamAmount = ifr.getValue("P_CP_LD_Requested_Loan_Amount").toString();
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String schemeID = pcm.getBaseSchemeID(ifr, ProcessInsanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String MinLoanAmout = "";
            String minLoanData_Query = ConfProperty.getQueryScript("GetMinLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> MinLoanAmtList = cf.mExecuteQuery(ifr, minLoanData_Query, "MinAmt loan Data from LoanInfo Query : ");
            if (MinLoanAmtList.size() > 0) {
                MinLoanAmout = MinLoanAmtList.get(0).get(0);
            }
            if (Integer.parseInt(ReqLoamAmount) < Integer.parseInt(MinLoanAmout)) {
                Log.consoleLog(ifr, "ReqLoanAmtCheck:mImpOnClickLoanDetailsDataCP::MinLoanAmout::" + ReqLoamAmount);
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter loan Amount greater than or equal to " + MinLoanAmout + "."));
                message.put("eflag", "false");
                return message.toString();
            }
            Log.consoleLog(ifr, "MinLoanAmout : " + MinLoanAmout);
            String MaxLoanAmt = ConfProperty.getCommonPropertyValue("MaxLoanAmtPension");//1500000
            if (Integer.parseInt(ReqLoamAmount) > Integer.parseInt(MaxLoanAmt)) {
                Log.consoleLog(ifr, "ReqLoanAmtCheck:mImpOnClickLoanDetailsDataCP::MaxLoanAmt::" + ReqLoamAmount);
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter loan Amount less than or equal to " + MaxLoanAmt + "."));
                message.put("eflag", "false");
                return message.toString();
            }
            if (Integer.parseInt(ReqLoamAmount) <= 100000) {
                Log.consoleLog(ifr, "inside requested loan amount is less than 1 lakh:::::");
                String cb = bpcc.mCallBureauLP(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataCP::::" + decision);
                    Log.consoleLog(ifr, "checkCICEligibility:::CIBIL Passed Successfully::mImpOnClickLoanDetailsDataCP::::");
                    Log.consoleLog(ifr, "calling data saving from pension:::populateLoanDetailsCP:::");
                    populateLoanDetailsCP(ifr, control, event, value);
                    bpcc.populatecicScore(ifr, "B");
//                    decision = objbcr.checkCICEligibility(ifr, productCode, subProductCode);
//                    Log.consoleLog(ifr, "decision1/EX" + decision);
//                    if (decision.contains(RLOS_Constants.ERROR)) {
//                        return RLOS_Constants.ERROR;
//                    } else if (decision.equalsIgnoreCase("Approve")) {
//                        Log.consoleLog(ifr, "checkCICEligibility:::Cibil Failed:::mImpOnClickLoanDetailsDataCP::::");
//                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
//                        message.put("eflag", "false");
//                        return message.toString();
//                    }
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataCP::::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else if (Integer.parseInt(ReqLoamAmount) > 100000) {
                Log.consoleLog(ifr, "inside requested loan amount is greater than 1 lakh:::::");
                String cb = bpcc.mCallBureauLP(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataCP::::");
                    String EX = bpcc.mCallBureauLP(ifr, "EX", AADHARNUMBER, "B", ReqLoamAmount);
                    if (EX.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    }
                    decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "Ex", "B");

                    Log.consoleLog(ifr, "decision2/EX::" + decision);
                    if (decision.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    } else if (decision.equalsIgnoreCase("Approve")) {
                        Log.consoleLog(ifr, "EXPERIAN Passed Successfully::mImpOnClickLoanDetailsDataCP:::");
                        Log.consoleLog(ifr, "calling budget for data saving from pension:::populateLoanDetailsCP:::");
                        populateLoanDetailsCP(ifr, control, event, value);
                        bpcc.populatecicScore(ifr, "B");
                    } else {
                        Log.consoleLog(ifr, "Experian Failed:::mImpOnClickLoanDetailsDataCP:::");
                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                        message.put("eflag", "false");
                        return message.toString();
                    }
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataCP::::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else {
                Log.consoleLog(ifr, "inside requested loan amount is empty:::::");
                //message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Please enter Recommended Loan Amount"));
                //message.put("eflag", "false");
                return message.toString();
            }
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception inside mImpOnClickLoanDetailsDataCP::::" + e);
            Log.errorLog(ifr, "Exception inside mImpOnClickLoanDetailsDataCP::::" + e);
            return pcm.returnError(ifr);
        }
        return "";
    }
    //added by keerthanaR for phase-2 pension development
    //doing validation for onchange of requested loan amt

//    public String OnChangeReqLoanAmountCP(IFormReference ifr) {
//        JSONObject message = new JSONObject();
//        try {
//            String ReqLoanAntMin = "";
//            Log.consoleLog(ifr, "inside try block OnChangeReqLoanAmountCP:::: ");
//            String reqAmount = ifr.getValue("P_CP_LD_Requested_Loan_Amount").toString();
//            Log.consoleLog(ifr, "reqAmount:::: " + reqAmount);
//            ReqLoanAntMin = ConfProperty.getCommonPropertyValue("MINREQUESTEDLOANAMT");//50000
//            int minAmount = Integer.parseInt(ReqLoanAntMin);
//            Log.consoleLog(ifr, "minAmount from properties:::: " + minAmount);
//            if (Integer.parseInt(reqAmount) < minAmount) {
//                Log.consoleLog(ifr, "inside if block OnChangeReqLoanAmountCP:::: ");
//                message.put("showMessage", cf.showMessage(ifr, "P_CP_LD_Requested_Loan_Amount", "error", "Please Enter the Requested Loan Amount greater than " +ReqLoanAntMin+ "!"));
//                ifr.setValue("P_CP_LD_Requested_Loan_Amount", "");
//                return message.toString();
//            } else {
//                return "";
//            }
//        } catch (NumberFormatException e) {
//            Log.errorLog(ifr, "Error in OnChangeReqLoanAmountCP" + e);
//            return pcm.returnError(ifr);
//        }
//    }
    //added by keerthanaR for phase-2 pension development
    //method changes for data saving
    public String populateLoanDetailsCP(IFormReference ifr, String control, String event, String value) {

        try {
            Log.consoleLog(ifr, "inside try block populateLoanDetailsCP:::: ");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTLOANDETAILS").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "LoanDetails query::" + IndexQuery);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            Log.consoleLog(ifr, "dataResult query::" + dataResult);
            String datacount = dataResult.get(0).get(0);
            String subProductValue = "";
            String ProductValue = "";
            if (Integer.parseInt(datacount) == 0) {
                Log.consoleLog(ifr, "inside if LoanDetails query::");
                String portalFields = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCP");
                Log.consoleLog(ifr, "portalFields Loan Details::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, Arrays.toString(portalFieldStr) + "portalFieldStr[]:populateLoanDetailsCP:");
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1:populateLoanDetailsCP:" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                if (portalValue[1].equalsIgnoreCase("Canara Pension")) {
                    ProductValue = "PL";
                    subProductValue = "STP-CP";

                }
                Log.consoleLog(ifr, "portalFields array portalValue[i]:populateLoanDetailsCP:" + Arrays.toString(portalValue));
                String insertQuery = ConfProperty.getQueryScript("insertLoanDetailsDataCB")
                        .replaceAll("#ProcessInstanceId#", PID).replaceAll("#portalValue0#", ProductValue)
                        .replaceAll("#portalValue1#", subProductValue).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "insertQuery for Loan Details Info::" + insertQuery);
                ifr.saveDataInDB(insertQuery);
            } else {
                Log.consoleLog(ifr, "inside else LoanDetails query::");
                String portalFields = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCP");
                Log.consoleLog(ifr, "portalFields Loan Details::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, Arrays.toString(portalFieldStr) + "portalFieldStr[]:populateLoanDetailsCP:");
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1:populateLoanDetailsCP:" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                if (portalValue[1].equalsIgnoreCase("Canara Pension")) {
                    ProductValue = "PL";
                    subProductValue = "STP-CP";
                }
                String updateQuery = ConfProperty.getQueryScript("updateLoanDetailsDataCB")
                        .replaceAll("#ProcessInstanceId#", PID).replaceAll("#portalValue0#", ProductValue)
                        .replaceAll("#portalValue1#", subProductValue).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "update for Loan Details Info::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "populateLoanDetailsCP::" + count);
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "error in populateLoanDetailsCP" + e);
            Log.errorLog(ifr, "error in populateLoanDetailsCP" + e);
        }
        return "";
    }
    //added by keerthanaR for phase-2 pension development
    //method changes for co-obligant check

    public String CoObligantCBSCheckCP(IFormReference ifr, String control, String event, String ApplicantType) {
        JSONObject message = new JSONObject();
        Log.consoleLog(ifr, "Inside CoObligantCBSCheck");
        ValidateChkFlag = "Yes";
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String COMobNo = ifr.getValue("P_CP_OD_MOBILE_NUMBER").toString();
            String COCustID = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
            String Ext = ifr.getValue("P_CP_OD_EXISTING_CUSTOMER").toString();
            String RESHPWTBRWR = ifr.getValue("P_CP_OD_Relationship_with_Borrower").toString();
            Log.consoleLog(ifr, "Existing Customer Radio button ===> " + Ext);
            Log.consoleLog(ifr, "MOBILE NUMBER ===> " + Ext);
            Log.consoleLog(ifr, "CUSTOMER ID ===> " + Ext);
            if (Ext.equalsIgnoreCase("YES")) {
                Log.consoleLog(ifr, "Inside Existing Customer Radio button ===> " + Ext);
                if (!RESHPWTBRWR.isEmpty()) {
                    Log.consoleLog(ifr, "Inside Relationship with borrower field validation pension ===> " + RESHPWTBRWR);
                    if (!COMobNo.isEmpty() && !COCustID.isEmpty()) {
                        Log.consoleLog(ifr, "Inside MOBILE NUMBER & CUSTOMER_ID Validation ");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("MobileNumber", COMobNo);
                        map.put("CustomerId", COCustID);
                        Log.consoleLog(ifr, "PensionPortalCustomCode:CoObligantCBSCheck- MobileNumber:" + COMobNo);
                        Log.consoleLog(ifr, "PensionPortalCustomCode:CoObligantCBSCheck- CustomerId:" + COCustID);

                        String responseCO = cbcas.getCustomerAccountSummary(ifr, map);
                        String ErrorMessage = "";
                        if (responseCO.contains(RLOS_Constants.ERROR)) {

                            ErrorMessage = responseCO.replaceAll(RLOS_Constants.ERROR, "");
                            message.put("MSGSTS", "N");
                            message.put("SHOWMSG", ErrorMessage + ". Kindly enter valid data");
                            return message.toString();
                            //return pcm.returnErrorcustmessage(ifr, responseCO);
                        }
                        JSONParser jparser = new JSONParser();
                        JSONObject object = (JSONObject) jparser.parse(responseCO);
                        String ext_cust = object.get("CustomerFlag").toString();
                        String responseCOMobNumber = object.get("mobile_Number").toString();
                        String responseCOCustId = object.get("CustomerID").toString();
                        Log.consoleLog(ifr, " CustomerFlag =>" + ext_cust);

                        Log.consoleLog(ifr, "mobile_Number =>" + responseCOMobNumber);

                        Log.consoleLog(ifr, "CustomerID =>" + responseCOCustId);
                        String fName = object.get("CustomerFirstName").toString();
                        String mName = object.get("CustomerMiddleName").toString();
                        String lName = object.get("CustomerLastName").toString();
                        String fullName = "";
                        if ((mName.equalsIgnoreCase("")) || (mName.equalsIgnoreCase("null")) || (mName == null)) {
                            fullName = fName + " " + lName;
                        } else {
                            fullName = fName + " " + mName + " " + lName;
                        }
                        Log.consoleLog(ifr, "fullName by api::" + fullName);
                        if ((!ext_cust.equalsIgnoreCase("Y")) || ext_cust.isEmpty()) {
                            Log.consoleLog(ifr, "Co-obligant validation failed CBS");
                            ifr.setValue("P_CP_OD_MOBILE_NUMBER", "");
                            ifr.setValue("P_CP_OD_CUSTOMER_ID", "");
                            //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_OD_ValidateCoObligantCB", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance"));
                            //message.put("eflag", "false");//Hard Stop
                            message.put("MSGSTS", "Y");
                            message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance");
                            return message.toString();
                        } else {
                            String knockoffDecision = mImpPensionOnClickDocumentUploadKnockoff(ifr, ApplicantType);
                            if (knockoffDecision.toUpperCase().equalsIgnoreCase("Approve")) {
                                ifr.setValue("CoApplicantName_CP", fullName);
                                Log.consoleLog(ifr, "Into Co-obligant validation -- Before DataSaving ");
                                Log.consoleLog(ifr, "ApplicantType :: " + ApplicantType);
                                String result = new PortalCustomCode().saveDataInPartyDetailGrid(ifr, ApplicantType, COMobNo + "~" + COCustID);
                                Log.consoleLog(ifr, "Co-obligant validation is Successful");
                                ifr.setStyle("P_CoObligant_OD_Section", "visible", "true");
                                ifr.setStyle("P_OD_ValidateCoObligantCP", "disable", "true");
                                Log.consoleLog(ifr, "P_OD_ValidateCoObligantCP ====> Validate Button is disabled <==== ");

                                Demographic objDemographic = new Demographic();
                                String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, COCustID);
                                Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
                                if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                                    Log.consoleLog(ifr, "inside error condition Demographic Budget");
                                    return pcm.returnErrorAPIThroughExecute(ifr);
                                } else {
                                    Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
                                    JSONParser jsonparser = new JSONParser();
                                    JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                                    Log.consoleLog(ifr, obj.toString());
                                    String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
                                    Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
                                    LocalDate curDate = LocalDate.now();
                                    Log.consoleLog(ifr, "curDate  :" + curDate);
                                    LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
                                    Log.consoleLog(ifr, "PastDate  :" + PastDate);
                                    long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
                                    Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
                                    int YearsWithCanara = (int) (monthsBetween / 12);
                                    int MonthsWithCanara = (int) (monthsBetween % 12);
                                    Log.consoleLog(ifr, "YearsWithCanara: " + YearsWithCanara);
                                    Log.consoleLog(ifr, "MonthsWithCanara :" + MonthsWithCanara);

                                    ifr.setValue("P_CP_OD_RelationshipWithCanara_COB", String.valueOf(YearsWithCanara));
                                    ifr.setValue("P_CP_OD_RelationshipWithCanara_InMonths_COB", String.valueOf(MonthsWithCanara));
                                    ifr.setStyle("P_CP_OD_RelationshipWithCanara_COB", "disable", "true");
                                    ifr.setStyle("P_CP_OD_RelationshipWithCanara_InMonths_COB", "disable", "true");
                                    //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_OD_ValidateCoObligantCB", "error", "Co-Obligant is Existing to the Canara Bank. Please fill the below Co-Obligant Details"));
                                    //message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Co-Obligant is Existing to the Canara Bank. Please fill the below Co-Obligant Details"));
                                    message.put("MSGSTS", "N");
                                    message.put("SHOWMSG", "Co-Obligant is Existing to the Canara Bank. Kindly fill the below Co-Obligant Details");
                                    ifr.setStyle("P_CP_OD_MOBILE_NUMBER", "disable", "true");
                                    ifr.setStyle("P_CP_OD_CUSTOMER_ID", "disable", "true");
                                    ifr.setStyle("P_CP_OD_Relationship_with_Borrower", "disable", "true");
                                    ifr.setStyle("P_OD_ValidateCoObligantCB", "disable", "true");
                                    ifr.setStyle("P_CP_OD_RELATIONSHIP_BORROWER_OTHERS", "disable", "true");
                                    return message.toString();
                                }
                            } else {
                                Log.consoleLog(ifr, "Kindly fill the relationship with borrower validation for Co-Obligant ");
                                message.put("MSGSTS", "N");
                                message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank.");
                                return message.toString();
                            }
                        }
                    } else {
                        Log.consoleLog(ifr, "Kindly fill the Mobile Number & Customer ID to Validate Co-Obligant ");
                        message.put("MSGSTS", "N");
                        message.put("SHOWMSG", "Kindly fill the Mobile Number & Customer ID to validate Co-Obligant");
                        return message.toString();
                    }
                } else {
                    Log.consoleLog(ifr, "Kindly fill the relationship with borrower validation for Co-Obligant ");
                    message.put("MSGSTS", "N");
                    message.put("SHOWMSG", "Kindly fill the relationship with borrower field to validate Co-Obligant");
                    return message.toString();
                }
            } else {
                Log.consoleLog(ifr, "Kindly select existing customer 'Yes' to validate the Co-Obligant ");
                message.put("MSGSTS", "N");
                message.put("SHOWMSG", "Kindly select existing customer 'Yes' to validate the Co-Obligant");
                return message.toString();
            }
        } catch (Exception ex) {
            Log.errorLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
            Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
            return pcm.returnError(ifr);

        }
    }

    //added by keerthanaR for phase-2 pension development //modified by keerthana for eligibility on 16/06/2024
    //method for eligibility calculation data saving
// modified by keerthana for calculation on 26/06/2024
    public String getAmountForFinalEligibilityCPDataSaveBO(IFormReference ifr, HashMap<String, String> loandata) {
        String finaleligibilityRound = "";
        try {

            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + subProductCode);
            String KnockofCIC_Eligibility = objbcr.checkCICEligibility(ifr, productCode, subProductCode);
            JSONObject message = new JSONObject();
            if (KnockofCIC_Eligibility.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            } else if (KnockofCIC_Eligibility.equalsIgnoreCase("Approve")) {

                Log.consoleLog(ifr, "inside getAmountForFinalEligibilityCPDataSaveBO:::::");
                String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String indchk = "";
                String grossmultiple = "";
                String nthPercentage = "";
                String mobileNo = "";
                String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
                List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
                String loan_selected = loanSelected.get(0).get(0);
                Log.consoleLog(ifr, "loan type==>" + loan_selected);
                String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInsanceId);
                List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
                if (MobileDataList.size() > 0) {
                    mobileNo = MobileDataList.get(0).get(0);
                    Log.consoleLog(ifr, "MobileNo==>" + mobileNo);
                }
                Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobileNo);
                String indChkData_Query = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobileNo);
                List<List<String>> list1 = cf.mExecuteQuery(ifr, indChkData_Query, "indChkData_Query:");
                if (list1.size() > 0) {
                    indchk = list1.get(0).get(0);
                }
                if (indchk.equalsIgnoreCase("R4") || indchk.equalsIgnoreCase("Z4") || indchk.equalsIgnoreCase("Z8")) {
                    grossmultiple = ConfProperty.getCommonPropertyValue("GROSSMULTIPENSIONEX");//20
                    nthPercentage = ConfProperty.getCommonPropertyValue("NTHPERCENTAGE_EX");//25
                } else {
                    grossmultiple = ConfProperty.getCommonPropertyValue("GROSSMULTIPENSIONGEN");//24
                    nthPercentage = ConfProperty.getCommonPropertyValue("NTHPERCENTAGE_GEN");//40
                }
                Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR GROSSMULTIPENSION " + grossmultiple);
                Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR PERCENTAGEPEN " + nthPercentage);
                BigDecimal grossSalaryMultiple = new BigDecimal(grossmultiple);
                BigDecimal netsalary;
                String ReqAmount = loandata.get("reqAmount").toString();
                BigDecimal ftReqAmount = pcm.mCheckBigDecimalValue(ifr, loandata.get("reqAmount"));
                BigDecimal ftTenure = pcm.mCheckBigDecimalValue(ifr, loandata.get("tenure"));
                BigDecimal ftRoi = pcm.mCheckBigDecimalValue(ifr, loandata.get("roi"));
                BigDecimal deductionsalary = pcm.mCheckBigDecimalValue(ifr, loandata.get("deductionmonth"));
                BigDecimal grosssalaryip = pcm.mCheckBigDecimalValue(ifr, loandata.get("grosssalary"));
                //BigDecimal lacAmount = new BigDecimal(100000);
                int tenure1 = Integer.parseInt(ftTenure.toString());
                Log.consoleLog(ifr, "tenure conversion 123@@ ===> " + tenure1);
                BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, tenure1);
                Log.consoleLog(ifr, "emiperlc calculatePMT124" + emiperlc);
                Log.consoleLog(ifr, "basicInfo deductionmonth: " + deductionsalary);
                Log.consoleLog(ifr, "basicInfo grosssalary: " + grosssalaryip);
                String LOANAMOUNTPERPOLICY = "";
                String LOANAMOUNTPERPOLICY_Query = ConfProperty.getQueryScript("LOANAMOUNTPERPOLICYMUTIPLE");
                List<List<String>> LoanAmtMutiplelist = cf.mExecuteQuery(ifr, LOANAMOUNTPERPOLICY_Query,
                        "LOANAMOUNTPERPOLICY_Query:");
                if (!LoanAmtMutiplelist.isEmpty()) {
                    LOANAMOUNTPERPOLICY = LoanAmtMutiplelist.get(0).get(0);
                }
                BigDecimal LOANAMOUNTPERPOLICYMULTIPLE = new BigDecimal(LOANAMOUNTPERPOLICY);
                Log.consoleLog(ifr, "LOANAMOUNTPERPOLICYMULTIPLE ===> " + LOANAMOUNTPERPOLICYMULTIPLE);
                //Modified by Keerthana for Age and Eligibility Calculation on 01/07/2024
                BigDecimal cbCibilOblig = pcm.mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
                String lacAmount = ConfProperty.getCommonPropertyValue("LACAMOUNT");
                Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR LACAMOUNT " + lacAmount);
                BigDecimal laAmount = new BigDecimal(lacAmount);
                BigDecimal nthPercentageBD = new BigDecimal(nthPercentage);
                Log.consoleLog(ifr, "nthPercentageBD===> " + nthPercentageBD);
                BigDecimal nthPercentageBDDecimal = nthPercentageBD.divide(new BigDecimal(100));
                Log.consoleLog(ifr, "nthPercentageBDDecimal===> " + nthPercentageBDDecimal);
                BigDecimal GrossPercentage = grosssalaryip.multiply(nthPercentageBDDecimal);
                //BigDecimal lac = new BigDecimal(100000);
                //BigDecimal netTakeHomeNTH = grosssalaryip.(percentage, 2, RoundingMode.HALF_UP);
                Log.consoleLog(ifr, "netTakeHomeNTH===> " + GrossPercentage);
                //BigDecimal netIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig);
                //Log.consoleLog(ifr, "net_income===> " + netIncome);
                BigDecimal netAvailIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig).subtract(GrossPercentage);
                Log.consoleLog(ifr, "netAvailIncome===> " + netAvailIncome);
                BigDecimal twofivetimesgrosssal = grossSalaryMultiple.multiply(grosssalaryip);
                Log.consoleLog(ifr, "twofivetimesgrosssal===> " + twofivetimesgrosssal);

                BigDecimal loanAmountperpolicy = ((netAvailIncome).divide(emiperlc, 2, RoundingMode.HALF_UP)).multiply(laAmount);
                Log.consoleLog(ifr, "loanAmountperpolicy ===> " + loanAmountperpolicy);

                BigDecimal prodspeccapping = pcm.mCheckBigDecimalValue(ifr, loandata.get("loancap"));
                Log.consoleLog(ifr, "prodspeccapping===> " + prodspeccapping);
                BigDecimal finaleligibility = loanAmountperpolicy.min(twofivetimesgrosssal).min(prodspeccapping).min(ftReqAmount);

                Log.consoleLog(ifr, "finaleligibility===> " + finaleligibility);
                Log.consoleLog(ifr, "Before EligibilityDataObj : ");

                String grosssalaryipRound = String.valueOf(Math.round(Double.parseDouble(grosssalaryip.toString())));
                Log.consoleLog(ifr, "Before grosssalaryip : " + grosssalaryip);
                String deductionsalaryRound = String.valueOf(Math.round(Double.parseDouble(deductionsalary.toString())));
                Log.consoleLog(ifr, "Before deductionsalary : " + deductionsalary);
                String cbCibilObligRound = String.valueOf(Math.round(Double.parseDouble(cbCibilOblig.toString())));
                Log.consoleLog(ifr, "Before cbCibilOblig : " + cbCibilOblig);
                String netTakeHomeNTHRound = String.valueOf(Math.round(Double.parseDouble(GrossPercentage.toString())));
                Log.consoleLog(ifr, "Before netTakeHomeNTH : " + netTakeHomeNTHRound);
                String netIncomeRound = String.valueOf(Math.round(Double.parseDouble(netAvailIncome.toString())));
                Log.consoleLog(ifr, "Before netIncome : " + netIncomeRound);
                String ftTenureRound = String.valueOf(Math.round(Double.parseDouble(ftTenure.toString())));
                Log.consoleLog(ifr, "Before ftTenure : " + ftTenureRound);
                String ftRoiRound = ftRoi.toString();
                Log.consoleLog(ifr, "Before ftRoi : " + ftRoiRound);
                String loanAmountRound = String.valueOf(Math.round(Double.parseDouble(loanAmountperpolicy.toString())));
                Log.consoleLog(ifr, "Before loanAmount : " + loanAmountRound);
                //Modified by Keerthana for Age and Eligibility Calculation on 01/07/2024
                String twofivetimesgrosssalRound = String.valueOf(Math.round(Double.parseDouble(twofivetimesgrosssal.toString())));
                Log.consoleLog(ifr, "Before twofivetimesgrosssal : " + twofivetimesgrosssalRound);
                String prodspeccappingRound = String.valueOf(Math.round(Double.parseDouble(prodspeccapping.toString())));
                Log.consoleLog(ifr, "Before prodspeccapping : " + prodspeccappingRound);
                double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibility.toString()) / 1000) * 1000;
                finaleligibilityRound = String.valueOf(Math.round(finaleligibilityDouble));

                Log.consoleLog(ifr, "After rounded finaleligibility Value : " + finaleligibilityRound);
                double roundOffFinal = Double.parseDouble(finaleligibilityRound);
                Log.consoleLog(ifr, "After roundOffFinal Value : " + roundOffFinal);

                //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
                int finalMinusCheck = (int) roundOffFinal;
                Log.consoleLog(ifr, "After finalMinusCheck Value : " + finalMinusCheck);

                String EligibleCheckResult = checkEligibilityWhetherValueIsEligible(ifr, finalMinusCheck);
                Log.consoleLog(ifr, "After EligibleCheckResult Value : " + EligibleCheckResult);
                if (EligibleCheckResult.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                } else if (EligibleCheckResult.contains("showMessage")) {
                    return EligibleCheckResult;
                }
                //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
                if (finaleligibilityRound.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }
                String finalEligibleAmount = "";
                //String productCode = pcm.getProductCode(ifr);
                String finalAmountInParams = finaleligibilityRound + "," + finaleligibilityRound + "," + "CP";
                //String finalAmountInParams = "CP" + "," + finaleligibilityRound;
                finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_PENSION", finalAmountInParams, "final_eligibility");
                //Log.consoleLog(ifr, "value of finalEligibleAmount@@@:::"+finalEligibleAmount);
                if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {
                    Log.consoleLog(ifr, "eligibility Passed Successfully:::");
                } else {
                    Log.consoleLog(ifr, " eligibility fail" + finalEligibleAmount);
                    message.put("showMessage", cf.showMessage(ifr, "F_InPrincipleEligibility", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
                    message.put("eflag", "false");
                    return message.toString();
                }
                String loanAmount = "";
                BigDecimal rate = new BigDecimal(ftRoiRound);
                int tenure = Integer.parseInt(ftTenureRound);
                Log.consoleLog(ifr, " SliderFlag emi installment@@@ : " + SliderFlag);
                String emi = "";
                if (SliderFlag.equalsIgnoreCase("Yes")) {
                    loanAmount = "-" + ReqAmount;
                    BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
                    emi = emicalc.toString();
                    Log.consoleLog(ifr, " emi installment@@@ : " + emi);
                    ifr.setValue("P_CP_PA_EMI_AMT", ("₹ " + emi));
                } else if (SliderFlag.equalsIgnoreCase("") || !SliderFlag.equalsIgnoreCase("Yes")) {
                    loanAmount = "-" + finaleligibilityRound;
                    BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
                    emi = emicalc.toString();
                    Log.consoleLog(ifr, " emi installment@@@ : " + emi);
                }
                //added by keerthana for pension change on 19/07/2024

                String query11 = "select * from los_nl_la_finaleligibility where pid = '" + ProcessInsanceId + "'";
                List<List<String>> result = cf.mExecuteQuery(ifr, query11, "Query for checking los_nl_la_finaleligibility datas available or not");
                if (result.size() > 0) {
                    String query12 = "delete from los_nl_la_finaleligibility where pid = '" + ProcessInsanceId + "'";
                    result = cf.mExecuteQuery(ifr, query12, "Query for deleting the los_nl_la_finaleligibility datas available inside table");
                }
                //added by keerthana for pension change on 19/07/2024
                if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                    Log.consoleLog(ifr, "ActivityName ::" + ifr.getActivityName());
                    JSONArray gridResultSetArr = new JSONArray();
                    JSONObject formDetailsJson = new JSONObject();
//                JSONArray gridResultSetArr = new JSONArray();
//                JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_AverageGrossIncome", grosssalaryipRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_AverageDeductions", deductionsalaryRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_Obligations", cbCibilObligRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_NetIncome", netIncomeRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_Tenure", ftTenureRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_ROI", ftRoiRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_Loanamountasperpolicy", loanAmountRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanamountasperIncome", twofivetimesgrosssalRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanAmountasperProductPolicy", prodspeccappingRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_RequestedLoanAmount", ReqAmount);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_InPrincipalloanamount", finaleligibilityRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE_InstalmentAmount", emi);
                    gridResultSetArr.add(formDetailsJson);
                    ifr.clearTable("ALV_INPRINCIPLE_ELIGIBILITY");
                    ifr.addDataToGrid("ALV_INPRINCIPLE_ELIGIBILITY", gridResultSetArr, true);
                    Log.consoleLog(ifr, "ALV_INPRINCIPLE_ELIGIBILITY Populated Portal to Backoffice ==>" + gridResultSetArr.toString());
                } else if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                    result = cf.mExecuteQuery(ifr, query, "Query for DecisionCreditAppraisalQuery");
                    query11 = "select * from los_nl_la_inprinciple where pid = '" + ProcessInsanceId + "'";
                    result = cf.mExecuteQuery(ifr, query11, "Query for checking los_nl_la_inprinciple datas available or not");
                    if (result.size() > 0) {
                        String query12 = "delete from los_nl_la_inprinciple where pid = '" + ProcessInsanceId + "'";
                        result = cf.mExecuteQuery(ifr, query12, "Query for deleting the los_nl_la_inprinciple datas available inside table");
                    }
                    Log.consoleLog(ifr, "ActivityName ::" + ifr.getActivityName());
                    JSONArray gridResultSetArr = new JSONArray();
                    JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-AverageGrossIncome", grosssalaryipRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-AverageDeductions", deductionsalaryRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-Obligations", cbCibilObligRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-NetIncome", netIncomeRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-Tenure", ftTenureRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-ROI", ftRoiRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-Loanamountasperpolicy", loanAmountRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanamountasperIncome", twofivetimesgrosssalRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanAmountasperProductPolicy", prodspeccappingRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-RequestedLoanAmount", ReqAmount);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-InPrincipalloanamount", finaleligibilityRound);
                    formDetailsJson.put("QNL_LA_INPRINCIPLE-InstalmentAmount", emi);
                    gridResultSetArr.add(formDetailsJson);
                    ((IFormAPIHandler) ifr).clearTable("QNL_LA_INPRINCIPLE");
                    ((IFormAPIHandler) ifr).addDataToGrid("QNL_LA_INPRINCIPLE", gridResultSetArr, true);
                    Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE Populated Portal to Backoffice ==>" + gridResultSetArr.toString());
                } else {
                    Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE Populated Portal to Backoffice ==>datasaving not happened1");
                }
                if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                    JSONArray gridResultSetArr = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryipRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalaryRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Obligations", cbCibilObligRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_NetIncome", netIncomeRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Tenure", ftTenureRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_ROI", ftRoiRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", loanAmountRound);
                    //obj.put("QNL_LA_FINALELIGIBILITY-Multipliergrossincomepolicy", grossmultiple);
                    obj.put("QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount", prodspeccappingRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finaleligibilityRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", ReqAmount);
                    gridResultSetArr.add(obj);
                    ifr.clearTable("QNL_LA_FINALELIGIBILITY");
                    ifr.addDataToGrid("QNL_LA_FINALELIGIBILITY", gridResultSetArr);
                    Log.consoleLog(ifr, "QNL_LA_FINALELIGIBILITY gridResultSet Populated : " + gridResultSetArr.toString());

                } else if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                    JSONArray gridResultSetArr = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryipRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalaryRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Obligations", cbCibilObligRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_NetIncome", netIncomeRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Tenure", ftTenureRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_ROI", ftRoiRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", loanAmountRound);
                    //obj.put("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", grossmultiple);
                    obj.put("QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount", prodspeccappingRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finaleligibilityRound);
                    obj.put("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", ReqAmount);
                    gridResultSetArr.add(obj);
                    ifr.clearTable("ALV_FINAL_ELIGIBILITY");
                    ifr.addDataToGrid("ALV_FINAL_ELIGIBILITY", gridResultSetArr, true);
                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + gridResultSetArr);
                    return finaleligibilityRound;
                } else {
                    Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE Populated Portal to Backoffice ==>datasaving not happened2");
                }
            } else {
                Log.consoleLog(ifr, "Knockoffeligibility Failed:::");
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                message.put("eflag", "false");
                return message.toString();
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        }
        return finaleligibilityRound;
    }
    //added by keerthanaR for phase-2 pension development
    //method for agecalculation
    // modified by keerthana for calculation on 26/06/2024

    public static int calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if ((birthDate != null) && (currentDate != null)) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;
        }
    }

    public void mSavingLoanDetailsDataCP(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "inside try block mSavingLoanDetailsDataCP:::: ");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String PortalFieldLoanDetails = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCP");
            String portalfieldLoanDetailsArr[] = PortalFieldLoanDetails.split(",");

            String LoanDetailsFieldsQuery = ConfProperty.getQueryScript("PORTALLOANDETAILSCB").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "LoanDetailsFieldsQuery.." + LoanDetailsFieldsQuery);
            List<List<String>> LoanDetailsQueryresult = ifr.getDataFromDB(LoanDetailsFieldsQuery);
            Log.consoleLog(ifr, "LoanDetailsQueryresult.." + LoanDetailsQueryresult);
            if (LoanDetailsQueryresult.size() > 0) {
                for (int i = 0; i < portalfieldLoanDetailsArr.length; i++) {
                    Log.consoleLog(ifr, "Inside if mSavingLoanDetailsDataCP portalfieldCoBorrowerArr[i]==>" + i + " " + portalfieldLoanDetailsArr[i]);
                    if (LoanDetailsQueryresult.get(0).get(i).equalsIgnoreCase("STP-CP")) {
                        ifr.setValue(portalfieldLoanDetailsArr[i], "Canara Pension");
                    } else {
                        ifr.setValue(portalfieldLoanDetailsArr[i], LoanDetailsQueryresult.get(0).get(i));
                    }

                }
            }
            String ROIType = ifr.getValue("P_CP_LD_ROI_Type").toString();
            if (ROIType.equalsIgnoreCase("Fixed")) {
                ifr.setStyle("P_CP_LD_FRP", "visible", "true");
                ifr.setStyle("P_CP_LD_FRP", "mandatory", "true");
            } else {
                ifr.setStyle("P_CP_LD_FRP", "visible", "false");
            }
            String loanPurpose = ifr.getValue("P_CP_LD_Purpose").toString();
            if (loanPurpose.equalsIgnoreCase("OTH")) {
                ifr.setStyle("P_CP_LD_Purpose_Others", "visible", "true");
            } else {
                ifr.setStyle("P_CP_LD_Purpose_Others", "visible", "false");
            }
            // added by logaraj 15-07-2024
            String Product = ifr.getValue("P_CP_LD_Product").toString();
            if (Product.equalsIgnoreCase("PL")) {
                ifr.setValue("P_CP_LD_Product", "Personal Loan");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "error in mSavingLoanDetailsDataCP" + e);
            Log.errorLog(ifr, "error in mSavingLoanDetailsDataCP" + e);
        }
    }

    public String pensionDocumentValidation(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside pensionDocumentValidation::");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
            int gridSize = ifr.getDataFromGrid("CP_UPLOAD_DOCUMENT").size();
            Log.consoleLog(ifr, "gridSize : " + gridSize);
            HashMap<String, String> docMap = new HashMap<>();
            if (gridSize > 0) {
                String documentName[] = new String[gridSize];
                String uploadedDate[] = new String[gridSize];
                for (int i = 0; i < gridSize; i++) {
                    documentName[i] = ifr.getTableCellValue("CP_UPLOAD_DOCUMENT", i, 2);
                    uploadedDate[i] = ifr.getTableCellValue("CP_UPLOAD_DOCUMENT", i, 4);
                    Log.consoleLog(ifr, "documentName : " + documentName[i] + "uploadedDate : " + uploadedDate[i]);
                    docMap.put(documentName[i], uploadedDate[i]);
                }
                String docQuery = ConfProperty.getQueryScript("PORTALDOUMENTQUERYNEXT");
                List<List<String>> docQueryResult = cf.mExecuteQuery(ifr, docQuery, "Query for docQuery::");
                int chkDocMandotoryCount = 0;
                int documentName_Length = documentName.length;
                int docQueryResult_size = uploadedDate.length;
                Log.consoleLog(ifr, "pensionDocumentValidation-> documentName size from Form : " + documentName_Length);
                Log.consoleLog(ifr, "pensionDocumentValidation-> docQueryResult size from DB : " + docQueryResult_size);
                Log.consoleLog(ifr, "docQueryResult.size() : " + docQueryResult.size());

                for (int i = 0; i < docQueryResult.size(); i++) {
                    String strDocName = docQueryResult.get(i).get(0);
                    Log.consoleLog(ifr, "pensionDocumentValidation->strDocName::" + strDocName);
                    if (docMap.get(strDocName).equalsIgnoreCase("")) {
                        Log.consoleLog(ifr, "pensionDocumentValidation-> Mandatory doc not uploaded is " + strDocName);
                        chkDocMandotoryCount++;
                    }
                }
                Log.consoleLog(ifr, "pensionDocumentValidation->chkDocMandotoryCount::" + chkDocMandotoryCount);
                if (chkDocMandotoryCount > 0) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please upload mandatory document!!"));
                    message.put("eflag", "false");// Hard Stop
                    return message.toString();
                } else {

                    String ApplicationNo = "";
                    Log.consoleLog(ifr, "pensionPortalCustomcode:validateDocumentUpload:::ApplicationNo::::" + ApplicationNo);
                    String query1 = ConfProperty.getQueryScript("PORTALAPPLICATIONNOQUERY").replaceAll("#WINAME#", PID);
                    //select application_no from LOS_WIREFERENCE_TABLE  where winame='#WINAME#'
                    List< List< String>> ApplicationNoList = ifr.getDataFromDB(query1);
                    if (!ApplicationNoList.isEmpty()) {
                        ApplicationNo = ApplicationNoList.get(0).get(0);

                    }
                    Log.consoleLog(ifr, "pensionPortalCustomcode:validateDocumentUpload:::ApplicationNo::::" + ApplicationNo);
                    Log.consoleLog(ifr, "Inside update count Wf done ::");
                    String Updatestepname = ConfProperty.getQueryScript("Updatestepname").replaceAll("#WINAME#", PID);
                    String query = ConfProperty.getQueryScript("PORTALUPDATECOUNTWFDONE").replaceAll("#WINAME#", PID);
                    Log.consoleLog(ifr, "query : -->" + query);
                    ifr.saveDataInDB(query);
                    cf.mExecuteQuery(ifr, Updatestepname, "Query for Updatestepnamequery:PensioncustomCode:validateDocumentUpload:");

                    return "Yes" + "~" + ApplicationNo;

                }

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in pensionDocumentValidation::" + e);
        }
        return "";
    }

    public String savePansionOccuapationBorrower(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside savePansionOccuapationBorrower");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO1").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "OcuppationInfoDetails query::" + IndexQuery);
            Log.consoleLog(ifr, "OcuppationInfoDetails1 query::" + IndexQuery1);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);

            if (dataResult.size() == 0) {
                String f_key = dataResult1.get(0).get(0);
                String portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFields");
                Log.consoleLog(ifr, "portalFields::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, "portalFieldStr[]::" + portalFieldStr);
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1::" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                Log.consoleLog(ifr, "portalFields array portalValue[i]::" + portalValue);
                String insertQuery = ConfProperty.getQueryScript("InsertPensionOccupationDetails")
                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
                        .replaceAll("#portalValue17#", portalValue[17]);
                Log.consoleLog(ifr, "insertQuery for Occupation Info::" + insertQuery);
                ifr.saveDataInDB(insertQuery);
            } else {
                String f_key = dataResult1.get(0).get(0);
                String portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFields");
                Log.consoleLog(ifr, "portalFields : " + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                int size1 = portalFieldStr.length;
                String portalValue[] = new String[size1];
                for (int i = 0; i < portalFieldStr.length; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                Log.consoleLog(ifr, "portalValue : " + portalValue);
                String updateQuery = ConfProperty.getQueryScript("UpdatePensionOccupationDetails")
                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
                        .replaceAll("#portalValue17#", portalValue[17]);

                Log.consoleLog(ifr, "update for Occupation Info::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "updateOccuapationDetails::" + count);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "error in savePansionOccuapationBorrower" + e);
        }
        return "";
    }

    public String getOccupationBorrowerDataPension(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "setValue getOccupationBorrowerDataPension :");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String PortalField = ConfProperty.getCommonPropertyValue("PensionBorrowerFields");
            Log.consoleLog(ifr, "PortalField :" + PortalField);
            String portalfield[] = PortalField.split(",");
            String query = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
            List<List<String>> result = ifr.getDataFromDB(query);
            String F_Key = "";
            if (result.size() > 0) {
                F_Key = result.get(0).get(0);
            }
            Log.consoleLog(ifr, "FKey.." + F_Key);
            String query1 = ConfProperty.getQueryScript("PensionPopulatedataBorrower").replaceAll("#f_key#", F_Key);
            List<List<String>> result1 = ifr.getDataFromDB(query1);
            Log.consoleLog(ifr, "query1.." + query1);
            Log.consoleLog(ifr, "portalfield.length.." + portalfield.length);
            if (result1.size() > 0) {
                for (int i = 0; i < portalfield.length; i++) {

                    ifr.setValue(portalfield[i], result1.get(0).get(i).toString());

                }
            }
            String OccupationSubtype = ConfProperty.getCommonPropertyValue("OCCUPATIONTYPESUBTYPE");
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR OCCCUPATION " + OccupationSubtype);
            ifr.addItemInCombo("P_CP_OCCUPATION", OccupationSubtype); //Added by monesh on 11/07/2024
            ifr.setValue("P_CP_OCCUPATION", OccupationSubtype);
            ifr.setStyle("P_CP_OCCUPATION", "disable", "true");
            //Natureofsecurity value on load
            String natureOfSecurity = ConfProperty.getCommonPropertyValue("NATUREOFSECURITY");
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR NATUREOFSECURITY " + natureOfSecurity);
            ifr.addItemInCombo("P_CP_OD_NatureOfSecurity", natureOfSecurity); //Added by monesh on 11/07/2024
            ifr.setValue("P_CP_OD_NatureOfSecurity", natureOfSecurity);
            ifr.setStyle("P_CP_OD_NatureOfSecurity", "disable", "true");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception getOccupationBorrowerDataPension : " + e);
        }
        return "";
    }

    public String savePansionOccuapationCoBorrower(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "savePansionOccuapationCoBorrower : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);
            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "OccuppationInfoDetails query Co-Obligant::" + IndexQuery);
            Log.consoleLog(ifr, "OccuppationInfoDetails1 query Co-Obligant::" + IndexQuery1);
            List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query Co-Obligant ");
            List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
            Log.consoleLog(ifr, "dataResult::" + dataResult);
            Log.consoleLog(ifr, "dataResult1::" + dataResult1);
            String f_key = "";
            if (!dataResult1.isEmpty()) {
                f_key = dataResult1.get(0).get(0);
            }
            String cobliName = ifr.getValue("CoApplicantName_CP").toString();
            String Query0 = ConfProperty.getQueryScript("UPDATEKNOCKFKEYQUERY").replaceAll("#cobliName#", cobliName).replaceAll("#fkey#", f_key).replaceAll("#ProcessInstanceId#", PID).replaceAll("#InsertionOrderID#", "S_LOS_KNOCKOFFRULES_PARAMETERS.nextval");
            Log.consoleLog(ifr, "Query0 :" + Query0);
            ifr.saveDataInDB(Query0);
            String Query1 = ConfProperty.getQueryScript("UPDATEKNOCKCHILDFKEYQUERY").replaceAll("#fkey#", f_key).replaceAll("#ProcessInstanceId#", PID).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextval");
            Log.consoleLog(ifr, "Query1 :" + Query1);
            ifr.saveDataInDB(Query1);
            String portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFieldsCoborrower");
            if (dataResult.size() == 0) {
                Log.consoleLog(ifr, " INSIDE IF f_key::" + f_key);
                Log.consoleLog(ifr, "portalFields CoBorrower::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, "portalFieldStr[] CoBorrower::" + portalFieldStr);
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields CoBorrower size1::" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                Log.consoleLog(ifr, "portalFields array portalValue[i]::CoBorrower ::" + portalValue);
                String insertQuery = ConfProperty.getQueryScript("InsertOccupationCoborrowerPension")
                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
                        .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
                        .replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
                        .replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22])
                        .replaceAll("#portalValue23#", portalValue[23]).replaceAll("#portalValue24#", portalValue[24])
                        .replaceAll("#portalValue25#", portalValue[25]);
                Log.consoleLog(ifr, "insertQuery for Occupation Info::CoBorrower ::" + insertQuery);
                ifr.saveDataInDB(insertQuery);

            } else {
                Log.consoleLog(ifr, " INSIDE IF dataResult1::" + dataResult.size());
                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower f_key::" + f_key);
                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::size1" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < portalFieldStr.length; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                String updateQuery = ConfProperty.getQueryScript("UpdateOccupationCoborrowerPension")
                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
                        .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
                        .replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
                        .replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22])
                        .replaceAll("#portalValue23#", portalValue[23]).replaceAll("#portalValue24#", portalValue[24])
                        .replaceAll("#portalValue25#", portalValue[25]);
                Log.consoleLog(ifr, "update for Occupation Info::CoBorrower::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "updateOccuapationDetails::CoBorrower::count" + count);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "error in savePansionOccuapationCoBorrower " + e);
            Log.errorLog(ifr, "error in savePansionOccuapationCoBorrower " + e);
        }
        return "";
    }

    public String getOccupationCoBorrowerDataPension(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "setValue getOccupationCoBorrowerDataPension :");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            //String IndexQuery = "select a.CUSTOMERID,b.MobileNo from LOS_NL_BASIC_INFO a inner join LOS_L_BASIC_INFO_I b on a.f_key=b.f_key where a.PID='" + PID + "' and a.applicanttype='CB'";

            String PortalField = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFieldsCoborrower");
            Log.consoleLog(ifr, "PortalField :" + PortalField);
            String portalfield[] = PortalField.split(",");
            String query = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);
            List<List<String>> result = ifr.getDataFromDB(query);
            String F_Key = "";
            if (result.size() > 0) {
                ifr.setStyle("P_CoObligant_OD_Section", "visible", "true");
                ifr.setStyle("P_OD_ValidateCoObligantCB", "disable", "true");
                F_Key = result.get(0).get(0);
            }
            Log.consoleLog(ifr, "FKey.." + F_Key);
            String query1 = ConfProperty.getQueryScript("SelectCoBorrowerPension").replaceAll("#f_key#", F_Key);
            List<List<String>> result1 = ifr.getDataFromDB(query1);
            Log.consoleLog(ifr, "query1.." + query1);
            Log.consoleLog(ifr, "portalfield.length.." + portalfield.length);
            if (result1.size() > 0) {
                for (int i = 0; i < portalfield.length; i++) {

                    ifr.setValue(portalfield[i], result1.get(0).get(i).toString());

                }
            }
            //modified by logaraj on 16-07-2024 for pension
            String IndexQuery = ConfProperty.getQueryScript("getMobilewithCustIdQuery").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateCoborrowerData query::" + IndexQuery);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            Log.consoleLog(ifr, "populateCoborrowerData query::" + dataResult);

            if (dataResult.size() == 1) {
                ifr.setValue("P_CP_OD_CUSTOMER_ID", dataResult.get(0).get(0));
                ifr.setStyle("P_CP_OD_CUSTOMER_ID", "disable", "true");
                String MobileNum = dataResult.get(0).get(1);
                String trimMobileNum = MobileNum.substring(2);
                ifr.setValue("P_CP_OD_MOBILE_NUMBER", trimMobileNum);
                ifr.setStyle("P_CP_OD_MOBILE_NUMBER", "disable", "true");
                ifr.setStyle("P_CP_OD_Relationship_with_Borrower", "disable", "true");
                Log.consoleLog(ifr, "populateCoborrowerData query::" + MobileNum);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception getOccupationCoBorrowerDataPension : " + e);
        }
        return "";
    }
    //added by keerthana for calculate month difference on 20/06/2024 
//	public static int differenceInMonths(IFormReference ifr,String oldDate) {
//        Log.consoleLog(ifr, "calculateDifferenceInMonths:Pension:oldDate" + oldDate);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        LocalDate currentDate = LocalDate.now();
//        LocalDate date = LocalDate.parse(oldDate, formatter);
//        Period period = Period.between(date.withDayOfMonth(1), currentDate.withDayOfMonth(1));
//        return period.getYears() * 12 + period.getMonths();
//    }
//	
    //added by keerthana for FRP Field Visibility on 20/06/2024 starts

    public void onChangeROITypeCP(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, " Inside::PensionPortalCustomCode::onChangeROITypeCP" + ifr.getValue("P_CP_LD_ROI_Type").toString());
        String ROIType = ifr.getValue("P_CP_LD_ROI_Type").toString();
        if (ROIType.equalsIgnoreCase("Fixed")) {
            ifr.setStyle("P_CP_LD_FRP", "visible", "true");
            ifr.setStyle("P_CP_LD_FRP", "mandatory", "true");
        } else if (ROIType.equalsIgnoreCase("Floating")) {
            ifr.setStyle("P_CP_LD_FRP", "visible", "false");
        } else {
            Log.consoleLog(ifr, " Inside::PensionPortalCustomCode::onChangeROITypeCP::no values coming");
        }
        String schemeId = pcm.getPensionSchemeId(ifr, PID);
        Log.consoleLog(ifr, "schemeId==>" + schemeId);
        String roiData = ConfProperty.getQueryScript("PENSIONTOTALROI").replaceAll("#schemeID#", schemeId).replaceAll("#ROIType#", ROIType);
        Log.consoleLog(ifr, "pension roiData query : " + roiData);
        List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
        String roi = "";
        if (!loanROI.isEmpty()) {
            roi = loanROI.get(0).get(0);
        }
        String ROI = roi + "%";
        ifr.setValue("P_CP_LD_Total_ROI", ROI);
        ifr.setStyle("P_CP_LD_Total_ROI", "disable", "true");
    }

    public static String calculateDifferenceInYearsAndMonths(IFormReference ifr, String oldDate) {
        Log.consoleLog(ifr, "calculateDifferenceInYearsAndMonths:Pension:oldDate " + oldDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate date;
        try {
            date = LocalDate.parse(oldDate, formatter);
        } catch (DateTimeParseException e) {
            Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
            return "Error: Invalid date format"; // Return a meaningful error message
        }

        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date, currentDate);

        int differenceInYears = period.getYears();
        int differenceInMonths = period.getMonths();

        return differenceInYears + "," + differenceInMonths;
    }

    public void controlSetForCoBorrowerOccupationDetailsPage(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside controlSetForCoBorrowerOccupationDetailsPage method ::");
        String OccupationType = ifr.getValue("P_CP_OD_Profile_COB").toString();
        Log.consoleLog(ifr, "Inside controlSetForCoBorrowerOccupationDetailsPage method11 ::" + OccupationType);
        if (OccupationType.equalsIgnoreCase("Salaried")) {
            ifr.applyGroup("S_CP_Co_B_OccupationDetails_Salaried");
        } else if (OccupationType.equalsIgnoreCase("PEN")) {
            ifr.applyGroup("S_CP_Co_B_OccupationDetails_Retired");
        } else if (OccupationType.equalsIgnoreCase("NIE")) {
            ifr.applyGroup("S_CP_Co_B_OccupationDetails_NonIncomeEar");
        } else if (OccupationType.equalsIgnoreCase("PROF") || OccupationType.equalsIgnoreCase("SELF")) {
            ifr.applyGroup("S_CP_Co_B_OccupationDetails_SelfEmployed");
        }
    }

    //Added by logaraj on 26/06/2024 for finaleligibility onchange
    public String mCalculateUserIncomePolicy(IFormReference ifr) {
        String nth = ifr.getValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser").toString();
        String Multiplier = ifr.getValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser").toString();
        String AvgGrossIncome = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
        return mCalculateUserMultiIncomePolicy(ifr, nth, "QNL_LA_FINALELIGIBILITY-NetTakeHomeasperuser", "QNL_LA_FINALELIGIBILITY-Mutiplierenteredbyuser", "QNL_LA_FINALELIGIBILITY-Loanamountgrossincomepolicy");

    }

    //Added by logaraj on 26/06/2024 for finaleligibility onchange
    public String mCalculateUserMultiIncomePolicy(IFormReference ifr, String nth, String percentage, String multiplier, String income) {
        BigDecimal grossSalary = new BigDecimal(nth);
        int usermultiplier = Integer.parseInt(multiplier);
        BigDecimal userpercentage = new BigDecimal(percentage);
        BigDecimal percentageOfGross = grossSalary.multiply(userpercentage);
        BigDecimal incomeAmount = percentageOfGross.multiply(new BigDecimal(usermultiplier));
        ifr.setValue("QNL_LA_FINALELIGIBILITY-Loanamountgrossincomepolicy", "incomeAmount");
        return incomeAmount.toString();
    }

    //Added by logaraj and keerthana on 26/06/2024 for finaleligibility onchange of calculation
    public String calcLoanAmount(IFormReference ifr) {
        try {

            Log.consoleLog(ifr, " into calcLoanAmount ");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = "";
            if (!loanSelected.isEmpty()) {
                loan_selected = loanSelected.get(0).get(0);
                Log.consoleLog(ifr, "loan type==>" + loan_selected);
            }
            String nth = ifr.getValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser").toString();
            String Multiplier = ifr.getValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser").toString();
            String AvgGrossIncome = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();

            Log.consoleLog(ifr, "nth:: " + nth);
            Log.consoleLog(ifr, "Multiplier:: " + Multiplier);
            Log.consoleLog(ifr, "AvgGrossIncome:: " + AvgGrossIncome);
            BigDecimal nthPer = new BigDecimal(nth);
            BigDecimal MultiplierValue = new BigDecimal(Multiplier);
            BigDecimal grossIncome = new BigDecimal(AvgGrossIncome);
            int usermultiplier = Integer.parseInt(Multiplier);
            BigDecimal percent = new BigDecimal(100);
            BigDecimal percentAmount = (grossIncome.multiply(nthPer)).divide(percent);
//find the percent value

            BigDecimal loanAmountasPerIncPolicy = percentAmount.multiply(MultiplierValue);
            Log.consoleLog(ifr, "loanAmountasPolicy ===> " + loanAmountasPerIncPolicy);
            String finalloanAmountasPerIncPolicy = String.valueOf(Math.round(Double.parseDouble(loanAmountasPerIncPolicy.toString())));

            Log.consoleLog(ifr, "ActualAmount:: " + finalloanAmountasPerIncPolicy);

            ifr.setValue("QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy", finalloanAmountasPerIncPolicy);

            String eligibleLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_Eligibileloanamount").toString();
            String ProductCapp = ifr.getValue("QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount").toString();
            String reqLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_LoanAmountrequested").toString();

            Log.consoleLog(ifr, "eligibleLoanAmount:: " + eligibleLoanAmount);
            Log.consoleLog(ifr, "ProductCapp:: " + ProductCapp);
            Log.consoleLog(ifr, "reqLoanAmount:: " + reqLoanAmount);

            BigDecimal loanAmoutEligible = new BigDecimal(eligibleLoanAmount);
            BigDecimal productSpeccapp = new BigDecimal(ProductCapp);
            BigDecimal loanAmountreq = new BigDecimal(reqLoanAmount);

            BigDecimal finaleligibility = loanAmoutEligible.min(loanAmountasPerIncPolicy).min(productSpeccapp).min(loanAmountreq);

            String finaleligibleloanAmount = String.valueOf(Math.round(Double.parseDouble(finaleligibility.toString())));
            String finalEligibleAmount = "";
            String loaAmountFinal = "";
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                String productCode = pcm.getProductCode(ifr);
                String finalAmountInParams = productCode + "," + finaleligibleloanAmount;
                finalEligibleAmount = bpcc.checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");

            } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                String finalAmountInParams = finaleligibleloanAmount + "," + finaleligibleloanAmount + "," + "CP";
                finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_PENSION", finalAmountInParams, "final_eligibility");
            } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                String Purpose = null;
                String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery,
                        "Execute query for fetching Purpose data from portal");
                if (PurposePortal.size() > 0) {
                    Purpose = PurposePortal.get(0).get(0);
                }
                String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
                String finalAmountInParams = productCode + "," + finaleligibleloanAmount;
                finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");
            } else {
                Log.consoleLog(ifr, "No loan selected:: ");

            }
            if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {
                Log.consoleLog(ifr, "eligibility Passed Successfully:::");
            } else {
                Log.consoleLog(ifr, " eligibility fail" + finalEligibleAmount);
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "F_InPrincipleEligibility", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
                message.put("eflag", "false");
                return message.toString();
            }
            BigDecimal FinalEligibleLoanAmount = mCheckFinalEligibleAmount(ifr, ProcessInstanceId, finaleligibleloanAmount);
            Log.consoleLog(ifr, "After fianal calculation and rounded finaleligibility Value 1 : " + finaleligibleloanAmount);
            double finaleligibilityAmtFinal = Math.floor(Double.parseDouble(FinalEligibleLoanAmount.toString()) / 1000) * 1000;
            loaAmountFinal = String.valueOf(Math.round(finaleligibilityAmtFinal));
            Log.consoleLog(ifr, "After fianal calculation and rounded finaleligibility Value 3: " + loaAmountFinal);
            ifr.setValue("QNL_LA_FINALELIGIBILITY_FinalNetTakeHome", loaAmountFinal);

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in FetchNTHRuleInput:: " + e);
            Log.errorLog(ifr, "Exception in FetchNTHRuleInput:: " + ExceptionUtils.getStackTrace(e));
        }

        return "";

    }
    //Added by Keerthana on 26/06/2024 for finaleligibility extra calculation by using liable table and caclulation

    public BigDecimal mCheckFinalEligibleAmount(IFormReference ifr, String ProcessInsanceId, String value) {
        Log.consoleLog(ifr, "Inside mCheckFinalEligibleAmount:" + value);
        BigDecimal FinalEligibleAmt = new BigDecimal(0);
        try {
            String AccountNo = "";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String journeyType = "";
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                journeyType = "BUDGET";
            } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                journeyType = "Pension";
            } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                journeyType = "VL";
            } else {
                Log.consoleLog(ifr, "No Journey type selected==>" + journeyType);
            }
            String acctnoQuery = ConfProperty.getQueryScript("getAccountNoQuery").replaceAll("#PID#", ProcessInsanceId);
            Log.consoleLog(ifr, "acctno query : " + acctnoQuery);
            List<List<String>> AcctNoBList = ifr.getDataFromDB(acctnoQuery);
            if (!AcctNoBList.isEmpty()) {
                AccountNo = AcctNoBList.get(0).get(0);
            }
            Log.consoleLog(ifr, "AccountNo value : " + AccountNo);
            BigDecimal bd = new BigDecimal(value);
            String exLoanAmt = "";
            if (!(value.trim().equalsIgnoreCase(""))) {
                Log.consoleLog(ifr, "Inside mCheckFinalEligibleAmount convert:");
                String EXISTINGLOANCHECKQUERY = ConfProperty.getQueryScript("EXISTINGLOANCHECKQUERY").replaceAll("#PID#", ProcessInsanceId);
                List<List<String>> ExLoanAmtlist = cf.mExecuteQuery(ifr, EXISTINGLOANCHECKQUERY, "EXISTINGLOANCHECKQUERY:");
                if (ExLoanAmtlist.size() > 0) {
                    exLoanAmt = ExLoanAmtlist.get(0).get(0);
                }
            }
            Log.consoleLog(ifr, "Existing loan amt from table value: " + exLoanAmt);
            BigDecimal EXISTINGLOANAMOUNT = new BigDecimal(exLoanAmt);
            Log.consoleLog(ifr, "EXISTINGLOANAMOUNT ===> " + EXISTINGLOANAMOUNT);
            Log.consoleLog(ifr, "calling TFLEnquiry api");
            String amtfrominsta = "";

            String insta = instaApiData.executeTFLEnquiry(ifr, ProcessInsanceId, journeyType, AccountNo);
            Log.consoleLog(ifr, "response==>" + insta);
            if (insta.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition TFLEnquiry ");
                //return RLOS_Constants.ERROR;
            } else {
                Log.consoleLog(ifr, "inside non-error condition TFLEnquiry Pension");
                JSONParser jsonparser = new JSONParser();
                JSONObject instaObj = (JSONObject) jsonparser.parse(insta);
                Log.consoleLog(ifr, instaObj.toString());
                amtfrominsta = instaObj.get("TFLAmtUtilized").toString();
                Log.consoleLog(ifr, "TFLAmtUtilized Value" + amtfrominsta);
            }
            BigDecimal bdDecimal = new BigDecimal(amtfrominsta);
            FinalEligibleAmt = bd.subtract(EXISTINGLOANAMOUNT).subtract(bdDecimal);
            Log.consoleLog(ifr, "PortalCommonMethods::mCheckFinalEligibleAmount::FinalEligibleAmt===> " + FinalEligibleAmt);
            //return FinalEligibleAmt;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside mCheckFinalEligibleAmount:" + e);
            Log.errorLog(ifr, "Exception inside mCheckFinalEligibleAmount:" + e);
        }
        return FinalEligibleAmt;

    }

    //Added by keerthana for account.no update on casa table on 01/07/2024
    public String addacctdetailsCP(IFormReference ifr) {
        Log.consoleLog(ifr, "Portal Pension customcode :: addacctdetailsCP ::IFormReference==>" + ifr);
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String AccountNo = "";
            String Strbranch = "";
            String StrDataopen = "";
            String mobileNo = "";
            String custid = "";
            String loanAcctOpen = "";
            String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", PID);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobileNo = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobileNo);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobileNo);
            String query1 = ConfProperty.getQueryScript("PORTALPENSIONDATAACC").replaceAll("#MOBILE_NUMBER#", mobileNo);
            Log.consoleLog(ifr, "Query for OccupationNonEditValues " + query1);
            List<List<String>> executeData = ifr.getDataFromDB(query1);;
            if (!executeData.isEmpty()) {
                Log.consoleLog(ifr, "Query for OccupationNonEditValues " + executeData.toString());
                AccountNo = executeData.get(0).get(0);
                Strbranch = executeData.get(0).get(1);
                custid = executeData.get(0).get(2);
            }
            Log.consoleLog(ifr, "calling 360 api");
            String response360 = objCbs360.executeCBSAdvanced360Inquiryv2(ifr, PID, custid, "Pension", "", "");
            Log.consoleLog(ifr, "response==>" + response360);
            if (response360.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition 360API Pension");
                return RLOS_Constants.ERROR;
            } else {
                Log.consoleLog(ifr, "inside non-error condition 360API Pension");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj360 = (JSONObject) jsonparser.parse(response360);
                Log.consoleLog(ifr, obj360.toString());
                loanAcctOpen = obj360.get("loanAcctOpenDatae").toString();
                Log.consoleLog(ifr, "loanAcctOpenDatae Value" + loanAcctOpen);
            }
            Log.consoleLog(ifr, "Portal Pension customcode :: mImpOnClickOccupationDetails:::" + AccountNo);
            String strfkey = bpcc.Fkey(ifr, "B");
            String Acctid = bpcc.Accountdetail(ifr);
            if (Acctid.equalsIgnoreCase("")) {

                String queryin = ConfProperty.getQueryScript("InsertAccdetailsCPQuery").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#loanAcctOpen#", loanAcctOpen);
                Log.consoleLog(ifr, "Portal Pension customcode :: addacctdetailsCP:::" + queryin);
                ifr.getDataFromDB(queryin);
            } else {
                String queryin = ConfProperty.getQueryScript("UPDATEACCDETCPQUERY").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#loanAcctOpen#", loanAcctOpen);
                Log.consoleLog(ifr, "Portal Pension customcode :: addacctdetailsCP:::" + queryin);
                ifr.getDataFromDB(queryin);

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in :addacctdetailsCP" + e);
            Log.errorLog(ifr, "Exception:addacctdetailsCP" + e);

        }
        return RLOS_Constants.ERROR;
    }

    //Added by keerthana for account.no update on casa table
    //Added by Ahmed on 02-07-2024 for Pension Disbursement 
    public String execLoanAccCreateAndDisbure(IFormReference ifr, String ProcessInsatnceId) {

        Log.consoleLog(ifr, "Entered into execLoanAccCreateAndDisbure Screen...");
        try {

            Log.consoleLog(ifr, "Details available to execute CBS API`s");
            String LoanAccountNumber = objPreprocess.execLoanAccountCreation(ifr, "Pension");
            if (!(LoanAccountNumber.contains(RLOS_Constants.ERROR))) {
                String CBSDisbursementEnquiry = objPreprocess.execDisbursementEnquiry(ifr, LoanAccountNumber, "Pension");
                if (!(CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR))) {
                    String CBS_LoanDeduction = objPreprocess.execLoanDeduction(ifr, LoanAccountNumber, "Pension");
                    if (!(CBS_LoanDeduction.contains(RLOS_Constants.ERROR))) {
                        String SessionId = objPreprocess.execComputeLoanSchedule(ifr, LoanAccountNumber, "Pension");
                        if (!(SessionId.contains(RLOS_Constants.ERROR))) {
                            String CBS_GenerateLoanSchedule = objPreprocess.execGenerateLoanSchedule(ifr, LoanAccountNumber, SessionId, "Pension");
                            if (!(CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                String CBS_SaveLoanSchedule = objPreprocess.execSaveLoanSchedule(ifr, LoanAccountNumber, SessionId, "Pension");
                                if (!(CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                    //CB7.CBS_RepaymentLoanSchedule(ifr, LoanAccountNumber);
                                    String CBS_BranchDisbursement = objPreprocess.execBranchDisbursement(ifr, LoanAccountNumber, "Pension");

                                    if (!(CBS_BranchDisbursement.contains(RLOS_Constants.ERROR))) {

                                        String Query = "SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
                                                + "WHERE WINAME='" + ProcessInsatnceId + "'";
                                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
                                        String loanAmount = "";
                                        String tenure = "";
                                        if (Output3.size() > 0) {
                                            loanAmount = Output3.get(0).get(0);
                                            tenure = Output3.get(0).get(1);
                                        }
                                        String SBACCNUMBER = "";
                                        String SBAccNo = "select accountid from los_nl_basic_info "
                                                + "where pid='" + ProcessInsatnceId + "' and rownum=1";
                                        Log.consoleLog(ifr, "SBAccNo==>" + SBAccNo);
                                        List<List<String>> SBACCNOOutput = ifr.getDataFromDB(SBAccNo);

                                        if (SBACCNOOutput.size() > 0) {
                                            SBACCNUMBER = SBACCNOOutput.get(0).get(0);
                                        }

                                        String CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER,
                                                "Pension", loanAmount, tenure);
                                        return CBS_FundTransfer;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);

        }
        return RLOS_Constants.ERROR;
    }

    //Added by logaraj on 02/07/2024 for pension
    public String caluclateEMI(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside caluclateEMI");
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String loanTenure = null;
//            String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
//            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query FROM PORTAL:");
//            if (list1.size() > 0) {
//                loanTenure = list1.get(0).get(2);
//            }
            loanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
//            String crg = pcm.mGetCRG(ifr);
//            String loanROI = pcm.mGetRoi(ifr, crg);
            String loanROI = ifr.getValue("QNL_LA_FINALELIGIBILITY_ROI").toString();
            Log.consoleLog(ifr, "loanROI : " + loanROI);
            String recommendeLoanamount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            String grossAmountStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
            String recommendeLoanAmount = recommendeLoanamount;
            Log.consoleLog(ifr, " recommendeLoanamount" + recommendeLoanamount);
            recommendeLoanamount = "-" + recommendeLoanamount;
            BigDecimal loanroii = new BigDecimal(loanROI);

            BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanamount, loanroii, Integer.parseInt(loanTenure));
            Log.consoleLog(ifr, "emi : " + emi);
            ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", ("₹ " + emi));
            BigDecimal grossAmount = new BigDecimal(grossAmountStr);
            BigDecimal recommendedLoanAmountstr = new BigDecimal(recommendeLoanAmount);
            BigDecimal salaryMultiplier = recommendedLoanAmountstr.divide(grossAmount, 2, RoundingMode.HALF_UP);
            Log.consoleLog(ifr, "Inside salaryMultiplier: " + salaryMultiplier);
            String salaryMultiplierStr = salaryMultiplier.toString();
            Log.consoleLog(ifr, "Inside SalaryMultiplier" + salaryMultiplierStr);
            ifr.setValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser", salaryMultiplierStr);
        } catch (Exception e) {
            Log.consoleLog(ifr, "caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    public void onChangeOthersLoanPurposeCP(IFormReference ifr) {
        Log.consoleLog(ifr, " Inside  onchange others loan purpose");
        String loanPurpose = ifr.getValue("P_CP_LD_Purpose").toString();
        if (loanPurpose.equalsIgnoreCase("OTH")) {
            ifr.setStyle("P_CP_LD_Purpose_Others", "visible", "true");
            ifr.setStyle("P_CP_LD_Purpose_Others", "mandatory", "true");
        } else {
            ifr.setStyle("P_CP_LD_Purpose_Others", "visible", "false");
        }
    }

    public void loanAccChangeCP(IFormReference ifr) {
        Log.consoleLog(ifr, "inside loanAccChange: ");

        JSONArray arr = new JSONArray();
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside loanAccChange: ");

                String query1 = "select LOANACCOUNTNO from LOS_NL_LOAN_ACC_CREATION where PID ='#PID#'".replaceAll("#PID#", ProcessInstanceId);
                //  ConfProperty.getQueryScript("LOANACCOUNTCREATEDCHECK").replaceAll("#PID#", ProcessInstanceId);

                List<List<String>> Loanaccount = cf.mExecuteQuery(ifr, query1, "Execute query for fetching LOAN ACCOUNTNO :: ");

                //Modified by Aravindh on 02/07/24
                String queryLAN = "SELECT * FROM  LOS_INTEGRATION_CBS_STATUS   WHERE TRANSACTION_ID ='#PID#'".replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> LANStatus = cf.mExecuteQuery(ifr, queryLAN, "Execute query for fetching LOANACCOUNTNO queryLAN:: ");
                if (Loanaccount.size() > 0) {
                    if (!Loanaccount.get(0).get(0).equalsIgnoreCase("ERROR")) {
                        Log.consoleLog(ifr, "inside Loanaccount: " + Loanaccount.get(0).get(0));
                        String LoanAccNumber = Loanaccount.get(0).get(0).replaceAll(" ", "");
                        Log.consoleLog(ifr, "inside Loanaccount Space Replaced: ====>" + LoanAccNumber + "<======");
                        boolean LoanaccountFlag = LoanAccNumber.matches("\\d+");
                        if (LoanaccountFlag == true) {
                            ifr.setStyle("BTN_LoanCreation", "disable", "true");
                            Log.consoleLog(ifr, "inside Loanaccount BTN_LoanCreation Hided: ");
                        } else {
                            ifr.setStyle("BTN_LoanCreation", "disable", "false");
                            ifr.clearTable("ALV_LOAN_ACC_CREATION");
                            Log.consoleLog(ifr, "inside Loanaccount BTN_LoanCreation Enabled as LAN digit not available: ");
                        }
                    } else if (Loanaccount.get(0).get(0).equalsIgnoreCase("ERROR")) {
                        ifr.clearTable("ALV_LOAN_ACC_CREATION");
                        ifr.setStyle("BTN_LoanCreation", "disable", "false");
                        Log.consoleLog(ifr, "inside Loanaccount BTN_LoanCreation Enabled as  ERROR Occured in previous Loan Account Creation: ");

                    }
                } else {
                    ifr.clearTable("ALV_LOAN_ACC_CREATION");
                    ifr.setStyle("BTN_LoanCreation", "disable", "false");
                    Log.consoleLog(ifr, "inside Loanaccount BTN_LoanCreation Enabled ");
                }
            }

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside loanAccChange Disbursement Checker : ");
                ifr.setStyle("BTN_LoanCreation", "disable", "true");
            }

        }
    }
    //added by logaraj for pension reset button on 05/07/2024

    public void ResetCoObligantCP(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside ResetCoObligantCB ===>");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "PID ResetCoObligantCB ===>" + PID);
        ifr.setValue("CoApplicantName_CB", "");
        ifr.setValue("P_CP_OD_MOBILE_NUMBER", "");
        ifr.setValue("P_CP_OD_CUSTOMER_ID", "");
        ifr.setValue("P_CP_OD_RELATIONSHIP_BORROWER_OTHERS", "");
        //ifr.setValue("P_CP_OD_Relationship_with_Borrower", "");
        Log.consoleLog(ifr, "Inside ResetCoObligantCB ===>");

        String PortalField = "";
        String strApplicantType = "CB";
        Log.consoleLog(ifr, "Applicant type::" + strApplicantType);
        String checkCountData = "Select  pid,Applicanttype from los_nl_basic_info  WHERE PID= '" + PID + "' and Applicanttype='" + strApplicantType + "'";
        Log.consoleLog(ifr, " checkCountData : " + checkCountData);
        List<List<String>> listCheck = ifr.getDataFromDB(checkCountData);
        Log.consoleLog(ifr, "listCheck : " + listCheck);
        if (!listCheck.isEmpty()) {
            for (int i = 0; i < listCheck.size(); i++) {
                if (listCheck.get(i).get(1).contains("CB")) {
                    try {
                        String query = "delete LOS_L_BASIC_INFO_I where f_key in (select f_key from LOS_NL_BASIC_INFO where PID='" + PID + "' and applicanttype='CB')";
                        cf.mExecuteQuery(ifr, query, "Delete query LOS_L_BASIC_INFO_I");
                        String query1 = " delete LOS_NL_Address where f_key in (select f_key from LOS_NL_BASIC_INFO where PID='" + PID + "' and applicanttype='CB')";
                        cf.mExecuteQuery(ifr, query1, "Delete query1 LOS_NL_Address");
                        String query2 = " delete los_nl_occupation_info where f_key in (select f_key from LOS_NL_BASIC_INFO where PID='" + PID + "' and applicanttype='CB')";
                        cf.mExecuteQuery(ifr, query2, "Delete query2 los_nl_occupation_info");
                        String query3 = " delete LOS_NL_BASIC_INFO where PID='" + PID + "' and applicanttype='CB'";
                        cf.mExecuteQuery(ifr, query3, "Delete query3 LOS_NL_BASIC_INFO");

                        ifr.setStyle("P_CoObligant_OD_Section", "visible", "false");
                        ifr.setStyle("P_OD_ValidateCoObligantCB", "disable", "false");
                        ifr.setStyle("P_CP_OD_MOBILE_NUMBER", "disable", "false");
                        ifr.setStyle("P_CP_OD_CUSTOMER_ID", "disable", "false");
                        ifr.setStyle("P_CP_OD_Relationship_with_Borrower", "disable", "false");
                        ifr.setStyle("P_CP_OD_RELATIONSHIP_BORROWER_OTHERS", "visible", "false");

                        ifr.setValue("CoApplicantName_CP", "");
                        ifr.setValue("P_CP_OD_MOBILE_NUMBER", "");
                        ifr.setValue("P_CP_OD_CUSTOMER_ID", "");
                        //ifr.setValue("P_CP_OD_EXISTING_CUSTOMER", "");
                        //ifr.setValue("P_CB_OD_Relationship_with_Borrower", "");

                        //PortalOccupationDetailsFieldsCoborrower
                        PortalField = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFieldsCoborrower");

                        Log.consoleLog(ifr, "PortalField::" + PortalField);
                        String portalfield[] = PortalField.split(",");
                        for (int j = 0; j < portalfield.length; j++) {
                            Log.consoleLog(ifr, "ResetCoObligantCP:setValue for occupation1 CoBo::" + (portalfield[j]));

                            ifr.setValue(portalfield[j], "");

                        }
                        Log.consoleLog(ifr, "End ResetCoObligantCP");
                        //updateCustomerDetails(ifr, Request, MobNum, map);
                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in updateCustomerAccountSummary ::" + e);
                        Log.errorLog(ifr, "Exception in updateCustomerAccountSummary::" + e);
                    }
                }
            }

        }

    }

    public static int differenceInMonths(IFormReference ifr, String oldDate) {
        Log.consoleLog(ifr, "differenceInMonths::oldDate" + oldDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(oldDate, formatter);
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date.withDayOfMonth(1), currentDate.withDayOfMonth(1));
        int monthsDiff = period.getYears() * 12 + period.getMonths();
        Log.consoleLog(ifr, "differenceInMonths::monthsDiff " + monthsDiff);
        return monthsDiff;
    }

    //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
    public String checkEligibilityWhetherValueIsEligible(IFormReference ifr, int eligibilityValue) {
        String minValue1 = ConfProperty.getCommonPropertyValue("MINVALUE1");//0
        String minValue2 = ConfProperty.getCommonPropertyValue("MINVALUE2");//50000
        int value1 = Integer.parseInt(minValue1);
        int value2 = Integer.parseInt(minValue2);
        if (eligibilityValue < value1 || eligibilityValue < value2) {
            JSONObject re = new JSONObject();
            Log.consoleLog(ifr, "Applicant Eligibility failed due to Non eligible values for Pension Scheme");
            re.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
            re.put("eflag", "false");
            return re.toString();
        }
        return "";
    }
    //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
    // Added by kathir for pension lead capture knockoff on 01-08-2024

    public String checkKnockOffLCPension(IFormReference ifr, String RuleName, String values, String ValueTag) {
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("total_knockoff_cp_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);

        try {
            Log.consoleLog(ifr, "inside checkKnockOffLCPension:::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId:::" + ProcessInstanceId);

            String[] brmsKeys = {
                "cp_kyc_op", "cp_writeoffhistfromapi_op", "cp_npavalfromapi_op", "nrichk_op", "cp_indcheck_op", "cp_extperloan_op", "cp_sma_op",
                "cp_entryage_op", "total_knockoff_cp_op"
            };

            String[] ruleNames = {
                "KYC Validation", "WRITEOFF HISTORY", "NPA CHECK", "NRI CHECK", "INDIVIDUAL CHECK",
                "EXISTING LOAN", "SMA CHECK", "ACCOUNT ELIGIBILITY", "KNOCK-OFF RULES"
            };

            List<String> brmsOutputs = new ArrayList<>();
            for (String key : brmsKeys) {
                String value = (String) objm.get(key);
                if (value != null && value.contains("#" + key + "#")) {
                    value = "NA";
                }
                brmsOutputs.add(value == null ? "NA" : value);
                Log.consoleLog(ifr, key + ":LCPension::" + value);
            }

            Log.consoleLog(ifr, "brmsOutputs:LCPension::" + brmsOutputs);

            String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
                    + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
                    + "WHERE c.PID = '" + ProcessInstanceId + "' "
                    + "and (c.ApplicantType='B')";
            Log.consoleLog(ifr, "Query1 data::" + Query1);
            List<List<String>> resultData = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "resultData::" + resultData.toString());

            JSONArray ALV_KnockOffRules = ifr.getDataFromGrid("ALV_KnockOffRules");
            Log.consoleLog(ifr, "ALV_KnockOffRules==>" + ALV_KnockOffRules);
            if (ALV_KnockOffRules.size() == 0) {
                if (!resultData.isEmpty()) {
                    JSONArray arr = new JSONArray();

                    for (List<String> rowData : resultData) {
                        Log.consoleLog(ifr, "rowData : " + rowData);
                        String brdata = rowData.get(0);
                        Log.consoleLog(ifr, "brdata : " + brdata);
                        JSONObject re = new JSONObject();
                        JSONArray childJsonArray = new JSONArray();

                        re.put("QNL_K_KNOCKOFFRULES_PartyType", brdata);
                        re.put("QNL_K_KNOCKOFFRULES_RuleName", "KNOCK-OFF RULES");
                        re.put("QNL_K_KNOCKOFFRULES_Output", totalGrade);

                        for (int i = 0; i < ruleNames.length && i < brmsOutputs.size(); i++) {
                            JSONObject childJsonObj = new JSONObject();
                            childJsonObj.put("RuleName", ruleNames[i]);
                            childJsonObj.put("Output", brmsOutputs.get(i));
                            childJsonArray.add(childJsonObj);
                        }

                        re.put("LV_KNOCKOFF", childJsonArray);
                        arr.add(re);
                    }
                    Log.consoleLog(ifr, "Knockoff grid json array:LCPension:" + arr);
                    ifr.addDataToGrid("ALV_KnockOffRules", arr, true);
                }
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception checkKnockOff LCPension: " + ex);
            Log.errorLog(ifr, "Exception checkKnockOff LCPension: " + ex);
        }

        Log.consoleLog(ifr, "LCPension totalGrade RETURN" + totalGrade);
        return totalGrade;
    }
}
