package com.newgen.iforms.staffGold;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.newgen.commonlogger.NGUtil;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryHRMSData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.common.APIPreprocessor;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;

import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;

import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;

public class StaffGoldPortalCustomCode {
	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();
	CommonMethods cm = new CommonMethods();
	APIPreprocessor objPP = new APIPreprocessor();

	/**
	 * @param ifr
	 * @param empId
	 * @return
	 * @throws ParseException
	 */
	public String staffDetailsVL(IFormReference ifr, String empId) throws ParseException {

		double grossSal = 0.0;
		String completedYears = "";
		double totalDed = 0.0;
		double loanDed = 0.0;
		double otherDed = 0.0;
		double dpnAvail = 0.0;
		double odAvail = 0.0;
		Double emiOdAvail = 0.0;
		double nth = 0.0;
		String totalDeduction = "";
		String empid = "";
		String designation = "";
		String branchDPCode = "";
		String probation = "";
		int years = 0;
		int remainingService = 0;
		double loanDeduction = 0.0;
		double finalLoanDeduction = 0.0;
		double notionalEMI = 0.0;
		String notionalEMIINStr = "";
		String finalLoanDeductionINStr = "";
		Log.consoleLog(ifr, "#Inside staffDetailsVL...");
		Validator valid = new KnockOffValidator("None");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		JSONParser parser = new JSONParser();
		Log.consoleLog(ifr, "#Inside processInstanceId..." + processInstanceId);
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Staff Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		CustomerAccountSummaryPAPL CBS1 = new CustomerAccountSummaryPAPL();
		String status = CBS1.executeCustomerAccountSummary(ifr, processInstanceId);
		if (status.contains(RLOS_Constants.ERROR)) {
			Log.consoleLog(ifr, "inside error condition hrmsDetails");
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		Date dateTimeFormat = new Date();
		SimpleDateFormat sDateTime = new SimpleDateFormat("yyyy-MM-dd");
		String strCurDateTime = sDateTime.format(dateTimeFormat);
//		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE) SELECT '" + processInstanceId + "', '"
//				+ strCurDateTime
//				+ "' FROM dual WHERE NOT EXISTS (    SELECT 1 FROM SLOS_TRN_LOANSUMMARY WHERE WINAME = '"
//				+ processInstanceId + "')";
//
//		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query1);

		String Query3 = "INSERT INTO SLOS_TRN_LOANDETAILS (PID) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_TRN_LOANDETAILS WHERE PID = '" + processInstanceId
				+ "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query3);

		cf.mExecuteQuery(ifr, Query3, "insert into SLOS_TRN_LOANDETAILS");

		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE,GENERATE_DOC) SELECT '"
				+ processInstanceId + "', '" + strCurDateTime
				+ "','YES' FROM dual WHERE NOT EXISTS (    SELECT 1 FROM SLOS_TRN_LOANSUMMARY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query1);

		cf.mExecuteQuery(ifr, Query1, "mImpOnClickFianlEligibility ");

		String Query2 = "INSERT INTO SLOS_STAFF_GOLD_ELIGIBILITY (WINAME) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_STAFF_GOLD_ELIGIBILITY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query2);

		cf.mExecuteQuery(ifr, Query2, "insert into SLOS_STAFF_GOLD_ELIGIBILITY");

		String customerId = "";
		String gender = "";
		String query = "select CUSTOMERID,customersex,dateofbirth,FLOOR(MONTHS_BETWEEN(SYSDATE, dateofbirth) / 12) from los_trn_customersummary where  WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "getCustomerIDFromPAPLMaster veh loan===>" + query);
		List<List<String>> list = ifr.getDataFromDB(query);
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (!list.isEmpty()) {
			customerId = list.get(0).get(0);
			ifr.setValue("Q_CUSTOMERID", customerId);
			Log.consoleLog(ifr, "customerId" + customerId);
			gender = list.get(0).get(1).toString();
			Log.consoleLog(ifr, "gender" + gender);
			ifr.setValue("AGE_VL", list.get(0).get(3));
			LocalDateTime dateTime = LocalDateTime.parse(list.get(0).get(2), inputFormatter);
			String dateOnly = dateTime.toLocalDate().toString();
			ifr.setValue("DATEOFBIRTH", dateOnly);
		}
		String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age='" + list.get(0).get(3) + "' " + "where WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
		ifr.saveDataInDB(ageQuery);

		HRMS hrms = new HRMS();
		String hrmsDetails = hrms.getHrmsDetailsVL(ifr, empId, true, "Staff Gold Loan");
		if (hrmsDetails.equals(AccelatorStaffConstant.EMPTY_RESPONSE_MESSAGE)
				|| hrmsDetails.equals(AccelatorStaffConstant.PAN_ERROR_MESSAGE)
				|| hrmsDetails.equals(AccelatorStaffConstant.STATUS_ACTIVE_MESSAGE)
				|| hrmsDetails.equals(AccelatorStaffConstant.IR_STATUS_MESSAGE)
				|| hrmsDetails.equals(AccelatorStaffConstant.RETIRING_STAFF_MESSAGE)) {
			return pcm.returnErrorcustmessage(ifr, hrmsDetails);
		}
		String[] hrmsD = hrmsDetails.split(":");
		if (hrmsDetails.contains(RLOS_Constants.ERROR) && hrmsD.length > 1) {
			Log.consoleLog(ifr, "inside error condition hrmsDetails");
			return "error" + "," + hrmsD[1];
		}

		if (hrmsDetails.contains("Invalid Staff ID")) {
			return "error" + ","
					+ "The staff ID is not associated with the logged-in user, as per the provided mobile number,Customer ID/Account Number, or PAN details";
		}
		if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		JSONObject hrmsObject = (JSONObject) parser.parse(hrmsDetails);
		designation = hrmsObject.get("Designation").toString();
		probation = hrmsObject.get("Probation").toString();
		totalDeduction = hrmsObject.get("TotalDeduction").toString();
		String gross = hrmsObject.get("Gross").toString();
		String probationQuery = "SELECT probation from slos_staff_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (queryRes.isEmpty()) {
			String insert = "INSERT INTO slos_staff_trn(winame,probation,statutory_deductions) values('"
					+ processInstanceId + "','" + probation + "','" + totalDeduction + "')";
			ifr.saveDataInDB(insert);
			Log.consoleLog(ifr, "statutory_deductions==>" + insert);
		} else {
			String updateQuery = "UPDATE slos_Staff_trn SET gender='" + gender + "', probation='" + probation
					+ "',statutory_deductions='" + totalDeduction + "' where winame='" + processInstanceId + "'";
			ifr.saveDataInDB(updateQuery);
			Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
		}

		String warning = hrmsObject.get("Warning").toString();
		String salaryAcc = hrmsObject.get("SalaryAccount").toString();
//		String validProductCodeQuery = "SELECT productcode,wheelertype FROM STAFF_VL_VALID_PRODUCT_CODE where isactive='Y'";
//		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
//		List<String> fourWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "4");
//		List<String> twoWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "2");
//		List<String> temp = new ArrayList<>();
//		temp.addAll(fourWheelerProduct);
//		temp.addAll(twoWheelerProduct);

		String validProduct = "SELECT productCode, productType from staff_valid_products where producttype = 'Staff Gold'";
		Log.consoleLog(ifr, "validProduct qquery==>" + validProduct);
		List<List<String>> validProductResult = ifr.getDataFromDB(validProduct);
		List<String> StaffGoldProductCode = valid.getFlatList(ifr, validProductResult, "StaffGold");
		Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
		String adv360Response = adv360.executeCBSAdvanced360Inquiryv2Kavach(ifr, processInstanceId, customerId,
				salaryAcc, designation, probation, StaffGoldProductCode, false, "StaffGold");

		if (adv360Response.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + adv360Response;
		}
		JSONObject adv360Obj = (JSONObject) parser.parse(adv360Response);

		String amtInstal = adv360Obj.get("AmtInstal").toString();
		String amtOdInt = adv360Obj.get("AmtOdInt").toString();
		String originalBalance = (adv360Obj.get("OriginalBalance") == null
				|| adv360Obj.get("OriginalBalance").toString().trim().isEmpty()) ? "0"
						: adv360Obj.get("OriginalBalance").toString().trim();
		Log.consoleLog(ifr, "OriginalBalance : " + originalBalance);

		Log.consoleLog(ifr, "amtInstal : " + amtInstal);

		double amtInstalDouble = 0.0;
		double totalDeductionDouble = 0.0;
		double grossDouble = 0.0;
		if (amtInstal != null && !amtInstal.trim().isEmpty()) {
			amtInstalDouble = Double.parseDouble(amtInstal);
		}
		if (totalDeduction != null && !totalDeduction.trim().isEmpty()) {
			totalDeductionDouble = Double.parseDouble(totalDeduction);
		}
		if (gross != null && !gross.trim().isEmpty()) {
			grossDouble = Double.parseDouble(gross);
		}
		String irStatus = ifr.getValue("IR_Status").toString();
		String extLoanDed = ifr.getValue("EXT_LOAN_DED").toString();
		if (extLoanDed.trim().isEmpty()) {
			ifr.setValue("EXT_LOAN_DED", "0.0");
			extLoanDed = "0.0";
		}

		double totaldeductions = amtInstalDouble + totalDeductionDouble + Double.parseDouble(extLoanDed);

		ifr.setValue("TOTAL_ALL_DEDUCTIONS", String.valueOf(totaldeductions));

		double netSalary = grossDouble - totaldeductions;

		ifr.setValue("Net_Salary", String.valueOf(netSalary));

		String queryForIRNum = "SELECT IR_REFERANCH_ID from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> queryForIRNumResult = ifr.getDataFromDB(queryForIRNum);
		Log.consoleLog(ifr, "queryForIRNumResult : " + queryForIRNumResult);
		if (irStatus.equalsIgnoreCase("N")) {
			ifr.setValue("IR_Ref_Num", "NA");
		} else {
			if (!queryForIRNumResult.isEmpty()) {
				ifr.setValue("IR_Ref_Num", queryForIRNumResult.get(0).get(0));
			}

		}

		String queryForEMI = "select EMI from SLOS_ALL_ACTIVE_PRODUCT where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT queryForEMI==>" + queryForEMI);
		List<List<String>> resqueryForEMI = ifr.getDataFromDB(queryForEMI);

		double totalEMI = 0.0;

		if (resqueryForEMI != null && !resqueryForEMI.isEmpty()) {
			totalEMI = resqueryForEMI.stream()
					.filter(row -> row != null && !row.isEmpty() && row.get(0) != null && !row.get(0).trim().isEmpty())
					.map(row -> {
						try {
							return Double.parseDouble(row.get(0).trim());
						} catch (NumberFormatException e) {
							Log.consoleLog(ifr, "Invalid EMI value: " + row.get(0));
							return 0.0;
						}
					}).mapToDouble(Double::doubleValue).sum();
		}

		ifr.setValue("LoanDeductionDPN", String.valueOf(totalEMI));

		// String appNumber = "";
		String queryForRefrenceNumber = "select APPLICATION_NO from LOS_WIREFERENCE_TABLE " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> listqueryForRefrenceNumber = ifr.getDataFromDB(queryForRefrenceNumber);
		Log.consoleLog(ifr, "queryForRefrenceNumber==> " + queryForRefrenceNumber);
		Log.consoleLog(ifr, "listqueryForRefrenceNumber==> " + listqueryForRefrenceNumber);
		if (!listqueryForRefrenceNumber.isEmpty()) {
			String appNumber = listqueryForRefrenceNumber.get(0).get(0);
			Log.consoleLog(ifr, "appNumber==> " + appNumber);
			ifr.setValue("Q_ApproveRecommendAuthority", appNumber);
		}

		String queryForLatestAmtODLimit = "select LIMIT from SLOS_ALL_ACTIVE_PRODUCT where WINAME='" + processInstanceId
				+ "' AND PRODUCTCODE IN ('253','254','1129')";
		String amtOdLim = "";
		double amtOdLimitD = 0.0;
		Log.consoleLog(ifr, "queryForLatestAmtODLimit==>" + queryForLatestAmtODLimit);
		List<List<String>> queryForLatestAmtODLimitRes = ifr.getDataFromDB(queryForLatestAmtODLimit);
		if (!queryForLatestAmtODLimitRes.isEmpty()) {
			amtOdLim = queryForLatestAmtODLimitRes.get(0).get(0);
			if (amtOdLim != null && !amtOdLim.trim().isEmpty()) {
				try {
					double val = Double.parseDouble(amtOdLim.trim());
					if (val > 0) {
						amtOdLimitD = val;
					}
				} catch (NumberFormatException e) {
					return "error, invalid od limit please approach branch";
				}
			}
		} else {
			amtOdLimitD = 0;
		}
		// String amtOdLimit = String.valueOf(amtOdLimitD);

		DecimalFormat df = new DecimalFormat("#.##");
		String amtOdLimit = (amtOdLimitD == 0) ? "0" : df.format(amtOdLimitD);

		String jsonString = "";
		Double totaUtilization = 0.0;

		String Query = "SELECT PRODUCTCODEUSEDBORROWER  from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId
				+ "'";
		List<List<String>> Output3 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output3);
		if (Output3.size() > 0 && Output3.get(0).size() > 0 && !Output3.get(0).get(0).toString().trim().isEmpty()) {
			Log.consoleLog(ifr, "valid in if condition==>");
			jsonString = Output3.get(0).get(0);
			Log.consoleLog(ifr, "valid in if condition==>" + jsonString);
		} else {
			JSONObject jsonObjInp = new JSONObject();
			Log.consoleLog(ifr, "valid==>" + jsonObjInp);
			for (List<String> res1 : validProductResult) {
				Log.consoleLog(ifr, "value added==>" + res1.get(0));
				jsonObjInp.put(res1.get(0), 0);
			}
			jsonString = jsonObjInp.toString();
			Log.consoleLog(ifr, "jsonString valid product code result==> " + jsonString);
			// return "error, technical glitch";
		}
		JSONParser pars = new JSONParser();

		try {
			JSONObject jsonObject = (JSONObject) pars.parse(jsonString);
			Log.consoleLog(ifr, "jsonObject for Staff Kavach" + jsonObject);

			for (String productCode : StaffGoldProductCode) {
				String code = productCode.trim();
				if (jsonObject.containsKey(code)) {
					Object value = jsonObject.get(code);

					if (value instanceof Number) {
						totaUtilization += ((Number) value).doubleValue();
						Log.consoleLog(ifr, "inside totaUtilization " + totaUtilization);
					}
				}

			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		query = "SELECT MAXIMUM_LIMIT FROM SLOS_STAFF_GOLD_SCHEME_LIMIT WHERE DESIGNATION ='" + designation + "'";

		Log.consoleLog(ifr, "insurance limit query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "insurance===>" + res);

		String amtRequest = "";

		if (!res.isEmpty()) {

			amtRequest = res.get(0).get(0).toString();

		}
		double eligiblityasPerPolicy = Double.parseDouble(amtRequest) - totaUtilization;
		Log.consoleLog(ifr, "eligiblityasPerPolicy===>" + eligiblityasPerPolicy);

		ifr.setValue("total_gold_elg", amtRequest);
		// ifr.setValue("Total_DPN_Utilized", String.valueOf(totaUtilization));
		ifr.setValue("total_gold_util", String.valueOf(totaUtilization));
		ifr.setValue("total_gold_avl", String.valueOf(eligiblityasPerPolicy));
		double netSalaryAfterDed = Double.parseDouble(gross)
				- (Double.parseDouble(totalDeduction) + Double.parseDouble(amtInstal) + notionalEMI);
		Log.consoleLog(ifr, "gross===>" + gross);
		Log.consoleLog(ifr, "totalDeduction===>" + totalDeduction);
		Log.consoleLog(ifr, "amtInstal===>" + amtInstal);
		Log.consoleLog(ifr, "notionalEMI===>" + notionalEMI);
		String netSalaryAfterD = String.format("%.2f", netSalaryAfterDed);
		ifr.setValue("StaffNTH", String.valueOf(netSalaryAfterD));
		ifr.setValue("Gross_Salary", gross);

		Log.consoleLog(ifr, "netSalaryAfterD===>" + netSalaryAfterD);
		String loanType = "Canara Kavach";
		String Querytenure = "UPDATE SLOS_STAFF_TRN SET DPNUTILIZED= '" + originalBalance + "',OD_UTILIZED='"
				+ amtOdLimit + "',LOAN_TYPE='" + loanType + "' WHERE WINAME= '" + processInstanceId + "'";

		ifr.saveDataInDB(Querytenure);
		Log.consoleLog(ifr, "Querytenure : " + Querytenure);

		if (!adv360Obj.get("Warning").toString().equalsIgnoreCase("NoError")) {
			warning = adv360Obj.get("Warning").toString();
		}
		return gender + "$" + "SUCCESS : " + warning;
	}

	public void availOfferDetails(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Avail Offer'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);

	}

	public void goldJewelleryDetails(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Jewellery Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);

	}

	public void setUserIDGold(IFormReference ifr, String value) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String activityName = ifr.getActivityName();
		String userName = "";
		String vehType = "";

		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equalsIgnoreCase("Staff_Gold_Branch_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_MAKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Gold_Branch_Checker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Gold_RO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Gold_CO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Gold_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_SANCTION_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Gold_CO_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}

	}

	public String populateLoanSummaryGold(IFormReference ifr, String tBranch) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTransactionBranch(IFormReference ifr) {
		String branchCode = "";
		String processingUser = ifr.getUserName();
		Log.consoleLog(ifr, "getTransactionBranch/getTransactionBranch" + processingUser);
		String queryProcessingUserBranchDetails = "select branchcode " + "from los_m_branch b, los_m_user u, pdbuser p "
				+ "where u.employee_id=p.username " + "and u.office_code=b.branchcode " + "and username= '"
				+ processingUser + "'";
		Log.consoleLog(ifr, "getTransactionBranch/queryProcessingUserBranchDetails" + queryProcessingUserBranchDetails);
		List<List<String>> dataToPopulate = ifr.getDataFromDB(queryProcessingUserBranchDetails);
		Log.consoleLog(ifr, "getTransactionBranch/dataToPopulate" + dataToPopulate);

		if (dataToPopulate.size() > 0) {
			branchCode = dataToPopulate.get(0).get(0);
		}
		Log.consoleLog(ifr, "getTransactionBranch/branchCode" + branchCode);

		return branchCode;
	}

	public String approvalProcess(IFormReference ifr) {
		CommonFunctionality cf = new CommonFunctionality();

		JSONParser parser = new JSONParser();
		String customerID = "";
		String mobileNumber = "";
		String accoutNumber = "";
		String[] agriProducts = { "842", "836", "890", "269" };
		String[] retailProducts = { "3028" };

		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String productCode = "";
		String shortName = "";
		double codeFarm = 0;

//	        String productCodeQuery = "select a.FACILITY, b.schemeid from ALOS_GOLDLOAN_APP_TRN a inner join ALOS_M_GOLD_PRODUCT b on a.FACILITY = b.CBS_PRODUCT_CODE where winame='" + ProcessInstanceId + "'";
//	        List<List<String>> productCodeQueryList = ifr.getDataFromDB(productCodeQuery);
//
//	        if (productCodeQueryList.size() > 0 && productCodeQueryList.get(0).get(0) != null) {
//	            productCode = productCodeQueryList.get(0).get(0);
//	            Log.consoleLog(ifr, "productCode :" + productCode);
//	        } else {
//	            return "Product Code/Scheme ID not found";
//	        }
		productCode = "3028";

		boolean agriProductFound = Arrays.asList(agriProducts).contains(productCode);

		String custIDQuery = "select CUSTOMERID from LOS_TRN_CUSTOMERSUMMARY where winame='" + ProcessInstanceId + "'";

		Log.consoleLog(ifr, "Query CUSTOMERID for table  :" + custIDQuery);
		List<List<String>> list1 = ifr.getDataFromDB(custIDQuery);
		if (list1.size() > 0) {
			customerID = list1.get(0).get(0);

		} else {
			return "customerID not found";
		}
		Log.consoleLog(ifr, "customerID :" + customerID);
		String mobileNumberQuery = "select MOBILENUMBER from LOS_TRN_CUSTOMERSUMMARY where winame='" + ProcessInstanceId
				+ "'";
		Log.consoleLog(ifr, "Query mobileNumber for table  :" + custIDQuery);
		List<List<String>> list2 = ifr.getDataFromDB(mobileNumberQuery);

		if (list2.size() > 0) {
			mobileNumber = list2.get(0).get(0);
			Log.consoleLog(ifr, "mobileNumber :" + mobileNumber);
		} else {
			return "mobileNumber not found";
		}

		Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen with PID::::" + ProcessInstanceId);
		try {
			String collateralResponse = collateralProperty(ifr, customerID, productCode);

			JSONObject resultObject = (JSONObject) parser.parse(collateralResponse);
			String statusCollateral = resultObject.get("status").toString();
			if (statusCollateral.equals(RLOS_Constants.ERROR)) {
				return resultObject.get("errorMessage").toString();
			}

			String lendableAmount = resultObject.get("landableAmount").toString();
			String collateralID = resultObject.get("collateralId").toString();
			String accoutNumberResponse = "";
			String accoutActivate = "";
			if (!Optional.ofNullable(collateralID).isEmpty() && Optional.ofNullable(collateralID).isPresent()) {
				accoutNumberResponse = objPP.execLoanAccountCreationForGold(ifr, ProcessInstanceId, customerID, "GOLD",
						collateralID, productCode, agriProductFound, lendableAmount);
				JSONObject accoutNumberResult = (JSONObject) parser.parse(accoutNumberResponse);
				String accoutNumberStatus = accoutNumberResult.get("status").toString();
				if (accoutNumberStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
					return accoutNumberResult.get("errorMessage").toString();
				} else {
					accoutNumber = accoutNumberResult.get("LoanAccNumber").toString();
					ifr.setStyle("Disb_Loan_OD_Acc_Num", "visible", "true");
					ifr.setValue("Disb_Loan_OD_Acc_Num", accoutNumber.trim());
					ifr.setValue("Q_BranchTypeCode", "Approved");
				}
			}

			String updateNomineeRes = updateNomineeDetailsFromAPI(ifr, accoutNumber.trim());
			JSONObject updateNomineeResResult = (JSONObject) parser.parse(updateNomineeRes);
			String updateNomineeStatus = updateNomineeResResult.get("status").toString();
			if (updateNomineeStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return updateNomineeResResult.get("errorMessage").toString();
			} else {
				return RLOS_Constants.SUCCESS;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return e.getMessage();
		}
	}

	public String updateNomineeDetailsFromAPI(IFormReference ifr, String accoutNumber) {
		Log.consoleLog(ifr, "disburseTo: " + accoutNumber);
		String nomineeName = "";
		String nomineerel = "";
		String nomineedob = "";
		String nomineeaddLine1 = "";
		String nomineeaddLine2 = "";
		String nomineeaddLine3 = "";
		String nomineeCity = "";
		String nomineeState = "";
		String nomineePincode = "";

		String guardianName = "";
		String guardianRel = "";
		String guardianaddLine1 = "";
		String guardianaddLine2 = "";
		String guardianaddLine3 = "";
		String guardianCity = "";
		String guardianState = "";
		String guardianPincode = "";
		String guardianCountry = "";

		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		// String customerID = ifr.getValue("Portal_L_CustomerID").toString();

		String mobileNumber = "";
		String customerId = "";
		Map<String, String> map = new HashMap<>();
		String query = "SELECT mobileNumber,customerId FROM LOS_TRN_CUSTOMERSUMMARY WHERE winame='" + PID + "' ";
		Log.consoleLog(ifr, "get user details===>" + query.toString());
		List<List<String>> response = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "get user details for nominee===>" + response);
		for (List<String> row : response) {
			mobileNumber = row.get(0);
			customerId = row.get(1);
			Log.consoleLog(ifr, "customerId ===>" + customerId + " " + mobileNumber);
		}
//	        String disbAccountNo = loadDisbAccountNumberForGold(ifr, PID, customerId, "leadMaker");
		map.put("CustomerId", customerId);
		map.put("MobileNumber", mobileNumber);
//	        map.put("AccountNo", disbAccountNo);
		Log.consoleLog(ifr, "getNomineeDeatils CustomerId: " + customerId);
		Log.consoleLog(ifr, "getNomineeDeatils MobileNumber: " + mobileNumber);
//	        Log.consoleLog(ifr, "getNomineeDeatils AccountNo: " + disbAccountNo);

		String nomineeQuery = "SELECT NOM_NAME,NOM_RELATION,TO_CHAR(NOM_DOB,'YYYYMMDD') as NOMINEE_DOB ,NOM_ADD_1,NOM_ADD_2,NOM_ADD_3,NOM_CITY,NOM_STATE,NOM_PINCODE FROM SLOS_STAFF_JEWELLERY_DETAILS WHERE winame='"
				+ PID + "'";
		// + "GUARDIAN_NAME, GUARDIAN_RELATION, GUARDIAN_ADD_LINE1, GUARDIAN_ADD_LINE2,
		// GUARDIAN_ADD_LINE3, GUARDIAN_CITY, GUARDIAN_PINCODE, GUARDIAN_STATE FROM
		// ALOS_GOLDLOAN_APP_TRN WHERE winame='" + PID + "' ";
		List<List<String>> nomineeQueryresponse = ifr.getDataFromDB(nomineeQuery);
		Log.consoleLog(ifr, "get  nominee===>" + nomineeQueryresponse);
		if (nomineeQueryresponse.size() > 0) {
			nomineeName = nomineeQueryresponse.get(0).get(0);
			Log.consoleLog(ifr, "nomineeName: " + nomineeName);
			nomineerel = nomineeQueryresponse.get(0).get(1);
			Log.consoleLog(ifr, "nomineerel: " + nomineerel);
			nomineedob = nomineeQueryresponse.get(0).get(2);
			Log.consoleLog(ifr, "nomineedob: " + nomineedob);
			Log.consoleLog(ifr,
					"nomineeQueryresponse.get(0).get(3).length(): " + nomineeQueryresponse.get(0).get(3).length());
			if (nomineeQueryresponse.get(0).get(3).length() > 35) {
				nomineeaddLine1 = nomineeQueryresponse.get(0).get(3).replaceAll(nomineeQueryresponse.get(0).get(4), "")
						.replaceAll(nomineeQueryresponse.get(0).get(5), "");
				nomineeaddLine1 = nomineeaddLine1.substring(0, Math.min(nomineeaddLine1.length(), 35));
			} else {
				Log.consoleLog(ifr, "in else condition nomineeaddLine1 : " + nomineeaddLine1);
				nomineeaddLine1 = nomineeQueryresponse.get(0).get(3);
			}
			Log.consoleLog(ifr, "nomineeaddLine1: " + nomineeaddLine1);
			Log.consoleLog(ifr, "before trimming nomineeaddLine2: " + nomineeaddLine2);
			if (nomineeQueryresponse.get(0).get(4).length() > 35) {
				nomineeaddLine2 = nomineeQueryresponse.get(0).get(4);
				nomineeaddLine2 = nomineeaddLine2.substring(0, Math.min(nomineeaddLine2.length(), 35));
			} else {
				nomineeaddLine2 = nomineeQueryresponse.get(0).get(4);
			}
			Log.consoleLog(ifr, "after trimming nomineeaddLine2: " + nomineeaddLine2);
			Log.consoleLog(ifr, "before trimmming nomineeaddLine3: " + nomineeaddLine3);
			if (nomineeQueryresponse.get(0).get(5).length() > 35) {
				nomineeaddLine3 = nomineeQueryresponse.get(0).get(5);
				nomineeaddLine3 = nomineeaddLine3.substring(0, Math.min(nomineeaddLine3.length(), 35));
			} else {
				nomineeaddLine3 = nomineeQueryresponse.get(0).get(5);
			}
			Log.consoleLog(ifr, "after trimmming nomineeaddLine3: " + nomineeaddLine3);
			nomineeCity = nomineeQueryresponse.get(0).get(6);
			Log.consoleLog(ifr, "nomineeCity: " + nomineeCity);
			nomineeState = nomineeQueryresponse.get(0).get(7);
			Log.consoleLog(ifr, "nomineeState: " + nomineeState);
			nomineePincode = nomineeQueryresponse.get(0).get(8);
			Log.consoleLog(ifr, "nomineePincode: " + nomineePincode);

//	            boolean isguardianNameNotNullOrEmpty = Optional.ofNullable( nomineeQueryresponse.get(0).get(9)).filter(s -> !s.isEmpty()).isPresent();
//	            Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isguardianNameNotNullOrEmpty" + isguardianNameNotNullOrEmpty);
//	            if(isguardianNameNotNullOrEmpty){
//	                guardianName = nomineeQueryresponse.get(0).get(9);
//	                Log.consoleLog(ifr, "guardianName: " + guardianName);
//	                guardianRel = nomineeQueryresponse.get(0).get(10);
//	                Log.consoleLog(ifr, "guardianrel: " + guardianRel);
//	                guardianaddLine1 = nomineeQueryresponse.get(0).get(11);
//	                Log.consoleLog(ifr, "guardianaddLine1: " + guardianaddLine1);
//	                guardianaddLine2 = nomineeQueryresponse.get(0).get(12);
//	                Log.consoleLog(ifr, "guardianaddLine2: " + guardianaddLine2);
//	                guardianaddLine3 = nomineeQueryresponse.get(0).get(13);
//	                Log.consoleLog(ifr, "guardianaddLine3: " + guardianaddLine3);
//	                guardianCity = nomineeQueryresponse.get(0).get(14);
//	                Log.consoleLog(ifr, "guardianCity: " + guardianCity);
//	                guardianPincode = nomineeQueryresponse.get(0).get(15);
//	                Log.consoleLog(ifr, "guardianPincode: " + guardianPincode);
//	                guardianState = nomineeQueryresponse.get(0).get(16);
//	                Log.consoleLog(ifr, "guardianState: " + guardianState);
//	                guardianCountry = "IN";
//	            }

		}
		boolean isNumeric = nomineerel.matches("\\d +");
		if (!isNumeric) {
			Log.consoleLog(ifr, "updateNomineeDetailsFromAPI/isNumeric" + isNumeric);
			String queryForNomrel = "SELECT RELATION_CODE FROM ALOS_MST_GOLD_NOMINEE_RELATION WHERE RELATION_NAME='"
					+ nomineerel + "'";
			Log.consoleLog(ifr, "updateNomineeDetailsFromAPI/queryForNomrel" + isNumeric);

			List<List<String>> queryForNomrelresponse = ifr.getDataFromDB(queryForNomrel);
			Log.consoleLog(ifr, "updateNomineeDetailsFromAPI/queryForNomrel" + queryForNomrelresponse);

			if (queryForNomrelresponse.size() > 0) {
				nomineerel = queryForNomrelresponse.get(0).get(0);
			}
			Log.consoleLog(ifr, "updateNomineeDetailsFromAPI/nomineerel: " + nomineerel);

		}
		boolean isnomineeNameNotNullOrEmpty = Optional.ofNullable(nomineeName).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineeNameNotNullOrEmpty" + isnomineeNameNotNullOrEmpty);
		boolean isnomineerelNotNullOrEmpty = Optional.ofNullable(nomineerel).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineerelNotNullOrEmpty" + isnomineerelNotNullOrEmpty);
		boolean isnomineedobNotNullOrEmpty = Optional.ofNullable(nomineedob).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineedobNotNullOrEmpty" + isnomineedobNotNullOrEmpty);
		boolean isnomineeaddLine1NotNullOrEmpty = Optional.ofNullable(nomineeaddLine1).filter(s -> !s.isEmpty())
				.isPresent();
		Log.consoleLog(ifr,
				"updateNomineeDetailsFromAPI: isnomineeaddLine1NotNullOrEmpty" + isnomineeaddLine1NotNullOrEmpty);
		boolean isnomineeaddLine2NotNullOrEmpty = Optional.ofNullable(nomineeaddLine2).filter(s -> !s.isEmpty())
				.isPresent();
		Log.consoleLog(ifr,
				"updateNomineeDetailsFromAPI: isnomineeaddLine2NotNullOrEmpty" + isnomineeaddLine2NotNullOrEmpty);
		boolean isnomineeaddLine3NotNullOrEmpty = Optional.ofNullable(nomineeaddLine3).filter(s -> !s.isEmpty())
				.isPresent();
		Log.consoleLog(ifr,
				"updateNomineeDetailsFromAPI: isnomineeaddLine3NotNullOrEmpty" + isnomineeaddLine3NotNullOrEmpty);
		boolean isnomineeCityNotNullOrEmpty = Optional.ofNullable(nomineeCity).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineeCityNotNullOrEmpty" + isnomineeCityNotNullOrEmpty);
		boolean isnomineeStateNotNullOrEmpty = Optional.ofNullable(nomineeState).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineeStateNotNullOrEmpty" + isnomineeStateNotNullOrEmpty);
		boolean isnomineePincodeNotNullOrEmpty = Optional.ofNullable(nomineePincode).filter(s -> !s.isEmpty())
				.isPresent();
		Log.consoleLog(ifr,
				"updateNomineeDetailsFromAPI: isnomineePincodeNotNullOrEmpty" + isnomineePincodeNotNullOrEmpty);
		boolean isaccountNoNotNullOrEmpty = Optional.ofNullable(accoutNumber).filter(s -> !s.isEmpty()).isPresent();
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: isnomineePincodeNotNullOrEmpty" + isaccountNoNotNullOrEmpty);
		if (!isnomineeNameNotNullOrEmpty) {
			JSONObject jsonValues = new JSONObject();
			jsonValues.put("status", RLOS_Constants.SUCCESS);
			return jsonValues.toString();
		}
		if (isnomineeNameNotNullOrEmpty && isnomineerelNotNullOrEmpty && isnomineedobNotNullOrEmpty
				&& isnomineeaddLine1NotNullOrEmpty && isnomineeaddLine2NotNullOrEmpty && isnomineeCityNotNullOrEmpty
				&& isnomineeStateNotNullOrEmpty && isnomineePincodeNotNullOrEmpty && isaccountNoNotNullOrEmpty) {
			Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: Inside updateNominee If block for Null check");
			map.put("NomineeName", nomineeName);
			map.put("NomineeRel", nomineerel);
			map.put("NomineeDob", nomineedob);
			map.put("NomineeAddLine1", nomineeaddLine1);
			map.put("NomineeAddLine2", nomineeaddLine2);
			map.put("NomineeCity", nomineeCity);
			map.put("NomineeState", nomineeState);
			map.put("NomineePincode", nomineePincode);
			map.put("AccountNo", accoutNumber);
		}
		if (!isnomineeaddLine3NotNullOrEmpty) {
			map.put("NomineeAddLine3", nomineeaddLine3);
		} else {
			map.put("NomineeAddLine3", "");
		}
		map.put("NomineeCountry", "IN");

		map.put("GuardianName", guardianName);
		map.put("Guardianrel", guardianRel);
		map.put("GuardianaddLine1", guardianaddLine1);
		map.put("GuardianaddLine2", guardianaddLine2);
		map.put("GuardianaddLine3", guardianaddLine3);
		map.put("GuardianCity", guardianCity);
		map.put("GuardianState", guardianState);
		map.put("GuardianPincode", guardianPincode);
		map.put("GuardianCountry", guardianCountry);

		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: map" + map);

		String cNominationDetails = updateNominationDetails(ifr, map);
		Log.consoleLog(ifr, "updateNomineeDetailsFromAPI: cNominationDetails" + cNominationDetails);
		return cNominationDetails;
	}

	public String updateNominationDetails(IFormReference ifr, Map map) {

		Log.consoleLog(ifr, "NominationCreateRequest:getCNominationDetails->Started");

		String apiName = "DetailsOfNominee";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		JSONObject jsonValues = new JSONObject();
		String errorCode = "";
		String errorMessage = "";
		String customerId = "";
		String mobileNo = "";
		String accountNo = "";
		String extUniqueRefId = generateShortUuid();
		String nomineeName = "";
		String nomineeRel = "";
		String nomineeDob = "";
		String nomineeAddLine1 = "";
		String nomineeAddLine2 = "";
		String nomineeAddLine3 = "";
		String nomineeCity = "";
		String nomineeState = "";
		String nomineePincode = "";
		String nomineeCountry = "";

		String guardianName = "";
		String guardianRel = "";
		String guardianaddLine1 = "";
		String guardianaddLine2 = "";
		String guardianaddLine3 = "";
		String guardianCity = "";
		String guardianState = "";
		String guardianPincode = "";
		String guardianCountry = "";

		try {
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
			APICommonMethods pcm = new APICommonMethods();
			// PortalCommonMethods pcm = new PortalCommonMethods();

			customerId = (String) map.get("CustomerId");
			mobileNo = (String) map.get("MobileNumber");
			nomineeName = (String) map.get("NomineeName");
			nomineeRel = (String) map.get("NomineeRel");
			nomineeDob = (String) map.get("NomineeDob");
			// remove special characters because api not allow special characters
			Log.consoleLog(ifr, "nomineeDob: while preparing request " + nomineeDob);
			nomineeAddLine1 = removeSpecialCharacters((String) map.get("NomineeAddLine1"));
			Log.consoleLog(ifr, "nomineeDob: while preparing request " + nomineeAddLine1);
			nomineeAddLine2 = removeSpecialCharacters((String) map.get("NomineeAddLine2"));
			nomineeAddLine3 = removeSpecialCharacters((String) map.get("NomineeAddLine3"));
			nomineeCity = (String) map.get("NomineeCity");
			nomineeState = (String) map.get("NomineeState");
			nomineePincode = (String) map.get("NomineePincode");
			nomineeCountry = (String) map.get("NomineeCountry");
			accountNo = (String) map.get("AccountNo");

			guardianName = (String) map.get("GuardianName");
			guardianRel = (String) map.get("Guardianrel");
			// remove special characters because api not allow special characters
			guardianaddLine1 = removeSpecialCharacters((String) map.get("GuardianaddLine1"));
			guardianaddLine2 = removeSpecialCharacters((String) map.get("GuardianaddLine2"));
			guardianaddLine3 = removeSpecialCharacters((String) map.get("GuardianaddLine3"));
			guardianCity = (String) map.get("GuardianCity");
			guardianState = (String) map.get("GuardianState");
			guardianPincode = (String) map.get("GuardianPincode");
			guardianCountry = (String) map.get("GuardianCountry");

			String Channel = pcm.getConstantValue(ifr, "CBSNOMINEE", "CHANNEL");

			String BankCode = pcm.getConstantValue(ifr, "CBDLP", "BANKCODE");
			String tBranch = "";
			String UserId = pcm.getConstantValue(ifr, "CBSACCACT", "OFFICERID");

			String tBranchCodeQuery = "select disbursement_brancode from glos_l_loansummary where winame='"
					+ ProcessInstanceId + "'";
			Log.consoleLog(ifr, "updateNominationDetails/tBranchCodeQuery==>" + tBranchCodeQuery);
			List<List<String>> tBranchCodeResult = ifr.getDataFromDB(tBranchCodeQuery);
			if (tBranchCodeResult.size() > 0) {
				tBranch = tBranchCodeResult.get(0).get(0);
			}

			String request = "{\n" + " \"input\": {\n" + " \"SessionContext\": {\n" + " \"SupervisorContext\": {\n"
					+ " \"PrimaryPassword\": \"" + "" + "\",\n" + " \"UserId\": \"" + "" + "\"\n" + " },\n"
					+ " \"BankCode\": \"" + BankCode + "\",\n" + " \"Channel\": \"" + Channel + "\",\n"
					+ " \"ExternalBatchNumber\": \"" + "" + "\",\n" + " \"ExternalReferenceNo\": \"" + "" + "\",\n"
					+ " \"ExternalSystemAuditTrailNumber\": \"" + "" + "\",\n" + " \"LocalDateTimeText\": \"" + ""
					+ "\",\n" + " \"OriginalReferenceNo\": \"" + "" + "\",\n" + " \"OverridenWarnings\": \"" + ""
					+ "\",\n" + " \"PostingDateText\": \"" + "" + "\",\n" + " \"ServiceCode\": \"" + "" + "\",\n"
					+ " \"SessionTicket\": \"" + "" + "\",\n" + " \"TransactionBranch\": \"" + tBranch + "\",\n"
					+ " \"UserId\": \"" + UserId + "\",\n" + " \"ValueDateText\": \"" + "" + "\"\n" + " },\n"
					+ " \"CustomerID\": \"" + customerId + "\",\n" + " \"AccountNo\": \"" + accountNo + "\",\n"
					+ " \"Nomineename\": \"" + nomineeName + "\",\n" + " \"Relation\": \"" + nomineeRel + "\",\n"
					+ " \"NomineeDOB\": \"" + nomineeDob + "\",\n" + " \"NomineeAddress1\": \"" + nomineeAddLine1
					+ "\",\n" + " \"NomineeAddress2\": \"" + nomineeAddLine2 + "\",\n" + " \"NomineeAddress3\": \""
					+ nomineeAddLine3 + "\",\n" + " \"NomineeCity\": \"" + nomineeCity + "\",\n"
					+ " \"NomineeState\": \"" + nomineeState + "\",\n" + " \"NomineeCountry\": \"" + nomineeCountry
					+ "\",\n" + " \"NomineePINCode\": \"" + nomineePincode + "\",\n" + " \"Nominee_Mob_PhoneNo\": \""
					+ "" + "\",\n" + " \"NomineeEmailID\": \"" + "" + "\",\n" + " \"GuardianName\": \"" + guardianName
					+ "\",\n" + " \"GuardianRelation\": \"" + guardianRel + "\",\n" + " \"GuardianAddress1\": \""
					+ guardianaddLine1 + "\",\n" + " \"GuardianAddress2\": \"" + guardianaddLine2 + "\",\n"
					+ " \"GuardianAddress3\": \"" + guardianaddLine3 + "\",\n" + " \"GuardianCity\": \"" + guardianCity
					+ "\",\n" + " \"GuardianState\": \"" + guardianState + "\",\n" + " \"GuardianCountry\": \""
					+ guardianCountry + "\",\n" + " \"GuardianPINCode\": \"" + guardianPincode + "\",\n"
					+ " \"Guardian_Mob_PhoneNo\": \"" + "" + "\",\n" + " \"GuardianEmailID\": \"" + "" + "\",\n"
					+ " \"ExtUniqueRefId\": \"" + extUniqueRefId + "\"\n" + " }\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			String Response = pcm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + Response);

			if (!Response.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(Response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyObj = new JSONObject(bodyJSON);

				String CheckError = pcm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("No data exists for this customer")) {
					Log.consoleLog(ifr, "Handle==> No Nomination Details" + CheckError);
					jsonValues.put("status", RLOS_Constants.SUCCESS);

				}

			} else {
				Response = "No response from the server.";
				errorMessage = "No response from the CBS server.";
			}

			String apiStatus = "";
			if (errorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
				jsonValues.put("status", RLOS_Constants.SUCCESS);
			} else {
				apiStatus = "FAIL";
				jsonValues.put("status", RLOS_Constants.ERROR);
				jsonValues.put("errorMessage", errorMessage);
			}

			// ===================================================================
			Log.consoleLog(ifr, "captureRequestResponse Calling...");
			pcm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, Response, errorCode, errorMessage,
					apiStatus);
			Log.consoleLog(ifr, "captureRequestResponse Ended....");
			// ===================================================================

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			jsonValues.put("status", RLOS_Constants.ERROR);
			jsonValues.put("errorMessage", e.getMessage());
		}
		return jsonValues.toString();

	}

	private static String generateShortUuid() {
		UUID uuid = UUID.randomUUID();
		String uuidString = uuid.toString();
		String numericString = uuidString.replaceAll("[^0-9]", "");
		return numericString.length() > 19 ? numericString.substring(0, 19) : numericString;

	}

	// Method to remove special characters from the address
	private String removeSpecialCharacters(String address) {
		if (address == null && address == "null") {
			return ""; // Return an empty string if the input is null
		}
		// This regex will retain only alphanumeric characters (A-Z, a-z, 0-9) and
		// spaces
		return address.replaceAll("[^A-Za-z0-9 ]", " ").trim();
	}

	public String collateralProperty(IFormReference ifr, String customerID, String productCode) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String collateralId = "";
		CreateStaffGoldCollateral objCL = new CreateStaffGoldCollateral();
		String lendableAmt = "";
		JSONObject responseObject = new JSONObject();
		Log.consoleLog(ifr, "Gold:collateralProperty->processInstanceId: " + processInstanceId);
		try {

			String queryCollateralId = "SELECT COLLATERAL_ID, TOTAL_ELIGIBLE FROM SLOS_STAFF_JEWELLERY_DETAILS WHERE WINAME = '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryCollateralId " + queryCollateralId);
			List<List<String>> rsqueryCollateralId = ifr.getDataFromDB(queryCollateralId);
			Log.consoleLog(ifr, "rsqueryCollateralId " + rsqueryCollateralId);
			if (rsqueryCollateralId != null && rsqueryCollateralId.size() > 0) {
				List<String> row = rsqueryCollateralId.get(0);

				if (row != null && row.size() > 1) {
					collateralId = row.get(0) != null ? row.get(0) : "";
					lendableAmt = row.get(1) != null ? row.get(1) : "";
				} else {
					Log.consoleLog(ifr, "Row is empty or missing columns");
				}
			} else {
				Log.consoleLog(ifr, "No data returned from query");
			}
			if (collateralId != null && !collateralId.isEmpty() && lendableAmt != null && !lendableAmt.isEmpty()) {
				responseObject.put("landableAmount", lendableAmt);
				responseObject.put("collateralId", collateralId);
				responseObject.put("status", RLOS_Constants.SUCCESS);
			} else {

				String queryCollateral = "select doj.DESC_OF_JEWELLERY, trn.total_gross_weight, trn.total_net_weight from SLOS_STAFF_JEWELLERY_DETAILS_C doj inner join SLOS_STAFF_JEWELLERY_DETAILS trn on trn.WINAME=doj.WINAME  where doj.WINAME  = '"
						+ processInstanceId + "'";
				String descOfJewelry = "";
				String grossWt = "";
				String netWt = "";

				List<List<String>> rsQryColletral = ifr.getDataFromDB(queryCollateral);
				Log.consoleLog(ifr, "Gold:collateralProperty->queryColletral: " + rsQryColletral);
				for (List<String> row : rsQryColletral) {
					descOfJewelry = row.get(0).trim();
					Log.consoleLog(ifr, "descOfJewellery===>" + descOfJewelry);
					grossWt = row.get(1).trim();
					Log.consoleLog(ifr, "grossWt===>" + grossWt);
					netWt = row.get(2).trim();
					Log.consoleLog(ifr, "netWt===>" + netWt);

				}
				String queryamtPresentVal = "SELECT CBS_PRODUCT_CODE, LENDING_RATE FROM ALOS_M_GOLD_PRODUCT";
				Log.consoleLog(ifr, "queryamtPresentVal" + queryamtPresentVal);
				List<List<String>> queryamtPresentlist = ifr.getDataFromDB(queryamtPresentVal);
				Log.consoleLog(ifr, "queryamtPresentlist" + queryamtPresentlist);
				String lendingRate = "";
				for (List<String> row : queryamtPresentlist) {
					if (row.get(0).equalsIgnoreCase(productCode)) {
						lendingRate = row.get(1).trim();
					}
				}
				Log.consoleLog(ifr, "lendingRate" + lendingRate);
				HashMap<String, String> mapCM = new HashMap<String, String>();
				Double netWtInDouble;
				Double lendingRateInDouble;
				Double amtPresentValInDouble = 0.0;
				mapCM.put("CustomerID", customerID);
				mapCM.put("DescOfJewellery", "gold ornaments");
				mapCM.put("GrossWt", grossWt);
				mapCM.put("NetWt", netWt);
				// String dojValueQuery = "SELECT QUALITY_FINENESS, NET_WEIGHT FROM
				// ALOS_GOLDLOAN_APP_TRN_DOJ WHERE WINAME = '" + processInstanceId + "'";

				// List<List<String>> dojValueQuerylist = ifr.getDataFromDB(dojValueQuery);
				String codCollQuery = "select flt_margin from alos_mst_collateral_gold where codprod='" + productCode
						+ "'";
				List<List<String>> queryRes = ifr.getDataFromDB(codCollQuery);
				String fltmgn = queryRes.get(0).get(0);
				double fltmgnInDouble = Double.parseDouble(fltmgn);
				double amtPresentValuemarket = 0.0;
				// Added new code here change from fltmrgn to lendingrate
				double lendingRateDouble = Double.parseDouble(lendingRate);
				double landableAmountValInDouble = 0.0;
				double netWtTotal = 0.0;
				String netweightQuery = "SELECT TOTAL_NET_WEIGHT FROM SLOS_STAFF_JEWELLERY_DETAILS WHERE WINAME = '"
						+ processInstanceId + "'";
				List<List<String>> netweightQuerylist = ifr.getDataFromDB(netweightQuery);
				if (netweightQuerylist.size() > 0) {
					netWtTotal = Double.parseDouble(netweightQuerylist.get(0).get(0));
				}
				// New ended here
				Log.consoleLog(ifr, "netWtTotal" + netWtTotal);
				String qualityFineNess = cm.getConstantValue(ifr, "GOLDCOLL", "QUALITYFINESS");
				Log.consoleLog(ifr, "qualityFineNess" + qualityFineNess);
				String marketValueQuery = "SELECT MARKET_VALUE FROM ALOS_M_COLLATERAL_GOLD_RATE  WHERE CARATAGE = '"
						+ qualityFineNess + "'";
				List<List<String>> marketValueQuerylist = ifr.getDataFromDB(marketValueQuery);
				double marketValue = Double.parseDouble(marketValueQuerylist.get(0).get(0));
				amtPresentValuemarket = netWtTotal * marketValue;
				amtPresentValInDouble = netWtTotal * lendingRateDouble;

				Log.consoleLog(ifr,
						"Gold:collateralProperty->amtPresentValInDouble before round and floor: amtPresentValInDouble"
								+ amtPresentValInDouble);
				amtPresentValInDouble = (double) Math.round(Math.floor(amtPresentValInDouble));
				Log.consoleLog(ifr,
						"Gold:collateralProperty->amtPresentValInDouble after round and floor: amtPresentValInDouble"
								+ amtPresentValInDouble);
				String amtPresentVal = String.valueOf(amtPresentValInDouble);
				String amtPresentValmarket = String.valueOf(Math.round(Math.floor(amtPresentValuemarket)));

				lendingRateInDouble = amtPresentValInDouble;
				Log.consoleLog(ifr,
						"Gold:collateralProperty->amtPresentValInDouble before round and floor: lendingRateInDouble"
								+ lendingRateInDouble);
				String landableAmountVal = String.valueOf(Math.round(Math.floor(lendingRateInDouble)));
				Log.consoleLog(ifr,
						"Gold:collateralProperty->amtPresentValInDouble after round and floor: lendingRateInDouble"
								+ lendingRateInDouble);

				mapCM.put("AmtPresentVal", amtPresentValmarket);
				mapCM.put("LandableAmountVal", landableAmountVal);
				Log.consoleLog(ifr, "Gold:collateralProperty->mapCM: " + mapCM);

				String resposeCollateral = objCL.getCollateral(ifr, mapCM, productCode);
				String[] resposeCollateralData = resposeCollateral.split("#");

//				if (resposeCollateralData[0].equals("")) {
//					responseObject.put("status", RLOS_Constants.ERROR);
//					responseObject.put("errorMessage", resposeCollateralData[1]);
//				}
				if (resposeCollateral.toLowerCase().contains("error")) {
					responseObject.put("status", RLOS_Constants.ERROR);
					responseObject.put("errorMessage", resposeCollateralData[1]);
				}
				
				else {
					responseObject.put("status", RLOS_Constants.SUCCESS);
					responseObject.put("landableAmount", landableAmountVal);
					responseObject.put("collateralId", resposeCollateral);
				}

			}
		} catch (Exception e) {
			Log.errorLog(ifr, "GoldLoanDetails:collateralProperty->Exception Block  :" + e.getMessage());
			Log.consoleLog(ifr, "GoldLoanDetails:collateralProperty->Exception Block  :" + e.getMessage());
			responseObject.put("status", RLOS_Constants.ERROR);
			responseObject.put("errorMessage", e.getMessage());

		}
		return responseObject.toString();
	}

	public String getGoldNeslFlageByBranch(IFormReference ifr) {
		String neslFlage = "";
		String processingUser = ifr.getUserName();
		Log.consoleLog(ifr, "getGoldNeslFlageByBranch/getTransactionBranch" + processingUser);
		String queryProcessingUserBranchDetails = "select IS_GOLD_NESL_REQUIRED " + "from los_m_branch b, los_m_user u "
				+ "where u.office_code=b.branchcode " + "and u.employee_id= '" + processingUser + "'";
		Log.consoleLog(ifr,
				"getGoldNeslFlageByBranch/queryProcessingUserBranchDetails" + queryProcessingUserBranchDetails);
		List<List<String>> dataToPopulate = ifr.getDataFromDB(queryProcessingUserBranchDetails);
		Log.consoleLog(ifr, "getGoldNeslFlageByBranch/dataToPopulate" + dataToPopulate);

		if (dataToPopulate.size() > 0) {
			neslFlage = dataToPopulate.get(0).get(0);
		}
		Log.consoleLog(ifr, "getGoldNeslFlageByBranch/neslFlage" + neslFlage);

		return neslFlage;
	}

	public String autoPopulateInitialdata(IFormReference ifr, String value) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
		ArrayList<String> accountIdList = new ArrayList<>();
		String savingACCNo = "";
		String customerID = "";
		try {
			String ProductType = ifr.getValue("ProductType").toString();
			Log.consoleLog(ifr, "loadDisbAccountNumberForGold  : " + ProductType);
			String savingACC = "";
			String casaAccount = "";

			String query = "Select CUSTOMERID from LOS_TRN_CUSTOMERSUMMARY where WINAME ='" + pid + "'";
			Log.consoleLog(ifr, "inside query: " + query);
			List<List<String>> list1 = ifr.getDataFromDB(query);
			Log.consoleLog(ifr, "inside query: " + list1.size());
			if (!list1.isEmpty()) {
				customerID = list1.get(0).get(0);
			}
			Advanced360EnquiryData advanced360EnquiryData = new Advanced360EnquiryData();
			String response = advanced360EnquiryData.filterDisbursementAccount(ifr, pid, customerID, "GOLD");
			Log.consoleLog(ifr, " filterDisbursementAccount response::::" + response);

			String inputString = response;
			if (inputString.equalsIgnoreCase("ERROR") || inputString.equalsIgnoreCase("")) {
				// Adding no items in Dropdown
			} else {
				// Added by Subham on 27-05-2024
				try {
					JSONParser parser = new JSONParser();
					JSONArray jsonArray = (JSONArray) parser.parse(response);

					// Iterate through the JSONArray and retrieve AccountId values
					for (Object obj : jsonArray) {
						JSONObject jsonObj = (JSONObject) obj;
						if (jsonObj.get("AccountId") != null) {
							String accountId = (String) jsonObj.get("AccountId");
							// System.out.println("AccountId: " + accountId);
							accountIdList.add(accountId.trim());
							Log.consoleLog(ifr, " accountIdList::::" + accountIdList);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}

				savingACC = value.equalsIgnoreCase("onload") ? "Q_SLOS_GOLD_DETAILS_DISBURSE_TO" : "disburseToGold";
				casaAccount = value.equalsIgnoreCase("onload") ? "Q_SLOS_GOLD_DETAILS_CASA_ACCOUNT"
						: "C_SLOS_GOLD_DETAILS_CASA_ACCOUNT";

				ifr.clearCombo(savingACC);
				ifr.clearCombo(casaAccount);

				for (String accountID : accountIdList) {
					if (!accountID.isEmpty() && accountID != null) {
						savingACCNo = accountID.trim();
						ifr.addItemInCombo(savingACC, accountID.trim());
						ifr.addItemInCombo(casaAccount, accountID.trim());
					}
				}

			}
		} catch (ArithmeticException ae) {
			NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArithmeticException " + ae);
		} catch (ArrayIndexOutOfBoundsException aioe) {
			NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArrayIndexOutOfBoundsException " + aioe);
		} catch (NumberFormatException nfe) {
			NGUtil.writeErrorLog("ATS", "ATS", "Exception in NumberFormatException " + nfe);
		} catch (Exception e) {
			Log.consoleLog(ifr, e.getMessage());
		}
		Log.consoleLog(ifr, " DisbAccountNumberForGold::::" + savingACCNo);
		return savingACCNo;
	}

	public String getRequiredLoanAmtGold(IFormReference ifr) throws Exception {
		String query = "";
		String minLimit = "";
		String schemeId = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		double totalEligibleAt = Double.parseDouble(ifr.getValue("total_elig_gold").toString());
		double loanamtEnteredByUser = Double.parseDouble(ifr.getValue("getRequiredLoanAmt").toString());
		Log.consoleLog(ifr, "loanamtEnteredByUser:: " + loanamtEnteredByUser);
		Log.consoleLog(ifr, "totalEligibleAmt:: " + totalEligibleAt);
		query = "SELECT MIN_SCHEME_LIMIT_RS, schemeid FROM ALOS_M_GOLD_PRODUCT WHERE CBS_PRODUCT_CODE ='3028'";
		Log.consoleLog(ifr, "getRequiredLoanAmt/query" + query);

		List<List<String>> schemeDetailsList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "getRequiredLoanAmt/schemeDetailsList" + schemeDetailsList);
		if (schemeDetailsList.size() > 0) {
			minLimit = schemeDetailsList.get(0).get(0);
			schemeId = schemeDetailsList.get(0).get(1);
		}
		String status = "";
		double minSchemeLimit = cm.parseToDouble(minLimit);
		Log.consoleLog(ifr, "Min Scheme Limit:: " + minSchemeLimit);

		if (loanamtEnteredByUser < minSchemeLimit) {
			Log.consoleLog(ifr, "Inside User Added  Minumum Limit");
			return "Please Enter within Minimum loan amount limit";

		} else if (loanamtEnteredByUser > totalEligibleAt) {
			Log.consoleLog(ifr, "Inside User Added  Maximum Limit ");
			return "Please Enter within Total Eligible loan amount limit" + totalEligibleAt;

		}
		return calculateFeesChargesGold(ifr, loanamtEnteredByUser, schemeId);
	}

	private String calculateFeesChargesGold(IFormReference ifr, Double loanamtEnteredByUser, String schemeId)
			throws Exception {
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONParser parser = new JSONParser();
		DecimalFormat df = new DecimalFormat("0.00");
		String tBranch = "";

		tBranch = pcm.getTransactionBranch(ifr);
		Log.consoleLog(ifr, "calculateOtherCharges/tBranch" + tBranch);
		String stateCode = pcm.getStateCodeByBranch(ifr, tBranch);
		Log.consoleLog(ifr, "calculateOtherCharges/stateCode" + stateCode);
		String jsonResString = getChargesForScheme(ifr, schemeId, String.valueOf(loanamtEnteredByUser), stateCode,
				tBranch, "", false);
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(jsonResString);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Failed to parse JSON response: " + e.getMessage());
			return "Failed to parse JSON response: " + e.getMessage(); // Exit the method if JSON parsing fails
		}
		Log.consoleLog(ifr, "calculateOtherCharges/jsonObject" + jsonObject);

		// Initialize variables
		JSONObject processingChargesDetail = null;
		double processingFee = 0, processingGst = 0, apraiserFees = 0, apraiserGst = 0;
		double dDEFee = 0, dDEGst = 0, stampFee = 0, stampGst = 0, certificateFee = 0, certificateGst = 0;

		try {
			if (jsonObject.containsKey("GAPC")) {
				processingChargesDetail = (JSONObject) parser.parse(jsonObject.get("GAPC").toString());
			} else if (jsonObject.containsKey("GRPC")) {
				processingChargesDetail = (JSONObject) parser.parse(jsonObject.get("GRPC").toString());
			}

			if (processingChargesDetail != null) {
				processingFee = cm.parseToDouble(processingChargesDetail.get("amountWithoutGst").toString());
				processingGst = cm.parseToDouble(processingChargesDetail.get("gstAmt").toString());
			}
			Log.consoleLog(ifr,
					"calculateOtherCharges/processingFee, processingGst ==> " + processingFee + ", " + processingGst);

			JSONObject apraiserFeesDetails = jsonObject.containsKey("GAC")
					? (JSONObject) parser.parse(jsonObject.get("GAC").toString())
					: null;
			if (apraiserFeesDetails != null) {
				apraiserFees = cm.parseToDouble(apraiserFeesDetails.get("amountWithoutGst").toString());
				apraiserGst = cm.parseToDouble(apraiserFeesDetails.get("gstAmt").toString());
			}
			Log.consoleLog(ifr,
					"calculateOtherCharges/apraiserFees, apraiserGst ==> " + apraiserFees + ", " + apraiserGst);

			JSONObject ddeChargesDetails = jsonObject.containsKey("GDDEC")
					? (JSONObject) parser.parse(jsonObject.get("GDDEC").toString())
					: null;
			if (ddeChargesDetails != null) {
				dDEFee = cm.parseToDouble(ddeChargesDetails.get("amountWithoutGst").toString());
				dDEGst = cm.parseToDouble(ddeChargesDetails.get("gstAmt").toString());
			}
			Log.consoleLog(ifr, "calculateOtherCharges/dDEFee, dDEGst ==> " + dDEFee + ", " + dDEGst);

			JSONObject stampChargesDetails = jsonObject.containsKey("GSC")
					? (JSONObject) parser.parse(jsonObject.get("GSC").toString())
					: null;
			if (stampChargesDetails != null) {
				stampFee = cm.parseToDouble(stampChargesDetails.get("amountWithoutGst").toString());
				stampGst = cm.parseToDouble(stampChargesDetails.get("gstAmt").toString());
			}
			Log.consoleLog(ifr, "calculateOtherCharges/stampFee, stampGst ==> " + stampFee + ", " + stampGst);

			JSONObject certificateChargesDetails = jsonObject.containsKey("GSCC")
					? (JSONObject) parser.parse(jsonObject.get("GSCC").toString())
					: null;
			if (certificateChargesDetails != null) {
				certificateFee = cm.parseToDouble(certificateChargesDetails.get("amountWithoutGst").toString());
				certificateGst = cm.parseToDouble(certificateChargesDetails.get("gstAmt").toString());
			}
			Log.consoleLog(ifr, "calculateOtherCharges/certificateFee, certificateGst ==> " + certificateFee + ", "
					+ certificateGst);

			double stampCharges = stampFee + stampGst + certificateFee + certificateGst;
			Log.consoleLog(ifr, "calculateOtherCharges/stampCharges ==> " + stampCharges);

			double charges = stampCharges + processingFee + apraiserFees + dDEFee;
			Log.consoleLog(ifr, "calculateOtherCharges/charges ==> " + charges);

			double calculateGstCharges = processingGst + apraiserGst + dDEGst;
			Log.consoleLog(ifr, "calculateOtherCharges/calculateGstCharges ==> " + calculateGstCharges);

			double totalCharges = charges + calculateGstCharges;
			Log.consoleLog(ifr, "calculateOtherCharges/totalCharges ==> " + totalCharges);

			// Setting values
			ifr.setValue("process_charges_gold", String.valueOf(processingFee));
			ifr.setValue("dde_charges_gold", String.valueOf(df.format(dDEFee)));
			ifr.setValue("appraiser_charges_gold", String.valueOf(apraiserFees));
			ifr.setValue("stamp_charges_gold", String.valueOf(df.format(stampCharges)));
			ifr.setValue("subtotal_charges_gold", String.valueOf(df.format(charges)));
			ifr.setValue("gst_charges_gold", String.valueOf(df.format(calculateGstCharges)));
			ifr.setValue("total_charges_gold", String.valueOf(df.format(totalCharges)));
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error in calculateOtherCharges: " + e.getMessage());
		}
		return "SUCCESS";
	}

	public String getChargesForScheme(IFormReference ifr, String SchemeId, String loanAmount, String Statecode,
			String TBranch, String loanAccountNumber, Boolean isForApi) throws Exception {
		Log.consoleLog(ifr, "getChargesForScheme called");

		JSONObject jsonObject = new JSONObject();
		BigDecimal total = new BigDecimal("0");
		BigDecimal totalWithoutGst = new BigDecimal("0");
		BigDecimal totalGst = new BigDecimal("0");
		Log.consoleLog(ifr, "getChargesForScheme called");
		Log.consoleLog(ifr, "getChargesForScheme/calling getIsGoldNeslFlag TBranch:" + TBranch);
		String isNeslEnabled = "";
		// isNeslEnabled = getIsGoldNeslFlag(TBranch, ifr);
		isNeslEnabled = "Y"; // As of now its hardcoded
		Log.consoleLog(ifr, "getChargesForScheme/isNeslEnabled" + isNeslEnabled);

		String query = "SELECT A.TYPE,A.FIXED_FEE,A.PERCENTAGE,A.TAXPERCENTAGE,A.MIN_FEE,A.MAX_FEE,A.GLCODE,A.GLDESC,A.FEECODE, A.gl_branch_code FROM "
				+ "GLOS_M_FEE_CHARGES A INNER JOIN GLOS_M_FEE_CHARGES_SCHEME B ON A.FEECODE=B.FEECODE "
				+ "WHERE A.IsActive='Y' and B.SCHEMEID='" + SchemeId + "' and (A.LOAN_AMT_FROM <= to_number('"
				+ loanAmount + "') AND to_number('" + loanAmount + "')  <= A.LOAN_AMT_TO)";
		// incase if nesl not required then no need to show nesl related charges
		if (isNeslEnabled.equalsIgnoreCase("N")) {
			query += " AND A.IS_NESL_CHARGES='N'";
		}
		if (isForApi) {
			query += " AND A.charge_debit_required='Y'";
		}

		Log.consoleLog(ifr, "getChargesForScheme/query:" + query);

		List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
		Log.consoleLog(ifr, "getChargesForScheme/result" + result);
		if (result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				String FeeType = result.get(i).get(0);
				String FixedFee = result.get(i).get(1);
				String FeePercentage = result.get(i).get(2);
				String TaxPercentage = result.get(i).get(3);
				String MINAMT = result.get(i).get(4);
				String MAXAMT = result.get(i).get(5);
				String GLCODE = result.get(i).get(6);
				String GLDESC = result.get(i).get(7);
				String feeCode = result.get(i).get(8).replaceAll("\\d+", "");
				String glBranchCode = result.get(i).get(9);

				String ADDITIONAL_STAMP_CHARGES = "0";
				String STAMP_CESS_PERCENT = "0";
				BigDecimal FeeAmount = new BigDecimal("0.00");
				BigDecimal FeeGstAmt = new BigDecimal("0.00");
				BigDecimal totalAmt = new BigDecimal("0.00");
				BigDecimal taxPercentageAmt = new BigDecimal("0.00");
				Log.consoleLog(ifr, "feeCode:" + feeCode);
				if (FeeType.equalsIgnoreCase("STATE_STAMP_CHRG")) {
					query = "select AMOUNT_TYPE,STAMP_AMT,STAMP_PERCENTAGE,ADDITIONAL_STAMP_CHARGES,STAMP_MIN_AMT,"
							+ "STAMP_MAX_AMT,STAMP_CESS_PERCENT,STAMP_GST_PERCENT from GLOS_MST_STATEFEECHARGES "
							+ "where SchemeId='" + SchemeId + "' and State_code='" + Statecode
							+ "' and IsActive='Y' and " + "(LOAN_AMT_MIN <= to_number('" + loanAmount
							+ "') AND  to_number('" + loanAmount + "')  <= LOAN_AMT_MAX)";
					List<List<String>> stateChargeResult = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
					if (stateChargeResult.size() > 0) {
						FeeType = stateChargeResult.get(0).get(0);
						FixedFee = stateChargeResult.get(0).get(1);
						FeePercentage = stateChargeResult.get(0).get(2);
						ADDITIONAL_STAMP_CHARGES = stateChargeResult.get(0).get(3);
						MINAMT = stateChargeResult.get(0).get(4);
						MAXAMT = stateChargeResult.get(0).get(5);
						STAMP_CESS_PERCENT = stateChargeResult.get(0).get(6);
						TaxPercentage = stateChargeResult.get(0).get(7);
						Log.consoleLog(ifr, "getChargesForScheme/FeeType:" + FeeType + ", FixedFee:" + FixedFee
								+ ", FeePercentage:" + FeePercentage + ", loanAmount:" + loanAmount);
						Log.consoleLog(ifr, "getChargesForScheme/MINAMT:" + MINAMT + ", MAXAMT:" + MAXAMT);
						FeeAmount = FeeAmount.add(mGetFeesAmount(ifr, FeeType, FixedFee, FeePercentage, loanAmount));
						Log.consoleLog(ifr, "getChargesForScheme/ADDITIONAL_STAMP_CHARGES:" + ADDITIONAL_STAMP_CHARGES);
						FeeAmount = FeeAmount.add(mCheckBigDecimalValue(ifr, ADDITIONAL_STAMP_CHARGES));
						Log.consoleLog(ifr, "FeeAmount:" + FeeAmount);
						totalAmt = mCalculateTotal(ifr, MINAMT, MAXAMT, FeeAmount);
						Log.consoleLog(ifr, "totalAmt:" + totalAmt);
						taxPercentageAmt = mCalculateGSTAmount(ifr, TaxPercentage, totalAmt);
						Log.consoleLog(ifr, "TaxPercentageAmt:" + taxPercentageAmt);
						BigDecimal STAMPCESSPERCENTAMT = mCalculateGSTAmount(ifr, STAMP_CESS_PERCENT, totalAmt);
						Log.consoleLog(ifr, "STAMPCESSPERCENTAMT:" + STAMPCESSPERCENTAMT);
						FeeGstAmt = taxPercentageAmt.add(STAMPCESSPERCENTAMT).add(totalAmt);
						Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
					}
				} else if (FeeType.equalsIgnoreCase("STATE_DDE_CHRG")) {
					query = "select STAMP_CERTIFICATE_AMT,STAMP_CERTIFICATE_GST_PERCENT from GLOS_MST_STATE_DDE_FEE_CHARGES where IsActive='Y' and STATE_CODE='"
							+ Statecode + "'";
					List<List<String>> stateStamCetChargeResult = cf.mExecuteQuery(ifr, query,
							"GLOS_MST_STATE_DDE_FEE_CHARGES Fetching:");
					if (stateStamCetChargeResult.size() > 0) {
						FeeType = "Flat";
						FeePercentage = "0";
						FixedFee = stateStamCetChargeResult.get(0).get(0);
						TaxPercentage = stateStamCetChargeResult.get(0).get(1);
						Log.consoleLog(ifr, "getChargesForScheme/FeeType:" + FeeType + ", FixedFee:" + FixedFee
								+ ", FeePercentage:" + FeePercentage + ", loanAmount:" + loanAmount);
						totalAmt = FeeAmount.add(mGetFeesAmount(ifr, FeeType, FixedFee, FeePercentage, loanAmount));
						Log.consoleLog(ifr, "totalAmt:" + totalAmt);
						taxPercentageAmt = mCalculateGSTAmount(ifr, TaxPercentage, totalAmt);
						Log.consoleLog(ifr, "TaxPercentageAmt:" + taxPercentageAmt);
						FeeGstAmt = taxPercentageAmt.add(totalAmt);
						Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
					}

				} else {
					Log.consoleLog(ifr, "getChargesForScheme/FeeType:" + FeeType + ", FixedFee:" + FixedFee
							+ ", FeePercentage:" + FeePercentage + ", loanAmount:" + loanAmount);
					FeeAmount = FeeAmount.add(mGetFeesAmount(ifr, FeeType, FixedFee, FeePercentage, loanAmount));
					Log.consoleLog(ifr, "FeeAmount:" + FeeAmount);
					Log.consoleLog(ifr, "getChargesForScheme/MINAMT:" + MINAMT + ", MAXAMT:" + MAXAMT);
					totalAmt = mCalculateTotal(ifr, MINAMT, MAXAMT, FeeAmount);
					taxPercentageAmt = mCalculateGSTAmount(ifr, TaxPercentage, totalAmt);
					Log.consoleLog(ifr, "TaxPercentageAmt:" + taxPercentageAmt);
					FeeGstAmt = taxPercentageAmt.add(totalAmt);
					Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
				}
				total = total.add(FeeGstAmt);
				totalWithoutGst = totalWithoutGst.add(totalAmt);
				totalGst = totalGst.add(taxPercentageAmt);
				if (!(String.valueOf(FeeGstAmt).equalsIgnoreCase("0.00"))) {
					JSONObject obj = new JSONObject();
					String accountBranch = glBranchCode;
					boolean isAccountPresnt = Optional.ofNullable(accountBranch).filter(s -> !s.isEmpty()).isPresent();
					if (!isAccountPresnt) {
						accountBranch = TBranch;
					}
					if (isForApi) {
						obj.put("AccountNumber", GLCODE);
						obj.put("AccountType", "2");
						obj.put("AccountBranch", accountBranch);
						obj.put("DrCrFlag", "C");
						obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId() + " " + GLDESC);
						obj.put("TransactionAmount", String.valueOf(FeeGstAmt.setScale(0, RoundingMode.CEILING)));
						obj.put("TransactionCurrency", "INR");
						obj.put("chequeNumber", "");
						obj.put("chequeDate", "");
						obj.put("forcePost", "N");
						obj.put("cashTransfer", "Y");
					} else {
						obj.put("amountWithoutGst", totalAmt);
						obj.put("gstAmt", taxPercentageAmt);
					}
					jsonObject.put(feeCode, obj);
				}
			}
			double stampFee = 0.0;
			if (isNeslEnabled.equalsIgnoreCase("Y")) {
				JSONParser parser = new JSONParser();
				JSONObject stampChargesDetails = jsonObject.containsKey("GSC")
						? (JSONObject) parser.parse(jsonObject.get("GSC").toString())
						: null;
				JSONObject stampCertChargesDetails = jsonObject.containsKey("GSCC")
						? (JSONObject) parser.parse(jsonObject.get("GSCC").toString())
						: null;
				String key = isForApi ? "TransactionAmount" : "amountWithoutGst";

				stampFee = stampChargesDetails != null && stampChargesDetails.containsKey(key)
						? cm.parseToDouble(stampChargesDetails.get(key).toString())
						: 0.00;

				// if stamp charges not available then not need to subtract GSCC amount from
				// total
				if (stampFee <= 0) {
					if (isForApi) {
						String stamCertFeeStr = stampCertChargesDetails != null
								&& stampCertChargesDetails.containsKey(key)
										? stampCertChargesDetails.get(key).toString()
										: "0.00";
						BigDecimal stampCertFee = new BigDecimal(stamCertFeeStr);
						total = total.subtract(stampCertFee);
					}
					jsonObject.remove("GSCC");
					jsonObject.remove("GSC");
				}
			}

			if (!(String.valueOf(total).equalsIgnoreCase("0.00")) && isForApi) {
				JSONObject obj = new JSONObject();
				obj.put("AccountNumber", loanAccountNumber);
				obj.put("AccountType", "1");
				obj.put("AccountBranch", TBranch);
				obj.put("DrCrFlag", "D");
				obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId());
				obj.put("TransactionAmount", String.valueOf(total.setScale(0, RoundingMode.CEILING)));
				obj.put("TransactionCurrency", "INR");
				obj.put("chequeNumber", "");
				obj.put("chequeDate", "");
				obj.put("forcePost", "Y");
				obj.put("cashTransfer", "Y");
				jsonObject.put("debitcharge", obj);
			}

		}
		return jsonObject.toJSONString();
	}

	public String getIsGoldNeslFlag(String TBranch, IFormReference ifr) throws Exception {
		Log.consoleLog(ifr, "Inside getIsGoldNeslFlag");

		String isNeslEnabled = "Y";
		try {
			String formattedTbranch = String.format("%05d", Integer.parseInt(TBranch));
			Log.consoleLog(ifr, "getIsGoldNeslFlag/formattedTbranch" + formattedTbranch);
			String branchQuery = "SELECT IS_GOLD_NESL_REQUIRED FROM LOS_M_BRANCH WHERE BRANCHCODE='" + formattedTbranch
					+ "'";
			Log.consoleLog(ifr, "getIsGoldNeslFlag/branchQuery" + branchQuery);

			List<List<String>> branchList = cf.mExecuteQuery(ifr, branchQuery, "LOS_M_BRANCH Fetching:");
			Log.consoleLog(ifr, "getIsGoldNeslFlag/branchList" + branchList);

			if (branchList.size() > 0) {
				isNeslEnabled = branchList.get(0).get(0);
			} else {
				throw new Exception("Error while fetchinh IS_GOLD_NESL_REQUIRED flage");
			}
			Log.consoleLog(ifr, "getIsGoldNeslFlag/isNeslEnabled" + isNeslEnabled);

			if (isNeslEnabled.equalsIgnoreCase("O")) {
				// Below Value entered by checker, for other role considering Y only.
				String custLevelNeslFlag = ifr.getValue("Q_ALOS_GOLDLOAN_APP_TRN_BO_NESL_REQUIRED").toString();
				// In case if NESL optional in branch level and not required cutomer level then
				// returning N otherwise Y
				if (custLevelNeslFlag.equalsIgnoreCase("N")) {
					isNeslEnabled = "N";
				} else {
					isNeslEnabled = "Y";
				}
			}
			Log.consoleLog(ifr, "getIsGoldNeslFlag/updated isNeslEnabled" + isNeslEnabled);
		} catch (Exception e) {
			Log.consoleLog(ifr, "getIsGoldNeslFlag/catch Exception" + e.getMessage());
		}
		return isNeslEnabled;
	}

	public BigDecimal mGetFeesAmount(IFormReference ifr, String feetype, String amount, String percentage,
			String loanAmount) {
		BigDecimal total = new BigDecimal("0");
		try {
			if (feetype.equalsIgnoreCase("Manual")) {
			} else if (feetype.equalsIgnoreCase("Flat")) {
				total = total.add(new BigDecimal(amount));
			} else if (feetype.equalsIgnoreCase("Percentage") || feetype.equalsIgnoreCase("Combined")) {
				BigDecimal per = new BigDecimal(percentage.equalsIgnoreCase("") ? "0" : percentage);
				Log.consoleLog(ifr, "percentage Amount:" + per);
				BigDecimal loanAmt = new BigDecimal(loanAmount.equalsIgnoreCase("") ? "0" : loanAmount);
				Log.consoleLog(ifr, "loanAmt:" + loanAmt);
				total = (loanAmt.multiply(per)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
				Log.consoleLog(ifr, "total Amount:" + total);
				if (feetype.equalsIgnoreCase("Combined")) {
					total = total.add(new BigDecimal(amount));
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception In mGetFeesAmount:" + e);
			Log.errorLog(ifr, "Exception In mGetFeesAmount:" + e);
		}
		return total;
	}

	public BigDecimal mCheckBigDecimalValue(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "Inside mCheckBigDecimalValue:" + value);
		BigDecimal b = new BigDecimal("0.0");
		try {
			if (!(value.trim().equalsIgnoreCase(""))) {
				Log.consoleLog(ifr, "Inside mCheckBigDecimalValue convert:");
				BigDecimal bd = new BigDecimal(value);
				return bd;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception inside mCheckBigDecimalValue:" + e);
			Log.errorLog(ifr, "Exception inside mCheckBigDecimalValue:" + e);
		}
		return b;
	}

	public BigDecimal mCalculateTotal(IFormReference ifr, String minAmount, String maxAmount, BigDecimal total) {
		BigDecimal MinAmount = new BigDecimal(minAmount.equalsIgnoreCase("") ? "0" : minAmount);
		BigDecimal MaxAmount = new BigDecimal(maxAmount.equalsIgnoreCase("") ? "0" : maxAmount);
		if (total.compareTo(MaxAmount) > 0) {
			total = MaxAmount;
		} else if (total.compareTo(MinAmount) < 0) {
			total = MinAmount;
		}
		return total;
	}

	public BigDecimal mCalculateGSTAmount(IFormReference ifr, String gst, BigDecimal NetAmt) {
		BigDecimal GSTAmt = new BigDecimal(gst.equalsIgnoreCase("") ? "0" : gst);
		Log.consoleLog(ifr, "GSTAmt:" + GSTAmt);
		BigDecimal Gsttotal = (NetAmt.multiply(GSTAmt)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		Log.consoleLog(ifr, "Gsttotal Amount:" + Gsttotal);
		return Gsttotal;
	}

	/**
	 * added by ranshaw
	 *
	 * @param ifr
	 * @param event
	 * @return
	 * @throws ParseException
	 */
	public String createLoanAndDisburse(IFormReference ifr) throws ParseException {
		CommonFunctionality cf = new CommonFunctionality();

		JSONParser parser = new JSONParser();
		String customerID = "";
		String mobileNumber = "";
		String CBSDisbursementEnquiry = "";
		String CBS_LoanDeduction = "";
		String accoutNumber = "";
		String SessionId = "";
		String CBS_GenerateLoanSchedule = "";
		String CBS_SaveLoanSchedule = "";
		String accoutActivate = "";
		String oDLoanAccount = "";

		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String productCode = "";
		String schemeCode = "";
		String shortName = "";
		String branch = "";
		boolean agriProductFound = false;
		String sanSactionDate = "";

		String productCodeSn = "GOLD"; // cmobj.getProductCode(ifr);
		String subProductCodeSn = "GOLD"; // cmobj.getSubProductCode(ifr);

		Log.consoleLog(ifr, "productCode=======>" + productCodeSn);
		Log.consoleLog(ifr, "subProductCode====>" + subProductCodeSn);

		String days = cm.getConfigValue(ifr, productCodeSn, subProductCodeSn, "DISB_ELIGIBLE_VAR", "DAY");
//		String sanSactionDateQuery = "select TO_CHAR(SANCTION_DATE, 'dd-MM-yyyy') as SANCTIONDATE from glos_l_loansummary where winame='"
//				+ ProcessInstanceId + "'";
//
//		Log.consoleLog(ifr, "Query sanSactionDateQuery for table  :" + sanSactionDateQuery);
//		List<List<String>> list3 = ifr.getDataFromDB(sanSactionDateQuery);
//		Log.consoleLog(ifr, "Result sanSactionDateQuery for table :" + list3);
//
//		if (list3.size() > 0) {
//			Log.consoleLog(ifr, "Result sanSactionDateQuery if :" + list3.get(0).get(0));
//
//			sanSactionDate = list3.get(0).get(0);
//		} else {
//			Log.consoleLog(ifr, "Result sanSactionDateQuery else : sanSactionDate not found");
//			return "sanSactionDate not found";
//		}
//		Log.consoleLog(ifr, "sanSactionDate : " + sanSactionDate);
		
		String sancDate = "";
		String collaterId = "";
		
		String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loansummary " + "WHERE WINAME='"
				+ ProcessInstanceId + "' AND ROWNUM=1";
		Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);

		List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");

		if (!ResultSanctionDate.isEmpty()) {
			String sanctionDate = ResultSanctionDate.get(0).get(0);
			Log.consoleLog(ifr, "sanctionDate==>" + sanctionDate);

			try {
				SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date parsedDate = originalFormat.parse(sanctionDate);

				// Format to yyyyMMdd
				SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
				sanSactionDate = targetFormat.format(parsedDate); // Original date in yyyyMMdd
			} catch (Exception e) {
				Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
			}

		} else {
			return "error,No sanction date found";
		}

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			LocalDate inputDate = LocalDate.parse(sanSactionDate, formatter);
			Log.consoleLog(ifr, "inputDate : " + inputDate);

			LocalDate today = LocalDate.now();
			Log.consoleLog(ifr, "today : " + today);

			long daysDifference = ChronoUnit.DAYS.between(inputDate, today);
			Log.consoleLog(ifr, "daysDifference : " + daysDifference);

			long longDays = Long.parseLong(days);
			Log.consoleLog(ifr, "longDays : " + longDays);

			if (daysDifference > longDays) {
				return "Loan Disbursement time has been expired as Per Bank Policy.";
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Pasing Exception : " + sanSactionDate);
			return "Date Parsing Exception";
		}

		productCode = "3028";
		String custIDQuery = "select CUSTOMERID from LOS_TRN_CUSTOMERSUMMARY where winame='" + ProcessInstanceId + "'";

		Log.consoleLog(ifr, "Query CUSTOMERID for table  :" + custIDQuery);
		List<List<String>> list1 = ifr.getDataFromDB(custIDQuery);
		if (list1.size() > 0) {
			customerID = list1.get(0).get(0);

		} else {
			return "customerID not found";
		}
		Log.consoleLog(ifr, "customerID :" + customerID);
		String mobileNumberQuery = "select MOBILENUMBER from LOS_TRN_CUSTOMERSUMMARY where winame='"
				+ ProcessInstanceId + "'";
		Log.consoleLog(ifr, "Query mobileNumber for table  :" + custIDQuery);
		List<List<String>> list2 = ifr.getDataFromDB(mobileNumberQuery);

		if (list2.size() > 0) {
			mobileNumber = list2.get(0).get(0);
			Log.consoleLog(ifr, "mobileNumber :" + mobileNumber);
		} else {
			return "mobileNumber not found";
		}

		String lendableAmount = "";
		String collateralID = "";
		
		String queryCollateralId = "SELECT COLLATERAL_ID, TOTAL_ELIGIBLE FROM SLOS_STAFF_JEWELLERY_DETAILS WHERE WINAME = '"
				+ ProcessInstanceId + "'";
		Log.consoleLog(ifr, "queryCollateralId " + queryCollateralId);
		List<List<String>> rsqueryCollateralId = ifr.getDataFromDB(queryCollateralId);
		Log.consoleLog(ifr, "rsqueryCollateralId " + rsqueryCollateralId);
		if (rsqueryCollateralId != null && rsqueryCollateralId.size() > 0) {
			List<String> row = rsqueryCollateralId.get(0);

			if (row != null && row.size() > 1) {
				collateralID = row.get(0) != null ? row.get(0) : "";
				lendableAmount = row.get(1) != null ? row.get(1) : "";
			} else {
				Log.consoleLog(ifr, "Row is empty or missing columns");
			}
		} else {
			Log.consoleLog(ifr, "No data returned from query");
		}

		String queryCollateralId2 = "SELECT  LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS WHERE PID = '"
				+ ProcessInstanceId + "'";

		List<List<String>> rsqueryCollateralId2 = ifr.getDataFromDB(queryCollateralId2);
		if (rsqueryCollateralId2.size() > 0) {
			accoutNumber = rsqueryCollateralId2.get(0).get(0);
		}

		if (!Optional.ofNullable(collateralID).isEmpty() && Optional.ofNullable(collateralID).isPresent()
				&& !Optional.ofNullable(accoutNumber).isEmpty() && Optional.ofNullable(accoutNumber).isPresent()
				&& !Optional.ofNullable(lendableAmount).isEmpty() && Optional.ofNullable(lendableAmount).isPresent()) {

			Log.consoleLog(ifr, "LoanAccountNumber::::" + accoutNumber);

			CBSDisbursementEnquiry = objPP.execDisbursementEnquiryForRetail(ifr, accoutNumber, ProcessInstanceId);

			if (!(CBSDisbursementEnquiry.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return CBSDisbursementEnquiry;
			}

			CBS_LoanDeduction = objPP.execLoanDeductionForRetail(ifr, accoutNumber);

			if (!(CBS_LoanDeduction.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return CBS_LoanDeduction;
			}
			String computeLoanScheduleResponse = "";

			computeLoanScheduleResponse = objPP.execComputeLoanScheduleForRetail(ifr, accoutNumber, productCode);

			JSONObject computeLoanScheduleResponseResult = (JSONObject) parser.parse(computeLoanScheduleResponse);
			String computeLoanScheduleStatus = computeLoanScheduleResponseResult.get("status").toString();
			SessionId = computeLoanScheduleResponseResult.get("SessionId").toString();
			if (!(computeLoanScheduleStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return computeLoanScheduleResponseResult.get("errorMessage").toString();
			}
			CBS_GenerateLoanSchedule = objPP.execGenerateLoanScheduleForRetail(ifr, accoutNumber, SessionId);

			if (!(CBS_GenerateLoanSchedule.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return CBS_GenerateLoanSchedule;
			}

			CBS_SaveLoanSchedule = objPP.execSaveLoanScheduleForRetail(ifr, accoutNumber, SessionId, productCode);

			if (!(CBS_SaveLoanSchedule.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return CBS_SaveLoanSchedule;
			}
			// Time Delay 30 Milli Seconds
//                                String TimeDelay = cm.getConfigValue(ifr, "GOLD", "GOLD", "DISBURSEMENT", "TIME_DELAY_MILLISECONDS");
//                                log.consoleLog(ifr, "TimeDelay====>"+TimeDelay);      
//                                if(!TimeDelay.trim().isBlank() || !TimeDelay.trim().isEmpty())
//                                {
//                                try {
//                                            TimeUnit.MICROSECONDS.sleep(Integer.parseInt(TimeDelay));
//                                            log.consoleLog(ifr, "TimeDelay called successfully====>"+TimeDelay);     
//                                                        
//
//                                        } catch (InterruptedException e) {
//                                            Log.consoleLog(ifr, "Exception:" + e);
//                                            Thread.currentThread().interrupt();
//                                        }
//                                }
			// Log.consoleLog(ifr, "CBS_BranchDisbursement initiated afeter TimeDelay
			// successfully====>"+TimeDelay);
			String CBS_BranchDisbursement = objPP.execBranchDisbursementGold(ifr, accoutNumber, "GOLD",
					agriProductFound);

			if (!(CBS_BranchDisbursement.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
				return CBS_BranchDisbursement;
			}
//			String Query = "SELECT SANCTION_AMOUNT, DISBURSEMENT_ACCOUNT, TENURE FROM GLOS_L_LOANSUMMARY "
//					+ "WHERE WINAME='" + ProcessInstanceId + "'";
//
//			List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "Fund transfer query :: ");
//
//			String loanAmount = "";
//			String tenure = "";
//			String disbAccount = "";
//			if (Output3.size() > 0) {
//				loanAmount = Output3.get(0).get(0);
//				disbAccount = Output3.get(0).get(1);
//				tenure = Output3.get(0).get(2);
//
//			}
//
//			Log.consoleLog(ifr, "loanAmount::::" + loanAmount);
//			Log.consoleLog(ifr, "disbAccount::::" + disbAccount);
//			Log.consoleLog(ifr, "tenure::::" + tenure);
			/*
			 * PortalCommonMethods pcm = new PortalCommonMethods(); String stateCode =
			 * pcm.getTransactionBranchStateCode(ifr); String CBS_FundTransfer =
			 * objPP.execFundTransferForGold(ifr, disbAccount, "GOLD", loanAmount,
			 * stateCode, tenure, schemeCode);
			 * 
			 * if (CBS_FundTransfer.equalsIgnoreCase(RLOS_Constants.SUCCESS)) { String
			 * smsResponse = sendLoanDisbursmentSmsEmail(ifr, "1014", "1016", loanAmount); }
			 */

//                                                  if (CBS_BranchDisbursement.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                                                   String smsResponse = sendLoanDisbursmentSmsEmail(ifr, "1014", "1016", loanAmount);
//                                                   }                                            

			return CBS_BranchDisbursement;

			// }

		} else {
			return "Either collateralID, accountNumber, or lendableAmount is not present.";
		}
	}
	
	public String goldDisbursementMsg(IFormReference ifr) {
		Log.consoleLog(ifr, "inside goldDisbursementMsg start::::");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loan="";
		String queryLoanAmt = "SELECT RECOMMEND_LOAN_AMT from SLOS_STAFF_GOLD_ELIGIBILITY  WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "GoldLoan:collateralProperty->queryColletral: " + queryLoanAmt);

		List<List<String>> rsQryLoanAmt = ifr.getDataFromDB(queryLoanAmt);

		if (rsQryLoanAmt.size() > 0) {
			loan = rsQryLoanAmt.get(0).get(0);
			//Tenure = rsQryLoanAmt.get(0).get(1);
		}
		
		String loanQuery = "Select LOAN_ACCOUNTNO from SLOS_TRN_LOANDETAILS where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "Query mobileNumber for table  :" + loanQuery);
		List<List<String>> list2 = ifr.getDataFromDB(loanQuery);
		//String loan = "";
		String loanAccountNo = "";
		String customerName = "";
		if (list2.size() > 0) {
			//loan = list2.get(0).get(0);
			loanAccountNo = list2.get(0).get(0);
		} else {
			return "loanAccountNo not found";
		}
		String custIDQuery = "select TRIM(COALESCE(TO_CHAR(CUSTOMERFIRSTNAME), '')) || COALESCE(TO_CHAR(CUSTOMERMIDDLENAME), '')  || COALESCE(TO_CHAR(CUSTOMERLASTNAME), '')   name from LOS_TRN_CUSTOMERSUMMARY where winame='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "Query CUSTOMERID for table  :" + custIDQuery);
		List<List<String>> list1 = ifr.getDataFromDB(custIDQuery);
		if (list1.size() > 0) {
			customerName = list1.get(0).get(0);

		} else {
			return "customerID not found";
		}
		return "<b>Loan Disbursement Successful!</b> <br><br> Applicant Name: " + customerName + "<br> Loan Amount: "
				+ loan + "<br>Loan Account Number: " + loanAccountNo + "";
	}
}
