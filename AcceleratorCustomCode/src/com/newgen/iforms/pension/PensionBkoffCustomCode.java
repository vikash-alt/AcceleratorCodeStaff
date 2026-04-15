/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.pension;

import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.json.simple.JSONArray;
import com.newgen.iforms.budget.BudgetBkoffCustomCode;
import com.newgen.iforms.constants.RLOS_Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author monesh.kumar
 */
public class PensionBkoffCustomCode {

    PortalCommonMethods pcm = new PortalCommonMethods();
    BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
    CommonFunctionality cf = new CommonFunctionality();
    PensionLoanPortalCustomCode plpc = new PensionLoanPortalCustomCode();
    BudgetBkoffCustomCode bbcc = new BudgetBkoffCustomCode();
    CommonMethods objcm = new CommonMethods();
    //added by keerthana for pension visibility changes on 16/07/2024

    public void formLoadSanctionCP(IFormReference ifr) {
        ifr.setTabStyle("tab1", "8", "visible", "false");//Collateral
        ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
        ifr.setTabStyle("tab1", "15", "visible", "false");//Risk
        ifr.setTabStyle("tab1", "16", "visible", "false");//E-signTab
        ifr.setTabStyle("tab1", "7", "visible", "false");//Eligibility
        ifr.setStyle("CTRID_PD_FETCHEXTCUST", "visible", "false");
        ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
        ifr.setStyle("BTN_Dedupe_Click", "visible", "false");
        String nonVisibleSectionGrids = "F_Mortgage,F_CollateralSummary,F_Risk_Rating,F_Bureau_NegativeDetails,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_CoborrowerEligibility_Sec,F_Deviation,ALV_Deviations,F_Deviation,F_ESIGN_ID,ALV_SCORERATING,F_CoborrowerEligibility_Sec";
        pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
    }

    //added by keerthana for pension visibility changes on 16/07/2024
    public void formLoadDisbursementMakerVisibilityCP(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
        ifr.setTabStyle("tab1", "14", "visible", "true");//Disbursement
        ifr.setTabStyle("tab1", "8", "visible", "false");//Collateral
        ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
        ifr.setTabStyle("tab1", "15", "visible", "false");//Risk
        ifr.setTabStyle("tab1", "16", "visible", "false");//E-signTab
        ifr.setStyle("CTRID_PD_FETCHEXTCUST", "visible", "false");
        ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
        ifr.setStyle("BTN_Dedupe_Click", "visible", "false");
        String ActivityName = ifr.getActivityName();
        if (ActivityName.equalsIgnoreCase("Disbursement Checker")) {
            Log.consoleLog(ifr, "inside formLoadDisbursementMakerVisibilityCP PensionBkoffCustomCode:: Convenor ");
            String QueSamp2 = "QL_SOURCINGINFO_RefCustomerName_patternString,QL_SOURCINGINFO_RefCust_CIFID,QL_SOURCINGINFO_TEST,QL_SOURCINGINFO_Search_Term,QL_SOURCINGINFO_Search_Criteria,QL_SOURCINGINFO_AgentName,QL_SOURCINGINFO_AgentCode,QL_SOURCINGINFO_BuilderName,QL_SOURCINGINFO_BuilderCode,QL_SOURCINGINFO_OtherReference,QL_SOURCINGINFO_SourceOfficerDesignation,QL_SOURCINGINFO_SourcingOfficerName"
                    + ",QL_SOURCINGINFO_SourcingEmployeeId,QL_SOURCINGINFO_CampaignName,QL_SOURCINGINFO_CampaignCode,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_EmpNameID,QL_SOURCINGINFO_EmpNameID,QL_SOURCINGINFO_RefCustomerName,BTN_SEARCHSOURCE,SourceWise,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch";
            pcm.controlinvisiblity(ifr, QueSamp2);
        }
        String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,F_ESIGN_ID";
        pcm.controlvisiblity(ifr, visibleSectionsGrids);
        String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
        pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
        String DisbursmentVisibleFields = "QNL_DISBURSMENT_ProbableDateofDisbursement,QNL_DISBURSMENT_ProposedLoanAmount,QNL_DISBURSMENT_InstallmentAmount,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_RepaymentType,QNL_DISBURSMENT_OtherFeeAndCharges,QNL_DISBURSMENT_DateofDisbursement,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_FixedTerm,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerName";
        pcm.controlvisiblity(ifr, DisbursmentVisibleFields);
        String DisbursmentHideFields = "datepick474,combo1737,datepick475";
        pcm.controlinvisiblity(ifr, DisbursmentHideFields);
        Log.consoleLog(ifr, " DisbursementMaker Pension Code End");
    }

    public String calclateEMIOnTenureChangePension(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside calclateEMIOnTenureChangePension");
        try {
            JSONObject message = new JSONObject();
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
            String MaxLoanTenure = null;
            String Fkey = bpcc.Fkey(ifr, "B");
            String custAge = "";
            String mobile_no = "";
            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "calclateEMIOnTenureChangePension::Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", ProcessInstanceId);
            List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
            if (MobileDataList.size() > 0) {
                mobile_no = MobileDataList.get(0).get(0);
                Log.consoleLog(ifr, "MobileNo==>" + mobile_no);
            }
            Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
            String strquery = ConfProperty.getQueryScript("getIndchkDataQuery").replaceAll("#loanSelected#", loan_selected).replaceAll("#mobileNo#", mobile_no);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "calclateEMIOnTenureChangePension::Check Pension temp Data  Query:");
            if (!list1.isEmpty()) {
                custAge = list1.get(0).get(1);
                Log.consoleLog(ifr, "custAge value  : " + custAge);
            }
            String schemeID = pcm.getSchemeID(ifr, ProcessInstanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String tenureData_Query = ConfProperty.getQueryScript("PenTenMaxAmtQuery").replaceAll("#SCHEMEID#", schemeID).replaceAll("#AGE#", custAge);
            List<List<String>> MaxLoanTenureList = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
            if (MaxLoanTenureList.size() > 0) {
                MaxLoanTenure = MaxLoanTenureList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Max LoanTenure : " + MaxLoanTenure);
            recommendeLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            Log.consoleLog(ifr, "calclateEMIOnTenureChangePension::FINALELIGIBILITY recommendeLoanAmount==>" + recommendeLoanAmount);
            if (!recommendeLoanAmount.isEmpty()) {
                if (!RecommendloanTenure.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "MaxLoanTenure : " + MaxLoanTenure);
                    if (RecommendloanTenureCheck <= Double.parseDouble(MaxLoanTenure)) {
                        Log.consoleLog(ifr, "calclateEMIOnTenureChangePension::RecommendloanTenure is less than MaxLoanTenure: " + RecommendloanTenure);
                        recommendeLoanAmount = "-" + recommendeLoanAmount;
                        BigDecimal loanroii = new BigDecimal(loanROI);
                        BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanAmount, loanroii, Integer.parseInt(RecommendloanTenure));
                        Log.consoleLog(ifr, "emi : " + emi);
                        String grossAmountStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageGrossIncome").toString();
                        BigDecimal grossAmount = new BigDecimal(grossAmountStr);
                        String loanTenure = "";
                        String reqAmount = "";
                        String deductionsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_AverageDeductions").toString();
                        Log.consoleLog(ifr, "deductionsStr : " + deductionsStr);
                        BigDecimal AverageDeductions = new BigDecimal(deductionsStr);
                        String eligibleLoanAmt = ifr.getValue("QNL_LA_FINALELIGIBILITY_Eligibileloanamount").toString();
                        String ObligationsStr = ifr.getValue("QNL_LA_FINALELIGIBILITY_Obligations").toString();
                        Log.consoleLog(ifr, "ObligationsStr : " + ObligationsStr);
                        BigDecimal Obligations = new BigDecimal(ObligationsStr);
                        String configAmt = "";
                        BigDecimal NthAsperRecommendLoan = grossAmount.subtract(AverageDeductions).subtract(Obligations).subtract(emi);
                        Log.consoleLog(ifr, "NthAsperRecommendLoan : " + NthAsperRecommendLoan.toString());
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", NthAsperRecommendLoan.toString());
                        configAmt = pcm.mCheckBigDecimalValue(ifr, pcm.getParamConfig(ifr, "PL", "STP-CP", "LOANELIGIBILITY", "NETHOMEPERENT")).toString();
                        Log.consoleLog(ifr, "CmpNTHsalary : " + configAmt);
                        String recommamt = NthAsperRecommendLoan.toString();
                        Log.consoleLog(ifr, "recommamt : " + NthAsperRecommendLoan.toString());
                        if (NthAsperRecommendLoan.compareTo(new BigDecimal(eligibleLoanAmt)) < 0) {
                            BigDecimal HundredPerc = new BigDecimal("100");
                            BigDecimal NthPERCAsperRecommendLoan = (NthAsperRecommendLoan.divide(grossAmount, MathContext.DECIMAL128)).multiply(HundredPerc).setScale(2, RoundingMode.DOWN);
                            String recommloanAmt = NthPERCAsperRecommendLoan.toString();
                            Log.consoleLog(ifr, "recommloanAmt : " + NthPERCAsperRecommendLoan.toString());
                            if (NthPERCAsperRecommendLoan.compareTo(new BigDecimal(configAmt)) < 0) {
                                ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "");
                                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", "error", "NTH Percentage should not be lesser than 40%"));
                                message.put("saveWorkitem", "true");
                                return message.toString();
                            }
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_NetTakeHomeasperuser", NthPERCAsperRecommendLoan.toString());
                        } else {
                            ifr.setValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "");
                            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount", "error", "Recommended Loan Amount should not be greater than Eliglible Loan Amount"));
                            message.put("saveWorkitem", "true");
                            return message.toString();
                        }

                        String EMI = emi.toString();
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", EMI);
                    } else {
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                        message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Recommended Tenure cannot be greater than Maximum Tenure as per product."));
                        return message.toString();
                    }
                } else {
                    message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Please Enter the Valid Tenure"));
                    return message.toString();
                }
            } else {
                ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Please Enter the Valid recommended LoanAmount"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "calclateEMIOnTenureChangePension::caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    //added by logaraj for co-borrower occ load on 20/07/2024
    public void hideOccupationInfoFields(IFormReference ifr) {
        Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside hideOccupationInfoFields");
        String occupationType = String.valueOf(ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType"));
        Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: occupationType: " + occupationType);
        String commonFields = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_RecoveryMechanism";
        String salariedFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfMonthsInCurOrg,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome";
        String retiredFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly";
        String nonIncomeEarnerFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType";
        String professionalFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome";
        // String nonProfessionalFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual";
        String extraFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentContractBasis,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentTransferrable,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MobilityOfIndividual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ProofOfIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_StabilityOfIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile";
        String[] FieldVisibleFalse = new String[]{
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID",
            // "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentEmployment",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameAddrPensionDept",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LandholdingAcrs",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NatureOfLand",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PlaceOfAgriculturalLand",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentCroppingPattern",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ConstitutionType",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EstablishmentDate",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustryType",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustrySubType",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LastApprovalDT",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_AnnualReviewDT",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationNo",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationDT",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExpiryDT",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoOfEmployees",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CountryOfIncorporation",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactEmailID",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonEmailID",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_BusinessActivity",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofCrop",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ScaleofFinance",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EligibleLoanAmount",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofActivity",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NumberofUnits",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TotalEligibleAmount",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome",
            "table565_table388_relationshiPBborrower",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension",
            //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType",
            "table565_table388_RelationshipCanara",
            "table565_table388_OccupationType",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose", // Purpose Not mentioned in May22 FSD
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing", // Modified by Aravindh By refering May22 FSD
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", // Modified by Aravindh By refering May22 FSD
            //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", // Modified by Aravindh By refering May22 FSD
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", // Modified by Aravindh By refering May22 FSD
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile", // Modified by Aravindh By refering May22 FSD
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName",
            "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExperienceWithCurrentEmployer"
        };
        for (int i = 0; i < FieldVisibleFalse.length; i++) {
            ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
        }

        if (occupationType.equalsIgnoreCase("NIE")) {
            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside Case NIE");
            pcm.controlinvisiblity(ifr, salariedFields);
            pcm.controlinvisiblity(ifr, retiredFields);
            pcm.controlinvisiblity(ifr, professionalFields);
            // pcm.controlinvisiblity(ifr, nonProfessionalFields);
            pcm.controlinvisiblity(ifr, extraFields);
            pcm.controlvisiblity(ifr, commonFields);
            pcm.controlvisiblity(ifr, nonIncomeEarnerFields);
            pcm.controlDisable(ifr, commonFields);
            pcm.controlDisable(ifr, nonIncomeEarnerFields);

        } else if (occupationType.equalsIgnoreCase("Salaried")) {

            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside Case Salaried");
            pcm.controlinvisiblity(ifr, nonIncomeEarnerFields);
            pcm.controlinvisiblity(ifr, retiredFields);
            pcm.controlinvisiblity(ifr, professionalFields);
            pcm.controlinvisiblity(ifr, extraFields);
            pcm.controlvisiblity(ifr, commonFields);
            pcm.controlvisiblity(ifr, salariedFields);
            pcm.controlDisable(ifr, commonFields);
            pcm.controlDisable(ifr, salariedFields);

        } else if (occupationType.equalsIgnoreCase("PEN")) {

            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside Case PEN");
            pcm.controlinvisiblity(ifr, nonIncomeEarnerFields);
            pcm.controlinvisiblity(ifr, salariedFields);
            pcm.controlinvisiblity(ifr, professionalFields);
            pcm.controlinvisiblity(ifr, extraFields);
            pcm.controlvisiblity(ifr, commonFields);
            pcm.controlvisiblity(ifr, retiredFields);
            pcm.controlDisable(ifr, commonFields);
            pcm.controlDisable(ifr, retiredFields);

        } else if (occupationType.equalsIgnoreCase("PROF") || (occupationType.equalsIgnoreCase("SELF"))) {
            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside Case PROF");
            pcm.controlinvisiblity(ifr, nonIncomeEarnerFields);
            pcm.controlinvisiblity(ifr, retiredFields);
            pcm.controlinvisiblity(ifr, salariedFields);
            // pcm.controlinvisiblity(ifr, nonProfessionalFields);
            pcm.controlvisiblity(ifr, commonFields);
            pcm.controlvisiblity(ifr, professionalFields);
            pcm.controlinvisiblity(ifr, extraFields);
            pcm.controlDisable(ifr, commonFields);
            pcm.controlDisable(ifr, professionalFields);

//        } else if (occupationType.equalsIgnoreCase("SELF")) {
//            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside Case SELF");
//
//            pcm.controlinvisiblity(ifr, nonIncomeEarnerFields);
//
//            pcm.controlinvisiblity(ifr, retiredFields);
//
//            pcm.controlinvisiblity(ifr, professionalFields);
//
//            pcm.controlinvisiblity(ifr, salariedFields);
//
//            pcm.controlvisiblity(ifr, commonFields);
//
//           // pcm.controlvisiblity(ifr, nonProfessionalFields);
//
        } else {

            Log.consoleLog(ifr, "PensionBkoffCustomCode:hideOccupationInfoFields: Inside else case - no fields matched");

        }

    }

    public void populateLCDocumentsUploadCP(IFormReference ifr) {
        Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> inside the populateLCDocumentsUploadCP");
        try {
            String Coobligant = " ";
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String docQuery = ConfProperty.getQueryScript("getPIDfromPOBDGUPLOAD").replaceAll("#PID#", PID);
            List<List<String>> docResultData = ifr.getDataFromDB(docQuery);
            int rowCount = docResultData.size();
            Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> rowCount::" + rowCount);
            if (rowCount == 0) {
                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b  inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType WHERE c.PID = '" + PID + "' and (c.ApplicantType='B' or c.ApplicantType='CB')";
                Log.consoleLog(ifr, "Query1 data::" + Query1);
                List<List<String>> resultData = ifr.getDataFromDB(Query1);
                Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> resultData::" + resultData.toString());
                String applicantName[] = new String[resultData.size()];
                if (resultData.size() > 0) {
                    for (int i = 0; i < resultData.size(); i++) {
                        applicantName[i] = resultData.get(i).get(0);
                        Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> applicantName is " + applicantName);
                    }
                }
                for (int i = 0; i < applicantName.length; i++) {
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> Inside second for loop::");
                    String applicantNameArrayData = applicantName[i];
                    String applicantType = "";
                    if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                        applicantType = "C";
                    } else if (applicantNameArrayData.contains("Borrower")) {
                        applicantType = "B";
                    }
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> applicantType value1::" + applicantType);
                    String query = ConfProperty.getQueryScript("PORTALDOUMENTQUERY").replaceAll("#applicablefor#", applicantType);
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> finalquery value1::" + query);
                    List<List<String>> result = ifr.getDataFromDB(query);
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> result is.." + result);
                    int resultsize = result.size();
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> size is " + resultsize);
                    if (resultsize > 0) {
                        JSONArray arr = new JSONArray();
                        for (int j = 0; j < result.size(); j++) {
                            JSONObject re = new JSONObject();
                            if (applicantNameArrayData.contains("Co Borrower") || applicantNameArrayData.contains("Co-Borrower")) {
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_DocumentType", result.get(j).get(0) + Coobligant);
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_Mandatory", result.get(j).get(1));
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_ApplicantType", applicantName[i]);
                                arr.add(re);
                            } else {
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_DocumentType", result.get(j).get(0));
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_Mandatory", result.get(j).get(1));
                                re.put("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_ApplicantType", applicantName[i]);
                                arr.add(re);
                            }
                        }
                        Log.consoleLog(ifr, "PensionBkoffCustomCode:populateLCDocumentsUploadCP -> Document grid  json array::" + arr);
                        ifr.addDataToGrid("ALV_UPLOAD_DOCUMENT", arr);
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured inside the popluateDocumentsUploadCb" + e);
        }
    }

    // Added by kathir for pension lead capture knockoff on 01-08-2024
    public String KnockoffCheckLCPension(IFormReference ifr, String applicantType) {
        JSONObject message = new JSONObject();
        try {
            String processInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String knockoffDecision = plpc.mImpPensionOnClickDocumentUploadKnockoff(ifr, applicantType);
            if (knockoffDecision.toUpperCase().equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "knockoff Passed Successfully:::" + knockoffDecision);
                String cibilandexperiandecission = bbcc.CheckCibilandExperianBudgetBorrower(ifr, applicantType);
                Log.consoleLog(ifr, "PensionBkoffCustomCode:KnockoffCheckLCPension -> Cibil and Experian Decision: " + cibilandexperiandecission);

                if (cibilandexperiandecission.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:KnockoffCheckLCPension -> Cibil passed successfully " + cibilandexperiandecission);
                    String loanTenure = "";
                    String reqAmount = "";
                    String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY").replaceAll("#ProcessInsanceId#", processInsanceId);
                    List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
                    if (list4.size() > 0) {
                        reqAmount = list4.get(0).get(0);
                        loanTenure = list4.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);
                    String tenure = "";
                    String schemeId = "";
                    String maxamt_stp = "";
                    String custAge = "";
                    String f_key = bpcc.Fkey(ifr, applicantType);
                    String age = "select age from LOS_L_BASIC_INFO_I where f_key = '" + f_key + "'";
                    Log.consoleLog(ifr, "Queryover query BORROWER::" + age);
                    List<List<String>> CAge = cf.mExecuteQuery(ifr, age, "age_inps for applicant");
                    if (!CAge.isEmpty()) {
                        custAge = CAge.get(0).get(0);
                    }
                    List<List<String>> pension_stp = null;
                    String schemeID = ConfProperty.getQueryScript("SchemeVariantFetchQuery").replaceAll("#PID#", processInsanceId);
                    List<List<String>> schemeList = cf.mExecuteQuery(ifr, schemeID, "PenAgeschemeIdQuery:");
                    if (schemeList.size() > 0) {
                        schemeId = schemeList.get(0).get(0);
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
                    pension_stp = ifr.getDataFromDB(dataStp);
                    String roiData = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", processInsanceId);
                    Log.consoleLog(ifr, "pension roiData query : " + roiData);
                    List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
                    String roi = "";
                    if (!loanROI.isEmpty()) {
                        roi = loanROI.get(0).get(2);
                    }
                    String grossPension = "";
                    String netPension = "";
                    String DeductionMonthly = "0.00";
                    String PensionerData_Query = ConfProperty.getQueryScript("GETPENSIONERDATAQUERY").replaceAll("#WINAME#", processInsanceId);
                    List<List<String>> PensionerDataList = cf.mExecuteQuery(ifr, PensionerData_Query, "PensionerDataList:");
                    if (PensionerDataList.size() > 0) {
                        grossPension = PensionerDataList.get(0).get(0);
                        netPension = PensionerDataList.get(0).get(1);
                        DeductionMonthly = PensionerDataList.get(0).get(2);
                        Log.consoleLog(ifr, "deductions==>" + DeductionMonthly);
                        DeductionMonthly = DeductionMonthly.isEmpty() ? "0.00" : DeductionMonthly;
                        //Modified by Aravindh on 03/08/2024 for null handling 
                        Log.consoleLog(ifr, "grossPension==>" + grossPension);
                        Log.consoleLog(ifr, "netPension==>" + netPension);
                    }
                    double maxstpAmt = Double.parseDouble(maxamt_stp);
                    String BureauDataResponseBorrower = objcm.getMaxTotalEMIAmountCICDatas(ifr, "B");
                    String[] bSplitter = BureauDataResponseBorrower.split("-");
                    String bureauTypeB = bSplitter[0];
                    String ApplicantTypeB = bSplitter[1];
                    String cibiloblig = bSplitter[2];
                    double prodspeccapping = maxstpAmt;
                    HashMap hm = new HashMap();
                    hm.put("cibiloblig", cibiloblig);
                    hm.put("tenure", tenure);
                    hm.put("roi", roi);
                    hm.put("loancap", maxamt_stp);
                    hm.put("reqAmount", reqAmount);
                    hm.put("deductionmonth", DeductionMonthly);
                    hm.put("grosssalary", grossPension);

                    String finaleligibility = null;
                    try {
                        finaleligibility = plpc.getAmountForFinalEligibilityCPDataSaveBO(ifr, hm);
                        Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);
                        //added by keerthana to throw error msg for invalid eligible amt on 17/07/2024
                        if (finaleligibility.contains(RLOS_Constants.ERROR)) {
                            return pcm.returnError(ifr);
                        } else if (finaleligibility.contains("showMessage")) {
                            return finaleligibility;
                        }
                    } catch (Exception e) {
                        Log.errorLog(ifr, "Error occured in CoObligantCBSCheck" + e);
                        Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck" + e);
                        return pcm.returnError(ifr);

                    }
                } else {
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:KnockoffCheckLCPension -> Cibil Failed");
                    message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                    message.put("eflag", "false");
                    return message.toString();
                }
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("saveWorkitem", "true");
                return returnJSON.toString();
                //CheckCibilandExperianBudgetBorrower(ifr, applicantType);
            } else {
                Log.consoleLog(ifr, "Kindly fill the relationship with borrower validation for Co-Obligant ");
                message.put("MSGSTS", "N");
                message.put("SHOWMSG", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank.");
                return message.toString();
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in CoObligantCBSCheck" + e);
            Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck" + e);
            return pcm.returnError(ifr);

        }
    }

    public String FinalEligibilityPensionBO(IFormReference ifr) {
        JSONObject message = new JSONObject();
        String finaleligibility = "";
        try {
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInsanceId : " + ProcessInsanceId);
            String InsertionOrderId = ConfProperty.getQueryScript("BASICINFOBorrower").replaceAll("#PID#", ProcessInsanceId).replaceAll("#Applicanttype#", "B");
            List<List<String>> insertId = cf.mExecuteQuery(ifr, InsertionOrderId, "insertion Query");
            String CUSTOMERTYPE = "";
            if (insertId.size() > 0) {
                CUSTOMERTYPE = insertId.get(0).get(0);
                Log.consoleLog(ifr, "CUSTOMERTYPE : " + CUSTOMERTYPE);
            }
            String loanTenure = "";
            String reqAmount = "";
            String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
            if (list4.size() > 0) {
                reqAmount = list4.get(0).get(0);
                loanTenure = list4.get(0).get(1);
            }
            Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);

            String TotalEmiQuery = ConfProperty.getQueryScript("GetTotalEmiFromLiability").replaceAll("#PID#", ProcessInsanceId).replaceAll("#applicanttype#", "B");
            List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "FinancialInfoLiabilityEMI");

            String cibiloblig = "";
            if (TotalEmi.size() > 0) {
                cibiloblig = TotalEmi.get(0).get(0);
                Log.consoleLog(ifr, "cibiloblig FinancialInfoLiabilityEMI::" + cibiloblig);
            } else {
                cibiloblig = "0.00";
            }
            Log.consoleLog(ifr, "cibiloblig FinancialInfoLiabilityEMI::" + cibiloblig);

            String flag = "";
            String custAge = "";
            String mobile_no = "";
            String grossPension = "";
            String netPension = "";
            String DeductionMonthly = "0.00";
            String PensionerData_Query = ConfProperty.getQueryScript("GETPENSIONERDATAQUERY").replaceAll("#WINAME#", ProcessInsanceId);
            List<List<String>> PensionerDataList = cf.mExecuteQuery(ifr, PensionerData_Query, "PensionerDataList:");
            if (PensionerDataList.size() > 0) {
                grossPension = PensionerDataList.get(0).get(0);
                netPension = PensionerDataList.get(0).get(1);
                DeductionMonthly = PensionerDataList.get(0).get(2);
                Log.consoleLog(ifr, "deductions==>" + DeductionMonthly);
                DeductionMonthly = DeductionMonthly.isEmpty() ? "0.00" : DeductionMonthly;
                //Modified by Aravindh on 03/08/2024 for null handling 
                Log.consoleLog(ifr, "grossPension==>" + grossPension);
                Log.consoleLog(ifr, "netPension==>" + netPension);
            }
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
            String maxamt_stp = "";
            List<List<String>> pension_stp = null;
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
            pension_stp = ifr.getDataFromDB(dataStp);
            String roiData = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", ProcessInsanceId);
            Log.consoleLog(ifr, "pension roiData query : " + roiData);
            List<List<String>> loanROI = cf.mExecuteQuery(ifr, roiData, "Execute ROI Query->");
            String roi = "";
            if (!loanROI.isEmpty()) {
                roi = loanROI.get(0).get(2);
            }
            double maxstpAmt = Double.parseDouble(maxamt_stp);
            BigDecimal emiperlc = pcm.calculatePMT(ifr, new BigDecimal(roi), Integer.parseInt(tenure));
            String TotalDeductionsQ = ConfProperty.getQueryScript("FinancialInfoDeductionPension").replaceAll("#PID#", ProcessInsanceId).replaceAll("#CUSTOMERTYPE#", CUSTOMERTYPE);
            List<List<String>> TotalDeductions = cf.mExecuteQuery(ifr, TotalDeductionsQ, "FinancialInfoDeductionPension");
            String deductionB = "";
            double deductions = 0.00;
            if (!TotalDeductions.isEmpty()) {
                for (int i = 0; i < TotalDeductions.size(); i++) {
                    Log.consoleLog(ifr, "TotalDeductions FinancialInfoDeduction::" + TotalDeductions.size());
                    deductionB = TotalDeductions.get(i).get(0).isEmpty() ? "0.00" : TotalDeductions.get(i).get(0);
                    Log.consoleLog(ifr, "after deductionB ::" + deductionB);
                    deductions = deductions + Double.parseDouble(deductionB);
                }

            }
            Log.consoleLog(ifr, "deductions before adding dedction from form ::" + deductions);
            double deduction = Double.parseDouble(DeductionMonthly) + deductions;
            Log.consoleLog(ifr, "deductions after adding dedction from form ::" + deduction);
            //double prodspeccapping = maxstpAmt;
            HashMap hm = new HashMap();
            hm.put("cibiloblig", cibiloblig);
            hm.put("tenure", tenure);
            hm.put("roi", roi);
            hm.put("loancap", maxamt_stp);
            hm.put("reqAmount", reqAmount);
            hm.put("deductionmonth", String.valueOf(deduction));
            hm.put("grosssalary", grossPension);
            try {
                finaleligibility = plpc.getAmountForFinalEligibilityCPDataSaveBO(ifr, hm);
                Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);

            } catch (Exception e) {
                Log.errorLog(ifr, "Error occured in FinalEligibilityPensionBO inside catch" + e);
                Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck inside catch" + e);
                return pcm.returnError(ifr);

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in FinalEligibilityPensionBO" + e);
            Log.consoleLog(ifr, "Error occured in FinalEligibilityPensionBO" + e);
            return pcm.returnError(ifr);

        }
        return finaleligibility;
    }

    // Added by kathir for pension lead capture occupation info population on 07-08-2024
    public void pensionOccPopulateLC(IFormReference ifr) {
        Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> Inside pensionOccPopulateLC");
        String PartyType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
        Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> PartyType: " + PartyType);
        if (PartyType.equalsIgnoreCase("B")) {
            Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> Inside Lead Capture Borrower");

            String borrowerFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly";
            pcm.controlDisable(ifr, borrowerFields);
            pcm.controlvisiblity(ifr, borrowerFields);
            String[] otherFields = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExperienceWithCurrentEmployer", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_StabilityOfIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ProofOfIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MobilityOfIndividual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentTransferrable", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentContractBasis", "table565_table388_RelationshipCanara", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfMonthsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile_label", "table565_table388_relationshiPBborrower", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentEmployment", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameAddrPensionDept", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LandholdingAcrs", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NatureOfLand", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PlaceOfAgriculturalLand", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentCroppingPattern", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ConstitutionType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EstablishmentDate", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustryType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustrySubType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LastApprovalDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_AnnualReviewDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationNo", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExpiryDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoOfEmployees", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CountryOfIncorporation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactEmailID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonEmailID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_BusinessActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofCrop", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ScaleofFinance", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EligibleLoanAmount", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NumberofUnits", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TotalEligibleAmount", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing"};
            for (String field : otherFields) {
                ifr.setStyle(field, "visible", "false");
            }

            String fkey = bpcc.Fkey(ifr, PartyType);
            String customerId = ifr.getValue("CTRID_PD_SRCHVAL").toString();
            String mobileNumber = ifr.getValue("Mobile_Number_PD").toString();
            Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> fkey: " + fkey);
            Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> customerId: " + customerId);
            Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> mobileNumber: " + mobileNumber);

            String getPensionDetailsQuery = ConfProperty.getQueryScript("GETPENSIONDETAILSLC").replaceAll("#CUSTOMERID#", customerId).replaceAll("#MOBILENUMBER#", mobileNumber);
            List<List<String>> pensionData = cf.mExecuteQuery(ifr, getPensionDetailsQuery, "PensionBkoffCustomCode:pensionOccPopulateLC -> getPensionDetailsQuery: ");
            Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> pensionData: " + pensionData);

            if (pensionData.size() > 0) {
                String grossPension = pensionData.get(0).get(0);
                String netPension = pensionData.get(0).get(1);
                String category = pensionData.get(0).get(2);
                String dateOfRetirement = pensionData.get(0).get(3);
                String deductions = "";

                String occupationType = ConfProperty.getCommonPropertyValue("OCCUPATIONTYPE");
                String occupationSubType = ConfProperty.getCommonPropertyValue("OCCUPATIONTYPESUBTYPE");
                Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> occupationType: " + occupationType);
                Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> occupationSubType: " + occupationSubType);

                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension", grossPension);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension", netPension);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category", category);

                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSubType);

                SimpleDateFormat inputFormat = new SimpleDateFormat("DD-MM-YYYY");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    Date date = inputFormat.parse(dateOfRetirement);
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> inputDate: " + date);
                    String formattedDate = outputFormat.format(date);
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> formattedDate: " + formattedDate);

                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate", formattedDate);
                } catch (ParseException ex) {
                    Log.errorLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> Exception: " + ex);
                }

                if (!grossPension.equalsIgnoreCase("") && !netPension.equalsIgnoreCase("")) {
                    deductions = String.valueOf(Integer.parseInt(netPension) - Integer.parseInt(grossPension));
                    Log.consoleLog(ifr, "PensionBkoffCustomCode:pensionOccPopulateLC -> deductions: " + deductions);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deductions);
                }

            }
        }
    }
}
