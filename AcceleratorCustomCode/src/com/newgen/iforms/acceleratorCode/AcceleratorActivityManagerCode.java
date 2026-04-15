/*      
 '------------------------------------------------------------------------------
 '      NEWGEN SOFTWARE TECHNOLOGIES LIMITED
 '   Group                  : CIG
 '   Product/Project/Utility: RLOS_ACCELERATOR
 '   Module                 : RLOS
 '   File Name              : LOS
 '   Author                 : RLOS CORE TEAM
 '   Date written           : 08/01/2023
 '   Description            : AcceleratorActivityManagerCode Class FOR METHOD
 '   ----------------------------------------------------------------------------
 '   CHANGE HISTORY
 '   ----------------------------------------------------------------------------
 '   Date:        Bug ID:   Change By :     Change Description
 '   ----------------------------------------------------------------------------
 */
package com.newgen.iforms.acceleratorCode;

import com.newgen.iforms.AccAPIS.CIBILAPI;
import com.newgen.iforms.AccAPIS.EquiFaxAPI;
import com.newgen.iforms.AccAPIS.ExperianAPI;
import com.newgen.iforms.AccAPIS.HighMarkAPI;
import com.newgen.iforms.commonXMLAPI.UploadCreateWI;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commonXMLAPI.ChildWorkItem;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.main.RLOSHead;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCustomCode;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.staffHL.StaffHLCommanCustomeCode;
import com.newgen.iforms.staffHL.StaffHLPortalCustomCode;
import com.newgen.iforms.staffVL.StaffVLPortalCustomCode;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import com.newgen.omni.wf.util.xml.api.dms.WFXmlResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import com.newgen.iforms.pension.PensionLoanPortalCustomCode;
import com.newgen.iforms.pension.PensionBkoffCustomCode;
import com.newgen.iforms.vl.VLBkoffcCustomCode;
import com.newgen.iforms.vl.VLPortalCustomCode;
import com.newgen.iforms.homeloan.HLPortalCustomCode;
import com.newgen.iforms.hrms.HRMSPortalCustomCode;
import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import com.newgen.dlp.integration.cbs.Ammortization;
import static com.newgen.dlp.integration.cbs.Demographic.calculateAge;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.budget.BudgetBkoffCustomCode;
import com.newgen.iforms.budget.BudgetDisbursementScreen;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import java.text.DecimalFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author m_gupta
 */
public class AcceleratorActivityManagerCode extends PortalCustomCode {

    CommonMethods cm = new CommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    HLPortalCustomCode hlpcc = new HLPortalCustomCode();
    StaffHLPortalCustomCode shlpcc = new  StaffHLPortalCustomCode();
    PortalCommonMethods pcm = new PortalCommonMethods();
    BudgetPortalCustomCode Bpcm = new BudgetPortalCustomCode();
    //  CustomerAccountSummary cas = new CustomerAccountSummary();
    PensionLoanPortalCustomCode plpc = new PensionLoanPortalCustomCode();
    VLPortalCustomCode Vlpc = new VLPortalCustomCode();
    VLBkoffcCustomCode vlbcc = new VLBkoffcCustomCode();
    LoanEligibilityCheck lec = new LoanEligibilityCheck();
    BudgetBkoffCustomCode bbcc = new BudgetBkoffCustomCode();
    Advanced360EnquiryDatav2 Adv360V2 = new Advanced360EnquiryDatav2();
    PensionBkoffCustomCode pbcc = new PensionBkoffCustomCode();
    StaffVLPortalCustomCode staffVLPortalCustomCode =new StaffVLPortalCustomCode();
//    PensionDisbursementScreen pdbs = new PensionDisbursementScreen();
    // BRMSRules brmsRule = new BRMSRules();
    JSONParser parser = new JSONParser();
    String nmi = "0";
    BudgetDisbursementScreen bds = new BudgetDisbursementScreen();

    public void mAccPostHookPickListOk(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            cf.setPostHookPickListOkData(ifr, Control, Event, JSdata);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Inside mAccPostHookPickListOk Exception:" + e);
            Log.errorLog(ifr, "Inside mAccPostHookPickListOk Exception:" + e);
        }
    }

    public void mAccClearPicklistPostHook(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            cf.clearPicklistPostHookData(ifr, Control, Event, JSdata);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Inside mAccClearPicklistPostHook Exception:" + e);
            Log.errorLog(ifr, "Inside mAccClearPicklistPostHook Exception:" + e);
        }
    }

    //Function Name         : mAccLoadDI_SI_SourceInfo
    //Stage                 : Initiation Stage Only
    //Description           : This function has used for setting branch User Details like BranchCode,EmployeeId,EmployeeName,BranchName
    public void mAccLoadDI_SI_SourceInfo(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccLoadDI_SI_SourceInfo");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            ifr.setValue("PID", PID);
            String query = ConfProperty.getQueryScript("querySourceInfo").replaceAll("#username#", ifr.getUserName());
            cf.mGetLabelAndSetToField(ifr, query, "", "QL_SOURCINGINFO_EmployeeName,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_EmployeeId,QL_LEAD_DET_EmployeeName,QL_SOURCINGINFO_Branch");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mAccLoadDI_SI_SourceInfo:" + e);
            Log.errorLog(ifr, "Exception mAccLoadDI_SI_SourceInfo:" + e);
        }
    }
    
    
//    public String generateCIC(IFormReference ifr, String control, String event, String value) {
//    	StaffHLCommanCustomeCode staffHLCommanCustomeCode= new StaffHLCommanCustomeCode();
//        Log.consoleLog(ifr, "Inside AcceleratorActivityManagerCode->generateCIC:");
//        return staffHLCommanCustomeCode.fetchCICResponse(ifr, control);
//    }


    //============================================Party Code Code Start===================================//
    //Function Name         : mAccChangeApplicantTypeCheck
    //Stage                 : Initiation Stage Only
    //Description           : This function has used for Checking Duplicate Applicant Type
    public String mAccChangeApplicantTypeCheck(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccChangeApplicantTypeCheck");
            int count = ifr.getDataFromGrid("ALV_BASIC_INFO").size();
            Boolean flag = true;
            for (int i = 0; i < count; i++) {
                String checkdata = ifr.getTableCellValue("ALV_BASIC_INFO", i, "QNL_BASIC_INFO_ApplicantType").toString();
                if (checkdata.equalsIgnoreCase("B")) {
                    flag = false;
                }
            }
            String currAppType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            if (flag.equals(false) && currAppType.equalsIgnoreCase("B")) {
                ifr.setValue("QNL_BASIC_INFO_ApplicantType", "");
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_ApplicantType", "error", "Duplicate Borrower Request!"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mAccChangeApplicantTypeCheck:" + e);
            Log.errorLog(ifr, "Exception mAccChangeApplicantTypeCheck:" + e);
        }
        return "";
    }

    public void mAccChangeApplicantName(IFormReference ifr, String Control, String Event, String value) {//Checked
        String fName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
        String mName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
        String lName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
        String bName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName").toString();
        String eType = ifr.getValue("QNL_BASIC_INFO_EntityType").toString();
        String partyName = "";
        if (eType.equalsIgnoreCase("I")) {
            if (!fName.equalsIgnoreCase("")) {
                partyName = partyName + " " + fName;
            }
            if (!mName.equalsIgnoreCase("")) {
                partyName = partyName + " " + mName;
            }
            if (!lName.equalsIgnoreCase("")) {
                partyName = partyName + " " + lName;
            }
        } else {
            partyName = bName;
        }
        ifr.setValue("QNL_BASIC_INFO_FullName", partyName);
    }

    //Function Name         : mAccChangeApplicantTypeCheck
    //Stage                 : Initiation Stage Only
    //Description           : This function has used for Checking DOB and Setting Age
    // modified by vandana 03/07/24
    public String mAcconChangeDI_PD_DOB(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            String messageValue = "";
            // int diffyrs = cf.mCalculateDateDiffInYrs(ifr, ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString());
            String DOB = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
            Log.consoleLog(ifr, "QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB  : " + DOB);
            SimpleDateFormat dateofcustopenFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat TargetDate = new SimpleDateFormat("yyyy-MM-dd");
            Date AgeOfCustRelFormat = dateofcustopenFormat.parse(DOB);
            String DateofBirth = TargetDate.format(AgeOfCustRelFormat);
            Log.consoleLog(ifr, "DateofBirth  : " + DateofBirth);
            LocalDate dob = LocalDate.parse(DateofBirth);
            Log.consoleLog(ifr, "dob  : " + dob);
            int Age = cm.calculateAge(dob);

            //i age = Years;
            Log.consoleLog(ifr, "age  : " + Age);

            if (Age < 18) {
                messageValue = "Applicant's age should be 18 years or older";
            } else {
                ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age", String.valueOf(Age));
                // String custType = ifr.getValue("QNL_BASIC_INFO_ExistingCustomer").toString();
                // String borrower_age_kf = String.valueOf(Age);
//            int diffyrs = cf.mCalculateDateDiffInYrs(ifr, ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString());
//            if (diffyrs < 18) {
//                messageValue = "Applicant's age should be 18 years or older";
//            } else {
//                ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age", String.valueOf(diffyrs));
//                String custType = ifr.getValue("QNL_BASIC_INFO_ExistingCustomer").toString();
//                String borrower_age_kf = String.valueOf(diffyrs);
//                String loantype = "All";
//                JSONObject result = cf.executeBRMSRule(ifr, "Knock_Of_Rule", borrower_age_kf + "," + custType + "," + loantype);
//                if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                    if (cf.getJsonValue(result, "eligibility_result_op").equalsIgnoreCase("Not Eligible")) {
//                        messageValue = "Applicant's age should be 22 years or older";
//                    }
//                } else {
//                    messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
//                }
//            }
//            if (!(messageValue.equalsIgnoreCase(""))) {
//                JSONObject message = new JSONObject();
//                List ClearList = new ArrayList();
//                ClearList.add("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB");
//                ClearList.add("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age");
//                message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB", "error", messageValue));
//                message.put("clearValue", cf.clearValue(ifr, ClearList));
//                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception Inside mAcconChangeDI_PD_DOB" + e);
            Log.errorLog(ifr, "Exception Inside mAcconChangeDI_PD_DOB" + e);
        }
        return "";
    }

    public String mAccChangeAddressParty(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        int rowSize = ifr.getDataFromGrid("LV_ADDRESS").size();
        if (rowSize > 0) {
            String AddressType = ifr.getValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType").toString();
            for (int i = 0; i < rowSize; i++) {
                String Address = ifr.getTableCellValue("LV_ADDRESS", i, 0);
                if (Address.equalsIgnoreCase(AddressType)) {
                    JSONObject message = new JSONObject();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "");
                    message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType",
                            "error", "Can't added duplicate value!!"));
                    return message.toString();
                }
            }
        }
        return "";
    }

    //Function Name         : mAccChangeApplicantTypeCheck
    //Stage                 : Initiation Stage Only
    //Description           : This function has used for set KYC Validation Done
    public String mAccClickBTN_KYC_Validate(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            JSONObject message = new JSONObject();
            if (ifr.getValue("QNL_BASIC_INFO_CNL_KYC2_KYC_ID").toString().equalsIgnoreCase("")) {
                message.put("showMessage", cf.showMessage(ifr, "kyc_id", "error", "Please Choose KYC ID"));
                return message.toString();
            } else if (ifr.getValue("QNL_BASIC_INFO_CNL_KYC2_KYC_No").toString().equalsIgnoreCase("")) {
                message.put("showMessage", cf.showMessage(ifr, "kyc_No", "error", "Please Fill KYC No"));
                return message.toString();
            }
            ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidationStatus", "Yes");
            ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedOn", cf.getCurrentDate(ifr));
            ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedBy", ifr.getUserName());
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception Inside mAccClickBTN_KYC_Validate:" + e);
            Log.errorLog(ifr, "Exception Inside mAccClickBTN_KYC_Validate" + e);
        }
        return "";
    }
    public void mImplListViewLoadCoBorrowerHL(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplListViewLoadCoBorrowerHL");
        shlpcc.autoPopulateCoBorowerDetailsByFkeySHL(ifr, "");
    }
    
    public void mImplListViewLoadCoBorrowerBKFHL(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplListViewLoadCoBorrowerHL");
        shlpcc.autoPopulateCoBorowerDetailsListViewBKFSHL(ifr);
    }
    
    public void mImplListViewOccupationInfo(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplListViewOccupationInfo");
        shlpcc.populateFieldsOnListViewOccupation(ifr);
    }

    public String mAccChangeKYCID(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAccChangeKYCID:");
        int rowSize = ifr.getDataFromGrid("LV_KYC").size();
        Log.consoleLog(ifr, "Inside mAccChangeKYCID rowSize:" + rowSize);
        if (rowSize > 0) {
            String KYCType = ifr.getValue("QNL_BASIC_INFO_CNL_KYC2_KYC_ID").toString();
            for (int i = 0; i < rowSize; i++) {
                String kyc = ifr.getTableCellValue("LV_KYC", i, 0);
                if (kyc.equalsIgnoreCase(KYCType)) {
                    JSONObject message = new JSONObject();
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "");
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_KYC_No", "");
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedOn", "");
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedBy", "");
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidationStatus", "");
                    message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "error",
                            "Can't added duplicate value!!"));
                    return message.toString();
                }
            }
        }
        return "";
    }

//    public void mAccChangeRetirementDate(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
//        try {
//            int diffyrs = cf.mCalculateDateDiffInYrs(ifr, ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate").toString());
//            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod", String.valueOf(diffyrs));
//            int diffyrsage = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age").toString().equalsIgnoreCase("") ? 0 : Integer.parseInt(ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age").toString());
//            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", String.valueOf(diffyrs + diffyrsage));
//        } catch (NumberFormatException e) {
//            Log.consoleLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
//            Log.errorLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
//        }
//    }
    public void mAccCustomListViewValidationPartyDetails(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            String appType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            if (appType.equalsIgnoreCase("B")) {
                String fullname = ifr.getValue("QNL_BASIC_INFO_FullName").toString();
                Log.consoleLog(ifr, "fullname:" + fullname);
                ifr.setValue("CustomerName", fullname);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
            Log.errorLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
        }
    }

    /*public String mImplChangePurpose(IFormReference ifr, String Control, String Event, String value) {//Checked
     Log.consoleLog(ifr, "Inside mImplChangePurpose:");
     return Vlpc.validateGrossMonthlyIncomeVL(ifr, Control);
     }*/
    //============================================Party Info Code END===================================//
    //============================================Bureau Tab Code Start===================================//
    /*  public String mAccClickSetConsentStatus(IFormReference ifr, String Control, String Event, String value) {//Checked
     Log.consoleLog(ifr, "Inside mSetConsentStatus");
     try {
     String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
     String query = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
     List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
     String loan_selected = loanSelected.get(0).get(0);
     Log.consoleLog(ifr, "loan type==>" + loan_selected);

     JSONArray LV_BCGrid = ifr.getDataFromGrid("ALV_BUREAU_CONSENT");
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
     String consentFlag = bureauList.get(0).get(0);
     Log.consoleLog(ifr, "consentFlag:::" + consentFlag);
     String status = "";
     if (consentFlag.equalsIgnoreCase("Y")) {
     Log.consoleLog(ifr, "consentFlag::: inside Y");
     status = "Accepted";
     } else if (consentFlag.equalsIgnoreCase("N")) {
     Log.consoleLog(ifr, "consentFlag::: inside N");
     status = "Rejected";
     } else if (consentFlag.equalsIgnoreCase("I")) {
     Log.consoleLog(ifr, "consentFlag::: inside empty");
     status = "Initiated";
     }
     //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentReceived", status);
     ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 2, status);
     if ((ifr.getTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedOn").toString().equalsIgnoreCase("")
     && consentFlag.equalsIgnoreCase("Y")) || consentFlag.equalsIgnoreCase("N")) {
     //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedOn", cf.getCurrentDate(ifr));
     ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 3, cf.getCurrentDate(ifr));
     //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedBy", ifr.getUserName());
     ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 4, ifr.getUserName());
                        
     }
     if (loan_selected.equalsIgnoreCase("Canara Pension") && consentFlag.equalsIgnoreCase("Y")) {
     JSONObject message_err = new JSONObject();
     String knockoffDecision = plpc.mImpPensionOnClickDocumentUploadKnockoff(ifr);

     if (knockoffDecision.toUpperCase().equalsIgnoreCase("PROCEED")) {

     Log.consoleLog(ifr, "pension document knockoffDecision Passed Successfully:::");

     } else {
     Log.consoleLog(ifr, "pension document knockoffDecision fail" + knockoffDecision);
     message_err.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank."));

     message_err.put("eflag", "false");

     return message_err.toString();
     }

     }

                    
     }
     }
     cm.mCheckRecievedConsent(ifr, Control, Event, value);
     JSONObject returnJSON = new JSONObject();
     returnJSON.put("saveWorkitem", "true");
     return returnJSON.toString();
     } catch (Exception e) {
     Log.consoleLog(ifr, "Exception in mSetConsentStatus" + e.getMessage());
     }
     return "";
     }
     */
    //---Added by Priya on 06-03-2023
    public String mAccClickSetConsentStatus(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mSetConsentStatus");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            JSONArray LV_BCGrid = ifr.getDataFromGrid("ALV_BUREAU_CONSENT");
            Log.consoleLog(ifr, "LV_BCGrid==>" + LV_BCGrid);
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
                    String consentFlag = bureauList.get(0).get(0);
                    Log.consoleLog(ifr, "consentFlag:::" + consentFlag);
                    String status = "";
                    if (consentFlag.equalsIgnoreCase("Y")) {
                        Log.consoleLog(ifr, "consentFlag::: inside Y");
                        status = "Accepted";
                    } else if (consentFlag.equalsIgnoreCase("N")) {
                        Log.consoleLog(ifr, "consentFlag::: inside N");
                        status = "Rejected";
                    } else if (consentFlag.equalsIgnoreCase("I")) {
                        Log.consoleLog(ifr, "consentFlag::: inside empty");
                        status = "Initiated";
                    }
                    //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentReceived", status);
                    ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 2, status);
                    if ((ifr.getTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedOn").toString().equalsIgnoreCase("")
                            && consentFlag.equalsIgnoreCase("Y")) || consentFlag.equalsIgnoreCase("N")) {
                        //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedOn", cf.getCurrentDate(ifr));
                        ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 3, cf.getCurrentDate(ifr));
                        //ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, "QNL_BUREAU_CONSENT_ConsentVerifiedBy", ifr.getUserName());
                        ifr.setTableCellValue("ALV_BUREAU_CONSENT", i, 4, ifr.getUserName());
                    }
                    if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                        Log.consoleLog(ifr, "Inside mAccClickSetConsentStatus referesh check for Pension");
                        String message = plpc.pensionCoBoEligibility(ifr, Control, Event, value);
                        return message;
                    }
                    if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                        Log.consoleLog(ifr, "Inside mAccClickSetConsentStatus referesh check for Canara Budget");
                        String message = Bpcm.BudgetCoBoEligibility(ifr, Control, Event, value);
                        return message;
                    }

                    if (loan_selected.equalsIgnoreCase("VEHICLE LOAN") && consentFlag.equalsIgnoreCase("Y")) {
                        Log.consoleLog(ifr, "Inside cibilandEXDecision and knockoffDecision referesh check for CB VL");
                        JSONObject message_err = new JSONObject();

                        String knockoffDecision = "";
                        String knockoffRules = ConfProperty.getQueryScript("KNOCKOFFCOUNTVL").replaceAll("#ProcessInsanceId#", PID)
                                .replaceAll("#PARTY_TYPE#", "C");
                        List<List<String>> knockoffRulesdata = cf.mExecuteQuery(ifr, knockoffRules, "Execute query for fetching knockoffRules::");
                        Log.consoleLog(ifr, "knockoffRules value  : " + knockoffRulesdata);
                        if (!knockoffRulesdata.isEmpty()) {
                            knockoffDecision = knockoffRulesdata.get(0).get(2);
                        }
                        Log.consoleLog(ifr, "knockoffDecision::" + knockoffDecision);
                        if (knockoffDecision.equalsIgnoreCase("Reject")) {
                            Log.consoleLog(ifr, " mAccClickSetConsentStatus::VL CB knockoffDecision fail " + knockoffDecision);
                            message_err.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error",
                                    "Co-Borrower is not eligible for the selected digital loan journey."));
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
                        } else if (knockoffDecision.equalsIgnoreCase("APPROVE")) {

                            JSONArray gridResultSet = new JSONArray();
                            String eligibilityGrid = fetchDataFromGrid(ifr, "CB_Eligibility_Grid");
                            org.json.JSONArray eligibilityGridData = new org.json.JSONArray(eligibilityGrid);
                            Log.consoleLog(ifr,
                                    "Entered eligibilityGridData eligibilityGridData " + eligibilityGridData);

                            String coBorrowerName = ifr.getTableCellValue("ALV_BUREAU_CONSENT", 0, 0);
                            Log.consoleLog(ifr, "inside executiveSummary empBusiName" + coBorrowerName);

                            JSONObject formDetailsJson = new JSONObject();
                            formDetailsJson.put("Name", coBorrowerName);
                            formDetailsJson.put("Eligibility Status", "Eligible");

                            gridResultSet.add(formDetailsJson);
                            ifr.clearTable("CB_Eligibility_Grid");
                            ifr.addDataToGrid("CB_Eligibility_Grid", gridResultSet);
                            Log.consoleLog(ifr, "eligibilityGridData gridResultSet : " + gridResultSet.toString());
                            String cibilandEXDecision = vlbcc.CheckCibilandExperian(ifr, "CB");
                            Log.consoleLog(ifr, "mAccClickSetConsentStatus::cibilandEXDecision for CB  :" + cibilandEXDecision);

                            Log.consoleLog(ifr, "mAccClickSetConsentStatus::knockoffDecision for CB  :" + knockoffDecision);

                            if (cibilandEXDecision.equalsIgnoreCase("Approve")) {
                                Log.consoleLog(ifr, "mAccClickSetConsentStatus::Checking knockoffDecision for CB  :" + knockoffDecision);
                                String scoreCardDecisionCB = Vlpc.vlScoreCardDecisionForCB(ifr);
                                Log.consoleLog(ifr, "scoreCardDecisionCB gridResultSet : " + scoreCardDecisionCB);
                                if (scoreCardDecisionCB.equalsIgnoreCase("Low Risk- I") || scoreCardDecisionCB.equalsIgnoreCase("Low Risk- II") || scoreCardDecisionCB.equalsIgnoreCase("Normal Risk")
                                        || scoreCardDecisionCB.equalsIgnoreCase("Moderate Risk") || scoreCardDecisionCB.equalsIgnoreCase("")) {
                                    Log.consoleLog(ifr, "scoreCard Passed:::" + scoreCardDecisionCB);
                                } else {
                                    Log.consoleLog(ifr, "scoreCard Failed:::" + scoreCardDecisionCB);
                                    message_err.put("showMessage", cf.showMessage(ifr,
                                            "Btn_Refresh", "error", "Co-Borrower is not eligible for the selected digital loan journey.VL CB scoreCard Failed."));
                                    message_err.put("eflag", "false");
                                    return message_err.toString();
                                }
                            } else {
                                Log.consoleLog(ifr, "mAccClickSetConsentStatus:: VL CB cibilandEXDecision fail " + cibilandEXDecision);
                                message_err.put("showMessage", cf.showMessage(ifr, "Btn_Refresh", "error",
                                        "Co-Borrower is not eligible for the selected digital loan journey.VL CB cibilandEXDecision fail"));
                                message_err.put("eflag", "false");
                            }
                        }
                    } else if (loan_selected.equalsIgnoreCase("VEHICLE LOAN") && consentFlag.equalsIgnoreCase("N")) {
                        JSONObject message = new JSONObject();
                        message.put("showMessage",
                                cf.showMessage(ifr, "Btn_Refresh", "error", "Co-Borrower Rejected the Consent "));
                        Log.consoleLog(ifr, " mAccClickSetConsentStatus::VL CB rejected");
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

                        return message.toString();
                    } else if (loan_selected.equalsIgnoreCase("VEHICLE LOAN") && consentFlag.equalsIgnoreCase("I")) {
                        JSONObject message = new JSONObject();
                        Log.consoleLog(ifr, "mAccClickSetConsentStatus:: VL CB Consent Not received");
                        message.put("showMessage",
                                cf.showMessage(ifr, "Btn_Refresh", "error", "Co-Borrower consent Not Received"));
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

                        return message.toString();
                    }
                }
            }
            cm.mCheckRecievedConsent(ifr, Control, Event, value);
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("saveWorkitem", "true");
            return returnJSON.toString();

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mSetConsentStatus" + e.getMessage());
        }

        return "";
    }

    public String mAccClickmOTPGeneration(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickmOTPGeneration");
        try {
            String result = cm.mBureauOTP(ifr, "GenerateOTP");
            JSONObject message = new JSONObject();
            if (result.equalsIgnoreCase("SUCCESS")) {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "OTP Send Successfully on Mobile"));
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", result));
            }
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickmOTPGeneration::" + e);
            Log.errorLog(ifr, "Exception in mAccClickmOTPGeneration::" + e);
        }
        return "";
    }
    
    public void mImplChnageCoBorrowerYN(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplChnageCoBorrowerYN");
        StaffHLPortalCustomCode hlpcc =new StaffHLPortalCustomCode();
        hlpcc.setControlIdForCoBorrower(ifr,Control);
    }

    public String mAccClickmOTPValidation(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickmOTPValidation");
        try {
            String result = cm.mBureauOTP(ifr, "ValidateOTP");
            JSONObject message = new JSONObject();
            if (result.equalsIgnoreCase("SUCCESS")) {
                cm.mCheckRecievedConsent(ifr, Control, Event, value);
                cm.mSetBureauVerified(ifr);
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Validate OTP Successfully"));
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", result));
            }
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickmOTPValidation::" + e);
            Log.errorLog(ifr, "Exception in mAccClickmOTPValidation::" + e);
        }
        return "";
    }

    public String mAccClickPortalConsent(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickPortalConsent");
        try {
            String result = cm.mBureauOTP(ifr, "Portal");
            JSONObject message = new JSONObject();
            if (result.equalsIgnoreCase("SUCCESS")) {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Portal Consent Successfully"));
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", result));
            }
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickPortalConsent::" + e);
            Log.errorLog(ifr, "Exception in mAccClickPortalConsent::" + e);
        }
        return "";
    }

    public String mAccClickUploadBureau(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickUploadBureau");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String appType = (String) ifr.getValue("QNL_BUREAU_CONSENT_PartyType");
            Log.consoleLog(ifr, "appType:: " + appType);
            cm.mInsertBureauConsent(ifr, PID, appType);
            cm.mUpdateBureauConsent(ifr, PID, appType);
            String query = ConfProperty.getQueryScript("SelectBureauConsentDoc").replaceAll("#PID#", PID).replaceAll("#ApplicantType#", appType);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "To Check Entry for Bureau Consent");
            if (result.size() > 0) {
                query = ConfProperty.getQueryScript("UPDATEBureauConsentDoc").replaceAll("#PID#", PID).replaceAll("#ApplicantType#", appType).
                        replaceAll("#DocId#", ifr.getValue("QNL_BUREAU_CONSENT_docID").toString());
            } else {
                query = ConfProperty.getQueryScript("InsertBureauConsentDoc").
                        replaceAll("#PID#", PID).replaceAll("#ApplicantType#", appType).
                        replaceAll("#DocId#", ifr.getValue("QNL_BUREAU_CONSENT_docID").toString()).
                        replaceAll("#UploadedBy#", ifr.getUserName()).replaceAll("#UploadedDate#", cf.getCurrentDateTime(ifr));
            }
            Log.consoleLog(ifr, "insert query1 is " + query);
            int retval = ifr.saveDataInDB(query);
            Log.consoleLog(ifr, "retval1 " + retval);

            ifr.setValue("QNL_BUREAU_CONSENT_ConsentReceived", "Accepted");
            ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "true");
            cm.mSetBureauVerified(ifr);
            JSONObject message = new JSONObject();
            message.put("refreshFrame", cf.refreshFrame("F_InwardDocument"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Portal Consent Successfully"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickUploadBureau::" + e);
            Log.errorLog(ifr, "Exception in mAccClickUploadBureau::" + e);
        }
        return "";
    }

    public void mAccloadConsentBureauGrid(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_BUREAU_CONSENT_ConsentReceived").toString().equalsIgnoreCase("")) {
            ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "false");
        } else {
            ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "true");
        }
    }

    public void mAcconChangeSectionStateF_Bureau_Consent(IFormReference ifr, String Control, String Event, String value) {//Checked
        String firstQuery = ConfProperty.getQueryScript("BASICINFO").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        String SecondQuery = ConfProperty.getQueryScript("BUREAUCONSENTTABLE").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        cf.mCommonAddDeleteApplicantDetails(ifr, firstQuery, SecondQuery, "ALV_BUREAU_CONSENT", "QNL_BUREAU_CONSENT_PartyType", "AD");
    }

    public void mAcconChangeSectionStateF_Bureau_Details(IFormReference ifr, String Control, String Event, String value) {//Checked
        String firstQuery = ConfProperty.getQueryScript("BASICINFO").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        String SecondQuery = ConfProperty.getQueryScript("CBDETAILS").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        cf.mCommonAddDeleteApplicantDetails(ifr, firstQuery, SecondQuery, "ALV_CB_Details", "QNL_CB_Details_Applicant_Type", "A");
    }

    public void mAcconChangeSectionStateF_Bureau_NegativeDetails(IFormReference ifr, String Control, String Event, String value) {//Checked
        String firstQuery = ConfProperty.getQueryScript("BASICINFO").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        String SecondQuery = ConfProperty.getQueryScript("NEGATIVEDBDEFAULTER").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        cf.mCommonAddDeleteApplicantDetails(ifr, firstQuery, SecondQuery, "ALV_NEGATIVE_DB_DEFAULTER", "QNL_NEGATIVE_DB_DEFAULTER_PartyType", "D");
    }

    public String mAccLoadBureauAppType(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccLoadBureauAppType value:" + value);
        try {
            if (value.split("~")[0].equalsIgnoreCase(ConfProperty.getCommonPropertyValue("BureauCheckName"))) {
                String validateKycDetails = cm.mValidatePANAadharDetails(ifr);
                Log.consoleLog(ifr, "validateKycDetails::" + validateKycDetails);
                if (!validateKycDetails.isEmpty()) {
                    JSONObject returnJSON = new JSONObject();
                    returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Kindly add Aadhar for all Applicants"));
                    return returnJSON.toString();
                } else {
                    ifr.setStyle("F_Bureau_Consent", "sectionstate", "expanded");
                    ifr.setStyle("F_Bureau_Consent", "sectionstate", "collapsed");
                    ifr.setStyle("F_Bureau_Details", "sectionstate", "expanded");
                    ifr.setStyle("F_Bureau_Details", "sectionstate", "collapsed");
                    ifr.setStyle("F_Bureau_NegativeDetails", "sectionstate", "expanded");
                    ifr.setStyle("F_Bureau_NegativeDetails", "sectionstate", "collapsed");
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mLoadBureauAppTypeDD::" + e);
        }
        return "";
    }

    public void mAccChangeCBTypeConsent(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccChangeCBTypeConsent");
        try {
            cm.mCheckRecievedConsent(ifr, Control, Event, value);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccChangeCBTypeConsent" + e.getMessage());
            Log.errorLog(ifr, "Exception in mAccChangeCBTypeConsent" + e.getMessage());
        }
    }

    public String mAccClickAddCBReportDoc(IFormReference ifr, String Control, String Event, String value) {//Checked
        JSONObject returnJSON = new JSONObject();
        try {
            Log.consoleLog(ifr, "Inside mAccClickAddCBReportDoc::" + Control);

            if (ifr.getValue("DD_CB_APPTYPE_ID").toString().equalsIgnoreCase("")) {
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select Applicant Type"));
                return returnJSON.toString();
            } else if (ifr.getValue("DD_CB_BUREAU_ID").toString().equalsIgnoreCase("")) {
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select Credit Bureau Type"));
                return returnJSON.toString();
            } else if (!(ifr.getValue("ConsentRcvd").toString().equalsIgnoreCase("Yes"))) {
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Consent Not Received"));
                return returnJSON.toString();
            }
            String result = cm.mAddCBReportDocVal(ifr);
            if (!result.isEmpty()) {
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", result));
                return returnJSON.toString();
            }
            String applicant = (String) ifr.getValue("DD_CB_APPTYPE_ID");
            Log.consoleLog(ifr, "applicant .." + applicant);
            String negResponse = cm.mFetchRBIDefaultersNegDB(ifr, applicant);
            if (!negResponse.isEmpty()) {
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", negResponse));
                return returnJSON.toString();
            }
            String CB_Type = ifr.getValue("DD_CB_BUREAU_ID").toString();
            Log.consoleLog(ifr, "CB_Type-> " + CB_Type);
            if (CB_Type.equalsIgnoreCase("HM")) {
                HighMarkAPI ca = new HighMarkAPI();
                String response = ca.mCallHighMarkAPI(ifr, applicant);
                if (!(response.equalsIgnoreCase("SUCCESS"))) {
                    returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", response));
                    return returnJSON.toString();
                }
            } else if (CB_Type.equalsIgnoreCase("EX")) {
                ExperianAPI ca = new ExperianAPI();
                String response = ca.mCallExperianAPI(ifr, applicant);
                if (!(response.equalsIgnoreCase("SUCCESS"))) {
                    returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", response));
                    return returnJSON.toString();
                }
            } else if (CB_Type.equalsIgnoreCase("CB")) {
                CIBILAPI ca = new CIBILAPI();
                String response = ca.mCallCIBILAPI(ifr, applicant);
                if (!(response.equalsIgnoreCase("SUCCESS"))) {
                    returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", response));
                    return returnJSON.toString();
                }
            } else if (CB_Type.equalsIgnoreCase("EF")) {
                EquiFaxAPI ca = new EquiFaxAPI();
                String response = ca.mCallEquiFaxAPIAPI(ifr, applicant);
                if (!(response.equalsIgnoreCase("SUCCESS"))) {
                    returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", response));
                    return returnJSON.toString();
                }
            }
            //returnJSON.put("saveWorkitem", "true");
            returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Bureau Generated Successfully!"));
            returnJSON.put("retValue", "true");
            return returnJSON.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
            Log.errorLog(ifr, "Exception occur inside mAccClickAddCBReportDoc:" + e);
        }
        //  returnJSON.put("retValue", "true");
        return "";
    }

    //============================================Bureau Tab Code End===================================//
    //============================================CheckList Tab Code Start===================================//
    public void mAcconChangeSectionStateF_Checklist(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAcconSectionLoadchecklist");
        cm.mcheckerlist(ifr);
        ifr.setColumnDisable("LV_CheckList", "1", true);
    }
    //============================================CheckList Tab Code END===================================//

    public void mAccClickExtractVerificationData(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickExtractVerificationData");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String queryforDATA = ConfProperty.getQueryScript("queryforDATA");
            queryforDATA = queryforDATA.replaceAll("#PID#", PID);
            String QueryAddress = ConfProperty.getQueryScript("QueryAddress");
            QueryAddress = QueryAddress.replaceAll("#PID#", PID);
            List<List<String>> employeeSourcingDet = cf.mExecuteQuery(ifr, queryforDATA, "");
            List<List<String>> Address = cf.mExecuteQuery(ifr, QueryAddress, "");
            String FirstName = "", LastName = "", MiddleName = "", DOB = "";
            if (employeeSourcingDet.size() > 0) {
                FirstName = employeeSourcingDet.get(0).get(0);
                LastName = employeeSourcingDet.get(0).get(1);
                MiddleName = employeeSourcingDet.get(0).get(2);
                DOB = employeeSourcingDet.get(0).get(3);
                if (Address.size() > 0) {
                    String AddressVarify = "Line" + "-" + Address.get(0).get(0) + " " + "Zone" + "-" + Address.get(0).get(1) + " " + "State" + "-" + Address.get(0).get(2) + " " + Address.get(0).get(3);
                    ifr.setTableCellValue("LV_VerifyData", 0, 5, AddressVarify);
                } else {
                    ifr.setTableCellValue("LV_VerifyData", 0, 5, "");
                }
            }
            ifr.setTableCellValue("LV_VerifyData", 0, 1, FirstName);
            ifr.setTableCellValue("LV_VerifyData", 0, 3, LastName);
            ifr.setTableCellValue("LV_VerifyData", 0, 2, MiddleName);
            ifr.setTableCellValue("LV_VerifyData", 0, 4, DOB);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Inside mAccClickExtractVerificationData Exception:" + e);
            Log.errorLog(ifr, "Inside mAccClickExtractVerificationData Exception:" + e);
        }
    }

    public void mAccClickVerifyVerificationData(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        String fName = ifr.getTableCellValue("LV_VerifyData", 0, 1);
        String lName = ifr.getTableCellValue("LV_VerifyData", 0, 3);
        String mName = ifr.getTableCellValue("LV_VerifyData", 0, 2);
        String panFname = ifr.getTableCellValue("LV_VerifyData", 1, 1);
        String panLname = ifr.getTableCellValue("LV_VerifyData", 1, 3);
        String panMname = ifr.getTableCellValue("LV_VerifyData", 1, 2);
        if (fName.equalsIgnoreCase(panFname) && !panFname.equalsIgnoreCase("")) {
            ifr.setTableCellValue("LV_VerifyData", 3, 1, "Matched");
        } else {
            ifr.setTableCellValue("LV_VerifyData", 3, 1, "Not Matched");
        }
        if (lName.equalsIgnoreCase(panLname) && !panLname.equalsIgnoreCase("")) {
            ifr.setTableCellValue("LV_VerifyData", 3, 3, "Matched");
        } else {
            ifr.setTableCellValue("LV_VerifyData", 3, 3, "Not Matched");
        }
        if (mName.equalsIgnoreCase(panMname) && !panMname.equalsIgnoreCase("")) {
            ifr.setTableCellValue("LV_VerifyData", 3, 2, "Matched");
        } else {
            ifr.setTableCellValue("LV_VerifyData", 3, 2, "Not Matched");
        }
    }

    public String mAcconChangePopulateProductDetails(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAcconChangePopulateProductDetails:" + Control);
        switch (Control) {
            case "QNL_LOS_PROPOSED_FACILITY_ProposalType":
                cm.mLoadProduct(ifr);
                break;
            case "QNL_LOS_PROPOSED_FACILITY_Product":
                cm.mLoadSubProduct(ifr);
                break;
            case "QNL_LOS_PROPOSED_FACILITY_SubProduct":
                cm.mLoadPurpose(ifr);
                break;
            case "QNL_LOS_PROPOSED_FACILITY_LoanPurpose":
                Log.consoleLog(ifr, "LOAN PURPOSE");
                return cm.mLoadVariant(ifr);
            case "QNL_LOS_PROPOSED_FACILITY_Variant":
                cm.mLoadSchemeId(ifr);
                cm.mLoadBehaiourId(ifr);
                cm.mLoadROIType(ifr);
                cm.mLoadTenure(ifr);
                cm.mLoadFacilityType(ifr);
                String query = ConfProperty.getQueryScript("SchemeValidDate");//ADD
                query = query.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
                List<List<String>> ValidDateList = cf.mExecuteQuery(ifr, query, "");
                if (ValidDateList.get(0).get(0).equalsIgnoreCase("0")) {
                    cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_Variant,"
                            + "QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt");
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "Variant", "error", "Scheme ID is not available for this period!"));
                    return message.toString();
                } else {
                    Log.consoleLog(ifr, "Scheme allowed:" + Control);
                }
                break;
            default:
                break;
        }
        return "";
    }

    public String mAcconChangeLoanAmount(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAcconChangeLoanAmount:" + Control);
        String loan_amount = (String) ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt");
        String SchemeID = ConfProperty.getQueryScript("SchemeID");
        SchemeID = SchemeID.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        String minAmount = "";
        String maxAmount = "";
        String multiple = "";
        List<List<String>> SchemeIDlist = cf.mExecuteQuery(ifr, SchemeID, "Checking Loan Amount:");
        if (SchemeIDlist.size() > 0) {
            for (int i = 0; i < SchemeIDlist.size(); i++) {
                minAmount = SchemeIDlist.get(i).get(0);
                maxAmount = SchemeIDlist.get(i).get(1);
                multiple = SchemeIDlist.get(i).get(2);
            }
        }
        BigDecimal loan_amount1 = new BigDecimal(loan_amount.equalsIgnoreCase("") ? "0" : loan_amount);
        BigDecimal minAmount1 = new BigDecimal(minAmount.equalsIgnoreCase("") ? "0" : minAmount);
        BigDecimal maxAmount1 = new BigDecimal(maxAmount.equalsIgnoreCase("") ? "0" : maxAmount);
        BigDecimal multiple1 = new BigDecimal(multiple.equalsIgnoreCase("") ? "0" : multiple);
        JSONObject message = new JSONObject();
        if ((loan_amount1.compareTo(minAmount1) >= 0) && (loan_amount1.compareTo(maxAmount1) <= 0)) {
            if ((loan_amount1.remainder(multiple1)).compareTo(BigDecimal.ZERO) == 0) {
            } else {
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", "error", "Please Enter Amount in multiple of " + multiple + ""));
                return message.toString();
            }
        } else {
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", "");
            message.put("showMessage", cf.showMessage(ifr, "QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", "error", "Please Enter Amount in Range of " + minAmount + "-" + maxAmount + ""));
            return message.toString();
        }
        return "";
    }

    public void mAcconChangeROIType(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAcconChangeROIType:" + Control);
        String ROITYPE = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString();
        String EFFECTIVEROI = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI").toString();
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,"
                + "QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_EffectiveROI");
        if (ROITYPE.equalsIgnoreCase("Fixed")) {
            String ROIQuery = ConfProperty.getQueryScript("ROIFIXED");
            ROIQuery = ROIQuery.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
            ROIQuery = ROIQuery.replaceAll("#ROIType#", ROITYPE);
            //modified by kathir for pension Effective ROI population in lead capture on 22/07/2024
            cf.mGetLabelAndSetToField(ifr, ROIQuery, "ROI setting:", "QNL_LOS_PROPOSED_FACILITY_EffectiveROI");
        } else if (ROITYPE.equalsIgnoreCase("Floating")) {
            String ROIQuery = ConfProperty.getQueryScript("ROIFLOATING");
            ROIQuery = ROIQuery.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
            ROIQuery = ROIQuery.replaceAll("#ROIType#", ROITYPE);

            // ROIQuery = ROIQuery.replaceAll("#ROIType#", EFFECTIVEROI);
            cf.mGetLabelAndSetToField(ifr, ROIQuery, "ROI setting:", "QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium");
            // Add by Vandana
            //    String ROIValue = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROI").toString();
            String CRG = pcm.mGetCRG(ifr);
            String ROIValue = pcm.mGetRoi(ifr, CRG);
            Log.consoleLog(ifr, "ROIValue " + ROIValue);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", ROIValue);
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Spread", "visible", "false");
        }
        //modified by kathir for pension Effective ROI population in lead capture on 22/07/2024
        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture") && ROITYPE.equalsIgnoreCase("Floating")
                && String.valueOf(ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct")).equalsIgnoreCase("STP-CP")) {
            Log.consoleLog(ifr, "AcceleratorActivityManagerCode-mAcconChangeROIType-Inside lead capture floating roi type");
            String effectiveROIQuery = "SELECT A.TOTALROI FROM LOS_M_ROI A INNER JOIN LOS_M_ROI_SCHEME B "
                    + "ON A.ROIID=B.ROIID WHERE B.SchemeID='" + ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString() + "' AND A.ROITYPE='" + ROITYPE + "' AND A.ISACTIVE='Y' AND ROWNUM = 1";
            cf.mGetLabelAndSetToField(ifr, effectiveROIQuery, "ROI setting:", "QNL_LOS_PROPOSED_FACILITY_EffectiveROI");
        }
    }

    public String mAcconChangeConcession(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mAcconChangeConcession:" + Control);
        String concession = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Concession").toString();
        String roi = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROI").toString();
        if ((!(concession.equalsIgnoreCase(""))) && (!(roi.equalsIgnoreCase("")))) {
            return cm.mChangeConcession(ifr);
        }
        return "";
    }

    public String mAccClickRepaymentEngine(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccClickRepaymentEngine:");
        try {
            String amt = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt").toString().replaceAll(",", "");
            amt = amt.equalsIgnoreCase("") ? "0" : amt;
            String tenure = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Tenure").toString();
            tenure = tenure.equalsIgnoreCase("") ? "0" : tenure;
            String rate = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI").toString();
            rate = rate.equalsIgnoreCase("") ? "0" : rate;
            String params = "LoanAmount=" + amt + "&Tenure=" + tenure + "&RateEMI=" + rate;
            //params = java.util.Base64.getEncoder().encodeToString(params.getBytes("UTF-8"));
            String Ip = ConfProperty.getCommonPropertyValue("AppServerIpPort");
            String url = Ip + "//Repayment_Schedules/EMIScheduler.jsp?" + params;
            JSONObject message = new JSONObject();
            message.put("openWindow", cf.openWindow(url));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickRepaymentEngine:" + e);
            Log.errorLog(ifr, "Exception in mAccClickRepaymentEngine:" + e);
            return AcceleratorConstants.TRYCATCHERROR;
        }
    }

    public void mAccClickAfterSubmitCode(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "On Click submit button");
        Log.consoleLog(ifr, "DecisionValue:: " + ifr.getValue("DecisionValue") + " Workstep:: " + ifr.getActivityName());
        cm.InsertIntoGeneralCommentsCmplxTbl(ifr);
        String query = ConfProperty.getQueryScript("DecisionDeviationQuery").replaceAll("#ActivityName#", ifr.getActivityName());
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for DecisionCreditAppraisalQuery");
        String devWorkstep = "";
        if (result.size() > 0) {
            devWorkstep = result.get(0).get(0);
        }
        if (devWorkstep.equalsIgnoreCase(ifr.getActivityName())) {
            cm.mSetCurrentDeviationLevel(ifr);
        }

        query = "SELECT ActivityName FROM LOS_M_ActivityName  WHERE ActivityCode in ('W6') and ActivityName='" + ifr.getActivityName() + "'";
        result = cf.mExecuteQuery(ifr, query, "Query for DecisionBranchMaker");
        devWorkstep = "";
        if (result.size() > 0) {
            devWorkstep = result.get(0).get(0);
        }
        if (devWorkstep.equalsIgnoreCase(ifr.getActivityName())) {
            cm.mSetCurrentDeviationLevelCB(ifr);
        }

        /*Added By: Veena.K
         Date: 14-06-2024
         Description: To update approval cycles in checker stage.*/
        String RecommendingWorkstepFetchQ = ConfProperty.getQueryScript("RecommendingWorkstepFetchQuery").replaceAll("##ActivityName##", ifr.getActivityName());
        List<List<String>> RecommendingWorkstepFetchR = cf.mExecuteQuery(ifr, RecommendingWorkstepFetchQ, "Query for RecommendingWorkstepFetch");
        Log.consoleLog(ifr, "RecommendingWorkstepFetchR::: " + RecommendingWorkstepFetchR);
        String RecommendingActivity = "";
        if (RecommendingWorkstepFetchR.size() > 0) {
            RecommendingActivity = RecommendingWorkstepFetchR.get(0).get(0);
            Log.consoleLog(ifr, "inside mAccClickAfterSubmitCode:: RecommendingActivity:: " + RecommendingActivity);

        }
        if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("REC") && RecommendingActivity.equalsIgnoreCase(ifr.getActivityName())) {
            Log.consoleLog(ifr, "inside mAccClickAfterSubmitCode:: Branch Checker code:: DecisionValue:: " + ifr.getValue("DecisionValue"));
            String Status = bbcc.ApprovalCycleSettings(ifr, RecommendingActivity, "", "", "");
            Log.consoleLog(ifr, "Status from ApprovalCycleSettings for " + RecommendingActivity + " ::" + Status);

        }

    }

    public String mAccClickSubmit(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "On Click submit button");
        try {
            if (ifr.getValue("DecisionValue").equals("SD")) {
                String query = ConfProperty.getQueryScript("DecisionBranchCheckerQuery");
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for DecisionBranchCheckerQuery");
                if (result.size() > 0) {
                    String branchChecker = result.get(0).get(0);
                    if (branchChecker.equalsIgnoreCase(ifr.getActivityName())) {
                        String resultValue = cm.mAccClickDAMCSubProcess(ifr);
                        if (!(resultValue.equalsIgnoreCase(""))) {
                            return resultValue;
                        }
                    }
                }
            }
            String query = ConfProperty.getQueryScript("DecisionCreditAppraisalQuery");
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for DecisionCreditAppraisalQuery");
            if (result.size() > 0) {
                String underwriter = result.get(0).get(0);
                if (underwriter.equalsIgnoreCase(ifr.getActivityName())) {
                    cm.mSetDeviationLevel(ifr);
                    if (ifr.getValue("DevMaxLevel").toString().equalsIgnoreCase("") || ifr.getValue("DevCurrentLevel").toString().equalsIgnoreCase("")) {
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Level Not Found kindly Check Approval Matrix!"));
                        return message.toString();
                    }
                }
            }
            query = "SELECT ActivityName FROM LOS_M_ActivityName  WHERE ActivityCode in ('S2') and ActivityName='" + ifr.getActivityName() + "'";
            result = cf.mExecuteQuery(ifr, query, "Query for DecisionBranchMaker");
            String devWorkstep = "";
            if (result.size() > 0) {
                devWorkstep = result.get(0).get(0);
            }
            if (devWorkstep.equalsIgnoreCase(ifr.getActivityName())) {
                cm.mSetDeviationLevelCB(ifr);
            }

            query = ConfProperty.getQueryScript("DecisionConvenorQuery");
            result = cf.mExecuteQuery(ifr, query, "Query for DecisionConvenorQuery");
            if (result.size() > 0) {
                String convenor = result.get(0).get(0);
                if (convenor.equalsIgnoreCase(ifr.getActivityName())) {
                    String resultValue = cm.mCheckCommittee(ifr);
                    if (!(resultValue.equalsIgnoreCase(""))) {
                        return resultValue;
                    }
                }
            }
            query = ConfProperty.getQueryScript("DecisionCommitteeApprovalQuery");
            result = cf.mExecuteQuery(ifr, query, "Query for DecisionCommitteeApprovalQuery");
            if (result.size() > 0) {
                String committee = result.get(0).get(0);
                if (committee.equalsIgnoreCase(ifr.getActivityName())) {
                    cm.mSetChildCommiteeStatus(ifr);
                }
            }
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("")
                    || ifr.getValue("Decision_Remarks").toString().equalsIgnoreCase("")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Mandatory Fields cannot be left empty!"));
                return message.toString();
            }
            //LoanRecord Validation code start.
            RLOSHead rh = new RLOSHead();
            JSONObject message = new JSONObject();
            rh.mSetGridAMLoanRecordValue(ifr, Control, Event, value, "Linear");
            String returnMessage = rh.mLoanRecordValidation(ifr, Control, Event, value);
            if (!returnMessage.equalsIgnoreCase("")) {
                message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please Fill " + returnMessage + " Data !!"));
                return message.toString();
            }
            //LoanRecord Validation code end.

            String workstep = ifr.getValue("ActivityName").toString();
            ifr.setValue("Decision", ifr.getValue("DecisionValue").toString());
            query = ConfProperty.getQueryScript("DecisionInitiateQuery");
            result = cf.mExecuteQuery(ifr, query, "Query for QDE_Initiation");
            String activity1 = "";
            if (result.size() > 0) {
                activity1 = result.get(0).get(0);
            }
            query = ConfProperty.getQueryScript("DecisionReInitiateQuery");
            result = cf.mExecuteQuery(ifr, query, "Query for QDE_ReInitiation");
            String activity2 = "";
            if (result.size() > 0) {
                activity2 = result.get(0).get(0);
            }

            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query1 = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query1, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String queryWS = ConfProperty.getQueryScript("IfPortalQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> data = cf.mExecuteQuery(ifr, queryWS, "Execute query for fetching data");
            String isPortal = data.get(0).get(0);
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S") && (workstep.equalsIgnoreCase("Branch Maker"))) {
                //Added code for Co-Borrower Eligibility Branch Maker VL

                //Added by monesh on 16-06-2024 For checking the MISdata 
                if (bds.checkmisdata(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly Update the MIS Data "));
                    return message.toString();
                }
                if (isPortal.equalsIgnoreCase("Yes")) {
                    String consentQuery = "select ConsentReceived from LOS_NL_BUREAU_CONSENT where PID='" + PID + "'";
                    List<List<String>> consentQueryResult = cf.mExecuteQuery(ifr, consentQuery, "Query for consentQueryResult");
                    String consentReceived = "";
                    if (consentQueryResult.size() > 0) {
                        consentReceived = consentQueryResult.get(0).get(0);
                        Log.consoleLog(ifr, " ConsentReceived in BTN_Submit for CB  :" + consentReceived);
                        String E_Status = ifr.getTableCellValue("CB_Eligibility_Grid", 0, 1);
                        Log.consoleLog(ifr, "mAccClickSubmit::Eligibility for CB  :" + E_Status);
                        if (!consentReceived.equalsIgnoreCase("Accepted")) {
                            Log.consoleLog(ifr, "mAccClickSubmit::CB Consent Not Received  and Eligible validation success" + E_Status + consentReceived);
                            message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Co-Borrower consent is not received!"));
                            return message.toString();
                        }

                    }
                }

                if (loan_selected.equalsIgnoreCase("Canara Pension")
                        || loan_selected.equalsIgnoreCase("Canara Budget")
                        || loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                    String Status = bbcc.ApprovalMatrixDerivationUpdated(ifr, workstep, loan_selected);
                    Log.consoleLog(ifr, " ApprovalMatrixDerivationUpdated status  :" + Status);

                    if (!Status.equalsIgnoreCase("success") && !Status.equalsIgnoreCase("error")) {
                        Log.consoleLog(ifr, " ApprovalMatrixDerivation status if:" + Status);
                        message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", Status));
                        return message.toString();
                    } else if (Status.equalsIgnoreCase("error")) {
                        Log.consoleLog(ifr, " ApprovalMatrixDerivation status else:" + Status);
                        message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error",
                                "Error occured in Approval Matrix Derivation, Please contact Administrator"));
                        return message.toString();

                    }

                }
            }

            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("R")) {
                if (workstep.equalsIgnoreCase(activity1)) {
                    ifr.setValue("PreviousRejActivity", activity2);
                } else {
                    ifr.setValue("PreviousRejActivity", workstep);
                }
                if (ifr.getValue("RejectCategory").toString().equalsIgnoreCase("") || ifr.getValue("RejectSubCategoryDecision").toString().equalsIgnoreCase("")) {
                    message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please enter Mandatory Fields"));
                } else if (ifr.getValue("FE_Recommended_Loan_Amount").toString().equalsIgnoreCase("") && (workstep.equalsIgnoreCase("Lead Capture"))) {
                    message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please enter Recommended Loan Amount"));
                }

                ifr.setValue("RejectCategory", ifr.getValue("RejectCategoryDecision").toString());
                ifr.setValue("RejectSubCategory", ifr.getValue("RejectSubCategoryDecision").toString());
                message.put("completeWorkitem", "true");
                Log.consoleLog(ifr, "Status from Rejected and branchmaker  completeWorkitem Called ");
                return message.toString();
            }
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("SM") && workstep.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "inside decision send back");
                onclickSendBack(ifr, Control, Event, value);
                return message.toString();
            }
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("SM") && workstep.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "inside decision send back");

                if (ifr.getValue("FE_Recommended_Loan_Amount").toString().equalsIgnoreCase("") && (workstep.equalsIgnoreCase("Lead Capture"))) {
                    message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please enter Recommended Loan Amount"));
                    return message.toString();
                }
            }
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S") && workstep.equalsIgnoreCase("Disbursement Maker")) {
                JSONObject returnJSON = new JSONObject();
                String loanAccountNum = "";
                String loanAccQuery = "SELECT LOANACCOUNTNO FROM LOS_NL_LOAN_ACC_CREATION WHERE PID ='" + PID + "'";
                List<List<String>> loanAccQueryResult = cf.mExecuteQuery(ifr, loanAccQuery, "Query for consentQueryResult");
                if (loanAccQueryResult.size() > 0) {
                    loanAccountNum = loanAccQueryResult.get(0).get(0);
                }
                Log.consoleLog(ifr, "loanAccountNum:::" + loanAccountNum);
                if (loanAccountNum.equalsIgnoreCase("")) {
                    returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please create Loan Account to proceed."));
                } else {
                    returnJSON.put("completeWorkitem", "true");
                }
                Log.consoleLog(ifr, "from disbmaker completeWorkitem Called ");
                return returnJSON.toString();
            }
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S") && workstep.equalsIgnoreCase("Disbursement Checker")) {
                Log.consoleLog(ifr, "step-1 :: ");
                if (loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Canara Budget")) {
                    try {
                        Log.consoleLog(ifr, "step-2 :: ");
                        String retailExposureTotalFcr = cm.getRetailExposureTotalFcr(ifr);
                        String loanAmount = ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", 0, 3);
                        Log.consoleLog(ifr, "loanAmount : " + loanAmount);
                        BigDecimal acyAmountDecimal = new BigDecimal(retailExposureTotalFcr);
                        Log.consoleLog(ifr, "acyAmountDecimal : " + acyAmountDecimal);
                        BigDecimal loanAmountDecimal = new BigDecimal(loanAmount);
                        Log.consoleLog(ifr, "loanAmountDecimal : " + loanAmountDecimal);
                        BigDecimal totalExposureDecimal = acyAmountDecimal.add(loanAmountDecimal);
                        Log.consoleLog(ifr, "totalExposureDecimal : " + totalExposureDecimal);
                        String totalExposure = totalExposureDecimal.toString();
                        Log.consoleLog(ifr, "totalExposure : " + totalExposure);
                        String strAcctquery = "update LOS_EXT_TABLE set LOANAMOUNT='#LOANAMOUNT#' WHERE PID ='#WINAME#'".replaceAll("#WINAME#", PID).replaceAll("#LOANAMOUNT#", totalExposure);
                        Log.consoleLog(ifr, "amounr :::" + strAcctquery);
                        List<List<String>> amounr = cf.mExecuteQuery(ifr, strAcctquery, "Query for Loan Amount update");
                        Log.consoleLog(ifr, "FKEYQUERY :::" + amounr);
                        JSONObject returnJSON = new JSONObject();

                        returnJSON.put("completeWorkitem", "true");
                        Log.consoleLog(ifr, "Status from disbmaker completeWorkitem Called ");
                        return returnJSON.toString();
                    } catch (Exception e) {
                        Log.consoleLog(ifr, " Exception in Cal Exposer value " + e);
                    }
                }
            }
            if (workstep.equalsIgnoreCase("PostSanction")) {
                if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S")) {
                    Log.consoleLog(ifr, "Status from NESL triggered  ");
                    JSONObject returnJSON = new JSONObject();
                    String EsignStatusQuery = ConfProperty.getQueryScript("getESignStatusQuery").replaceAll("#PID#", ProcessInstanceId);
                    Log.consoleLog(ifr, "EsignStatusQuery ::" + EsignStatusQuery);
                    String EsignStatus = "";
                    List<List<String>> EsignStatusResult = ifr.getDataFromDB(EsignStatusQuery);
                    if (EsignStatusResult.size() > 0) {
                        EsignStatus = EsignStatusResult.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "EsignStatus --> " + EsignStatus);
                    if (EsignStatus.equalsIgnoreCase("Manual")) {
                        String documentIndex = "";
                        String esignManual = "select DOCUMENTINDEX from LOS_NL_ESIGN_STATUS where PID ='" + PID + "'";
                        List<List<String>> manualUpload = cf.mExecuteQuery(ifr, esignManual, "Query for esignManual");
                        if (manualUpload.size() > 0) {
                            documentIndex = manualUpload.get(0).get(0);
                        }
                        Log.consoleLog(ifr, "documentIndex --> " + documentIndex);
                        if (documentIndex.equalsIgnoreCase("")) {
                            message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly Upload the manual Nesl Document"));
                            return message.toString();
                        }
                    }
                    String Message = nesltriger(ifr);
                    if (Message.contains("success")) {
                        Log.consoleLog(ifr, "Status from NESL triggered  passed ");
                        returnJSON.put("completeWorkitem", "true");
                    } else {
                        Log.consoleLog(ifr, "Status from NESL triggered  failed ");
                        returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", Message));
                    }
                    Log.consoleLog(ifr, "PostSanction from PostSanction completeWorkitem Called ");
                    return returnJSON.toString();
                }

            }
            if (workstep.equalsIgnoreCase("Sanction")) {
                if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S")) {
                    Log.consoleLog(ifr, "Approved Loan Amount::");
                    JSONObject returnJSON = new JSONObject();
                    String loanAccountNum = "";
                    String loanAccQuery = "SELECT APPROVED_LOANAMOUNT FROM LOS_NL_LA_FINALELIGIBILITY WHERE PID ='" + PID + "'";
                    List<List<String>> loanAccQueryResult = cf.mExecuteQuery(ifr, loanAccQuery, "Query for consentQueryResult");
                    if (loanAccQueryResult.size() > 0) {
                        loanAccountNum = loanAccQueryResult.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "loanAccountNum:::" + loanAccountNum);
                    if (loanAccountNum.equalsIgnoreCase("")) {
                        returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly enter Approved Loan Amount in Final Eligibility to proceed."));
                        return returnJSON.toString();
                    } else {
                        Log.consoleLog(ifr, "Approved Loam Amount filled::");

                        //Added by Ahmed on 14-08-2024 triggering MailContent from DLPCommonObjects=========
                        String bodyParams = pcm.getLoanSelected(ifr) + "#" + pcm.getApplicationRefNumber(ifr) + "#" + pcm.getCurrentDate();
                        String subjectParams = "";
                        String fileName = "";
                        String fileContent = "";
                        pcm.triggerCCMAPIs(ifr, PID, "", "11", bodyParams, subjectParams, fileName, fileContent);

                        returnJSON.put("completeWorkitem", "true");
                    }
                    Log.consoleLog(ifr, "Sanction from Sanction completeWorkitem Called ");
                    //return returnJSON.toString();
                }
            }

            if (workstep.equalsIgnoreCase("Sanction")) {
                int gridSize = ifr.getDataFromGrid("ALV_FEE_CHARGES").size();
                Log.consoleLog(ifr, " Fees & Charges Grid :: " + gridSize);
                if (gridSize == 0) {
                    JSONObject respJSON = new JSONObject();
                    respJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly click Fees & Charges in Financial info to proceed."));
                    return respJSON.toString();
                }
            }

            if (workstep.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, " Branch maker:: ");
                String settledhis = "";
                String overdue = "";
                String MandatoryCheck = "SELECT SETTLEDACCOUNTINCREDITHISTORY,OVERDUEINCREDITHISTORY FROM LOS_NL_BASIC_INFO WHERE PID='" + PID + "' and APPLICANTTYPE='B'";
                List<List<String>> mandatoryresult = cf.mExecuteQuery(ifr, MandatoryCheck, "Generate Document List");
                if (mandatoryresult.size() > 0) {
                    settledhis = mandatoryresult.get(0).get(0);
                    overdue = mandatoryresult.get(0).get(1);
                    Log.consoleLog(ifr, " settledhis:: " + settledhis);
                    Log.consoleLog(ifr, " overdue:: " + overdue);

                    if (settledhis.isEmpty() || overdue.isEmpty()) {
                        JSONObject respJSON = new JSONObject();
                        respJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please Check in party details  'written-off/settled' && 'Overdue in credit history' fields is empty "));
                        return respJSON.toString();
                    }
                }
                String scheduleCode = "";
                String rateChartcode = "";
                String roiType = "";
                String MandCheck = "select schedulecode,ratechartcode,roitype from los_nl_proposed_facility where pid='" + PID + "'";
                List<List<String>> mandatorycheckresult = cf.mExecuteQuery(ifr, MandCheck, "mandcheck");

                if (mandatorycheckresult.size() > 0) {
                    scheduleCode = mandatorycheckresult.get(0).get(0);
                    rateChartcode = mandatorycheckresult.get(0).get(1);
                    roiType = mandatorycheckresult.get(0).get(2);
                    Log.consoleLog(ifr, " scheduleCode:: " + scheduleCode);
                    Log.consoleLog(ifr, " rateChartcode:: " + rateChartcode);
                    Log.consoleLog(ifr, " roiType:: " + roiType);

                    if (scheduleCode.isEmpty() || rateChartcode.isEmpty() || roiType.isEmpty()) {
                        JSONObject respJSON = new JSONObject();
                        respJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Please select the Roitype,Schedulecode and the Ratechartcode in the proposed details"));
                        return respJSON.toString();
                    }

                }

            }
            JSONObject returnJSON = new JSONObject();

            returnJSON.put("completeWorkitem", "true");
            Log.consoleLog(ifr, "completeWorkitem Called ");
            return returnJSON.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inm AccClickSubmit" + e);
            Log.errorLog(ifr, "Exception inm AccClickSubmit" + e);
            return pcm.returnErrorThroughExecute(ifr, "Error in Submit ");
        }

    }

    public void mDBRcalculation(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside DBR calculation:");
        try {
            String inc = ifr.getValue("QL_DEAL_MonthlyIncome").toString();
            Log.consoleLog(ifr, "Income= " + inc);
            String exp = ifr.getValue("QL_DEAL_MonthlyExpenses").toString();
            Log.consoleLog(ifr, "Liability= " + exp);
            String Query = ConfProperty.getQueryScript("InstallmentAmount").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            List<List<String>> inst = cf.mExecuteQuery(ifr, Query, "Installment Amount");
            String instamt = inst.get(0).get(0);
            Log.consoleLog(ifr, "Final amount= " + instamt);
            try {
                BigDecimal INC = new BigDecimal(inc.equalsIgnoreCase("") ? "0" : inc);
                BigDecimal EXP = new BigDecimal(exp.equalsIgnoreCase("") ? "0" : exp);
                BigDecimal INSAMT = new BigDecimal(instamt.equalsIgnoreCase("") ? "0" : instamt);
                BigDecimal b = new BigDecimal("100");
                BigDecimal dbr = (EXP.divide(INC, 2, RoundingMode.HALF_UP)).multiply(b);
                Log.consoleLog(ifr, "DBR= " + dbr);
                BigDecimal rdbr = ((EXP.add(INSAMT)).divide(INC, 2, RoundingMode.HALF_UP)).multiply(b);
                Log.consoleLog(ifr, "Revised DBR= " + rdbr);

                ifr.setValue("QL_DEAL_DBR", String.valueOf(dbr));
                ifr.setValue("QL_DEAL_Revised_dbr", String.valueOf(rdbr));
            } catch (Exception e) {
                ifr.setValue("QL_DEAL_DBR", "");
                ifr.setValue("QL_DEAL_Revised_dbr", "");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in DBR Calculation" + e);
            Log.errorLog(ifr, "Exception in DBR Calculation" + e);
        }
    }

    public String mEligiblityBRMS(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            Log.consoleLog(ifr, "Inside Eligibility BRMS :::::");
            String inc = ifr.getValue("QL_DEAL_MonthlyIncome").toString();
            String exp = ifr.getValue("QL_DEAL_MonthlyExpenses").toString();
            if (inc.equalsIgnoreCase("") || exp.equalsIgnoreCase("")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Mandatory Fields cannot be left empty!"));
                return message.toString();
            }
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID = " + PID);

            String query = ConfProperty.getQueryScript("EligiblityRuleScoreCheck").replaceAll("#PID#", PID);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "To fetch CB Score query:");
            String CB_Score = "";
            if (result.size() > 0) {
                CB_Score = result.get(0).get(0);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please generate CIBIL for Applicant!"));
                return message.toString();
            }
            query = ConfProperty.getQueryScript("EligiblityRuleAddressCheck").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "To fetch Address:");
            String Ownership = "";
            if (result.size() > 0) {
                Ownership = result.get(0).get(0);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Fill Current Address for Applicant!"));
                return message.toString();
            }
            query = ConfProperty.getQueryScript("EligiblityRuleProductCheck").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "To fetch Address:");
            String Product = "";
            if (result.size() > 0) {
                Product = result.get(0).get(0);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Enter Product Details!"));
                return message.toString();
            }
            query = ConfProperty.getQueryScript("EligiblityRuleOccupationCheck").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "To fetch Address:");
            String OccupationType = "";
            String EmployerApproved = "";
            if (result.size() > 0) {
                OccupationType = result.get(0).get(0);
                EmployerApproved = result.get(0).get(1);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Enter Occupation Details!"));
                return message.toString();
            }
            query = ConfProperty.getQueryScript("EligiblityRuleOccupationCheck").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "To fetch Address:");
            String Existing_Customer = "";
            if (result.size() > 0) {
                Existing_Customer = result.get(0).get(0);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Enter Customer Details!"));
                return message.toString();
            }

            BigDecimal INC = new BigDecimal(inc.equalsIgnoreCase("") ? "0" : inc);
            BigDecimal EXP = new BigDecimal(inc.equalsIgnoreCase("") ? "0" : exp);
            BigDecimal dispIncome = INC.subtract(EXP);
            int decimalPlaces = 0;
            dispIncome = dispIncome.setScale(decimalPlaces, BigDecimal.ROUND_DOWN);
            String disp = dispIncome.toString();
            Log.consoleLog(ifr, "dispIncome = " + disp);
            String Foir = ifr.getValue("QL_DEAL_Revised_dbr").toString();
            Log.consoleLog(ifr, "Foir = " + Foir);

            String messageValue = "";
            JSONObject result1 = cf.executeBRMSRule(ifr, "eligibility", CB_Score + "," + Existing_Customer + "," + disp + "," + EmployerApproved + "," + Foir + "," + OccupationType + "," + Product + "," + Ownership);
            Log.consoleLog(ifr, "Result of Eligibility = " + result1);
            if (cf.getJsonValue(result1, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                if (cf.getJsonValue(result1, "set_eligibility002").equalsIgnoreCase("Not Eligible")) {
                    messageValue = "Applicant's is not Eligible";
                    ifr.setValue("QL_DEAL_EligibilityCheck", "Not Eligible");
                } else {
                    messageValue = "Applicant's Eligible";
                    ifr.setValue("QL_DEAL_EligibilityCheck", "Eligible with Approval");
                    JSONObject result2 = cf.executeBRMSRule(ifr, "LoanCalRule_HL", disp + "," + OccupationType + "," + Product);
                    Log.consoleLog(ifr, "Result of Eligibility = " + result2);
                    if (cf.getJsonValue(result2, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {

                    } else {
                        ifr.setValue("QL_DEAL_EligibilityCheck", "");
                        messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                    }
                }
            } else {
                ifr.setValue("QL_DEAL_EligibilityCheck", "");
                messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
            }
            if (!(messageValue.equalsIgnoreCase(""))) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", messageValue));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mEligiblityBRMS" + e);
        }
        return "";
    }

    public void mAccSubFormLoadFetchExistCust(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccSubFormLoadFetchExistCustmethod");
        try {
            ifr.clearTable("SF_CUST_DET");
            String param = ifr.getValue("CTRID_PD_SRCHPARM").toString();
            String val = ifr.getValue("CTRID_PD_SRCHVAL").toString();
            String query = "";
            boolean f = false;
            if (param != null && param.equalsIgnoreCase("cif")) {
                if (val != null && !val.trim().equals("")) {
                    query = ConfProperty.getQueryScript(AcceleratorConstants.FETCHCUSTCIF).replaceAll("#val#", val);
                    Log.consoleLog(ifr, "------final query------" + query);
                    f = true;
                }
            } else if (param != null && val != null && !val.trim().equals("")) {
                query = ConfProperty.getQueryScript(AcceleratorConstants.FETCHCUST).replaceAll("#val#", val).replaceAll("#param#", param);
                Log.consoleLog(ifr, "------final query------" + query);
                f = true;
            }
            if (f) {
                cf.addToTable(ifr, "SF_CUST_DET", query, "Customer Type~CIF~Customer Name");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccSubFormLoadFetchExistCust method" + e);
            Log.errorLog(ifr, "Exception in mAccSubFormLoadFetchExistCust method" + e);
        }
    }

    public void mAccSubFormViewDataFetchGridData(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccSubFormViewDataFetchGridData");
        try {
            String partyType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
            Log.consoleLog(ifr, "------final partyType------" + partyType);
            String kycCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.KYCSUBFORMCOLS);
            String addrCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.ADDSUBFORMSCOLS);
            String occCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.OCCSUBCOLS);
            ifr.clearTable("SF_KYC");
            ifr.clearTable("SF_ADDRESS");
            ifr.clearTable("SF_OCCUPATION");
            String cif = ifr.getTableCellValue("SF_CUST_DET", 0, 0);
            // fetching details for kyc
            String q1 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHKYCDET).replaceAll("#val#", cif);
            Log.consoleLog(ifr, "------final query for KYC------" + q1);
            cf.addToTable(ifr, "SF_KYC", q1, kycCols);
            // fetching details for addr
            String q2 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHADDRDET).replaceAll("#val#", cif);
            Log.consoleLog(ifr, "------final query ADDR-----" + q2);
            cf.addToTable(ifr, "SF_ADDRESS", q2, addrCols);
            // fetching details for occupation
            String q3 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHOCCSUBQUERY).replaceAll("#val#", cif);
            Log.consoleLog(ifr, "------final query Occ-----" + q3);
            cf.addToTable(ifr, "SF_OCCUPATION", q3, occCols);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mAccSubFormViewDataFetchGridData method" + e);
            Log.errorLog(ifr, "Exception in  mAccSubFormViewDataFetchGridData method" + e);
        }
    }

    public String mAccSubformDoneClickPartyDetail(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccsubformDoneClickPartyDetail");
        try {
            String partyType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
            String entityType = ifr.getValue("CTRID_PD_ENTYTYPE").toString();
            String kycGridID = "LV_KYC";
            String addrGridID = "LV_ADDRESS";
            String occGridID = "LV_OCCUPATION_INFO";
            String[] kycCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.KYCMAINFORMCOLS).split(",");
            String[] addrCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.ADDRMAINFORMCOLS).split(",");
            String[] occCols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.OCCMAINCOLS).split(",");
            ifr.clearTable(kycGridID);
            ifr.clearTable(addrGridID);
            ifr.clearTable(occGridID);
            String cif = ifr.getTableCellValue("SF_CUST_DET", 0, 0);
            //fetching details for kyc
            String q1 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHKYCDET).replaceAll("#val#", cif);
            List<List<String>> r1 = cf.mExecuteQuery(ifr, q1, " query for Fetch Details of KYC");
            JSONArray finalKycGrid = new JSONArray();
            for (int i = 0; i < r1.size(); i++) {
                JSONObject row = new JSONObject();
                for (int j = 0; j < kycCols.length; j++) {
                    String kycID = kycCols[j];
                    if (kycID.contains("~")) {
                        kycID = kycID.split("~")[1];
                        boolean dtFlag = (kycID.split("~")[0].equalsIgnoreCase("dt") ? false : true);
                        if (dtFlag) {
                            row.put(kycID, cf.getStringFormattedDate(ifr, r1.get(i).get(j)));
                        }
                    } else {
                        row.put(kycID, r1.get(i).get(j));
                    }
                }
                finalKycGrid.add(row);
            }
            // fetching details for address            
            String q2 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHADDRDET).replaceAll("#val#", cif);
            List<List<String>> r2 = cf.mExecuteQuery(ifr, q2, " query for Fetch Details of ADDR");
            JSONArray finalAddressGrid = new JSONArray();
            for (int i = 0; i < r2.size(); i++) {
                JSONObject row = new JSONObject();
                for (int j = 0; j < addrCols.length; j++) {
                    row.put(addrCols[j], r2.get(i).get(j));
                }
                finalAddressGrid.add(row);
            }
            // fetching details for occupation
            String q3 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHOCCMAINQUERY).replaceAll("#val#", cif);
            Log.consoleLog(ifr, "------final query Occ-----" + q3);
            List<List<String>> r3 = cf.mExecuteQuery(ifr, q3, " query for Fetch Details of Occupation");
            Log.consoleLog(ifr, "Query result = " + r3);
            JSONArray finalOccupationGrid = new JSONArray();
            for (int i = 0; i < r3.size(); i++) {
                JSONObject row = new JSONObject();
                for (int j = 0; j < occCols.length; j++) {
                    if (occCols[j].contains("~")) {
                        occCols[j] = occCols[j].split("~")[1];
                        boolean dtFlag = (occCols[j].split("~")[0].equalsIgnoreCase("d") ? false : true);
                        if (dtFlag) {
                            row.put(occCols[j], cf.getStringFormattedDate(ifr, r3.get(i).get(j)));
                        }
                    } else {
                        row.put(occCols[j], r3.get(i).get(j));
                    }
                }
                finalOccupationGrid.add(row);
            }
            // fetching the party details in main form
            String q4 = ConfProperty.getQueryScript(AcceleratorConstants.FETCHCIFDATA).replaceAll("#val#", cif);
            Log.consoleLog(ifr, "------final query PARTY-----" + q4);
            List<List<String>> r4 = cf.mExecuteQuery(ifr, q4, " query for Fetch Details of PARTY");
            Log.consoleLog(ifr, "Query result = " + r4);
            JSONArray finalBasicInfoGrid = new JSONArray();
            String[] partyICols = ConfProperty.getCommonPropertyValue(AcceleratorConstants.PARTYCIFINDVCOL).split(",");
            for (int i = 0; i < r4.size(); i++) {
                JSONObject row = new JSONObject();
                if (ifr.getValue("CTRID_PD_ENTYTYPE").toString().equalsIgnoreCase("I")) {
                    for (int j = 0; j < partyICols.length; j++) {
                        if (partyICols[j] != null && !partyICols[j].trim().equals("")
                                && partyICols[j].equalsIgnoreCase("QNL_BASIC_INFO_ApplicantType")) {
                            row.put(partyICols[j], partyType);
                        } else if (partyICols[j].equalsIgnoreCase("QNL_BASIC_INFO_ExistingCustomer")) {
                            row.put(partyICols[j], "Yes");
                        } else if (partyICols[j].equalsIgnoreCase("QNL_BASIC_INFO_EntityType")) {
                            row.put(partyICols[j], entityType);
                        } else if (partyICols[j].equalsIgnoreCase("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB")) {
                            row.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB", cf.getStringFormattedDate(ifr, r4.get(i).get(j)));
                        } else {
                            row.put(partyICols[j], r4.get(i).get(j));
                        }
                    }
                }
                row.put("QNL_BASIC_INFO_CustomerID", cif);
                row.put(addrGridID, finalAddressGrid);
                row.put(kycGridID, finalKycGrid);
                row.put(occGridID, finalOccupationGrid);
                finalBasicInfoGrid.add(row);
            }
            Log.consoleLog(ifr, "Final JSON Array::" + finalBasicInfoGrid);
            JSONObject obj = new JSONObject();
            obj.put("addDataToGridInMainForm", cf.showMessage(ifr, "ALV_BASIC_INFO", finalBasicInfoGrid.toString(), ""));
            obj.put("saveWorkitem", "true");
            return obj.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccsubformDoneClickPartyDetail method" + e);
            Log.errorLog(ifr, "Exception in mAccsubformDoneClickPartyDetail method" + e);
        }
        return "";
    }

    public void mAccClickResetButton(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickResetButton method===Acc::");

            ifr.setStyle("Addhar_check", "visible", "false");
            ifr.setStyle("fetch_details", "visible", "false");

            String[] resetPartyFiels = ConfProperty.getCommonPropertyValue(AcceleratorConstants.RESETPARTYFIELDS).split(",");
            Log.consoleLog(ifr, "Reset fields for Party details section" + Arrays.toString(resetPartyFiels));
            for (String resetPartyFiel : resetPartyFiels) {
                ifr.setValue(resetPartyFiel, "");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickResetButton method" + e);
            Log.errorLog(ifr, "Exception in mAccClickResetButton method" + e);
        }
    }

    public void mAccChangePartyTypeDocGrid(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            String query = ConfProperty.getQueryScript("SelectApplicantForDoc").replaceAll("#insertionOrderId#", ifr.getValue("QNL_UPLOAD_DOCUMENT_ApplicantType").toString());
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for document Name");
            String PartyType = "";
            String EntityType = "";
            if (result.size() > 0) {
                PartyType = result.get(0).get(0);
                EntityType = result.get(0).get(1);
            }

            query = ConfProperty.getQueryScript("SelectDocumentType").replaceAll("#ApplicableFor#", PartyType).
                    replaceAll("#EntityType#", EntityType).replaceAll("#ActivityName#", ifr.getActivityName()).
                    replaceAll("#pid#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            cf.loadComboValues(ifr, "QNL_UPLOAD_DOCUMENT_DocumentName", query, "", "LV");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccChangePartyTypeDocGrid method" + e);
            Log.errorLog(ifr, "Exception in mAccChangePartyTypeDocGrid method" + e);
        }
    }

    public String mAccChangePartyTypeValidation(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            String partyType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
            for (int i = 0; i < cf.getGridCount(ifr, "ALV_BASIC_INFO"); i++) {
                if (partyType.equalsIgnoreCase("B") && partyType.equalsIgnoreCase(ifr.getTableCellValue("ALV_BASIC_INFO", i, "QNL_BASIC_INFO_ApplicantType").toString())) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Duplicate " + "Borrower" + " PartyType"));
                    ifr.setValue("CTRID_PD_PARTYTYPE", "");
                    return message.toString();
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccChangePartyTypeValidation method" + e);
            Log.errorLog(ifr, "Error in mAccChangePartyTypeValidation method" + e);
        }
        return "";
    }

    public String mAccClickRCUSubprocess(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickRCUSubprocess method");
            JSONObject message = new JSONObject();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.RCUSUBPROCESSMAINQUERY).replaceAll("#PID#", PID);
            List<List<String>> queryResult = ifr.getDataFromDB(query);
            Log.consoleLog(ifr, "queryResult.size::" + queryResult.size());
            if (queryResult.size() > 0) {
                for (int i = 0; i < queryResult.size(); i++) {
                    String Branch = ifr.getValue("QL_LEAD_DET_BranchName").toString();
                    String Name = queryResult.get(i).get(0);
                    String Code = queryResult.get(i).get(1);
                    String Mobile = queryResult.get(i).get(2);
                    String Email = queryResult.get(i).get(3);
                    String InitiatedBy = queryResult.get(i).get(4);
                    String InitiatorRemarks = queryResult.get(i).get(5);
                    String insertionOrderID = queryResult.get(i).get(6);
                    String F_Key = queryResult.get(i).get(7);
                    String ApplicantID = queryResult.get(i).get(8);
                    String attributes = "<ParentWINo>" + PID + "</ParentWINo>"
                            + "<ParentQueryID>" + insertionOrderID + "</ParentQueryID>"
                            + "<Q_ParentRCUID>" + F_Key + "</Q_ParentRCUID>"
                            + "<BorrowerName>" + ApplicantID + "</BorrowerName>"
                            + "<Branch>" + Branch + "</Branch>"
                            + "<InitiatedBy>" + InitiatedBy + "</InitiatedBy>"
                            + "<AgencyName>" + Name + "</AgencyName>"
                            + "<Name>" + Name + "</Name>"
                            + "<MobileNo>" + Mobile + "</MobileNo>"
                            + "<Code>" + Code + "</Code>"
                            + "<Email>" + Email + "</Email>"
                            + "<InitiatorRemarks>" + InitiatorRemarks + "</InitiatorRemarks>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String processDefId = ConfProperty.getCommonPropertyValue(AcceleratorConstants.RCUSUBPROCESSDEFID);
                    Log.consoleLog(ifr, "RCU processDefId  ====" + processDefId);
                    String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        Log.consoleLog(ifr, "RCU Sub process PID " + pid);
                        String query1 = ConfProperty.getQueryScript(AcceleratorConstants.RCUSUBPROCESSUPDATEPID).replaceAll("#pid#", pid).replaceAll("#PID#", PID).replaceAll("#insertionOrderID#", insertionOrderID);
                        Log.consoleLog(ifr, "query1" + query1);
                        ifr.saveDataInDB(query1);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All RCU are already Initiated!"));
                return message.toString();
            }
            message.put("refreshFrame", cf.refreshFrame("F_RCU_Details"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "RCU request has been created"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickRCUSubprocess method");
            Log.errorLog(ifr, "Exception in mAccClickRCUSubprocess method");
        }
        return "";
    }

    public String mAccClickFIInitiate(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickFISubProcess");
            JSONObject message = new JSONObject();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.FISUBPROCESSMAINQUERY).replaceAll("#PID#", PID);
            List<List<String>> queryResult1 = cf.mExecuteQuery(ifr, query, "Inside FI query");
            Log.consoleLog(ifr, "queryResult.size::" + queryResult1.size());
            if (queryResult1.size() > 0) {
                for (int i = 0; i < queryResult1.size(); i++) {
                    String addressOf = queryResult1.get(0).get(0);
                    String addressType = queryResult1.get(0).get(1);
                    String line1 = queryResult1.get(0).get(2);
                    String line2 = queryResult1.get(0).get(3);
                    String line3 = queryResult1.get(0).get(4);
                    String landmark = queryResult1.get(0).get(5);
                    String zipCode = queryResult1.get(0).get(6);
                    String areaZone = queryResult1.get(0).get(7);
                    String cityTownVillage = queryResult1.get(0).get(8);
                    String District = queryResult1.get(0).get(9);
                    String state = queryResult1.get(0).get(10);
                    String country = queryResult1.get(0).get(11);
                    String Agency = queryResult1.get(0).get(12);
                    String initiatedBy = queryResult1.get(0).get(13);
                    String intiatorRemarks = queryResult1.get(0).get(14);
                    String FiVerificationType = queryResult1.get(0).get(15);
                    String insertionOrderID = queryResult1.get(0).get(16);
                    String F_Key = queryResult1.get(0).get(17);
                    String attributes = "<ParentWINo>" + PID + "</ParentWINo>"
                            + "<ParentQueryID>" + insertionOrderID + "</ParentQueryID>"
                            + "<Q_ParentFIID>" + F_Key + "</Q_ParentFIID>"
                            + "<AddressOf>" + addressOf + "</AddressOf>"
                            + "<AddressType>" + addressType + "</AddressType>"
                            + "<Line1>" + line1 + "</Line1>"
                            + "<Line2>" + line2 + "</Line2>"
                            + "<Line3>" + line3 + "</Line3>"
                            + "<Landmark>" + landmark + "</Landmark>"
                            + "<ZIPCode>" + zipCode + "</ZIPCode>"
                            + "<Zone>" + areaZone + "</Zone>"
                            + "<CityTownVillage>" + cityTownVillage + "</CityTownVillage>"
                            + "<District>" + District + "</District>"
                            + "<State>" + state + "</State>"
                            + "<Country>" + country + "</Country>"
                            + "<Agency>" + Agency + "</Agency>"
                            + "<InitiatedBy>" + initiatedBy + "</InitiatedBy>"
                            + "<InitiatorRemark>" + intiatorRemarks + "</InitiatorRemark>"
                            + "<FIVerificationType>" + FiVerificationType + "</FIVerificationType>"
                            + "<FIVendorName>" + Agency + "</FIVendorName>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String processDefId = ConfProperty.getCommonPropertyValue(AcceleratorConstants.FISUBPROCESSDEFID);
                    Log.consoleLog(ifr, "FI processDefId  ====" + processDefId);
                    String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        Log.consoleLog(ifr, "RCU Sub process PID " + pid);
                        String query1 = ConfProperty.getQueryScript(AcceleratorConstants.FISUBPROCESSUPDATEPID).replaceAll("#pid#", pid).replaceAll("#PID#", PID).replaceAll("#insertionOrderID#", insertionOrderID);
                        Log.consoleLog(ifr, "query1" + query1);
                        ifr.saveDataInDB(query1);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All FI are already Initiated!"));
                return message.toString();
            }
            message.put("refreshFrame", cf.refreshFrame("F_FI_Details"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "FI request has been created"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickRCUSubprocess method");
            Log.errorLog(ifr, "Exception in mAccClickRCUSubprocess method");
        }
        return "";
    }

    public String mAccClickQueryInitiate(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickQueryInitiate===>");
            String Pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String Query = ConfProperty.getQueryScript(AcceleratorConstants.QUERYSUBPROCESSMAIN).replaceAll("#Pid#", Pid);
            List<List<String>> queryout = cf.mExecuteQuery(ifr, Query, "Inside BTN_query===>");
            JSONObject message = new JSONObject();
            if (queryout.size() > 0) {
                for (int i = 0; i < queryout.size(); i++) {
                    String Department = queryout.get(i).get(0);
                    String Applicant_Details = queryout.get(i).get(1);
                    String Query_Category = queryout.get(i).get(2);
                    String Query_SubCategory = queryout.get(i).get(3);
                    String Query_Description = queryout.get(i).get(4);
                    String Query_RaisedBy = queryout.get(i).get(5);
                    String Query_Status = "Raised";
                    String insertionorderid = queryout.get(i).get(8);
                    String F_key = queryout.get(i).get(9);

                    String attributes = "<Q_ParentID>" + Pid + "</Q_ParentID>"
                            + "<ParentWINo>" + Pid + "</ParentWINo>"
                            + "<Department>" + Department + "</Department>"
                            + "<ApplicantID>" + Applicant_Details + "</ApplicantID>"
                            + "<QueryCategory>" + Query_Category + "</QueryCategory>"
                            + "<QuerySubCategory>" + Query_SubCategory + "</QuerySubCategory>"
                            + "<QueryDescription>" + Query_Description + "</QueryDescription>"
                            + "<RaisedBy>" + Query_RaisedBy + "</RaisedBy>"
                            + "<ParentQueryID>" + insertionorderid + "</ParentQueryID>"
                            + "<Q_ParentQueryID>" + F_key + "</Q_ParentQueryID>"
                            + "<QueryStatus>" + Query_Status + "</QueryStatus>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String pid = ucwi.uploadWI(ifr, attributes, ConfProperty.getCommonPropertyValue(AcceleratorConstants.QUERYPROCESSDEFID), "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        String query = ConfProperty.getQueryScript(AcceleratorConstants.QUERYSUBPROCESSMAINUPDATE)
                                .replaceAll("#ChildWINo#", pid).replaceAll("#Pid#", Pid).replaceAll("#Query_Status#", Query_Status).replaceAll("#insertionorderid#", insertionorderid);
                        Log.consoleLog(ifr, "query  ====" + query);
                        int result = ifr.saveDataInDB(query);
                        Log.consoleLog(ifr, "result  ====" + result);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All Query are already Raised!"));
                return message.toString();
            }
            message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess created Successfully!"));
            message.put("refreshFrame", cf.refreshFrame("F_Query_Details"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In Query SubProcess:" + e);
            Log.errorLog(ifr, "Exception In Query SubProcess:" + e);
        }
        return "";
    }

    public void mAccChangeRCUGridForUserName(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_RCU_RCU_PID").toString().equalsIgnoreCase("")) {
            String User_name = ifr.getObjGeneralData().getM_strUserName();
            ifr.setValue("QNL_RCU_INITIATED_BY", User_name);
        }
    }

    public void mAccChangeFIGridForUserName(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_FI_DETAILS_ChildWINo").toString().equalsIgnoreCase("")) {
            String User_name = ifr.getObjGeneralData().getM_strUserName();
            ifr.setValue("QNL_FI_DETAILS_Initiated_By", User_name);
        }
    }

    public void mAccChangeLegalGridForUserName(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_Agency_Legal_Child_WI_No").toString().equalsIgnoreCase("")) {
            String User_name = ifr.getObjGeneralData().getM_strUserName();
            ifr.setValue("QNL_Agency_Legal_initiated_by", User_name);
        }
    }

    public void mAccChangePVGridForUserName(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_Property_Valuation_Valuation_WorkItem_Number").toString().equalsIgnoreCase("")) {
            String User_name = ifr.getObjGeneralData().getM_strUserName();
            ifr.setValue("QNL_Property_Valuation_Initiated_By", User_name);
        }
    }

    public void mAccChangeVVGridForUserName(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_Vehicle_Valuation_Child_WI_No").toString().equalsIgnoreCase("")) {
            String User_name = ifr.getObjGeneralData().getM_strUserName();
            ifr.setValue("QNL_Vehicle_Valuation_Initiated_By", User_name);
        }
    }

    public void mAccGetUser(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_QUERY_DETAILS_QueryWINo").toString().equalsIgnoreCase("")) {
            ifr.setValue("QNL_QUERY_DETAILS_QUERY_RAISED_BY", ifr.getUserName());
        }
    }

    public String mAccClickLegalSubProcess(IFormReference ifr, String control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickLegalSubProcess");
            JSONObject message = new JSONObject();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.LEGALSUBPROCESSMAINQUERY).replaceAll("#PID#", PID);
            List<List<String>> queryResult1 = cf.mExecuteQuery(ifr, query, "Inside FI query");
            Log.consoleLog(ifr, "queryResult.size::" + queryResult1.size());
            if (queryResult1.size() > 0) {
                for (int i = 0; i < queryResult1.size(); i++) {
                    String Branch = ifr.getValue("QL_LEAD_DET_BranchName").toString();
                    String agencyName = queryResult1.get(i).get(0);
                    String Initiated_By = queryResult1.get(i).get(1);
                    String insertionOrderID = queryResult1.get(i).get(2);
                    String attributes = "<ParentWINo>" + PID + "</ParentWINo>"
                            + "<ParentLegalID>" + insertionOrderID + "</ParentLegalID>"
                            + "<Branch>" + Branch + "</Branch>"
                            + "<Initiated_By>" + Initiated_By + "</Initiated_By>"
                            + "<AgencyName>" + agencyName + "</AgencyName>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String processDefId = ConfProperty.getCommonPropertyValue(AcceleratorConstants.LEGALSUBPROCESSDEFID);
                    Log.consoleLog(ifr, "FI processDefId  ====" + processDefId);
                    String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        Log.consoleLog(ifr, "Legal Sub process PID " + pid);
                        String query1 = ConfProperty.getQueryScript(AcceleratorConstants.LEGALSUBPROCESSUPDATEPID).replaceAll("#pid#", pid).replaceAll("#PID#", PID).replaceAll("#insertionOrderID#", insertionOrderID);
                        Log.consoleLog(ifr, "query1" + query1);
                        ifr.saveDataInDB(query1);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All Legal are already Initiated!"));
                return message.toString();
            }
            message.put("refreshFrame", cf.refreshFrame("F_Legal_Details"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Legal request has been created"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickLegalSubProcess method" + e);
            Log.errorLog(ifr, "Exception in mAccClickLegalSubProcess method" + e);
        }
        return "";
    }

    /* public void mAccChangeLeadSource(IFormReference ifr, String control, String Event, String value) {//Checked
     try {
     String leadSourceCode = ifr.getValue("QL_SOURCINGINFO_LeadSource").toString();
     if (leadSourceCode.equalsIgnoreCase("")) {
     ifr.setValue("QL_LEAD_DET_LeadSource", "");
     } else {
     String query = ConfProperty.getQueryScript(AcceleratorConstants.LEADSOURCEQUERY).replaceAll("#leadSourceCode#", leadSourceCode);
     List<List<String>> quertResult = cf.mExecuteQuery(ifr, query, "Lead source Query");
     if (quertResult.size() > 0) {
     ifr.setValue("QL_LEAD_DET_LeadSource", quertResult.get(0).get(0));
     ifr.setValue("QL_SOURCINGINFO_LeadSource", quertResult.get(0).get(0));
     }
     }
     } catch (Exception e) {
     Log.consoleLog(ifr, "Exception in mAccChangeLeadSource" + e);
     Log.errorLog(ifr, "Exception in mAccChangeLeadSource" + e);
     }
     }
     */
    public void mAccChangeLeadSource(IFormReference ifr, String control, String Event, String value) {//Checked
        try {
            String leadSourceCode = ifr.getValue("QL_SOURCINGINFO_LeadSource").toString();
            if (leadSourceCode.equalsIgnoreCase("")) {
                ifr.setValue("QL_LEAD_DET_LeadSource", "");
            } else {
                String query = "";
                String stageName = ifr.getActivityName();
                if (stageName.equalsIgnoreCase("Lead Capture")) {
                    query = ConfProperty.getQueryScript(AcceleratorConstants.LEADSOURCEQUERY).replaceAll("#leadSourceCode#", "WI");
                } else {
                    query = ConfProperty.getQueryScript(AcceleratorConstants.LEADSOURCEQUERY).replaceAll("#leadSourceCode#", leadSourceCode);
                }
                List<List<String>> quertResult = cf.mExecuteQuery(ifr, query, "Lead source Query");
                if (quertResult.size() > 0) {
                    ifr.setValue("QL_LEAD_DET_LeadSource", quertResult.get(0).get(0));
                    ifr.setValue("QL_SOURCINGINFO_LeadSource", quertResult.get(0).get(0));
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccChangeLeadSource" + e);
            Log.errorLog(ifr, "Exception in mAccChangeLeadSource" + e);
        }
    }

    public String mAccOnChangeDocumentCheckBox(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccOnChangeDocumentCheckBox Documents in checklist" + value);
            cm.mAddDocumentSubProcess(ifr, value, "ALV_QUERY_DETAILS_LV_QUERY_DOCUMENT", "QueryDocument");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccOnChangeDocumentCheckBox:" + e);
            Log.errorLog(ifr, "Exception In mAccOnChangeDocumentCheckBox:" + e);
        }
        return "";
    }

    public String mAccChangeRCUDocument(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccChangeRCUDocument Documents in checklist" + value);
            cm.mAddDocumentSubProcess(ifr, value, "ALV_RCU_LV_RCU_Document", "QueryDocument");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccChangeRCUDocument:" + e);
            Log.errorLog(ifr, "Exception In mAccChangeRCUDocument:" + e);
        }
        return "";
    }

    public String mAccChangeFIDocument(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccChangeFIDocument Documents in checklist" + value);
            cm.mAddDocumentSubProcess(ifr, value, "ALV_FI_DETAILS_LV_FI_Document", "QueryDocument");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccChangeFIDocument:" + e);
            Log.errorLog(ifr, "Exception In mAccChangeFIDocument:" + e);
        }
        return "";
    }

    public String mAccChangeFIAddressType(IFormReference ifr, String Control, String Event, String value) {//Checked
        if (ifr.getValue("QNL_FI_DETAILS_AddressOf").toString().equalsIgnoreCase("")) {
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "QNL_FI_DETAILS_AddressOf", "error", "Please Select Address Of"));
            return message.toString();
        }
        if (ifr.getValue("QNL_FI_DETAILS_AddressType").toString().equalsIgnoreCase("")) {
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "QNL_FI_DETAILS_AddressOf", "error", "Please Select Address Type"));
            return message.toString();
        }
        String query = ConfProperty.getQueryScript("FIMainAddressFetching")
                .replaceAll("#insertionOrderId#", ifr.getValue("QNL_FI_DETAILS_AddressOf").toString())
                .replaceAll("#AddressType#", ifr.getValue("QNL_FI_DETAILS_AddressType").toString())
                .replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
        boolean result = cf.mGetLabelAndSetToField(ifr, query, "", "QNL_FI_DETAILS_Line1,QNL_FI_DETAILS_Line2,QNL_FI_DETAILS_Line3,QNL_FI_DETAILS_Landmark,QNL_FI_DETAILS_ZipCode,QNL_FI_DETAILS_Area_Zone,QNL_FI_DETAILS_City_Town_Village,QNL_FI_DETAILS_District,QNL_FI_DETAILS_State,QNL_FI_DETAILS_Country");
        if (!result) {
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "QNL_FI_DETAILS_AddressOf", "error", "This Address Not Available against User"));
            return message.toString();
        }
        return "";
    }

    public String mAccClickPVInitiate(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside PropertyValution_btn===>");
            JSONObject message = new JSONObject();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.PVSUBPROCESSMAINQUERY).replaceAll("#PID#", PID);
            List<List<String>> queryResult1 = cf.mExecuteQuery(ifr, query, "Inside FI query");
            Log.consoleLog(ifr, "queryResult.size::" + queryResult1.size());
            if (queryResult1.size() > 0) {
                for (int i = 0; i < queryResult1.size(); i++) {
                    String Collateral_ID = queryResult1.get(i).get(0);
                    String Agency = queryResult1.get(i).get(1);
                    String Collateral_Address = queryResult1.get(i).get(2);
                    String Remarks = queryResult1.get(i).get(3);
                    String insertionOrderID = queryResult1.get(i).get(4);
                    String user = ifr.getUserName();
                    Log.consoleLog(ifr, "attributes  ====" + user);
                    String attributes = "<ParentWINo>" + PID + "</ParentWINo>"
                            + "<ParentPVID>" + insertionOrderID + "</ParentPVID>"
                            + "<Agency>" + Agency + "</Agency>"
                            + "<Collateral_ID>" + Collateral_ID + "</Collateral_ID>"
                            + "<initiatedby>" + user + "</initiatedby>"
                            + "<Remarks>" + Remarks + "</Remarks>"
                            + "<collateralAddress>" + Collateral_Address + "</collateralAddress>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String processDefId = ConfProperty.getCommonPropertyValue(AcceleratorConstants.PVSUBPROCESSDEFID);
                    Log.consoleLog(ifr, "FI processDefId  ====" + processDefId);
                    String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        Log.consoleLog(ifr, "Legal Sub process PID " + pid);
                        String query1 = ConfProperty.getQueryScript(AcceleratorConstants.PVSUBPROCESSUPDATEPID).replaceAll("#pid#", pid).replaceAll("#PID#", PID).replaceAll("#insertionOrderID#", insertionOrderID);
                        Log.consoleLog(ifr, "query1" + query1);
                        ifr.saveDataInDB(query1);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All Property Valuation are already Initiated!"));
                return message.toString();
            }
            message.put("refreshFrame", cf.refreshFrame("F_PropertyValuation"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Property Valuation request has been created"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccClickPVInitiate SubProcess:" + e);
            Log.errorLog(ifr, "Exception In mAccClickPVInitiate SubProcess:" + e);
        }
        return "";
    }

    public String mAccClickVVInitiate(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccClickVVInitiate===>");
            JSONObject message = new JSONObject();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.VVSUBPROCESSMAINQUERY).replaceAll("#PID#", PID);
            List<List<String>> queryResult1 = cf.mExecuteQuery(ifr, query, "Inside FI query");
            Log.consoleLog(ifr, "queryResult.size::" + queryResult1.size());
            if (queryResult1.size() > 0) {
                for (int i = 0; i < queryResult1.size(); i++) {
                    String user = ifr.getUserName();
                    String agency = queryResult1.get(i).get(7);
                    String insertionOrderID = queryResult1.get(i).get(9);
                    String attributes = "<ParentWINo>" + PID + "</ParentWINo>"
                            + "<ParentVVID>" + insertionOrderID + "</ParentVVID>"
                            + "<AgencyName>" + agency + "</AgencyName>";
                    Log.consoleLog(ifr, "attributes  ====" + attributes);
                    UploadCreateWI ucwi = new UploadCreateWI();
                    String processDefId = ConfProperty.getCommonPropertyValue(AcceleratorConstants.VVSUBPROCESSDEFID);
                    Log.consoleLog(ifr, "FI processDefId  ====" + processDefId);
                    String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                    if (pid.isEmpty()) {
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                        return message.toString();
                    } else {
                        Log.consoleLog(ifr, "Vehicle valuation Sub process PID " + pid);
                        String query1 = ConfProperty.getQueryScript(AcceleratorConstants.VVSUBPROCESSUPDATEPID).replaceAll("#pid#", pid).replaceAll("#PID#", PID).replaceAll("#insertionOrderID#", insertionOrderID);
                        Log.consoleLog(ifr, "query1" + query1);
                        ifr.saveDataInDB(query1);
                    }
                }
            } else {
                message.put("showMessage", cf.showMessage(ifr, "", "error", "All Vehicle Valuation are already Initiated!"));
                return message.toString();
            }
            message.put("refreshFrame", cf.refreshFrame("F_VehileValuation"));
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Vehicle Valuation request has been created"));
            return message.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccClickVVInitiate SubProcess:" + e);
            Log.errorLog(ifr, "Exception In mAccClickVVInitiate SubProcess:" + e);
        }
        return "";
    }

    public String mAccChangeFeeType(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccChangeFeeType:" + Control);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("FeeChargesProductFetching").replaceAll("#PID#", PID)
                    .replaceAll("#ProductType#", ifr.getValue("QNL_FEE_CHARGES_ProductType").toString())
                    .replaceAll("#Fees_Description#", ifr.getValue("QNL_FEE_CHARGES_Fees_Description").toString())
                    .replaceAll("#Fees_Type#", ifr.getValue("QNL_FEE_CHARGES_Fees_Type").toString());
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
            if (result.size() > 0) {
                if (ifr.getValue("QNL_FEE_CHARGES_Fees_Type").toString().equalsIgnoreCase("Flat")) {
                    ifr.setValue("QNL_FEE_CHARGES_Amount", result.get(0).get(0));
                } else if (ifr.getValue("QNL_FEE_CHARGES_Fees_Type").toString().equalsIgnoreCase("Percentage")) {
                    BigDecimal per = new BigDecimal(result.get(0).get(1).equalsIgnoreCase("") ? "0" : result.get(0).get(1));
                    Log.consoleLog(ifr, "percentage Amount:" + per);
                    BigDecimal loanAmt = new BigDecimal(result.get(0).get(4).equalsIgnoreCase("") ? "0" : result.get(0).get(4));
                    Log.consoleLog(ifr, "loanAmt:" + loanAmt);
                    BigDecimal total = (loanAmt.multiply(per)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    Log.consoleLog(ifr, "total Amount:" + total);
                    ifr.setValue("QNL_FEE_CHARGES_Amount", String.valueOf(total));
                }
                ifr.setValue("QNL_FEE_CHARGES_GST", result.get(0).get(2));
                ifr.setValue("QNL_FEE_CHARGES_FeeCode", result.get(0).get(3));
                cm.setFeeChargesCalculation(ifr);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccChangeFeeType:" + e);
            Log.errorLog(ifr, "Exception In mAccChangeFeeType:" + e);
        }
        return "";
    }

    public String mAccChangeConcessionCalculation(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccChangeConcessionCalculation:" + Control);
            if (ifr.getValue("QNL_FEE_CHARGES_Fees_Type").toString().equalsIgnoreCase("")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select Fees Type"));
                return message.toString();
            }
            cm.setFeeChargesCalculation(ifr);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccChangeConcessionCalculation:" + e);
            Log.errorLog(ifr, "Exception In mAccChangeConcessionCalculation:" + e);
        }
        return "";
    }

    public String mAccListViewClearCombo(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccListViewClearCombo:");
        try {
            cf.mSetClearComboValue(ifr, "QNL_FEE_CHARGES_Fees_Description,QNL_FEE_CHARGES_Fees_Type,"
                    + "QNL_FEE_CHARGES_FeeCode,QNL_FEE_CHARGES_Amount,QNL_FEE_CHARGES_Amount,"
                    + "QNL_FEE_CHARGES_ConcessionAmt,QNL_FEE_CHARGES_GST,QNL_FEE_CHARGES_fee_amount,QNL_FEE_CHARGES_Total");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccListViewClearCombo:" + e);
            Log.errorLog(ifr, "Exception In mAccListViewClearCombo:" + e);
        }
        return "";
    }

    public String mAccChangeOutstandingAmount(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccChangeOutstandingAmount");
        return cm.mCommonChangeOutstandingAmount(ifr, "Fee_Charges_OutstandingCheck",
                "QA_FI_FEECHARGE_COLL_OustandingAmount", "QA_FI_FEECHARGE_COLL_TotalAmount");
    }

    public String mAcclistViewLoadFeeChargeCollection(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAcclistViewLoadFeeChargeCollection");
        return cm.mCommonlistViewLoadCollection(ifr, "TotalChargesAmountQuery", "QA_FI_FEECHARGE_COLL_TotalChargeAmount",
                "ALV_FEE_CHARGE_COLLECT", "QA_FI_FEECHARGE_COLL_TotalAmount", "QA_FI_FEECHARGE_COLL_OustandingAmount", "Fee_Charges_OutstandingCheck");
    }

    public String mAccChangeInsuranceOutstandingAmount(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccChangeInsuranceOutstandingAmount");
        return cm.mCommonChangeOutstandingAmount(ifr, "Insurance_OutstandingCheck",
                "QA_INSURANCE_COLLECT_OutstandingAmount", "QA_INSURANCE_COLLECT_PremiumAmount");
    }

    public String mAcclistViewLoadInsuranceCollection(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAcclistViewLoadInsuranceCollection");
        return cm.mCommonlistViewLoadCollection(ifr, "InsuranceTotalPremium", "Insurance_TotalpremiumAmount",
                "ALV_A_INSURANCE_COLLECT", "QA_INSURANCE_COLLECT_PremiumAmount", "QA_INSURANCE_COLLECT_OutstandingAmount", "Insurance_OutstandingCheck");
    }

    public String mAccChangeLiabAmt(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mAccChangeLiabAmt:");
        String amt = ifr.getValue("QNL_AL_LIAB_VAL_Loan_LiabAmt").toString();
        String outamt = ifr.getValue("QNL_AL_LIAB_VAL_Loan_LiabOut").toString();
        BigDecimal loanAmt = new BigDecimal(amt.equalsIgnoreCase("") ? "0" : amt);
        BigDecimal OutstandingAmt = new BigDecimal(outamt.equalsIgnoreCase("") ? "0" : outamt);
        if (OutstandingAmt.compareTo(loanAmt) >= 0) {
            ifr.setValue("QNL_AL_LIAB_VAL_Loan_LiabAmt", "");
            ifr.setValue("QNL_AL_LIAB_VAL_Loan_LiabOut", "");
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Outstanding Amount can not be more than Loan Amount"));
            return message.toString();
        }
        return "";
    }

    public String mAccAddModifyDeleteNetworth(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mAccAddModifyDeleteNetworth code ");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String query = ConfProperty.getQueryScript("FinancialInfoLiability").replaceAll("#PID#", PID);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "FinancialInfoLiability Query:");
            HashMap liab = new HashMap();
            for (List<String> result1 : result) {
                liab.put(result1.get(0), result1.get(1));
            }
            query = ConfProperty.getQueryScript("FinancialInfoAsset").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "FinancialInfoAsset Query:");
            HashMap asset = new HashMap();
            for (List<String> result1 : result) {
                asset.put(result1.get(0), result1.get(1));
            }
            query = ConfProperty.getQueryScript("BASICINFO").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, query, "BASICINFO Query:");
            ifr.clearTable("ALV_AL_NETWORTH");
            JSONArray NetworthGrid = new JSONArray();
            for (int i = 0; i < result.size(); i++) {
                String Applicanttype = result.get(i).get(0);
                JSONObject row = new JSONObject();
                row.put("QNL_AL_NETWORTH_ApplicantType", Applicanttype);
                String assetValue = String.valueOf(asset.get(Applicanttype));
                if (assetValue.equalsIgnoreCase("") || assetValue.equalsIgnoreCase("null")) {
                    assetValue = "0";
                }
                row.put("QNL_AL_NETWORTH_TotAssetVal", assetValue);
                String liabValue = String.valueOf(liab.get(Applicanttype));
                if (liabValue.equalsIgnoreCase("") || liabValue.equalsIgnoreCase("null")) {
                    liabValue = "0";
                }
                row.put("QNL_AL_NETWORTH_TotLiab", liabValue);
                BigDecimal total = new BigDecimal(assetValue).subtract(new BigDecimal(liabValue));
                row.put("QNL_AL_NETWORTH_TotOutstanding", String.valueOf(total));
                NetworthGrid.add(row);
            }
            Log.consoleLog(ifr, "NetworthGrid:" + NetworthGrid);
            ifr.addDataToGrid("ALV_AL_NETWORTH", NetworthGrid);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccAddModifyDeleteNetworth:" + e);
            Log.errorLog(ifr, "Exception in mAccAddModifyDeleteNetworth:" + e);
        }
        return "";
    }

    public void mAccOnChangeSectionStateInsuranceDetails(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.INSURANCEROITENURE).replaceAll("#PID#", PID);
            List<List<String>> queryResult = cf.mExecuteQuery(ifr, query, "Insurance ROI & Tenure");
            if (queryResult.size() > 0) {
                ifr.setValue("QL_INSURANCE_SUMMARY_InsuranceLoanROI", queryResult.get(0).get(0));
                ifr.setValue("QL_INSURANCE_SUMMARY_InsuranceLoanTenure", queryResult.get(0).get(1));
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccOnChangeSectionStateInsuranceDetails:" + e);
            Log.errorLog(ifr, "Exception In mAccOnChangeSectionStateInsuranceDetails:" + e);
        }
    }

    public void mAccAddModifyDeletePosthookGeneralInsuranceGrid(IFormReference ifr, String Control, String Event, String value) {//Checked
        try {
            BigDecimal PremimumAmt = new BigDecimal("0");
            BigDecimal PremimumAmtFunded = new BigDecimal("0");
            int size = cf.getGridCount(ifr, "ALV_A_GENERAL_INSURANCE");
            Log.consoleLog(ifr, "grid count" + size);
            for (int i = 0; i < size; i++) {
                String InsurancePremimumAmt = ifr.getTableCellValue("ALV_A_GENERAL_INSURANCE", i, "QA_GENERAL_INSURANCE_PremiumAmount").toString();
                BigDecimal PremimumAmt1 = new BigDecimal(InsurancePremimumAmt.equalsIgnoreCase("") ? "0" : InsurancePremimumAmt);
                PremimumAmt = PremimumAmt.add(PremimumAmt1);
                if (ifr.getTableCellValue("ALV_A_GENERAL_INSURANCE", i, "QA_GENERAL_INSURANCE_Funded").toString().equalsIgnoreCase("Yes")) {
                    String InsurancePremimumAmtFunded = ifr.getTableCellValue("ALV_A_GENERAL_INSURANCE", i, "QA_GENERAL_INSURANCE_PremiumAmount").toString();
                    BigDecimal PremimumAmtFunded1 = new BigDecimal(InsurancePremimumAmtFunded.equalsIgnoreCase("") ? "0" : InsurancePremimumAmtFunded);
                    PremimumAmtFunded = PremimumAmtFunded.add(PremimumAmtFunded1);
                }
            }
            ifr.setValue("QL_INSURANCE_SUMMARY_TotalInsPremium", String.valueOf(PremimumAmt));
            ifr.setValue("QL_INSURANCE_SUMMARY_TotalInsuPremiumFunded", String.valueOf(PremimumAmtFunded));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccAddModifyDeletePosthookGeneralInsuranceGrid:" + e);
            Log.errorLog(ifr, "Exception In mAccAddModifyDeletePosthookGeneralInsuranceGrid:" + e);
        }
    }

    public String mAccCalculateTDSR(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mCalculateTDSR");
        ifr.setValue("Q_SUMMARY_total_asset", cm.mCommonValueFromTable(ifr, "FinancialInfoTotalAsset"));
        ifr.setValue("Q_SUMMARY_total_liability", cm.mCommonValueFromTable(ifr, "FinancialInfoTotalLiability"));
        ifr.setValue("Q_SUMMARY_total_consideredLiability", cm.mCommonValueFromTable(ifr, "FinancialInfoConditionalLiability"));
        ifr.setValue("Q_SUMMARY_total_cashOutFlow", cm.mCommonValueFromTable(ifr, "FinancialInfoExpense"));
        ifr.setValue("Q_SUMMARY_total_cashinFlow", cm.mCommonValueFromTable(ifr, "FinancialInfoIncome"));
        ifr.setValue("Q_SUMMARY_toal_considered_income", cm.mCommonValueFromTable(ifr, "FinancialInfoConditionalIncome"));
        String ConsLiab = ifr.getValue("Q_SUMMARY_total_consideredLiability").toString();
        String ProposedValue = cm.mCommonValueFromTable(ifr, "NetIncomeProposed");
        String ConsInc = ifr.getValue("Q_SUMMARY_toal_considered_income").toString();
        BigDecimal totalConsLiab = new BigDecimal(ConsLiab.equalsIgnoreCase("") ? "0" : ConsLiab);
        BigDecimal totalConsInc = new BigDecimal(ConsInc.equalsIgnoreCase("") ? "0" : ConsInc);
        BigDecimal totalProposedValue = new BigDecimal(ConsInc.equalsIgnoreCase("") ? "0" : ProposedValue);
        if (totalConsInc.compareTo(BigDecimal.ZERO) == 0) {
            ifr.setValue("Q_SUMMARY_tdsr", "");
        } else {

            BigDecimal tdsr = ((totalConsLiab.add(totalProposedValue)).divide(totalConsInc, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal("100"));
            Log.consoleLog(ifr, "TDSR=======>" + tdsr);
            ifr.setValue("Q_SUMMARY_tdsr", String.valueOf(tdsr));
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_FinancialInfo_Calculate", "", "Summary Calculated"));
        return returnJSON.toString();
    }

    public void mAccClickFetchFeeCharges(IFormReference ifr, String Control, String Event, String value) {
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(AcceleratorConstants.FETCHFEECHARGES).replaceAll("#PID#", PID);
            List<List<String>> queryResult = cf.mExecuteQuery(ifr, query, "FETCHFEECHARGES All Data:");
            if (queryResult.size() > 0) {
                ifr.clearTable("ALV_FEE_CHARGES");
                JSONArray gridArray = new JSONArray();
                Log.consoleLog(ifr, "Result size:::" + queryResult.size());
                for (int i = 0; i < queryResult.size(); i++) {
                    BigDecimal Amount = new BigDecimal("0");
                    JSONObject gridObject = new JSONObject();
                    gridObject.put("QNL_FEE_CHARGES_ProductType", queryResult.get(i).get(0));
                    gridObject.put("QNL_FEE_CHARGES_Fees_Description", queryResult.get(i).get(1));
                    gridObject.put("QNL_FEE_CHARGES_Fees_Type", queryResult.get(i).get(2));
                    gridObject.put("QNL_FEE_CHARGES_GST", queryResult.get(i).get(6));
                    gridObject.put("QNL_FEE_CHARGES_FeeCode", queryResult.get(i).get(7));
                    gridObject.put("QNL_FEE_CHARGES_Fees_Appropriation", queryResult.get(i).get(10));
                    if (queryResult.get(i).get(2).equalsIgnoreCase("Flat")) {
                        Amount = new BigDecimal(queryResult.get(i).get(3));
                    } else if (queryResult.get(i).get(2).equalsIgnoreCase("Percentage")) {
                        BigDecimal per = new BigDecimal(queryResult.get(i).get(5).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(5));
                        BigDecimal loanAmt = new BigDecimal(queryResult.get(i).get(4).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(4));
                        Amount = (loanAmt.multiply(per)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    }
                    gridObject.put("QNL_FEE_CHARGES_Amount", String.valueOf(Amount));
                    gridObject.put("QNL_FEE_CHARGES_ConcessionAmt", String.valueOf(Amount));
                    BigDecimal GSTAmt = new BigDecimal(queryResult.get(i).get(6).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(6));
                    BigDecimal Gsttotal = (Amount.multiply(GSTAmt)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal total = Amount.add(Gsttotal);
                    gridObject.put("QNL_FEE_CHARGES_fee_amount", String.valueOf(total));
                    BigDecimal MinAmount = new BigDecimal(queryResult.get(i).get(8).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(8));
                    BigDecimal MaxAmount = new BigDecimal(queryResult.get(i).get(9).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(9));
                    if (total.compareTo(MaxAmount) > 0) {
                        gridObject.put("QNL_FEE_CHARGES_Total", String.valueOf(MaxAmount));
                    } else if (total.compareTo(MinAmount) < 0) {
                        gridObject.put("QNL_FEE_CHARGES_Total", String.valueOf(MinAmount));
                    } else {
                        gridObject.put("QNL_FEE_CHARGES_Total", String.valueOf(total));
                    }
                    gridArray.add(gridObject);
                    Log.consoleLog(ifr, "JSONArray" + gridArray);
                }
                Log.consoleLog(ifr, "JSONArray1" + gridArray);
                ifr.addDataToGrid("ALV_FEE_CHARGES", gridArray);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAccClickFetchFeeCharges:" + e);
            Log.errorLog(ifr, "Exception In mAccClickFetchFeeCharges:" + e);
        }
    }

    //========================================Above Code Checked by Mayank Gupta========================================//
    public String mScorecarddata(IFormReference ifr, String Control, String Event, String value) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "Inside Scorecard BRMS Data Populate");

        String Query = "";

        Query = ConfProperty.getQueryScript("populate_all_data").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "Query= " + Query);
        List<List<String>> fetchAmount = cf.mExecuteQuery(ifr, Query, "First Query");
        Log.consoleLog(ifr, "Query" + fetchAmount);
        if (fetchAmount.size() > 0) {
            ifr.setValue("QL_RISK_RATING_EmployType", fetchAmount.get(0).get(0));
            ifr.setValue("QL_RISK_RATING_WorkExpnce", fetchAmount.get(0).get(1));
            ifr.setValue("QL_RISK_RATING_ResidenceType", fetchAmount.get(0).get(2));
            ifr.setValue("QL_RISK_RATING_CreditScore", fetchAmount.get(0).get(3));
            ifr.setValue("QL_RISK_RATING_MonAvgDisIncome", fetchAmount.get(0).get(4));
        }
        /* else {
         JSONObject message = new JSONObject();
         message.put("showMessage", cf.showMessage(ifr, "", "error", "Please fill Employee details"));
         return message.toString();
         }*/

        Query = ConfProperty.getQueryScript("populate_all_datas").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "Query2= " + Query);
        fetchAmount = cf.mExecuteQuery(ifr, Query, "Second Query");
        Log.consoleLog(ifr, "Query3" + fetchAmount);
        if (fetchAmount.size() > 0) {
            ifr.setValue("QL_RISK_RATING_LTV", fetchAmount.get(0).get(0));
        }
        /*  else {
         JSONObject message = new JSONObject();
         message.put("showMessage", cf.showMessage(ifr, "", "error", "Please fill LTV"));
         return message.toString();
         }*/
        Query = ConfProperty.getQueryScript("populate_all_score").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "Quer3= " + Query);
        fetchAmount = cf.mExecuteQuery(ifr, Query, "Third Query");
        Log.consoleLog(ifr, "Query3" + fetchAmount);
        if (fetchAmount.size() > 0) {
            ifr.setValue("QL_RISK_RATING_AcadQualify", fetchAmount.get(0).get(0));
            ifr.setValue("QL_RISK_RATING_MaritalStatus", fetchAmount.get(0).get(1));
            ifr.setValue("QL_RISK_RATING_ApplicantAge", fetchAmount.get(0).get(2));
            ifr.setValue("QL_RISK_RATING_NoOfDependents", fetchAmount.get(0).get(3));

        }
        /*   else {
         JSONObject message = new JSONObject();
         message.put("showMessage", cf.showMessage(ifr, "", "error", "Please fill Personal details"));
         return message.toString();
         }*/
        Query = ConfProperty.getQueryScript("populate_all_scores").replaceAll("#PID#", PID);
        Log.consoleLog(ifr, "Quer4= " + Query);
        fetchAmount = cf.mExecuteQuery(ifr, Query, "Fourth Query");
        Log.consoleLog(ifr, "Query4" + fetchAmount);
        if (fetchAmount.size() > 0) {
            ifr.setValue("QL_RISK_RATING_BankRelnship", fetchAmount.get(0).get(0));

            BigDecimal dbr = new BigDecimal("0");
            dbr = new BigDecimal(fetchAmount.get(0).get(1)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            ifr.setValue("QL_RISK_RATING_FOIR", String.valueOf(dbr));
        }
        /*   else {
         JSONObject message = new JSONObject();
         message.put("showMessage", cf.showMessage(ifr, "", "error", "Please fill Foir"));
         return message.toString();
         }*/
        return "";
    }

    public String mScorecardBRMS(IFormReference ifr, String Control, String Event, String value) {//Working For ScoreCard BRMS
        Log.consoleLog(ifr, "Inside Scorecard BRMS");
        try {
            String acadQualify = ifr.getValue("QL_RISK_RATING_AcadQualify").toString();
            String income = ifr.getValue("QL_RISK_RATING_MonAvgDisIncome").toString();
            String applicantAge = ifr.getValue("QL_RISK_RATING_ApplicantAge").toString();
            String creditScore = ifr.getValue("QL_RISK_RATING_CreditScore").toString();
            String foir = ifr.getValue("QL_RISK_RATING_FOIR").toString();
            String employType = ifr.getValue("QL_RISK_RATING_EmployType").toString();
            String ltv = ifr.getValue("QL_RISK_RATING_LTV").toString();
            String noOfDependents = ifr.getValue("QL_RISK_RATING_NoOfDependents").toString();
            String bankRelnship = ifr.getValue("QL_RISK_RATING_BankRelnship").toString();
            String residenceType = ifr.getValue("QL_RISK_RATING_ResidenceType").toString();
            String workExpnce = ifr.getValue("QL_RISK_RATING_WorkExpnce").toString();
            String maritalStatus = ifr.getValue("QL_RISK_RATING_MaritalStatus").toString();

            if (acadQualify.equalsIgnoreCase("") || income.equalsIgnoreCase("") || applicantAge.equalsIgnoreCase("") || creditScore.equalsIgnoreCase("") || foir.equalsIgnoreCase("") || employType.equalsIgnoreCase("") || ltv.equalsIgnoreCase("") || noOfDependents.equalsIgnoreCase("") || bankRelnship.equalsIgnoreCase("") || residenceType.equalsIgnoreCase("") || workExpnce.equalsIgnoreCase("") || maritalStatus.equalsIgnoreCase("")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter all details "));
                return message.toString();
            } else {
                String messageValue = "";
                JSONObject result = cf.executeBRMSRule(ifr, "ScoreCardBRMSRuleFlow", acadQualify + "," + income + "," + applicantAge + "," + creditScore + "," + foir + "," + employType + "," + ltv + "," + noOfDependents + "," + bankRelnship + "," + residenceType + "," + workExpnce + "," + maritalStatus);
                Log.consoleLog(ifr, "Result of Scorecard BRMS = " + result);
                if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) { //Fetching From JSON
                    String Bankrelation = cf.getJsonValue(result, "sc_op_rela_with_bank");
                    String Maritalstatus = cf.getJsonValue(result, "sc_op_marital_status");
                    String Depen = cf.getJsonValue(result, "sc_op_no_dependedant");
                    String DBR = cf.getJsonValue(result, "sc_op_dbr");
                    String Score = cf.getJsonValue(result, "sc_op_credit_score");
                    String Emptype = cf.getJsonValue(result, "sc_ip_emptype_op");
                    String Workexp = cf.getJsonValue(result, "sc_op_workexp");
                    String Age = cf.getJsonValue(result, "sc_opapplicant_age_");
                    String LTV = cf.getJsonValue(result, "sc_op_tvl");
                    String Education = cf.getJsonValue(result, "aq_op");
                    String Income = cf.getJsonValue(result, "sc_op_app_incm");
                    String Residence = cf.getJsonValue(result, "sc_residancetype");

                    //Data Parsing
                    int bank = Integer.parseInt(Bankrelation);
                    int marit = Integer.parseInt(Maritalstatus);
                    int dependent = Integer.parseInt(Depen);
                    int dbr = Integer.parseInt(DBR);
                    int credit = Integer.parseInt(Score);
                    int emptype = Integer.parseInt(Emptype);
                    int exp = Integer.parseInt(Workexp);
                    int age = Integer.parseInt(Age);
                    int ltvs = Integer.parseInt(LTV);
                    int edu = Integer.parseInt(Education);
                    int inc = Integer.parseInt(Income);
                    int residence = Integer.parseInt(Residence);

                    int totalScore = bank + marit + dependent + dbr + credit + emptype + exp + age + ltvs + edu + inc + residence;
                    Log.consoleLog(ifr, "Total score = " + totalScore);

                    JSONObject message = new JSONObject();
                    ifr.setValue("QL_RISK_RATING_RiskScore", String.valueOf(totalScore));
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Total Score =" + totalScore));
                    return message.toString();

                } else {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Provide Valid Inputs"));
                    return message.toString();
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception Inside Scorecard BRMS" + e);
        }
        return "";

    }

    public void mAccClickPermanentAddress(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mAccClickPermanentAddress Method");
        JSONArray AddressDetailsGrid = ifr.getDataFromGrid("LV_ADDRESS");
        for (int i = 0; i < AddressDetailsGrid.size(); i++) {
            JSONObject AddressDetailsGridData = (JSONObject) AddressDetailsGrid.get(i);
            Log.consoleLog(ifr, "AddressDetailsGridData:: " + AddressDetailsGridData.toString());
            String AddressType = AddressDetailsGridData.get("Address Type").toString();
            Log.consoleLog(ifr, "AddressType= " + AddressType);
            if (AddressType.equalsIgnoreCase("Permanent")) {
                String line1 = AddressDetailsGridData.get("Line 1").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line1", line1);
                String line2 = AddressDetailsGridData.get("Line 2").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line2", line2);
                String Line3 = AddressDetailsGridData.get("Line 3").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line3", Line3);
                String LandMarks = AddressDetailsGridData.get("Landmark").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark", LandMarks);
                String ZipCode = AddressDetailsGridData.get("PIN Code").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_PinCode", ZipCode);
                String Area_Zone = AddressDetailsGridData.get("Area").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone", Area_Zone);
                String Citys = AddressDetailsGridData.get("City").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_City_Town_Village", Citys);
                String Districts = AddressDetailsGridData.get("District").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_District", Districts);
                String Countrys = AddressDetailsGridData.get("Country").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country", Countrys);
                String States = AddressDetailsGridData.get("State").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_State", States);
                String Ownerships = AddressDetailsGridData.get("Ownership").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", Ownerships);
            }
        }
    }

    public void mAccPopulateDeviation(IFormReference ifr, String Control, String Event, String JSdata) {

        Log.consoleLog(ifr, "Inside mAccPopulateDeviation ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String query = ConfProperty.getQueryScript("DEV_IP").replaceAll("#PID#", PID);
        List<List<String>> tabledata = cf.mExecuteQuery(ifr, query, "JOIN Query");
        Log.consoleLog(ifr, "Table Values:: " + tabledata);

        String facilityType = "Fund Based";
        String product = tabledata.get(0).get(0);
        String subproduct = tabledata.get(0).get(1);
        String loanPurpose = tabledata.get(0).get(2);
        float dbr = Float.parseFloat(tabledata.get(0).get(3));

        JSONObject rule = cf.executeBRMSRule(ifr, "Deviation_FOIR", dbr + "," + facilityType + "," + product + "," + loanPurpose + "," + subproduct);
        Log.consoleLog(ifr, "Rule O/P::" + rule.toString());
        String DevID = rule.get("deviation_type_op").toString();
        Log.consoleLog(ifr, "DEVIATION ID::" + DevID);

        String query1 = ConfProperty.getQueryScript("DEV_OP").replaceAll("#DevID#", DevID);
        List<List<String>> tabledata1 = cf.mExecuteQuery(ifr, query1, "DEV OP");
        Log.consoleLog(ifr, "Deviation Values:: " + tabledata1);
        String DevCategory = tabledata1.get(0).get(0);
        String DevDesc = tabledata1.get(0).get(1);
        String DevAuthority = rule.get("deviation_level_op").toString();
        String DevRaisedtype = tabledata1.get(0).get(2);
        String DevStatus = rule.get("deviation1").toString();

        JSONArray DeviationGrid = new JSONArray();
        JSONObject row = new JSONObject();
        row.put("QNL_DEVIATIONS_DeviationID", DevID);
        row.put("QNL_DEVIATIONS_DeviationLevel", DevAuthority);
        row.put("QNL_DEVIATIONS_DeviationCategory", DevCategory);
        row.put("QNL_DEVIATIONS_DeviationDescription", DevDesc);
        row.put("QNL_DEVIATIONS_DeviationType", DevRaisedtype);
        row.put("QNL_DEVIATIONS_DeviationStatus", DevStatus);
        DeviationGrid.add(row);
        int count = ifr.getDataFromGrid("ALV_Deviations").size();
        int[] gridsize = new int[count];

        ifr.deleteRowsFromGrid("ALV_Deviations", gridsize);
        ifr.addDataToGrid("ALV_Deviations", DeviationGrid);
        Log.consoleLog(ifr, "Data added to Grid:: " + DeviationGrid.toString());

        if (DevStatus.equalsIgnoreCase("Raised")) {
            Log.consoleLog(ifr, "Inside Raised condition: ");
            ifr.clearCombo("DecisionValue");
            String raisedQuery = ConfProperty.getQueryScript("RaisedDecision");
            List<List<String>> tabledata2 = cf.mExecuteQuery(ifr, raisedQuery, "Decision");
            Log.consoleLog(ifr, "Query Data:: " + tabledata2.toString());
            String Decision = tabledata2.get(0).get(0);
            String DecisionValue = tabledata2.get(0).get(1);
            ifr.addItemInCombo("DecisionValue", Decision, DecisionValue);
        }

    }

    public void mAccPopulateTandC(IFormReference ifr, String Control, String Event, String JSdata) {//Working For Advanced List View 
        try {
            int size = ifr.getDataFromGrid("ALV_TERMS_AND_CONDITIONS").size();
            if (size == 0) {
                Log.consoleLog(ifr, "Inside Terms and Condition: ");
                String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
                Log.consoleLog(ifr, "PID value: " + PID);
                String schemeid = ConfProperty.getQueryScript("mgetschemeID").replaceAll("#PID#", PID);
                List<List<String>> SchemeID = cf.mExecuteQuery(ifr, schemeid, "Scheme");
                if (!SchemeID.isEmpty()) {
                    String Scheme = SchemeID.get(0).get(0);
                    String query = ConfProperty.getQueryScript("mAccpopulateTandC").replaceAll("#Scheme#", Scheme);

                    //   cf.addToTable(ifr, "ALV_TERMS_AND_CONDITIONS", query, "QNL_TERMS_AND_CONDITIONS_Termsandconditions~QNL_TERMS_AND_CONDITIONS_Code");
                    cf.addToTable(ifr, "ALV_TERMS_AND_CONDITIONS", query, "Terms and Conditions~Code");
                    ifr.setColumnDisable("ALV_TERMS_AND_CONDITIONS", "0", true);
                    if (ifr.getActivityName().equalsIgnoreCase(ConfProperty.getCommonPropertyValue("CreditAppraisalBranch"))
                            || ifr.getActivityName().equalsIgnoreCase(ConfProperty.getCommonPropertyValue("CreditAppraisalBranchCode"))) {
                        ifr.setColumnDisable("ALV_TERMS_AND_CONDITIONS", "3", true);
                        ifr.setColumnDisable("ALV_TERMS_AND_CONDITIONS", "4", true);
                    }
                }

            } else {
                Log.consoleLog(ifr, "Rows are already there in the grid ");

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  Terms and Condition: " + e);
        }
    }

    public void mImplLoadSetLeadId(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        Log.consoleLog(ifr, "Inside mImplLoadSetLeadId:");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String LeadId = PID + "-LLMS";
        ifr.setValue("QL_SOURCINGINFO_LeadID", LeadId);
        ifr.setValue("QL_LEAD_DET_LMSID", LeadId);
    }

    public String mImplChangeLoanAmount(IFormReference ifr, String Control, String Event, String value) {//Checked
        JSONObject obj = cf.getJSonResponseFromAccMethod(ifr, value);
        cm.mImplNumberToWorkds(ifr);
        if (obj.size() > 0) {
            return obj.toString();
        }
        return "";
    }

    //modified by keerthana for HL-LeadCapture on 21-08-2024
    public String mImplChangeLoanForPlotAndConstruction(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangeLoanForPlotAndConstruction:");
        try {
            String amtForPlot = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqAmtForPlot").toString();
            Log.consoleLog(ifr, "amtForPlot" + amtForPlot);
            String amtForConstruction = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqAmtForConstruction").toString();
            Log.consoleLog(ifr, "amtForConstruction" + amtForConstruction);
            BigDecimal amtPlot = new BigDecimal(amtForPlot.equalsIgnoreCase("") ? "0.0" : amtForPlot);
            BigDecimal amtConstruction = new BigDecimal(amtForConstruction.equalsIgnoreCase("") ? "0.0" : amtForConstruction);
            BigDecimal reqAmt = amtPlot.add(amtConstruction);
            Log.consoleLog(ifr, "loanamount" + reqAmt);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", String.valueOf(reqAmt));
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", "disable", "true");
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", "disable", "true");
            String resultdata = new AcceleratorActivityManagerCode().mAcconChangeLoanAmount(ifr, Control, Event, value);
            if (resultdata.equalsIgnoreCase("")) {
                cm.mImplNumberToWorkds(ifr);
                return "";
            }
            return resultdata;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplonChangeLoanForPlotAndConstruction method" + e);
            Log.errorLog(ifr, "Error in mImplonChangeLoanForPlotAndConstruction method" + e);
        }
        return "";
    }

    public void mImplChangeOccupationType(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangeOccupationType:");
        String occpType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();

        String[] salariedF = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears"};

        String[] nonSalariedF = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile"};

        String[] pension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly"};
        String[] nonPension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo", "table565_table388_relationshiPBborrower"};

        if (occpType.equalsIgnoreCase("Salaried")) {
            for (int i = 0; i < salariedF.length; i++) {
                ifr.setStyle(salariedF[i], "visible", "true");
                ifr.setStyle(salariedF[i], "mandatory", "true");
            }
            for (int i = 0; i < nonSalariedF.length; i++) {
                ifr.setStyle(nonSalariedF[i], "visible", "false");
                ifr.setValue(nonSalariedF[i], "");
            }
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "disable", "true");

        } else if (occpType.equalsIgnoreCase("AGRI") || occpType.equalsIgnoreCase("PROF")
                || occpType.equalsIgnoreCase("SELF")) {

            for (int i = 0; i < nonSalariedF.length; i++) {
                ifr.setStyle(nonSalariedF[i], "visible", "true");
            }
            for (int i = 0; i < salariedF.length; i++) {
                ifr.setStyle(salariedF[i], "visible", "false");
                ifr.setValue(salariedF[i], "");
            }
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome", "disable", "true");
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome", "mandatory", "true");
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual", "mandatory", "true");
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome", "mandatory", "true");
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness", "mandatory", "true");

        } else if (occpType.equalsIgnoreCase("PEN")) {
            Log.consoleLog(ifr, "Inside Pension chkkk");

            for (int i = 0; i < pension.length; i++) {
                ifr.setStyle(pension[i], "visible", "true");
                ifr.setStyle(pension[i], "mandatory", "true");
            }
            for (int i = 0; i < nonPension.length; i++) {
                if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")
                        && nonPension[i].equalsIgnoreCase("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType")) {
                    continue;
                }
                ifr.setStyle(nonPension[i], "visible", "false");
                ifr.setValue(nonPension[i], "");
            }

        } else {
            for (int i = 0; i < (salariedF.length + nonSalariedF.length); i++) {
                ifr.setStyle(salariedF[i], "visible", "false");
                ifr.setStyle(nonSalariedF[i], "visible", "false");
                ifr.setValue(salariedF[i], "");
                ifr.setValue(nonSalariedF[i], "");
            }
            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress", "");
            ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo", "");

        }
        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", "disable", "false");
        }

    }

    public String mImplChangeTotalWorkExp(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangeTotalWorkExp");
        return cm.mTotalExpCheck(ifr, "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg");
    }

    public String mImplChangeTotalExpInCurrBussiness(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "inside mImplChangeTotalExpInCurrBussiness");
        return cm.mTotalExpCheck(ifr, "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness");
    }

    public String mImplClickEligibiltyBRMS(IFormReference ifr, String Control, String Event, String JSdata) {
        return cm.mExecuteBRMSMargin_LTV(ifr);
    }

    //added by yeole 10-07-23
    public void mImplClickPermanentAddressType(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside address codetype= ");
        JSONArray AddressDetailsGrid = ifr.getDataFromGrid("LV_ADDRESS");//ALV_BASIC_INFO
        //cf.mSetClearComboValue(ifr, "Same_As_Permanent_Address");
        for (int i = 0; i < AddressDetailsGrid.size(); i++) {
            JSONObject AddressDetailsGridData = (JSONObject) AddressDetailsGrid.get(i);
            Log.consoleLog(ifr, "AddressDetailsGridData:: " + AddressDetailsGridData.toString());
            String AddressType = AddressDetailsGridData.get("Address Type").toString();
            Log.consoleLog(ifr, "AddressType= " + AddressType);
            ifr.addItemInCombo("Same_As_Permanent_Address", AddressType);
        }
    }

    public void mImplChangeNetMonthlyIncome(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangeNetMonthlyIncome:");
        cm.mSetNetIncome(ifr, "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome");
    }

    public void mImplChangeNetAnnualIncome(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangeNetAnnualIncome:");
        cm.mSetNetIncome(ifr, "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome");
    }

    //added by yeole 04-08-2023
    public String mImplChangeAddressTypeCheck(IFormReference ifr, String Control, String Event, String value) {
        try {
            Log.consoleLog(ifr, "Inside mAccChangeAddressTypeCheck");
            int count = ifr.getDataFromGrid("LV_ADDRESS").size();
            Boolean flagPer = Boolean.valueOf(true);
            Boolean flagCur = Boolean.valueOf(true);
            Boolean flagOff = Boolean.valueOf(true);
            JSONArray AddressDetailsGrid = ifr.getDataFromGrid("LV_ADDRESS");//LV_KYC_label
            JSONObject AddressDetailsGridData = null;
            //String checkdata = ifr.getValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType").toString();
            //cf.mSetClearComboValue(ifr, "QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType");
            for (int i = 0; i < count; i++) {
                String checkdata = ifr.getValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType").toString();
                Log.consoleLog(ifr, "checkdata = " + checkdata);
                AddressDetailsGridData = (JSONObject) AddressDetailsGrid.get(i);
                String AddressType = AddressDetailsGridData.get("Address Type").toString();
                Log.consoleLog(ifr, "AddressType = " + AddressType);
                if (AddressType.equalsIgnoreCase("Permanent") || AddressType.equalsIgnoreCase("P")) {
                    flagPer = false;
                    Log.consoleLog(ifr, "flagPer = " + flagPer);
                    if (checkdata.equalsIgnoreCase("Permanent") || checkdata.equalsIgnoreCase("P")) {
                        Log.consoleLog(ifr, "Inside showmessage = " + flagPer);
                        Log.consoleLog(ifr, "Inside showmessage checkdata = " + checkdata);
                        ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", this.cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "error", "Duplicate Address Type!"));
                        return message.toString();
                    }
                } else if (AddressType.equalsIgnoreCase("Office") || AddressType.equalsIgnoreCase("OA")) {
                    flagOff = false;
                    Log.consoleLog(ifr, "flagOff = " + flagOff);
                    if (checkdata.equalsIgnoreCase("Office") || checkdata.equalsIgnoreCase("OA")) {
                        Log.consoleLog(ifr, "Inside showmessage = " + flagOff);
                        Log.consoleLog(ifr, "Inside showmessage checkdata = " + checkdata);
                        ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", this.cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "error", "Duplicate Address Type!"));
                        return message.toString();
                    }
                } else if (AddressType.equalsIgnoreCase("Current") || AddressType.equalsIgnoreCase("CA")) {
                    flagCur = false;
                    Log.consoleLog(ifr, "flagCur = " + flagCur);
                    if (checkdata.equalsIgnoreCase("Current") || checkdata.equalsIgnoreCase("CA")) {
                        Log.consoleLog(ifr, "Inside showmessage = " + flagCur);
                        Log.consoleLog(ifr, "Inside showmessage checkdata = " + checkdata);
                        ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", this.cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "error", "Duplicate Address Type!"));
                        return message.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mAccChangeAddressTypeCheck:" + e);
            Log.errorLog(ifr, "Exception mAccChangeAddressTypeCheck:" + e);
        }
        return "";
    }

    public void ImplLoadCollateralLTV(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside ImplLoadCollateralLTV ");
        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("RISKDET_LTV").replaceAll("#PID#", PID);
            List<List<String>> list = cf.mExecuteQuery(ifr, query, "fetchDataQueryLTV:");
            if (list.size() > 0) {
                String LTV = list.get(0).get(0);
                Log.consoleLog(ifr, "list" + LTV);
                ifr.setValue("LV_RE_Margin", LTV);
            }
            String query1 = ConfProperty.getQueryScript("COLLATERALCOST").replaceAll("#PID#", PID);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, query1, "Collateral cost :");
            if (list1.size() > 0) {
                Log.consoleLog(ifr, "list1" + list1);
                String CollateralCost = list1.get(0).get(0);
                ifr.setValue("QNL_Mortgage_TotalPropertyValue", CollateralCost);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in ImplLoadCollateralLTV:" + e);
        }
    }
//added by yeole 29-06-2023

    public void mImplClickPermanentAddress(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            Log.consoleLog(ifr, "Inside address code= ");
            JSONArray AddressDetailsGrid = ifr.getDataFromGrid("LV_ADDRESS");//LV_KYC_label

            String AddType = ifr.getValue("Same_As_Permanent_Address").toString();
            Log.consoleLog(ifr, "AddressTypeAsSame= " + AddType);
            JSONObject AddressDetailsGridData = null;
            int count = ifr.getDataFromGrid("LV_ADDRESS").size();
            Log.consoleLog(ifr, "count= " + count);

            for (int i = 0; i < count; i++) {
                AddressDetailsGridData = (JSONObject) AddressDetailsGrid.get(i);
                Log.consoleLog(ifr, "AddressDetailsGridData:: " + AddressDetailsGridData.toString());
                String AddressType = AddressDetailsGridData.get("Address Type").toString();
                Log.consoleLog(ifr, "AddressType= " + AddressType);

                if (AddType.equalsIgnoreCase(AddressType)) {
                    Log.consoleLog(ifr, "insidePermanent= " + AddType);
                    String line1 = AddressDetailsGridData.get("Line 1").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line1", line1);
                    String line2 = AddressDetailsGridData.get("Line 2").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line2", line2);
                    String Line3 = AddressDetailsGridData.get("Line 3").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line3", Line3);
                    String LandMarks = AddressDetailsGridData.get("Landmark").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark", LandMarks);
                    String ZipCode = AddressDetailsGridData.get("PIN Code").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_PinCode", ZipCode);
                    String Area_Zone = AddressDetailsGridData.get("Area").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone", Area_Zone);
                    String Citys = AddressDetailsGridData.get("City").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_City_Town_Village", Citys);
                    String Districts = AddressDetailsGridData.get("District").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_District", Districts);
                    String Countrys = AddressDetailsGridData.get("Country").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country", Countrys);
                    String States = AddressDetailsGridData.get("State").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_State", States);
                    String Ownerships = AddressDetailsGridData.get("Ownership").toString();
                    ifr.setValue("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", Ownerships);
                } else if (AddType.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "insidenull " + AddType);
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line1");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line2");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line3");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_PinCode");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_City_Town_Village");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_District");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country");
                    ifr.clearCombo("QNL_BASIC_INFO_CNL_CUST_ADDRESS_State");
                }
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplClickPermanentAddress method" + e.getMessage());
            Log.errorLog(ifr, "Error in mImplClickPermanentAddress method");
        }
    }

    public void mImplCriteriaChecklist(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mImplCriteriaChecklist");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String valcount = ConfProperty.getQueryScript("criteriadata");
            valcount = valcount.replaceAll("#PID#", PID);
            List<List<String>> valcountresult = cf.mExecuteQuery(ifr, valcount, "criteriadata");
            Log.consoleLog(ifr, "valcount " + valcountresult);
            int count = ifr.getDataFromGrid("LV_OTHERCHKLIST").size();
            if (count == 0) {
                if (!valcountresult.isEmpty()) {
                    JSONArray CriteriaGrid1 = new JSONArray();
                    for (List<String> tmp : valcountresult) {
                        JSONObject ChklistGridData1 = new JSONObject();
                        ChklistGridData1.put("Criteria", tmp.get(0));
                        ChklistGridData1.put("ROI", tmp.get(1));
                        CriteriaGrid1.add(ChklistGridData1);
                    }
                    Log.consoleLog(ifr, "ChklistGridData:" + CriteriaGrid1);
                    ifr.addDataToGrid("LV_OTHERCHKLIST", CriteriaGrid1);
                } else {
                    Log.consoleLog(ifr, "No Data found");
                }
            }
            ifr.setColumnDisable("LV_OTHERCHKLIST", "1", true);
            ifr.setColumnDisable("LV_OTHERCHKLIST", "2", true);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in citeria" + e);
            Log.errorLog(ifr, "Exception in citeria" + e);
        }
    }

    public String mImplLoadDocumentTab(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplLoadBureauAppType value:" + value);
        JSONObject obj = cf.getJSonResponseFromAccMethod(ifr, value);
        if (obj.size() > 0) {
            return obj.toString();
        }
        try {
            if (value.split("~")[1].equalsIgnoreCase(ConfProperty.getCommonPropertyValue("DocumentTabName"))) {
                ifr.setStyle("F_OutwardDocument", "sectionstate", "expanded");
                ifr.setStyle("F_OutwardDocument", "sectionstate", "collapsed");
            } else if (value.split("~")[1].equalsIgnoreCase(ConfProperty.getCommonPropertyValue("RiskTabName"))) {
                ifr.setStyle("F_Risk_Rating", "sectionstate", "expanded");
                ifr.setStyle("F_Risk_Rating", "sectionstate", "collapsed");
            } else if (value.split("~")[1].equalsIgnoreCase(ConfProperty.getCommonPropertyValue("ActionTabName"))) {
                ifr.setStyle("F_Deviation", "sectionstate", "expanded");
                ifr.setStyle("F_Deviation", "sectionstate", "collapsed");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplLoadBureauAppType" + e);
            Log.errorLog(ifr, "Exception in mImplLoadBureauAppType" + e);
        }
        return "";
    }

    //showMandatory doc
    public void mImplShowMandatoryDoc(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            Log.consoleLog(ifr, "Inside mImplShowMandatoryDoc");
            ifr.clearTable("ALV_GENERATE_DOCUMENT");
            String mandatory = ifr.getValue("F_ShowDoc_Mandatory").toString();
            JSONArray jsonarray = new JSONArray();

            if (mandatory.equalsIgnoreCase("true")) {
                String query = ConfProperty.getQueryScript("MandDocumentName").
                        replaceAll("#ACTIVITYNAME#", ifr.getActivityName()).
                        replaceAll("#pid#", ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "query" + query);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Generate Document List");
                Log.consoleLog(ifr, "result " + result);
                for (int i = 0; i < result.size(); i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("QNL_GENERATE_DOCUMENT_DocumentName", result.get(i).get(0));
                    obj.put("QNL_GENERATE_DOCUMENT_DMSName", result.get(i).get(1));
                    obj.put("QNL_GENERATE_DOCUMENT_ActionID", result.get(i).get(2));
                    obj.put("QNL_GENERATE_DOCUMENT_DocStageName", result.get(i).get(3));
                    obj.put("QNL_GENERATE_DOCUMENT_ISMANDATORY", result.get(i).get(4));
                    Log.consoleLog(ifr, "jsonarray" + jsonarray);
                    jsonarray.add(obj);

                }
                ifr.addDataToGrid("ALV_GENERATE_DOCUMENT", jsonarray);
            } else {
                String query = ConfProperty.getQueryScript("DOCUMENTNAME").
                        replaceAll("#ACTIVITYNAME#", ifr.getActivityName()).
                        replaceAll("#pid#", ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "query" + query);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Generate Document List");

                // String query = "select A.DOCUMENTID,A.DMSNAME,A.ACTIONID,C.ACTIVITYNAME,A.MANDATORY from LOS_M_DOCUMENT A inner join LOS_M_DOCUMENT_SCHEME B on A.DOCUMENTID=B.DOCUMENTID inner join LOS_M_ACTIVITYNAME C on B.ACTIVITYCODE=C.ACTIVITYCODE WHERE A.GENERATE='Y' and C.ACTIVITYNAME='#ACTIVITYNAME#' and B.SCHEMEID in (select SCHEMEID from LOS_NL_PROPOSED_FACILITY where pid='#pid#')";
                // List<List<String>> result = ifr.getDataFromDB(query);
                // Log.consoleLog(ifr, "result " + result);
                for (int i = 0; i < result.size(); i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("QNL_GENERATE_DOCUMENT_DocumentName", result.get(i).get(0));
                    obj.put("QNL_GENERATE_DOCUMENT_DMSName", result.get(i).get(1));
                    obj.put("QNL_GENERATE_DOCUMENT_ActionID", result.get(i).get(2));
                    obj.put("QNL_GENERATE_DOCUMENT_DocStageName", result.get(i).get(3));
                    obj.put("QNL_GENERATE_DOCUMENT_ISMANDATORY", result.get(i).get(4));
                    jsonarray.add(obj);
                    Log.consoleLog(ifr, "jsonarray" + jsonarray);
                }
                ifr.addDataToGrid("ALV_GENERATE_DOCUMENT", jsonarray);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplShowMandatoryDoc" + e.getMessage());
            Log.errorLog(ifr, "Exception in mImplShowMandatoryDoc" + e);
        }
    }

    public String mImplChangeCampaignConcession(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        String query = ConfProperty.getQueryScript("FetchingCampaignValue").replaceAll("#CAMPAIGN_CODE#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Campaign").toString());
        cf.mGetLabelAndSetToField(ifr, query, "", "QNL_LOS_PROPOSED_FACILITY_ConsessionRoi,QNL_LOS_PROPOSED_FACILITY_ConcessionInProcessingFees");
        return cm.mChangeConcession(ifr);
    }

    public void mImplChangeGuidelineMarketLandValueLandArea(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplChangeGuidelineMarketLandValueLandArea:" + JSdata);
        try {
            BigDecimal guideLineValue = new BigDecimal(ifr.getValue("LV_RE_GuidelineRate").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("LV_RE_GuidelineRate").toString());
            BigDecimal marketValue = new BigDecimal(ifr.getValue("LV_RE_MarketVal").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("LV_RE_MarketVal").toString());
            BigDecimal landArea = new BigDecimal(ifr.getValue("LV_RE_PlotArea").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("LV_RE_PlotArea").toString());
            BigDecimal totalGuideLineValue = guideLineValue.multiply(landArea);
            BigDecimal totalMarketValue = marketValue.multiply(landArea);
            BigDecimal consideredLandValue = marketValue;
            BigDecimal consideredLandValuepersqt = totalGuideLineValue.min(totalMarketValue);
            ifr.setValue("LV_RE_TotGuidlnVal", totalGuideLineValue.toString());
            ifr.setValue("LV_RE_TotMarketVal", totalMarketValue.toString());
            ifr.setValue("QNL_Mortgage_ConsideredLandValueperSqft", consideredLandValue.toString());
            ifr.setValue("LV_RE_LandValConsd", consideredLandValuepersqt.toString());
            BigDecimal ConsideredBuildingValue = new BigDecimal(ifr.getValue("QNL_Mortgage_ConsideredBuildingValue").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_ConsideredBuildingValue").toString());
            BigDecimal ConsideredValue = consideredLandValuepersqt.add(ConsideredBuildingValue);
            ifr.setValue("QNL_Mortgage_TotalPropertyValue", ConsideredValue.toString());
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplChangeGuidelineMarketLandValueLandArea:" + e);
            Log.errorLog(ifr, "Exception in mImplChangeGuidelineMarketLandValueLandArea:" + e);
        }
    }

    public String mImplChangeLTVDeviationpercentage(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplChangeLTVDeviationpercentage:" + JSdata);
        try {
            BigDecimal min = new BigDecimal("0");
            BigDecimal max = new BigDecimal("10");
            BigDecimal LTVDeviationpercentage = new BigDecimal(ifr.getValue("QNL_Mortgage_LTVDeviationpercentage").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_LTVDeviationpercentage").toString());
            if (LTVDeviationpercentage.compareTo(min) >= 0 && LTVDeviationpercentage.compareTo(max) <= 0) {
                BigDecimal LTVPercentageas = new BigDecimal(ifr.getValue("LV_RE_Margin").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("LV_RE_Margin").toString());
                BigDecimal FinalLTV = LTVDeviationpercentage.add(LTVPercentageas);
                ifr.setValue("QNL_Mortgage_FinalLTVpercentage", FinalLTV.toString());
                BigDecimal TotalPropertyValue = new BigDecimal(ifr.getValue("QNL_Mortgage_TotalPropertyValue").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_TotalPropertyValue").toString());
                BigDecimal LoanEligibilityasperLTVonproperty = TotalPropertyValue.multiply(FinalLTV.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                ifr.setValue("QNL_Mortgage_LoanEligibilityasperLTVprop", LoanEligibilityasperLTVonproperty.toString());
            } else {
                JSONObject message = new JSONObject();
                ifr.setValue("QNL_Mortgage_LTVDeviationpercentage", "");
                message.put("showMessage", cf.showMessage(ifr, "QL_DEAL_LTV", "error", "Value should be between 0 to 10"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplChangeLTVDeviationpercentage:" + e);
            Log.errorLog(ifr, "Exception in mImplChangeLTVDeviationpercentage:" + e);
        }
        return "";
    }

    public void mImplChangeGuideliMarLandValLandBuilding(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplChangeGuideliMarLandValLandBuilding:" + JSdata);
        try {//
            BigDecimal guideLineValue = new BigDecimal(ifr.getValue("QNL_Mortgage_BuildingValueperSqft").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_BuildingValueperSqft").toString());
            BigDecimal marketValue = new BigDecimal(ifr.getValue("QNL_Mortgage_BuildingValueperSqftAsperVal").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_BuildingValueperSqftAsperVal").toString());
            BigDecimal landArea = new BigDecimal(ifr.getValue("QNL_Mortgage_BuildupArea").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_BuildupArea").toString());
            BigDecimal age = new BigDecimal(ifr.getValue("QNL_Mortgage_AgeofBuilding").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_Mortgage_AgeofBuilding").toString());
            BigDecimal depricatedValue = age.multiply(new BigDecimal(5));
            ifr.setValue("QNL_Mortgage_Depreciationapplicable", depricatedValue.toString());
            BigDecimal totalGuideLineValue = guideLineValue.multiply(landArea);
            BigDecimal consideredBuildingValue = (guideLineValue.min(marketValue)).multiply(landArea);
            BigDecimal depricatedpercentageValue = (consideredBuildingValue.multiply(depricatedValue)).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal FinalconsideredBuildingValue = consideredBuildingValue.subtract(depricatedpercentageValue);
            BigDecimal consideredLandValuepersqt = guideLineValue.min(marketValue);
            ifr.setValue("QNL_Mortgage_TotalBuildingValue", totalGuideLineValue.toString());
            ifr.setValue("QNL_Mortgage_ConsideredBuildingValue", FinalconsideredBuildingValue.toString());
            ifr.setValue("QNL_Mortgage_ConsideredBuildingValueSqf", consideredLandValuepersqt.toString());
            BigDecimal ConsideredLandValue = new BigDecimal(ifr.getValue("LV_RE_LandValConsd").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("LV_RE_LandValConsd").toString());
            BigDecimal ConsideredValue = consideredBuildingValue.add(ConsideredLandValue);
            ifr.setValue("QNL_Mortgage_TotalPropertyValue", ConsideredValue.toString());
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplChangeGuideliMarLandValLandBuilding:" + e);
            Log.errorLog(ifr, "Exception in mImplChangeGuideliMarLandValLandBuilding:" + e);
        }
    }

    public void mImplChangePropertyType(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplChangePropertyType:" + JSdata);
        try {
            String ids = "QNL_Mortgage_IsConsideredLandvalue,LV_RE_GuidelineRate,LV_RE_MarketVal,LV_RE_PlotArea,LV_RE_TotGuidlnVal,LV_RE_TotMarketVal,QNL_Mortgage_ConsideredLandValueperSqft,LV_RE_LandValConsd,QNL_Mortgage_BuildingValueperSqft,QNL_Mortgage_BuildingValueperSqftAsperVal,QNL_Mortgage_BuildupArea,QNL_Mortgage_TotalBuildingValue,QNL_Mortgage_AgeofBuilding,QNL_Mortgage_Depreciationapplicable,QNL_Mortgage_ConsideredBuildingValue,QNL_Mortgage_ConsideredBuildingValueSqf,QNL_Mortgage_TotalPropertyValue,LV_RE_Margin,QNL_Mortgage_LTVDeviationpercentage,QNL_Mortgage_FinalLTVpercentage,QNL_Mortgage_LoanEligibilityasperLTVprop";
            String[] idsArray = ids.split(",");
            for (int i = 0; i < idsArray.length; i++) {
                ifr.setValue(idsArray[i], "");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplChangePropertyType:" + e);
            Log.errorLog(ifr, "Exception in mImplChangePropertyType:" + e);
        }
    }

    public void mImplCollateralSummary(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplCollateralSummary:" + JSdata);
        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("collSummaryQuery").replaceAll("#PID#", PID);
            List<List<String>> queryResult = cf.mExecuteQuery(ifr, query, "CollateralSummary query:");
            if (queryResult.size() > 0) {
                BigDecimal totalNewColl = new BigDecimal("0.0");
                BigDecimal totalCollAvailCoverage = new BigDecimal("0.0");
                BigDecimal totalPrimaryColl = new BigDecimal("0.0");
                BigDecimal totalSecondaryColl = new BigDecimal("0.0");
                BigDecimal overallLTVPercentage = new BigDecimal("0.0");
                for (int i = 0; i < queryResult.size(); i++) {
                    BigDecimal totalNewColl1 = new BigDecimal(queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
                    totalNewColl = totalNewColl.add(totalNewColl1);
                    BigDecimal totalCollAvailCoverage1 = new BigDecimal(queryResult.get(i).get(2).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(2));
                    totalCollAvailCoverage = totalCollAvailCoverage.add(totalCollAvailCoverage1);
                    BigDecimal overallLTVPercentage1 = new BigDecimal(queryResult.get(i).get(2).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(2));
                    overallLTVPercentage = overallLTVPercentage.add(overallLTVPercentage1);
                    if (queryResult.get(i).get(3).equalsIgnoreCase("PS")) {
                        BigDecimal totalPrimaryColl1 = new BigDecimal(queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
                        totalPrimaryColl = totalPrimaryColl.add(totalPrimaryColl1);
                    } else if (queryResult.get(i).get(3).equalsIgnoreCase("CS")) {
                        BigDecimal totalSecondaryColl1 = new BigDecimal(queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
                        totalSecondaryColl = totalSecondaryColl.add(totalSecondaryColl1);
                    }
                }
                overallLTVPercentage = overallLTVPercentage.divide(new BigDecimal(queryResult.size()), 2, RoundingMode.HALF_UP);
                ifr.setValue("QL_COLLAT_SUMMARY_TOTNEWCOLL", totalNewColl.toString());
                ifr.setValue("QL_COLLAT_SUMMARY_TOTPRIMARYCOLL", totalPrimaryColl.toString());
                ifr.setValue("QL_COLLAT_SUMMARY_TOTSECNDCOLL", totalSecondaryColl.toString());
                ifr.setValue("QL_COLLAT_SUMMARY_TOTCOLLAVAILBL", totalCollAvailCoverage.toString());
                ifr.setValue("QL_COLLAT_SUMMARY_OVERALLLTV", overallLTVPercentage.toString());
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplCollateralSummary:" + e);
            Log.errorLog(ifr, "Exception in mImplCollateralSummary:" + e);
        }
    }

    public String mImplFetchDataScoreCard(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplFetchDataScoreCard");
        try {
            ifr.setStyle("QL_RISK_RATING_SpouseCoAppIncome", "disable", "false");
            ifr.setStyle("BTN_FetchScoring", "disable", "false");

            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String fetchDataQuery = ConfProperty.getQueryScript("RISKDET_partyDetails").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "Query" + fetchDataQuery);
            String fetchDataQueryProposeLoan = ConfProperty.getQueryScript("RISKDET_proposeLoan").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "Query" + fetchDataQueryProposeLoan);
            String fetchDataQueryLTV = ConfProperty.getQueryScript("RISKDET_LTV").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "Query" + fetchDataQueryLTV);
            String fetchDataQueryNetworth = ConfProperty.getQueryScript("RISKDET_networth").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "Query" + fetchDataQueryNetworth);

            List<List<String>> scoreIPData = cf.mExecuteQuery(ifr, fetchDataQuery, "");
            Log.consoleLog(ifr, "Scorecard Data" + scoreIPData);
            List<List<String>> scoreIPDataProposeLoan = cf.mExecuteQuery(ifr, fetchDataQueryProposeLoan, "");
            Log.consoleLog(ifr, "Scorecard Data" + scoreIPDataProposeLoan);
            List<List<String>> scoreIPDataLTV = cf.mExecuteQuery(ifr, fetchDataQueryLTV, "");
            Log.consoleLog(ifr, "Scorecard Data" + scoreIPDataLTV);
            List<List<String>> scoreIPDataNetworth = cf.mExecuteQuery(ifr, fetchDataQueryNetworth, "");
            Log.consoleLog(ifr, "Scorecard Data" + scoreIPDataNetworth);
//            if (!scoreIPData.isEmpty()) {
//                String[] result = new String[]{"QL_RISK_RATING_AcadQualify", "QL_RISK_RATING_MaritalStatus", "QL_RISK_RATING_ApplicantAge", "QL_RISK_RATING_NoOfDependents",
//                    "QL_RISK_RATING_OCCTYPE", "QL_RISK_RATING_EmployType", "QL_RISK_RATING_WorkExpnce", "QL_RISK_RATING_WorkExpnce",
//                    "QL_RISK_RATING_CurAddrsYears", "QL_RISK_RATING_ResidenceType", "QL_RISK_RATING_BankRelnship"};
//                for (int i = 0; i < result.length; i++) {
//                    if (result[i].equalsIgnoreCase("QL_RISK_RATING_EmployType")) {
//                        if (scoreIPData.get(0).get(i).equalsIgnoreCase("PSU") || scoreIPData.get(0).get(i).equalsIgnoreCase("MNC") || scoreIPData.get(0).get(i).equalsIgnoreCase("CGE") || scoreIPData.get(0).get(i).equalsIgnoreCase("L") || scoreIPData.get(0).get(i).equalsIgnoreCase("J")) {
//                            ifr.setValue("QL_RISK_RATING_WorkExpnce", scoreIPData.get(0).get(i + 1));
//                        } else {
//                            ifr.setValue("QL_RISK_RATING_WorkExpnce", scoreIPData.get(0).get(i + 2));
//                        }
//                        ifr.setValue(result[i], scoreIPData.get(0).get(i));
//                        i = i + 2;
//                    } else {
//                        ifr.setValue(result[i], scoreIPData.get(0).get(i));
//                    }
//                }
//            } 
            if (!scoreIPData.isEmpty()) {
                String[] result = new String[]{"QL_RISK_RATING_AcadQualify", "QL_RISK_RATING_MaritalStatus", "QL_RISK_RATING_ApplicantAge", "QL_RISK_RATING_NoOfDependents",
                    "QL_RISK_RATING_OCCTYPE", "QL_RISK_RATING_EmployType", "QL_RISK_RATING_WorkExpnce", "QL_RISK_RATING_WorkExpnce",
                    "QL_RISK_RATING_CurAddrsYears", "QL_RISK_RATING_ResidenceType", "QL_RISK_RATING_BankRelnship"};
                for (int i = 0; i < result.length; i++) {
                    if (result[i].equalsIgnoreCase("QL_RISK_RATING_EmployType")) {
                        int flag = 0;
                        String ar[] = new String[]{"HW", "J", "L", "MR", "MLA", "MP", "MNC", "NUR", "PM", "PR", "PSE", "PSU", "PLE", "SP", "SCI", "SGE", "T", "O", "CGE"};
                        for (int j = 0; j < ar.length; j++) {
                            if (scoreIPData.get(0).get(i).equalsIgnoreCase(ar[j])) {// || scoreIPData.get(0).get(i).equalsIgnoreCase("MNC") || scoreIPData.get(0).get(i).equalsIgnoreCase("CGE") || scoreIPData.get(0).get(i).equalsIgnoreCase("L") || scoreIPData.get(0).get(i).equalsIgnoreCase("J")) {
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 1) {
                            ifr.setValue("QL_RISK_RATING_WorkExpnce", scoreIPData.get(0).get(i + 1));
                        } else {
                            ifr.setValue("QL_RISK_RATING_WorkExpnce", scoreIPData.get(0).get(i + 2));
                        }

                        ifr.setValue(result[i], scoreIPData.get(0).get(i));
                        i = i + 2;
                    } else {
                        ifr.setValue(result[i], scoreIPData.get(0).get(i));
                    }
                }
            } else {
                Log.consoleLog(ifr, "===No Data Found in scoreIPData===");
            }
            if (!scoreIPDataProposeLoan.isEmpty()) {
                String[] ctrlID = new String[]{"QL_RISK_RATING_RepayPeriod", "QL_RISK_RATING_RepayType", "QL_RISK_RATING_LoanPurpose",
                    "QL_RISK_RATING_LOANAMT"};
                for (int i = 0; i < ctrlID.length; i++) {
                    ifr.setValue(ctrlID[i], scoreIPDataProposeLoan.get(0).get(i));
                }
            } else {
                Log.consoleLog(ifr, "===No Data Found in scoreIPDataProposeLoan===");
            }
            if (!scoreIPDataLTV.isEmpty()) {
                ifr.setValue("QL_RISK_RATING_LTV", scoreIPDataLTV.get(0).get(0));
            } else {
                Log.consoleLog(ifr, "===No Data Found in scoreIPDataLTV===");
            }
            if (!scoreIPDataNetworth.isEmpty()) {
                BigDecimal netWorth = new BigDecimal(scoreIPDataNetworth.get(0).get(0));
                BigDecimal reqLoanAmount = new BigDecimal(scoreIPDataProposeLoan.get(0).get(3));
                Log.consoleLog(ifr, " netWorth " + netWorth);
                Log.consoleLog(ifr, "reqLoanAmount " + reqLoanAmount);

                BigDecimal netWorthReqLoanAmtRatio = (netWorth.divide(reqLoanAmount, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
                Log.consoleLog(ifr, "netWorthReqLoanAmtRatio " + netWorthReqLoanAmtRatio);
                ifr.setValue("QL_RISK_RATING_NetWorth", String.valueOf(netWorthReqLoanAmtRatio));
            } else {
                Log.consoleLog(ifr, "===No Data Found in scoreIPDataNetworth===");
            }

            String queryInstallmentAmtLia = ConfProperty.getQueryScript("EMI_NMI_Ratio").replaceAll("#PID#", PID);
            String queryAvgIncome = ConfProperty.getQueryScript("RiskAvgIncomeDet").replaceAll("#PID#", PID);
            String queryCoBorrower = ConfProperty.getQueryScript("FinacialTabDataRISKDET").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "NoOfapplicant" + queryCoBorrower);
            // BigDecimal coApplicantIncome = BigDecimal.ZERO;
            String NoOfapplicant = null;
            BigDecimal emiInstallment = BigDecimal.ZERO;
            BigDecimal netIncome = BigDecimal.ZERO;
            int avgCOunt = 0;
            BigDecimal avgnetIncome = BigDecimal.ZERO;
            //BigDecimal RepaymentEmngineEMI = BigDecimal.ZERO;
            BigDecimal RepaymentEmngineEMI = new BigDecimal(scoreIPDataProposeLoan.get(0).get(4).equalsIgnoreCase("") ? "0.0" : scoreIPDataProposeLoan.get(0).get(4));
            List<List<String>> executeData = cf.mExecuteQuery(ifr, queryCoBorrower, "");
            List<List<String>> dataEMI = cf.mExecuteQuery(ifr, queryInstallmentAmtLia, "");
            List<List<String>> dataAvgIncome = cf.mExecuteQuery(ifr, queryAvgIncome, "");

            if (!dataAvgIncome.isEmpty()) {
                for (int i = 0; i < dataAvgIncome.size(); i++) {
                    BigDecimal income = new BigDecimal(dataAvgIncome.get(i).get(1).equals("Salaried") || dataAvgIncome.get(i).get(1).equals("PEN") ? dataAvgIncome.get(i).get(0) : dataAvgIncome.get(i).get(2));
                    netIncome = netIncome.add(income);
                    if (dataAvgIncome.get(i).get(1).equals("Salaried") || dataAvgIncome.get(i).get(1).equals("PEN")) {
                        avgCOunt++;
                    } else {
                        avgCOunt = 12;
                    }
                }
            } else {
                Log.consoleLog(ifr, "===No Data Found in dataAvgIncome===");
            }
            avgnetIncome = netIncome.divide(new BigDecimal(avgCOunt));
            if (!dataEMI.isEmpty()) {
                for (int i = 0; i < dataEMI.size(); i++) {
                    BigDecimal emtAmt = new BigDecimal(dataEMI.get(i).get(0));
                    emiInstallment = emiInstallment.add(emtAmt);
                }
            } else {
                Log.consoleLog(ifr, "===No Data Found in dataEMI===");
            }
            if (executeData.isEmpty()) {
                NoOfapplicant = "Single";
                //  ifr.setValue("QL_RISK_RATING_SpouseCoAppIncome", "0.0");
            } else {
                for (int i = 0; i < executeData.size(); i++) {
                    Log.consoleLog(ifr, "inside for loop");
                    if (executeData.get(i).get(0).equals("Yes")) {
                        if (NoOfapplicant == null) {
                            NoOfapplicant = "Joint Where co-applicant is earning ";
                        }
                        // BigDecimal coApplicantIncomeVal = new BigDecimal(executeData.get(i).get(1));
                        // coApplicantIncome = coApplicantIncome.add(coApplicantIncomeVal);
                    }
                }
                if (NoOfapplicant == null) {
                    NoOfapplicant = "Joint";
                }
                Log.consoleLog(ifr, "NoOfapplicant" + NoOfapplicant);
                //  Log.consoleLog(ifr, "coApplicantIncome" + coApplicantIncome);
            }

            BigDecimal avgDisposableIncome = avgnetIncome.subtract(emiInstallment).subtract(RepaymentEmngineEMI);
            BigDecimal EMI_NMIRatio = (emiInstallment.add(RepaymentEmngineEMI)).divide(avgnetIncome, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ifr.setValue("QL_RISK_RATING_NoApplicants", NoOfapplicant);
            //  ifr.setValue("QL_RISK_RATING_SpouseCoAppIncome", coApplicantIncome.toString());
            ifr.setValue("QL_RISK_RATING_MonAvgDisIncome", avgDisposableIncome.toString());
            ifr.setValue("QL_RISK_RATING_EMINMIRatio", EMI_NMIRatio.toString());

            ifr.setValue("QL_RISK_RATING_CibilScore", "747"); //HARDCODED
            nmi = avgnetIncome.toString();
            //returnData = mImplFetchScoreScoreCard(ifr, Control, Event, avgnetIncome.toString());           
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mImplFetchDataScoreCard" + e);
            Log.errorLog(ifr, "Exception Inside mImplFetchDataScoreCard" + e);
        }

        return "";

    }

    public String mImplFetchScoreScoreCard(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplFetchScoreScoreCard");
        String returnData = "";
        try {
            String value = "";

            String sendMessage = "";
            String setIpParams = "";

            String acadQualify = ifr.getValue("QL_RISK_RATING_AcadQualify").toString();
            if (acadQualify.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Academic Qualification";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String employType = ifr.getValue("QL_RISK_RATING_EmployType").toString();
            if (employType.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Employer Type";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String workExpnce = ifr.getValue("QL_RISK_RATING_WorkExpnce").toString();
            if (workExpnce.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Work Experience";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String maritalStatus = ifr.getValue("QL_RISK_RATING_MaritalStatus").toString();
            if (maritalStatus.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Marital Status";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String applicantAge = ifr.getValue("QL_RISK_RATING_ApplicantAge").toString();
            if (applicantAge.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Applicant Age";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String bankRelnship = ifr.getValue("QL_RISK_RATING_BankRelnship").toString();
            if (bankRelnship.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Bank Relationship";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String residenceType = ifr.getValue("QL_RISK_RATING_ResidenceType").toString();
            if (residenceType.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Residence Type";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String noOfYrsInCurrAdd = ifr.getValue("QL_RISK_RATING_CurAddrsYears").toString();
            if (noOfYrsInCurrAdd.equalsIgnoreCase("")) {
                sendMessage = "Please Fill No Of Years In Current Address";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String coapp = ifr.getValue("QL_RISK_RATING_SpouseCoAppIncome").toString();
            if (coapp.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Co-Applicant Income";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            // String coapp = coappIncome.replaceAll("\\..*", "");
            String noOfDependents = ifr.getValue("QL_RISK_RATING_NoOfDependents").toString();
            if (noOfDependents.equalsIgnoreCase("")) {
                sendMessage = "Please Fill No Of Dependents";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }

            String noOfApplicants = ifr.getValue("QL_RISK_RATING_NoApplicants").toString();
            if (noOfApplicants.equalsIgnoreCase("")) {
                sendMessage = "Please Fill No Of Applicants";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String repayPeriod = ifr.getValue("QL_RISK_RATING_RepayPeriod").toString();
            if (repayPeriod.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Repayment Period";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String repayType = ifr.getValue("QL_RISK_RATING_RepayType").toString();
            if (repayType.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Repayment Type";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String loanPurpose = ifr.getValue("QL_RISK_RATING_LoanPurpose").toString();
            if (loanPurpose.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Loan Purpose";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String netWorth = ifr.getValue("QL_RISK_RATING_NetWorth").toString();
            if (netWorth.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Net Worth";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String ltv = ifr.getValue("QL_RISK_RATING_LTV").toString();
            if (ltv.equalsIgnoreCase("")) {
                sendMessage = "Please Fill LTV";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String disposibleIncome = ifr.getValue("QL_RISK_RATING_MonAvgDisIncome").toString();
            if (disposibleIncome.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Disposible Income";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String eminmiratio = ifr.getValue("QL_RISK_RATING_EMINMIRatio").toString();
            if (eminmiratio.equalsIgnoreCase("")) {
                sendMessage = "Please Fill EMI/NMI Ratio";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }
            String cibilScore = ifr.getValue("QL_RISK_RATING_CibilScore").toString();
            if (cibilScore.equalsIgnoreCase("")) {
                sendMessage = "Please Fill CIBIL Score";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }

            String loanAmt = ifr.getValue("QL_RISK_RATING_LOANAMT").toString();
            if (loanAmt.equalsIgnoreCase("")) {
                sendMessage = "Please Fill Loan Amount";
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoring", "error", sendMessage));
                return message.toString();
            }

            String grossincome = "0";
            JSONObject result = cf.executeBRMSRule(ifr, "SCORE", acadQualify + "," + coapp + "," + applicantAge + "," + bankRelnship + "," + noOfYrsInCurrAdd + "," + employType + "," + noOfDependents + "," + residenceType + "," + workExpnce + "," + maritalStatus + "," + loanAmt + "," + ltv);
            Log.consoleLog(ifr, "Result of Scorecard BRMS = " + result);
            JSONObject message = new JSONObject();

            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) { //Fetching From JSON
                Log.consoleLog(ifr, "inside if");
                ifr.setValue("QL_RISK_RATING_AcadQualifyScore", cf.getJsonValue(result, "aq_op"));
                ifr.setValue("QL_RISK_RATING_CoAppIncomeScore", cf.getJsonValue(result, "sc_op_app_incm"));
                ifr.setValue("QL_RISK_RATING_ApplicantAgeScore", cf.getJsonValue(result, "sc_opapplicant_age_"));
                ifr.setValue("QL_RISK_RATING_BankRelnshipScore", cf.getJsonValue(result, "sc_op_rela_with_bank"));
                ifr.setValue("QL_RISK_RATING_CurAddrsYearsScore", cf.getJsonValue(result, "sc_op_noofyrsincurradd"));
                ifr.setValue("QL_RISK_RATING_EmployTypeScore", cf.getJsonValue(result, "sc_ip_emptype_op"));
                ifr.setValue("QL_RISK_RATING_NoOfDependentsScore", cf.getJsonValue(result, "sc_op_no_dependedant"));
                ifr.setValue("QL_RISK_RATING_ResidenceTypeScore", cf.getJsonValue(result, "sc_residancetype"));
                ifr.setValue("QL_RISK_RATING_WorkExprnceScore", cf.getJsonValue(result, "sc_op_workexp"));
                ifr.setValue("QL_RISK_RATING_MaritalStatusScore", cf.getJsonValue(result, "sc_op_marital_status"));
                ifr.setValue("QL_RISK_RATING_LTVScore", cf.getJsonValue(result, "sc_op_tvl"));

            } else {
                sendMessage = AcceleratorConstants.TRYCATCHERRORBRMS;
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
                return message.toString();
            }

            int finalScore = Integer.parseInt(cf.getJsonValue(result, "aq_op"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_app_incm"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_opapplicant_age_"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_rela_with_bank"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_noofyrsincurradd"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_ip_emptype_op"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_no_dependedant"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_residancetype"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_workexp"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_marital_status"))
                    + Integer.parseInt(cf.getJsonValue(result, "sc_op_tvl"));

            result = cf.executeBRMSRule(ifr, "Scorecard", cibilScore + "," + eminmiratio + "," + grossincome + "," + loanPurpose + "," + disposibleIncome + "," + netWorth + "," + nmi + "," + noOfApplicants + "," + employType + "," + repayPeriod + "," + repayType);//+ "," + finalScore +","+grade);
            Log.consoleLog(ifr, "Result of Scorecard BRMS = " + result);

            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) { //Fetching From JSON
                Log.consoleLog(ifr, "inside if again##");
                ifr.setValue("QL_RISK_RATING_NoApplicantsScore", cf.getJsonValue(result, "sc_out_no_of_applicant_sc"));
                ifr.setValue("QL_RISK_RATING_RepayPeriodScore", cf.getJsonValue(result, "sc_out_repaymentperiod"));
                ifr.setValue("QL_RISK_RATING_RepayTypeScore", cf.getJsonValue(result, "sc_out_repaymenttype"));
                ifr.setValue("QL_RISK_RATING_LoanPurposeScore", cf.getJsonValue(result, "sc_out_loanpurpose"));
                ifr.setValue("QL_RISK_RATING_NetWorthScore", cf.getJsonValue(result, "sc_out_networth"));
                ifr.setValue("QL_RISK_RATING_MonAvgDisIncomeScore", cf.getJsonValue(result, "sc_ou_mnthly_avg_dis_incm"));
                ifr.setValue("QL_RISK_RATING_EMINMIRatioScore", cf.getJsonValue(result, "sc_out_eminmi_ratio_score"));
                ifr.setValue("QL_RISK_RATING_CibilScoreScore", cf.getJsonValue(result, "sc_out_cibilscore"));

                finalScore += Integer.parseInt(cf.getJsonValue(result, "sc_out_no_of_applicant_sc"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_repaymentperiod"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_repaymenttype"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_loanpurpose"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_networth"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_ou_mnthly_avg_dis_incm"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_eminmi_ratio_score"))
                        + Integer.parseInt(cf.getJsonValue(result, "sc_out_cibilscore"));
                ifr.setValue("QL_RISK_RATING_RiskScore", String.valueOf(finalScore));

                returnData = finalScorecard(ifr);
            } else {
                sendMessage = AcceleratorConstants.TRYCATCHERRORBRMS;
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
                return message.toString();
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception mImplFetchScoreScoreCard" + e);
            Log.errorLog(ifr, "Exception Inside mImplFetchScoreScoreCard" + e);
        }
        return returnData;
    }

    public String finalScorecard(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside finalScorecard");
        JSONObject message = new JSONObject();
        try {
            String sendMessage = "";
            // String finalscore = ifr.getValue("QL_RISK_RATING_RiskScore").toString();
            String finalscore = ifr.getValue("QNL_R_SCORERATING_CanaraRetailGrade").toString();
            JSONObject result = cf.executeBRMSRule(ifr, "TotalGradeAndDecision", finalscore);
            Log.consoleLog(ifr, "Result of Scorecard BRMS = " + result);

            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) { //Fetching From JSON
                Log.consoleLog(ifr, "inside if");
                //  ifr.setValue("QL_RISK_RATING_RiskRank", cf.getJsonValue(result, "sc_out_totalgrade"));
                ifr.setValue("QNL_R_SCORERATING_RiskScore_Rating", cf.getJsonValue(result, "sc_out_totalgrade"));
                ifr.setValue("QL_RISK_RATING_Decision", cf.getJsonValue(result, "sc_out_lendingdecision"));
                sendMessage = "Scorecard Calculated Successfully!";
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
            } else {
                sendMessage = AcceleratorConstants.TRYCATCHERRORBRMS;
                message.put("showMessage", cf.showMessage(ifr, "F_Risk_Rating", "error", sendMessage));
            }
            Log.consoleLog(ifr, "Scorecard Calculated Successfully!");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception finalScorecard" + e);
            Log.errorLog(ifr, "Exception Inside finalScorecard" + e);
        }
        return message.toString();
    }

//modified by keerthana for HL-LeadCapture on 21-08-2024
    public void ImplChangeLoanAvail(IFormReference ifr, String Control, String Event, String JSdata) {

        Log.consoleLog(ifr, "Inside ImplChangeLoanAvail");
        String loanPurpose = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose").toString();
        if (loanPurpose.equalsIgnoreCase("OTH")) {
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "true");
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "mandatory", "true");
        } else {
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "false");
        }
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_ReqAmtForPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtForConstruction,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords");

        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
            if (String.valueOf(ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct")).equalsIgnoreCase("STP-CP")) {
                Log.consoleLog(ifr, "AcceleratorActivityManagerCode:ImplChangeLoanAvail -> Inside lead capture preferred emi date hide for pension");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_EMI_DATE", "visible", "false");
            } else if (String.valueOf(ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product")).equalsIgnoreCase("HL") && String.valueOf(ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose")).equalsIgnoreCase("PSC")) {
                Log.consoleLog(ifr, "AcceleratorActivityManagerCode:ImplChangeLoanAvail -> Inside lead capture HL ");
                String VisibleFields = "QNL_LOS_PROPOSED_FACILITY_ReqAmtForPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtForConstruction";
                pcm.controlvisiblity(ifr, VisibleFields);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqAmtForPlot", "disable", "false");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqAmtForConstruction", "disable", "false");
            } else {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_EMI_DATE", "visible", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqAmtForPlot", "visible", "false");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ReqAmtForConstruction", "visible", "false");
            }
        }
    }
    
// modiefied by vandana 29-07-2024

    public String ImplLoadPartyGrid(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            Log.consoleLog(ifr, "Inside ImplLoadPartyGrid");
            String exttype = ifr.getValue("CTRID_PD_EXTCUST").toString();
            if (ifr.getValue("QNL_BASIC_INFO_ExistingCustomer").toString().equalsIgnoreCase("")) {
                ifr.setValue("QNL_BASIC_INFO_ExistingCustomer", exttype);
            }
            // int count = ifr.getDataFromGrid("LV_MIS_Data").size();
            String fkey = Bpcm.Fkey(ifr, "B");
            Log.consoleLog(ifr, "f_key==>" + fkey);
            String count = "";
            String MisCount = " SELECT count(*) FROM LOS_NL_MIS_DATA where f_key='" + fkey + "'";
            List<List<String>> MisDATA = ifr.getDataFromDB(MisCount);
            if (!MisDATA.isEmpty()) {
                count = MisDATA.get(0).get(0);
            }
            Log.consoleLog(ifr, "count==> " + count);
            if (Integer.parseInt(count) == 0) {
                Log.consoleLog(ifr, "Inside ImplLoadPartyGrid IF");
                bbcc.mPopulateMisDataCB(ifr);
            }
            Log.consoleLog(ifr, "inside ImplLoadPartyGrid AcceleratorActivityManagerCode ");
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            ifr.setStyle("LV_ADDRESS", "visible", "true");
            ifr.setStyle("LV_KYC", "visible", "true");
            ifr.setStyle("LV_MIS_DATA", "visible", "true");

            String PartyType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            Log.consoleLog(ifr, "PartyType ::" + PartyType);
            if (PartyType.equalsIgnoreCase("B") || PartyType.equalsIgnoreCase("CB")) {
                ifr.setStyle("LV_OCCUPATION_INFO", "visible", "true");
                Log.consoleLog(ifr, " OCCUPATION_INFO is Visible for Borrower::");
            }
            // Devarinti Vandana

            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                ifr.addItemInCombo("QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality", "Indian");

                ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality", "Indian");

                Log.consoleLog(ifr, "inside Lead Capture ");
                ifr.setStyle("LV_GUARANTOR", "visible", "false");
                ifr.setStyle("LV_MIS_Data", "visible", "false");
                ifr.setStyle("F_P_ExistingRelationship", "visible", "false");
                ifr.setStyle("duplicateAdvancedListviewchanges_ALV_BASIC_INFO", "disable", "true"); //modified by Vandana 17/06/2024
                String ActivityName = ifr.getActivityName();
                String VisibleFieldsB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                String VisibleFieldsCB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName"
                        + "QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO.AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_Relationshipwithapplicant,QNL_BASIC_INFO_FullName";

                String editable = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents";

                pcm.controlvisiblity(ifr, VisibleFieldsB);
                pcm.controlvisiblity(ifr, VisibleFieldsCB);
                String nonEditableFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_FullName";//QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality               
                pcm.controlvisiblity(ifr, VisibleFieldsB);
                String InvisibleFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,table565_textbox6838,QNL_BASIC_INFO_customerFlag,table565_textbox6908,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_MotherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_KeyPersonName,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ConstitutionType,table565_datepick2,QNL_BASIC_INFO_CL_BASIC_INFO_NI_IndustryType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifyIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifySubIndustry,table565_datepick375,table565_datepick97,QNL_BASIC_INFO_CL_BASIC_INFO_NI_RegistrationNumber,table565_datepick101,table565_datepick102,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofYearsinBusiness,mQNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofEmployees,QNL_BASIC_INFO_CL_BASIC_INFO_NI_CountryOfIncorp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_TurnOverm,QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BusinessActivity";//(table565_combo1754-recovery mechanism)
                pcm.controlDisable(ifr, nonEditableFields);
                String InvisibleFieldsCB = "QNL_BASIC_INFO_SelectSalaryAccount";
                pcm.controlinvisiblity(ifr, InvisibleFields);
                String NonEditableFileds = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                Log.consoleLog(ifr, " partyDetailsSection InvisibleFields;:" + InvisibleFields);

                String salaryAccount = "";
                String CustAccopen = "";

                String firstName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
                String middleName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
                String LastName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
                String Fullname = concatFullname(firstName, middleName, LastName);
                Log.consoleLog(ifr, " partyDetailsSection Fullname;:" + Fullname);
                ifr.setValue("QNL_BASIC_INFO_FullName", Fullname);

                if (PartyType.equalsIgnoreCase("B")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");
                    pcm.controlvisiblity(ifr, VisibleFieldsB);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    pcm.controlEnable(ifr, editable);
                    ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "visible", "false");//modified by Vandana 17/06/2024
                    String MartialStatus = ifr.getValue("QNL_BASIC_INFO_CL_BASIC _INFO_I_MaritalStatus").toString();
                    Log.consoleLog(ifr, "OccuppationInfoDetails MartialStatus BORROWER::" + MartialStatus);
                    salaryAccount = Bpcm.Accountdetail(ifr);

                    //for calculating age based on the dob
                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        // Parse the date string into a Date object
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    String CustomerId = pcm.getCustomerIDCB(ifr, PartyType);
                    String MobileNumber = pcm.getMobileNumber(ifr);
                    String mobNum = "91" + MobileNumber;
                    Log.consoleLog(ifr, "mobilenumber::" + MobileNumber);
                    Log.consoleLog(ifr, "CustomerId::" + CustomerId);
                    //String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
                    Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
                    String response360 = objCbs360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "Canara Budget");
                    Log.consoleLog(ifr, "response==>" + response360);
                    if (response360.contains(RLOS_Constants.ERROR)) {
                        Log.consoleLog(ifr, "inside error condition 360API LAD");
                        return pcm.returnError(ifr);
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
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:AccountId::" + AccountId);

                                CustAccopen = InputStringResponseJSON.get("DatAcctOpen").toString().trim();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:CustAccopen::" + CustAccopen);
                                String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:BranchCode::" + BranchCode);
                                //ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId, AccountId + "-" + BranchCode + "-" + strAcctOpen + "-" + strAcctbal + "-" + stractprdcode);
                                ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId);
                            }
                        }
                    }
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation::");

                    String FkeyB = Bpcm.Fkey(ifr, ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString());

                    if (MartialStatus.equalsIgnoreCase("Married")) {

                        Log.consoleLog(ifr, "into marital status condition");

                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "true");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "true");

                    } else {
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "false");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "false");

                    }
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd"); // Input format
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd"); // Output format
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    System.out.println("Formatted Date: " + formattedDate);
                    LocalDate PastDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate curDate = LocalDate.now();
                    Log.consoleLog(ifr, "PastDate: " + PastDate);
                    Log.consoleLog(ifr, "curDate :" + curDate);
                    long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
                    int YearsWithCanara = (int) (monthsBetween / 12);
                    int MonthsWithCanara = (int) (monthsBetween % 12);
                    Log.consoleLog(ifr, "YearsWithCanara: " + YearsWithCanara);
                    Log.consoleLog(ifr, "MonthsWithCanara :" + MonthsWithCanara);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);
                    ifr.setValue("QNL_BASIC_INFO_RelationshipWithBank", String.valueOf(YearsWithCanara));
                    ifr.setValue("QNL_BASIC_INFO_RelationshipwithBankMonths", String.valueOf(MonthsWithCanara));

                }

                if (PartyType.equalsIgnoreCase("CB")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");
                    pcm.controlvisiblity(ifr, VisibleFieldsCB);
                    pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                    pcm.controlEnable(ifr, editable);

                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    String FkeyCB = "";
                    String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", ProcessInstanceId);
                    Log.consoleLog(ifr, "OccuppationInfoDetails1 query CO-BORROWER1::" + IndexQuery1);
                    List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
                    if (!dataResult1.isEmpty()) {
                        FkeyCB = dataResult1.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "dataResult1 fKey CB::" + FkeyCB);

                    String relationshipwithapplicant = "";
                    String RealtionShipWithApplicant = ConfProperty.getQueryScript("RealtionShipWithApplicant").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> RealtionShipWithApplicantQuery = cf.mExecuteQuery(ifr, RealtionShipWithApplicant, "RealtionShipWithApplicant Query");
                    if (!RealtionShipWithApplicantQuery.isEmpty()) {
                        relationshipwithapplicant = RealtionShipWithApplicantQuery.get(0).get(0);
                        ifr.setValue("QNL_BASIC_INFO_Relationshipwithapplicant", relationshipwithapplicant);
                        ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "disable", "true");
                    }

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);

                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception finalScorecard" + e);
            Log.errorLog(ifr, "Exception Inside finalScorecard" + e);
        }
        return "";
    }

    public void mACCONClickSourcedBy(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mACCONClickSourcedBy Method");
        try {
            String Query = "";
            String Criteria = "";
            Criteria = ifr.getValue("QL_SOURCINGINFO_Search_Criteria").toString();
            String Term = ifr.getValue("QL_SOURCINGINFO_Search_Term").toString();
            Log.consoleLog(ifr, " Criteria = " + Criteria);
            Log.consoleLog(ifr, " Term = " + Term);
            if (Criteria.equalsIgnoreCase("DSA Code")) {
                Query = ConfProperty.getQueryScript("SearchQuery1").replaceAll("#EMPLOYEEID#", Term);
                List<List<String>> list = cf.mExecuteQuery(ifr, Query, "Name with Code");
                String EMPLOYEE = list.get(0).get(0);
                ifr.setValue("LV_EmployeeID", EMPLOYEE);
            } else if (Criteria.equalsIgnoreCase("DSA Name")) {
                Query = ConfProperty.getQueryScript("SearchQuery2").replaceAll("#EMPLOYEENAME#", Term);
                List<List<String>> list = cf.mExecuteQuery(ifr, Query, "Code with Name");
                String EMPLOYEE = list.get(0).get(0);
                ifr.setValue("LV_EmployeeID", EMPLOYEE);
            } else if (Criteria.equalsIgnoreCase("PF Number")) {
                Query = ConfProperty.getQueryScript("SearchQuery3").replaceAll("#EMPLOYEEID#", Term);
                List<List<String>> list = cf.mExecuteQuery(ifr, Query, "Code with Name");
                String EMPLOYEE = list.get(0).get(0);
                ifr.setValue("LV_EmployeeID", EMPLOYEE);
            } else if (Criteria.equalsIgnoreCase("Employee Name")) {
                Query = ConfProperty.getQueryScript("SearchQuery4").replaceAll("#EMPLOYEENAME#", Term);
                List<List<String>> list = cf.mExecuteQuery(ifr, Query, "Code with Name");
                String EMPLOYEE = list.get(0).get(0);
                ifr.setValue("LV_EmployeeID", EMPLOYEE);
            } else {
                Log.consoleLog(ifr, "Invalid Value");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mACCONClickSourcedBy Method " + e);
        }
    }

    public void mACCONChangeCriteria(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mACCONChangeCriteria Method");
        try {
            ifr.setValue("QL_SOURCINGINFO_Search_Term", "");
            ifr.setValue("LV_EmployeeID", "");

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mACCONChangeCriteria Method " + e);
        }
    }

    public void mImplClearEligibility(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplClearEligibility Method");
        try {

            ifr.setValue("QL_DEAL_Margin", "");
            ifr.setValue("QL_DEAL_LTV", "");
            ifr.setValue("QL_DEAL_PermissibleDeduction", "");
            ifr.setValue("QL_DEAL_AmountAvailableforDeduction", "");
            ifr.setValue("QL_DEAL_IndicativeEligibleLimit", "");
            // mImplConcessionDeatils(ifr, Control, Event, JSdata);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplClearEligibility Method " + e);
        }
    }

    public void ImplSetMonthlyIncome(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside ImplSetMonthlyIncome");
        String OccupationType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();
        Log.consoleLog(ifr, " OccupationType  " + OccupationType);
        try {
            if (OccupationType.equalsIgnoreCase("SELF") || OccupationType.equalsIgnoreCase("AGRI") || OccupationType.equalsIgnoreCase("PEN") || OccupationType.equalsIgnoreCase("PROF")) {
                Log.consoleLog(ifr, "Inside id of ImplSetMonthlyIncome");

                BigDecimal Annual = new BigDecimal(ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome").toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome").toString());
                Log.consoleLog(ifr, "value of annaul ==" + Annual);
                BigDecimal result = Annual.divide(new BigDecimal("12"), 2, 0);
                Log.consoleLog(ifr, "value of result ==" + result);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", result.toString());
                String years = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness").toString();
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", years);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, e.getMessage());
        }

    }

    public void ImplSetVerifyPanData(IFormReference ifr, String Control, String Event, String value) {
        try {
            JSONObject obj = cf.getJSonResponseFromAccMethod(ifr, value);
            Log.consoleLog(ifr, "Inside ImplLoadPartyGrid");
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("PanDetails").replaceAll("#PID#", PID);
            String Aadhar = ConfProperty.getQueryScript("AadharDetails").replaceAll("#PID#", PID);
            List<List<String>> executeData = cf.mExecuteQuery(ifr, query, "");
            List<List<String>> executeDatasec = cf.mExecuteQuery(ifr, Aadhar, "");
            if (!executeData.isEmpty()) {
                String fullName = executeData.get(0).get(0);
                String middlename = "";
                String firstname = fullName.substring(0, fullName.indexOf(" "));
                String lastname = fullName.substring(fullName.lastIndexOf(" ") + 1);
                //middlename = "";
                if (fullName.split(" ").length - 1 == 1) {
                    middlename = "";
                }
                if (fullName.split(" ").length - 1 >= 2) {
                    middlename = fullName.substring(fullName.indexOf(" ") + 1, fullName.lastIndexOf(" "));
                }
                ifr.setTableCellValue("LV_VerifyData", 1, 1, firstname);
                Log.consoleLog(ifr, "firstname" + firstname);
                ifr.setTableCellValue("LV_VerifyData", 1, 3, lastname);
                Log.consoleLog(ifr, "lastname" + lastname);
                ifr.setTableCellValue("LV_VerifyData", 1, 2, middlename);
                Log.consoleLog(ifr, middlename);
            }
            if (!executeDatasec.isEmpty()) {
                String fullName = executeDatasec.get(0).get(0);
                String middlename = "";
                String firstname = fullName.substring(0, fullName.indexOf(" "));
                String lastname = fullName.substring(fullName.lastIndexOf(" ") + 1);
                if (fullName.split(" ").length - 1 == 1) {
                    middlename = "";
                }
                if (fullName.split(" ").length - 1 >= 2) {
                    middlename = fullName.substring(fullName.indexOf(" ") + 1, fullName.lastIndexOf(" "));
                }

                ifr.setTableCellValue("LV_VerifyData", 2, 1, firstname);
                Log.consoleLog(ifr, "firstname" + firstname);
                ifr.setTableCellValue("LV_VerifyData", 2, 3, lastname);
                Log.consoleLog(ifr, "lastname" + lastname);
                ifr.setTableCellValue("LV_VerifyData", 2, 2, middlename);
                Log.consoleLog(ifr, middlename);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, e.getMessage());
        }
    }

    public void ImplloadOccupation(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside ImplloadOccupation");

        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String partyType = ifr.getValue("QA_FI_PI_MINCOME_CustomerType").toString();
            String query = ConfProperty.getQueryScript("OccupationTypeValues").replaceAll("#PID#", PID).replaceAll("#ptype#", partyType);
            List<List<String>> executeData = cf.mExecuteQuery(ifr, query, "");
            if (!executeData.isEmpty()) {
                ifr.setValue("QA_FI_PI_MINCOME_IncSOurce", executeData.get(0).get(0));
                ifr.setValue("QA_FI_PI_MINCOME_IncomeType", executeData.get(0).get(1));
            }
        } catch (Exception e) {
            Log.errorLog(ifr, e.getMessage());
        }
    }

    public void ImplCalculationMonthly(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside ImplCalculationMonthly");
        try {

            int size = ifr.getDataFromGrid("ALV_PL_MIncome_Calc").size();
            BigDecimal totalnet = new BigDecimal("0");
            for (int i = 0; i < size; i++) {
                BigDecimal gross = new BigDecimal(ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 1).equalsIgnoreCase("") ? "0.0" : ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 1));
                BigDecimal other = new BigDecimal(ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 3).equalsIgnoreCase("") ? "0.0" : ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 3));
                BigDecimal deduction = new BigDecimal(ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 5).equalsIgnoreCase("") ? "0.0" : ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 5));
                BigDecimal result = (gross.add(other)).subtract(deduction);
                BigDecimal totalinc = gross.add(other);
                totalnet = totalnet.add(result);
                ifr.setTableCellValue("ALV_PL_MIncome_Calc", i, 4, totalinc.toString());
                ifr.setTableCellValue("ALV_PL_MIncome_Calc", i, 6, result.toString());
            }
            Log.consoleLog(ifr, totalnet.toString());
            BigDecimal preresult = totalnet.divide(new BigDecimal(size), 2, 0);
            String Checkvalue = ifr.getValue("QA_FI_PI_MINCOME_IncSOurce").toString();
            if (Checkvalue.equalsIgnoreCase("Salaried") || Checkvalue.equalsIgnoreCase("PEN")) {
                ifr.setValue("QA_FI_PI_MINCOME_NetAmount", preresult.toString());
            } else {
                ifr.setValue("QA_FI_PI_MINCOME_NetAmount", preresult.divide(new BigDecimal(12), 2, 0).toString());
            }
        } catch (Exception e) {
            Log.errorLog(ifr, e.getMessage());
        }

    }

    public String ImplChangeKycvalidate(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplChangeKycvalidate");
        try {
            int count = ifr.getDataFromGrid("LV_KYC").size();
            String checkdata = "select KYCType from LOS_MASTER_KYC where KYCCode = '" + value + "'";
            List<List<String>> executeData = cf.mExecuteQuery(ifr, checkdata, "");
            if (!executeData.isEmpty()) {
                checkdata = executeData.get(0).get(0);
            }
            for (int i = 0; i < count; i++) {
                JSONObject AddressDetailsGridData = null;
                JSONArray AddressDetailsGrid = ifr.getDataFromGrid("LV_KYC");
                AddressDetailsGridData = (JSONObject) AddressDetailsGrid.get(i);
                String KycType = AddressDetailsGridData.get("ID Type").toString();
                if (checkdata.equalsIgnoreCase(KycType)) {
                    ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "");
                    JSONObject message = new JSONObject();
                    message.put("showMessage", this.cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "error", "" + KycType + " is Already Added"));
                    return message.toString();
                }
            }
        } catch (Exception e) {
            Log.errorLog(ifr, e.getMessage());
        }
        return "";
    }

    public void ImplYearormonth(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "ImplYearormonth");
        int size = ifr.getDataFromGrid("ALV_PL_MIncome_Calc").size();
        if (size > 0) {
            Log.consoleLog(ifr, "size===>" + size);
            String Checkvalue = ifr.getValue("QA_FI_PI_MINCOME_IncSOurce").toString();
            Log.consoleLog(ifr, "Checkvalue ==>" + Checkvalue);
            if (Checkvalue.equalsIgnoreCase("Salaried") || Checkvalue.equalsIgnoreCase("PEN")) {
                String type = "Month ".concat(String.valueOf(size));
                ifr.setTableCellValue("ALV_PL_MIncome_Calc", size - 1, 0, type);
            } else {
                String type = "Year ".concat(String.valueOf(size));
                ifr.setTableCellValue("ALV_PL_MIncome_Calc", size - 1, 0, type);
            }
        }
    }

    public void mImplLoadVerificationData(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplLoadVerificationData value:" + value);
        try {
            if (ifr.getDataFromGrid("LV_VerifyData").size() <= 0 && value.split("~")[1].equalsIgnoreCase(ConfProperty.getCommonPropertyValue("VerificationTabName"))) {
                String Mfields = ConfProperty.getCommonPropertyValue("VerificationListValues");
                String[] picklistsepdata = Mfields.split(",");
                int size = picklistsepdata.length;
                JSONArray objArr = new JSONArray();

                for (int i = 0; i < size; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("Parameters", picklistsepdata[i]);
                    objArr.add(obj);
                }
                ifr.addDataToGrid("LV_VerifyData", objArr);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, e.getMessage());
        }
    }

    public String mImplOnChangeSectionStateDeviationBRMS(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "mImplOnClickTabDeviationBRMS");
        String messageValue = "";
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilitiesMaker Budget ");
            String visibleDeviationFields = "";
            pcm.controlvisiblity(ifr, visibleDeviationFields);
            String INvisibleDeviationFields = "QNL_DeviationBRMS_DeviationLevel,QNL_DeviationBRMS_RaisedType,QNL_DeviationBRMS_RaisedComments";
            //pcm.controlDisable(ifr, disableActionFileds);
            pcm.controlinvisiblity(ifr, INvisibleDeviationFields);

        }

        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String fetchDataQuery = ConfProperty.getQueryScript("DeviationQuery").replaceAll("#PID#", PID);
            List<List<String>> data = cf.mExecuteQuery(ifr, fetchDataQuery, "DeviationQuery:");
            if (data.size() > 0) {
                String AgeOfApplicant = data.get(0).get(0);
                if (AgeOfApplicant.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Age Of Property"));
                    return message.toString();
                }
                String OccType = data.get(0).get(1);
                if (OccType.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Occupation Type"));
                    return message.toString();
                }
                String CurrJobExp = data.get(0).get(2);
                if (CurrJobExp.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Current Job Experience"));
                    return message.toString();
                }
                String SchemeID = data.get(0).get(3);
                if (SchemeID.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter SchemeID Details"));
                    return message.toString();
                }
                String LoanTenure = data.get(0).get(4);
                if (LoanTenure.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Tenure Of Loan"));
                    return message.toString();
                }
                String ConcessionInPFee = data.get(0).get(5);
                if (ConcessionInPFee.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Concession In Processing Fees"));
                    return message.toString();
                }
                String ConsessionInROI = data.get(0).get(6);
                if (ConsessionInROI.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Consession In ROI"));
                    return message.toString();
                }
                String AgeOfProperty = data.get(0).get(7);
                if (AgeOfProperty.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Age Of Property"));
                    return message.toString();
                }
                String RiskScore = data.get(0).get(8);
                if (RiskScore.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Risk Score"));
                    return message.toString();
                }
                String ProposalType = data.get(0).get(9);
                if (ProposalType.equalsIgnoreCase("")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Proposal Type"));
                    return message.toString();
                }
                String netamt = ConfProperty.getQueryScript("MonthlyIncome").replaceAll("#PID#", PID);
                List<List<String>> netamtIncome = cf.mExecuteQuery(ifr, netamt, "MonthlyIncome:");
                if (netamtIncome.size() > 0 && !(netamtIncome.get(0).get(0).equalsIgnoreCase(""))) {
                    String IncomeBor = netamtIncome.get(0).get(0);
                    String AgeAtMat = String.valueOf(Integer.valueOf(AgeOfApplicant) + (Integer.valueOf(LoanTenure) / 12));
                    Log.consoleLog(ifr, "Deviation Data AgeAtMat" + AgeAtMat);

                    JSONObject result = cf.executeBRMSRule(ifr, "DeviationRules", AgeOfApplicant + "," + AgeAtMat + "," + AgeOfProperty + "," + "0" + "," + ConsessionInROI + "," + ConcessionInPFee + "," + RiskScore + "," + "0" + "," + IncomeBor + "," + CurrJobExp + "," + OccType + "," + ProposalType + "," + SchemeID);
                    Log.consoleLog(ifr, "BRMS Result of  DeviationRules : " + result);

                    if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                        String[] deviationOP = new String[]{"agematurity_deviation_op", "cibil_deviation_op", "consessionpro_deviation_o", "consessionroi_deviation_o", "creditscore_deviation_op", "deviation_op", "foir_deviation_op", "incomenorm_deviation_op", "jobstabilityexp_deviatn_o", "propertyage_deviation_op"};
                        String[] deviationID = new String[]{"agematurity_devtype_op", "cibil_devtype_op", "consessionpro_devtype_op", "consessionroi_devtype_op", "creditscore_devtype_op", "devtype_op", "foir_devtype_op", "incomenorm_devtype_op", "jobstabilityexp_devtype_o", "propertyage_devtype_op"};

                        String RaisedDev = ConfProperty.getQueryScript("RaisedDev").replaceAll("#PID#", PID);
                        List<List<String>> RaisedDevData = cf.mExecuteQuery(ifr, RaisedDev, "RaisedDev:");

                        JSONArray jsonarray = new JSONArray();
                        for (int i = 0; i < deviationOP.length; i++) {
                            if (cf.getJsonValue(result, deviationOP[i]).equalsIgnoreCase("Yes")) {
                                String devid = cf.getJsonValue(result, deviationID[i]);
                                int flag = 0;
                                for (int y = 0; y < RaisedDevData.size(); y++) {
                                    if (devid.equalsIgnoreCase(RaisedDevData.get(y).get(0))) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag == 0) {
                                    String deviation = ConfProperty.getQueryScript("DEVIATION").replaceAll("#DEVID#", devid);
                                    List<List<String>> deviationdata = cf.mExecuteQuery(ifr, deviation, "");
                                    if (deviationdata.size() > 0) {
                                        JSONObject jsonobj = new JSONObject();
                                        jsonobj.put("QNL_DeviationBRMS_Description", deviationdata.get(0).get(0));
                                        jsonobj.put("QNL_DeviationBRMS_RaisedType", deviationdata.get(0).get(2));
                                        jsonobj.put("QNL_DeviationBRMS_DeviationLevel", deviationdata.get(0).get(1));
                                        jsonobj.put("QNL_DeviationBRMS_DeviationID", deviationdata.get(0).get(3));
                                        jsonobj.put("QNL_DeviationBRMS_DeviationLevelCode", deviationdata.get(0).get(4));
                                        Log.consoleLog(ifr, "$$$$$$$$$$$$$$$$$$$$$$$$$");
                                        jsonarray.add(jsonobj);
                                    } else {
                                        JSONObject message = new JSONObject();
                                        message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Deviation Details not present!"));
                                        return message.toString();
                                    }
                                }
                            }
                        }
                        Log.consoleLog(ifr, "json array is :" + jsonarray);
                        ifr.addDataToGrid("ALV_Deviations", jsonarray);
                        Log.consoleLog(ifr, "added successfully!!!!!!!!!!!");
                    } else {
                        messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                    }
                } else {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Please Enter Monthly Income Details!"));
                    return message.toString();
                }
            } else {
                messageValue = "Please Enter Proposed Loan Data";
            }
            if (!(messageValue.equalsIgnoreCase(""))) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", messageValue));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception Inside mImplOnClickTabDeviationBRMS" + e);
            Log.errorLog(ifr, "Exception Inside mImplOnClickTabDeviationBRMS" + e);
        }
        return "";
    }

    public String mImplRedundantDeviation(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplRedundantDeviation Method");
        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String devdesc = ifr.getValue("QNL_DeviationBRMS_Description").toString();
            //String query = ConfProperty.getQueryScript("ManualDeviation").replaceAll("#PID#", PID).replaceAll("#DEVIATIONS#", devdesc);
            String query = "SELECT distinct A.DEVIATIONS,A.DEVIATIONID,D.ORGANISATIONLEVEL,D.HIERARCHYLEVEL,"
                    + "A.PROPOSEDDEVIATIONLEVEL FROM los_m_deviation A inner join los_m_deviation_scheme B on "
                    + "A.DEVIATIONID=B.DEVID inner join los_nl_proposed_facility C on C.SCHEMEID=B.SCHEMEID "
                    + "inner join LOS_M_DESIGNATION E on A.PROPOSEDDEVIATIONLEVEL=E.GROUPNAME inner join "
                    + "LOS_M_ORGANISATIONAL_HIERARCHY D on E.ORGANIZATIONLEVEL=D.ORGANISATIONLEVEL and "
                    + "A.PROPOSEDDEVIATIONLEVEL=E.GROUPNAME WHERE NatureOfDeviations='Manual' and "
                    + "A.DEVIATIONS='" + devdesc + "' and C.PID='" + PID + "'";
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "");
            if (result.size() > 0) {
                ifr.setValue("QNL_DeviationBRMS_DeviationLevel", result.get(0).get(2));
                ifr.setValue("QNL_DeviationBRMS_DeviationID", result.get(0).get(1));
                ifr.setValue("QNL_DeviationBRMS_DeviationLevelCode", result.get(0).get(3));
                ifr.setValue("QNL_DeviationBRMS_ApprovingAuthority", result.get(0).get(4));
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QNL_DeviationBRMS_Description", "error", "Deviation Details Not Present!"));
                return message.toString();
            }
            String devid = ifr.getValue("QNL_DeviationBRMS_DeviationID").toString();
            Log.consoleLog(ifr, "devid is " + devid);
            String RaisedDev = ConfProperty.getQueryScript("RaisedDev").replaceAll("#PID#", PID);
            List<List<String>> RaisedDevData = cf.mExecuteQuery(ifr, RaisedDev, "");
            Log.consoleLog(ifr, "RaisedDevData is " + RaisedDevData);
            int flag = 0;
            for (int y = 0; y < RaisedDevData.size(); y++) {
                if (devid.equalsIgnoreCase(RaisedDevData.get(y).get(0))) {
                    Log.consoleLog(ifr, "INSIDE IFFF");
                    flag = 1;
                    break;
                }
            }
            if (flag == 1) {
                ifr.setValue("QNL_DeviationBRMS_RaisedType", "");
                ifr.setValue("QNL_DeviationBRMS_Description", "");
                ifr.setValue("QNL_DeviationBRMS_DeviationLevel", "");
                ifr.setValue("QNL_DeviationBRMS_DeviationID", "");
                ifr.setValue("QNL_DeviationBRMS_DeviationLevelCode", "");
                ifr.setValue("QNL_DeviationBRMS_ApprovingAuthority", "");
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QNL_DeviationBRMS_Description", "error", "Same Deviation Is Already Raised!"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplRedundantDeviation Method# " + e);
        }
        return "";
    }

    public String mLoanAssessment(IFormReference ifr, String Control, String Event, String value) {//Added by Jaydeep on 14/07/2023  
        try {
            Log.consoleLog(ifr, "Inside mLoanAssessment method:" + Control);
            ifr.setStyle("QL_LOAN_CALC_LoanAmtRecommended", "disable", "false");
            ifr.setStyle("BTN_FetchLoan", "disable", "false");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, " PID : " + PID);
            //1st Field
            String query = ConfProperty.getQueryScript("LTVAmt").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "query= " + query);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "LTVAmt:");
            Log.consoleLog(ifr, " result : " + result);
            ifr.setValue("QL_LOAN_CALC_AmtAsPerColltMargin", result.get(0).get(0));
            //2nd Field
            String query1 = ConfProperty.getQueryScript("ReqLoanAmt").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "query1= " + query1);
            List<List<String>> result1 = cf.mExecuteQuery(ifr, query1, "ReqLoanAmt:");
            Log.consoleLog(ifr, " result1 : " + result1);
            ifr.setValue("QL_LOAN_CALC_FacilityAmtRequested", result1.get(0).get(0));
            //3rd Field
            String query2 = ConfProperty.getQueryScript("Maxloanamt").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "query2= " + query2);
            List<List<String>> result2 = cf.mExecuteQuery(ifr, query2, "AverageIncm:");
            Log.consoleLog(ifr, " result2 : " + result2);
            ifr.setValue("QL_LOAN_CALC_MaxLoanAmtAsPerScheme", result2.get(0).get(0));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mLoanAssessment method");
        }
        return "";
    }

    public String mLoanAssessmentcalc(IFormReference ifr, String Control, String Event, String value) {//Added by Jaydeep on 14/07/2023  
        try {
            Log.consoleLog(ifr, "Inside mLoanAssessmentcalc method:" + Control);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, " PID : " + PID);
            //4th Field
            String query = ConfProperty.getQueryScript("Quant").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "query= " + query);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Quant: ");
            Log.consoleLog(ifr, " result : " + result);
            ifr.setValue("QL_LOAN_CALC_QntmOfLoanAsPerAmtAvailable", result.get(0).get(0));
            //5th Field
            String Ltv = ifr.getValue("QL_LOAN_CALC_AmtAsPerColltMargin").toString();
            String Req = ifr.getValue("QL_LOAN_CALC_FacilityAmtRequested").toString();
            String Max = ifr.getValue("QL_LOAN_CALC_MaxLoanAmtAsPerScheme").toString();

            BigDecimal ltv = new BigDecimal(Ltv);
            BigDecimal req = new BigDecimal(Req);
            BigDecimal max = new BigDecimal(Max);
            BigDecimal quant = new BigDecimal(result.get(0).get(0));
            BigDecimal min = quant.min(max.min(ltv.min(req)));
            ifr.setValue("QL_LOAN_CALC_EligibleLoanAmt", String.valueOf(min));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mLoanAssessmentcalc method" + e);
            Log.errorLog(ifr, "Exception in  mLoanAssessmentcalc method" + e);
        }

        return "";
    }

    public String mLoanAssessmentmin(IFormReference ifr, String Control, String Event, String value) {//Added by Jaydeep on 14/07/2023  
        try {
            Log.consoleLog(ifr, "Inside mLoanAssessmentcalc method:" + Control);
            String eli = ifr.getValue("QL_LOAN_CALC_EligibleLoanAmt").toString();
            String recom = ifr.getValue("QL_LOAN_CALC_LoanAmtRecommended").toString();
            float elig = Float.parseFloat(eli);
            float recommand = Float.parseFloat(recom);
            if (recommand > elig) {
                ifr.setValue("QL_LOAN_CALC_LoanAmtRecommended", "");
                JSONObject message = new JSONObject();
                message.put("showMessage", this.cf.showMessage(ifr, "QL_LOAN_CALC_LoanAmtRecommended", "error", "Recommended Loan Amount can not be greater then Eligible Loan Amount"));
                return message.toString();
            } else {
                Log.consoleLog(ifr, "Good to Go");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mLoanAssessmentcalc method= ");
        }
        return "";
    }

    public void mImplListviewLoadDeviations(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            if (JSdata.equalsIgnoreCase("A")) {
                ifr.setValue("QNL_DeviationBRMS_RaisedType", "Manual");
            }
            String deviationtype = ifr.getValue("QNL_DeviationBRMS_RaisedType").toString();
            if (deviationtype.equalsIgnoreCase("Auto")) {
                ifr.setStyle("QNL_DeviationBRMS_Description", "disable", "true");
                ifr.setStyle("QNL_DeviationBRMS_DeviationLevel", "disable", "true");
            } else {
                ifr.setStyle("QNL_DeviationBRMS_Description", "disable", "false");
                ifr.setStyle("QNL_DeviationBRMS_DeviationLevel", "disable", "false");
            }
            String nonEditablefields = "QNL_DeviationBRMS_Description,QNL_DeviationBRMS_DeviationLevel,QNL_DeviationBRMS_RaisedType,QNL_DeviationBRMS_RaisedComments,QNL_DeviationBRMS_DeviationID,QNL_DeviationBRMS_DeviationLevelCode,QNL_DeviationBRMS_ApprovingAuthority";
            String ActivityName = ifr.getActivityName();
            if (ActivityName.equalsIgnoreCase("Deviation")) {
                ifr.setStyle("QNL_DeviationBRMS_RaisedComments", "visible", "false");
                ifr.setStyle("QNL_DeviationBRMS_DeviationID", "visible", "false");
                ifr.setStyle("QNL_DeviationBRMS_DeviationLevelCode", "visible", "false");
                pcm.controlDisable(ifr, nonEditablefields);
            }
           
            if (ActivityName.equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, nonEditablefields);
            }
            if (ActivityName.equalsIgnoreCase("Sanction")) {
                pcm.controlDisable(ifr, nonEditablefields);
            }
            if (ActivityName.equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, nonEditablefields);
            }
            if (ActivityName.equalsIgnoreCase("Disbursment maker")) {
                pcm.controlDisable(ifr, nonEditablefields);
            }
            if (ActivityName.equalsIgnoreCase("Disbursment Checker")) {
                pcm.controlDisable(ifr, nonEditablefields);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplListviewLoadDeviations method= " + e);
            Log.errorLog(ifr, "Exception in  mImplListviewLoadDeviations method= " + e);
        }
    }

    public void mImplListviewLoadTermAndCond(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            String termCode = ifr.getValue("QNL_TERMS_AND_CONDITIONS_Code").toString();
            if (!termCode.equalsIgnoreCase("")) {
                ifr.setStyle("QNL_TERMS_AND_CONDITIONS_Termsandconditions", "disable", "true");
            } else {
                ifr.setStyle("QNL_TERMS_AND_CONDITIONS_Termsandconditions", "disable", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mImplListviewLoadTermAndCond method= " + e);
        }
    }

    public void mImplClickFetchCommitteeStatus(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplClickFetchCommitteeStatus:");
        try {
            JSONArray jsonarr = ifr.getDataFromGrid("ALV_COMMITTEE");
            Log.consoleLog(ifr, "jsonarr:" + jsonarr);
            if (jsonarr.size() > 0) {
                for (int i = 0; i < jsonarr.size(); i++) {
                    JSONObject obj = (JSONObject) jsonarr.get(i);
                    String Status = cf.getJsonValue(obj, "QNL_COMMITTEE_COMMITTEESTATUS");
                    if (Status.equalsIgnoreCase("Pending")) {
                        if (!(cf.getJsonValue(obj, "LV_COMMITTEE_CNL_COMMITTEE_DETAILS").equalsIgnoreCase(""))) {
                            JSONArray jsonarr1 = (JSONArray) obj.get("LV_COMMITTEE_CNL_COMMITTEE_DETAILS");
                            if (jsonarr1.size() > 0) {
                                Boolean checkStatus = true;
                                for (int j = 0; j < jsonarr1.size(); j++) {
                                    JSONObject obj1 = (JSONObject) jsonarr1.get(i);
                                    String Status1 = cf.getJsonValue(obj1, "Status");
                                    if (!(Status1.equalsIgnoreCase("Completed"))) {
                                        checkStatus = false;
                                    }
                                }
                                if (checkStatus) {
                                    ifr.setTableCellValue("ALV_COMMITTEE", i, 1, "Completed");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mImplClickFetchCommitteeStatus:" + e);
            Log.errorLog(ifr, "Exception In mImplClickFetchCommitteeStatus:" + e);
        }
    }

    public void mImplOnChangeSectionStateF_Committee(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplOnChangeSectionStateF_Committee:");
        try {
            String maxlevel = ifr.getValue("DevMaxLevel").toString();
            String currentlevel = ifr.getValue("DevCurrentLevel").toString();
            if (maxlevel.equalsIgnoreCase(currentlevel)) {
                if (ifr.getDataFromGrid("ALV_COMMITTEE").size() > 0) {
                } else {
                    JSONArray jsonarr = new JSONArray();
                    JSONObject obj = new JSONObject();
                    String query = ConfProperty.getQueryScript("FindDevLevelName").replaceAll("#DEVLEVEL#", maxlevel);
                    List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelName:");
                    if (result.size() > 0) {
                        obj.put("QNL_COMMITTEE_COMMITTEETYPE", result.get(0).get(0));
                    }
                    obj.put("QNL_COMMITTEE_COMMITTEESTATUS", "Pending");
                    jsonarr.add(obj);
                    ifr.addDataToGrid("ALV_COMMITTEE", jsonarr);
                    mImplClickFetchCommitteeStatus(ifr, Control, Event, JSdata);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mImplOnChangeSectionStateF_Committee:" + e);
            Log.errorLog(ifr, "Exception In mImplOnChangeSectionStateF_Committee:" + e);
        }
    }

    public void ImplCalculationCriteriaROI(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside ImplCalculationCriteriaROI");
        try {
            int size = ifr.getDataFromGrid("LV_OTHERCHKLIST").size();
            Log.consoleLog(ifr, "Size" + size);
            BigDecimal totalnet = new BigDecimal("0");
            for (int i = 0; i < size; i++) {
                if (ifr.getTableCellValue("LV_OTHERCHKLIST", i, 3).equalsIgnoreCase("YES")) {
                    Log.consoleLog(ifr, "Inside Yes");
                    BigDecimal ROI = new BigDecimal(ifr.getTableCellValue("LV_OTHERCHKLIST", i, 2));
                    Log.consoleLog(ifr, "ROI " + ROI);
                    totalnet = totalnet.add(ROI);
                    Log.consoleLog(ifr, "totalnet " + totalnet);
                }
            }
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria", totalnet.toString());
            mAcconChangeConcession(ifr, Control, Event, Event);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in ImplCalculationCriteriaROI " + e.getMessage());
        }

    }

    //Added by yeole for Avg
    public void ImplAvgGrossNet(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside ImplAvgGrossNet");
        try {
            int size = ifr.getDataFromGrid("ALV_PL_MIncome_Calc").size();
            Log.consoleLog(ifr, "Size" + size);
            BigDecimal grossresult = new BigDecimal("0");
            BigDecimal Netresult = new BigDecimal("0");
            BigDecimal AvgGross = new BigDecimal("0");
            BigDecimal AvgNet = new BigDecimal("0");
            for (int i = 0; i < size; i++) {

                {
                    Log.consoleLog(ifr, "Inside For");
                    BigDecimal GrossAvgAmount = new BigDecimal(ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 4));
                    Log.consoleLog(ifr, "GrossAvgAmount " + GrossAvgAmount);
                    AvgGross = AvgGross.add(GrossAvgAmount);
                    Log.consoleLog(ifr, "AvgGross " + AvgGross);
                    BigDecimal NetAvgAmount = new BigDecimal(ifr.getTableCellValue("ALV_PL_MIncome_Calc", i, 6));
                    Log.consoleLog(ifr, "NetAvgAmount " + NetAvgAmount);
                    AvgNet = AvgNet.add(NetAvgAmount);
                    Log.consoleLog(ifr, "AvgNet " + AvgNet);
                    grossresult = AvgGross.divide(new BigDecimal(size), 2, 0);
                    Netresult = AvgNet.divide(new BigDecimal(size), 2, 0);
                }
            }

            ifr.setValue("QA_FI_PI_MINCOME_GrossAnnualAmt", grossresult.toString());
            ifr.setValue("QA_FI_PI_MINCOME_NetAnnualAmt", Netresult.toString());
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in ImplAvgGrossNet " + e.getMessage());
        }

    }

    public String mImplClickInitiateCommittee(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Inside mImplOnChangeSectionStateF_Committee:");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("getLOSCommittee").replaceAll("#PID#", PID);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "getLOSCommittee:");
            if (result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    query = ConfProperty.getQueryScript("getLOSSubCommittee").replaceAll("#F_Key#", result.get(i).get(0));
                    List<List<String>> result1 = cf.mExecuteQuery(ifr, query, "getLOSSubCommittee:");
                    if (result1.size() > 0) {
                        for (int j = 0; j < result1.size(); j++) {
                            String insertionorderid = result1.get(j).get(2);
                            ChildWorkItem cw = new ChildWorkItem();
                            query = ConfProperty.getQueryScript("DecisionCommitteeApprovalQuery");
                            List<List<String>> result2 = cf.mExecuteQuery(ifr, query, "DecisionCommitteeApprovalQuery:");
                            String sOutputXml = cw.childworkitem_FI(ifr, PID, result2.get(0).get(0));
                            WFXmlResponse xmlResponse = new WFXmlResponse(sOutputXml);
                            Log.consoleLog(ifr, "==> Xml Response ==> " + xmlResponse);
                            String mainCode = xmlResponse.getVal("MainCode");
                            Log.consoleLog(ifr, "==> Main Code after Product Call ==> " + mainCode);
                            if ("0".equals(mainCode)) {
                                String workitemid = xmlResponse.getVal("WorkItemId");
                                Log.consoleLog(ifr, "workitemid id is " + workitemid);
                                query = ConfProperty.getQueryScript("updateLOSSubCommittee").
                                        replaceAll("#INSERTIONORDERID#", insertionorderid).replaceAll("#CHILDWORKITEM#", workitemid)
                                        .replaceAll("#COMMITTEESTATUS#", "Initiated");
                                int resultCount = ifr.saveDataInDB(query);
                                Log.consoleLog(ifr, "resultCount" + resultCount);
                            } else {
                                JSONObject message = new JSONObject();
                                message.put("showMessage", this.cf.showMessage(ifr, "", "error", "Committee Child WorkItem Creation Failed!"));
                                return message.toString();
                            }
                        }
                    }
                }
                JSONObject message = new JSONObject();
                message.put("refreshFrame", cf.refreshFrame("F_Committee"));
                message.put("showMessage", this.cf.showMessage(ifr, "", "error", "All Committee Created!"));
                return message.toString();
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", this.cf.showMessage(ifr, "", "error", "No Committee Pending!"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mImplOnChangeSectionStateF_Committee:" + e);
            Log.errorLog(ifr, "Exception In mImplOnChangeSectionStateF_Committee:" + e);
        }
        return "";
    }

    public String mImplConcessionDeatils(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            Log.consoleLog(ifr, "Inside mImplConcessionDeatils");
            int count = ifr.getDataFromGrid("LV_OTHERCHKLIST").size();
            Log.consoleLog(ifr, "count## " + count);
            if (count != 0) {
                for (int i = 0; i < count; i++) {
                    if (ifr.getTableCellValue("LV_OTHERCHKLIST", i, 3).equalsIgnoreCase("YES") || ifr.getTableCellValue("LV_OTHERCHKLIST", i, 3).equalsIgnoreCase("No")) {
                        Log.consoleLog(ifr, "Inside if##");
                    } else {
                        Log.consoleLog(ifr, "Inside else@@");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", this.cf.showMessage(ifr, "LV_OTHERCHKLIST", "error", "Please Select Status Of All Criterias!"));
                        message.put("eflag", "false");
                        return message.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplConcessionDeatils" + e);
            Log.errorLog(ifr, "Exception in mImplConcessionDeatils" + e);
        }
        return "";
    }

    public void mAccFetchExistingODDetails(IFormReference ifr, String Control, String Event, String JSdata) {
        try {
            Log.consoleLog(ifr, "Inside  mAccFetchExistingODDetails method::");
            if (ifr.getDataFromGrid("ALV_ExistingCIFDetailsOD").size() == 1) {
                JSONArray gridData = ifr.getDataFromGrid("ALV_ExistingCIFDetailsOD");
                JSONObject gridjson = (JSONObject) gridData.get(0);
                Log.consoleLog(ifr, "gridDataOD object::" + gridData);
                String cifNumber = gridjson.get("QNL_ExistingCIFDetailsOD_CIFNumber").toString();
                String Exposuretype = gridjson.get("QNL_ExistingCIFDetailsOD_Exposuretype").toString();
                String ODStatus = gridjson.get("QNL_ExistingCIFDetailsOD_ODStatus").toString();
                String query = ConfProperty.getQueryScript("existingODDetils").replaceAll("#cifNumber#", cifNumber).replaceAll("#Exposuretype#", Exposuretype).replaceAll("#ODStatus#", ODStatus);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "existingODDetils:");
                if (result.size() == 1) {
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositType", result.get(0).get(0));
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositNumber", result.get(0).get(1));
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositAmount", result.get(0).get(2));
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositMaturityDate", result.get(0).get(3));
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositRate", result.get(0).get(4));
                    ifr.setValue("QNL_ExistingDepositDetailsOD_DepositMode", result.get(0).get(5));
                }
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccFetchExistingODDetails" + e);
            Log.errorLog(ifr, "Exception in mAccFetchExistingODDetails" + e);
        }
    }

    public void autopopulate_docUpload(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Entered into autopopulate_docUpload==>>>");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Canara Budget")) {
            String query = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> doc = cf.mExecuteQuery(ifr, query, "Execute query for fetching doc details ");
            String appType = doc.get(0).get(0);
            //String updQry="update LOS_DOX_POPNSUPLOAD set value='"+appType+"' where PID='"+ProcessInstanceId+"'";
            String updQry = ConfProperty.getQueryScript("updDocAppType").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#appType#", appType);

            cf.mExecuteQuery(ifr, updQry, "updating appType");
            Log.consoleLog(ifr, "testingg....");

        }
    }

    public void autopopulate_FinalEligibility(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "Entered into==>>>");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        // String query = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        /*    if (loan_selected.equalsIgnoreCase("Canara Pension")) {
         String query1 = "select CUSTOMERID from LOS_NL_BASIC_INFO where APPLICANTTYPE ='B' AND PID='" + ProcessInstanceId + "'";
         List<List<String>> cusID = cf.mExecuteQuery(ifr, query1, "Execute query1 for fetching customer ID ");
         String customerID = cusID.get(0).get(0);
         Log.consoleLog(ifr, "customerID==>" + customerID);
         String query2 = "select GROSS_PENSION,NET_PENSION from LOS_MST_PENSION where CUSTOMER_ID='" + customerID + "'";
         List<List<String>> penData = cf.mExecuteQuery(ifr, query2, "Execute query2 for gross pension");
         String gross_Pension = penData.get(0).get(0);
         String net_Pension = penData.get(0).get(1);
         Integer deduction = Integer.parseInt(gross_Pension) - Integer.parseInt(net_Pension);
         //float netTakeHome =float((25 / 100) * Integer.parseInt(gross_Pension));

         String query4 = "select TENURE,ROI,LOAN_AMOUNT FROM LOS_PEN_PRINCIPLE_APPROVAL where PID='" + ProcessInstanceId + "'";
         List<List<String>> fe_data = cf.mExecuteQuery(ifr, query4, "Execute query4 for eligibility data");
         String tenure = fe_data.get(0).get(0);
         Log.consoleLog(ifr, "Tenure==>" + tenure);
         String roi = fe_data.get(0).get(1);
         Log.consoleLog(ifr, "ROI==>" + roi);
         String loan_amt = fe_data.get(0).get(2);
         double loanAmt = Math.round(Double.parseDouble(loan_amt));
         double takeHomePen = (0.25 * Double.parseDouble(gross_Pension));
         Integer pen_24times = 24 * Integer.parseInt(gross_Pension);

         String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
         Log.consoleLog(ifr, "schemeID:" + schemeID);
         String Prodcapping = null;
         String ProdCapping_Query = "select MAXLOANAMOUNT from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
         List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
         if (ProdcappingList.size() > 0) {
         Prodcapping = ProdcappingList.get(0).get(0);
         }
         Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

         BigDecimal grossPen = new BigDecimal(Integer.parseInt(gross_Pension));
         BigDecimal deduct = new BigDecimal(deduction);
         BigDecimal netTakeHomePen = new BigDecimal(takeHomePen);
         BigDecimal pen6Times = new BigDecimal(pen_24times);
         BigDecimal lacAmount = new BigDecimal(100000);
         BigDecimal netPension = new BigDecimal(net_Pension);
         BigDecimal ftRoi = new BigDecimal(roi);
         BigDecimal prodspcCapping = new BigDecimal(Prodcapping);
         //BigDecimal emiperlc = pcm.calculateEMI(ifr, lacAmount, ftRoi, Integer.parseInt(String.valueOf(tenure)));
         BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, Integer.parseInt(tenure));
         Log.consoleLog(ifr, " emiperlc:::: " + emiperlc);
         BigDecimal loanAmount = new BigDecimal("10000").multiply(netPension).divide(emiperlc, 2, RoundingMode.HALF_UP);
         Log.consoleLog(ifr, "loan amt" + loanAmount);
         BigDecimal finaleligibility = loanAmount.min(pen6Times).min(prodspcCapping);
         Log.consoleLog(ifr, "eligible loan amt==>" + finaleligibility);
         String TotalEmiQuery = "SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK WHERE BUREAUTYPE ='EX' AND PROCESSINSTANCEID ='" + ProcessInstanceId + "'";
         List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");
         String cibiloblig = "";
         if (TotalEmi.size() > 0) {
         cibiloblig = TotalEmi.get(0).get(0).toString();
         Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
         } else {
         cibiloblig = "0.00";
         }
         BigDecimal oblig = new BigDecimal(cibiloblig);
         BigDecimal netPen = grossPen.subtract(deduct).subtract(oblig).subtract(netTakeHomePen);
         Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
         ifr.setValue("FE_Gross_Monthly_Pension", gross_Pension);
         ifr.setValue("FE_Deduction_From_Salary", deduction.toString());
         ifr.setValue("FE_Net_Income", netPen.toString());
         ifr.setValue("FE_NetTakeHome_Pension", Double.toString(takeHomePen));
         ifr.setValue("FE_6Times_GrossPension", pen_24times.toString());
         ifr.setValue("FE_Maximum_Tenure_Product", tenure);
         ifr.setValue("FE_ROI", roi);
         // ifr.setValue("FE_Eligibilie_Loan_Amount", loan_amt);
         ifr.setValue("FE_Product_Specific_Capping", prodspcCapping.toString());
         ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibility.toString());
         ifr.setValue("FE_Loan_Amount_Per_Policy", loanAmount.toString());
         ifr.setValue("FE_Obligations", cibiloblig);

         }
         */
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {

            // String query1 = "select CUSTOMERID from LOS_NL_BASIC_INFO where APPLICANTTYPE ='B' AND PID='" + ProcessInstanceId + "'";
            String query1 = ConfProperty.getQueryScript("PENSIONCUSTID").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

            List<List<String>> cusID = cf.mExecuteQuery(ifr, query1, "Execute query1 for fetching customer ID ");

            String customerID = cusID.get(0).get(0);

            Log.consoleLog(ifr, "customerID==>" + customerID);

            // String query2 = "select GROSS_PENSION,NET_PENSION from LOS_MST_PENSION where CUSTOMER_ID='" + customerID + "'";
            String query2 = ConfProperty.getQueryScript("PensionData").replaceAll("#customerID#", customerID);

            List<List<String>> penData = cf.mExecuteQuery(ifr, query2, "Execute query2 for gross pension");

            String gross_Pension = penData.get(0).get(0);

            String net_Pension = penData.get(0).get(1);

            double deduction = Double.parseDouble(gross_Pension) - Double.parseDouble(net_Pension);

            //float netTakeHome =float((25 / 100) * Integer.parseInt(gross_Pension));
            // String query4 = "select TENURE,ROI,LOAN_AMOUNT FROM LOS_PEN_PRINCIPLE_APPROVAL where PID='" + ProcessInstanceId + "'";
            String query4 = ConfProperty.getQueryScript("PEN_PRINCIPLEData").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

            List<List<String>> fe_data = cf.mExecuteQuery(ifr, query4, "Execute query4 for eligibility data");

            String tenure = fe_data.get(0).get(0);

            Log.consoleLog(ifr, "Tenure==>" + tenure);

            String roi = fe_data.get(0).get(1);

            Log.consoleLog(ifr, "ROI==>" + roi);

            String loan_amt = fe_data.get(0).get(2);

            double loanAmt = Math.round(Double.parseDouble(loan_amt));

            double takeHomePen = (0.4 * Double.parseDouble(gross_Pension));

            Integer pen_24times = 24 * Integer.parseInt(gross_Pension);

            // String schemeID = pcm.mGetSchemeID(ifr);
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);

            String Prodcapping = null;

            //String ProdCapping_Query = "select MAXLOANAMOUNT from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
            String ProdCapping_Query = ConfProperty.getQueryScript("PENSIONMAXLOANAMT").replaceAll("#schemeID#", schemeID);
            List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");

            if (ProdcappingList.size() > 0) {

                Prodcapping = ProdcappingList.get(0).get(0);

            }

            Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

            BigDecimal grossPen = new BigDecimal(Integer.parseInt(gross_Pension));

            //  BigDecimal deduct = new BigDecimal(deduction).setScale(2, RoundingMode.HALF_UP);
            //Added by prakash 25-03-2024 for sonar report
            BigDecimal deduct = BigDecimal.valueOf(deduction).setScale(2, RoundingMode.HALF_UP);

            // BigDecimal netTakeHomePen = new BigDecimal(takeHomePen);
            // BigDecimal netTakeHomePen = new BigDecimal(takeHomePen).setScale(2, RoundingMode.HALF_UP);
            //Added by prakash 25-03-2024 for sonar report
            BigDecimal netTakeHomePen = BigDecimal.valueOf(takeHomePen).setScale(2, RoundingMode.HALF_UP);

            Log.consoleLog(ifr, " roundoff net take home pension:::: " + netTakeHomePen);

            BigDecimal pen6Times = new BigDecimal(pen_24times);

            BigDecimal lacAmount = new BigDecimal(100000);

            BigDecimal netPension = new BigDecimal(net_Pension);

            BigDecimal ftRoi = new BigDecimal(roi);

            BigDecimal prodspcCapping = new BigDecimal(Prodcapping);

            //BigDecimal emiperlc = pcm.calculateEMI(ifr, lacAmount, ftRoi, Integer.parseInt(String.valueOf(tenure)));
            BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, Integer.parseInt(tenure));

            Log.consoleLog(ifr, " emiperlc:::: " + emiperlc);

            // String TotalEmiQuery = "SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK WHERE BUREAUTYPE ='EX' AND PROCESSINSTANCEID ='" + ProcessInstanceId + "'";
            String least_score = "select distinct(BUREAUTYPE), EXP_CBSCORE from los_can_ibps_bureaucheck where PROCESSINSTANCEID='" + ProcessInstanceId + "'";
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

            String TotalEmiQuery = ConfProperty.getQueryScript("getEMIBasedOnScore").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#BT#", bureau_Type);
            List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");

            String cibiloblig = "";

            if (TotalEmi.size() > 0) {

                cibiloblig = TotalEmi.get(0).get(0).toString();

                Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

            } else {

                cibiloblig = "0.00";

            }

            BigDecimal oblig = new BigDecimal(cibiloblig);

            BigDecimal netPen = (grossPen.subtract(deduct).subtract(oblig).subtract(netTakeHomePen)).setScale(2, RoundingMode.HALF_UP);

            BigDecimal loanAmount = new BigDecimal("100000").multiply(netPen).divide(emiperlc, 2, RoundingMode.HALF_UP);

            Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

            //   BigDecimal loanAmount = new BigDecimal("10000").multiply(netPension).divide(emiperlc, 2, RoundingMode.HALF_UP);
            Log.consoleLog(ifr, "loan amt" + loanAmount);
            String prodCode = "PL";
            String subProdCode = "STP-CP";
            String loanoffer = "0";
            HashMap<String, String> loandata = new HashMap<String, String>();
            loandata.put("roi", ftRoi.toString());
            loandata.put("tenure", tenure);
            loandata.put("gross", gross_Pension);
            loandata.put("cibiloblig", cibiloblig);
            loandata.put("loanoffer", loanoffer);
            loandata.put("deduction", deduct.toString());
            loandata.put("loancap", prodspcCapping.toString());

            String finaleligibility = null;
            try {
                finaleligibility = lec.getAmountForEligibilityCheck(ifr, prodCode, subProdCode, loandata);
                Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);

            } catch (Exception ex) {
                Logger.getLogger(AcceleratorActivityManagerCode.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

            // BigDecimal finaleligibility = loanAmount.min(pen6Times).min(prodspcCapping);
            String eligibleAmt = finaleligibility.toString();
            double finaleligibilityDouble = Math.floor(Double.parseDouble(eligibleAmt) / 1000) * 1000;
            String finaleligibilityPen = String.valueOf(Math.round(finaleligibilityDouble));

            Log.consoleLog(ifr, "final eligible loan amt==>" + finaleligibilityPen);

            Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

            ifr.setValue("FE_Gross_Monthly_Pension", gross_Pension);

            ifr.setValue("FE_Deduction_From_Salary", deduct.toString());

            ifr.setValue("FE_Net_Income", netPen.toString());

            ifr.setValue("FE_NetTakeHome_Pension", netTakeHomePen.toString());

            ifr.setValue("FE_6Times_GrossPension", pen_24times.toString());

            ifr.setValue("FE_Maximum_Tenure_Product", tenure);

            ifr.setValue("FE_ROI", roi);

            // ifr.setValue("FE_Eligibilie_Loan_Amount", loan_amt);
            ifr.setValue("FE_Product_Specific_Capping", prodspcCapping.toString());

            ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibilityPen);

            ifr.setValue("FE_Loan_Amount_Per_Policy", loanAmount.toString());

            ifr.setValue("FE_Obligations", cibiloblig);

            String LoanAMTQuery = ConfProperty.getQueryScript("LOS_PORTAL_SLIDERVALUEQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTQuery, "Execute query for fetching Loan amount data from portal in principal");
            String reqLoanAmount = null;
            String LoanAmountStr = null;
            if (LoanAmountList.size() > 0) {
                LoanAmountStr = LoanAmountList.get(0).get(1);
                reqLoanAmount = String.valueOf(Math.round(Double.parseDouble(LoanAmountStr)));
                Log.consoleLog(ifr, "requested loan amt::" + reqLoanAmount);
            }

            ifr.setValue("FE_Requested_Loan_Amount", reqLoanAmount);

        }
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {

            Log.consoleLog(ifr, "Inside Budget Final Eligibility");

            String RequestedLoanAmount = "";
            String LoanAMTTenureQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTTenureQuery, "Execute query for fetching Slider Loan amount,Tenure data from portal in principal");
            if (LoanAmountList.size() > 0) {
                RequestedLoanAmount = LoanAmountList.get(0).get(1);
                Log.consoleLog(ifr, "RequestedLoanAmount :: " + RequestedLoanAmount);
            }
            ifr.setValue("FE_Requested_Loan_Amount", RequestedLoanAmount);
            ifr.setStyle("FE_Requested_Loan_Amount", "disable", "true");

            //String EligibityQuery = "SELECT PID FROM LOS_LIN_FINAL_ELIGIBILITY WHERE PID='" + ProcessInstanceId + "'";
            String EligibityQuery = ConfProperty.getQueryScript("CheckFinalEligibilityBO").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> EligibityCount = cf.mExecuteQuery(ifr, EligibityQuery, "Execute query for fetching customer data");
            Log.consoleLog(ifr, "EligibityCount ::" + EligibityCount.size());
            if (EligibityCount.size() == 0) {
                try {
                    //String IncomeQuery = "SELECT GROSSSALARY, NETSALARY , DEDUCTIONMONTH FROM LOS_NL_Occupation_INFO a INNER JOIN  LOS_NL_BASIC_INFO b ON a.F_KEY=b.F_KEY WHERE b.APPLICANTTYPE ='B' AND b.PID='" + ProcessInstanceId + "'";
                    String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoCB").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching income data from portal");
                    String GrossSalary = IncomeDtsPortal.get(0).get(0);
                    String NETSALARY = IncomeDtsPortal.get(0).get(1);
                    String DeductionMonthly = IncomeDtsPortal.get(0).get(2);
                    Log.consoleLog(ifr, "GROSSSALARY ::" + GrossSalary);
                    Log.consoleLog(ifr, "NETSALARY ::" + NETSALARY);
                    Log.consoleLog(ifr, "DEDUCTIONMONTH ::" + DeductionMonthly);

                    //String TotalEmiQuery = "SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK WHERE BUREAUTYPE ='EX' AND PROCESSINSTANCEID ='" + ProcessInstanceId + "'";
                    String TotalEmiQuery = ConfProperty.getQueryScript("GetExperianTotalEMI").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");

                    String cibiloblig = "";
                    if (TotalEmi.size() > 0) {
                        cibiloblig = TotalEmi.get(0).get(0).toString();
                        Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
                    } else {
                        cibiloblig = "0.00";
                    }
                    Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

                    String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                    Log.consoleLog(ifr, "schemeID:" + schemeID);
                    String loanTenure = null;
                    //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                    String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query FROM PORTAL:");
                    if (list1.size() > 0) {
                        loanTenure = list1.get(0).get(2);
                    }
                    Log.consoleLog(ifr, "loanTenure : " + loanTenure);
//                    String roiID = pcm.mGetRoiID(ifr);
//                    Log.consoleLog(ifr, "roiID:" + roiID);
                    String loanROI = pcm.mGetROICB(ifr);
                    //String roiData_Query = "select totalroi from los_m_roi where roiid='" + roiID + "'";
//                    String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
//                    List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
//                    if (list2.size() > 0) {
//                        loanROI = list2.get(0).get(0);
//                    }
                    Log.consoleLog(ifr, "loanROI : " + loanROI);
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
                        Logger.getLogger(AcceleratorActivityManagerCode.class
                                .getName()).log(Level.SEVERE, null, ex);
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
                    String netsalary = String.valueOf(Math.round(Double.parseDouble(netsalaryBG)));
                    String netIncome = String.valueOf(Math.round(Double.parseDouble(netIncomeBG)));
                    String ftTenure = String.valueOf(Math.round(Double.parseDouble(ftTenureBG)));
                    //String ftRoi = String.valueOf(Math.round(Double.parseDouble(ftRoiBG)));
                    String loanAmount = String.valueOf(Math.round(Double.parseDouble(loanAmountBG)));
                    String sixtimesgrosssal = String.valueOf(Math.round(Double.parseDouble(sixtimesgrosssalBG)));
                    String prodspeccapping = String.valueOf(Math.round(Double.parseDouble(prodspeccappingBG)));
                    //  String finaleligibility = String.valueOf(Math.round(Double.parseDouble(finaleligibilityBG)));

                    double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibilityBG) / 1000) * 1000;
                    String finaleligibility = String.valueOf(Math.round(finaleligibilityDouble));
                    Log.consoleLog(ifr, "After rounded finaleligibility : " + finaleligibility);

                    String prodCode = "PL";
                    String subProdCode = "STP-CB";
                    String loanoffer = "0";
                    HashMap<String, String> loandata = new HashMap<String, String>();
                    loandata.put("roi", loanROI);
                    loandata.put("tenure", loanTenure);
                    loandata.put("gross", GrossSalary);
                    loandata.put("cibiloblig", cibiloblig);
                    loandata.put("loanoffer", loanoffer);
                    loandata.put("deduction", DeductionMonthly);
                    loandata.put("loancap", Prodcapping);

                    String finaleligibilityCommon = null;
                    try {
                        finaleligibilityCommon = lec.getAmountForEligibilityCheck(ifr, prodCode, subProdCode, loandata);
                        Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibilityCommon);

                    } catch (Exception ex) {
                        Logger.getLogger(AcceleratorActivityManagerCode.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    double finaleligibilityDoubled = Math.floor(Double.parseDouble(finaleligibilityCommon) / 1000) * 1000;
                    finaleligibilityCommon = String.valueOf(Math.round(finaleligibilityDoubled));
                    Log.consoleLog(ifr, "After rounded finaleligibilityCommon : " + finaleligibilityCommon);

                    ifr.setValue("FE_Gross_Monthly_Salary", grosssalaryip);
                    ifr.setValue("FE_Deduction_From_Salary", deductionsalary);
                    ifr.setValue("FE_Obligations", cbCibilOblig);
                    ifr.setValue("FE_Net_Home_Salary", netsalary);
                    ifr.setValue("FE_Net_Income", netIncome);
                    ifr.setValue("FE_Maximum_Tenure_Product", ftTenure);
                    ifr.setValue("FE_ROI", ftRoiBG);
                    ifr.setValue("FE_Loan_Amount_Per_Policy", loanAmount);
                    ifr.setValue("FE_Six_Times_Gross_Salary", sixtimesgrosssal);
                    ifr.setValue("FE_Product_Specific_Capping", prodspeccapping);
                    ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibilityCommon);
                    Log.consoleLog(ifr, "After EligibilityDataObj Data setted : ");

                } catch (Exception ex) {
                    Log.consoleLog(ifr, "Exception IN OnLoadFinalEligibility " + ex);
                }
            }
        }
    }

//Modified by Aravindh on 18-06-2024 For populating Liabilities 
    public String autopopulate_liability(IFormReference ifr, String Control, String Event, String JSdata) throws ParseException, java.text.ParseException {
        Log.consoleLog(ifr, "inside autopopulate_liability==>");

        WDGeneralData Data = ifr.getObjGeneralData();
        String PID = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + PID);

        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            int LiabilitySize = ifr.getDataFromGrid("ALV_AL_LIAB_VAL").size();
            Log.consoleLog(ifr, "inside LiabilitySize::" + LiabilitySize);
            if (LiabilitySize == 0) {
                try {
                    Log.consoleLog(ifr, "LiabilitySize::" + LiabilitySize);
                    String responseBody = "";
                    JSONParser parser1 = new JSONParser();

                    String BureauDataResponseBorrower = cm.getMaxTotalEMIAmountCICDatas(ifr, "B");
                    Log.consoleLog(ifr, "BureauDataResponseBorrower ::" + BureauDataResponseBorrower);
                    String[] bSplitter = BureauDataResponseBorrower.split("-");
                    String bureauTypeB = bSplitter[0];
                    String ApplicantTypeB = bSplitter[1];
                    String MaxTotalEmiAmtB = bSplitter[2];
                    String bureauScoreB = bSplitter[3];
                    String PayHistoryCombinedB = bSplitter[4];

                    String BorrowerReturn = bbcc.PopulateCibilExperianLiabilities(ifr, bureauTypeB, ApplicantTypeB);
                    if (BorrowerReturn.contains(RLOS_Constants.ERROR)) {
                        Log.consoleLog(ifr, "Technical glitch in getting Liabilities from response for BORROWER");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Borrower Liabilities!"));
                        return message.toString();
                    }

                    String BureauDataResponseCoborrower = cm.getMaxTotalEMIAmountCICDatas(ifr, "CB");
                    Log.consoleLog(ifr, "BureauDataResponseCoborrower ::" + BureauDataResponseCoborrower);
                    String[] CBSplitter = BureauDataResponseCoborrower.split("-");
                    String bureauTypeCB = CBSplitter[0];
                    String ApplicantTypeCB = CBSplitter[1];
                    String MaxTotalEmiAmtCB = CBSplitter[2];
                    String bureauScoreCB = CBSplitter[3];
                    String PayHistoryCombinedCB = CBSplitter[4];

                    String CoBorrowerReturn = bbcc.PopulateCibilExperianLiabilities(ifr, bureauTypeCB, ApplicantTypeCB);
                    if (CoBorrowerReturn.contains(RLOS_Constants.ERROR)) {
                        Log.consoleLog(ifr, "Technical glitch in getting Liabilities from response for COBORROWER ");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Coborrower Liabilities!"));
                        return message.toString();
                    }

                } catch (Exception e) {
                    Log.consoleLog(ifr, "Exception autopopulate_liability" + e);
                    Log.errorLog(ifr, "Exception autopopulate_liability" + e);
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
                    return message.toString();
                }

            }

        }
        /*if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            Log.consoleLog(ifr, "inside autopopulate_liabilityVL VL==>");
            vlbcc.autopopulate_liabilityVL(ifr, Control, Event, JSdata);
        }*/
        return "";
    }

    public void autopopulate_headerDetails(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside autopopulate_headerDetails==>");
        String activityName = ifr.getActivityName();
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        // String query = "select PORTAL from los_ext_table where PID='" + ProcessInstanceId + "'";
        String query = ConfProperty.getQueryScript("IfPortalQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
        List<List<String>> data = cf.mExecuteQuery(ifr, query, "Execute query for fetching data");
        String isPortal = data.get(0).get(0);
//        if (isPortal.equalsIgnoreCase("Yes")) {
//            ifr.setValue("QL_LEAD_DET_LeadSource", "Portal");
//        }

        if (isPortal.equalsIgnoreCase("Yes")) {
            ifr.setValue("QL_LEAD_DET_LeadSource", "Portal");
        } else {
            ifr.setValue("QL_LEAD_DET_LeadSource", "Walk-In");
        }
        String FirstNameCB = "";
        String MiddleNameCB = "";
        String LastNameCB = "";
        //String FullNameCBQuery = "SELECT a.FIRSTNAME,a.MIDDLENAME,a.LASTNAME FROM LOS_L_BASIC_INFO_I a INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY WHERE b.CUSTOMERFLAG = 'Y' and B.APPLICANTTYPE='B' AND b.PID ='" + ProcessInstanceId + "'";
        String FullNameCBQuery = ConfProperty.getQueryScript("FullNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
        Log.consoleLog(ifr, "query : " + FullNameCBQuery);
        List<List<String>> FullNameCBList = ifr.getDataFromDB(FullNameCBQuery);
        if (!FullNameCBList.isEmpty()) {
            FirstNameCB = FullNameCBList.get(0).get(0);
            MiddleNameCB = FullNameCBList.get(0).get(1);
            LastNameCB = FullNameCBList.get(0).get(2);
            if (!MiddleNameCB.isEmpty()) {
                MiddleNameCB = MiddleNameCB.replace("null", "");
            }
            Log.consoleLog(ifr, "Middle name" + MiddleNameCB);
            if (!LastNameCB.isEmpty()) {
                LastNameCB = LastNameCB.replace("null", "");
            }
            Log.consoleLog(ifr, "last name" + LastNameCB);
            String Fullname = FirstNameCB + " " + MiddleNameCB + " " + LastNameCB;
            Log.consoleLog(ifr, "Full name" + Fullname);
            ifr.setValue("CustomerName", Fullname);

        }
        String employeeID = ifr.getObjGeneralData().getM_strUserName();
        ifr.setValue("QL_SOURCINGINFO_EmployeeId", employeeID);
        String FirstNameEmp = "";
        String LastNameEmp = "";
        String empNameQuery = ConfProperty.getQueryScript("employeeNameQuery").replaceAll("#empID#", employeeID);
        Log.consoleLog(ifr, "empNameQuery : " + empNameQuery);
        List<List<String>> empNameList = ifr.getDataFromDB(empNameQuery);
        if (!empNameList.isEmpty()) {
            FirstNameEmp = empNameList.get(0).get(0);
            LastNameEmp = empNameList.get(0).get(1);
            if (!LastNameEmp.isEmpty()) {
                LastNameEmp = LastNameEmp.replace("null", "");
            }
            Log.consoleLog(ifr, "first name employee ::" + FirstNameEmp);
            Log.consoleLog(ifr, "last name employee ::" + LastNameEmp);
            String FullnameEmp = FirstNameEmp + " " + LastNameEmp;
            Log.consoleLog(ifr, "Full name employee ::" + FullnameEmp);
            ifr.setValue("QL_SOURCINGINFO_EmployeeName", FullnameEmp);
        }
        if (activityName.equalsIgnoreCase("Branch Maker")) {
            String branchcodeQuery = ConfProperty.getQueryScript("branchcodeQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> branchcodeData = cf.mExecuteQuery(ifr, branchcodeQuery, "Execute query for fetching branch code data");
            int branchCode1 = Integer.parseInt(branchcodeData.get(0).get(0));
            String branchCode = String.format("%05d", branchCode1);
            ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
            ifr.setValue("QL_SOURCINGINFO_ParkingBranchCode", branchCode);
            Log.consoleLog(ifr, "branchCode" + branchCode);
            ifr.setValue("Q_ProcessingBranchCode", branchCode);
            Log.consoleLog(ifr, "Q_ProcessingBranchCode::: " + ifr.getValue("Q_ProcessingBranchCode"));
            String branchCodeLeadingZero = String.format("%05d", Integer.parseInt(branchCode));
            Log.consoleLog(ifr, "branchCodeLeadingZero" + branchCodeLeadingZero);
            String branchQuery = ConfProperty.getQueryScript("branchQuery").replaceAll("#branchCode#", branchCodeLeadingZero);
            List<List<String>> branchData = cf.mExecuteQuery(ifr, branchQuery, "Execute query for fetching branch data");
            String branchName = branchData.get(0).get(0);
            ifr.setValue("QL_LEAD_DET_BranchName", branchName);
            ifr.setValue("QL_SOURCINGINFO_Branch", branchName);
            ifr.setValue("QL_SOURCINGINFO_ParkingBranch", branchName);
        }

        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            ifr.setStyle("F_SourcingInfo", "visible", "true");
            ifr.setStyle("ALV_AL_ASSET_DET", "visible", "true");
            ifr.setStyle("ALV_PL_MExpense", "visible", "true");
            ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            ifr.setStyle("F_Fin_Summary", "visible", "true");

        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            ifr.setStyle("F_SourcingInfo", "visible", "true");
            ifr.setStyle("ALV_AL_ASSET_DET", "visible", "true");
            ifr.setStyle("ALV_PL_MExpense", "visible", "true");
            ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            ifr.setStyle("F_Fin_Summary", "visible", "true");

        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            ifr.setStyle("F_SourcingInfo", "visible", "true");
            ifr.setStyle("ALV_AL_ASSET_DET", "visible", "true");
            ifr.setStyle("ALV_PL_MExpense", "visible", "true");
            ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            ifr.setStyle("F_Fin_Summary", "visible", "true");
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            if (Purpose.equalsIgnoreCase("N4W")) {
                String branchcodeQuery = ConfProperty.getQueryScript("branchcodeQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> branchcodeData = cf.mExecuteQuery(ifr, branchcodeQuery, "Execute query for fetching branch code data");
                int branchCode1 = Integer.parseInt(branchcodeData.get(0).get(0));
                String branchCode = String.format("%05d", branchCode1);
                ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
                ifr.setValue("QL_SOURCINGINFO_ParkingBranchCode", branchCode);
                Log.consoleLog(ifr, "branchCode" + branchCode);
                ifr.setValue("Q_ProcessingBranchCode", branchCode);
                Log.consoleLog(ifr, "Q_ProcessingBranchCode::: " + ifr.getValue("Q_ProcessingBranchCode"));
                String branchCodeLeadingZero = String.format("%05d", Integer.parseInt(branchCode));
                Log.consoleLog(ifr, "branchCodeLeadingZero" + branchCodeLeadingZero);
                String branchQuery = ConfProperty.getQueryScript("branchQuery").replaceAll("#branchCode#", branchCodeLeadingZero);
                List<List<String>> branchData = cf.mExecuteQuery(ifr, branchQuery, "Execute query for fetching branch data");
                String branchName = branchData.get(0).get(1);
                branchCode = branchData.get(0).get(2);
                ifr.setValue("QL_LEAD_DET_BranchName", branchName);
                ifr.setValue("QL_SOURCINGINFO_Branch", branchName);
                ifr.setValue("QL_SOURCINGINFO_ParkingBranch", branchName);
                ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
                ifr.setValue("QL_SOURCINGINFO_ParkingBranchCode", branchCode);
            }
        }
    }

    public void autopopulateFinancialInfoIncome(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside autopopulateFinancialInfoIncome==>");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("IfPortalQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

        ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
        try {
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = "";
            if (loanSelected.size() > 0) {
                loan_selected = loanSelected.get(0).get(1);
            }
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
                //String IncomeQuery = "SELECT GROSSSALARY, NETSALARY , DEDUCTIONMONTH FROM LOS_NL_Occupation_INFO a INNER JOIN  LOS_NL_BASIC_INFO b ON a.F_KEY=b.F_KEY WHERE b.APPLICANTTYPE ='B' AND b.PID='" + ProcessInstanceId + "'";
                String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoCB").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching customer data");
                String GROSSSALARY = "";
                String NETSALARY = "";
                String DEDUCTIONMONTH = "";
                if (IncomeDtsPortal.size() > 0) {
                    GROSSSALARY = IncomeDtsPortal.get(0).get(0);
                    NETSALARY = IncomeDtsPortal.get(0).get(1);
                    DEDUCTIONMONTH = IncomeDtsPortal.get(0).get(2);
                }
                Log.consoleLog(ifr, "GROSSSALARY ::" + GROSSSALARY);
                Log.consoleLog(ifr, "NETSALARY ::" + NETSALARY);
                Log.consoleLog(ifr, "DEDUCTIONMONTH ::" + DEDUCTIONMONTH);
                //String queryIncome = "select insertionOrderID from LOS_NL_BASIC_INFO where   Applicanttype='B' and PID='" + ProcessInstanceId + "'";
                String queryIncome = ConfProperty.getQueryScript("GetInsertionOrderIDBasicInfo").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> data = cf.mExecuteQuery(ifr, queryIncome, "Execute query for fetching customer data");
                String party_type = "";
                if (data.size() > 0) {
                    party_type = data.get(0).get(0);
                }
                Log.consoleLog(ifr, "Party Type==>" + party_type);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QA_FI_PI_MINCOME_ConsiderEligibilty", "Yes");
                obj.put("QA_FI_PI_MINCOME_CustomerType", party_type);
                obj.put("QA_FI_PI_MINCOME_IncSource", "Salaried");
                obj.put("QA_FI_PI_MINCOME_GrossAmt", GROSSSALARY);
                obj.put("QA_FI_PI_MINCOME_NetAmount", NETSALARY);
                obj.put("QA_FI_PI_MINCOME_Deduction", DEDUCTIONMONTH);
                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                //String checkQuery = " select PID from LOS_NL_PL_MIncome where PID='" + ProcessInstanceId + "'";
                String checkQuery = ConfProperty.getQueryScript("CheckIncomeGridCountBO").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> checkQueryData = cf.mExecuteQuery(ifr, checkQuery, "Execute query for fetching customer data");
                if (checkQueryData.size() == 0) {
                    ifr.addDataToGrid("ALV_PL_MIncome", jsonarr);
                    Log.consoleLog(ifr, "Income from portal Added==>");
                }
            }
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                // String queryData = "select GROSS_PENSION,NET_PENSION from LOS_MST_PENSION where CUSTOMER_ID in (select CUSTOMERID from LOS_NL_BASIC_INFO where PID='" + ProcessInstanceId + "')";
                String query1 = ConfProperty.getQueryScript("PENSIONCUSTID").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> cusID = cf.mExecuteQuery(ifr, query1, "Execute query1 for fetching customer ID ");
                String customerID = cusID.get(0).get(0);
                Log.consoleLog(ifr, "customerID==>" + customerID);
                String queryData = ConfProperty.getQueryScript("PensionData").replaceAll("#customerID#", customerID);
                List<List<String>> penData = cf.mExecuteQuery(ifr, queryData, "Execute queryData for gross pension");
                String gross_Pension = penData.get(0).get(0);
                String net_Pension = penData.get(0).get(1);
                Integer deduction = Integer.parseInt(gross_Pension) - Integer.parseInt(net_Pension);
                Log.consoleLog(ifr, "GROSSSPENSION ::" + gross_Pension);
                Log.consoleLog(ifr, "NETPENSION ::" + net_Pension);
                Log.consoleLog(ifr, "Deduction ::" + deduction);
                //String queryIncome = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b \n"
                //   + "inner JOIN LOS_NL_BASIC_INFO c  ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + ProcessInstanceId + "' AND  c.ApplicantType ='B'";
                String queryIncome = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

                List<List<String>> data = cf.mExecuteQuery(ifr, queryIncome, "Execute query for fetching customer data");
                String party_type = data.get(0).get(0);
                Log.consoleLog(ifr, "Party Type==>" + party_type);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QA_FI_PI_MINCOME_ConsiderEligibilty", "Yes");
                obj.put("QA_FI_PI_MINCOME_CustType", party_type);
                //bj.put("QA_FI_PI_MINCOME_ConsiderEligibilty", "");
                obj.put("QA_FI_PI_MINCOME_GrossAmt", gross_Pension);
                obj.put("QA_FI_PI_MINCOME_NetAmount", net_Pension);
                obj.put("QA_FI_PI_MINCOME_Deduction", deduction);
                obj.put("QA_FI_PI_MINCOME_IncSource", "Retired");
                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                //String checkQuery = " select PID from LOS_NL_PL_MIncome where PID='" + ProcessInstanceId + "'";
                String checkQuery = ConfProperty.getQueryScript("CheckIncomeGridCountBO").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> checkQueryData = cf.mExecuteQuery(ifr, checkQuery, "Execute query for fetching customer data");
                if (checkQueryData.size() == 0) {
                    ifr.addDataToGrid("ALV_PL_MIncome", jsonarr, true);
                    Log.consoleLog(ifr, "Income from portal Added==>");
                }//modified by logaraj on 08/07/2024
                ifr.setStyle("ALV_AL_NETWORTH", "visible", "true");
            }
            if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
//modified by ishwarya on 15-07-2024
                String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
                        + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
                        + "WHERE c.PID = '" + ProcessInstanceId + "' "
                        + " and (c.ApplicantType='B' or c.ApplicantType='CB')";

                Log.consoleLog(ifr, "Query1 data::" + Query1);
                List<List<String>> resultData = ifr.getDataFromDB(Query1);
                Log.consoleLog(ifr, "resultData::" + resultData.toString());

                boolean BorrowerAdded = false;
                boolean coBorrowerAdded = false;

                for (List<String> rowData : resultData) {
                    Log.consoleLog(ifr, "rowData : " + rowData);
                    String brdata = rowData.get(0);
                    Log.consoleLog(ifr, "brdata : " + brdata);
                    if (!BorrowerAdded) {
                        Log.consoleLog(ifr, "inside !BorrowerAdded " + brdata.toLowerCase() + "::::" + brdata);
                        String f_key = Bpcm.Fkey(ifr, "B");
                        String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoVL").replaceAll("#f_key#", f_key);
                        List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching customer data");
                        String GROSSSALARY = "";
                        String NETSALARY = "";
                        String DEDUCTIONMONTH = "";
                        String PROFILE = "";
                        if (IncomeDtsPortal.size() > 0) {
                            PROFILE = IncomeDtsPortal.get(0).get(0);
                            GROSSSALARY = IncomeDtsPortal.get(0).get(1);
                            NETSALARY = IncomeDtsPortal.get(0).get(2);
                            DEDUCTIONMONTH = IncomeDtsPortal.get(0).get(3);
                        }
                        Log.consoleLog(ifr, "PROFILE ::" + PROFILE);
                        Log.consoleLog(ifr, "GROSSSALARY ::" + GROSSSALARY);
                        Log.consoleLog(ifr, "NETSALARY ::" + NETSALARY);
                        Log.consoleLog(ifr, "DEDUCTIONMONTH ::" + DEDUCTIONMONTH);

                        JSONObject obj = new JSONObject();
                        JSONArray jsonarr = new JSONArray();

                        if (PROFILE.equalsIgnoreCase("PROF") || PROFILE.equalsIgnoreCase("SELF")) {
                            Log.consoleLog(ifr, "inside profile ::" + PROFILE);
                            int grosssal = Integer.parseInt(GROSSSALARY) / 12;
                            int deduction = Integer.parseInt(DEDUCTIONMONTH) / 12;
                            int netsal = Integer.parseInt(NETSALARY) / 12;
                            Log.consoleLog(ifr, "grosssal ::" + grosssal);
                            Log.consoleLog(ifr, "netsal ::" + netsal);
                            Log.consoleLog(ifr, "deduction ::" + deduction);
                            obj.put("QA_FI_PI_MINCOME_GrossAmt", grosssal);
                            obj.put("QA_FI_PI_MINCOME_NetAmount", netsal);
                            obj.put("QA_FI_PI_MINCOME_Deduction", deduction);
                        } else {
                            obj.put("QA_FI_PI_MINCOME_GrossAmt", GROSSSALARY);
                            obj.put("QA_FI_PI_MINCOME_NetAmount", NETSALARY);
                            obj.put("QA_FI_PI_MINCOME_Deduction", DEDUCTIONMONTH);
                        }
                        obj.put("QA_FI_PI_MINCOME_ConsiderEligibilty", "Yes");
                        obj.put("QA_FI_PI_MINCOME_CustomerType", brdata);
                        obj.put("QA_FI_PI_MINCOME_IncSource", PROFILE);

                        jsonarr.add(obj);
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                        String checkQuery = ConfProperty.getQueryScript("CheckIncomeGridCountBO").replaceAll("#PID#", ProcessInstanceId);
                        List<List<String>> checkQueryData = cf.mExecuteQuery(ifr, checkQuery, "Execute query for fetching customer data");
                        Log.consoleLog(ifr, "checkQueryData ::" + checkQueryData);
                        if (checkQueryData.isEmpty()) {
                            ifr.addDataToGrid("ALV_PL_MIncome", jsonarr);
                            Log.consoleLog(ifr, "Income from portal Added==>" + jsonarr);
                        }
                        BorrowerAdded = true;
                    } else if (!coBorrowerAdded) {
                        Log.consoleLog(ifr, "inside !coBorrowerAdded " + brdata.toLowerCase() + "::::" + brdata);
                        String f_key = Bpcm.Fkey(ifr, "CB");
                        String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoVL").replaceAll("#f_key#", f_key);
                        List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching customer data");
                        String GROSSSALARY = "";
                        String NETSALARY = "";
                        String DEDUCTIONMONTH = "";
                        String PROFILE = "";
                        if (IncomeDtsPortal.size() > 0) {
                            PROFILE = IncomeDtsPortal.get(0).get(0);
                            GROSSSALARY = IncomeDtsPortal.get(0).get(1);
                            NETSALARY = IncomeDtsPortal.get(0).get(2);
                            DEDUCTIONMONTH = IncomeDtsPortal.get(0).get(3);
                        }
                        Log.consoleLog(ifr, "PROFILE ::" + PROFILE);
                        Log.consoleLog(ifr, "GROSSSALARY ::" + GROSSSALARY);
                        Log.consoleLog(ifr, "NETSALARY ::" + NETSALARY);
                        Log.consoleLog(ifr, "DEDUCTIONMONTH ::" + DEDUCTIONMONTH);

                        JSONObject obj = new JSONObject();
                        JSONArray jsonarr = new JSONArray();

                        if (PROFILE.equalsIgnoreCase("PROF") || PROFILE.equalsIgnoreCase("SELF")) {
                            Log.consoleLog(ifr, "inside profile ::" + PROFILE);
                            int grosssal = Integer.parseInt(GROSSSALARY) / 12;
                            int deduction = Integer.parseInt(DEDUCTIONMONTH) / 12;
                            int netsal = Integer.parseInt(NETSALARY) / 12;
                            Log.consoleLog(ifr, "grosssal ::" + grosssal);
                            Log.consoleLog(ifr, "netsal ::" + netsal);
                            Log.consoleLog(ifr, "deduction ::" + deduction);
                            obj.put("QA_FI_PI_MINCOME_GrossAmt", grosssal);
                            obj.put("QA_FI_PI_MINCOME_NetAmount", netsal);
                            obj.put("QA_FI_PI_MINCOME_Deduction", deduction);
                        } else {
                            obj.put("QA_FI_PI_MINCOME_GrossAmt", GROSSSALARY);
                            obj.put("QA_FI_PI_MINCOME_NetAmount", NETSALARY);
                            obj.put("QA_FI_PI_MINCOME_Deduction", DEDUCTIONMONTH);
                        }
                        obj.put("QA_FI_PI_MINCOME_ConsiderEligibilty", "Yes");
                        obj.put("QA_FI_PI_MINCOME_CustomerType", brdata);
                        obj.put("QA_FI_PI_MINCOME_IncSource", PROFILE);

                        jsonarr.add(obj);
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                        String checkQuery = ConfProperty.getQueryScript("CheckIncomeGridCountBO").replaceAll("#PID#", ProcessInstanceId);
                        List<List<String>> checkQueryData = cf.mExecuteQuery(ifr, checkQuery, "Execute query for fetching customer data");
                        Log.consoleLog(ifr, "checkQueryData ::" + checkQueryData);
                        if (checkQueryData.isEmpty()) {
                            ifr.addDataToGrid("ALV_PL_MIncome", jsonarr);
                            Log.consoleLog(ifr, "Income from portal Added==>" + jsonarr);
                        }
                        coBorrowerAdded = true;
                    } else {
                        Log.consoleLog(ifr, "party type is null");
                    }
                }
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception IN autopopulateFinancialInfoIncome " + ex);
        }
    }

    public void OnLoadOccupationDetails(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside OccupationDetails==>");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        // Added by kathir for pension lead capture occupation info population on 07-08-2024
        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "AcceleratorActivityManager:OnLoadOccupationDetails -> activityName: " + activityName);
        if (activityName.equalsIgnoreCase("Lead Capture")) {
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "visible", "true");
            ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "visible", "true");
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                pbcc.pensionOccPopulateLC(ifr);
                return;
            }
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                Log.consoleLog(ifr, "Inside Ocuupation Canara Budget");
                String nonVisibleFileds = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentEmployment,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameAddrPensionDept,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LandholdingAcrs,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NatureOfLand,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PlaceOfAgriculturalLand,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentCroppingPattern,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ConstitutionType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EstablishmentDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustryType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustrySubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LastApprovalDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_AnnualReviewDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationNo,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExpiryDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoOfEmployees,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CountryOfIncorporation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactEmailID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonEmailID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_BusinessActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofCrop,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ScaleofFinance,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EligibleLoanAmount,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NumberofUnits,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TotalEligibleAmount,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome,table565_table388_relationshiPBborrower,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType,table565_table388_RelationshipCanara,table565_table388_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension";
                pcm.controlinvisiblity(ifr, nonVisibleFileds);
            }

        }
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "loan type====Canara Budget=>");
            String[] FieldVisibleFalse = new String[]{
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID",
                // "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity",
                //                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName",
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
                //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg",
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
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths",
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
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType",
                "table565_table388_RelationshipCanara",
                "table565_table388_OccupationType",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose", // Purpose Not mentioned in May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing", // Modified by Aravindh By refering May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", // Modified by Aravindh By refering May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", // Modified by Aravindh By refering May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", // Modified by Aravindh By refering May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile", // Modified by Aravindh By refering May22 FSD
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExperienceWithCurrentEmployer",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension"
            };
            for (int i = 0; i < FieldVisibleFalse.length; i++) {
                ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
            }
            String[] FieldVisibleMandatoryDisable = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status", // ID for Employment Status
                //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose",  
                //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing", // 
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome",
                //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName",
                // "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfMonthsInCurOrg", //"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", 
        };

            for (int i = 0; i < FieldVisibleMandatoryDisable.length; i++) {
                ifr.setStyle(FieldVisibleMandatoryDisable[i], "visible", "true");
                ifr.setStyle(FieldVisibleMandatoryDisable[i], "disable", "true");
                ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "true");
            }
            //Added by Aravindh ON 6/6/24

            String[] FieldVisibleEditable = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentContractBasis",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentTransferrable",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MobilityOfIndividual",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ProofOfIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_StabilityOfIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome",
                "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly"};
//            if (activityName.equalsIgnoreCase("Lead Capture")) {
//                for (int i = 0; i < FieldVisibleFalse.length; i++) {
//                    ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
//                }
//                for (int i = 0; i < FieldVisibleEditable.length; i++) {
//                    ifr.setStyle(FieldVisibleEditable[i], "visible", "true");
//                    ifr.setStyle(FieldVisibleEditable[i], "disable", "false");
//                }
//                String OccupationType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();
//                for (int i = 0; i < FieldVisibleEditable.length; i++) {
//                    if (OccupationType.equalsIgnoreCase("Salaried")) {
//                        ifr.setStyle(FieldVisibleEditable[i], "mandatory", "true");
//                    } else {
//                        ifr.setStyle(FieldVisibleEditable[i], "mandatory", "false");
//
//                    }
//
//                }
//            }
            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                for (int i = 0; i < FieldVisibleEditable.length; i++) {
                    ifr.setStyle(FieldVisibleEditable[i], "visible", "true");
                    ifr.setStyle(FieldVisibleEditable[i], "disable", "false");
                }
                Log.consoleLog(ifr, " Budget occupation Details editable fields Branch Maker");
            } else {
                for (int i = 0; i < FieldVisibleEditable.length; i++) {
                    ifr.setStyle(FieldVisibleEditable[i], "visible", "true");
                    ifr.setStyle(FieldVisibleEditable[i], "disable", "true");
                }
                Log.consoleLog(ifr, " Budget occupation Details editable fields to Non Editable => Other than Branch Maker");
            }

            String OccupationType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();
            for (int i = 0; i < FieldVisibleEditable.length; i++) {
                if (OccupationType.equalsIgnoreCase("Salaried")) {
                    ifr.setStyle(FieldVisibleEditable[i], "mandatory", "true");
                } else {
                    ifr.setStyle(FieldVisibleEditable[i], "mandatory", "false");

                }

            }
            Log.consoleLog(ifr, " Budget occupation Details fields visible hide end");
        } //Added by Priya 
        /*else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
 
         Log.consoleLog(ifr, "Inside Pension chkkk");
         String[] pension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly"};
         String[] nonPension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo", "table565_table388_relationshiPBborrower"};
         for (int i = 0; i < pension.length; i++) {
         ifr.setStyle(pension[i], "visible", "true");
         ifr.setStyle(pension[i], "mandatory", "true");
         }
         for (int i = 0; i < nonPension.length; i++) {
         ifr.setStyle(nonPension[i], "visible", "false");
         ifr.setValue(nonPension[i], "");
         }
 
         }*/ else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "Inside Pension chkkk");
            String PartyType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            Log.consoleLog(ifr, "PartyType ::" + PartyType);
            if (PartyType.equalsIgnoreCase("B")) {
                Log.consoleLog(ifr, "BorrowerType");
                String FkeyCB = Bpcm.Fkey(ifr, PartyType);
                String deduction = "";
                String deductionCBQ = "SELECT DEDUCTIONMONTH FROM los_nl_occupation_info where F_KEY = '" + FkeyCB + "'";
                Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + deductionCBQ);
                List<List<String>> deductionCB = cf.mExecuteQuery(ifr, deductionCBQ, "occupation Fkey query Co-Obligant ");

                Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + deductionCB);
                if (!deductionCB.isEmpty()) {
                    deduction = deductionCB.get(0).get(0);
                }
                Log.consoleLog(ifr, "OccuppationInfoDetails deduction::" + deduction);

                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                String pension1 = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly";
                pcm.controlDisable(ifr, pension1);
                pcm.controlvisiblity(ifr, pension1);
                //  String[] pension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly"};
                //  String[] nonPension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing"};
                String[] nonPension = new String[]{"QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExperienceWithCurrentEmployer", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_StabilityOfIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ProofOfIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MobilityOfIndividual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentTransferrable", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentContractBasis", "table565_table388_RelationshipCanara", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfMonthsInCurOrg", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile_label", "table565_table388_relationshiPBborrower", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentEmployment", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameAddrPensionDept", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LandholdingAcrs", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NatureOfLand", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PlaceOfAgriculturalLand", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentCroppingPattern", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ConstitutionType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EstablishmentDate", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustryType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustrySubType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LastApprovalDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_AnnualReviewDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationNo", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExpiryDT", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoOfEmployees", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CountryOfIncorporation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactEmailID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonEmailID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_BusinessActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofCrop", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ScaleofFinance", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EligibleLoanAmount", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NumberofUnits", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TotalEligibleAmount", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome", "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Residing"};
//                for (int i = 0; i < pension.length; i++) {
//                    ifr.setStyle(pension[i],"visible","true");
//                    ifr.setStyle(pension[i],"disable","true");
//                    ifr.setStyle(pension[i],"mandatory","true");
//                }
                for (int i = 0; i < nonPension.length; i++) {
                    ifr.setStyle(nonPension[i], "visible", "false");
                    ifr.setValue(nonPension[i], "");
                }

            }
            if (PartyType.equalsIgnoreCase("CB")) {
                pbcc.hideOccupationInfoFields(ifr);
                String FkeyCB = Bpcm.Fkey(ifr, PartyType);
                String deduction = "";
                String OccType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();
                if (OccType.equalsIgnoreCase("Salaried") || OccType.equalsIgnoreCase("PROF") || (OccType.equalsIgnoreCase("SELF"))) {
                    String deductionCBQ = "SELECT DEDUCTIONMONTHLY FROM los_nl_occupation_info where F_KEY = '" + FkeyCB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + deductionCBQ);
                    List<List<String>> deductionCB = cf.mExecuteQuery(ifr, deductionCBQ, "occupation Fkey query Co-Obligant ");

                    Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + deductionCB);
                    if (!deductionCB.isEmpty()) {
                        deduction = deductionCB.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "OccuppationInfoDetails deduction::" + deduction);
                    ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", "visible", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension", "visible", "false");
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                }
                if (OccType.equalsIgnoreCase("PEN")) {
                    String deductionCBQ = "SELECT DEDUCTIONMONTH FROM los_nl_occupation_info where F_KEY = '" + FkeyCB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + deductionCBQ);
                    List<List<String>> deductionCB = cf.mExecuteQuery(ifr, deductionCBQ, "occupation Fkey query Co-Obligant ");

                    Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + deductionCB);
                    if (!deductionCB.isEmpty()) {
                        deduction = deductionCB.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "OccuppationInfoDetails deduction::" + deduction);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);

                }

            }

        } else if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            Log.consoleLog(ifr, "loan type====Vechicle loan>");
            String PartyType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            if (PartyType.equalsIgnoreCase("B")) {
                String f_key = Bpcm.Fkey(ifr, "B");
                String occupationType = null;
                String occupationSub = null;
                String occupationquery = "select PROFILE,OCCUPATIONTYPE from LOS_NL_Occupation_INFO where  F_KEY='" + f_key + "'";
                List<List<String>> Occupationdetails = cf.mExecuteQuery(ifr, occupationquery, "Execute query for fetching Purpose data from portal");
                if (!Occupationdetails.isEmpty()) {
                    Log.consoleLog(ifr, "inside proposed grid ");
                    occupationType = Occupationdetails.get(0).get(0);
                    occupationSub = Occupationdetails.get(0).get(1);
                }
                Log.consoleLog(ifr, "occupation..." + occupationType);
                Log.consoleLog(ifr, "occupationSub..." + occupationSub);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
            } else if (PartyType.equalsIgnoreCase("CB")) {
                String f_key = Bpcm.Fkey(ifr, "CB");
                String occupationType = null;
                String occupationSub = null;
                String occupationquery = "select PROFILE,OCCUPATIONTYPE from LOS_NL_Occupation_INFO where  F_KEY='" + f_key + "'";
                List<List<String>> Occupationdetails = cf.mExecuteQuery(ifr, occupationquery, "Execute query for fetching Purpose data from portal");
                if (!Occupationdetails.isEmpty()) {
                    Log.consoleLog(ifr, "inside proposed grid ");
                    occupationType = Occupationdetails.get(0).get(0);
                    occupationSub = Occupationdetails.get(0).get(1);
                }
                Log.consoleLog(ifr, "occupation CB..." + occupationType);
                Log.consoleLog(ifr, "occupationSub CB..." + occupationSub);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
            }

            String FieldVisibleFalse = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionSalaryPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployeeType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmployerName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentStatus,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OtherDesignation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementAge,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ResidualPeriod,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentEmployment,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameAddrPensionDept,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LandholdingAcrs,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NatureOfLand,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PlaceOfAgriculturalLand,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PresentCroppingPattern,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ConstitutionType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EstablishmentDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustryType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_IndustrySubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_LastApprovalDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_AnnualReviewDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationNo,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RegistrationDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExpiryDT,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoOfEmployees,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CountryOfIncorporation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmailAddress,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OfficeNo,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactEmailID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonEmailID,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_BusinessActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CurrentInMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofCrop,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ScaleofFinance,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EligibleLoanAmount,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NameofActivity,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NumberofUnits,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TotalEligibleAmount,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome,table565_table388_relationshiPBborrower,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EntityName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_PartyType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Profile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_CompanyName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Purpose,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetPension,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Category,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfMonthsInCurOrg"
                    + ",QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ExperienceWithCurrentEmployer,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentContractBasis,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_EmploymentTransferrable,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MobilityOfIndividual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ProofOfIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_StabilityOfIncome";

            pcm.controlinvisiblity(ifr, FieldVisibleFalse);

            String FieldVisibleMandatoryDisable = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossMonthlyIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_MonthlyIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TypeOfOcc,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate";

            pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
            pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
            pcm.controlMandatory(ifr, FieldVisibleMandatoryDisable);

            //ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "true");
            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
                pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
                pcm.controlinvisiblity(ifr, FieldVisibleFalse);
                Log.consoleLog(ifr, " Vechicle Loan occupation Details:: VL Code End Branch Checker");
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
                pcm.controlinvisiblity(ifr, FieldVisibleFalse);
                pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
                Log.consoleLog(ifr, " Vechicle Loan occupation Details:: VL Code End PostSanction");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
                pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
                pcm.controlinvisiblity(ifr, FieldVisibleFalse);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                Log.consoleLog(ifr, " Vechicle Loan occupation Details:: VL Code End Sanction");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
                pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
                pcm.controlinvisiblity(ifr, FieldVisibleFalse);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                Log.consoleLog(ifr, " Vechicle Loan occupation Details:: VL Code End Disbursement Maker");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Post Disbursement Doc Upload")) {
                Log.consoleLog(ifr, " enter into AcceleratorActivityManagerCode :: onLoadOccupationDetails:: VL :: PDD");
                pcm.controlvisiblity(ifr, FieldVisibleMandatoryDisable);
                pcm.controlDisable(ifr, FieldVisibleMandatoryDisable);
                pcm.controlinvisiblity(ifr, FieldVisibleFalse);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                Log.consoleLog(ifr, " end AcceleratorActivityManagerCode :: onLoadOccupationDetails:: VL :: PDD");
            }
            ifr.setStyle("LV_OCCUPATION_INFO", "disable", "true");
            if (PartyType.equalsIgnoreCase("B")) {
                ifr.setStyle("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate", "disable", "true");
            } else if (PartyType.equalsIgnoreCase("CB")) {
                String inVisibleFields = "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_RetirementDate,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Designation,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_Status";
                pcm.controlinvisiblity(ifr, inVisibleFields);
            }
            ifr.setStyle("savechanges_LV_OCCUPATION_INFO", "disable", "true");
            Log.consoleLog(ifr, " Vechicle Loan occupation Details:: VL Code End");

        }

    }

    public void OnLoadAddressDetails(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside OnLoadAddressDetails==>");
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                String[] FieldVisibleFalse = new String[]{"QNL_BASIC_INFO_CNL_CUST_ADDRESS_CommunicationAddress", "Same_As_Permanent_Address", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", "BTN_Address_Fetch", "Copy_Address_From", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_District", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_PeriodOfCurrentStay"};
                for (int i = 0; i < FieldVisibleFalse.length; i++) {
                    ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                }
                String[] FieldVisibleMandatoryDisable = new String[]{"QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line1", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line2", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Line3", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_PinCode", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_City_Town_Village", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_State", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country"};
                for (int i = 0; i < FieldVisibleMandatoryDisable.length; i++) {
                    ifr.setStyle(FieldVisibleMandatoryDisable[i], "visible", "true");
                    ifr.setStyle(FieldVisibleMandatoryDisable[i], "disable", "true");
                    ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "true");
                }
                ifr.setStyle("QNL_BASIC_INFO_CNL_CUST_ADDRESS_AdressType", "disable", "true");
                Log.consoleLog(ifr, " Budget Address Details fields visible hide end");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in OnLoadAddressDetails::" + e);
            Log.errorLog(ifr, "Exception in OnLoadAddressDetails::" + e);
        }
    }

    public String OnChangePerfiosDataFE(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside OnChangePerfiosDataFE==>");

        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        try {
            //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                String GrossSalary = ifr.getValue("FE_Gross_Monthly_Salary").toString();
                Log.consoleLog(ifr, "GrossSalary::" + GrossSalary);
                if (GrossSalary.equalsIgnoreCase("") || GrossSalary.equalsIgnoreCase("0") || GrossSalary.isEmpty()) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Gross Salary Amount!"));
                    return message.toString();
                }

                String DeductionMonthly = ifr.getValue("FE_Deduction_From_Salary").toString();
                Log.consoleLog(ifr, "DeductionMonthly::" + DeductionMonthly);
                if (DeductionMonthly.equalsIgnoreCase("") || DeductionMonthly.isEmpty()) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Deduction Monthly Amount!"));
                    return message.toString();
                }
                String cibiloblig = ifr.getValue("FE_Obligations").toString();
                Log.consoleLog(ifr, "cibiloblig::" + cibiloblig);
                if (cibiloblig.equalsIgnoreCase("") || cibiloblig.isEmpty()) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Obiligation Amount!"));
                    return message.toString();
                }
                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String loanTenure = null;
                //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query FROM PORTAL:");
                if (list1.size() > 0) {
                    loanTenure = list1.get(0).get(2);
                }
                Log.consoleLog(ifr, "loanTenure : " + loanTenure);
//                String roiID = pcm.mGetRoiID(ifr);
//                Log.consoleLog(ifr, "roiID:" + roiID);

                //String roiData_Query = "select totalroi from los_m_roi where roiid='" + roiID + "'";
//                String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
//                List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
//                if (list2.size() > 0) {
//                    loanROI = list2.get(0).get(0);
//                }
                String loanROI = pcm.mGetROICB(ifr);
                Log.consoleLog(ifr, "loanROI : " + loanROI);
                String Prodcapping = null;
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
                    Logger.getLogger(AcceleratorActivityManagerCode.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                Log.consoleLog(ifr, "EligibilityDataObj : " + EligibilityDataObj);

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
                String netsalary = String.valueOf(Math.round(Double.parseDouble(netsalaryBG)));
                String netIncome = String.valueOf(Math.round(Double.parseDouble(netIncomeBG)));
                String ftTenure = String.valueOf(Math.round(Double.parseDouble(ftTenureBG)));
                //String ftRoi = String.valueOf(Math.round(Double.parseDouble(ftRoiBG)));
                String loanAmount = String.valueOf(Math.round(Double.parseDouble(loanAmountBG)));
                String sixtimesgrosssal = String.valueOf(Math.round(Double.parseDouble(sixtimesgrosssalBG)));
                String prodspeccapping = String.valueOf(Math.round(Double.parseDouble(prodspeccappingBG)));
                // String finaleligibility = String.valueOf(Math.round(Double.parseDouble(finaleligibilityBG)));

                double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibilityBG) / 1000) * 1000;
                String finaleligibility = String.valueOf(Math.round(finaleligibilityDouble));
                Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheckPerfios::==>" + finaleligibility);

                String prodCode = "PL";
                String subProdCode = "STP-CB";
                HashMap<String, String> loandata = new HashMap<String, String>();
                loandata.put("roi", loanROI);
                loandata.put("tenure", loanTenure);
                loandata.put("gross", GrossSalary);
                loandata.put("cibiloblig", cibiloblig);
                loandata.put("loanoffer", loanAmountBG);
                loandata.put("deduction", DeductionMonthly);
                loandata.put("loancap", Prodcapping);

                String finaleligibilityCommon = null;
                try {
                    finaleligibilityCommon = lec.getAmountForEligibilityCheck(ifr, prodCode, subProdCode, loandata);
                    Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibilityCommon);

                } catch (Exception ex) {
                    Logger.getLogger(AcceleratorActivityManagerCode.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
//                    double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibilityBG) / 1000) * 1000;
//                    finaleligibility = String.valueOf(Math.round(finaleligibilityDouble));
//                    Log.consoleLog(ifr, "After rounded finaleligibility : " + finaleligibility);
                double finaleligibilityDoubled = Math.floor(Double.parseDouble(finaleligibilityCommon) / 1000) * 1000;
                finaleligibilityCommon = String.valueOf(Math.round(finaleligibilityDoubled));
                Log.consoleLog(ifr, "After rounded finaleligibility : " + finaleligibilityCommon);

                Log.consoleLog(ifr, "After rounded finaleligibility : " + finaleligibilityCommon);
                ifr.setValue("FE_Gross_Monthly_Salary", grosssalaryip);
                ifr.setValue("FE_Deduction_From_Salary", deductionsalary);
                ifr.setValue("FE_Obligations", cbCibilOblig);
                ifr.setValue("FE_Net_Home_Salary", netsalary);
                ifr.setValue("FE_Net_Income", netIncome);
                ifr.setValue("FE_Maximum_Tenure_Product", ftTenure);
                ifr.setValue("FE_ROI", ftRoiBG);
                ifr.setValue("FE_Loan_Amount_Per_Policy", loanAmount);
                ifr.setValue("FE_Six_Times_Gross_Salary", sixtimesgrosssal);
                ifr.setValue("FE_Product_Specific_Capping", prodspeccapping);
                ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibilityCommon);
                Log.consoleLog(ifr, "After EligibilityDataObj Data setted : ");

            } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                vlbcc.OnChangePerfiosDataFEVL(ifr, Control, Event, JSdata);
            }
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception IN OnChangePerfiosDataFE " + ex);
        }
        return "";
    }

    //modified by Sharon on 06-08-2024
    public void mAccDocumentColumnDisable(IFormReference ifr, String Control, String Event, String Value) {
        Log.consoleLog(ifr, "Inside mAccDocumentColumnDisable method");
        String stageName = ifr.getActivityName();
        if (stageName.equalsIgnoreCase("Branch Checker")) {
            Log.consoleLog(ifr, "Document Disable::ALV_UPLOAD_DOCUMENT:" + stageName);
            ifr.setColumnDisable("ALV_UPLOAD_DOCUMENT", "6", false);
        }
        if (!stageName.equalsIgnoreCase("Branch Maker")) {
            Log.consoleLog(ifr, "Document Disable::ALV_UPLOAD_DOCUMENT::" + stageName);
            ifr.setColumnDisable("ALV_UPLOAD_DOCUMENT", "6", true);
            ifr.setStyle("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_ApplicantType", "disable", "true");
            ifr.setStyle("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_DocumentType", "disable", "true");
            ifr.setStyle("QNL_BDG_DOCUPLOAD_CNL_BDG_DOCUMENTUPLOAD_uploadDate", "disable", "true");
            ifr.setStyle("table556_button354", "visible", "false");
            ifr.setStyle("table556_button355", "visible", "false");
            ifr.setStyle("saveAdvancedListviewchanges_ALV_UPLOAD_DOCUMENT", "disable", "true");
            ifr.setStyle("table556_textbox5238", "visible", "false");
            ifr.setStyle("table556_textbox5901", "visible", "false");
            ifr.setStyle("table556_combo1251", "visible", "false");
            ifr.setStyle("table556_combo1250", "visible", "false");
            ifr.setStyle("table556_datepick350", "visible", "false");
            ifr.setStyle("table556_textbox5902", "visible", "false");
            ifr.setStyle("table556_textbox5241", "visible", "false");
            Log.consoleLog(ifr, "Document Disable::ALV_UPLOAD_DOCUMENT ENDs::" + stageName);
        }
    }

    public void PopulateBureauCB(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside PopulateBureauScore==>");

        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
                String[] FieldVisibleFalse = new String[]{"QNL_CB_Details_Document_Name", "QNL_CB_Details_Remarks", "QNL_CB_Details_Matching_Records", "QNL_CB_Details_Suit", "QNL_CB_Details_Report_Enquiry_Date", "QNL_CB_Details_PersonalScore", "QNL_CB_Details_UserRemarks", "QNL_CB_Details_Reportdate"};
                for (int i = 0; i < FieldVisibleFalse.length; i++) {
                    ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                }
                String[] FieldVisibleMandatoryDisable = new String[]{"QNL_CB_Details_Applicant_Type", "QNL_CB_Details_CB_Type"};
                for (int i = 0; i < FieldVisibleMandatoryDisable.length; i++) {
                    ifr.setStyle(FieldVisibleMandatoryDisable[i], "disable", "true");
                    ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "true");
                }
                ifr.setStyle("QNL_CB_Details_Applicant_Type", "visible", "true");
                ifr.setStyle("QNL_CB_Details_CB_Type", "visible", "true");
                ifr.setStyle("QNL_CB_Details_CB_Score", "visible", "true");
                ifr.setStyle("QNL_CB_Details_CB_Score", "disable", "true");
                ifr.setStyle("QNL_CB_Details_CB_Score", "mandatory", "true");
                Log.consoleLog(ifr, "PopulateBureauCB Function end..");
            }
            if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                Log.consoleLog(ifr, "inside autoPopulateBureauVL ==>");
                vlbcc.autoPopulateBureauVL(ifr, Control, Event, JSdata);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In PopulateBureauCB:" + e);
        }

    }

    // Start Pension 
    public void populateOccDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside populateOccDetails AcceleratorActivityManagerCode ");
        plpc.populateOccDetails(ifr, Control, Event, value);
    }
    // End Pension 

    public String OnChangeRecommandLoanAmt(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeRecommandLoanAmt AcceleratorActivityManagerCode ");

        JSONObject message = new JSONObject();
        String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
        Log.consoleLog(ifr, "schemeID:" + schemeID);
        String MinimumLoanAmount = null;
        double MinimumLoanAmountchck = 0.00;
        //String MinLoanAmt_Query = "select MINLOANAMOUNT from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
        String MinLoanAmt_Query = ConfProperty.getQueryScript("GetMinLoanAmount").replaceAll("#schemeID#", schemeID);
        List<List<String>> Minloanlist = cf.mExecuteQuery(ifr, MinLoanAmt_Query, "Minimum Loan Amount Query:");
        if (Minloanlist.size() > 0) {
            MinimumLoanAmount = Minloanlist.get(0).get(0);
            MinimumLoanAmountchck = Double.parseDouble(MinimumLoanAmount);
        }
        String EligibilieLoanAmt = ifr.getValue("FE_Eligibilie_Loan_Amount").toString();
        String RecmdLoanAmt = ifr.getValue("FE_Recommended_Loan_Amount").toString();
        Log.consoleLog(ifr, "EligibilieLoanAmt==>" + EligibilieLoanAmt);
        Log.consoleLog(ifr, "RecmdLoanAmt==>" + RecmdLoanAmt);
        String reqLoanAmount = ifr.getValue("FE_Requested_Loan_Amount").toString();

        double reqLoanAmountcheck = Double.parseDouble(reqLoanAmount);
        Log.consoleLog(ifr, "OnChangeRecommandLoanAmt::::>>reqLoanAmountchek" + reqLoanAmountcheck + "reqLoanAmount==>" + reqLoanAmount);
        double EligibilieLoanAmtChck = Double.parseDouble(EligibilieLoanAmt);

        double RecmdLoanAmtchck = Double.parseDouble(RecmdLoanAmt);
        if (!RecmdLoanAmt.equalsIgnoreCase("") || !RecmdLoanAmt.isEmpty()) {

            if (RecmdLoanAmtchck > EligibilieLoanAmtChck) {
                List ClearList = new ArrayList();
                ClearList.add("FE_Recommended_Loan_Amount");
                message.put("showMessage", cf.showMessage(ifr, "FE_Recommended_Loan_Amount", "error", "Recommended Loan Amount should not be greater than Eliglible Loan Amount"));
                message.put("clearValue", cf.clearValue(ifr, ClearList));
                return message.toString();
            } else if (RecmdLoanAmtchck < MinimumLoanAmountchck) {
                List ClearList = new ArrayList();
                ClearList.add("FE_Recommended_Loan_Amount");
                message.put("showMessage", cf.showMessage(ifr, "FE_Recommended_Loan_Amount", "error", "Recommended Loan Amount should be greater or equal to 50000 as per policy"));
                message.put("clearValue", cf.clearValue(ifr, ClearList));
                return message.toString();
            } else if (RecmdLoanAmtchck > reqLoanAmountcheck) {
                List ClearList = new ArrayList();
                ClearList.add("FE_Recommended_Loan_Amount");
                message.put("showMessage", cf.showMessage(ifr, "FE_Recommended_Loan_Amount", "error", "Recommended Loan Amount should be less than Requested loan amount  as per policy"));
                message.put("clearValue", cf.clearValue(ifr, ClearList));
                return message.toString();
            }
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID::" + PID);
            String indexQuery = "SELECT PID FROM los_nl_la_finaleligibility WHERE PID='" + PID + "'";
            List<List<String>> dataresult = cf.mExecuteQuery(ifr, indexQuery, "fetching pid for los_nl_la_finaleligibility ");
            Log.consoleLog(ifr, "dataResult pid for los_nl_la_finaleligibility::" + dataresult);
            if (dataresult.isEmpty()) {
                Log.consoleLog(ifr, "inside data result is empty");
                String Query = "Insert into los_nl_la_finaleligibility (PID,RECOMMENDEDLOANAMOUNT) values ('" + PID + "','" + RecmdLoanAmt + "')";
                Log.consoleLog(ifr, "Query :" + Query);
                ifr.saveDataInDB(Query);
            } else {
                Log.consoleLog(ifr, "inside data result is not empty");
                String Query = "UPDATE los_nl_la_finaleligibility set RECOMMENDEDLOANAMOUNT='" + RecmdLoanAmt + "' where PID='" + PID + "'";
                Log.consoleLog(ifr, "Query :" + Query);
                Log.consoleLog(ifr, " query for updating recommended loan amount");
                ifr.saveDataInDB(Query);
            }
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("saveWorkitem", "true");
            returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "", "Scorecard Rating is Fetched"));
            return returnJSON.toString();
        } else if (RecmdLoanAmt.equalsIgnoreCase("") || RecmdLoanAmt.isEmpty()) {
            message.put("showMessage", cf.showMessage(ifr, "FE_Recommended_Loan_Amount", "error", "Please enter the valid Recommended loan amount!"));
            return message.toString();
        }
        return "";

    }

    //added by ishwarya for revised scorecard in maker 
    public void mAccDecisionValue(IFormReference ifr, String Control, String Event, String value) {
        try {
            Log.consoleLog(ifr, "inside mAccDecisionValue method::");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String consentQuery = "select ConsentReceived from LOS_NL_BUREAU_CONSENT where PID='" + PID + "'";
            List<List<String>> consentQueryResult = cf.mExecuteQuery(ifr, consentQuery, "Query for consentQueryResult");
            String consentReceived = "";
            if (consentQueryResult.size() > 0) {
                consentReceived = consentQueryResult.get(0).get(0);
                if (consentReceived.equalsIgnoreCase("Rejected")) {
                    Log.consoleLog(ifr, "inside mAccDecisionValue method::Rejected:::");
                    ifr.clearCombo("DecisionValue");
                    ifr.addItemInCombo("DecisionValue", "Reject", "R");
                    ifr.addItemInCombo("DecisionValue", "Send Back", "SM");
                }
                if (consentReceived.equalsIgnoreCase("Accepted")) {
                    Log.consoleLog(ifr, "inside mAccDecisionValue method::Accepted:::");
                    ifr.clearCombo("DecisionValue");
                    ifr.addItemInCombo("DecisionValue", "Reject", "R");
                    ifr.addItemInCombo("DecisionValue", "Recommend", "S");
                }
                if (loan_selected.equalsIgnoreCase("Canara Budget") && consentReceived.equalsIgnoreCase("Accepted")) {
                    Log.consoleLog(ifr, "inside mAccDecisionValue Budget LOAN condition");
                    bbcc.setDecisionValueBudget(ifr, Control, Event, value);
                }

                if (loan_selected.equalsIgnoreCase("Canara Pension") && consentReceived.equalsIgnoreCase("Accepted")) {
                    Log.consoleLog(ifr, "inside pension condition");
                    JSONObject message_err = new JSONObject();
                    //String knockoffDecision = plpc.mImpPensionOnClickDocumentUploadKnockoff(ifr);
                    if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                        //String knockoffDecision = "PROCEED";
                        //if (knockoffDecision.toUpperCase().equalsIgnoreCase("PROCEED")) {
                        Log.consoleLog(ifr, "pension document knockoffDecision Passed Successfully in mAccDecisionValue:::");
                        ifr.clearCombo("DecisionValue");
                        ifr.addItemInCombo("DecisionValue", "Recommend", "S");
                        //ifr.addItemInCombo("DecisionValue", "Approve", "S");
                        //} else {
                        //  Log.consoleLog(ifr, "pension document knockoffDecision fail in mAccDecisionValue" + knockoffDecision);
                        // ifr.clearCombo("DecisionValue");
                        //ifr.addItemInCombo("DecisionValue", "Reject", "R");
                        //ifr.addItemInCombo("DecisionValue", "Send Back", "SM");
                        //}
                    } else if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                        ifr.clearCombo("DecisionValue");
                        ifr.addItemInCombo("DecisionValue", "Recommend", "S");
                        ifr.addItemInCombo("DecisionValue", "Reject", "R");
                        ifr.addItemInCombo("DecisionValue", "Send Back", "SM");
                    } else if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                        ifr.clearCombo("DecisionValue");
                        ifr.addItemInCombo("DecisionValue", "Reject", "R");
                        ifr.addItemInCombo("DecisionValue", "Approve", "S");
                    }
                    String ActivityName = ifr.getActivityName();
                    //                   modified by Sharon on 02/08/2024
                    if (ActivityName.equalsIgnoreCase("Reviewer")) {
                        Log.consoleLog(ifr, "inside setDecisionValuePension BkoffCustomCode:: Reviewer ");
                        ifr.clearCombo("DecisionValue");
                        ifr.addItemInCombo("DecisionValue", "Submit", "S");

                    }

                }
                if (loan_selected.equalsIgnoreCase("VEHICLE LOAN") && consentReceived.equalsIgnoreCase("Accepted")) {
                    Log.consoleLog(ifr, "inside mAccDecisionValue VEHICLE LOAN condition");
                    vlbcc.checkCBEligibilityDecisionVL(ifr, Control, Event, value);
                }

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  mAccDecisionValue method::" + e);
            Log.errorLog(ifr, "Exception in  mAccDecisionValue method::" + e);
        }
    }

    public String revisedScorecardcheck(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside revisedScorecardcheck AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String message = "";
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "revisedScorecardcheck --> Budget");
            message = Bpcm.checkScorecardbackOffice(ifr);
            Log.consoleLog(ifr, "message -->" + message);
            if (message.equalsIgnoreCase("SUCCESS")) {
                Log.consoleLog(ifr, "Scorecardcheck Backoffice is Successful");
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("saveWorkitem", "true");
                returnJSON.put("showMessage", cf.showMessage(ifr, "BTN_FetchScoreRating", "", "Scorecard Rating is Fetched"));
                message = returnJSON.toString();
            }
        } else if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            Log.consoleLog(ifr, "revisedScorecardcheck --> VL");
            message = Vlpc.checkScorecardbackOffice(ifr);
        }

        return message;
    }

    public String OnChangeFinancialInfo(IFormReference ifr, String Control, String Event, String JSdata) {

        Log.consoleLog(ifr, "inside OnChangeFinancialInfo for pension==>");

        WDGeneralData Data = ifr.getObjGeneralData();

        String ProcessInstanceId = Data.getM_strProcessInstanceId();

        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        try {

            // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");

            String loan_selected = loanSelected.get(0).get(0);

            Log.consoleLog(ifr, "loan type==>" + loan_selected);

            if (loan_selected.equalsIgnoreCase("Canara Pension")) {

                //  String GrossAmt = ifr.getValue("QA_FI_PI_MINCOME_GrossAmt").toString();
                // Log.consoleLog(ifr, "Gross Amount on change::" + GrossAmt);
                String gross_amount = "";

                String net_amount = "";

                //String incomeQuery = "select GROSSAMOUNT,NETAMOUNT from los_nl_pl_mincome where PID='" + ProcessInstanceId + "'";
                String incomeQuery = ConfProperty.getQueryScript("los_nl_pl_mincomeData").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> incomeQueryList = cf.mExecuteQuery(ifr, incomeQuery, "Execute query for fetching income data ");

                if (incomeQueryList.size() > 0) {

                    gross_amount = incomeQueryList.get(0).get(1);

                    Log.consoleLog(ifr, "Gross Amount from query::" + gross_amount);

                    net_amount = incomeQueryList.get(0).get(0);

                    Log.consoleLog(ifr, "Net Amount from query::" + net_amount);

                }

                if (gross_amount.equalsIgnoreCase("") || gross_amount.equalsIgnoreCase("0") || gross_amount.isEmpty()) {

                    JSONObject message = new JSONObject();

                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Gross Salary Amount!"));

                    return message.toString();

                }

                // String netAmt = ifr.getValue("QA_FI_PI_MINCOME_NetAmount").toString();
                // Log.consoleLog(ifr, "Net amount on change::" + netAmt);
                if (net_amount.equalsIgnoreCase("") || net_amount.isEmpty()) {

                    JSONObject message = new JSONObject();

                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Deduction Monthly Amount!"));

                    return message.toString();

                }

                //  BigDecimal deduction = new BigDecimal(Double.parseDouble(gross_amount) - Double.parseDouble(net_amount)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal deduction = BigDecimal.valueOf(Double.parseDouble(gross_amount) - Double.parseDouble(net_amount)).setScale(2, RoundingMode.HALF_UP);

                Log.consoleLog(ifr, "Deduction::" + deduction);

                // String query4 = "select TENURE,ROI,LOAN_AMOUNT FROM LOS_PEN_PRINCIPLE_APPROVAL where PID='" + ProcessInstanceId + "'";
                String query4 = ConfProperty.getQueryScript("PEN_PRINCIPLEData").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> fe_data = cf.mExecuteQuery(ifr, query4, "Execute query4 for eligibility data");

                String tenure = fe_data.get(0).get(0);

                Log.consoleLog(ifr, "Tenure==>" + tenure);

                String roi = fe_data.get(0).get(1);

                Log.consoleLog(ifr, "ROI==>" + roi);

                String loan_amt = fe_data.get(0).get(2);

                double loanAmt = Math.round(Double.parseDouble(loan_amt));

                // BigDecimal takeHomePen = new BigDecimal((0.4 * Double.parseDouble(gross_amount)))
                //.setScale(2, RoundingMode.HALF_UP);
                //Added by prakash 25-03-2024 for sonar report
                BigDecimal takeHomePen = BigDecimal.valueOf(0.4 * Double.parseDouble(gross_amount))
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal pen_24times = new BigDecimal(24 * Integer.parseInt(gross_amount));

                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());

                Log.consoleLog(ifr, "schemeID:" + schemeID);

                String Prodcapping = null;

                //String ProdCapping_Query = "select MAXLOANAMOUNT from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                String ProdCapping_Query = ConfProperty.getQueryScript("PENSIONMAXLOANAMT").replaceAll("#schemeID#", schemeID);
                List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");

                if (ProdcappingList.size() > 0) {

                    Prodcapping = ProdcappingList.get(0).get(0);

                }

                Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

                BigDecimal grossPen = new BigDecimal(Integer.parseInt(gross_amount));

                BigDecimal lacAmount = new BigDecimal(100000);

                BigDecimal netPension = new BigDecimal(net_amount);

                BigDecimal ftRoi = new BigDecimal(roi);

                BigDecimal prodspcCapping = new BigDecimal(Prodcapping);

                // BigDecimal emiperlc = pcm.calculateEMI(ifr, lacAmount, ftRoi, Integer.parseInt(String.valueOf(tenure)));
                //Log.consoleLog(ifr, "emi per lac==>" + emiperlc);
                BigDecimal emiperlc = pcm.calculatePMT(ifr, ftRoi, Integer.parseInt(tenure));

                Log.consoleLog(ifr, " emiperlc:::: " + emiperlc);

                //String TotalEmiQuery = "SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK WHERE BUREAUTYPE ='EX' AND PROCESSINSTANCEID ='" + ProcessInstanceId + "'";
                //  String TotalEmiQuery = ConfProperty.getQueryScript("TotalEmiQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                String least_score = "select distinct(BUREAUTYPE), EXP_CBSCORE from los_can_ibps_bureaucheck where PROCESSINSTANCEID='" + ProcessInstanceId + "'";
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

                String TotalEmiQuery = ConfProperty.getQueryScript("getEMIBasedOnScore").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#BT#", bureau_Type);

                List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "Execute query for fetching TotalEmi data");

                String cibiloblig = "";

                if (TotalEmi.size() > 0) {

                    cibiloblig = TotalEmi.get(0).get(0).toString();

                    Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);

                } else {

                    cibiloblig = "0.00";

                }

                BigDecimal oblig = new BigDecimal(cibiloblig);

                BigDecimal netPen = (grossPen.subtract(deduction).subtract(oblig).subtract(takeHomePen)).setScale(2, RoundingMode.HALF_UP);

                Log.consoleLog(ifr, "Net Pension::" + netPen);

                BigDecimal loanAmount = new BigDecimal("100000").multiply(netPen).divide(emiperlc, 2, RoundingMode.HALF_UP);

                Log.consoleLog(ifr, "loan amt" + loanAmount);

                /*   BigDecimal finaleligibility = loanAmount.min(pen_24times).min(prodspcCapping);

                 String eligibleAmt = finaleligibility.toString();
                 double finaleligibilityDouble = Math.floor(Double.parseDouble(eligibleAmt) / 1000) * 1000;
                 String finaleligibilityPen = String.valueOf(Math.round(finaleligibilityDouble));

                 Log.consoleLog(ifr, "final eligible loan amt==>" + finaleligibilityPen);

                 Log.consoleLog(ifr, "eligible loan amt==>" + finaleligibility);*/
                String prodCode = "PL";
                String subProdCode = "STP-CP";
                HashMap<String, String> loandata = new HashMap<String, String>();
                loandata.put("roi", ftRoi.toString());
                loandata.put("tenure", tenure);
                loandata.put("gross", grossPen.toString());
                loandata.put("cibiloblig", cibiloblig);
                loandata.put("loanoffer", loanAmount.toString());
                loandata.put("deduction", deduction.toString());
                loandata.put("loancap", prodspcCapping.toString());

                String finaleligibility = null;
                try {
                    finaleligibility = lec.getAmountForEligibilityCheck(ifr, prodCode, subProdCode, loandata);
                    Log.consoleLog(ifr, "final eligibility from getAmountForEligibilityCheck::==>" + finaleligibility);

                } catch (Exception ex) {
                    Logger.getLogger(AcceleratorActivityManagerCode.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                // BigDecimal finaleligibility = loanAmount.min(pen6Times).min(prodspcCapping);
                String eligibleAmt = finaleligibility.toString();
                double finaleligibilityDouble = Math.floor(Double.parseDouble(eligibleAmt) / 1000) * 1000;
                String finaleligibilityPen = String.valueOf(Math.round(finaleligibilityDouble));

                Log.consoleLog(ifr, "final eligible loan amt==>" + finaleligibilityPen);

                Log.consoleLog(ifr, "Net Pension::" + netPen);

                ifr.setValue("FE_Gross_Monthly_Pension", grossPen.toString());

                ifr.setValue("FE_Deduction_From_Salary", deduction.toString());

                ifr.setValue("FE_Net_Income", netPen.toString());

                ifr.setValue("FE_NetTakeHome_Pension", takeHomePen.toString());

                ifr.setValue("FE_6Times_GrossPension", pen_24times.toString());

                ifr.setValue("FE_Maximum_Tenure_Product", tenure);

                ifr.setValue("FE_ROI", roi);

                // ifr.setValue("FE_Eligibilie_Loan_Amount", loan_amt);
                ifr.setValue("FE_Product_Specific_Capping", prodspcCapping.toString());

                ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibilityPen);

                ifr.setValue("FE_Loan_Amount_Per_Policy", loanAmount.toString());

                ifr.setValue("FE_Obligations", cibiloblig);

                /*   String deductUpdateQ = "update LOS_NL_PL_MIncome set DEDUCTION='" + deduction + "' where PID='" + ProcessInstanceId + "'";

                 Log.consoleLog(ifr, "Update Query for deduction" + deductUpdateQ);

                 ifr.saveDataInDB(deductUpdateQ);*/
                ifr.setTableCellValue("ALV_PL_MIncome", 0, 10, deduction.toString());

                Log.consoleLog(ifr, "Updated deduction");

            }

        } catch (Exception ex) {

            Log.consoleLog(ifr, "Exception IN OnChangeFinancialInfo " + ex);

        }

        return "";

    }

    public void docUploadChecker(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside docUploadChecker");
        ifr.setColumnVisible("ALV_UPLOAD_DOCUMENT1", "5", false);
        ifr.setColumnVisible("ALV_UPLOAD_DOCUMENT", "6", false);
    }

//    public void onChangeBranchcheckerDecisionSubmit(IFormReference ifr, String Control, String Event, String value) {
//        Log.consoleLog(ifr, "inside onChangeBranchcheckerDecisionSubmit AcceleratorActivityManagerCode ");
//        vlbcc.mBranchcheckerNeslVL(ifr);
//    }
//    public void OnChangeProposedLoanSectionDMaker(IFormReference ifr, String Control, String Event, String value) {
//        Log.consoleLog(ifr, "inside onChangeBranchcheckerDecisionSubmit AcceleratorActivityManagerCode ");
//        vlbcc.mOnChangeProposedLoanSectionDisMaker(ifr);
//    }
    public void autoPopulateNeslStatus(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside autoPopulateNeslStatus AcceleratorActivityManagerCode ");
        vlbcc.autoPopulateNeslStatus(ifr);
    }

    public void mDisbursementCheckerApiCall(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside autoPopulateNeslStatus AcceleratorActivityManagerCode ");
        vlbcc.mDisbursementCheckerApiCall(ifr, control, event, value);
    }

    public String CollateralDataPopulation(IFormReference ifr, String Control, String Event, String value) {

        try {
            Log.consoleLog(ifr, "inside CollateralDataPopulation AcceleratorActivityManagerCode::");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String VehicleType = "", ASSETMANUFACTURER = "", ASSETMODEL = "", VEHICLEVARIANT = "", VEHICLECOST = "", DOWNPAYMENTAMT = "",
                    FUELTYPE = "", MANUFACTURERYEAR = "", CATEGORY = "";
            Date EXPECTEDDELIVERYDATE = null;
            String CollateralDataQuery = ConfProperty.getQueryScript("CollateralInfoGrid").replaceAll("#PID#", PID);
            List<List<String>> CollateralDataQueryList = cf.mExecuteQuery(ifr, CollateralDataQuery, "Execute query for fetching Collateral data");
            if (CollateralDataQueryList.size() > 0) {
                VehicleType = CollateralDataQueryList.get(0).get(0);
                ASSETMANUFACTURER = CollateralDataQueryList.get(0).get(1);
                ASSETMODEL = CollateralDataQueryList.get(0).get(2);
                VEHICLEVARIANT = CollateralDataQueryList.get(0).get(3);
                VEHICLECOST = CollateralDataQueryList.get(0).get(4);
                DOWNPAYMENTAMT = CollateralDataQueryList.get(0).get(5);
                FUELTYPE = CollateralDataQueryList.get(0).get(6);
                MANUFACTURERYEAR = CollateralDataQueryList.get(0).get(7);
                CATEGORY = CollateralDataQueryList.get(0).get(8);
                //EXPECTEDDELIVERYDATE = CollateralDataQueryList.get(0).get(2);
            }
            Log.consoleLog(ifr, "VehicleType : " + VehicleType);
            Log.consoleLog(ifr, "ASSETMANUFACTURER: " + ASSETMANUFACTURER);
            Log.consoleLog(ifr, "ASSETMODEL: " + ASSETMODEL);
            Log.consoleLog(ifr, "VEHICLEVARIANT: " + VEHICLEVARIANT);
            Log.consoleLog(ifr, "VEHICLECOST: " + VEHICLECOST);
            Log.consoleLog(ifr, "DOWNPAYMENTAMT: " + DOWNPAYMENTAMT);
            Log.consoleLog(ifr, "FUELTYPE: " + FUELTYPE);
            Log.consoleLog(ifr, "MANUFACTURERYEAR: " + MANUFACTURERYEAR);
            Log.consoleLog(ifr, "CATEGORY: " + CATEGORY);
            Log.consoleLog(ifr, "EXPECTEDDELIVERYDATE: " + EXPECTEDDELIVERYDATE);

            String VehicleTypeQuery = ConfProperty.getQueryScript("VehicleTypeQuery").replaceAll("#VEHICLETYPE#", VehicleType);
            Log.consoleLog(ifr, "VehicleTypeQuery::" + VehicleTypeQuery);
            List<List<String>> VehicleTypeQueryList = cf.mExecuteQuery(ifr, VehicleTypeQuery, "Execute query for fetching VehicleType");
            if (VehicleTypeQueryList.size() > 0) {
                VehicleType = VehicleTypeQueryList.get(0).get(0);
            }
            Log.consoleLog(ifr, "VehicleType: " + VehicleType);
            String AssetManufacturerQuery = ConfProperty.getQueryScript("AssetManufacturerQuery").replaceAll("#ASSETMANUFACTURER#", ASSETMANUFACTURER).replaceAll("#ASSETMODEL#", ASSETMODEL);
            Log.consoleLog(ifr, "AssetManufacturerQuery::" + AssetManufacturerQuery);
            List<List<String>> AssetManufacturerQueryList = cf.mExecuteQuery(ifr, AssetManufacturerQuery, "Execute query for fetching ASSETMANUFACTURER");
            if (AssetManufacturerQueryList.size() > 0) {
                ASSETMANUFACTURER = AssetManufacturerQueryList.get(0).get(0);
            }
            Log.consoleLog(ifr, "ASSETMANUFACTURER: " + ASSETMANUFACTURER);
            String AssetModelQuery = ConfProperty.getQueryScript("AssetModelQuery").replaceAll("#ASSETMANUFACTURER#", ASSETMANUFACTURER).replaceAll("#ASSETMODEL#", ASSETMODEL);
            Log.consoleLog(ifr, "AssetModelQuery::" + AssetModelQuery);
            List<List<String>> AssetModelQueryList = cf.mExecuteQuery(ifr, AssetModelQuery, "Execute query for fetching ASSETMODEL");
            if (AssetModelQueryList.size() > 0) {
                ASSETMODEL = AssetModelQueryList.get(0).get(0);
            }
            Log.consoleLog(ifr, "ASSETMODEL: " + ASSETMODEL);
            String AssetVariantQuery = ConfProperty.getQueryScript("AssetVariantQuery").replaceAll("#VEHICLEVARIANT#", VEHICLEVARIANT);
            Log.consoleLog(ifr, "AssetVariantQuery: " + AssetVariantQuery);
            List<List<String>> AssetVariantQueryList = cf.mExecuteQuery(ifr, AssetVariantQuery, "Execute query for fetching VEHICLEVARIANT");
            if (AssetVariantQueryList.size() > 0) {
                VEHICLEVARIANT = AssetVariantQueryList.get(0).get(0);
            }
            Log.consoleLog(ifr, "VEHICLEVARIANT: " + VEHICLEVARIANT);
            int CollateralDataSize = ifr.getDataFromGrid("ALV_VEHICLE_COLLATERAL").size();
            Log.consoleLog(ifr, "CollateralData Size ::" + CollateralDataSize);
            if (CollateralDataSize == 0) {
                Log.consoleLog(ifr, "inside add data to grid ALV_VEHICLE_COLLATERAL::" + CollateralDataSize);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_FUELTYPE", FUELTYPE);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_VEHICLETYPE", VehicleType);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_ASSETMANUFACTURER", ASSETMANUFACTURER);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_ASSETMODEL", ASSETMODEL);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_VEHICLEVARIANT", VEHICLEVARIANT);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_VEHICLECOST", VEHICLECOST);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_DOWNPAYMENTAMT", DOWNPAYMENTAMT);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_CATEGORY", CATEGORY);
                obj.put("QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURERYEAR", MANUFACTURERYEAR);
                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                ((IFormAPIHandler) ifr).addDataToGrid("ALV_VEHICLE_COLLATERAL", jsonarr, true);
                Log.consoleLog(ifr, "Collateral Data row Added==>");
            }
            ifr.setStyle("QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME", "disable", "false");
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in  CollateralDataPopulation AcceleratorActivityManagerCode::" + e);
        }
        return "";
    }

    public void onclickSendBack(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside onclickSendBack ==>");

        WDGeneralData Data = ifr.getObjGeneralData();

        String processInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "processInstanceId==>" + processInstanceId);

        try {

            // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", processInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");

            String loan_selected = loanSelected.get(0).get(0);

            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                String stepnameUpdQ = " update LOS_WIREFERENCE_TABLE set CURR_STAGE='Occupation Details ' where WINAME='" + processInstanceId + "'";
                cf.mExecuteQuery(ifr, stepnameUpdQ, "Update query for step name");

            }
            if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                String stepnameUpdQ = " update LOS_WIREFERENCE_TABLE set CURR_STAGE='Occupation Details' where WINAME='" + processInstanceId + "'";
                cf.mExecuteQuery(ifr, stepnameUpdQ, "Update query for step name");
            }
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                String stepnameUpdQ = " update LOS_WIREFERENCE_TABLE set CURR_STAGE='Occupation Details' where WINAME='" + processInstanceId + "'";
                cf.mExecuteQuery(ifr, stepnameUpdQ, "Update query for step name");
            }

            //Added by Ahmed on 10-05-2024 triggering MailContent from DLPCommonObjects=========
            String fileName = "";
            String fileContent = "";
            String bodyParams = "";
            String subjectParams = "";
            pcm.triggerCCMAPIs(ifr, processInstanceId, "Budget", "4", bodyParams, subjectParams, fileName, fileContent);
            //Ended by Ahmed on 10-05-2024 triggering MailContent from DLPCommonObjects=========

            /*
             Email em = new Email();
             SMS sms = new SMS();
             WhatsApp wh = new WhatsApp();

             String emailId = pcm.getCurrentEmailId(ifr, "Budget", "");
             String mobileNumber = pcm.getMobileNumber(ifr);
            

             em.sendEmail(ifr, ProcessInstanceId, emailId, "", "", "RETAIL", "4");
             sms.sendSMS(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber, "", "", "RETAIL", "4");
             wh.sendWhatsAppMsg(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber, "", "", "RETAIL", "4");
             */
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in   onclickSendBack::" + e);
        }

    }

    public void onChangeEmailandSMS(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onChangeBranchcheckerDecisionSubmit AcceleratorActivityManagerCode ");
        vlbcc.mEmailSmscheck(ifr);
    }

    public void incomeGridFieldVisibility(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside incomeGridFieldVisibility AcceleratorActivityManagerCode ");
        plpc.incomeGridFieldVisibility(ifr, control, event, value);
    }

    public static String fetchDataFromGrid(IFormReference ifr, String gridId) {
        JSONArray jsonArray = ifr.getDataFromGrid(gridId);
        return jsonArray.toString();
    }

    public void bureauCheckHideReferhButton(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside bureauCheckHideReferhButton");
        ifr.setStyle("Btn_Refresh", "visible", "false");
        ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
    }

    public void mCalculateFeeandChargesVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside mCalculateFeeandChargesVL");
        vlbcc.mCalculateFeeandChargesVL(ifr, control, event, value);
    }

    public void tabVisibility(IFormReference ifr, String visibleTabs) {
        String[] TabVisibleTrueArr = visibleTabs.split(",");
        for (String sheetid : TabVisibleTrueArr) {
            Log.consoleLog(ifr, "tab Visibility " + sheetid);
            ifr.setTabStyle("tab1", sheetid, "visible", "true");
        }
    }

    public void tabInVisibility(IFormReference ifr, String InvisibleTabs) {
        String[] TabVisibleFalseArr = InvisibleTabs.split(",");
        for (String sheetid : TabVisibleFalseArr) {
            Log.consoleLog(ifr, "tab InVisibility " + sheetid);
            ifr.setTabStyle("tab1", sheetid, "visible", "false");
        }
    }

    public String disbursementMakerAPICallCB(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside disbursementCheckerAPICallCB AcceleratorActivityManagerCode ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        int gridSize = ifr.getDataFromGrid("ALV_BAM83").size();
        Log.consoleLog(ifr, "ALV_BAM83 :" + gridSize);
        if (gridSize == 0) {
            Log.consoleLog(ifr, "ALV_BAM83 :" + gridSize);
            String msg = "BAM83NOTFILLED";
            return msg;
        }
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
            return bbcc.mDisbursementMakerAPICallCB(ifr, loan_selected);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            return vlbcc.mDisbursementMakerAPICallVL(ifr);
        }
        return "";
    }

    public void onChangeBranchcheckerDecisionSubmit(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onChangeBranchcheckerDecisionSubmit AcceleratorActivityManagerCode ");
        vlbcc.mBranchcheckerNeslVL(ifr);
        bbcc.mPostSanctionNeslBudget(ifr);
    }

    public void OnLoadRiskScoreRatigMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadRiskScoreRatigMaker AcceleratorActivityManagerCode ");
        bbcc.riskScoreRatig(ifr);
    }

    public void mImplClickNetWorthMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mImplClickNetWorthMaker AcceleratorActivityManagerCode ");
        bbcc.netWorthMaker(ifr);
    }

    public void mImplClickMonthlyExpensesMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mImplClickMonthlyExpensesMaker AcceleratorActivityManagerCode ");
        bbcc.monthlyExpensesMaker(ifr);
    }

    public void OnLoadLAFinalEligibilityMKR(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadLoanAssesment AcceleratorActivityManagerCode ");
        bbcc.finalEligibility(ifr);
    }

    public void OnLoadLAInPrincipleEligibilityMKR(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadLoanAssesment AcceleratorActivityManagerCode ");
        bbcc.inPrincipleEligibility(ifr);
    }

    public void OnLoadKnockoffMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadKnockoffMaker AcceleratorActivityManagerCode ");
        bbcc.knockoffMaker(ifr);
    }

    public void mImplClickAssetMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mImplClickAssetMaker AcceleratorActivityManagerCode ");
        bbcc.assetMaker(ifr);
    }

    public void OnChangeFinancialInfoSummarySectionMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeFinancialInfoSummarySectionMaker AcceleratorActivityManagerCode ");
        bbcc.financialInfoSummarySectionMaker(ifr);
    }

    public void OnLoadOutwardDocumentMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadOutwardDocumentMaker AcceleratorActivityManagerCode ");
        bbcc.outwardDocument(ifr);
    }

    public void OnLoadFinancialInfoLiabilitiesMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadFinancialInfoLiabilitiesMaker AcceleratorActivityManagerCode ");
        bbcc.OnLoadFinancialInfoLiabilities(ifr);
    }

    public void OnLoadFinancialInfoIncomeMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadFinancialInfoIncomeMaker AcceleratorActivityManagerCode ");
        bbcc.OnLoadFinancialInfoIncome(ifr);
    }

    public void OnChangePersonalFinancialInfomation(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation AcceleratorActivityManagerCode ");
        bbcc.OnChangePersonalFinancialInfomation(ifr);
    }

    public void OnchangeScoreRating(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation AcceleratorActivityManagerCode ");
        bbcc.OnchangeScoreRating(ifr);
    }

    public void formLoadPostSanction(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside formLoadDisbursementMaker");
        bbcc.formLoadPostSanctionVisibility(ifr);
    }

    //Modified by Keerthana on 01/08/2024
    public String budgetLCBRMSRulesCheck(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, " Inside budgetLCBRMSRulesCheck");
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String insertionId = ifr.getValue("DD_CB_APPTYPE_ID").toString();
        String applicanttype = "";
        String loanSelected = "";
        String applicantType = "select applicanttype from los_nl_basic_info where PID='" + ProcessInsanceId + "' and INSERTIONORDERID='" + insertionId + "'";
        List<List<String>> List = cf.mExecuteQuery(ifr, applicantType, "applicantType FROM Table:");
        if (List.size() > 0) {
            applicanttype = List.get(0).get(0);
        }
        // Modified by kathir for pension lead capture knockoff on 01-08-2024
        String loanTypeQuery = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInsanceId);
        List<List<String>> loanType = cf.mExecuteQuery(ifr, loanTypeQuery, "AcceleratorActivityManagerCode:budgetLCBRMSRulesCheck -> Execute query for fetching loan selected ");
        if (loanType.size() > 0) {
            loanSelected = loanType.get(0).get(0);
        }
        Log.consoleLog(ifr, "AcceleratorActivityManagerCode:budgetLCBRMSRulesCheck -> loan type==>" + loanSelected);
        if (loanSelected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "AcceleratorActivityManagerCode:budgetLCBRMSRulesCheck -> Inside Budget case");
            bbcc.KnockoffCheckLCCB(ifr, applicanttype);
        } else if (loanSelected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "AcceleratorActivityManagerCode:budgetLCBRMSRulesCheck -> Inside Pension case");
            return pbcc.KnockoffCheckLCPension(ifr, applicanttype);
        }
        return "";
    }

    public String popluateLCDocumentsUploadCB(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "AcceleratorActivityManagerCode:popluateLCDocumentsUploadCB -> Inside popluateLCDocumentsUploadCB");
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String subProductQuery = "SELECT SUBPRODUCT FROM LOS_NL_PROPOSED_FACILITY WHERE PID = '" + processInstanceId + "'";
        Log.consoleLog(ifr, "AcceleratorActivityManagerCode:popluateLCDocumentsUploadCB -> subProductQuery: " + subProductQuery);
        List<List<String>> subProd = cf.mExecuteQuery(ifr, subProductQuery, "subProductQuery Result:");
        Log.consoleLog(ifr, "AcceleratorActivityManagerCode:popluateLCDocumentsUploadCB -> subProd: " + subProd);
        if (subProd.size() > 0) {
            String subProduct = subProd.get(0).get(0);
            if (subProduct.equalsIgnoreCase("STP-CP")) {
                Log.consoleLog(ifr, "AcceleratorActivityManagerCode:popluateLCDocumentsUploadCB -> Inside Pension: ");
                pbcc.populateLCDocumentsUploadCP(ifr);
            } else if (subProduct.equalsIgnoreCase("STP-CB")) {
                Log.consoleLog(ifr, "AcceleratorActivityManagerCode-popluateLCDocumentsUploadCB -> Inside Budget: ");
                bbcc.popluateLCDocumentsUploadCB(ifr);
            }
        }
        return "";
    }

    public void formLoadDisbursementMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside formLoadDisbursementMaker");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        ifr.setStyle("CBSPRODUCTCODE", "disable", "true");
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.formLoadDisbursementMakerVisibility(ifr);
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            pbcc.formLoadDisbursementMakerVisibilityCP(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.formLoadDisbursementMakerVisibilityVL(ifr);
        }
    }

    //added by logaraj
    public String formLoadConvenor(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "intoConvenorDetails");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String visibleTab = "0,4,6,7,8,11,12,13,16,17,18,19";
        pcm.tabInVisibility(ifr, "tab1", visibleTab);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {

            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "CTRID_PD_FETCHEXTCUST,CTRID_PD_RESETDET,BTN_Dedupe_Click,F_ESIGN_ID,F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);

            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);
        }
        return "";
    }

    public void mAccChangeRetirementDate(IFormReference ifr, String Control, String Event, String JSdata) throws java.text.ParseException {//Checked

        Log.consoleLog(ifr, "inside mAccChangeRetirementDate");
        bbcc.mAccChangeRetirementDate(ifr);
    }

    public String InPrincipleLCBCheck(IFormReference ifr, String Control, String Event, String JSdata) {//Checkedsss
        Log.consoleLog(ifr, "inside InPrincipleLCBCheck");
        String message = bbcc.InPrincipleLCBCheck(ifr);
        return message;
    }

    public void formLoadReviewer(IFormReference ifr, String Control, String Event, String value) {

        Log.consoleLog(ifr, "inside formLoaReviewer");
        bbcc.OnLoadReviwerVisibility(ifr);
    }

    public void onLoadBureauConsent(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onLoadBureauConsent AcceleratorActivityManagerCode ");
        bbcc.onLoadBureauConsent(ifr);
    }

    public void onChangeSubProductEC(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change sub product for EC");
        bbcc.existingCustomerAutoPopulate(ifr);
    }

    public void mAccChangeProcessingBranch(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mAccChangeProcessingBranch method");
        bbcc.mAccChangeProcessingBranchName(ifr, Control, Event, value);
    }

    //added by aravind
    public void OnChangeSourcingInfoSectionMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangePersonalFinancialInfomation AcceleratorActivityManagerCode ");
        bbcc.OnChangeSourcingInfoSectionMaker(ifr, Control, Event, value);
    }

    public void onChangeLoanPurpose(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change Loan Purpose");
        bbcc.onChangeOthersLoanPurpose(ifr);
    }

    public void onChangeLoanPurposeCP(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change Loan Purpose");
        plpc.onChangeOthersLoanPurposeCP(ifr);
    }

    public void mformOnLoadLeadCapture(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mformOnLoadLeadCapture AcceleratorActivityManagerCode ");
        bbcc.onLoadLeadCapture(ifr);
    }

    public void mOnChangeROIType(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change ROI Type");
        bbcc.onChangeROIType(ifr);
    }

    public String mOnChangeLoanAmount(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change LoanAmount AcceleratorActivityManagerCode");
        String resValue = Bpcm.onChangeLoanAmount(ifr);
        return resValue;
    }
    //added by KeerthanaR for pension phase-2 development

//    public String OnChangeReqLoanAmountCP(IFormReference ifr, String Control, String Event, String value) {
//        Log.consoleLog(ifr, "inside on change LoanAmount AcceleratorActivityManagerCode");
//        String resValue = plpc.OnChangeReqLoanAmountCP(ifr);
//        return resValue;
//    }
    //modified by Sharon on 06-08-2024
    public void fromLoadBranchMaker(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside fromLoadMaker AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String queryPreviousWS = ConfProperty.getQueryScript("getPreviousWS").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> previousWS = cf.mExecuteQuery(ifr, queryPreviousWS, "Execute query for fetching previous work step ");
        String previous_workstep = previousWS.get(0).get(0);
        Log.consoleLog(ifr, "previous work step==>" + previous_workstep);
        String queryPortalYn = ConfProperty.getQueryScript("getPortalYn").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> portalYn = cf.mExecuteQuery(ifr, queryPortalYn, "Execute query for fetching its a fully assisted loan journey");
        String isPortal = portalYn.get(0).get(0);
        Log.consoleLog(ifr, "isPortal ==> " + isPortal);

        if (previous_workstep.equalsIgnoreCase("Lead Capture")) {
            Log.consoleLog(ifr, "inside branch maker from lead capture");
            ifr.setStyle("F_Bureau_Consent", "visible", "false");
            ifr.setStyle("F_CoborrowerEligibility_Sec", "visible", "false");
            ifr.setStyle("DD_CB_BUREAU_ID", "visible", "false");
            ifr.setStyle("F_CoborrowerEligibility_Sec", "visible", "false");
            Log.consoleLog(ifr, "end of branch maker from lead capture");
        }
        if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            Log.consoleLog(ifr, "inside vehicle loan branch maker");
            String nonVisibleSectionGrids = "F_ESIGN_ID,CTRID_PD_FETCHEXTCUST,CTRID_PD_RESETDET,BTN_Dedupe_Click,F_Deviation,QNL_BASIC_INFO_RelationshipwithBankMonths,table565_textbox6838,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,BTN_Dedupe_Click,DD_CB_BUREAU_ID,QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify,F_DAMC_DOCUMENT,F_Sign_Document,F_E-Sign,F_Mortgage,F_CollateralSummary,F_LoanAssess,F_TermsConditions";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            //phase 2 proposedloan grid fields added by Janani
            String disableFields = "QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify,QNL_LOS_PROPOSED_FACILITY_Variant,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_TypeofIntrestRegime,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_EMI_DATE,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_SCHEDULECODE_DESC,QNL_LOS_PROPOSED_FACILITY_RATECHARTCODE_DESC,QNL_LOS_PROPOSED_FACILITY_Variant,LV_KYC,QNL_BUREAU_CONSENT_PartyType";
            pcm.controlDisable(ifr, disableFields);
            ifr.setTabStyle("tab1", "9", "visible", "true");
            ifr.setTabStyle("tab1", "8", "visible", "true");
            ifr.setStyle("ALV_VEHICLE_COLLATERAL", "visible", "true");
            ifr.setStyle("ALV_PL_MExpense", "visible", "true");
            ifr.setColumnVisible("ALV_VEHICLE_COLLATERAL", "0", false);
            ifr.setStyle("ALV_UPLOAD_DOCUMENT1", "visible", "false");
            ifr.setStyle("QL_SOURCINGINFO_BranchCode", "visible", "false");
            ifr.setStyle("QL_SOURCINGINFO_Branch", "visible", "false");
            ifr.setStyle("QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME", "disable", "false");
            ifr.setStyle("Vehicle_Title", "disable", "false");
            ifr.setStyle("ALV_BUREAU_CONSENT", "disable", "false");
            ifr.setStyle("LV_KYC", "visible", "true");
            CollateralDataPopulation(ifr, Control, Event, value);

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
            ifr.setColumnVisible("ALV_FINAL_ELIGIBILITY", "5", false);
            ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "8", false);
            ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "13", true);
            ifr.setStyle("table599_button367", "visible", "false");
            ifr.setStyle("table599_button368", "visible", "false");
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                ifr.setStyle("F_FeeCharges", "visible", "true");
            }
            Log.consoleLog(ifr, " fromLoadMaker VL Code End");
        }
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            Log.consoleLog(ifr, "inside fromLoadMaker Budget ");
            ifr.setStyle("DD_CB_BUREAU_ID", "visible", "false");
            ifr.setTabStyle("tab1", "9", "visible", "true");
            //ifr.setTabStyle("tab1", "127", "visible", "true");

            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_ESIGN_ID,F_Mortgage,F_CollateralSummary";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            String InVisibleBtn = "QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank,BTN_FetchScoreRating";
            pcm.controlinvisiblity(ifr, InVisibleBtn);
            Log.consoleLog(ifr, " fromLoadMaker Budget Code End");
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "inside fromLoadMaker Pension ");
            ifr.setStyle("DD_CB_BUREAU_ID", "visible", "false");
            ifr.setTabStyle("tab1", "9", "visible", "true");
            ifr.setTabStyle("tab1", "0", "visible", "false");
            //ifr.setTabStyle("tab1", "127", "visible", "true");
            ifr.setStyle("Btn_Refresh", "disable", "false");
            String visibleSectionsGrids = "F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "F_CoborrowerEligibility_Sec,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,F_ESIGN_ID,F_Mortgage,F_CollateralSummary,F_CoborrowerEligibility_Sec";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            Log.consoleLog(ifr, " fromLoadMaker Budget Code End");
        } else {
            Log.consoleLog(ifr, "inside fromLoadMaker loanselected empty ");
        }
        String InvisibleSectionsGrids = "ALV_UPLOAD_DOCUMENT1";
        if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
            pcm.controlDisable(ifr, InvisibleSectionsGrids);
            ifr.setStyle("CTRID_PD_FETCHEXTCUST", "visible", "false");
            ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
            ifr.setStyle("F_Sign_Document", "visible", "false");
            ifr.setStyle("F_E-Sign", "visible", "false");
            ifr.setStyle("F_ESIGN_ID", "visible", "false");
            ifr.setStyle("ALV_UPLOAD_DOCUMENT1", "visible", "false");
            if ("No".equals(isPortal)) {
                ifr.setStyle("F_Bureau_Consent", "visible", "false");
            }
        }
        ifr.setStyle("table628_checkbox32", "visible", "false");
        ifr.setStyle("table628_checkbox31", "visible", "false");
        ifr.setColumnVisible("ALV_SCORERATING", "2", false);
        ifr.setColumnVisible("ALV_SCORERATING", "3", false);
        ifr.setColumnVisible("ALV_SCORERATING", "4", false);
        ifr.setColumnVisible("ALV_SCORERATING", "5", false);
        ifr.setColumnVisible("ALV_SCORERATING", "6", false);
        ifr.setStyle("F_CoborrowerEligibility_Sec", "visible", "false");
        ifr.setStyle("add_ALV_AL_LIAB_VAL", "visible", "true");
    }

    public void OnLoadActionHistory(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadRiskScoreRatigMaker AcceleratorActivityManagerCode ");
        bbcc.OnLoadActionHistory(ifr);
    }

    public String mImplSetAadharDetailsInPartyDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mAccCheckExistingCustomer AcceleratorActivityManagerCode ");
        String msg = bbcc.checkExistingCustomer(ifr, Control);
        return msg;
    }

    public void ImplLoadExistingCust(IFormReference ifr, String Control, String Event, String JSdata) {

        Log.consoleLog(ifr, "inside ImplLoadExistingCust ");

        String Query = ConfProperty.getQueryScript("LoadExistingCust");

        Log.consoleLog(ifr, "insidenull " + Query);

        cf.loadComboValues(ifr, "CTRID_PD_EXTCUST", Query, "", "LV");

        //ifr.setValue("CTRID_PD_EXTCUST", "No");
        //added by vandana
        Log.consoleLog(ifr, "inside ImplLoadExistingCust AcceleratorActivityManagerCode ");

        WDGeneralData Data = ifr.getObjGeneralData();

        String ProcessInstanceId = Data.getM_strProcessInstanceId();

        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");

        String loan_selected = loanSelected.get(0).get(0);

        Log.consoleLog(ifr, "loan type==>" + loan_selected);

        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {

            Log.consoleLog(ifr, "inside fromLoadMaker Budget party details");
            String visibleFields = "CTRID_PD_EXTCUST,CTRID_PD_PARTYTYPE,CTRID_PD_SRCHPARM,CTRID_PD_SRCHVAL,CTRID_PD_FETCHEXTCUST,CTRID_PD_RESETDET,CTRID_PD_PARTYTYPE,BTN_Dedupe_Click";

            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                Log.consoleLog(ifr, "inside fromLoadMaker Budget party details linear fields visible for Lead Capture");
                pcm.controlvisiblity(ifr, visibleFields);
            } else {
                Log.consoleLog(ifr, "inside fromLoadMaker Budget party details linear fields InVisible for Other Work item");
                pcm.controlinvisiblity(ifr, visibleFields);
            }

            Log.consoleLog(ifr, "party details ImplLoadExistingCust Budget Code End ");

        }

    }

    //Common for All Journey's. Check with Leads before Modifying.
    public String mAccChangeVariantType(IFormReference ifr, String Control, String Event, String value) {

        Log.consoleLog(ifr, "into change Variant type :: value" + value);
        Log.consoleLog(ifr, "into change Variant type :: Schedule Code  " + value);

        cm.mLoadSchemeId(ifr);
        if (ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString().equalsIgnoreCase("STP-CB")) {
            cm.mLoadROIType(ifr);
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "false");
        }

        return "";

    }

    public String OnChangeProposedLoanSection(IFormReference ifr, String Control, String Event, String value) {

        Log.consoleLog(ifr, "inside OnChange State ProposedLoanGrid AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        JSONObject returnJSON = new JSONObject();

        ifr.setColumnVisible("ALV_PROPOSED_FACILITY", "1", false);
        ifr.setColumnVisible("ALV_PROPOSED_FACILITY", "5", false);
        ifr.setColumnVisible("ALV_PROPOSED_FACILITY", "8", false);
        ifr.setStyle("add_ALV_PROPOSED_FACILITY", "disable", "true");
        Log.consoleLog(ifr, " OnChangeProposedLoanSection Column Visibility Code End");
        // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);

        String[] FieldVisibleFalse = new String[]{
            "ALV_ExistingCIFDetailsOD",
            "LoanAmount",
            "ProductType",
            "label370",
            "BTN_FETCHEXISTINGOD",
            "QNL_ExistingDepositDetailsOD_DepositType",
            "QNL_ExistingDepositDetailsOD_DepositNumber",
            "QNL_ExistingDepositDetailsOD_DepositAmount",
            "QNL_ExistingDepositDetailsOD_DepositMaturityDate",
            "QNL_ExistingDepositDetailsOD_DepositRate",
            "QNL_ExistingDepositDetailsOD_DepositMode"
        };
        for (String FieldVisibleFalse1 : FieldVisibleFalse) {
            ifr.setStyle(FieldVisibleFalse1, "visible", "false");
        }

        if (loan_selected.equalsIgnoreCase("Canara Budget")) {

//            bbcc.dataPopulationInPl(ifr);
            Log.consoleLog(ifr, "inside OnChangeProposedLoanSection proposed details population ");
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String purpose = "";
            String ProposedLoanAmount = null;
            String LoanAmountStr = null;
            String loanTenure = null;
            String loanROI = pcm.mGetROICB(ifr);
            String loanDetailQuery = "select LOANPURPOSE,IF_OTHERS_PLEASE_SPECIFY,ROITYPE,RLLR,FRP,EMI_DATE from los_nl_proposed_facility where PID='" + PID + "'";
            String ifothers = "";
            String roitype = "";
            String rllr = "";
            String frp = "";
            String preemidate = "";

            String roiID = pcm.mGetRoiID(ifr);
            Log.consoleLog(ifr, "roiID:" + roiID);

            Log.consoleLog(ifr, "loanROI : " + loanROI);

            String minTerm = "";
            String maxTerm = "";
            String RepaymnetFrequency = "";
            String requestedLoanAmountWord = "";
            String productvariance = "";
            String variance = "";
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
            String loanAmountInWord = "SELECT convert_to_indian_words(REQLOANAMT) FROM los_nl_proposed_facility WHERE PID='" + PID + "'";
            List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
            if (loanAmountInWords.size() > 0) {
                requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
            }

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
            //String varianceQuery="select PRODUCT_VARIANCE,INTEREST_VARIANCE from los_m_roi where roiid='R21' and CRG='CRG-3'";

            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, loanDetailQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Log.consoleLog(ifr, "inside proposed grid ");
                purpose = PurposePortal.get(0).get(0);
                ifothers = PurposePortal.get(0).get(1);
                if (ifothers.equalsIgnoreCase("") || ifothers.isEmpty()) {
                    Log.consoleLog(ifr, "inside if others");
                    ifothers = "no others";
                }
            }
            roitype = PurposePortal.get(0).get(2);
            rllr = PurposePortal.get(0).get(3);
            frp = PurposePortal.get(0).get(4);
            preemidate = PurposePortal.get(0).get(5);
            Log.consoleLog(ifr, "preemidate :" + preemidate);
            String CRG = pcm.mGetCRG(ifr);

            String varianceQuery = ConfProperty.getQueryScript("varianceQuery").replaceAll("#CRG#", CRG).replaceAll("#roiid#", roiID);
            List<List<String>> varianceListQuery = cf.mExecuteQuery(ifr, varianceQuery, "Execute query for fetching varianceQuery");
            if (varianceListQuery.size() > 0) {
                productvariance = varianceListQuery.get(0).get(0);
                variance = varianceListQuery.get(0).get(1);

            }
            int gridRowCount = ifr.getDataFromGrid("ALV_PROPOSED_FACILITY").size();
            Log.consoleLog(ifr, "gridRowCount-----" + gridRowCount);

            if (gridRowCount == 0) {
                Log.consoleLog(ifr, "Inside adddatatogrid :: " + gridRowCount);

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
                formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_EMI", preemidate);
                formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance", productvariance);
                formDetailsJson.put("QNL_LOS_PROPOSED_FACILITY_Variance", variance);

                gridResultSet.add(formDetailsJson);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + gridResultSet);
                ((IFormAPIHandler) ifr).addDataToGrid("ALV_PROPOSED_FACILITY", gridResultSet, true);

            } else {
                Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::");

//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 2, productCode);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 3, subProductCode);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 4, purpose);
                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, loanTenure);
                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 7, ProposedLoanAmount);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 9, loanROI);
                returnJSON.put("saveWorkitem", "true");
                return returnJSON.toString();
            }
            Log.consoleLog(ifr, "OnChange Section ProposedLoanGrid Budget Code End");

        }
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            Log.consoleLog(ifr, "inside OnChange State ProposedLoanGrid Pension ");
            String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);

            String Purpose = null;
            // String PurposeQuery = "SELECT PURPOSE FROM LOS_NL_Occupation_INFO a INNER JOIN  LOS_NL_BASIC_INFO b ON a.F_KEY=b.F_KEY WHERE b.APPLICANTTYPE ='B' AND b.PID='" + ProcessInstanceId + "'";
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String loanTenure = null;
            String loanROI = null;
            // String roiQuery = "select ROI from LOS_PEN_PRINCIPLE_APPROVAl where PID='" + ProcessInstanceId + "'";
            String roiQuery = ConfProperty.getQueryScript("roiQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> roiList = cf.mExecuteQuery(ifr, roiQuery, "Execute query for fetching Roi data from portal in principal");
            if (roiList.size() > 0) {
                loanROI = roiList.get(0).get(0);
            }
            String roiID = pcm.mGetRoiID(ifr);
            Log.consoleLog(ifr, "roiID:" + roiID);

            String ProposedLoanAmount = null;
            String LoanAmountStr = null;
            // String LoanAMTQuery = "select LOAN_AMOUNT,ROI from LOS_PEN_PRINCIPLE_APPROVAl where PID='" + ProcessInstanceId + "'";
            // String LoanAMTQuery = "select * from LOS_PORTAL_SLIDERVALUE where PID='" + ProcessInstanceId + "'";
            String LoanAMTQuery = ConfProperty.getQueryScript("LOS_PORTAL_SLIDERVALUEQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);

            List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTQuery, "Execute query for fetching Loan amount data from portal in principal");
            if (LoanAmountList.size() > 0) {
                LoanAmountStr = LoanAmountList.get(0).get(1);
                ProposedLoanAmount = String.valueOf(Math.round(Double.parseDouble(LoanAmountStr)));
                loanTenure = LoanAmountList.get(0).get(2);
                Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            }
            Log.consoleLog(ifr, "roi : " + loanROI);
            Log.consoleLog(ifr, "ProposedLoanAmount : " + ProposedLoanAmount);
            String query2 = "select SUBPRODUCTNAME ,SUBPRODUCTCODE from LOS_M_subproduct where SUBPRODUCTCODE='" + subProductCode + "'";
            List<List<String>> subProductName = cf.mExecuteQuery(ifr, query2, "Execute query for fetching subProductName1  ");
            String subProductName1 = subProductName.get(0).get(1);
            Log.consoleLog(ifr, "subProductName1==>" + subProductName1);
            int ProposedLoanSize = ifr.getDataFromGrid("ALV_PROPOSED_FACILITY").size();
            Log.consoleLog(ifr, "inside ALV_PROPOSED_FACILITY::" + ProposedLoanSize);
            if (ProposedLoanSize <= 0) {
                Log.consoleLog(ifr, "inside ALV_PROPOSED_FACILITY::" + ProposedLoanSize);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                obj.put("QNL_LOS_PROPOSED_FACILITY_SubProduct", subProductName1);
                //obj.put("QNL_LOS_PROPOSED_FACILITY_LoanPurpose", Purpose);
                obj.put("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", ProposedLoanAmount);
                obj.put("QNL_LOS_PROPOSED_FACILITY_Tenure", loanTenure);
                obj.put("QNL_LOS_PROPOSED_FACILITY_ROI", loanROI);
                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);

                ((IFormAPIHandler) ifr).addDataToGrid("ALV_PROPOSED_FACILITY", jsonarr, true);
                Log.consoleLog(ifr, "Proposed loan Added==>");

            }
            Log.consoleLog(ifr, " OnChangeProposedLoanSection Pension Code End");
        }
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "inside OnChangeProposedLoanSection VL ");
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String subProductCode = pcm.mGetSubproductVL(ifr, Purpose);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String query1 = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            List<List<String>> selectedLoan = cf.mExecuteQuery(ifr, query1, "Execute query for fetching loan selected ");
            String selectedLoan1 = selectedLoan.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + selectedLoan1);
            String query2 = "select SUBPRODUCTNAME ,SUBPRODUCTCODE from LOS_M_subproduct where SUBPRODUCTCODE='" + subProductCode + "'";
            List<List<String>> subProductName = cf.mExecuteQuery(ifr, query2, "Execute query for fetching subProductName1  ");
            String subProductName1 = subProductName.get(0).get(1);
            String subProdName = subProductName.get(0).get(0);
            Log.consoleLog(ifr, "subProductName1==>" + subProductName1);
            String query3 = "select PURPOSENAME,PURPOSECODE from los_m_purpose where PURPOSECODE='" + Purpose + "'";
            List<List<String>> purposeCodeName = cf.mExecuteQuery(ifr, query3, "Execute query for fetching ProductCodeName1 ");
            String purposeCodeName1 = purposeCodeName.get(0).get(1);
            String purposeName = purposeCodeName.get(0).get(0);
            Log.consoleLog(ifr, "ProductCodeName1==>" + purposeCodeName1);

            String prodNameQuery = "select PRODUCTNAME, PRODUCTCODE from LOS_M_PRODUCT where PRODUCTCODE='" + productCode + "'";
            List<List<String>> productName = cf.mExecuteQuery(ifr, prodNameQuery, "Execute query for fetching productName1  ");
            String prodName = productName.get(0).get(0);
            Log.consoleLog(ifr, "productName1==>" + prodName);

            String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
            Log.consoleLog(ifr, "roiID:" + roiID);
            String loanROI = null;
            String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
            List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
            if (list2.size() > 0) {
                loanROI = list2.get(0).get(0);
            }
            Log.consoleLog(ifr, "roi : " + loanROI);
            String requestedLoanAmountWord = "";
            String loanAmountInWord = "SELECT convert_to_indian_words(\"Loan_Amount\") FROM LOS_PORTAL_SLIDERVALUE WHERE PID='" + ProcessInstanceId + "'";
            List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
            if (loanAmountInWords.size() > 0) {
                requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
            }
            String queryV = "Select Variantname,Variantcode From Los_M_Variant  Where Isactive='Y' And Variantcode In (Select Variantcode From Los_M_Product_Rlos  Where Productcode='" + productCode + "' and subproductcode='" + subProductCode + "' and PURPOSECODE='" + Purpose + "' and IsActive='Y') ORDER BY VARIANTNAME";
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load Variant:: ");
            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                String value1 = list.get(i).get(1);
                Log.consoleLog(ifr, "value1 ::  " + value1);
                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Variant", label, value1);
            }
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);
            String ProposedLoanAmount = null;
            String LoanAmountStr = null;
            String loanTenure = null;
            String LoanAMTTenureQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTTenureQuery, "Execute query for fetching Slider Loan amount,Tenure data from portal in principal");
            if (LoanAmountList.size() > 0) {
                LoanAmountStr = LoanAmountList.get(0).get(1);
                loanTenure = LoanAmountList.get(0).get(2);
                ProposedLoanAmount = String.valueOf(Math.round(Double.parseDouble(LoanAmountStr)));
            }
            Log.consoleLog(ifr, "Slider ProposedLoanAmount : " + ProposedLoanAmount);
            Log.consoleLog(ifr, "Slider loanTenure : " + loanTenure);
            cm.mImplNumberToWorkds(ifr);
            int ProposedLoanSize = ifr.getDataFromGrid("ALV_PROPOSED_FACILITY").size();
            Log.consoleLog(ifr, "ProposedLoanSize ::" + ProposedLoanSize);
            JSONObject obj = new JSONObject();
            JSONArray jsonarr = new JSONArray();
            if (ProposedLoanSize == 0) {
                Log.consoleLog(ifr, "inside add data to grid ALV_PROPOSED_FACILITY::" + ProposedLoanSize);
//                obj.put("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                obj.put("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                obj.put("QNL_LOS_PROPOSED_FACILITY_SubProduct", subProductName1);
                obj.put("QNL_LOS_PROPOSED_FACILITY_LoanPurpose", purposeCodeName1);
                obj.put("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt", ProposedLoanAmount);
                obj.put("QNL_LOS_PROPOSED_FACILITY_Tenure", loanTenure);
                obj.put("QNL_LOS_PROPOSED_FACILITY_ROI", loanROI);
                obj.put("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", loanROI);
                obj.put("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);
                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                ((IFormAPIHandler) ifr).addDataToGrid("ALV_PROPOSED_FACILITY", jsonarr, true);
                Log.consoleLog(ifr, "Proposed loan row Added==>");
            } else {
                Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::" + ProposedLoanSize);
                // ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 2, productCode);
                /*ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 2, prodName);
                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 3, subProductName1);
                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 4, purposeName);
                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, loanTenure);*/

                //Commented by aravindh on 8/8/24 for Product subproduct population. As of now Data population getting overrided. 
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 2, productCode);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 3, subProductName1);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 4, purposeCodeName1);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, loanTenure);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 7, ProposedLoanAmount);
//                ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 9, loanROI);
                Log.consoleLog(ifr, "Proposed loan data setted to 0th index row==>");
            }
            Log.consoleLog(ifr, "AcceleratorActivityManagerCode : OnChangeProposedLoanSection : productName : " + prodName
                    + " , subProductName1 : " + subProdName + " , purposeCodeName1 : " + purposeName
                    + " , loanTenure : " + loanTenure);
            /*ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 2, prodName);
            ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 3, subProdName);
            ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 4, purposeName);
            ifr.setTableCellValue("ALV_PROPOSED_FACILITY", 0, 6, loanTenure + " Months");*/
            Log.consoleLog(ifr, " OnChangeProposedLoanSection VL Code End");
        }
        Log.consoleLog(ifr, " OnChangeProposedLoanSection Code End");
        return "";
    }

    public String OnLoadPartyDetails(IFormReference ifr, String Control, String Event, String value) {

        try {
            Log.consoleLog(ifr, "inside OnLoadPartyDetails AcceleratorActivityManagerCode ");
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            ifr.setStyle("LV_ADDRESS", "visible", "true");
            ifr.setStyle("LV_KYC", "visible", "true");
            ifr.setStyle("LV_MIS_DATA", "visible", "true");

            String PartyType = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();
            Log.consoleLog(ifr, "PartyType ::" + PartyType);
            if (PartyType.equalsIgnoreCase("B") || PartyType.equalsIgnoreCase("CB")) {
                ifr.setStyle("LV_OCCUPATION_INFO", "visible", "true");
                Log.consoleLog(ifr, " OCCUPATION_INFO is Visible for Borrower::");
            }
            // Devarinti Vandana
            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                ifr.addItemInCombo("QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality", "Indian");

                ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality", "Indian");

            }

            if (loan_selected.equalsIgnoreCase("Canara Budget")) {

                Log.consoleLog(ifr, "inside fromLoadMaker Budget ");
                ifr.setStyle("LV_GUARANTOR", "visible", "false");
                ifr.setStyle("F_P_ExistingRelationship", "visible", "false");
                ifr.setStyle("duplicateAdvancedListviewchanges_ALV_BASIC_INFO", "disable", "true"); //modified by Vandana 17/06/2024
                String ActivityName = ifr.getActivityName();
                String VisibleFieldsB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                String VisibleFieldsCB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName"
                        + "QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO.AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_Relationshipwithapplicant,QNL_BASIC_INFO_FullName";

                String editable = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents";

                pcm.controlvisiblity(ifr, VisibleFieldsB);
                pcm.controlvisiblity(ifr, VisibleFieldsCB);
                String nonEditableFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_FullName";//QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality               
                pcm.controlvisiblity(ifr, VisibleFieldsB);
                String InvisibleFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,table565_textbox6838,QNL_BASIC_INFO_customerFlag,table565_textbox6908,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_MotherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_KeyPersonName,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ConstitutionType,table565_datepick2,QNL_BASIC_INFO_CL_BASIC_INFO_NI_IndustryType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifyIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifySubIndustry,table565_datepick375,table565_datepick97,QNL_BASIC_INFO_CL_BASIC_INFO_NI_RegistrationNumber,table565_datepick101,table565_datepick102,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofYearsinBusiness,mQNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofEmployees,QNL_BASIC_INFO_CL_BASIC_INFO_NI_CountryOfIncorp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_TurnOverm,QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BusinessActivity";//(table565_combo1754-recovery mechanism)
                pcm.controlDisable(ifr, nonEditableFields);
                String InvisibleFieldsCB = "QNL_BASIC_INFO_SelectSalaryAccount";
                pcm.controlinvisiblity(ifr, InvisibleFields);
                String NonEditableFileds = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";
                String NonEditable = "QNL_BASIC_INFO_ApplicantType,QNL_BASIC_INFO_EntityType,QNL_BASIC_INFO_ExistingCustomer,QNL_BASIC_INFO_CustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo,QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste,QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency_status";

                Log.consoleLog(ifr, " partyDetailsSection InvisibleFields;:" + InvisibleFields);
                String CustAccopen = "";
                String natureofSecurity = "";
                String recoveryMechanism = "";
                String relationMonths = "";
                String Residence = "";
                String relationyears = "";
                String salaryAccount = "";
                String NRI = "";
                String SETTLEDACCOUNTINCREDITHISTORY = "";
                String OVERDUEINCREDITHISTORY = "";
                String SALARYCREDITEDTHROUGHBANK = "";
                String QUALIFICATION = "";
                String CustAccOpen = "";

                //Changes added by Logaraj for introduction of new field Applicant full name
                String firstName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
                String middleName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
                String LastName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
                String Fullname = concatFullname(firstName, middleName, LastName);
                Log.consoleLog(ifr, " partyDetailsSection Fullname;:" + Fullname);
                ifr.setValue("QNL_BASIC_INFO_FullName", Fullname);

//NRI,SETTLEDACCOUNTINCREDITHISTORY,OVERDUEINCREDITHISTORY,SALARYACCOUNTWITHCANARA--QUALIFICATION
                //added by sharon for onclick of B Data
                if (PartyType.equalsIgnoreCase("B")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");
                    pcm.controlvisiblity(ifr, VisibleFieldsB);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    pcm.controlEnable(ifr, editable);
                    ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "visible", "false");//modified by Vandana 17/06/2024
                    String MartialStatus = ifr.getValue("QNL_BASIC_INFO_CL_BASIC _INFO_I_MaritalStatus").toString();
                    Log.consoleLog(ifr, "OccuppationInfoDetails MartialStatus BORROWER::" + MartialStatus);
                    salaryAccount = Bpcm.Accountdetail(ifr);

//for calculating age based on the dob
                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        // Parse the date string into a Date object
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

//added by sharon Code for Disbursal Account
                    String CustomerId = pcm.getCustomerIDCB(ifr, PartyType);
                    String MobileNumber = pcm.getMobileNumber(ifr);
                    String mobNum = "91" + MobileNumber;
                    Log.consoleLog(ifr, "mobilenumber::" + MobileNumber);
                    Log.consoleLog(ifr, "CustomerId::" + CustomerId);
                    //String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
                    Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
                    String response360 = objCbs360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "Canara Budget");
                    Log.consoleLog(ifr, "response==>" + response360);
                    if (response360.contains(RLOS_Constants.ERROR)) {
                        Log.consoleLog(ifr, "inside error condition 360API LAD");
                        return pcm.returnError(ifr);
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
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:AccountId::" + AccountId);

                                CustAccopen = InputStringResponseJSON.get("DatAcctOpen").toString().trim();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:CustAccopen::" + CustAccopen);
                                String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:BranchCode::" + BranchCode);
                                //ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId, AccountId + "-" + BranchCode + "-" + strAcctOpen + "-" + strAcctbal + "-" + stractprdcode);
                                ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId);
                            }
                        }
                    }
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation::");

                    String FkeyB = Bpcm.Fkey(ifr, ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString());
//                    String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#", ProcessInstanceId);
//                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + IndexQuery);
//                    List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query BORROWER ");
//                    if (!dataResult.isEmpty()) {
//                        FkeyB = dataResult.get(0).get(0);
//                    }
                    Log.consoleLog(ifr, "dataResult fKey B::" + FkeyB);
                    String QueryB = "SELECT o.recovery_mechanism,o.nature_of_security,o.relationshipcanara_inmonths,o.RESIDENCE, o.RELATIONSHIPCANARA,o.QUALIFICATION,b.NRI,b.SETTLEDACCOUNTINCREDITHISTORY,b.OVERDUEINCREDITHISTORY,b.SALARYCREDITEDTHROUGHBANK FROM los_nl_occupation_info o JOIN los_nl_basic_info b ON o.F_KEY = b.F_KEY WHERE o.F_KEY = '" + FkeyB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + QueryB);
                    List<List<String>> getdatafromoccuB = cf.mExecuteQuery(ifr, QueryB, "occupation Fkey query Co-Obligant ");
                    Log.consoleLog(ifr, "getdatafromoccuB  fKeyB::" + getdatafromoccuB);
                    if (!getdatafromoccuB.isEmpty()) {

                        recoveryMechanism = getdatafromoccuB.get(0).get(0);
                        natureofSecurity = getdatafromoccuB.get(0).get(1);
                        relationMonths = getdatafromoccuB.get(0).get(2);
                        Residence = getdatafromoccuB.get(0).get(3);
                        relationyears = getdatafromoccuB.get(0).get(4);
                        QUALIFICATION = getdatafromoccuB.get(0).get(5);
                        NRI = getdatafromoccuB.get(0).get(6);
                        SETTLEDACCOUNTINCREDITHISTORY = getdatafromoccuB.get(0).get(7);
                        OVERDUEINCREDITHISTORY = getdatafromoccuB.get(0).get(8);
                        SALARYCREDITEDTHROUGHBANK = getdatafromoccuB.get(0).get(9);

                        Log.consoleLog(ifr, "OccuppationInfoDetails recoveryMechanism BORROWER::" + recoveryMechanism);
                        Log.consoleLog(ifr, "OccuppationInfoDetails natureofSecurity BORROWER::" + natureofSecurity);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths BORROWER::" + relationMonths);
                        Log.consoleLog(ifr, "OccuppationInfoDetails Residence BORROWER::" + Residence);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths BORROWER::" + relationyears);
                        Log.consoleLog(ifr, "OccuppationInfoDetails salaryAccount BORROWER::" + salaryAccount);
                        Log.consoleLog(ifr, "OccuppationInfoDetails QUALIFICATION BORROWER::" + QUALIFICATION);
                        Log.consoleLog(ifr, "OccuppationInfoDetails NRI BORROWER::" + NRI);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SETTLEDACCOUNTINCREDITHISTORY BORROWER::" + SETTLEDACCOUNTINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails OVERDUEINCREDITHISTORY BORROWER::" + OVERDUEINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SALARYCREDITEDTHROUGHBANK BORROWER::" + SALARYCREDITEDTHROUGHBANK);

                        ifr.setValue("QNL_BASIC_INFO_RecoveryMechanism", recoveryMechanism);
                        ifr.setValue("QNL_BASIC_INFO_NatureofSecurity", natureofSecurity);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipwithBankMonths", relationMonths);
                        ifr.setValue("QNL_BASIC_INFO_Residence", Residence);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipWithBank", relationyears);
                        ifr.setValue("QNL_BASIC_INFO_SelectSalaryAccount", salaryAccount);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", QUALIFICATION);
                        if (NRI.equalsIgnoreCase("N")) {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "No");

                        } else {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "Yes");

                        }
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", SETTLEDACCOUNTINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", OVERDUEINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", SALARYCREDITEDTHROUGHBANK);

                    }

                    if (MartialStatus.equalsIgnoreCase("Married")) {

                        Log.consoleLog(ifr, "into marital status condition");

                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "true");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "true");

                    } else {
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "false");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "false");

                    }
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");//modified by sharon 18/06/2024
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);//modified by sharon 18/06/2024
                    // Add vandana
                    String Queryover = "select  settledaccountincredithistory,overdueincredithistory from LOS_NL_BASIC_INFO where PID= '" + ProcessInstanceId + "'  and  applicanttype ='B' ";
                    Log.consoleLog(ifr, "Queryover query BORROWER::" + Queryover);
                    List<List<String>> overhisty = cf.mExecuteQuery(ifr, Queryover, "overduedays_inps  query Obligant ");
                    if (!overhisty.isEmpty()) {
                        String guarantorwriteoff = overhisty.get(0).get(0);
                        String overduedays_inps = overhisty.get(0).get(1);
                        Log.consoleLog(ifr, "overduedays_inps : " + overduedays_inps + " guarantorwriteoff : " + guarantorwriteoff);
                        ifr.addItemInCombo("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", overduedays_inps);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", overduedays_inps);
                        ifr.addItemInCombo("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", guarantorwriteoff);
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", guarantorwriteoff);
                    }
                }

                //added by sharon for onclick of CB Data
                if (PartyType.equalsIgnoreCase("CB")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");

                    pcm.controlvisiblity(ifr, VisibleFieldsCB);
                    pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                    pcm.controlEnable(ifr, editable);
                    ifr.setStyle("LV_MIS_Data", "visible", "false");

//for calculating age based on the dob
                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        // Parse the date string into a Date object
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    String FkeyCB = "";
                    String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", ProcessInstanceId);
                    Log.consoleLog(ifr, "OccuppationInfoDetails1 query CO-BORROWER1::" + IndexQuery1);
                    List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
                    if (!dataResult1.isEmpty()) {
                        FkeyCB = dataResult1.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "dataResult1 fKey CB::" + FkeyCB);
                    String QueryCB = "SELECT o.recovery_mechanism,o.nature_of_security,o.relationshipcanara_inmonths,o.RESIDENCE, o.RELATIONSHIPCANARA,o.QUALIFICATION,b.NRI,b.SETTLEDACCOUNTINCREDITHISTORY,b.OVERDUEINCREDITHISTORY,b.SALARYCREDITEDTHROUGHBANK FROM los_nl_occupation_info o JOIN los_nl_basic_info b ON o.F_KEY = b.F_KEY WHERE o.F_KEY = '" + FkeyCB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + QueryCB);
                    List<List<String>> getdatafromoccuCB = cf.mExecuteQuery(ifr, QueryCB, "occupation Fkey query Co-Obligant ");

                    Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + getdatafromoccuCB);
                    if (!getdatafromoccuCB.equals(null)) {

                        recoveryMechanism = getdatafromoccuCB.get(0).get(0);
                        natureofSecurity = getdatafromoccuCB.get(0).get(1);
                        relationMonths = getdatafromoccuCB.get(0).get(2);
                        Residence = getdatafromoccuCB.get(0).get(3);
                        relationyears = getdatafromoccuCB.get(0).get(4);
                        QUALIFICATION = getdatafromoccuCB.get(0).get(5);
                        NRI = getdatafromoccuCB.get(0).get(6);
                        SETTLEDACCOUNTINCREDITHISTORY = getdatafromoccuCB.get(0).get(7);
                        OVERDUEINCREDITHISTORY = getdatafromoccuCB.get(0).get(8);
                        SALARYCREDITEDTHROUGHBANK = getdatafromoccuCB.get(0).get(9);

                        Log.consoleLog(ifr, "OccuppationInfoDetails recoveryMechanism CO-BORROWER::" + recoveryMechanism);
                        Log.consoleLog(ifr, "OccuppationInfoDetails natureofSecurity CO-BORROWER::" + natureofSecurity);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths CO-BORROWER::" + relationMonths);
                        Log.consoleLog(ifr, "OccuppationInfoDetails Residence CO-BORROWER::" + Residence);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths CO-BORROWER::" + relationyears);
                        Log.consoleLog(ifr, "OccuppationInfoDetails QUALIFICATION CO-BORROWER::" + QUALIFICATION);
                        Log.consoleLog(ifr, "OccuppationInfoDetails NRI CO-BORROWER::" + NRI);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SETTLEDACCOUNTINCREDITHISTORY CO-BORROWER::" + SETTLEDACCOUNTINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails OVERDUEINCREDITHISTORY CO-BORROWER::" + OVERDUEINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SALARYCREDITEDTHROUGHBANK CO-BORROWER::" + SALARYCREDITEDTHROUGHBANK);

                        ifr.setValue("QNL_BASIC_INFO_RecoveryMechanism", recoveryMechanism);
                        ifr.setValue("QNL_BASIC_INFO_NatureofSecurity", natureofSecurity);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipwithBankMonths", relationMonths);
                        ifr.setValue("QNL_BASIC_INFO_Residence", Residence);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipWithBank", relationyears);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", QUALIFICATION);

                        if (NRI.equalsIgnoreCase("N")) {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "No");

                        } else {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "Yes");

                        }
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", SETTLEDACCOUNTINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", OVERDUEINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", SALARYCREDITEDTHROUGHBANK);

                    }

                    String relationshipwithapplicant = "";
                    String RealtionShipWithApplicant = ConfProperty.getQueryScript("RealtionShipWithApplicant").replaceAll("#PID#", ProcessInstanceId);
                    List<List<String>> RealtionShipWithApplicantQuery = cf.mExecuteQuery(ifr, RealtionShipWithApplicant, "RealtionShipWithApplicant Query");
                    if (!RealtionShipWithApplicantQuery.isEmpty()) {
                        relationshipwithapplicant = RealtionShipWithApplicantQuery.get(0).get(0);
                        ifr.setValue("QNL_BASIC_INFO_Relationshipwithapplicant", relationshipwithapplicant);
                        ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "disable", "true");
                    }

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");//modified by sharon 18/06/2024
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);//modified by sharon 18/06/2024

                }

                if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }
                if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker") || ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                    Log.consoleLog(ifr, "inside Disbursement Maker Budget partyDetailsSection controlDisable");
                }
                if (ActivityName.equalsIgnoreCase("Reviewer")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget:Reviewer ");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("P_Industry_Details", "visible", "false");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }

                }

                //added by logaraj
                String nonEditableFieldsSanction = "QNL_BASIC_INFO_ApplicantType,QNL_BASIC_INFO_EntityType,QNL_BASIC_INFO_ExistingCustomer,QNL_BASIC_INFO_CustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo,QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste,QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency_status,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY";
                if (ActivityName.equalsIgnoreCase("Sanction")) {

                    Log.consoleLog(ifr, "inside partyDetailsSection Budget Code End:Sanction");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    pcm.controlDisable(ifr, nonEditableFieldsSanction);
                    pcm.controlDisable(ifr, NonEditable);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                }
                if (ActivityName.equalsIgnoreCase("Convenor")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget: Convenor");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }
                if (ActivityName.equalsIgnoreCase("Deviation")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget: Deviation");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }

                if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    pcm.controlDisable(ifr, nonEditableFieldsSanction);
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                    Log.consoleLog(ifr, "inside PostSanction Budget partyDetailsSection controlDisable");
                }

                //LV_MIS_Data
                ifr.setColumnVisible("LV_MIS_Data", "5", false);
                ifr.setColumnVisible("LV_MIS_Data", "6", false);
                ifr.setColumnVisible("LV_MIS_Data", "7", false);
                ifr.setColumnVisible("LV_MIS_Data", "8", false);
                ifr.setColumnVisible("LV_MIS_Data", "9", false);
                ifr.setColumnVisible("LV_MIS_Data", "10", false);

                //LV_OCCUPATION_INFO Modified by Aravindh on 07/06/24
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "0", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "1", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "66", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", false);

                Log.consoleLog(ifr, " OnLoadPartyDetails Budget Code End");
            }
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {

                Log.consoleLog(ifr, "inside fromLoadMaker Pension ");
                ifr.setStyle("LV_GUARANTOR", "visible", "false");
                ifr.setStyle("F_P_ExistingRelationship", "visible", "false");
                ifr.setStyle("duplicateAdvancedListviewchanges_ALV_BASIC_INFO", "disable", "true"); //modified by Vandana 17/06/2024
                String ActivityName = ifr.getActivityName();
                String VisibleFieldsB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                String VisibleFieldsCB = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName"
                        + "QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO.AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_Relationshipwithapplicant,QNL_BASIC_INFO_FullName";

                String editable = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents";

                pcm.controlvisiblity(ifr, VisibleFieldsB);
                pcm.controlvisiblity(ifr, VisibleFieldsCB);
                String nonEditableFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_FullName,QNL_BASIC_INFO_SelectSalaryAccount";//QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality               
                pcm.controlvisiblity(ifr, VisibleFieldsB);
                String InvisibleFields = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,table565_textbox6838,QNL_BASIC_INFO_customerFlag,table565_textbox6908,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_MotherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_KeyPersonName,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ConstitutionType,table565_datepick2,QNL_BASIC_INFO_CL_BASIC_INFO_NI_IndustryType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifyIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifySubIndustry,table565_datepick375,table565_datepick97,QNL_BASIC_INFO_CL_BASIC_INFO_NI_RegistrationNumber,table565_datepick101,table565_datepick102,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofYearsinBusiness,mQNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofEmployees,QNL_BASIC_INFO_CL_BASIC_INFO_NI_CountryOfIncorp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_TurnOverm,QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BusinessActivity";
                pcm.controlDisable(ifr, nonEditableFields);
                String InvisibleFieldsCB = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_SalaryTieUp";
                pcm.controlinvisiblity(ifr, InvisibleFields);
                String NonEditableFileds = "QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT"
                        + "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                Log.consoleLog(ifr, " partyDetailsSection InvisibleFields;:" + InvisibleFields);
                String CustAccopen = "";
                String natureofSecurity = "";
                String recoveryMechanism = "";
                String relationMonths = "";
                String Residence = "";
                String relationyears = "";
                String salaryAccount = "";
                String NRI = "";
                String SETTLEDACCOUNTINCREDITHISTORY = "";
                String OVERDUEINCREDITHISTORY = "";
                String SALARYCREDITEDTHROUGHBANK = "";
                String QUALIFICATION = "";
                String CustAccOpen = "";
                String firstName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
                String middleName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
                String LastName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
                String Fullname = concatFullname(firstName, middleName, LastName);
                Log.consoleLog(ifr, " partyDetailsSection Fullname;:" + Fullname);
                ifr.setValue("QNL_BASIC_INFO_FullName", Fullname);
                if (PartyType.equalsIgnoreCase("B")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");
                    pcm.controlvisiblity(ifr, VisibleFieldsB);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    pcm.controlEnable(ifr, editable);
                    ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "visible", "false");//modified by Vandana 17/06/2024
                    String MartialStatus = ifr.getValue("QNL_BASIC_INFO_CL_BASIC _INFO_I_MaritalStatus").toString();
                    Log.consoleLog(ifr, "OccuppationInfoDetails MartialStatus BORROWER::" + MartialStatus);
                    salaryAccount = Bpcm.Accountdetail(ifr);
                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        // Parse the date string into a Date object
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    String CustomerId = pcm.getCustomerIDCB(ifr, PartyType);
                    String MobileNumber = pcm.getMobileNumber(ifr);
                    String mobNum = "91" + MobileNumber;
                    Log.consoleLog(ifr, "mobilenumber::" + MobileNumber);
                    Log.consoleLog(ifr, "CustomerId::" + CustomerId);
                    //String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
                    Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
                    String response360 = objCbs360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "Canara Budget");
                    Log.consoleLog(ifr, "response==>" + response360);
                    if (response360.contains(RLOS_Constants.ERROR)) {
                        Log.consoleLog(ifr, "inside error condition 360API LAD");
                        return pcm.returnError(ifr);
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
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:AccountId::" + AccountId);

                                CustAccopen = InputStringResponseJSON.get("DatAcctOpen").toString().trim();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:CustAccopen::" + CustAccopen);
                                String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
                                Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:BranchCode::" + BranchCode);
                                //ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId, AccountId + "-" + BranchCode + "-" + strAcctOpen + "-" + strAcctbal + "-" + stractprdcode);
                                ifr.addItemInCombo("QNL_BASIC_INFO_SelectSalaryAccount", AccountId);
                            }
                        }
                    }
                    Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation::");

                    String FkeyB = Bpcm.Fkey(ifr, ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString());
                    Log.consoleLog(ifr, "dataResult fKey B::" + FkeyB);
                    String QueryB = "SELECT o.recovery_mechanism,o.nature_of_security,o.relationshipcanara_inmonths,o.RESIDENCE, o.RELATIONSHIPCANARA,o.QUALIFICATION,b.NRI,b.SETTLEDACCOUNTINCREDITHISTORY,b.OVERDUEINCREDITHISTORY,b.SALARYCREDITEDTHROUGHBANK FROM los_nl_occupation_info o JOIN los_nl_basic_info b ON o.F_KEY = b.F_KEY WHERE o.F_KEY = '" + FkeyB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + QueryB);
                    List<List<String>> getdatafromoccuB = cf.mExecuteQuery(ifr, QueryB, "occupation Fkey query Co-Obligant ");
                    Log.consoleLog(ifr, "getdatafromoccuB  fKeyB::" + getdatafromoccuB);
                    if (!getdatafromoccuB.isEmpty()) {

                        recoveryMechanism = getdatafromoccuB.get(0).get(0);
                        natureofSecurity = getdatafromoccuB.get(0).get(1);
                        relationMonths = getdatafromoccuB.get(0).get(2);
                        Residence = getdatafromoccuB.get(0).get(3);
                        relationyears = getdatafromoccuB.get(0).get(4);
                        QUALIFICATION = getdatafromoccuB.get(0).get(5);
                        NRI = getdatafromoccuB.get(0).get(6);
                        SETTLEDACCOUNTINCREDITHISTORY = getdatafromoccuB.get(0).get(7);
                        OVERDUEINCREDITHISTORY = getdatafromoccuB.get(0).get(8);
                        SALARYCREDITEDTHROUGHBANK = getdatafromoccuB.get(0).get(9);

                        Log.consoleLog(ifr, "OccuppationInfoDetails recoveryMechanism BORROWER::" + recoveryMechanism);
                        Log.consoleLog(ifr, "OccuppationInfoDetails natureofSecurity BORROWER::" + natureofSecurity);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths BORROWER::" + relationMonths);
                        Log.consoleLog(ifr, "OccuppationInfoDetails Residence BORROWER::" + Residence);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths BORROWER::" + relationyears);
                        Log.consoleLog(ifr, "OccuppationInfoDetails salaryAccount BORROWER::" + salaryAccount);
                        Log.consoleLog(ifr, "OccuppationInfoDetails QUALIFICATION BORROWER::" + QUALIFICATION);
                        Log.consoleLog(ifr, "OccuppationInfoDetails NRI BORROWER::" + NRI);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SETTLEDACCOUNTINCREDITHISTORY BORROWER::" + SETTLEDACCOUNTINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails OVERDUEINCREDITHISTORY BORROWER::" + OVERDUEINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SALARYCREDITEDTHROUGHBANK BORROWER::" + SALARYCREDITEDTHROUGHBANK);

                        ifr.setValue("QNL_BASIC_INFO_RecoveryMechanism", recoveryMechanism);
                        ifr.setValue("QNL_BASIC_INFO_NatureofSecurity", natureofSecurity);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipwithBankMonths", relationMonths);
                        ifr.setValue("QNL_BASIC_INFO_Residence", Residence);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipWithBank", relationyears);
                        ifr.setValue("QNL_BASIC_INFO_SelectSalaryAccount", salaryAccount);
                        Log.consoleLog(ifr, "QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification value Bchk::" + QUALIFICATION);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", QUALIFICATION);
                        if (QUALIFICATION.equals("")) {
                            ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", "disable", "false");
                            ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", "mandatory", "true");
                        }
                        String deduction = "";
                        String deductionCBQ = "SELECT DEDUCTIONMONTH FROM los_nl_occupation_info where F_KEY = '" + FkeyB + "'";
                        Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + deductionCBQ);
                        List<List<String>> deductionCB = cf.mExecuteQuery(ifr, deductionCBQ, "occupation Fkey query Co-Obligant ");

                        Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + deductionCB);
                        if (!deductionCB.isEmpty()) {
                            deduction = deductionCB.get(0).get(0);
                        }
                        Log.consoleLog(ifr, "OccuppationInfoDetails deduction::" + deduction);
                        ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                        int OccupationDetailSize = ifr.getDataFromGrid("LV_OCCUPATION_INFO").size();
                        Log.consoleLog(ifr, "OccupationDetailSize ::" + OccupationDetailSize);
                        if (OccupationDetailSize == 0) {
                            Log.consoleLog(ifr, "inside add data to gridOccupationDetailSize" + OccupationDetailSize);
                            JSONObject obj = new JSONObject();
                            JSONArray jsonarr = new JSONArray();
                            obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                            jsonarr.add(obj);
                            Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                            ((IFormAPIHandler) ifr).addDataToGrid("LV_OCCUPATION_INFO", jsonarr, true);
                            Log.consoleLog(ifr, "Occuppation loan row Added==>");
                        } else {
                            Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::" + OccupationDetailSize);
                            ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 61, deduction);
                            Log.consoleLog(ifr, "Occuppation loan data setted to 0th index row==>");
                        }

                        if (NRI.equalsIgnoreCase("N")) {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "No");

                        } else {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "Yes");

                        }
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", SETTLEDACCOUNTINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", OVERDUEINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", SALARYCREDITEDTHROUGHBANK);

                    }
                    ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", false);
                    ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                    ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", false);
                    ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", true);
                    ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", true);
                    if (MartialStatus.equalsIgnoreCase("Married")) {

                        Log.consoleLog(ifr, "into marital status condition");

                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "true");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "true");

                    } else {
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "false");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "false");

                    }

                    String pensionCredits = "Yes";
                    ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", pensionCredits);
                    ifr.setStyle("QNL_BASIC_INFO_SalaryCreditedthroughBank", "disable", "true");
                    String EmailID = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID").toString();
                    Log.consoleLog(ifr, "EmailIDBORROWER::" + EmailID);

                    if (EmailID.equalsIgnoreCase("") || EmailID.isEmpty()) {
                        Log.consoleLog(ifr, "EmailIDBORROWER1::" + EmailID);
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", "disable", "false");
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", "mandatory", "false");
                    } else {
                        Log.consoleLog(ifr, "EmailIDBORROWER2::" + EmailID);
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", "disable", "true");
                    }

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");//modified by sharon 18/06/2024
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);
                    String Queryover = "select  settledaccountincredithistory,overdueincredithistory from LOS_NL_BASIC_INFO where PID= '" + ProcessInstanceId + "'  and  applicanttype ='B' ";
                    Log.consoleLog(ifr, "Queryover query BORROWER::" + Queryover);
                    List<List<String>> overhisty = cf.mExecuteQuery(ifr, Queryover, "overduedays_inps  query Obligant ");
                    if (!overhisty.isEmpty()) {
                        String guarantorwriteoff = overhisty.get(0).get(0);
                        String overduedays_inps = overhisty.get(0).get(1);
                        Log.consoleLog(ifr, "overduedays_inps : " + overduedays_inps + " guarantorwriteoff : " + guarantorwriteoff);
                        ifr.addItemInCombo("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", overduedays_inps);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", overduedays_inps);
                        ifr.addItemInCombo("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", guarantorwriteoff);
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", guarantorwriteoff);
                    }

                }
                if (PartyType.equalsIgnoreCase("CB")) {
                    Log.consoleLog(ifr, "inside CB Details fKey::");
                    ifr.setStyle("LV_MIS_Data", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_RecoveryMechanism", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_Residence", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_NatureofSecurity", "visible", "false");
                    pcm.controlvisiblity(ifr, VisibleFieldsCB);
                    pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                    pcm.controlEnable(ifr, editable);
                    String dob = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB").toString();
                    Log.consoleLog(ifr, "dob" + dob);
                    SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        // Parse the date string into a Date object
                        Date date = inputAge.parse(dob);
                        // Format the Date object back into a string in "dd/MM/yyyy" format
                        String formattedDate = outputAge.format(date);
                        LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        int age = calculateAge(calAge);
                        Log.consoleLog(ifr, "age" + age);
                        String customerAge = Integer.toString(age);
                        Log.consoleLog(ifr, "customerAge" + age);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_AGE", customerAge);
                        // Output: 19/08/1992
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    String FkeyCB = "";
                    String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1").replaceAll("#PID#", ProcessInstanceId);
                    Log.consoleLog(ifr, "OccuppationInfoDetails1 query CO-BORROWER1::" + IndexQuery1);
                    List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
                    if (!dataResult1.isEmpty()) {
                        FkeyCB = dataResult1.get(0).get(0);
                    }
                    Log.consoleLog(ifr, "dataResult1 fKey CB::" + FkeyCB);
                    String QueryCB = "SELECT o.recovery_mechanism,o.nature_of_security,o.relationshipcanara_inmonths,o.RESIDENCE, o.RELATIONSHIPCANARA,o.QUALIFICATION,b.NRI,b.SETTLEDACCOUNTINCREDITHISTORY,b.OVERDUEINCREDITHISTORY,b.SALARYCREDITEDTHROUGHBANK FROM los_nl_occupation_info o JOIN los_nl_basic_info b ON o.F_KEY = b.F_KEY WHERE o.F_KEY = '" + FkeyCB + "'";
                    Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + QueryCB);
                    List<List<String>> getdatafromoccuCB = cf.mExecuteQuery(ifr, QueryCB, "occupation Fkey query Co-Obligant ");

                    Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + getdatafromoccuCB);
                    if (!getdatafromoccuCB.equals(null)) {

                        recoveryMechanism = getdatafromoccuCB.get(0).get(0);
                        natureofSecurity = getdatafromoccuCB.get(0).get(1);
                        relationMonths = getdatafromoccuCB.get(0).get(2);
                        Residence = getdatafromoccuCB.get(0).get(3);
                        relationyears = getdatafromoccuCB.get(0).get(4);
                        QUALIFICATION = getdatafromoccuCB.get(0).get(5);
                        NRI = getdatafromoccuCB.get(0).get(6);
                        SETTLEDACCOUNTINCREDITHISTORY = getdatafromoccuCB.get(0).get(7);
                        OVERDUEINCREDITHISTORY = getdatafromoccuCB.get(0).get(8);
                        SALARYCREDITEDTHROUGHBANK = getdatafromoccuCB.get(0).get(9);

                        Log.consoleLog(ifr, "OccuppationInfoDetails recoveryMechanism CO-BORROWER::" + recoveryMechanism);
                        Log.consoleLog(ifr, "OccuppationInfoDetails natureofSecurity CO-BORROWER::" + natureofSecurity);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths CO-BORROWER::" + relationMonths);
                        Log.consoleLog(ifr, "OccuppationInfoDetails Residence CO-BORROWER::" + Residence);
                        Log.consoleLog(ifr, "OccuppationInfoDetails relationMonths CO-BORROWER::" + relationyears);
                        Log.consoleLog(ifr, "OccuppationInfoDetails QUALIFICATION CO-BORROWER::" + QUALIFICATION);
                        Log.consoleLog(ifr, "OccuppationInfoDetails NRI CO-BORROWER::" + NRI);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SETTLEDACCOUNTINCREDITHISTORY CO-BORROWER::" + SETTLEDACCOUNTINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails OVERDUEINCREDITHISTORY CO-BORROWER::" + OVERDUEINCREDITHISTORY);
                        Log.consoleLog(ifr, "OccuppationInfoDetails SALARYCREDITEDTHROUGHBANK CO-BORROWER::" + SALARYCREDITEDTHROUGHBANK);

                        ifr.setValue("QNL_BASIC_INFO_RecoveryMechanism", recoveryMechanism);
                        ifr.setValue("QNL_BASIC_INFO_NatureofSecurity", natureofSecurity);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipwithBankMonths", relationMonths);
                        ifr.setValue("QNL_BASIC_INFO_Residence", Residence);
                        ifr.setValue("QNL_BASIC_INFO_RelationshipWithBank", relationyears);
                        Log.consoleLog(ifr, "QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification value CBchk::" + QUALIFICATION);
                        ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", QUALIFICATION);
                        if (QUALIFICATION.equals("")) {
                            ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", "disable", "false");
                            ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification", "mandatory", "true");
                        }
                        if (NRI.equalsIgnoreCase("N")) {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "No");

                        } else {
                            ifr.setValue("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", "Yes");

                        }
                        ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", SETTLEDACCOUNTINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_OVERDUEINCREDITHISTORY", OVERDUEINCREDITHISTORY);
                        ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", SALARYCREDITEDTHROUGHBANK);
                        String deduction = "";
                        String deductionCBQ = "SELECT DEDUCTIONMONTHLY FROM los_nl_occupation_info where F_KEY = '" + FkeyCB + "'";
                        Log.consoleLog(ifr, "OccuppationInfoDetails query BORROWER::" + deductionCBQ);
                        List<List<String>> deductionCB = cf.mExecuteQuery(ifr, deductionCBQ, "occupation Fkey query Co-Obligant ");

                        Log.consoleLog(ifr, "getdatafromoccuCB dataResult fKey::" + deductionCB);
                        if (!deductionCB.isEmpty()) {
                            deduction = deductionCB.get(0).get(0);
                        }
                        Log.consoleLog(ifr, "OccuppationInfoDetails deduction::" + deduction);
                        ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                        int OccupationDetailSize = ifr.getDataFromGrid("LV_OCCUPATION_INFO").size();
                        Log.consoleLog(ifr, "OccupationDetailSize ::" + OccupationDetailSize);
                        if (OccupationDetailSize == 0) {
                            Log.consoleLog(ifr, "inside add data to gridOccupationDetailSize" + OccupationDetailSize);
                            JSONObject obj = new JSONObject();
                            JSONArray jsonarr = new JSONArray();
                            obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionMonthly", deduction);
                            jsonarr.add(obj);
                            Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                            ((IFormAPIHandler) ifr).addDataToGrid("LV_OCCUPATION_INFO", jsonarr, true);
                            Log.consoleLog(ifr, "Occuppation loan row Added==>");
                        } else {
                            Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::" + OccupationDetailSize);
                            ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 61, deduction);
                            Log.consoleLog(ifr, "Occuppation loan data setted to 0th index row==>");
                        }

                    }
                    String ColumValue = ifr.getTableCellValue("LV_OCCUPATION_INFO", 0, 3);
                    if (ColumValue.equalsIgnoreCase("Non Income Earner") || ColumValue.equalsIgnoreCase("NIE")) {
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "4", false);

                    } else if (ColumValue.equalsIgnoreCase("Salaried")) {
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", false);
                    } else if (ColumValue.equalsIgnoreCase("Self-Employed/Professional") || ColumValue.equalsIgnoreCase("Self-Employed/Non-Professional")) {
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "4", false);

                    } else {
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", false);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", true);
                        ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", true);
                    }
                    String relationshipwithapplicant = "";
                    String FkeyB = Bpcm.Fkey(ifr, "CB");
                    Log.consoleLog(ifr, "FkeyCB::" + FkeyB);
                    String RealtionShipWithApplicant = ConfProperty.getQueryScript("RealtionShipWithApplicantVL").replaceAll("#F_KEY#", FkeyB);
                    List<List<String>> RealtionShipWithApplicantQuery = cf.mExecuteQuery(ifr, RealtionShipWithApplicant, "RealtionShipWithApplicant Query");
                    if (!RealtionShipWithApplicantQuery.isEmpty()) {
                        relationshipwithapplicant = RealtionShipWithApplicantQuery.get(0).get(0);
                        ifr.addItemInCombo("QNL_BASIC_INFO_Relationshipwithapplicant", relationshipwithapplicant);
                        ifr.setValue("QNL_BASIC_INFO_Relationshipwithapplicant", relationshipwithapplicant);
                        ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "disable", "true");
                    }

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");//modified by sharon 18/06/2024
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat.parse(CustAccopen);
                    String formattedDate = outputFormat.format(date);
                    ifr.setValue("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);//modified by sharon 18/06/2024

                    String pensionCredits = "Yes";
                    ifr.setValue("QNL_BASIC_INFO_SalaryCreditedthroughBank", pensionCredits);
                    ifr.setStyle("QNL_BASIC_INFO_SalaryCreditedthroughBank", "disable", "true");

                }
                if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                    ifr.setStyle("QNL_BASIC_INFO_RecoveryMechanism", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_Residence", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_NatureofSecurity", "disable", "true");
                }
                if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency", "visible", "false");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }
                if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker") || ifr.getActivityName().equalsIgnoreCase("Disbursement Checker")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_ApplicantType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_EntityType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_ExistingCustomer", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CustomerID", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", "disable", "true");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                    Log.consoleLog(ifr, "inside Disbursement Maker Budget partyDetailsSection controlDisable");
                }
                if (ActivityName.equalsIgnoreCase("Reviewer")) {
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency", "visible", "false");
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget:Reviewer ");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("QNL_BASIC_INFO_ApplicantType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_EntityType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_ExistingCustomer", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CustomerID", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", "disable", "true");
                    ifr.setStyle("P_Industry_Details", "visible", "false");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }

                }
                if (ActivityName.equalsIgnoreCase("Sanction")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection pension Code End:Sanction");
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_ApplicantType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_EntityType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_ExistingCustomer", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CustomerID", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", "disable", "true");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }
                if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                    pcm.controlDisable(ifr, NonEditableFileds);
                    pcm.controlinvisiblity(ifr, InvisibleFields);
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_ApplicantType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_EntityType", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_ExistingCustomer", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CustomerID", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", "disable", "true");
                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                    Log.consoleLog(ifr, "inside PostSanction Budget partyDetailsSection controlDisable");
                }

                //LV_MIS_Data
                ifr.setColumnVisible("LV_MIS_Data", "5", false);
                ifr.setColumnVisible("LV_MIS_Data", "6", false);
                ifr.setColumnVisible("LV_MIS_Data", "7", false);
                ifr.setColumnVisible("LV_MIS_Data", "8", false);
                ifr.setColumnVisible("LV_MIS_Data", "9", false);
                ifr.setColumnVisible("LV_MIS_Data", "10", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "0", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "1", false);
                ifr.setStyle("add_LV_MIS_Data", "disable", "true");
                Log.consoleLog(ifr, " OnLoadPartyDetails Budget Code End");
            }
            if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                //modified by Sharon on 26-07-2024
                Log.consoleLog(ifr, "inside AccActivityManager:: OnLoadPartyDetails::VL Condition " + loan_selected);
                String PartyType1 = ifr.getValue("QNL_BASIC_INFO_ApplicantType").toString();

                ifr.setStyle("LV_OCCUPATION_INFO", "disable", "true");

                String VisibleFieldsB = "QNL_BASIC_INFO_ApplicantType,"
                        + "QNL_BASIC_INFO_EntityType,"
                        + "QNL_BASIC_INFO_ExistingCustomer,"
                        + "QNL_BASIC_INFO_CBSCustomerID,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName";

                String VisibleFieldsCB = "QNL_BASIC_INFO_ApplicantType,"
                        + "QNL_BASIC_INFO_EntityType,"
                        + "QNL_BASIC_INFO_ExistingCustomer,"
                        + "QNL_BASIC_INFO_CBSCustomerID,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,"
                        // + "QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName"
                        // + "QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED,"
                        + "QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,"
                        + "QNL_BASIC_INFO_Relationshipwithapplicant";

                String NonEditableFileds = "QNL_BASIC_INFO_CNL_KYC2_KYC_No,QNL_BASIC_INFO_CNL_KYC2_KYC_ID,QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus,QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID,QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo,QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB,QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender,QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName,QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName,QNL_BASIC_INFO_CL_BASIC_INFO_I_Title,QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName,QNL_BASIC_INFO_StaffMember,QNL_BASIC_INFO_CustomerID,QNL_BASIC_INFO_ExistingCustomer,QNL_BASIC_INFO_EntityType,QNL_BASIC_INFO_ApplicantType,QNL_BASIC_INFO_CBSCustomerID,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_FullName";

                String InvisibleFieldsB = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste,QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency_status,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo,table565_textbox6838,QNL_BASIC_INFO_customerFlag,table565_textbox6908,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_MotherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_KeyPersonName,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonNamez,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ConstitutionType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_IndustryType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifyIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifySubIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_RegistrationNumber,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofYearsinBusiness,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofEmployees,QNL_BASIC_INFO_CL_BASIC_INFO_NI_CountryOfIncorp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_TurnOver,QNL_BASIC_INFO_CL_BASIC_INFO_NI_EmailID,QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BusinessActivity,table565_datepick2,table565_datepick375,table565_datepick97,table565_datepick101,table565_datepick102";
                String InvisibleFieldsCB = "QNL_BASIC_INFO_CL_BASIC_INFO_I_Caste,QNL_BASIC_INFO_CL_BASIC_INFO_I_Residency_status,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_CL_BASIC_INFO_I_NoOfDependents,QNL_BASIC_INFO_CL_BASIC_INFO_I_Qualification_Desc,QNL_BASIC_INFO_CL_BASIC_INFO_I_Education_Qualification,QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality,QNL_BASIC_INFO_CL_BASIC_INFO_I_Age,QNL_BASIC_INFO_CL_BASIC_INFO_I_Alias,QNL_BASIC_INFO_CustomerSinceDate,QNL_BASIC_INFO_RelationshipWithBank,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_NatureofSecurity,QNL_BASIC_INFO_AlternateMobileNumber,QNL_BASIC_INFO_Residence,QNL_BASIC_INFO_RecoveryMechanism,QNL_BASIC_INFO_SalaryTieUp,QNL_BASIC_INFO_SelectSalaryAccount,QNL_BASIC_INFO_OVERDUEINCREDITHISTORY,QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY,QNL_BASIC_INFO_CUSTOMERISNRIORNOT,QNL_BASIC_INFO_SalaryCreditedthroughBank,QNL_BASIC_INFO_RelationshipwithBankMonths,QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo,table565_textbox6838,QNL_BASIC_INFO_customerFlag,table565_textbox6908,QNL_BASIC_INFO_SalaryAccountwithCanara,QNL_BASIC_INFO_NUMBEROFCHILDREN,QNL_BASIC_INFO_CL_BASIC_INFO_I_MotherName,QNL_BASIC_INFO_CL_BASIC_INFO_I_KeyPersonName,QNL_BASIC_INFO_CL_BASIC_INFO_I_ReligionOthers,QNL_BASIC_INFO_CL_BASIC_INFO_I_Religion,QNL_BASIC_INFO_CATEGORY,QNL_BASIC_INFO_IFothersSpecify,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InYears,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_InMonths,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_TWE_NoOfYearsInCurOrg,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_ContactName,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonNamez,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_KeyPersonMobile,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_GrossAnnualIncome,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_DeductionAnnual,QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NetAnnualIncome,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ConstitutionType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_IndustryType,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifyIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_ClassifySubIndustry,QNL_BASIC_INFO_CL_BASIC_INFO_NI_RegistrationNumber,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofYearsinBusiness,QNL_BASIC_INFO_CL_BASIC_INFO_NI_NumberofEmployees,QNL_BASIC_INFO_CL_BASIC_INFO_NI_CountryOfIncorp,QNL_BASIC_INFO_CL_BASIC_INFO_NI_TurnOver,QNL_BASIC_INFO_CL_BASIC_INFO_NI_EmailID,QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo,QNL_BASIC_INFO_CL_BASIC_INFO_NI_BusinessActivity,table565_datepick2,table565_datepick375,table565_datepick97,table565_datepick101,table565_datepick102";

                String occupationType = null;
                String occupationSub = null;
                if (PartyType1.equalsIgnoreCase("B")) {
                    pcm.controlvisiblity(ifr, VisibleFieldsB);
                    pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                    pcm.controlDisable(ifr, NonEditableFileds);
                    String f_key = Bpcm.Fkey(ifr, "B");
                    String occupationquery = "select PROFILE,OCCUPATIONTYPE from LOS_NL_Occupation_INFO where  F_KEY='" + f_key + "'";
                    List<List<String>> Occupationdetails = cf.mExecuteQuery(ifr, occupationquery, "Execute query for fetching Purpose data from portal");
                    if (!Occupationdetails.isEmpty()) {
                        Log.consoleLog(ifr, "inside proposed grid ");
                        occupationType = Occupationdetails.get(0).get(0);
                        occupationSub = Occupationdetails.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "occupation..." + occupationType);
                    Log.consoleLog(ifr, "occupationSub..." + occupationSub);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
                    int OccupationDetailSize = ifr.getDataFromGrid("LV_OCCUPATION_INFO").size();
                    Log.consoleLog(ifr, "OccupationDetailSize ::" + OccupationDetailSize);
                    if (OccupationDetailSize == 0) {
                        Log.consoleLog(ifr, "inside add data to gridOccupationDetailSize" + OccupationDetailSize);
                        JSONObject obj = new JSONObject();
                        JSONArray jsonarr = new JSONArray();
                        obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
                        obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                        jsonarr.add(obj);
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                        ((IFormAPIHandler) ifr).addDataToGrid("LV_OCCUPATION_INFO", jsonarr, true);
                        Log.consoleLog(ifr, "Proposed loan row Added==>");
                    } else {
                        Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::" + OccupationDetailSize);
                        ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 3, occupationType);
                        ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 4, occupationSub);
                        Log.consoleLog(ifr, "Proposed loan data setted to 0th index row==>");
                    }

                    ifr.setStyle("LV_GUARANTOR", "visible", "false");
                    ifr.setStyle("LV_KYC", "visible", "true");
                    ifr.setStyle("LV_KYC", "disable", "true");
                    ifr.setStyle("F_P_ExistingRelationship", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "visible", "false");
                    ifr.setStyle("duplicateAdvancedListviewchanges_ALV_BASIC_INFO", "disable", "true");
                    ifr.setStyle("QNL_BASIC_INFO_StaffMember", "disable", "true");

                    String firstName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
                    String middleName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
                    String LastName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
                    String Fullname = concatFullname(firstName, middleName, LastName);
                    Log.consoleLog(ifr, " partyDetailsSection Fullname;:" + Fullname);
                    ifr.setValue("QNL_BASIC_INFO_FullName", Fullname);
                    String FkeyB = Bpcm.Fkey(ifr, PartyType);

                    String MartialStatus = ifr.getValue("QNL_BASIC_INFO_CL_BASIC _INFO_I_MaritalStatus").toString();
                    Log.consoleLog(ifr, "OccuppationInfoDetails MartialStatus BORROWER::" + MartialStatus);

                    if (MartialStatus.equalsIgnoreCase("Married")) {
                        Log.consoleLog(ifr, "into marital status condition");
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "true");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "true");
                    } else {
                        ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_SpouseName", "visible", "false");
                        ifr.setStyle("QNL_BASIC_INFO_WHETHERTHESPOUSEISEMPLOYED", "visible", "false");
                    }
                    String lastName = null;
                    String mailID = null;
                    String lastNameQ = "select lastname,emailid from LOS_L_BASIC_INFO_I where F_KEY='" + FkeyB + "'";
                    List<List<String>> lastname = cf.mExecuteQuery(ifr, lastNameQ, "Execute query for fetching Purpose data from portal");
                    if (!lastname.isEmpty()) {
                        Log.consoleLog(ifr, "inside proposed grid ");
                        lastName = lastname.get(0).get(0);
                        mailID = lastname.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "lastName::" + lastName);
                    Log.consoleLog(ifr, "mailID::" + mailID);

                } else if (PartyType1.equalsIgnoreCase("CB")) {
                    pcm.controlvisiblity(ifr, VisibleFieldsCB);
                    pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                    pcm.controlDisable(ifr, NonEditableFileds);
                    String f_key = Bpcm.Fkey(ifr, "CB");
                    String occupationquery = "select PROFILE,OCCUPATIONTYPE from LOS_NL_Occupation_INFO where  F_KEY='" + f_key + "'";
                    List<List<String>> Occupationdetails = cf.mExecuteQuery(ifr, occupationquery, "Execute query for fetching Purpose data from portal");
                    if (!Occupationdetails.isEmpty()) {
                        Log.consoleLog(ifr, "inside proposed grid ");
                        occupationType = Occupationdetails.get(0).get(0);
                        occupationSub = Occupationdetails.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "occupation CB..." + occupationType);
                    Log.consoleLog(ifr, "occupationSub CB..." + occupationSub);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                    ifr.setValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
                    int OccupationDetailSize = ifr.getDataFromGrid("LV_OCCUPATION_INFO").size();
                    Log.consoleLog(ifr, "OccupationDetailSize ::" + OccupationDetailSize);
                    if (OccupationDetailSize == 0) {
                        Log.consoleLog(ifr, "inside add data to gridOccupationDetailSize" + OccupationDetailSize);
                        JSONObject obj = new JSONObject();
                        JSONArray jsonarr = new JSONArray();
                        obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType", occupationType);
                        obj.put("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationSubType", occupationSub);
                        jsonarr.add(obj);
                        Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                        ((IFormAPIHandler) ifr).addDataToGrid("LV_OCCUPATION_INFO", jsonarr, true);
                        Log.consoleLog(ifr, "Proposed loan row Added==>");
                    } else {
                        Log.consoleLog(ifr, "inside settablecell.. ProposedLoanSize ::" + OccupationDetailSize);
                        ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 3, occupationType);
                        ifr.setTableCellValue("LV_OCCUPATION_INFO", 0, 4, occupationSub);
                        Log.consoleLog(ifr, "Proposed loan data setted to 0th index row==>");
                    }

                    ifr.setStyle("LV_GUARANTOR", "visible", "false");
                    ifr.setStyle("LV_KYC", "visible", "true");
                    ifr.setStyle("LV_KYC", "disable", "true");
                    ifr.setStyle("F_P_ExistingRelationship", "visible", "false");
                    ifr.setStyle("duplicateAdvancedListviewchanges_ALV_BASIC_INFO", "disable", "true");
                    ifr.setStyle("LV_MIS_Data", "visible", "false");
                    ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "visible", "true");
                    ifr.setStyle("QNL_BASIC_INFO_StaffMember", "disable", "true");

                    String firstName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName").toString();
                    String middleName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
                    String LastName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName").toString();
                    String Fullname = concatFullname(firstName, middleName, LastName);
                    Log.consoleLog(ifr, " partyDetailsSection Fullname;:" + Fullname);
                    ifr.setValue("QNL_BASIC_INFO_FullName", Fullname);
                    String relationshipwithapplicant = "";
                    String FkeyB = Bpcm.Fkey(ifr, "B");
                    Log.consoleLog(ifr, "FkeyB::" + FkeyB);
                    String RealtionShipWithApplicant = ConfProperty.getQueryScript("RealtionShipWithApplicantVL").replaceAll("#F_KEY#", FkeyB);
                    List<List<String>> RealtionShipWithApplicantQuery = cf.mExecuteQuery(ifr, RealtionShipWithApplicant, "RealtionShipWithApplicant Query");
                    if (!RealtionShipWithApplicantQuery.isEmpty()) {
                        relationshipwithapplicant = RealtionShipWithApplicantQuery.get(0).get(0);
                        ifr.setValue("QNL_BASIC_INFO_Relationshipwithapplicant", relationshipwithapplicant);
                        ifr.setStyle("QNL_BASIC_INFO_Relationshipwithapplicant", "disable", "true");
                    }

                    String lastName = null;
                    String mailID = null;
                    String FkeyCB = Bpcm.Fkey(ifr, PartyType);
                    String lastNameQ = "select lastname,emailid from LOS_L_BASIC_INFO_I where F_KEY='" + FkeyCB + "'";
                    List<List<String>> lastname = cf.mExecuteQuery(ifr, lastNameQ, "Execute query for fetching Purpose data from portal");
                    if (!lastname.isEmpty()) {
                        Log.consoleLog(ifr, "inside proposed grid ");
                        lastName = lastname.get(0).get(0);
                        mailID = lastname.get(0).get(1);
                    }
                    Log.consoleLog(ifr, "lastName::" + lastName);
                    Log.consoleLog(ifr, "mailID::" + mailID);

                }
                String ActivityName = ifr.getActivityName();

                //Branch Checker
                if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {

                    if (PartyType.equalsIgnoreCase("B")) {

                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");
                    }
                }
                //Disbursement Maker
                if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {

                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");
                    }
                    Log.consoleLog(ifr, "inside Disbursement Maker Budget partyDetailsSection controlDisable");
                }

                //Sanction
                if (ActivityName.equalsIgnoreCase("Sanction")) {

                    Log.consoleLog(ifr, "inside partyDetailsSection Budget Code End:Sanction");

                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");
                    }
                }
                //Convenor
                if (ActivityName.equalsIgnoreCase("Convenor")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget: Convenor");

                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");

                    }
                }

                //PostSanction
                if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {

                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        pcm.controlDisable(ifr, NonEditableFileds);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");
                    }
                    Log.consoleLog(ifr, "inside PostSanction Budget partyDetailsSection controlDisable");
                }
                //Post Disbursement Doc Upload
                if (ActivityName.equalsIgnoreCase("Post Disbursement Doc Upload")) {
                    Log.consoleLog(ifr, "inside partyDetailsSection Budget Code End:Post Disbursement Doc Upload");
                    pcm.controlDisable(ifr, NonEditableFileds);

                    if (PartyType.equalsIgnoreCase("B")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsB);
                        pcm.controlDisable(ifr, VisibleFieldsB);
                        ifr.setStyle("LV_MIS_Data", "visible", "true");
                    }
                    if (PartyType.equalsIgnoreCase("CB")) {
                        pcm.controlvisiblity(ifr, VisibleFieldsCB);
                        pcm.controlinvisiblity(ifr, InvisibleFieldsCB);
                        pcm.controlDisable(ifr, VisibleFieldsCB);
                        ifr.setStyle("LV_MIS_Data", "visible", "false");
                    }
                }
                //LV_MIS_Data
                ifr.setColumnVisible("LV_MIS_Data", "5", false);
                ifr.setColumnVisible("LV_MIS_Data", "6", false);
                ifr.setColumnVisible("LV_MIS_Data", "7", false);
                ifr.setColumnVisible("LV_MIS_Data", "8", false);
                ifr.setColumnVisible("LV_MIS_Data", "9", false);
                ifr.setColumnVisible("LV_MIS_Data", "10", false);

                //LV_OCCUPATION_INFO
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "0", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "1", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "60", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "61", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "62", true);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "64", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "66", false);
                ifr.setColumnVisible("LV_OCCUPATION_INFO", "65", false);

                Log.consoleLog(ifr, " OnLoadPartyDetails VL Code End");
            }

            String midName = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName").toString();
            Log.consoleLog(ifr, "After getting MidName" + midName);
            if (midName.equalsIgnoreCase("null") || midName.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "check MidName");
                midName = "";
                ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", "mandatory", "false");
            }
            ifr.setValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", midName);
            String email = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID").toString();
            Log.consoleLog(ifr, "AcceleratorActivityManagerCode-OnLoadPartyDetails-After getting email" + email);
            if (email.equalsIgnoreCase("null") || email.equalsIgnoreCase("")) {
                Log.consoleLog(ifr, "AcceleratorActivityManagerCode-OnLoadPartyDetails-check email");
                ifr.setStyle("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", "disable", "false");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onloadparty details" + e);
        }

        return "";
    }

    public void mImplChangeKYCIDType(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside mformOnLoadLeadCapture AcceleratorActivityManagerCode ");
        bbcc.OnLoadKYC(ifr);
    }

    public String OnLoadProposedLoanGrid(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadProposedLoanGrid AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String ActivityName = ifr.getActivityName();
        Log.consoleLog(ifr, "ActivityName" + ActivityName);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (ActivityName.equalsIgnoreCase("Lead Capture")) {
            Log.consoleLog(ifr, "into leadCapture ");
            String Invsible = "QNL_LOS_PROPOSED_FACILITY_ConsessionRoi,BTN_RepaymentEngine,QNL_LOS_PROPOSED_FACILITY_Type_of_Advance,QNL_LOS_PROPOSED_FACILITY_ProposalType,QNL_LOS_PROPOSED_FACILITY_ReqAmtforPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtforConstruction,QNL_LOS_PROPOSED_FACILITY_Loan_for_plot_already_availed_from_BOM,QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Premium,QNL_LOS_PROPOSED_FACILITY_Campaign,QNL_LOS_PROPOSED_FACILITY_ConcessionInProcessingFees,QNL_LOS_PROPOSED_FACILITY_ConcessionROI,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_InstallmentAmt,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId";
            pcm.controlinvisiblity(ifr, Invsible);
            String disable = "QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_RepayFreq,QNL_LOS_PROPOSED_FACILITY_RateChartCode";
            pcm.controlDisable(ifr, disable);
            String enable = "QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_EffectiveROI";
            pcm.controlEnable(ifr, enable);

        }

        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            String visiblefield = "QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_Variant, QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_TypeofIntrestRegime,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq, QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_Tenure,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_RepayType";
            String Invsible = "QNL_LOS_PROPOSED_FACILITY_RATECHARTCODE_DESC,QNL_LOS_PROPOSED_FACILITY_SCHEDULECODE_DESC,BTN_RepaymentEngine,QNL_LOS_PROPOSED_FACILITY_Type_of_Advance,QNL_LOS_PROPOSED_FACILITY_ProposalType,QNL_LOS_PROPOSED_FACILITY_ReqAmtforPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtforConstruction,QNL_LOS_PROPOSED_FACILITY_Loan_for_plot_already_availed_from_BOM,QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Premium,QNL_LOS_PROPOSED_FACILITY_Campaign,QNL_LOS_PROPOSED_FACILITY_ConcessionInProcessingFees,QNL_LOS_PROPOSED_FACILITY_ConsessionRoi,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_InstallmentAmt,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId";
            String nonEditableFileds = "QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_EMI_DATE,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq,QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_Tenure,QNL_LOS_PROPOSED_FACILITY_RepayType";

            String Loanpurpose = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose").toString();
            Log.consoleLog(ifr, "Loanpurpose ==> " + Loanpurpose);
            if (Loanpurpose.equalsIgnoreCase("OTH")) {
                Log.consoleLog(ifr, "into OTH field ");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "disable", "false");
            } else if (Loanpurpose.equalsIgnoreCase("PERS") || Loanpurpose.equalsIgnoreCase("DOM")) {
                Log.consoleLog(ifr, "into else field " + Loanpurpose);
                //ifr.setValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose", "Others");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "false");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "disable", "true");
            }

            if (ActivityName.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "inside mImplClickAssetChecker Budget ");
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlEnable(ifr, visiblefield);
                pcm.controlDisable(ifr, nonEditableFileds);
                String productCode = pcm.mGetProductCode(ifr);
                Log.consoleLog(ifr, "ProductCode:" + productCode);
                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                ifr.setStyle("LV_OTHERCHKLIST", "visible", "false");
                String requestedLoanAmountWord = "";
                String loanAmountInWord = "SELECT convert_to_indian_words(REQLOANAMT) FROM los_nl_proposed_facility WHERE PID='" + ProcessInstanceId + "'";
                List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
                if (loanAmountInWords.size() > 0) {
                    requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                    Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
                }
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);

                String purpose = "";
                String Subproduct = "";
                String roitype = "";//modified by Vandana 17/06/2024
                String loanDetailQuery = "select LOANPURPOSE,ROITYPE,Subproduct from los_nl_proposed_facility where  PID='" + ProcessInstanceId + "'";
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, loanDetailQuery, "Execute query for fetching Purpose data from portal");
                if (!PurposePortal.isEmpty()) {
                    Log.consoleLog(ifr, "inside proposed grid ");
                    purpose = PurposePortal.get(0).get(0);
                    roitype = PurposePortal.get(0).get(1);//modified by Vandana 17/06/2024
                    Subproduct = PurposePortal.get(0).get(2);
                }

                Log.consoleLog(ifr, "purpose :" + purpose);

                String RepaymentType = "";
                ////modified by Vandana 17/06/2024 starts
                String productvariance = "";
                String variance = "";
                String Repaymenttype = ConfProperty.getQueryScript("REPAYMENTTYPEQUERY");
                List<List<String>> RepaymenttypeQuery = cf.mExecuteQuery(ifr, Repaymenttype, "Repaymenttype Query");
                if (!RepaymenttypeQuery.isEmpty()) {
                    RepaymentType = RepaymenttypeQuery.get(0).get(0);
                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RepayType", RepaymentType);
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RepayType", "disable", "true");
                }
                //  String CRG = pcm.mGetCRG(ifr);
                //String schemeId = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
//                String varianceQuery = ConfProperty.getQueryScript("varianceQuery").replaceAll("#schemeid#", schemeId).replaceAll("#ROITYPE#", roitype);
//                List<List<String>> varianceListQuery = cf.mExecuteQuery(ifr, varianceQuery, "Execute query for fetching varianceQuery");
//                if (varianceListQuery.size() > 0) {
//                    productvariance = varianceListQuery.get(0).get(0);
//                    variance = varianceListQuery.get(0).get(1);
//                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance", productvariance);
//                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Variance", variance);
//
//                }//modified by Vandana 17/06/2024 ends
                String queryV = "Select Variantname,Variantcode From Los_M_Variant  Where Isactive='Y' And "
                        + "Variantcode In (Select Variantcode From Los_M_Product_Rlos  Where Productcode='" + productCode + "' "
                        + "and subproductcode='STP-CB' and PURPOSECODE='" + purpose + "' "
                        + "and IsActive='Y') ORDER BY VARIANTNAME";
                Log.consoleLog(ifr, "queryV:" + queryV);
                List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load Varainat");
                for (int i = 0; i < list.size(); i++) {
                    String label = list.get(i).get(0);
                    String value1 = list.get(i).get(1);
                    ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Variant", label, value1);
                }
                Log.consoleLog(ifr, " OnLoadProposedLoanGrid Budget Branch Maker Code End");
            } else {
                //All other than Branch Maker
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlDisable(ifr, nonEditableFileds);
                //Modified by monesh to handle  disable the values on 19/08/2024
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
                ifr.setStyle("LV_OTHERCHKLIST", "visible", "false");

            }

            //roi type validation
            String roiType = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString();
            Log.consoleLog(ifr, " onLoadRoittype Budget Branch Maker Code End" + roiType);
            if (roiType.equalsIgnoreCase("Floating")) {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "false");
            } else {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "true");

            }
        }
        //added by keerthana for pension journey on 04/07/2024
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            String visiblefield = "QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_Variant, QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_TypeofIntrestRegime,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq, QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_Tenure,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_ROIType";
            String Invsible = "QNL_LOS_PROPOSED_FACILITY_RATECHARTCODE_DESC,QNL_LOS_PROPOSED_FACILITY_SCHEDULECODE_DESC,BTN_RepaymentEngine,QNL_LOS_PROPOSED_FACILITY_Type_of_Advance,QNL_LOS_PROPOSED_FACILITY_ProposalType,QNL_LOS_PROPOSED_FACILITY_ReqAmtforPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtforConstruction,QNL_LOS_PROPOSED_FACILITY_Loan_for_plot_already_availed_from_BOM,QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Premium,QNL_LOS_PROPOSED_FACILITY_Campaign,QNL_LOS_PROPOSED_FACILITY_ConcessionInProcessingFees,QNL_LOS_PROPOSED_FACILITY_ConsessionRoi,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_InstallmentAmt,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId,QNL_LOS_PROPOSED_FACILITY_EMI_DATE";
            String nonEditableFileds = "QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq,QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_Variant,QNL_LOS_PROPOSED_FACILITY_Tenure";
            if (ActivityName.equalsIgnoreCase("Branch Maker")) {
                Log.consoleLog(ifr, "inside mImplClickAssetChecker Budget ");
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlEnable(ifr, visiblefield);
                pcm.controlDisable(ifr, nonEditableFileds);
                String productCode = pcm.getProductCode(ifr);
                Log.consoleLog(ifr, "ProductCode:" + productCode);
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
                ifr.setStyle("LV_OTHERCHKLIST", "visible", "false");
                String requestedLoanAmountWord = "";
                String loanAmountInWord = "SELECT convert_to_indian_words(REQLOANAMT) FROM los_nl_proposed_facility WHERE PID='" + ProcessInstanceId + "'";
                List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
                if (loanAmountInWords.size() > 0) {
                    requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                    Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
                }
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);

                String purpose = "";
                String Subproduct = "";
                String roitype = "";//modified by Vandana 17/06/2024
                String loanDetailQuery = "select LOANPURPOSE,ROITYPE,Subproduct from los_nl_proposed_facility where  PID='" + ProcessInstanceId + "'";
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, loanDetailQuery, "Execute query for fetching Purpose data from portal");
                if (!PurposePortal.isEmpty()) {
                    Log.consoleLog(ifr, "inside proposed grid ");
                    purpose = PurposePortal.get(0).get(0);
                    roitype = PurposePortal.get(0).get(1);
                    Subproduct = PurposePortal.get(0).get(2);
                }
                if (purpose.equalsIgnoreCase("OTHS")) {
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "true");

                } else {
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "false");

                }
                Log.consoleLog(ifr, "purpose :" + purpose);

                String RepaymentType = "";

                String Repaymenttype = ConfProperty.getQueryScript("REPAYMENTTYPEQUERY");
                List<List<String>> RepaymenttypeQuery = cf.mExecuteQuery(ifr, Repaymenttype, "Repaymenttype Query");
                if (!RepaymenttypeQuery.isEmpty()) {
                    RepaymentType = RepaymenttypeQuery.get(0).get(0);
                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RepayType", RepaymentType);
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RepayType", "disable", "true");
                }

                Log.consoleLog(ifr, " OnLoadProposedLoanGrid Budget Branch Maker Code End");
            } else {
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlDisable(ifr, visiblefield);
                ifr.setStyle("LV_OTHERCHKLIST", "visible", "false");
                //Modified by monesh to handle  disable the values on 19/08/2024
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");

            }
            Log.consoleLog(ifr, " OnLoadProposedLoanGrid Pension  Branch Maker Code for Pension ");

            if (ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RateChartCode").toString().isEmpty()) {
                cm.mLoadSchemeId(ifr);
                mChangeROIType(ifr, Control, Event, value);
                OnChangeSchedulecode(ifr, Control, Event, value);
                mChangeRate(ifr, Control, Event, value);
            }
            //roi type validation
            String roiType = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString();
            Log.consoleLog(ifr, " onLoadRoittype Budget Branch Maker Code End" + roiType);
            if (roiType.equalsIgnoreCase("Floating")) {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "false");
            } else {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "true");

            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlDisable(ifr, visiblefield);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_EMI_DATE", "disable", "true");
                Log.consoleLog(ifr, " onLoadProposedLoan:: Pension Code End Sanction");
            }
            if (ActivityName.equalsIgnoreCase("Lead Capture")) {
                String loanPurpose = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose").toString();
                if (loanPurpose.equalsIgnoreCase("OTH")) {
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "true");
                } else {
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify", "visible", "false");
                }
            }
        }

        //modified by ishwarya on 29-06-2024
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {//added by Janani
            Log.consoleLog(ifr, " VL On load proposed loan");
            String visiblefield = "QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_Variant,QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq,QNL_LOS_PROPOSED_FACILITY_Tenure,QNL_LOS_PROPOSED_FACILITY_Concessionrate";
            String Invsible = "QNL_LOS_PROPOSED_FACILITY_EMI_DATE,QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm,QNL_LOS_PROPOSED_FACILITY_TypeofIntrestRegime,BTN_RepaymentEngine,QNL_LOS_PROPOSED_FACILITY_If_Others_Please_Specify,QNL_LOS_PROPOSED_FACILITY_Type_of_Advance,QNL_LOS_PROPOSED_FACILITY_FRP,QNL_LOS_PROPOSED_FACILITY_RLLR,QNL_LOS_PROPOSED_FACILITY_ProposalType,QNL_LOS_PROPOSED_FACILITY_ReqAmtforPlot,QNL_LOS_PROPOSED_FACILITY_ReqAmtforConstruction,QNL_LOS_PROPOSED_FACILITY_Loan_for_plot_already_availed_from_BOM,QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Premium,QNL_LOS_PROPOSED_FACILITY_Campaign,QNL_LOS_PROPOSED_FACILITY_ConcessionInProcessingFees,QNL_LOS_PROPOSED_FACILITY_ConsessionRoi,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_InstallmentAmt,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId";
            String nonEditableFileds = "QNL_LOS_PROPOSED_FACILITY_RateChartCode,QNL_LOS_PROPOSED_FACILITY_Variant,QNL_LOS_PROPOSED_FACILITY_ScheduleCode,QNL_LOS_PROPOSED_FACILITY_TypeofIntrestRegime,QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_EMI_DATE,QNL_LOS_PROPOSED_FACILITY_Tenure,QNL_LOS_PROPOSED_FACILITY_Concessionrate,QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt,QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_RepayFreq,QNL_LOS_PROPOSED_FACILITY_MaxTerm,QNL_LOS_PROPOSED_FACILITY_MinTerm";
            Log.consoleLog(ifr, "inside branch maker VL:::");
            pcm.controlinvisiblity(ifr, Invsible);
            pcm.controlvisiblity(ifr, visiblefield);
            pcm.controlEnable(ifr, visiblefield);
            pcm.controlDisable(ifr, nonEditableFileds);
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
            Log.consoleLog(ifr, "ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubproductVL(ifr, Purpose);
            Log.consoleLog(ifr, "subProductCode:" + subProductCode);
            ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Product", productCode);
            ifr.setStyle("LV_OTHERCHKLIST", "visible", "false");
            String RepaymentType = "";

            String Repaymenttype = ConfProperty.getQueryScript("REPAYMENTTYPEQUERY");
            List<List<String>> RepaymenttypeQuery = cf.mExecuteQuery(ifr, Repaymenttype, "Repaymenttype Query");
            if (!RepaymenttypeQuery.isEmpty()) {
                RepaymentType = RepaymenttypeQuery.get(0).get(0);
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RepayType", RepaymentType);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RepayType", "disable", "true");
            }

            String requestedLoanAmountWord = "";
            String loanAmountInWord = "SELECT convert_to_indian_words(\"Loan_Amount\") FROM LOS_PORTAL_SLIDERVALUE WHERE PID='" + ProcessInstanceId + "'";
            List<List<String>> loanAmountInWords = cf.mExecuteQuery(ifr, loanAmountInWord, "Execute query for fetching Loan amount data from portal in principal");
            if (loanAmountInWords.size() > 0) {
                requestedLoanAmountWord = loanAmountInWords.get(0).get(0);
                Log.consoleLog(ifr, "requestedLoanAmountWord : " + requestedLoanAmountWord);
            }
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", requestedLoanAmountWord);
            // QNL_LOS_PROPOSED_FACILITY_RateChartCode
            ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Variant", "NA", "NA");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Variant", "NA");
            if (ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RateChartCode").toString().isEmpty()) {
                cm.mLoadSchemeId(ifr);
                mChangeROIType(ifr, Control, Event, value);
                OnChangeSchedulecode(ifr, Control, Event, value);
                mChangeRate(ifr, Control, Event, value);
            }
            String purpose1 = "";
            String Subproduct = "";
            String roitype = "";
            String tenure = "";
            String loanDetailQuery = "select LOANPURPOSE,ROITYPE,Subproduct,tenure from los_nl_proposed_facility where  PID='" + ProcessInstanceId + "'";
            List<List<String>> PurposePortal1 = cf.mExecuteQuery(ifr, loanDetailQuery, "Execute query for fetching Purpose data from portal");
            if (!PurposePortal1.isEmpty()) {
                Log.consoleLog(ifr, "inside proposed grid ");
                purpose1 = PurposePortal1.get(0).get(0);
                roitype = PurposePortal1.get(0).get(1);
                Subproduct = PurposePortal1.get(0).get(2);
                tenure = PurposePortal1.get(0).get(3);
            }
            Log.consoleLog(ifr, "purpose :" + purpose1);
            Log.consoleLog(ifr, "tenure :" + tenure);

            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_TENUREMONTHS", tenure);
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_TENUREMONTHS", "disable", "true");
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "mandatory", "false");
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", "mandatory", "false");
            String Query = "UPDATE los_nl_proposed_facility set SCHEMEID='" + schemeID + "' where PID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query :" + Query);
            Log.consoleLog(ifr, " query for inserting Scheme id");
            cf.mExecuteQuery(ifr, Query, "Update los_nl_proposed_facility scheme id ");

            if (ifr.getActivityName().equalsIgnoreCase("Branch Checker")) {
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlDisable(ifr, nonEditableFileds);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
                Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End Branch Checker");
            }
            if (ifr.getActivityName().equalsIgnoreCase("PostSanction")) {
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlDisable(ifr, nonEditableFileds);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
                Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End PostSanction");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Sanction")) {
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlDisable(ifr, nonEditableFileds);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
                Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End Sanction");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlDisable(ifr, nonEditableFileds);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
                ifr.setStyle("saveAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "visible", "false");
                Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End Disbursement Maker");
            }
            if (ifr.getActivityName().equalsIgnoreCase("Post Disbursement Doc Upload")) {
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, Invsible);
                pcm.controlDisable(ifr, nonEditableFileds);
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ROIType", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variance", "disable", "true");
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
                Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End Post Disbursement Doc Upload");
            }
            String roiType = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString();
            Log.consoleLog(ifr, " onLoadRoittype VL Branch Maker Code End" + roiType);
            if (roiType.equalsIgnoreCase("Floating")) {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "false");
            } else {
                ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "true");
            }
            Log.consoleLog(ifr, " onLoadProposedLoan:: VL Code End");

        }
        //QNL_LOS_PROPOSED_FACILITY_RISK_PREMIUM
        //QNL_LOS_PROPOSED_FACILITY_LIQUIDITY_PREMIUM//QNL_LOS_PROPOSED_FACILITY_GRD_CONCESSION//QNL_LOS_PROPOSED_FACILITY_ECAIUNRATED_PREMIUM

        ifr.setStyle("duplicateAdvancedListviewchanges_ALV_PROPOSED_FACILITY", "disable", "true");
        Log.consoleLog(ifr, "inside OnLoadProposedLoanGrid field visiblity end ");
        changeROISpread(ifr);
        return "";
    }

    public String changeROISpread(IFormReference ifr) {
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            //  String ProcessInstanceId = Data.getM_strProcessInstanceId();
            String queryL = ConfProperty.getQueryScript("ROISPREAD").replaceAll("#PRODUCTCODE#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString())
                    .replaceAll("#SUBPRODUCTCODE#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString());
            List<List<String>> roiSpreadresult = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching Seeting ROI ");
            String visiblefield = "QNL_LOS_PROPOSED_FACILITY_RISK_PREMIUM,QNL_LOS_PROPOSED_FACILITY_LIQUIDITY_PREMIUM,QNL_LOS_PROPOSED_FACILITY_GRD_CONCESSION,QNL_LOS_PROPOSED_FACILITY_ECAIUNRATED_PREMIUM";
            String enable = "QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_Variance";
            if (roiSpreadresult.isEmpty()) {

                pcm.controlinvisiblity(ifr, visiblefield);
                pcm.controlDisable(ifr, visiblefield);
                pcm.controlEnablenumber(ifr, enable);
                pcm.controlvisiblity(ifr, enable);

            } else {
                // QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance,QNL_LOS_PROPOSED_FACILITY_Variance,QNL_LOS_PROPOSED_FACILITY_EffectiveROI

                pcm.controlEnablenumber(ifr, visiblefield);
                pcm.controlvisiblity(ifr, visiblefield);
                pcm.controlinvisiblity(ifr, enable);

                pcm.controlDisable(ifr, enable);
            }
        } catch (Exception e) {
            JSONObject message = new JSONObject();
            message.put("saveWorkitem", "true");
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Error in Saving data!"));

            Log.consoleLog(ifr, "Exception in Change spread " + e);
            Log.errorLog(ifr, "Exception in Change" + e);

            return message.toString();
        }

        return "";
    }

    public String onchangeAccVariance(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onchangeAccVariance AcceleratorActivityManagerCode ");

        String prdVariance = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance").toString();
        if (pcm.numberformatvalidation(ifr, prdVariance)) {

            String accVariance = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Variance").toString();

            if (pcm.numberformatvalidation(ifr, prdVariance)) {
                String rllr = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RLLR").toString();
                String sumalue = sumstring(ifr, accVariance + "," + rllr + "," + prdVariance);
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", sumalue);

            } else {
                return pcm.returnErrorcustmessage(ifr, "kindly enter the number  ");

            }
            return "";
        } else {
            return pcm.returnErrorcustmessage(ifr, "kindly enter the number format ");

        }

    }

    public String onchangeSpreadVariance(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onchangeAccVariance AcceleratorActivityManagerCode ");
        //   String visiblefield="QNL_LOS_PROPOSED_FACILITY_RISK_PREMIUM,QNL_LOS_PROPOSED_FACILITY_LIQUIDITY_PREMIUM,QNL_LOS_PROPOSED_FACILITY_GRD_CONCESSION,QNL_LOS_PROPOSED_FACILITY_ECAIUNRATED_PREMIUM";

        String RISK_PREMIUM = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RISK_PREMIUM").toString();
        String LIQUIDITY_PREMIUM = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LIQUIDITY_PREMIUM").toString();

        String GRD_CONCESSION = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_GRD_CONCESSION").toString();

        String ECAIUNRATED_PREMIUM = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ECAIUNRATED_PREMIUM").toString();
        String queryL = ConfProperty.getQueryScript("ROISPREAD").replaceAll("#PRODUCTCODE#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString())
                .replaceAll("#SUBPRODUCTCODE#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString());
        List<List<String>> roiSpreadresult = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching Seeting ROI ");
        if (pcm.numberformatvalidation(ifr, RISK_PREMIUM) && pcm.numberformatvalidation(ifr, LIQUIDITY_PREMIUM) && pcm.numberformatvalidation(ifr, GRD_CONCESSION) && pcm.numberformatvalidation(ifr, ECAIUNRATED_PREMIUM)) {
            if (pcm.checkROISpread(ifr, RISK_PREMIUM, roiSpreadresult.get(0).get(0), roiSpreadresult.get(0).get(1))) {
                if (pcm.checkROISpread(ifr, LIQUIDITY_PREMIUM, roiSpreadresult.get(0).get(2), roiSpreadresult.get(0).get(3))) {
                    if (pcm.checkROISpread(ifr, GRD_CONCESSION, roiSpreadresult.get(0).get(4), roiSpreadresult.get(0).get(5))) {
                        if (pcm.checkROISpread(ifr, ECAIUNRATED_PREMIUM, roiSpreadresult.get(0).get(6), roiSpreadresult.get(0).get(7))) {

                            String rllr = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RLLR").toString();
                            String sumalue = sumstring(ifr, RISK_PREMIUM + "," + rllr + "," + LIQUIDITY_PREMIUM + "," + GRD_CONCESSION + "," + ECAIUNRATED_PREMIUM);
                            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", sumalue);
                        } else {
                            return pcm.returnErrorcustmessage(ifr, "kindly enter the ECAIUNRATED_PREMIUM   with the range of  " + roiSpreadresult.get(0).get(6) + "to " + roiSpreadresult.get(0).get(7));

                        }

                    } else {
                        return pcm.returnErrorcustmessage(ifr, "kindly enter the GRD_CONCESSION   with the range of  " + roiSpreadresult.get(0).get(4) + "to " + roiSpreadresult.get(0).get(5));

                    }

                } else {
                    return pcm.returnErrorcustmessage(ifr, "kindly enter the LIQUIDITY_PREMIUM   with the range of  " + roiSpreadresult.get(0).get(2) + "to " + roiSpreadresult.get(0).get(3));

                }

            } else {
                return pcm.returnErrorcustmessage(ifr, "kindly enter the RISK_PREMIUM   with the range of  " + roiSpreadresult.get(0).get(0) + "to " + roiSpreadresult.get(0).get(1));

            }
        } else {
            return pcm.returnErrorcustmessage(ifr, "kindly enter the number format ");

        }
        return "";
    }

    public String sumstring(IFormReference ifr, String values) {

        Log.consoleLog(ifr, "inside sumstring AcceleratorActivityManagerCode " + values);
        String[] numberStrings = values.split(",");
        DecimalFormat df = new DecimalFormat("#.00");
        double totalSum = 0;
        for (String numStr : numberStrings) {
            totalSum += Double.parseDouble(numStr.trim());
        }
        String totalSumString = df.format(totalSum);
        Log.consoleLog(ifr, "inside onchangeAccVariance sumstring " + totalSumString);
        return df.format(totalSum);
    }

    public String autopopulateFinalEligibility(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadRiskScoreRatigMaker AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            String finalEligReturn = bbcc.autopopulate_FinalEligibility(ifr);
            if (finalEligReturn.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            String finalElig = vlbcc.autopopulateFinalEligibilityVL(ifr);
            Log.consoleLog(ifr, "finalelig===>" + finalElig);
            if (finalElig.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            } else if (finalElig.contains("showMessage")) {
                return finalElig;
            }
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            String finalElig = pbcc.FinalEligibilityPensionBO(ifr);
            Log.consoleLog(ifr, "finalelig===>" + finalElig);
            if (finalElig.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            } else if (finalElig.contains("showMessage")) {
                return finalElig;
            }
        }
        JSONObject message = new JSONObject();
        message.put("saveWorkitem", "true");
        return message.toString();
    }
    //Changes added by Logaraj for introduction of new field Applicant full name

    public String concatFullname(String firstName, String middlename, String Lastname) {
        if (!middlename.isEmpty() || middlename != null) {
            return firstName + " " + middlename + " " + Lastname;
        } else {
            return firstName + " " + Lastname;
        }
    }

//OnchangeDisbursement
    public void OnchangeDisbursement(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside on change OnchangeDisbursement AcceleratorActivityManagerCode");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.OnchangeDisbursement(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.OnchangeDisbursement(ifr);
        }
    }

    public void onLoadBeneficiaryDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnLoadRiskScoreRatigMaker AcceleratorActivityManagerCode ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.beneficiaryDetails(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.beneficiaryDetails(ifr);
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            bbcc.beneficiaryDetails(ifr);
        }
    }

    public void OnChangeBeneficiaryFrame(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onChangeBeneficiaryFrame AcceleratorActivityManagerCode");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.beneficiaryFrameChange(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.beneficiaryFrameChange(ifr);
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            bbcc.beneficiaryFrameChange(ifr);
        }
    }

    public String mImplOnChangeSectionStateOutwardDocument(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        JSONObject message = new JSONObject();
        try {

            Log.consoleLog(ifr, "inside beneficiaryDetails AcceleratorActivityManagerCode: ");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
            String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            String schemeID = mGetSchemeIDForInQuery(ifr, ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                ifr.setStyle("P_OutwardDocument_Button", "disable", "false");
                // ifr.setColumnDisable("ALV_GENERATE_DOCUMENT", "View", false);
                //added by keerthana to enable outward button on 19/07/2024
                ifr.setColumnDisable("ALV_GENERATE_DOCUMENT", "8", false);
                ifr.setColumnDisable("ALV_GENERATE_DOCUMENT", "11", false);
                //added by keerthana to enable outward button on 19/07/2024
            }
            Log.consoleLog(ifr, "Inside mImplOnChangeSectionStateOutwardDocument");
            // int count = ifr.getDataFromGrid("ALV_GENERATE_DOCUMENT").size();
            // Log.consoleLog(ifr, "count = " + count);
//       

            String DocumentList = "";
            JSONArray jsonarr = new JSONArray();

            // if (count == 0) {
            String Qyery = "SELECT distinct (a.documentname) FROM los_m_document a INNER JOIN los_m_document_scheme  b ON a.documentid = b.documentid inner join  los_m_activityname c on b.ACTIVITYCODE=c.activitycode WHERE b.schemeid in(" + schemeID + ") AND UPPER(a.doctype) = 'OUTWARD' AND a.generate = 'Y' AND A.ISACTIVE = 'Y' and c.ACTIVITYNAME='" + ifr.getActivityName() + "'";
            List<List<String>> documentResults = cf.mExecuteQuery(ifr, Qyery, "Generate Document List");
            JSONArray jsonArr = new JSONArray();

            if (!documentResults.isEmpty()) {
                int count = ifr.getDataFromGrid("ALV_GENERATE_DOCUMENT").size();
                /*  String Gridcount = "";
                String gridCountQuery = "select count(*) from Los_nl_generate_document where PID = '" + ProcessInstanceId + "'";
                List<List<String>> gridCount = cf.mExecuteQuery(ifr, gridCountQuery, "Execute query for getting grid count");
                if (!gridCount.isEmpty()) {
                    Log.consoleLog(ifr, "inside grid count:: ");
                    Gridcount = gridCount.get(0).get(0);
                }
                Log.consoleLog(ifr, "Gridcount ==>" + Gridcount);
                int count = Integer.parseInt(Gridcount);*/
                Set<String> existingDocuments = new HashSet<>();
                Log.consoleLog(ifr, "count ==>" + count);
                if (count > 0) {
                    for (int j = 0; j < count; j++) {
                        existingDocuments.add(ifr.getTableCellValue("ALV_GENERATE_DOCUMENT", j, 3));
                        Log.consoleLog(ifr, "existingDocuments ==>" + existingDocuments);
                    }
                }

                for (List<String> result : documentResults) {
                    String documentName = result.get(0);
                    Log.consoleLog(ifr, "documentName ==>" + documentName);
                    if (!existingDocuments.contains(documentName)) {
                        Log.consoleLog(ifr, documentName + " ===> Document Added Successfully");
                        JSONObject obj = new JSONObject();
                        obj.put("QNL_GENERATE_DOCUMENT_DocumentName", documentName);
                        jsonArr.add(obj);
                    }
                }

                Log.consoleLog(ifr, "GENERATE_DOCUMENT jsonArr: " + jsonArr);
                ifr.addDataToGrid("ALV_GENERATE_DOCUMENT", jsonArr);
            }
            message.put("saveWorkitem", "true");
            message.put("showMessage", cf.showMessage(ifr, "", "", "Document Added  successfully!"));

            return message.toString();

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplOnChangeSectionStateOutwardDocument" + e);
            Log.errorLog(ifr, "Exception in mImplOnChangeSectionStateOutwardDocument" + e);

            message.put("saveWorkitem", "true");
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Document not Added  successfully!"));
            return message.toString();
        }

    }

    public String mImplClickGenerate(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside document generation click ");
        PortalCommonMethods pcm = new PortalCommonMethods();
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "inside mGenerateDoc::");
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + processInstanceId);
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", processInstanceId);
            String loan_selected = "";
            String loanType = "";
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            if (!loanSelected.isEmpty()) {
                loan_selected = loanSelected.get(0).get(0);
            }
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                loanType = "BUDGET";
            } else if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                loanType = "VL";
            } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                loanType = "PENSION";
            }

            //String TOTLIABQuery = select TOTLIAB from los_nl_al_networth where PID ='#PID#'
            String TOTLIABQuery = ConfProperty.getQueryScript("TOTLIABNetWorth").replaceAll("#PID#", processInstanceId);
            List<List<String>> TOTLIAB = cf.mExecuteQuery(ifr, TOTLIABQuery, "TOTAL LIAB Query");
            if (TOTLIAB.isEmpty()) {
                Log.consoleLog(ifr, "No data available for Doc generation==>");
                JSONObject respJSON = new JSONObject();
                respJSON.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Kindly fill asset details."));
                return respJSON.toString();

            }
            int count = ifr.getDataFromGrid("ALV_GENERATE_DOCUMENT").size();
            String docName = "";
            String returnMessageFromDocGen = "";
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    docName = ifr.getTableCellValue("ALV_GENERATE_DOCUMENT", i, 3);
                    Log.consoleLog(ifr, "docName ==>" + docName.toUpperCase());
                    Log.consoleLog(ifr, "docName ==>" + "Experian_Report".toUpperCase());

                    if (docName.contains("KFS")) {
                        String loanamount = "";
                        String tenure = "";
                        String Finalquery = ConfProperty.getQueryScript("KFSDETAILS").replaceAll("#PID#", processInstanceId);
                        List<List<String>> generateQuery = cf.mExecuteQuery(ifr, Finalquery, "Finalquery Query");
                        if (!generateQuery.isEmpty()) {
                            loanamount = generateQuery.get(0).get(0);
                            tenure = generateQuery.get(0).get(1);

                        }
                        Log.consoleLog(ifr, "loanamount==>" + loanamount);
                        Log.consoleLog(ifr, "tenure==>" + tenure);

                        Ammortization AMR = new Ammortization();
                        Log.consoleLog(ifr, "loanamount ==>" + loanamount);
                        Log.consoleLog(ifr, "tenure ==>" + tenure);
                        String returnMessage = AMR.ExecuteCBS_Ammortization(ifr, processInstanceId, loanamount, tenure, loanType);
                        Log.consoleLog(ifr, "returnMessage from ExecuteCBS_Ammortization ==>" + returnMessage);
                    }
                    String returnValue = "";
                    if ((docName).toUpperCase().contains("Experian_Report".toUpperCase()) || (docName.toUpperCase()).contains("_CIBIL_Report".toUpperCase())) {
                        returnValue = "success," + ifr.getTableCellValue("ALV_GENERATE_DOCUMENT", i, 0);
                        Log.consoleLog(ifr, "returnMessage from CB Execution ==>" + returnValue);
                    } else {

                        returnValue = cm.generatedocCB(ifr, docName, loanType);
                        Log.consoleLog(ifr, "returnMessage from other API ==>" + returnValue);

                    }
                    Log.consoleLog(ifr, "returnValue for document generation::" + returnValue);
                    if (!returnValue.equalsIgnoreCase("")) {
                        String returnMessageFromDocGenArray[] = returnValue.split(",");
                        if (returnMessageFromDocGenArray.length > 0) {
                            returnMessageFromDocGen = returnMessageFromDocGenArray[0];
                            Log.consoleLog(ifr, "returnMessageFromDocGen ==>" + returnMessageFromDocGen);
                            String docindex = returnMessageFromDocGenArray[1].replaceAll("\"", "");
                            Log.consoleLog(ifr, "docindexdocindex ==>" + docindex);
                            if (returnMessageFromDocGen.equalsIgnoreCase("success")) {
                                ifr.setTableCellValue("ALV_GENERATE_DOCUMENT", i, 0, docindex);
                                ifr.setTableCellValue("ALV_GENERATE_DOCUMENT", i, 5, cf.getCurrentDate(ifr));
                                ifr.setTableCellValue("ALV_GENERATE_DOCUMENT", i, 6, ifr.getUserName());
                                ifr.setTableCellValue("ALV_GENERATE_DOCUMENT", i, 4, "Generated");

                            } else if (returnMessageFromDocGen.equalsIgnoreCase("Error")) {
                                Log.consoleLog(ifr, "Enterend Error Condition  ==>");
                                message.put("saveWorkitem", "true");
                                message.put("showMessage", cf.showMessage(ifr, "", "error", "Document Generation Failed!"));
                                Log.consoleLog(ifr, "Enterend Error Condition  ==>" + message.toString());
                                Log.errorLog(ifr, "Document is not added!");
                                return message.toString();
                            }
                        }
                    }
                }
                message.put("saveWorkitem", "true");
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Document generated successfully!"));

                return message.toString();
            } else {
                Log.errorLog(ifr, "Document is not added!");
                message.put("saveWorkitem", "true");
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Document is not added!"));
                Log.consoleLog(ifr, "Enterend Document is not added Condition  ==>" + message.toString());
                return message.toString();

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception" + e.getMessage());
            //Log.consoleLog(ifr, "Enterend Exception Condition  ==>"+e.getMessage());
            Log.errorLog(ifr, "Excetion" + e);
            message.put("saveWorkitem", "true");
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Some error occured!")).toString();
            Log.consoleLog(ifr, "Exception" + message);
            return message.toString();
        }
    }

    public String closeWI(IFormReference ifr) {
        Log.consoleLog(ifr, "inside closeWI : ");
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String closeWIQuery = "UPDATE LOS_WIREFERENCE_TABLE SET "
                    + "APPLICATION_STATUS='CANCELLED' WHERE WINAME='" + ProcessInsanceId + "'";
            Log.consoleLog(ifr, "closeWIQuery:" + closeWIQuery);
            ifr.saveDataInDB(closeWIQuery);
        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception in closeWI : " + e);
        }
        return "";
    }

    public void onLoadCifCreartion(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onLoadCifCreartion AcceleratorActivityManagerCode: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.cifListViewLoad(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.cifListViewLoad(ifr);
        }
    }

    public void OnChangeCIFframe(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onLoadCifCreartion AcceleratorActivityManagerCode: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            bbcc.cifFrameChange(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.cifFrameChange(ifr);
        }

    }

    //disabling button 
//Added by monesh for nesl trigger 
    public String OnChangeLoanAccCreation(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeLoanAccCreation AcceleratorActivityManagerCode: ");
        JSONObject message = new JSONObject();
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);

        String EsignStatusQuery = ConfProperty.getQueryScript("getESignStatusQuery").replaceAll("#PID#", ProcessInstanceId);
        Log.consoleLog(ifr, "EsignStatusQuery ::" + EsignStatusQuery);
        List<List<String>> EsignStatusResult = ifr.getDataFromDB(EsignStatusQuery);
        String EsignStatus = EsignStatusResult.get(0).get(0);
        Log.consoleLog(ifr, "EsignStatus --> " + EsignStatus);
        if (EsignStatus.equalsIgnoreCase("Initiated")) {
            Log.consoleLog(ifr, "inside NESL initiated ::");
            EsignCommonMethods em = new EsignCommonMethods();
            String neslstatus = em.checkNESLWorkflowStatus(ifr);
            if (neslstatus.equalsIgnoreCase("")) {
                if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                    bbcc.loanAccChange(ifr);
                } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                    vlbcc.loanAccChange(ifr);
                } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                    plpc.loanAccChangeCP(ifr);
                }
                return "";
            } else {

                ifr.setStyle("BTN_LoanCreation", "disable", "true");
                message.put("showMessage", cf.showMessage(ifr, "", "error", "NESL is not complete. Request Customer to complete the same"));
                Log.consoleLog(ifr, "Error in nesl " + message);
                return message.toString();
            }
        } else {
            Log.consoleLog(ifr, "inside NESL Manual ::");
        }
        return "";
    }

    public void OnChangeLoanDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeLoanDetails AcceleratorActivityManagerCode: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        ifr.setStyle("CBSPRODUCTCODE", "disable", "true");
        ifr.setStyle("L_Product_code", "visible", "false");
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
            bbcc.loanDetailsChange(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.loanDetailsChange(ifr);
        }
    }

    public String onchageDownpayment(IFormReference ifr, String Control, String Event, String value) {//added by Sharon for Down payment validation on 17/06/2024
        Log.consoleLog(ifr, "inside onchageDownpayment : ");
        String downPaymentValidation = Vlpc.onchageDownpayment(ifr);
        return downPaymentValidation;

    }
//    public String onchageRequiredLoanAmt(IFormReference ifr, String Control, String Event, String value)
//    {
//    	HRMSPortalCustomCode hrmpcc=new HRMSPortalCustomCode();
//    	 Log.consoleLog(ifr, "inside onchageRequiredLoanAmt : ");
//         String LoanAmt = hrmpcc.getLoanAmtOnPageLoad(ifr);
//         return LoanAmt;
//    }

    /*Added By: Veena
     Date: 19-06-2024
     Description: TO raise auto deviation.*/
    public String mImplOnChangeSectionStateDeviationBRMS_CB(IFormReference ifr, String Control, String Event, String value) {
        try {
            BRMSRules brmsCall = new BRMSRules();
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + PID);
            String messageValue = "";
            Log.consoleLog(ifr, "inside mImplOnChangeSectionStateDeviationBRMS_CB:PID: " + PID);
            String NTHRule_IPStr = FetchNTHRuleInput(ifr, PID);
            Log.consoleLog(ifr, "NTHRule_IPStr: " + NTHRule_IPStr);
            int NTHRule_IP = Integer.parseInt(NTHRule_IPStr == "" ? "0" : NTHRule_IPStr);
            Log.consoleLog(ifr, "NTHRule_IP: " + NTHRule_IP);
            //HashMap<String, Object> objm = new HashMap();
            //objm = brmsCall.getExecuteBRMSRule(ifr, "Deviation_CB", String.valueOf(NTHRule_IP));
            //JSONObject result = cf.executeBRMSRule(ifr, "Deviation_CB", String.valueOf(NTHRule_IP));           
            JSONObject result = brmsCall.executeLOSBRMSRule(ifr, "Deviation_CB", String.valueOf(NTHRule_IP), "deviation_cb_op");
            Log.consoleLog(ifr, "BRMS Result of  DeviationRules : " + result);
            String[] deviationOP = new String[]{"deviation_op"};
            String[] deviationID = new String[]{"deviationid_op"};

            String RaisedDev = ConfProperty.getQueryScript("RaisedDev").replaceAll("#PID#", PID);
            List<List<String>> RaisedDevData = cf.mExecuteQuery(ifr, RaisedDev, "RaisedDev:");

            JSONArray jsonarray = new JSONArray();
            for (int i = 0; i < deviationOP.length; i++) {
                Log.consoleLog(ifr, "Inside for loop: result.get(deviationOP[i]).toString():: "
                        + result.get(deviationOP[i]).toString() + " result.get(deviationID[i]).toString():: "
                        + result.get(deviationID[i]).toString());
                if (result.get(deviationOP[i]).toString().equalsIgnoreCase("Yes")) {
                    Log.consoleLog(ifr, "Inside Deviationresult Yes");
                    String devid = result.get(deviationID[i]).toString();
                    Log.consoleLog(ifr, "Inside Deviationresult Yes::devid:: " + devid);
                    int flag = 0;
                    for (int y = 0; y < RaisedDevData.size(); y++) {
                        if (devid.equalsIgnoreCase(RaisedDevData.get(y).get(0))) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        String deviation = ConfProperty.getQueryScript("AutoDeviationDetailsFetch").replace("##devid##", devid);
                        List<List<String>> deviationdata = cf.mExecuteQuery(ifr, deviation, "");
                        if (deviationdata.size() > 0) {
                            JSONObject jsonobj = new JSONObject();
                            jsonobj.put("QNL_DeviationBRMS_Description", deviationdata.get(0).get(0));
                            jsonobj.put("QNL_DeviationBRMS_RaisedType", deviationdata.get(0).get(2));
                            jsonobj.put("QNL_DeviationBRMS_DeviationLevel", deviationdata.get(0).get(1));
                            jsonobj.put("QNL_DeviationBRMS_DeviationID", deviationdata.get(0).get(3));
                            jsonobj.put("QNL_DeviationBRMS_DeviationLevelCode", deviationdata.get(0).get(4));
                            jsonobj.put("QNL_DeviationBRMS_ApprovingAuthority", deviationdata.get(0).get(5));
                            Log.consoleLog(ifr, "$$$$$$$$$$$$$$$$$$$$$$$$$");
                            jsonarray.add(jsonobj);
                        } else {
                            JSONObject message = new JSONObject();
                            message.put("showMessage", cf.showMessage(ifr, "ALV_Deviations", "error", "Deviation Details not present!"));
                            return message.toString();
                        }
                    }
                }
            }
            Log.consoleLog(ifr, "json array is :" + jsonarray);
            ifr.addDataToGrid("ALV_Deviations", jsonarray);
            Log.consoleLog(ifr, "added successfully!!!!!!!!!!!");

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in mImplOnChangeSectionStateDeviationBRMS_CB:: " + e);
            Log.errorLog(ifr, "Exception in mImplOnChangeSectionStateDeviationBRMS_CB:: " + ExceptionUtils.getStackTrace(e));

        }
        return null;
    }

    public String FetchNTHRuleInput(IFormReference ifr, String PID) {
        try {
            Log.consoleLog(ifr, "inside FetchNTHRuleInput:PID:: " + PID);
            String LoanSanctionDateStr = "";
            String[] tags = new String[]{"##PID##"};
            String[] data = new String[]{PID};
            Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
            String CBSProdCode = bbcc.GetDataFromDbCommon(ifr, "CBSProductCodeFetch", tags, data);
            Log.consoleLog(ifr, "CBSProdCode:: " + CBSProdCode);

            tags = new String[]{"##PID##", "##ApplicantType##"};
            data = new String[]{PID, "B"};
            //Changed from Borrower to B.
            Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
            String CustID = bbcc.GetDataFromDbCommon(ifr, "CustomerIDFetch", tags, data);

            tags = new String[]{"##CBSProdCode##"};
            data = new String[]{CBSProdCode};
            Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
            String NTHRelations = bbcc.GetDataFromDbCommon(ifr, "NTHDevParamsFetch", tags, data);
            Log.consoleLog(ifr, "NTHRelations:: " + NTHRelations);

            String AccountDetails = Adv360V2.executeCBSAdvanced360Inquiryv2(ifr, PID,
                    CustID, "", "", "FetchNTHRuleInput");

            Log.consoleLog(ifr, "AccountDetails from Advance360V2:: " + AccountDetails);

            org.json.JSONArray AccountDetailsArr = new org.json.JSONArray(AccountDetails);
            Log.consoleLog(ifr, "AccountDetailsArr:: " + AccountDetailsArr);

            if (!AccountDetailsArr.isEmpty()) {
                for (int i = 0; i < AccountDetailsArr.length(); i++) {
                    Log.consoleLog(ifr, "AccountDetailsArr==>" + AccountDetailsArr.get(i));
                    String inputJSON = AccountDetailsArr.get(i).toString();
                    JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
                    String ProductCode = inputJSONObj.get("ProductCode").toString();
                    Log.consoleLog(ifr, "ProductCode=============>" + ProductCode);
                    String CustomerRelationship = inputJSONObj.get("CustomerRelationship").toString();
                    Log.consoleLog(ifr, "CustomerRelationship=============>" + CustomerRelationship);
                    boolean NTHRelMatch = Arrays.stream(NTHRelations.split(","))
                            .anyMatch(element -> element.equals(CustomerRelationship));
                    Log.consoleLog(ifr, "NTHRelMatch=============>" + NTHRelMatch);
                    if (ProductCode.equalsIgnoreCase(CBSProdCode) && NTHRelMatch) {
                        LoanSanctionDateStr = inputJSONObj.get("DatAcctOpen").toString();
                        Log.consoleLog(ifr, "LoanSanctionDateStr=============>" + LoanSanctionDateStr);
                    }
                }
            }
            if (!LoanSanctionDateStr.equalsIgnoreCase("")) {
                tags = new String[]{"##PID##"};
                data = new String[]{PID};
                Log.consoleLog(ifr, "tags:: " + Arrays.toString(tags) + " data:: " + Arrays.toString(data));
                String AppInitiationDateStr = bbcc.GetDataFromDbCommon(ifr, "AppInitiationDateFetch", tags, data);
                Log.consoleLog(ifr, "AppInitiationDateStr:: " + AppInitiationDateStr);
                if (!AppInitiationDateStr.equalsIgnoreCase("")) {
                    DateTimeFormatter fA = DateTimeFormatter.ofPattern("yyyyMMdd");
                    DateTimeFormatter fB = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    // Parse the strings into LocalDate objects
                    LocalDate LoanSanctionDate = LocalDate.parse(LoanSanctionDateStr, fA);
                    LocalDate AppInitiationDate = LocalDate.parse(AppInitiationDateStr, fB);

                    // Calculate the difference in months
                    long monthsDifference = ChronoUnit.MONTHS.between(LoanSanctionDate, AppInitiationDate);
                    Log.consoleLog(ifr, "monthsDifference:: " + monthsDifference);
                    return Long.toString(monthsDifference);
                } else {
                    return "error";
                }
            } else {
                return LoanSanctionDateStr;
            }

        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in FetchNTHRuleInput:: " + e);
            Log.errorLog(ifr, "Exception in FetchNTHRuleInput:: " + ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    ////Common for All Journey's. Check with Leads before Modifying.
    public void OnChangeSchedulecode(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside OnChangeSchedulecode::: ");

        ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_RateChartCode");
        ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "false");
        // ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_RateChartCode");

        String RoiType = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString().toUpperCase();
        String schedule = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ScheduleCode").toString();
        //  schedule.split("-");
        String subStr = schedule.split("-")[0];
//schedule.substring(0, 4);
        Log.consoleLog(ifr, "subStr ::" + subStr);
        String RateChart = ConfProperty.getQueryScript("RATECHART").replaceAll("#SCHEDULECODE#", subStr).replaceAll("#ROITYPE#", RoiType).replaceAll("#SCHEMEID#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());

        List<List<String>> rccode = cf.mExecuteQuery(ifr, RateChart, "Execute query for rate code ");
        String Rccode = "";
        String Rccvalue = "";

        if (!rccode.isEmpty()) {

            for (int i = 0; i < rccode.size(); i++) {
                Rccode = rccode.get(i).get(0);
                Rccvalue = rccode.get(i).get(1);
                Log.consoleLog(ifr, "Rccode ::" + Rccode);

                Log.consoleLog(ifr, "Rccvalue ::" + Rccvalue);

                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_RateChartCode", Rccvalue, Rccvalue);
                Log.consoleLog(ifr, "rccode.size() ::" + rccode.size());
                if (rccode.size() < 2) {

                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RateChartCode", Rccvalue);
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
                    mChangeRate(ifr, "", "", "");
                }

            }
        }

    }

    public void onLoadBamList(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside onLoadBamList::AcceleratorActivityManagerCode: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Staff Vehicle")) {
        	staffVLPortalCustomCode.bamListViewLoad(ifr);
        }

    }

    public String mGetSchemeIDForInQuery(IFormReference ifr, String processInstanceId) {
        String query = "select schemeid from LOS_NL_PROPOSED_FACILITY  Where Pid='" + processInstanceId + "'";
        Log.consoleLog(ifr, "query:" + query);
        List<List<String>> result = ifr.getDataFromDB(query);
        String schemeIDs = "";
        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                String schemeID = result.get(i).get(0);
                if (!schemeID.equalsIgnoreCase("")) {
                    schemeIDs = schemeIDs + "'" + schemeID + "',";
                }
            }
            if (schemeIDs.endsWith(",")) {
                schemeIDs = schemeIDs.substring(0, schemeIDs.length() - 1);
            }
            Log.consoleLog(ifr, "mGetSchemeIDForInQuery::schemeIDs::" + schemeIDs);
            return schemeIDs;
        }
        return "";
    }

//Common for All Journey's. Check with Leads before Modifying.
    public String mChangeROIType(IFormReference ifr, String Control, String Event, String JSdata) {//Checked
        ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_RateChartCode");
        ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_ScheduleCode");
        ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode", "disable", "true");
        ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "false");
        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Spread", "visible", "false");
        }

        //ifr.setstyle("QNL_LOS_PROPOSED_FACILITY_RateChartCode","disable","false");
        ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_ScheduleCode");
        String subproductcode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString();
        Log.consoleLog(ifr, "Inside To fetch subproductcode:" + subproductcode);
        String productcode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString();
        Log.consoleLog(ifr, "Inside To fetch productcode:" + productcode);
        String validate = ConfProperty.getCommonPropertyValue(productcode + "_" + subproductcode + "-CRG");

        if (validate.equalsIgnoreCase("Y")) {

            String query = ConfProperty.getQueryScript("BGENDERQUERY").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "Inside To fetch FullName:" + query);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "BGENDERQUERY For ROI Change ");

            String GENDER = "";
            String SalaryTieUp = "";
            String CANARARETAILGRADE = "";
            if (result.size() > 0) {

                GENDER = result.get(0).get(0);
                SalaryTieUp = result.get(0).get(1);
            }
            if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
                Log.consoleLog(ifr, "Inside Lead Capture:");
                CANARARETAILGRADE = "CRG-3";
                SalaryTieUp = "Yes";
                GENDER = "Others";
                cm.mLoadSchedulecode(ifr, GENDER, SalaryTieUp, CANARARETAILGRADE);

                return "";
            }
            if (SalaryTieUp.equalsIgnoreCase("") && !productcode.equalsIgnoreCase("VL")) {
                Log.consoleLog(ifr, "Inside --> productcode " + productcode);
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select Salary Tie Up inside Party Details"));
                return message.toString();
            }
            if (!(SalaryTieUp.equalsIgnoreCase("Yes"))) {
                SalaryTieUp = "No";
            }
            if (GENDER.toLowerCase().equalsIgnoreCase("female")) {
                GENDER = "Women";
            } else {
                GENDER = "Others";
            }
            query = ConfProperty.getQueryScript("GETCRGVALUE").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "Inside To fetch grade:" + query);
            result = cf.mExecuteQuery(ifr, query, "CRG  For ROI Change ");

            if (productcode.equalsIgnoreCase("VL")) {
                SalaryTieUp = "NA";

            }
            if (result.size() > 0) {
                CANARARETAILGRADE = result.get(0).get(0);

                cm.mLoadSchedulecode(ifr, GENDER, SalaryTieUp, CANARARETAILGRADE);

            }
        } else if (validate.equalsIgnoreCase("N")) {
            cm.mLoadSchedulecode(ifr, "NA", "NA", "NA");

        }
        Log.consoleLog(ifr, "Inside To fetch OnChangeSchedulecode:");
        OnChangeSchedulecode(ifr, Control, Event, JSdata);
        return "";
    }

    public void onSectionChangeLoanDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside onSectionChangeLoanDetails");
        bbcc.onSectionChangeLoanDetails(ifr);
        int BAMCount = ifr.getDataFromGrid("ALV_BAM83").size();
        Log.consoleLog(ifr, "BAMCount ::" + BAMCount);
        if (BAMCount == 0) {
            if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
                autoPopulateBAMvalues(ifr);
            }
        }

    }

    //Added by monesh on 05/07/2024 for ROI selection
    //Common for All Journey's. Check with Leads before Modifying
    public String mChangeRate(IFormReference ifr, String Control, String Event, String JSdata) {
        //Checked

        String subproductcode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString();
        Log.consoleLog(ifr, "Inside To fetch subproductcode:" + subproductcode);
        String productcode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString();
        Log.consoleLog(ifr, "Inside To fetch productcode:" + productcode);
        String validate = ConfProperty.getCommonPropertyValue(productcode + "_" + subproductcode + "-CRG");
        Log.consoleLog(ifr, "validate: " + validate);
        String query = ConfProperty.getQueryScript("BGENDERQUERY").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());

        Log.consoleLog(ifr, "Inside To fetch FullName:" + query);
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "Fetching Geneder and tie up ");

        String GENDER = "";
        String SalaryTieUp = "";
        String CANARARETAILGRADE = "";
        if (!result.isEmpty()) {

            GENDER = result.get(0).get(0);
            SalaryTieUp = result.get(0).get(1);
            Log.consoleLog(ifr, "Inside To fetch grade:" + SalaryTieUp);
        }
        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
            Log.consoleLog(ifr, "Inside Lead Capture ROI:");
            SalaryTieUp = "Yes";
            GENDER = "Others";
        }
        if (SalaryTieUp.equalsIgnoreCase("") && validate.equalsIgnoreCase("Y")) {
            Log.consoleLog(ifr, "Inside  SalaryTieUp: " + SalaryTieUp + ",validate : " + validate);
            if (productcode.equalsIgnoreCase("VL")) {
                SalaryTieUp = "";
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select Salary Tie Up inside Party Details"));
                return message.toString();
            }

        }
        if (!(SalaryTieUp.equalsIgnoreCase("Yes"))) {
            SalaryTieUp = "No";
        }
        if (GENDER.toLowerCase().equalsIgnoreCase("female")) {
            GENDER = "Women";
        } else {
            GENDER = "Others";
        }
        query = ConfProperty.getQueryScript("GETCRGVALUE").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());

        Log.consoleLog(ifr, "Inside To fetch grade:" + query);
        result = cf.mExecuteQuery(ifr, query, "Fetching Roi CANARARETAILGRADE  ");

        if (result.size() > 0) {
            CANARARETAILGRADE = result.get(0).get(0);
        }
        String schedulecode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ScheduleCode").toString().split("-")[0];
        String ratechartcode = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_RateChartCode").toString().split("-")[0];

        Log.consoleLog(ifr, "Inside To fetch productcode:" + productcode);

        if (!validate.equalsIgnoreCase("Y")) {
            SalaryTieUp = "NA";
            GENDER = "NA";
            CANARARETAILGRADE = "NA";
            Log.consoleLog(ifr, "No data Avaialble for pension ");
        }
        if (productcode.equalsIgnoreCase("VL")) {
            SalaryTieUp = "NA";

        }
        if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
            Log.consoleLog(ifr, "Inside Lead Capture ROI:");
            CANARARETAILGRADE = (subproductcode.equalsIgnoreCase("STP-CP") ? "NA" : "CRG-3");

        }
        query = ConfProperty.getQueryScript("BKGETTOTALROI").replaceAll("#SCHEMEID#",
                ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString()).replaceAll("#CRG#", CANARARETAILGRADE).replaceAll("#ROITYPE#",
                ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString()).replaceAll("#SALARY_TIEUP#",
                SalaryTieUp).replaceAll("#GENDER#", GENDER).replaceAll("#SCHEDULECODE#", schedulecode).replaceAll("#COD_RATE_CHART#", ratechartcode);
        Log.consoleLog(ifr, "Inside mChangeROI data :" + query);

        result = cf.mExecuteQuery(ifr, query, "Fetching Roi table  ");

        DecimalFormat df = new DecimalFormat("0.##");
        double interestVariance = Double.parseDouble(result.get(0).get(1));
        double frp = Double.parseDouble(result.get(0).get(3));
        String formattedInterestVariance = df.format(interestVariance);
        String formattedFrp = df.format(frp);
        if (result.size() > 0) {
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", result.get(0).get(0));
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Variance", formattedInterestVariance);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance", result.get(0).get(2));
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_FRP", formattedFrp);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RLLR", result.get(0).get(4));

        } else {
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Variance", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ProductLevelVariance", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_FRP", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_RLLR", "");
            JSONObject message = new JSONObject();
            Log.consoleLog(ifr, "Inside Rate chart code ");
            message.put("showMessage", cf.showMessage(ifr, "", "error", "Thank you for Choosing Canara Bank, You are not Eligible for the Selected Digital Loan Journey as per the Scheme Guideline of the bank."));
            return message.toString();
        }
        // }
        return "";
    }

    public String onClickNetValue(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();
        String messageResult = pcm.mAccCalculateNetIncomeCommonMethod(ifr, "P_CP_GROSS_PENSION", "P_CP_OD_DeductionFromPension", "P_CP_NET_PENSION");
        String returnMessage = "";
        if (!messageResult.equalsIgnoreCase("")) {
            message.put("showMessage", cf.showMessage(ifr, Control, "error", messageResult));
            return message.toString();
        }
        return "";
    }

    public String onClickNetValueForCoborrower(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();
        String messageResult = pcm.mAccCalculateNetIncomeCommonMethod(ifr, "P_CP_OD_GrossPen_COB", "P_CP_OD_DeductionFromPen_COB", "P_CP_OD_NetPension_COB");
        if (!messageResult.equalsIgnoreCase("")) {
            message.put("showMessage", cf.showMessage(ifr, Control, "error", messageResult));
            return message.toString();
        }
        return "";
    }

    public String onClickNetValueForCoborrowerSalried(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();
        String messageResult = pcm.mAccCalculateNetIncomeCommonMethod(ifr, "P_CP_OD_GrossSalary_Mon", "P_CP_OD_Deduction_Mon", "P_CP_OD_NetIncome_Mon");
        if (!messageResult.equalsIgnoreCase("")) {
            message.put("showMessage", cf.showMessage(ifr, Control, "error", messageResult));
            return message.toString();
        }
        return "";
    }

    public String onClickNetValueForCoborrowerNonPro(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();
        String messageResult = pcm.mAccCalculateNetIncomeCommonMethod(ifr, "P_CP_OD_GrossSalary_Year", "P_CP_OD_Deduction_Year", "P_CP_OD_NetIncome_Year");
        if (!messageResult.equalsIgnoreCase("")) {
            message.put("showMessage", cf.showMessage(ifr, Control, "error", messageResult));
            return message.toString();
        }
        return "";
    }

    //Added by Aravindh For handling Disbursement button disabling if FundTransfer already done
    public String OnClickCheckerDisbursementTab(IFormReference ifr, String Control, String Event, String value) {
        JSONObject message = new JSONObject();

        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String query = "SELECT * FROM LOS_INTEGRATION_CBS_STATUS WHERE TRANSACTION_ID = '" + PID + "' AND API_NAME ='CBS_FundTransfer'  AND API_STATUS ='SUCCESS'";
        Log.consoleLog(ifr, " CBS_FundTransfer Status query: " + query);
        List<List<String>> FundTransferResult = ifr.getDataFromDB(query);
        if (FundTransferResult.size() > 0) {
            Log.consoleLog(ifr, " CBS_FundTransfer is Done already " + FundTransferResult);
            ifr.setColumnDisable("ALV_BENEFICIARY_DETAILS", "7", true);
            ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", 0, 7, "Disbursed");

            int BeneficiaryGridCount = ifr.getDataFromGrid("ALV_BENEFICIARY_DETAILS").size();
            Log.consoleLog(ifr, "  BeneficiaryGridCount :: " + BeneficiaryGridCount);
            if (BeneficiaryGridCount > 0) {
                ifr.setStyle("add_ALV_BENEFICIARY_DETAILS", "disable", "true");
                ifr.setStyle("select_ALV_BENEFICIARY_DETAILS", "disable", "true");
                ifr.setStyle("ALV_BENEFICIARY_DETAILS_0", "disable", "true");
                Log.consoleLog(ifr, "  Beneficiary Grid plus button disabled :: ");

            }
        }

        return "";
    }
//Added by logaraj on 26/06/2024 for finaleligibility onchange

    public String mCalculateUserIncomePolicy(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mCalculateUserIncomePolicy");
        String res = plpc.mCalculateUserIncomePolicy(ifr);
        return res;
    }

    public String calcLoanAmount(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside calcLoanAmount");
        plpc.calcLoanAmount(ifr);
        return "";

    }
//Added by logaraj on 26/06/2024 for finaleligibility onchange

    public String caluclateEMIFinalEligibility(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside caluclateEMIFinalEligibility");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            return bbcc.CalcEmiOnChangeRecommendLoan(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            return bbcc.CalcEmiOnChangeRecommendLoan(ifr);
        } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            return bbcc.CalcEmiOnChangeRecommendLoan(ifr);
        }
        return "";
    }
    //added by vandana

    public String calclateEMIOnTenureChange(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside caluclateEMIFinalEligibility");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);

        String MaxLoanTenure = "";
        String MinLoanTenure = "";
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
        String RecommendloanTenure = ifr.getValue("QNL_LA_FINALELIGIBILITY_Tenure").toString();
        Log.consoleLog(ifr, "FINALELIGIBILITY loanTenure==>" + RecommendloanTenure);
        double RecommendloanTenureCheck = Double.parseDouble(RecommendloanTenure);

        if (RecommendloanTenureCheck >= Double.parseDouble(MinLoanTenure)) {
            if (loan_selected.equalsIgnoreCase("Canara Budget")) {
                return bbcc.calclateEMIOnTenureChange(ifr);
            } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                return vlbcc.calclateEMIOnTenureChangeVL(ifr);
            } else if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                return pbcc.calclateEMIOnTenureChangePension(ifr);
            } else {
                Log.consoleLog(ifr, "No loan type selected==>" + loan_selected);
            }
        } else {
            ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Recommended Tenure should not be less than Minimum Tenure as per product."));
            return message.toString();
        }
        return "";
    }

    //Added by Sharon for Reset values of Co-Borrower
    public String ResetCoObligantCB(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside calcLoanAmount");
        Bpcm.ResetCoObligantCB(ifr);
        return "";

    }

    //Added by Janani for PDD VL
    public void formLoadPDD(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside formLoadDisbursementMaker");
        vlbcc.formLoadPDD(ifr);
    }

    //added by ishwarya on 03-07-2024
    public void mImplChangeProductSubProduct(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplChangeProductSubProduct");

        String product = "", subproduct = "";
        product = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString();
        subproduct = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString();
        Log.consoleLog(ifr, "product name:::" + product);
        Log.consoleLog(ifr, "subproduct name:::" + subproduct);
        if (product.equalsIgnoreCase(
                "PL") && subproduct.equalsIgnoreCase("STP-CB")) {
            ifr.setValue("LOAN_SELECTED", "Canara Budget");
            ifr.setValue("producttype", product);
            ifr.setValue("subproducttype", subproduct);
        } else if (product.equalsIgnoreCase(
                "PL") && subproduct.equalsIgnoreCase("STP-CP")) {
            ifr.setValue("LOAN_SELECTED", "Canara Pension");
            ifr.setValue("producttype", product);
            ifr.setValue("subproducttype", subproduct);
        } else if (product.equalsIgnoreCase(
                "VL") && subproduct.equalsIgnoreCase("CANTWO") || subproduct.equalsIgnoreCase("CANFOURG")) {
            ifr.setValue("LOAN_SELECTED", "Vehicle Loan");
            ifr.setValue("producttype", product);
            ifr.setValue("subproducttype", subproduct);
        } else {
            Log.consoleLog(ifr, "product, subproduct is null in proposed grid:::");
        }
    }

    //added by janani on 04/07/2024
    public void formLoadSanction(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside formLoadSanction");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            pbcc.formLoadSanctionCP(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.formLoadSanctionVL(ifr);
        }
    }

    //added by keerthana for fee&charge button enable(pension) on 04/07/2024
    public void OnChangeFeesAndChargeSection(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside OnChangeFeesAndChargeSection");
        ifr.setStyle("BTN_FetchFeeCharge", "disable", "false");
    }

    //added by logaraj for pension reset button on 04/07/2024
    public String resetCoObligantCP(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside calcLoanAmount");
        plpc.ResetCoObligantCP(ifr);
        return "";

    }

    //Added  by Aravindh for 06/07/24 
    public String OnChangeApprovedLoanAmountFinalEligibility(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside caluclateEMIFinalEligibility");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
            return bbcc.caluclateEMIOnChangeApprovedLoanAmount(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            return vlbcc.caluclateEMIOnChangeApprovedLoanAmount(ifr);
        }
        return "";
    }

    // added by vandana 08/07/2024
    public void listViewMisData(IFormReference ifr, String Control, String Event, String value) {//added by Sharon for Down payment validation on 17/06/2024
        Log.consoleLog(ifr, "inside listViewMisData : ");
        bbcc.listviewMisData(ifr);
    }

    //Added by Sharon for Reset values of Co-Borrower
    public String ResetCoObligantVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside calcLoanAmount");
        Vlpc.ResetCoObligantVL(ifr);
        return "";

    }

    //modified by keerthana to display accout.no on disbursement section
    public String disbursmentDataPopulation(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside disbursmentDataPopulation");
        JSONObject message = new JSONObject();

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);

        String inVisibleFields = "QNL_DISBURSMENT_ProbableDateofDisbursement,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_RepaymentType,QNL_DISBURSMENT_OtherFeeAndCharges,QNL_DISBURSMENT_DateofDisbursement,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_FixedTerm,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_Drawdown";
        String nonEditableFields = "QNL_DISBURSMENT_CustomerName,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_ProposedLoanAmount,QNL_DISBURSMENT_InstallmentAmount,QNL_DISBURSMENT_ROI,QNL_DISBURSMENT_Tenure";

        pcm.controlinvisiblity(ifr, inVisibleFields);
        pcm.controlDisable(ifr, nonEditableFields);
        //String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String query1 = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query1, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String query = ConfProperty.getQueryScript("DisbursmentCustomerDetails").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> customerDetails = cf.mExecuteQuery(ifr, query, "Execute query for fetching DisbursmentCustomerDetails ");
        if (customerDetails.size() > 0) {
            String customerName = customerDetails.get(0).get(0);
            String customerId = customerDetails.get(0).get(1);
            String accountNumber = customerDetails.get(0).get(2);

            ifr.setValue("QNL_DISBURSMENT_CustomerName", customerName);
            ifr.setValue("QNL_DISBURSMENT_CustomerID", customerId);
            if (loan_selected.equalsIgnoreCase("Canara Pension")) {
                ifr.setValue("QNL_DISBURSMENT_SalaryAccountnumber", accountNumber);
            }
        }
        //Modified by Aravindh on 8/8/24 for Disbursement details Population
        String proposedLoanAmont = ConfProperty.getQueryScript("DisbursmentproposedLoanAmont").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> DisbursementDetails = cf.mExecuteQuery(ifr, proposedLoanAmont, "Execute query for fetching DisbursmentproposedLoanAmont ");
        if (DisbursementDetails.size() > 0) {
            String SanctionAmount = DisbursementDetails.get(0).get(0);
            String FinalTenure = DisbursementDetails.get(0).get(1);
            String FinalRoi = DisbursementDetails.get(0).get(2);
            String EMI = DisbursementDetails.get(0).get(3);
            ifr.setValue("QNL_DISBURSMENT_ProposedLoanAmount", SanctionAmount);
            ifr.setValue("QNL_DISBURSMENT_ROI", FinalTenure);
            ifr.setValue("QNL_DISBURSMENT_Tenure", FinalRoi);
            ifr.setValue("QNL_DISBURSMENT_InstallmentAmount", EMI);
        }

        message.put("saveWorkitem", "true");
        return message.toString();
    }

    //Added by Sharon for onchange requeseted loan amount validation on 11/7/2024
    public String onchangeReqLoanAmt(IFormReference ifr, String Control, String Event, String value) {
        String onchangeReqLoanAmtVL = Vlpc.onchangeReqLoanAmtVL(ifr);
        return onchangeReqLoanAmtVL;

    }

    //added by janani on 11-07-2024
    public String mOnChangeAssetType(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside mOnChangeAssetType :; ");
        vlbcc.OnChangeAssetType(ifr);
        return "";
    }

    public String monchangeProfileVL(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside monchangeProfileVL :; ");
        Vlpc.onchangeProfileVL(ifr);
        return "";
    }

    public void coObligantCheck(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "inside coObligantCheck AcceleratorActivityManagerCode ");
        ifr.setStyle("Btn_Refresh", "disable", "false");
    }

    /*//added by ishwarya to calculate LTV on change of recommened and approved loan amt on 16-7-2024
    public String onChangeRecommendedAmountFinalEligibility(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside onChangeRecommendedAmountFinalEligibility :; ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.onChangeRecommendedAmountFinalEligibilityVL(ifr);
        }
        return "";
    }*/
    //modified by keerthana on 29/07/2024 for pension validation
    public void onChangeESignStatus(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside onChangeESignStatus AcceleratorActivityManagerCode ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            vlbcc.onChangeESignStatus(ifr);
        } else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            vlbcc.onChangeESignStatus(ifr);
        } else if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            vlbcc.onChangeESignStatus(ifr);
        }
    }

    public String onLoadCollateralDetails(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside onLoadCollateralDetails AcceleratorActivityManagerCode ");
        String mess = vlbcc.onLoadCollateralDetails(ifr, control, event, value);
        return mess;
    }

    public String nesltriger(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String subProduct = "";
        if (loan_selected.equalsIgnoreCase("Canara Budget")) {
            subProduct = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "subProductCode:" + subProduct);
        }

//        EsignIntegrationChannel NESL = new EsignIntegrationChannel();
//        String returnMessage = NESL.redirectNESLRequest(ifr, subProduct, "eSigning");
//        Log.consoleLog(ifr, "returnMessage from NESL :" + returnMessage);
//        if ((returnMessage.contains(RLOS_Constants.ERROR)) || (returnMessage.equalsIgnoreCase(""))
//                || returnMessage.contains("showMessage")) {
//            return pcm.returnErrorAPIThroughExecute(ifr);
//        }
        return "success";

    }

    public String mChangeBorrowerCreditHis(IFormReference ifr, String control, String event, String value) {
        String npaWriteOffCheck = ifr.getValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY").toString();
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);

        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Pension")) {
            if (npaWriteOffCheck.equalsIgnoreCase("Yes")) {
                JSONObject message = new JSONObject();
//                message.put("saveWorkitem", "true");
                ifr.setValue("QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", "");
                message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_SETTLEDACCOUNTINCREDITHISTORY", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
                return message.toString();
            }

        }
        return "";
    }

    public void onClickESignInitiate(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "inside onChangeESignStatus AcceleratorActivityManagerCode ");
        vlbcc.onClickESignInitiate(ifr);
    }

    //added by sharon on 29/07/2024
    public String mImplChangePurpose(IFormReference ifr, String Control, String Event, String value) {//Checked
        Log.consoleLog(ifr, "Inside mImplChangePurpose:");
        return Vlpc.mImplChangePurpose(ifr);
    }

    //Add by vishal yadav
    public void mAccOnSectionStateAddBorrowerConsentInMaker(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside mAccOnSectionStateAddBorrowerConsentInMaker:");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String ApplicantType = ConfProperty.getCommonPropertyValue("ApplicantTypeConsent");
            String[] ApplicantTypeArray = ApplicantType.split("~");

            String query1 = ConfProperty.getQueryScript("BASICINFOBorrower").replaceAll("#PID#", PID).replaceAll("#Applicanttype#", ApplicantTypeArray[0]);
            String query2 = ConfProperty.getQueryScript("BUREAUCONSENTTABLE").replaceAll("#PID#", PID);
            List<List<String>> query1Result = cf.mExecuteQuery(ifr, query1, "Fetching insertion order id for given applicant type");
            List<List<String>> query2Result = cf.mExecuteQuery(ifr, query2, "Fetching all added consent");
            JSONArray jsonArray = new JSONArray();
            boolean borrAdded = false;

            String date = "";
            String queryDate = "SELECT DATETI FROM LOS_WIREFERENCE_TABLE WHERE WINAME='" + PID + "'";
            List<List<String>> queryDateResult = cf.mExecuteQuery(ifr, queryDate, "Fetching insertion order id for given applicant type");
            if (queryDateResult.size() > 0) {
                date = queryDateResult.get(0).get(0);
            }
            Log.consoleLog(ifr, "Inside mAccOnSectionStateAddBorrowerConsentInMaker:date::" + date);
            SimpleDateFormat sdf = new SimpleDateFormat(date);
            String currDate = sdf.format(new Date());
            if (query1Result.size() > 0) {
                List<String> existingConsents = new ArrayList<>();
                for (List<String> resultRow : query2Result) {
                    existingConsents.add(resultRow.get(0));
                }
                if (!existingConsents.contains(query1Result.get(0).get(0)) && !borrAdded) {
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("QNL_BUREAU_CONSENT_PartyType", query1Result.get(0).get(0));
                    jsonobj.put("QNL_BUREAU_CONSENT_Methodology", "P");
                    jsonobj.put("QNL_BUREAU_CONSENT_ConsentReceived", "Accepted");
                    jsonobj.put("QNL_BUREAU_CONSENT_ConsentVerifiedOn", currDate);
                    jsonobj.put("QNL_BUREAU_CONSENT_ConsentVerifiedBy", ifr.getUserName());
                    jsonArray.add(jsonobj);
                    ifr.addDataToGrid("ALV_BUREAU_CONSENT", jsonArray);
                    borrAdded = true;
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccOnSectionStateAddBorrowerConsentInMaker method::" + e);
            Log.errorLog(ifr, "Exception in mAccOnSectionStateAddBorrowerConsentInMaker method::" + e);
        }
    }

    public void autoPopulateBAMvalues(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside autoPopulateBAMvalues:");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonobj = new JSONObject();
            //String Misclassname = "";
            String BAMCode = "";
            String CODPROD = "";
            String BAMQueueVar = "";
            HashMap<String, Object> objm = new HashMap<>();
            String[] BAMKey = {"PRISECTOR", "SECTOR", "SSISUBSEC", "GUARANTEECOVER", "SCHEMES", "SUBSCHEME", "BSRCODE", "RETAILBASEL-IICUSTOMERTYPE", "STATUSIB", "RBIPURPOSECODE", "RAH"};
            String[] ControlId = {"QNL_BAM83_PRI_SECTOR_N_PRI_SECTOR", "QNL_BAM83_SECTOR", "QNL_BAM83_SSISUBSEC", "QNL_BAM83_GUARANTEE_COVER", "QNL_BAM83_SCHEMES", "QNL_BAM83_BSR_CODE", "QNL_BAM83_RETAIL_BASEL_II_CUSTOMER_TYPE", "QNL_BAM83_STATUSIB", "QNL_BAM83_RBI_PURPOSE_CODE", "QNL_BAM83_SPECIAL_BENEFICIARIES", "QNL_BAM83_RAH"};
            // Query to get MIS_CLASS values

            String codprod = "select CBSPRODUCTCODE  from  LOS_EXT_TABLE  where PID='" + PID + "'";
            List<List<String>> codProd = cf.mExecuteQuery(ifr, codprod, "codprod_Query:");
            if (codProd.size() > 0) {
                CODPROD = codProd.get(0).get(0);
            }
            Log.consoleLog(ifr, "CODPROD  " + CODPROD);
            for (int i = 0; i < BAMKey.length; i++) {
                //String misDescQuery = objm.getOrDefault(BAMKey[i], "0").toString().replaceAll("#CODPROD#", CODPROD);
                String misDescQuery = ConfProperty.getQueryScript(BAMKey[i]).replaceAll("#CODPROD#", CODPROD);
                List<List<String>> descResults = cf.mExecuteQuery(ifr, misDescQuery, "misDescQuery_Query:");
                if (descResults.size() > 0 && descResults.size() < 2) {
                    BAMCode = descResults.get(0).get(1); // Get MIS_CLASS value (code)
                    Log.consoleLog(ifr, "BAMCode: " + BAMCode);
                    BAMQueueVar = ControlId[i];
                    Log.consoleLog(ifr, "BAMQueueVariable: " + BAMQueueVar);
                    jsonobj.put(BAMQueueVar, BAMCode);
                }
            }
            jsonArray.add(jsonobj);
            Log.consoleLog(ifr, "jsonArray::" + jsonArray);
            ifr.addDataToGrid("ALV_BAM83", jsonArray);

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in autoPopulateBAMvalues method::" + e);
            Log.errorLog(ifr, "Exception in autoPopulateBAMvalues method::" + e);
        }
    }

    public void OnLoadESignDetails(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside AccleratorActivityManager --> OnLoadESignDetails : ");
        vlbcc.onLoadESign(ifr);
    }

    public void mImplChangePurposeHL(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplChangePurposeHL:");
        hlpcc.mChangePurpose(ifr);
    }

    public void onchangeReqLoanAmtHL(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside onchangeReqLoanAmtHL:");
        hlpcc.onchangeReqLoanAmt(ifr);
    }

}
