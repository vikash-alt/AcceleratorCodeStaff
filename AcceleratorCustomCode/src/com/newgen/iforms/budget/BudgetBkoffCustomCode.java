/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.budget;

import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import org.json.simple.JSONArray;
import java.util.List;
import com.newgen.iforms.custom.IFormAPIHandler;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import java.util.HashMap;
import com.newgen.dlp.integration.brm.BRMCommonRules;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.cbs.PensionDetails;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import java.math.MathContext;
import java.util.Arrays;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import com.newgen.dlp.commonobjects.api.MockDataLoader;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import static com.newgen.iforms.pension.PensionLoanPortalCustomCode.differenceInMonths;
import com.newgen.iforms.vl.VLPortalCustomCode;
import static java.nio.file.Files.list;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author neethiyal.s
 */
public class BudgetBkoffCustomCode {

    CommonMethods cm = new CommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    // BRMSRules jsonBRMSCall = new BRMSRules();
    CommonFunctionality cf = new CommonFunctionality();
    BRMCommonRules objbcr = new BRMCommonRules();
    BudgetPortalCustomCode bcc = new BudgetPortalCustomCode();
    CustomerAccountSummary cas = new CustomerAccountSummary();
    //LoanEligibilityCheck LoanEC = new LoanEligibilityCheck();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
    MockDataLoader apimr = new MockDataLoader();
    VLPortalCustomCode vlpcc = new VLPortalCustomCode();
    PensionDetails pd = new PensionDetails();
    Advanced360EnquiryDatav2 objCbs360 = new Advanced360EnquiryDatav2();

    //Function modified by Ahmed on 28-06-2024
//    public String checkExistingCanaraBudgetLoan(IFormReference ifr, String Control, String Event, String value) {
//        JSONObject obje = new JSONObject();
//        try {
//            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
//            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
//            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
//            Advanced360EnquiryData API360 = new Advanced360EnquiryData();
//            String response = API360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "Budget");
//            Log.consoleLog(ifr, "response==>" + response);
//            String ProductCode = "";
//
//            if (response.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            } else {
//                JSONParser jsonparser = new JSONParser();
//                JSONObject obj = (JSONObject) jsonparser.parse(response);
//                ProductCode = cf.getJsonValue(obj, "ProductCode");
//
//                Log.consoleLog(ifr, "canaraBudgetProductCode==>" + ProductCode);
//
//            }
//
//            if (ProductCode.equalsIgnoreCase("626")) {
//
//                obje.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly ensure to close the existing Canara Loan/Other Personal loans"));
//                Log.consoleLog(ifr, "canaraBudgetProductCode check :customer  already has an existing Canara budget loan :" + ProductCode);
//                obje.put("", "false");
//            } else {
//                //obje.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Customer not having  existing Canara Loan/Other Personal loans"));
//                Log.consoleLog(ifr, "canaraBudgetProductCode check :customer not having existing Canara budget loan :" + ProductCode);
//
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception in checkExistingCanaraBudgetLoan");
//            Log.errorLog(ifr, "Exception in checkExistingCanaraBudgetLoan");
//        }
//        Log.consoleLog(ifr, "obje.toString()");
//        return obje.toString();
//    }
    public String checkExistingCanaraBudgetLoan(IFormReference ifr, String Control, String Event, String value) {
        JSONObject obje = new JSONObject();
        try {
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "processInstanceId==>" + processInstanceId);

            String querylAPExist = "SELECT EXISTLOANYN FROM LOS_TRN_CUSTOMERSUMMARY WHERE WINAME='" + processInstanceId + "'";
            List< List< String>> querylAPExistResult = ifr.getDataFromDB(querylAPExist);
            Log.consoleLog(ifr, "querylAPExistResult===>" + querylAPExistResult);
            String lapExists = "";
            if (!querylAPExistResult.isEmpty()) {
                lapExists = querylAPExistResult.get(0).get(0);
            }
            Log.consoleLog(ifr, "lapExists===>" + lapExists);

            if (lapExists.equalsIgnoreCase("Yes")) {
                obje.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly ensure to close the existing Canara Loan/Other Personal loans"));
                Log.consoleLog(ifr, "canaraBudgetProductCode check :customer  already has an existing Canara budget loan :" + lapExists);
                obje.put("", "false");
            } else {
                Log.consoleLog(ifr, "canaraBudgetProductCode check :customer not having existing Canara budget loan :" + lapExists);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in checkExistingCanaraBudgetLoan");
            Log.errorLog(ifr, "Exception in checkExistingCanaraBudgetLoan");
        }
        Log.consoleLog(ifr, "obje.toString()");
        return obje.toString();
    }

    public String mDisbursementCheckerAPICallCB(IFormReference ifr, String ProductType) {
        try {
            Log.consoleLog(ifr, "inside method mDisbursementCheckerAPICallCB");
            LocalDate curDate = LocalDate.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Define a custom format
            String DateToday = curDate.format(formatter);
            Log.consoleLog(ifr, "Current Date  :" + DateToday);

            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            BudgetDisbursementScreen bds = new BudgetDisbursementScreen();
            String response = bds.branchdisbursement(ifr, ProductType);
            Log.consoleLog(ifr, "response==>" + response);
            if (response.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside if::::");
                return pcm.returnErrorHold(ifr);
            } else {
                Log.consoleLog(ifr, "inside else::::");
//                ifr.setValue("QNL_BENEFICIARY_DETAILS_Status", "Success");

                //Added by Ahmed on 14-08-2024 triggering MailContent from DLPCommonObjects=========
                String qryLoanDetails = "select LOAN_ACCOUNTNO,DISB_AMOUNT,SB_ACCOUNTNO from  LOS_TRN_LOANDETAILS where PID='" + ProcessInstanceId + "'";
                List< List< String>> result = ifr.getDataFromDB(qryLoanDetails);
                Log.consoleLog(ifr, "#result===>" + result.toString());
                String loanAccNumber = "";
                String loanAmount = "";
                String disbAccNumber = "";
                if (!result.isEmpty()) {
                    loanAccNumber = result.get(0).get(0);
                    loanAmount = result.get(0).get(1);
                    disbAccNumber = result.get(0).get(2);
                }
                String bodyParams = pcm.getLoanSelected(ifr) + "#" + loanAccNumber + "#" + loanAmount + "#" + disbAccNumber;
                String subjectParams = "";
                String fileName = "";
                String fileContent = "";
                pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "", "12", bodyParams, subjectParams, fileName, fileContent);

                ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", 0, 7, "Disbursed");
                ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", 0, 8, "Success");
                ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", 0, 9, DateToday);

                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mDisbursementCheckerAPICallCB");
        }
        return RLOS_Constants.ERROR;

    }

    public String mDisbursementMakerAPICallCB(IFormReference ifr, String ProductType) {
        try {
            Log.consoleLog(ifr, "inside method mDisbursementMakerAPICallCB" + ProductType);
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONArray arr = new JSONArray();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String LoanAmount = "";
            String Tenure = "";
            String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", ProcessInstanceId);
            //"SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
            //  + "WHERE WINAME='" + ProcessInstanceId + "'";
            List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Get Loan account details ");

            if (Output3.size() == 0) {
                Log.consoleLog(ifr, "LoanAmount Empty==>" + LoanAmount);
                return pcm.returnErrorcustmessage(ifr, "Kindly Enter Recommended LoanAmount to Proceed Loan Account Creation");
            }
            BudgetDisbursementScreen bds = new BudgetDisbursementScreen();
            //   CBS_LoanAccountCreation CB1 = new CBS_LoanAccountCreation();
            String LoanAccountNumber = bds.getloanaccountcreation(ifr, ProductType);
            if (LoanAccountNumber.contains("ERROR")) {
                return pcm.returnErrorcustmessage(ifr, LoanAccountNumber);
            } else if (!LoanAccountNumber.isEmpty()) {
                Log.consoleLog(ifr, "LoanAccountNumber==>" + LoanAccountNumber);
                String Product = pcm.getProductCode(ifr);
                Log.consoleLog(ifr, "disbursement maker::product::" + Product);
                String ProductName = "";
                String ProductNameQuery = "SELECT ProductName FROM LOS_M_Product WHERE ProductCode='" + Product + "'";
                List<List<String>> ProductNameList = cf.mExecuteQuery(ifr, ProductNameQuery, "Get Product Query ");
                if (!ProductNameList.isEmpty()) {
                    ProductName = ProductNameList.get(0).get(0);
                }
                String loanCreatedDate = "select ACCOUNT_CREATEDDATE from los_trn_loandetails where pid='" + ProcessInstanceId + "'";
                List<List<String>> loanCreated = cf.mExecuteQuery(ifr, loanCreatedDate, "created date query");
                String CreatedDate = "";
                if (!loanCreated.isEmpty()) {
                    CreatedDate = loanCreated.get(0).get(0);
                }
                SimpleDateFormat CreatedDateformat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
                SimpleDateFormat TargetDate = new SimpleDateFormat("dd/MM/yyyy");
                Date dateDD = CreatedDateformat.parse(CreatedDate);
                Log.consoleLog(ifr, "CreatedDate before formatted :" + dateDD);
                CreatedDate = TargetDate.format(dateDD);
                Log.consoleLog(ifr, "CreatedDate formatted :" + CreatedDate);

                JSONObject obj = new JSONObject();
                obj.put("Loan Account No", LoanAccountNumber);
                obj.put("Product", ProductName);
                obj.put("Account Opening Date", CreatedDate);

                arr.add(obj);
                Log.consoleLog(ifr, "Json Arr" + arr);

                ifr.addDataToGrid("ALV_LOAN_ACC_CREATION", arr, true);

                ifr.setStyle("BTN_LoanCreation", "disable", "true");
                ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "0", true);
                ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "1", true);
                ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "2", true);
                DisbursementDataToBeneficiaryGrid(ifr);
            }
            // 
            if (LoanAccountNumber.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorHold(ifr);
            } else {

                return RLOS_Constants.SUCCESS;

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mDisbursementCheckerAPICallCB");
            return RLOS_Constants.ERROR;
        }

    }

    public String mPostSanctionNeslBudget(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mPostSanctionNeslBudget");

        try {
            String decision = ifr.getValue("DecisionValue").toString();
            Log.consoleLog(ifr, "decision::" + decision);
            if (decision.equalsIgnoreCase("S")) { //Post Sanction submit

                BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
                String returnMessageFromDocGen = bpcc.mGenerateDoc(ifr);
                if (returnMessageFromDocGen.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorHold(ifr);
                }
                EsignIntegrationChannel NESL = new EsignIntegrationChannel();
               // String returnMessage = NESL.redirectNESLRequest(ifr, "BUDGET", "eStamping");
//                Log.errorLog(ifr, "returnMessage:" + returnMessage);
//
//                if (returnMessage.contains(RLOS_Constants.ERROR)) {
//                    return pcm.returnErrorHold(ifr);
//                } else if (returnMessage.contains("showMessage")) {
//                    JSONParser jp = new JSONParser();
//                    JSONObject obj = (JSONObject) jp.parse(returnMessage);
//                    obj.put("eflag", "false");
//                    return obj.toString();
//                }
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception mPostSanctionNeslBudget : " + e);
        }
        return RLOS_Constants.ERROR;
    }

    // added by vandana
    public void monthlyExpensesMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "inside MonthlyExpensesMaker BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String ActivityName = ifr.getActivityName();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside mImplClickMonthlyExpensesMaker Budget ");
            String QueSampl = "QA_FI_PI_MEXPENSE_CustomerType,QA_FI_PI_MEXPENSE_ExpenseSource,QA_FI_PI_MEXPENSE_Description,QA_FI_PI_MEXPENSE_NetAmount";

            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlvisiblity(ifr, QueSampl);
                ifr.setStyle("QA_FI_PI_MEXPENSE_CustomerType", "disable", "false");
                ifr.setStyle("QA_FI_PI_MEXPENSE_ExpenseSource", "disable", "false");
                ifr.setStyle("QA_FI_PI_MEXPENSE_NetAmount", "disable", "false");
                ifr.setStyle("QA_FI_PI_MEXPENSE_Description", "disable", "false");
                ifr.setStyle("addAdvancedListviewrow_ALV_PL_MExpense", "disable", "false");
                ifr.setStyle("table602_button369", "visible", "false");
                ifr.setStyle("table602_button370", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PL_MExpense", "disable", "true");
                ifr.setStyle("table602_button369", "visible", "false");
                ifr.setStyle("table602_button370", "visible", "false");
                ifr.setStyle("delete_ALV_PL_MExpense", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PL_MExpense", "disable", "true");
                ifr.setStyle("table602_button369", "visible", "false");
                ifr.setStyle("table602_button370", "visible", "false");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " monthlyExpensesMaker Budget Reviewer WS");
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside monthlyExpensesMaker Budget Sanction ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSampl);
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PL_MExpense", "disable", "true");
                ifr.setStyle("table602_button369", "visible", "false");
                ifr.setStyle("table602_button370", "visible", "false");
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside monthlyExpensesMaker Budget Convenor ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Post Disbursement Doc Upload")) {
                Log.consoleLog(ifr, "BudgetBkOffCustomCode :: monthlyExpensesMaker :: inside PDD");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PL_MExpense", "disable", "true");
                ifr.setStyle("table602_button369", "visible", "false");
                ifr.setStyle("table602_button370", "visible", "false");
            }

            Log.consoleLog(ifr, " MonthlyExpensesMaker Budget Code End");
        }
    }

    public void netWorthMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "inside NetWorthMaker BudgetBkoffCustomCode:: ");
        String ActivityName = ifr.getActivityName();

        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "inside mImplClickNetWorthMaker Budget ");
            String visibleFields = "QNL_AL_NETWORTH_TotLiab,QNL_AL_NETWORTH_TotOutstanding";
            pcm.controlvisiblity(ifr, visibleFields);
            pcm.controlDisable(ifr, visibleFields);
            String nonVisibleFields = "QNL_AL_NETWORTH_TotAssetVal,QNL_AL_NETWORTH_ApplicantType";
            pcm.controlinvisiblity(ifr, nonVisibleFields);

            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " NetWorthMaker Budget Reviewer WS");
                pcm.controlDisable(ifr, visibleFields);
                pcm.controlvisiblity(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, " NetWorthMaker Budget Sanction WS");
                pcm.controlDisable(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, " NetWorthMaker Budget Convenor WS");
                pcm.controlDisable(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, " NetWorthMaker Budget Convenor WS");
                pcm.controlDisable(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, " NetWorthMaker Budget Convenor WS");
                pcm.controlDisable(ifr, visibleFields);
            }

            Log.consoleLog(ifr, " NetWorthMaker Budget Code End");
        }

    }

    public void finalEligibility(IFormReference ifr) {
        Log.consoleLog(ifr, "inside finalEligibility BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ActivityName = ifr.getActivityName();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadLoanAssesment Budget ");

            ifr.setStyle("saveAdvancedListviewchanges_ALV_FINAL_ELIGIBILITY", "disable", "false");
            String QueSampl = "QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy,QNL_LA_FINALELIGIBILITY_FinalNetTakeHome,QNL_LA_FINALELIGIBILITY_NetIncome,QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy,QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy,QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser,QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy,QNL_LA_FINALELIGIBILITY_LoanAmountrequested,QNL_LA_FINALELIGIBILITY_Eligibileloanamount,QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount,QNL_LA_FINALELIGIBILITY_ROI,QNL_LA_FINALELIGIBILITY_Tenure,QNL_LA_FINALELIGIBILITY_Requested_Tenure";
            pcm.controlvisiblity(ifr, QueSampl);
            String nonEditableFields = "QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser,QNL_LA_FINALELIGIBILITY_LoanAmountrequested,QNL_LA_FINALELIGIBILITY_Eligibileloanamount,QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy,QNL_LA_FINALELIGIBILITY_FinalNetTakeHome,QNL_LA_FINALELIGIBILITY_NetIncome,QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy,QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy,QNL_LA_FINALELIGIBILITY_AverageGrossIncome,QNL_LA_FINALELIGIBILITY_AverageDeductions,QNL_LA_FINALELIGIBILITY_Obligations,QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser,QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy,QNL_LA_FINALELIGIBILITY_ROI,QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT,QNL_LA_FINALELIGIBILITY_Requested_Tenure";
            pcm.controlDisable(ifr, nonEditableFields);
            //added by logaraj for finalsection field visibility on 26/06/2024
            String editable = "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount,QNL_LA_FINALELIGIBILITY_Tenure";
            pcm.controlEnable(ifr, editable);
            String inVisibilityFields = "QNL_LA_FINALELIGIBILITY_LoanAmountAsLtv,QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT,QNL_LA_FINALELIGIBILITY_EMIAPPROVEDLOANAMOUNT,QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy,QNL_LA_FINALELIGIBILITY_FinalNetTakeHome,QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount";
            pcm.controlinvisiblity(ifr, inVisibilityFields);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlinvisiblity(ifr, inVisibilityFields);
            }
            //ifr.setStyle("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", "disable", "true");
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlinvisiblity(ifr, inVisibilityFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {

                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlinvisiblity(ifr, inVisibilityFields);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                Log.consoleLog(ifr, "inside Disbursement Maker Budget finalEligibility controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {

                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlinvisiblity(ifr, inVisibilityFields);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                Log.consoleLog(ifr, "inside Disbursement Maker Budget finalEligibility controlDisable");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " finalEligibility Budget Reviewer WS");

                pcm.controlvisiblity(ifr, QueSampl);

                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");

                pcm.controlDisable(ifr, QueSampl);

                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");

                pcm.controlvisiblity(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                //ADDED BY Aravindh
                Log.consoleLog(ifr, "inside finalEligibility Budget Sanction ");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "false");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside finalEligibility Budget Convenor ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSampl);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy", "visible", "false");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_FinalNetTakeHome", "visible", "false");

            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside PostSanction Budget finalEligibility controlDisable");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "visible", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy", "visible", "false");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_FinalNetTakeHome", "visible", "false");

            }

            Log.consoleLog(ifr, " finalEligibility Budget Code End");
        }
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, " finalEligibility Vehicle Code Inside");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "false");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_LoanAmountAsLtv", "visible", "true");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_LoanAmountAsLtv", "disable", "true");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_Loanamountgrossincomepolicy", "visible", "false");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_FinalNetTakeHome", "visible", "false");
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside finalEligibility VL Sanction ");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
            }
            if (ActivityName.equalsIgnoreCase("Post Disbursement Doc Upload")) {
                Log.consoleLog(ifr, "inside finalEligibility VL Post Disbursement Doc Upload::" + ActivityName);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "disable", "true");
            }
            if (ActivityName.equalsIgnoreCase("PostSanction")) {
                Log.consoleLog(ifr, "inside finalEligibility VL PostSanction:::" + ActivityName);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_FINAL_ELIGIBILITY", "disable", "true");
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside finalEligibility VL Disbursement Maker::" + ActivityName);
                ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_FINAL_ELIGIBILITY", "disable", "true");
            }
            Log.consoleLog(ifr, " finalEligibility Vehicle Code End");
        }
    }

    public void inPrincipleEligibility(IFormReference ifr) {
        Log.consoleLog(ifr, "inside inPrincipleEligibility BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ActivityName = ifr.getActivityName();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "inside OnLoadLoanAssesment Budget ");
            String QueSampl = "QNL_LA_INPRINCIPLE_RequestedLoanAmount,QNL_LA_INPRINCIPLE_LoanamountasperIncome,QNL_LA_INPRINCIPLE_AverageGrossIncome,QNL_LA_INPRINCIPLE_AverageDeductions,QNL_LA_INPRINCIPLE_Obligations,QNL_LA_INPRINCIPLE_NetTakeHomeSalaryasperpolicy,QNL_LA_INPRINCIPLE_NetIncome,QNL_LA_INPRINCIPLE_Tenure,QNL_LA_INPRINCIPLE_ROI,QNL_LA_INPRINCIPLE_Loanamountasperpolicy,QNL_LA_INPRINCIPLE_InPrincipalloanamount,QNL_LA_INPRINCIPLE_InstalmentAmount";
            pcm.controlvisiblity(ifr, QueSampl);
            pcm.controlDisable(ifr, QueSampl);
            ifr.setStyle("QNL_LA_INPRINCIPLE_LoanAmountasperProductPolicy", "visible", "false");
            ifr.setStyle("QNL_LA_INPRINCIPLE_LoanAmountAsPerLtv", "visible", "false");
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " inPrincipleEligibility Budget Reviewer WS");
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
            }

            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility Budget Sanction ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility Budget Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside PostSanction Budget inPrincipleEligibility controlDisable");

            }
            Log.consoleLog(ifr, " inPrincipleEligibility Budget Code End");
            //Added by Aravindh on 09/07/2024
            //Commented by Aravindh on 09/07/2024
//            String requestedLoanAmt ="";
//            String requestedTenure ="";
//            String query = "SELECT REQLOANAMT FROM LOS_NL_PROPOSED_FACILITY WHERE PID ='"+ ProcessInstanceId + "'";
//            Log.consoleLog(ifr, "query requestedLoanAmt ::" + query);
//            List<List<String>> result = ifr.getDataFromDB(query);
//            if (!result.isEmpty()) {
//                requestedLoanAmt = result.get(0).get(0);
//                requestedTenure = result.get(0).get(0);
//                Log.consoleLog(ifr, "requestedLoanAmt :" + requestedLoanAmt);
//                Log.consoleLog(ifr, "requestedTenure :" + requestedTenure);
//            }
//            ifr.setValue("QNL_LA_INPRINCIPLE_RequestedLoanAmount", requestedLoanAmt);
//            ifr.setValue("QNL_LA_INPRINCIPLE_Tenure", requestedTenure);
//            
        }
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadLoanAssesment VL ");
            String QueSampl = "QNL_LA_INPRINCIPLE_RequestedLoanAmount,QNL_LA_INPRINCIPLE_LoanAmountAsPerLtv,QNL_LA_INPRINCIPLE_AverageGrossIncome,QNL_LA_INPRINCIPLE_AverageDeductions,QNL_LA_INPRINCIPLE_Obligations,QNL_LA_INPRINCIPLE_NetTakeHomeSalaryasperpolicy,QNL_LA_INPRINCIPLE_NetIncome,QNL_LA_INPRINCIPLE_Tenure,QNL_LA_INPRINCIPLE_ROI,QNL_LA_INPRINCIPLE_Loanamountasperpolicy,QNL_LA_INPRINCIPLE_InPrincipalloanamount,QNL_LA_INPRINCIPLE_InstalmentAmount";
            pcm.controlvisiblity(ifr, QueSampl);
            pcm.controlDisable(ifr, QueSampl);
            ifr.setStyle("QNL_LA_INPRINCIPLE_LoanAmountasperProductPolicy", "visible", "false");
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker VL inPrincipleEligibility controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker VL inPrincipleEligibility controlDisable");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " inPrincipleEligibility VL Reviewer WS");
                pcm.controlDisable(ifr, QueSampl);
            }

            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility VL Sanction ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility VL Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside PostSanction VL inPrincipleEligibility controlDisable");

            }
            ifr.setStyle("QNL_LA_INPRINCIPLE_LoanamountasperIncome", "visible", "false");
            Log.consoleLog(ifr, " inPrincipleEligibility VL Code End");
            //Added by Aravindh on 09/07/2024
            //Commented by Aravindh on 09/07/2024
//            String requestedLoanAmt ="";
//            String requestedTenure ="";
//            String query = "SELECT REQLOANAMT FROM LOS_NL_PROPOSED_FACILITY WHERE PID ='"+ ProcessInstanceId + "'";
//            Log.consoleLog(ifr, "query requestedLoanAmt ::" + query);
//            List<List<String>> result = ifr.getDataFromDB(query);
//            if (!result.isEmpty()) {
//                requestedLoanAmt = result.get(0).get(0);
//                requestedTenure = result.get(0).get(0);
//                Log.consoleLog(ifr, "requestedLoanAmt :" + requestedLoanAmt);
//                Log.consoleLog(ifr, "requestedTenure :" + requestedTenure);
//            }
//            ifr.setValue("QNL_LA_INPRINCIPLE_RequestedLoanAmount", requestedLoanAmt);
//            ifr.setValue("QNL_LA_INPRINCIPLE_Tenure", requestedTenure);
//            
        }
    }

    public void knockoffMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "inside knockoffMaker BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String ActivityName = ifr.getActivityName();

        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadKnockoffMaker Budget ");
            String QueSampl = "QNL_K_KNOCKOFFRULES_PartyType,QNL_K_KNOCKOFFRULES_RuleName,QNL_K_KNOCKOFFRULES_Output";
            pcm.controlvisiblity(ifr, QueSampl);
            // pcm.controlDisable(ifr, QueSampl);
            if (ActivityName.equalsIgnoreCase("Branch Checker")) {
                Log.consoleLog(ifr, " knockoffMaker Budget Reviewer WorkStep");
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " knockoffMaker Budget Reviewer WorkStep");
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside knockoffMaker BudgetBkoffCustomCode::  Sanction");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside knockoffMaker BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            Log.consoleLog(ifr, " knockoffMaker Budget Code End");
        }

    }

    public void assetMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "inside assetMaker BudgetBkoffCustomCode::");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside mImplClickAssetMaker Budget ");
            String QueSampl = "QNL_AL_ASSET_DET_ApplicantType,QNL_AL_ASSET_DET_AssetType,QNL_AL_ASSET_DET_AssetSubType,QNL_AL_ASSET_DET_AccBalance_Value,QNL_AL_ASSET_DET_Encumbered";
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlvisiblity(ifr, QueSampl);
                ifr.setStyle("QNL_AL_ASSET_DET_ApplicantType", "disable", "false");
                ifr.setStyle("QNL_AL_ASSET_DET_AssetType", "disable", "false");
                ifr.setStyle("QNL_AL_ASSET_DET_AssetSubType", "disable", "false");
                ifr.setStyle("QNL_AL_ASSET_DET_AccBalance_Value", "disable", "false");
                ifr.setStyle("QNL_AL_ASSET_DET_Encumbered", "disable", "false");
                ifr.setStyle("table599_button367", "visible", "false");
                ifr.setStyle("table599_button368", "visible", "false");
                ifr.setStyle("addAdvancedListviewrow_ALV_AL_ASSET_DET", "disable", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget inPrincipleEligibility controlDisable");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " inPrincipleEligibility Budget Reviewer WorkStep");
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility BudgetBkoffCustomCode::  Sanction");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside inPrincipleEligibility BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, QueSampl);
                Log.consoleLog(ifr, "inside PostSanction Budget inPrincipleEligibility controlDisable");
            }
            Log.consoleLog(ifr, " assetMaker Budget Code End");
        }

    }

    public void financialInfoSummarySectionMaker(IFormReference ifr) {
        Log.consoleLog(ifr, "inside FinancialInfoSummarySectionMaker BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String ActivityName = ifr.getActivityName();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnChangeFinancialInfoSummarySectionMaker Budget/Pension ");
            String QueSampl = "BTN_FinancialInfo_Calculate,Q_SUMMARY_total_asset,Q_SUMMARY_total_cashinFlow,Q_SUMMARY_total_consideredLiability,Q_SUMMARY_toal_considered_income,Q_SUMMARY_total_liability";
            pcm.controlvisiblity(ifr, QueSampl);
            String QueSamp2 = "Q_SUMMARY_total_asset,Q_SUMMARY_total_cashinFlow,Q_SUMMARY_total_consideredLiability,Q_SUMMARY_toal_considered_income,Q_SUMMARY_total_liability";
            pcm.controlDisable(ifr, QueSamp2);
            String QueSamp3 = "Q_SUMMARY_tdsr,Q_SUMMARY_total_cashOutFlow";
            ifr.setStyle("Q_SUMMARY_total_cashOutFlow", "visible", "false");
            ifr.setStyle("Q_SUMMARY_tdsr", "visible", "false");
            String QueSamp4 = "BTN_FinancialInfo_Calculate";
            pcm.controlinvisiblity(ifr, QueSamp3);
            if (ActivityName.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Reviewer ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                pcm.controlEnable(ifr, QueSamp4);
            }
            if (ActivityName.equalsIgnoreCase("Branch Checker")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Reviewer ");
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Reviewer ");
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");

            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Sanction");
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Convenor ");
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");

            }
            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Reviewer ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");

            }
            if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside financialInfoSummarySectionMaker Budget:Reviewer ");
                pcm.controlvisiblity(ifr, QueSampl);
                pcm.controlDisable(ifr, QueSamp2);
                pcm.controlinvisiblity(ifr, QueSamp3);
                ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");

            }
            Log.consoleLog(ifr, " FinancialInfoSummarySectionMaker Budget Code End");
        }

    }

    public void riskScoreRatig(IFormReference ifr) {
        Log.consoleLog(ifr, "inside RiskScoreRatigMaker BudgetBkoffCustomCode::");
        String ActivityName = ifr.getActivityName();

        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadRiskScoreRatigMaker Budget ");
            String QueSampl = "QNL_R_SCORERATING_PartyType,QNL_R_SCORERATING_CanaraRetailGrade,QNL_R_SCORERATING_RiskScore_Rating,QNL_R_SCORERATING_TotalMarkesSecured,QNL_R_SCORERATING_InPercentage";
            pcm.controlvisiblity(ifr, QueSampl);
            pcm.controlDisable(ifr, QueSampl);
            String QueSamp2 = "QNL_R_SCORERATING_Parameter,QNL_R_SCORERATING_MaximumMarks,QNL_R_SCORERATING_MarksReceived";
            pcm.controlinvisiblity(ifr, QueSamp2);
            JSONObject message = new JSONObject();

            String sendMessage = "";
            String finalscore = ifr.getValue("QNL_R_SCORERATING_CanaraRetailGrade").toString();
            JSONObject result = cf.executeBRMSRule(ifr, "TotalGradeAndDecision", finalscore);
            Log.consoleLog(ifr, "Result of Scorecard BRMS = " + result);

            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) { //Fetching From JSON
                Log.consoleLog(ifr, "inside if");
                ifr.setValue("QNL_R_SCORERATING_RiskScore_Rating", cf.getJsonValue(result, "sc_out_totalgrade"));
                //  ifr.setValue("QL_RISK_RATING_Decision", cf.getJsonValue(result, "sc_out_lendingdecision"));
                sendMessage = "Scorecard Calculated Successfully!";
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
            } else {
                sendMessage = AcceleratorConstants.TRYCATCHERRORBRMS;
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, " riskScoreRatig Budget Reviewer WS");
                pcm.controlDisable(ifr, QueSampl);
            }

            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside riskScoreRatig BudgetBkoffCustomCode::  Sanction");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside riskScoreRatig BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside riskScoreRatig BudgetBkoffCustomCode:: DEviation ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Post Disbursement Doc Upload")) {
                Log.consoleLog(ifr, "inside BudgetBkoffCustomCode :: riskScoreRatig ::  Post Disbursement Doc Upload");
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlinvisiblity(ifr, QueSamp2);
            }
            Log.consoleLog(ifr, " OnLoadRiskScoreRatigMaker Budget Code End");
        }

    }

    public void partyDetailsSection(IFormReference ifr) {
        Log.consoleLog(ifr, "inside partyDetailsSection BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        //invisibing guarantor list

        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside fromLoadMaker Budget / Pension");
            String visibleSectionsGrids = "QNL_BASIC_INFO_CL_BASIC_INFO_I_RelationshipBank,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName"
                    + "QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification"
                    + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO.CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents"
                    + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonEditableFields = ",QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality"
                    + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste";
            pcm.controlDisable(ifr, nonEditableFields);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visibleSectionsGrids);
                ifr.setStyle("E-CTRID_PD_FETCHEXTCUST", "visible", "false");
                ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
                ifr.setStyle("BTN_Dedupe_Click", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, visibleSectionsGrids);
                pcm.controlvisiblity(ifr, visibleSectionsGrids);
                Log.consoleLog(ifr, "inside Disbursement Maker Budge/Pensiont partyDetailsSection controlDisable");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, visibleSectionsGrids);
                pcm.controlvisiblity(ifr, visibleSectionsGrids);
                Log.consoleLog(ifr, "inside Disbursement Maker Budget/Pension partyDetailsSection controlDisable");
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside partyDetailsSection Budget/Pension:Reviewer ");
                pcm.controlDisable(ifr, visibleSectionsGrids);
            }

            //added by logaraj
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                pcm.controlDisable(ifr, visibleSectionsGrids);
                Log.consoleLog(ifr, "inside partyDetailsSection Budget/Pension Code End:Sanction");
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside partyDetailsSection Budget/Pension: Convenor");
                pcm.controlDisable(ifr, visibleSectionsGrids);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside partyDetailsSection Budget/Pension: Deviation");
                pcm.controlDisable(ifr, visibleSectionsGrids);
            }

            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, visibleSectionsGrids);
                Log.consoleLog(ifr, "inside PostSanction Budget/Pension partyDetailsSection controlDisable");
            }

            Log.consoleLog(ifr, " partyDetailsSection Budget/Pension Code End");
        }

    }

    public void outwardDocument(IFormReference ifr) {
        Log.consoleLog(ifr, "inside OutwardDocumentMaker BudgetBkoffCustomCode::");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ActivityName = ifr.getActivityName();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadOutwardDocumentMaker Budget ");
            String QueSampl = "QNL_GENERATE_DOCUMENT_DocumentName,QNL_GENERATE_DOCUMENT_GeneratedDate,QNL_GENERATE_DOCUMENT_GeneratedBy";
            pcm.controlvisiblity(ifr, QueSampl);
            String nonEditableFields = "QNL_GENERATE_DOCUMENT_DocumentName,QNL_GENERATE_DOCUMENT_GeneratedDate,QNL_GENERATE_DOCUMENT_GeneratedBy";
            pcm.controlDisable(ifr, nonEditableFields);
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside outwardDocument BudgetBkoffCustomCode::  Reviewer");
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside outwardDocument BudgetBkoffCustomCode::  Sanction");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside outwardDocument BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside outwardDocument BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, QueSampl);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);

            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlvisiblity(ifr, QueSampl);

            }
            Log.consoleLog(ifr, " OutwardDocumentMaker Budget Code End");
        }

    }

    public void OnLoadFinancialInfoLiabilities(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilitiesMaker Budget ");

            String visibleLiabilitiesFields = "QNL_AL_LIAB_VAL_ApplicantType,QNL_AL_LIAB_VAL_ConsiderForEligibility,QNL_AL_LIAB_VAL_LoanType,QNL_AL_LIAB_VAL_Loan_LiabAmt,QNL_AL_LIAB_VAL_Bank,QNL_AL_LIAB_VAL_loanStartDate,QNL_AL_LIAB_VAL_Loan_LiabOut,QNL_AL_LIAB_VAL_Overdue,QNL_AL_LIAB_VAL_EMIAmt";
            pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
            //   String HideLiabilitiesFields = "QNL_AL_LIAB_VAL_EMIAmt";
            //    pcm.controlinvisiblity(ifr, HideLiabilitiesFields);
            //pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visibleLiabilitiesFields);
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities Branch Checker:: " + loan_selected);

            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, visibleLiabilitiesFields);
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);

            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, visibleLiabilitiesFields);
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                pcm.controlDisable(ifr, visibleLiabilitiesFields);
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);

            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {

                pcm.controlDisable(ifr, visibleLiabilitiesFields);
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);

            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);

                pcm.controlDisable(ifr, visibleLiabilitiesFields);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                pcm.controlvisiblity(ifr, visibleLiabilitiesFields);
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities:: " + loan_selected + "," + ActivityName);

                pcm.controlDisable(ifr, visibleLiabilitiesFields);
            }
            Log.consoleLog(ifr, " OnLoadFinancialInfoLiabilitiesMaker Budget Code End");
        }
    }

    public void OnLoadFinancialInfoIncome(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String ActivityName = ifr.getActivityName();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadFinancialInfoIncome Budget ");
            ifr.setStyle("ALV_PL_MIncome_Calc", "visible", "true");
            String visibleIncomeGrids = "QA_FI_PI_MINCOME_CustomerType,QA_FI_PI_MINCOME_ConsiderEligibilty,QA_FI_PI_MINCOME_IncSource,QA_FI_PI_MINCOME_GrossAmt,QA_FI_PI_MINCOME_NetAmount,QA_FI_PI_MINCOME_Deduction,QA_FI_PI_MINCOME_IncomeType,QA_FI_PI_MINCOME_Description,QA_FI_PI_MINCOME_MonthInWords,QA_FI_PI_MINCOME_DeductionMonthly,QA_FI_PI_MINCOME_GrossAmt,QA_FI_PI_MINCOME_NetAmount";
            pcm.controlvisiblity(ifr, visibleIncomeGrids);
            pcm.controlDisable(ifr, visibleIncomeGrids);
            String InvisibleIncomeGrids = "QA_FI_PI_MINCOME_occCombo,QA_FI_PI_MINCOME_Form16orITR,QA_FI_PI_MINCOME_FinancialYearLatest,QA_FI_PI_MINCOME_OtherDeduction,QA_FI_PI_MINCOME_OtherDeductionDescription,QA_FI_PI_MINCOME_NetAnnualAmt,QA_FI_PI_MINCOME_MonthInWords,QA_FI_PI_MINCOME_occTxt,QA_FI_PI_MINCOME_AssessmentYear,QA_FI_PI_MINCOME_GrossAvgAmt,QA_FI_PI_MINCOME_Description,ALV_PL_MIncome_Calc,"
                    + "QA_FI_PI_MINCOME_srcIncCombo,QA_FI_PI_MINCOME_occTxt,QA_FI_PI_MINCOME_FinancialYearlatest,Income_BTN_SaveAndClose,Income_BTN_SaveAndNext";
            pcm.controlinvisiblity(ifr, InvisibleIncomeGrids);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                ifr.setStyle("QA_FI_PI_MINCOME_ConsiderEligibilty", "disable", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visibleIncomeGrids);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, visibleIncomeGrids);
                pcm.controlinvisiblity(ifr, InvisibleIncomeGrids);
                pcm.controlvisiblity(ifr, visibleIncomeGrids);
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PL_MIncome", "disable", "true");
            }

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, visibleIncomeGrids);
                pcm.controlvisiblity(ifr, visibleIncomeGrids);
            }

            if (ActivityName.equalsIgnoreCase("PostSanction")) {
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities BudgetBkoffCustomCode::  PostSanction");
                pcm.controlDisable(ifr, visibleIncomeGrids);
                pcm.controlinvisiblity(ifr, InvisibleIncomeGrids);
            }
            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilities BudgetBkoffCustomCode::  Reviewer" + loan_selected);
                pcm.controlDisable(ifr, visibleIncomeGrids);
                pcm.controlinvisiblity(ifr, InvisibleIncomeGrids);
                pcm.controlvisiblity(ifr, visibleIncomeGrids);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoIncome BudgetBkoffCustomCode::  Sanction");
                pcm.controlinvisiblity(ifr, InvisibleIncomeGrids);
                pcm.controlDisable(ifr, visibleIncomeGrids);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoIncome BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, visibleIncomeGrids);
            }
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside OnLoadFinancialInfoIncome BudgetBkoffCustomCode:: Deviation ");

                pcm.controlDisable(ifr, visibleIncomeGrids);
            }

            Log.consoleLog(ifr, " OnLoadFinancialInfoIncome Budget Code End");
        }
    }

    public void OnChangePersonalFinancialInfomation(IFormReference ifr) {
        Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation BudgetBkoffCustomCode::");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation Budget ");
            ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");

            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation BudgetBkoffCustomCode:: Sanction ");
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation BudgetBkoffCustomCode:: Convenor ");
//                ifr.setColumnVisible("ALV_AL_NETWORTH", "0", false);
//                ifr.setColumnVisible("ALV_AL_NETWORTH", "1", false);
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation BudgetBkoffCustomCode:: Reviewer ");
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation BudgetBkoffCustomCode:: Deviation ");
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            Log.consoleLog(ifr, " OnChangePersonalFinancialInfomation Budget Code End");
        }

    }

    public void OnchangeScoreRating(IFormReference ifr) {
        Log.consoleLog(ifr, "inside OnchangeScoreRating BudgetBkoffCustomCode::");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnchangeScoreRating Budget ");
//            String QueSampl = "QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank,BTN_FetchScoreRating";
//            pcm.controlinvisiblity(ifr, QueSampl);
            ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
            ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
            ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursment Maker")) {
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursment Checker")) {
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Reviewer")) {
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside OnchangeScoreRating BudgetBkoffCustomCode:: Convenor ");
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside OnchangeScoreRating BudgetBkoffCustomCode:: Sanction ");
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("postSanction")) {
                Log.consoleLog(ifr, "inside OnchangeScoreRating BudgetBkoffCustomCode:: Sanction ");
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside OnchangeScoreRating BudgetBkoffCustomCode:: Sanction ");
                ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
                ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
                ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            }
            Log.consoleLog(ifr, " OnchangeScoreRating Budget Code End");
        }
        if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            Log.consoleLog(ifr, "inside OnchangeScoreRating VL");
            ifr.setStyle("QL_RISK_RATING_RiskScore", "visible", "false");
            ifr.setStyle("QL_RISK_RATING_Rank", "visible", "false");
            ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
            ifr.setStyle("QNL_R_SCORERATING_Parameter", "visible", "false");
            ifr.setStyle("QNL_R_SCORERATING_MaximumMarks", "visible", "false");
            ifr.setStyle("QNL_R_SCORERATING_MarksReceived", "visible", "false");

            ifr.setStyle("QNL_R_SCORERATING_PartyType", "disable", "true");
            ifr.setStyle("QNL_R_SCORERATING_CanaraRetailGrade", "disable", "true");
            ifr.setStyle("QNL_R_SCORERATING_RiskScore_Rating", "disable", "true");
            ifr.setStyle("QNL_R_SCORERATING_TotalMarkesSecured", "disable", "true");
            ifr.setStyle("QNL_R_SCORERATING_InPercentage", "disable", "true");
            Log.consoleLog(ifr, " OnchangeScoreRating VL Code End");
        }
    }

    public void formLoadPostSanctionVisibility(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTNESL").replaceAll("#PID#", ProcessInstanceId);
        Log.consoleLog(ifr, "NESLQuery query::" + IndexQuery);
        List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
        String count = dataResult.get(0).get(0);
        Log.consoleLog(ifr, "count::" + count);
        if (Integer.parseInt(count) > 0) {
            Log.consoleLog(ifr, "inside count...");
            //String docName = ifr.getValue("QNL_DOC_ESIGN_STATUS_DocumentName").toString();
            String EsignStatusQuery = ConfProperty.getQueryScript("getESignStatusQuery").replaceAll("#PID#", ProcessInstanceId);
            Log.consoleLog(ifr, "EsignStatusQuery ::" + EsignStatusQuery);
            List<List<String>> EsignResultResult = ifr.getDataFromDB(EsignStatusQuery);
            String EsignStatus = EsignResultResult.get(0).get(0);
            Log.consoleLog(ifr, "count::" + count);
            Log.consoleLog(ifr, "EsignStatus --> " + EsignStatus);
            if (EsignStatus.equalsIgnoreCase("Initiated")) {
                ifr.setValue("E-Sign_initiated", "Yes");
                ifr.setStyle("E-Sign_initiated", "disable", "false");
                ifr.setStyle("ESign_Initaite", "visible", "true");
                ifr.setStyle("ESign_Initaite", "disable", "true");
                ifr.setStyle("ALV_ESIGN", "disable", "true");
            } else {
                ifr.setValue("E-Sign_initiated", "No");
                ifr.setStyle("E-Sign_initiated", "disable", "false");
                ifr.setStyle("ESign_Initaite", "visible", "false");
                ifr.setStyle("ESign_Initaite", "disable", "true");
                ifr.setStyle("ALV_ESIGN", "disable", "true");
            }
        }
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            ifr.setTabStyle("tab1", "8", "visible", "false");//Collateral
            //ifr.setTabStyle("tab1", "14", "visible", "false");//Disbursement
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            //ifr.setTabStyle("tab1", "127", "visible", "true");
            //ifr.setTabStyle("tab1", "15", "visible", "false");// commented by aravindh - Risk Tab need on basis of sheet Change
            //ifr.setTabStyle("tab1", "16", "visible", "false");

            String visibleSectionsGrids = "LV_OCCUPATION_INFO,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation,F_ESIGN_ID";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            pcm.controlDisable(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "textbox6939,F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign ,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            Log.consoleLog(ifr, " DisbursementMaker Budget Code End");
            
            String InVisibleBtn = "CTRID_PD_FETCHEXTCUST,BTN_Dedupe_Click,CTRID_PD_RESETDET,QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank,BTN_FetchScoreRating";
            pcm.controlinvisiblity(ifr, InVisibleBtn);
            
            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);

            //Tab Visibility for formLoadPostSanctionVisibility
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) { //added by sharon
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);
            Log.consoleLog(ifr, "inside formLoadPostSanctionVisibility VL");
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            ifr.setTabStyle("tab1", "8", "visible", "true");//Collateral
            ifr.setTabStyle("tab1", "14", "visible", "false");//Disbursement
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            ifr.setTabStyle("tab1", "7", "visible", "false");//Eligibility
            //ifr.setTabStyle("tab1", "127", "visible", "true");

            String visibleSectionsGrids = "F_CoborrowerEligibility_Sec,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation,F_ESIGN_ID";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_Mortgage,F_CollateralSummary,textbox6939,F_E-Sign,F_Deviation,F_Risk_Rating,F_Bureau_NegativeDetails,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign ,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            Log.consoleLog(ifr, " formLoadPostSanctionVisibility VL Code");

            String InVisibleBtn = "QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank,CTRID_PD_FETCHEXTCUST,BTN_Dedupe_Click,CTRID_PD_RESETDET,Btn_Refresh,BTN_FetchScoreRating,DD_CB_BUREAU_ID_label,DD_CB_BUREAU_ID";
            pcm.controlinvisiblity(ifr, InVisibleBtn);
            ifr.setStyle("BTN_FinancialInfo_Calculate", "disable", "true");

            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);

            String[] FieldVisibleFalse = new String[]{"QNL_LOS_COLLATERAL_VEHICLES_COLOR", "QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURINGDATE", "QNL_LOS_COLLATERAL_VEHICLES_REGISTRATIONNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_ENGINENUMBER", "QNL_LOS_COLLATERAL_VEHICLES_CHASISNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_INSURANCECOMPANYNAME", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEPOLICYNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_AGEOFVEHICLE", "QNL_LOS_COLLATERAL_VEHICLES_ASSETOWNER", "QNL_LOS_COLLATERAL_VEHICLES_DEALERCODE",
                "QNL_LOS_COLLATERAL_VEHICLES_DEALERADDRESS",
                "QNL_LOS_COLLATERAL_VEHICLES_EX_SHOWROOMPRICE", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEAMOUNT",
                "QNL_LOS_COLLATERAL_VEHICLES_LTT_REGISTRATIONCHARGES", "QNL_LOS_COLLATERAL_VEHICLES_OTHERCHARGES",
                "QNL_LOS_COLLATERAL_VEHICLES_ROADTAX", "QNL_LOS_COLLATERAL_VEHICLES_ONROADPRICE", "QNL_LOS_COLLATERAL_VEHICLES_MARGIN",
                "QNL_LOS_COLLATERAL_VEHICLES_PERMISSIBLEVALUE", "VL_Fetch_Margin", "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONBY",
                "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONON", "Vehicle_Title", "QNL_LOS_COLLATERAL_VEHICLES_SECURITYTYPE", "VL_Transmission_Type"};
            for (int i = 0; i < FieldVisibleFalse.length; i++) {
                ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                Log.consoleLog(ifr, "FieldVisibleFalse[i]" + FieldVisibleFalse[i]);
            }
            String collateralDisable = "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country,QNL_BASIC_INFO_CNL_CUST_ADDRESS_State,QNL_LOS_COLLATERAL_VEHICLES_FUELTYPE,QNL_LOS_COLLATERAL_VEHICLES_VEHICLETYPE,QNL_LOS_COLLATERAL_VEHICLES_ASSETMANUFACTURER,QNL_LOS_COLLATERAL_VEHICLES_ASSETMODEL,QNL_LOS_COLLATERAL_VEHICLES_VEHICLEVARIANT,QNL_LOS_COLLATERAL_VEHICLES_VEHICLECOST,QNL_LOS_COLLATERAL_VEHICLES_DOWNPAYMENTAMT,QNL_LOS_COLLATERAL_VEHICLES_CATEGORY,QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURERYEAR,QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME,QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE";
            pcm.controlDisable(ifr, collateralDisable);
            ifr.setStyle("ALV_VEHICLE_COLLATERAL", "disable", "true");
            ifr.setColumnVisible("ALV_FINAL_ELIGIBILITY", "5", false);
            ifr.setStyle("QNL_AL_LIAB_VAL_ConsiderForEligibility", "disable", "true");
            ifr.setStyle("QNL_AL_LIAB_VAL_EMIAmt", "disable", "true");
            ifr.setStyle("QA_FI_PI_MINCOME_AssessmentYear", "disable", "true");
            ifr.setStyle("QA_FI_PI_MINCOME_GrossAvgAmt", "disable", "true");
            ifr.setStyle("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "disable", "true");
            ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "8", false);

            //Tab Visibility for formLoadPostSanctionVisibility VL
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            ifr.setTabStyle("tab1", "8", "visible", "false");//Collateral
            ifr.setTabStyle("tab1", "14", "visible", "false");//Disbursement
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            ifr.setTabStyle("tab1", "15", "visible", "false");//Risk
            ifr.setTabStyle("tab1", "16", "visible", "false");//E-signTab
            //ifr.setTabStyle("tab1", "127", "visible", "true");
            ifr.setStyle("CTRID_PD_FETCHEXTCUST", "visible", "false");
            ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
            ifr.setStyle("BTN_Dedupe_Click", "visible", "false");
            String visibleSectionsGrids = "textbox6939,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,F_ESIGN_ID";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_CoborrowerEligibility_Sec,F_Deviation,ALV_Deviations,F_Deviation,F_ESIGN_ID,ALV_SCORERATING";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            Log.consoleLog(ifr, " DisbursementMaker Pension Code End");

            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);

            //Tab Visibility for DisbursementMaker
        }
    }

    //modified by ishwarya on 03-07-2024
    // modified by vandana 23-07-2024
    public String CheckCibilandExperianBudgetBorrower(IFormReference ifr, String applicanttype) {
        JSONObject message = new JSONObject();
        String decision = "";
        try {
            Log.consoleLog(ifr, "Inside Budget CheckCibilandExperianBudget:B ");
            String mobnoB = "";
            String custidB = "";
            String Aadhar = "";
            String cust_id = "";
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            /*String UpdateProdCodeLCEXT_Query = ConfProperty.getQueryScript("UPDATEPRODUCTCODELC").replaceAll("#PID#", ProcessInsanceId);
            Log.consoleLog(ifr, "UpdateProdCodeLCEXT_Query list :" + UpdateProdCodeLCEXT_Query);
            ifr.saveDataInDB(UpdateProdCodeLCEXT_Query);*/
            CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
            HashMap<String, String> map = new HashMap<>();
            String MobileData_Query = ConfProperty.getQueryScript("getMobileNumberQueryL").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#applicanttype#", applicanttype);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobnoB = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobnoB);
            }
            if (mobnoB.startsWith("91")) {
                // Remove the country code
                mobnoB = mobnoB.substring(2);
            }
            Log.consoleLog(ifr, "MobileNo ==>" + mobnoB);

            custidB = pcm.getCustomerIDCB(ifr, applicanttype);
            Log.consoleLog(ifr, "CustomerId==>" + custidB);
            //Log.consoleLog(ifr, "CUSTOMERID" + custidB);

            Log.consoleLog(ifr, "Budget loan map value:::==>" + map);
            map.clear();
            map.put("MobileNumber", mobnoB);
            map.put("CustomerId", custidB);
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

            String cb = bpcc.mCallBureau(ifr, "CB", Aadhar, applicanttype);
            if (cb.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            Log.consoleLog(ifr, "before mCheckCIBILScoreknockOff");
            //     objbcr.checkCICScore(ifr, productCode, subProductCode, "B");
            decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", applicanttype);
            Log.consoleLog(ifr, "decision1/B::" + decision);
            if (decision.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            } else if (decision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "CIBIL Passed Successfully for Borrower:::");
                String EX = bpcc.mCallBureau(ifr, "EX", Aadhar, applicanttype);
                if (EX.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "EX", applicanttype);
                Log.consoleLog(ifr, "decision2/EX::" + decision);
                if (decision.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "inside bureau grid data saving");
                    String bueroTableQuery = ConfProperty.getQueryScript("getbueroTableQueryAt").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#applicanttype#", applicanttype);
                    Log.consoleLog(ifr, "bueroTableQuery ::" + bueroTableQuery);
                    List<List<String>> bueroTableData = ifr.getDataFromDB(bueroTableQuery);
                    String dataSaveBueroCheckGridQuery = "";
                    //getbueroTableQuery=select PID from LOS_NL_CB_Details where PID='#ProcessInstanceId#' and APPLICANTCODE='#applicanttype#'
                    if (bueroTableData.isEmpty()) {
                        Log.consoleLog(ifr, "bueroTableData is empty::::" + bueroTableData);
                        dataSaveBueroCheckGridQuery = ConfProperty.getQueryScript("getdataSaveBueroCheckGridQuery").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#applicanttype#", applicanttype);
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
                            String borrowerQuery = ConfProperty.getQueryScript("getinsertionOrderID").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#applicanttype#", applicanttype);
                            List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
                            if (!borrowerQueryData.isEmpty()) {
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

                            } else {
                                ifr.setStyle("Btn_GenerateBureau", "disable", "true");

                            }

                        }
                    }
                } else {
                    Log.consoleLog(ifr, "Experian Failed:::");
                    message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }

            } else {
                Log.consoleLog(ifr, "Cibil Failed:::");
                message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                message.put("eflag", "false");
                return message.toString();
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in CheckCibilandExperian" + e);
            return pcm.returnError(ifr);
        }
        return decision;
    }

    //========================Modified by Ahmed on 28-06-2024========================
    //modified by ishwarya on 03-07-2024
    // modified by vandana 23-07-2024
    public String KnockoffCheckLCCB(IFormReference ifr, String applicantType) {
        JSONObject message = new JSONObject();
        String knockoffDecision = "";
        try {
            Log.consoleLog(ifr, "inside try block KnockoffCheckLCCB:::");
            String processInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String CustomerId = pcm.getCustomerIDCB(ifr, applicantType);
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
            String f_key = bpcc.Fkey(ifr, applicantType);
            Log.consoleLog(ifr, "MobileNo==>" + f_key);
            String MobileNo = "";
            String Mobileno = " SELECT MOBILENO FROM LOS_L_BASIC_INFO_I where f_key='" + f_key + "'";
            List<List<String>> mobileno = ifr.getDataFromDB(Mobileno);
            if (!mobileno.isEmpty()) {
                MobileNo = mobileno.get(0).get(0);;
            }
            String SMobileNo = MobileNo.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "SMobileNo ==>" + SMobileNo);
            MobileNo = SMobileNo; //.substring(2, 12); -- modified by Monesh on 02/08/2024
            Log.consoleLog(ifr, "MobileNo ==>" + MobileNo);

            String response = cas.getCustomerAccountParams_VL(ifr, MobileNo, applicantType);
            Log.consoleLog(ifr, "response/getCustomerAccountParams_CB===>" + response);

            JSONParser parser = new JSONParser();
            JSONObject OutputJSON = (JSONObject) parser.parse(response);
            String AADHARNUMBER = OutputJSON.get("AadharNo").toString();
            String PANNUMBER = OutputJSON.get("PanNumber").toString();
            String DateofBirth = OutputJSON.get("DateofBirth").toString();
            String NRI = OutputJSON.get("NRI").toString();
            String Staff = OutputJSON.get("Staff").toString();

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
            String APIResponse = bpcc.mGetAPIData(ifr);
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
            String loanAcctOpen = "";
            String productcode_ExistingLoan_ip = "";
            String response360 = objCbs360.executeCBSAdvanced360Inquiryv2(ifr, processInsanceId, CustomerId, "Budget", "", "");
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
                int monthsDiff = differenceInMonths(ifr, loanAcctOpen);
                Log.consoleLog(ifr, " sinceDateMonthDiff value after calculating the monthdifference" + monthsDiff);
//                String loanAcctMonthDiffs = Integer.toString(monthsDiff);
//                Log.consoleLog(ifr, "Month Difference calculation:" + loanAcctMonthDiffs);
                if (monthsDiff > 12 || monthsDiff == 0) {
                    //productcode_ExistingLoan_ip = "Yes";
                    productcode_ExistingLoan_ip = "No";//Modiified by Ahmed on 26-07-2024
                } else {
                    //productcode_ExistingLoan_ip = "No";
                    productcode_ExistingLoan_ip = "Yes";//Modiified by Ahmed on 26-07-2024
                }
            }
            String salariedacc = "";
            String Sma2Count2Months = cf.getJsonValue(obj, "smaExists");
            String FlgCustType = cf.getJsonValue(obj, "FlgCustType");
            //    String salariedacc = cf.getJsonValue(obj, "salExists");
            String salariedAcc = "select SALARYCREDITEDTHROUGHBANK from los_nl_basic_info where pid ='" + processInsanceId + "'";
            List<List<String>> SalariedAcc = cf.mExecuteQuery(ifr, salariedAcc, "salariedAccQuery:");
            if (SalariedAcc.size() > 0) {
                salariedacc = SalariedAcc.get(0).get(0);
            }
            Log.consoleLog(ifr, "salariedacc:::" + salariedacc);
            String Age = cf.getJsonValue(obj, "Age");

            Log.consoleLog(ifr, " Aadhar:: " + AADHARNUMBER + " ,Pan:: " + PANNUMBER + " ,writeOffPresent:: "
                    + writeOffPresent + " ,WriteOffDate:: " + WriteOffDate + " ,Classification:: "
                    + classification + " ,PAPLExist:: " + productcode_ExistingLoan_ip + " ,count:: " + count + " ,ProductCode:: "
                    + productCode + " ,ExistSTP:: " + existSTP + " ,staffcheck:: " + Staff + " ,Nri:: " + NRI
                    + " ,NRO:: " + NRO + " ,salariedacc:: " + salariedacc);

            String knockoffInParams = applicantType + "," + AADHARNUMBER + "," + PANNUMBER + "," + WriteOffDate + "," + writeOffPresent + ","
                    + classification + "," + productcode_ExistingLoan_ip + "," + productCode + "," + Staff + "," + NRO + ","
                    + NRI + "," + salariedacc + "," + FlgCustType + "," + Age + "," + Sma2Count2Months;

            knockoffDecision = bpcc.checkKnockOff(ifr, "knock_of_CB_Rule", knockoffInParams,
                    "total_knockoff_cb_op");
            if (knockoffDecision.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
            if (knockoffDecision.contains("Proceed")) {
                Log.consoleLog(ifr, "knockoff Passed Successfully:::" + knockoffDecision);
                String cibilandexperiandecission = CheckCibilandExperianBudgetBorrower(ifr, applicantType);
                if (cibilandexperiandecission.equalsIgnoreCase("Approve")) {
                    String Eligibility = bpcc.elibilityRuleForLeadCapture(ifr, applicantType);
                    if (Eligibility.equalsIgnoreCase("Eligible")) {
                        Log.consoleLog(ifr, "Eligibility Passed Successfully:::" + Eligibility);
                        String scorecardDecision = scoreCardDecisionForLeadcapture(ifr, applicantType);

                        if (scorecardDecision.equalsIgnoreCase("Low Risk") || scorecardDecision.equalsIgnoreCase("Low Risk-II") || scorecardDecision.equalsIgnoreCase("Normal Risk")
                                || scorecardDecision.equalsIgnoreCase("Moderate Risk")) {
                            Log.consoleLog(ifr, "scorecardDecision Passed Successfully:::" + scorecardDecision);
                            ifr.setStyle("Btn_GenerateBureau", "disable", "true");

                        } else {
                            Log.consoleLog(ifr, "scoreCard Failed:::");
                            message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                            message.put("eflag", "false");
                            return message.toString();
                        }

                    } else {
                        Log.consoleLog(ifr, "Eligiblility Failed:::");
                        message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                        message.put("eflag", "false");
                        return message.toString();
                    }

                } else {
                    Log.consoleLog(ifr, "CibilandExperian Failed:::");
                    message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }

            } else {
                Log.consoleLog(ifr, "knockoff Failed:::");
                message.put("showMessage", cf.showMessage(ifr, "Btn_GenerateBureau", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                message.put("eflag", "false");
                return message.toString();
            }
        } catch (ParseException ex) {
            Logger.getLogger(BudgetBkoffCustomCode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return knockoffDecision;
    }

    public void formLoadDisbursementMakerVisibility(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "inside formLoadDisbursementMakerVisibility Canara Budget");
            //Section Visibilty for DisbursementMaker
//        String[] visibleSectionsGrids = new String[]{ "F_SourcingInfo","ALV_AL_ASSET_DET","ALV_AL_NETWORTH","ALV_PL_MExpense","F_Fin_Summary","F_OutwardDocument","F_InPrincipleEligibility","frame666","ALV_SCORERATING"};
//        for (int i = 0; i < visibleSectionsGrids.length; i++) {
//                ifr.setStyle(visibleSectionsGrids[i], "visible", "true");
//            }
//        Log.consoleLog(ifr, "visibleSectionsGrids visibled" + loan_selected);
//        //Section Hiding for DisbursementMaker
//        String[] InvisibleSectionsGrids = new String[]{"F_LoanAssess","F_TermsConditions"};
//        for (int i = 0; i < InvisibleSectionsGrids.length; i++) {
//                ifr.setStyle(InvisibleSectionsGrids[i], "visible", "false");
//            }
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            ifr.setTabStyle("tab1", "14", "visible", "true");//Disbursement
            ifr.setTabStyle("tab1", "8", "visible", "false");//Collateral
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            //ifr.setTabStyle("tab1", "127", "visible", "true");
            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation,F_ESIGN_ID";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "BTN_FetchScoreRating,F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,CTRID_PD_FETCHEXTCUST,BTN_Dedupe_Click,CTRID_PD_RESETDET,DD_CB_BUREAU_ID,QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            //Disbursment Tab field visibility
            String DisbursmentVisibleFields = "QNL_DISBURSMENT_ProbableDateofDisbursement,QNL_DISBURSMENT_ProposedLoanAmount,QNL_DISBURSMENT_InstallmentAmount,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_RepaymentType,QNL_DISBURSMENT_OtherFeeAndCharges,QNL_DISBURSMENT_DateofDisbursement,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_FixedTerm,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerName";
            pcm.controlvisiblity(ifr, DisbursmentVisibleFields);
            String DisbursmentHideFields = "datepick474,combo1737,datepick475";
            pcm.controlinvisiblity(ifr, DisbursmentHideFields);
            Log.consoleLog(ifr, " DisbursementMaker Budget Code End");
            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);
            /*String CBSproductCode = pcm.getCBSProductCode(ifr);
            Log.consoleLog(ifr, "inside formLoadDisbursementMakerVisibility Budget CBSproductCode" +CBSproductCode);*/
            //Tab Visibility for DisbursementMaker
            //Disbursment Tab field visibility
            /*String CBSproductCode = pcm.getCBSProductCode(ifr);
            Log.consoleLog(ifr, "inside formLoadDisbursementMakerVisibility Pension CBSproductCode" +CBSproductCode);*/
            //Tab Visibility for DisbursementMaker
        } else {
            Log.consoleLog(ifr, "No loan selected" + loan_selected);
        }
    }

//Tab Visibility for Reviwer by sharon
    public void OnLoadReviwerVisibility(IFormReference ifr) {

        Log.consoleLog(ifr, "inside OnLoadReviwerVisibility ");

        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "OnLoadReviwerVisibility::ProcessInstanceId==>" + ProcessInstanceId);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";

        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "OnLoadReviwerVisibility::loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "intoReviewerDetails");
            String visibleTab = "0,4,6,7,8,11,12,13,14,16,17,18,19";
            pcm.tabInVisibility(ifr, "tab1", visibleTab);
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:visiblefield==>" + visibleTab);

            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation";
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:visibleSectionsGrids==>" + visibleTab);
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_ESIGN_ID,F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:nonVisibleSectionGrids==>" + nonVisibleSectionGrids);
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);

            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, " OnLoad ReviwerVisibility Budget Code End");

        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "intoReviewerDetails");
            String visibleTab = "0,4,6,7,8,11,12,13,14,15,16,17,18,19";
            pcm.tabInVisibility(ifr, "tab1", visibleTab);
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:visiblefield==>" + visibleTab);

            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation,F_ESIGN_ID";
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:visibleSectionsGrids==>" + visibleTab);
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            Log.consoleLog(ifr, "OnLoadReviwerVisibility:nonVisibleSectionGrids==>" + nonVisibleSectionGrids);
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);

            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, " OnLoad ReviwerVisibility Pension Code End");

        }

    }

    public void onLoadBureauConsent(IFormReference ifr) {
        Log.consoleLog(ifr, "inside onLoadBureauConsent BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "inside onLoadBureauConsent:: Budget ");
            String QueSampl = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_Methodology,QNL_BUREAU_CONSENT_ConsentVerifiedBy,QNL_BUREAU_CONSENT_ConsentVerifiedOn,Btn_OtpBureau";
            pcm.controlvisiblity(ifr, QueSampl);
            String QueSamp2 = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_ConsentVerifiedOn,QNL_BUREAU_CONSENT_ConsentVerifiedBy";
            pcm.controlDisable(ifr, QueSamp2);
            String QueSamp3 = "Btn_ViewBureau,QNL_BUREAU_CONSENT_docID,Btn_GenerateOTPBureau,Btn_ReGenerateOTPBureau,Btn_ValidateOTPBureau,Btn_PortalBureau";
            pcm.controlinvisiblity(ifr, QueSamp3);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            Log.consoleLog(ifr, " onLoadBureauConsent:: Budget Code End");
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "inside onLoadBureauConsent:: Pension ");
            String QueSampl = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_Methodology,QNL_BUREAU_CONSENT_ConsentVerifiedBy,QNL_BUREAU_CONSENT_ConsentVerifiedOn,Btn_OtpBureau";
            pcm.controlvisiblity(ifr, QueSampl);
            String QueSamp2 = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_ConsentVerifiedOn,QNL_BUREAU_CONSENT_ConsentVerifiedBy";
            pcm.controlDisable(ifr, QueSamp2);
            String QueSamp3 = "Btn_ViewBureau,QNL_BUREAU_CONSENT_docID,Btn_GenerateOTPBureau,Btn_ReGenerateOTPBureau,Btn_ValidateOTPBureau,Btn_PortalBureau";
            pcm.controlinvisiblity(ifr, QueSamp3);
            String QueSamp4 = "Btn_Refresh";
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlEnable(ifr, QueSamp4);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            Log.consoleLog(ifr, " onLoadBureauConsent:: Pension Code End");
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "BudgetBkOfficeCustomCode : onLoadBureauConsent : inside onLoadBureauConsent:: VL ");
            String QueSampl = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_Methodology,QNL_BUREAU_CONSENT_ConsentVerifiedBy,QNL_BUREAU_CONSENT_ConsentVerifiedOn,Btn_OtpBureau";
            pcm.controlvisiblity(ifr, QueSampl);
            String QueSamp2 = "QNL_BUREAU_CONSENT_PartyType,QNL_BUREAU_CONSENT_ConsentVerifiedOn,QNL_BUREAU_CONSENT_ConsentVerifiedBy,QNL_BUREAU_CONSENT_Methodology";
            pcm.controlDisable(ifr, QueSamp2);
            String QueSamp3 = "Btn_ViewBureau,QNL_BUREAU_CONSENT_docID,Btn_GenerateOTPBureau,Btn_ReGenerateOTPBureau,Btn_ValidateOTPBureau,Btn_PortalBureau";
            pcm.controlinvisiblity(ifr, QueSamp3);
            String QueSamp4 = "Btn_Refresh";
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlEnable(ifr, QueSamp4);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, QueSampl);
            }
            Log.consoleLog(ifr, "BudgetBkOfficeCustomCode : onLoadBureauConsent : onLoadBureauConsent:: VL Code End");
        }
    }

    public String InPrincipleLCBCheck(IFormReference ifr) {
        Log.consoleLog(ifr, "inside the InPrincipleLCBCheck : ");
        JSONObject message = new JSONObject();
        JSONParser parser = new JSONParser();
        JSONObject EligibilityDataObj = new JSONObject();
        try {

            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String deductionmonth = "";
            String grosssalary = "";
            String net_salary = "";
            String reqAmount = "";
            String Fkey = bpcc.Fkey(ifr, "B");
            String occupation_info = ConfProperty.getQueryScript("OCCUPINFOQUERY").replaceAll("#Fkey#", Fkey);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, occupation_info, "occupation_info Query:");
            if (list1.size() > 0) {
                grosssalary = list1.get(0).get(0);
                deductionmonth = list1.get(0).get(1);
                net_salary = list1.get(0).get(2);
            }

            Log.consoleLog(ifr, "basicInfo deductionmonth: " + deductionmonth);
            Log.consoleLog(ifr, "basicInfo grosssalary: " + grosssalary);

            String loanTenure = "";
            String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
            if (list4.size() > 0) {
                reqAmount = list4.get(0).get(0);
                loanTenure = list4.get(0).get(1);
            }
            Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);
            Log.consoleLog(ifr, "inside eligibility brms:::");
            String obligations = "";
            String loanROI = pcm.mGetROICB(ifr);
            Log.consoleLog(ifr, "roi : " + loanROI);
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
            String TotalEmiQuery = ConfProperty.getQueryScript("getEMIBasedOnScore").replaceAll("#ProcessInstanceId#", ProcessInsanceId).replaceAll("#BT#", bureau_Type);
            List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");

            String cibiloblig = "";

            if (TotalEmi.size() > 0) {

                cibiloblig = TotalEmi.get(0).get(0).toString();

                Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

            } else {

                cibiloblig = "0.00";

            }

            //BigDecimal oblig = new BigDecimal(cibiloblig);
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);

            String Prodcapping = null;
            String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
            if (ProdcappingList.size() > 0) {
                Prodcapping = ProdcappingList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);
            HashMap hm = new HashMap();
            hm.put("cibiloblig", cibiloblig);
            Log.consoleLog(ifr, "cibiloblig===>" + cibiloblig);
            hm.put("tenure", String.valueOf(loanTenure));
            hm.put("roi", String.valueOf(loanROI));
            hm.put("loancap", Prodcapping);
            hm.put("reqAmount", reqAmount);
            hm.put("deductionmonth", deductionmonth);
            hm.put("grosssalary", grosssalary);

            String FinalEligibilityLoanAmt = getAmountForInprincipleDataSaveBO(ifr, hm);
            Log.consoleLog(ifr, "FinalEligibilityLoanAmt : " + FinalEligibilityLoanAmt);

//            String grosssalaryipBG = (String) EligibilityDataObj.get("grosssalaryip");
//            String deductionsalaryBG = (String) EligibilityDataObj.get("deductionsalary");
//            String cbCibilObligBG = (String) EligibilityDataObj.get("cbCibilOblig");
//            String netTakeHomeNTHBG = (String) EligibilityDataObj.get("netTakeHome");
//            String netIncomeBG = (String) EligibilityDataObj.get("netIncome");
//            String ftTenureBG = (String) EligibilityDataObj.get("ftTenure");
//            String ftRoiBG = (String) EligibilityDataObj.get("ftRoi");
//            String loanAmountBG = (String) EligibilityDataObj.get("loanAmount");
//            String twofivetimesgrosssal1 = (String) EligibilityDataObj.get("twofivetimesgrosssal");
//            String prodspeccappingBG = (String) EligibilityDataObj.get("prodspeccapping");
//            String finaleligibilityBG = (String) EligibilityDataObj.get("finaleligibility");
//            String loanAmountPerPolicy = (String) EligibilityDataObj.get("loanAmtPerPolicy");
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }

        return "";
    }

    //Modified by keerthana on 26/06/2024
    //Modified by Aravindh  29/05/2024
    public String getAmountForInprincipleDataSaveBO(IFormReference ifr, HashMap<String, String> loandata) {
        String finaleligibilityRound = "";
        try {
            Log.consoleLog(ifr, "inside getAmountForInprincipleDataSaveBO:::::");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            //BigDecimal grossSalaryMultiple = pcm.mCheckBigDecimalValue(ifr, pcm.getConstantValue(ifr, "LOANELIGIBILITYCB", "GROSSSALMULTIPLE"));
            //String grossmultiple = ConfProperty.getCommonPropertyValue("GROSSMULTIBUDGET");
            //Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR GROSSMULTIBUDGET " + grossmultiple);
            //Commented by Aravindh.K.K on 16/08/2024 getting same from Constants table.
            BigDecimal grossSalaryMultiple = pcm.mCheckBigDecimalValue(ifr,
                    pcm.getConstantValue(ifr, "LOANELIGIBILITYCB", "GROSSSALMULTIPLE"));
            Log.consoleLog(ifr, " grossSalary Multiple from constants ::::" + grossSalaryMultiple);
            BigDecimal netsalary;
            String ReqAmount = loandata.get("reqAmount").toString();
            BigDecimal ftReqAmount = pcm.mCheckBigDecimalValue(ifr, loandata.get("reqAmount"));
            BigDecimal ftTenure = pcm.mCheckBigDecimalValue(ifr, loandata.get("tenure"));
            BigDecimal ftRoi = pcm.mCheckBigDecimalValue(ifr, loandata.get("roi"));
            BigDecimal deductionsalary = pcm.mCheckBigDecimalValue(ifr, loandata.get("deductionmonth"));
            BigDecimal grosssalaryip = pcm.mCheckBigDecimalValue(ifr, loandata.get("grosssalary"));
            BigDecimal lacAmount = new BigDecimal(100000);
            int tenure1 = Integer.parseInt(ftTenure.toString());
            Log.consoleLog(ifr, "tenure conversion 123@@ ===> " + tenure1);
            BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, tenure1);
            Log.consoleLog(ifr, "emiperlc calculatePMT ::" + emiperlc);
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
            BigDecimal cbCibilOblig = pcm.mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
            Log.consoleLog(ifr, "cbCibilOblig ===> " + cbCibilOblig);
            ////Commented by Aravindh.kk on 16/08/24 getting from Constants Table instead of Properties.
//            String percentageBUG = ConfProperty.getCommonPropertyValue("PERCENTAGEPEN");
//            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR PERCENTAGEPEN " + percentageBUG);
//            String lac_Amount = ConfProperty.getCommonPropertyValue("LACAMOUNT");
//            Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR LACAMOUNT " + lac_Amount);
//            BigDecimal laAmount = new BigDecimal(lac_Amount);
            
            BigDecimal mulValue4 = pcm.mCheckBigDecimalValue(ifr, pcm.getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "NETHOMEPERENT"));
            Log.consoleLog(ifr, " mulValue4 :::: " + mulValue4);
            BigDecimal Hundred = new BigDecimal(100);
            BigDecimal PercentOfGrossSalary = mulValue4.divide(Hundred);
            Log.consoleLog(ifr, " PercentOfGrossSalary :::: " + PercentOfGrossSalary);
            //BigDecimal percentage = new BigDecimal(percentageBUG);

            //BigDecimal Cmpsalary = new BigDecimal(10000);
            BigDecimal netTakeHomeSalary = new BigDecimal(0);
            BigDecimal Cmpsalary = pcm.mCheckBigDecimalValue(ifr, pcm.getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "NETTAKEHOMESALARY"));
            Log.consoleLog(ifr, " Cmpsalary::::" + Cmpsalary);
            BigDecimal grossSalary25Perc = new BigDecimal(0);
            grossSalary25Perc = grosssalaryip.multiply(PercentOfGrossSalary, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_UP);
            Log.consoleLog(ifr, " grossSalary25Perc ==> " + grossSalary25Perc);
            int comparisonResult = grossSalary25Perc.compareTo(Cmpsalary);
            if (comparisonResult > 0) {
                Log.consoleLog(ifr, "inside if comparisonResult::::" + comparisonResult);
                netTakeHomeSalary = grossSalary25Perc;
            } else {
                Log.consoleLog(ifr, "inside else comparisonResult::::" + comparisonResult);
                netTakeHomeSalary = Cmpsalary;
            }
            Log.consoleLog(ifr, "netTakeHomeSalary===> " + Cmpsalary);
            BigDecimal netIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig);
            Log.consoleLog(ifr, "net_income===> " + netIncome);

//            if(Integer.parseInt(netTakeHomeNTH.toString())>Integer.parseInt(lac.toString())){
//                netTakeHomeNTH=netTakeHomeNTH;
//            }else{
//                netTakeHomeNTH=lac;
//            }
//            String cibil=cbCibilOblig.toString();
//            if(Integer.parseInt(cibil)==0.00){
//                cbCibilOblig=netTakeHomeNTH;
//            }
//            Log.consoleLog(ifr, "netTakeHomeNTH123===> " + cbCibilOblig);
            BigDecimal netAvailIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig).subtract(netTakeHomeSalary);
            Log.consoleLog(ifr, "netAvailIncome===> " + netAvailIncome);
            BigDecimal twofivetimesgrosssal = grossSalaryMultiple.multiply(grosssalaryip);
            Log.consoleLog(ifr, "twofivetimesgrosssal===> " + twofivetimesgrosssal);

            BigDecimal loanAmountperpolicy = ((netAvailIncome).divide(emiperlc, 2, RoundingMode.HALF_UP)).multiply(LOANAMOUNTPERPOLICYMULTIPLE);
            Log.consoleLog(ifr, "loanAmountperpolicy ===> " + loanAmountperpolicy);

            BigDecimal prodspeccapping = pcm.mCheckBigDecimalValue(ifr, loandata.get("loancap"));
            Log.consoleLog(ifr, "prodspeccapping===> " + prodspeccapping);
            //BigDecimal inprincipleamount = ftReqAmount.min(loanAmountperpolicy).min(twofivetimesgrosssalNTH).min(prodspeccapping);
            // Log.consoleLog(ifr, "inprincipleamount===> " + inprincipleamount);
            BigDecimal finaleligibility = ftReqAmount.min(loanAmountperpolicy).min(twofivetimesgrosssal).min(prodspeccapping);

            Log.consoleLog(ifr, "finaleligibility===> " + finaleligibility);
            Log.consoleLog(ifr, "Before EligibilityDataObj : ");

            String grosssalaryipRound = String.valueOf(Math.round(Double.parseDouble(grosssalaryip.toString())));
            Log.consoleLog(ifr, "Before grosssalaryip : " + grosssalaryip);
            String deductionsalaryRound = String.valueOf(Math.round(Double.parseDouble(deductionsalary.toString())));
            Log.consoleLog(ifr, "Before deductionsalary : " + deductionsalary);
            String cbCibilObligRound = String.valueOf(Math.round(Double.parseDouble(cbCibilOblig.toString())));
            Log.consoleLog(ifr, "Before cbCibilOblig : " + cbCibilOblig);
            String netTakeHomeNTHRound = String.valueOf(Math.round(Double.parseDouble(netTakeHomeSalary.toString())));
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

            String twofivetimesgrosssalRound = String.valueOf(Math.round(Double.parseDouble(twofivetimesgrosssal.toString())));
            Log.consoleLog(ifr, "Before twofivetimesgrosssal : " + twofivetimesgrosssalRound);
            String prodspeccappingRound = String.valueOf(Math.round(Double.parseDouble(prodspeccapping.toString())));
            Log.consoleLog(ifr, "Before prodspeccapping : " + prodspeccappingRound);
            //  String finaleligibility = String.valueOf(Math.round(Double.parseDouble(finaleligibilityBG)));
            //  String loanAmttPerPolicy = String.valueOf(Math.round(Double.parseDouble(LOANAMOUNTPERPOLICYMULTIPLE.toString())));
            //  Log.consoleLog(ifr, "Before loanAmttPerPolicy : " + loanAmttPerPolicy);
            double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibility.toString()) / 1000) * 1000;
            finaleligibilityRound = String.valueOf(Math.round(finaleligibilityDouble));

            Log.consoleLog(ifr, "After rounded finaleligibility Value : " + finaleligibilityRound);

            if (finaleligibilityRound.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
            String finalEligibleAmount = "";
            String productCode = pcm.getProductCode(ifr);
            String finalAmountInParams = productCode + "," + finaleligibilityRound;
            finalEligibleAmount = bcc.checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams, "validcheck1op");

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
                formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanamountasperIncome", twofivetimesgrosssalRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_LoanAmountasperProductPolicy", prodspeccappingRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_RequestedLoanAmount", ReqAmount);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_InPrincipalloanamount", finaleligibilityRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE_InstalmentAmount", emi);
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
                formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanamountasperIncome", twofivetimesgrosssalRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-LoanAmountasperProductPolicy", prodspeccappingRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-RequestedLoanAmount", ReqAmount);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-InPrincipalloanamount", finaleligibilityRound);
                formDetailsJson.put("QNL_LA_INPRINCIPLE-InstalmentAmount", emi);
                gridResultSetArr.add(formDetailsJson);
                Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE gridResultSetArr ==> " + gridResultSetArr);
                ((IFormAPIHandler) ifr).clearTable("QNL_LA_INPRINCIPLE");
                ((IFormAPIHandler) ifr).addDataToGrid("QNL_LA_INPRINCIPLE", gridResultSetArr, true);
                Log.consoleLog(ifr, "QNL_LA_INPRINCIPLE Populated Portal to Backoffice ==>" + gridResultSetArr.toString());
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return RLOS_Constants.ERROR;
        }
        return finaleligibilityRound;
    }

// modified by Kathir & Keerthana for pension LC changes on 06/08/2024
    public String msaveDataInPartyDetailGridFetch(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "inside try block::::msaveDataInPartyDetailGridFetch::::: ");
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            HashMap<String, String> customerdetails = new HashMap<>();
            String ApplicantType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
            String strCusterid = ifr.getValue("CTRID_PD_SRCHVAL").toString();
            Log.consoleLog(ifr, "msaveDataInPartyDetailGridFetch::Param Value::strCusterid::" + strCusterid);
            String mobileNumber = ifr.getValue("Mobile_Number_PD").toString();
            Log.consoleLog(ifr, "msaveDataInPartyDetailGridFetch::Param Value::mobileNumber::" + mobileNumber);
            customerdetails.put("CustomerId", strCusterid);
            customerdetails.put("mobileNumber", mobileNumber);
            customerdetails.put("ApplicantType", ApplicantType);
            String pensionAPIStatus = "";
            String APIStatus = cas.fetchCustomerAccountSummaryBorrower(ifr, customerdetails);
            if (APIStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            Log.consoleLog(ifr, "before branchcode ");
            String strbrnchcode = APIStatus;
            Log.consoleLog(ifr, "pensionAPIStatus:strbrnchcode " + strbrnchcode);

            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "msaveDataInPartyDetailsGridFetch::Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            if ((loan_selected.equalsIgnoreCase("Canara Pension")) && (ApplicantType.equalsIgnoreCase("B"))) {
                Log.consoleLog(ifr, "msaveDataInPartyDetailGridFetch:: Calling getPensioner API");
                pensionAPIStatus = pd.getPensionExternalDetails(ifr, strCusterid, strbrnchcode);
                Log.consoleLog(ifr, "pensionAPIStatus: " + pensionAPIStatus);
                if (pensionAPIStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
            }

            //mPopulateMisDataCB(ifr);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
            Log.errorLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
            return pcm.returnError(ifr);
        }
        return "";

    }

    public String mPopulateMisDataCB(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "inside try block::::mPopulateMisDataCB::::: ");

            // String CustomerId = pcm.getCustomerIDPAPL(ifr);
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId:::mPopulateMisDataCB::::" + CustomerId);

            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Demographic API = new Demographic();
            String GetDemoGraphicData = API.getDemographic(ifr, ProcessInstanceId, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData::mPopulateMisDataCB::::" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, GetDemoGraphicData);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
            Log.errorLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
            return pcm.returnError(ifr);
        }
        return "";

    }

    public static String fetchDataFromGrid(IFormReference ifr, String gridId) {
        JSONArray jsonArray = ifr.getDataFromGrid(gridId);
        return jsonArray.toString();
    }

    public void existingCustomerAutoPopulate(IFormReference ifr) {
        Log.consoleLog(ifr, " Inside  autopopulate EC");
        String subProduct = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString();
        if (subProduct.equalsIgnoreCase("STP-CB")) {
            Log.consoleLog(ifr, " Inside CB sub produt for auto populate");
            //ifr.addItemInCombo("CTRID_PD_EXTCUST","Yes","Yes");
            ifr.setValue("CTRID_PD_EXTCUST", "Yes");
            ifr.setStyle("CTRID_PD_EXTCUST", "disable", "true");
        }
    }

    public void mAccChangeProcessingBranchName(IFormReference ifr, String control, String Event, String value) {
        try {
            Log.consoleLog(ifr, "Enter mAccChangeProcessingBranchName");
            String processingBranchName = ifr.getValue("QL_SOURCINGINFO_ParkingBranch").toString();
            String processingBranchCode = ifr.getValue("QL_SOURCINGINFO_Branch").toString();
            Log.consoleLog(ifr, "processingBranchName==>" + processingBranchName);
            Log.consoleLog(ifr, "processingBranchCode==>" + processingBranchCode);
            ifr.setValue("QL_LEAD_DET_BranchName", processingBranchName);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccChangeProcessingBranchName" + e);
            Log.errorLog(ifr, "Exception in mAccChangeProcessingBranchName" + e);
        }
        Log.consoleLog(ifr, "End mAccChangeProcessingBranchName");
    }

    //added by aravind
    //reviewer decision
    public String setDecisionValueBudget(IFormReference ifr, String Control, String Event, String value) {

        try {
            Log.consoleLog(ifr, "inside setDecisionValueBudget method::");
            //String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String ActivityName = ifr.getActivityName();

            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside setDecisionValueBudget BudgetBkoffCustomCode:: Reviewer ");
                ifr.clearCombo("DecisionValue");
                ifr.addItemInCombo("DecisionValue", "Submit", "S");

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  checkCBEligibilityDecisionVL method::" + e);
            Log.errorLog(ifr, "Exception in  checkCBEligibilityDecisionVL method::" + e);
        }
        return "";

    }

    public void onChangeOthersLoanPurpose(IFormReference ifr) {
        Log.consoleLog(ifr, " Inside  onchange others loan purpose");
        String loanPurpose = ifr.getValue("P_CB_LD_Purpose").toString();
        if (loanPurpose.equalsIgnoreCase("OTH")) {
            ifr.setStyle("P_CB_LD_Purpose_Others", "visible", "true");
        } else {
            ifr.setStyle("P_CB_LD_Purpose_Others", "visible", "false");
        }
    }

    public void OnLoadActionHistory(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilitiesMaker Budget ");

            String visibleActionsFields = "QNL_Action_History_Decision,QNL_Action_History_Remarks,QNL_Action_History_SubmitStageName,QNL_Action_History_SubmitUser,QNL_Action_History_Receiving_Date,QNL_Action_History_SubmitDate,QNL_Action_History_Total_Days,QNL_Action_History_Rejection_Category,QNL_Action_History_Rejection_SubCategory";
            pcm.controlvisiblity(ifr, visibleActionsFields);
            String disableActionFileds = "QNL_Action_History_Remarks,QNL_Action_History_SubmitStageName,QNL_Action_History_SubmitUser,QNL_Action_History_Receiving_Date,QNL_Action_History_SubmitDate,QNL_Action_History_Total_Days,QNL_Action_History_Decision";
            pcm.controlDisable(ifr, disableActionFileds);

            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visibleActionsFields);
                pcm.controlvisiblity(ifr, visibleActionsFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, visibleActionsFields);
                pcm.controlvisiblity(ifr, visibleActionsFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlDisable(ifr, visibleActionsFields);
                pcm.controlvisiblity(ifr, visibleActionsFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Deviation")) {
                pcm.controlDisable(ifr, visibleActionsFields);
                pcm.controlvisiblity(ifr, visibleActionsFields);
            }
        }
    }

    public void onLoadLeadCapture(IFormReference ifr) {
        Log.consoleLog(ifr, "inside mformOnLoadLeadCapture budgetBkoffcustomCode budget");
        Log.consoleLog(ifr, "inside fromLoadMaker Budget ");
        String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,F_OutwardDocument,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_FinalEligibility";
        pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
        Log.consoleLog(ifr, " fromLoadMaker Budget Code End");
        ifr.setStyle("RejectCategoryDecision", "visible", "false");
        ifr.setStyle("RejectSubCategoryDecision", "visible", "false");
        String midName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
        Log.consoleLog(ifr, "After getting MidName" + midName);
        if (midName.equalsIgnoreCase("null") || midName.equalsIgnoreCase("")) {
            Log.consoleLog(ifr, "check MidName");
            midName = "";
            ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", "mandatory", "false");
        }
        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", midName);
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String portalYN = ConfProperty.getQueryScript("updateFullyAssistedDataQuery").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "updateFullyAssistedDataQuery :: " + portalYN + " , PID :: " + PID);
        ifr.saveDataInDB(portalYN);
        Log.consoleLog(ifr, " fromLoad LeadCapture Budget Code End");
    }

    public void popluateLCDocumentsUploadCB(IFormReference ifr) {
        Log.consoleLog(ifr, "inside the popluateLCDocumentsUploadCB");
        try {

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
            String cbquery = "select F_KEY from los_nl_basic_info where PID='" + PID + "' and APPLICANTTYPE='CB'";
            List<List<String>> docResultDataCB = ifr.getDataFromDB(cbquery);
            Log.consoleLog(ifr, "CB::" + docResultDataCB);
            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
            int rowCount = docResultData.size();
            Log.consoleLog(ifr, "BudgetPortalCustomCode:popluateLCDocumentsUploadCB->rowCount::" + rowCount);
            if (rowCount == 0) {
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
                            if (applicantNameArrayData.contains("Borrower")) {
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_DocumentType", result.get(j).get(0));
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_Mandatory", result.get(j).get(1));
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_ApplicantType", applicantName[i]);
                                arr.add(re);
                            } else if (applicantNameArrayData.contains("Co Borrower")) {
                                Log.consoleLog(ifr, "applicantName is " + Salaried);
                                if (!Salaried.equalsIgnoreCase("")) {
                                    re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_Mandatory", result.get(j).get(1));
                                    re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_DocumentType", result.get(j).get(0));
                                    re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_ApplicantType", applicantName[i]);
                                    arr.add(re);
                                }
                            }
                        }
                        Log.consoleLog(ifr, "Document grid  json array::" + arr);
                        ifr.addDataToGrid("ALV_UPLOAD_DOCUMENT", arr);
                    }
                }

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the popluateLCDocumentsUploadCB" + e);
        }
    }

    public void onChangeROIType(IFormReference ifr) {
        Log.consoleLog(ifr, " Inside  onchange ROI Type bk office");
        String ROIType = ifr.getValue("P_CB_LD_ROI_Type").toString();
        if (ROIType.equalsIgnoreCase("Fixed")) {
            ifr.setStyle("P_CB_LD_FRP", "visible", "true");
            ifr.setStyle("P_CB_LD_FRP", "mandatory", "true");
        } else {
            ifr.setStyle("P_CB_LD_FRP", "visible", "false");
        }
    }

    public String mAccChangeRetirementDate(IFormReference ifr) throws java.text.ParseException {//Checked
        try {
            JSONObject message = new JSONObject();
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID .." + ProcessInsanceId);
            int diffyrs = cf.mCalculateDateDiffInYrs(ifr, ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate").toString());
            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod", String.valueOf(diffyrs));
            int diffyrsage = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age").toString().equalsIgnoreCase("") ? 0 : Integer.parseInt(ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age").toString());
            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", String.valueOf(diffyrs + diffyrsage));
            LocalDate startDate = LocalDate.now();
            String dateOfRetirement = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate").toString();
            SimpleDateFormat dateOfRetirementFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat TargetDate = new SimpleDateFormat("yyyy-MM-dd");
            Date dateofcustopenFo = dateOfRetirementFormat.parse(dateOfRetirement);
            dateOfRetirement = TargetDate.format(dateofcustopenFo);
            LocalDate endDate = LocalDate.parse(dateOfRetirement);
            long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate);
            String tenure = "";
            if (monthsBetween < 12) {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Retirement Date cannot be with in 12 months from Current Date!"));
                JSONObject messagereturn = new JSONObject();
            } else {
                if (monthsBetween > 84) {
                    tenure = "84";
                    ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, "84");
                } else {
                    tenure = "61";
                    ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, "61");
                }
            }

            String tenureAge_Query = ConfProperty.getQueryScript("updateTenureAgeProposeDataQuery").replaceAll("#PID#", ProcessInsanceId).replaceAll("#tenure#", tenure);
            ifr.saveDataInDB(tenureAge_Query);

        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
            Log.errorLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
        }
        return "";
    }

    public String checkExistingCustomer(IFormReference ifr, String Control) {
        Log.consoleLog(ifr, "inside checkExistingCustomer budgetBkoffcustomCode budget");
        JSONObject message = new JSONObject();
        try {
            String ExtCustomer = "";
            if ("CTRID_PD_EXTCUST".equals(Control)) {
                ExtCustomer = ifr.getValue("CTRID_PD_EXTCUST").toString();
            } else {
                ExtCustomer = ifr.getValue("QNL_BASIC_INFO_ExistingCustomer").toString();
            }
            Log.consoleLog(ifr, " ExtCustomer : " + ExtCustomer);
            if ("No".equalsIgnoreCase(ExtCustomer)) {
                Log.consoleLog(ifr, " ExtCustomer is No ");
                if ("CTRID_PD_EXTCUST".equals(Control)) {
                    message.put("showMessage", cf.showMessage(ifr, "CTRID_PD_EXTCUST", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per the scheme guidelines of the Bank."));
                } else {
                    message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_ExistingCustomer", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per the scheme guidelines of the Bank."));
                }
                message.put("eflag", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the checkExistingCustomer" + e);
        }
        Log.consoleLog(ifr, " checkExistingCustomer Budget Code End");
        return message.toString();
    }

    public String dataPopulationInPl(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "inside proposed loan details population ");
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            int gridRowCount = ifr.getDataFromGrid("ALV_PROPOSED_FACILITY").size();
            Log.consoleLog(ifr, "gridRowCount-----" + gridRowCount);

            if (gridRowCount == 0) {

                String loanDetailQuery = "select purpose,ifothers,roitype,rllr,frp,preemidate from \n" + "LOS_TRN_LOAN_DETAILS where PID='" + PID + "'";
                String purpose = "";
                String ifothers = "";
                String roitype = "";
                String rllr = "";
                String frp = "";
                String preemidate = "";
                String loanROI = pcm.mGetROICB(ifr);

                Log.consoleLog(ifr, "loanROI : " + loanROI);
                String minTerm = "";
                String maxTerm = "";
                String RepaymnetFrequency = "";
                String requestedLoanAmountWord = "";
                String tenure = "select MINTENURE,MAXTENURE from los_m_fee_charges where FEECODE='CHR30'";

                List<List<String>> tenureList = cf.mExecuteQuery(ifr, tenure, "Execute query for fetching Loan amount data from portal in principal");
                if (tenureList.size() > 0) {
                    minTerm = tenureList.get(0).get(0);
                    maxTerm = tenureList.get(0).get(1);
                    Log.consoleLog(ifr, "maxTerm : " + maxTerm);
                    Log.consoleLog(ifr, "minTerm : " + minTerm);
                }
                String repaymnetFrequency = "SELECT RepayFrq FROM LOS_M_REPAY_FRQ  WHERE IsActive='Y'  and REPAYFRQCODE='M'";
                List<List<String>> repaymnetFrequencyList = cf.mExecuteQuery(ifr, repaymnetFrequency, "Execute query for fetching Loan amount data from portal in principal");
                if (repaymnetFrequencyList.size() > 0) {
                    RepaymnetFrequency = repaymnetFrequencyList.get(0).get(0);
                    Log.consoleLog(ifr, "RepaymnetFrequency : " + RepaymnetFrequency);
                }
                String loanAmountInWord = "SELECT convert_to_indian_words(\"Loan_Amount\") FROM LOS_PORTAL_SLIDERVALUE WHERE PID='" + PID + "'";
                List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
                if (loanAmountInWords.size() > 0) {
                    requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                    Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
                }
                String ProposedLoanAmount = null;
                String LoanAmountStr = null;
                String loanTenure = null;
                //String LoanAMTTenureQuery = "SELECT * FROM LOS_PORTAL_SLIDERVALUE WHERE PID ='" + ProcessInstanceId + "'";
                String LoanAMTTenureQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", PID);
                List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTTenureQuery, "Execute query for fetching Slider Loan amount,Tenure data from portal in principal");
                if (LoanAmountList.size() > 0) {
                    LoanAmountStr = LoanAmountList.get(0).get(1);
                    loanTenure = LoanAmountList.get(0).get(2);
                    ProposedLoanAmount = String.valueOf(Math.round(Double.parseDouble(LoanAmountStr)));
                }
                Log.consoleLog(ifr, "Slider ProposedLoanAmount : " + ProposedLoanAmount);
                Log.consoleLog(ifr, "Slider loanTenure : " + loanTenure);
                int ProposedLoanSize = ifr.getDataFromGrid("ALV_PROPOSED_FACILITY").size();
                Log.consoleLog(ifr, "ProposedLoanSize ::" + ProposedLoanSize);
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, loanDetailQuery, "Execute query for fetching Purpose data from portal");
                if (PurposePortal.size() > 0) {
                    Log.consoleLog(ifr, "inside proposed grid ");
                    purpose = PurposePortal.get(0).get(0);
                    ifothers = PurposePortal.get(0).get(1);
                    if (ifothers.equalsIgnoreCase("") || ifothers.isEmpty()) {
                        Log.consoleLog(ifr, "inside if others");
                        ifothers = "no others";
                    }
                    roitype = PurposePortal.get(0).get(2);
                    rllr = PurposePortal.get(0).get(3);
                    frp = PurposePortal.get(0).get(4);
                    preemidate = PurposePortal.get(0).get(5);

                    JSONArray gridResultSet = new JSONArray();
                    JSONObject formDetailsJson = new JSONObject();
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_SubProduct", subProductCode);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", ProposedLoanAmount);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_Tenure", loanTenure);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_ROI", loanROI);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_MinTerm", minTerm);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_MaxTerm", maxTerm);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_RepayFreq", RepaymnetFrequency);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_LoanPurpose", purpose);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_ROIType", roitype);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_RLLR", rllr);
                    formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_FRP", frp);

                    gridResultSet.add(formDetailsJson);
                    //ifr.addDataToGrid("ALV_PROPOSED_FACILITY", gridResultSet);
                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + gridResultSet);
                    ((IFormAPIHandler) ifr).addDataToGrid("ALV_PROPOSED_FACILITY", gridResultSet, true);

                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "error inside the  InPrincipleLCBCheck: " + e);
        }

        return "";

    }

    public String OnChangeSourcingInfoSectionMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeSourcingInfoSection AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String ApplicationNumberQuery = ConfProperty.getQueryScript("ApplicationNumber").replaceAll("#PID#", ProcessInstanceId);
        Log.consoleLog(ifr, "ApplicationNumberQuery==>" + ApplicationNumberQuery);
        List<List<String>> ApplicationNumberQueryExe = cf.mExecuteQuery(ifr, ApplicationNumberQuery, "Execute query for fetching ApplicationNumber ");
        String AppNumber = ApplicationNumberQueryExe.get(0).get(0);
        Log.consoleLog(ifr, "AppNumber From Database" + AppNumber);

        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);

        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {

            Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker Budget ");
            Log.consoleLog(ifr, "AppNumber From Database" + AppNumber);
            ifr.setValue("QL_SOURCINGINFO_LeadID", AppNumber);
            String query = ConfProperty.getQueryScript(AcceleratorConstants.LEADSOURCEQUERY).replaceAll("#leadSourceCode#", "P");
            Log.consoleLog(ifr, "query leadsource" + query);
            List<List<String>> quertResult = ifr.getDataFromDB(query);
            String leadSourceCode = "";
            if (quertResult.size() > 0) {
                leadSourceCode = quertResult.get(0).get(0);
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker Budget/Pension QL_SOURCINGINFO_LeadSource " + quertResult.get(0).get(0));
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker Budget/Pension QL_SOURCINGINFO_LeadSource " + leadSourceCode);
                if (leadSourceCode.equalsIgnoreCase("Portal")) {
                    ifr.setValue("QL_SOURCINGINFO_LeadSource", "Online");
                } else {
                    ifr.setValue("QL_SOURCINGINFO_LeadSource", "Walk In");
                }

            }
            //ifr.setValue("QL_SOURCINGINFO_LeadSource", leadSourceCode);

            String QueSampl = "QL_SOURCINGINFO_LeadID,QL_SOURCINGINFO_LeadDate,QL_SOURCINGINFO_EmployeeId,QL_SOURCINGINFO_EmployeeName,QL_SOURCINGINFO_ParkingBranchCode,QL_SOURCINGINFO_ParkingBranch,QL_SOURCINGINFO_LeadSource";
            pcm.controlvisiblity(ifr, QueSampl);
            pcm.controlDisable(ifr, QueSampl);
            String QueSamp2 = "QL_SOURCINGINFO_RefCustomerName_patternString,QL_SOURCINGINFO_RefCust_CIFID,QL_SOURCINGINFO_TEST,QL_SOURCINGINFO_Search_Term,QL_SOURCINGINFO_Search_Criteria,QL_SOURCINGINFO_AgentName,QL_SOURCINGINFO_AgentCode,QL_SOURCINGINFO_BuilderName,QL_SOURCINGINFO_BuilderCode,QL_SOURCINGINFO_OtherReference,QL_SOURCINGINFO_SourceOfficerDesignation,QL_SOURCINGINFO_SourcingOfficerName"
                    + ",QL_SOURCINGINFO_SourcingEmployeeId,QL_SOURCINGINFO_CampaignName,QL_SOURCINGINFO_CampaignCode,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_EmpNameID,QL_SOURCINGINFO_EmpNameID,QL_SOURCINGINFO_RefCustomerName,BTN_SEARCHSOURCE,SourceWise,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch";

            pcm.controlinvisiblity(ifr, QueSamp2);

            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  Reviewer");
                pcm.controlDisable(ifr, QueSampl);
                pcm.controlinvisiblity(ifr, QueSamp2);
                pcm.controlvisiblity(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  Sanction");
                pcm.controlinvisiblity(ifr, QueSamp2);
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("PostSanction")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  PostSanction");
                pcm.controlinvisiblity(ifr, QueSamp2);
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Convenor")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode:: Convenor ");
                pcm.controlinvisiblity(ifr, QueSamp2);
                pcm.controlDisable(ifr, QueSampl);
            }

            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  Sanction");
                pcm.controlinvisiblity(ifr, QueSamp2);
                pcm.controlDisable(ifr, QueSampl);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode:: Convenor ");
                pcm.controlinvisiblity(ifr, QueSamp2);

                pcm.controlDisable(ifr, QueSampl);
            }

            Log.consoleLog(ifr, " OnChangeSourcingInfoSection Budget Code End");
        }
        return null;
    }

    public void OnLoadKYC(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilitiesMaker Budget ");
            String visibleFields = "QNL_BASIC_INFO_CNL_KYC2_KYC_ID,QNL_BASIC_INFO_CNL_KYC2_KYC_No";
            pcm.controlvisiblity(ifr, visibleFields);
            String nonVisiblesFields = "KYC_Electricity_Provider,KYC_Consumer_Number,QNL_BASIC_INFO_CNL_KYC2_DateofIssue,QNL_BASIC_INFO_CNL_KYC2_PlaceOfIssue,QNL_BASIC_INFO_CNL_KYC2_DateOfExpiry,QNL_BASIC_INFO_CNL_KYC2_ValidatedOn,QNL_BASIC_INFO_CNL_KYC2_ValidatedBy,QNL_BASIC_INFO_CNL_KYC2_ValidationStatus,BTN_KYC_Validate,BTN_KYC_SendOtp,Aadhar_OTP,KYC_BTN_FetchDetails,AadharVaultRefId,"
                    + "DateofBirth,Name_As_Per_Aadhar,Name_As_Per_PAN,Name_As_Per_VoterID,Name_As_Per_Passport";
            pcm.controlinvisiblity(ifr, nonVisiblesFields);
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                pcm.controlvisiblity(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("PostSanction")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  PostSanction");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  PostSanction");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }

            if (ActivityName.equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  Sanction");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }
            if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode:: Convenor ");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }

            if (ActivityName.equalsIgnoreCase("Deviation")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode:: Deviation ");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }

            if (ActivityName.equalsIgnoreCase("Reviewer")) {
                Log.consoleLog(ifr, "inside OnChangeSourcingInfoSectionMaker BudgetBkoffCustomCode::  PostSanction");

                pcm.controlDisable(ifr, visibleFields);
                pcm.controlinvisiblity(ifr, nonVisiblesFields);
                pcm.controlvisiblity(ifr, visibleFields);
            }
        }
    }

    public String autopopulate_FinalEligibility(IFormReference ifr) {
        Log.consoleLog(ifr, "Entered into==>>>");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        // String query = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        LoanEligibilityCheck lec = new LoanEligibilityCheck();
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {

            Log.consoleLog(ifr, "Inside Budget Final Eligibility");
            try {
                String RequestedLoanAmount = "";
                String LoanAMTTenureQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTTenureQuery, "Execute query for fetching Slider Loan amount,Tenure data from portal in principal");
                if (LoanAmountList.size() > 0) {
                    RequestedLoanAmount = LoanAmountList.get(0).get(1);
                    Log.consoleLog(ifr, "RequestedLoanAmount :: " + RequestedLoanAmount);
                }

                //int countOfGrid = ifr.getDataFromGrid("ALV_FINAL_ELIGIBILITY").size();
//            ifr.setValue("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", RequestedLoanAmount);
//            ifr.setStyle("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", "disable", "true");
                //String EligibityQuery = "SELECT PID FROM LOS_LIN_FINAL_ELIGIBILITY WHERE PID='" + ProcessInstanceId + "'";
//            String EligibityQuery = ConfProperty.getQueryScript("CheckFinalEligibilityBO").replaceAll("#PID#", ProcessInstanceId);
//            List<List<String>> EligibityCount = cf.mExecuteQuery(ifr, EligibityQuery, "Execute query for fetching customer data");
//            Log.consoleLog(ifr, "EligibityCount ::" + EligibityCount.size());
                //String IncomeQuery = "SELECT GROSSSALARY, NETSALARY , DEDUCTIONMONTH FROM LOS_NL_Occupation_INFO a INNER JOIN  LOS_NL_BASIC_INFO b ON a.F_KEY=b.F_KEY WHERE b.APPLICANTTYPE ='B' AND b.PID='" + ProcessInstanceId + "'";
                String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoCB").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching income data from portal");
                String GrossSalary = IncomeDtsPortal.get(0).get(0);
                String NETSALARY = IncomeDtsPortal.get(0).get(1);
                String DeductionMonthly = IncomeDtsPortal.get(0).get(2);
                Log.consoleLog(ifr, "GROSSSALARY ::" + GrossSalary);
                Log.consoleLog(ifr, "NETSALARY ::" + NETSALARY);
                Log.consoleLog(ifr, "DEDUCTIONMONTH ::" + DeductionMonthly);

                //String TotalEmiQuery = "SELECT  SUM(EMIAMT) FROM LOS_NL_AL_LIAB_VAL WHERE PID='#PID#' AND CONSIDERELIGIBILITY ='Yes' 
                //AND APPLICANTTYPE = (select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='#applicanttype#'  and PID='#PID#')";
                String TotalEmiQuery = ConfProperty.getQueryScript("GetTotalEmiFromLiability").replaceAll("#PID#", ProcessInstanceId).replaceAll("#applicanttype#", "B");
                List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");

                String cibiloblig = "";
                if (!TotalEmi.isEmpty()) {
                    cibiloblig = TotalEmi.get(0).get(0);
                    Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
                } else {
                    cibiloblig = "0.00";
                    Log.consoleLog(ifr, "cibiloblig TotalEmi Considered as ::" + cibiloblig);
                }
                Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

//                    CommonMethods cm = new CommonMethods();
//                    String BureauDataResponseBorrower = cm.getMaxTotalEMIAmountCICDatas(ifr, "B");
//                    String[] bSplitter = BureauDataResponseBorrower.split("-");
//                    String bureauTypeB = bSplitter[0];
//                    String ApplicantTypeB = bSplitter[1];
//                    String MaxTotalEmiAmtB = bSplitter[2];
//                    Log.consoleLog(ifr, "cibiloblig TotalEmi MaxTotalEmiAmtB::" + MaxTotalEmiAmtB);
                //Commented by Aravindh.K.K on 29/07/2024 
                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String loanTenure = null;
                String loanROI = "";
                //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                // String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
                String tenureData_Query = "select EFFECTIVEROI,TENURE from los_nl_proposed_facility where PID='" + ProcessInstanceId + "'";
                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, " EFFECTIVEROI, TENURE Query From  proposed facility:");
                if (list1.size() > 0) {
                    loanROI = list1.get(0).get(0);
                    loanTenure = list1.get(0).get(1);
                }
                Log.consoleLog(ifr, "loanTenure : " + loanTenure);

                String Prodcapping = null;
                //String ProdCapping_Query = "select MAXLOANAMOUNT from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
                List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
                if (ProdcappingList.size() > 0) {
                    Prodcapping = ProdcappingList.get(0).get(0);
                }
                Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

                JSONParser parser = new JSONParser();
                JSONObject EligibilityDataObj = new JSONObject();
                HashMap hm = new HashMap();
                hm.put("GrossSalary", String.valueOf(GrossSalary));
                hm.put("DeductionMonthly", String.valueOf(DeductionMonthly));
                hm.put("cibiloblig", String.valueOf(cibiloblig));
                hm.put("tenure", String.valueOf(loanTenure));
                hm.put("roi", String.valueOf(loanROI));
                hm.put("loancap", String.valueOf(Prodcapping));

                String EligibilityDatas = pcm.getAmountForEligibilityCheckCBperfiosBO(ifr, hm);
                Log.consoleLog(ifr, "EligibilityDatas : " + EligibilityDatas);
                try {
                    EligibilityDataObj = (JSONObject) parser.parse(EligibilityDatas);
                } catch (ParseException ex) {

                    //   Logger.getLogger(AcceleratorActivityManagerCode.class.getName()).log(Level.SEVERE, null, ex);
                }

                Log.consoleLog(ifr, "Before EligibilityDataObj : " + EligibilityDataObj);

                String grosssalaryipBG = (String) EligibilityDataObj.get("grosssalaryip");
                String deductionsalaryBG = (String) EligibilityDataObj.get("deductionsalary");
                String cbCibilObligBG = (String) EligibilityDataObj.get("cbCibilOblig");
                String netsalaryBG = (String) EligibilityDataObj.get("netsalary");
                String netIncomeBG = (String) EligibilityDataObj.get("netIncome");
                String ftTenureBG = (String) EligibilityDataObj.get("ftTenure");
                String ftRoiBG = (String) EligibilityDataObj.get("ftRoi");
                String loanAmountBG = (String) EligibilityDataObj.get("loanAmount");
                String sixtimesgrosssalBG = (String) EligibilityDataObj.get("sixtimesgrosssal");
                String prodspeccappingBG = (String) EligibilityDataObj.get("prodspeccapping");
                String finaleligibilityBG = (String) EligibilityDataObj.get("finaleligibility");

                Log.consoleLog(ifr, "Before EligibilityDataObj : ");
                String grosssalaryip = String.valueOf(Math.round(Double.parseDouble(grosssalaryipBG)));
                String deductionsalary = String.valueOf(Math.round(Double.parseDouble(deductionsalaryBG)));
                String cbCibilOblig = String.valueOf(Math.round(Double.parseDouble(cbCibilObligBG)));
                //String netsalary = String.valueOf(Math.round(Double.parseDouble(netsalaryBG)));
                //String netIncome = String.valueOf(Math.round(Double.parseDouble(netIncomeBG)));
                String ftTenure = String.valueOf(Math.round(Double.parseDouble(ftTenureBG)));
                //String ftRoi = String.valueOf(Math.round(Double.parseDouble(ftRoiBG)));
                //String loanAmount = String.valueOf(Math.round(Double.parseDouble(loanAmountBG)));
                String sixtimesgrosssal = String.valueOf(Math.round(Double.parseDouble(sixtimesgrosssalBG)));
                String prodspeccapping = String.valueOf(Math.round(Double.parseDouble(prodspeccappingBG)));
                //  String finaleligibility = String.valueOf(Math.round(Double.parseDouble(finaleligibilityBG)));

                double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibilityBG) / 1000) * 1000;
                String finalEligibleLoanAmount = String.valueOf(Math.round(finaleligibilityDouble));
                Log.consoleLog(ifr, "After rounded finalEligibleLoanAmount : " + finalEligibleLoanAmount);
                //Modified by Aravindh on 06/07/24

////                    String finaleligibilityCommon = null;
////                    try {
////                        finaleligibilityCommon = lec.getAmountForEligibilityCheck(ifr, prodCode, subProdCode, loandata);
////                        Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibilityCommon);
////                    } catch (Exception ex) {
////                        // Logger.getLogger(AcceleratorActivityManagerCode.class.getName()).log(Level.SEVERE, null, ex);
////                    }
//Commented By Aravindh on 06/07/24 (Not Used for Phase2)
//                    String loanAmountBGCommon = null;
//                    double finaleligibilityDoubled = Math.floor(Double.parseDouble(loanAmountBG) / 1000) * 1000;
//                    loanAmountBGCommon = String.valueOf(Math.round(finaleligibilityDoubled));
//                    Log.consoleLog(ifr, "After rounded loanAmountBGCommon : " + loanAmountBGCommon);
                String grossmultiple = ConfProperty.getCommonPropertyValue("GROSSMULTIBUDGET");
                Log.consoleLog(ifr, "KEY FROM COMMONPROPERTIES FOR GROSSMULTIBUDGET " + grossmultiple);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryip);
                obj.put("QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalary);
                obj.put("QNL_LA_FINALELIGIBILITY_Obligations", cbCibilOblig);
                obj.put("QNL_LA_FINALELIGIBILITY_NetIncome", netIncomeBG);
                //obj.put("QNL_LA_FINALELIGIBILITY_Tenure", ftTenure);
                obj.put("QNL_LA_FINALELIGIBILITY_Requested_Tenure", ftTenure);
                obj.put("QNL_LA_FINALELIGIBILITY_ROI", ftRoiBG);
                obj.put("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netsalaryBG);
                obj.put("QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", loanAmountBG);
                //obj.put("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", grossmultiple);
                obj.put("QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount", prodspeccapping);

                obj.put("QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finalEligibleLoanAmount);
                obj.put("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", RequestedLoanAmount);

                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);

                int FinalEligibilityCount = ifr.getDataFromGrid("ALV_FINAL_ELIGIBILITY").size();
                if (FinalEligibilityCount == 0) {
                    //ifr.clearTable("ALV_FINAL_ELIGIBILITY");
                    ifr.addDataToGrid("ALV_FINAL_ELIGIBILITY", jsonarr);
                    Log.consoleLog(ifr, "==> Calculation addDataToGrid to ALV_FINAL_ELIGIBILITY <==");

                } else {
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryip);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalary);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_Obligations", cbCibilOblig);

                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_NetIncome", netIncomeBG);
                    //ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_Tenure", ftTenure);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_ROI", ftRoiBG);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netsalaryBG);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", loanAmountBG);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount", prodspeccapping);

                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finalEligibleLoanAmount);
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, "QNL_LA_FINALELIGIBILITY_LoanAmountrequested", RequestedLoanAmount);
                    Log.consoleLog(ifr, "==> Calculation setTableCellValue to ALV_FINAL_ELIGIBILITY <==");

                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, 1, finalEligibleLoanAmount);//Grid outside Eligible Loan Amount
                    ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, 3, ftRoiBG);//Grid outside roi 

                }
            } catch (Exception ex) {
                Log.consoleLog(ifr, "Exception IN OnLoadFinalEligibility " + ex);
                return RLOS_Constants.ERROR;
            }
        }
        return "";
    }

//OnchangeDisbursement added by sharon
    public void OnchangeDisbursement(IFormReference ifr) {

        Log.consoleLog(ifr, "inside try block::::OnchangeDisbursement::::: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::OnchangeDisbursement::::" + ProcessInstanceId);

        String disableFields = "QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_CustomerName,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_InstallmentAmount";
        Log.consoleLog(ifr, "disableFields:::OnchangeDisbursement::::" + disableFields);
        pcm.controlDisable(ifr, disableFields);
        String inVisible = "QNL_DISBURSMENT_FixedTerm";
        Log.consoleLog(ifr, "inVisible:::OnchangeDisbursement::::" + inVisible);
        pcm.controlinvisiblity(ifr, inVisible);

        //CustomerId//Fullname //salaryAccount
        String CustomerId = "";
        String Fullname = "";
        String salaryAccount = "";
        String query = "select customerid,FULLNAME,SELECTSALARYACCOUNT from los_nl_basic_info where PID = '" + ProcessInstanceId + "' and APPLICANTTYPE='B'";
        List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
        //String salquery = "select ACCTNUMBER from LOS_NL_CASA_ASSET_VAL where PID = '" + ProcessInstanceId + "'";
        //List<List<String>> sallist = cf.mExecuteQuery(ifr, salquery, "Get salquery ID:");
        if (list.size() > 0) {
            CustomerId = list.get(0).get(0);
            Fullname = list.get(0).get(1);
            //salaryAccount = sallist.get(0).get(0);
        }
        Log.consoleLog(ifr, "CustomerId:::OnchangeDisbursement::::" + CustomerId);
        ifr.setValue("QNL_DISBURSMENT_CustomerID", CustomerId);
        Log.consoleLog(ifr, " OnchangeDisbursement Fullname;:" + Fullname);
        ifr.setValue("QNL_DISBURSMENT_CustomerName", Fullname);
        salaryAccount = bpcc.Accountdetail(ifr);
        Log.consoleLog(ifr, "salaryAccount:::OnchangeDisbursement::::" + salaryAccount);
        ifr.setValue("QNL_DISBURSMENT_SalaryAccountnumber", salaryAccount);

        //Installment Amount,No.of Installment
        String INSTALMENTAMOUNT = "";
        String NoOfINSTALMENT = "";
        String INSTALMENTQuery = "select INSTALLMENTAMT,TENURE from los_nl_proposed_facility where  PID = '" + ProcessInstanceId + "'";
        List<List<String>> INSTALMENTList = cf.mExecuteQuery(ifr, INSTALMENTQuery, "Get INSTALMENTAMOUNT,No.of Installment:");
        if (!INSTALMENTList.isEmpty()) {
            INSTALMENTAMOUNT = INSTALMENTList.get(0).get(0);
            NoOfINSTALMENT = INSTALMENTList.get(0).get(1);
        }
        Log.consoleLog(ifr, "INSTALMENTAMOUNT:::OnchangeDisbursement::::" + INSTALMENTAMOUNT);
        Log.consoleLog(ifr, "NoOfINSTALMENT:::OnchangeDisbursement::::" + NoOfINSTALMENT);
        ifr.setValue("QNL_DISBURSMENT_InstallmentAmount", INSTALMENTAMOUNT);
        ifr.setValue("QNL_DISBURSMENT_NoofInstallment", NoOfINSTALMENT);

        //Drawdown
        String drawDown = "Yes";
        Log.consoleLog(ifr, "drawDown:::OnchangeDisbursement::::" + drawDown);
        ifr.setValue("QNL_DISBURSMENT_Drawdown", drawDown);

    }

    public void beneficiaryDetails(IFormReference ifr) {

        Log.consoleLog(ifr, "inside beneficiaryDetails: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside beneficiaryFields Disbursement Maker stage: ");
                String enableFields = "QNL_BENEFICIARY_DETAILS_DisbursalTo,QNL_BENEFICIARY_DETAILS_BeneficiaryName,QNL_BENEFICIARY_DETAILS_Amount,QNL_BENEFICIARY_DETAILS_PaymentMode,QNL_BENEFICIARY_DETAILS_DisburseDate";
                //pcm.controlEnable(ifr, enableFields);
                String invisibleFields = "QNL_BENEFICIARY_DETAILS_Status,QNL_BENEFICIARY_DETAILS_DisburseDate,QNL_BENEFICIARY_DETAILS_StartDate,QNL_BENEFICIARY_DETAILS_ExpiryDate,QNL_BENEFICIARY_DETAILS_FromAccount";
                pcm.controlinvisiblity(ifr, invisibleFields);
                String disableFields = "QNL_BENEFICIARY_DETAILS_DisbursalTo,QNL_BENEFICIARY_DETAILS_BeneficiaryName,QNL_BENEFICIARY_DETAILS_Amount,QNL_BENEFICIARY_DETAILS_PaymentMode,QNL_BENEFICIARY_DETAILS_DisburseDate";
                pcm.controlDisable(ifr, disableFields);
                pcm.controlvisiblity(ifr, disableFields);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside beneficiaryFields Disbursement Checker stage: ");
                String enableFields = "QNL_BENEFICIARY_DETAILS_DisbursalTo,QNL_BENEFICIARY_DETAILS_BeneficiaryName,QNL_BENEFICIARY_DETAILS_Amount,QNL_BENEFICIARY_DETAILS_PaymentMode,QNL_BENEFICIARY_DETAILS_DisburseDate";
                //pcm.controlEnable(ifr, enableFields);
                String invisibleFields = "QNL_BENEFICIARY_DETAILS_StartDate,QNL_BENEFICIARY_DETAILS_ExpiryDate,QNL_BENEFICIARY_DETAILS_FromAccount";
                pcm.controlinvisiblity(ifr, invisibleFields);
                String disableFields = "QNL_BENEFICIARY_DETAILS_DisbursalTo,QNL_BENEFICIARY_DETAILS_BeneficiaryName,QNL_BENEFICIARY_DETAILS_Amount,QNL_BENEFICIARY_DETAILS_PaymentMode,QNL_BENEFICIARY_DETAILS_DisburseDate,QNL_BENEFICIARY_DETAILS_Status,QNL_BENEFICIARY_DETAILS_DisburseDate";
                pcm.controlDisable(ifr, disableFields);
                pcm.controlvisiblity(ifr, disableFields);
            }
        }
    }

    public void beneficiaryFrameChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside beneficiaryDetails: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside beneficiaryColumn stage: ");
                ifr.setStyle("add_ALV_BENEFICIARY_DETAILS", "disable", "visible");
                ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "7", false);
                ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "9", false);
                ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "8", false);

                int beneficiaryGridCount = ifr.getDataFromGrid("ALV_BENEFICIARY_DETAILS").size();
                Log.consoleLog(ifr, "beneficiaryGrid Count ----- > " + beneficiaryGridCount);

//                if (beneficiaryGridCount == 0) {
//                    Log.consoleLog(ifr, " Inside beneficiaryGrid Count ");
//                    DisbursementDataToBeneficiaryGrid(ifr);
//                }
            }

        }
    }

//    Added by Aravindh on 11/07/2024 For Autopopulation of Beneficiary Grid
    public void DisbursementDataToBeneficiaryGrid(IFormReference ifr) {
        Log.consoleLog(ifr, "inside DisbursementDataToBeneficiaryGrid: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);

        String ApprovedLoanAmt = "";
        String DisbursmentAmtQuery = ConfProperty.getQueryScript("DisbursmentproposedLoanAmont").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> DisbursmentAmountList = cf.mExecuteQuery(ifr, DisbursmentAmtQuery, "Execute query for fetching DisbursmentAmt Query : ");
        if (!DisbursmentAmountList.isEmpty()) {
            ApprovedLoanAmt = DisbursmentAmountList.get(0).get(0);
            Log.consoleLog(ifr, "ApprovedLoanAmt --- >" + ApprovedLoanAmt);
        }
        String DisbursalTo = "";
        String DisbursalToQuery = "select BENEFICIARIES,BENEID from LOS_M_BENEFICIARIES where BENEID ='BENE2'";
        List<List<String>> DisbursalToQueryList = cf.mExecuteQuery(ifr, DisbursalToQuery, "Execute query for fetching DisbursalToQuery Query : ");
        if (!DisbursalToQueryList.isEmpty()) {
            DisbursalTo = DisbursalToQueryList.get(0).get(1);
            Log.consoleLog(ifr, "DisbursalTo --- > " + DisbursalTo);
        }

        String BorrowerName = "";
        String CustomerNameQuery = ConfProperty.getQueryScript("CustomerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPLICANTTYPE#", "B");
        List<List<String>> CustomerNameQueryList = cf.mExecuteQuery(ifr, CustomerNameQuery, "Execute query for fetching CustomerNameQuery Query : ");
        if (!CustomerNameQueryList.isEmpty()) {
            BorrowerName = CustomerNameQueryList.get(0).get(0);
            Log.consoleLog(ifr, "BorrowerName --- > " + BorrowerName);
        }

        String PaymentMode = "";
        String RepaymentTypeQuery = ConfProperty.getQueryScript("REPAYMENTTYPEQUERY").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> RepaymentTypeQueryList = cf.mExecuteQuery(ifr, RepaymentTypeQuery, "Execute query for fetching RepaymentType Query : ");
        if (!RepaymentTypeQueryList.isEmpty()) {
            PaymentMode = RepaymentTypeQueryList.get(0).get(0);
            Log.consoleLog(ifr, "PaymentMode --- > " + PaymentMode);
        }

        Log.consoleLog(ifr, "Inside adddatatogrid :: ");

        JSONArray jsonArr = new JSONArray();
        JSONObject Obj = new JSONObject();
        Obj.put("QNL_BENEFICIARY_DETAILS_DisbursalTo", "Customer");
        Obj.put("QNL_BENEFICIARY_DETAILS_BeneficiaryName", BorrowerName);
        Obj.put("QNL_BENEFICIARY_DETAILS_Amount", ApprovedLoanAmt);
        Obj.put("QNL_BENEFICIARY_DETAILS_PaymentMode", PaymentMode);
        //Obj.put("QNL_BENEFICIARY_DETAILS_Status", loanTenure);
        //Obj.put("QNL_BENEFICIARY_DETAILS_DisburseDate", );

        jsonArr.add(Obj);
        Log.consoleLog(ifr, "BENEFICIARY DETAILS JSONARRAY RESULT::" + jsonArr);
        ((IFormAPIHandler) ifr).addDataToGrid("ALV_BENEFICIARY_DETAILS", jsonArr, true);

    }

    public void cifListViewLoad(IFormReference ifr) {
        Log.consoleLog(ifr, "inside cifListViewLoad: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside cifListViewLoad: ");
                String visiblefield = "QNL_CustomerCreation_details_CIFNUMBER";
                pcm.controlvisiblity(ifr, visiblefield);
                String invisibleFields = "QNL_CustomerCreation_details_RelationshipManager,QNL_CustomerCreation_details_FaxNumber,QNL_CustomerCreation_details_PhoneBusiness,QNL_CustomerCreation_details_Occupancy,QNL_CustomerCreation_details_DomesticRisk,QNL_CustomerCreation_details_CrossBorderRisk,QNL_CustomerCreation_details_RelativeCode,QNL_CustomerCreation_details_ConstitutionCode,QNL_CustomerCreation_details_TradeCustomer,QNL_CustomerCreation_details_KeyPersonName,QNL_CustomerCreation_details_KeyPersonContactNumber,QNL_CustomerCreation_details_MaidenName";
                pcm.controlinvisiblity(ifr, invisibleFields);

                //get customer id as cif number
                String cifnumber = pcm.getCustomerIDCB(ifr, "B");
                Log.consoleLog(ifr, "cifnumber:::::::" + cifnumber);
                JSONObject obj = new JSONObject();
                obj.put("QNL_CustomerCreation_details_CIFNUMBER", cifnumber);
                Log.consoleLog(ifr, "inside obj: " + obj);

            }

        }

    }

    public void cifFrameChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside beneficiaryDetails: ");
        JSONArray arr = new JSONArray();
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside onchangeFrame: ");
                ifr.setStyle("ALV_CIFNumber", "visible", "false");
                ifr.setStyle("BTN_CIF_CREATION", "visible", "false");
                int gridCount = ifr.getDataFromGrid("ALV_CustomerCreation_Detail").size();
                if (gridCount == 0) {
                    String ApplicantType = "";
                    String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b  inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID = '" + ProcessInstanceId + "' and (c.ApplicantType='B')";
                    Log.consoleLog(ifr, "Query1 data::" + Query1);
                    List<List<String>> resultData = ifr.getDataFromDB(Query1);
                    if (!resultData.isEmpty()) {
                        ApplicantType = resultData.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "resultData::" + resultData.toString());

                    Log.consoleLog(ifr, "inside onchangeFrame: ");

                    String cifnumber = pcm.getCustomerIDCB(ifr, "B");
                    Log.consoleLog(ifr, "cifnumber:::::::" + cifnumber);
                    JSONObject obj = new JSONObject();
                    obj.put("QNL_CustomerCreation_details_CIFNUMBER", cifnumber);
                    obj.put("QNL_CustomerCreation_details_ApplicantType", ApplicantType);
                    arr.add(obj);
                    Log.consoleLog(ifr, "inside obj: " + obj);
                    ((IFormAPIHandler) ifr).addDataToGrid("ALV_CustomerCreation_Detail", arr, true);
                }
            }

        }

    }

    /*Added By: Veena.K
     Date: 13-06-2024
     Description: To replace where tags in DB Query.*/
    public String DBqryWhereTags(String query, String tags[], String data[]) {
        if (tags != null && data != null && (tags.length == data.length)) {
            for (int i = 0; i < tags.length; i++) {
                try {
                    query = query.replaceAll(tags[i], data[i]);
                } catch (Exception e) {
                    query = query.replaceAll(tags[i], "");
                }
            }
        }
        return query;
    }

    /*Added By: Veena.K
     Date: 13-06-2024
     Description: To get the data from DB - Common Method.*/
    public String GetDataFromDbCommon(IFormReference ifr, String QueryName, String tags[], String data[]) {
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String Data = "";
            Log.consoleLog(ifr, "Inside GetDataFromDbCommon::PID:: " + PID + " QueryName:: " + QueryName);
            String Query = ConfProperty.getQueryScript(QueryName);
            Log.consoleLog(ifr, "Query:: " + Query);
            Query = DBqryWhereTags(Query, tags, data);
            Log.consoleLog(ifr, "Query after replacing:: " + Query);
            List<List<String>> Result = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "Result:: " + Result);

            if (Result.size() > 1) {
                Log.consoleLog(ifr, "Result greater than 1:: " + Result);
                return Result.toString();
            }
            for (List<String> rowData : Result) {
                if (rowData.size() > 1) {
                    Log.consoleLog(ifr, "rowData greater than 1:: " + rowData);
                    return rowData.toString();
                } else {
                    Log.consoleLog(ifr, "rowData greater equal to 1:: " + rowData);
                    Data = rowData.get(0);
                    Log.consoleLog(ifr, "Data:: " + Data);
                    return Data;
                }
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception inside GetFinalLoanEligibleAmt:: " + e);
            Log.errorLog(ifr, "Exception inside GetFinalLoanEligibleAmt:: " + e);
        }
        return "";
    }

    /*Added By: Veena.K
    Date: 13-06-2024
    Description: To derieve the approval matrix.*/
    public String ApprovalMatrixDerivationUpdated(IFormReference ifr, String CallFrom, String LoanSelected) {
        String ReturnMsg = "";
        try {
            HashMap<String, String> LoanMapping = new HashMap<String, String>();
            LoanMapping.put("Pre-Approved Personal Loan", "PAPL");
            LoanMapping.put("Loan Against Deposit", "LAD");
            LoanMapping.put("VEHICLE LOAN", "VL");
            LoanMapping.put("Canara Budget", "CB");
            LoanMapping.put("Canara Pension", "CP");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            if (CallFrom.equalsIgnoreCase("Branch Maker")) {
                //String tags[] = {"##PID##", "##PartyType##", "##ApplicantType##"};
                //String data[] = {PID, "Borrower", "B"};

                String ProcessingBranch = (String) ifr.getValue("QL_SOURCINGINFO_ParkingBranchCode");
                Log.consoleLog(ifr, "Inside ApprovalMatrixDerivationUpdated::PID:: " + PID + " CallFrom:: " + CallFrom
                        + " LoanSelected:: " + LoanSelected + " LoanMapping:: " + LoanMapping
                        + " ProcessingBranch:: " + ProcessingBranch);

                String LoggedinUser = ifr.getUserName();
                Log.consoleLog(ifr, "LoggedinUser:: " + LoggedinUser);

                String tags[] = new String[]{"##LoggedinUser##"};
                String data[] = new String[]{LoggedinUser};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String LoggedInUserGrp = GetDataFromDbCommon(ifr, "UserGroupFetchQuery", tags, data);
                Log.consoleLog(ifr, "LoggedInUserGrp:: " + LoggedInUserGrp);

                if (LoggedInUserGrp.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("UserNotFoundAlert");
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;
                }

                tags = new String[]{"##GROUPID##"};
                data = new String[]{LoggedInUserGrp};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String LoggedInUserOrg = GetDataFromDbCommon(ifr, "OrganizationLevelFetchQuery", tags, data);
                Log.consoleLog(ifr, "LoggedInUserOrg:: " + LoggedInUserOrg);
                if (LoggedInUserOrg.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("OrganizationNotfoundAlert")
                            .replace("##Grp##", LoggedInUserGrp)
                            .replace("##stage##", "Logged In User");
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;
                }

                tags = new String[]{"##OrgName##"};
                data = new String[]{LoggedInUserOrg};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String LoggedInUserHierarchy = GetDataFromDbCommon(ifr, "HirarchyLevelFetchQuery", tags, data);
                Log.consoleLog(ifr, "LoggedInUserHierarchy:: " + LoggedInUserHierarchy);

                if (LoggedInUserHierarchy.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("HierarchyNotfoundAlert")
                            .replace("##Org##", LoggedInUserOrg)
                            .replace("##stage##", "Logged In User");
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;

                }

                Log.consoleLog(ifr, "Current Application processinng Org:: " + LoggedInUserOrg
                        + " Current Application processinng Hierarchy:: " + LoggedInUserHierarchy);

                Log.consoleLog(ifr, "Current Application processinng Org:: " + LoggedInUserOrg
                        + " Current Application processinng Hierarchy:: " + LoggedInUserHierarchy);

                tags = new String[]{"##ProcessingBranch##"};
                data = new String[]{ProcessingBranch};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String RAHFlag = GetDataFromDbCommon(ifr, "BranchRAHCheck", tags, data);
                Log.consoleLog(ifr, "RAHFlag:: " + RAHFlag);

                if (RAHFlag.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("RAHFlagNotFoundAlert")
                            .replace("##ProcessingBranch##", ProcessingBranch);
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;
                }

                tags = new String[]{"##PID##", "##PartyType##", "##ApplicantType##"};
                data = new String[]{PID, "Borrower", "B"};

                String APStr = ConfProperty.getCommonPropertyValue(LoanMapping.get(LoanSelected)
                        + "ApplicableParamsApprovalMatrix");
                String[] APArr = APStr.split(",");
                Log.consoleLog(ifr, "ApplicableParamsStr:: " + Arrays.toString(APArr)
                        + " ApplicableParams:: " + Arrays.toString(APArr));
                ArrayList<String> ApplicableParams = new ArrayList<>(Arrays.asList(APArr));

                ArrayList<String> ApplicableParamValues = new ArrayList<>();

                for (String ap : ApplicableParams) {
                    String ParamValue = GetDataFromDbCommon(ifr, ap + "FetchQuery", tags, data);
                    Log.consoleLog(ifr, "ParamValue:: " + ParamValue);
                    ApplicableParamValues.add(ParamValue);
                }

                Log.consoleLog(ifr, "ApplicableParamValues:: " + ApplicableParamValues);

                String FinalAuthorityFetchQ = ConfProperty.getQueryScript("FinalAuthorityPartFetchQuery")
                        + " " + ConfProperty.getQueryScript(LoanMapping.get(LoanSelected) + "WhereConditions");
                Log.consoleLog(ifr, "FinalAuthorityFetchQ b4 replacing:: " + FinalAuthorityFetchQ);
                for (int i = 0; i < ApplicableParams.size(); i++) {
                    FinalAuthorityFetchQ = FinalAuthorityFetchQ.replace("##" + ApplicableParams.get(i) + "##",
                            ApplicableParamValues.get(i));
                }
                Log.consoleLog(ifr, "FinalAuthorityFetchQ:: " + FinalAuthorityFetchQ);
                List<List<String>> FinalApprovingAuthorityR = cf.mExecuteQuery(ifr, FinalAuthorityFetchQ, "FinalAuthorityFetchResult:: ");
                String FinalApprovingAuthority = "";
                Log.consoleLog(ifr, "FinalApprovingAuthorityR:: " + FinalApprovingAuthorityR);
                if (FinalApprovingAuthorityR.size() > 0) {
                    FinalApprovingAuthority = FinalApprovingAuthorityR.get(0).get(0);
                }
                Log.consoleLog(ifr, "FinalApprovingAuthority 1st time:: " + FinalApprovingAuthority);

                if (FinalApprovingAuthority.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "FinalApprovingAuthority empty case: ");
                    ReturnMsg = ConfProperty.getCommonPropertyValue("ApprovalMatrixNotFoundAlertUpdated");

                    for (int j = 0; j < ApplicableParams.size(); j++) {
                        ReturnMsg = ReturnMsg + " " + ApplicableParams.get(j) + ": " + ApplicableParamValues.get(j);
                    }
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;

                }
                Log.consoleLog(ifr, "BudgetBkOffCustCode : ApprovalMatrixDerivationUpdated : ProcessingBranch::" + ProcessingBranch + " ,FinalApprovingAuthority : "+ FinalApprovingAuthority);
                String OrgFetchQ = ConfProperty.getQueryScript("FinalOrganizationLevelFetchQuery")
                        .replace("##ProcessingBranch##", ProcessingBranch)
                        .replace("##FinalApprovingAuthority##", FinalApprovingAuthority);
                Log.consoleLog(ifr, "BudgetBkOffCustCode : ApprovalMatrixDerivationUpdated : RAHFlag::" + RAHFlag);
                if (RAHFlag.equalsIgnoreCase("Y")) {
                    OrgFetchQ = OrgFetchQ.replace("##RAHBranchCondition##", ConfProperty.getQueryScript("NotBranchCondition"));
                } else {
                    OrgFetchQ = OrgFetchQ.replace("##RAHBranchCondition##", ConfProperty.getQueryScript("NotRAHCondition"));
                }
                Log.consoleLog(ifr, "OrgFetchQ:: " + OrgFetchQ);
                List<List<String>> OrgFetchR = cf.mExecuteQuery(ifr, OrgFetchQ, "OrgFetchResult:: ");
                String FinalAuthorityOrg = "";
                Log.consoleLog(ifr, "OrgFetchR:: " + OrgFetchR);
                if (OrgFetchR.size() > 0) {
                    FinalAuthorityOrg = OrgFetchR.get(0).get(0);
                    Log.consoleLog(ifr, "FinalAuthorityOrg:: " + FinalAuthorityOrg);

                    tags = new String[]{"##FinalApprovingAuthority##", "##FinalAuthorityOrg##"};
                    data = new String[]{FinalApprovingAuthority, FinalAuthorityOrg};
                    Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                    FinalApprovingAuthority = GetDataFromDbCommon(ifr, "FinalAuthorityDesgFiltering", tags, data);
                    Log.consoleLog(ifr, "FinalApprovingAuthority 2nd time:: " + FinalApprovingAuthority);

                    if (FinalAuthorityOrg.equalsIgnoreCase("RAH")
                            || FinalAuthorityOrg.equalsIgnoreCase("Branch")) {
                        Log.consoleLog(ifr, "Inside Branch or RAH condition::");
                        tags = new String[]{"##ProcessingBranch##", "##FinalApprovingAuthority##"};
                        data = new String[]{ProcessingBranch, FinalApprovingAuthority};
                        Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                        FinalApprovingAuthority = GetDataFromDbCommon(ifr, "BranchRAHHeadFetch", tags, data);
                        Log.consoleLog(ifr, "FinalApprovingAuthority 3rd time:: " + FinalApprovingAuthority);

                        if (FinalApprovingAuthority.equalsIgnoreCase("")) {
                            ReturnMsg = ConfProperty.getCommonPropertyValue("BranchHeadNotFoundAlert")
                                    .replace("##ProcessingBranch##", ProcessingBranch);
                            Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                            return ReturnMsg;
                        }
                    }

                    //ifr.setValue("Q_ApproveRecommendAuthority", FinalAuthorityOrg);
                }

                if (FinalAuthorityOrg.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("OrganizationNotfoundAlert")
                            .replace("##Grp##", FinalApprovingAuthority)
                            .replace("##stage##", "Final Approving Authority");
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;
                }

                tags = new String[]{"##OrgName##"};
                data = new String[]{FinalAuthorityOrg};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String FinalAuthorityHierarchy = GetDataFromDbCommon(ifr, "HirarchyLevelFetchQuery", tags, data);
                Log.consoleLog(ifr, "FinalAuthorityHierarchy:: " + FinalAuthorityHierarchy);

                if (FinalAuthorityHierarchy.equalsIgnoreCase("")) {
                    ReturnMsg = ConfProperty.getCommonPropertyValue("HierarchyNotfoundAlert")
                            .replace("##Org##", FinalAuthorityHierarchy)
                            .replace("##stage##", "Final Approving Authority");
                    Log.consoleLog(ifr, "ReturnMsg:: " + ReturnMsg);
                    return ReturnMsg;

                }

                ifr.setValue("FinalAuthority", FinalAuthorityOrg);
                ifr.setValue("FinalAuthorityCount", FinalAuthorityHierarchy);
                ifr.setValue("Q_FinalAuthorityDesignation", FinalApprovingAuthority);
                //ifr.setValue("FinalAuthorityGroup", FinalApprovingAuthority);
                //Need to add logic for handling Branch and RAH after clarifing with BA.

                String ApprovalCycleStatus = ApprovalCycleSettings(ifr, CallFrom,
                        String.valueOf(Integer.parseInt(LoggedInUserHierarchy) + 1),
                        FinalAuthorityHierarchy, FinalAuthorityOrg);
                Log.consoleLog(ifr, "ApprovalCycleStatus:: " + ApprovalCycleStatus);
                ReturnMsg = "success";
                return ReturnMsg;
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception IN ApprovalMatrixDerivation " + e);
            Log.errorLog(ifr, "Exception IN ApprovalMatrixDerivation " + ExceptionUtils.getStackTrace(e));
            ReturnMsg = "error";
            return ReturnMsg;
        }
        return "";
    }

    /*Added By: Veena.K
     Date: 14-06-2024
     Description: To Set the Approving cycles count.*/
    public String ApprovalCycleSettings(IFormReference ifr, String CallFrom,
            String Param1, String Param2, String Param3) {
        try {
            Log.consoleLog(ifr, "Inside ApprovalCycleSettings::CallFrom:: " + CallFrom
                    + "Param1:: " + Param1 + " param2:: " + Param2 + " Param3:: " + Param3);
            //param1 = next, param2 = final
            String[] tags = new String[0];
            String[] data = new String[0];
            int FinalApprovingHierarchy = Integer.parseInt((String) ifr.getValue("FinalAuthorityCount"));
            Log.consoleLog(ifr, "FinalApprovingHierarchy:: " + FinalApprovingHierarchy);
            switch (CallFrom) {
                case "Branch Maker": {
                    Log.consoleLog(ifr, "Inside Branch Maker case::");
                    Log.consoleLog(ifr, "Next Recomending Hierarchy:: " + Param1);
                    Log.consoleLog(ifr, "Final approval Hierarchy:: " + Param2);
                    Log.consoleLog(ifr, "Final approval org:: " + Param3);
                    tags = new String[]{"##HierarchyLevel##"};
                    data = new String[]{Param1};
                    Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                    String NextRecommendingOrg = GetDataFromDbCommon(ifr, "OrgHierarchyFetchQuery", tags, data);
                    Log.consoleLog(ifr, "NextRecommendingOrg:: " + NextRecommendingOrg);
                    ifr.setValue("RecommendingAuthority", NextRecommendingOrg);
                    ifr.setValue("RecommendingAuthorityCount", Param1);
                    ifr.setValue("Q_ApproveRecommendAuthority", NextRecommendingOrg);
                    Log.consoleLog(ifr, "Q_ApproveRecommendAuthority after set:: " + ifr.getValue("Q_ApproveRecommendAuthority"));

                    if (Integer.parseInt(Param1) == Integer.parseInt(Param2)) {
                        Log.consoleLog(ifr, "param1 == Param2 case");
                        ifr.setValue("NextWS", "Sanction");
                        Log.consoleLog(ifr, "NextWS after set:: " + ifr.getValue("NextWS"));
                        return "success";
                    } else if (Integer.parseInt(Param1) < Integer.parseInt(Param2)) {
                        Log.consoleLog(ifr, "param1 < Param2 case");
                        ifr.setValue("NextWS", "Branch Checker");
                        Log.consoleLog(ifr, "NextWS after set:: " + ifr.getValue("NextWS"));
                        return "success";
                    } else if ((Integer.parseInt(Param1) - 1) == Integer.parseInt(Param2)) {
                        Log.consoleLog(ifr, "param1-1 == Param2 case");
                        ifr.setValue("NextWS", "Sanction");
                        ifr.setValue("Q_ApproveRecommendAuthority", Param3);
                        ifr.setValue("RecommendingAuthority", Param3);
                        ifr.setValue("RecommendingAuthorityCount", Param1);
                        Log.consoleLog(ifr, "NextWS after set:: " + ifr.getValue("NextWS"));
                        return "success";
                    }

                    break;

                    //return "success";
                }
                case "Branch Checker": {
                    Log.consoleLog(ifr, "Inside Branch Checker case:: at the start");
                    String PreviousWS = (String) ifr.getValue("PreviousWS");
                    Log.consoleLog(ifr, "PreviousWS:: " + PreviousWS);
                    String CurrentRecommendingOrg = (String) ifr.getValue("RecommendingAuthority");
                    int CurrentRecommendingHierarchy = Integer.parseInt(
                            (String) ifr.getValue("RecommendingAuthorityCount"));
                    Log.consoleLog(ifr, "CurrentRecommendingOrg:: " + CurrentRecommendingOrg
                            + " CurrentRecommendingHierarchy:: " + CurrentRecommendingHierarchy);
                    int NextRecomendingHierarchy = CurrentRecommendingHierarchy + 1;
                    Log.consoleLog(ifr, "NextRecomendingHierarchy:: " + NextRecomendingHierarchy);
                    ifr.setValue("RecommendingAuthorityCount", String.valueOf(NextRecomendingHierarchy));
                    Log.consoleLog(ifr, "RecommendingAuthorityCount after set:: " + ifr.getValue("RecommendingAuthorityCount"));
                    tags = new String[]{"##HierarchyLevel##"};
                    data = new String[]{String.valueOf(NextRecomendingHierarchy)};
                    Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                    String NextRecommendingOrg = GetDataFromDbCommon(ifr, "OrgHierarchyFetchQuery", tags, data);
                    Log.consoleLog(ifr, "NextRecommendingOrg:: " + NextRecommendingOrg);
                    ifr.setValue("RecommendingAuthority", NextRecommendingOrg);
                    Log.consoleLog(ifr, "RecommendingAuthority after set:: " + ifr.getValue("RecommendingAuthority"));
                    ifr.setValue("Q_ApproveRecommendAuthority", NextRecommendingOrg);
                    Log.consoleLog(ifr, "Q_ApproveRecommendAuthority after set:: " + ifr.getValue("Q_ApproveRecommendAuthority"));

                    if (NextRecomendingHierarchy == FinalApprovingHierarchy) {
                        Log.consoleLog(ifr, "NextRecomendingHierarchy == FinalApprovingHierarchy case");
                        ifr.setValue("NextWS", "Sanction");
                        return "success";
                    } else if (NextRecomendingHierarchy < FinalApprovingHierarchy) {
                        Log.consoleLog(ifr, "NextRecomendingHierarchy < FinalApprovingHierarchy case");
                        ifr.setValue("NextWS", "Branch Checker");
                        return "success";
                    } else if ((NextRecomendingHierarchy - 1) == FinalApprovingHierarchy) {
                        Log.consoleLog(ifr, "(NextRecomendingHierarchy-1) == FinalApprovingHierarchy case");
                        ifr.setValue("NextWS", "Sanction");
                        return "success";
                    }
                    Log.consoleLog(ifr, "NextWS after set:: " + ifr.getValue("NextWS"));
                    break;

                }
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in ApprovalCycleSettings:: " + e);
            Log.errorLog(ifr, "Exception IN ApprovalCycleSettings " + ExceptionUtils.getStackTrace(e));
            return "error";
        }

        return "";
    }

    public void loanAccChange(IFormReference ifr) {
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
                //String queryLAN = "SELECT * FROM  LOS_INTEGRATION_CBS_STATUS   WHERE TRANSACTION_ID ='#PID#'".replaceAll("#PID#", ProcessInstanceId);
                //List<List<String>> LANStatus = cf.mExecuteQuery(ifr, queryLAN, "Execute query for fetching LOANACCOUNTNO queryLAN:: ");
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

    public void loanDetailsChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside loanAccChange: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                ifr.setStyle("ALV_BAM83", "disable", "false");

                Log.consoleLog(ifr, "inside loanDetailsChange: ");
                String InvisibleFields = "QL_LOAN_DETAILS_SubsidyProcessRequired,QL_LOAN_DETAILS_LoanInsured,QL_LOAN_DETAILS_InsuranceScheme,QL_LOAN_DETAILS_MortgageFlag,QL_LOAN_DETAILS_SecurityCount, QL_LOAN_DETAILS_HypothecationFlag,QL_LOAN_DETAILS_CGSTMECovered,QL_LOAN_DETAILS_CGPAN,QL_LOAN_DETAILS_LeadBank,QL_LOAN_DETAILS_Shares,QL_LOAN_DETAILS_TotalLimitunderConsortium,QL_LOAN_DETAILS_OtherLeadBank,QL_LOAN_DETAILS_INCREMENTAL_INTEREST";
                pcm.controlinvisiblity(ifr, InvisibleFields);

            }
        }
    }

    public void bamListViewLoad(IFormReference ifr) {

        Log.consoleLog(ifr, "inside bamListViewLoad: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {

            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                Log.consoleLog(ifr, "inside bamListViewLoad: ");

                String editableFields = "QNL_BAM83_RBI_PURPOSE_CODE,QNL_BAM83_SECTOR,QNL_BAM83_RETAIL_BASEL_II_CUSTOMER_TYPE,QNL_BAM83_SCHEMES,QNL_BAM83_GUARANTEE_COVER,QNL_BAM83_BSR_CODE,QNL_BAM83_SSISUBSEC,QNL_BAM83_STATUSIB,QNL_BAM83_PRI_SECTOR_N_PRI_SECTOR,QNL_BAM83_SPECIAL_BENEFICIARIES,QNL_BAM83_SUB_SCHEME,QNL_BAM83_RAH";
                pcm.controlEnable(ifr, editableFields);

            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "inside bamListViewLoad Disbursement Checker: ");

                String noneditableFields = "QNL_BAM83_RBI_PURPOSE_CODE,QNL_BAM83_SECTOR,QNL_BAM83_RETAIL_BASEL_II_CUSTOMER_TYPE,QNL_BAM83_SCHEMES,QNL_BAM83_GUARANTEE_COVER,QNL_BAM83_BSR_CODE,QNL_BAM83_SSISUBSEC,QNL_BAM83_STATUSIB,QNL_BAM83_PRI_SECTOR_N_PRI_SECTOR,QNL_BAM83_SPECIAL_BENEFICIARIES,QNL_BAM83_SUB_SCHEME,QNL_BAM83_RAH";
                pcm.controlDisable(ifr, noneditableFields);

            }

        }

    }

// modified by Sharon on 04/07/2024
    public void onSectionChangeLoanDetails(IFormReference ifr) {
        Log.consoleLog(ifr, "inside onLoadBureauConsent BudgetBkoffCustomCode:: ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside loanDetails:: Budget ");
            String nonVisibleFileds = "QL_LOAN_DETAILS_SegmentCode,QL_LOAN_DETAILS_CBSProductCode,QL_LOAN_DETAILS_CBSSubProductCode,QL_LOAN_DETAILS_TransactionDate,QL_LOAN_DETAILS_SanctioningDetails,QL_LOAN_DETAILS_FacilityNo,QL_LOAN_DETAILS_AccountType,QL_LOAN_DETAILS_TakeOverfromBank,QL_LOAN_DETAILS_SchemeCode,QL_LOAN_DETAILS_SubsidyProcessRequired,QL_LOAN_DETAILS_AccountSectorInd,QL_LOAN_DETAILS_CustomerType,QL_LOAN_DETAILS_InterestApplied,QL_LOAN_DETAILS_LoanInsured,QL_LOAN_DETAILS_InsuranceScheme,QL_LOAN_DETAILS_SanctionAuthorityCode,QL_LOAN_DETAILS_MortgageFlag,QL_LOAN_DETAILS_SecurityCount,QL_LOAN_DETAILS_HypothecationFlag,QL_LOAN_DETAILS_HypothecationSecurityCount,QL_LOAN_DETAILS_CGSTMECovered,QL_LOAN_DETAILS_CGPAN,QL_LOAN_DETAILS_RBIIndustryCode,QL_LOAN_DETAILS_AccountTypeFlag,QL_LOAN_DETAILS_CBSROI,QL_LOAN_DETAILS_LeadBank,QL_LOAN_DETAILS_Shares,QL_LOAN_DETAILS_TotalLimitunderConsortium,QL_LOAN_DETAILS_OtherLeadBank,QL_LOAN_DETAILS_INCREMENTAL_INTEREST";
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlinvisiblity(ifr, nonVisibleFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                pcm.controlinvisiblity(ifr, nonVisibleFileds);
                //ifr.setStyle("BTN_LoanCreation", "disable", "true");
            }
            String CBSproductCode = pcm.getCBSProductCode(ifr);
            Log.consoleLog(ifr, "inside onSectionChangeLoanDetails Budget CBSproductCode" + CBSproductCode);
        }
    }

    //Added by Aravindh on 24-06-2024 for Cibil Experian Liabilities data population in Backoffice
    public String PopulateCibilExperianLiabilities(IFormReference ifr, String bureauType, String ApplicantType) throws ParseException, java.text.ParseException {

        Log.consoleLog(ifr, "inside PopulateCibilExperianLiabilities: ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String responseBody = "";
        JSONParser parser1 = new JSONParser();
        try {
            //========================================> Experian Response Parsing
            if (bureauType.equalsIgnoreCase("EX")) {
                Log.consoleLog(ifr, "Bureau type: " + bureauType);
                if (ConfProperty.getIntegrationValue("MOCKFLG_EXPERIAN").equalsIgnoreCase("Y")) {
                    responseBody = apimr.readMockResponse(ifr, "EXPERIAN");
                    //responseBody = "{\"body\":{\"INProfileResponse\":{\"Header\":{\"SystemCode\":0,\"MessageText\":\"\",\"ReportDate\":20240205,\"ReportTime\":183314},\"UserMessage\":{\"UserMessageText\":\"Normal Response\"},\"CreditProfileHeader\":{\"Enquiry_Username\":\"cpu2canara_prod07\",\"ReportDate\":20240205,\"ReportTime\":183314,\"Version\":\"V2.4\",\"ReportNumber\":1707138193771,\"Subscriber\":\"\",\"Subscriber_Name\":\"Canara Bank\"},\"Current_Application\":{\"Current_Application_Details\":{\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"Current_Applicant_Details\":{\"Last_Name\":\"s\",\"First_Name\":\"AMARA PAVANKUMAR\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":1,\"IncomeTaxPan\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Passport_Number\":\"\",\"Passport_Issue_Date\":\"\",\"Passport_Expiration_Date\":\"\",\"Voter_s_Identity_Card\":\"\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"Ration_Card_Number\":\"\",\"Ration_Card_Issue_Date\":\"\",\"Ration_Card_Expiration_Date\":\"\",\"Universal_ID_Number\":\"\",\"Universal_ID_Issue_Date\":\"\",\"Universal_ID_Expiration_Date\":\"\",\"Date_Of_Birth_Applicant\":19890525,\"Telephone_Number_Applicant_1st\":\"\",\"Telephone_Extension\":\"\",\"Telephone_Type\":\"\",\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"Current_Other_Details\":{\"Income\":\"\",\"Marital_Status\":2,\"Employment_Status\":\"S\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":0},\"Current_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"},\"Current_Applicant_Additional_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"}}},\"CAIS_Account\":{\"CAIS_Summary\":{\"Credit_Account\":{\"CreditAccountTotal\":6,\"CreditAccountActive\":6,\"CreditAccountDefault\":0,\"CreditAccountClosed\":0,\"CADSuitFiledCurrentBalance\":0},\"Total_Outstanding_Balance\":{\"Outstanding_Balance_Secured\":4825631,\"Outstanding_Balance_Secured_Percentage\":99,\"Outstanding_Balance_UnSecured\":42529,\"Outstanding_Balance_UnSecured_Percentage\":1,\"Outstanding_Balance_All\":4868160}},\"CAIS_Account_DETAILS\":[{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C000115766\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20140721,\"Credit_Limit_Amount\":25000,\"Highest_Credit_or_Original_Loan_Amount\":\"\",\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"?00000000000000000000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20210531,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20170228,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2021,\"Month\":5,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":3,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":2510,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":2,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":1,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":12,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":11,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST TELLABA\",\"Third_Line_Of_Address_non_normalized\":\"VIA ANDRAPRADESH\",\"City_non_normalized\":\"###\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9492537874},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889254001153,\"Portfolio_Type\":\"I\",\"Account_Type\":12,\"Open_Date\":20190121,\"Highest_Credit_or_Original_Loan_Amount\":200000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20240102,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":7,\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":10,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":139710,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":117563,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":115619,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":93056,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":123065,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":[{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}]},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":2536667005808,\"Portfolio_Type\":\"M\",\"Account_Type\":2,\"Open_Date\":20190424,\"Highest_Credit_or_Original_Loan_Amount\":4500000,\"Terms_Duration\":360,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":5170000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":360,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4704642,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705175,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705017,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705367,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705026,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889694000003,\"Portfolio_Type\":\"I\",\"Account_Type\":13,\"Open_Date\":20200716,\"Highest_Credit_or_Original_Loan_Amount\":75000,\"Terms_Duration\":84,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"P\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":88000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":84,\"Promotional_Rate_Flag\":\"\",\"Income\":100001,\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20200731,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":31874,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":33059,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":34235,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":35407,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":36569,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PVTXXXXXXXX\",\"Subscriber_Name\":\"XXXXXXXXXX\",\"Account_Number\":\"XXXXXXXXXXXX2879\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20220926,\"Credit_Limit_Amount\":340000,\"Highest_Credit_or_Original_Loan_Amount\":45111,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"0000000000000???????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231112,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231023,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20221012,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":11,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":14929,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4593,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9830,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9043,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":6,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":10429,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA\",\"First_Name_Non_Normalized\":\"PAVAN\",\"Middle_Name_1_Non_Normalized\":\"KUMAR\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":[{\"First_Line_Of_Address_non_normalized\":\"CANARA BANK 2ND FLOOR  NAVEENCOMPLEX MG\",\"Second_Line_Of_Address_non_normalized\":\"ROAD HEAD OFFICE  OPP 1MG MALL\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560002,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"27   3RD FLOOR GANESH ILLAM ATMANANDA CO\",\"Second_Line_Of_Address_non_normalized\":\"LONY  SULTANPALYA MAIN ROAD RT NAGAR  NE\",\"Third_Line_Of_Address_non_normalized\":\"AR NARAYANA APARTMENT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560032,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"3 108 CHIMAKURTHI CHANDRAPADU  PRAKASAM\",\"Second_Line_Of_Address_non_normalized\":\"\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523226,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"}],\"CAIS_Holder_Phone_Details\":[{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"},{\"Telephone_Number\":2011111111,\"Telephone_Type\":3,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}],\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C117024315\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20230830,\"Credit_Limit_Amount\":100000,\"Highest_Credit_or_Original_Loan_Amount\":100000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"000?????????????????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0,\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20230930,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":48588,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":7337,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVAN KUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":560009,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}}]},\"Match_result\":{\"Exact_match\":\"Y\"},\"TotalCAPS_Summary\":{\"TotalCAPSLast7Days\":2,\"TotalCAPSLast30Days\":2,\"TotalCAPSLast90Days\":2,\"TotalCAPSLast180Days\":2},\"CAPS\":{\"CAPS_Summary\":{\"CAPSLast7Days\":2,\"CAPSLast30Days\":2,\"CAPSLast90Days\":2,\"CAPSLast180Days\":2},\"CAPS_Application_Details\":[{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":161727,\"ReportNumber\":1706611647587,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"},{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":132619,\"ReportNumber\":1706601379101,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"}]},\"NonCreditCAPS\":{\"NonCreditCAPS_Summary\":{\"NonCreditCAPSLast7Days\":0,\"NonCreditCAPSLast30Days\":0,\"NonCreditCAPSLast90Days\":0,\"NonCreditCAPSLast180Days\":0}},\"PSV\":{\"BFHL_Ex_HL\":{\"TN_of_BFHL_CAD_Ex_HL\":\"\",\"Tot_Val_of_BFHL_CAD\":\"\",\"MNT_SMR_BFHL_CAD\":\"\"},\"HL_CAD\":{\"TN_of_HL_CAD\":\"\",\"Tot_Val_of_HL_CAD\":\"\",\"MNT_SMR_HL_CAD\":\"\"},\"Telcos_CAD\":{\"TN_of_Telcos_CAD\":\"\",\"Tot_Val_of_Telcos_CAD\":\"\",\"MNT_SMR_Telcos_CAD\":\"\"},\"MF_CAD\":{\"TN_of_MF_CAD\":\"\",\"Tot_Val_of_MF_CAD\":\"\",\"MNT_SMR_MF_CAD\":\"\"},\"Retail_CAD\":{\"TN_of_Retail_CAD\":\"\",\"Tot_Val_of_Retail_CAD\":\"\",\"MNT_SMR_Retail_CAD\":\"\"},\"Total_CAD\":{\"TN_of_All_CAD\":\"\",\"Tot_Val_of_All_CAD\":\"\",\"MNT_SMR_CAD_All\":\"\"},\"BFHL_ACA_ExHL\":{\"TN_of_BFHL_ACA_Ex_HL\":\"\",\"Bal_BFHL_ACA_Ex_HL\":\"\",\"WCD_St_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_6_MNT_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_7_12_MNT_BFHL_ACA_Ex_HL\":\"\",\"Age_of_Oldest_BFHL_ACA_Ex_HL\":\"\",\"HCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\",\"TCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\"},\"HL_ACA\":{\"TN_of_HL_ACA\":\"\",\"Bal_HL_ACA\":\"\",\"WCD_St_HL_ACA\":\"\",\"WDS_Pr_6_MNT_HL_ACA\":\"\",\"WDS_Pr_7_12_MNT_HL_ACA\":\"\",\"Age_of_Oldest_HL_ACA\":\"\"},\"MF_ACA\":{\"TN_of_MF_ACA\":\"\",\"Total_Bal_MF_ACA\":\"\",\"WCD_St_MF_ACA\":\"\",\"WDS_Pr_6_MNT_MF_ACA\":\"\",\"WDS_Pr_7_12_MNT_MF_ACA\":\"\",\"Age_of_Oldest_MF_ACA\":\"\"},\"Telcos_ACA\":{\"TN_of_Telcos_ACA\":\"\",\"Total_Bal_Telcos_ACA\":\"\",\"WCD_St_Telcos_ACA\":\"\",\"WDS_Pr_6_MNT_Telcos_ACA\":\"\",\"WDS_Pr_7_12_MNT_Telcos_ACA\":\"\",\"Age_of_Oldest_Telcos_ACA\":\"\"},\"Retail_ACA\":{\"TN_of_Retail_ACA\":\"\",\"Total_Bal_Retail_ACA\":\"\",\"WCD_St_Retail_ACA\":\"\",\"WDS_Pr_6_MNT_Retail_ACA\":\"\",\"WDS_Pr_7_12_MNT_Retail_ACA\":\"\",\"Age_of_Oldest_Retail_ACA\":\"\",\"HCB_Lm_Per_Rev_Acc_Ret\":\"\",\"Tot_Cur_Bal_Lm_Per_Rev_Acc_Ret\":\"\"},\"Total_ACA\":{\"TN_of_All_ACA\":\"\",\"Bal_All_ACA_Ex_HL\":\"\",\"WCD_St_All_ACA\":\"\",\"WDS_Pr_6_MNT_All_ACA\":\"\",\"WDS_Pr_7_12_MNT_All_ACA\":\"\",\"Age_of_Oldest_All_ACA\":\"\"},\"BFHL_ICA_Ex_HL\":{\"TN_of_NDel_BFHL_InACA_Ex_HL\":\"\",\"TN_of_Del_BFHL_InACA_Ex_HL\":\"\"},\"HL_ICA\":{\"TN_of_NDel_HL_InACA\":\"\",\"TN_of_Del_HL_InACA\":\"\"},\"MF_ICA\":{\"TN_of_NDel_MF_InACA\":\"\",\"TN_of_Del_MF_InACA\":\"\"},\"Telcos_ICA\":{\"TN_of_NDel_Telcos_InACA\":\"\",\"TN_of_Del_Telcos_InACA\":\"\"},\"Retail_ICA\":{\"TN_of_NDel_Retail_InACA\":\"\",\"TN_of_Del_Retail_InACA\":\"\"},\"PSV_CAPS\":{\"BFHL_CAPS_Last_90_Days\":\"\",\"MF_CAPS_Last_90_Days\":\"\",\"Telcos_CAPS_Last_90_Days\":\"\",\"Retail_CAPS_Last_90_Days\":\"\"},\"Own_Company_Data\":{\"TN_of_OCom_CAD\":\"\",\"Tot_Val_of_OCom_CAD\":\"\",\"MNT_SMR_OCom_CAD\":\"\",\"TN_of_OCom_ACA\":\"\",\"Bal_OCom_ACA_Ex_HL\":\"\",\"Bal_OCom_ACA_HL_Only\":\"\",\"WCD_St_OCom_ACA\":\"\",\"HCB_Lm_Per_Rev_OCom_ACA\":\"\",\"TN_of_NDel_OCom_InACA\":\"\",\"TN_of_Del_OCom_InACA\":\"\",\"TN_of_OCom_CAPS_Last_90_days\":\"\"},\"Oth_CB_Information\":{\"Any_Rel_CB_Data_Dis_Y_N\":\"\",\"Oth_Rel_CB_DFC_Pos_Mat_Y_N\":\"\"},\"Indian_Market_Specific_Var\":{\"TN_of_CAD_classed_as_SFWDWO\":\"\",\"MNT_SMR_CAD_classed_as_SFWDWO\":\"\",\"Num_of_CAD_SFWDWO_Last_24_MNT\":\"\",\"Tot_Cur_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Live_UAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_UAcc\":\"\"}},\"SCORE\":{\"BureauScore\":829,\"BureauScoreConfidLevel\":\"H\",\"CreditRating\":\"\"}}}}";
                    //responseBody = "{  \"body\": {    \"INProfileResponse\": {      \"SCORE\": {        \"BureauScore\": 765,        \"BureauScoreConfidLevel\": \"H\",        \"CreditRating\": \"\"      },      \"Header\": {        \"ReportTime\": 155554,        \"SystemCode\": 0,        \"ReportDate\": 20230925,        \"MessageText\": \"\"      },      \"TotalCAPS_Summary\": {        \"TotalCAPSLast90Days\": 1,        \"TotalCAPSLast7Days\": 1,        \"TotalCAPSLast30Days\": 1,        \"TotalCAPSLast180Days\": 1      },      \"CreditProfileHeader\": {        \"ReportTime\": 155554,        \"Version\": \"V2.4\",        \"Subscriber\": \"\",        \"Enquiry_Username\": \"cpu2canara_prod02\",        \"ReportNumber\": 1695637552936,        \"ReportDate\": 20230925,        \"Subscriber_Name\": \"Canara Bank\"      },      \"NonCreditCAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220622,          \"Subscriber_code\": \"PVTXXXXXXXX\",          \"Enquiry_Reason\": 99,          \"Amount_Financed\": 0,          \"Duration_Of_Agreement\": 0,          \"ReportTime\": 161428,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 1,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 6381086803,            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": \"\",            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \".\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"\",            \"FlatNoPlotNoHouseNo\": \"MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": \"\",          \"ReportNumber\": 1655894668194,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"NonCreditCAPS_Summary\": {          \"NonCreditCAPSLast30Days\": 1,          \"NonCreditCAPSLast180Days\": 1,          \"NonCreditCAPSLast90Days\": 1,          \"NonCreditCAPSLast7Days\": 1        }      },      \"UserMessage\": {        \"UserMessageText\": \"Normal Response\"      },      \"CAIS_Account\": {        \"CAIS_Summary\": {          \"Total_Outstanding_Balance\": {            \"Outstanding_Balance_Secured\": 444505,            \"Outstanding_Balance_UnSecured_Percentage\": 0,            \"Outstanding_Balance_All\": 983589,            \"Outstanding_Balance_Secured_Percentage\": 45,            \"Outstanding_Balance_UnSecured\": -20          },          \"Credit_Account\": {            \"CreditAccountActive\": 6,            \"CreditAccountClosed\": 36,            \"CreditAccountDefault\": 0,            \"CreditAccountTotal\": 42,            \"CADSuitFiledCurrentBalance\": 0          }        },        \"CAIS_Account_DETAILS\": [          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160613,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSSSSSSS???????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20170630,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20170606,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842021906,            \"Date_of_Last_Payment\": 20170606,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 90000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 12          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160606,            \"Account_Type\": 0,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"0000000000000000000000000000001?0100\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": 0,            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 25,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 53,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 30,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 31,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20220731,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20220704,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 10,            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": [              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              },              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              }            ],            \"Account_Number\": 1028261000011,            \"Date_of_Last_Payment\": 20220704,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 800000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 0,            \"Terms_Duration\": \"\",            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 13          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170607,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS????????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20171031,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20171003,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842023502,            \"Date_of_Last_Payment\": 20171003,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 100000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 15          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170608,            \"Account_Type\": 7,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Surname_Non_Normalized\": \"MUMTHAJ\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"K\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS?SS?????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"XXXXXXXXXX\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": 0,            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": [              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107/5 MAIN ROAD\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PERITCHIPALAYAM,KOTTATHURAI\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"KEERANUR,DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              },              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107 NAFILA ILLAM\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PALANI MAIN ROAD\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"PALANI TK, DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              }            ],            \"Date_Reported\": 20180131,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": {              \"Telephone_Number\": \"\",              \"Mobile_Telephone_Number\": 9787458226,              \"Telephone_Type\": 1            },            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20180108,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 9,            \"Terms_Frequency\": \"M\",            \"CAIS_Holder_ID_Details\": {              \"Ration_Card_Number\": \"22G0539113\",              \"Voter_ID_Expiration_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"Ration_Card_Expiration_Date\": \"\",              \"Ration_Card_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Voter_ID_Issue_Date\": \"\",              \"Driver_License_Issue_Date\": \"\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": \"XXXXXXXXXXXXX6718\",            \"Date_of_Last_Payment\": \"\",            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 47000,            \"Settlement_Amount\": 0,            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": 0,            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 24,            \"Terms_Duration\": 24,            \"Identification_Number\": \"PUBXXXXXXXX\",            \"Account_Status\": 15          }        ]      },      \"Match_result\": {        \"Exact_match\": \"Y\"      },      \"Current_Application\": {        \"Current_Application_Details\": {          \"Current_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"S\",            \"Number_of_Major_Credit_Card_Held\": 0,            \"Marital_Status\": 2,            \"Income\": \"\"          },          \"Enquiry_Reason\": 14,          \"Current_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Amount_Financed\": 1500000,          \"Current_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Duration_Of_Agreement\": 180,          \"Finance_Purpose\": 48,          \"Current_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": \"\",            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 9787458226,            \"Middle_Name3\": \"\",            \"Passport_Number\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 1,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"K\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          }        }      },      \"CAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220210,          \"Subscriber_code\": \"OTHXXXXXXXX\",          \"Enquiry_Reason\": 14,          \"Amount_Financed\": 2500000,          \"Duration_Of_Agreement\": 180,          \"ReportTime\": 125442,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": 9787458226,            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 0,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 2,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"KAJINI\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"DINDIGUL\",            \"FlatNoPlotNoHouseNo\": \"107 5 PERITCHIPALAYAM MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"VILLAGE DINDUGUL\",            \"FlatNoPlotNoHouseNo\": \"PERICHIPALAYAM SF NO 88 2 KOTTADURAI\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"ReportNumber\": 1644477882780,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"CAPS_Summary\": {          \"CAPSLast30Days\": 0,          \"CAPSLast7Days\": 0,          \"CAPSLast180Days\": 0,          \"CAPSLast90Days\": 0        }      }    }  },  \"responseCode\": 200}";
                    Log.consoleLog(ifr, "Mock Flag is Y ::");
                } else {
                    try {
                        // String ExperianResQuery = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME ='Experian_API' AND TRANSACTION_ID ='" + PID + "' AND APPLICANTTYPE ='" + ApplicantType + "'" ;
                        String ExperianResQuery = ConfProperty.getQueryScript("ExperianResQuery").replaceAll("#PID#", PID).replaceAll("#APPLICANTTYPE#", ApplicantType);

                        List<List<String>> ExperianResponse = cf.mExecuteQuery(ifr, ExperianResQuery, "Execute query for fetching Experian Response");
                        if (!ExperianResponse.isEmpty()) {

                            String ExperianResponseStr = ExperianResponse.get(0).get(0);
                            Log.consoleLog(ifr, "ExperianResponseStr From Table==>" + ExperianResponseStr);
                            JSONObject ExperianResponseObj = (JSONObject) parser1.parse(ExperianResponseStr);
                            responseBody = ExperianResponseObj.toString();
                            Log.consoleLog(ifr, "Experian Response from table Parsed ::");
                        }
                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in ExperianResQuery :" + e);
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
                        return message.toString();
                    }
                }

                Log.consoleLog(ifr, "EXPERIAN API CALLED SUCCESSFULLY:::>>" + responseBody);

                JSONObject OutputJSON1 = (JSONObject) parser1.parse(responseBody);
                JSONObject resultObj = new JSONObject(OutputJSON1);

                Log.consoleLog(ifr, "resultObj==>" + resultObj);

                String body = resultObj.get("body").toString();
                Log.consoleLog(ifr, "body==>" + body);
                JSONObject bodyJSON = (JSONObject) parser1.parse(body);
                JSONObject bodyObj = new JSONObject(bodyJSON);

                String INProfileResponseData = bodyObj.get("INProfileResponse").toString();
                Log.consoleLog(ifr, "INProfileResponseData==>" + INProfileResponseData);
                JSONObject INProfileResponseDataJSON = (JSONObject) parser1.parse(INProfileResponseData);
                JSONObject INProfileResponseDataJSONObj = new JSONObject(INProfileResponseDataJSON);
                JSONArray jsonarr = new JSONArray();
                // String queryData = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='#applicanttype#'  and PID='#ProcessInstanceId#';
                String queryData = ConfProperty.getQueryScript("getinsertionOrderID").replaceAll("#ProcessInstanceId#", PID).replaceAll("#applicanttype#", ApplicantType);

                List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                String party_type = data.get(0).get(0);
                Log.consoleLog(ifr, "Party Type==>" + party_type);

                double TotalEmiOblig = 0.00;
                String closed = "";
                if (!(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account").equalsIgnoreCase(""))) {
                    JSONObject CAIS_Account = (JSONObject) parser1.parse(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account"));
                    if (!(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").equalsIgnoreCase(""))) {
                        JSONArray CAIS_Account_DETAILS = (JSONArray) parser1.parse(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
                        for (int i = 0; i < CAIS_Account_DETAILS.size(); i++) {
                            JSONObject CAIS_Account_DETAILSObj = (JSONObject) CAIS_Account_DETAILS.get(i);
                            String subscriberName = CAIS_Account_DETAILSObj.get("Subscriber_Name").toString();
                            String openDate = CAIS_Account_DETAILSObj.get("Open_Date").toString();
                            closed = CAIS_Account_DETAILSObj.get("Date_Closed").toString();
                            String Account_Type = CAIS_Account_DETAILSObj.get("Account_Type").toString();
                            //String loanAmount = CAIS_Account_DETAILSObj.get("Highest_Credit_or_Original_Loan_Amount").toString();
                            String loanOutstanding = CAIS_Account_DETAILSObj.get("Current_Balance").toString();
                            String overdue = CAIS_Account_DETAILSObj.get("Amount_Past_Due").toString();
                            //String emi_amt = CAIS_Account_DETAILSObj.get("Scheduled_Monthly_Payment_Amount").toString();
                            String accStatus = CAIS_Account_DETAILSObj.get("Account_Status").toString();
                            String AccountNumber = CAIS_Account_DETAILSObj.get("Account_Number").toString();
                            int acc_status = Integer.parseInt(accStatus);

                            String loanDate = "";
                            if (!openDate.isEmpty() && openDate != null) {

                                SimpleDateFormat dOriginalDate = new SimpleDateFormat("yyyyMMdd");
                                SimpleDateFormat dTargetDate = new SimpleDateFormat("dd/MM/yyyy");
                                Date date = dOriginalDate.parse(openDate);
                                // openDate = dTargetDate.format(date);

                                Log.consoleLog(ifr, "Date After parsing::" + date);
                                //   Timestamp timestamp = new Timestamp(date.getTime());
                                //  Log.consoleLog(ifr, "formatted timestamp::" + timestamp);

                                //   SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                //  loanDate = dTargetDate.format(timestamp);
                                openDate = dTargetDate.format(date);
                                Log.consoleLog(ifr, "formatted loanDate date::" + openDate);
                            }

                            String Credit_Limit_Amount = "";
                            String emi_amt = "";
                            JSONArray Advanced_Account_History = (JSONArray) CAIS_Account_DETAILSObj.get("Advanced_Account_History");
                            for (int j = 0; j < 1; j++) {
                                JSONObject Advanced_Account_HistoryOBJ = (JSONObject) Advanced_Account_History.get(j);
                                Credit_Limit_Amount = Advanced_Account_HistoryOBJ.get("Credit_Limit_Amount").toString();
                                emi_amt = Advanced_Account_HistoryOBJ.get("EMI_Amount").toString();
                                Log.consoleLog(ifr, "Credit_Limit_Amount::" + Credit_Limit_Amount);
                                Log.consoleLog(ifr, "emi_amt::" + emi_amt);

                            }
                            Log.consoleLog(ifr, "subscriber Name::" + subscriberName);

                            JSONObject obj = new JSONObject();
                            obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                            obj.put("QNL_AL_LIAB_VAL_Bank", subscriberName);
                            obj.put("QNL_AL_LIAB_VAL_loanStartDate", openDate);
                            obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", Credit_Limit_Amount);
                            obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", loanOutstanding);
                            obj.put("QNL_AL_LIAB_VAL_Overdue", overdue.equalsIgnoreCase("") ? "0.00" : overdue);
                            obj.put("QNL_AL_LIAB_VAL_ApplicantType", party_type);
                            obj.put("QNL_AL_LIAB_VAL_LoanType", Account_Type);
                            obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", AccountNumber);
                            obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                            overdue = overdue.equalsIgnoreCase("") ? "0.00" : overdue;
                            obj.put("QNL_AL_LIAB_VAL_Overdue", overdue);
                            //if (acc_status >= 12 && acc_status <= 17) 
                            if (emi_amt.equalsIgnoreCase("") || emi_amt.equalsIgnoreCase(null)) {
                                obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
                                Log.consoleLog(ifr, "EMI is null");

                            } else {
                                obj.put("QNL_AL_LIAB_VAL_EMIAmt", emi_amt);
                                Log.consoleLog(ifr, "EMI is present");
                                TotalEmiOblig = TotalEmiOblig + Double.parseDouble(emi_amt);
                            }

                            Log.consoleLog(ifr, "JSON obj RESULT::" + obj);
                            Log.consoleLog(ifr, "closed Date :: " + closed);
                            if (closed.isEmpty() || closed == null) {
                                jsonarr.add(obj);
                            }
                        }
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr.size());

                        ((IFormAPIHandler) ifr).addDataToGrid("ALV_AL_LIAB_VAL", jsonarr, true);
                        Log.consoleLog(ifr, "test1==>");
                    }
                } else {
                    Log.consoleLog(ifr, "CAIS_Account is null in response autopopulate_liability");
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "CAIS_Account is Null. Technical glitch in getting Liabilities!"));
                    return message.toString();
                }
                //ifr.setValue("FE_Obligations", String.valueOf(TotalEmiOblig));
                Log.consoleLog(ifr, "TotalEmiOblig::" + TotalEmiOblig);

            } //========================================> CIBIL response parsing 
            else if (bureauType.equalsIgnoreCase("CB")) {
                Log.consoleLog(ifr, "Bureau type: " + bureauType);
                JSONArray jsonarr1 = new JSONArray();

                String responseBodyCIBIL = "";
                if (ConfProperty.getIntegrationValue("MOCKFLG_CIBIL").equalsIgnoreCase("Y")) {

                    responseBodyCIBIL = apimr.readMockResponse(ifr, "CIBIL");
                    //responseBodyCIBIL = "{\"body\":{\"INProfileResponse\":{\"Header\":{\"SystemCode\":0,\"MessageText\":\"\",\"ReportDate\":20240205,\"ReportTime\":183314},\"UserMessage\":{\"UserMessageText\":\"Normal Response\"},\"CreditProfileHeader\":{\"Enquiry_Username\":\"cpu2canara_prod07\",\"ReportDate\":20240205,\"ReportTime\":183314,\"Version\":\"V2.4\",\"ReportNumber\":1707138193771,\"Subscriber\":\"\",\"Subscriber_Name\":\"Canara Bank\"},\"Current_Application\":{\"Current_Application_Details\":{\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"Current_Applicant_Details\":{\"Last_Name\":\"s\",\"First_Name\":\"AMARA PAVANKUMAR\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":1,\"IncomeTaxPan\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Passport_Number\":\"\",\"Passport_Issue_Date\":\"\",\"Passport_Expiration_Date\":\"\",\"Voter_s_Identity_Card\":\"\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"Ration_Card_Number\":\"\",\"Ration_Card_Issue_Date\":\"\",\"Ration_Card_Expiration_Date\":\"\",\"Universal_ID_Number\":\"\",\"Universal_ID_Issue_Date\":\"\",\"Universal_ID_Expiration_Date\":\"\",\"Date_Of_Birth_Applicant\":19890525,\"Telephone_Number_Applicant_1st\":\"\",\"Telephone_Extension\":\"\",\"Telephone_Type\":\"\",\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"Current_Other_Details\":{\"Income\":\"\",\"Marital_Status\":2,\"Employment_Status\":\"S\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":0},\"Current_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"},\"Current_Applicant_Additional_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"}}},\"CAIS_Account\":{\"CAIS_Summary\":{\"Credit_Account\":{\"CreditAccountTotal\":6,\"CreditAccountActive\":6,\"CreditAccountDefault\":0,\"CreditAccountClosed\":0,\"CADSuitFiledCurrentBalance\":0},\"Total_Outstanding_Balance\":{\"Outstanding_Balance_Secured\":4825631,\"Outstanding_Balance_Secured_Percentage\":99,\"Outstanding_Balance_UnSecured\":42529,\"Outstanding_Balance_UnSecured_Percentage\":1,\"Outstanding_Balance_All\":4868160}},\"CAIS_Account_DETAILS\":[{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C000115766\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20140721,\"Credit_Limit_Amount\":25000,\"Highest_Credit_or_Original_Loan_Amount\":\"\",\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"?00000000000000000000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20210531,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20170228,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2021,\"Month\":5,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":3,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":2510,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":2,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":1,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":12,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":11,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST TELLABA\",\"Third_Line_Of_Address_non_normalized\":\"VIA ANDRAPRADESH\",\"City_non_normalized\":\"###\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9492537874},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889254001153,\"Portfolio_Type\":\"I\",\"Account_Type\":12,\"Open_Date\":20190121,\"Highest_Credit_or_Original_Loan_Amount\":200000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20240102,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":7,\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":10,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":139710,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":117563,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":115619,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":93056,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":123065,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":[{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}]},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":2536667005808,\"Portfolio_Type\":\"M\",\"Account_Type\":2,\"Open_Date\":20190424,\"Highest_Credit_or_Original_Loan_Amount\":4500000,\"Terms_Duration\":360,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":5170000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":360,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4704642,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705175,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705017,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705367,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705026,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889694000003,\"Portfolio_Type\":\"I\",\"Account_Type\":13,\"Open_Date\":20200716,\"Highest_Credit_or_Original_Loan_Amount\":75000,\"Terms_Duration\":84,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"P\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":88000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":84,\"Promotional_Rate_Flag\":\"\",\"Income\":100001,\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20200731,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":31874,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":33059,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":34235,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":35407,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":36569,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PVTXXXXXXXX\",\"Subscriber_Name\":\"XXXXXXXXXX\",\"Account_Number\":\"XXXXXXXXXXXX2879\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20220926,\"Credit_Limit_Amount\":340000,\"Highest_Credit_or_Original_Loan_Amount\":45111,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"0000000000000???????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231112,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231023,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20221012,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":11,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":14929,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4593,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9830,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9043,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":6,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":10429,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA\",\"First_Name_Non_Normalized\":\"PAVAN\",\"Middle_Name_1_Non_Normalized\":\"KUMAR\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":[{\"First_Line_Of_Address_non_normalized\":\"CANARA BANK 2ND FLOOR  NAVEENCOMPLEX MG\",\"Second_Line_Of_Address_non_normalized\":\"ROAD HEAD OFFICE  OPP 1MG MALL\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560002,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"27   3RD FLOOR GANESH ILLAM ATMANANDA CO\",\"Second_Line_Of_Address_non_normalized\":\"LONY  SULTANPALYA MAIN ROAD RT NAGAR  NE\",\"Third_Line_Of_Address_non_normalized\":\"AR NARAYANA APARTMENT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560032,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"3 108 CHIMAKURTHI CHANDRAPADU  PRAKASAM\",\"Second_Line_Of_Address_non_normalized\":\"\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523226,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"}],\"CAIS_Holder_Phone_Details\":[{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"},{\"Telephone_Number\":2011111111,\"Telephone_Type\":3,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}],\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C117024315\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20230830,\"Credit_Limit_Amount\":100000,\"Highest_Credit_or_Original_Loan_Amount\":100000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"000?????????????????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0,\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20230930,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":48588,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":7337,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVAN KUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":560009,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}}]},\"Match_result\":{\"Exact_match\":\"Y\"},\"TotalCAPS_Summary\":{\"TotalCAPSLast7Days\":2,\"TotalCAPSLast30Days\":2,\"TotalCAPSLast90Days\":2,\"TotalCAPSLast180Days\":2},\"CAPS\":{\"CAPS_Summary\":{\"CAPSLast7Days\":2,\"CAPSLast30Days\":2,\"CAPSLast90Days\":2,\"CAPSLast180Days\":2},\"CAPS_Application_Details\":[{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":161727,\"ReportNumber\":1706611647587,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"},{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":132619,\"ReportNumber\":1706601379101,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"}]},\"NonCreditCAPS\":{\"NonCreditCAPS_Summary\":{\"NonCreditCAPSLast7Days\":0,\"NonCreditCAPSLast30Days\":0,\"NonCreditCAPSLast90Days\":0,\"NonCreditCAPSLast180Days\":0}},\"PSV\":{\"BFHL_Ex_HL\":{\"TN_of_BFHL_CAD_Ex_HL\":\"\",\"Tot_Val_of_BFHL_CAD\":\"\",\"MNT_SMR_BFHL_CAD\":\"\"},\"HL_CAD\":{\"TN_of_HL_CAD\":\"\",\"Tot_Val_of_HL_CAD\":\"\",\"MNT_SMR_HL_CAD\":\"\"},\"Telcos_CAD\":{\"TN_of_Telcos_CAD\":\"\",\"Tot_Val_of_Telcos_CAD\":\"\",\"MNT_SMR_Telcos_CAD\":\"\"},\"MF_CAD\":{\"TN_of_MF_CAD\":\"\",\"Tot_Val_of_MF_CAD\":\"\",\"MNT_SMR_MF_CAD\":\"\"},\"Retail_CAD\":{\"TN_of_Retail_CAD\":\"\",\"Tot_Val_of_Retail_CAD\":\"\",\"MNT_SMR_Retail_CAD\":\"\"},\"Total_CAD\":{\"TN_of_All_CAD\":\"\",\"Tot_Val_of_All_CAD\":\"\",\"MNT_SMR_CAD_All\":\"\"},\"BFHL_ACA_ExHL\":{\"TN_of_BFHL_ACA_Ex_HL\":\"\",\"Bal_BFHL_ACA_Ex_HL\":\"\",\"WCD_St_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_6_MNT_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_7_12_MNT_BFHL_ACA_Ex_HL\":\"\",\"Age_of_Oldest_BFHL_ACA_Ex_HL\":\"\",\"HCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\",\"TCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\"},\"HL_ACA\":{\"TN_of_HL_ACA\":\"\",\"Bal_HL_ACA\":\"\",\"WCD_St_HL_ACA\":\"\",\"WDS_Pr_6_MNT_HL_ACA\":\"\",\"WDS_Pr_7_12_MNT_HL_ACA\":\"\",\"Age_of_Oldest_HL_ACA\":\"\"},\"MF_ACA\":{\"TN_of_MF_ACA\":\"\",\"Total_Bal_MF_ACA\":\"\",\"WCD_St_MF_ACA\":\"\",\"WDS_Pr_6_MNT_MF_ACA\":\"\",\"WDS_Pr_7_12_MNT_MF_ACA\":\"\",\"Age_of_Oldest_MF_ACA\":\"\"},\"Telcos_ACA\":{\"TN_of_Telcos_ACA\":\"\",\"Total_Bal_Telcos_ACA\":\"\",\"WCD_St_Telcos_ACA\":\"\",\"WDS_Pr_6_MNT_Telcos_ACA\":\"\",\"WDS_Pr_7_12_MNT_Telcos_ACA\":\"\",\"Age_of_Oldest_Telcos_ACA\":\"\"},\"Retail_ACA\":{\"TN_of_Retail_ACA\":\"\",\"Total_Bal_Retail_ACA\":\"\",\"WCD_St_Retail_ACA\":\"\",\"WDS_Pr_6_MNT_Retail_ACA\":\"\",\"WDS_Pr_7_12_MNT_Retail_ACA\":\"\",\"Age_of_Oldest_Retail_ACA\":\"\",\"HCB_Lm_Per_Rev_Acc_Ret\":\"\",\"Tot_Cur_Bal_Lm_Per_Rev_Acc_Ret\":\"\"},\"Total_ACA\":{\"TN_of_All_ACA\":\"\",\"Bal_All_ACA_Ex_HL\":\"\",\"WCD_St_All_ACA\":\"\",\"WDS_Pr_6_MNT_All_ACA\":\"\",\"WDS_Pr_7_12_MNT_All_ACA\":\"\",\"Age_of_Oldest_All_ACA\":\"\"},\"BFHL_ICA_Ex_HL\":{\"TN_of_NDel_BFHL_InACA_Ex_HL\":\"\",\"TN_of_Del_BFHL_InACA_Ex_HL\":\"\"},\"HL_ICA\":{\"TN_of_NDel_HL_InACA\":\"\",\"TN_of_Del_HL_InACA\":\"\"},\"MF_ICA\":{\"TN_of_NDel_MF_InACA\":\"\",\"TN_of_Del_MF_InACA\":\"\"},\"Telcos_ICA\":{\"TN_of_NDel_Telcos_InACA\":\"\",\"TN_of_Del_Telcos_InACA\":\"\"},\"Retail_ICA\":{\"TN_of_NDel_Retail_InACA\":\"\",\"TN_of_Del_Retail_InACA\":\"\"},\"PSV_CAPS\":{\"BFHL_CAPS_Last_90_Days\":\"\",\"MF_CAPS_Last_90_Days\":\"\",\"Telcos_CAPS_Last_90_Days\":\"\",\"Retail_CAPS_Last_90_Days\":\"\"},\"Own_Company_Data\":{\"TN_of_OCom_CAD\":\"\",\"Tot_Val_of_OCom_CAD\":\"\",\"MNT_SMR_OCom_CAD\":\"\",\"TN_of_OCom_ACA\":\"\",\"Bal_OCom_ACA_Ex_HL\":\"\",\"Bal_OCom_ACA_HL_Only\":\"\",\"WCD_St_OCom_ACA\":\"\",\"HCB_Lm_Per_Rev_OCom_ACA\":\"\",\"TN_of_NDel_OCom_InACA\":\"\",\"TN_of_Del_OCom_InACA\":\"\",\"TN_of_OCom_CAPS_Last_90_days\":\"\"},\"Oth_CB_Information\":{\"Any_Rel_CB_Data_Dis_Y_N\":\"\",\"Oth_Rel_CB_DFC_Pos_Mat_Y_N\":\"\"},\"Indian_Market_Specific_Var\":{\"TN_of_CAD_classed_as_SFWDWO\":\"\",\"MNT_SMR_CAD_classed_as_SFWDWO\":\"\",\"Num_of_CAD_SFWDWO_Last_24_MNT\":\"\",\"Tot_Cur_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Live_UAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_UAcc\":\"\"}},\"SCORE\":{\"BureauScore\":829,\"BureauScoreConfidLevel\":\"H\",\"CreditRating\":\"\"}}}}";
                    //responseBodyCIBIL = "{  \"body\": {    \"INProfileResponse\": {      \"SCORE\": {        \"BureauScore\": 765,        \"BureauScoreConfidLevel\": \"H\",        \"CreditRating\": \"\"      },      \"Header\": {        \"ReportTime\": 155554,        \"SystemCode\": 0,        \"ReportDate\": 20230925,        \"MessageText\": \"\"      },      \"TotalCAPS_Summary\": {        \"TotalCAPSLast90Days\": 1,        \"TotalCAPSLast7Days\": 1,        \"TotalCAPSLast30Days\": 1,        \"TotalCAPSLast180Days\": 1      },      \"CreditProfileHeader\": {        \"ReportTime\": 155554,        \"Version\": \"V2.4\",        \"Subscriber\": \"\",        \"Enquiry_Username\": \"cpu2canara_prod02\",        \"ReportNumber\": 1695637552936,        \"ReportDate\": 20230925,        \"Subscriber_Name\": \"Canara Bank\"      },      \"NonCreditCAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220622,          \"Subscriber_code\": \"PVTXXXXXXXX\",          \"Enquiry_Reason\": 99,          \"Amount_Financed\": 0,          \"Duration_Of_Agreement\": 0,          \"ReportTime\": 161428,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 1,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 6381086803,            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": \"\",            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \".\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"\",            \"FlatNoPlotNoHouseNo\": \"MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": \"\",          \"ReportNumber\": 1655894668194,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"NonCreditCAPS_Summary\": {          \"NonCreditCAPSLast30Days\": 1,          \"NonCreditCAPSLast180Days\": 1,          \"NonCreditCAPSLast90Days\": 1,          \"NonCreditCAPSLast7Days\": 1        }      },      \"UserMessage\": {        \"UserMessageText\": \"Normal Response\"      },      \"CAIS_Account\": {        \"CAIS_Summary\": {          \"Total_Outstanding_Balance\": {            \"Outstanding_Balance_Secured\": 444505,            \"Outstanding_Balance_UnSecured_Percentage\": 0,            \"Outstanding_Balance_All\": 983589,            \"Outstanding_Balance_Secured_Percentage\": 45,            \"Outstanding_Balance_UnSecured\": -20          },          \"Credit_Account\": {            \"CreditAccountActive\": 6,            \"CreditAccountClosed\": 36,            \"CreditAccountDefault\": 0,            \"CreditAccountTotal\": 42,            \"CADSuitFiledCurrentBalance\": 0          }        },        \"CAIS_Account_DETAILS\": [          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160613,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSSSSSSS???????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20170630,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20170606,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842021906,            \"Date_of_Last_Payment\": 20170606,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 90000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 12          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160606,            \"Account_Type\": 0,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"0000000000000000000000000000001?0100\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": 0,            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 25,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 53,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 30,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 31,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20220731,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20220704,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 10,            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": [              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              },              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              }            ],            \"Account_Number\": 1028261000011,            \"Date_of_Last_Payment\": 20220704,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 800000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 0,            \"Terms_Duration\": \"\",            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 13          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170607,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS????????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20171031,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20171003,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842023502,            \"Date_of_Last_Payment\": 20171003,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 100000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 15          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170608,            \"Account_Type\": 7,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Surname_Non_Normalized\": \"MUMTHAJ\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"K\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS?SS?????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"XXXXXXXXXX\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": 0,            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": [              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107/5 MAIN ROAD\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PERITCHIPALAYAM,KOTTATHURAI\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"KEERANUR,DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              },              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107 NAFILA ILLAM\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PALANI MAIN ROAD\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"PALANI TK, DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              }            ],            \"Date_Reported\": 20180131,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": {              \"Telephone_Number\": \"\",              \"Mobile_Telephone_Number\": 9787458226,              \"Telephone_Type\": 1            },            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20180108,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 9,            \"Terms_Frequency\": \"M\",            \"CAIS_Holder_ID_Details\": {              \"Ration_Card_Number\": \"22G0539113\",              \"Voter_ID_Expiration_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"Ration_Card_Expiration_Date\": \"\",              \"Ration_Card_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Voter_ID_Issue_Date\": \"\",              \"Driver_License_Issue_Date\": \"\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": \"XXXXXXXXXXXXX6718\",            \"Date_of_Last_Payment\": \"\",            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 47000,            \"Settlement_Amount\": 0,            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": 0,            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 24,            \"Terms_Duration\": 24,            \"Identification_Number\": \"PUBXXXXXXXX\",            \"Account_Status\": 15          }        ]      },      \"Match_result\": {        \"Exact_match\": \"Y\"      },      \"Current_Application\": {        \"Current_Application_Details\": {          \"Current_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"S\",            \"Number_of_Major_Credit_Card_Held\": 0,            \"Marital_Status\": 2,            \"Income\": \"\"          },          \"Enquiry_Reason\": 14,          \"Current_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Amount_Financed\": 1500000,          \"Current_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Duration_Of_Agreement\": 180,          \"Finance_Purpose\": 48,          \"Current_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": \"\",            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 9787458226,            \"Middle_Name3\": \"\",            \"Passport_Number\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 1,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"K\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          }        }      },      \"CAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220210,          \"Subscriber_code\": \"OTHXXXXXXXX\",          \"Enquiry_Reason\": 14,          \"Amount_Financed\": 2500000,          \"Duration_Of_Agreement\": 180,          \"ReportTime\": 125442,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": 9787458226,            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 0,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 2,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"KAJINI\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"DINDIGUL\",            \"FlatNoPlotNoHouseNo\": \"107 5 PERITCHIPALAYAM MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"VILLAGE DINDUGUL\",            \"FlatNoPlotNoHouseNo\": \"PERICHIPALAYAM SF NO 88 2 KOTTADURAI\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"ReportNumber\": 1644477882780,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"CAPS_Summary\": {          \"CAPSLast30Days\": 0,          \"CAPSLast7Days\": 0,          \"CAPSLast180Days\": 0,          \"CAPSLast90Days\": 0        }      }    }  },  \"responseCode\": 200}";
                    Log.consoleLog(ifr, "Mock Flag is Y ::");
                } else {
                    try {
                        // String CibilResQuery = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME ='Transunion_Consumer' AND TRANSACTION_ID ='" + PID + "' AND APPLICANTTYPE ='" + ApplicantType + "'";
                        String ExperianResQuery = ConfProperty.getQueryScript("CibilResQuery").replaceAll("#PID#", PID).replaceAll("#APPLICANTTYPE#", ApplicantType);

                        List<List<String>> CibilResponse = cf.mExecuteQuery(ifr, ExperianResQuery, "Execute query for fetching CIBIL Response ");
                        if (!CibilResponse.isEmpty()) {

                            String CibilResponseStr = CibilResponse.get(0).get(0);
                            Log.consoleLog(ifr, "CibilResponseStr From Table==>" + CibilResponseStr);
                            JSONObject CibilResponseObj = (JSONObject) parser1.parse(CibilResponseStr);
                            responseBodyCIBIL = CibilResponseObj.toString();
                            Log.consoleLog(ifr, "Cibil Response from table Parsed ::");
                        }
                    } catch (Exception e) {
                        Log.consoleLog(ifr, "Exception in CibilResQuery :" + e);
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
                        return message.toString();
                    }
                }

                String BureauScore = "";
                int cibilScore = 0;
                String paymentHistory = "";
                String suitFiled = "";
                String accountType = "";
                String dateClosed = "Y";
                String currentBalance = "";
                String memberShortName = "";
                String dateOpened = "";
                String highCreditAmount = "";
                String amountOverdue = "";
                String accountNumber = "";
                String Scores = "";
                String accounts = "";
                String emiAmount = "0";
                int totalEmiAmnt = 0;
                int totalNonEmiCount = 0;

                JSONObject OutputJSON2 = (JSONObject) parser1.parse(responseBodyCIBIL);
                JSONObject resultObj1 = new JSONObject(OutputJSON2);

                Log.consoleLog(ifr, "resultObj==>" + resultObj1);

                String body = resultObj1.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser1.parse(body);

                String ControlData = bodyObj.get("controlData").toString();
                Log.consoleLog(ifr, "ControlData==>" + ControlData);

                JSONObject ControlDataObj = (JSONObject) parser1.parse(ControlData);
                String ControlDataStatus = ControlDataObj.get("success").toString();

                Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);

                String consumerCreditData = bodyObj.get("consumerCreditData").toString();
                Log.consoleLog(ifr, "consumerCreditData==>" + consumerCreditData);
                JSONArray consumerCreditDataJSON = (JSONArray) parser1.parse(consumerCreditData);
                if (!consumerCreditDataJSON.isEmpty()) {
                    for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
                        String InputString = consumerCreditDataJSON.get(i).toString();
                        JSONObject consumerCreditDataJSONObj = (JSONObject) parser1.parse(InputString);
                        Log.consoleLog(ifr, "consumerCreditDataJSON ==> " + i);
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
                JSONArray ScoresJSON = (JSONArray) parser1.parse(Scores);

                if (!ScoresJSON.isEmpty()) {
                    for (int i = 0; i < ScoresJSON.size(); i++) {
                        String InputString = ScoresJSON.get(i).toString();
                        JSONObject ScoresJSONObj = (JSONObject) parser1.parse(InputString);
                        String transCIBILScore = ScoresJSONObj.get("score").toString();
                        Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
                        cibilScore = Integer.parseInt(transCIBILScore);
                        Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
                        BureauScore = String.valueOf(cibilScore);
                        Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
                        Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
                    }
                }
                Log.consoleLog(ifr, "accounts==>" + accounts);

                if (!accounts.equalsIgnoreCase("")) {
                    JSONArray accountsJSON = (JSONArray) parser1.parse(accounts);

                    if (!accountsJSON.isEmpty()) {

                        for (int i = 0; i < accountsJSON.size(); i++) {
                            Log.consoleLog(ifr, "accountsJSON index:: " + i);

                            String InputString = accountsJSON.get(i).toString();
                            JSONObject accountsJSONObj = (JSONObject) parser1.parse(InputString);

                            if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
                                accountType = accountsJSONObj.get("accountType").toString();
                                Log.consoleLog(ifr, "accountType==>" + accountType);
                            } else {
                                Log.consoleLog(ifr, "accountType tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
                                currentBalance = accountsJSONObj.get("currentBalance").toString();
                                Log.consoleLog(ifr, "currentBalance==>" + currentBalance);

                                if (currentBalance.equalsIgnoreCase("")) {
                                    currentBalance = "";
                                }

                            } else {
                                Log.consoleLog(ifr, "currentBalance tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "memberShortName").equalsIgnoreCase(""))) {
                                memberShortName = accountsJSONObj.get("memberShortName").toString();
                                Log.consoleLog(ifr, "memberShortName==>" + memberShortName);

                                if (memberShortName.equalsIgnoreCase("")) {
                                    memberShortName = "";
                                }

                            } else {
                                Log.consoleLog(ifr, "memberShortName tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
                                dateOpened = accountsJSONObj.get("dateOpened").toString();
                                Log.consoleLog(ifr, "dateOpened==>" + dateOpened);

                                if (dateOpened.equalsIgnoreCase("")) {
                                    dateOpened = "";
                                }
                                //dateClosed
                            } else {
                                Log.consoleLog(ifr, "dateOpened tag not available for accountsJSON index ::" + i);
                            }
                            if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
                                dateClosed = accountsJSONObj.get("dateClosed").toString();
                                Log.consoleLog(ifr, "dateClosed==>" + dateClosed);

                                if (!dateClosed.equalsIgnoreCase("")) {
                                    dateClosed = "N";
                                }
                                // dateClosed
                            } else {
                                dateClosed = "Y";
                                Log.consoleLog(ifr, "dateClosed tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "highCreditAmount").equalsIgnoreCase(""))) {
                                highCreditAmount = accountsJSONObj.get("highCreditAmount").toString();
                                Log.consoleLog(ifr, "highCreditAmount==>" + highCreditAmount);

                                if (highCreditAmount.equalsIgnoreCase("")) {
                                    highCreditAmount = "";
                                }

                            } else {
                                Log.consoleLog(ifr, "highCreditAmount tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "amountOverdue").equalsIgnoreCase(""))) {
                                amountOverdue = accountsJSONObj.get("amountOverdue").toString();
                                Log.consoleLog(ifr, "amountOverdue==>" + amountOverdue);

                                if (amountOverdue.equalsIgnoreCase("")) {
                                    amountOverdue = "";
                                }

                            } else {
                                Log.consoleLog(ifr, "amountOverdue tag not available for accountsJSON index ::" + i);
                            }

                            if (!(cf.getJsonValue(accountsJSONObj, "accountNumber").equalsIgnoreCase(""))) {
                                accountNumber = accountsJSONObj.get("accountNumber").toString();
                                Log.consoleLog(ifr, "accountNumber==>" + accountNumber);

                                if (accountNumber.equalsIgnoreCase("")) {
                                    accountNumber = "";
                                }

                            } else {
                                Log.consoleLog(ifr, "accountNumber tag not available for accountsJSON index ::" + i);
                            }

                            // String queryData = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='#applicanttype#'  and PID='#ProcessInstanceId#';
                            String queryData = ConfProperty.getQueryScript("getinsertionOrderID").replaceAll("#ProcessInstanceId#", PID).replaceAll("#applicanttype#", ApplicantType);

                            List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching ApplicantName");
                            String party_type = data.get(0).get(0);
                            Log.consoleLog(ifr, "Party Type==>" + party_type);

                            JSONObject obj = new JSONObject();
                            obj.put("QNL_AL_LIAB_VAL_LoanType", accountType);
                            obj.put("QNL_AL_LIAB_VAL_ApplicantType", party_type);
                            obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                            obj.put("QNL_AL_LIAB_VAL_Bank", memberShortName);
                            obj.put("QNL_AL_LIAB_VAL_loanStartDate", dateOpened);
                            obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", highCreditAmount);
                            obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", currentBalance);
                            obj.put("QNL_AL_LIAB_VAL_Overdue", amountOverdue.equalsIgnoreCase("") ? "0.00" : amountOverdue);
                            obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", accountNumber);

                            if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
                                emiAmount = accountsJSONObj.get("emiAmount").toString();
                                Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
                                if (!emiAmount.equalsIgnoreCase("")) {
                                    obj.put("QNL_AL_LIAB_VAL_EMIAmt", emiAmount);
                                    totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
                                } else {
                                    Log.consoleLog(ifr, "emiAmount tag available but empty");
                                    obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
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
                            Log.consoleLog(ifr, "JSON obj RESULT::" + obj);

                            if (dateClosed.equalsIgnoreCase("Y")) {
                                jsonarr1.add(obj);
                                Log.consoleLog(ifr, "Added successfully==>");
                            }
                            Log.consoleLog(ifr, "test2==>");
                        }
                    }

                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr1.size());
                    Log.consoleLog(ifr, "JSONARRAY ::" + jsonarr1);
                    ((IFormAPIHandler) ifr).addDataToGrid("ALV_AL_LIAB_VAL", jsonarr1, true);
                }
            } else {
                Log.consoleLog(ifr, "Unsupported bureau type: " + bureauType);

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
            Log.errorLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
            JSONObject message = new JSONObject();
            return RLOS_Constants.ERROR;
        }
        return "";
    }

    //Modified by Keerthana on 25/07/24 for MinLoanAmt Validation
    // added by vandana for onchange Recommended Loan Amount
    //Modified by Aravindh on 05/07/24 for OnChange Validation 
    public String CalcEmiOnChangeRecommendLoan(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside CalcEmiOnChangeRecommendLoan");
        JSONObject message = new JSONObject();
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String loanTenure = null;
            //added by keerthana for pension change on 19/07/2024
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String recommendedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            Log.consoleLog(ifr, " recommendeLoanAmount" + recommendedLoanAmount);
            String requestedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_LoanAmountrequested").toString();
            Log.consoleLog(ifr, " requestedLoanAmount" + requestedLoanAmount);
            double requestedLoanAmountcheck = Double.parseDouble(requestedLoanAmount);
            String schemeID = pcm.getBaseSchemeID(ifr, ProcessInstanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String MinLoanAmout = "";
            String minLoanData_Query = ConfProperty.getQueryScript("GetMinLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> MinLoanAmtList = cf.mExecuteQuery(ifr, minLoanData_Query, "MinAmt loan Data from LoanInfo Query : ");
            if (MinLoanAmtList.size() > 0) {
                MinLoanAmout = MinLoanAmtList.get(0).get(0);
            }
            Log.consoleLog(ifr, "MinLoanAmout : " + MinLoanAmout);
            if (Integer.parseInt(recommendedLoanAmount) < Integer.parseInt(MinLoanAmout)) {
                ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Recommended Loan Amount should be greater than Minimum Eligible Amount as per your scheme."));
                message.put("saveWorkitem", "true");
                return message.toString();
            }
            double recommendedLoanAmountcheck = Double.parseDouble(recommendedLoanAmount);
            String EligibleLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_Eligibileloanamount").toString();
            Log.consoleLog(ifr, " EligibleLoanAmount " + EligibleLoanAmount);
            double EligibleLoanAmountcheck = Double.parseDouble(EligibleLoanAmount);
            if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                if (recommendedLoanAmountcheck <= EligibleLoanAmountcheck) {
                    Log.consoleLog(ifr, "Given recommendedLoanAmount is less than EligibleLoanAmount");
                    loanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
                    Log.consoleLog(ifr, "loanTenure : " + loanTenure);
                    String loanROI = ifr.getValue("QNL_LA_FINALELIGIBILITY_ROI").toString();
                    Log.consoleLog(ifr, "loanROI : " + loanROI);
                    String grossAmountStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
                    //String recommendeLoanAmountCalc = recommendeLoanAmount;
                    Log.consoleLog(ifr, " recommendeLoanAmount" + recommendedLoanAmount);
                    String recommendeLoanAmountCalc = "-" + recommendedLoanAmount;
                    BigDecimal loanroii = new BigDecimal(loanROI);

                    BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanAmountCalc, loanroii, Integer.parseInt(loanTenure));
                    Log.consoleLog(ifr, "emi : " + emi);
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", emi.toString());

                    // NetIncome > EMI Validation BY Aravindh.K.K on 16/08/24
                    String NetIncomeStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_NetIncome").toString();
                    Log.consoleLog(ifr, "NetIncome : " + NetIncomeStr);
                    BigDecimal NetIncome = new BigDecimal(NetIncomeStr);
                    if (NetIncome.compareTo(emi) < 0) {
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                        //message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "You are not eligible to avail beyond this tenure as per bank's guidelines."));
                        message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Not Eligible for this combination."));
                        return message.toString();
                    }

                    BigDecimal grossAmount = new BigDecimal(grossAmountStr);
                    BigDecimal recommendedLoanAmountstr = new BigDecimal(recommendedLoanAmount);
                    BigDecimal salaryMultiplier = recommendedLoanAmountstr.divide(grossAmount, MathContext.DECIMAL128).setScale(2, RoundingMode.DOWN);
                    Log.consoleLog(ifr, "Inside salaryMultiplier: " + salaryMultiplier);
                    String salaryMultiplierStr = salaryMultiplier.toString();
                    Log.consoleLog(ifr, "Inside SalaryMultiplier" + salaryMultiplierStr);
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser", salaryMultiplierStr);

                    String deductionsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageDeductions").toString();
                    Log.consoleLog(ifr, "deductionsStr : " + deductionsStr);
                    BigDecimal AverageDeductions = new BigDecimal(deductionsStr);

                    String ObligationsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_Obligations").toString();
                    Log.consoleLog(ifr, "ObligationsStr : " + ObligationsStr);
                    BigDecimal Obligations = new BigDecimal(ObligationsStr);
                    //modified by keerthana for pension change on 19/07/2024   
                    // NTH as per Recommended Loan Amount
                    String configAmt = "";
                    BigDecimal NthAsperRecommendLoan = grossAmount.subtract(AverageDeductions).subtract(Obligations).subtract(emi);
                    Log.consoleLog(ifr, "NthAsperRecommendLoan : " + NthAsperRecommendLoan.toString());
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", NthAsperRecommendLoan.toString());
                    if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                        configAmt = pcm.mCheckBigDecimalValue(ifr, pcm.getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "NETTAKEHOMESALARY")).toString();
                        Log.consoleLog(ifr, "CmpNTHsalary : " + configAmt);
                        BigDecimal CmpNTHsalary = new BigDecimal(configAmt);
                        if (NthAsperRecommendLoan.compareTo(CmpNTHsalary) > 0) {
                            // NTH(%) Percentage as per Recommended Loan Amount
                            BigDecimal HundredPerc = new BigDecimal("100");
                            BigDecimal NthPERCAsperRecommendLoan = (NthAsperRecommendLoan.divide(grossAmount, MathContext.DECIMAL128)).multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                            Log.consoleLog(ifr, "NthAsperRecommendLoan : " + NthAsperRecommendLoan.toString());
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", NthPERCAsperRecommendLoan.toString());
                        } else {

                            //ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", "");
                            //ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "");
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", CmpNTHsalary.toString());
                            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "NTH set to a default value of 10,000"));
                            message.put("saveWorkitem", "true");
                            return message.toString();
                        }
                    } //modified by ishwarya on 29/07/2024
                    else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                        String f_key = bpcc.Fkey(ifr, "B");
                        String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoVL").replaceAll("#f_key#", f_key);
                        List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching income data from portal");
                        String occupationType = IncomeDtsPortal.get(0).get(0);
                        Log.consoleLog(ifr, "occupationType ::" + occupationType);
                        String Purpose = null;
                        String nthSaraly = null;
                        String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                        List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
                        if (PurposePortal.size() > 0) {
                            Purpose = PurposePortal.get(0).get(0);
                        }
                        Log.consoleLog(ifr, "occupationType ::" + occupationType);
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", NthAsperRecommendLoan.toString());
                        if (occupationType.equalsIgnoreCase("Salaried") || !occupationType.equalsIgnoreCase("Salaried") && Purpose.equalsIgnoreCase("N2W")) {
                            Log.consoleLog(ifr, "inside purpose two wheeler " + Purpose);
                            configAmt = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_TWL");//35
                        } else if (occupationType.equalsIgnoreCase("Salaried") || !occupationType.equalsIgnoreCase("Salaried") && Purpose.equalsIgnoreCase("N4W")) {
                            Log.consoleLog(ifr, "inside purpose four wheeler " + Purpose);
                            configAmt = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_FWL");//25
                            nthSaraly = ConfProperty.getCommonPropertyValue("NTHSALARY_VL");//12000
                        } else if (occupationType.equalsIgnoreCase("Pen") && Purpose.equalsIgnoreCase("N4W")) {
                            Log.consoleLog(ifr, "inside purpose four wheeler and occupation type pension " + Purpose + occupationType);
                            configAmt = ConfProperty.getCommonPropertyValue("NTHPERCENTAGEVL_FWLPEN");//50
                            nthSaraly = ConfProperty.getCommonPropertyValue("NTHSALARY_VL");//12000
                        }
                        Log.consoleLog(ifr, "CmpNTHsalary : " + configAmt);
                        BigDecimal CmpNTHsalary = new BigDecimal(configAmt);
                        Log.consoleLog(ifr, "nthPercentage_final" + CmpNTHsalary);
                        BigDecimal HundredPerc = new BigDecimal("100");
                        BigDecimal nthSaraly_final = pcm.mCheckBigDecimalValue(ifr, nthSaraly);
                        Log.consoleLog(ifr, "nthSaraly" + nthSaraly);
                        BigDecimal netTakeHomeNTH;
                        if (Purpose.equalsIgnoreCase("N4W")) {
                            Log.consoleLog(ifr, "inside purpose four wheeler " + Purpose);
                            int comparisonResult = grossAmount.compareTo(nthSaraly_final);
                            if (comparisonResult > 0) {
                                netTakeHomeNTH = NthAsperRecommendLoan.divide(grossAmount, MathContext.DECIMAL128)
                                        .multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                            } else {
                                netTakeHomeNTH = NthAsperRecommendLoan.divide(nthSaraly_final, MathContext.DECIMAL128)
                                        .multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                            }
                            Log.consoleLog(ifr, "netTakeHomeNTH===> " + netTakeHomeNTH);
                        } else {
                            Log.consoleLog(ifr, "inside purpose two wheeler " + Purpose);
                            netTakeHomeNTH = NthAsperRecommendLoan.divide(grossAmount, MathContext.DECIMAL128)
                                    .multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                            Log.consoleLog(ifr, "netTakeHomeNTH===> " + netTakeHomeNTH);
                        }

                        if (netTakeHomeNTH.compareTo(new BigDecimal(configAmt)) < 0) {
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "");
                            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "error", "NTH Percentage should not be lesser than 40%"));
                            message.put("saveWorkitem", "true");
                            return message.toString();
                        }
                        //for 2 wheeler maximum quantum added by sharon on 21-08-2024
                        Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::Purpose ::" + Purpose);
                        String CRG = "";
                        String Risk = "";
                        String RecLoanAmt = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
                        String MinQuantumn = "150000";
                        Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::RecLoanAmt ::" + RecLoanAmt);
                        Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::MinQuantumn ::" + MinQuantumn);
                        BigDecimal RecLoanAmnt = new BigDecimal(RecLoanAmt);
                        BigDecimal MinQuantumnamt = new BigDecimal(MinQuantumn);
                        Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::RecLoanAmnt ::" + RecLoanAmnt);
                        Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::MinQuantumnamt ::" + MinQuantumnamt);
                        if (Purpose.equalsIgnoreCase("N2W")) {
                            Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::Purpose inside N2W ::" + Purpose);
                            String RiskQuery = "select CANARARETAILGRADE,RISKSCORE_RATING from LOS_NL_R_SCORERATING where pid='" + ProcessInstanceId + "' and  PARTYTYPE like 'Borrower%' and APPLICANTCODETYPE='B'";
                            List<List<String>> RiskQueryB = cf.mExecuteQuery(ifr, RiskQuery, "Execute query for fetching Risk data for B");
                            if (!RiskQueryB.isEmpty()) {
                                CRG = RiskQueryB.get(0).get(0);
                                Risk = RiskQueryB.get(0).get(1);
                            }
                            Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::CRG ::" + CRG);
                            Log.consoleLog(ifr, "BudgetBKO::CalcEmiOnChangeRecommendLoan::Risk ::" + Risk);

                            if ((CRG.equalsIgnoreCase("CRG-3") && Risk.equalsIgnoreCase("Moderate Risk"))
                                    || (CRG.equalsIgnoreCase("CRG-4") && Risk.equalsIgnoreCase("High Risk"))) {
                                if (RecLoanAmnt.compareTo(MinQuantumnamt) > 0){
                                ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Recommended Loan Amount for Two wheeler should be less than or equal to Rs.1.50 lakhs for risk moderate and above."));
                                message.put("saveWorkitem", "true");
                                return message.toString();
                            }

                        }
                        }

                        ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", netTakeHomeNTH.toString());
                    }
                } else {
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                    message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Recommended Loan Amount should be less than or equal to Eligible Loan Amount"));
                    message.put("saveWorkitem", "true");
                    return message.toString();
                }

            } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                if (recommendedLoanAmountcheck <= requestedLoanAmountcheck) {
                    Log.consoleLog(ifr, "Given recommendedLoanAmount is less than requestedLoanAmountcheck pension check");
                    loanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
                    Log.consoleLog(ifr, "loanTenure : " + loanTenure);
                    String loanROI = ifr.getValue("QNL_LA_FINALELIGIBILITY_ROI").toString();
                    Log.consoleLog(ifr, "loanROI : " + loanROI);
                    String grossAmountStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
                    Log.consoleLog(ifr, " recommendeLoanAmount" + recommendedLoanAmount);
                    String recommendeLoanAmountCalc = "-" + recommendedLoanAmount;
                    BigDecimal loanroii = new BigDecimal(loanROI);
                    BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanAmountCalc, loanroii, Integer.parseInt(loanTenure));
                    Log.consoleLog(ifr, "emi : " + emi);
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", emi.toString());
                    BigDecimal grossAmount = new BigDecimal(grossAmountStr);
                    BigDecimal recommendedLoanAmountstr = new BigDecimal(recommendedLoanAmount);
                    BigDecimal salaryMultiplier = recommendedLoanAmountstr.divide(grossAmount, MathContext.DECIMAL128).setScale(2, RoundingMode.DOWN);
                    Log.consoleLog(ifr, "Inside salaryMultiplier: " + salaryMultiplier);
                    String salaryMultiplierStr = salaryMultiplier.toString();
                    Log.consoleLog(ifr, "Inside SalaryMultiplier" + salaryMultiplierStr);
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser", salaryMultiplierStr);
                    String deductionsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageDeductions").toString();
                    Log.consoleLog(ifr, "deductionsStr : " + deductionsStr);
                    BigDecimal AverageDeductions = new BigDecimal(deductionsStr);
                    String ObligationsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_Obligations").toString();
                    Log.consoleLog(ifr, "ObligationsStr : " + ObligationsStr);
                    BigDecimal Obligations = new BigDecimal(ObligationsStr);
                    String configAmt = "";
                    BigDecimal NthAsperRecommendLoan = grossAmount.subtract(AverageDeductions).subtract(Obligations).subtract(emi);
                    Log.consoleLog(ifr, "NthAsperRecommendLoan : " + NthAsperRecommendLoan.toString());
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", NthAsperRecommendLoan.toString());
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", NthAsperRecommendLoan.toString());
                    configAmt = pcm.mCheckBigDecimalValue(ifr, pcm.getParamConfig(ifr, "PL", "STP-CP", "LOANELIGIBILITY", "NETHOMEPERENT")).toString();
                    Log.consoleLog(ifr, "CmpNTHsalary : " + configAmt);
                    BigDecimal CmpNTHsalary = new BigDecimal(configAmt);
                    BigDecimal HundredPerc = new BigDecimal("100");
                    BigDecimal NthPERCAsperRecommendLoan = (NthAsperRecommendLoan.divide(grossAmount, MathContext.DECIMAL128)).multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                    String recommloanAmt = NthPERCAsperRecommendLoan.toString();
                    Log.consoleLog(ifr, "NthAsperRecommendLoan : " + NthPERCAsperRecommendLoan.toString());
                    if (NthPERCAsperRecommendLoan.compareTo(new BigDecimal(configAmt)) < 0) {
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "");
                        message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "error", "NTH Percentage should not be lesser than 40%"));
                        message.put("saveWorkitem", "true");
                        return message.toString();
                    }
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", NthPERCAsperRecommendLoan.toString());
                } else {
                    ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                    message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Recommended Loan Amount should be less than or equal to Requested Loan Amount"));
                    message.put("saveWorkitem", "true");
                    return message.toString();
                }
            }
        } catch (Exception e) {

            Log.consoleLog(ifr, "Exception in caluclateEMI" + e);
            ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Kindly Enter Valid Amount."));
            message.put("saveWorkitem", "true");
            return message.toString();
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    //Added By Vandana    
    //Modified by Aravindh for OnChange Validation on 09/07/24
    //Modified by Aravindh for OnChange Validation on 22/07/24 for Min/Max tenure Handling
    //Modified by Aravindh for OnChange Validation on 8/8/24 for (NetIncome > Emi) Onchange tenure Handling
    public String calclateEMIOnTenureChange(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside calclateEMIOnTenureChange");
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String RecommendloanTenure = "";
            String recommendeLoanAmount = "";
            String loanROI = "";
            RecommendloanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
            Log.consoleLog(ifr, "FINALELIGIBILITY loanTenure==>" + RecommendloanTenure);
            loanROI = ifr.getValue("QNL_LA_FINALELIGIBILITY_ROI").toString();
            Log.consoleLog(ifr, "FINALELIGIBILITY loanROI==>" + loanROI);
            double RecommendloanTenureCheck = Double.parseDouble(RecommendloanTenure);

            String MaxLoanTenure = "";
            String MinLoanTenure = "";
            String Fkey = bpcc.Fkey(ifr, "B");
            String schemeID = pcm.getBaseSchemeID(ifr, ProcessInstanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            //String tenureData_Query = "select maxtenure,mintenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
            String tenureData_Query = ConfProperty.getQueryScript("getTenureDataQuery").replaceAll("#schemeID#", schemeID);
            List<List<String>> MaxMinLoanTenureList = cf.mExecuteQuery(ifr, tenureData_Query, "MinMax tenure Data from LoanInfo Query : ");
            if (MaxMinLoanTenureList.size() > 0) {
                MaxLoanTenure = MaxMinLoanTenureList.get(0).get(0);
                MinLoanTenure = MaxMinLoanTenureList.get(0).get(1);
            }
            Log.consoleLog(ifr, "Max LoanTenure : " + MaxLoanTenure);
            Log.consoleLog(ifr, "Min LoanTenure : " + MinLoanTenure);
            String superAnnuationTenure = "";
            //String tenureData_Query1 = ConfProperty.getQueryScript("CBTENUREQUERY").replaceAll("#Fkey#", Fkey);
            String tenureData_Query1 = "SELECT FLOOR(MONTHS_BETWEEN(DATEOFSUPERANNUATION, sysdate))AS month_difference from LOS_NL_Occupation_INFO Where F_KEY='" + Fkey + "'";
            List<List<String>> superannotationL = cf.mExecuteQuery(ifr, tenureData_Query1, "Based on super annotation:");
            if (superannotationL.size() > 0) {
                superAnnuationTenure = superannotationL.get(0).get(0);
            }
            Log.consoleLog(ifr, "superAnnuationTenure : " + superAnnuationTenure);
            String FinalLoanTenure = MaxLoanTenure;
            if (Integer.parseInt(superAnnuationTenure) <= Integer.parseInt(MaxLoanTenure)) {
                FinalLoanTenure = superAnnuationTenure;
            }
            Log.consoleLog(ifr, "Final after Validation FinalLoanTenure : " + FinalLoanTenure);
            double FinalLoanTenureCheck = Double.parseDouble(FinalLoanTenure);
            recommendeLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            Log.consoleLog(ifr, "FINALELIGIBILITY recommendeLoanAmount==>" + recommendeLoanAmount);
            if (!recommendeLoanAmount.isEmpty()) {
                if (!RecommendloanTenure.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "MaxLoanTenure : " + MaxLoanTenure);

                    if (RecommendloanTenureCheck <= Double.parseDouble(MaxLoanTenure)) {
                        Log.consoleLog(ifr, "RecommendloanTenure is less than MaxLoanTenure: " + RecommendloanTenure);

                        if (RecommendloanTenureCheck <= FinalLoanTenureCheck) {
                            Log.consoleLog(ifr, "RecommendloanTenure is less than Retirement Tenure: " + RecommendloanTenure);

                            recommendeLoanAmount = "-" + recommendeLoanAmount;
                            BigDecimal loanroii = new BigDecimal(loanROI);

                            BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanAmount, loanroii, Integer.parseInt(RecommendloanTenure));
                            Log.consoleLog(ifr, "emi : " + emi);
                            String EMI = emi.toString();
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", EMI);

                            String NetIncomeStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_NetIncome").toString();
                            Log.consoleLog(ifr, "NetIncome : " + NetIncomeStr);
                            BigDecimal NetIncome = new BigDecimal(NetIncomeStr);
                            if (NetIncome.compareTo(emi) < 0) {
                                ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                                JSONObject message = new JSONObject();
                                //message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "You are not eligible to avail beyond this tenure as per bank's guidelines."));
                                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Not Eligible for this combination."));
                                return message.toString();
                            }

                        } else {
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                            JSONObject message = new JSONObject();
                            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Recommended Tenure cannot be greater than Retirement Date."));
                            return message.toString();
                        }
                    } else {
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Recommended Tenure cannot be greater than Maximum Tenure as per product."));
                        return message.toString();
                    }
//                  String updateQuery= "UPDATE los_nl_la_finaleligibility SET EMIRECOMMENDEDLOANAMOUNT ='"+emi+"' WHERE PID='"+ProcessInstanceId+"' ";
//                  ifr.saveDataInDB(updateQuery);
//                  ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", ("₹ " + emi));
//                String Tenure = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Tenure").toString();
//                ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, 12, EMI);
//                ifr.setTableCellValue("ALV_FINAL_ELIGIBILITY", 0, 13, Tenure);
                    //Commented by Aravindh on 08/07/24
                } else {

                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Please Enter the Valid Tenure"));
                    return message.toString();
                }
            } else {
                ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Please Enter the Valid recommended LoanAmount"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    // added by Aravindh on 05/07/24
    // modified by Keerthana 30-07-2024
    public String caluclateEMIOnChangeApprovedLoanAmount(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside caluclateEMIOnChangeAprrovedLoanAmount");
        JSONObject message = new JSONObject();
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            Log.consoleLog(ifr, "Given recommendedLoanAmount is less than EligibleLoanAmount");
            String ApprovedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT").toString();
            Log.consoleLog(ifr, " APPROVED_LOANAMOUNT" + ApprovedLoanAmount);
            double APPROVEDLoanAmountCheck = Double.parseDouble(ApprovedLoanAmount);
            String recommendedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            Log.consoleLog(ifr, " recommendeLoanAmount" + recommendedLoanAmount);
            double recommendedLoanAmountcheck = Double.parseDouble(recommendedLoanAmount);
            String schemeID = pcm.getBaseSchemeID(ifr, ProcessInstanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String MinLoanAmout = "";
            String minLoanData_Query = ConfProperty.getQueryScript("GetMinLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> MinLoanAmtList = cf.mExecuteQuery(ifr, minLoanData_Query, "MinAmt loan Data from LoanInfo Query : ");
            if (MinLoanAmtList.size() > 0) {
                MinLoanAmout = MinLoanAmtList.get(0).get(0);
            }
            Log.consoleLog(ifr, "MinLoanAmout : " + MinLoanAmout);
            if (Integer.parseInt(ApprovedLoanAmount) < Integer.parseInt(MinLoanAmout)) {
                ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "error", "Approved Loan Amount should be greater than Minimum Eligible Amount as per your scheme."));
                message.put("saveWorkitem", "true");
                return message.toString();
            }
            if (APPROVEDLoanAmountCheck <= recommendedLoanAmountcheck) {

                String loanTenure = null;
                loanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
                Log.consoleLog(ifr, "loanTenure : " + loanTenure);
                String loanROI = ifr.getValue("QNL_LA_FINALELIGIBILITY_ROI").toString();
                Log.consoleLog(ifr, "loanROI : " + loanROI);

                String grossAmountStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
                Log.consoleLog(ifr, " grossAmountStr" + grossAmountStr);

                String APPROVED_LOANAMOUNTCalc = "-" + ApprovedLoanAmount; // for calculateEMIPMT calculation
                BigDecimal loanroii = new BigDecimal(loanROI);

                BigDecimal emi = pcm.calculateEMIPMT(ifr, APPROVED_LOANAMOUNTCalc, loanroii, Integer.parseInt(loanTenure));
                Log.consoleLog(ifr, "emi : " + emi);
                ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", emi.toString());

            } else {
                ifr.setValue("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT", "error", "Approved Loan Amount should not be greater than Recommended Loan Amount"));
                message.put("saveWorkitem", "true");
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    // added by vandana
    public void listviewMisData(IFormReference ifr) {
        Log.consoleLog(ifr, "inside listviewMisData BudgetBkoffCustomCode::");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside listviewMisData Budget ");
            String nonEditableFileds = "MIS_MINORITIES,MIS_CASTE,MIS_CUST_TYPE,MIS_CUST_STAT,MIS_CUST_CATEGORY,MIS_CUST_SPECIAL_CATEGORY,MIS_WEAKER_SECTOR,MIS_CUSTOMER_PREFERRED_LANGUAGE,MIS_INTERNAL_RATING_DETAILS,MIS_BSR_CODE,MIS_RETAIL_ASSET_HUB";

            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursment Maker")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursment Checker")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Reviewer")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Convenor")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("postSanction")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Deviation")) {
                pcm.controlDisable(ifr, nonEditableFileds);
            }
        }
    }

    // added by vandana 16-07-2024
    // modified by vandana 23-07-2024
    public String scoreCardDecisionForLeadcapture(IFormReference ifr, String applicantType) {
        String scoreCardDecisionForLeadcapture = "";
        try {
            Log.consoleLog(ifr, "inside Lead Capture scorecard brms:::");
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

            String APIResponse = bpcc.mGetAPIData(ifr);

            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String Age = cf.getJsonValue(obj, "Age");
            String Years = "0";
            Years = cf.getJsonValue(obj, "Years");
            String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
            writeOffPresent = writeOffPresent.equalsIgnoreCase("NA") ? "No" : "Yes";
            String loanTenure = null;

            String occupationtype = "";
            String category = "";
            String grossSalary = "";
            String deductionSalary = "";
            String expYears = "";
            //  String fkey="select * from LOS_NL_BASIC_INFO where pid='"+ProcessInstanceId+"' and APPLICANTTYPE='CB'";
            String fkey = bpcc.Fkey(ifr, applicantType);
            String F_key = bpcc.Fkey(ifr, "B");
            String occupationType = "select SPROFILE from los_nl_occupation_info where f_key='" + fkey + "'";
            List<List<String>> OccupationType = ifr.getDataFromDB(occupationType);
            if (!OccupationType.isEmpty()) {
                occupationtype = OccupationType.get(0).get(0);
                Log.consoleLog(ifr, "occupationtype:::: " + occupationtype);
            }

            if (occupationtype.equalsIgnoreCase("NIE")) {
                Log.consoleLog(ifr, "Non Income Earners :::: ");

                String occupationDetails = "select GROSSSALARY,DEDUCTIONMONTH,CATEGORY,TO_CHAR (DATEOFSUPERANNUATION,'DD/MM/YYYY'),CURRENTINYEARS from los_nl_occupation_info where f_key='" + F_key + "'";
                List<List<String>> occupationdetails = ifr.getDataFromDB(occupationDetails);
                if (!occupationdetails.isEmpty()) {
                    grossSalary = occupationdetails.get(0).get(0);
                    deductionSalary = occupationdetails.get(0).get(1);
                    category = occupationdetails.get(0).get(2);
                    String loantenure = occupationdetails.get(0).get(3);
                    expYears = occupationdetails.get(0).get(4);

                    int months = bpcc.differenceInMonths(ifr, loantenure);
                    loanTenure = String.valueOf(months);
                }

            } else {

                String occupationDetails = "select GROSSSALARY,DEDUCTIONMONTH,CATEGORY,TO_CHAR (DATEOFSUPERANNUATION,'DD/MM/YYYY'),CURRENTINYEARS from los_nl_occupation_info where f_key='" + fkey + "'";
                List<List<String>> occupationdetails = ifr.getDataFromDB(occupationDetails);
                if (!occupationdetails.isEmpty()) {
                    grossSalary = occupationdetails.get(0).get(0);
                    deductionSalary = occupationdetails.get(0).get(1);
                    category = occupationdetails.get(0).get(2);
                    String loantenure = occupationdetails.get(0).get(3);
                    expYears = occupationdetails.get(0).get(4);
                    Log.consoleLog(ifr, "occupationtype:::: " + occupationtype);
                    int months = bpcc.differenceInMonths(ifr, loantenure);
                    loanTenure = String.valueOf(months);
                }
            }
            ///modified by vandana on 26/06/2024 for scorecard ends
            String loanROI = pcm.mGetROICB(ifr);

            Log.consoleLog(ifr, "roi : " + loanROI);

            String settleHistory = "";
            String GUARANTORNPAINP = "";
            String GUARANTORWRITEOFFSETTLEDHIST = "";
            String BureauDataResponseBorrower = cm.getMaxCICScoreDatas(ifr, applicantType);
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

            //npa_inp = (npa_inp.equalsIgnoreCase("NA") || npa_inp.equalsIgnoreCase("")) ? "No" : "Yes";
            Log.consoleLog(ifr, "npa_inp:::: " + npa_inp);
            Log.consoleLog(ifr, "settleHistory:::: " + settleHistory);
            Log.consoleLog(ifr, "GUARANTORNPAINP:::: " + GUARANTORNPAINP);
            Log.consoleLog(ifr, "GUARANTORWRITEOFFSETTLEDHIST:::: " + GUARANTORWRITEOFFSETTLEDHIST);
            monthlydeduction = monthlydeduction.equalsIgnoreCase("") ? "0" : monthlydeduction;

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
            String highestRangeKey = bpcc.getRangeKey(maxPaymentHistory);

            // Count numbers within the highest range
            int countInHighestRange = bpcc.getCountInRange(countMap, highestRangeKey);

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

            String getOccupationDetails = "select NATUREOFSECURITY,RECOVERYMEACHANISM,RESIDENCE,RELATIONSHIPWITHBANK from LOS_NL_BASIC_INFO where pid='" + ProcessInstanceId + "' and APPLICANTTYPE='" + applicantType + "'";
            // List<List<String>> getOccupationdetails = ifr.getDataFromDB(getOccupationDetails);
            List<List<String>> getOccupationdetails = cf.mExecuteQuery(ifr, getOccupationDetails, "getOccupationDetails");
            if (!getOccupationdetails.isEmpty()) {
                natureOfSecurity = getOccupationdetails.get(0).get(0);
                recovery = getOccupationdetails.get(0).get(1);
                residence_inp = getOccupationdetails.get(0).get(2);
                ExistInpRange = getOccupationdetails.get(0).get(3);

            }

            String guarantorwriteoff = GUARANTORWRITEOFFSETTLEDHIST;

            String nparestmonths_inpo = settleHistory;

            String settledhist_inp = settleHistory;

            String writeOff = settleHistory;

            String guarantornpa_inp = GUARANTORNPAINP;

            String guarantorsettledhist_inp = GUARANTORWRITEOFFSETTLEDHIST;

            String Updatequery = "Update LOS_NL_BASIC_INFO set OVERDUEINCREDITHISTORY='" + overduedays_inps + "' , SETTLEDACCOUNTINCREDITHISTORY ='" + settledhist_inp + "' where PID='" + ProcessInstanceId + "' and APPLICANTTYPE='" + applicantType + "'";
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
            Log.consoleLog(ifr, "bscore:::" + bscore + ",overduedays_inp:::" + overduedays_inp + ",overduedaysupto::" + overduedaysupto + ",guarantornpa_inp::" + guarantornpa_inp + ",guarantorwriteoff:::"
                    + guarantorwriteoff + ",npa_inp:::" + npa_inp + ",nparestmonths_inpo:::" + nparestmonths_inpo + ",settledhist_inp::" + settledhist_inp + ",writeOffPresent:::" + writeOffPresent
                    + ",nethomeins_inprange:::" + nethomeins_inprange + ",category:::" + category + ",anninc_inp:::" + anninc_inp + ",grossSalary:::" + grossSalary + ",expYears:::" + expYears + ",existcust_inp:::" + existcust_inp
                    + ",ExistInpRange::" + ExistInpRange + ",natureOfSecurity:::" + natureOfSecurity + ",residence_inp:::" + residence_inp + ",recovery:::" + recovery);
            String scorecardInParams = "";

            if (!bscore.equalsIgnoreCase("I")) {

                scorecardInParams = bscore + "," + overduedays_inp + "," + overduedaysupto + "," + guarantorsettledhist_inp + "," + guarantornpa_inp + ","
                        + guarantorwriteoff + "," + npa_inp + "," + nparestmonths_inpo + "," + settledhist_inp + "," + writeOff
                        + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                        + "," + ExistInpRange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                scoreCardDecisionForLeadcapture = bpcc.checkScoreCard(ifr, "CB_SCORECARD", scorecardInParams, "totalgrade_out", applicantType);
            } else {

                scorecardInParams = npa_inp + "," + nethomeins_inprange + "," + category + "," + anninc_inp + "," + grossSalary + "," + expYears + "," + existcust_inp
                        + "," + ExistInpRange + "," + natureOfSecurity + "," + residence_inp + "," + recovery;

                scoreCardDecisionForLeadcapture = bpcc.checkScoreCard(ifr, "CB_ScoreCard_CICImmune", scorecardInParams, "totalgrade_out", "CB");

            }
            if (scoreCardDecisionForLeadcapture.equalsIgnoreCase(RLOS_Constants.ERROR)) {//Added by Ahmed for Error handling
                return pcm.returnError(ifr);
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in scorecardFor Co-Applicant brms::" + e);
            return pcm.returnError(ifr);
        }
        return scoreCardDecisionForLeadcapture;
    }

}
