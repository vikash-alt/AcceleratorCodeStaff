/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.budget;

import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.commonobjects.ccm.Email;
import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.AccountMiniStatement;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.fintec.ConsumerAPI;
import com.newgen.dlp.integration.fintec.ExperianAPI;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import static com.newgen.iforms.acceleratorCode.AcceleratorActivityManagerCode.fetchDataFromGrid;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCustomCode;
import com.newgen.iforms.lad.LADPortalCustomCode;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

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
 * @author subham.bhakat
 */
public class BudgetPortalCustomCode {

    PortalCommonMethods pcm = new PortalCommonMethods();
    BRMSRules jsonBRMSCall = new BRMSRules();
    CommonFunctionality cf = new CommonFunctionality();
    ExperianAPI exp = new ExperianAPI();
    CustomerAccountSummary cas = new CustomerAccountSummary();
    CommonMethods objcm = new CommonMethods();
    LADPortalCustomCode LADPC = new LADPortalCustomCode();
    BRMCommonRules objbcr = new BRMCommonRules();
    LoanEligibilityCheck objlec = new LoanEligibilityCheck();

    public String CB_fromJSSampleFunction1(IFormReference ifr, String control, String event, String value) {

        //1 - Create SendOTP Request. Call Common method for this
        //2 - Create Connection and Call -  Common Function
        //3 - Parse Response - Common Function
        //4 - Write Business Logic. If needed decompose it in to other class and method
        //Called only on onload of any form, onload of a form need to put switch case in AcceleratorBaseCode class so that execution come to this classes this method.
        return "";
    }

    public String CB_fromActivityManagerTableSampleFunction2(IFormReference ifr, String control, String event, String value) {

        //1 - Create SendOTP Request. Call Common method for this
        //2 - Create Connection and Call -  Common Function
        //3 - Parse Response - Common Function
        //4 - Write Business Logic. If needed decompose it in to other class and method
        //called onclick or some other event where we have a scope to put custom control id in iform, need to insert a row in LOS_ACTIVITY_MANAGER_RLOS to make the execution come directly to this calsses this method/
        //dont need to route from the js file 
        return "";
    }

    public void setValueInHiddenSuperannuationField(IFormReference ifr) {
        Log.consoleLog(ifr, "inside  setValueInHiddenSuperannuationField");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("HiddenSuperannuationFieldValue").replaceAll("#PID#", PID);
            String query1 = ConfProperty.getQueryScript("HiddenProductType").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "setValueInHiddenSuperannuationField query value::" + query);
            Log.consoleLog(ifr, "setValueInHiddenSuperannuationField query1 value::" + query1);
            List<List<String>> ResultData = ifr.getDataFromDB(query);
            List<List<String>> ResultData1 = ifr.getDataFromDB(query1);
            Log.consoleLog(ifr, "setValueInHiddenSuperannuationField ResultData value::" + ResultData);
            Log.consoleLog(ifr, "setValueInHiddenSuperannuationField ResultData1 value::" + ResultData1);
            if (ResultData.size() > 0) {
                ifr.setValue("Hide_SuperannuationField", ResultData.get(0).get(0));
            }
            if (ResultData1.size() > 0) {
                ifr.setValue("Hide_ProductType", ResultData1.get(0).get(0));
                //ifr.setValue("P_CB_PA_TENURE","");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  setValueInHiddenSuperannuationField" + e);
            Log.errorLog(ifr, "Exception in  setValueInHiddenSuperannuationField" + e);
        }
    }

    public void addacctdetails(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String AccountNo = ifr.getValue("P_CB_OD_SalaryAccount").toString().split("-")[0];
        String Strbranch = ifr.getValue("P_CB_OD_SalaryAccount").toString().split("-")[1];
        String StrDataopen = ifr.getValue("P_CB_OD_SalaryAccount").toString().split("-")[2];
        String StrAccountbala = ifr.getValue("P_CB_OD_SalaryAccount").toString().split("-")[3];
        String stractprdcode = ifr.getValue("P_CB_OD_SalaryAccount").toString().split("-")[4];
        Log.consoleLog(ifr, "Portal Budget customcode :: mImpOnClickOccupationDetails:::" + AccountNo);
        String strfkey = Fkey(ifr, "B");
        String Acctid = Accountdetail(ifr);
        if (Acctid.equalsIgnoreCase("")) {

            String queryin = ConfProperty.getQueryScript("InsertAccdetailsQuery").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#StrDataopen#", StrDataopen).replaceAll("#StrAccountbala#", StrAccountbala).replaceAll("#stractprdcode#", stractprdcode);
            Log.consoleLog(ifr, "Portal Budget customcode :: addacctdetails:::" + queryin);
            ifr.getDataFromDB(queryin);
        } else {
            String queryin = ConfProperty.getQueryScript("UPDATEACCDETQUERY").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#StrDataopen#", StrDataopen).replaceAll("#StrAccountbala#", StrAccountbala).replaceAll("#stractprdcode#", stractprdcode);
            Log.consoleLog(ifr, "Portal Budget customcode :: addacctdetails:::" + queryin);
            ifr.getDataFromDB(queryin);

        }

    }

    public String Accountdetail(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String FKEY = Fkey(ifr, "B");
        String strAcctquery = ConfProperty.getQueryScript("SELECTACCOUNTDETAIL").replaceAll("#PID#", PID).replaceAll("#FKEY#", FKEY);
        Log.consoleLog(ifr, "FKEYQUERY :::" + strAcctquery);
        List<List<String>> strAcct = ifr.getDataFromDB(strAcctquery);
        String AccountId = "";
        //  String strfkey=
        if (strAcct.size() > 0) {
            AccountId = strAcct.get(0).get(0);

            Log.consoleLog(ifr, "AccountId :::" + AccountId);
            //   ifr.setValue("Occupation_Details_CB_combo12", AccountId);
        }
        return AccountId;
    }

    public String Fkey(IFormReference ifr, String Applicanttype) {

        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String Fquery = ConfProperty.getQueryScript("FKEYSELECTQUERY").replaceAll("#PID#", PID).replaceAll("#Applicanttype#", Applicanttype);

        Log.consoleLog(ifr, "FKEYQUERY :::" + Fquery);
        List<List<String>> strgey = ifr.getDataFromDB(Fquery);
        String strfkey = strgey.get(0).get(0);
        Log.consoleLog(ifr, "FKEYQUERY Output :::" + strfkey);

        return strfkey;
    }

    public void setfeild(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        //String query = "select Fullname , b.producttype ,c.branchcode from los_nl_basic_info a , los_ext_table b  ,los_l_sourcinginfo c where a.PID=b.PID  and b.PID=c.PID \n"
        // + "and b.PID='" + PID + "'";
        String query = ConfProperty.getQueryScript("setFieldQuery").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "setfeild Method called " + query);
        List<List<String>> result = ifr.getDataFromDB(query);
        if (!result.isEmpty()) {
            ifr.setValue("Q_BORROWERNAME", result.get(0).get(0));
            ifr.setValue("Q_PRODUCTTYPE", result.get(0).get(1));
            ifr.setValue("Q_BRANCHNAME", result.get(0).get(2));
        }

    }

    public String coapplicantcheck(IFormReference ifr, String strcocust, String strcomobile) {
        Log.consoleLog(ifr, "coapplicantcheck");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String strcustid = "";
        String strMobile = "";
        JSONObject message = new JSONObject();
        //String strCustquer = "select cifnumber,mobilenumber from los_wireference_table where winame='" + PID + "'";
        String strCustquer = ConfProperty.getQueryScript("coApplicantCheckquery").replaceAll("#PID#", PID);
        List<List<String>> result = ifr.getDataFromDB(strCustquer);
        if (!result.isEmpty()) {
            strcustid = result.get(0).get(0);
            strMobile = result.get(0).get(1);
            Log.consoleLog(ifr, "Co obligant validation Monesh");
        }
        Log.consoleLog(ifr, " Applicant custid ::" + strcustid);
        Log.consoleLog(ifr, "Applicant Mobile ::" + strMobile);

        if (strcustid.equalsIgnoreCase(strcocust)) {

            Log.consoleLog(ifr, "Co obligant validation failed");
            //JSONObject re = new JSONObject();
            return "Error";

        } else {
            return "Sucess";
        }
    }

    public String mImpOnClickOccupationDetails(IFormReference ifr, String control, String event, String value) {

        JSONObject message = new JSONObject();

        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            setfeild(ifr);
            pcm.setbranchcode(ifr);//Added by Ahmed on 07-08-2024

            String COMobNo = ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();
            String COCustID = ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
            String StrMessage = coapplicantcheck(ifr, COCustID, COMobNo);
            if (StrMessage.equalsIgnoreCase("Error")) {
                ifr.setValue("P_CB_OD_MOBILE_NUMBER", "");
                ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
                ifr.setStyle("P_CB_OD_MOBILE_NUMBER", "disable", "false");
                ifr.setStyle("P_CB_OD_CUSTOMER_ID", "disable", "false");
                Log.consoleLog(ifr, "before coapplicantcheck ");
                //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error", "Kindly enter different co-obligant customer number"));
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter different co-obligant customer number"));
                return message.toString();
            }
            String AccountNo = ifr.getValue("P_CB_OD_SalaryAccount").toString();
            if (AccountNo.isEmpty() || AccountNo.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "strAccoutnumber :::");
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly Choose the disbursement account details "));
                message.put("eflag", "false");
                return message.toString();

            }
            Log.consoleLog(ifr, "Entered into mImpOnClickOccupationDetails:::");
            Log.consoleLog(ifr, "Entered into mImpOnClickOccupationDetails:::" + ifr.getValue("P_CB_OD_SalaryAccount").toString());
            //  String strAccoutnumber=ifr.getValue("Occupation_Details_CB_combo12").toString();

            //Added by Aravindh
            pcm.populateOccuapationDetails(ifr, control, event, value);
            pcm.populateOccuapationDetailsforCoBorrower(ifr, control, event, value);

            //added by subham for co-borrower
            String Ext = ifr.getValue("P_CB_OD_EXISTING_CUSTOMER").toString();
            HashMap<String, String> map = new HashMap<>();
            map.put("MobileNumber", COMobNo);
            map.put("CustomerId", COCustID);
            Log.consoleLog(ifr, "BudgetPortalCustomCode:mImpOnClickOccupationDetails-MobileNumber:" + COMobNo);
            Log.consoleLog(ifr, "BudgetPortalCustomCode:mImpOnClickOccupationDetails-CustomerId:" + COCustID);

//        JSONParser jparser = new JSONParser();
//        JSONObject object = (JSONObject) jparser.parse(responseCO);
//        String ext_cust = object.get("CustomerFlag").toString();
//        String responseCOMobNumber = object.get("mobile_Number").toString();
//        String responseCOCustId = object.get("CustomerID").toString();
//        Log.consoleLog(ifr, "CustomerFlag...." + ext_cust);
//        Log.consoleLog(ifr, "mobile_Number...." + responseCOMobNumber);
//        Log.consoleLog(ifr, "CustomerID...." + responseCOCustId);
//        String MobileNo = pcm.getMobileNumber(ifr);
//        Log.consoleLog(ifr, "MobileNo==>" + MobileNo);
//        String applicantCustID = "";
//
//        //String Query1 = "select Customerid from  LOS_NL_BASIC_INFO where pid='" + PID + "' and applicanttype='B'";
//        String Query1 = ConfProperty.getQueryScript("getCustIdforBorrower").replaceAll("#PID#", PID);
//        List<List<String>> result = ifr.getDataFromDB(Query1);
//        if (!result.isEmpty()) {
//            applicantCustID = result.get(0).get(0);
//        }
//        if ((!ext_cust.equalsIgnoreCase("Y")) || ext_cust.isEmpty()) {
//            Log.consoleLog(ifr, "Co obligant validation failed");
//            message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
//            message.put("eflag", "false");//Hard Stop
//            return message.toString();
//        }
//        else {
            //if coborrower is existing
            if (Ext.equalsIgnoreCase("YES")) {

                String finalEligibleAmount = "";
                String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String CustomerId = pcm.getCustomerIDCB(ifr, "B");
                Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

                //Added by Ahmed..
                String MobileNo = pcm.getMobileNumber(ifr);
                Log.consoleLog(ifr, "MobileNo==>" + MobileNo);
                String response = cas.getCustomerAccountParams_CB(ifr, MobileNo);//Modified on 17-1-24
                Log.consoleLog(ifr, "response/getCustomerAccountParams_CB===>" + response);
                String productCode = pcm.getProductCode(ifr);
                Log.consoleLog(ifr, "ProductCode:" + productCode);
                String subProductCode = pcm.getSubProductCode(ifr);
                Log.consoleLog(ifr, "ProductCode:" + subProductCode);
                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
                String PANNUMBER = OutputJSON.get("PanNumber").toString();
                String DateofBirth = OutputJSON.get("DateofBirth").toString();

                Log.consoleLog(ifr, "AADHARNUMBER==>" + AADHARNUMBER);
                Log.consoleLog(ifr, "PANNUMBER===>" + PANNUMBER);
                Log.consoleLog(ifr, "DateofBirth===>" + DateofBirth);
                String APIResponse = mGetAPIData(ifr);
                //added by vandana 22-04-2024
                JSONParser parser1 = new JSONParser();
                //JSONObject OutputJSON1 = (JSONObject) parser1.parse(responseCO);
                //String AADHARNUMBERS = OutputJSON1.get("AadharNo").toString();
                Log.consoleLog(ifr, "Entered into mGetAPIData:::");

                if (APIResponse.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                JSONParser jp = new JSONParser();
                JSONObject obj = (JSONObject) jp.parse(APIResponse);
                String CustMisStatus = cf.getJsonValue(obj, "CustMisStatus");
                Log.consoleLog(ifr, "CustMisStatus==>" + CustMisStatus);
                CustMisStatus = "Complete";
                String custMisDecision = checkCustMisstatus(ifr, "knockoff_mischeck", CustMisStatus, "misstatus_output");
                if (custMisDecision.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                if (custMisDecision.equalsIgnoreCase("Eligible")) {
                    Log.consoleLog(ifr, "customer mis status Passed Successfully:::");
                    //added by ishwarya on 12022024
                    String cb = mCallBureau(ifr, "CB", AADHARNUMBER, "B");
                    if (cb.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    }
                    //added by vandana on 19-04-2024

                    String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                    //    mCheckCIBILScoreknockOff(ifr, "CB", subProductCode, productCode);
                    Log.consoleLog(ifr, "decisionBorrowerCibil::" + decision);
                    if (decision.contains(RLOS_Constants.ERROR)) {
                        return RLOS_Constants.ERROR;
                    } else if (decision.equalsIgnoreCase("Approve")) {
                        Log.consoleLog(ifr, "CIBIL Passed Successfully:::");
                        
                        String requestloan = ConfProperty.getQueryScript("REQUESTEDLOANAMT").replaceAll("#PID#", ProcessInstanceId);
                           List< List< String>> querylAPExistResult =cf.mExecuteQuery(ifr,requestloan,"Requested loan amount query ");
            Log.consoleLog(ifr, "querylAPExistResult===>" + querylAPExistResult);
            String requestloanamt = "";
            if (!querylAPExistResult.isEmpty()) {
                requestloanamt = querylAPExistResult.get(0).get(0);
            }
            else {
             return pcm.returnErrorcustmessage(ifr, "Error Loan Amount ");
            
            }
                        if (Integer.parseInt(requestloanamt) > 100000) {
                         String EX = mCallBureau(ifr, "EX", AADHARNUMBER, "B");
                        if (EX.contains(RLOS_Constants.ERROR)) {
                            return pcm.returnError(ifr);
                        }
                        
                        decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "Ex", "B");
                          }
                          else {
                          decision="Approve";
                          
                          }
                        //mCheckCIBILScoreknockOff(ifr, "EX", subProductCode, productCode);
                        Log.consoleLog(ifr, "decision2/EX::" + decision);

                        if (decision.contains(RLOS_Constants.ERROR)) {
                            return pcm.returnError(ifr);
                        } else if (decision.equalsIgnoreCase("Approve")) {
                            //Bureo check data population End 
                            populatecicScore(ifr, "B");
                            String KnockofCIC_Eligibility = objbcr.checkCICEligibility(ifr, productCode, subProductCode);
                            if (KnockofCIC_Eligibility.contains(RLOS_Constants.ERROR)) {
                                return pcm.returnError(ifr);
                            } else if (KnockofCIC_Eligibility.equalsIgnoreCase("Approve")) {

                                // populatecicScore(ifr, "CB");
                                Log.consoleLog(ifr, "inside knockoff brms:::");
                                //  String pan = "";
                                //   String aadhar = "";
                                // String Age;
                                String Years = "0";
                                //   String nri = "";
                                //    String staffcheck = "";
                                // String salariedacc = "";
//                            aadhar = OutputJSON.get("AadharNo").toString();
//                            aadhar = aadhar.equalsIgnoreCase("") ? "No" : "Yes";
//                            Log.consoleLog(ifr, "aadhar:::" + aadhar);
////                            pan = OutputJSON.get("PanNumber").toString();
////                            pan = pan.equalsIgnoreCase("") ? "No" : "Yes";
////                            Log.consoleLog(ifr, "pan:::" + pan);
//                            nri = OutputJSON.get("NRI").toString();
//                            nri = nri.equalsIgnoreCase("N") ? "No" : "Yes";
//                            Log.consoleLog(ifr, "nri:::" + nri);
//                            staffcheck = OutputJSON.get("Staff").toString();
//                            staffcheck = staffcheck.equalsIgnoreCase("Y") ? "Yes" : "No";
//                            Log.consoleLog(ifr, "staffcheck:::" + staffcheck);
                                // salariedacc = OutputJSON.get("productCode").toString();
                                //   Log.consoleLog(ifr, "salariedacc:::" + salariedacc);
//                            //pcm.getParamValue(ifr, "CASACHECK", "SBACCTRELATION");
//                            String[] pCodeConstants = pcm.getParamValue(ifr, "CASACHECKCB", "SBACCTPRD").split(",");
//                            //{"144", "145", "146", "148"};
//                            String[] prodCode = salariedacc.split(",");
//                            String FKEY = Fkey(ifr, "B");
//                            String strAcctquery = ConfProperty.getQueryScript("SELECTACCOUNTDETAIL").replaceAll("#PID#", PID).replaceAll("#FKEY#", FKEY);
//                            Log.consoleLog(ifr, "FKEYQUERY :::" + strAcctquery);
//                            List<List<String>> strAcct = cf.mExecuteQuery(ifr, strAcctquery, "Getproductcode for account selected :::");
//                            // ifr.getDataFromDB(strAcctquery);
//                            Log.consoleLog(ifr, "strAcct :::" + strAcct);
//                            //String strprdcode = strAcct.get(0).get(4);
//                            String strprdcode = strAcct.get(0).get(4);
//
//                            salariedacc = strprdcode;
//                            Log.consoleLog(ifr, "strprdcode :::" + strprdcode);
//                            boolean containsCode = false;
//
//                            for (String constant : pCodeConstants) {
//                                if (strprdcode.contains(constant)) {
//                                    containsCode = true;
//                                    salariedacc = constant;
//                                    break;
//                                }
//
//                                if (containsCode) {
//                                    break;
//                                }
//                            }
//                            salariedacc = "144";

                                //  Log.consoleLog(ifr, "salariedacc :::" + salariedacc);
                                //Age = cf.getJsonValue(obj, "Age");
//Commented by Ahmed as the below things are not used in BRMS
//                            String count = cf.getJsonValue(obj, "count");
//                            //String FlgCustType = cf.getJsonValue(obj, "FlgCustType");
//                            String FlgCustType = "R";
                                //String Sma2Count2Months = cf.getJsonValue(obj, "Sma2Count2Months");// need calladvanced 360 api v2
//
//                            String Sma2Count2Months = cf.getJsonValue(obj, "smaExists");
//                            String FlgCustType = cf.getJsonValue(obj, "FlgCustType");
//                            String salariedacc = cf.getJsonValue(obj, "salExists");
                                //   String Sma2Count2Months = "0";
                                //    String accrelation_ip = "Y";
                                //   String productcode_ExistingLoan_ip = "101";
//                            String cic_dpd_ip = "20";
//                            String cic_npa_ip = "Y";
//                            String cic_writeoff_ip = "01";
//                            String paplExist = cf.getJsonValue(obj, "PAPLExist");
//                            paplExist = paplExist.equalsIgnoreCase("NA") ? "No" : "Yes";
//                            String classification = cf.getJsonValue(obj, "Classification");
//                            classification = classification.equalsIgnoreCase("NA") ? "No" : "Yes";
//
//                            String existSTP = paplExist;
//                            String nro = nri;
                                Log.consoleLog(ifr, "Before knockoffDecision:::: ");
//                            Log.consoleLog(ifr, " Aadhar:: " + aadhar + " ,Pan:: " + pan + " ,writeOffPresent:: " + writeOffPresent + " ,DOB:: " + DOB + " ,Classification:: "
//                                    + classification + " ,ProductCode:: " + productCode + " ,staffcheck:: " + staffcheck + " ,Nri:: " + nri
//                                    + " ,NRO:: " + nro + " ,salariedacc:: " + salariedacc + ",FlgCustType::" + FlgCustType + ",CustomerAge::" + Age
//                                    + ",Sma2Count::" + Sma2Count2Months + ",accrelation_ip::" + accrelation_ip + ",productcode_ExistingLoan_ip::" + productcode_ExistingLoan_ip
//                            );
//                            Log.consoleLog(ifr, "after knockoffDecision:::: ");
//                            String knockoffInParams = "B," + aadhar + "," + pan + "," + DOB + "," + writeOffPresent + "," + classification
//                                    + "," + productCode + "," + staffcheck + "," + nro + "," + nri + "," + salariedacc + "," + FlgCustType
//                                    + "," + Age + "," + Sma2Count2Months + "," + accrelation_ip + "," + productcode_ExistingLoan_ip;
//                            
//                            String knockoffDecision = checkKnockOff(ifr, "knock_of_CB_Rule", knockoffInParams, "total_knockoff_cb_op");
//                            if (knockoffDecision.contains(RLOS_Constants.ERROR)) {
//                                return pcm.returnError(ifr);
//                            } else if (knockoffDecision.contains("Proceed")) {
//                                Log.consoleLog(ifr, "knockoff Passed Successfully:::");

                                //  String DOB = cf.getJsonValue(obj, "DOB");
                                Years = cf.getJsonValue(obj, "Years");

                                String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
                                writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";

                                Log.consoleLog(ifr, "inside eligibility brms:::");
                                String loanTenure = null;
                                //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='S22'";
                                String tenureData_Query = ConfProperty.getQueryScript("getmaxTenureQuery");
                                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
                                if (list1.size() > 0) {
                                    loanTenure = list1.get(0).get(0);
                                }
                                Log.consoleLog(ifr, "loanTenure : " + loanTenure);
                                String loanROI = "";
                                String loanROIQuery = "select Effectiveroi from los_nl_proposed_facility where pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
                                List<List<String>> list2 = cf.mExecuteQuery(ifr, loanROIQuery, "loanROIQuery:");
                                if (list2.size() > 0) {
                                    loanROI = list2.get(0).get(0);
                                }
                                //String roiData_Query = "select totalroi from los_m_roi where roiid='R21'";
                                /* String roiData_Query = ConfProperty.getQueryScript("getROIDataQuery");
                                List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
                                if (list2.size() > 0) {
                                    loanROI = list2.get(0).get(0);
                                }*/
                                Log.consoleLog(ifr, "roi : " + loanROI);
                                String obligation = null;
                                //String obligation_Query = "select TOTEMIAMOUNT from LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BureauType='EX'";
                                String obligation_Query = ConfProperty.getQueryScript("getTotalAmountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);;
                                Log.consoleLog(ifr, "obligation_Query===>" + obligation_Query);
                                List<List<String>> list = cf.mExecuteQuery(ifr, obligation_Query, "obligation_Query:");
                                if (list.size() > 0) {
                                    obligation = list.get(0).get(0);
                                }
                                String obligationInput = obligation.equalsIgnoreCase("") ? "0" : obligation;

                                String schemeID = pcm.getBaseSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                                Log.consoleLog(ifr, "schemeID:" + schemeID);

                                String Prodcapping = "";
                                String reqAmount = "";
                                String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
                                List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
                                if (ProdcappingList.size() > 0) {
                                    Prodcapping = ProdcappingList.get(0).get(0);
                                }
                                Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

                                String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY").replaceAll("#ProcessInsanceId#", ProcessInstanceId);
                                List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
                                if (list4.size() > 0) {
                                    reqAmount = list4.get(0).get(0);
                                    // reqAmount = reqAmount.isEmpty() ? "50000" : reqAmount; // NEED TO BE REMOVED 
                                }
                                Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);
                                // Add vandana 
                                String loantenure = ifr.getValue("P_CB_OD_DATEOFSUPERANNUATION").toString();
                                Log.consoleLog(ifr, "P_CB_OD_DATEOFSUPERANNUATION  : " + loantenure);
                                int months = differenceInMonths(ifr, loantenure);
                                loanTenure = String.valueOf(months);
                                String MaxTenure = "";
                                //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                                String MaxTenure_Query = ConfProperty.getQueryScript("getTenureDataQuery").replaceAll("#schemeID#", schemeID);
                                List<List<String>> MaxTenurelist1 = cf.mExecuteQuery(ifr, MaxTenure_Query, "MaxTenure_Query:");
                                if (MaxTenurelist1.size() > 0) {
                                    MaxTenure = MaxTenurelist1.get(0).get(0);
                                }
                                if (months >= Integer.parseInt(MaxTenure)) {
                                    loanTenure = String.valueOf(MaxTenure);
                                }
                                Log.consoleLog(ifr, "P_CB_OD_DATEOFSUPERANNUATION loanTenure : " + loanTenure);
                                //End
                                String deductionmonth = ifr.getValue("P_CB_OD_DeductionFromSalary").toString();
                                String grosssalary = ifr.getValue("P_CB_OD_GrossSalary").toString();
                                HashMap hm = new HashMap();
                                hm.put("cibiloblig", obligationInput);
                                Log.consoleLog(ifr, "cibiloblig===>" + obligationInput);
                                hm.put("tenure", String.valueOf(loanTenure));
                                hm.put("roi", String.valueOf(loanROI));
                                hm.put("loancap", Prodcapping);
                                hm.put("reqAmount", reqAmount);
                                hm.put("deductionmonth", deductionmonth);
                                hm.put("grosssalary", grosssalary);
                                BudgetBkoffCustomCode budgetBO = new BudgetBkoffCustomCode();
                                String finalelig = budgetBO.getAmountForInprincipleDataSaveBO(ifr, hm);
                                Log.consoleLog(ifr, "finalelig===>" + finalelig);
                                if (finalelig.contains(RLOS_Constants.ERROR)) {
                                    return RLOS_Constants.ERROR;
                                }
                                String Query = "SELECT COUNT(*) FROM LOS_L_FINAL_ELIGIBILITY "
                                        + "WHERE PID='" + ProcessInstanceId + "'";
                                Log.consoleLog(ifr, "Query===>" + Query);
                                List Result = ifr.getDataFromDB(Query);
                                String Count = Result.toString().replace("[", "").replace("]", "");
                                if (Count.equalsIgnoreCase("")) {
                                    Count = "0";
                                }
                                if (Integer.parseInt(Count) == 0) {
                                    //String Query2 = "INSERT INTO LOS_L_FINAL_ELIGIBILITY (PID) VALUES ('" + ProcessInstanceId + "')";
                                    String Query2 = ConfProperty.getQueryScript("insertQueryforPIDinFinalEligibility").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#finalelig#", finalelig);
                                    Log.consoleLog(ifr, "Query1===>" + Query2);
                                    ifr.saveDataInDB(Query2);
                                }
                                //String Query2 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
                                // + "SET LOAN_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
                                String Query2 = ConfProperty.getQueryScript("updateQueryforLoanamtinFinalEligibility").replaceAll("#finalelig#", finalelig).replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                                Log.consoleLog(ifr, "Query2===>" + Query2);
                                ifr.saveDataInDB(Query2);
                                //String Query3 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
                                //+ "SET IN_PRINCIPLE_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
                                String Query3 = ConfProperty.getQueryScript("updateQueryforPrincipleamtinFinalEligibility").replaceAll("#finalelig#", finalelig).replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                                Log.consoleLog(ifr, "Query3===>" + Query3);
                                ifr.saveDataInDB(Query3);

                                String finalAmountInParams = productCode + "," + finalelig;
                                finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");
                                //String finalEligibleAmountCB = elibilityRuleForCoBorrower(ifr);
                                //if (finalEligibleAmount.equalsIgnoreCase("Eligible") && finalEligibleAmountCB.equalsIgnoreCase("Eligible")) {
                                if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {
                                    Log.consoleLog(ifr, "eligibility Passed Successfully:::");
                                    Log.consoleLog(ifr, "inside scorecard brms:::");
                                    String npa_inp = "";
                                    String bscore = "";
                                    String overduedays_inp = "";
                                    String existcust_inp = "";
                                    String overduedaysupto = "";
                                    String monthlydeduction = "0";
                                    int maxOccurrences = 0;
                                    String paymentHistoryIp = "";
                                    String maxOccurrencesNumber = null;
                                    String settleHistory = "";
                                    String GUARANTORNPAINP = "";
                                    String GUARANTORWRITEOFFSETTLEDHIST = "";

                                    String BureauDataResponseBorrower = objcm.getMaxCICScoreDatas(ifr, "B");
                                    String[] bSplitter = BureauDataResponseBorrower.split("-");
                                    String bureauTypeB = bSplitter[0];
                                    String ApplicantTypeB = bSplitter[1];
                                    monthlydeduction = bSplitter[2];

                                    bscore = bSplitter[3];
                                    paymentHistoryIp = bSplitter[4];
                                    npa_inp = bSplitter[5];
                                    settleHistory = bSplitter[6];

                                    GUARANTORNPAINP = bSplitter[7];

                                    GUARANTORWRITEOFFSETTLEDHIST = bSplitter[8];

                                    // npa_inp = (npa_inp.equalsIgnoreCase("NA") || npa_inp.equalsIgnoreCase("")) ? "No" : "Yes";
                                    Log.consoleLog(ifr, "npa_inp:::: " + npa_inp);
                                    Log.consoleLog(ifr, "settleHistory:::: " + settleHistory);
                                    Log.consoleLog(ifr, "GUARANTORNPAINP:::: " + GUARANTORNPAINP);
                                    Log.consoleLog(ifr, "GUARANTORWRITEOFFSETTLEDHIST:::: " + GUARANTORWRITEOFFSETTLEDHIST);
                                    monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;
                                    //String scoreQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "'";
//                                String scoreQuery = ConfProperty.getQueryScript("getscoreQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
//                                List<List<String>> Result1 = cf.mExecuteQuery(ifr, scoreQuery, "Exexuting the Query value ");
//                                if (!Result1.isEmpty()) {
//
//                                    bscore = Result1.get(0).get(0);
//                                    npa_inp = Result1.get(0).get(1);
//                                    npa_inp = npa_inp.equalsIgnoreCase("") ? "No" : "Yes";
//                                    monthlydeduction = Result1.get(0).get(3);
//                                   
//                                    Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
//                                    Log.consoleLog(ifr, "bscore:::: " + bscore);
//                                }
                                    Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
                                    Log.consoleLog(ifr, "bscore:::: " + bscore);
                                    Log.consoleLog(ifr, "before query"); //modified by vandana on 26/06/2024 for scorecard
//                                String paymentquery = "SELECT NVL(REGEXP_REPLACE(PAYHISTORYCOMBINED, '[^0-9]', ''), 0) AS replaced_column FROM LOS_CAN_IBPS_BUREAUCHECK where PROCESSINSTANCEID='" + ProcessInstanceId + "'";
//
//                                List<List<String>> paymentHistory = ifr.getDataFromDB(paymentquery);
//                                if (!paymentHistory.isEmpty()) {
//                                    paymentHistoryIp = paymentHistory.get(0).get(0);
//                                    Log.consoleLog(ifr, "PAYMENTHISTORY:::: " + paymentHistoryIp);
//                                }
                                    Log.consoleLog(ifr, "PAYMENTHISTORY:::: " + paymentHistoryIp);
                                    String[] splitPaymentHistory = paymentHistoryIp.split("(?<=\\G.{3})"); // Splits every 3 chars without adding a delimiter
                                    Log.consoleLog(ifr, "parts:::: " + splitPaymentHistory);
                                    int maxPaymentHistory = Integer.MIN_VALUE;
                                    Map<Integer, Integer> countMap = new HashMap<>();

                                    // Find maxPaymentHistory and count occurrences
                                    for (String part : splitPaymentHistory) {
                                        int values = Integer.parseInt(part); // Assuming decimal integers
                                        countMap.put(values, countMap.getOrDefault(values, 0) + 1);
                                        maxPaymentHistory = Math.max(maxPaymentHistory, values);
                                    }

                                    // Find the range containing the max value
                                    String highestRangeKey = getRangeKey(maxPaymentHistory);

                                    // Count numbers within the highest range
                                    int countInHighestRange = getCountInRange(countMap, highestRangeKey);

                                    String maxOccurrencesip = String.valueOf(countInHighestRange);
                                    maxOccurrencesNumber = String.valueOf(maxPaymentHistory);
                                    overduedays_inp = maxOccurrencesNumber;
                                    Log.consoleLog(ifr, "overduedays_inp:::: " + overduedays_inp);
                                    String overduedays_inps = overduedays_inp.equalsIgnoreCase("0") ? "No" : "yes";
                                    overduedaysupto = maxOccurrencesip;
                                    Log.consoleLog(ifr, "overduedaysupto :::: " + overduedaysupto);

                                    Log.consoleLog(ifr, "Before netincome :::: ");
                                    String netIncome = ifr.getValue("P_CB_OD_NetIncome").toString();
                                    String category = ifr.getValue("P_CB_OD_Category").toString();
                                    String grossSalary = ifr.getValue("P_CB_OD_GrossSalary").toString();
                                    String deductionSalary = ifr.getValue("P_CB_OD_DeductionFromSalary").toString();
                                    String expYears = ifr.getValue("P_CB_OD_ExperienceYear").toString();//modified by vandana on 26/06/2024 for scorecard
                                    String residence_inp = ifr.getValue("P_CB_OD_Residence").toString();
                                    String natureOfSecurity = ifr.getValue("P_CB_OD_NatureOfSecurity").toString();//P_CB_OD_NatureOfSecurity
                                    String recovery = ifr.getValue("P_CB_OD_RecoveryMechanism").toString();//P_CB_OD_RecoveryMechanism
                                    String Existinginprange = ifr.getValue("P_CB_OD_RelationshipWithCanara").toString();

//                                String AccountHoldertypeCode = cf.getJsonValue(obj, "AccountHoldertypeCode");
                                    //log.consoleLog(ifr, "AccountHoldertypeCode " + AccountHoldertypeCode);//modified by vandana on 26/06/2024 for scorecard
//                                AccountHoldertypeCode = AccountHoldertypeCode.equalsIgnoreCase("7") ? "Yes" : "No";
                                    //String guarantorwriteoff = AccountHoldertypeCode;
//                                Log.consoleLog(ifr, "overduedays_inps : "+overduedays_inps +" guarantorwriteoff : "+guarantorwriteoff );
//                                ifr.addItemInCombo("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", overduedays_inps);
//                                ifr.addItemInCombo("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", guarantorwriteoff);
                                    //    Log.consoleLog(ifr, "insert :::: " + Updatequery);
                                    //
//                                String accountstatus = cf.getJsonValue(obj, "Account_Status");
//                                Log.consoleLog(ifr, "accountstatus " + accountstatus);
//                                if ((accountstatus.equalsIgnoreCase("00") || accountstatus.equalsIgnoreCase("40") || accountstatus.equalsIgnoreCase("52") || accountstatus.equalsIgnoreCase("13")
//                                        || accountstatus.equalsIgnoreCase("15") || accountstatus.equalsIgnoreCase("16") || accountstatus.equalsIgnoreCase("17")
//                                        || accountstatus.equalsIgnoreCase("12") || accountstatus.equalsIgnoreCase("11") || accountstatus.equalsIgnoreCase("71") || accountstatus.equalsIgnoreCase("78")
//                                        || accountstatus.equalsIgnoreCase("80") || accountstatus.equalsIgnoreCase("82") || accountstatus.equalsIgnoreCase("83") || accountstatus.equalsIgnoreCase("84")
//                                        || accountstatus.equalsIgnoreCase("DEFAULTVALUE") || accountstatus.equalsIgnoreCase("21") || accountstatus.equalsIgnoreCase("22") || accountstatus.equalsIgnoreCase("23")
//                                        || accountstatus.equalsIgnoreCase("24") || accountstatus.equalsIgnoreCase("25"))) {
//
//                                    accountstatus = "No";
//                                } else {
//                                    accountstatus = "Yes";
//                                }
//                                String guarantornpa_inp;
//                                if (AccountHoldertypeCode.equalsIgnoreCase("Yes") && accountstatus.equalsIgnoreCase("Yes")) {
//                                    guarantornpa_inp = "Yes";
//                                } else {
//                                    guarantornpa_inp = "No";
//                                }
//                                String nparestmonths_inpo = accountstatus;
//                                String settledhist_inp = accountstatus;
                                    String guarantorwriteoff = GUARANTORWRITEOFFSETTLEDHIST;
                                    String nparestmonths_inpo = settleHistory;
                                    String settledhist_inp = settleHistory;
                                    String writeOff = settleHistory;
                                    String guarantornpa_inp = GUARANTORNPAINP;
                                    String guarantorsettledhist_inp = GUARANTORWRITEOFFSETTLEDHIST;
                                    // added by vandana
                                    //    Log.consoleLog(ifr, "Befor Updatequery :::: ");
                                    String subProduct = "Update los_nl_proposed_facility set SUBPRODUCT='STP-CB'  where PID='" + PID + "'";
                                    //ifr.saveDataInDB(subProduct);
                                    cf.mExecuteQuery(ifr, subProduct, "subProduct");

                                    String Updatequery = "Update LOS_NL_BASIC_INFO set OVERDUEINCREDITHISTORY='" + overduedays_inps + "' , SETTLEDACCOUNTINCREDITHISTORY ='" + settledhist_inp + "' where PID='" + PID + "' and APPLICANTTYPE='B'";
                                    ifr.saveDataInDB(Updatequery);
                                    String existcust_inpQuery = ConfProperty.getQueryScript("getExistingCustomer").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                                    List<List<String>> Results = cf.mExecuteQuery(ifr, existcust_inpQuery, "Exitingquery");
                                    if (!Results.isEmpty()) {
                                        existcust_inp = Results.get(0).get(0);
                                        existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
                                    }
                                    //added by vandana
                                    String inPrincipleAmount = "";
                                    String amount_Query = "SELECT round(IN_PRINCIPLE_AMOUNT) FROM LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInstanceId + "'";
                                    List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

                                    if (!Results1.isEmpty()) {
                                        inPrincipleAmount = Results1.get(0).get(0);
                                        Log.consoleLog(ifr, "inPrincipleAmount " + inPrincipleAmount);
                                    }
                                    Log.consoleLog(ifr, "Before conversion ");
                                    int grossSalaryip1 = Integer.parseInt(grossSalary);
//                            
                                    Log.consoleLog(ifr, "Before bigdecimal conversion  ");
                                    BigDecimal deductionSalaryip = new BigDecimal(deductionSalary);
                                    Log.consoleLog(ifr, "after bigdecimal conversion deductionSalary" + deductionSalaryip);
                                    BigDecimal grossSalaryip = new BigDecimal(grossSalary);
                                    Log.consoleLog(ifr, "after bigdecimal conversion grossSalaryip " + grossSalaryip);
                                    BigDecimal MonthlyDeductionip = new BigDecimal(monthlydeduction);
                                    Log.consoleLog(ifr, "mImpOnClickOccupationDetails:::::after bigdecimal conversion MonthlyDeductionip " + MonthlyDeductionip);
                                    BigDecimal loanamount = new BigDecimal(inPrincipleAmount);
                                    Log.consoleLog(ifr, "after bigdecimal conversion loanamount" + loanamount);
                                    BigDecimal ftRoi = new BigDecimal(loanROI);
                                    Log.consoleLog(ifr, "after bigdecimal conversion ftRoi " + ftRoi);
                                    int loanTenures = Integer.parseInt(loanTenure);
                                    BigDecimal perposedEmi = calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
                                    Log.consoleLog(ifr, "after bigdecimal conversion perposedEmi" + perposedEmi);
                                    BigDecimal netIncomeip = grossSalaryip.subtract(deductionSalaryip).subtract(MonthlyDeductionip).subtract(perposedEmi);

                                    Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip" + netIncomeip);

                                    double netIncomeip1 = netIncomeip.intValue();
                                    Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip1" + netIncomeip1);

                                    double nethomeins_inprange1 = (netIncomeip1 / grossSalaryip1) * 100;
                                    double doubleValue = nethomeins_inprange1;
                                    int nethomeins_inprange = (int) doubleValue;
                                    Log.consoleLog(ifr, "after bigdecimal conversion nethomeins_inprange" + nethomeins_inprange);
                                    int anninc_inp = grossSalaryip1 * 12;

                                    Log.consoleLog(ifr, "bscore:::" + bscore + ",overduedays_inp:::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff:::"
                                            + guarantorwriteoff + ",npa_inp:::" + npa_inp + ",nparestmonths_inpo:::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",writeOffPresent:::" + writeOffPresent
                                            + ",nethomeins_inprange:::" + nethomeins_inprange + ",category:::" + category + ",anninc_inp:::" + anninc_inp + ",grossSalary:::" + grossSalary + ",expYears:::" + expYears + ",existcust_inp:::" + existcust_inp
                                            + ",Existinginprange::" + Existinginprange + ",natureOfSecurity:::" + natureOfSecurity + ",residence_inp:::" + residence_inp + ",recovery:::" + recovery);
                                    String scorecardInParams = "";
                                    String scorecardDecision = "";
                                    if (!bscore.equalsIgnoreCase("I")) {

                                        scorecardInParams = bscore + "," + overduedays_inp + "," + overduedaysupto + "," + guarantorsettledhist_inp + "," + guarantornpa_inp + ","
                                                + guarantorwriteoff + "," + npa_inp + "," + nparestmonths_inpo + "," + settledhist_inp + "," + writeOff
                                                + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                                                + "," + Existinginprange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                                        scorecardDecision = checkScoreCard(ifr, "CB_SCORECARD", scorecardInParams, "totalgrade_out", "B");
                                        Log.consoleLog(ifr, "scoreCardDecisionIf:::" + scorecardDecision);
                                    } else {

                                        scorecardInParams = npa_inp + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                                                + "," + Existinginprange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                                        scorecardDecision = checkScoreCard(ifr, "CB_ScoreCard_CICImmune", scorecardInParams, "totalgrade_out", "B");
                                        Log.consoleLog(ifr, "scoreCardDecisionElse:::" + scorecardDecision);
                                    }
                                    //String scoreCardDecisionCB = scoreCardDecisionForCB(ifr);
                                    //Log.consoleLog(ifr, "scoreCardDecisionCB:::" + scoreCardDecisionCB);
                                    if (scorecardDecision.equalsIgnoreCase(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                                        return pcm.returnError(ifr);
                                    }
                                    if (scorecardDecision.equalsIgnoreCase("Low Risk") || scorecardDecision.equalsIgnoreCase("Low Risk-II") || scorecardDecision.equalsIgnoreCase("Normal Risk")
                                            || scorecardDecision.equalsIgnoreCase("Moderate Risk")) {

                                    } else {
                                        Log.consoleLog(ifr, "scoreCard Failed:::");
                                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                                        message.put("eflag", "false");
                                        return message.toString();
                                    }

                                } else {
                                    Log.consoleLog(ifr, "eligibility Failed:::");
                                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                                    message.put("eflag", "false");
                                    return message.toString();
                                }
                            } else {
                                Log.consoleLog(ifr, "Knockoffeligibility Failed:::");
                                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                                message.put("eflag", "false");
                                return message.toString();
                            }
                        } else {
                            Log.consoleLog(ifr, "Experian Failed:::");
                            message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                            message.put("eflag", "false");
                            return message.toString();
                        }

                    } else {
                        Log.consoleLog(ifr, "Cibil Failed:::");
                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                        message.put("eflag", "false");
                        return message.toString();
                    }
                } else {
                    Log.consoleLog(ifr, "customer mis status Failed:::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else {
                Log.consoleLog(ifr, "Co-applicant mobile is not same as applicant mobile number");
                message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Applicant and Co-applicant CustomerId and Mobile Number should not be same."));
                message.put("eflag", "false");
                return message.toString();
            }

            //Added by Ahmed on 17-07-2024
            if (ConfProperty.getCommonPropertyValue("ENABLEMINISTATEMENT").equalsIgnoreCase("Y")) {

                String Accountumber = ifr.getValue("P_CB_OD_SalaryAccount").toString();
                Log.consoleLog(ifr, "AccountMiniStatement::Accountumber::" + Accountumber);
                //int Minusyear = 1;
                int Minusyear = Integer.parseInt(ConfProperty.getCommonPropertyValue("fromDateFormat"));
                Log.consoleLog(ifr, "AccountMiniStatement::Minusyear::" + Minusyear);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");//2023/02/06
                LocalDate today = LocalDate.now();
                LocalDate oneYearAgo = today.minusYears(Minusyear);

                String toDate = today.format(formatter);
                String fromDate = oneYearAgo.format(formatter);
                Log.consoleLog(ifr, "AccountMiniStatement::from date ::" + fromDate);
                Log.consoleLog(ifr, "AccountMiniStatement::To date ::" + toDate);
                AccountMiniStatement ams = new AccountMiniStatement();
                String responseAcc = ams.getAccountMiniStatementDetails(ifr, "Budget");//product type,acc no,from date,to date
                Log.consoleLog(ifr, "AccountMiniStatement responseAcc ::" + responseAcc);

                if (responseAcc.contains(RLOS_Constants.ERROR)) {
                    Log.consoleLog(ifr, "AccountMiniStatement::Error in AccountMiniStatement");
                    return pcm.returnErrorcustmessage(ifr, responseAcc);
                }

            }

        } catch (NumberFormatException | ParseException e) {
            Log.consoleLog(ifr, "Exception mImpOnClickOccupationDetails : " + e);
            Log.errorLog(ifr, "Exception mImpOnClickOccupationDetails : " + e);
            return pcm.returnError(ifr);
        } catch (Exception ex) {
            Logger.getLogger(BudgetPortalCustomCode.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    /*
        checks if CIBIL/Experian is already called,
        if yes- sends the count of how many times called.
        if No - calls the getExperianCIBILScore API ,which inserts data to LOS_can_ibps
    
     */
    public String mCallBureau(IFormReference ifr, String BureauType, String aadharNo, String applicantType) throws ParseException {
        Log.consoleLog(ifr, "Inside mCallBureau:");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery;
        if (BureauType.equalsIgnoreCase("CB")) {
            //CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
            CountQuery = ConfProperty.getQueryScript("getCibilCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType);
        } else {
            //CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='EX'";
            CountQuery = ConfProperty.getQueryScript("getExperianCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType);
        }

        List< List< String>> Result = ifr.getDataFromDB(CountQuery);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        String Count = "";
        if (Result.size() > 0) {
            Count = Result.get(0).get(0);
        }

        if (Integer.parseInt(Count) > 0) {
            return Count;
        }
        //added by ishwarya on 14022024
        String minLoanmaount = pcm.mGetMinLoanAmount(ifr);
        
        Log.consoleLog(ifr, "minLoanamount:" + minLoanmaount);
        //Code Logic Modified for Experian
        if (BureauType.equalsIgnoreCase("EX")) {
            ExperianAPI EXP = new ExperianAPI();
//           // String BureauScore = EXP.getExperianCIBILScore(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount, applicantType);
//            Log.consoleLog(ifr, "BureauScore From Experian===>" + BureauScore);
//            if (BureauScore.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            }
        } else {
            ConsumerAPI CB1 = new ConsumerAPI();
            String BureauScore = CB1.getConsumerCIBILScore(ifr, "CB", minLoanmaount, aadharNo, applicantType);
            Log.consoleLog(ifr, "BureauScore From Transunion==>" + BureauScore);
            if (BureauScore.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
        }

        return "";
    }
//Added by monesh for taking mini loan amount 

    public String mCallBureauLP(IFormReference ifr, String BureauType, String aadharNo, String applicantType, String loanamount) throws ParseException {
        Log.consoleLog(ifr, "Inside mCallBureau:");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery;
        if (BureauType.equalsIgnoreCase("CB")) {
            //CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
            CountQuery = ConfProperty.getQueryScript("getCibilCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType);
        } else {
            //CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='EX'";
            CountQuery = ConfProperty.getQueryScript("getExperianCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType);
        }

        List< List< String>> Result = ifr.getDataFromDB(CountQuery);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        String Count = "";
        if (Result.size() > 0) {
            Count = Result.get(0).get(0);
        }

        if (Integer.parseInt(Count) > 0) {
            return Count;
        }
        //added by ishwarya on 14022024
        String minLoanmaount = loanamount;
        //pcm.mGetMinLoanAmount(ifr);
        Log.consoleLog(ifr, "minLoanamount:" + minLoanmaount);
        //Code Logic Modified for Experian
        if (BureauType.equalsIgnoreCase("EX")) {
            ExperianAPI EXP = new ExperianAPI();
//            String BureauScore = EXP.getExperianCIBILScore(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount, applicantType);
//            Log.consoleLog(ifr, "BureauScore From Experian===>" + BureauScore);
//            if (BureauScore.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            }
        } else {
            ConsumerAPI CB1 = new ConsumerAPI();
            String BureauScore = CB1.getConsumerCIBILScore(ifr, "CB", minLoanmaount, aadharNo, applicantType);
            Log.consoleLog(ifr, "BureauScore From Transunion==>" + BureauScore);
            if (BureauScore.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
        }

        return "";
    }

    public String checkCustMisstatus(IFormReference ifr, String RuleName, String values, String ValueTag) {
        JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
        Log.consoleLog(ifr, "BRMS Result:" + result);
        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String misstatusOutput = cf.getJsonValue(result, ValueTag);
            Log.consoleLog(ifr, misstatusOutput);
            return misstatusOutput;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    //modified by ishwarya on 03-07-2024
    public String checkKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
        Log.consoleLog(ifr, "Entered checkKnockOff:::: for rule " + RuleName);
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("total_knockoff_cb_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);

        try {
            Log.consoleLog(ifr, "inside checkKnockOff:::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId:::" + ProcessInstanceId);

            String[] brmsKeys = {
                "kyc_valid_op", "cb_writeoffhist_op", "cb_npavalfromapi_op",
                "cb_exis_loan_op", "staffchk_op", "accchk_op",
                "salaryacc_op", "individualcheck_op", "agecheck_op", "sma2_op"
            };

            String[] ruleNames = {
                "KYC VALIDATION", "WRITEOFF HISTORY", "NPA CHECK",
                "EXISTING LOAN", "STAFF ELIGIBILITY", "ACCOUNT ELIGIBILITY",
                "SALARY ACCOUNT ELIGIBILITY", "INDIVIDUAL CHECK", "AGE CHECK", "SMA CHECK"
            };

            List<String> brmsOutputs = new ArrayList<>();
            for (String key : brmsKeys) {
                String value = (String) objm.get(key);
                if (value != null && value.contains("#" + key + "#")) {
                    value = "NA";
                }
                brmsOutputs.add(value != null ? value : "NA");
                Log.consoleLog(ifr, key + ":::" + value);
            }

            Log.consoleLog(ifr, "brmsOutputs:::" + brmsOutputs);
            List<List<String>> resultData = new ArrayList<>();
            List<List<String>> checkqueryData = new ArrayList<>();
            if (activityName.equalsIgnoreCase("Lead Capture")) {
                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
                        + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
                        + "WHERE c.PID = '" + ProcessInstanceId + "' "
                        + "and (c.ApplicantType='CB' or c.ApplicantType='B')";
                Log.consoleLog(ifr, "Query1 data::" + Query1);
                resultData = ifr.getDataFromDB(Query1);
                Log.consoleLog(ifr, resultData.toString() + "resultData::");
                if (!resultData.isEmpty()) {
                    JSONArray arr = new JSONArray();

                    // Track if borrower and co-borrower rows have been added
                    for (List<String> rowData : resultData) {
                        String brdata = rowData.get(0);
                        JSONObject re = new JSONObject();
                        JSONArray childJsonArray = new JSONArray();

                        re.put("QNL_K_KNOCKOFFRULES_PartyType", brdata);
                        re.put("QNL_K_KNOCKOFFRULES_RuleName", "Knock-off Rules");
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
                    Log.consoleLog(ifr, "Knockoff grid json array::" + arr);
                    ifr.addDataToGrid("ALV_KnockOffRules", arr);
                }
            } else {
                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
                        + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
                        + "WHERE c.PID = '" + ProcessInstanceId + "' "
                        + "and (c.ApplicantType='CB')";
                Log.consoleLog(ifr, "Query1 data::" + Query1);
                resultData = cf.mExecuteQuery(ifr, Query1, "   query fired");
                Log.consoleLog(ifr, resultData.toString() + "resultData::");

                String browertype = resultData.get(0).get(0);

                String checkquery = "SELECT * FROM LOS_NL_K_KNOCKOFFRULES WHERE PID='" + ProcessInstanceId + "'   AND  PARTYTYPE like '" + browertype + "%' ";
                checkqueryData = cf.mExecuteQuery(ifr, checkquery, "Check   query fired");
                // ifr.getDataFromDB(checkquery);
                Log.consoleLog(ifr, checkquery.toString() + "resultData::");
                if (checkqueryData.isEmpty()) {
                    if (!resultData.isEmpty()) {
                        JSONArray arr = new JSONArray();

                        // Track if borrower and co-borrower rows have been added
                        for (List<String> rowData : resultData) {
                            String brdata = rowData.get(0);
                            JSONObject re = new JSONObject();
                            JSONArray childJsonArray = new JSONArray();

                            re.put("QNL_K_KNOCKOFFRULES_PartyType", brdata);
                            re.put("QNL_K_KNOCKOFFRULES_RuleName", "Knock-off Rules");
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
                        Log.consoleLog(ifr, "Knockoff grid json array::" + arr);
                        ifr.addDataToGrid("ALV_KnockOffRules", arr);
                    }

                }
            }

        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception checkScoreCard : " + ex);
            Log.errorLog(ifr, "Exception checkScoreCard : " + ex);
        }

        Log.consoleLog(ifr, "totalGrade RETURN" + totalGrade);
        return totalGrade;
    }

    public String checkBRMSKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
        Log.consoleLog(ifr, "Entered checkBRMSKnockOff for rule:" + RuleName);
        JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
        Log.consoleLog(ifr, "BRMS Result:" + result);
        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String cibilOutput = cf.getJsonValue(result, ValueTag);
            Log.consoleLog(ifr, cibilOutput);
            return cibilOutput;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    public String checkFinalEligibility(IFormReference ifr, String RuleName, String values, String ValueTag) {

        JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
        Log.consoleLog(ifr, "BRMS Result:" + result);
        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String validityCheck = cf.getJsonValue(result, ValueTag);
            Log.consoleLog(ifr, validityCheck);
            return validityCheck;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    public String mGetAPIData(IFormReference ifr) throws ParseException {
        Log.consoleLog(ifr, "Entered mGetAPIData:::::::");
        String CustomerId = pcm.getCustomerIDCB(ifr, "B");
        String Age = "", Dob = "", CustMisStatus = "", Years = "", DateOfCustOpen = "";
        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Demographic API = new Demographic();
        String GetDemoGraphicData = API.getDemographic(ifr, ProcessInstanceId, CustomerId);
        Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
        String writeOffPresent;
        if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
            writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
            Age = cf.getJsonValue(obj, "Age");
            Years = cf.getJsonValue(obj, "Years");
            Dob = cf.getJsonValue(obj, "DOB");
            CustMisStatus = cf.getJsonValue(obj, "CustMisStatus");
            DateOfCustOpen = cf.getJsonValue(obj, "DateOfCustOpen");
        }

        Advanced360EnquiryDatav2 objAdv360 = new Advanced360EnquiryDatav2();
        String response = objAdv360.executeCBSAdvanced360Inquiryv2(ifr, ProcessInstanceId, CustomerId, "Budget", "", "");
//Commented by Ahmed for using v2 instead of v1 in code for budget on 28-06-2024
//        Advanced360EnquiryData API360 = new Advanced360EnquiryData();
//        String response = API360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "Budjet");
//        
        Log.consoleLog(ifr, "response==>" + response);
        String canaraBudgetProductCode = "";
        String PAPLExist;
        String count = "";
        String Classification;

        String salExists = "";
        String smaExists = "";

        if (response.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject) jsonparser.parse(response);
            canaraBudgetProductCode = cf.getJsonValue(obj, "ProductCode");
            PAPLExist = cf.getJsonValue(obj, "PAPLExist");
            Classification = cf.getJsonValue(obj, "Classification");
            count = cf.getJsonValue(obj, "count");
            salExists = cf.getJsonValue(obj, "salExists");
            smaExists = cf.getJsonValue(obj, "smaExists");
        }
        JSONObject obj = new JSONObject();
        obj.put("writeOffPresent", writeOffPresent);
        obj.put("Age", Age);
        obj.put("Years", Years);
        obj.put("DOB", Dob);
        obj.put("count", count);
        obj.put("productCode", canaraBudgetProductCode);
        obj.put("CustMisStatus", CustMisStatus);
        obj.put("PAPLExist", PAPLExist);
        obj.put("Classification", Classification);
        obj.put("salExists", salExists);
        obj.put("smaExists", smaExists);
        obj.put("FlgCustType", "R");//Need to check with Monesh how it was taken

        return obj.toString();
    }

    public String checkScoreCard(IFormReference ifr, String RuleName, String values, String ValueTag, String applicantType) throws ParseException {

        Log.consoleLog(ifr, "checkScoreCard");
        HashMap<String, Object> objm = new HashMap();
        objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);
        String partyType = "";
        JSONParser parser = new JSONParser();
        String activityName = ifr.getActivityName();
        String totalGrade = "";
        String totalgrade_out = "";

        try {
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String Scoringband_out = objm.get("scoringband_out").toString();
            totalgrade_out = objm.get("totalgrade_out").toString();
//            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                Log.consoleLog(ifr, "Get status value ==>" + cf.getJsonValue(result, "status"));
//                totalGrade = cf.getJsonValue(result, ValueTag);
//                Log.consoleLog(ifr, "totalGrade==>" + totalGrade);
//                String CB_SCORECARD_Response = "";
//                //   String GetBRMSQuery = "SELECT RESPONSE FROM LOS_HIS_BRMSRULES WHERE "
//                //         + "RULE_NAME ='" + RuleName + "' AND "
//                //       + "PROCESSINSTANCEID = '" + ProcessInstanceId + "' ORDER BY ROWID DESC";
//                String GetBRMSQuery = ConfProperty.getQueryScript("GetBRMSResponse").replaceAll("#RuleName#", RuleName).replaceAll("#PID#", ProcessInstanceId);
//                List<List<String>> CB_SCORECARD = cf.mExecuteQuery(ifr, GetBRMSQuery, "Execute query for fetching CB_SCORECARD Response ");
//                if (!CB_SCORECARD.isEmpty()) {
//                    CB_SCORECARD_Response = CB_SCORECARD.get(0).get(0);
//                    Log.consoleLog(ifr, "CB_SCORECARD_Response " + CB_SCORECARD_Response);
//                } else {
//                    Log.consoleLog(ifr, "scoreCard Failed:::");
//                    JSONObject message = new JSONObject();
//                    message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "error", "Technical glitch. Error in getting BRMS Response"));
//                    message.put("", "false");
//                    return message.toString();
//                }
//                //  String ScoringBand = cf.getJsonValue(result, "scoringband");
//                //JSONObject ScoringBandJSON = (JSONObject) parser.parse(ScoringBand);
//
//                //  String Scoringband_out = cf.getJsonValue(ScoringBandJSON, "scoringband_out");
//                //String Scoringband = cf.getJsonValue(ScoringBandJSON, "scoringband_out");
//                JSONObject SCORECARDJSON = (JSONObject) parser.parse(CB_SCORECARD_Response);
//                JSONObject ScorecardResponse = new JSONObject(SCORECARDJSON);
//                Log.consoleLog(ifr, "ScorecardResponse ==>" + ScorecardResponse);
//                String OutputStr = ScorecardResponse.get("Output").toString();
//                JSONObject OutputObj = (JSONObject) parser.parse(OutputStr);
//                Log.consoleLog(ifr, "OutputObj==>" + OutputObj.toString());
//
//                String ScoringBand = OutputObj.get("scoringband").toString();
//                Log.consoleLog(ifr, "ScoringBand==>" + ScoringBand);
            //JSONObject ScoringBandJSON = (JSONObject) parser.parse(ScoringBand);
//                Log.consoleLog(ifr, "ScoringBandJSON==>" + ScoringBandJSON.toString());
//                String Scoringband_out = ScoringBandJSON.get("scoringband_out").toString();
//                Log.consoleLog(ifr, "Scoringband_out==>" + Scoringband_out);
//                String totalgradeOut = OutputObj.get("totalgrade").toString();
//                JSONObject totalgradeOutJSON = (JSONObject) parser.parse(totalgradeOut);
//                Log.consoleLog(ifr, "totalgradeOutJSON :: " + totalgradeOutJSON.toString());
//                String totalgrade_out = totalgradeOutJSON.get("totalgrade_out").toString();
            //Log.consoleLog(ifr, "totalgrade_out:: " + totalgrade_out);

            Log.consoleLog(ifr, "activityName  :" + activityName);
            if (activityName.equalsIgnoreCase("Portal")) {

                String recoverymechanism_out = objm.get("recoverymechanism_out").toString();
                String anninc_out = objm.get("anninc_out").toString();
                String satbanking_out = objm.get("satbanking_out").toString();
                String natsec_out = objm.get("natsec_out").toString();
                String overdays_out = objm.get("overdays_out").toString();
                String emp_out = objm.get("emp_out").toString();
                String settledhist_out = objm.get("settledhist_out").toString();
                String nethomeinc_out = objm.get("nethomeinc_out").toString();
                String busitenure_out = objm.get("busitenure_out").toString();
                String cibil_out = objm.get("cibil_out").toString();
                String residence_out = objm.get("residence_out").toString();

                if (applicantType.equalsIgnoreCase("B")) {
                    // String queryData = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b \n"
                    // + "inner JOIN LOS_NL_BASIC_INFO c  ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + PID + "' AND  c.ApplicantType ='B'";
                    String queryData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

                    List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                    partyType = data.get(0).get(0);
                    Log.consoleLog(ifr, "Party Type==>" + partyType);
                }
                if (applicantType.equalsIgnoreCase("CB")) {
                    String queryData = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b \n"
                            + "inner JOIN LOS_NL_BASIC_INFO c  ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + ProcessInstanceId + "' AND  c.ApplicantType ='CB'";
                    //   String queryData = ConfProperty.getQueryScript("CoBorrowerNameQueryCB").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

                    List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                    partyType = data.get(0).get(0);
                    Log.consoleLog(ifr, "Party Type==>" + partyType);
                }

                JSONObject JsonObject = new JSONObject();
                JSONArray JsonArray = new JSONArray();
                JSONObject ChildJsonObj1 = new JSONObject();
                JSONObject ChildJsonObj2 = new JSONObject();
                JSONObject ChildJsonObj3 = new JSONObject();
                JSONObject ChildJsonObj4 = new JSONObject();
                JSONObject ChildJsonObj5 = new JSONObject();
                JSONObject ChildJsonObj6 = new JSONObject();
                JSONObject ChildJsonObj7 = new JSONObject();
                JSONObject ChildJsonObj8 = new JSONObject();
                JSONObject ChildJsonObj9 = new JSONObject();
                JSONObject ChildJsonObj10 = new JSONObject();
                JSONObject ChildJsonObj11 = new JSONObject();
                JSONArray ChildJsonArray = new JSONArray();

                ChildJsonObj1.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "CIC SCORE");
                ChildJsonObj1.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "50");
                ChildJsonObj1.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", cibil_out);
                ChildJsonObj2.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "DAYS PAST DUE");
                ChildJsonObj2.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "40");
                ChildJsonObj2.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", overdays_out);
                ChildJsonObj3.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "HISTORY OF SETTLED /NPA/ WRITE OFF ACCOUNT");
                ChildJsonObj3.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "40");
                ChildJsonObj3.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", settledhist_out);
                ChildJsonObj4.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "PERCENTAGE OF NET TAKE HOME INCOME TO GROSS SALARY");
                ChildJsonObj4.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "20");
                ChildJsonObj4.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", nethomeinc_out);
                ChildJsonObj5.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "EMPLOYMENT/PROFESSION/PENSIONER");
                ChildJsonObj5.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "15");
                ChildJsonObj5.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", emp_out);
                ChildJsonObj6.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "GROSS MONTHLY INCOME FROM SALARY");
                ChildJsonObj6.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "10");
                ChildJsonObj6.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", anninc_out);
                ChildJsonObj7.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "HOW LONG WITH THE PRESENT EMPLOYER / IN THE BUSINESS");
                ChildJsonObj7.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "5");
                ChildJsonObj7.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", busitenure_out);
                ChildJsonObj8.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "NO OF YEARS OF SATISFACTORY BANKING");
                ChildJsonObj8.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "5");
                ChildJsonObj8.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", satbanking_out);
                ChildJsonObj9.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "NATURE OF SECURITY (BOTH PRIME & COLLATERAL)");
                ChildJsonObj9.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "5");
                ChildJsonObj9.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", natsec_out);
                ChildJsonObj10.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "RESIDENCE");
                ChildJsonObj10.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "5");
                ChildJsonObj10.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", residence_out);
                ChildJsonObj11.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", "RECOVERY MECHANISM");
                ChildJsonObj11.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", "5");
                ChildJsonObj11.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", recoverymechanism_out);
                double recoverymechanism_outs = Double.parseDouble(recoverymechanism_out);
                double residence_outs = Double.parseDouble(residence_out);
                double natsec_outs = Double.parseDouble(natsec_out);
                double satbanking_outs = Double.parseDouble(satbanking_out);
                double busitenure_outs = Double.parseDouble(busitenure_out);
                double anninc_outs = Double.parseDouble(anninc_out);
                double emp_outs = Double.parseDouble(emp_out);
                double nethomeinc_outs = Double.parseDouble(nethomeinc_out);
                double settledhist_outs = Double.parseDouble(settledhist_out);
                double overdays_outs = Double.parseDouble(overdays_out);
                double cibil_outs = Double.parseDouble(cibil_out);
                double TotalMarkesSecured = recoverymechanism_outs + residence_outs + natsec_outs + satbanking_outs + busitenure_outs + anninc_outs + emp_outs + nethomeinc_outs + settledhist_outs + overdays_outs + cibil_outs;

                double TotalPercentage = (TotalMarkesSecured / 200) * 100;
                ChildJsonArray.add(ChildJsonObj1);
                ChildJsonArray.add(ChildJsonObj2);
                ChildJsonArray.add(ChildJsonObj3);
                ChildJsonArray.add(ChildJsonObj4);
                ChildJsonArray.add(ChildJsonObj5);
                ChildJsonArray.add(ChildJsonObj6);
                ChildJsonArray.add(ChildJsonObj7);
                ChildJsonArray.add(ChildJsonObj8);
                ChildJsonArray.add(ChildJsonObj9);
                ChildJsonArray.add(ChildJsonObj10);
                ChildJsonArray.add(ChildJsonObj11);
                Log.consoleLog(ifr, "jsonArray For each parameters ::" + ChildJsonArray);

                JsonObject.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS", ChildJsonArray);

                JsonObject.put("QNL_R_SCORERATING-PartyType", partyType);
                JsonObject.put("QNL_R_SCORERATING-CanaraRetailGrade", Scoringband_out);
                JsonObject.put("QNL_R_SCORERATING-RiskScore_Rating", totalgrade_out);
                JsonObject.put("QNL_R_SCORERATING-TotalMarkesSecured", String.valueOf(TotalMarkesSecured));
                JsonObject.put("QNL_R_SCORERATING-InPercentage", String.valueOf(TotalPercentage));
                JsonObject.put("QNL_R_SCORERATING-ApplicantCodeType", applicantType);
                Log.consoleLog(ifr, "JsonObject ::" + JsonObject);

                JsonArray.add(JsonObject);
                Log.consoleLog(ifr, "jsonArray Final Score Rating ::" + JsonArray);
                //((IFormAPIHandler) ifr).addDataToGrid("QNL_R_SCORERATING", JsonArray, true);
                String APPLICANTCODETYPEQuery = "select APPLICANTCODETYPE from LOS_NL_R_SCORERATING where PID='" + ProcessInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                List<List<String>> APPLICANTCODETYPE = cf.mExecuteQuery(ifr, APPLICANTCODETYPEQuery, "APPLICANTCODETYPEQuery ");
                if (APPLICANTCODETYPE.size() == 0) {
                    Log.consoleLog(ifr, "APPLICANTCODETYPE ::" + APPLICANTCODETYPE);
                    ((IFormAPIHandler) ifr).addDataToGrid("QNL_R_SCORERATING", JsonArray, true);

                } else {
                    Log.consoleLog(ifr, "Inside Else APPLICANTCODETYPE ::");
                    String fkey = "select F_KEY from LOS_NL_R_SCORERATING where PID='" + ProcessInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                    List<List<String>> FKEY = cf.mExecuteQuery(ifr, fkey, "fkey");
                    String deleteScoreCardParametersQuery = "delete from LOS_NL_SCORERATING_PARAMETERS where  F_KEY='" + FKEY + "'";
                    List<List<String>> deleteScoreCardParameters = cf.mExecuteQuery(ifr, deleteScoreCardParametersQuery, "deleteScoreCardParametersQuery");

                    String deleteScoreCardEntryQuery = "delete from LOS_NL_R_SCORERATING where PID='" + ProcessInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                    List<List<String>> deleteScoreCardEntry = cf.mExecuteQuery(ifr, deleteScoreCardEntryQuery, "deleteScoreCardEntryQuery");

                    ((IFormAPIHandler) ifr).addDataToGrid("QNL_R_SCORERATING", JsonArray, true);

                }
//                //String strInsertqueryforCRG = Insert into LOS_L_RISK_RATING (PID,InsertionOrderID,RISKSCORE,RISK_RANK)values ('#PID#',S_LOS_NL_BASIC_INFO.nextval,'#Scoringband#','#totalgrade#');
//                String ScorcardCountQuery = ConfProperty.getQueryScript("ScorcardCountQuery").replaceAll("#PID#", ProcessInstanceId);
//                //String ScorcardCountQuery = "select PID from LOS_L_RISK_RATING where PID='" + ProcessInstanceId + "'";
//                //ScorcardCountQuery = select PID from LOS_L_RISK_RATING where PID='#PID#'
//                List<List<String>> ScorcardCount = cf.mExecuteQuery(ifr, ScorcardCountQuery, "ScorcardCountQuery ");
//                Log.consoleLog(ifr, "ScorcardCount  :" + ScorcardCount.size());
//                if (ScorcardCount.size() == 0) {
//                    String strInsertqueryforCRG = ConfProperty.getQueryScript("PortalScorecardInsertQuery").replaceAll("#PID#", ProcessInstanceId).replaceAll("#Scoringband#", Scoringband_out).replaceAll("#totalgrade#", totalgrade_out);
//                    Log.consoleLog(ifr, "strInsertqueryforCRG  :" + strInsertqueryforCRG);
//                    ifr.saveDataInDB(strInsertqueryforCRG);
//                }
            } else {
                String[] keys = {"recoverymechanism_out", "anninc_out", "satbanking_out", "anninc_out", "overdays_out", "emp_out", "settledhist_out", "nethomeinc_out", "busitenure_out", "cibil_out", "residence_out"};

                String[] ruleNames = {"RECOVERY MECHANISM", "GROSS MONTHLY INCOME FROM SALARY", "NO OF YEARS OF SATISFACTORY BANKING", "NATURE OF SECURITY (BOTH PRIME & COLLATERAL)", "DAYS PAST DUE", "EMPLOYMENT/PROFESSION/PENSIONER", "HISTORY OF SETTLED /NPA/ WRITE OFF ACCOUNT", "PERCENTAGE OF NET TAKE HOME INCOME TO GROSS SALARY", "HOW LONG WITH THE PRESENT EMPLOYER / IN THE BUSINESS", "CIC SCORE", "RESIDENCE"};
                String[] maxMarks = {"5", "10", "5", "5", "40", "15", "40", "20", "5", "50", "5"};

                double totalMarksSecured = 0.0;
                JSONArray childJsonArray = new JSONArray();
                Log.consoleLog(ifr, "inside else ALV_SCORERATING");
                int ALV_SCORERATING = ifr.getDataFromGrid("ALV_SCORERATING").size();
                Log.consoleLog(ifr, "ALV_SCORERATING==>" + ALV_SCORERATING);
                if (ALV_SCORERATING == 0) {
                    Log.consoleLog(ifr, "inside else condition " + ALV_SCORERATING);
                    for (int i = 0; i < keys.length; i++) {
                        Log.consoleLog(ifr, "inside for loop condition " + keys[i]);
                        String value = objm.get(keys[i]).toString();

                        totalMarksSecured += Double.parseDouble(value);
                        JSONObject childJsonObj = new JSONObject();
                        childJsonObj.put("Parameter", ruleNames[i]);
                        childJsonObj.put("Maximum Marks", maxMarks[i]);
                        childJsonObj.put("Marks Obtained", value);
                        childJsonArray.add(childJsonObj);

                    }
                    double totalPercentage = (totalMarksSecured / 200) * 100;
                    Log.consoleLog(ifr, "TotalMarksSecured: " + totalMarksSecured + " TotalPercentage: " + totalPercentage);

                    String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "ProcessInstanceId:::" + processInstanceId);

                    String queryData = "B".equalsIgnoreCase(applicantType) ? ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", processInstanceId)
                            : "SELECT concat(b.borrowertype, concat('-', c.fullname)), c.insertionOrderId FROM LOS_MASTER_BORROWER b INNER JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + processInstanceId + "' AND  c.ApplicantType ='CB'";

                    List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                    String PartyType = data.get(0).get(0);
                    Log.consoleLog(ifr, "Party Type==>" + PartyType);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ANL_SCORERATING_PARAMETERS", childJsonArray);
                    jsonObject.put("QNL_R_SCORERATING_PartyType", PartyType);
                    jsonObject.put("QNL_R_SCORERATING_CanaraRetailGrade", Scoringband_out);
                    jsonObject.put("QNL_R_SCORERATING_RiskScore_Rating", totalgrade_out);
                    jsonObject.put("QNL_R_SCORERATING_TotalMarkesSecured", String.valueOf(totalMarksSecured));
                    jsonObject.put("QNL_R_SCORERATING_InPercentage", String.valueOf(totalPercentage));
                    jsonObject.put("QNL_R_SCORERATING_ApplicantCodeType", applicantType);
                    Log.consoleLog(ifr, "JsonObject ::" + jsonObject);

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(jsonObject);
                    Log.consoleLog(ifr, "jsonArray Final Score Rating ::" + jsonArray);

                    String applicantCodeTypeQuery = "select APPLICANTCODETYPE from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                    List<List<String>> applicantCodeType = cf.mExecuteQuery(ifr, applicantCodeTypeQuery, "applicantCodeTypeQuery");

                    if (applicantCodeType.isEmpty()) {
                        Log.consoleLog(ifr, "inside if applicantCodeType is empty");
                        ifr.addDataToGrid("ALV_SCORERATING", jsonArray, true);
                    } else {
                        Log.consoleLog(ifr, "inside else of applicantCodeType is empty");
                        String fkey = cf.mExecuteQuery(ifr, "select F_KEY from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "fkey").get(0).get(0);
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_SCORERATING_PARAMETERS where F_KEY='" + fkey + "'", "deleteScoreCardParametersQuery");
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "deleteScoreCardEntryQuery");
                        ifr.addDataToGrid("ALV_SCORERATING", jsonArray, true);
                    }
                }
                Log.consoleLog(ifr, "end of else ALV_SCORERATING");
            }

            Log.consoleLog(ifr, "activityName  :" + activityName);
            if (activityName.equalsIgnoreCase("Branch Maker")) {
                ifr.setValue("QL_RISK_RATING_RiskScore", Scoringband_out);
                ifr.setValue("QL_RISK_RATING_Rank", totalgrade_out);
                ifr.setStyle("QL_RISK_RATING_RiskScore", "disable", "true");
                ifr.setStyle("QL_RISK_RATING_Rank", "disable", "true");
                ifr.setStyle("BTN_FetchScoreRating", "disable", "true");

                Log.consoleLog(ifr, "Score rating populated :");
            }
//            } else {
//                Log.consoleLog(ifr, "Get status value error ==>" + cf.getJsonValue(result, "status"));
//                Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
//                return "ERROR";
            //}
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception checkScoreCard : " + ex);
            Log.errorLog(ifr, "Exception checkScoreCard : " + ex);

        }

        Log.consoleLog(ifr, "totalGrade RETURN" + totalgrade_out);
        return totalgrade_out;
    }

    /*
    * added by Nikhil - for calculating scoreCard
    * need to update the above method to this.
     */
    public String checkScoreCardBudget(IFormReference ifr, String RuleName, String values, String ValueTag, String applicantType) {
        // added by ishwarya for scorecard data saving on 20/06/2024
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);
        Log.consoleLog(ifr, "activityName: " + ifr.getActivityName());

        String totalGrade = objm.get("totalgrade_out").toString();
        String scoreband_op = objm.get("scoringband_out").toString();
        Log.consoleLog(ifr, "objm totalGrade::: " + totalGrade + " scoreband_op::: " + scoreband_op);

        String[] keys = {"recoverymechanism_out", "anninc_out", "satbanking_out", "anninc_out", "overdays_out", "emp_out", "settledhist_out", "nethomeinc_out", "busitenure_out", "cibil_out", "residence_out"};

        String[] ruleNames = {"RECOVERY MECHANISM", "GROSS MONTHLY INCOME FROM SALARY", "NO OF YEARS OF SATISFACTORY BANKING", "NATURE OF SECURITY (BOTH PRIME & COLLATERAL)", "DAYS PAST DUE", "EMPLOYMENT/PROFESSION/PENSIONER", "HISTORY OF SETTLED /NPA/ WRITE OFF ACCOUNT", "PERCENTAGE OF NET TAKE HOME INCOME TO GROSS SALARY", "HOW LONG WITH THE PRESENT EMPLOYER / IN THE BUSINESS", "CIC SCORE", "RESIDENCE"};
        String[] maxMarks = {"5", "10", "5", "5", "40", "15", "40", "20", "5", "50", "5"};

        double totalMarksSecured = 0.0;
        JSONArray childJsonArray = new JSONArray();
        //modified by ishwarya
        try {
            if ("Portal".equalsIgnoreCase(ifr.getActivityName())) {
                int ALV_SCORERATING = ifr.getDataFromGrid("ALV_SCORERATING").size();
                Log.consoleLog(ifr, "ALV_SCORERATING==>" + ALV_SCORERATING);
                if (ALV_SCORERATING == 0) {
                    Log.consoleLog(ifr, "inside if condition " + ALV_SCORERATING);
                    for (int i = 0; i < keys.length; i++) {
                        Log.consoleLog(ifr, "inside for loop condition " + keys[i]);
                        String value = objm.getOrDefault(keys[i], "0").toString();
                        totalMarksSecured += Double.parseDouble(value);

                        JSONObject childJsonObj = new JSONObject();
                        childJsonObj.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-Parameter", ruleNames[i]);
                        childJsonObj.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MaximumMarks", maxMarks[i]);
                        childJsonObj.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS-MarksReceived", value);
                        childJsonArray.add(childJsonObj);
                    }

                    double totalPercentage = (totalMarksSecured / 200) * 100;
                    Log.consoleLog(ifr, "TotalMarksSecured: " + totalMarksSecured + " TotalPercentage: " + totalPercentage);

                    String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "ProcessInstanceId:::" + processInstanceId);

                    String queryData = "B".equalsIgnoreCase(applicantType) ? ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", processInstanceId)
                            : "SELECT concat(b.borrowertype, concat('-', c.fullname)), c.insertionOrderId FROM LOS_MASTER_BORROWER b INNER JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + processInstanceId + "' AND  c.ApplicantType ='CB'";

                    List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                    String partyType = data.get(0).get(0);
                    Log.consoleLog(ifr, "Party Type==>" + partyType);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("QNL_R_SCORERATING-CNL_SCORERATING_PARAMETERS", childJsonArray);
                    jsonObject.put("QNL_R_SCORERATING-PartyType", partyType);
                    jsonObject.put("QNL_R_SCORERATING-CanaraRetailGrade", scoreband_op);
                    jsonObject.put("QNL_R_SCORERATING-RiskScore_Rating", totalGrade);
                    jsonObject.put("QNL_R_SCORERATING-TotalMarkesSecured", String.valueOf(totalMarksSecured));
                    jsonObject.put("QNL_R_SCORERATING-InPercentage", String.valueOf(totalPercentage));
                    jsonObject.put("QNL_R_SCORERATING-ApplicantCodeType", applicantType);
                    Log.consoleLog(ifr, "JsonObject ::" + jsonObject);

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(jsonObject);
                    Log.consoleLog(ifr, "jsonArray Final Score Rating ::" + jsonArray);

                    String applicantCodeTypeQuery = "select APPLICANTCODETYPE from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                    List<List<String>> applicantCodeType = cf.mExecuteQuery(ifr, applicantCodeTypeQuery, "applicantCodeTypeQuery");

                    if (applicantCodeType.isEmpty()) {
                        Log.consoleLog(ifr, "inside if applicantCodeType is empty");
                        ifr.addDataToGrid("QNL_R_SCORERATING", jsonArray, true);
                    } else {
                        Log.consoleLog(ifr, "inside else of applicantCodeType is empty");
                        String fkey = cf.mExecuteQuery(ifr, "select F_KEY from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "fkey").get(0).get(0);
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_SCORERATING_PARAMETERS where F_KEY='" + fkey + "'", "deleteScoreCardParametersQuery");
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "deleteScoreCardEntryQuery");
                        ifr.addDataToGrid("QNL_R_SCORERATING", jsonArray, true);
                    }
                    Log.consoleLog(ifr, "end of if ALV_SCORERATING");
                }
                Log.consoleLog(ifr, "end of portal condition check");
            } else {
                Log.consoleLog(ifr, "inside else ALV_SCORERATING");
                int ALV_SCORERATING = ifr.getDataFromGrid("ALV_SCORERATING").size();
                Log.consoleLog(ifr, "ALV_SCORERATING==>" + ALV_SCORERATING);
                if (ALV_SCORERATING == 1) {
                    Log.consoleLog(ifr, "inside else condition " + ALV_SCORERATING);
                    for (int i = 0; i < keys.length; i++) {
                        Log.consoleLog(ifr, "inside for loop condition " + keys[i]);
                        String value = objm.get(keys[i]).toString();

                        totalMarksSecured += Double.parseDouble(value);
                        JSONObject childJsonObj = new JSONObject();
                        childJsonObj.put("Parameter", ruleNames[i]);
                        childJsonObj.put("Maximum Marks", maxMarks[i]);
                        childJsonObj.put("Marks Obtained", value);
                        childJsonArray.add(childJsonObj);

                    }
                    double totalPercentage = (totalMarksSecured / 200) * 100;
                    Log.consoleLog(ifr, "TotalMarksSecured: " + totalMarksSecured + " TotalPercentage: " + totalPercentage);

                    String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "ProcessInstanceId:::" + processInstanceId);

                    String queryData = "B".equalsIgnoreCase(applicantType) ? ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", processInstanceId)
                            : "SELECT concat(b.borrowertype, concat('-', c.fullname)), c.insertionOrderId FROM LOS_MASTER_BORROWER b INNER JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + processInstanceId + "' AND  c.ApplicantType ='CB'";

                    List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                    String partyType = data.get(0).get(0);
                    Log.consoleLog(ifr, "Party Type==>" + partyType);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ANL_SCORERATING_PARAMETERS", childJsonArray);
                    jsonObject.put("QNL_R_SCORERATING_PartyType", partyType);
                    jsonObject.put("QNL_R_SCORERATING_CanaraRetailGrade", scoreband_op);
                    jsonObject.put("QNL_R_SCORERATING_RiskScore_Rating", totalGrade);
                    jsonObject.put("QNL_R_SCORERATING_TotalMarkesSecured", String.valueOf(totalMarksSecured));
                    jsonObject.put("QNL_R_SCORERATING_InPercentage", String.valueOf(totalPercentage));
                    jsonObject.put("QNL_R_SCORERATING_ApplicantCodeType", applicantType);
                    Log.consoleLog(ifr, "JsonObject ::" + jsonObject);

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(jsonObject);
                    Log.consoleLog(ifr, "jsonArray Final Score Rating ::" + jsonArray);

                    String applicantCodeTypeQuery = "select APPLICANTCODETYPE from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'";
                    List<List<String>> applicantCodeType = cf.mExecuteQuery(ifr, applicantCodeTypeQuery, "applicantCodeTypeQuery");

                    if (applicantCodeType.isEmpty()) {
                        Log.consoleLog(ifr, "inside if applicantCodeType is empty");
                        ifr.addDataToGrid("ALV_SCORERATING", jsonArray, true);
                    } else {
                        Log.consoleLog(ifr, "inside else of applicantCodeType is empty");
                        String fkey = cf.mExecuteQuery(ifr, "select F_KEY from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "fkey").get(0).get(0);
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_SCORERATING_PARAMETERS where F_KEY='" + fkey + "'", "deleteScoreCardParametersQuery");
                        cf.mExecuteQuery(ifr, "delete from LOS_NL_R_SCORERATING where PID='" + processInstanceId + "' and APPLICANTCODETYPE='" + applicantType + "'", "deleteScoreCardEntryQuery");
                        ifr.addDataToGrid("ALV_SCORERATING", jsonArray, true);
                    }
                }
                Log.consoleLog(ifr, "end of else ALV_SCORERATING");
            }
            Log.consoleLog(ifr, "end of try block for checkScoreCard");
        } catch (NumberFormatException ex) {
            Log.consoleLog(ifr, "Exception checkScoreCard : " + ex);
            Log.errorLog(ifr, "Exception checkScoreCard : " + ex);
        }

        Log.consoleLog(ifr, "totalGrade RETURN: " + totalGrade);
        return totalGrade;
    }

    //Added By Subham  
    public String mAccClickSendOTPRecieveMoneyCB(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyCB");
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
            //  CBS_SM.ExecuteCBS_Email(ifr, currentDate, randomnum, "PAPL", "1");//Added by Ahmed Alireza on 08-01-2024
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
            ifr.setStyle("P_CB_RM_RESEND", "visible", "false");
            ifr.setStyle("P_CB_ENTEROTP", "visible", "true");
            ifr.setStyle("P_CB_RM_VALIDATE", "visible", "true");
            ifr.setStyle("Portal_L_Timer_Level", "visible", "true");
            ifr.setStyle("Portal_L_Timer", "visible", "true");
            messagereturn.put("clearOTPTypeField", "P_CB_ENTEROTP");
            messagereturn.put("retValue", "optRMCB");
            return messagereturn.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
            Log.errorLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
        }
        return "";
    }
    //Added By subham          

    public String mAccValidateOTPRecieveMoneyCB(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyCB  : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        JSONObject re = new JSONObject();
        String enterOTP = ifr.getValue("P_CB_ENTEROTP").toString();
        Log.consoleLog(ifr, "enterOTP ; " + enterOTP);
        if (enterOTP.equalsIgnoreCase("")) {
            re.put("showMessage", cf.showMessage(ifr, "P_CB_RM_VALIDATE", "error", "Kindly Enter OTP"));
            return re.toString();
        }
        String sendOTP = "";
        String mobileNumber = pcm.getCurrentWiMobileNumber(ifr);
        String query = ConfProperty.getQueryScript("POTPQuery").replaceAll("#mobileNumber#", mobileNumber);
        Log.consoleLog(ifr, "query:" + query);
        List<List<String>> OTPD = cf.mExecuteQuery(ifr, query, "Query for OTP Check:");
        if (!OTPD.isEmpty()) {
            sendOTP = OTPD.get(0).get(0);
            Log.consoleLog(ifr, "sendOTP==>" + sendOTP);
        }
        if (enterOTP.equalsIgnoreCase(sendOTP)) {
            try {
                Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");
                BudgetDisbursementScreen bds = new BudgetDisbursementScreen();

                String LoanDisbStatus = bds.CBSFinalScreenValidation(ifr, PID);
                if (LoanDisbStatus.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }

                // added by Shivam on 16-01-24
                Log.consoleLog(ifr, "before setPortalDataReceiveMoneyCB:");
                pcm.setPortalDataReceiveMoneyCB(ifr, control, event, value);
                Log.consoleLog(ifr, "after setPortalDataReceiveMoneyCB:");
                re.put("NavigationNextClick", "true");
                return re.toString();
            } catch (Exception e) {
                Log.consoleLog(ifr, "Excpetion:" + e);
                Log.errorLog(ifr, "Excpetion:" + e);
            }
        } else {
            re.put("clearOTPTypeField", "P_CB_ENTEROTP");
            re.put("showMessage", cf.showMessage(ifr, "P_CB_RM_VALIDATE", "error", "Kindly Enter Correct OTP"));
            return re.toString();
        }
        return "";
    }

    public String mImpOnClickFianlEligibility(IFormReference ifr, String control, String event, String value) {
        String LoanAmount = "";
        String Tenure = "";
        try {
            Log.consoleLog(ifr, "inside mImpOnClickFianlEligibility :: by monesh");
            pcm.setPortalDataFinalEligibiltyCB(ifr, control, event, value);
            //Modified by Ahmed for getting the values via Query instead of FormObject
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            //String Query = "select LOANAMOUNT,TENURE from los_trn_finaleligibility where WINAME='" + ProcessInstanceId + "'";
            String Query = ConfProperty.getQueryScript("getLaonamtandTenureQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> resultData = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "resultData" + resultData);
            if (!resultData.isEmpty()) {
                LoanAmount = resultData.get(0).get(0);
                Tenure = resultData.get(0).get(1);
                Log.consoleLog(ifr, "LoanAmount=========>" + LoanAmount);
                Log.consoleLog(ifr, "Tenure=============>" + Tenure);
            }
            String checkbox1 = ifr.getValue("FinalEligibility_CB_checkbox2").toString();
            String checkbox2 = ifr.getValue("FinalEligibility_CB_checkbox3").toString();
            if (checkbox1.equalsIgnoreCase("true") && (checkbox2.equalsIgnoreCase("true"))) {
                String queryupdate = ConfProperty.getQueryScript("PORTALUPDATELIDERVALUEFECB").replaceAll("#PID#", ProcessInstanceId).replaceAll("#loanAmount#", LoanAmount).replaceAll("#tenure#", Tenure);
                Log.consoleLog(ifr, "queryupdate is .." + queryupdate);
                ifr.saveDataInDB(queryupdate);
            } else {
                return pcm.returnErrorThroughExecute(ifr, "Kindly Select Both Disclaimer and Consent!");
            }
            //Ended by Ahmed for getting the values via Query instead of FormObject
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

        } catch (Exception e) {
            Log.consoleLog(ifr, "mImpOnClickFianlEligibility" + e);
            Log.errorLog(ifr, "mImpOnClickFianlEligibility" + e);
            return pcm.returnErrorHold(ifr);
        }

        return "";
    }

    public String mGenerateDoc(IFormReference ifr) {

        try {

            String Docustatus = "success";

            if (Docustatus.contains("success")) {

                Docustatus = objcm.generatedoc(ifr, "KFS", "BUDGET");

                Log.consoleLog(ifr, "mGenerateDoc output " + Docustatus);

                if (Docustatus.contains("success")) {

                    Docustatus = objcm.generatedoc(ifr, "LoanAggrement", "BUDGET");

                    Log.consoleLog(ifr, "mGenerateDoc output LoanAggrement " + Docustatus);

                    if (Docustatus.contains("success")) {

                        Docustatus = objcm.generatedoc(ifr, "SanctionLetter", "BUDGET");

                        Log.consoleLog(ifr, "mGenerateDoc output SanctionLetter " + Docustatus);

                        if (Docustatus.contains("success")) {

                            Docustatus = objcm.generatedoc(ifr, "RepaymentLetter", "BUDGET");

                            Log.consoleLog(ifr, "mGenerateDoc output RepaymentLetter " + Docustatus);

                            if (Docustatus.contains("success")) {

                                Docustatus = objcm.generatedoc(ifr, "ProcessNote", "BUDGET");

                                Log.consoleLog(ifr, "mGenerateDoc output ProcessNote " + Docustatus);

                                //added by hemanth for new documents on 02-05-2024
                                if (Docustatus.contains("success")) {

                                    Docustatus = objcm.generatedoc(ifr, "CRG_Template", "BUDGET");

                                    Log.consoleLog(ifr, "mGenerateDoc output CRG_Template " + Docustatus);

                                    if (Docustatus.contains("success")) {

                                        Docustatus = objcm.generatedoc(ifr, "NF_803", "BUDGET");

                                        Log.consoleLog(ifr, "mGenerateDoc output NF_803 " + Docustatus);

                                        if (Docustatus.contains("success")) {

                                            Docustatus = objcm.generatedoc(ifr, "NF_967", "BUDGET");

                                            Log.consoleLog(ifr, "mGenerateDoc output NF_967 " + Docustatus);

                                            if (Docustatus.contains("success")) {

                                                Docustatus = objcm.generatedoc(ifr, "NF_991", "BUDGET");

                                                Log.consoleLog(ifr, "mGenerateDoc output NF_991 " + Docustatus);

                                                if (Docustatus.contains("success")) {

                                                    Docustatus = objcm.generatedoc(ifr, "NF_1024", "BUDGET");

                                                    Log.consoleLog(ifr, "mGenerateDoc output NF_1024 " + Docustatus);

                                                    if (Docustatus.contains("success")) {

                                                        Docustatus = objcm.generatedoc(ifr, "LoanApplication", "BUDGET");

                                                        Log.consoleLog(ifr, "mGenerateDoc output LoanApplication " + Docustatus);

                                                        if (Docustatus.contains("success")) {

                                                            return RLOS_Constants.SUCCESS;

                                                        } else {

                                                            return RLOS_Constants.ERROR;

                                                        }

                                                    }

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

            } else {

                return RLOS_Constants.ERROR;

            }

        } catch (Exception e) {

            Log.consoleLog(ifr, "mGenerateDoc" + e);

            Log.errorLog(ifr, "mGenerateDoc" + e);

        }

        return RLOS_Constants.ERROR;

    }

    //added by ishwarya on 22-2-2024
    public String mImpCoObligantCheck(IFormReference ifr) {
        String knockoffDecision = "";
        try {
            Log.consoleLog(ifr, "inside mImpCoObligantCheck ::");
            String bureauConsent = "";
            int size = ifr.getDataFromGrid("ALV_BUREAU_CONSENT").size();
            if (size > 0) {
                Log.consoleLog(ifr, "Inside Bureau Consent Grid: ");
                bureauConsent = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 2);
                Log.consoleLog(ifr, "bureauConsent: " + bureauConsent);
            }
            Log.consoleLog(ifr, "bureauConsent1: " + bureauConsent);

            if (bureauConsent.equalsIgnoreCase("Accepted")) {
                String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String CustomerId = pcm.getCustomerIDCB(ifr, "CB");
                Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

                //String MobileData_Query = "select a.mobileno  from LOS_L_BASIC_INFO_I a inner join  LOS_NL_BASIC_INFO b on a.f_key=b.f_key where b.PID='" + ProcessInsanceId + "' and b.applicanttype='CB'";
                String MobileData_Query = ConfProperty.getQueryScript("getMobileNumberQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
                List list = cf.mExecuteQuery(ifr, MobileData_Query, "MobileData_Query:");
                String SMobileNo = list.toString().replace("[", "").replace("]", "");
                Log.consoleLog(ifr, "SMobileNo ==>" + SMobileNo);
                String MobileNo = SMobileNo; //.substring(2, 12); -- modified by Monesh on 02/08/2024
                Log.consoleLog(ifr, "MobileNo ==>" + MobileNo);
                //Updated by vandana on 15/07/2024
                String response = cas.getCustomerAccountParams_VL(ifr, MobileNo, "CB");
                //  cas.getCustomerAccountParams_CB(ifr, MobileNo);
                Log.consoleLog(ifr, "response/getCustomerAccountParams_CB===>" + response);

                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(response);
                String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
                String PANNUMBER = OutputJSON.get("PanNumber").toString();
                String DateofBirth = OutputJSON.get("DateofBirth").toString();
                String NRI = OutputJSON.get("NRI").toString();
                String Staff = OutputJSON.get("Staff").toString();

                //Commenetd by Ahmed on 28-06-2024 for taking dynamic values
                // String salariedacc = OutputJSON.get("productCode").toString();
//                String[] pCodeConstants = {"144", "145", "146", "148"};
//                String[] prodCode = salariedacc.split(",");
//
//                boolean containsCode = false;
//                for (String code : prodCode) {
//                    for (String constant : pCodeConstants) {
//                        if (code.contains(constant)) {
//                            containsCode = true;
//                            salariedacc = constant;
//                            break;
//                        }
//                    }
//                    if (containsCode) {
//                        break;
//                    }
//                }
                //Log.consoleLog(ifr, "salariedacc===>" + salariedacc);
                Log.consoleLog(ifr, "AADHARNUMBER==>" + AADHARNUMBER);
                Log.consoleLog(ifr, "PANNUMBER===>" + PANNUMBER);
                Log.consoleLog(ifr, "DateofBirth===>" + DateofBirth);
                Log.consoleLog(ifr, "NRI===>" + NRI);
                Log.consoleLog(ifr, "Staff===>" + Staff);

                AADHARNUMBER = AADHARNUMBER.equalsIgnoreCase("") ? "No" : "Yes";
                Log.consoleLog(ifr, "aadhar:::" + AADHARNUMBER);
                PANNUMBER = PANNUMBER.equalsIgnoreCase("") ? "No" : "Yes";
                Log.consoleLog(ifr, "pan:::" + PANNUMBER);
                NRI = NRI.equalsIgnoreCase("N") ? "No" : "Yes";
                Log.consoleLog(ifr, "nri:::" + NRI);
                Staff = Staff.equalsIgnoreCase("Y") ? "Yes" : "No";
                Log.consoleLog(ifr, "staffcheck:::" + Staff);
                String APIResponse = mGetAPIData(ifr);
                Log.consoleLog(ifr, "Entered into mGetAPIData:::");

                if (APIResponse.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                JSONParser jp = new JSONParser();
                JSONObject obj = (JSONObject) jp.parse(APIResponse);
                String WriteOffDate = cf.getJsonValue(obj, "DOB");
                String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
                writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
                String count = cf.getJsonValue(obj, "count");
                String paplExist = cf.getJsonValue(obj, "PAPLExist");
                paplExist = paplExist.equalsIgnoreCase("NA") ? "No" : "Yes";
                String classification = cf.getJsonValue(obj, "Classification");
                classification = classification.equalsIgnoreCase("NA") ? "No" : "Yes";
                String productCode = pcm.mGetProductCode(ifr);
                Log.consoleLog(ifr, "ProductCode:" + productCode);
                String existSTP = paplExist;
                String NRO = NRI;

                String Sma2Count2Months = cf.getJsonValue(obj, "smaExists");
                String FlgCustType = cf.getJsonValue(obj, "FlgCustType");
                String salariedacc = cf.getJsonValue(obj, "salExists");

                String Age = cf.getJsonValue(obj, "Age");

                Log.consoleLog(ifr, " Aadhar:: " + AADHARNUMBER + " ,Pan:: " + PANNUMBER + " ,writeOffPresent:: "
                        + writeOffPresent + " ,WriteOffDate:: " + WriteOffDate + " ,Classification:: "
                        + classification + " ,PAPLExist:: " + paplExist + " ,count:: " + count + " ,ProductCode:: "
                        + productCode + " ,ExistSTP:: " + existSTP + " ,staffcheck:: " + Staff + " ,Nri:: " + NRI
                        + " ,NRO:: " + NRO + " ,salariedacc:: " + salariedacc);

                String knockoffInParams = "CB," + AADHARNUMBER + "," + PANNUMBER + "," + WriteOffDate + "," + writeOffPresent + ","
                        + classification + "," + paplExist + "," + productCode + "," + Staff + "," + NRO + ","
                        + NRI + "," + salariedacc + "," + FlgCustType + "," + Age + "," + Sma2Count2Months;

                knockoffDecision = checkKnockOff(ifr, "knock_of_CB_Rule", knockoffInParams,
                        "total_knockoff_cb_op");
                Log.consoleLog(ifr, "mImpCoObligantCheck::::::knockoffDecision value" + knockoffDecision);
                return knockoffDecision;
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in mImpCoObligantCheck" + e);
        }
        return knockoffDecision;
    }

    public String mCheckmanatPortalFieldsCB(IFormReference ifr) {

        Log.consoleLog(ifr, " INSIDE mCheckmanatPortalFieldsCB for occupation:: ");
        String PortalField = ConfProperty.getCommonPropertyValue("PortalmanaOccupationDetailsFields");
        String portalfield[] = PortalField.split(",");

        for (int i = 0; i < portalfield.length; i++) {
            Log.consoleLog(ifr, "mCheckmanatPortalFieldsCB:Value for occupation1::" + (portalfield[i]));
            if (ifr.getValue(portalfield[i]).toString().isEmpty()) {

                Log.consoleLog(ifr, "mCheckmanatPortalFieldsCB:setValue for occupation:: " + portalfield[i]);

                return portalfield[i].replaceAll("P_CB_OD_", "");

            }
            // break;
        }
        return "success";
    }

    public void autoPupulateBueroConsentFromPortal(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            Log.consoleLog(ifr, "Inside autoPupulateBueroConsentFromPortal::");
            //String bueroConsentTableQuery = "select PID from LOS_NL_BUREAU_CONSENT where PID='" + ProcessInstanceId + "'";
            String bueroConsentTableQuery = ConfProperty.getQueryScript("getPIDfromBureauConsent").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            Log.consoleLog(ifr, "bueroTableQuery ::" + bueroConsentTableQuery);
            List<List<String>> bueroConsentTableData = ifr.getDataFromDB(bueroConsentTableQuery);
            if (bueroConsentTableData.size() == 0) {
                //String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='CB' and PID='" + ProcessInstanceId + "'";
                String borrowerQuery = ConfProperty.getQueryScript("getInsertionOrderIdforCB").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                Log.consoleLog(ifr, "borrowerQuery for consent::" + borrowerQuery);
                List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                Log.consoleLog(ifr, "borrowerQueryData for consent::" + borrowerQueryData);
                if (borrowerQueryData.size() > 0) {
                    Log.consoleLog(ifr, "appType:: " + borrowerQueryData.get(0).get(0));
                    String brdata = borrowerQueryData.get(0).get(0);
                    objcm.mInsertBureauConsent(ifr, ProcessInstanceId, borrowerQueryData.get(0).get(0));
                    //String dataSavingINConsentGridQuery = "insert into LOS_NL_BUREAU_CONSENT (PID,InsertionOrderID,PartyType,Methodology,ConsentReceived) values('" + ProcessInstanceId + "',S_LOS_NL_BUREAU_CONSENT.nextval,'" + borrowerQueryData.get(0).get(0) + "','P','Initiated')";
                    String dataSavingINConsentGridQuery = ConfProperty.getQueryScript("insertQueryfordataSavingINConsentGrid").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#brdata#", brdata);
                    Log.consoleLog(ifr, "insert dataSavingINConsentGridQuery for consent" + dataSavingINConsentGridQuery);
                    Email em = new Email();
                    String emailId = pcm.getCurrentEmailId(ifr, "Canara Budget", "CB");
                    Log.consoleLog(ifr, "autoPupulateBueroConsentFromPortal :emailId::" + emailId);
//                    em.sendEmail(ifr, ProcessInstanceId, emailId, borrowerQueryData.get(0).get(0), "", "CB", "RETAIL", "Canara Budget", "6");//Need to add template in table CAN_MST_EMAIL_HEADERS
                    ifr.saveDataInDB(dataSavingINConsentGridQuery);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in autoPupulateBueroConsentFromPortal::" + e);
            Log.errorLog(ifr, "Exception in  autoPupulateBueroConsentFromPortal::" + e);
        }
    }

    public void popluateDocumentsUploadCb(IFormReference ifr) {
//        Log.consoleLog(ifr, "inside the popluateDocumentsUploadCb");
//        try {
//            String applicantName = "";
//            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            //String docQuery = "select PID from LOS_DOX_POBDGUPLOAD where  PID='" + PID + "'";
//            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
//            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
//            int rowCount = docResultData.size();
//            Log.consoleLog(ifr, "BudgetPortalCustomCode:popluateDocumentsUploadCb->rowCount::" + rowCount);
//            if (rowCount == 0) {
//                //String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b  \n"
//                // + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID = '" + PID + "' and c.ApplicantType='B'";
//                String Query1 = ConfProperty.getQueryScript("getBorrowertypewithName").replaceAll("#PID#", PID);
//                Log.consoleLog(ifr, Query1);
//                List<List<String>> resultData = ifr.getDataFromDB(Query1);
//                if (!resultData.isEmpty()) {
//                    applicantName = resultData.get(0).get(0);
//                    Log.consoleLog(ifr, "applicantName is " + applicantName);
//                }
//                //String query = "Select a.DocumentName,a.Mandatory from los_m_document a inner join LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='S22'";
//                String query = ConfProperty.getQueryScript("getDocNameQuery");
//                List<List<String>> result = ifr.getDataFromDB(query);
//                Log.consoleLog(ifr, "result is.." + result);
//                int resultsize = result.size();
//                Log.consoleLog(ifr, "size is " + resultsize);
//                if (!result.isEmpty()) {
//                    JSONArray arr = new JSONArray();
//                    for (int i = 0; i < result.size(); i++) {
//                        JSONObject re = new JSONObject();
//                        re.put("Document Type ", result.get(i).get(0));
//                        re.put("Mandatory", result.get(i).get(1));
//                        re.put("Applicant Type", applicantName);
//                        arr.add(re);
//                    }
//                    Log.consoleLog(ifr, "Document grid  json array::" + arr);
//                    ifr.addDataToGrid("CB_UPLOAD_DOCUMENT", arr);
//                }
//
//            }
//            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "1", true);
//            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "2", true);
//            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "4", true);
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Error occured inside the popluateDocumentsUploadCb" + e);
//        }
        try {

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            //String docQuery = "select PID from LOS_DOX_POBDGUPLOAD where  PID='" + PID + "'";
            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
            String cbquery = "select F_KEY from los_nl_basic_info where PID='" + PID + "' and APPLICANTTYPE='CB'";
            List<List<String>> docResultDataCB = ifr.getDataFromDB(cbquery);
            Log.consoleLog(ifr, "CB::" + docResultDataCB);
            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
            int rowCount = docResultData.size();
            Log.consoleLog(ifr, "BudgetPortalCustomCode:popluateDocumentsUploadCb->rowCount::" + rowCount);
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
                String Salaried = ifr.getActivityName();
                for (int i = 0; i < applicantName.length; i++) {
                    String applicantNameArrayData = applicantName[i];
                    String query = ConfProperty.getQueryScript("getDocNameQuery");
                    List<List<String>> result = ifr.getDataFromDB(query);
                    Log.consoleLog(ifr, "result is.." + result);
                    int resultsize = result.size();
                    Log.consoleLog(ifr, "size is " + resultsize);
                    if (resultsize > 0) {
                        JSONArray arr = new JSONArray();
                        for (int j = 0; j < result.size(); j++) {
                            JSONObject re = new JSONObject();

                            if (applicantNameArrayData.contains("Co-Borrower")) {
                                Log.consoleLog(ifr, "applicantName is " + Salaried);
                                if (!Salaried.equalsIgnoreCase("")) {
                                    re.put("Mandatory", result.get(j).get(1));
                                    re.put("Document Type ", result.get(j).get(0));
                                    re.put("Applicant Type", applicantName[i]);
                                    arr.add(re);
                                }
//                            re.put("Applicant Type", applicantName[i]);
//                            arr.add(re);
                            } else {
                                re.put("Document Type ", result.get(j).get(0));
                                re.put("Mandatory", result.get(j).get(1));
                                re.put("Applicant Type", applicantName[i]);
                                arr.add(re);
                            }
                        }
                        Log.consoleLog(ifr, "Document grid  json array::" + arr);
                        ifr.addDataToGrid("CB_UPLOAD_DOCUMENT", arr);
                    }
                }

            }

            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "1", true);
            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "2", true);
            ifr.setColumnDisable("CB_UPLOAD_DOCUMENT", "4", true);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the popluateDocumentsUploadVL" + e);
        }

    }

    //added by ishwarya for revised scorecard on 2-22-2024
    public String checkScorecardbackOffice(IFormReference ifr) {
        String scorecardDecision = "";
        try {
            Log.consoleLog(ifr, "inside checkScorecardbackOffice:::");
            Log.consoleLog(ifr, "inside checkScorecardbackOffice:::");
            String RecommendLoanAmt = ifr.getValue("FE_Recommended_Loan_Amount").toString();
            Log.consoleLog(ifr, "RecommendLoanAmt:: " + RecommendLoanAmt);
            if (!RecommendLoanAmt.isEmpty()) {
                Log.consoleLog(ifr, "inside scorecard for backoffice brms:::");
                String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
                String npa_inp = "";
                String bscore = "";
                String overduedays_inp = "";
                String existcust_inp = "";
                String overduedaysupto = "";
                int maxOccurrences = 0;
                String grossSalary = "";
                String netIncome = "";
                String deductionSalary = "";
                String category = "";
                String expYears = "";
                String residence_inp = "";
                String monthlydeduction = "0";
                String maxOccurrencesNumber = null;
                String APIResponse = mGetAPIData(ifr);
                Log.consoleLog(ifr, "Entered into mGetAPIData:::");

                if (APIResponse.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                JSONParser jp = new JSONParser();
                JSONObject obj = (JSONObject) jp.parse(APIResponse);
                //String scoreQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE FROM LOS_CAN_IBPS_BUREAUCHECK "
                //+ "WHERE PROCESSINSTANCEID='" + ProcessInsanceId + "'";
                String scoreQuery = ConfProperty.getQueryScript("getScoreQuery").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
                List<List<String>> Result1 = cf.mExecuteQuery(ifr, scoreQuery, "Execute query for fetching scoreQuery data");
                if (!Result1.isEmpty()) {

                    bscore = Result1.get(0).get(0);
                    npa_inp = Result1.get(0).get(1);
                    npa_inp = npa_inp.equalsIgnoreCase("") ? "No" : "Yes";
                    Log.consoleLog(ifr, "bscore:::: " + bscore);
                    monthlydeduction = Result1.get(0).get(3);
                    monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;
                    Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
                }
                Log.consoleLog(ifr, "before query");
                //String query = "SELECT PROCESSINSTANCEID, LISTAGG(CICOVERDUE, ',') WITHIN GROUP (ORDER BY CICOVERDUE) AS"
                //+ " CICOVERDUE_LIST FROM LOS_CAN_IBPS_BUREAUCHECK  WHERE "
                // + "PROCESSINSTANCEID='" + ProcessInsanceId + "' GROUP BY PROCESSINSTANCEID";
//                String query = ConfProperty.getQueryScript("getCICCoverDueQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
//                Log.consoleLog(ifr, "after query");
//                List<List<String>> Result2 = ifr.getDataFromDB(query);
//                Log.consoleLog(ifr, "Result2:::: " + Result2);
//                if (!Result2.isEmpty()) {
//                    overduedays_inp = Result2.get(0).get(1);
//                    String[] numbersArray = overduedays_inp.replaceAll("[\\[\\]]", "").split(",");
//                    List<String> numbersList = Arrays.asList(numbersArray);
//                    Map<String, Integer> countMap = new HashMap<>();
//                    numbersList.forEach((number) -> {
//                        countMap.put(number, countMap.getOrDefault(number, 0) + 1);
//                    });
//                    for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
//                        if (entry.getValue() > maxOccurrences || (entry.getValue() == maxOccurrences && entry.getKey().compareTo(maxOccurrencesNumber) > 0)) {
//                            maxOccurrences = entry.getValue();
//                            maxOccurrencesNumber = entry.getKey();
//                        }
//                    }
//                }
//                String maxOccurrencesip = String.valueOf(maxOccurrences);
//                overduedays_inp = maxOccurrencesNumber;
//                overduedaysupto = maxOccurrencesip;

                String paymentHistoryIp = "";

                String paymentquery = "SELECT NVL(REGEXP_REPLACE(PAYMENTHISTORY, '[^0-9]', ''), 0) AS replaced_column FROM LOS_CAN_IBPS_BUREAUCHECK where PROCESSINSTANCEID='" + ProcessInsanceId + "'";

                List<List<String>> paymentHistory = ifr.getDataFromDB(paymentquery);
                if (!paymentHistory.isEmpty()) {
                    paymentHistoryIp = paymentHistory.get(0).get(0);
                    Log.consoleLog(ifr, "PAYMENTHISTORY:::: " + paymentHistoryIp);
                }
                String[] splitPaymentHistory = paymentHistoryIp.split("(?<=\\G.{3})"); // Splits every 3 chars without adding a delimiter
                Log.consoleLog(ifr, "parts:::: " + splitPaymentHistory);
                int maxPaymentHistory = Integer.MIN_VALUE;
                Map<Integer, Integer> countMap = new HashMap<>();

                // Find maxPaymentHistory and count occurrences
                for (String part : splitPaymentHistory) {
                    int values = Integer.parseInt(part); // Assuming decimal integers
                    countMap.put(values, countMap.getOrDefault(values, 0) + 1);
                    maxPaymentHistory = Math.max(maxPaymentHistory, values);
                }

                // Find the range containing the max value
                String highestRangeKey = getRangeKey(maxPaymentHistory);

                // Count numbers within the highest range
                int countInHighestRange = getCountInRange(countMap, highestRangeKey);

                String maxOccurrencesip = String.valueOf(countInHighestRange);
                maxOccurrencesNumber = String.valueOf(maxPaymentHistory);
                overduedays_inp = maxOccurrencesNumber;
                Log.consoleLog(ifr, "overduedays_inp:::: " + overduedays_inp);
                overduedaysupto = maxOccurrencesip;
                Log.consoleLog(ifr, "overduedaysupto :::: " + overduedaysupto);

                String Age = cf.getJsonValue(obj, "Age");
                String Years = "0";
                Years = cf.getJsonValue(obj, "Years");
                String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
                writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
                //String IncomeQuery = "SELECT a.GROSSSALARY,a.NETSALARY,a.OVER_ALL_EXPERIENCE,a.CATEGORY,a.RESIDENCE "
                //+ "from LOS_NL_Occupation_INFO a inner join LOS_NL_BASIC_INFO b on a.f_key=b.f_key "
                // + "where b.PID='" + ProcessInsanceId + "' and b.applicanttype='B'";
                String IncomeQuery = ConfProperty.getQueryScript("getIncomeQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
                List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching customer data");
                if (!IncomeDtsPortal.isEmpty()) {
                    grossSalary = IncomeDtsPortal.get(0).get(0);
                    netIncome = IncomeDtsPortal.get(0).get(1);
                    expYears = IncomeDtsPortal.get(0).get(2);
                    category = IncomeDtsPortal.get(0).get(3);
                    residence_inp = IncomeDtsPortal.get(0).get(4);
                    deductionSalary = IncomeDtsPortal.get(0).get(5);
                    Log.consoleLog(ifr, "GROSSSALARY ::" + grossSalary);
                    Log.consoleLog(ifr, "NETSALARY ::" + netIncome);
                    Log.consoleLog(ifr, "OVER_ALL_EXPERIENCE ::" + expYears);
                    Log.consoleLog(ifr, "CATEGORY ::" + category);
                    Log.consoleLog(ifr, "RESIDENCE ::" + residence_inp);
                }
                String natureOfSecurity = "Only Third party personal Guarantee";
                String recovery = "Salary Account";
                String AccountHoldertypeCode = cf.getJsonValue(obj, "AccountHoldertypeCode");
                Log.consoleLog(ifr, "AccountHoldertypeCode ::" + AccountHoldertypeCode);
                AccountHoldertypeCode = AccountHoldertypeCode.equalsIgnoreCase("7") ? "Yes" : "No";
                String guarantorwriteoff = AccountHoldertypeCode;
                String accountstatus = cf.getJsonValue(obj, "Account_Status");
                Log.consoleLog(ifr, "accountstatus :: " + accountstatus);
                if ((accountstatus.equalsIgnoreCase("00") || accountstatus.equalsIgnoreCase("40") || accountstatus.equalsIgnoreCase("52") || accountstatus.equalsIgnoreCase("13")
                        || accountstatus.equalsIgnoreCase("15") || accountstatus.equalsIgnoreCase("16") || accountstatus.equalsIgnoreCase("17")
                        || accountstatus.equalsIgnoreCase("12") || accountstatus.equalsIgnoreCase("11") || accountstatus.equalsIgnoreCase("71") || accountstatus.equalsIgnoreCase("78")
                        || accountstatus.equalsIgnoreCase("80") || accountstatus.equalsIgnoreCase("82") || accountstatus.equalsIgnoreCase("83") || accountstatus.equalsIgnoreCase("84")
                        || accountstatus.equalsIgnoreCase("DEFAULTVALUE") || accountstatus.equalsIgnoreCase("21") || accountstatus.equalsIgnoreCase("22") || accountstatus.equalsIgnoreCase("23")
                        || accountstatus.equalsIgnoreCase("24") || accountstatus.equalsIgnoreCase("25"))) {

                    accountstatus = "No";
                } else {
                    accountstatus = "Yes";
                }
                String guarantornpa_inp;
                if (AccountHoldertypeCode.equalsIgnoreCase("Yes") && accountstatus.equalsIgnoreCase("Yes")) {
                    guarantornpa_inp = "Yes";
                } else {
                    guarantornpa_inp = "No";
                }
                String nparestmonths_inpo = accountstatus;
                String settledhist_inp = accountstatus;
//                String existcust_inpQuery = "SELECT EXISTINGCUSTOMER FROM LOS_NL_BASIC_INFO where PID = '"
//                        + ProcessInsanceId + "' ";
//                List<List<String>> Results = ifr.getDataFromDB(existcust_inpQuery);
//                if (!Results.isEmpty()) {
//                    existcust_inp = Results.get(0).get(0);
//                    existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
//                    Log.consoleLog(ifr, "existcust_inp: " + existcust_inp);
//                }
                String existcust_inpQuery = ConfProperty.getQueryScript("getExistingCustomer").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
                List<List<String>> Results = ifr.getDataFromDB(existcust_inpQuery);
                if (!Results.isEmpty()) {
                    existcust_inp = Results.get(0).get(0);
                    existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
                }
                //added by aravindh
                String RecommendLoanAmount = "";
                String amount_Query = ConfProperty.getQueryScript("GetRecommendLoanAmount").replaceAll("#PID#", ProcessInsanceId);
                Log.consoleLog(ifr, "RecommendLoanAmount_Query: " + amount_Query);
                List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

                if (!Results1.isEmpty()) {
                    RecommendLoanAmount = Results1.get(0).get(0);
                    Log.consoleLog(ifr, "RecommendLoanAmount " + RecommendLoanAmount);
                } else {
                    JSONObject message = new JSONObject();
                    Log.consoleLog(ifr, "RecommendLoanAmount  is emptyy" + RecommendLoanAmount);
                    message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "error", "Please enter the valid Recommended loan amount in Eligibilty and save to fetch Scorecard Rating!"));
                    return message.toString();
                }
                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String loanTenure = null;
                String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInsanceId);
                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
                if (list1.size() > 0) {
                    loanTenure = list1.get(0).get(2);
                }
                Log.consoleLog(ifr, "loanTenure From slider: " + loanTenure);
                String loanROI = pcm.mGetROICB(ifr);
                /*  String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
                List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
                if (list2.size() > 0) {
                    loanROI = list2.get(0).get(0);
                }*/
                Log.consoleLog(ifr, "roi : " + loanROI);
                Log.consoleLog(ifr, "Before conversion ");
                int grossSalaryip1 = Integer.parseInt(grossSalary);
                //int deductionSalaryip=Integer.parseInt(deductionSalary);
                //int MonthlyDeductionip=Integer.parseInt(monthlydeduction);
                //int netIncomeip1 = Integer.parseInt(netIncome);
                Log.consoleLog(ifr, "Before bigdecimal conversion  ");
                BigDecimal deductionSalaryip = new BigDecimal(deductionSalary);
                Log.consoleLog(ifr, "after bigdecimal conversion deductionSalary ::" + deductionSalaryip);
                BigDecimal grossSalaryip = new BigDecimal(grossSalary);
                Log.consoleLog(ifr, "after bigdecimal conversion grossSalaryip ::" + grossSalaryip);
                BigDecimal MonthlyDeductionip = new BigDecimal(monthlydeduction);
                Log.consoleLog(ifr, "checkScorecardbackOffice:::::after bigdecimal conversion MonthlyDeductionip ::" + MonthlyDeductionip);
                BigDecimal loanamount = new BigDecimal(RecommendLoanAmount);
                Log.consoleLog(ifr, "after bigdecimal conversion loanamount ::" + loanamount);
                BigDecimal ftRoi = new BigDecimal(loanROI);
                Log.consoleLog(ifr, "after bigdecimal conversion ftRoi ::" + ftRoi);
                int loanTenures = Integer.parseInt(loanTenure);
                BigDecimal perposedEmi = calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
                Log.consoleLog(ifr, "after bigdecimal conversion perposedEmi ::" + perposedEmi);
                BigDecimal netIncomeip = grossSalaryip.subtract(deductionSalaryip).subtract(MonthlyDeductionip).subtract(perposedEmi);

                Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip ::" + netIncomeip);

                int netIncomeip1 = netIncomeip.intValue();
                Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip1 ::" + netIncomeip1);
                int nethomeins_inprange = netIncomeip1 / grossSalaryip1 * 100;
                int anninc_inp = grossSalaryip1 * 12;
                Log.consoleLog(ifr, "bscore:::" + bscore + ",overduedays_inp:::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff:::"
                        + guarantorwriteoff + ",npa_inp:::" + npa_inp + ",nparestmonths_inpo:::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",writeOffPresent:::" + writeOffPresent
                        + ",nethomeins_inprange:::" + nethomeins_inprange + ",category:::" + category + ",anninc_inp:::" + anninc_inp + ",grossSalary:::" + grossSalary + ",expYears:::" + expYears + ",existcust_inp:::" + existcust_inp
                        + ",Years::" + Years + ",natureOfSecurity:::" + natureOfSecurity + ",residence_inp:::" + residence_inp + ",recovery:::" + recovery);
                String scorecardInParams = "";

                if (!bscore.equalsIgnoreCase("I")) {

                    scorecardInParams = bscore + "," + overduedays_inp + "," + overduedaysupto + "," + guarantornpa_inp + ","
                            + guarantorwriteoff + "," + npa_inp + "," + nparestmonths_inpo + "," + settledhist_inp + "," + writeOffPresent
                            + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                            + "," + Years + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                    scorecardDecision = checkScoreCardBudget(ifr, "CB_SCORECARD", scorecardInParams, "totalgrade_out", "B");
                } else {

                    scorecardInParams = npa_inp + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                            + "," + Years + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                    scorecardDecision = checkScoreCardBudget(ifr, "CB_ScoreCard_CICImmune", scorecardInParams, "totalgrade_out", "B");

                }
                //    scorecardDecision = checkScoreCard(ifr, "CB_SCORECARD", scorecardInParams, "totalgrade_out");

                if (scorecardDecision.equalsIgnoreCase(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                    return pcm.returnError(ifr);
                } else {
                    Log.consoleLog(ifr, " Returning success ");
                    return "SUCCESS";
                }
            } else {
                JSONObject message = new JSONObject();
                Log.consoleLog(ifr, "Recommended loan amount is empty::");
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "error", "Please enter the valid Recommended loan amount in Eligibilty to fetch Scorecard Rating!"));
                return message.toString();
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in scorecard for backoffice brms::" + e);
        }
        return scorecardDecision;
    }

// added by vandana
    public BigDecimal calculatePMTScorecard(IFormReference ifr, BigDecimal LoanAmount, BigDecimal rate, int nper) {
        rate = rate.divide(new BigDecimal("1200"), MathContext.DECIMAL64);
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
//        BigDecimal pv = new BigDecimal("-420964");
        BigDecimal pv = LoanAmount;
        BigDecimal ratePowerN = onePlusRate.pow(nper);
        BigDecimal fv = BigDecimal.ZERO;
        BigDecimal numerator = rate.multiply(ratePowerN, MathContext.DECIMAL64);
        BigDecimal denominator = ratePowerN.subtract(BigDecimal.ONE);
        BigDecimal factor = numerator.divide(denominator, MathContext.DECIMAL64);
        BigDecimal pmt = pv.negate().multiply(factor, MathContext.DECIMAL64)
                .add(fv.divide(ratePowerN, MathContext.DECIMAL64), MathContext.DECIMAL64)
                .setScale(2, RoundingMode.HALF_UP);
        return pmt;
    }

// added by vandana
    public String getRangeKey(int value) {
        if (value >= 1 && value <= 10) {
            return "1 to 10";
        } else if (value >= 11 && value <= 29) {
            return "11 to 29";
        } else if (value >= 30 && value <= 59) {
            return "30 to 59";
        } else if (value >= 60 && value <= 89) {
            return "60 to 89";
        } else if (value >= 90 && value <= 100) {
            return "90 to 100";
        }
        return "0"; // Invalid range
    }

    public int getCountInRange(Map<Integer, Integer> countMap, String rangeKey) {
        int count = 0;
        String[] rangeParts = rangeKey.split(" ");
        if (rangeParts.length != 3) {
            // Invalid range key format
            System.err.println("Invalid range key format: " + rangeKey);
            return 0;
        }
        int start = Integer.parseInt(rangeParts[0]); // Extract start of range
        int end = Integer.parseInt(rangeParts[2]); // Extract end of range

        // Sum counts within the range
        for (int i = start; i <= end; i++) {
            count += countMap.getOrDefault(i, 0);
        }
        return count;
    }

    public String CheckMisStatusBudget(IFormReference ifr) {//Corrected
        Log.consoleLog(ifr, "CheckMisStatusBudget : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            Demographic objDemographic = new Demographic();
            String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition Demographic Budget");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj.toString());
                String CustMisStatus = obj.get("CustMisStatus").toString();
                //  CustMisStatus="Incomplete";
                Log.consoleLog(ifr, "CustMisStatus Value " + CustMisStatus);
                if (CustMisStatus.equalsIgnoreCase("Incomplete")) {
                    ifr.setStyle("P_CB_OD_Details", "visible", "true");
                    ifr.setStyle("P_CB_OD_D_Minority_Status", "visible", "true");
                    ifr.setStyle("P_CB_OD_D_Category", "visible", "true");
                    Log.consoleLog(ifr, " Field CustMisStatus visibled " + CustMisStatus);
//                    String codMisCustCode1 = ifr.getValue("Portal_ODAD_EE_MINORITYSTATUS").toString();
//                    String codMisCustCode2 = ifr.getValue("Portal_ODAD_EE_CASTE").toString();
//                    if (codMisCustCode1.equalsIgnoreCase("")) {
//                        return pcm.returnErrorThroughExecute(ifr, "Please Select Minority Status!");
//                    }
//                    if (codMisCustCode2.equalsIgnoreCase("")) {
//                        return pcm.returnErrorThroughExecute(ifr, "Please Select Caste!");
//                    }

                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in CheckMisStatusBudget");
        }
        return "";
    }

    public String onNextClickUpdateMIS(IFormReference ifr) {
        Log.consoleLog(ifr, "onNextClickUpdateMIS : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String codeMis1 = ifr.getValue("P_CB_OD_D_Minority_Status").toString();
            String codeMis2 = ifr.getValue("P_CB_OD_D_Category").toString();
            Log.consoleLog(ifr, "Minority_Status codeMis1 : " + codeMis1);
            Log.consoleLog(ifr, "Category codeMis2 : " + codeMis2);
            if (!codeMis1.equalsIgnoreCase("") && !codeMis2.equalsIgnoreCase("")) {
                String Response = LADPC.updateMIS(ifr, codeMis1, codeMis2);
                if (Response.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                    try {
                        List<List<String>> list = cf.mExecuteQuery(ifr, "select miscodedesc from LOS_MST_MISCODE where MISCODE='" + codeMis1 + "' and ISACTIVE='Y'", "GeTMISCODEDETailS:codeMis1");
                        String codeMis1value = list.get(0).get(0);
                        list = cf.mExecuteQuery(ifr, "select miscodedesc from LOS_MST_MISCODE where MISCODE='" + codeMis2 + "' and ISACTIVE='Y'", "GeTMISCODEDETailS:codeMis2 ");
                        String codeMis1value2 = list.get(0).get(0);
                        String miscode = "update los_l_basic_info_i set religion='" + codeMis1value + "', caste='" + codeMis1value2 + "' where f_key = \n"
                                + "(select f_key from los_nl_basic_info where PID='" + PID + "'  and applicanttype='B' and rownum=1)";
                        cf.mExecuteQuery(ifr, miscode, "UpdateMISCODEDETailS ");
                    } catch (Exception e) {
                        Log.errorLog(ifr, "Exception in Updating MIS  in table  " + e);
                        return pcm.returnError(ifr);
                    }
                }
                if (Response.contains(RLOS_Constants.ERROR)) {

                    Log.errorLog(ifr, "Exception in Updating MIS  ");
                    return pcm.returnError(ifr);
                }

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in  onNextClickUpdateMIS " + e);
            return pcm.returnError(ifr);
        }
        return "";
    }

    public String BudgetCoBoEligibility(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();

        try {
            Log.consoleLog(ifr, "inside BudgetCoBoEligibility");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONArray LV_BCGrid = ifr.getDataFromGrid("ALV_BUREAU_CONSENT");
            String consentFlag = "";
            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + subProductCode);

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
                    ifr.setStyle("Btn_Refresh", "disable", "true");
//                    String CibilExpDecision = CheckCibilandExperianBudget(ifr);
//                    Log.consoleLog(ifr, "CibilExpDecision:::" + CibilExpDecision);
//                    if (CibilExpDecision.equalsIgnoreCase("Approve")) {
//                        Log.consoleLog(ifr, "mAccClickSetConsentStatus::Checking knockoffDecision for CB  :");
                    String result = CheckAllRules(ifr);

                    JSONObject returnJSON = new JSONObject();
                    returnJSON.put("saveWorkitem", "true");
                    return returnJSON.toString();
//                    } else {
//                        Log.consoleLog(ifr, "BudgetCoBoEligibility:: Budget expCibilDecision fail " + CibilExpDecision);
//                        message_err.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error",
//                                "Co-Borrower is not eligible for the selected digital loan journey."));
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
//                    }

                } else if (consentFlag.equalsIgnoreCase("N")) {
                    //JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error", "Co borrower consent Rejected"));
                    Log.consoleLog(ifr, " Budget Coborrower consent is rejected");
                    // message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
                    // "Thank you for choosing Canara Bank. You are not eligible for the selected
                    // digital loan journey, as per scheme guidelines of the Bank."));
                    message.put("eflag", "false");

                    JSONArray gridResultSet = new JSONArray();
                    String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
                    org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
                    Log.consoleLog(ifr, "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);

                    String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
                    Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);

                    JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("Name", coBorrowerName);
                    formDetailsJson.put("Eligibility Status", "Not Eligible");

                    gridResultSet.add(formDetailsJson);
                    ifr.clearTable("CB_Eligibility_Grid");
                    ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
                    Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());
                    ifr.setStyle("Btn_Refresh", "disable", "true");
                    return message.toString();
                } else if (consentFlag.equalsIgnoreCase("I")) {
                    // JSONObject message = new JSONObject();
                    Log.consoleLog(ifr, "Co borrower consent Not received");
                    message.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error", "Co borrower consent Not Received"));
                    message.put("eflag", "false");
                    JSONArray gridResultSet = new JSONArray();
                    String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
                    org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
                    Log.consoleLog(ifr, "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);

                    String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
                    Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);

                    JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("Name", coBorrowerName);
                    formDetailsJson.put("Eligibility Status", "Consent Pending");

                    gridResultSet.add(formDetailsJson);
                    ifr.clearTable("CB_Eligibility_Grid");
                    ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
                    Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());

                    //ifr.addItemInCombo("DecisionValue", "Submit", "S");
                    return message.toString();
                }

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in BudgetCoBoEligibility::" + e);
        }

        return "";
    }

    /*
    * Author: Nikhil Vangala 
    * performs 
    * 1) CIC score check
    * 2) knock off check
    * 3) ScoreCard check
     */
    public String CheckAllRules(IFormReference ifr) {
        Log.consoleLog(ifr, "CheckAllRules:::: entered CheckAllRules");
        JSONObject message_err = new JSONObject();
        String knockoffDecision = mImpCoObligantCheck(ifr);
        Log.consoleLog(ifr, "knockoffDecision::: " + knockoffDecision);
        if (knockoffDecision.toUpperCase().equalsIgnoreCase("PROCEED")) {
            Log.consoleLog(ifr, "Budget Coborrower knockoffDecision Passed Successfully:::");
            // String CibilExpDecision = CheckCibilandExperianBudget(ifr);
//                    Log.consoleLog(ifr, "CibilExpDecision:::" + CibilExpDecision);
            //call mCallBureau and checkCICScore to get CIC score

            String result = checkBureauAndCIC(ifr);
            if (result.equalsIgnoreCase("Approve")) {
                String scoreCardDecisionCB = scoreCardDecisionForCB(ifr);
                Log.consoleLog(ifr, "CheckAllRules:::: scoreCardDecisionCB  is" + scoreCardDecisionCB);
                if (scoreCardDecisionCB.equalsIgnoreCase(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                    Log.consoleLog(ifr, "CheckAllRules:::: error in scoreCardDecisionCB ");
                    return pcm.returnError(ifr);
                }
                if (scoreCardDecisionCB.equalsIgnoreCase("Low Risk") || scoreCardDecisionCB.equalsIgnoreCase("Low Risk-II") || scoreCardDecisionCB.equalsIgnoreCase("Normal Risk")
                        || scoreCardDecisionCB.equalsIgnoreCase("Moderate Risk")) {
                    Log.consoleLog(ifr, "CheckAllRules:::: proceed in scoreCardDecisionCB ");
                    JSONArray gridResultSet = new JSONArray();
                    String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
                    org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
                    Log.consoleLog(ifr, "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);

                    String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
                    Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);

                    JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("Name", coBorrowerName);
                    formDetailsJson.put("Eligibility Status", "Eligible");

                    gridResultSet.add(formDetailsJson);
                    ifr.clearTable("CB_Eligibility_Grid");
                    ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
                    ifr.setStyle("Btn_Refresh", "disable", "true");
                    Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());

                } else {
                    Log.consoleLog(ifr, "scoreCard Failed for CB:::");
                    message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message_err.put("eflag", "false");
                    return message_err.toString();
                }
            } else {
                Log.consoleLog(ifr, "checkBureauAndCIC:::::Not Approved");
                return result;
            }
        } else if (knockoffDecision.toUpperCase().equalsIgnoreCase("REJECT")) {
            Log.consoleLog(ifr, " Budget Coborrower knockoffDecision fail " + knockoffDecision);
            message_err.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error", "Co-Borrower is not eligible for the selected digital loan journey."));
            message_err.put("eflag", "false");
            JSONArray gridResultSet = new JSONArray();
            String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
            org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
            Log.consoleLog(ifr,
                    "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);
            String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
            Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);

            JSONObject formDetailsJson = new JSONObject();
            formDetailsJson.put("Name", coBorrowerName);
            formDetailsJson.put("Eligibility Status", "Not Eligible");

            gridResultSet.add(formDetailsJson);
            ifr.clearTable("CB_Eligibility_Grid");
            ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
            Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());

            return message_err.toString();
        } else {
            Log.consoleLog(ifr, "CheckAllRules:::: knockoffDecision is not Approve or Reject");
        }

        return "";
    }

    /*
    * Author: Nikhil Vangala 
    * Calls mCallBureau and then checkCICScore
    * to fetch the CICscore.
    *
     */
    public String checkBureauAndCIC(IFormReference ifr) {
        Log.consoleLog(ifr, "checkBureauAndCIC:::::::: entered");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String productCode = pcm.mGetProductCode(ifr);
        Log.consoleLog(ifr, "checkBureauAndCIC::::::ProductCode:" + productCode);
        String subProductCode = pcm.mGetSubProductCode(ifr);
        Log.consoleLog(ifr, "checkBureauAndCIC::::::subProductCode:" + subProductCode);
        String COMobNo = pcm.getMobileNumber(ifr, "CB");
        //ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();

        String COCustID = pcm.getCustomerIDCB(ifr, "CB");
        //ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
        //getAadharCustomerAccountSummary
        HashMap<String, String> map = new HashMap<>();
        map.put("MobileNumber", COMobNo);
        map.put("CustomerId", COCustID);
        // JSONObject message_err = new JSONObject(); 
        String cb1 = "";
        try {
            Log.consoleLog(ifr, "checkBureauAndCIC::::::::about to call getAadharCustomerAccountSummary ");
            String AADHARNUMBERS = cas.getAadharCustomerAccountSummary(ifr, map);
            Log.consoleLog(ifr, "checkBureauAndCIC::::::::AADHARNUMBERS value-->>" + AADHARNUMBERS);
            //calling Bureau for CIBIL of Co-Borrower
            Log.consoleLog(ifr, "checkBureauAndCIC:::::::calling Bureau for CIBIL of Co-Borrower");
            cb1 = mCallBureau(ifr, "CB", AADHARNUMBERS, "CB");

            if (cb1.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "checkBureauAndCIC:::::::error in  mCallBureau for CIBIL");
                return pcm.returnError(ifr);
            }
            String decisionCBCibil = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "CB");
            Log.consoleLog(ifr, "checkBureauAndCIC::::::decisionCBCibil::" + decisionCBCibil);
            if (decisionCBCibil.equalsIgnoreCase("Approve") || decisionCBCibil.equalsIgnoreCase("Reject")) {
                Log.consoleLog(ifr, "checkBureauAndCIC::::::CIBIL Value is:::" + decisionCBCibil);

                String Experrian = mCallBureau(ifr, "EX", AADHARNUMBERS, "CB");
                if (Experrian.contains(RLOS_Constants.ERROR)) {
                    Log.consoleLog(ifr, "checkBureauAndCIC:::::::error in  mCallBureau for EXPERIAN");
                    return pcm.returnError(ifr);
                }

                String decisionCBExperian = objbcr.checkCICScore(ifr, productCode, subProductCode, "EX", "CB");
                //mCheckCIBILScoreknockOff(ifr, "EX", subProductCode, productCode);

                Log.consoleLog(ifr, "decisionCBExperian::" + decisionCBExperian);
                if (decisionCBExperian.equalsIgnoreCase("Approve") || decisionCBExperian.equalsIgnoreCase("Reject")) {
                    Log.consoleLog(ifr, "checkBureauAndCIC::::::Experian Value is:::" + decisionCBExperian);
                    Log.consoleLog(ifr, "CheckCibilandExperian::EXPERIAN Passed Successfully:::" + decisionCBExperian);
                    String bueroTableQuery = "select count(*) from los_nl_cb_details where pid = '" + PID + "' and APPLICANT_TYPE in (select INSERTIONORDERID from los_nl_basic_info where  pid ='" + PID + "' and APPLICANTTYPE ='CB')";
                    Log.consoleLog(ifr, "bueroTableQuery ::" + bueroTableQuery);
                    List<List<String>> bueroTableData = ifr.getDataFromDB(bueroTableQuery);
                    String count = bueroTableData.get(0).get(0);
                    if (Integer.parseInt(count) == 0) {
                        String dataSaveBueroCheckGridQuery = "select distinct BureauType,Exp_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK where ProcessInstanceId='" + PID + "' and applicant_type='CB'";
                        List<List<String>> dataSaveBueroCheckGridData = ifr.getDataFromDB(dataSaveBueroCheckGridQuery);
                        Log.consoleLog(ifr, "dataSaveBueroCheckGridData:::" + dataSaveBueroCheckGridData);
                        if (dataSaveBueroCheckGridData.size() > 0) {
                            String BureauType[] = new String[dataSaveBueroCheckGridData.size()];
                            String Exp_CBSCORE[] = new String[dataSaveBueroCheckGridData.size()];
                            for (int i = 0; i < dataSaveBueroCheckGridData.size(); i++) {
                                Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData BureauType::" + dataSaveBueroCheckGridData.get(i).get(0));
                                BureauType[i] = dataSaveBueroCheckGridData.get(i).get(0);
                                Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData:: Exp_CBSCORE" + dataSaveBueroCheckGridData.get(i).get(1));
                                Exp_CBSCORE[i] = dataSaveBueroCheckGridData.get(i).get(1);
                                Log.consoleLog(ifr, "BureauType:::Exp_CBSCORE" + BureauType[i] + Exp_CBSCORE[i]);
                            }
                            String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where Applicanttype='CB' and PID='" + PID + "'";
                            List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                            //ifr.getDataFromDB(checkquery);

                            if (borrowerQueryData.size() > 0) {
                                JSONArray arr = new JSONArray();
                                for (int i = 0; i < BureauType.length; i++) {
                                    JSONObject re = new JSONObject();

                                    String brdata = borrowerQueryData.get(0).get(0);
                                    String StrBureScore = Exp_CBSCORE[i];
                                    String strBureauType = BureauType[i];

                                    re.put("QNL_CB_Details_Applicant_Type", brdata);
                                    re.put("QNL_CB_Details_CB_Type", strBureauType);
                                    re.put("QNL_CB_Details_CB_Score", StrBureScore);
                                    arr.add(re);
                                }
                                Log.consoleLog(ifr, "Credit bureau grid json array::" + arr);
                                ifr.addDataToGrid("ALV_CB_Details", arr);
                            }

                        }
                    }//Bureo check data population for CB End  
                    else {
                        Log.consoleLog(ifr, "Credit bureau grid json added already array::");

                    }
                    return "Approve";
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(BudgetPortalCustomCode.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public String CheckCibilandExperianBudget(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside Budget CheckCibilandExperianBudget:CB ");

        try {
            String mobnoCB = "";
            String custidCB = "";
            String Aadhar = "";
            String cust_id = "";
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
            HashMap<String, String> map = new HashMap<>();
            String MobileData_Query = ConfProperty.getQueryScript("getMobileNumberQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            //   List list = cf.mExecuteQuery(ifr, MobileData_Query, "MobileData_Query:");
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobnoCB = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobnoCB);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobnoCB);
            //  custid = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
            custidCB = pcm.getCustomerIDCB(ifr, "CB");
            Log.consoleLog(ifr, "CustomerId==>" + custidCB);
            Log.consoleLog(ifr, ".CUSTOMERID" + custidCB);

            Log.consoleLog(ifr, "Budget loan map value:::==>" + map);
            //  String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            map.clear();
            map.put("MobileNumber", mobnoCB);
            map.put("CustomerId", custidCB);
            Log.consoleLog(ifr, "Budget.Map" + map);
            String cbsresp = objCustSummary.getCustomerAccountSummary(ifr, map);
            JSONParser jp = new JSONParser();
            JSONObject cbsrespobj;
            cbsrespobj = (JSONObject) jp.parse(cbsresp);
            cust_id = cbsrespobj.get("CustomerID").toString();
            Aadhar = cbsrespobj.get("AadharNo").toString();
            Log.consoleLog(ifr, "Aadhar no::" + Aadhar);
            Log.consoleLog(ifr, "For bureau check:::");
            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);

            String cb = mCallBureau(ifr, "CB", Aadhar, "CB");
            if (cb.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            Log.consoleLog(ifr, "before mCheckCIBILScoreknockOff");
            //   objbcr.checkCICScore(ifr, productCode, subProductCode, "CB");
            String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");
            //     bpcc.mCheckCIBILScoreknockOff(ifr, "CB", subProductCode, productCode);
            Log.consoleLog(ifr, "decision1/CB::" + decision);
            if (decision.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "BPCC:::CheckCibilandExperianBudget:: CIBIL Failed for Co-Borrower:::");
                return RLOS_Constants.ERROR;
            } else if (decision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "CIBIL Passed Successfully for Co-Borrower:::");
                String EX = mCallBureau(ifr, "EX", Aadhar, "CB");
                if (EX.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "EX", "B");
                //bpcc.mCheckCIBILScoreknockOff(ifr, "EX", subProductCode, productCode);
                Log.consoleLog(ifr, "decision2/EX::" + decision);
                if (decision.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "EXPERIAN Passed Successfully for Co-Borrower:::");
                } else {
                    Log.consoleLog(ifr, "EXPERIAN FAILED for Co-Borrower:::");
                }
            } else {
                Log.consoleLog(ifr, "CIBIL FAILED for Co-Borrower:::");
            }
            return decision;

        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in CheckCibilandExperian" + e);
            return pcm.returnError(ifr);
        }
    }

    //Added by Aravindh on 23/05/24
    public String CoObligantCBSCheck(IFormReference ifr, String control, String event, String ApplicantType) {
        JSONObject message = new JSONObject();
        Log.consoleLog(ifr, "Inside CoObligantCBSCheck");

        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String COMobNo = ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();
            String COCustID = ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
            String Ext = ifr.getValue("P_CB_OD_EXISTING_CUSTOMER").toString();
            Log.consoleLog(ifr, "Existing Customer Radio button ===> " + Ext);
            Log.consoleLog(ifr, "MOBILE NUMBER ===> " + Ext);
            Log.consoleLog(ifr, "CUSTOMER ID ===> " + Ext);
            if (Ext.equalsIgnoreCase("YES")) {
                Log.consoleLog(ifr, "Inside Existing Customer Radio button ===> " + Ext);

                if (!COMobNo.isEmpty() && !COCustID.isEmpty()) {
                    Log.consoleLog(ifr, "Inside MOBILE NUMBER & CUSTOMER_ID Validation ");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("MobileNumber", COMobNo);
                    map.put("CustomerId", COCustID);
                    Log.consoleLog(ifr, "BudgetPortalCustomCode:CoObligantCBSCheck- MobileNumber:" + COMobNo);
                    Log.consoleLog(ifr, "BudgetPortalCustomCode:CoObligantCBSCheck- CustomerId:" + COCustID);

                    String responseCO = cas.getCustomerAccountSummary(ifr, map);
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
                        ifr.setValue("P_CB_OD_MOBILE_NUMBER", "");
                        ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
                        //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_OD_ValidateCoObligantCB", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance"));
                        //message.put("eflag", "false");//Hard Stop
                        message.put("MSGSTS", "Y");
                        message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance");
                        return message.toString();
                    } else {
                        ifr.setValue("CoApplicantName_CB", fullName);
                        Log.consoleLog(ifr, "Into Co-obligant validation -- Before DataSaving ");
                        Log.consoleLog(ifr, "ApplicantType :: " + ApplicantType);
                        String result = new PortalCustomCode().saveDataInPartyDetailGrid(ifr, ApplicantType, COMobNo + "~" + COCustID);
                        Log.consoleLog(ifr, "Co-obligant validation is Successful");
                        ifr.setStyle("P_CoObligant_OD_Section", "visible", "true");
                        ifr.setStyle("P_OD_ValidateCoObligantCB", "disable", "true");
                        ifr.setStyle("P_CB_OD_MOBILE_NUMBER", "disable", "true");
                        ifr.setStyle("P_CB_OD_CUSTOMER_ID", "disable", "true");
                        //ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER", "disable", "true");
                        //Commented by Aravindh.K.K on 18/07/2024
                        Log.consoleLog(ifr, "P_OD_ValidateCoObligantCB ====> Validate Button is disabled <==== ");

                        Demographic objDemographic = new Demographic();
                        String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, COCustID);
                        Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
                        if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                            Log.consoleLog(ifr, "inside error condition Demographic Budget");
                            message.put("MSGSTS", "N");
                            message.put("SHOWMSG", "Technical glitch, Try after sometime!");
                            return message.toString();
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

                            ifr.setValue("P_CB_OD_RelationshipWithCanara_COB", String.valueOf(YearsWithCanara));
                            ifr.setValue("P_CB_OD_RelationshipWithCanara_InMonths_COB", String.valueOf(MonthsWithCanara));
                            ifr.setStyle("P_CB_OD_RelationshipWithCanara_COB", "disable", "true");
                            ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths_COB", "disable", "true");
                            String RecoveryMechanism = "Salary / Pension account with us";
                            Log.consoleLog(ifr, "RecoveryMechanism==>" + RecoveryMechanism);
                            ifr.setValue("P_CB_OD_RecoveryMechanism_COB", RecoveryMechanism); //For Co-Applicant
                            // ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "disable", "true");
                            //message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_OD_ValidateCoObligantCB", "error", "Co-Obligant is Existing to the Canara Bank. Please fill the below Co-Obligant Details"));
                            //message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Co-Obligant is Existing to the Canara Bank. Please fill the below Co-Obligant Details"));
                            message.put("MSGSTS", "N");
                            message.put("SHOWMSG", "Co-Obligant is Existing to the Canara Bank. Kindly fill the below Co-Obligant Details");
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
                Log.consoleLog(ifr, "Kindly select existing customer 'Yes' to validate the Co-Obligant ");
                message.put("MSGSTS", "N");
                message.put("SHOWMSG", "Kindly select existing customer 'Yes' to validate the Co-Obligant");
                return message.toString();
            }
        } catch (Exception ex) {
            Log.errorLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
            Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
            message.put("MSGSTS", "N");
            message.put("SHOWMSG", "Technical glitch, Try after sometime!");
            return message.toString();

        }

    }

    /*
    public String populateOccuapationDetailsforCoBorrower(IFormReference ifr, String control, String event, String value) {

        Log.consoleLog(ifr, "populateOccuapationDetailsforCoBorrower : ");

        try {

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);

            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", PID);

            Log.consoleLog(ifr, "OccuppationInfoDetails query Co-Obligant::" + IndexQuery);

            Log.consoleLog(ifr, "OccuppationInfoDetails1 query Co-Obligant::" + IndexQuery1);

//            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
//            List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);
            List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query Co-Obligant ");

            List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");

            Log.consoleLog(ifr, "dataResult::" + dataResult);

            Log.consoleLog(ifr, "dataResult1::" + dataResult1);

            String f_key = "";

            if (dataResult.size() == 0) {

                //String f_key = "3709";
                f_key = dataResult1.get(0).get(0);

                Log.consoleLog(ifr, " INSIDE IF f_key::" + f_key);

                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");

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

                String insertQuery = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGridCoborrower")
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
                        .replaceAll("#portalValue21#", portalValue[21]);
                Log.consoleLog(ifr, "insertQuery for Occupation Info::CoBorrower ::" + insertQuery);

                ifr.saveDataInDB(insertQuery);

            } else {

                Log.consoleLog(ifr, " INSIDE IF dataResult1::" + dataResult.size());

                f_key = dataResult1.get(0).get(0);

                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower f_key::" + f_key);

                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");

                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::" + portalFields);

                String portalFieldStr[] = portalFields.split(",");

                int size1 = portalFieldStr.length;

                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::size1" + size1);

                String portalValue[] = new String[size1];

                for (int i = 0; i < portalFieldStr.length; i++) {

                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();

                }

                String updateQuery = ConfProperty.getQueryScript(" UpdateQueryForOccupationInfoGridCoborrower")
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
                        .replaceAll("#portalValue21#", portalValue[21]);
                Log.consoleLog(ifr, "update for Occupation Info::CoBorrower::" + updateQuery);

                int count = ifr.saveDataInDB(updateQuery);

                Log.consoleLog(ifr, "updateOccuapationDetails::CoBorrower::count" + count);

            }

        } catch (Exception e) {

            Log.consoleLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);

            Log.errorLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);

        }

        return "";

    }*/
    //Added by Aravindh on 05/05/2024
//    public String populateOccuapationDetailsforCoBorrower(IFormReference ifr, String control, String event, String value) {
//        Log.consoleLog(ifr, "populateOccuapationDetailsforCoBorrower : ");
//        try {
//            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);
//            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", PID);
//            Log.consoleLog(ifr, "OccuppationInfoDetails query Co-Obligant::" + IndexQuery);
//            Log.consoleLog(ifr, "OccuppationInfoDetails1 query Co-Obligant::" + IndexQuery1);
//
////            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
////            List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);
//            List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query Co-Obligant ");
//            List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
//            Log.consoleLog(ifr, "dataResult::" + dataResult);
//            Log.consoleLog(ifr, "dataResult1::" + dataResult1);
//            String f_key = "";
//            if (dataResult.size() == 0) {
//                //String f_key = "3709";
//                f_key = dataResult1.get(0).get(0);
//                Log.consoleLog(ifr, " INSIDE IF f_key::" + f_key);
//                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");
//                Log.consoleLog(ifr, "portalFields CoBorrower::" + portalFields);
//                String portalFieldStr[] = portalFields.split(",");
//                Log.consoleLog(ifr, "portalFieldStr[] CoBorrower::" + portalFieldStr);
//                int size1 = portalFieldStr.length;
//                Log.consoleLog(ifr, "portalFields CoBorrower size1::" + size1);
//                String portalValue[] = new String[size1];
//                for (int i = 0; i < size1; i++) {
//                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
//                }
//                Log.consoleLog(ifr, "portalFields array portalValue[i]::CoBorrower ::" + portalValue);
//                String insertQuery = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGridCoborrower")
//                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
//                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
//                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
//                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
//                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
//                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
//                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
//                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
//                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
//                        .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
//                        .replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
//                        .replaceAll("#portalValue21#", portalValue[21]);
//                Log.consoleLog(ifr, "insertQuery for Occupation Info::CoBorrower ::" + insertQuery);
//                ifr.saveDataInDB(insertQuery);
//
//            } else {
//                Log.consoleLog(ifr, " INSIDE IF dataResult1::" + dataResult.size());
//                f_key = dataResult1.get(0).get(0);
//                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower f_key::" + f_key);
//                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");
//                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::" + portalFields);
//                String portalFieldStr[] = portalFields.split(",");
//                int size1 = portalFieldStr.length;
//                Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::size1" + size1);
//                String portalValue[] = new String[size1];
//                for (int i = 0; i < portalFieldStr.length; i++) {
//                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
//                }
//                String updateQuery = ConfProperty.getQueryScript(" UpdateQueryForOccupationInfoGridCoborrower")
//                        .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
//                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
//                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
//                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
//                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
//                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
//                        .replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
//                        .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
//                        .replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
//                        .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
//                        .replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
//                        .replaceAll("#portalValue21#", portalValue[21]);
//                Log.consoleLog(ifr, "update for Occupation Info::CoBorrower::" + updateQuery);
//                int count = ifr.saveDataInDB(updateQuery);
//                Log.consoleLog(ifr, "updateOccuapationDetails::CoBorrower::count" + count);
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);
//            Log.errorLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);
//        }
//        return "";
//
//    }
    public void populateCoborrowerData(IFormReference ifr) {
        Log.consoleLog(ifr, "populateCoborrowerData : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            //String IndexQuery = "select a.CUSTOMERID,b.MobileNo from LOS_NL_BASIC_INFO a inner join LOS_L_BASIC_INFO_I b on a.f_key=b.f_key where a.PID='" + PID + "' and a.applicanttype='CB'";
            String IndexQuery = ConfProperty.getQueryScript("getMobilewithCustIdQuery").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateCoborrowerData query::" + IndexQuery);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            if (dataResult.size() == 1) {
                ifr.setValue("P_CB_OD_CUSTOMER_ID", dataResult.get(0).get(0));
                String MobileNum = dataResult.get(0).get(1);
                String trimMobileNum = MobileNum.substring(2);
                ifr.setValue("P_CB_OD_MOBILE_NUMBER", trimMobileNum);
                Log.consoleLog(ifr, "populateCoborrowerData query::" + MobileNum);
                // added by vandana 20-03-2024
                String AccountId = Accountdetail(ifr);
                ifr.setValue("P_CB_OD_SalaryAccount", AccountId);
                Log.consoleLog(ifr, "populateCoborrowerData query::AccountId" + AccountId);

                String PortalFieldCoBorrower = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");
                String portalfieldCoBorrowerArr[] = PortalFieldCoBorrower.split(",");
                String queryCoBorrower = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);
                List<List<String>> resultOccF_KEY = ifr.getDataFromDB(queryCoBorrower);
                String OccF_Key = "";
                if (resultOccF_KEY.size() > 0) {
                    OccF_Key = resultOccF_KEY.get(0).get(0);
                }
                Log.consoleLog(ifr, "resultOccF_KEY.." + OccF_Key);
                String CoBorrowerFieldsQuery = ConfProperty.getQueryScript("PORTALOCCUPATIONINFODATACOBORROWER").replaceAll("#F_Key#", OccF_Key);
                Log.consoleLog(ifr, "CoBorrowerFieldsQuery.." + CoBorrowerFieldsQuery);
                List<List<String>> CoBorrowerFieldsQueryresult1 = ifr.getDataFromDB(CoBorrowerFieldsQuery);
                Log.consoleLog(ifr, "CoBorrowerFieldsQueryresult1.." + CoBorrowerFieldsQueryresult1);
                if (CoBorrowerFieldsQueryresult1.size() > 0) {
                    for (int i = 0; i < portalfieldCoBorrowerArr.length; i++) {
                        Log.consoleLog(ifr, "Inside if CoBorrowerFieldsQueryresult1 portalfieldCoBorrowerArr[i]==>" + i + " " + portalfieldCoBorrowerArr[i]);

                        ifr.setValue(portalfieldCoBorrowerArr[i], CoBorrowerFieldsQueryresult1.get(0).get(i).toString());

                    }
                }

            }
            //Added by Aravindh for Back Click validation 03/07/24
            String OccupationTypeCOB = ifr.getValue("P_CB_OD_Profile_COB").toString();
            Log.consoleLog(ifr, "populateCoborrowerData query:: Coborrower OccupationTypeCOB " + OccupationTypeCOB);
            String CoborrowerCustID = ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
            Log.consoleLog(ifr, "populateCoborrowerData query:: Coborrower CustID ::" + CoborrowerCustID);
            String CoborrowerMobileNo = ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();
            Log.consoleLog(ifr, "populateCoborrowerData query:: Coborrower Mobile No ::" + CoborrowerMobileNo);
            if (!CoborrowerCustID.isEmpty() && !CoborrowerMobileNo.isEmpty()) {
                Log.consoleLog(ifr, "populateCoborrowerData :: Coborrower CustID = " + CoborrowerCustID + " is already validated in Customer account summary Api");
                ifr.setStyle("P_CoObligant_OD_Section", "visible", "true");

                //if (OccupationTypeCOB.equalsIgnoreCase("Salaried") || OccupationTypeCOB.equalsIgnoreCase("NIE") || OccupationTypeCOB.equalsIgnoreCase("OTH")) {
                ifr.setStyle("P_OD_ValidateCoObligantCB", "disable", "true");
                ifr.setStyle("P_CB_OD_RelationshipWithCanara_COB", "disable", "true");
                ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths_COB", "disable", "true");
                ifr.setStyle("P_CB_OD_CUSTOMER_ID", "disable", "true");
                ifr.setStyle("P_CB_OD_MOBILE_NUMBER", "disable", "true");
                Log.consoleLog(ifr, "Validate Co-Obligant,Mobile No, Customer Id are disabled on Back Click");
                //}
            }
            //Added by Aravindh for Back Click validation 03/06/24
            PortalCustomCode PortalCC = new PortalCustomCode();
            PortalCC.OnChangeOccupationTypeCOB(ifr, "", "", "");
        } catch (Exception e) {
            Log.consoleLog(ifr, "error in populateCoborrowerData" + e);
            Log.errorLog(ifr, "error in populateCoborrowerData" + e);
        }
    }

//added by ishwarya 
    public String autoPopulateLoanDetailsDataCB(IFormReference ifr, String control, String event, String value) {

        String currentStep = pcm.setGetPortalStepName(ifr, value);
        Log.consoleLog(ifr, "currentStep CB::::: ");
        try {
            Log.consoleLog(ifr, "inside try block::::autoPopulateLoanDetailsDataCB::::: ");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            ifr.setValue("PIDValue", ProcessInsanceId);
            String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            String queryPeradd = ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsData-> CA:" + queryResadd);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsData-> PA:" + queryPeradd);

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
                ifr.setValue("P_CB_LD_EMAILID", EMAILID);
                ifr.setValue("P_CB_LD_MOBILENO", strmobile);
                ifr.setValue("P_CB_LD_COMMUNICATION_ADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + "," + state + "," + country + "," + pincode);
            }
            if (resultPeradd.size() > 0) {
                String addressLine1 = resultPeradd.get(0).get(0);
                String addressLine2 = resultPeradd.get(0).get(1);
                String addressLine3 = resultPeradd.get(0).get(2);
                String pincode = resultPeradd.get(0).get(3);
                ifr.setValue("P_CB_LD_PERMANENT_ADDRESS", addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + pincode);
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
                ifr.setValue("P_CB_H_CUSTOMERNAME1", Fullname.replace("  ", " "));
            }
            String Product = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataCB:getProductCode::" + Product);
            String ProductName = "";
            String ProductNameQuery = "SELECT ProductName FROM LOS_M_Product WHERE ProductCode='" + Product + "'";
            List<List<String>> ProductNameList = ifr.getDataFromDB(ProductNameQuery);
            if (!ProductNameList.isEmpty()) {
                ProductName = ProductNameList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Product Name::autoPopulateLoanDetailsDataCB:::" + ProductName);
            Log.consoleLog(ifr, "Before");
            ifr.setValue("P_CB_LD_Product", ProductName);
            Log.consoleLog(ifr, "After");
            ifr.setStyle("P_CB_LD_Product", "disable", "true");
            String subProduct = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataCB:getSubProductCode::" + subProduct);
            String subProductName = "";
            String subProductNameQuery = "SELECT SubProductName FROM LOS_M_SUBPRODUCT WHERE SubProductCode='" + subProduct + "'";
            List<List<String>> subProductNameList = cf.mExecuteQuery(ifr, subProductNameQuery, "subProductNameQuery:");
            if (!subProductNameList.isEmpty()) {
                subProductName = subProductNameList.get(0).get(0);
            }
            Log.consoleLog(ifr, "subProductName::autoPopulateLoanDetailsDataCB:::" + subProductName);
            ifr.setValue("P_CB_LD_SubProduct", subProductName);
            ifr.setStyle("P_CB_LD_SubProduct", "disable", "true");
            String ROI = pcm.mGetROICB(ifr);
            ifr.setValue("P_CB_LD_Total_ROI", ROI);
            ifr.setStyle("P_CB_LD_Total_ROI", "disable", "true");
            ifr.setValue("P_CB_LD_FRP", "0.75%");
            ifr.setStyle("P_CB_LD_FRP", "disable", "true");
            String RLLR = "";
            String RLLRQuery = "select final_rllr from LOS_MST_RLLR where base_type='RLLR'";
            List<List<String>> resultRLLRQuery = ifr.getDataFromDB(RLLRQuery);
            if (!resultRLLRQuery.isEmpty()) {
                RLLR = resultRLLRQuery.get(0).get(0);
            }
            Log.consoleLog(ifr, "RLLR::autoPopulateLoanDetailsDataCB:::" + RLLR);
            ifr.setValue("P_CB_LD_RLLR", RLLR);
            ifr.setStyle("P_CB_LD_RLLR", "disable", "true");
            String mobileNumber = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "mobileNumber::autoPopulateLoanDetailsDataCB:::" + mobileNumber);

            //saving data in knockoff-grid 
            String PARTY_TYPE = "";
            String RULE_NAME = "";
            String OUTPUT = "";
            String knockoffTemprules = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERY").replaceAll("#mobileNumber#", mobileNumber);
            Log.consoleLog(ifr, "Customersummary value  : " + knockoffTemprules);
            List<List<String>> dbdata = cf.mExecuteQuery(ifr, knockoffTemprules, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
            Log.consoleLog(ifr, "dbdata value  : " + dbdata);
            if (!dbdata.isEmpty()) {
                PARTY_TYPE = dbdata.get(0).get(0);
                RULE_NAME = dbdata.get(0).get(1);
                OUTPUT = dbdata.get(0).get(2);
            }
            String knockoffRules = ConfProperty.getQueryScript("KNOCKOFFCOUNT").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List<List<String>> knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            Log.consoleLog(ifr, "knockoffRules value  : " + knockoffRulesdata);
            String Fkey = Fkey(ifr, "B");
            if (knockoffRulesdata.isEmpty()) {
                String dataSavingGridQuery = ConfProperty.getQueryScript("KNOCKOFFINSERT").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#PARTY_TYPE#", PARTY_TYPE)
                        .replaceAll("#RULE_NAME#", RULE_NAME).replaceAll("#OUTPUT#", OUTPUT).replaceAll("#f_key#", Fkey).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextVal");
                Log.consoleLog(ifr, "dataSavingGridQuery::::" + dataSavingGridQuery);
                ifr.saveDataInDB(dataSavingGridQuery);
            }
            knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            String knockoffTemprules1 = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERY1").replaceAll("#mobileNumber#", mobileNumber);
            Log.consoleLog(ifr, "knockoffTemprules1 value  : " + knockoffTemprules1);
            List<List<String>> dataSaveknockoffGridData = cf.mExecuteQuery(ifr, knockoffTemprules1, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
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
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside autoPopulateLoanDetailsDataCB::::" + e);
            Log.errorLog(ifr, "Exception inside autoPopulateLoanDetailsDataCB::::" + e);
            return pcm.returnError(ifr);
        }
        return currentStep;
    }

    //added by ishwarya 
    public String mImpOnClickLoanDetailsDataCB(IFormReference ifr, String control, String event, String value) {
        JSONObject message = new JSONObject();

        try {
            Log.consoleLog(ifr, "inside try block::::mImpOnClickLoanDetailsDataCB::::: ");

            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId::mImpOnClickLoanDetailsDataCB:::" + CustomerId);
            String MobileNo = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "MobileNo:::mImpOnClickLoanDetailsDataCB:::" + MobileNo);
            String response = cas.getCustomerAccountParams_CB(ifr, MobileNo);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB:::mImpOnClickLoanDetailsDataCB:::>" + response);
            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataCB::" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataCB::" + subProductCode);
            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
            String PANNUMBER = OutputJSON.get("PanNumber").toString();
            String DateofBirth = OutputJSON.get("DateofBirth").toString();

            Log.consoleLog(ifr, "AADHARNUMBER::mImpOnClickLoanDetailsDataCB::" + AADHARNUMBER);
            Log.consoleLog(ifr, "PANNUMBER::mImpOnClickLoanDetailsDataCB::" + PANNUMBER);
            Log.consoleLog(ifr, "DateofBirth::mImpOnClickLoanDetailsDataCB::" + DateofBirth);
            String ReqLoamAmount = ifr.getValue("P_CB_LD_Requested_Loan_Amount").toString();
            if (Integer.parseInt(ReqLoamAmount) < 100000) {
                Log.consoleLog(ifr, "inside requested loan amount is less than 1 lakh:::::");
                String cb = mCallBureauLP(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataCB::::");
                    populateLoanDetailsCB(ifr, control, event, value);
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataCB::::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else if (Integer.parseInt(ReqLoamAmount) >= 100000) {
                Log.consoleLog(ifr, "inside requested loan amount is greater than 1 lakh:::::");
                String cb = mCallBureauLP(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }

                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataCB::::");
                    String EX = mCallBureauLP(ifr, "EX", AADHARNUMBER, "B", ReqLoamAmount);
                    if (EX.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    }
                    decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "Ex", "B");

                    Log.consoleLog(ifr, "decision2/EX::" + decision);
                    if (decision.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    } else if (decision.equalsIgnoreCase("Approve")) {
                        Log.consoleLog(ifr, "EXPERIAN Passed Successfully::mImpOnClickLoanDetailsDataCB:::");
                        populateLoanDetailsCB(ifr, control, event, value);
                    } else {
                        Log.consoleLog(ifr, "Experian Failed:::mImpOnClickLoanDetailsDataCB:::");
                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                        message.put("eflag", "false");
                        return message.toString();
                    }
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataCB::::");
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
            Log.consoleLog(ifr, "Exception inside mImpOnClickLoanDetailsDataCB::::" + e);
            Log.errorLog(ifr, "Exception inside mImpOnClickLoanDetailsDataCB::::" + e);
            return pcm.returnError(ifr);
        }
        return "";
    }
    // added by vandana for elibilityRuleForLeadCapture

    public String elibilityRuleForLeadCapture(IFormReference ifr, String applicantType) {
        String finalEligibleAmount = "";
        try {
            Log.consoleLog(ifr, "inside Budget eligibility brms:::");
            String loanTenure = null;
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String productCode = pcm.getProductCode(ifr);
            //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='S22'";
            String tenureData_Query = ConfProperty.getQueryScript("getmaxTenureQuery");
            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
            if (list1.size() > 0) {
                loanTenure = list1.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            String loanROI = pcm.mGetROICB(ifr);
            Log.consoleLog(ifr, "roi : " + loanROI);
            String obligation = null;
            String obligation_Query = ConfProperty.getQueryScript("getTotalAmountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);;
            Log.consoleLog(ifr, "obligation_Query===>" + obligation_Query);
            List<List<String>> list = cf.mExecuteQuery(ifr, obligation_Query, "obligation_Query:");
            if (list.size() > 0) {
                obligation = list.get(0).get(0);
            }
            String cibiloblig = obligation.equalsIgnoreCase("") ? "0" : obligation;

            String schemeID = pcm.getBaseSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);

            String Prodcapping = null;
            String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
            if (ProdcappingList.size() > 0) {
                Prodcapping = ProdcappingList.get(0).get(0);
            }
            String grosssalary = "";
            String deductionmonth = "";
            String fkey = Fkey(ifr, applicantType);
            String occupationType = "select  GROSSSALARY,DEDUCTIONMONTH  from los_nl_occupation_info where f_key='" + fkey + "'";
            List<List<String>> OccupationType = ifr.getDataFromDB(occupationType);
            if (!OccupationType.isEmpty()) {
                grosssalary = OccupationType.get(0).get(0);
                deductionmonth = OccupationType.get(0).get(1);
                Log.consoleLog(ifr, "grosssalary:::: " + grosssalary);
                Log.consoleLog(ifr, "deductionmonth:::: " + deductionmonth);
            }
            Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);
            String reqAmount = "";
            String ReqAmount = "select REQLOANAMT from los_nl_proposed_facility where pid='" + ProcessInstanceId + "'";
            List<List<String>> RequestAmount = ifr.getDataFromDB(ReqAmount);
            if (!RequestAmount.isEmpty()) {
                reqAmount = RequestAmount.get(0).get(0);

                Log.consoleLog(ifr, "reqAmount:::: " + reqAmount);
            }
            HashMap hm = new HashMap();
            hm.put("cibiloblig", cibiloblig);
            Log.consoleLog(ifr, "cibiloblig===>" + cibiloblig);
            hm.put("tenure", String.valueOf(loanTenure));
            hm.put("roi", String.valueOf(loanROI));
            hm.put("loancap", Prodcapping);

            hm.put("grosssalary", grosssalary);
            hm.put("reqAmount", reqAmount);

            hm.put("deductionmonth", deductionmonth);
            //String finalelig = pcm.getAmountForEligibilityCheckCB(ifr, hm);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);
            BudgetBkoffCustomCode budgetBO = new BudgetBkoffCustomCode();
            String finalelig = budgetBO.getAmountForInprincipleDataSaveBO(ifr, hm);
            Log.consoleLog(ifr, "finalelig===>" + finalelig);
            if (finalelig.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
            String Query = "SELECT COUNT(*) FROM LOS_L_FINAL_ELIGIBILITY "
                    + "WHERE PID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query===>" + Query);
            List Result = ifr.getDataFromDB(Query);
            String Count = Result.toString().replace("[", "").replace("]", "");
            if (Count.equalsIgnoreCase("")) {
                Count = "0";
            }
            if (Integer.parseInt(Count) == 0) {
                //String Query2 = "INSERT INTO LOS_L_FINAL_ELIGIBILITY (PID) VALUES ('" + ProcessInstanceId + "')";
                String Query2 = ConfProperty.getQueryScript("insertQueryforPIDinFinalEligibility").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#finalelig#", finalelig);
                Log.consoleLog(ifr, "Query1===>" + Query2);
                ifr.saveDataInDB(Query2);
            }
            //String Query2 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
            // + "SET LOAN_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
            String Query2 = ConfProperty.getQueryScript("updateQueryforLoanamtinFinalEligibility").replaceAll("#finalelig#", finalelig).replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            Log.consoleLog(ifr, "Query2===>" + Query2);
            ifr.saveDataInDB(Query2);
            //String Query3 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
            //+ "SET IN_PRINCIPLE_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
            String Query3 = ConfProperty.getQueryScript("updateQueryforPrincipleamtinFinalEligibility").replaceAll("#finalelig#", finalelig).replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            Log.consoleLog(ifr, "Query3===>" + Query3);
            ifr.saveDataInDB(Query3);

            String finalAmountInParams = productCode + "," + finalelig;

            finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");

        } catch (Exception e) {

            Log.consoleLog(ifr, "error in elibilityRuleForCoBorrower " + e);
        }
        return finalEligibleAmount;

    }

    public String scoreCardDecisionForCB(IFormReference ifr) {
        String scoreCardDecisionCB = "";
        try {
            Log.consoleLog(ifr, "inside Co-Borrower scorecard brms:::");
            String npa_inp = "";
            String bscore = "";
            String overduedays_inp = "";
            String existcust_inp = "";
            String overduedaysupto = "";
            String monthlydeduction = "0";
            int maxOccurrences = 0;
            String paymentHistoryIp = "";
            String maxOccurrencesNumber = null;
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String APIResponse = mGetAPIData(ifr);

            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String Age = cf.getJsonValue(obj, "Age");
            String Years = "0";
            Years = cf.getJsonValue(obj, "Years");
            String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
            writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
            String loanTenure = null;
            //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='S22'";
//            String tenureData_Query = ConfProperty.getQueryScript("getmaxTenureQuery");
//            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
//            if (list1.size() > 0) {
//                loanTenure = list1.get(0).get(0);
//            }
//            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            //modified by vandana on 26/06/2024 for scorecard starts
            // String occupationtype = ifr.getValue("P_CB_OD_Profile_COB").toString();
            String occupationtype = "";
            String category = "";
            String grossSalary = "";
            String deductionSalary = "";
            String expYears = "";
            //  String fkey="select * from LOS_NL_BASIC_INFO where pid='"+ProcessInstanceId+"' and APPLICANTTYPE='CB'";
            String fkey = Fkey(ifr, "CB");
            String occupationType = "select OCCUPATIONTYPE from los_nl_occupation_info where f_key='" + fkey + "'";
            List<List<String>> OccupationType = ifr.getDataFromDB(occupationType);
            if (!OccupationType.isEmpty()) {
                occupationtype = OccupationType.get(0).get(0);
                Log.consoleLog(ifr, "occupationtype:::: " + occupationtype);
            }

            if (occupationtype.equalsIgnoreCase("NIE")) {
                Log.consoleLog(ifr, "Non Income Earners :::: ");
//                category = ifr.getValue("P_CB_OD_Category").toString();
//                grossSalary = ifr.getValue("P_CB_OD_GrossSalary").toString();
//                deductionSalary = ifr.getValue("P_CB_OD_DeductionFromSalary").toString();
//                expYears = ifr.getValue("P_CB_OD_ExperienceYear").toString();//modified by vandana on 26/06/2024 for scorecard
//
//                String loantenure = ifr.getValue("P_CB_OD_DATEOFSUPERANNUATION").toString();
//                Log.consoleLog(ifr, "P_CB_OD_DATEOFSUPERANNUATION  : " + loantenure);
                String F_key = Fkey(ifr, "B");
                String occupationDetails = "select GROSSSALARY,DEDUCTIONMONTH,CATEGORY,TO_CHAR(DATEOFSUPERANNUATION,'DD/MM/YYYY'),CURRENTINYEARS from los_nl_occupation_info where f_key='" + F_key + "'";
                List<List<String>> occupationdetails = ifr.getDataFromDB(occupationDetails);
                if (!occupationdetails.isEmpty()) {
                    grossSalary = occupationdetails.get(0).get(0);
                    deductionSalary = occupationdetails.get(0).get(1);
                    category = occupationdetails.get(0).get(2);
                    String loantenure = occupationdetails.get(0).get(3);
                    expYears = occupationdetails.get(0).get(4);
                    Log.consoleLog(ifr, "occupationtype:::: " + occupationtype);
                    int months = differenceInMonths(ifr, loantenure);
                    loanTenure = String.valueOf(months);
                }

            } else {
                Log.consoleLog(ifr, "scoreCardDecisionForCB:::::entered income earners");
//                String loantenure = ifr.getValue("P_CB_OD_DATEOFSUPERANNUATION_COB").toString();
//                Log.consoleLog(ifr, "P_CB_OD_DATEOFSUPERANNUATION_COB  : " + loantenure);
//                int months = differenceInMonths(ifr, loantenure);
//                loanTenure = String.valueOf(months);
//                Log.consoleLog(ifr, "P_CB_OD_DATEOFSUPERANNUATION_COB loanTenure : " + loanTenure);
//                category = ifr.getValue("P_CB_OD_Category_COB").toString();
//                grossSalary = ifr.getValue("P_CB_OD_GrossSalary_COB").toString();
//                deductionSalary = ifr.getValue("P_CB_OD_DeductionFromSalary_COB").toString();
//                expYears = ifr.getValue("P_CB_OD_ExperienceYear_COB").toString();
                String occupationDetails = "select GROSSSALARY,DEDUCTIONMONTH,CATEGORY,TO_CHAR(DATEOFSUPERANNUATION,'DD/MM/YYYY'),CURRENTINYEARS from los_nl_occupation_info where f_key='" + fkey + "'";
                List<List<String>> occupationdetails = ifr.getDataFromDB(occupationDetails);
                if (!occupationdetails.isEmpty()) {
                    grossSalary = occupationdetails.get(0).get(0);
                    Log.consoleLog(ifr, "grossSalary:::: " + grossSalary);
                    deductionSalary = occupationdetails.get(0).get(1);
                    Log.consoleLog(ifr, "grossSalary:::: " + grossSalary);
                    category = occupationdetails.get(0).get(2);
                    String loantenure = occupationdetails.get(0).get(3);
                    expYears = occupationdetails.get(0).get(4);
                    Log.consoleLog(ifr, "occupationtype:::: " + occupationtype);
                    int months = differenceInMonths(ifr, loantenure);
                    loanTenure = String.valueOf(months);
                    Log.consoleLog(ifr, "loanTenure IN MONTHS:::: " + loanTenure);
                }
            }
            ///modified by vandana on 26/06/2024 for scorecard ends
            Log.consoleLog(ifr, "scoreCardDecisionForCB::::calling loanROI" + loanTenure);
            String loanROI = pcm.mGetROICB(ifr);

            Log.consoleLog(ifr, "roi : " + loanROI);
//            String accountstatus = "";
//            String AccountHoldertypeCode = "";
            String settleHistory = "";
            String GUARANTORNPAINP = "";
            String GUARANTORWRITEOFFSETTLEDHIST = "";
            String BureauDataResponseBorrower = objcm.getMaxCICScoreDatas(ifr, "CB");
            Log.consoleLog(ifr, "scoreCardDecisionForCB::: getMaxCICScoreDatas response:::: " + BureauDataResponseBorrower);
            String[] bSplitter = BureauDataResponseBorrower.split("-");
            String bureauTypeB = bSplitter[0];
            String ApplicantTypeB = bSplitter[1];
            monthlydeduction = bSplitter[2];
            Log.consoleLog(ifr, "scoreCardDecisionForCB::: monthlydeduction value " + monthlydeduction);
            bscore = bSplitter[3];
            paymentHistoryIp = bSplitter[4];
            npa_inp = bSplitter[5];
            settleHistory = bSplitter[6];
            GUARANTORNPAINP = bSplitter[7];
            GUARANTORWRITEOFFSETTLEDHIST = bSplitter[8];

            //npa_inp = (npa_inp.equalsIgnoreCase("NA") || npa_inp.equalsIgnoreCase("")) ? "No" : "Yes";
            Log.consoleLog(ifr, "npa_inp:::: " + npa_inp);
            Log.consoleLog(ifr, "settleHistory:::: " + settleHistory);
            Log.consoleLog(ifr, "GUARANTORNPAINP:::: " + GUARANTORNPAINP);
            Log.consoleLog(ifr, "GUARANTORWRITEOFFSETTLEDHIST:::: " + GUARANTORWRITEOFFSETTLEDHIST);
            monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;
            //String scoreQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "'";
//            String scoreQuery = ConfProperty.getQueryScript("getscoreQueryCB").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
//            List<List<String>> Result1 = cf.mExecuteQuery(ifr, scoreQuery, "Exexuting the Query value ");
//            if (!Result1.isEmpty()) {
//
//                bscore = Result1.get(0).get(0);
//                npa_inp = Result1.get(0).get(1);
//                npa_inp = npa_inp.equalsIgnoreCase("") ? "No" : "Yes";
//                monthlydeduction = Result1.get(0).get(3);
//                monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;
//                Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
//                Log.consoleLog(ifr, "bscore:::: " + bscore);
//            }
            Log.consoleLog(ifr, "before query");
            //modified by vandana on 26/06/2024 for scorecard
            // String test = "011012013014025000000000000077061062069000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
//            String paymentquery = "SELECT NVL(REGEXP_REPLACE(PAYHISTORYCOMBINED, '[^0-9]', ''), 0) AS replaced_column FROM LOS_CAN_IBPS_BUREAUCHECK where PROCESSINSTANCEID='" + ProcessInstanceId + "'and APPLICANT_TYPE='CB'";
//
//            List<List<String>> paymentHistory = ifr.getDataFromDB(paymentquery);
//            if (!paymentHistory.isEmpty()) {
//                paymentHistoryIp = paymentHistory.get(0).get(0);
//                Log.consoleLog(ifr, "PAYMENTHISTORY:::: " + paymentHistoryIp);
//            }
            Log.consoleLog(ifr, "PAYMENTHISTORY:::: " + paymentHistoryIp);
            String[] splitPaymentHistory = paymentHistoryIp.split("(?<=\\G.{3})"); // Splits every 3 chars without adding a delimiter
            Log.consoleLog(ifr, "parts:::: " + splitPaymentHistory);
            int maxPaymentHistory = Integer.MIN_VALUE;
            Map<Integer, Integer> countMap = new HashMap<>();

            // Find maxPaymentHistory and count occurrences
            for (String part : splitPaymentHistory) {
                int values = Integer.parseInt(part); // Assuming decimal integers
                countMap.put(values, countMap.getOrDefault(values, 0) + 1);
                maxPaymentHistory = Math.max(maxPaymentHistory, values);
            }

            // Find the range containing the max value
            String highestRangeKey = getRangeKey(maxPaymentHistory);

            // Count numbers within the highest range
            int countInHighestRange = getCountInRange(countMap, highestRangeKey);

            String maxOccurrencesip = String.valueOf(countInHighestRange);
            maxOccurrencesNumber = String.valueOf(maxPaymentHistory);
            overduedays_inp = maxOccurrencesNumber;
            Log.consoleLog(ifr, "overduedays_inp:::: " + overduedays_inp);
            overduedaysupto = maxOccurrencesip;
            Log.consoleLog(ifr, "overduedaysupto :::: " + overduedaysupto);
            String overduedays_inps = overduedays_inp.equalsIgnoreCase("0") ? "No" : "yes";
            Log.consoleLog(ifr, "Before netincome :::: ");
            String residence_inp = "";
            String natureOfSecurity = "";
            String recovery = "";
            String ExistInpRange = "";
            //String netIncome = ifr.getValue("P_CB_OD_NetIncome_COB").toString();
            //modified by vandana on 26/06/2024 for scorecard
//            String residence_inp = ifr.getValue("P_CB_OD_Residence_COB").toString();
//            String natureOfSecurity = ifr.getValue("P_CB_OD_NatureOfSecurity_COB").toString();
//            String recovery = ifr.getValue("P_CB_OD_RecoveryMechanism_COB").toString();//modified by vandana on 26/06/2024 for scorecard
//            String ExistInpRange = ifr.getValue("P_CB_OD_RelationshipWithCanara_COB").toString();
            String getOccupationDetails = "select NATURE_OF_SECURITY,RECOVERY_MECHANISM,RESIDENCE,RELATIONSHIPCANARA from los_nl_occupation_info where f_key='" + fkey + "'";
            List<List<String>> getOccupationdetails = ifr.getDataFromDB(getOccupationDetails);
            if (!getOccupationdetails.isEmpty()) {
                natureOfSecurity = getOccupationdetails.get(0).get(0);
                recovery = getOccupationdetails.get(0).get(1);
                residence_inp = getOccupationdetails.get(0).get(2);
                ExistInpRange = getOccupationdetails.get(0).get(3);

            }
            //  String AccountHoldertypeCode = cf.getJsonValue(obj, "AccountHoldertypeCode");//
//            AccountHoldertypeCode = AccountHoldertypeCode.equalsIgnoreCase("7") ? "Yes" : "No";
//            String guarantorwriteoff = AccountHoldertypeCode;
            //String accountstatus = cf.getJsonValue(obj, "Account_Status");
//            if ((accountstatus.equalsIgnoreCase("00") || accountstatus.equalsIgnoreCase("40") || accountstatus.equalsIgnoreCase("52") || accountstatus.equalsIgnoreCase("13")
//                    || accountstatus.equalsIgnoreCase("15") || accountstatus.equalsIgnoreCase("16") || accountstatus.equalsIgnoreCase("17")
//                    || accountstatus.equalsIgnoreCase("12") || accountstatus.equalsIgnoreCase("11") || accountstatus.equalsIgnoreCase("71") || accountstatus.equalsIgnoreCase("78")
//                    || accountstatus.equalsIgnoreCase("80") || accountstatus.equalsIgnoreCase("82") || accountstatus.equalsIgnoreCase("83") || accountstatus.equalsIgnoreCase("84")
//                    || accountstatus.equalsIgnoreCase("DEFAULTVALUE") || accountstatus.equalsIgnoreCase("21") || accountstatus.equalsIgnoreCase("22") || accountstatus.equalsIgnoreCase("23")
//                    || accountstatus.equalsIgnoreCase("24") || accountstatus.equalsIgnoreCase("25"))) {
//
//                accountstatus = "No";
//            } else {
//                accountstatus = "Yes";
//            }
//            String guarantornpa_inp;
//            if (AccountHoldertypeCode.equalsIgnoreCase("Yes") && accountstatus.equalsIgnoreCase("Yes")) {
//                guarantornpa_inp = "Yes";
//            } else {
//                guarantornpa_inp = "No";
//            }
//            String nparestmonths_inpo = accountstatus;
//            String settledhist_inp = accountstatus;
            String guarantorwriteoff = GUARANTORWRITEOFFSETTLEDHIST;

            String nparestmonths_inpo = settleHistory;

            String settledhist_inp = settleHistory;

            String writeOff = settleHistory;

            String guarantornpa_inp = GUARANTORNPAINP;

            String guarantorsettledhist_inp = GUARANTORWRITEOFFSETTLEDHIST;
            Log.consoleLog(ifr, "scoreCardDecisionForCB:::::guarantorsettledhist_inp" + guarantorsettledhist_inp);
            String Updatequery = "Update LOS_NL_BASIC_INFO set OVERDUEINCREDITHISTORY='" + overduedays_inps + "' , SETTLEDACCOUNTINCREDITHISTORY ='" + settledhist_inp + "' where PID='" + ProcessInstanceId + "' and APPLICANTTYPE='CB'";
            ifr.saveDataInDB(Updatequery);
            //String existcust_inpQuery = "SELECT EXISTINGCUSTOMER FROM LOS_NL_BASIC_INFO where PID = '" + ProcessInstanceId + "' ";
            String existcust_inpQuery = ConfProperty.getQueryScript("getExistingCustomer").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> Results = cf.mExecuteQuery(ifr, existcust_inpQuery, "Exitingquery");
            if (!Results.isEmpty()) {
                existcust_inp = Results.get(0).get(0);
                existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
            }
            //added by vandana
            String inPrincipleAmount = "";
            String amount_Query = "SELECT round(IN_PRINCIPLE_AMOUNT) FROM LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInstanceId + "'";
            List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

            if (!Results1.isEmpty()) {
                inPrincipleAmount = Results1.get(0).get(0);
                Log.consoleLog(ifr, "inPrincipleAmount " + inPrincipleAmount);
            }
            Log.consoleLog(ifr, "Before conversion ");
            int grossSalaryip1 = Integer.parseInt(grossSalary);
//                                        int deductionSalaryip=Integer.parseInt(deductionSalary);
//                                        int MonthlyDeductionip=Integer.parseInt(monthlydeduction);
            //int netIncomeip1 = Integer.parseInt(netIncome);
            Log.consoleLog(ifr, "Before bigdecimal conversion  ");
            BigDecimal deductionSalaryip = new BigDecimal(deductionSalary);
            Log.consoleLog(ifr, "after bigdecimal conversion deductionSalary" + deductionSalaryip);
            BigDecimal grossSalaryip = new BigDecimal(grossSalary);
            Log.consoleLog(ifr, "after bigdecimal conversion grossSalaryip " + grossSalaryip);
            BigDecimal MonthlyDeductionip = new BigDecimal(monthlydeduction);
            Log.consoleLog(ifr, "scoreCardDecisionForCB:::::::after bigdecimal conversion MonthlyDeductionip " + MonthlyDeductionip);
            BigDecimal loanamount = new BigDecimal(inPrincipleAmount);
            Log.consoleLog(ifr, "after bigdecimal conversion loanamount" + loanamount);
            BigDecimal ftRoi = new BigDecimal(loanROI);
            Log.consoleLog(ifr, "after bigdecimal conversion ftRoi " + ftRoi);
            int loanTenures = Integer.parseInt(loanTenure);
            BigDecimal perposedEmi = calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
            Log.consoleLog(ifr, "after bigdecimal conversion perposedEmi" + perposedEmi);
            BigDecimal netIncomeip = grossSalaryip.subtract(deductionSalaryip).subtract(MonthlyDeductionip).subtract(perposedEmi);

            Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip" + netIncomeip);

            double netIncomeip1 = netIncomeip.intValue();
            Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip1" + netIncomeip1);

            double nethomeins_inprange1 = (netIncomeip1 / grossSalaryip1) * 100;
            double doubleValue = nethomeins_inprange1;
            int nethomeins_inprange = (int) doubleValue;
            Log.consoleLog(ifr, "after bigdecimal conversion nethomeins_inprange" + nethomeins_inprange);
            int anninc_inp = grossSalaryip1 * 12;
            Log.consoleLog(ifr, "bscore:::" + bscore + ",overduedays_inp:::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff:::"
                    + guarantorwriteoff + ",npa_inp:::" + npa_inp + ",nparestmonths_inpo:::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",writeOffPresent:::" + writeOffPresent
                    + ",nethomeins_inprange:::" + nethomeins_inprange + ",category:::" + category + ",anninc_inp:::" + anninc_inp + ",grossSalary:::" + grossSalary + ",expYears:::" + expYears + ",existcust_inp:::" + existcust_inp
                    + ",ExistInpRange::" + ExistInpRange + ",natureOfSecurity:::" + natureOfSecurity + ",residence_inp:::" + residence_inp + ",recovery:::" + recovery);
            String scorecardInParams = "";
            // VLPortalCustomCode vpcc=new VLPortalCustomCode();

            if (!bscore.equalsIgnoreCase("I")) {

                scorecardInParams = bscore + "," + overduedays_inp + "," + overduedaysupto + "," + guarantorsettledhist_inp + "," + guarantornpa_inp + ","
                        + guarantorwriteoff + "," + npa_inp + "," + nparestmonths_inpo + "," + settledhist_inp + "," + writeOff
                        + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                        + "," + ExistInpRange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                scoreCardDecisionCB = checkScoreCardBudget(ifr, "CB_SCORECARD", scorecardInParams, "totalgrade_out", "CB");
            } else {

                scorecardInParams = npa_inp + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                        + "," + ExistInpRange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                scoreCardDecisionCB = checkScoreCardBudget(ifr, "CB_ScoreCard_CICImmune", scorecardInParams, "totalgrade_out", "CB");

            }
            if (scoreCardDecisionCB.equalsIgnoreCase(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                return pcm.returnError(ifr);
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in scorecardFor Co-Applicant brms::" + e);
            return pcm.returnError(ifr);
        }
        return scoreCardDecisionCB;
    }

    public void mSetLoanDetailsData(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "inside try block mSetLoanDetailsData:::: ");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String PortalFieldLoanDetails = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCB");
            String portalfieldLoanDetailsArr[] = PortalFieldLoanDetails.split(",");

            String LoanDetailsFieldsQuery = ConfProperty.getQueryScript("PORTALLOANDETAILSCB").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "LoanDetailsFieldsQuery.." + LoanDetailsFieldsQuery);
            List<List<String>> LoanDetailsQueryresult = ifr.getDataFromDB(LoanDetailsFieldsQuery);
            Log.consoleLog(ifr, "LoanDetailsQueryresult.." + LoanDetailsQueryresult);
            if (LoanDetailsQueryresult.size() > 0) {
                for (int i = 0; i < portalfieldLoanDetailsArr.length; i++) {
                    Log.consoleLog(ifr, "Inside if mSetLoanDetailsData portalfieldCoBorrowerArr[i]==>" + i + " " + portalfieldLoanDetailsArr[i]);
                    ifr.setValue(portalfieldLoanDetailsArr[i], LoanDetailsQueryresult.get(0).get(i));

                }
            }
            String ROIType = ifr.getValue("P_CB_LD_ROI_Type").toString();
            if (ROIType.equalsIgnoreCase("Fixed")) {
                ifr.setStyle("P_CB_LD_FRP", "visible", "true");
                ifr.setStyle("P_CB_LD_FRP", "mandatory", "true");
            } else {
                ifr.setStyle("P_CB_LD_FRP", "visible", "false");
            }
            String loanPurpose = ifr.getValue("P_CB_LD_Purpose").toString();
            if (loanPurpose.equalsIgnoreCase("OTH")) {
                ifr.setStyle("P_CB_LD_Purpose_Others", "visible", "true");
            } else {
                ifr.setStyle("P_CB_LD_Purpose_Others", "visible", "false");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "error in mSetLoanDetailsData" + e);
            Log.errorLog(ifr, "error in mSetLoanDetailsData" + e);
        }
    }

    public String onChangeLoanAmount(IFormReference ifr) {
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "inside try block onChangeLoanAmount:::: ");
            String reqAmount = ifr.getValue("P_CB_LD_Requested_Loan_Amount").toString();
            String LoanDetailsFieldsQuery = ConfProperty.getQueryScript("GETREQLOANAMT");
            Log.consoleLog(ifr, "LoanDetailsFieldsQuery.." + LoanDetailsFieldsQuery);
            List<List<String>> LoanDetailsQueryresult = ifr.getDataFromDB(LoanDetailsFieldsQuery);
            Log.consoleLog(ifr, "reqAmount:::: " + reqAmount);
            int minAmount = Integer.parseInt(LoanDetailsQueryresult.get(0).get(0));
            Log.consoleLog(ifr, "inside if block onChangeLoanAmount:::: ");
            if (Integer.parseInt(reqAmount) < minAmount) {
                Log.consoleLog(ifr, "inside if block onChangeLoanAmount:::: ");
                message.put("showMessage", cf.showMessage(ifr, "P_CB_LD_Requested_Loan_Amount", "error", "Please Enter the Requested Loan Amount greater than 0!"));
                ifr.setValue("P_CB_LD_Requested_Loan_Amount", "");
                return message.toString();
            } else {
                return "";
            }
        } catch (NumberFormatException e) {
            Log.errorLog(ifr, "Error in onChangeLoanAmount" + e);
            return pcm.returnError(ifr);
        }
    }

    public String populateLoanDetailsCB(IFormReference ifr, String control, String event, String value) {

        try {
            Log.consoleLog(ifr, "inside try block populateLoanDetailsCB:::: ");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTLOANDETAILS").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "LoanDetails query::" + IndexQuery);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            Log.consoleLog(ifr, "dataResult query::" + dataResult);
            String datacount = dataResult.get(0).get(0);
            if (Integer.parseInt(datacount) == 0) {
                Log.consoleLog(ifr, "inside if LoanDetails query::");
                String portalFields = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCB");
                Log.consoleLog(ifr, "portalFields Loan Details::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, Arrays.toString(portalFieldStr) + "portalFieldStr[]:populateLoanDetailsCB:");
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1:populateLoanDetailsCB:" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                    Log.consoleLog(ifr, "populateLoanDetailsCB: portalValue[i] => " + portalValue[i].toString());

                }
                Log.consoleLog(ifr, "portalFields array portalValue[i]:populateLoanDetailsCB:" + Arrays.toString(portalValue));
                String insertQuery = ConfProperty.getQueryScript("insertLoanDetailsDataCB")
                        .replaceAll("#ProcessInstanceId#", PID).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "insertQuery for Loan Details Info::" + insertQuery);
                ifr.saveDataInDB(insertQuery);
            } else {
                Log.consoleLog(ifr, "inside else LoanDetails query::");
                String portalFields = ConfProperty.getCommonPropertyValue("PortalLoanDetailsCB");
                Log.consoleLog(ifr, "portalFields Loan Details::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, Arrays.toString(portalFieldStr) + "portalFieldStr[]:populateLoanDetailsCB:");
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1:populateLoanDetailsCB:" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                String updateQuery = ConfProperty.getQueryScript("updateLoanDetailsDataCB")
                        .replaceAll("#ProcessInstanceId#", PID).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "update for Loan Details Info::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "populateLoanDetailsCB::" + count);
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "error in populateLoanDetailsCB" + e);
            Log.errorLog(ifr, "error in populateLoanDetailsCB" + e);
        }
        return "";
    }

    public void populatecicScore(IFormReference ifr, String applicanttype) {
        String fkey = Fkey(ifr, applicanttype);
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "EXPERIAN Passed Successfully:::");
        //Bureo check data population Start added by vishal
        //String bueroTableQuery = "select PID from LOS_NL_CB_Details where PID='" + ProcessInstanceId + "' and applicantcode='"+applicanttype+"' ";
        String bueroTableQuery = ConfProperty.getQueryScript("getbueroTableQueryAt").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#applicanttype#", applicanttype);
        Log.consoleLog(ifr, "bueroTableQuery ::" + bueroTableQuery);
        List<List<String>> bueroTableData = ifr.getDataFromDB(bueroTableQuery);
        String dataSaveBueroCheckGridQuery = "";
        //getbueroTableQuery=select PID from LOS_NL_CB_Details where PID='#ProcessInstanceId#' and APPLICANTCODE='#applicanttype#'
        if (bueroTableData.isEmpty()) {
            Log.consoleLog(ifr, "bueroTableData is empty::::" + bueroTableData);
            //String dataSaveBueroCheckGridQuery = "select distinct BureauType,Exp_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK where ProcessInstanceId='" + ProcessInstanceId + "'";
            dataSaveBueroCheckGridQuery = ConfProperty.getQueryScript("getdataSaveBueroCheckGridQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#applicanttype#", applicanttype);
            List<List<String>> dataSaveBueroCheckGridData = ifr.getDataFromDB(dataSaveBueroCheckGridQuery);
            Log.consoleLog(ifr, "dataSaveBueroCheckGridData:::" + dataSaveBueroCheckGridData);
            String BureauType[] = new String[dataSaveBueroCheckGridData.size()];
            String Exp_CBSCORE[] = new String[dataSaveBueroCheckGridData.size()];
            if (!dataSaveBueroCheckGridData.isEmpty()) {
                Log.consoleLog(ifr, "dataSaveBueroCheckGridData is empty::::" + dataSaveBueroCheckGridData);

                for (int i = 0; i < dataSaveBueroCheckGridData.size(); i++) {
                    Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData BureauType::" + dataSaveBueroCheckGridData.get(i).get(0));
                    BureauType[i] = dataSaveBueroCheckGridData.get(i).get(0);
                    Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData:: Exp_CBSCORE" + dataSaveBueroCheckGridData.get(i).get(1));
                    Exp_CBSCORE[i] = dataSaveBueroCheckGridData.get(i).get(1);
                    Log.consoleLog(ifr, "BureauType:::Exp_CBSCORE" + BureauType[i] + Exp_CBSCORE[i]);
                }
                //String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='B'  and PID='" + ProcessInstanceId + "'";
                String borrowerQuery = ConfProperty.getQueryScript("getinsertionOrderID").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#applicanttype#", applicanttype);
                List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                if (!borrowerQueryData.isEmpty()) {

                    for (int i = 0; i < BureauType.length; i++) {
                        String brdata = borrowerQueryData.get(0).get(0);
                        String StrBureScore = Exp_CBSCORE[i];
                        String strBureauType = BureauType[i];
                        String dataSavingGridQuery = ConfProperty.getQueryScript("insertQueryforDataSavingGrid").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#brdata#", brdata).replaceAll("#strBureauType#", strBureauType).replaceAll("#StrBureScore#", StrBureScore).replace("#fkey#", fkey).replaceAll("#applicantcode#", applicanttype);
                        Log.consoleLog(ifr, "dataSavingGridQuery::::" + dataSavingGridQuery);
                        ifr.saveDataInDB(dataSavingGridQuery);
                    }
                }
            }

        } //modified by sharon on 20/06/2024 for avoid duplicate entry
        else {
            Log.consoleLog(ifr, "bueroTableData not empty::::" + bueroTableData);
            dataSaveBueroCheckGridQuery = ConfProperty.getQueryScript("getdataSaveBueroCheckGridQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#applicanttype#", applicanttype);
            List<List<String>> dataSaveBueroCheckGridData = ifr.getDataFromDB(dataSaveBueroCheckGridQuery);
            Log.consoleLog(ifr, "dataSaveBueroCheckGridData:::" + dataSaveBueroCheckGridData);
            String BureauType[] = new String[dataSaveBueroCheckGridData.size()];
            String Exp_CBSCORE[] = new String[dataSaveBueroCheckGridData.size()];
            for (int i = 0; i < dataSaveBueroCheckGridData.size(); i++) {
                Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData BureauType::" + dataSaveBueroCheckGridData.get(i).get(0));
                BureauType[i] = dataSaveBueroCheckGridData.get(i).get(0);
                Log.consoleLog(ifr, "Inside dataSaveBueroCheckGridData:: Exp_CBSCORE" + dataSaveBueroCheckGridData.get(i).get(1));
                Exp_CBSCORE[i] = dataSaveBueroCheckGridData.get(i).get(1);
                Log.consoleLog(ifr, "BureauType:::Exp_CBSCORE" + BureauType[i] + Exp_CBSCORE[i]);
            }
            //String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='B'  and PID='" + ProcessInstanceId + "'";
            String borrowerQuery = ConfProperty.getQueryScript("getinsertionOrderID").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#applicanttype#", applicanttype);
            List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
            if (!borrowerQueryData.isEmpty()) {
                Log.consoleLog(ifr, "borrowerQueryData not empty::::" + borrowerQueryData);
                for (int i = 0; i < BureauType.length; i++) {
                    String brdata = borrowerQueryData.get(0).get(0);
                    String StrBureScore = Exp_CBSCORE[i];
                    String strBureauType = BureauType[i];
                    Log.consoleLog(ifr, "dataSavingGridQuery::::" + StrBureScore);
                    Log.consoleLog(ifr, "dataSavingGridQuery::::" + strBureauType);
                    String updatequery = ConfProperty.getQueryScript("updateQueryforDataSavingGrid").replaceAll("#brdata#", brdata).replaceAll("#strBureauType#", strBureauType).replaceAll("#StrBureScore#", StrBureScore).replaceAll("#ProcessInstanceId#", ProcessInstanceId).replace("#fkey#", fkey).replaceAll("#applicantcode#", applicanttype);
                    Log.consoleLog(ifr, "dataSavingGridQuery::::" + updatequery);
                    ifr.saveDataInDB(updatequery);

                }

            }
        }
    }

    //Getting FullName
    public String mFullNameByApplicantName(IFormReference ifr, String applicanttype) {
        String fullName = "";
        try {
            String query = ConfProperty.getQueryScript("FullNameForCBQuery").replaceAll("#applicanttype#", applicanttype).replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "FullNameByApplicantName Query data::" + query);
            List<List<String>> queryResult = ifr.getDataFromDB(query);
            if (queryResult.size() > 0) {
                fullName = queryResult.get(0).get(0);
                return fullName;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mFullNameByApplicantName method" + e);
            Log.errorLog(ifr, "Exception in mFullNameByApplicantName method" + e);
        }
        return "";
    }

    public static int calculateAge(LocalDate dob) {
        LocalDate curDate = LocalDate.now();
        if ((dob != null) && (curDate != null)) {
            return Period.between(dob, curDate).getMonths();
        } else {
            return 0;
        }
    }

    // added by vandana
    public static int differenceInMonths(IFormReference ifr, String oldDate) {
        Log.consoleLog(ifr, "differenceInMonths::oldDate " + oldDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(oldDate, formatter);
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date.withDayOfMonth(1), currentDate.withDayOfMonth(1));
        Log.consoleLog(ifr, "differenceInMonths::period " + period);
        int monthsBetween = period.getYears() * 12 + period.getMonths();
        Log.consoleLog(ifr, "differenceInMonths::monthsBetween " + monthsBetween);
        if (currentDate.isBefore(date)) {
            monthsBetween = -monthsBetween; // Make the monthsBetween negative
        }
        Log.consoleLog(ifr, "differenceInMonths::Months between dates: " + monthsBetween);
        return monthsBetween;
    }

    //added by sharon for co-borrower Mandatory check on next click 28/06/2024
    public String mCheckmanatPortalFieldsCBCoBo(IFormReference ifr) {
        Log.consoleLog(ifr, " INSIDE mCheckmanatPortalFieldsCB Co-Borrower for occupation:: ");
        String PortalField = "";
        String OccupationDetailsCOB = ifr.getValue("P_CB_OD_Profile_COB").toString();
        if (!OccupationDetailsCOB.isEmpty()) {
            if (OccupationDetailsCOB.equalsIgnoreCase("Salaried")) {
                //Salaried
                PortalField = ConfProperty.getCommonPropertyValue("PortalmanCoBoSalariedOccupationDetailsFields");
            } else if (OccupationDetailsCOB.equalsIgnoreCase("NIE")) {
                //Non Income Earner
                PortalField = ConfProperty.getCommonPropertyValue("PortalmanNIEOccupationDetailsFields");

            } else if (OccupationDetailsCOB.equalsIgnoreCase("OTH")) {
                //Others
                PortalField = ConfProperty.getCommonPropertyValue("PortalmanOthersOccupationDetailsFields");
            }
        } else {
            return "Occupation Type";
        }

        Log.consoleLog(ifr, "portalfield::" + PortalField);
        String portalfield[] = PortalField.split(",");
        for (int i = 0; i < portalfield.length; i++) {
            Log.consoleLog(ifr, "mCheckmanatPortalFieldsCBCoBo:Value for occupation1 CoBo::" + (portalfield[i]));
            if (ifr.getValue(portalfield[i]).toString().isEmpty()) {

                Log.consoleLog(ifr, "mCheckmanatPortalFieldsCBCoBo:setValue for occupation CoBo:: " + portalfield[i]);

                return portalfield[i].replaceAll("P_CB_OD_", "").replaceAll("_COB", " Co-Obligant");

            }
            // break;
        }

        return "success";
    }

    //Added by Aravindh for Tenure Elongation Updation in backend
    public void TenureElongationUpdate(IFormReference ifr, String TenureElongation) {
        Log.consoleLog(ifr, "TenoreElongationUpdate ===>" + TenureElongation);
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

        //String tenureUpdationQuery = ConfProperty.getQueryScript("getmaxTenureQuery");
        String tenureUpdationQuery = "UPDATE LOS_NL_PROPOSED_FACILITY SET TENURE_ELONGATION ='" + TenureElongation + "' WHERE PID ='" + PID + "'";
        Log.consoleLog(ifr, "tenureUpdationQuery ::::" + tenureUpdationQuery);
        int Result = ifr.saveDataInDB(tenureUpdationQuery);
        Log.consoleLog(ifr, "### TENURE_ELONGATION Updated ###" + Result);

    }

    //Added by Sharon for Reset values of Co-Borrower
    public void ResetCoObligantCB(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside ResetCoObligantCB ===>");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "PID ResetCoObligantCB ===>" + PID);
        ifr.setValue("CoApplicantName_CB", "");
        ifr.setValue("P_CB_OD_MOBILE_NUMBER", "");
        ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
        ifr.setValue("P_CB_OD_EXISTING_CUSTOMER", "");
        ifr.setValue("P_CB_OD_RELATIONSHIP_BORROWER", "");
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
                        ifr.setStyle("P_CB_OD_MOBILE_NUMBER", "disable", "false");
                        ifr.setStyle("P_CB_OD_CUSTOMER_ID", "disable", "false");
                        ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER", "disable", "false");

                        ifr.setValue("CoApplicantName_CB", "");
                        ifr.setValue("P_CB_OD_MOBILE_NUMBER", "");
                        ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
                        ifr.setValue("P_CB_OD_EXISTING_CUSTOMER", "");
                        ifr.setValue("P_CB_OD_RELATIONSHIP_BORROWER", "");

                        //PortalOccupationDetailsFieldsCoborrower
                        PortalField = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");

                        Log.consoleLog(ifr, "PortalField::" + PortalField);
                        String portalfield[] = PortalField.split(",");
                        for (int j = 0; j < portalfield.length; j++) {
                            Log.consoleLog(ifr, "ResetCoObligantCB:setValue for occupation1 CoBo::" + (portalfield[j]));

                            ifr.setValue(portalfield[j], "");

                        }
                        Log.consoleLog(ifr, "End ResetCoObligantCB");
                        //updateCustomerDetails(ifr, Request, MobNum, map);
                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in updateCustomerAccountSummary ::" + e);
                        Log.errorLog(ifr, "Exception in updateCustomerAccountSummary::" + e);
                    }
                }
            }

        }

    }

}
