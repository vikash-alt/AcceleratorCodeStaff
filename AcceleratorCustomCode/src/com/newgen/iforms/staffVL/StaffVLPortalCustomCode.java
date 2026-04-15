package com.newgen.iforms.staffVL;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newgen.dlp.docgen.gen.GenerateDocument;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryHRMSData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.APIPreprocessor;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.dlp.integration.staff.common.APIHrmsPreprocessor;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.LoanAmtInWords;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.vl.VLAPIPreprocessor;

/**
 * 
 */
public class StaffVLPortalCustomCode {
	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();

	/**
	 * @param ifr
	 * @param empId
	 * @return
	 * @throws ParseException
	 */
	public String staffDetailsVL(IFormReference ifr, String empId) throws ParseException {
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
//
//		cf.mExecuteQuery(ifr, Query1, "mImpOnClickFianlEligibility ");

		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE,GENERATE_DOC) SELECT '"
				+ processInstanceId + "', '" + strCurDateTime
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
		// List<List<String>> list = cf.mExecuteQuery(ifr, query,
		// "getCustomerIDFromPAPLMaster:");
		List<List<String>> list = ifr.getDataFromDB(query);
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (!list.isEmpty()) {
			customerId = list.get(0).get(0);
			ifr.setValue("Q_CUSTOMERID", customerId);
			Log.consoleLog(ifr, "customerId" + customerId);
			gender = list.get(0).get(1).toString();
			Log.consoleLog(ifr, "gender" + gender);
			// ifr.setValue("Staff_Gender", list.get(0).get(1));
			ifr.setValue("AGE_VL", list.get(0).get(3));
			// this code should be reverted when giving build to UAT
			LocalDateTime dateTime = LocalDateTime.parse(list.get(0).get(2), inputFormatter);
			String dateOnly = dateTime.toLocalDate().toString();
			ifr.setValue("DATEOFBIRTH", dateOnly);

//			ifr.setValue("DATEOFBIRTH", list.get(0).get(2));
		}
		String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age='" + list.get(0).get(3) + "' " + "where WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
		ifr.saveDataInDB(ageQuery);

		HRMS hrms = new HRMS();
		// String response = hrms.getHrmsDetailsVL(ifr, empId, true, "Staff Vehicle");
		String hrmsDetails = hrms.getHrmsDetailsVL(ifr, empId, true, "Staff Vehicle");
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
//		if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
//			return "error" + "," + "technical glitch";
//		}

		if (hrmsDetails.contains("Invalid Staff ID")) {
			return "error" + ","
					+ "The staff ID is not associated with the logged-in user, as per the provided mobile number,Customer ID/Account Number, or PAN details";
		}
		if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		JSONObject hrmsObject = (JSONObject) parser.parse(hrmsDetails);
		String designation = hrmsObject.get("Designation").toString();
		String probation = hrmsObject.get("Probation").toString();
		String totalDeduction = hrmsObject.get("TotalDeduction").toString();
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
		String validProductCodeQuery = "SELECT productcode,wheelertype FROM STAFF_VL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
		List<String> fourWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "4");
		List<String> twoWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "2");
		List<String> temp = new ArrayList<>();
		temp.addAll(fourWheelerProduct);
		temp.addAll(twoWheelerProduct);
		// IFormReference ifr, String ProcessInstanceId, String CustomerID,
		// String salaryacc, String designation, String probation,List<String>
		// validProductCode
		Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
		String adv360Response = adv360.executeCBSAdvanced360Inquiryv2VL(ifr, processInstanceId, customerId, salaryAcc,
				designation, probation, temp, false, "vl");

//		String deleteQuery = "DELETE FROM SLOS_ALL_ACTIVE_PRODUCT WHERE ROWID IN (SELECT rid FROM ( SELECT ROWID AS rid, ROW_NUMBER() OVER (PARTITION BY winame, LOAN_ACC_NUMBER ORDER BY ROWID) AS rn FROM SLOS_ALL_ACTIVE_PRODUCT WHERE winame = '"+processInstanceId+"' AND LOAN_ACC_NUMBER = '"+accountId.trim()+"') WHERE rn > 1)";
//		Log.consoleLog(ifr, "deleteQuery qquery==>" + deleteQuery);
//		ifr.saveDataInDB(deleteQuery);

		if (adv360Response.equalsIgnoreCase(RLOS_Constants.ERROR)) {
			return "error" + "," + adv360Response;
		}
		JSONObject adv360Obj = (JSONObject) parser.parse(adv360Response);

		String amtInstal = adv360Obj.get("AmtInstal").toString();
		String amtOdInt = adv360Obj.get("AmtOdInt").toString();

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

		// ifr.setValue("TOTAL_DED", amtOdInt);
		if (!adv360Obj.get("Warning").toString().equalsIgnoreCase("NoError")) {
			warning = adv360Obj.get("Warning").toString();
		}
		return gender + "$" + "SUCCESS : " + warning;
	}

	/**
	 * @param ifr
	 * @return
	 * @throws ParseException
	 */
	// public String staffDetailsLoanAvailbilityCalc(IFormReference ifr) throws
	// ParseException {}
	public String staffDetailsLoanAvailbilityCalc(IFormReference ifr) throws ParseException {
		Log.consoleLog(ifr, "into staffDetailsLoanAvailbilityCalc");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String designation = "";
		String salaryAccNumber = "";
		String probation = "";
		String customerId = "";
		String purposeLoan = "";
		String fuelType = "";
		Validator valid = new KnockOffValidator("");
		JSONParser parser = new JSONParser();
		String query = "SELECT designation,salary_acc_number,probation from slos_staff_trn where winame='"
				+ processInstanceId + "'";
		List<List<String>> queryResult = ifr.getDataFromDB(query);
		if (!queryResult.isEmpty()) {
			designation = queryResult.get(0).get(0);
			salaryAccNumber = queryResult.get(0).get(1);
			if (queryResult.get(0).get(2).toLowerCase().equals("")) {
				return "error, Probation data not found. Please reach out to admin";
			}
			probation = queryResult.get(0).get(2).toLowerCase().contains("y") ? "Y" : "N";
		} else {
			return "error,Techincal glitch. Please try again";
		}
		String getCutomerIdQuery = "SELECT customerId from los_trn_customerSummary where winame='" + processInstanceId
				+ "'";
		List<List<String>> customerIdQueryRes = ifr.getDataFromDB(getCutomerIdQuery);
		Log.consoleLog(ifr, "getCutomerIdQuery-->" + getCutomerIdQuery);
		if (!customerIdQueryRes.isEmpty()) {
			customerId = customerIdQueryRes.get(0).get(0);
		}
		String validProductCodeQuery = "SELECT productcode,wheelertype FROM STAFF_VL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
		List<String> fourWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "4");
		List<String> twoWheelerProduct = valid.getFlatList(ifr, validProductCodeResult, "2");
		Log.consoleLog(ifr, "validProductCodeQuery--->" + validProductCodeQuery);
		Log.consoleLog(ifr, "fourWheelerProduct--->" + fourWheelerProduct);
		Log.consoleLog(ifr, "twoWheelerProduct--->" + twoWheelerProduct);
		List<String> temp = new ArrayList<>();
		temp.addAll(fourWheelerProduct);
		temp.addAll(twoWheelerProduct);
		Log.consoleLog(ifr, "temp--->" + temp);

		double fourWheelerAmtUtilized = 0.0;
		double fourWheelerAmtUtil = 0.0;
		double twoWheelerAmtUtil = 0.0;
		double twoWheelerAmtUtilized = 0.0;
		purposeLoan = (String) ifr.getValue("Loan_Purpose_VL");
		fuelType = (String) ifr.getValue("Fuel_Type_VL");
		String vehicleCategory = ifr.getValue("VehicleCat_VL").toString();
		if (vehicleCategory.isEmpty()) {
			return "error, Please choose vehicle category";
		}
		if (purposeLoan.equals("")) {
			return "error, please choose loan & fuel Type";
		}
		if (fuelType.equals("")) {
			return "error, please choose fuel Type";
		}
		Log.consoleLog(ifr,
				"purposeLoan==>" + purposeLoan + " fuelType==>" + fuelType + " vehicleCategory==>" + vehicleCategory);
		String schemeLimitQuery = "SELECT prd_rng_to, category,SUB_PRODUCT_CODE_CBS,SUB_PRODUCT from staff_vl_prd_designation_matrix where designation='"
				+ designation + "' and lower(loan_purpose) like '%" + purposeLoan
				+ "%' and lower(loan_purpose) like lower('%" + vehicleCategory + "%') and fuel_type='" + fuelType
				+ "' and PROBATION_TAG='" + probation + "'";
		List<List<String>> schemeLimitQueryRes = ifr.getDataFromDB(schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQuery" + schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQueryRes==>" + schemeLimitQueryRes);
		if (schemeLimitQueryRes.isEmpty()) {
			return "error, Designation  not found. Please reach out to admin";
		}
		String result = "";
		try {
			if (schemeLimitQueryRes.get(0).get(3).toString() != null
					&& schemeLimitQueryRes.get(0).get(3).toString().contains("-")) {
				result = schemeLimitQueryRes.get(0).get(3).toString().split("-", 2)[0];
				ifr.setValue("Q_CurrBranchType", result);
			} else {
				result = schemeLimitQueryRes.get(0).get(3).toString();
				ifr.setValue("Q_CurrBranchType", result);
			}
		} catch (Exception e) {
			// handle any unexpected runtime issue
			Log.consoleLog(ifr, "error while extracting the data");

		}

		ifr.setValue("Prod_Scheme_Desc", schemeLimitQueryRes.get(0).get(3).toString());
		ifr.setStyle("Prod_Scheme_Desc", "disable", "true");
		double reaminigBalanceFor2wheeler = 0.0;
		double reaminigBalanceFor4wheeler = 0.0;
		String jsonString = "";
		String Query = "SELECT PRODUCTCODEUSED  from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
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
			for (List<String> res1 : validProductCodeResult) {
				Log.consoleLog(ifr, "value added==>" + res1.get(0));
				jsonObjInp.put(res1.get(0), 0);
			}
			jsonString = jsonObjInp.toString();
			Log.consoleLog(ifr, "jsonString valid product code result==> " + jsonString);
			// return "error, technical glitch";
		}
		JSONParser pars = new JSONParser();

		String vehicleCondition = ifr.getValue("vehicle_Type_Condition").toString();

		if (purposeLoan.toLowerCase().contains("four")) {
			Double totaUtilization = 0.0;
			JSONObject jsonObject = (JSONObject) pars.parse(jsonString);
			Log.consoleLog(ifr, "jsonObject for Four Wheeler" + jsonObject);
			for (String productCode : fourWheelerProduct) {
				String code = productCode.trim();
				Log.consoleLog(ifr, "code" + code);
				if (jsonObject.containsKey(code)) {
					Object value = jsonObject.get(code);

					if (value instanceof Number) {
						totaUtilization += ((Number) value).doubleValue();
						Log.consoleLog(ifr, "inside totaUtilization " + totaUtilization);
					}
				}
				Log.consoleLog(ifr, "inside totaUtilization" + totaUtilization);
				ifr.setStyle("Two_VL_Loan_Elg", "visible", "false");
				ifr.setStyle("Two_VL_Loan_Util", "visible", "false");
				ifr.setStyle("Two_VL_Loan_Avl", "visible", "false");
				double maxAmtEligibleFor4Wheeler = Double.parseDouble(schemeLimitQueryRes.get(0).get(0).toString());
				try {
					Log.consoleLog(ifr, "maxAmtEligibleFor4Wheeler " + maxAmtEligibleFor4Wheeler);
					fourWheelerAmtUtilized = Double
							.parseDouble(jsonObject.get(schemeLimitQueryRes.get(0).get(2).toString()).toString());
					// String vehType = schemeLimitQueryRes.get(0).get(4).toString();
					if (vehicleCondition.contains("New")) {
						fourWheelerAmtUtilized = Math.min(totaUtilization, maxAmtEligibleFor4Wheeler);
					} else {
						fourWheelerAmtUtilized = totaUtilization;
					}
					Log.consoleLog(ifr, "fourWheelerAmtUtilized " + fourWheelerAmtUtilized);
				} catch (Exception e) {
					return "error, required sub product code not found. Please reach out admin";
				}
				reaminigBalanceFor4wheeler = maxAmtEligibleFor4Wheeler - fourWheelerAmtUtilized;
				Log.consoleLog(ifr, "reaminigBalanceFor4wheeler " + reaminigBalanceFor4wheeler);
				ifr.setStyle("Four_VL_Loan_Elg", "visible", "true");
				ifr.setStyle("Four_VL_Loan_Util", "visible", "true");
				ifr.setStyle("Four_VL_Loan_Avl", "visible", "true");
				ifr.setValue("Four_VL_Loan_Elg", schemeLimitQueryRes.get(0).get(0).toString());
				ifr.setValue("Four_VL_Loan_Util", String.valueOf(fourWheelerAmtUtilized));
				ifr.setValue("Four_VL_Loan_Avl", String.valueOf(reaminigBalanceFor4wheeler));
				ifr.setStyle("Four_VL_Loan_Elg", "disable", "true");
				ifr.setStyle("Four_VL_Loan_Util", "disable", "true");
				ifr.setStyle("Four_VL_Loan_Avl", "disable", "true");
				if (reaminigBalanceFor4wheeler <= 0.0) {
					return "error, not eligible for four wheeler loan as balance is 0";
				}
			}
		} else if (purposeLoan.toLowerCase().contains("two")) {
			JSONObject jsonObject = (JSONObject) pars.parse(jsonString);
			Log.consoleLog(ifr, "jsonObject for two Wheeler" + jsonObject);
			Double totaUtilization = 0.0;
			for (String productCode : twoWheelerProduct) {
				String code = productCode.trim();
				if (jsonObject.containsKey(code)) {
					Object value = jsonObject.get(code);

					if (value instanceof Number) {
						totaUtilization += ((Number) value).doubleValue();
						Log.consoleLog(ifr, "inside totaUtilization " + totaUtilization);
					}
				}

			}
//				for (Object value : jsonObject.values()) {
//					totaUtilization += (Double) value;
//				}
			ifr.setStyle("Four_VL_Loan_Elg", "visible", "false");
			ifr.setStyle("Four_VL_Loan_Util", "visible", "false");
			ifr.setStyle("Four_VL_Loan_Avl", "visible", "false");
			double maxAmtEligibleFor2Wheeler = Double.parseDouble(schemeLimitQueryRes.get(0).get(0).toString());
			Log.consoleLog(ifr, "maxAmtEligibleFor2Wheeler " + maxAmtEligibleFor2Wheeler);
			try {
				twoWheelerAmtUtil = Double
						.parseDouble(jsonObject.get(schemeLimitQueryRes.get(0).get(2).toString()).toString());
				twoWheelerAmtUtilized = totaUtilization;
				Log.consoleLog(ifr, "twoWheelerAmtUtilized " + twoWheelerAmtUtilized);
			} catch (Exception e) {
				return "error, required sub product code not found. Please reach out admin";
			}
			reaminigBalanceFor2wheeler = maxAmtEligibleFor2Wheeler - twoWheelerAmtUtilized;
			Log.consoleLog(ifr, "reaminigBalanceFor2wheeler " + reaminigBalanceFor2wheeler);
			ifr.setStyle("Two_VL_Loan_Elg", "visible", "true");
			ifr.setStyle("Two_VL_Loan_Util", "visible", "true");
			ifr.setStyle("Two_VL_Loan_Avl", "visible", "true");
			ifr.setValue("Two_VL_Loan_Elg", schemeLimitQueryRes.get(0).get(0).toString());
			ifr.setValue("Two_VL_Loan_Util", String.valueOf(twoWheelerAmtUtilized));
			ifr.setValue("Two_VL_Loan_Avl", String.valueOf(reaminigBalanceFor2wheeler));
			ifr.setStyle("Two_VL_Loan_Elg", "disable", "true");
			ifr.setStyle("Two_VL_Loan_Util", "disable", "true");
			ifr.setStyle("Two_VL_Loan_Avl", "disable", "true");
//			if (twoWheelerAmtUtil > 0.0) {
//				return "error, not eligible for two wheeler loan as loan exists";
//			}
			if (twoWheelerAmtUtilized > 0.0) {
				return "error, not eligible for two wheeler loan as loan exists";
			}
			if (reaminigBalanceFor2wheeler <= 0.0) {
				return "error, Two wheeler loan amount utilized";
			}
			String dateOfJoining = "";
			String dateOfRetirement = "";
			int years = 0;
			int remainingService = 0;
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
				if (dateOfJoining.isEmpty() || dateOfJoining == null) {
					Log.consoleLog(ifr, "inside dateOfJoining ====>");
					return "error,date of Joining is Blank";
				} else {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					Log.consoleLog(ifr, "inside else dateOfJoining ====>");
					LocalDate startDate = LocalDate.parse(dateOfJoining.trim(), formatter);
					Log.consoleLog(ifr, "startDate====>" + startDate);
					LocalDate currentDate = LocalDate.now();
					Log.consoleLog(ifr, "currentDate====>" + currentDate);
					Period period = Period.between(startDate, currentDate);
					Log.consoleLog(ifr, "period====>" + period);
					years = period.getYears();
					Log.consoleLog(ifr, "Completed_Years -> " + years);
				}
				if (dateOfRetirement.isEmpty() || dateOfRetirement == null) {
					Log.consoleLog(ifr, "inside dateOfRetirement ====>");
					return "error,date of retirement is Blank";
				} else {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					Log.consoleLog(ifr, "inside else dateOfRetirement ====>");
					LocalDate dateOfRetire = LocalDate.parse(dateOfRetirement.trim(), formatter);
					// LocalDate dateOfRetire = LocalDate.parse(dateOfRetirement.trim());
					Log.consoleLog(ifr, "dateOfRetire====>" + dateOfRetire);
					LocalDate currentDate = LocalDate.now();
					Log.consoleLog(ifr, "currentDate====>" + currentDate);
					Period period1 = Period.between(currentDate, dateOfRetire);
					remainingService = period1.getYears();
					Log.consoleLog(ifr, "Remaining_Years -> " + remainingService);

					String Query1 = "UPDATE SLOS_STAFF_TRN SET COMPLETED_YEARS= '" + years + "',REMAINING_SERVICE='"
							+ remainingService + "' WHERE WINAME= '" + processInstanceId + "'";

					ifr.saveDataInDB(Query1);
					Log.consoleLog(ifr, "update query STAFFVL CODE " + Query1);
				}
			}

		} else {
			return "error, Something went wrong";
		}
		return "SUCCESS";
	}

	/**
	 * @param ifr
	 * @return
	 */
	public String onLoadDocUploadVL(IFormReference ifr) {
		Log.consoleLog(ifr, "into staffDetailsLoanAvailbilityCalc");
		String vT = "";
		String documentsName = "";
		String pensioner = "";
		List docName = new ArrayList<>();
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		List<List<String>> listMandatoryDocuments = new ArrayList<>();
		List<List<String>> listOptionalDocuments = new ArrayList<>();

		String staffTrn = "select vehicle_category,pensioner from slos_staff_trn where winame='" + processInstanceId
				+ "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (!staffTrnRes.isEmpty()) {

			vT = staffTrnRes.get(0).get(0);
			pensioner = staffTrnRes.get(0).get(1);
		} else {
			return "error, vehicle category not found";
		}

		// String vT = ifr.getValue("VALUESETVL").toString();
		if (pensioner.equalsIgnoreCase("YES")) {
			String getOptionalDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND VEHICLE_TYPE IN ('PNSR')";
			listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
			Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);
		}

		if (vT.equalsIgnoreCase("USED")) {
			String getOptionalDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND VEHICLE_TYPE IN ('ALL','USED')";
			listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
			Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);
		} else {
			String getOptionalDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND VEHICLE_TYPE IN ('ALL','NEW')";
			listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
			Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);
		}
		List<String> responseForGrid = listOptionalDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
		String[] Columnheading = new String[] { "Document Name" };
		ifr.clearTable("Doc_Table_Portal_VL");
		JSONArray arrayRes = new JSONArray();
		for (int i = 0; i < responseForGrid.size(); i++) {
			// for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Doc_Table_Portal_VL==>" + obj);
			obj.put(Columnheading[0], responseForGrid.get(i));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Doc_Table_Portal_VL", arrayRes);
		if (vT.equalsIgnoreCase("USED")) {
			String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'N' AND VEHICLE_TYPE IN ('ALL','USED')";
			listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
		} else {
			String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'N' AND VEHICLE_TYPE IN ('ALL','NEW')";
			listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
		}

		List<String> responseForGrid1 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String[] Columnheading1 = new String[] { "Document Name" };
		ifr.clearTable("Opt_Doc_Table_Portal_VL");
		JSONArray arrayRes1 = new JSONArray();
		for (int i = 0; i < responseForGrid1.size(); i++) {
			// for (List<String> row : responseForGrid1) {
			JSONObject obj1 = new JSONObject();
			Log.consoleLog(ifr, "Opt_Doc_Table_Portal_VL==>" + obj1);
			obj1.put(Columnheading1[0], responseForGrid1.get(i));
			arrayRes1.add(obj1);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes1);

		ifr.addDataToGrid("Opt_Doc_Table_Portal_VL", arrayRes1);

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Document Upload' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
		return "Success";

	}

	public String mGenDoc(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryForCount = "";
		String Count1 = "";
		List<List<String>> listgetAllDocuments = new ArrayList<>();
		String activityName = ifr.getActivityName();
		Log.consoleLog(ifr, "activityName====>" + activityName);
		String docGen = "";
		String Query = "SELECT GENERATE_DOC from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		List<List<String>> Output3 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output3);
		if (Output3.size() > 0 && Output3.get(0).size() > 0 && !Output3.get(0).get(0).toString().trim().isEmpty()) {
			docGen = Output3.get(0).get(0).toString().trim();
		}
		if (activityName.equalsIgnoreCase("Staff_VL_Branch_Maker")) {
			String genDoc = mGenerateOneDoc(ifr);
			if (genDoc.contains("error")) {
				return genDoc;
			}

			String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE = 'B'";
			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

			List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			String NAME = "'" + String.join("','", responseForGrid2) + "'";

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

			String queryforDocGrid = "SELECT  DISTINCT\r\n" + " d.NAME, d.docstatus, d.createddatetime,\r\n"
					+ "	COUNT(*) OVER() AS total_count FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "	WHERE d.WINAME = '"
					+ processInstanceId + "'\r\n" + "   AND d.NAME IN (" + NAME + ")\r\n"
					+ "		   AND d.CREATEDDATETIME = (\r\n" + " SELECT MAX(CREATEDDATETIME)\r\n"
					+ "		      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "		      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME\r\n" + " )";
			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
			List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
			ifr.clearTable("Outward_Doc_Details_Grid");
			JSONArray arrayRes = new JSONArray();
			if (!responseForGrid.isEmpty()) {
				List<String> row = responseForGrid.get(0);
				JSONObject obj = new JSONObject();
				Log.consoleLog(ifr, "Outward_Doc_Details_Grid==>" + obj);
				obj.put("Document Name", row.get(0));
				obj.put("Document Status", "Generated");
				obj.put("Generated Date", row.get(2));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);

			ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			if (!responseForGrid.isEmpty()) {
				Count1 = responseForGrid.get(0).get(3);
			}
			// Count1 = Result1.toString().replace("[", "").replace("]", "");
			if (Integer.parseInt(Count1) == 1 && activityName.equals("Staff_VL_Branch_Maker")) {
				return "SUCCESS";
			} else if ("NO".equalsIgnoreCase(docGen)) {
				return "SUCCESS";
			}

			else {
				return "FAILED";
			}
		} else if (activityName.equalsIgnoreCase("Staff_VL_Sanction")
				|| activityName.equalsIgnoreCase("Staff_VL_CO_Sanction")
				|| activityName.equalsIgnoreCase("Staff_VL_Post_Sanction")
				|| activityName.equalsIgnoreCase("Staff_VL_Branch_Checker")) {
			String vT = "";
			String probation = "";
			String emi = "";
			String approveLoanAmt = "";
			String approveLoanAmtInWords = "";
			String emiInWords = "";
			String staffTrn = "select vehicle_category,probation,MI_ROC_VL,APP_LOAN_AMT_VL from slos_staff_trn where winame='"
					+ processInstanceId + "'";
			List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
			Log.consoleLog(ifr, "staffTrn==" + staffTrn);
			Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
			if (!staffTrnRes.isEmpty()) {

				vT = staffTrnRes.get(0).get(0);
				probation = staffTrnRes.get(0).get(1);
				emi = staffTrnRes.get(0).get(2);
				approveLoanAmt = staffTrnRes.get(0).get(3);
			} else {
				return "error, vehicle category not found";
			}
			if (emi != null && !emi.trim().isEmpty()) {
				emiInWords = LoanAmtInWords.amtInWords(Double.parseDouble(emi));
			}
			if (approveLoanAmt != null && !approveLoanAmt.trim().isEmpty()) {
				approveLoanAmtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(approveLoanAmt));
			}

			String QueryamtInWords = "UPDATE SLOS_STAFF_TRN SET  LOAN_AMOUNT_IN_WORDS= '" + approveLoanAmtInWords
					+ "',EMI_IN_WORDS= '" + emiInWords + "' WHERE WINAME= '" + processInstanceId + "'";
			ifr.saveDataInDB(QueryamtInWords);
			Log.consoleLog(ifr, "QueryamtInWords==" + QueryamtInWords);

			if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("Yes")) {
				String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','YES')";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','YES')";
				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
			} else if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("No")) {
				String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','NO')";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','NO')";
				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");

			} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("Yes")) {
				String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','YES')";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','YES')";
				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
			} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("No")) {
				String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','NO')";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','NO')";
				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
			}

			List<String> responseForGrid1 = listgetAllDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			String NAME = "'" + String.join("','", responseForGrid1) + "'";

			if (activityName.equals("Staff_VL_Sanction") || activityName.equals("Staff_VL_CO_Sanction")
					|| activityName.equalsIgnoreCase("Staff_VL_Branch_Checker")) {
				Log.consoleLog(ifr, "Inside Staff Sanction====>");
				Date currentDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				String formattedDate = dateFormat.format(currentDate);

				String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
						+ "' WHERE winame = '" + processInstanceId + "'";

				ifr.saveDataInDB(updateQuerySD);
				getDocGeneration(ifr, processInstanceId);
				String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
						+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
						+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
						+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
						+ processInstanceId + "' AND d.NAME IN (" + NAME + " )";

				Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

				cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

				String updateQuery = "UPDATE SLOS_TRN_BKOF_DOCUMENTS SET DOCTYPE='SANCTION' where winame='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, " updateQuery SLOS_TRN_BKOF_DOCUMENTS===>" + updateQuery);
				ifr.saveDataInDB(updateQuery);

				String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId + "'\r\n"
						+ "      AND NAME IN (" + NAME + ")\r\n" + "      AND DOCTYPE IS NOT NULL\r\n" + ") \r\n"
						+ "WHERE rn = 1";
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
					obj.put(Columnheading[1], "Generated");
					obj.put(Columnheading[2], row.get(2));
					arrayRes.add(obj);
				}
				Log.consoleLog(ifr, "Json Array " + arrayRes);

				ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
			} else if (activityName.equalsIgnoreCase("Staff_VL_Post_Sanction")) {
				Log.consoleLog(ifr, "Inside Staff Post Sanction====>");
				String genDoc = mGenerateDoc(ifr, activityName);
				if (genDoc.contains("error")) {
					return genDoc;
				}
				getDocGeneration(ifr, processInstanceId);

				String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
						+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
						+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
						+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
						+ processInstanceId + "' AND d.NAME IN (" + NAME + " )";

				Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

				cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

				String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId + "'\r\n"
						+ "      AND NAME IN (" + NAME + ") \r\n" + "      AND DOCTYPE IS NULL\r\n" + ") \r\n"
						+ "WHERE rn = 1";
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
					obj.put(Columnheading[1], "Generated");
					obj.put(Columnheading[2], row.get(2));
					arrayRes.add(obj);
				}
				Log.consoleLog(ifr, "Json Array " + arrayRes);

				ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);

			}
//			queryForCount = "SELECT COUNT(DISTINCT name) AS distinct_name_count\r\n"
//					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d WHERE WINAME = '" + processInstanceId
//					+ "' AND d.CREATEDDATETIME = (\r\n" + "      SELECT MAX(CREATEDDATETIME)\r\n"
//					+ "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
//					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME\r\n" + "  )";
//			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
//			List Result1 = ifr.getDataFromDB(queryForCount);
//			Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);
			if (Integer.parseInt(Count1) >= 20 && vT.equalsIgnoreCase("NEW")) {
				Log.consoleLog(ifr, "Count." + Count1);
				return "SUCCESS";
			} else if (Integer.parseInt(Count1) >= 19 && vT.equalsIgnoreCase("USED")) {
				Log.consoleLog(ifr, "Count." + Count1);
				return "SUCCESS";
			}
			else if ("NO".equalsIgnoreCase(docGen)) {
				return "SUCCESS";
			}
		}
		return "FAILED";

	}

	public String mGenerateOneDoc(IFormReference ifr) {
		docGen(ifr, "NF_425", "STAFF_VL");
		return "success";
	}

	public String mGenerateDoc(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "Inside mGenerateDoc method==>");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String fixedFee = "0.0";
		String stampAmt = "0.0";
		String dDECharges = "";
		String loanAmt = "";
		String tenure = "";
		String branchCode = "";
		String esignStatus = "";

		String queryForEsignstatus = "select ESIGNSTATUS FROM SLOS_TRN_LOANSUMMARY where winame='" + processInstanceId
				+ "'";
		List<List<String>> resEsignstatus = ifr.getDataFromDB(queryForEsignstatus);
		if (!resEsignstatus.isEmpty()) {
			esignStatus = resEsignstatus.get(0).get(0);
		}

		if (value.equalsIgnoreCase("Staff_VL_Post_Sanction") && esignStatus.equalsIgnoreCase("YES")) {

			String query = " select app_loan_amt_vl,app_loan_tenure_vl,branch_code from slos_staff_trn where winame='"
					+ processInstanceId + "'";
			List<List<String>> resList = ifr.getDataFromDB(query);
			if (!resList.isEmpty()) {
				loanAmt = resList.get(0).get(0);
				tenure = resList.get(0).get(1);
				branchCode = resList.get(0).get(2);
			} else {
				return "error,Technical glitch";
			}
			Aprvalue(ifr);
			String sqlCharge = "SELECT A.FixedFee,        A.TaxPercentage FROM SLOS_M_FEE_CHARGES A INNER JOIN SLOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode = B.FeeCode WHERE A.IsActive = 'Y' AND B.SCHEMEID = 'SL1' AND (A.MINTENURE <= TO_NUMBER('"
					+ tenure + "') AND TO_NUMBER('" + tenure + "') <= A.MAXTENURE)";
			List<List<String>> listsqlCharge = ifr.getDataFromDB(sqlCharge);
			Log.consoleLog(ifr, "listsqlCharge==>" + sqlCharge);
			Log.consoleLog(ifr, "listsqlChargelistsqlCharge==>" + listsqlCharge);
			if (!listsqlCharge.isEmpty()) {
				fixedFee = listsqlCharge.get(0).get(0);
				dDECharges = listsqlCharge.get(1).get(0);
				Log.consoleLog(ifr, "fixedFee : " + fixedFee);
				Log.consoleLog(ifr, "dDECharges : " + dDECharges);
			}

			double fixedfee = fixedFee.trim().isEmpty() ? 0
					: Double.parseDouble(fixedFee) + Double.parseDouble(fixedFee) * 0.18;
			double dDEfeeCharges = dDECharges.trim().isEmpty() ? 0
					: Double.parseDouble(dDECharges) + Double.parseDouble(dDECharges) * 0.18;
			String state = "";
			String branchName = "";
			String stateCodeForNESL = "";
//		String queryForBaranch = "SELECT DISB_BRANCH FROM SLOS_TRN_LOANDETAILS WHERE PID='" + processInstanceId + "'";
//		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForBaranch);
//		List<List<String>> result = ifr.getDataFromDB(queryForBaranch);
//		if (!result.isEmpty()) {
//			branchCode = result.get(0).get(0);
//		}
			String custParamsDataQry = "SELECT STATE,BRANCHNAME FROM LOS_M_BRANCH WHERE BRANCHCODE=LPAD('" + branchCode
					+ "','5','0') AND ROWNUM=1";

			custParamsDataQry = custParamsDataQry.replaceAll("#BRANCHCODE#", branchCode);
			Log.consoleLog(ifr, "custParamsDataQry==>" + custParamsDataQry);
			List<List<String>> Result = ifr.getDataFromDB(custParamsDataQry);
			Log.consoleLog(ifr, "#Result===>" + Result.toString());
			if (!Result.isEmpty()) {
				state = Result.get(0).get(0);
				branchName = Result.get(0).get(1);
			}

			Log.consoleLog(ifr, "State===>" + state);

			if (state.contains("ERROR")) {
				return "error,state error";
			}
			String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state
					+ "')) AND ROWNUM=1";
			double stampPercentage = 0.0;
			double stampMincharges = 0.0;
			double stampMaxcharges = 0.0;
			double stampAmtChres = 0.0;
			double stampAmtCharges = 0.0;

			Log.consoleLog(ifr, "Query3==>" + Query3);
			List<List<String>> Result3 = ifr.getDataFromDB(Query3);
			Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
			if (!Result3.isEmpty()) {
				stateCodeForNESL = Result3.get(0).get(0);
			}
			try {
				String sqlStateFeeCharge = "Select STAMP_AMT,STAMP_PERCENTAGE,STAMP_MIN_AMT, STAMP_MAX_AMT,AMOUNT_TYPE from SLOS_MST_STATEFEECHARGES WHERE STATE_CODE='"
						+ stateCodeForNESL + "' AND '" + loanAmt
						+ "' BETWEEN LOAN_AMT_MIN AND LOAN_AMT_MAX and schemeid='SVL1'";

				List<List<String>> listsqlStateFeeCharge = ifr.getDataFromDB(sqlStateFeeCharge);
				Log.consoleLog(ifr, "listsqlStateFeeCharge==>" + listsqlStateFeeCharge);
				Log.consoleLog(ifr, "sqlStateFeeCharge==>" + sqlStateFeeCharge);
				if (!listsqlStateFeeCharge.isEmpty()) {
					if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Flat")) {
						stampAmt = listsqlStateFeeCharge.get(0).get(0);
						stampAmtChres = Double.parseDouble(stampAmt);
						stampAmtCharges = (int) Math.ceil(stampAmtChres);
						Log.consoleLog(ifr, "stampAmt : " + stampAmt);
						Log.consoleLog(ifr, "stampAmtCharges from flat charges block : " + stampAmt);
					} else if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Percentage")) {
						stampPercentage = Double.parseDouble(listsqlStateFeeCharge.get(0).get(1));
						stampMincharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(2));
						stampMaxcharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(3));
						Log.consoleLog(ifr, "stampPercentage : " + stampPercentage);
						double stampcharges = Double.parseDouble(loanAmt) * (stampPercentage / 100);
						stampAmtChres = (stampcharges > stampMaxcharges) ? stampMaxcharges
								: (stampcharges < stampMincharges) ? stampMincharges : stampcharges;
						stampAmtCharges = (int) Math.ceil(stampAmtChres);
						Log.consoleLog(ifr, "stampAmtCharges from percentage block : " + stampAmtCharges);

					}
//				stampAmt = String.format("%.2f", stampcharges);

				}

			} catch (Exception ee) {
				return "error,Server error";
			}

			// double stampAmtCharges = Double.parseDouble(stampAmt);
			String otherCharges = String.valueOf(fixedfee + dDEfeeCharges + stampAmtCharges);
			String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET OTHERCHARGES='" + otherCharges
					+ "',   DDECHARGES='" + dDEfeeCharges + "',STAMPCHARGES='" + String.format("%.2f", stampAmtCharges)
					+ "', BRANCH='" + branchName + "'  WHERE WINAME= '" + processInstanceId + "'";
			Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
			ifr.saveDataInDB(QueryIntrestAmount);
		}

		return "success";

	}

	private void getDocGeneration(IFormReference ifr, String processInstanceId) {
		String probation = "";
		String vT = "";
		String staffTrn = "select vehicle_category,probation from slos_staff_trn where winame='" + processInstanceId
				+ "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (!staffTrnRes.isEmpty()) {

			vT = staffTrnRes.get(0).get(0);
			probation = staffTrnRes.get(0).get(1);
		}

		if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("No")) {
			docGen(ifr, "RTO_FORM_20", "STAFF_VL");
			docGen(ifr, "RTO_FORM_29", "STAFF_VL");
			docGen(ifr, "RTO_FORM_30", "STAFF_VL");
			docGen(ifr, "RTO_FORM_34", "STAFF_VL");
			docGen(ifr, "RTO_FORM_35", "STAFF_VL");
			// docGen(ifr, "NF_425", "STAFF_VL");
			docGen(ifr, "NF_546", "STAFF_VL");
			docGen(ifr, "NF_967", "STAFF_VL");
			docGen(ifr, "NF_803", "STAFF_VL");
			docGen(ifr, "NF_373", "STAFF_VL");
			docGen(ifr, "NF_1024", "STAFF_VL");
			docGen(ifr, "PROCESS_NOTE", "STAFF_VL");
			docGen(ifr, "REPAYMENT_LETTER", "STAFF_VL");
			docGen(ifr, "CUSTOMER_SANCTION_LETTER", "STAFF_VL");
			docGen(ifr, "KFS", "STAFF_VL");
			docGen(ifr, "NF_825", "STAFF_VL");
			docGen(ifr, "BRANCH_SANCTION", "STAFF_VL");
			docGen(ifr, "NF_681", "STAFF_VL");
			docGen(ifr, "Appendix_13", "STAFF_VL");
			docGen(ifr, "Appendix_10", "STAFF_VL");
			docGen(ifr, "NF_482", "STAFF_VL");
			// docGen(ifr, "NF_928", "STAFF_VL");
			// docGen(ifr, "NF_1088", "STAFF_VL");

		} else if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("Yes")) {
			docGen(ifr, "RTO_FORM_20", "STAFF_VL");
			docGen(ifr, "RTO_FORM_29", "STAFF_VL");
			docGen(ifr, "RTO_FORM_30", "STAFF_VL");
			docGen(ifr, "RTO_FORM_34", "STAFF_VL");
			docGen(ifr, "RTO_FORM_35", "STAFF_VL");
			// docGen(ifr, "NF_425", "STAFF_VL");
			docGen(ifr, "NF_546", "STAFF_VL");
			docGen(ifr, "NF_967", "STAFF_VL");
			docGen(ifr, "NF_803", "STAFF_VL");
			docGen(ifr, "NF_373", "STAFF_VL");
			docGen(ifr, "NF_1024", "STAFF_VL");
			docGen(ifr, "PROCESS_NOTE", "STAFF_VL");
			docGen(ifr, "REPAYMENT_LETTER", "STAFF_VL");
			docGen(ifr, "CUSTOMER_SANCTION_LETTER", "STAFF_VL");
			docGen(ifr, "KFS", "STAFF_VL");
			// docGen(ifr, "NF_825", "STAFF_VL");
			docGen(ifr, "BRANCH_SANCTION", "STAFF_VL");
			docGen(ifr, "NF_681", "STAFF_VL");
			docGen(ifr, "Appendix_13", "STAFF_VL");
			docGen(ifr, "Appendix_10", "STAFF_VL");
			docGen(ifr, "NF_482", "STAFF_VL");
			// docGen(ifr, "NF_928", "STAFF_VL");
			docGen(ifr, "NF_1088", "STAFF_VL");

		} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("No")) {
			docGen(ifr, "RTO_FORM_20", "STAFF_VL");
			docGen(ifr, "RTO_FORM_29", "STAFF_VL");
			docGen(ifr, "RTO_FORM_30", "STAFF_VL");
			docGen(ifr, "RTO_FORM_34", "STAFF_VL");
			docGen(ifr, "RTO_FORM_35", "STAFF_VL");
			// docGen(ifr, "NF_425", "STAFF_VL");
			docGen(ifr, "NF_546", "STAFF_VL");
			docGen(ifr, "NF_967", "STAFF_VL");
			docGen(ifr, "NF_803", "STAFF_VL");
			// docGen(ifr, "NF_373", "STAFF_VL");
			docGen(ifr, "NF_1024", "STAFF_VL");
			docGen(ifr, "PROCESS_NOTE", "STAFF_VL");
			docGen(ifr, "REPAYMENT_LETTER", "STAFF_VL");
			docGen(ifr, "CUSTOMER_SANCTION_LETTER", "STAFF_VL");
			docGen(ifr, "KFS", "STAFF_VL");
			docGen(ifr, "NF_825", "STAFF_VL");
			docGen(ifr, "BRANCH_SANCTION", "STAFF_VL");
			docGen(ifr, "NF_681", "STAFF_VL");
			docGen(ifr, "Appendix_13", "STAFF_VL");
			docGen(ifr, "Appendix_10", "STAFF_VL");
			docGen(ifr, "NF_482", "STAFF_VL");
			// docGen(ifr, "NF_928", "STAFF_VL");
			// docGen(ifr, "NF_1088", "STAFF_VL");

		} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("Yes")) {
			docGen(ifr, "RTO_FORM_20", "STAFF_VL");
			docGen(ifr, "RTO_FORM_29", "STAFF_VL");
			docGen(ifr, "RTO_FORM_30", "STAFF_VL");
			docGen(ifr, "RTO_FORM_34", "STAFF_VL");
			docGen(ifr, "RTO_FORM_35", "STAFF_VL");
			// docGen(ifr, "NF_425", "STAFF_VL");
			docGen(ifr, "NF_546", "STAFF_VL");
			docGen(ifr, "NF_967", "STAFF_VL");
			docGen(ifr, "NF_803", "STAFF_VL");
			// docGen(ifr, "NF_373", "STAFF_VL");
			docGen(ifr, "NF_1024", "STAFF_VL");
			docGen(ifr, "PROCESS_NOTE", "STAFF_VL");
			docGen(ifr, "REPAYMENT_LETTER", "STAFF_VL");
			docGen(ifr, "CUSTOMER_SANCTION_LETTER", "STAFF_VL");
			docGen(ifr, "KFS", "STAFF_VL");
			// docGen(ifr, "NF_825", "STAFF_VL");
			docGen(ifr, "BRANCH_SANCTION", "STAFF_VL");
			docGen(ifr, "NF_681", "STAFF_VL");
			docGen(ifr, "Appendix_13", "STAFF_VL");
			docGen(ifr, "Appendix_10", "STAFF_VL");
			docGen(ifr, "NF_482", "STAFF_VL");
			// docGen(ifr, "NF_928", "STAFF_VL");
			docGen(ifr, "NF_1088", "STAFF_VL");

		}
	}

	public String Aprvalue(IFormReference ifr) {
		try {
			Log.consoleLog(ifr, "inside Aprvalue ");
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String query = " select (select OTHERCHARGES from slos_trn_loansummary where WINAME='" + PID
					+ "') as LOC ,APPROVED_LOAN_AMT,APPROVED_TENURE,EFFECTIVE_ROI from SLOS_STAFF_TRN\r\n"
					+ "where WINAME = '" + PID + "'";
			List<List<String>> result = ifr.getDataFromDB(query);
			double calculateAPR = calculateAPR(ifr, Double.parseDouble(result.get(0).get(0)),
					Integer.parseInt(result.get(0).get(1)), Integer.parseInt(result.get(0).get(2)),
					Double.parseDouble(result.get(0).get(3)));
			Log.consoleLog(ifr, "inside Aprvalue  calculateAPR" + calculateAPR);
//	        String deletequery = "delete from los_nl_aprval where PID='" + PID + "'";
//	        Log.consoleLog(ifr, "inside deletequeryforapr  calculateAPR" + deletequery);
//	        cf.mExecuteQuery(ifr, deletequery, "Logs for deletequery  " + PID);
			String aprQuery = "UPDATE SLOS_TRN_LOANSUMMARY set APRVALUE='" + calculateAPR + "' " + "where WINAME='"
					+ PID + "'";

			Log.consoleLog(ifr, "aprQuery====>" + aprQuery);
			ifr.saveDataInDB(aprQuery);

			String insertqueryforapr = ConfProperty.getQueryScript("APRINSERT").replaceAll("#PID#", PID)
					.replaceAll("#APR#", Double.toString(calculateAPR));
			Log.consoleLog(ifr, "inside insertqueryforapr  calculateAPR" + insertqueryforapr);
			cf.mExecuteQuery(ifr, insertqueryforapr, "Logs for insert " + PID);
			return "Success";
			// }
			// else {
			// return RLOS_Constants.ERROR;
			// }

		} catch (Exception e) {
			Log.consoleLog(ifr, "inside Aprvalue PortalCommonMethods Exception  calculateAPR" + e);
			return RLOS_Constants.ERROR;
		}

	}

	public double calculateAPR(IFormReference ifr, double LOC, int loanamt, int tenure, double roi) {
		try {
			// Input validation
			if (LOC < 0 || LOC > 50000000) {
				Log.consoleLog(ifr, "Invalid LOC value. LOC should be between 0 and 50000000.");
				return -1;
			}

			if (loanamt <= 0 || tenure <= 0 || roi < 0) {
				Log.consoleLog(ifr, "Invalid input values. Loan amount, tenure, and ROI must be positive.");
				return -1;
			}

			// Initialize variables
			double periods = tenure;
			double charges = LOC;
			double present = loanamt - charges;
			double guess = 0.01, future = 0, type = 0;
			double ROI = roi / 100;
			double rateI = ROI / 12, fv = 0;
			double pvif = Math.pow(1 + rateI, periods);
			double pmt = rateI / (pvif - 1) * -(loanamt * pvif + fv);
			double payment = pmt;

			// Set maximum epsilon for end of iteration
			double epsMax = 1e-10;
			// Set maximum number of iterations
			int iterMax = 10;

			// Implement Newton's method
			double y, y0, y1, x0, x1 = 0, f = 0, i = 0;
			double rate = guess;
			if (Math.abs(rate) < epsMax) {
				f = Math.exp(periods * Math.log(1 + rate));
			}

			y0 = present + payment * periods + future;
			y1 = present * f + payment * (1 / rate + type) * (f - 1) + future;
			i = x0 = 0;
			x1 = rate;

			while ((Math.abs(y0 - y1) > epsMax) && (i < iterMax)) {
				rate = (y1 * x0 - y0 * x1) / (y1 - y0);
				x0 = x1;
				x1 = rate;

				if (Math.abs(rate) < epsMax) {
					y = present * (1 + periods * rate) + payment * (1 + rate * type) * periods + future;
				} else {
					f = Math.exp(periods * Math.log(1 + rate));
					y = present * f + payment * (1 / rate + type) * (f - 1) + future;
				}
				y0 = y1;
				y1 = y;
				++i;
			}

			double rate1 = rate * 100;
			double ddk = rate1 * 12;
			DecimalFormat df = new DecimalFormat("#.##");
			String APR = df.format(ddk) + "%";

			Log.consoleLog(ifr, "Calculated APR: " + APR);
			return Double.parseDouble(APR.replace("%", ""));
		} catch (ArithmeticException e) {
			Log.consoleLog(ifr, "Arithmetic error during APR calculation: " + e.getMessage());
		} catch (Exception e) {
			Log.consoleLog(ifr, "An error occurred during APR calculation: " + e.getMessage());
		}

		return -1; // Indicate failure if an exception occurred
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

	/**
	 * @param ifr
	 * @param value
	 */
	public static void calCulateROI(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanAmount = "";
		String tenure = "";
		String[] productCode = value.split(":");
		String[] scheduleCode = productCode[1].split("-");
		if (productCode.length > 1 && scheduleCode.length > 1) {
			String prodcode = productCode[0].trim();
			String queryUpdate = "UPDATE LOS_EXT_TABLE SET CBSPRODUCTCODE='" + prodcode + "' WHERE PID='"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
			ifr.saveDataInDB(queryUpdate);

			String schecode = scheduleCode[0];

			String QueryscheduleCode = "UPDATE SLOS_STAFF_TRN SET SCHEDULE_CODE='" + schecode + "', PRODUCT_CODE_VL= '"
					+ prodcode + "' WHERE WINAME= '" + processInstanceId + "'";
			Log.consoleLog(ifr, "QueryscheduleCode==> " + QueryscheduleCode);
			ifr.saveDataInDB(QueryscheduleCode);

			String rateCodeQuery = "SELECT DISTINCT\r\n" + "    a.cod_rate_chart,\r\n"
					+ "    (a.cod_rate_chart || '--' || a.nam_rate_chart) AS cbs_ratechartcode,\r\n"
					+ "    a.cod_prod,\r\n" + "    a.cod_sched_type,\r\n" + "    a.COD_RATE_REGIME\r\n"
					+ "FROM LN_PROD_RATE_CHART_XREF a,\r\n" + "     LN_SCHED_TYPES b\r\n"
					+ "WHERE a.COD_PROD = b.COD_PROD\r\n" + "  AND a.COD_SCHED_TYPE = b.COD_SCHED_TYPE\r\n"
					+ "  AND a.COD_PROD = '" + prodcode + "'\r\n" + "  AND a.FLG_DELETE = 'N'\r\n"
					+ "  AND a.FLG_MNT_STATUS = 'A'\r\n" + "  AND b.dat_sched_exp >= SYSDATE\r\n"
					+ "  AND a.COD_SCHED_TYPE = '" + schecode + "'\r\n" + "  AND a.COD_RATE_REGIME = 'L'";
			List<List<String>> listrateCodeQuery = ifr.getDataFromDB(rateCodeQuery);
			String queryForLoanType = "SELECT REQ_AMT_VL, REQ_TENURE_VL FROM SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
			List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
			Log.consoleLog(ifr, "res===>" + res);
			if (!res.isEmpty()) {
				loanAmount = res.get(0).get(0);
				// ifr.setValue("Q_DataOnDemand", loanAmount);
				tenure = res.get(0).get(1);
			}
			if (!listrateCodeQuery.isEmpty()) {
				String rateCode = listrateCodeQuery.get(0).get(0);
				String rateCodewithName = listrateCodeQuery.get(0).get(1);
				ifr.setValue("RateChartCode", rateCodewithName);

				String productLevelVarianceQuery = "SELECT cod_rate_chart, ctr_from_term_slab, cod_int_index,rat_var_slab\r\n"
						+ "                FROM pr_rate_chart_detl c\r\n"
						+ "               WHERE c.flg_mnt_status = 'A'\r\n"
						+ "                 AND c.cod_rate_chart =  '" + rateCode + "'\r\n"
						+ "                 and c.cod_rat_type = DECODE(NVL(0,0),0,1,0)\r\n"
						+ "                 AND c.ctr_from_term_slab =\r\n"
						+ "                     (SELECT  decode(cod_tier_criteria_typ,0,0,(SELECT MAX(ctr_from_term_slab)\r\n"
						+ "                        FROM pr_rate_chart_detl d\r\n"
						+ "                       WHERE d.cod_rate_chart = c.cod_rate_chart\r\n"
						+ "                         AND d.flg_mnt_status = c.flg_mnt_status\r\n"
						+ "                         AND d.ctr_amd_no = 1\r\n"
						+ "                         And d.cod_rat_type= c.cod_rat_type\r\n"
						+ "                         AND d.ctr_from_term_slab < '" + tenure
						+ "'))  FROM pr_pricing_policy  f WHERE f.cod_policy = c.cod_policy_no and f.flg_mnt_status = c.flg_mnt_status)\r\n"
						+ "                     \r\n" + "                 AND c.ctr_from_amt_slab =\r\n"
						+ "                     (select decode (cod_tier_criteria_typ,0,0,(SELECT MAX(ctr_from_amt_slab)                        \r\n"
						+ "                         FROM pr_rate_chart_detl e\r\n"
						+ "                        WHERE e.cod_rate_chart = c.cod_rate_chart\r\n"
						+ "                          AND e.flg_mnt_status = c.flg_mnt_status\r\n"
						+ "                          AND e.ctr_amd_no = 1                        \r\n"
						+ "                          AND e.cod_rat_type = c.cod_rat_type                        \r\n"
						+ "                          AND e.ctr_from_amt_slab <= '" + loanAmount
						+ "'))FROM pr_pricing_policy  f WHERE f.cod_policy = c.cod_policy_no and f.flg_mnt_status = c.flg_mnt_status)\r\n"
						+ "                 AND c.ctr_amd_no =\r\n" + "                     (SELECT MAX(ctr_amd_no)\r\n"
						+ "                        FROM pr_rate_chart_detl e\r\n"
						+ "                       WHERE e.cod_rate_chart = c.cod_rate_chart\r\n"
						+ "                         AND e.flg_mnt_status = c.flg_mnt_status\r\n"
						+ "                         AND e.cod_rat_type = c.cod_rat_type                        \r\n"
						+ "                         )";
				List<List<String>> listproductLevelVarianceQuery = ifr.getDataFromDB(productLevelVarianceQuery);
				Log.consoleLog(ifr, "productLevelVarianceQuery===>" + productLevelVarianceQuery);
				if (!listproductLevelVarianceQuery.isEmpty()) {
					String rllR = "";
					String mcLr = "";
					String indexVariance = listproductLevelVarianceQuery.get(0).get(2);
					String productLevelVariance = listproductLevelVarianceQuery.get(0).get(3);
					ifr.setValue("Productlevelvariance", productLevelVariance);
					String RLLRquery = "SELECT RAT_INDX\r\n" + "FROM ba_int_indx_rate\r\n"
							+ "WHERE flg_mnt_status = 'A'\r\n" + "  AND DAT_EFF_INT_INDX <= TRUNC(SYSDATE)\r\n"
							+ "  AND COD_INT_INDX = '" + indexVariance + "'\r\n" + "ORDER BY DAT_EFF_INT_INDX DESC\r\n"
							+ "FETCH FIRST ROW ONLY";
					List<List<String>> listRLLRque = ifr.getDataFromDB(RLLRquery);
					Log.consoleLog(ifr, "RLLRquery===>" + RLLRquery);
					if (!listRLLRque.isEmpty()) {
						rllR = listRLLRque.get(0).get(0);
						Log.consoleLog(ifr, "rllR===>" + rllR);
					} else {
						rllR = "0.0";
					}
					String mcLrQuery = "SELECT RAT_INDX_SPREAD\r\n" + "FROM ba_int_indx_spread_rate\r\n"
							+ "WHERE COD_INT_INDX = '" + indexVariance + "'\r\n"
							+ "ORDER BY DAT_EFF_SPREAD_INDX DESC\r\n" + "FETCH FIRST ROW ONLY";
					List<List<String>> listmcLr = ifr.getDataFromDB(mcLrQuery);
					Log.consoleLog(ifr, "mcLrQuery===>" + mcLrQuery);
					if (!listmcLr.isEmpty()) {
						mcLr = listmcLr.get(0).get(0);
						Log.consoleLog(ifr, "mcLr===>" + mcLr);
					} else {
						mcLr = "0.0";
					}
					Double RllrMclr = Double.parseDouble(rllR) + Double.parseDouble(mcLr);
					Log.consoleLog(ifr, "RllrMclr===>" + RllrMclr);
					ifr.setValue("RLLR_MCLR", String.valueOf(RllrMclr));
					ifr.setValue("Accountlevelvariance", "0");
					Double eROI = RllrMclr + Double.parseDouble(productLevelVariance);
					Log.consoleLog(ifr, "eROI===>" + eROI);
					ifr.setValue("EffectiveROI", String.valueOf(eROI));

				}

			}

		}

	}

	/**
	 * @param ifr
	 */
	public void bamListViewLoad(IFormReference ifr) {

		Log.consoleLog(ifr, "inside bamListViewLoad: ");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId:::::::" + ProcessInstanceId);
		String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);

		List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
		String loan_selected = loanSelected.get(0).get(0);
		Log.consoleLog(ifr, "loan type==>" + loan_selected);
		if (loan_selected.equalsIgnoreCase("Staff Vehicle")) {

			if (ifr.getActivityName().equalsIgnoreCase("Disbursement Maker")) {
				Log.consoleLog(ifr, "inside bamListViewLoad: ");

				String editableFields = "QNL_BAM83_RBI_PURPOSE_CODE,QNL_BAM83_SECTOR,QNL_BAM83_RETAIL_BASEL_II_CUSTOMER_TYPE,QNL_BAM83_SCHEMES,QNL_BAM83_GUARANTEE_COVER,QNL_BAM83_BSR_CODE,QNL_BAM83_SSISUBSEC,QNL_BAM83_STATUSIB,QNL_BAM83_PRI_SECTOR_N_PRI_SECTOR,QNL_BAM83_SPECIAL_BENEFICIARIES,QNL_BAM83_SUB_SCHEME,QNL_BAM83_RAH";
				pcm.controlEnable(ifr, editableFields);

			}

		}

	}

	/**
	 * @param ifr
	 * @return
	 */
	public String createVLLoanDisburse(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanAccountNumber = "";
		Log.consoleLog(ifr, "#createVLLoanDisburse started...");
		try {
			String queryLoanAccNumber = "SELECT LOAN_ACCOUNTNO FROM los_trn_loandetails " + "WHERE PID='"
					+ processInstanceId + "' AND ROWNUM=1";
			Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + queryLoanAccNumber);
			List<List<String>> result = cf.mExecuteQuery(ifr, queryLoanAccNumber, "LoanAccountAvl_Query:");
			if (!result.isEmpty() && result.get(0).get(0) != null && !result.get(0).get(0).isEmpty()) {
				loanAccountNumber = result.get(0).get(0);
				Log.consoleLog(ifr, "LoanAccNumber==>" + loanAccountNumber);
				return "error, Loan Account number already Exists";
			}

			VLAPIPreprocessor objvlPreprocessor = new VLAPIPreprocessor();
			String collateralStatus = objvlPreprocessor.execCollateral(ifr, "VL");
			Log.consoleLog(ifr, "after calling collateral api....." + collateralStatus);
			if (collateralStatus != null && collateralStatus.startsWith(RLOS_Constants.ERROR)) {

				String[] collateralIds = collateralStatus.split(":");

				return (collateralIds.length > 1) ? "error," + collateralIds[1] : "error," + collateralIds[0];
			} else {

				loanAccountNumber = objvlPreprocessor.execLoanAccountCreation(ifr, "VL");
				Log.consoleLog(ifr, "VLBkoffcCustomCode:createVLLoanDisburse->after calling loanAccountNumber api: "
						+ loanAccountNumber);
				if (loanAccountNumber != null && (loanAccountNumber.startsWith(RLOS_Constants.ERROR))) {

					String[] loanAccountNumberIds = loanAccountNumber.split(":");

					return (loanAccountNumberIds.length > 1) ? "error," + loanAccountNumberIds[1]
							: "error," + loanAccountNumberIds[0];
				}

			}

		} catch (ParseException e) {
			Log.consoleLog(ifr, "Exception:/createVLLoanDisburse" + e);
			Log.errorLog(ifr, "Exception:/createVLLoanDisburse" + e);
		}
		return RLOS_Constants.SUCCESS;
	}

	/**
	 * @param ifr
	 */
	public void OnExternalLoanDetailsDetails(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside OnExternalLoanDetailsDetails method");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "select LOAN_TYPE,LOAN_AMOUNT,EMI,OUTST_BALANCE from SLOS_STAFF_LOAN_DEDUCTIONS where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_STAFF_LOAN_DEDUCTIONS==>" + query);
		List<List<String>> responseForGrid = ifr.getDataFromDB(query);
		responseForGrid = ifr.getDataFromDB(query);
		String[] Colheading = new String[] { "Loan Type", "Loan Amount", "EMI", "Outstanding Balance" };
		ifr.clearTable("External_Loan_Details");
		JSONArray array = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "External_Loan_Details===>" + obj);
			obj.put(Colheading[0], row.get(0));
			obj.put(Colheading[1], row.get(1));
			obj.put(Colheading[2], row.get(2));
			obj.put(Colheading[3], row.get(3));
			array.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + array);

		ifr.addDataToGrid("External_Loan_Details", array);

	}

	public String mNESLClick(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanType = "Staff Vehicle";
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
				+ processInstanceId + "'";
		List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
		Log.consoleLog(ifr, "query for esign===>" + queryESign);
		String CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, "", "STAFFVL",
				ifr.getValue("App_Tenure_VL").toString(), loanType, ifr.getValue("App_Loan_Amt_VL").toString());

		String[] fundTransfer = CBS_FundTransfer.split(":");

		if (fundTransfer[0].equalsIgnoreCase(RLOS_Constants.ERROR) && fundTransfer.length > 1) {
			Log.consoleLog(ifr, "Fund Transfer inside===================>");
			return "error" + "," + fundTransfer[1];
		}
		String vehCat = "";
		String probation = "";
		String returnMessage = "";
		String query = " select vehicle_category, probation from slos_staff_trn where winame='" + processInstanceId
				+ "'";
		List<List<String>> resList = ifr.getDataFromDB(query);
		if (!resList.isEmpty()) {
			vehCat = resList.get(0).get(0);
			probation = resList.get(0).get(1);
		} else {
			return "error,Technical glitch";
		}

		if (CBS_FundTransfer.equalsIgnoreCase("SUCCESS")) {
			EsignCommonMethods objEsign = new EsignCommonMethods();
			try {
				String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
				if (Integer.parseInt(triggeredStatus) == 0) {
					EsignIntegrationChannel NESL = new EsignIntegrationChannel();
					if (vehCat.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("Yes")) {
						returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_VL_NEW_P", "eStamping", loanType);
						Log.errorLog(ifr, "returnMessage:" + returnMessage);
					} else if (vehCat.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("No")) {
						returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_VL_NEW", "eStamping", loanType);
						Log.errorLog(ifr, "returnMessage:" + returnMessage);
					} else if (vehCat.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("Yes")) {
						returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_VL_USED_P", "eStamping", loanType);
						Log.errorLog(ifr, "returnMessage:" + returnMessage);
					} else if (vehCat.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("No")) {
						returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_VL_USED", "eStamping", loanType);
						Log.errorLog(ifr, "returnMessage:" + returnMessage);
					}

					if (returnMessage.contains(RLOS_Constants.ERROR)) {
						return pcm.returnErrorHold(ifr);
					}
					if (returnMessage.contains("error")) {
						String[] errorMessage = returnMessage.split(",");
						return "error" + "," + errorMessage[1];
					}
					return returnMessage;
				} else if (resultEsign.get(0).get(0).equalsIgnoreCase("N")) {
					return "SUCCESS";
				} else if (resultEsign.get(0).get(0).equalsIgnoreCase("Y")
						&& resultEsign.get(0).get(1).equalsIgnoreCase("Success")) {
					return "recordFound";
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
				Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
				return "error, Server Error , please try after sometime";
			}
		}
		return "";

	}

	public String UpdateApprovalMatrix(IFormReference ifr, String value) {
		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String roBranch = "";
			String coBranch = "";
			String staffId = ifr.getValue("StaffNumber").toString();
			String availBranch = ifr.getValue("BRANCHCODEVL").toString();
			int branchNumber = Integer.parseInt(availBranch);
			availBranch = String.format("%05d", branchNumber);

			// String queryForBranch = "SELECT RO_CODE,CO_CODE FROM LOS_M_BRANCH WHERE
			// BRANCHCODE='"+availBranch+"'";

			String queryForBranch = "SELECT RO_CODE,CO_CODE FROM LOS_M_BRANCH WHERE BRANCHCODE='" + availBranch + "'";
			List<List<String>> resultqueryForBranch = ifr.getDataFromDB(queryForBranch);
			Log.consoleLog(ifr, "queryForBranch===>" + queryForBranch);

			if (!resultqueryForBranch.isEmpty()) {
				roBranch = resultqueryForBranch.get(0).get(0);
				Log.consoleLog(ifr, "roBranch===>" + roBranch);
				coBranch = resultqueryForBranch.get(0).get(1);
				Log.consoleLog(ifr, "coBranch===>" + coBranch);
			}

			String vehType = ifr.getValue("Loan_Purpose_VL").toString();
//			String query = " select PURPOSE_LOAN_VL from slos_staff_trn where winame = '" + processInstanceId + "'";
//			List<List<String>> resList = ifr.getDataFromDB(query);
//			Log.consoleLog(ifr, "queryForvehType===>" + query);
//			if (!resList.isEmpty()) {
//				 vehType = resList.get(0).get(0);
//			     Log.consoleLog(ifr, "vehType===>" + vehType);
//			}

			if (vehType.equalsIgnoreCase("four")) {
				Log.consoleLog(ifr, "Inside Four====>");
//				String updateQueryForwfinstrumenttable = "UPDATE wfinstrumenttable SET var_str1='" + staffId
//						+ "', var_str2='" + availBranch + "', var_str3='" + roBranch + "', var_str4='" + coBranch
//						+ "' WHERE processInstanceId='" + processInstanceId + "'";
//				Log.consoleLog(ifr, "updateQueryForwfinstrumenttable==> " + updateQueryForwfinstrumenttable);
//				ifr.saveDataInDB(updateQueryForwfinstrumenttable);
				ifr.setValue("Q_BORROWERNAME", staffId);
				// Log.consoleLog(ifr, "Q_BORROWERNAME===>" + staffId);
				ifr.setValue("Q_PRODUCTTYPE", availBranch);
				// Log.consoleLog(ifr, "Q_BORROWERNAME===>" + staffId);
				ifr.setValue("Q_BRANCHNAME", roBranch);
				ifr.setValue("Q_LEADSOURCE", coBranch);

			}
			if (vehType.equalsIgnoreCase("two")) {
				Log.consoleLog(ifr, "Inside two====>");
//				String updateQueryForwfinstrumenttable = "UPDATE wfinstrumenttable SET var_str1='" + staffId
//						+ "', var_str2='" + availBranch + "', var_str3='" + availBranch + "', var_str4='" + coBranch
//						+ "' WHERE processInstanceId='" + processInstanceId + "'";
//				Log.consoleLog(ifr, "updateQueryForwfinstrumenttable==> " + updateQueryForwfinstrumenttable);
//				ifr.saveDataInDB(updateQueryForwfinstrumenttable);
				ifr.setValue("Q_BORROWERNAME", staffId);
				ifr.setValue("Q_PRODUCTTYPE", availBranch);
				ifr.setValue("Q_BRANCHNAME", availBranch);
				ifr.setValue("Q_LEADSOURCE", coBranch);

			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception occurred in processing: " + e.getMessage());
			return "error, server issue, please try after sometimes";
		}
		return "updated approval";
	}

	public static void ddeCharges(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String ddeCharges = "0";
		String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET DDECHARGES='" + ddeCharges + "'  WHERE WINAME= '"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
		ifr.saveDataInDB(QueryIntrestAmount);

	}

	public String loadGrid(IFormReference ifr) {
		String gender = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "select LOAN_ACC_NUMBER,productname,limit,OUTSTANDING_BALANCE,EMI,roi,tenure from SLOS_ALL_ACTIVE_PRODUCT where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT==>" + query);
		List<List<String>> responseForGrid = ifr.getDataFromDB(query);
		responseForGrid = ifr.getDataFromDB(query);
		String[] Colheading = new String[] { "Loan Account Number", "Product Code - Loan Type",
				"Original Balance/ Limit Amount", "Outstanding balance", "EMI", "Rate of interest",
				"Maturity/Expiry Date" };
		ifr.clearTable("Existing_Loan_Details");
		JSONArray array = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Existing_Loan_Details===>" + obj);
			String accnumber = row.get(0);
			String first4 = accnumber.trim().substring(0, 4);
			String last4 = accnumber.trim().substring(accnumber.trim().length() - 4);
			String middlePart = "X".repeat(accnumber.trim().length() - 8);
			String accId = first4 + middlePart + last4;
			row.set(0, accId);
			obj.put(Colheading[0], row.get(0));
			obj.put(Colheading[1], row.get(1));
			obj.put(Colheading[2], row.get(2));
			obj.put(Colheading[3], row.get(3));
			obj.put(Colheading[4], row.get(4));
			obj.put(Colheading[5], row.get(5));
			obj.put(Colheading[6], row.get(6));
			Log.consoleLog(ifr, "LoanSanctionedAmount_BO " + obj);
			array.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + array);

		ifr.addDataToGrid("Existing_Loan_Details", array);
		String queryForGender = "select customersex from los_trn_customersummary where  WINAME='" + processInstanceId
				+ "'";
		Log.consoleLog(ifr, "queryForGender===>" + queryForGender);
		List<List<String>> list = ifr.getDataFromDB(queryForGender);
		String pensioner = "";
		String pensionIncome = "";
		String totalGrossSalary = "";
		String totalNetSalary = "";
		String queryForPensioner = "SELECT PENSIONER,PENSION_INCOME,TOTAL_GROSS_SALARY,TOTAL_NET_SALARY FROM slos_staff_trn "
				+ "where WINAME ='" + processInstanceId + "'";
		List<List<String>> listqueryForPensioner = ifr.getDataFromDB(queryForPensioner);
		Log.consoleLog(ifr, "queryForPensioner==> " + queryForPensioner);
		if (!listqueryForPensioner.isEmpty()) {
			pensioner = listqueryForPensioner.get(0).get(0);
			if (pensioner.equalsIgnoreCase("YES")) {
				pensionIncome = listqueryForPensioner.get(0).get(1);
				totalGrossSalary = listqueryForPensioner.get(0).get(2);
				totalNetSalary = listqueryForPensioner.get(0).get(3);
				ifr.setStyle("Pensioner", "visible", "true");
				ifr.setStyle("PensionIncomePerMonth", "visible", "true");
				ifr.setStyle("TOT_GROSS_SAL", "visible", "true");
				ifr.setStyle("TOT_NET_SAL", "visible", "true");
				ifr.setValue("Pensioner", "YES");
				ifr.setValue("PensionIncomePerMonth", pensionIncome.toString());
				ifr.setValue("TOT_GROSS_SAL", totalGrossSalary.toString());
				ifr.setValue("TOT_NET_SAL", totalNetSalary.toString());
			} else {
				ifr.setValue("Pensioner", "NO");
				ifr.setStyle("Pensioner", "visible", "false");
				ifr.setStyle("PensionIncomePerMonth", "visible", "false");
				ifr.setStyle("TOT_GROSS_SAL", "visible", "false");
				ifr.setStyle("TOT_NET_SAL", "visible", "false");
			}

		}

		if (!list.isEmpty()) {
			gender = list.get(0).get(0);
		}
		return gender;
	}

	public String documentsCheck(IFormReference ifr, String activityName) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y'";
		List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

		List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME = "'" + String.join("','", responseForGrid2) + "'";

		String getUploadedDocuments = "SELECT NAME,MANDATORY from SLOS_VL_DOCUMENTS where GENERATE = 'U'";
		List<List<String>> listUploadedDocuments = ifr.getDataFromDB(getUploadedDocuments);

		List<String> responseForGrid31 = listUploadedDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME1 = "'" + String.join("','", responseForGrid31) + "'";

		if (activityName.contains("Branch")) {

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");
			String queryforDocGrid1 = "SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_vl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + ")\r\n" + "  )";
			Log.consoleLog(ifr, "queryforDocGrid1==>" + queryforDocGrid1);
			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			Log.consoleLog(ifr, "responseForGrid1==>" + responseForGrid1);
			String[] Columnheading2 = new String[] { "Document Name", "Mandatory" };
			ifr.clearTable("Inward_Doc_Details_Grid");

			String queryforDocGrid2 = "SELECT DISTINCT d.NAME, v1.MANDATORY, d.createddatetime FROM SLOS_TRN_BKOF_DOCUMENTS d INNER JOIN slos_vl_documents v1 ON d.name = v1.name WHERE d.WINAME = '"
					+ processInstanceId
					+ "' AND d.CREATEDDATETIME = (SELECT MAX(CREATEDDATETIME) FROM SLOS_TRN_BKOF_DOCUMENTS d2 WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN ("
					+ NAME1 + "))";
			Log.consoleLog(ifr, "queryforDocGrid2==>" + queryforDocGrid2);
			List<List<String>> responseForGrid4 = ifr.getDataFromDB(queryforDocGrid2);
			responseForGrid4 = ifr.getDataFromDB(queryforDocGrid2);
			Log.consoleLog(ifr, "responseForGrid4==>" + responseForGrid4);
			ifr.clearTable("Inward_Doc_Details_Grid");
			ifr.clearTable("Inward_Doc_DetailsIn_Grid");

			JSONArray arrayRes = new JSONArray();
			JSONArray arrayRes1 = new JSONArray();

			String nf425 = "";

			String gMandatoryDocumentsIn = "SELECT MANDATORY from SLOS_VL_DOCUMENTS where NAME IN ('NF_425')";
			List<List<String>> listManatoryDocumentsIn = ifr.getDataFromDB(gMandatoryDocumentsIn);
			if (!listManatoryDocumentsIn.isEmpty()) {
				nf425 = listManatoryDocumentsIn.get(0).get(0);
			}

			Set<String> uniqueDocNames1 = new HashSet<>();
			// for (List<String> doc : responseForGrid4) {
			if (responseForGrid4 == null || responseForGrid4.isEmpty()) {
				for (List<String> doc : listUploadedDocuments) {
					JSONObject docObj = new JSONObject();
					String docName = doc.get(0);
					String mandatoryFlag = doc.get(1);
					docObj.put("Document Name", docName);
					if ("Y".equalsIgnoreCase(mandatoryFlag)) {
						docObj.put("Mandatory", "Yes");
					} else if ("N".equalsIgnoreCase(mandatoryFlag)) {
						docObj.put("Mandatory", "No");
					}
					arrayRes1.add(docObj);

					Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + docObj.toString());
				}

				// }
			} else {
				for (List<String> doc : responseForGrid4) {
					String docName = doc.get(0);
					String mandatoryFlag = doc.get(1);
					JSONObject docObj = new JSONObject();
					docObj.put("Document Name", docName);
					if ("Y".equalsIgnoreCase(mandatoryFlag)) {
						docObj.put("Mandatory", "Yes");
					} else if ("N".equalsIgnoreCase(mandatoryFlag)) {
						docObj.put("Mandatory", "No");
					}
					String uploadedDate = doc.get(2);

					if (uploadedDate != null && !uploadedDate.trim().isEmpty()) {
						docObj.put("Uploaded Date", uploadedDate);
					} else {
						docObj.put("Uploaded Date", "");
					}

					arrayRes1.add(docObj);
				}
			}

			Set<String> uniqueDocNames = new HashSet<>();
			for (List<String> row : responseForGrid1) {
				String docName = row.get(0);
				String mandatoryFlag = row.get(1);
				String uploadedDate = row.get(2);
				if (uniqueDocNames.add(docName)) {
					JSONObject obj1 = new JSONObject();
					Log.consoleLog(ifr, "Inward_Doc_Details_Grid==>" + obj1);
					obj1.put("Document Name", docName);
					if ("Y".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "Yes");
					} else if ("N".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "No");
					}
					obj1.put("Uploaded Date", uploadedDate);
					arrayRes.add(obj1);
				}
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);
			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes);
			ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);

		}

		if (activityName.equalsIgnoreCase("Staff_VL_Branch_Checker")
				|| activityName.equalsIgnoreCase("Staff_VL_Sanction")
				|| activityName.equalsIgnoreCase("Staff_VL_RO_Maker")
				|| activityName.equalsIgnoreCase("Staff_VL_CO_Maker")
				|| activityName.equalsIgnoreCase("Staff_VL_CO_Sanction")) {

			String queryforDocGrid1 = "  SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_vl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + "," + NAME1
					+ ")\r\n" + "  )";

			Log.consoleLog(ifr, "queryforDocGrid1==>" + queryforDocGrid1);
			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			Log.consoleLog(ifr, "responseForGrid1==>" + responseForGrid1);
			String[] Columnheading2 = new String[] { "Document Name", "Mandatory" };
			ifr.clearTable("Inward_Doc_Details_Grid");
			JSONArray arrayRes1 = new JSONArray();
			List<String> responseForGrid3 = responseForGrid1.stream().map(row -> row.get(0)) // NAME
					.collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid3) + "'";

			/* ---------- Populate Grid from DB (NAME + MANDATORY) ---------- */
			Set<String> uniqueDocNames = new HashSet<>();
			for (List<String> row : responseForGrid1) {

				String docName = row.get(0); // d.NAME
				String mandatoryFlag = row.get(1); // v1.MANDATORY (Y / N)
				String uploadedDate = row.get(2);
				if (uniqueDocNames.add(docName)) {
					JSONObject obj1 = new JSONObject();
					obj1.put("Document Name", docName);
					if ("Y".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "Yes");
					} else if ("N".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "No");
					}
					obj1.put("Uploaded Date", uploadedDate);
					arrayRes1.add(obj1);

					Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + obj1.toString());
				}
			}

			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes1);
			String vehType = "";
			String query = "select PURPOSE_LOAN_VL from slos_staff_trn where winame = '" + processInstanceId + "'";
			List<List<String>> resList = ifr.getDataFromDB(query);
			Log.consoleLog(ifr, "queryForvehType===>" + query);
			if (!resList.isEmpty()) {
				vehType = resList.get(0).get(0);
				Log.consoleLog(ifr, "vehType===>" + vehType);
			}

			if (activityName.equalsIgnoreCase("Staff_VL_Sanction")
					|| activityName.equalsIgnoreCase("Staff_VL_CO_Sanction")
					|| (activityName.equalsIgnoreCase("Staff_VL_Branch_Checker") && vehType.equalsIgnoreCase("two"))) {
				String vT = "";
				String probation = "";
				List<List<String>> listgetAllDocuments = new ArrayList<>();
				String staffTrn = "select vehicle_category,probation from slos_staff_trn where winame='"
						+ processInstanceId + "'";
				List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
				Log.consoleLog(ifr, "staffTrn==" + staffTrn);
				Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
				if (!staffTrnRes.isEmpty()) {

					vT = staffTrnRes.get(0).get(0);
					probation = staffTrnRes.get(0).get(1);
				}
				String Count1 = "";

				if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("Yes")) {
					String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','YES')";
					listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
					Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
				} else if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("No")) {
					String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','NO')";
					listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
					Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
				} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("Yes")) {
					String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','YES')";
					listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
					Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
				} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("No")) {
					String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','NO')";
					listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
					Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
				}

				List<String> responseForGrid21 = listgetAllDocuments.stream().map(doc -> doc.get(0))
						.collect(Collectors.toList());

				NAME = "'" + String.join("','", responseForGrid21) + "'";
				String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
						+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE='SANCTION' \r\n" + ")\r\n" + "WHERE rn = 1";

				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
				Log.consoleLog(ifr, "Count1==>" + Count1);

				getDocCountforSanction(ifr, processInstanceId, NAME);

				if (Integer.parseInt(Count1) > 20 && vT.equalsIgnoreCase("NEW")) {
					return "SUCCESS";

				} else if (Integer.parseInt(Count1) > 19 && vT.equalsIgnoreCase("USED")) {
					return "SUCCESS";

				}

			}
			return "FAILED";

		}
		if (activityName.equalsIgnoreCase("Staff_VL_Post_Sanction")) {
			getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where enable='Y' AND MANDATORY = 'N' AND VEHICLE_TYPE IN ('ALL','USED','NEW')";
			listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

			responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0)).collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid2) + "'";

			String queryforDocGrid1 = "SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_vl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + "," + NAME1
					+ ")\r\n" + "  )";
			Log.consoleLog(ifr, "queryforDocGrid1 ==> " + queryforDocGrid1);

			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);

			Log.consoleLog(ifr, "responseForGrid1 ==> " + responseForGrid1);

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
			ifr.clearTable("Inward_Doc_Details_Grid");
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

			List<String> responseForGrid3 = responseForGrid1.stream().map(row -> row.get(0)) // NAME
					.collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid3) + "'";

			/* ---------- Populate Grid from DB (NAME + MANDATORY) ---------- */
			Set<String> uniqueDocNames = new HashSet<>();
			for (List<String> row : responseForGrid1) {

				String docName = row.get(0); // d.NAME
				String mandatoryFlag = row.get(1); // v1.MANDATORY (Y / N)
				String uploadedDate = row.get(2);
				if (uniqueDocNames.add(docName)) {
					JSONObject obj1 = new JSONObject();
					obj1.put("Document Name", docName);
					if ("Y".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "Yes");
					} else if ("N".equalsIgnoreCase(mandatoryFlag)) {
						obj1.put("Mandatory", "No");
					}
					obj1.put("Uploaded Date", uploadedDate);
					arrayRes.add(obj1);

					Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + obj1.toString());
				}
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);
			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes);
			ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);
			return "SUCCESS";

		}
		return "FAILED";
	}

	private static String getDocCountforSanction(IFormReference ifr, String processInstanceId, String NAME) {
		String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
				+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
				+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
				+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
				+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE='SANCTION'\r\n" + ")\r\n" + "WHERE rn = 1";
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
			obj.put(Columnheading[1], "Generated");
			obj.put(Columnheading[2], row.get(2));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
		return "SUCCESS";
	}

	public void backOffice(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='BackOffice' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
//		String activityId = "";
//		String activityName = "";
//		String queryForActivity = "SELECT ACTIVITYID, ACTIVITYNAME FROM ACTIVITYTABLE WHERE ACTIVITYNAME='"+value+"'";
//		Log.consoleLog(ifr, "queryForActivity query===>" + queryForActivity);
//		List<List<String>> resqueryForActivity = ifr.getDataFromDB(queryForActivity);
//		Log.consoleLog(ifr, "resqueryForActivity===>" + resqueryForActivity);
//		if (!resqueryForActivity.isEmpty()) {
//			activityId = resqueryForActivity.get(0).get(0);
//			activityName = resqueryForActivity.get(0).get(1);
//		}
//		
//		ifr.setValue("ActivityId", activityId.trim());
//		ifr.setValue("ActivityName", activityName.trim());

		String loanAmount = "";
		String queryForLoanType = "SELECT REQ_AMT_VL  FROM SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME='" + processInstanceId
				+ "'";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
		List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty() && res.get(0).get(0) != null && !res.get(0).get(0).isEmpty()) {
			loanAmount = res.get(0).get(0);
			ifr.setValue("Q_DataOnDemand", loanAmount);
		}

	}

	public static String checkDocumentOnLoad(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		
		String vT = "";
		String probation = "";
		List<List<String>> listgetAllDocuments = new ArrayList<>();
		String staffTrn = "select vehicle_category,probation from slos_staff_trn where winame='" + processInstanceId
				+ "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (!staffTrnRes.isEmpty()) {

			vT = staffTrnRes.get(0).get(0);
			probation = staffTrnRes.get(0).get(1);
		}
		
		String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE = 'B'";
		List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

		List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME = "'" + String.join("','", responseForGrid2) + "'";
		
		
		String Count1 = "";
		String vehType = "";
		
		String query = "select PURPOSE_LOAN_VL from slos_staff_trn where winame = '" + processInstanceId + "'";
		List<List<String>> resList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "queryForvehType===>" + query);
		if (!resList.isEmpty()) {
			vehType = resList.get(0).get(0);
			Log.consoleLog(ifr, "vehType===>" + vehType);
		}

		String activityName = ifr.getActivityName();
		if (activityName.contains("Staff_VL_Branch_Maker")) {
			String queryforDocGrid = "SELECT  DISTINCT\r\n" + " d.NAME, d.docstatus, d.createddatetime\r\n"
					+ "		FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "	WHERE d.WINAME = '" + processInstanceId + "'\r\n"
					+ "   AND d.NAME IN ("+NAME+")\r\n"
					+ "		   AND d.CREATEDDATETIME = (\r\n" + " SELECT MAX(CREATEDDATETIME)\r\n"
					+ "		      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "		      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME\r\n" + "		  )";
			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
			List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
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
			return "SUCCESS";
		
		}
		
		
		else if (activityName.contains("Staff_VL_Sanction") || activityName.contains("Staff_VL_CO_Sanction") || activityName.contains("Staff_VL_Post_Sanction") || (vehType.equalsIgnoreCase("two") &&  activityName.contains("Staff_VL_Checker")) ) {

		if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("Yes")) {
			String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','YES')";
			listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
			Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
		} else if (vT.equalsIgnoreCase("NEW") && probation.equalsIgnoreCase("No")) {
			String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','NEW') AND PROBATION in ('ALL','NO')";
			listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
			Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
		} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("Yes")) {
			String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','YES')";
			listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
			Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
		} else if (vT.equalsIgnoreCase("USED") && probation.equalsIgnoreCase("No")) {
			String getAllNewDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE='Y' AND VEHICLE_TYPE in ('ALL','USED') AND PROBATION in ('ALL','NO')";
			listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
			Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
		}

		List<String> responseForGrid1 = listgetAllDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		NAME = "'" + String.join("','", responseForGrid1) + "'";
		if (value.equalsIgnoreCase("NO") || value.equalsIgnoreCase("S") || value.equalsIgnoreCase("P") || activityName.contains("Staff_VL_CO_Sanction")|| activityName.contains("Staff_VL_Sanction")) {
			String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE='SANCTION' \r\n" + ")\r\n" + "WHERE rn = 1";

			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
			List Result1 = ifr.getDataFromDB(queryForCount);
			Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);
		}
		}
		else {
			String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND NAME IN (" + NAME + " )  AND DOCTYPE IS NULL \r\n" + ")\r\n" + "WHERE rn = 1";

			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
			List Result1 = ifr.getDataFromDB(queryForCount);
			Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);
		}
		// total 21 documents for New vehicle
		if (Integer.parseInt(Count1) > 0 && vT.equalsIgnoreCase("NEW")) {
			return getDocCount(ifr, processInstanceId, NAME, value);
			// total 20 documents for Used vehicle
		} else if (Integer.parseInt(Count1) > 0 && vT.equalsIgnoreCase("USED")) {
			return getDocCount(ifr, processInstanceId, NAME, value);

		}

		return "FAILED";

	}

	private static String getDocCount(IFormReference ifr, String processInstanceId, String NAME, String value) {
		if (value.equalsIgnoreCase("NO") || value.equalsIgnoreCase("S") || value.equalsIgnoreCase("P")) {
			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND NAME IN (" + NAME + " ) AND DOCTYPE='SANCTION'\r\n" + ")\r\n" + "WHERE rn = 1";
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
				obj.put(Columnheading[1], "Generated");
				obj.put(Columnheading[2], row.get(2));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);

			ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);
		} else {
			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
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
				obj.put(Columnheading[0], row.get(0));
				obj.put(Columnheading[1], "Generated");
				obj.put(Columnheading[2], row.get(2));
				arrayRes.add(obj);
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);

			ifr.addDataToGrid("Outward_Doc_Details_Grid", arrayRes);

		}

		return "SUCCESS";
	}

	public static String checkNeslOnLoad(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
				+ processInstanceId + "'";
		List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
		Log.consoleLog(ifr, "query for esign===>" + queryESign);
		if (!resultEsign.isEmpty()) {
			if (resultEsign.get(0).get(0).equalsIgnoreCase("Y")
					&& resultEsign.get(0).get(1).equalsIgnoreCase("Success")) {
				return "NESLFOUND";
			}

		}
		return "FAILED";
	}

	public String onLoadCRNumber(IFormReference ifr) {
		String appNumber = "";
		String appStatus = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryForRefrenceNumber = "select APPLICATION_NO,APPLICATION_STATUS from LOS_WIREFERENCE_TABLE "
				+ "where WINAME ='" + processInstanceId + "'";
		List<List<String>> listqueryForRefrenceNumber = ifr.getDataFromDB(queryForRefrenceNumber);
		Log.consoleLog(ifr, "queryForRefrenceNumber==> " + queryForRefrenceNumber);
		if (!listqueryForRefrenceNumber.isEmpty()) {
			appNumber = listqueryForRefrenceNumber.get(0).get(0);
			appStatus = listqueryForRefrenceNumber.get(0).get(1);
			if (appStatus.toLowerCase().contains("expire")) {
				return "error, Application is expired Kindly initiate fresh application";
			}
			return appNumber;

		}
		return "";
	}

	public static String onLoadGetBranchCodeandNameVL(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String branch = "";
		String branchName = "";
		String Query = "select branch_code from slos_staff_trn where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "branch code query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			branch = res.get(0).get(0);
		}
		String QueryB = "select branchname from los_m_branch where BRANCHCODE=LPAD('" + branch + "', 5, '0')";
		Log.consoleLog(ifr, "Branch Name query===>" + QueryB);
		List<List<String>> result = ifr.getDataFromDB(QueryB);
		Log.consoleLog(ifr, "result===>" + result);
		if (!result.isEmpty()) {
			branchName = result.get(0).get(0);
		}
		return branchName + "/" + branch;
	}

	public void onLoadFinalScreenVL(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Summary' WHERE WINAME='" + processInstanceId
				+ "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

	}

//		public static void uploadInwardDocument(IFormReference ifr) {
//			// TODO Auto-generated method stub
//			
//		}

	public static void saveESignStatus(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String updateQuery = "UPDATE SLOS_TRN_LOANSUMMARY SET ESIGNSTATUS='" + value + "' where winame='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(updateQuery);

	}

	public void setUserIDVL(IFormReference ifr, String value) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String activityName = ifr.getActivityName();
		String userName = "";
		String vehType = "";

		String query = "select PURPOSE_LOAN_VL from slos_staff_trn where winame = '" + PID + "'";
		List<List<String>> resList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "queryForvehType===>" + query);
		if (!resList.isEmpty()) {
			vehType = resList.get(0).get(0);
			Log.consoleLog(ifr, "vehType===>" + vehType);
		}

		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equalsIgnoreCase("Staff_VL_Branch_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_MAKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_VL_Branch_Checker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

			Log.consoleLog(ifr, "Inside Staff Sanction====>");
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String formattedDate = dateFormat.format(currentDate);

			String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
					+ "' WHERE winame = '" + PID + "'";

			ifr.saveDataInDB(updateQuerySD);

		}
		if (activityName.equalsIgnoreCase("Staff_VL_RO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_VL_CO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_VL_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "";
			if (vehType.equalsIgnoreCase("two")) {
				updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID ='" + userName + "' where winame='" + PID
						+ "'";
				ifr.saveDataInDB(updateQuery);
			} else {

				updateQuery = "UPDATE slos_Staff_trn SET RO_SANCTION_ID ='" + userName + "' where winame='" + PID + "'";
				ifr.saveDataInDB(updateQuery);
			}

		}
		if (activityName.equalsIgnoreCase("Staff_VL_CO_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "";
			if (vehType.equalsIgnoreCase("two")) {
				updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID ='" + userName + "' where winame='" + PID
						+ "'";
				ifr.saveDataInDB(updateQuery);
			} else {

				updateQuery = "UPDATE slos_Staff_trn SET CO_SANCTION_ID ='" + userName + "' where winame='" + PID + "'";
				ifr.saveDataInDB(updateQuery);
			}

		}

	}

	public String checkLoanAccNumVL(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String Query = "select count(*) from slos_trn_loandetails where LOAN_ACCOUNTNO IS NOT NULL and pid ='" + PID
				+ "'";
		Log.consoleLog(ifr, "Count query===>" + Query);
		List Result1 = ifr.getDataFromDB(Query);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		if (Integer.parseInt(Count1) > 0) {
			String productCOde = "";
			String loanAccountNum = "";
			String accountCreatedDate = "";
			String queryForProductCode = "SELECT prod_scheme_desc from slos_staff_trn where winame='" + PID + "'";
			List<List<String>> queForProCodeRes = ifr.getDataFromDB(queryForProductCode);
			Log.consoleLog(ifr, "queForProCodeRes : " + queForProCodeRes);
			if (!queForProCodeRes.isEmpty()) {
				productCOde = queForProCodeRes.get(0).get(0);
			}
			String queryForLoanAccountNumber = "SELECT LOAN_ACCOUNTNO,ACCOUNT_CREATEDDATE from SLOS_TRN_LOANDETAILS where PID='"
					+ PID + "'";
			List<List<String>> queryForLoanAccountNumberRes = ifr.getDataFromDB(queryForLoanAccountNumber);
			Log.consoleLog(ifr, "queryForLoanAccountNumberRes : " + queryForLoanAccountNumberRes);
			if (!queryForLoanAccountNumberRes.isEmpty()) {
				loanAccountNum = queryForLoanAccountNumberRes.get(0).get(0);
				accountCreatedDate = queryForLoanAccountNumberRes.get(0).get(1);
			}
			// String formaDate = getCurrentAPIDate(ifr);
			ifr.setStyle("Acc_Open_Date_VL", "visible", "true");
			ifr.setStyle("Product_VL_LAC", "visible", "true");
			ifr.setStyle("LAC_LoanAcc_Num_VL", "visible", "true");
			ifr.setValue("Product_VL_LAC", productCOde);
			ifr.setValue("LAC_LoanAcc_Num_VL", loanAccountNum);
			ifr.setValue("Acc_Open_Date_VL", accountCreatedDate);
			return "SUCCESS";

		} else {
			return "FAILED";
		}

	}

	public String uploadMandatoryDocsVL(IFormReference ifr, String value) {
		String documentNames = "";
		if (value.equalsIgnoreCase("Staff_HL_Branch_Maker")) {
			String getMandatoryDocuments = "SELECT NAME from SLOS_VL_DOCUMENTS where GENERATE = 'U'";
			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			List<String> names = listMandatoryDocuments.stream().flatMap(tempMap -> tempMap.stream())
					.collect(Collectors.toList());
			documentNames = names.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));
		}
		return documentNames;
	}

	public void summaryB(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Summary' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
		
	}

}
