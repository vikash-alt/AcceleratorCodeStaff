/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.fasterxml.jackson.databind.JsonNode;
//import com.newgen.iforms.AccScoremeAPIS.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newgen.dlp.docgen.gen.GenerateDocument;
import com.newgen.dlp.homeLoan.PensionApi;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryHRMSData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.FundTransfer;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.dlp.singleton.CommonFunctionalityCreator;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commonXMLAPI.UploadCreateWI;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.LoanAmtInWords;
import com.newgen.iforms.hrms.OmnidocDownload;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.vl.VLAPIPreprocessor;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;

import jsp.custom;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StaffHLPortalCustomCode {
	StaffHLCommanCustomeCode shlccc = new StaffHLCommanCustomeCode();
	CommonFunctionality cf = new CommonFunctionality();
	CustomerAccountSummary cas = new CustomerAccountSummary();
	PortalCommonMethods pcm = new PortalCommonMethods();
	APICommonMethods cm = new APICommonMethods();
	OmnidocDownload ominDocDownload = new OmnidocDownload();
	// private String string;

	public String validateCoBorrower(IFormReference ifr, String control, String value, String ApplicantType) {
		JSONObject message = new JSONObject();
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "Inside mClickValidateCoBorr");
		String result = "";
		try {

			String[] parts = value.split(",", -1); // -1 ensures even empty values are kept

			// Extract each field safely
			String COMobNo = parts.length > 0 ? parts[0] : "";
			String COCustID = parts.length > 1 ? parts[1] : "";
			String relationWithBorrower = parts.length > 2 ? parts[2] : "";
			String coObligantType = parts.length > 3 ? parts[3] : "";
			String staffId = parts.length > 4 ? parts[4] : "";

			Log.consoleLog(ifr, "MOBILE NUMBER ===> " + COMobNo);
			Log.consoleLog(ifr, "CUSTOMER ID ===> " + COCustID);
			Log.consoleLog(ifr, "Relationship with Borrower ===> " + relationWithBorrower);
			Log.consoleLog(ifr, "Co-Obligant Type ===> " + coObligantType);

			if (!COMobNo.isEmpty() && !COCustID.isEmpty() && !relationWithBorrower.isEmpty()
					&& !coObligantType.isEmpty()) {

				String borrowerCID = "";
				String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
				String query = "select CUSTOMERID from los_trn_customersummary where winame='" + PID + "'";
				Log.consoleLog(ifr, "borrower CID fetch Query:: " + query);
				List<List<String>> borrowerCIDData = ifr.getDataFromDB(query);
				if (!borrowerCIDData.isEmpty()) {
					borrowerCID = borrowerCIDData.get(0).get(0);
					Log.consoleLog(ifr, "Borrower's Customer ID:: sTaff HL:: " + borrowerCID);
				}
				Boolean isBorrowerAndCBSame = borrowerCID.equalsIgnoreCase(COCustID);
				Log.consoleLog(ifr, "Is Borrower and Coborrower are same::" + isBorrowerAndCBSame);
				if (isBorrowerAndCBSame) {
					Log.consoleLog(ifr, "Inside Checking condition :: is Borrower and Coborrower are same");
					message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
							"Borrower cannot be Co-borrower or Guarantor"));
					return message.toString();
				}

				// Duplicate check
				Boolean isDuplicateEntry = shlccc.isUserEntryExist(ifr, COCustID);
				if (isDuplicateEntry) {
					Log.consoleLog(ifr, "before coapplicantcheck ");
					message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
							" User already exists as Applicant/Co-Obligant . Kindly enter a different co-obligant customer number."));
					return message.toString();
				}
				ifr.setStyle("addAdvancedListviewrowNext_CNL_BASIC_INFO", "disable", "false");
				ifr.setStyle("addAdvancedListviewrow_CNL_BASIC_INFO", "disable", "false");
				Log.consoleLog(ifr, "Inside MOBILE NUMBER & CUSTOMER ID Validation");
				HashMap<String, String> map = new HashMap<>();
				map.put("MobileNumber", COMobNo);
				map.put("CustomerId", COCustID);
				map.put("ApplicantType", coObligantType);
				Log.consoleLog(ifr, "StaffHLPortalCustomCode:CoObligantCBSCheck- MobileNumber:" + COMobNo);
				Log.consoleLog(ifr, "StaffHLCustomCode:CoObligantCBSCheck- CustomerId:" + COCustID);

				String responseCO = cas.getCustomerAccountSummarySHL(ifr, map);
				String ErrorMessage = "";
				if (responseCO.contains(RLOS_Constants.ERROR)) {
					ErrorMessage = responseCO.replaceAll(RLOS_Constants.ERROR, "");
					message.put("MSGSTS", "N");
					message.put("SHOWMSG", ErrorMessage + ". Kindly enter valid data");
					return message.toString();
				}
				String responseArr[] = responseCO.split("###");
				String actualResponse = responseArr[0];
				String CASResponse = responseArr[1];
				JSONParser jparser = new JSONParser();
				JSONObject object = (JSONObject) jparser.parse(actualResponse);
				String ext_cust = object.get("CustomerFlag").toString();
				String responseCOMobNumber = object.get("mobile_Number").toString();
				String responseCOCustId = object.get("CustomerID").toString();
				Log.consoleLog(ifr, "Customer Flag =>" + ext_cust);
				Log.consoleLog(ifr, "Mobile number =>" + responseCOMobNumber);
				Log.consoleLog(ifr, "Customer ID =>" + responseCOCustId);
				String fName = object.get("CustomerFirstName").toString();
				String mName = object.get("CustomerMiddleName").toString();
				String lName = object.get("CustomerLastName").toString();
				String fullName = "";
				if ((mName.equalsIgnoreCase("")) || (mName.equalsIgnoreCase("null")) || (mName == null)) {
					fullName = fName + " " + lName;
				} else {
					fullName = fName + " " + mName + " " + lName;
				}
				Log.consoleLog(ifr, "fullName by api::" + fullName);
				if ((!ext_cust.equalsIgnoreCase("Y")) || ext_cust.isEmpty()) {
					Log.consoleLog(ifr, "Co-obligant validation failed CBS");
					ifr.setValue("QNL_HL_PORTAL_CNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo", "");
					ifr.setValue("Portal_CustDet_CustID", "");
					ifr.setValue("P_CB_OD_RELATIONSHIP_BORROWER", "");
					ifr.setValue("Portal_CustDet_Applicant_Type", "");

					message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
							"This Co-Borrower is not a customer of Canara Bank. Kindly contact your branch for further assistance, or add another Co-Borrower who is a customer of Canara Bank."));
					return message.toString();
				} else {
					HashMap<String, String> customerDetails = new HashMap<>();
					customerDetails.put("mobileNumber", COMobNo);
					customerDetails.put("CustomerId", COCustID);
					customerDetails.put("ApplicantType", ApplicantType);
					result = cas.updateCustomerAccountSummaryStaffHL(ifr, customerDetails);
					Log.consoleLog(ifr, "Co-obligant validation is Successful" + result);
					if (result.contains(RLOS_Constants.ERROR)) {
						message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
								"Technical glitch, Try after sometime!"));
						return message.toString();

					} else {
						JSONParser parser = new JSONParser();
						JSONObject pasedCustomerDetails = (JSONObject) parser.parse(result);
						String bredecision = shlccc.getDBValuesFromTempStaffHL(ifr, pasedCustomerDetails,
								coObligantType);
						if (!bredecision.equalsIgnoreCase("APPROVE")) {
							JSONObject re = new JSONObject();
							Log.consoleLog(ifr, "Applicant Knockoff Rejected for Home Loan " + bredecision);
							re.put("showMessage", cf.showMessage(ifr, "portal_login_proceed", "error",
									"Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank"));
							return re.toString();
						}
						String mobileNumber = COMobNo;
						if (!mobileNumber.equalsIgnoreCase("")) {
							mobileNumber = "91" + mobileNumber;
						}
						String extraMappingStatus = cas.updateCustomerDetailsCoBorrowerPortal(ifr, CASResponse,
								mobileNumber, result);
						if (extraMappingStatus.contains("Kindly update the CBS")) {
							JSONObject re = new JSONObject();
							Log.consoleLog(ifr, "Inside Error Condition::" + extraMappingStatus);
							re.put("showMessage",
									cf.showMessage(ifr, "portal_login_proceed", "error", extraMappingStatus));
							return re.toString();
						}
						if (extraMappingStatus.contains(RLOS_Constants.ERROR)) {
							JSONObject re = new JSONObject();
							Log.consoleLog(ifr, "Inside Error Condition::" + extraMappingStatus);
							re.put("showMessage",
									cf.showMessage(ifr, "portal_login_proceed", "error", extraMappingStatus));
							return re.toString();
						}
						if (extraMappingStatus.equalsIgnoreCase("Customer does not exist")) {
							JSONObject re = new JSONObject();
							Log.consoleLog(ifr, "Inside Error Condition::" + extraMappingStatus);
							re.put("showMessage",
									cf.showMessage(ifr, "portal_login_proceed", "error", extraMappingStatus));
							return re.toString();
						}

						Log.consoleLog(ifr, "consent is saved in DB");
						ifr.setStyle("QNL_HL_PORTAL_CNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo", "disable", "true");
						ifr.setStyle("Portal_CustDet_CustID", "disable", "true");
						ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER", "disable", "true");
						ifr.setValue("Portal_CustDet_FullName", fullName);
						ifr.setStyle("Portal_CustDet_FullName", "visible", "true");
						Log.consoleLog(ifr, "P_OD_ValidateCoObligantCB ====> " + fullName);
						ifr.setStyle("P_OD_ValidateCoObligantCB", "visible", "false");
						ifr.setStyle("P_OD_ResetCoObligantHL", "visible", "true");
						ifr.setStyle("P_OD_ResetCoObligantHL", "disable", "false");
						ifr.setStyle("HL_CB_Basic_Details_Sec", "visible", "true");
						ifr.setStyle("HL_CB_Additional_Details_Sec", "visible", "true");
						ifr.setStyle("Portal_CustDet_Applicant_Type", "disable", "true");
						Log.consoleLog(ifr, "P_OD_ValidateCoObligantCB ====> Validate Button is disabled <==== ");
						message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
								"Co-Obligant is Existing to the Canara Bank. Kindly fill the employement details"));

						String mesError = calculateEligiblityCheck(ifr, processInstanceId, staffId, parser);
						if (mesError.contains("error")) {
							return mesError;
						}
						return message.toString();
					}
				}
			} else {
				Log.consoleLog(ifr,
						"Kindly fill Co-Obligant Type, Relationship with Borrower, Mobile Number, Customer ID fields  to Validate Co-Obligant ");

				message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
						"Kindly fill Co-Obligant Type, Relationship with Borrower, Mobile Number, Customer ID fields  to Validate Co-Obligant"));
				return message.toString();
			}
		} catch (ParseException ex) {
			Log.errorLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
			Log.consoleLog(ifr, "Error occured in CoObligantCBSCheck" + ex);
			message.put("showMessage",
					cf.showMessage(ifr, "navigationNextBtn", "error", "Technical glitch, Try after sometime!"));
			return message.toString();
		}
	}

	private double parseDoubleSafe(String val) {
		return (val == null || val.trim().isEmpty()) ? 0.0 : Double.parseDouble(val);
	}

	
	
	public String calculateEligiblityCheck(IFormReference ifr, String processInstanceId, String staffId,
			JSONParser parser) throws ParseException {

		String customerId = "";
		String activityName = ifr.getActivityName();

		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker") || activityName.equalsIgnoreCase("Staff_HL_RO_Maker")
				|| activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {
			customerId = ifr.getValue("CTRID_PD_SRCHVAL").toString();
		} else {
			customerId = ifr.getValue("Portal_CustDet_CustID").toString();
		}

		String s = ifr.getValue("Co_borrower_staff_HL_status").toString();
		Log.consoleLog(ifr, "s===>" + s);
		String staffID = ifr.getValue("Co_borrower_staff_HL_ID").toString();

//		if (!staffID.isEmpty()) {
//			HRMS hrms = new HRMS();
//			String hrmsDetails = hrms.getHrmsDetailsVL(ifr, staffID, false, "Staff Home Loan");
//			if (hrmsDetails.equals(AccelatorStaffConstant.EMPTY_RESPONSE_MESSAGE)
//					|| hrmsDetails.equals(AccelatorStaffConstant.PAN_ERROR_MESSAGE)
//					|| hrmsDetails.equals(AccelatorStaffConstant.STATUS_ACTIVE_MESSAGE)
//					|| hrmsDetails.equals(AccelatorStaffConstant.IR_STATUS_MESSAGE)
//					|| hrmsDetails.equals(AccelatorStaffConstant.RETIRING_STAFF_MESSAGE)) {
//				return pcm.returnErrorcustmessage(ifr, hrmsDetails);
//			}
//			String[] hrmsD = hrmsDetails.split(":");
//			if (hrmsDetails.contains(RLOS_Constants.ERROR) && hrmsD.length > 1) {
//				Log.consoleLog(ifr, "inside error condition hrmsDetails");
//				return "error" + "," + hrmsD[1];
//			}
//			if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
//				return "error" + "," + "technical glitch";
//			}
//
//			if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
//				return "error" + "," + "Technical glitch, Try after sometime!";
//			}
//			JSONObject hrmsObject = (JSONObject) parser.parse(hrmsDetails);
//			String designation = hrmsObject.get("Designation").toString();
//			Log.consoleLog(ifr, "designation==> " + designation);
//			String probation = hrmsObject.get("Probation").toString();
//			Log.consoleLog(ifr, "probation==> " + probation);
//			String totalDeduction = hrmsObject.get("TotalDeduction").toString();
//			Log.consoleLog(ifr, "totalDeduction==> " + totalDeduction);
//			String gross = (hrmsObject.get("Gross") == null || hrmsObject.get("Gross").toString().trim().isEmpty())
//					? "0"
//					: hrmsObject.get("Gross").toString();
//			Log.consoleLog(ifr, "gross==> " + gross);
//			String dateOfJoing = hrmsObject.get("DateOfJoining").toString();
//			Log.consoleLog(ifr, "dateOfJoing==> " + dateOfJoing);
//			String dateOfRetirement = hrmsObject.get("DateOfRetirement").toString();
//			Log.consoleLog(ifr, "dateOfRetirement==> " + dateOfRetirement);
//			String gedr = hrmsObject.get("Gender").toString();
//			Log.consoleLog(ifr, "gedr==> " + gedr);
//			String netSalary = String.valueOf(hrmsObject.get("NetSalary")).trim().equals("null")
//					|| String.valueOf(hrmsObject.get("NetSalary")).trim().isEmpty() ? "0"
//							: String.valueOf(hrmsObject.get("NetSalary"));
//			String totalDed = (hrmsObject.get("TOTAL_DED") == null
//					|| hrmsObject.get("TOTAL_DED").toString().trim().isEmpty()) ? "0"
//							: hrmsObject.get("TOTAL_DED").toString();
//			ifr.setValue("grosspension_hl", gross);
//			ifr.setValue("netpension_hl", netSalary);
//			ifr.setValue("SatutoryDeductionSHL", totalDed);
//
//		}

		String queryForEMICo = " SELECT  SUM(NVL(EMI, 0)) AS emiCob FROM SLOS_ALL_ACTIVE_PRODUCT WHERE c.winame = '"
				+ processInstanceId + "'";

		List<List<String>> listqueryqueryForEMI = ifr.getDataFromDB(queryForEMICo);
		Log.consoleLog(ifr, "queryForEMICo===>" + queryForEMICo);
		double loanDeduction = !listqueryqueryForEMI.isEmpty() ? parseDoubleSafe(listqueryqueryForEMI.get(0).get(0))
				: 0.0;

		long loanDed = (long) loanDeduction;
		// long loanDed =(Long) loanDeduction;
		ifr.setValue("JointLoanDeduction", String.valueOf(loanDed));

		return "";
	}

//	public String calculateEligiblityCheck(IFormReference ifr, String processInstanceId, String staffId,
//			JSONParser parser) throws ParseException {
//		String relationShip = "";
//		String eligiblity = "";
//		String maxLoanAMtCobrower = "";
//		String maxLoanAMtbrower = "";
//		String customerId = "";
//		String limitCob = "";
//		String maxLoanAMtCobrowerRneovation = "";
//		String maxLoanAMtbrowerRenovation = "";
//		String queryForRelationship = "select RELATIONSHIPWITHAPPLICANT,CONSIDERELIGIBILITY from los_nl_basic_info where pid='"
//				+ processInstanceId + "'";
//		List<List<String>> resqueryForRelationship = ifr.getDataFromDB(queryForRelationship);
//		Log.consoleLog(ifr, "RelationShip : " + resqueryForRelationship);
//		if (!resqueryForRelationship.isEmpty()) {
//			relationShip = resqueryForRelationship.get(0).get(0);
//			eligiblity = resqueryForRelationship.get(0).get(1);
//		}
//		String relation = ifr.getValue("P_CB_OD_RELATIONSHIP_BORROWER").toString();
//		String considerForEligiblity = ifr.getValue("HL_CB_ConsiderForEligibility").toString();
//		String activityName = ifr.getActivityName();
//		Log.consoleLog(ifr, "activityName====>" + activityName);
//		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker")
//				|| activityName.equalsIgnoreCase("Staff_HL_RO_Maker")) {
//			customerId = ifr.getValue("CTRID_PD_SRCHVAL").toString();
//		}
//
//		else {
//			customerId = ifr.getValue("Portal_CustDet_CustID").toString();
//		}
//
//		String validProductCodeQuery = "SELECT productcode FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
//		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
//		List<String> productCodes = validProductCodeResult.stream().flatMap(tempMap -> tempMap.stream())
//				.collect(Collectors.toList());
//		Validator valid = new KnockOffValidator("None");
//		//List<String> temp = new ArrayList<>();
//		//temp.addAll(productCodes);
//		
//		String validProductCodeQuery1 = "SELECT productcode,productType FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
//		List<List<String>> validProductCodeResult1 = ifr.getDataFromDB(validProductCodeQuery1);
//		List<String> eHLProductCodes = valid.getFlatList(ifr, validProductCodeResult1, "EHL");
//		List<String> aHLProductCode = valid.getFlatList(ifr, validProductCodeResult1, "AHL");
//		List<String> temp = new ArrayList<>();
//		temp.addAll(eHLProductCodes);
//		temp.addAll(aHLProductCode);
//		
//		String inClause = productCodes.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));
//		
//		String inClauseAHL = aHLProductCode.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));
//
//		String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='"
//				+ processInstanceId + "'";
//		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
//		List Result1 = ifr.getDataFromDB(exStaffQuery);
//		String Count1 = Result1.toString().replace("[", "").replace("]", "");
//		Log.consoleLog(ifr, "Count1==>" + Count1);
//		if (Integer.parseInt(Count1) > 0) {
//
//			String queryForDesignation = "select EX_STAFF_DESIGNATION from slos_staff_home_trn where winame ='"
//					+ processInstanceId + "'";
//			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
//			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
//			String prodCode = "";
//			if (!listqueryForDesignation.isEmpty()) {
//				String inClauseStaff = "666,667,670";
//				String designation2 = listqueryForDesignation.get(0).get(0);
//				if (designation2 != null && !designation2.trim().isEmpty()) {
//					String queryForMaLoanAmout = "select MAX_LOAN_AMT_EX from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
//							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
//							+ ") and probation_tag NOT IN ('Y')";
//					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
//					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
//					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
//					if (!listqueryForMaLoanAmout.isEmpty()) {
//						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
//						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
//						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);
//
//					}
//
//				}
//			}
//
//		} else {
//			String inClauseStaff = "666,667,670";
//			String queryForDesignation = "select DESIGNATION from slos_staff_home_trn where winame ='"
//					+ processInstanceId + "'";
//			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
//			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
//			String prodCode = "";
//			if (!listqueryForDesignation.isEmpty()) {
//				String designation2 = listqueryForDesignation.get(0).get(0);
//				if (designation2 != null && !designation2.trim().isEmpty()) {
//					String queryForMaLoanAmout = "select MAX_LOAN_AMT from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
//							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
//							+ ") and probation_tag NOT IN ('Y')";
//					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
//					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
//					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
//					if (!listqueryForMaLoanAmout.isEmpty()) {
//						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
//						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
//						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);
//
//					}
//
//				}
//			}
//
//		}
//		String s=ifr.getValue("Co_borrower_staff_HL_status").toString();
//		Log.consoleLog(ifr, "spouse s===>" + s);
//		if ((relation.equalsIgnoreCase("S") || relation.equalsIgnoreCase("Spouse"))
//				&& considerForEligiblity.equalsIgnoreCase("Yes") && s.contains("YES")) {
//			// String customerId = "";
//			HRMS hrms = new HRMS();
//			String response = hrms.getHrmsDetailsVL(ifr, staffId, false,"");
//			if (response.contains(RLOS_Constants.ERROR)) {
//				Log.consoleLog(ifr, "Inside Error condition");
//				JSONObject re = new JSONObject();
//				re.put("showMessage",
//						cf.showMessage(ifr, "portal_login_proceed", "error", "Technical glitch, Try after sometime!"));
//				return re.toString();
//
//			}
//			// JSONParser parser = new JSONParser();
//			JSONObject hrmsObject = (JSONObject) parser.parse(response);
//			String designation = hrmsObject.get("Designation").toString();
//			String salaryAcc = hrmsObject.get("SalaryAccount").toString();
//			String probation = hrmsObject.get("Probation").toString();
//			String gross = hrmsObject.get("Gross").toString();
//			String netSalary = hrmsObject.get("NetSalary").toString();
//			String inClauseStaff= "666,667,670";
//			String queryForMaLoanAmout = "select MAX_LOAN_AMT from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
//					+ designation + "' and sub_product_code_cbs in  (" + inClauseStaff + ")";
//			Log.consoleLog(ifr, "coborrower maxAmount===>" + queryForMaLoanAmout);
//			List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
//			Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
//			if (!listqueryForMaLoanAmout.isEmpty()) {
//				maxLoanAMtCobrower = listqueryForMaLoanAmout.get(0).get(0);
//				maxLoanAMtCobrowerRneovation = listqueryForMaLoanAmout.get(1).get(0);
//				Log.consoleLog(ifr, "maxLoanAMtCobrower===>" + maxLoanAMtCobrower);
//
//			}
//
//			Log.consoleLog(ifr, "Spouse===>" + relation);
//			Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
//			String adv360Response = adv360.executeCBSAdvanced360Inquiryv2VL(ifr, processInstanceId, customerId,
//					salaryAcc, designation, probation, temp, false, "CoBorrower");
//
//			if (adv360Response.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//				return "error" + "," + adv360Response;
//			}
//			String limitUsed = "";
//			String limit = "";
//			double reducedVal = 0.0;
//			String limitRenovationUtilized = "";
//			String limitForRenovation = "";
//			double totalAvailableRenovation = 0.0;
//			double totalAvailableRenovate = 0.0;
//			double utilizedRenovation = 0.0;
//			String prodCode = "";
//
//			double calculateLoanUtilzed = calculateLoanUtilzed(ifr);
//			double calculateLoanRenovationUtilized = calculateLoanRenovationUtilized(ifr);
//			String queryForEMI = "SELECT sum(a.limit) FROM SLOS_ALL_ACTIVE_PRODUCT a JOIN SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c ON a.LOAN_ACC_NUMBER = c.LOAN_ACC_NUMBER WHERE a.productcode IN ("
//					+ inClause + ") AND a.winame = '" + processInstanceId + "'";
//
//			Log.consoleLog(ifr, "queryForEMI===>" + queryForEMI);
//			List<List<String>> listqueryForEMI = ifr.getDataFromDB(queryForEMI);
//			Log.consoleLog(ifr, "listqueryForEMI===>" + listqueryForEMI);
//
//			String limitUsedAHL = "";
//			double reducedValAHL = 0.0;
//			String limitAHL = "";
//
//			String queryForEMIAHL = "SELECT sum(a.limit) FROM SLOS_ALL_ACTIVE_PRODUCT a JOIN SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c ON a.LOAN_ACC_NUMBER = c.LOAN_ACC_NUMBER WHERE a.productcode IN ("
//					+ inClauseAHL + ") AND a.winame = '" + processInstanceId + "'";
//
//			Log.consoleLog(ifr, "queryForEMIAHL===>" + queryForEMIAHL);
//			List<List<String>> listqueryForEMIAHL = ifr.getDataFromDB(queryForEMIAHL);
//			Log.consoleLog(ifr, "listqueryForEMIAHL===>" + listqueryForEMIAHL);
//
//			if (!listqueryForEMI.isEmpty()) {
//				limitUsed = listqueryForEMI.get(0).get(0);
//				reducedVal = calculateLoanUtilzed - Double.parseDouble(limitUsed);
//				limit = String.valueOf(reducedVal);
//				Log.consoleLog(ifr, "limit===>" + limit);
//			}
//
//			if (!listqueryForEMIAHL.isEmpty()) {
//				limitUsedAHL = listqueryForEMIAHL.get(0).get(0);
//				reducedValAHL = calculateLoanRenovationUtilized - Double.parseDouble(limitUsedAHL);
//				limitAHL = String.valueOf(reducedValAHL);
//				Log.consoleLog(ifr, "limitAHL===>" + limitAHL);
//			}
//
//			Log.consoleLog(ifr, "Inside limit===>");
//			limitRenovationUtilized = limitAHL;
//			double totallimit = Double.parseDouble(maxLoanAMtCobrower) + Double.parseDouble(maxLoanAMtbrower);
//			Log.consoleLog(ifr, "totallimit===>" + totallimit);
//
//			double totallimitRenovation = Double.parseDouble(maxLoanAMtbrowerRenovation)
//					+ Double.parseDouble(maxLoanAMtCobrowerRneovation);
//			Log.consoleLog(ifr, "totallimitRenovation===>" + totallimitRenovation);
//
//			double utilized = Double.parseDouble(limit);
//			Log.consoleLog(ifr, "utilized===>" + utilized);
//
//			if (limitRenovationUtilized != null && !limitRenovationUtilized.trim().isEmpty()) {
//				utilizedRenovation = Double.parseDouble(limitRenovationUtilized);
//				totalAvailableRenovate = totallimitRenovation - utilizedRenovation;
//			} else {
//				utilizedRenovation = 0.0;
//				totalAvailableRenovate = totallimitRenovation;
//			}
//
//			Log.consoleLog(ifr, "utilized===>" + utilized);
//			double totalAvailable = totallimit - utilized;
//			Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
//
//			totalAvailableRenovation = Math.min(totalAvailable, totalAvailableRenovate);
//			
//			long totalAvailableL = (long) totalAvailable;
//			long utilizedL = (long) utilized;
//			long totallimitL = (long) totallimit;
//			long totalAvailableRenovationL = (long) totalAvailableRenovation;
//			long utilizedRenovationL = (long) utilizedRenovation;
//			long totallimitRenovationL = (long) totallimitRenovation;
//			
//			String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
//			        + totalAvailableL + "',TOTAL_HL_UTIL='" + utilizedL
//			        + "',TOTAL_HL_ELIG='" + totallimitL + "',RENOVATION_ELIG='"
//			        + totallimitRenovationL + "',RENOVATION_UTIL='"
//			        + utilizedRenovationL + "',RENOVATION_AVAIL='"
//			        + totalAvailableRenovationL + "' where winame='" + processInstanceId + "'";
//			
////
////			String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
////					+ String.format("%.2f", totalAvailableL) + "',TOTAL_HL_UTIL='" + String.format("%.2f", utilizedL)
////					+ "',TOTAL_HL_ELIG='" + String.format("%.2f", totallimitL) + "' ,RENOVATION_AVAIL='"
////					+ String.format("%.2f", totalAvailableRenovationL) + "',RENOVATION_UTIL='"
////					+ String.format("%.2f", utilizedRenovationL) + "',RENOVATION_ELIG='"
////					+ String.format("%.2f", totallimitRenovationL) + "' where winame='" + processInstanceId + "'";
//			ifr.saveDataInDB(updateQuery);
//			Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
//
//			
////			else {
////				double limitForRen = 0.0;
////				double limitAvailable = 0.0;
////				double limitAvail = 0.0;
////				double utilized = 0.0;
////				double totalAvailable = 0.0;
////				String queryForLimit = "SELECT \r\n" + "    SUM(NVL(c.limit, 0)) AS limitCob  \r\n" + "FROM \r\n"
////						+ "    SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c\r\n" + "WHERE \r\n" + "    c.productcode IN ("
////						+ inClause + ")\r\n" + "    AND c.winame = '" + processInstanceId + "'\r\n"
////						+ "    AND NOT EXISTS (\r\n" + "        SELECT 1 \r\n"
////						+ "        FROM SLOS_ALL_ACTIVE_PRODUCT a\r\n"
////						+ "        WHERE a.LOAN_ACC_NUMBER = c.LOAN_ACC_NUMBER\r\n" + "    )";
////				Log.consoleLog(ifr, "queryForLimit==>" + queryForLimit);
////				List<List<String>> listqueryqueryForLimit = ifr.getDataFromDB(queryForLimit);
////				Log.consoleLog(ifr, "maxLoanAMtCobrower==>" + maxLoanAMtCobrower);
////				Log.consoleLog(ifr, "maxLoanAMtbrower==>" + maxLoanAMtbrower);
////				double totallimit = Double.parseDouble(maxLoanAMtCobrower) + Double.parseDouble(maxLoanAMtbrower);
////				Log.consoleLog(ifr, "totallimit===>" + totallimit);
////				double totallimitRenovation = Double.parseDouble(maxLoanAMtbrowerRenovation)
////						+ Double.parseDouble(maxLoanAMtCobrowerRneovation);
////				Log.consoleLog(ifr, "totallimitRenovation===>" + totallimitRenovation);
////				limit = listqueryqueryForLimit.get(0).get(0);
////				if (!listqueryqueryForLimit.isEmpty() && (limit != null && !limit.trim().isEmpty())) {
////
////					utilized = Double.parseDouble(limit);
////					Log.consoleLog(ifr, "utilized===>" + utilized);
////					totalAvailable = totallimit - utilized;
////					Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
////
////				} else {
////					utilized = 0.0;
////					Log.consoleLog(ifr, "utilized===>" + utilized);
////					totalAvailable = totallimit;
////					Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
////				}
////
////				String queryForLimitRenovation = "SELECT \r\n" + "    SUM(NVL(c.limit, 0)) AS limitCob  \r\n"
////						+ "FROM \r\n" + "    SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c\r\n" + "WHERE \r\n"
////						+ "    c.productcode IN ('670')\r\n" + "    AND c.winame = '" + processInstanceId + "'\r\n"
////						+ "    AND NOT EXISTS (\r\n" + "        SELECT 1 \r\n"
////						+ "        FROM SLOS_ALL_ACTIVE_PRODUCT a\r\n"
////						+ "        WHERE a.LOAN_ACC_NUMBER = c.LOAN_ACC_NUMBER\r\n" + "    )";
////				Log.consoleLog(ifr, "queryForLimit==>" + queryForLimit);
////				List<List<String>> listqueryqueryForLimitRenovation = ifr.getDataFromDB(queryForLimitRenovation);
////				limitForRenovation = listqueryqueryForLimitRenovation.get(0).get(0);
////				if (!listqueryqueryForLimitRenovation.isEmpty()
////						&& (limitForRenovation != null && !limitForRenovation.trim().isEmpty())) {
////
////					limitForRen = Double.parseDouble(limitForRenovation);
////					limitAvail = totallimitRenovation - limitForRen;
////				} else {
////					limitForRen = 0.0;
////					limitAvail = totallimitRenovation;
////				}
////
////				limitAvailable = Math.min(totalAvailable, limitAvail);
////
////				String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
////						+ String.format("%.2f", totalAvailable) + "',TOTAL_HL_UTIL='" + String.format("%.2f", utilized)
////						+ "',TOTAL_HL_ELIG='" + String.format("%.2f", totallimit) + "',RENOVATION_ELIG='"
////						+ String.format("%.2f", totallimitRenovation) + "',RENOVATION_UTIL='"
////						+ String.format("%.2f", limitForRen) + "',RENOVATION_AVAIL='"
////						+ String.format("%.2f", limitAvailable) + "' where winame='" + processInstanceId + "'";
////				ifr.saveDataInDB(updateQuery);
////				Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
////
////
////			}
//
//
//		}
//
//		boolean containsSAndYes = resqueryForRelationship.stream()
//				.anyMatch(row -> "S".equalsIgnoreCase(row.get(0)) && "Yes".equalsIgnoreCase(row.get(1)));
//
//		if (!containsSAndYes) {
//			// else {
//			String limit = "";
//			double totallimit = 0.0;
//			double utilized = 0.0;
//			double totalAvailable = 0.0;
//			
//			double calculateLoanUtilizedBorrower = calculateLoanUtilizedBorrower(ifr);
//			totallimit = Double.parseDouble(maxLoanAMtbrower);
//			Log.consoleLog(ifr, "totallimit===>" + totallimit);
//			utilized= calculateLoanUtilizedBorrower;
//			Log.consoleLog(ifr, "utilized===>" + utilized);
//			totalAvailable = totallimit - utilized;
//			Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
//			
////			String queryForLimit = " SELECT \r\n" + " SUM(NVL(c.limit, 0)) AS limitCob \r\n" + "  FROM \r\n"
////					+ " SLOS_ALL_ACTIVE_PRODUCT c\r\n" + " WHERE \r\n" + " c.productcode IN (" + inClause + ")\r\n"
////					+ " AND c.winame = '" + processInstanceId + "'";
////			Log.consoleLog(ifr, "queryForLimit==>" + queryForLimit);
////			List<List<String>> listqueryqueryForLimit = ifr.getDataFromDB(queryForLimit);
////			limit = listqueryqueryForLimit.get(0).get(0);
////			if (!listqueryqueryForLimit.isEmpty() && (limit != null && !limit.trim().isEmpty())) {
////				totallimit = Double.parseDouble(maxLoanAMtbrower);
////				Log.consoleLog(ifr, "totallimit===>" + totallimit);
////				utilized = Double.parseDouble(limit);
////				Log.consoleLog(ifr, "utilized===>" + utilized);
////				totalAvailable = totallimit - utilized;
////				Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
////			} else {
////				totallimit = Double.parseDouble(maxLoanAMtbrower);
////				Log.consoleLog(ifr, "totallimit===>" + totallimit);
////				utilized = 0.0;
////				Log.consoleLog(ifr, "utilized===>" + utilized);
////				totalAvailable = totallimit;
////				Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
////			}
//
//			if (Integer.parseInt(Count1) == 0) {
//				String limitForRenovation = "";
//				double limitForRen = 0.0;
//				double limitAvailable = 0.0;
//				double limitAvail = 0.0;
//				String queryForLimitRenovation = " SELECT \r\n" + " SUM(NVL(c.limit, 0)) AS limitCob \r\n"
//						+ "  FROM \r\n" + " SLOS_ALL_ACTIVE_PRODUCT c\r\n" + " WHERE \r\n"
//						+ " c.productcode IN ('"+inClauseAHL+"')\r\n" + " AND c.winame = '" + processInstanceId + "'";
//				//Log.consoleLog(ifr, "queryForLimit==>" + queryForLimit);
//				List<List<String>> listqueryqueryForLimitRenovation = ifr.getDataFromDB(queryForLimitRenovation);
//				double totallimitRenovation = Double.parseDouble(maxLoanAMtbrowerRenovation);
//				Log.consoleLog(ifr, "totallimitRenovation===>" + totallimitRenovation);
//				limitForRenovation = listqueryqueryForLimitRenovation.get(0).get(0);
//				if (!listqueryqueryForLimitRenovation.isEmpty()
//						&& (limitForRenovation != null && !limitForRenovation.trim().isEmpty())) {
//					// limitForRenovation = listqueryqueryForLimitRenovation.get(0).get(0);
//					limitForRen = Double.parseDouble(limitForRenovation);
//					limitAvail = totallimitRenovation - limitForRen;
//					limitAvailable = Math.min(totalAvailable, limitAvail);
//				} else {
//					limitForRen = 0.0;
//					limitAvailable = totallimitRenovation;
//				}
//				
//				long totalAvailableL = (long) totalAvailable;
//				long utilizedL = (long) utilized;
//				long totallimitL = (long) totallimit;
//				long totalAvailableRenovationL = (long) limitAvailable;
//				long utilizedRenovationL = (long) limitForRen;
//				long totallimitRenovationL = (long) totallimitRenovation;
//
////				String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
////						+ String.format("%.2f", totalAvailableL) + "',TOTAL_HL_UTIL='" + String.format("%.2f", utilizedL)
////						+ "',TOTAL_HL_ELIG='" + String.format("%.2f", totallimitL) + "',RENOVATION_ELIG='"
////						+ String.format("%.2f", totallimitRenovationL) + "',RENOVATION_UTIL='"
////						+ String.format("%.2f", utilizedRenovationL) + "',RENOVATION_AVAIL='"
////						+ String.format("%.2f", totalAvailableRenovationL) + "' where winame='" + processInstanceId + "'";
////				ifr.saveDataInDB(updateQuery);
//
//				String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
//				        + totalAvailableL + "',TOTAL_HL_UTIL='" + utilizedL
//				        + "',TOTAL_HL_ELIG='" + totallimitL + "',RENOVATION_ELIG='"
//				        + totallimitRenovationL + "',RENOVATION_UTIL='"
//				        + utilizedRenovationL + "',RENOVATION_AVAIL='"
//				        + totalAvailableRenovationL + "' where winame='" + processInstanceId + "'";
//
//				ifr.saveDataInDB(updateQuery);
//				
//				Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
//			} else {
//				long totalAvailableL = (long) totalAvailable;
//				long utilizedL = (long) utilized;
//				long totallimitL = (long) totallimit;
//				
//				String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
//				        + totalAvailableL + "',TOTAL_HL_UTIL='" + utilizedL
//				        + "',TOTAL_HL_ELIG='" + totallimitL + "'  where winame='" + processInstanceId + "'";
//
////				String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='"
////						+ String.format("%.2f", totalAvailable) + "',TOTAL_HL_UTIL='" + String.format("%.2f", utilized)
////						+ "',TOTAL_HL_ELIG='" + String.format("%.2f", totallimit) + "' where winame='"
////						+ processInstanceId + "'";
//				ifr.saveDataInDB(updateQuery);
//				Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
//			}
//
//			// }
//		}
//		return "";
//	}

	private double calculateLoanRenovationUtilized(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside Query===>");
		Validator valid = new KnockOffValidator("None");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "Inside Query===>" + processInstanceId);
		String jsonString = "";
		Double totaUtilization = 0.0;
		String validProductCodeQuery = "SELECT productcode,producttype  FROM STAFF_HL_VALID_PRODUCT_CODE where isactive='Y'";
		Log.consoleLog(ifr, "Inside Query===>" + validProductCodeQuery);
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
		Log.consoleLog(ifr, "Inside Query===>" + validProductCodeResult);
		// List<List<String>> validProductCodeResult =
		// ifr.getDataFromDB(validProductCodeQuery);
		List<String> eHLProductCodes = valid.getFlatList(ifr, validProductCodeResult, "EHL");
		Log.consoleLog(ifr, "Inside Query===>" + eHLProductCodes);
		List<String> aHLProductCode = valid.getFlatList(ifr, validProductCodeResult, "AHL");
		Log.consoleLog(ifr, "Inside Query===>" + aHLProductCode);
		List<String> temp = new ArrayList<>();
		temp.addAll(eHLProductCodes);
		temp.addAll(aHLProductCode);

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
			for (List<String> res1 : validProductCodeResult) {
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
			Log.consoleLog(ifr, "jsonObject for Staff Home Loan" + jsonObject);

			for (String productCode : aHLProductCode) {
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

		String jsonStringCO = "";
		Double totaUtilizationCo = 0.0;
		Query = "SELECT PRODUCTCODEUSEDCOBORROWER  from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		List<List<String>> Output4 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output4);
		if (Output4.size() > 0 && Output4.get(0).size() > 0 && !Output4.get(0).get(0).toString().trim().isEmpty()) {
			Log.consoleLog(ifr, "valid in if condition==>");
			jsonStringCO = Output4.get(0).get(0);
			Log.consoleLog(ifr, "valid in if condition==>" + jsonStringCO);
		} else {
			JSONObject jsonObjInp = new JSONObject();
			Log.consoleLog(ifr, "valid==>" + jsonObjInp);
			for (List<String> res1 : validProductCodeResult) {
				Log.consoleLog(ifr, "value added==>" + res1.get(0));
				jsonObjInp.put(res1.get(0), 0);
			}
			jsonStringCO = jsonObjInp.toString();
			Log.consoleLog(ifr, "jsonString valid product code result==> " + jsonStringCO);
			// return "error, technical glitch";
		}
		// JSONParser pars = new JSONParser();

		try {
			JSONObject jsonObject = (JSONObject) pars.parse(jsonStringCO);
			Log.consoleLog(ifr, "jsonObject for Staff Home Loan" + jsonObject);
			for (String productCode : aHLProductCode) {
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
		Double totaUtilizationFinal = totaUtilizationCo + totaUtilization;
		return totaUtilizationFinal;

	}

	private double calculateLoanUtilizedBorrower(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String jsonString = "";
		Double totaUtilization = 0.0;
		String validProductCodeQuery = "SELECT productcode,productType FROM STAFF_HL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
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
			for (List<String> res1 : validProductCodeResult) {
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
			Log.consoleLog(ifr, "jsonObject for Home Loan Borrower" + jsonObject);
			for (Object value : jsonObject.values()) {
				if (value instanceof Number) {
					totaUtilization += ((Number) value).doubleValue();
					Log.consoleLog(ifr, "inside totaUtilization " + totaUtilization);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totaUtilization;

	}

	private double calculateLoanUtilzed(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String jsonString = "";
		Double totaUtilization = 0.0;
		String validProductCodeQuery = "SELECT productcode FROM STAFF_HL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
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
			for (List<String> res1 : validProductCodeResult) {
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
			Log.consoleLog(ifr, "jsonObject for Staff Home Loan" + jsonObject);
			for (Object value : jsonObject.values()) {
				if (value instanceof Number) {
					totaUtilization += ((Number) value).doubleValue();
					Log.consoleLog(ifr, "inside totaUtilization " + totaUtilization);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String jsonStringCO = "";
		Double totaUtilizationCo = 0.0;
		Query = "SELECT PRODUCTCODEUSEDCOBORROWER  from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId + "'";
		List<List<String>> Output4 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output4);
		if (Output4.size() > 0 && Output4.get(0).size() > 0 && !Output4.get(0).get(0).toString().trim().isEmpty()) {
			Log.consoleLog(ifr, "valid in if condition==>");
			jsonStringCO = Output4.get(0).get(0);
			Log.consoleLog(ifr, "valid in if condition==>" + jsonStringCO);
		} else {
			JSONObject jsonObjInp = new JSONObject();
			Log.consoleLog(ifr, "valid==>" + jsonObjInp);
			for (List<String> res1 : validProductCodeResult) {
				Log.consoleLog(ifr, "value added==>" + res1.get(0));
				jsonObjInp.put(res1.get(0), 0);
			}
			jsonStringCO = jsonObjInp.toString();
			Log.consoleLog(ifr, "jsonString valid product code result==> " + jsonStringCO);
			// return "error, technical glitch";
		}
		// JSONParser pars = new JSONParser();

		try {
			JSONObject jsonObject = (JSONObject) pars.parse(jsonStringCO);
			Log.consoleLog(ifr, "jsonObject for Staff Home Loan" + jsonObject);
			// Double totaUtilizationCo = 0.0;
			for (Object value : jsonObject.values()) {
				totaUtilization += ((Number) value).doubleValue();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double totaUtilizationFinal = totaUtilizationCo + totaUtilization;
		return totaUtilizationFinal;

	}

	public String staffDetailsHL(IFormReference ifr, String empId) throws ParseException {
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
		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE,GENERATE_DOC) SELECT '" + processInstanceId + "', '"
				+ strCurDateTime
				+ "','YES' FROM dual WHERE NOT EXISTS (    SELECT 1 FROM SLOS_TRN_LOANSUMMARY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query1);
		cf.mExecuteQuery(ifr, Query1, "insert into SLOS_STAFF_VL_ELIGIBILITY");

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
	

		CommonFunctionalityCreator.getInstance().mExecuteQuery(ifr, Query1, "mImpOnClickFianlEligibility ");
		String customerId = "";
		String gender = "";
		String fname = "";
		String lname = "";
		String dateOnly = "";
		String age = "";
		String mname = "";
		String mobilenum = "";
		String query = "select CUSTOMERID,customersex,dateofbirth,FLOOR(MONTHS_BETWEEN(SYSDATE, dateofbirth) / 12),customerfirstname,customermiddlename,customerlastname,mobilenumber from los_trn_customersummary where  WINAME='"
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
			age = list.get(0).get(3).toString();
			ifr.setValue("AGE_VL", age);
			// this code should be reverted when giving build to UAT
			LocalDateTime dateTime = LocalDateTime.parse(list.get(0).get(2), inputFormatter);
			dateOnly = dateTime.toLocalDate().toString();
			ifr.setValue("DATEOFBIRTH", dateOnly);
			Log.consoleLog(ifr, "***********");
			fname = list.get(0).get(4);
			mname = list.get(0).get(5);
			lname = list.get(0).get(6);
			mobilenum = list.get(0).get(7);
			// ifr.setValue("DATEOFBIRTH", list.get(0).get(2));
		}
		Log.consoleLog(ifr, "***********");
		String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age='" + list.get(0).get(3) + "' " + "where WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
		ifr.saveDataInDB(ageQuery);

		HRMS hrms = new HRMS();
		String hrmsDetails = hrms.getHrmsDetailsVL(ifr, empId, true, "Staff Home Loan");
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

		if (hrmsDetails.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		JSONObject hrmsObject = (JSONObject) parser.parse(hrmsDetails);
		String designation = hrmsObject.get("Designation").toString();
		Log.consoleLog(ifr, "designation==> " + designation);
		String probation = hrmsObject.get("Probation").toString();
		Log.consoleLog(ifr, "probation==> " + probation);
		String totalDeduction = hrmsObject.get("TotalDeduction").toString();
		Log.consoleLog(ifr, "totalDeduction==> " + totalDeduction);
		String gross = hrmsObject.get("Gross").toString();
		Log.consoleLog(ifr, "gross==> " + gross);
		String dateOfJoing = hrmsObject.get("DateOfJoining").toString();
		Log.consoleLog(ifr, "dateOfJoing==> " + dateOfJoing);
		String dateOfRetirement = hrmsObject.get("DateOfRetirement").toString();
		Log.consoleLog(ifr, "dateOfRetirement==> " + dateOfRetirement);
		String gedr = hrmsObject.get("Gender").toString();
		Log.consoleLog(ifr, "gedr==> " + gedr);
		String probationQuery = "SELECT probation from slos_staff_home_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (queryRes.isEmpty()) {
			String insert = "INSERT INTO slos_staff_home_trn(winame,probation,statutory_deductions) values('"
					+ processInstanceId + "','" + probation + "','" + totalDeduction + "')";
			ifr.saveDataInDB(insert);
			Log.consoleLog(ifr, "statutory_deductions==>" + insert);
		} else {
			String updateQuery = "UPDATE slos_staff_home_trn SET probation='" + probation + "',statutory_deductions='"
					+ totalDeduction + "' where winame='" + processInstanceId + "'";
			ifr.saveDataInDB(updateQuery);
			Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
		}

//		String updateQueryHL = "UPDATE slos_staff_home_trn SET JOINING_DATE='" + dateOfJoing + "',RETIREMENT_DATE='"
//				+ dateOfRetirement + "',AGE='"+age+"',GENDER='"+gedr+"',DATE_OF_BIRTH='"+dateOnly+"' where winame='" + processInstanceId + "'";

		String updateQueryHL = "UPDATE slos_staff_home_trn SET JOINING_DATE=TO_DATE('" + dateOfJoing
				+ "','YYYY-MM-DD'), RETIREMENT_DATE=TO_DATE('" + dateOfRetirement + "','YYYY-MM-DD'), AGE='" + age
				+ "', GENDER='" + gender + "', DATE_OF_BIRTH=TO_DATE('" + dateOnly + "','YYYY-MM-DD') WHERE winame='"
				+ processInstanceId + "'" + "";

		ifr.saveDataInDB(updateQueryHL);
		Log.consoleLog(ifr, "updateQueryHL==>" + updateQueryHL);

		String queryForMaLoanAmout = "select count(*) from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='" + designation
				+ "' and probation_tag IN ('Y')";
		Log.consoleLog(ifr, "Count query===>" + queryForMaLoanAmout);
		List Result1 = ifr.getDataFromDB(queryForMaLoanAmout);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);

		if (Integer.parseInt(Count1) > 0) {
			Log.consoleLog(ifr, "Not Eligible.");
			return "error, Staff is under probationary period, hence is not eligible for Staff Home Loan ";
		}

		String warning = hrmsObject.get("Warning").toString();
		String salaryAcc = hrmsObject.get("SalaryAccount").toString();
		String validProductCodeQuery = "SELECT productcode,producttype FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
		List<String> eHLProductCodes = valid.getFlatList(ifr, validProductCodeResult, "EHL");
		List<String> aHLProductCode = valid.getFlatList(ifr, validProductCodeResult, "AHL");
		List<String> temp = new ArrayList<>();
		temp.addAll(eHLProductCodes);
		temp.addAll(aHLProductCode);

		Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
		String adv360Response = adv360.executeCBSAdvanced360Inquiryv2VL(ifr, processInstanceId, customerId, salaryAcc,
				designation, probation, temp, false, "Barrower");

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

		DecimalFormat df = new DecimalFormat("0.00");
		ifr.setValue("Net_Salary", df.format(netSalary));

		String queryForIRNum = "SELECT IR_REFERANCH_ID from slos_staff_home_trn where winame='" + processInstanceId
				+ "'";
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

//		String mobilenumber = "";
//		String mobileQuery = "select mobileNumber from los_wireference_table WHERE WINAME = '" + processInstanceId
//				+ "'";
//		List<List<String>> resmobileQuery = ifr.getDataFromDB(mobileQuery);
//		if (!resmobileQuery.isEmpty()) {
//			mobilenumber = resmobileQuery.get(0).get(0);
//		}

		String deleteQuery = "delete from LOS_NL_BASIC_INFO where PID='" + processInstanceId
				+ "' and APPLICANTTYPE='B'";
		Log.consoleLog(ifr, "deleteQuery ==>" + deleteQuery);
		ifr.saveDataInDB(deleteQuery);
		String basicDetailInsertQuery = "INSERT INTO LOS_NL_BASIC_INFO (PID, APPLICANTTYPE, ENTITYTYPE, EXISTINGCUSTOMER, CUSTOMERID, F_KEY, INSERTIONORDERID, FULLNAME, STAFFMEMBER, CONSIDERELIGIBILITY, STATUS,MOBILENUMBER) VALUES ('"
				+ processInstanceId + "', 'B', 'I', 'Yes', '" + customerId
				+ "', (SELECT NVL(MAX(F_KEY),0) + 1 FROM LOS_NL_BASIC_INFO), (SELECT NVL(MAX(INSERTIONORDERID),0) - 50 FROM LOS_NL_BASIC_INFO), '"
				+ fname + " " + mname + " " + lname + "', 'Y', 'Yes', 'IN-PROGRESS','" + mobilenum + "')";
		// List<List<String>> resbasicDetailInsertQuery =
		// ifr.getDataFromDB(basicDetailInsertQuery);
		Log.consoleLog(ifr, "basicDetailInsertQuery ==>" + basicDetailInsertQuery);
		int insertResult = ifr.saveDataInDB(basicDetailInsertQuery);
		if (insertResult <= 0) {
			Log.consoleLog(ifr, "Insert failed. Stopping the flow.");
			return "error" + "," + "Insert into LOS_NL_BASIC_INFO failed, Please try to fetch again";
		}

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

	public String HlStaffEligibility(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String designation = "";
		String probation = "";
		Log.consoleLog(ifr, "Testing in HlStaffEligibility");

		try {
			String ex_staff_id = "";
			String exStaffQuery = "select count(*) from slos_staff_home_trn where EX_STAFF_ID IS NULL AND winame='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "exStaffQuery===>" + exStaffQuery);
			List Result1 = ifr.getDataFromDB(exStaffQuery);
			String Count1 = Result1.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count1==>" + Count1);

//			String officerQuery = "select * from staff_hl_prod_des_matrix m inner join slos_staff_home_trn s on m.designation= s.designation where STAFF_SCALE like '%Scale%' and winame='"
//					+ processInstanceId + "'";
//			List<List<String>> officerQueryRes = ifr.getDataFromDB(officerQuery);
//			Log.consoleLog(ifr, "officerQuery===>" + officerQuery);
//			
//			
//			if (!officerQueryRes.isEmpty()) {
			String prodpurp = ifr.getValue("P_HL_LD_Product").toString();
			if (prodpurp.contains("Officer") || prodpurp.contains("Workmen")) {
				ifr.clearCombo("P_HL_LD_Purpose");
				String purpose = "select purposename from slos_home_purpose";
				List<List<String>> purposeRes = ifr.getDataFromDB(purpose);
				Log.consoleLog(ifr, "purposeRes===>" + purposeRes);
				Log.consoleLog(ifr, "purpose===>" + purpose);
				for (List<String> row : purposeRes) {
					if (row != null && !row.isEmpty()) {
						String dbPurpose = row.get(0); // first column: purpose
						ifr.addItemInCombo("P_HL_LD_Purpose", dbPurpose, dbPurpose);
					}
				}
			}
			if (prodpurp.contains("Renovation")) {
				ifr.clearCombo("P_HL_LD_Purpose");
				String purpose = "select purposename from slos_home_purpose where productcode='670'";
				List<List<String>> purposeRes = ifr.getDataFromDB(purpose);
				Log.consoleLog(ifr, "purposeRes===>" + purposeRes);
				Log.consoleLog(ifr, "purpose===>" + purpose);
				for (List<String> row : purposeRes) {
					if (row != null && !row.isEmpty()) {
						String dbPurpose = row.get(0); // first column: purpose
						ifr.addItemInCombo("P_HL_LD_Purpose", dbPurpose, dbPurpose);
					}
				}
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception===>" + e.getMessage());
		}
		return "success";
	}

	private void showSHL(IFormReference ifr) {
		ifr.setStyle("SHL_Elg", "disable", "true");
		ifr.setStyle("SHL_Util", "disable", "true");
		ifr.setStyle("SHL_Avail", "disable", "true");

		ifr.setStyle("SHL_Elg", "visible", "true");
		ifr.setStyle("SHL_Util", "visible", "true");
		ifr.setStyle("SHL_Avail", "visible", "true");

		ifr.setStyle("Renovation_Util_SHL", "visible", "false");
		ifr.setStyle("Renovation_Elig_SHL", "visible", "false");
		ifr.setStyle("Renovation_Avail_SHL", "visible", "false");

		ifr.setStyle("SHL_Ren_Util", "visible", "false");
		ifr.setStyle("SHL_Ren_Elg", "visible", "false");
		ifr.setStyle("SHL_Ren_Avail", "visible", "false");

	}

	private void showRenovation(IFormReference ifr) {
		ifr.setStyle("Renovation_Util_SHL", "visible", "true");
		ifr.setStyle("Renovation_Elig_SHL", "visible", "true");
		ifr.setStyle("Renovation_Avail_SHL", "visible", "true");
		ifr.setStyle("Renovation_Util_SHL", "disable", "true");
		ifr.setStyle("Renovation_Elig_SHL", "disable", "true");
		ifr.setStyle("Renovation_Avail_SHL", "disable", "true");

		ifr.setStyle("SHL_Ren_Util", "visible", "true");
		ifr.setStyle("SHL_Ren_Elg", "visible", "true");
		ifr.setStyle("SHL_Ren_Avail", "visible", "true");

		ifr.setStyle("SHL_Ren_Util", "disable", "true");
		ifr.setStyle("SHL_Ren_Elg", "disable", "true");
		ifr.setStyle("SHL_Ren_Avail", "disable", "true");

		ifr.setStyle("SHL_Elg", "visible", "false");
		ifr.setStyle("SHL_Util", "visible", "false");
		ifr.setStyle("SHL_Avail", "visible", "false");

	}

	public String autoPopulateCoBorowerDetailsDataStaffHL(IFormReference ifr, String control, String event,
			String value) {
		Log.consoleLog(ifr, "inside autoPopulateCoBorowerDetailsDataHL: ");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

//		String queryForStaffInfo = "select PERMADDRESS1,PERMADDRESS2,PERMADDRESS3,PERMSTATE,PERMCITY,PERMZIP,MAILADDRESS1,PERMADDRESS2,PERMADDRESS3,MAILINGSTATE,EMAILID,MOBILENUMBER FROM SLOS_TRN_LOANSUMMARY where winame='" + processInstanceId
//				+ "'";
//		List<List<String>> resqueryForStaffInfo = ifr.getDataFromDB(queryForStaffInfo);
//		if (!resqueryForStaffInfo.isEmpty()) {
//			permAddress = resqueryForStaffInfo.get(0).get(0)+" "+resqueryForStaffInfo.get(0).get(1)+" "+resqueryForStaffInfo.get(0).get(2)+" "+resqueryForStaffInfo.get(0).get(3)+" "+resqueryForStaffInfo.get(0).get(4)+" "+resqueryForStaffInfo.get(0).get(5);
//			mailAddress = resqueryForStaffInfo.get(0).get(6)+" "+resqueryForStaffInfo.get(0).get(7)+" "+resqueryForStaffInfo.get(0).get(8)+" "+resqueryForStaffInfo.get(0).get(9);
//		    email= resqueryForStaffInfo.get(0).get(10);
//		    mobilenumber= resqueryForStaffInfo.get(0).get(10);
//		}

		pcm.mImpPopulatePortalHeader(ifr, "P_CB_LD_EMAILID", "P_CB_LD_MOBILENO", "P_CB_LD_COMMUNICATION_ADDRESS",
				"P_CB_LD_PERMANENT_ADDRESS", "P_CB_H_CUSTOMERNAME1");

		String exStaffQuery = "select ex_staff_id from slos_staff_home_trn where winame ='" + processInstanceId + "'";
		List<List<String>> exStaffQueryRes = ifr.getDataFromDB(exStaffQuery);
		Log.consoleLog(ifr, "exStaffQueryRes===>" + exStaffQueryRes);

		String officerQuery = "select * from staff_hl_prod_des_matrix m inner join slos_staff_home_trn s on m.designation= s.designation where STAFF_SCALE like '%Scale%' and winame='"
				+ processInstanceId + "'";
		List<List<String>> officerQueryRes = ifr.getDataFromDB(officerQuery);
		Log.consoleLog(ifr, "officerQuery===>" + officerQuery);

		if (!exStaffQueryRes.isEmpty()) {
			String ex_staff_id = exStaffQueryRes.get(0).get(0);
			if (ex_staff_id != null && !ex_staff_id.trim().isEmpty()) {
				ifr.clearCombo("P_HL_LD_Product");
				ifr.addItemInCombo("P_HL_LD_Product", "Housing Loan-Workmen Staff (Retd Employees)",
						"Housing Loan-Workmen Staff (Retd Employees)");
				ifr.addItemInCombo("P_HL_LD_Product", "Housing Loan-Officer Staff (Retd Employees)",
						"Housing Loan-Officer Staff (Retd Employees)");
				ifr.setStyle("Renovation_Util_SHL", "visible", "false");
				ifr.setStyle("Renovation_Elig_SHL", "visible", "false");
				ifr.setStyle("Renovation_Avail_SHL", "visible", "false");
				ifr.setStyle("SHL_Elg", "visible", "false");
				ifr.setStyle("SHL_Util", "visible", "false");
				ifr.setStyle("SHL_Avail", "visible", "false");

			} else {

				if (!officerQueryRes.isEmpty()) {
					ifr.clearCombo("P_HL_LD_Product");
					ifr.addItemInCombo("P_HL_LD_Product", "Housing Loan-Officer Staff", "Housing Loan-Officer Staff");
					ifr.addItemInCombo("P_HL_LD_Product", "HL(Staff)- Renovation/ Repair",
							"HL(Staff)- Renovation/ Repair");
				} else {
					ifr.clearCombo("P_HL_LD_Product");
					ifr.addItemInCombo("P_HL_LD_Product", "Housing Loan-Workmen Staff", "Housing Loan-Workmen Staff");
					ifr.addItemInCombo("P_HL_LD_Product", "HL(Staff)- Renovation/ Repair",
							"HL(Staff)- Renovation/ Repair");
				}
				ifr.setStyle("Renovation_Util_SHL", "visible", "false");
				ifr.setStyle("Renovation_Elig_SHL", "visible", "false");
				ifr.setStyle("Renovation_Avail_SHL", "visible", "false");
				ifr.setStyle("SHL_Elg", "visible", "false");
				ifr.setStyle("SHL_Util", "visible", "false");
				ifr.setStyle("SHL_Avail", "visible", "false");
			}
		}

		String dateOfJoining = "";
		String dateOfRetirement = "";
		int years = 0;
		int remainingService = 0;
		String querydojdor = "SELECT JOINING_DATE,RETIREMENT_DATE FROM SLOS_STAFF_HOME_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		List<List<String>> listdojdor = ifr.getDataFromDB(querydojdor);
		Log.consoleLog(ifr, "querydojdor====>" + querydojdor);
		if (!listdojdor.isEmpty()) {

			Log.consoleLog(ifr, "querydojdor inside if condition====>");

			dateOfJoining = listdojdor.get(0).get(0);
			Log.consoleLog(ifr, "dateOfJoining====>" + dateOfJoining);

			dateOfRetirement = listdojdor.get(0).get(1);
			Log.consoleLog(ifr, "dateOfRetirement====>" + dateOfRetirement);

			/* ================= DATE OF JOINING ================= */

			if (dateOfJoining == null || dateOfJoining.trim().isEmpty()) {
				Log.consoleLog(ifr, "inside dateOfJoining ====>");
				return "error,date of Joining is Blank";
			} else {

				Log.consoleLog(ifr, "inside else dateOfJoining ====>");

				DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				LocalDate startDate = LocalDateTime.parse(dateOfJoining.trim(), dbFormatter).toLocalDate();

				Log.consoleLog(ifr, "startDate====>" + startDate);

				LocalDate currentDate = LocalDate.now();
				Log.consoleLog(ifr, "currentDate====>" + currentDate);

				Period period = Period.between(startDate, currentDate);
				Log.consoleLog(ifr, "period====>" + period);

				years = period.getYears();
				Log.consoleLog(ifr, "Completed_Years -> " + years);
			}

			/* ================= DATE OF RETIREMENT ================= */

			if (dateOfRetirement == null || dateOfRetirement.trim().isEmpty()) {
				Log.consoleLog(ifr, "inside dateOfRetirement ====>");
				return "error,date of retirement is Blank";
			} else {

				Log.consoleLog(ifr, "inside else dateOfRetirement ====>");

				DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				LocalDate dateOfRetire = LocalDateTime.parse(dateOfRetirement.trim(), dbFormatter).toLocalDate();

				Log.consoleLog(ifr, "dateOfRetire====>" + dateOfRetire);

				LocalDate currentDate = LocalDate.now();
				Log.consoleLog(ifr, "currentDate====>" + currentDate);

				Period period1 = Period.between(currentDate, dateOfRetire);
				remainingService = period1.getYears();

				Log.consoleLog(ifr, "Remaining_Years -> " + remainingService);

				String Query1 = "UPDATE SLOS_STAFF_TRN SET REMAINING_SERVICE='" + remainingService + "' WHERE WINAME='"
						+ processInstanceId + "'";

				Log.consoleLog(ifr, "SLOS_STAFF_TRN SET REMAINING_SERVICE -> " + Query1);

				ifr.saveDataInDB(Query1);
			}
		}

		try {
			pcm.mImpPopulatePortalHeader(ifr, "P_CB_OD_EMAILID", "P_CB_OCCINFO_MOBILENO",
					"P_CB_OD_COMMUNICATION_ADDRESS", "P_CB_OD_PERMANENT_ADDRESS", "P_CB_H_CUSTOMERNAME1");
			ifr.setStyle("P_HL_EXISTING_CUSTOMER", "visible", "true");

		} catch (Exception ex) {

			Log.consoleLog(ifr, "autoPopulateCoBorowerDetailsDataHL:Exception::" + ex.getMessage());
		}

		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Co-Applicant'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);

		return "";

	}

	public String autoPopulateSectionCollateral(IFormReference ifr, String control, String event, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "HLPortal:autoPopulateSectionCollateral:setValue:");
		try {

			pcm.mImpPopulatePortalHeader(ifr, "P_CB_LD_EMAILID", "P_CB_LD_MOBILENO", "P_CB_LD_COMMUNICATION_ADDRESS",
					"P_CB_LD_PERMANENT_ADDRESS", "P_CB_H_CUSTOMERNAME1");

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in autoPopulateCollateralDataHL::HLPortal" + e);
		}
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Collateral Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		return "";
	}

	public String setControlIdForCoBorrower(IFormReference ifr, String Control) {
		String productCode = pcm.getProductCodeByProductName(ifr);
		Log.consoleLog(ifr, "Inside getControlIdForBorrower:" + Control);
		if (productCode.equals("HL")) {
			Control = "P_HL_EXISTING_CUSTOMER";
		} else {
			Control = Control.replaceAll("_(\\d+)$", "");
		}
		Log.consoleLog(ifr, "Control=" + Control);
		String isCoBorrowerRequired = ifr.getValue(Control).toString();
		Log.consoleLog(ifr, "isCoBorrowerRequired: " + isCoBorrowerRequired);
		appControlSetCoBorrowe(ifr, isCoBorrowerRequired);
		return "";
	}

	public String appControlSetCoBorrowe(IFormReference ifr, String isCoBorrowerRequired) {
		Log.consoleLog(ifr, "Inside getControlIdForBorrower");
		if ("YES".equalsIgnoreCase(isCoBorrowerRequired)) {
			ifr.setStyle("P_CoObligant_OD_Section", "visible", "true");
		} else {
			ifr.setStyle("P_CoObligant_OD_Section", "visible", "false");
		}
		// Return default value if no match is foundreturn"Invalid Input";
		return "";
	}

	public String exStaffDetailsHL(IFormReference ifr, String empId) throws ParseException {
		Log.consoleLog(ifr, "#Inside staffDetailsVL...");
		Validator valid = new KnockOffValidator("None");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONParser parser = new JSONParser();
		Log.consoleLog(ifr, "#Inside processInstanceId..." + processInstanceId);
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Staff Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		// CustomerAccountSummaryPAPL CBS1 = new CustomerAccountSummaryPAPL();
		// String status = CBS1.executeCustomerAccountSummary(ifr, processInstanceId);
		// if (status.contains(RLOS_Constants.ERROR)) {
		// Log.consoleLog(ifr, "inside error condition hrmsDetails");
		// return "error" + "," + "Technical glitch, Try after sometime!";
		// }
		Date dateTimeFormat = new Date();
		SimpleDateFormat sDateTime = new SimpleDateFormat("yyyy-MM-dd");
		String strCurDateTime = sDateTime.format(dateTimeFormat);
		String Query1 = "INSERT INTO SLOS_TRN_LOANSUMMARY (WINAME, SANCTION_DATE,GENERATE_DOC) SELECT '" + processInstanceId + "', '"
				+ strCurDateTime
				+ "','YES' FROM dual WHERE NOT EXISTS (    SELECT 1 FROM SLOS_TRN_LOANSUMMARY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query1);
		
		String Query3 = "INSERT INTO SLOS_TRN_LOANDETAILS (PID) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_TRN_LOANDETAILS WHERE PID = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query3);

		cf.mExecuteQuery(ifr, Query3, "insert into SLOS_STAFF_VL_ELIGIBILITY");

		CommonFunctionalityCreator.getInstance().mExecuteQuery(ifr, Query1, "mImpOnClickFianlEligibility ");
		String customerId = "";
		String gender = "";
		String age = "";
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
			age = list.get(0).get(3);
			ifr.setValue("AGE_VL", age);
			// this code should be reverted when giving build to UAT
			LocalDateTime dateTime = LocalDateTime.parse(list.get(0).get(2), inputFormatter);
			String dateOnly = dateTime.toLocalDate().toString();
			ifr.setValue("DATEOFBIRTH", dateOnly);
			Log.consoleLog(ifr, "***********");
			// ifr.setValue("DATEOFBIRTH", list.get(0).get(2));
		}
		if (!age.trim().isEmpty() && age != null) {
			if (Integer.parseInt(age) < 60 && Integer.parseInt(age) > 65) {
				return "error, Not Eligible as age is not falling in range of 60 to 65 ";
			}
		}

		String Query2 = "INSERT INTO SLOS_STAFF_VL_ELIGIBILITY (WINAME) SELECT '" + processInstanceId
				+ "' FROM dual WHERE NOT EXISTS (  SELECT 1 FROM SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '"
				+ processInstanceId + "')";

		Log.consoleLog(ifr, "mImpOnClickFianlEligibility===>" + Query2);

		cf.mExecuteQuery(ifr, Query2, "insert into SLOS_STAFF_VL_ELIGIBILITY");

		Log.consoleLog(ifr, "***********");
		String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set Age='" + list.get(0).get(3) + "' " + "where WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
		ifr.saveDataInDB(ageQuery);
		PensionApi pensionApiRes = new PensionApi();
		String response = pensionApiRes.pension(ifr, customerId);

		// HRMS hrms = new HRMS();
		// String response = hrms.getHrmsDetailsVL(ifr, empId);
		// if (response.contains(RLOS_Constants.ERROR)) {
		// return "error" + "," + "Technical glitch, Try after sometime!";
		// }

		if (response.contains(RLOS_Constants.ERROR)) {
			return "error" + "," + "Technical glitch, Try after sometime!";
		}
		JSONObject pensionObject = (JSONObject) parser.parse(response);
		Log.consoleLog(ifr, "pensionObject====>" + pensionObject);
		ifr.setValue("EXStaffName", pensionObject.get("Name").toString());
		ifr.setValue("Q_FinalAuthorityDesignation", pensionObject.get("Name").toString());
		// Log.consoleLog(ifr, "pensionObject====>" + pensionObject);
		ifr.setValue("EXStaffID", pensionObject.get("empId").toString());
		ifr.setValue("EXPPOnumber", pensionObject.get("ppoNumber").toString());
		ifr.setValue("EXStaffDesignation", pensionObject.get("designation").toString());
		ifr.setValue("EXDate_of_Retirement", pensionObject.get("dateOfRetirement").toString());
		ifr.setValue("EXDATEOFBIRTH", pensionObject.get("dateOfBirth").toString());
		ifr.setValue("EX_PRNSION_CREDIT_ACCOUNT", pensionObject.get("accNumber").toString());
		ifr.setValue("LATEST_PENSION_CREDIT_MONTH", pensionObject.get("penMonth").toString());
		ifr.setStyle("EX_STAFF_GROSS_INCOME", "visible", "true");
		ifr.setStyle("EX_STAFF_NET_INCOME", "visible", "true");
		ifr.setValue("EX_STAFF_GROSS_INCOME", pensionObject.get("grossPension").toString());
		ifr.setValue("EX_STAFF_NET_INCOME", pensionObject.get("netPension").toString());
		ifr.setStyle("Gross_Salary", "visible", "false");
		ifr.setStyle("Net_Salary", "visible", "false");

		String designation = pensionObject.get("designation").toString();
		String probation = "N";
		String totalDeduction = "0";
		String gross = "0";
		String probationQuery = "SELECT probation from slos_staff_home_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (queryRes.isEmpty()) {
			String insert = "INSERT INTO slos_staff_home_trn(winame,probation,statutory_deductions) values('"
					+ processInstanceId + "','" + probation + "','" + totalDeduction + "')";
			ifr.saveDataInDB(insert);
			Log.consoleLog(ifr, "statutory_deductions==>" + insert);
		} else {
			String updateQuery = "UPDATE slos_staff_home_trn SET probation='" + probation + "',statutory_deductions='"
					+ totalDeduction + "' where winame='" + processInstanceId + "'";
			ifr.saveDataInDB(updateQuery);
			Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);
		}

		String updateQueryHL = "UPDATE slos_staff_home_trn SET EX_STAFF_DOR=TO_DATE('"
				+ pensionObject.get("dateOfRetirement").toString() + "','YYYY-MM-DD'),EX_STAFF_DOB=TO_DATE('"
				+ pensionObject.get("dateOfBirth").toString() + "','YYYY-MM-DD') WHERE winame='" + processInstanceId
				+ "'";

		ifr.saveDataInDB(updateQueryHL);
		Log.consoleLog(ifr, "updateQueryHL==>" + updateQueryHL);

		String queryForMaLoanAmout = "select count(*) from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
				+ pensionObject.get("designation").toString()
				+ "' and probation_tag IN ('Y','N') and DESIGNATION like '%Probationary%'";
		Log.consoleLog(ifr, "Count query===>" + queryForMaLoanAmout);
		List Result1 = ifr.getDataFromDB(queryForMaLoanAmout);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);

		if (Integer.parseInt(Count1) > 0) {
			Log.consoleLog(ifr, "Not Eligible.");
			return "error, ExStaff is under probationary period, hence is not eligible for Staff Home Loan ";
		}

		String warning = pensionObject.get("Warning").toString();
		String salaryAcc = pensionObject.get("accNumber").toString();
		String validProductCodeQuery = "SELECT productcode FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult = ifr.getDataFromDB(validProductCodeQuery);
		List<String> productCodes = validProductCodeResult.stream().flatMap(tempMap -> tempMap.stream())
				.collect(Collectors.toList());
		List<String> temp = new ArrayList<>();
		temp.addAll(productCodes);
		// IFormReference ifr, String ProcessInstanceId, String CustomerID,vali
		// String salaryacc, String designation, String probation,List<String>
		// validProductCode
		Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
		String adv360Response = adv360.executeCBSAdvanced360Inquiryv2VL(ifr, processInstanceId, customerId, salaryAcc,
				designation, probation, temp, false, "Barrower");

//			String deleteQuery = "DELETE FROM SLOS_ALL_ACTIVE_PRODUCT WHERE ROWID IN (SELECT rid FROM ( SELECT ROWID AS rid, ROW_NUMBER() OVER (PARTITION BY winame, LOAN_ACC_NUMBER ORDER BY ROWID) AS rn FROM SLOS_ALL_ACTIVE_PRODUCT WHERE winame = '"+processInstanceId+"' AND LOAN_ACC_NUMBER = '"+accountId.trim()+"') WHERE rn > 1)";
//			Log.consoleLog(ifr, "deleteQuery qquery==>" + deleteQuery);
//			ifr.saveDataInDB(deleteQuery);

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

		String queryForIRNum = "SELECT IR_REFERANCH_ID from slos_staff_home_trn where winame='" + processInstanceId
				+ "'";
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

		String queryForLoan = "select sum(limit) as limitCob from SLOS_ALL_ACTIVE_PRODUCT where winame='"
				+ processInstanceId + "' and productcode in ('666','667')";
		Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT queryForLoan==>" + queryForEMI);
		List<List<String>> resqueryForLoan = ifr.getDataFromDB(queryForLoan);
		if (resqueryForLoan != null && !resqueryForLoan.isEmpty() && !resqueryForLoan.get(0).isEmpty()) {
			String value = resqueryForLoan.get(0).get(0);
			if (value != null && !value.trim().isEmpty()) {
				boolean isGreater = Double.parseDouble(value) > 0;
				if (isGreater) {
					return "error, already existing a loan so not eligible";
				}
			}
		}

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

		String mobilenumber = "";
		String mobileQuery = "select mobileNumber from los_wireference_table WHERE WINAME = '" + processInstanceId
				+ "'";
		List<List<String>> resmobileQuery = ifr.getDataFromDB(mobileQuery);
		if (!resmobileQuery.isEmpty()) {
			mobilenumber = resmobileQuery.get(0).get(0);
		}

		String deleteQuery = "delete from LOS_NL_BASIC_INFO where PID='" + processInstanceId
				+ "' and APPLICANTTYPE='B'";
		ifr.saveDataInDB(deleteQuery);

		String basicDetailInsertQuery = "INSERT INTO LOS_NL_BASIC_INFO (PID, APPLICANTTYPE, ENTITYTYPE, EXISTINGCUSTOMER, CUSTOMERID, F_KEY, INSERTIONORDERID, FULLNAME, STAFFMEMBER, CONSIDERELIGIBILITY, STATUS,MOBILENUMBER) VALUES ('"
				+ processInstanceId + "', 'B', 'I', 'Yes', '" + customerId
				+ "', (SELECT NVL(MAX(F_KEY),0) + 1 FROM LOS_NL_BASIC_INFO), (SELECT NVL(MAX(INSERTIONORDERID),0) - 50 FROM LOS_NL_BASIC_INFO), '"
				+ pensionObject.get("Name").toString() + "', 'Y', 'Yes', 'IN-PROGRESS','" + mobilenumber + "')";
		// List<List<String>> resbasicDetailInsertQuery =
		// ifr.getDataFromDB(basicDetailInsertQuery);
		Log.consoleLog(ifr, "basicDetailInsertQuery ==>" + basicDetailInsertQuery);
		ifr.saveDataInDB(basicDetailInsertQuery);

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
	 * @return ,'EX-STAFF'
	 */
	public String onLoadDocUploadHL(IFormReference ifr) {
		Log.consoleLog(ifr, "into staffDetailsLoanAvailbilityCalc");
		String vT = "";
		String documentsName = "";
		String pensioner = "";
		String exStaff = "";
		List docName = new ArrayList<>();
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		List<List<String>> listMandatoryDocuments = new ArrayList<>();
		List<List<String>> listOptionalDocuments = new ArrayList<>();

		String queryForEXStaff = "select EX_STAFF_ID from SLOS_STAFF_HOME_TRN where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_STAFF_HOME_TRN queryForEXStaff==>" + queryForEXStaff);
		List<List<String>> resqueryForEXStaff = ifr.getDataFromDB(queryForEXStaff);
		if (!resqueryForEXStaff.isEmpty()) {
			exStaff = resqueryForEXStaff.get(0).get(0);
		}

		if (exStaff == null || exStaff.trim().isEmpty()) {
			String getOptionalDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND MANDATORY = 'N' AND GENERATE='N' AND STAFF_TYPE IN ('STAFF')";
			listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
			Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);

			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND GENERATE='N' AND STAFF_TYPE IN ('STAFF')";
			listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
		} else {
			String getOptionalDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND MANDATORY = 'N' AND GENERATE='N' AND STAFF_TYPE IN ('STAFF','EX-STAFF')";
			listOptionalDocuments = ifr.getDataFromDB(getOptionalDocuments);
			Log.consoleLog(ifr, "listOptionalDocuments==>" + listOptionalDocuments);

			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND MANDATORY = 'Y' AND GENERATE='N 'AND STAFF_TYPE IN ('STAFF','EX-STAFF')";
			listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
		}
		// Grid for Optional documents to Upload
		List<String> responseForGrid = listOptionalDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
		String[] Columnheading = new String[] { "Document Name" };
		ifr.clearTable("Opt_Doc_Table_Portal_VL");
		JSONArray arrayRes = new JSONArray();
		for (int i = 0; i < responseForGrid.size(); i++) {
			// for (List<String> row : responseForGrid) {
			JSONObject obj = new JSONObject();
			Log.consoleLog(ifr, "Opt_Doc_Table_Portal_VL==>" + obj);
			obj.put(Columnheading[0], responseForGrid.get(i));
			arrayRes.add(obj);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes);

		ifr.addDataToGrid("Opt_Doc_Table_Portal_VL", arrayRes);

		// Grid for Mandatory documents to Upload
		List<String> responseForGrid1 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String[] Columnheading1 = new String[] { "Document Name" };
		ifr.clearTable("Doc_Table_Portal_VL");
		JSONArray arrayRes1 = new JSONArray();
		for (int i = 0; i < responseForGrid1.size(); i++) {
			// for (List<String> row : responseForGrid1) {
			JSONObject obj1 = new JSONObject();
			Log.consoleLog(ifr, "Doc_Table_Portal_VL==>" + obj1);
			obj1.put(Columnheading1[0], responseForGrid1.get(i));
			arrayRes1.add(obj1);
		}
		Log.consoleLog(ifr, "Json Array " + arrayRes1);

		ifr.addDataToGrid("Doc_Table_Portal_VL", arrayRes1);

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Document Upload' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
		return "Success";

	}

	public String populateCollateralCostingOnLoadSHL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside method StaffHLPortal:populateCollateralCostingOnLoadSHL");
		try {
			String selectedPurpose = "";
			String purpose = "";
			String purposeQuery = "select PURPOSENAME from slos_home_purpose WHERE PURPOSECODE='SHLP2'";
			Log.consoleLog(ifr, "populateCollateralCostingOnLoadSHL purposeQuery:: " + purposeQuery);
			List<List<String>> purposeData = ifr.getDataFromDB(purposeQuery);
			if (!purposeData.isEmpty()) {
				purpose = purposeData.get(0).get(0);
				Log.consoleLog(ifr, "Purpose data from DB:: sTaff HL:: " + purpose);
			}
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String selectedPurposeQuery = "select hl_purpose from SLOS_STAFF_HOME_TRN where winame='" + PID + "'";
			Log.consoleLog(ifr, "populateCollateralCostingOnLoadSHL SelectedPurposeQuery:: " + selectedPurposeQuery);
			List<List<String>> SelectedPurposeData = ifr.getDataFromDB(selectedPurposeQuery);
			if (!SelectedPurposeData.isEmpty()) {
				selectedPurpose = SelectedPurposeData.get(0).get(0);
				Log.consoleLog(ifr, "Selcted Purpose:: sTaff HL:: " + selectedPurpose);
				if (selectedPurpose.equalsIgnoreCase(purpose)) {
					ifr.setStyle("P_HL_CD_PLOTCOST", "visible", "true");
					ifr.setStyle("P_HL_CD_CONSTRUCTIONCOST", "visible", "true");
				}
			}
		} catch (Exception ex) {
			Log.consoleLog(ifr, "populateCollateralCostingOnLoadSHL:Exception::" + ex.getMessage());
		}

		return "";
	}

//	public String calculateMarginByProjectCost(IFormReference ifr, String control, String event, String value) {
//		Log.consoleLog(ifr, "::Inside calculateMarginByProjectCost Method::");
//		int marginPercentage = 0;
//		Log.consoleLog(ifr, "Margin Percentage (initial):: " + marginPercentage);
//
//		try {
//			String projectCost = ifr.getValue("P_HL_CD_PROJECTCOST").toString();
//			Log.consoleLog(ifr, "Parsed Project cost:: " + projectCost);
//			String hlProductQuery = "select HL_PRODUCT,TOTAL_HL_ELIG from slos_staff_home_trn where winame='"
//					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
//			Log.consoleLog(ifr, "HL Product Query:: " + hlProductQuery);
//			List<List<String>> hlProductRes = ifr.getDataFromDB(hlProductQuery);
//			Log.consoleLog(ifr, "HL Product Query Result:: " + hlProductRes);
//			if (hlProductRes.size() <= 0) {
//				Log.consoleLog(ifr, "Error: No data found for HL product.");
//				return "Error,Techincal glitch";
//			}
//
//			if (!projectCost.isEmpty()
//					&& Double.parseDouble(projectCost) > Double.parseDouble(hlProductRes.get(0).get(1))) {
//				Log.consoleLog(ifr, "Error: Project cost > scheme limit");
//				return "Error, Project cost cannot be greater than scheme limit";
//			}
//
//			String isExStaffQuery = "select flgcustype from los_trn_customerSummary where winame='"
//					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
//			Log.consoleLog(ifr, "isExStaff Query:: " + isExStaffQuery);
//			List<List<String>> res = ifr.getDataFromDB(isExStaffQuery);
//			Log.consoleLog(ifr, "isExStaff Query Result:: " + res);
//			String minMarginQuery = "";
//			if (!((String) ((List) res.get(0)).get(0)).equalsIgnoreCase("R1")) {
//				Log.consoleLog(ifr, "Customer type: Retired Staff");
//				String propertyAge = ifr.getValue("P_CD_HL_Age_of_Property").toString();
//				if(propertyAge.trim().isEmpty() || propertyAge.trim().equalsIgnoreCase("")) {
//					return  "Please Enter age of property.";
//				}
//				minMarginQuery = "SELECT case when " + propertyAge
//						+ " <= TO_NUMBER(property_type) then margin_percentage else MARGIN_PERCENTAGE_GT_10 end as marginPercentage FROM slos_staff_home_loan_amount_margin WHERE "
//						+ projectCost + " BETWEEN loan_amount_min AND loan_amount_max";
//			} else {
//				Log.consoleLog(ifr, "Customer type: Active Staff");
//				if (hlProductRes.size() <= 0) {
//					Log.consoleLog(ifr, "Error: No HL product data found.");
//					return "Error,Technical glitch";
//				}
//
//				minMarginQuery = "SELECT margin_percentage FROM slos_staff_home_loan_amount_margin WHERE trim(STAFF_PRODUCT_CODE)='"
//						+ hlProductRes.get(0).get(0).toString().trim() + "'";
//			}
//
//			Log.consoleLog(ifr, "Min Margin Query:: " + minMarginQuery);
//			List<List<String>> resOfMinMargin = ifr.getDataFromDB(minMarginQuery);
//			Log.consoleLog(ifr, "Min Margin Query Result:: " + resOfMinMargin);
//			BigDecimal cost = new BigDecimal(projectCost);
//			BigDecimal percentage = new BigDecimal((String) ((List) resOfMinMargin.get(0)).get(0));
//			Log.consoleLog(ifr, "Project Cost (BigDecimal):: " + cost);
//			Log.consoleLog(ifr, "Margin Percentage (from DB):: " + percentage);
//			BigDecimal margin = cost.multiply(percentage).divide(BigDecimal.valueOf(100L));
//			Log.consoleLog(ifr, "Actual margin amount (BigDecimal):: " + margin);
//			margin = margin.setScale(0, RoundingMode.HALF_UP);
//			Log.consoleLog(ifr, "Calculated Margin:: " + margin);
//			BigDecimal eligibleAmount = cost.subtract(margin);
//			eligibleAmount = eligibleAmount.setScale(2, RoundingMode.HALF_UP);
//			Log.consoleLog(ifr, "Calculated Eligible Amount:: " + eligibleAmount);
//			ifr.setValue("SHL_COLL_MARGIN_AMT", margin.toString());
//			Log.consoleLog(ifr, "Set Margin Amount to form:: " + margin);
//			ifr.setValue("SHL_COLL_MARGIN_PERCENT", (String) ((List) resOfMinMargin.get(0)).get(0));
//			Log.consoleLog(ifr, "Set Margin Percentage to form:: " + (String) ((List) resOfMinMargin.get(0)).get(0));
//			ifr.setValue("P_HL_CD_Eligible_amount", eligibleAmount.toString());
//			Log.consoleLog(ifr, "Set Eligible Amount to form:: " + eligibleAmount);
//		} catch (Exception ex) {
//			Log.consoleLog(ifr, "Exception during Margin Calculation:: " + ex.getMessage());
//		}
//
//		Log.consoleLog(ifr, "::Exiting calculateMarginByProjectCost Method::");
//		return "";
//	}

	public String calculateMarginByProjectCost(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		Log.consoleLog(ifr, "::Inside calculateMarginByProjectCost Method::");
		int marginPercentage = 0;
		Log.consoleLog(ifr, "Margin Percentage (initial):: " + marginPercentage);

		try {
			String projectCost = ifr.getValue("P_HL_CD_PROJECTCOST").toString().trim();
			Log.consoleLog(ifr, "Parsed Project cost:: " + projectCost);
			if (projectCost.isEmpty() || projectCost.trim().equalsIgnoreCase("")) {
				message.put("showMessage",
						cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter the Project Cost."));
				return message.toString();
			}
			String propertyAge = ifr.getValue("P_CD_HL_Age_of_Property").toString().trim();
			Log.consoleLog(ifr, "Parsed propertyAge:: " + propertyAge);
			String hlProductQuery = "select HL_PRODUCT,TOTAL_HL_ELIG from slos_staff_home_trn where winame='"
					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
			Log.consoleLog(ifr, "HL Product Query:: " + hlProductQuery);
			List<List<String>> hlProductRes = ifr.getDataFromDB(hlProductQuery);
			Log.consoleLog(ifr, "HL Product Query Result:: " + hlProductRes);
			if (hlProductRes.size() <= 0) {
				Log.consoleLog(ifr, "Error: No data found for Selected HL product.");
				message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
						"Error: No data found for Selected HL product."));
				return message.toString();
			}

			String isExStaffQuery = "select flgcustype from los_trn_customerSummary where winame='"
					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
			Log.consoleLog(ifr, "isExStaff Query:: " + isExStaffQuery);
			List<List<String>> res = ifr.getDataFromDB(isExStaffQuery);
			Log.consoleLog(ifr, "isExStaff Query Result:: " + res);
			String minMarginQuery = "";
			if (!((String) ((List) res.get(0)).get(0)).equalsIgnoreCase("R1")) {
				if (propertyAge.isEmpty() || propertyAge.trim().equalsIgnoreCase("")) {
					message.put("showMessage",
							cf.showMessage(ifr, "navigationNextBtn", "error", "Kindly enter the Age of property."));
					return message.toString();
				}
				Log.consoleLog(ifr, "Customer type: Retired Staff");
				minMarginQuery = "SELECT case when " + propertyAge
						+ " <= TO_NUMBER(property_type) then margin_percentage else MARGIN_PERCENTAGE_GT_10 end as marginPercentage FROM slos_staff_home_loan_amount_margin WHERE "
						+ projectCost + " BETWEEN loan_amount_min AND loan_amount_max";
			} else {
				Log.consoleLog(ifr, "Customer type: Active Staff");
				if (hlProductRes.size() <= 0) {
					Log.consoleLog(ifr, "Error: No HL product data found.");
					message.put("showMessage", cf.showMessage(ifr, "navigationNextBtn", "error",
							"Error: No data found for Selected HL product."));
					return message.toString();
				}

				minMarginQuery = "SELECT margin_percentage FROM slos_staff_home_loan_amount_margin WHERE trim(STAFF_PRODUCT_CODE)='"
						+ hlProductRes.get(0).get(0).toString().trim() + "'";
			}

			Log.consoleLog(ifr, "Min Margin Query:: " + minMarginQuery);
			List<List<String>> resOfMinMargin = ifr.getDataFromDB(minMarginQuery);
			Log.consoleLog(ifr, "Min Margin Query Result:: " + resOfMinMargin);
			BigDecimal cost = new BigDecimal(projectCost);
			BigDecimal percentage = new BigDecimal((String) ((List) resOfMinMargin.get(0)).get(0));
			Log.consoleLog(ifr, "Project Cost (BigDecimal):: " + cost);
			Log.consoleLog(ifr, "Margin Percentage (from DB):: " + percentage);
			BigDecimal margin = cost.multiply(percentage).divide(BigDecimal.valueOf(100L));
			Log.consoleLog(ifr, "Actual margin amount (BigDecimal):: " + margin);
			margin = margin.setScale(0, RoundingMode.HALF_UP);
			Log.consoleLog(ifr, "Calculated Margin:: " + margin);
			BigDecimal eligibleAmount = cost.subtract(margin);
			eligibleAmount = eligibleAmount.setScale(2, RoundingMode.HALF_UP);
			Log.consoleLog(ifr, "Calculated Eligible Amount:: " + eligibleAmount);
			ifr.setValue("SHL_COLL_MARGIN_AMT", margin.toString());
			Log.consoleLog(ifr, "Set Margin Amount to form:: " + margin);
			ifr.setValue("SHL_COLL_MARGIN_PERCENT", (String) ((List) resOfMinMargin.get(0)).get(0));
			Log.consoleLog(ifr, "Set Margin Percentage to form:: " + (String) ((List) resOfMinMargin.get(0)).get(0));
			ifr.setValue("P_HL_CD_Eligible_amount", eligibleAmount.toString());
			Log.consoleLog(ifr, "Set Eligible Amount to form:: " + eligibleAmount);
		} catch (Exception ex) {
			Log.consoleLog(ifr, "Exception during Margin Calculation:: " + ex.getMessage());
		}

		Log.consoleLog(ifr, "::Exiting calculateMarginByProjectCost Method::");
		return "";
	}

	public String msaveDataInPartyDetailGridFetch(IFormReference ifr) {
		try {
			JSONObject message = new JSONObject();
			Log.consoleLog(ifr, "inside try block::::msaveDataInPartyDetailGridFetch::::: ");
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			HashMap<String, String> customerdetails = new HashMap<>();
			String applicantType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
			String strCusterid = ifr.getValue("CTRID_PD_SRCHVAL").toString();
			Log.consoleLog(ifr, "msaveDataInPartyDetailGridFetch::Param Value::strCusterid::" + strCusterid);
			String mobileNumber = ifr.getValue("Mobile_Number_PD").toString();
			Log.consoleLog(ifr, "msaveDataInPartyDetailGridFetch::Param Value::mobileNumber::" + mobileNumber);

			String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
			List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query,
					"msaveDataInPartyDetailsGridFetch::Execute query for fetching loan selected ");
			String loan_selected = loanSelected.get(0).get(0);
			Log.consoleLog(ifr, "loan type==>" + loan_selected);
			if (Arrays.asList(AcceleratorConstants.CANARA_MORTGAGE_LOANS).contains(loan_selected)) {
				Log.consoleLog(ifr, "::Inside if condition after matching correct loan selected::");
				String appType = "'" + applicantType + "'";
				if (applicantType.equalsIgnoreCase("CB") || applicantType.equalsIgnoreCase("G")) {
					appType = "'CB', 'G'";
				}
				String applicantQuery = "Select count(*) from los_nl_basic_info where pid ='" + ProcessInstanceId
						+ "' and applicanttype in (" + appType + ")";
				List<List<String>> typeOfApplicant = cf.mExecuteQuery(ifr, applicantQuery,
						"msaveDataInPartyDetailsGridFetch::Execute query for fetching loan selected ");
				String coApplicantCount = typeOfApplicant.get(0).get(0);
				Log.consoleLog(ifr, "applicanType==>" + coApplicantCount);
				String productCode = "";
				String productCodeQuery = "SELECT PRODUCT FROM los_nl_proposed_facility WHERE PID= '"
						+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
				Log.consoleLog(ifr, "PCM::::getProductCode:" + productCodeQuery);
				List<List<String>> productCodeResult = cf.mExecuteQuery(ifr, productCodeQuery, "getproductcode");
				if (!productCodeResult.isEmpty()) {
					productCode = productCodeResult.get(0).get(0);
				}
				Log.consoleLog(ifr, "productCode==>" + productCode);
				if (applicantType.equalsIgnoreCase("B")) {
					if (Integer.parseInt(coApplicantCount) == 1) {
						ifr.setValue("Mobile_Number_PD", "");
						ifr.setValue("CTRID_PD_SRCHVAL", "");
						ifr.setValue("CTRID_PD_EXTCUST", "");
						ifr.setValue("CTRID_PD_PARTYTYPE", "");
						message.put("MSGSTS", "N");
						message.put("SHOWMSG", "Maximum number of Borrowers Added");
						Log.consoleLog(ifr, "BudgetBkoffCustomCode:msaveDataInPartyDetailGridFetch -> message: "
								+ message.toString());
						return message.toString();
					}
				}
			}
			int gridSize = ifr.getDataFromGrid("ALV_BASIC_INFO").size();
			if (gridSize > 0) {
				for (int i = 0; i < gridSize; i++) {
					String custId = ifr.getTableCellValue("ALV_BASIC_INFO", i, "QNL_BASIC_INFO_CustomerID").toString();
					if (custId.equalsIgnoreCase(strCusterid)) {
						ifr.setValue("Mobile_Number_PD", "");
						ifr.setValue("CTRID_PD_SRCHVAL", "");
						ifr.setValue("CTRID_PD_EXTCUST", "");
						ifr.setValue("CTRID_PD_PARTYTYPE", "");
						message.put("MSGSTS", "N");
						message.put("SHOWMSG", "Same Customer data cannot be added");
						return message.toString();
					}
				}
			}

			customerdetails.put("CustomerId", strCusterid);
			customerdetails.put("mobileNumber", mobileNumber);
			customerdetails.put("ApplicantType", applicantType);
			String APIStatus = cas.fetchCustomerAccountSummaryCoBorrower(ifr, customerdetails);

			Log.consoleLog(ifr, "APIStatus:::: " + APIStatus);

			if (APIStatus.contains("Kindly update the CBS")) {
				message.put("MSGSTS", "N");
				message.put("SHOWMSG", APIStatus);
				return message.toString();
			}
			if (APIStatus.contains(RLOS_Constants.ERROR)) {
				Log.consoleLog(ifr, "inside error condition fetchCustomerAccountSummaryBorrower");
				ifr.setValue("Mobile_Number_PD", "");
				ifr.setValue("CTRID_PD_SRCHVAL", "");
				message.put("MSGSTS", "N");
				message.put("SHOWMSG", APIStatus);
				return message.toString();
			}
			if (APIStatus.equalsIgnoreCase("Customer does not exist")) {
				Log.consoleLog(ifr, "Customer does not exist");
				ifr.setValue("Mobile_Number_PD", "");
				ifr.setValue("CTRID_PD_SRCHVAL", "");
				message.put("MSGSTS", "N");
				message.put("SHOWMSG", "Customer does not exist.");
				return message.toString();
			} else {
				Log.consoleLog(ifr,
						"BudgetBkoffCustomCode:msaveDataInPartyDetailGridFetch -> CustomerAccountSummary JSONArray: "
								+ APIStatus);
				JSONParser parser = new JSONParser();
				JSONArray result = (JSONArray) parser.parse(APIStatus);
//				ifr.addDataToGrid("ALV_BASIC_INFO", result, true);

				ifr.setValue("Mobile_Number_PD", "");
				ifr.setValue("CTRID_PD_SRCHVAL", "");
				ifr.setValue("CTRID_PD_EXTCUST", "");
				ifr.setValue("CTRID_PD_PARTYTYPE", "");
				// onChangeNoBorrowerDetailsYes(ifr);

			}

			Log.consoleLog(ifr, "before autoPupulateBueroConsentFromPortal$$");
			Log.consoleLog(ifr, "ApplicantType" + applicantType);
			if ((applicantType.equalsIgnoreCase("CB"))) {
				Log.consoleLog(ifr, "before autoPupulateBueroConsentFromPortal");
			}

			message.put("MSGSTS", "Y");
			message.put("SHOWMSG", "");
			return message.toString();
			// mPopulateMisDataCB(ifr);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
			Log.errorLog(ifr, "Exception msaveDataInPartyDetailGridFetch : " + e);
			JSONObject message = new JSONObject();
			message.put("MSGSTS", "N");
			message.put("SHOWMSG", "Technical glitch, Try after sometime!");
			return message.toString();
		}
	}

	public String EXServiceManCheck(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String EXSTAFFID = "";
		// String tenure = "";
		try {
			String Query = "SELECT EX_STAFF_ID from SLOS_STAFF_HOME_TRN WHERE WINAME='" + processInstanceId + "'";
			Log.consoleLog(ifr, "EX-staff query===>" + Query);
			List<List<String>> res = ifr.getDataFromDB(Query);
			Log.consoleLog(ifr, "res===>" + res);

			if (res != null && !res.isEmpty()) {
				List<String> firstRow = res.get(0);
				List<String> secondRow = res.get(1);
				if (firstRow != null && !firstRow.isEmpty()) {
					String totalHlAvailStr = firstRow.get(0);
					if (totalHlAvailStr != null && !totalHlAvailStr.trim().isEmpty()) {
						// if (!totalHlAvailStr.isEmpty() && totalHlAvailStr != null) {
						// EXSTAFFID = Double.parseDouble(totalHlAvailStr.trim());
						Log.consoleLog(ifr, "EX Working STAFF " + totalHlAvailStr);
						ifr.setStyle("PersonalDetails_EX", "visible", "true");
						ifr.setStyle("PersonalDetails_S", "visible", "false");
						ifr.setStyle("STAFFID_SEC", "visible", "flase");
					} else {
						Log.consoleLog(ifr, "current working staff" + totalHlAvailStr);
					}
				}

			}
		} catch (Exception e) {

		}
		try {
			String Query = "SELECT HL_PRODUCT from SLOS_STAFF_HOME_TRN WHERE WINAME='" + processInstanceId + "'";
			List<List<String>> res = ifr.getDataFromDB(Query);
			if (res != null && !res.isEmpty()) {
				List<String> firstRow = res.get(0);
				if (firstRow != null && !firstRow.isEmpty()) {
					String productName = firstRow.get(0);
					if (productName != null && !productName.trim().isEmpty()) {
						String Query1 = "SELECT PRODUCTCODE from SLOS_HOME_PURPOSE where PURPOSENAME='" + productName
								+ "'";
						List<List<String>> res1 = ifr.getDataFromDB(Query1);
						ifr.setValue("CBSPRODUCTCODE", res1.get(0).get(0));
					} else {
						Log.consoleLog(ifr, "Designation not present");
					}
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception while fetching TOTAL_HL_AVAIL: " + e.getMessage());
			// EXSTAFFID = 0.0;
		}
		return "success";
	}

	public String createHLLoanDisburse(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanAccountNumber = "";
		Log.consoleLog(ifr, "VLBkoffcCustomCode:createVLLoanDisburse started...");
		try {
			VLAPIPreprocessor objvlPreprocessor = new VLAPIPreprocessor();
			String productCode = pcm.getProductCodeByProductName(ifr);
			String collateralStatus = objvlPreprocessor.execCollateralHL(ifr, productCode);
			Log.consoleLog(ifr, "VLBkoffcCustomCode:createVLLoanDisburse->calling collateral api: " + collateralStatus);
			if (collateralStatus != null && collateralStatus.startsWith(RLOS_Constants.ERROR)) {

				String[] collateralIds = collateralStatus.split(":");

				return (collateralIds.length > 1) ? "error," + collateralIds[1] : "error," + collateralIds[0];
			} else {

				loanAccountNumber = objvlPreprocessor.execLoanAccountCreationHL(ifr, productCode);
				Log.consoleLog(ifr, "VLBkoffcCustomCode:createVLLoanDisburse->after calling loanAccountNumber api: "
						+ loanAccountNumber);
				if (loanAccountNumber != null && (loanAccountNumber.startsWith(RLOS_Constants.ERROR))) {

					String[] loanAccountNumberIds = loanAccountNumber.split(":");

					return (loanAccountNumberIds.length > 1) ? "error," + loanAccountNumberIds[1]
							: "error," + loanAccountNumberIds[0];
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "VLBkoffcCustomCode:createVLLoanDisburse->Exception: " + e);
			Log.errorLog(ifr, "Exception:/createVLLoanDisburse" + e);
		}
		return RLOS_Constants.ERROR;
	}

	public String documentsCheck(IFormReference ifr, String activityName) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String exStaff = "";
		String NAME = "";
		String NAME2 = "";
		String netSalary = "";
		String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
		List Result1 = ifr.getDataFromDB(exStaffQuery);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		// ifr.setValue("Net_Salary", netSalary);
		if (Integer.parseInt(Count1) == 0) {

			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND GENERATE = 'N' AND STAFF_TYPE='STAFF'";
			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

			List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid2) + "'";
		} else {
			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND GENERATE = 'N' AND STAFF_TYPE IN ('STAFF','EX-STAFF')";
			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);

			List<String> responseForGrid2 = listMandatoryDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid2) + "'";

		}

//		String getMandatoryDocumentsIn = "SELECT NAME,MANDATORY from SLOS_HL_DOCUMENTS where GENERATE='Y' AND ENABLE='N' order by name";
		String getMandatoryDocumentsIn = "SELECT NAME , MANDATORY FROM SLOS_HL_DOCUMENTS WHERE GENERATE='U' AND ENABLE='N' ORDER BY NAME";
		List<List<String>> listMandatoryDocumentsIn = ifr.getDataFromDB(getMandatoryDocumentsIn);

		List<String> responseForGrid2In = listMandatoryDocumentsIn.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME1 = "'" + String.join("','", responseForGrid2In) + "'";
		
		Log.consoleLog(ifr, "NAME1===>" + NAME1);
		
		
		String getMandatoryDocumentsS = "SELECT NAME from SLOS_HL_DOCUMENTS where enable='Y' AND GENERATE = 'S'";
		List<List<String>> listMandatoryDocumentsS = ifr.getDataFromDB(getMandatoryDocumentsS);

		List<String> responseForGrid21 = listMandatoryDocumentsS.stream().map(doc -> doc.get(0))
				.collect(Collectors.toList());

		String NAME4 = "'" + String.join("','", responseForGrid21) + "'";
		
		if (activityName.contains("Staff_HL_Branch_Maker")) {

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

			String queryforDocGrid1 = "SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_hl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + ")\r\n" + "  )";
			Log.consoleLog(ifr, "queryforDocGrid1==>" + queryforDocGrid1);
			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);
			Log.consoleLog(ifr, "responseForGrid1==>" + responseForGrid1);
			String[] Columnheading2 = new String[] { "Document Name", "Mandatory" };
			ifr.clearTable("Inward_Doc_Details_Grid");

			String queryforDocGrid2 = "SELECT DISTINCT d.NAME, v1.MANDATORY, d.CREATEDDATETIME FROM SLOS_TRN_BKOF_DOCUMENTS d INNER JOIN slos_hl_documents v1 ON d.NAME = v1.NAME WHERE d.WINAME = '"
					+ processInstanceId + "' AND d.NAME IN (" + NAME1
					+ ") AND d.CREATEDDATETIME = (SELECT MAX(d2.CREATEDDATETIME) FROM SLOS_TRN_BKOF_DOCUMENTS d2 WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME) ORDER BY d.NAME";

			Log.consoleLog(ifr, "queryforDocGrid2==>" + queryforDocGrid2);
			List<List<String>> responseForGrid2 = ifr.getDataFromDB(queryforDocGrid2);
			responseForGrid2 = ifr.getDataFromDB(queryforDocGrid2);
			Log.consoleLog(ifr, "responseForGrid2==>" + responseForGrid2);
			// String[] Columnheading2 = new String[] { "Document Name", "Mandatory" };
			ifr.clearTable("Inward_Doc_Details_Grid");
			ifr.clearTable("Inward_Doc_DetailsIn_Grid");

			JSONArray arrayRes = new JSONArray();
			JSONArray arrayRes1 = new JSONArray();

			// Create JSON array to hold document info
			arrayRes1 = new JSONArray();

			/* ---------- 2. Prepare Grid JSON using NAME + MANDATORY ---------- */

			Set<String> returnedNames = new HashSet<>();

			if (responseForGrid2 != null) {
				for (List<String> row : responseForGrid2) {
					returnedNames.add(row.get(0)); // d.NAME
				}
			}
			Log.consoleLog(ifr, "responseForGrid2==>" + responseForGrid2);

			// Check if both 'a' and 'b' exist
//			boolean containsA = returnedNames.contains("APPLICATION");
//			boolean containsB = returnedNames.contains("INSPECTION");

			Set<String> uniqueDocNames = new HashSet<>();
			if (responseForGrid2 == null || responseForGrid2.isEmpty()) {
				Log.consoleLog(ifr, "listMandatoryDocumentsIn==>" + listMandatoryDocumentsIn);
				for (List<String> doc : listMandatoryDocumentsIn) {
					

					String docName = doc.get(0);
					String mandatoryFlag = doc.get(1); // Y / N// NAME

					if (uniqueDocNames.add(docName)) {

						JSONObject docObj = new JSONObject();
						docObj.put("Document Name", docName);

						if ("Y".equalsIgnoreCase(mandatoryFlag)) {
							docObj.put("Mandatory", "Yes");
						} else {
							docObj.put("Mandatory", "No");
						}

						arrayRes1.add(docObj);

						Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + docObj.toString());
					}

				}
			} else {

				// Set<String> uniqueDocNames = new HashSet<>();
				for (List<String> doc : responseForGrid2) {

					String docName = doc.get(0);
					String mandatoryFlag = doc.get(1);

					if (uniqueDocNames.add(docName)) {

						JSONObject docObj = new JSONObject();
						docObj.put("Document Name", docName);

						if ("Y".equalsIgnoreCase(mandatoryFlag)) {
							docObj.put("Mandatory", "Yes");
						} else {
							docObj.put("Mandatory", "No");
						}

						String uploadedDate = doc.get(2);

						if (uploadedDate != null && !uploadedDate.trim().isEmpty()) {
							docObj.put("Uploaded Date", uploadedDate);
						} else {
							docObj.put("Uploaded Date", "");
						}

						arrayRes1.add(docObj);

						Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + docObj.toString());
					}
				}
			}

			Log.consoleLog(ifr, "All Mandatory Documents ==> " + NAME1);

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
					Log.consoleLog(ifr, "Inward_Doc_Details_Grid==>" + obj1);
//				obj1.put("Document Name", row.get(0));
//				obj1.put("Mandatory", "Yes");
					arrayRes.add(obj1);
				}
			}
			Log.consoleLog(ifr, "Json Array " + arrayRes);
			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes);
			ifr.addDataToGrid("Inward_Doc_Details_Grid", arrayRes1);
			return "SUCCESS";
		}

		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Checker")
				|| activityName.equalsIgnoreCase("Staff_HL_Sanction")
				|| activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")
				|| activityName.equalsIgnoreCase("Staff_HL_RO_Maker")
				|| activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {

			String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

			cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");

			String queryforDocGrid1 = "  SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_hl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + "," + NAME1
					+ ")\r\n" + "  )";

			Log.consoleLog(ifr, "queryforDocGrid1 ==> " + queryforDocGrid1);

			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);

			Log.consoleLog(ifr, "responseForGrid1 ==> " + responseForGrid1);

			/* ---------- Prepare NAME for further IN clause usage ---------- */
			List<String> responseForGrid3 = responseForGrid1.stream().map(doc -> doc.get(0)) // NAME
					.collect(Collectors.toList());

			NAME = "'" + String.join("','", responseForGrid3) + "'";

			ifr.clearTable("Inward_Doc_Details_Grid");
			JSONArray arrayRes1 = new JSONArray();
			String lsr = "";
			String valReport = "";

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
					} else {
						obj1.put("Mandatory", "No");
					}
					obj1.put("Uploaded Date", uploadedDate);

					arrayRes1.add(obj1);

					Log.consoleLog(ifr, "Inward_Doc_Details_Grid ==> " + obj1.toString());
				}

			}
			Log.consoleLog(ifr, "Json Array " + arrayRes1);

			ifr.addDataToGrid("Inward_Doc_DetailsIn_Grid", arrayRes1);
			ifr.setStyle("Inward_Doc_Details_Grid", "visible", "false");
			return "SUCCESS";

		}
		if (activityName.equalsIgnoreCase("Staff_HL_Post_Sanction")) {

			String Query2 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
					+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
					+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
					+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query2);

			cf.mExecuteQuery(ifr, Query2, "SLOS_TRN_BKOF_DOCUMENTS ");

			String queryforDocGrid1 = "  SELECT DISTINCT \r\n" + "    d.NAME, v1.MANDATORY, d.createddatetime\r\n"
					+ "FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "INNER JOIN slos_hl_documents v1 ON d.name = v1.name\r\n"
					+ "WHERE d.WINAME = '" + processInstanceId + "'\r\n"
					+ "  AND (d.NAME = 'NF_425' OR d.NAME = v1.name)\r\n" + "  AND d.CREATEDDATETIME = (\r\n"
					+ "      SELECT MAX(CREATEDDATETIME)\r\n" + "      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME AND d2.NAME IN (" + NAME + "," + NAME1
					+ "," +NAME4+ " )\r\n" + "  )";
			Log.consoleLog(ifr, "queryforDocGrid1 ==> " + queryforDocGrid1);

			List<List<String>> responseForGrid1 = ifr.getDataFromDB(queryforDocGrid1);

			Log.consoleLog(ifr, "responseForGrid1 ==> " + responseForGrid1);

			/* ---------- Clear Grid ---------- */
			ifr.clearTable("Inward_Doc_Details_Grid");

			JSONArray arrayRes = new JSONArray();
			JSONArray arrayRes1 = new JSONArray();

			/* ---------- Prepare NAME for reuse ---------- */

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
					} else {
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
			// ifr.setStyle("Inward_Doc_Details_Grid", "visible", "false");
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

	public String mGenDoc(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryForCount = "";
		String Count1 = "";
		List<List<String>> listgetAllDocuments = new ArrayList<>();
		String activityName = ifr.getActivityName();
		Log.consoleLog(ifr, "activityName====>" + activityName);
		String docGen="";
		String Query = "SELECT GENERATE_DOC  from SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId
				+ "'";
		List<List<String>> Output3 = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "Output3 query===>" + Query);
		Log.consoleLog(ifr, "Output3===>" + Output3);
		if (Output3.size() > 0 && Output3.get(0).size() > 0 && !Output3.get(0).get(0).toString().trim().isEmpty()) {
			docGen = Output3.get(0).get(0).toString().trim();
		}
		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker")) {
			checkLosBasicInfo(ifr, processInstanceId);

			String genDoc = mGenerateTwoDoc(ifr);
			if (genDoc.contains("error")) {
				return genDoc;
			}
			
			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where GENERATE = 'B'";
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
					+ "	COUNT(*) OVER() AS total_count FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "	WHERE d.WINAME = '" + processInstanceId + "'\r\n"
					+ "   AND d.NAME IN (" +NAME+ ")\r\n"
					+ "		   AND d.CREATEDDATETIME = (\r\n" + " SELECT MAX(CREATEDDATETIME)\r\n"
					+ "		      FROM SLOS_TRN_BKOF_DOCUMENTS d2\r\n"
					+ "		      WHERE d2.NAME = d.NAME AND d2.WINAME = d.WINAME\r\n" + "		  )";
			Log.consoleLog(ifr, "queryforDocGrid==>" + queryforDocGrid);
			List<List<String>> responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			//Log.consoleLog(ifr, "responseForGrid==>" + responseForGrid);
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
			Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryforDocGrid);
			responseForGrid = ifr.getDataFromDB(queryforDocGrid);
			if(!responseForGrid.isEmpty())
			{
				Count1 =responseForGrid.get(0).get(3);
			}
			//Count1 = Result1.toString().replace("[", "").replace("]", "");
			if (Integer.parseInt(Count1) == 2
					&& activityName.equals("Staff_HL_Branch_Maker")) {
				if ("NO".equalsIgnoreCase(docGen)) {
					return "SUCCESS";
				}
				return "SUCCESS";
			} else {
				return "FAILED";
			}
		} else if (activityName.equalsIgnoreCase("Staff_HL_Sanction")
				|| activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")
				|| activityName.equalsIgnoreCase("Staff_HL_Post_Sanction")) {

			String subProductCode = "";
			String scheduleCode = "";
			String purpose = "";
			String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
			List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
			Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
			if (!listqueryproductandScheduleCode.isEmpty()) {
				subProductCode = listqueryproductandScheduleCode.get(0).get(0);
				scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
				purpose = listqueryproductandScheduleCode.get(0).get(2);
			}
			Count1 = "";

			if (activityName.equals("Staff_HL_Sanction") || activityName.equals("Staff_HL_CO_Sanction")) {
				String getAllNewDocuments = "select * from SLOS_HL_DOCUMENTS where ENABLE='Y' and generate='S'";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_HL_DOCUMENTS where GENERATE='S' AND ENABLE='Y'";

				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
				
			}
			else if (activityName.equals("Staff_HL_Post_Sanction")) 
			{
				String getAllNewDocuments = "select * from SLOS_HL_DOCUMENTS where ENABLE='Y' and generate='P'";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);

				queryForCount = "SELECT COUNT(DISTINCT NAME) from SLOS_HL_DOCUMENTS where GENERATE='P' AND ENABLE='Y'";

				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
			}

			List<String> responseForGrid1 = listgetAllDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			String NAME = "'" + String.join("','", responseForGrid1) + "'";

			if (activityName.equals("Staff_HL_Sanction") || activityName.equals("Staff_HL_CO_Sanction")
					|| activityName.equals("Staff_HL_Post_Sanction")) {
				if (activityName.equals("Staff_HL_Sanction") || activityName.equals("Staff_HL_CO_Sanction")) {
					Log.consoleLog(ifr, "Inside Staff Sanction====>");
					Date currentDate = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
					String formattedDate = dateFormat.format(currentDate);

					String updateQuerySD = "UPDATE slos_trn_loansummary SET sanction_date = '" + formattedDate.trim()
							+ "' WHERE winame = '" + processInstanceId + "'";

					ifr.saveDataInDB(updateQuerySD);
				}
				checkLosBasicInfo(ifr, processInstanceId);
				getDocGeneration(ifr, processInstanceId, activityName);
				String Query1 = "INSERT INTO SLOS_TRN_BKOF_DOCUMENTS (NAME, DOCSTATUS, CREATEDDATETIME, WINAME)\r\n"
						+ "SELECT d.NAME, d.DOCSTATUS, d.CREATEDDATETIME, f.NAME\r\n" + "FROM PDBDOCUMENT d\r\n"
						+ "JOIN PDBDOCUMENTCONTENT dc ON d.DOCUMENTINDEX = dc.DOCUMENTINDEX\r\n"
						+ "JOIN PDBFOLDER f ON dc.PARENTFOLDERINDEX = f.FOLDERINDEX\r\n" + "WHERE f.NAME = '"
						+ processInstanceId + "' AND d.NAME IN (" + NAME + " )";

				Log.consoleLog(ifr, "SLOS_TRN_BKOF_DOCUMENTS===>" + Query1);

//				cf.mExecuteQuery(ifr, Query1, "SLOS_TRN_BKOF_DOCUMENTS ");
//
//				String updateQuery = "UPDATE SLOS_TRN_BKOF_DOCUMENTS SET DOCTYPE='SANCTION' where winame='"
//						+ processInstanceId + "'";
//				Log.consoleLog(ifr, " updateQuery SLOS_TRN_BKOF_DOCUMENTS===>" + updateQuery);
//				ifr.saveDataInDB(updateQuery);

				String queryforDocGrid = "SELECT DISTINCT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId + "'\r\n"
						+ "      AND NAME IN (" + NAME + ")\r\n" + ") \r\n" + "WHERE rn = 1";
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
				// Aprvalue(ifr);
			}
			if (Integer.parseInt(Count1) == 3
					&& (activityName.equals("Staff_HL_Sanction") || activityName.equals("Staff_HL_CO_Sanction"))) {
				return "SUCCESS";
			} else if (Integer.parseInt(Count1) >= 14 && activityName.equals("Staff_HL_Post_Sanction")) {
				return "SUCCESS";
			} 
			else if ("NO".equalsIgnoreCase(docGen)) {
				return "SUCCESS";
			}
			else {
				return "FAILED";
			}
		}
		return "FAILED";

	}

	private void checkLosBasicInfo(IFormReference ifr, String processInstanceId) {
		String customerId = "";
		String gender = "";
		String fname = "";
		String lname = "";
		String dateOnly = "";
		String age = "";
		String mname = "";
		String mobilenum = "";
		String query = "select CUSTOMERID,customersex,dateofbirth,FLOOR(MONTHS_BETWEEN(SYSDATE, dateofbirth) / 12),customerfirstname,customermiddlename,customerlastname,mobilenumber from los_trn_customersummary where  WINAME='"
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
			age = list.get(0).get(3).toString();
			ifr.setValue("AGE_VL", age);
			// this code should be reverted when giving build to UAT
			LocalDateTime dateTime = LocalDateTime.parse(list.get(0).get(2), inputFormatter);
			dateOnly = dateTime.toLocalDate().toString();
			ifr.setValue("DATEOFBIRTH", dateOnly);
			Log.consoleLog(ifr, "***********");
			fname = list.get(0).get(4);
			mname = list.get(0).get(5);
			lname = list.get(0).get(6);
			mobilenum = list.get(0).get(7);
			// ifr.setValue("DATEOFBIRTH", list.get(0).get(2));
		}

		String basicDetailInsertQuery = "INSERT INTO LOS_NL_BASIC_INFO (PID, APPLICANTTYPE, ENTITYTYPE, EXISTINGCUSTOMER, CUSTOMERID, F_KEY, INSERTIONORDERID, FULLNAME, STAFFMEMBER, CONSIDERELIGIBILITY, STATUS, MOBILENUMBER) "
				+ "SELECT '" + processInstanceId + "', 'B', 'I', 'Yes', '" + customerId + "', "
				+ "(SELECT NVL(MAX(F_KEY),0) + 1 FROM LOS_NL_BASIC_INFO), "
				+ "(SELECT NVL(MAX(INSERTIONORDERID),0) - 50 FROM LOS_NL_BASIC_INFO), " + "'" + fname + " " + mname
				+ " " + lname + "', 'Y', 'Yes', 'IN-PROGRESS', '" + mobilenum + "' " + "FROM DUAL "
				+ "WHERE NOT EXISTS (SELECT 1 FROM LOS_NL_BASIC_INFO WHERE PID = '" + processInstanceId + "')";

		Log.consoleLog(ifr, "basicDetailInsertQuery ==>" + basicDetailInsertQuery);
		ifr.saveDataInDB(basicDetailInsertQuery);
	}

	public String Aprvalue(IFormReference ifr) {
		try {
			Log.consoleLog(ifr, "inside Aprvalue ");
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String queryforRIinfor = "SELECT RATE_OF_INTEREST FROM SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME='" + PID
					+ "'";
			Log.consoleLog(ifr, "inside Aprvalue  queryforRIinfor" + queryforRIinfor);
			List<List<String>> result1 = ifr.getDataFromDB(queryforRIinfor);

			String queryforloaninfor = "select '9000',APP_LOAN_AMT_VL,APP_LOAN_TENURE_VL,ROI from SLOS_STAFF_TRN where WINAME = '"
					+ PID + "'";
			Log.consoleLog(ifr, "inside Aprvalue  queryforloaninfor" + queryforloaninfor);
			List<List<String>> result = ifr.getDataFromDB(queryforloaninfor);
			// if
			// ((result.get(0).get(0)).isEmpty()||result.get(0).get(0).equalsIgnoreCase(""))
			// {
			double calculateAPR = calculateAPR(ifr, Double.parseDouble(result.get(0).get(0)),
					Integer.parseInt(result.get(0).get(1)), Integer.parseInt(result1.get(0).get(0)),
					Double.parseDouble(result.get(0).get(3)));
			Log.consoleLog(ifr, "inside Aprvalue  calculateAPR" + calculateAPR);
			String deletequery = "delete from los_nl_aprval where PID='" + PID + "'";
			Log.consoleLog(ifr, "inside deletequeryforapr  calculateAPR" + deletequery);
			cf.mExecuteQuery(ifr, deletequery, "Logs for deletequery  " + PID);
			String apr = Double.toString(calculateAPR);
			String insertqueryforapr = "insert into los_nl_aprval (PID,APRVALUE) values ('" + PID + "','" + apr + "')";
			// String insertqueryforapr =
			// ConfProperty.getQueryScript("APRINSERT").replaceAll("#PID#",
			// PID).replaceAll("#APR#", Double.toString(calculateAPR));
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

		if (value.equalsIgnoreCase("Staff_HL_Post_Sanction") && esignStatus.equalsIgnoreCase("YES")) {

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
			// Aprvalue(ifr);
			String sqlCharge = "SELECT A.FixedFee,        A.TaxPercentage FROM SLOS_M_FEE_CHARGES A INNER JOIN SLOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode = B.FeeCode WHERE A.IsActive = 'Y' AND B.SCHEMEID = 'SHL1' AND (A.MINTENURE <= TO_NUMBER('"
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

	private void getDocGeneration(IFormReference ifr, String processInstanceId, String activityName) {

		if (activityName.equals("Staff_HL_Sanction") || activityName.equals("Staff_HL_CO_Sanction")) {
			docGen(ifr, "PROCESS_NOTE", "STAFF_HL");
			docGen(ifr, "CUSTOMER_SANCTION_LETTER", "STAFF_HL");
			docGen(ifr, "BRANCH_SANCTION", "STAFF_HL");
		}
		else if (activityName.equals("Staff_HL_Post_Sanction")) {
			docGen(ifr, "APPENDIX_25", "STAFF_HL");
			docGen(ifr, "APPENDIX_9", "STAFF_HL");
			docGen(ifr, "NF_894", "STAFF_HL");
			docGen(ifr, "APPENDIX_18_18B", "STAFF_HL");
			docGen(ifr, "APPENDIX_10", "STAFF_HL");
			docGen(ifr, "APPENDIX_12", "STAFF_HL");
			docGen(ifr, "APPENDIX_11_DISBURSEMENT_REQUEST", "STAFF_HL");
			docGen(ifr, "KFS", "STAFF_HL");
			docGen(ifr, "APPENDIX_26", "STAFF_HL");
			docGen(ifr, "APPENDIX_4_HL_AGREEMENT_STAFF", "STAFF_HL");
			docGen(ifr, "APPENDIX_6", "STAFF_HL");
			docGen(ifr, "APPENDIX_7_LEDTD", "STAFF_HL");
			docGen(ifr, "NF_482", "STAFF_HL");
			docGen(ifr, "NF_803", "STAFF_HL");
		}
	}

	public String mGenerateTwoDoc(IFormReference ifr) {
		docGen(ifr, "NF_523_HL_APPLICATION", "STAFF_HL");
		docGen(ifr, "PROPERTY_INSPECTION_REPORT", "STAFF_HL");
		return "success";
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

	public String tenureCheck(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String ageOfBorrower = "";
		String query = "select FLOOR(MONTHS_BETWEEN(SYSDATE, dateofbirth) / 12) from los_trn_customersummary where  WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "getCustomerIDFromPAPLMaster veh loan===>" + query);
		// List<List<String>> list = cf.mExecuteQuery(ifr, query,
		// "getCustomerIDFromPAPLMaster:");
		List<List<String>> list = ifr.getDataFromDB(query);
		if (!list.isEmpty()) {
			ageOfBorrower = list.get(0).get(0);
		}
		if (ageOfBorrower != null && !ageOfBorrower.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
			int age = Integer.parseInt(ageOfBorrower);
			int tenure = Integer.parseInt(value);
			String tenureNotValid = isTenureValid(age, tenure);
			if (tenureNotValid.contains("error")) {
				return tenureNotValid;
			}
		}

		return "success";
	}

	public String isTenureValid(int age, int tenure) {

		// Rule 1: Age cannot exceed 75 years
		if (age > 75) {
			return "error,tenure can't be more than 75 years";
		}

		// Rule 2: Tenure cannot exceed 30 years
		if (tenure > 360) {
			return "error,tenure can't be more than 360 months";
		}

		// Rule 3: Age + Tenure cannot exceed 75 years
		if (((age * 12) + tenure) > 900) {
			return "error,Kindly reduce the tenure as it crosses 75 years with respect to your age";
		}

		// All rules satisfied
		return "success";
	}

	public String tenureCheckR(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String ageOfBorrower = "";
		String query = "select FLOOR(MONTHS_BETWEEN(SYSDATE, dateofbirth) / 12) from los_trn_customersummary where  WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "getCustomerIDFromPAPLMaster veh loan===>" + query);
		// List<List<String>> list = cf.mExecuteQuery(ifr, query,
		// "getCustomerIDFromPAPLMaster:");
		List<List<String>> list = ifr.getDataFromDB(query);
		if (!list.isEmpty()) {
			ageOfBorrower = list.get(0).get(0);
		}
		if (ageOfBorrower != null && !ageOfBorrower.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
			int age = Integer.parseInt(ageOfBorrower);
			int tenure = Integer.parseInt(value);
			String tenureNotValid = isTenureValidR(age, tenure);
			if (tenureNotValid.contains("error")) {
				return tenureNotValid;
			}
		}

		return "success";
	}

	public String isTenureValidR(int age, int tenure) {

		// Rule 1: Age cannot exceed 75 years
		if (age > 75) {
			return "error,tenure can't be more than 75 years";
		}

		// Rule 2: Tenure cannot exceed 30 years
		if (tenure > 120) {
			return "error,tenure can't be more than 120 months";
		}

		// Rule 3: Age + Tenure cannot exceed 75 years
		if (((age * 12) + tenure) > 900) {
			return "error,Kindly reduce the tenure as it crosses 75 years with respect to your age";
		}

		// All rules satisfied
		return "success";
	}

	public String mAccClickFetchFeeCharges(IFormReference ifr, String Control, String Event, String value) {
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String reqLoanAmt = "";
			String reqLoanAmtQuery = "SELECT REQ_AMT_TOT_PLD FROM slos_staff_home_trn WHERE WINAME='" + PID + "'";
			List<List<String>> reqLoanAmtRes = cf.mExecuteQuery(ifr, reqLoanAmtQuery,
					"mAccClickFetchFeeCharges:: Fetching Requested Loan amount");
			if (reqLoanAmtRes.size() > 0) {
				reqLoanAmt = reqLoanAmtRes.get(0).get(0);
			}
			String query = "SELECT FeesDescription, FeeType, FixedFee, FeePercentage, TaxPercentage, FeeCode, MinAmt, MaxAmt, Appropritation FROM SLOS_STAFF_HL_FEESMASTER";
			List<List<String>> queryResult = cf.mExecuteQuery(ifr, query, "FETCHFEECHARGES All Data:");
			if (queryResult.size() > 0) {
				ifr.clearTable("ALV_FEE_CHARGES");
				JSONArray gridArray = new JSONArray();
				Log.consoleLog(ifr, "Result size:::" + queryResult.size());

				for (int i = 0; i < queryResult.size(); i++) {
					String fixedFee = queryResult.get(i).get(2);
					String feeCode = queryResult.get(i).get(5);
					if (feeCode.equalsIgnoreCase("CHR45")) {
						String cbCount = "0";
						String cbTypeQuery = "select COUNT(*) from los_nl_cb_details WHERE PID='" + PID
								+ "' AND cb_type='CB'";
						List<List<String>> cbTypeQueryRes = cf.mExecuteQuery(ifr, cbTypeQuery,
								"mAccClickFetchFeeCharges:: Fetching total cibil count");
						if (cbTypeQueryRes.size() > 0) {
							cbCount = cbTypeQueryRes.get(0).get(0);
						}
						double baseAmount = Integer.parseInt(cbCount) * Double.parseDouble(fixedFee);
						double totalFee = baseAmount + (baseAmount * 0.18);// gst 18%
						int finalTotalFee = (int) totalFee;
						fixedFee = String.valueOf(finalTotalFee);
					} else if (feeCode.equalsIgnoreCase("CHR46")) {
						String cbCount = "0";
						String cbTypeQuery = "select COUNT(*) from los_nl_cb_details WHERE PID='" + PID
								+ "' AND cb_type='EX'";
						List<List<String>> cbTypeQueryRes = cf.mExecuteQuery(ifr, cbTypeQuery,
								"mAccClickFetchFeeCharges:: Fetching total EXPERIAN count");
						if (cbTypeQueryRes.size() > 0) {
							cbCount = cbTypeQueryRes.get(0).get(0);
						}
						double baseAmount = Integer.parseInt(cbCount) * Double.parseDouble(fixedFee);
						double totalFee = baseAmount + (baseAmount * 0.18);// gst 18%
						int finalTotalFee = (int) totalFee;
						fixedFee = String.valueOf(finalTotalFee);
					} else if (feeCode.equalsIgnoreCase("CHR47")) {
						String cbCount = "0";
						String cbTypeQuery = "select COUNT(*) from los_nl_cb_details WHERE PID='" + PID
								+ "' AND cb_type='HM'";
						List<List<String>> cbTypeQueryRes = cf.mExecuteQuery(ifr, cbTypeQuery,
								"mAccClickFetchFeeCharges:: Fetching total Highmark count");
						if (cbTypeQueryRes.size() > 0) {
							cbCount = cbTypeQueryRes.get(0).get(0);
						}
						double baseAmount = Integer.parseInt(cbCount) * Double.parseDouble(fixedFee);
						double totalFee = baseAmount + (baseAmount * 0.18);// gst 18%
						int finalTotalFee = (int) totalFee;
						fixedFee = String.valueOf(finalTotalFee);
					} else if (feeCode.equalsIgnoreCase("CHR48")) {
						String cbCount = "0";
						String cbTypeQuery = "select COUNT(*) from los_nl_cb_details WHERE PID='" + PID
								+ "' AND cb_type='EF'";
						List<List<String>> cbTypeQueryRes = cf.mExecuteQuery(ifr, cbTypeQuery,
								"mAccClickFetchFeeCharges:: Fetching total equifax count");
						if (cbTypeQueryRes.size() > 0) {
							cbCount = cbTypeQueryRes.get(0).get(0);
						}
						double baseAmount = Integer.parseInt(cbCount) * Double.parseDouble(fixedFee);
						double totalFee = baseAmount + (baseAmount * 0.18);// gst 18%
						int finalTotalFee = (int) totalFee;
						fixedFee = String.valueOf(finalTotalFee);
					} else if (fixedFee.equalsIgnoreCase("Actual Amount")) {
						// Actual amount should be calculated when StateFeesCharges Table will be
						// created
						fixedFee = "0";
					} else if (fixedFee.equalsIgnoreCase("Seven Per Each")) {
						// Actual need document count * 7 is need to be added here in future
						fixedFee = "0";
					} else if (fixedFee.equalsIgnoreCase("According Signatory")) {
						String coApplicantCount = "0";
						String coApplicantCountQ = "select count(*) from los_nl_basic_info where pid='" + PID + "'";
						List<List<String>> coApplicantCountRes = cf.mExecuteQuery(ifr, coApplicantCountQ,
								"mAccClickFetchFeeCharges:: Fetching Co Applicant count");
						if (coApplicantCountRes.size() > 0) {
							coApplicantCount = coApplicantCountRes.get(0).get(0);
						}
						fixedFee = String.valueOf(5 * Integer.parseInt(coApplicantCount));

					} else if (fixedFee.equalsIgnoreCase("According Tenure")) {
						String reqTenure = "0";
						String reqTenureQury = "select APP_LOAN_TENURE_VL from slos_staff_trn where winame='" + PID
								+ "'";
						List<List<String>> reqTenureQuryRes = cf.mExecuteQuery(ifr, reqTenureQury,
								"mAccClickFetchFeeCharges:: Fetching Requested Tenure");
						if (reqTenureQuryRes.size() > 0) {
							reqTenure = reqTenureQuryRes.get(0).get(0);
						}
						if (reqTenure == null || reqTenure.trim().isEmpty() || !reqTenure.matches("\\d+")) {
							reqTenure = "25";
						}
						long tenureYears = Long.parseLong(reqTenure) / 12;
						long fixedFeeLong = tenureYears > 3 ? (25 + 10 * (tenureYears - 3)) : 25;
						fixedFee = String.valueOf(fixedFeeLong);
					}
					BigDecimal Amount = new BigDecimal("0");
					JSONObject gridObject = new JSONObject();
					gridObject.put("QNL_FEE_CHARGES_ProductType", "Staff Home Loan");
					gridObject.put("QNL_FEE_CHARGES_Fees_Description", queryResult.get(i).get(0));
					gridObject.put("QNL_FEE_CHARGES_Fees_Type", queryResult.get(i).get(1));
					gridObject.put("QNL_FEE_CHARGES_GST", queryResult.get(i).get(4));
					gridObject.put("QNL_FEE_CHARGES_FeeCode", queryResult.get(i).get(5));
					gridObject.put("QNL_FEE_CHARGES_Fees_Appropriation", queryResult.get(i).get(8));
					if (queryResult.get(i).get(1).equalsIgnoreCase("Flat")) {
						Amount = new BigDecimal(fixedFee);
					} else if (queryResult.get(i).get(1).equalsIgnoreCase("Percentage")) {
						BigDecimal per = new BigDecimal(
								queryResult.get(i).get(3).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(3));
						BigDecimal loanAmt = new BigDecimal(reqLoanAmt.equalsIgnoreCase("") ? "0" : reqLoanAmt);
						Amount = (loanAmt.multiply(per)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
					}
					gridObject.put("QNL_FEE_CHARGES_Amount", String.valueOf(Amount));
					gridObject.put("QNL_FEE_CHARGES_ConcessionAmt", String.valueOf(Amount));
					BigDecimal GSTAmt = new BigDecimal(
							queryResult.get(i).get(4).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(4));
					BigDecimal Gsttotal = (Amount.multiply(GSTAmt)).divide(new BigDecimal("100"), 2,
							RoundingMode.HALF_UP);
					BigDecimal total = Amount.add(Gsttotal);
					gridObject.put("QNL_FEE_CHARGES_fee_amount", String.valueOf(total));
					BigDecimal MinAmount = new BigDecimal(
							queryResult.get(i).get(6).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(6));
					BigDecimal MaxAmount = new BigDecimal(
							queryResult.get(i).get(7).equalsIgnoreCase("") ? "0" : queryResult.get(i).get(7));
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
		return "";
	}

	public String PopulateCibilLiabilitiesSHL(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "inside PopulateCibilExperianLiabilities: ");
		String responseBody = "";
		JSONParser parser1 = new JSONParser();
		String productCode = "HL";
		ifr.clearTable("ALV_AL_LIAB_VAL");
		JSONArray jsonarr1 = new JSONArray();
		Log.consoleLog(ifr, "productCode=======>" + productCode);
		try {
			String allCoAppQuery = "SELECT INSERTIONORDERID FROM los_nl_basic_info WHERE INSERTIONORDERID IN (select distinct (substr(APPLICANT_TYPE, instr(APPLICANT_TYPE, '~') + 1)) from los_integration_reqres where TRANSACTION_ID = '"
					+ PID + "' AND API_NAME LIKE '%Transunion_Consumer%')";
			List<List<String>> allCoAppData = cf.mExecuteQuery(ifr, allCoAppQuery, "Feching all the co borrowers");
			if (!allCoAppData.isEmpty()) {
				String allLoanTypeQuery = "SELECT distinct trim(loancode), loantype FROM LOS_M_LOAN_TYPE";
				List<List<String>> allLoanTypeRes = cf.mExecuteQuery(ifr, allLoanTypeQuery,
						"Feching all the Loan types");
				Log.consoleLog(ifr, "allLoanTypeRes=======>" + allLoanTypeRes);
				HashMap<String, String> loanTypeMap = new HashMap<>();
				if (!allLoanTypeRes.isEmpty()) {
					for (int z = 0; z < allLoanTypeRes.size(); z++) {
						loanTypeMap.put(allLoanTypeRes.get(z).get(0), allLoanTypeRes.get(z).get(1));
					}
				}
				for (int k = 0; k < allCoAppData.size(); k++) {
					String ApplicantType = allCoAppData.get(k).get(0);
					String responseBodyCIBIL = "";
					try {
						String CibilResQuery = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME ='Transunion_Consumer' AND TRANSACTION_ID ='"
								+ PID + "' AND APPLICANT_TYPE like '%" + ApplicantType + "%'";
						List<List<String>> CibilResponse = cf.mExecuteQuery(ifr, CibilResQuery,
								"Execute query for fetching CIBIL Response ");
						if (!CibilResponse.isEmpty()) {

							String CibilResponseStr = CibilResponse.get(0).get(0);
							Log.consoleLog(ifr, "CibilResponseStr From Table==>" + CibilResponseStr);
							JSONObject CibilResponseObj = (JSONObject) parser1.parse(CibilResponseStr);
							responseBodyCIBIL = CibilResponseObj.toString();
							Log.consoleLog(ifr, "Cibil Response from table Parsed ::");
						}
					} catch (Exception e) {
						Log.consoleLog(ifr, "Exception in CibilResQuery :" + e);
						JSONObject message = new JSONObject();
						message.put("showMessage",
								cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
						return message.toString();
					}
					// }
					String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "ACCTTYPE");
					Log.consoleLog(ifr, "excludedEMIAccnts=>" + excludedEMIAccnts);
					String excludedOwners = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "OWNERTYPE");
					Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);
					String BureauScore = "";
					int cibilScore = 0;
					String paymentHistory = "";
					String suitFiled = "";
					String accountType = "";
					String dateClosed = "Y";
					String currentBalance = "";
					String memberShortName = "";
					String dateOpened = "";
					String formattedDate = "";
					String highCreditAmount = "";
					String amountOverdue = "";
					String accountNumber = "";
					String ownershipIndicator = "";
					String Scores = "";
					String accounts = "";
					String emiAmount = "0";
					int totalEmiAmnt = 0;
					int totalNonEmiCount = 0;

					JSONObject OutputJSON2 = (JSONObject) parser1.parse(responseBodyCIBIL);
					JSONObject resultObj1 = new JSONObject(OutputJSON2);

					Log.consoleLog(ifr, "resultObj==>" + resultObj1);

					String body = resultObj1.get("body").toString();
					JSONObject bodyObj = (JSONObject) parser1.parse(body);

					String ControlData = bodyObj.get("controlData").toString();
					Log.consoleLog(ifr, "ControlData==>" + ControlData);

					JSONObject ControlDataObj = (JSONObject) parser1.parse(ControlData);
					String ControlDataStatus = ControlDataObj.get("success").toString();

					Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);

					String consumerCreditData = bodyObj.get("consumerCreditData").toString();
					Log.consoleLog(ifr, "consumerCreditData==>" + consumerCreditData);
					JSONArray consumerCreditDataJSON = (JSONArray) parser1.parse(consumerCreditData);
					if (!consumerCreditDataJSON.isEmpty()) {
						for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
							String InputString = consumerCreditDataJSON.get(i).toString();
							JSONObject consumerCreditDataJSONObj = (JSONObject) parser1.parse(InputString);
							Log.consoleLog(ifr, "consumerCreditDataJSON ==> " + i);
							Scores = consumerCreditDataJSONObj.get("scores").toString();
							Log.consoleLog(ifr, "Scores=" + Scores);

							try {
								accounts = consumerCreditDataJSONObj.get("accounts").toString();
								Log.consoleLog(ifr, "account=" + accounts);
							} catch (Exception e) {
								Log.consoleLog(ifr, "Exception/accounts=" + e);
							}

						}
					}

					Log.consoleLog(ifr, "Scores==>" + Scores);
					JSONArray ScoresJSON = (JSONArray) parser1.parse(Scores);

					if (!ScoresJSON.isEmpty()) {
						for (int i = 0; i < ScoresJSON.size(); i++) {
							String InputString = ScoresJSON.get(i).toString();
							JSONObject ScoresJSONObj = (JSONObject) parser1.parse(InputString);
							String transCIBILScore = ScoresJSONObj.get("score").toString();
							Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
							cibilScore = Integer.parseInt(transCIBILScore);
							Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
							BureauScore = String.valueOf(cibilScore);
							Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
							Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
						}
					}
					Log.consoleLog(ifr, "accounts==>" + accounts);

					if (!accounts.equalsIgnoreCase("")) {
						JSONArray accountsJSON = (JSONArray) parser1.parse(accounts);

						if (!accountsJSON.isEmpty()) {

							for (int i = 0; i < accountsJSON.size(); i++) {
								Log.consoleLog(ifr, "accountsJSON index:: " + i);

								String InputString = accountsJSON.get(i).toString();
								JSONObject accountsJSONObj = (JSONObject) parser1.parse(InputString);

								if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
									accountType = accountsJSONObj.get("accountType").toString();
									Log.consoleLog(ifr, "accountType==>" + accountType);
								} else {
									Log.consoleLog(ifr, "accountType tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "ownershipIndicator").equalsIgnoreCase(""))) {
									ownershipIndicator = accountsJSONObj.get("ownershipIndicator").toString();
									Log.consoleLog(ifr, "ownershipIndicator==>" + ownershipIndicator);
								} else {
									Log.consoleLog(ifr, "ownershipIndicator tag not available.");
								}
								if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
									currentBalance = accountsJSONObj.get("currentBalance").toString();
									Log.consoleLog(ifr, "currentBalance==>" + currentBalance);

									if (currentBalance.equalsIgnoreCase("")) {
										currentBalance = "";
									}

								} else {
									Log.consoleLog(ifr,
											"currentBalance tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "memberShortName").equalsIgnoreCase(""))) {
									memberShortName = accountsJSONObj.get("memberShortName").toString();
									Log.consoleLog(ifr, "memberShortName==>" + memberShortName);

									if (memberShortName.equalsIgnoreCase("")) {
										memberShortName = "";
									}

								} else {
									Log.consoleLog(ifr,
											"memberShortName tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
									dateOpened = accountsJSONObj.get("dateOpened").toString();
									Log.consoleLog(ifr, "dateOpened==>" + dateOpened);

									if (dateOpened.equalsIgnoreCase("")) {
										dateOpened = "";
									}
								} else {
									Log.consoleLog(ifr, "dateOpened tag not available for accountsJSON index ::" + i);
								}
								if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
									dateClosed = accountsJSONObj.get("dateClosed").toString();
									Log.consoleLog(ifr, "dateClosed==>" + dateClosed);

									if (!dateClosed.equalsIgnoreCase("")) {
										dateClosed = "N";
									}
									// dateClosed
								} else {
									dateClosed = "Y";
									Log.consoleLog(ifr, "dateClosed tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "highCreditAmount").equalsIgnoreCase(""))) {
									highCreditAmount = accountsJSONObj.get("highCreditAmount").toString();
									Log.consoleLog(ifr, "highCreditAmount==>" + highCreditAmount);

									if (highCreditAmount.equalsIgnoreCase("")) {
										highCreditAmount = "";
									}

								} else {
									Log.consoleLog(ifr,
											"highCreditAmount tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "amountOverdue").equalsIgnoreCase(""))) {
									amountOverdue = accountsJSONObj.get("amountOverdue").toString();
									Log.consoleLog(ifr, "amountOverdue==>" + amountOverdue);

									if (amountOverdue.equalsIgnoreCase("")) {
										amountOverdue = "";
									}

								} else {
									Log.consoleLog(ifr,
											"amountOverdue tag not available for accountsJSON index ::" + i);
								}

								if (!(cf.getJsonValue(accountsJSONObj, "accountNumber").equalsIgnoreCase(""))) {
									accountNumber = accountsJSONObj.get("accountNumber").toString();
									Log.consoleLog(ifr, "accountNumber==>" + accountNumber);

									if (accountNumber.equalsIgnoreCase("")) {
										accountNumber = "";
									}

								} else {
									Log.consoleLog(ifr,
											"accountNumber tag not available for accountsJSON index ::" + i);
								}

								String party_type = "";
								if (ApplicantType.contains("~")) {
									party_type = ApplicantType.split("~")[1];
								} else {
									party_type = ApplicantType;
								}
								Log.consoleLog(ifr, "Party Type==>" + party_type);
								String loanType = "";
								if (!accountType.trim().isEmpty()) {
									loanType = loanTypeMap.get(accountType.trim());
									Log.consoleLog(ifr, "Loan Type:: " + loanType);
								}

								JSONObject obj = new JSONObject();
								obj.put("QNL_AL_LIAB_VAL_LoanType", loanType);
								obj.put("QNL_AL_LIAB_VAL_ApplicantType", party_type);
								obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
								obj.put("QNL_AL_LIAB_VAL_Bank", memberShortName);
								obj.put("QNL_AL_LIAB_VAL_loanStartDate", dateOpened);
								obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", highCreditAmount);
								obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", currentBalance);
								obj.put("QNL_AL_LIAB_VAL_Overdue",
										amountOverdue.equalsIgnoreCase("") ? "0.00" : amountOverdue);
								obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", accountNumber);

								if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
									emiAmount = accountsJSONObj.get("emiAmount").toString();
									Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
									if (!emiAmount.equalsIgnoreCase("")) {
										obj.put("QNL_AL_LIAB_VAL_EMIAmt", emiAmount);
										totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
									} else {
										Log.consoleLog(ifr, "emiAmount tag available but empty");
										obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
										if (Integer.parseInt(currentBalance) > 0) {
											totalNonEmiCount++;
										}
									}
								} else {
									Log.consoleLog(ifr, "emiAmount tag not available.");

									if (Integer.parseInt(currentBalance) > 0) {
										totalNonEmiCount++;
									}
								}
								Log.consoleLog(ifr, "JSON obj RESULT::" + obj);

								boolean OwnerStatus = checkOwnerTypeFilterStatus(ifr, ownershipIndicator,
										excludedOwners);
								Log.consoleLog(ifr, "OwnerStatus ==> " + OwnerStatus);
								if (dateClosed.equalsIgnoreCase("Y") && (Integer.parseInt(currentBalance) > 0)
										&& OwnerStatus == true) {
									jsonarr1.add(obj);
									Log.consoleLog(ifr, "Added successfully Obj==>");
								}
								Log.consoleLog(ifr, "test2==>");
							}
						}
					}
				}
			}

			String borrIntId = "";
			String borrowerIntertIdQ = "SELECT INSERTIONORDERID FROM LOS_NL_BASIC_INFO WHERE PID='" + PID
					+ "' AND APPLICANTTYPE='B'";
			List<List<String>> borrowerIntertIdRes = cf.mExecuteQuery(ifr, borrowerIntertIdQ,
					"Feching intertionorderid of Borrower");
			Log.consoleLog(ifr, "borrowerIntertIdRes::" + borrowerIntertIdRes);
			if (!borrowerIntertIdRes.get(0).get(0).equalsIgnoreCase("")) {
				borrIntId = borrowerIntertIdRes.get(0).get(0);
			}
			Log.consoleLog(ifr, "Borrower IntertionOrderId:: " + borrIntId);
			String allLoanDedQ = "SELECT LOAN_ACC_NUMBER, \"LIMIT\", OUTSTANDING_BALANCE, EMI, PRODUCTNAME FROM SLOS_ALL_ACTIVE_PRODUCT WHERE WINAME='"
					+ PID + "'";
			List<List<String>> allLiabBorrwower = cf.mExecuteQuery(ifr, allLoanDedQ,
					"Feching all the Liabilites of Borrower");
			Log.consoleLog(ifr, "allLiabBorrwower::" + allLiabBorrwower);
			Log.consoleLog(ifr, "allLiabBorrwower size::" + allLiabBorrwower.size());
			if (allLiabBorrwower.size() > 0) {
				for (int z = 0; z < allLiabBorrwower.size(); z++) {
					JSONObject jsonObject = new JSONObject();
					String loanTypeB = allLiabBorrwower.get(z).get(4);
					Log.consoleLog(ifr, "loanType[" + z + "]:: " + loanTypeB);
					String loanAccNo = allLiabBorrwower.get(z).get(0);
					Log.consoleLog(ifr, "loanAccNo[" + z + "]:: " + loanAccNo);
					String loanAmt = allLiabBorrwower.get(z).get(1);
					Log.consoleLog(ifr, "loanAmt[" + z + "]:: " + loanAmt);
					String loanOutstanding = allLiabBorrwower.get(z).get(2);
					Log.consoleLog(ifr, "loanOutstanding[" + z + "]:: " + loanOutstanding);
					String installment = allLiabBorrwower.get(z).get(3);
					Log.consoleLog(ifr, "installment[" + z + "]:: " + installment);

					jsonObject.put("QNL_AL_LIAB_VAL_ApplicantType", borrIntId);
					jsonObject.put("QNL_AL_LIAB_VAL_LoanType", loanTypeB);
					jsonObject.put("QNL_AL_LIAB_VAL_Bank", "Canara Bank");
					jsonObject.put("QNL_AL_LIAB_VAL_Loan_Acc_No", loanAccNo);
					jsonObject.put("QNL_AL_LIAB_VAL_loanStartDate", "Not Available");
					jsonObject.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", loanAmt);
					jsonObject.put("QNL_AL_LIAB_VAL_Loan_LiabOut", loanOutstanding);
					jsonObject.put("QNL_AL_LIAB_VAL_EMIAmt", installment);
					jsonarr1.add(jsonObject);
				}
			}
			Log.consoleLog(ifr, "Child JSONARRAY RESULT Size::" + jsonarr1.size());
			Log.consoleLog(ifr, "Child JSONARRAY ::" + jsonarr1);
			ifr.addDataToGrid("ALV_AL_LIAB_VAL", jsonarr1);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
			Log.errorLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
			return RLOS_Constants.ERROR;
		}
		return "";
	}

//	public String PopulateCibilLiabilitiesSHL(IFormReference ifr) {
//
//		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//
//		Log.consoleLog(ifr, "inside PopulateCibilExperianLiabilities: ");
//		String responseBody = "";
//		JSONParser parser1 = new JSONParser();
//		String productCode = "HL";
//		ifr.clearTable("ALV_AL_LIAB_VAL");
//		JSONArray jsonarr1 = new JSONArray();
//		Log.consoleLog(ifr, "productCode=======>" + productCode);
//		try {
//			String allCoAppQuery = "SELECT INSERTIONORDERID FROM los_nl_basic_info WHERE INSERTIONORDERID IN (select distinct (substr(APPLICANT_TYPE, instr(APPLICANT_TYPE, '~') + 1)) from los_integration_reqres where TRANSACTION_ID = '"
//					+ PID + "' AND API_NAME LIKE '%Transunion_Consumer%')";
//			List<List<String>> allCoAppData = cf.mExecuteQuery(ifr, allCoAppQuery, "Feching all the co borrowers");
//			for (int k = 0; k < allCoAppData.size(); k++) {
//				String ApplicantType = allCoAppData.get(k).get(0);
//				String responseBodyCIBIL = "";
//				String checkLiabQ = "SELECT COUNT(*) FROM LOS_NL_AL_LIAB_VAL WHERE PID='" + PID
//						+ "' AND APPLICANTTYPE='" + ApplicantType + "'";
//				List<List<String>> checkLiabData = cf.mExecuteQuery(ifr, checkLiabQ,
//						"Checking Liabilites is already available or not");
//				int liabCount = Integer.parseInt(checkLiabData.get(0).get(0));
//				Log.consoleLog(ifr, "liabCount value ==>" + liabCount);
//				if (liabCount <= 0) {
//					try {
//						String CibilResQuery = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE API_NAME ='Transunion_Consumer' AND TRANSACTION_ID ='"
//								+ PID + "' AND APPLICANT_TYPE like '%" + ApplicantType + "%'";
//						List<List<String>> CibilResponse = cf.mExecuteQuery(ifr, CibilResQuery,
//								"Execute query for fetching CIBIL Response ");
//						if (!CibilResponse.isEmpty()) {
//
//							String CibilResponseStr = CibilResponse.get(0).get(0);
//							Log.consoleLog(ifr, "CibilResponseStr From Table==>" + CibilResponseStr);
//							JSONObject CibilResponseObj = (JSONObject) parser1.parse(CibilResponseStr);
//							responseBodyCIBIL = CibilResponseObj.toString();
//							Log.consoleLog(ifr, "Cibil Response from table Parsed ::");
//						}
//					} catch (Exception e) {
//						Log.consoleLog(ifr, "Exception in CibilResQuery :" + e);
//						JSONObject message = new JSONObject();
//						message.put("showMessage",
//								cf.showMessage(ifr, "", "error", "Technical glitch in getting Liabilities!"));
//						return message.toString();
//					}
//					// }
//					String excludedEMIAccnts = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "ACCTTYPE");
//					Log.consoleLog(ifr, "excludedEMIAccnts=>" + excludedEMIAccnts);
//					String excludedOwners = pcm.getParamConfig2(ifr, productCode, "TRANSUNIONCONF", "OWNERTYPE");
//					Log.consoleLog(ifr, "excludedOwners====>" + excludedOwners);
//					String BureauScore = "";
//					int cibilScore = 0;
//					String paymentHistory = "";
//					String suitFiled = "";
//					String accountType = "";
//					String dateClosed = "Y";
//					String currentBalance = "";
//					String memberShortName = "";
//					String dateOpened = "";
//					String formattedDate = "";
//					String highCreditAmount = "";
//					String amountOverdue = "";
//					String accountNumber = "";
//					String ownershipIndicator = "";
//					String Scores = "";
//					String accounts = "";
//					String emiAmount = "0";
//					int totalEmiAmnt = 0;
//					int totalNonEmiCount = 0;
//
//					JSONObject OutputJSON2 = (JSONObject) parser1.parse(responseBodyCIBIL);
//					JSONObject resultObj1 = new JSONObject(OutputJSON2);
//
//					Log.consoleLog(ifr, "resultObj==>" + resultObj1);
//
//					String body = resultObj1.get("body").toString();
//					JSONObject bodyObj = (JSONObject) parser1.parse(body);
//
//					String ControlData = bodyObj.get("controlData").toString();
//					Log.consoleLog(ifr, "ControlData==>" + ControlData);
//
//					JSONObject ControlDataObj = (JSONObject) parser1.parse(ControlData);
//					String ControlDataStatus = ControlDataObj.get("success").toString();
//
//					Log.consoleLog(ifr, "ControlDataStatus==>" + ControlDataStatus);
//
//					String consumerCreditData = bodyObj.get("consumerCreditData").toString();
//					Log.consoleLog(ifr, "consumerCreditData==>" + consumerCreditData);
//					JSONArray consumerCreditDataJSON = (JSONArray) parser1.parse(consumerCreditData);
//					if (!consumerCreditDataJSON.isEmpty()) {
//						for (int i = 0; i < consumerCreditDataJSON.size(); i++) {
//							String InputString = consumerCreditDataJSON.get(i).toString();
//							JSONObject consumerCreditDataJSONObj = (JSONObject) parser1.parse(InputString);
//							Log.consoleLog(ifr, "consumerCreditDataJSON ==> " + i);
//							Scores = consumerCreditDataJSONObj.get("scores").toString();
//							Log.consoleLog(ifr, "Scores=" + Scores);
//
//							try {
//								accounts = consumerCreditDataJSONObj.get("accounts").toString();
//								Log.consoleLog(ifr, "account=" + accounts);
//							} catch (Exception e) {
//								Log.consoleLog(ifr, "Exception/accounts=" + e);
//							}
//
//						}
//					}
//
//					Log.consoleLog(ifr, "Scores==>" + Scores);
//					JSONArray ScoresJSON = (JSONArray) parser1.parse(Scores);
//
//					if (!ScoresJSON.isEmpty()) {
//						for (int i = 0; i < ScoresJSON.size(); i++) {
//							String InputString = ScoresJSON.get(i).toString();
//							JSONObject ScoresJSONObj = (JSONObject) parser1.parse(InputString);
//							String transCIBILScore = ScoresJSONObj.get("score").toString();
//							Log.consoleLog(ifr, "transCIBILScore==>" + transCIBILScore);
//							cibilScore = Integer.parseInt(transCIBILScore);
//							Log.consoleLog(ifr, "cibilScore=======>" + cibilScore);
//							BureauScore = String.valueOf(cibilScore);
//							Log.consoleLog(ifr, "BureauScore======>" + BureauScore);
//							Log.consoleLog(ifr, "CIBILScore==>" + BureauScore);
//						}
//					}
//					Log.consoleLog(ifr, "accounts==>" + accounts);
//
//					if (!accounts.equalsIgnoreCase("")) {
//						JSONArray accountsJSON = (JSONArray) parser1.parse(accounts);
//
//						if (!accountsJSON.isEmpty()) {
//
//							for (int i = 0; i < accountsJSON.size(); i++) {
//								Log.consoleLog(ifr, "accountsJSON index:: " + i);
//
//								String InputString = accountsJSON.get(i).toString();
//								JSONObject accountsJSONObj = (JSONObject) parser1.parse(InputString);
//
//								if (!(cf.getJsonValue(accountsJSONObj, "accountType").equalsIgnoreCase(""))) {
//									accountType = accountsJSONObj.get("accountType").toString();
//									Log.consoleLog(ifr, "accountType==>" + accountType);
//								} else {
//									Log.consoleLog(ifr, "accountType tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "ownershipIndicator").equalsIgnoreCase(""))) {
//									ownershipIndicator = accountsJSONObj.get("ownershipIndicator").toString();
//									Log.consoleLog(ifr, "ownershipIndicator==>" + ownershipIndicator);
//								} else {
//									Log.consoleLog(ifr, "ownershipIndicator tag not available.");
//								}
//								if (!(cf.getJsonValue(accountsJSONObj, "currentBalance").equalsIgnoreCase(""))) {
//									currentBalance = accountsJSONObj.get("currentBalance").toString();
//									Log.consoleLog(ifr, "currentBalance==>" + currentBalance);
//
//									if (currentBalance.equalsIgnoreCase("")) {
//										currentBalance = "";
//									}
//
//								} else {
//									Log.consoleLog(ifr,
//											"currentBalance tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "memberShortName").equalsIgnoreCase(""))) {
//									memberShortName = accountsJSONObj.get("memberShortName").toString();
//									Log.consoleLog(ifr, "memberShortName==>" + memberShortName);
//
//									if (memberShortName.equalsIgnoreCase("")) {
//										memberShortName = "";
//									}
//
//								} else {
//									Log.consoleLog(ifr,
//											"memberShortName tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "dateOpened").equalsIgnoreCase(""))) {
//									dateOpened = accountsJSONObj.get("dateOpened").toString();
//									Log.consoleLog(ifr, "dateOpened==>" + dateOpened);
//
//									if (dateOpened.equalsIgnoreCase("")) {
//										dateOpened = "";
//									}
//								} else {
//									Log.consoleLog(ifr, "dateOpened tag not available for accountsJSON index ::" + i);
//								}
//								if (!(cf.getJsonValue(accountsJSONObj, "dateClosed").equalsIgnoreCase(""))) {
//									dateClosed = accountsJSONObj.get("dateClosed").toString();
//									Log.consoleLog(ifr, "dateClosed==>" + dateClosed);
//
//									if (!dateClosed.equalsIgnoreCase("")) {
//										dateClosed = "N";
//									}
//									// dateClosed
//								} else {
//									dateClosed = "Y";
//									Log.consoleLog(ifr, "dateClosed tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "highCreditAmount").equalsIgnoreCase(""))) {
//									highCreditAmount = accountsJSONObj.get("highCreditAmount").toString();
//									Log.consoleLog(ifr, "highCreditAmount==>" + highCreditAmount);
//
//									if (highCreditAmount.equalsIgnoreCase("")) {
//										highCreditAmount = "";
//									}
//
//								} else {
//									Log.consoleLog(ifr,
//											"highCreditAmount tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "amountOverdue").equalsIgnoreCase(""))) {
//									amountOverdue = accountsJSONObj.get("amountOverdue").toString();
//									Log.consoleLog(ifr, "amountOverdue==>" + amountOverdue);
//
//									if (amountOverdue.equalsIgnoreCase("")) {
//										amountOverdue = "";
//									}
//
//								} else {
//									Log.consoleLog(ifr,
//											"amountOverdue tag not available for accountsJSON index ::" + i);
//								}
//
//								if (!(cf.getJsonValue(accountsJSONObj, "accountNumber").equalsIgnoreCase(""))) {
//									accountNumber = accountsJSONObj.get("accountNumber").toString();
//									Log.consoleLog(ifr, "accountNumber==>" + accountNumber);
//
//									if (accountNumber.equalsIgnoreCase("")) {
//										accountNumber = "";
//									}
//
//								} else {
//									Log.consoleLog(ifr,
//											"accountNumber tag not available for accountsJSON index ::" + i);
//								}
//
//								String party_type = "";
//								if (ApplicantType.contains("~")) {
//									party_type = ApplicantType.split("~")[1];
//								} else {
//									party_type = ApplicantType;
//								}
//								Log.consoleLog(ifr, "Party Type==>" + party_type);
//
//								JSONObject obj = new JSONObject();
//								obj.put("QNL_AL_LIAB_VAL_LoanType", accountType);
//								obj.put("QNL_AL_LIAB_VAL_ApplicantType", party_type);
//								obj.put("QNL_AL_LIAB_VAL_ConsiderForEligibility", "Yes");
//								obj.put("QNL_AL_LIAB_VAL_Bank", memberShortName);
//								obj.put("QNL_AL_LIAB_VAL_loanStartDate", dateOpened);
//								obj.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", highCreditAmount);
//								obj.put("QNL_AL_LIAB_VAL_Loan_LiabOut", currentBalance);
//								obj.put("QNL_AL_LIAB_VAL_Overdue",
//										amountOverdue.equalsIgnoreCase("") ? "0.00" : amountOverdue);
//								obj.put("QNL_AL_LIAB_VAL_Loan_Acc_No", accountNumber);
//
//								if (!(cf.getJsonValue(accountsJSONObj, "emiAmount").equalsIgnoreCase(""))) {
//									emiAmount = accountsJSONObj.get("emiAmount").toString();
//									Log.consoleLog(ifr, "emiAmount==>" + emiAmount);
//									if (!emiAmount.equalsIgnoreCase("")) {
//										obj.put("QNL_AL_LIAB_VAL_EMIAmt", emiAmount);
//										totalEmiAmnt = totalEmiAmnt + Integer.parseInt(emiAmount);
//									} else {
//										Log.consoleLog(ifr, "emiAmount tag available but empty");
//										obj.put("QNL_AL_LIAB_VAL_EMIAmt", "0.00");
//										if (Integer.parseInt(currentBalance) > 0) {
//											totalNonEmiCount++;
//										}
//									}
//								} else {
//									Log.consoleLog(ifr, "emiAmount tag not available.");
//
//									if (Integer.parseInt(currentBalance) > 0) {
//										totalNonEmiCount++;
//									}
//								}
//								Log.consoleLog(ifr, "JSON obj RESULT::" + obj);
//
//								boolean OwnerStatus = checkOwnerTypeFilterStatus(ifr, ownershipIndicator,
//										excludedOwners);
//								Log.consoleLog(ifr, "OwnerStatus ==> " + OwnerStatus);
//								if (dateClosed.equalsIgnoreCase("Y") && (Integer.parseInt(currentBalance) > 0)
//										&& OwnerStatus == true) {
//									jsonarr1.add(obj);
//									Log.consoleLog(ifr, "Added successfully Obj==>");
//								}
//								Log.consoleLog(ifr, "test2==>");
//							}
//						}
//					}
//				}
//			}
//			
//			String borrIntId="";
//			String borrowerIntertIdQ = "SELECT INSERTIONORDERID FROM LOS_NL_BASIC_INFO WHERE PID='"+PID+"' AND APPLICANTTYPE='B'";
//			List<List<String>> borrowerIntertIdRes = cf.mExecuteQuery(ifr, borrowerIntertIdQ, "Feching intertionorderid of Borrower");
//			Log.consoleLog(ifr, "borrowerIntertIdRes::" + borrowerIntertIdRes);
//			if (!borrowerIntertIdRes.get(0).get(0).equalsIgnoreCase("")){
//			    borrIntId=borrowerIntertIdRes.get(0).get(0);
//			}
//			Log.consoleLog(ifr, "Borrower IntertionOrderId:: "+borrIntId);
//			String allLoanDedQ = "SELECT LOAN_ACC_NUMBER, \"LIMIT\", OUTSTANDING_BALANCE, EMI FROM SLOS_ALL_ACTIVE_PRODUCT WHERE WINAME='"+PID+"'";
//			List<List<String>> allLiabBorrwower = cf.mExecuteQuery(ifr, allLoanDedQ, "Feching all the Liabilites of Borrower");
//			Log.consoleLog(ifr, "allLiabBorrwower::" + allLiabBorrwower);
//			Log.consoleLog(ifr, "allLiabBorrwower size::"+allLiabBorrwower.size());
//			if (allLiabBorrwower.size()>0){
//			    for (int z=0; z<allLiabBorrwower.size(); z++){
//			        JSONObject jsonObject = new JSONObject();
//			        String loanType = "00";
//			        Log.consoleLog(ifr, "loanType["+z+"]:: " + loanType);
//			        String loanAccNo = allLiabBorrwower.get(z).get(0);
//			        Log.consoleLog(ifr, "loanAccNo["+z+"]:: " + loanAccNo);
//			        String loanAmt = allLiabBorrwower.get(z).get(1);
//			        Log.consoleLog(ifr, "loanAmt["+z+"]:: " + loanAmt);
//			        String loanOutstanding = allLiabBorrwower.get(z).get(2);
//			        Log.consoleLog(ifr, "loanOutstanding["+z+"]:: " + loanOutstanding);
//			        String installment = allLiabBorrwower.get(z).get(3);
//			        Log.consoleLog(ifr, "installment["+z+"]:: " + installment);
//
//			        jsonObject.put("QNL_AL_LIAB_VAL_ApplicantType", borrIntId);
//			        jsonObject.put("QNL_AL_LIAB_VAL_LoanType", loanType);
//			        jsonObject.put("QNL_AL_LIAB_VAL_Bank", "Canara Bank");
//			        jsonObject.put("QNL_AL_LIAB_VAL_Loan_Acc_No", loanAccNo);
//			        jsonObject.put("QNL_AL_LIAB_VAL_loanStartDate", "Not Available");
//			        jsonObject.put("QNL_AL_LIAB_VAL_Loan_LiabAmt", loanAmt);
//			        jsonObject.put("QNL_AL_LIAB_VAL_Loan_LiabOut", loanOutstanding);
//			        jsonObject.put("QNL_AL_LIAB_VAL_EMIAmt", installment);
//			        jsonarr1.add(jsonObject);
//			    }
//			}
//			
//			Log.consoleLog(ifr, "Child JSONARRAY RESULT Size::" + jsonarr1.size());
//			Log.consoleLog(ifr, "Child JSONARRAY ::" + jsonarr1);
//			ifr.addDataToGrid("ALV_AL_LIAB_VAL", jsonarr1);
//		} catch (Exception e) {
//			Log.consoleLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
//			Log.errorLog(ifr, "Exception in PopulateCibilExperianLiabilities :" + e);
//			return RLOS_Constants.ERROR;
//		}
//		return "";
//	}

	private boolean checkOwnerTypeFilterStatus(IFormReference ifr, String ownerType, String excludedOwners) {

		Log.consoleLog(ifr, "#checkOwnerFilterStatus started...");

		Log.consoleLog(ifr, "ownerType=====>" + ownerType);
		Log.consoleLog(ifr, "excludedOwners=====>" + excludedOwners);

		String[] excludedOwnerTypes = excludedOwners.split(",");
		for (String typeOfOwner : excludedOwnerTypes) {
			if (typeOfOwner.equals(ownerType)) {
				Log.consoleLog(ifr, "excludedOwnerTypes===> " + typeOfOwner);
				return false;
			}
		}

		Log.consoleLog(ifr, "Filter Condition not satisfied..");
		return true;

	}

	public String panelValuationInitiateSHL(IFormReference ifr) {
		try {
			Log.consoleLog(ifr, "Inside PropertyValution_btn===>");
			JSONObject message = new JSONObject();
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String query = "SELECT Collateral_ID,Agency_Name,Collateral_Address,Initiator_Remarks,insertionOrderId FROM LOS_NL_Property_Valuation  where PID='"
					+ PID + "' and Valuation_WorkItem_Number is null";
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
					String attributes = "<ParentWINo>" + PID + "</ParentWINo>" + "<ParentPVID>" + insertionOrderID
							+ "</ParentPVID>" + "<Agency>" + Agency + "</Agency>" + "<Collateral_ID>" + Collateral_ID
							+ "</Collateral_ID>" + "<initiatedby>" + user + "</initiatedby>" + "<Remarks>" + Remarks
							+ "</Remarks>" + "<collateralAddress>" + Collateral_Address + "</collateralAddress>";
					Log.consoleLog(ifr, "attributes  ====" + attributes);
					UploadCreateWI ucwi = new UploadCreateWI();
					String processDefId = "5"; // Same as home loan
					Log.consoleLog(ifr, "FI processDefId  ====" + processDefId);
					String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
					if (pid.isEmpty()) {
						message.put("showMessage",
								cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
						return message.toString();
					} else {
						Log.consoleLog(ifr, "Legal Sub process PID " + pid);
						String query1 = "UPDATE LOS_NL_Property_Valuation SET Valuation_WorkItem_Number ='" + pid
								+ "',Val_Status ='Initiated'  WHERE PID ='" + PID + "' and insertionOrderId='"
								+ insertionOrderID + "'";
						Log.consoleLog(ifr, "query1" + query1);
						ifr.saveDataInDB(query1);
					}
				}
			} else {
				message.put("showMessage",
						cf.showMessage(ifr, "", "error", "All Property Valuation are already Initiated!"));
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

	public String calculateCollateralSummarySHL(IFormReference ifr) { // By Vikash Mehta
		Log.consoleLog(ifr, "Inside calculateCollateralSummarySHL:");
		try {
			String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
			ifr.setStyle("QL_COLLAT_SUMMARY_TOTSECNDCOLL", "Visible", "True");
			ifr.setStyle("QL_COLLAT_SUMMARY_TOTNEWCOLL", "visible", "False");
			ifr.setStyle("QL_COLLAT_SUMMARY_TOTSECNDCOLL", "visible", "true");
			ifr.setStyle("QL_COLLAT_SUMMARY_TOTPRIMARYCOLL", "disable", "true");
			ifr.setStyle("QL_COLLAT_SUMMARY_TOTCOLLAVAILBL", "disable", "true");
			ifr.setStyle("QL_COLLAT_SUMMARY_OVERALLLTV", "disable", "true");
			String query = "select PROJECT_COST,LOANELIGASPERMARKET,SECURITY_TYPE,MARKETVALUEOFPLOT,MARGINPERCENTAGE from LOS_CAM_COLLATERAL_DETAILS where  PID='"
					+ PID + "'";
			List<List<String>> queryResult = cf.mExecuteQuery(ifr, query, "CollateralSummary query:");
			if (!queryResult.isEmpty()) {
				BigDecimal totalNewColl = new BigDecimal("0.0");
				BigDecimal totalCollAvailCoverage = new BigDecimal("0.0");
				BigDecimal totalPrimaryColl = new BigDecimal("0.0");
				BigDecimal totalSecondaryColl = new BigDecimal("0.0");
				BigDecimal overallLTVPercentage = new BigDecimal("0.0");
				for (int i = 0; i < queryResult.size(); i++) {
					BigDecimal projectCost = new BigDecimal(
							queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
					Log.consoleLog(ifr, "Project cost:: " + projectCost);
					BigDecimal markteValue = new BigDecimal(
							queryResult.get(i).get(3).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(3));
					Log.consoleLog(ifr, "Market Value:: " + markteValue);
					Double finalComparedValue = Math.min(projectCost.doubleValue(), markteValue.doubleValue());
					Log.consoleLog(ifr, "finalComparedValue:: " + finalComparedValue);
					totalNewColl = totalNewColl.add(BigDecimal.valueOf(finalComparedValue));
					Log.consoleLog(ifr, "Before intializing totalCollAvailCoverage1");
					BigDecimal totalCollAvailCoverage1 = new BigDecimal(
							queryResult.get(i).get(1) == null || queryResult.get(i).get(1).trim().isEmpty()
									|| queryResult.get(i).get(1).equalsIgnoreCase("") ? "0.0"
											: queryResult.get(i).get(1).replaceAll(",", "").trim());
					Log.consoleLog(ifr, "totalCollAvailCoverage1 ==> " + totalCollAvailCoverage1);
					Log.consoleLog(ifr, "totalCollAvailCoverage ==> " + totalCollAvailCoverage);
					if (queryResult.get(i).get(2).equalsIgnoreCase("Primary")) {
						BigDecimal projectCost1 = new BigDecimal(
								queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
						Log.consoleLog(ifr, "Project cost1:: " + projectCost1);
						BigDecimal markteValue1 = new BigDecimal(
								queryResult.get(i).get(3).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(3));
						Log.consoleLog(ifr, "Market Value1:: " + markteValue1);
//	                 Double finalComparedValue1 = Math.min(projectCost1.doubleValue(), markteValue1.doubleValue()); // Previous Logic
						Double finalComparedValue1 = markteValue1.doubleValue();
						Log.consoleLog(ifr, "finalComparedValue1:: " + finalComparedValue1);
						totalPrimaryColl = totalPrimaryColl.add(BigDecimal.valueOf(finalComparedValue1));
					} else if (queryResult.get(i).get(2).equalsIgnoreCase("Secondary")) {
						BigDecimal projectCost2 = new BigDecimal(
								queryResult.get(i).get(0).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(0));
						Log.consoleLog(ifr, "Project cost1:: " + projectCost2);
						BigDecimal markteValue2 = new BigDecimal(
								queryResult.get(i).get(3).equalsIgnoreCase("") ? "0.0" : queryResult.get(i).get(3));
						Log.consoleLog(ifr, "Market Value1:: " + markteValue2);
//	                 Double finalComparedValue2 = Math.min(projectCost2.doubleValue(), markteValue2.doubleValue());  // Previous Logic
						Double finalComparedValue2 = markteValue2.doubleValue();
						Log.consoleLog(ifr, "finalComparedValue1:: " + finalComparedValue2);
						totalSecondaryColl = totalSecondaryColl.add(BigDecimal.valueOf(finalComparedValue2));
					}
				}
				int marginPercent = Integer.parseInt(queryResult.get(0).get(4));
				totalCollAvailCoverage = totalPrimaryColl.add(totalSecondaryColl);
				overallLTVPercentage = new BigDecimal("100").subtract(BigDecimal.valueOf(marginPercent)).setScale(2,
						RoundingMode.HALF_UP);
				ifr.setValue("QL_COLLAT_SUMMARY_TOTNEWCOLL", totalNewColl.toString());
				ifr.setValue("QL_COLLAT_SUMMARY_TOTPRIMARYCOLL", totalPrimaryColl.toString());
				ifr.setValue("QL_COLLAT_SUMMARY_TOTSECNDCOLL", totalSecondaryColl.toString());
				ifr.setValue("QL_COLLAT_SUMMARY_OVERALLLTV", overallLTVPercentage.toString());
				ifr.setValue("QL_COLLAT_SUMMARY_TOTCOLLAVAILBL", totalCollAvailCoverage.toString());

				String summaryCountQ = "select count(*) from los_l_collat_summary where pid='" + PID + "'";
				List<List<String>> summaryCountRes = cf.mExecuteQuery(ifr, summaryCountQ,
						"CollateralSummary count query:");
				int summaryCount = Integer.parseInt(summaryCountRes.get(0).get(0));
				Log.consoleLog(ifr, "summaryCount====>" + summaryCount);
				if (summaryCount == 0) {
					String summaryInsertQuery = "insert into los_l_collat_summary(PID, TOTNEWCOLL, TOTPRIMARYCOLL, TOTSECNDCOLL, TOTCOLLAVAILBL, OVERALLLTV)\n"
							+ "values('" + PID + "', '" + totalNewColl + "', '" + totalPrimaryColl + "' , '"
							+ totalSecondaryColl + "', '" + totalCollAvailCoverage + "', '" + overallLTVPercentage
							+ "')";
					Log.consoleLog(ifr, "summaryInsertQuery====>" + summaryInsertQuery);
					ifr.saveDataInDB(summaryInsertQuery);
				} else {
					String summaryUpdateQuery = "update los_l_collat_summary set TOTNEWCOLL='" + totalNewColl
							+ "', TOTPRIMARYCOLL='" + totalPrimaryColl + "', TOTSECNDCOLL='" + totalSecondaryColl
							+ "', TOTCOLLAVAILBL='" + totalCollAvailCoverage + "', OVERALLLTV='" + overallLTVPercentage
							+ "' where pid='" + PID + "'";
					Log.consoleLog(ifr, "summaryUpdateQuery====>" + summaryUpdateQuery);
					ifr.saveDataInDB(summaryUpdateQuery);
				}

//	                String summaryQuery = "INSERT INTO los_l_collat_summary (PID, TOTNEWCOLL, TOTPRIMARYCOLL, TOTSECNDCOLL, TOTCOLLAVAILBL, OVERALLLTV) SELECT '"+PID+"','"+totalNewColl+"','"+totalPrimaryColl+"','"+totalSecondaryColl+"','"+totalCollAvailCoverage+"','"+overallLTVPercentage+"' FROM dual WHERE NOT EXISTS (SELECT 1 FROM los_l_collat_summary WHERE PID='"+PID+"')";
//	                Log.consoleLog(ifr, "summaryQuery====>" + summaryQuery);
//	                ifr.saveDataInDB(summaryQuery);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  calculateCollateralSummarySHL:" + e);
			Log.errorLog(ifr, "Exception in calculateCollateralSummarySHL:" + e);
		}
		return "";
	}

	public String mImplClickValidateBtnSHL(IFormReference ifr) {// Checked
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

	public String kycVerified(IFormReference ifr) {
		String idType = "";
		JSONObject kycDetailsGridData = null;
		JSONArray kycDetailsGrid = ifr.getDataFromGrid("LV_KYC"); // LV_KYC
		Log.consoleLog(ifr, "kycDetailsGrid " + kycDetailsGrid);
		int count = ifr.getDataFromGrid("LV_KYC").size();
		Log.consoleLog(ifr, "count= " + count);
		for (int i = 0; i < count; i++) {
			kycDetailsGridData = (JSONObject) kycDetailsGrid.get(i);
			Log.consoleLog(ifr, "kycDetailsGridData:: " + kycDetailsGridData.toString());
			idType = kycDetailsGridData.get("ID Type").toString();
			Log.consoleLog(ifr, "idType= " + idType);
		}
		return idType;
	}

	public void setValidateKYCDetails(IFormReference ifr) {

		ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidationStatus", "Yes");
		ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedOn", cf.getCurrentDate(ifr));
		ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedBy", ifr.getUserName());
		ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_No", "disable", "true");
		ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "disable", "true");
		ifr.setStyle("AadharVaultRefId", "disable", "true");
		ifr.setStyle("BTN_KYC_Validate", "visible", "false");
	}

	public String getCurrentTimeStamp() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss aa");
		String formattedDate = dateFormat.format(new Date()).toString();
		System.out.println(formattedDate);
		return formattedDate;
	}

	public String feesAndCharges(IFormReference ifr) {
		String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
		String apiCriterion = "'CBS_FundTransfer'";
		String executeCBSStatus = cm.executeCBSStatus(ifr, PID, apiCriterion);
		String queryFeesandChgs = "select count(*) from LOS_NL_GENERATE_DOCUMENT where PID = '" + PID + "'";
		Log.consoleLog(ifr, "queryFeesandChgs===>" + queryFeesandChgs);
		List Result1 = ifr.getDataFromDB(queryFeesandChgs);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);

		if (executeCBSStatus.equalsIgnoreCase("SUCCESS") || Integer.parseInt(Count1) == 0) {
			return "SUCCESS";
		}
//		else {
//			FundTransfer fundTransfer = new FundTransfer();
//			String Status = fundTransfer.prefundTransfer(ifr);
//			return Status;
//		}
		return "failed";

	}

	public String checkForExStaff(IFormReference ifr) {
		String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
		String cstid1 = "select  flgcustype from los_trn_customersummary where winame='" + PID + "'";
		Log.consoleLog(ifr, "cstid1 query" + cstid1);
		List<List<String>> resCstid1 = ifr.getDataFromDB(cstid1);
		String custFLag = resCstid1.get(0).get(0).toString();
		Log.consoleLog(ifr, "custFLag" + custFLag);
		if (resCstid1.isEmpty() || custFLag.isBlank()) {
			Log.consoleLog(ifr, "Inside error message");
			return "error,Technical glitch";

		}
		if (custFLag.equalsIgnoreCase("R1")) {
			return "Not a exstaff";
		} else {
			return "exstaff";
		}

	}

	public String checkDebitCharges(IFormReference ifr) {
		String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
		String apiCriterion = "'CBS_FundTransfer'";
		String executeCBSStatus = cm.executeCBSStatus(ifr, PID, apiCriterion);

		if (executeCBSStatus.equalsIgnoreCase("SUCCESS")) {
			return "SUCCESS";
		} else {
			FundTransfer fundTransfer = new FundTransfer();
			String Status = fundTransfer.prefundTransfer(ifr);
			return Status;
			// return "FAILED";
		}

	}

	public String autoPopulateCoBorowerDetailsByFkeySHL(IFormReference ifr, String fkey) {
		Log.consoleLog(ifr, "inside autoPopulateCoBorowerDetailsByFkeySHL: ");
		try {
			String appType = ifr.getValue("Portal_CustDet_Applicant_Type").toString();
			if (appType.trim().equalsIgnoreCase("B")) {
				ifr.setStyle("Portal_Co_App_Details", "visible", "false");
				ifr.setStyle("Portal_CustDet_Applicant_Type", "disable", "true");
				ifr.setStyle("HL_CB_Basic_Details_Sec", "visible", "false");
				ifr.setStyle("HL_CB_Additional_Details_Sec", "visible", "false");
				ifr.setStyle("F_PartyDetails2", "visible", "false");
				ifr.setStyle("F_PartyDetails3", "visible", "false");
			} else {
				String occupationType = ifr.getValue("Occupationtype_coborr").toString();
				String COCustID = ifr.getValue("QNL_HL_PORTAL_CNL_BASIC_INFO_CustomerID").toString();
				String Fkey = fKeyByCustomerId(ifr, COCustID);
				Log.consoleLog(ifr, "FKEY_HL: " + Fkey);
				shlccc.populateDropDownForSelectedValueSHL(ifr, occupationType, "SUBOCCUPATION_DROPDOWN",
						"Occupationsubtype_coborr");
				if (Fkey.isEmpty()) {
					ifr.setStyle("addAdvancedListviewrowNext_CNL_BASIC_INFO", "disable", "true");
					ifr.setStyle("addAdvancedListviewrowNext_CNL_BASIC_INFO", "disable", "true");
				} else {
					ifr.setStyle("P_OD_ValidateCoObligantCB", "visible", "false");
					ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER", "disable", "true");
					ifr.setStyle("QNL_HL_PORTAL_CNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo", "disable", "true");
					ifr.setStyle("Portal_CustDet_CustID", "disable", "true");
					ifr.setStyle("Portal_CustDet_FullName", "disable", "true");
					ifr.setStyle("HL_CB_Basic_Details_Sec", "visible", "true");
					ifr.setStyle("HL_CB_Additional_Details_Sec", "visible", "true");
				}
			}
		} catch (Exception ex) {
			Log.consoleLog(ifr, "autoPopulateCoBorowerDetailsByFkeySHL:Exception::" + ex.getMessage());
		}
		return "";
	}

	public String fKeyByCustomerId(IFormReference ifr, String customerId) {

		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Fquery = "select F_KEY from los_nl_basic_info where PID='" + PID
				+ "' and Applicanttype IN ('CB', 'G', 'B') and CUSTOMERID='" + customerId + "' and rownum=1";

		Log.consoleLog(ifr, "FKEYQUERY :::" + Fquery);
		List<List<String>> strgey = ifr.getDataFromDB(Fquery);
		String strfkey = strgey.get(0).get(0);
		Log.consoleLog(ifr, "FKEYQUERY Output :::" + strfkey);
		return strfkey;
	}

	public String checkExserStaff(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryForMaLoanAmout = "select count(*) from slos_staff_home_trn where EX_STAFF_ID IS NULL AND winame='"
				+ PID + "'";
		Log.consoleLog(ifr, "Count query===>" + queryForMaLoanAmout);
		List Result1 = ifr.getDataFromDB(queryForMaLoanAmout);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);

		if (Integer.parseInt(Count1) > 0) {
			// Log.consoleLog(ifr, "Not Eligible.");
			return "staff";
		} else {
			return "exstaff";
		}
	}

	public static String checkDocumentOnLoadHL(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String subProductCode = "";
		String scheduleCode = "";
		String purpose = "";
		List<List<String>> listgetAllDocuments = new ArrayList<>();
		List<List<String>> listgetAllDocumentsP = new ArrayList<>();
		String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
		List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
		Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
		if (!listqueryproductandScheduleCode.isEmpty()) {
			subProductCode = listqueryproductandScheduleCode.get(0).get(0);
			scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
			purpose = listqueryproductandScheduleCode.get(0).get(2);
		}
		String Count1 = "";

		String activityName = ifr.getActivityName();
		if (activityName.contains("Staff_HL_Branch_Maker")) {
			String queryforDocGrid = "SELECT  DISTINCT\r\n" + " d.NAME, d.docstatus, d.createddatetime\r\n"
					+ "		FROM SLOS_TRN_BKOF_DOCUMENTS d\r\n" + "	WHERE d.WINAME = '" + processInstanceId + "'\r\n"
					+ "   AND d.NAME IN ('NF_523_HL_APPLICATION', 'PROPERTY_INSPECTION_REPORT')\r\n"
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

		else {
			if (subProductCode.equalsIgnoreCase("667") || subProductCode.equalsIgnoreCase("666")
					|| subProductCode.equalsIgnoreCase("670")) {
				String getAllNewDocuments = "select * from SLOS_HL_DOCUMENTS where  generate='S'";
				listgetAllDocuments = ifr.getDataFromDB(getAllNewDocuments);
				Log.consoleLog(ifr, "listgetAllNewDocuments==>" + listgetAllDocuments);
				String getAllNewDocumentsP = "select * from SLOS_HL_DOCUMENTS where  generate='P'";
				listgetAllDocumentsP = ifr.getDataFromDB(getAllNewDocumentsP);
				Log.consoleLog(ifr, "listgetAllDocumentsP==>" + listgetAllDocumentsP);
			}

			List<String> responseForGrid1 = listgetAllDocuments.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			String NAME = "'" + String.join("','", responseForGrid1) + "'";
			
			List<String> responseForGrid2 = listgetAllDocumentsP.stream().map(doc -> doc.get(0))
					.collect(Collectors.toList());

			String NAME2 = "'" + String.join("','", responseForGrid2) + "'";
			
			activityName = ifr.getActivityName();
			if (activityName.equalsIgnoreCase("Staff_HL_Sanction")
					|| activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")
					|| activityName.equalsIgnoreCase("Staff_HL_Post_Sanction") || value.equalsIgnoreCase("NO")) {
				String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
						+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
						+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
						+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
						+ "' AND NAME IN (" + NAME + " )\r\n" + ")\r\n" + "WHERE rn = 1";

				Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
				List Result1 = ifr.getDataFromDB(queryForCount);
				Count1 = Result1.toString().replace("[", "").replace("]", "");
				Log.consoleLog(ifr, "Count1==>" + Count1);
			}
		else
	       {
			 String queryForCount = "SELECT COUNT(DISTINCT NAME) AS distinct_name_count \r\n" + "FROM (\r\n"
		   				+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
		   				+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
		   				+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId + "' AND NAME IN (" + NAME2 + " )  AND DOCTYPE IS NULL \r\n"
		   				+ ")\r\n" + "WHERE rn = 1";
	          
	   		Log.consoleLog(ifr, "isAllDocumentsDownloaded query :==>" + queryForCount);
	   		List Result1 = ifr.getDataFromDB(queryForCount);
	   		 Count1 = Result1.toString().replace("[", "").replace("]", "");
	   		Log.consoleLog(ifr, "Count1==>" + Count1);   
	       }
			// 17 documents need to be generated
			if (activityName.equalsIgnoreCase("Staff_HL_Sanction") || activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")) {
				return getDocCount(ifr, processInstanceId, NAME, value);

			}
			else if (activityName.equalsIgnoreCase("Staff_HL_Post_Sanction")) {
				return getDocCount(ifr, processInstanceId, NAME2, value);

			}
		}

		return "FAILED";

	}

	private static String getDocCount(IFormReference ifr, String processInstanceId, String NAME, String value) {
		String activityName = ifr.getActivityName();
		if (activityName.equalsIgnoreCase("Staff_HL_Sanction") || activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")
				|| activityName.equalsIgnoreCase("Staff_HL_Post_Sanction")) {
			String queryforDocGrid = "SELECT DISTINCT NAME, DOCSTATUS, CREATEDDATETIME, WINAME\r\n" + "FROM (\r\n"
					+ "    SELECT NAME, DOCSTATUS, CREATEDDATETIME, WINAME,\r\n"
					+ "           ROW_NUMBER() OVER (PARTITION BY NAME ORDER BY CREATEDDATETIME DESC) rn\r\n"
					+ "    FROM SLOS_TRN_BKOF_DOCUMENTS\r\n" + "    WHERE WINAME = '" + processInstanceId
					+ "' AND NAME IN (" + NAME + " ) \r\n" + ")\r\n" + "WHERE rn = 1";
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

	public String OnLoadConsentSectionAddCoBorrowerConsent(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside OnLoadConsentSectionAddCoBorrowerConsent:");
		try {
			OnLoadConsentSectionDeleteCoBorrowerConsent(ifr);
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String query1 = "Select insertionorderid from LOS_NL_BASIC_INFO where PID='" + PID
					+ "' and applicanttype not in ('B')";
			String query2 = "Select PartyType from LOS_NL_BUREAU_CONSENT where PID='" + PID + "'";
			List<List<String>> query1Result = cf.mExecuteQuery(ifr, query1,
					"Fetching insertion order id for given applicant type");
			List<List<String>> query2Result = cf.mExecuteQuery(ifr, query2, "Fetching all added consent");
			if (query1Result.size() > 0) {
				Set<String> existingConsents = new HashSet<>();
				for (List<String> resultRow : query2Result) {
					existingConsents.addAll(resultRow);
				}
				for (int i = 0; i < query1Result.size(); i++) {
					if (!existingConsents.contains(query1Result.get(i).get(0))) {// && !borrAdded) {
						JSONObject jsonobj = new JSONObject();
						JSONArray jsonArray = new JSONArray();
						jsonobj.put("QNL_BUREAU_CONSENT_PartyType", query1Result.get(i).get(0));
						jsonobj.put("QNL_BUREAU_CONSENT_Methodology", "MU");
						jsonobj.put("QNL_BUREAU_CONSENT_ConsentReceived", "Initiated");

						jsonArray.add(jsonobj);
						ifr.addDataToGrid("ALV_BUREAU_CONSENT", jsonArray);
					}

				}

			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in OnLoadConsentSectionAddCoBorrowerConsent method::" + e);
			Log.errorLog(ifr, "Exception in OnLoadConsentSectionAddCoBorrowerConsent method::" + e);
		}
		return "SUCCESS";
	}

	public void OnLoadConsentSectionDeleteCoBorrowerConsent(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside OnLoadConsentSectionDeleteCoBorrowerConsent:");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String query1 = "Select insertionorderid from LOS_NL_BASIC_INFO where PID='" + PID
					+ "' and applicanttype not in ('B')";
			String query2 = "Select PartyType from LOS_NL_BUREAU_CONSENT where PID='" + PID + "'";
			List<List<String>> query1Result = cf.mExecuteQuery(ifr, query1,
					"Fetching insertion order id for given applicant type");
			List<List<String>> query2Result = cf.mExecuteQuery(ifr, query2, "Fetching all added consent");

			if (query2Result.size() > 0) {
				Set<String> existingConsents = new HashSet<>();
				for (List<String> resultRow : query1Result) {
					existingConsents.addAll(resultRow);
				}
				Log.consoleLog(ifr, "Inside Delete Started:" + existingConsents.toString());
				for (int i = 0; i < query2Result.size(); i++) {
					Log.consoleLog(ifr, "Inside Delete Started:" + query2Result.get(i).get(0));
					if (!existingConsents.contains(query2Result.get(i).get(0))) {
						Log.consoleLog(ifr, "Inside Delete Started:" + i);
						int[] ints = { i };

						ifr.deleteRowsFromGrid("ALV_BUREAU_CONSENT", ints);

					}
					Log.consoleLog(ifr, "Inside dones Delete Started:");
				}

				Log.consoleLog(ifr, "Inside dbdggDelete Started:");

			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in OnLoadConsentSectionDeleteCoBorrowerConsent method::" + e);
			Log.errorLog(ifr, "Exception in OnLoadConsentSectionDeleteCoBorrowerConsent method::" + e);
		}
	}

	public String onChangeNoBorrowerDetails(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String maxLoanAMtbrower = "";
		String maxLoanAMtbrowerRenovation = "";
		Validator valid = new KnockOffValidator("None");
		String validProductCodeQuery1 = "SELECT productcode,productType FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
		List<List<String>> validProductCodeResult1 = ifr.getDataFromDB(validProductCodeQuery1);
		List<String> eHLProductCodes = valid.getFlatList(ifr, validProductCodeResult1, "EHL");
		List<String> aHLProductCode = valid.getFlatList(ifr, validProductCodeResult1, "AHL");
		List<String> temp = new ArrayList<>();
		temp.addAll(eHLProductCodes);
		temp.addAll(aHLProductCode);
		String inClauseStaff = "666,667,670";

		String inClauseEHL = eHLProductCodes.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));

		String inClauseAHL = aHLProductCode.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));

		String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='"
				+ PID + "'";
		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
		List Result1 = ifr.getDataFromDB(exStaffQuery);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		if (Integer.parseInt(Count1) > 0) {

			String queryForDesignation = "select EX_STAFF_DESIGNATION from slos_staff_home_trn where winame ='" + PID
					+ "'";
			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
			String prodCode = "";
			if (!listqueryForDesignation.isEmpty()) {
				inClauseStaff = "666,667,670";
				String designation2 = listqueryForDesignation.get(0).get(0);
				if (designation2 != null && !designation2.trim().isEmpty()) {
					String queryForMaLoanAmout = "select MAX_LOAN_AMT_EX from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
							+ ") and probation_tag NOT IN ('Y') order by sub_product_code_cbs";
					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
					if (!listqueryForMaLoanAmout.isEmpty()) {
						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);

					}

				}
			}

		} else {
			inClauseStaff = "666,667,670";
			String queryForDesignation = "select DESIGNATION from slos_staff_home_trn where winame ='" + PID + "'";
			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
			String prodCode = "";
			if (!listqueryForDesignation.isEmpty()) {
				String designation2 = listqueryForDesignation.get(0).get(0);
				if (designation2 != null && !designation2.trim().isEmpty()) {
					String queryForMaLoanAmout = "select MAX_LOAN_AMT from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
							+ ") and probation_tag NOT IN ('Y') order by sub_product_code_cbs";
					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
					if (!listqueryForMaLoanAmout.isEmpty()) {
						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);

					}

				}
			}

		}

		String limit = "";
		double totallimit = 0.0;
		double utilized = 0.0;
		double totalAvailable = 0.0;

		double calculateLoanUtilizedBorrower = calculateLoanUtilizedBorrower(ifr);
		totallimit = (maxLoanAMtbrower == null || maxLoanAMtbrower.trim().isEmpty()) ? 0.0
				: Double.parseDouble(maxLoanAMtbrower);
		// totallimit = Double.parseDouble(maxLoanAMtbrower);
		Log.consoleLog(ifr, "totallimit===>" + totallimit);
		utilized = calculateLoanUtilizedBorrower;
		Log.consoleLog(ifr, "utilized===>" + utilized);
		totalAvailable = totallimit - utilized;
		Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);

		String limitForRenovation = "";
		double limitForRen = 0.0;
		double limitAvailable = 0.0;
		double limitAvail = 0.0;
		String queryForLimitRenovation = " SELECT \r\n" + " SUM(NVL(c.limit, 0)) AS limitCob \r\n" + "  FROM \r\n"
				+ " SLOS_ALL_ACTIVE_PRODUCT c\r\n" + " WHERE \r\n" + " c.productcode IN (" + inClauseAHL + ")\r\n"
				+ " AND c.winame = '" + PID + "'";

		// Log.consoleLog(ifr, "queryForLimit==>" + queryForLimit);
		List<List<String>> listqueryqueryForLimitRenovation = ifr.getDataFromDB(queryForLimitRenovation);
		// double totallimitRenovation = Double.parseDouble(maxLoanAMtbrowerRenovation);

		double totallimitRenovation = (maxLoanAMtbrowerRenovation == null
				|| maxLoanAMtbrowerRenovation.trim().isEmpty()) ? 0.0 : Double.parseDouble(maxLoanAMtbrowerRenovation);
		Log.consoleLog(ifr, "totallimitRenovation===>" + totallimitRenovation);
		if (listqueryqueryForLimitRenovation != null && !listqueryqueryForLimitRenovation.isEmpty()
				&& listqueryqueryForLimitRenovation.get(0).get(0) != null) {
			limitForRenovation = listqueryqueryForLimitRenovation.get(0).get(0);
			Log.consoleLog(ifr, "limitForRenovation===>" + limitForRenovation);
			Log.consoleLog(ifr, "Inside loop==>" + limitForRenovation);
			// limitForRenovation = listqueryqueryForLimitRenovation.get(0).get(0);
			limitForRen = (limitForRenovation == null || limitForRenovation.trim().isEmpty()) ? 0.0
					: Double.parseDouble(limitForRenovation);
			// limitForRen = Double.parseDouble(limitForRenovation);
			Log.consoleLog(ifr, "limitForRen===>" + limitForRen);
			limitAvail = totallimitRenovation - limitForRen;
			Log.consoleLog(ifr, "limitAvail===>" + limitAvail);
			limitAvailable = Math.min(totalAvailable, limitAvail);
			Log.consoleLog(ifr, "limitAvailable===>" + limitAvailable);
		} else {
			limitForRen = 0.0;
			limitAvailable = totallimitRenovation;
			Log.consoleLog(ifr, "limitAvailable===>" + limitAvailable);
		}

		long totalAvailableL = (long) totalAvailable;
		Log.consoleLog(ifr, "totalAvailableL===>" + totalAvailableL);
		long utilizedL = (long) utilized;
		Log.consoleLog(ifr, "utilizedL===>" + utilizedL);
		long totallimitL = (long) totallimit;
		Log.consoleLog(ifr, "totallimitL===>" + totallimitL);
		long totalAvailableRenovationL = (long) limitAvailable;
		Log.consoleLog(ifr, "totalAvailableRenovationL===>" + totalAvailableRenovationL);
		long utilizedRenovationL = (long) limitForRen;
		Log.consoleLog(ifr, "utilizedRenovationL===>" + utilizedRenovationL);
		long totallimitRenovationL = (long) totallimitRenovation;
		Log.consoleLog(ifr, "totallimitRenovationL===>" + totallimitRenovationL);

		String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='" + totalAvailableL + "',TOTAL_HL_UTIL='"
				+ utilizedL + "',TOTAL_HL_ELIG='" + totallimitL + "',RENOVATION_ELIG='" + totallimitRenovationL
				+ "',RENOVATION_UTIL='" + utilizedRenovationL + "',RENOVATION_AVAIL='" + totalAvailableRenovationL
				+ "' where winame='" + PID + "'";
		Log.consoleLog(ifr, "updateQuery===>" + updateQuery);

		ifr.saveDataInDB(updateQuery);
		Log.consoleLog(ifr, "statutory_deductions==>" + updateQuery);

		String available = "";
		String utilized1 = "";
		String eligible = "";
		String renovationAvail = "";
		String renovationUtilized = "";
		String renovationeligible = "";
		String activityName = ifr.getActivityName().toString();
		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker") || activityName.equalsIgnoreCase("Staff_HL_RO_Maker")
				|| activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {
			String purposeQuery = "select TOTAL_HL_AVAIL,TOTAL_HL_UTIL,TOTAL_HL_ELIG,RENOVATION_AVAIL,RENOVATION_UTIL,RENOVATION_ELIG from slos_staff_home_trn where winame='"
					+ PID + "'";
			List<List<String>> purposeQueryRes = ifr.getDataFromDB(purposeQuery);
			Log.consoleLog(ifr, "purposeQuery===>" + purposeQuery);
			Log.consoleLog(ifr, "purposeQueryRes===>" + purposeQueryRes);
			if (!purposeQueryRes.isEmpty()) {
				available = purposeQueryRes.get(0).get(0);
				Log.consoleLog(ifr, "available" + available);
				utilized1 = purposeQueryRes.get(0).get(1);
				Log.consoleLog(ifr, "utilized" + utilized);
				eligible = purposeQueryRes.get(0).get(2);
				Log.consoleLog(ifr, "eligible" + eligible);
				renovationAvail = purposeQueryRes.get(0).get(3);
				Log.consoleLog(ifr, "renovationAvail" + renovationAvail);
				renovationUtilized = purposeQueryRes.get(0).get(4);
				Log.consoleLog(ifr, "renovationUtilized" + renovationUtilized);
				renovationeligible = purposeQueryRes.get(0).get(5);
				Log.consoleLog(ifr, "renovationeligible" + renovationeligible);
			}
			String value = ifr.getValue("P_HL_LD_Purpose").toString();
			if (value.contains("Renovation")) {

				showRenovation(ifr);

			} else {
				showSHL(ifr);
			}
		}

		return "success";
	}

	public String onClickAddModifyDeleteNetworth(IFormReference ifr) {
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String query = "SELECT ApplicantType,Sum(Loan_LiabOut) FROM LOS_NL_AL_LIAB_VAL WHERE PID='" + PID
					+ "' GROUP BY ApplicantType";
			List<List<String>> result = cf.mExecuteQuery(ifr, query, "FinancialInfoLiability Query:");
			HashMap liab = new HashMap();
			for (List<String> result1 : result) {
				liab.put(result1.get(0), result1.get(1));
			}
			query = "SELECT ApplicantType,Sum(AccBalance_Value) FROM LOS_NL_AL_ASSET_DET  WHERE PID='" + PID
					+ "' GROUP BY ApplicantType";
			result = cf.mExecuteQuery(ifr, query, "FinancialInfoAsset Query:");
			HashMap asset = new HashMap();
			for (List<String> result1 : result) {
				asset.put(result1.get(0), result1.get(1));
			}
			query = "select insertionorderID from los_nl_basic_info where pid='" + PID + "'";
			result = cf.mExecuteQuery(ifr, query, "BASICINFO Query:");
			BigDecimal total = new BigDecimal("0");
			;
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
				total = new BigDecimal(assetValue).subtract(new BigDecimal(liabValue));
				row.put("QNL_AL_NETWORTH_TotOutstanding", String.valueOf(total));
				NetworthGrid.add(row);
			}
			Log.consoleLog(ifr, "NetworthGrid:" + NetworthGrid);
			ifr.addDataToGrid("ALV_AL_NETWORTH", NetworthGrid);
			if (total.compareTo(BigDecimal.ZERO) < 0) {
				JSONObject message = new JSONObject();
				message.put("showMessage", cf.showMessage(ifr, "", "error", "Total networth is Negative."));
				return message.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccAddModifyDeleteNetworth:" + e);
			Log.errorLog(ifr, "Exception in mAccAddModifyDeleteNetworth:" + e);
		}
		return "";
	}

	public void populateFieldsOnListViewOccupation(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside populateFieldsOnListViewOccupation::");
		String occType = ifr.getValue("QNL_BASIC_INFO_CNL_OCCUPATION_INFO_OccupationType").toString();
		Log.consoleLog(ifr, "Ocupation Type selected:: " + occType);
		if (occType.equalsIgnoreCase("Non-Salaried")) {
			ifr.setStyle("YEAR_1_PARTY_OCC", "visible", "true");
			ifr.setStyle("GROSS_ANNUAL_INCOME_1_PARTY_OCC", "visible", "true");
			ifr.setStyle("DEDUCTION_ANNUAL_INCOME_1_PARTY_OCC", "visible", "true");
			ifr.setStyle("NET_ANNUAL_INCOME_1_PARTY_OCC", "visible", "true");

			ifr.setStyle("YEAR_2_PARTY_OCC", "visible", "true");
			ifr.setStyle("GROSS_ANNUAL_INCOME_2_PARTY_OCC", "visible", "true");
			ifr.setStyle("DEDUCTION_ANNUAL_INCOME_2_PARTY_OCC", "visible", "true");
			ifr.setStyle("NET_ANNUAL_INCOME_2_PARTY_OCC", "visible", "true");

			ifr.setStyle("YEAR_3_PARTY_OCC", "visible", "true");
			ifr.setStyle("GROSS_ANNUAL_INCOME_3_PARTY_OCC", "visible", "true");
			ifr.setStyle("DEDUCTION_ANNUAL_INCOME_3_PARTY_OCC", "visible", "true");
			ifr.setStyle("NET_ANNUAL_INCOME_3_PARTY_OCC", "visible", "true");

			ifr.setStyle("AVERAGE_GROSS_ANNUAL_INCOME_PARTY_OCC", "visible", "true");
			ifr.setStyle("AVERAGE_DEDUCTION_ANNUAL_INCOME_PARTY_OCC", "visible", "true");
			ifr.setStyle("AVERAGE_NET_ANNUAL_INCOME_PARTY_OCC", "visible", "true");

			ifr.setStyle("CALCULATE_BTN_PARTY_OCC", "visible", "true");
		}
	}

	public String setCbsProductCodeHL(IFormReference ifr) {
		String subProductCode = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
		List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
		Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
		if (!listqueryproductandScheduleCode.isEmpty()) {
			subProductCode = listqueryproductandScheduleCode.get(0).get(0);

		}
		return subProductCode;
	}

	public String autoPopulateCollateralDropdownSHL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "HLPortal:autoPopulateCollateralDropdownSHL");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String subProductQuery = "SELECT TRIM(SUB_PRODUCT_CODE) FROM SLOS_HOME_PRODUCT_SHEET WHERE TRIM(SUB_PRODUCT) IN (SELECT TRIM(HL_PRODUCT) FROM SLOS_STAFF_HOME_TRN WHERE WINAME='"
					+ PID + "')";
			Log.consoleLog(ifr, "subProductQuery:: " + subProductQuery);
			List<List<String>> subProductQueryRes = cf.mExecuteQuery(ifr, subProductQuery,
					"Fetching sub product code from slos tables");
			String subProductCode = subProductQueryRes.get(0).get(0);
			Log.consoleLog(ifr, "Fetched subProductCode:: " + subProductCode);
			if (subProductCode.equalsIgnoreCase("670")) {
				Log.consoleLog(ifr, "Select product is Renovation/Repair::");
				ifr.setValue("COLL_SUB_PROD_CODE", subProductCode);
			} else {
				Log.consoleLog(ifr, "Select product is General Staff Home Loan::");
				ifr.setValue("COLL_SUB_PROD_CODE", "GEN");
				subProductCode = "GEN";
			}
			shlccc.populateDropDownForCollateralSHL(ifr, subProductCode, "P_CD_HL_Asset_Security_type");
			shlccc.populateDropDownForCollateralSHL(ifr, subProductCode,
					"Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_SECURITYTYPE");
			shlccc.changeFieldsMandatoryStatus(ifr);

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in autoPopulateCollateralDropdownSHL::SHLPortal" + e);
		}
		return "";
	}

	public String UpdateApprovalMatrix(IFormReference ifr, String value) {
		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String roBranch = "";
			String coBranch = "";
			String availBranch = "";

			String staffIdQuery = "select STAFF_NUMBER from slos_staff_home_trn where winame like '" + processInstanceId
					+ "'";
			List<List<String>> resultstaffIdQuery = ifr.getDataFromDB(staffIdQuery);

			String staffId = resultstaffIdQuery.get(0).get(0).toString();
			availBranch = ifr.getValue("P_CB_LD_ProcessingBranchName").toString();
//			int branchNumber = Integer.parseInt(availBranch);
//			availBranch = String.format("%05d", branchNumber);

			String queryForBranch = "SELECT RO_CODE,CO_CODE, BRANCHCODE FROM LOS_M_BRANCH WHERE BRANCHNAME='"
					+ availBranch + "'";
			List<List<String>> resultqueryForBranch = ifr.getDataFromDB(queryForBranch);
			Log.consoleLog(ifr, "queryForBranch===>" + queryForBranch);

			if (!resultqueryForBranch.isEmpty()) {
				roBranch = resultqueryForBranch.get(0).get(0);
				coBranch = resultqueryForBranch.get(0).get(1);
				availBranch = resultqueryForBranch.get(0).get(2).trim();
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

	public String autopopulate_headerDetailsSHL(IFormReference ifr) {
		Log.consoleLog(ifr, "inside autopopulate_headerDetailsSHL==>");
		String activityName = ifr.getActivityName();
		Log.consoleLog(ifr, "activityName==>" + activityName);
		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		String query = "SELECT \"NAME\", BRANCH_NAME FROM SLOS_STAFF_HOME_TRN WHERE WINAME='" + ProcessInstanceId + "'";
		List<List<String>> data = cf.mExecuteQuery(ifr, query, "Execute query for fetching SHL data");
		String staffName = data.get(0).get(0);
		Log.consoleLog(ifr, "staffName==>" + staffName);
		ifr.setValue("Q_FinalAuthorityDesignation", staffName);
		String branchName = data.get(0).get(1);
		Log.consoleLog(ifr, "branchName==>" + branchName);
		ifr.setValue("QL_LEAD_DET_LeadSource", "Portal");
		ifr.setValue("CustomerName", staffName);
		ifr.setValue("QL_LEAD_DET_BranchName", branchName);

		return "";
	}

	public void onLoadExternalDeductions(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String joiningDate = "";
		String retireDate = "";
		String doB = "";

		String query = " select JOINING_DATE,RETIREMENT_DATE,DATE_OF_BIRTH from slos_staff_home_trn where winame='"
				+ processInstanceId + "'";
		List<List<String>> resList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query==>" + query);
		SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (!resList.isEmpty()) {
			joiningDate = resList.get(0).get(0);
			retireDate = resList.get(0).get(1);
			doB = resList.get(0).get(2);
		}
		ifr.setValue("Date_of_joining", joiningDate);
		ifr.setValue("Date_of_Retirement", retireDate);
		ifr.setValue("DATEOFBIRTH", retireDate);

		String queryForGrid = "select LOAN_ACC_NUMBER,productname,limit,OUTSTANDING_BALANCE,EMI,roi,tenure from SLOS_ALL_ACTIVE_PRODUCT where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_ALL_ACTIVE_PRODUCT==>" + queryForGrid);
		List<List<String>> responseForGrid = ifr.getDataFromDB(queryForGrid);
		responseForGrid = ifr.getDataFromDB(queryForGrid);
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

	}

	public void backOffice(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='BackOffice' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

	}

	public void onLoadFinalScreenHL(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Summary' WHERE WINAME='" + processInstanceId
				+ "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

	}

	public String uploadMandatoryDocs(IFormReference ifr, String value) {
		String documentNames = "";
		if (value.equalsIgnoreCase("Staff_HL_Branch_Maker")) {
			String getMandatoryDocuments = "SELECT NAME from SLOS_HL_DOCUMENTS where MANDATORY = 'Y' AND GENERATE='U'";
			Log.consoleLog(ifr, "uploadMandatoryDocs query==>" + getMandatoryDocuments);
			List<List<String>> listMandatoryDocuments = ifr.getDataFromDB(getMandatoryDocuments);
			Log.consoleLog(ifr, "listMandatoryDocuments==>" + listMandatoryDocuments);
			List<String> names = listMandatoryDocuments.stream().flatMap(tempMap -> tempMap.stream())
					.collect(Collectors.toList());
			documentNames = names.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));
		} 
		return documentNames;
	}

	public String NetSalSet(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String netSalary = "";
		String queryForEXStaff = "select NET_SALARY from SLOS_STAFF_HOME_TRN where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "SLOS_STAFF_HOME_TRN queryForEXStaff==>" + queryForEXStaff);
		List<List<String>> resqueryForEXStaff = ifr.getDataFromDB(queryForEXStaff);
		if (!resqueryForEXStaff.isEmpty()) {
			netSalary = resqueryForEXStaff.get(0).get(0);
			// netSalary = resqueryForEXStaff.get(0).get(1);
			return netSalary;
		}
		return "";
	}

	public void autoPopulateCoBorowerDetailsListViewBKFSHL(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String customerIdQuery = "select CUSTOMERID from los_trn_customersummary where winame='" + PID + "'";
		List<List<String>> customerIdRes = cf.mExecuteQuery(ifr, customerIdQuery, "Customer ID Query::");
		String customerId = "";
		if (!customerIdRes.isEmpty()) {
			customerId = customerIdRes.get(0).get(0).trim();
		}
		String customerIdFromGrid = ifr.getValue("SHL_BKF_CUST_ID").toString().trim();
		if (customerId.equalsIgnoreCase(customerIdFromGrid)) {
			ifr.setStyle("F_PartyDetails1", "visible", "false");
			ifr.setStyle("F_PartyDetails2", "visible", "false");
			ifr.setStyle("F_PartyDetails3", "visible", "false");
			ifr.setStyle("P_Industry_Details", "visible", "false");
		}
	}

	public void setUserID(IFormReference ifr, String value) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String activityName = ifr.getActivityName();
		String userName = "";
		Log.consoleLog(ifr, "activityName====>" + activityName);
		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_MAKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Checker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET BRANCH_CHECKER_ID='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_HL_RO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_MAKER_ID='" + userName + "' where winame='" + PID + "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_HL_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET RO_SANCTION_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}
		if (activityName.equalsIgnoreCase("Staff_HL_CO_Sanction")) {
			userName = ifr.getUserName().toString();
			String updateQuery = "UPDATE slos_Staff_trn SET CO_SANCTION_ID ='" + userName + "' where winame='" + PID
					+ "'";
			ifr.saveDataInDB(updateQuery);

		}

	}

	public String checkLoanAccNumHomeLoan(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String exStaffQuery = "select count(*) from slos_trn_loandetails where LOAN_ACCOUNTNO IS NOT NULL and pid ='"
				+ PID + "'";
		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
		List Result1 = ifr.getDataFromDB(exStaffQuery);
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
				accountCreatedDate = queryForLoanAccountNumberRes.get(0).get(1).toString();
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

	public String checkIsAppForSelfHL(IFormReference ifr) {
		Log.consoleLog(ifr, "::Inside checkIsAppForSelfHL Method::");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String applicantSNoQ = "select STAFF_NUMBER from slos_staff_home_trn WHERE WINAME='" + PID + "'";
		Log.consoleLog(ifr, "applicantSNoQ query:: " + applicantSNoQ);
		List<List<String>> resApplicantSNo = ifr.getDataFromDB(applicantSNoQ);
		String applicantStaffNO = "";
		if (!resApplicantSNo.isEmpty()) {
			applicantStaffNO = resApplicantSNo.get(0).get(0).trim();
		}
		Log.consoleLog(ifr, "applicantStaffNO:: " + applicantStaffNO);
		String currentUser = ifr.getUserName().trim();
		Log.consoleLog(ifr, "currentUser:: " + currentUser);
		if (currentUser.equalsIgnoreCase(applicantStaffNO)) {
			Log.consoleLog(ifr, "Inside true Condition where Applicant Is Current User");
			return "yes";
		}
		return "no";
	}

	public String checkIsAppForSelfVLOD(IFormReference ifr) {
		Log.consoleLog(ifr, "::Inside checkIsAppForSelfVLOD Method::");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String applicantSNoQ = "select STAFF_NUMBER from slos_staff_trn WHERE WINAME='" + PID + "'";
		Log.consoleLog(ifr, "applicantSNoQ query:: " + applicantSNoQ);
		List<List<String>> resApplicantSNo = ifr.getDataFromDB(applicantSNoQ);
		String applicantStaffNO = "";
		if (!resApplicantSNo.isEmpty()) {
			applicantStaffNO = resApplicantSNo.get(0).get(0).trim();
		}
		Log.consoleLog(ifr, "applicantStaffNO:: " + applicantStaffNO);
		String currentUser = ifr.getUserName().trim();
		Log.consoleLog(ifr, "currentUser:: " + currentUser);
		if (currentUser.equalsIgnoreCase(applicantStaffNO)) {
			Log.consoleLog(ifr, "Inside true Condition where Applicant Is Current User");
			return "yes";
		}
		return "no";
	}

	public String HlStaffEligibilityOnselectPurpose(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String retVal = getLoanEligiblityCalc(ifr, value, processInstanceId);
		if (retVal.contains("Error")) {
			return retVal;
		}

		return "success";
	}

	private String getLoanEligiblityCalc(IFormReference ifr, String value, String processInstanceId) {
		String available = "";
		String utilized = "";
		String eligible = "";
		String renovationAvail = "";
		String renovationUtilized = "";
		String renovationeligible = "";
		String purposeQuery = "select TOTAL_HL_AVAIL,TOTAL_HL_UTIL,TOTAL_HL_ELIG,RENOVATION_AVAIL,RENOVATION_UTIL,RENOVATION_ELIG from slos_staff_home_trn where winame='"
				+ processInstanceId + "'";
		List<List<String>> purposeQueryRes = ifr.getDataFromDB(purposeQuery);
		Log.consoleLog(ifr, "purposeQuery===>" + purposeQuery);
		Log.consoleLog(ifr, "purposeQueryRes===>" + purposeQueryRes);
		if (!purposeQueryRes.isEmpty()) {
			available = purposeQueryRes.get(0).get(0);
			Log.consoleLog(ifr, "available" + available);
			utilized = purposeQueryRes.get(0).get(1);
			Log.consoleLog(ifr, "utilized" + utilized);
			eligible = purposeQueryRes.get(0).get(2);
			Log.consoleLog(ifr, "eligible" + eligible);
			renovationAvail = purposeQueryRes.get(0).get(3);
			Log.consoleLog(ifr, "renovationAvail" + renovationAvail);
			renovationUtilized = purposeQueryRes.get(0).get(4);
			Log.consoleLog(ifr, "renovationUtilized" + renovationUtilized);
			renovationeligible = purposeQueryRes.get(0).get(5);
			Log.consoleLog(ifr, "renovationeligible" + renovationeligible);
		}
		// String prodpurp = ifr.getValue("P_HL_LD_Purpose").toString();

		BigDecimal bd = new BigDecimal(eligible);
		String formattedeligible = bd.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		BigDecimal bd1 = new BigDecimal(utilized);
		String formattedutilized = bd1.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		BigDecimal bd2 = new BigDecimal(available);
		String formattedavailable = bd2.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		BigDecimal bd3 = new BigDecimal(renovationAvail);
		String formattedAvailRenovation = bd3.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		BigDecimal bd4 = new BigDecimal(renovationUtilized);
		String formattedUtilizeRenovation = bd4.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		BigDecimal bd5 = new BigDecimal(renovationeligible);
		String formattedEligibleRenovation = bd5.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();

		ifr.setValue("SHL_Elg", formattedeligible);
		Log.consoleLog(ifr, "SHL_Elg===" + formattedeligible);
		ifr.setValue("SHL_Util", formattedutilized);
		Log.consoleLog(ifr, "SHL_Util===" + formattedutilized);
		ifr.setValue("SHL_Avail", formattedavailable);
		Log.consoleLog(ifr, "SHL_Avail===" + formattedavailable);
		ifr.setValue("Renovation_Avail_SHL", formattedAvailRenovation);
		ifr.setValue("Renovation_Util_SHL", formattedUtilizeRenovation);
		ifr.setValue("Renovation_Elig_SHL", formattedEligibleRenovation);

		if (Double.parseDouble(available) <= 0) {
			return "Error, Available amount is less than or equals 0.";
		}

		if (value.contains("Renovation")) {

			showRenovation(ifr);

		} else {
			showSHL(ifr);
		}
		return "success";
	}

	public String onChangeNoBorrowerDetailsYes(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String maxLoanAMtCobrower = "";
		String maxLoanAMtbrower = "";
		String customerId = "";
		String limitCob = "";
		String maxLoanAMtCobrowerRneovation = "";
		String maxLoanAMtbrowerRenovation = "";
		String maxLoanCobrrowerAMtbrower = "";
		String maxLoanAMtCobrowerRenovation = "";

		String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
		List Result1 = ifr.getDataFromDB(exStaffQuery);
		String Count1 = Result1.toString().replace("[", "").replace("]", "");
		Log.consoleLog(ifr, "Count1==>" + Count1);
		// String coBorrowerDesignation = ifr.getValue("COBORR_DESIGNATION").toString();
		String queryForRelationship = "select RELATIONSHIPWITHAPPLICANT,CONSIDERELIGIBILITY from los_nl_basic_info where pid='"
				+ processInstanceId + "'";
		List<List<String>> resqueryForRelationship = ifr.getDataFromDB(queryForRelationship);

		boolean containsSAndYes = resqueryForRelationship.stream()
				.anyMatch(row -> "S".equalsIgnoreCase(row.get(0)) && "Yes".equalsIgnoreCase(row.get(1)));
		Log.consoleLog(ifr, "containsSAndYes===>" + containsSAndYes);

		String staff = "";
		String STAFFSTATUSCB = "";
		String queryForRelationStaff = "select STAFFIDCB, STAFFSTATUSCB,CB_DESIGNATION from LOS_NL_OCCUPATION_INFO a join los_nl_basic_info b on b.f_key = a.f_key where b.pid='"
				+ processInstanceId + "'";

		List<List<String>> resqueryqueryForRelationStaff = ifr.getDataFromDB(queryForRelationStaff);

		// Initialize designation as null
		String coBorrowerDesignation = null;

// Find the first valid row and get the designation
		for (List<String> row : resqueryqueryForRelationStaff) {
			if (row != null && row.size() >= 3 && // at least 3 columns for CB_DESIGNATION
					row.get(0) != null && !row.get(0).trim().isEmpty() && // STAFFIDCB
					row.get(1) != null && !row.get(1).trim().isEmpty() // STAFFSTATUSCB
			) {
				coBorrowerDesignation = row.get(2); // CB_DESIGNATION
				// break; // Stop at the first valid row
			}
		}

		Log.consoleLog(ifr, "hasValidStaffRow ===> " + coBorrowerDesignation);

		if (Integer.parseInt(Count1) > 0) {

			String queryForDesignation = "select EX_STAFF_DESIGNATION from slos_staff_home_trn where winame ='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
			String prodCode = "";
			if (!listqueryForDesignation.isEmpty()) {
				String inClauseStaff = "666,667,670";
				String designation2 = listqueryForDesignation.get(0).get(0);
				if (designation2 != null && !designation2.trim().isEmpty()) {
					String queryForMaLoanAmout = "select MAX_LOAN_AMT_EX from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
							+ ") and probation_tag NOT IN ('Y') order by sub_product_code_cbs";
					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
					if (!listqueryForMaLoanAmout.isEmpty()) {
						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);

					}

				}
			}

		} else {
			String inClauseStaff = "666,667,670";
			String queryForDesignation = "select DESIGNATION from slos_staff_home_trn where winame ='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "borrower Designation===>" + queryForDesignation);
			List<List<String>> listqueryForDesignation = ifr.getDataFromDB(queryForDesignation);
			String prodCode = "";
			if (!listqueryForDesignation.isEmpty()) {
				String designation2 = listqueryForDesignation.get(0).get(0);
				if (designation2 != null && !designation2.trim().isEmpty()) {
					String queryForMaLoanAmout = "select MAX_LOAN_AMT from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
							+ designation2 + "' and sub_product_code_cbs in  (" + inClauseStaff
							+ ") and probation_tag NOT IN ('Y') order by sub_product_code_cbs";
					Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
					List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
					Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
					if (!listqueryForMaLoanAmout.isEmpty()) {
						maxLoanAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
						maxLoanAMtbrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
						Log.consoleLog(ifr, "maxLoanAMtbrower===>" + maxLoanAMtbrower);

					}

				}
			}

			if (coBorrowerDesignation != null && !coBorrowerDesignation.trim().isEmpty()) {
				String queryForMaLoanAmout = "select MAX_LOAN_AMT from Staff_Hl_Prod_Des_Matrix where DESIGNATION ='"
						+ coBorrowerDesignation + "' and sub_product_code_cbs in  (" + inClauseStaff
						+ ") and probation_tag NOT IN ('Y') order by sub_product_code_cbs";
				Log.consoleLog(ifr, "borrower maxAmount===>" + queryForMaLoanAmout);
				List<List<String>> listqueryForMaLoanAmout = ifr.getDataFromDB(queryForMaLoanAmout);
				Log.consoleLog(ifr, "listqueryForMaLoanAmout===>" + listqueryForMaLoanAmout);
				if (!listqueryForMaLoanAmout.isEmpty()) {
					maxLoanCobrrowerAMtbrower = listqueryForMaLoanAmout.get(0).get(0);
					maxLoanAMtCobrowerRenovation = listqueryForMaLoanAmout.get(1).get(0);
					Log.consoleLog(ifr, "maxLoanCobrrowerAMtbrower===>" + maxLoanAMtbrower);

				}

			}

		}

		if (!containsSAndYes) {

			onChangeNoBorrowerDetails(ifr);

		}

		else if (containsSAndYes) {
			Validator valid = new KnockOffValidator("None");
			String validProductCodeQuery1 = "SELECT productcode,productType FROM STAFF_hL_VALID_PRODUCT_CODE where isactive='Y'";
			List<List<String>> validProductCodeResult1 = ifr.getDataFromDB(validProductCodeQuery1);
			List<String> eHLProductCodes = valid.getFlatList(ifr, validProductCodeResult1, "EHL");
			List<String> aHLProductCode = valid.getFlatList(ifr, validProductCodeResult1, "AHL");
			List<String> temp = new ArrayList<>();
			temp.addAll(eHLProductCodes);
			temp.addAll(aHLProductCode);

			String inClauseEHL = eHLProductCodes.stream().map(code -> "'" + code + "'")
					.collect(Collectors.joining(","));

			String inClauseAHL = aHLProductCode.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));
			double calculateLoanUtilzed = calculateLoanUtilzed(ifr);

			Log.consoleLog(ifr, "calculateLoanUtilzed===>" + calculateLoanUtilzed);

			String queryForEMI = "SELECT NVL(SUM(a.limit),0) FROM SLOS_ALL_ACTIVE_PRODUCT a WHERE a.productcode IN ("
					+ inClauseEHL + ") AND a.winame = '" + processInstanceId
					+ "' AND EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c WHERE c.LOAN_ACC_NUMBER = a.LOAN_ACC_NUMBER AND c.winame = a.winame)";

			Log.consoleLog(ifr, "queryForEMI===>" + queryForEMI);
			List<List<String>> listqueryForEMI = ifr.getDataFromDB(queryForEMI);
			Log.consoleLog(ifr, "listqueryForEMI===>" + listqueryForEMI);

			String limitUsedAHL = "";
			double reducedValAHL = 0.0;
			String limitAHL = "";

			String limitUsed = "";
			String limit = "";
			double reducedVal = 0.0;
			if (!listqueryForEMI.isEmpty()) {
				limitUsed = listqueryForEMI.get(0).get(0);
				reducedVal = calculateLoanUtilzed - parseDoubleSafe(limitUsed);
				limit = String.valueOf(reducedVal);
				Log.consoleLog(ifr, "limit===>" + limit);
			}

			double calculateLoanRenovationUtilized = calculateLoanRenovationUtilized(ifr);
			Log.consoleLog(ifr, "calculateLoanRenovationUtilized===>" + calculateLoanRenovationUtilized);

			String queryForEMIAHL = "SELECT NVL(SUM(a.limit),0) FROM SLOS_ALL_ACTIVE_PRODUCT a WHERE a.productcode IN ("
					+ inClauseAHL + ") AND a.winame = '" + processInstanceId
					+ "' AND EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT_COBORROWER c WHERE c.LOAN_ACC_NUMBER = a.LOAN_ACC_NUMBER AND c.winame = a.winame)";

			Log.consoleLog(ifr, "queryForEMIAHL===>" + queryForEMIAHL);
			List<List<String>> listqueryForEMIAHL = ifr.getDataFromDB(queryForEMIAHL);
			Log.consoleLog(ifr, "listqueryForEMIAHL===>" + listqueryForEMIAHL);

			String limitUsedRenovation = "";
			String limitRenovation = "";
			double reducedValRenovation = 0.0;
			if (!listqueryForEMIAHL.isEmpty()) {
				limitUsedRenovation = listqueryForEMIAHL.get(0).get(0);
				reducedValRenovation = calculateLoanRenovationUtilized - parseDoubleSafe(limitUsedRenovation);
				limitRenovation = String.valueOf(reducedValRenovation);
				Log.consoleLog(ifr, "limitRenovation===>" + limitRenovation);
			}

			double totallimit = parseDoubleSafe(maxLoanCobrrowerAMtbrower) + parseDoubleSafe(maxLoanAMtbrower);
			Log.consoleLog(ifr, "totallimit===>" + totallimit);

			double totallimitRenovation = parseDoubleSafe(maxLoanAMtbrowerRenovation)
					+ parseDoubleSafe(maxLoanAMtCobrowerRenovation);
			Log.consoleLog(ifr, "totallimitRenovation===>" + totallimitRenovation);

			double utilized = parseDoubleSafe(String.valueOf(limit));
			Log.consoleLog(ifr, "utilized===>" + utilized);
			double utilizedRenovation = parseDoubleSafe(String.valueOf(limitRenovation));
			Log.consoleLog(ifr, "utilizedRenovation===>" + utilizedRenovation);

			double totalAvailable = totallimit - utilized;
			Log.consoleLog(ifr, "totalAvailable===>" + totalAvailable);
			double totalAvailableRenovate = totallimitRenovation - utilizedRenovation;
			Log.consoleLog(ifr, "totalAvailableRenovate===>" + totalAvailableRenovate);
			double totalAvailableRenovation = Math.min(totalAvailable, totalAvailableRenovate);
			Log.consoleLog(ifr, "totalAvailableRenovation===>" + totalAvailableRenovation);

			long totalAvailableL = (long) totalAvailable;
			long utilizedL = (long) utilized;
			long totallimitL = (long) totallimit;
			long utilizedRenovationL = (long) utilizedRenovation;
			long totallimitRenovationL = (long) totallimitRenovation;
			long totalAvailableRenovationL = (long) totalAvailableRenovation;

			String updateQuery = "UPDATE slos_staff_home_trn SET TOTAL_HL_AVAIL='" + totalAvailableL
					+ "',TOTAL_HL_UTIL='" + utilizedL + "',TOTAL_HL_ELIG='" + totallimitL + "',RENOVATION_ELIG='"
					+ totallimitRenovationL + "',RENOVATION_UTIL='" + utilizedRenovationL + "',RENOVATION_AVAIL='"
					+ totalAvailableRenovationL + "' where winame='" + processInstanceId + "'";

			ifr.saveDataInDB(updateQuery);

			Log.consoleLog(ifr, "updateQuery===>" + updateQuery);
		}
		String activityName = ifr.getActivityName().toString();
		if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker") || activityName.equalsIgnoreCase("Staff_HL_RO_Maker")
				|| activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {
			String value = ifr.getValue("P_HL_LD_Purpose").toString();
			String retVal = getLoanEligiblityCalc(ifr, value, processInstanceId);
			if (retVal.contains("Error")) {
				return retVal;
			}

		}
		return "success";

	}

	public void UpdateBorrowerMobileNumber(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String mobile = "";
		String fkey = "";
		String queryForMobAndFkey = "select MOBILENUMBER,F_KEY from los_nl_basic_info where pid='" + processInstanceId
				+ "' and applicanttype='B'";
		Log.consoleLog(ifr, "queryForMobAndFkey query===>" + queryForMobAndFkey);
		List<List<String>> queryForMobAndFkeyRes = ifr.getDataFromDB(queryForMobAndFkey);
		Log.consoleLog(ifr, "queryForMobAndFkeyRes===>" + queryForMobAndFkeyRes);
		if (!queryForMobAndFkeyRes.isEmpty()) {
			mobile = queryForMobAndFkeyRes.get(0).get(0);
			fkey = queryForMobAndFkeyRes.get(0).get(1);
		}
		String deletequery = "delete from los_l_basic_info_ni where OFFPHONENO='" + mobile + "' and F_KEY='" + fkey
				+ "'";
		ifr.saveDataInDB(deletequery);
		String insertQuery = "insert into los_l_basic_info_ni(OFFPHONENO, F_KEY) values ('" + mobile + "', '" + fkey
				+ "')";
		ifr.saveDataInDB(insertQuery);

	}

	public String downLoadGeneratedDocument(IFormReference ifr, String value) {
		String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();

		Log.consoleLog(ifr, "downLoadSignedGeneratedDocument/getDocIndexQuery===>" + value);
		String docContent = getDocumentContent(ifr, value);
		return docContent;
	}

	public String getDocumentContent(IFormReference ifr, String query) {
		// List<List<String>> response = ifr.getDataFromDB(query);
		// Log.consoleLog(ifr, "getDocumentContent/response " + response);
		JSONArray jsonArr1 = new JSONArray();
		// if (response.size() > 0) {
		try {
			// for (int i = 0; i < response.size(); i++) {
			Log.consoleLog(ifr, "getDocumentContent/doceIndex " + query);
			String ominDocResponse = this.ominDocDownload.downloadDocumentFromOD(ifr, query);
			JSONParser parser = new JSONParser();
			JSONObject OutputJSON = (JSONObject) parser.parse(ominDocResponse);
			JSONObject resultObj = new JSONObject((Map) OutputJSON);
			Log.consoleLog(ifr, "getDocumentContent/doceIndex " + resultObj.get("statusCode"));
			if (!resultObj.get("statusCode").equals("500")) {
				jsonArr1.add(resultObj);
			}
			// }
			Log.consoleLog(ifr, "getDocumentContent/no of documents" + jsonArr1.size());
			if (jsonArr1.size() > 0) {
				return jsonArr1.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "getDocumentContent doc exception:- " + e.getMessage());
			Log.consoleLog(ifr, "getDocumentContent doc exception:- " + e.getLocalizedMessage());
		}
		// }
		Log.consoleLog(ifr, "getDocumentContent/final error");
		return RLOS_Constants.ERROR;
	}

	public String onLoadShowLoanDetailsHL(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String value = ifr.getValue("P_HL_LD_Purpose").toString();
		String retVal = getLoanEligiblityCalc(ifr, value, processInstanceId);
		if (retVal.contains("Error")) {
			return retVal;
		}
		return "success";

	}

	public String checkLoanAccNumKavach(IFormReference ifr) {
	    Log.consoleLog(ifr, "##Inside checkLoanAccNumKavach##");
	    String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

	    String exStaffQuery = "select count(*) from slos_trn_loandetails where LOAN_ACCOUNTNO IS NOT NULL and pid ='"
	            + PID + "'";
	    Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
	    List Result1 = ifr.getDataFromDB(exStaffQuery);
	    String Count1 = Result1.toString().replace("[", "").replace("]", "");
	    Log.consoleLog(ifr, "Count1==>" + Count1);
	    if (Integer.parseInt(Count1) > 0) {
	        String sanctionAmt = "";
	        String loanAccountNum = "";
	        String accountCreatedDate = "";

	        String queryForLoanAccountNumber = "SELECT LOAN_ACCOUNTNO,ACCOUNT_CREATEDDATE,SANCTION_AMOUNT from SLOS_TRN_LOANDETAILS where PID='"
	                + PID + "'";
	        List<List<String>> queryForLoanAccountNumberRes = ifr.getDataFromDB(queryForLoanAccountNumber);
	        Log.consoleLog(ifr, "queryForLoanAccountNumberRes : " + queryForLoanAccountNumberRes);
	        if (!queryForLoanAccountNumberRes.isEmpty()) {
	            loanAccountNum = queryForLoanAccountNumberRes.get(0).get(0);
	            accountCreatedDate = queryForLoanAccountNumberRes.get(0).get(1).toString();
	            sanctionAmt = queryForLoanAccountNumberRes.get(0).get(2).toString();
	        }
	        // String formaDate = getCurrentAPIDate(ifr);
	        ifr.setStyle("Disb_Loan_OD_Acc_Num", "visible", "true");
	        ifr.setStyle("Disb_Loan_Amt", "visible", "true");
	        ifr.setStyle("Disb_Loan_Acc_Open_Date", "visible", "true");
	        ifr.setValue("Disb_Loan_Amt", sanctionAmt);
	        ifr.setValue("Disb_Loan_OD_Acc_Num", loanAccountNum);
	        ifr.setValue("Disb_Loan_Acc_Open_Date", accountCreatedDate);
	        return "SUCCESS";

	    } else {
	        return "FAILED";
	    }

	}

	public String setNomineeAddAsApplicant(IFormReference ifr) {
	    String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
	    String add1 = "";
	    String add2 = "";
	    String add3 = "";
	    String city = "";
	    String zip = "";
	    String state = "";
	    String addressQ = "select PERMADDRESS1, PERMADDRESS2, PERMADDRESS3, PERMCITY, PERMZIP, PERMSTATE from los_trn_customersummary where winame = '"+PID+"'";
	    Log.consoleLog(ifr, "addressQ query:: " + addressQ);
	    List<List<String>> addressResult = ifr.getDataFromDB(addressQ);
	    Log.consoleLog(ifr, "addressResult:: " + addressResult);
	    if (!addressResult.isEmpty()) {
	        add1 = addressResult.get(0).get(0).trim();
	        add2 = addressResult.get(0).get(1).trim();
	        add3 = addressResult.get(0).get(2).trim();
	        city = addressResult.get(0).get(3).trim();
	        zip = addressResult.get(0).get(4).trim();
	        state = addressResult.get(0).get(5).trim();
	    }
	    ifr.setValue("nominee_line1_gold", add1);
	    ifr.setValue("nominee_line2_gold", add2);
	    ifr.setValue("nominee_line3_gold", add3);
	    ifr.setValue("nominee_city_gold", city);
	    ifr.setValue("nominee_zip_gold", zip);
	    ifr.setValue("nominee_state_gold", state);
	    return "SUCCESS";
	}

}
