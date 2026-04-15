/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.common;

import com.newgen.dlp.integration.cbs.BranchDisbursement;
import com.newgen.dlp.integration.cbs.LoanAccountCreation;
import com.newgen.dlp.integration.cbs.DisbursementEnquiry;
import com.newgen.dlp.integration.cbs.FundTransfer;
import com.newgen.dlp.integration.cbs.LoanDeduction;
import com.newgen.dlp.integration.cbs.LoanSchedule;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author ahmed.zindha
 */
public class APIPreprocessor {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    LoanAccountCreation objLAC = new LoanAccountCreation();
    DisbursementEnquiry objDE = new DisbursementEnquiry();
    LoanDeduction objLD = new LoanDeduction();
    LoanSchedule objLS = new LoanSchedule();
    BranchDisbursement objBD = new BranchDisbursement();
    FundTransfer objFT = new FundTransfer();

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
                if (journeyType.equalsIgnoreCase("PAPL")) {
//Modified by Ahmed on 24-07-2024 for queryReadingFromProp File
//                    queryLoanAccNumber = "SELECT LOAN_ACCOUNT_NO FROM LOS_T_IBPS_LOAN_DETAILS "
//                            + "WHERE WINAME='" + processInstanceId + "' AND ROWNUM=1";
//                    Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>PAPL::::" + queryLoanAccNumber);
                    queryLoanAccNumber = ConfProperty.getQueryScript("PAPL_GETLOANACCNOQRY")
                            .replace("#WINAME#", processInstanceId);
                    Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:queryLoanAccNumber->" + queryLoanAccNumber);

                } else {
//                    queryLoanAccNumber = "SELECT LOAN_ACCOUNTNO FROM los_trn_loandetails "
//                            + "WHERE PID='" + processInstanceId + "' AND ROWNUM=1";
//                    Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + queryLoanAccNumber);

                    queryLoanAccNumber = ConfProperty.getQueryScript("GETLOANACCNOQRY")
                            .replace("#WINAME#", processInstanceId);
                    Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:queryLoanAccNumber->" + queryLoanAccNumber);
                }
                List<List<String>> Result = cf.mExecuteQuery(ifr, queryLoanAccNumber, "LoanAccountAvl_Query:");
                if (!Result.isEmpty()) {
                    loanAccountNumber = Result.get(0).get(0);
                    Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
                    return loanAccountNumber;
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
                        //Modified by Ahmed on 24-07-2024 for queryReadingFromProp File
//                        String Query = "SELECT loanamount,tenure FROM los_tran_papl_finaleligibility"
//                                + " WHERE winame='" + processInstanceId + "'";
                        String Query = ConfProperty.getQueryScript("PAPL_GETLOANDTLSQRY")
                                .replace("#WINAME#", processInstanceId);
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Query:");

                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                        }
                    } else {

                        CustomerId = pcm.getCustomerIDForOtherProducts(ifr);
                        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

                        String Query = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#", processInstanceId);
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                        }
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
                    if (loanAccountNumber.equalsIgnoreCase("")) {
                        return RLOS_Constants.ERROR;
                    }

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

    public String execDisbursementEnquiry(IFormReference ifr, String loanAccountNumber, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execDisbursementEnquiry-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule',"
                    + "'CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);
            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
                String sanctionAmount = objDE.updateCBSDisbursementEnquiry(ifr, processInstanceId, loanAccountNumber, "", journeyType);
                return sanctionAmount;
            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
            Log.errorLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execLoanDeduction(IFormReference ifr, String loanAccountNumber, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execLoanDeduction-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {

                String querySanctionAmount = "";
                if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                    if (journeyType.equalsIgnoreCase("PAPL")) {
                        //Modified by Ahmed on 24-07-2024 for queryReadingFromProp File

//                        querySanctionAmount = "select SANCTION_AMOUNT from LOS_T_IBPS_LOAN_DETAILS WHERE"
//                                + " winame='" + processInstanceId + "' and rownum=1";
                        querySanctionAmount = ConfProperty.getQueryScript("PAPL_GETSANCAMNTQRY")
                                .replace("#WINAME#", processInstanceId);
                        Log.consoleLog(ifr, "APIPreprocessor:execLoanDeduction:querySanctionAmount->" + querySanctionAmount);
                    } else {
                        querySanctionAmount = "select SANCTION_AMOUNT from los_trn_loandetails WHERE"
                                + " PID='" + processInstanceId + "' and rownum=1";
                    }
                } else {
                    querySanctionAmount = ConfProperty.getQueryScript("SANCTIONAMOUNTQUERY").replaceAll("#WINAME#", processInstanceId);
                }
                List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
                String SanctionAmount = "";
                if (!result.isEmpty()) {
                    SanctionAmount = result.get(0).get(0);
                }
                String Status = objLD.getLoanDeductionDetails(ifr, processInstanceId, loanAccountNumber, SanctionAmount, journeyType);
                Log.consoleLog(ifr, "Status" + Status);
                return Status;

            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  loanDedDetails" + e.getMessage());
            Log.errorLog(ifr, "Exception in  loanDedDetails" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execComputeLoanSchedule(IFormReference ifr, String loanAccountNumber, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execComputeLoanSchedule-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
                String querySanctionAmount = "";
                if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                    if (journeyType.equalsIgnoreCase("PAPL")) {
                        querySanctionAmount = "select SANCTION_AMOUNT from LOS_T_IBPS_LOAN_DETAILS WHERE"
                                + " winame='" + processInstanceId + "' and rownum=1";
                    } else {
                        querySanctionAmount = "select SANCTION_AMOUNT from los_trn_loandetails WHERE"
                                + " PID='" + processInstanceId + "' and rownum=1";
                    }
                } else {
                    querySanctionAmount = ConfProperty.getQueryScript("SANCTIONAMOUNTQUERY").replaceAll("#WINAME#", processInstanceId);
                }
                List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
                String SanctionAmount = "";
                if (!result.isEmpty()) {
                    SanctionAmount = result.get(0).get(0);
                }
                String SessionId = objLS.computeLoanSchedule(ifr, processInstanceId, loanAccountNumber, SanctionAmount, journeyType);

                if (SessionId.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                } else {
                    return SessionId;
                }

            } else {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  CBS_ComputeLoanSchedule:" + e.getMessage());
            Log.errorLog(ifr, "Exception in  CBS_ComputeLoanSchedule:" + e.getMessage());
        }
        return RLOS_Constants.ERROR;

    }

    public String execGenerateLoanSchedule(IFormReference ifr, String loanAccountNumber, String SessionId, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execGenerateLoanSchedule-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
                String Status = objLS.generateLoanSchedule(ifr, processInstanceId, loanAccountNumber, SessionId, journeyType);
                Log.consoleLog(ifr, "Status==>" + Status);
                return Status;
            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  CBS_GenerateLoanSchedule:" + e.getMessage());
            Log.errorLog(ifr, "Exception in  CBS_GenerateLoanSchedule:" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execSaveLoanSchedule(IFormReference ifr, String loanAccountNumber, String SessionId, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execSaveLoanSchedule-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
                String querySanctionAmount = "";
                if (ifr.getActivityName().equalsIgnoreCase("Portal")) {
                    if (journeyType.equalsIgnoreCase("PAPL")) {
                        querySanctionAmount = "select SANCTION_AMOUNT from LOS_T_IBPS_LOAN_DETAILS WHERE"
                                + " winame='" + processInstanceId + "' and rownum=1";
                    } else {
                        querySanctionAmount = "select SANCTION_AMOUNT from los_trn_loandetails WHERE"
                                + " PID='" + processInstanceId + "' and rownum=1";
                    }
                } else {
                    querySanctionAmount = ConfProperty.getQueryScript("SANCTIONAMOUNTQUERY").replaceAll("#WINAME#", processInstanceId);
                }
                List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
                String SanctionAmount = "";
                if (!result.isEmpty()) {
                    SanctionAmount = result.get(0).get(0);
                }
                String Status = objLS.updateLoanSchedule(ifr, processInstanceId, loanAccountNumber,
                        SessionId, SanctionAmount, journeyType);
                Log.consoleLog(ifr, "Status===>" + Status);
                return Status;
            } else {
                return RLOS_Constants.SUCCESS;
            }
            // }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  CBS_SaveLoanSchedule" + e.getMessage());
            Log.errorLog(ifr, "Exception in  CBS_SaveLoanSchedule" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execBranchDisbursement(IFormReference ifr, String loanAccountNumber, String journeyType) {
        Log.errorLog(ifr, "APIPreprocessor:execBranchDisbursement-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule','CBS_BranchDisbursement'";
            String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
            Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

            if (executeCBSStatus.equalsIgnoreCase("FAIL")) {

                String LoanAmount = "";
                String Tenure = "";
                String SBACCNUMBER = "";

                if (ifr.getActivityName().equalsIgnoreCase("Portal")) {

                    if (journeyType.equalsIgnoreCase("PAPL")) {
                        String Query = "SELECT loanamount,tenure FROM los_tran_papl_finaleligibility"
                                + " WHERE winame='" + processInstanceId + "'";
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Query:");

                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                        }

                        String SBAccNo = "select SALARYACC_NO from los_mst_papl where mobile_no like ("
                                + "select mobilenumber from LOS_WIREFERENCE_TABLE "
                                + "where winame='" + processInstanceId + "') and rownum=1";
                        Log.consoleLog(ifr, "SBAccNo==>" + SBAccNo);
                        List<List<String>> SBACCNOOutput = ifr.getDataFromDB(SBAccNo);

                        if (!SBACCNOOutput.isEmpty()) {
                            SBACCNUMBER = SBACCNOOutput.get(0).get(0);
                        }
                    } else {

                        String Query = "SELECT LOANAMOUNT,Tenure from LOS_TRN_FINALELIGIBILITY "
                                + "WHERE WINAME='" + processInstanceId + "'";
                        List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);

                        if (!Output3.isEmpty()) {
                            LoanAmount = Output3.get(0).get(0);
                            Tenure = Output3.get(0).get(1);
                        }

                        Log.consoleLog(ifr, "LoanAmount==>" + LoanAmount);
                        Log.consoleLog(ifr, "Tenure==>" + Tenure);

                        String SBAccNo = "select accountid from los_nl_basic_info "
                                + "where pid='" + processInstanceId + "' and rownum=1";
                        Log.consoleLog(ifr, "SBAccNo==>" + SBAccNo);
                        List<List<String>> SBACCNOOutput = ifr.getDataFromDB(SBAccNo);

                        if (!SBACCNOOutput.isEmpty()) {
                            SBACCNUMBER = SBACCNOOutput.get(0).get(0);
                        }
                    }

                } else {
                    String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", processInstanceId);
                    List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "GETLOANDETAILS");

                    if (!Output3.isEmpty()) {
                        LoanAmount = Output3.get(0).get(0);
                        Tenure = Output3.get(0).get(1);
                        SBACCNUMBER = Output3.get(0).get(2);
                    }
                }

                String Status = objBD.updateBranchDisbursement(ifr, processInstanceId, loanAccountNumber, SBACCNUMBER, LoanAmount, journeyType);
                Log.consoleLog(ifr, "Status==>" + Status);
                return Status;
            } else {
                return RLOS_Constants.SUCCESS;
            }
            //}
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
            Log.errorLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execFundTransfer(IFormReference ifr, String loanAccountNumber, String journeyType, String loanAmount, String loantenure) {
        Log.errorLog(ifr, "APIPreprocessor:execFundTransfer-> started for JourneyType=>" + journeyType);
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {

            String schemeId = "";
            String executeCBSStatus = RLOS_Constants.SUCCESS;
            if (journeyType.equalsIgnoreCase("ODAD")) {
                schemeId = pcm.getSchemeIDODAD(ifr);
            } else {
                //Added by monesh on 22/07/2024
                if (journeyType.equalsIgnoreCase("PAPL")) {
                    schemeId = pcm.mGetSchemeID(ifr, processInstanceId);
                } else {
                    schemeId = pcm.getSchemeID(ifr, processInstanceId);
                }
                String apiCriterion = "'CBS_BranchDisbursement'";
                executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
                Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);
            }

            if (executeCBSStatus.equalsIgnoreCase("SUCCESS")) {
                String stateCode = pcm.getStateCode(ifr, journeyType, processInstanceId);//StateCode KA Hardcodes Value removed for Launch on 04-02-2024
//                String Status = objLAC.fundTransfer(ifr, processInstanceId, loanAccountNumber, journeyType, loanAmount,
//                        stateCode, loantenure, schemeId);
                //Log.consoleLog(ifr, "Status==>" + Status);
                return "0";
            } else {
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in  CBS_FundTransfer" + e.getMessage());
            Log.errorLog(ifr, "Exception in  CBS_FundTransfer" + e.getMessage());
        }
        return RLOS_Constants.ERROR;
    }

    public String execLoanAccountCreationForGold(IFormReference ifr, String processInstanceId, String customerID,
			String ProductType, String collateralID, String productCode, Boolean isAgriProduct, String lendableAmount) {

		Log.consoleLog(ifr, "#CBSLoanAccountCreation=============ProductType:" + ProductType);

		String LoanAccNumber = "";
                 JSONObject responseObject = new JSONObject();
		try {
			String processInstanceIds = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String APICriterion = "'CBS_LoanAccountCreation'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, processInstanceIds, APICriterion);
			if (ExecutionStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				Log.consoleLog(ifr, "LoanAccNumber already available");
				String LoanAccountAvl_Query = null;

				LoanAccountAvl_Query = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS " + "WHERE WINAME='"
						+ processInstanceId + "' AND ROWNUM=1";
				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>Gold Loan::::" + LoanAccountAvl_Query);

				List<List<String>> Result = cf.mExecuteQuery(ifr, LoanAccountAvl_Query, "LoanAccountAvl_Query:");
				if (Result.size() > 0) {
					LoanAccNumber = Result.get(0).get(0);
					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
                    responseObject.put("status", RLOS_Constants.SUCCESS);
                   responseObject.put("LoanAccNumber", LoanAccNumber);
					
				}else {
                       responseObject.put("status", RLOS_Constants.ERROR);
                       responseObject.put("errorMessage", "Technical Glitch");
                                }

			} else {
				Log.consoleLog(ifr, "LoanAccNumber not available");
				try {

					String CustomerId = "";
					String LoanAmount = "";
					String Tenure = "";

					// CustomerId = pcm.getCustomerIDForOtherProducts(ifr);
					// Added by Parkash.k on 24-05-2024 for getting cusomer id and LoanAmt,Tenure
					String queryGetCustomerID = "  SELECT CUSTOMERID from LOS_TRN_CUSTOMERSUMMARY  "
							+ " WHERE WINAME='" + processInstanceId + "'";
					List<List<String>> getCustomerId = cf.mExecuteQuery(ifr, queryGetCustomerID, queryGetCustomerID);

					if (getCustomerId.size() > 0) {
						CustomerId = getCustomerId.get(0).get(0);

					}

					Log.consoleLog(ifr, "CustomerId==>" + customerID);
					String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT, RECOMMEND_TENURE  from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
							+ processInstanceId + "'";
					Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

					List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

					if (rsQryLoanAmt.size() > 0) {
						LoanAmount = rsQryLoanAmt.get(0).get(0);
						Tenure = rsQryLoanAmt.get(0).get(1);
					}

					Log.consoleLog(ifr, "CBS_LoanAccountCreation::LoanAmount GOLD==>" + LoanAmount);
					Log.consoleLog(ifr, "CBS_LoanAccountCreation::Tenure GOLD==>" + Tenure);

					Date currentDate = new Date();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(currentDate);
					calendar.add(Calendar.DAY_OF_YEAR, 90);
					Date dateAfter90Days = calendar.getTime();
					SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
					String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);
					Log.consoleLog(ifr, "CBS_LoanAccountCreation::SanctionExpiryDate GOLD==>" + SanctionExpiryDate);
					Double amtMarginInDouble;
					String amtMargin = "";
					String lendableMargin = "";
					String collateralCode = "";
					String codCollQuery = "select codprod, flt_margin, codcoll from alos_mst_collateral_gold";
					List<List<String>> queryRes = ifr.getDataFromDB(codCollQuery);
					for (List<String> row : queryRes) {
						if (row.get(0).equalsIgnoreCase(productCode)) {
							amtMarginInDouble = 100.00 - Double.parseDouble(row.get(1));
							amtMargin = String.valueOf(amtMarginInDouble);
							lendableMargin = row.get(1);
							collateralCode = row.get(2);
						}

					}
                                        
//                                        String casaAccountNumber = "";
//                                        String drawdownRequired = (String)ifr.getValue("Q_ALOS_GOLDLOAN_APP_TRN_BO_DRAWDOWN_REQUIRED");
//                                        if(drawdownRequired.equalsIgnoreCase("Y")){
                                           String casaAccountNumber = (String)ifr.getValue("CASA_ACCOUNTS_GOLD");
                                       // }
                    
                     //GST flag code added here          
                     String AppriaserReg="";                   
                     String gstflagquery="SELECT count(*) FROM SLOS_STAFF_JEWELLERY_DETAILS " + "WHERE WINAME='"
                            + processInstanceIds + "' AND APPRAISER_GST_TYPE is not null";       
                     
                     Log.consoleLog(ifr, "gstflagquery==>::::" + gstflagquery);

                     List<List<String>> gstflagvalue = ifr.getDataFromDB(gstflagquery);
                     
                     Log.consoleLog(ifr, "gstflagvalue==>::::" + gstflagvalue);
                     
                     if (Integer.parseInt(gstflagvalue.get(0).get(0)) > 0) {
                    	 AppriaserReg="Y";
                     }
                     else
                     {
                    	 AppriaserReg="N";
                     }
                     //GST flag code end here 
                                        
					HashMap<String, String> map = new HashMap<>();
					if (isAgriProduct || productCode.equalsIgnoreCase("780")) {
						map.put("AgriLoanPurpose", "16");
						map.put("CropDuration", "S");
					} else {
						map.put("AgriLoanPurpose", "");
						map.put("CropDuration", "");
					}
					map.put("CustomerId", CustomerId);
					map.put("ProductCode", productCode);
					map.put("LoanAmount", LoanAmount);
					map.put("Tenure", Tenure);
					map.put("Purpose", "GOLD LOAN");
					map.put("TakeOveLoan", "N");
					map.put("IntSubvFlag", "Y");
					map.put("AmtMargin", amtMargin);
					map.put("LendableMargin", lendableMargin);
					map.put("CollateralCode", collateralCode);
					map.put("CropLoan", "Y");
					map.put("CropType", "1");
					map.put("Season", "1");
					map.put("AcPlanCode", "12");
					map.put("ReviewPeriod", "12");
					map.put("PMFBYApplicable", "N");
					map.put("SubsidyAvailable", "N");
					map.put("DrawdownRequired", "Y");
                    map.put("CasaAccountNumber", casaAccountNumber);
					map.put("LinkTDRDForInterest", "N");
					map.put("ConcessionPermitted", "N");
					map.put("PrimarySecondary", "P");
					map.put("ReviewAccount", "N");
					map.put("BankArr", "1");
					map.put("AppriaserReg", AppriaserReg);
					map.put("CollateralID", collateralID);
					map.put("ProjectCost", LoanAmount);
					map.put("Lendable_Amount", lendableAmount);
                     

					String response = objLAC.getLoanAccountDetailsForGold(ifr, processInstanceId, map, productCode,
							SanctionExpiryDate, ProductType);

					String[] responseData = response.split("#");
					if (response.toLowerCase().contains("error")) {
						responseObject.put("status", RLOS_Constants.ERROR);
						responseObject.put("errorMessage", responseData[1]);
					} else {
						responseObject.put("status", RLOS_Constants.SUCCESS);
						responseObject.put("LoanAccNumber", responseData[0]);
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
					Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
					responseObject.put("status", RLOS_Constants.ERROR);
					responseObject.put("errorMessage", e.getMessage());
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
			Log.errorLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
                        responseObject.put("status", RLOS_Constants.ERROR);
                        responseObject.put("errorMessage", e.getMessage());
		}
		// }
		return responseObject.toString();

	}

	public String execDisbursementEnquiryForRetail(IFormReference ifr, String LoanAccountNumber,
			String ProcessInsatnceId) {
		Log.consoleLog(ifr, "Entered into CBSDisbursementEnquiryRetail...LoanAccountNumber:" + LoanAccountNumber);
		try {
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

			String APICriterion = "'CBS_DisbursementEnquiryRetail','CBS_LoanDeductionRetail','CBS_ComputeLoanScheduleRetail','CBS_LoanScheduleRetail','CBS_SaveLoanScheduleRetail'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);
			if (ExecutionStatus.equalsIgnoreCase("FAIL")) {
				String status = objDE.updateCBSDisbursementEnquiryForRetail(ifr, ProcessInsatnceId,
						LoanAccountNumber);
				return status;
			} else {
				return RLOS_Constants.SUCCESS;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
			Log.errorLog(ifr, "Exception in  DisbursementEnquiry" + e.getMessage());
                        return e.getMessage();
		}
		
	}

	public String execLoanDeductionForRetail(IFormReference ifr, String LoanAccountNumber) {
		Log.consoleLog(ifr, "Entered into CBS_LoanDeductionRetail.. loanAccountNumber:" + LoanAccountNumber);
		String Status = "";
		
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String APICriterion = "'CBS_DisbursementEnquiryRetail','CBS_LoanDeductionRetail','CBS_ComputeLoanScheduleRetail','CBS_LoanScheduleRetail','CBS_SaveLoanScheduleRetail'";	
		String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
		Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);
		if (ExecutionStatus.equalsIgnoreCase("FAIL")) {
			try {
				String SanctionAmount = "";
				String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT, RECOMMEND_TENURE  from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
						+ ProcessInstanceId + "'";
				Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

				List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

				if (rsQryLoanAmt.size() > 0) {
					SanctionAmount = rsQryLoanAmt.get(0).get(0);
					//Tenure = rsQryLoanAmt.get(0).get(1);
				}
				Status = objLD.getLoanDeductionDetailsForRetail(ifr, ProcessInstanceId, LoanAccountNumber,
						SanctionAmount);
				Log.consoleLog(ifr, "Status" + Status);
				return Status;
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception in  loanDedDetails" + e.getMessage());
				Log.errorLog(ifr, "Exception in  loanDedDetails" + e.getMessage());
                                return e.getMessage();
			}
		} else {
			return RLOS_Constants.SUCCESS;
		}
	}

	public String execComputeLoanScheduleForRetail(IFormReference ifr, String LoanAccountNumber, String productCode) {
		Log.consoleLog(ifr, "Entered into CBS_CompleteLoanScheduleRetail...productCode:" + productCode);
		String SessionId = "";
                JSONObject responseObject = new JSONObject();
		try {
			String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String APICriterion = "'CBS_DisbursementEnquiryRetail','CBS_LoanDeductionRetail','CBS_ComputeLoanScheduleRetail','CBS_LoanScheduleRetail','CBS_SaveLoanScheduleRetail'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);
			if (ExecutionStatus.equalsIgnoreCase("FAIL")) {
				String SANCTION_AMOUNT_Query = "";

//				SANCTION_AMOUNT_Query = "SELECT SANCTION_AMOUNT FROM GLOS_L_LOANSUMMARY " + "WHERE WINAME='"
//						+ ProcessInstanceId + "'";
//
//				List<List<String>> result = cf.mExecuteQuery(ifr, SANCTION_AMOUNT_Query, "Sanction Amount:");
//				String SanctionAmount = "";
//				if (result.size() > 0) {
//					SanctionAmount = result.get(0).get(0);
//				}
				String SanctionAmount = "";
				String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT, RECOMMEND_TENURE  from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
						+ ProcessInstanceId + "'";
				Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

				List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

				if (rsQryLoanAmt.size() > 0) {
					SanctionAmount = rsQryLoanAmt.get(0).get(0);
					//Tenure = rsQryLoanAmt.get(0).get(1);
				}

				String responseComputeSchedule = objLS.computeLoanScheduleForRetail(ifr, ProcessInstanceId, LoanAccountNumber,
						SanctionAmount, productCode);

				String[] responseCollateralData = responseComputeSchedule.split("#");
                 
                                if (responseCollateralData[0].equals("")) {
                                    responseObject.put("status", RLOS_Constants.ERROR);
                                    responseObject.put("errorMessage", responseCollateralData[1]);
                                } else {
                                    responseObject.put("status", RLOS_Constants.SUCCESS);
                                    responseObject.put("SessionId", responseCollateralData[0]);
                                }

			} else {
				responseObject.put("status", RLOS_Constants.SUCCESS);
                                responseObject.put("SessionId", ""); // sending empty because next api also not call
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_ComputeLoanSchedule:" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_ComputeLoanSchedule:" + e.getMessage());
                        responseObject.put("status", RLOS_Constants.ERROR);
                        responseObject.put("errorMessage", e.getMessage());
		}
		return responseObject.toString();
	}


	public String execGenerateLoanScheduleForRetail(IFormReference ifr, String LoanAccountNumber, String SessionId) {
		Log.consoleLog(ifr, "Entered into CBS_GenerateLoanScheduleRetail...SessionId:" + SessionId);
		String Status = "";
		try {
			String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
			String APICriterion = "'CBS_DisbursementEnquiryRetail','CBS_LoanDeductionRetail','CBS_ComputeLoanScheduleRetail','CBS_LoanScheduleRetail','CBS_SaveLoanScheduleRetail'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);
			if (ExecutionStatus.equalsIgnoreCase("FAIL")) {

				Status = objLS.generateLoanScheduleForRetail(ifr, ProcessInstanceId, LoanAccountNumber, SessionId);

				Log.consoleLog(ifr, "Status==>" + Status);
				return Status;
			} else {
				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_GenerateLoanSchedule:" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_GenerateLoanSchedule:" + e.getMessage());
                        return e.getMessage();
		}
	}

	public String execSaveLoanScheduleForRetail(IFormReference ifr, String LoanAccountNumber, String SessionId,
			String productCode) {
		String Status = "";
		Log.consoleLog(ifr, "Entered into SaveLoanScheduleRetail...SessionId:" + SessionId);
		try {
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
                        String APICriterion = "'CBS_DisbursementEnquiryRetail','CBS_LoanDeductionRetail','CBS_ComputeLoanScheduleRetail','CBS_LoanScheduleRetail','CBS_SaveLoanScheduleRetail'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);
			if (ExecutionStatus.equalsIgnoreCase("FAIL")) {
				//String SANCTION_AMOUNT_Query = null;

//				SANCTION_AMOUNT_Query = "SELECT SANCTION_AMOUNT FROM GLOS_L_LOANSUMMARY " + "WHERE WINAME='"
//						+ ProcessInstanceId + "'";
//				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>" + SANCTION_AMOUNT_Query);
//
//				List<List<String>> result = cf.mExecuteQuery(ifr, SANCTION_AMOUNT_Query, "Sanction Amount:");
//				String SanctionAmount = "";
//				if (result.size() > 0) {
//					SanctionAmount = result.get(0).get(0);
//				}
				String SanctionAmount = "";
				String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT, RECOMMEND_TENURE  from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
						+ ProcessInstanceId + "'";
				Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

				List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

				if (rsQryLoanAmt.size() > 0) {
					SanctionAmount = rsQryLoanAmt.get(0).get(0);
					//Tenure = rsQryLoanAmt.get(0).get(1);
				}

				Status = objLS.updateLoanScheduleForRetail(ifr, ProcessInstanceId, LoanAccountNumber, SessionId,
						SanctionAmount, productCode);

				Log.consoleLog(ifr, "Status===>" + Status);
				return Status;
			} else {
				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_SaveLoanSchedule" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_SaveLoanSchedule" + e.getMessage());
                        return e.getMessage();
                }
	}


	public String execBranchDisbursementGold(IFormReference ifr, String LoanAccountNumber, String ProductType,
			Boolean isAgri) {
		String Status = "";
		Log.consoleLog(ifr, "Entered into CBS_BranchDisbursementAgri...ProductType:" + ProductType);
		try {
			String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

			String APICriterion = "'CBS_BranchDisbursement', 'CBS_BranchDisbursementRetail'";
			String ExecutionStatus = cm.Get_CBSExecutionStatus(ifr, ProcessInstanceId, APICriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + ExecutionStatus);

			if (ExecutionStatus.equalsIgnoreCase("FAIL")) {

				String SANCTION_AMOUNT_Query = null;

				String SanctionAmount = "";
				String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT, RECOMMEND_TENURE  from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
						+ ProcessInstanceId + "'";
				Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

				List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

				if (rsQryLoanAmt.size() > 0) {
					SanctionAmount = rsQryLoanAmt.get(0).get(0);
					//Tenure = rsQryLoanAmt.get(0).get(1);
				}

				Log.consoleLog(ifr, "LoanAmount==>" + SanctionAmount);

				
					Status = objBD.updateBranchDisbursementForGoldRetail(ifr, ProcessInstanceId, LoanAccountNumber,
							cm.getDisbAccountNoGold(ifr), SanctionAmount, ProductType);
				Log.consoleLog(ifr, "Status==>" + Status);
				return Status;
			} else {
				return RLOS_Constants.SUCCESS;
			}
			// }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
                        return e.getMessage();
		}
		
	}



}
