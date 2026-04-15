/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.vl;

import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.fintec.ConsumerAPI;
import com.newgen.dlp.integration.fintec.ExperianAPI;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import static com.newgen.iforms.budget.BudgetPortalCustomCode.differenceInMonths;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCustomCode;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class VLPortalCustomCode {

    PortalCommonMethods pcm = new PortalCommonMethods();
    BRMSRules jsonBRMSCall = new BRMSRules();
    CommonFunctionality cf = new CommonFunctionality();
    ///  ExperianAPI exp = new ExperianAPI();
    CustomerAccountSummary cas = new CustomerAccountSummary();
    CommonMethods objcm = new CommonMethods();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
    BRMCommonRules objbcr = new BRMCommonRules();

    ////Modified by ishwarya on 20-08-2024
    public String mImpOnClickOccupationDetails(IFormReference ifr, String control, String event, String value) {

        pcm.setbranchcode(ifr);//Added by Ahmed on 07-08-2024
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "Entered into mImpOnClickOccupationDetails:::");

            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String Purpose = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
            String SchemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
            Log.consoleLog(ifr, "productCode:" + productCode);
            String subProduct = pcm.mGetSubproductVL(ifr, Purpose);
            Log.consoleLog(ifr, "mImpOnClickOccupationDetails subProductCode:" + subProduct);

            String extTableupdateQuery = ConfProperty.getQueryScript("UPDATESUBPRODUCTCODE").replaceAll("#SUBPRODUCTTYPE#", subProduct).replaceAll("#PID#", ProcessInstanceId);
            Log.consoleLog(ifr, "extTableupdateQuery===>" + extTableupdateQuery);
            ifr.saveDataInDB(extTableupdateQuery);
            try {
                String insertionid = "";
                String getinsertionid = "select INSERTIONORDERID from LOS_NL_BASIC_INFO where PID = '" + ProcessInstanceId + "' and APPLICANTTYPE='CB'";
                List<List<String>> executeInsertion = cf.mExecuteQuery(ifr, getinsertionid, "Execute query for fetching insertionid");
                Log.consoleLog(ifr, "vlPortalCustomCode:: mImpOnClickOccupationDetails :: INSERTIONORDERID value : " + executeInsertion);
                if (!executeInsertion.isEmpty()) {
                    insertionid = executeInsertion.get(0).get(0);
                    Log.consoleLog(ifr, "vlPortalCustomCode:: mImpOnClickOccupationDetails :: insertionid==>" + insertionid);

                }
                String updateinsertionid = "update los_nl_bureau_consent set PARTYTYPE='" + insertionid + "' where pid='" + ProcessInstanceId + "'";
                List<List<String>> updateInsertion = cf.mExecuteQuery(ifr, updateinsertionid, "Execute query for fetching insertionid");
                Log.consoleLog(ifr, "vlPortalCustomCode:: mImpOnClickOccupationDetails :: updateInsertion==>" + updateInsertion);
            } catch (Exception e) {
                Log.consoleLog(ifr, "vlPortalCustomCode:: mImpOnClickOccupationDetails :: error updating insertion orderid==>" + e);
            }
            String MobileNo = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "MobileNo==>" + MobileNo);

            String finalEligibleAmount = ""; //changes by Ishwarya
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            HashMap<String, String> map = new HashMap<>();
            map.put("MobileNumber", MobileNo);
            //map.put("CustomerId", CustomerId);
            map.put("customerId", CustomerId);//Modified by Ahmed on 31-07-2024

            String response = cas.getAadharCustomerAccountSummary(ifr, map);
            Log.consoleLog(ifr, "response/getCustomerAccountSummary===>" + response);
            String subProductCode = "SUPVL";
            Log.consoleLog(ifr, "ProductCode:" + productCode);

            Log.consoleLog(ifr, "AADHARNUMBER==>" + response);
            if (response.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }
            String ext_cust = "";
            String Gender = "";
            String Customersummary = "SELECT CUSTOMERFLAG,CUSTOMERSEX from LOS_Trn_Customersummary where WINAME='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Customersummary value  : " + Customersummary);
            List<List<String>> dbdata = cf.mExecuteQuery(ifr, Customersummary, "Execute query for fetching income data from temp LOS_Trn_Customersummary");
            Log.consoleLog(ifr, "dbdata value  : " + dbdata);
            if (dbdata.size() > 0) {
                ext_cust = dbdata.get(0).get(0);
                Gender = dbdata.get(0).get(1);
                Log.consoleLog(ifr, "ext_cust==>" + ext_cust);
                Log.consoleLog(ifr, "Gender==>" + Gender);

            }

            String APIResponse = mGetAPIData(ifr);
            Log.consoleLog(ifr, "Entered into mGetAPIData:::");

            if (APIResponse.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }
            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String CustMisStatus = cf.getJsonValue(obj, "CustMisStatus");
            Log.consoleLog(ifr, "CustMisStatus==>" + CustMisStatus);
            CustMisStatus = "Complete";
            String cb = mCallBureau(ifr, "CB", response, "B");
            if (cb.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }

            String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

            Log.consoleLog(ifr, "decisionBCibil::" + decision);
            if (decision.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else if (decision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "CIBIL Passed Successfully:::");
                String EX = mCallBureau(ifr, "EX", response, "B");
                if (EX.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorAPIThroughExecute(ifr);
                }

                decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "EX", "B");

                Log.consoleLog(ifr, "decisionBEx::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorAPIThroughExecute(ifr);
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "EXPERIAN Passed Successfully:::");
                    //Bureo check data population Start added by vishal
                    String bueroTableQuery = "select PID from LOS_NL_CB_Details where PID='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "bueroTableQuery ::" + bueroTableQuery);
                    List<List<String>> bueroTableData = ifr.getDataFromDB(bueroTableQuery);
                    if (bueroTableData.isEmpty()) {
                        String dataSaveBueroCheckGridQuery = "select distinct BureauType,Exp_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK where ProcessInstanceId='" + ProcessInstanceId + "'";
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
                            String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='B'  and PID='" + ProcessInstanceId + "'";
                            List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                            if (borrowerQueryData.size() > 0) {
                                for (int i = 0; i < BureauType.length; i++) {
                                    String dataSavingGridQuery = "insert into LOS_NL_CB_Details (PID,InsertionOrderID,Applicant_type,CB_TYPE,CB_SCORE) values ('" + ProcessInstanceId + "',S_LOS_NL_BASIC_INFO.nextval,'" + borrowerQueryData.get(0).get(0) + "','" + BureauType[i] + "','" + Exp_CBSCORE[i] + "')";
                                    Log.consoleLog(ifr, "dataSavingGridQuery::::" + dataSavingGridQuery);
                                    ifr.saveDataInDB(dataSavingGridQuery);
                                }
                            }
                        }
                    }//Bureo check data population End   
                    //added by ishwarya .
                    Log.consoleLog(ifr, "inside LTV brms:::");
                    String OccupationType = ifr.getValue("P_VL_OD_Profile").toString();
                    String finalLTVAmount;
                    Log.consoleLog(ifr, "Purpose:::" + Purpose);
                    ext_cust = ext_cust.equalsIgnoreCase("Y") ? "Yes" : "No";
                    if (Purpose.equalsIgnoreCase("N2W")) {
                        Log.consoleLog(ifr, "iniside Purpose Two Wheeler:::" + Purpose);
                        String AssestCategory = ifr.getValue("P_VL_OD_CATEGORYC").toString();
                        OccupationType = ifr.getValue("P_VL_OD_Profile").toString();
                        Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                        //input_parameter,assetcategory_ip,existingcustomer_ip,occupationtype_ip,purpose_ip
                        Log.consoleLog(ifr, "AssestCategory::" + AssestCategory + ",Ext::" + ext_cust
                                + ",OccupationType::" + OccupationType + ",Purpose::" + Purpose);
                        String LTVTwoWheelerInParams = AssestCategory + "," + ext_cust + "," + OccupationType + "," + Purpose;
                        finalLTVAmount = checkLTVTwowheeler(ifr, "VL_LTV_Twowheeler", LTVTwoWheelerInParams, "ltv_op");
                        Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
                    } else {
                        Log.consoleLog(ifr, "iniside Purpose Four Wheeler:::" + Purpose);
                        String AssestCategory = ifr.getValue("P_VL_OD_CATEGORYC").toString();
                        OccupationType = ifr.getValue("P_VL_OD_Profile").toString();
                        String OccupationCategory = ifr.getValue("P_VL_OD_Category").toString();
                        String reqLoanAmt = ifr.getValue("P_OD_VL_LOANAMTREQ").toString();
                        Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                        //maxAmount
                        //input_parameter,purpose_ip,occupationtype_ip,occupationcategory_ip,loanamt_ip,assetcategory_ip,
                        Log.consoleLog(ifr, "Purpose::" + Purpose + ",OccupationType::" + OccupationType
                                + ",OccupationCategory::" + OccupationCategory + ",reqLoanAmt::" + reqLoanAmt + ",AssestCategory::" + AssestCategory);
                        String LTVFourWheelerInParams = Purpose + "," + OccupationType + "," + OccupationCategory + "," + reqLoanAmt + "," + AssestCategory;
                        finalLTVAmount = checkLTVFourwheeler(ifr, "VL_LTV_Fourwheeler", LTVFourWheelerInParams, "ltv_op");
                        Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
                    }
                    String Age;

                    Age = cf.getJsonValue(obj, "Age");
                    String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
                    writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";

                    Log.consoleLog(ifr, "inside eligibility brms:::");
                    String schemeID = pcm.mGetSchemeIDVL(ifr, ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString());
                    Log.consoleLog(ifr, "schemeID:" + schemeID);
                    String maxTenure = "0";
                    String maxloanamount = "0";
                    String query = "select maxloanamount from los_m_loaninfo where Scheme_ID='" + schemeID + "'";
                    List<List<String>> result = ifr.getDataFromDB(query);
                    if (result.size() > 0) {
                        maxloanamount = result.get(0).get(0);
                    }
                    String laonT = "select maxtenure from los_m_loaninfo where Scheme_ID='" + schemeID + "'";
                    List<List<String>> result1 = ifr.getDataFromDB(laonT);
                    if (result.size() > 0) {
                        maxTenure = result1.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "eligible loanTenure : " + maxTenure);
                    String reqLoanAm = ifr.getValue("P_OD_VL_LOANAMTREQ").toString();
                    String purpose = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
                    Log.consoleLog(ifr, "VLportal::onclickoccpation::reqLoanAmt  : " + reqLoanAm);
                    Log.consoleLog(ifr, "VLportal::onclickoccpation::purpose  : " + purpose);
                    String loanROI = null;
                    String SCHEDULECODE = "";
                    if (purpose.equalsIgnoreCase("N2W")) {
                        if (Gender.equalsIgnoreCase("MALE")) {

                            SCHEDULECODE = "3005";
                            Log.consoleLog(ifr, "VLportal::onclickoccpation::SCHEDULECODE  : " + SCHEDULECODE);

                        } else if (Gender.equalsIgnoreCase("FEMALE")) {
                            SCHEDULECODE = "3004";
                            Log.consoleLog(ifr, "VLportal::onclickoccpation::SCHEDULECODE  : " + SCHEDULECODE);

                        }
                    } else if (purpose.equalsIgnoreCase("N4W")) {
                        if (Gender.equalsIgnoreCase("MALE")) {
                            SCHEDULECODE = "3013";
                            Log.consoleLog(ifr, "VLportal::onclickoccpation::SCHEDULECODE  : " + SCHEDULECODE);

                        } else if (Gender.equalsIgnoreCase("FEMALE")) {
                            SCHEDULECODE = "3012";
                            Log.consoleLog(ifr, "VLportal::onclickoccpation::SCHEDULECODE  : " + SCHEDULECODE);

                        }
                    }

                    String roiData_Query = "select TOTALROI from los_m_roi where ROIID in (select ROIID from Los_m_Roi_Scheme where SCHEMEID in (Select SCHEMEID from los_m_product_rlos where SCHEMEID ='" + schemeID + "')) and CRG='CRG-3'  and ROITYPE='FLOATING' and SCHEDULECODE='" + SCHEDULECODE + "' AND " + reqLoanAm + " BETWEEN MINLOANAMOUNT AND MAXLOANAMOUNT";
                    List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
                    if (list2.size() > 0) {
                        loanROI = list2.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "roi : " + loanROI);
                    String obligation = "";
                    String obligation_Query = ConfProperty.getQueryScript("GetExperianTotalEMI").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> list = cf.mExecuteQuery(ifr, obligation_Query, "GetExperianTotalEMI");
                    if (list.size() > 0) {
                        obligation = list.get(0).get(0);
                        Log.consoleLog(ifr, "cibiloblig FinancialInfoLiabilityEMI::" + obligation);
                    } else {
                        obligation = "0.00";
                    }

                    //Modified by ishwarya on 12-07-2024
                    String grosssalary = "";
                    String deductionmonth = "";
                    String grosssalaryCB = "";
                    String deductionmonthCB = "";
                    int grosssal = 0;
                    int deduction = 0;
                    HashMap hm = new HashMap();
//                    String CBRequired = ifr.getValue("P_VL_OD_Co_Applicant").toString();
//                    if (CBRequired.equalsIgnoreCase("Yes")) {
//                        String OccupationTypeCB = ifr.getValue("P_VL_OD_Profile_CB").toString();
//                        Log.consoleLog(ifr, "inside coborrower yes ===>");
                    if (OccupationType.equalsIgnoreCase("Salaried") || OccupationType.equalsIgnoreCase("PEN")) {
                        Log.consoleLog(ifr, "inside IF OccupationType===>" + OccupationType);
//                            Log.consoleLog(ifr, "inside IF OccupationType===>" + OccupationType + "OccupationTypeCB===>" + OccupationTypeCB);
                        grosssalary = ifr.getValue("P_VL_OD_GrossSalary").toString();
                        deductionmonth = ifr.getValue("P_VL_OD_DeductionFromSalary").toString();
                        /* if (OccupationTypeCB.equalsIgnoreCase("Salaried") || OccupationTypeCB.equalsIgnoreCase("PEN")) {
                                Log.consoleLog(ifr, "inside IF OccupationTypeCB===>" + OccupationTypeCB);
                                grosssalaryCB = ifr.getValue("P_VL_OD_GrossSalary_CB").toString();
                                deductionmonthCB = ifr.getValue("P_VL_OD_DeductionFromSalary_CB").toString();
                                grosssal = Integer.parseInt(grosssalary) + Integer.parseInt(grosssalaryCB);
                                deduction = Integer.parseInt(deductionmonth) + Integer.parseInt(deductionmonthCB);
                                Log.consoleLog(ifr, "grosssal===>" + grosssal);
                                Log.consoleLog(ifr, "deduction===>" + deduction);
                            } else if (OccupationTypeCB.equalsIgnoreCase("SELF") || OccupationTypeCB.equalsIgnoreCase("PROF")) {
                                Log.consoleLog(ifr, "inside IF OccupationTypeCB===>" + OccupationTypeCB);
                                grosssalaryCB = ifr.getValue("P_VL_OD_GrossAnnualSalary_CB").toString();
                                deductionmonthCB = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary_CB").toString();
                                int grosssalCB = Integer.parseInt(grosssalaryCB) / 12;
                                int deductionCB = Integer.parseInt(deductionmonthCB) / 12;
                                grosssal = Integer.parseInt(grosssalary) + grosssalCB;
                                deduction = Integer.parseInt(deductionmonth) + deductionCB;
                                Log.consoleLog(ifr, "grosssal===>" + grosssal);
                                Log.consoleLog(ifr, "deduction===>" + deduction);
                            } */
                        hm.put("deductionmonth", deductionmonth);
                        hm.put("grosssalary", grosssalary);
                    } else if (OccupationType.equalsIgnoreCase("SELF") || OccupationType.equalsIgnoreCase("PROF")) {
                        Log.consoleLog(ifr, "inside else OccupationType===>" + OccupationType);
                        grosssalary = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
                        deductionmonth = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary").toString();
                        grosssal = Integer.parseInt(grosssalary) / 12;
                        deduction = Integer.parseInt(deductionmonth) / 12;
                        Log.consoleLog(ifr, "grosssal===>" + grosssal);
                        Log.consoleLog(ifr, "deduction===>" + deduction);
                        /* if (OccupationTypeCB.equalsIgnoreCase("Salaried") || OccupationTypeCB.equalsIgnoreCase("PEN")) {
                                Log.consoleLog(ifr, "inside IF OccupationTypeCB===>" + OccupationTypeCB);
                                grosssalaryCB = ifr.getValue("P_VL_OD_GrossSalary_CB").toString();
                                deductionmonthCB = ifr.getValue("P_VL_OD_DeductionFromSalary_CB").toString();
                                grosssal = grosssal + Integer.parseInt(grosssalaryCB);
                                deduction = deduction + Integer.parseInt(deductionmonthCB);
                                Log.consoleLog(ifr, "grosssal===>" + grosssal);
                                Log.consoleLog(ifr, "deduction===>" + deduction);
                            } else if (OccupationTypeCB.equalsIgnoreCase("SELF") || OccupationTypeCB.equalsIgnoreCase("PROF")) {
                                Log.consoleLog(ifr, "inside IF OccupationTypeCB===>" + OccupationTypeCB);
                                grosssalaryCB = ifr.getValue("P_VL_OD_GrossAnnualSalary_CB").toString();
                                deductionmonthCB = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary_CB").toString();
                                int grosssalCB = Integer.parseInt(grosssalaryCB) / 12;
                                int deductionCB = Integer.parseInt(deductionmonthCB) / 12;
                                grosssal = grosssal + grosssalCB;
                                deduction = deduction + deductionCB;
                                Log.consoleLog(ifr, "grosssal===>" + grosssal);
                                Log.consoleLog(ifr, "deduction===>" + deduction);
                            }*/
                        hm.put("deductionmonth", String.valueOf(deduction));
                        hm.put("grosssalary", String.valueOf(grosssal));
                    }
                    //  } 
                    /*else {
                        Log.consoleLog(ifr, "inside coborrower NO ===>");
                        if (OccupationType.equalsIgnoreCase("Salaried") || OccupationType.equalsIgnoreCase("PEN")) {
                            Log.consoleLog(ifr, "inside IF OccupationType===>" + OccupationType);
                            grosssalary = ifr.getValue("P_VL_OD_GrossSalary").toString();
                            deductionmonth = ifr.getValue("P_VL_OD_DeductionFromSalary").toString();
                            hm.put("deductionmonth", deductionmonth);
                            hm.put("grosssalary", grosssalary);
                        } else {
                            Log.consoleLog(ifr, "inside else OccupationType===>" + OccupationType);
                            grosssalary = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
                            deductionmonth = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary").toString();
                            grosssal = Integer.parseInt(grosssalary) / 12;
                            deduction = Integer.parseInt(deductionmonth) / 12;
                            hm.put("deductionmonth", String.valueOf(deduction));
                            hm.put("grosssalary", String.valueOf(grosssal));
                        }
                    }*/
                    String reqLoanAmt = ifr.getValue("P_OD_VL_LOANAMTREQ").toString();
                    Log.consoleLog(ifr, "reqLoanAmt===>" + reqLoanAmt);
                    Log.consoleLog(ifr, "P_VL_OD_GrossSalary===>" + grosssalary);
                    Log.consoleLog(ifr, "P_VL_OD_DeductionFromSalary===>" + deductionmonth);
                    String onRoadPrice = ifr.getValue("P_VL_OD_COST").toString();
                    Log.consoleLog(ifr, "onRoadPrice===>" + onRoadPrice);
                    hm.put("cibiloblig", obligation);
                    Log.consoleLog(ifr, "cibiloblig===>" + obligation);
                    hm.put("tenure", String.valueOf(maxTenure));
                    hm.put("roi", String.valueOf(loanROI));
                    hm.put("loancap", maxloanamount);
                    hm.put("finalLTVAmount", finalLTVAmount);
                    hm.put("reqLoanAmt", reqLoanAmt);
                    hm.put("Purpose", Purpose);
                    hm.put("OccupationType", OccupationType);
                    hm.put("onRoadPrice", onRoadPrice);

                    String finalelig = getAmountForEligibilityCheckVL(ifr, hm);
                    Log.consoleLog(ifr, "finalelig===>" + finalelig);
                    if (finalelig.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnErrorAPIThroughExecute(ifr);
                    } else if (finalelig.contains("showMessage")) {
                        return finalelig;
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
                        String Query2 = "INSERT INTO LOS_L_FINAL_ELIGIBILITY (PID) VALUES ('" + ProcessInstanceId + "')";
                        Log.consoleLog(ifr, "Query1===>" + Query2);
                        ifr.saveDataInDB(Query2);
                    }
                    String Query2 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
                            + "SET LOAN_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "Query2===>" + Query2);
                    ifr.saveDataInDB(Query2);
                    String Query3 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
                            + "SET IN_PRINCIPLE_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "Query3===>" + Query3);
                    ifr.saveDataInDB(Query3);//changes by Ishwarya
                    String finalAmountInParams = productCode + "," + finalelig;
                    finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");
                    if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {//changes by Ishwarya
                        Log.consoleLog(ifr, "eligibility Passed Successfully:::");
                        populateOccuapationDetailsVL(ifr, control, event, value);
                        populateOccuapationDetailsforCoBorrowerVL(ifr, control, event, value);
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

                        Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
                        Log.consoleLog(ifr, "bscore:::: " + bscore);
                        Log.consoleLog(ifr, "before query");

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

                        //modified by ishwarya on 07-09-2024
                        String netIncome = "";
                        String grossSalary = "";
                        String deductionSalary = "";
                        String occpationType = ifr.getValue("P_VL_OD_Profile").toString();
                        Log.consoleLog(ifr, "occpationType : " + occpationType);
                        if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {
                            netIncome = ifr.getValue("P_VL_OD_NetIncome").toString();
                            grossSalary = ifr.getValue("P_VL_OD_GrossSalary").toString();
                            deductionSalary = ifr.getValue("P_VL_OD_DeductionFromSalary").toString();
                        } else {
                            netIncome = ifr.getValue("P_VL_OD_AnnualNetIncome").toString();
                            grossSalary = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
                            deductionSalary = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary").toString();
                        }
                        String category = ifr.getValue("P_VL_OD_Category").toString();
                        String expYears = "";
                        if (occpationType.equalsIgnoreCase("PEN")) {
                            expYears = ifr.getValue("P_VL_OD_OverAllExperience").toString();
                        } else {
                            expYears = ifr.getValue("P_VL_OD_ExperienceYear").toString();
                        }
                        Log.consoleLog(ifr, "expYears : " + expYears);
                        String residence_inp = ifr.getValue("P_VL_OD_Residence").toString();
                        String natureOfSecurity = ifr.getValue("VL_OD_NatureofSecurity").toString();
                        String recovery = ifr.getValue("P_VL_OD_RecoveryMechanism").toString();

                        String guarantorwriteoff = GUARANTORWRITEOFFSETTLEDHIST;
                        String nparestmonths_inpo = settleHistory;
                        String settledhist_inp = settleHistory;
                        String writeOff = settleHistory;
                        String guarantornpa_inp = GUARANTORNPAINP;
                        String guarantorsettledhist_inp = GUARANTORWRITEOFFSETTLEDHIST;
                        String Updatequery = "Update LOS_NL_BASIC_INFO set OVERDUEINCREDITHISTORY='" + overduedays_inps + "' , SETTLEDACCOUNTINCREDITHISTORY ='" + settledhist_inp + "' where PID='" + ProcessInstanceId + "' and APPLICANTTYPE='B'";
                        ifr.saveDataInDB(Updatequery);
                        String existcust_inpQuery = ConfProperty.getQueryScript("getExistingCustomer").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                        List<List<String>> Results = cf.mExecuteQuery(ifr, existcust_inpQuery, "Exitingquery");
                        if (!Results.isEmpty()) {
                            existcust_inp = Results.get(0).get(0);
                            existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
                        }

                        String inPrincipleAmount = "";
                        String amount_Query = "SELECT round(IN_PRINCIPLE_AMOUNT) FROM LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInstanceId + "'";
                        List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

                        if (!Results1.isEmpty()) {
                            inPrincipleAmount = Results1.get(0).get(0);
                            Log.consoleLog(ifr, "inPrincipleAmount " + inPrincipleAmount);
                        }
                        Log.consoleLog(ifr, "Before conversion ");
                        int grossSalaryip1 = Integer.parseInt(grossSalary);

                        Log.consoleLog(ifr, "Before bigdecimal conversion  ");
                        BigDecimal deductionSalaryip = new BigDecimal(deductionSalary);
                        Log.consoleLog(ifr, "after bigdecimal conversion deductionSalary" + deductionSalaryip);
                        BigDecimal grossSalaryip = new BigDecimal(grossSalary);
                        Log.consoleLog(ifr, "after bigdecimal conversion grossSalaryip " + grossSalaryip);
                        BigDecimal MonthlyDeductionip = new BigDecimal(monthlydeduction);
                        Log.consoleLog(ifr, "after bigdecimal conversion MonthlyDeductionip " + MonthlyDeductionip);
                        BigDecimal loanamount = new BigDecimal(inPrincipleAmount);
                        Log.consoleLog(ifr, "after bigdecimal conversion loanamount" + loanamount);
                        BigDecimal ftRoi = new BigDecimal(loanROI);
                        Log.consoleLog(ifr, "after bigdecimal conversion ftRoi " + ftRoi);
                        int loanTenures = Integer.parseInt(maxTenure);
                        BigDecimal perposedEmi = bpcc.calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
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

                        Log.consoleLog(ifr, "business tenure::" + expYears + ",cibil score::" + bscore + ",emp input::" + category + ",anninc_inp::"
                                + anninc_inp + ",grossSalary::" + grossSalary + ",natureOfSecurity::" + natureOfSecurity + ",nethomeins_inprange::" + nethomeins_inprange
                                + ",existcust_inp::" + existcust_inp + ",excustrange" + Age + ",overduedays_inp::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto
                                + ",recovery::" + recovery + ",residence_inp::" + residence_inp + ",guarantorsettledhist_inp::" + guarantorsettledhist_inp + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff::" + guarantorwriteoff
                                + ",nparestmonths_inpo::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",npa_inp::" + npa_inp + ",writeOffPresent::" + writeOff);
                        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
                        String scorecardInParams = "";
                        String scorecardDecision = "";
                        if (!bscore.equalsIgnoreCase("I")) {
                            scorecardInParams = expYears + "," + bscore + "," + category + "," + anninc_inp + "," + grossSalary
                                    + "," + natureOfSecurity + "," + nethomeins_inprange + "," + existcust_inp + "," + Age + "," + overduedays_inp + "," + overduedaysupto
                                    + "," + recovery + "," + residence_inp + "," + guarantorsettledhist_inp + "," + guarantornpa_inp + "," + guarantorwriteoff
                                    + "," + nparestmonths_inpo + "," + settledhist_inp + "," + npa_inp + "," + writeOff;
                            //VL_ScoreCard_CICImmune             
                            scorecardDecision = checkScoreCard(ifr, "VL_SCORECARD", scorecardInParams, "totalgrade_op", "B");
                        } else {

                            scorecardInParams = expYears + "," + category + "," + anninc_inp + "," + grossSalary
                                    + "," + natureOfSecurity + "," + nethomeins_inprange + "," + existcust_inp + "," + Age + "," + overduedaysupto
                                    + "," + recovery + "," + residence_inp + "," + guarantornpa_inp + "," + guarantorwriteoff
                                    + "," + nparestmonths_inpo + "," + npa_inp + "," + writeOffPresent;

                            scorecardDecision = checkScoreCard(ifr, "VL_ScoreCard_CICImmune", scorecardInParams, "totalgrade_op", "B");

                        }
                        if (scorecardDecision.contains(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                            return pcm.returnErrorAPIThroughExecute(ifr);
                        }
                        if (scorecardDecision.equalsIgnoreCase("Low Risk- I") || scorecardDecision.equalsIgnoreCase("Low Risk- II") || scorecardDecision.equalsIgnoreCase("Normal Risk")
                                || scorecardDecision.equalsIgnoreCase("Moderate Risk") || scorecardDecision.equalsIgnoreCase("")) {
                            Log.consoleLog(ifr, "scoreCard Passed Successfully:::");

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

        } catch (NumberFormatException | ParseException e) {
            Log.consoleLog(ifr, "Exception mImpOnClickOccupationDetails : " + e);
            Log.errorLog(ifr, "Exception mImpOnClickOccupationDetails : " + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }

        return "";
    }

    //added by ishwarya on 12022024
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
        String schemeID = pcm.mGetSchemeIDVL(ifr, ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString());
        Log.consoleLog(ifr, "schemeID:" + schemeID);
        String minLoanmaount = pcm.mGetMinLoanAmountVL(ifr, schemeID);
        //Code Logic Modified for Experian
        if (BureauType.equalsIgnoreCase("EX")) {
            ExperianAPI EXP = new ExperianAPI();
            String BureauScore = EXP.getExperianCIBILScore2(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount, applicantType);
            Log.consoleLog(ifr, "BureauScore From Experian===>" + BureauScore);
            if (BureauScore.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
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

    // added by ishwarya for co-borrower data saving on 25-06-2024
    // added by ishwarya for co-borrower data saving on 25-06-2024
    public String vlScoreCardDecisionForCB(IFormReference ifr) {
        String scorecardDecisionCB = "";
        try {
            Log.consoleLog(ifr, "inside try block scoreCardDecisionForCB:::");
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

            String settleHistory = "";
            String GUARANTORNPAINP = "";
            String GUARANTORWRITEOFFSETTLEDHIST = "";
            String APIResponse = mGetAPIData(ifr);
            Log.consoleLog(ifr, "Entered into mGetAPIData:::");

            if (APIResponse.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }
            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String Age;
            Age = cf.getJsonValue(obj, "Age");
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

            Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
            Log.consoleLog(ifr, "bscore:::: " + bscore);
            Log.consoleLog(ifr, "before query");

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

            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String Fkey = bpcc.Fkey(ifr, "CB");
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String loanTenure = "0";
            String tenureData_Query = "select maxtenure,maxloanamount from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
            if (list1.size() > 0) {
                loanTenure = list1.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            String loanROI = null;
            String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
            Log.consoleLog(ifr, "roiID:" + roiID);
            String roiData_Query = "select totalroi from los_m_roi where roiid='" + roiID + "'";
            List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
            if (list2.size() > 0) {
                loanROI = list2.get(0).get(0);
            }
            Log.consoleLog(ifr, "roi : " + loanROI);

            String netIncome = "";
            String category = "";
            String grossSalary = "";
            String deductionSalary = "";
            String expYears = "";
            String residence_inp = "";
            String natureOfSecurity = "";
            String recovery = "";
            String occuType = "";
            String occuinfo = "select PROFILE,NETSALARY,GROSSSALARY,DEDUCTIONMONTH,CATEGORY,EXPERIENCEYEAR,OVER_ALL_EXPERIENCE,RESIDENCE,RECOVERY_MECHANISM,NATURE_OF_SECURITY from LOS_NL_OCCUPATION_INFO where F_KEY ='" + Fkey + "'";
            List<List<String>> occuInfo = ifr.getDataFromDB(occuinfo);
            Log.consoleLog(ifr, "occuinfo data:::: " + occuinfo);
            if (!occuInfo.isEmpty()) {
                occuType = occuInfo.get(0).get(0);
                netIncome = occuInfo.get(0).get(1);
                grossSalary = occuInfo.get(0).get(2);
                deductionSalary = occuInfo.get(0).get(3);
                category = occuInfo.get(0).get(4);
                residence_inp = occuInfo.get(0).get(7);
                recovery = occuInfo.get(0).get(8);
                natureOfSecurity = occuInfo.get(0).get(9);
                if (occuType.equalsIgnoreCase("PEN")) {
                    expYears = occuInfo.get(0).get(6);
                } else {
                    expYears = occuInfo.get(0).get(5);
                }
                Log.consoleLog(ifr, "netIncome:::: " + netIncome);
                Log.consoleLog(ifr, "grossSalary:::: " + grossSalary);
                Log.consoleLog(ifr, "deductionSalary:::: " + deductionSalary);
                Log.consoleLog(ifr, "category:::: " + category);
                Log.consoleLog(ifr, "expYears:::: " + expYears);
                Log.consoleLog(ifr, "residence_inp:::: " + residence_inp);
                Log.consoleLog(ifr, "residence_inp:::: " + natureOfSecurity);
                Log.consoleLog(ifr, "residence_inp:::: " + recovery);

            }
            String guarantorwriteoff = GUARANTORWRITEOFFSETTLEDHIST;
            String nparestmonths_inpo = settleHistory;
            String settledhist_inp = settleHistory;
            String writeOff = settleHistory;
            String guarantornpa_inp = GUARANTORNPAINP;
            String guarantorsettledhist_inp = GUARANTORWRITEOFFSETTLEDHIST;
            String Updatequery = "Update LOS_NL_BASIC_INFO set OVERDUEINCREDITHISTORY='" + overduedays_inps + "' , SETTLEDACCOUNTINCREDITHISTORY ='" + settledhist_inp + "' where PID='" + ProcessInstanceId + "' and APPLICANTTYPE='B'";
            ifr.saveDataInDB(Updatequery);

            String existcust_inpQuery = ConfProperty.getQueryScript("getExistingCustomer").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> Results = cf.mExecuteQuery(ifr, existcust_inpQuery, "Exitingquery");
            if (!Results.isEmpty()) {
                existcust_inp = Results.get(0).get(0);
                existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
            }

            String inPrincipleAmount = "";
            String amount_Query = "SELECT round(IN_PRINCIPLE_AMOUNT) FROM LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInstanceId + "'";
            List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

            if (!Results1.isEmpty()) {
                inPrincipleAmount = Results1.get(0).get(0);
                Log.consoleLog(ifr, "inPrincipleAmount " + inPrincipleAmount);
            }
            Log.consoleLog(ifr, "Before conversion ");
            int grossSalaryip1 = Integer.parseInt(grossSalary);

            Log.consoleLog(ifr, "Before bigdecimal conversion  ");
            BigDecimal deductionSalaryip = new BigDecimal(deductionSalary);
            Log.consoleLog(ifr, "after bigdecimal conversion deductionSalary" + deductionSalaryip);
            BigDecimal grossSalaryip = new BigDecimal(grossSalary);
            Log.consoleLog(ifr, "after bigdecimal conversion grossSalaryip " + grossSalaryip);
            BigDecimal MonthlyDeductionip = new BigDecimal(monthlydeduction);
            Log.consoleLog(ifr, "after bigdecimal conversion MonthlyDeductionip " + MonthlyDeductionip);
            BigDecimal loanamount = new BigDecimal(inPrincipleAmount);
            Log.consoleLog(ifr, "after bigdecimal conversion loanamount" + loanamount);
            BigDecimal ftRoi = new BigDecimal(loanROI);
            Log.consoleLog(ifr, "after bigdecimal conversion ftRoi " + ftRoi);
            int loanTenures = Integer.parseInt(loanTenure);
            BigDecimal perposedEmi = bpcc.calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
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
            Log.consoleLog(ifr, "business tenure::" + expYears + ",cibil score::" + bscore + ",emp input::" + category + ",anninc_inp::"
                    + anninc_inp + ",grossSalary::" + grossSalary + ",natureOfSecurity::" + natureOfSecurity + ",nethomeins_inprange::" + nethomeins_inprange
                    + ",existcust_inp::" + existcust_inp + ",excustrange" + Age + ",overduedays_inp::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto
                    + ",recovery::" + recovery + ",residence_inp::" + residence_inp + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff::" + guarantorwriteoff
                    + ",nparestmonths_inpo::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",npa_inp::" + npa_inp + ",writeOffPresent::" + writeOff);
            //Modifed by monesh on 12/04/2024 for handling CIC Immune Case
            String scorecardInParams = "";

            if (!bscore.equalsIgnoreCase("I")) {
                scorecardInParams = expYears + "," + bscore + "," + category + "," + anninc_inp + "," + grossSalary
                        + "," + natureOfSecurity + "," + nethomeins_inprange + "," + existcust_inp + "," + Age + "," + overduedays_inp + "," + overduedaysupto
                        + "," + recovery + "," + residence_inp + "," + guarantorsettledhist_inp + "," + guarantornpa_inp + "," + guarantorwriteoff
                        + "," + nparestmonths_inpo + "," + settledhist_inp + "," + npa_inp + "," + writeOff;
//VL_ScoreCard_CICImmune                       
                scorecardDecisionCB = checkScoreCard(ifr, "VL_SCORECARD", scorecardInParams, "totalgrade_op", "CB");
            } else {

                scorecardInParams = expYears + "," + category + "," + anninc_inp + "," + grossSalary
                        + "," + natureOfSecurity + "," + nethomeins_inprange + "," + existcust_inp + "," + Age + "," + overduedaysupto
                        + "," + recovery + "," + residence_inp + "," + guarantornpa_inp + "," + guarantorwriteoff
                        + "," + nparestmonths_inpo + "," + npa_inp + "," + writeOff;

                scorecardDecisionCB = checkScoreCard(ifr, "VL_ScoreCard_CICImmune", scorecardInParams, "totalgrade_op", "CB");

            }
            if (scorecardDecisionCB.contains(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                return pcm.returnErrorAPIThroughExecute(ifr);
            }

        } catch (NumberFormatException | ParseException e) {
            Log.errorLog(ifr, "Exception in scorecardFor Co-Applicant brms::" + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
        return scorecardDecisionCB;
    }

//added by ishwarya on 12022024
//Commented by Monesh on 13042024
/*  public String mCheckCIBILScoreknockOff(IFormReference ifr, String BureauType, String subProductCode, String ProductCode,String Rulename) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String cbScore = "";
        String exScore = "";
        if (BureauType.equalsIgnoreCase("CB")) {
            String cbQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
            List<List<String>> cbResult = cf.mExecuteQuery(ifr, cbQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (cbResult.size() > 0) {
                cbScore = cbResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

        } else {
            String cbQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
            List<List<String>> cbResult = cf.mExecuteQuery(ifr, cbQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (cbResult.size() > 0) {
                cbScore = cbResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

            String exQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='EX'";
            List<List<String>> exResult = cf.mExecuteQuery(ifr, exQuery, " mCheckCIBILScoreknockOff :: cibil Query: ");
            if (exResult.size() > 0) {
                exScore = exResult.get(0).get(0);
            } else {
                return RLOS_Constants.ERROR;
            }

        }

        String decision;
        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

        if (!cbScore.equalsIgnoreCase("I")) {
            if (BureauType.equalsIgnoreCase("CB")) {
                decision = checkBRMSKnockOff(ifr, "CIBILSCORE", ProductCode + "," + subProductCode + "," + cbScore, "cibilscrop");
            } else {
                decision = checkBRMSKnockOff(ifr, "EXPERIANSCORE", ProductCode + "," + subProductCode + "," + cbScore + "," + exScore, "experianscrop");
            }
        } else {
            decision = "Approve";
        }
        Log.consoleLog(ifr, "decision==>" + decision);
        if (decision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            return decision;
        }
    }
     */
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

    //Modified by Sharon on 20-08-2024
    public String checkKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("totalknockoff_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);

        try {
            Log.consoleLog(ifr, "inside checkKnockOff:::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId:::" + ProcessInstanceId);

            String[] brmsKeys = {
                "borage_op", "kycval_op", "misdata_op", "cbsnpa_op", "nre_nro_check", "staffch_op", "writeoff_op",
                "totalknockoff_op"
            };

            String[] ruleNames = {
                "AGE CHECK", "KYC Validation", "MIS DATA CHECK", "NPA CHECK", "NRI CHECK",
                "STAFF CHECK", "WRITEOFF HISTORY", "KNOCK-OFF RULES"
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

            String brdata = "Co-Borrower";

            String mobileno = ifr.getValue("P_VL_OD_MOBILE_NUMBER").toString();
            Log.consoleLog(ifr, "MobileNo==>" + mobileno);
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            String knockoffTable = "SELECT * FROM LOS_TMP_Knockoff_Rules WHERE MOBILE_NO ='" + mobileno + "' and LOAN_SELECTED = '" + loan_selected + "' and PARTY_TYPE like 'C%'";
            Log.consoleLog(ifr, "knockoffTable: " + knockoffTable);
            List<List<String>> knockoffTableDATA = cf.mExecuteQuery(ifr, knockoffTable, "knockoffTable:");

            if (knockoffTableDATA.isEmpty() && brdata.contains("Co-")) {
                for (int i = 0; i < ruleNames.length && i < brmsOutputs.size(); i++) {
                    String dataSavingQuery = "INSERT INTO LOS_TMP_Knockoff_Rules "
                            + "(LOAN_SELECTED, MOBILE_NO, PARTY_TYPE, RULE_NAME, OUTPUT) "
                            + "VALUES('" + loan_selected + "','" + mobileno + "', '" + brdata + "', '" + ruleNames[i] + "', '" + brmsOutputs.get(i) + "')";
                    Log.consoleLog(ifr, "dataSavingQuery: " + dataSavingQuery);
                    ifr.saveDataInDB(dataSavingQuery);
                }
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception checkKnockOff : " + ex);
            Log.errorLog(ifr, "Exception checkKnockOff : " + ex);
        }

        Log.consoleLog(ifr, "totalGrade RETURN" + totalGrade);
        return totalGrade;
    }

    public String checkBRMSKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
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
        String CustomerId = pcm.getCustomerIDCB(ifr, "B");
        String Age = "", Dob = "", CustMisStatus = "";
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
            Dob = cf.getJsonValue(obj, "DOB");
            CustMisStatus = cf.getJsonValue(obj, "CustMisStatus");
        }

        Advanced360EnquiryDatav2 objAdv360 = new Advanced360EnquiryDatav2();
        String response = objAdv360.executeCBSAdvanced360Inquiryv2(ifr, ProcessInstanceId, CustomerId, "Budget", "", "");
//Commented by Ahmed for using v2 instead of v1 in code for budget on 29-06-2024

//        Advanced360EnquiryData API360 = new Advanced360EnquiryData();
//        String response = API360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "VL");
//      
        Log.consoleLog(ifr, "response==>" + response);

        String canaraBudgetProductCode = "";
        String PAPLExist;
        String count = "";
        String Classification;

        if (response.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject) jsonparser.parse(response);
            canaraBudgetProductCode = cf.getJsonValue(obj, "ProductCode");
            PAPLExist = cf.getJsonValue(obj, "PAPLExist");
            Classification = cf.getJsonValue(obj, "Classification");
            count = cf.getJsonValue(obj, "count");
        }
        JSONObject obj = new JSONObject();
        obj.put("writeOffPresent", writeOffPresent);
        obj.put("Age", Age);
        obj.put("DOB", Dob);
        obj.put("count", count);
        obj.put("productCode", canaraBudgetProductCode);
        obj.put("CustMisStatus", CustMisStatus);
        obj.put("PAPLExist", PAPLExist);
        obj.put("Classification", Classification);
        return obj.toString();
    }

    public String checkScoreCard(IFormReference ifr, String RuleName, String values, String ValueTag, String applicantType) {
        // added by ishwarya for scorecard data saving on 20/06/2024
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);
        Log.consoleLog(ifr, "activityName: " + ifr.getActivityName());

        String totalGrade = objm.get("totalgrade_op").toString();
        String scoreband_op = objm.get("scoreband_op").toString();
        Log.consoleLog(ifr, "objm totalGrade::: " + totalGrade + " scoreband_op::: " + scoreband_op);

        String[] keys = {"recmech_op", "aninc_op", "satbanking_op", "natsec_op", "overduedays_op", "emp_op", "settledhis_op", "nethomeinc_op", "emp_busiten_op", "vlcibil_op", "residence_op"};
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

    public void autoPupulateBueroConsentFromPortal(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            Log.consoleLog(ifr, "Inside autoPupulateBueroConsentFromPortal::");
            String bueroConsentTableQuery = "select PID from LOS_NL_BUREAU_CONSENT where PID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "bueroTableQuery ::" + bueroConsentTableQuery);
            List<List<String>> bueroConsentTableData = ifr.getDataFromDB(bueroConsentTableQuery);
            if (bueroConsentTableData.size() == 0) {
                String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='CB' and PID='" + ProcessInstanceId + "'";
                Log.consoleLog(ifr, "borrowerQuery for consent::" + borrowerQuery);
                List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                Log.consoleLog(ifr, "borrowerQueryData for consent::" + borrowerQueryData);
                if (borrowerQueryData.size() > 0) {
                    Log.consoleLog(ifr, "appType:: " + borrowerQueryData.get(0).get(0));
                    objcm.mInsertBureauConsent(ifr, ProcessInstanceId, borrowerQueryData.get(0).get(0));
                    String dataSavingINConsentGridQuery = "insert into LOS_NL_BUREAU_CONSENT (PID,InsertionOrderID,PartyType,Methodology,ConsentReceived) values('" + ProcessInstanceId + "',S_LOS_NL_BUREAU_CONSENT.nextval,'" + borrowerQueryData.get(0).get(0) + "','P','Initiated')";
                    Log.consoleLog(ifr, "insert dataSavingINConsentGridQuery for consent" + dataSavingINConsentGridQuery);
//                    Email em = new Email();
//                    String emailId = pcm.getCurrentEmailId(ifr, "VL", "CB");
//                    Log.consoleLog(ifr, "autoPupulateBueroConsentFromPortal :emailId::" + emailId);
//                    em.sendEmail(ifr, ProcessInstanceId, emailId, "", "CB", "RETAIL", "6");//Need to add template in table CAN_MST_EMAIL_HEADERS

                    //Added by Ahmed on 27-05-2024 using DLPCommonObjects instead of directly calling
                    String bodyParams = "";
                    String subjectParams = "";
                    String fileName = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    // pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "VL", "6", bodyParams, subjectParams, fileName, fileContent);

                    ifr.saveDataInDB(dataSavingINConsentGridQuery);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in autoPupulateBueroConsentFromPortal::" + e);
            Log.errorLog(ifr, "Exception in  autoPupulateBueroConsentFromPortal::" + e);
        }
    }

    public String autoPopulateOccupationDetailsDataVL(IFormReference ifr, String value) {
        Log.consoleLog(ifr, "inside autoPopulateOccupationDetailsDataVL  : ");
        String currentStep = pcm.setGetPortalStepName(ifr, value);
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

//            String queryResadd = "select a.Line1,a.Line2,a.Line3,c.EMAILID,a.state,a.country,a.pincode,c.MobileNo from LOS_NL_Address a inner join  LOS_NL_BASIC_INFO b on a.f_key=b.f_key inner join LOS_L_BASIC_INFO_I c on b.f_key=c.f_key where b.PID='" + ProcessInsanceId + "' and a.AddressType='CA' and b.ApplicantType='B'";
//            String queryPeradd = "select a.Line1,a.Line2,a.Line3,a.pincode from LOS_NL_Address a inner join  LOS_NL_BASIC_INFO b on a.f_key=b.f_key  where b.PID='" + ProcessInsanceId + "' and a.AddressType='P' and b.ApplicantType='B'";
            String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            String queryPeradd = ConfProperty.getQueryScript("getPerAddrQueryVL").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsDataVL-> CA:" + queryResadd);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsDataVL-> PA:" + queryPeradd);

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
                ifr.setValue("P_VL_OD_EMAILID", EMAILID);
                ifr.setValue("P_VL_OCCINFO_MOBILENO", strmobile);
                ifr.setValue("P_VL_OD_COMMUNICATION_ADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + "," + state + "," + country + "," + pincode);
            }
            if (resultPeradd.size() > 0) {
                String addressLine1 = resultPeradd.get(0).get(0);
                String addressLine2 = resultPeradd.get(0).get(1);
                String addressLine3 = resultPeradd.get(0).get(2);
                String state = resultPeradd.get(0).get(3);
                String country = resultPeradd.get(0).get(4);
                String pincode = resultPeradd.get(0).get(5);
                ifr.setValue("P_VL_OD_PERMANENT_ADDRESS", addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + state + "," + country + "," + pincode);
            }

            String FirstNameCB = "", MiddleNameCB = "", LastNameCB = "";
            String FullNameCBQuery = "SELECT TITLE||'. '||regexp_replace(a.FIRSTNAME,'{}'),regexp_replace(a.MIDDLENAME,'{}'),regexp_replace(a.LASTNAME,'{}') FROM LOS_L_BASIC_INFO_I a INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY WHERE b.CUSTOMERFLAG = 'Y' AND b.PID ='" + ProcessInsanceId + "' and b.ApplicantType='B'";
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
                ifr.setValue("P_VL_H_CUSTOMERNAME1", Fullname.replace("  ", " "));
            }
            Log.consoleLog(ifr, "VLPortalCustomCode:autoPopulateOccupationDetailsDataVL:before asset set");
            ifr.setValue("P_VL_OD_CATEGORYC", "New");
            ifr.setStyle("P_VL_OD_CATEGORYC", "disable", "true");
            String NatureOfsecurity = "Hypothecation of durable utility article or vehicle to the extent of stipulation under the scheme";
            ifr.addItemInCombo("VL_OD_NatureofSecurity", NatureOfsecurity);
            ifr.setValue("VL_OD_NatureofSecurity", NatureOfsecurity);
            ifr.addItemInCombo("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
            ifr.setValue("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
            Log.consoleLog(ifr, "VLPortalCustomCode:autoPopulateOccupationDetailsDataVL:after asset set");
            //Code for Disbursal Account
            String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
            Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
            String response360 = objCbs360.executeAdvanced360Inquiry(ifr, pid, CustomerId, "VL");
            Log.consoleLog(ifr, "response==>" + response360);
            if (response360.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition 360API LAD");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition 360API LAD");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(response360);
                Log.consoleLog(ifr, obj.toString());
                String sbAccountDetails = obj.get("AccountDetails").toString();
                Log.consoleLog(ifr, "sbAccountDetails Value" + sbAccountDetails);
                JSONParser parser = new JSONParser();
                JSONArray accountDetailsJSON = (JSONArray) parser.parse(sbAccountDetails);
                if (!accountDetailsJSON.isEmpty()) {
                    Log.consoleLog(ifr, "inside accountDetailsJSON");
                    for (int i = 0; i < accountDetailsJSON.size(); i++) {
                        String InputString = accountDetailsJSON.get(i).toString();
                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);
                        String AccountId = InputStringResponseJSON.get("AccountId").toString().trim();
                        String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
                        String strAcctOpen = InputStringResponseJSON.get("DatAcctOpen").toString();
                        String strAcctbal = InputStringResponseJSON.get("AcyAmount").toString();
                        ifr.addItemInCombo("P_VL_OD_DISBURSAL_ACCOUNT", AccountId, AccountId + "-" + BranchCode + "-" + strAcctOpen + "-" + strAcctbal);

                    }
                }
            }
            String mobileNumber = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "mobileNumber::autoPopulateLoanDetailsDataCB:::" + mobileNumber);

            //added by ishwarya for saving data in knockoff-grid on 19/06/2024
            String PARTY_TYPE = "";
            String RULE_NAME = "";
            String OUTPUT = "";
            String knockoffTemprules = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYVL").replaceAll("#mobileNumber#", mobileNumber)
                    .replaceAll("#PARTY_TYPE#", "B");
            Log.consoleLog(ifr, "Customersummary value  : " + knockoffTemprules);
            List<List<String>> dbdata = cf.mExecuteQuery(ifr, knockoffTemprules, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
            Log.consoleLog(ifr, "dbdata value  : " + dbdata);
            if (!dbdata.isEmpty()) {
                PARTY_TYPE = dbdata.get(0).get(0);
                RULE_NAME = dbdata.get(0).get(1);
                OUTPUT = dbdata.get(0).get(2);
            }

            String knockoffRules = ConfProperty.getQueryScript("KNOCKOFFCOUNTVL").replaceAll("#ProcessInsanceId#", ProcessInsanceId)
                    .replaceAll("#PARTY_TYPE#", "B");
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
            String knockoffTemprules1 = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYVL1").replaceAll("#mobileNumber#", mobileNumber)
                    .replaceAll("#PARTY_TYPE#", "B");
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
                String knockoffTable1 = ConfProperty.getQueryScript("KNOCKOFFCOUNTVL1").replaceAll("#f_key#", f_key);
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

        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return currentStep;
    }

    public String getPortalDataLoadVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "setValue getPortalDataLoadVL:");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            onchangeProfileVL(ifr);
            String PortalField = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsVL");
            String portalfield[] = PortalField.split(",");
            String query = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateOccupationData query::" + query);
            List<List<String>> result = ifr.getDataFromDB(query);
            String F_Key = "";
            if (result.size() > 0) {
                F_Key = result.get(0).get(0);
            }
            Log.consoleLog(ifr, "FKey.." + F_Key);
            String query1 = ConfProperty.getQueryScript("PORTALOCCUPATIONINFODATAVL").replaceAll("#F_Key#", F_Key);
            Log.consoleLog(ifr, "populateOccupationData query::" + query1);
            List<List<String>> result1 = ifr.getDataFromDB(query1);
            //modified by ishwarya
            if (result1.size() > 0) {
                for (int i = 0; i < portalfield.length; i++) {

                    String netIncome = result1.get(0).get(24);
                    String deductionSalary = result1.get(0).get(26);
                    String grossSalary = result1.get(0).get(25);
                    Log.consoleLog(ifr, "deductionVL:::" + deductionSalary);
                    Log.consoleLog(ifr, "netincome:::" + netIncome);
                    Log.consoleLog(ifr, "gross:::" + grossSalary);

                    String occpationType = ifr.getValue("P_VL_OD_Profile").toString();
                    Log.consoleLog(ifr, "occpationType : " + occpationType);
                    Log.consoleLog(ifr, "before if loop : ");
                    if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {
                        Log.consoleLog(ifr, "inside if occpationType : " + occpationType);
                        ifr.setValue("P_VL_OD_GrossSalary", grossSalary);
                        ifr.setValue("P_VL_OD_NetIncome", netIncome);
                        ifr.setValue("P_VL_OD_DeductionFromSalary", deductionSalary);
                    } else {
                        Log.consoleLog(ifr, "inside else occpationType : " + occpationType);
                        ifr.setValue("P_VL_OD_GrossAnnualSalary", grossSalary);
                        ifr.setValue("P_VL_OD_AnnualNetIncome", netIncome);
                        ifr.setValue("P_VL_OD_AnnualDeductionFromSalary", deductionSalary);
                    }
                    String occupation = result1.get(0).get(0);
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:occupation:: " + occupation);
                    ifr.setValue("P_VL_OD_TypeOfOccupation", occupation);
                    //ifr.setValue("P_VL_OD_TypeOfOccupation", "");
                    String NatureOfsecurity = "Hypothecation of durable utility article or vehicle to the extent of stipulation under the scheme";
                    ifr.addItemInCombo("VL_OD_NatureofSecurity", NatureOfsecurity);
                    ifr.setValue("VL_OD_NatureofSecurity", NatureOfsecurity);
                    ifr.addItemInCombo("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                    ifr.setValue("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                    ifr.setValue("P_VL_OD_CATEGORYC", "New");
                    ifr.setStyle("P_VL_OD_CATEGORYC", "disable", "true");
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation:: success");
                    ifr.setValue(portalfield[i], result1.get(0).get(i));

                }
            }
            String PortalField1 = ConfProperty.getCommonPropertyValue("PortalCollateralDetailsFieldsVL");
            String portalfield1[] = PortalField1.split(",");
            Log.consoleLog(ifr, "portalFields array portalValue[i]::" + Arrays.toString(portalfield1));
            String query2 = ConfProperty.getQueryScript("PORTALCOLLATERALINFODATA").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateCollateralData query::" + query2);
            List<List<String>> result2 = ifr.getDataFromDB(query2);
            if (result2.size() > 0) {
                for (int i = 0; i < portalfield1.length; i++) {
                    /*String model = result2.get(0).get(2);
                    Log.consoleLog(ifr, "modelVL:::" + model);
                    String modelQuery = "SELECT ASSETMODEL FROM LOS_M_VEHICLE_ASSET WHERE ASSETMODELCODE = '" + model + "' OR ASSETMODEL ='" + model + "'";
                    Log.consoleLog(ifr, "modelVL query:::" + modelQuery);
                    List<List<String>> dataResult = ifr.getDataFromDB(modelQuery);
                    String modelName = dataResult.get(0).get(0);
                    Log.consoleLog(ifr, "modelName VL:::" + modelName);
                    ifr.setValue("P_VL_OD_MODEL", modelName);*/
                    String cost = result2.get(0).get(4);
                    Log.consoleLog(ifr, "costVL:::" + cost);
                    ifr.setValue("P_VL_OD_COST", cost);
                    ifr.setValue(portalfield1[i], result2.get(0).get(i));
                }
            }

            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
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
                String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
                Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
                if (!DateOfCustOpen.isEmpty()) {
                    LocalDate curDate = LocalDate.now();
                    Log.consoleLog(ifr, "curDate  :" + curDate);
                    LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
                    Log.consoleLog(ifr, "PastDate  :" + PastDate);
                    long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
                    Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
                    int YearsWithCanara = (int) (monthsBetween / 12);
                    Log.consoleLog(ifr, "YearsWithCanara: " + YearsWithCanara);

                    ifr.setValue("P_VL_OD_RelationshipWithCanara", String.valueOf(YearsWithCanara));
                    ifr.setStyle("P_VL_OD_RelationshipWithCanara", "disable", "true");
                }
            }
        } catch (Exception e) {

            Log.errorLog(ifr, "Exception getPortalDataLoadVL : " + e);
        }
        return "";
    }

    //modified by Sharon on 22/07/2024
    public String populateOccuapationDetailsforCoBorrowerVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "populateOccupationDetailsforCoBorrower VL: ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", PID);
            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "OccupationInfoDetails query Co-Obligant VL::" + IndexQuery);
            Log.consoleLog(ifr, "OccupationInfoDetails1 query Co-Obligant VL::" + IndexQuery1);

            List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query Co-Obligant VL");
            List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant VL");
            Log.consoleLog(ifr, "dataResult VL::" + dataResult);
            Log.consoleLog(ifr, "dataResult1 VL::" + dataResult1);

            if (dataResult1.isEmpty()) {
                Log.errorLog(ifr, "No basic information found for Co-Borrower.");
                return "No basic information found for Co-Borrower.";
            }

            String f_key = dataResult.isEmpty() ? dataResult1.get(0).get(0) : dataResult1.get(0).get(0);
            Log.consoleLog(ifr, "f_key VL::" + f_key);

            String portalFields = ConfProperty.getCommonPropertyValue("CBPortalOccupationDetailsFieldsVL");
            String[] portalFieldStr = portalFields.split(",");
            int size1 = portalFieldStr.length;
            String[] portalValue = new String[size1];
            String netIncome, grossSalary, deductionSalary;

            String occupationType = ifr.getValue("P_VL_OD_Profile_CB").toString();
            if ("Salaried".equalsIgnoreCase(occupationType) || "PEN".equalsIgnoreCase(occupationType)) {
                grossSalary = ifr.getValue("P_VL_OD_GrossSalary_CB").toString();
                netIncome = ifr.getValue("P_VL_OD_NetIncome_CB").toString();
                deductionSalary = ifr.getValue("P_VL_OD_DeductionFromSalary_CB").toString();
            } else {
                grossSalary = ifr.getValue("P_VL_OD_GrossAnnualSalary_CB").toString();
                netIncome = ifr.getValue("P_VL_OD_AnnualNetIncome_CB").toString();
                deductionSalary = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary_CB").toString();
            }
            Log.consoleLog(ifr, " grossSalary::" + grossSalary);
            Log.consoleLog(ifr, " deductionSalary::" + deductionSalary);
            Log.consoleLog(ifr, " netIncome::" + netIncome);

            for (int i = 0; i < size1; i++) {
                portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
            }

            String query;
            if (dataResult.isEmpty()) {
                query = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGridCoborrowerVL")
                        .replaceAll("#f_key#", f_key)
                        .replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1])
                        .replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3])
                        .replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5])
                        .replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7])
                        .replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9])
                        .replaceAll("#portalValue10#", netIncome)
                        .replaceAll("#portalValue11#", grossSalary)
                        .replaceAll("#portalValue12#", deductionSalary);

                Log.consoleLog(ifr, "Insert query for Occupation Info VL::CoBorrower ::" + query);
            } else {
                query = ConfProperty.getQueryScript("UpdateQueryForOccupationInfoGridCoborrowerVL")
                        .replaceAll("#f_key#", f_key)
                        .replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1])
                        .replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3])
                        .replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5])
                        .replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7])
                        .replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9])
                        .replaceAll("#portalValue10#", netIncome)
                        .replaceAll("#portalValue11#", grossSalary)
                        .replaceAll("#portalValue12#", deductionSalary);
                Log.consoleLog(ifr, "Update query for Occupation Info VL::CoBorrower ::" + query);
            }

            int count = ifr.saveDataInDB(query);
            Log.consoleLog(ifr, "Database update count VL::CoBorrower::" + count);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error in populateOccupationDetailsforCoBorrower VL: " + e);
            Log.errorLog(ifr, "Error in populateOccupationDetailsforCoBorrower VL: " + e);
        }
        return "";
    }

    public void populateCoborrowerDataVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "populateCoborrowerDataVL : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String PortalFieldCoBorrower = ConfProperty.getCommonPropertyValue("CBPortalOccupationDetailsFieldsVL");
            String portalfieldCoBorrowerArr[] = PortalFieldCoBorrower.split(",");
            String OccF_Key = bpcc.Fkey(ifr, "CB");
            Log.consoleLog(ifr, "resultOccF_KEY VL.." + OccF_Key);
            String CoBorrowerFieldsQuery = ConfProperty.getQueryScript("PORTALOCCUPATIONINFODATACOBORROWERVL").replaceAll("#F_Key#", OccF_Key);
            Log.consoleLog(ifr, "CoBorrowerFieldsQuery VL.." + CoBorrowerFieldsQuery);
            List<List<String>> CoBorrowerFieldsQueryresult1 = ifr.getDataFromDB(CoBorrowerFieldsQuery);
            Log.consoleLog(ifr, "CoBorrowerFieldsQueryresult1 VL.." + CoBorrowerFieldsQueryresult1);
            if (CoBorrowerFieldsQueryresult1.size() > 0) {
                for (int i = 0; i < portalfieldCoBorrowerArr.length; i++) {
                    String netIncome = CoBorrowerFieldsQueryresult1.get(0).get(10);
                    String deductionSalary = CoBorrowerFieldsQueryresult1.get(0).get(12);
                    String grossSalary = CoBorrowerFieldsQueryresult1.get(0).get(11);
                    Log.consoleLog(ifr, "deductionVL:::" + deductionSalary);
                    Log.consoleLog(ifr, "netincome:::" + netIncome);
                    Log.consoleLog(ifr, "gross:::" + grossSalary);

                    String occpationType = CoBorrowerFieldsQueryresult1.get(0).get(7);
                    Log.consoleLog(ifr, "occpationType : " + occpationType);
                    Log.consoleLog(ifr, "before if loop : ");
                    if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {

                        Log.consoleLog(ifr, "inside if occpationType : " + occpationType);
                        ifr.setValue("P_VL_OD_GrossSalary_CB", grossSalary);
                        ifr.setValue("P_VL_OD_NetIncome_CB", netIncome);
                        ifr.setValue("P_VL_OD_DeductionFromSalary_CB", deductionSalary);
                    } else {
                        Log.consoleLog(ifr, "inside else occpationType : " + occpationType);
                        ifr.setValue("P_VL_OD_GrossAnnualSalary_CB", grossSalary);
                        ifr.setValue("P_VL_OD_AnnualNetIncome_CB", netIncome);
                        ifr.setValue("P_VL_OD_AnnualDeductionFromSalary_CB", deductionSalary);
                    }
                    String occupation = CoBorrowerFieldsQueryresult1.get(0).get(0);
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:occupation:: " + occupation);
                    ifr.setValue("P_VL_OD_TypeOfOccupation_CB", occupation);
                    String NatureOfsecurity = "Hypothecation of durable utility article or vehicle to the extent of stipulation under the scheme";
                    ifr.addItemInCombo("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                    ifr.setValue("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                    ifr.setValue("P_VL_OD_CATEGORYC", "New");
                    ifr.setStyle("P_VL_OD_CATEGORYC", "disable", "true");

                    Log.consoleLog(ifr, "Inside if CoBorrowerFieldsQueryresult1 portalfieldCoBorrowerArr[i] VL==>" + i + " " + portalfieldCoBorrowerArr[i]);
                    ifr.setValue(portalfieldCoBorrowerArr[i], CoBorrowerFieldsQueryresult1.get(0).get(i));
                }
            }
            String applicantname = bpcc.mFullNameByApplicantName(ifr, "CB");
            //ifr.setValue("CoApplicantName_VL", applicantname);
            //added by ishwarya for back click validation on 19/06/2024
            OnClickCoApplicant(ifr, control);
            OnClickExistingCust(ifr);
            mOnchangeOccTypeIsSalariedVL(ifr);
            onChangeOccTypeCBVL(ifr);
            String strfkey = bpcc.Fkey(ifr, "B");
            Log.consoleLog(ifr, "strfkey::" + strfkey);
            String custType1 = "";
            String query = "SELECT EXISTINGCUSTOMER from LOS_NL_Occupation_INFO Where F_KEY='" + strfkey + "'";
            Log.consoleLog(ifr, "query is.." + query);
            List<List<String>> result = ifr.getDataFromDB(query);
            if (!result.isEmpty()) {
                custType1 = result.get(0).get(0);
            }
            Log.consoleLog(ifr, "custType1::" + custType1);
            String custType = ifr.getValue("P_VL_OD_EXISTING_CUSTOMER").toString();
            if (custType.equalsIgnoreCase("Yes") || custType1.equalsIgnoreCase("Yes")) {
                ifr.setStyle("CB_OCCUPATION_SECTION", "visible", "true");
                String NatureOfsecurity = "Hypothecation of durable utility article or vehicle to the extent of stipulation under the scheme";
                ifr.addItemInCombo("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                ifr.setValue("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
            }
            // Changes by Ishwarya for coborrower details validate button fix
            String CoborrowerCustID = ifr.getValue("P_VL_OD_CUSTOMER_ID").toString();
            Log.consoleLog(ifr, "populateCoborrowerData query:: Coborrower CustID " + CoborrowerCustID);
            if (!CoborrowerCustID.isEmpty() || !CoborrowerCustID.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "populateCoborrowerData :: Coborrower CustID =" + CoborrowerCustID + " is already validated in Customer account summary Api");
                ifr.setStyle("P_OD_ValidateCoObligantVL", "disable", "true");//modified by sharon for back click check
                ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "disable", "true");
                ifr.setStyle("P_VL_OD_CUSTOMER_ID", "disable", "true");
                ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "disable", "true");
                /*ifr.setStyle("P_VL_OD_EXISTING_CUSTOMER", "disable", "true");
                ifr.setStyle("P_VL_OD_Co_Applicant", "disable", "true");*/
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "error in populateCoborrowerData VL" + e);
            Log.errorLog(ifr, "error in populateCoborrowerData VL" + e);
        }
    }

    public String autoPopulateInPrincipleDataVL(IFormReference ifr, String value) {
        String sliderValue = "";
        try {
            Log.consoleLog(ifr, "inside autoPopulateInPrincipleDataVL  : ");
            pcm.setGetPortalStepName(ifr, value);
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String customername = null;
            String customerdata_Query = "SELECT a.FIRSTNAME FROM LOS_L_BASIC_INFO_I a INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY WHERE b.CUSTOMERFLAG = 'Y' AND b.PID ='" + ProcessInsanceId + "' and b.ApplicantType='B'";
            List<List<String>> list3 = cf.mExecuteQuery(ifr, customerdata_Query, "customerdata_Query:");
            if (list3.size() > 0) {
                customername = list3.get(0).get(0);
            }
            Log.consoleLog(ifr, "Customer Name : " + customername);
            ifr.setValue("P_VL_L_CUSNAME", "Congratulations! " + customername);
            String inprinciple = null;
            String amount_Query = "SELECT IN_PRINCIPLE_AMOUNT FROM LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInsanceId + "'";
            List<List<String>> list4 = cf.mExecuteQuery(ifr, amount_Query, "amount_Query:");
            if (list4.size() > 0) {
                inprinciple = list4.get(0).get(0);
            }
            Log.consoleLog(ifr, "inprinciple : " + inprinciple);
            double doubleValue = Double.parseDouble(inprinciple);
            long roundedValue = Math.round(doubleValue);
            long roundedString = Math.round(roundedValue / 100.0) * 100;
            Log.consoleLog(ifr, "roundedString : " + roundedString);
            String roundedLongValue = String.valueOf(roundedString);
            ifr.setValue("P_VL_PA_LOAN_AMOUNT", roundedLongValue);
            String schemeID = pcm.mGetSchemeIDVL(ifr, ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            //MODIFIED BY ISHWARYA FOR TENURE 
            String loanTenure = null;
            String tenureData_Query = "SELECT TENURE FROM LOS_NL_LA_INPRINCIPLE where PID ='" + ProcessInsanceId + "'";
            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
            if (list1.size() > 0) {
                loanTenure = list1.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanTenure vl: " + loanTenure);
            ifr.setValue("P_VL_PA_TENURE", loanTenure);
            ifr.setStyle("P_VL_PA_TENURE", "readonly", "true");
            String Gender = "";
            String Customersummary = "SELECT CUSTOMERSEX from LOS_Trn_Customersummary where WINAME='" + ProcessInsanceId + "'";
            Log.consoleLog(ifr, "Customersummary value  : " + Customersummary);
            List<List<String>> dbdata = cf.mExecuteQuery(ifr, Customersummary, "Execute query for fetching income data from temp LOS_Trn_Customersummary");
            Log.consoleLog(ifr, "dbdata value  : " + dbdata);
            if (dbdata.size() > 0) {
                Gender = dbdata.get(0).get(0);
                Log.consoleLog(ifr, "Gender==>" + Gender);
            }
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            Log.consoleLog(ifr, "VLportal::autoPopulateInPrincipleDataVL::Purpose  : " + Purpose);
            String f_key = bpcc.Fkey(ifr, "B");
            String reqLoanAmt = "";
            String reqLoanAmt_Query = "SELECT ELIGIBLELOANAMOUNT FROM LOS_NL_Occupation_INFO WHERE F_KEY ='" + f_key + "'";
            List<List<String>> list = cf.mExecuteQuery(ifr, reqLoanAmt_Query, "tenureData_Query FROM PORTAL:");
            if (list.size() > 0) {
                reqLoanAmt = list.get(0).get(0);
            }
            Log.consoleLog(ifr, "reqLoanAmt : " + reqLoanAmt);
            String loanROI = null;
            String SCHEDULECODE = "";
            if (Purpose.equalsIgnoreCase("N2W")) {
                if (Gender.equalsIgnoreCase("MALE")) {
                    SCHEDULECODE = "3005";
                    Log.consoleLog(ifr, "VLportal::autoPopulateInPrincipleDataVL::SCHEDULECODE  : " + SCHEDULECODE);
                } else if (Gender.equalsIgnoreCase("FEMALE")) {
                    SCHEDULECODE = "3004";
                    Log.consoleLog(ifr, "VLportal::autoPopulateInPrincipleDataVL::SCHEDULECODE  : " + SCHEDULECODE);
                }
            } else if (Purpose.equalsIgnoreCase("N4W")) {
                if (Gender.equalsIgnoreCase("MALE")) {
                    SCHEDULECODE = "3013";
                    Log.consoleLog(ifr, "VLportal::autoPopulateInPrincipleDataVL::SCHEDULECODE  : " + SCHEDULECODE);
                } else if (Gender.equalsIgnoreCase("FEMALE")) {
                    SCHEDULECODE = "3012";
                    Log.consoleLog(ifr, "VLportal::autoPopulateInPrincipleDataVL::SCHEDULECODE  : " + SCHEDULECODE);
                }
            }
            String roiData_Query = "select TOTALROI from los_m_roi where ROIID in (select ROIID from Los_m_Roi_Scheme where SCHEMEID in (Select SCHEMEID from los_m_product_rlos where SCHEMEID ='" + schemeID + "')) and CRG='CRG-3'  and ROITYPE='FLOATING' and SCHEDULECODE='" + SCHEDULECODE + "' AND " + reqLoanAmt + " BETWEEN MINLOANAMOUNT AND MAXLOANAMOUNT";
            List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
            if (list2.size() > 0) {
                loanROI = list2.get(0).get(0);
            }
            Log.consoleLog(ifr, "roi : " + loanROI);
            String ROI = loanROI + "%";
            ifr.setValue("P_VL_PA_RATE_OF_INTEREST", ROI);
            ifr.setStyle("P_VL_PA_RATE_OF_INTEREST", "readonly", "true");

            String Query1 = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#", ProcessInsanceId);
            List<List<String>> result = ifr.getDataFromDB(Query1);
            String tenure1 = "";
            String loanAmount1 = "";
            String loanAmount = "-" + inprinciple;
            //modified by ishwarya on 26/07/2024
            BigDecimal rate = new BigDecimal(loanROI);
            int tenure = Integer.parseInt(loanTenure);
            /*BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
            String emi = emicalc.toString();
            Log.consoleLog(ifr, "emi : " + emi);
            ifr.setValue("P_VL_PA_EMI", emi);*/
            if (result.size() > 0) {
                loanAmount1 = result.get(0).get(0);
                tenure1 = result.get(0).get(1);
                sliderValue = loanAmount1 + "," + tenure1;
            } else {
                loanAmount1 = ifr.getValue("P_VL_PA_LOAN_AMOUNT").toString();
                tenure1 = loanTenure;
                sliderValue = loanAmount1 + "," + tenure1;
            }
            String loanAmount2 = "-" + loanAmount1;
            BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount2, rate, Integer.parseInt(tenure1));
            String emi = emicalc.toString();
            Log.consoleLog(ifr, "emi : " + emi);
            ifr.setValue("P_VL_PA_EMI", emi);

            //added by ishwarya for saving data in knockoff-grid on 19/06/2024
            String PARTY_TYPE = "";
            String RULE_NAME = "";
            String OUTPUT = "";
            String mobile_no = "";
            String Fkey = bpcc.Fkey(ifr, "CB");
            String mobileNumber = "SELECT MOBILENO FROM LOS_L_BASIC_INFO_I where f_key='" + Fkey + "'";
            List<List<String>> mobileNumberQ = cf.mExecuteQuery(ifr, mobileNumber, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
            if (!mobileNumberQ.isEmpty()) {
                mobile_no = mobileNumberQ.get(0).get(0);
            }
            String substringToRemove = "91";
            mobile_no = mobile_no.replace(substringToRemove, "");
            Log.consoleLog(ifr, "mobile_no value  : " + mobile_no);
            String knockoffTemprules = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYVL").replaceAll("#mobileNumber#", mobile_no)
                    .replaceAll("#PARTY_TYPE#", "C");
            Log.consoleLog(ifr, "Customersummary value  : " + knockoffTemprules);
            List<List<String>> dbdata1 = cf.mExecuteQuery(ifr, knockoffTemprules, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
            Log.consoleLog(ifr, "dbdata1 value  : " + dbdata1);
            if (!dbdata1.isEmpty()) {
                PARTY_TYPE = dbdata1.get(0).get(0);
                RULE_NAME = dbdata1.get(0).get(1);
                OUTPUT = dbdata1.get(0).get(2);
            }
            String cbdata = "";
            String queryData = "SELECT concat(b.borrowertype, concat('-', c.fullname)), c.insertionOrderId FROM LOS_MASTER_BORROWER b INNER JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + ProcessInsanceId + "' AND  c.ApplicantType ='CB'";
            List<List<String>> bdata = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching co-borrower from temp LOS_TMP_Knockoff_Rules::");
            if (!bdata.isEmpty()) {
                cbdata = bdata.get(0).get(0);
            }
            Log.consoleLog(ifr, "cbdata::autoPopulateLoanDetailsDataCB:::" + cbdata);
            String knockoffRules = ConfProperty.getQueryScript("KNOCKOFFCOUNTVL").replaceAll("#ProcessInsanceId#", ProcessInsanceId)
                    .replaceAll("#PARTY_TYPE#", "C");;
            List<List<String>> knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            Log.consoleLog(ifr, "knockoffRules value  : " + knockoffRulesdata);

            if (knockoffRulesdata.isEmpty()) {
                String dataSavingGridQuery = ConfProperty.getQueryScript("KNOCKOFFINSERT").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#PARTY_TYPE#", cbdata)
                        .replaceAll("#RULE_NAME#", RULE_NAME).replaceAll("#OUTPUT#", OUTPUT).replaceAll("#f_key#", Fkey).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextVal");
                Log.consoleLog(ifr, "dataSavingGridQuery::::" + dataSavingGridQuery);
                ifr.saveDataInDB(dataSavingGridQuery);
            }
            knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
            String knockoffTemprules1 = ConfProperty.getQueryScript("KNOCKOFFTEMPQUERYVL1")
                    .replaceAll("#mobileNumber#", mobile_no)
                    .replaceAll("#PARTY_TYPE#", "C");
            Log.consoleLog(ifr, "knockoffTemprules1 value  : " + knockoffTemprules1);
            List<List<String>> dataSaveknockoffGridData = cf.mExecuteQuery(ifr, knockoffTemprules1, "Execute query for fetching knock-off data from temp LOS_TMP_Knockoff_Rules::");
            Log.consoleLog(ifr, "dbdata value  : " + dataSaveknockoffGridData);
            if (!dataSaveknockoffGridData.isEmpty()) {
                String RULENAME[] = new String[dataSaveknockoffGridData.size()];
                String OUT_PUT[] = new String[dataSaveknockoffGridData.size()];
                String f_key1 = knockoffRulesdata.get(0).get(5);
                Log.consoleLog(ifr, "f_key: " + f_key1);
                for (int i = 0; i < dataSaveknockoffGridData.size(); i++) {
                    Log.consoleLog(ifr, "Inside dataSaveknockoffGridData RULENAME::" + dataSaveknockoffGridData.get(i).get(0));
                    RULENAME[i] = dataSaveknockoffGridData.get(i).get(0);
                    Log.consoleLog(ifr, "Inside dataSaveknockoffGridData:: OUT_PUT" + dataSaveknockoffGridData.get(i).get(1));
                    OUT_PUT[i] = dataSaveknockoffGridData.get(i).get(1);
                    Log.consoleLog(ifr, "RULENAME:::OUT_PUT::" + RULENAME[i] + " " + OUT_PUT[i]);
                }
                String knockoffTable1 = ConfProperty.getQueryScript("KNOCKOFFCOUNTVL1").replaceAll("#f_key#", f_key1);
                Log.consoleLog(ifr, "knockoffTable1: " + knockoffTable1);
                List<List<String>> knockoffTableDATA1 = cf.mExecuteQuery(ifr, knockoffTable1, "knockoffTable1:");
                Log.consoleLog(ifr, "knockoffTableDATA1:::" + knockoffTableDATA1);
                if (knockoffTableDATA1.isEmpty()) {
                    for (int i = 0; i < RULENAME.length; i++) {
                        String dataSavingQuery1 = ConfProperty.getQueryScript("KNOCKOFFINSERT1").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#f_key#", f_key1)
                                .replaceAll("#RULENAME#", RULENAME[i]).replaceAll("#OUT_PUT#", OUT_PUT[i]).replaceAll("#InsertionOrderID#", "S_LOS_NL_K_KNOCKOFFRULES.nextVal");
                        Log.consoleLog(ifr, "dataSavingQuery1: " + dataSavingQuery1);
                        ifr.saveDataInDB(dataSavingQuery1);
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateInPrincipleDataCB " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateInPrincipleDataCB " + e);
        }
        return sliderValue;
    }

    //modified by ishwarya
    public String getPortalInPricipleApprovalVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside setValue getPortalInPricipleApprovalVL :");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String roi = ifr.getValue("P_VL_PA_RATE_OF_INTEREST").toString();
        String tenure = ifr.getValue("P_VL_PA_REQ_TENURE").toString();
        String emi = ifr.getValue("P_VL_PA_EMI").toString();
        String Query = ConfProperty.getQueryScript("UPDATEINPRINCIPLEDATAQUERY").replaceAll("#tenure#", tenure).replaceAll("#emi#", emi).replaceAll("#roi#", roi).replaceAll("#PID#", PID).replace("%", "").replace("₹", "");
        Log.consoleLog(ifr, "Query :" + Query);
        ifr.saveDataInDB(Query);

        String tenureElong = "";
        String query = "select Checkbox2  from LOS_PORTAL_SLIDERVALUE WHERE PID='" + PID + "'";
        Log.consoleLog(ifr, "Query1 data::" + query);
        List<List<String>> resultData = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "resultData::" + resultData.toString());
        if (resultData.size() > 0) {
            tenureElong = resultData.get(0).get(0);
            Log.consoleLog(ifr, "tenureElong VL::" + tenureElong);
        }
        ifr.setValue("P_VL_PA_CHKBX1_INPRNCPL", tenureElong);
        return "";
    }

    public String populateInPrincipalApprovalVL(IFormReference ifr, String control, String event, String value) {
        try {
            Log.consoleLog(ifr, "inside populateInPrincipalApprovalVL : ");

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID VL:::: " + PID);

            //added by sharon
            Log.consoleLog(ifr, "inside LTV brms:::");
            String OccupationType = "";
            String finalLTVAmount = "";
            String Purpose = "";
            String Ext = "";
            String f_key = "";
            String AssestCategory = "";
            String OccupationCategory = "";
            String VEHICLECOST = "";
            String loanAmount = "";

            String fkeyQuery = "SELECT b.EXISTINGCUSTOMER, b.f_key,o.PROFILE,o.CATEGORY as OccupationCategory,c.VEHICLETYPE, c.CATEGORY as VehicleCategory,c.VEHICLECOST,l.\"Loan_Amount\",l.\"Tenure\" \n"
                    + "FROM LOS_NL_BASIC_INFO b JOIN LOS_NL_Occupation_INFO o ON b.f_key = o.f_key JOIN LOS_NL_COLLATERAL_VEHICLES c ON b.PID = c.PID JOIN \n"
                    + "LOS_PORTAL_SLIDERVALUE l ON b.PID = l.PID WHERE b.PID='" + PID + "' and b.APPLICANTTYPE='B'";
            Log.consoleLog(ifr, "Fkey query::" + fkeyQuery);
            List<List<String>> dataResult = ifr.getDataFromDB(fkeyQuery);
            if (!dataResult.isEmpty()) {

                Ext = dataResult.get(0).get(0);
                f_key = dataResult.get(0).get(1);
                OccupationType = dataResult.get(0).get(2);
                OccupationCategory = dataResult.get(0).get(3);
                Purpose = dataResult.get(0).get(4);
                AssestCategory = dataResult.get(0).get(5);
                VEHICLECOST = dataResult.get(0).get(6);
                loanAmount = dataResult.get(0).get(7);

            }
            Log.consoleLog(ifr, "inside LTV brms Ext:::" + Ext);
            Log.consoleLog(ifr, "inside LTV brms f_key:::" + f_key);

            Log.consoleLog(ifr, "OccupationType:::" + OccupationType);
            Log.consoleLog(ifr, "OccupationCategory:::" + OccupationCategory);

            Log.consoleLog(ifr, "Purpose:::" + Purpose);
            Log.consoleLog(ifr, "AssestCategory:::" + AssestCategory);
            Log.consoleLog(ifr, "VEHICLECOST:::" + VEHICLECOST);
            Log.consoleLog(ifr, "reqLoanAmt:::" + loanAmount);
            if (Purpose.equalsIgnoreCase("N2W")) {
                Log.consoleLog(ifr, "iniside Purpose Two Wheeler:::" + Purpose);
                Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                //input_parameter,assetcategory_ip,existingcustomer_ip,occupationtype_ip,purpose_ip
                Log.consoleLog(ifr, "AssestCategory::" + AssestCategory + ",Ext::" + Ext
                        + ",OccupationType::" + OccupationType + ",Purpose::" + Purpose);
                String LTVTwoWheelerInParams = AssestCategory + "," + Ext + "," + OccupationType + "," + Purpose;
                finalLTVAmount = checkLTVTwowheeler(ifr, "VL_LTV_Twowheeler", LTVTwoWheelerInParams, "ltv_op");
                Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
            } else {
                Log.consoleLog(ifr, "iniside Purpose Four Wheeler:::" + Purpose);

                Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                //maxAmount
                //input_parameter,purpose_ip,occupationtype_ip,occupationcategory_ip,loanamt_ip,assetcategory_ip,
                Log.consoleLog(ifr, "Purpose::" + Purpose + ",OccupationType::" + OccupationType
                        + ",OccupationCategory::" + OccupationCategory + ",reqLoanAmt::" + loanAmount + ",AssestCategory::" + AssestCategory);
                String LTVFourWheelerInParams = Purpose + "," + OccupationType + "," + OccupationCategory + "," + loanAmount + "," + AssestCategory;
                // Purchase of New Four-Wheeler,Salaried,State Government,,State Government

                finalLTVAmount = checkLTVFourwheeler(ifr, "VL_LTV_Fourwheeler", LTVFourWheelerInParams, "ltv_op");
                Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
            }
            BigDecimal ftReqAmount = pcm.mCheckBigDecimalValue(ifr, loanAmount);

            BigDecimal onRoadPrice = pcm.mCheckBigDecimalValue(ifr, VEHICLECOST);
            BigDecimal ftLTV = pcm.mCheckBigDecimalValue(ifr, finalLTVAmount);
            Log.consoleLog(ifr, "eligible LTV ===> " + ftLTV);
            BigDecimal eligLTV = (ftReqAmount.divide(onRoadPrice, 2, RoundingMode.HALF_DOWN)).multiply(new BigDecimal(100));
            Log.consoleLog(ifr, "calculated LTV ===> " + eligLTV);
            BigDecimal finalLTV;
            BigDecimal finalLTVMargin;
            if (eligLTV.compareTo(ftLTV) <= 0) {
                finalLTVMargin = eligLTV;
            } else {
                finalLTVMargin = ftLTV;
            }
            Log.consoleLog(ifr, "final LTV taken ===> " + finalLTVMargin);
            finalLTV = finalLTVMargin.multiply(onRoadPrice).divide(new BigDecimal(100));
            Log.consoleLog(ifr, "final LTV taken bt mul onRoadPrice ===> " + finalLTV);

            String updateLTV = ConfProperty.getQueryScript("updateLTV")
                    .replaceAll("#Pid#", PID).replaceAll("#finalLTVMargin#", finalLTVMargin.toString()).
                    replaceAll("#finalLTV#", finalLTV.toString());
            Log.consoleLog(ifr, "updateLTV ===> " + updateLTV);
            ifr.saveDataInDB(updateLTV);
            Log.consoleLog(ifr, "LTV update Success updatedLTV ===> " + updateLTV);
            ///LTV update End

            Purpose = Purpose.equalsIgnoreCase("Purchase of New Two-wheeler") ? "N2W" : "N4W";

            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL::productCode:" + productCode);
            String subProduct = pcm.mGetSubproductVL(ifr, Purpose);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL subProductCode:" + subProduct);
            String tenure = "";
            String roi = ifr.getValue("P_VL_PA_RATE_OF_INTEREST").toString();
            String InprincipleQ = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL query " + InprincipleQ);
            List<List<String>> Inprinciple = ifr.getDataFromDB(InprincipleQ);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL Inprinciple " + Inprinciple);
            if (!Inprinciple.isEmpty()) {
                tenure = Inprinciple.get(0).get(2);
            }
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL tenure:" + tenure);
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTINPRINAPPROVAL").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL query " + IndexQuery);
            List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
            Log.consoleLog(ifr, "populateInPrincipalApprovalVL docIndexList " + docIndexList);
            if (docIndexList.isEmpty()) {
                String queryvalue = ConfProperty.getQueryScript("PORTALINPRINAPPROVALINSERTVL")
                        .replaceAll("#PRODUCT#", productCode)
                        .replaceAll("#SUBPRODUCT#", subProduct).replaceAll("#LOANPURPOSE#", Purpose)
                        .replaceAll("#loanAmount#", loanAmount).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi).replaceAll("#PID#", PID).replace("%", "").replace("₹", "")
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "populateInPrincipalApprovalVL Info queryvalue:" + queryvalue);
                ifr.saveDataInDB(queryvalue);
            } else {
                String queryvalue = ConfProperty.getQueryScript("PORTALINPRINAPPROVALVALUEVL")
                        .replaceAll("#PRODUCT#", productCode)
                        .replaceAll("#SUBPRODUCT#", subProduct).replaceAll("#LOANPURPOSE#", Purpose)
                        .replaceAll("#loanAmount#", loanAmount).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi).replaceAll("#PID#", PID).replace("%", "").replace("₹", "")
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_PROPOSED_FACILITY.nextVal");
                Log.consoleLog(ifr, "populateInPrincipalApprovalVL Info queryvalue:" + queryvalue);
                ifr.saveDataInDB(queryvalue);
            }
            // Added By Janani for tenure opt for elongation 
            String checkbox1 = ifr.getValue("P_VL_PA_CHKBX1_INPRNCPL").toString();

            Log.consoleLog(ifr, "inside tenure elongation product ");
            if (checkbox1.equalsIgnoreCase("true")) {
                String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", PID).replaceAll("#checkbox2#", checkbox1);
                Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
                ifr.saveDataInDB(queryCheckbox);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "inside populateInPrincipalApprovalVL ::: " + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
        return "";
    }

    //modified by Ishwarya on 06-08-2024    
    public String validateDocumentUpload(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside VLPortalCustomCode:validateDocumentUpload::");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            int gridSize = ifr.getDataFromGrid("VL_UPLOAD_DOCUMENT").size();
            HashMap<String, String> docMap = new HashMap<>();
            if (gridSize > 0) {
                String documentName[] = new String[gridSize];
                String uploadedDate[] = new String[gridSize];
                String applicantType[] = new String[gridSize];
                for (int i = 0; i < gridSize; i++) {
                    documentName[i] = ifr.getTableCellValue("VL_UPLOAD_DOCUMENT", i, 1);
                    uploadedDate[i] = ifr.getTableCellValue("VL_UPLOAD_DOCUMENT", i, 4);
                    applicantType[i] = ifr.getTableCellValue("VL_UPLOAD_DOCUMENT", i, 3).split("-")[0];
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload->Show Documents:" + documentName[i] + " - " + applicantType[i] + " - " + uploadedDate[i]);
                    docMap.put(documentName[i] + "-" + applicantType[i], uploadedDate[i]);
                }
                String schemeID = pcm.mGetSchemeIDVL(ifr, ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                //String docQuery = "Select a.DocumentName from los_m_document a inner join LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='" + schemeID + "' and DOCTYPE ='Inward'  and a.Mandatory='Yes'";
                String FkeyB = bpcc.Fkey(ifr, "B");
                String occupationTypeB = checkSalariedOrNot(ifr, FkeyB);
                Log.consoleLog(ifr, "occupationTypeB : " + occupationTypeB);
                String occupationTypeCB = "";

                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b  inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID = '" + PID + "' and (c.ApplicantType='B' or c.ApplicantType='CB')";
                Log.consoleLog(ifr, "Query1 data::" + Query1);
                List<List<String>> resultData = ifr.getDataFromDB(Query1);
                Log.consoleLog(ifr, "resultData::" + resultData.toString());
                String applicantName[] = new String[resultData.size()];
                if (resultData.size() > 0) {
                    for (int i = 0; i < resultData.size(); i++) {
                        applicantName[i] = resultData.get(i).get(0);
                        Log.consoleLog(ifr, "applicantName is " + Arrays.toString(applicantName));
                    }
                }

                for (int i = 0; i < applicantName.length; i++) {
                    String applicantNameArrayData = applicantName[i];
                    Log.consoleLog(ifr, "applicantNameArrayData : " + applicantNameArrayData);
                    if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                        Log.consoleLog(ifr, " inside Co Borrower");
                        String FkeyCB = bpcc.Fkey(ifr, "CB");
                        occupationTypeCB = checkSalariedOrNot(ifr, FkeyCB);
                        Log.consoleLog(ifr, "occupationTypeCB : " + occupationTypeCB);
                    }
                }
                String docQuery = "SELECT  a.DocumentName,a.APPLICABLEFOR FROM los_m_document a INNER JOIN LOS_M_DOCUMENT_Scheme b ON b.Documentid = a.Documentid WHERE b.schemeid = '" + schemeID + "' AND a.ISACTIVE='Y'  AND DOCTYPE = 'Inward'  AND a.Mandatory = 'Yes' AND ((a.APPLICABLEFOR = 'B' AND a.OCCUPATIONTYPE != '" + occupationTypeB + "') OR (a.APPLICABLEFOR = 'CB' AND a.OCCUPATIONTYPE != '" + occupationTypeCB + "'))";
                List<List<String>> docQueryResult = cf.mExecuteQuery(ifr, docQuery, "Query for docQuery::");
                int chkDocMandotoryCount = 0;
                int documentName_Length = documentName.length;
                int docQueryResult_size = documentName.length;
                Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload-> documentName size from Form" + documentName_Length);

                Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload-> docQueryResult size from DB" + docQueryResult_size);

                for (int i = 0; i < docQueryResult.size(); i++) {
                    String strDocName = docQueryResult.get(i).get(0);
                    String strApplicantType = docQueryResult.get(i).get(1);
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload->strApplicantType::" + strApplicantType);
                    if ("B".equalsIgnoreCase(strApplicantType)) {
                        strApplicantType = "Borrower";
                    } else if ("CB".equalsIgnoreCase(strApplicantType)) {
                        strApplicantType = "Co Borrower";
                    }
                    strDocName = strDocName + "-" + strApplicantType;
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload->strDocName::" + strDocName);
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload->docMap->strDocName::" + docMap.get(strDocName));
                    if (docMap.get(strDocName).equalsIgnoreCase("")) {
                        Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload-> Mandatory doc not uploaded is " + strDocName);
                        chkDocMandotoryCount++;
                    }
                }
                Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload->chkDocMandotoryCount::" + chkDocMandotoryCount);
                if (chkDocMandotoryCount > 0) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please upload mandatory document!!"));
                    message.put("eflag", "false");//Hard Stop
                    return message.toString();
                } //Modified by Sharon on 31/07/2024
                else {
                    String ApplicationNo = "";
                    String BranchName = "";
                    String branchcodeQuery = ConfProperty.getQueryScript("branchcodeQuery").replaceAll("#ProcessInstanceId#", PID);
                    List<List<String>> branchcodeData = cf.mExecuteQuery(ifr, branchcodeQuery, "Execute query for fetching branch code data");
                    int branchCode1 = Integer.parseInt(branchcodeData.get(0).get(0));
                    String branchCode = String.format("%05d", branchCode1);
                    String branchCodeLeadingZero = String.format("%05d", Integer.parseInt(branchCode));
                    Log.consoleLog(ifr, "branchCodeLeadingZero" + branchCodeLeadingZero);
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload:::ApplicationNo::BranchName::" + ApplicationNo);
                    String query1 = ConfProperty.getQueryScript("PORTALAPPLICATIONNOQUERY").replaceAll("#WINAME#", PID);
                    List<List<String>> ApplicationNoList = cf.mExecuteQuery(ifr, query1, "ApplicationNoList Query::");
                    if (!ApplicationNoList.isEmpty()) {
                        ApplicationNo = ApplicationNoList.get(0).get(0);
                    }
                    String Purpose = null;
                    String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", PID);
                    List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
                    if (PurposePortal.size() > 0) {
                        Purpose = PurposePortal.get(0).get(0);
                    }
                    if (Purpose.equalsIgnoreCase("N4W")) {
                        String branchQuery = ConfProperty.getQueryScript("branchQuery").replaceAll("#branchCode#", branchCodeLeadingZero);
                        List<List<String>> BranchList = cf.mExecuteQuery(ifr, branchQuery, "BranchList Query::");
                        if (!BranchList.isEmpty()) {
                            BranchName = BranchList.get(0).get(1);
                        }
                    } else {
                        String branchQuery = ConfProperty.getQueryScript("branchQuery").replaceAll("#branchCode#", branchCodeLeadingZero);
                        List<List<String>> BranchList = cf.mExecuteQuery(ifr, branchQuery, "BranchList Query::");
                        if (!BranchList.isEmpty()) {
                            BranchName = BranchList.get(0).get(0);
                        }
                    }
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload:::ApplicationNo::::" + ApplicationNo);
                    Log.consoleLog(ifr, "VLPortalCustomCode:validateDocumentUpload:::BranchName::::" + BranchName);
                    Log.consoleLog(ifr, "Inside update count Wf done ::");
                    String Updatestepname = ConfProperty.getQueryScript("Updatestepname").replaceAll("#WINAME#", PID);
                    String query = ConfProperty.getQueryScript("PORTALUPDATECOUNTWFDONE").replaceAll("#WINAME#", PID);
                    ifr.saveDataInDB(query);
                    cf.mExecuteQuery(ifr, Updatestepname, "Query for Updatestepnamequery:VLPortalCustomCode:validateDocumentUpload:");

                    return "Yes" + "~" + ApplicationNo + "~" + BranchName;

                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in VLPortalCustomCode:validateDocumentUpload::" + e);
        }
        return "";
    }

    private static String getRangeKey(int value) {
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

    private static int getCountInRange(Map<Integer, Integer> countMap, String rangeKey) {
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

    public String mImpCoObligantCheck(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "inside mImpCoObligantCheck ::");

            String CustomerId = ifr.getValue("P_VL_OD_CUSTOMER_ID").toString();
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            String MobileNo = ifr.getValue("P_VL_OD_MOBILE_NUMBER").toString();
            Log.consoleLog(ifr, "MobileNo==>" + MobileNo);
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", MobileNo);
            customerdetails.put("CustomerId", CustomerId);

            HashMap<String, String> customerdetails1 = new HashMap<>();
            customerdetails1.put("MobileNumber", MobileNo);
            customerdetails1.put("customerId", CustomerId);

            String AadharNo = cas.getAadharCustomerAccountSummary(ifr, customerdetails1);
            Log.consoleLog(ifr, "AadharNo==>" + AadharNo);
            if (AadharNo.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }
            String response = cas.getCustomerAccountSummary(ifr, customerdetails);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB===>" + response);

            if (response.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }

            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String PanNumber, Age, NRI, staffcheck;
            AadharNo = AadharNo.equalsIgnoreCase("") ? "No" : "Yes";
            Log.consoleLog(ifr, "aadhar:::" + AadharNo);
            PanNumber = OutputJSON.get("PanNumber").toString();
            PanNumber = PanNumber.equalsIgnoreCase("") ? "No" : "Yes";
            Log.consoleLog(ifr, "pan:::" + PanNumber);
            NRI = OutputJSON.get("NRI").toString();
            Log.consoleLog(ifr, "nri:::" + NRI);
            staffcheck = OutputJSON.get("Staff").toString();
            Log.consoleLog(ifr, "staffcheck:::" + staffcheck);

            String APIResponse = mGetAPIData(ifr);
            if (APIResponse.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            }
            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String CustMisStatus = cf.getJsonValue(obj, "CustMisStatus");
            Log.consoleLog(ifr, "CustMisStatus==>" + CustMisStatus);
            CustMisStatus = "Complete";
            String DOB = cf.getJsonValue(obj, "DOB");
            Log.consoleLog(ifr, "DOB:::: " + DOB);
            Age = cf.getJsonValue(obj, "Age");
            String productCode = "VL";
            String Classification = cf.getJsonValue(obj, "Classification");
            Classification = Classification.equalsIgnoreCase("NA") ? "No" : "Yes";
            String NRO = NRI;
            String Applicanttype = "CB";
            Log.consoleLog(ifr, "Before knockoffDecision:::: ");
            Log.consoleLog(ifr, "Applicanttype :: " + Applicanttype + ",CustomerAge::" + Age + ", Aadhar:: "
                    + AadharNo + " ,Pan:: " + PanNumber + " ,productCode:: " + productCode + ",CustMisStatus::" + CustMisStatus
                    + " ,Classification:: " + Classification + " ,Nri:: " + NRI + " ,NRO:: " + NRO + ",staffcheck::" + staffcheck
                    + " ,WriteOffDate:: " + DOB
            );
            String knockoffInParams = Applicanttype + "," + Age + "," + AadharNo + "," + PanNumber + ","
                    + productCode + "," + CustMisStatus + "," + Classification + "," + NRI
                    + "," + NRO + "," + staffcheck + "," + DOB;

            String knockoffDecision = checkKnockOff(ifr, "VL_KNOCKOFFRULE", knockoffInParams, "totalknockoff_op");
            if (knockoffDecision.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else if (knockoffDecision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "knockoff Passed Successfully:::");
                return knockoffDecision;
            } else {
                Log.consoleLog(ifr, "knockoff Failed :::" + knockoffDecision);
                return knockoffDecision;
            }

        } catch (ParseException e) {
            Log.errorLog(ifr, "Error occured in mImpCoObligantCheck" + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
    }

    //mofified by ishwarya on 26-07-2024
    public String mOnchangeOccTypeIsSalariedVL(IFormReference ifr) {
        Log.consoleLog(ifr, "inside in mOnchangeOccTypeIsSalariedVL");
        try {
            String occpationType = ifr.getValue("P_VL_OD_Profile").toString();
            Log.consoleLog(ifr, "occpationType : " + occpationType);
            if (occpationType.equalsIgnoreCase("Salaried")) {
                ifr.setStyle("P_VL_OD_Status", "visible", "true");
                ifr.setStyle("P_VL_OD_Status", "mandatory", "true");
                ifr.setStyle("P_VL_OD_GrossSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome", "disable", "true");
                ifr.setStyle("P_VL_OD_DeductionFromSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualNetIncome", "visible", "false");
                ifr.setStyle("P_VL_OD_DATEOFSUPERANNUATION", "mandatory", "true");
                ifr.setStyle("P_VL_OD_GrossSalary", "mandatory", "true");
                ifr.setStyle("P_VL_OD_DeductionFromSalary", "mandatory", "true");
                ifr.setStyle("P_VL_OD_NetIncome", "mandatory", "true");
                ifr.setStyle("P_VL_OD_DATEOFSUPERANNUATION", "visible", "true");
                ifr.setStyle("P_VL_OD_ExperienceYear", "visible", "true");
                ifr.setStyle("P_VL_OD_OverAllExperience", "visible", "true");
                ifr.setStyle("P_VL_OD_Designation", "visible", "true");
            } else if (occpationType.equalsIgnoreCase("PEN")) {
                ifr.setStyle("P_VL_OD_Status", "visible", "false");
                ifr.setStyle("P_VL_OD_ExperienceYear", "visible", "false");
                ifr.setStyle("P_VL_OD_OverAllExperience", "visible", "true");
                ifr.setStyle("P_VL_OD_GrossSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome", "disable", "true");
                ifr.setStyle("P_VL_OD_Designation", "visible", "false");
                ifr.setStyle("P_VL_OD_DeductionFromSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_DATEOFSUPERANNUATION", "visible", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualNetIncome", "visible", "false");
            } else if (occpationType.equalsIgnoreCase("SELF") || occpationType.equalsIgnoreCase("PROF")) {
                ifr.setStyle("P_VL_OD_Status", "visible", "false");
                ifr.setStyle("P_VL_OD_GrossSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_NetIncome", "visible", "false");
                ifr.setStyle("P_VL_OD_DeductionFromSalary", "visible", "false");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome", "visible", "true");
                ifr.setStyle("P_VL_OD_ExperienceYear", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome", "disable", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary", "mandatory", "true");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary", "mandatory", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome", "mandatory", "true");
                ifr.setStyle("P_VL_OD_DATEOFSUPERANNUATION", "visible", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in mOnchangeOccTypeIsSalariedVL" + e);
        }
        return "";
    }

    //mofified by ishwarya on 07-09-2024
    public String populateOccuapationDetailsVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "populateOccuapationDetailsVL : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
            String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO1").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "OcuppationInfoDetails query::" + IndexQuery);
            Log.consoleLog(ifr, "OcuppationInfoDetails1 query::" + IndexQuery1);
            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
            List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);
            if (dataResult.isEmpty()) {
                String f_key = dataResult1.get(0).get(0);
                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsVL");
                Log.consoleLog(ifr, "portalFields::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, "portalFieldStr[]::" + Arrays.toString(portalFieldStr));
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1::" + size1);
                String portalValue[] = new String[size1];
                String netIncome = "";
                String grossSalary = "";
                String deductionSalary = "";
                String occpationType = ifr.getValue("P_VL_OD_Profile").toString();
                Log.consoleLog(ifr, "occpationType : " + occpationType);
                Log.consoleLog(ifr, "before if loop : ");
                if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {
                    Log.consoleLog(ifr, "inside if occpationType : " + occpationType);
                    grossSalary = ifr.getValue("P_VL_OD_GrossSalary").toString();
                    netIncome = ifr.getValue("P_VL_OD_NetIncome").toString();
                    deductionSalary = ifr.getValue("P_VL_OD_DeductionFromSalary").toString();
                } else {
                    Log.consoleLog(ifr, "inside else occpationType : " + occpationType);
                    grossSalary = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
                    netIncome = ifr.getValue("P_VL_OD_AnnualNetIncome").toString();
                    deductionSalary = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary").toString();
                }
                Log.consoleLog(ifr, "before for loop : " + Arrays.toString(portalValue));
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                Log.consoleLog(ifr, "portalFields array portalValue[i]::" + Arrays.toString(portalValue));
                String insertQuery = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGridVL").
                        replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0]).
                        replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2]).
                        replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4]).
                        replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6]).
                        replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8]).
                        replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10]).
                        replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12]).
                        replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14]).
                        replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16]).
                        replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18]).
                        replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20]).
                        replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22]).
                        replaceAll("#portalValue23#", netIncome).replaceAll("#portalValue24#", grossSalary).
                        replaceAll("#portalValue25#", deductionSalary).replaceAll("#portalValue26#", portalValue[26]);
                Log.consoleLog(ifr, "insertQuery for Occupation Info::" + insertQuery);
                ifr.saveDataInDB(insertQuery);
            } else {
                String f_key = dataResult1.get(0).get(0);
                String portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsVL");
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, "portalFieldStr[]::" + Arrays.toString(portalFieldStr));
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalFields size1::" + size1);
                String portalValue[] = new String[size1];
                Log.consoleLog(ifr, "before for loop : " + Arrays.toString(portalValue));
                for (int i = 0; i < portalFieldStr.length; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                String netIncome = "";
                String grossSalary = "";
                String deductionSalary = "";
                String occpationType = ifr.getValue("P_VL_OD_Profile").toString();
                Log.consoleLog(ifr, "occpationType : " + occpationType);
                Log.consoleLog(ifr, "before if loop : ");
                if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {
                    Log.consoleLog(ifr, "inside if occpationType : " + occpationType);
                    grossSalary = ifr.getValue("P_VL_OD_GrossSalary").toString();
                    netIncome = ifr.getValue("P_VL_OD_NetIncome").toString();
                    deductionSalary = ifr.getValue("P_VL_OD_DeductionFromSalary").toString();
                } else {
                    Log.consoleLog(ifr, "inside else occpationType : " + occpationType);
                    grossSalary = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
                    netIncome = ifr.getValue("P_VL_OD_AnnualNetIncome").toString();
                    deductionSalary = ifr.getValue("P_VL_OD_AnnualDeductionFromSalary").toString();
                }
                String updateQuery = ConfProperty.getQueryScript("UpdateQueryForOccupationInfoGridVL").
                        replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0]).
                        replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2]).
                        replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4]).
                        replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6]).
                        replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8]).
                        replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10]).
                        replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12]).
                        replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14]).
                        replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16]).
                        replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18]).
                        replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20]).
                        replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22]).
                        replaceAll("#portalValue23#", portalValue[23]).
                        replaceAll("#portalValue24#", netIncome).replaceAll("#portalValue25#", grossSalary).
                        replaceAll("#portalValue26#", deductionSalary);
                Log.consoleLog(ifr, "update for Occupation Info::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "updateOccuapationDetails::" + count);
            }
            String IndexQuery3 = ConfProperty.getQueryScript("ROWINDEXCOUNTCOLLATERALINFO").replaceAll("#PID#", PID);
            List<List<String>> dataResult3 = ifr.getDataFromDB(IndexQuery3);
            Log.consoleLog(ifr, "dataResult3::" + dataResult3);
            String count1 = dataResult3.get(0).get(0);
            if (Integer.parseInt(count1) == 0) {
                String portalFields = ConfProperty.getCommonPropertyValue("PortalCollateralDetailsFieldsVL");
                Log.consoleLog(ifr, "portalCollateralFields::" + portalFields);
                String portalFieldStr[] = portalFields.split(",");
                Log.consoleLog(ifr, "portalCollateralFieldStr[]::" + Arrays.toString(portalFieldStr));
                int size1 = portalFieldStr.length;
                Log.consoleLog(ifr, "portalCollateralFields size1::" + size1);
                String portalValue[] = new String[size1];
                for (int i = 0; i < size1; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                Log.consoleLog(ifr, "portalCollateralFields array portalValue[i]::" + Arrays.toString(portalValue));
                String insertQuery = ConfProperty.getQueryScript("InsertQueryForCollateralInfoGrid")
                        .replaceAll("#PID#", PID).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9])
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_COLLATERAL_VEHICLES.nextval")
                        .replaceAll("#portalValue10#", portalValue[10]);
                Log.consoleLog(ifr, "insertQuery for Collateral Info::" + insertQuery);
                ifr.saveDataInDB(insertQuery);
            } else {
                String portalFields = ConfProperty.getCommonPropertyValue("PortalCollateralDetailsFieldsVL");
                String portalFieldStr[] = portalFields.split(",");
                int size1 = portalFieldStr.length;
                String portalValue[] = new String[size1];
                for (int i = 0; i < portalFieldStr.length; i++) {
                    portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
                }
                String updateQuery = ConfProperty.getQueryScript("UpdateQueryForCollateralInfoGrid")
                        .replaceAll("#PID#", PID).replaceAll("#portalValue0#", portalValue[0])
                        .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
                        .replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
                        .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
                        .replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
                        .replaceAll("#portalValue9#", portalValue[9])
                        .replaceAll("#InsertionOrderID#", "S_LOS_NL_COLLATERAL_VEHICLES.nextval")
                        .replaceAll("#portalValue10#", portalValue[10]);
                Log.consoleLog(ifr, "update for Collateral Info::" + updateQuery);
                int count = ifr.saveDataInDB(updateQuery);
                Log.consoleLog(ifr, "updateCollateralDetails::" + count);

            }
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", PID);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            Log.consoleLog(ifr, "VLPortalCustomCode Purpose:" + Purpose);
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "VLPortalCustomCode schemeID:" + schemeID);
            String schemeCodeUpdationQuery = "UPDATE LOS_EXT_TABLE SET SCHEMEID = '" + schemeID + "' WHERE PID ='" + PID + "'";
            Log.consoleLog(ifr, "VLPortalCustomCode schemeCodeUpdationQuery ::::" + schemeCodeUpdationQuery);
            int Result = ifr.saveDataInDB(schemeCodeUpdationQuery);
            Log.consoleLog(ifr, "### VLPortalCustomCode scheme Code Updated ###" + Result);
        } catch (NumberFormatException e) {
            Log.errorLog(ifr, "error in populateOccuapationDetailsVL" + e);
        }
        return "";
    }

    public String checkScorecardbackOffice(IFormReference ifr) {
        String scorecardDecision = "";
        try {
            Log.consoleLog(ifr, "inside checkScorecardbackOffice:::");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String RecommendLoanAmt = ifr.getValue("FE_Recommended_Loan_Amount").toString();
            Log.consoleLog(ifr, "RecommendLoanAmt:: " + RecommendLoanAmt);
            if (!RecommendLoanAmt.isEmpty()) {
                Log.consoleLog(ifr, "inside scorecard for backoffice brms:::");
                String npa_inp = "";
                String bscore = "";
                String overduedays_inp = "";
                String existcust_inp = "";
                String overduedaysupto = "";
                int maxOccurrences = 0;
                String maxOccurrencesNumber = null;
                String grossSalary = "";
                String occuType = "";
                String netIncome = "";
                String deductionSalary = "";
                String category = "";
                String expYears = "";
                String residence_inp = "";
                String monthlydeduction = "0";

                String APIResponse = mGetAPIData(ifr);
                Log.consoleLog(ifr, "Entered into mGetAPIData:::");

                if (APIResponse.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorAPIThroughExecute(ifr);
                }
                JSONParser jp = new JSONParser();
                JSONObject obj = (JSONObject) jp.parse(APIResponse);
                String scoreQuery = ConfProperty.getQueryScript("getScoreQuery").replaceAll("#ProcessInstanceId#", PID);
                List<List<String>> Result1 = cf.mExecuteQuery(ifr, scoreQuery, "Execute query for fetching scoreQuery data");
                if (!Result1.isEmpty()) {

                    bscore = Result1.get(0).get(0);
                    npa_inp = Result1.get(0).get(1);
                    npa_inp = npa_inp.equalsIgnoreCase("NA") ? "No" : "Yes";
                    Log.consoleLog(ifr, "bscore:::: " + bscore);
                    monthlydeduction = Result1.get(0).get(3);
                    monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;
                    Log.consoleLog(ifr, "monthlydeduction:::: " + monthlydeduction);
                }
                Log.consoleLog(ifr, "before query");
                String paymentHistoryIp = "";
                String paymentquery = "SELECT NVL(REGEXP_REPLACE(PAYMENTHISTORY, '[^0-9]', ''), 0) AS replaced_column FROM LOS_CAN_IBPS_BUREAUCHECK where PROCESSINSTANCEID='" + PID + "'";

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
                String IncomeQuery = ConfProperty.getQueryScript("getIncomeQuery").replaceAll("#ProcessInsanceId#", PID);
                List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching customer data");
                if (!IncomeDtsPortal.isEmpty()) {
                    occuType = IncomeDtsPortal.get(0).get(0);
                    grossSalary = IncomeDtsPortal.get(0).get(1);
                    netIncome = IncomeDtsPortal.get(0).get(2);
                    category = IncomeDtsPortal.get(0).get(4);
                    residence_inp = IncomeDtsPortal.get(0).get(5);
                    deductionSalary = IncomeDtsPortal.get(0).get(6);
                    if (occuType.equalsIgnoreCase("PEN")) {
                        expYears = IncomeDtsPortal.get(0).get(3);
                    } else {
                        expYears = IncomeDtsPortal.get(0).get(7);
                    }
                    Log.consoleLog(ifr, "GROSSSALARY ::" + grossSalary);
                    Log.consoleLog(ifr, "NETSALARY ::" + netIncome);
                    Log.consoleLog(ifr, "OVER_ALL_EXPERIENCE ::" + expYears);
                    Log.consoleLog(ifr, "CATEGORY ::" + category);
                    Log.consoleLog(ifr, "RESIDENCE ::" + residence_inp);
                }
                String natureOfSecurity = "Only Third party personal Guarantee";
                String recovery = "Salary Account";
                String AccountHoldertypeCode = cf.getJsonValue(obj, "AccountHoldertypeCode");
                AccountHoldertypeCode = AccountHoldertypeCode.equalsIgnoreCase("7") ? "Yes" : "No";
                String Age = cf.getJsonValue(obj, "Age");
                String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
                writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
                String guarantorwriteoff = AccountHoldertypeCode;
                String accountstatus = cf.getJsonValue(obj, "Account_Status");
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
                String existcust_inpQuery = "SELECT EXISTINGCUSTOMER FROM LOS_NL_BASIC_INFO where PID = '" + PID + "' ";
                List<List<String>> Results = ifr.getDataFromDB(existcust_inpQuery);
                if (!Results.isEmpty()) {
                    existcust_inp = Results.get(0).get(0);
                    existcust_inp = existcust_inp.equalsIgnoreCase("Yes") ? "Existing" : "New";
                }
                // String RecommendLoanAmount = "";
//                String amount_Query = ConfProperty.getQueryScript("GetRecommendLoanAmount").replaceAll("#PID#", PID);
//                Log.consoleLog(ifr, "RecommendLoanAmount_Query: " + amount_Query);
//                List<List<String>> Results1 = ifr.getDataFromDB(amount_Query);

                if (!RecommendLoanAmt.isEmpty()) {
                    // RecommendLoanAmount = Results1.get(0).get(0);
                    Log.consoleLog(ifr, "RecommendLoanAmount " + RecommendLoanAmt);
                } else {
                    JSONObject message = new JSONObject();
                    Log.consoleLog(ifr, "RecommendLoanAmount  is emptyy" + RecommendLoanAmt);
                    message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "error", "Please enter the valid Recommended loan amount in Eligibilty and save to fetch Scorecard Rating!"));
                    return message.toString();
                }
                String Purpose = null;
                String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", PID);
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
                if (PurposePortal.size() > 0) {
                    Purpose = PurposePortal.get(0).get(0);
                }
                String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String loanTenure = null;
                String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", PID);
                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
                if (list1.size() > 0) {
                    loanTenure = list1.get(0).get(2);
                }
                Log.consoleLog(ifr, "loanTenure From slider: " + loanTenure);
                String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
                Log.consoleLog(ifr, "roiID:" + roiID);
                String loanROI = pcm.mGetRoiVL(ifr, schemeID);
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
                Log.consoleLog(ifr, "after bigdecimal conversion MonthlyDeductionip ::" + MonthlyDeductionip);
                BigDecimal loanamount = new BigDecimal(RecommendLoanAmt);
                Log.consoleLog(ifr, "after bigdecimal conversion loanamount ::" + loanamount);
                BigDecimal ftRoi = new BigDecimal(loanROI);
                Log.consoleLog(ifr, "after bigdecimal conversion ftRoi ::" + ftRoi);
                int loanTenures = Integer.parseInt(loanTenure);
                BigDecimal perposedEmi = bpcc.calculatePMTScorecard(ifr, loanamount, ftRoi, loanTenures);
                Log.consoleLog(ifr, "after bigdecimal conversion perposedEmi ::" + perposedEmi);
                BigDecimal netIncomeip = grossSalaryip.subtract(deductionSalaryip).subtract(MonthlyDeductionip).subtract(perposedEmi);

                Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip ::" + netIncomeip);

                int netIncomeip1 = netIncomeip.intValue();
                Log.consoleLog(ifr, "after bigdecimal conversion netIncomeip1 ::" + netIncomeip1);
                int nethomeins_inprange = netIncomeip1 / grossSalaryip1 * 100;
                int anninc_inp = grossSalaryip1 * 12;
                Log.consoleLog(ifr, "business tenure::" + expYears + ",cibil score::" + bscore + ",emp input::" + category + ",anninc_inp::"
                        + anninc_inp + ",grossSalary::" + grossSalary + ",natureOfSecurity::" + natureOfSecurity + ",nethomeins_inprange::" + nethomeins_inprange
                        + ",existcust_inp::" + existcust_inp + ",excustrange" + Age + ",overduedays_inp::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto
                        + ",recovery::" + recovery + ",residence_inp::" + residence_inp + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff::" + guarantorwriteoff
                        + ",nparestmonths_inpo::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",npa_inp::" + npa_inp + ",writeOffPresent::" + writeOffPresent);
                String scorecardInParams = expYears + "," + bscore + "," + category + "," + anninc_inp + "," + grossSalary
                        + "," + natureOfSecurity + "," + nethomeins_inprange + "," + existcust_inp + "," + Age + "," + overduedays_inp + "," + overduedaysupto
                        + "," + recovery + "," + residence_inp + "," + guarantornpa_inp + "," + guarantorwriteoff
                        + "," + nparestmonths_inpo + "," + settledhist_inp + "," + npa_inp + "," + writeOffPresent;

                String scorecarddecision = checkScoreCard(ifr, "VL_SCORECARD", scorecardInParams, "totalgrade_op", "B");

                if (scorecarddecision.contains(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                    return pcm.returnErrorAPIThroughExecute(ifr);
                } else {
                    Log.consoleLog(ifr, " Returning success ");
                    return "SUCCESS";
                }
            } else {
                JSONObject message = new JSONObject();
                Log.consoleLog(ifr, "Recommended loan amount is empty::");
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "error", "Please enter Recommended Loan Amount in Eligibility to fetch scorecard rating."));
                return message.toString();
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in scorecard for backoffice brms::" + e);
        }
        return scorecardDecision;
    }

    public void popluateDocumentsUploadVL(IFormReference ifr, String value) {
        Log.consoleLog(ifr, "inside the popluateDocumentsUploadVL");
        try {
            pcm.setGetPortalStepName(ifr, value);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
            int rowCount = docResultData.size();
            Log.consoleLog(ifr, "VLPortalCustomCode:popluateDocumentsUploadVL->rowCount::" + rowCount);
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", PID);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            Log.consoleLog(ifr, "Purpose:" + Purpose);
            String appTypeQuery = "Select distinct a.applicablefor from los_m_document a inner join LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='S32'";
            List<String> result1 = ifr.getDataFromDB(appTypeQuery);
            Log.consoleLog(ifr, "result is.." + result1);
            int resultsize1 = result1.size();
            Log.consoleLog(ifr, "size is " + resultsize1);
            //String[] res = result1.toArray(new String[0]);
            //   if (rowCount == 0) {
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

            //  String FkeyB = bpcc.Fkey(ifr, "B");
            // String occupationTypeB = checkSalariedOrNot(ifr, FkeyB);
            //    String FkeyCB = bpcc.Fkey(ifr, "CB");
            //   String occupationTypeCB = checkSalariedOrNot(ifr, FkeyCB);
            for (int i = 0; i < applicantName.length; i++) {
                String applicantNameArrayData = applicantName[i];
                String finalApplicantType = "";
                String Occuaption = "";
                if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                    Log.consoleLog(ifr, "Co Borrower");
                    finalApplicantType = "CB";
                    String FkeyCB = bpcc.Fkey(ifr, "CB");
                    String occupationTypeCB = checkSalariedOrNot(ifr, FkeyCB);
                    Occuaption = occupationTypeCB;
                } else if (applicantNameArrayData.contains("Borrower")) {
                    Log.consoleLog(ifr, "Borrower");
                    finalApplicantType = "B";
                    String FkeyB = bpcc.Fkey(ifr, "B");
                    String occupationTypeB = checkSalariedOrNot(ifr, FkeyB);
                    Occuaption = occupationTypeB;
                }
                String query = "Select a.DocumentName,a.Mandatory,a.DMSNAME, a.DOCUMENTID from los_m_document a inner join LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='" + schemeID + "' and a.isactive='Y' and a.APPLICABLEFOR='" + finalApplicantType + "' and  a.OCCUPATIONTYPE !='" + Occuaption + "'";
                Log.consoleLog(ifr, "query is.." + query);
                List<List<String>> result = ifr.getDataFromDB(query);
                Log.consoleLog(ifr, "result is.." + result);
                int resultsize = result.size();
                Log.consoleLog(ifr, "size is " + resultsize);
                if (resultsize > 0) {
                    JSONArray arr = new JSONArray();
                    for (int j = 0; j < result.size(); j++) {
                        JSONObject re = new JSONObject();
                        re.put("Document Type ", result.get(j).get(0));
                        re.put("Mandatory", result.get(j).get(1));
                        re.put("DMSName", result.get(j).get(2));
                        re.put("DocumentID", result.get(j).get(3));
                        re.put("Applicant Type", applicantName[i]);
                        arr.add(re);
                    }
                    Log.consoleLog(ifr, "Document grid  json array::" + arr);
                    if (rowCount == 0) {
                        Log.consoleLog(ifr, "first Time Document Data" + arr);
                        ifr.addDataToGrid("VL_UPLOAD_DOCUMENT", arr);
                    } else {
                        String checkDocID = ConfProperty.getQueryScript("getDocIDfromPOBDGUPLOAD").replaceAll("#PID#", PID).replaceAll("#APPLICANTTYPE#", applicantNameArrayData);
                        Log.consoleLog(ifr, "checkDocID : " + checkDocID);
                        List<List<String>> docData = ifr.getDataFromDB(checkDocID);
                        Log.consoleLog(ifr, "docData.size() : " + docData.size());
                        for (int j = 0; j < docData.size(); j++) {
                            String oldDocId = docData.get(j).get(0);
                            Log.consoleLog(ifr, "oldDocId : " + oldDocId);
                            boolean docIdFound = false;
                            Iterator<JSONObject> iterator = arr.iterator();
                            while (iterator.hasNext()) {
                                JSONObject obj = iterator.next();
                                if (obj.get("DocumentID").equals(oldDocId)) {
                                    iterator.remove();
                                    docIdFound = true;
                                    Log.consoleLog(ifr, "delete oldDocId from JSONArray: " + oldDocId);
                                    break; // Assuming DOCID is unique, break after removal
                                }
                            }

                            if (!docIdFound) {
                                // delete a row based on DOCID from table
                                String deleteRow = ConfProperty.getQueryScript("DeleteRowByDocIDfromPOBDGUPLOAD")
                                        .replaceAll("#PID#", PID)
                                        .replaceAll("#DOCID#", oldDocId);
                                Log.consoleLog(ifr, "deleteRow : " + deleteRow);
                                ifr.saveDataInDB(deleteRow);
                            }
                        }
                        Log.consoleLog(ifr, "Document grid  json array::" + arr);
                        ifr.addDataToGrid("VL_UPLOAD_DOCUMENT", arr);
                    }
                }
            }
            ifr.setColumnDisable("VL_UPLOAD_DOCUMENT", "1", true);
            ifr.setColumnDisable("VL_UPLOAD_DOCUMENT", "2", true);
            ifr.setColumnDisable("VL_UPLOAD_DOCUMENT", "4", true);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the popluateDocumentsUploadVL" + e);
        }
    }

    //modified by ishwarya on 07-09-2024
    public String validateGrossMonthlyIncomeVL(IFormReference ifr, String control) {
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL");
            String Purpose = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
            String customerProfile = ifr.getValue("P_VL_OD_Profile").toString();
            String customerGrossAMT = "";
            Log.consoleLog(ifr, "customerProfile : " + customerProfile);
            if (customerProfile.equalsIgnoreCase("Salaried") || customerProfile.equalsIgnoreCase("PEN")) {
                customerGrossAMT = ifr.getValue("P_VL_OD_GrossSalary").toString();
            } else {
                customerGrossAMT = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
            }
            Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL: Purpose :" + Purpose + "customerProfile :" + customerProfile + " customerGrossAMT:  " + customerGrossAMT);
            int grossSalaryip1 = Integer.parseInt(customerGrossAMT);
            Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL grossSalaryip1 :" + grossSalaryip1);
            int grossSalaryip2 = grossSalaryip1 * 12;
            if (Purpose.equalsIgnoreCase("N2W") || Purpose.equalsIgnoreCase("U2W")) {
                Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL if Purpose" + Purpose);
                if (customerProfile.equalsIgnoreCase("Salaried")) {
                    if (grossSalaryip2 < 175000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("eflag", "false");//Hard Stop
                        message.put("SHOWMSG", "Salaried customers gross income should be minimum 175000");
                        return message.get("SHOWMSG").toString();
                    } else {
                        Log.consoleLog(ifr, "inside else validateGrossMonthlyIncomeVL <175000 ");
                        return "";
                    }
                } else if (customerProfile.equalsIgnoreCase("PEN")) {
                    if (grossSalaryip2 < 200000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("eflag", "false");//Hard Stop
                        message.put("SHOWMSG", "Salaried customers gross income should be minimum 200000");
                        return message.get("SHOWMSG").toString();
                    } else {
                        Log.consoleLog(ifr, "inside else validateGrossMonthlyIncomeVL <200000 ");
                        return "";
                    }
                } else if ((!customerProfile.equalsIgnoreCase("Salaried") || !customerProfile.equalsIgnoreCase("PEN")) && (grossSalaryip1 < 200000)) {
                    Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL if Purpose" + Purpose);
                    ifr.setValue("P_VL_OD_GrossSalary", "");
                    ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                    message.put("eflag", "false");//Hard Stop
                    message.put("SHOWMSG", "Customers gross income should be minimum 200000");
                    return message.get("SHOWMSG").toString();
                } else {
                    Log.consoleLog(ifr, "inside N2W else validateGrossMonthlyIncomeVL");
                    return "";
                }
            } else if (Purpose.equalsIgnoreCase("N4W") || Purpose.equalsIgnoreCase("U4W")) {
                Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL if Purpose" + Purpose);
                if (customerProfile.equalsIgnoreCase("Salaried") || customerProfile.equalsIgnoreCase("PEN")) {
                    if (grossSalaryip2 < 300000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("eflag", "false");//Hard Stop
                        message.put("SHOWMSG", "Salaried customers gross income should be minimum 300000");
                        return message.get("SHOWMSG").toString();
                    } else {
                        Log.consoleLog(ifr, "inside  else validateGrossMonthlyIncomeVL <300000 ");
                        return "";
                    }
                } else if ((!customerProfile.equalsIgnoreCase("Salaried") || !customerProfile.equalsIgnoreCase("PEN")) && (grossSalaryip1 < 300000)) {
                    Log.consoleLog(ifr, "inside  validateGrossMonthlyIncomeVL if Purpose" + Purpose);
                    ifr.setValue("P_VL_OD_GrossSalary", "");
                    ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                    message.put("eflag", "false");//Hard Stop
                    message.put("SHOWMSG", "Customers gross income should be minimum 300000");
                    return message.get("SHOWMSG").toString();
                } else {
                    Log.consoleLog(ifr, "inside else N4W validateGrossMonthlyIncomeVL");
                    return "";
                }
            } else {
                return "";
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Error occured in mprofileValidationCB" + e);
        }
        return message.get("SHOWMSG").toString();
    }

    public String mCheckmanatPortalFieldsVL(IFormReference ifr) {

        Log.consoleLog(ifr, "INSIDE mCheckmanatPortalFieldsVL for occupation:: ");
        String strfkey = bpcc.Fkey(ifr, "B");
        Log.consoleLog(ifr, "strfkey :: " + strfkey);
        String CBRequired = ifr.getValue("P_VL_OD_Co_Applicant").toString();
        Log.consoleLog(ifr, "CBRequired : " + CBRequired);
        Log.consoleLog(ifr, "CBRequired::" + CBRequired);
        if (CBRequired.equalsIgnoreCase("Yes")) {
            String PortalField = ConfProperty.getCommonPropertyValue("PortalmandatoryOccupationDetailsFieldsVLB");
            String portalfield[] = PortalField.split(",");
            for (String portalfield1 : portalfield) {
                Log.consoleLog(ifr, "mCheckmanatPortalFieldsVL:Value for occupation::" + (portalfield1));
                if (ifr.getValue(portalfield1).toString().isEmpty()) {
                    Log.consoleLog(ifr, "mCheckmanatPortalFieldsVL:setValue for occupation:: " + portalfield1);
                    return portalfield1.replaceAll("P_VL_OD_|_CB|P_OD_VL_|VL_OD_", "");
                }
            }
        } else {
            String PortalField = ConfProperty.getCommonPropertyValue("PortalmandatoryOccupationDetailsFieldsVL");
            String portalfield[] = PortalField.split(",");
            for (String portalfield1 : portalfield) {
                Log.consoleLog(ifr, "mCheckmanatPortalFieldsVL:Value for occupation::" + (portalfield1));
                if (ifr.getValue(portalfield1).toString().isEmpty()) {
                    Log.consoleLog(ifr, "mCheckmanatPortalFieldsVL:setValue for occupation:: " + portalfield1);
                    return portalfield1.replaceAll("P_VL_OD_|_CB|P_OD_VL_|VL_OD_", "");
                }
            }
        }

        return "success";
    }

//modified by ishwarya on 01/08/2024
    public String getAmountForEligibilityCheckVL(IFormReference ifr, HashMap<String, String> loandata) {
        String finaleligibilityRound = "";
        try {
            Log.consoleLog(ifr, "entered into getAmountForEligibilityCheckVL:::::");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInsanceId ===> " + ProcessInsanceId);
            String Purpose = loandata.get("Purpose");
            Log.consoleLog(ifr, "Purpose ===> " + Purpose);
            Purpose = Purpose.equalsIgnoreCase("Purchase of New Two-wheeler") ? "N2W" : "N4W";
            Log.consoleLog(ifr, "Purpose ===> " + Purpose);
            String occupationType = loandata.get("OccupationType");
            Log.consoleLog(ifr, "occupationType ===> " + occupationType);
            String ReqAmount = loandata.get("reqLoanAmt");
            Log.consoleLog(ifr, "ReqAmount ===> " + ReqAmount);
            BigDecimal ftReqAmount = pcm.mCheckBigDecimalValue(ifr, ReqAmount);
            BigDecimal ftTenure = pcm.mCheckBigDecimalValue(ifr, loandata.get("tenure"));
            BigDecimal ftRoi = pcm.mCheckBigDecimalValue(ifr, loandata.get("roi"));
            BigDecimal deductionsalary = pcm.mCheckBigDecimalValue(ifr, loandata.get("deductionmonth"));
            BigDecimal grosssalaryip = pcm.mCheckBigDecimalValue(ifr, loandata.get("grosssalary"));

            int tenure1 = Integer.parseInt(ftTenure.toString());
            Log.consoleLog(ifr, "tenure conversion 123@@ ===> " + tenure1);
            BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, tenure1);
            Log.consoleLog(ifr, "emiperlc calculatePMT ::" + emiperlc);
            Log.consoleLog(ifr, "basicInfo deductionmonth: " + deductionsalary);
            Log.consoleLog(ifr, "basicInfo grosssalary: " + grosssalaryip);
            String nthPercentage = "";
            String nthSaraly = "";
            BigDecimal cbCibilOblig = pcm.mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
            Log.consoleLog(ifr, "CIBIL OBLIGATION " + cbCibilOblig);
            String lac_Amount = ConfProperty.getCommonPropertyValue("LACAMOUNT");//100000
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR LACAMOUNT " + lac_Amount);
            BigDecimal laAmount = new BigDecimal(lac_Amount);
            Log.consoleLog(ifr, "before eligibility validation");
            if (Purpose.equalsIgnoreCase("N2W")) {
                if (occupationType.equalsIgnoreCase("Salaried") || occupationType.equalsIgnoreCase("SELF")
                        || occupationType.equalsIgnoreCase("PROF") || occupationType.equalsIgnoreCase("PEN")) {
                    Log.consoleLog(ifr, "inside purpose two wheeler " + Purpose);
                    nthPercentage = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_TWL");//35
                }
            } else {
                if (occupationType.equalsIgnoreCase("Salaried") || occupationType.equalsIgnoreCase("SELF")
                        || occupationType.equalsIgnoreCase("PROF")) {
                    Log.consoleLog(ifr, "inside purpose four wheeler " + Purpose);
                    nthPercentage = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_FWL");//25
                    nthSaraly = ConfProperty.getCommonPropertyValue("NTHSALARY_VL");//12000
                } else if (occupationType.equalsIgnoreCase("PEN")) {
                    Log.consoleLog(ifr, "inside purpose four wheeler and occupation type pension " + Purpose + occupationType);
                    nthPercentage = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_FWLPEN");//50
                    nthSaraly = ConfProperty.getCommonPropertyValue("NTHSALARY_VL");//12000
                }
            }
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR NTH PERCENTAGE" + nthPercentage);
            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR NTH SALARY" + nthSaraly);
            BigDecimal nthPercentage_final = pcm.mCheckBigDecimalValue(ifr, nthPercentage);
            Log.consoleLog(ifr, "nthPercentage_final" + nthPercentage_final);
            BigDecimal nthSaraly_final = pcm.mCheckBigDecimalValue(ifr, nthSaraly);
            Log.consoleLog(ifr, "nthSaraly" + nthSaraly);
            BigDecimal grossSalary25Perc;
            grossSalary25Perc = grosssalaryip.multiply(nthPercentage_final)
                    .divide(new BigDecimal(100), MathContext.DECIMAL128)
                    .setScale(2, RoundingMode.HALF_UP);
            Log.consoleLog(ifr, "grossSalary25Perc ==> " + grossSalary25Perc);
            BigDecimal netTakeHomeNTH;
            if (Purpose.equalsIgnoreCase("N4W")) {
                Log.consoleLog(ifr, "inside purpose four wheeler " + Purpose);
                int comparisonResult = grossSalary25Perc.compareTo(nthSaraly_final);
                if (comparisonResult > 0) {
                    netTakeHomeNTH = grossSalary25Perc;
//                    netTakeHomeNTH = nthPercentage_final.divide(new BigDecimal(100), MathContext.DECIMAL128)
//                            .setScale(2, RoundingMode.DOWN)
//                            .multiply(grossSalary25Perc);
                } else {
                    netTakeHomeNTH = nthSaraly_final;
//                    netTakeHomeNTH = nthPercentage_final.divide(new BigDecimal(100), MathContext.DECIMAL128)
//                            .setScale(2, RoundingMode.DOWN)
//                            .multiply(nthSaraly_final);
                }
                Log.consoleLog(ifr, "netTakeHomeNTH===> " + netTakeHomeNTH);
            } else {
                Log.consoleLog(ifr, "inside purpose two wheeler " + Purpose);
                netTakeHomeNTH = nthPercentage_final.divide(new BigDecimal(100), MathContext.DECIMAL128)
                        .setScale(2, RoundingMode.DOWN)
                        .multiply(grosssalaryip);
                Log.consoleLog(ifr, "netTakeHomeNTH===> " + netTakeHomeNTH);
            }
            BigDecimal prodspeccapping = pcm.mCheckBigDecimalValue(ifr, loandata.get("loancap"));
            Log.consoleLog(ifr, "prodspeccapping===> " + prodspeccapping);

            BigDecimal netAvailIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig).subtract(netTakeHomeNTH);
            Log.consoleLog(ifr, "netAvailIncome===> " + netAvailIncome);

            BigDecimal loanAmountperpolicy = ((netAvailIncome).divide(emiperlc, 2, RoundingMode.DOWN)).multiply(laAmount);
            Log.consoleLog(ifr, "loanAmountperpolicy ===> " + loanAmountperpolicy);
            BigDecimal onRoadPrice = pcm.mCheckBigDecimalValue(ifr, loandata.get("onRoadPrice"));
            BigDecimal ftLTV = pcm.mCheckBigDecimalValue(ifr, loandata.get("finalLTVAmount"));
            Log.consoleLog(ifr, "eligible LTV ===> " + ftLTV);
            BigDecimal eligLTV = ftReqAmount.divide(onRoadPrice, 9, BigDecimal.ROUND_UP).multiply(new BigDecimal(100));
            Log.consoleLog(ifr, "calculated LTV ===> " + eligLTV);

            BigDecimal finalLTV;
            BigDecimal finalLTVMargin;
            if (eligLTV.compareTo(ftLTV) <= 0) {
                finalLTVMargin = eligLTV;
            } else {
                finalLTVMargin = ftLTV;
            }
            Log.consoleLog(ifr, "final LTV taken ===> " + finalLTVMargin);
            // Convert finalLTV from percentage to decimal

            finalLTV = finalLTVMargin.multiply(onRoadPrice).divide(new BigDecimal(100));
            Log.consoleLog(ifr, "final LTV taken bt mul onRoadPrice ===> " + finalLTV);

            String query = "SELECT MARGIN,FETCHMARGIN FROM LOS_NL_COLLATERAL_VEHICLES where PID='" + ProcessInsanceId + "'";
            List<List<String>> marginValue = cf.mExecuteQuery(ifr, query, "Execute query for fetching  MARGIN,FETCHMARGIN ");
            //String MARGIN = marginValue.get(0).get(0);
            //String FETCHMARGIN = marginValue.get(0).get(1);
            if (marginValue.isEmpty()) {
                Log.consoleLog(ifr, "inside if===> " + query);
                String insertLTV = ConfProperty.getQueryScript("insertLTV").
                        replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#finalLTVMargin#", finalLTVMargin.toString()).
                        replaceAll("#finalLTV#", finalLTV.toString());
                Log.consoleLog(ifr, "insertLTV ===> " + insertLTV);
                ifr.saveDataInDB(insertLTV);

            } else {
                Log.consoleLog(ifr, "inside else===> " + query);
                String updateLTV = ConfProperty.getQueryScript("updateLTV")
                        .replaceAll("#Pid#", ProcessInsanceId).replaceAll("#finalLTVMargin#", finalLTVMargin.toString()).
                        replaceAll("#finalLTV#", finalLTV.toString());
                Log.consoleLog(ifr, "updateLTV ===> " + updateLTV);
                ifr.saveDataInDB(updateLTV);
            }
            BigDecimal finaleligibility = ftReqAmount.min(loanAmountperpolicy).min(finalLTV);

            Log.consoleLog(ifr, "finaleligibility===> " + finaleligibility);
            Log.consoleLog(ifr, "Before EligibilityDataObj : ");
            String grosssalaryipRound = String.valueOf(Math.round(Double.parseDouble(grosssalaryip.toString())));
            Log.consoleLog(ifr, "Before grosssalaryip : " + grosssalaryip);
            String deductionsalaryRound = String.valueOf(Math.round(Double.parseDouble(deductionsalary.toString())));
            Log.consoleLog(ifr, "Before deductionsalary : " + deductionsalary);
            String cbCibilObligRound = String.valueOf(Math.round(Double.parseDouble(cbCibilOblig.toString())));
            Log.consoleLog(ifr, "Before cbCibilOblig : " + cbCibilOblig);
            String netTakeHomeNTHRound = String.valueOf(Math.round(Double.parseDouble(netTakeHomeNTH.toString())));
            Log.consoleLog(ifr, "Before netTakeHomeNTH : " + netTakeHomeNTHRound);
            String netIncomeRound = String.valueOf(Math.round(Double.parseDouble(netAvailIncome.toString())));
            Log.consoleLog(ifr, "Before netIncome : " + netIncomeRound);
            // String ftTenureRound = String.valueOf(Math.round(Double.parseDouble(ftTenure.toString())));
            String ftTenureRound = ftTenure.toString();
            Log.consoleLog(ifr, "Before ftTenure : " + ftTenureRound);
            //String ftRoiRound = String.valueOf(Math.round(Double.parseDouble(ftRoi.toString())));
            String ftRoiRound = ftRoi.toString();
            Log.consoleLog(ifr, "Before ftRoi : " + ftRoiRound);
            String loanAmountRound = String.valueOf(Math.round(Double.parseDouble(loanAmountperpolicy.toString())));
            Log.consoleLog(ifr, "Before loanAmount : " + loanAmountRound);

            String prodspeccappingRound = String.valueOf(Math.round(Double.parseDouble(prodspeccapping.toString())));
            Log.consoleLog(ifr, "Before prodspeccapping : " + prodspeccappingRound);

            BigInteger integerPart = finaleligibility.toBigInteger();
            // Convert BigInteger to int
            int finaleligibilityInt = integerPart.intValue();
            Log.consoleLog(ifr, "Number without decimal: " + finaleligibilityInt);
            double roundedValue = Math.floor(finaleligibilityInt / 1000) * 1000;
            finaleligibilityRound = String.valueOf(roundedValue);
            Log.consoleLog(ifr, "After rounded finaleligibility Value : " + finaleligibilityRound);

            String finalLTVRound = String.valueOf(Math.floor(Double.parseDouble(finalLTV.toString())));
            Log.consoleLog(ifr, "After rounded finaleligibility Value : " + finaleligibilityRound);

            if (finaleligibilityRound.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
            String finalEligibleAmount = "";
            String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
            String finalAmountInParams = productCode + "," + finaleligibilityRound;
            finalEligibleAmount = checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");

            if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {
                Log.consoleLog(ifr, "eligibility Passed Successfully:::");
            } else {
                Log.consoleLog(ifr, " eligibility fail" + finalEligibleAmount);
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "F_InPrincipleEligibility", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));
                message.put("eflag", "false");
                return message.toString();
            }

            String loanAmount = "-" + finaleligibilityRound;
            BigDecimal rate = new BigDecimal(ftRoiRound);
            int tenure = Integer.parseInt(ftTenureRound);
            BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
            String emi = emicalc.toString();
            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                Log.consoleLog(ifr, "ActivityName ::" + ifr.getActivityName());
                JSONArray gridResultSetArr = new JSONArray();
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("QNL_LA_INPRINCIPLE_AverageGrossIncome", grosssalaryipRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_AverageDeductions", deductionsalaryRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_Obligations", cbCibilObligRound);//
                formDetailsJson.put("QNL_LA_INPRINCIPLE_NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_NetIncome", netIncomeRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_Tenure", ftTenureRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_ROI", ftRoiRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_Loanamountasperpolicy", loanAmountRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanAmountasperProductPolicy", prodspeccappingRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_RequestedLoanAmount", ReqAmount);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_InPrincipalloanamount", finaleligibilityRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_InstalmentAmount", emi);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanAmountAsPerLtv", finalLTVRound);
                gridResultSetArr.add(formDetailsJson);
                ifr.clearTable("ALV_INPRINCIPLE_ELIGIBILITY");
                ifr.addDataToGrid("ALV_INPRINCIPLE_ELIGIBILITY", gridResultSetArr);
                Log.consoleLog(ifr, "ALV_INPRINCIPLE_ELIGIBILITY gridResultSet Populated : " + gridResultSetArr.toString());

            } else if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                Log.consoleLog(ifr, "ActivityName ::" + ifr.getActivityName());
                JSONArray gridResultSetArr = new JSONArray();
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("QNL_LA_INPRINCIPLE-AverageGrossIncome", grosssalaryipRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-AverageDeductions", deductionsalaryRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-Obligations", cbCibilObligRound);//
                formDetailsJson.put("QNL_LA_INPRINCIPLE-NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-NetIncome", netIncomeRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-Tenure", ftTenureRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-ROI", ftRoiRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-Loanamountasperpolicy", loanAmountRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanAmountasperProductPolicy", prodspeccappingRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-RequestedLoanAmount", ReqAmount);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-InPrincipalloanamount", finaleligibilityRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-InstalmentAmount", emi);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanAmountAsPerLtv", finalLTVRound);
                gridResultSetArr.add(formDetailsJson);
                ((IFormAPIHandler) ifr).clearTable("QNL_LA_INPRINCIPLE");
                ((IFormAPIHandler) ifr).addDataToGrid("QNL_LA_INPRINCIPLE", gridResultSetArr, true);
                Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE Populated Portal to Backoffice ==>" + gridResultSetArr.toString());
            } else if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "ActivityName ::" + ifr.getActivityName());

                JSONArray gridResultSetArr = new JSONArray();
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryipRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalaryRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_Obligations", cbCibilObligRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_NetIncome", netIncomeRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_Tenure", ftTenureRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_ROI", ftRoiRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netTakeHomeNTHRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", prodspeccappingRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_LoanAmountAsLtv", finalLTVRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finaleligibilityRound);
                formDetailsJson.put("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", ReqAmount);
                gridResultSetArr.add(formDetailsJson);
                String loanROI = "";
                String obligation = "";
                String deduction = "";
                String roiData_Query = "select ROI,OBLIGATIONS,AVERAGEDEDUCTIONS from LOS_NL_LA_FINALELIGIBILITY where PID='" + ProcessInsanceId + "'";
                List<List<String>> list1 = cf.mExecuteQuery(ifr, roiData_Query, "tenureData_Query FROM PORTAL:");
                if (list1.size() > 0) {
                    loanROI = list1.get(0).get(0);
                    obligation = list1.get(0).get(1);
                    deduction = list1.get(0).get(2);

                }
                Log.consoleLog(ifr, "loanROI : " + loanROI);
                Log.consoleLog(ifr, "obligation : " + obligation);
                Log.consoleLog(ifr, "deduction : " + deduction);
                if (!ftRoiRound.equalsIgnoreCase(loanROI) || !cbCibilObligRound.equalsIgnoreCase(obligation)
                        || !deductionsalaryRound.equalsIgnoreCase(deduction)) {
                    ifr.clearTable("ALV_FINAL_ELIGIBILITY");
                    ifr.addDataToGrid("ALV_FINAL_ELIGIBILITY", gridResultSetArr);
                    Log.consoleLog(ifr, "ALV_FINAL_ELIGIBILITY gridResultSet Populated : " + gridResultSetArr.toString());
                } else {
                    Log.consoleLog(ifr, "loanROI==>" + loanROI);
                }

            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        }
        return finaleligibilityRound;
    }

    //modified by Sharon on 06-08-2024
    public String coObligantCBSCheckVL(IFormReference ifr, String control, String event, String value) { // added by ishwarya for co-obligant check on 18/06/2024
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "entered into coObligantCBSCheckVL:::::");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String COMobNo = ifr.getValue("P_VL_OD_MOBILE_NUMBER").toString();
            String COCustID = ifr.getValue("P_VL_OD_CUSTOMER_ID").toString();
            String Ext = ifr.getValue("P_VL_OD_EXISTING_CUSTOMER").toString();
            String RelationshipCB = ifr.getValue("P_VL_OD_RELATIONSHIP_BORROWER").toString();
            Log.consoleLog(ifr, "Existing Customer Radio button ===> " + Ext);
            Log.consoleLog(ifr, "MOBILE NUMBER ===> " + COMobNo);
            Log.consoleLog(ifr, "CUSTOMER ID ===> " + COCustID);
            Log.consoleLog(ifr, "RelationshipCB===> " + RelationshipCB);
            if (Ext.equalsIgnoreCase("YES")) {
                Log.consoleLog(ifr, "Inside Existing Customer Radio button ===> " + Ext);
                if (!RelationshipCB.isEmpty()) {
                    Log.consoleLog(ifr, "RelationshipCB if===> " + RelationshipCB);

                    if (!COMobNo.isEmpty() && !COCustID.isEmpty()) {
                        Log.consoleLog(ifr, "Inside MOBILE NUMBER & CUSTOMER_ID Validation ");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("MobileNumber", COMobNo);
                        map.put("CustomerId", COCustID);
                        Log.consoleLog(ifr, "BudgetPortalCustomCode:CoObligantCBSCheck -MobileNumberVL:::" + COMobNo);
                        Log.consoleLog(ifr, "BudgetPortalCustomCode:CoObligantCBSCheck -CustomerIdVL:::" + COCustID);
                        String responseCO = cas.getCustomerAccountSummary(ifr, map);
                        //modified by ishwarya on 25-07-2024
                        String ErrorMessage = "";
                        if (responseCO.contains(RLOS_Constants.ERROR)) {
                            ErrorMessage = responseCO.replaceAll(RLOS_Constants.ERROR, "");
                            message.put("MSGSTS", "N");
                            message.put("SHOWMSG", ErrorMessage + ". Kindly enter valid data");
                            return message.toString();
                        }
                        JSONParser jparser = new JSONParser();
                        JSONObject object = (JSONObject) jparser.parse(responseCO);
                        String ext_cust = object.get("CustomerFlag").toString();
                        String responseCOMobNumber = object.get("mobile_Number").toString();
                        String responseCOCustId = object.get("CustomerID").toString();
                        Log.consoleLog(ifr, " CustomerFlag VL=>" + ext_cust);
                        Log.consoleLog(ifr, "mobile_Number VL=>" + responseCOMobNumber);
                        Log.consoleLog(ifr, "CustomerID VL=>" + responseCOCustId);
                        String fName = object.get("CustomerFirstName").toString();
                        String mName = object.get("CustomerMiddleName").toString();
                        String lName = object.get("CustomerLastName").toString();
                        String fullName = "";
                        if ((mName == null) || (mName.equalsIgnoreCase("")) || (mName.equalsIgnoreCase("null"))) {
                            fullName = fName + " " + lName;
                        } else {
                            fullName = fName + " " + mName + " " + lName;
                        }
                        Log.consoleLog(ifr, "fullName by api::" + fullName);
                        if ((!ext_cust.equalsIgnoreCase("Y")) || ext_cust.isEmpty()) {
                            Log.consoleLog(ifr, "Co-obligant validation failed CBS VL");
                            ifr.setValue("P_CB_OD_MOBILE_NUMBER", "");
                            ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
                            message.put("MSGSTS", "N");
                            message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance");
                            return message.toString();
                        } else {
                            ifr.setValue("CoApplicantName_VL", fullName);
                            Log.consoleLog(ifr, "Into Co-obligant validation -- Before DataSaving ");
                            String ApplicantType = "CB";
                            Log.consoleLog(ifr, "ApplicantType :: " + ApplicantType);
                            new PortalCustomCode().saveDataInPartyDetailGrid(ifr, ApplicantType, COMobNo + "~" + COCustID);
                            Log.consoleLog(ifr, "Co-obligant validation is Successfull VL-- After DataSaving");
                            String knockoffDecision = mImpCoObligantCheck(ifr);
                            if (knockoffDecision.contains("showMessage")) {
                                message.put("MSGSTS", "N");
                                message.put("SHOWMSG", "Technical glitch, Try after sometime!");
                                return message.toString();
                            } else if (knockoffDecision.equalsIgnoreCase("Reject")) {
                                Log.consoleLog(ifr, "co-borrower knock-off failed :::");
                                message.put("MSGSTS", "N");
                                message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance");
                                return message.toString();
                            } else {
                                Log.consoleLog(ifr, "co-borrower knock-off passed succesfully :::");
                                Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + COCustID);
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
                                    ifr.setStyle("CB_OCCUPATION_SECTION", "visible", "true");
                                    ifr.setStyle("P_OD_ValidateCoObligantVL", "disable", "true");
                                    ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "disable", "true");
                                    ifr.setStyle("P_VL_OD_CUSTOMER_ID", "disable", "true");
                                    ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "disable", "true");
                                    String NatureOfsecurity = "Hypothecation of durable utility article or vehicle to the extent of stipulation under the scheme";
                                    ifr.addItemInCombo("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                                    ifr.setValue("VL_OD_NatureofSecurity_CB", NatureOfsecurity);
                                    Log.consoleLog(ifr, "P_OD_ValidateCoObligantCB ====> Validate Button is disabled <==== ");
                                    JSONParser jsonparser = new JSONParser();
                                    JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                                    Log.consoleLog(ifr, obj.toString());
                                    String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
                                    Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
                                    if (!DateOfCustOpen.isEmpty()) {
                                        LocalDate curDate = LocalDate.now();
                                        Log.consoleLog(ifr, "curDate  :" + curDate);
                                        LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
                                        Log.consoleLog(ifr, "PastDate  :" + PastDate);
                                        long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
                                        Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
                                        int YearsWithCanara = (int) (monthsBetween / 12);
                                        Log.consoleLog(ifr, "YearsWithCanara: " + YearsWithCanara);
                                        ifr.setValue("P_VL_OD_RelationshipWithCanara_CB", String.valueOf(YearsWithCanara));
                                        ifr.setStyle("P_VL_OD_RelationshipWithCanara_CB", "disable", "true");

                                    }
                                }
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
                    Log.consoleLog(ifr, "Kindly select Relationship with borrower to Validate Co-Obligant ");
                    message.put("MSGSTS", "N");
                    message.put("SHOWMSG", "Kindly select Relationship with borrower to Validate Co-Obligant.");
                    return message.toString();
                }
            } else {
                Log.consoleLog(ifr, "Kindly select existing customer 'Yes' to validate the Co-Obligant ");
                message.put("MSGSTS", "N");
                message.put("SHOWMSG", "Kindly select existing customer 'Yes' to validate the Co-Obligant");
                return message.toString();
            }

        } catch (ParseException ex) {
            Log.errorLog(ifr, "Error occured in CheckCibilandExperian VL" + ex);
            Log.consoleLog(ifr, "Error occured in CheckCibilandExperian VL" + ex);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
    }

    //modified by ishwarya on 07-09-2024 
    public String OnClickCoApplicant(IFormReference ifr, String control) {
        Log.consoleLog(ifr, "inside on click co-applicant::");
        String CBRequired = ifr.getValue("P_VL_OD_Co_Applicant").toString();
        Log.consoleLog(ifr, "CBRequired::" + CBRequired);
        if (CBRequired.equalsIgnoreCase("Yes")) {
            ifr.setStyle("P_VL_OD_EXISTING_CUSTOMER", "visible", "true");
            OnClickExistingCust(ifr);
        } else {
            ifr.setStyle("CB_OCCUPATION_SECTION", "visible", "false");
            ifr.setStyle("P_VL_OD_EXISTING_CUSTOMER", "visible", "false");
            ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "visible", "false");
            ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "visible", "false");
            ifr.setValue("P_VL_OD_EXISTING_CUSTOMER", " ");
            ifr.setStyle("P_VL_OD_CUSTOMER_ID", "visible", "false");
            ifr.setStyle("P_OD_ValidateCoObligantVL", "visible", "false");
            ifr.setStyle("CoApplicantName_VL", "visible", "false");
            ifr.setStyle("P_OD_ResetCoObligantVL", "visible", "false");
        }
        return "";
    }

    public String OnClickExistingCust(IFormReference ifr) {
        JSONObject message = new JSONObject();
        Log.consoleLog(ifr, "inside on click ExistingCusts::");
        String custType = ifr.getValue("P_VL_OD_EXISTING_CUSTOMER").toString();
        Log.consoleLog(ifr, "custType1::" + custType);
        if (custType.equalsIgnoreCase("Yes")) {
            Log.consoleLog(ifr, "custType::" + custType);
            ifr.setStyle("P_VL_OD_EXISTING_CUSTOMER", "mandatory", "true");
            ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "visible", "true");
            ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "visible", "true");
            ifr.setStyle("P_VL_OD_CUSTOMER_ID", "visible", "true");
            ifr.setStyle("CoApplicantName_VL", "visible", "true");
            ifr.setStyle("P_OD_ValidateCoObligantVL", "visible", "true");
            ifr.setStyle("P_OD_ResetCoObligantVL", "visible", "true");
            ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "mandatory", "true");
            ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "mandatory", "true");
            ifr.setStyle("P_VL_OD_CUSTOMER_ID", "mandatory", "true");
            ifr.setStyle("P_VL_OD_OverAllExperience_CB", "mandatory", "true");
        } else if (custType.equalsIgnoreCase("No")) {
            Log.consoleLog(ifr, "inside else::" + custType);
            ifr.setStyle("CB_OCCUPATION_SECTION", "visible", "false");
            ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "visible", "false");
            ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "visible", "false");
            ifr.setStyle("P_VL_OD_CUSTOMER_ID", "visible", "false");
            ifr.setStyle("P_OD_ValidateCoObligantVL", "visible", "false");
            ifr.setStyle("P_OD_ResetCoObligantVL", "visible", "false");
            ifr.setStyle("CoApplicantName_VL", "visible", "false");
            message.put("eflag", "false");//Hard Stop
            message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey. Kindly contact branch for further assistance");
            ifr.setStyle("navigationNextBtn", "disable", "true");
            return message.toString();
        }
        return "";
    }

    public String onchageDownpayment(IFormReference ifr) { //modified by Sharon for Down payment validation on 30/07/2024
        Log.consoleLog(ifr, "inside onchageDownpayment::");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId::" + ProcessInstanceId);
        JSONObject message = new JSONObject();
        String OccuType = ifr.getValue("P_VL_OD_Profile").toString();
        String costOfVehicle = ifr.getValue("P_VL_OD_COST").toString();
        String downPayment = ifr.getValue("P_VL_OD_DOWNPAYMENT").toString();
        String purposeOfLoan = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
        String Category = ifr.getValue("P_VL_OD_Category").toString();

        Log.consoleLog(ifr, "OccuType::" + OccuType);
        Log.consoleLog(ifr, "costOfVehicle::" + costOfVehicle);
        Log.consoleLog(ifr, "downPayment::" + downPayment);
        Log.consoleLog(ifr, "purposeOfLoan::" + purposeOfLoan);
        Log.consoleLog(ifr, "Category::" + Category);

        double downpaymentAmount = Double.parseDouble(downPayment);
        double vehicleCost = Double.parseDouble(costOfVehicle);

        Log.consoleLog(ifr, "downpaymentAmount::" + downpaymentAmount);
        Log.consoleLog(ifr, "vehicleCost::" + vehicleCost);
        double requiredDownpayment = 0;
        try {
            if (downpaymentAmount > vehicleCost) {
                Log.consoleLog(ifr, "downpaymentAmount:" + downpaymentAmount + ", vehicleCost:" + vehicleCost);
                Log.consoleLog(ifr, "Validation for downpaymentAmount greater than vehicleCost");
                message.put("showMessage", cf.showMessage(ifr, "P_VL_OD_DOWNPAYMENT", "error", "Down payment should be lesser than the cost of vehicle."));
                ifr.setValue("P_VL_OD_DOWNPAYMENT", "");
                message.put("eflag", "false");
                return message.toString();
            } else {
                if (purposeOfLoan.equals("N2W")) {
                    Log.consoleLog(ifr, "inside if::" + purposeOfLoan);
                    if (OccuType.equals("Salaried")) {
                        Log.consoleLog(ifr, "inside if OccuType Salaried");
                        requiredDownpayment = vehicleCost * 0.15;
                        Log.consoleLog(ifr, "requiredDownpayment::" + requiredDownpayment);
                        if (downpaymentAmount < requiredDownpayment || downpaymentAmount == vehicleCost) {
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                            Log.consoleLog(ifr, "Downpayment validation");
                            message.put("showMessage", cf.showMessage(ifr, "P_VL_OD_DOWNPAYMENT", "error", "Downpayment should be " + (requiredDownpayment / vehicleCost * 100) + "% or less than cost of vehicle."));
                            ifr.setValue("P_VL_OD_DOWNPAYMENT", "");
                            message.put("eflag", "false");
                            return message.toString();
                        }
                    } else {
                        Log.consoleLog(ifr, "inside else of OccuType Salaried");
                        requiredDownpayment = vehicleCost * 0.25;
                        Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        if (downpaymentAmount < requiredDownpayment || downpaymentAmount == vehicleCost) {
                            Log.consoleLog(ifr, "Downpayment validation");
                            message.put("showMessage", cf.showMessage(ifr, "P_VL_OD_DOWNPAYMENT", "error", "Downpayment should be " + (requiredDownpayment / vehicleCost * 100) + "% or less than cost of vehicle."));
                            ifr.setValue("P_VL_OD_DOWNPAYMENT", "");
                            message.put("eflag", "false");
                            return message.toString();
                        }
                    }
                } else if (purposeOfLoan.equals("N4W")) {
                    Log.consoleLog(ifr, "inside else::" + purposeOfLoan);

                    if (OccuType.equals("Salaried") && Category.equals("Central Government")
                            || Category.equals("State Government")
                            || Category.equals("PSU")
                            || Category.equals("Autonomous bodies of Central")) {
                        Log.consoleLog(ifr, "Category if::" + Category);
                        if (vehicleCost <= 1000000) {
                            requiredDownpayment = vehicleCost * 0.10;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        } else if (vehicleCost > 100000 && vehicleCost <= 2500000) {
                            requiredDownpayment = vehicleCost * 0.10;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        } else {
                            requiredDownpayment = vehicleCost * 0.20;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        }
                    } else if (OccuType.equals("Salaried") || OccuType.equals("PEN")
                            || OccuType.equals("SELF") || OccuType.equals("PROF")
                            && !Category.equals("Central Government")
                            || !Category.equals("State Government")
                            || !Category.equals("PSU")
                            || !Category.equals("Autonomous bodies of Central")) {
                        Log.consoleLog(ifr, "Category else::" + Category);
                        if (vehicleCost <= 1000000) {
                            requiredDownpayment = vehicleCost * 0.10;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        } else if (vehicleCost > 1000000 && vehicleCost <= 2500000) {
                            requiredDownpayment = vehicleCost * 0.15;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        } else {
                            requiredDownpayment = vehicleCost * 0.20;
                            Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        }
                    }

                    if (downpaymentAmount < requiredDownpayment || downpaymentAmount == vehicleCost) {
                        Log.consoleLog(ifr, "requiredDownpayment" + requiredDownpayment);
                        Log.consoleLog(ifr, "Downpayment validation for 4-wheeler");
                        message.put("eflag", "false");
                        //message.put("SHOWMSG", cf.showMessage(ifr, "P_VL_OD_DOWNPAYMENT", "error", "Downpayment should be " + (requiredDownpayment / vehicleCost * 100) + "% or less than cost of vehicle."));
                        message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_VL_OD_DOWNPAYMENT", "error", "Downpayment should be " + (requiredDownpayment / vehicleCost * 100) + "% or less than cost of vehicle."));
                        ifr.setValue("P_VL_OD_DOWNPAYMENT", "");
                        return message.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception::" + e);
        }
        return "";
    }

    //added by ishwarya for DM on 22/6/2024
    public String Accountdetail(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String FKEY = bpcc.Fkey(ifr, "B");
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

    //added by ishwarya for DM on 22/6/2024
    public void addacctdetailsVL(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside addacctdetailsVL");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "Portal VL customcode PID:: addacctdetailsVL:::" + PID);
        // Get the value of the P_VL_OD_DISBURSAL_ACCOUNT and split it
        String[] accountDetails = ifr.getValue("P_VL_OD_DISBURSAL_ACCOUNT").toString().split("-");
        // Check and log each part of the split string
        String AccountNo = accountDetails.length > 0 ? accountDetails[0] : "";
        Log.consoleLog(ifr, "Portal VL customcode :: addacctdetailsVL:::AccountNo " + AccountNo);
        String Strbranch = accountDetails.length > 1 ? accountDetails[1] : "";
        Log.consoleLog(ifr, "Portal VL customcode :: addacctdetailsVL:::Strbranch " + Strbranch);
        String StrDataopen = accountDetails.length > 2 ? accountDetails[2] : "";
        Log.consoleLog(ifr, "Portal VL customcode :: addacctdetailsVL:::StrDataopen " + StrDataopen);
        String StrAccountbala = accountDetails.length > 3 ? accountDetails[3] : "";
        Log.consoleLog(ifr, "Portal VL customcode :: addacctdetailsVL:::StrAccountbala " + StrAccountbala);
        String stractprdcode = accountDetails.length > 4 ? accountDetails[4] : "null";
        stractprdcode = stractprdcode.equalsIgnoreCase(" ") ? "null" : stractprdcode;
        Log.consoleLog(ifr, "Portal VL customcode :: addacctdetailsVL:::stractprdcode " + stractprdcode);
        String strfkey = bpcc.Fkey(ifr, "B");
        String Acctid = Accountdetail(ifr);
        if (Acctid.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "inside Acctid is empty:::Portal VL customcode :: addacctdetailsVL:::" + AccountNo);
            String queryin = ConfProperty.getQueryScript("InsertAccdetailsQueryVL").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#StrDataopen#", StrDataopen).replaceAll("#StrAccountbala#", StrAccountbala).replaceAll("#stractprdcode#", stractprdcode);
            Log.consoleLog(ifr, "Portal VL customcode :: addacctdetails:::" + queryin);
            ifr.getDataFromDB(queryin);
        } else {
            String queryin = ConfProperty.getQueryScript("UPDATEACCDETQUERY").replaceAll("#PID#", PID).replaceAll("#Strbranch#", Strbranch).replaceAll("#AccountNo#", AccountNo).replaceAll("#FKEY#", strfkey).replaceAll("#StrDataopen#", StrDataopen).replaceAll("#StrAccountbala#", StrAccountbala).replaceAll("#stractprdcode#", stractprdcode);
            Log.consoleLog(ifr, "Portal VL customcode :: addacctdetails:::" + queryin);
            ifr.getDataFromDB(queryin);
        }
    }

    public String checkSalariedOrNot(IFormReference ifr, String Fkey) {
        Log.consoleLog(ifr, "inside checkSalariedOrNot");
        String occupationType = "";
        String getOccupationType = ConfProperty.getQueryScript("GETOCCUPATIONTYPE").replaceAll("#Fkey#", Fkey);
        List<List<String>> list1 = cf.mExecuteQuery(ifr, getOccupationType, "getOccupationType:");
        if (list1.size() > 0) {
            occupationType = list1.get(0).get(0);
            Log.consoleLog(ifr, "occupationType : " + occupationType);
        }
        if (occupationType.equalsIgnoreCase("Salaried")) {
            occupationType = "Non-Salaried";
        } else {
            occupationType = "Salaried";
        }

        return occupationType;
    }

    //modified by ishwarya on 07-09-2024
    public String mprofileValidationCB(IFormReference ifr, String control) {
        JSONObject message = new JSONObject();
        String errorMsg = "";
        try {
            Log.consoleLog(ifr, "inside  mprofileValidationCB");
            String Purpose = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
            String customerProfile = ifr.getValue("P_VL_OD_Profile").toString();
            String customerProfileCB = ifr.getValue("P_VL_OD_Profile_CB").toString();
            Log.consoleLog(ifr, " VLPortal::mprofileValidationCB::customerProfileCB :" + customerProfileCB);
            String customerGrossAMT = "";
            if (customerProfile.equalsIgnoreCase("Salaried") || customerProfile.equalsIgnoreCase("PEN")) {
                customerGrossAMT = ifr.getValue("P_VL_OD_GrossSalary").toString();
            } else {
                customerGrossAMT = ifr.getValue("P_VL_OD_GrossAnnualSalary").toString();
            }
            String customerGrossAMTCB = "";
            if (customerProfileCB.equalsIgnoreCase("Salaried") || customerProfileCB.equalsIgnoreCase("PEN")) {
                customerGrossAMTCB = ifr.getValue("P_VL_OD_GrossSalary_CB").toString();
            } else {
                customerGrossAMTCB = ifr.getValue("P_VL_OD_GrossAnnualSalary_CB").toString();
            }

            Log.consoleLog(ifr, "inside  mprofileValidationCB: Purpose :" + Purpose + "customerProfile :" + customerProfile + " customerGrossAMT:  " + customerGrossAMT + "customerGrossAMTCB :  " + customerGrossAMTCB);
            int grossSalaryip1 = Integer.parseInt(customerGrossAMT);
            int grossSalaryip1CB = Integer.parseInt(customerGrossAMTCB);
            int combinedgrossSalaryip1 = grossSalaryip1 + grossSalaryip1CB;
            Log.consoleLog(ifr, "inside  mprofileValidationCB grossSalaryip1 :" + grossSalaryip1 + ", grossSalaryip1CB :" + grossSalaryip1CB);
            Log.consoleLog(ifr, "combinedgrossSalaryip1  :" + combinedgrossSalaryip1);
            int grossSalaryip2 = combinedgrossSalaryip1 * 12;
            Log.consoleLog(ifr, " grossSalaryip2 :" + grossSalaryip2);
            if (Purpose.equalsIgnoreCase("N2W") || Purpose.equalsIgnoreCase("U2W")) {
                Log.consoleLog(ifr, "inside  mprofileValidationCB if Purpose: " + Purpose);
                if (customerProfile.equalsIgnoreCase("Salaried")) {
                    if (grossSalaryip2 < 175000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("showMessage", cf.showMessage(ifr, control, "error", "salaried customers Gross Income should be minimum 175000"));
                        errorMsg = "salaried customers Gross Income should be minimum 175000";
                        return errorMsg;
                    }
                } else if (customerProfile.equalsIgnoreCase("PEN")) {
                    if (grossSalaryip2 < 200000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("showMessage", cf.showMessage(ifr, control, "error", "salaried customers Gross Income should be minimum 200000"));
                        errorMsg = "salaried customers Gross Income should be minimum 200000";
                        return errorMsg;
                    }
                } else if ((!customerProfile.equalsIgnoreCase("Salaried") || !customerProfile.equalsIgnoreCase("PEN")) && (combinedgrossSalaryip1 < 200000)) {
                    Log.consoleLog(ifr, "inside  mprofileValidationCB if Purpose" + Purpose);
                    ifr.setValue("P_VL_OD_GrossSalary", "");
                    ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                    message.put("showMessage", cf.showMessage(ifr, control, "error", "customers Gross Income should be minimum 200000"));
                    errorMsg = "customers Gross Income should be minimum 200000";
                    return errorMsg;
                }
            } else if (Purpose.equalsIgnoreCase("N4W") || Purpose.equalsIgnoreCase("U4W")) {
                Log.consoleLog(ifr, "inside  mprofileValidationCB if Purpose" + Purpose);
                if (customerProfile.equalsIgnoreCase("Salaried") || customerProfile.equalsIgnoreCase("PEN")) {
                    if (grossSalaryip2 < 300000) {
                        ifr.setValue("P_VL_OD_GrossSalary", "");
                        ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                        message.put("showMessage", cf.showMessage(ifr, control, "error", "salaried customers Gross Income should be minimum 300000"));
                        errorMsg = "salaried customers Gross Income should be minimum 300000";
                        return errorMsg;
                    }
                } else if ((!customerProfile.equalsIgnoreCase("Salaried") || !customerProfile.equalsIgnoreCase("PEN")) && (combinedgrossSalaryip1 < 300000)) {
                    Log.consoleLog(ifr, "inside  mprofileValidationCB if Purpose" + Purpose);
                    ifr.setValue("P_VL_OD_GrossSalary", "");
                    ifr.setValue("P_VL_OD_LOAN_PURPOSE", "");
                    message.put("showMessage", cf.showMessage(ifr, control, "error", "customers Gross Income should be minimum 300000"));
                    errorMsg = "customers Gross Income should be minimum 300000";
                    return errorMsg;
                }
            } else {
                return errorMsg;
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Error occured in mprofileValidationCB" + e);
        }
        return errorMsg;
    }

    //added by janani for co-borrower validation
    public String validateCheck(IFormReference ifr) {//added by janani for co-borrower validation
        Log.consoleLog(ifr, "INSIDE validateCheck for coborrower:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String apllicantType = "";
        String applicantTypeQuery = "select APPLICANTTYPE from LOS_NL_BASIC_INFO where  pid='" + ProcessInstanceId + "'";
        List<List<String>> applicantTypeQueryEx = cf.mExecuteQuery(ifr, applicantTypeQuery, "Execute query for fetching Applicant Type");
        int rowCount = applicantTypeQueryEx.size();
        Log.consoleLog(ifr, "inside rowCount :: " + rowCount);
        if (rowCount == 1) {
            return "success";
        }
        return "";
    }

    //added by ishwarya on 02/07/2024
    public void TenureElongationUpdate(IFormReference ifr, String TenureElongation) {
        Log.consoleLog(ifr, "TenoreElongationUpdate VL===>" + TenureElongation);
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String tenureUpdationQuery = "UPDATE LOS_NL_PROPOSED_FACILITY SET TENURE_ELONGATION ='" + TenureElongation + "' WHERE PID ='" + PID + "'";
        Log.consoleLog(ifr, "tenureUpdationQuery ::::" + tenureUpdationQuery);
        int Result = ifr.saveDataInDB(tenureUpdationQuery);
        Log.consoleLog(ifr, "### TENURE_ELONGATION Updated ###" + Result);

    }

    //added by Janani on 06-07-2024
    public String OnChangeVehicleModel(IFormReference ifr) {
        Log.consoleLog(ifr, "inside in OnChangeVehicleModel");
        try {
            String brand = ifr.getValue("P_VL_OD_BRAND").toString();
            ifr.setValue("P_VL_OD_MODEL", "");
            ifr.clearCombo("P_VL_OD_MODEL");
            ifr.setValue("P_VL_OD_VARIANT", "");
            String queryV = "Select ASSETMODEL, ASSETMODELCODE From LOS_M_VEHICLE_ASSET WHERE ISACTIVE = 'Y' AND ASSETMANUFACTURERCODE ='" + brand + "'";
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load brand type");
            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                String value1 = list.get(i).get(1);
                Log.consoleLog(ifr, "value1 ::  " + value1);
                ifr.addItemInCombo("P_VL_OD_MODEL", label, value1);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in OnChangeVehicleModel" + e);
        }
        return "";
    }

    //modified by Sharon on 01-08-2024
    public String OnChangeProfileCB(IFormReference ifr) {
        Log.consoleLog(ifr, "inside in OnChangeProfileCB");
        try {
            String occpationType = ifr.getValue("P_VL_OD_Profile_CB").toString();
            ifr.setValue("P_VL_OD_TypeOfOccupation_CB", "");
            ifr.clearCombo("P_VL_OD_TypeOfOccupation_CB");
            String queryV = "Select OCCSUBTYPE From LOS_M_OCCUPATION where OCCUPATIONCODE ='" + occpationType + "' and ISACTIVE='Y' order by OCCSUBTYPE";
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load occupation type");
            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                //String value1 = list.get(i).get(1);
                ifr.addItemInCombo("P_VL_OD_TypeOfOccupation_CB", label);
            }

            Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::check category : ");
            String category = "";

            if (occpationType.equalsIgnoreCase("PEN") || occpationType.equalsIgnoreCase("Salaried")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType: " + occpationType);
                ifr.clearCombo("P_VL_OD_Category_CB");

                String queryC = "Select EMPLOYMENTCATEGORY From LOS_M_CATEGORY";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {
                    for (int i = 0; i < Math.min(8, listC.size()); i++) {
                        category = listC.get(i).get(0);
                        Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::category:" + category);
                        ifr.addItemInCombo("P_VL_OD_Category_CB", category);
                    }
                }
            } else if (occpationType.equalsIgnoreCase("SELF")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType : " + occpationType);
                ifr.clearCombo("P_VL_OD_Category_CB");

                String queryC = "SELECT EMPLOYMENTCATEGORY  FROM LOS_M_CATEGORY WHERE EMPLOYMENTCATEGORY ='Any other establishment not covered above' ";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {

                    category = listC.get(0).get(0);
                    Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::category:" + category);
                    ifr.addItemInCombo("P_VL_OD_Category_CB", category);

                }
            } else if (occpationType.equalsIgnoreCase("PROF")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType : " + occpationType);
                ifr.clearCombo("P_VL_OD_Category_CB");

                String queryC = "SELECT EMPLOYMENTCATEGORY  FROM LOS_M_CATEGORY WHERE EMPLOYMENTCATEGORY ='Self employed professionals - Doctors/Engineers/CA/ICWA/Lawyers/Architects/Own business' ";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {

                    category = listC.get(0).get(0);
                    Log.consoleLog(ifr, "occupationTypecategory:" + category);
                    ifr.addItemInCombo("P_VL_OD_Category_CB", category);

                }
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in OnChangeProfileCB" + e);
        }
        return "";
    }

    //Added by Sharon for Reset values of Co-Borrower 09/07/2024
    public void ResetCoObligantVL(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside ResetCoObligantVL ===>");
        try {
            String occpationType = ifr.getValue("P_VL_OD_Profile_CB").toString();
            Log.consoleLog(ifr, "occpationType : " + occpationType);

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID ResetCoObligantVL ===>" + PID);
            ifr.setValue("CoApplicantName_VL", "");
            ifr.setValue("P_VL_OD_MOBILE_NUMBER", "");
            ifr.setValue("P_VL_OD_CUSTOMER_ID", "");
            ifr.setValue("P_VL_OD_RELATIONSHIP_BORROWER", "");
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

                            ifr.setStyle("CB_OCCUPATION_SECTION", "visible", "false");
                            ifr.setStyle("P_OD_ValidateCoObligantVL", "disable", "false");
                            ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "disable", "false");
                            ifr.setStyle("P_VL_OD_CUSTOMER_ID", "disable", "false");
                            ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "disable", "false");

                            //PortalOccupationDetailsFieldsCoborrower
                            PortalField = ConfProperty.getCommonPropertyValue("CBPortalOccupationDetailsFieldsVL");

                            Log.consoleLog(ifr, "PortalField::" + PortalField);
                            String portalfield[] = PortalField.split(",");
                            if (occpationType.equalsIgnoreCase("Salaried") || occpationType.equalsIgnoreCase("PEN")) {
                                ifr.setValue("P_VL_OD_GrossSalary_CB", "");
                                ifr.setValue("P_VL_OD_NetIncome_CB", "");
                                ifr.setValue("P_VL_OD_DeductionFromSalary_CB", "");

                            } else {

                                ifr.setValue("P_VL_OD_GrossAnnualSalary_CB", "");
                                ifr.setValue("P_VL_OD_AnnualDeductionFromSalary_CB", "");
                                ifr.setValue("P_VL_OD_AnnualNetIncome_CB", "");

                            }
                            for (int j = 0; j < portalfield.length; j++) {
                                Log.consoleLog(ifr, "ResetCoObligantVL:setValue for occupation1 CoBo::" + (portalfield[j]));
                                ifr.setValue(portalfield[j], "");

                            }
                            Log.consoleLog(ifr, "End ResetCoObligantCB");
                        } catch (Exception e) {
                            Log.consoleLog(ifr, "Exception in ResetCoObligantVL ::" + e);
                            Log.errorLog(ifr, "Exception in ResetCoObligantVL::" + e);
                        }
                    }
                }
            } else {
                ifr.setStyle("P_OD_ValidateCoObligantVL", "disable", "false");
                ifr.setStyle("P_VL_OD_MOBILE_NUMBER", "disable", "false");
                ifr.setStyle("P_VL_OD_CUSTOMER_ID", "disable", "false");
                ifr.setStyle("P_VL_OD_RELATIONSHIP_BORROWER", "disable", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in ResetCoObligantVL" + e);
        }
    }

    public String checkLTVFourwheeler(IFormReference ifr, String RuleName, String values, String ValueTag) {

        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("ltv_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);
        Log.consoleLog(ifr, "totalGrade RETURN" + totalGrade);
        return totalGrade;
    }

    //added by ishwarya on 07-09-2024
    public String checkLTVTwowheeler(IFormReference ifr, String RuleName, String values, String ValueTag) {
        HashMap<String, Object> objm = jsonBRMSCall.getExecuteBRMSRule(ifr, RuleName, values);

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "activityName  :" + activityName);
        String totalGrade = objm.get("ltv_op").toString();
        Log.consoleLog(ifr, "objm  :" + objm);
        Log.consoleLog(ifr, "totalGrade RETURN" + totalGrade);
        return totalGrade;
    }

    //added by Sharon on 11/7/2024
//modified by ishwarya on 16/7/2024
    public String onchangeReqLoanAmtVL(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "inside onchangeReqLoanAmtVL::");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "onchangeReqLoanAmtVL ProcessInstanceId::" + ProcessInstanceId);
            JSONObject message = new JSONObject();
            String ReqLoanAmt = ifr.getValue("P_OD_VL_LOANAMTREQ").toString();
            String costOfVehicle = ifr.getValue("P_VL_OD_COST").toString();
            String downPayment = ifr.getValue("P_VL_OD_DOWNPAYMENT").toString();
            Log.consoleLog(ifr, "onchangeReqLoanAmtVL ReqLoanAmt::" + ReqLoanAmt);
            Log.consoleLog(ifr, "onchangeReqLoanAmtVL costOfVehicle::" + costOfVehicle);
            Log.consoleLog(ifr, "onchangeReqLoanAmtVL downPayment::" + downPayment);

            double vehicleCost = Double.parseDouble(costOfVehicle);
            double ReqLoan = Double.parseDouble(ReqLoanAmt);
            double downPay = Double.parseDouble(downPayment);

            double eligAmt = vehicleCost - downPay;
            Log.consoleLog(ifr, "onchangeReqLoanAmtVL eligAmt::" + eligAmt);

            if (ReqLoan > eligAmt) {
                message.put("eflag", "false");
                message.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_OD_VL_LOANAMTREQ", "error", "Requested Loan Amount cannot be greater than (Cost of Vehicle - Downpayment)."));
                ifr.setValue("P_OD_VL_LOANAMTREQ", "");
                return message.toString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in onchangeReqLoanAmtVL" + e);
        }
        return "";

    }

    //modified by Janani on 22/08/2024
    public String onchangeProfileVL(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside onchangeProfileVL");
            String occupationType = ifr.getValue("P_VL_OD_Profile").toString();
            Log.consoleLog(ifr, "occupationType : " + occupationType);
            if (occupationType.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "occupationType empty" );
                String fkey = bpcc.Fkey(ifr, "B");
                Log.consoleLog(ifr, "f_key==>" + fkey);
                String profileQuery = "select PROFILE from los_nl_occupation_info where f_key ='" + fkey + "'";
                Log.consoleLog(ifr, "profileQuery:" + profileQuery);
                List<List<String>> list = cf.mExecuteQuery(ifr, profileQuery, "Load profile");
                if (!list.isEmpty()) {
                    occupationType = list.get(0).get(0);
                }
            }
            Log.consoleLog(ifr, "occupationType : " + occupationType);
            ifr.clearCombo("P_VL_OD_TypeOfOccupation");

            String queryV = "Select OCCSUBTYPE From LOS_M_OCCUPATION where OCCUPATIONCODE ='" + occupationType + "' and ISACTIVE='Y' order by OCCSUBTYPE";
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load occupation type");

            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                ifr.addItemInCombo("P_VL_OD_TypeOfOccupation", label);
            }

            Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::check category : ");
            String category = "";

            if (occupationType.equalsIgnoreCase("PEN") || occupationType.equalsIgnoreCase("Salaried")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType: " + occupationType);
                ifr.clearCombo("P_VL_OD_Category");

                String queryC = "Select EMPLOYMENTCATEGORY From LOS_M_CATEGORY";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {
                    for (int i = 0; i < Math.min(8, listC.size()); i++) {
                        category = listC.get(i).get(0);
                        Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::category:" + category);
                        ifr.addItemInCombo("P_VL_OD_Category", category);
                    }
                }
            } else if (occupationType.equalsIgnoreCase("SELF")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType : " + occupationType);
                ifr.clearCombo("P_VL_OD_Category");

                String queryC = "SELECT EMPLOYMENTCATEGORY  FROM LOS_M_CATEGORY WHERE EMPLOYMENTCATEGORY ='Any other establishment not covered above'";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {

                    category = listC.get(0).get(0);
                    Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::category:" + category);
                    ifr.addItemInCombo("P_VL_OD_Category", category);

                }
            } else if (occupationType.equalsIgnoreCase("PROF")) {
                Log.consoleLog(ifr, "VLPortal::onchangeProfileVL::occupationType : " + occupationType);
                ifr.clearCombo("P_VL_OD_Category");

                String queryC = "SELECT EMPLOYMENTCATEGORY  FROM LOS_M_CATEGORY WHERE EMPLOYMENTCATEGORY ='Self employed professionals - Doctors/Engineers/CA/ICWA/Lawyers/Architects/Own business' ";
                Log.consoleLog(ifr, "queryC:" + queryC);
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryC, "Load LOS_M_CATEGORY");

                if (!listC.isEmpty()) {

                    category = listC.get(0).get(0);
                    Log.consoleLog(ifr, "occupationTypecategory:" + category);
                    ifr.addItemInCombo("P_VL_OD_Category", category);

                }
            }

            if (occupationType.equalsIgnoreCase("PEN")) {
                ifr.setValue("P_VL_OD_DATEOFSUPERANNUATION", "");
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Error occurred in onchangeProfileVL: " + e);
        }
        return "";
    }

    //added by Sharon on 19/07/2024
    public String OnchangeCostofVehicle(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside OnchangeCostofVehicle");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "OnchangeCostofVehicle ProcessInstanceId::" + ProcessInstanceId);
            JSONObject message = new JSONObject();
            String costOfVehicle = ifr.getValue("P_VL_OD_COST").toString();
            String purposeOfLoan = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();

            Log.consoleLog(ifr, "OnchangeCostofVehicle::costOfVehicle::" + costOfVehicle);
            Log.consoleLog(ifr, "OnchangeCostofVehicle::purposeOfLoan::" + purposeOfLoan);
            double vehicleCost = Double.parseDouble(costOfVehicle);
            Log.consoleLog(ifr, "OnchangeCostofVehicle::vehicleCost::" + vehicleCost);

            if (purposeOfLoan.equals("N2W") && vehicleCost < 100000) {
                Log.consoleLog(ifr, "OnchangeCostofVehicle::inside if::" + purposeOfLoan);
                String queryBRANCHCODE = "select BRANCHCODE from LOS_NL_CASA_ASSET_VAL where PID ='" + ProcessInstanceId + "'";
                Log.consoleLog(ifr, "OnchangeCostofVehicle::queryBRANCHCODE:" + queryBRANCHCODE);
                List<List<String>> list = cf.mExecuteQuery(ifr, queryBRANCHCODE, "OnchangeCostofVehicle::queryBRANCHCODE");
                String branchCode = "";
                if (!list.isEmpty()) {
                    branchCode = list.get(0).get(0);
                }
                Log.consoleLog(ifr, "OnchangeCostofVehicle::branchCode::" + branchCode);
                String queryOrgCategory = "select ORG_CATEGORY from los_m_branch where BRANCHCODE='" + branchCode + "'";
                Log.consoleLog(ifr, "OnchangeCostofVehicle::queryBRANCHCODE:" + queryOrgCategory);
                List<List<String>> listCategory = cf.mExecuteQuery(ifr, queryOrgCategory, "OnchangeCostofVehicle::queryBRANCHCODE");
                String OrgCategory = "";
                if (!listCategory.isEmpty()) {
                    OrgCategory = listCategory.get(0).get(0);
                }

                Log.consoleLog(ifr, "OnchangeCostofVehicle::OrgCategory:" + OrgCategory);

                if (OrgCategory.equalsIgnoreCase("Metro") || OrgCategory.equalsIgnoreCase("Urban")) {
                    Log.consoleLog(ifr, "OnchangeCostofVehicle::inside if::" + OrgCategory);
                    message.put("showMessage", cf.showMessage(ifr, "P_VL_OD_COST", "error", "Minimum Invoice value of the vehicle should be 100000 or above."));
                    ifr.setValue("P_VL_OD_COST", "");
                    message.put("eflag", "false");
                    return message.toString();
                }

            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in OnchangeCostofVehicle" + e);
        }
        return "";
    }

    //added by janani on 22-07-2024	
    //modified by janani on 26-07-2024
    public String onChangeOccTypeCBVL(IFormReference ifr) {
        Log.consoleLog(ifr, "VLPortalCustomCode:onChangeOccTypeCBVL->onChangeOccTypeCBVL :  ");
        try {
            String occpationType = ifr.getValue("P_VL_OD_Profile_CB").toString();
            Log.consoleLog(ifr, "occpationType : " + occpationType);
            if (occpationType.equalsIgnoreCase("Salaried")) {
                ifr.setStyle("P_VL_OD_GrossSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome_CB", "disable", "true");
                ifr.setStyle("P_VL_OD_DeductionFromSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualNetIncome_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_ExperienceYear_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_RelationshipWithCanara_CB", "visible", "true");
            } else if (occpationType.equalsIgnoreCase("PEN")) {
                ifr.setStyle("P_VL_OD_GrossSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_NetIncome_CB", "disable", "true");
                ifr.setStyle("P_VL_OD_DeductionFromSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_AnnualNetIncome_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_ExperienceYear_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_RelationshipWithCanara_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_RelationshipWithCanara_CB", "visible", "true");
            } else if (occpationType.equalsIgnoreCase("SELF") || occpationType.equalsIgnoreCase("PROF") || occpationType.equalsIgnoreCase("NIE")) {
                ifr.setStyle("P_VL_OD_GrossSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_NetIncome_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_DeductionFromSalary_CB", "visible", "false");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_ExperienceYear_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome_CB", "disable", "true");
                ifr.setStyle("P_VL_OD_GrossAnnualSalary_CB", "mandatory", "true");
                ifr.setStyle("P_VL_OD_AnnualDeductionFromSalary_CB", "mandatory", "true");
                ifr.setStyle("P_VL_OD_AnnualNetIncome_CB", "mandatory", "true");
                ifr.setStyle("P_VL_OD_ExperienceYear_CB", "visible", "true");
                ifr.setStyle("P_VL_OD_RelationshipWithCanara_CB", "visible", "true");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in VLPortalCustomCode:onChangeOccTypeCBVL->onChangeOccTypeCBVL : " + e);
        }
        return "";
    }

    public String mCheckWorkingExperienceB(IFormReference ifr, String currentExperienceID, String totalExperienceID) {
        try {
            Log.consoleLog(ifr, "Inside mCheckWorkingExperienceB VL:");
            String currentExperience = ifr.getValue(currentExperienceID).toString();
            String totalExperience = ifr.getValue(totalExperienceID).toString();
            Log.consoleLog(ifr, "Inside mCheckWorkingExperience: currentExperience " + currentExperience);
            Log.consoleLog(ifr, "Inside mCheckWorkingExperience: totalExperience " + totalExperience);
            String OccupationType = ifr.getValue("P_VL_OD_Profile").toString();
            Log.consoleLog(ifr, "Inside if is empty P_VL_OD_Profile: P_VL_OD_Profile " + OccupationType);
            if (!OccupationType.equalsIgnoreCase("PEN")) {
                if (!(totalExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase(null))) {
                    Log.consoleLog(ifr, "Inside if is empty mCheckWorkingExperience: totalExperience " + totalExperience);
                    int currentExp = 0;
                    if (!(currentExperience.equalsIgnoreCase(""))) {
                        currentExp = Integer.parseInt(currentExperience);
                    }
                    int totalExp = 0;
                    if (!(totalExperience.equalsIgnoreCase(""))) {
                        totalExp = Integer.parseInt(totalExperience);
                    }
                    if (currentExp == 0 || totalExp == 0) {
                        Log.consoleLog(ifr, "Inside if condition of mCheckWorkingExperience:");
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Current Experience and Total Experience Should Not Be Zero!"));
                        return message.toString();
                    }
                    if (currentExp > totalExp
                            && (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
                        Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp : " + totalExp + "currentExp : " + currentExp);
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Current Experience cannot be greater than Total Experience!"));
                        return message.toString();
                    }
                    if (totalExp < currentExp
                            && (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
                        Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp Less : " + totalExp + "currentExp : " + currentExp);
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Total Experience cannot be Less than Current Experience!"));
                        return message.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in VLPortalCustomCode:onChangeOccTypeCBVL->onChangeOccTypeCBVL : " + e);
        }
        return "";
    }

    public String mCheckWorkingExperienceCB(IFormReference ifr, String currentExperienceID, String totalExperienceID) {
        try {
            Log.consoleLog(ifr, "Inside mCheckWorkingExperienceCB VL:");
            String currentExperience = ifr.getValue(currentExperienceID).toString();
            String totalExperience = ifr.getValue(totalExperienceID).toString();
            Log.consoleLog(ifr, "Inside mCheckWorkingExperience: currentExperience " + currentExperience);
            Log.consoleLog(ifr, "Inside mCheckWorkingExperience: totalExperience " + totalExperience);
            String OccupationType = ifr.getValue("P_VL_OD_Profile_CB").toString();
            Log.consoleLog(ifr, "Inside if is empty P_VL_OD_Profile_CB: P_VL_OD_Profile_CB " + OccupationType);
            if (!OccupationType.equalsIgnoreCase("PEN")) {
                if (!(totalExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase(null))) {
                    Log.consoleLog(ifr, "Inside if is empty mCheckWorkingExperience: totalExperience " + totalExperience);
                    int currentExp = 0;
                    if (!(currentExperience.equalsIgnoreCase(""))) {
                        currentExp = Integer.parseInt(currentExperience);
                    }
                    int totalExp = 0;
                    if (!(totalExperience.equalsIgnoreCase(""))) {
                        totalExp = Integer.parseInt(totalExperience);
                    }
                    if (currentExp == 0 || totalExp == 0) {
                        Log.consoleLog(ifr, "Inside if condition of mCheckWorkingExperience:");
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Current Experience and Total Experience Should Not Be Zero!"));
                        return message.toString();
                    }
                    if (currentExp > totalExp
                            && (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
                        Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp : " + totalExp + "currentExp : " + currentExp);
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Current Experience cannot be greater than Total Experience!"));
                        return message.toString();
                    }
                    if (totalExp < currentExp
                            && (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
                        Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp Less : " + totalExp + "currentExp : " + currentExp);
                        ifr.setValue(currentExperienceID, "");
                        ifr.setValue(totalExperienceID, "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error",
                                "Total Experience cannot be Less than Current Experience!"));
                        return message.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in VLPortalCustomCode:->mCheckWorkingExperienceCB : " + e);
        }
        return "";
    }

    //ADDED by sharon on 29/07/2024 
    public String mImplChangePurpose(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside VLPortal::mImplChangePurpose VL:");
            String purposeVL = ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString();
            String fuelType = "";
            String fuelType1 = "";
            String fuelType2 = "";
            if (purposeVL.equalsIgnoreCase("N2W")) {
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL:" + purposeVL);
                ifr.clearCombo("P_VL_OD_FUEL");
                String queryV = "SELECT FUELTYPE  FROM LOS_MST_FUELTYPE WHERE FUELTYPE IN ('Petrol','EV')";
                // String queryV = "select FUELTYPE from LOS_MST_FUELTYPE where FUELTYPE <> 'Diesel' and  FUELTYPE <> 'CNG' order by FUELTYPE";
                List<List<String>> listC = cf.mExecuteQuery(ifr, queryV, "Load occupation type");
                if (!listC.isEmpty()) {
                    fuelType = listC.get(0).get(0);
                    fuelType1 = listC.get(1).get(0);
                }
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL::fuelType:" + fuelType);
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL::fuelType1:" + fuelType1);
                ifr.addItemInCombo("P_VL_OD_FUEL", fuelType);
                ifr.addItemInCombo("P_VL_OD_FUEL", fuelType1);

            } else if (purposeVL.equalsIgnoreCase("N4W")) {
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL:" + purposeVL);
                ifr.clearCombo("P_VL_OD_FUEL");
                String queryV = "select FUELTYPE from LOS_MST_FUELTYPE where FUELTYPE <> 'EV' order by FUELTYPE";

                List<List<String>> listC = cf.mExecuteQuery(ifr, queryV, "Load occupation type");
                if (!listC.isEmpty()) {
                    fuelType = listC.get(0).get(0);
                    fuelType1 = listC.get(1).get(0);
                    fuelType2 = listC.get(2).get(0);
                }
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL::fuelType:" + fuelType);
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL::fuelType1:" + fuelType1);
                Log.consoleLog(ifr, "VLPortal::mImplChangePurpose VL::fuelType2:" + fuelType2);
                ifr.addItemInCombo("P_VL_OD_FUEL", fuelType);
                ifr.addItemInCombo("P_VL_OD_FUEL", fuelType1);
                ifr.addItemInCombo("P_VL_OD_FUEL", fuelType2);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in VLPortalCustomCode:->mImplChangePurpose : " + e);
        }
        return "";
    }
}
