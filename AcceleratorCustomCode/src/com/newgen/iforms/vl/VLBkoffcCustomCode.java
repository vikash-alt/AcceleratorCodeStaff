/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.vl;

import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.acceleratorCode.AcceleratorActivityManagerCode;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class VLBkoffcCustomCode {

    //Collateral CB1 = new Collateral();
    // CBS_LoanAccountCreation CB2 = new CBS_LoanAccountCreation();
    //CBS_FundTransfer CB3 = new CBS_FundTransfer();
    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonMethods objcm = new CommonMethods();
    CustomerAccountSummary cas = new CustomerAccountSummary();
    VLPortalCustomCode vlpc = new VLPortalCustomCode();
    BRMCommonRules brmcr = new BRMCommonRules();
    //  LoanAccountCreation API = new LoanAccountCreation();
    LoanEligibilityCheck lec = new LoanEligibilityCheck();
    //APIPreprocessor objPreprocess = new APIPreprocessor();
    BudgetPortalCustomCode bpcm = new BudgetPortalCustomCode();

    //Added by Ahmed on 01-07-2024 for Vechile Loan Disbursement
    public String createVLLoanDisburse(IFormReference ifr) {

        Log.consoleLog(ifr, "#createVLLoanDisburse started...");
        try {

            VLAPIPreprocessor objvlPreprocessor = new VLAPIPreprocessor();
            String collateralStatus = objvlPreprocessor.execCollateral(ifr, "VL");
            Log.consoleLog(ifr, "after calling collateral api....." + collateralStatus);
            if (!collateralStatus.contains(RLOS_Constants.ERROR)) {
                String loanAccountNumber = objvlPreprocessor.execLoanAccountCreation(ifr, "VL");
                Log.consoleLog(ifr, "after calling loanAccountNumber api....." + loanAccountNumber);
                if (!loanAccountNumber.contains(RLOS_Constants.ERROR)) {
                    String creditRatingStatus = objvlPreprocessor.execCreditRating(ifr, loanAccountNumber, "VL");
                    Log.consoleLog(ifr, "after calling creditRatingStatus api....." + creditRatingStatus);
                    if (!creditRatingStatus.contains(RLOS_Constants.ERROR)) {
                        return RLOS_Constants.SUCCESS;
                    }
                }
            }
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception:/createVLLoanDisburse" + e);
            Log.errorLog(ifr, "Exception:/createVLLoanDisburse" + e);
        }
        return RLOS_Constants.ERROR;
    }

    //    public String CBSFinalScreenValidation(IFormReference ifr, String ProcessInsatnceId) {
    //
    //        Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
    //        try {
    //
    //            Log.consoleLog(ifr, "Details available to execute CBS API`s");
    //            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
    //            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
    //            String Collateral = CB1.getCollateral(ifr, CustomerId);
    //
    //            if (!(Collateral.contains(RLOS_Constants.ERROR))) {
    //                String LoanAccountNumber = objPreprocess.execLoanAccountCreation(ifr, "VL");
    //                if (!(LoanAccountNumber.contains(RLOS_Constants.ERROR))) {
    //
    //                    String Query = "SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
    //                            + "WHERE WINAME='" + ProcessInsatnceId + "'";
    //                    List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
    //                    String loanAmount = "";
    //                    String tenure = "";
    //                    if (!Output3.isEmpty()) {
    //                        loanAmount = Output3.get(0).get(0);
    //                        tenure = Output3.get(0).get(1);
    //                    }
    //                    String SBACCNUMBER = "";
    //                    String SBAccNo = "select accountid from los_nl_basic_info "
    //                            + "where pid='" + ProcessInsatnceId + "' and rownum=1";
    //                    Log.consoleLog(ifr, "SBAccNo==>" + SBAccNo);
    //                    List<List<String>> SBACCNOOutput = ifr.getDataFromDB(SBAccNo);
    //
    //                    if (SBACCNOOutput.size() > 0) {
    //                        SBACCNUMBER = SBACCNOOutput.get(0).get(0);
    //                    }
    //
    //                    String CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER,
    //                            "VL", loanAmount, tenure);
    //                    return CBS_FundTransfer;
    //                }
    //            }
    //
    //        } catch (ParseException e) {
    //            Log.consoleLog(ifr, "Exception:" + e);
    //            Log.errorLog(ifr, "Exception:" + e);
    //
    //        }
    //        return RLOS_Constants.ERROR;
    //    }
    /*public String autopopulate_liabilityVL(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside autopopulate_liability VL==>");

        String[] FieldVisibleFalse = new String[]{"QNL_AL_LIAB_VAL_Loan_Acc_No"};

        for (int i = 0; i < FieldVisibleFalse.length; i++) {
            ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
        }

        String[] FieldVisibleMandatoryDisable = new String[]{"QNL_AL_LIAB_VAL_ApplicantType",
            "QNL_AL_LIAB_VAL_ConsiderForEligibility", "QNL_AL_LIAB_VAL_LoanType",
            "QNL_AL_LIAB_VAL_Bank", "QNL_AL_LIAB_VAL_loanStartDate",
            "QNL_AL_LIAB_VAL_Loan_LiabAmt", "QNL_AL_LIAB_VAL_Loan_LiabOut",
            "QNL_AL_LIAB_VAL_EMIAmt"};

        for (int i = 0; i < FieldVisibleMandatoryDisable.length; i++) {
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "visible", "true");
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "disable", "true");
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "true");
        }
        Log.consoleLog(ifr, " Vechicle Loan liability Details fields visible hide and mandatory check end");

        WDGeneralData Data = ifr.getObjGeneralData();
        String PID = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + PID);
        //String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + PID + "'";
        int LiabilitySize = ifr.getDataFromGrid("ALV_AL_LIAB_VAL").size();
        Log.consoleLog(ifr, "inside LiabilitySize::" + LiabilitySize);
        if (LiabilitySize <= 0) {
            try {
                Log.consoleLog(ifr, "LiabilitySize::" + LiabilitySize);
                String responseBody = "";
                JSONParser parser1 = new JSONParser();
//                    if (ConfProperty.getIntegrationValue("CBSMOCKFLAG").equalsIgnoreCase("Y")) {
//                        responseBody = "{\"body\":{\"INProfileResponse\":{\"Header\":{\"SystemCode\":0,\"MessageText\":\"\",\"ReportDate\":20240205,\"ReportTime\":183314},\"UserMessage\":{\"UserMessageText\":\"Normal Response\"},\"CreditProfileHeader\":{\"Enquiry_Username\":\"cpu2canara_prod07\",\"ReportDate\":20240205,\"ReportTime\":183314,\"Version\":\"V2.4\",\"ReportNumber\":1707138193771,\"Subscriber\":\"\",\"Subscriber_Name\":\"Canara Bank\"},\"Current_Application\":{\"Current_Application_Details\":{\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"Current_Applicant_Details\":{\"Last_Name\":\"s\",\"First_Name\":\"AMARA PAVANKUMAR\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":1,\"IncomeTaxPan\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Passport_Number\":\"\",\"Passport_Issue_Date\":\"\",\"Passport_Expiration_Date\":\"\",\"Voter_s_Identity_Card\":\"\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"Ration_Card_Number\":\"\",\"Ration_Card_Issue_Date\":\"\",\"Ration_Card_Expiration_Date\":\"\",\"Universal_ID_Number\":\"\",\"Universal_ID_Issue_Date\":\"\",\"Universal_ID_Expiration_Date\":\"\",\"Date_Of_Birth_Applicant\":19890525,\"Telephone_Number_Applicant_1st\":\"\",\"Telephone_Extension\":\"\",\"Telephone_Type\":\"\",\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"Current_Other_Details\":{\"Income\":\"\",\"Marital_Status\":2,\"Employment_Status\":\"S\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":0},\"Current_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"},\"Current_Applicant_Additional_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"}}},\"CAIS_Account\":{\"CAIS_Summary\":{\"Credit_Account\":{\"CreditAccountTotal\":6,\"CreditAccountActive\":6,\"CreditAccountDefault\":0,\"CreditAccountClosed\":0,\"CADSuitFiledCurrentBalance\":0},\"Total_Outstanding_Balance\":{\"Outstanding_Balance_Secured\":4825631,\"Outstanding_Balance_Secured_Percentage\":99,\"Outstanding_Balance_UnSecured\":42529,\"Outstanding_Balance_UnSecured_Percentage\":1,\"Outstanding_Balance_All\":4868160}},\"CAIS_Account_DETAILS\":[{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C000115766\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20140721,\"Credit_Limit_Amount\":25000,\"Highest_Credit_or_Original_Loan_Amount\":\"\",\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"?00000000000000000000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20210531,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20170228,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2021,\"Month\":5,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":3,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":2510,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":2,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":1,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":12,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":11,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST TELLABA\",\"Third_Line_Of_Address_non_normalized\":\"VIA ANDRAPRADESH\",\"City_non_normalized\":\"###\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9492537874},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889254001153,\"Portfolio_Type\":\"I\",\"Account_Type\":12,\"Open_Date\":20190121,\"Highest_Credit_or_Original_Loan_Amount\":200000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20240102,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":7,\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":10,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":139710,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":117563,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":115619,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":93056,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":123065,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":[{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}]},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":2536667005808,\"Portfolio_Type\":\"M\",\"Account_Type\":2,\"Open_Date\":20190424,\"Highest_Credit_or_Original_Loan_Amount\":4500000,\"Terms_Duration\":360,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":5170000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":360,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4704642,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705175,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705017,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705367,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705026,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889694000003,\"Portfolio_Type\":\"I\",\"Account_Type\":13,\"Open_Date\":20200716,\"Highest_Credit_or_Original_Loan_Amount\":75000,\"Terms_Duration\":84,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"P\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":88000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":84,\"Promotional_Rate_Flag\":\"\",\"Income\":100001,\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20200731,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":31874,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":33059,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":34235,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":35407,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":36569,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PVTXXXXXXXX\",\"Subscriber_Name\":\"XXXXXXXXXX\",\"Account_Number\":\"XXXXXXXXXXXX2879\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20220926,\"Credit_Limit_Amount\":340000,\"Highest_Credit_or_Original_Loan_Amount\":45111,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"0000000000000???????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231112,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231023,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20221012,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":11,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":14929,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4593,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9830,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9043,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":6,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":10429,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA\",\"First_Name_Non_Normalized\":\"PAVAN\",\"Middle_Name_1_Non_Normalized\":\"KUMAR\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":[{\"First_Line_Of_Address_non_normalized\":\"CANARA BANK 2ND FLOOR  NAVEENCOMPLEX MG\",\"Second_Line_Of_Address_non_normalized\":\"ROAD HEAD OFFICE  OPP 1MG MALL\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560002,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"27   3RD FLOOR GANESH ILLAM ATMANANDA CO\",\"Second_Line_Of_Address_non_normalized\":\"LONY  SULTANPALYA MAIN ROAD RT NAGAR  NE\",\"Third_Line_Of_Address_non_normalized\":\"AR NARAYANA APARTMENT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560032,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"3 108 CHIMAKURTHI CHANDRAPADU  PRAKASAM\",\"Second_Line_Of_Address_non_normalized\":\"\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523226,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"}],\"CAIS_Holder_Phone_Details\":[{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"},{\"Telephone_Number\":2011111111,\"Telephone_Type\":3,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}],\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C117024315\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20230830,\"Credit_Limit_Amount\":100000,\"Highest_Credit_or_Original_Loan_Amount\":100000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"000?????????????????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0,\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20230930,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":48588,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":7337,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVAN KUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":560009,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}}]},\"Match_result\":{\"Exact_match\":\"Y\"},\"TotalCAPS_Summary\":{\"TotalCAPSLast7Days\":2,\"TotalCAPSLast30Days\":2,\"TotalCAPSLast90Days\":2,\"TotalCAPSLast180Days\":2},\"CAPS\":{\"CAPS_Summary\":{\"CAPSLast7Days\":2,\"CAPSLast30Days\":2,\"CAPSLast90Days\":2,\"CAPSLast180Days\":2},\"CAPS_Application_Details\":[{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":161727,\"ReportNumber\":1706611647587,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"},{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":132619,\"ReportNumber\":1706601379101,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"}]},\"NonCreditCAPS\":{\"NonCreditCAPS_Summary\":{\"NonCreditCAPSLast7Days\":0,\"NonCreditCAPSLast30Days\":0,\"NonCreditCAPSLast90Days\":0,\"NonCreditCAPSLast180Days\":0}},\"PSV\":{\"BFHL_Ex_HL\":{\"TN_of_BFHL_CAD_Ex_HL\":\"\",\"Tot_Val_of_BFHL_CAD\":\"\",\"MNT_SMR_BFHL_CAD\":\"\"},\"HL_CAD\":{\"TN_of_HL_CAD\":\"\",\"Tot_Val_of_HL_CAD\":\"\",\"MNT_SMR_HL_CAD\":\"\"},\"Telcos_CAD\":{\"TN_of_Telcos_CAD\":\"\",\"Tot_Val_of_Telcos_CAD\":\"\",\"MNT_SMR_Telcos_CAD\":\"\"},\"MF_CAD\":{\"TN_of_MF_CAD\":\"\",\"Tot_Val_of_MF_CAD\":\"\",\"MNT_SMR_MF_CAD\":\"\"},\"Retail_CAD\":{\"TN_of_Retail_CAD\":\"\",\"Tot_Val_of_Retail_CAD\":\"\",\"MNT_SMR_Retail_CAD\":\"\"},\"Total_CAD\":{\"TN_of_All_CAD\":\"\",\"Tot_Val_of_All_CAD\":\"\",\"MNT_SMR_CAD_All\":\"\"},\"BFHL_ACA_ExHL\":{\"TN_of_BFHL_ACA_Ex_HL\":\"\",\"Bal_BFHL_ACA_Ex_HL\":\"\",\"WCD_St_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_6_MNT_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_7_12_MNT_BFHL_ACA_Ex_HL\":\"\",\"Age_of_Oldest_BFHL_ACA_Ex_HL\":\"\",\"HCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\",\"TCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\"},\"HL_ACA\":{\"TN_of_HL_ACA\":\"\",\"Bal_HL_ACA\":\"\",\"WCD_St_HL_ACA\":\"\",\"WDS_Pr_6_MNT_HL_ACA\":\"\",\"WDS_Pr_7_12_MNT_HL_ACA\":\"\",\"Age_of_Oldest_HL_ACA\":\"\"},\"MF_ACA\":{\"TN_of_MF_ACA\":\"\",\"Total_Bal_MF_ACA\":\"\",\"WCD_St_MF_ACA\":\"\",\"WDS_Pr_6_MNT_MF_ACA\":\"\",\"WDS_Pr_7_12_MNT_MF_ACA\":\"\",\"Age_of_Oldest_MF_ACA\":\"\"},\"Telcos_ACA\":{\"TN_of_Telcos_ACA\":\"\",\"Total_Bal_Telcos_ACA\":\"\",\"WCD_St_Telcos_ACA\":\"\",\"WDS_Pr_6_MNT_Telcos_ACA\":\"\",\"WDS_Pr_7_12_MNT_Telcos_ACA\":\"\",\"Age_of_Oldest_Telcos_ACA\":\"\"},\"Retail_ACA\":{\"TN_of_Retail_ACA\":\"\",\"Total_Bal_Retail_ACA\":\"\",\"WCD_St_Retail_ACA\":\"\",\"WDS_Pr_6_MNT_Retail_ACA\":\"\",\"WDS_Pr_7_12_MNT_Retail_ACA\":\"\",\"Age_of_Oldest_Retail_ACA\":\"\",\"HCB_Lm_Per_Rev_Acc_Ret\":\"\",\"Tot_Cur_Bal_Lm_Per_Rev_Acc_Ret\":\"\"},\"Total_ACA\":{\"TN_of_All_ACA\":\"\",\"Bal_All_ACA_Ex_HL\":\"\",\"WCD_St_All_ACA\":\"\",\"WDS_Pr_6_MNT_All_ACA\":\"\",\"WDS_Pr_7_12_MNT_All_ACA\":\"\",\"Age_of_Oldest_All_ACA\":\"\"},\"BFHL_ICA_Ex_HL\":{\"TN_of_NDel_BFHL_InACA_Ex_HL\":\"\",\"TN_of_Del_BFHL_InACA_Ex_HL\":\"\"},\"HL_ICA\":{\"TN_of_NDel_HL_InACA\":\"\",\"TN_of_Del_HL_InACA\":\"\"},\"MF_ICA\":{\"TN_of_NDel_MF_InACA\":\"\",\"TN_of_Del_MF_InACA\":\"\"},\"Telcos_ICA\":{\"TN_of_NDel_Telcos_InACA\":\"\",\"TN_of_Del_Telcos_InACA\":\"\"},\"Retail_ICA\":{\"TN_of_NDel_Retail_InACA\":\"\",\"TN_of_Del_Retail_InACA\":\"\"},\"PSV_CAPS\":{\"BFHL_CAPS_Last_90_Days\":\"\",\"MF_CAPS_Last_90_Days\":\"\",\"Telcos_CAPS_Last_90_Days\":\"\",\"Retail_CAPS_Last_90_Days\":\"\"},\"Own_Company_Data\":{\"TN_of_OCom_CAD\":\"\",\"Tot_Val_of_OCom_CAD\":\"\",\"MNT_SMR_OCom_CAD\":\"\",\"TN_of_OCom_ACA\":\"\",\"Bal_OCom_ACA_Ex_HL\":\"\",\"Bal_OCom_ACA_HL_Only\":\"\",\"WCD_St_OCom_ACA\":\"\",\"HCB_Lm_Per_Rev_OCom_ACA\":\"\",\"TN_of_NDel_OCom_InACA\":\"\",\"TN_of_Del_OCom_InACA\":\"\",\"TN_of_OCom_CAPS_Last_90_days\":\"\"},\"Oth_CB_Information\":{\"Any_Rel_CB_Data_Dis_Y_N\":\"\",\"Oth_Rel_CB_DFC_Pos_Mat_Y_N\":\"\"},\"Indian_Market_Specific_Var\":{\"TN_of_CAD_classed_as_SFWDWO\":\"\",\"MNT_SMR_CAD_classed_as_SFWDWO\":\"\",\"Num_of_CAD_SFWDWO_Last_24_MNT\":\"\",\"Tot_Cur_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Live_UAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_UAcc\":\"\"}},\"SCORE\":{\"BureauScore\":829,\"BureauScoreConfidLevel\":\"H\",\"CreditRating\":\"\"}}}}";
//                        //responseBody = "{  \"body\": {    \"INProfileResponse\": {      \"SCORE\": {        \"BureauScore\": 765,        \"BureauScoreConfidLevel\": \"H\",        \"CreditRating\": \"\"      },      \"Header\": {        \"ReportTime\": 155554,        \"SystemCode\": 0,        \"ReportDate\": 20230925,        \"MessageText\": \"\"      },      \"TotalCAPS_Summary\": {        \"TotalCAPSLast90Days\": 1,        \"TotalCAPSLast7Days\": 1,        \"TotalCAPSLast30Days\": 1,        \"TotalCAPSLast180Days\": 1      },      \"CreditProfileHeader\": {        \"ReportTime\": 155554,        \"Version\": \"V2.4\",        \"Subscriber\": \"\",        \"Enquiry_Username\": \"cpu2canara_prod02\",        \"ReportNumber\": 1695637552936,        \"ReportDate\": 20230925,        \"Subscriber_Name\": \"Canara Bank\"      },      \"NonCreditCAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220622,          \"Subscriber_code\": \"PVTXXXXXXXX\",          \"Enquiry_Reason\": 99,          \"Amount_Financed\": 0,          \"Duration_Of_Agreement\": 0,          \"ReportTime\": 161428,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 1,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 6381086803,            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": \"\",            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \".\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"\",            \"FlatNoPlotNoHouseNo\": \"MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": \"\",          \"ReportNumber\": 1655894668194,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"NonCreditCAPS_Summary\": {          \"NonCreditCAPSLast30Days\": 1,          \"NonCreditCAPSLast180Days\": 1,          \"NonCreditCAPSLast90Days\": 1,          \"NonCreditCAPSLast7Days\": 1        }      },      \"UserMessage\": {        \"UserMessageText\": \"Normal Response\"      },      \"CAIS_Account\": {        \"CAIS_Summary\": {          \"Total_Outstanding_Balance\": {            \"Outstanding_Balance_Secured\": 444505,            \"Outstanding_Balance_UnSecured_Percentage\": 0,            \"Outstanding_Balance_All\": 983589,            \"Outstanding_Balance_Secured_Percentage\": 45,            \"Outstanding_Balance_UnSecured\": -20          },          \"Credit_Account\": {            \"CreditAccountActive\": 6,            \"CreditAccountClosed\": 36,            \"CreditAccountDefault\": 0,            \"CreditAccountTotal\": 42,            \"CADSuitFiledCurrentBalance\": 0          }        },        \"CAIS_Account_DETAILS\": [          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160613,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSSSSSSS???????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20170630,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20170606,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842021906,            \"Date_of_Last_Payment\": 20170606,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 90000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 12          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20160606,            \"Account_Type\": 0,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"0000000000000000000000000000001?0100\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20160930,            \"Payment_Rating\": 0,            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2022              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 25,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2021              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2020              },              {                \"Days_Past_Due\": 53,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 30,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2019              },              {                \"Days_Past_Due\": 0,                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 31,                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": 0,                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 5,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 4,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 3,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 2,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2016              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2016              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20220731,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20220704,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 10,            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": [              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              },              {                \"PAN_Issue_Date\": \"\",                \"Universal_ID_Issue_Date\": \"\",                \"Driver_License_Number\": \"\",                \"Income_TAX_PAN\": \"BOBPM6304K\",                \"Driver_License_Issue_Date\": \"\",                \"Driver_License_Expiration_Date\": \"\",                \"EMailId\": \"\",                \"PAN_Expiration_Date\": \"\",                \"Universal_ID_Number\": \"\",                \"Universal_ID_Expiration_Date\": \"\"              }            ],            \"Account_Number\": 1028261000011,            \"Date_of_Last_Payment\": 20220704,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 800000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 0,            \"Terms_Duration\": \"\",            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 13          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170607,            \"Account_Type\": 53,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Surname_Non_Normalized\": \"MUMTAJ K\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS????????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"Canara Bank\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": \"\",            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 8,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": {              \"State_non_normalized\": 33,              \"Fifth_Line_Of_Address_non_normalized\": \"\",              \"Residence_code_non_normalized\": \"\",              \"First_Line_Of_Address_non_normalized\": \"W/O KAJNIMOHAMED\",              \"City_non_normalized\": \"PALANI TK\",              \"Second_Line_Of_Address_non_normalized\": \"5/107 PERICHIPALAYAM\",              \"ZIP_Postal_Code_non_normalized\": 624617,              \"Address_indicator_non_normalized\": 4,              \"Third_Line_Of_Address_non_normalized\": \"PERICHIPALAYAM PO\",              \"CountryCode_non_normalized\": \"IB\"            },            \"Date_Reported\": 20171031,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": [              {                \"Telephone_Number\": 917373538853,                \"Telephone_Type\": 0              },              {                \"Telephone_Number\": 919787458226,                \"Telephone_Type\": 0              }            ],            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20171003,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": \"\",            \"Terms_Frequency\": \"\",            \"CAIS_Holder_ID_Details\": {              \"PAN_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Income_TAX_PAN\": \"BOBPM6304K\",              \"Driver_License_Issue_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"PAN_Expiration_Date\": \"\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": 1028842023502,            \"Date_of_Last_Payment\": 20171003,            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 100000,            \"Settlement_Amount\": \"\",            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": \"\",            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 12,            \"Terms_Duration\": 12,            \"Identification_Number\": \"PUBCANAR03\",            \"Account_Status\": 15          },          {            \"AccountHoldertypeCode\": 1,            \"LitigationStatusDate\": \"\",            \"Open_Date\": 20170608,            \"Account_Type\": 7,            \"Original_Charge_off_Amount\": \"\",            \"Income\": \"\",            \"Subscriber_comments\": \"\",            \"CurrencyCode\": \"INR\",            \"CAIS_Holder_Details\": {              \"Surname_Non_Normalized\": \"MUMTHAJ\",              \"Alias\": \"\",              \"Gender_Code\": 2,              \"Date_of_birth\": 19750605,              \"First_Name_Non_Normalized\": \"K\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Middle_Name_1_Non_Normalized\": \"\",              \"Middle_Name_3_Non_Normalized\": \"\",              \"Middle_Name_2_Non_Normalized\": \"\"            },            \"Payment_History_Profile\": \"SSSS?SS?????????????????????????????\",            \"Portfolio_Type\": \"I\",            \"DateOfAddition\": 20170630,            \"Payment_Rating\": \"S\",            \"Value_of_Collateral\": \"\",            \"Occupation_Code\": \"\",            \"Subscriber_Name\": \"XXXXXXXXXX\",            \"SuitFiled_WilfulDefault\": \"\",            \"Written_off_Settled_Status\": \"\",            \"Written_Off_Amt_Total\": 0,            \"Date_Of_First_Delinquency\": \"\",            \"Promotional_Rate_Flag\": \"\",            \"CAIS_Account_History\": [              {                \"Days_Past_Due\": \"\",                \"Month\": 1,                \"Asset_Classification\": \"S\",                \"Year\": 2018              },              {                \"Days_Past_Due\": \"\",                \"Month\": 12,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 11,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 10,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 9,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 7,                \"Asset_Classification\": \"S\",                \"Year\": 2017              },              {                \"Days_Past_Due\": \"\",                \"Month\": 6,                \"Asset_Classification\": \"S\",                \"Year\": 2017              }            ],            \"CAIS_Holder_Address_Details\": [              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107/5 MAIN ROAD\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PERITCHIPALAYAM,KOTTATHURAI\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"KEERANUR,DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              },              {                \"State_non_normalized\": 33,                \"Fifth_Line_Of_Address_non_normalized\": \"\",                \"Residence_code_non_normalized\": \"\",                \"First_Line_Of_Address_non_normalized\": \"107 NAFILA ILLAM\",                \"City_non_normalized\": \"DINDIGUL\",                \"Second_Line_Of_Address_non_normalized\": \"PALANI MAIN ROAD\",                \"ZIP_Postal_Code_non_normalized\": 624617,                \"Address_indicator_non_normalized\": 4,                \"Third_Line_Of_Address_non_normalized\": \"PALANI TK, DINDIGUL\",                \"CountryCode_non_normalized\": \"IB\"              }            ],            \"Date_Reported\": 20180131,            \"SuitFiledWillfulDefaultWrittenOffStatus\": \"\",            \"CAIS_Holder_Phone_Details\": {              \"Telephone_Number\": \"\",              \"Mobile_Telephone_Number\": 9787458226,              \"Telephone_Type\": 1            },            \"Value_of_Credits_Last_Month\": \"\",            \"Date_Closed\": 20180108,            \"Current_Balance\": 0,            \"Scheduled_Monthly_Payment_Amount\": \"\",            \"Amount_Past_Due\": \"\",            \"Rate_of_Interest\": 9,            \"Terms_Frequency\": \"M\",            \"CAIS_Holder_ID_Details\": {              \"Ration_Card_Number\": \"22G0539113\",              \"Voter_ID_Expiration_Date\": \"\",              \"Driver_License_Expiration_Date\": \"\",              \"EMailId\": \"\",              \"Ration_Card_Expiration_Date\": \"\",              \"Ration_Card_Issue_Date\": \"\",              \"Universal_ID_Issue_Date\": \"\",              \"Driver_License_Number\": \"\",              \"Voter_ID_Issue_Date\": \"\",              \"Driver_License_Issue_Date\": \"\",              \"Voter_ID_Number\": \"FJV3384443\",              \"Universal_ID_Number\": \"\",              \"Universal_ID_Expiration_Date\": \"\"            },            \"Account_Number\": \"XXXXXXXXXXXXX6718\",            \"Date_of_Last_Payment\": \"\",            \"Special_Comment\": \"\",            \"DefaultStatusDate\": \"\",            \"Income_Indicator\": \"\",            \"Highest_Credit_or_Original_Loan_Amount\": 47000,            \"Settlement_Amount\": 0,            \"Type_of_Collateral\": \"\",            \"WriteOffStatusDate\": \"\",            \"Written_Off_Amt_Principal\": 0,            \"Income_Frequency_Indicator\": \"\",            \"Consumer_comments\": \"\",            \"Repayment_Tenure\": 24,            \"Terms_Duration\": 24,            \"Identification_Number\": \"PUBXXXXXXXX\",            \"Account_Status\": 15          }        ]      },      \"Match_result\": {        \"Exact_match\": \"Y\"      },      \"Current_Application\": {        \"Current_Application_Details\": {          \"Current_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"S\",            \"Number_of_Major_Credit_Card_Held\": 0,            \"Marital_Status\": 2,            \"Income\": \"\"          },          \"Enquiry_Reason\": 14,          \"Current_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Amount_Financed\": 1500000,          \"Current_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"5/107 PERICHIPALAYAM\",            \"FlatNoPlotNoHouseNo\": \"W/O KAJNIMOHAMED\",            \"City\": \"PALANI TK\",            \"PINCode\": 624617          },          \"Duration_Of_Agreement\": 180,          \"Finance_Purpose\": 48,          \"Current_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": \"\",            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": \"\",            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"MobilePhoneNumber\": 9787458226,            \"Middle_Name3\": \"\",            \"Passport_Number\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 1,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"K\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          }        }      },      \"CAPS\": {        \"CAPS_Application_Details\": {          \"Date_of_Request\": 20220210,          \"Subscriber_code\": \"OTHXXXXXXXX\",          \"Enquiry_Reason\": 14,          \"Amount_Financed\": 2500000,          \"Duration_Of_Agreement\": 180,          \"ReportTime\": 125442,          \"CAPS_Applicant_Details\": {            \"IncomeTaxPan\": \"BOBPM6304K\",            \"PAN_Issue_Date\": \"\",            \"Passport_number\": \"\",            \"Voter_ID_Expiration_Date\": \"\",            \"Telephone_Number_Applicant_1st\": 9787458226,            \"Voter_s_Identity_Card\": \"\",            \"Telephone_Type\": 0,            \"Middle_Name2\": \"\",            \"Middle_Name1\": \"\",            \"Middle_Name3\": \"\",            \"First_Name\": \"MUMTAJ\",            \"Ration_Card_Issue_Date\": \"\",            \"Telephone_Extension\": \"\",            \"Driver_License_Number\": \"\",            \"Voter_ID_Issue_Date\": \"\",            \"Gender_Code\": 2,            \"PAN_Expiration_Date\": \"\",            \"Universal_ID_Number\": \"\",            \"Universal_ID_Expiration_Date\": \"\",            \"Ration_Card_Number\": \"\",            \"Driver_License_Expiration_Date\": \"\",            \"EMailId\": \"MUMTAJK809@GMAIL.COM\",            \"Ration_Card_Expiration_Date\": \"\",            \"Universal_ID_Issue_Date\": \"\",            \"Date_Of_Birth_Applicant\": 19750605,            \"Last_Name\": \"KAJINI\",            \"Driver_License_Issue_Date\": \"\",            \"Passport_Expiration_Date\": \"\",            \"Passport_Issue_Date\": \"\"          },          \"CAPS_Applicant_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"DINDIGUL\",            \"FlatNoPlotNoHouseNo\": \"107 5 PERITCHIPALAYAM MAIN ROAD\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"CAPS_Other_Details\": {            \"Time_with_Employer\": \"\",            \"Employment_Status\": \"\",            \"Number_of_Major_Credit_Card_Held\": \"\",            \"Marital_Status\": \"\",            \"Income\": \"\"          },          \"CAPS_Applicant_Additional_Address_Details\": {            \"Country_Code\": \"IB\",            \"RoadNoNameAreaLocality\": \"\",            \"State\": 33,            \"Landmark\": \"\",            \"BldgNoSocietyName\": \"VILLAGE DINDUGUL\",            \"FlatNoPlotNoHouseNo\": \"PERICHIPALAYAM SF NO 88 2 KOTTADURAI\",            \"City\": \"DINDIGUL\",            \"PINCode\": 624617          },          \"ReportNumber\": 1644477882780,          \"Finance_Purpose\": 99,          \"Subscriber_Name\": \"XXXXXXXXXX\"        },        \"CAPS_Summary\": {          \"CAPSLast30Days\": 0,          \"CAPSLast7Days\": 0,          \"CAPSLast180Days\": 0,          \"CAPSLast90Days\": 0        }      }    }  },  \"responseCode\": 200}";
//                        Log.consoleLog(ifr, "Mock Flag is Y ::");
//                    } else {
//                        try {
//                           // String ExperianResQuery = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME ='Experian_API' AND TRANSACTION_ID ='" + PID + "'";
//                           String ExperianResQuery=ConfProperty.getQueryScript("ExperianResQuery").replaceAll("#PID#", PID);
//                           
//                           List<List<String>> ExperianResponse = cf.mExecuteQuery(ifr, ExperianResQuery, "Execute query for fetching loan selected ");
//                            if (!ExperianResponse.isEmpty()) {
//
//                              String ExperianResponseStr = ExperianResponse.get(0).get(0);
//                                Log.consoleLog(ifr, "ExperianResponseStr From Table==>" + ExperianResponseStr);
//                                JSONObject ExperianResponseObj = (JSONObject) parser1.parse(ExperianResponseStr);
//                                responseBody = ExperianResponseObj.toString();
//                                Log.consoleLog(ifr, "Experian Response from table Parsed ::");
//                            }
//                        } catch (Exception e) {
//                            Log.consoleLog(ifr, "Exception in ExperianResQuery :" + e);
//                            JSONObject message = new JSONObject();
//                            message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
//                            return message.toString();
//                        }
//                    }

                responseBody = "{\"body\":{\"INProfileResponse\":{\"Header\":{\"SystemCode\":0,\"MessageText\":\"\",\"ReportDate\":20240205,\"ReportTime\":183314},\"UserMessage\":{\"UserMessageText\":\"Normal Response\"},\"CreditProfileHeader\":{\"Enquiry_Username\":\"cpu2canara_prod07\",\"ReportDate\":20240205,\"ReportTime\":183314,\"Version\":\"V2.4\",\"ReportNumber\":1707138193771,\"Subscriber\":\"\",\"Subscriber_Name\":\"Canara Bank\"},\"Current_Application\":{\"Current_Application_Details\":{\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"Current_Applicant_Details\":{\"Last_Name\":\"s\",\"First_Name\":\"AMARA PAVANKUMAR\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":1,\"IncomeTaxPan\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Passport_Number\":\"\",\"Passport_Issue_Date\":\"\",\"Passport_Expiration_Date\":\"\",\"Voter_s_Identity_Card\":\"\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"Ration_Card_Number\":\"\",\"Ration_Card_Issue_Date\":\"\",\"Ration_Card_Expiration_Date\":\"\",\"Universal_ID_Number\":\"\",\"Universal_ID_Issue_Date\":\"\",\"Universal_ID_Expiration_Date\":\"\",\"Date_Of_Birth_Applicant\":19890525,\"Telephone_Number_Applicant_1st\":\"\",\"Telephone_Extension\":\"\",\"Telephone_Type\":\"\",\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"Current_Other_Details\":{\"Income\":\"\",\"Marital_Status\":2,\"Employment_Status\":\"S\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":0},\"Current_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"},\"Current_Applicant_Additional_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"S\\/O SRINIVASARAO\",\"BldgNoSocietyName\":\"CHANDRAPADU POST\",\"RoadNoNameAreaLocality\":\"TELLABADU VIA PRAKASAM DT\",\"City\":\"ONGOLE\",\"Landmark\":\"\",\"State\":33,\"PINCode\":624617,\"Country_Code\":\"IB\"}}},\"CAIS_Account\":{\"CAIS_Summary\":{\"Credit_Account\":{\"CreditAccountTotal\":6,\"CreditAccountActive\":6,\"CreditAccountDefault\":0,\"CreditAccountClosed\":0,\"CADSuitFiledCurrentBalance\":0},\"Total_Outstanding_Balance\":{\"Outstanding_Balance_Secured\":4825631,\"Outstanding_Balance_Secured_Percentage\":99,\"Outstanding_Balance_UnSecured\":42529,\"Outstanding_Balance_UnSecured_Percentage\":1,\"Outstanding_Balance_All\":4868160}},\"CAIS_Account_DETAILS\":[{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C000115766\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20140721,\"Credit_Limit_Amount\":25000,\"Highest_Credit_or_Original_Loan_Amount\":\"\",\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"?00000000000000000000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20210531,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20170228,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2018,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2017,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2021,\"Month\":5,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4931,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":3,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":2510,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":2,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2021,\"Month\":1,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":1610,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":12,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"},{\"Year\":2020,\"Month\":11,\"Cash_Limit\":10000,\"Credit_Limit_Amount\":25000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST TELLABA\",\"Third_Line_Of_Address_non_normalized\":\"VIA ANDRAPRADESH\",\"City_non_normalized\":\"###\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9492537874},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889254001153,\"Portfolio_Type\":\"I\",\"Account_Type\":12,\"Open_Date\":20190121,\"Highest_Credit_or_Original_Loan_Amount\":200000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20240102,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":7,\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":10,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":90334,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":139710,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":117563,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":115619,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":93056,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":200000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":123065,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":[{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}]},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":2536667005808,\"Portfolio_Type\":\"M\",\"Account_Type\":2,\"Open_Date\":20190424,\"Highest_Credit_or_Original_Loan_Amount\":4500000,\"Terms_Duration\":360,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":5170000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":360,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20190430,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2019,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4704611,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4704642,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705175,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705017,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705367,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":4500000,\"Actual_Payment_Amount\":18367,\"EMI_Amount\":18367,\"Current_Balance\":4705026,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":889694000003,\"Portfolio_Type\":\"I\",\"Account_Type\":13,\"Open_Date\":20200716,\"Highest_Credit_or_Original_Loan_Amount\":75000,\"Terms_Duration\":84,\"Terms_Frequency\":\"M\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"00000000000000000S000000000000000000\",\"Special_Comment\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231227,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"P\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":88000,\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":6,\"Repayment_Tenure\":84,\"Promotional_Rate_Flag\":\"\",\"Income\":100001,\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20200731,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":6,\"Days_Past_Due\":\"\",\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2022,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2021,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2020,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":30686,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":31874,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":33059,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":34235,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":35407,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":\"\",\"Credit_Limit_Amount\":75000,\"Actual_Payment_Amount\":1253,\"EMI_Amount\":1253,\"Current_Balance\":36569,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVANKUMAR\",\"First_Name_Non_Normalized\":\"\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Voter_ID_Number\":\"BHM3068210\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"ONGOLE\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523263,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":4,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":919538272315,\"Telephone_Type\":0,\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Voter_ID_Number\":\"BHM3068210\",\"Voter_ID_Issue_Date\":\"\",\"Voter_ID_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"PAVANKUMARAMARA@GMAIL.COM\"}},{\"Identification_Number\":\"PVTXXXXXXXX\",\"Subscriber_Name\":\"XXXXXXXXXX\",\"Account_Number\":\"XXXXXXXXXXXX2879\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20220926,\"Credit_Limit_Amount\":340000,\"Highest_Credit_or_Original_Loan_Amount\":45111,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"0000000000000???????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\",\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231112,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":20231023,\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20221012,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":8,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":7,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":6,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":5,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":4,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":3,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":2,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2023,\"Month\":1,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"},{\"Year\":2022,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"?\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":11,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":31360,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":14929,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":4593,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":8,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9830,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":7,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":9043,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":6,\"Cash_Limit\":34000,\"Credit_Limit_Amount\":340000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":10429,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA\",\"First_Name_Non_Normalized\":\"PAVAN\",\"Middle_Name_1_Non_Normalized\":\"KUMAR\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":[{\"First_Line_Of_Address_non_normalized\":\"CANARA BANK 2ND FLOOR  NAVEENCOMPLEX MG\",\"Second_Line_Of_Address_non_normalized\":\"ROAD HEAD OFFICE  OPP 1MG MALL\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560002,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"27   3RD FLOOR GANESH ILLAM ATMANANDA CO\",\"Second_Line_Of_Address_non_normalized\":\"LONY  SULTANPALYA MAIN ROAD RT NAGAR  NE\",\"Third_Line_Of_Address_non_normalized\":\"AR NARAYANA APARTMENT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":29,\"ZIP_Postal_Code_non_normalized\":560032,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"},{\"First_Line_Of_Address_non_normalized\":\"3 108 CHIMAKURTHI CHANDRAPADU  PRAKASAM\",\"Second_Line_Of_Address_non_normalized\":\"\",\"Third_Line_Of_Address_non_normalized\":\"\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":523226,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":3,\"Residence_code_non_normalized\":\"\"}],\"CAIS_Holder_Phone_Details\":[{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"},{\"Telephone_Number\":2011111111,\"Telephone_Type\":3,\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}],\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"AMARAPAVANK@CANARABANK.IN\"}},{\"Identification_Number\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Account_Number\":\"C117024315\",\"Portfolio_Type\":\"R\",\"Account_Type\":10,\"Open_Date\":20230830,\"Credit_Limit_Amount\":100000,\"Highest_Credit_or_Original_Loan_Amount\":100000,\"Terms_Duration\":\"\",\"Terms_Frequency\":\"\",\"Scheduled_Monthly_Payment_Amount\":\"\",\"Account_Status\":11,\"Payment_Rating\":0,\"Payment_History_Profile\":\"000?????????????????????????????????\",\"Special_Comment\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0,\"Original_Charge_off_Amount\":\"\",\"Date_Reported\":20231231,\"Date_Of_First_Delinquency\":\"\",\"Date_Closed\":\"\",\"Date_of_Last_Payment\":\"\",\"SuitFiledWillfulDefaultWrittenOffStatus\":\"\",\"SuitFiled_WilfulDefault\":\"\",\"Written_off_Settled_Status\":\"\",\"Value_of_Credits_Last_Month\":\"\",\"Occupation_Code\":\"\",\"Settlement_Amount\":\"\",\"Value_of_Collateral\":\"\",\"Type_of_Collateral\":\"\",\"Written_Off_Amt_Total\":\"\",\"Written_Off_Amt_Principal\":\"\",\"Rate_of_Interest\":\"\",\"Repayment_Tenure\":0,\"Promotional_Rate_Flag\":\"\",\"Income\":\"\",\"Income_Indicator\":\"\",\"Income_Frequency_Indicator\":\"\",\"DefaultStatusDate\":\"\",\"LitigationStatusDate\":\"\",\"WriteOffStatusDate\":\"\",\"DateOfAddition\":20230930,\"CurrencyCode\":\"INR\",\"Subscriber_comments\":\"\",\"Consumer_comments\":\"\",\"AccountHoldertypeCode\":1,\"CAIS_Account_History\":[{\"Year\":2023,\"Month\":12,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":11,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":10,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"},{\"Year\":2023,\"Month\":9,\"Days_Past_Due\":0,\"Asset_Classification\":\"S\"}],\"Advanced_Account_History\":[{\"Year\":2023,\"Month\":12,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":6238,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":11,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":48588,\"Amount_Past_Due\":0},{\"Year\":2023,\"Month\":10,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":7337,\"Amount_Past_Due\":\"\"},{\"Year\":2023,\"Month\":9,\"Cash_Limit\":50000,\"Credit_Limit_Amount\":100000,\"Actual_Payment_Amount\":\"\",\"EMI_Amount\":\"\",\"Current_Balance\":0,\"Amount_Past_Due\":\"\"}],\"CAIS_Holder_Details\":{\"Surname_Non_Normalized\":\"AMARA PAVAN KUMAR\",\"First_Name_Non_Normalized\":\".\",\"Middle_Name_1_Non_Normalized\":\"\",\"Middle_Name_2_Non_Normalized\":\"\",\"Middle_Name_3_Non_Normalized\":\"\",\"Alias\":\"\",\"Gender_Code\":1,\"Income_TAX_PAN\":\"AXSPA5004F\",\"Date_of_birth\":19890525},\"CAIS_Holder_Address_Details\":{\"First_Line_Of_Address_non_normalized\":\"S\\/O SRINIVASARAO\",\"Second_Line_Of_Address_non_normalized\":\"CHANDRAPADU POST\",\"Third_Line_Of_Address_non_normalized\":\"TELLABADU VIA PRAKASAM DT\",\"City_non_normalized\":\"\",\"Fifth_Line_Of_Address_non_normalized\":\"\",\"State_non_normalized\":28,\"ZIP_Postal_Code_non_normalized\":560009,\"CountryCode_non_normalized\":\"IB\",\"Address_indicator_non_normalized\":2,\"Residence_code_non_normalized\":\"\"},\"CAIS_Holder_Phone_Details\":{\"Telephone_Number\":\"\",\"Telephone_Type\":1,\"Mobile_Telephone_Number\":9538272315},\"CAIS_Holder_ID_Details\":{\"Income_TAX_PAN\":\"AXSPA5004F\",\"PAN_Issue_Date\":\"\",\"PAN_Expiration_Date\":\"\",\"Driver_License_Number\":\"\",\"Driver_License_Issue_Date\":\"\",\"Driver_License_Expiration_Date\":\"\",\"EMailId\":\"\"}}]},\"Match_result\":{\"Exact_match\":\"Y\"},\"TotalCAPS_Summary\":{\"TotalCAPSLast7Days\":2,\"TotalCAPSLast30Days\":2,\"TotalCAPSLast90Days\":2,\"TotalCAPSLast180Days\":2},\"CAPS\":{\"CAPS_Summary\":{\"CAPSLast7Days\":2,\"CAPSLast30Days\":2,\"CAPSLast90Days\":2,\"CAPSLast180Days\":2},\"CAPS_Application_Details\":[{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":161727,\"ReportNumber\":1706611647587,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"},{\"Subscriber_code\":\"PUBCANAR03\",\"Subscriber_Name\":\"Canara Bank\",\"Date_of_Request\":20240130,\"ReportTime\":132619,\"ReportNumber\":1706601379101,\"Enquiry_Reason\":14,\"Finance_Purpose\":48,\"Amount_Financed\":0,\"Duration_Of_Agreement\":180,\"CAPS_Applicant_Details\":{\"Last_Name\":\"\",\"First_Name\":\"\",\"Middle_Name1\":\"\",\"Middle_Name2\":\"\",\"Middle_Name3\":\"\",\"Gender_Code\":\"\",\"Date_Of_Birth_Applicant\":\"\",\"Telephone_Type\":1,\"MobilePhoneNumber\":9538272315,\"EMailId\":\"\"},\"CAPS_Other_Details\":{\"Income\":\"\",\"Marital_Status\":\"\",\"Employment_Status\":\"\",\"Time_with_Employer\":\"\",\"Number_of_Major_Credit_Card_Held\":\"\"},\"CAPS_Applicant_Address_Details\":{\"FlatNoPlotNoHouseNo\":\"\",\"BldgNoSocietyName\":\"\",\"RoadNoNameAreaLocality\":\"\",\"City\":\"\",\"Landmark\":\"\",\"State\":\"\",\"PINCode\":\"\",\"Country_Code\":\"IB\"},\"CAPS_Applicant_Additional_Address_Details\":\"\"}]},\"NonCreditCAPS\":{\"NonCreditCAPS_Summary\":{\"NonCreditCAPSLast7Days\":0,\"NonCreditCAPSLast30Days\":0,\"NonCreditCAPSLast90Days\":0,\"NonCreditCAPSLast180Days\":0}},\"PSV\":{\"BFHL_Ex_HL\":{\"TN_of_BFHL_CAD_Ex_HL\":\"\",\"Tot_Val_of_BFHL_CAD\":\"\",\"MNT_SMR_BFHL_CAD\":\"\"},\"HL_CAD\":{\"TN_of_HL_CAD\":\"\",\"Tot_Val_of_HL_CAD\":\"\",\"MNT_SMR_HL_CAD\":\"\"},\"Telcos_CAD\":{\"TN_of_Telcos_CAD\":\"\",\"Tot_Val_of_Telcos_CAD\":\"\",\"MNT_SMR_Telcos_CAD\":\"\"},\"MF_CAD\":{\"TN_of_MF_CAD\":\"\",\"Tot_Val_of_MF_CAD\":\"\",\"MNT_SMR_MF_CAD\":\"\"},\"Retail_CAD\":{\"TN_of_Retail_CAD\":\"\",\"Tot_Val_of_Retail_CAD\":\"\",\"MNT_SMR_Retail_CAD\":\"\"},\"Total_CAD\":{\"TN_of_All_CAD\":\"\",\"Tot_Val_of_All_CAD\":\"\",\"MNT_SMR_CAD_All\":\"\"},\"BFHL_ACA_ExHL\":{\"TN_of_BFHL_ACA_Ex_HL\":\"\",\"Bal_BFHL_ACA_Ex_HL\":\"\",\"WCD_St_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_6_MNT_BFHL_ACA_Ex_HL\":\"\",\"WDS_Pr_7_12_MNT_BFHL_ACA_Ex_HL\":\"\",\"Age_of_Oldest_BFHL_ACA_Ex_HL\":\"\",\"HCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\",\"TCB_Per_Rev_Acc_BFHL_ACA_Ex_HL\":\"\"},\"HL_ACA\":{\"TN_of_HL_ACA\":\"\",\"Bal_HL_ACA\":\"\",\"WCD_St_HL_ACA\":\"\",\"WDS_Pr_6_MNT_HL_ACA\":\"\",\"WDS_Pr_7_12_MNT_HL_ACA\":\"\",\"Age_of_Oldest_HL_ACA\":\"\"},\"MF_ACA\":{\"TN_of_MF_ACA\":\"\",\"Total_Bal_MF_ACA\":\"\",\"WCD_St_MF_ACA\":\"\",\"WDS_Pr_6_MNT_MF_ACA\":\"\",\"WDS_Pr_7_12_MNT_MF_ACA\":\"\",\"Age_of_Oldest_MF_ACA\":\"\"},\"Telcos_ACA\":{\"TN_of_Telcos_ACA\":\"\",\"Total_Bal_Telcos_ACA\":\"\",\"WCD_St_Telcos_ACA\":\"\",\"WDS_Pr_6_MNT_Telcos_ACA\":\"\",\"WDS_Pr_7_12_MNT_Telcos_ACA\":\"\",\"Age_of_Oldest_Telcos_ACA\":\"\"},\"Retail_ACA\":{\"TN_of_Retail_ACA\":\"\",\"Total_Bal_Retail_ACA\":\"\",\"WCD_St_Retail_ACA\":\"\",\"WDS_Pr_6_MNT_Retail_ACA\":\"\",\"WDS_Pr_7_12_MNT_Retail_ACA\":\"\",\"Age_of_Oldest_Retail_ACA\":\"\",\"HCB_Lm_Per_Rev_Acc_Ret\":\"\",\"Tot_Cur_Bal_Lm_Per_Rev_Acc_Ret\":\"\"},\"Total_ACA\":{\"TN_of_All_ACA\":\"\",\"Bal_All_ACA_Ex_HL\":\"\",\"WCD_St_All_ACA\":\"\",\"WDS_Pr_6_MNT_All_ACA\":\"\",\"WDS_Pr_7_12_MNT_All_ACA\":\"\",\"Age_of_Oldest_All_ACA\":\"\"},\"BFHL_ICA_Ex_HL\":{\"TN_of_NDel_BFHL_InACA_Ex_HL\":\"\",\"TN_of_Del_BFHL_InACA_Ex_HL\":\"\"},\"HL_ICA\":{\"TN_of_NDel_HL_InACA\":\"\",\"TN_of_Del_HL_InACA\":\"\"},\"MF_ICA\":{\"TN_of_NDel_MF_InACA\":\"\",\"TN_of_Del_MF_InACA\":\"\"},\"Telcos_ICA\":{\"TN_of_NDel_Telcos_InACA\":\"\",\"TN_of_Del_Telcos_InACA\":\"\"},\"Retail_ICA\":{\"TN_of_NDel_Retail_InACA\":\"\",\"TN_of_Del_Retail_InACA\":\"\"},\"PSV_CAPS\":{\"BFHL_CAPS_Last_90_Days\":\"\",\"MF_CAPS_Last_90_Days\":\"\",\"Telcos_CAPS_Last_90_Days\":\"\",\"Retail_CAPS_Last_90_Days\":\"\"},\"Own_Company_Data\":{\"TN_of_OCom_CAD\":\"\",\"Tot_Val_of_OCom_CAD\":\"\",\"MNT_SMR_OCom_CAD\":\"\",\"TN_of_OCom_ACA\":\"\",\"Bal_OCom_ACA_Ex_HL\":\"\",\"Bal_OCom_ACA_HL_Only\":\"\",\"WCD_St_OCom_ACA\":\"\",\"HCB_Lm_Per_Rev_OCom_ACA\":\"\",\"TN_of_NDel_OCom_InACA\":\"\",\"TN_of_Del_OCom_InACA\":\"\",\"TN_of_OCom_CAPS_Last_90_days\":\"\"},\"Oth_CB_Information\":{\"Any_Rel_CB_Data_Dis_Y_N\":\"\",\"Oth_Rel_CB_DFC_Pos_Mat_Y_N\":\"\"},\"Indian_Market_Specific_Var\":{\"TN_of_CAD_classed_as_SFWDWO\":\"\",\"MNT_SMR_CAD_classed_as_SFWDWO\":\"\",\"Num_of_CAD_SFWDWO_Last_24_MNT\":\"\",\"Tot_Cur_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Live_UAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_SAcc\":\"\",\"Tot_Cur_Bal_Max_Bal_Live_UAcc\":\"\"}},\"SCORE\":{\"BureauScore\":829,\"BureauScoreConfidLevel\":\"H\",\"CreditRating\":\"\"}}}}";
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
                // String queryData = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId  FROM LOS_MASTER_BORROWER b \n"
                // + "inner JOIN LOS_NL_BASIC_INFO c  ON b.borrowercode = c.ApplicantType WHERE c.PID ='" + PID + "' AND  c.ApplicantType ='B'";
                String queryData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", PID);

                List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                String party_type = data.get(0).get(0);
                Log.consoleLog(ifr, "Party Type==>" + party_type);

                ifr.setStyle("QNL_AL_LIAB_VAL_ApplicantType", "disable", "true");
                double TotalEmiOblig = 0.00;
                if (!(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account").equalsIgnoreCase(""))) {
                    JSONObject CAIS_Account = (JSONObject) parser1.parse(cf.getJsonValue(INProfileResponseDataJSONObj, "CAIS_Account"));
                    if (!(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS").equalsIgnoreCase(""))) {
                        JSONArray CAIS_Account_DETAILS = (JSONArray) parser1.parse(cf.getJsonValue(CAIS_Account, "CAIS_Account_DETAILS"));
                        for (int i = 0; i < CAIS_Account_DETAILS.size(); i++) {
                            JSONObject CAIS_Account_DETAILSObj = (JSONObject) CAIS_Account_DETAILS.get(i);
                            String subscriberName = CAIS_Account_DETAILSObj.get("Subscriber_Name").toString();
                            String openDate = CAIS_Account_DETAILSObj.get("Open_Date").toString();
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
                                Timestamp timestamp = new Timestamp(date.getTime());
                                Log.consoleLog(ifr, "formatted timestamp::" + timestamp);

                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                loanDate = dateFormatter.format(timestamp);
                                // openDate=dateFormatter.format(timestamp);
                                Log.consoleLog(ifr, "formatted loanDate::" + loanDate);
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
                            //modified by ishwarya on 15-07-2024
                            String Query1 = "SELECT concat(b.borrowertype,concat('-',c.fullname)),c.insertionOrderId FROM LOS_MASTER_BORROWER b "
                                    + "inner JOIN LOS_NL_BASIC_INFO c ON b.borrowercode = c.ApplicantType "
                                    + "WHERE c.PID = '" + PID + "' "
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
                                    obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                                    obj.put("QNL_AL_LIAB_VAL_Bank", subscriberName);
                                    obj.put("QNL_AL_LIAB_VAL_SanctionDT", loanDate);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", Credit_Limit_Amount);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", loanOutstanding);
                                    obj.put("QNL_AL_LIAB_VAL_Overdue", overdue.equalsIgnoreCase("") ? "0.00" : overdue);
                                    obj.put("QNL_AL_LIAB_VAL_ApplicantType", brdata);
                                    obj.put("QNL_AL_LIAB_VAL_LoanType", Account_Type);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", AccountNumber);
                                    obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                                    overdue = overdue.equalsIgnoreCase("") ? "0.00" : overdue;
                                    obj.put("QNL_AL_LIAB_VAL_Overdue", overdue);
                                    if (acc_status >= 12 && acc_status <= 17) {
                                        if (emi_amt.equalsIgnoreCase("") || emi_amt.equalsIgnoreCase(null)) {
                                            obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
                                            Log.consoleLog(ifr, "EMI is null");

                                        } else {
                                            obj.put("QNL_AL_LIAB_VAL_EMIAmt", emi_amt);
                                            Log.consoleLog(ifr, "EMI is present");
                                            TotalEmiOblig = TotalEmiOblig + Double.parseDouble(emi_amt);
                                        }

                                    } else {
                                        obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
                                        Log.consoleLog(ifr, "EMI not taken into account");
                                    }
                                    Log.consoleLog(ifr, "JSON obj RESULT::" + obj);
                                    jsonarr.add(obj);

                                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr.size());
                                    ((IFormAPIHandler) ifr).addDataToGrid("ALV_AL_LIAB_VAL", jsonarr, true);
                                    Log.consoleLog(ifr, "test1==>" + jsonarr);
                                    BorrowerAdded = true;
                                } else if (!coBorrowerAdded) {
                                    obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                                    obj.put("QNL_AL_LIAB_VAL_Bank", subscriberName);
                                    obj.put("QNL_AL_LIAB_VAL_SanctionDT", loanDate);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", Credit_Limit_Amount);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", loanOutstanding);
                                    obj.put("QNL_AL_LIAB_VAL_Overdue", overdue.equalsIgnoreCase("") ? "0.00" : overdue);
                                    obj.put("QNL_AL_LIAB_VAL_ApplicantType", brdata);
                                    obj.put("QNL_AL_LIAB_VAL_LoanType", Account_Type);
                                    obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", AccountNumber);
                                    obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
                                    overdue = overdue.equalsIgnoreCase("") ? "0.00" : overdue;
                                    obj.put("QNL_AL_LIAB_VAL_Overdue", overdue);
                                    if (acc_status >= 12 && acc_status <= 17) {
                                        if (emi_amt.equalsIgnoreCase("") || emi_amt.equalsIgnoreCase(null)) {
                                            obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
                                            Log.consoleLog(ifr, "EMI is null");

                                        } else {
                                            obj.put("QNL_AL_LIAB_VAL_EMIAmt", emi_amt);
                                            Log.consoleLog(ifr, "EMI is present");
                                            TotalEmiOblig = TotalEmiOblig + Double.parseDouble(emi_amt);
                                        }

                                    } else {
                                        obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
                                        Log.consoleLog(ifr, "EMI not taken into account");
                                    }
                                    Log.consoleLog(ifr, "JSON obj RESULT::" + obj);
                                    jsonarr.add(obj);

                                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);
                                    Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr.size());
                                    ((IFormAPIHandler) ifr).addDataToGrid("ALV_AL_LIAB_VAL", jsonarr, true);
                                    Log.consoleLog(ifr, "test1==>" + jsonarr);
                                    coBorrowerAdded = true;
                                }
                            }
                        }
                    }
                } else {
                    Log.consoleLog(ifr, "CAIS_Account is null in response autopopulate_liability");
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "CAIS_Account is Null. Technical glitch in getting Liabilities!"));
                    return message.toString();
                }
                //ifr.setValue("FE_Obligations", String.valueOf(TotalEmiOblig));
                Log.consoleLog(ifr, "TotalEmiOblig::" + TotalEmiOblig);

                //ifr.addDataToGrid("ALV_AL_LIAB_VAL", jsonarr);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception autopopulate_liability" + e);
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
                return message.toString();
            }
        }
        return "";
    }*/
    public String autopopulateFinancialInfoIncomeVL(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside autopopulateFinancialInfoIncomeVL==>");
        String[] FieldVisibleFalse = new String[]{"QNL_AL_LIAB_VAL_Loan_Acc_No"};

        for (int i = 0; i < FieldVisibleFalse.length; i++) {
            ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
        }

        String[] FieldVisibleMandatoryDisable = new String[]{"QA_FI_PI_MINCOME_CustomerType",
            "QA_FI_PI_MINCOME_ConsiderEligibilty", "QA_FI_PI_MINCOME_IncSource",
            "QA_FI_PI_MINCOME_GrossAmt",
            "QA_FI_PI_MINCOME_NetAmount", "QA_FI_PI_MINCOME_Deduction"};
//modified by ishwarya on 26-07-2024
        String[] FieldInVisible = new String[]{"Income_BTN_SaveAndClose",
            "Income_BTN_SaveAndNext", "QA_FI_PI_MINCOME_FinancialYearlatest", "QA_FI_PI_MINCOME_occCombo"};

        for (int i = 0; i < FieldVisibleMandatoryDisable.length; i++) {
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "visible", "true");
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "disable", "true");
            ifr.setStyle(FieldVisibleMandatoryDisable[i], "mandatory", "false");
            ifr.setStyle(FieldInVisible[i], "visible", "false");
        }
        Log.consoleLog(ifr, " Vechicle Loan FinancialInfoIncome Details fields visible hide and mandatory check end");

        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
        String queryL = ConfProperty.getQueryScript("IfPortalQuery").replaceAll("#PID#", ProcessInstanceId);

        try {
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = "";
            if (loanSelected.size() > 0) {
                loan_selected = loanSelected.get(0).get(1);
            }
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
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
            int IncomeGridSize = ifr.getDataFromGrid("ALV_PL_MIncome").size();
            if (IncomeGridSize <= 0) {
                ((IFormAPIHandler) ifr).addDataToGrid("ALV_PL_MIncome", jsonarr, true);
                Log.consoleLog(ifr, "Income from portal Added==>");
            }
            /* if (checkQueryData.size() == 0) {
             ifr.addDataToGrid("ALV_PL_MIncome", jsonarr);
             Log.consoleLog(ifr, "Income from portal Added==>");
             }*/

        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception IN autopopulateFinancialInfoIncome " + ex);
        }
        return "";
    }

    //modified by ishwarya for data population in final elig grid on 25/06/2024
    /*   public String autopopulateFinalEligibilityVL(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        Log.consoleLog(ifr, "Inside VL Final Eligibility");

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
        if (EligibityCount.isEmpty()) {
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
                    cibiloblig = TotalEmi.get(0).get(0);
                    Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
                } else {
                    cibiloblig = "0.00";
                }
                Log.consoleLog(ifr, "cibiloblig TotalEmi::" + cibiloblig);
                String Purpose = null;
                String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
                if (PurposePortal.size() > 0) {
                    Purpose = PurposePortal.get(0).get(0);
                }

                String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String loanTenure = null;
                //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
                String tenureData_Query = ConfProperty.getQueryScript("GetMaxTenure").replaceAll("#schemeID#", schemeID);
                List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
                if (list1.size() > 0) {
                    loanTenure = list1.get(0).get(0);
                }
                Log.consoleLog(ifr, "loanTenure : " + loanTenure);
                String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
                Log.consoleLog(ifr, "roiID:" + roiID);
                String loanROI = null;
                //String roiData_Query = "select totalroi from los_m_roi where roiid='" + roiID + "'";
                String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
                List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
                if (list2.size() > 0) {
                    loanROI = list2.get(0).get(0);
                }
                Log.consoleLog(ifr, "roi : " + loanROI);
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
                    Logger.getLogger(AcceleratorActivityManagerCode.class.getName()).log(Level.SEVERE, null, ex);
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

                double finaleligibilityDouble = Math.floor(Double.parseDouble(finaleligibilityBG) / 1000) * 1000;
                String finaleligibility = String.valueOf(Math.round(finaleligibilityDouble));
                Log.consoleLog(ifr, "After rounded finaleligibility : " + finaleligibility);

                String prodCode = "VL";
                String subProdCode = "SUPVL";
                String loanoffer = "0";
                HashMap<String, String> loandata = new HashMap<>(); //changes by Ishwarya
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
                    // Logger.getLogger(AcceleratorActivityManagerCode.class.getName()).log(Level.SEVERE, null, ex);
                }
                double finaleligibilityDoubled = Math.floor(Double.parseDouble(finaleligibilityCommon) / 1000) * 1000;
                finaleligibilityCommon = String.valueOf(Math.round(finaleligibilityDoubled));
                Log.consoleLog(ifr, "After rounded finaleligibilityCommon : " + finaleligibilityCommon);
                JSONObject obj = new JSONObject();
                JSONArray jsonarr = new JSONArray();
                obj.put("QNL_LA_FINALELIGIBILITY_AverageGrossIncome", grosssalaryip);
                obj.put("QNL_LA_FINALELIGIBILITY_AverageDeductions", deductionsalary);
                obj.put("QNL_LA_FINALELIGIBILITY_Obligations", cbCibilOblig);
                obj.put("QNL_LA_FINALELIGIBILITY_NetIncome", netIncome);
                obj.put("QNL_LA_FINALELIGIBILITY_Tenure", ftTenure);
                obj.put("QNL_LA_FINALELIGIBILITY_ROI", ftRoiBG);
                obj.put("QNL_LA_FINALELIGIBILITY_NetTakeHomeSalaryasperpolicy", netsalary);
                obj.put("QNL_LA_FINALELIGIBILITY_Loanamountasperpolicy", loanAmount);
                obj.put("QNL_LA_FINALELIGIBILITY_Multipliergrossincomepolicy", sixtimesgrosssal);
                obj.put("QNL_LA_FINALELIGIBILITY_ApprovedLoanAmount", prodspeccapping);

                obj.put("QNL_LA_FINALELIGIBILITY_Eligibileloanamount", finaleligibilityCommon);
                obj.put("QNL_LA_FINALELIGIBILITY_LoanAmountrequested", RequestedLoanAmount);

                jsonarr.add(obj);
                Log.consoleLog(ifr, "JSONARRAY RESULT::" + jsonarr);

                ifr.addDataToGrid("ALV_FINAL_ELIGIBILITY", jsonarr);
            } catch (NumberFormatException ex) {
                Log.consoleLog(ifr, "Exception IN OnLoadFinalEligibility " + ex);
            }
        }
        return "";
    }
     */
    //modified by ishwarya for data population in final elig grid on 29/07/2024
    public String autopopulateFinalEligibilityVL(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

        Log.consoleLog(ifr, "Inside VL Final Eligibility");

        String RequestedLoanAmount = "";
        String LoanAMTTenureQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> LoanAmountList = cf.mExecuteQuery(ifr, LoanAMTTenureQuery, "Execute query for fetching Slider Loan amount,Tenure data from portal in principal");
        if (LoanAmountList.size() > 0) {
            RequestedLoanAmount = LoanAmountList.get(0).get(1);
            Log.consoleLog(ifr, "RequestedLoanAmount :: " + RequestedLoanAmount);
        }
        ifr.setValue("FE_Requested_Loan_Amount", RequestedLoanAmount);
        ifr.setStyle("FE_Requested_Loan_Amount", "disable", "true");

        String EligibityQuery = ConfProperty.getQueryScript("CheckFinalEligibilityBO").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> EligibityCount = cf.mExecuteQuery(ifr, EligibityQuery, "Execute query for fetching customer data");
        Log.consoleLog(ifr, "EligibityCount ::" + EligibityCount.size());
        try {
            String f_key = bpcm.Fkey(ifr, "B");
            String IncomeQuery = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoVL").replaceAll("#f_key#", f_key);
            List<List<String>> IncomeDtsPortal = cf.mExecuteQuery(ifr, IncomeQuery, "Execute query for fetching income data from portal");
            String OccupationType = IncomeDtsPortal.get(0).get(0);
            Log.consoleLog(ifr, "OccupationType ::" + OccupationType);

            String TotalEmiQuery = ConfProperty.getQueryScript("FinancialInfoLiabilityEMI").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> TotalEmi = cf.mExecuteQuery(ifr, TotalEmiQuery, "FinancialInfoLiabilityEMI");

            String cibiloblig = "";
            if (TotalEmi.size() > 0) {
                cibiloblig = TotalEmi.get(0).get(1);
                Log.consoleLog(ifr, "cibiloblig FinancialInfoLiabilityEMI::" + cibiloblig);
            } else {
                cibiloblig = "0.00";
            }
            Log.consoleLog(ifr, "cibiloblig FinancialInfoLiabilityEMI::" + cibiloblig);

            String netIncomeQ = ConfProperty.getQueryScript("NetIncomeDeduction").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> netIncome = cf.mExecuteQuery(ifr, netIncomeQ, "NetIncomeDeduction");

            String netIncomegross = "";
            String netIncomedeductions = "";
            double netIncomeGross = 0.0;
            double netIncomeDeductions = 0.0;
            if (!netIncome.isEmpty()) {
                for (int i = 0; i < netIncome.size(); i++) {
                    Log.consoleLog(ifr, "TotalDeductions NetIncomeDeduction::" + netIncome.size());
                    netIncomegross = netIncome.get(i).get(0);
                    Log.consoleLog(ifr, "netIncomegross ::" + netIncome.get(i).get(0));
                    netIncomeGross = netIncomeGross + Double.parseDouble(netIncomegross);
                    netIncomedeductions = netIncome.get(i).get(1);
                    Log.consoleLog(ifr, "netIncomegross ::" + netIncome.get(i).get(1));
                    netIncomeDeductions = netIncomeDeductions + Double.parseDouble(netIncomedeductions);
                }
                Log.consoleLog(ifr, "netIncomeGross ::" + netIncomeGross);
                Log.consoleLog(ifr, "netIncomeDeductions ::" + netIncomeDeductions);
            }
            String TotalDeductionsQ = ConfProperty.getQueryScript("FinancialInfoDeduction").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> TotalDeductions = cf.mExecuteQuery(ifr, TotalDeductionsQ, "FinancialInfoDeduction");

            String deductionB = "";
            double deductions = 0.0;
            if (!TotalDeductions.isEmpty()) {
                for (int i = 0; i < TotalDeductions.size(); i++) {
                    Log.consoleLog(ifr, "TotalDeductions FinancialInfoDeduction::" + TotalDeductions.size());
                    deductionB = TotalDeductions.get(i).get(0);
                    Log.consoleLog(ifr, "after deductionB ::" + TotalDeductions.get(i).get(0));
                    deductions = deductions + Double.parseDouble(deductionB);
                }

            }
            Log.consoleLog(ifr, "deductions before adding dedction from form ::" + deductions);
            double deduction = netIncomeDeductions + deductions;
            Log.consoleLog(ifr, "deductions after adding dedction from form ::" + deduction);
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }

            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String loanROI = "";
            String loanTenure = "";
            String reqLoanAmt = "";
            String roiData_Query = "select EFFECTIVEROI from los_nl_proposed_facility where PID='" + ProcessInstanceId + "'";
            List<List<String>> list1 = cf.mExecuteQuery(ifr, roiData_Query, "tenureData_Query FROM PORTAL:");
            if (list1.size() > 0) {
                loanROI = list1.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanROI : " + loanROI);
            String tenureData_Query = "SELECT REQLOANAMT,TENURE FROM LOS_NL_PROPOSED_FACILITY where PID ='" + ProcessInstanceId + "'";
            List<List<String>> list2 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query FROM PORTAL:");
            if (list2.size() > 0) {
                loanTenure = list2.get(0).get(1);
                reqLoanAmt = list2.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            Log.consoleLog(ifr, "reqLoanAmt : " + reqLoanAmt);
            String Prodcapping = null;
            String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
            if (ProdcappingList.size() > 0) {
                Prodcapping = ProdcappingList.get(0).get(0);
            }
            Log.consoleLog(ifr, "Prodcapping : " + Prodcapping);

            String finalLTVAmount = null;
            String onRoadPrice = null;
            String finalLTV_Query = ConfProperty.getQueryScript("LTVAmount").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> finalLTVList = cf.mExecuteQuery(ifr, finalLTV_Query, "finalLTV_Query:");
            if (finalLTVList.size() > 0) {
                finalLTVAmount = finalLTVList.get(0).get(0);
                onRoadPrice = finalLTVList.get(0).get(1);
            }
            Log.consoleLog(ifr, "finalLTVAmount : " + finalLTVAmount);
            Log.consoleLog(ifr, "onRoadPrice : " + onRoadPrice);

            HashMap hm = new HashMap();
            hm.put("grosssalary", String.valueOf(netIncomeGross));
            hm.put("deductionmonth", String.valueOf(deduction));
            hm.put("cibiloblig", String.valueOf(cibiloblig));
            hm.put("tenure", String.valueOf(loanTenure));
            hm.put("roi", String.valueOf(loanROI));
            hm.put("loancap", String.valueOf(Prodcapping));
            hm.put("finalLTVAmount", finalLTVAmount);
            hm.put("reqLoanAmt", reqLoanAmt);
            hm.put("Purpose", Purpose);
            hm.put("OccupationType", OccupationType);
            hm.put("onRoadPrice", onRoadPrice);

            String EligibilityDatas = vlpc.getAmountForEligibilityCheckVL(ifr, hm);
            Log.consoleLog(ifr, "EligibilityDatas : " + EligibilityDatas);
            if (EligibilityDatas.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            } else if (EligibilityDatas.contains("showMessage")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "F_InPrincipleEligibility", "error", "Deduction should be less than Gross Income."));
                message.put("eflag", "false");
                return message.toString();
            }

        } catch (NumberFormatException ex) {
            Log.consoleLog(ifr, "Exception IN OnLoadFinalEligibility " + ex);
        }
        return "";
    }

    public String autoPopulateBureauVL(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside PopulateBureauScoreVL==>");

        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceIdVL==>" + ProcessInstanceId);

            // String queryL = "select LOAN_SELECTED from los_ext_table where PID='" + ProcessInstanceId + "'";
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String[] FieldVisibleFalse = new String[]{"QNL_CB_Details_Remarks", "QNL_CB_Details_Matching_Records", "QNL_CB_Details_Suit", "QNL_CB_Details_Report_Enquiry_Date", "QNL_CB_Details_PersonalScore", "QNL_CB_Details_UserRemarks", "QNL_CB_Details_Reportdate"};
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

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In PopulateBureauVL:" + e);
        }
        return "";
    }

    public String mGenerateDoc(IFormReference ifr) {

        try {
            objcm.generatedoc(ifr, "KFS", "VL");
            objcm.generatedoc(ifr, "LoanAggrement", "VL");
            objcm.generatedoc(ifr, "SanctionLetter", "VL");
            objcm.generatedoc(ifr, "ProcessNote", "VL");
            objcm.generatedoc(ifr, "LoanApplication", "VL");
            objcm.generatedoc(ifr, "RepaymentLetter", "VL");
            return RLOS_Constants.SUCCESS;
        } catch (Exception e) {
            Log.consoleLog(ifr, "mGenerateDocVL" + e);
            Log.errorLog(ifr, "mGenerateDocVL" + e);
        }
        return RLOS_Constants.ERROR;
    }

    public void autoPopulateNeslStatus(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            Log.consoleLog(ifr, "Inside autoPopulateNeslStatusVL::");

            String NeslStatus = "";
            String NeslMode = "";
            Log.consoleLog(ifr, "BEFORE NESL STATUS QUERY VL::");
            String NeslStatusQuery = ConfProperty.getQueryScript("getNeslStatusQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> NeslStatusPortal = cf.mExecuteQuery(ifr, NeslStatusQuery, "Execute query for fetching Purpose data from portal");
            if (NeslStatusPortal.size() > 0) {
                NeslStatus = NeslStatusPortal.get(0).get(0);
                NeslMode = NeslStatusPortal.get(0).get(1);
            }
            Log.consoleLog(ifr, "AFTER NESL STATUS QUERY VL::");
            String neslStatusTableQuery = "select PROCESSINSTANCEID from LOS_INTEGRATION_NESL_STATUS where PROCESSINSTANCEID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "neslStatusTableQuery ::" + neslStatusTableQuery);
            List<List<String>> neslStatusTableQueryData = ifr.getDataFromDB(neslStatusTableQuery);
            if (!neslStatusTableQueryData.isEmpty()) {
                String queryData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                String party_type = data.get(0).get(0);
                Log.consoleLog(ifr, "Party Type==>" + party_type);
                String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTNESL").replaceAll("#PID#", ProcessInstanceId);
                Log.consoleLog(ifr, "NESLQuery query::" + IndexQuery);
                List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
                String count = dataResult.get(0).get(0);
                Log.consoleLog(ifr, "count::" + count);
                if (Integer.parseInt(count) == 0) {
                    String dataSavingINNeslGridQuery = ConfProperty.getQueryScript("insertNeslStatusQueryVL").
                            replaceAll("#ProcessInstanceId#", ProcessInstanceId)
                            .replaceAll("#InsertionOrderID#", "S_LOS_NL_ESIGN_STATUS.nextval").
                            replaceAll("#borrowerName#", party_type).
                            replaceAll("#NeslStatus#", NeslStatus).replaceAll("#NeslMode#", NeslMode);
                    Log.consoleLog(ifr, "insert dataSavingINNeslGridQuery for consent" + dataSavingINNeslGridQuery);
                    ifr.saveDataInDB(dataSavingINNeslGridQuery);
                } else {
                    String dataSavingINNeslGridQuery = ConfProperty.getQueryScript("updateNeslStatusQueryVL").
                            replaceAll("#ProcessInstanceId#", ProcessInstanceId)
                            .replaceAll("#InsertionOrderID#", "S_LOS_NL_ESIGN_STATUS.nextval").
                            replaceAll("#borrowerName#", party_type).
                            replaceAll("#NeslStatus#", NeslStatus).replaceAll("#NeslMode#", NeslMode);
                    Log.consoleLog(ifr, "insert dataSavingINNeslGridQuery for consent" + dataSavingINNeslGridQuery);
                    ifr.saveDataInDB(dataSavingINNeslGridQuery);
                }
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in  autoPopulateNeslStatusVL::" + e);
        }
    }

    public String mDisbursementCheckerApiCall(IFormReference ifr, String control, String event, String value) {
        JSONObject message = new JSONObject();
        try {
            Log.consoleLog(ifr, "inside mDisbursementCheckerApiCall VL::");
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            String neslStatus = "";
            int size = ifr.getDataFromGrid("ALV_NESL_STATUS").size();
            if (size > 0) {
                Log.consoleLog(ifr, "Inside Nesl Ststus Grid: ");
                neslStatus = ifr.getTableCellValue("ALV_NESL_STATUS", 0, 2);
                Log.consoleLog(ifr, "neslStatus: " + neslStatus);
            }
            String NeslStatus = "";
            String NeslStatusQuery = ConfProperty.getQueryScript("getNeslStatusQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> NeslStatusPortal = cf.mExecuteQuery(ifr, NeslStatusQuery, "Execute query for fetching Purpose data from portal");
            if (NeslStatusPortal.size() > 0) {
                NeslStatus = NeslStatusPortal.get(0).get(0);
            }
            if (neslStatus.equalsIgnoreCase("Success") || NeslStatus.equalsIgnoreCase("Success")) {
                Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");

                String LoanDisbStatus = createVLLoanDisburse(ifr);//Added by Ahmed on 01-07-2024 for VL Loan Disb
                //  String LoanDisbStatus = CBSFinalScreenValidation(ifr, ProcessInstanceId);
                if (LoanDisbStatus.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
            } else {
                Log.consoleLog(ifr, "Nesl Status Pending:::");
                message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "Nesl complete status is pending"));
                message.put("eflag", "false");
                return message.toString();
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception in  mDisbursementCheckerApiCallVL::" + e);
        }
        return "";
    }

    public String OnChangePerfiosDataFEVL(IFormReference ifr, String Control, String Event, String JSdata) {
        Log.consoleLog(ifr, "inside OnChangePerfiosDataFEVL==>");

        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String GrossSalary = ifr.getValue("FE_Gross_Monthly_Salary").toString();
            Log.consoleLog(ifr, "GrossSalaryVL::" + GrossSalary);
            if (GrossSalary.equalsIgnoreCase("") || GrossSalary.equalsIgnoreCase("0") || GrossSalary.isEmpty()) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Gross Salary Amount!"));
                return message.toString();
            }

            String DeductionMonthly = ifr.getValue("FE_Deduction_From_Salary").toString();
            Log.consoleLog(ifr, "DeductionMonthlyVL::" + DeductionMonthly);
            if (DeductionMonthly.equalsIgnoreCase("") || DeductionMonthly.isEmpty()) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Deduction Monthly Amount!"));
                return message.toString();
            }
            String cibiloblig = ifr.getValue("FE_Obligations").toString();
            Log.consoleLog(ifr, "cibilobligVL::" + cibiloblig);
            if (cibiloblig.equalsIgnoreCase("") || cibiloblig.isEmpty()) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "", "error", "Please enter the valid Obiligation Amount!"));
                return message.toString();
            }
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeIDVL:" + schemeID);
            String loanTenure = null;
            //String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where scheme_id='" + schemeID + "'";
            String tenureData_Query = ConfProperty.getQueryScript("GetMaxTenure").replaceAll("#schemeID#", schemeID);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
            if (list1.size() > 0) {
                loanTenure = list1.get(0).get(0);
            }
            Log.consoleLog(ifr, "loanTenureVL : " + loanTenure);
            String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
            Log.consoleLog(ifr, "roiIDVL:" + roiID);
            String loanROI = null;
            //String roiData_Query = "select totalroi from los_m_roi where roiid='" + roiID + "'";
            String roiData_Query = ConfProperty.getQueryScript("GetTotalROI").replaceAll("#roiID#", roiID);
            List<List<String>> list2 = cf.mExecuteQuery(ifr, roiData_Query, "roiData_Query:");
            if (list2.size() > 0) {
                loanROI = list2.get(0).get(0);
            }
            Log.consoleLog(ifr, "roiVL : " + loanROI);
            String Prodcapping = null;
            String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#", schemeID);
            List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
            if (ProdcappingList.size() > 0) {
                Prodcapping = ProdcappingList.get(0).get(0);
            }
            Log.consoleLog(ifr, "ProdcappingVL : " + Prodcapping);

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
            Log.consoleLog(ifr, "EligibilityDatasVL : " + EligibilityDatas);
            try {
                EligibilityDataObj = (JSONObject) parser.parse(EligibilityDatas);

            } catch (ParseException ex) {
                Logger.getLogger(AcceleratorActivityManagerCode.class.getName()).log(Level.SEVERE, null, ex);
            }
            Log.consoleLog(ifr, "EligibilityDataObjVL : " + EligibilityDataObj);

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

            Log.consoleLog(ifr, "Before EligibilityDataObjVL : ");
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

            Log.consoleLog(ifr, "After rounded finaleligibilityVL : " + finaleligibility);
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
            ifr.setValue("FE_Eligibilie_Loan_Amount", finaleligibility);
            Log.consoleLog(ifr, "After EligibilityDataObj Data setted VL : ");
        } catch (NumberFormatException ne) {
            Log.consoleLog(ifr, "Exception In OnChangePerfiosDataFEVL:" + ne);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In OnChangePerfiosDataFEVL:" + e);
        }

        return "";
    }

    public String mEmailSmscheck(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mEmailSmscheck");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String decision = ifr.getValue("DecisionValue").toString();
            Log.consoleLog(ifr, "decision::" + decision);
            String activityName = ifr.getActivityName();
            if (activityName.equalsIgnoreCase("Branch Maker")) {
                if (decision.equalsIgnoreCase("SM") || decision.equalsIgnoreCase("R")) {

                    //Added by Ahmed on 27-05-2024 using DLPCommonObjects instead of directly calling
                    String bodyParams = "";
                    String subjectParams = "";
                    String fileName = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "VL", "4", bodyParams, subjectParams, fileName, fileContent);

//                    Email em = new Email();
//                    SMS sms = new SMS();
//                    String emailId = pcm.getCurrentEmailId(ifr, "VL", "B");
//                    String mobileNumber = pcm.getMobileNumber(ifr);
//                    em.sendEmail(ifr, ProcessInstanceId, emailId, "", "VL", "RETAIL", "4");
//                    sms.sendSMS(ifr, ProcessInstanceId, mobileNumber, "", "VL", "RETAIL", "4");
                }
            } else {
                if (decision.equalsIgnoreCase("R")) {
//                    Email em = new Email();
//                    SMS sms = new SMS();
//                    String emailId = pcm.getCurrentEmailId(ifr, "VL", "B");
//                    String mobileNumber = pcm.getMobileNumber(ifr);
//                    em.sendEmail(ifr, ProcessInstanceId, emailId, "", "VL", "RETAIL", "4");
//                    sms.sendSMS(ifr, ProcessInstanceId, mobileNumber, "", "VL", "RETAIL", "4");

                    //Added by Ahmed on 27-05-2024 using DLPCommonObjects instead of directly calling
                    String bodyParams = "";
                    String subjectParams = "";
                    String fileName = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                    pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "VL", "4", bodyParams, subjectParams, fileName, fileContent);

                }
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception mEmailSmscheck : " + e);
        }
        return RLOS_Constants.ERROR;
    }

    public String mCalculateFeeandChargesVL(IFormReference ifr, String control, String event, String value) {

        try {
            Log.consoleLog(ifr, "inside  mCalculateFeeandChargesVL");

            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInsanceId = Data.getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript("getMobNumFromWIREFTable").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "MOBILENUMBER Query : " + query);
            List list = ifr.getDataFromDB(query);
            String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);

            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery,
                    "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeIDVL:" + schemeID);

            List<List<String>> loanAmount = null;
            String data = ConfProperty.getQueryScript("getRecLoanAmtQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            Log.consoleLog(ifr, "loanAmount queryVL : " + data);
            loanAmount = ifr.getDataFromDB(data);
            String amount = loanAmount.get(0).get(0);
            Log.consoleLog(ifr, "loanAmount VL: " + amount);

            List<List<String>> loanTenure = null;
            String tenuredata = ConfProperty.getQueryScript("getmaxTenureData").replaceAll("#schemeID#", schemeID);
            Log.consoleLog(ifr, "tenuredata queryVL : " + data);
            loanTenure = ifr.getDataFromDB(tenuredata);
            String tenure = loanTenure.get(0).get(0);
            Log.consoleLog(ifr, "tenure VL: " + amount);

            String roiID = pcm.mGetRoiIDVL(ifr, schemeID);
            Log.consoleLog(ifr, "roiIDVL:" + schemeID);

            List<List<String>> roiData = null;
            String roidataQuery = ConfProperty.getQueryScript("getTotalROIData").replaceAll("#roiID#", roiID);
            Log.consoleLog(ifr, "roidataQuery queryVL : " + roidataQuery);
            roiData = ifr.getDataFromDB(roidataQuery);
            String roi = roiData.get(0).get(0);
            Log.consoleLog(ifr, "roi VL: " + amount);

            String processingFee = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode='CHR30'");
            String CICCharges = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR31','CHR32')");
            String esignAndStamp = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR34','CHR35')");
            Log.consoleLog(ifr, "schemeIDVL::::" + schemeID + "CICChargesVL::::" + CICCharges + "esignAndStampVL::::"
                    + esignAndStamp + "processingFeeVL : " + processingFee);

            Log.consoleLog(ifr, " before calling emicalculator api the values are  amountVL" + amount
                    + " tenureVL " + tenure + " roiVL " + roi);

            String FrameSection = "Branch Maker";
            EMICalculator e = new EMICalculator();
            String EMIAmount = e.getEmiCalculatorInstallment(ifr, ProcessInsanceId, amount,
                    tenure, roi, FrameSection);

            Log.consoleLog(ifr, "after calling emicalculator api EMIAmoumt : " + EMIAmount);

            List<List<String>> countData = null;
            String QueryCount = ConfProperty.getQueryScript("ROWINDEXCOUNTLOANAMOUNT").replaceAll("#WINAME#", ProcessInsanceId);
            Log.consoleLog(ifr, "QueryCount VL==>" + QueryCount);
            countData = ifr.getDataFromDB(QueryCount);
            String count = countData.get(0).get(0);
            if (Integer.parseInt(count) == 0) {
                String Query = ConfProperty.getQueryScript("INSERTTRNFINALQUERY").replaceAll("#ProcessInsanceId#", ProcessInsanceId).replaceAll("#amount#", amount).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi).replaceAll("#EMIAmoumt#", EMIAmount).replaceAll("#processingFeeRound#", processingFee);
                Log.consoleLog(ifr, "Insert Query VL==>" + Query);
                ifr.saveDataInDB(Query);
            } else {
                String Query = ConfProperty.getQueryScript("UPDATETRNFINALQUERY").replaceAll("#PID#", ProcessInsanceId).replaceAll("#tenure#", tenure).replaceAll("#processingFee#", processingFee).replaceAll("#roi#", roi).replaceAll("#LoanAmount#", amount).replaceAll("#emi#", EMIAmount);
                Log.consoleLog(ifr, "Update Query VL==>" + Query);
                ifr.saveDataInDB(Query);
            }
            String Query = ConfProperty.getQueryScript("UPDATEFINALELIPENQUERY").replaceAll("#PID#", ProcessInsanceId).replaceAll("#emi#", EMIAmount).replaceAll("#processingFee#", processingFee).replaceAll("#LoanAmount#", amount).replaceAll("#CICFee#", CICCharges).replaceAll("#signStamp#", esignAndStamp);
            Log.consoleLog(ifr, "Update Proposed Loan Query VL:" + Query);
            ifr.saveDataInDB(Query);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in mCalculateFeeandChargesVL " + e);
        }
        return "";
    }

    public String checkCBEligibilityDecisionVL(IFormReference ifr, String Control, String Event, String value) {
        try {
            Log.consoleLog(ifr, "inside checkCBEligibilityDecisionVL method::");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String consentQuery = "select ConsentReceived from LOS_NL_BUREAU_CONSENT where PID='" + PID + "'";
            List<List<String>> consentQueryResult = cf.mExecuteQuery(ifr, consentQuery, "Query for consentQueryResult");
            String consentReceived = "";
            if (consentQueryResult.size() > 0) {
                consentReceived = consentQueryResult.get(0).get(0);
                Log.consoleLog(ifr, "checkCBEligibilityDecisionVL::knockoffDecision for CB  :" + consentReceived);

                if (consentReceived.equalsIgnoreCase("Accepted")) {

                    String E_Status = ifr.getTableCellValue("CB_Eligibility_Grid", 0, 1);
                    Log.consoleLog(ifr, "knockoffDecision for CB  :" + E_Status);

                    if (E_Status.equalsIgnoreCase("Eligible")) {

                        ifr.clearCombo("DecisionValue");
//                        if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                        ifr.addItemInCombo("DecisionValue", "Recommend", "S");
                        ifr.addItemInCombo("DecisionValue", "Reject", "R");
                        /*}else{
                            ifr.addItemInCombo("DecisionValue", "Reject", "R");
                            ifr.addItemInCombo("DecisionValue", "Recommend", "S");
                        }*/
                    } else if (E_Status.contains("Not Eligible")) {
                        Log.consoleLog(ifr, " VL CB knockoffDecision fail " + E_Status);

                        ifr.clearCombo("DecisionValue");
                        ifr.addItemInCombo("DecisionValue", "Send Back", "SM");
                        ifr.addItemInCombo("DecisionValue", "Reject", "R");

                    }

                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  checkCBEligibilityDecisionVL method::" + e);
            Log.errorLog(ifr, "Exception in  checkCBEligibilityDecisionVL method::" + e);
        }
        return "";

    }

    public String CheckCibilandExperian(IFormReference ifr, String applicantType) {
        Log.consoleLog(ifr, "Inside VLBkoffcCustomCode::CheckCibilandExperian:VL ");

        try {
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String CustomerId = pcm.getCustomerIDCB(ifr, "CB");
            Log.consoleLog(ifr, "CheckCibilandExperian::CustomerId==>" + CustomerId);

            String MobileData_Query = ConfProperty.getQueryScript("getMobileNumberQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
            List list = cf.mExecuteQuery(ifr, MobileData_Query, "MobileData_Query:");
            String MobileNo = list.toString().replace("[", "").replace("]", "");
            String substringToRemove = "91";
            MobileNo = MobileNo.replace(substringToRemove, "");
            Log.consoleLog(ifr, "CheckCibilandExperian::MobileNo==>" + MobileNo);

            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", MobileNo);
            customerdetails.put("customerId", CustomerId);
            String responseCO = cas.getAadharCustomerAccountSummary(ifr, customerdetails);
            if (responseCO.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            Log.consoleLog(ifr, "CheckCibilandExperian::responseCO==>" + responseCO);

            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInsanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            Log.consoleLog(ifr, "CheckCibilandExperian::Purpose:" + Purpose);

            String productCode = pcm.mGetProductCodeVL(ifr, Purpose);
            Log.consoleLog(ifr, "CheckCibilandExperian::productCode:" + productCode);

            String subProductCode = "SUPVL";
            Log.consoleLog(ifr, "ProductCode:" + productCode);

            Log.consoleLog(ifr, "CheckCibilandExperian::AADHARNUMBER==>" + responseCO);

            String APIResponse = vlpc.mGetAPIData(ifr);
            Log.consoleLog(ifr, "CheckCibilandExperian::Entered into mGetAPIData:::");

            if (APIResponse.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(APIResponse);

            String cb = vlpc.mCallBureau(ifr, "CB", responseCO, "CB");
            if (cb.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }
            String decision = brmcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");
            Log.consoleLog(ifr, "decision1/CB::" + decision);
            if (decision.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;

            } else if (decision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "CheckCibilandExperian::CIBIL Passed Successfully:::");
                Log.consoleLog(ifr, "CheckCibilandExperian::CIBIL Passed Successfully:::" + decision);

                String EX = vlpc.mCallBureau(ifr, "EX", responseCO, "CB");
                if (EX.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                }
                decision = brmcr.checkCICScore(ifr, productCode, subProductCode, "EX", "B");
                Log.consoleLog(ifr, "decision2/EX::" + decision);
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnError(ifr);
                } else if (decision.equalsIgnoreCase("Approve")) {
                    Log.consoleLog(ifr, "CheckCibilandExperian::EXPERIAN Passed Successfully:::" + decision);
                    String bueroTableQuery = "select count(*) from LOS_NL_CB_Details where PID='" + ProcessInsanceId + "'";
                    Log.consoleLog(ifr, "bueroTableQuery ::" + bueroTableQuery);
                    List<List<String>> bueroTableData = ifr.getDataFromDB(bueroTableQuery);
                    String count = bueroTableData.get(0).get(0);
                    if (Integer.parseInt(count) == 2) {
                        String dataSaveBueroCheckGridQuery = "select distinct BureauType,Exp_CBSCORE from LOS_CAN_IBPS_BUREAUCHECK where ProcessInstanceId='" + ProcessInsanceId + "' and applicant_type='CB'";
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
                            String borrowerQuery = "select insertionOrderID from LOS_NL_BASIC_INFO where Applicanttype='CB' and PID='" + ProcessInsanceId + "'";
                            List<List<String>> borrowerQueryData = ifr.getDataFromDB(borrowerQuery);
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
                } else {
                    Log.consoleLog(ifr, "VLBkoffcCustomCode::CheckCibilandExperian::Experian Failed:::");

                }
            } else {
                Log.consoleLog(ifr, "VLBkoffcCustomCode::CheckCibilandExperian::Cibil Failed:::");

            }
            return decision;

        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in CheckCibilandExperian" + e);
        }
        return "";

    }

    public void OnloadBureaucheck(IFormReference ifr, String Control, String Event, String JSdata) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String PID = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + PID);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        String ActivityName = (String) ifr.getValue("activityname");
        if (ActivityName.equalsIgnoreCase("Branch Maker") || ActivityName.equalsIgnoreCase("Branch Checker")) {
            if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
                ifr.setStyle("F_CoborrowerEligibility_Sec", "visible", "true");
                ifr.setStyle("F_CoborrowerEligibility_Sec", "disable", "true");
            }
        }
        if (loan_selected.equalsIgnoreCase("VEHICLE LOAN")) {
            String apllicantType = "";
            String applicantTypeQuery = "select APPLICANTTYPE from LOS_NL_BASIC_INFO where  pid='" + PID + "'";
            List<List<String>> applicantTypeQueryEx = cf.mExecuteQuery(ifr, applicantTypeQuery, "Execute query for fetching Applicant Type");
            int rowCount = applicantTypeQueryEx.size();
            Log.consoleLog(ifr, "inside rowCount :: " + rowCount);
            if (rowCount == 1) {
                ifr.setStyle("F_CoborrowerEligibility_Sec", "visible", "false");
                ifr.setStyle("F_Bureau_Consent", "visible", "true");
                ifr.setStyle("Btn_Refresh", "visible", "false");
            }
        }
    }

    public String mAccPortalCalculateNetIncomeVLBK(IFormReference ifr, String control, String event, String value) {
        return pcm.mCalculateNetIncome(ifr, "QA_FI_PI_MINCOME_GrossAmt", "QA_FI_PI_MINCOME_Deduction", "QA_FI_PI_MINCOME_NetAmount", "", "");
    }

    public String mBranchcheckerNeslVL(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mBranchcheckerNeslVL");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String decision = ifr.getValue("DecisionValue").toString();
            Log.consoleLog(ifr, "decision::" + decision);
            if (decision.equalsIgnoreCase("S")) { //Branch Checker
                String loanAmount = "";
                String loanTenure = "";
                String IndexQuery = ConfProperty.getQueryScript("GETLOANAMOUNTTENUREVL").replaceAll("#WINAME#", ProcessInstanceId);
                Log.consoleLog(ifr, "Loan Amount and Loan Tenure VL query " + IndexQuery);
                List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
                if (!docIndexList.isEmpty()) {
                    loanAmount = docIndexList.get(0).get(0);
                    loanTenure = docIndexList.get(0).get(1);
                }
                String query = ConfProperty.getQueryScript("PUPDATEREFERENCEQuery").replaceAll("#WINAME#", ProcessInstanceId).replaceAll("#LOANAMOUNT#", loanAmount).replaceAll("#LOANTENURE#", loanTenure);
                ifr.saveDataInDB(query);
                Log.consoleLog(ifr, "inside if:::" + decision);
                String returnMessageFromDocGen = mGenerateDoc(ifr);
                if (returnMessageFromDocGen.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorHold(ifr);
                }
                EsignIntegrationChannel NESL = new EsignIntegrationChannel();
              //  String returnMessage = NESL.redirectNESLRequest(ifr, "VL", "eStamping");
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
            } else if (decision.equalsIgnoreCase("SM") || decision.equalsIgnoreCase("R")) {
//                Email em = new Email();
//                SMS sms = new SMS();
//                String emailId = pcm.getCurrentEmailId(ifr, "VL", "B");
//                String mobileNumber = pcm.getMobileNumber(ifr);
//                em.sendEmail(ifr, ProcessInstanceId, emailId, "", "VL", "RETAIL", "4");
//                sms.sendSMS(ifr, ProcessInstanceId, mobileNumber, "", "VL", "RETAIL", "4");
//                
//                
                //Added by Ahmed on 27-05-2024 using DLPCommonObjects instead of directly calling
                String bodyParams = "";
                String subjectParams = "";
                String fileName = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                pcm.triggerCCMAPIs(ifr, ProcessInstanceId, "VL", "4", bodyParams, subjectParams, fileName, fileContent);

            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Exception mBranchcheckerNeslVL : " + e);
        }
        return RLOS_Constants.ERROR;
    }
//added by ishwarya for DM on 22/6/2024

    public void bamListViewLoad(IFormReference ifr) {
        Log.consoleLog(ifr, "inside bamListViewLoad VL: ");
        if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
            Log.consoleLog(ifr, "inside bamListViewLoad: ");
            String editableFields = "QNL_BAM83_RBI_PURPOSE_CODE,QNL_BAM83_SECTOR,QNL_BAM83_RETAIL_BASEL_II_CUSTOMER_TYPE,QNL_BAM83_SCHEMES,QNL_BAM83_GUARANTEE_COVER,QNL_BAM83_BSR_CODE,QNL_BAM83_SSISUBSEC,QNL_BAM83_STATUSIB,QNL_BAM83_PRI_SECTOR_N_PRI_SECTOR,QNL_BAM83_SPECIAL_BENEFICIARIES,QNL_BAM83_SUB_SCHEME,QNL_BAM83_RAH";
            pcm.controlEnable(ifr, editableFields);
        }
    }

    //added by ishwarya for DM on 22/6/2024
    public void loanDetailsChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside loanAccChange VL: ");
        if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
            ifr.setStyle("ALV_BAM83", "disable", "false");

            Log.consoleLog(ifr, "inside loanDetailsChange: ");
            String InvisibleFields = "QL_LOAN_DETAILS_SubsidyProcessRequired,QL_LOAN_DETAILS_LoanInsured,QL_LOAN_DETAILS_InsuranceScheme,QL_LOAN_DETAILS_MortgageFlag,QL_LOAN_DETAILS_SecurityCount, QL_LOAN_DETAILS_HypothecationFlag,QL_LOAN_DETAILS_CGSTMECovered,QL_LOAN_DETAILS_CGPAN,QL_LOAN_DETAILS_LeadBank,QL_LOAN_DETAILS_Shares,QL_LOAN_DETAILS_TotalLimitunderConsortium,QL_LOAN_DETAILS_OtherLeadBank,QL_LOAN_DETAILS_INCREMENTAL_INTEREST";
            pcm.controlinvisiblity(ifr, InvisibleFields);

        }
    }

    //added by ishwarya for DM on 22/6/2024
    public void loanAccChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside loanAccChange VL: ");
        if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
            Log.consoleLog(ifr, "inside loanAccChange: ");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            ifr.setStyle("BTN_LoanCreation", "disable", "false");
            String query1 = "select LOANACCOUNTNO from LOS_NL_LOAN_ACC_CREATION where PID ='#PID#'".replaceAll("#PID#", ProcessInstanceId);
            //  ConfProperty.getQueryScript("LOANACCOUNTCREATEDCHECK").replaceAll("#PID#", ProcessInstanceId);

            List<List<String>> Loanaccount = cf.mExecuteQuery(ifr, query1, "Execute query for fetching loan selected ");

            if (Loanaccount.size() > 0) {
                if (Loanaccount.get(0).get(0).equalsIgnoreCase("ERROR")) {
                    ifr.clearTable("ALV_LOAN_ACC_CREATION");
                } else {
                    ifr.setStyle("BTN_LoanCreation", "disable", "true");
                }
            }
        }
    }

    //added by ishwarya for DM on 22/6/2024
    public void cifListViewLoad(IFormReference ifr) {
        Log.consoleLog(ifr, "inside cifListViewLoad VL: ");
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

    //added by ishwarya for DM on 22/6/2024
    public void beneficiaryFrameChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside beneficiaryDetails VL: ");
        if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
            Log.consoleLog(ifr, "inside beneficiaryColumn stage: ");

            ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "7", false);
            ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "9", false);
            ifr.setColumnVisible("ALV_BENEFICIARY_DETAILS", "8", false);
        }
    }

    //added by ishwarya for DM on 22/6/2024
    public void beneficiaryDetails(IFormReference ifr) {

        Log.consoleLog(ifr, "inside beneficiaryDetails VL : ");

        if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
            Log.consoleLog(ifr, "inside beneficiaryFields stage: ");
            String enableFields = "QNL_BENEFICIARY_DETAILS_DisbursalTo,QNL_BENEFICIARY_DETAILS_BeneficiaryName,QNL_BENEFICIARY_DETAILS_Amount,QNL_BENEFICIARY_DETAILS_StartDate,QNL_BENEFICIARY_DETAILS_ExpiryDate,QNL_BENEFICIARY_DETAILS_PaymentMode,QNL_BENEFICIARY_DETAILS_FromAccount,QNL_BENEFICIARY_DETAILS_DisburseDate";
            pcm.controlEnable(ifr, enableFields);
            String invisibleFields = "QNL_BENEFICIARY_DETAILS_Status,QNL_BENEFICIARY_DETAILS_DisburseDate";
            pcm.controlinvisiblity(ifr, invisibleFields);
        }
    }

    //added by ishwarya for DM on 22/6/2024
    public String mDisbursementMakerAPICallVL(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "inside method mDisbursementMakerAPICall VL");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            JSONArray arr = new JSONArray();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
//            String LoanAccountNumber = getloanaccountcreation(ifr);
//            if (LoanAccountNumber.equalsIgnoreCase("ERROR")) {
//                return pcm.returnErrorcustmessage(ifr, "Error In Loan Account Creation ");
//            }

            String schCodeUpdationQuery = "UPDATE LOS_NL_PROPOSED_FACILITY SET SCHEDULECODE = '3012-' WHERE PID ='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "schCodeUpdationQuery ::::" + schCodeUpdationQuery);
            int Result = ifr.saveDataInDB(schCodeUpdationQuery);
            Log.consoleLog(ifr, "### schedule Code Updated ###" + Result);

            String disbStatus = createVLLoanDisburse(ifr);
            if (disbStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }

            // Log.consoleLog(ifr, "LoanAccountNumber==>" + LoanAccountNumber);
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String Product = pcm.mGetProductCodeVL(ifr, Purpose);
            Log.consoleLog(ifr, "disbursement maker::product::" + Product);
            String ProductName = "";
            String ProductNameQuery = "SELECT ProductName FROM LOS_M_Product WHERE ProductCode='" + Product + "'";
            List<List<String>> ProductNameList = ifr.getDataFromDB(ProductNameQuery);
            if (!ProductNameList.isEmpty()) {
                ProductName = ProductNameList.get(0).get(0);
            }
            String loanCreatedDate = "select ACCOUNT_CREATEDDATE,LOAN_ACCOUNTNO from los_trn_loandetails where pid='" + ProcessInstanceId + "'";
            List<List<String>> loanCreated = cf.mExecuteQuery(ifr, loanCreatedDate, "created date query");
            String CreatedDate = "";
            String loanAccNumber = "";
            if (!loanCreated.isEmpty()) {
                CreatedDate = loanCreated.get(0).get(0);
                loanAccNumber = loanCreated.get(0).get(1);
            }
            SimpleDateFormat CreatedDateformat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
            SimpleDateFormat TargetDate = new SimpleDateFormat("dd/MM/yyyy");
            Date dateDD = CreatedDateformat.parse(CreatedDate);
            Log.consoleLog(ifr, "CreatedDate before formatted :" + dateDD);
            CreatedDate = TargetDate.format(dateDD);
            Log.consoleLog(ifr, "CreatedDate formatted :" + CreatedDate);
            JSONObject obj = new JSONObject();
            obj.put("Loan Account No", loanAccNumber);
            obj.put("Product", ProductName);
            obj.put("Account Opening Date", CreatedDate);
            arr.add(obj);
            Log.consoleLog(ifr, "Json Arr" + arr);

            ifr.addDataToGrid("ALV_LOAN_ACC_CREATION", arr, true);
            ifr.setStyle("BTN_LoanCreation", "disable", "true");
            ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "0", true);
            ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "1", true);
            ifr.setColumnDisable("ALV_LOAN_ACC_CREATION", "2", true);
            if (loanAccNumber.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorHold(ifr);
            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mDisbursementCheckerAPICallVL");
        }
        return RLOS_Constants.ERROR;
    }

//    //added by ishwarya for DM on 22/6/2024
//    public String getloanaccountcreation(IFormReference ifr) {
//        String LoanAccNumber = "";
//        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//
//        Log.consoleLog(ifr, "LoanAccNumber not available");
//        try {
//            Log.consoleLog(ifr, "inside method getloanaccountcreation VL");
//            String ProductCode = "";//Need to change as oer the final requets
//            String CustomerId = "";
//            String LoanAmount = "";
//            String Tenure = "";
//
//            CustomerId = pcm.getCustomerIDForOtherProducts(ifr);
//            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
//
//            String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", ProcessInstanceId);
//
//            //"SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
//            //  + "WHERE WINAME='" + ProcessInstanceId + "'";
//            List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Get Loan account details ");
//
//            if (Output3.size() > 0) {
//                LoanAmount = Output3.get(0).get(0);
//                Tenure = Output3.get(0).get(1);
//            }
//
//            Date currentDate = new Date();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(currentDate);
//            calendar.add(Calendar.DAY_OF_YEAR, 90);
//            Date dateAfter90Days = calendar.getTime();
//            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
//            String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);
//
//            LoanAccNumber = API.getLoanAccountDetails(ifr, ProcessInstanceId, ProductCode,
//                    CustomerId, LoanAmount, Tenure, SanctionExpiryDate, "VEHICLE LOAN");
//            Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
//            if (LoanAccNumber.equalsIgnoreCase("")) {
//                return RLOS_Constants.ERROR;
//            }
//
//            return LoanAccNumber;
//        } catch (ParseException e) {
//            Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
//            Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
//        }
//        return RLOS_Constants.ERROR;
//    }
    //added by ishwarya for DM on 22/6/2024
    public void OnchangeDisbursement(IFormReference ifr) {

        Log.consoleLog(ifr, "inside try block::::OnchangeDisbursement:::VL:: ");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::OnchangeDisbursement::::" + ProcessInstanceId);

        String disableFields = "QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_CustomerName,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_InstallmentAmount";
        Log.consoleLog(ifr, "disableFields:::OnchangeDisbursement::::" + disableFields);
        pcm.controlDisable(ifr, disableFields);
        String inVisible = "QNL_DISBURSMENT_FixedTerm,textbox6876";
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
        salaryAccount = vlpc.Accountdetail(ifr);
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
        Log.consoleLog(ifr, "drawDown:::OnchangeDisbursement::VL::" + drawDown);
        ifr.setValue("QNL_DISBURSMENT_Drawdown", drawDown);
    }

    //added by ishwarya for DM on 22/6/2024
    public void cifFrameChange(IFormReference ifr) {
        Log.consoleLog(ifr, "inside beneficiaryDetails VL: ");
        JSONArray arr = new JSONArray();
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);

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

    //added by ishwarya on 29/06/2024
    public String caluclateEMI(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside caluclateEMI VL:::");
        try {
            WDGeneralData Data = ifr.getObjGeneralData();
            String ProcessInstanceId = Data.getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            String loanTenure = null;
            String tenureData_Query = ConfProperty.getQueryScript("PortalInprincipleSliderData").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query FROM PORTAL:");
            if (list1.size() > 0) {
                loanTenure = list1.get(0).get(2);
            }
            Log.consoleLog(ifr, "loanTenure : " + loanTenure);
            String Purpose = null;
            String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery,
                    "Execute query for fetching Purpose data from portal");
            if (PurposePortal.size() > 0) {
                Purpose = PurposePortal.get(0).get(0);
            }
            String schemeID = pcm.mGetSchemeIDVL(ifr, Purpose);
            Log.consoleLog(ifr, "schemeIDVL:" + schemeID);
            String loanROI = pcm.mGetRoiVL(ifr, schemeID);
            Log.consoleLog(ifr, "loanROI VL:" + loanROI);
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
            BigDecimal salaryMultiplier = recommendedLoanAmountstr.divide(grossAmount, 2, RoundingMode.HALF_DOWN);
            Log.consoleLog(ifr, "Inside salaryMultiplier: " + salaryMultiplier);
            String salaryMultiplierStr = salaryMultiplier.toString();
            Log.consoleLog(ifr, "Inside SalaryMultiplier" + salaryMultiplierStr);
            ifr.setValue("QNL_LA_FINALELIGIBILITY_Mutiplierenteredbyuser", salaryMultiplierStr);
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    //modified by janani for pdd on 23-07-2024
    public void formLoadPDD(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, " PDD VL Code start");
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            //ifr.setTabStyle("tab1", "14", "visible", "true");//Disbursement
            ifr.setTabStyle("tab1", "7", "visible", "false");//eligibility
            ifr.setTabStyle("tab1", "8", "visible", "true");//Collateral
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            ifr.setTabStyle("tab1", "16", "visible", "false");//E-sign Check
            String visibleSectionsGrids = "F_FeeCharges,F_CoborrowerEligibility_Sec,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "BTN_FetchScoreRating,F_Deviation,QL_RISK_RATING_Rank,QL_RISK_RATING_RiskScore,DD_CB_BUREAU_ID,CTRID_PD_FETCHEXTCUST,CTRID_PD_RESETDET,BTN_Dedupe_Click,F_ESIGN_ID,F_CoborrowerEligibility_Sec,F_Mortgage,F_CollateralSummary,F_Risk_Rating,F_Bureau_NegativeDetails,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            //Disbursment Tab field visibility
            String DisbursmentVisibleFields = "QNL_DISBURSMENT_ProbableDateofDisbursement,QNL_DISBURSMENT_ProposedLoanAmount,QNL_DISBURSMENT_InstallmentAmount,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_RepaymentType,QNL_DISBURSMENT_OtherFeeAndCharges,QNL_DISBURSMENT_DateofDisbursement,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_FixedTerm,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerName";
            pcm.controlvisiblity(ifr, DisbursmentVisibleFields);
            String DisbursmentHideFields = "datepick474,combo1737,datepick475";
            pcm.controlinvisiblity(ifr, DisbursmentHideFields);
            String[] FieldVisibleFalse = new String[]{"QNL_LOS_COLLATERAL_VEHICLES_COLOR", "QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURINGDATE", "QNL_LOS_COLLATERAL_VEHICLES_REGISTRATIONNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_ENGINENUMBER", "QNL_LOS_COLLATERAL_VEHICLES_CHASISNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_INSURANCECOMPANYNAME", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEPOLICYNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_AGEOFVEHICLE", "QNL_LOS_COLLATERAL_VEHICLES_ASSETOWNER", "QNL_LOS_COLLATERAL_VEHICLES_DEALERCODE",
                "QNL_LOS_COLLATERAL_VEHICLES_DEALERADDRESS",
                "QNL_LOS_COLLATERAL_VEHICLES_EX_SHOWROOMPRICE", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEAMOUNT",
                "QNL_LOS_COLLATERAL_VEHICLES_LTT_REGISTRATIONCHARGES", "QNL_LOS_COLLATERAL_VEHICLES_OTHERCHARGES",
                "QNL_LOS_COLLATERAL_VEHICLES_ROADTAX", "QNL_LOS_COLLATERAL_VEHICLES_ONROADPRICE", "QNL_LOS_COLLATERAL_VEHICLES_MARGIN",
                "QNL_LOS_COLLATERAL_VEHICLES_PERMISSIBLEVALUE", "VL_Fetch_Margin", "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONBY",
                "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONON", "Vehicle_Title", "QNL_LOS_COLLATERAL_VEHICLES_SECURITYTYPE", "VL_Transmission_Type",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_CommunicationAddress", "Same_As_Permanent_Address", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", "BTN_Address_Fetch", "Copy_Address_From", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_District"};
            for (int i = 0; i < FieldVisibleFalse.length; i++) {
                ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                Log.consoleLog(ifr, "FieldVisibleFalse[i]" + FieldVisibleFalse[i]);
            }
            String collateralDisable = "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country,QNL_BASIC_INFO_CNL_CUST_ADDRESS_State,QNL_LOS_COLLATERAL_VEHICLES_FUELTYPE,QNL_LOS_COLLATERAL_VEHICLES_VEHICLETYPE,QNL_LOS_COLLATERAL_VEHICLES_ASSETMANUFACTURER,QNL_LOS_COLLATERAL_VEHICLES_ASSETMODEL,QNL_LOS_COLLATERAL_VEHICLES_VEHICLEVARIANT,QNL_LOS_COLLATERAL_VEHICLES_VEHICLECOST,QNL_LOS_COLLATERAL_VEHICLES_DOWNPAYMENTAMT,QNL_LOS_COLLATERAL_VEHICLES_CATEGORY,QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURERYEAR,QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME,QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE";
            pcm.controlDisable(ifr, collateralDisable);
            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "8", false);
            ifr.setStyle("LV_ADDRESS", "disable", "true");
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_Variant", "disable", "true");
            ifr.setStyle("ALV_AL_ASSET_DET", "disable", "true");
            ifr.setStyle("ALV_PL_MExpense", "disable", "true");
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);
            Log.consoleLog(ifr, " PDD VL Code End");
        }
    }

    //added by Janani on 11-07-2024
    //modified by ishu on 19-07-2024
    public void formLoadSanctionVL(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        Log.consoleLog(ifr, "inside sanctionvVisibility VL");
        ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
        ifr.setTabStyle("tab1", "8", "visible", "true");//Collateral
        ifr.setTabStyle("tab1", "14", "visible", "false");//Disbursement
        ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
        ifr.setTabStyle("tab1", "7", "visible", "false");//Eligibility
        ifr.setStyle("ALV_VEHICLE_COLLATERAL", "visible", "true");
        ifr.setStyle("LV_OCCUPATION_INFO", "disable", "true");
        ifr.setColumnVisible("ALV_VEHICLE_COLLATERAL", "0", false);
        String visibleSectionsGrids = "F_CoborrowerEligibility_Sec,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_Deviation,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation";
        pcm.controlvisiblity(ifr, visibleSectionsGrids);
        String nonVisibleSectionGrids = "F_Deviation,F_ESIGN_ID,DD_CB_BUREAU_ID,F_Mortgage,F_CollateralSummary,F_Risk_Rating,F_Bureau_NegativeDetails,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST";
        pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
        String[] FieldVisibleFalse = new String[]{"QNL_LOS_COLLATERAL_VEHICLES_COLOR", "QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURINGDATE", "QNL_LOS_COLLATERAL_VEHICLES_REGISTRATIONNUMBER",
            "QNL_LOS_COLLATERAL_VEHICLES_ENGINENUMBER", "QNL_LOS_COLLATERAL_VEHICLES_CHASISNUMBER",
            "QNL_LOS_COLLATERAL_VEHICLES_INSURANCECOMPANYNAME", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEPOLICYNUMBER",
            "QNL_LOS_COLLATERAL_VEHICLES_AGEOFVEHICLE", "QNL_LOS_COLLATERAL_VEHICLES_ASSETOWNER", "QNL_LOS_COLLATERAL_VEHICLES_DEALERCODE",
            "QNL_LOS_COLLATERAL_VEHICLES_DEALERADDRESS",
            "QNL_LOS_COLLATERAL_VEHICLES_EX_SHOWROOMPRICE", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEAMOUNT",
            "QNL_LOS_COLLATERAL_VEHICLES_LTT_REGISTRATIONCHARGES", "QNL_LOS_COLLATERAL_VEHICLES_OTHERCHARGES",
            "QNL_LOS_COLLATERAL_VEHICLES_ROADTAX", "QNL_LOS_COLLATERAL_VEHICLES_ONROADPRICE", "QNL_LOS_COLLATERAL_VEHICLES_MARGIN",
            "QNL_LOS_COLLATERAL_VEHICLES_PERMISSIBLEVALUE", "VL_Fetch_Margin", "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONBY",
            "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONON", "Vehicle_Title", "QNL_LOS_COLLATERAL_VEHICLES_SECURITYTYPE", "VL_Transmission_Type",
            "QNL_BASIC_INFO_CNL_CUST_ADDRESS_CommunicationAddress", "Same_As_Permanent_Address", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark",
            "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", "BTN_Address_Fetch", "Copy_Address_From", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone",
            "QNL_BASIC_INFO_CNL_CUST_ADDRESS_District"};
        for (int i = 0; i < FieldVisibleFalse.length; i++) {
            ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
            Log.consoleLog(ifr, "FieldVisibleFalse[i]" + FieldVisibleFalse[i]);
        }
        String collateralDisable = "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country,QNL_BASIC_INFO_CNL_CUST_ADDRESS_State,QNL_LOS_COLLATERAL_VEHICLES_FUELTYPE,QNL_LOS_COLLATERAL_VEHICLES_VEHICLETYPE,QNL_LOS_COLLATERAL_VEHICLES_ASSETMANUFACTURER,QNL_LOS_COLLATERAL_VEHICLES_ASSETMODEL,QNL_LOS_COLLATERAL_VEHICLES_VEHICLEVARIANT,QNL_LOS_COLLATERAL_VEHICLES_VEHICLECOST,QNL_LOS_COLLATERAL_VEHICLES_DOWNPAYMENTAMT,QNL_LOS_COLLATERAL_VEHICLES_CATEGORY,QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURERYEAR,QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME,QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE";
        pcm.controlDisable(ifr, collateralDisable);
        ifr.setStyle("ALV_VEHICLE_COLLATERAL", "disable", "true");
        ifr.setStyle("QNL_BUREAU_CONSENT_PartyType", "disable", "true");
        ifr.setStyle("CTRID_PD_FETCHEXTCUST", "visible", "false");
        ifr.setStyle("BTN_Dedupe_Click", "visible", "false");
        ifr.setStyle("QNL_LA_FINALELIGIBILITY_Tenure", "disable", "true");
        ifr.setStyle("CTRID_PD_RESETDET", "visible", "false");
        ifr.setStyle("BTN_FinancialInfo_Calculate", "visible", "false");
        ifr.setStyle("Btn_Refresh", "visible", "false");
        ifr.setStyle("BTN_FetchScoreRating", "visible", "false");
        ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "8", false);
        ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "13", true);
        ifr.setColumnVisible("ALV_SCORERATING", "2", false);
        ifr.setColumnVisible("ALV_SCORERATING", "3", false);
        ifr.setColumnVisible("ALV_SCORERATING", "4", false);
        ifr.setColumnVisible("ALV_SCORERATING", "5", false);
        ifr.setColumnVisible("ALV_SCORERATING", "6", false);
        Log.consoleLog(ifr, " formLoadSanctionVisibility VL Code");

    }

    public String OnChangeAssetType(IFormReference ifr) {
        Log.consoleLog(ifr, "inside in OnChangeAssetType");
        try {
            ifr.setValue("QNL_AL_ASSET_DET_AssetSubType", "");
            ifr.clearCombo("QNL_AL_ASSET_DET_AssetSubType");
            String assetType = ifr.getValue("QNL_AL_ASSET_DET_AssetType").toString();
            String queryV = "SELECT DISTINCT AssetPartiType,AssetPartiCode FROM LOS_M_ASSET_TYPE WHERE IsActive='Y' AND AssetCode = '" + assetType + "'";
            Log.consoleLog(ifr, "queryV:" + queryV);
            List<List<String>> list = cf.mExecuteQuery(ifr, queryV, "Load occupation type");
            for (int i = 0; i < list.size(); i++) {
                String label = list.get(i).get(0);
                Log.consoleLog(ifr, "label ::  " + label);
                String value1 = list.get(i).get(1);
                Log.consoleLog(ifr, "label ::  " + label);
                ifr.addItemInCombo("QNL_AL_ASSET_DET_AssetSubType", label, value1);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in OnChangeAssetType" + e);
        }
        return "";
    }

    //added by ishwarya to calculate LTV on change of recommened and approved loan amt on 16-7-2024
    public String onChangeRecommendedAmountFinalEligibilityVL(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "inside onChangeRecommendedAmountFinalEligibility :; ");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String recommendedLoanAmount = "";

            if (ifr.getActivityName().equalsIgnoreCase("Branch Maker")) {
                recommendedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            } else {
                recommendedLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_APPROVED_LOANAMOUNT").toString();
            }
            Log.consoleLog(ifr, " recommendeLoanAmount:::" + recommendedLoanAmount);
            double recommendedLoanAmountD = Double.parseDouble(recommendedLoanAmount);
            Log.consoleLog(ifr, " recommendedLoanAmountD:::" + recommendedLoanAmountD);
            Log.consoleLog(ifr, "inside LTV brms:onChangeRecommendedAmountFinalEligibility::");

            String Purpose = "";
            String OccupationType = "";
            String AssestCategory = "";
            String OccupationCategory = "";
            String CostofV = "";
            String Ext = "";

            String F_KEY = bpcm.Fkey(ifr, "B");
            Log.consoleLog(ifr, "inside LTV brms:F_KEY::" + F_KEY);

            String occupationInfoQ = "SELECT PROFILE,CATEGORY,EXISTINGCUSTOMER FROM LOS_NL_OCCUPATION_INFO WHERE F_KEY = '" + F_KEY + "'";
            List<List<String>> occupationInfo = cf.mExecuteQuery(ifr, occupationInfoQ, "occupationInfoQ:");
            Log.consoleLog(ifr, " occupationInfo:::" + occupationInfo);

            if (occupationInfo.size() > 0) {
                OccupationType = occupationInfo.get(0).get(0);
                OccupationCategory = occupationInfo.get(0).get(1);
                Ext = occupationInfo.get(0).get(2);
            }

            String collateralInfoQ = "SELECT CATEGORY,VEHICLETYPE,VEHICLECOST FROM LOS_NL_COLLATERAL_VEHICLES WHERE PID = '" + ProcessInstanceId + "'";
            List<List<String>> collateralInfo = cf.mExecuteQuery(ifr, collateralInfoQ, "collateralInfoQ:");
            Log.consoleLog(ifr, "collateralInfo:::" + collateralInfo);

            if (collateralInfo.size() > 0) {
                AssestCategory = collateralInfo.get(0).get(0);
                Purpose = collateralInfo.get(0).get(1);
                CostofV = collateralInfo.get(0).get(2);
            }

            Log.consoleLog(ifr, "inside LTV brms:OccupationType::" + OccupationType);
            Log.consoleLog(ifr, "inside LTV brms:Purpose::" + Purpose);
            Log.consoleLog(ifr, "inside LTV brms:AssestCategory::" + AssestCategory);
            Log.consoleLog(ifr, "inside LTV brms:OccupationCategory::" + OccupationCategory);
            Log.consoleLog(ifr, "inside LTV brms:AssestCategory::" + AssestCategory);

            String finalLTVAmount = "";
            if (Purpose.equalsIgnoreCase("N2W")) {
                Log.consoleLog(ifr, "iniside Purpose Two Wheeler:::" + Purpose);
                Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                //input_parameter,assetcategory_ip,existingcustomer_ip,occupationtype_ip,purpose_ip
                Log.consoleLog(ifr, "AssestCategory::" + AssestCategory + ",Ext::" + Ext
                        + ",OccupationType::" + OccupationType + ",Purpose::" + Purpose);
                String LTVTwoWheelerInParams = AssestCategory + "," + Ext + "," + OccupationType + "," + Purpose;
                finalLTVAmount = vlpc.checkLTVTwowheeler(ifr, "VL_LTV_Twowheeler", LTVTwoWheelerInParams, "ltv_op");
                Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
            } else {
                Log.consoleLog(ifr, "iniside Purpose Four Wheeler:::" + Purpose);
                Purpose = Purpose.equalsIgnoreCase("N2W") ? "Purchase of New Two-wheeler" : "Purchase of New Four-Wheeler";
                //input_parameter,purpose_ip,occupationtype_ip,occupationcategory_ip,loanamt_ip,assetcategory_ip,
                Log.consoleLog(ifr, "Purpose::" + Purpose + ",OccupationType::" + OccupationType
                        + ",OccupationCategory::" + OccupationCategory + ",reqLoanAmt::" + recommendedLoanAmount + ",AssestCategory::" + AssestCategory);

                String LTVFourWheelerInParams = Purpose + "," + OccupationType + "," + OccupationCategory + "," + recommendedLoanAmount + "," + AssestCategory;
                finalLTVAmount = vlpc.checkLTVFourwheeler(ifr, "VL_LTV_Fourwheeler", LTVFourWheelerInParams, "ltv_op");
                Log.consoleLog(ifr, "finalLTVAmount:::" + finalLTVAmount);
            }

            double finalLTVAmountD = Double.parseDouble(finalLTVAmount);
            double CostofVehD = Double.parseDouble(CostofV);
            double finalLTVAmountC = (recommendedLoanAmountD / CostofVehD) * 100;

            Log.consoleLog(ifr, " finalLTVAmountD:::" + finalLTVAmountD);
            Log.consoleLog(ifr, " CostofVehD:::" + CostofVehD);
            Log.consoleLog(ifr, " finalLTVAmountC:::" + finalLTVAmountC);

            // Format results to two decimal places
            DecimalFormat df = new DecimalFormat("#.##");

//            double finalEligLTVAmount = finalLTVAmountD / 100;
            double finalLTV = finalLTVAmountD <= finalLTVAmountC ? finalLTVAmountD : finalLTVAmountC;

            Log.consoleLog(ifr, "final LTV taken ===> " + df.format(finalLTV));

            //double LoanAmountAsPerLtv = finalLTV * CostofVehD;
            double LoanAmountAsPerLtv = (finalLTV * CostofVehD) / 100;

            Log.consoleLog(ifr, "LoanAmountAsPerLtv===> " + df.format(LoanAmountAsPerLtv));

            double downPayment = CostofVehD - LoanAmountAsPerLtv;
            Log.consoleLog(ifr, "downPayment===> " + df.format(downPayment));

            // Set values with formatted strings
//            ifr.setValue("QNL_LA_FINALELIGIBILITY_LOAMAMOUNTASPERLTV", df.format(LoanAmountAsPerLtv));
            ifr.setValue("QNL_LA_FINALELIGIBILITY_LoanAmountAsLtv", df.format(LoanAmountAsPerLtv));
            ifr.setTableCellValue("ALV_VEHICLE_COLLATERAL", 0, 4, df.format(downPayment));
            Log.consoleLog(ifr, "ALV_VEHICLE_COLLATERAL gridResultSet Populated : " + downPayment);

            JSONObject returnJSON = new JSONObject();
            returnJSON.put("saveWorkitem", "true");
            return returnJSON.toString();

        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception in onChangeRecommendedAmountFinalEligibility" + e);
            return "error";
        }
    }

    public String onChangeESignStatus(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside onChangeESignStatus");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            String esignStatus = ifr.getValue("E-Sign_initiated").toString();
            if (esignStatus.equalsIgnoreCase("yes")) {
                ifr.setStyle("ESign_Initaite", "visible", "true");
                ifr.setStyle("ESign_Initaite", "disable", "false");
                ifr.setStyle("textbox6939 ", "visible", "false");
            } else if (esignStatus.equalsIgnoreCase("no")) {
                ifr.setStyle("E-Sign_initiated", "disable", "false");
                ifr.setStyle("ESign_Initaite", "visible", "false");
                ifr.setStyle("textbox6939 ", "visible", "false");
                SimpleDateFormat sdf = new SimpleDateFormat(ConfProperty.getCommonPropertyValue("AvailableDateTime"));
                String currDate = sdf.format(new Date());
                Log.consoleLog(ifr, "Current Date Time :::" + currDate);
                String documentName = "NESL_1_eSigning";
                String e_signStatus = "Manual";
                String queryData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
                String party_type = data.get(0).get(0);
                Log.consoleLog(ifr, "Party Type==>" + party_type);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("QNL_DOC_ESIGN_STATUS_DocumentName", documentName);
                jsonObj.put("QNL_DOC_ESIGN_STATUS_initiatedDate", currDate);
                jsonObj.put("QNL_DOC_ESIGN_STATUS_EsignCompletedDate", currDate);
                jsonObj.put("QNL_DOC_ESIGN_STATUS_Status", e_signStatus);
                jsonObj.put("QNL_DOC_ESIGN_STATUS_PartyType", party_type);
                jsonObj.put("QNL_DOC_ESIGN_STATUS_ESignMode", esignStatus);
                JSONArray jsonArrayObj = new JSONArray();
                jsonArrayObj.add(jsonObj);
                Log.consoleLog(ifr, "jsonObjArr" + jsonArrayObj.toString());
                int esignCount = ifr.getDataFromGrid("ALV_ESIGN").size();
                Log.consoleLog(ifr, "esignCount :: " + esignCount);
                if (esignCount == 0) {
                    Log.consoleLog(ifr, " inside else esignCount = 0:: ");
                    ifr.addDataToGrid("ALV_ESIGN", jsonArrayObj, true);
                    //ifr.setStyle("ALV_ESIGN", "disable", "true");
                } else {
                    Log.consoleLog(ifr, " inside else esignCount :: ");
                    ifr.clearTable("ALV_ESIGN");
                    ifr.addDataToGrid("ALV_ESIGN", jsonArrayObj, true);
                    //ifr.setStyle("ALV_ESIGN", "disable", "true");
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onChangeESignStatus ::: General Contract " + e);
            Log.errorLog(ifr, "Exception in onChangeESignStatus ::: General Contract " + e);
        }
        return "";
    }

    public String onLoadCollateralDetails(IFormReference ifr, String control, String event, String value) {
        Log.consoleLog(ifr, "Inside onLoadCollateralDetails");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            JSONObject message = new JSONObject();
            String expDeliveryDate = ifr.getValue("QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE").toString();
            Log.consoleLog(ifr, "Current expDeliveryDate :::" + expDeliveryDate);
            SimpleDateFormat sdf = new SimpleDateFormat(ConfProperty.getCommonPropertyValue("AvailableDateTime"));
            String currDate = sdf.format(new Date());
            Log.consoleLog(ifr, "Current Date Time :::" + currDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date1 = LocalDate.parse(currDate, formatter);
            LocalDate date2 = LocalDate.parse(expDeliveryDate, formatter);
            long daysDiff = ChronoUnit.DAYS.between(date1, date2);
            Log.consoleLog(ifr, "daysDiff :::" + daysDiff);
            if (daysDiff > 15) {
                Log.consoleLog(ifr, "inside less than 15 days :::");
                ifr.setValue("QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE", "");
                ifr.setStyle("QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE", "disable", "false");
                message.put("showMessage", cf.showMessage(ifr, control, "error", "Expected Delivery Date should not be more than 15 days"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onLoadCollateralDetails ::: General Contract " + e);
            Log.errorLog(ifr, "Exception in onLoadCollateralDetails ::: General Contract " + e);
        }
        return "";
    }

    //added by janani on 22-07-2024
    //modified by ishwarya on 29-07-2024
    public void formLoadDisbursementMakerVisibilityVL(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, " DisbursementMaker VL Code start");
            ifr.setTabStyle("tab1", "9", "visible", "true");//LoanAssessment
            ifr.setTabStyle("tab1", "14", "visible", "true");//Disbursement
            ifr.setTabStyle("tab1", "8", "visible", "true");//Collateral
            ifr.setTabStyle("tab1", "19", "visible", "false");//E-signTab
            ifr.setTabStyle("tab1", "16", "visible", "false");//E-sign Check
            ifr.setColumnVisible("ALV_INPRINCIPLE_ELIGIBILITY", "8", false);
            String visibleSectionsGrids = "F_CoborrowerEligibility_Sec,F_FeeCharges,F_SourcingInfo,F_Fin_Summary,ALV_AL_LIAB_VAL,ALV_AL_ASSET_DET,ALV_AL_NETWORTH,ALV_PL_MExpense,F_Fin_Summary,F_InPrincipleEligibility,F_OutwardDocument,LV_KYC,LV_MIS_Data,F_FinalEligibility,F_InPrincipleEligibility,ALV_SCORERATING,ALV_Deviations,F_Deviation,F_ESIGN_ID";
            pcm.controlvisiblity(ifr, visibleSectionsGrids);
            String nonVisibleSectionGrids = "D_BENEFICIARY_DETAILS,BTN_FetchScoreRating,F_ESIGN_ID,F_CollateralDetails,F_LoanAssess,F_TermsConditions,F_E-Sign,F_Sign_Document,F_DAMC_DOCUMENT,ALV_UPLOAD_DOCUMENT1,QL_SOURCINGINFO_BranchCode,QL_SOURCINGINFO_Branch,QL_SOURCINGINFO_CPC,QL_SOURCINGINFO_TEST,CTRID_PD_FETCHEXTCUST,BTN_Dedupe_Click,CTRID_PD_RESETDET,DD_CB_BUREAU_ID,QL_RISK_RATING_RiskScore,QL_RISK_RATING_Rank,F_Deviation";
            pcm.controlinvisiblity(ifr, nonVisibleSectionGrids);
            //Disbursment Tab field visibility
            String DisbursmentVisibleFields = "QNL_DISBURSMENT_ProbableDateofDisbursement,QNL_DISBURSMENT_ProposedLoanAmount,QNL_DISBURSMENT_InstallmentAmount,QNL_DISBURSMENT_NoofInstallment,QNL_DISBURSMENT_RepaymentType,QNL_DISBURSMENT_OtherFeeAndCharges,QNL_DISBURSMENT_DateofDisbursement,QNL_DISBURSMENT_DateofInprinciplelettergeneration,QNL_DISBURSMENT_FixedTerm,QNL_DISBURSMENT_ProposedSanctionAuthority,QNL_DISBURSMENT_Drawdown,QNL_DISBURSMENT_CustomerID,QNL_DISBURSMENT_SalaryAccountnumber,QNL_DISBURSMENT_CustomerName";
            pcm.controlvisiblity(ifr, DisbursmentVisibleFields);
            String DisbursmentHideFields = "datepick474,combo1737,datepick475";
            pcm.controlinvisiblity(ifr, DisbursmentHideFields);
            ifr.setColumnVisible("ALV_SCORERATING", "2", false);
            ifr.setColumnVisible("ALV_SCORERATING", "3", false);
            ifr.setColumnVisible("ALV_SCORERATING", "4", false);
            ifr.setColumnVisible("ALV_SCORERATING", "5", false);
            ifr.setColumnVisible("ALV_SCORERATING", "6", false);
            ifr.setStyle("F_ESIGN_ID", "visible", "false");
            Log.consoleLog(ifr, "visibleSectionsGrids hided" + loan_selected);
            String[] FieldVisibleFalse = new String[]{"QNL_LOS_COLLATERAL_VEHICLES_COLOR", "QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURINGDATE", "QNL_LOS_COLLATERAL_VEHICLES_REGISTRATIONNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_ENGINENUMBER", "QNL_LOS_COLLATERAL_VEHICLES_CHASISNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_INSURANCECOMPANYNAME", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEPOLICYNUMBER",
                "QNL_LOS_COLLATERAL_VEHICLES_AGEOFVEHICLE", "QNL_LOS_COLLATERAL_VEHICLES_ASSETOWNER", "QNL_LOS_COLLATERAL_VEHICLES_DEALERCODE",
                "QNL_LOS_COLLATERAL_VEHICLES_DEALERADDRESS",
                "QNL_LOS_COLLATERAL_VEHICLES_EX_SHOWROOMPRICE", "QNL_LOS_COLLATERAL_VEHICLES_INSURANCEAMOUNT",
                "QNL_LOS_COLLATERAL_VEHICLES_LTT_REGISTRATIONCHARGES", "QNL_LOS_COLLATERAL_VEHICLES_OTHERCHARGES",
                "QNL_LOS_COLLATERAL_VEHICLES_ROADTAX", "QNL_LOS_COLLATERAL_VEHICLES_ONROADPRICE", "QNL_LOS_COLLATERAL_VEHICLES_MARGIN",
                "QNL_LOS_COLLATERAL_VEHICLES_PERMISSIBLEVALUE", "VL_Fetch_Margin", "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONBY",
                "QNL_LOS_COLLATERAL_VEHICLES_VALUATIONON", "Vehicle_Title", "QNL_LOS_COLLATERAL_VEHICLES_SECURITYTYPE", "VL_Transmission_Type",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_CommunicationAddress", "Same_As_Permanent_Address", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_LandMark",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Ownership", "BTN_Address_Fetch", "Copy_Address_From", "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Area_Zone",
                "QNL_BASIC_INFO_CNL_CUST_ADDRESS_District"};
            for (int i = 0; i < FieldVisibleFalse.length; i++) {
                ifr.setStyle(FieldVisibleFalse[i], "visible", "false");
                Log.consoleLog(ifr, "FieldVisibleFalse[i]" + FieldVisibleFalse[i]);
            }
            String collateralDisable = "QNL_BASIC_INFO_CNL_CUST_ADDRESS_Country,QNL_BASIC_INFO_CNL_CUST_ADDRESS_State,QNL_LOS_COLLATERAL_VEHICLES_FUELTYPE,QNL_LOS_COLLATERAL_VEHICLES_VEHICLETYPE,QNL_LOS_COLLATERAL_VEHICLES_ASSETMANUFACTURER,QNL_LOS_COLLATERAL_VEHICLES_ASSETMODEL,QNL_LOS_COLLATERAL_VEHICLES_VEHICLEVARIANT,QNL_LOS_COLLATERAL_VEHICLES_VEHICLECOST,QNL_LOS_COLLATERAL_VEHICLES_DOWNPAYMENTAMT,QNL_LOS_COLLATERAL_VEHICLES_CATEGORY,QNL_LOS_COLLATERAL_VEHICLES_MANUFACTURERYEAR,QNL_LOS_COLLATERAL_VEHICLES_DEALERNAME,QNL_LOS_COLLATERAL_VEHICLES_EXPECTEDDELIVERYDATE";
            pcm.controlDisable(ifr, collateralDisable);
            Log.consoleLog(ifr, " DisbursementMaker VL Code End");
        }
    }

    public String onClickESignInitiate(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside onClickESignInitiate");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            ifr.setStyle("ESign_Initaite", "disable", "true");
            ifr.setStyle("E-Sign_initiated", "disable", "false");
            ifr.setStyle("textbox6939 ", "visible", "false");
            String esignStatus = ifr.getValue("E-Sign_initiated").toString();
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            String loan_selected = loanSelected.get(0).get(0);
            Log.consoleLog(ifr, "loan type==>" + loan_selected);
            String subProduct = "";
            if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension")) {
                subProduct = pcm.mGetSubProductCode(ifr);
                Log.consoleLog(ifr, "subProductCode:" + subProduct);
            }
            if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
                String Purpose = null;
                String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
                List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery, "Execute query for fetching Purpose data from portal");
                if (PurposePortal.size() > 0) {
                    Purpose = PurposePortal.get(0).get(0);
                }
                subProduct = pcm.mGetSubproductVL(ifr, Purpose);
                Log.consoleLog(ifr, "subProductCode:" + subProduct);
            }
            Log.consoleLog(ifr, "subProduct :::" + subProduct);
            EsignIntegrationChannel NESL = new EsignIntegrationChannel();
            //String returnMessage = NESL.redirectNESLRequest(ifr, subProduct, "eSigning");
//            Log.consoleLog(ifr, "returnMessage from NESL :" + returnMessage);
//            if ((returnMessage.contains(RLOS_Constants.ERROR)) || (returnMessage.equalsIgnoreCase(""))
//                    || returnMessage.contains("showMessage")) {
//                return pcm.returnErrorAPIThroughExecute(ifr);
//            }
            SimpleDateFormat sdf = new SimpleDateFormat(ConfProperty.getCommonPropertyValue("AvailableDateTime"));
            String currDate = sdf.format(new Date());
            Log.consoleLog(ifr, "Current Date Time :::" + currDate);
            String documentName = "NESL_1_eSigning";
            String e_signStatus = "Initiated";
            String queryData = ConfProperty.getQueryScript("BorrowerNameQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId);
            List<List<String>> data = cf.mExecuteQuery(ifr, queryData, "Execute query for fetching customer data");
            String party_type = data.get(0).get(0);
            Log.consoleLog(ifr, "Party Type==>" + party_type);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("QNL_DOC_ESIGN_STATUS_DocumentName", documentName);
            jsonObj.put("QNL_DOC_ESIGN_STATUS_initiatedDate", currDate);
            jsonObj.put("QNL_DOC_ESIGN_STATUS_Status", e_signStatus);
            jsonObj.put("QNL_DOC_ESIGN_STATUS_PartyType", party_type);
            jsonObj.put("QNL_DOC_ESIGN_STATUS_ESignMode", esignStatus);
            JSONArray jsonArrayObj = new JSONArray();
            jsonArrayObj.add(jsonObj);
            Log.consoleLog(ifr, "jsonObjArr" + jsonArrayObj.toString());
            int esignCount = ifr.getDataFromGrid("ALV_ESIGN").size();
            Log.consoleLog(ifr, "esignCount :: " + esignCount);
            if (esignCount == 0) {
                Log.consoleLog(ifr, " inside else esignCount = 0:: ");
                ifr.addDataToGrid("ALV_ESIGN", jsonArrayObj, true);
                //ifr.setStyle("ALV_ESIGN", "disable", "true");
            } else {
                Log.consoleLog(ifr, " inside else esignCount :: ");
                ifr.clearTable("ALV_ESIGN");
                ifr.addDataToGrid("ALV_ESIGN", jsonArrayObj, true);
                //ifr.setStyle("ALV_ESIGN", "disable", "true");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onChangeESignStatus ::: General Contract " + e);
            Log.errorLog(ifr, "Exception in onChangeESignStatus ::: General Contract " + e);
        }
        return "";
    }

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
                onChangeRecommendedAmountFinalEligibilityVL(ifr);

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

    public String calclateEMIOnTenureChangeVL(IFormReference ifr) {
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
            String schemeID = pcm.getBaseSchemeID(ifr, ProcessInstanceId);
            Log.consoleLog(ifr, "schemeID:" + schemeID);
            String tenureData_Query = ConfProperty.getQueryScript("getTenureDataQuery").replaceAll("#schemeID#", schemeID);
            List<List<String>> MaxMinLoanTenureList = cf.mExecuteQuery(ifr, tenureData_Query, "MinMax tenure Data from LoanInfo Query : ");
            if (MaxMinLoanTenureList.size() > 0) {
                MaxLoanTenure = MaxMinLoanTenureList.get(0).get(0);
                MinLoanTenure = MaxMinLoanTenureList.get(0).get(1);
            }
            Log.consoleLog(ifr, "Max LoanTenure : " + MaxLoanTenure);
            Log.consoleLog(ifr, "Min LoanTenure : " + MinLoanTenure);
            recommendeLoanAmount = ifr.getValue("QNL_LA_FINALELIGIBILITY_RecommendedLoanAmount").toString();
            Log.consoleLog(ifr, "FINALELIGIBILITY recommendeLoanAmount==>" + recommendeLoanAmount);
            if (!recommendeLoanAmount.isEmpty()) {
                if (!RecommendloanTenure.equalsIgnoreCase("")) {
                    Log.consoleLog(ifr, "MaxLoanTenure : " + MaxLoanTenure);

                    if (RecommendloanTenureCheck <= Double.parseDouble(MaxLoanTenure)) {
                        Log.consoleLog(ifr, "RecommendloanTenure is less than MaxLoanTenure: " + RecommendloanTenure);

                        recommendeLoanAmount = "-" + recommendeLoanAmount;
                        BigDecimal loanroii = new BigDecimal(loanROI);

                        BigDecimal emi = pcm.calculateEMIPMT(ifr, recommendeLoanAmount, loanroii, Integer.parseInt(RecommendloanTenure));
                        Log.consoleLog(ifr, "emi : " + emi);
                        String EMI = emi.toString();
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_EMIRECCOMENDEDLOANAMOUNT", EMI);
                    } else {
                        ifr.setValue("QNL_LA_FINALELIGIBILITY_Tenure", "");
                        JSONObject message = new JSONObject();
                        message.put("showMessage", cf.showMessage(ifr, "QNL_LA_FINALELIGIBILITY_Tenure", "error", "Recommended Tenure cannot be greater than Maximum Tenure as per product."));
                        return message.toString();
                    }
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
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "caluclateEMI" + e);
            return "error";
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    public void onLoadESign(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside VLBkoffcCustomCode --> OnLoadESignDetails : ");
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        String ActivityName = ifr.getActivityName();
        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
        String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
        List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
        String loan_selected = loanSelected.get(0).get(0);
        Log.consoleLog(ifr, "loan type==>" + loan_selected);
        if (loan_selected.equalsIgnoreCase("Canara Budget") || loan_selected.equalsIgnoreCase("Canara Pension") || loan_selected.equalsIgnoreCase("Vehicle Loan")) {
            Log.consoleLog(ifr, "Inside VLBkoffcCustomCode --> OnLoadESignDetails --> fieldVisibility : ");
            String visibleFields = "QNL_DOC_ESIGN_STATUS_DocumentName,QNL_DOC_ESIGN_STATUS_initiatedDate,QNL_DOC_ESIGN_STATUS_Status,QNL_DOC_ESIGN_STATUS_EsignCompletedDate,QNL_DOC_ESIGN_STATUS_PartyType,QNL_DOC_ESIGN_STATUS_ESignMode";
            pcm.controlvisiblity(ifr, visibleFields);
            pcm.controlDisable(ifr, visibleFields);
            String InvisibleFields = "table689_button383,table689_button384,table689_button385,table689_button386,table689_textbox7012";
            pcm.controlinvisiblity(ifr, InvisibleFields);
            String ESignStatus = ifr.getValue("QNL_DOC_ESIGN_STATUS_Status").toString();
            Log.consoleLog(ifr, "ESignStatus==>" + ESignStatus);
            if (ESignStatus.equalsIgnoreCase("Manual")) {
                ifr.setStyle("Btn_Upload", "visible", "true");
                ifr.setStyle("Btn_Upload", "disable", "false");
            }else{
                ifr.setStyle("Btn_Upload", "visible", "false");
            }
        }
    }
}
