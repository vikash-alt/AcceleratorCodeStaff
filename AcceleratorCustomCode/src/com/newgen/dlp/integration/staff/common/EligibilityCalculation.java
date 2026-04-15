/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.staff.common;

import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.common.EligibilityCalculationInterface;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.LoanAmtInWords;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.staffHL.StaffHLPortalCustomCode;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.json.simple.JSONObject;

public class EligibilityCalculation implements EligibilityCalculationInterface {
	CommonFunctionality cf = new CommonFunctionality();

	@Override
	public JSONObject EligibilityCalculationAsLTVNewVehicle(IFormReference ifr) {
		Log.consoleLog(ifr, "EligibilityCalculationAsLTVNewVehicle");
		JSONObject json = new JSONObject();
		json.put("Error", "");
		json.put("Success", "");
		String prodSchemeORDesc = "";
		double ltv = 0.0;
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		// String insertCheck="SELECT winame from STAFF_VL_FINAL_ELIGIBILTY where
		// winame='"+processInstanceId+"'";
		// List<List<String>> insertRes=ifr.getDataFromDB(insertCheck);
		// if(insertRes.isEmpty()){
		// String insert="INSERT INTO STAFF_VL_FINAL_ELIGIBILTY(WINAME)
		// VAlues('"+processInstanceId+"')";
		// ifr.saveDataInDB(insert);
		// }
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Collateral Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		Validator valid = new KnockOffValidator("");
		JSONObject jobj = new JSONObject();
		jobj.put("Key", ifr.getValue("Ex_Showroom_P").toString());
		double exShowRoomPrice = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Road_Tax").toString());
		double roadTax = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Cost_Acc_VL").toString());
		double accessoryCost = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Insurance_VL").toString());
		double insuranceCost = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));
		jobj.put("Key", ifr.getValue("Booking_Amt_Paid_VL").toString());
		double bookingAmtPaid = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Reg_Charges_VL").toString());
		double registrationCharges = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Other_Exp_VL").toString());
		double otherCharge = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		jobj.put("Key", ifr.getValue("Downpayment_Amount_VL_Exp").toString());
		double downPaymentAmt = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));

		String purpose = "";
		double recommendedLoanAmt = 0.0;
		double recommendedLoanAmtForTotalCost = 0.0;
		double vehicleCostPerLtv = 0.0;
		double totalCost = 0.0;

		String staffTrn = "select purpose_loan_vl from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (staffTrnRes.isEmpty()) {
			json.put("Error", "error, technical glitch");
			return json;
		} else {
			purpose = staffTrnRes.get(0).get(0);
		}

		if (purpose.trim().toLowerCase().equals("four")) {
			recommendedLoanAmt = roadTax + Math.min(accessoryCost, AccelatorStaffConstant.ACCESSORIES_COST_LIMIT)
					+ insuranceCost + registrationCharges;

			recommendedLoanAmtForTotalCost = roadTax + accessoryCost + insuranceCost + registrationCharges
					+ otherCharge;

			Log.consoleLog(ifr, "Recommended LoanAmt==>" + recommendedLoanAmt);
			vehicleCostPerLtv = exShowRoomPrice + recommendedLoanAmt;
			totalCost = exShowRoomPrice + recommendedLoanAmtForTotalCost;
		} else if (purpose.trim().toLowerCase().equals("two")) {
			recommendedLoanAmt = roadTax
					+ Math.min(accessoryCost, AccelatorStaffConstant.ACCESSORIES_COST_TWO_WHEELER_LIMIT) + insuranceCost
					+ registrationCharges;

			recommendedLoanAmtForTotalCost = roadTax + accessoryCost + insuranceCost + registrationCharges
					+ otherCharge;

			Log.consoleLog(ifr, "Recommended LoanAmt==>" + recommendedLoanAmt);
			vehicleCostPerLtv = exShowRoomPrice + recommendedLoanAmt;
			totalCost = exShowRoomPrice + recommendedLoanAmtForTotalCost;

		}

		ifr.setStyle("TOTAL_COST_VL", "visible", "true");
		ifr.setValue("TOTAL_COST_VL", String.valueOf(totalCost));
		ifr.setStyle("TOTAL_COST_VL", "disable", "true");
		ifr.setStyle("Total_Vehicle_Cost", "visible", "true");
		ifr.setValue("Total_Vehicle_Cost", String.valueOf(vehicleCostPerLtv));
		ifr.setStyle("Total_Vehicle_Cost", "disable", "true");

		String prodSchemeQuery = "select PROD_SCHEME_DESC from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> prodSchemeRes = ifr.getDataFromDB(prodSchemeQuery);
		if (prodSchemeRes.isEmpty()) {
			json.put("Error", "Error");
			return json;
		}
		prodSchemeORDesc = prodSchemeRes.get(0).get(0).toString();
		String ltvQuery = "SELECT LTV_PERCENT FROM staff_vl_product_sheet WHERE PRD_DESC='" + prodSchemeORDesc + "'"
				+ " AND PRODUCT_CODE='SVL' AND IS_ACTIVE='Y' ";
		Log.consoleLog(ifr, "ltvQuery==>" + ltvQuery);
		List<List<String>> ltvRes = ifr.getDataFromDB(ltvQuery);
		if (ltvRes.isEmpty()) {
			json.put("Error", "Error, LTV Not found. reach out to admin");
			return json;
		}
		ltv = Double.parseDouble(ltvRes.get(0).get(0).toString());

		ifr.setStyle("LTV_Ratio_VL", "visible", "true");
		ifr.setValue("LTV_Ratio_VL", String.valueOf(ltv * 100));
		ifr.setStyle("LTV_Ratio_VL", "disable", "true");
		double minMarginReq = vehicleCostPerLtv * (1 - ltv);
		minMarginReq = Math.round(minMarginReq * 100.0) / 100.0;
		ifr.setStyle("Min_Margin_Req", "visible", "true");
		ifr.setValue("Min_Margin_Req", String.valueOf(minMarginReq));
		ifr.setStyle("Min_Margin_Req", "disable", "true");
//		if (downPaymentAmt < minMarginReq) {
//			json.put("Error",
//					"Error, margin amount is less than min margin amount. Minium margin required = " + minMarginReq);
//			return json;
//		}
		double maxLoanAsLTV = ltv * vehicleCostPerLtv;
		// double maxLoanAsLTV = totalCost - minMarginReq;
		ifr.setStyle("Max_Loan_Amt_Per_LTV", "visible", "true");
		ifr.setValue("Max_Loan_Amt_Per_LTV", String.valueOf(maxLoanAsLTV));
		ifr.setStyle("Max_Loan_Amt_Per_LTV", "disable", "true");
		if (maxLoanAsLTV <= 0.0) {
			json.put("Error", "Error, Maximum loan amount is less than 0");
			return json;
		}
		json.put("TotalCostOfVehicle", totalCost);
		json.put("MinimumMarginRequired", minMarginReq);
		json.put("MaxLoanAsLTV", maxLoanAsLTV);
		json.put("Success", "SUCCESS");
		// String updateQuery="UPDATE STAFF_VL_FINAL_ELIGIBILTY SET
		// TOTAL_COST_OF_VEHICLE='"+totalCost+"', "
		// +
		// "MIN_MARGIN_REQ='"+minMarginReq+"',MAX_LOAN_PER_LTV='"+maxLoanAsLTV+"',LTV='"+ltv+"'"
		// + " WHERE winame='"+processInstanceId+"' ";
		// ifr.saveDataInDB(updateQuery);
		return json;

	}

	@Override
	public JSONObject EligibilityCalculationAsLTVUsedVehicle(IFormReference ifr) {
		JSONObject json = new JSONObject();
		json.put("Error", "");
		json.put("Success", "");
		String prodSchemeORDesc = "";
		double ltv = 0.0;
		Validator valid = new KnockOffValidator("");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Collateral Details'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		/*
		 * String
		 * insertCheck="SELECT winame from STAFF_VL_FINAL_ELIGIBILTY where winame='"
		 * +processInstanceId+"'"; List<List<String>>
		 * insertRes=ifr.getDataFromDB(insertCheck); if(insertRes.isEmpty()){ String
		 * insert="INSERT INTO STAFF_VL_FINAL_ELIGIBILTY(WINAME) VAlues('"
		 * +processInstanceId+"')"; ifr.saveDataInDB(insert); }
		 */
		JSONObject jobj = new JSONObject();

		Log.consoleLog(ifr, "t" + ifr.getValue("Q_SLOS_STAFF_COLLATERAL_OG_PURCHASE_PRICE_USED"));
		Log.consoleLog(ifr, "t2==" + ifr.getValue("Q_SLOS_STAFF_COLLATERAL.OG_PURCHASE_PRICE_USED"));
		String temp = String.valueOf(ifr.getValue("Original_Purchase_Price"));
		jobj.put("Key", temp);

		double originalPurposePrice = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));
		Log.consoleLog(ifr, "originalPurposePrice==" + originalPurposePrice);
		jobj.put("Key", ifr.getValue("Sale_Consideration_Used").toString());
		double saleConsideration = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));
		Log.consoleLog(ifr, "saleConsideration==" + saleConsideration);
		jobj.put("Key", ifr.getValue("Pres_Mark_Val").toString());
		double valueAmt = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));
		Log.consoleLog(ifr, "valueAmt==" + valueAmt);
		double totalCostOfVehicle = Math.min(originalPurposePrice, Math.min(saleConsideration, valueAmt));
		Log.consoleLog(ifr, "totalCostOfVehicle==" + totalCostOfVehicle);
		jobj.put("Key", ifr.getValue("Downpayment_Amount_VL_Val").toString());
		double downPayment = Double.parseDouble(valid.getValue(ifr, jobj, "Key", "0.0"));
		Log.consoleLog(ifr, "downPayment==" + downPayment);
		String prodSchemeQuery = "select PROD_SCHEME_DESC from slos_staff_trn where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "prodSchemeQuery==" + prodSchemeQuery);
		List<List<String>> prodSchemeRes = ifr.getDataFromDB(prodSchemeQuery);
		Log.consoleLog(ifr, "prodSchemeRes==" + prodSchemeRes);
		if (prodSchemeRes.isEmpty()) {
			json.put("Error", "Error");
			return json;
		}
		prodSchemeORDesc = prodSchemeRes.get(0).get(0).toString();
		String ltvQuery = "SELECT LTV_PERCENT FROM staff_vl_product_sheet WHERE PRD_DESC='" + prodSchemeORDesc + "'"
				+ " AND PRODUCT_CODE='SVL' AND IS_ACTIVE='Y' ";
		Log.consoleLog(ifr, "ltvQuery==" + ltvQuery);
		List<List<String>> ltvRes = ifr.getDataFromDB(ltvQuery);
		if (ltvRes.isEmpty()) {
			json.put("Error", "Error, LTV Not found. reach out to admin");
			return json;
		}
		ltv = Double.parseDouble(ltvRes.get(0).get(0).toString());
		Log.consoleLog(ifr, "ltv==" + ltv);
		ifr.setStyle("LTV_Ratio_VL", "visible", "true");
		ifr.setValue("LTV_Ratio_VL", String.valueOf(ltv * 100));
		ifr.setStyle("LTV_Ratio_VL", "disable", "true");
		double minMargin = (1.00 - ltv) * totalCostOfVehicle;
		minMargin = Math.round(minMargin * 100.0) / 100.0;
		Log.consoleLog(ifr, "minMargin==" + minMargin);
//		if (downPayment < minMargin) {
//			json.put("Error", "Error, margin amount validation failed");
//		}
		ifr.setStyle("Min_Margin_Req", "visible", "true");
		ifr.setValue("Min_Margin_Req", String.valueOf(minMargin));
		ifr.setStyle("Min_Margin_Req", "disable", "true");
		double maxLoanAmtLTV = totalCostOfVehicle - downPayment;
		double maxLoanAmtLTVUsed = totalCostOfVehicle - minMargin;
		Log.consoleLog(ifr, "maxLoanAmtLTV===" + maxLoanAmtLTV);
		ifr.setStyle("Total_Veh_Cost", "visible", "true");
		ifr.setValue("Total_Veh_Cost", String.valueOf(totalCostOfVehicle));
		ifr.setStyle("Total_Veh_Cost", "disable", "true");
		ifr.setStyle("Max_Loan_Amt_Per_LTV", "visible", "true");
		ifr.setValue("Max_Loan_Amt_Per_LTV", String.valueOf(maxLoanAmtLTVUsed));
		ifr.setStyle("Max_Loan_Amt_Per_LTV", "disable", "true");
		if (maxLoanAmtLTV <= 0.0) {
			json.put("Error", "Error, Maximum loan amount is less than 0");
			return json;
		}

		json.put("TotalCostOfVehicle", totalCostOfVehicle);
		json.put("MinimumMarginRequired", downPayment);
		json.put("MaxLoanAsLTV", maxLoanAmtLTV);
		json.put("Success", "SUCCESS");

		/*
		 * String
		 * updateQuery="UPDATE STAFF_VL_FINAL_ELIGIBILTY SET TOTAL_COST_OF_VEHICLE='"
		 * +totalCostOfVehicle+"', " +
		 * "MIN_MARGIN_REQ='"+downPayment+"',MAX_LOAN_PER_LTV='"+maxLoanAmtLTV+"',LTV='"
		 * +ltv+"'" + " WHERE winame='"+processInstanceId+"' ";
		 * ifr.saveDataInDB(updateQuery);
		 */
		return json;
	}

	@Override
	public JSONObject EligibilityAsPerRequestedAmt(IFormReference ifr, String value) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONObject json = new JSONObject();
		json.put("Error", "");
		json.put("Success", "");
		double maxLimitOnSlider = 0;
		String val = "";
		double totalHlAvail = 0.0;
		double elegibleProjCost = 0.0;
		long elegibleProjCostL = 0;
		double marketvalofplot= 0.0;
		String finalLimit = "0";
		String loanType = ifr.getValue("LOAN_TYPE_HL").toString();
		Log.consoleLog(ifr, "loanType==========" + loanType);
		String activityName = ifr.getActivityName().toString();
		String subProductCode ="";
		String scheduleCode ="";
		String purpose ="";
		double amtRequest = 0.0;
		String subProduct = "";
		String stage = ifr.getValue("PresentStage").toString();
		Log.consoleLog(ifr, "stage" + stage);
		
		if(stage.equalsIgnoreCase("PORTAL")) {
		String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='In-Principle Approval'" + "where WINAME='"
				+ processInstanceId + "'";
		ifr.saveDataInDB(StaffResumequery);
		}
		
		
	
		
		if ((loanType.equalsIgnoreCase("HOMELOAN") && value.equalsIgnoreCase("onLoad")) || activityName.equalsIgnoreCase("Staff_HL_Branch_Maker") || activityName.equalsIgnoreCase("Staff_HL_RO_Maker") || activityName.equalsIgnoreCase("Saff_HL_CO_Maker")) {
			String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose, a.SUB_PRODUCT FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
			List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
			Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
			if (!listqueryproductandScheduleCode.isEmpty()) {
				subProductCode = listqueryproductandScheduleCode.get(0).get(0);
				scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
				purpose=listqueryproductandScheduleCode.get(0).get(2);
				subProduct=listqueryproductandScheduleCode.get(0).get(3);
			}
			
			String result = subProductCode + "-" + subProduct;
			
			String QueryProdSchemeDesc = "UPDATE SLOS_STAFF_TRN SET PROD_SCHEME_DESC= '" + result + "' WHERE WINAME= '"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "QueryProdSchemeDesc==>" + QueryProdSchemeDesc);

			ifr.saveDataInDB(QueryProdSchemeDesc);

			String reqloanAmt = "";
			String reqTenure = "";
			String ex_staff_id = "";
			String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='" + processInstanceId + "'";
			Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
			 List Result1 = ifr.getDataFromDB(exStaffQuery);
	         String Count1 = Result1.toString().replace("[", "").replace("]", "");
	         Log.consoleLog(ifr, "Count1==>" + Count1);
	         if (subProductCode.equalsIgnoreCase("666") || subProductCode.equalsIgnoreCase("667") || subProductCode.equalsIgnoreCase("670")) {

	        	    int count = Integer.parseInt(Count1);

	        	    if (count == 0 && !purpose.contains("Repair")) {
	        	        reqTenure = "360";
	        	    } else if (purpose.contains("Repair")) {
	        	        reqTenure = "120";
	        	    } else if (count > 0) {
	        	        reqTenure = "180";
	        	    }
	        }
			
			try {
				String queryForSchemelimitHL = "select HL_PRODUCT,RENOVATION_AVAIL,TOTAL_HL_AVAIL from slos_staff_home_trn WHERE WINAME = '"
						+ processInstanceId + "'";

				List<List<String>> listqueryForSchemelimitHL = ifr.getDataFromDB(queryForSchemelimitHL);

				if (listqueryForSchemelimitHL != null && !listqueryForSchemelimitHL.isEmpty()) {
					List<String> firstRow = listqueryForSchemelimitHL.get(0);

					String hlProduct = firstRow.get(0); // HL_PRODUCT
					String renovationAvail = firstRow.get(1); // RENOVATION_AVAIL
					String totalHlAvail1 = firstRow.get(2); // TOTAL_HL_AVAIL

					if (hlProduct != null && purpose.toLowerCase().contains("Repair")) {
						finalLimit = renovationAvail;
					} else {
						finalLimit = totalHlAvail1;
					}
				}
				String queryForEligibleProjectCost = "SELECT eligible_amount from LOS_CAM_COLLATERAL_DETAILS where PID  = '"
						+ processInstanceId + "'";

				List<List<String>> listqueryForEligibleProjectCost = ifr.getDataFromDB(queryForEligibleProjectCost);

				if (listqueryForEligibleProjectCost != null && !listqueryForEligibleProjectCost.isEmpty()) {
					List<String> secondRow = listqueryForEligibleProjectCost.get(0);

					if (secondRow != null && !secondRow.isEmpty()) {
						String elegibleProjectCost = secondRow.get(0);

						if (elegibleProjectCost != null && !elegibleProjectCost.trim().isEmpty()) {
							try {
								elegibleProjCost = Double.parseDouble(elegibleProjectCost.trim());
							} catch (NumberFormatException nfe) {
								Log.consoleLog(ifr,
										"Invalid number format for ELIGIBLE PROJECT COST " + elegibleProjCost);
								elegibleProjCost = 0.0;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception while fetching TOTAL_HL_AVAIL: " + e.getMessage());
				totalHlAvail = 0.0;
			}
			
			if((activityName.equalsIgnoreCase("Staff_HL_Branch_Maker") || activityName.equalsIgnoreCase("Staff_HL_RO_Maker") || activityName.equalsIgnoreCase("Saff_HL_CO_Maker"))  && value.equalsIgnoreCase("onLoad"))
			{
				Validator valid = new KnockOffValidator("");
				String EMIAmount = "";
				double eligibleAsPNth = 0.0;
				int eligibleAsPerNth;

//				double maxeligibleEmi = calculateMaxElgEMI(ifr, processInstanceId, json, valid);
				
				//double netSalAftDed = grossSalary + pension + coBorrowerAmt - (statDed + loanDed + otherDed);
				//double totalLoanDed = (statDed + loanDed + otherDed);
				

				try {
					String queryForEligibleProjectCost = "SELECT eligible_amount,marketvalueofplot from LOS_CAM_COLLATERAL_DETAILS where PID  = '"
							+ processInstanceId + "'";

					List<List<String>> listqueryForEligibleProjectCost = ifr.getDataFromDB(queryForEligibleProjectCost);

					if (listqueryForEligibleProjectCost != null && !listqueryForEligibleProjectCost.isEmpty()) {
						List<String> secondRow = listqueryForEligibleProjectCost.get(0);

						if (secondRow != null && !secondRow.isEmpty()) {
							String elegibleProjectCost = secondRow.get(0);
							String marketvalueofplot = secondRow.get(1);

							if (elegibleProjectCost != null && !elegibleProjectCost.trim().isEmpty()) {
								try {
									elegibleProjCost = Double.parseDouble(elegibleProjectCost.trim());
								} catch (NumberFormatException nfe) {
									Log.consoleLog(ifr, "Invalid number format for ELIGIBLE PROJECT COST " + elegibleProjCost);
									elegibleProjCost = 0.0;
								}
							}
							if (marketvalueofplot != null && !marketvalueofplot.trim().isEmpty()) {
								try {
									marketvalofplot = Double.parseDouble(marketvalueofplot.trim());
								} catch (NumberFormatException nfe) {
									Log.consoleLog(ifr, "Invalid number format for marketvalueofplot " + elegibleProjCost);
									marketvalofplot = 0.0;
								}
							}
						}
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception while fetching TOTAL_HL_AVAIL: " + e.getMessage());
					// = 0.0;
				}
				
				//elegibleProjCost= Math.min(elegibleProjCost, marketvalofplot);
				elegibleProjCost= elegibleProjCost;
				Log.consoleLog(ifr, "Elegible Project cost:: "+elegibleProjCost);
				String loanAMt=ifr.getValue("Rec_Loan_Amt_VL").toString();
				long loanAmtL=Long.parseLong(loanAMt);
				if(loanAMt.isEmpty() || loanAMt.isBlank() || loanAmtL==0)
				{
					json.put("Error", "Recommended Loan AMount should not be blank or 0");
					return json;
				}
				String tenure=ifr.getValue("Rec_Loan_Tenure_VL").toString();
				if(tenure.isEmpty() || tenure.isBlank() || Integer.parseInt(tenure)==0)
				{
					json.put("Error", "Tenure should not be blank or 0");
					return json;
				}
				
				   EMIAmount = getEMIAmount(ifr, processInstanceId, tenure, EMIAmount,loanAmtL,purpose);
				   double maxeligibleEmi = calculateMaxElgEMI(ifr, processInstanceId, json, valid,EMIAmount);
				   Long maxeligibleEmiLong = (long) Math.floor(maxeligibleEmi);

					String nthPercentage = ifr.getValue("NTH_Real_Percent_Maker").toString();

					double nthPercent = 0;
					try {
						nthPercent = Double.parseDouble(nthPercentage);
					} catch (NumberFormatException e) {
						// handle invalid or empty value
						nthPercent = 0;
					}

					if (nthPercent < 30) {

						json.put("Error",
								"error,The recommended loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the recommended loan amount and calculate again.");
						return json;
					}
				   
					ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(finalLimit));
					eligibleAsPNth = (maxeligibleEmi / Double.parseDouble(EMIAmount)) * Double.parseDouble(ifr.getValue("Rec_Loan_Amt_VL").toString());
					eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
					Log.consoleLog(ifr, "EMIAmount===" + EMIAmount);
					Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
					double inPrincipleElgLoan = Math.min(Math.min(Double.parseDouble(finalLimit),elegibleProjCost), eligibleAsPerNth);
					long roundedVal = (long) (Math.floor(inPrincipleElgLoan / 1000.0) * 1000);
					ifr.setValue("Final_Elg_VL_P", String.valueOf(roundedVal));
					
					ifr.setValue("Elg_Per_NTH_P", String.valueOf(eligibleAsPerNth));
					elegibleProjCostL = (long) elegibleProjCost;
					ifr.setValue("Elg_Per_LTV_P", String.valueOf(elegibleProjCostL));
					try {
						if (loanAmtL > inPrincipleElgLoan) {
							json.put("Error", "The recommended loan amount is higher than the Final Eligiblity. Please reduce the recommended loan amount and calculate again.");
							return json;
						}
						if (Integer.parseInt(tenure) > Integer.parseInt(reqTenure)) {
							json.put("Error", "Recommended Tenure is higher than max tenure.lease reduce the recommended tenure and calculate again.");
							return json;
						}
					} catch (NumberFormatException e) {
						json.put("Error", "Tenure must be numeric");
					}
					
//					String queryUpdateMaxElgEMI = "UPDATE Slos_Staff_Vl_Eligibility SET FINAL_ELG_VL_BM='" + eligibleAsPerNth
//							+ "' WHERE WINAME='" + processInstanceId + "'";
//
//					Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
//					ifr.saveDataInDB(queryUpdateNth);
//
//					//maxeligibleEmi = calculateMaxElgEMI(ifr, processInstanceId, json, valid,EMIAmount);
//					ifr.setValue("MAX_ELG_EMI", String.valueOf(maxeligibleEmiLong));	
					
					try {

						String queryUpdateNth = "UPDATE Slos_Staff_Vl_Eligibility SET ELG_PER_NTH='" + eligibleAsPerNth
								+ "', FINAL_ELG_VL_BM ='"+maxeligibleEmiLong+"' WHERE WINAME='" + processInstanceId + "'";

						Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
						ifr.saveDataInDB(queryUpdateNth);
						ifr.setValue("MAX_ELG_EMI", String.valueOf(maxeligibleEmiLong));
					} catch (Exception e) {
						json.put("Error", "database errors occured");
						return json;
					}

					

			   //}
			}

			Log.consoleLog(ifr, "TOTAL_HL_AVAIL value: " + finalLimit);
			ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(finalLimit));
			elegibleProjCostL = (long) elegibleProjCost;
			ifr.setValue("Elg_Per_LTV", String.valueOf(elegibleProjCostL));
			ifr.setValue("Max_Tenure_VL", reqTenure);
			ifr.setValue("ROI_Type_VL", "Floating");
			
			String nthEligiblity = "";
			String queryForNth = "Select ELG_PER_NTH from SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "query for Nth" + queryForNth);
			List<List<String>> list = ifr.getDataFromDB(queryForNth);
			Log.consoleLog(ifr, "query for Nth list" + list);
			if (!list.isEmpty()) {
				Log.consoleLog(ifr, "Inside Nth list");
				nthEligiblity = list.get(0).get(0);

			}
			double min = Math.min(Math.min(Double.parseDouble(finalLimit), elegibleProjCost),
					Double.parseDouble(nthEligiblity));
			//Log.consoleLog(ifr, "amtRequest===" + amtRequest);
			double finalEligiblity = min;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));
			ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			int eligibleAsPerNth = (int) Math.floor(Integer.parseInt(nthEligiblity));
			ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
			
			calCulateROI(ifr, subProductCode, scheduleCode, roundedVal, "360");
			
			String queryMORATORIAM = "SELECT MORATORIAM FROM SLOS_STAFF_HOME_TRN where winame='" + processInstanceId + "'";
			List<List<String>> resqueryMORATORIAM = ifr.getDataFromDB(queryMORATORIAM);
			Log.consoleLog(ifr, "resqueryMORATORIAM..." + resqueryMORATORIAM);
			String morotoriam = (!resqueryMORATORIAM.isEmpty()
			        && resqueryMORATORIAM.get(0).get(0) != null
			        && !resqueryMORATORIAM.get(0).get(0).trim().isEmpty())
			        ? resqueryMORATORIAM.get(0).get(0).trim()
			        : "0";
			
			int tenure = Integer.parseInt(reqTenure) - Integer.parseInt(morotoriam);
//			int part1 =0;
//			int part2 =0;
//			if (purpose.contains("Repair")) {
//				part1 = (int) Math.floor(tenure * 0.67);
//				part2 = tenure - part1;
//			} else {
//				part1 = (int) Math.floor(tenure * 0.75);
//				part2 = tenure - part1;
//			}
			
			int part1 = 0;
			int part2 = 0;

			if (purpose.contains("Repair")) {
			    part1 = (tenure * 7) / 10;  // floor automatically
			} else {
			    part1 = (tenure * 3) / 4;   // floor automatically
			}
			part2 = tenure - part1;
			
			
			

			ifr.setValue("Principal_Repay_Tenure_VL", String.valueOf(part1));

			ifr.setValue("Interest_Repay_Tenure_VL", String.valueOf(part2));
			

//			String StaffRquery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='In-Principle Approval'"
//					+ "where WINAME='" + processInstanceId + "'";
//			Log.consoleLog(ifr, "StaffRquery" + StaffRquery);
//			ifr.saveDataInDB(StaffRquery);
		
		}
			// if (!value.equalsIgnoreCase("onLoad")) {
		else if(stage.equalsIgnoreCase("PORTAL") && !value.equalsIgnoreCase("onLoad") && loanType.equalsIgnoreCase("HOMELOAN")) {
				String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE, b.hl_purpose FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) join SLOS_HOME_PURPOSE c on trim(a.SUB_PRODUCT_CODE)=trim(c.PRODUCTCODE) where b.winame='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
				List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
				Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
				if (!listqueryproductandScheduleCode.isEmpty()) {
					subProductCode = listqueryproductandScheduleCode.get(0).get(0);
					scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
					purpose=listqueryproductandScheduleCode.get(0).get(2);
				}

				String reqloanAmt = "";
				String reqTenure = "";
				String ex_staff_id = "";
				String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='" + processInstanceId + "'";
				Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
				 List Result1 = ifr.getDataFromDB(exStaffQuery);
		         String Count1 = Result1.toString().replace("[", "").replace("]", "");
		         Log.consoleLog(ifr, "Count1==>" + Count1);
					if (subProductCode.equalsIgnoreCase("666") || subProductCode.equalsIgnoreCase("667") ||  subProductCode.equalsIgnoreCase("670")) {

						int count = Integer.parseInt(Count1);

						if (count == 0 && !purpose.contains("Repair")) {
							reqTenure = "360";
						} else if (purpose.contains("Repair")) {
							reqTenure = "120";
						} else if (count > 0) {
							reqTenure = "180";
						}
					}
				
				val = ifr.getValue("Req_Loan_Amt_VL").toString();
				Log.consoleLog(ifr, "val" + val);
				amtRequest = Double.parseDouble(val);
				Log.consoleLog(ifr, "amtRequest" + amtRequest);
				
				int ratioA = 3;
				int ratioB = 1;
				
				reqTenure = ifr.getValue("Req_Tenure_VL").toString();
				
				int part1 = ( Integer.parseInt(reqTenure) * ratioA) / (ratioA + ratioB);
				int part2 = (Integer.parseInt(reqTenure)  * ratioB) / (ratioA + ratioB);
				ifr.setValue("Req_Prin_Repay_Tenure_VL", String.valueOf(part1));
				ifr.setValue("Req_Int_Repay_Tenure_VL", String.valueOf(part2));
				Validator valid = new KnockOffValidator("");
					
				
				String EMIAmount="";
				String loanAmt = ifr.getValue("Req_Loan_Amt_VL").toString();
				String reqTenureVL = ifr.getValue("Req_Tenure_VL").toString();
				long loanAmtInt=Long.parseLong(loanAmt);
				
				try {
					if (Integer.parseInt(reqTenureVL) > Integer.parseInt(reqTenure)) {
						json.put("Error", "Requested Tenure is greater than max tenure");
						return json;
					}
				} catch (NumberFormatException e) {
					json.put("Error", "Tenure must be numeric");
					return json;
				}
				
				EMIAmount = getEMIAmount(ifr, processInstanceId, reqTenureVL, EMIAmount,loanAmtInt,purpose);
				double maxeligibleEmi = calculateMaxElgEMI(ifr, processInstanceId, json, valid,EMIAmount);
				ifr.setValue("MAX_ELG_EMI", String.format("%.2f", maxeligibleEmi));	
//				if (!stage.equalsIgnoreCase("PORTAL") && value.equalsIgnoreCase("onLoad")) {
//				val = ifr.getValue("Rec_Loan_Amt_VL").toString();
//				Log.consoleLog(ifr, "else val" + val);
//				amtRequest = Double.parseDouble(val);
//				Log.consoleLog(ifr, "else amtRequest" + amtRequest);
//			   }	
		
//			if (amtRequest <= min) {

//				JSONObject result = eligibiltyAsSalaryNthHL(ifr, amtRequest, value, 0.0, json);
//				if (result.containsKey("Error")) {
//					return result;
//				} else {
//					result.put("Error", "");
//					return result;
//				}
		  } 
		  else {
//				String StaffResquery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Avail Offer'" + "where WINAME='"
//						+ processInstanceId + "'";
//				ifr.saveDataInDB(StaffResquery);

				String queryBranchCodeAndVehicleCat = "select VEHICLE_CATEGORY from SLOS_STAFF_TRN " + "where WINAME ='"
						+ processInstanceId + "'";
				List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);

				String maxLimitOnSliderQuery = "SELECT MAX_LOAN_AMT_PER_LTV,TOTAL_COST, DOWNPAYMENT_AMOUNT_EXP FROM slos_staff_collateral where winame='"
						+ processInstanceId + "'";
				List<List<String>> maxLimit = ifr.getDataFromDB(maxLimitOnSliderQuery);
				if (maxLimit.isEmpty()) {
					json.put("Error", "error, Max amount not present");
					return json;
				}
				Log.consoleLog(ifr, "maxLimitOnSliderQuery==" + maxLimitOnSliderQuery);
				if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("USED")) {
					val = maxLimit.get(0).get(0).toString().trim().isEmpty() ? "0.0" : maxLimit.get(0).get(0).toString();
				}
				if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("NEW")) {
//					Double totalcostperltv = Double.parseDouble(maxLimit.get(0).get(1))
//							- Double.parseDouble(maxLimit.get(0).get(2));
					val = maxLimit.get(0).get(0).toString().trim().isEmpty() ? "0.0" : maxLimit.get(0).get(0).toString();
					//val = String.valueOf(totalcostperltv);
				}
				Log.consoleLog(ifr, "val===" + val);
				maxLimitOnSlider = Double.parseDouble(val);
				if (maxLimitOnSlider <= 0.0) {
					json.put("Error", "error, Technical glitch");
					return json;
				}
				amtRequest = 0.0;
				stage = ifr.getValue("PresentStage").toString();
				// if (!value.equalsIgnoreCase("onLoad")) {
				if (stage.equalsIgnoreCase("PORTAL")) {
					val = ifr.getValue("Req_Loan_Amt_VL").toString().trim().isEmpty() ? String.valueOf(maxLimitOnSlider)
							: ifr.getValue("Req_Loan_Amt_VL").toString();
					amtRequest = Double.parseDouble(val);
				} else {
					val = ifr.getValue("Rec_Loan_Amt_VL").toString().trim().isEmpty() ? String.valueOf(maxLimitOnSlider)
							: ifr.getValue("Rec_Loan_Amt_VL").toString();
					amtRequest = Double.parseDouble(val);
				}

				Log.consoleLog(ifr, "amtRequest===" + amtRequest);
				if (amtRequest <= maxLimitOnSlider) {

					JSONObject result = eligibiltyAsSalaryNth(ifr, amtRequest, value);
					if (result.containsKey("Error")) {
						return result;
					} else {
						result.put("Error", "");
						return result;
					}

				} else {
					if (!value.equalsIgnoreCase("onLoad")) {
						json.put("Error", "error, Amount request greater than the MAX LIMIT");
					}
				}

			}
			
			if (json.containsKey("Error")) {
				return json;
			} else {
				json.put("Error", "");
				return json;
			}
		
	
	}	
			



	private double calculateMaxElgEMI(IFormReference ifr, String processInstanceId, JSONObject json, Validator valid, String eMIAmount) {
		String query = "select  gross_salary,PENSION_INCOME,STATUTORY_DEDUCTIONS,LOAN_DEDUCTIONS,EXT_LOAN_DED,roi,NET_SALARY from slos_staff_home_trn where winame ='"
				+ processInstanceId + "'";

		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query===" + query);
		String netSalary = "";
		String grosssalary = "";
		String query1 = "select netsalary,grosssalary from los_nl_occupation_info where F_KEY in (select F_KEY from los_nl_basic_info where PID = '"
				+ processInstanceId + "' and CONSIDERELIGIBILITY = 'Yes' and APPLICANTTYPE = 'CB')";
		List<List<String>> res1 = ifr.getDataFromDB(query1);
		Log.consoleLog(ifr, "query1===" + query1);

		if (res1 != null && !res1.isEmpty()) {
			for (List<String> row : res1) {
				if (row != null && !row.isEmpty()) {
					netSalary = row.get(0);
					grosssalary = row.get(1);
					Log.consoleLog(ifr, "NETSALARY = " + netSalary);
				}
			}
		} else {
			Log.consoleLog(ifr, "No NETSALARY data found for processInstanceId = " + processInstanceId);
		}
		json.put("grossSalary", res.get(0).get(0));
		double grossSalary = Double.parseDouble(valid.getValue(ifr, json, "grossSalary", "0.0"));
		Log.consoleLog(ifr, "grossSalary===" + grossSalary);
		json.put("pension", res.get(0).get(1));
		double pension = Double.parseDouble(valid.getValue(ifr, json, "pension", "0.0"));
		Log.consoleLog(ifr, "pension===" + pension);

		double coBorrowerAmtNet = netSalary.trim().isEmpty() ? 0.0 : Double.parseDouble(netSalary);
		Log.consoleLog(ifr, "coBorrowerAmtNet===" + coBorrowerAmtNet);

		double coBorrowerAmtGross = grosssalary.trim().isEmpty() ? 0.0 : Double.parseDouble(grosssalary);
		Log.consoleLog(ifr, "coBorrowerAmtGross===" + coBorrowerAmtGross);

		json.put("statDed", res.get(0).get(2));
		double statDed = Double.parseDouble(valid.getValue(ifr, json, "statDed", "0.0"));
		Log.consoleLog(ifr, "statDed===" + statDed);

		json.put("loanDed", res.get(0).get(3));
		double loanDed = Double.parseDouble(valid.getValue(ifr, json, "loanDed", "0.0"));
		Log.consoleLog(ifr, "loanDed===" + loanDed);

		json.put("otherDed", res.get(0).get(4));
		double otherDed = Double.parseDouble(valid.getValue(ifr, json, "otherDed", "0.0"));

		Log.consoleLog(ifr, "otherDed===" + otherDed);
		// double roi=Double.parseDouble(res.get(0).get(5).trim());

		json.put("netSal", res.get(0).get(6));
		double netSal = Double.parseDouble(valid.getValue(ifr, json, "netSal", "0.0"));

		double minNth = (0.3 * (grossSalary + pension)) + (0.3 * coBorrowerAmtGross);
		Log.consoleLog(ifr, "minNth===" + minNth);
		double totalNetSal = netSal + coBorrowerAmtNet;
		Log.consoleLog(ifr, "totalNetSal===" + totalNetSal);
		
		double totalGrossSal = grossSalary + coBorrowerAmtGross;

		double maxeligibleEmi = totalNetSal - minNth;
		Log.consoleLog(ifr, "maxeligibleEmi===" + maxeligibleEmi);
		
		double netSalAftDed  =  totalNetSal+pension;
		
		double emiAmount = 0.0;

		try {
		    emiAmount = Double.parseDouble(eMIAmount);
		} catch (NumberFormatException e) {
		    // handle invalid or empty value
		    emiAmount = 0.0;
		}
		
		double netSalaryAfterMonthlyInstall = netSalAftDed - Double.parseDouble(eMIAmount);
		String formattedinp = String.format("%.2f", netSalaryAfterMonthlyInstall);
		netSalaryAfterMonthlyInstall = Double.parseDouble(formattedinp);
		ifr.setValue("Sal_after_monthly_insta_Maker", String.valueOf(netSalaryAfterMonthlyInstall));
//		double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
		double actualNTHPercentage = (netSalaryAfterMonthlyInstall / totalGrossSal) * 100;
		String formatted = String.format("%.2f", actualNTHPercentage);
		int actualNTHPercentageInt = (int) Double.parseDouble(formatted);
		//actualNTHPercentage = Double.parseDouble(formatted);
		ifr.setValue("NTH_Real_Percent_Maker", String.valueOf(actualNTHPercentageInt));
		
		
		return maxeligibleEmi;
	}

	private JSONObject eligibiltyAsSalaryNthHL(IFormReference ifr, double amtRequest, String value, double min,
			JSONObject json) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String subProductCode = "";
		String scheduleCode = "";
		String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) where b.winame='"
				+ processInstanceId + "'";
		List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
		if (!listqueryproductandScheduleCode.isEmpty()) {
			subProductCode = listqueryproductandScheduleCode.get(0).get(0);
			scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
		}
		String tenure = "";
		String queryForRequestedLoanAmt = "select REQUESTED_TENURE from slos_staff_home_trn where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForRequestedLoanAmt===>" + queryForRequestedLoanAmt);
		List<List<String>> listqueryForRequestedLoanAmt = ifr.getDataFromDB(queryForRequestedLoanAmt);
		Log.consoleLog(ifr, "listqueryForRequestedLoanAmt===>" + listqueryForRequestedLoanAmt);
		if (!listqueryForRequestedLoanAmt.isEmpty()) {
			// reqloanAmt = listqueryForRequestedLoanAmt.get(0).get(0);
			tenure = listqueryForRequestedLoanAmt.get(0).get(0);
		}

		Validator valid = new KnockOffValidator("");
		String query = "select  gross_salary,PENSION_INCOME,STATUTORY_DEDUCTIONS,LOAN_DEDUCTIONS,EXT_LOAN_DED,roi,NET_SALARY from slos_staff_home_trn where winame ='"
				+ processInstanceId + "'";

		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query===" + query);
		String netSalary = "";
		String grosssalary = "";
		String query1 = "select netsalary,grosssalary from los_nl_occupation_info where F_KEY in (select F_KEY from los_nl_basic_info where PID = '"
				+ processInstanceId + "' and CONSIDERELIGIBILITY = 'Yes' and APPLICANTTYPE = 'CB')";
		List<List<String>> res1 = ifr.getDataFromDB(query1);
		Log.consoleLog(ifr, "query1===" + query1);

		if (res.isEmpty()) {
			json.put("Error", "Technical error.");
			return json;
		}

		if (res1 != null && !res1.isEmpty()) {
			for (List<String> row : res1) {
				if (row != null && !row.isEmpty()) {
					netSalary = row.get(0);
					grosssalary = row.get(1);
					Log.consoleLog(ifr, "NETSALARY = " + netSalary);
					// You can now use netSalary in your logic (e.g., assign to variable,
					// calculation, etc.)
				}
			}
		} else {
			Log.consoleLog(ifr, "No NETSALARY data found for processInstanceId = " + processInstanceId);
		}
        json.put("grossSalary", res.get(0).get(0));
		double grossSalary = Double.parseDouble(valid.getValue(ifr, json, "grossSalary", "0.0"));
		Log.consoleLog(ifr, "grossSalary===" + grossSalary);
		json.put("pension", res.get(0).get(1));
		double pension = Double.parseDouble(valid.getValue(ifr, json, "pension", "0.0"));
		Log.consoleLog(ifr, "pension===" + pension);

		double coBorrowerAmtNet = netSalary.trim().isEmpty() ? 0.0 : Double.parseDouble(netSalary);
		Log.consoleLog(ifr, "coBorrowerAmtNet===" + coBorrowerAmtNet);

		double coBorrowerAmtGross = grosssalary.trim().isEmpty() ? 0.0 : Double.parseDouble(grosssalary);
		Log.consoleLog(ifr, "coBorrowerAmtGross===" + coBorrowerAmtGross);

		json.put("statDed", res.get(0).get(2));
		double statDed = Double.parseDouble(valid.getValue(ifr, json, "statDed", "0.0"));
		Log.consoleLog(ifr, "statDed===" + statDed);

		json.put("loanDed", res.get(0).get(3));
		double loanDed = Double.parseDouble(valid.getValue(ifr, json, "loanDed", "0.0"));
		Log.consoleLog(ifr, "loanDed===" + loanDed);

		json.put("otherDed", res.get(0).get(4));
		double otherDed = Double.parseDouble(valid.getValue(ifr, json, "otherDed", "0.0"));

		Log.consoleLog(ifr, "otherDed===" + otherDed);
		// double roi=Double.parseDouble(res.get(0).get(5).trim());

		json.put("netSal", res.get(0).get(6));
		double netSal = Double.parseDouble(valid.getValue(ifr, json, "netSal", "0.0"));

//64335
		double minNth = (0.3 * (grossSalary + pension)) + (0.3 * coBorrowerAmtGross);
		double totalNetSal = netSal + coBorrowerAmtNet;

		double maxeligibleEmi = totalNetSal - minNth;

		Log.consoleLog(ifr, "minNth===" + minNth);
		if (minNth <= 0.0) {
			json.put("Error", "Minimum nth is cannnot be less tha or equals zero");
			return json;
		}
//		double netSalAftDed = grossSalary + pension + coBorrowerAmtGross
//				- (statDed + loanDed + otherDed + coBorrowerAmtNet);
		double totalLoanDed = (statDed + loanDed + otherDed);
		
		double netSalAftDed  =  totalNetSal+pension;

		Log.consoleLog(ifr, "totalLoanDed===" + totalLoanDed);
		String stage = ifr.getValue("PresentStage").toString();
		if (stage.equalsIgnoreCase("PORTAL")) {
			if (value.equalsIgnoreCase("onload")) {
				ifr.setValue("ROI_Type_VL", "Floating");
				Ammortization ammortization = new Ammortization();
				JSONObject calculateNthAndInpriciplePortalHL = calculateNthAndInpriciplePortalHL(ifr, amtRequest, value,
						json, min, scheduleCode, processInstanceId, subProductCode, String.valueOf(minNth),
						String.valueOf(netSalAftDed), ammortization, tenure, subProductCode);
				if (calculateNthAndInpriciplePortalHL.containsKey("Error")) {
					return calculateNthAndInpriciplePortalHL;
				}

			}

			else {
				Ammortization ammortization = new Ammortization();
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						ifr.getValue("Req_Loan_Amt_VL").toString(), ifr.getValue("Req_Tenure_VL").toString(),
						subProductCode, scheduleCode);
				Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
				String[] ammortizationResp = ammortizationResponse.split(":");

				if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
					Log.consoleLog(ifr, "Ammortization inside===================>");
					if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
						json.put("Error", "error, Ammortization error, No Response from the server");
						return json;
					} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
						json.put("Error", ammortizationResp[1]);
						return json;
					}
				}
				
				int ratioA = 3;
				int ratioB = 1;
				
				String reqTenure = ifr.getValue("Req_Tenure_VL").toString();
				
				if(reqTenure!=null  || !reqTenure.trim().isEmpty()) {
				int part1 = ( Integer.parseInt(reqTenure) * ratioA) / (ratioA + ratioB);
				int part2 = (Integer.parseInt(reqTenure)  * ratioB) / (ratioA + ratioB);
				

				ifr.setValue("Req_Prin_Repay_Tenure_VL", String.valueOf(part1));

				ifr.setValue("Req_Int_Repay_Tenure_VL", String.valueOf(part2));
				}

				JSONObject jsonObj = new JSONObject();
				jsonObj = calculateReqAmmortizationPortalHL(ifr, json, processInstanceId, grossSalary, pension, minNth,
						totalLoanDed, maxeligibleEmi, min, value);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			}

		} else {
			if (value.equalsIgnoreCase("onload")) {
				Ammortization ammortization = new Ammortization();
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						ifr.getValue("Rec_Loan_Amt_VL").toString(), ifr.getValue("Rec_Loan_Tenure_VL").toString(),
						subProductCode, scheduleCode);
				Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
				String[] ammortizationResp = ammortizationResponse.split(":");

				if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
					Log.consoleLog(ifr, "Ammortization inside===================>");
					if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
						json.put("Error", "error, Ammortization error, No Response from the server");
						return json;
					} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
						json.put("Error", ammortizationResp[1]);
						return json;
					}
				}
				
				int ratioA = 3;
				int ratioB = 1;
				
				String reqTenure = ifr.getValue("Rec_Loan_Tenure_VL").toString();
				
				if (!reqTenure.trim().isEmpty() || reqTenure != null) {
					     int ten = Integer.parseInt(reqTenure);
					    double totalRatio = ratioA + ratioB;

					    int part1 = (int) Math.ceil(ten * (ratioA / totalRatio));
					    int part2 = ten - part1;  // ensure sum = original tenure
					   ifr.setValue("Req_Prin_Repay_Tenure_VL", String.valueOf(part1));
					    Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + String.valueOf(part1));
					   ifr.setValue("Req_Int_Repay_Tenure_VL", String.valueOf(part2));
					    Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + String.valueOf(part2));

//				ifr.setValue("Principal_Repay_Tenure_VL", String.valueOf(part1));
//
//				ifr.setValue("Interest_Repay_Tenure_VL", String.valueOf(part2));
				}

				JSONObject jsonObj = new JSONObject();
				jsonObj = calculateReqAmmortizationPortalHL(ifr, json, processInstanceId, grossSalary, pension, minNth,
						totalLoanDed, maxeligibleEmi, min, value);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			}
		}
		return json;
	}

	public JSONObject EligibilityAsPerRequestedAmtPortalHL(IFormReference ifr, String value) {
		JSONObject json = new JSONObject();
		Validator valid = new KnockOffValidator("");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		//String tenure = "360";
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
			purpose=listqueryproductandScheduleCode.get(0).get(2);
		}

		String reqloanAmt = "";
		String reqTenure = "";
		String queryForRequestedLoanAmt = "select REQ_AMT_TOT_PLD,REQUESTED_TENURE from slos_staff_home_trn where winame='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForRequestedLoanAmt===>" + queryForRequestedLoanAmt);
		List<List<String>> listqueryForRequestedLoanAmt = ifr.getDataFromDB(queryForRequestedLoanAmt);
		Log.consoleLog(ifr, "listqueryForRequestedLoanAmt===>" + listqueryForRequestedLoanAmt);
		if (!listqueryForRequestedLoanAmt.isEmpty()) {
			reqloanAmt = listqueryForRequestedLoanAmt.get(0).get(0);
			//reqTenure = listqueryForRequestedLoanAmt.get(0).get(1);
		}
		String exStaffQuery = "select count(*) from slos_staff_home_trn where ex_staff_id IS NOT NULL and winame ='" + processInstanceId + "'";
		Log.consoleLog(ifr, "Count query===>" + exStaffQuery);
		 List Result1 = ifr.getDataFromDB(exStaffQuery);
         String Count1 = Result1.toString().replace("[", "").replace("]", "");
         Log.consoleLog(ifr, "Count1==>" + Count1);
         ifr.setValue("Q_CurrBranchType", subProductCode);
         if (subProductCode.equalsIgnoreCase("666") || subProductCode.equalsIgnoreCase("667") || subProductCode.equalsIgnoreCase("670")) {

        	    int count = Integer.parseInt(Count1);

        	    if (count == 0 && !purpose.contains("Repair")) {
        	        reqTenure = "360";
        	    } else if (purpose.contains("Repair")) {
        	        reqTenure = "120";
        	    } else if (count > 0) {
        	        reqTenure = "180";
        	    }
        }

		String EMIAmount = "";
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth;
		


		
		
		EMIAmount = getEMIAmount(ifr, processInstanceId, reqTenure, EMIAmount,100000,purpose);
		Log.consoleLog(ifr, "EMIAmount : " + EMIAmount);
		double maxeligibleEmi = calculateMaxElgEMI(ifr, processInstanceId, json, valid,EMIAmount);
		Log.consoleLog(ifr, "maxeligibleEmi : " + maxeligibleEmi);
		Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
		eligibleAsPNth = maxeligibleEmi / Double.parseDouble(EMIAmount) * AccelatorStaffConstant.PMT_VALUE;
		eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
		ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
		try {

			String queryUpdateNth = "UPDATE Slos_Staff_Vl_Eligibility SET ELG_PER_NTH='" + eligibleAsPerNth
					+ "' WHERE WINAME='" + processInstanceId + "'";

			Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
			ifr.saveDataInDB(queryUpdateNth);
		} catch (Exception e) {
			json.put("Error", "database errors occured");
			return json;
		}


		if (json.containsKey("Error")) {
			return json;
		} else {
			json.put("Error", "");
			return json;
		}
//		return json;

	}

	private String getEMIAmount(IFormReference ifr, String processInstanceId, String reqTenure, String EMIAmount, long loanAmt, String purpose) {
		String morotoriam = "";
		String queryMORATORIAM = "SELECT MORATORIAM FROM SLOS_STAFF_HOME_TRN where winame='" + processInstanceId + "'";
		List<List<String>> resqueryMORATORIAM = ifr.getDataFromDB(queryMORATORIAM);
		Log.consoleLog(ifr, "resqueryMORATORIAM..." + resqueryMORATORIAM);
		morotoriam = (!resqueryMORATORIAM.isEmpty()
		        && resqueryMORATORIAM.get(0).get(0) != null
		        && !resqueryMORATORIAM.get(0).get(0).trim().isEmpty())
		        ? resqueryMORATORIAM.get(0).get(0).trim()
		        : "0";
		
		String dateOfDisbursement = "";
		String querydateOfDisburse = "SELECT PROBABLE_DISB_DATE FROM LOS_CAM_COLLATERAL_DETAILS where PID='" + processInstanceId + "'";
		List<List<String>> resquerydateOfDisburse = ifr.getDataFromDB(querydateOfDisburse);
		Log.consoleLog(ifr, "querydateOfDisburse..." + querydateOfDisburse);
		if (!resquerydateOfDisburse.isEmpty()
		        && resquerydateOfDisburse.get(0).get(0) != null
		        && !resquerydateOfDisburse.get(0).get(0).trim().isEmpty()) {
			dateOfDisbursement = resquerydateOfDisburse.get(0).get(0);
			Log.consoleLog(ifr, "dateOfDisbursement..." + dateOfDisbursement);
		}
		
		// Input format coming from DB
		DateTimeFormatter dbFormatter =
		        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Output format required
		DateTimeFormatter outputFormatter =
		        DateTimeFormatter.ofPattern("dd-MMM-yyyy");

		// Convert DB timestamp → LocalDate
		LocalDate date = LocalDateTime
		        .parse(dateOfDisbursement, dbFormatter)
		        .toLocalDate();

		Log.consoleLog(ifr, "parsed date..." + date);

		// Final formatted date
		String formattedDate = date.format(outputFormatter);
		Log.consoleLog(ifr, "formattedDate..." + formattedDate);
		
		
		Log.consoleLog(ifr, "formattedDate..." + formattedDate);
		String IntrestAmount="";

		int morotoriamInt = Integer.parseInt(morotoriam);
		//String EMIAmount="";
		String IntrestAmt="";

//		int tenure = (int) Math.ceil((Double.parseDouble(reqTenure) - Double.parseDouble(morotoriam)) * 0.75);//principle tenure
		int tenure = Integer.parseInt(reqTenure) - Integer.parseInt(morotoriam);
		Log.consoleLog(ifr, "tenure..." + tenure);
		int tenureRemaining = Integer.parseInt(reqTenure) - tenure;
		Log.consoleLog(ifr, "tenureRemaining..." + tenureRemaining);
		
		int part1 = 0;
		int part2 = 0;

		if (purpose.contains("Repair")) {
			part1 = (tenure * 7) / 10;  // floor automatically
		} else {
			part1 = (tenure * 3) / 4;   // floor automatically
		}
		part2 = tenure - part1;
		
//		
//        int principleTenure = (int) Math.floor(tenure* 0.75);//principle tenure
//		
//		
//		int part1 = principleTenure;
//		int part2 = tenure - part1;
		

		ifr.setValue("Req_Prin_Repay_Tenure_VL", String.valueOf(part1));

		ifr.setValue("Req_Int_Repay_Tenure_VL", String.valueOf(part2));
		
		
		String ammortizationQueryPrincipalCalc = "SELECT emi_princ_amt from generate_fpi_emi ("+loanAmt+","
				+ part1 + "," + part2 + "," + morotoriamInt + ",'" + formattedDate + "') WHERE emi_princ_amt <> 0 FETCH FIRST 1 ROW ONLY";
		List<List<String>> resultammortizationQueryPrincipalCalc = ifr.getDataFromDB(ammortizationQueryPrincipalCalc);
		Log.consoleLog(ifr, "ammortizationQueryPrincipalCalc..." + ammortizationQueryPrincipalCalc);
		
		String ammortizationQueryIntrestCal = "SELECT emi_int_amt from generate_fpi_emi ("+loanAmt+","
				+ part1 + "," + part2 + "," + morotoriamInt + ",'" + formattedDate + "') WHERE emi_int_amt <> 0 FETCH FIRST 1 ROW ONLY";
		List<List<String>> resultammortizationQueryIntrestCal = ifr.getDataFromDB(ammortizationQueryIntrestCal);
		Log.consoleLog(ifr, "ammortizationQueryIntrestCal..." + ammortizationQueryIntrestCal);
		
		if (!resultammortizationQueryPrincipalCalc.isEmpty()) {
			EMIAmount = resultammortizationQueryPrincipalCalc.get(0).get(0);
			Log.consoleLog(ifr, "EMIAmount..." + EMIAmount);
			//IntrestAmt = resultammortizationQuery.get(0).get(1);
			//Log.consoleLog(ifr, "IntrestAmt..." + IntrestAmt);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", Double.parseDouble(EMIAmount)));
			//ifr.setValue("Interest_Monthly_Inst_VL", String.format("%.2f", Double.parseDouble(IntrestAmt)));
			ifr.setValue("Monthly_Instal_Maker", String.format("%.2f", Double.parseDouble(EMIAmount)));
		}
		if (!resultammortizationQueryIntrestCal.isEmpty()) {
			IntrestAmt = resultammortizationQueryIntrestCal.get(0).get(0);
			Log.consoleLog(ifr, "IntrestAmt..." + IntrestAmt);
			//ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", Double.parseDouble(EMIAmount)));
			ifr.setValue("Interest_Monthly_Inst_VL", String.format("%.2f", Double.parseDouble(IntrestAmt)));
			//ifr.setValue("Monthly_Instal_Maker", String.format("%.2f", Double.parseDouble(EMIAmount)));
		}
		
//		String ammortizationQueryForSchedule = "SELECT sl_no,date_from,date_to,emi_princ_amt,emi_int_amt,amt_princ_balance from generate_fpi_emi ("+loanAmt+","
//				+ part1 + "," + part2 + "," + morotoriamInt + ",'" + formattedDate + "')";
//		List<List<String>> resultammortizationQueryForSchedule = ifr.getDataFromDB(ammortizationQueryForSchedule);
//		List<List<String>> resultammortizationQueryForSchedule = cf.mExecuteQuery(ifr, ammortizationQueryForSchedule, "Log for Schedule");
//		Log.consoleLog(ifr, "ammortizationQueryForSchedule..." + ammortizationQueryForSchedule);
		
		//cf.mExecuteQuery(ifr, ammortizationQueryIntrestCal, ammortizationQueryForSchedule)
		
		//String insertSQL = "INSERT INTO table_a (INSTALLMENTNO, STARTDATE, REPAYMENTDATE, PRINCIPAL, INTEREST, INSTALLMENT, OUTSTANDINGBALANCE) VALUES (?, ?, ?, ?, ?, ?, ?)";
		  String deletequery = "delete from LOS_STG_CBS_AMM_SCH_DETAILS where PROCESSINSTANCEID='" + processInstanceId + "'";
			ifr.saveDataInDB(deletequery);
			
//			int size = resultammortizationQueryForSchedule.size();
//			Log.consoleLog(ifr, "Size of schedule..." + size);
//			
//			int count = 0;
		
//		for (int i = 0; i < resultammortizationQueryForSchedule.size(); i++) {
//			count++;
//
//		    List<String> row = resultammortizationQueryForSchedule.get(i);
//
//		    String installmentNo = row.get(0);
//		    Log.consoleLog(ifr, "installmentNo..." + installmentNo);
//		    String startDate = row.get(1);        // DATE_FROM
//		    Log.consoleLog(ifr, "startDate..." + startDate);
//		    String repaymentDate = row.get(2);    // DATE_TO
//		    Log.consoleLog(ifr, "repaymentDate..." + repaymentDate);
//		    String principal = row.get(3);
//		    Log.consoleLog(ifr, "principal..." + principal);
//		    String interest = row.get(4);
//		    Log.consoleLog(ifr, "interest..." + interest);
//		    
//			// Convert DB timestamp → LocalDate
//			LocalDate dateStart = LocalDateTime
//			        .parse(startDate, dbFormatter)
//			        .toLocalDate();
//
//			Log.consoleLog(ifr, "parsed date..." + dateStart);
//
//			// Final formatted date
//			String dateStartformattedDate = dateStart.format(outputFormatter);
//			//Log.consoleLog(ifr, "formattedDate..." + formattedDate);
//			
//			// Convert DB timestamp → LocalDate
//						LocalDate dateRepayment = LocalDateTime
//						        .parse(repaymentDate, dbFormatter)
//						        .toLocalDate();
//
//						Log.consoleLog(ifr, "parsed date..." + dateRepayment);
//
//						// Final formatted date
//						String dateRepaymentformattedDate = dateRepayment.format(outputFormatter);
//
//		    double installment =
//		            Math.ceil(Double.parseDouble(principal))
//		          + Math.ceil(Double.parseDouble(interest));
//		    Log.consoleLog(ifr, "installment..." + installment);
//
//		    String outstandingBalance = row.get(5);
//		    Log.consoleLog(ifr, "outstandingBalance..." + outstandingBalance);
//		    
//		    String insertQuery =
//		        "INSERT INTO LOS_STG_CBS_AMM_SCH_DETAILS " +
//		        "(INSTALLMENTNO, STARTDATE, REPAYMENTDATE, PRINCIPAL, INTEREST, INSTALLMENT, OUTSTANDINGBALANCE,PROCESSINSTANCEID) " +
//		        "VALUES (" +
//		            installmentNo + ", " +
//		            "'" + startDate + "'" + ", " +
//		            "'" + repaymentDate + "'" + ", " +
//		            "CEIL(" + principal + "), " +
//		            "CEIL(" + interest + "), " +
//		            installment + ", " +
//		            outstandingBalance + ", " +
//		            "'" + processInstanceId + "'" +
//		        ")";
//
//		    cf.mExecuteQuery(ifr, insertQuery, "insertion to LOS_STG_CBS_AMM_SCH_DETAILS ");
//		}
		//Log.consoleLog(ifr, "count of schedule..." + count);

		String schedule2 = "INSERT INTO LOS_STG_CBS_AMM_SCH_DETAILS (INSTALLMENTNO, STARTDATE, REPAYMENTDATE, PRINCIPAL, INTEREST, INSTALLMENT, OUTSTANDINGBALANCE, PROCESSINSTANCEID) SELECT sl_no, TO_CHAR(date_from, 'YYYY-MM-DD HH24:MI:SS'), TO_CHAR(date_to, 'YYYY-MM-DD HH24:MI:SS'), CEIL(emi_princ_amt), CEIL(emi_int_amt), CEIL(emi_princ_amt) + CEIL(emi_int_amt), amt_princ_balance, '"+processInstanceId+"' FROM generate_fpi_emi ("+loanAmt+","+ part1 +" ," + part2 + "," + morotoriamInt + ",'" + formattedDate + "') ORDER BY TO_NUMBER(sl_no) ASC";
		cf.mExecuteQuery(ifr, schedule2, "insertion to LOS_STG_CBS_AMM_SCH_DETAILS ");
		return EMIAmount;
	}

	@Override
	public JSONObject EligibilityAsPerRequestedAmtRO(IFormReference ifr, String value) {
		JSONObject json = new JSONObject();
		json.put("Error", "");
		json.put("Success", "");
		double maxLimitOnSlider = 0;
		String val = "";

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		double totalHlAvail = 0.0;
		double elegibleProjCost = 0.0;
		String loanType = ifr.getValue("LOAN_TYPE_HL").toString();
		if (loanType.equalsIgnoreCase("HOMELOAN")) {
			try {
				String queryForSchemelimitHL = "SELECT TOTAL_HL_AVAIL FROM SLOS_STAFF_HOME_TRN WHERE WINAME = '"
						+ processInstanceId + "'";

				List<List<String>> listqueryForSchemelimitHL = ifr.getDataFromDB(queryForSchemelimitHL);

				if (listqueryForSchemelimitHL != null && !listqueryForSchemelimitHL.isEmpty()) {
					List<String> firstRow = listqueryForSchemelimitHL.get(0);

					if (firstRow != null && !firstRow.isEmpty()) {
						String totalHlAvailStr = firstRow.get(0);

						if (totalHlAvailStr != null && !totalHlAvailStr.trim().isEmpty()) {
							try {
								totalHlAvail = Double.parseDouble(totalHlAvailStr.trim());
							} catch (NumberFormatException nfe) {
								Log.consoleLog(ifr, "Invalid number format for TOTAL_HL_AVAIL: " + totalHlAvailStr);
								totalHlAvail = 0.0;
							}
						}
					}
				}
				String queryForEligibleProjectCost = "SELECT eligible_amount from LOS_CAM_COLLATERAL_DETAILS where PID  = '"
						+ processInstanceId + "'";

				List<List<String>> listqueryForEligibleProjectCost = ifr.getDataFromDB(queryForEligibleProjectCost);

				if (listqueryForEligibleProjectCost != null && !listqueryForEligibleProjectCost.isEmpty()) {
					List<String> secondRow = listqueryForEligibleProjectCost.get(0);

					if (secondRow != null && !secondRow.isEmpty()) {
						String elegibleProjectCost = secondRow.get(0);

						if (elegibleProjectCost != null && !elegibleProjectCost.trim().isEmpty()) {
							try {
								elegibleProjCost = Double.parseDouble(elegibleProjectCost.trim());
							} catch (NumberFormatException nfe) {
								Log.consoleLog(ifr,
										"Invalid number format for ELIGIBLE PROJECT COST " + elegibleProjCost);
								elegibleProjCost = 0.0;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception while fetching TOTAL_HL_AVAIL: " + e.getMessage());
				totalHlAvail = 0.0;
			}
			double amtRequest = 0.0;
			String stage = ifr.getValue("PresentStage").toString();
			// if (!value.equalsIgnoreCase("onLoad")) {
			if (stage.equalsIgnoreCase("PORTAL")) {
				val = ifr.getValue("Req_Loan_Amt_VL").toString().trim();
				amtRequest = Double.parseDouble(val);
			} else {
				val = ifr.getValue("Rec_Loan_Amt_VL").toString().trim();
				amtRequest = Double.parseDouble(val);
			}
			String nthEligiblity = "";
			String queryForNth = "Select ELG_PER_NTH from SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '"
					+ processInstanceId + "'";

			List<List<String>> list = ifr.getDataFromDB(queryForNth);
			Log.consoleLog(ifr, "query for Nth" + queryForNth);
			Log.consoleLog(ifr, "query for Nth list" + list);
			if (!list.isEmpty()) {
				Log.consoleLog(ifr, "Inside Nth list");
				nthEligiblity = list.get(0).get(0);

			}
			double min = Math.min(Math.min(totalHlAvail, elegibleProjCost), Double.parseDouble(nthEligiblity));
			Log.consoleLog(ifr, "amtRequest===" + amtRequest);
			if (amtRequest <= min) {

				JSONObject result = eligibiltyAsSalaryNthHLRO(ifr, amtRequest, value, min, json, totalHlAvail,
						elegibleProjCost);
				if (result.containsKey("Error")) {
					return result;
				} else {
					result.put("Error", "");
					return result;
				}

			} else {
				if (!value.equalsIgnoreCase("onLoad")) {
					json.put("Error", "error, Amount request greater than the MAX LIMIT");
				}
				return json;
			}
		} else {

//			String StaffResumequery = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='Avail Offer'" + "where WINAME='"
//					+ processInstanceId + "'";
//			ifr.saveDataInDB(StaffResumequery);

			String queryBranchCodeAndVehicleCat = "select VEHICLE_CATEGORY from SLOS_STAFF_TRN " + "where WINAME ='"
					+ processInstanceId + "'";
			List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);

			String maxLimitOnSliderQuery = "SELECT MAX_LOAN_AMT_PER_LTV,TOTAL_COST, DOWNPAYMENT_AMOUNT_EXP FROM slos_staff_collateral where winame='"
					+ processInstanceId + "'";
			List<List<String>> maxLimit = ifr.getDataFromDB(maxLimitOnSliderQuery);
			if (maxLimit.isEmpty()) {
				json.put("Error", "error, Max amount not present");
				return json;
			}
			Log.consoleLog(ifr, "maxLimitOnSliderQuery==" + maxLimitOnSliderQuery);
			if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("USED")) {
				val = maxLimit.get(0).get(0).toString().trim().isEmpty() ? "0.0" : maxLimit.get(0).get(0).toString();
			}
			if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("NEW")) {
//				Double totalcostperltv = Double.parseDouble(maxLimit.get(0).get(1))
//						- Double.parseDouble(maxLimit.get(0).get(2));
//				val = String.valueOf(totalcostperltv);
				val = maxLimit.get(0).get(0).toString().trim().isEmpty() ? "0.0" : maxLimit.get(0).get(0).toString();
			}
			Log.consoleLog(ifr, "val===" + val);
			maxLimitOnSlider = Double.parseDouble(val);
			if (maxLimitOnSlider <= 0.0) {
				json.put("Error", "error, Technical glitch");
				return json;
			}

			double amtRequest = 0.0;

			val = ifr.getValue("App_Loan_Amt_VL").toString().trim().isEmpty() ? String.valueOf(maxLimitOnSlider)
					: ifr.getValue("App_Loan_Amt_VL").toString();
			amtRequest = Double.parseDouble(val);

			Log.consoleLog(ifr, "amtRequest===" + amtRequest);
			if (amtRequest <= maxLimitOnSlider) {

				JSONObject result = eligibiltyAsSalaryNthRO(ifr, amtRequest, value);
				if (result.containsKey("Error")) {
					return result;
				} else {
					result.put("Error", "");
					return result;
				}

			} else {
				json.put("Error", "error, Amount request greater than the MAX LIMIT");
				return json;
			}

		}

	}

	private JSONObject eligibiltyAsSalaryNthHLRO(IFormReference ifr, double amtRequest, String value, double min,
			JSONObject json, double totalHlAvail, double elegibleProjCost) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String subProductCode = "";
		String scheduleCode = "";
		String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE,a.SCHEDULE_CODE FROM SLOS_HOME_PRODUCT_SHEET a join  SLOS_STAFF_HOME_TRN b on trim(a.sub_product)=trim(b.hl_product) where b.winame='"
				+ processInstanceId + "'";
		List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
		if (!listqueryproductandScheduleCode.isEmpty()) {
			subProductCode = listqueryproductandScheduleCode.get(0).get(0);
			scheduleCode = listqueryproductandScheduleCode.get(0).get(1);
		}
		String tenure = "360";
		Validator valid = new KnockOffValidator("");
		String query = "select  gross_salary,PENSION_INCOME,STATUTORY_DEDUCTIONS,LOAN_DEDUCTIONS,EXT_LOAN_DED,roi,NET_SALARY from slos_staff_home_trn where winame ='"
				+ processInstanceId + "'";

		List<List<String>> res = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "query===" + query);
		String netSalary = "";
		String grosssalary = "";
		String query1 = "select netsalary,grosssalary from los_nl_occupation_info where F_KEY in (select F_KEY from los_nl_basic_info where PID = '"
				+ processInstanceId + "' and CONSIDERELIGIBILITY = 'Yes' and APPLICANTTYPE = 'CB')";
		List<List<String>> res1 = ifr.getDataFromDB(query1);
		Log.consoleLog(ifr, "query1===" + query1);

		if (res.isEmpty()) {
			json.put("Error", "Technical error.");
			return json;
		}

		if (res1 != null && !res1.isEmpty()) {
			for (List<String> row : res1) {
				if (row != null && !row.isEmpty()) {
					netSalary = row.get(0);
					grosssalary = row.get(1);
					Log.consoleLog(ifr, "NETSALARY = " + netSalary);
					// You can now use netSalary in your logic (e.g., assign to variable,
					// calculation, etc.)
				}
			}
		} else {
			Log.consoleLog(ifr, "No NETSALARY data found for processInstanceId = " + processInstanceId);
		}
        json.put("grossSalary", res.get(0).get(0));
		double grossSalary = Double.parseDouble(valid.getValue(ifr, json, "grossSalary", "0.0"));
		Log.consoleLog(ifr, "grossSalary===" + grossSalary);
		json.put("pension", res.get(0).get(1));
		double pension = Double.parseDouble(valid.getValue(ifr, json, "pension", "0.0"));
		Log.consoleLog(ifr, "pension===" + pension);

		double coBorrowerAmtNet = netSalary.trim().isEmpty() ? 0.0 : Double.parseDouble(netSalary);
		Log.consoleLog(ifr, "coBorrowerAmtNet===" + coBorrowerAmtNet);

		double coBorrowerAmtGross = grosssalary.trim().isEmpty() ? 0.0 : Double.parseDouble(grosssalary);
		Log.consoleLog(ifr, "coBorrowerAmtGross===" + coBorrowerAmtGross);

		json.put("statDed", res.get(0).get(2));
		double statDed = Double.parseDouble(valid.getValue(ifr, json, "statDed", "0.0"));
		Log.consoleLog(ifr, "statDed===" + statDed);

		json.put("loanDed", res.get(0).get(3));
		double loanDed = Double.parseDouble(valid.getValue(ifr, json, "loanDed", "0.0"));
		Log.consoleLog(ifr, "loanDed===" + loanDed);

		json.put("otherDed", res.get(0).get(4));
		double otherDed = Double.parseDouble(valid.getValue(ifr, json, "otherDed", "0.0"));

		Log.consoleLog(ifr, "otherDed===" + otherDed);
		// double roi=Double.parseDouble(res.get(0).get(5).trim());

		json.put("netSal", res.get(0).get(6));
		double netSal = Double.parseDouble(valid.getValue(ifr, json, "netSal", "0.0"));

//64335
		double minNth = (0.3 * (grossSalary + pension)) + (0.3 * coBorrowerAmtGross);
		double totalNetSal = netSal + coBorrowerAmtNet;

		double maxeligibleEmi = totalNetSal - minNth;
		//double netSalAftDed = grossSalary + pension + coBorrowerAmt - (statDed + loanDed + otherDed);
		double totalLoanDed = (statDed + loanDed + otherDed);

		if (value.equalsIgnoreCase("onload")) {
			ifr.setValue("ROI_Type_VL", "Fixed");
			Ammortization ammortization = new Ammortization();

			String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
					ifr.getValue("App_Loan_Amt_VL").toString(), ifr.getValue("App_Tenure_VL").toString(),
					subProductCode, scheduleCode);
			Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
			String[] ammortizationResp = ammortizationResponse.split(":");

			if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
				Log.consoleLog(ifr, "Ammortization inside===================>");
				if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
					json.put("Error", "error, Ammortization error, No Response from the server");
					return json;
				} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
					json.put("Error", ammortizationResp[1]);
					return json;
				}
			}

			JSONObject jsonObj = new JSONObject();
			jsonObj = calculateReqAmmortizationPortalHLRO(ifr, json, processInstanceId, grossSalary, pension, minNth,
					totalLoanDed, totalNetSal, totalNetSal, minNth, totalHlAvail, elegibleProjCost);
			if (jsonObj.containsKey("Error")) {
				return jsonObj;
			}

		}

		String apploanAMt = "";
		String emiM = "";
		String getApprovedLoanAmout = "select APP_LOAN_AMT_VL,MI_ROC_VL from SLOS_STAFF_HOME_TRN where winame='"
				+ processInstanceId + "'";

		List<List<String>> getApprovedLoanAmoutResult = ifr.getDataFromDB(getApprovedLoanAmout);
		if (!getApprovedLoanAmoutResult.isEmpty()) {
			apploanAMt = getApprovedLoanAmoutResult.get(0).get(0);
			emiM = getApprovedLoanAmoutResult.get(0).get(1);
		}
		String amtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(apploanAMt));
		String emiInWords = LoanAmtInWords.amtInWords(Double.parseDouble(emiM));
		String QueryamtInWords = "UPDATE SLOS_STAFF_HOME_TRN SET LOAN_AMOUNT_IN_WORDS= '" + amtInWords
				+ "' WHERE WINAME= '" + processInstanceId + "'";
		Log.consoleLog(ifr, "QueryamtInWords : " + QueryamtInWords);
		ifr.saveDataInDB(QueryamtInWords);
		String QueryamtWords = "UPDATE SLOS_TRN_LOANSUMMARY SET EMI_IN_WORDS= '" + emiInWords + "' WHERE WINAME= '"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "QueryamtInWords : " + QueryamtInWords);
		ifr.saveDataInDB(QueryamtInWords);
		Log.consoleLog(ifr, "QueryamtWords : " + QueryamtWords);
		ifr.saveDataInDB(QueryamtWords);

		return json;
	}

	private JSONObject calculateReqAmmortizationPortalHLRO(IFormReference ifr, JSONObject json,
			String processInstanceId, double grossSalary, double pension, double minNth, double totalLoanDed,
			double netSalAftDed, double currentNthSalary, double minNthVal, double totalHlAvail,
			double elegibleProjCost) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;

		String inPrincipleEligibleLoan = ifr.getValue("Q_SLOS_STAFF_VEHICLE_ELIGIBILITY_IN_PRIN_ELG_LOAN").toString();
		String recLoanAmount = ifr.getValue("Rec_Loan_Amt_VL").toString();

		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);
		String amount = ifr.getValue("App_Loan_Amt_VL").toString();

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
//			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
//			* AccelatorStaffConstant.PMT_VALUE;

			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* Double.parseDouble(amount);

			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH_RO", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			Integer repay = Integer.parseInt(RepaymentAmount) + 1;
			Log.consoleLog(ifr, "repay===>" + repay);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pension);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

//				if (Double.parseDouble(EMIAmount) > eligibileEmi) {
//					json.put("Error", "error,Eligibily emi greater than the net salary after all deducation");
//					return json;
//				}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(totalHlAvail, elegibleProjCost));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			// ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Elg_Per_LTV_RO", String.valueOf(elegibleProjCost));
			ifr.setValue("Final_Elg_VL_RO", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("Monthly_Instal_Maker_ROC", String.format("%.2f", emiAmount));
			ifr.setValue("Monthly_Instal_ROC", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL_RO", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL_RO===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL_RO", IntrestAmount);
			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL_RO===>" + IntrestAmount);
			double netSalAfterAllDed = grossSalary + pension - totalLoanDed;
			String ammortizationIntrestMonthly = "SELECT TotalInstallmentAmount from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
					+ processInstanceId + "' and InstallmentNo ='" + repay + "'";
			List<List<String>> resammortizationIntrestMonthly = ifr.getDataFromDB(ammortizationIntrestMonthly);
			Log.consoleLog(ifr, "ammortizationIntrestMonthly===>" + ammortizationIntrestMonthly);
			if (!resammortizationIntrestMonthly.isEmpty()) {
				String IntrestMonthlyInstallMent = resammortizationIntrestMonthly.get(0).get(0);
				if (IntrestMonthlyInstallMent.isEmpty() || IntrestMonthlyInstallMent == null) {
					json.put("Error", "error,IntrestMonthlyInstallMent is Empty");
					return json;
				}
				Double IntrestMonthlyInstall = Double.parseDouble(IntrestMonthlyInstallMent);
				ifr.setValue("Interest_Monthly_Inst_VL_RO", String.format("%.2f", IntrestMonthlyInstall));
				Log.consoleLog(ifr, "Interest_Monthly_Inst_VL_RO===>" + IntrestMonthlyInstall);
			}
			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			ifr.setValue("Sal_after_monthly_instal_ROC", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			ifr.setValue("NTH_Real_Percent_ROC", String.valueOf(actualNTHPercentage));
			if (actualNTHPercentage < 30) {
				json.put("Error",
						"eeror,The requested loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the requested loan amount and calculate again.");
				return json;
			}

		}
		return json;
	}

	JSONObject eligibiltyAsSalaryNth(IFormReference ifr, double amtRequest, String value) {
		JSONObject json = new JSONObject();
		double maxLimitOnSlider = 0;
		double rcValidUpToMonth = 0;
		int principalTenureFactor = 0;
		int interesetTenureFactor = 0;
		String scheduleCode = "";
		Log.consoleLog(ifr, "inside eligibiltyAsSalaryNth");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryBranchCodeAndVehicleCat = "select VEHICLE_CATEGORY from SLOS_STAFF_TRN " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);

		String maxLimitOnSliderQuery = "SELECT MAX_LOAN_AMT_PER_LTV, TOTAL_COST, DOWNPAYMENT_AMOUNT_EXP, TOTAL_COST_USED,DOWNPAYMENT_AMOUNT_USED FROM slos_staff_collateral where winame='"
				+ processInstanceId + "'";
		List<List<String>> maxLimit = ifr.getDataFromDB(maxLimitOnSliderQuery);
		Log.consoleLog(ifr, "maxLimitOnSliderQuery===" + maxLimitOnSliderQuery);
		if (maxLimit.isEmpty()) {
			json.put("Error", "error, Max amount not present");
			return json;
		}
		if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("USED")) {
			// maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(3).toString())
//					- Double.parseDouble(maxLimit.get(0).get(4).toString());
			double totalCostUsed = Double.parseDouble(maxLimit.get(0).get(3));
			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
			ifr.setValue("Elg_Per_LTV", String.valueOf(maxLimitOnSlider));
			
			double margin= totalCostUsed - amtRequest;
			double marginPercentage=((totalCostUsed - amtRequest)/totalCostUsed)*100;
			
			ifr.setValue("MARGIN_AMT_SVL", String.format("%.2f", margin));
			ifr.setStyle("MARGIN_AMT_SVL", "disable", "true");
			
			ifr.setValue("MARGIN_PERCENT_SVL", String.format("%.2f", marginPercentage));
			ifr.setStyle("MARGIN_PERCENT_SVL", "disable", "true");
		}
		if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("NEW")) {
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(1).toString())
//					- Double.parseDouble(maxLimit.get(0).get(2).toString());
			double totalCost = Double.parseDouble(maxLimit.get(0).get(1));
			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
			ifr.setValue("Elg_Per_LTV", String.valueOf(maxLimitOnSlider));
			
			double margin= totalCost - amtRequest;
			double marginPercentage=((totalCost - amtRequest)/totalCost)*100;
			
			ifr.setValue("MARGIN_AMT_SVL", String.format("%.2f", margin));
			ifr.setStyle("MARGIN_AMT_SVL", "disable", "true");
			ifr.setValue("MARGIN_PERCENT_SVL", String.format("%.2f", marginPercentage));
			ifr.setStyle("MARGIN_PERCENT_SVL", "disable", "true");
		}
		
//		double margin= maxLimitOnSlider - amtRequest;
//		double marginPercentage=((maxLimitOnSlider - amtRequest)/maxLimitOnSlider)*100;
//		
//		ifr.setValue("MARGIN_AMT_SVL", String.valueOf(margin));
//		ifr.setValue("MARGIN_PERCENT_SVL",  String.valueOf(marginPercentage));
		
//		json.put("Error", "");
//		json.put("Success", "");
		double availAmt = 0.0;
		String productCode = "";
		String designation = "";
		String probation = "";
		String purpose = "";
		String fuelType = "";
		double roi = 0.0;
		double fourWheelerAvailable = 0.0;
		double twoWheelerAvailable = 0.0;
		String subProductCode = "";
		String emiType = "";
		String minimumNTHPer = "";
		double minNTHPercentage = 0.0;
		String defaultProductCode = "";
		String val = "";
		int reqTenure = 0;
		double otherDeducation = 0.0;
		String staffTrn = "select gross_salary,comp_years_of_service,pension_income,"
				+ "loan_deduction,statutory_deductions, " + "designation,probation,purpose_loan_vl,fuel_type,"
				+ "four_w_vl_loan_available,two_w_vl_loan_available,EXT_LOAN_DED,vehicle_category,TOTAL_LOAN_DED,PENSIONER"
				+ ",TRUNC(MONTHS_BETWEEN( " + "    CASE "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}-\\d{2}-\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MM-YYYY') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{4}-\\d{2}-\\d{2}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'YYYY-MM-DD') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}/[A-Z]{3}/\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MON-YYYY', 'NLS_DATE_LANGUAGE=ENGLISH') "
				+ "        ELSE NULL " + "    END, " + "    SYSDATE " + ")) AS MONTHS_LEFT "

				+ " from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (staffTrnRes.isEmpty()) {
			json.put("Error", "error, technical glitch");
			return json;
		}
		String residualAgeInMonths = "";
		// String resAgeQuery = "select RES_VEHICLE_AGE_USED from slos_staff_collateral
		// where WINAME='" + processInstanceId
		// + "'";
		String resAgeQuery = "select RES_VEHICLE_AGE_USED,NVL(TRUNC(MONTHS_BETWEEN(RC_VAL_UPTO_USED, SYSDATE)), 0) as rcVal from slos_staff_collateral where WINAME='"
				+ processInstanceId + "'";
		List<List<String>> resAgeQueryRes = ifr.getDataFromDB(resAgeQuery);
		Log.consoleLog(ifr, "resAgeQuery==" + resAgeQuery);
		Log.consoleLog(ifr, "resAgeQueryRes==" + resAgeQueryRes);
		if (!resAgeQueryRes.isEmpty()) {
			residualAgeInMonths = resAgeQueryRes.get(0).get(0).toString();
			rcValidUpToMonth = Double.parseDouble(resAgeQueryRes.get(0).get(1).toString());
			Log.consoleLog(ifr, "residualAgeInMonths==" + residualAgeInMonths);
			Log.consoleLog(ifr, "residualAgeInMonths==" + residualAgeInMonths);
		}
		double grossSalary = Double.parseDouble(staffTrnRes.get(0).get(0).toString());
		double years = Double.parseDouble(staffTrnRes.get(0).get(1).toString());
		String peinsionIn = staffTrnRes.get(0).get(2).toString();
		String pensioner = staffTrnRes.get(0).get(14).toString();
		double pensionIncome = Double
				.parseDouble((peinsionIn.isEmpty() || pensioner.equalsIgnoreCase("NO")) ? "0.0" : peinsionIn);
		double loanDeductions = Double.parseDouble(staffTrnRes.get(0).get(3).toString());
		double statutoryDeductions = Double.parseDouble(staffTrnRes.get(0).get(4).toString());
		double dateOfRetirementInMonths = Double.parseDouble(staffTrnRes.get(0).get(15).toString());

		double minNthVal = 0.0;

		Log.consoleLog(ifr, "grossSalary==" + grossSalary);
		Log.consoleLog(ifr, "years==" + years);
		Log.consoleLog(ifr, "pensionIncome==" + pensionIncome);
		Log.consoleLog(ifr, "loanDeductions==" + loanDeductions);
		Log.consoleLog(ifr, "statutoryDeductions==" + statutoryDeductions);

		designation = staffTrnRes.get(0).get(5).toString();
		probation = staffTrnRes.get(0).get(6).toLowerCase().contains("y") ? "Y" : "N";
		purpose = staffTrnRes.get(0).get(7).toString();
		fuelType = staffTrnRes.get(0).get(8).toString();
		Validator valid = new KnockOffValidator("");
		JSONObject jsonObj = new JSONObject();

		String fourWAvailable = staffTrnRes.get(0).get(9);
		jsonObj.put("Key", fourWAvailable);
		fourWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		;
		fourWheelerAvailable = Double.parseDouble(fourWAvailable);
		Log.consoleLog(ifr, "fourWheelerAvailable==" + fourWheelerAvailable);
		String twoWAvailable = staffTrnRes.get(0).get(10);
		jsonObj.put("Key", twoWAvailable);
		twoWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "twoWAvailable==" + twoWAvailable);
		twoWheelerAvailable = Double.parseDouble(twoWAvailable);

		// change
		if (purpose.trim().toLowerCase().equals("two")) {
			ifr.setValue("Elg_Per_Scale_Scheme", twoWAvailable);
		} else if (purpose.trim().toLowerCase().equals("four")) {
			ifr.setValue("Elg_Per_Scale_Scheme", fourWAvailable);
		}
		// twoWheelerAvailable= valid.getValue(ifr, jsonObj,"Key", "0.0");
		Log.consoleLog(ifr, "twoWheelerAvailable==" + twoWheelerAvailable);
		String otherDed = staffTrnRes.get(0).get(11).toString();
		otherDeducation = Double.parseDouble(otherDed.isEmpty() ? "0.0" : otherDed);
		String vehicleCotegory = staffTrnRes.get(0).get(12).toString();

		String totalLoanDed = staffTrnRes.get(0).get(13).toString();
		jsonObj.put("Key", totalLoanDed);
		totalLoanDed = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "totalLoanDed==" + totalLoanDed);

		Log.consoleLog(ifr, "designation==" + designation);
		Log.consoleLog(ifr, "probation==" + probation);
		Log.consoleLog(ifr, "purpose==" + purpose);
		Log.consoleLog(ifr, "fuelType==" + fuelType);
		Log.consoleLog(ifr, "fourWAvailable==" + fourWAvailable);
		Log.consoleLog(ifr, "twoWAvailable==" + twoWAvailable);
		Log.consoleLog(ifr, "otherDed==" + otherDeducation);
		Log.consoleLog(ifr, "vehicleCotegory==" + vehicleCotegory);
		String schemeLimitQuery = "SELECT prd_rng_to, category,SUB_PRODUCT_CODE_CBS,"
				+ "SUB_PRODUCT,AMMORTIZATION_PORTAL_RATE_OF_INTEREST,roi_type,prd_rng_from,AMMORTIZATION_PORTAL_SCHEDULE_CODE,MINIMUM_NTH_PERCENT,AMMORTIZATION_PORTAL_PRODUCT_CODE "
				+ "from staff_vl_prd_designation_matrix " + "where designation='" + designation
				+ "' and lower(loan_purpose) like lower('%" + purpose + "%') and lower(loan_purpose) like lower('%"
				+ vehicleCotegory + "%') and fuel_type='" + fuelType + "' and PROBATION_TAG='" + probation + "'";
		List<List<String>> schemeLimitQueryRes = ifr.getDataFromDB(schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQuery==" + schemeLimitQuery);
		if (!schemeLimitQueryRes.isEmpty()) {
			Log.consoleLog(ifr, "productCode" + productCode);
			productCode = schemeLimitQueryRes.get(0).get(2);
			Log.consoleLog(ifr, "productCode==" + productCode);
			roi = Double.parseDouble(schemeLimitQueryRes.get(0).get(4));
			Log.consoleLog(ifr, "roi==" + roi);
			subProductCode = schemeLimitQueryRes.get(0).get(3);
			Log.consoleLog(ifr, "subProductCode===" + subProductCode);
			emiType = schemeLimitQueryRes.get(0).get(5);
			Log.consoleLog(ifr, "emiType===" + emiType);

			scheduleCode = schemeLimitQueryRes.get(0).get(7);
			Log.consoleLog(ifr, "scheduleCode===" + scheduleCode);
			minNTHPercentage = Double.parseDouble(schemeLimitQueryRes.get(0).get(8).trim());
			Log.consoleLog(ifr, "minNTHPercentage===" + minNTHPercentage);
			String stage = ifr.getValue("PresentStage").toString();
			if (ifr.getValue("Req_Loan_Amt_VL").toString().trim().isEmpty() && stage.equalsIgnoreCase("PORTAL")) {
				amtRequest = Math.min(amtRequest, Double.parseDouble(schemeLimitQueryRes.get(0).get(0)));
			} else {
				amtRequest = Math.min(amtRequest, Double.parseDouble(schemeLimitQueryRes.get(0).get(0)));
			}
			if (stage.equalsIgnoreCase("PORTAL")) {
				defaultProductCode = schemeLimitQueryRes.get(0).get(9);
			} else {

				String queryForScheduleType = "SELECT SCHEDULE_CODE,PRODUCT_CODE_VL FROM SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "queryForScheduleType query===>" + queryForScheduleType);
				List<List<String>> res = ifr.getDataFromDB(queryForScheduleType);
				Log.consoleLog(ifr, "res===>" + res);
				if (!res.isEmpty()) {
					scheduleCode = res.get(0).get(0);
					defaultProductCode = res.get(0).get(1);
				}
			}
			Log.consoleLog(ifr, "defaultProductCode===" + defaultProductCode);
			Log.consoleLog(ifr, "scheduleCode===" + scheduleCode);
		}
		String stage = ifr.getValue("PresentStage").toString();
		if (stage.equalsIgnoreCase("PORTAL") && !(amtRequest >= Double.parseDouble(schemeLimitQueryRes.get(0).get(6))
				&& amtRequest <= Double.parseDouble(schemeLimitQueryRes.get(0).get(0))

		)) {
			Log.consoleLog(ifr, "amtRequest if condition failed");
			json.put("Error", "error, please select amount between = " + schemeLimitQueryRes.get(0).get(6) + " to = "
					+ schemeLimitQueryRes.get(0).get(0));
			return json;
		} else if (stage.equalsIgnoreCase("BACKOFFICE")
				&& !(amtRequest >= Double.parseDouble(schemeLimitQueryRes.get(0).get(6))
						&& amtRequest <= Double.parseDouble(schemeLimitQueryRes.get(0).get(0))

				)) {
			Log.consoleLog(ifr, "amtRequest if condition failed");
			json.put("Error", "error, please select amount between = " + schemeLimitQueryRes.get(0).get(6) + " to = "
					+ schemeLimitQueryRes.get(0).get(0));
			return json;
		}
		Log.consoleLog(ifr, "roi==" + roi);
		ifr.setValue("ROI_VL", String.valueOf(roi * 100));
		Log.consoleLog(ifr, "productCode==" + productCode);
		Log.consoleLog(ifr, "roi==" + roi);
		Log.consoleLog(ifr, "subProductCode==" + subProductCode);
		Log.consoleLog(ifr, "emiType==" + emiType);
		if (productCode.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		String productSheetQuery = "SELECT PRD_MINTERM,PRD_TERM,Principal_tenure_factor,Interest_tenure_factor from staff_vl_product_sheet where prd_desc='"
				+ subProductCode + "'";
		List<List<String>> prdSheetRes = ifr.getDataFromDB(productSheetQuery);
		if (prdSheetRes.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		double minTenure = Double.parseDouble(prdSheetRes.get(0).get(0));
		// if()

		double maxTenure = Double.parseDouble(prdSheetRes.get(0).get(1));

		if (vehicleCotegory.equalsIgnoreCase("used")) {
			// maxTenure = Math.min(maxTenure, Double.parseDouble(residualAgeInMonths));
//			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),
//					Math.min(dateOfRetirementInMonths, rcValidUpToMonth)));
			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),rcValidUpToMonth));
		}
		principalTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(2));
		interesetTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(3));
		Log.consoleLog(ifr, "minTenure==" + minTenure);
		Log.consoleLog(ifr, "maxTenure==" + maxTenure);
		stage = ifr.getValue("PresentStage").toString();
		// String stage = "PORTAL";
		if (stage.equalsIgnoreCase("PORTAL")) {
			val = ifr.getValue("Req_Tenure_VL").toString().isEmpty() ? String.valueOf((int) maxTenure)
					: String.valueOf((int) Double.parseDouble(String.valueOf(ifr.getValue("Req_Tenure_VL"))));
			reqTenure = Integer.parseInt(val);
		} else {
			val = ifr.getValue("Rec_Loan_Tenure_VL").toString().isEmpty() ? String.valueOf((int) maxTenure)
					: String.valueOf((int) Double.parseDouble(String.valueOf(ifr.getValue("Rec_Loan_Tenure_VL"))));
			reqTenure = Integer.parseInt(val);
		}
		Log.consoleLog(ifr, "reqTenure====" + reqTenure);
		minNthVal = (minNTHPercentage * (pensionIncome + grossSalary));
		Log.consoleLog(ifr, "minNthVal====" + minNthVal);

		// if(productCode.trim().equals("700")){
		// minNthVal=(AccelatorStaffConstant.NTH_VALUE_700*(pensionIncome+grossSalary));
		// }
		Log.consoleLog(ifr, "minNthVal==" + minNthVal);
		// ifr.setValue("Elg_Per_NTH", String.valueOf(minNthVal));
		String pension = "";
		String totalGrossSal = "";
		double currentNthSalary = 0.0;
		double totalGrossSa = 0.0;
		double totalnetsalary = 0.0;
		String queryForPensionType = "SELECT PENSIONER,PENSION_INCOME FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForPensionType query===>" + queryForPensionType);
		List<List<String>> res = ifr.getDataFromDB(queryForPensionType);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			pension = res.get(0).get(0);
		}
		if (res.size() > 0 && pension.equalsIgnoreCase("YES")) {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary + pensionIncome - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			totalGrossSa = grossSalary + pensionIncome;
			totalnetsalary = netsalary + pensionIncome;
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + totalGrossSa
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(totalnetsalary) + "' WHERE WINAME= '"
					+ processInstanceId + "'";
			ifr.saveDataInDB(QueryNetSal);

		} else {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + grossSalary
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(netsalary) + "' WHERE WINAME= '" + processInstanceId
					+ "'";
			ifr.saveDataInDB(QueryNetSal);
		}
		if (!(reqTenure >= minTenure && reqTenure <= maxTenure)) {
			json.put("Error", "error, Eligbile tenure from = " + minTenure + " to = " + maxTenure);
			return json;
		} else {
			ifr.setValue("Max_Tenure_VL", String.valueOf(maxTenure));
		}
		double interesntRepaymentTerm = 0.0;
		double principleRepaymentTerm = maxTenure * principalTenureFactor
				/ (principalTenureFactor + interesetTenureFactor);
		Log.consoleLog(ifr, "principleRepaymentTerm==" + principleRepaymentTerm);
		interesntRepaymentTerm = maxTenure * interesetTenureFactor / (principalTenureFactor + interesetTenureFactor);
		int principalFloor = (int) Math.floor(principleRepaymentTerm);
		int intrestCeil = (int) Math.ceil(interesntRepaymentTerm);
		int eligibleAsPerNth = 0;
		double eligibleAsPNth = 0.0;
		String EMIAmount = "";
		String prob = "";

		Log.consoleLog(ifr, "eligibleAsPerNth if ");

		String probationQuery = "SELECT probation from slos_staff_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (!queryRes.isEmpty()) {
			prob = queryRes.get(0).get(0);
		} else {
			json.put("Error", "error,probation field is empty");
			return json;
		}
		// String roiR = String.valueOf(roi * 100);
		stage = ifr.getValue("PresentStage").toString();
		// String stage = "PORTAL";
		String tenure = "";
		if (stage.equalsIgnoreCase("PORTAL")) {
			defaultProductCode = schemeLimitQueryRes.get(0).get(9);
			if (value.equalsIgnoreCase("onload")) {
				ifr.setValue("ROI_Type_VL", emiType);
				Ammortization ammortization = new Ammortization();
				if (prob.equalsIgnoreCase("No")) {
					JSONObject calculateNthAndInpriciplePortalProbN = calculateNthAndInpriciplePortalProbN(ifr,
							amtRequest, value, json, maxLimitOnSlider, scheduleCode, processInstanceId, purpose,
							fourWheelerAvailable, twoWheelerAvailable, defaultProductCode, minNthVal, currentNthSalary,
							prob, ammortization, tenure, vehicleCotegory);
					if (calculateNthAndInpriciplePortalProbN.containsKey("Error")) {
						return calculateNthAndInpriciplePortalProbN;
					}
				} else {
					JSONObject calculateNthAndInpriciplePortalProbY = calculateNthAndInpriciplePortalProbY(ifr,
							amtRequest, value, json, maxLimitOnSlider, scheduleCode, processInstanceId, purpose,
							fourWheelerAvailable, twoWheelerAvailable, defaultProductCode, minNthVal, currentNthSalary,
							prob, ammortization, tenure, vehicleCotegory);
					if (calculateNthAndInpriciplePortalProbY.containsKey("Error")) {
						return calculateNthAndInpriciplePortalProbY;
					}

				}

			}

			else {
				Ammortization ammortization = new Ammortization();
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						ifr.getValue("Req_Loan_Amt_VL").toString(), ifr.getValue("Req_Tenure_VL").toString(),
						defaultProductCode, scheduleCode);
				Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
				String[] ammortizationResp = ammortizationResponse.split(":");

				if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
					Log.consoleLog(ifr, "Ammortization inside===================>");
					if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
						json.put("Error", "error, Ammortization error, No Response from the server");
						return json;
					} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
						json.put("Error", ammortizationResp[1]);
						return json;
					}
				}

				if (prob.equalsIgnoreCase("No")) {
					jsonObj = calculateReqAmmortizationPortalProbN(ifr, json, processInstanceId, grossSalary,
							pensionIncome, minNthVal, totalLoanDed, currentNthSalary, minNthVal);
					if (jsonObj.containsKey("Error")) {
						return jsonObj;
					}

				}

				if (prob.equalsIgnoreCase("Yes")) {
					jsonObj = calculateReqAmmortizationPortalProbY(ifr, json, processInstanceId, grossSalary,
							pensionIncome, minNthVal, totalLoanDed);
					if (jsonObj.containsKey("Error")) {
						return jsonObj;
					}

				}

			}

		} else {
			if (value.equalsIgnoreCase("onload")) {
				Ammortization ammortization = new Ammortization();
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						ifr.getValue("Rec_Loan_Amt_VL").toString(), ifr.getValue("Rec_Loan_Tenure_VL").toString(),
						defaultProductCode, scheduleCode);
				Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
				String[] ammortizationResp = ammortizationResponse.split(":");

				if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
					Log.consoleLog(ifr, "Ammortization inside===================>");
					if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
						json.put("Error", "error, Ammortization error, No Response from the server");
						return json;
					} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
						json.put("Error", ammortizationResp[1]);
						return json;
					}
				}

				if (prob.equalsIgnoreCase("No")) {
					jsonObj = calculatePaymentsBackOfficeProbN(ifr, json, processInstanceId, grossSalary, pensionIncome,
							totalLoanDed, currentNthSalary, minNthVal, maxLimitOnSlider, purpose, twoWheelerAvailable,
							fourWheelerAvailable);
					if (jsonObj.containsKey("Error")) {
						return jsonObj;
					}

				}

				if (prob.equalsIgnoreCase("Yes")) {
					jsonObj = calculatePaymentsBackOfficeProbY(ifr, json, processInstanceId, grossSalary, pensionIncome,
							totalLoanDed, currentNthSalary, minNthVal, maxLimitOnSlider, purpose, twoWheelerAvailable,
							fourWheelerAvailable);
					if (jsonObj.containsKey("Error")) {
						return jsonObj;
					}

				}

			}
		}
		return jsonObj;

	}

	private JSONObject calculatePaymentsBackOfficeProbY(IFormReference ifr, JSONObject json, String processInstanceId,
			double grossSalary, double pensionIncome, String totalLoanDed, double currentNthSalary, double minNthVal,
			double maxLimitOnSlider, String purpose, double twoWheelerAvailable, double fourWheelerAvailable) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;
		double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
//		String inPrincipleEligibleLoan = ifr.getValue("Q_SLOS_STAFF_VEHICLE_ELIGIBILITY_IN_PRIN_ELG_LOAN").toString();
//		String recLoanAmount = ifr.getValue("Rec_Loan_Amt_VL").toString();
//		
//		if (!inPrincipleEligibleLoan.isBlank() && !recLoanAmount.isEmpty()) {
//		try {
//			if (Double.parseDouble(recLoanAmount) > Double.parseDouble(inPrincipleEligibleLoan)) {
//				json.put("Error",
//						"error,required Loan Amount is greater than Inprinciple eligible loan, please enter less amount");
//				return json;
//			}
//		} catch (NumberFormatException e) {
//			json.put("Error", "error,Server Error");
//			return json;
//		}
//	}
		// EMI - CALENDAR
		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' AND (STAGENAME LIKE '%EMI%' OR STAGENAME = 'E M I')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* Double.parseDouble(ifr.getValue("Rec_Loan_Amt_VL").toString());
			ifr.setValue("Monthly_Instal_Maker", String.valueOf(EMIAmount));
			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

			double availAmt = 0.0;
			if (purpose.trim().toLowerCase().equals("two")) {
				availAmt = twoWheelerAvailable;
				ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(twoWheelerAvailable));
			} else if (purpose.trim().toLowerCase().equals("four")) {
				availAmt = fourWheelerAvailable;
				ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(fourWheelerAvailable));
			}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Elg_Per_LTV", String.valueOf(maxLimitOnSlider));
			ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + EMIAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL", IntrestAmount);
			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + IntrestAmount);
			ifr.setValue("Interest_Monthly_Inst_VL", "NA");

			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			String formattedinp = String.format("%.2f", netSalaryAfterMonthlyInstall);
			netSalaryAfterMonthlyInstall = Double.parseDouble(formattedinp);
			ifr.setValue("Sal_after_monthly_insta_Maker", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			String formatted = String.format("%.2f", actualNTHPercentage);
			actualNTHPercentage = Double.parseDouble(formatted);
			ifr.setValue("NTH_Real_Percent_Maker", String.valueOf(actualNTHPercentage));
			String value = ifr.getValue("Rec_Loan_Amt_VL").toString();
			if (value != null && !value.isEmpty()) {
				if (Double.parseDouble(value) > roundedVal) {
					json.put("Error",
							"error,The recommended loan amount is higher than the Final Eligiblity. Please reduce the recommended loan amount and calculate again.");
					return json;
				}
			}
			if (actualNTHPercentage < 40) {
				json.put("Error",
						"error,The recommended loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the recommended loan amount and calculate again.");
				return json;
			}

		}
		return json;
	}

	private JSONObject calculatePaymentsBackOfficeProbN(IFormReference ifr, JSONObject json, String processInstanceId,
			double grossSalary, double pensionIncome, String totalLoanDed, double currentNthSalary, double minNthVal,
			double maxLimitOnSlider, String purpose, double twoWheelerAvailable, double fourWheelerAvailable) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;
		double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

//		String inPrincipleEligibleLoan = ifr.getValue("Q_SLOS_STAFF_VEHICLE_ELIGIBILITY_IN_PRIN_ELG_LOAN").toString();
//		String recLoanAmount = ifr.getValue("Rec_Loan_Amt_VL").toString();
//		
//		if (!inPrincipleEligibleLoan.isBlank() && !recLoanAmount.isEmpty()) {
//		try {
//			if (Double.parseDouble(recLoanAmount) > Double.parseDouble(inPrincipleEligibleLoan)) {
//				json.put("Error",
//						"error,required Loan Amount is greater than Inprinciple eligible loan, please enter less amount");
//				return json;
//			}
//		} catch (NumberFormatException e) {
//			json.put("Error", "error,Server Error");
//			return json;
//		}
//	}

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* Double.parseDouble(ifr.getValue("Rec_Loan_Amt_VL").toString());

			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
			ifr.setValue("Monthly_Instal_Maker", String.valueOf(EMIAmount));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			Integer repay = Integer.parseInt(RepaymentAmount) + 1;
			Log.consoleLog(ifr, "repay===>" + repay);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

			// change
			double availAmt = 0.0;
			if (purpose.trim().toLowerCase().equals("two")) {
				availAmt = twoWheelerAvailable;
				ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(twoWheelerAvailable));
			} else if (purpose.trim().toLowerCase().equals("four")) {
				availAmt = fourWheelerAvailable;
				ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(fourWheelerAvailable));
			}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Elg_Per_LTV", String.valueOf(maxLimitOnSlider));
			ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL", IntrestAmount);
			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + IntrestAmount);

			String ammortizationIntrestMonthly = "SELECT TotalInstallmentAmount from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
					+ processInstanceId + "' and InstallmentNo ='" + repay + "'";
			List<List<String>> resammortizationIntrestMonthly = ifr.getDataFromDB(ammortizationIntrestMonthly);
			Log.consoleLog(ifr, "ammortizationIntrestMonthly===>" + ammortizationIntrestMonthly);
			if (!resammortizationIntrestMonthly.isEmpty()) {
				String IntrestMonthlyInstallMent = resammortizationIntrestMonthly.get(0).get(0);
				if (IntrestMonthlyInstallMent.isEmpty() || IntrestMonthlyInstallMent == null) {
					json.put("Error", "error,IntrestMonthlyInstallMent is Empty");
					return json;
				}
				Double IntrestMonthlyInstall = Double.parseDouble(IntrestMonthlyInstallMent);
				ifr.setValue("Interest_Monthly_Inst_VL", String.format("%.2f", IntrestMonthlyInstall));
				Log.consoleLog(ifr, "Interest_Monthly_Inst_VL===>" + IntrestMonthlyInstall);
			}

			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			String formattedinp = String.format("%.2f", netSalaryAfterMonthlyInstall);
			netSalaryAfterMonthlyInstall = Double.parseDouble(formattedinp);
			ifr.setValue("Sal_after_monthly_insta_Maker", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			String formatted = String.format("%.2f", actualNTHPercentage);
			actualNTHPercentage = Double.parseDouble(formatted);
			ifr.setValue("NTH_Real_Percent_Maker", String.valueOf(actualNTHPercentage));

			String value = ifr.getValue("Rec_Loan_Amt_VL").toString();
			if (value != null && !value.isEmpty()) {
				if (Double.parseDouble(value) > roundedVal) {
					json.put("Error",
							"error,The recommended loan amount is higher than the Final Eligiblity. Please reduce the recommended loan amount and calculate again.");
					return json;
				}
			}

			if (actualNTHPercentage < 30) {
				json.put("Error",
						"error,The recommended loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the recommended loan amount and calculate again.");
				return json;
			}

		}
		return json;
	}

	private JSONObject calculateReqAmmortizationPortalProbY(IFormReference ifr, JSONObject json,
			String processInstanceId, double grossSalary, double pensionIncome, double minNthVal, String totalLoanDed) {
		String EMIAmount;

		String inPrincipleEligibleLoan = ifr.getValue("In_Prin_Elg_Loan").toString();
		String requiredLoanAmount = ifr.getValue("Req_Loan_Amt_VL").toString();

		if (!inPrincipleEligibleLoan.isBlank() && !requiredLoanAmount.isEmpty()) {
			try {
				if (Double.parseDouble(requiredLoanAmount) > Double.parseDouble(inPrincipleEligibleLoan)) {
					json.put("Error",
							"error,required Loan Amount is greater than Inprinciple eligible loan, please enter less amount");
					return json;
				}
			} catch (NumberFormatException e) {
				json.put("Error", "error,Server Error");
				return json;
			}
		}

		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1'AND (STAGENAME LIKE '%EMI%' OR STAGENAME = 'E M I')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
//			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
//			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
//				json.put("Error", "error,IntrestAmount is Empty");
//				return json;
//			}
//			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

			double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
			double eligibileEmi = netSalAfterAllDed - minNthVal;
			Log.consoleLog(ifr, "eligibileEmi===>" + eligibileEmi);
			ifr.setValue("MAX_ELG_EMI", String.format("%.2f", eligibileEmi));

			if (Double.parseDouble(EMIAmount) > eligibileEmi) {
				json.put("Error", "error,Eligibily emi greater than the net salary after all deducation");
				return json;
			}

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + EMIAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL", "NA");
			// Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + IntrestAmount);
			ifr.setValue("Interest_Monthly_Inst_VL", "NA");
		}
		return json;
	}

	private JSONObject calculateReqAmmortizationPortalProbN(IFormReference ifr, JSONObject json,
			String processInstanceId, double grossSalary, double pensionIncome, double minNthVal, String totalLoanDed,
			double currentNthSalary, double minNthVal2) {
		String EMIAmount;

		String inPrincipleEligibleLoan = ifr.getValue("In_Prin_Elg_Loan").toString();
		Log.consoleLog(ifr, "inPrincipleEligibleLoan===>" + inPrincipleEligibleLoan);
		String requiredLoanAmount = ifr.getValue("Req_Loan_Amt_VL").toString();
		Log.consoleLog(ifr, "requiredLoanAmount===>" + requiredLoanAmount);

		if (!inPrincipleEligibleLoan.isBlank() && !requiredLoanAmount.isEmpty()) {
			try {
				if (Double.parseDouble(requiredLoanAmount) > Double.parseDouble(inPrincipleEligibleLoan)) {
					json.put("Error",
							"error,required Loan Amount is greater than Inprinciple eligible loan, please enter less amount");
					return json;
				}
			} catch (NumberFormatException e) {
				json.put("Error", "error,Server Error");
				return json;
			}
		}

		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			Integer repay = Integer.parseInt(RepaymentAmount) + 1;
			Log.consoleLog(ifr, "repay===>" + repay);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);
			double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
			double eligibileEmi = netSalAfterAllDed - minNthVal;
			Log.consoleLog(ifr, "eligibileEmi===>" + eligibileEmi);
			ifr.setValue("MAX_ELG_EMI", String.format("%.2f", eligibileEmi));

			if (Double.parseDouble(EMIAmount) > eligibileEmi) {
				json.put("Error", "error,Eligibily emi greater than the net salary after all deducation");
				return json;
			}

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL", IntrestAmount);
			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + IntrestAmount);

			String ammortizationIntrestMonthly = "SELECT TotalInstallmentAmount from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
					+ processInstanceId + "' and InstallmentNo ='" + repay + "'";
			List<List<String>> resammortizationIntrestMonthly = ifr.getDataFromDB(ammortizationIntrestMonthly);
			Log.consoleLog(ifr, "ammortizationIntrestMonthly===>" + ammortizationIntrestMonthly);
			if (!resammortizationIntrestMonthly.isEmpty()) {
				String IntrestMonthlyInstallMent = resammortizationIntrestMonthly.get(0).get(0);
				Double IntrestMonthlyInstall = Double.parseDouble(IntrestMonthlyInstallMent);
				ifr.setValue("Interest_Monthly_Inst_VL", String.format("%.2f", IntrestMonthlyInstall));
				Log.consoleLog(ifr, "Interest_Monthly_Inst_VL===>" + IntrestMonthlyInstall);
			} else {
				json.put("Error", "error,IntrestMonthlyInstallMent is Empty");
				return json;
			}
		}
		return json;
	}

	private JSONObject calculateReqAmmortizationPortalHL(IFormReference ifr, JSONObject json, String processInstanceId,
			double grossSalary, double pensionIncome, double minNthVal, double totalLoanDed, double eligibleEMI,
			double min, String valueOn) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;
		double netSalAfterAllDed = grossSalary + pensionIncome - totalLoanDed;
		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId
				+ "' and stagenumber='1' and (STAGENAME LIKE '%FPI%' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

//			String inPrincipleEligibleLoan = ifr.getValue("Q_SLOS_STAFF_VEHICLE_ELIGIBILITY_IN_PRIN_ELG_LOAN").toString();
//			String recLoanAmount = ifr.getValue("Rec_Loan_Amt_VL").toString();
//			
//			if (!inPrincipleEligibleLoan.isBlank() && !recLoanAmount.isEmpty()) {
//			try {
//				if (Double.parseDouble(recLoanAmount) > Double.parseDouble(inPrincipleEligibleLoan)) {
//					json.put("Error",
//							"error,required Loan Amount is greater than Inprinciple eligible loan, please enter less amount");
//					return json;
//				}
//			} catch (NumberFormatException e) {
//				json.put("Error", "error,Server Error");
//				return json;
//			}
//		}

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			// Log.consoleLog(ifr, "currentNthSalary" + currentNthSalary);
			Log.consoleLog(ifr, "minNthVal" + minNthVal);
			if (valueOn.equalsIgnoreCase("onLoad")) {
				eligibleAsPNth = eligibleEMI / Double.parseDouble(EMIAmount)
						* Double.parseDouble(ifr.getValue("Rec_Loan_Amt_VL").toString());

				eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
				ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
				ifr.setValue("Monthly_Instal_Maker", String.valueOf(EMIAmount));
				Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
				Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
				if (eligibleAsPerNth <= 0.0) {
					json.put("Error", "error, Eligiblity as per nth salary is less than zero");
					return json;
				}
			}
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			Integer repay = Integer.parseInt(RepaymentAmount) + 1;
			Log.consoleLog(ifr, "repay===>" + repay);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

			double eligibleLoanAmt = min;
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			// ifr.setValue("Elg_Per_LTV", String.valueOf(maxLimitOnSlider));
			ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
//			ifr.setValue("Req_Prin_Repay_Tenure_VL", RepaymentAmount);
//			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL===>" + RepaymentAmount);
//			ifr.setValue("Req_Int_Repay_Tenure_VL", IntrestAmount);
//			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL===>" + IntrestAmount);

			// double eligibileEmi=netSalAfterAllDed -minNthVal;
			ifr.setValue("MAX_ELG_EMI", String.format("%.2f", eligibleEMI));
			
			String StaffFinalMaxEmi = "UPDATE SLOS_STAFF_VL_ELIGIBILITY SET FINAL_ELG_VL_BM='"+eligibleEMI+"'  where WINAME='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "StaffFinalMaxEmi===>" + StaffFinalMaxEmi);
			ifr.saveDataInDB(StaffFinalMaxEmi);

			if (Double.parseDouble(EMIAmount) > eligibleEMI) {
				json.put("Error", "error,Eligibily emi greater than the net salary after all deducation");
				return json;
			}

			String ammortizationIntrestMonthly = "SELECT TotalInstallmentAmount from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
					+ processInstanceId + "' and InstallmentNo ='" + repay + "'";
			List<List<String>> resammortizationIntrestMonthly = ifr.getDataFromDB(ammortizationIntrestMonthly);
			Log.consoleLog(ifr, "ammortizationIntrestMonthly===>" + ammortizationIntrestMonthly);
			if (!resammortizationIntrestMonthly.isEmpty()) {
				String IntrestMonthlyInstallMent = resammortizationIntrestMonthly.get(0).get(0);
				if (IntrestMonthlyInstallMent.isEmpty() || IntrestMonthlyInstallMent == null) {
					json.put("Error", "error,IntrestMonthlyInstallMent is Empty");
					return json;
				}
				Double IntrestMonthlyInstall = Double.parseDouble(IntrestMonthlyInstallMent);
				ifr.setValue("Interest_Monthly_Inst_VL", String.format("%.2f", IntrestMonthlyInstall));
				Log.consoleLog(ifr, "Interest_Monthly_Inst_VL===>" + IntrestMonthlyInstall);
			}

			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			String formattedinp = String.format("%.2f", netSalaryAfterMonthlyInstall);
			netSalaryAfterMonthlyInstall = Double.parseDouble(formattedinp);
			ifr.setValue("Sal_after_monthly_insta_Maker", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			String formatted = String.format("%.2f", actualNTHPercentage);
			actualNTHPercentage = Double.parseDouble(formatted);
			ifr.setValue("NTH_Real_Percent_Maker", String.valueOf(actualNTHPercentage));

			String value = ifr.getValue("Rec_Loan_Amt_VL").toString();
			if (value != null && !value.isEmpty()) {
				if (Double.parseDouble(value) > roundedVal) {
					json.put("Error",
							"error,The recommended loan amount is higher than the Final Eligiblity. Please reduce the recommended loan amount and calculate again.");
					return json;
				}
			}

			if (value.equalsIgnoreCase("onload")) {
				if (actualNTHPercentage < 30) {
					json.put("Error",
							"error,The recommended loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the recommended loan amount and calculate again.");
					return json;
				}
			}

		}
		return json;
	}

	private JSONObject calculateNthAndInpriciplePortalProbY(IFormReference ifr, double amtRequest, String value,
			JSONObject json, double maxLimitOnSlider, String scheduleCode, String processInstanceId, String purpose,
			double fourWheelerAvailable, double twoWheelerAvailable, String defaultProductCode, double minNthVal,
			double currentNthSalary, String prob, Ammortization ammortization, String residualAgeInMonths,
			String vehicleCotegory) {
		double availAmt;
		double eligibleAsPNth;
		String nthEligiblity = "";
		int eligibleAsPerNth;

		String queryForNth = "Select ELG_PER_NTH from SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '" + processInstanceId
				+ "'";

		List<List<String>> list = ifr.getDataFromDB(queryForNth);
		Log.consoleLog(ifr, "query for Nth" + queryForNth);
		Log.consoleLog(ifr, "query for Nth list" + list);
		if (!list.isEmpty()) {
			Log.consoleLog(ifr, "Inside Nth list");
			nthEligiblity = list.get(0).get(0);

		}
		eligibleAsPerNth = (int) Math.floor(Integer.parseInt(nthEligiblity));
		Log.consoleLog(ifr, "eligibleAsPerNth" + eligibleAsPerNth);
		ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
		Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
		Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
		if (eligibleAsPerNth <= 0.0) {
			json.put("Error", "error, Eligiblity as per nth salary is less than zero");
			return json;
		}
		Log.consoleLog(ifr, "eligibleAsPerNth if ==" + nthEligiblity);
		if (Integer.parseInt(nthEligiblity) <= 0.0) {
			json.put("Error", "error, Eligiblity as per nth salary is less than zero");
			return json;
		}
		if (purpose.toLowerCase().contains("four")) {
			Log.consoleLog(ifr, "inside if fourWheelerAvailable==" + fourWheelerAvailable);
			availAmt = fourWheelerAvailable;
		} else {
			Log.consoleLog(ifr, "inside else twoWheelerAvailable==" + twoWheelerAvailable);
			availAmt = twoWheelerAvailable;
		}
		Log.consoleLog(ifr, "availAmt==" + availAmt);

		double eligibleLoanAmt = Math.min(Integer.parseInt(nthEligiblity), Math.min(maxLimitOnSlider, availAmt));
		Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
		//double finalEligiblity = eligibleLoanAmt;
		//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
		double finalEligiblity = eligibleLoanAmt;
		long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
		Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
		Log.consoleLog(ifr, "roundedVal===" + roundedVal);
		String finalRoundedVal = String.valueOf(roundedVal);
		ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
		ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));
		if (purpose.trim().toLowerCase().equals("four")) {
			ifr.setValue("Principal_Repay_Tenure_VL", "180");
			ifr.setValue("Interest_Repay_Tenure_VL", "NA");
		} else if (purpose.trim().toLowerCase().equals("two")) {
			ifr.setValue("Principal_Repay_Tenure_VL", "84");
			ifr.setValue("Interest_Repay_Tenure_VL", "NA");
		}

//			if (amtRequest > roundedVal) {
//				json.put("Error", "error, Requested loan amount is more than" + eligibleLoanAmt);
//				return json;
//			}
		if (roundedVal <= 0) {
			json.put("Error", "error, In-Principle Eligible Loan is less than zero");
			return json;
		}
		Log.consoleLog(ifr, "finalEligiblity==" + finalEligiblity);

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + roundedVal + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		return json;

	}

	private JSONObject getAmmortization(IFormReference ifr, JSONObject json, String ammortizationResponse) {
		Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
		String[] ammortizationResp = ammortizationResponse.split(":");

		if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
			Log.consoleLog(ifr, "Ammortization inside===================>");
			if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
				json.put("Error", "error, Ammortization error, No Response from the server");
				return json;
			} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
				json.put("Error", ammortizationResp[1]);
				return json;
			}
		}
		return json;
	}

	private JSONObject calculateNthAndInpriciplePortalProbN(IFormReference ifr, double amtRequest, String value,
			JSONObject json, double maxLimitOnSlider, String scheduleCode, String processInstanceId, String purpose,
			double fourWheelerAvailable, double twoWheelerAvailable, String defaultProductCode, double minNthVal,
			double currentNthSalary, String prob, Ammortization ammortization, String residualAgeInMonths,
			String vehicleCotegory) {
		double availAmt;
		int eligibleAsPerNth;
		double eligibleAsPNth;
		String nthEligiblity = "";
		String ammortizationEmiQuery = "";

		String queryForNth = "Select ELG_PER_NTH from SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '" + processInstanceId
				+ "'";

		List<List<String>> list = ifr.getDataFromDB(queryForNth);
		Log.consoleLog(ifr, "query for Nth" + queryForNth);
		Log.consoleLog(ifr, "query for Nth list" + list);
		if (!list.isEmpty()) {
			Log.consoleLog(ifr, "Inside Nth list");
			nthEligiblity = list.get(0).get(0);

		}
		eligibleAsPerNth = (int) Math.floor(Integer.parseInt(nthEligiblity));
		Log.consoleLog(ifr, "eligibleAsPerNth" + eligibleAsPerNth);
		ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
		Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
		Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
		if (eligibleAsPerNth <= 0.0) {
			json.put("Error", "error, Eligiblity as per nth salary is less than zero");
			return json;
		}
		if (purpose.toLowerCase().contains("four")) {
			Log.consoleLog(ifr, "inside if fourWheelerAvailable==" + fourWheelerAvailable);
			availAmt = fourWheelerAvailable;
		} else {
			Log.consoleLog(ifr, "inside else twoWheelerAvailable==" + twoWheelerAvailable);
			availAmt = twoWheelerAvailable;
		}
		Log.consoleLog(ifr, "availAmt==" + availAmt);
		Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
		Log.consoleLog(ifr, "maxLimitOnSlider ==" + maxLimitOnSlider);
		double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
		Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
		double finalEligiblity = eligibleLoanAmt;
		long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
		Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
		Log.consoleLog(ifr, "roundedVal===" + roundedVal);
		String finalRoundedVal = String.valueOf(roundedVal);
		ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));
		ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
		// String vT = ifr.getValue("VALUESETVL").toString();
		ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			String PrincipalPayments = ammortizationEmiQueryRes.get(0).get(1);
			ifr.setValue("Principal_Repay_Tenure_VL", PrincipalPayments);
			if (PrincipalPayments.isEmpty() || PrincipalPayments == null) {
				json.put("Error", "error,EMIAmount is Empty");
				return json;
			}
			String InterestPayments = ammortizationEmiQueryRes.get(0).get(2);
			ifr.setValue("Interest_Repay_Tenure_VL", InterestPayments);
			if (InterestPayments.isEmpty() || InterestPayments == null) {
				json.put("Error", "error,EMIAmount is Empty");
				return json;
			}

		}

//			if (amtRequest > roundedVal) {
//				json.put("Error", "error, Requested loan amount is more than" + eligibleLoanAmt);
//				return json;
//			}
		if (roundedVal <= 0) {
			json.put("Error", "error, In-Principle ELigible Loan is less than zero");
			return json;
		}
		Log.consoleLog(ifr, "finalEligiblity==" + finalEligiblity);

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + roundedVal + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		return json;
	}

	private JSONObject calculateNthAndInpriciplePortalHL(IFormReference ifr, double amtRequest, String value,
			JSONObject json, double maxLimitOnSlider, String scheduleCode, String processInstanceId, String purpose,
			String defaultProductCode, String prob, Ammortization ammortization, String residualAgeInMonths,
			String subProductCode) {
		double availAmt;
		int eligibleAsPerNth;
		double eligibleAsPNth;
		String nthEligiblity = "";
		String ammortizationEmiQuery = "";

		String queryForNth = "Select ELG_PER_NTH from SLOS_STAFF_VL_ELIGIBILITY WHERE WINAME = '" + processInstanceId
				+ "'";

		List<List<String>> list = ifr.getDataFromDB(queryForNth);
		Log.consoleLog(ifr, "query for Nth" + queryForNth);
		Log.consoleLog(ifr, "query for Nth list" + list);
		if (!list.isEmpty()) {
			Log.consoleLog(ifr, "Inside Nth list");
			nthEligiblity = list.get(0).get(0);

		}
		eligibleAsPerNth = (int) Math.floor(Integer.parseInt(nthEligiblity));
		Log.consoleLog(ifr, "eligibleAsPerNth" + eligibleAsPerNth);
		ifr.setValue("Elg_Per_NTH", String.valueOf(eligibleAsPerNth));
		Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
		Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
		if (eligibleAsPerNth <= 0.0) {
			json.put("Error", "error, Eligiblity as per nth salary is less than zero");
			return json;
		}
		// Log.consoleLog(ifr, "availAmt==" + availAmt);
		Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
		Log.consoleLog(ifr, "maxLimitOnSlider ==" + maxLimitOnSlider);
		//double finalEligiblity = maxLimitOnSlider;
		//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
		double finalEligiblity = maxLimitOnSlider;
		long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
		Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
		Log.consoleLog(ifr, "roundedVal===" + roundedVal);
		String finalRoundedVal = String.valueOf(roundedVal);
		ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));
//		ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
//		ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
//				+ processInstanceId
//				+ "' and stagenumber='1' and (STAGENAME LIKE '%FPI%' OR STAGENAME LIKE '%PRINCIPAL%')";
//		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
//		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
//		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);
//
//		if (!ammortizationEmiQueryRes.isEmpty()) {
//			String PrincipalPayments = ammortizationEmiQueryRes.get(0).get(1);
//			ifr.setValue("Principal_Repay_Tenure_VL", PrincipalPayments);
//			if (PrincipalPayments.isEmpty() || PrincipalPayments == null) {
//				json.put("Error", "error,EMIAmount is Empty");
//				return json;
//			}
//			String InterestPayments = ammortizationEmiQueryRes.get(0).get(2);
//			ifr.setValue("Interest_Repay_Tenure_VL", InterestPayments);
//			if (InterestPayments.isEmpty() || InterestPayments == null) {
//				json.put("Error", "error,EMIAmount is Empty");
//				return json;
//			}
//
//		}

		if (roundedVal <= 0) {
			json.put("Error", "error, In-Principle ELigible Loan is less than zero");
			return json;
		}
		Log.consoleLog(ifr, "finalEligiblity==" + finalEligiblity);

		calCulateROI(ifr, subProductCode, scheduleCode, roundedVal, "360");

		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + roundedVal + "' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);

		return json;
	}

	private void calCulateROI(IFormReference ifr, String productCode, String scheduleCode, long roundedVal,
			String tenure) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String loanAmount = String.valueOf(roundedVal);

		String rateCodeQuery = "SELECT DISTINCT\r\n" + "    a.cod_rate_chart,\r\n"
				+ "    (a.cod_rate_chart || '--' || a.nam_rate_chart) AS cbs_ratechartcode,\r\n" + "    a.cod_prod,\r\n"
				+ "    a.cod_sched_type,\r\n" + "    a.COD_RATE_REGIME\r\n" + "FROM LN_PROD_RATE_CHART_XREF a,\r\n"
				+ "     LN_SCHED_TYPES b\r\n" + "WHERE a.COD_PROD = b.COD_PROD\r\n"
				+ "  AND a.COD_SCHED_TYPE = b.COD_SCHED_TYPE\r\n" + "  AND a.COD_PROD = '" + productCode + "'\r\n"
				+ "  AND a.FLG_DELETE = 'N'\r\n" + "  AND a.FLG_MNT_STATUS = 'A'\r\n"
				+ "  AND b.dat_sched_exp >= SYSDATE\r\n" + "  AND a.COD_SCHED_TYPE = '" + scheduleCode + "'\r\n"
				+ "  AND a.COD_RATE_REGIME = 'L'";
		List<List<String>> listrateCodeQuery = ifr.getDataFromDB(rateCodeQuery);
		if (!listrateCodeQuery.isEmpty()) {
			String rateCode = listrateCodeQuery.get(0).get(0);
			String rateCodewithName = listrateCodeQuery.get(0).get(1);
			ifr.setValue("RateChartCode", rateCodewithName);

			String productLevelVarianceQuery = "SELECT cod_rate_chart, ctr_from_term_slab, cod_int_index,rat_var_slab\r\n"
					+ "                FROM pr_rate_chart_detl c\r\n"
					+ "               WHERE c.flg_mnt_status = 'A'\r\n" + "                 AND c.cod_rate_chart =  '"
					+ rateCode + "'\r\n" + "                 and c.cod_rat_type = DECODE(NVL(0,0),0,1,0)\r\n"
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
						+ "WHERE COD_INT_INDX = '" + indexVariance + "'\r\n" + "ORDER BY DAT_EFF_SPREAD_INDX DESC\r\n"
						+ "FETCH FIRST ROW ONLY";
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
				ifr.setValue("ROI_VL", String.valueOf(eROI));

			}

		}

	}

	public String placeHolderRecommendedMonthlyInstallement(IFormReference ifr, String val) {
		Log.consoleLog(ifr, "val==" + val);
		String[] split = val.split(",");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String recLoanAmt = ifr.getValue(split[0]).toString();
		Log.consoleLog(ifr, "Recommensed Loan Amt==" + recLoanAmt);
		String recLoanTen = ifr.getValue(split[1]).toString();
		Log.consoleLog(ifr, "Recommensed Loan tenure====" + recLoanTen);
		// String monthlyInstallement=
		if (recLoanTen.trim().isEmpty()) {
			return "Recommended loan tenure cannot be empty";
		}
		if (recLoanAmt.trim().isEmpty()) {
			return "Recommended loan amount cannot be empty";
		}
//		String finalEligibilityquery = "select final_elg_vl from slos_staff_vl_eligibility where winame='"
//				+ processInstanceId + "'";
//		List<List<String>> finalEligbilityRes = ifr.getDataFromDB(finalEligibilityquery);
//		Log.consoleLog(ifr, "finalEligibilityquery====" + finalEligibilityquery);
//		if (finalEligbilityRes.isEmpty()) {
//			return "error, technical glitch";
//		}
//		double finalElig = Double.parseDouble(finalEligbilityRes.get(0).get(0));
//		Log.consoleLog(ifr, "finalElig====" + finalElig);
//		double recLoanAmtDoub = Double.parseDouble(recLoanAmt);
//		Log.consoleLog(ifr, "recLoanAmtDoub====" + recLoanAmtDoub);
//		if (recLoanAmtDoub > finalElig) {
//			return "Error, Recommended amount cannot be higher than final eligibility";
//		}
		int recLoanTenureDouble = Integer.parseInt(recLoanTen);
		String staffTrn = "select gross_salary,comp_years_of_service,pension_income,"
				+ "loan_deduction,statutory_deductions, " + "designation,probation,purpose_loan_vl,fuel_type,"
				+ "four_w_vl_loan_available,two_w_vl_loan_available,EXT_LOAN_DED,net_salary,VEHICLE_CATEGORY"
				+ " from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrnRes====" + staffTrnRes);
		if (staffTrnRes.isEmpty()) {
			return "error, technical glitch";
		}
		String designation = staffTrnRes.get(0).get(5).toString();
		Log.consoleLog(ifr, "designation====" + designation);
		String probation = staffTrnRes.get(0).get(6).toLowerCase().contains("y") ? "Y" : "N";
		Log.consoleLog(ifr, "probation====" + probation);
		String purpose = staffTrnRes.get(0).get(7).toString();
		Log.consoleLog(ifr, "purpose====" + purpose);
		String fuelType = staffTrnRes.get(0).get(8).toString();
		Log.consoleLog(ifr, "fuelType====" + fuelType);
		String fourWAvailable = staffTrnRes.get(0).get(9);
		Log.consoleLog(ifr, "fourWAvailable====" + fourWAvailable);
		String grossSalary = staffTrnRes.get(0).get(0);
		Log.consoleLog(ifr, "grossSalary====" + grossSalary);
		String netSalary = staffTrnRes.get(0).get(12);
		Log.consoleLog(ifr, "netSalary====" + netSalary);
		String vehicleCateogir = staffTrnRes.get(0).get(13);
		Log.consoleLog(ifr, "vehicleCateogir====" + vehicleCateogir);
		Log.consoleLog(ifr, "grossSalary==" + grossSalary);
		Log.consoleLog(ifr, "vehicleCateogir==" + vehicleCateogir);
		Log.consoleLog(ifr, "netSalary==" + netSalary);
		Log.consoleLog(ifr, "designation==" + designation);
		Log.consoleLog(ifr, "probation==" + probation);
		Log.consoleLog(ifr, "purpose==" + purpose);
		Log.consoleLog(ifr, "fuelType==" + fuelType);
		Log.consoleLog(ifr, "fourWAvailable==" + fourWAvailable);
		String schemeLimitQuery = "SELECT prd_rng_to, category,SUB_PRODUCT_CODE_CBS,"
				+ "SUB_PRODUCT,AMMORTIZATION_PORTAL_RATE_OF_INTEREST,roi_type,prd_rng_from,AMMORTIZATION_PORTAL_PRODUCT_CODE,AMMORTIZATION_PORTAL_SCHEDULE_CODE "
				+ "from staff_vl_prd_designation_matrix " + "where designation='" + designation
				+ "' and lower(loan_purpose) like lower('%" + purpose + "%') and lower(loan_purpose) like lower('%"
				+ vehicleCateogir + "%') and fuel_type='" + fuelType + "' and PROBATION_TAG='" + probation + "'";
		List<List<String>> schemeLimitQueryRes = ifr.getDataFromDB(schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQuery==" + schemeLimitQuery);
		if (schemeLimitQueryRes.isEmpty()) {
			return "error, technical glitch";
		}
		String productCode = schemeLimitQueryRes.get(0).get(2);
		Log.consoleLog(ifr, "productCode==" + productCode);
		String subProductCode = schemeLimitQueryRes.get(0).get(3);
		String defaultScheduleCode = "";
		String defaultProductCode = "";
		String queryForScheduleType = "SELECT SCHEDULE_CODE,PRODUCT_CODE_VL FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForScheduleType query===>" + queryForScheduleType);
		List<List<String>> res = ifr.getDataFromDB(queryForScheduleType);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			defaultScheduleCode = res.get(0).get(0);
			defaultProductCode = res.get(0).get(1);
		}
		Log.consoleLog(ifr, "defaultProductCode==" + defaultProductCode);
		Log.consoleLog(ifr, "defaultScheduleCode==" + defaultScheduleCode);

		Log.consoleLog(ifr, "subProductCode===" + subProductCode);
		String productSheetQuery = "SELECT PRD_MINTERM,PRD_TERM,Principal_tenure_factor,Interest_tenure_factor from staff_vl_product_sheet where prd_desc='"
				+ subProductCode + "'";
		List<List<String>> prdSheetRes = ifr.getDataFromDB(productSheetQuery);
		if (prdSheetRes.isEmpty()) {
			return "Error, Technical glitch";
			// return json;
		}
		double minTenure = Double.parseDouble(prdSheetRes.get(0).get(0));
		double maxTenure = Double.parseDouble(prdSheetRes.get(0).get(1));
		if (!(recLoanTenureDouble >= minTenure && recLoanTenureDouble <= maxTenure)) {
			return "error, Eligbile tenure from = " + minTenure + " to = " + maxTenure;
			// return json;
		}
		Ammortization ammortization = new Ammortization();
		// (IFormReference ifr, String ProcessInstanceId,
		// String loanAmount, String loanterm, String productCode)
		String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId, recLoanAmt,
				recLoanTen, defaultProductCode, defaultScheduleCode);
		Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
		String[] ammortizationResp = ammortizationResponse.split(":");

		if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
			Log.consoleLog(ifr, "Ammortization inside===================>");
			if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
				return "error, Ammortization error, No Response from the server";
			} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
				return "error," + ammortizationResp[1];
			}
		}
		String ammortizationQuery = "SELECT PRINCIPAL,INTEREST from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and installmentno='1'";
		List<List<String>> ammortizationRes = ifr.getDataFromDB(ammortizationQuery);
		Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationRes);
		if (ammortizationRes.isEmpty()) {
			return "error,Technical glitch";
			// return json;
		}
		Log.consoleLog(ifr, "InterestPayments==" + ammortizationRes.get(0).get(0));
		Log.consoleLog(ifr, "EMIAmount===>" + ammortizationRes.get(0).get(0));
//		ifr.setValue(split[2], ammortizationRes.get(0).get(0));
		Validator valid = new KnockOffValidator("");
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("KEY", grossSalary);
		grossSalary = valid.getValue(ifr, jsonObj, "KEY", "0.0");
		jsonObj.put("KEY", netSalary);
		netSalary = valid.getValue(ifr, jsonObj, "KEY", "0.0");
		double netSalaryAfterMonthlyInstall = Double.parseDouble(netSalary)
				- Double.parseDouble(ammortizationRes.get(0).get(0));
		ifr.setValue(split[3], String.valueOf(netSalaryAfterMonthlyInstall));
		double actualNTHPercentage = (netSalaryAfterMonthlyInstall / Double.parseDouble(grossSalary)) * 100;
		ifr.setValue(split[4], String.valueOf(actualNTHPercentage));
		if (actualNTHPercentage < 30 && probation.equalsIgnoreCase("N")) {
			return "error,The requested loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the requested loan amount and calculate again.";
		}
		if (actualNTHPercentage < 40 && probation.equalsIgnoreCase("Y")) {
			return "error,The requested loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the requested loan amount and calculate again.";
		}
		return "SUCCESS";
	}

//	public static double calculatePMT(double annualRatePercentage, int totalMonths, double loanAmt) {
//		double monthlyRate = annualRatePercentage / 12.0;
//		if (monthlyRate == 0.0) {
//			return loanAmt / totalMonths;
//		}
//		return (monthlyRate * loanAmt) / (1 - Math.pow(1 + monthlyRate, -totalMonths));
//	}

	private double calculatePMT(double tenure, double roi) {
		double monthlyInterestRate = roi / 12;
		double numerator = monthlyInterestRate * 100000;
		double denominator = 1 - Math.pow(1 + monthlyInterestRate, -tenure);

		return numerator / denominator;
	}

	public static String purposeLoan(IFormReference ifr) {
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String res = "";
		String query = " select vehicle_category " + " from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> resList = ifr.getDataFromDB(query);
		if (resList.isEmpty()) {
			return "error,Technical glitch";
		}
		if (resList.get(0).get(0).toLowerCase().contains("used")) {
			return "USED";
		}
		return "NEW";
	}

	JSONObject eligibiltyAsSalaryNthRO(IFormReference ifr, double amtRequest, String value) {
		JSONObject json = new JSONObject();
		double maxLimitOnSlider = 0;
		int principalTenureFactor = 0;
		int interesetTenureFactor = 0;
		double rcValidUpToMonth = 0;
		String scheduleCode = "";
		Log.consoleLog(ifr, "inside eligibiltyAsSalaryNthRO");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryBranchCodeAndVehicleCat = "select VEHICLE_CATEGORY from SLOS_STAFF_TRN " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);

		String maxLimitOnSliderQuery = "SELECT MAX_LOAN_AMT_PER_LTV, TOTAL_COST, DOWNPAYMENT_AMOUNT_EXP, TOTAL_COST_USED,DOWNPAYMENT_AMOUNT_USED FROM slos_staff_collateral where winame='"
				+ processInstanceId + "'";
		List<List<String>> maxLimit = ifr.getDataFromDB(maxLimitOnSliderQuery);
		Log.consoleLog(ifr, "maxLimitOnSliderQuery===" + maxLimitOnSliderQuery);
		if (maxLimit.isEmpty()) {
			json.put("Error", "error, Max amount not present");
			return json;
		}
		if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("USED")) {
			// maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
			Log.consoleLog(ifr, "inside Elg_Per_LTV_RO_USED");
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(3).toString())
//					- Double.parseDouble(maxLimit.get(0).get(4).toString());
			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
			ifr.setValue("Elg_Per_LTV_RO", String.valueOf(maxLimitOnSlider));
		}
		if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("NEW")) {
			Log.consoleLog(ifr, "inside Elg_Per_LTV_RO_NEW");
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(1).toString())
//					- Double.parseDouble(maxLimit.get(0).get(2).toString());
			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
			ifr.setValue("Elg_Per_LTV_RO", String.valueOf(maxLimitOnSlider));
		}
//		json.put("Error", "");
//		json.put("Success", "");
		double availAmt = 0.0;
		String productCode = "";
		String designation = "";
		String probation = "";
		String purpose = "";
		String fuelType = "";
		double roi = 0.0;
		double fourWheelerAvailable = 0.0;
		double twoWheelerAvailable = 0.0;
		String subProductCode = "";
		String emiType = "";
		String minimumNTHPer = "";
		double minNTHPercentage = 0.0;
		String defaultProductCode = "";
		String val = "";
		int reqTenure = 0;
		double otherDeducation = 0.0;
		String staffTrn = "select gross_salary,comp_years_of_service,pension_income,"
				+ "loan_deduction,statutory_deductions, " + "designation,probation,purpose_loan_vl,fuel_type,"
				+ "four_w_vl_loan_available,two_w_vl_loan_available,EXT_LOAN_DED,vehicle_category,TOTAL_LOAN_DED, "
				+ "  " + "TRUNC(MONTHS_BETWEEN( " + "    CASE "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}-\\d{2}-\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MM-YYYY') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{4}-\\d{2}-\\d{2}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'YYYY-MM-DD') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}/[A-Z]{3}/\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MON-YYYY', 'NLS_DATE_LANGUAGE=ENGLISH') "
				+ "        ELSE NULL " + "    END, " + "    SYSDATE " + ")) AS MONTHS_LEFT "
				+ " from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrnRO==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnResRO==" + staffTrnRes);
		if (staffTrnRes.isEmpty()) {
			json.put("Error", "error, technical glitch");
			return json;
		}
		String residualAgeInMonths = "";
		// String resAgeQuery = "select RES_VEHICLE_AGE_USED from slos_staff_collateral
		// where WINAME='" + processInstanceId
		// + "'";
		String resAgeQuery = "select RES_VEHICLE_AGE_USED,NVL(TRUNC(MONTHS_BETWEEN(RC_VAL_UPTO_USED, SYSDATE)), 0) as rcVal from slos_staff_collateral where WINAME='"
				+ processInstanceId + "'";
		List<List<String>> resAgeQueryRes = ifr.getDataFromDB(resAgeQuery);
		Log.consoleLog(ifr, "resAgeQueryRO==" + resAgeQuery);
		Log.consoleLog(ifr, "resAgeQueryResRO==" + resAgeQueryRes);
		if (!resAgeQueryRes.isEmpty()) {
			residualAgeInMonths = resAgeQueryRes.get(0).get(0).toString();
			rcValidUpToMonth = Double.parseDouble(resAgeQueryRes.get(0).get(1).toString());
			Log.consoleLog(ifr, "residualAgeInMonthsRO==" + residualAgeInMonths);
		}
		double grossSalary = Double.parseDouble(staffTrnRes.get(0).get(0).toString());
		double years = Double.parseDouble(staffTrnRes.get(0).get(1).toString());
		String peinsionIn = staffTrnRes.get(0).get(2).toString();
		double pensionIncome = Double.parseDouble(peinsionIn.isEmpty() ? "0.0" : peinsionIn);
		double loanDeductions = Double.parseDouble(staffTrnRes.get(0).get(3).toString());
		double statutoryDeductions = Double.parseDouble(staffTrnRes.get(0).get(4).toString());
		double minNthVal = 0.0;
		double dateOfRetirementInMonths = Double.parseDouble(staffTrnRes.get(0).get(14).toString());
		Log.consoleLog(ifr, "grossSalaryRO==" + grossSalary);
		Log.consoleLog(ifr, "yearsRO==" + years);
		Log.consoleLog(ifr, "pensionIncomeRO==" + pensionIncome);
		Log.consoleLog(ifr, "loanDeductionsRO==" + loanDeductions);
		Log.consoleLog(ifr, "statutoryDeductionsRO==" + statutoryDeductions);

		designation = staffTrnRes.get(0).get(5).toString();
		probation = staffTrnRes.get(0).get(6).toLowerCase().contains("y") ? "Y" : "N";
		purpose = staffTrnRes.get(0).get(7).toString();
		fuelType = staffTrnRes.get(0).get(8).toString();
		Validator valid = new KnockOffValidator("");
		JSONObject jsonObj = new JSONObject();

		String fourWAvailable = staffTrnRes.get(0).get(9);
		jsonObj.put("Key", fourWAvailable);
		fourWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		;
		fourWheelerAvailable = Double.parseDouble(fourWAvailable);
		Log.consoleLog(ifr, "fourWheelerAvailableRO==" + fourWheelerAvailable);
		String twoWAvailable = staffTrnRes.get(0).get(10);
		jsonObj.put("Key", twoWAvailable);
		twoWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "twoWAvailablROe==" + twoWAvailable);
		twoWheelerAvailable = Double.parseDouble(twoWAvailable);

		// change
		if (purpose.trim().toLowerCase().equals("two")) {
			ifr.setValue("Elg_Per_Scale_Scheme_RO", twoWAvailable);
		} else if (purpose.trim().toLowerCase().equals("four")) {
			ifr.setValue("Elg_Per_Scale_Scheme_RO", fourWAvailable);
		}
		// twoWheelerAvailable= valid.getValue(ifr, jsonObj,"Key", "0.0");
		Log.consoleLog(ifr, "twoWheelerAvailableRO==" + twoWheelerAvailable);
		String otherDed = staffTrnRes.get(0).get(11).toString();
		otherDeducation = Double.parseDouble(otherDed.isEmpty() ? "0.0" : otherDed);
		String vehicleCotegory = staffTrnRes.get(0).get(12).toString();

		String totalLoanDed = staffTrnRes.get(0).get(13).toString();
		jsonObj.put("Key", totalLoanDed);
		totalLoanDed = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "totalLoanDedRO==" + totalLoanDed);

		Log.consoleLog(ifr, "designationRO==" + designation);
		Log.consoleLog(ifr, "probationRO==" + probation);
		Log.consoleLog(ifr, "purposeRO==" + purpose);
		Log.consoleLog(ifr, "fuelTypeRO==" + fuelType);
		Log.consoleLog(ifr, "fourWAvailableRO==" + fourWAvailable);
		Log.consoleLog(ifr, "twoWAvailableRO==" + twoWAvailable);
		Log.consoleLog(ifr, "otherDedRO==" + otherDeducation);
		Log.consoleLog(ifr, "vehicleCotegoryRO==" + vehicleCotegory);
		String schemeLimitQuery = "SELECT prd_rng_to, category,SUB_PRODUCT_CODE_CBS,"
				+ "SUB_PRODUCT,AMMORTIZATION_PORTAL_RATE_OF_INTEREST,roi_type,prd_rng_from,AMMORTIZATION_PORTAL_SCHEDULE_CODE,MINIMUM_NTH_PERCENT,AMMORTIZATION_PORTAL_PRODUCT_CODE "
				+ "from staff_vl_prd_designation_matrix " + "where designation='" + designation
				+ "' and lower(loan_purpose) like lower('%" + purpose + "%') and lower(loan_purpose) like lower('%"
				+ vehicleCotegory + "%') and fuel_type='" + fuelType + "' and PROBATION_TAG='" + probation + "'";
		List<List<String>> schemeLimitQueryRes = ifr.getDataFromDB(schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQueryRO==" + schemeLimitQuery);
		if (!schemeLimitQueryRes.isEmpty()) {
			Log.consoleLog(ifr, "productCodeRO" + productCode);
			productCode = schemeLimitQueryRes.get(0).get(2);
			Log.consoleLog(ifr, "productCodeRO==" + productCode);
			roi = Double.parseDouble(schemeLimitQueryRes.get(0).get(4));
			Log.consoleLog(ifr, "roiRO==" + roi);
			subProductCode = schemeLimitQueryRes.get(0).get(3);
			Log.consoleLog(ifr, "subProductCodeRO===" + subProductCode);
			emiType = schemeLimitQueryRes.get(0).get(5);
			Log.consoleLog(ifr, "emiTypeRO===" + emiType);

			scheduleCode = schemeLimitQueryRes.get(0).get(7);
			Log.consoleLog(ifr, "scheduleCodeRO===" + scheduleCode);
			minNTHPercentage = Double.parseDouble(schemeLimitQueryRes.get(0).get(8).trim());
			Log.consoleLog(ifr, "minNTHPercentageRO===" + minNTHPercentage);
			String stage = ifr.getValue("PresentStage").toString();
			if (ifr.getValue("App_Loan_Amt_VL").toString().trim().isEmpty()) {
				amtRequest = Math.min(amtRequest, Double.parseDouble(schemeLimitQueryRes.get(0).get(0)));
			}
			if (stage.equalsIgnoreCase("PORTAL")) {
				Log.consoleLog(ifr, "Inside PortalRO===");
				defaultProductCode = schemeLimitQueryRes.get(0).get(9);
			} else {

				String queryForScheduleType = "SELECT SCHEDULE_CODE,PRODUCT_CODE_VL FROM SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "queryForScheduleType query===>" + queryForScheduleType);
				List<List<String>> res = ifr.getDataFromDB(queryForScheduleType);
				Log.consoleLog(ifr, "res===>" + res);
				if (!res.isEmpty()) {
					scheduleCode = res.get(0).get(0);
					defaultProductCode = res.get(0).get(1);
				}
			}
			Log.consoleLog(ifr, "defaultProductCodeRO===" + defaultProductCode);
			Log.consoleLog(ifr, "scheduleCodeRO===" + scheduleCode);
		}
		if (!(amtRequest >= Double.parseDouble(schemeLimitQueryRes.get(0).get(6))
				&& amtRequest <= Double.parseDouble(schemeLimitQueryRes.get(0).get(0))

		)) {
			Log.consoleLog(ifr, "amtRequest if condition failed");
			json.put("Error", "error, please select amount between = " + schemeLimitQueryRes.get(0).get(6) + " to = "
					+ schemeLimitQueryRes.get(0).get(0));
			return json;
		}
		Log.consoleLog(ifr, "roiRO==" + roi);
		ifr.setValue("ROI_VL", String.valueOf(roi * 100));
		Log.consoleLog(ifr, "productCodeRO==" + productCode);
		Log.consoleLog(ifr, "roiRO==" + roi);
		Log.consoleLog(ifr, "subProductCodeRO==" + subProductCode);
		Log.consoleLog(ifr, "emiTypeRO==" + emiType);
		if (productCode.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		String productSheetQuery = "SELECT PRD_MINTERM,PRD_TERM,Principal_tenure_factor,Interest_tenure_factor from staff_vl_product_sheet where prd_desc='"
				+ subProductCode + "'";
		List<List<String>> prdSheetRes = ifr.getDataFromDB(productSheetQuery);
		if (prdSheetRes.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		double minTenure = Double.parseDouble(prdSheetRes.get(0).get(0));
		// if()

		double maxTenure = Double.parseDouble(prdSheetRes.get(0).get(1));

		if (vehicleCotegory.equalsIgnoreCase("used")) {
			// maxTenure = Math.min(maxTenure, Double.parseDouble(residualAgeInMonths));
//			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),
//					Math.min(dateOfRetirementInMonths, rcValidUpToMonth)));
			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),rcValidUpToMonth));
					
		}
		principalTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(2));
		interesetTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(3));
		Log.consoleLog(ifr, "minTenureRO==" + minTenure);
		Log.consoleLog(ifr, "maxTenureRO==" + maxTenure);
		val = ifr.getValue("App_Tenure_VL").toString().isEmpty() ? String.valueOf((int) maxTenure)
				: String.valueOf((int) Double.parseDouble(String.valueOf(ifr.getValue("App_Tenure_VL"))));
		reqTenure = Integer.parseInt(val);
		Log.consoleLog(ifr, "reqTenure====" + reqTenure);
		minNthVal = (minNTHPercentage * (pensionIncome + grossSalary));
		Log.consoleLog(ifr, "minNthValRO====" + minNthVal);

		// if(productCode.trim().equals("700")){
		// minNthVal=(AccelatorStaffConstant.NTH_VALUE_700*(pensionIncome+grossSalary));
		// }
		Log.consoleLog(ifr, "minNthValRO==" + minNthVal);
		// ifr.setValue("Elg_Per_NTH", String.valueOf(minNthVal));
		String pension = "";
		String totalGrossSal = "";
		double currentNthSalary = 0.0;
		double totalGrossSa = 0.0;
		double totalnetsalary = 0.0;
		String queryForPensionType = "SELECT PENSIONER,PENSION_INCOME FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForPensionType query===>" + queryForPensionType);
		List<List<String>> res = ifr.getDataFromDB(queryForPensionType);
		Log.consoleLog(ifr, "resRO===>" + res);
		if (!res.isEmpty()) {
			pension = res.get(0).get(0);
		}
		if (res.size() > 0 && pension.equalsIgnoreCase("YES")) {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary + pensionIncome - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			totalGrossSa = grossSalary + pensionIncome;
			totalnetsalary = netsalary + pensionIncome;
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + totalGrossSa
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(totalnetsalary) + "' WHERE WINAME= '"
					+ processInstanceId + "'";
			ifr.saveDataInDB(QueryNetSal);

		} else {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + grossSalary
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(netsalary) + "' WHERE WINAME= '" + processInstanceId
					+ "'";
			ifr.saveDataInDB(QueryNetSal);
		}
		if (!(reqTenure >= minTenure && reqTenure <= maxTenure)) {
			json.put("Error", "error, Eligbile tenure from = " + minTenure + " to = " + maxTenure);
			return json;
		} else {
			ifr.setValue("Max_Tenure_VL", String.valueOf(maxTenure));
		}

		int eligibleAsPerNth = 0;
		double eligibleAsPNth = 0.0;
		String EMIAmount = "";
		String prob = "";

		Log.consoleLog(ifr, "eligibleAsPerNthRO if ");

		String probationQuery = "SELECT probation from slos_staff_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (!queryRes.isEmpty()) {
			prob = queryRes.get(0).get(0);
		} else {
			json.put("Error", "error,probation field is empty");
			return json;
		}

		if (value.equalsIgnoreCase("onload")) {
			ifr.setValue("ROI_Type_VL", emiType);
			Ammortization ammortization = new Ammortization();

			String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
					ifr.getValue("App_Loan_Amt_VL").toString(), ifr.getValue("App_Tenure_VL").toString(),
					defaultProductCode, scheduleCode);
			Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
			String[] ammortizationResp = ammortizationResponse.split(":");

			if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
				Log.consoleLog(ifr, "Ammortization inside===================>");
				if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
					json.put("Error", "error, Ammortization error, No Response from the server");
					return json;
				} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
					json.put("Error", ammortizationResp[1]);
					return json;
				}
			}
			if (prob.equalsIgnoreCase("No")) {
				jsonObj = getPaymentsFromAmmortizationProbN(ifr, json, processInstanceId, grossSalary, pensionIncome,
						totalLoanDed, minNthVal, currentNthSalary, ifr.getValue("App_Loan_Amt_VL").toString(),
						maxLimitOnSlider, purpose, twoWAvailable, fourWAvailable);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			}

			if (prob.equalsIgnoreCase("Yes")) {
				jsonObj = jsonObj = getPaymentsFromAmmortizationProbY(ifr, json, processInstanceId, grossSalary,
						pensionIncome, totalLoanDed, minNthVal, currentNthSalary,
						ifr.getValue("App_Loan_Amt_VL").toString(), maxLimitOnSlider, purpose, twoWAvailable,
						fourWAvailable);

				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}
			}

		}

		String apploanAMt = "";
		String emiM = "";
		String getApprovedLoanAmout = "select APP_LOAN_AMT_VL,MI_ROC_VL from SLOS_STAFF_TRN where winame='"
				+ processInstanceId + "'";

		List<List<String>> getApprovedLoanAmoutResult = ifr.getDataFromDB(getApprovedLoanAmout);
		if (!getApprovedLoanAmoutResult.isEmpty()) {
			apploanAMt = getApprovedLoanAmoutResult.get(0).get(0);
			emiM = getApprovedLoanAmoutResult.get(0).get(1);
		}
		String amtInWords = LoanAmtInWords.amtInWords(Double.parseDouble(apploanAMt));
		String emiInWords = LoanAmtInWords.amtInWords(Double.parseDouble(emiM));
		String QueryamtInWords = "UPDATE SLOS_STAFF_TRN SET LOAN_AMOUNT_IN_WORDS= '" + amtInWords + "' WHERE WINAME= '"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "QueryamtInWords : " + QueryamtInWords);
		ifr.saveDataInDB(QueryamtInWords);
		String QueryamtWords = "UPDATE SLOS_TRN_LOANSUMMARY SET EMI_IN_WORDS= '" + emiInWords + "' WHERE WINAME= '"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "QueryamtInWords : " + QueryamtInWords);
		ifr.saveDataInDB(QueryamtInWords);
		Log.consoleLog(ifr, "QueryamtWords : " + QueryamtWords);
		ifr.saveDataInDB(QueryamtWords);

		return jsonObj;
	}

	private JSONObject pMTFourWheeler(IFormReference ifr, JSONObject json, String scheduleCode,
			String processInstanceId, String defaultProductCode, Ammortization ammortization) {
		String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId, "100000",
				"180", defaultProductCode, scheduleCode);
		return getAmmortization(ifr, json, ammortizationResponse);

	}

	private JSONObject pMTTwoWheeler(IFormReference ifr, JSONObject json, String scheduleCode, String processInstanceId,
			String defaultProductCode, Ammortization ammortization) {
		String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId, "100000",
				"84", defaultProductCode, scheduleCode);
		return getAmmortization(ifr, json, ammortizationResponse);

	}

	private JSONObject calculatePMTProbY(IFormReference ifr, double amtRequest, String value, JSONObject json,
			double maxLimitOnSlider, String processInstanceId, String purpose, double fourWheelerAvailable,
			double twoWheelerAvailable, double minNthVal, double currentNthSalary) {
		double availAmt;
		int eligibleAsPerNth;
		double eligibleAsPNth;
		String EMIAmount;
		String ammortizationEmiQuery = "SELECT InstallmentAmount from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId
				+ "' and stagenumber='1' and (STAGENAME LIKE '%EMI%' OR STAGENAME = 'E M I') AND PRINCIPALAMOUNT='100000.00'";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);
		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,EMIAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);

			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* AccelatorStaffConstant.PMT_VALUE;
			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH_RO", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			if (purpose.toLowerCase().contains("four")) {
				Log.consoleLog(ifr, "inside if fourWheelerAvailable==" + fourWheelerAvailable);
				availAmt = fourWheelerAvailable;
			} else {
				Log.consoleLog(ifr, "inside else twoWheelerAvailable==" + twoWheelerAvailable);
				availAmt = twoWheelerAvailable;
			}
			Log.consoleLog(ifr, "availAmt==" + availAmt);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "maxLimitOnSlider ==" + maxLimitOnSlider);
			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			if (value.equalsIgnoreCase("onload"))
				ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Final_Elg_VL_RO", String.valueOf(roundedVal));
			if (amtRequest > roundedVal) {
				json.put("Error", "error, Requested loan amount is more than" + eligibleLoanAmt);
				return json;
			}
			if (roundedVal <= 0) {
				json.put("Error", "error, final eligiblity is less than zero");
				return json;
			}
			Log.consoleLog(ifr, "finalEligiblity==" + finalEligiblity);

			String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + roundedVal + "' WHERE WINAME='"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
			ifr.saveDataInDB(queryUpdate);

		}
		return json;
	}

	private JSONObject calculatePMTProbN(IFormReference ifr, double amtRequest, String value, JSONObject json,
			double maxLimitOnSlider, String processInstanceId, String purpose, double fourWheelerAvailable,
			double twoWheelerAvailable, double minNthVal, double currentNthSalary) {
		double availAmt;
		int eligibleAsPerNth;
		double eligibleAsPNth;
		String EMIAmount;
		String ammortizationEmiQuery = "SELECT InstallmentAmount from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId
				+ "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%') AND PRINCIPALAMOUNT='100000.00'";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,EMIAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* AccelatorStaffConstant.PMT_VALUE;
			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH_RO", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			if (purpose.toLowerCase().contains("four")) {
				Log.consoleLog(ifr, "inside if fourWheelerAvailable==" + fourWheelerAvailable);
				availAmt = fourWheelerAvailable;
			} else {
				Log.consoleLog(ifr, "inside else twoWheelerAvailable==" + twoWheelerAvailable);
				availAmt = twoWheelerAvailable;
			}
			Log.consoleLog(ifr, "availAmt==" + availAmt);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "maxLimitOnSlider ==" + maxLimitOnSlider);
			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			if (value.equalsIgnoreCase("onload"))
				ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Final_Elg_VL_RO", String.valueOf(roundedVal));
			if (amtRequest > roundedVal) {
				json.put("Error", "error, Requested loan amount is more than" + eligibleLoanAmt);
				return json;
			}
			if (roundedVal <= 0) {
				json.put("Error", "error, final eligiblity is less than zero");
				return json;
			}
			Log.consoleLog(ifr, "finalEligiblity==" + finalEligiblity);

			String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET LOANAMOUNT='" + roundedVal + "' WHERE WINAME='"
					+ processInstanceId + "'";

			Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
			ifr.saveDataInDB(queryUpdate);

		}
		return json;
	}

	private JSONObject getPaymentsFromAmmortizationProbN(IFormReference ifr, JSONObject json, String processInstanceId,
			double grossSalary, double pensionIncome, String totalLoanDed, double minNthVal, double currentNthSalary,
			String amount, double maxLimitOnSlider, String purpose, String twoWAvailable, String fourWAvailable) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;

		String inPrincipleEligibleLoan = ifr.getValue("Q_SLOS_STAFF_VEHICLE_ELIGIBILITY_IN_PRIN_ELG_LOAN").toString();
		String recLoanAmount = ifr.getValue("Rec_Loan_Amt_VL").toString();

		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
//			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
//			* AccelatorStaffConstant.PMT_VALUE;

			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* Double.parseDouble(amount);

			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH_RO", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
			Integer repay = Integer.parseInt(RepaymentAmount) + 1;
			Log.consoleLog(ifr, "repay===>" + repay);
			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
				json.put("Error", "error,IntrestAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

//				if (Double.parseDouble(EMIAmount) > eligibileEmi) {
//					json.put("Error", "error,Eligibily emi greater than the net salary after all deducation");
//					return json;
//				}

			double availAmt = 0.0;
			if (purpose.trim().toLowerCase().equals("two")) {
				availAmt = Double.parseDouble(twoWAvailable);
				ifr.setValue("Elg_Per_Scale_Scheme_RO", String.valueOf(availAmt));
			} else if (purpose.trim().toLowerCase().equals("four")) {
				availAmt = Double.parseDouble(fourWAvailable);
				ifr.setValue("Elg_Per_Scale_Scheme_RO", String.valueOf(availAmt));
			}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			// ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Elg_Per_LTV_RO", String.valueOf(maxLimitOnSlider));
			ifr.setValue("Final_Elg_VL_RO", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("Monthly_Instal_Maker_ROC", String.format("%.2f", emiAmount));
			ifr.setValue("Monthly_Instal_ROC", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL_RO", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL_RO===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL_RO", IntrestAmount);
			Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL_RO===>" + IntrestAmount);
			double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
			String ammortizationIntrestMonthly = "SELECT TotalInstallmentAmount from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='"
					+ processInstanceId + "' and InstallmentNo ='" + repay + "'";
			List<List<String>> resammortizationIntrestMonthly = ifr.getDataFromDB(ammortizationIntrestMonthly);
			Log.consoleLog(ifr, "ammortizationIntrestMonthly===>" + ammortizationIntrestMonthly);
			if (!resammortizationIntrestMonthly.isEmpty()) {
				String IntrestMonthlyInstallMent = resammortizationIntrestMonthly.get(0).get(0);
				if (IntrestMonthlyInstallMent.isEmpty() || IntrestMonthlyInstallMent == null) {
					json.put("Error", "error,IntrestMonthlyInstallMent is Empty");
					return json;
				}
				Double IntrestMonthlyInstall = Double.parseDouble(IntrestMonthlyInstallMent);
				ifr.setValue("Interest_Monthly_Inst_VL_RO", String.format("%.2f", IntrestMonthlyInstall));
				Log.consoleLog(ifr, "Interest_Monthly_Inst_VL_RO===>" + IntrestMonthlyInstall);
			}
			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			ifr.setValue("Sal_after_monthly_instal_ROC", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			ifr.setValue("NTH_Real_Percent_ROC", String.valueOf(actualNTHPercentage));
			if (actualNTHPercentage < 30) {
				json.put("Error",
						"eeror,The requested loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the requested loan amount and calculate again.");
				return json;
			}

		}
		return json;
	}

	private JSONObject getPaymentsFromAmmortizationProbY(IFormReference ifr, JSONObject json, String processInstanceId,
			double grossSalary, double pensionIncome, String totalLoanDed, double minNthVal, double currentNthSalary,
			String amount, double maxLimitOnSlider, String purpose, String twoWAvailable, String fourWAvailable) {
		String EMIAmount;
		double eligibleAsPNth = 0.0;
		int eligibleAsPerNth = 0;
		String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
				+ processInstanceId + "' and stagenumber='1' AND (STAGENAME LIKE '%EMI%' OR STAGENAME = 'E M I')";
		List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
		Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
		if (!ammortizationEmiQueryRes.isEmpty()) {
			EMIAmount = ammortizationEmiQueryRes.get(0).get(0);
			if (EMIAmount.isEmpty() || EMIAmount == null) {
				json.put("Error", "error,InstallmentAmount is Empty");
				return json;
			}
			Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
//			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
//			* AccelatorStaffConstant.PMT_VALUE;

			eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
					* Double.parseDouble(amount);

			eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
			ifr.setValue("Elg_Per_NTH_RO", String.valueOf(eligibleAsPerNth));
			Log.consoleLog(ifr, "eligibleAsPerNth if ==" + eligibleAsPerNth);
			Log.consoleLog(ifr, "eligibleAsPerNth==" + eligibleAsPerNth);
			if (eligibleAsPerNth <= 0.0) {
				json.put("Error", "error, Eligiblity as per nth salary is less than zero");
				return json;
			}
			String RepaymentAmount = ammortizationEmiQueryRes.get(0).get(1);
			if (RepaymentAmount.isEmpty() || RepaymentAmount == null) {
				json.put("Error", "error,PrincipalPayments is Empty");
				return json;
			}
			Log.consoleLog(ifr, "RepaymentAmount===>" + RepaymentAmount);
//			String IntrestAmount = ammortizationEmiQueryRes.get(0).get(2);
//			if (IntrestAmount.isEmpty() || IntrestAmount == null) {
//				json.put("Error", "error,IntrestAmount is Empty");
//				return json;
//			}
			// Log.consoleLog(ifr, "IntrestAmount===>" + IntrestAmount);
			Log.consoleLog(ifr, "grossSalary===>" + grossSalary);
			Log.consoleLog(ifr, "pensionIncome===>" + pensionIncome);
			Log.consoleLog(ifr, "totalLoanDed===>" + totalLoanDed);

			double availAmt = 0.0;
			if (purpose.trim().toLowerCase().equals("two")) {
				availAmt = Double.parseDouble(twoWAvailable);
				ifr.setValue("Elg_Per_Scale_Scheme_RO", String.valueOf(availAmt));
			} else if (purpose.trim().toLowerCase().equals("four")) {
				availAmt = Double.parseDouble(fourWAvailable);
				ifr.setValue("Elg_Per_Scale_Scheme_RO", String.valueOf(availAmt));
			}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider, availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			Log.consoleLog(ifr, "finalEligiblity===" + finalEligiblity);
			Log.consoleLog(ifr, "roundedVal===" + roundedVal);
			String finalRoundedVal = String.valueOf(roundedVal);
			// ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));
			ifr.setValue("Elg_Per_LTV_RO", String.valueOf(maxLimitOnSlider));
			ifr.setValue("Final_Elg_VL_RO", String.valueOf(roundedVal));

			double emiAmount = Double.parseDouble(EMIAmount);
			ifr.setValue("Monthly_Instal_Maker_ROC", String.format("%.2f", emiAmount));
			ifr.setValue("Monthly_Instal_ROC", String.format("%.2f", emiAmount));
			Log.consoleLog(ifr, "EMI_P_Monthly_Inst_VL===>" + emiAmount);
			ifr.setValue("Req_Prin_Repay_Tenure_VL_RO", RepaymentAmount);
			Log.consoleLog(ifr, "Req_Prin_Repay_Tenure_VL_RO===>" + RepaymentAmount);
			ifr.setValue("Req_Int_Repay_Tenure_VL_RO", "NA");
			// Log.consoleLog(ifr, "Req_Int_Repay_Tenure_VL_RO===>" + IntrestAmount);
			double netSalAfterAllDed = grossSalary + pensionIncome - Double.parseDouble(totalLoanDed);
			ifr.setValue("Interest_Monthly_Inst_VL_RO", "NA");
			double netSalaryAfterMonthlyInstall = netSalAfterAllDed - emiAmount;
			ifr.setValue("Sal_after_monthly_instal_ROC", String.valueOf(netSalaryAfterMonthlyInstall));
			double actualNTHPercentage = (netSalaryAfterMonthlyInstall / grossSalary) * 100;
			ifr.setValue("NTH_Real_Percent_ROC", String.valueOf(actualNTHPercentage));
			if (actualNTHPercentage < 40) {
				json.put("Error",
						"error,The requested loan amount is higher than the eligibility due to breach of minimum NTH. Please reduce the requested loan amount and calculate again.");
				return json;
			}

		}
		return json;
	}

	/**
	 * @param ifr
	 * @param value
	 * @return
	 */
	public JSONObject EligibilityAsPerRequestedAmtPortal(IFormReference ifr, String value) {
		JSONObject json = new JSONObject();
		double maxLimitOnSlider = 0.0;
		double rcValidUpToMonth = 0;
		int principalTenureFactor = 0;
		int interesetTenureFactor = 0;
		String scheduleCode = "";
		Log.consoleLog(ifr, "inside eligibiltyAsSalaryNth");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String queryBranchCodeAndVehicleCat = "select VEHICLE_CATEGORY from SLOS_STAFF_TRN " + "where WINAME ='"
				+ processInstanceId + "'";
		List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);

		String maxLimitOnSliderQuery = "SELECT MAX_LOAN_AMT_PER_LTV, TOTAL_COST, DOWNPAYMENT_AMOUNT_EXP, TOTAL_COST_USED,DOWNPAYMENT_AMOUNT_USED FROM slos_staff_collateral where winame='"
				+ processInstanceId + "'";
		List<List<String>> maxLimit = ifr.getDataFromDB(maxLimitOnSliderQuery);
		Log.consoleLog(ifr, "maxLimitOnSliderQuery===" + maxLimitOnSliderQuery);
		if (maxLimit.isEmpty()) {
			json.put("Error", "error, Max amount not present Technical glitch");
			return json;
		}
		
		//maxLimitOnSlider = ifr.getValue("Max_Loan_Amt_Per_LTV").toString();
			if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("USED")) {
//			// maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(3).toString())
//					- Double.parseDouble(maxLimit.get(0).get(4).toString());
		maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
//
     	}
		if (list.size() > 0 && list.get(0).get(0).equalsIgnoreCase("NEW")) {
//			maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(1).toString())
//					- Double.parseDouble(maxLimit.get(0).get(2).toString());
		maxLimitOnSlider = Double.parseDouble(maxLimit.get(0).get(0).toString());
//
		}

		double availAmt = 0.0;
		String productCode = "";
		String designation = "";
		String probation = "";
		String purpose = "";
		String fuelType = "";
		double roi = 0.0;
		double fourWheelerAvailable = 0.0;
		double twoWheelerAvailable = 0.0;
		String subProductCode = "";
		String emiType = "";
		String minimumNTHPer = "";
		double minNTHPercentage = 0.0;
		String defaultProductCode = "";
		String val = "";
		int reqTenure = 0;
		double otherDeducation = 0.0;
		String staffTrn = "select gross_salary,comp_years_of_service,pension_income,"
				+ "loan_deduction,statutory_deductions, " + "designation,probation,purpose_loan_vl,fuel_type,"
				+ "four_w_vl_loan_available,two_w_vl_loan_available,EXT_LOAN_DED,vehicle_category,TOTAL_LOAN_DED,PENSIONER"
				+ ",TRUNC(MONTHS_BETWEEN( " + "    CASE "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}-\\d{2}-\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MM-YYYY') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{4}-\\d{2}-\\d{2}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'YYYY-MM-DD') "
				+ "        WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}/[A-Z]{3}/\\d{4}$') "
				+ "             THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MON-YYYY', 'NLS_DATE_LANGUAGE=ENGLISH') "
				+ "        ELSE NULL " + "    END, " + "    SYSDATE " + ")) AS MONTHS_LEFT "

				+ " from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> staffTrnRes = ifr.getDataFromDB(staffTrn);
		Log.consoleLog(ifr, "staffTrn==" + staffTrn);
		Log.consoleLog(ifr, "staffTrnRes==" + staffTrnRes);
		if (staffTrnRes.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		String residualAgeInMonths = "";
		// String resAgeQuery = "select RES_VEHICLE_AGE_USED from slos_staff_collateral
		// where WINAME='" + processInstanceId
		// + "'";
		String resAgeQuery = "select RES_VEHICLE_AGE_USED,NVL(TRUNC(MONTHS_BETWEEN(RC_VAL_UPTO_USED, SYSDATE)), 0) as rcVal from slos_staff_collateral where WINAME='"
				+ processInstanceId + "'";
		List<List<String>> resAgeQueryRes = ifr.getDataFromDB(resAgeQuery);
		Log.consoleLog(ifr, "resAgeQuery==" + resAgeQuery);
		Log.consoleLog(ifr, "resAgeQueryRes==" + resAgeQueryRes);
		if (!resAgeQueryRes.isEmpty()) {
			residualAgeInMonths = resAgeQueryRes.get(0).get(0).toString();
			rcValidUpToMonth = Double.parseDouble(resAgeQueryRes.get(0).get(1).toString());
			Log.consoleLog(ifr, "residualAgeInMonths==" + residualAgeInMonths);
			Log.consoleLog(ifr, "residualAgeInMonths==" + residualAgeInMonths);
		}
		double grossSalary = Double.parseDouble(staffTrnRes.get(0).get(0).toString());
		double years = Double.parseDouble(staffTrnRes.get(0).get(1).toString());
		String peinsionIn = staffTrnRes.get(0).get(2).toString();
		String pensioner = staffTrnRes.get(0).get(14).toString();
		double pensionIncome = Double
				.parseDouble((peinsionIn.isEmpty() || pensioner.equalsIgnoreCase("NO")) ? "0.0" : peinsionIn);
		double loanDeductions = Double.parseDouble(staffTrnRes.get(0).get(3).toString());
		double statutoryDeductions = Double.parseDouble(staffTrnRes.get(0).get(4).toString());
		double dateOfRetirementInMonths = Double.parseDouble(staffTrnRes.get(0).get(15).toString());

		double minNthVal = 0.0;

		Log.consoleLog(ifr, "grossSalary==" + grossSalary);
		Log.consoleLog(ifr, "years==" + years);
		Log.consoleLog(ifr, "pensionIncome==" + pensionIncome);
		Log.consoleLog(ifr, "loanDeductions==" + loanDeductions);
		Log.consoleLog(ifr, "statutoryDeductions==" + statutoryDeductions);

		designation = staffTrnRes.get(0).get(5).toString();
		probation = staffTrnRes.get(0).get(6).toLowerCase().contains("y") ? "Y" : "N";
		purpose = staffTrnRes.get(0).get(7).toString();
		fuelType = staffTrnRes.get(0).get(8).toString();
		Validator valid = new KnockOffValidator("");
		JSONObject jsonObj = new JSONObject();

		String fourWAvailable = staffTrnRes.get(0).get(9);
		jsonObj.put("Key", fourWAvailable);
		fourWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		;
		fourWheelerAvailable = Double.parseDouble(fourWAvailable);
		Log.consoleLog(ifr, "fourWheelerAvailable==" + fourWheelerAvailable);
		String twoWAvailable = staffTrnRes.get(0).get(10);
		jsonObj.put("Key", twoWAvailable);
		twoWAvailable = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "twoWAvailable==" + twoWAvailable);
		twoWheelerAvailable = Double.parseDouble(twoWAvailable);

		// twoWheelerAvailable= valid.getValue(ifr, jsonObj,"Key", "0.0");
		Log.consoleLog(ifr, "twoWheelerAvailable==" + twoWheelerAvailable);
		String otherDed = staffTrnRes.get(0).get(11).toString();
		otherDeducation = Double.parseDouble(otherDed.isEmpty() ? "0.0" : otherDed);
		String vehicleCotegory = staffTrnRes.get(0).get(12).toString();

		String totalLoanDed = staffTrnRes.get(0).get(13).toString();
		jsonObj.put("Key", totalLoanDed);
		totalLoanDed = valid.getValue(ifr, jsonObj, "Key", "0.0");
		Log.consoleLog(ifr, "totalLoanDed==" + totalLoanDed);

		Log.consoleLog(ifr, "designation==" + designation);
		Log.consoleLog(ifr, "probation==" + probation);
		Log.consoleLog(ifr, "purpose==" + purpose);
		Log.consoleLog(ifr, "fuelType==" + fuelType);
		Log.consoleLog(ifr, "fourWAvailable==" + fourWAvailable);
		Log.consoleLog(ifr, "twoWAvailable==" + twoWAvailable);
		Log.consoleLog(ifr, "otherDed==" + otherDeducation);
		Log.consoleLog(ifr, "vehicleCotegory==" + vehicleCotegory);
		String schemeLimitQuery = "SELECT prd_rng_to, category,SUB_PRODUCT_CODE_CBS,"
				+ "SUB_PRODUCT,AMMORTIZATION_PORTAL_RATE_OF_INTEREST,roi_type,prd_rng_from,AMMORTIZATION_PORTAL_SCHEDULE_CODE,MINIMUM_NTH_PERCENT,AMMORTIZATION_PORTAL_PRODUCT_CODE "
				+ "from staff_vl_prd_designation_matrix " + "where designation='" + designation
				+ "' and lower(loan_purpose) like lower('%" + purpose + "%') and lower(loan_purpose) like lower('%"
				+ vehicleCotegory + "%') and fuel_type='" + fuelType + "' and PROBATION_TAG='" + probation + "'";
		List<List<String>> schemeLimitQueryRes = ifr.getDataFromDB(schemeLimitQuery);
		Log.consoleLog(ifr, "schemeLimitQuery==" + schemeLimitQuery);
		if (!schemeLimitQueryRes.isEmpty()) {
			Log.consoleLog(ifr, "productCode" + productCode);
			productCode = schemeLimitQueryRes.get(0).get(2);
			Log.consoleLog(ifr, "productCode==" + productCode);
			roi = Double.parseDouble(schemeLimitQueryRes.get(0).get(4));
			Log.consoleLog(ifr, "roi==" + roi);
			subProductCode = schemeLimitQueryRes.get(0).get(3);
			Log.consoleLog(ifr, "subProductCode===" + subProductCode);
			emiType = schemeLimitQueryRes.get(0).get(5);
			Log.consoleLog(ifr, "emiType===" + emiType);

			scheduleCode = schemeLimitQueryRes.get(0).get(7);
			Log.consoleLog(ifr, "scheduleCode===" + scheduleCode);
			minNTHPercentage = Double.parseDouble(schemeLimitQueryRes.get(0).get(8).trim());
			Log.consoleLog(ifr, "minNTHPercentage===" + minNTHPercentage);
			String stage = ifr.getValue("PresentStage").toString();
			if (stage.equalsIgnoreCase("PORTAL")) {
				defaultProductCode = schemeLimitQueryRes.get(0).get(9);
			} else {

				String queryForScheduleType = "SELECT SCHEDULE_CODE,PRODUCT_CODE_VL FROM SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "queryForScheduleType query===>" + queryForScheduleType);
				List<List<String>> res = ifr.getDataFromDB(queryForScheduleType);
				Log.consoleLog(ifr, "res===>" + res);
				if (!res.isEmpty()) {
					scheduleCode = res.get(0).get(0);
					defaultProductCode = res.get(0).get(1);
				}
			}
			Log.consoleLog(ifr, "defaultProductCode===" + defaultProductCode);
			Log.consoleLog(ifr, "scheduleCode===" + scheduleCode);
		}
		Log.consoleLog(ifr, "productCode==" + productCode);
		Log.consoleLog(ifr, "roi==" + roi);
		Log.consoleLog(ifr, "subProductCode==" + subProductCode);
		Log.consoleLog(ifr, "emiType==" + emiType);
		if (productCode.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		String productSheetQuery = "SELECT PRD_MINTERM,PRD_TERM,Principal_tenure_factor,Interest_tenure_factor from staff_vl_product_sheet where prd_desc='"
				+ subProductCode + "'";
		List<List<String>> prdSheetRes = ifr.getDataFromDB(productSheetQuery);
		if (prdSheetRes.isEmpty()) {
			json.put("Error", "error, Technical glitch");
			return json;
		}
		double minTenure = Double.parseDouble(prdSheetRes.get(0).get(0));
		// if()

		double maxTenure = Double.parseDouble(prdSheetRes.get(0).get(1));

		if (vehicleCotegory.equalsIgnoreCase("used")) {
			// maxTenure = Math.min(maxTenure, Double.parseDouble(residualAgeInMonths));
//			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),
//					Math.min(dateOfRetirementInMonths, rcValidUpToMonth)));
			
			maxTenure = Math.min(maxTenure, Math.min(Double.parseDouble(residualAgeInMonths),rcValidUpToMonth));
			
		}
		principalTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(2));
		interesetTenureFactor = Integer.parseInt(prdSheetRes.get(0).get(3));
		Log.consoleLog(ifr, "minTenure==" + minTenure);
		Log.consoleLog(ifr, "maxTenure==" + maxTenure);
		minNthVal = (minNTHPercentage * (pensionIncome + grossSalary));
		Log.consoleLog(ifr, "minNthVal====" + minNthVal);

		Log.consoleLog(ifr, "minNthVal==" + minNthVal);

		String pension = "";
		String totalGrossSal = "";
		double currentNthSalary = 0.0;
		double totalGrossSa = 0.0;
		double totalnetsalary = 0.0;
		String queryForPensionType = "SELECT PENSIONER,PENSION_INCOME FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ processInstanceId + "'";
		Log.consoleLog(ifr, "queryForPensionType query===>" + queryForPensionType);
		List<List<String>> res = ifr.getDataFromDB(queryForPensionType);
		Log.consoleLog(ifr, "res===>" + res);
		if (!res.isEmpty()) {
			pension = res.get(0).get(0);
		}
		if (res.size() > 0 && pension.equalsIgnoreCase("YES")) {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary + pensionIncome - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			totalGrossSa = grossSalary + pensionIncome;
			totalnetsalary = netsalary + pensionIncome;
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + totalGrossSa
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(totalnetsalary) + "' WHERE WINAME= '"
					+ processInstanceId + "'";
			ifr.saveDataInDB(QueryNetSal);

		} else {
			double netsalary = grossSalary - Double.parseDouble(totalLoanDed);
			currentNthSalary = grossSalary - (statutoryDeductions + loanDeductions + otherDeducation);
			Log.consoleLog(ifr, "currentNthSalary==" + currentNthSalary);
			String QueryNetSal = "UPDATE SLOS_STAFF_TRN SET TOTAL_GROSS_SALARY='" + grossSalary
					+ "', TOTAL_NET_SALARY= '" + String.valueOf(netsalary) + "' WHERE WINAME= '" + processInstanceId
					+ "'";
			ifr.saveDataInDB(QueryNetSal);
		}
		int eligibleAsPerNth = 0;
		double eligibleAsPNth = 0.0;
		String EMIAmount = "";
		String prob = "";

		Log.consoleLog(ifr, "eligibleAsPerNth if ");

		String probationQuery = "SELECT probation from slos_staff_trn where winame='" + processInstanceId + "' ";
		Log.consoleLog(ifr, "probation query===>" + probationQuery);
		List<List<String>> queryRes = ifr.getDataFromDB(probationQuery);
		if (!queryRes.isEmpty()) {
			prob = queryRes.get(0).get(0);
		} else {
			json.put("Error", "error,probation field is empty Technical glitch");
			return json;
		}

		Ammortization ammortization = new Ammortization();
		if (purpose.trim().toLowerCase().equals("two") && vehicleCotegory.equalsIgnoreCase("NEW")) {
			Log.consoleLog(ifr, "purposeTwo==" + purpose);
			JSONObject pMTTwoWheeler = pMTTwoWheeler(ifr, json, scheduleCode, processInstanceId, defaultProductCode,
					ammortization);
			if (pMTTwoWheeler.containsKey("Error")) {
				return pMTTwoWheeler;
			}

		} else if (purpose.trim().toLowerCase().equals("four") && vehicleCotegory.equalsIgnoreCase("NEW")) {
			Log.consoleLog(ifr, "purposeFour==" + purpose);
			JSONObject pMTFourWheeler = pMTFourWheeler(ifr, json, scheduleCode, processInstanceId, defaultProductCode,
					ammortization);
			if (pMTFourWheeler.containsKey("Error")) {
				return pMTFourWheeler;
			}
		} else {
			String tenure = String.valueOf(maxTenure);
			if (tenure == null || tenure.isEmpty() || Double.parseDouble(tenure) <= 0) {
				json.put("Error", "error, Invalid tenure");
				return json;
			}
			String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
					"100000", tenure, defaultProductCode, scheduleCode);
			Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
			String[] ammortizationResp = ammortizationResponse.split(":");

			if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
				Log.consoleLog(ifr, "Ammortization inside===================>");
				if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
					json.put("Error", "error, Ammortization error, No Response from the server");
					return json;
				} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
					json.put("Error", ammortizationResp[1]);
					return json;
				}
			}

		}

		if (prob.equalsIgnoreCase("No")) {
			String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
					+ processInstanceId
					+ "' and stagenumber='1' and (STAGENAME='FPI' OR STAGENAME LIKE '%PRINCIPAL%') AND PRINCIPALAMOUNT='100000.00'";
			List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
			Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
			Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);

			if (!ammortizationEmiQueryRes.isEmpty()) {
				EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
				if (EMIAmount.isEmpty() || EMIAmount == null) {
					json.put("Error", "error,EMIAmount is Empty");
					return json;
				}
				Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);
				eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
						* AccelatorStaffConstant.PMT_VALUE;
				eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
//				String QueryForNthCalc = "UPDATE SLOS_STAFF_VL_ELIGIBILITY SET ELG_PER_NTH='" + eligibleAsPerNth+ "' WHERE WINAME= '"+ processInstanceId + "'";
//				ifr.saveDataInDB(QueryForNthCalc);
				try {

					String queryUpdateNth = "UPDATE Slos_Staff_Vl_Eligibility SET ELG_PER_NTH='" + eligibleAsPerNth
							+ "' WHERE WINAME='" + processInstanceId + "'";

					Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
					ifr.saveDataInDB(queryUpdateNth);
//				String QueryForNthCalc = "MERGE INTO Slos_Staff_Vl_Eligibility t\r\n"
//						+ "USING (SELECT '"+processInstanceId+"' AS WINAME,\r\n"
//						+ "              '"+eligibleAsPerNth+"' AS ELG_PER_NTH\r\n"
//						+ "         FROM dual) s\r\n"
//						+ "  ON (t.WINAME = s.WINAME)\r\n"
//						+ "  WHEN MATCHED THEN\r\n"
//						+ "  UPDATE SET t.ELG_PER_NTH = s.ELG_PER_NTH\r\n"
//						+ "  WHERE NVL(t.ELG_PER_NTH,-1) <> NVL(s.ELG_PER_NTH, -1)\r\n"
//						+ " WHEN NOT MATCHED THEN\r\n"
//						+ "  INSERT (WINAME, ELG_PER_NTH)\r\n"
//						+ "  VALUES (s.WINAME, s.ELG_PER_NTH)";
//				cf.mExecuteQuery(ifr, QueryForNthCalc, "QueryForNthCalc ");
				} catch (Exception e) {
					json.put("Error", "database errors occured");
					return json;
				}

			}
		} else {
			String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
					+ processInstanceId
					+ "' and stagenumber='1' AND (STAGENAME LIKE '%EMI%' OR STAGENAME = 'E M I') AND PRINCIPALAMOUNT='100000.00'";
			List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
			Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
			Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);
			if (!ammortizationEmiQueryRes.isEmpty()) {
				EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
				if (EMIAmount.isEmpty() || EMIAmount == null) {
					json.put("Error", "error,EMIAmount is Empty");
					return json;
				}
				Log.consoleLog(ifr, "EMIAmount===>" + EMIAmount);

				eligibleAsPNth = (currentNthSalary - minNthVal) / Double.parseDouble(EMIAmount)
						* AccelatorStaffConstant.PMT_VALUE;
				eligibleAsPerNth = (int) Math.floor(eligibleAsPNth);
				try {

					String queryUpdateNth = "UPDATE Slos_Staff_Vl_Eligibility SET ELG_PER_NTH='" + eligibleAsPerNth
							+ "' WHERE WINAME='" + processInstanceId + "'";

					Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
					ifr.saveDataInDB(queryUpdateNth);
//				String QueryForNthCalc = "MERGE INTO Slos_Staff_Vl_Eligibility t\r\n"
//						+ "USING (SELECT '"+processInstanceId+"' AS WINAME,\r\n"
//						+ "              '"+eligibleAsPerNth+"' AS ELG_PER_NTH\r\n"
//						+ "         FROM dual) s\r\n"
//						+ "  ON (t.WINAME = s.WINAME)\r\n"
//						+ "  WHEN MATCHED THEN\r\n"
//						+ "  UPDATE SET t.ELG_PER_NTH = s.ELG_PER_NTH\r\n"
//						+ "  WHERE NVL(t.ELG_PER_NTH,-1) <> NVL(s.ELG_PER_NTH, -1)\r\n"
//						+ " WHEN NOT MATCHED THEN\r\n"
//						+ "  INSERT (WINAME, ELG_PER_NTH)\r\n"
//						+ "  VALUES (s.WINAME, s.ELG_PER_NTH)";
//				cf.mExecuteQuery(ifr, QueryForNthCalc, "QueryForNthCalc ");
				} catch (Exception e) {
					json.put("Error", "database errors occured");
					return json;
				}

			}

			double eligibleLoanAmt = Math.min(eligibleAsPerNth, Math.min(maxLimitOnSlider,availAmt));
			Log.consoleLog(ifr, "eligibleLoanAmt==" + eligibleLoanAmt);
			double finalEligiblity = eligibleLoanAmt;
			//long roundedVal = Math.round(finalEligiblity / 1000.0) * 1000;
			long roundedVal = (long) (Math.floor(finalEligiblity / 1000.0) * 1000);
			if (purpose.trim().toLowerCase().equals("two") && vehicleCotegory.equalsIgnoreCase("NEW")) {
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						String.valueOf(roundedVal), "84", defaultProductCode, scheduleCode);
				jsonObj = getAmmortization(ifr, json, ammortizationResponse);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			} else if (purpose.trim().toLowerCase().equals("four") && vehicleCotegory.equalsIgnoreCase("NEW")) {
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						String.valueOf(roundedVal), "180", defaultProductCode, scheduleCode);
				jsonObj = getAmmortization(ifr, json, ammortizationResponse);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			} else if (purpose.trim().toLowerCase().equals("four")
					|| purpose.trim().toLowerCase().equals("two") && vehicleCotegory.equalsIgnoreCase("USED")) {
				// tenure in place of residualAgeInMonths
				String tenure = String.valueOf(maxTenure);
				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
						String.valueOf(roundedVal), tenure, defaultProductCode, scheduleCode);
				jsonObj = getAmmortization(ifr, json, ammortizationResponse);
				if (jsonObj.containsKey("Error")) {
					return jsonObj;
				}

			}

		}
		return jsonObj;
	}

	@Override
	public JSONObject EligibilityAsPerRequestedAmtPortalGold(IFormReference ifr, String value) {

	    JSONObject json = new JSONObject();
	    json.put("Error", "");
	    json.put("Success", "");

	    try {
	        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	        String tenure = "12";

	        // -------- Fetch Staff Data --------
	        String staffTrnQuery = "select gross_salary,comp_years_of_service,pension_income,"
	                + "loan_deduction,statutory_deductions,designation,probation,purpose_loan_vl,fuel_type,"
	                + "four_w_vl_loan_available,two_w_vl_loan_available,EXT_LOAN_DED,vehicle_category,"
	                + "TOTAL_LOAN_DED,PENSIONER,"
	                + "TRUNC(MONTHS_BETWEEN( "
	                + "CASE "
	                + "WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}-\\d{2}-\\d{4}$') THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MM-YYYY') "
	                + "WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{4}-\\d{2}-\\d{2}$') THEN TO_DATE(DATE_OF_RETIREMENT, 'YYYY-MM-DD') "
	                + "WHEN REGEXP_LIKE(DATE_OF_RETIREMENT, '^\\d{2}/[A-Z]{3}/\\d{4}$') THEN TO_DATE(DATE_OF_RETIREMENT, 'DD-MON-YYYY', 'NLS_DATE_LANGUAGE=ENGLISH') "
	                + "ELSE NULL END, SYSDATE)) AS MONTHS_LEFT "
	                + "from slos_staff_trn where winame='" + processInstanceId + "'";

	        List<List<String>> staffRes = ifr.getDataFromDB(staffTrnQuery);

	        if (staffRes == null || staffRes.isEmpty()) {
	            json.put("Error", "Unable to fetch staff details");
	            return json;
	        }

	        List<String> row = staffRes.get(0);

	        double grossSalary = safeParseDouble(row.get(0));
	        double pensionIncome = safeParseDouble(row.get(2));
	        double loanDeductions = safeParseDouble(row.get(3));
	        double statutoryDeductions = safeParseDouble(row.get(4));
	        double otherDeductions = safeParseDouble(row.get(11));
	        double totalLoanDed = safeParseDouble(row.get(13));
	        
	        String pensioner = row.get(14);
	  
	      

	        if ("NO".equalsIgnoreCase(pensioner)) {
	            pensionIncome = 0.0;
	        }

	        // -------- Validate Tenure --------
	        if (tenure == null || tenure.trim().isEmpty() || safeParseDouble(tenure) <= 0) {
	            json.put("Error", "Invalid tenure");
	            return json;
	        }

//	        // -------- Ammortization Call --------
//	        Ammortization ammortization = new Ammortization();
//	        String response = ammortization.ExecuteCBS_AmmortizationHRMSVL(
//	                ifr, processInstanceId, "100000", tenure, "3028", "3001");
//
//	        if (response == null || response.isEmpty()) {
//	            json.put("Error", "fslAmmortization service returned empty response");
//	            return json;
//	        }
//
//	        String[] respArr = response.split(":");
//
//	        if (respArr.length > 0 && ("FAIL".equalsIgnoreCase(respArr[0]) || "ERROR".equalsIgnoreCase(respArr[0]))) {
//	            json.put("Error", respArr.length > 1 ? respArr[1] : "Ammortization error");
//	            return json;
//	        }
//
//	        // -------- EMI Fetch --------
//	        String emiQuery = "SELECT InstallmentAmount FROM LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
//	                + processInstanceId
//	                + "' and stagenumber='1' and STAGENAME='IPI' AND PRINCIPALAMOUNT='100000.00'";
//
//	        List<List<String>> emiRes = ifr.getDataFromDB(emiQuery);
//
//	        if (emiRes == null || emiRes.isEmpty()) {
//	            json.put("Error", "EMI data not found");
//	            return json;
//	        }
//
//	        double emiAmount = safeParseDouble(emiRes.get(0).get(0));
//
//	        if (emiAmount <= 0) {
//	            json.put("Error", "Invalid EMI amount");
//	            return json;
//	        }
	        
	        

	        // -------- Eligibility Calculation --------
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
						json.put("Error", "invalid od limit please approach branch");
						return json;
					}
				}
			} else {
				amtOdLimitD = 0;
			}

	        
	        double notionalEMI = amtOdLimitD * AcceleratorConstants.ODRATE.doubleValue()
					/ AcceleratorConstants.TWELVE.intValue();
	        double currentNthSalary = grossSalary + pensionIncome
	                - (statutoryDeductions + loanDeductions + otherDeductions +notionalEMI);

              double minNthVal = 0.25 * (pensionIncome + grossSalary);
//
//	        double eligibleAsPNth = (currentNthSalary - minNthVal) / emiAmount
//	                * AccelatorStaffConstant.PMT_VALUE;
              
          	double roi = 0.0;
    		String prodcode = "";
    		double eligiblityamtPerSal = 0.0;
    		String roiData = "select prod_code,RAT_INDEX from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%GOLD%'";

    		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
    		Log.consoleLog(ifr, "listroiData====>" + listroiData);
    		if (!listroiData.isEmpty()) {
    			prodcode = listroiData.get(0).get(0);
    			roi = Double.parseDouble(listroiData.get(0).get(1));
    		}
	        
	        double eligibleAsPNth = (currentNthSalary - 0.25 * grossSalary) * 12.0 / (roi/100);
			Log.consoleLog(ifr, "eligibleAsPerNth : " + eligibleAsPNth);

	        double eligibleAsPerNth = Math.floor(eligibleAsPNth);
	        
	        double netSalAfterAllDed = grossSalary + pensionIncome - totalLoanDed;
	        double eligibleEmi = netSalAfterAllDed - minNthVal;
	        Log.consoleLog(ifr, "eligibleEmi ===> " + eligibleEmi);

	        // Ensure value is not negativeROI
	        if (eligibleEmi <= 0) {
	            Log.consoleLog(ifr, "eligibleEmi is zero or negative");
	            eligibleEmi = 0.0;
	        }

	        // Convert to long safely
	        long maxEligibleEmiLong = (long) Math.floor(eligibleEmi);
	        Log.consoleLog(ifr, "maxEligibleEmiLong ===> " + maxEligibleEmiLong);
			//ifr.setValue("MAX_ELG_EMI", String.format("%.2f", eligibileEmi));

	        // -------- Update DB --------
	        try {
	        	String queryUpdateNth = "UPDATE Slos_Staff_GOLD_Eligibility SET ELG_PER_NTH='" + eligibleAsPerNth
						+ "' WHERE WINAME='" + processInstanceId + "'";

				Log.consoleLog(ifr, "queryUpdateNth : " + queryUpdateNth);
				ifr.saveDataInDB(queryUpdateNth);

	        } catch (Exception dbEx) {
	            Log.consoleLog(ifr, "DB Update Error: " + dbEx.getMessage());
	            json.put("Error", "Database update failed");
	            return json;
	        }

	    } catch (NumberFormatException e) {
	        Log.consoleLog(ifr, "Number format error: " + e.getMessage());
	        json.put("Error", "Invalid numeric value");

	    } catch (NullPointerException e) {
	        Log.consoleLog(ifr, "Null pointer error: " + e.getMessage());
	        json.put("Error", "Unexpected null value encountered");

	    } catch (Exception e) {
	        Log.consoleLog(ifr, "Unexpected error: " + e.getMessage());
	        json.put("Error", "System error occurred. Please try again later.");
	    }

	    return json;
	}

	private double safeParseDouble(String value) {
	    try {
	        if (value == null || value.trim().isEmpty()) {
	            return 0.0;
	        }
	        return Double.parseDouble(value);
	    } catch (Exception e) {
	        return 0.0;
	    }
	}

	@Override
	public JSONObject EligibilityAsPerRequestedAmtGold(IFormReference ifr, String value) {
		JSONObject json = new JSONObject();
		json.put("Error", "");
		json.put("Success", "");

		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String activityName = ifr.getActivityName();

			if ("onLoad".equalsIgnoreCase(value)) {

				// -------- Scheme Limit --------
				double glProduct = getDoubleFromDB(ifr,
						"select TOTAL_GOLD_LOAN_AVL from slos_staff_trn WHERE WINAME = '" + processInstanceId + "'",
						"Scheme limit not found");

				ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(glProduct));

				// -------- LTV Calculation --------
				double netWeight = getDoubleFromDB(ifr,
						"select TOTAL_NET_WEIGHT from slos_staff_jewellery_details WHERE WINAME = '" + processInstanceId
								+ "'",
						"Net weight not found");

				double lendingRate = getDoubleFromDB(ifr,
						"select LENDING_RATE from alos_m_gold_product WHERE CBS_PRODUCT_CODE = '3028'",
						"Lending rate not found");

				double ltvR = netWeight * lendingRate;
				ifr.setValue("Elg_Per_LTV", String.valueOf(ltvR));

				// -------- NTH Eligibility --------
				double nthEligibility = getDoubleFromDB(ifr,
						"Select ELG_PER_NTH from SLOS_STAFF_GOLD_ELIGIBILITY WHERE WINAME = '" + processInstanceId + "'",
						"NTH eligibility not found");

				ifr.setValue("Elg_Per_NTH", String.valueOf((int) Math.floor(nthEligibility)));
				

				// -------- Final Eligibility --------
				double finalEligibility = Math.min(Math.min(glProduct, ltvR), nthEligibility);
				long roundedVal = (long) (Math.floor(finalEligibility / 1000.0) * 1000);
				
				double roi = 0.0;
	    		String prodcode = "";
	    		double eligiblityamtPerSal = 0.0;
	    		String roiData = "select prod_code,RAT_INDEX from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where NAM_PRODUCT LIKE '%GOLD%'";

	    		List<List<String>> listroiData = ifr.getDataFromDB(roiData);
	    		Log.consoleLog(ifr, "listroiData====>" + listroiData);
	    		if (!listroiData.isEmpty()) {
	    			prodcode = listroiData.get(0).get(0);
	    			roi = Double.parseDouble(listroiData.get(0).get(1));
	    		}

				ifr.setValue("Final_Elg_VL", String.valueOf(roundedVal));
				ifr.setValue("In_Prin_Elg_Loan", String.valueOf(roundedVal));

				ifr.setValue("Max_Tenure_VL", "12");
				ifr.setValue("ROI_Type_VL", "Floating");
				ifr.setValue("ROI_VL", String.valueOf(roi));
				
				

				// -------- Requested Loan Validation --------
				String reqLoanStr = ifr.getValue("Req_Loan_Amt_VL").toString();
				

			}
//			else
//			{
//				String reqLoanStr = ifr.getValue("Req_Loan_Amt_VL").toString();
//				ifr.setValue("Req_Tenure_VL", "12");
//				ifr.setStyle("Req_Tenure_VL", "disable", "true");
//				long reqLoanAmt = Long.parseLong(reqLoanStr);
//				double finalEligiblity = getDoubleFromDB(ifr,
//						"select FINAL_ELG_GOLD_BM from SLOS_STAFF_GOLD_ELIGIBILITY WHERE WINAME = '" + processInstanceId + "'",
//						"NTH eligibility not found");
//
//				ifr.setValue("MAX_ELG_EMI", String.valueOf((int) Math.floor(finalEligiblity)));
//				
//				Ammortization ammortization = new Ammortization();
//				String ammortizationResponse = ammortization.ExecuteCBS_AmmortizationHRMSVL(ifr, processInstanceId,
//						ifr.getValue("Req_Loan_Amt_VL").toString(), "12",
//						"3028", "3001");
//				Log.consoleLog(ifr, "ammortizationResponse===>" + ammortizationResponse);
//				String[] ammortizationResp = ammortizationResponse.split(":");
//
//				if (ammortizationResp[0].equalsIgnoreCase(RLOS_Constants.ERROR) && ammortizationResp.length > 1) {
//					Log.consoleLog(ifr, "Ammortization inside===================>");
//					if (ammortizationResp[0].equalsIgnoreCase("FAIL")) {
//						json.put("Error", "error, Ammortization error, No Response from the server");
//						return json;
//					} else if (ammortizationResp[0].equalsIgnoreCase("ERROR")) {
//						json.put("Error", ammortizationResp[1]);
//						return json;
//					}
//				}
//				String ammortizationEmiQuery = "SELECT InstallmentAmount,PrincipalPayments,InterestPayments from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='"
//						+ processInstanceId + "' and stagenumber='1' and STAGENAME='IPI'";
//				List<List<String>> ammortizationEmiQueryRes = ifr.getDataFromDB(ammortizationEmiQuery);
//				Log.consoleLog(ifr, "ammortizationEmiQueryRes===>" + ammortizationEmiQueryRes);
//				Log.consoleLog(ifr, "ammortizationEmiQuery===>" + ammortizationEmiQuery);
//
//
//
//				if (!ammortizationEmiQueryRes.isEmpty()) {
//					String EMIAmount = ammortizationEmiQueryRes.get(0).get(0);// calpmt
//					if (!EMIAmount.isEmpty() || EMIAmount != null) {
//						ifr.setValue("EMI_P_Monthly_Inst_VL", String.format("%.2f", Double.parseDouble(EMIAmount)));
//					}
//				
//			}
//
//		} 
		}catch (NumberFormatException e) {
			Log.consoleLog(ifr, "Number format error: " + e.getMessage());
			json.put("Error", "Invalid numeric value encountered");

		} catch (NullPointerException e) {
			Log.consoleLog(ifr, "Null pointer error: " + e.getMessage());
			json.put("Error", "Unexpected null value encountered");

		} catch (Exception e) {
			Log.consoleLog(ifr, "Unexpected error: " + e.getMessage());
			json.put("Error", "System error occurred. Please try again later.");
		}

		return json;
	}

	private double getDoubleFromDB(IFormReference ifr, String query, String errorMsg) throws Exception {
		List<List<String>> result = ifr.getDataFromDB(query);
		Log.consoleLog(ifr, "result " + result);

		if (result == null || result.isEmpty() || result.get(0).isEmpty()) {
			throw new Exception(errorMsg);
		}

		String value = result.get(0).get(0);

		if (value == null || value.trim().isEmpty()) {
			throw new Exception(errorMsg);
		}
		Log.consoleLog(ifr, "value " + value);
		return Double.parseDouble(value);
	}
}