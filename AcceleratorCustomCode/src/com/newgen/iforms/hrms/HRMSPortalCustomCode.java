package com.newgen.iforms.hrms;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.dlp.integration.staff.common.APIHrmsPreprocessor;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
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
public class HRMSPortalCustomCode {
	Advanced360EnquiryHRMSData advanced360EnquiryHRMSData = new Advanced360EnquiryHRMSData();
	HRMS hrms = new HRMS();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();
	OmnidocDownload ominDocDownload = new OmnidocDownload();
	String productCode = "";
	EsignCommonMethods eSign = new EsignCommonMethods();
	APICommonMethods cm = new APICommonMethods();
	

	public String getHRMSData(IFormReference ifr, String empid) throws ParseException {
		Log.consoleLog(ifr, "#Inside getHRMSData...");
		Log.consoleLog(ifr, "#Inside empid..." + empid);
		String dpnLimit = "0.0";
		String odLimit = "0.0";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "#Inside processInstanceId..." + processInstanceId);
		CustomerAccountSummaryPAPL CBS1 = new CustomerAccountSummaryPAPL();
		String status = CBS1.executeCustomerAccountSummary(ifr, processInstanceId);
		if (status.contains(RLOS_Constants.ERROR)) {
			Log.consoleLog(ifr, "inside error condition hrmsDetails");
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		Log.consoleLog(ifr, "#status..." + status);
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
		
		String Query3 = "INSERT INTO SLOS_TRN_LOANDETAILS (PID) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_TRN_LOANDETAILS WHERE PID = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query3);

		cf.mExecuteQuery(ifr, Query3, "insert into SLOS_STAFF_VL_ELIGIBILITY");
		
		String custId = "";
		String query = "SELECT CUSTOMERID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "#Inside query..." + query);

		List<List<String>> list = ifr.getDataFromDB(query);
		if (!list.isEmpty()) {
			custId = list.get(0).get(0);
			ifr.setValue("Q_CUSTOMERID", custId);
		} else {
			return "error" + "," + "customer id not found";
		}

		String hrmsDetails = hrms.getHRMSDetails(ifr, empid);
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
		if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "technical glitch";
		}
		Log.consoleLog(ifr, "inside non-error condition executeCBSAdvanced360Inquiryv2");
		JSONParser jsonparser = new JSONParser();
		JSONObject obj1 = (JSONObject) jsonparser.parse(hrmsDetails);
		Log.consoleLog(ifr, obj1.toString());
		String name = obj1.get("NAME").toString();
		Log.consoleLog(ifr, "name : " + name);
		String designation = obj1.get("DESIGNATION").toString();
		Log.consoleLog(ifr, "designation : " + designation);
		String branchDPCode = obj1.get("BRANCHDPCODE").toString();
		Log.consoleLog(ifr, "branchDPCode : " + branchDPCode);
		String gross = obj1.get("GROSS").toString();
		Log.consoleLog(ifr, "gross : " + gross);
		String nth = obj1.get("NTH").toString();
		Log.consoleLog(ifr, "nth : " + nth);
		String probation = obj1.get("PROBATION").toString();
		Log.consoleLog(ifr, "probation : " + probation);
		String totalDed = obj1.get("TOTALDED").toString();
		Log.consoleLog(ifr, "totalDed : " + totalDed);
		String salaryacc = obj1.get("SALARYACCOUNT").toString();
		Log.consoleLog(ifr, "salaryacc : " + salaryacc);
		String executeCBSAdvanced360Inquiryv2 = advanced360EnquiryHRMSData.executeCBSAdvanced360Inquiryv2(ifr,
				processInstanceId, custId, salaryacc, designation, probation);

		Log.consoleLog(ifr, "executeCBSAdvanced360Inquiryv2==>" + executeCBSAdvanced360Inquiryv2);

		String branchCode = "";

		String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='" + processInstanceId
				+ "' ";

		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		if (!branchCodeResult.isEmpty()) {
			branchCode = branchCodeResult.get(0).get(0);
		}
		if (branchCode.equalsIgnoreCase("")) {
			Log.consoleLog(ifr, "Branch Code founds to be Empty for the WorkItem");
			return "error" + "," + "Branch Code founds to be Empty for the WorkItem";
		}
		String[] executeCBSAdvanced360InquiryArray = executeCBSAdvanced360Inquiryv2.split(":");
		if (executeCBSAdvanced360Inquiryv2.contains(RLOS_Constants.ERROR)) {
			Log.consoleLog(ifr, "inside error condition executeCBSAdvanced360Inquiryv2");
			return "error" + "," + executeCBSAdvanced360InquiryArray[1];
		}
		JSONObject obj = (JSONObject) jsonparser.parse(executeCBSAdvanced360Inquiryv2);
		Log.consoleLog(ifr, obj.toString());
		String isDisableYes = obj.get("IsRenewableButton").toString();
		if (isDisableYes.equalsIgnoreCase("disableYes")) {
			String QueryDisableYes = "UPDATE SLOS_TRN_LOANSUMMARY SET RENEWENHANCEFLAG= 'yes' WHERE WINAME= '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "QueryDisableYes====>" + QueryDisableYes);
			ifr.saveDataInDB(QueryDisableYes);
		}
		String originalBalance = obj.get("OriginalBalance").toString();
		Log.consoleLog(ifr, "OriginalBalance : " + originalBalance);

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

		// String amtOdLimit = obj.get("AmtOdLimit").toString();
		String amtOdLimit = String.valueOf(amtOdLimitD);
		Log.consoleLog(ifr, "amtOdLimit : " + amtOdLimit);
		ifr.setValue("StaffName", name);
		ifr.setValue("StaffDesignation", designation);
		ifr.setValue("Salary_Credit_Branch", branchDPCode);
		ifr.setValue("Gross_Salary", gross);

		ifr.setValue("Salary_Account_Number", salaryacc);
		String tag = (probation.equalsIgnoreCase("no") || probation.equalsIgnoreCase("n")) ? "N" : "Y";

		String queryForDPN = "SELECT DPN_LIMIT, OD_LIMIT,PRODUCTCODE FROM LOS_STAFF_SCHEME_LIMIT WHERE DESIGNATION='"
				+ designation.trim() + "' and probation_tag='" + tag + "'";
		Log.consoleLog(ifr, "#Inside query..." + queryForDPN);
		String productCode = "";
		List<List<String>> listForDPN = ifr.getDataFromDB(queryForDPN);
		if (!listForDPN.isEmpty()) {
			if (listForDPN.get(0).get(0) != null || !listForDPN.get(0).get(0).equalsIgnoreCase("0.0")) {
				dpnLimit = listForDPN.get(0).get(0);
			}
			Log.consoleLog(ifr, "dpnLimit : " + dpnLimit);
			if (listForDPN.get(0).get(1) != null || !listForDPN.get(0).get(1).equals("0.0"))
				odLimit = listForDPN.get(0).get(1);
			Log.consoleLog(ifr, "odLimit : " + odLimit);
			productCode = listForDPN.get(0).get(2);
		} else {
			return "error,designation not found please reach out to admin";
		}
		List<String> odValidProductCode = (List<String>) obj.get("odProductCode");
		Log.consoleLog(ifr, "odValidProductCode : " + odValidProductCode);
		String queryForvalidProductCode = "SELECT distinct(PRODUCTCODE) FROM LOS_STAFF_SCHEME_LIMIT WHERE PRODUCTCODE <> '"
				+ productCode + "'";
		List<List<String>> resForODPrd = ifr.getDataFromDB(queryForvalidProductCode);
		Log.consoleLog(ifr, "queryForvalidProductCode==>" + queryForvalidProductCode);
		Log.consoleLog(ifr, "queryForvalidProductCode res==>" + resForODPrd);

		boolean isValid = odValidProductCode.stream()
				.anyMatch(b -> resForODPrd.stream().flatMap(a -> a.stream()).anyMatch(x -> x.equals(b)));
		Log.consoleLog(ifr, "queryForvalidProductCode isValid==>" + isValid);

		ifr.setValue("Eligible_DPN_Amount", dpnLimit);

		ifr.setValue("Eligible_OD_Amount", odLimit);

		ifr.setValue("Total_DPN_Utilized", originalBalance);

		double dpnAvail = 0.0;
		double odAvail = 0.0;
		if (Double.parseDouble(originalBalance) + Double.parseDouble(amtOdLimit) == 0.0) {
			ifr.setValue("Total_DPN_Utilized", "0.0");
			ifr.setValue("Total_OD_Utilized", "0.0");
		}

		dpnAvail = Double.parseDouble(dpnLimit)
				- (Double.parseDouble(originalBalance) + Double.parseDouble(amtOdLimit));
		ifr.setValue("Total_DPN_Availed", String.valueOf(dpnAvail));

		Log.consoleLog(ifr, "latestProduction jar");
		Log.consoleLog(ifr, "dpnAvail > 0==>" + dpnAvail);

		if (Double.parseDouble(amtOdLimit) == 0.0) {

			odAvail = Math.min(Double.parseDouble(dpnLimit) - Double.parseDouble(originalBalance),
					Double.parseDouble(odLimit));
			ifr.setValue("Total_OD_Availed", String.valueOf(odAvail));
			Log.consoleLog(ifr, "odAvail==0==>" + odAvail);
		} else if (Double.parseDouble(amtOdLimit) > 0.0) {
			if (Double.parseDouble(originalBalance) == 0.0) {
				odAvail = Double.parseDouble(odLimit) - Double.parseDouble(amtOdLimit);

			} else {

				odAvail = Math.min(Double.parseDouble(dpnLimit) - Double.parseDouble(originalBalance),
						Double.parseDouble(odLimit) - Double.parseDouble(amtOdLimit));

//				odAvail = Math.min(Double.parseDouble(originalBalance),
//						Double.parseDouble(odLimit) - Double.parseDouble(amtOdLimit));
			}
			// ifr.setValue("Total_OD_Availed", String.valueOf(odAvail));
			if (Double.parseDouble(originalBalance) + Double.parseDouble(amtOdLimit) >= Double.parseDouble(dpnLimit)) {
				ifr.setValue("Total_OD_Availed", "0.0");
				odAvail = 0.0;
			} else {
				ifr.setValue("Total_OD_Availed", String.valueOf(odAvail));
			}
			Log.consoleLog(ifr, "odAvail>0==>" + odAvail);

			Log.consoleLog(ifr, "odAvail>0==>" + odAvail);
		}

		ifr.setValue("Total_OD_Utilized", amtOdLimit);

		if (odAvail <= 0.0) {
			ifr.setStyle("LoanTypeSelected_1", "disable", "true");
		}

		String productName = obj.get("ProductName").toString();
		Log.consoleLog(ifr, "ProductName : " + productName);
		String amtInstal = obj.get("AmtInstal").toString();
		String amtOdInt = obj.get("AmtOdInt").toString();

		Log.consoleLog(ifr, "amtInstal : " + amtInstal);
		String productNameOD = obj.get("ProductNameOD").toString();
		Log.consoleLog(ifr, "productNameOD : " + productNameOD);

		double notionalEMI = (Double.parseDouble(amtOdLimit) * AcceleratorConstants.ODRATE)
				/ AcceleratorConstants.TWELVE;

		double netSalaryAfterDed = Double.parseDouble(gross)
				- (Double.parseDouble(totalDed) + Double.parseDouble(amtInstal) + notionalEMI);
		String netSalaryAfterD = String.format("%.2f", netSalaryAfterDed);
		ifr.setValue("StaffNTH", String.valueOf(netSalaryAfterD));
		ifr.setValue("LoanDeductionDPN", amtInstal);
		ifr.setValue("LoanDeductionOD", amtOdInt);

		ifr.setValue("Salary_Credit_Branch", branchCode);

		ifr.setStyle("first_staffdetails_checkbox1", "mandatory", "true");
//		if (dpnAvail <= 0.0) {
//			ifr.setStyle("LoanTypeSelected_0", "disable", "true");
//			JSONObject message = new JSONObject();
//			message.put("showMessage", cf.showMessage(ifr, "", "error", "Error utilized all dpn amount"));
//			return "error,Error utilized all dpn amount";
//		}
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Staff Details' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		String queryforProductCode = "select PRODUCTCODE from SLOS_ALL_ACTIVE_PRODUCT  where WINAME='"
				+ processInstanceId + "' ";

		Log.consoleLog(ifr, "queryforProductCode==>" + queryforProductCode);
		List<List<String>> productCodeResult = ifr.getDataFromDB(queryforProductCode);
		boolean isPresent = productCodeResult.stream().anyMatch(innerList -> innerList.contains("1127"));
		Log.consoleLog(ifr, "isPresent : " + isPresent);
		ifr.setValue("Q_FinalAuthorityDesignation", name);
		String appNumber = "";
		String queryForRefrenceNumber = "select APPLICATION_NO from LOS_WIREFERENCE_TABLE " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> listqueryForRefrenceNumber = ifr.getDataFromDB(queryForRefrenceNumber);
		Log.consoleLog(ifr, "queryForRefrenceNumber==> " + queryForRefrenceNumber);
		if (!listqueryForRefrenceNumber.isEmpty()) {
			appNumber = listqueryForRefrenceNumber.get(0).get(0);
			ifr.setValue("Q_ApproveRecommendAuthority", appNumber);

		}

		if ((isValid && (Double.parseDouble(amtOdLimit) > 0.0)) && dpnAvail >= 0 || isPresent) {
			return "dpnUtil :" + dpnAvail + "error," + AccelatorStaffConstant.PRODUCTCODEPROMOTE;
		}
		return amtOdLimit + "," + isDisableYes;
	}

	public String nonNeslStateValidation(IFormReference ifr, String value) {
		String branchCode = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='" + processInstanceId
				+ "' ";

		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		if (!branchCodeResult.isEmpty()) {
			branchCode = branchCodeResult.get(0).get(0);
		}
		String city = "";
		String state = "";
		String zipCode = "";
		String stateCode = "";

		if (branchCode.length() == 4) {
			branchCode = "0" + branchCode;
		}

		String custParamsDataQry = ConfProperty.getCommonPropertyValue("PAPLBranchCodeQuery");
		custParamsDataQry = custParamsDataQry.replaceAll("#BRANCHCODE#", branchCode);
		Log.consoleLog(ifr, "custParamsDataQry==>" + custParamsDataQry);
		List<List<String>> Result = ifr.getDataFromDB(custParamsDataQry);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		if (!Result.isEmpty()) {
			city = Result.get(0).get(0);
			state = Result.get(0).get(1);
			zipCode = Result.get(0).get(2);
		}
		Log.consoleLog(ifr, "City====>" + city);
		Log.consoleLog(ifr, "State===>" + state);
		Log.consoleLog(ifr, "Zip=====>" + zipCode);
		if (state.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "state is empty";
		}
		String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state
				+ "')) AND ROWNUM=1";

		Log.consoleLog(ifr, "Query3==>" + Query3);
		List<List<String>> Result3 = ifr.getDataFromDB(Query3);
		Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
		if (!Result3.isEmpty()) {
			stateCode = Result3.get(0).get(0);
		}
//		String Query4 = "select STATE_CODE from SLOS_MST_STATEFEECHARGES where STATE_CODE='" + stateCode + "'";
		if (value.contains("VL")) {
			String Query4 = "select ISACTIVE from SLOS_MST_STATEFEECHARGES where STATE_CODE='" + stateCode
					+ "' AND SCHEMEID='SVL1'";
			Log.consoleLog(ifr, "Query4==>" + Query4);
			List<List<String>> Result4 = ifr.getDataFromDB(Query4);
			Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
			if (!Result4.isEmpty() && Result4.get(0).get(0).equalsIgnoreCase("N")) {
				getCurrentStage(ifr, processInstanceId);
				return "error" + "," + AccelatorStaffConstant.NESL_STATE_VALIDATION;
			}
		} else if (value.contains("HL")) {
			String Query4 = "select ISACTIVE from SLOS_MST_STATEFEECHARGES where STATE_CODE='" + stateCode
					+ "' AND SCHEMEID='SHL1'";
			Log.consoleLog(ifr, "Query4==>" + Query4);
			List<List<String>> Result4 = ifr.getDataFromDB(Query4);
			Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
			if (!Result4.isEmpty() && Result4.get(0).get(0).equalsIgnoreCase("N")) {
				getCurrentStage(ifr, processInstanceId);
				return "error" + "," + AccelatorStaffConstant.NESL_STATE_VALIDATION;
			}
		} else {
			String Query4 = "select ISACTIVE from SLOS_MST_STATEFEECHARGES where STATE_CODE='" + stateCode
					+ "' AND SCHEMEID='SL1'";
			Log.consoleLog(ifr, "Query4==>" + Query4);
			List<List<String>> Result4 = ifr.getDataFromDB(Query4);
			Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
			if (!Result4.isEmpty() && Result4.get(0).get(0).equalsIgnoreCase("N")) {
				getCurrentStage(ifr, processInstanceId);
				return "error" + "," + AccelatorStaffConstant.NESL_STATE_VALIDATION;
			}
		}
		return branchCode;
	}

	private void getCurrentStage(IFormReference ifr, String processInstanceId) {
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='BackOffice' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
	}

	private double calculateMinFinal(double eligibleAmount, double amtRequested) {
		return (Math.floor(Math.min(eligibleAmount, amtRequested) / 1000) * 1000);
	}

	private double calculateMin(double eligiblityamtPerSal, double eligibleAsPerNth, double eligiblityasPerPolicy,
			String loanType, String dpnUtilized, String odUtlizied, IFormReference ifr, String odAvailable,
			String amtRequest) {
		double odAvailableInDouble = Double.parseDouble(odAvailable);
		double odUtliziedInDouble = Double.parseDouble(odUtlizied);

		boolean dpnUtilizedisEmpty = dpnUtilized == null || dpnUtilized.isEmpty();
		boolean odUtilizedisEmpty = odUtlizied == null || odUtlizied.isEmpty();
		if (dpnUtilizedisEmpty) {
			dpnUtilized = "0.0";
		}
		if (odUtilizedisEmpty) {
			odUtlizied = "0.0";
		}
		double odLimit = Double.parseDouble(odUtlizied) + odAvailableInDouble;

		double minValue = Math.min(Math.min(eligiblityamtPerSal, eligibleAsPerNth), eligiblityasPerPolicy);

		Log.consoleLog(ifr, "Eligilbe ==> " + minValue + " dpnUtilized " + dpnUtilized + " final ==" + (minValue -

				Double.parseDouble(dpnUtilized)) + " "
				+ (Math.floor((minValue - Double.parseDouble(dpnUtilized)) / 1000) * 1000));
//		if (loanType.equalsIgnoreCase("OD Renewal")) {
//			return (minValue < 0) ? 0 : (Math.floor(Math.min(minValue, odUtliziedInDouble) / 1000) * 1000);
		if (loanType.equalsIgnoreCase("OD Renewal")) {
//			return (minValue < 0) ? 0 : Double.parseDouble(odUtlizied);
			return Double.parseDouble(odUtlizied);

		} else if (loanType.equalsIgnoreCase("OD Enhancement/Reduction")) {
			return (minValue < 0) ? 0 : (Math.floor(Math.min(minValue, odLimit) / 1000) * 1000);
		}
		return (minValue < 0) ? 0
				: (loanType.equals("OD") ? (Math.floor(Math.min(minValue, odAvailableInDouble) / 1000) * 1000)
						: (Math.floor(minValue / 1000) * 1000));
//		return (minValue < 0) ? 0
//				: (loanType.equals("OD") ? Double.parseDouble(amtRequest)
//						: (Math.floor(minValue / 1000) * 1000));
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
			if (loanType.equalsIgnoreCase("DPN")) {
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
			} else if (loanType.equalsIgnoreCase("OD")) {
				String slidderValues = pcm.getSliderAmount(ifr);
				Log.consoleLog(ifr, "slidderValues : " + slidderValues);
				if (slidderValues.equalsIgnoreCase("")) {
					return this.pcm.returnError(ifr);
				}
				String[] slidderValuesarr = slidderValues.split("~");
				amtRequest = slidderValuesarr[0];
				Log.consoleLog(ifr, "amtRequest : " + amtRequest);
				tenure = "24";
				Log.consoleLog(ifr, "tenure : " + tenure);
			} else if (loanType.contains("Renewal")) {
				String slidderValues = pcm.getSliderAmount(ifr);
				Log.consoleLog(ifr, "slidderValues : " + slidderValues);
				if (slidderValues.equalsIgnoreCase("")) {
					return this.pcm.returnError(ifr);
				}
				String[] slidderValuesarr = slidderValues.split("~");
				amtRequest = slidderValuesarr[0];
				Log.consoleLog(ifr, "amtRequest : " + amtRequest);
				tenure = "24";
				Log.consoleLog(ifr, "tenure : " + tenure);
			} else if (loanType.contains("Enhancement")) {
				String slidderValues = pcm.getSliderAmount(ifr);
				Log.consoleLog(ifr, "slidderValues : " + slidderValues);
				if (slidderValues.equalsIgnoreCase("")) {
					return this.pcm.returnError(ifr);
				}
				String[] slidderValuesarr = slidderValues.split("~");
				amtRequest = slidderValuesarr[0];
				Log.consoleLog(ifr, "amtRequest : " + amtRequest);
				tenure = "24";
				Log.consoleLog(ifr, "tenure : " + tenure);
			}

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
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Staff Details' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
		return "success next button enabled";
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
			}
		}
		if (stage.equalsIgnoreCase("availoffer")) {
			String Query1 = "UPDATE SLOS_STAFF_TRN SET COMPLETED_YEARS= '" + years + "',REMAINING_SERVICE='"
					+ remainingService + "' WHERE WINAME= '" + processInstanceId + "'";

			ifr.saveDataInDB(Query1);
			Log.consoleLog(ifr, "update query HRMSPORTAL CODE " + Query1);

			query = "SELECT GROSS_SALARY,COMPLETED_YEARS, TOTAL_DED, LOAN_DEDUCTION, DPNUTILIZED,OD_UTILIZED, NTH, LOAN_TYPE,DATE_OF_JOINING,DATE_OF_RETIREMENT,STAFF_NUMBER,DESIGNATION, SALARY_CREDIT_BRAND_DP_CODE,PROBATION,TOTAL_DED, CURRENT_BRANCH,SB_ACCOUNT_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='"
					+ processInstanceId + "'";
		}
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

		if (stage.equalsIgnoreCase("finalEligiblity") || stage.equalsIgnoreCase("disbursement")) {
			query = "SELECT GROSS_SALARY,COMPLETED_YEARS, TOTAL_DED, LOAN_DEDUCTION, DPNUTILIZED,OD_UTILIZED, NTH, LOAN_TYPE,DATE_OF_JOINING,DATE_OF_RETIREMENT,STAFF_NUMBER,DESIGNATION, SALARY_CREDIT_BRAND_DP_CODE,PROBATION,TOTAL_DED, CURRENT_BRANCH,SB_ACCOUNT_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='"
					+ processInstanceId + "'";
			String executeCBSAdvanced360Inquiryv2 = advanced360EnquiryHRMSData.executeCBSAdvanced360Inquiryv2(ifr,
					processInstanceId, custId, salaryAccount, designation, probation);
			if (executeCBSAdvanced360Inquiryv2.contains(RLOS_Constants.ERROR)) {
				Log.consoleLog(ifr, "inside error condition executeCBSAdvanced360Inquiryv2");
				return "error" + "," + "Technical glitch, Try after sometime!";
			}
			Log.consoleLog(ifr, "inside non-error condition executeCBSAdvanced360Inquiryv2");
			JSONParser jsonparser = new JSONParser();
			JSONObject obj = (JSONObject) jsonparser.parse(executeCBSAdvanced360Inquiryv2);
			Log.consoleLog(ifr, obj.toString());
			originalBalance = obj.get("OriginalBalance").toString();
			Log.consoleLog(ifr, "OriginalBalance : " + originalBalance);

			String queryForLatestAmtODLimit = "select LIMIT from SLOS_ALL_ACTIVE_PRODUCT where WINAME='"
					+ processInstanceId
					+ "' AND PRODUCTCODE IN ('253','254','1129') order by TO_DATE(TENURE,'DD/MM/YYYY') DESC";
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

			// String amtOdLimit = obj.get("AmtOdLimit").toString();
			amtOdLimit = String.valueOf(amtOdLimitD);
			amtInstal = obj.get("AmtInstal").toString();

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
		if (stage.equalsIgnoreCase("availoffer")) {
			String QueryCity = "UPDATE SLOS_TRN_LOANSUMMARY SET CITY= '" + currCity + "' WHERE WINAME= '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "QueryCity====>" + QueryCity);
			ifr.saveDataInDB(QueryCity);

			String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age=(SELECT TRUNC(MONTHS_BETWEEN(SYSDATE, TO_DATE(DATEOFBIRTH, 'DD-MM-YYYY')) / 12) AS age FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY where WINAME='"
					+ processInstanceId + "') where WINAME='" + processInstanceId + "'";

			Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
			ifr.saveDataInDB(ageQuery);
		}
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
			if (dpnAvailOptional.isPresent() && !dpnAvailOptional.get().isEmpty()
					&& stage.equalsIgnoreCase("availoffer")) {
				dpnAvail = Double.parseDouble(dpnAvailOptional.get());
				Log.consoleLog(ifr, "dpnAvail : " + dpnAvail);
			} else {
				dpnAvail = Double.parseDouble(originalBalance);
				Log.consoleLog(ifr, "dpnAvail : " + dpnAvail);
			}
			if (odAvailOptional.isPresent() && !odAvailOptional.get().isEmpty()
					&& stage.equalsIgnoreCase("availoffer")) {
				odAvail = Double.parseDouble(odAvailOptional.get());
				Log.consoleLog(ifr, "odAvail : " + odAvail);
			} else {
				odAvail = Double.parseDouble(amtOdLimit);
				Log.consoleLog(ifr, "odAvail : " + odAvail);
			}
			if (loanDedOptional.isPresent() && !loanDedOptional.get().isEmpty()
					&& stage.equalsIgnoreCase("availoffer")) {
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
				// DecimalFormat decimalFormat = new DecimalFormat("0.00");
				if (!amtInstal.isEmpty()) {
					Log.consoleLog(ifr, "amtInstal : " + amtInstal);
//					double amtInstalD=Double.parseDouble(amtInstal);
//				
//				String roundedResultLoanded = decimalFormat.format(amtInstal);
//				String queryForLoanded = "UPDATE SLOS_STAFF_TRN SET LOAN_DEDUCTION= '" + roundedResultLoanded
//						+ "' where WINAME='" + processInstanceId + "'";
//
//				ifr.saveDataInDB(queryForLoanded);
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

//		if (grossSal < deductions) {
//
//			return "error" + "," + "Sorry, you are not eligible for the requested amount";
//		}
		double netSalAfterDed = grossSal - deductions;
		Log.consoleLog(ifr, "netSalAfterDed : " + netSalAfterDed);
		double roi = 0.0;
		String prodcode = "";
		double eligiblityamtPerSal = 0.0;
		String roiData = "select prod_code,RAT_INDEX from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
		Log.consoleLog(ifr, "listroiData====>" + listroiData);
		if (!listroiData.isEmpty()) {
			prodcode = listroiData.get(0).get(0);
			roi = Double.parseDouble(listroiData.get(0).get(1));
		}
		DecimalFormat df = new DecimalFormat("#.00");
		double dpnandodAvail = dpnAvail + odAvail;
		if (loanType.equalsIgnoreCase("DPN") || loanType.equalsIgnoreCase("OD")) {
			eligiblityamtPerSal = Double
					.parseDouble(df.format(grossSal * Double.parseDouble(completedYears) * 1.5 - dpnandodAvail));
		} else {
			eligiblityamtPerSal = Double
					.parseDouble(df.format(grossSal * Double.parseDouble(completedYears) * 1.5 - dpnAvail));
		}
		Log.consoleLog(ifr, "eligiblityamtPerSal : " + eligiblityamtPerSal);
		if (loanType.equalsIgnoreCase("DPN")) {
			double calculatePMT = calculatePMT(Integer.parseInt(tenure), roi);
			Log.consoleLog(ifr, "calculatePMT : " + calculatePMT);
			eligibleAsPerNth = (netSalAfterDed - 0.25 * grossSal) / calculatePMT * AcceleratorConstants.ONELAKH;
			Log.consoleLog(ifr, "eligibleAsPerNth : " + eligibleAsPerNth);
		} else {
			eligibleAsPerNth = (netSalAfterDed - 0.25 * grossSal) * 12.0 / AcceleratorConstants.ODRATE;
			Log.consoleLog(ifr, "eligibleAsPerNth : " + eligibleAsPerNth);
		}
		double eligiblityasPerPolicy = 0.0;
		String tag = (probation.equalsIgnoreCase("no") || probation.equalsIgnoreCase("n") || probation.contains("No"))
				? "N"
				: "Y";

		String odavailable = "";
		String maxLimitQuery = "select DPN_LIMIT,OD_LIMIT from LOS_STAFF_SCHEME_LIMIT where designation ='"
				+ designation.trim() + "' and probation_tag ='" + tag + "'";
		List<List<String>> resultQuery = ifr.getDataFromDB(maxLimitQuery);
		Log.consoleLog(ifr, "maxLimitQuery==>" + maxLimitQuery);
		if (!resultQuery.isEmpty()) {
			odavailable = resultQuery.get(0).get(1);
		}

		double calculateMinFinal = 0.0;
		double eligibleAmount = 0.0;
		String getLimitQuery = "select DPNUTILIZED, OD_UTILIZED, OD_AVAILABLE  from SLOS_STAFF_TRN where winame='"
				+ processInstanceId + "'";

		List<List<String>> getlimitResult = ifr.getDataFromDB(getLimitQuery);
		Log.consoleLog(ifr, "getlimitResult query==>" + getLimitQuery);
		Log.consoleLog(ifr, "getlimitResult==>res ==>" + getlimitResult);

		try {
			if (stage.equalsIgnoreCase("availoffer")) {
				String dpnUtlizied = getlimitResult.get(0).get(0);
				String odUtlizied = getlimitResult.get(0).get(1);
				String odAvailable = getlimitResult.get(0).get(2);

				if (!resultQuery.isEmpty()) {
					Log.consoleLog(ifr, "getLimitQuery==>" + getLimitQuery);
					if (!getlimitResult.isEmpty()) {
						eligiblityasPerPolicy = loanType.equalsIgnoreCase("DPN")
								? Double.parseDouble(resultQuery.get(0).get(0))
								: Double.parseDouble(resultQuery.get(0).get(0));
					}
				}

				if (loanType.equalsIgnoreCase("OD") || loanType.equalsIgnoreCase("DPN")) {
					eligiblityasPe = eligiblityasPerPolicy - Double.parseDouble(dpnUtlizied)
							- Double.parseDouble(odUtlizied);
				} else if (loanType.contains("Renewal") || loanType.contains("Enhancement")) {
					eligiblityasPe = eligiblityasPerPolicy - Double.parseDouble(dpnUtlizied);
				}

				eligibleAmount = calculateMin(eligiblityamtPerSal, eligibleAsPerNth, eligiblityasPe, loanType,
						dpnUtlizied, odUtlizied, ifr, odAvailable, amtRequest);
				Log.consoleLog(ifr, "eligibleAmount : " + eligibleAmount);
				calculateMinFinal = calculateMinFinal(eligibleAmount, Double.parseDouble(amtRequest));

				double notionalEMIProposed = Double.parseDouble(amtRequest) * AcceleratorConstants.ODRATE.doubleValue()
						/ 12.0D;

//				if (loanType.equalsIgnoreCase("OD") || loanType.contains("Renewal") || loanType.contains("Reduction")) {
//					Log.consoleLog(ifr, "entered into OD block for comparision with notionalEMIProposed");
//					if (netSalAfterDed - 0.25 * grossSal < notionalEMIProposed) {
//						return "error, You are not eligible for this amount. Kindly try a lower amount";
//					}
//				}
				if (loanType.equalsIgnoreCase("OD")) {
					Log.consoleLog(ifr, "entered into OD block for comparision with notionalEMIProposed");
					if (netSalAfterDed - 0.25 * grossSal < notionalEMIProposed) {
						return "error, You are not eligible for this amount. Kindly try a lower amount";
					}
				}

			} else {
				String dpnUtlizied = String.valueOf(dpnAvail);
				String odUtlizied = String.valueOf(odAvail);
				String odAvailable = odavailable;

				if (!resultQuery.isEmpty()) {
					Log.consoleLog(ifr, "getLimitQuery==>" + getLimitQuery);
					if (!getlimitResult.isEmpty()) {
						eligiblityasPerPolicy = loanType.equalsIgnoreCase("DPN")
								? Double.parseDouble(resultQuery.get(0).get(0))
								: Double.parseDouble(resultQuery.get(0).get(0));
					}
				}

				if (loanType.equalsIgnoreCase("DPN") || loanType.equalsIgnoreCase("OD")) {
					eligiblityasPe = eligiblityasPerPolicy - Double.parseDouble(dpnUtlizied)
							- Double.parseDouble(odUtlizied);
				}
//				else if (loanType.equalsIgnoreCase("OD")) {
//					eligiblityasPe = eligiblityasPerPolicy - Double.parseDouble(dpnUtlizied)
//							- Double.parseDouble(odUtlizied);
//				}
				else if (loanType.contains("Renewal") || loanType.contains("Enhancement")) {
					eligiblityasPe = eligiblityasPerPolicy - Double.parseDouble(dpnUtlizied);
				}

				eligibleAmount = calculateMin(eligiblityamtPerSal, eligibleAsPerNth, eligiblityasPe, loanType,
						dpnUtlizied, odUtlizied, ifr, odAvailable, amtRequest);
				Log.consoleLog(ifr, "eligibleAmount : " + eligibleAmount);
				calculateMinFinal = calculateMinFinal(eligibleAmount, Double.parseDouble(amtRequest));

				double notionalEMIProposed = Double.parseDouble(amtRequest) * AcceleratorConstants.ODRATE.doubleValue()
						/ 12.0D;

//				if (loanType.equalsIgnoreCase("OD") || loanType.contains("Renewal") || loanType.contains("Reduction")) {
//					Log.consoleLog(ifr, "entered into OD block for comparision with notionalEMIProposed");
//					if (netSalAfterDed - 0.25 * grossSal < notionalEMIProposed) {
//						return "error, You are not eligible for this amount. Kindly try a lower amount";
//					}
//				}
				if (loanType.equalsIgnoreCase("OD")) {
					Log.consoleLog(ifr, "entered into OD block for comparision with notionalEMIProposed");
					if (netSalAfterDed - 0.25 * grossSal < notionalEMIProposed) {
						return "error, You are not eligible for this amount. Kindly try a lower amount";
					}
				}
			}
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

		if (stage.equalsIgnoreCase("availoffer")) {
			String deletequery = "delete from SLOS_STAFF_ELIGIBILITY where pid='" + processInstanceId + "'";
			ifr.saveDataInDB(deletequery);
			String insertQuery = "insert into SLOS_STAFF_ELIGIBILITY(pid,ELIGIBILITY_AMT_PER_SALARY,ELIGIBILITY_AMT_PER_NTH,\n"
					+ "ELIGIBILITY_AMT_PER_POLICY,AMOUNT_REQUESTED,FINAL_ELIGIBILITY) values('" + processInstanceId
					+ "','" + String.format("%.2f", eligiblityamtPerSal) + "','"
					+ String.format("%.2f", eligibleAsPerNth) + "','" + String.format("%.2f", eligiblityasPe) + "','"
					+ calculateMinFinal + "','" + String.format("%.2f", eligibleAmount) + "')";

			Log.consoleLog(ifr, "insertQuery===> : " + insertQuery);
			this.cf.mExecuteQuery(ifr, insertQuery, "INSERT into SLOS_STAFF_ELIGIBILITY");
		}
		Log.consoleLog(ifr, "calculateMinFinal : " + calculateMinFinal);
		ifr.setValue("STAFF_LOAN_AMOUNT", String.valueOf(calculateMinFinal));
		ifr.setValue("STAFF_RATEOFINTEREST", String.valueOf(roi));
		Log.consoleLog(ifr, "STAFF_LOAN_AMOUNT ==>" + String.valueOf(calculateMinFinal));
		Log.consoleLog(ifr, "STAFF_RATEOFINTEREST ==>" + String.valueOf(roi));
		if (loanType.contains("DPN")) {
			String FrameSection = "AvailOfferHRMS";
			EMICalculator e = new EMICalculator();
			EMIAmount = e.getEmiCalculatorInstallment(ifr, processInstanceId, String.valueOf(calculateMinFinal),
					String.valueOf(tenure), String.valueOf(roi), FrameSection);
			if (EMIAmount.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return "error" + "," + "ERROR,technical glitch";
			}
			Log.consoleLog(ifr, "EMIInWords started");
			EMIInWords = LoanAmtInWords.amtInWords(Double.parseDouble(EMIAmount));
			Log.consoleLog(ifr, "EMIInWords ==>" + EMIInWords);
		}
		String netSalAfterDedInStr = String.valueOf(netSalAfterDed);
		String queryForLoanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
		List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
		Log.consoleLog(ifr, "res===>" + res);
		String totalInterest = "";
		String netDisburseAmt = "";
		if (!res.isEmpty() && res.get(0).get(0).toString().equals("DPN")) {

			totalInterest = String
					.valueOf(Double.parseDouble(EMIAmount) * Integer.parseInt(tenure) - calculateMinFinal);
			netDisburseAmt = String.valueOf(calculateMinFinal);
		} else {
			totalInterest = String.valueOf(0.149 * calculateMinFinal);
			netDisburseAmt = String.valueOf(calculateMinFinal);
		}
		Log.consoleLog(ifr, "totalInterest ==>" + totalInterest);
		Log.consoleLog(ifr, "netDisburseAmt ==>" + netDisburseAmt);

		String queryForloanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		String totalAmtPaidByBorrowers = "";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForloanType);
		List<List<String>> resqueryForLoanType = ifr.getDataFromDB(queryForloanType);
		Log.consoleLog(ifr, "resqueryForLoanType===>" + resqueryForLoanType);
		if (!resqueryForLoanType.isEmpty() && resqueryForLoanType.get(0).get(0).equalsIgnoreCase("DPN")) {

			totalAmtPaidByBorrowers = String.valueOf(calculateMinFinal + Double.parseDouble(totalInterest));
		} else if (!resqueryForLoanType.isEmpty()) {

			totalAmtPaidByBorrowers = String.valueOf(calculateMinFinal);
		}

		Log.consoleLog(ifr, "totalAmtPaidByBorrowers ==>" + totalAmtPaidByBorrowers);
		String totalAmtPaidByBorrowersnWords = LoanAmtInWords.amtInWords(Double.parseDouble(totalAmtPaidByBorrowers));
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

		if (stage.equalsIgnoreCase("availoffer")) {
			// double stampAmtCharges = Double.parseDouble(stampAmt);
		//	String otherCharges = String.valueOf(fixedfee + dDEfeeCharges + stampAmtCharges);
			Log.consoleLog(ifr, "netDisburseAmtInWords ==>" + netDisburseAmtInWords);
			String QueryamtInWords = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + calculateMinFinal
					+ "', LOAN_AMOUNT_IN_WORDS= '" + amtInWords + "',TENURE_MONTHS= '" + tenure + "',  NOTIONAL_EMI= '"
					+ String.format("%.2f", notionalEMI) + "', FINAL_LOAN_DEDUCTION='"
					+ String.format("%.2f", finalLoanDeduction) + "', NTH='" + netSalAfterDedInStr + "' WHERE WINAME= '"
					+ processInstanceId + "'";
//			String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET INTEREST_AMOUNT= '" + totalInterest
//					+ "', INTEREST_AMOUNT_IN_WORDS='" + intrestAmtInWords + "', NET_DISBURSED_AMOUNT='" + netDisburseAmt
//					+ "',NET_DISBURSED_AMOUNT_IN_WORDS='" + netDisburseAmtInWords + "',TOTAL_AMT_PAID_BORROWES='"
//					+ totalAmtPaidByBorrowers + "',TOTAL_AMT_PAID_BORROWES_WORDS='" + totalAmtPaidByBorrowersnWords
//					+ "',EMI='" + EMIAmount + "', EMI_IN_WORDS='" + EMIInWords + "',OTHERCHARGES='" + otherCharges
//					+ "',   DDECHARGES='" + dDEfeeCharges + "',STAMPCHARGES='" + String.format("%.2f", stampAmtCharges)
//					+ "', BRANCH='" + branchName + "',CESSCHARGES=" + cessChargesDB + "  WHERE WINAME= '"
//					+ processInstanceId + "'";
			String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET INTEREST_AMOUNT= '" + totalInterest
					+ "', INTEREST_AMOUNT_IN_WORDS='" + intrestAmtInWords + "', NET_DISBURSED_AMOUNT='" + netDisburseAmt
					+ "',NET_DISBURSED_AMOUNT_IN_WORDS='" + netDisburseAmtInWords + "',TOTAL_AMT_PAID_BORROWES='"
					+ totalAmtPaidByBorrowers + "',TOTAL_AMT_PAID_BORROWES_WORDS='" + totalAmtPaidByBorrowersnWords
					+ "',EMI='" + EMIAmount + "', EMI_IN_WORDS='" + EMIInWords + "'   WHERE WINAME= '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "QueryamtInWords==>" + QueryamtInWords);
			Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
			ifr.saveDataInDB(QueryamtInWords);
			ifr.saveDataInDB(QueryIntrestAmount);
			Log.consoleLog(ifr, "QueryamtInWords==>" + QueryamtInWords);
			Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
			ifr.saveDataInDB(QueryamtInWords);
			ifr.saveDataInDB(QueryIntrestAmount);

			EsignCommonMethods objEsign = new EsignCommonMethods();
			String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
			try {
				if (Integer.parseInt(triggeredStatus) > 0) {
					Log.consoleLog(ifr, "triggeredStatus===>" + triggeredStatus);
					String Query = "SELECT LOAN_AMOUNT,TENURE from SLOS_NESL_LOAN_PASSED_STATUS WHERE WINAME='"
							+ processInstanceId + "'";
					List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
					if (Output3.size() > 0) {
						String loanAmount = Output3.get(0).get(0);
						tenure = Output3.get(0).get(1);
						if (loanType.equalsIgnoreCase("DPN")) {
							String FrameSection = "AvailOfferHRMS";
							EMICalculator e = new EMICalculator();
							EMIAmount = e.getEmiCalculatorInstallment(ifr, processInstanceId, loanAmount, tenure,
									String.valueOf(roi), FrameSection);
							Log.consoleLog(ifr, "EMIAmount===> : " + EMIAmount);
							ifr.setValue("STAFF_EMI", EMIAmount);
							ifr.setStyle("STAFF_EMI", "disable", "true");
						}
						ifr.setValue("STAFF_TENURE", tenure);
						ifr.setStyle("STAFF_TENURE", "disable", "true");
						ifr.setValue("STAFF_LOAN_AMOUNT", loanAmount);
						ifr.setStyle("STAFF_LOAN_AMOUNT", "disable", "true");

					}
					String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + calculateMinFinal
							+ "' WHERE WINAME='" + processInstanceId + "'";

					Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
					ifr.saveDataInDB(queryUpdate);
					return "NESLDONE";
				} else {
					String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + calculateMinFinal
							+ "' WHERE WINAME='" + processInstanceId + "'";

					Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
					ifr.saveDataInDB(queryUpdate);
				}
			} catch (NumberFormatException ex) {
				return "error, Database Exception occured, please try after sometime";
			}
		}
		ifr.setValue("STAFF_PROCESSINGFEE", AcceleratorConstants.PROCESSINGFEE);
		Log.consoleLog(ifr, "EMIAmount===> : " + EMIAmount);
		ifr.setValue("STAFF_EMI", EMIAmount);
		Log.consoleLog(ifr, "tenure===> : " + tenure);
		ifr.setValue("STAFF_TENURE", tenure);
		Log.consoleLog(ifr, "EMIAmount after rounding off===> : " + EMIAmount);
		return String.valueOf(calculateMinFinal);
	}

	public String autoPopulateAvailOfferData(IFormReference ifr, String value) throws ParseException {
		ifr.setStyle("navigationNextBtn", "disable", "false");
		String limit = "";
		String orginallimit = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT DPN_AVAILABLE,OD_AVAILABLE,LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "res===>" + res);

//		String queryForRenewal = "SELECT s.LIMIT, l.OD_LIMIT FROM SLOS_ALL_ACTIVE_PRODUCT s INNER JOIN SLOS_STAFF_TRN b ON s.winame=b.winame inner join LOS_STAFF_SCHEME_LIMIT l on b.DESIGNATION=l.DESIGNATION WHERE b.WINAME='"
//				+ processInstanceId + "' AND s.PRODUCTCODE IN ('253','254','1129')";
//
//		Log.consoleLog(ifr, "queryForRenewal query===>" + queryForRenewal);
//		List<List<String>> resForRenewal = ifr.getDataFromDB(queryForRenewal);
		// Log.consoleLog(ifr, "res===>" + resForRenewal);

		if (!res.isEmpty() && res.get(0).get(2).toString().equals("DPN")) {
			String prodData = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

			List<List<String>> listprodData = ifr.getDataFromDB(prodData);
			Log.consoleLog(ifr, "listprodData====>" + listprodData);
			if (!listprodData.isEmpty()) {
				String prodcode = listprodData.get(0).get(0);

				String QueryProdSchemeDesc = "UPDATE SLOS_STAFF_TRN SET PROD_SCHEME_DESC= '" + prodcode
						+ "' WHERE WINAME= '" + processInstanceId + "'";

				Log.consoleLog(ifr, "QueryProdSchemeDesc==>" + QueryProdSchemeDesc);

				ifr.saveDataInDB(QueryProdSchemeDesc);

			}

			String loanAmt = getLoanAmt(ifr, processInstanceId, res.get(0).get(0), "120", "availoffer");
			Log.consoleLog(ifr, "loanAmt ===>" + loanAmt);
			if (loanAmt.contains("NESLDONE")) {
				return loanAmt;
			} else {
				setLoanAmt(ifr, processInstanceId, loanAmt);
				Log.consoleLog(ifr, "else block");
				return loanAmt + ",DPN";
			}
		}
		if (!res.isEmpty() && res.get(0).get(2).toString().equals("OD")) {

			updateProductCodeInProdSchmeDesc(ifr, processInstanceId);

			ifr.setStyle("STAFF_EMI", "visible", "false");
			String loanAmt = getLoanAmt(ifr, processInstanceId, res.get(0).get(1), "24", "availoffer");
			Log.consoleLog(ifr, "loanAmt ===>" + loanAmt);
			if (loanAmt.contains("NESLDONE")) {
				return loanAmt;
			} else {
				setLoanAmt(ifr, processInstanceId, loanAmt);
				Log.consoleLog(ifr, "else block");
				return loanAmt + ",OD";
			}
		}
		if (!res.isEmpty() && res.get(0).get(2).toString().contains("Renewal")) {
			ifr.setStyle("STAFF_EMI", "visible", "false");
			updateProductCodeInProdSchmeDesc(ifr, processInstanceId);
			String queryForRenewal = "SELECT s.LIMIT, l.OD_LIMIT FROM SLOS_ALL_ACTIVE_PRODUCT s INNER JOIN SLOS_STAFF_TRN b ON s.winame=b.winame inner join LOS_STAFF_SCHEME_LIMIT l on b.DESIGNATION=l.DESIGNATION WHERE b.WINAME='"
					+ processInstanceId + "' AND s.PRODUCTCODE IN ('253','254','1129')";

			Log.consoleLog(ifr, "queryForRenewal query===>" + queryForRenewal);
			List<List<String>> resForRenewal = ifr.getDataFromDB(queryForRenewal);
			Log.consoleLog(ifr, "res===>" + resForRenewal);
			if (!resForRenewal.isEmpty()) {
				// this is new implemented code ask by shivaji sir
				orginallimit = resForRenewal.get(0).get(0);
				limit = resForRenewal.get(0).get(1);
			}
			if (orginallimit != null && !orginallimit.trim().isEmpty() && limit != null && !limit.trim().isEmpty()) {

				double original = Double.parseDouble(orginallimit);
				double od = Double.parseDouble(limit);

				if (original > od) {
					return "error, As per policy your OD limit is Rs " + od
							+ ", kindly reduce your limit under OD Renewal with reduction facility.";
				}
			}
			String loanAmt = getLoanAmt(ifr, processInstanceId, limit, "24", "availoffer");
			String[] loanAmtsplit = loanAmt.split(",");
			if (loanAmtsplit.length > 1) {
				setLoanAmt(ifr, processInstanceId, loanAmtsplit[0]);
				return loanAmtsplit[1];
			} else {
				setLoanAmt(ifr, processInstanceId, loanAmt);
				return loanAmt + ",OD Renewal";
			}
		}
		if (!res.isEmpty() && res.get(0).get(2).toString().contains("Enhancement")) {
			updateProductCodeInProdSchmeDesc(ifr, processInstanceId);
			String queryForRenewal = "SELECT s.LIMIT, l.OD_LIMIT FROM SLOS_ALL_ACTIVE_PRODUCT s INNER JOIN SLOS_STAFF_TRN b ON s.winame=b.winame inner join LOS_STAFF_SCHEME_LIMIT l on b.DESIGNATION=l.DESIGNATION WHERE b.WINAME='"
					+ processInstanceId + "' AND s.PRODUCTCODE IN ('253','254','1129')";

			Log.consoleLog(ifr, "queryForRenewal query===>" + queryForRenewal);
			List<List<String>> resForRenewal = ifr.getDataFromDB(queryForRenewal);
			Log.consoleLog(ifr, "res===>" + resForRenewal);
			ifr.setStyle("STAFF_EMI", "visible", "false");
			if (!resForRenewal.isEmpty()) {
				limit = resForRenewal.get(0).get(1);
			}
			String loanAmt = getLoanAmt(ifr, processInstanceId, limit, "24", "availoffer");
			String[] loanAmtsplit = loanAmt.split(",");
			if (loanAmtsplit.length > 1) {
				setLoanAmt(ifr, processInstanceId, loanAmtsplit[0]);
				return loanAmtsplit[1];
			} else {
				setLoanAmt(ifr, processInstanceId, loanAmt);
				return loanAmt + ",OD Enhancement/Reduction";
			}
		}
		return "0.0";

	}

	private void updateProductCodeInProdSchmeDesc(IFormReference ifr, String processInstanceId) {
		String productQ = "SELECT b.PRODUCTCODE FROM SLOS_STAFF_TRN a INNER JOIN LOS_STAFF_SCHEME_LIMIT b ON a.DESIGNATION=b.DESIGNATION where a.WINAME ='"
				+ processInstanceId + "'";

		List<List<String>> listproductQ = ifr.getDataFromDB(productQ);
		if (!listproductQ.isEmpty()) {
			String productCode = listproductQ.get(0).get(0);
			Log.consoleLog(ifr, "productCode : " + productCode);
			String QueryProdSchemeDesc = "UPDATE SLOS_STAFF_TRN SET PROD_SCHEME_DESC= '" + productCode
					+ "' WHERE WINAME= '" + processInstanceId + "'";

			Log.consoleLog(ifr, "QueryProdSchemeDesc==>" + QueryProdSchemeDesc);

			ifr.saveDataInDB(QueryProdSchemeDesc);
		}
	}

	private void setLoanAmt(IFormReference ifr, String processInstanceId, String loanAmt) {
		String queryUpdate = "UPDATE SLOS_TRN_LOANSUMMARY SET CURRENT_STATUS ='" + loanAmt + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
	}

	private String getAmtRequested(IFormReference ifr) {
		String amtRequest = "";
		String slidderValues = this.pcm.getSliderAmount(ifr);
		Log.consoleLog(ifr, "slidderValues : " + slidderValues);
		if (slidderValues.equalsIgnoreCase("")) {
			return this.pcm.returnError(ifr);
		}
		String[] slidderValuesarr = slidderValues.split("~");
		amtRequest = slidderValuesarr[0];

		return amtRequest;
	}

	public String CBSFinalScreenValidation(IFormReference ifr, String processInstanceId, String loanType, String type) {
		Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
		Log.consoleLog(ifr, "processInstanceId===>" + processInstanceId);
		Log.consoleLog(ifr, "loanTypey===>" + loanType);
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		String LoanAccountNumber = "";
		if (!type.equalsIgnoreCase("backOffice")) {
			String statusCHeck = checkNesLLoanPassed(ifr, processInstanceId);
			if (statusCHeck.contains("error")) {
				return statusCHeck;
			}
		}
		try {
			if (loanType.equalsIgnoreCase("DPN")) {
				String status = objPreprocess.execLoanAccountCreation(ifr, "HRMS");
				String[] loanStatus = status.split(":");
				if (loanStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR) && loanStatus.length > 1) {
					return "error" + "," + loanStatus[1];
				}
				String query = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS WHERE PID='" + processInstanceId + "'";

				Log.consoleLog(ifr, "LoanAccount Number  query===>" + query);
				List<List<String>> res = ifr.getDataFromDB(query);
				Log.consoleLog(ifr, "res===>" + res);
				if (!res.isEmpty()) {
					LoanAccountNumber = res.get(0).get(0);
				}

				if (!status.contains(RLOS_Constants.ERROR)) {
					String CBSDisbursementEnquiry = objPreprocess.execDisbursementEnquiry(ifr, LoanAccountNumber,
							"HRMS");

					String[] CBSDisbursementEnquiryStatus = CBSDisbursementEnquiry.split(":");
					if (CBSDisbursementEnquiryStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
							&& CBSDisbursementEnquiryStatus.length > 1) {
						return "error" + "," + CBSDisbursementEnquiryStatus[1];
					}

					if (!CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR)) {
						String CBS_LoanDeduction = objPreprocess.execLoanDeduction(ifr, LoanAccountNumber, "HRMS");
						String[] CBS_LoanDeductionStatus = CBS_LoanDeduction.split(":");
						if (CBS_LoanDeductionStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
								&& CBS_LoanDeductionStatus.length > 1) {
							return "error" + "," + CBS_LoanDeductionStatus[1];
						}
						if (!CBS_LoanDeduction.contains(RLOS_Constants.ERROR)) {
							String SessionId = objPreprocess.execComputeLoanSchedule(ifr, LoanAccountNumber, "HRMS");
							String[] SessionIdStatus = SessionId.split(":");
							if (SessionIdStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
									&& SessionIdStatus.length > 1) {
								return "error" + "," + SessionIdStatus[1];
							}
							if (!SessionId.contains(RLOS_Constants.ERROR)) {
								String CBS_GenerateLoanSchedule = objPreprocess.execGenerateLoanSchedule(ifr,
										LoanAccountNumber, SessionId, "HRMS");

								String[] CBS_GenerateLoanScheduleStatus = CBS_GenerateLoanSchedule.split(":");
								if (CBS_GenerateLoanScheduleStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
										&& CBS_GenerateLoanScheduleStatus.length > 1) {
									return "error" + "," + CBS_GenerateLoanScheduleStatus[1];
								}
								if (!CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR)) {
									String CBS_SaveLoanSchedule = objPreprocess.execSaveLoanSchedule(ifr,
											LoanAccountNumber, SessionId, "HRMS");

									String[] CBS_SaveLoanScheduleStatus = CBS_SaveLoanSchedule.split(":");
									if (CBS_SaveLoanScheduleStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
											&& CBS_SaveLoanScheduleStatus.length > 1) {
										return "error" + "," + CBS_SaveLoanScheduleStatus[1];
									}
									if (!CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR)) {
										String CBS_BranchDisbursement = objPreprocess.execBranchDisbursement(ifr,
												LoanAccountNumber, "HRMS");

										String[] CBS_BranchDisbursementStatus = CBS_BranchDisbursement.split(":");
										if (CBS_BranchDisbursementStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
												&& CBS_BranchDisbursementStatus.length > 1) {
											return "error" + "," + CBS_BranchDisbursementStatus[1];
										}

										return CBS_BranchDisbursement;
									}

								}
							}
						}
					}
				}
			} else if (loanType.equalsIgnoreCase("OD")) {
				String productCode = "";
				String shortName = "";
				String customerID = "";
				String accoutActivate = "";
				String accoutNumber = "";
				String name = "";
				String productQ = "SELECT b.PRODUCTCODE, a.NAME FROM SLOS_STAFF_TRN a INNER JOIN LOS_STAFF_SCHEME_LIMIT b ON a.DESIGNATION=b.DESIGNATION where a.WINAME ='"
						+ processInstanceId + "'";

				List<List<String>> listproductQ = ifr.getDataFromDB(productQ);
				if (!listproductQ.isEmpty()) {
					productCode = listproductQ.get(0).get(0);
					name = listproductQ.get(0).get(1);
					Log.consoleLog(ifr, "productCode : " + productCode);
				}

				String query = "SELECT CUSTOMERID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='"
						+ processInstanceId + "'";

				Log.consoleLog(ifr, "#Inside query..." + query);

				List<List<String>> list = ifr.getDataFromDB(query);
				if (!list.isEmpty()) {
					customerID = list.get(0).get(0);
				} else {
					return "error" + "," + "customer id not found";
				}
				accoutNumber = objPreprocess.execLoanAccountBlocking(ifr, processInstanceId, customerID, "HRMS",
						productCode, name);

				String[] accoutNumberStatus = accoutNumber.split(":");
				if (accoutNumberStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR) && accoutNumberStatus.length > 1) {
					return "error" + "," + accoutNumberStatus[1];
				}
				if (!accoutNumber.contains(RLOS_Constants.ERROR)) {
					accoutActivate = objPreprocess.execLoanAccountActivate(ifr, processInstanceId, customerID, "HRMS",
							accoutNumber, shortName);

					String[] accoutActivateStatus = accoutActivate.split(":");
					if (accoutActivateStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
							&& accoutActivateStatus.length > 1) {
						return "error" + "," + accoutActivateStatus[1];
					}
					if (!accoutActivate.contains(RLOS_Constants.ERROR)) {
						String odLimitCreation = objPreprocess.execcreateLADODLoanAccount(ifr, processInstanceId,
								productCode);

						String[] odLimitCreationStatus = odLimitCreation.split(":");
						if (odLimitCreationStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
								&& odLimitCreationStatus.length > 1) {
							return "error" + "," + odLimitCreationStatus[1];
						}
						return odLimitCreation;
					}

				}
			} else if (loanType.contains("Renewal") || loanType.contains("Enhancement")) {
				String productCode = "";

				String productQ = "SELECT PRODUCTCODE FROM SLOS_ALL_ACTIVE_PRODUCT where WINAME ='" + processInstanceId
						+ "' AND PRODUCTCODE IN ('253','254','1129')";
				List<List<String>> listproductQ = ifr.getDataFromDB(productQ);
				if (!listproductQ.isEmpty()) {
					productCode = listproductQ.get(0).get(0);
					Log.consoleLog(ifr, "productCode : " + productCode);
				}

//				String productQ = "SELECT b.PRODUCTCODE FROM SLOS_STAFF_TRN a INNER JOIN LOS_STAFF_SCHEME_LIMIT b ON a.DESIGNATION=b.DESIGNATION where a.WINAME ='"
//						+ processInstanceId + "'";
//
//				List<List<String>> listproductQ = ifr.getDataFromDB(productQ);
//				if (!listproductQ.isEmpty()) {
//					productCode = listproductQ.get(0).get(0);
//					Log.consoleLog(ifr, "productCode : " + productCode);
//				}
				String odLimitModification = objPreprocess.execcreateLADODModication(ifr, processInstanceId,
						productCode);
				String[] odLimitModificationStatus = odLimitModification.split(":");
				if (odLimitModificationStatus[0].equalsIgnoreCase(RLOS_Constants.ERROR)
						&& odLimitModificationStatus.length > 1) {
					return "error" + "," + odLimitModificationStatus[1];
				}
				return odLimitModification;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return "ERROR";
		}
		return RLOS_Constants.ERROR;
	}

	private String checkNesLLoanPassed(IFormReference ifr, String processInstanceId) {

		try {
			String ten = "";
			String loanAmt = "";
			String loanAmount = "";
			String tenure = "";
			String queryForTenure = "Select TENURE_MONTHS,LOAN_AMOUNT from SLOS_STAFF_TRN WHERE WINAME = '"
					+ processInstanceId + "'";

			List<List<String>> list = ifr.getDataFromDB(queryForTenure);
			Log.consoleLog(ifr, "query for tenure" + queryForTenure);
			if (!list.isEmpty()) {
				ten = list.get(0).get(0);
				loanAmt = list.get(0).get(1);

			}

			String Query = "SELECT LOAN_AMOUNT,TENURE from SLOS_NESL_LOAN_PASSED_STATUS WHERE WINAME='"
					+ processInstanceId + "'";
			List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
			if (Output3.size() > 0) {
				loanAmount = Output3.get(0).get(0);
				tenure = Output3.get(0).get(1);
			}

			if (loanAmt.equalsIgnoreCase(loanAmount) && tenure.equalsIgnoreCase(ten)) {

			} else {
				String Querytenure = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT= '" + loanAmount + "', TENURE_MONTHS='"
						+ tenure + "' WHERE WINAME= '" + processInstanceId + "'";

				ifr.saveDataInDB(Querytenure);
			}

		} catch (Exception e) {
			return "error, Database Exception occured, please try after sometime";
		}
		return "success";

		// TODO Auto-generated method stub

	}

	private String sendLoanDisbursmentSmsEmail(IFormReference ifr, String stage, String emailStage,
			String netDisbursement) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		String scheme = "";
		String smsContent = "";
		String emailContent = "";
		String emailSubject = "";
		try {
			String query = "select LOAN_TYPE from SLOS_STAFF_TRN where winame='" + pid + "' ";
			List<List<String>> response = ifr.getDataFromDB(query);
			if (response.size() > 0) {
				scheme = response.get(0).get(0);
			}

			String smsBody = "select body from CAN_MST_EMAIL_HEADERS where stage='" + stage + "' and product='STAFF'";
			List<List<String>> smsResp = ifr.getDataFromDB(smsBody);
			if (smsResp.size() > 0) {
				smsContent = smsResp.get(0).get(0);
				smsContent = smsContent.replace("{#Scheme#}", scheme);
				smsContent = smsContent.replace("{#DisbLoanAmount#}", netDisbursement);
			}

			Log.consoleLog(ifr, "sendLoanDisbursmentSmsEmail/smsContent===>" + smsContent);

			String emailBody = "select body, subject from CAN_MST_EMAIL_HEADERS where stage='" + emailStage
					+ "' and product='STAFF'";

			List<List<String>> emailResp = ifr.getDataFromDB(emailBody);
			if (emailResp.size() > 0) {
				emailContent = emailResp.get(0).get(0);
				emailSubject = emailResp.get(0).get(1);
				emailContent = emailContent.replace("{#Scheme#}", scheme);
				emailContent = emailContent.replace("{#DisbLoanAmount#}", netDisbursement);
			}

			Log.consoleLog(ifr, "sendLoanDisbursmentSmsEmail/emailContent===>" + emailContent);
			Log.consoleLog(ifr, "sendLoanDisbursmentSmsEmail/emailSubject===>" + emailSubject);

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception ====>" + e);
			return RLOS_Constants.SUCCESS;
		}
		return RLOS_Constants.SUCCESS;
	}

	/**
	 * @param ifr
	 * @param value
	 * @return
	 */
	public String autopopulateDataHFinalEligibility(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "Entered into autopopulateDataHFinalEligibility Screen...");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String tenure = "";
		String loanAmt = "";
		String loanType = "";
		String queryForTenure = "Select TENURE_MONTHS,LOAN_AMOUNT,LOAN_TYPE from SLOS_STAFF_TRN WHERE WINAME = '"
				+ processInstanceId + "'";

		List<List<String>> list = ifr.getDataFromDB(queryForTenure);
		Log.consoleLog(ifr, "query for tenure" + queryForTenure);
		if (!list.isEmpty()) {
			tenure = list.get(0).get(0);
			loanAmt = list.get(0).get(1);
			loanType = list.get(0).get(2);
			Log.consoleLog(ifr, "tenure===>" + tenure);
			ifr.setValue("STAFF_LOAN_AMOUNT", loanAmt);
			ifr.setValue("STAFF_TENURE", tenure);
		}

		if (loanType.equalsIgnoreCase("OD") || loanType.equalsIgnoreCase("OD Renewal")
				|| loanType.equalsIgnoreCase("OD Enhancement/Reduction")) {
			ifr.setStyle("STAFF_EMI", "visible", "false");
		}
		String appNumQuery = "UPDATE SLOS_STAFF_TRN set LOAN_APPLICATION_NUMBER=(SELECT APPLICATION_NO FROM LOS_WIREFERENCE_TABLE where WINAME='"
				+ processInstanceId + "') where WINAME='" + processInstanceId + "'";

		ifr.saveDataInDB(appNumQuery);

		EsignCommonMethods objEsign = new EsignCommonMethods();
		String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
		if (Integer.parseInt(triggeredStatus) > 0) {
			Log.consoleLog(ifr, "triggeredStatus===>" + triggeredStatus);
			return triggeredStatus;
		}
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Final Eligibility and Doc' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		String queryUpdateNesLStatus = "UPDATE SLOS_TRN_LOANSUMMARY SET ESIGNSTATUS='YES' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdateNesLStatus : " + queryUpdateNesLStatus);
		ifr.saveDataInDB(queryUpdateNesLStatus);
		return "0";
	}

	public String mImpOnClickFianlEligibility(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "populateFinalEligibilityDetail : ");
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		EsignCommonMethods objEsign = new EsignCommonMethods();
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String checkbox1 = ifr.getValue("Portal_RB_Final_STAFF").toString();
		String checkbox2 = ifr.getValue("Portal_RB_Final_STAFF_2").toString();
		String checkbox3 = ifr.getValue("Portal_RB_Final_STAFF_3").toString();
		if (checkbox1.equalsIgnoreCase("true") && checkbox2.equalsIgnoreCase("true")
				&& checkbox3.equalsIgnoreCase("true")) {
			String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", PID)
					.replaceAll("#checkbox2#", checkbox2);
			Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
			ifr.saveDataInDB(queryCheckbox);

		} else {
			return "error,Kindly Select all Disclaimer and consent!";
		}

		try {
			String neslMode = pcm.getNESLModeQuery(ifr, "STAFF");
			Log.consoleLog(ifr, "neslMode===>" + neslMode);
			if (neslMode.equalsIgnoreCase("1")) {

				EsignCommonMethods ecm = new EsignCommonMethods();
				String returnMessage = ecm.getNESLWorkflowStatus(ifr);
				Log.errorLog(ifr, "returnMessage:" + returnMessage);

				if (!returnMessage.equalsIgnoreCase("SUCCESS")) {
					JSONObject message = new JSONObject();
					message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", returnMessage));
					return message.toString();
				}
				return "";
			}

			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String prodCode = "";
			String prodCodeName = "";
			String nth = "";
			String grossSal = "";
			String loanAmount = "";
			String tenure = "";
			String SBACCNUMBER = "";
			String loanType = "";
			String prodcode = "";
			String prodData = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

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
			if (!res.isEmpty() && res.get(0).get(0).toString().equals("DPN")) {
				loanType = res.get(0).get(0);
				nth = res.get(0).get(1);
				grossSal = res.get(0).get(2);
				loanAmount = res.get(0).get(3);
				tenure = res.get(0).get(4);
				SBACCNUMBER = res.get(0).get(5);

				String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
						+ processInstanceId + "'";
				List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
				Log.consoleLog(ifr, "query for esign===>" + queryESign);

				String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
				String apiCriterion = "'CBS_FundTransfer'";
				String executeCBSStatus = cm.executeCBSStatus(ifr, PID, apiCriterion);
				if (!executeCBSStatus.equalsIgnoreCase("SUCCESS")) {
					String response = getLoanAmt(ifr, processInstanceId, loanAmount, tenure, "finalEligiblity");
					Log.consoleLog(ifr, "responseAfter360CallinFinalEligiblity : " + response);
					if (response != null && !response.isEmpty()) {
						String goBack = checkEligiblityLoantakenFromBranch(ifr, processInstanceId, response);
						if (goBack.contains("GOBACK") || goBack.contains("error")) {
							return goBack;
						}
					}

					String ammortizationHRMS = getAmmortizationHRMS(ifr, processInstanceId, prodcode, Double.parseDouble(nth),
							Double.parseDouble(grossSal), loanAmount, tenure,loanType);
					if(ammortizationHRMS.contains("error"))
					{
						return ammortizationHRMS;
					}
					Date currentDate = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
					String formattedDate = dateFormat.format(currentDate);
					String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
							+ "' WHERE winame = '" + processInstanceId + "'";

					ifr.saveDataInDB(updateQuerySD);
					mGenerateDoc(ifr, loanType);
				}

				String charDebit = "";
				String CBS_FundTransfer = "";
				String QueryCh = "SELECT CHARGES_DEBITED_FOR  from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> Output4 = cf.mExecuteQuery(ifr, QueryCh, QueryCh);
				if (Output4.size() > 0) {
					charDebit = Output4.get(0).get(0);
				}
				if (charDebit != null && !charDebit.isEmpty()) {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, charDebit, "HRMS", tenure, loanType,
							loanAmount);
				} else {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER, "HRMS", tenure, loanType,
							loanAmount);
				}

				String[] fundTransfer = CBS_FundTransfer.split(":");

				if (fundTransfer[0].equalsIgnoreCase(RLOS_Constants.ERROR) && fundTransfer.length > 1) {
					Log.consoleLog(ifr, "Fund Transfer inside===================>");
					return "error" + "," + fundTransfer[1];
				}
				if (CBS_FundTransfer.equalsIgnoreCase("SUCCESS")) {
					String currentStepForStaff = "Final Eligibility and Doc";
					String queryForFT = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='" + currentStepForStaff + "'"
							+ "where WINAME='" + processInstanceId + "'";
					Log.consoleLog(ifr, "query for currentStageModification : " + queryForFT);
					ifr.saveDataInDB(queryForFT);
					try {
						// String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
						if (Integer.parseInt(triggeredStatus) == 0) {
							EsignIntegrationChannel NESL = new EsignIntegrationChannel();

							String returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_DPN", "eStamping",
									loanType);
							Log.errorLog(ifr, "returnMessage:" + returnMessage);

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
						Log.consoleLog(ifr,
								"Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						return "error, Server Error , please try after sometime";
					}
				}

			} else if (!res.isEmpty() && res.get(0).get(0).toString().equals("OD")
					|| !res.isEmpty() && res.get(0).get(0).toString().contains("Renewal")
					|| !res.isEmpty() && res.get(0).get(0).toString().contains("Enhancement")) {
				// prodCode = "254";
				String returnMessage = "";
				Double loanAmt = 0.0;
				Double odUtilized = 0.0;
				String Query = "SELECT LOAN_AMOUNT,TENURE_MONTHS,SALARY_ACC_NUMBER,LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
				if (Output3.size() > 0) {
					loanAmount = Output3.get(0).get(0);
					tenure = Output3.get(0).get(1);
					SBACCNUMBER = Output3.get(0).get(2);
					loanType = Output3.get(0).get(3);
					odUtilized = Double.parseDouble(Output3.get(0).get(4));
				}

				String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
						+ processInstanceId + "'";
				List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
				Log.consoleLog(ifr, "query for esign===>" + queryESign);

				String apiCriterion = "'CBS_FundTransfer'";
				String executeCBSStatus = cm.executeCBSStatus(ifr, PID, apiCriterion);
				if (!executeCBSStatus.equalsIgnoreCase("SUCCESS")) {
					String response = getLoanAmt(ifr, processInstanceId, loanAmount, tenure, "finalEligiblity");
					Log.consoleLog(ifr, "responseAfter360CallinFinalEligiblity : " + response);
					if (response != null && !response.isEmpty()) {
						String goBack = checkEligiblityLoantakenFromBranch(ifr, processInstanceId, response);
						if (goBack.contains("GOBACK") || goBack.contains("error")) {
							return goBack;
						}
					}
					Date currentDate = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
					String formattedDate = dateFormat.format(currentDate);
					String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
							+ "' WHERE winame = '" + processInstanceId + "'";

					ifr.saveDataInDB(updateQuerySD);
					mGenerateDoc(ifr, loanType);
				}

				String charDebit = "";
				String CBS_FundTransfer = "";
				String QueryCh = "SELECT CHARGES_DEBITED_FOR  from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> Output4 = cf.mExecuteQuery(ifr, QueryCh, QueryCh);
				if (Output4.size() > 0) {
					charDebit = Output4.get(0).get(0);
				}
				if (charDebit != null && !charDebit.isEmpty()) {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, charDebit, "HRMS", tenure, loanType,
							loanAmount);
				} else {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER, "HRMS", tenure, loanType,
							loanAmount);
				}

				String[] fundTransfer = CBS_FundTransfer.split(":");

				if (fundTransfer[0].equalsIgnoreCase(RLOS_Constants.ERROR) && fundTransfer.length > 1) {
					return "error" + "," + fundTransfer[1];
				}
				if (CBS_FundTransfer.equalsIgnoreCase("SUCCESS")) {
					String currentStepForStaff = "Final Eligibility and Doc";
					String queryForFT = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='" + currentStepForStaff + "'"
							+ "where WINAME='" + processInstanceId + "'";
					Log.consoleLog(ifr, "query for currentStageModification : " + queryForFT);
					ifr.saveDataInDB(queryForFT);

					// mGenerateDoc(ifr, loanType);
					// EsignCommonMethods objEsign = new EsignCommonMethods();
					try {
						String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
						if (Integer.parseInt(triggeredStatus) == 0) {
//						if ((!res.isEmpty() && !res.get(0).get(0).equalsIgnoreCase("Y")
//								&& !res.get(0).get(1).equalsIgnoreCase("Success")) || res.isEmpty()) {
							EsignIntegrationChannel NESL = new EsignIntegrationChannel();

							if (loanType.equalsIgnoreCase("OD")) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD", "eStamping", loanType);
							} else if (loanType.contains("Renewal")) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_RENEWAL", "eStamping",
										loanType);
							} else if (loanType.contains("Enhancement")
									&& Double.parseDouble(loanAmount) > odUtilized) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_ENHANCEMENT", "eStamping",
										loanType);
							} else if (loanType.contains("Enhancement")
									&& Double.parseDouble(loanAmount) <= odUtilized) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_REDUCTION", "eStamping",
										loanType);
							}
							Log.errorLog(ifr, "returnMessage from HRMSPortal NESL Call:" + returnMessage);

							if (returnMessage.contains(RLOS_Constants.ERROR))
								return pcm.returnErrorHold(ifr);
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
						Log.consoleLog(ifr,
								"Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						return "error, Server Error , please try after sometime";
					}
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return this.pcm.returnErrorHold(ifr);
		}

		return "";
	}

	private String checkEligiblityLoantakenFromBranch(IFormReference ifr, String processInstanceId, String response) {
		String queryforEligiblity = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE PID='"
				+ processInstanceId + "'";
		List<List<String>> resultforEligiblity = ifr.getDataFromDB(queryforEligiblity);
		Log.consoleLog(ifr, "resultforEligiblity===>" + resultforEligiblity);
		if (!resultforEligiblity.isEmpty()) {
			String result = resultforEligiblity.get(0).get(0);
			try {
				String responseClean = response.trim();
				String resultClean = result.trim();

				Log.consoleLog(ifr, "Attempting to parse result: '" + result + "'");
				Log.consoleLog(ifr, "Attempting to parse response: '" + response + "'");
				double responseElg = Double.parseDouble(responseClean);
				Log.consoleLog(ifr, "Attempting to parse responseElg: '" + responseElg + "'");
				double resultElg = Double.parseDouble(resultClean);
				Log.consoleLog(ifr, "Attempting to parse resultElg: '" + resultElg + "'");
//							if (!isNumeric(responseClean) || !isNumeric(resultClean)) {
//								return "error, invalid format for loanamount please check";
//							}

				if (responseElg < resultElg) {
					Log.consoleLog(ifr, "GOBACK");
					return "GOBACK";
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
				return "error, unexpected exception";
			}
		}
		return "ok";
	}

	public static boolean isNumeric(String str) {
		if (str == null)
			return false;
		try {
			Double.parseDouble(str.trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
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

	public void mGenerateDoc(IFormReference ifr, String loanType) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Double loanAmt = 0.0;
		Double odUtilized = 0.0;
		String query = "SELECT LOAN_AMOUNT, OD_UTILIZED FROM SLOS_STAFF_TRN WHERE WINAME='" + PID + "'";
		Log.consoleLog(ifr, "mGenerateDoc query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			loanAmt = Double.parseDouble(res.get(0).get(0));
			odUtilized = Double.parseDouble(res.get(0).get(1));
		}
		if (loanType.equalsIgnoreCase("DPN")) {
			Log.consoleLog(ifr, "Loan Type in mGenerateDoc===" + loanType);
			docGen(ifr, "AGREEMENT", "STAFF_LOAN");
			docGen(ifr, "KFS", "STAFF_LOAN");
			docGen(ifr, "NF_425", "STAFF_LOAN");
			docGen(ifr, "PROCESS_NOTE", "STAFF_LOAN");
			docGen(ifr, "REPAYMENT_LETTER", "STAFF_LOAN");
			docGen(ifr, "SANCTION_LETTER", "STAFF_LOAN");
		} else if (loanType.contains("Renewal")) {
			// docGen(ifr, "AGREEMENT", "STAFF_LOAN");
			docGen(ifr, "KFS_OD", "STAFF_LOAN");
			docGen(ifr, "NF_425", "STAFF_LOAN");
			docGen(ifr, "PROCESS_NOTE", "STAFF_LOAN");
			docGen(ifr, "REPAYMENT_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "SANCTION_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "NF_493_OD_RENEWABLE", "STAFF_LOAN");
		} else if (loanType.contains("Enhancement") && loanAmt > odUtilized) {
			// docGen(ifr, "AGREEMENT_ENHANCEMENT", "STAFF_LOAN");
			docGen(ifr, "AGREEMENT", "STAFF_LOAN");
			docGen(ifr, "KFS_OD", "STAFF_LOAN");
			docGen(ifr, "NF_425", "STAFF_LOAN");
			docGen(ifr, "PROCESS_NOTE", "STAFF_LOAN");
			docGen(ifr, "REPAYMENT_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "SANCTION_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "NF_493_OD_RENEWABLE", "STAFF_LOAN");
		} else if (loanType.equalsIgnoreCase("OD")) {
			docGen(ifr, "AGREEMENT", "STAFF_LOAN");
			docGen(ifr, "KFS_OD", "STAFF_LOAN");
			docGen(ifr, "NF_425", "STAFF_LOAN");
			docGen(ifr, "PROCESS_NOTE", "STAFF_LOAN");
			docGen(ifr, "REPAYMENT_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "SANCTION_LETTER_OD", "STAFF_LOAN");
		} else if (loanType.contains("Enhancement") && loanAmt <= odUtilized) {
			docGen(ifr, "AGREEMENT", "STAFF_LOAN");
			docGen(ifr, "KFS_OD", "STAFF_LOAN");
			docGen(ifr, "NF_425", "STAFF_LOAN");
			docGen(ifr, "PROCESS_NOTE", "STAFF_LOAN");
			docGen(ifr, "REPAYMENT_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "SANCTION_LETTER_OD", "STAFF_LOAN");
			docGen(ifr, "NF_493_OD_RENEWABLE", "STAFF_LOAN");
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

	public void autoOnLoadAccountDetailsScreen(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "autoOnLoadAccountDetailsScreen : ");
		String salAccNum = "";
		String loanAmt = "";
		String loanType = "";
		String tenure = "";
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String loanDisbCondMode = pcm.getConstantValue(ifr, "HRMSLOANDISBURSE", "CONDITIONALENABLE");// Y,N
		Log.consoleLog(ifr, "loanDisbCondMode====>" + loanDisbCondMode);

		boolean loanDisbFlag = false;
		if (loanDisbCondMode.equalsIgnoreCase("Y")) {
			Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Enabled##");
			Date d = new Date();
			SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
			String curDate = sd1.format(d);

			String Query1 = "INSERT INTO SLOS_STG_LOANDISBURSEMENT(WINAME, STATUS, CREATEDDATE, UPDATEDDATE) "
					+ "SELECT '" + PID + "', '', '" + curDate + "','' " + "FROM dual " + "WHERE NOT EXISTS ("
					+ "    SELECT 1 FROM SLOS_STG_LOANDISBURSEMENT WHERE WINAME = '" + PID + "'" + ")";
			Log.consoleLog(ifr, "Query1 SLOS_STG_LOANDISBURSEMENT ===>" + Query1);
			cf.mExecuteQuery(ifr, Query1, "autoOnLoadAccountDetailsScreen");

//			String Query1 = "INSERT INTO SLOS_STG_LOANDISBURSEMENT(WINAME,STATUS,CREATEDDATE,UPDATEDDATE) "
//					+ "VALUES ('" + PID + "','','" + curDate + "','')";
//			Log.consoleLog(ifr, "Query1==>" + Query1);
//			ifr.saveDataInDB(Query1);

			String loanDisbMode = pcm.getConstantValue(ifr, "HRMSLOANDISBURSE", "ENABLE");
			String selectQuery = "select status from SLOS_STG_LOANDISBURSEMENT where winame='" + PID + "'";
			List<List<String>> resSelectQuery = ifr.getDataFromDB(selectQuery);

			if (loanDisbMode.equalsIgnoreCase("Y")) {
				if (resSelectQuery.get(0).get(0).trim().isEmpty()) {
					String queryUpdate = "UPDATE SLOS_STG_LOANDISBURSEMENT SET STATUS='Y' WHERE WINAME='" + PID + "'";
					Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
					ifr.saveDataInDB(queryUpdate);
					// loanDisbFlag = true;
				}
			}

			if (loanDisbMode.equalsIgnoreCase("N")) {
				if (resSelectQuery.get(0).get(0).trim().isEmpty()) {
					String queryUpdate = "UPDATE SLOS_STG_LOANDISBURSEMENT SET STATUS='N' WHERE WINAME='" + PID + "'";
					Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
					ifr.saveDataInDB(queryUpdate);
					// loanDisbFlag = false;
				}
			}

		} else {
			Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Disabled##");
			loanDisbFlag = false;
		}

		Log.consoleLog(ifr, "##loanDisbFlag##" + loanDisbFlag);

		String query = "Select SALARY_ACC_NUMBER, LOAN_AMOUNT, LOAN_TYPE, TENURE_MONTHS from SLOS_STAFF_TRN WHERE WINAME = '"
				+ PID + "'";

		List<List<String>> list = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query " + query);
		if (!list.isEmpty()) {
			salAccNum = list.get(0).get(0);
			loanAmt = list.get(0).get(1);
			loanType = list.get(0).get(2);
			tenure = list.get(0).get(3);
			Log.consoleLog(ifr, "salAccNum===>" + salAccNum);
			Log.consoleLog(ifr, "loanAmt===>" + loanAmt);
			Log.consoleLog(ifr, "loanType===>" + loanType);
			if (loanType.equalsIgnoreCase("OD") || loanType.contains("Renewal") || loanType.contains("Enhancement")) {
				ifr.setStyle("STAFF_EMI", "visible", "false");
			}
			ifr.setValue("STAFF_SBACCOUNTNO", salAccNum);
			ifr.setValue("STAFF_REPAYMENTNO", salAccNum);
			ifr.setValue("STAFF_NETDISBURSEMENTNO", loanAmt);
			ifr.setValue("STAFF_LOAN_AMOUNT", loanAmt);
			ifr.setValue("STAFF_TENURE", tenure);
		}

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Receive the Money' WHERE WINAME='" + PID
				+ "' AND Application_Status!='COMPLETED' ";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
	}

	public void autoOnLoadFinalScreen(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "autoOnLoadFinalScreen : ");
		String loanaccno = "";
		String disbdate = "";
		String sancAmt = "";
		String emidtst = "";
		String loanType = "";
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "Select a.LOAN_ACCOUNTNO,a.DISB_DATE,a.SANCTION_AMOUNT,b.LOAN_TYPE,a.EMI_STARTDATE from SLOS_TRN_LOANDETAILS a INNER JOIN SLOS_STAFF_TRN b ON a.PID=b.WINAME  WHERE a.PID  = '"
				+ PID + "'";

		List<List<String>> list = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query " + query);
		if (!list.isEmpty()) {
			loanaccno = list.get(0).get(0);
			Log.consoleLog(ifr, "loanaccno===>" + loanaccno);
			ifr.setValue("H_HRMS_RM_LOANACCNO", loanaccno);
			disbdate = list.get(0).get(1);
			Log.consoleLog(ifr, "disbdate===>" + disbdate);
			ifr.setValue("H_HRMS_RM_DTDISB", disbdate);
			sancAmt = list.get(0).get(2);
			Log.consoleLog(ifr, "sancAmt===>" + sancAmt);
			ifr.setValue("H_HRMS_RM_LNAMT", sancAmt);
			loanType = list.get(0).get(3);
			if (loanType.equalsIgnoreCase("DPN")) {
				emidtst = list.get(0).get(4);
				Log.consoleLog(ifr, "emidtst===>" + emidtst);
				ifr.setValue("H_HRMS_RM_EMISTDT", emidtst);
			} else {
				ifr.setValue("H_HRMS_RM_EMISTDT", "NA");
			}
		}

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Summary' WHERE WINAME='" + PID
				+ "' AND Application_Status='COMPLETED' ";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		ifr.setValue("Q_DataOnDemand", sancAmt);
	}

	public String mImplApplicationNameRefrenceNum(IFormReference ifr, String ProductType) {
		Log.consoleLog(ifr, "Inside mImplApplicationNameRefrenceNum : ");

		try {
			String mobileData = "";
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String query = "SELECT MOBILENUMBER ,LoanType from LOS_WIREFERENCE_TABLE where WINAME = '"
					+ processInstanceId + "' ";

			String Loantype = "";
			String ApplicationNumber = "";
			Log.consoleLog(ifr, "mobileData query : " + query);
			List<List<String>> list = ifr.getDataFromDB(query);
			if (!list.isEmpty()) {
				mobileData = list.get(0).get(0);
				Loantype = list.get(0).get(1);
			}

			try {
				String ApplicationNumberQuery = "SELECT APPLICATIONNUMBER from LOS_EXT_TABLE where ITEMINDEX IN(SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE PROCESSINSTANCEID='"
						+ processInstanceId + "' AND ROWNUM=1)";

				Log.consoleLog(ifr, "ApplicationNumberQuery==>" + ApplicationNumberQuery);
				List<List<String>> Result1 = ifr.getDataFromDB(ApplicationNumberQuery);
				String AppNumber = "";
				if (!Result1.isEmpty()) {
					AppNumber = Result1.get(0).get(0);
					if (AppNumber.isEmpty() || AppNumber == null) {
						String applicationNumQuery = "SELECT APPLICATION_NO FROM LOS_WIREFERENCE_TABLE WHERE  WINAME ='"
								+ processInstanceId + "'";
						List<List<String>> applicationNumQueryResult = ifr.getDataFromDB(applicationNumQuery);
						if (!applicationNumQueryResult.isEmpty()) {
							AppNumber = applicationNumQueryResult.get(0).get(0);
						}
					}
				}

				Log.consoleLog(ifr, "AppNumber From Database" + AppNumber);

				if (AppNumber.equalsIgnoreCase("")) {
					Log.consoleLog(ifr, "AppNumber is empty");

					EsignCommonMethods objEsign = new EsignCommonMethods();
					String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
					if (Integer.parseInt(triggeredStatus) == 0) {
						ApplicationNumber = pcm.GenerateApplicationNumber(ifr, "RETAIL");
					} else {
						return "error occured on Database end, Please try after sometimes!";
					}

					if (this.pcm.CheckDuplicateAppNumber(ifr, ApplicationNumber)) {
						Log.consoleLog(ifr, "No duplicates..");
						ifr.setValue("P_PAPL_REFERENCENO1", ApplicationNumber);
						String AppNumUpQuery = "UPDATE LOS_WIREFERENCE_TABLE SET APPLICATION_NO='" + ApplicationNumber
								+ "' WHERE winame='" + processInstanceId + "'";

						Log.consoleLog(ifr, "AppNumUpQuery==> " + AppNumUpQuery);
						ifr.saveDataInDB(AppNumUpQuery);

						String AppNumUpQuery1 = "UPDATE LOS_EXT_TABLE SET APPLICATIONNUMBER='" + ApplicationNumber
								+ "' WHERE ITEMINDEX IN (SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE PROCESSINSTANCEID='"
								+ processInstanceId + "')";

						Log.consoleLog(ifr, "AppNumUpQuery1==> " + AppNumUpQuery1);
						ifr.saveDataInDB(AppNumUpQuery1);
					} else {
						ApplicationNumber = pcm.GenerateApplicationNumber(ifr, "RETAIL");
						ifr.setValue("P_PAPL_REFERENCENO1", ApplicationNumber);

						String AppNumUpQuery = "UPDATE LOS_WIREFERENCE_TABLE SET APPLICATION_NO='" + ApplicationNumber
								+ "' WHERE winame='" + processInstanceId + "'";

						Log.consoleLog(ifr, "AppNumUpQuery==> " + AppNumUpQuery);
						ifr.saveDataInDB(AppNumUpQuery);

						String AppNumUpQuery1 = "UPDATE LOS_EXT_TABLE SET APPLICATIONNUMBER='" + ApplicationNumber
								+ "' WHERE ITEMINDEX IN (SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE PROCESSINSTANCEID='"
								+ processInstanceId + "')";

						Log.consoleLog(ifr, "AppNumUpQuery1==> " + AppNumUpQuery1);
						ifr.saveDataInDB(AppNumUpQuery1);
					}

					String bodyParams = "RETAIL#" + ApplicationNumber;
					String subjectParams = ApplicationNumber;
					String fileName = "";
					String str1 = "";

				} else {

					Log.consoleLog(ifr, "AppNumber is available");
				}

			} catch (Exception e) {
				Log.consoleLog(ifr, "ApplicationNumber Exception : " + e);
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  mImplApplicationNameRefrenceNum" + e);
		}
		return "success";
	}

	public String downLoadSignedGeneratedDocument(IFormReference ifr) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String getDocIndexQuery = "Select d.documentindex from PDBDOCUMENTCONTENT d \ninner join pdbfolder e on d.parentfolderindex = e.folderindex \ninner join PDBDOCUMENT f on d.documentindex = f.DOCUMENTINDEX \nWHERE e.NAME ='"
				+ pid + "' and f.NAME like 'NESL_%'";

		Log.consoleLog(ifr, "downLoadSignedGeneratedDocument/getDocIndexQuery===>" + getDocIndexQuery);
		String docContent = getDocumentContent(ifr, getDocIndexQuery);
		return docContent;
	}

	public String downLoadGeneratedDocument(IFormReference ifr, String name) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String getDocIndexQuery = "Select d.DOCUMENTINDEX from PDBDOCUMENTCONTENT d inner join pdbfolder e on d.parentfolderindex = e.folderindex inner join PDBDOCUMENT f on d.documentindex = f.DOCUMENTINDEX WHERE f.name='"
				+ name + "' and e.NAME ='" + pid + "'";

		Log.consoleLog(ifr, "downLoadSignedGeneratedDocument/getDocIndexQuery===>" + getDocIndexQuery);
		String docContent = getDocumentContent(ifr, getDocIndexQuery);
		return docContent;
	}

	public String getDocumentContent(IFormReference ifr, String query) {
		List<List<String>> response = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "getDocumentContent/response " + response);
		JSONArray jsonArr1 = new JSONArray();
		if (response.size() > 0) {
			try {
				for (int i = 0; i < response.size(); i++) {
					Log.consoleLog(ifr, "getDocumentContent/doceIndex " + response.get(i).get(0));
					String ominDocResponse = this.ominDocDownload.downloadDocumentFromOD(ifr, response.get(i).get(0));
					JSONParser parser = new JSONParser();
					JSONObject OutputJSON = (JSONObject) parser.parse(ominDocResponse);
					JSONObject resultObj = new JSONObject((Map) OutputJSON);
					Log.consoleLog(ifr, "getDocumentContent/doceIndex " + resultObj.get("statusCode"));
					if (!resultObj.get("statusCode").equals("500")) {
						jsonArr1.add(resultObj);
					}
				}
				Log.consoleLog(ifr, "getDocumentContent/no of documents" + jsonArr1.size());
				if (jsonArr1.size() > 0) {
					return jsonArr1.toString();
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "getDocumentContent doc exception:- " + e.getMessage());
				Log.consoleLog(ifr, "getDocumentContent doc exception:- " + e.getLocalizedMessage());
			}
		}
		Log.consoleLog(ifr, "getDocumentContent/final error");
		return RLOS_Constants.ERROR;
	}

	public String generateRefNumber(IFormReference ifr) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT     NVL(CUSTOMERFIRSTNAME, '') || ' ' ||NVL(CUSTOMERMIDDLENAME,'') || ' ' || NVL(CUSTOMERLASTNAME, '') AS FULLNAME \nFROM     los_trn_customersummary where winame ='"
				+ pid + "'";

		List<List<String>> result = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "customerName==> " + query);
		String name = "";
		String ApplicationNumber = "";
		if (!result.isEmpty()) {
			name = result.get(0).get(0);
		}
		ifr.setValue("P_PAPL_CUSTOMERNAME1", name);
		String mImplApplicationNameRefrenceNum = mImplApplicationNameRefrenceNum(ifr, "HRMS");
		if (mImplApplicationNameRefrenceNum.contains("error")) {
			return mImplApplicationNameRefrenceNum;
		}
		String queryForAppRef = "SELECT APPLICATION_NO FROM LOS_WIREFERENCE_TABLE WHERE WINAME='" + pid + "'";
		Log.consoleLog(ifr, "application number uery===>" + queryForAppRef);
		List<List<String>> res = ifr.getDataFromDB(queryForAppRef);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			ApplicationNumber = res.get(0).get(0);
		}

		String bodyParams = "STAFF#" + ApplicationNumber;
		String subjectParams = ApplicationNumber;
		String fileName = "";
		String fileContent = "";
		try {
			this.pcm.triggerCCMAPIs(ifr, pid, "STAFF", "27", bodyParams, subjectParams, fileName, fileContent);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception ==>" + e);
		}
		ifr.setValue("P_PAPL_REFERENCENO1", ApplicationNumber);
		// ifr.setValue("Q_ApproveRecommendAuthority", ApplicationNumber);
		return "success";
	}

	public String mClickEligiblityValidation(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT DPN_AVAILABLE,OD_AVAILABLE,LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty() && res.get(0).get(2).toString().equals("DPN")
				&& res.get(0).get(0).toString().equals("0.0")) {
			JSONObject message = new JSONObject();
			// message.put("showMessage", this.cf.showMessage(ifr, "navigationNextBtn",
			// "error", "Error utilized all dpn amount"));
			return "error,Error utilized all dpn amount";
		}

//		if (!res.isEmpty() && res.get(0).get(2).toString().equals("OD")
//				&& res.get(0).get(1).toString().equals("0.0")) {
//			JSONObject message = new JSONObject();
//			message.put("showMessage", this.cf.showMessage(ifr, "", "error", "Error utilized all OD amount"));
//			return message.toString();
//		}

		return "";
	}

	public String getLoanAmtOnPageLoad(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String intialLoanAmt = "";
		String query = "SELECT CURRENT_STATUS FROM SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "getLoanAmtOnPageLoad query===>" + query);
		if (!res.isEmpty()) {
			intialLoanAmt = res.get(0).get(0);
		}
		Log.consoleLog(ifr, "intialLoanAmt===>" + intialLoanAmt);
		return intialLoanAmt;
	}

	public String nESLEntry(IFormReference ifr, String value) {
		// String ProcessInstanceId =
		// ifr.getObjGeneralData().getM_strProcessInstanceId();

		try {
			Log.consoleLog(ifr, "CheckNESLWorkflowStatus Started..");
			String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

			String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
					+ ProcessInstanceId + "'";
			Log.consoleLog(ifr, "Query1==>" + Query1);
			List Result1 = ifr.getDataFromDB(Query1);
			String Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);

			if (Integer.parseInt(Count1) == 0) {
				Log.consoleLog(ifr, "NESL Not triggered...");
				return "FAILEDCHECK";
			}

			String Query2 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE " + "PROCESSINSTANCEID='"
					+ ProcessInstanceId + "' AND REQ_STATUS='Y' AND E_SIGN_STATUS ='Success'";
			Log.consoleLog(ifr, "Query2==>" + Query2);
			List Result2 = ifr.getDataFromDB(Query2);
			String Count2 = Result2.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count2==>" + Count2);

			if (Integer.parseInt(Count2) > 0) {
				Log.consoleLog(ifr, "NESL eSign Request is in progress...");
				return "SUCCESSCHECK";
			}

			String Query3 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE " + "PROCESSINSTANCEID='"
					+ ProcessInstanceId + "' AND REQ_STATUS='N' OR E_SIGN_STATUS! ='Success'";
			Log.consoleLog(ifr, "Query3==>" + Query3);
			List Result3 = ifr.getDataFromDB(Query3);
			String Count3 = Result3.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count3==>" + Count3);

			if (Integer.parseInt(Count3) > 0) {
				Log.consoleLog(ifr, "NESL eSign Failed...");
				return "FAILEDCHECKCONTACTBR";
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception==>" + e);
		}
		return "";

//		String reqStatus = "";
//		String esignStatus = "";
//		String query = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
//				+ processInstanceId + "'";
//		List<List<String>> res = ifr.getDataFromDB(query);
//		Log.consoleLog(ifr, "getLoanAmtOnPageLoad query===>" + query);
//		if (!res.isEmpty() && res.get(0).get(0).equalsIgnoreCase("Y")
//				&& res.get(0).get(1).equalsIgnoreCase("Success")) {
//			reqStatus = res.get(0).get(0);
//			esignStatus = res.get(0).get(1);
//		}
//		if (reqStatus.equalsIgnoreCase("Y") && esignStatus.equalsIgnoreCase("Success")) {
//			return "SUCCESSCHECK";
//		}
//		return "FAILEDCHECK";
	}

	public String avaIlMoreLoanOnProdCode(IFormReference ifr, String value) {
		String dpnAvailable = "";
		String odAvailable = "";
		String loanType = "";
		String finalEligiblity = "";
		String odLimit = "";
		String designation = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT * FROM STAFF_VALID_PRODUCTS sv INNER JOIN SLOS_ALL_ACTIVE_PRODUCT sa ON sv.PRODUCTCODE=sa.PRODUCTCODE  WHERE sv.PRODUCTTYPE IN ('GHL','VL','CM','CS') and sa.winame='"
				+ processInstanceId + "'";
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "notEligibleForLoan query===>" + query);
		String queryForDPNOD = "SELECT DPN_AVAILABLE,OD_AVAILABLE,LOAN_TYPE,DESIGNATION FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryForDPNOD query===>" + queryForDPNOD);
		List<List<String>> resForDPNOD = ifr.getDataFromDB(queryForDPNOD);
		Log.consoleLog(ifr, "resForDPNOD===>" + resForDPNOD);
		if (!resForDPNOD.isEmpty()) {
			dpnAvailable = resForDPNOD.get(0).get(0);
			odAvailable = resForDPNOD.get(0).get(1);
			loanType = resForDPNOD.get(0).get(2);
			designation = resForDPNOD.get(0).get(3);
		}
		String queryForEligiblity = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE PID='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryForEligiblity query===>" + queryForEligiblity);
		List<List<String>> resForEligiblity = ifr.getDataFromDB(queryForEligiblity);
		Log.consoleLog(ifr, "resForEligiblity===>" + resForEligiblity);
		if (!resForEligiblity.isEmpty()) {
			finalEligiblity = resForEligiblity.get(0).get(0);
		}
		String queryForODLIMIT = "SELECT OD_LIMIT from LOS_STAFF_SCHEME_LIMIT  WHERE DESIGNATION='" + designation + "'";

		Log.consoleLog(ifr, "queryForODLIMIT query===>" + queryForODLIMIT);
		List<List<String>> resForODLIMIT = ifr.getDataFromDB(queryForODLIMIT);
		Log.consoleLog(ifr, "resForODLIMIT===>" + resForODLIMIT);
		if (!resForODLIMIT.isEmpty()) {
			odLimit = resForODLIMIT.get(0).get(0);
		}
		if (!res.isEmpty() && loanType.equalsIgnoreCase("DPN")
				&& Double.parseDouble(finalEligiblity) < Double.parseDouble(dpnAvailable)) {
			Log.consoleLog(ifr, "Inside AVAILHIGERAMOUNT  Block===>");
			getCurrentStage(ifr, processInstanceId);
			return AccelatorStaffConstant.AVAILHIGHERAMOUNT;
		}
		if (!res.isEmpty() && loanType.contains("OD")
				&& Double.parseDouble(finalEligiblity) < Double.parseDouble(odLimit)
				&& Double.parseDouble(dpnAvailable) != 0.0) {
			Log.consoleLog(ifr, "Inside AVAILHIGERAMOUNT  Block===>");
			getCurrentStage(ifr, processInstanceId);
			return AccelatorStaffConstant.AVAILHIGHERAMOUNT;
		}

		return "";
	}

	public String notEligibleForLoan(IFormReference ifr, String value) {
		double amountreq = 0.0;
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE PID='" + processInstanceId + "'";
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "notEligibleForLoan query===>" + query);
		if (!res.isEmpty()) {
			amountreq = Double.parseDouble(res.get(0).get(0));
			if (amountreq <= 1000) {
				Log.consoleLog(ifr, "Inside notEligibleForLoan  Block===>");
				getCurrentStage(ifr, processInstanceId);
				return AccelatorStaffConstant.NOTELIGIBALEFORLOAN;
			}

		}
		return "";
	}

	public String calculateEMI(IFormReference ifr, String value) {
		String EMIAmount = "";
		String loanAmt = "";
		String tenure = "";
		String roi = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String roiData = "select RAT_INDEX from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
		Log.consoleLog(ifr, "listroiData====>" + listroiData);
		if (!listroiData.isEmpty()) {
			roi = listroiData.get(0).get(0);
		}
		String FrameSection = "AvailOfferHRMS";
		EMICalculator e = new EMICalculator();
		if (value.equalsIgnoreCase("Staff_Branch_Maker")) {
			loanAmt = ifr.getValue("REC_LOAN_AMT").toString();
			Log.consoleLog(ifr, "loanAmt===>" + loanAmt);
			tenure = ifr.getValue("REC_LOAN_TENURE").toString();
			Log.consoleLog(ifr, "tenure===>" + tenure);
			String QueryForRecLoan = "UPDATE SLOS_STAFF_TRN SET RECOMMENDED_LOAN_AMT='" + loanAmt
					+ "', RECOMMENDED_TENURE= '" + tenure + "' WHERE WINAME= '" + processInstanceId + "'";
			Log.consoleLog(ifr, "QueryForRecLoan==> " + QueryForRecLoan);
			ifr.saveDataInDB(QueryForRecLoan);
			EMIAmount = e.getEmiCalculatorInstallment(ifr, processInstanceId, loanAmt, tenure, roi, FrameSection);
			if (EMIAmount.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return "error" + "," + "ERROR,technical glitch";
			}
			ifr.setValue("LOAN_EMI", EMIAmount);
		}
		if (value.equalsIgnoreCase("Staff_Sanction") || value.equalsIgnoreCase("Staff_CO_Sanction")) {
			String grossSal = "";
			String loanAmount = "";
			String tenureMonths = "";
			String nth = "";
			loanAmt = ifr.getValue("APP_LOAN_AMT").toString();
			Log.consoleLog(ifr, "loanAmt===>" + loanAmt);
			tenure = ifr.getValue("APP_LOAN_TENURE").toString();
			Log.consoleLog(ifr, "tenure===>" + tenure);
			String QueryForApproveLoan = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + loanAmt + "', TENURE_MONTHS= '"
					+ tenure + "' WHERE WINAME= '" + processInstanceId + "'";
			Log.consoleLog(ifr, "QueryForApproveLoan==> " + QueryForApproveLoan);
			ifr.saveDataInDB(QueryForApproveLoan);
			EMIAmount = e.getEmiCalculatorInstallment(ifr, processInstanceId, loanAmt, tenure, roi, FrameSection);
			if (EMIAmount.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return "error" + "," + "ERROR,technical glitch";
			}
			ifr.setValue("LOAN_EMI", EMIAmount);
			String query = "SELECT GROSS_SALARY,LOAN_AMOUNT,TENURE_MONTHS, NTH FROM SLOS_STAFF_TRN WHERE WINAME='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "getLoanamt backoffice query===>" + query);
			List<List<String>> res = ifr.getDataFromDB(query);
			Log.consoleLog(ifr, "res===>" + res);
			if (!res.isEmpty()) {
				grossSal = res.get(0).get(0);
				loanAmount = res.get(0).get(1);
				tenureMonths = res.get(0).get(2);
				nth = res.get(0).get(3);
			}
			String prodcode = "";
			String prdCode = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

			List<List<String>> listprdCode = ifr.getDataFromDB(prdCode);
			Log.consoleLog(ifr, "listprdCode====>" + listprdCode);
			if (!listprdCode.isEmpty()) {
				prodcode = listprdCode.get(0).get(0);
			}
//			getAmmortizationHRMS(ifr, processInstanceId, prodcode, Double.parseDouble(nth),
//					Double.parseDouble(grossSal), loanAmount, tenureMonths);
		}
		return EMIAmount;
	}

	public String getHRMSDetailsKnockOff(IFormReference ifr) {
		String irStatus = "";
		String exServiceman = "";
		String dateOfRetire = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT IR_STATUS,EX_SERVICEMAN,DATE_OF_RETIREMENT FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "getHRMSDetailsKnockOff query===>" + query);
		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			irStatus = res.get(0).get(0);
			exServiceman = res.get(0).get(1);
			dateOfRetire = res.get(0).get(2);
		}
		if (Optional.ofNullable(exServiceman).isPresent() && !Optional.ofNullable(exServiceman).isEmpty()
				&& exServiceman.trim().contains("Y")) {
			String dpnAvailable = "";
			String odAvailable = "";
			String loanType = "";
			String finalEligiblity = "";
			String odLimit = "";
			String designation = "";
			String queryForDPNOD = "SELECT DPN_AVAILABLE,OD_AVAILABLE,LOAN_TYPE,DESIGNATION FROM SLOS_STAFF_TRN WHERE WINAME='"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "queryForDPNOD query===>" + queryForDPNOD);
			List<List<String>> resForDPNOD = ifr.getDataFromDB(queryForDPNOD);
			Log.consoleLog(ifr, "resForDPNOD===>" + resForDPNOD);
			if (!resForDPNOD.isEmpty()) {
				dpnAvailable = resForDPNOD.get(0).get(0);
				odAvailable = resForDPNOD.get(0).get(1);
				loanType = resForDPNOD.get(0).get(2);
				designation = resForDPNOD.get(0).get(3);
			}
			String queryForEligiblity = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE PID='"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "queryForEligiblity query===>" + queryForEligiblity);
			List<List<String>> resForEligiblity = ifr.getDataFromDB(queryForEligiblity);
			Log.consoleLog(ifr, "resForEligiblity===>" + resForEligiblity);
			if (!resForEligiblity.isEmpty()) {
				finalEligiblity = resForEligiblity.get(0).get(0);
			}
			String queryForODLIMIT = "SELECT OD_LIMIT from LOS_STAFF_SCHEME_LIMIT  WHERE DESIGNATION='" + designation
					+ "'";

			Log.consoleLog(ifr, "queryForODLIMIT query===>" + queryForODLIMIT);
			List<List<String>> resForODLIMIT = ifr.getDataFromDB(queryForODLIMIT);
			Log.consoleLog(ifr, "resForODLIMIT===>" + resForODLIMIT);
			if (!resForODLIMIT.isEmpty()) {
				odLimit = resForODLIMIT.get(0).get(0);
			}
			if (!res.isEmpty() && loanType.equalsIgnoreCase("DPN")
					&& Double.parseDouble(finalEligiblity) < Double.parseDouble(dpnAvailable)) {
				Log.consoleLog(ifr, "Exservicement Block===>");
				getCurrentStage(ifr, processInstanceId);
				return AccelatorStaffConstant.EX_SERVICESMEN_ERROR_MESSAGE;
			}
			if (!res.isEmpty() && loanType.contains("OD")
					&& Double.parseDouble(finalEligiblity) < Double.parseDouble(odLimit)) {
				Log.consoleLog(ifr, "Exservicement Block===>");
				getCurrentStage(ifr, processInstanceId);
				return AccelatorStaffConstant.EX_SERVICESMEN_ERROR_MESSAGE;
			}

		}
		if (Optional.ofNullable(irStatus).isPresent() && !Optional.ofNullable(irStatus).isEmpty()
				&& irStatus.contains("Y")) {
			Log.consoleLog(ifr, "Inside IR BLOCK===>");
			return AccelatorStaffConstant.IR_STATUS_MESSAGE;

		}
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM-dd-yyyy");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter formatter4 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		boolean res1 = isDateInFormat(dateOfRetire, formatter1, ifr);
		boolean res2 = isDateInFormat(dateOfRetire, formatter2, ifr);
		boolean res3 = isDateInFormat(dateOfRetire, formatter3, ifr);
		boolean res4 = isDateInFormat(dateOfRetire, formatter4, ifr);
		String format = "";
		if (res1) {
			format = "MM-dd-yyyy";
		} else if (res2) {
			format = "yyyy-MM-dd";
		} else if (res3) {
			format = "dd-MM-yyyy";
		} else {
			format = "dd/MM/yyyy";
		}
		Log.consoleLog(ifr, "Local Date===>" + LocalDate.now());
		if (!isEligibleForLoan(dateOfRetire, LocalDate.now(), format, ifr)) {
			Log.consoleLog(ifr, "Retiring Staff Block===>");
			return AccelatorStaffConstant.RETIRING_STAFF_MESSAGE;
		}

		return "SUCCESS";

	}

	public boolean isEligibleForLoan(String retirementDateStr, LocalDate loanApplicationDate, String formate,
			IFormReference ifr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formate);
		LocalDate retirementDate = LocalDate.parse(retirementDateStr, formatter);
		Log.consoleLog(ifr, "retirementDate===>" + retirementDate);
		long monthsUntilRetirement = ChronoUnit.MONTHS.between(loanApplicationDate, retirementDate);
		Log.consoleLog(ifr, "monthsUntilRetirement===>" + monthsUntilRetirement);
		return monthsUntilRetirement > 12;
	}

	private boolean isDateInFormat(String dateStr, DateTimeFormatter formatter, IFormReference ifr) {
		try {
			Log.consoleLog(ifr, "dateStr===>" + dateStr);
			Log.consoleLog(ifr, "formatter===>" + formatter);
			LocalDate.parse(dateStr, formatter);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public void OnLoadGetLoanDetails(IFormReference ifr, String value) {
		Log.consoleLog(ifr, "Inside OnLoadGetLoanDetails method");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "select LOAN_ACC_NUMBER,productname,limit,OUTSTANDING_BALANCE,EMI,roi,tenure from SLOS_ALL_ACTIVE_PRODUCT where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT==>" + query);
		List<List<String>> responseForGrid = ifr.getDataFromDB(query);
		responseForGrid = ifr.getDataFromDB(query);
		String[] Colheading = new String[] { "Loan Account Number", "Product Code - Loan Type",
				"Original Balance/ Limit Amount", "Outstanding balance", "EMI", "Rate of interest",
				"Maturity/Expiry Date" };
		ifr.clearTable("first_staffdetails_table2");
		JSONArray array = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "first_staffdetails_table2===>" + obj);
			String accnumber = row.get(0);
			String first4 = accnumber.trim().substring(0, 4);
			String last4 = accnumber.trim().substring(accnumber.trim().length() - 4);
			String middlePart = "X".repeat(accnumber.trim().length() - 8);
			String accId = first4 + middlePart + last4;
			row.set(0, accId);
			Log.consoleLog(ifr, "first_staffdetails_table2===>" + obj);
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
			if (loanType.contains("DPN")) {
				getLaonDetails(ifr, processInstanceId);
				return "success" + "," + "Disbursement successful";
			} else {
				getLaonDetails(ifr, processInstanceId);
				return "success" + "," + "Create Limit successful";
			}
		}

		return "error" + "," + "Technical Glitch";

	}

	private void getLaonDetails(IFormReference ifr, String processInstanceId) {
		String loanaccno;
		String disbdate;
		String sancAmt;
		String savingAcc;
		String query = "Select a.LOAN_ACCOUNTNO,a.DISB_DATE,a.SANCTION_AMOUNT,b.SALARY_ACC_NUMBER from SLOS_TRN_LOANDETAILS a INNER JOIN SLOS_STAFF_TRN b ON a.PID=b.WINAME  WHERE a.PID  = '"
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

	public String onLoadGetLoanSelType(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String odLimit = "";
		String maturity = "";
		String mobilenumber = "";
		String date2 = "";
		String datet1 = "";
		String designation = ifr.getValue("StaffDesignation").toString();
		String queryForOdLimit = "select OD_LIMIT from LOS_STAFF_SCHEME_LIMIT where DESIGNATION='" + designation + "'";
		List<List<String>> listForOdLimit = ifr.getDataFromDB(queryForOdLimit);
		Log.consoleLog(ifr, "listForOdLimit " + listForOdLimit);
		if (!listForOdLimit.isEmpty()) {
			odLimit = listForOdLimit.get(0).get(0);
			ifr.setValue("Eligible_OD_Amount", odLimit);
		}
		String queryForMaturity = "select TENURE from SLOS_ALL_ACTIVE_PRODUCT where PRODUCTCODE IN ('253','254','1127','1129') AND winame='"
				+ processInstanceId + "'";
		List<List<String>> listForMaturity = ifr.getDataFromDB(queryForMaturity);
		Log.consoleLog(ifr, "listForMaturity " + listForMaturity);
		if (!listForMaturity.isEmpty()) {
			maturity = listForMaturity.get(0).get(0);
			ifr.setValue("Existing_OD_Expiry_Date", maturity);
		}

		String query = "select LOAN_DEDUCTION, LOAN_DESCRIPTION from SLOS_STAFF_LOAN_DEDUCTIONS where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_STAFF_LOAN_DEDUCTIONS==>" + query);
		List<List<String>> responseForGrid = ifr.getDataFromDB(query);
		responseForGrid = ifr.getDataFromDB(query);
		String[] Colheading = new String[] { "External Deductions", "Loan Description" };
		ifr.clearTable("External_Loan_Details");
		JSONArray array = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "External_Loan_Details==>" + obj);
			obj.put(Colheading[0], row.get(0));
			obj.put(Colheading[1], row.get(1));
			// obj.put(Colheading[2], row.get(2));
			array.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + array);

		ifr.addDataToGrid("External_Loan_Details", array);

		String queryMobile = "select MOBILENUMBER from LOS_WIREFERENCE_TABLE  where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "queryMobile==>" + queryMobile);
		List<List<String>> listMobile = ifr.getDataFromDB(queryMobile);
		if (!listMobile.isEmpty()) {
			mobilenumber = listMobile.get(0).get(0);
		}

		String queryResume = "select APPLICATION_NO, DATETI, LOANTYPE, LOANAMOUNT,DATE2, APPLICATION_STATUS from LOS_WIREFERENCE_TABLE  where APPLICATION_STATUS ='In-Progress' AND MOBILENUMBER='"
				+ mobilenumber + "' AND winame !='" + processInstanceId + "'";
		Log.consoleLog(ifr, "queryResume==>" + queryResume);
		List<List<String>> resqueryResume = ifr.getDataFromDB(queryResume);
		if (!resqueryResume.isEmpty()) {
			datet1 = resqueryResume.get(0).get(1);
			Log.consoleLog(ifr, "datet1 " + datet1);
			date2 = resqueryResume.get(0).get(4);
			Log.consoleLog(ifr, "date2 " + date2);
			if (date2.isEmpty() || date2 == null) {
				date2 = datet1;
			}
		}
		responseForGrid = ifr.getDataFromDB(queryResume);
		String[] Columnheading = new String[] { "Reference Number", "Initiated on", "Product Code - Loan Type",
				"Requested Loan / Limit Amount", "Latest Modified On", "Status" };
		ifr.clearTable("Ongoing_App_Details_Grid");
		JSONArray arrayRes = new JSONArray();
		for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Ongoing_App_Details_Grid==>" + obj);
			obj.put(Columnheading[0], row.get(0));
			obj.put(Columnheading[1], row.get(1));
			obj.put(Columnheading[2], row.get(2));
			obj.put(Columnheading[3], row.get(3));
			obj.put(Columnheading[4], date2);
			obj.put(Columnheading[5], row.get(5));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Ongoing_App_Details_Grid", arrayRes);

		String loanType = "";
		String loanAmt = "";
		String queryLoanType = "SELECT LOAN_TYPE,LOAN_AMOUNT FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
				+ "'";

		Log.consoleLog(ifr, "queryLoanType query===>" + queryLoanType);
		List<List<String>> resLoanType = ifr.getDataFromDB(queryLoanType);
		Log.consoleLog(ifr, "resLoanType===>" + resLoanType);
		if (!resLoanType.isEmpty()) {
			loanType = resLoanType.get(0).get(0);
			loanAmt = resLoanType.get(0).get(1);
			// ifr.setValue("FINAL_ELG", loanAmt);
			ifr.setStyle("FINAL_ELG", "disable", "true");
			Log.consoleLog(ifr, "loanType " + loanType);
		}
		return loanType;

	}

	public String mGenDoc(IFormReference ifr, String value) throws ParseException {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		/* Need to implement */

//		if (value.equalsIgnoreCase("Staff_Sanction"))
//		{
//			String response = getLoanAmt(ifr, processInstanceId, loanAmount, tenure, "finalEligiblity");
//			Log.consoleLog(ifr, "responseAfter360CallinFinalEligiblity : " + response);
//			//need to implement
////			if (response != null && !response.isEmpty()) {
////				String queryforEligiblity = "SELECT FINAL_ELIGIBILITY FROM SLOS_STAFF_ELIGIBILITY WHERE PID='"
////						+ processInstanceId + "'";
////				List<List<String>> resultforEligiblity = ifr.getDataFromDB(queryforEligiblity);
////				Log.consoleLog(ifr, "resultforEligiblity===>" + resultforEligiblity);
////				if (!resultforEligiblity.isEmpty()) {
////					String result = resultforEligiblity.get(0).get(0);
////					try {
////						String responseClean = response.trim();
////						String resultClean = result.trim();
////
////						Log.consoleLog(ifr, "Attempting to parse result: '" + result + "'");
////						Log.consoleLog(ifr, "Attempting to parse response: '" + response + "'");
////						double responseElg = Double.parseDouble(responseClean);
////						Log.consoleLog(ifr, "Attempting to parse responseElg: '" + responseElg + "'");
////						double resultElg = Double.parseDouble(resultClean);
////						Log.consoleLog(ifr, "Attempting to parse resultElg: '" + resultElg + "'");
////
////						if (responseElg < resultElg) {
////							Log.consoleLog(ifr, "GOBACK");
////							return "error, Eligibility was recalculated and found to have reduced to Rs " + responseElg + 
////								       ". You may Send back the application to change the Recommendation. Click on Ok to continue.";
////						}
////					} catch (Exception e) {
////						Log.consoleLog(ifr, "Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
////						return "error, unexpected exception";
////					}
////				}
////			}
//
//		}

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

		String queryUpdate = "UPDATE SLOS_STAFF_ELIGIBILITY SET AMOUNT_REQUESTED='" + loanAmt + "' WHERE PID='"
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
//		String eSignStatus="";
//		String esignStatus = "select  ESIGNSTATUS from SLOS_TRN_LOANSUMMARY where winame='"+processInstanceId+"'"; 
//		List<List<String>> resesignStatus = ifr.getDataFromDB(esignStatus);
//		if (!resesignStatus.isEmpty()) {
//			eSignStatus = resesignStatus.get(0).get(0);
//			Log.consoleLog(ifr, "eSignStatus===>" + eSignStatus);
//		}
//		Log.consoleLog(ifr, "eSignStatusEmpty===>" + eSignStatus);
		String activityName = ifr.getActivityName();
		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equals("Staff_Sanction") || activityName.equals("Staff_CO_Sanction")) {
			Log.consoleLog(ifr, "Inside Staff Sanction====>");
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
					+ "' WHERE winame = '" + processInstanceId + "'";

			ifr.saveDataInDB(updateQuerySD);
			String mGenerateDocument = mGenerateDocument(ifr, pid);
			if(mGenerateDocument.contains("error"))
			{
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

			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "    ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + pid
					+ "' AND DOCTYPE IS NOT NULL\r\n" + ") \r\n" + "WHERE rn = 1";
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

			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + pid
					+ "' AND DOCTYPE IS NULL\r\n" + ") \r\n" + "WHERE rn = 1";
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
		String prodData = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

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
		if (!res.isEmpty() && res.get(0).get(0).toString().equals("DPN")) {
			loanType = res.get(0).get(0);
			nth = res.get(0).get(1);
			grossSal = res.get(0).get(2);
			loanAmount = res.get(0).get(3);
			tenure = res.get(0).get(4);
			SBACCNUMBER = res.get(0).get(5);
			String ammortizationHRMS = getAmmortizationHRMS(ifr, processInstanceId, prodcode, Double.parseDouble(nth),
					Double.parseDouble(grossSal), loanAmount, tenure,loanType);
			if(ammortizationHRMS.contains("error"))
			{
				return ammortizationHRMS;
			}
			
			mGenerateDoc(ifr, loanType);

		} else if (!res.isEmpty() && res.get(0).get(0).toString().equals("OD")
				|| !res.isEmpty() && res.get(0).get(0).toString().contains("Renewal")
				|| !res.isEmpty() && res.get(0).get(0).toString().contains("Enhancement")) {
			String returnMessage = "";
			String odUtilized = "";
			Double odUtilize = 0.0;
			Double loanAmt = 0.0;
			String Query = "SELECT LOAN_AMOUNT,TENURE_MONTHS,SALARY_ACC_NUMBER,LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='"
					+ processInstanceId + "'";
			List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
			if (Output3.size() > 0) {
				loanAmount = Output3.get(0).get(0);
				tenure = Output3.get(0).get(1);
				SBACCNUMBER = Output3.get(0).get(2);
				loanType = Output3.get(0).get(3);
				odUtilized = Output3.get(0).get(4);
				if (!odUtilized.isEmpty() && odUtilized != null) {
					odUtilize = Double.parseDouble(odUtilized);
				}
			}

			mGenerateDoc(ifr, loanType);

		}
		return "success";

	}

	public String mNESLClick(IFormReference ifr) {

		Log.consoleLog(ifr, "populateFinalEligibilityDetail : ");
		APIHrmsPreprocessor objPreprocess = new APIHrmsPreprocessor();
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		try {

			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String prodCode = "";
			String prodCodeName = "";
			String nth = "";
			String grossSal = "";
			String loanAmount = "";
			String tenure = "";
			String SBACCNUMBER = "";
			String loanType = "";
			String prodcode = "";
			String prodData = "select prod_code from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%DPN%'";

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
			if (!res.isEmpty() && (res.get(0).get(0).toString().equals("DPN") || res.get(0).get(0).toString().equals("Canara Kavach"))) {
				loanType = res.get(0).get(0);
				nth = res.get(0).get(1);
				grossSal = res.get(0).get(2);
				loanAmount = res.get(0).get(3);
				tenure = res.get(0).get(4);
				SBACCNUMBER = res.get(0).get(5);
				String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
						+ processInstanceId + "'";
				List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
				Log.consoleLog(ifr, "query for esign===>" + queryESign);

				String charDebit = "";
				String CBS_FundTransfer = "";
				String QueryCh = "SELECT CHARGES_DEBITED_FOR  from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> Output4 = cf.mExecuteQuery(ifr, QueryCh, QueryCh);
				if (Output4.size() > 0) {
					charDebit = Output4.get(0).get(0);
				}
				if (charDebit != null && !charDebit.isEmpty()) {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, charDebit, "HRMS", tenure, loanType,
							loanAmount);
				} else {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER, "HRMS", tenure, loanType,
							loanAmount);
				}

				String[] fundTransfer = CBS_FundTransfer.split(":");

				if (fundTransfer[0].equalsIgnoreCase(RLOS_Constants.ERROR) && fundTransfer.length > 1) {
					Log.consoleLog(ifr, "Fund Transfer inside===================>");
					return "error" + "," + fundTransfer[1];
				}
				if (CBS_FundTransfer.equalsIgnoreCase("SUCCESS")) {
					EsignCommonMethods objEsign = new EsignCommonMethods();
					try {
						String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
						if (Integer.parseInt(triggeredStatus) == 0) {
							EsignIntegrationChannel NESL = new EsignIntegrationChannel();

							String returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_DPN", "eStamping",
									loanType);
							Log.errorLog(ifr, "returnMessage:" + returnMessage);

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
						Log.consoleLog(ifr,
								"Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						return "error, Server Error , please try after sometime";
					}
				}

			} else if (!res.isEmpty() && res.get(0).get(0).toString().equals("OD")
					|| !res.isEmpty() && res.get(0).get(0).toString().contains("Renewal")
					|| !res.isEmpty() && res.get(0).get(0).toString().contains("Enhancement")) {
				// prodCode = "254";
				String returnMessage = "";
				Double loanAmt = 0.0;
				Double odUtilized = 0.0;
				String Query = "SELECT LOAN_AMOUNT,TENURE_MONTHS,SALARY_ACC_NUMBER,LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
				if (Output3.size() > 0) {
					loanAmount = Output3.get(0).get(0);
					tenure = Output3.get(0).get(1);
					SBACCNUMBER = Output3.get(0).get(2);
					loanType = Output3.get(0).get(3);
					odUtilized = Double.parseDouble(Output3.get(0).get(4));
				}

				String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
						+ processInstanceId + "'";
				List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
				Log.consoleLog(ifr, "query for esign===>" + queryESign);

				String charDebit = "";
				String CBS_FundTransfer = "";
				String QueryCh = "SELECT CHARGES_DEBITED_FOR  from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> Output4 = cf.mExecuteQuery(ifr, QueryCh, QueryCh);
				if (Output4.size() > 0) {
					charDebit = Output4.get(0).get(0);
				}
				if (charDebit != null && !charDebit.isEmpty()) {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, charDebit, "HRMS", tenure, loanType,
							loanAmount);
				} else {
					CBS_FundTransfer = objPreprocess.execFundTransfer(ifr, SBACCNUMBER, "HRMS", tenure, loanType,
							loanAmount);
				}

				String[] fundTransfer = CBS_FundTransfer.split(":");

				if (fundTransfer[0].equalsIgnoreCase(RLOS_Constants.ERROR) && fundTransfer.length > 1) {
					return "error" + "," + fundTransfer[1];
				}
				if (CBS_FundTransfer.equalsIgnoreCase("SUCCESS")) {
					String currentStepForStaff = "Final Eligibility and Doc";
					String queryForFT = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='" + currentStepForStaff + "'"
							+ "where WINAME='" + processInstanceId + "'";
					Log.consoleLog(ifr, "query for currentStageModification : " + queryForFT);
					ifr.saveDataInDB(queryForFT);

					mGenerateDoc(ifr, loanType);
					EsignCommonMethods objEsign = new EsignCommonMethods();
					try {
						String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
						if (Integer.parseInt(triggeredStatus) == 0) {
//						if ((!res.isEmpty() && !res.get(0).get(0).equalsIgnoreCase("Y")
//								&& !res.get(0).get(1).equalsIgnoreCase("Success")) || res.isEmpty()) {
							EsignIntegrationChannel NESL = new EsignIntegrationChannel();
							if (loanType.equalsIgnoreCase("OD")) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD", "eStamping", loanType);
							} else if (loanType.contains("Renewal")) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_RENEWAL", "eStamping",
										loanType);
							} else if (loanType.contains("Enhancement")
									&& Double.parseDouble(loanAmount) > odUtilized) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_ENHANCEMENT", "eStamping",
										loanType);
							} else if (loanType.contains("Enhancement")
									&& Double.parseDouble(loanAmount) <= odUtilized) {
								returnMessage = NESL.redirectNESLRequest(ifr, "STAFF_LOAN_OD_REDUCTION", "eStamping",
										loanType);
							}
							Log.errorLog(ifr, "returnMessage from HRMSPortal NESL Call:" + returnMessage);

							if (returnMessage.contains(RLOS_Constants.ERROR))
								return pcm.returnErrorHold(ifr);
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
						Log.consoleLog(ifr,
								"Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
						return "error, Server Error , please try after sometime";
					}
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return this.pcm.returnErrorHold(ifr);
		}

		return "";

	}

	public String checkFinalElgiblityComp(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT LOAN_AMOUNT,APPROVED_LOAN_AMT from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
				+ "'";

		List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
		String loanAmount = "";
		String approvedLoanAmt = "";
		String tenure = "";
		if (Output3.size() > 0) {
			loanAmount = Output3.get(0).get(0);
			approvedLoanAmt = Output3.get(0).get(1);
			if (Double.parseDouble(loanAmount) >= Double.parseDouble(approvedLoanAmt)) {
				return "" + true;
			} else {
				return "" + false;
			}

		} else {
			return "error,final or approved loan amount missing";
		}

	}

	public String onLoadGetBranchCodeandName(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String branch = "";
		String branchName = "";
		String Query = "SELECT DISB_BRANCH from SLOS_TRN_LOANDETAILS WHERE PID='" + processInstanceId + "'";
		Log.consoleLog(ifr, "branch code query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			branch = res.get(0).get(0);
		}
		String QueryB = "SELECT BRANCH from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "Branch Name query===>" + QueryB);
		List<List<String>> result = ifr.getDataFromDB(QueryB);
		Log.consoleLog(ifr, "result===>" + result);
		if (!result.isEmpty()) {
			branchName = result.get(0).get(0);
		}
		return branchName + "/" + branch;
	}

	public void onLoadGetRecLoanSelType(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT RECOMMENDED_LOAN_AMT,RECOMMENDED_TENURE,LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "onLoadGetRecLoanSelType query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			Log.consoleLog(ifr, "REC_LOAN_AMT===>" + res.get(0).get(0));
			Log.consoleLog(ifr, "REC_LOAN_TENURE===>" + res.get(0).get(1));
			ifr.setValue("REC_LOAN_AMT", String.valueOf(res.get(0).get(0)));
			ifr.setValue("REC_LOAN_TENURE", String.valueOf(res.get(0).get(1)));
			ifr.setStyle("REC_LOAN_AMT", "disable", "true");
			ifr.setStyle("REC_LOAN_TENURE", "disable", "true");
		}

	}

	public void onLoadGetRecAppLoanSelType(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT RECOMMENDED_LOAN_AMT,RECOMMENDED_TENURE, LOAN_AMOUNT,TENURE_MONTHS from SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "branch code query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.get(0).get(0).isEmpty() && !res.get(0).get(1).isEmpty() && !res.get(0).get(2).isEmpty()
				&& !res.get(0).get(3).isEmpty()) {
			ifr.setValue("REC_LOAN_AMT", res.get(0).get(0));
			ifr.setValue("REC_LOAN_TENURE", res.get(0).get(1));
			ifr.setValue("APP_LOAN_AMT", res.get(0).get(2));
			ifr.setValue("APP_LOAN_TENURE", res.get(0).get(3));
			ifr.setStyle("REC_LOAN_AMT", "disable", "true");
			ifr.setStyle("REC_LOAN_TENURE", "disable", "true");
			ifr.setStyle("APP_LOAN_AMT", "disable", "true");
			ifr.setStyle("APP_LOAN_TENURE", "disable", "true");
		}

	}

	public String sliderCheck(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String tenureMonths = "";
		String tenure = "";
		String Query = "SELECT TENURE_MONTHS from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "branch code query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			tenureMonths = res.get(0).get(0);
			Log.consoleLog(ifr, "tenureMonths:" + tenureMonths);
		}
		String QueryT = "SELECT \"Tenure\" from LOS_PORTAL_SLIDERVALUE WHERE PID='" + processInstanceId + "'";
		Log.consoleLog(ifr, "branch code query===>" + QueryT);
		List<List<String>> result = ifr.getDataFromDB(QueryT);
		Log.consoleLog(ifr, "result===>" + result);
		if (!result.isEmpty()) {
			tenure = result.get(0).get(0);
			Log.consoleLog(ifr, "tenure:" + tenure);
		}

		if (!tenure.trim().equalsIgnoreCase(tenureMonths.trim())) {
			return "error,Slider value not updated. Please click on 'Check EMI' button after modifying the tenure";
		}
		return "";
	}

	public void actionGrid(IFormReference ifr, String value) {
		String previousStage = "";
		String activityname = "";
		String winme = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String q = "SELECT ENTRYDATETIME,ACTIVITYNAME,STATUS,LASTMODIFIEDTIME, "
				+ "ROUND(SYSDATE - ENTRYDATETIME),PREVIOUSSTAGE " + "FROM " + "WFINSTRUMENTTABLE " + "WHERE "
				+ "processinstanceid like '" + winme + "'";
		Log.consoleLog(ifr, "actionGrid==>" + q);
		List<List<String>> responseForGrid = ifr.getDataFromDB(q);
		String[] Colheading = new String[] { "Receiving Date", "Submit Stage Name", "Decision", "Submit Date",
				"Total Days" };
		ifr.clearTable("ALV_Action_History");
		JSONArray array = new JSONArray();
		if (!responseForGrid.isEmpty()) {
			activityname = responseForGrid.get(0).get(1);
			previousStage = responseForGrid.get(0).get(5);
		}
		// activityname=staffsanction and previous stage= staff_branch_ma
		if ((previousStage.trim().contains("Branch") && value.contains(activityname))
				|| (previousStage.trim().contains("Staff_Sanction") && value.contains(activityname))
				|| (previousStage.trim().contains("Staff_CO_Sanction") && value.contains(activityname))) {
			for (List<String> row : responseForGrid) {
				JSONObject obj2 = new JSONObject();
				Log.consoleLog(ifr, "ALV_Action_History===>" + obj2);
				obj2.put(Colheading[0], row.get(0));
				obj2.put(Colheading[1], row.get(1));
				obj2.put(Colheading[2], row.get(2));
				obj2.put(Colheading[3], row.get(3));
				obj2.put(Colheading[4], row.get(4));
				Log.consoleLog(ifr, "actionGrid " + obj2);
				array.add(obj2);
			}
			Log.consoleLog(ifr, "Json Array " + array);

			ifr.addDataToGrid("ALV_Action_History", array);
		}

	}

	public String actionGridSave(IFormReference ifr, String value) {
		// TODO Auto-generated method stub
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
//		String updateQuery = "update WFINSTRUMENTTABLE set status='" + value + "', LASTMODIFIEDTIME= SYSDATE WHERE "
//				+ "    processinstanceid like '" + pid + "'";
//		ifr.saveDataInDB(updateQuery);

		String query = "SELECT APPLICATION_NO from LOS_WIREFERENCE_TABLE where WINAME = '" + pid + "' ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Refrence number:");
		if (!list.isEmpty()) {
			return list.get(0).get(0);
		}
		return "";

	}

	public String OnClickSaveApproveAmt(IFormReference ifr, String value) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String designation = "";
		String loanType = "";
		String oddpnLimit = "";
		String[] loaAmtTenure = value.split(",");
		String query = "SELECT DESIGNATION, LOAN_TYPE from SLOS_STAFF_TRN where WINAME = '" + pid + "' ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get designation:");
		if (!list.isEmpty()) {
			designation = list.get(0).get(0);
			loanType = list.get(0).get(1);
		}
		if (loaAmtTenure.length > 1 && loaAmtTenure[0].contains("Branch")) {
			String loanAmt = loaAmtTenure[1];
			String tenure = loaAmtTenure[2];

			if (designation != null && !designation.isEmpty() && loanType.contains("OD")) {
				String queryForOdLimit = "select OD_LIMIT from LOS_STAFF_SCHEME_LIMIT where DESIGNATION='" + designation
						+ "'";
				List<List<String>> listForOdLimit = ifr.getDataFromDB(queryForOdLimit);
				Log.consoleLog(ifr, "listForOdLimit " + listForOdLimit);
				if (!listForOdLimit.isEmpty()) {
					oddpnLimit = listForOdLimit.get(0).get(0);

				}
			}
			if (designation != null && !designation.isEmpty() && loanType.contains("DPN")) {
				String queryFordpnLimit = "select DPN_LIMIT from LOS_STAFF_SCHEME_LIMIT where DESIGNATION='"
						+ designation + "'";
				List<List<String>> listFordpnLimit = ifr.getDataFromDB(queryFordpnLimit);
				Log.consoleLog(ifr, "listFordpnLimit " + listFordpnLimit);
				if (!listFordpnLimit.isEmpty()) {
					oddpnLimit = listFordpnLimit.get(0).get(0);

				}
			}
			if (Double.parseDouble(loanAmt) > Double.parseDouble(oddpnLimit)) {
				return "error, please enter a recommended loan amount that is less than or equal to " + oddpnLimit
						+ ".";
			}
			String QueryForRecLoan = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + loanAmt + "', TENURE_MONTHS= '"
					+ tenure + "' WHERE WINAME= '" + pid + "'";
			Log.consoleLog(ifr, "QueryForRecLoan==> " + QueryForRecLoan);
			ifr.saveDataInDB(QueryForRecLoan);
		}
//		if (loaAmtTenure.length > 1 && loaAmtTenure[0].contains("Sanction")) {
////			String aPPloanAmt = loaAmtTenure[3];
////			String aPPtenure = loaAmtTenure[4];
////			String amtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(aPPloanAmt));
////			if (designation != null && !designation.isEmpty() && loanType.contains("OD")) {
////				String queryForOdLimit = "select OD_LIMIT from LOS_STAFF_SCHEME_LIMIT where DESIGNATION='" + designation
////						+ "'";
////				List<List<String>> listForOdLimit = ifr.getDataFromDB(queryForOdLimit);
////				Log.consoleLog(ifr, "listForOdLimit " + listForOdLimit);
////				if (!listForOdLimit.isEmpty()) {
////					oddpnLimit = listForOdLimit.get(0).get(0);
////
////				}
////			}
////			if (designation != null && !designation.isEmpty() && loanType.contains("DPN")) {
////				String queryFordpnLimit = "select DPN_LIMIT from LOS_STAFF_SCHEME_LIMIT where DESIGNATION='"
////						+ designation + "'";
////				List<List<String>> listFordpnLimit = ifr.getDataFromDB(queryFordpnLimit);
////				Log.consoleLog(ifr, "listFordpnLimit " + listFordpnLimit);
////				if (!listFordpnLimit.isEmpty()) {
////					oddpnLimit = listFordpnLimit.get(0).get(0);
////
////				}
////			}
////			if (Double.parseDouble(aPPloanAmt) > Double.parseDouble(oddpnLimit)) {
////				String result = "error, please enter approved loan amount that is less than or equal to " + oddpnLimit
////						+ ".";
////				ifr.setStyle("CalculateBtn", "disable", "true");
////				return result;
////
////			}
//			String QueryForApproveLoan = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + aPPloanAmt + "', TENURE_MONTHS= '"
//					+ aPPtenure + "', LOAN_AMOUNT_IN_WORDS='" + amtInWords + "' WHERE WINAME= '" + pid + "'";
//			Log.consoleLog(ifr, "QueryForApproveLoan==> " + QueryForApproveLoan);
		// ifr.saveDataInDB(QueryForApproveLoan);

		// }
		return "success";

	}

	public String isAllDocumentsUploaded(IFormReference ifr) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, "isAllDocumentsUploaded pid :" + pid);
		String Count = "";
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

	public static String checkDocumentOnLoad(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanType = "";
		String query = "SELECT LOAN_TYPE from SLOS_STAFF_TRN where WINAME = '" + processInstanceId + "' ";
		List<List<String>> list = ifr.getDataFromDB(query);
		if (!list.isEmpty()) {
			loanType = list.get(0).get(0);
			// loanType = list.get(0).get(1);
		}
		String Count1 = "";
//       String queryForCount = "SELECT COUNT(DISTINCT NAME)\r\n"
//       		+ "FROM PDBDOCUMENT\r\n"
//       		+ "WHERE DOCUMENTINDEX IN (\r\n"
//       		+ "    SELECT DOCUMENTINDEX\r\n"
//       		+ "    FROM PDBDOCUMENTCONTENT\r\n"
//       		+ "    WHERE PARENTFOLDERINDEX IN (\r\n"
//       		+ "        SELECT FOLDERINDEX\r\n"
//       		+ "        FROM PDBFOLDER\r\n"
//       		+ "        WHERE NAME = '"+processInstanceId+"'\r\n"
//       		+ "    )\r\n"
//       		+ ")";

		if (value.equalsIgnoreCase("NO") || value.equalsIgnoreCase("S") || value.equalsIgnoreCase("P")) {
			String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND DOCTYPE='SANCTION' \r\n" + ")\r\n" + "WHERE rn = 1";

			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
			List Result1 = ifr.getDataFromDB(queryForCount);
			Log.consoleLog(ifr, "Result1==>" + Result1);
			Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);
		} else {
			String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND DOCTYPE IS NULL \r\n" + ")\r\n" + "WHERE rn = 1";

			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
			List Result1 = ifr.getDataFromDB(queryForCount);
			Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);
		}

		if (Integer.parseInt(Count1) == 6 && (loanType.equalsIgnoreCase("DPN") || loanType.equalsIgnoreCase("OD")
				|| loanType.contains("Renewal"))) {

			return getDocCount(ifr, processInstanceId, value);
		} else if (Integer.parseInt(Count1) == 7 && loanType.contains("Enhancement")) {

			return getDocCount(ifr, processInstanceId, value);
		}

		return "FAILED";
	}

	private static String getDocCount(IFormReference ifr, String processInstanceId, String value) {
		if (value.equalsIgnoreCase("NO") || value.equalsIgnoreCase("S") || value.equalsIgnoreCase("P")) {
			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND DOCTYPE='SANCTION' \r\n" + ")\r\n" + "WHERE rn = 1";
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
			ifr.setStyle("Outward_Doc_Details_Grid", "visible", "true");
			if (value.equalsIgnoreCase("P") || value.equalsIgnoreCase("NO")) {
////			String queryforDocGrid1 = "select distinct(a.NAME) as NAME, b.DOCUMENTTYPE,b.MANDATORY,b.UPLOADDATE from SLOS_TRN_BKOF_DOCUMENTS a \r\n"
////					+ "left outer join LOS_DOX_UPLOADDATA b on a.winame=b.pid\r\n" + "where a.winame='"
////					+ processInstanceId + "'";
//			String queryforDocGrid1 = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
//					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
//					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
//					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId + "' AND DOCTYPE='SANCTION' \r\n"
//					+ ")\r\n" + "WHERE rn = 1";
//			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid1);
//			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
//			;
//			Log.consoleLog(ifr, "responseForGrid1==>" + queryforDocGrid1);
//
//			String[] Columnheading2 = new String[] { "Document Name", "Mandatory", "Uploaded Date" };
//			ifr.clearTable("Inward_Doc_Details_Grid");
				JSONArray arrayRes1 = new JSONArray();
//			for (List<String> row : responseForGrid1) {
//				JSONObject obj1 = new JSONObject();
//				Log.consoleLog(ifr, "Inward_Doc_Details_Grid==>" + obj1);
//				obj1.put(Columnheading2[0], row.get(0));
//				obj1.put(Columnheading2[1], "Yes");
//				//obj1.put(Columnheading2[2], row.get(4));
//				// obj.put(Columnheading[2], row.get(2));
//				arrayRes1.add(obj1);
//			}
//				ifr.clearTable("Inward_Doc_Details_Grid");
//				JSONObject nf425Obj = new JSONObject();
//				nf425Obj.put("Document Name", "All_Document_Upload");
//				nf425Obj.put("Mandatory", "Yes");
//				arrayRes1.add(nf425Obj);
//				Log.consoleLog(ifr, "Json Array " + arrayRes1);
//				ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);

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
				Log.consoleLog(ifr, "Json Array " + arrayRes1);
				ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);

			}

		} else {
			String queryforDocGrid = "SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND DOCTYPE IS NULL \r\n" + ")\r\n" + "WHERE rn = 1";
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

	public String availedLoanOnSameDay(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		String query = "SELECT COUNT(*) " + "FROM SLOS_ALL_ACTIVE_PRODUCT " + "WHERE WINAME = '" + processInstanceId
				+ "' " + "AND (LIMITSTARTDATE = '" + currentDate + "' OR DATEOFACCOPEN = '" + currentDate + "')";
		Log.consoleLog(ifr, "query for availedLoanOnSameDay :==>" + query);
		List Result1 = ifr.getDataFromDB(query);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		if (Integer.parseInt(Count1) > 0) {
			Log.consoleLog(ifr, "Count." + Count1);
			return AccelatorStaffConstant.AVAILLOANONTODAY;
		}

		return "0";
	}

	public void OnSaveRecLoanAmtTenure(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String[] loanAmtTen = value.split(":");
		if (loanAmtTen.length > 1) {
			String loanAmt = loanAmtTen[0];
			String tenure = loanAmtTen[1];
			String amtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(loanAmt));
			String QueryamtInWords = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT_IN_WORDS= '" + amtInWords
					+ "' WHERE WINAME= '" + processInstanceId + "'";

			Log.consoleLog(ifr, "QueryamtInWords==>" + QueryamtInWords);

			ifr.saveDataInDB(QueryamtInWords);

			String QueryForRecomLoan = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT='" + loanAmt + "', TENURE_MONTHS= '"
					+ tenure + "' WHERE WINAME= '" + processInstanceId + "'";
			Log.consoleLog(ifr, "QueryForRecomLoan==> " + QueryForRecomLoan);
			ifr.saveDataInDB(QueryForRecomLoan);
		}

	}

	public String UpdateApprovalMatrix(IFormReference ifr, String value) {
		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String roBranch = "";
			String coBranch = "";
			String staffId = ifr.getValue("StaffNumber").toString();
			String availBranch = ifr.getValue("Salary_Credit_Branch").toString();
			int branchNumber = Integer.parseInt(availBranch);
			availBranch = String.format("%05d", branchNumber);

			String queryForBranch = "SELECT RO_CODE,CO_CODE FROM LOS_M_BRANCH WHERE BRANCHCODE='" + availBranch + "'";
			List<List<String>> resultqueryForBranch = ifr.getDataFromDB(queryForBranch);
			Log.consoleLog(ifr, "queryForBranch===>" + queryForBranch);

			if (!resultqueryForBranch.isEmpty()) {
				roBranch = resultqueryForBranch.get(0).get(0);
				coBranch = resultqueryForBranch.get(0).get(1);
			}

//			String updateQueryForwfinstrumenttable = "UPDATE wfinstrumenttable SET var_str1='" + staffId
//					+ "', var_str2='" + availBranch + "', var_str3='" + roBranch + "', var_str4='" + coBranch
//					+ "' WHERE processInstanceId='" + processInstanceId + "'";
//			ifr.saveDataInDB(updateQueryForwfinstrumenttable);
//			Log.consoleLog(ifr, "updateQueryForwfinstrumenttable==> " + updateQueryForwfinstrumenttable);
			ifr.setValue("Q_BORROWERNAME", staffId);
			ifr.setValue("Q_PRODUCTTYPE", availBranch);
			ifr.setValue("Q_BRANCHNAME", roBranch);
			ifr.setValue("Q_LEADSOURCE", coBranch);

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception occurred in processing: " + e.getMessage());
			return "error, server issue, please try after sometimes";
		}
		return "updated approval";
	}

	public void setStatusSuccess(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String esignStatus = "";
		String QueryForLoanSummary = "";
		String queryForEsignstatus = "select ESIGNSTATUS FROM SLOS_TRN_LOANSUMMARY where winame='" + processInstanceId
				+ "'";
		List<List<String>> resEsignstatus = ifr.getDataFromDB(queryForEsignstatus);
		if (!resEsignstatus.isEmpty()) {
			esignStatus = resEsignstatus.get(0).get(0);
		}
		if (esignStatus.equalsIgnoreCase("Yes") && value.equalsIgnoreCase("Yes")) {
			QueryForLoanSummary = "UPDATE SLOS_TRN_LOANSUMMARY SET STATUS = 'ESignDocs' WHERE WINAME= '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "QueryForLoanSummary==> " + QueryForLoanSummary);
			ifr.saveDataInDB(QueryForLoanSummary);
		} else {
			QueryForLoanSummary = "UPDATE SLOS_TRN_LOANSUMMARY SET STATUS = 'InwardDocs' WHERE WINAME= '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "QueryForLoanSummary==> " + QueryForLoanSummary);
			ifr.saveDataInDB(QueryForLoanSummary);
		}
	}

	public String checkStatusCheck(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT STATUS from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		String status = "";
		if (!res.isEmpty()) {
			status = res.get(0).get(0);
			return status;
		}
		return "";

	}

	public String nonNeslStateValidationForStaff(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT ISACTIVE from SLOS_MST_STATEFEECHARGES WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		return null;
	}

	public String checkStampCharges(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT STAMPCHARGES from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		if (res != null && !res.isEmpty()) {
			List<String> firstRow = res.get(0);
			String stampCharge = firstRow.get(0);
			if (stampCharge != null && !stampCharge.isEmpty()) {
				return stampCharge;
			} else {
				return "0";
			}
		}
		return "0";

	}

	public String checkODRenEnhance(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";

		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "result===>" + result);
		if (!result.isEmpty() && (result.get(0).get(0).toString().contains("Renewal")
				|| result.get(0).get(0).toString().contains("Enhancement"))) {

			String Query = "SELECT RENEWENHANCEFLAG from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
			Log.consoleLog(ifr, "query===>" + Query);
			List<List<String>> res = ifr.getDataFromDB(Query);
			if (res != null && !res.isEmpty()) {
				List<String> firstRow = res.get(0);
				String renew = firstRow.get(0);
				if (renew != null && !renew.isEmpty() && renew.equalsIgnoreCase("yes")) {
					return renew;
				} else {
					return "no";
				}
			}
			return "no";

		}
		return "no";
	}

	public String getNesLCherges(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String loanType = "";
		String loanAmount  = "";
		String tenure  = "";
		
		String query = "SELECT LOAN_AMOUNT,TENURE_MONTHS FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + query);
		List<List<String>> res1 = ifr.getDataFromDB(query);
		if(!res1.isEmpty())
		{
			loanAmount = res1.get(0).get(0);
			tenure = res1.get(0).get(1);
		}
		
		String calculatedStampCharges = calculateStampCharges(ifr,tenure,loanAmount);

		String queryForLoanType = "SELECT LOAN_TYPE FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForLoanType);
		List<List<String>> res = ifr.getDataFromDB(queryForLoanType);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			loanType = res.get(0).get(0);
			ifr.setValue("Q_CurrBranchType", loanType);
		}
		if (loanType.contains("Renewal")) {
			String Query = "SELECT DDECHARGES from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
			Log.consoleLog(ifr, "query===>" + Query);
			List<List<String>> result = ifr.getDataFromDB(Query);
			if (result != null && !result.isEmpty()) {
				List<String> firstRow = result.get(0);
				String stampCharge = firstRow.get(0);
				if (stampCharge != null && !stampCharge.isEmpty()) {
					return "stampcharges," + stampCharge;
				} else {
					return "stampcharges," + "0";
				}
			}
		} else {
			String Query = "SELECT OTHERCHARGES from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
			Log.consoleLog(ifr, "query===>" + Query);
			List<List<String>> result = ifr.getDataFromDB(Query);
			if (result != null && !result.isEmpty()) {
				List<String> firstRow = result.get(0);
				String stampCharge = firstRow.get(0);
				if (stampCharge != null && !stampCharge.isEmpty()) {
					return "stampcharges," + stampCharge;
				} else {
					return "stampcharges," + "0";
				}
			}
		}
		return "stampcharges," + "0";
	}
	
	private String calculateStampCharges(IFormReference ifr, String tenure, String loanAmount) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		double calculateMinFinal = Double.parseDouble(loanAmount);
		String branchName = "";
		String fixedFee = "0.0";
		String stampAmt = "0.0";
		String dDECharges = "";
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
		String branchCode = "";
		String state = "";
		String stateCodeForNESL = "";
		String queryForBaranch = "SELECT DISB_BRANCH FROM SLOS_TRN_LOANDETAILS WHERE PID='" + processInstanceId + "'";
		Log.consoleLog(ifr, "autoPopulateAvailOfferData query===>" + queryForBaranch);
		List<List<String>> result = ifr.getDataFromDB(queryForBaranch);
		if (!result.isEmpty()) {
			branchCode = result.get(0).get(0);
		}
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
			return "ERROR,state error";
		}
		String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state
				+ "')) AND ROWNUM=1";
		double stampPercentage = 0.0;
		double stampMincharges = 0.0;
		double stampMaxcharges = 0.0;
		double stampAmtChres = 0.0;
		double stampAmtCharges = 0.0;
		String cessCharges = "";
		String additionalCharges = "";
		double cessCharge = 0.0;
		double additionalCharge = 0.0;
		double calculatedStampAmt = 0.0;
		double cessChargesDB = 0.0;

		Log.consoleLog(ifr, "Query3==>" + Query3);
		List<List<String>> Result3 = ifr.getDataFromDB(Query3);
		Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
		if (!Result3.isEmpty()) {
			stateCodeForNESL = Result3.get(0).get(0);
		}
		try {
			String sqlStateFeeCharge = "Select STAMP_AMT,STAMP_PERCENTAGE,STAMP_MIN_AMT, STAMP_MAX_AMT,AMOUNT_TYPE,STAMP_CESS_PERCENT,ADDITIONAL_STAMP_CHARGES from SLOS_MST_STATEFEECHARGES WHERE STATE_CODE='"
					+ stateCodeForNESL + "' AND " + calculateMinFinal
					+ " BETWEEN LOAN_AMT_MIN AND LOAN_AMT_MAX and schemeid='SL1'";

			List<List<String>> listsqlStateFeeCharge = ifr.getDataFromDB(sqlStateFeeCharge);
			Log.consoleLog(ifr, "listsqlStateFeeCharge==>" + listsqlStateFeeCharge);
			Log.consoleLog(ifr, "sqlStateFeeCharge==>" + sqlStateFeeCharge);
			if (!listsqlStateFeeCharge.isEmpty()) {
				if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Flat")) {
					stampAmt = listsqlStateFeeCharge.get(0).get(0);
					cessCharges = listsqlStateFeeCharge.get(0).get(5);
					// cessCharge = Double.parseDouble(cessCharges);
					additionalCharges = listsqlStateFeeCharge.get(0).get(6);
					// additionalCharge = Double.parseDouble(additionalCharges);
					// stampAmtChres = Double.parseDouble(stampAmt);

					additionalCharge = Double.parseDouble(additionalCharges != null ? additionalCharges : "0");
					cessCharge = Double.parseDouble(cessCharges != null ? cessCharges : "0");
					stampAmtChres = Double.parseDouble(stampAmt != null ? stampAmt : "0");
					calculatedStampAmt = stampAmtChres + (stampAmtChres * cessCharge / 100) + additionalCharge;
					stampAmtCharges = (int) Math.ceil(calculatedStampAmt);
					Log.consoleLog(ifr, "stampAmt : " + stampAmt);
					Log.consoleLog(ifr, "stampAmtCharges from flat charges block : " + stampAmt);
					cessChargesDB = stampAmtChres * cessCharge / 100;
				} else if (listsqlStateFeeCharge.get(0).get(4).equalsIgnoreCase("Percentage")) {
					stampPercentage = Double.parseDouble(listsqlStateFeeCharge.get(0).get(1));
					stampMincharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(2));
					stampMaxcharges = Double.parseDouble(listsqlStateFeeCharge.get(0).get(3));
					cessCharges = listsqlStateFeeCharge.get(0).get(5);
					additionalCharges = listsqlStateFeeCharge.get(0).get(6);
					Log.consoleLog(ifr, "stampPercentage : " + stampPercentage);
					additionalCharge = Double.parseDouble(additionalCharges != null ? additionalCharges : "0");
					cessCharge = Double.parseDouble(cessCharges != null ? cessCharges : "0");
					double stampcharges = calculateMinFinal * (stampPercentage / 100);
					stampAmtChres = (stampcharges > stampMaxcharges) ? stampMaxcharges
							: (stampcharges < stampMincharges) ? stampMincharges : stampcharges;
					calculatedStampAmt = stampAmtChres + (stampAmtChres * cessCharge / 100) + additionalCharge;
					stampAmtCharges = (int) Math.ceil(calculatedStampAmt);
					Log.consoleLog(ifr, "stampAmtCharges from percentage block : " + stampAmtCharges);
					cessChargesDB = stampAmtChres * cessCharge / 100;

				}

			}
			String otherCharges = String.valueOf(fixedfee + dDEfeeCharges + stampAmtCharges);
			String QueryIntrestAmount = "UPDATE SLOS_TRN_LOANSUMMARY SET OTHERCHARGES='" + otherCharges
					+ "',   DDECHARGES='" + dDEfeeCharges + "',STAMPCHARGES='" + String.format("%.2f", stampAmtCharges)
					+ "', BRANCH='" + branchName + "',CESSCHARGES=" + cessChargesDB + "  WHERE WINAME= '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "QueryIntrestAmount==>" + QueryIntrestAmount);
			ifr.saveDataInDB(QueryIntrestAmount);

		} catch (Exception ee) {
			Log.consoleLog(ifr, "Exception==>" + ee);
		}
		return String.valueOf(stampAmtCharges);

		
	}


	public void setUserIDSL(IFormReference ifr, String value) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String activityName = ifr.getActivityName();
		String userName = "";
		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equalsIgnoreCase("Staff_Branch_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_MAKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Branch_Checker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_RO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Saff_CO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_SANCTION_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_CO_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_SANCTION_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}

	}

	public String getFundTransferStatuss(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String noofAPIs = "1";
		String apiNames = "'CBS_FundTransfer'";
		String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE API_NAME IN " + "(" + apiNames
				+ ") AND TRANSACTION_ID='" + PID + "' " + "and API_STATUS IN ('SUCCESS')";

		List<List<String>> CountOutput1 = cf.mExecuteQuery(ifr, Query1, "Checking Count");
		if (CountOutput1.get(0).get(0).equalsIgnoreCase(noofAPIs)) {
			return RLOS_Constants.SUCCESS;
		} else {
			return RLOS_Constants.ERROR;
		}
	}

	public String onLoadGetRecLoanSelTyp(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT RECOMMENDED_LOAN_AMT,RECOMMENDED_TENURE,LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "onLoadGetRecLoanSelType query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty() && res.get(0).get(2).contains("Renewal")) {
			ifr.setValue("REC_LOAN_AMT", String.valueOf(res.get(0).get(3)));
			ifr.setValue("REC_LOAN_TENURE", "24");
			ifr.setStyle("REC_LOAN_AMT", "disable", "true");
			ifr.setStyle("REC_LOAN_TENURE", "disable", "true");
			return "Renewal";
		}
		return "success";

	}

	public void OnSaveRecLoanAmtTenure(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query = "SELECT LOAN_TYPE,OD_UTILIZED from SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "onLoadGetRecLoanSelType query===>" + Query);
		List<List<String>> res = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty() && res.get(0).get(0).contains("Renewal")) {
			String odUtilized = (res.get(0).get(1) == null || res.get(0).get(1).trim().isEmpty()) ? "0"
					: res.get(0).get(1);
			String Querytenure = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT= '" + odUtilized + "' WHERE WINAME= '"
					+ processInstanceId + "'";

			ifr.saveDataInDB(Querytenure);
			Log.consoleLog(ifr, "Querytenure : " + Querytenure);
		}

	}

	public String checkDocToByPas(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String docGen = "";
		String Query = "SELECT GENERATE_DOC from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		List<List<String>> Output3 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output3);
		if (Output3.size() > 0 && Output3.get(0).size() > 0 && !Output3.get(0).get(0).toString().trim().isEmpty()) {
			docGen = Output3.get(0).get(0).toString().trim();
		}
		return docGen;
	}

}