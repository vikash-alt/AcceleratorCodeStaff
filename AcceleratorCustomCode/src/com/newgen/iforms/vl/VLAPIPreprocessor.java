/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.vl;

import com.newgen.dlp.integration.cbs.Collateral;
import com.newgen.dlp.integration.cbs.CreditRating;
import com.newgen.dlp.integration.cbs.FundTransfer;
import com.newgen.dlp.integration.cbs.LoanAccountCreation;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.acceleratorCode.AcceleratorBaseCode;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class VLAPIPreprocessor {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();

    Collateral objCT = new Collateral();
    LoanAccountCreation objLAC = new LoanAccountCreation();
    FundTransfer objFT = new FundTransfer();
    CreditRating objCR = new CreditRating();

    public String execCollateral(IFormReference ifr, String journeyType) {

        Log.errorLog(ifr, "APIPreprocessor:execCollateral-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            String apiCriterion = "'CBS_Collateral'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);

            if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                Log.consoleLog(ifr, "Collateral Id already created");
//                return "ERROR: Collateral Id already created";
                return "Collateral Id already created";
            } else {
				String customerId = pcm.getCustomerIDCB(ifr, "B");
				String collateralId = objCT.getCollateral(ifr, customerId);
				Log.consoleLog(ifr, "Collateral Id :" + collateralId);
                return collateralId;
            }
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Exception in  execCollateral" + e.getMessage());
            Log.errorLog(ifr, "Exception in  execCollateral" + e.getMessage());
        }
        return RLOS_Constants.ERROR;

    }

    public String execLoanAccountCreation(IFormReference ifr, String journeyType) throws ParseException {
        Log.errorLog(ifr, "APIPreprocessor:execLoanAccountCreation-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String loanAccountNumber = "";
        try {

            String apiCriterion = "'CBS_LoanAccountCreation'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            

            if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                Log.consoleLog(ifr, "LoanAccNumber already available");
                String queryLoanAccNumber = "";
                    queryLoanAccNumber = "SELECT LOAN_ACCOUNTNO FROM slos_trn_loandetails "
                            + "WHERE WINAME='" + processInstanceId + "' AND ROWNUM=1";
                    Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + queryLoanAccNumber);

                
                List<List<String>> Result = cf.mExecuteQuery(ifr, queryLoanAccNumber, "LoanAccountAvl_Query:");
                if (!Result.isEmpty()) {
                    loanAccountNumber = Result.get(0).get(0);
                    Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
                    ifr.setStyle("LAC_LoanAcc_Num_VL","disable","true");
                    return "ERROR: Loan account already created Kindly check in CBS";
                    
                }

            } else {
                Log.consoleLog(ifr, "LoanAccNumber not available");
                try {

                    String ProductCode = "";//Need to change as oer the final requets
                    String CustomerId = "";
                    String LoanAmount = "";
                    String Tenure = "";

                        CustomerId  = pcm.getCustomerIDCB(ifr, "B");
                        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

                        String queryBranchCodeAndVehicleCat = "select APP_LOAN_AMT_VL,APP_LOAN_TENURE_VL from SLOS_STAFF_TRN "
        						+ "where WINAME ='" + processInstanceId + "'";
        				List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);
        				Log.consoleLog(ifr, "queryBranchCodeAndVehicleCat==> " + queryBranchCodeAndVehicleCat);
        				if (!list.isEmpty()) {
        					LoanAmount = list.get(0).get(0);
        					Tenure=list.get(0).get(1);
        				}
                    
                    Date currentDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    calendar.add(Calendar.DAY_OF_YEAR, 90);
                    Date dateAfter90Days = calendar.getTime();
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
                    String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);

                    loanAccountNumber = objLAC.getLoanAccountDetails(ifr, processInstanceId, ProductCode,
                            CustomerId, LoanAmount, Tenure, SanctionExpiryDate, journeyType);
                    Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
                   // String[] loanAccountNumberArr = loanAccountNumber.split(":");

					if (loanAccountNumber.contains(RLOS_Constants.ERROR)) {
						return loanAccountNumber;
					}
                    
//                    
//                    if (!loanAccountNumber.contains("SUCCESS")) {
//                        return RLOS_Constants.ERROR;
//                    }
                    
                    String productCOde="";
                    String loanAccountNum="";
                    String accountCreatedDate="";
                	String queryForProductCode = "SELECT prod_scheme_desc from slos_staff_trn where winame='"
            				+ processInstanceId + "'";
            		List<List<String>> queForProCodeRes = ifr.getDataFromDB(queryForProductCode);
            		Log.consoleLog(ifr, "queForProCodeRes : " + queForProCodeRes);
            		if(!queForProCodeRes.isEmpty())
            		{
            			productCOde=queForProCodeRes.get(0).get(0);
            		}
            		String queryForLoanAccountNumber = "SELECT LOAN_ACCOUNTNO,ACCOUNT_CREATEDDATE from SLOS_TRN_LOANDETAILS where PID='"
            				+ processInstanceId + "'";
            		List<List<String>> queryForLoanAccountNumberRes = ifr.getDataFromDB(queryForLoanAccountNumber);
            		Log.consoleLog(ifr, "queryForLoanAccountNumberRes : " + queryForLoanAccountNumberRes);
            		if(!queryForLoanAccountNumberRes.isEmpty())
            		{
            			loanAccountNum=queryForLoanAccountNumberRes.get(0).get(0);
            			if(loanAccountNum.trim().isEmpty() || loanAccountNum.isBlank())
            			{
            				return "ERROR:" + "Technical Glitch";
            			}
            			accountCreatedDate=queryForLoanAccountNumberRes.get(0).get(1);
            		}
            		String formaDate = getCurrentAPIDate(ifr);
                    ifr.setStyle("Acc_Open_Date_VL","visible","true");
					ifr.setStyle("Product_VL_LAC","visible","true");
					ifr.setStyle("LAC_LoanAcc_Num_VL","visible","true");
					ifr.setValue("Product_VL_LAC", productCOde);
					ifr.setValue("LAC_LoanAcc_Num_VL", loanAccountNum);
					ifr.setValue("Acc_Open_Date_VL", accountCreatedDate);
                    return RLOS_Constants.SUCCESS;
                } catch (Exception e) {
                    Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                    Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
            Log.errorLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String getCurrentAPIDate(IFormReference ifr) {
        Date d = new Date();
        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
        String APIDate = sd.format(d);
        Log.consoleLog(ifr, "APIDate:" + APIDate);
        return APIDate;
    }

	public String execCreditRating(IFormReference ifr, String loanAccountNumber, String journeyType) {

        Log.errorLog(ifr, "APIPreprocessor:execLoanAccountCreation-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        try {

            // String apiCriterion = "'CBS_Collateral','CBS_LoanAccountCreation','CBS_InquireInternalCreditRating'";
            String apiCriterion = "'CBS_Collateral','CBS_LoanAccountCreation','CBS_MaintainInternalCreditRating'";

            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
                String customerId = pcm.getCustomerIDCB(ifr, "B");
                JSONObject objCredMain = new JSONObject();
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
                String SancDate = AcceleratorBaseCode.SanctionDate;
                Date currentDate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                SancDate = dateFormat2.format(calendar.getTime());
                calendar.add(Calendar.MONTH, 15);
                Date dateAfter90Days = calendar.getTime();

                String DateOfCreditExpiry = dateFormat2.format(dateAfter90Days);
                String query = ConfProperty.getQueryScript("GETCRGVALUE").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());

                Log.consoleLog(ifr, "Inside To fetch grade:" + query);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Fetching Roi CANARARETAILGRADE  ");

//Modified by Ahmed on 19-07-2024                
//                String CANARARETAILGRADE = "";
//                String RiskCategory = "";
//                String InternalCreditGrade = "";
//                if (result.size() > 0) {
//                    CANARARETAILGRADE = result.get(0).get(0);
//                }
//                
//                
//                if (CANARARETAILGRADE.equalsIgnoreCase("CRG-Prime")) {
//                    RiskCategory = "3";
//                    InternalCreditGrade = "13";
//                } else if (CANARARETAILGRADE.equalsIgnoreCase("CRG-1")) {
//                    RiskCategory = "4";
//                    InternalCreditGrade = "14";
//                } else if (CANARARETAILGRADE.equalsIgnoreCase("CRG-2")) {
//                    RiskCategory = "6";
//                    InternalCreditGrade = "15";
//                } else if (CANARARETAILGRADE.equalsIgnoreCase("CRG-3")) {
//                    RiskCategory = "7";
//                    InternalCreditGrade = "16";
//                } else if (CANARARETAILGRADE.equalsIgnoreCase("CRG-4")) {
//                    RiskCategory = "9";
//                    InternalCreditGrade = "17";
//                }
//                
                String productCode = pcm.mGetProductCode(ifr);
                String subProductCode = pcm.mGetSubProductCode(ifr);
                Log.consoleLog(ifr, "productCode=======>" + productCode);
                Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
                String canaraRetailGrade = "";
                if (!result.isEmpty()) {
                    canaraRetailGrade = result.get(0).get(0);
                }
                Log.consoleLog(ifr, "canaraRetailGrade==>" + canaraRetailGrade);
                String riskCategory = pcm.getParamConfig(ifr, productCode, subProductCode, canaraRetailGrade, "RISKCATEGORY");
                String internalCreditGrade = pcm.getParamConfig(ifr, productCode, subProductCode, canaraRetailGrade, "CREDITGRADE");

                String action = pcm.getParamConfig(ifr, productCode, subProductCode, "BA188MODIFY", "ACTION");
                String internalRatingSrl = pcm.getParamConfig(ifr, productCode, subProductCode, "BA188MODIFY", "InternalRatingSrl");
                String ratingModel = pcm.getParamConfig(ifr, productCode, subProductCode, "BA188MODIFY", "RatingModel");
                String isThisRatingDowngrade = pcm.getParamConfig(ifr, productCode, subProductCode, "BA188MODIFY", "IsThisRatingDowngrade");

                objCredMain.put("AccountNo", loanAccountNumber);
                objCredMain.put("Action", action);
                objCredMain.put("InternalRatingSrl", internalRatingSrl);
                objCredMain.put("RatingModel", ratingModel);
                objCredMain.put("InternalCreditGrade", internalCreditGrade);
                objCredMain.put("YearOfABS", "");
                objCredMain.put("DateOfCreditRating", SancDate);
                objCredMain.put("IsThisRatingDowngrade", isThisRatingDowngrade);
                objCredMain.put("DateOfSigningBalanceSheet", SancDate);
                objCredMain.put("RiskCategory", riskCategory);
              objCredMain.put("DateOfCreditExpiry", DateOfCreditExpiry);

                String creditRatingStatus = objCR.maintainInternalCreditRating(ifr, customerId, journeyType, objCredMain);
                if (!creditRatingStatus.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.SUCCESS;
                }
            } else {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  execCreditRating" + e.getMessage());
            Log.errorLog(ifr, "Exception in  execCreditRating" + e.getMessage());
        }
        return RLOS_Constants.ERROR;

    }

	public String execCollateralHL(IFormReference ifr, String journeyType) {
		 Log.errorLog(ifr, "APIPreprocessor:execCollateral-> started for JourneyType=>" + journeyType);
	        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

	        try {

	            String apiCriterion = "'CBS_Collateral'";
	            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);

	            if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
	                Log.consoleLog(ifr, "Collateral Id already created");
//	                return "ERROR: Collateral Id already created";
	                return "Collateral Id already created";
	            } else {
	                String customerId = pcm.getCustomerIDCB(ifr, "B");
	                String collateralId = objCT.getMultiCollateral(ifr, customerId);
	                Log.consoleLog(ifr, "Collateral Id :" + collateralId);
	                return collateralId;
	            }
	        } catch (ParseException e) {
	            Log.consoleLog(ifr, "Exception in  execCollateral" + e.getMessage());
	            Log.errorLog(ifr, "Exception in  execCollateral" + e.getMessage());
	        }
			return RLOS_Constants.ERROR;
	       
	}

	public String execLoanAccountCreationHL(IFormReference ifr, String journeyType) {

        Log.consoleLog(ifr, "APIPreprocessor:execLoanAccountCreation-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String loanAccountNumber = "";
        try {

            String apiCriterion = "'CBS_LoanAccountCreation'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);

            if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                Log.consoleLog(ifr, "LoanAccNumber already available");
                String queryLoanAccNumber = "";
                if (journeyType.equalsIgnoreCase("PAPL")) {
                    queryLoanAccNumber = "SELECT LOAN_ACCOUNT_NO FROM LOS_T_IBPS_LOAN_DETAILS "
                            + "WHERE WINAME='" + processInstanceId + "' AND ROWNUM=1";
                    Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>PAPL::::" + queryLoanAccNumber);

                } else {
                    queryLoanAccNumber = "SELECT LOAN_ACCOUNTNO FROM los_trn_loandetails "
                            + "WHERE PID='" + processInstanceId + "' AND ROWNUM=1";
                    Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + queryLoanAccNumber);

                }
                List<List<String>> result = cf.mExecuteQuery(ifr, queryLoanAccNumber, "LoanAccountAvl_Query:");
                if (!result.isEmpty() && result.get(0).get(0) != null && !result.get(0).get(0).isEmpty()) {
               	    loanAccountNumber = result.get(0).get(0);
               	    Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
               	    return "ERROR: Loan account already created Kindly check in CBS";
               }

            } else {
                Log.consoleLog(ifr, "LoanAccNumber not available");
                try {

                    String ProductCode = "";//Need to change as oer the final requets
                    String CustomerId = "";
                    String LoanAmount = "";
                    String Tenure = "";
                    if (journeyType.equalsIgnoreCase("PAPL")) {

                        CustomerId = pcm.getCustomerIDPAPL(ifr);
                        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
                        String Query = "SELECT loanamount,tenure FROM los_tran_papl_finaleligibility"
                                + " WHERE winame='" + processInstanceId + "'";
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Query:");

                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                        }
                    }
                    else {

                        CustomerId = pcm.getCustomerIDForOtherProducts(ifr);
                        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

                        String Query = "Select APP_LOAN_AMT_VL,APP_LOAN_TENURE_VL from slos_staff_trn WHERE winame='"+processInstanceId+"'";
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                            Log.consoleLog(ifr, "Loan amount:: "+LoanAmount);
                            Log.consoleLog(ifr, "Loan Tenure :: "+Tenure);
                        }
                    
                    }

                    Date currentDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    calendar.add(Calendar.DAY_OF_YEAR, 90);
                    Date dateAfter90Days = calendar.getTime();
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
                    String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);
                	String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
            				+ processInstanceId + "'";
            		Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
            		List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
            		Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
            		if (!listqueryproductandScheduleCode.isEmpty()) {
            			ProductCode = listqueryproductandScheduleCode.get(0).get(0);
            		
            		}
                    
                    Log.consoleLog(ifr, "ProductCode==>" + ProductCode+"---CustomerId==>" + CustomerId+"----------LoanAmount==>"+LoanAmount+"----------" +
                            "Tenure==>"+Tenure+"---------------------SanctionExpiryDate==>"+SanctionExpiryDate);
                    loanAccountNumber = objLAC.getLoanAccountDetailsHL(ifr, processInstanceId, ProductCode,
                            CustomerId, LoanAmount, Tenure, SanctionExpiryDate, journeyType);
                    Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
                    
                    if (loanAccountNumber != null 
                            && loanAccountNumber.startsWith(RLOS_Constants.ERROR)) {
                    	return loanAccountNumber;
                    }
                    
                    String productCOde="";
                    String loanAccountNum="";
                	String queryForProductCode = "SELECT HL_PRODUCT from slos_staff_home_trn where winame='"
            				+ processInstanceId + "'";
            		List<List<String>> queForProCodeRes = ifr.getDataFromDB(queryForProductCode);
            		Log.consoleLog(ifr, "queForProCodeRes : " + queForProCodeRes);
            		if(!queForProCodeRes.isEmpty())
            		{
            			productCOde=queForProCodeRes.get(0).get(0);
            		}
            		String queryForLoanAccountNumber = "SELECT LOAN_ACCOUNTNO from SLOS_TRN_LOANDETAILS where PID='"
            				+ processInstanceId + "'";
            		List<List<String>> queryForLoanAccountNumberRes = ifr.getDataFromDB(queryForLoanAccountNumber);
            		Log.consoleLog(ifr, "queryForLoanAccountNumberRes : " + queryForLoanAccountNumberRes);
            		if(!queryForLoanAccountNumberRes.isEmpty())
            		{
            			loanAccountNum=queryForLoanAccountNumberRes.get(0).get(0);
            		}
            		String formaDate = getCurrentAPIDate(ifr);
                    ifr.setStyle("Acc_Open_Date_VL","visible","true");
					ifr.setStyle("Product_VL_LAC","visible","true");
					ifr.setStyle("LAC_LoanAcc_Num_VL","visible","true");
					ifr.setValue("Product_VL_LAC", productCOde);
					ifr.setValue("LAC_LoanAcc_Num_VL", loanAccountNum);
					ifr.setValue("Acc_Open_Date_VL", formaDate);
                    return loanAccountNumber;

                } catch (Exception e) {
                    Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                    Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
            Log.errorLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    
	}

   
}