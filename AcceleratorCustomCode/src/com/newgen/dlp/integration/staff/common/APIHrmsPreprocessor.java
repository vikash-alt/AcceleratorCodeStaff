package com.newgen.dlp.integration.staff.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.newgen.dlp.integration.cbs.BranchDisbursement;
import com.newgen.dlp.integration.cbs.DisbursementEnquiry;
import com.newgen.dlp.integration.cbs.FundTransfer;
import com.newgen.dlp.integration.cbs.LoanAccountCreation;
import com.newgen.dlp.integration.cbs.LoanDeduction;
import com.newgen.dlp.integration.cbs.LoanSchedule;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;

/**
*
* @author ranshaw
*/
public class APIHrmsPreprocessor {

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

			String ProductCode = "";
			String apiCriterion = "'CBS_LoanAccountCreation'";
			String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);

			if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				Log.consoleLog(ifr, "LoanAccNumber already available");
				String LoanAccNumber = "";
				Log.consoleLog(ifr, "LoanAccNumber already available");
				String LoanAccountAvl_Query = null;

				LoanAccountAvl_Query = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS " + "WHERE PID='"
						+ processInstanceId + "' AND ROWNUM=1";
				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>Staff Loan::::" + LoanAccountAvl_Query);

				List<List<String>> Result = cf.mExecuteQuery(ifr, LoanAccountAvl_Query, "LoanAccountAvl_Query:");
				if (Result.size() > 0) {
					LoanAccNumber = Result.get(0).get(0);
					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					return "Loan account lready created, Kindly check in CBS";
				}

			} else {
				Log.consoleLog(ifr, "LoanAccNumber not available");
				try {

					if(journeyType.contains("HRMS")) {
					   ProductCode = AcceleratorConstants.PRODUCTCODE;
					}
					else
					{
					   ProductCode ="671";
					}
					String CustomerId = "";
					String LoanAmount = "";
					String Tenure = "";

					String query = "SELECT CUSTOMERID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='"
							+ processInstanceId + "'";
					Log.consoleLog(ifr, "#Inside query..." + query);

					List<List<String>> list = ifr.getDataFromDB(query);
					if (!list.isEmpty()) {
						CustomerId = list.get(0).get(0);
					} else {
						return "customer id not found";
					}
					Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

					String Query = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#",
							processInstanceId);
					List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
					if (!Output3.isEmpty()) {
						//LoanAmount = Output3.get(0).get(0);
						Tenure = Output3.get(0).get(1);
					}
                                        
                      String trnQuery="Select Loan_Amount from slos_staff_trn where winame='"+processInstanceId+"'";
                      List<List<String>> trnResponse=ifr.getDataFromDB(trnQuery);
                      Log.consoleLog(ifr,"trnQuery==> "+trnQuery);
                      Log.consoleLog(ifr,"res=="+trnResponse);
                      if(!trnResponse.isEmpty()){
                          LoanAmount=trnResponse.get(0).get(0);
                         // String updateQuery="UPDATE SLOS_TRN_LOANDETAILS set sanction_amount='"+LoanAmount+"' where winame='"+processInstanceId+"'";
                          //ifr.saveDataInDB(updateQuery);
                          Log.consoleLog(ifr, "");
                      }
					Date currentDate = new Date();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(currentDate);
					calendar.add(Calendar.DAY_OF_YEAR, 90);
					Date dateAfter90Days = calendar.getTime();
					SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
					String SanctionExpiryDate = dateFormat2.format(dateAfter90Days);

					loanAccountNumber = objLAC.getLoanAccountDetailsForHRMS(ifr, processInstanceId, ProductCode,
							CustomerId, LoanAmount, Tenure, SanctionExpiryDate, journeyType);
					Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
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
				String sanctionAmount = objDE.updateCBSDisbursementEnquiryForHRMS(ifr, processInstanceId,
						loanAccountNumber, "", journeyType);
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

				querySanctionAmount = "select SANCTION_AMOUNT from SLOS_TRN_LOANDETAILS WHERE" + " PID='"
						+ processInstanceId + "' and rownum=1";

				List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
				String SanctionAmount = "";
				if (!result.isEmpty()) {
					SanctionAmount = result.get(0).get(0);
				}
				String Status = objLD.getLoanDeductionDetailsForHRMS(ifr, processInstanceId, loanAccountNumber,
						SanctionAmount, journeyType);
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

				querySanctionAmount = "select SANCTION_AMOUNT from SLOS_TRN_LOANDETAILS WHERE" + " PID='"
						+ processInstanceId + "' and rownum=1";

				List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
				String SanctionAmount = "";
				if (!result.isEmpty()) {
					SanctionAmount = result.get(0).get(0);
				}
				String SessionId = objLS.computeLoanScheduleForHRMS(ifr, processInstanceId, loanAccountNumber,
						SanctionAmount, journeyType);

				if (SessionId.equalsIgnoreCase(RLOS_Constants.ERROR)) {
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

	public String execGenerateLoanSchedule(IFormReference ifr, String loanAccountNumber, String sessionId,
			String journeyType) {
		Log.errorLog(ifr, "APIPreprocessor:execGenerateLoanSchedule-> started for JourneyType=>" + journeyType);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
			String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

			if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
				String Status = objLS.generateLoanScheduleForHRMS(ifr, processInstanceId, loanAccountNumber, sessionId,
						journeyType);
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

	public String execSaveLoanSchedule(IFormReference ifr, String loanAccountNumber, String sessionId,
			String journeyType) {
		Log.errorLog(ifr, "APIPreprocessor:execSaveLoanSchedule-> started for JourneyType=>" + journeyType);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {

			String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule'";
			String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

			if (executeCBSStatus.equalsIgnoreCase("FAIL")) {
				String querySanctionAmount = "";

				querySanctionAmount = "select SANCTION_AMOUNT from SLOS_TRN_LOANDETAILS WHERE" + " PID='"
						+ processInstanceId + "' and rownum=1";

				List<List<String>> result = cf.mExecuteQuery(ifr, querySanctionAmount, "querySanctionAmount Amount:");
				String SanctionAmount = "";
				if (!result.isEmpty()) {
					SanctionAmount = result.get(0).get(0);
				}
				String Status = objLS.updateLoanScheduleForHRMS(ifr, processInstanceId, loanAccountNumber, sessionId,
						SanctionAmount, journeyType);
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
		// String SBACCNUMBER="";
		try {
                   // String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule','CBS_BranchDisbursement'";
 
//                   String apiCriterion = "'CBS_DisbursementEnquiry','CBS_LoanDeduction','CBS_ComputeLoanSchedule','CBS_GenerateLoanSchedule','CBS_SaveLoanSchedule','CBS_BranchDisbursement'";
			//String apiCriterion = "'CBS_BranchDisbursement'";
			String apiCriterion="'CBS_BranchDisbursement'";
                        String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
			Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

			if (executeCBSStatus.equalsIgnoreCase("FAIL")) {

				String LoanAmount = "";
				String Tenure = "";
				String SBACCNUMBER = "";

				String Query = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#",
						processInstanceId);
				List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
				if (!Output3.isEmpty()) {
					//LoanAmount = Output3.get(0).get(0);
					Tenure = Output3.get(0).get(1);
				}

				Log.consoleLog(ifr, "LoanAmount==>" + LoanAmount);
				Log.consoleLog(ifr, "Tenure==>" + Tenure);

				String SBAccNo = "select SB_ACCOUNT_NUMBER,LOAN_AMOUNT from SLOS_STAFF_TRN " + "where WINAME='" + processInstanceId
						+ "' and rownum=1";
				Log.consoleLog(ifr, "SBAccNo==>" + SBAccNo);
				List<List<String>> SBACCNOOutput = ifr.getDataFromDB(SBAccNo);
                                Log.consoleLog(ifr, "SBAccNo==>" + SBACCNOOutput);
				if (!SBACCNOOutput.isEmpty()) {
					SBACCNUMBER = SBACCNOOutput.get(0).get(0);
                                        LoanAmount=SBACCNOOutput.get(0).get(1);
                                        Log.consoleLog(ifr, "loanAmount for branch disbursement==> "+LoanAmount);
				}

				String Status = objBD.updateBranchDisbursementForHRMS(ifr, processInstanceId, loanAccountNumber,
						SBACCNUMBER, LoanAmount, journeyType);
				Log.consoleLog(ifr, "Status==>" + Status);
				return Status;
			}else {
				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_BranchDisbursement" + e.getMessage());
		}
		return RLOS_Constants.ERROR;
	}

	public String execLoanAccountBlocking(IFormReference ifr, String processInstanceId, String customerID, String Gold,
			String productCode, String shortName) throws ParseException {
		String LoanAccNumber = "";
		try {
			String processInstanceIds = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String APICriterion = "'CBS_AccountBlocking'";
			String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, APICriterion);
			if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				Log.consoleLog(ifr, "LoanAccNumber already available");
				String LoanAccountAvl_Query = null;

				LoanAccountAvl_Query = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS " + "WHERE PID='"
						+ processInstanceId + "' AND ROWNUM=1";
				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>Staff Loan::::" + LoanAccountAvl_Query);

				List<List<String>> Result = cf.mExecuteQuery(ifr, LoanAccountAvl_Query, "LoanAccountAvl_Query:");
				if (Result.size() > 0) {
					LoanAccNumber = Result.get(0).get(0);
					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					return LoanAccNumber;
				}

			} else {
				LoanAccNumber = objLAC.execLoanAccountBlocking(ifr, processInstanceId, customerID, productCode,
						shortName);
				Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
				return LoanAccNumber;

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
			Log.errorLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
		}
		return RLOS_Constants.ERROR;

	}

	public String execLoanAccountActivate(IFormReference ifr, String processInstanceId, String customerID,
			String productCode, String LoanAccountNumber, String shortName) throws ParseException {

		String processInstanceIds = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String APICriterion = "'CBS_AccountActivate'";
		String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, APICriterion);
		if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
			return RLOS_Constants.SUCCESS;
		} else {
			HashMap<String, String> map = new HashMap<>();
			map.put("InterestWaiver", "N");
			map.put("MinorAccountStatus", "0");

			return objLAC.execLoanAccountActivate(ifr, processInstanceId, customerID, productCode, LoanAccountNumber,
					map, shortName);
		}
	}

	public String execcreateLADODLoanAccount(IFormReference ifr, String processInstanceId, String productCode) {
		String LoanAccNumber = "";
		try {
			String processInstanceIds = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String APICriterion = "'CBS_ODLIMITCREATION'";
			String executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, APICriterion);

			if (executeCBSStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				Log.consoleLog(ifr, "LoanAccNumber already available");
				String LoanAccountAvl_Query = null;

				LoanAccountAvl_Query = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS " + "WHERE PID='"
						+ processInstanceId + "' AND ROWNUM=1";
				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>Staff Loan::::" + LoanAccountAvl_Query);

				List<List<String>> Result = cf.mExecuteQuery(ifr, LoanAccountAvl_Query, "LoanAccountAvl_Query:");
				if (Result.size() > 0) {
					LoanAccNumber = Result.get(0).get(0);
					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					return LoanAccNumber;
				}

			} else {

				String custId = "";
				String query = "SELECT CUSTOMERID,PERMCITY, PERMSTATE FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "#Inside query..." + query);
				String statename = "";
				String distictName = "";
				List<List<String>> list = ifr.getDataFromDB(query);
				if (!list.isEmpty()) {
					custId = list.get(0).get(0);
					distictName = list.get(0).get(1);
					statename = list.get(0).get(2);
				}

				Log.consoleLog(ifr, "CustomerId==>" + custId);

				HashMap<String, String> map = new HashMap<>();

				map.put("InterestIndx", "31900");
				map.put("CodMisComp1", "NA");
				map.put("takeOverOD", "N");
				map.put("bankArr", "1");
				map.put("InternalFD", "N");
				map.put("SanctionAuthority", "75");
				map.put("DistrictName", distictName);
				map.put("StateName", statename);

				map.put("LimitPurpose", "6");

				map.put("concessionPermitted", "N");

				map.put("TxnCharge", "N");
				map.put("CollateralDegree_1", "P");
				map.put("CollateralType_1", "N");
				

				LoanAccNumber = objLAC.createLADODLoanAccount(ifr, processInstanceId, custId, productCode, map);
				Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
				return LoanAccNumber;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		}

		return RLOS_Constants.ERROR;
	}

	public String execFundTransfer(IFormReference ifr, String sBACCNUMBER, String journeyType,
			String tenure, String loanType, String loanAmount) {
		Log.errorLog(ifr, "APIPreprocessor:execFundTransfer-> started for JourneyType=>" + journeyType);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String schemeID = "";
		try {

			String executeCBSStatus = RLOS_Constants.SUCCESS;

			if (journeyType.equalsIgnoreCase("STAFFVL")) {
				schemeID = pcm.mGetSchemeIDFORVL(ifr, processInstanceId);
			} else {
				schemeID = pcm.mGetSchemeID(ifr, processInstanceId);
			}

			//String apiCriterion = "'CBS_BranchDisbursement'";
                        String apiCriterionFund="'CBS_FundTransfer'";
			//executeCBSStatus = cm.executeCBSStatus(ifr, processInstanceId, apiCriterion);
                        String executeCBStatusForFund=cm.executeCBSStatus(ifr, processInstanceId, apiCriterionFund);
			Log.consoleLog(ifr, "ExecutionStatus==>" + executeCBSStatus);

			if (executeCBStatusForFund.equals("FAIL") ) {
				String stateCode = pcm.getStateCode(ifr, journeyType, processInstanceId);
				
				String Status = objLAC.fundTransfer(ifr, processInstanceId, sBACCNUMBER, journeyType,
						loanAmount ,stateCode, tenure, schemeID,loanType);
				Log.consoleLog(ifr, "Status==>" + Status);
				return Status;
			} else {
				return RLOS_Constants.SUCCESS;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  CBS_FundTransfer" + e.getMessage());
			Log.errorLog(ifr, "Exception in  CBS_FundTransfer" + e.getMessage());
		}
		return RLOS_Constants.ERROR;
	}

	public String execcreateLADODModication(IFormReference ifr, String processInstanceId, String productCode) throws ParseException, java.text.ParseException {
		String status = objLAC.modifyLADODLoanAccount(ifr, processInstanceId, productCode);
		Log.consoleLog(ifr, "Status==>" + status);
		return status;
	}

	public String createCollalwteral(IFormReference ifr, String prodCode) {
		// TODO Auto-generated method stub
		return null;
	}

}
