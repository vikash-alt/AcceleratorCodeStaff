/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.portalAcceleratorCode;

import com.newgen.dlp.commonobjects.bso.CreateApplicationNumber;
import com.newgen.dlp.commonobjects.ccm.Email;
import com.newgen.dlp.commonobjects.ccm.SMS;
import com.newgen.dlp.commonobjects.ccm.WhatsApp;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.parser.JSONParser;
import java.math.MathContext;
import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryDatav2;
import com.newgen.dlp.integration.cbs.Demographic;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sandeep.g
 */
public class PortalCommonMethods {

	CommonMethods cm = new CommonMethods();
	CommonFunctionality cf = new CommonFunctionality();
	// SendEmail CBS_EMAIL = new SendEmail();
	// SendSMS CBS_SMS = new SendSMS();
	LoanEligibilityCheck LoanEC = new LoanEligibilityCheck();

	public String getParamConfig2(IFormReference ifr, String productCode, String paramType, String paramName) {
//        String query0 = "select PARAMVALUE from los_mst_configs where "
//                + "productcode='" + productCode + "' "
//                + "AND SUBPRODCODE='" + subProductCode + "' "
//                + "AND PARAMTYPE='" + paramType + "' "
//                + "AND PARAMNAME='" + paramName + "'";

		String query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS\n" + "WHERE PRODUCTCODE = '" + productCode + "' "
				+ "AND PARAMTYPE = '" + paramType + "' " + "AND PARAMNAME = '" + paramName + "'";

		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0 && result.get(0).get(0) != null) {
			return result.get(0).get(0);
		}
//return "0";
		return "";// Modified by Ahmed on 15-07-2024
	}

	public String populateProductDetail(IFormReference ifr, String control, String event, String value) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTPRODUCT").replaceAll("#PID#", PID);
		Log.consoleLog(ifr, "navigationNextProductDetails query " + IndexQuery);
		List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
		Log.consoleLog(ifr, "navigationNextProductDetails docIndexList " + docIndexList);
		if (docIndexList.size() == 0) {
			Log.consoleLog(ifr, "populateProductDetails");
			// JSONArray jsonArray = new JSONArray();
			String Product_Detail = ConfProperty.getCommonPropertyValue("Product_Details");
			Log.consoleLog(ifr, Product_Detail);
			String[] Product_mappings = Product_Detail.split(",");

			String BackoffmappProduct_Detail = ConfProperty
					.getCommonPropertyValue("Product_Details_SetValue_BackOffice");
			Log.consoleLog(ifr, BackoffmappProduct_Detail);
			String[] sttingmappingProduct_Detail = BackoffmappProduct_Detail.split(",");
			String[] srtProduct_Detail = new String[Product_mappings.length];
			for (int i = 0; i < Product_mappings.length; i++) {

				srtProduct_Detail[i] = ifr.getValue(Product_mappings[i]).toString();

			}
			for (int i = 0; i < sttingmappingProduct_Detail.length; i++) {
				jsonObject.put(sttingmappingProduct_Detail[i], srtProduct_Detail[i]);

			}
			jsonArray.add(jsonObject);
			Log.consoleLog(ifr, "jsonArray1234QNL_LOS_PROPOSED_FACILITY" + jsonArray);
			((IFormAPIHandler) ifr).addDataToGrid("QNL_LOS_PROPOSED_FACILITY", jsonArray, true);

		} else {
			Log.consoleLog(ifr, "inside else condition product ");
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-Product",
					ifr.getValue("PortalLOS_C_LD_Product").toString());
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-SubProduct",
					ifr.getValue("PortalLOS_C_LD_SubProduct").toString());
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-LoanPurpose",
					ifr.getValue("PortalLOS_C_LD_Purpose").toString());
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-Variant",
					ifr.getValue("PortalLOS_C_LD_Variant").toString());
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-ReqLoanAmt",
					ifr.getValue("PortalLOS_T_LD_LoanAmount").toString());
			ifr.setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY-Tenure",
					ifr.getValue("PortalLOS_T_LD_Tenure").toString());
		}
		return "";
	}

	public String populateAdharDetail(IFormReference ifr, String control, String event, String value) {

		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		String Adhar_Detail = ConfProperty.getCommonPropertyValue("Adhar_Details");
		Log.consoleLog(ifr, "Adhar_Detail" + Adhar_Detail);
		String[] Portal_mappingAdharDetail = Adhar_Detail.split(",");
		String adharBackoffmapp = ConfProperty.getCommonPropertyValue("Adhar_Details_SetValue_BackOffice");
		Log.consoleLog(ifr, "adharBackoffmapp" + adharBackoffmapp);

		String[] sttingmappingAdhar = adharBackoffmapp.split(",");
		String[] srtAdhar = new String[Portal_mappingAdharDetail.length];
		for (int i = 0; i < Portal_mappingAdharDetail.length; i++) {
			srtAdhar[i] = ifr.getValue(Portal_mappingAdharDetail[i]).toString();
		}
		for (int i = 0; i < sttingmappingAdhar.length; i++) {
			jsonObject.put(sttingmappingAdhar[i], srtAdhar[i]);
		}
		jsonArray.add(jsonObject);
		Log.consoleLog(ifr, "jsonArray::::::" + jsonArray);
		((IFormAPIHandler) ifr).setTableCellValue("QNL_BASIC_INFO", 0, "CNL_KYC2", jsonArray, 1);
		jsonObject.put("saveWorkitem", "true");
		return jsonObject.toString();
	}

	public void mACCLoadLoanDetails(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCLoadLoanDetails");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTloandetail").replaceAll("#PID#", PID);
		Log.consoleLog(ifr, "navigationNextLoadLoanDetails query " + IndexQuery);
		List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
		Log.consoleLog(ifr, "docIndexList query " + docIndexList);

		if (docIndexList.size() == 0) {
			final String winame = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
			Log.consoleLog(ifr, " winame : " + winame);
			final String query = ConfProperty.getQueryScript("PORTALLOANDETAILS").replaceAll("#winame#", winame);
			Log.consoleLog(ifr, " query : " + query);
			final List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(query);
			Log.consoleLog(ifr, " getDataList : " + getDataList);
			if (getDataList.size() > 0) {
				final String loanAmount = getDataList.get(0).get(0);
				Log.consoleLog(ifr, "IngetDataListside mACCLoadLoanDetails loanAmount : " + loanAmount);
				final String loanTenure = getDataList.get(0).get(1);
				Log.consoleLog(ifr, "Inside mACCLoadLoanDetails loanTenure : " + loanTenure);
				final String loanType = getDataList.get(0).get(2);
				Log.consoleLog(ifr, "Inside mACCLoadLoanDetails loanType : " + loanType);
				ifr.addItemInCombo("PortalLOS_C_LD_Product", loanType);
				// ifr.setValue("PortalLOS_T_H_LD_HiddenProduct", loanType);
				ifr.setValue("PortalLOS_T_LD_LoanAmount", loanAmount);
				ifr.setValue("PortalLOS_T_LD_Tenure", loanTenure);
			}
		} else {
			Log.consoleLog(ifr, "setValue getProductDataLoad :");
			JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_LOS_PROPOSED_FACILITY", true);
			Log.consoleLog(ifr, "setValue getProductDataLoad :" + dataFromGrid.toString());
			if (!dataFromGrid.isEmpty()) {
				JSONObject jsonObject = new JSONObject();
				// JSONArray personalDetails = new JSONArray();
				jsonObject = (JSONObject) dataFromGrid.get(0);
				Log.consoleLog(ifr, "inside if personalDetails condition jsonObject" + jsonObject.toString());

				if (!jsonObject.isEmpty()) {
					Log.consoleLog(ifr, "inside if personalDetails condition ");
					if (jsonObject != null && jsonObject.toString().trim() != "" && jsonObject.toString() != null) {
						Log.consoleLog(ifr, "productdet jsonobject :" + jsonObject.toString());
						if (checkNullObject(ifr, jsonObject.get("QNL_LOS_PROPOSED_FACILITY-Product"))
								|| checkNullObject(ifr, jsonObject.get("QNL_LOS_PROPOSED_FACILITY-SubProduct"))) {
							Log.consoleLog(ifr, "Product or SubProduct is not null");
							// disableFrame(ifr,
							// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
							ifr.setValue("PortalLOS_C_LD_Product", checkIfNullOrReturnSpaceObject(ifr,
									jsonObject.get("QNL_LOS_PROPOSED_FACILITY-Product"), "PortalLOS_C_LD_Product"));
							ifr.setValue("PortalLOS_C_LD_SubProduct",
									checkIfNullOrReturnSpaceObject(ifr,
											jsonObject.get("QNL_LOS_PROPOSED_FACILITY-SubProduct"),
											"PortalLOS_C_LD_SubProduct"));
							ifr.setValue("PortalLOS_C_LD_Purpose", checkIfNullOrReturnSpaceObject(ifr,
									jsonObject.get("QNL_LOS_PROPOSED_FACILITY-LoanPurpose"), "PortalLOS_C_LD_Purpose"));
							ifr.setValue("PortalLOS_C_LD_Variant", checkIfNullOrReturnSpaceObject(ifr,
									jsonObject.get("QNL_LOS_PROPOSED_FACILITY-Variant"), "PortalLOS_C_LD_Variant"));
							ifr.setValue("PortalLOS_T_LD_LoanAmount",
									checkIfNullOrReturnSpaceObject(ifr,
											jsonObject.get("QNL_LOS_PROPOSED_FACILITY-ReqLoanAmt"),
											"PortalLOS_T_LD_LoanAmount"));
							ifr.setValue("PortalLOS_T_LD_Tenure", checkIfNullOrReturnSpaceObject(ifr,
									jsonObject.get("QNL_LOS_PROPOSED_FACILITY-Tenure"), "PortalLOS_T_LD_Tenure"));

							Log.consoleLog(ifr,
									"setValue output PortalLOS_T_LD_Tenure :" + ifr.getValue("PortalLOS_T_LD_Tenure"));
						}
					}
				}
			}

		}
	}

	public void mACCLoadPreApproved(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCLoadPreApproved");
		final String winame = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, " winame : " + winame);
		final String query = ConfProperty.getQueryScript("PORTALLOANDETAILS").replaceAll("#winame#", winame);
		Log.consoleLog(ifr, " query : " + query);
		final List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(query);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		if (getDataList.size() > 0) {
			final String loanAmount = getDataList.get(0).get(0);
			Log.consoleLog(ifr, "IngetDataListside mACCLoadPreApproved loanAmount : " + loanAmount);
			final String loanTenure = getDataList.get(0).get(1);
			Log.consoleLog(ifr, "Inside mACCLoadPreApproved loanTenure : " + loanTenure);
			ifr.setValue("PortalLOS_T_PreApp_LoanAmt", loanAmount);
			ifr.setValue("PortalLOS_T_PreApp_Tenure", loanTenure);
		}
		String str = ifr.getValue("LOS_PORTAL_T_HIGHER_AMT").toString();
		if (str.length() > 0) {
			ifr.setStyle("LOS_PORTAL_T_HIGHER_AMT", "visible", "true");
		}
	}

	public String populateResidenceDetail(IFormReference ifr, String control, String event, String value) {
		JSONArray jsonArray = new JSONArray();
		// JSONArray chilGridArray=new JSONArray();
		JSONObject jsonObject = new JSONObject();
		// JSONObject childGridRowObject=new JSONObject();
		String Residence_Detail = ConfProperty.getCommonPropertyValue("Residence_Details");
		Log.consoleLog(ifr, "Residence_Detail" + Residence_Detail);
		String[] Portal_mappings = Residence_Detail.split(",");
		String Residence_Detail_CA = ConfProperty.getCommonPropertyValue("Residence_Details_CA");
		Log.consoleLog(ifr, "Residence_Detail_CA" + Residence_Detail_CA);
		String[] Portal_mappings_CA = Residence_Detail_CA.split(",");
		String ResBackoffmapp = ConfProperty.getCommonPropertyValue("Residence_Details_SetValue_Backoffice");
		Log.consoleLog(ifr, "ResBackoffmapp" + ResBackoffmapp);
		Log.consoleLog(ifr, ResBackoffmapp);
		String[] sttingmapping = ResBackoffmapp.split(",");
		String[] srt = new String[Portal_mappings.length];
		String[] srt_CA = new String[Portal_mappings_CA.length];
		for (int i = 0; i < Portal_mappings.length; i++) {

			srt[i] = ifr.getValue(Portal_mappings[i]).toString();

		}
		for (int i = 0; i < Portal_mappings_CA.length; i++) {

			Log.consoleLog(ifr, "Portal_mappings_CA[i]" + Portal_mappings_CA[i]);
			srt_CA[i] = ifr.getValue(Portal_mappings_CA[i]).toString();

		}
		for (int i = 0; i < sttingmapping.length; i++) {
			jsonObject.put(sttingmapping[i], srt[i]);
		}
		jsonArray.add(jsonObject);
		Log.consoleLog(ifr, "jsonArray------pa::::::" + jsonArray);
		jsonObject = new JSONObject();
		for (int i = 0; i < sttingmapping.length; i++) {
			jsonObject.put(sttingmapping[i], srt_CA[i]);

		}
		jsonArray.add(jsonObject);
		Log.consoleLog(ifr, "jsonArray-------ca::::::" + jsonArray);
		Log.consoleLog(ifr, "jsonArray::::::" + jsonArray);
		((IFormAPIHandler) ifr).setTableCellValue("QNL_BASIC_INFO", 0, "CNL_CUST_ADDRESS", jsonArray, 1);
		jsonObject.put("saveWorkitem", "true");
		return jsonObject.toString();
	}

	public String populateDisbursementDetail(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "populateFinalEligibilityDetail : ");
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "PID : " + PID);
		String Personal_Detail = ConfProperty.getCommonPropertyValue("Disbursement_Screen");
		Log.consoleLog(ifr, "Personal_Detail : " + Personal_Detail);
		String[] Portal_mappings = Personal_Detail.split(",");
		Log.consoleLog(ifr, "Portal_mappings" + Portal_mappings);
		String Backoffmapp = ConfProperty.getCommonPropertyValue("Disbursement_Screen_SetValue_BackOffice");
		Log.consoleLog(ifr, " Backoffmapp : " + Backoffmapp);
		String[] sttingmapping = Backoffmapp.split(",");
		Log.consoleLog(ifr, "sttingmapping : " + sttingmapping);
		String[] srt = new String[Portal_mappings.length];
		for (int i = 0; i < Portal_mappings.length; i++) {

			srt[i] = ifr.getValue(Portal_mappings[i]).toString();

		}
		Log.consoleLog(ifr, "srt : " + srt);
		for (int i = 0; i < sttingmapping.length; i++) {
			ifr.setValue(sttingmapping[i], srt[i]);

		}

		return "";
	}

	public String populateEmploymentDetail(IFormReference ifr, String control, String event, String value) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		String Employment_Detail = ConfProperty.getCommonPropertyValue("Employment_Details");
		Log.consoleLog(ifr, "Employment_Detail" + Employment_Detail);
		String[] Portal_mappings = Employment_Detail.split(",");
		String EmpBackoffmapp = ConfProperty.getCommonPropertyValue("Employment_Details_SetValue_Backoffice");
		Log.consoleLog(ifr, "ResBackoffmapp" + EmpBackoffmapp);
		Log.consoleLog(ifr, EmpBackoffmapp);
		String[] sttingmapping = EmpBackoffmapp.split(",");
		String[] srt = new String[Portal_mappings.length];
		for (int i = 0; i < Portal_mappings.length; i++) {

			srt[i] = ifr.getValue(Portal_mappings[i]).toString();

		}
		for (int i = 0; i < sttingmapping.length; i++) {
			jsonObject.put(sttingmapping[i], srt[i]);
		}
		jsonArray.add(jsonObject);
		Log.consoleLog(ifr, "jsonArray::::::" + jsonArray);
		((IFormAPIHandler) ifr).setTableCellValue("QNL_BASIC_INFO", 0, "CNL_OCCUPATION_INFO", jsonArray, 1);
		jsonObject.put("saveWorkitem", "true");
		return jsonObject.toString();

	}

	public String getPortalDataLoad(IFormReference ifr, String control, String event, String value) {

		Log.consoleLog(ifr, "setValue getPortalDataLoad :");
		IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
		HttpServletRequest req = iFormAPIHandler.getRequest();
		HttpSession session = req.getSession();
		String aadharNumber = (String) session.getAttribute("PortalLOS_T_AKYC_AadharNo");
		Log.consoleLog(ifr, "PortalLOS_T_AKYC_AadharNo aadharNumber" + aadharNumber);
		ifr.setValue("PortalLOS_T_PerDet_NationalID", aadharNumber);
		JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_Basic_Info", true);
		Log.consoleLog(ifr, "setValue getPortalDataLoad :" + dataFromGrid.toString());
		if (!dataFromGrid.isEmpty()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject = (JSONObject) dataFromGrid.get(0);
			Log.consoleLog(ifr, "inside if personalDetails condition jsonObject" + jsonObject.toString());

			if (!jsonObject.isEmpty()) {
				Log.consoleLog(ifr, "inside if personalDetails condition ");
				// Registered address
				// JSONObject perdet = new JSONObject();
				if (jsonObject != null && jsonObject.toString().trim() != "" && jsonObject.toString() != null) {
					// perdet = (JSONObject) personalDetails.get(0);
					Log.consoleLog(ifr, "perdet jsonobject :" + jsonObject.toString());
					if (checkNullObject(ifr, jsonObject.get("QNL_Basic_Info-FirstName"))
							|| checkNullObject(ifr, jsonObject.get("QNL_Basic_Info-MiddleName"))) {
						Log.consoleLog(ifr, "FirstName or MiddleName is not null");
						// disableFrame(ifr,
						// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
						ifr.setValue("PortalLOS_T_PerDet_Title", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Title"), "PortalLOS_T_PerDet_Title"));
						ifr.setValue("PortalLOS_T_PerDet_Fname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-FirstName"), "PortalLOS_T_PerDet_Fname"));
						ifr.setValue("PortalLOS_T_PerDet_Mname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MiddleName"), "PortalLOS_T_PerDet_Mname"));
						ifr.setValue("PortalLOS_T_PerDet_Lname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-LastName"), "PortalLOS_T_PerDet_Lname"));
						ifr.setValue("PortalLOS_C_PerDet_Gender", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Gender"), "PortalLOS_C_PerDet_Gender"));
						ifr.setValue("PortalLOS_C_PerDet_MaritalStatus", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MaritalStatus"), "PortalLOS_C_PerDet_MaritalStatus"));
						ifr.setValue("PortalLOS_C_PerDet_Dependents", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-NoOfDependents"), "PortalLOS_C_PerDet_Dependents"));
						ifr.setValue("PortalLOS_T_PerDet_FatherName", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-FatherName"), "PortalLOS_T_PerDet_FatherName"));
						ifr.setValue("PortalLOS_T_PerDet_MotherName", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MotherName"), "PortalLOS_T_PerDet_MotherName"));
						ifr.setValue("PortalLOS_C_PerDet_EducationQualification",
								checkIfNullOrReturnSpaceObject(ifr,
										jsonObject.get("QNL_Basic_Info-Education_Qualification"),
										"PortalLOS_C_PerDet_EducationQualification"));
						ifr.setValue("PortalLOS_T_PerDet_Nationality", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Nationality"), "PortalLOS_T_PerDet_Nationality"));
						// String str =checkIfNullOrReturnSpaceObject(ifr,
						// jsonObject.get("QNL_Basic_Info-EmailID"),
						// "PortalLOS_T_PerDet_EmailAddress").toString();
						// Log.consoleLog(ifr, "str .."+str);
						ifr.setValue("PortalLOS_T_PerDet_SpouseName", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-SpouseName"), "PortalLOS_T_PerDet_SpouseName"));
						ifr.setValue("PortalLOS_T_PerDet_EmailAddress", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-EmailID"), "PortalLOS_T_PerDet_EmailAddress"));
						Log.consoleLog(ifr, "setValue output :" + ifr.getValue("PortalLOS_T_PerDet_Gender"));
						Log.consoleLog(ifr, "setValue output PortalLOS_T_PerDet_EmailAddress :"
								+ ifr.getValue("PortalLOS_T_PerDet_EmailAddress"));

						String str = (ifr.getValue("PortalLOS_C_PerDet_MaritalStatus")).toString();
						Log.consoleLog(ifr, " str : " + str);
						if (str.equalsIgnoreCase("M")) {
							Log.consoleLog(ifr, "Indide Om if");
							ifr.setStyle("PortalLOS_T_PerDet_SpouseName", "visible", "true");
						} else {
							ifr.setStyle("PortalLOS_T_PerDet_SpouseName", "visible", "false");
						}
					}
				}
			}
		}
		return "";
	}

	public String getResidencePortalData(IFormReference ifr, String control, String event, String value) {
		JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_Basic_Info", true);
		Log.consoleLog(ifr, "setValue dataFromGrid :" + dataFromGrid.toString());
		if (!dataFromGrid.isEmpty()) {
			JSONObject jsonObject = new JSONObject();
			JSONArray addressDetails = new JSONArray();
			jsonObject = (JSONObject) dataFromGrid.get(0);
			addressDetails = (JSONArray) jsonObject.get("QNL_BASIC_INFO.CNL_CUST_ADDRESS");
			Log.consoleLog(ifr, "addressDetails array :" + addressDetails.toString());
			if (!addressDetails.isEmpty()) {
				Log.consoleLog(ifr, "inside if addressDetails condition ");
				// Registered address
				JSONObject regAdd = new JSONObject();
				JSONObject regAddpr = new JSONObject();
				if (regAdd != null && regAdd.toString().trim() != "" && regAdd.toString() != null) {
					regAdd = (JSONObject) addressDetails.get(0);
					regAddpr = (JSONObject) addressDetails.get(1);
					Log.consoleLog(ifr, "regAdd jsonobject :" + regAdd.toString());
					if (checkNullObject(ifr, regAdd.get("Line1")) || checkNullObject(ifr, regAdd.get("Line2"))) {
						Log.consoleLog(ifr, "Line1 or Line2 is not null");
						// disableFrame(ifr,
						// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
						ifr.setValue("PortalLOS_T_ResDet_PA_AD1",
								checkIfNullOrReturnSpaceObject(ifr, regAdd.get("Line1"), "PortalLOS_T_ResDet_PA_AD1"));
						ifr.setValue("PortalLOS_T_ResDet_PA_AD2",
								checkIfNullOrReturnSpaceObject(ifr, regAdd.get("Line2"), "PortalLOS_T_ResDet_PA_AD2"));
						ifr.setValue("PortalLOS_T_ResDet_PA_City", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("City_Town_Village"), "PortalLOS_T_ResDet_PA_City"));
						ifr.setValue("PortalLOS_T_ResDet_PA_State", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("State"), "PortalLOS_T_ResDet_PA_State"));
						ifr.setValue("PortalLOS_T_ResDet_PA_Country", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("Country"), "PortalLOS_T_ResDet_PA_Country"));
						ifr.setValue("PortalLOS_T_ResDet_PA_Pincode", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("PinCode"), "PortalLOS_T_ResDet_PA_Pincode"));
						ifr.setValue("PortalLOS_T_ResDet_CA_AD1", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Line1"), "PortalLOS_T_ResDet_CA_AD1"));
						ifr.setValue("PortalLOS_T_ResDet_CA_AD2", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Line2"), "PortalLOS_T_ResDet_CA_AD2"));
						ifr.setValue("PortalLOS_C_ResDet_CA_City", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("City_Town_Village"), "PortalLOS_C_ResDet_CA_City"));
						ifr.setValue("PortalLOS_C_ResDet_CA_State", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("State"), "PortalLOS_C_ResDet_CA_State"));
						ifr.setValue("PortalLOS_C_ResDet_CA_Country", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Country"), "PortalLOS_C_ResDet_CA_Country"));
						ifr.setValue("PortalLOS_T_ResDet_CA_Pincode", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("PinCode"), "PortalLOS_T_ResDet_CA_Pincode"));
					}
				}
			}
		}
		return "";
	}

	public String getEmploymentPortalData(IFormReference ifr, String control, String event, String value) {
		JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_Basic_Info", true);
		Log.consoleLog(ifr, "setValue Employment dataFromGrid :" + dataFromGrid.toString());
		if (!dataFromGrid.isEmpty()) {
			JSONObject jsonObject = new JSONObject();
			JSONArray EmploymentDetails = new JSONArray();
			jsonObject = (JSONObject) dataFromGrid.get(0);
			EmploymentDetails = (JSONArray) jsonObject.get("QNL_BASIC_INFO.CNL_OCCUPATION_INFO");
			Log.consoleLog(ifr, "addressDetails array :" + EmploymentDetails.toString());
			if (!EmploymentDetails.isEmpty()) {
				Log.consoleLog(ifr, "inside if EmploymentDetails condition ");
				// Registered address
				JSONObject Empdet = new JSONObject();
				if (Empdet != null && Empdet.toString().trim() != "" && Empdet.toString() != null) {
					Empdet = (JSONObject) EmploymentDetails.get(0);
					Log.consoleLog(ifr, "regAdd jsonobject :" + Empdet.toString());
					if (checkNullObject(ifr, Empdet.get("OccupationType"))) {
						Log.consoleLog(ifr, "OccupationType is not null");
						// disableFrame(ifr,
						// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
						ifr.setValue("PortalLOS_C_EmpDet_EmpType", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("OccupationType"), "PortalLOS_C_EmpDet_EmpType"));
						ifr.setValue("PortalLOS_C_EmpDet_EmpSubType", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("OccupationSubType"), "PortalLOS_C_EmpDet_EmpSubType"));
						ifr.setValue("PortalLOS_C_EmpDet_EmpName", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("EmployerName"), "PortalLOS_C_EmpDet_EmpName"));
						ifr.setValue("PortalLOS_T_EmpDet_TotalExp", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("TWE_InMonths"), "PortalLOS_T_EmpDet_TotalExp"));
						ifr.setValue("PortalLOS_T_EmpDet_ExpCurrEmployer", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("TWE_Months"), "PortalLOS_T_EmpDet_ExpCurrEmployer"));
						ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_OFFEmailID", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("EmailAddress"), "PortalLOS_T_EmpDet_EmpAdd_OFFEmailID"));
						ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_MonthlyInc", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("MonthlyIncome"), "PortalLOS_T_EmpDet_EmpAdd_MonthlyInc"));
						ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_MonthlyLiab", checkIfNullOrReturnSpaceObject(ifr,
								Empdet.get("MonthlyExpenses"), "PortalLOS_T_EmpDet_EmpAdd_MonthlyLiab"));
					}
				}
			}
		}

		String str = (ifr.getValue("PortalLOS_C_EmpDet_EmpType")).toString();
		Log.consoleLog(ifr, "str :" + str);
		if (str.equalsIgnoreCase("Salaried")) {
			Log.consoleLog(ifr, "Indide Om if");
			ifr.applyGroup("EMP_TYPE_SALARIED");
		} else if (str.equalsIgnoreCase("Self-Employed/Professional")) {
			ifr.applyGroup("EMP_TYPE_SELFEMPLOYEE_P");
		} else if (str.equalsIgnoreCase("Self-Employed/Non-Professional")) {
			ifr.applyGroup("EMP_TYPE_SELFEMPLOYEE_NP");
		} else if (str.equalsIgnoreCase("Agriculture")) {
			ifr.applyGroup("EMP_TYPE_AGRI");
		} else if (str.equalsIgnoreCase("Retired")) {
			ifr.applyGroup("EMP_TYPE_RETIRED");
		}

		String loanType = getLoanType(ifr, control, event, value);
		Log.consoleLog(ifr, "loanType : " + loanType);
		if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
			Log.consoleLog(ifr, "loanType : " + loanType);
			ifr.applyGroup("IncomeDetails_H");
		} else {
			ifr.applyGroup("IncomeDetails_S");
			Log.consoleLog(ifr, "loanType :" + loanType);

		}

		return "";
	}

	// added by Anish on 20-09-2023
	public String mACCConsentDisclosures(IFormReference ifr, String Control, String Event, String Value) {
		Log.consoleLog(ifr, " Inside mACCConsentDisclosures ");
		JSONObject message = new JSONObject();

		String agriESign = "";
		try {
			String borrower = "RAM Mohan";
			String currentAddress = "Noida";
			String amountFinanced = "100000";
			String processingFees = "50000";
			String tenure = "24";
			String emi = "10000";
			String interestRate = "10.8";
			String signType = Value;

			agriESign = ConfProperty.getCommonPropertyValue("AGRIOPEN").replaceAll("#borrower#", borrower)
					.replaceAll("#currentAddress#", currentAddress).replaceAll("#amountFinanced#", amountFinanced)
					.replaceAll("#processingFees#", processingFees).replaceAll("#tenure#", tenure)
					.replaceAll("#emi#", emi).replaceAll("#interestRate#", interestRate)
					.replaceAll("#signType#", signType);
			Log.consoleLog(ifr, " agriESign : " + agriESign);
			message.put("url", agriESign);
		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in mACCConsentDisclosures" + e);
		}
		return message.toString();
	}

	// on 21-09-2023
	public String setLoanDetailsOnEMI(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside setLoanDetailsOnEMI");
		String loanAmount = "";
		String loanTenure = "";
		JSONObject jsonObject = new JSONObject();
		String winame = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, " winame : " + winame);
		String query = ConfProperty.getQueryScript("PORTALLOANDETAILS").replaceAll("#winame#", winame);
		Log.consoleLog(ifr, " query : " + query);
		List<List<String>> getDataList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		if (getDataList.size() > 0) {
			IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
			HttpServletRequest req = iFormAPIHandler.getRequest();
			HttpSession session = req.getSession();
			String s1 = (String) session.getAttribute("LOS_PORTAL_T_HIGHER_AMT");
			Log.consoleLog(ifr, "value of need amount : " + s1);
			if (!("".equals(s1)) && s1 != null) {
				loanAmount = s1;
			} else {
				loanAmount = ((List<String>) getDataList.get(0)).get(0);
				Log.consoleLog(ifr, "IngetDataListside setLoanDetailsOnEMI loanAmount : " + loanAmount);
			}
			loanTenure = ((List<String>) getDataList.get(0)).get(1);
			Log.consoleLog(ifr, "Inside setLoanDetailsOnEMI loanTenure : " + loanTenure);
			value = loanAmount.concat(",").concat(loanTenure);
			Log.consoleLog(ifr, " value : " + value);
			jsonObject.put("loanAmount", loanAmount);
			jsonObject.put("loanTenure", loanTenure);
		}
		loanCalculator(ifr, control, event, value);
		return jsonObject.toJSONString();
	}

	// on 04-10-2023
	public void loanCalculator(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, " Inside loanCalculator ");
		String principalStr = "";
		String tenureInMonthsStr = "";
		Log.consoleLog(ifr, " value : " + value);
		String[] parts = value.split(",");
		String len = String.valueOf(parts.length);
		if (len.equalsIgnoreCase("2")) {
			principalStr = parts[0];
			tenureInMonthsStr = parts[1];
			Log.consoleLog(ifr, "tenureinmonths : " + tenureInMonthsStr);
		}
		String loanType = getLoanType(ifr, control, event, value);
		try {
			String annualInterestRateStr = "";
			if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
				annualInterestRateStr = ifr.getValue("P_AOD_ELIGIBLE_ROI").toString();
				Log.consoleLog(ifr, "annualInterestRateStr : " + annualInterestRateStr);
			} else {
				annualInterestRateStr = "10.8";
			}
			Log.consoleLog(ifr, " principalStr : " + principalStr);
			Log.consoleLog(ifr, " annualInterestRateStr : " + annualInterestRateStr);
			IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
			HttpServletRequest req = iFormAPIHandler.getRequest();
			HttpSession session = req.getSession();
			String s1 = (String) session.getAttribute("LOS_PORTAL_T_HIGHER_AMT");
			Log.consoleLog(ifr, "value of need amount : " + s1);
			if ("".equals(s1) || s1 == null) {
				Log.consoleLog(ifr, "qinside if of length : ");
				String[] emiDetails = getCalulatedLoanDetails(ifr, principalStr, tenureInMonthsStr,
						annualInterestRateStr);
				String totalAmountPayable = emiDetails[0];
				String emi = emiDetails[1];
				String interestAmount = emiDetails[2];
				Log.consoleLog(ifr, "Principal Amount: " + totalAmountPayable);
				Log.consoleLog(ifr, "EMI (Equated Monthly Installment): " + emi);
				Log.consoleLog(ifr, "Interest Amount: " + interestAmount);
				if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
					Log.consoleLog(ifr, "In side if loanType");
					ifr.setValue("P_AOD_MONTHLY_EMI", emi);
					ifr.setValue("P_AOD_TOTAL_INTEREST", interestAmount);
					ifr.setValue("P_AOD_PAYABLE_AMOUNT", totalAmountPayable);
					ifr.setValue("P_AOD_PROCESS_FEE", "10000");
					ifr.setValue("P_AOD_REQ_LIMIT_AMOUNT", principalStr);
					ifr.setValue("P_AOD_REQ_LIMIT_TENURE", tenureInMonthsStr);
				} else {
					ifr.setValue("PortalLOS_T_EMICal_EMI", emi);
					ifr.setValue("PortalLOS_T_EMICal_TOTInt", interestAmount);
					ifr.setValue("PortalLOS_T_EMICal_PAYAMT", totalAmountPayable);
					ifr.setValue("PortalLOS_T_EMICal_PROFEES", "10000");
				}
			} else {
				Log.consoleLog(ifr, "inside else  : ");
				principalStr = s1;
				Log.consoleLog(ifr, "changed principal amt : " + principalStr);
				String[] emiDetails = getCalulatedLoanDetails(ifr, principalStr, tenureInMonthsStr,
						annualInterestRateStr);
				String totalAmountPayable = emiDetails[0];
				String emi = emiDetails[1];
				String interestAmount = emiDetails[2];
				Log.consoleLog(ifr, "Principal Amount: " + totalAmountPayable);
				Log.consoleLog(ifr, "EMI (Equated Monthly Installment): " + emi);
				Log.consoleLog(ifr, "Interest Amount: " + interestAmount);
				if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
					Log.consoleLog(ifr, "In side if loanType");
					ifr.setValue("P_AOD_MONTHLY_EMI", emi);
					ifr.setValue("P_AOD_TOTAL_INTEREST", interestAmount);
					ifr.setValue("P_AOD_PAYABLE_AMOUNT", totalAmountPayable);
					ifr.setValue("P_AOD_PROCESS_FEE", "10000");
					ifr.setValue("P_AOD_REQ_LIMIT_AMOUNT", principalStr);
					ifr.setValue("P_AOD_REQ_LIMIT_TENURE", tenureInMonthsStr);
				} else {
					ifr.setValue("PortalLOS_T_EMICal_EMI", emi);
					ifr.setValue("PortalLOS_T_EMICal_TOTInt", interestAmount);
					ifr.setValue("PortalLOS_T_EMICal_PAYAMT", totalAmountPayable);
					ifr.setValue("PortalLOS_T_EMICal_PROFEES", "10000");
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input. Please enter valid numeric values.");
		}
	}

	public String[] getCalulatedLoanDetails(IFormReference ifr, String amount, String tenure, String roi) {
		Log.consoleLog(ifr, "Inside getCalulatedLoanDetails");
		String[] emidetails = new String[3];
		try {
			double principal = Double.parseDouble(amount);
			Log.consoleLog(ifr, " principal : " + principal);
			double annualInterestRate = Double.parseDouble(roi);
			Log.consoleLog(ifr, " annualInterestRate : " + annualInterestRate);
			double tenureInMonths = Double.parseDouble(tenure);
			Log.consoleLog(ifr, " tenureInMonths : " + tenureInMonths);

			// Convert annual interest rate to monthly interest rate
			double monthlyInterestRate = (annualInterestRate / 12) / 100;
			Log.consoleLog(ifr, " monthlyInterestRate : " + monthlyInterestRate);

			// Calculate EMI
			double emi = (principal * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenureInMonths))
					/ (Math.pow(1 + monthlyInterestRate, tenureInMonths) - 1);
			Log.consoleLog(ifr, " emi : " + emi);
			// Calculate Total Amount Payable
			double totalAmountPayable = emi * tenureInMonths;
			// Calculate Interest Amount
			double interestAmount = totalAmountPayable - principal;
			DecimalFormat df = new DecimalFormat("#.##");
			return new String[] { df.format(totalAmountPayable), df.format(emi), df.format(interestAmount) };
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getCalulatedLoanDetails : " + e);
		}
		return emidetails;
	}

	// summary field-
	public void mSummaryFieldDetails(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mSummaryFieldDetails");
		String winame = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, " winame : " + winame);
		String loanType = getLoanType(ifr, control, event, value);

		String query = ConfProperty.getQueryScript("PORTALLOANDETAILS").replaceAll("#winame#", winame);
		Log.consoleLog(ifr, " query : " + query);
		List<List<String>> getDataList = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		if (getDataList.size() > 0) {

			String loanAmount = getDataList.get(0).get(0);
			Log.consoleLog(ifr, " mSummaryFieldDetails loanAmount : " + loanAmount);
			String loanTenure = getDataList.get(0).get(1);
			Log.consoleLog(ifr, "Inside mSummaryFieldDetails loanTenure : " + loanTenure);
			String eMI = "14020";
			String interestRate = "8.5";
			if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
				ifr.setValue("LD_sec3_tb1", "90000");
				ifr.setValue("LD_sec3_tb2", eMI);
				ifr.setValue("LD_sec3_tb3", "36");
				ifr.setValue("LD_sec3_tb4", interestRate);
			} else {
				ifr.setValue("LD_sec3_tb1", loanAmount);
				ifr.setValue("LD_sec3_tb2", eMI);
				ifr.setValue("LD_sec3_tb3", loanTenure);
				ifr.setValue("LD_sec3_tb4", interestRate);
			}
		}

	}

	// code ends
	public String POSTRequest(IFormReference ifr, String control, String event, String value) throws IOException {

		String PANNo = ifr.getValue("PortalLOS_T_EmpDet_EmpAdd_TaxID").toString();
		Log.consoleLog(ifr, "inside POSTRequest" + PANNo);
		JSONObject req = new JSONObject();
		req.put("pan", PANNo);
		req.put("consent", "Y");
		final String POST_PARAMS = req.toString();

		String ResponseValue = null;
		// String CustIPValue = value;
		Log.consoleLog(ifr, "inside MyGETRequest");
		Log.consoleLog(ifr, "Exception in loadGenerateDocs");

		Log.consoleLog(ifr, "" + POST_PARAMS);
		Log.consoleLog(ifr, "POST_PARAMS" + POST_PARAMS);
		URL obj = new URL("https://testapi.karza.in/v2/pan");
		String key = "JVQIRCvt5TjILQG5";
		Log.consoleLog(ifr, "key : " + key);

		HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
		postConnection.setRequestMethod("POST");
		postConnection.setRequestProperty("x-karza-key", key);
		postConnection.setRequestProperty("Content-Type", "application/json");

		postConnection.setDoOutput(true);
		OutputStream os = postConnection.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();

		int responseCode = postConnection.getResponseCode();
		Log.consoleLog(ifr, "POST Response Code : " + responseCode);
		Log.consoleLog(ifr, "POST Response Message : " + postConnection.getResponseMessage());
		Log.consoleLog(ifr, "After HTTP Connection in POST");
		Log.consoleLog(ifr, "POST Response Code" + responseCode);
		Log.consoleLog(ifr, "POST Response Message " + postConnection.getResponseMessage());
		JSONObject message = new JSONObject();

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			Log.consoleLog(ifr, "" + response.toString());
			Log.consoleLog(ifr, "After HTTP Connection" + response.toString());
			ResponseValue = response.toString();
			Log.consoleLog(ifr, "ResponseValue" + ResponseValue);
			message.put("showMessage", cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_VerTaxID", "error",
					postConnection.getResponseMessage()));

		} else {

			message.put("showMessage", cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_VerTaxID", "error",
					postConnection.getResponseMessage()));
			Log.consoleLog(ifr, "POST NOT WORKED");
			Log.consoleLog(ifr, "POST NOT WORKED");
			return message.toString();
		}

		return ResponseValue;
	}

	public boolean checkNullObject(IFormReference ifr, Object object) {
		Log.consoleLog(ifr, "Inside checkNullObject function");
		try {
			if (object != null && object.toString() != null && !"".equals(object.toString().trim())
					&& object.toString().trim().length() != 0) {
				return true;
			}
		} catch (Exception e) {
			Log.errorLog(ifr, "Exception in checkNullObject function : ");
			Log.consoleLog(ifr, "Exception in checkNullObject function : ");
		}
		return false;
	}

	public String checkIfNullOrReturnSpaceObject(IFormReference ifr, Object object, String fieldID) {
		Log.consoleLog(ifr, "Inside checkIfNullOrReturnSpace function");
		try {
			if (object != null && object.toString() != null && !"".equals(object.toString().trim())
					&& object.toString().trim().length() != 0) {
				// ifr.setStyle(fieldID,"mandatory","true");
				return object.toString();
			}
		} catch (Exception e) {
			Log.errorLog(ifr, "Exception in checkIfNullOrReturnSpace function : ");
			Log.consoleLog(ifr, "Exception in checkIfNullOrReturnSpace function : ");
		}
		ifr.setStyle(fieldID, "mandatory", "false");
		return "";
	}

	public void mAccOnloadExistingDepositDet(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside the mAccOnloadExistingDepositDet........");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "PID ..." + PID);
		String query = ConfProperty.getQueryScript("PORTALEXISTINGDEPODET").replaceAll("#PID#", PID);
		List<List<String>> data1 = cf.mExecuteQuery(ifr, query, "Query for fetching exposure type");
		Log.consoleLog(ifr, query + "query for  cif details...");
		String mobileNumber = data1.get(0).get(0);
		String cifNumber = data1.get(0).get(1);
		String exposureType = data1.get(0).get(2);
		String status = data1.get(0).get(3);

		String query1 = ConfProperty.getQueryScript("PORTALEXISTINGDETAILSPOPULATE")
				.replaceAll("#mobileNumber#", mobileNumber).replaceAll("#cifNumber#", cifNumber)
				.replaceAll("#exposureType#", exposureType).replaceAll("#status#", status);
		List<List<String>> existingDepositDet = cf.mExecuteQuery(ifr, query1,
				"Query for fetching Existing Deposit Details....");
		Log.consoleLog(ifr, query1 + "query for existing Deposit details...");
		String depositType = existingDepositDet.get(0).get(0);
		String depositnumber = existingDepositDet.get(0).get(1);
		String depositamount = existingDepositDet.get(0).get(2);
		String maturityDate = existingDepositDet.get(0).get(3);
		String depositRate = existingDepositDet.get(0).get(4);
		String depositMode = existingDepositDet.get(0).get(5);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_TYPE", depositType);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_NUM", depositnumber);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_AMOUNT", depositamount);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_MATURITY_DATE", maturityDate);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_RATE", depositRate);
		ifr.setValue("PORTAL_T_ED_DEPOSIT_MODE", depositMode);
		ifr.setStyle("navigationNextBtn", "disable", "true");
	}

	public String mAccOnloadAppliedOdDetails(IFormReference ifr, String control, String event, String value) {
		JSONObject jsonObject = new JSONObject();
		Log.consoleLog(ifr, "inside the mAccOnloadAppliedOdDetails........");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "PID ..." + PID);
		String query = ConfProperty.getQueryScript("PORTALEXISTINGDEPODET").replaceAll("#PID#", PID);
		List<List<String>> data1 = cf.mExecuteQuery(ifr, query, "Query for fetching exposure type");
		Log.consoleLog(ifr, query + "query for  cif details...");
		String mobileNumber = data1.get(0).get(0);
		String cifNumber = data1.get(0).get(1);
		String exposureType = data1.get(0).get(2);
		String status = data1.get(0).get(3);

		String query1 = ConfProperty.getQueryScript("PORTALEXISTINGDETAILSPOPULATE")
				.replaceAll("#mobileNumber#", mobileNumber).replaceAll("#cifNumber#", cifNumber)
				.replaceAll("#exposureType#", exposureType).replaceAll("#status#", status);
		List<List<String>> existingDepositDet = cf.mExecuteQuery(ifr, query1,
				"Query for fetching Existing Deposit Details....");
		Log.consoleLog(ifr, query1 + "query for existing Deposit details...");
		String depositType = existingDepositDet.get(0).get(0);
		String depositnumber = existingDepositDet.get(0).get(1);
		String depositamount = existingDepositDet.get(0).get(2);
		String maturityDate = existingDepositDet.get(0).get(3);
		String depositRate = existingDepositDet.get(0).get(4);
		String depositMode = existingDepositDet.get(0).get(5);

		float eligibleLimAmt = Float.parseFloat(depositamount);
		String EligibleLimitAmount = Float.toString((90 * eligibleLimAmt) / 100);
		String LimitDate = cf.getCurrentDate(ifr);
		Log.consoleLog(ifr, "EligibleLimitAmount...." + EligibleLimitAmount);

		String[] mDate = maturityDate.split("/");
		int date = Integer.parseInt(mDate[0]);
		int month = Integer.parseInt(mDate[1]);
		int year = Integer.parseInt(mDate[2]);

		String[] LDate = LimitDate.split("/");
		int date1 = Integer.parseInt(LDate[0]);
		int month1 = Integer.parseInt(LDate[1]);
		int year1 = Integer.parseInt(LDate[2]);

		LocalDate first_date = LocalDate.of(year, month, date);
		LocalDate second_date = LocalDate.of(year1, month1, date1);
		Period difference = Period.between(second_date, first_date);
		Log.consoleLog(ifr, "difference...." + difference);
		int monthDiff = difference.getMonths();
		int yearDiff = (difference.getYears());
		int tenure = monthDiff + yearDiff * 12;
		String Tenure = Integer.toString(tenure);
		Log.consoleLog(ifr, "Tenure..." + Tenure);
		float ROI = Float.parseFloat(depositRate) + 2;
		Log.consoleLog(ifr, "ROI..." + ROI);
		String roi = Float.toString(ROI);
		Log.consoleLog(ifr, "roi..." + roi);

		ifr.setValue("P_AOD_ELIGIBLE_L_AMOUNT", EligibleLimitAmount);
		ifr.setValue("P_AOD_ELIGIBLE_L_DATE", LimitDate);
		ifr.setValue("P_AOD_ELIGIBLE_L_EXPIRY_DATE", maturityDate);
		ifr.setValue("P_AOD_ELIGIBLE_TENURE", Tenure);
		ifr.setValue("P_AOD_ELIGIBLE_ROI", roi);
		Log.consoleLog(ifr, "testing setValue AOD....");
		jsonObject.put("EligibleLimitAmount", EligibleLimitAmount);
		jsonObject.put("Tenure", Tenure);
		return jsonObject.toJSONString();
	}

	public void mAccOnloadPersonalDetailsPopulate(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside the mAccOnloadPersonalDetailsPopulate  ....");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, "PID : " + PID);
		String query = ConfProperty.getQueryScript("PORTALPERSONALDETQuery").replaceAll("#WINAME#", PID);
		Log.consoleLog(ifr, " query : " + query);
		List<List<String>> dataPersonalDet = cf.mExecuteQuery(ifr, query, "query for loan type..");
		Log.consoleLog(ifr, " dataPersonalDet : " + dataPersonalDet);
		String loanType = dataPersonalDet.get(0).get(0);
		String mobileNumber = dataPersonalDet.get(0).get(1);

		String query1 = ConfProperty.getQueryScript("PORTALPERSONALDETPOPULATEQUERY").replaceAll("#mobileNumber#",
				mobileNumber);
		List<List<String>> result = cf.mExecuteQuery(ifr, query1, "Query for fetching  Personal Details.");

		if (result.size() > 0) {

			String Title = result.get(0).get(0);
			String CUST_FNAME = result.get(0).get(1);
			String CUST_MNAME = result.get(0).get(2);
			String CUST_LNAME = result.get(0).get(3);
			String GENDER = result.get(0).get(4);
			String DOB = result.get(0).get(5);
			String NATIONAL_ID = result.get(0).get(6);
			String MARITALSTATUS = result.get(0).get(7);
			String FATHERNAME = result.get(0).get(8);
			String MOTHERNAME = result.get(0).get(9);
			String EDUCATIONALQUALIFICATION = result.get(0).get(10);
			String NATIONALITY = result.get(0).get(11);
			String E_MAIL = result.get(0).get(12);
			String CustType = result.get(0).get(13);
			if (CustType.equalsIgnoreCase("ETB_PA")) {
				ifr.setStyle("PortalLOS_T_PerDet_Fname", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_Mname", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_Lname", "disable", "true");
				ifr.setStyle("PortalLOS_C_PerDet_Gender", "disable", "true");
				ifr.setStyle("PortalLOS_DatePick_PerDet_Dob", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_NationalID", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_FatherName", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_MotherName", "disable", "true");
				ifr.setStyle("PortalLOS_T_PerDet_Nationality", "disable", "true");
				ifr.setValue("PortalLOS_T_PerDet_Title", Title);
				ifr.setValue("PortalLOS_T_PerDet_Fname", CUST_FNAME);
				ifr.setValue("PortalLOS_T_PerDet_Mname", CUST_MNAME);
				ifr.setValue("PortalLOS_T_PerDet_Lname", CUST_LNAME);
				ifr.setValue("PortalLOS_C_PerDet_Gender", GENDER);
				ifr.setValue("PortalLOS_DatePick_PerDet_Dob", DOB);
				ifr.setValue("PortalLOS_T_PerDet_NationalID", NATIONAL_ID);
				ifr.setValue("PortalLOS_C_PerDet_MaritalStatus", MARITALSTATUS);
				ifr.setValue("PortalLOS_T_PerDet_FatherName", FATHERNAME);
				ifr.setValue("PortalLOS_T_PerDet_MotherName", MOTHERNAME);
				ifr.setValue("PortalLOS_C_PerDet_EducationQualification", EDUCATIONALQUALIFICATION);
				ifr.setValue("PortalLOS_T_PerDet_Nationality", NATIONALITY);
				ifr.setValue("PortalLOS_T_PerDet_EmailAddress", E_MAIL);
				if (MARITALSTATUS.equalsIgnoreCase("M")) {
					ifr.setStyle("PortalLOS_T_PerDet_SpouseName", "visible", "true");
				}
			}
		}
	}

	public String getLoanType(IFormReference ifr, String control, String event, String value) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, "PID : " + PID);
		String query = ConfProperty.getQueryScript("PORTALLOANTYPEQuery").replaceAll("#WINAME#", PID);
		Log.consoleLog(ifr, " query : " + query);
		List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(query);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		String loanType = "";
		if (getDataList.size() > 0) {
			loanType = getDataList.get(0).get(0);
			Log.consoleLog(ifr, "loanType : " + loanType);
		}
		return loanType;
	}

	public void mAccOnloadResidenceDetailsPopulate(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside the mAccOnloadPersonalDetailsPopulate  ....");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
		Log.consoleLog(ifr, "PID : " + PID);
		String query = ConfProperty.getQueryScript("PORTALPERSONALDETQuery").replaceAll("#WINAME#", PID);
		Log.consoleLog(ifr, " query : " + query);
		List<List<String>> dataPersonalDet = cf.mExecuteQuery(ifr, query, "query for loan type..");
		Log.consoleLog(ifr, " dataPersonalDet : " + dataPersonalDet);
		String loanType = dataPersonalDet.get(0).get(0);
		String mobileNumber = dataPersonalDet.get(0).get(1);

		String query1 = ConfProperty.getQueryScript("PORTALRESIDENCEDETPOP").replaceAll("#mobileNumber#", mobileNumber);
		List<List<String>> result = cf.mExecuteQuery(ifr, query1, "Query for fetching  Personal Details.");

		if (result.size() > 0) {

			String addLine1 = result.get(0).get(0);
			Log.consoleLog(ifr, "addLine1..." + addLine1);

			String addLine2 = result.get(0).get(1);
			Log.consoleLog(ifr, "addLine2..." + addLine2);

			String City = result.get(0).get(2);
			Log.consoleLog(ifr, "City..." + City);

			String state = result.get(0).get(3);
			Log.consoleLog(ifr, "state..." + state);

			String country = result.get(0).get(4);
			Log.consoleLog(ifr, "country..." + country);

			String pincode = result.get(0).get(5);
			Log.consoleLog(ifr, "pincode..." + pincode);

			String CustFlag = result.get(0).get(6);
			Log.consoleLog(ifr, "CustFlag..." + CustFlag);

			if (CustFlag.equalsIgnoreCase("ETB_PA")) {
				Log.consoleLog(ifr, "Inside If...");
				ifr.setValue("PortalLOS_T_ResDet_PA_AD1", addLine1);
				ifr.setValue("PortalLOS_T_ResDet_PA_AD2", addLine2);
				ifr.setValue("PortalLOS_T_ResDet_PA_City", City);
				ifr.setValue("PortalLOS_T_ResDet_PA_State", state);
				ifr.setValue("PortalLOS_T_ResDet_PA_Country", country);
				ifr.setValue("PortalLOS_T_ResDet_PA_Pincode", pincode);

			}
		}
	}

	public String getPortalPreviewPersonanalDataLoad(IFormReference ifr, String control, String event, String value) {

		Log.consoleLog(ifr, "setValue getPortalPreviewPersonanalDataLoad :");
		JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_Basic_Info", true);
		Log.consoleLog(ifr, "setValue getPortalPreviewPersonanalDataLoad :" + dataFromGrid.toString());
		if (!dataFromGrid.isEmpty()) {
			JSONObject jsonObject = new JSONObject();
			// JSONArray personalDetails = new JSONArray();
			jsonObject = (JSONObject) dataFromGrid.get(0);
			Log.consoleLog(ifr, "inside if personalDetails condition jsonObject" + jsonObject.toString());
			// personalDetails = (JSONArray)
			// jsonObject.get("QNL_BASIC_INFO.CL_BASIC_INFO_I");
			// Log.consoleLog(ifr, "personalDetails array :" + personalDetails.toString());
			if (!jsonObject.isEmpty()) {
				Log.consoleLog(ifr, "inside if personalDetails condition ");
				// Registered address
				// JSONObject perdet = new JSONObject();
				if (jsonObject != null && jsonObject.toString().trim() != "" && jsonObject.toString() != null) {
					// perdet = (JSONObject) personalDetails.get(0);
					Log.consoleLog(ifr, "perdet jsonobject :" + jsonObject.toString());
					if (checkNullObject(ifr, jsonObject.get("QNL_Basic_Info-FirstName"))
							|| checkNullObject(ifr, jsonObject.get("QNL_Basic_Info-MiddleName"))) {
						Log.consoleLog(ifr, "FirstName or MiddleName is not null");
						// disableFrame(ifr,
						// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
						ifr.setValue("PortalLOS_OD_T_PerDet_Title", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Title"), "PortalLOS_OD_T_PerDet_Title"));
						ifr.setValue("PortalLOS_OD_T_PerDet_Fname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-FirstName"), "PortalLOS_OD_T_PerDet_Fname"));
						ifr.setValue("PortalLOS_OD_T_PerDet_Mname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MiddleName"), "PortalLOS_OD_T_PerDet_Mname"));
						ifr.setValue("PortalLOS_OD_T_PerDet_Lname", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-LastName"), "PortalLOS_OD_T_PerDet_Lname"));
						ifr.setValue("PortalLOS_OD_C_PerDet_Gender", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Gender"), "PortalLOS_OD_C_PerDet_Gender"));
						ifr.setValue("PortalLOS_OD_C_PerDet_MaritalStatus", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MaritalStatus"), "PortalLOS_OD_C_PerDet_MaritalStatus"));
						ifr.setValue("PortalLOS_OD_C_PerDet_Dependents", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-NoOfDependents"), "PortalLOS_OD_C_PerDet_Dependents"));
						ifr.setValue("PortalLOS_OD_T_PerDet_FatherName", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-FatherName"), "PortalLOS_OD_T_PerDet_FatherName"));
						ifr.setValue("PortalLOS_OD_T_PerDet_MotherName", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-MotherName"), "PortalLOS_OD_T_PerDet_MotherName"));
						ifr.setValue("PortalLOS_OD_C_PerDet_EducationQualification",
								checkIfNullOrReturnSpaceObject(ifr,
										jsonObject.get("QNL_Basic_Info-Education_Qualification"),
										"PortalLOS_OD_C_PerDet_EducationQualification"));
						ifr.setValue("PortalLOS_OD_T_PerDet_Nationality", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-Nationality"), "PortalLOS_OD_T_PerDet_Nationality"));
						ifr.setValue("PortalLOS_OD_T_PerDet_EmailAddress", checkIfNullOrReturnSpaceObject(ifr,
								jsonObject.get("QNL_Basic_Info-EmailID"), "PortalLOS_OD_T_PerDet_EmailAddress"));
						Log.consoleLog(ifr, "setValue output :" + ifr.getValue("PortalLOS_OD_C_PerDet_Gender"));
						Log.consoleLog(ifr, "setValue output PortalLOS_T_PerDet_EmailAddress :"
								+ ifr.getValue("PortalLOS_OD_T_PerDet_EmailAddress"));

						/////////////////
						String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
						Log.consoleLog(ifr, "PID : " + PID);
						String query = ConfProperty.getQueryScript("PORTALPERSONALDETQuery").replaceAll("#WINAME#",
								PID);
						Log.consoleLog(ifr, " query : " + query);
						List<List<String>> dataPersonalDet = cf.mExecuteQuery(ifr, query, "query for loan type..");
						Log.consoleLog(ifr, " dataPersonalDet : " + dataPersonalDet);
						String loanType = dataPersonalDet.get(0).get(0);
						String mobileNumber = dataPersonalDet.get(0).get(1);
						String query1 = ConfProperty.getQueryScript("PORTALPERSONALDETPOPULATEQUERY")
								.replaceAll("#mobileNumber#", mobileNumber);
						List<List<String>> result = cf.mExecuteQuery(ifr, query1,
								"Query for fetching  Personal Details.");
						String DOB = "";
						String NATIONAL_ID = "";
						String E_MAIL = "";
						if (result.size() > 0) {
							DOB = result.get(0).get(5);
							Log.consoleLog(ifr, "DOB..." + DOB);
							NATIONAL_ID = result.get(0).get(6);
							Log.consoleLog(ifr, "NATIONAL_ID..." + NATIONAL_ID);
							E_MAIL = result.get(0).get(12);
							Log.consoleLog(ifr, "E_MAIL..." + E_MAIL);
						}
						ifr.setValue("PortalLOS_OD_DatePick_PerDet_Dob", DOB);
						Log.consoleLog(ifr, "PortalLOS_OD_DatePick_PerDet_Dob..." + DOB);
						ifr.setValue("PortalLOS_OD_T_PerDet_NationalID", NATIONAL_ID);
						Log.consoleLog(ifr, "PortalLOS_OD_T_PerDet_NationalID..." + NATIONAL_ID);
						ifr.setValue("PortalLOS_OD_T_PerDet_EmailAddress", E_MAIL);
						Log.consoleLog(ifr, "PortalLOS_OD_T_PerDet_EmailAddress..." + E_MAIL);
						////////////////

					}
				}
			}
		}
		return "";
	}

	public String getPreviewResidenceData(IFormReference ifr, String control, String event, String value) {
		JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_Basic_Info", true);
		Log.consoleLog(ifr, "setValue dataFromGrid :" + dataFromGrid.toString());
		if (!dataFromGrid.isEmpty()) {
			JSONObject jsonObject = new JSONObject();
			JSONArray addressDetails = new JSONArray();
			jsonObject = (JSONObject) dataFromGrid.get(0);
			addressDetails = (JSONArray) jsonObject.get("QNL_BASIC_INFO.CNL_CUST_ADDRESS");
			Log.consoleLog(ifr, "addressDetails array :" + addressDetails.toString());
			if (!addressDetails.isEmpty()) {
				Log.consoleLog(ifr, "inside if addressDetails condition ");
				// Registered address
				JSONObject regAdd = new JSONObject();
				JSONObject regAddpr = new JSONObject();
				if (regAdd != null && regAdd.toString().trim() != "" && regAdd.toString() != null) {
					regAdd = (JSONObject) addressDetails.get(0);
					regAddpr = (JSONObject) addressDetails.get(1);
					Log.consoleLog(ifr, "regAdd jsonobject :" + regAdd.toString());
					if (checkNullObject(ifr, regAdd.get("Line1")) || checkNullObject(ifr, regAdd.get("Line2"))) {
						Log.consoleLog(ifr, "Line1 or Line2 is not null");
						// disableFrame(ifr,
						// "Borrower_Info_RegAddLine1,Borrower_Info_RegLine2,Borrower_Info_RegCity,Borrower_Info_RegState,Borrower_Info_PinCode");
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_AD1", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("Line1"), "PortalLOS_OD_T_ResDet_PA_AD1"));
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_AD2", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("Line2"), "PortalLOS_OD_T_ResDet_PA_AD2"));
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_City", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("City_Town_Village"), "PortalLOS_OD_T_ResDet_PA_City"));
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_State", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("State"), "PortalLOS_OD_T_ResDet_PA_State"));
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_Country", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("Country"), "PortalLOS_OD_T_ResDet_PA_Country"));
						ifr.setValue("PortalLOS_OD_T_ResDet_PA_Pincode", checkIfNullOrReturnSpaceObject(ifr,
								regAdd.get("PinCode"), "PortalLOS_OD_T_ResDet_PA_Pincode"));
						ifr.setValue("PortalLOS_OD_T_ResDet_CA_AD1", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Line1"), "PortalLOS_OD_T_ResDet_CA_AD1"));
						ifr.setValue("PortalLOS_OD_T_ResDet_CA_AD2", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Line2"), "PortalLOS_OD_T_ResDet_CA_AD2"));
						ifr.setValue("PortalLOS_OD_C_ResDet_CA_City", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("City_Town_Village"), "PortalLOS_OD_C_ResDet_CA_City"));
						ifr.setValue("PortalLOS_OD_C_ResDet_CA_State", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("State"), "PortalLOS_OD_C_ResDet_CA_State"));
						ifr.setValue("PortalLOS_OD_C_ResDet_CA_Country", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("Country"), "PortalLOS_OD_C_ResDet_CA_Country"));
						ifr.setValue("PortalLOS_OD_T_ResDet_CA_Pincode", checkIfNullOrReturnSpaceObject(ifr,
								regAddpr.get("PinCode"), "PortalLOS_OD_T_ResDet_CA_Pincode"));
					}
				}
			}
		}
		return "";
	}

	public void mAccOnLoadConsent(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccOnLoadConsent");
		String loanType = getLoanType(ifr, control, event, value);
		Log.consoleLog(ifr, "loanType mAccOnLoadConsent : " + loanType);
		if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
			ifr.setStyle("PortalLOS_B_ConDisc_ViewAggrement", "visible", "false");
			ifr.setStyle("PortalLOS_B_ConDisc_DownloadDocument", "visible", "false");
			ifr.setStyle("CD_FRAME4", "visible", "false");
		}
	}

	public void mAccgenerateDoc(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccgenerateDoc");

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		jsonObj.put("Document Name", "Sanction Latter");
		jsonObj.put("Generate", "");
		jsonObj.put("view", "");
		jsonObj.put("Status", "Pending");
		jsonArr.add(jsonObj);
		jsonObj1.put("Document Name", "Pledge Latter");
		jsonObj1.put("Generate", "");
		jsonObj1.put("view", "");
		jsonObj1.put("Status", "Pending");
		jsonArr.add(jsonObj1);
		ifr.addDataToGrid("Portal_Generate_Doc_L", jsonArr);

	}

	public String executeRestBRMS(IFormReference ifr, String req, String url) throws IOException {
		final String POST_PARAMS = req;
		String ResponseValue = null;
		Log.consoleLog(ifr, "\"executeRestBRMS POST_PARAMS\" : " + POST_PARAMS);

		HttpURLConnection httpConn = null;
		OutputStream os = null;
		try {
			URL obj = new URL(url);
			httpConn = (HttpURLConnection) obj.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setDoOutput(true);
			os = httpConn.getOutputStream();
			os.write(POST_PARAMS.getBytes());
			os.flush();
			os.close();
			int responseCode = httpConn.getResponseCode();
			Log.consoleLog(ifr, "After HTTP Connection in POST");
			Log.consoleLog(ifr, "POST Response Code" + responseCode);
			Log.consoleLog(ifr, "POST Response Message " + httpConn.getResponseMessage());
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				Log.consoleLog(ifr, "After HTTP Connection" + response.toString());
				ResponseValue = response.toString();
			} else {
				Log.consoleLog(ifr, "POST NOT WORKED");
			}
		} catch (Exception ex) {
			Log.errorLog(ifr, "Error in executeRestBRMS finally1 : " + ex);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception ex) {
				Log.errorLog(ifr, "Error in executeRestBRMS finally1 : " + ex);
			} finally {
				try {
					if (httpConn != null) {
						httpConn.disconnect();
					}
				} catch (Exception exc) {
					Log.errorLog(ifr, "Error in executeRestBRMS finally2 : " + exc);
				}
			}
		}
		return ResponseValue;
	}

	public String getCalulatedProcessingFeeCB(IFormReference ifr, String amount) {
		Log.consoleLog(ifr, " getCalulatedProcessingFeeCB : ");
		String processFee = "";
		double principal = Double.parseDouble(amount);
		Log.consoleLog(ifr, " principal : " + principal);
		double fee = Math.round(principal * 0.50) / 100.0;
		if (fee < 1000) {
			fee = 1000;
		} else if (fee > 5000) {
			fee = 5000;
		}
		Log.consoleLog(ifr, " fee : " + fee);
		double taxFee = Math.round(fee * 0.18) / 100.0;
		Log.consoleLog(ifr, " taxFee : " + taxFee);
		double proFee = fee + taxFee;
		Log.consoleLog(ifr, " proFee : " + proFee);
		processFee = String.valueOf(proFee);
		return processFee;
	}
	// Added document Validation code for canara budget

	public String countWFDone(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside countWFDone::");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId().toString();
			int gridSize = ifr.getDataFromGrid("CB_UPLOAD_DOCUMENT").size();
			HashMap<String, String> docMap = new HashMap<>();
			if (gridSize > 0) {
				String documentName[] = new String[gridSize];
				String uploadedDate[] = new String[gridSize];
				for (int i = 0; i < gridSize; i++) {
					documentName[i] = ifr.getTableCellValue("CB_UPLOAD_DOCUMENT", i, 2);
					uploadedDate[i] = ifr.getTableCellValue("CB_UPLOAD_DOCUMENT", i, 4);
					docMap.put(documentName[i], uploadedDate[i]);
				}
				// String docQuery = "Select a.DocumentName from los_m_document a inner join
				// LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='S22'
				// and a.Mandatory='Yes'";
				String docQuery = ConfProperty.getQueryScript("getDocNameQueryNext");
				List<List<String>> docQueryResult = cf.mExecuteQuery(ifr, docQuery, "Query for docQuery::");
				int chkDocMandotoryCount = 0;
				int documentName_Length = documentName.length;
				int docQueryResult_size = documentName.length;
				Log.consoleLog(ifr,
						"PortalCommonMethods:countWFDone-> documentName size from Form" + documentName_Length);

				Log.consoleLog(ifr,
						"PortalCommonMethods:countWFDone-> docQueryResult size from DB" + docQueryResult_size);

				for (int i = 0; i < docQueryResult.size(); i++) {
					String strDocName = docQueryResult.get(i).get(0);
					Log.consoleLog(ifr, "PortalCommonMethods:countWFDone->strDocName::" + strDocName);
					if (docMap.get(strDocName).equalsIgnoreCase("")) {
						Log.consoleLog(ifr,
								"PortalCommonMethods:countWFDone-> Mandatory doc not uploaded is " + strDocName);
						chkDocMandotoryCount++;
					}
				}
				Log.consoleLog(ifr, "PortalCommonMethods:countWFDone->chkDocMandotoryCount::" + chkDocMandotoryCount);
				if (chkDocMandotoryCount > 0) {
					JSONObject message = new JSONObject();
					message.put("showMessage", cf.showMessage(ifr, "", "error", "Please upload mandatory document!!"));
					message.put("eflag", "false");// Hard Stop
					return message.toString();
				} else {
					String ApplicationNo = "";
					Log.consoleLog(ifr, "PCM:::ApplicationNo::::" + ApplicationNo);
					String query1 = ConfProperty.getQueryScript("PORTALAPPLICATIONNOQUERY").replaceAll("#WINAME#", PID);
					// select application_no from LOS_WIREFERENCE_TABLE where winame='#WINAME#'
					List<List<String>> ApplicationNoList = ifr.getDataFromDB(query1);
					if (!ApplicationNoList.isEmpty()) {
						ApplicationNo = ApplicationNoList.get(0).get(0);

					}
					Log.consoleLog(ifr, "PCM:::ApplicationNo::::" + ApplicationNo);
					// Added by Ahmed on 27-06-2024 for perfios Integration
//                    ITROrchestrator objITROrch = new ITROrchestrator();
//                    String itrStatus = objITROrch.triggerITROrchestrator(ifr);
//                    if (itrStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                        return RLOS_Constants.ERROR;
//                    }
//
//                    BSAOrchestrator objBSAOrch = new BSAOrchestrator();
//                    String bsaStatus = objBSAOrch.triggerBSAOrchestrator(ifr);
//                    if (bsaStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                        return RLOS_Constants.ERROR;
//                    }
//
//                    SSAOrchestrator objSSAOrch = new SSAOrchestrator();
//                    String ssaStatus = objSSAOrch.triggerSSAOrchestrator(ifr);
//                    if (ssaStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                        return RLOS_Constants.ERROR;
//                    }

					Log.consoleLog(ifr, "Inside update count Wf done ::");
					String Updatestepname = ConfProperty.getQueryScript("Updatestepname").replaceAll("#WINAME#", PID);
					String query = ConfProperty.getQueryScript("PORTALUPDATECOUNTWFDONE").replaceAll("#WINAME#", PID);
					ifr.saveDataInDB(query);
					cf.mExecuteQuery(ifr, Updatestepname, "Query for Updatestepnamequery::");

					return "Yes" + "~" + ApplicationNo;

				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in countWFDone::" + e);
			Log.consoleLog(ifr, "Exception in countWFDone::" + e);
		}
		return "";
	}

	public String countWFDonePension(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside countWFDone::");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			int gridSize = ifr.getDataFromGrid("CP_UPLOAD_DOCUMENT").size();
			HashMap<String, String> docMap = new HashMap<>();
			if (gridSize > 0) {
				String documentName[] = new String[gridSize];
				String uploadedDate[] = new String[gridSize];
				for (int i = 0; i < gridSize; i++) {
					documentName[i] = ifr.getTableCellValue("CP_UPLOAD_DOCUMENT", i, 1);
					uploadedDate[i] = ifr.getTableCellValue("CP_UPLOAD_DOCUMENT", i, 4);
					docMap.put(documentName[i], uploadedDate[i]);
				}
				String docQuery = "Select a.DocumentName from los_m_document a inner join LOS_M_DOCUMENT_Scheme b on b.Documentid=a.Documentid where b.schemeid='S30' and a.Mandatory='Yes'";
				List<List<String>> docQueryResult = cf.mExecuteQuery(ifr, docQuery, "Query for docQuery::");
				int chkDocMandotoryCount = 0;
				for (int i = 0; i < docQueryResult.size(); i++) {
					String strDocName = docQueryResult.get(i).get(0);
					Log.consoleLog(ifr, "PortalCommonMethods:countWFDonePension->strDocName::" + strDocName);
					if (docMap.get(strDocName).equalsIgnoreCase("")) {
						chkDocMandotoryCount++;
					}
				}
				Log.consoleLog(ifr,
						"PortalCommonMethods:countWFDonePension->chkDocMandotoryCount::" + chkDocMandotoryCount);
				if (chkDocMandotoryCount > 0) {
					JSONObject message = new JSONObject();
					message.put("showMessage", cf.showMessage(ifr, "", "error", "Please upload mandatory document!!"));
					message.put("eflag", "false");// Hard Stop
					return message.toString();
				} else {
					Log.consoleLog(ifr, "Inside update count Wf done ::");
					String query = ConfProperty.getQueryScript("PORTALUPDATECOUNTWFDONE").replaceAll("#WINAME#", PID);
					ifr.saveDataInDB(query);

					String updateQuery = "Update los_wireference_table " + "set curr_stage='Final Eligibility ' "
							+ "where winame='" + PID + "'";
					ifr.saveDataInDB(updateQuery);

					return "Yes";
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in countWFDone::" + e);
			Log.consoleLog(ifr, "Exception in countWFDone::" + e);
		}
		return "";
	}

	// modified by ishwarya on 16-08-2024
	public String mAccSetSliderValue(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside mAccSetSliderValue : ");
		Log.consoleLog(ifr, "value : " + value);
		try {
			String splitValue[] = value.split(",");
			String loanAmount = splitValue[0];
			Log.consoleLog(ifr, "loanAmount : " + loanAmount);
			String tenure = splitValue[1];
			Log.consoleLog(ifr, "tenure : " + tenure);
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "PID : " + PID);
			String query = ConfProperty.getQueryScript("PORTALSLIDERCHECK").replaceAll("#WINAME#", PID);
			Log.consoleLog(ifr, " query : " + query);
			List<List<String>> list = ifr.getDataFromDB(query);
			Log.consoleLog(ifr, "list : " + list);
			String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
			List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL,
					"Execute query for fetching loan selected ");
			String loan_selected = loanSelected.get(0).get(0);
			Log.consoleLog(ifr, "loan type==>" + loan_selected);
			if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
				String netIncome = "";
				String roi = "";
				String queryN = ConfProperty.getQueryScript("netIncomeQuery").replaceAll("#PID#", PID);
				List<List<String>> netIncomeQuery = cf.mExecuteQuery(ifr, queryN,
						"Execute query for fetching loan selected ");
				if (!netIncomeQuery.isEmpty()) {
					netIncome = netIncomeQuery.get(0).get(0);
					roi = netIncomeQuery.get(0).get(1);
					Log.consoleLog(ifr, "netIncome==>" + netIncome);
					Log.consoleLog(ifr, "roi==>" + roi);
				}
				String loanAmnt = "-" + loanAmount;
				BigDecimal rate = new BigDecimal(roi);
				int tenureInt = Integer.parseInt(tenure);
				BigDecimal emicalc = calculateEMIPMT(ifr, loanAmnt, rate, tenureInt);
				String emi = emicalc.toString();
				Log.consoleLog(ifr, "emi : " + emi);

				BigDecimal emiDecimal = new BigDecimal(emi);
				BigDecimal netIncomeDecimal = new BigDecimal(netIncome);

				Log.consoleLog(ifr, "netIncome : " + netIncomeDecimal + " and emi : " + emiDecimal);
				if (emiDecimal.compareTo(netIncomeDecimal) > 0) {
					Log.consoleLog(ifr,
							"inside if condition:::netIncome : " + netIncomeDecimal + " and emi : " + emiDecimal);
					JSONObject message = new JSONObject();
					message.put("MSGSTS", "N");
					message.put("SHOWMSG",
							"You are not eligible to avail beyond this tenure as per bank's guidelines.");
					return message.toString();
				} else {
					Log.consoleLog(ifr,
							"inside else condition:::netIncome : " + netIncomeDecimal + " and emi : " + emiDecimal);
					if (list.isEmpty()) {
						String instQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUEINSERT")
								.replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount)
								.replaceAll("#Tenure#", tenure);
						Log.consoleLog(ifr, "instQuery : " + instQuery);
						ifr.saveDataInDB(instQuery);
					} else {
						String updateQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUPDATE")
								.replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount)
								.replaceAll("#Tenure#", tenure);
						Log.consoleLog(ifr, "updateQuery : " + updateQuery);
						ifr.saveDataInDB(updateQuery);
					}
					ifr.setValue("P_VL_PA_EMI", emi);
				}
			} else {
				if (list.isEmpty()) {
					String instQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUEINSERT")
							.replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount)
							.replaceAll("#Tenure#", tenure);
					Log.consoleLog(ifr, " instQuery : " + instQuery);
					cf.mExecuteQuery(ifr, instQuery, "Update value of query");
				} else {
					String updateQuery = ConfProperty.getQueryScript("PORTALSLIDERVALUPDATE")
							.replaceAll("#WINAME#", PID).replaceAll("#Loan_Amount#", loanAmount)
							.replaceAll("#Tenure#", tenure);
					Log.consoleLog(ifr, " updateQuery : " + updateQuery);
					cf.mExecuteQuery(ifr, updateQuery, "Update value of query");
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in mAccSetSliderValue method : " + e);
			Log.errorLog(ifr, " Exception in mAccSetSliderValue method : " + e);
		}
		return "";
	}

	/*
	 * public String populateOccuapationDetails(IFormReference ifr, String control,
	 * String event, String value) { Log.consoleLog(ifr,
	 * "populateOccuapationDetails : "); try { String PID =
	 * ifr.getObjGeneralData().getM_strProcessInstanceId(); String IndexQuery =
	 * ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll(
	 * "#PID#", PID); String IndexQuery1 =
	 * ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO1").replaceAll(
	 * "#PID#", PID); Log.consoleLog(ifr, "OcuppationInfoDetails query::" +
	 * IndexQuery); Log.consoleLog(ifr, "OcuppationInfoDetails1 query::" +
	 * IndexQuery1); List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
	 * List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1); if
	 * (dataResult.size() == 0) { String f_key = dataResult1.get(0).get(0); String
	 * portalFields =
	 * ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFields");
	 * Log.consoleLog(ifr, "portalFields::" + portalFields); String portalFieldStr[]
	 * = portalFields.split(","); Log.consoleLog(ifr, "portalFieldStr[]::" +
	 * portalFieldStr); int size1 = portalFieldStr.length; Log.consoleLog(ifr,
	 * "portalFields size1::" + size1); String portalValue[] = new String[size1];
	 * for (int i = 0; i < size1; i++) { portalValue[i] =
	 * ifr.getValue(portalFieldStr[i]).toString(); } Log.consoleLog(ifr,
	 * "portalFields array portalValue[i]::" + portalValue); String insertQuery =
	 * ConfProperty.getQueryScript("InsertQueryForOccupationInfoGrid")
	 * .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
	 * .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#",
	 * portalValue[2]) .replaceAll("#portalValue3#",
	 * portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
	 * .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#",
	 * portalValue[6]) .replaceAll("#portalValue7#",
	 * portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
	 * .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#",
	 * portalValue[10]) .replaceAll("#portalValue11#",
	 * portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
	 * .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#",
	 * portalValue[14]) .replaceAll("#portalValue15#",
	 * portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
	 * .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#",
	 * portalValue[18]) .replaceAll("#portalValue19#",
	 * portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
	 * .replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#",
	 * portalValue[22]) .replaceAll("#portalValue23#",
	 * portalValue[23]).replaceAll("#portalValue24#", portalValue[24]);
	 * Log.consoleLog(ifr, "insertQuery for Occupation Info::" + insertQuery);
	 * ifr.saveDataInDB(insertQuery); } else { String f_key =
	 * dataResult1.get(0).get(0); String portalFields =
	 * ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFields"); String
	 * portalFieldStr[] = portalFields.split(","); int size1 =
	 * portalFieldStr.length; String portalValue[] = new String[size1]; for (int i =
	 * 0; i < portalFieldStr.length; i++) { portalValue[i] =
	 * ifr.getValue(portalFieldStr[i]).toString(); } String updateQuery =
	 * ConfProperty.getQueryScript("UpdateQueryForOccupationInfoGrid")
	 * .replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
	 * .replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#",
	 * portalValue[2]) .replaceAll("#portalValue3#",
	 * portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
	 * .replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#",
	 * portalValue[6]) .replaceAll("#portalValue7#",
	 * portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
	 * .replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#",
	 * portalValue[10]) .replaceAll("#portalValue11#",
	 * portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
	 * .replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#",
	 * portalValue[14]) .replaceAll("#portalValue15#",
	 * portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
	 * .replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#",
	 * portalValue[18]) .replaceAll("#portalValue19#",
	 * portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
	 * .replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#",
	 * portalValue[22]) .replaceAll("#portalValue23#",
	 * portalValue[23]).replaceAll("#portalValue24#", portalValue[24]);
	 * Log.consoleLog(ifr, "update for Occupation Info::" + updateQuery); int count
	 * = ifr.saveDataInDB(updateQuery); Log.consoleLog(ifr,
	 * "updateOccuapationDetails::" + count); } } catch (Exception e) {
	 * Log.consoleLog(ifr, "error in populateOccuapationDetails" + e);
	 * Log.errorLog(ifr, "error in populateOccuapationDetails" + e); } return ""; }
	 */
	public String populateOccuapationDetails(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "populateOccuapationDetails : ");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
			String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO1").replaceAll("#PID#", PID);
			Log.consoleLog(ifr, "OcuppationInfoDetails query::" + IndexQuery);
			Log.consoleLog(ifr, "OcuppationInfoDetails1 query::" + IndexQuery1);
			List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
			List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);
			String loantype = getLoanType(ifr, control, event, value);
			if (dataResult.size() == 0) {
				String f_key = dataResult1.get(0).get(0);
				String portalFields = "";
				if (loantype.equalsIgnoreCase("Canara Budget")) {
					portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFields");
				} else if (loantype.equalsIgnoreCase("Canara Pension")) {
					portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFields");
				}
				Log.consoleLog(ifr, "portalFields::" + portalFields);
				String portalFieldStr[] = portalFields.split(",");
				Log.consoleLog(ifr, "portalFieldStr[]::" + portalFieldStr);
				int size1 = portalFieldStr.length;
				Log.consoleLog(ifr, "portalFields size1::" + size1);
				String portalValue[] = new String[size1];
				for (int i = 0; i < size1; i++) {
					portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
				}
				Log.consoleLog(ifr, "portalFields array portalValue[i]::" + portalValue);
				String insertQuery = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGrid")
						.replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
						.replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
						.replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
						.replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
						.replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
						.replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
						.replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
						.replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
						.replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
						.replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
						.replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
						.replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22])
						.replaceAll("#portalValue23#", portalValue[23]).replaceAll("#portalValue24#", portalValue[24]);
				Log.consoleLog(ifr, "insertQuery for Occupation Info::" + insertQuery);
				ifr.saveDataInDB(insertQuery);
			} else {
				String f_key = dataResult1.get(0).get(0);
				String portalFields = "";
				if (loantype.equalsIgnoreCase("Canara Budget")) {
					portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFields");
				} else if (loantype.equalsIgnoreCase("Canara Pension")) {
					portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFields");
				}
				Log.consoleLog(ifr, "portalFields : " + portalFields);
				String portalFieldStr[] = portalFields.split(",");
				int size1 = portalFieldStr.length;
				String portalValue[] = new String[size1];
				for (int i = 0; i < portalFieldStr.length; i++) {
					portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
				}
				Log.consoleLog(ifr, "portalValue : " + portalValue);
				String updateQuery = ConfProperty.getQueryScript("UpdateQueryForOccupationInfoGrid")
						.replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
						.replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
						.replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
						.replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
						.replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
						.replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
						.replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
						.replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
						.replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
						.replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
						.replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
						.replaceAll("#portalValue21#", portalValue[21]).replaceAll("#portalValue22#", portalValue[22])
						.replaceAll("#portalValue23#", portalValue[23]).replaceAll("#portalValue24#", portalValue[24]);
				Log.consoleLog(ifr, "update for Occupation Info::" + updateQuery);
				int count = ifr.saveDataInDB(updateQuery);
				Log.consoleLog(ifr, "updateOccuapationDetails::" + count);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "error in populateOccuapationDetails" + e);
			Log.errorLog(ifr, "error in populateOccuapationDetails" + e);
		}
		return "";
	}

	public String populateOccuapationDetailsforCoBorrower(IFormReference ifr, String control, String event,
			String value) {
		Log.consoleLog(ifr, "populateOccuapationDetailsforCoBorrower : ");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER").replaceAll("#PID#",
					PID);
			String IndexQuery1 = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFOCOBORROWER1")
					.replaceAll("#PID#", PID);
			Log.consoleLog(ifr, "OccuppationInfoDetails query Co-Obligant::" + IndexQuery);
			Log.consoleLog(ifr, "OccuppationInfoDetails1 query Co-Obligant::" + IndexQuery1);

//            List<List<String>> dataResult = ifr.getDataFromDB(IndexQuery);
//            List<List<String>> dataResult1 = ifr.getDataFromDB(IndexQuery1);
			List<List<String>> dataResult = cf.mExecuteQuery(ifr, IndexQuery, "occupation Fkey query Co-Obligant ");
			List<List<String>> dataResult1 = cf.mExecuteQuery(ifr, IndexQuery1, "basicInfo Fkey query Co-Obligant ");
			Log.consoleLog(ifr, "dataResult::" + dataResult);
			Log.consoleLog(ifr, "dataResult1::" + dataResult1);
			String f_key = "";
			if (!dataResult1.isEmpty()) {
				f_key = dataResult1.get(0).get(0);
			}

			String loanType = getLoanType(ifr, control, event, value);
			String portalFields = "";
			if (loanType.equalsIgnoreCase("Canara Budget")) {
				portalFields = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFieldsCoborrower");
			} else if (loanType.equalsIgnoreCase("Canara Pension")) {
				portalFields = ConfProperty.getCommonPropertyValue("PensionOccupationDetailsFieldsCoborrower");
			}
			if (dataResult.size() == 0) {
				// String f_key = "3709";
				Log.consoleLog(ifr, " INSIDE IF f_key::" + f_key);
				Log.consoleLog(ifr, "portalFields CoBorrower::" + portalFields);
				String portalFieldStr[] = portalFields.split(",");
				Log.consoleLog(ifr, "portalFieldStr[] CoBorrower::" + portalFieldStr);
				int size1 = portalFieldStr.length;
				Log.consoleLog(ifr, "portalFields CoBorrower size1::" + size1);
				String portalValue[] = new String[size1];
				for (int i = 0; i < size1; i++) {
					portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
				}
				Log.consoleLog(ifr, "portalFields array portalValue[i]::CoBorrower ::" + portalValue);
				String insertQuery = ConfProperty.getQueryScript("InsertQueryForOccupationInfoGridCoborrower")
						.replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
						.replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
						.replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
						.replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
						.replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
						.replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
						.replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
						.replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
						.replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
						.replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
						.replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
						.replaceAll("#portalValue21#", portalValue[21]);
				Log.consoleLog(ifr, "insertQuery for Occupation Info::CoBorrower ::" + insertQuery);
				ifr.saveDataInDB(insertQuery);

			} else {
				Log.consoleLog(ifr, " INSIDE IF dataResult1::" + dataResult.size());
				Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower f_key::" + f_key);
				Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::" + portalFields);
				String portalFieldStr[] = portalFields.split(",");
				int size1 = portalFieldStr.length;
				Log.consoleLog(ifr, "portalFields for Occupation Info::CoBorrower ::size1" + size1);
				String portalValue[] = new String[size1];
				for (int i = 0; i < portalFieldStr.length; i++) {
					portalValue[i] = ifr.getValue(portalFieldStr[i]).toString();
				}
				String updateQuery = ConfProperty.getQueryScript(" UpdateQueryForOccupationInfoGridCoborrower")
						.replaceAll("#f_key#", f_key).replaceAll("#portalValue0#", portalValue[0])
						.replaceAll("#portalValue1#", portalValue[1]).replaceAll("#portalValue2#", portalValue[2])
						.replaceAll("#portalValue3#", portalValue[3]).replaceAll("#portalValue4#", portalValue[4])
						.replaceAll("#portalValue5#", portalValue[5]).replaceAll("#portalValue6#", portalValue[6])
						.replaceAll("#portalValue7#", portalValue[7]).replaceAll("#portalValue8#", portalValue[8])
						.replaceAll("#portalValue9#", portalValue[9]).replaceAll("#portalValue10#", portalValue[10])
						.replaceAll("#portalValue11#", portalValue[11]).replaceAll("#portalValue12#", portalValue[12])
						.replaceAll("#portalValue13#", portalValue[13]).replaceAll("#portalValue14#", portalValue[14])
						.replaceAll("#portalValue15#", portalValue[15]).replaceAll("#portalValue16#", portalValue[16])
						.replaceAll("#portalValue17#", portalValue[17]).replaceAll("#portalValue18#", portalValue[18])
						.replaceAll("#portalValue19#", portalValue[19]).replaceAll("#portalValue20#", portalValue[20])
						.replaceAll("#portalValue21#", portalValue[21]);
				Log.consoleLog(ifr, "update for Occupation Info::CoBorrower::" + updateQuery);
				int count = ifr.saveDataInDB(updateQuery);
				Log.consoleLog(ifr, "updateOccuapationDetails::CoBorrower::count" + count);
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);
			Log.errorLog(ifr, "error in populateOccuapationDetailsforCoBorrower " + e);
		}
		return "";

	}

	public String mAccRoundOffvalue(IFormReference ifr, String originalValue) {
		Log.consoleLog(ifr, "inside mAccRoundOffvalue : ");
		double doubleValue = Double.parseDouble(originalValue);
		long roundedValue = Math.round(doubleValue);
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		String formattedValue = decimalFormat.format(roundedValue);
		Log.consoleLog(ifr, "roundedValueString : " + formattedValue);
		return formattedValue;

	}

	// Replace in PortalCommonMethods.java
	public String getPortalDataLoadCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "setValue getPortalDataLoadCB :");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String PortalField = ConfProperty.getCommonPropertyValue("PortalOccupationDetailsFields");
			String portalfield[] = PortalField.split(",");
			String query = ConfProperty.getQueryScript("ROWINDEXCOUNTOCCUPATIONINFO").replaceAll("#PID#", PID);
			List<List<String>> result = ifr.getDataFromDB(query);
			String F_Key = "";
			if (result.size() > 0) {
				F_Key = result.get(0).get(0);
			}
			Log.consoleLog(ifr, "FKey.." + F_Key);
			String query1 = ConfProperty.getQueryScript("PORTALOCCUPATIONINFODATA").replaceAll("#F_Key#", F_Key);
			List<List<String>> result1 = ifr.getDataFromDB(query1);
			Log.consoleLog(ifr, "query1.." + query1);
			if (result1.size() > 0) {
				for (int i = 0; i < portalfield.length; i++) {
					// Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for
					// occupation::"+result1.get(0).get(2).toString());
					if (result1.get(0).get(2).toString().isEmpty()) {
						ifr.setValue("P_CB_OD_TypeOfOccupation", "");
						Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation:: sucess");
					}
					ifr.setValue(portalfield[i], result1.get(0).get(i).toString());

				}
			}
			String ProfileCB = "Salaried";
			Log.consoleLog(ifr, "ProfileCB1==>" + ProfileCB);
			ifr.setValue("P_CB_OD_Profile", ProfileCB);
			ifr.setStyle("P_CB_OD_Profile", "disable", "true");
			String RecoveryMechanism = "Salary / Pension account with us";
			Log.consoleLog(ifr, "RecoveryMechanism==>" + RecoveryMechanism);
			ifr.setValue("P_CB_OD_RecoveryMechanism", RecoveryMechanism); // For Applicant
			ifr.setStyle("P_CB_OD_RecoveryMechanism", "disable", "true");
			// P_CB_OD_RecoveryMechanism_COB
			ifr.setValue("P_CB_OD_RecoveryMechanism_COB", RecoveryMechanism); // For Co-Applicant
			// ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "disable", "true");
			String NatureOfSecurity = "Only Third party personal Guarantee/Co-obligation/waiver permitted by competent authority";
			Log.consoleLog(ifr, "NatureOfSecurity==>" + NatureOfSecurity);
			// Modified by Aravindh on 29-06-24
			ifr.setValue("P_CB_OD_NatureOfSecurity", NatureOfSecurity); // For Applicant
			// ifr.setStyle("P_CB_OD_NatureOfSecurity", "disable", "true");

			// Commented by Ahmed on 17-07-2024
			// ifr.setValue("P_CB_OD_NatureOfSecurity_COB", NatureOfSecurity); // For
			// Co-Obligant
			// ifr.setStyle("P_CB_OD_NatureOfSecurity_COB", "disable", "true");
//            String superannuation = result1.get(0).get(19);
//            Log.consoleLog(ifr, "superannuation date before format: " + superannuation);
//            if(!superannuation.isEmpty()){
//            SimpleDateFormat Originalformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            SimpleDateFormat Targetformat = new SimpleDateFormat("dd/MM/yyyy");
//            Date dateDD = Originalformat.parse(superannuation);
//            Log.consoleLog(ifr, "dateDD : " + dateDD);
//            superannuation = Targetformat.format(dateDD);
//            Log.consoleLog(ifr, "superannuation date after format: " + superannuation);
//            ifr.setValue("P_CB_OD_DATEOFSUPERANNUATION", superannuation);
//            }
			String CustomerId = getCustomerIDCB(ifr, "B");
			Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
			Demographic objDemographic = new Demographic();
			String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, CustomerId);
			Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
			if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
				Log.consoleLog(ifr, "inside error condition Demographic Budget");
				return returnErrorAPIThroughExecute(ifr);
			} else {
				Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
				JSONParser jsonparser = new JSONParser();
				JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
				Log.consoleLog(ifr, obj.toString());
				String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
				Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
				if (!DateOfCustOpen.isEmpty()) {
					LocalDate curDate = LocalDate.now();
					Log.consoleLog(ifr, "curDate  :" + curDate);
					LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
					Log.consoleLog(ifr, "PastDate  :" + PastDate);
					long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
					Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
					int YearsWithCanara = (int) (monthsBetween / 12);
					int MonthsWithCanara = (int) (monthsBetween % 12);
					Log.consoleLog(ifr, "YearsWithCanara: " + YearsWithCanara);
					Log.consoleLog(ifr, "MonthsWithCanara :" + MonthsWithCanara);

					ifr.setValue("P_CB_OD_RelationshipWithCanara", String.valueOf(YearsWithCanara));
					ifr.setValue("P_CB_OD_RelationshipWithCanara_InMonths", String.valueOf(MonthsWithCanara));
					ifr.setStyle("P_CB_OD_RelationshipWithCanara", "disable", "true");
					ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths", "disable", "true");
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception getPortalDataLoadCB : " + e);
			Log.errorLog(ifr, "Exception getPortalDataLoadCB : " + e);
		}
		return "";
	}

	public String populateInPrincipalApprovalCB(IFormReference ifr, String control, String event, String value) {
		try {
			Log.consoleLog(ifr, "populateInPrincipalApprovalCB : ");
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "PID : " + PID);
			String InPricipal = ConfProperty.getCommonPropertyValue("populateInPrinciplePortalFields").replace("₹", "")
					.replace("%", "");
			Log.consoleLog(ifr, "InPricipal" + InPricipal);
			String EmpBackoffmapp = ConfProperty.getCommonPropertyValue("InPrincipleBackOfficeFields").replace("₹", "")
					.replace("%", "");
			Log.consoleLog(ifr, "EmpBackoffmapp" + EmpBackoffmapp);
			String[] Portal_mappings_ED = InPricipal.split(",");
			String[] sttingmapping_EA = EmpBackoffmapp.split(",");
			String[] srt_ED = new String[Portal_mappings_ED.length];
			for (int i = 0; i < Portal_mappings_ED.length; i++) {
				srt_ED[i] = ifr.getValue(Portal_mappings_ED[i]).toString().replace("₹", "").replace("%", "");

			}
			for (int i = 0; i < sttingmapping_EA.length; i++) {
				jsonObject.put(sttingmapping_EA[i], srt_ED[i]);
			}
			jsonArray.add(jsonObject);
			Log.consoleLog(ifr, "jsonArray1234 :" + jsonArray);

			String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTINPRINAPPROVAL").replaceAll("#PID#", PID);
			Log.consoleLog(ifr, "populateInPrincipalApprovalCB query " + IndexQuery);
			List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
			Log.consoleLog(ifr, "populateInPrincipalApprovalCB docIndexList " + docIndexList);
			if (docIndexList.size() == 0) {
				Log.consoleLog(ifr, "populateInPrincipalApprovalCB Info :");

				((IFormAPIHandler) ifr).addDataToGrid("QNL_LOS_PROPOSED_FACILITY", jsonArray, false);
				String loanAmount = ifr.getValue("P_CB_PA_LOAN_AMOUNT").toString();
				String tenure = ifr.getValue("P_CB_PA_TENURE").toString();
				String roi = ifr.getValue("P_CB_PA_RATE_OF_INTEREST").toString();
				String emi = ifr.getValue("P_CB_PA_EMI_INPRNCPL").toString();
				String queryvalue = ConfProperty.getQueryScript("PORTALINPRINAPPROVALVALUE")
						.replaceAll("#loanAmount#", loanAmount).replaceAll("#tenure#", tenure).replaceAll("#roi#", roi)
						.replaceAll("#emi#", emi).replaceAll("#PID#", PID).replace("%", "").replace("₹", "");
				ifr.saveDataInDB(queryvalue);
			} else {
				Log.consoleLog(ifr, "inside else condition product of in principle ");
				((IFormAPIHandler) ifr).setTableCellValue("QNL_LOS_PROPOSED_FACILITY", 0, "CNL_LOS_PROPOSED_FACILITY",
						jsonArray, 1);
			}
			//// tenure opt for elongation
			String checkbox1 = ifr.getValue("P_CB_PA_CHKBX1_INPRNCPL").toString();
			if (checkbox1.equalsIgnoreCase("true")) {
				String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", PID)
						.replaceAll("#checkbox2#", checkbox1);
				Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
				ifr.saveDataInDB(queryCheckbox);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "getCurrentWiMobileNumber : " + e);
			return returnError(ifr);
		}
		return "";
	}

//    public String getPortalInPricipleApprovalCB(IFormReference ifr, String control, String event, String value) {
//
//        Log.consoleLog(ifr, "setValue getPortalInPricipleApprovalCB :");
//        JSONArray dataFromGrid = ((IFormAPIHandler) ifr).getDataFromGrid("QNL_LOS_PROPOSED_FACILITY", true);
//        Log.consoleLog(ifr, "setValue getPortalDataLoadCB :" + dataFromGrid.toString());
//        if (!dataFromGrid.isEmpty()) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject = (JSONObject) dataFromGrid.get(0);
//            Log.consoleLog(ifr, "inside if In principle condition jsonObject" + jsonObject.toString());
//            if (!jsonObject.isEmpty()) {
//                Log.consoleLog(ifr, "inside if In principle condition123.... ");
//                if (jsonObject != null && jsonObject.toString().trim() != "" && jsonObject.toString() != null) {
//                    // perdet = (JSONObject) personalDetails.get(0);
//                    Log.consoleLog(ifr, "perdet jsonobject :" + jsonObject.toString());
//                    ifr.setValue("P_CB_RM_LOANAMOUNT", checkIfNullOrReturnSpaceObject(ifr,
//                            jsonObject.get("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt"), "P_CB_RM_LOANAMOUNT"));
//                    ifr.setValue("P_CB_RM_RATE_OF_INTEREST", checkIfNullOrReturnSpaceObject(ifr,
//                            jsonObject.get("QNL_LOS_PROPOSED_FACILITY_ROI"), "P_CB_RM_RATE_OF_INTEREST"));
//                    ifr.setValue("P_CB_RM_TENURE", checkIfNullOrReturnSpaceObject(ifr,
//                            jsonObject.get("QNL_LOS_PROPOSED_FACILITY_Tenure"), "P_CB_RM_TENURE"));
//                    ifr.setValue("P_CB_PA_EMI_INPRNCPL", checkIfNullOrReturnSpaceObject(ifr,
//                            jsonObject.get("QNL_LOS_PROPOSED_FACILITY_EMI"), "P_CB_PA_EMI_INPRNCPL"));
//
//                }
//            }
//        }
//        return "";
//    }
	public String getPortalInPricipleApprovalCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside the getPortalInPricipleApprovalCB :");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String roi = ifr.getValue("P_CB_PA_RATE_OF_INTEREST").toString();
		String emi = ifr.getValue("P_CB_PA_EMI_INPRNCPL").toString();
		String tenure = ifr.getValue("P_CB_PA_TENURE").toString();
		String Query = ConfProperty.getQueryScript("UPDATEINPRINCIPLEDATAQUERY").replaceAll("#tenure#", tenure)
				.replaceAll("#roi#", roi).replaceAll("#emi#", emi).replaceAll("#PID#", PID).replace("%", "")
				.replace("₹", "");
		Log.consoleLog(ifr, "Query :" + Query);
		ifr.saveDataInDB(Query);
		return "";
	}

	public String checkExistingCustomerCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside the checkExistingCustomerCB :");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Query1 = ConfProperty.getQueryScript("PORTALITEMINDEX").replaceAll("#PID#", PID);
		List<List<String>> result = ifr.getDataFromDB(Query1);
		String Itemindex = result.get(0).get(0);
		String CustType = value;
		if (value.equalsIgnoreCase("No")) {
			Log.consoleLog(ifr, "inside if condition of checkExistingCustomerCB :");
			String Query = ConfProperty.getQueryScript("PORTALUPDATEEXTCUST").replaceAll("#Itemindex#", Itemindex);
			Log.consoleLog(ifr, "Update Query  :" + Query);
			ifr.saveDataInDB(Query);
			return "No";
		}
		return "";
	}

	public void stepNameUpdate(IFormReference ifr, String value) {
		Log.consoleLog(ifr, " StepName" + value);
		String StepName = value;
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Stepquery = ConfProperty.getQueryScript("PUPDATEStepQuery").replaceAll("#WINAME#", PID)
				.replaceAll("#Curr_Stage#", StepName);
		Log.consoleLog(ifr, "Stepquery:" + Stepquery);
		ifr.saveDataInDB(Stepquery);

	}

	public String generateRandomNumber(IFormReference ifr) {
		Random rnd = new Random();
		Log.consoleLog(ifr, "rnd : " + rnd);
		int number = rnd.nextInt(999999);
		Log.consoleLog(ifr, "number:" + number);// this will convert any number sequence into 6 character.
		return String.format("%06d", number);
	}

	public String getCurrentWiMobileNumber(IFormReference ifr) {
		String mob = "";
		try {
			String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "pid::" + pid);
			String queryOTP = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", pid);
			Log.consoleLog(ifr, "queryOTP : " + queryOTP);
			List<List<String>> list = ifr.getDataFromDB(queryOTP);
			Log.consoleLog(ifr, "list : " + list);
			if (!list.isEmpty()) {
				mob = list.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "getCurrentWiMobileNumber : " + e);
		}
		return mob;

	}

	public String GenerateApplicationNumber(IFormReference ifr, String Product) {
		Log.consoleLog(ifr, "Inside GenerateApplicationNumber...");
		Log.consoleLog(ifr, "Product==>" + Product);
		String AppNumber = "";
		try {
			String Query = "SELECT SEGMENT FROM LOS_GENAPPLICATION_NUMBER " + "WHERE PRODUCT='" + Product
					+ "' AND ROWNUM=1";
			List Result = ifr.getDataFromDB(Query);
			String SegmentSeries = Result.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "SegmentSeries==>" + SegmentSeries);
			String RandomNumber = String.format("%06d", Integer.parseInt(SegmentSeries));
			Log.consoleLog(ifr, "RandomNumber==>" + RandomNumber);
			if (Product.equalsIgnoreCase("RETAIL")) {
				String Segment1 = "CR";
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
				LocalDateTime now = LocalDateTime.now();
				String CurrentDate = dtf.format(now);
				Log.consoleLog(ifr, "CurrentDate==>" + CurrentDate);
				String Segment2 = CurrentDate;
				String Segment3 = RandomNumber;
				AppNumber = Segment1 + Segment2 + Segment3;
				Log.consoleLog(ifr, "AppNumber==>" + AppNumber);
				int SeriesIncrement = Integer.parseInt(SegmentSeries) + 1;
				String SegIncrement = String.valueOf(SeriesIncrement);
				Log.consoleLog(ifr, "SegIncrement==>" + SegIncrement);

				String UpdateQuery = "UPDATE LOS_GENAPPLICATION_NUMBER " + "SET LAST_APPNUMBER='" + AppNumber + "',"
						+ "SEGMENT='" + SegIncrement + "' WHERE PRODUCT='" + Product + "'";
				Log.consoleLog(ifr, "UpdateQuery==>" + UpdateQuery);
				ifr.saveDataInDB(UpdateQuery);
			} else {

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception==>" + e);
		}
		return AppNumber;
	}

	public boolean CheckDuplicateAppNumber(IFormReference ifr, String ApplicationNumber) {
		try {
			String CountQuery = "SELECT COUNT(*) FROM LOS_WIREFERENCE_TABLE " + "WHERE APPLICATION_NO LIKE '%"
					+ ApplicationNumber + "%'";
			List Result = ifr.getDataFromDB(CountQuery);
			String Count = Result.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "Count==>" + Count);
			if (Integer.parseInt(Count) == 0) {
				return true;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception==>" + e);
			Log.errorLog(ifr, "Exception==>" + e);
		}
		return false;
	}

	public String resumeForm(IFormReference ifr) {
		String currentStage = "";
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONObject jsonobject = new JSONObject();
		Date currDate = new Date();
		SimpleDateFormat dateForm = new SimpleDateFormat("dd/MM/yyyy");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String resumeDate = dateForm.format(currDate);
		try {
			Log.consoleLog(ifr, "Inside resumeForm");
			String currentStep = "";
			String queryESign = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
					+ PID + "'";
			List<List<String>> resultEsign = ifr.getDataFromDB(queryESign);
			Log.consoleLog(ifr, "query for esign===>" + queryESign);

			String loanType = getLoanType(ifr, "", "", "");
			Log.consoleLog(ifr, "loanType : " + loanType);
			String currentStepForStaff = "Final Eligibility and Doc";
			EsignCommonMethods objEsign = new EsignCommonMethods();
			String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
			String fundTransferStatus = objEsign.checkFundTransferStatus(ifr);
//			int =Integer.parseInt(triggeredStatus);
			String queryForBackOffice = "SELECT CURR_STAGE FROM LOS_WIREFERENCE_TABLE where WINAME='" + PID + "'";
			Log.consoleLog(ifr, "#Inside query..." + queryForBackOffice);
			List<List<String>> list = ifr.getDataFromDB(queryForBackOffice);
			if (!list.isEmpty()) {
				currentStage = list.get(0).get(0);
			}
			if (currentStage.equalsIgnoreCase("BackOffice") && loanType.equalsIgnoreCase("Staff Loan")) {
				return "error,Your application is under processing. Kindly contact your salary account branch for more details";
			}
			else if (currentStage.equalsIgnoreCase("BackOffice")) {
				return "error,Your application is under processing. Kindly contact your loan availing branch for more details";
			}
			String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET DATE2='" + resumeDate + "' WHERE WINAME='" + PID
					+ "'";

			Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
			ifr.saveDataInDB(queryUpdate);
			String selectquery = ConfProperty.getQueryScript("PSELECTStepQuery").replaceAll("#winame#", PID);
			List<List<String>> StepList = cf.mExecuteQuery(ifr, selectquery, "Resume Journey Query:");
			if (loanType.equalsIgnoreCase("Staff Loan")) {
				if (Integer.parseInt(fundTransferStatus) > 0) {
					currentStep = "Final Eligibility and Doc";
					String query = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='" + currentStepForStaff + "'"
							+ "where WINAME='" + PID + "'";
					Log.consoleLog(ifr, "query for nesl : " + query);
					ifr.saveDataInDB(query);
					currentStep = "Final Eligibility and Doc";
					Log.consoleLog(ifr, "current step for resuem" + currentStep);

//	                if (loanType.trim().equalsIgnoreCase("")) {
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Final Eligibility and Doc")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Receive the Money")) {
						jsonobject.put("StepCode", "3");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else {
						jsonobject.put("StepCode", "10");
					}
					ifr.setStyle("navigationBackBtn", "disable", "true");
				} else if (!resultEsign.isEmpty()
						&& (Integer.parseInt(triggeredStatus) > 0 && resultEsign.get(0).get(0).equalsIgnoreCase("Y")
								&& resultEsign.get(0).get(1).equalsIgnoreCase("Success"))) {
					currentStep = "Receive the Money";
					Log.consoleLog(ifr, "current step for resuem" + currentStep);

//	                if (loanType.trim().equalsIgnoreCase("")) {
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Final Eligibility and Doc")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Receive the Money")) {
						jsonobject.put("StepCode", "3");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else {
						jsonobject.put("StepCode", "10");
					}
					ifr.setStyle("navigationBackBtn", "disable", "true");
				} else if (StepList.size() > 0) {
					Log.consoleLog(ifr, "current step for resuem" + currentStep);

//	                if (loanType.trim().equalsIgnoreCase("")) {
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Final Eligibility and Doc")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Receive the Money")) {
						jsonobject.put("StepCode", "3");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else {
						jsonobject.put("StepCode", "10");
					}
					ifr.setStyle("navigationBackBtn", "disable", "true");
				} else {
					jsonobject.put("StepCode", "10");
					Log.consoleLog(ifr, "data is empty");
				}
			}
			else if (loanType.equalsIgnoreCase("Staff Vehicle")) {
				if (StepList.size() > 0) {
					currentStep = StepList.get(0).get(0);
					Log.consoleLog(ifr, "current step for resuem" + currentStep);
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Collateral Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Document Upload")) {
						jsonobject.put("StepCode", "3");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else {
						jsonobject.put("StepCode", "10");
					}
				} else {
					jsonobject.put("StepCode", "10");
					Log.consoleLog(ifr, "data is empty");
				}

			}
			else if (loanType.equalsIgnoreCase("Staff Home Loan")) {
				if (StepList.size() > 0) {
					currentStep = StepList.get(0).get(0);
					Log.consoleLog(ifr, "current step for resuem" + currentStep);
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Co-Applicant")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Collateral Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("In-Principle Approval")) {
						jsonobject.put("StepCode", "3");
					} else if (currentStep.trim().equalsIgnoreCase("Document Upload")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						// mAccCompleteWorkItemStatus(ifr);
						jsonobject.put("StepCode", "4");
					} else {
						jsonobject.put("StepCode", "10");
					}
				} else {
					jsonobject.put("StepCode", "10");
					Log.consoleLog(ifr, "data is empty");
				}

			}
			else if (loanType.contains("Kavach")) {
				if (StepList.size() > 0) {
					currentStep = StepList.get(0).get(0);
					Log.consoleLog(ifr, "current step for resuem" + currentStep);
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Insurance Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						jsonobject.put("StepCode", "3");
					}  else {
						jsonobject.put("StepCode", "10");
					}
				} else {
					jsonobject.put("StepCode", "10");
					Log.consoleLog(ifr, "data is empty");
				}

			}
			else if (loanType.contains("Gold")) {
				if (StepList.size() > 0) {
					currentStep = StepList.get(0).get(0);
					Log.consoleLog(ifr, "current step for resuem" + currentStep);
					Log.consoleLog(ifr, "loanType : " + loanType);
					if (currentStep.trim().equalsIgnoreCase("Staff Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "0");
					} else if (currentStep.trim().equalsIgnoreCase("Jewellery Details")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "1");
					} else if (currentStep.trim().equalsIgnoreCase("Avail Offer")) {
						Log.consoleLog(ifr, "currentStep : " + currentStep);
						jsonobject.put("StepCode", "2");
					} else if (currentStep.trim().equalsIgnoreCase("Summary")) {
						jsonobject.put("StepCode", "3");
					}  else {
						jsonobject.put("StepCode", "10");
					}
				} else {
					jsonobject.put("StepCode", "10");
					Log.consoleLog(ifr, "data is empty");
				}

			}
			return jsonobject.toJSONString();
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
			Log.errorLog(ifr, "Exception in getting currentStepName : " + ExceptionUtils.getStackTrace(e));
			return "error, Server Exception occured, please try after sometime";
		}

	}

	public String getCalulatedProcessingFeeLad(IFormReference ifr, String amount) {
		String processFee = "";
		double principal = Double.parseDouble(amount);
		Log.consoleLog(ifr, " principal : " + principal);
		double fee = Math.round(principal * 0.50) / 100.0;

		if (fee < 500) {
			fee = 500;
		} else if (fee > 2500) {
			fee = 2500;
		}
		Log.consoleLog(ifr, " fee : " + fee);
		double taxFee = Math.round(fee * 0.18) / 100.0;
		Log.consoleLog(ifr, " taxFee : " + taxFee);
		double proFee = fee + taxFee;
		Log.consoleLog(ifr, " proFee : " + proFee);
		processFee = String.valueOf(proFee);
		return processFee;
	}
	// ===============================Mayank Code
	// Start===================================================//

	public String setValueInBackOffice(IFormReference ifr, String PortalFields, String BackOfficeFields) {
		Log.consoleLog(ifr, "Inside setValueInBackOffice:" + PortalFields + ":" + BackOfficeFields);
		String[] Portal_mappings = ConfProperty.getCommonPropertyValue(PortalFields).split(",");
		String[] sttingmapping = ConfProperty.getCommonPropertyValue(BackOfficeFields).split(",");
		for (int i = 0; i < sttingmapping.length; i++) {
			ifr.setValue(sttingmapping[i], ifr.getValue(Portal_mappings[i]).toString());
		}
		return "";
	}

	// Commented by monesh for getting Application number from dlp common object
	public void mImplApplicationNameRefrenceNum(IFormReference ifr, String ProductType) {
		Log.consoleLog(ifr, "Inside mImplApplicationNameRefrenceNum : ");

		try {
			String mobileData = "";
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String query = "SELECT MOBILENUMBER ,LoanType from LOS_WIREFERENCE_TABLE where WINAME = '"
					+ processInstanceId + "' ";
			String Loantype = "";
			Log.consoleLog(ifr, "mobileData query : " + query);
			List<List<String>> list = ifr.getDataFromDB(query);
			if (!list.isEmpty()) {
				mobileData = list.get(0).get(0);
				Loantype = list.get(0).get(1);
			}

			String getPreApprLoanQuery = ConfProperty.getQueryScript("getPreApprLoanQuery").replaceAll("#mobile_no#",
					mobileData);
			Log.consoleLog(ifr, "loanAmount query : " + getPreApprLoanQuery);
			List<List<String>> loanAmount = ifr.getDataFromDB(getPreApprLoanQuery);
			String customerName = "";
			if (!loanAmount.isEmpty()) {
				customerName = loanAmount.get(0).get(1);
				ifr.setValue("P_PAPL_CUSTOMERNAME1", customerName);
			}
			try {

				String ApplicationNumberQuery = "SELECT APPLICATIONNUMBER from LOS_EXT_TABLE "
						+ "where ITEMINDEX IN(SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE " + "PROCESSINSTANCEID='"
						+ processInstanceId + "' AND ROWNUM=1)";
				Log.consoleLog(ifr, "ApplicationNumberQuery==>" + ApplicationNumberQuery);
				List<List<String>> Result1 = ifr.getDataFromDB(ApplicationNumberQuery);
				String AppNumber = "";
				if (!Result1.isEmpty()) {
					AppNumber = Result1.get(0).get(0);
				}
				// String AppNumber = Result1.toString().replace("[", "").replace("]", "");
				Log.consoleLog(ifr, "AppNumber From Database" + AppNumber);

				if (AppNumber.equalsIgnoreCase("")) {
					Log.consoleLog(ifr, "AppNumber is empty");

					String ApplicationNumber = GenerateApplicationNumber(ifr, "RETAIL");

					if (CheckDuplicateAppNumber(ifr, ApplicationNumber)) {
						Log.consoleLog(ifr, "No duplicates..");
						ifr.setValue("P_PAPL_REFERENCENO1", ApplicationNumber);
						String AppNumUpQuery = "UPDATE LOS_WIREFERENCE_TABLE " + "SET APPLICATION_NO='"
								+ ApplicationNumber + "' WHERE winame='" + processInstanceId + "'";
						Log.consoleLog(ifr, "AppNumUpQuery==> " + AppNumUpQuery);
						ifr.saveDataInDB(AppNumUpQuery);

						String AppNumUpQuery1 = "UPDATE LOS_EXT_TABLE " + "SET APPLICATIONNUMBER='" + ApplicationNumber
								+ "' WHERE " + "ITEMINDEX IN (SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE "
								+ "PROCESSINSTANCEID='" + processInstanceId + "')";
						Log.consoleLog(ifr, "AppNumUpQuery1==> " + AppNumUpQuery1);
						ifr.saveDataInDB(AppNumUpQuery1);
					} else {
						ApplicationNumber = GenerateApplicationNumber(ifr, "RETAIL");
						ifr.setValue("P_PAPL_REFERENCENO1", ApplicationNumber);

						String AppNumUpQuery = "UPDATE LOS_WIREFERENCE_TABLE " + "SET APPLICATION_NO='"
								+ ApplicationNumber + "' WHERE winame='" + processInstanceId + "'";
						Log.consoleLog(ifr, "AppNumUpQuery==> " + AppNumUpQuery);
						ifr.saveDataInDB(AppNumUpQuery);

						String AppNumUpQuery1 = "UPDATE LOS_EXT_TABLE " + "SET APPLICATIONNUMBER='" + ApplicationNumber
								+ "' WHERE " + "ITEMINDEX IN (SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE "
								+ "PROCESSINSTANCEID='" + processInstanceId + "')";
						Log.consoleLog(ifr, "AppNumUpQuery1==> " + AppNumUpQuery1);
						ifr.saveDataInDB(AppNumUpQuery1);

					}

					// Added by Ahmed on 10-05-2024 triggering MailContent from
					// DLPCommonObjects=========
					String bodyParams = "RETAIL" + "#" + ApplicationNumber;
					String subjectParams = ApplicationNumber;
					String fileName = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
					String fileContent = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
					triggerCCMAPIs(ifr, processInstanceId, "PAPL", "2", bodyParams, subjectParams, fileName,
							fileContent);
					// Ended by Ahmed on 10-05-2024 triggering MailContent from
					// DLPCommonObjects=========

					/*
					 * Email em = new Email(); SMS sms = new SMS(); WhatsApp wh = new WhatsApp();
					 * String emailId = ""; Log.consoleLog(ifr, "Loantype query ==> " + Loantype);
					 * if (Loantype.equalsIgnoreCase("Pre-Approved Personal Loan")) { emailId =
					 * getCurrentEmailId(ifr, "PAPL", ""); } else { emailId = getCurrentEmailId(ifr,
					 * ProductType, ""); } String mobileNumber = getMobileNumber(ifr);
					 * em.sendEmail(ifr, PID, emailId, ApplicationNumber, "", "RETAIL", "2");
					 * sms.sendSMS(ifr, PID, mobileNumber, ApplicationNumber, "", "RETAIL", "2");
					 * wh.sendWhatsAppMsg(ifr, PID, mobileNumber, ApplicationNumber, "", "RETAIL",
					 * "2");
					 * 
					 * 
					 */
				} else {
					Log.consoleLog(ifr, "AppNumber is available");
					ifr.setValue("P_PAPL_REFERENCENO1", AppNumber);
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "ApplicationNumber Exception : " + e);

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  mImplApplicationNameRefrenceNum" + e);
		}
	}

	public void mImplApplicationNameRefrenceNum_ProductTeamTesting(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside mImplApplicationNameRefrenceNum_ProductTeamTesting : ");
		String applicationNumber = "";
		try {

			try {
				applicationNumber = getApplicationNumber(ifr, "Exist");
				Log.consoleLog(ifr, "applicationNumber from procedure.... " + applicationNumber);
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception in  mImplApplicationNameRefrenceNum_ProductTeamTesting" + e);
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  mImplApplicationNameRefrenceNum_ProductTeamTesting" + e);
		}
	}

	/*
	 * public void mImplApplicationNameRefrenceNum(IFormReference ifr) {
	 * Log.consoleLog(ifr, "Inside mImplApplicationNameRefrenceNum : "); String
	 * applicationNumber = null; try { String mobileData = ""; String PID =
	 * ifr.getObjGeneralData().getM_strProcessInstanceId(); String query =
	 * "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" + PID +
	 * "' "; Log.consoleLog(ifr, "mobileData query : " + query); List<List<String>>
	 * list = ifr.getDataFromDB(query); if (!list.isEmpty()) { mobileData =
	 * list.get(0).get(0); } String getPreApprLoanQuery =
	 * ConfProperty.getQueryScript("getPreApprLoanQuery").replaceAll("#mobile_no#",
	 * mobileData); Log.consoleLog(ifr, "loanAmount query : " +
	 * getPreApprLoanQuery); List<List<String>> loanAmount =
	 * ifr.getDataFromDB(getPreApprLoanQuery); String customerName = ""; if
	 * (!loanAmount.isEmpty()) { customerName = loanAmount.get(0).get(1);
	 * ifr.setValue("P_PAPL_CUSTOMERNAME1", customerName); } try { applicationNumber
	 * = getApplicationNumber(ifr, "Exist");
	 * 
	 * ifr.setValue("P_PAPL_REFERENCENO1", applicationNumber);
	 * 
	 * String AppNumUpQuery = "UPDATE LOS_WIREFERENCE_TABLE " +
	 * "SET APPLICATION_NO='" + applicationNumber + "' WHERE winame='" + PID + "'";
	 * Log.consoleLog(ifr, "AppNumUpQuery==> " + AppNumUpQuery);
	 * ifr.saveDataInDB(AppNumUpQuery);
	 * 
	 * String AppNumUpQuery1 = "UPDATE LOS_EXT_TABLE " + "SET APPLICATIONNUMBER='" +
	 * applicationNumber + "' WHERE " +
	 * "ITEMINDEX IN (SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE " +
	 * "PROCESSINSTANCEID='" + PID + "')"; Log.consoleLog(ifr, "AppNumUpQuery1==> "
	 * + AppNumUpQuery1); ifr.saveDataInDB(AppNumUpQuery1); } catch (Exception e) {
	 * Log.consoleLog(ifr, "Exception in  mImplApplicationNameRefrenceNum" + e); }
	 * 
	 * } catch (Exception e) { Log.consoleLog(ifr,
	 * "Exception in  mImplApplicationNameRefrenceNum" + e); } }
	 */
	public String setGetPortalStepName(IFormReference ifr, String stepName) {
		Log.consoleLog(ifr, "Inside StepName : " + stepName);
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String StepQuery = ConfProperty.getQueryScript("PSELECTStepQuery").replaceAll("#winame#", PID);
		List<List<String>> StepList = cf.mExecuteQuery(ifr, StepQuery, "navigationNextLoadLoanDetails Query:");
		String oldWorkstepName = "";
		if (!StepList.isEmpty()) {
			oldWorkstepName = StepList.get(0).get(0);
		}
		Log.consoleLog(ifr, "oldWorkstepName : " + oldWorkstepName);
		if (oldWorkstepName.equalsIgnoreCase("")) {
			oldWorkstepName = stepName;
			stepNameUpdate(ifr, stepName);
		} else {
			String getLoanType = getLoanType(ifr, "", "", "");
			String firstStageName = "";
			if (getLoanType.equalsIgnoreCase("Pre-Approved Personal Loan")) {
				firstStageName = "Avail Offer";
			} else if (getLoanType.equalsIgnoreCase("Loan Against Deposit")) {
				firstStageName = "Initial Data";
			} else if (getLoanType.equalsIgnoreCase("Canara Budget")) {
				firstStageName = "Loan Details";
			} else if (getLoanType.equalsIgnoreCase("Canara Pension")) {
				Log.consoleLog(ifr, "Canara Pension :: ");
				firstStageName = "Loan Details ";
			} else if (getLoanType.equalsIgnoreCase("VEHICLE LOAN")) {
				firstStageName = "Occupation Detail";
			} else if (getLoanType.equalsIgnoreCase("Home Loan")) {
				firstStageName = "Loan details Applicant";
			}
			if ((!(oldWorkstepName.equalsIgnoreCase(stepName))) && stepName.equalsIgnoreCase(firstStageName)) {
				Log.consoleLog(ifr, "satified:" + oldWorkstepName);
				return oldWorkstepName;
			} else {
				Log.consoleLog(ifr, "else");
				oldWorkstepName = stepName;
				stepNameUpdate(ifr, stepName);
			}
		}
		Log.consoleLog(ifr, "going:" + oldWorkstepName);
		return oldWorkstepName;
	}

	public String getMobileNumber(IFormReference ifr) {
		try {
			String mobileNumber = "";
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" + PID + "' ";
			List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
			if (!list.isEmpty()) {
				return list.get(0).get(0).length() == 10 ? "91" + list.get(0).get(0) : list.get(0).get(0);
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getMobileNumber::" + e);
			Log.errorLog(ifr, "Exception in getMobileNumber::" + e);
		}
		return "";
	}

	public String getMobileNumber(IFormReference ifr, String applicantType) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String query = "SELECT MOBILENUMBER FROM LOS_NL_BASIC_INFO where PID = '" + PID + "' and applicanttype='"
					+ applicantType + "' AND ROWNUM = 1 ORDER BY INSERTIONORDERID DESC ";

			List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
			if (list.size() > 0) {
				return list.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getCustomerIDCB::" + e);
			Log.errorLog(ifr, "Exception in getCustomerIDCB::" + e);
		}
		return "";
	}

	public String getWICustomerID(IFormReference ifr) {
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String query = "SELECT cifnumber from LOS_WIREFERENCE_TABLE where WINAME = '" + PID + "' ";
			List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get CIF Number:");
			if (!list.isEmpty()) {
				return list.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getMobileNumber::" + e);
			Log.errorLog(ifr, "Exception in getMobileNumber::" + e);
		}
		return "";
	}

	public String getSliderAmount(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#", PID);
		Log.consoleLog(ifr, "query for slider" + query);
		List<List<String>> list1 = cf.mExecuteQuery(ifr, query, "Query Slider:");
		Log.consoleLog(ifr, "list1" + list1);
		if (list1.size() > 0) {
			return list1.get(0).get(0) + "~" + list1.get(0).get(1);

		}
		return "";
	}

	public String getCustomerIDPAPL(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT CUSTOMERID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY where WINAME = '" + PID + "' ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
		if (list.size() > 0) {
			return list.get(0).get(0);
		}
		return "";
	}

	public String getCustomerIDForOtherProducts(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "select customerid from los_nl_basic_info where PID = '" + PID
				+ "' and applicanttype in ('CB','G','B') AND ROWNUM = 1 ORDER BY INSERTIONORDERID DESC ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
		if (list.size() > 0) {
			return list.get(0).get(0);
		}
		return "";
	}

	public String getCustomerIDCB(IFormReference ifr, String applicantType) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String query = "SELECT CUSTOMERID FROM LOS_TRN_CUSTOMERSUMMARY where WINAME = '" + PID + "'";
			List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
			if (list.size() > 0) {
				return list.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in getCustomerIDCB::" + e);
			Log.errorLog(ifr, "Exception in getCustomerIDCB::" + e);
		}
		return "";
	}

//    public String getCustomerIDCB(IFormReference ifr) {
//        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        try {
//            String query = "SELECT CUSTOMERID FROM LOS_NL_BASIC_INFO where PID = '" + PID + "' and applicanttype='B' ";
//            List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Customer ID:");
//            if (list.size() > 0) {
//                return list.get(0).get(0);
//            }
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception in getCustomerIDCB::" + e);
//            Log.errorLog(ifr, "Exception in getCustomerIDCB::" + e);
//        }
//        return "";
//    }
	// Added by Ishwarya on 02/01/2024
	public String getCustomeAccountNumber(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT LOAN_ACCOUNT_NO FROM LOS_T_IBPS_LOAN_DETAILS WHERE WINAME='" + PID + "' AND ROWNUM=1 ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get getCustomeAccountNumber ID:");
		if (list.size() > 0) {
			return list.get(0).get(0);
		}
		return "";
	}

	public String returnErrorThroughExecute(IFormReference ifr, String Message) {
		String objRes = "{\"showMessage\":" + cf.showMessage(ifr, "", "error", Message)
				+ ",\"NavigationNextClick\":\"false\"}";
		return objRes;
	}

	public String returnError(IFormReference ifr) {
		JSONObject message = new JSONObject();
		// message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical
		// glitch, Try after sometime!"));
		return "error" + "," + "Technical glitch, Try after sometime!";
	}

	public String returnErrorForStaff(IFormReference ifr, String response) {
		CommonFunctionality cf = new CommonFunctionality();
		JSONObject message = new JSONObject();
		message.put("showMessage", cf.showMessage(ifr, "", "error", response));
		return message.toString();
	}

	public String returnErrorAPIThroughExecute(IFormReference ifr) {
		String objRes = "{\"showMessage\":" + cf.showMessage(ifr, "", "error", "Technical glitch, Try after sometime!")
				+ ",\"NavigationNextClick\":\"false\"}";
		return objRes;
	}

	public String returnErrorcustmessage(IFormReference ifr, String messag) {
		JSONObject message = new JSONObject();
		message.put("showMessage", cf.showMessage(ifr, "", "error", messag));
		return "error" + "," + messag;
	}

	public String returnErrorHold(IFormReference ifr) {
		JSONObject message = new JSONObject();
		message.put("showMessage", cf.showMessage(ifr, "", "error", "Technical glitch, Try after sometime!"));
		message.put("eflag", "false");
		return "error" + "," + "Technical glitch, Try after sometime!";
	}

	public String getApplicationNumber(IFormReference ifr, String custType) {
		CreateApplicationNumber can = new CreateApplicationNumber();
		return can.getApplicationNumber(ifr, "RETAIL", "", custType);
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

	public BigDecimal mCheckBigDecimalValueDecimal(IFormReference ifr, String value) {
		// Log.consoleLog(ifr, "Inside mCheckBigDecimalValueDecimal:" + value);
		BigDecimal b = new BigDecimal("0.0");
		try {
			if (!(value.trim().equalsIgnoreCase(""))) {
				Log.consoleLog(ifr, "Inside mCheckBigDecimalValueDecimal convert:");
				BigDecimal bd = new BigDecimal(value).divide(new BigDecimal("1"), 2, RoundingMode.HALF_UP);
				return bd;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception inside mCheckBigDecimalValueDecimal:" + e);
			Log.errorLog(ifr, "Exception inside mCheckBigDecimalValueDecimal:" + e);
		}
		return b;
	}

	public String getConstantValue(IFormReference ifr, String constType, String constName) {
		String query = "SELECT CONSTVALUE FROM LOS_MST_CONSTANTS WHERE CONSTTYPE='" + constType + "' AND CONSTNAME='"
				+ constName + "'";
		Log.consoleLog(ifr, "query" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty() && result.get(0).get(0) != null) {
			Log.consoleLog(ifr, "result" + result.get(0).get(0));
			return result.get(0).get(0);
		}
		Log.consoleLog(ifr, "result" + result.get(0).get(0));
		return "0";
	}

	public String getParamValue(IFormReference ifr, String paramName, String paramKey) {
		String query = "SELECT SVALUE FROM LOS_MST_PARAMS WHERE SPARAM='" + paramName + "' AND SKEY='" + paramKey + "'";
		Log.consoleLog(ifr, "query" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0 && result.get(0).get(0) != null) {
			return result.get(0).get(0);
		}
		return "0";
	}

	public String getParamConfigSHL(IFormReference ifr, String paramType, String paramName) {
		// String query0 = "select PARAMVALUE from los_mst_configs where "
		// + "productcode='" + productCode + "' "
		// + "AND SUBPRODCODE='" + subProductCode + "' "
		// + "AND PARAMTYPE='" + paramType + "' "
		// + "AND PARAMNAME='" + paramName + "'";

		String query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS\n" + "WHERE PRODUCTCODE = 'SHL' " + "AND PARAMTYPE = '"
				+ paramType + "' " + "AND PARAMNAME = '" + paramName + "'";

		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0 && result.get(0).get(0) != null) {
			return result.get(0).get(0);
		}
		// return "0";
		return "";// Modified by Ahmed on 15-07-2024
	}

	public String getParamConfig(IFormReference ifr, String productCode, String subProductCode, String paramType,
			String paramName) {
		// String query0 = "select PARAMVALUE from los_mst_configs where "
		// + "productcode='" + productCode + "' "
		// + "AND SUBPRODCODE='" + subProductCode + "' "
		// + "AND PARAMTYPE='" + paramType + "' "
		// + "AND PARAMNAME='" + paramName + "'";

		String query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS\n" + "WHERE PRODUCTCODE = '" + productCode + "' "
				+ "AND SUBPRODCODE =NVL('" + subProductCode + "',SUBPRODCODE)\n" + "AND PARAMTYPE = '" + paramType
				+ "' " + "AND PARAMNAME = '" + paramName + "'";

		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0 && result.get(0).get(0) != null) {
			return result.get(0).get(0);
		}
		// return "0";
		return "";// Modified by Ahmed on 15-07-2024
	}

	public String getProcessingFee(IFormReference ifr, String SchemeId, String loanAmount, String whereCondition) {
		String query = "SELECT A.FeeType,A.FixedFee,A.FeePercentage,A.TaxPercentage,A.MINAMT,A.MAXAMT FROM "
				+ "LOS_M_FEE_CHARGES A INNER JOIN LOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode=B.FeeCode "
				+ "WHERE A.IsActive='Y' and B.SCHEMEID='" + SchemeId + "' " + whereCondition;
		Log.consoleLog(ifr, "Query fee :" + query);
		List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
		if (result.size() > 0) {
			BigDecimal totalCharge = new BigDecimal("0");
			for (int i = 0; i < result.size(); i++) {
				String FeeType = result.get(i).get(0);
				String FixedFee = result.get(i).get(1);
				String FeePercentage = result.get(i).get(2);
				String TaxPercentage = result.get(i).get(3);
				String MINAMT = result.get(i).get(4);
				String MAXAMT = result.get(i).get(5);
				BigDecimal FeeAmount = mGetFeesAmount(ifr, FeeType, FixedFee, FeePercentage, loanAmount);
				Log.consoleLog(ifr, "FeeAmount:" + FeeAmount);
				BigDecimal totalAmt = mCalculateTotal(ifr, MINAMT, MAXAMT, FeeAmount);
				Log.consoleLog(ifr, "totalAmt:" + totalAmt);
				BigDecimal FeeGstAmt = mCalculateFeeAmount(ifr, TaxPercentage, totalAmt);
				Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
				totalCharge = totalCharge.add(FeeGstAmt);
			}
			return String.valueOf(totalCharge);
		}
		return "0";
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

	public BigDecimal mCalculateFeeAmount(IFormReference ifr, String gst, BigDecimal NetAmt) {
		BigDecimal GSTAmt = new BigDecimal(gst.equalsIgnoreCase("") ? "0" : gst);
		Log.consoleLog(ifr, "GSTAmt:" + GSTAmt);
		BigDecimal Gsttotal = (NetAmt.multiply(GSTAmt)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		Log.consoleLog(ifr, "Gsttotal Amount:" + Gsttotal);
		BigDecimal total = NetAmt.add(Gsttotal);
		Log.consoleLog(ifr, "total Amount:" + total);
		return total;
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

	public String mGetRoi(IFormReference ifr, String crg) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		// String crg=mGetCRG(ifr);
		String schemeid = mGetSchemeID(ifr, processInstanceId);
		String queryroi = ConfProperty.getQueryScript("GETROIQUERY").replaceAll("#schemeid#", schemeid)
				.replaceAll("#crg#", crg);
		Log.consoleLog(ifr, "query:" + queryroi);
		List<List<String>> result = cf.mExecuteQuery(ifr, queryroi, "ROI SCHEME");
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetROICB(IFormReference ifr) {
		String loanROIQuery = "select Effectiveroi from los_nl_proposed_facility where pid='"
				+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
		List<List<String>> list2 = cf.mExecuteQuery(ifr, loanROIQuery, "loanROIQuery:");
		if (list2.size() > 0) {
			return list2.get(0).get(0);
		}
		return "";
	}

	public String mGetSchemeID(IFormReference ifr, String processInstanceId) {
//        String query = "select A.SchemeID from LOS_M_PRODUCT_RLOS A inner join LOS_EXT_TABLE B on "
//                + "A.SUBPRODUCTCODE=B.Coll_ProductType1 where B.PID='" + processInstanceId + "'";
		// String query = "select schemeid from los_m_product_rlos where productcode in
		// (select loan_type from SLOS_STAFF_TRN where winame='"+processInstanceId+"')";

		String query = "select schemeid from los_m_product_rlos where productcode in (select loan_type from SLOS_STAFF_TRN where winame='"
				+ processInstanceId + "') " + " and subproductcode ='STAFFLOAN' and ISACTIVE='Y'";
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetProductParams(IFormReference ifr) {
		String query = "select ProductCode,SUBPRODUCTCODE from LOS_M_PRODUCT_RLOS where SchemeID='"
				+ mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0) + "#" + result.get(0).get(1);
		}
		return "";
	}

	public String mGetProductCode(IFormReference ifr) {
		String query = "select ProductCode from LOS_M_PRODUCT_RLOS where SchemeID='"
				+ mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String getAmountForEligibilityCheck(IFormReference ifr, HashMap<String, String> loandata) {
		try {
			BigDecimal netSalPercentage = mCheckBigDecimalValue(ifr,
					getConstantValue(ifr, "LOANELIGIBILITY", "NETSALPERCENT"));
			BigDecimal netHomePercentage = mCheckBigDecimalValue(ifr,
					getConstantValue(ifr, "LOANELIGIBILITY", "NETHOMEPERENT"));
			BigDecimal netSalaryMultiple = mCheckBigDecimalValue(ifr,
					getConstantValue(ifr, "LOANELIGIBILITY", "NETSALMULTIPLE"));
			BigDecimal netHomeSal = mCheckBigDecimalValue(ifr, getConstantValue(ifr, "LOANELIGIBILITY", "NETHOMESAL"));

			BigDecimal ftNetsal = mCheckBigDecimalValue(ifr, loandata.get("netsal"));
			BigDecimal grossSalary = (ftNetsal.divide(netSalPercentage, 2, RoundingMode.HALF_UP))
					.multiply(new BigDecimal("100"));
			BigDecimal ftCibilOblig = mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
			BigDecimal netTakeHomeSalary = (grossSalary.multiply(netHomePercentage)).divide(new BigDecimal("100"));
			netTakeHomeSalary = netTakeHomeSalary.max(netHomeSal);
			BigDecimal netIncome = ftNetsal.subtract(ftCibilOblig).subtract(netTakeHomeSalary);
			BigDecimal ftTenure = mCheckBigDecimalValue(ifr, loandata.get("tenure"));
			BigDecimal ftRoi = mCheckBigDecimalValue(ifr, loandata.get("roi"));
			BigDecimal lacAmount = new BigDecimal(100000);
			BigDecimal emiperlc = calculateEMI(ifr, lacAmount, ftRoi, Integer.parseInt(String.valueOf(ftTenure)));
			BigDecimal loanAmount = (netIncome.divide(emiperlc, 2, RoundingMode.HALF_UP)).multiply(lacAmount);
			BigDecimal ftNetSalMul = ftNetsal.multiply(netSalaryMultiple);
			BigDecimal ftLoanMax = mCheckBigDecimalValue(ifr, loandata.get("loanmax"));
			BigDecimal ftLoanCap = mCheckBigDecimalValue(ifr, loandata.get("loancap"));
			BigDecimal ftLoanOffer = mCheckBigDecimalValue(ifr, loandata.get("loanoffer"));
			BigDecimal loanEvalAmt = loanAmount.min(ftNetSalMul).min(ftLoanMax).min(ftLoanCap).min(ftLoanOffer);
			return String.valueOf(loanEvalAmt);
		} catch (NumberFormatException e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return RLOS_Constants.ERROR;
	}

	public String getAmountForEligibilityCheckCB(IFormReference ifr, HashMap<String, String> loandata) {
		try {
			Log.consoleLog(ifr, "inside getAmountForEligibilityCheckCB:::::");
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String grosssalary = null;
			String deductionmonth = null;
			BigDecimal grossSalaryMultiple = mCheckBigDecimalValue(ifr,
					getConstantValue(ifr, "LOANELIGIBILITYCB", "GROSSSALMULTIPLE"));
			BigDecimal netsalary;
			BigDecimal ftTenure = mCheckBigDecimalValue(ifr, loandata.get("tenure"));
			BigDecimal ftRoi = mCheckBigDecimalValue(ifr, loandata.get("roi"));
			BigDecimal lacAmount = new BigDecimal(100000);

			String grosssalaryData_Query = "select grosssalary, deductionmonth from LOS_NL_OCCUPATION_INFO where F_KEY =(select F_KEY from los_nl_basic_info where PID ='"
					+ ProcessInsanceId + "' and applicanttype='B')";
			List<List<String>> list = cf.mExecuteQuery(ifr, grosssalaryData_Query, "grosssalaryData_Query:");
			BigDecimal emiperlc = calculatePMT(ifr, ftRoi, Integer.parseInt(String.valueOf(ftTenure)));
			// String grosssalaryData_Query = "select grosssalary, deductionmonth from
			// LOS_NL_OCCUPATION_INFO where F_KEY =(select F_KEY from los_nl_basic_info
			// where PID ='" + ProcessInsanceId + "' and applicanttype='B')";
			// List<List<String>> list = cf.mExecuteQuery(ifr, grosssalaryData_Query,
			// "grosssalaryData_Query:");
			deductionmonth = ifr.getValue("P_CB_OD_DeductionFromSalary").toString();
			grosssalary = ifr.getValue("P_CB_OD_GrossSalary").toString();
			/*
			 * commented by monesh >>>>>>> .r1567 if (list.size() > 0) { grosssalary
			 * =ifr.getValue("P_CB_OD_DeductionFromSalary").toString(); Log.consoleLog(ifr,
			 * "grosssalary ===> " + grosssalary); deductionmonth = list.get(0).get(1);
			 * Log.consoleLog(ifr, "deductionmonth ===> " + deductionmonth); }
			 */
			/*
			 * commented by monesh if (list.size() > 0) { grosssalary
			 * =ifr.getValue("P_CB_OD_DeductionFromSalary").toString(); Log.consoleLog(ifr,
			 * "grosssalary ===> " + grosssalary); deductionmonth = list.get(0).get(1);
			 * Log.consoleLog(ifr, "deductionmonth ===> " + deductionmonth); }
			 */
			/*
			 * commented by monesh if (list.size() > 0) { grosssalary
			 * =ifr.getValue("P_CB_OD_DeductionFromSalary").toString(); Log.consoleLog(ifr,
			 * "grosssalary ===> " + grosssalary); deductionmonth = list.get(0).get(1);
			 * Log.consoleLog(ifr, "deductionmonth ===> " + deductionmonth); }
			 */

			// String LOANAMOUNTPERPOLICY_Query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS
			// WHERE PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE
			// ='LOANELIGIBILITY' AND SUBPRODCODE ='STP-CB'";
			// String LOANAMOUNTPERPOLICY_Query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS
			// WHERE PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE
			// ='LOANELIGIBILITY' AND SUBPRODCODE ='STP-CB'";
			// String LOANAMOUNTPERPOLICY_Query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS
			// WHERE PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE
			// ='LOANELIGIBILITY' AND SUBPRODCODE ='STP-CB'";
			String LOANAMOUNTPERPOLICY = "";
			String LOANAMOUNTPERPOLICY_Query = ConfProperty.getQueryScript("LOANAMOUNTPERPOLICYMUTIPLE");
			List<List<String>> LoanAmtMutiplelist = cf.mExecuteQuery(ifr, LOANAMOUNTPERPOLICY_Query,
					"LOANAMOUNTPERPOLICY_Query:");
			if (!LoanAmtMutiplelist.isEmpty()) {
				LOANAMOUNTPERPOLICY = LoanAmtMutiplelist.get(0).get(0);
			}
			BigDecimal LOANAMOUNTPERPOLICYMULTIPLE = new BigDecimal(LOANAMOUNTPERPOLICY);
			Log.consoleLog(ifr, "LOANAMOUNTPERPOLICYMULTIPLE ===> " + LOANAMOUNTPERPOLICYMULTIPLE);

			BigDecimal deductionsalary = new BigDecimal(deductionmonth);
			BigDecimal grosssalaryip = new BigDecimal(grosssalary);
			BigDecimal cbCibilOblig = mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
			BigDecimal laAmount = new BigDecimal(10000);
			BigDecimal mulValue = new BigDecimal(4);
			// LOANAMOUNTPERPOLICYMUTIPLE = SELECT PARAMVALUE FROM LOS_MST_CONFIGS WHERE
			// PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE ='LOANELIGIBILITY' AND
			// SUBPRODCODE ='STP-CB'
			// String LOANAMOUNTPERPOLICY_Query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS
			// WHERE PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE
			// ='LOANELIGIBILITY' AND SUBPRODCODE ='STP-CB'";

			int comparisonResult = grosssalaryip.compareTo(laAmount);
			if (comparisonResult > 0) {
				netsalary = grosssalaryip.divide(mulValue, 2, RoundingMode.HALF_UP);
			} else {
				netsalary = laAmount.divide(mulValue, 2, RoundingMode.HALF_UP);
			}
			Log.consoleLog(ifr, "netsalary ===> " + netsalary);
			BigDecimal netIncome = grosssalaryip.subtract(deductionsalary).subtract(cbCibilOblig).subtract(netsalary);
			Log.consoleLog(ifr, "net_income===> " + netIncome);

			BigDecimal loanAmount = LOANAMOUNTPERPOLICYMULTIPLE.multiply(netIncome).divide(emiperlc, 2,
					RoundingMode.HALF_UP);
			Log.consoleLog(ifr, "loanAmount ===> " + loanAmount);
			BigDecimal prodspeccapping = mCheckBigDecimalValue(ifr, loandata.get("loancap"));
			BigDecimal ftGrossSalMul = grosssalaryip.multiply(grossSalaryMultiple);
			Log.consoleLog(ifr, "ftGrossSalMul===> " + ftGrossSalMul);
			BigDecimal inprincipleamount = loanAmount.min(ftGrossSalMul).min(prodspeccapping);
			Log.consoleLog(ifr, "inprincipleamount===> " + inprincipleamount);

			String prodCode_PL = "PL";
			String subProdCode = "STP-CB";
			String loanoffer = "0";
			HashMap<String, String> loandata_FinalChk = new HashMap<String, String>();
			loandata_FinalChk.put("roi", String.valueOf(ftRoi));
			loandata_FinalChk.put("tenure", String.valueOf(ftTenure));
			loandata_FinalChk.put("gross", grosssalary);
			loandata_FinalChk.put("cibiloblig", cbCibilOblig.toString());
			loandata_FinalChk.put("loanoffer", loanoffer);
			loandata_FinalChk.put("deduction", deductionmonth);
			loandata_FinalChk.put("loancap", prodspeccapping.toString());
			String finaleligibility = null;
			try {
				finaleligibility = LoanEC.getAmountForEligibilityCheck(ifr, prodCode_PL, subProdCode,
						loandata_FinalChk);
				Log.consoleLog(ifr,
						"final eligibility from getAmountForEligibilityCheck Budget::==>" + finaleligibility);
			} catch (Exception ex) {
				Log.consoleLog(ifr, "Exception:" + ex);
			}
			return String.valueOf(finaleligibility);
			// return String.valueOf(inprincipleamount);

		} catch (NumberFormatException e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return RLOS_Constants.ERROR;
	}

	public BigDecimal calculateEMI(IFormReference ifr, BigDecimal loanApplied, BigDecimal roi, int tenure) {
		Log.consoleLog(ifr, "In calculateEMI");
		Log.consoleLog(ifr, "loanApplied : " + loanApplied + " roi : " + roi + " tenure : " + tenure);
		try {
			BigDecimal roiBy1200 = roi.divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP); // R
			BigDecimal numerator1 = loanApplied.multiply(roiBy1200); // P*R
			BigDecimal numerator2 = ((BigDecimal.ONE.add(roiBy1200)).pow(tenure)).setScale(20, RoundingMode.HALF_UP); // (1+R)^N
			BigDecimal numerator = numerator1.multiply(numerator2); // P*R*(1+R)^N
			BigDecimal denominator = BigDecimal.ONE.subtract(numerator2); // ((1+R)^N)-1
			BigDecimal result = (numerator.divide(denominator, 2, RoundingMode.HALF_UP)).setScale(4,
					RoundingMode.HALF_UP); // P * R * (1+R)^N / [((1+R)^N)-1]
			return result.negate();
		} catch (Exception e) {
			Log.errorLog(ifr, e.getMessage());
			return BigDecimal.ZERO;
		}
	}

	public String getCustomerName(IFormReference ifr) {
		String mobileNumber = getMobileNumber(ifr);
		String getPreApprLoanQuery = ConfProperty.getQueryScript("getPreApprLoanQuery").replaceAll("#mobile_no#",
				mobileNumber);
		Log.consoleLog(ifr, "loanAmount query : " + getPreApprLoanQuery);
		List<List<String>> customerNameList = ifr.getDataFromDB(getPreApprLoanQuery);
		String customerName = "";
		if (!customerNameList.isEmpty()) {
			customerName = customerNameList.get(0).get(1);
		}
		return customerName;
	}

	// Method modified by Ahmed on 29-03-2024 for ROIType chosing
	public String getROI(IFormReference ifr, String SchemeId, String roiType) {

		// String query = "select A.ROITYPE,A.BaseRate,A.SPREAD,A.MINROI,A.MAXROI from
		// LOS_M_ROI A inner join "
		// + "LOS_M_ROI_Scheme B on A.ROIID=B.ROIID where B.SCHEMEID='" + SchemeId +
		// "'";
		String query = "select A.ROITYPE,A.BaseRate,A.SPREAD,A.MINROI,A.MAXROI from LOS_M_ROI A inner join "
				+ "LOS_M_ROI_Scheme B on A.ROIID=B.ROIID " + "where B.SCHEMEID='" + SchemeId + "' " + "AND A.ROITYPE='"
				+ roiType + "'";

		List<List<String>> result = cf.mExecuteQuery(ifr, query, "getROI:");
		if (result.size() > 0) {
			String ROITYPE = result.get(0).get(0);
			BigDecimal BaseRate = mCheckBigDecimalValue(ifr, result.get(0).get(1));
			BigDecimal SPREAD = mCheckBigDecimalValue(ifr, result.get(0).get(2));
			String MINROI = result.get(0).get(3);
			String MAXROI = result.get(0).get(4);
			BigDecimal CalculateROI = BaseRate.add(SPREAD);
			Log.consoleLog(ifr, "CalculateROI:" + CalculateROI);
			BigDecimal totalROI = mCalculateTotal(ifr, MINROI, MAXROI, CalculateROI);
			Log.consoleLog(ifr, "totalROI:" + totalROI);
			return String.valueOf(totalROI);
		}
		return "0";
	}

	public String openWebAPIDocument(IFormReference ifr, String documentURLType) {
		try {
			String WIOPENURL = ConfProperty.getCommonPropertyValue(documentURLType);
			Log.consoleLog(ifr, "WIOPENURL:" + WIOPENURL);
			String cabinet = ifr.getObjGeneralData().getM_strEngineName();
			String strSessionid = ifr.getObjGeneralData().getM_strDMSSessionId();
			WIOPENURL = WIOPENURL.replaceAll("#cabinet#", cabinet).replaceAll("#strSessionid#", strSessionid);
			Log.consoleLog(ifr, "WIOPENURL:" + WIOPENURL);
			JSONObject message = new JSONObject();
			message.put("openWindow", cf.openWindow(WIOPENURL));
			return message.toString();
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return "";
	}

	public String getDocID(IFormReference ifr, String name, String whereCondition) {
		try {
			String query = "select A.documentindex from pdbdocument A inner join PDBDOCUMENTCONTENT B on A.documentindex=B.documentindex inner join pdbfolder C on "
					+ "B.parentfolderindex=C.folderindex where A.name='" + name + "'" + whereCondition;
			List<List<String>> result = cf.mExecuteQuery(ifr, query, "DocumentIndex Query:");
			if (result.size() > 0) {
				return result.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return "";
	}

	public String getCurrentAPIDate(IFormReference ifr) {
		Date d = new Date();
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		String APIDate = sd.format(d);
		Log.consoleLog(ifr, "APIDate:" + APIDate);
		return APIDate;
	}

	public BigDecimal mCalculateGSTAmount(IFormReference ifr, String gst, BigDecimal NetAmt) {
		BigDecimal GSTAmt = new BigDecimal(gst.equalsIgnoreCase("") ? "0" : gst);
		Log.consoleLog(ifr, "GSTAmt:" + GSTAmt);
		BigDecimal Gsttotal = (NetAmt.multiply(GSTAmt)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		Log.consoleLog(ifr, "Gsttotal Amount:" + Gsttotal);
		return Gsttotal;
	}

	public JSONArray getTotalCharges(IFormReference ifr, String SchemeId, String loanAmount, String Statecode,
			String loantenure, String loanAccountNumber, String TBranch, String calculatedStampCharges) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONArray jsonArr = new JSONArray();
		JSONArray jsonArr1 = new JSONArray();
		BigDecimal total = new BigDecimal("0");
		BigDecimal stampAmtCHarges = new BigDecimal("0");
		BigDecimal FeeGstAmt = new BigDecimal("0");
		String query = "SELECT A.FeeType,A.FixedFee,A.FeePercentage,A.TaxPercentage,A.MINAMT,A.MAXAMT,A.GLCODE,A.GLDESC,"
				+ "A.MAXTENURE,A.MINTENURE "
				+ "FROM SLOS_M_FEE_CHARGES A INNER JOIN SLOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode=B.FeeCode "
				+ "WHERE A.IsActive='Y' and B.SCHEMEID='" + SchemeId + "' and (A.MINLOANAMOUNT <= to_number('"
				+ loanAmount + "') AND to_number('" + loanAmount + "') <= A.MAXLOANAMOUNT) and "
				+ "(A.MINTENURE <= to_number('" + loantenure + "') AND to_number('" + loantenure + "') <= A.MAXTENURE)";
		/*
		 * String query =
		 * "SELECT A.FeeType,A.FixedFee,A.FeePercentage,A.TaxPercentage,A.MINAMT,A.MAXAMT,A.GLCODE,A.GLDESC FROM "
		 * +
		 * "SLOS_M_FEE_CHARGES A INNER JOIN LOS_M_FEE_CHARGES_SCHEME B ON A.FeeCode=B.FeeCode "
		 * + "WHERE A.IsActive='Y' and B.SCHEMEID='" + SchemeId +
		 * "' and (A.MINLOANAMOUNT <= to_number('" + loanAmount + "') AND to_number('" +
		 * loanAmount + "')  <= A.MAXLOANAMOUNT) and " + "(A.MINTENURE <= to_number('" +
		 * loantenure + "') AND " + "A.MAXTENURE<= to_number('" + loantenure+"'))";
		 */
		Log.consoleLog(ifr, "Fund Transfer query====>:" + query);
		List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
		BigDecimal stampDutyAmnt = BigDecimal.ZERO;
		if (result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				String FeeType = result.get(i).get(0);
				Log.consoleLog(ifr, "FeeType====>:" + FeeType);
				String FixedFee = result.get(i).get(1);
				Log.consoleLog(ifr, "FixedFee====>:" + FixedFee);
				String FeePercentage = result.get(i).get(2);
				Log.consoleLog(ifr, "FeePercentage====>:" + FeePercentage);
				String TaxPercentage = result.get(i).get(3);
				Log.consoleLog(ifr, "TaxPercentage====>:" + FeePercentage);
				String MINAMT = result.get(i).get(4);
				String MAXAMT = result.get(i).get(5);
				String GLCODE = result.get(i).get(6);
				String GLDESC = result.get(i).get(7);
				String ADDITIONAL_STAMP_CHARGES = "0";
				String STAMP_CESS_PERCENT = "0";
				if (FeeType.equalsIgnoreCase("CHRGSTATE")) {
//					String QueryForStamp = "SELECT STAMPCHARGES  from SLOS_TRN_LOANSUMMARY WHERE WINAME='"
//							+ processInstanceId + "'";
//					List<List<String>> Output3ForStamp = cf.mExecuteQuery(ifr, QueryForStamp, QueryForStamp);
//
//					if (!Output3ForStamp.isEmpty() && Output3ForStamp.get(0).get(0) != null && !Output3ForStamp.get(0).get(0).trim().isEmpty()) {
//						stampAmtCHarges = new BigDecimal(Output3ForStamp.get(0).get(0).trim());
//					}
					if (calculatedStampCharges != null && !calculatedStampCharges.trim().isEmpty()) {
						stampAmtCHarges = new BigDecimal(calculatedStampCharges.trim());
					}
				}
				if (!FeeType.equalsIgnoreCase("CHRGSTATE")) {
					BigDecimal FeeAmount = mGetFeesAmount(ifr, FeeType, FixedFee, FeePercentage, loanAmount);
					Log.consoleLog(ifr, "FeeAmount:" + FeeAmount);
//				if (FeeType.equalsIgnoreCase("CHRGSTATE")) {
//					//FeeAmount = FeeAmount.add(mCheckBigDecimalValue(ifr, ADDITIONAL_STAMP_CHARGES));
//				}
					BigDecimal totalAmt = mCalculateTotal(ifr, MINAMT, MAXAMT, FeeAmount);
					Log.consoleLog(ifr, "totalAmt:" + totalAmt);
					FeeGstAmt = mCalculateFeeAmount(ifr, TaxPercentage, totalAmt);
					Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
				}
				else
				{
					FeeGstAmt = stampAmtCHarges;
					Log.consoleLog(ifr, "FeeGstAmt:" + FeeGstAmt);
				}
				if (i != 2) {
					total = total.add(FeeGstAmt);
				} else {
					total = total.add(FeeGstAmt.setScale(0, RoundingMode.CEILING));
				}
				if (!(String.valueOf(FeeGstAmt).equalsIgnoreCase("0.00"))) {
					JSONObject obj = new JSONObject();
					obj.put("AccountNumber", GLCODE);
					obj.put("AccountType", "2");
					Log.consoleLog(ifr, "GLDESC: for production modified on 01-02-2024" + GLDESC);

					String accountBranch;
					if (i < result.size() - 1) {
						obj.put("AccountBranch", TBranch);
						 //accountBranch = TBranch;
					} else {
						obj.put("AccountBranch", "00004");
						 //accountBranch = "00004";
					}
					//obj.put("AccountBranch", accountBranch);
					
					//String stampDutyAmnt="";
//					String QueryForStamp = "SELECT STAMPCHARGES  from SLOS_TRN_LOANSUMMARY WHERE WINAME='"
//							+ processInstanceId + "'";
//					List<List<String>> Output3ForStamp = cf.mExecuteQuery(ifr, QueryForStamp, QueryForStamp);
//
//					if (!Output3ForStamp.isEmpty() && Output3ForStamp.get(0).get(0) != null && !Output3ForStamp.get(0).get(0).trim().isEmpty()) {
//						FeeGstAmt = new BigDecimal(Output3ForStamp.get(0).get(0).trim());
//					}
//					
					obj.put("DrCrFlag", "C");
					obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId() + " " + GLDESC);
//					if ("00004".equals(accountBranch)) {
//						obj.put("TransactionAmount", FeeGstAmt);
//					} 
//					else 
					if (GLDESC.contains("STAMP")) {
						obj.put("TransactionAmount", FeeGstAmt.setScale(0, RoundingMode.CEILING));
					} else {
						obj.put("TransactionAmount", FeeGstAmt);
					}
					// obj.put("TransactionAmount", String.valueOf(FeeGstAmt));
					Log.consoleLog(ifr, "TransactionAmount==>:" + String.valueOf(FeeGstAmt));
					obj.put("TransactionCurrency", "INR");
					obj.put("chequeNumber", "");
					obj.put("chequeDate", "");
					obj.put("forcePost", "N");
					obj.put("cashTransfer", "Y");
					jsonArr.add(obj);
				}
			}
			JSONObject obj = new JSONObject();
			//total = total.add(stampDutyAmnt);
			obj.put("AccountNumber", loanAccountNumber);
			obj.put("AccountType", "1");
			obj.put("AccountBranch", TBranch);
			obj.put("DrCrFlag", "D");
			obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId());
			obj.put("TransactionAmount", String.valueOf(total));
			obj.put("TransactionCurrency", "INR");
			obj.put("chequeNumber", "");
			obj.put("chequeDate", "");
			obj.put("forcePost", "Y");
			obj.put("cashTransfer", "Y");
			jsonArr1.add(obj);
			for (int i = 0; i < jsonArr.size(); i++) {
				obj = (JSONObject) jsonArr.get(i);
				jsonArr1.add(obj);
			}
		}
		return jsonArr1;
	}

	private void getStampjSon(IFormReference ifr, String TBranch, JSONArray jsonArr, List<List<String>> result, int i,
			String GLCODE, String GLDESC, BigDecimal FeeGstAmt) {
		JSONObject obj = new JSONObject();
		obj.put("AccountNumber", GLCODE);
		obj.put("AccountType", "2");
		Log.consoleLog(ifr, "GLDESC: for production modified on 01-02-2024" + GLDESC);

		if (i < result.size() - 1) {
			obj.put("AccountBranch", TBranch);
		} else {
			obj.put("AccountBranch", "00004");
		}

		obj.put("DrCrFlag", "C");
		obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId() + " " + GLDESC);
		obj.put("TransactionAmount", String.valueOf(FeeGstAmt));
		Log.consoleLog(ifr, "TransactionAmount==>:" + String.valueOf(FeeGstAmt));
		obj.put("TransactionCurrency", "INR");
		obj.put("chequeNumber", "");
		obj.put("chequeDate", "");
		obj.put("forcePost", "N");
		obj.put("cashTransfer", "Y");
		jsonArr.add(obj);
	}

	// =====================================Mayank Code
	// End=====================================//
	// Modified by Ahmed on 24-06-2024 for getting SalaryAccountNumber
	public String getSalaryAccountNo(IFormReference ifr, String journeyType) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String mobileNumber = getMobileNumber(ifr);
		String querySalAcccNoData = "";

		if (journeyType.equalsIgnoreCase("PAPL")) {
			querySalAcccNoData = "select salaryacc_no from LOS_MST_PAPL where mobile_no ='" + mobileNumber + "'";
		} else {
			// querySalAcccNoData = "SELECT SELECTSALARYACCOUNT FROM LOS_NL_BASIC_INFO WHERE
			// PID = '" + processInstanceId + "' AND APPLICANTTYPE ='B'";
			querySalAcccNoData = "select ACCTNUMBER,BRANCHCODE from LOS_NL_CASA_ASSET_VAL where pid='"
					+ processInstanceId + "'";

		}

		// String accNoData = "select salaryacc_no from LOS_MST_PAPL where mobile_no ='"
		// + MOBILENUMBER + "'";
		// List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, accNoData,
		// "accNoData:");
		List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, querySalAcccNoData, "accNoData:");
		if (!loanAccNo.isEmpty()) {
			return loanAccNo.get(0).get(0);
		}
		return "";
	}

	public String getNESLdocIndex(IFormReference ifr) {
		Log.consoleLog(ifr, "#getNESLdocIndex..");
		String Query = "SELECT DOCUMENTINDEX FROM PDBDOCUMENT WHERE " + "DOCUMENTINDEX IN (\n"
				+ "SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT "
				+ "WHERE PARENTFOLDERINDEX IN (SELECT FOLDERINDEX FROM PDBFOLDER \n" + "WHERE NAME='"
				+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "')\n" + ") AND NAME LIKE '%NESL%'";
		Log.consoleLog(ifr, "#Query===>" + Query);
		List Result = ifr.getDataFromDB(Query);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		String DocumentIndex = "";
		if (Result.size() > 0) {
			DocumentIndex = Result.get(0).toString();
		}
		Log.consoleLog(ifr, "#DocumentIndex===>" + DocumentIndex);
		return DocumentIndex;
		// throw new UnsupportedOperationException("Not supported yet."); // Generated
		// from
		// nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public String downloadTestDocumentFromOD(IFormReference ifr, String sReferenceKey) {

		Log.consoleLog(ifr, "#downloadTestDocumentFromOD===>");
		try {

			JSONObject json = null;
			JSONParser jsonparser = new JSONParser();
			JSONObject jobj = new JSONObject();

			// String url = "";
			String content, name, ext, response, status;

			String url = getConstantValue(ifr, "ODDOWNLOAD", "URL");
			Log.consoleLog(ifr, "#url===>" + url);

			String docindexQuery = "SELECT DOCUMENTINDEX FROM PDBDOCUMENT WHERE " + "DOCUMENTINDEX IN (\n"
					+ "SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT "
					+ "WHERE PARENTFOLDERINDEX IN (SELECT FOLDERINDEX FROM PDBFOLDER \n" + "WHERE NAME='"
					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "')\n" + ")  ";
			Log.consoleLog(ifr, "#docindexQuery===>" + docindexQuery);
			List<List<String>> Result = ifr.getDataFromDB(docindexQuery);
			Log.consoleLog(ifr, "#Result===>" + Result.toString());
			String DocumentIndex = "";
			if (Result.size() > 0) {
				DocumentIndex = Result.get(0).get(0);
			}

			// Log.consoleLog(ifr, "jsonin :- " + jsonin);
			Log.consoleLog(ifr, "#DocumentIndex===>" + DocumentIndex);
			DocumentIndex = DocumentIndex.replace("[", "").replace("]", "");

			// jsonObject.put("docIndex", DocumentIndex);
			String jsonin = "{\n" + "    \"NGOGetDocumentBDO\": {\n" + "        \"cabinetName\": \""
					+ ifr.getCabinetName() + "\",\n" + "        \"siteId\": \"1\",\n" + "        \"volumeId\": \"1\",\n"
					+ "        \"userName\": \"\",\n" + "        \"userPassword\": \"\",\n" + "        \"userDBId\": \""
					+ ifr.getObjGeneralData().getM_strDMSSessionId() + "\",\n" + "        \"locale\": \"en_US\",\n"
					+ "        \"passAlgoType\": \"\",\n" + "        \"encrFlag\": \"\",\n" + "        \"docIndex\": \""
					+ DocumentIndex + "\"\n" + "    }\n" + "}";

			JSONObject jsonObject = (JSONObject) jsonparser.parse(jsonin);

			Log.consoleLog(ifr, "jsonObject :- " + jsonObject.toString());

			response = getDocumentInBase64Formate(ifr, jsonObject, url, sReferenceKey);
			Log.consoleLog(ifr, "OD get Document response " + response);

			APICommonMethods cm = new APICommonMethods();
			JSONObject returnrespone = new JSONObject();
			returnrespone = cm.getDownloadParams(ifr, response);
			Log.consoleLog(ifr, "returnrespone==>" + returnrespone.toString());

			return returnrespone.toString();
			/*
			 * json = (JSONObject) new JSONParser().parse(response); Log.consoleLog(ifr,
			 * "jobj " + jobj.toString());
			 * 
			 * json = (JSONObject) json.get("NGOGetDocumentBDOResponse"); content =
			 * json.get("docContent").toString(); name =
			 * json.get("documentName").toString(); ext =
			 * json.get("createdByAppName").toString(); status =
			 * json.get("statusCode").toString(); Log.consoleLog(ifr,
			 * "NGOGetDocumentBDOResponse:- " + json); jobj.put("docContent", content);
			 * jobj.put("documentName", name); jobj.put("createdByAppName", ext);
			 * jobj.put("statusCode", status);
			 */
		} catch (Exception e) {
			Log.consoleLog(ifr, "download doc exception:- " + e.getMessage());
			Log.consoleLog(ifr, "download doc exception:- " + e.getLocalizedMessage());

		}
		return null;
	}

	public static String getDocumentInBase64Formate(IFormReference ifr, JSONObject jsonObject, String downloadURL,
			String sReferenceKey) {
		HttpURLConnection httpConn = null;
		URLConnection connection = null;
		OutputStream out = null;
		BufferedReader in = null;
		String responseString;
		String responseJSon = "";
		try {
			URL url = new URL(downloadURL);
			connection = url.openConnection();
			httpConn = (HttpURLConnection) connection;

			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			out = httpConn.getOutputStream();
			out.write(jsonObject.toJSONString().getBytes());
			Log.consoleLog(ifr, "response received");
			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Log.consoleLog(ifr, "response received HTTP_OK");

				in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
				while ((responseString = in.readLine()) != null) {
					responseJSon = responseJSon + responseString;
				}

			} else {
				in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "UTF-8"));
				while ((responseString = in.readLine()) != null) {
					responseJSon = responseJSon + responseString;
				}

				responseJSon = "{\"statusCode\":\"500\"}";
			}
			Log.consoleLog(ifr, "responseJSon" + responseJSon);

		} catch (IOException e) {
			Log.consoleLog(ifr, "download doc exception:- " + e.getMessage());
			Log.consoleLog(ifr, "download doc exception:- " + e.getLocalizedMessage());

		}

		return responseJSon;

	}

	public String setPortalDataFinalEligibiltyCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside setPortalDataFinalEligibiltyCB ");
		// String LoanAmount = "";
		// String tenure = "";
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, PID);
			String LoanAmount = ifr.getValue("P_CB_FE_LOAN_AMOUNT").toString();
			Log.consoleLog(ifr, "Loan amount is " + LoanAmount);
			String tenure = ifr.getValue("P_CB_FE_TENURE").toString();
			Log.consoleLog(ifr, "Tenure is" + tenure);
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

			Log.consoleLog(ifr, "PID .." + ProcessInsanceId);

			String roi1 = ifr.getValue("P_CB_FE_RATE_OF_INTEREST").toString();
			String ROI[] = roi1.split("%");
			String roi = ROI[0];
			Log.consoleLog(ifr, "ROI is " + roi);

			String emi = ifr.getValue("P_CB_FE_EMI").toString();
			String EMI[] = emi.split("₹");
			emi = EMI[1];
			Log.consoleLog(ifr, "EMI is " + emi);
			Log.consoleLog(ifr, "emi :" + emi);
			String processingFee1 = ifr.getValue("P_CB_FE_PROCEESING_FEES").toString();
			String ProcessingFee[] = processingFee1.split("₹");
			processingFee1 = ProcessingFee[1];
			long cicFeeRound = Math.round(Double.parseDouble(processingFee1));
			String Key = Fkey(ifr, "B");

			String Acc = "select acctnumber from LOS_NL_CASA_ASSET_VAL where FKEY='" + Key + "' and rownum=1";
			List<List<String>> straccy = ifr.getDataFromDB(Acc);
			String stracc = straccy.get(0).get(0);
			String CICFee = ifr.getValue("P_CB_FE_CIC_FEES").toString();
			String CICFee1[] = CICFee.split("₹");
			CICFee = CICFee1[1];
			// String processingFee = ProFee[1];

			Log.consoleLog(ifr, "CICFee..." + CICFee);
			long processingFeeRound = Math.round(Double.parseDouble(CICFee));
			String signStamp = ifr.getValue("P_CB_FE_E_SIGN_STAMP").toString();
			String SignStamp[] = signStamp.split("₹");
			signStamp = SignStamp[1];

			Log.consoleLog(ifr, "signStamp..." + signStamp);
			long signStampRound = Math.round(Double.parseDouble(signStamp));
			Log.consoleLog(ifr, "schemeID::::CICCharges::::" + CICFee + "esignAndStamp::::" + signStamp);
			/*
			 * String accNoData =
			 * "SELECT ACCOUNTID FROM LOS_NL_BASIC_INFO WHERE CUSTOMERFLAG = 'Y' AND PID ='"
			 * + ProcessInsanceId + "'"; Log.consoleLog(ifr, "accNoData query : " +
			 * accNoData); List<List<String>> loanAccNo = ifr.getDataFromDB(accNoData);
			 */
			String accNo = stracc;
			Log.consoleLog(ifr, "accNo.." + accNo);

			String Query2 = "INSERT INTO los_trn_finaleligibility(WINAME,LOANAMOUNT,TENURE,RATEOFINTEREST,EMI,PROCESSINGFEE,SB_ACCOUNTNO,RP_ACCOUNTNO) VALUEs ('"
					+ ProcessInsanceId + "','" + LoanAmount + "','" + tenure + "','" + roi + "','" + emi + "','"
					+ processingFeeRound + "','" + accNo + "','" + accNo + "')";
			Log.consoleLog(ifr, Query2);

			cf.mExecuteQuery(ifr, Query2, "Insert Into  los_trn_finaleligibility ");
			// String Query1
			// String
			// String CICFee = ifr.getValue("P_CB_FE_CIC_FEES").toString();
			// String signStamp = ifr.getValue("P_CB_FE_E_SIGN_STAMP").toString();
			String FintecCharges = ifr.getValue("P_CB_FE_FINTECT_CHARGES").toString();
			String Query = "UPDATE los_nl_proposed_facility set TENURE='" + tenure + "',PROCESSINGFEES='"
					+ processingFeeRound + "',RATEOFINTEREST='" + roi + "',REQLOANAMT='" + LoanAmount + "',EMI='" + emi
					+ "',CICFEES='" + CICFee + "',FINTECHCHARGES='" + FintecCharges + "',ESIGN='" + signStamp
					+ "' where PID='" + PID + "'";
			Log.consoleLog(ifr, "Query :" + Query);
			cf.mExecuteQuery(ifr, Query, "Update los_nl_proposed_facility ");
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception .." + e);
		}
		return "";
	}

	public String setPortalDataReceiveMoneyCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside setPortalDataReceiveMoneyCB ");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, PID);
			String LoanAmount = ifr.getValue("P_CB_RM_LOANAMOUNT").toString();
			String tenure = ifr.getValue("P_CB_RM_TENURE").toString();
			String roi1 = ifr.getValue("P_CB_FE_RATE_OF_INTEREST").toString();
			String ROI[] = roi1.split("%");
			String roi = ROI[0];
			String emi1 = ifr.getValue("P_CB_RM_EMI").toString();
			String EMI[] = emi1.split(" ");
			String emi = EMI[1];
			Log.consoleLog(ifr, "emi :" + emi);
			String processingFee1 = ifr.getValue("P_CB_RM_PROCESSINGFEE").toString();
			String ProFee[] = processingFee1.split(" ");
			String processingFee = ProFee[1];
			Log.consoleLog(ifr, "processingFee :" + processingFee);
			String CICFee = ifr.getValue("P_CB_RM_CICFEES").toString();
			String signStamp = ifr.getValue("P_CB_RM_SIGN_STAMP").toString();
			String FintecCharges = ifr.getValue("P_CB_RM_FINTECH_CHARGES").toString();
			String SbAccNo = ifr.getValue("P_CB_SBACCOUNTNO").toString();
			String repaymentAccNo = ifr.getValue("P_CB_REPAYMENTNO").toString();
			String NetDisbrusmentAmnt = ifr.getValue("P_CB_NETDISBURSEMENTNO").toString();
			String Query = "UPDATE los_nl_proposed_facility set TENURE='" + tenure + "',PROCESSINGFEES='"
					+ processingFee + "'," + "RATEOFINTEREST='" + roi + "',REQLOANAMT='" + LoanAmount + "',EMI='" + emi
					+ "',CICFEES='" + CICFee + "'," + "FINTECHCHARGES='" + FintecCharges + "',ESIGN='" + signStamp
					+ "',SBACCOUNTNO='" + SbAccNo + "',REPAYMENTACCOUNTNO='" + repaymentAccNo + "',NETDISBRUSMENTAMT='"
					+ NetDisbrusmentAmnt + "' where PID='" + PID + "'";
			Log.consoleLog(ifr, "Query :" + Query);
			ifr.saveDataInDB(Query);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception .." + e);
		}
		return "";
	}

	/*
	 * public String mAccSetSliderValueFinalEligbilityCB(IFormReference ifr, String
	 * control, String event, String value) {
	 * 
	 * Log.consoleLog(ifr, "inside mAccSetSliderValueFinalEligibilityCB : ");
	 * 
	 * Log.consoleLog(ifr, "value : " + value);
	 * 
	 * try { String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
	 * 
	 * String selectquery =
	 * ConfProperty.getQueryScript("PSELECTStepQuery").replaceAll("#winame#", PID);
	 * 
	 * List<List<String>> StepList = cf.mExecuteQuery(ifr, selectquery, " Query:");
	 * 
	 * String loanType = getLoanType(ifr, "", "", "");
	 * 
	 * Log.consoleLog(ifr, "loanType : " + loanType);
	 * 
	 * String currentStep = "";
	 * 
	 * if (StepList.size() > 0) {
	 * 
	 * currentStep = StepList.get(0).get(0);
	 * 
	 * }
	 * 
	 * String splitValue[] = value.split(",");
	 * 
	 * String loanAmount = splitValue[0];
	 * 
	 * Log.consoleLog(ifr, "loanAmount : " + loanAmount);
	 * 
	 * String tenure = splitValue[1];
	 * 
	 * Log.consoleLog(ifr, "tenure : " + tenure);
	 * 
	 * Log.consoleLog(ifr, "PID : " + PID);
	 * 
	 * String checkbox1 = ifr.getValue("FinalEligibility_CB_checkbox2").toString();
	 * String checkbox2 = ifr.getValue("FinalEligibility_CB_checkbox3").toString();
	 * if (checkbox1.equalsIgnoreCase("true") &&
	 * (checkbox2.equalsIgnoreCase("true"))) { String queryupdate =
	 * ConfProperty.getQueryScript("PORTALUPDATELIDERVALUEFECB").replaceAll("#PID#",
	 * PID).replaceAll("#loanAmount#", loanAmount).replaceAll("#tenure#", tenure);
	 * Log.consoleLog(ifr, "queryupdate is .." + queryupdate);
	 * ifr.saveDataInDB(queryupdate); } else { return returnErrorThroughExecute(ifr,
	 * "Kindly Select Both Disclaimer and Consent!"); } } catch (Exception e) {
	 * 
	 * Log.consoleLog(ifr,
	 * " Exception in mAccSetSliderValueFinalEligibilityCB method : " + e);
	 * 
	 * } return ""; }
	 */
	public String Fkey(IFormReference ifr, String Applicanttype) {

		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Fquery = ConfProperty.getQueryScript("FKEYSELECTQUERY").replaceAll("#PID#", PID)
				.replaceAll("#Applicanttype#", Applicanttype);

		Log.consoleLog(ifr, "FKEYQUERY :::" + Fquery);
		List<List<String>> strgey = ifr.getDataFromDB(Fquery);
		String strfkey = strgey.get(0).get(0);
		Log.consoleLog(ifr, "FKEYQUERY Output :::" + strfkey);

		return strfkey;
	}

	public String getCurrentEmailId(IFormReference ifr, String productType, String data) {
		try {

			String Query = "";
			String EmailId = "";

			Query = "SELECT EMAILID FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY " + "WHERE WINAME='"
					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";

			Log.consoleLog(ifr, "Query====>" + Query);
			List<List<String>> Result = ifr.getDataFromDB(Query);
			Log.consoleLog(ifr, "#Result===>" + Result.toString() + "==>size==>" + Result.size());

			if (Result.size() > 0) {
				EmailId = Result.get(0).get(0);
			}

			Log.consoleLog(ifr, "#EmailId===>" + EmailId);
			return EmailId;
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getCurrentEmailId==>" + e);
		}

		return "";

	}

	// added by Aravindh
	public String getAmountForEligibilityCheckCBperfiosBO(IFormReference ifr, HashMap<String, String> loandata) {
		try {
			Log.consoleLog(ifr, "inside getAmountForEligibilityCheckCBperfiosBO:::::");
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, " ProcessInsanceId::::" + ProcessInsanceId);
			Log.consoleLog(ifr, " loandata::::" + loandata);
			BigDecimal grossSalaryMultiple = mCheckBigDecimalValue(ifr,
					getConstantValue(ifr, "LOANELIGIBILITYCB", "GROSSSALMULTIPLE"));
			Log.consoleLog(ifr, " grossSalaryMultiple from constants ::::" + grossSalaryMultiple);
			BigDecimal netsalary;
			BigDecimal ftTenure = mCheckBigDecimalValue(ifr, loandata.get("tenure"));
			Log.consoleLog(ifr, " ftTenure::::" + ftTenure);
			BigDecimal ftRoi = mCheckBigDecimalValue(ifr, loandata.get("roi"));
			Log.consoleLog(ifr, " ftRoi::::" + ftRoi);
			BigDecimal grosssalaryip = mCheckBigDecimalValue(ifr, loandata.get("GrossSalary"));
			Log.consoleLog(ifr, " grosssalaryip::::" + grosssalaryip);
			BigDecimal deductionsalary = mCheckBigDecimalValue(ifr, loandata.get("DeductionMonthly"));
			Log.consoleLog(ifr, " deductionsalary::::" + deductionsalary);

			// BigDecimal Cmpsalary = new BigDecimal(10000);
			BigDecimal Cmpsalary = mCheckBigDecimalValue(ifr,
					getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "NETTAKEHOMESALARY"));
			Log.consoleLog(ifr, " Cmpsalary::::" + Cmpsalary);
			BigDecimal mulValue4 = mCheckBigDecimalValue(ifr,
					getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "NETHOMEPERENT"));
			Log.consoleLog(ifr, " mulValue4 :::: " + mulValue4);
			BigDecimal Hundred = new BigDecimal(100);
			BigDecimal PercentOfGrossSalary = mulValue4.divide(Hundred);
			Log.consoleLog(ifr, " PercentOfGrossSalary :::: " + PercentOfGrossSalary);
			BigDecimal emiperlc = calculatePMT(ifr, ftRoi, Integer.parseInt(String.valueOf(ftTenure)));
			Log.consoleLog(ifr, " emiperlc:::: " + emiperlc);

			// String LOANAMOUNTPERPOLICY_Query = "SELECT PARAMVALUE FROM LOS_MST_CONFIGS
			// WHERE PARAMNAME ='LOANAMOUNTPERPOLICYMUTIPLE' AND PARAMTYPE
			// ='LOANELIGIBILITY' AND SUBPRODCODE ='STP-CB'";
//            String LOANAMOUNTPERPOLICY = "";
//
//            String LOANAMOUNTPERPOLICY_Query = ConfProperty.getQueryScript("LOANAMOUNTPERPOLICYMUTIPLE");
//            List<List<String>> LoanAmtMutiplelist = cf.mExecuteQuery(ifr, LOANAMOUNTPERPOLICY_Query,
//                    "LOANAMOUNTPERPOLICY_Query:");
//            if (!LoanAmtMutiplelist.isEmpty()) {
//                LOANAMOUNTPERPOLICY = LoanAmtMutiplelist.get(0).get(0);
//            }
			// BigDecimal lacAmount = new BigDecimal(100000);
			BigDecimal LOANAMOUNTPERPOLICYMULTIPLE = mCheckBigDecimalValue(ifr,
					getParamConfig(ifr, "PL", "STP-CB", "LOANELIGIBILITY", "LOANAMOUNTPERPOLICYMUTIPLE"));

			BigDecimal cbCibilOblig = mCheckBigDecimalValue(ifr, loandata.get("cibiloblig"));
			Log.consoleLog(ifr, " cbCibilOblig::::" + cbCibilOblig);
			BigDecimal grossSalary25Perc = new BigDecimal(0);
			grossSalary25Perc = grosssalaryip.multiply(PercentOfGrossSalary, MathContext.DECIMAL128).setScale(2,
					RoundingMode.HALF_UP);
			Log.consoleLog(ifr, " grossSalary25Perc ==> " + grossSalary25Perc);
			int comparisonResult = grossSalary25Perc.compareTo(Cmpsalary);
			if (comparisonResult > 0) {
				Log.consoleLog(ifr, "inside if comparisonResult::::" + comparisonResult);
				netsalary = grossSalary25Perc;
			} else {
				Log.consoleLog(ifr, "inside ELSE comparisonResult::::" + comparisonResult);
				netsalary = Cmpsalary;
			}
			Log.consoleLog(ifr, "netsalary ===> " + netsalary);
			BigDecimal netIncome = (grosssalaryip.subtract(deductionsalary, MathContext.DECIMAL32)
					.subtract(cbCibilOblig, MathContext.DECIMAL32).subtract(netsalary, MathContext.DECIMAL32))
					.setScale(2, RoundingMode.HALF_UP);
			Log.consoleLog(ifr, "net_income===> " + netIncome);

			BigDecimal loanAmount = LOANAMOUNTPERPOLICYMULTIPLE.multiply(netIncome).divide(emiperlc, 2,
					RoundingMode.HALF_UP);

			Log.consoleLog(ifr, "loanAmount===> " + loanAmount);

			String reqAmount = "";
			String proposedFacilityQuery = ConfProperty.getQueryScript("PROPOFACILITYQUERY")
					.replaceAll("#ProcessInsanceId#", ProcessInsanceId);
			List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
			if (list4.size() > 0) {
				reqAmount = list4.get(0).get(0);
			}
			Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);
			BigDecimal BDreqAmount = new BigDecimal(reqAmount);

			BigDecimal TwentyFivetimesgrosssal = grossSalaryMultiple.multiply(grosssalaryip);
			Log.consoleLog(ifr, "TwentyFivetimesgrosssal===> " + TwentyFivetimesgrosssal);
			BigDecimal prodspeccapping = mCheckBigDecimalValue(ifr, loandata.get("loancap"));
			Log.consoleLog(ifr, "prodspeccapping===> " + prodspeccapping);
			BigDecimal inprincipleamount = loanAmount.min(TwentyFivetimesgrosssal).min(prodspeccapping)
					.min(BDreqAmount);
			Log.consoleLog(ifr, "inprincipleamount===> " + inprincipleamount);
			BigDecimal finalEligibleLoanAmount = loanAmount.min(TwentyFivetimesgrosssal).min(prodspeccapping)
					.min(BDreqAmount);

			// BigDecimal finaleligibility1 = LoanEC.getAmountForEligibilityCheck(ifr,
			// loandata);
			Log.consoleLog(ifr, " final Eligible Loan Amount finaleligibility===> " + finalEligibleLoanAmount);
			JSONObject obj = new JSONObject();
			obj.put("grosssalaryip", grosssalaryip.toString());
			obj.put("deductionsalary", deductionsalary.toString());
			obj.put("cbCibilOblig", cbCibilOblig.toString());
			obj.put("netsalary", netsalary.toString());
			obj.put("netIncome", netIncome.toString());
			obj.put("ftTenure", ftTenure.toString());
			obj.put("ftRoi", ftRoi.toString());
			obj.put("loanAmount", loanAmount.toString());
			obj.put("sixtimesgrosssal", TwentyFivetimesgrosssal.toString());
			obj.put("prodspeccapping", prodspeccapping.toString());
			obj.put("finaleligibility", finalEligibleLoanAmount.toString());
			Log.consoleLog(ifr, " Data after EligibilitynCalculation  obj:: " + obj);

			return obj.toString();

		} catch (NumberFormatException e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return RLOS_Constants.ERROR;
	}

	// Added by Ahmed on 01-02-2024
	public String getSalaryAccountNo(IFormReference ifr, String ProductType, String CustomerId) {
		try {

			String salaryAccountNo = "";
			String Query = "select salaryacc_no from los_mst_papl where " + "customer_id='" + CustomerId
					+ "' and rownum=1";
			Log.consoleLog(ifr, "Query==>" + Query);
			List<List<String>> Result = ifr.getDataFromDB(Query);
			Log.consoleLog(ifr, "#Result===>" + Result.toString());
			salaryAccountNo = "";
			if (Result.size() > 0) {
				salaryAccountNo = Result.get(0).get(0);
			}
			return salaryAccountNo;

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getSalaryAccountNo" + e);
		}
		return "";

	}

	public String mGetRoiID(IFormReference ifr) {
		String query = "select roiid from LOS_M_ROI_Scheme where SchemeID='"
				+ mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "mGetRoiID : " + result);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetSubProductCode(IFormReference ifr) {
		String query = "select SubProductCode from LOS_M_PRODUCT_RLOS where SchemeID='"
				+ mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}
	// added by ishwarya on 14022024

	public void mAccCompleteWorkItemStatus(IFormReference ifr) {
		Log.consoleLog(ifr, "mAccCompleteWorkItemStatus :: ");
		try {
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET Application_Status='COMPLETED' WHERE WINAME='" + PID
					+ "'";
			Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
			ifr.saveDataInDB(queryUpdate);
		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception mAccCompleteWorkItemStatus :: " + e);
		}
	}

	public String mGetMinLoanAmount(IFormReference ifr) {
		String query = "select minloanamount from los_m_loaninfo where Scheme_ID='"
				+ mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String getStateCode(IFormReference ifr, String productType, String processInstanceId) {

		// String query = "";
		// String stateName = "";
		// String stateCode = "";
		// String processInstanceId =
		// ifr.getObjGeneralData().getM_strProcessInstanceId();
//Funcion modified by Ahmed on 08-08-2024
//        if (productType.equalsIgnoreCase("PAPL")) {
//            query = "SELECT PermState FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY " + "WHERE WINAME='" + processInstanceId
//                    + "' AND ROWNUM=1";
//        } else {
//            query = "select state from LOS_NL_Address where F_KEY =(select F_KEY from "
//                    + "los_nl_basic_info  where PID ='" + processInstanceId + "' "
//                    + "and applicanttype='B') and addresstype='P'";
//        }
//
//        Log.consoleLog(ifr, "query==>" + query);
//        List<List<String>> result = ifr.getDataFromDB(query);
//        if (result.size() > 0) {
//            stateName = result.get(0).get(0);
//        }
		String stateCode = "";
		String stateName = getSelectedAccStateName(ifr, productType);
		if (stateName.contains(RLOS_Constants.ERROR)) {
			return RLOS_Constants.ERROR;
		}

		// Added by Ahmed Alireza on 12-02-2024 for picking statecode from Master
		String Query3 = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE " + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('"
				+ stateName + "')) AND ROWNUM=1";
		Log.consoleLog(ifr, "Query3==>" + Query3);
		List<List<String>> Result3 = ifr.getDataFromDB(Query3);
		Log.consoleLog(ifr, "#Result3===>" + Result3.toString());
		if (!Result3.isEmpty()) {
			stateCode = Result3.get(0).get(0);
		}

		return stateCode;
	}

	public BigDecimal calculatePMT(IFormReference ifr, BigDecimal rate, int nper) {
		Log.consoleLog(ifr, "Inside calculatePMT roi :: " + rate + "Tenure ::" + nper);
		rate = rate.divide(new BigDecimal("1200"), MathContext.DECIMAL64);
		BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
		BigDecimal pv = new BigDecimal("-100000");
		BigDecimal ratePowerN = onePlusRate.pow(nper);
		BigDecimal fv = BigDecimal.ZERO;
		BigDecimal numerator = rate.multiply(ratePowerN, MathContext.DECIMAL64);
		BigDecimal denominator = ratePowerN.subtract(BigDecimal.ONE);

		BigDecimal factor = numerator.divide(denominator, MathContext.DECIMAL64);

		BigDecimal pmt = pv.negate().multiply(factor, MathContext.DECIMAL64)
				.add(fv.divide(ratePowerN, MathContext.DECIMAL64), MathContext.DECIMAL64)
				.setScale(2, RoundingMode.HALF_UP);
		Log.consoleLog(ifr, "pmt::" + pmt);

		return pmt;
	}

	public String mCheckWorkingExperience(IFormReference ifr, String currentExperienceID, String totalExperienceID) {
		Log.consoleLog(ifr, "Inside mCheckWorkingExperience:");
		String currentExperience = ifr.getValue(currentExperienceID).toString();
		String totalExperience = ifr.getValue(totalExperienceID).toString();

		Log.consoleLog(ifr, "Inside mCheckWorkingExperience: currentExperience " + currentExperience);
		Log.consoleLog(ifr, "Inside mCheckWorkingExperience: totalExperience " + totalExperience);
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
		List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
		String loan_selected = loanSelected.get(0).get(0);

		Log.consoleLog(ifr, "loan type==>" + loan_selected);
		if (!(totalExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase(null))) {
			Log.consoleLog(ifr, "Inside if is empty mCheckWorkingExperience: totalExperience " + totalExperience);
			int currentExp = 0;
			if (!(currentExperience.equalsIgnoreCase(""))) {
				currentExp = Integer.parseInt(currentExperience);
			}
			int totalExp = 0;
			if (!(totalExperience.equalsIgnoreCase(""))) {
				totalExp = Integer.parseInt(totalExperience);
			}
			if (currentExp == 0 || totalExp == 0) {
				Log.consoleLog(ifr, "Inside if condition of mCheckWorkingExperience:");
				ifr.setValue(currentExperienceID, "");
				ifr.setValue(totalExperienceID, "");
				JSONObject message = new JSONObject();
				message.put("showMessage", cf.showMessage(ifr, "", "error",
						"Current Experience and Total Experience Should Not Be Zero!"));
				return message.toString();
			}
			if (currentExp > totalExp
					&& (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
				Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp : " + totalExp
						+ "currentExp : " + currentExp);
				ifr.setValue(currentExperienceID, "");
				ifr.setValue(totalExperienceID, "");
				JSONObject message = new JSONObject();
				message.put("showMessage", cf.showMessage(ifr, "", "error",
						"Current Experience cannot be greater than Total Experience!"));
				return message.toString();
			}
			if (totalExp < currentExp
					&& (!(currentExperience.equalsIgnoreCase("")) && !(totalExperience.equalsIgnoreCase("")))) {
				Log.consoleLog(ifr, "inside the if condition of mCheckWorkingExperience totalExp Less : " + totalExp
						+ "currentExp : " + currentExp);
				ifr.setValue(currentExperienceID, "");
				ifr.setValue(totalExperienceID, "");
				JSONObject message = new JSONObject();
				message.put("showMessage",
						cf.showMessage(ifr, "", "error", "Total Experience cannot be Less than Current Experience!"));
				return message.toString();
			}
		}
		return "";
	}

	public String mCalculateNetIncome(IFormReference ifr, String grossIncomeID, String deductIncomeID,
			String netIncomeID, String grossPensionIncome, String netPensionIncome) {
		Log.consoleLog(ifr, "inside the mCalculateNetIncome : ");
		JSONObject message = new JSONObject();
		try {
			BigDecimal grossAmt = new BigDecimal(ifr.getValue(grossIncomeID).toString().equalsIgnoreCase("") ? "0.0"
					: ifr.getValue(grossIncomeID).toString());
			BigDecimal deductamt = new BigDecimal(ifr.getValue(deductIncomeID).toString().equalsIgnoreCase("") ? "0.0"
					: ifr.getValue(deductIncomeID).toString());
			String grossAmount = ifr.getValue(grossIncomeID).toString();
			String deduction = ifr.getValue(deductIncomeID).toString();
			BigDecimal grossPension = new BigDecimal(
					ifr.getValue(grossPensionIncome).toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue(grossPensionIncome).toString());
			String grossPen = ifr.getValue(grossIncomeID).toString();
			BigDecimal netPension;
			BigDecimal netSalary;
			BigDecimal comp = new BigDecimal(0);
			if ((!grossAmount.equalsIgnoreCase("")) || (!deduction.equalsIgnoreCase(""))
					|| (!grossPen.equalsIgnoreCase(""))) {
				if ((grossAmt.compareTo(deductamt) > 0) || (grossPension.compareTo(deductamt) > 0)) {
					netSalary = grossAmt.subtract(deductamt);
					String NetSalary = netSalary.toString();
					Log.consoleLog(ifr, "NetSalary ..:" + NetSalary);
					ifr.setValue(netIncomeID, NetSalary);
					netPension = grossPension.subtract(deductamt);
					String NetPension = netPension.toString();
					Log.consoleLog(ifr, "NetPension ..:" + NetPension);
					ifr.setValue(netPensionIncome, NetPension);

				} else {
					ifr.setValue(netIncomeID, "");
					ifr.setValue(deductIncomeID, "");
					ifr.setValue(netPensionIncome, "");
					message.put("showMessage",
							cf.showMessage(ifr, "", "error", "Net Income cannot be less than deductions!"));
					return message.toString();
				}
			} else {
				Log.consoleLog(ifr, "inside the else condition of mCalculateNetIncome :");
				if ((grossAmt.compareTo(comp) == 0) || (grossPension.compareTo(comp) == 0)) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue(netIncomeID, "");
					ifr.setValue(netPensionIncome, "");
				} else if (deductamt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue(netIncomeID, "");
					ifr.setValue(netPensionIncome, "");
				} else {
					Log.consoleLog(ifr, "inside the else condition:");
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "error inside the  mCalculateNetIncome: " + e);
			Log.errorLog(ifr, "error inside the  mCalculateNetIncome: " + e);
		}
		return "";
	}

	public String mGetMinLoanAmountVL(IFormReference ifr, String SchemeId) {
		String query = "select minloanamount from los_m_loaninfo where Scheme_ID='" + SchemeId + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetRoiIDVL(IFormReference ifr, String schemeID) {
		String query = "select roiid from LOS_M_ROI_Scheme where SchemeID='" + schemeID + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "mGetRoiID : " + result);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetProductCodeVL(IFormReference ifr, String purposeCode) {
		String query = "select ProductCode from LOS_M_PRODUCT_RLOS where SchemeID='" + mGetSchemeIDVL(ifr, purposeCode)
				+ "'and purposeCode='" + purposeCode + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetSchemeIDVL(IFormReference ifr, String purposeCode) {
		String query = "select A.SchemeID from LOS_M_PRODUCT_RLOS A where A.PurposeCode='" + purposeCode
				+ "' and A.SUBPRODUCTCODE='" + mGetSubproductVL(ifr, purposeCode) + "' and A.productcode='VL'";
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetSubproductVL(IFormReference ifr, String purposeCode) {
		String query = "select A.SUBPRODUCTCODE from LOS_M_PRODUCT_RLOS A where A.PurposeCode='" + purposeCode
				+ "' and A.productcode='VL'";
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetRoiVL(IFormReference ifr, String schemeid) {
		String crg = mGetCRG(ifr);
		String queryroi = ConfProperty.getQueryScript("GETROIQUERY").replaceAll("#schemeid#", schemeid)
				.replaceAll("#crg#", crg);
		Log.consoleLog(ifr, "query:" + queryroi);
		List<List<String>> result = cf.mExecuteQuery(ifr, queryroi, "ROI SCHEME");
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String mGetCRG(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		// replaceAll("#ProcessInsanceId#", ProcessInsanceId)
		String CRGquery = ConfProperty.getQueryScript("GETCRGQUERY").replaceAll("#ProcessInsanceId#",
				processInstanceId);
		// "select riskscore from LOS_L_RISK_RATING where PID='"+ProcessInsanceId+"'";
		List<List<String>> result = cf.mExecuteQuery(ifr, CRGquery, "CRG RATING ");
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	// Added by Ahmed on 29-03-2024 for ROI Discussion happened with Pandiyan
	public void selectROIValue(IFormReference ifr) {

		Log.consoleLog(ifr, "#selectROIValue starting....");

		String schemeID = mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
		Log.consoleLog(ifr, "schemeID==>" + schemeID);
		ifr.setValue("P_PAPL_RATEOFINTEREST", "0");// Initial Value Setting
		String roiType = ifr.getValue("AvailOffer_Papl2_combo1").toString();
		Log.consoleLog(ifr, "roiType selected=>" + roiType);
		ifr.setValue("P_PAPL_RATEOFINTEREST", getROI(ifr, schemeID, roiType));

		Log.consoleLog(ifr, "#selectROIValue Ended");
	}

	public String getNESLModeQuery(IFormReference ifr, String productType) {

		Log.consoleLog(ifr, "#getNESLModeQuery starting....");

		try {
			String query = "SELECT F2FMODE FROM LOS_MST_INT_NESL WHERE PRODUCT='" + productType + "'";
			Log.consoleLog(ifr, "query==>" + query);
			List<List<String>> result = ifr.getDataFromDB(query);
			if (result.size() > 0) {
				return result.get(0).get(0);
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getNESLModeQuery==>" + e);
		}
		return "N";
	}

	public void controlvisiblity(IFormReference ifr, String visiblefeild) {
		Log.consoleLog(ifr, "controlvisiblity" + visiblefeild);
		String[] FieldVisibleFalse = visiblefeild.split(",");
		for (String FieldVisibleFalse1 : FieldVisibleFalse) {
			ifr.setStyle(FieldVisibleFalse1, "visible", "true");
			Log.consoleLog(ifr, "FieldVisibleFalse1 " + FieldVisibleFalse1);
		}

	}

	public void controlinvisiblity(IFormReference ifr, String visiblefeild) {
		String[] FieldVisibleFalse = visiblefeild.split(",");
		for (String FieldVisibleFalse1 : FieldVisibleFalse) {
			ifr.setStyle(FieldVisibleFalse1, "visible", "false");
		}

	}

	public String mOutWardDocgen(IFormReference ifr, String loanSelected) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String schemeid = mGetSchemeID(ifr, processInstanceId);
		String documentquery = ConfProperty.getQueryScript("GETOUTWARDDOCQUERY").replaceAll("#schemeid#", schemeid);
		String strOutwarddocstatus = "";
		Log.consoleLog(ifr, "document query:" + documentquery);
		List<List<String>> result = cf.mExecuteQuery(ifr, documentquery, "Outward Document generation ");
		for (int i = 0; i < result.size(); i++) {
			strOutwarddocstatus = cm.generatedoc(ifr, result.get(i).toString(), loanSelected);
			if (!strOutwarddocstatus.contains("success")) {
				Log.consoleLog(ifr, "Error in Document gemeration ");
				return RLOS_Constants.ERROR;

			}
		}

		return RLOS_Constants.SUCCESS;
	}

	public void triggerCCMAPIs(IFormReference ifr, String processInstanceId, String productType, String triggerStage,
			String triggerBodyParams, String triggerSubjectParams, String fileName, String fileContent) {
		Log.consoleLog(ifr, "#triggerCCMAPIs starting....");
		try {
			Email em = new Email();
			SMS sms = new SMS();
			WhatsApp wh = new WhatsApp();

			// String emailId = getCurrentEmailId(ifr, productType, "CB");
			String mobileNumber = getMobileNumber(ifr);
			mobileNumber = mobileNumber.length() > 10 ? mobileNumber.substring(mobileNumber.length() - 10)
					: mobileNumber;

			Log.consoleLog(ifr, "triggerSubjectParams==>" + triggerSubjectParams);
			Log.consoleLog(ifr, "triggerBodyParams=====>" + triggerBodyParams);
			Log.consoleLog(ifr, "fileName==============>" + fileName);
			Log.consoleLog(ifr, "fileContent===========>" + fileContent);

			sms.sendSMS(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber, triggerBodyParams,
					triggerSubjectParams, "RETAIL", triggerStage);
			wh.sendWhatsAppMsg(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber,
					triggerBodyParams, triggerSubjectParams, "RETAIL", triggerStage);

		} catch (Exception e) {
			Log.consoleLog(ifr, "Inside triggerCCMAPIs" + e);
		}
		Log.consoleLog(ifr, "#triggerCCMAPIs Ended....");
	}

	public void controlDisable(IFormReference ifr, String disablefeild) {
		String[] FieldDisableFalse = disablefeild.split(",");
		for (String FieldDisableFalse1 : FieldDisableFalse) {
			Log.consoleLog(ifr, "FieldDisableFalse1 " + FieldDisableFalse1);
			ifr.setStyle(FieldDisableFalse1, "disable", "true");
		}

	}

	public void tabInVisibility(IFormReference ifr, String tabId, String visiblefield) {
		String[] FieldVisibleFalse = visiblefield.split(",");
		for (String sheetid : FieldVisibleFalse) {
			Log.consoleLog(ifr, "intoConvenorDetails" + sheetid);
			ifr.setTabStyle(tabId, sheetid, "visible", "false");
		}

	}

	public void controlEnable(IFormReference ifr, String visiblefeild) {
		String[] FieldVisibleFalse = visiblefeild.split(",");
		for (String FieldVisibleFalse1 : FieldVisibleFalse) {
			Log.consoleLog(ifr, "controlEnable" + FieldVisibleFalse1);
			ifr.setStyle(FieldVisibleFalse1, "disable", "false");
		}
	}

	public boolean numberformatvalidation(IFormReference ifr, String input) {
		String regex = "^[+-]?\\d*(\\.\\d+)?$";
		Log.consoleLog(ifr, "inside onchangeAccVariance numberformatvalidation " + input);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		boolean isValid = matcher.matches();
		Log.consoleLog(ifr, "inside onchangeAccVariance numberformatvalidation " + isValid);
		return isValid;
	}

	public void controlEnablenumber(IFormReference ifr, String visiblefeild) {
		String[] FieldVisibleFalse = visiblefeild.split(",");
		for (String FieldVisibleFalse1 : FieldVisibleFalse) {
			Log.consoleLog(ifr, "controlEnablenumber" + FieldVisibleFalse1);
			if (ifr.getValue(FieldVisibleFalse1).toString().isEmpty()) {
				ifr.setValue(FieldVisibleFalse1, "0.0");
			}
			ifr.setStyle(FieldVisibleFalse1, "disable", "false");
		}
	}

	/// Added by mnonesh to check the mini and max values
	public boolean checkROISpread(IFormReference ifr, String formvalue, String minvalue, String maxvalue) {

		return Double.parseDouble(formvalue) >= Double.parseDouble(minvalue)
				&& Double.parseDouble(formvalue) <= Double.parseDouble(minvalue);

	}

	// Added by Keerthana on 30/05/2024 to get SchemeId based on auxiliary data and
	// update in external table
	public void updateSchemeID(IFormReference ifr, String processInstanceId, HashMap<String, String> map) {
		String productCode = map.get("productcode");
		String subProductCode = map.get("subproductcode");
		String purposeCode = map.get("purposecode");
		String variantCode = map.get("variantcode");
		String schemeId = "";
		String queryGetSchemeId = "select schemeid from los_m_product_rlos " + "where productcode = '" + productCode
				+ "' and subproductcode = '" + subProductCode + "' " + "and purposecode = NVL('" + purposeCode
				+ "','NA') and variantcode = NVL('" + variantCode + "','NA')";
		Log.consoleLog(ifr, "PortalCommonMethods:updateSchemeID->querySchemeid:" + queryGetSchemeId);
		List<List<String>> result = ifr.getDataFromDB(queryGetSchemeId);
		if (result.size() > 0) {
			schemeId = result.get(0).get(0);
		}
		Log.consoleLog(ifr, "PortalCommonMethods:updateSchemeID->schemeId:" + schemeId);
		String querySetSchemeId = "update los_ext_table set schemid ='" + schemeId + "' where pid = '"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "PortalCommonMethods:updateSchemeID->querySetSchemeId:" + querySetSchemeId);
		ifr.saveDataInDB(querySetSchemeId);
	}

//Added by Keerthana on 30/05/2024 to get Basic SchemeId of Product
	public String getBaseSchemeID(IFormReference ifr, String processInstanceId) {
		String query = "select s.schemeid from los_m_product_rlos s,los_ext_table e "
				+ " where s.productcode = e.producttype and s.subproductcode=e.subproducttype and s.schemetype='BASIC' "
				+ " and e.pid = '" + processInstanceId + "'";
		Log.consoleLog(ifr, "PCM:::::getBaseSchemeID:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

//Added by Keerthana on 30/05/2024 to get final SchemeId of application
	public String getSchemeID(IFormReference ifr, String processInstanceId) {
		String query = "select schemeid from LOS_EXT_TABLE where PID='" + processInstanceId + "'";
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String getProductCode(IFormReference ifr) {
		String query = "select ProductCode from LOS_M_PRODUCT_RLOS where SchemeID='"
				+ getBaseSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		Log.consoleLog(ifr, "PCM::::getProductCode:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty()) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String getSubProductCode(IFormReference ifr) {
		String query = "select SubProductCode from LOS_M_PRODUCT_RLOS where SchemeID='"
				+ getBaseSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId()) + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty()) {
			return result.get(0).get(0);
		}
		return "";
	}

	// Added by Aravindh K.K on 24/06/24 to get ScheduleCode For Fully Assisted
	// Journey's
	public String getScheduleCode(IFormReference ifr) {
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

//        String query = "SELECT SUBSTR(SCHEDULECODE, 1, INSTR(SCHEDULECODE, '-') - 1) AS SCHEDULECODE FROM LOS_NL_PROPOSED_FACILITY "
//                + "WHERE PID ='" + PID + "'";
		String query = ConfProperty.getQueryScript("GETSCHEDULECODE").replaceAll("#PID#", ProcessInstanceId);
		Log.consoleLog(ifr, " getScheduleCode query: " + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty()) {
			Log.consoleLog(ifr, "getScheduleCode result====>" + result.get(0).get(0));
			return result.get(0).get(0);
		}
		return "";
	}

	// Added by Aravindh K.K on 24/06/24 to get Salary Account Selected For Fully
	// Assisted Journey's
	public String getSelectSalaryAccountBO(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String query = "SELECT SELECTSALARYACCOUNT FROM  LOS_NL_BASIC_INFO WHERE PID = '" + PID
				+ "' AND APPLICANTTYPE ='B'";
		Log.consoleLog(ifr, " getScheduleCode query: " + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String getApplicationRefNumber(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "SELECT APPLICATION_NO FROM LOS_WIREFERENCE_TABLE WHERE WINAME='" + processInstanceId
				+ "' AND ROWNUM=1";
		Log.consoleLog(ifr, "getApplicationRefNumber query==>" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty()) {
			return result.get(0).get(0);
		}
		return "";

	}

	public String getSelectedBranchCode(IFormReference ifr, String selectedAccountId) {
		Log.consoleLog(ifr, "#getBranchCodeFromAccount started..");

		try {

			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String customerId = getCustomerIDForOtherProducts(ifr);

			String queryGetBranchCode = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE " + "TRANSACTION_ID='"
					+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "' AND \n"
					+ "API_NAME='CBS_Advanced360Inquiryv2' AND API_STATUS='SUCCESS' AND ROWNUM=1";
			Log.consoleLog(ifr, "queryGetBranchCode===>" + queryGetBranchCode);
			List<List<String>> Result = ifr.getDataFromDB(queryGetBranchCode);
			Log.consoleLog(ifr, "#Result===>" + Result.toString());
			String responseFromDB = "";
			if (!Result.isEmpty()) {
				responseFromDB = Result.get(0).get(0);
			}

			Log.consoleLog(ifr, "responseFromDB===>" + responseFromDB);
			if (responseFromDB.equalsIgnoreCase("")) {
				Advanced360EnquiryDatav2 objAdv2 = new Advanced360EnquiryDatav2();
				responseFromDB = objAdv2.executeCBSAdvanced360Inquiryv2(ifr, processInstanceId, customerId, "Budget",
						"", "");
			}

			if ((!responseFromDB.equalsIgnoreCase("")) || (!responseFromDB.equalsIgnoreCase("{}"))) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(responseFromDB);
				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body==>" + body);
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
				Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
				JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser
						.parse(XfaceCustomerAccountDetailsDTO);
				String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
				Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
				JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

				if (!AccountDetailsObj.isEmpty()) {
					for (int i = 0; i < AccountDetailsObj.size(); i++) {
						Log.consoleLog(ifr, "AccountDetailsObj==>" + AccountDetailsObj.get(i));
						String inputJSON = AccountDetailsObj.get(i).toString();
						JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
						String accountId = inputJSONObj.get("AccountId").toString();
						Log.consoleLog(ifr, "accountId==============>" + accountId);
						Log.consoleLog(ifr, "selectedAccountId======>" + selectedAccountId);

						if (accountId.contains(selectedAccountId)) {
							String branchCode = inputJSONObj.get("BranchCode").toString();
							Log.consoleLog(ifr, "branchCode==============>" + branchCode);

							String upQuery = "UPDATE LOS_L_SOURCINGINFO SET " + "BRANCHCODE='" + branchCode
									+ "' WHERE PID='" + processInstanceId + "'";
							Log.consoleLog(ifr, "upQuery==============>" + upQuery);
							ifr.saveDataInDB(upQuery);
							return branchCode;
						}

					}
				}

			} else {
				return RLOS_Constants.ERROR;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getBranchCodeFromAccount==========>" + e);
		}
		return RLOS_Constants.ERROR;

	}

	public String mAccCalculateNetIncomeCommonMethod(IFormReference ifr, String value1, String value2,
			String targetControl) {
		Log.consoleLog(ifr, "inside the mAccCalculateNetIncomeCommonMethod : ");
		try {
			String message = "";
			BigDecimal grossAmt = new BigDecimal(
					ifr.getValue(value1).toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue(value1).toString());
			BigDecimal deductamt = new BigDecimal(
					ifr.getValue(value2).toString().equalsIgnoreCase("") ? "0.0" : ifr.getValue(value2).toString());
			String grossAmount = ifr.getValue(value1).toString();
			String deduction = ifr.getValue(value2).toString();
			BigDecimal netSalary;
			BigDecimal comp = new BigDecimal(0);
			if ((!grossAmount.equalsIgnoreCase("")) && (!deduction.equalsIgnoreCase(""))) {
				if (grossAmt.compareTo(deductamt) > 0) {
					netSalary = grossAmt.subtract(deductamt);
					String NetSalary = netSalary.toString();
					Log.consoleLog(ifr, "NetValueafter subtract ..:" + NetSalary);
					ifr.setValue(targetControl, NetSalary);
				} else {
					ifr.setValue(value1, "");
					ifr.setValue(value2, "");
					message = "Net Amount cannot be less than deductions!";
					return message;
				}
			} else {
				Log.consoleLog(ifr, "inside the else condition of mAccCalculateNetIncomeCommonMethod :");
				if (grossAmt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue(targetControl, "");
				} else if (deductamt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue(targetControl, "");
				} else {
					Log.consoleLog(ifr, "inside the else condition:");
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "error inside the  mAccCalculateNetIncomeCommonMethod: " + e);
		}
		return "";
	}

	public String getMISStatusFromDemography(IFormReference ifr) {
		Log.consoleLog(ifr, "#getMISStatusFromDemography started..");

		try {

			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

			String queryGetBranchCode = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE " + "TRANSACTION_ID='"
					+ processInstanceId + "' AND \n"
					+ "API_NAME='CBS_Demographic' AND API_STATUS='SUCCESS' AND ROWNUM=1";
			Log.consoleLog(ifr, "queryGetBranchCode===>" + queryGetBranchCode);
			List<List<String>> Result = ifr.getDataFromDB(queryGetBranchCode);
			Log.consoleLog(ifr, "#Result===>" + Result.toString());
			String responseFromDB = "";
			if (!Result.isEmpty()) {
				responseFromDB = Result.get(0).get(0);
			}

			if ((!responseFromDB.equalsIgnoreCase("")) || (!responseFromDB.equalsIgnoreCase("{}"))) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(responseFromDB);
				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CustDemographInqResponse = bodyObj.get("CustDemographInqResponse").toString();
				JSONObject CustDemographInqResponseObj = (JSONObject) parser.parse(CustDemographInqResponse);

				int incompleteCount = 0;
				for (int i = 1; i <= 10; i++) {
					String custMisCode = CustDemographInqResponseObj.get("CustMisCode" + i).toString();
					Log.consoleLog(ifr, "CustMisCode" + i + "===>" + custMisCode);

					if ((custMisCode.equalsIgnoreCase("")) || (custMisCode.equalsIgnoreCase("{}"))) {
						Log.consoleLog(ifr, "#Incomplete");
						incompleteCount++;
					}
				}

				Log.consoleLog(ifr, "incompleteCount" + incompleteCount);
				if (incompleteCount > 0) {
					return "Incomplete";
				} else {
					return "Complete";
				}

			} else {
				return RLOS_Constants.ERROR;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getMISStatusFromDemography==========>" + e);
		}
		return RLOS_Constants.ERROR;

	}

	public String getAccountNumber(IFormReference ifr, String journeyType) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			if (!journeyType.equalsIgnoreCase("PAPL")) {
				String f_Key = Fkey(ifr, "B");
				String strAcctquery = ConfProperty.getQueryScript("SELECTACCOUNTDETAIL")
						.replaceAll("#PID#", processInstanceId).replaceAll("#FKEY#", f_Key);
				Log.consoleLog(ifr, "FKEYQUERY :::" + strAcctquery);
				List<List<String>> strAcct = ifr.getDataFromDB(strAcctquery);
				String accountNumber = "";
				if (!strAcct.isEmpty()) {
					accountNumber = strAcct.get(0).get(0);
					Log.consoleLog(ifr, "accountNumber :::" + accountNumber);
				}
				return accountNumber;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getAccountNumber==========>" + e);
		}
		return RLOS_Constants.ERROR;
	}

	public BigDecimal calculateEMIPMT(IFormReference ifr, String LoanAmount, BigDecimal rate, int nper) {
		Log.consoleLog(ifr,
				"PortalCommonMethods::calculatePMT entering==========>" + LoanAmount + "," + rate + "," + nper);
		rate = rate.divide(new BigDecimal("1200"), MathContext.DECIMAL64);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT rate==========>" + rate);
		BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT onePlusRate==========>" + onePlusRate);
		BigDecimal pv = new BigDecimal(LoanAmount);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT pv==========>" + pv);
		BigDecimal ratePowerN = onePlusRate.pow(nper);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT ratePowerN==========>" + ratePowerN);
		BigDecimal fv = BigDecimal.ZERO;
		BigDecimal numerator = rate.multiply(ratePowerN, MathContext.DECIMAL64);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT numerator==========>" + numerator);
		BigDecimal denominator = ratePowerN.subtract(BigDecimal.ONE);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT denominator==========>" + denominator);
		BigDecimal factor = numerator.divide(denominator, MathContext.DECIMAL64);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT factor==========>" + factor);
		BigDecimal pmt = pv.negate().multiply(factor, MathContext.DECIMAL64)
				.add(fv.divide(ratePowerN, MathContext.DECIMAL64), MathContext.DECIMAL64)
				.setScale(2, RoundingMode.HALF_UP);
		Log.consoleLog(ifr, "PortalCommonMethods::calculatePMT pmt==========>" + pmt);
		return pmt;
	}

	public String getPANNumber(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryPANNumber = "SELECT PANNUMBER FROM LOS_TRN_CUSTOMERSUMMARY WHERE " + "WINAME='" + processInstanceId
				+ "'";
		Log.consoleLog(ifr, "queryPANNumber===>" + queryPANNumber);
		List<List<String>> Result = ifr.getDataFromDB(queryPANNumber);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		String panNumber = "";
		if (!Result.isEmpty()) {
			panNumber = Result.get(0).get(0);
		}
		return panNumber;
//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public String getSancAuhority(IFormReference ifr, String purpoLoan) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		//String loggedInUser = ifr.getUserName().toUpperCase();
		String userName = "";
		// String querySancAuhority = "SELECT GRP_ID FROM LOS_M_USER WHERE
		// UPPER(EMPLOYEE_ID)='" + loggedInUser + "'";
		
		String purpoLoanFirst ="";
		String queryBranchCodeAndVehicleCat = "select PURPOSE_LOAN_VL from SLOS_STAFF_TRN "
				+ "where WINAME ='" + processInstanceId + "'";
		List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);
		Log.consoleLog(ifr, "queryBranchCodeAndVehicleCat==> " + queryBranchCodeAndVehicleCat);
		if (!list.isEmpty()) {
			purpoLoanFirst = list.get(0).get(0);
		}
		
		
		if (purpoLoanFirst.trim().equalsIgnoreCase("two")) {
			 String queryforSanctionId = "select BRANCH_CHECKER_ID from SLOS_STAFF_TRN where WINAME = '"+processInstanceId+"'";
	         Log.consoleLog(ifr, "inside queryforSanctionId" + queryforSanctionId);
	         List< List< String>> result = ifr.getDataFromDB(queryforSanctionId);
	         if(!result.isEmpty())
	         {
	        	 userName =result.get(0).get(0);
	         }
		}
		else if (purpoLoanFirst.trim().equalsIgnoreCase("four")) {
			 String queryforSanctionId = "select RO_SANCTION_ID from SLOS_STAFF_TRN where WINAME = '"+processInstanceId+"'";
	         Log.consoleLog(ifr, "inside queryforSanctionId" + queryforSanctionId);
	         List< List< String>> result = ifr.getDataFromDB(queryforSanctionId);
	         if(!result.isEmpty())
	         {
	        	 userName =result.get(0).get(0);
	         }
		}
		else {
			 String queryforSanctionId = "select RO_SANCTION_ID from SLOS_STAFF_TRN where WINAME = '"+processInstanceId+"'";
	         Log.consoleLog(ifr, "inside queryforSanctionId" + queryforSanctionId);
	         List< List< String>> result = ifr.getDataFromDB(queryforSanctionId);
	         if(!result.isEmpty())
	         {
	        	 userName =result.get(0).get(0);
	         }
		}
		
		
		
	
		String querySancAuhority = "select GRP_ID from los_m_user where EMPLOYEE_ID = '" + userName + "'";
		Log.consoleLog(ifr, "guerySancAuhority===>" + querySancAuhority);
		List<List<String>> Result = ifr.getDataFromDB(querySancAuhority);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		String sancAuthority = "";
		if (!Result.isEmpty()) {
			sancAuthority = Result.get(0).get(0);
		}
		return sancAuthority;

		// throw new UnsupportedOperationException("Not supported yet."); // Generated
		// from
		// nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	// Added by Aravindh on 29/06/24 for getting Tenure Elongation Checkbox Flag
	public String getTenureElongation(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryTenureElongation = "SELECT TENURE_ELONGATION FROM LOS_NL_PROPOSED_FACILITY WHERE PID='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryTenureElongation===>" + queryTenureElongation);
		List<List<String>> Result = ifr.getDataFromDB(queryTenureElongation);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		String tenureElongationFlag = "";
		if (!Result.isEmpty()) {
			tenureElongationFlag = Result.get(0).get(0);
		}
		return tenureElongationFlag;

	}

	// Added by Aravindh on 29/06/24 for getting RateRegime Flag
	public String getTypeOfInterestRegime(IFormReference ifr) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryTypeOfInterest = "SELECT ROIType FROM LOS_NL_PROPOSED_FACILITY WHERE PID='" + processInstanceId
				+ "'";
		Log.consoleLog(ifr, "TypeOfIntrestRegimeQuery===>" + queryTypeOfInterest);
		List<List<String>> Result = ifr.getDataFromDB(queryTypeOfInterest);
		Log.consoleLog(ifr, "#Result===>" + Result.toString());
		String typeOfInterest = "";
		String rateRegime = "";
		if (!Result.isEmpty()) {
			typeOfInterest = Result.get(0).get(0);
			if (typeOfInterest.equalsIgnoreCase("Fixed")) {
				rateRegime = "F";
			} else if (typeOfInterest.equalsIgnoreCase("Floating")) {
				rateRegime = "L";
			}
		}
		return rateRegime;
	}

	// Function moved by Ahmed on 01-07-2024 for making it as common for LAD
	public String getSchemeIDODAD(IFormReference ifr) {
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String query = "select SchemeID from LOS_NL_PROPOSED_FACILITY where pid='" + PID + "'";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
		if (!list.isEmpty()) {
			return list.get(0).get(0);
		}
		return "";
	}

	public String getCollateralId(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryCollId = "select COLLATERALID from SLOS_COLLATERAL_DETAILS " + "where WINAME ='" + processInstanceId
				+ "'";
		List<List<String>> list = cf.mExecuteQuery(ifr, queryCollId, "Collateral ID:");
		if (!list.isEmpty()) {
			return list.get(0).get(0);
		}
		return "";

	}

	// Added by Ahmed on 03-07-2024 for getting CBSProductCode
	public String getCBSProductCode(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", processInstanceId);
		List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
		String loan_selected = loanSelected.get(0).get(0);
		Log.consoleLog(ifr, "loan type==>" + loan_selected);
		String cbsProdCode = "";
		if (loan_selected.equalsIgnoreCase("Canara Budget")) {
			String productCode = getProductCode(ifr);
			String subProductCode = getSubProductCode(ifr);
			Log.consoleLog(ifr, " productCode:" + productCode);
			Log.consoleLog(ifr, " subProductCode:" + subProductCode);
			String queryCBSProCode = "SELECT CBSPRODUCTCODE FROM LOS_M_PRODUCT_RLOS " + "WHERE PRODUCTCODE='"
					+ productCode + "' " + "AND SUBPRODUCTCODE='" + subProductCode + "'";
			List<List<String>> list = cf.mExecuteQuery(ifr, queryCBSProCode, "queryCBSProCode:");
			if (!list.isEmpty()) {
				cbsProdCode = list.get(0).get(0);
			}
			Log.consoleLog(ifr, " cbsProdCode:" + cbsProdCode);
			if (!cbsProdCode.equalsIgnoreCase("")) {
				String queryUpdateCBSPrCode = "UPDATE LOS_EXT_TABLE " + "SET CBSPRODUCTCODE='" + cbsProdCode + "' "
						+ "WHERE PID='" + processInstanceId + "'";
				Log.consoleLog(ifr, "queryUpdateCBSPrCode===>" + queryUpdateCBSPrCode);
				ifr.saveDataInDB(queryUpdateCBSPrCode);
				// modified by Sharon on 04/07/2024
				ifr.setValue("CBSPRODUCTCODE", cbsProdCode);
				String cbsProdCod = ifr.getValue("CBSPRODUCTCODE").toString();
				Log.consoleLog(ifr, "cbsProdCod===>" + cbsProdCod);

			}
		}
		// added by keerthana for pension journey on 04/07/2024
		if (loan_selected.equalsIgnoreCase("Canara Pension")) {
			String productCode = getProductCode(ifr);
			String subProductCode = getSubProductCode(ifr);
			Log.consoleLog(ifr, " productCode:" + productCode);
			Log.consoleLog(ifr, " subProductCode:" + subProductCode);
			String queryCBSProCode = "SELECT CBSPRODUCTCODE FROM LOS_M_PRODUCT_RLOS " + "WHERE PRODUCTCODE='"
					+ productCode + "' " + "AND SUBPRODUCTCODE='" + subProductCode + "'";
			List<List<String>> list = cf.mExecuteQuery(ifr, queryCBSProCode, "queryCBSProCode:");
			if (!list.isEmpty()) {
				cbsProdCode = list.get(0).get(0);
			}
			Log.consoleLog(ifr, " cbsProdCode:" + cbsProdCode);
			if (!cbsProdCode.equalsIgnoreCase("")) {
				String queryUpdateCBSPrCode = "UPDATE LOS_EXT_TABLE " + "SET CBSPRODUCTCODE='" + cbsProdCode + "' "
						+ "WHERE PID='" + processInstanceId + "'";
				Log.consoleLog(ifr, "queryUpdateCBSPrCode===>" + queryUpdateCBSPrCode);
				ifr.saveDataInDB(queryUpdateCBSPrCode);
				ifr.setValue("CBSPRODUCTCODE", cbsProdCode);
				Log.consoleLog(ifr, "cbsProdCod===>" + cbsProdCode);

			}
		}
		// added by Sharon for VL journey on 03/07/2024
		if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
			Log.consoleLog(ifr, "inside getCBSProductCodeVL:");
			String Purpose = null;
			String PurposeQuery = ConfProperty.getQueryScript("PurposeQueryVL").replaceAll("#ProcessInstanceId#",
					processInstanceId);
			List<List<String>> PurposePortal = cf.mExecuteQuery(ifr, PurposeQuery,
					"Execute query for fetching Purpose data from portal");
			if (PurposePortal.size() > 0) {
				Purpose = PurposePortal.get(0).get(0);
			}
			Log.consoleLog(ifr, "Purpose:" + Purpose);
			String subProductCode = mGetSubproductVL(ifr, Purpose);
			Log.consoleLog(ifr, "subProductCode:" + subProductCode);
			String productCode = mGetProductCodeVL(ifr, Purpose);
			Log.consoleLog(ifr, "ProductCode:" + productCode);
			String queryCBSProCode = "SELECT CBSPRODUCTCODE FROM LOS_M_PRODUCT_RLOS " + "WHERE PRODUCTCODE='"
					+ productCode + "' " + "AND SUBPRODUCTCODE='" + subProductCode + "'";
			List<List<String>> list = cf.mExecuteQuery(ifr, queryCBSProCode, "queryCBSProCode:");
			if (!list.isEmpty()) {
				cbsProdCode = list.get(0).get(0);
			}
			Log.consoleLog(ifr, "cbsProdCode:" + cbsProdCode);
			if (!cbsProdCode.equalsIgnoreCase("")) {
				String queryUpdateCBSPrCode = "UPDATE LOS_EXT_TABLE " + "SET CBSPRODUCTCODE='" + cbsProdCode + "' "
						+ "WHERE PID='" + processInstanceId + "'";
				Log.consoleLog(ifr, "queryUpdateCBSPrCode===>" + queryUpdateCBSPrCode);
				ifr.saveDataInDB(queryUpdateCBSPrCode);
				ifr.setValue("CBSPRODUCTCODE", cbsProdCode);
				Log.consoleLog(ifr, "cbsProdCod===>" + cbsProdCode);
			}
		}
		return cbsProdCode;
	}

	// Added by Aravindh on 04/07/24 to get
	public String getDateSuperannuationCIM09(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String F_Key = Fkey(ifr, "B");
		String queryDateSuperannuation = "SELECT TO_CHAR(DATEOFSUPERANNUATION,'YYYYMMDD') FROM LOS_NL_OCCUPATION_INFO WHERE F_KEY ='"
				+ F_Key + "'";
		List<List<String>> list = cf.mExecuteQuery(ifr, queryDateSuperannuation, "queryDateSuperannuation");
		if (!list.isEmpty()) {
			return list.get(0).get(0);
		}
		return "";
	}

	// added by keerthana for pension journey on 04/07/2024
	public String getPensionSchemeId(IFormReference ifr, String PID) {
		Log.consoleLog(ifr, "PensionPortalCustomCode:getPensionSchemeId:PID==>" + PID);
		String flag = "";
		String custAge = "";
		String mobile_no = "";
		String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
		List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query, "Execute query for fetching loan selected ");
		String loan_selected = loanSelected.get(0).get(0);
		Log.consoleLog(ifr, "loan type==>" + loan_selected);
		String MobileData_Query = ConfProperty.getQueryScript("PORTALRMSENDOTP").replaceAll("#WINAME#", PID);
		List<List<String>> MobileDataList = cf.mExecuteQuery(ifr, MobileData_Query, "MobileDataList:");
		if (MobileDataList.size() > 0) {
			mobile_no = MobileDataList.get(0).get(0);
			Log.consoleLog(ifr, "MobileNo==>" + mobile_no);
		}
		Log.consoleLog(ifr, "IFORM MOBILE NUMBER" + mobile_no);
		String query5 = ConfProperty.getQueryScript("PENSIONCUSTID").replaceAll("#ProcessInstanceId#", PID);
		Log.consoleLog(ifr, "pension custid query : " + query5);
		List<List<String>> customerId = cf.mExecuteQuery(ifr, query5, "Execute custid Query->");
		String customerid = "";
		if (!customerId.isEmpty()) {
			customerid = customerId.get(0).get(0);
		}
		Log.consoleLog(ifr, "CUSTOMERID : " + customerid);
		String strquery = "select FLGCUSTYPE from LOS_TRN_Customersummary where MOBILENUMBER = '" + mobile_no
				+ "' and CUSTOMERID = '" + customerid + "' and loanSelected = '" + loan_selected + "'";
		List<List<String>> list1 = cf.mExecuteQuery(ifr, strquery, "Check Pension temp Data  Query:");
		if (!list1.isEmpty()) {
			flag = list1.get(0).get(0);
			Log.consoleLog(ifr, "flag value:" + flag);
		}
		String Variant = "";
		if (flag.equalsIgnoreCase("R4") || flag.equalsIgnoreCase("Z4") || flag.equalsIgnoreCase("Z8")) {
			Variant = "Exs";
		} else if (flag.equalsIgnoreCase("Z5") || flag.equalsIgnoreCase("Z6") || flag.equalsIgnoreCase("R")
				|| flag.equalsIgnoreCase("S2") || flag.equalsIgnoreCase("Z3")) {
			Variant = "GEN";
		} else {
			Variant = "";
		}
		String pupose = ifr.getValue("P_CP_LD_Purpose").toString();
		String schemeId = "";
		String schemeID = "select schemeid from los_m_product_rlos where VARIANTCODE= '" + Variant
				+ "' and purposecode = '" + pupose + "'";
		List<List<String>> schemeList = cf.mExecuteQuery(ifr, schemeID, "PenAgeschemeIdQuery:");
		if (schemeList.size() > 0) {
			schemeId = schemeList.get(0).get(0);
			Log.consoleLog(ifr, "schemeId==>" + schemeId);
		}
		UpdateSchemeIDCommon(ifr, schemeId, PID);
		return schemeId;
	}
	// added by keerthana for pension journey on 04/07/2024

	public String getCollateralDetails(IFormReference ifr, String pid, String columnName) {
		// Query modified by Ahmed on 15-07-2024 for getting Single Row
		// String query = "SELECT " + columnName + " FROM LOS_NL_COLLATERAL_VEHICLES
		// WHERE PID ='" + pid + "'";
		String query = ConfProperty.getQueryScript(columnName).replaceAll("#PID#", pid);
		// "SELECT " + columnName + " FROM LOS_NL_COLLATERAL_VEHICLES WHERE PID ='" +
		// pid + "' AND ROWNUM=1";
		Log.consoleLog(ifr, "get Collateral Details query : " + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (!result.isEmpty() && result.get(0).get(0) != null) {
			Log.consoleLog(ifr, "result of " + columnName + " :" + result.get(0).get(0));
			return result.get(0).get(0);
		}
		Log.consoleLog(ifr, "result of " + columnName + " :" + result.get(0).get(0));
		return "0";
	}

	// added by keerthana on 08/07/2024 for common schemeid update
	public void UpdateSchemeIDCommon(IFormReference ifr, String schemeId, String PID) {
		Log.consoleLog(ifr, "inside try block::::UpdateSchemeIDCommon::getting schemeid::: " + schemeId + "," + PID);
		String UpdateSchmIdquery = ConfProperty.getQueryScript("UpdateSchmIdquery")
				.replaceAll("#ProcessInsanceId#", PID).replaceAll("#SchemeID#", schemeId);
		Log.consoleLog(ifr, "PortalCommonMethods:UpdateSchemeIDCommon-> UpdateSchmIdquery:" + UpdateSchmIdquery);
		cf.mExecuteQuery(ifr, UpdateSchmIdquery, "UpdateSchemeIDCommon ::: Execute query for update schemeid");
	}

	public String getCustomerIDFromPAPLMaster(IFormReference ifr, String mobNumber) {

		String query = "SELECT CUSTOMER_ID FROM LOS_MST_PAPL WHERE MOBILE_NO='" + mobNumber + "' AND ROWNUM=1 ";
		List<List<String>> list = cf.mExecuteQuery(ifr, query, "getCustomerIDFromPAPLMaster:");
		if (list.size() > 0) {
			return list.get(0).get(0);
		}
		return "";
	}

	public void controlMandatory(IFormReference ifr, String visiblefeild) {
		String[] FieldVisibMan = visiblefeild.split(",");
		for (String FieldVisiblemandatory : FieldVisibMan) {
			ifr.setStyle(FieldVisiblemandatory, "mandatory", "true");
		}
	}

	public JSONArray getTotalChargesNonSTP(IFormReference ifr, String SchemeId, String loanAmount, String Statecode,
			String loantenure, String loanAccountNumber, String TBranch) {

		JSONArray jsonArr = new JSONArray();
		JSONArray jsonArr1 = new JSONArray();
		BigDecimal total = new BigDecimal("0");

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String query = "SELECT AMOUNT,FEECODE,FEES_DESCRIPTION FROM LOS_NL_FEE_CHARGES "
//                + "WHERE PID='" + processInstanceId + "' ORDER BY INSERTIONORDERID ASC";
		String query = ConfProperty.getQueryScript("FUNDTRANSFER_NONSTP").replaceAll("#WINAME#", processInstanceId);

		List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:getTotalChargesNonSTP");
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				String amount = result.get(i).get(0);
				BigDecimal transactionAmount = new BigDecimal(amount);
				String transactionFeeCode = result.get(i).get(1);
				String glCode = getGLCodeDesc(ifr, transactionFeeCode);
				String glCodeDesc = result.get(i).get(2);

				JSONObject obj = new JSONObject();
				obj.put("AccountNumber", glCode);
				obj.put("AccountType", "2");

				if ((glCodeDesc.contains("SL-DDE STAMP COLL-POOLING GL")) || (glCodeDesc.contains("122422154"))) {
					obj.put("AccountBranch", "00029");
				} else {
					obj.put("AccountBranch", TBranch);
				}
				obj.put("DrCrFlag", "C");
				obj.put("Narrative", processInstanceId + " " + glCodeDesc);
				obj.put("TransactionAmount", String.valueOf(transactionAmount));
				obj.put("TransactionCurrency", "INR");
				obj.put("chequeNumber", "");
				obj.put("chequeDate", "");
				obj.put("forcePost", "N");
				obj.put("cashTransfer", "Y");
				jsonArr.add(obj);
				total = total.add(transactionAmount);
			}
			JSONObject obj = new JSONObject();
			obj.put("AccountNumber", loanAccountNumber);
			obj.put("AccountType", "1");
			obj.put("AccountBranch", TBranch);
			obj.put("DrCrFlag", "D");
			obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId());
			obj.put("TransactionAmount", String.valueOf(total));
			obj.put("TransactionCurrency", "INR");
			obj.put("chequeNumber", "");
			obj.put("chequeDate", "");
			obj.put("forcePost", "Y");
			obj.put("cashTransfer", "Y");
			jsonArr1.add(obj);
			for (int i = 0; i < jsonArr.size(); i++) {
				obj = (JSONObject) jsonArr.get(i);
				jsonArr1.add(obj);
			}
		}
		return jsonArr1;
	}

	public String getGLCodeDesc(IFormReference ifr, String feeCode) {

		String qryGetGLCode = "SELECT GLCODE,GLDESC FROM SLOS_STAFF_HL_FEESMASTER WHERE FEECODE='" + feeCode + "'";
		List<List<String>> list = cf.mExecuteQuery(ifr, qryGetGLCode, "qryGetGLCode:getGLCodeDesc");
		if (!list.isEmpty()) {
			return list.get(0).get(0);
		}
		return "";
	}

	public void setbranchcode(IFormReference ifr) {
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		String branchcodeQuery = ConfProperty.getQueryScript("branchcodeQuery").replaceAll("#ProcessInstanceId#",
				ProcessInstanceId);
		List<List<String>> branchcodeData = cf.mExecuteQuery(ifr, branchcodeQuery,
				"Execute query for fetching branch code data");
		int branchCode1 = Integer.parseInt(branchcodeData.get(0).get(0));
		String branchCode = String.format("%05d", branchCode1);
		Log.consoleLog(ifr, "branchCode" + branchCode);
		ifr.setValue("Q_ProcessingBranchCode", branchCode);
		Log.consoleLog(ifr, "Q_ProcessingBranchCode::: " + ifr.getValue("Q_ProcessingBranchCode"));
	}

	// Added by Ahmed on 08-08-2024
	public String getSelectedAccStateName(IFormReference ifr, String journeyType) {
		Log.consoleLog(ifr, "getSelectedAccStateName Started...");
		Log.consoleLog(ifr, "journeyType=====>" + journeyType);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String branchCode = "";
		String queryBranchCode = "";

		try {

//            if (journeyType.equalsIgnoreCase("PAPL")) {
//                queryBranchCode = ConfProperty.getQueryScript("PAPL_GETHOMEBRCODEQRY")
//                        .replace("#WINAME#", processInstanceId);
//            } else if ((journeyType.equalsIgnoreCase("TL")) || (journeyType.equalsIgnoreCase("OD"))) {
//                queryBranchCode = ConfProperty.getQueryScript("LAD_GETHOMEBRCODEQRY")
//                        .replace("#WINAME#", processInstanceId);
//            } else {
//                queryBranchCode = ConfProperty.getQueryScript("GETHOMEBRCODEQRY")
//                        .replace("#WINAME#", processInstanceId);
//            }
//            queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
//                    + " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
//            Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
//            List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
//            if (!result.isEmpty()) {
//                branchCode = result.get(0).get(0);
//            }
			String querySelectedBranchCode = "";
			if (journeyType.contains("VL")) {
				querySelectedBranchCode = "select BRANCH_CODE from SLOS_STAFF_TRN  where WINAME='" + processInstanceId
						+ "' ";
			} else {
				querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='"
						+ processInstanceId + "' ";
			}
			Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
			List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
			if (!branchCodeResult.isEmpty()) {
				branchCode = branchCodeResult.get(0).get(0);
			}

			if (branchCode.equalsIgnoreCase("")) {
				Log.consoleLog(ifr, "Branch Code founds to be Empty for the WorkItem");
				return RLOS_Constants.ERROR;
			} else {

				String city = "";
				String state = "";
				String zipCode = "";

				if (branchCode.length() == 4) {
					branchCode = "0" + branchCode;// Appending Zero in the master for 4 digit branchcode
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
				return state;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getSelectedAccStateName=>..." + e);
		}

		return RLOS_Constants.ERROR;
	}

	public String getSelectedStateCodeNo(IFormReference ifr, String stateName) {
		Log.consoleLog(ifr, "PortalCommonMethods:getSelectedStateCodeNo->Starting.");

		String stateCodeNo = "";
		try {
			String qryGetStateCodeNo = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
					+ "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + stateName + "')) AND ROWNUM=1";
			Log.consoleLog(ifr, "qryGetStateCodeNo==>" + qryGetStateCodeNo);
			List<List<String>> result = ifr.getDataFromDB(qryGetStateCodeNo);
			Log.consoleLog(ifr, "PortalCommonMethods:getSelectedStateCodeNo->result " + result);
			if (!result.isEmpty()) {
				stateCodeNo = result.get(0).get(0);
				return stateCodeNo;
			} else {
				Log.consoleLog(ifr, "StateCode founds to be empty for " + stateName + " in LOS_MST_STATE table.");
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "PortalCommonMethods:getSelectedStateCodeNo->Exception " + e);
		}
		return RLOS_Constants.ERROR;
	}

	public String getLoanSelected(IFormReference ifr) {
		Log.consoleLog(ifr, "PortalCommonMethods:getLoanSelected->Started ");
		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String qryGetLoanSelected = "select LOAN_SELECTED from los_ext_table where PID='" + processInstanceId + "'";
			List<List<String>> result = ifr.getDataFromDB(qryGetLoanSelected);
			Log.consoleLog(ifr, "PortalCommonMethods:qryGetLoanSelected->result " + result);
			if (!result.isEmpty()) {
				return result.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "PortalCommonMethods:getLoanSelected->Exception " + e);
		}
		return "";
	}

	public String getCurrentDate() {
		Date d1 = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String currentDate = dateFormat.format(d1);
		return currentDate;
	}

	public String mGetSchemeIDFORVL(IFormReference ifr, String processInstanceId) {
		String query = "select schemeid from los_m_product_rlos where productcode='STAFFVL' and subproductcode ='STAFFVEHICLE' and ISACTIVE='Y'";
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String parseDate(IFormReference ifr, String dateStr) {
		String targetFormate = getTargetFoemte(ifr);
		String availablePatterns = "dd-MM-yyyy,yyyyMMdd,yyyy/MM/dd,dd/MM/yyyy";
		DateFormat targetFormat = new SimpleDateFormat(targetFormate);
		String[] patterns = availablePatterns.split(",");
		for (String pattern : patterns) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				sdf.setLenient(false); // Enforce strict parsing
				Date date = sdf.parse(dateStr);
				String format = targetFormat.format(date);
				return format;
			} catch (Exception e) {
				Log.consoleLog(ifr, "Date parsing error");
			}
		}
		return "";
	}

	public String getTargetFoemte(IFormReference ifr) {

		String env = getParamValue(ifr, "DATEFORMATE", "ENV");
		Log.consoleLog(ifr, "env :" + env);
		String targetFormate = "";
		if (env.equalsIgnoreCase("PROD")) {
			targetFormate = "yyyy-MM-dd";
		} else {
			targetFormate = "dd/MM/yyyy";
		}
		return targetFormate;
	}

	public String returnCustomErrorMessage(IFormReference ifr, String customErrorMessage) {
		if ((customErrorMessage.equalsIgnoreCase("")) || (customErrorMessage.equalsIgnoreCase("ERROR:"))) {
			customErrorMessage = "Technical glitch, Try after sometime!";
		}
		JSONObject message = new JSONObject();
		message.put("showMessage", cf.showMessage(ifr, "", "error", customErrorMessage));
		return message.toString();
	}

	public String getProductCodeByProductName(IFormReference ifr) {
		String query = "SELECT HL_PRODUCT from SLOS_STAFF_HOME_TRN WHERE WINAME= '"
				+ ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
		Log.consoleLog(ifr, "PCM::::getProductCode:" + query);
		List<List<String>> result = cf.mExecuteQuery(ifr, query, "getproductcode");
		if (!result.isEmpty()) {
			return result.get(0).get(0);
		}
		return "";
	}

	public String returnErrorAPIThroughExecute2(IFormReference ifr, String customErrorMessage) {
		if ((customErrorMessage.equalsIgnoreCase("")) || (customErrorMessage.equalsIgnoreCase("ERROR:"))) {
			customErrorMessage = "Technical glitch, Try after sometime!";
		}
		String objRes = "{\"showMessage\":" + cf.showMessage(ifr, "", "error", customErrorMessage)
				+ ",\"NavigationNextClick\":\"false\"}";
		return objRes;
	}

	public String returnErrorAPIThroughExecute(IFormReference ifr, String customErrorMessage) {
		if ((customErrorMessage.equalsIgnoreCase("")) || (customErrorMessage.equalsIgnoreCase("ERROR:"))) {
			customErrorMessage = "Technical glitch, Try after sometime!";
		}
		String objRes = "{\"showMessage\":" + cf.showMessage(ifr, "", "error", customErrorMessage)
				+ ",\"NavigationNextClick\":\"false\"}";
		return objRes;
	}

	// GET MOBILE NO ON INSERTIONORDERID
	public String getMobileNumberonInsertionOrderId(IFormReference ifr, String insertionOrderId) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
			String query = "select b.mobileno from LOS_NL_BASIC_INFO a, " + " los_l_basic_info_i b where a.pid='"
					+ processInstanceId + "' " + " and a.f_key=b.f_key and a.insertionorderid='" + insertionOrderId
					+ "'";

			List<List<String>> list = cf.mExecuteQuery(ifr, query,
					"PortalCommonMethods:getMobileNumberonInsertionOrderId->Get Mobile: ");
			if (!list.isEmpty()) {
				return list.get(0).get(0);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr,
					"PortalCommonMethods:getMobileNumberonInsertionOrderId->Exception in getCustomerIDCB::" + e);
			Log.errorLog(ifr,
					"PortalCommonMethods:getMobileNumberonInsertionOrderId->Exception in getCustomerIDCB::" + e);
		}
		return "";
	}

	public void mImpPopulatePortalHeader(IFormReference ifr, String emailId, String MobileNo, String CommAddress,
			String PerAddress, String CustName) {

		try {
			Log.consoleLog(ifr, "inside try block::::mImpPopulatePortalHeader::::: ");

			// Added by AMAN on 02-01-2025 for isNewUser check
			// StaffHLPortalCustomCodeNTB vpccntb = new StaffHLPortalCustomCodeNTB();

			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String queryResadd = "";
			String queryPeradd = "";
			// queryResadd =
			// ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#",
			// ProcessInsanceId);
			// queryPeradd =
			// ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#",
			// ProcessInsanceId);

			queryResadd = "select MAILADDRESS1, MAILADDRESS2, MAILADDRESS3, MAILINGSTATE, MAILINGCOUNTRY, MOBILENUMBER, EMAILID from los_trn_customersummary where winame='"
					+ ProcessInsanceId + "' and APPLICANTTYPE = 'B'";
			queryPeradd = "select PERMADDRESS1, PERMADDRESS2, PERMADDRESS3, PERMSTATE, PERMCOUNTRY, PERMZIP from los_trn_customersummary where winame='"
					+ ProcessInsanceId + "' and APPLICANTTYPE = 'B'";
			// Ended by AMAN on 02-01-2025 for isNewUser check
			Log.consoleLog(ifr, "PortalCommonMethods:mImpPopulatePortalHeader-> CA:" + queryResadd);
			Log.consoleLog(ifr, "PortalCommonMethods:mImpPopulatePortalHeader-> PA:" + queryPeradd);

			List<List<String>> resultResadd = ifr.getDataFromDB(queryResadd);
			List<List<String>> resultPeradd = ifr.getDataFromDB(queryPeradd);

			if (!resultResadd.isEmpty()) {
				String comAddressLine1 = resultResadd.get(0).get(0);
				String comAddressLine2 = resultResadd.get(0).get(1);
				String comAddressLine3 = resultResadd.get(0).get(2);
				String state = resultResadd.get(0).get(3);
				String country = resultResadd.get(0).get(4);
				String strmobile = resultResadd.get(0).get(5);
				String EMAILID = resultResadd.get(0).get(6);
				ifr.setValue(emailId, EMAILID);
				ifr.setValue(MobileNo, strmobile);
				ifr.setValue(CommAddress, comAddressLine1 + " , " + comAddressLine2 + " , " + comAddressLine3 + ","
						+ state + "," + country);
			}
			if (resultPeradd.size() > 0) {
				String addressLine1 = resultPeradd.get(0).get(0);
				String addressLine2 = resultPeradd.get(0).get(1);
				String addressLine3 = resultPeradd.get(0).get(2);
				String state = resultResadd.get(0).get(3);
				String country = resultResadd.get(0).get(4);
				String pincode = resultResadd.get(0).get(5);
				ifr.setValue(PerAddress, addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + state + ","
						+ country + "," + pincode);
			}
//        String FirstNameCB = "", MiddleNameCB = "", LastNameCB = "";
//        //Changes added by AMAN for FullName Display
//        String FullNameCBQuery = "";
//
//        if (vpccntb.newUserCheck(ifr, ProcessInsanceId).equalsIgnoreCase("N")) {
//            FullNameCBQuery = ConfProperty.getQueryScript("HLgetFullNameCBQueryNTB").replaceAll("#pid#", ProcessInsanceId);
//        } else {
//            FullNameCBQuery = ConfProperty.getQueryScript("getFullNameCBQuery").replaceAll("#ProcessInsanceId#", ProcessInsanceId);
//        }
//        //Changes ended by AMAN for FullName Display
//        Log.consoleLog(ifr, "mobileData query : " + FullNameCBQuery);
//        List<List<String>> FullNameCBList = ifr.getDataFromDB(FullNameCBQuery);
//        if (!FullNameCBList.isEmpty()) {
//            FirstNameCB = FullNameCBList.get(0).get(0);
//            MiddleNameCB = FullNameCBList.get(0).get(1);
//            LastNameCB = FullNameCBList.get(0).get(2);
//            if (!MiddleNameCB.isEmpty()) {
//                MiddleNameCB = MiddleNameCB.replace("null", "");
//            }
//            if (!LastNameCB.isEmpty()) {
//                LastNameCB = LastNameCB.replace("null", "");
//            }
//            String Fullname = FirstNameCB + " " + MiddleNameCB + " " + LastNameCB;
//            ifr.setValue(CustName, Fullname.replace("  ", " "));
//        }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception inside mImpPopulatePortalHeader::::" + e);
			Log.errorLog(ifr, "Exception inside mImpPopulatePortalHeader::::" + e);
		}
	}

	public String getPrimarySalaryAccountNo(IFormReference ifr, String journeyType) {

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String mobileNumber = getMobileNumber(ifr);
		String querySalAcccNoData = "";
		String F_Key = getFkey(ifr, "B");

		// querySalAcccNoData = "SELECT SELECTSALARYACCOUNT FROM LOS_NL_BASIC_INFO WHERE
		// PID = '" + processInstanceId + "' AND APPLICANTTYPE ='B'";
		querySalAcccNoData = "select casa_yn,ACCOUNT_NO from los_nl_occupation_info where f_key='" + F_Key + "'";

		// String accNoData = "select salaryacc_no from LOS_MST_PAPL where mobile_no ='"
		// + MOBILENUMBER + "'";
		// List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, accNoData,
		// "accNoData:");
		List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, querySalAcccNoData, "accNoData:");
		if (!loanAccNo.isEmpty()) {
			if (Objects.equals(loanAccNo.get(0).get(0), "Yes")) {
				return loanAccNo.get(0).get(1);
			} else {
				return "";
			}
		}
		return "";
	}

	public String getFkey(IFormReference ifr, String Applicanttype) {

		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String Fquery = "select F_KEY from los_nl_basic_info where PID='" + PID
				+ "' and Applicanttype in ('CB','G','B') and rownum=1";

		Log.consoleLog(ifr, "FKEYQUERY :::" + Fquery);
		List<List<String>> strgey = ifr.getDataFromDB(Fquery);
		String strfkey = strgey.get(0).get(0);
		Log.consoleLog(ifr, "FKEYQUERY Output :::" + strfkey);

		return strfkey;
	}

	public JSONArray getpredisbTotalCharges(IFormReference ifr) {
		JSONArray jsonArr = new JSONArray();
		JSONArray jsonArr1 = new JSONArray();
		BigDecimal total = new BigDecimal("0");
		String loanAccountNumber = "";
		String TBranch = "";

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanAccountNumberquery = "select SALARY_ACC_NUMBER from slos_staff_home_trn where winame = '"
				+ processInstanceId + "'";
		List<List<String>> loanAccountNumberresult = cf.mExecuteQuery(ifr, loanAccountNumberquery,
				"loanAccountNumberquery");
		if (loanAccountNumberresult.isEmpty()) {
			return jsonArr;
		} else {
			loanAccountNumber = loanAccountNumberresult.get(0).get(0);
		}

		String tbranchquery = "select BRANCHCODE from los_m_branch where BRANCHNAME in (select BRANCH_NAME from slos_staff_home_trn where winame='"
				+ processInstanceId + "')";
		List<List<String>> tbranchqueryRes = cf.mExecuteQuery(ifr, tbranchquery, "tbranchquery");
		if (tbranchqueryRes.isEmpty()) {
			return jsonArr;
		} else {
			TBranch = tbranchqueryRes.get(0).get(0);
		}
		///
//      String Query = ConfProperty.getQueryScript("GETLOANDETAILSINFO").replaceAll("#WINAME#", processInstanceId);
//      List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, "LOANDETAILSQUERYFROMBACKOFFICE");
//
//      if (Output3.size() > 0) {
//
//          loanAccountNumber = Output3.get(0).get(2);
//      }
//      String query = "SELECT AMOUNT,FEECODE,FEES_DESCRIPTION FROM LOS_NL_FEE_CHARGES "
//              + "WHERE PID='" + processInstanceId + "' ORDER BY INSERTIONORDERID ASC";
		String query = "SELECT TOTAL, FEECODE, FEES_DESCRIPTION   FROM LOS_NL_FEE_CHARGES WHERE PID='"
				+ processInstanceId + "'";
		List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:getTotalChargesNonSTP");
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				String amount = result.get(i).get(0);
				BigDecimal transactionAmount = new BigDecimal(amount);
				if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
					// If amount is zero or negative, continue the loop
					continue;
				}
				String transactionFeeCode = result.get(i).get(1);
				String glCode = getGLCodeDesc(ifr, transactionFeeCode);
				String glCodeDesc = getGLCodedescrip(ifr, transactionFeeCode);
				String Stamdlcode = getConstantValue(ifr, "CBSFUNDTRANS", "ESTAMPCODE");
				String Stamdldesc = getConstantValue(ifr, "CBSFUNDTRANS", "ESTAMPDESC");
				JSONObject obj = new JSONObject();
				obj.put("AccountNumber", glCode);
				obj.put("AccountType", "2");
				if ((glCodeDesc.contains(Stamdldesc)) || (glCode.contains(Stamdlcode))) {
					obj.put("AccountBranch", "00029");
				} else {
					obj.put("AccountBranch", TBranch);
				}
				obj.put("DrCrFlag", "C");
				obj.put("Narrative", processInstanceId + " " + glCodeDesc);
				obj.put("TransactionAmount", String.valueOf(transactionAmount));
				obj.put("TransactionCurrency", "INR");
				obj.put("chequeNumber", "");
				obj.put("chequeDate", "");
				obj.put("forcePost", "N");
				obj.put("cashTransfer", "Y");
				jsonArr.add(obj);
				total = total.add(transactionAmount);
			}
			JSONObject obj = new JSONObject();
			obj.put("AccountNumber", loanAccountNumber);
			obj.put("AccountType", "1");
			obj.put("AccountBranch", TBranch);
			obj.put("DrCrFlag", "D");
			obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId());
			obj.put("TransactionAmount", String.valueOf(total));
			obj.put("TransactionCurrency", "INR");
			obj.put("chequeNumber", "");
			obj.put("chequeDate", "");
			obj.put("forcePost", "Y");
			obj.put("cashTransfer", "Y");
			jsonArr1.add(obj);
			for (int i = 0; i < jsonArr.size(); i++) {
				obj = (JSONObject) jsonArr.get(i);
				jsonArr1.add(obj);
			}
		}
		return jsonArr1;
	}

	public String getGLCodedescrip(IFormReference ifr, String feeCode) {

		String qryGetGLCode = "SELECT GLCODE,GLDESC FROM LOS_M_FEE_CHARGES WHERE FEECODE='" + feeCode + "'";
		List<List<String>> list = cf.mExecuteQuery(ifr, qryGetGLCode, "qryGetGLCode:getGLCodeDesc");
		if (!list.isEmpty()) {
			return list.get(0).get(1);
		}
		return "";
	}

	public JSONArray getpredisbTotalChargesSHL(IFormReference ifr) {
	    JSONArray jsonArr = new JSONArray();
	    JSONArray jsonArr1 = new JSONArray();
	    BigDecimal total = new BigDecimal("0");
	    String loanAccountNumber = "";
	    String TBranch = "";
	    String TBranchD = "";

	    String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	    String loanAccountNumberquery = "select SALARY_ACC_NUMBER from slos_staff_home_trn where winame = '"
	            + processInstanceId + "'";
	    List<List<String>> loanAccountNumberresult = cf.mExecuteQuery(ifr, loanAccountNumberquery,
	            "loanAccountNumberquery");
	    if (loanAccountNumberresult.isEmpty()) {
	        return jsonArr;
	    } else {
	        loanAccountNumber = loanAccountNumberresult.get(0).get(0);
	    }
	    	    
	    String querySelectedBranchCode = "SELECT DISB_BRANCH FROM SLOS_TRN_LOANDETAILS WHERE PID = '" + processInstanceId + "'";
		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		if (!branchCodeResult.isEmpty()) {
			TBranchD = branchCodeResult.get(0).get(0);
		} else {
			return jsonArr;
		}

	    String tbranchquery = "select BRANCHCODE from los_m_branch where BRANCHNAME in (select BRANCH_NAME from slos_staff_home_trn where winame='"
	            + processInstanceId + "')";
	    List<List<String>> tbranchqueryRes = cf.mExecuteQuery(ifr, tbranchquery, "tbranchquery");
	    if (tbranchqueryRes.isEmpty()) {
	        return jsonArr;
	    } else {
	        TBranch = tbranchqueryRes.get(0).get(0);
	    }
	    String query = "SELECT AMOUNT, FEECODE, FEES_DESCRIPTION FROM LOS_NL_FEE_CHARGES WHERE PID='"+processInstanceId+"' AND FEES_DESCRIPTION in ('Cibil Charges', 'Equifax Charges', 'Experian Charges', 'Crif Charges')";
	    
	    List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:getTotalChargesNonSTP");
	    if (!result.isEmpty()) {
	        for (int i = 0; i < result.size(); i++) {
	            String amount = result.get(i).get(0);
	            BigDecimal transactionAmount = new BigDecimal(amount);
	            if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
	                continue;
	            }
	            String transactionFeeCode = result.get(i).get(1);
	            String glCode = getGLCodeDescSHL(ifr, transactionFeeCode);
	            String glCodeDesc = getGLCodedescripSHL(ifr, transactionFeeCode);
	            String Stamdlcode = getConstantValue(ifr, "CBSFUNDTRANS", "ESTAMPCODE");
	            String Stamdldesc = getConstantValue(ifr, "CBSFUNDTRANS", "ESTAMPDESC");
	            JSONObject obj = new JSONObject();
	            obj.put("AccountNumber", glCode);
	            obj.put("AccountType", "2");
	            if ((glCodeDesc.contains(Stamdldesc)) || (glCode.contains(Stamdlcode))) {
	                obj.put("AccountBranch", "00029");
	            } else {
	                obj.put("AccountBranch", TBranch);
	            }
	            obj.put("DrCrFlag", "C");
	            obj.put("Narrative", processInstanceId + " " + glCodeDesc);
	            obj.put("TransactionAmount", String.valueOf(transactionAmount));
	            obj.put("TransactionCurrency", "INR");
	            obj.put("chequeNumber", "");
	            obj.put("chequeDate", "");
	            obj.put("forcePost", "N");
	            obj.put("cashTransfer", "Y");
	            jsonArr.add(obj);
	            total = total.add(transactionAmount);
	        }
	        JSONObject obj = new JSONObject();
	        obj.put("AccountNumber", loanAccountNumber);
	        obj.put("AccountType", "1");
	        obj.put("AccountBranch", TBranchD);
	        obj.put("DrCrFlag", "D");
	        obj.put("Narrative", ifr.getObjGeneralData().getM_strProcessInstanceId());
	        obj.put("TransactionAmount", String.valueOf(total));
	        obj.put("TransactionCurrency", "INR");
	        obj.put("chequeNumber", "");
	        obj.put("chequeDate", "");
	        obj.put("forcePost", "Y");
	        obj.put("cashTransfer", "Y");
	        jsonArr1.add(obj);
	        for (int i = 0; i < jsonArr.size(); i++) {
	            obj = (JSONObject) jsonArr.get(i);
	            jsonArr1.add(obj);
	        }
	    }
	    return jsonArr1;
	}

	public String getGLCodeDescSHL(IFormReference ifr, String feeCode) {

	    String qryGetGLCode = "SELECT GLCODE FROM slos_staff_hl_feesmaster WHERE FEECODE='" + feeCode + "'";
	    List<List<String>> list = cf.mExecuteQuery(ifr, qryGetGLCode, "qryGetGLCode:getGLCodeDesc");
	    if (!list.isEmpty()) {
	        return list.get(0).get(0);
	    }
	    return "";
	}

	public String getGLCodedescripSHL(IFormReference ifr, String feeCode) {

	    String qryGetGLCode = "SELECT GLDESC FROM slos_staff_hl_feesmaster WHERE FEECODE='" + feeCode + "'";
	    List<List<String>> list = cf.mExecuteQuery(ifr, qryGetGLCode, "qryGetGLCode:getGLCodeDesc");
	    if (!list.isEmpty()) {
	        return list.get(0).get(0);
	    }
	    return "";
	}

	public String getTransactionBranch(IFormReference ifr) {
	    String branchCode = "";
	    String processingUser = ifr.getUserName();
	    Log.consoleLog(ifr, "getTransactionBranch/getTransactionBranch" + processingUser);
	    String queryProcessingUserBranchDetails = "select branchcode "
	            + "from los_m_branch b, los_m_user u, pdbuser p "
	            + "where u.employee_id=p.username "
	            + "and u.office_code=b.branchcode "
	            + "and username= '" + processingUser + "'";
	    Log.consoleLog(ifr, "getTransactionBranch/queryProcessingUserBranchDetails" + queryProcessingUserBranchDetails);
	    List<List<String>> dataToPopulate = ifr.getDataFromDB(queryProcessingUserBranchDetails);
	    Log.consoleLog(ifr, "getTransactionBranch/dataToPopulate" + dataToPopulate);

	    if (dataToPopulate.size() > 0) {
	        branchCode = dataToPopulate.get(0).get(0);
	    }
	    Log.consoleLog(ifr, "getTransactionBranch/branchCode" + branchCode);

	    return branchCode;
	}

	public String getStateCodeByBranch(IFormReference ifr, String branchCode) {
	    String stateName = "";
	    String stateCode = "";
	    Log.consoleLog(ifr, "getStateCodeByBranch/branchCode" + branchCode);
	    String queryProcessingBranchDetails = "select STATE "
	            + "from los_m_branch "
	            + "where TO_CHAR(branchcode, 'FM00000') = TO_CHAR('"+branchCode+"', 'FM00000')";
	    Log.consoleLog(ifr, "getStateCodeByBranch/queryProcessingBranchDetails" + queryProcessingBranchDetails);
	    List<List<String>> dataToPopulate = ifr.getDataFromDB(queryProcessingBranchDetails);
	    Log.consoleLog(ifr, "getStateCodeByBranch/dataToPopulate" + dataToPopulate);

	    if (dataToPopulate.size() > 0) {
	        stateName = dataToPopulate.get(0).get(0);
	    } else {
	        return "";
	    }


	    Log.consoleLog(ifr, "getTransactionBranchStateCode/stateName" + stateName);


	    String featchStateCodeQuery = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE " + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('"
	            + stateName + "')) AND ROWNUM=1";
	    Log.consoleLog(ifr, "getTransactionBranchStateCode/" + featchStateCodeQuery);
	    List<List<String>> featchStateCodeList = ifr.getDataFromDB(featchStateCodeQuery);
	    Log.consoleLog(ifr, "#Result3===>" + featchStateCodeList.toString());
	    if (featchStateCodeList.size() > 0) {
	        stateCode = featchStateCodeList.get(0).get(0);
	    }

	    return stateCode;

	}

	  public String generateRandomNumber() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

}
