/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.homeloan;

import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.fintec.ConsumerAPI;
import com.newgen.dlp.integration.fintec.ExperianAPI;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class HLPortalCustomCode {

    PortalCommonMethods pcm = new PortalCommonMethods();
    BRMSRules jsonBRMSCall = new BRMSRules();
    CommonFunctionality cf = new CommonFunctionality();
    CustomerAccountSummary cas = new CustomerAccountSummary();
    CommonMethods objcm = new CommonMethods();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
    BRMCommonRules objbcr = new BRMCommonRules();

    //added by ishwarya on 21/08/2024
    public String autoPopulateLoanDetailsDataHL(IFormReference ifr, String control, String event, String value) {

        String currentStep = pcm.setGetPortalStepName(ifr, value);
        Log.consoleLog(ifr, "currentStep HL::::: ");
        try {
            Log.consoleLog(ifr, "inside try block::::autoPopulateLoanDetailsDataHL::::: ");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            ifr.setValue("PIDValue", ProcessInsanceId);
            ifr.setStyle("P_HL_LD_Loan_tw_plot", "visible", "false");
            ifr.setStyle("P_HL_LD_Loan_tw_construnction", "visible", "false");

            String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            String queryPeradd = ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateLoanDetailsDataHL-> CA:" + queryResadd);
            Log.consoleLog(ifr, "PortalCustomCode:autoPopulateLoanDetailsDataHL-> PA:" + queryPeradd);

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
                ifr.setValue("P_HL_LD_EMAILID", EMAILID);
                ifr.setValue("P_HL_LD_MOBILENO", strmobile);
                ifr.setValue("P_HL_LD_COMMUNICATION_ADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + "," + state + "," + country + "," + pincode);
            }
            if (resultPeradd.size() > 0) {
                String addressLine1 = resultPeradd.get(0).get(0);
                String addressLine2 = resultPeradd.get(0).get(1);
                String addressLine3 = resultPeradd.get(0).get(2);
                String pincode = resultPeradd.get(0).get(3);
                ifr.setValue("P_HL_LD_PERMANENT_ADDRESS", addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + pincode);
            }

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
                ifr.setValue("P_CB_H_CUSTOMERNAME1", Fullname.replace("  ", " "));
            }
            String Product = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataHL:getProductCode::" + Product);
            String ProductName = "";
            String ProductNameQuery = "SELECT ProductName FROM LOS_M_Product WHERE ProductCode='" + Product + "'";
            List<List<String>> ProductNameList = ifr.getDataFromDB(ProductNameQuery);
            if (!ProductNameList.isEmpty()) {
                ProductName = ProductNameList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Product Name::autoPopulateLoanDetailsDataHL:::" + ProductName);
            Log.consoleLog(ifr, "Before");
            ifr.setValue("P_HL_LD_Product", ProductName);
            ifr.setStyle("P_HL_LD_Product", "disable", "true");
            String subProduct = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "BudgetPortalCustomCode::autoPopulateLoanDetailsDataHL:getSubProductCode::" + subProduct);
            String ROI = pcm.mGetROICB(ifr);
            ifr.setValue("P_HL_LD_Total_ROI", ROI);
            ifr.setStyle("P_HL_LD_Total_ROI", "disable", "true");
            ifr.setValue("P_HL_LD_FRP", "0.75%");
            ifr.setStyle("P_HL_LD_FRP", "disable", "true");
            String RLLR = "";
            String RLLRQuery = "select final_rllr from LOS_MST_RLLR where base_type='RLLR'";
            List<List<String>> resultRLLRQuery = ifr.getDataFromDB(RLLRQuery);
            if (!resultRLLRQuery.isEmpty()) {
                RLLR = resultRLLRQuery.get(0).get(0);
            }
            Log.consoleLog(ifr, "RLLR::autoPopulateLoanDetailsDataHL:::" + RLLR);
            ifr.setValue("P_HL_LD_RLLR", RLLR);
            ifr.setStyle("P_HL_LD_RLLR", "disable", "true");
            String mobileNumber = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "mobileNumber::autoPopulateLoanDetailsDataHL:::" + mobileNumber);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside autoPopulateLoanDetailsDataHL::::" + e);
            Log.errorLog(ifr, "Exception inside autoPopulateLoanDetailsDataHL::::" + e);
            return pcm.returnError(ifr);
        }
        return currentStep;
    }

    //added by ishwarya on 21/08/2024
    public String mImpOnClickLoanDetailsDataHL(IFormReference ifr, String control, String event, String value) {
        JSONObject message = new JSONObject();

        try {
            Log.consoleLog(ifr, "inside try block::::mImpOnClickLoanDetailsDataHL::::: ");

            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId::mImpOnClickLoanDetailsDataHL:::" + CustomerId);
            String MobileNo = pcm.getMobileNumber(ifr);
            Log.consoleLog(ifr, "MobileNo:::mImpOnClickLoanDetailsDataHL:::" + MobileNo);
            String response = cas.getCustomerAccountParams_CB(ifr, MobileNo);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB:::mImpOnClickLoanDetailsDataHL:::>" + response);
            String productCode = pcm.getProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataHL::" + productCode);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:::mImpOnClickLoanDetailsDataHL::" + subProductCode);
            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
            String PANNUMBER = OutputJSON.get("PanNumber").toString();
            String DateofBirth = OutputJSON.get("DateofBirth").toString();

            Log.consoleLog(ifr, "AADHARNUMBER::mImpOnClickLoanDetailsDataHL::" + AADHARNUMBER);
            Log.consoleLog(ifr, "PANNUMBER::mImpOnClickLoanDetailsDataHL::" + PANNUMBER);
            Log.consoleLog(ifr, "DateofBirth::mImpOnClickLoanDetailsDataHL::" + DateofBirth);
            String ReqLoamAmount = ifr.getValue("P_HL_LD_Requested_Loan_Amount").toString();
            if (Integer.parseInt(ReqLoamAmount) < 1000000) {
                Log.consoleLog(ifr, "inside requested loan amount is less than 1 lakh:::::");
                String cb = mCallBureau(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataHL::::");
                    //populateLoanDetailsCB(ifr, control, event, value);
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataHL::::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else if (Integer.parseInt(ReqLoamAmount) >= 1000000) {
                Log.consoleLog(ifr, "inside requested loan amount is greater than 1 lakh:::::");
                String cb = mCallBureau(ifr, "CB", AADHARNUMBER, "B", ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }

                String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");

                Log.consoleLog(ifr, "decision1/CB::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CIBIL Passed Successfully::mImpOnClickLoanDetailsDataHL::::");
                    String EX = mCallBureau(ifr, "EX", AADHARNUMBER, "B", ReqLoamAmount);
                    if (EX.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    }
                    decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "Ex", "B");

                    Log.consoleLog(ifr, "decision2/EX::" + decision);
                    if (decision.contains(RLOS_Constants.ERROR)) {
                        return pcm.returnError(ifr);
                    } else if (decision.equalsIgnoreCase("Approve")) {
                        Log.consoleLog(ifr, "EXPERIAN Passed Successfully::mImpOnClickLoanDetailsDataHL:::");
                        //populateLoanDetailsCB(ifr, control, event, value);
                    } else {
                        Log.consoleLog(ifr, "Experian Failed:::mImpOnClickLoanDetailsDataHL:::");
                        message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                        message.put("eflag", "false");
                        return message.toString();
                    }
                } else {
                    Log.consoleLog(ifr, "Cibil Failed:::mImpOnClickLoanDetailsDataHL::::");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
            } else {
                Log.consoleLog(ifr, "inside requested loan amount is empty:::::");
                return message.toString();
            }
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception inside mImpOnClickLoanDetailsDataHL::::" + e);
            Log.errorLog(ifr, "Exception inside mImpOnClickLoanDetailsDataHL::::" + e);
            return pcm.returnError(ifr);
        }
        return "";
    }

    //added by ishwarya on 21/08/2024
    public String mCallBureau(IFormReference ifr, String BureauType, String aadharNo, String applicantType, String loanamount) throws ParseException {
        Log.consoleLog(ifr, "Inside mCallBureau:");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery;
        if (BureauType.equalsIgnoreCase("CB")) {
            CountQuery = ConfProperty.getQueryScript("getCibilCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType);
        } else {
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
        Log.consoleLog(ifr, "minLoanamount:" + minLoanmaount);
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

    //added by ishwarya on 21/08/2024
    public String mChangePurpose(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside HLPortal::mChangePurpose HL:");
            String purposeHL = ifr.getValue("P_HL_LD_Purpose").toString();
            if (purposeHL.equalsIgnoreCase("PSC")) {
                ifr.setStyle("P_HL_LD_Loan_tw_plot", "visible", "true");
                ifr.setStyle("P_HL_LD_Loan_tw_construnction", "visible", "true");
                ifr.setStyle("P_HL_LD_Requested_Loan_Amount", "disable", "true");
            } else {
                ifr.setStyle("P_HL_LD_Loan_tw_plot", "visible", "false");
                ifr.setStyle("P_HL_LD_Loan_tw_construnction", "visible", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in HLPortalCustomCode:->mChangePurpose : " + e);
        }
        return "";
    }

    //added by ishwarya on 21/08/2024
    public String onchangeReqLoanAmt(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside HLPortal::onchangeReqLoanAmtHL HL:");

            String loanPlot = ifr.getValue("P_HL_LD_Loan_tw_plot").toString();
            String loanConstruction = ifr.getValue("P_HL_LD_Loan_tw_construnction").toString();
            int loanplot = Integer.parseInt(loanPlot);
            int loanconst = Integer.parseInt(loanConstruction);
            int loanReq = loanplot + loanconst;
            Log.consoleLog(ifr, "HLPortal::onchangeReqLoanAmtHL HL::loanReq" + loanReq);
            ifr.setValue("P_HL_LD_Requested_Loan_Amount", String.valueOf(loanReq));
            ifr.setStyle("P_HL_LD_Requested_Loan_Amount", "disable", "true");

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in HLPortalCustomCode:->onchangeReqLoanAmtHL : " + e);
        }
        return "";
    }

}
