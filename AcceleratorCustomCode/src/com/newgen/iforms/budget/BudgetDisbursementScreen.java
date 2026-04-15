/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.budget;

import com.newgen.dlp.integration.cbs.LoanAccountCreation;
import com.newgen.dlp.integration.common.APIPreprocessor;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ahmed.zindha
 */
public class BudgetDisbursementScreen {

//    CBS_LoanAccountCreation CB1 = new CBS_LoanAccountCreation();
//    CBS_DisbursementEnquiry CB2 = new CBS_DisbursementEnquiry();
//    CBS_LoanDeduction CB3 = new CBS_LoanDeduction();
//    CBS_ComputeLoanSchedule CB4 = new CBS_ComputeLoanSchedule();
//    CBS_GenerateLoanSchedule CB5 = new CBS_GenerateLoanSchedule();
//    CBS_SaveLoanSchedule CB6 = new CBS_SaveLoanSchedule();
//    CBS_BranchDisbursement CB7 = new CBS_BranchDisbursement();
//    CBS_FundTransfer CB8 = new CBS_FundTransfer();
    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    LoanAccountCreation API = new LoanAccountCreation();
    APIPreprocessor objPreprocess = new APIPreprocessor();

    public String CBSFinalScreenValidation(IFormReference ifr, String ProcessInsatnceId) {

        Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
        try {

            Log.consoleLog(ifr, "Details available to execute CBS API`s");
            String LoanAccountNumber = objPreprocess.execLoanAccountCreation(ifr, "BUDGET");
            if (!(LoanAccountNumber.contains(RLOS_Constants.ERROR))) {
                String CBSDisbursementEnquiry = objPreprocess.execDisbursementEnquiry(ifr, LoanAccountNumber, "BUDGET");
                if (!(CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR))) {
                    String CBS_LoanDeduction = objPreprocess.execLoanDeduction(ifr, LoanAccountNumber, "BUDGET");
                    if (!(CBS_LoanDeduction.contains(RLOS_Constants.ERROR))) {
                        String SessionId = objPreprocess.execComputeLoanSchedule(ifr, LoanAccountNumber, "BUDGET");
                        if (!(SessionId.contains(RLOS_Constants.ERROR))) {
                            String CBS_GenerateLoanSchedule = objPreprocess.execGenerateLoanSchedule(ifr, LoanAccountNumber, SessionId, "BUDGET");
                            if (!(CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                String CBS_SaveLoanSchedule = objPreprocess.execSaveLoanSchedule(ifr, LoanAccountNumber, SessionId, "BUDGET");
                                if (!(CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                    //CB7.CBS_RepaymentLoanSchedule(ifr, LoanAccountNumber);
                                    String CBS_BranchDisbursement = objPreprocess.execBranchDisbursement(ifr, LoanAccountNumber, "BUDGET");

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
                                                "BUDGET", loanAmount, tenure);
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

    public String getloanaccountcreation(IFormReference ifr, String ProductType) {
        String LoanAccNumber = "";
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        Log.consoleLog(ifr, "LoanAccNumber not available");
        try {

            String ProductCode = "";//Need to change as oer the final requets
            String CustomerId = "";
            String LoanAmount = "";
            String Tenure = "";

            CustomerId = pcm.getCustomerIDForOtherProducts(ifr);
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", ProcessInstanceId);

            //"SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
            //  + "WHERE WINAME='" + ProcessInstanceId + "'";
            List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Get Loan account details ");

            if (Output3.size() > 0) {
                LoanAmount = Output3.get(0).get(0);
                Tenure = Output3.get(0).get(1);
            }

            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_YEAR, 90);
            Date dateAfter90Days = calendar.getTime();
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
            String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);

            LoanAccNumber = API.getLoanAccountDetails(ifr, ProcessInstanceId, ProductCode,
                    CustomerId, LoanAmount, Tenure, SanctionExpiryDate, ProductType);
            Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
            if (LoanAccNumber.equalsIgnoreCase("")) {
                return RLOS_Constants.ERROR;
            }

            return LoanAccNumber;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
            Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
            return RLOS_Constants.ERROR;
        }

    }

    public String branchdisbursement(IFormReference ifr, String ProductType) {
        String ProcessInsatnceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
        try {

            Log.consoleLog(ifr, "Details available to execute CBS API`s");
            String LoanAccountNumber = "";
            String loanaccountnumber = ConfProperty.getQueryScript("GETLOANNODETAILSINFO")
                    .replaceAll("#WINAME#", ProcessInsatnceId);
            List<List<String>> Output = cf.mExecuteQuery(ifr, loanaccountnumber, "loanaccountnumber Details ");

            if (!Output.isEmpty()) {
                LoanAccountNumber = Output.get(0).get(0);
            }

            String CBSDisbursementEnquiry = objPreprocess.execDisbursementEnquiry(ifr, LoanAccountNumber, ProductType);
            if (!(CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR))) {
                String CBS_LoanDeduction = objPreprocess.execLoanDeduction(ifr, LoanAccountNumber, ProductType);
                if (!(CBS_LoanDeduction.contains(RLOS_Constants.ERROR))) {
                    String SessionId = objPreprocess.execComputeLoanSchedule(ifr, LoanAccountNumber, ProductType);
                    if (!(SessionId.contains(RLOS_Constants.ERROR))) {
                        String CBS_GenerateLoanSchedule = objPreprocess.execGenerateLoanSchedule(ifr, LoanAccountNumber, SessionId, ProductType);
                        if (!(CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR))) {
                            String CBS_SaveLoanSchedule = objPreprocess.execSaveLoanSchedule(ifr, LoanAccountNumber, SessionId, ProductType);
                            if (!(CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                //CB7.CBS_RepaymentLoanSchedule(ifr, LoanAccountNumber);
                                String CBS_BranchDisbursement = objPreprocess.execBranchDisbursement(ifr, LoanAccountNumber, ProductType);

                                if (!(CBS_BranchDisbursement.contains(RLOS_Constants.ERROR))) {
                                    String loanAmount = "";
                                    String tenure = "";
                                    String SBACCNUMBER = "";

                                    String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", ProcessInsatnceId);
                                    List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "LOANDETAILSQUERYFROMBACKOFFICE");

                                    if (Output3.size() > 0) {
                                        loanAmount = Output3.get(0).get(0);
                                        tenure = Output3.get(0).get(1);
                                        SBACCNUMBER = Output3.get(0).get(2);
                                    }
//Added by monesh on 17/06/2024 for update the status
                                    String CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER,
                                            ProductType, loanAmount, tenure);
                                    String updatequery = ConfProperty.getQueryScript("UPDATEDISB").replaceAll("#WINAME#", ProcessInsatnceId);
                                    List<List<String>> Output4 = cf.mExecuteQuery(ifr, updatequery, "Update disbursementLOANDETAILSQUERYFROMBACKOFFICE");

                                    return CBS_FundTransfer;
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

    public String checkmisdata(IFormReference ifr) {
        try {
            BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
            String ProcessInsatnceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String FKEY = bpcc.Fkey(ifr, "B");
            String query = ConfProperty.getQueryScript("MISCODEUPDATEQUERY").replaceAll("#PID#", ProcessInsatnceId).replaceAll("#FKEY#", FKEY);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "MISCODEUPDATEQUERY");
            if (result.size() > 0) {
                for (List<String> row : result) {
                    for (String value : row) {
                        Log.consoleLog(ifr, "Exception: " + value);
                        if (value == null || value.equalsIgnoreCase("NULL") || value.isEmpty()) {
                            // Log and return the error constant
                            Log.consoleLog(ifr, "Error: Found null or empty value");
                            return RLOS_Constants.ERROR;
                        }
                    }
                }

            }
            return RLOS_Constants.SUCCESS;

        } catch (Exception e) {

            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        }

    }

	   public String checkbamdata(IFormReference ifr, String queryName, String replacevalue) {
        try {
            BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
            String ProcessInsatnceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String query = ConfProperty.getQueryScript(queryName).replaceAll("#" + replacevalue + "#", ProcessInsatnceId);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "queryName==" + queryName);
            if (result.size() > 0) {
                for (List<String> row : result) {
                    for (String value : row) {
                        Log.consoleLog(ifr, "Exception: " + value);
                        if (value == null || value.equalsIgnoreCase("NULL") || value.isEmpty()) {
                            // Log and return the error constant
                            Log.consoleLog(ifr, "Error: Found null or empty value");
                            return RLOS_Constants.ERROR;
                        }
                    }
                }

            }
            return RLOS_Constants.SUCCESS;

        } catch (Exception e) {

            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        }

    }

}
