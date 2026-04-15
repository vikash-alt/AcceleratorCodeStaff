package com.newgen.iforms.staffKavach;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newgen.dlp.docgen.gen.GenerateDocument;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryHRMSData;
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.staff.common.APIHrmsPreprocessor;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.LoanAmtInWords;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;

public class StaffKavachPortalCustomCode {
	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();

	/**
	 * @param ifr
	 * @param empId
	 * @return
	 * @throws ParseException
	 */
	public String staffDetailsKavach(IFormReference ifr, String empId) throws ParseException {
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
		
		
		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE,GENERATE_DOC) SELECT '" + processInstanceId + "', '"
				+ strCurDateTime
				+ "','YES' FROM dual WHERE NOT EXISTS (    SELECT 1 FROM SLOS_TRN_LOANSUMMARY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query1);

		cf.mExecuteQuery(ifr, Query1, "mImpOnClickFianlEligibility ");

		String Query2 = "INSERT INTO SLOS_STAFF_VL_ELIGIBILITY (WINAME) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query2);

		cf.mExecuteQuery(ifr, Query2, "insert into SLOS_STAFF_VL_ELIGIBILITY");
		
		String Query3 = "INSERT INTO SLOS_TRN_LOANDETAILS (PID) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_TRN_LOANDETAILS WHERE PID = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query3);

		cf.mExecuteQuery(ifr, Query3, "insert into SLOS_STAFF_VL_ELIGIBILITY");

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
		String hrmsDetails = hrms.getHrmsDetailsVL(ifr, empId, true, "Canara Kavach");
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

		String validProduct = "SELECT productCode, productType from staff_valid_products where producttype = 'Kavach'";
		Log.consoleLog(ifr, "validProduct qquery==>" + validProduct);
		List<List<String>> validProductResult = ifr.getDataFromDB(validProduct);
		List<String> KavachProductCode = valid.getFlatList(ifr, validProductResult, "Kavach");
		Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
		String adv360Response = adv360.executeCBSAdvanced360Inquiryv2Kavach(ifr, processInstanceId, customerId,
				salaryAcc, designation, probation, KavachProductCode, false, "kavach");

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

		String appNumber = "";
		String queryForRefrenceNumber = "select APPLICATION_NO from LOS_WIREFERENCE_TABLE " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> listqueryForRefrenceNumber = ifr.getDataFromDB(queryForRefrenceNumber);
		Log.consoleLog(ifr, "queryForRefrenceNumber==> " + queryForRefrenceNumber);
		if (!listqueryForRefrenceNumber.isEmpty()) {
			appNumber = listqueryForRefrenceNumber.get(0).get(0);
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

			for (String productCode : KavachProductCode) {
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

		query = "SELECT INSURANCE_LIMIT FROM SLOS_STAFF_KAVACH_SCHEME_LIMIT WHERE DESIGNATION ='" + designation + "'";

		Log.consoleLog(ifr, "insurance limit query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "insurance===>" + res);

		String amtRequest = "";

		if (!res.isEmpty()) {

			amtRequest = res.get(0).get(0).toString();

		}
		double eligiblityasPerPolicy = Double.parseDouble(amtRequest) - totaUtilization;
		Log.consoleLog(ifr, "eligiblityasPerPolicy===>" + eligiblityasPerPolicy);

		ifr.setValue("Eligible_DPN_Amount", amtRequest);
		ifr.setValue("Total_DPN_Utilized", String.valueOf(totaUtilization));
		ifr.setValue("Total_DPN_Utilized", String.valueOf(totaUtilization));
		ifr.setValue("Total_DPN_Availed", String.valueOf(eligiblityasPerPolicy));
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


	public String autoPopulateAvailOfferData(IFormReference ifr, String value) throws ParseException {
		ifr.setStyle("navigationNextBtn", "disable", "false");
		String limit = "";
		String designation = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String desquery = "SELECT DESIGNATION FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "designation query===>" + desquery);
		List<List<String>> resdesquery = ifr.getDataFromDB(desquery);
		Log.consoleLog(ifr, "resdesquery===>" + resdesquery);
		if (!resdesquery.get(0).get(0).isEmpty()) {
			designation = resdesquery.get(0).get(0);
		} else {
			return "error,designation not found";
		}

		String query = "SELECT INSURANCE_LIMIT FROM SLOS_STAFF_KAVACH_SCHEME_LIMIT WHERE DESIGNATION ='" + designation
				+ "'";

		Log.consoleLog(ifr, "insurance limit query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "insurance===>" + res);

		if (!res.isEmpty()) {
			String loanAmt = getLoanAmt(ifr, processInstanceId, res.get(0).get(0), "120", "availoffer");
			Log.consoleLog(ifr, "loanAmt ===>" + loanAmt);
			setLoanAmt(ifr, processInstanceId, loanAmt);
			Log.consoleLog(ifr, "else block");
			return loanAmt + ",Kavach";

		}
		return "0.0";

	}

	private void setLoanAmt(IFormReference ifr, String processInstanceId, String loanAmt) {
		String queryUpdate = "UPDATE SLOS_TRN_LOANSUMMARY SET CURRENT_STATUS ='" + loanAmt + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
	}

	public String getLoanAmt(IFormReference ifr, String processInstanceId, String amtRequest, String tenure,
			String stage) throws ParseException {
		Log.consoleLog(ifr, "inside getLoanAmt method====>");
		Log.consoleLog(ifr, "amtRequest=========>" + amtRequest);
		Log.consoleLog(ifr, "tenure=========>" + tenure);
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
		String loanType = "";
		double eligibleAsPerNth = 0.0;
		double eligiblityasPe = 0.0;
		String EMIAmount = "";
		String EMIInWords = "";
		String dateOfJoining = "";
		String dateOfRetirement = "";
		String query = "";
		String salaryAccount = "";
		String amtOdLimit = "";
		String amtInstal = "";
		String originalBalance = "";
		String custId = "";
		String jsonString = "";
		Double totaUtilization = 0.0;

		String eligiblityasPerP = "";
		String desquery = "SELECT TOTAL_KAVACH_AVAILABLE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "designation query===>" + desquery);
		List<List<String>> resdesquery = ifr.getDataFromDB(desquery);
		Log.consoleLog(ifr, "resdesquery===>" + resdesquery);
		if (!resdesquery.get(0).get(0).isEmpty()) {
			eligiblityasPerP = resdesquery.get(0).get(0);
		} else {
			return "error,availablity not there";
		}

		// double eligiblityasPerPolicy = Double.parseDouble(amtRequest) -
		// totaUtilization;
		double eligiblityasPerPolicy = Double.parseDouble(eligiblityasPerP);
		if (eligiblityasPerPolicy < 0.0) {
			return "error" + "," + "please select lesser amount";
		}

		String queryForCustId = "SELECT CUSTOMERID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "#Inside query..." + query);

		List<List<String>> listqueryForCustId = ifr.getDataFromDB(queryForCustId);
		if (!listqueryForCustId.isEmpty()) {
			custId = listqueryForCustId.get(0).get(0);
		} else {
			return "error" + "," + "customer id not found";
		}

		String querydojdor = "SELECT DATE_OF_JOINING,DATE_OF_RETIREMENT FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		List<List<String>> listdojdor = ifr.getDataFromDB(querydojdor);
		Log.consoleLog(ifr, "querydojdor====>" + querydojdor);
		if (!listdojdor.isEmpty()) {
			Log.consoleLog(ifr, "querydojdor inside if condition====>");
			dateOfJoining = listdojdor.get(0).get(0);
			Log.consoleLog(ifr, "dateOfJoining====>" + dateOfJoining);
			dateOfRetirement = listdojdor.get(0).get(1);
			Log.consoleLog(ifr, "dateOfRetirement====>" + dateOfRetirement);
			if (dateOfJoining == null || dateOfJoining.trim().isEmpty()) {
				Log.consoleLog(ifr, "inside dateOfJoining ====>");
				return "error,date of Joining is Blank";
			} else {
				Log.consoleLog(ifr, "inside else dateOfJoining ====>");

				LocalDate startDate = LocalDate.parse(dateOfJoining.trim()); // ISO format
				Log.consoleLog(ifr, "startDate====>" + startDate);

				LocalDate currentDate = LocalDate.now();
				Log.consoleLog(ifr, "currentDate====>" + currentDate);

				Period period = Period.between(startDate, currentDate);
				Log.consoleLog(ifr, "period====>" + period);

				years = period.getYears();
				Log.consoleLog(ifr, "Completed_Years -> " + years);
			}

			if (dateOfRetirement == null || dateOfRetirement.trim().isEmpty()) {
				Log.consoleLog(ifr, "inside dateOfRetirement ====>");
				return "error,date of retirement is Blank";
			} else {

				Log.consoleLog(ifr, "inside else dateOfRetirement ====>");

				LocalDate dateOfRetire = LocalDate.parse(dateOfRetirement.trim());
				Log.consoleLog(ifr, "dateOfRetire====>" + dateOfRetire);

				LocalDate currentDate = LocalDate.now();
				Log.consoleLog(ifr, "currentDate====>" + currentDate);

				Period period1 = Period.between(currentDate, dateOfRetire);
				Log.consoleLog(ifr, "period1====>" + period1);

				remainingService = period1.getYears();
				Log.consoleLog(ifr, "Remaining_Years -> " + remainingService);
			}
		}

		String Query1 = "UPDATE SLOS_STAFF_TRN SET COMPLETED_YEARS= '" + years + "',REMAINING_SERVICE='"
				+ remainingService + "' WHERE WINAME= '" + processInstanceId + "'";

		ifr.saveDataInDB(Query1);
		Log.consoleLog(ifr, "update query HRMSPORTAL CODE " + Query1);

		query = "SELECT GROSS_SALARY,COMPLETED_YEARS, TOTAL_DED, LOAN_DEDUCTION, DPNUTILIZED,OD_UTILIZED, NTH, LOAN_TYPE,DATE_OF_JOINING,DATE_OF_RETIREMENT,STAFF_NUMBER,DESIGNATION, SALARY_CREDIT_BRAND_DP_CODE,PROBATION,TOTAL_DED, CURRENT_BRANCH,SB_ACCOUNT_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		String queryotherDed = "SELECT COALESCE(SUM(LOAN_DEDUCTION), 0) FROM SLOS_STAFF_LOAN_DEDUCTIONS WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "#Inside query for autoPopulateAvailOfferData..." + queryotherDed);
		List<List<String>> listqueryotherDed = ifr.getDataFromDB(queryotherDed);
		Log.consoleLog(ifr, "listqueryotherDed====>" + listqueryotherDed);
		List<List<String>> list = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "list====>" + list);
		if (!list.isEmpty()) {
			empid = list.get(0).get(10);
			designation = list.get(0).get(11);
			branchDPCode = list.get(0).get(12);
			ifr.setValue("Q_ProcessingBranchCode", branchDPCode);
			probation = list.get(0).get(13);
			salaryAccount = list.get(0).get(16);
			Log.consoleLog(ifr, "empid====>" + empid);
			if (empid.isEmpty() || empid == null) {
				return "error" + "," + "empid not found";
			}
			if (designation.isEmpty() || designation == null) {
				return "error" + "," + "designation not found";
			}
			if (branchDPCode.isEmpty() || branchDPCode == null) {
				return "error" + "," + "branchDPCode not found";
			}
			if (probation.isEmpty() || probation == null) {
				return "error" + "," + "probation not found";
			}
			if (salaryAccount.isEmpty() || salaryAccount == null) {
				return "error" + "," + "salaryAccount not found";
			}
		}

		int branchDPCodeLength = branchDPCode.length();
		for (int i = 0; i < 5 - branchDPCodeLength; i++) {
			branchDPCode = "0" + branchDPCode;
			Log.consoleLog(ifr, "branchDPCode==>" + branchDPCode);
		}
		String currCity = "";
		String branchName = "";
		String stateCode = "";
		String currentAccountHCity = "SELECT CITY, BRANCHNAME,STATECODE FROM LOS_M_BRANCH where BRANCHCODE='"
				+ branchDPCode + "'";

		List<List<String>> listcurrentAccountHCity = ifr.getDataFromDB(currentAccountHCity);
		Log.consoleLog(ifr, "Log of City====>" + listcurrentAccountHCity);
		if (!listcurrentAccountHCity.isEmpty()) {
			currCity = listcurrentAccountHCity.get(0).get(0);
			branchName = listcurrentAccountHCity.get(0).get(1);
			stateCode = listcurrentAccountHCity.get(0).get(2);
			Log.consoleLog(ifr, "currCity====>" + currCity);
		}

		String QueryCity = "UPDATE SLOS_TRN_LOANSUMMARY SET CITY= '" + currCity + "' WHERE WINAME= '"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "QueryCity====>" + QueryCity);
		ifr.saveDataInDB(QueryCity);

		String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age=(SELECT TRUNC(MONTHS_BETWEEN(SYSDATE, TO_DATE(DATEOFBIRTH, 'DD-MM-YYYY')) / 12) AS age FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY where WINAME='"
				+ processInstanceId + "') where WINAME='" + processInstanceId + "'";

		Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
		ifr.saveDataInDB(ageQuery);

		Log.consoleLog(ifr, "#Inside query for autoPopulateAvailOfferData..." + query);
		Log.consoleLog(ifr, "#Inside query for autoPopulateAvailOfferData..." + queryotherDed);
		listqueryotherDed = ifr.getDataFromDB(queryotherDed);
		Log.consoleLog(ifr, "listqueryotherDed====>" + listqueryotherDed);
		list = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "list====>" + list);

		if (!list.isEmpty()) {
			empid = list.get(0).get(10);
			designation = list.get(0).get(11);
			branchDPCode = list.get(0).get(12);
			probation = list.get(0).get(13);
			salaryAccount = list.get(0).get(16);
			Log.consoleLog(ifr, "empid====>" + empid);
			if (empid.isEmpty() || empid == null) {
				return "error" + "," + "empid not found";
			}
			if (designation.isEmpty() || designation == null) {
				return "error" + "," + "designation not found";
			}
			if (branchDPCode.isEmpty() || branchDPCode == null) {
				return "error" + "," + "branchDPCode not found";
			}
			if (probation.isEmpty() || probation == null) {
				return "error" + "," + "probation not found";
			}
			if (salaryAccount.isEmpty() || salaryAccount == null) {
				return "error" + "," + "salaryAccount not found";
			}
		}

		if (!listqueryotherDed.isEmpty()) {
			Optional<String> loanotherDedOptional = Optional.ofNullable(listqueryotherDed.get(0).get(0));
			if (loanotherDedOptional.isPresent() && !loanotherDedOptional.get().isEmpty()) {
				otherDed = Double.parseDouble(loanotherDedOptional.get());
				Log.consoleLog(ifr, "otherDed : " + otherDed);
			}
		}
		if (!list.isEmpty()) {
			Optional<String> grossSalOptional = Optional.ofNullable(list.get(0).get(0));
			Optional<String> completedYearsOptional = Optional.ofNullable(list.get(0).get(1));
			Optional<String> totalDedOptional = Optional.ofNullable(list.get(0).get(2));
			Optional<String> loanDedOptional = Optional.ofNullable(list.get(0).get(3));
			Optional<String> dpnAvailOptional = Optional.ofNullable(list.get(0).get(4));
			Optional<String> odAvailOptional = Optional.ofNullable(list.get(0).get(5));
			Optional<String> nthOptional = Optional.ofNullable(list.get(0).get(6));
			Optional<String> loanTypeOptional = Optional.ofNullable(list.get(0).get(7));

			if (loanTypeOptional.isPresent() && !loanTypeOptional.get().isEmpty()) {
				loanType = loanTypeOptional.get();
				Log.consoleLog(ifr, "loanType : " + loanType);
			}

			if (grossSalOptional.isPresent() && !grossSalOptional.get().isEmpty()) {
				grossSal = Double.parseDouble(grossSalOptional.get());
				Log.consoleLog(ifr, "grossSal : " + grossSal);
			}
			if (completedYearsOptional.isPresent() && !completedYearsOptional.get().isEmpty()) {
				completedYears = completedYearsOptional.get();
				Log.consoleLog(ifr, "completedYears : " + completedYears);
			}
			if (totalDedOptional.isPresent() && !totalDedOptional.get().isEmpty()) {
				totalDed = Double.parseDouble(totalDedOptional.get());
				Log.consoleLog(ifr, "totalDed : " + totalDed);
			}
			if (dpnAvailOptional.isPresent() && !dpnAvailOptional.get().isEmpty()) {
				dpnAvail = Double.parseDouble(dpnAvailOptional.get());
				Log.consoleLog(ifr, "dpnAvail : " + dpnAvail);
			} else {
				dpnAvail = Double.parseDouble(originalBalance);
				Log.consoleLog(ifr, "dpnAvail : " + dpnAvail);
			}
			if (odAvailOptional.isPresent() && !odAvailOptional.get().isEmpty()) {
				odAvail = Double.parseDouble(odAvailOptional.get());
				Log.consoleLog(ifr, "odAvail : " + odAvail);
			} else {
				odAvail = Double.parseDouble(amtOdLimit);
				Log.consoleLog(ifr, "odAvail : " + odAvail);
			}
			if (loanDedOptional.isPresent() && !loanDedOptional.get().isEmpty()) {
				loanDeduction = Double.parseDouble(loanDedOptional.get());
				DecimalFormat decimalFormat = new DecimalFormat("0.00");
				String roundedResultLoanded = decimalFormat.format(loanDeduction);
				String queryForLoanded = "UPDATE SLOS_STAFF_TRN SET LOAN_DEDUCTION= '" + roundedResultLoanded
						+ "' where WINAME='" + processInstanceId + "'";

				ifr.saveDataInDB(queryForLoanded);
				loanDed = Double.parseDouble(roundedResultLoanded);
				Log.consoleLog(ifr, "loanDed : " + loanDed);
				notionalEMI = odAvail * AcceleratorConstants.ODRATE.doubleValue()
						/ AcceleratorConstants.TWELVE.intValue();

				finalLoanDeduction = loanDed + notionalEMI;
			} else {
				if (!amtInstal.isEmpty()) {
					Log.consoleLog(ifr, "amtInstal : " + amtInstal);
					loanDed = Double.parseDouble(amtInstal);
					Log.consoleLog(ifr, "loanDed : " + loanDed);
					notionalEMI = odAvail * AcceleratorConstants.ODRATE.doubleValue()
							/ AcceleratorConstants.TWELVE.intValue();

					finalLoanDeduction = loanDed + notionalEMI;
				}
			}

			if (nthOptional.isPresent() && !nthOptional.get().isEmpty()) {
				nth = Double.parseDouble(nthOptional.get());
				Log.consoleLog(ifr, "nth : " + nth);
			}
		}
		double deductions = totalDed + loanDed + otherDed + notionalEMI;
		Log.consoleLog(ifr, "deductions===> : " + deductions);
		Log.consoleLog(ifr, "grossSal===> : " + grossSal);
		double netSalAfterDed = grossSal - deductions;
		Log.consoleLog(ifr, "netSalAfterDed : " + netSalAfterDed);
		double roi = 0.0;
		// String prodcode = "";
		double eligiblityamtPerSal = 0.0;
		String prodcode = "";
		String schedCode = "";

		String roiData = "select prod_code,RAT_INDEX,COD_SCHED_TYPE from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%KAVACH%'";

		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
		Log.consoleLog(ifr, "listroiData====>" + listroiData);
		if (!listroiData.isEmpty()) {
			prodcode = listroiData.get(0).get(0);
			roi = Double.parseDouble(listroiData.get(0).get(1));
			schedCode = listroiData.get(0).get(2);
		}

		DecimalFormat df = new DecimalFormat("#.00");
		double dpnandodAvail = dpnAvail + odAvail;
		double calculatePMT = calculatePMT(Integer.parseInt(tenure), roi);
		Log.consoleLog(ifr, "calculatePMT : " + calculatePMT);
		eligibleAsPerNth = (netSalAfterDed - 0.25 * grossSal) / calculatePMT * AcceleratorConstants.ONELAKH;
		Log.consoleLog(ifr, "eligibleAsPerNth : " + eligibleAsPerNth);

		double calculateMinFinal = 0.0;
		double eligibleAmount = 0.0;

		try {
			calculateMinFinal = calculateMin(eligibleAsPerNth, eligiblityasPerPolicy, Double.parseDouble(amtRequest),
					ifr);
			Log.consoleLog(ifr, "eligibleAmount : " + calculateMinFinal);
			// calculateMinFinal = calculateMinFinal(eligibleAmount,
			// Double.parseDouble(amtRequest));

			double notionalEMIProposed = Double.parseDouble(amtRequest) * AcceleratorConstants.ODRATE.doubleValue()
					/ 12.0D;

		} catch (NumberFormatException nfe) {
			Log.consoleLog(ifr, "Number format exception: " + nfe.getMessage());
			return "error, invalid numeric format encountered. Please check the input values.";
		} catch (IndexOutOfBoundsException iobe) {
			Log.consoleLog(ifr, "Index out of bounds exception: " + iobe.getMessage());
			return "error, data missing in the database result. Please check the data source.";
		} catch (NullPointerException npe) {
			Log.consoleLog(ifr, "Null pointer exception: " + npe.getMessage());
			return "error, unexpected null value encountered during processing.";
		} catch (Exception e) {
			Log.consoleLog(ifr, "Unexpected exception: " + e.getMessage());
			return "error, an unexpected error occurred. Please contact support.";
		}

		String deletequery = "delete from SLOS_STAFF_KAVACH_ELIGIBILITY where pid='" + processInstanceId + "'";
		ifr.saveDataInDB(deletequery);
		String insertQuery = "insert into SLOS_STAFF_KAVACH_ELIGIBILITY(pid,ELIGIBILITY_AMT_PER_NTH,\n"
				+ "ELIGIBILITY_AMT_PER_POLICY,AMOUNT_REQUESTED,FINAL_ELIGIBILITY) values('" + processInstanceId + "','"
				+ String.format("%.2f", eligibleAsPerNth) + "','" + String.format("%.2f", eligiblityasPerPolicy) + "','"
				+ calculateMinFinal + "','" + String.format("%.2f", calculateMinFinal) + "')";

		Log.consoleLog(ifr, "insertQuery===> : " + insertQuery);
		this.cf.mExecuteQuery(ifr, insertQuery, "INSERT into SLOS_STAFF_KAVACH_ELIGIBILITY");

		Log.consoleLog(ifr, "calculateMinFinal : " + calculateMinFinal);
		ifr.setValue("STAFF_LOAN_AMOUNT", String.valueOf(calculateMinFinal));
		ifr.setValue("STAFF_RATEOFINTEREST", String.valueOf(roi));
		Log.consoleLog(ifr, "STAFF_LOAN_AMOUNT ==>" + String.valueOf(calculateMinFinal));
		Log.consoleLog(ifr, "STAFF_RATEOFINTEREST ==>" + String.valueOf(roi));

		Ammortization ammortization = new Ammortization();
		String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
				String.valueOf(calculateMinFinal), tenure, prodcode, schedCode);
		String[] splitError = ammortizationResponse.split(":");
		if (ammortizationResponse.contains("ERROR")) {
			return "error," + splitError[1];
		}

		String queryForEMI = "SELECT Installment FROM LOS_STG_CBS_AMM_SCH_DETAILS WHERE ProcessInstanceId='"
				+ processInstanceId + "' AND StageNumber='1' AND InstallmentNo='1'";
		Log.consoleLog(ifr, "queryForEMI query===>" + queryForEMI);
		List<List<String>> resqueryForEMI = ifr.getDataFromDB(queryForEMI);
		Log.consoleLog(ifr, "resqueryForEMI===>" + resqueryForEMI);

		if (!resqueryForEMI.get(0).get(0).isEmpty()) {
			EMIAmount = resqueryForEMI.get(0).get(0);
		}

		EMIInWords = LoanAmtInWords.amtInWords(Double.parseDouble(EMIAmount));
		Log.consoleLog(ifr, "EMIInWords ==>" + EMIInWords);

		String netSalAfterDedInStr = String.valueOf(netSalAfterDed);
		String queryForLoanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
		List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
		Log.consoleLog(ifr, "res===>" + res);
		String totalInterest = "";
		String netDisburseAmt = "";

		totalInterest = String.valueOf(Double.parseDouble(EMIAmount) * Integer.parseInt(tenure) - calculateMinFinal);
		netDisburseAmt = String.valueOf(calculateMinFinal);

//		if (!res.isEmpty() && res.get(0).get(0).toString().equals("DPN")) {
//
//			totalInterest = String
//					.valueOf(Double.parseDouble(EMIAmount) * Integer.parseInt(tenure) - calculateMinFinal);
//			netDisburseAmt = String.valueOf(calculateMinFinal);
//		} else {
//			totalInterest = String.valueOf(0.149 * calculateMinFinal);
//			netDisburseAmt = String.valueOf(calculateMinFinal);
//		}
		Log.consoleLog(ifr, "totalInterest ==>" + totalInterest);
		Log.consoleLog(ifr, "netDisburseAmt ==>" + netDisburseAmt);

		String queryForloanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		String totalAmtPaidByBorrowers = "";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForloanType);
		List<List<String>> resqueryForLoanType = ifr.getDataFromDB(queryForloanType);
		Log.consoleLog(ifr, "resqueryForLoanType===>" + resqueryForLoanType);
		totalAmtPaidByBorrowers = String.valueOf(calculateMinFinal + Double.parseDouble(totalInterest));
		String totalAmtPaidByBorrowersnWords = LoanAmtInWords.amtInWords(Double.parseDouble(totalAmtPaidByBorrowers));
//		if (!resqueryForLoanType.isEmpty() && resqueryForLoanType.get(0).get(0).equalsIgnoreCase("DPN")) {
//
//			totalAmtPaidByBorrowers = String.valueOf(calculateMinFinal + Double.parseDouble(totalInterest));
//		} else if (!resqueryForLoanType.isEmpty()) {
//
//			totalAmtPaidByBorrowers = String.valueOf(calculateMinFinal);
//		}

		Log.consoleLog(ifr, "totalAmtPaidByBorrowers ==>" + totalAmtPaidByBorrowers);
		// String totalAmtPaidByBorrowersnWords =
		// LoanAmtInWords.amtInWords(Double.parseDouble(totalAmtPaidByBorrowers));
		Log.consoleLog(ifr, "totalAmtPaidByBorrowersnWords ==>" + totalAmtPaidByBorrowersnWords);

		String amtInWords = LoanAmtInWords.amtInWords(calculateMinFinal);
		Log.consoleLog(ifr, "amtInWords ==>" + amtInWords);
		String intrestAmtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(totalInterest));
		Log.consoleLog(ifr, "intrestAmtInWords ==>" + intrestAmtInWords);

		String netDisburseAmtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(netDisburseAmt));

		Log.consoleLog(ifr, "netDisburseAmtInWords ==>" + netDisburseAmtInWords);

//		String fixedFee = "0.0";
//		String stampAmt = "0.0";
//		String dDECharges = "";
//		String sqlCharge = "SELECT A.FixedFee,        A.TaxPercentage FROM SLOS_M_FEE_CHARGES A INNER JOIN SLOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode = B.FeeCode WHERE A.IsActive = 'Y' AND B.SCHEMEID = 'SL1' AND (A.MINTENURE <= TO_NUMBER('"
//				+ tenure + "') AND TO_NUMBER('" + tenure + "') <= A.MAXTENURE)";
//		List<List<String>> listsqlCharge = ifr.getDataFromDB(sqlCharge);
//		Log.consoleLog(ifr, "listsqlCharge==>" + sqlCharge);
//		Log.consoleLog(ifr, "listsqlChargelistsqlCharge==>" + listsqlCharge);
//		if (!listsqlCharge.isEmpty()) {
//			fixedFee = listsqlCharge.get(0).get(0);
//			dDECharges = listsqlCharge.get(1).get(0);
//			Log.consoleLog(ifr, "fixedFee : " + fixedFee);
//			Log.consoleLog(ifr, "dDECharges : " + dDECharges);
//		}
//
//		double fixedfee = fixedFee.trim().isEmpty() ? 0
//				: Double.parseDouble(fixedFee) + Double.parseDouble(fixedFee) * 0.18;
//		double dDEfeeCharges = dDECharges.trim().isEmpty() ? 0
//				: Double.parseDouble(dDECharges) + Double.parseDouble(dDECharges) * 0.18;
//		String branchCode = "";
//		String state = "";
//		String stateCodeForNESL = "";
//		String queryForBaranch = "SELECT DISB_BRANCH FROM SLOS_TRN_LOANDETAILS WHERE PID='" + processInstanceId + "'";
//		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForBaranch);
//		List<List<String>> result = ifr.getDataFromDB(queryForBaranch);
//		if (!result.isEmpty()) {
//			branchCode = result.get(0).get(0);
//		}
//		String custParamsDataQry = "SELECT STATE,BRANCHNAME FROM LOS_M_BRANCH WHERE BRANCHCODE=LPAD('" + branchCode
//				+ "','5','0') AND ROWNUM=1";
//
//		custParamsDataQry = custParamsDataQry.replaceAll("#BRANCHCODE#", branchCode);
//		Log.consoleLog(ifr, "custParamsDataQry==>" + custParamsDataQry);
//		List<List<String>> Result = ifr.getDataFromDB(custParamsDataQry);
//		Log.consoleLog(ifr, "#Result===>" + Result.toString());
//		if (!Result.isEmpty()) {
//			state = Result.get(0).get(0);
//			branchName = Result.get(0).get(1);
//		}
//
//		Log.consoleLog(ifr, "State===>" + state);
//
//		if (state.contains("ERROR")) {
//			return "ERROR,state error";
//		}
//		String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state
//				+ "')) AND ROWNUM=1";
//		double stampPercentage = 0.0;
//		double stampMincharges = 0.0;
//		double stampMaxcharges = 0.0;
//		double stampAmtChres = 0.0;
//		double stampAmtCharges = 0.0;
//		String cessCharges = "";
//		String additionalCharges = "";
//		double cessCharge = 0.0;
//		double additionalCharge = 0.0;
//		double calculatedStampAmt = 0.0;
//		double cessChargesDB = 0.0;
//
//		Log.consoleLog(ifr, "Query3==>" + Query3);
//		List<List<String>> Result3 = ifr.getDataFromDB(Query3);
//		Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
//		if (!Result3.isEmpty()) {
//			stateCodeForNESL = Result3.get(0).get(0);
//		}
//		try {
//			String sqlStateFeeCharge = "Select STAMP_AMT,STAMP_PERCENTAGE,STAMP_MIN_AMT, STAMP_MAX_AMT,AMOUNT_TYPE,STAMP_CESS_PERCENT,ADDITIONAL_STAMP_CHARGES from SLOS_MST_STATEFEECHARGES WHERE STATE_CODE='"
//					+ stateCodeForNESL + "' AND " + calculateMinFinal
//					+ " BETWEEN LOAN_AMT_MIN AND LOAN_AMT_MAX and schemeid='SL1'";
//
//			List<List<String>> listsqlStateFeeCharge = ifr.getDataFromDB(sqlStateFeeCharge);
//			Log.consoleLog(ifr, "listsqlStateFeeCharge==>" + listsqlStateFeeCharge);
//			Log.consoleLog(ifr, "sqlStateFeeCharge==>" + sqlStateFeeCharge);
//			if (!listsqlStateFeeCharge.isEmpty()) {
//				if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Flat")) {
//					stampAmt = listsqlStateFeeCharge.get(0).get(0);
//					cessCharges = listsqlStateFeeCharge.get(0).get(5);
//					// cessCharge = Double.parseDouble(cessCharges);
//					additionalCharges = listsqlStateFeeCharge.get(0).get(6);
//					// additionalCharge = Double.parseDouble(additionalCharges);
//					// stampAmtChres = Double.parseDouble(stampAmt);
//
//					additionalCharge = Double.parseDouble(additionalCharges != null ? additionalCharges : "0");
//					cessCharge = Double.parseDouble(cessCharges != null ? cessCharges : "0");
//					stampAmtChres = Double.parseDouble(stampAmt != null ? stampAmt : "0");
//					calculatedStampAmt = stampAmtChres + (stampAmtChres * cessCharge / 100) + additionalCharge;
//					stampAmtCharges = (int) Math.ceil(calculatedStampAmt);
//					Log.consoleLog(ifr, "stampAmt : " + stampAmt);
//					Log.consoleLog(ifr, "stampAmtCharges from flat charges block : " + stampAmt);
//					cessChargesDB = stampAmtChres * cessCharge / 100;
//				} else if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Percentage")) {
//					stampPercentage = Double.parseDouble(listsqlStateFeeCharge.get(0).get(1));
//					stampMincharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(2));
//					stampMaxcharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(3));
//					cessCharges = listsqlStateFeeCharge.get(0).get(5);
//					additionalCharges = listsqlStateFeeCharge.get(0).get(6);
//					Log.consoleLog(ifr, "stampPercentage : " + stampPercentage);
//					additionalCharge = Double.parseDouble(additionalCharges != null ? additionalCharges : "0");
//					cessCharge = Double.parseDouble(cessCharges != null ? cessCharges : "0");
//					double stampcharges = calculateMinFinal * (stampPercentage / 100);
//					stampAmtChres = (stampcharges > stampMaxcharges) ? stampMaxcharges
//							: (stampcharges < stampMincharges) ? stampMincharges : stampcharges;
//					calculatedStampAmt = stampAmtChres + (stampAmtChres * cessCharge / 100) + additionalCharge;
//					stampAmtCharges = (int) Math.ceil(calculatedStampAmt);
//					Log.consoleLog(ifr, "stampAmtCharges from percentage block : " + stampAmtCharges);
//					cessChargesDB = stampAmtChres * cessCharge / 100;
//
//				}
//
//			}
//
//		} catch (Exception ee) {
//			Log.consoleLog(ifr, "Exception==>" + ee);
//		}

		// double stampAmtCharges = Double.parseDouble(stampAmt);
		//String otherCharges = String.valueOf(fixedfee + dDEfeeCharges + stampAmtCharges);
		// Log.consoleLog(ifr, "netDisburseAmtInWords ==>" + netDisburseAmtInWords);

		String QueryamtInWords = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + calculateMinFinal
				+ "', LOAN_AMOUNT_IN_WORDS= '" + amtInWords + "',TENURE_MONTHS= '" + tenure + "',  NOTIONAL_EMI= '"
				+ String.format("%.2f", notionalEMI) + "', FINAL_LOAN_DEDUCTION='"
				+ String.format("%.2f", finalLoanDeduction) + "', NTH='" + netSalAfterDedInStr + "' WHERE WINAME= '"
				+ processInstanceId + "'";
		String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET INTEREST_AMOUNT= '" + totalInterest
				+ "', INTEREST_AMOUNT_IN_WORDS='" + intrestAmtInWords + "', NET_DISBURSED_AMOUNT='" + netDisburseAmt
				+ "',NET_DISBURSED_AMOUNT_IN_WORDS='" + netDisburseAmtInWords + "',TOTAL_AMT_PAID_BORROWES='"
				+ totalAmtPaidByBorrowers + "',TOTAL_AMT_PAID_BORROWES_WORDS='" + totalAmtPaidByBorrowersnWords
				+ "',EMI='" + EMIAmount + "', EMI_IN_WORDS='" + EMIInWords + "',"
				//+ "OTHERCHARGES='" + otherCharges
				//+ "',   DDECHARGES='" + dDEfeeCharges + "',STAMPCHARGES='" + String.format("%.2f", stampAmtCharges)
				//+ "', BRANCH='" + branchName + "',CESSCHARGES=" + cessChargesDB + "  "
				+ "WHERE WINAME= '"+ processInstanceId + "'";
		Log.consoleLog(ifr, "QueryamtInWords==>" + QueryamtInWords);
		Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
		ifr.saveDataInDB(QueryamtInWords);
		ifr.saveDataInDB(QueryIntrestAmount);

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + calculateMinFinal + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		ifr.setValue("STAFF_PROCESSINGFEE", AcceleratorConstants.PROCESSINGFEE);
		Log.consoleLog(ifr, "EMIAmount===> : " + EMIAmount);
		ifr.setValue("STAFF_EMI", EMIAmount);
		Log.consoleLog(ifr, "tenure===> : " + tenure);
		ifr.setValue("STAFF_TENURE", tenure);
		Log.consoleLog(ifr, "EMIAmount after rounding off===> : " + EMIAmount);
		return String.valueOf(calculateMinFinal);
	}

	private double calculateMin(double eligibleAsPerNth, double eligiblityasPerPolicy, double amtRequested,
			IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryForNthCal = "SELECT IS_ELG_FOR_NTH FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "queryForNthCal query===>" + queryForNthCal);
		List<List<String>> resqueryForNthCal = ifr.getDataFromDB(queryForNthCal);
		if (!resqueryForNthCal.get(0).get(0).isEmpty()) {
			return Math.min(eligibleAsPerNth, Math.min(eligiblityasPerPolicy, amtRequested));
		} else {
			return Math.min(eligiblityasPerPolicy, amtRequested);
		}
	}

	private double calculatePMT(int tenure, double roi) {
		double monthlyInterestRate = roi / 100 / 12;
		double numerator = monthlyInterestRate * 100000;
		double denominator = 1 - Math.pow(1 + monthlyInterestRate, -tenure);

		return numerator / denominator;
	}

	public String mClickAvailHRMSButton(IFormReference ifr, String control) {
		Log.consoleLog(ifr, "Inside mClickAvailHRMSButton");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String amtRequest = "";
			String empid = "";

			String tenure = "";

			String loanType = "";

			String queryForLoanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
			Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
			List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
			Log.consoleLog(ifr, "res===>" + res);
			if (!res.isEmpty()) {
				loanType = res.get(0).get(0);
				ifr.setValue("Q_CurrBranchType", loanType);
			}
			String slidderValues = pcm.getSliderAmount(ifr);
			Log.consoleLog(ifr, "slidderValues : " + slidderValues);
			if (slidderValues.equalsIgnoreCase("")) {
				return pcm.returnError(ifr);
			}
			String[] slidderValuesarr = slidderValues.split("~");
			amtRequest = slidderValuesarr[0];
			Log.consoleLog(ifr, "amtRequest : " + amtRequest);
			tenure = slidderValuesarr[1];
			Log.consoleLog(ifr, "tenure : " + tenure);

			String Querytenure = "UPDATE SLOS_STAFF_TRN SET TENURE_MONTHS= '" + tenure + "' WHERE WINAME= '"
					+ processInstanceId + "'";

			ifr.saveDataInDB(Querytenure);
			Log.consoleLog(ifr, "Querytenure : " + Querytenure);

			String response = getLoanAmt(ifr, processInstanceId, amtRequest, tenure, "availoffer");
			if (response.toLowerCase().contains("error")) {
				return response;
			}

			ifr.setStyle("navigationBackBtn", "disable", "false");
			ifr.setStyle("navigationNextBtn", "disable", "false");
		} catch (Exception e) {
			Log.consoleLog(ifr, "Inside mImpOnClickAvailButton" + e);
			Log.errorLog(ifr, "Inside mImpOnClickAvailButton" + e);
		}
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Avail Offer' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
		return "success next button enabled";
	}

	public String onLoadDocUploadKavach(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Insurance Details' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		List<List<String>> listOptionalDocuments = new ArrayList<>();
		String getOptionalDocuments = "SELECT NAME from SLOS_KAVACH_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND GENERATE = 'N'";
		listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
		Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);
		List<String> responseForGrid = listOptionalDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());
		Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
		String[] Columnheading = new String[] { "Document Name" };
		ifr.clearTable("Doc_Table_Portal_Kavach");
		String createdDateT = "";
		String createdDateTime = "SELECT d.CREATEDDATETIME, f.NAME FROM PDBDOCUMENT d JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX WHERE f.NAME = '" + processInstanceId + "' ORDER BY d.CREATEDDATETIME DESC FETCH FIRST 1 ROW ONLY";
		List<List<String>> listcreatedDateTime = ifr.getDataFromDB(createdDateTime);
		Log.consoleLog(ifr, "createdDateTime==>" + createdDateTime);
		if (listcreatedDateTime.isEmpty()) {
			JSONArray arrayRes = new JSONArray();
			for (int i = 0; i < responseForGrid.size(); i++) {
				// for (List<String> row : responseForGrid) {
				JSONObject obj = new JSONObject();
				Log.consoleLog(ifr, "Doc_Table_Portal_Kavach==>" + obj);
				obj.put(Columnheading[0], responseForGrid.get(i));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);
			ifr.addDataToGrid("Doc_Table_Portal_Kavach", arrayRes);
		} else {
			createdDateT = listcreatedDateTime.get(0).get(0);
			JSONArray arrayRes = new JSONArray();
			for (int i = 0; i < responseForGrid.size(); i++) {
				// for (List<String> row : responseForGrid) {
				JSONObject obj = new JSONObject();
				Log.consoleLog(ifr, "Doc_Table_Portal_Kavach==>" + obj);
				obj.put(Columnheading[0], responseForGrid.get(i));
				obj.put("Uploaded Date", createdDateT);
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);
			ifr.addDataToGrid("Doc_Table_Portal_Kavach", arrayRes);
		}

		return "Success";
	}

	public String mGenDoc(IFormReference ifr, String value) throws ParseException {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String queryForLoan = "SELECT LOAN_AMOUNT,TENURE_MONTHS FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
				+ "'";

		String loanAmt = "";
		String tenureMonths = "";

		Log.consoleLog(ifr, " query===>" + queryForLoan);
		List<List<String>> result = ifr.getDataFromDB(queryForLoan);
		Log.consoleLog(ifr, "res===>" + result);
		if (!result.isEmpty()) {
			loanAmt = result.get(0).get(0);
			tenureMonths = result.get(0).get(1);
		}

		String queryUpdate = "UPDATE SLOS_STAFF_KAVACH_ELIGIBILITY SET AMOUNT_REQUESTED='" + loanAmt + "' WHERE PID='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		Log.consoleLog(ifr, "Inside mGenDoc====>");
		String loanType = "";
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		String query = "select loan_type from slos_staff_trn where winame='" + pid + "'";
		List<List<String>> res = ifr.getDataFromDB(query);
		if (!res.isEmpty()) {
			loanType = res.get(0).get(0);
		}
		String activityName = ifr.getActivityName();
		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equals("Staff_Kavach_Sanction") || activityName.equals("Staff_Kavach_CO_Sanction")) {
			Log.consoleLog(ifr, "Inside Staff Sanction====>");
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
					+ "' WHERE winame = '" + processInstanceId + "'";

			ifr.saveDataInDB(updateQuerySD);
			String mGenerateDocument = mGenerateDocument(ifr, pid);
			if (mGenerateDocument.contains("error")) {
				return mGenerateDocument;
			}

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '" + pid + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

			String updateQuery = "UPDATE SLOS_TRN_BKOF_DOCUMENTS SET DOCTYPE='SANCTION' where winame='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, " updateQuery SLOS_TRN_BKOF_DOCUMENTS===>" + updateQuery);
			ifr.saveDataInDB(updateQuery);

			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME FROM ( SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME, ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn FROM SLOS_TRN_BKOF_DOCUMENTS WHERE WINAME = '"
					+ pid + "' AND DOCTYPE IS NOT NULL AND NAME <> 'INSURANCEQUATION' ) WHERE rn = 1";
			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
			List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
			String[] Columnheading = new String[] { "Document Name", "Document Status", "Generated Date" };
			ifr.clearTable("Outward_Doc_Details_Grid");
			JSONArray arrayRes = new JSONArray();
			for (List<String> row : responseForGrid) {
				JSONObject obj = new JSONObject();
				Log.consoleLog(ifr, "Outward_Doc_Details_Grid==>" + obj);
				obj.put(Columnheading[0], row.get(0));
				obj.put(Columnheading[1], row.get(1));
				obj.put(Columnheading[2], row.get(2));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);

			ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
		} else {
			Log.consoleLog(ifr, "Inside Staff Post Sanction====>");
			mGenerateDocument(ifr, pid);

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '" + pid + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME FROM ( SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME, ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn FROM SLOS_TRN_BKOF_DOCUMENTS WHERE WINAME = '"
					+ pid + "' AND DOCTYPE IS NULL AND NAME <> 'INSURANCEQUATION' ) WHERE rn = 1";
			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
			List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
			String[] Columnheading = new String[] { "Document Name", "Document Status", "Generated Date" };
			ifr.clearTable("Outward_Doc_Details_Grid");
			JSONArray arrayRes = new JSONArray();
			for (List<String> row : responseForGrid) {
				JSONObject obj = new JSONObject();
				Log.consoleLog(ifr, "Outward_Doc_Details_Grid==>" + obj);
				obj.put(Columnheading[0], row.get(0));
				obj.put(Columnheading[1], row.get(1));
				obj.put(Columnheading[2], row.get(2));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);

			ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);

		}

		String queryForCount = "SELECT COUNT(DISTINCT name) AS distinct_name_count\r\n"
				+ "FROM SLOS_TRN_BKOF_DOCUMENTS d WHERE WINAME = '" + pid + "' AND d.CREATEDDATETIME = (\r\n"
				+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
				+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME\r\n" + "  )";
		Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
		List Result1 = ifr.getDataFromDB(queryForCount);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		if (Integer.parseInt(Count1) > 0) {
			Log.consoleLog(ifr, "Count." + Count1);
			return String.valueOf(Count1);
		}

		return "0";

	}

	private String mGenerateDocument(IFormReference ifr, String processInstanceId) {
		String prodCode = "";
		String prodCodeName = "";
		String nth = "";
		String grossSal = "";
		String loanAmount = "";
		String tenure = "";
		String SBACCNUMBER = "";
		String loanType = "";
		String prodcode = "";
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		String prodData = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%KAVACH%'";

		List<List<String>> listprodData = ifr.getDataFromDB(prodData);
		Log.consoleLog(ifr, "listprodData====>" + listprodData);
		if (!listprodData.isEmpty()) {
			prodcode = listprodData.get(0).get(0);
		}

		String query = "SELECT LOAN_TYPE ,NTH, GROSS_SALARY, LOAN_AMOUNT,TENURE_MONTHS, SALARY_ACC_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty() && res.get(0).get(0).toString().contains("Kavach")) {
			loanType = res.get(0).get(0);
			nth = res.get(0).get(1);
			grossSal = res.get(0).get(2);
			loanAmount = res.get(0).get(3);
			tenure = res.get(0).get(4);
			SBACCNUMBER = res.get(0).get(5);
			String ammortizationHRMS = getAmmortizationHRMS(ifr, processInstanceId, prodcode, Double.parseDouble(nth),
					Double.parseDouble(grossSal), loanAmount, tenure,loanType);
			if (ammortizationHRMS.contains("error")) {
				return ammortizationHRMS;
			}

			mGenerateDoc(ifr, loanType);

		}
		return "success";

	}

	public void mGenerateDoc(IFormReference ifr, String loanType) {

		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		if (loanType != null && loanType.contains("Kavach")) {

			Log.consoleLog(ifr, "Loan Type in mGenerateDoc === " + loanType);

			String getMandatoryDocuments = "SELECT NAME FROM SLOS_KAVACH_DOCUMENTS WHERE MANDATORY='Y' AND GENERATE='Y'";
			Log.consoleLog(ifr, "uploadMandatoryDocs query ==> " + getMandatoryDocuments);

			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments ==> " + listMandatoryDocuments);

			List<String> names = listMandatoryDocuments.stream().flatMap(List::stream).collect(Collectors.toList());

			for (String docName : names) {
				Log.consoleLog(ifr, "Generating document ==> " + docName);
				docGen(ifr, docName, "STAFF_KAVACH");
			}
		}
	}

	public void docGen(IFormReference ifr, String docid, String journeyName) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode userNode = objectMapper.createObjectNode();
			userNode.put("Mode", "SINGLE");
			userNode.put("DocID", docid);
			userNode.put("callFrom", "Backoffice");
			userNode.put("journey", journeyName);
			userNode.put("referenceKey", ifr.getObjGeneralData().getM_strProcessInstanceId());
			userNode.put("Activity", ifr.getActivityName());
			userNode.put("Identifier", "N");
			userNode.put("RPSchedule", "N");
			userNode.put("TypeOfFecility", "Term Loan");
			userNode.put("InterestRate", 8.5D);
			userNode.put("LoanTerm", "Y");
			userNode.put("LoanAmount", 15000);
			GenerateDocument dc = new GenerateDocument();
			JsonNode userDoc = dc.executeDocGenerator(ifr, (JsonNode) userNode);
			Log.consoleLog(ifr,
					"GenerateDoc:generateDoc->Document Generation Engine - Response: " + userDoc.toString());
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in docGen " + e);
			Log.errorLog(ifr, "Error occured in docGen " + e);
		}
	}

	private String getAmmortizationHRMS(IFormReference ifr, String processInstanceId, String prodCode, double nth,
			double grossSal, String loanAmt, String tenureMonths, String loanType) {
		Ammortization AMR = new Ammortization();
		String returnMessage = AMR.ExecuteCBS_AmmortizationHRMS(ifr, processInstanceId, loanAmt, tenureMonths,
				prodCode,loanType);

		Log.consoleLog(ifr, "returnMessage : " + returnMessage);
		String[] retAmmort = returnMessage.split(":");
		if (returnMessage.contains(RLOS_Constants.ERROR)) {
			return pcm.returnErrorcustmessage(ifr, retAmmort[1]);
		}
		String repaymentDate = "";
		String monthString = "";
		String yearString = "";
		String installmentAmount = "";
		String queryForRepaymentDate = "Select REPAYMENTDATE from LOS_STG_CBS_AMM_SCH_DETAILS WHERE PROCESSINSTANCEID = '"
				+ processInstanceId + "'";

		List<List<String>> list = ifr.getDataFromDB(queryForRepaymentDate);
		Log.consoleLog(ifr, "query for queryForRepaymentDate" + queryForRepaymentDate);
		if (!list.isEmpty()) {
			repaymentDate = list.get(0).get(0);
			Log.consoleLog(ifr, "query for repaymentDate" + repaymentDate);
			String dateString = String.valueOf(repaymentDate);
			Log.consoleLog(ifr, "query for dateString" + dateString);
			yearString = dateString.substring(0, 4);
			Log.consoleLog(ifr, "query for yearString" + yearString);
			monthString = dateString.substring(4, 6);
			Log.consoleLog(ifr, "query for monthString" + monthString);
		}

		String queryForNTHAmount = "Select INSTALLMENT from LOS_STG_CBS_AMM_SCH_DETAILS WHERE PROCESSINSTANCEID = '"
				+ processInstanceId + "'and stagenumber=1 and installmentno=1";

		List<List<String>> queryForNTHAmountlist = ifr.getDataFromDB(queryForNTHAmount);
		if (!queryForNTHAmountlist.isEmpty()) {
			installmentAmount = queryForNTHAmountlist.get(0).get(0);
		}
		double nthInProc = nth - Double.parseDouble(installmentAmount);
		String NthInProcessNote = String.valueOf(nthInProc);
		double nthInProcessNSal = nthInProc / grossSal * 100;
		String NthInProcessNotePerSal = String.valueOf(nthInProcessNSal);

		String queryForInst = "UPDATE SLOS_TRN_LOANSUMMARY SET FIRST_INSTALLMENT_MONTH='" + monthString
				+ "',FIRST_INSTALLMENT_YEAR='" + yearString + "',NTH_AMOUNT_PER='" + NthInProcessNotePerSal
				+ "',FINAL_NTH_AMOUNT='" + NthInProcessNote + "' WHERE WINAME='" + processInstanceId + "'";

		Log.consoleLog(ifr, "query for queryForInst" + queryForInst);
		ifr.saveDataInDB(queryForInst);

		String Querytenure = "UPDATE SLOS_STAFF_TRN SET EMI= '" + installmentAmount + "' WHERE WINAME= '"
				+ processInstanceId + "'";

		ifr.saveDataInDB(Querytenure);
		return "";
	}

	public String backOfficeLoanDisburse(IFormReference ifr, String value) {
		String loanaccno = "";
		String disbdate = "";
		String sancAmt = "";
		String loanType = "";
//		String loanType="";
//		String emidtst="";
		String savingAcc = "";
		String status = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String cbsFinalScreenValidation = CBSFinalScreenValidation(ifr, processInstanceId, value, "backOffice");
		String[] cbsFinScreenValidation = cbsFinalScreenValidation.split(",");
		Log.consoleLog(ifr, "cbsFinalScreenValidation=====================> " + cbsFinalScreenValidation);
		Log.consoleLog(ifr, "cbsFinScreenValidation=====================> " + cbsFinScreenValidation);
		Log.consoleLog(ifr, "cbsFinScreenValidation lenghth=====================> " + cbsFinScreenValidation.length);
		String queryForDPNOD = "SELECT LOAN_TYPE,DESIGNATION FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
				+ "'";

		Log.consoleLog(ifr, "Loan Type query===>" + queryForDPNOD);
		List<List<String>> resForDPNOD = ifr.getDataFromDB(queryForDPNOD);
		Log.consoleLog(ifr, "resForDPNOD===>" + resForDPNOD);
		if (!resForDPNOD.isEmpty()) {
			loanType = resForDPNOD.get(0).get(0);

		}
		if (cbsFinScreenValidation.length > 1
				&& cbsFinalScreenValidation.contains(RLOS_Constants.ERROR.toLowerCase())) {
			Log.consoleLog(ifr, "inside if loop");
			return "error" + "," + cbsFinScreenValidation[1];
		}
		if (cbsFinalScreenValidation.contains(RLOS_Constants.ERROR) && cbsFinScreenValidation.length == 1) {
			return "error" + "," + "technical glitch try after sometime";
		}
		if (cbsFinalScreenValidation.contains(RLOS_Constants.SUCCESS)) {
			pcm.mAccCompleteWorkItemStatus(ifr);
			ifr.setStyle("Disb_Loan_OD_Acc_Num", "visible", "true");
			ifr.setStyle("Disb_Loan_Amt", "visible", "true");
			ifr.setStyle("Disb_Loan_Acc_Open_Date", "visible", "true");
			ifr.setStyle("Disb_Savings_Account", "visible", "true");
			if (loanType.contains("Kavach")) {
				getLaonDetails(ifr, processInstanceId);
				return "success" + "," + "Disbursement successful";
			}
		}

		return "error" + "," + "Technical Glitch";

	}

	private String CBSFinalScreenValidation(IFormReference ifr, String processInstanceId, String value,
			String loanType) {
		Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
		Log.consoleLog(ifr, "processInstanceId===>" + processInstanceId);
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		String LoanAccountNumber = "";

		try {
			if (value.contains("Kavach")) {
				String status = objPreprocess.execLoanAccountCreation(ifr, "Kavach");
				String[] loanStatus = status.split(":");
				if (status != null && status.startsWith(RLOS_Constants.ERROR)) {

					String[] statusIds = status.split(":");

					return (statusIds.length > 1) ? "error," + statusIds[1] : "error," + statusIds[0];
				}
				return status;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return "ERROR";
		}
		return RLOS_Constants.ERROR;
	}

	private void getLaonDetails(IFormReference ifr, String processInstanceId) {
		String loanaccno;
		String disbdate;
		String sancAmt;
		String savingAcc;
		String query = "Select a.LOAN_ACCOUNTNO,a.ACCOUNT_CREATEDDATE,a.SANCTION_AMOUNT,b.SALARY_ACC_NUMBER from SLOS_TRN_LOANDETAILS a INNER JOIN SLOS_STAFF_TRN b ON a.PID=b.WINAME  WHERE a.PID  = '"
				+ processInstanceId + "'";

		List<List<String>> list = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query " + query);
		if (!list.isEmpty()) {
			loanaccno = list.get(0).get(0);
			Log.consoleLog(ifr, "loanaccno===>" + loanaccno);
			ifr.setValue("Disb_Loan_OD_Acc_Num", loanaccno);
			disbdate = list.get(0).get(1);
			Log.consoleLog(ifr, "disbdate===>" + disbdate);
			ifr.setValue("Disb_Loan_Acc_Open_Date", disbdate);
			sancAmt = list.get(0).get(2);
			Log.consoleLog(ifr, "sancAmt===>" + sancAmt);
			ifr.setValue("Disb_Loan_Amt", sancAmt);
			savingAcc = list.get(0).get(3);
			Log.consoleLog(ifr, "savingAcc===>" + savingAcc);
			ifr.setValue("Disb_Savings_Account", savingAcc);
		}
	}

	public String uploadMandatoryDocs(IFormReference ifr, String value) {
		String documentNames = "";
		String getMandatoryDocuments = "SELECT NAME from SLOS_KAVACH_DOCUMENTS where MANDATORY = 'Y' AND ENABLE='Y'";
		Log.consoleLog(ifr, "uploadMandatoryDocs query==>" + getMandatoryDocuments);
		List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
		Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
		List<String> names = listMandatoryDocuments.stream().flatMap(tempMap -> tempMap.stream())
				.collect(Collectors.toList());
		return documentNames = names.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));

	}

	public String onClickCalEMIKavach(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String prodcode = "";
		double roi = 0.0;
		String schedCode = "";
		String EMIAmount = "";

		String roiData = "select prod_code,RAT_INDEX,COD_SCHED_TYPE from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%KAVACH%'";

		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
		Log.consoleLog(ifr, "listroiData====>" + listroiData);
		if (!listroiData.isEmpty()) {
			prodcode = listroiData.get(0).get(0);
			roi = Double.parseDouble(listroiData.get(0).get(1));
			schedCode = listroiData.get(0).get(2);
		}
		Ammortization ammortization = new Ammortization();
		String loanAmt = ifr.getValue("REC_LOAN_AMT").toString();
		String tenure = ifr.getValue("REC_LOAN_TENURE").toString();
		if (!tenure.isEmpty() && Integer.parseInt(tenure) > 120) {
			return "error, loan amount cannot be greater than 120";
		}
		String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId, loanAmt,
				tenure, prodcode, schedCode);
		String[] splitError = ammortizationResponse.split(":");
		if (ammortizationResponse.contains("ERROR")) {
			return "error," + splitError[1];
		}

		String queryForEMI = "SELECT Installment FROM LOS_STG_CBS_AMM_SCH_DETAILS WHERE ProcessInstanceId='"
				+ processInstanceId + "' AND StageNumber='1' AND InstallmentNo='1'";
		Log.consoleLog(ifr, "queryForEMI query===>" + queryForEMI);
		List<List<String>> resqueryForEMI = ifr.getDataFromDB(queryForEMI);
		Log.consoleLog(ifr, "resqueryForEMI===>" + resqueryForEMI);

		if (!resqueryForEMI.get(0).get(0).isEmpty()) {
			EMIAmount = resqueryForEMI.get(0).get(0);
		}
		return EMIAmount;
	}

	public String documentsCheck(IFormReference ifr, String activityName) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String getMandatoryDocuments = "SELECT NAME from SLOS_KAVACH_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND GENERATE='N'";
		List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

		List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME = "'" + String.join("','", responseForGrid2) + "'";

		portalDocCheck(ifr, processInstanceId, NAME);
		if (activityName.contains("Staff_Kavach_Branch_Maker")) {

			// portalDocCheck(ifr, processInstanceId, NAME);
			return "SUCCESS";
		}

		if (activityName.equalsIgnoreCase("Staff_Kavach_Branch_Checker")
				|| activityName.equalsIgnoreCase("Staff_Kavach_Sanction")
				|| activityName.equalsIgnoreCase("Staff_Kavach_CO_Sanction")
				|| activityName.equalsIgnoreCase("Staff_Kavach_RO_Maker")
				|| activityName.equalsIgnoreCase("Saff_Kavach_CO_Maker")) {

			// portalDocCheck(ifr, processInstanceId, NAME);
			return "SUCCESS";
		}

		if (activityName.equalsIgnoreCase("Staff_Kavach_Post_Sanction")) {

			/* ---------- Clear Grid ---------- */
			ifr.clearTable("Inward_Doc_Details_Grid");

			JSONArray arrayRes = new JSONArray();
			JSONArray arrayRes1 = new JSONArray();

			String Query1 = "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "' and d.name='All_Document_Upload' order by d.CREATEDDATETIME desc";

			Log.consoleLog(ifr, "Query1 ==> " + Query1);

			List<List<String>> responseQuery1 = ifr.getDataFromDB(Query1);
			ifr.clearTable("Inward_Doc_Details_Grid_Kavach");
			if (!responseQuery1.isEmpty()) {
				String uploadedDate = responseQuery1.get(0).get(2);
				JSONObject nf425Obj = new JSONObject();
				nf425Obj.put("Document Name", "All_Document_Upload");
				nf425Obj.put("Mandatory", "Yes");
				nf425Obj.put("Uploaded Date", uploadedDate);
				arrayRes1.add(nf425Obj);
			} else {
				JSONObject nf425Obj = new JSONObject();
				nf425Obj.put("Document Name", "All_Document_Upload");
				nf425Obj.put("Mandatory", "Yes");
				// nf425Obj.put("Uploaded Date", uploadedDate);
				arrayRes1.add(nf425Obj);
			}

			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			// ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes);
			ifr.setStyle("Inward_Doc_Details_Grid", "visible", "true");
			ifr.addDataToGrid("Inward_Doc_Details_Grid_Kavach", arrayRes1);
			return "SUCCESS";

		}
		return "FAILED";
	}

	private void portalDocCheck(IFormReference ifr, String processInstanceId, String NAME) {
		String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
				+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
				+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
				+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

		cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

		String queryforDocGrid1 = "  SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
				+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_kavach_documents v1 ON d.name = v1.name\r\n"
				+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
				+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
				+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + ")\r\n" + "  )";
		Log.consoleLog(ifr, "queryforDocGrid1==>" + queryforDocGrid1);
		List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
		responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
		Log.consoleLog(ifr, "responseForGrid1==>" + responseForGrid1);
		String[] Columnheading2 = new String[] { "Document Name", "Mandatory" };
		ifr.clearTable("Inward_Doc_DetailsIn_Grid_Kavach");
		JSONArray arrayRes = new JSONArray();

		Set<String> uniqueDocNames = new HashSet<>();
		for (List<String> row : responseForGrid1) {
			// JSONObject obj1 = new JSONObject();
			String docName = row.get(0);
			String mandatoryFlag = row.get(1); // v1.MANDATORY (Y / N)
			String uploadedDate = row.get(2);
			if (uniqueDocNames.add(docName)) {
				JSONObject obj1 = new JSONObject();
				obj1.put("Document Name", docName);

				if ("Y".equalsIgnoreCase(mandatoryFlag)) {
					obj1.put("Mandatory", "Yes");
				} else {
					obj1.put("Mandatory", "No");
				}
				obj1.put("Uploaded Date", uploadedDate);
				Log.consoleLog(ifr, "Inward_Doc_DetailsIn_Grid_Kavach==>" + obj1);
//				obj1.put("Document Name", row.get(0));
//				obj1.put("Mandatory", "Yes");
				arrayRes.add(obj1);
			}
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid_Kavach", arrayRes);
		// ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);
		ifr.setStyle("Inward_Doc_Details_Grid", "visible", "false");
	}
	
	public String checkDocumentOnLoadKavach(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String activityName = ifr.getActivityName();
		String Count1 = "";
		String getMandatoryDocuments = "SELECT NAME FROM SLOS_KAVACH_DOCUMENTS WHERE MANDATORY='Y' AND GENERATE='Y'";
		Log.consoleLog(ifr, "uploadMandatoryDocs query ==> " + getMandatoryDocuments);

		List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
		Log.consoleLog(ifr, "listMandatoryDocuments ==> " + listMandatoryDocuments);

		List<String> names = listMandatoryDocuments.stream().flatMap(List::stream).collect(Collectors.toList());
		
		String nameList = names.stream().map(name -> "'" + name + "'").collect(Collectors.joining(","));
	    if (activityName.equalsIgnoreCase("Staff_Kavach_Sanction")
					|| activityName.equalsIgnoreCase("Staff_Kavach_CO_Sanction")
					|| activityName.equalsIgnoreCase("Staff_Kavach_Post_Sanction") || value.equalsIgnoreCase("NO")) {
				String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
						+ "' AND NAME IN (" + nameList + " ) AND DOCTYPE='SANCTION' \r\n" + ")\r\n" + "WHERE rn = 1";

				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
				Log.consoleLog(ifr, "Count1==>" + Count1);
			}

			if (Integer.parseInt(Count1) >= 6) {
				return getDocCount(ifr, processInstanceId, nameList, value);

			}
		return "FAILED";

	}

private static String getDocCount(IFormReference ifr, String processInstanceId, String NAME, String value) {
	String activityName = ifr.getActivityName();
	if (activityName.equalsIgnoreCase("Staff_Kavach_Sanction") || activityName.equalsIgnoreCase("Staff_Kavach_CO_Sanction")
			|| activityName.equalsIgnoreCase("Staff_HL_Post_Sanction") || value.equalsIgnoreCase("NO")) {
		String queryforDocGrid = "SELECT DISTINCT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
				+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
				+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
				+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
				+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE='SANCTION'\r\n" + ")\r\n" + "WHERE rn = 1";
		Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
		List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
		responseForGrid = ifr.getDataFromDB(queryforDocGrid);
		Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
		//String[] Columnheading = new String[] { "Document Name", "Document Status", "Generated Date" };
		ifr.clearTable("Outward_Doc_Details_Grid");
		JSONArray arrayRes = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Outward_Doc_Details_Grid==>" + obj);
			obj.put("Document Name", row.get(0));
			obj.put("Document Status", "Generated");
			obj.put("Generated Date" , row.get(2));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
	} else {
		String queryforDocGrid = "SELECT DISTINCT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
				+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
				+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
				+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
				+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE IS NULL \r\n" + ")\r\n" + "WHERE rn = 1";
		Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
		List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
		responseForGrid = ifr.getDataFromDB(queryforDocGrid);
		Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
		String[] Columnheading = new String[] { "Document Name", "Document Status", "Generated Date" };
		ifr.clearTable("Outward_Doc_Details_Grid");
		JSONArray arrayRes = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Outward_Doc_Details_Grid==>" + obj);
			obj.put("Document Name", row.get(0));
			obj.put("Document Status", "Generated");
			obj.put("Generated Date", row.get(2));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
		ifr.setStyle("Outward_Doc_Details_Grid", "visible", "true");

	}

	return "SUCCESS";
}

public void selectedLoansKavach(IFormReference ifr, String value) {
	String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	String queryUpdate = "UPDATE SLOS_STAFF_TRN SET LOANS_FOR_INSURANCE='"+value+"' WHERE WINAME='"
			+ processInstanceId + "'";

	Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
	ifr.saveDataInDB(queryUpdate);
	
	
}

public void onLoadLoanSelectedKavach(IFormReference ifr) {
	String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	String query = "select LOAN_ACC_NUMBER,productname,limit,OUTSTANDING_BALANCE,EMI,roi,tenure from SLOS_ALL_ACTIVE_PRODUCT where winame='"
			+ processInstanceId + "'";
	Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT==>" + query);
	List<List<String>> responseForGrid = ifr.getDataFromDB(query);
	responseForGrid = ifr.getDataFromDB(query);
	for (List<String> row : responseForGrid) {
		JSONObject obj = new JSONObject();
		Log.consoleLog(ifr, "Existing_Loan_Details===>" + obj);
		String accnumber = row.get(0);
		String first4 = accnumber.trim().substring(0, 4);
		String last4 = accnumber.trim().substring(accnumber.trim().length() - 4);
		String middlePart = "X".repeat(accnumber.trim().length() - 8);
		ifr.addItemInCombo("covered_loans", accnumber, accnumber);
	}

}
	


}
