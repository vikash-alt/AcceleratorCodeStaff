/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ranshaw
 */
public class Collateral {

	CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String getCollateral(IFormReference ifr, String CustomerId) throws ParseException {

		Log.consoleLog(ifr, "Entered into getCollateral...");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Collateral";
		String serviceName = "CBS_" + apiName;
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String TBranch = "";
		String vehicleCat = "";
		String productCode = "";
		String collateralCode = "";
		String totalCost = "";
		String chasisNum = "";
		String engineNo = "";
		String registrationNo = "";
		String model = "";
		String mfgYear = "";
		String brand = "";
		String result = "";
		String preOwned = "";
		String dateOfVal = "";

		try {
			String collateralId = pcm.getCollateralId(ifr);
			Log.consoleLog(ifr, "VL getCollateral..." + collateralId);
			if (collateralId.equalsIgnoreCase("")) {
				// String ErrorCode = "";
				// String ErrorMessage = "";
//
				Date currentDate = new Date();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//                String formattedDate = dateFormat.format(currentDate);

				// Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
				String todaysDate = dateFormat1.format(currentDate);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(currentDate);
				calendar.add(Calendar.MONTH, 12);
				Date futureDate = calendar.getTime();
				String todaysDatePlus12Months = dateFormat1.format(futureDate);

				// Print the formatted date
				Log.consoleLog(ifr, "todaysDatePlus12Months==> " + todaysDate);
				Log.consoleLog(ifr, "todaysDatePlus12Months==> " + todaysDatePlus12Months);

				String BankCode = pcm.getConstantValue(ifr, "COLLATERAL", "BANKCODE"); // 15
				String Channel = pcm.getConstantValue(ifr, "COLLATERAL", "CHANNEL"); // BRN
				String UserId = pcm.getConstantValue(ifr, "COLLATERAL", "USERID"); // DLPMAKER
				String S_USERID = pcm.getConstantValue(ifr, "COLLATERAL", "S_USERID");
				// String TBranch = pcm.getConstantValue(ifr, "COLLATERAL", "TBRANCH"); //00100
				// check it

				String queryBranchCodeAndVehicleCat = "select BRANCH_CODE,VEHICLE_CATEGORY,PROD_SCHEME_DESC from SLOS_STAFF_TRN "
						+ "where WINAME ='" + processInstanceId + "'";
				List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);
				Log.consoleLog(ifr, "queryBranchCodeAndVehicleCat==> " + queryBranchCodeAndVehicleCat);
				if (!list.isEmpty()) {
					TBranch = list.get(0).get(0);
					vehicleCat = list.get(0).get(1);
					String prodSchemeDesc = list.get(0).get(2);
					String[] splitprodSchemeDesc = prodSchemeDesc.split("-");
					if (splitprodSchemeDesc.length > 1) {
						productCode = splitprodSchemeDesc[0];
					}
				}

				if (vehicleCat.equalsIgnoreCase("USED")) {
					preOwned = "Y";
					String queryUsedNew = "select TOTAL_COST_USED,CHASSIS_NUM_USED,ENGINE_NUM_USED,REG_NUM_USED,MODEL,DATE_OF_MANUFACTURING,BRAND,DATE_OF_VALUATION from SLOS_STAFF_COLLATERAL "
							+ "where WINAME ='" + processInstanceId + "'";
					List<List<String>> listqueryUsedNew = ifr.getDataFromDB(queryUsedNew);
					Log.consoleLog(ifr, "listqueryUsedNew==> " + listqueryUsedNew);
					if (!listqueryUsedNew.isEmpty()) {
						totalCost = listqueryUsedNew.get(0).get(0);
						chasisNum = listqueryUsedNew.get(0).get(1);
						engineNo = listqueryUsedNew.get(0).get(2);
						registrationNo = listqueryUsedNew.get(0).get(3);
						model = listqueryUsedNew.get(0).get(4);
						mfgYear = listqueryUsedNew.get(0).get(5);
						brand = listqueryUsedNew.get(0).get(6);
						dateOfVal = listqueryUsedNew.get(0).get(7);
					}
				} else if (vehicleCat.equalsIgnoreCase("NEW")) {
					preOwned = "N";
					String queryUsedNew = "select TOTAL_COST,MODEL,DATE_OF_MANUFACTURING,BRAND from SLOS_STAFF_COLLATERAL "
							+ "where WINAME ='" + processInstanceId + "'";
					List<List<String>> listqueryUsedNew = ifr.getDataFromDB(queryUsedNew);
					Log.consoleLog(ifr, "listqueryUsedNew==> " + listqueryUsedNew);
					if (!listqueryUsedNew.isEmpty()) {
						totalCost = listqueryUsedNew.get(0).get(0);
						model = listqueryUsedNew.get(0).get(1);
						mfgYear = listqueryUsedNew.get(0).get(2);
						brand = listqueryUsedNew.get(0).get(3);
					}
				}

//				String queryUsedNew = "select TOTAL_COST_USED,CHASSIS_NUM_USED,ENGINE_NUM_USED,REG_NUM_USED,MODEL,DATE_OF_MANUFACTURING,BRAND,DATE_OF_VALUATION from SLOS_STAFF_COLLATERAL "
//						+ "where WINAME ='" + processInstanceId + "'";
//				List<List<String>> listqueryUsedNew = ifr.getDataFromDB(queryUsedNew);
//				Log.consoleLog(ifr, "listqueryUsedNew==> " + listqueryUsedNew);
//				if (!listqueryUsedNew.isEmpty()) {
//					totalCost = listqueryUsedNew.get(0).get(0);
//					chasisNum = listqueryUsedNew.get(0).get(1);
//					engineNo = listqueryUsedNew.get(0).get(2);
//					registrationNo = listqueryUsedNew.get(0).get(3);
//					model = listqueryUsedNew.get(0).get(4);
//					mfgYear = listqueryUsedNew.get(0).get(5);
//					brand = listqueryUsedNew.get(0).get(6);
//					dateOfVal = listqueryUsedNew.get(0).get(7);
//				}
				if (!mfgYear.isEmpty()) {
					// DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy
					// HH:mm:ss");
					DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
					DateTimeFormatter desiredFormatter = DateTimeFormatter.ofPattern("yyyyMM");
					Log.consoleLog(ifr, "desiredFormatter==> " + desiredFormatter);

					LocalDateTime dateTime = null;

					// Try first format
					try {
						dateTime = LocalDateTime.parse(mfgYear, formatter1);
						Log.consoleLog(ifr, "dateTime==> " + dateTime);
					} catch (DateTimeParseException e1) {
						// If failed, try second format
						try {
							dateTime = LocalDateTime.parse(mfgYear, formatter2);
							Log.consoleLog(ifr, "dateTime==> " + dateTime);
						} catch (DateTimeParseException e2) {
							throw new IllegalArgumentException("Input date format not recognized: " + mfgYear);
						}
					}

					// Format to desired output
					result = dateTime.format(desiredFormatter);
				}

//				String subProductCode = pcm.mGetSubProductCode(ifr);
//				Log.consoleLog(ifr, "productCode=======>" + productCode);
//				Log.consoleLog(ifr, "subProductCode====>" + subProductCode);
				// String collateralCode = pcm.getParamConfig(ifr, productCode, subProductCode,
				// "LOANACCCREATE", "COLLCODE");

				String querycollateralCode = "select COD_COLL from BA_PROD_COLL_XREF " + "where COD_PROD ='"
						+ productCode + "'";
				List<List<String>> listquerycollateralCode = ifr.getDataFromDB(querycollateralCode);
				Log.consoleLog(ifr, "listquerycollateralCode==> " + listquerycollateralCode);
				if (!listquerycollateralCode.isEmpty()) {
					collateralCode = listquerycollateralCode.get(0).get(0);
				}

				String txtNotes1 = "HYPOTHECATION OF Brand New " + brand + " " + model;
				String fltMargin = "";
				String marginCost = "";

				String queryLendableMar = "select FLT_MARGIN from BA_PROD_COLL_XREF " + "where COD_PROD ='"
						+ productCode + "'";
				List<List<String>> listqueryLendableMar = ifr.getDataFromDB(queryLendableMar);
				Log.consoleLog(ifr, "queryLendableMar==> " + queryLendableMar);
				if (!listqueryLendableMar.isEmpty()) {
					fltMargin = listqueryLendableMar.get(0).get(0);
					Double marginCostDouble = (Double.parseDouble(totalCost) * Double.parseDouble(fltMargin)) / 100;
					marginCost = String.format("%.2f", marginCostDouble);
				}

				if (vehicleCat.equalsIgnoreCase("USED")) {
					request = "{\n" + "  \"input\": {\n" + "    \"SessionContext\": {\n"
							+ "      \"SupervisorContext\": {\n" + "        \"PrimaryPassword\": \"\",\n"
							+ "        \"UserId\": \"" + S_USERID + "\"\n" + "      },\n" + "      \"BankCode\": \""
							+ BankCode + "\",\n" + "      \"Channel\": \"" + Channel + "\",\n"
							+ "      \"ExternalBatchNumber\": \"\",\n" + "      \"ExternalReferenceNo\": \"\",\n"
							+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
							+ "      \"LocalDateTimeText\": \"\",\n" + "      \"OriginalReferenceNo\": \"\",\n"
							+ "      \"OverridenWarnings\": \"\",\n" + "      \"PostingDateText\": \"\",\n"
							+ "      \"ServiceCode\": \"\",\n" + "      \"SessionTicket\": \"\",\n"
							+ "      \"TransactionBranch\": \"" + TBranch + "\",\n" + "      \"UserId\": \"" + UserId
							+ "\",\n" + "      \"ValueDateText\": \"\"\n" + "    },\n" + "    \"ExtUniqueRefId\": \""
							+ cm.getCBSExternalReferenceNo() + "\",\n" + "    \"codColl\": \"" + collateralCode
							+ "\",\n" + "    \"codCustId\": \"" + CustomerId + "\",\n"
							+ "    \"codChargeType\": \"1\",\n" + "    \"amtPresentVal\": \"" + totalCost + "\",\n"
							+ "    \"datPresentVal\": \"" + todaysDate + "\",\n" + "    \"datNextDue\": \""
							+ todaysDatePlus12Months + "\",\n" + "    \"namLender\": \"\",\n"
							+ "    \"codCustodyStatus\": \"1\",\n" + "    \"namCustodian\": \"\",\n"
							+ "    \"datDeedSent\": \"\",\n" + "    \"datDeedReturn\": \"\",\n"
							+ "    \"txtDeedDetl1\": \"VEHICLE\",\n" + "    \"namRegnAuth\": \"\",\n"
							+ "    \"codLastMntMakerid\": \"" + UserId + "\",\n" + "    \"codLastMntChkrid\": \""
							+ S_USERID + "\",\n" + "    \"baCollProp\": {\n" + "      \"amtQuitRent\": \"\",\n"
							+ "      \"txtCollatId\": \"\",\n" + "      \"codAreaUnit\": \"\",\n"
							+ "      \"fltArea\": \"\",\n" + "      \"codFreeLease\": \"\",\n"
							+ "      \"datLeaseExpiry\": \"\",\n" + "      \"amtForcedSale\": \"\",\n"
							+ "      \"txtDesc1\": \"\"\n" + "    },\n" + "    \"baCollCultivation\": {\n"
							+ "      \"codAreaWtUnit\": \"\",\n" + "      \"codAreaNo\": \"\",\n"
							+ "      \"txtNote1\": \"\",\n" + "      \"txtNote2\": \"\",\n"
							+ "      \"codSurveyNo\": \"\",\n" + "      \"textDesc1\": \"\",\n"
							+ "      \"textDesc2\": \"\"\n" + "    },\n" + "    \"baCollAutomobile\": {\n"
							+ "      \"codChassisNo\": \"" + chasisNum + "\",\n" + "      \"codEngineNo\": \""
							+ engineNo + "\",\n" + "      \"codRegnNo\": \"" + registrationNo + "\",\n"
							+ "      \"codModel\": \"" + model + "\",\n" + "      \"codMfgYear\": \"" + result + "\",\n"
							+ "      \"txtNotes1\": \"" + txtNotes1 + "\",\n"
							+ "      \"txtNotes2\": \"HYPOTHECATION\",\n" + "      \"hSRPNo\": \"" + result + "\",\n"
							+ "      \"preOwned\": \"" + preOwned + "\"\n" + "    },\n" + "    \"baCollFinsec\": {\n"
							+ "      \"codFinsec\": \"\",\n" + "      \"ctrUnits\": \"\",\n"
							+ "      \"codSeriesNum1\": \"\",\n" + "      \"codSeriesNum2\": \"\"\n" + "    },\n"
							+ "    \"baCollNs\": {\n" + "      \"codNscollatId\": \"\",\n"
							+ "      \"txtDesc1\": \"\",\n" + "      \"txtDesc2\": \"\"\n" + "    },\n"
							+ "    \"baCollCommodity\": [{\n" + "      \"codSec\": \"\",\n"
							+ "      \"grossWtVal\": \"\",\n" + "      \"netWtVal\": \"\",\n"
							+ "      \"amtApprVal\": \"\",\n" + "      \"txtDesc\": \"\"\n" + "    }],\n"
							+ "    \"baCollCattle\": {\n" + "      \"txtBreed\": \"\",\n" + "      \"ctrAge\": \"\",\n"
							+ "      \"ctrNo\": \"\",\n" + "      \"ctrQty\": \"\",\n" + "      \"txtIdMarks\": \"\",\n"
							+ "      \"txtDsc1\": \"\"\n" + "    },\n" + "    \"baCollMachinery\": {\n"
							+ "      \"codMake\": \"\",\n" + "      \"codSlNo\": \"\",\n"
							+ "      \"codRegNo\": \"\",\n" + "      \"codEngNo\": \"\",\n"
							+ "      \"codCapacity\": \"\",\n" + "      \"txtDes\": \"\"\n" + "    },\n"
							+ "    \"totStockVal\": \"\",\n" + "    \"totCredStock\": \"\",\n"
							+ "    \"totBookDebts\": \"\",\n" + "    \"totBookDebtsOut\": \"\",\n"
							+ "    \"codStockStmtFreq\": \"\",\n" + "    \"txtValuerName\": \"\",\n"
							+ "    \"txtValuerMobNo\": \"\",\n" + "    \"txtLongitude\": \"\",\n"
							+ "    \"txtLatitude\": \"\",\n" + "    \"assetMortCrt\": \"N\"\n" + "  }\n" + "}";

				}

				else if (vehicleCat.equalsIgnoreCase("NEW")) {
					request = "{\n" + "  \"input\": {\n" + "    \"SessionContext\": {\n"
							+ "      \"SupervisorContext\": {\n" + "        \"PrimaryPassword\": \"\",\n"
							+ "        \"UserId\": \"" + S_USERID + "\"\n" + "      },\n" + "      \"BankCode\": \""
							+ BankCode + "\",\n" + "      \"Channel\": \"" + Channel + "\",\n"
							+ "      \"ExternalBatchNumber\": \"\",\n" + "      \"ExternalReferenceNo\": \"\",\n"
							+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
							+ "      \"LocalDateTimeText\": \"\",\n" + "      \"OriginalReferenceNo\": \"\",\n"
							+ "      \"OverridenWarnings\": \"\",\n" + "      \"PostingDateText\": \"\",\n"
							+ "      \"ServiceCode\": \"\",\n" + "      \"SessionTicket\": \"\",\n"
							+ "      \"TransactionBranch\": \"" + TBranch + "\",\n" + "      \"UserId\": \"" + UserId
							+ "\",\n" + "      \"ValueDateText\": \"\"\n" + "    },\n" + "    \"ExtUniqueRefId\": \""
							+ cm.getCBSExternalReferenceNo() + "\",\n" + "    \"codColl\": \"" + collateralCode
							+ "\",\n" + "    \"codCustId\": \"" + CustomerId + "\",\n"
							+ "    \"codChargeType\": \"1\",\n" + "    \"amtPresentVal\": \"" + totalCost + "\",\n"
							+ "    \"datPresentVal\": \"" + todaysDate + "\",\n" + "    \"datNextDue\": \""
							+ todaysDatePlus12Months + "\",\n" + "    \"namLender\": \"\",\n"
							+ "    \"codCustodyStatus\": \"1\",\n" + "    \"namCustodian\": \"\",\n"
							+ "    \"datDeedSent\": \"\",\n" + "    \"datDeedReturn\": \"\",\n"
							+ "    \"txtDeedDetl1\": \"VEHICLE\",\n" + "    \"namRegnAuth\": \"\",\n"
							+ "    \"codLastMntMakerid\": \"" + UserId + "\",\n" + "    \"codLastMntChkrid\": \""
							+ S_USERID + "\",\n" + "    \"baCollProp\": {\n" + "      \"amtQuitRent\": \"\",\n"
							+ "      \"txtCollatId\": \"\",\n" + "      \"codAreaUnit\": \"\",\n"
							+ "      \"fltArea\": \"\",\n" + "      \"codFreeLease\": \"\",\n"
							+ "      \"datLeaseExpiry\": \"\",\n" + "      \"amtForcedSale\": \"\",\n"
							+ "      \"txtDesc1\": \"\"\n" + "    },\n" + "    \"baCollCultivation\": {\n"
							+ "      \"codAreaWtUnit\": \"\",\n" + "      \"codAreaNo\": \"\",\n"
							+ "      \"txtNote1\": \"\",\n" + "      \"txtNote2\": \"\",\n"
							+ "      \"codSurveyNo\": \"\",\n" + "      \"textDesc1\": \"\",\n"
							+ "      \"textDesc2\": \"\"\n" + "    },\n" + "    \"baCollAutomobile\": {\n"
							+ "      \"codChassisNo\": \"\",\n" + "      \"codEngineNo\": \"\",\n"
							+ "      \"codRegnNo\": \"\",\n" + "      \"codModel\": \"" + model + "\",\n"
							+ "      \"codMfgYear\": \"" + result + "\",\n" + "      \"txtNotes1\": \"" + txtNotes1
							+ "\",\n" + "      \"txtNotes2\": \"HYPOTHECATION\",\n" + "      \"hSRPNo\": \"" + result
							+ "\",\n" + "      \"preOwned\": \"" + preOwned + "\"\n" + "    },\n"
							+ "    \"baCollFinsec\": {\n" + "      \"codFinsec\": \"\",\n"
							+ "      \"ctrUnits\": \"\",\n" + "      \"codSeriesNum1\": \"\",\n"
							+ "      \"codSeriesNum2\": \"\"\n" + "    },\n" + "    \"baCollNs\": {\n"
							+ "      \"codNscollatId\": \"\",\n" + "      \"txtDesc1\": \"\",\n"
							+ "      \"txtDesc2\": \"\"\n" + "    },\n" + "    \"baCollCommodity\": [{\n"
							+ "      \"codSec\": \"\",\n" + "      \"grossWtVal\": \"\",\n"
							+ "      \"netWtVal\": \"\",\n" + "      \"amtApprVal\": \"\",\n"
							+ "      \"txtDesc\": \"\"\n" + "    }],\n" + "    \"baCollCattle\": {\n"
							+ "      \"txtBreed\": \"\",\n" + "      \"ctrAge\": \"\",\n" + "      \"ctrNo\": \"\",\n"
							+ "      \"ctrQty\": \"\",\n" + "      \"txtIdMarks\": \"\",\n"
							+ "      \"txtDsc1\": \"\"\n" + "    },\n" + "    \"baCollMachinery\": {\n"
							+ "      \"codMake\": \"\",\n" + "      \"codSlNo\": \"\",\n"
							+ "      \"codRegNo\": \"\",\n" + "      \"codEngNo\": \"\",\n"
							+ "      \"codCapacity\": \"\",\n" + "      \"txtDes\": \"\"\n" + "    },\n"
							+ "    \"totStockVal\": \"\",\n" + "    \"totCredStock\": \"\",\n"
							+ "    \"totBookDebts\": \"\",\n" + "    \"totBookDebtsOut\": \"\",\n"
							+ "    \"codStockStmtFreq\": \"\",\n" + "    \"txtValuerName\": \"\",\n"
							+ "    \"txtValuerMobNo\": \"\",\n" + "    \"txtLongitude\": \"\",\n"
							+ "    \"txtLatitude\": \"\",\n" + "    \"assetMortCrt\": \"N\"\n" + "  }\n" + "}";

				}

				Log.consoleLog(ifr, "request sent to API" + request);
				response = cm.getWebServiceResponse(ifr, apiName, request);
				Log.consoleLog(ifr, "Response===>" + response);

				if (!response.equalsIgnoreCase("{}")) {

					JSONParser parser = new JSONParser();
					JSONObject resultObj = (JSONObject) parser.parse(response);

					String body = resultObj.get("body").toString();
					JSONObject bodyObj = (JSONObject) parser.parse(body);
					String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
					if (CheckError.equalsIgnoreCase("true")) {
						String CollateralCreationResponse = bodyObj.get("CollateralCreationResponse").toString();
						JSONObject CollateralCreationResponseObj = (JSONObject) parser
								.parse(CollateralCreationResponse);
						String collateralID = CollateralCreationResponseObj.get("collaterID").toString();
						Log.consoleLog(ifr, "collateralID==>" + collateralID);
						String Query1 = "INSERT INTO SLOS_COLLATERAL_DETAILS "
								+ "(WINAME,COD_PROD,CODCOLL,MARGIN,TOTAL_COST,MARGINAL_COST,VALUATION_DATE,CODCHASISNO,CODENGINENO,CODREGNNO,CODMODEL,CODMFGYEAR,TXTNOTES1,TXTNOTES2,HSRPNO,PREOWNED,COLLATERALID)\n"
								+ "VALUES ('" + ProcessInstanceId + "','" + productCode + "','" + collateralCode + "','"
								+ fltMargin + "','" + totalCost + "','" + marginCost + "','" + dateOfVal + "','"
								+ chasisNum + "','" + engineNo + "','" + registrationNo + "','" + model + "','" + result
								+ "','" + txtNotes1 + "','HYPOTHECATION','" + result + "','" + preOwned + "','"
								+ collateralID + "')";
						Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
						cf.mExecuteQuery(ifr, Query1, "INSERT LOANACCOUNT disbur details  ");

					} else {
						String[] ErrorData = CheckError.split("#");
						apiErrorCode = ErrorData[0];
						apiErrorMessage = ErrorData[1];
					}
				} else {
					response = "No response from the server.";
					apiErrorMessage = "FAIL";
				}

				if (apiErrorMessage.equalsIgnoreCase("")) {
					apiStatus = RLOS_Constants.SUCCESS;
//					status = RLOS_Constants.SUCCESS;
				} else {
					apiStatus = "FAIL";
//					status = RLOS_Constants.ERROR;

				}

				if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
					return apiStatus;
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

	public String getMultiCollateral(IFormReference ifr, String CustomerId) throws ParseException {

		Log.consoleLog(ifr, "Entered into getCollateral...");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Collateral";
		String serviceName = "CBS_" + apiName;
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
			Date currentDate = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
			String todaysDate = dateFormat1.format(currentDate);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.MONTH, 12);
			Date futureDate = calendar.getTime();
			String todaysDatePlus12Months = dateFormat1.format(futureDate);
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "todaysDatePlus12Months==> " + todaysDate);
			Log.consoleLog(ifr, "todaysDatePlus12Months==> " + todaysDatePlus12Months);
			String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", PID);
			List<List<String>> loanSelectedQuery = cf.mExecuteQuery(ifr, queryL,
					"Execute query for fetching loan selected ");
			String loan_selected = loanSelectedQuery.get(0).get(0);
			Log.consoleLog(ifr, "loan_selected=======>" + loan_selected);
			String BankCode = pcm.getConstantValue(ifr, "COLLATERAL", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "COLLATERAL", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "COLLATERAL", "USERID");

			String TBranch = "";
			String queryForTBranch = " SELECT branchcode FROM LOS_M_BRANCH WHERE  BRANCHNAME IN  (SELECT branch_NAME FROM SLOS_STAFF_home_trn WHERE WINAME = '"
					+ processInstanceId + "')";
			Log.consoleLog(ifr, "queryForTBranch==>" + queryForTBranch);
			List<List<String>> resqueryForTBranch = ifr.getDataFromDB(queryForTBranch);
			if (!resqueryForTBranch.isEmpty()) {
				TBranch = resqueryForTBranch.get(0).get(0);
			}

			//String TprimaryCollateral = "";
//			String queryForTprimaryCollateral = "select REALISABLEVALUEOFPLOT from LOS_CAM_COLLATERAL_DETAILS where pid = '"
//					+ processInstanceId + "'";
//			Log.consoleLog(ifr, "queryForTprimaryCollateral==>" + queryForTprimaryCollateral);
//			List<List<String>> resqueryForTprimaryCollateral = ifr.getDataFromDB(queryForTprimaryCollateral);
//			if (!resqueryForTprimaryCollateral.isEmpty()) {
//				//TprimaryCollateral = resqueryForTprimaryCollateral.get(0).get(0);
//			}
			


			// String TBranch = cm.GetHomeBranchCode(ifr, processInstanceId, loan_selected);

			// String productCode = pcm.mGetProductCode(ifr);
			String subProductCode = pcm.mGetSubProductCode(ifr);
			// Log.consoleLog(ifr, "productCode=======>" + productCode);
			Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

			String productCode = "VL";
			String assetMortCrt = pcm.getParamValue(ifr, productCode, "ASSETMORTCRT");
			Log.consoleLog(ifr, "assetMortCrt==> " + assetMortCrt);
//	            String loanSelected = pcm.getLoanSelected(ifr);
//	            Log.consoleLog(ifr, "loanSelected" + loanSelected);
			String collateralQuery = "SELECT MARKETVALUEOFPLOT,DEED_DETAILS,BUILDUPAREA,LINE1,LINE2,DISTRICT,STATE,ZIPCODE,SURVEY_MUNICIPAL_NUMBER,PANEL_VALUERID,LATITUDE,LONGITUDE,REALISABLEVALUEOFPLOT, TITLE_DEED_RECEIVED,REASON,PORTABLE_DATE_RECEIPT_TITLEDEED,DUE_DATE_EMT_CREATION,DUE_DATE_SUBSEQUENT_CHARGES, ACTUAL_DATE_EMT_CREATION,ACTUAL_DATE_SUBSEQUENT_CHARGES,insertionorderid,ASSET_SUBCATEGORY, SALE_DEED_REG_NO, EMT_MODTD_REG_NO, SALE_DEED_DATE, EMT_MODTD_DATE,REGISTRATIONOFFICE FROM LOS_CAM_COLLATERAL_DETAILS WHERE PID='"
					+ processInstanceId + "'";
			List<List<String>> collateralsResult = cf.mExecuteQuery(ifr, collateralQuery,
					"HLBkoffcCustomCode:collateralQuery->" + "Execute query for fetching COLLATERAL selected: ");
			LocalDate currentDateAndTime = LocalDate.now();
			String sysdate = parseDate(currentDateAndTime.toString());
			for (List<String> collateral : collateralsResult) {

				String MARKETVALUEOFPLOT = collateral.get(0);
				Log.consoleLog(ifr, "MARKETVALUEOFPLOT==> " + MARKETVALUEOFPLOT);
				String DEED_DETAILS = collateral.get(1);
				Log.consoleLog(ifr, "DEED_DETAILS==> " + DEED_DETAILS);
				String BUILDUPAREA = collateral.get(2);
				Log.consoleLog(ifr, "BUILDUPAREA==> " + BUILDUPAREA);
				String ADDRESSLINE1 = collateral.get(3);
				Log.consoleLog(ifr, "ADDRESSLINE1==> " + ADDRESSLINE1);
				String ADDRESSLINE2 = collateral.get(4);
				Log.consoleLog(ifr, "ADDRESSLINE2==> " + ADDRESSLINE2);

				String DISTRICT = collateral.get(5);
				Log.consoleLog(ifr, "DISTRICT==> " + DISTRICT);

				String STATE = collateral.get(6);
				Log.consoleLog(ifr, "STATE==> " + STATE);

				String ZIPCODE = collateral.get(7);
				Log.consoleLog(ifr, "ZIPCODE==> " + ZIPCODE);

				String SURVEY_MUNICIPAL_NUMBER = collateral.get(8);
				Log.consoleLog(ifr, "SURVEY_MUNICIPAL_NUMBER==> " + SURVEY_MUNICIPAL_NUMBER);

//				String PANELVALUERCODE = collateral.get(9);
//				Log.consoleLog(ifr, "PANELVALUERCODE==> " + PANELVALUERCODE);

				String LATITUDE = collateral.get(10);
				Log.consoleLog(ifr, "LATITUDE==> " + LATITUDE);

				String LONGITUDE = collateral.get(11);
				Log.consoleLog(ifr, "LONGITUDE==> " + LONGITUDE);

				String REALISABLEVALUEOFPLOT = collateral.get(12);
				Log.consoleLog(ifr, "REALISABLEVALUEOFPLOT==> " + REALISABLEVALUEOFPLOT);

				String TITLE_DEED_RECEIVED = collateral.get(13).toString().equalsIgnoreCase("YES") ? "Y" : "N";
				Log.consoleLog(ifr, "TITLE_DEED_RECEIVED==> " + TITLE_DEED_RECEIVED);

				String REASON = collateral.get(14);
				Log.consoleLog(ifr, "REASON==> " + REASON);

				String PORTABLE_DATE_RECEIPT_TITLEDEED = collateral.get(15);
				PORTABLE_DATE_RECEIPT_TITLEDEED = checkIfOLdDate(ifr, parseDate(PORTABLE_DATE_RECEIPT_TITLEDEED));
				Log.consoleLog(ifr, "PORTABLE_DATE_RECEIPT_TITLEDEED==> " + PORTABLE_DATE_RECEIPT_TITLEDEED);
				String PORTABLE_DATE_RECEIPT_TITLEDEED_formattedDate = (PORTABLE_DATE_RECEIPT_TITLEDEED != null
						&& !PORTABLE_DATE_RECEIPT_TITLEDEED.isBlank()) ? PORTABLE_DATE_RECEIPT_TITLEDEED : "";
				Log.consoleLog(ifr, "PORTABLE_DATE_RECEIPT_TITLEDEED_formattedDate==> "
						+ PORTABLE_DATE_RECEIPT_TITLEDEED_formattedDate);

				String DUE_DATE_EMT_CREATION = collateral.get(16);
				Log.consoleLog(ifr, "DUE_DATE_EMT_CREATION==> " + DUE_DATE_EMT_CREATION);

				String DUE_DATE_EMT_CREATION_formattedDate = (DUE_DATE_EMT_CREATION != null
						&& !DUE_DATE_EMT_CREATION.isEmpty()) ? parseDate(DUE_DATE_EMT_CREATION) : "";
				Log.consoleLog(ifr, "DUE_DATE_EMT_CREATION_formattedDate==> " + DUE_DATE_EMT_CREATION_formattedDate);

				String DUE_DATE_SUBSEQUENT_CHARGES = collateral.get(17);
				Log.consoleLog(ifr, "DUE_DATE_SUBSEQUENT_CHARGES==> " + DUE_DATE_SUBSEQUENT_CHARGES);

				DUE_DATE_SUBSEQUENT_CHARGES = checkIfOLdDate(ifr, parseDate(DUE_DATE_SUBSEQUENT_CHARGES));
				String DUE_DATE_SUBSEQUENT_CHARGES_formattedDate = (DUE_DATE_SUBSEQUENT_CHARGES != null
						&& !DUE_DATE_SUBSEQUENT_CHARGES.isEmpty()) ? parseDate(DUE_DATE_SUBSEQUENT_CHARGES) : "";
				Log.consoleLog(ifr,
						"DUE_DATE_SUBSEQUENT_CHARGES_formattedDate==> " + DUE_DATE_SUBSEQUENT_CHARGES_formattedDate);

				String ACTUAL_DATE_EMT_CREATION = collateral.get(18);
				Log.consoleLog(ifr, "ACTUAL_DATE_EMT_CREATION==> " + ACTUAL_DATE_EMT_CREATION);

				String ACTUAL_DATE_EMT_CREATION_formattedDate = (ACTUAL_DATE_EMT_CREATION != null
						&& !ACTUAL_DATE_EMT_CREATION.isEmpty()) ? parseDate(ACTUAL_DATE_EMT_CREATION) : "";
				Log.consoleLog(ifr,
						"ACTUAL_DATE_EMT_CREATION_formattedDate==> " + ACTUAL_DATE_EMT_CREATION_formattedDate);

				String ACTUAL_DATE_SUBSEQUENT_CHARGES = collateral.get(19);
				Log.consoleLog(ifr, "ACTUAL_DATE_SUBSEQUENT_CHARGES==> " + ACTUAL_DATE_SUBSEQUENT_CHARGES);

				String INSERTIONORDERID = collateral.get(20);
				Log.consoleLog(ifr, "INSERTIONORDERID==> " + INSERTIONORDERID);

				String ASSET_SUBCATEGORY = collateral.get(21);
				Log.consoleLog(ifr, "ASSET_SUBCATEGORY==> " + ASSET_SUBCATEGORY);

				String SALE_DEED_REG_NO = collateral.get(22);
				Log.consoleLog(ifr, "SALE_DEED_REG_NO==> " + SALE_DEED_REG_NO);

				String CodMortgage = "1";

				String EMT_MODTD_REG_NO = collateral.get(23);
				Log.consoleLog(ifr, "EMT_MODTD_REG_NO==> " + EMT_MODTD_REG_NO);

				String SALE_DEED_DATE = collateral.get(24);
				Log.consoleLog(ifr, "SALE_DEED_DATE==> " + SALE_DEED_DATE);

				String SALE_DEED_DATE_formattedDate = (SALE_DEED_DATE != null && !SALE_DEED_DATE.isEmpty())
						? parseDate(SALE_DEED_DATE)
						: "";
				Log.consoleLog(ifr, "SALE_DEED_DATE_formattedDate==> " + SALE_DEED_DATE_formattedDate);

				String EMT_MODTD_DATE = collateral.get(25);
				Log.consoleLog(ifr, "EMT_MODTD_DATE==> " + EMT_MODTD_DATE);

				String EMT_MODTD_DATE_LOCALDATE_formattedDate = (EMT_MODTD_DATE != null && !EMT_MODTD_DATE.isEmpty())
						? parseDate(EMT_MODTD_DATE)
						: "";
				Log.consoleLog(ifr,
						"EMT_MODTD_DATE_LOCALDATE_formattedDate==> " + EMT_MODTD_DATE_LOCALDATE_formattedDate);

				if (TITLE_DEED_RECEIVED.equalsIgnoreCase("Y")) {
					REASON = "";
					PORTABLE_DATE_RECEIPT_TITLEDEED_formattedDate = "";
				} else {
					SALE_DEED_REG_NO = "";
					EMT_MODTD_REG_NO = "";
					SALE_DEED_DATE_formattedDate = "";
					EMT_MODTD_DATE_LOCALDATE_formattedDate = "";
				}
				String MV_RES_MOBNO = "";
				String MV_NAME ="";
				String REG_OOFICE = collateral.get(26);
				Log.consoleLog(ifr, "REG_OOFICE==> " + REG_OOFICE);

				String ACTUAL_DATE_SUBSEQUENT_CHARGES_formattedDate = (ACTUAL_DATE_SUBSEQUENT_CHARGES != null
						&& !ACTUAL_DATE_SUBSEQUENT_CHARGES.isEmpty()) ? parseDate(ACTUAL_DATE_SUBSEQUENT_CHARGES) : "";
				Log.consoleLog(ifr, "ACTUAL_DATE_SUBSEQUENT_CHARGES_formattedDate==> "
						+ ACTUAL_DATE_SUBSEQUENT_CHARGES_formattedDate);

				String valuerDetailsQuery = "select trim(VALUERNAME), trim(VALUERMOBILENUMBER) from LOS_NL_PROPERTY_VALUATION where pid='"+processInstanceId+"'";
				List<List<String>> valuerDetailsQueryResult = cf.mExecuteQuery(ifr, valuerDetailsQuery,
						"HLBkoffcCustomCode:collateralQuery->" + "Execute query for fetching Valuer selected: ");
				
				if (!valuerDetailsQueryResult.isEmpty()) {
					MV_RES_MOBNO = valuerDetailsQueryResult.get(0).get(1);
					Log.consoleLog(ifr, "MV_RES_MOBNO==> " + MV_RES_MOBNO);

					MV_NAME = valuerDetailsQueryResult.get(0).get(0);
				}
				if (MV_NAME.length() > 15) {
					MV_NAME = MV_NAME.substring(0, 15);
				}
				Log.consoleLog(ifr, "MV_NAME==> " + MV_NAME);
				

				// SELECT MV_RES_MOBNO,MV_NAME FROM LOS_M_VALUER_MASTER WHERE
				// MV_CODE='#MV_CODE#'
				//
				// SELECT
				// MARKETVALUEOFPLOT,DEED_DETAILS,BUILDUPAREA,LINE1,LINE2,DISTRICT,STATE,ZIPCODE,
				// SURVEY_MUNICIPAL_NUMBER,NAMEOFPANELVALUER,LATITUDE,LONGITUDE,REALISABLEVALUEOFPLOT,
				// TITLE_DEED_RECEIVED,REASON,PORTABLE_DATE_RECEIPT_TITLEDEED,DUE_DATE_EMT_CREATION,DUE_DATE_SUBSEQUENT_CHARGES,
				// ACTUAL_DATE_EMT_CREATION,ACTUAL_DATE_SUBSEQUENT_CHARGES
				// FROM LOS_CAM_COLLATERAL_DETAILS WHERE PID='LOS-00000000000005976'

				// valutaion market value, and

				request = "{\n" + " \"input\": {\n" + "     \"SessionContext\": {\n"
						+ "         \"SupervisorContext\": {\n" + "             \"PrimaryPassword\": \"\",\n"
						+ "             \"UserId\":  \"" + UserId + "\"\n" + "         },\n"
						+ "         \"BankCode\":\"" + BankCode + "\",\n" + "         \"Channel\": \"" + Channel
						+ "\",\n" + "         \"ExternalBatchNumber\": \"\",\n"
						+ "         \"ExternalReferenceNo\": \"\",\n"
						+ "         \"ExternalSystemAuditTrailNumber\": \"\",\n"
						+ "         \"LocalDateTimeText\": \"\",\n" + "         \"OriginalReferenceNo\": \"\",\n"
						+ "         \"OverridenWarnings\": \"\",\n" + "         \"PostingDateText\": \"\",\n"
						+ "         \"ServiceCode\": \"\",\n" + "         \"SessionTicket\": \"\",\n"
						+ "         \"TransactionBranch\": \"" + TBranch + "\",\n" + "         \"UserId\": \"" + UserId
						+ "\",\n" + "         \"ValueDateText\": \"\"\n" + "     },\n" + "     \"ExtUniqueRefId\": \""
						+ cm.getCBSExternalReferenceNo() + "\",\n" + "     \"codColl\": \"" + ASSET_SUBCATEGORY
						+ "\",\n" // code of land, building
						+ "     \"codCustId\": \"" + CustomerId + "\",\n" + "     \"codChargeType\": \"1\",\n"
						+ "     \"amtPresentVal\": \"" + MARKETVALUEOFPLOT + "\",\n" // Market Value Of Property/ Plot
						+ "     \"datPresentVal\": \"" + sysdate + "\",\n" // sysdate
						+ "     \"datNextDue\": \"" + sysdate + "\",\n" // sysdate
						+ "     \"namLender\": \"\",\n" + "     \"codCustodyStatus\": \"1\",\n"
						+ "     \"namCustodian\": \"\",\n" + "     \"datDeedSent\": \"" + sysdate + "\",\n" // sysdate
						+ "     \"datDeedReturn\": \"" + sysdate + "\",\n" // sysdate
						+ "     \"txtDeedDetl1\": \"" + DEED_DETAILS + "\",\n"// Deed Details
						+ "     \"namRegnAuth\": \"" + REG_OOFICE + "\",\n"// Reg office
						+ "     \"codLastMntMakerid\": \"" + UserId + "\",\n" + "     \"codLastMntChkrid\": \"" + UserId
						+ "\",\n" + "     \"baCollProp\": {\n" + "         \"amtQuitRent\": \"\",\n"
						+ "         \"txtCollatId\": \"0\",\n" + "         \"codAreaUnit\": \"Sq.Ft\",\n"
						+ "         \"fltArea\": \"" + BUILDUPAREA + "\",\n" // BUILDUPAREA
						+ "         \"codFreeLease\": \"0\",\n" + "         \"datLeaseExpiry\": \"\",\n"
						+ "         \"amtForcedSale\": \"\",\n" + "         \"txtDesc1\": \"" + ADDRESSLINE1 + "\",\n" // collateral
																														// table
						+ "         \"SurveyNos\": \"" + SURVEY_MUNICIPAL_NUMBER + "\",\n" // collateral table
						+ "         \"district\": \"" + DISTRICT + "\",\n" // collateral table
						+ "         \"state\": \"" + STATE + "\",\n" // collateral table
						+ "         \"address1\": \"" + ADDRESSLINE1 + "\",\n" // collateral table
						+ "         \"address2\": \"" + ADDRESSLINE2 + "\",\n" // collateral table
						+ "         \"zipCode\": \"" + ZIPCODE + "\"\n" // collateral table
						+ "     },\n" + "     \"baCollCultivation\": {\n" + "         \"codAreaWtUnit\": \"\",\n"
						+ "         \"codAreaNo\": \"\",\n" + "         \"txtNote1\": \"\",\n"
						+ "         \"txtNote2\": \"\",\n" + "         \"codSurveyNo\": \"\",\n"
						+ "         \"textDesc1\": \"\",\n" + "         \"textDesc2\": \"\"\n" + "     },\n"
						+ "     \"baCollAutomobile\": {\n" + "         \"codChassisNo\": \"\",\n"
						+ "         \"codEngineNo\": \"\",\n" + "         \"codRegnNo\": \"\",\n"
						+ "         \"codModel\": \"\",\n" + "         \"codMfgYear\": \"\",\n"
						+ "         \"txtNotes1\": \"\",\n" + "         \"txtNotes2\": \"\",\n"
						+ "         \"hSRPNo\": \"\",\n" + "         \"preOwned\": \"Y\"\n" + "     },\n"
						+ "     \"baCollFinsec\": {\n" + "         \"codFinsec\": \"\",\n"
						+ "         \"ctrUnits\": \"\",\n" + "         \"codSeriesNum1\": \"\",\n"
						+ "         \"codSeriesNum2\": \"\"\n" + "     },\n" + "     \"baCollNs\": {\n"
						+ "         \"codNscollatId\": \"\",\n" + "         \"txtDesc1\": \"" + ADDRESSLINE1 + "\",\n" // collateral
																														// table
						+ "         \"txtDesc2\": \"\"\n" + "     },\n" + "     \"baCollCommodity\": {\n"
						+ "         \"codSec\": \"\",\n" + "         \"grossWtVal\": \"\",\n"
						+ "         \"netWtVal\": \"\",\n" + "         \"amtApprVal\": \"\",\n"
						+ "         \"txtDesc\": \"\"\n" + "     },\n" + "     \"baCollCattle\": {\n"
						+ "         \"txtBreed\": \"\",\n" + "         \"ctrAge\": \"\",\n"
						+ "         \"ctrNo\": \"\",\n" + "         \"ctrQty\": \"\",\n"
						+ "         \"txtIdMarks\": \"\",\n" + "         \"txtDsc1\": \"\"\n" + "     },\n"
						+ "     \"baCollMachinery\": {\n" + "         \"codMake\": \"\",\n"
						+ "         \"codSlNo\": \"\",\n" + "         \"codRegNo\": \"\",\n"
						+ "         \"codEngNo\": \"\",\n" + "         \"codCapacity\": \"\",\n"
						+ "         \"txtDes\": \"\"\n" + "     },\n" + "     \"txtValuerName\": \"" + MV_NAME + "\",\n" // collateral
																															// table
						+ "     \"txtValuerMobNo\": \"" + MV_RES_MOBNO + "\",\n" + "     \"txtLongitude\": \""
						+ LONGITUDE + "\",\n" //// collateral table
						+ "     \"txtLatitude\": \"" + LATITUDE + "\",\n" // collateral table
						+ "     \"baCollSecondVal\": {\n"// agengy management value dexcription
						+ "         \"txtValuerName2\": \"\",\n" + "         \"amtPresentVal2\": \"\",\n"
						+ "         \"txtValuerMobNo2\": \"\",\n" + "         \"txtLongitude2\": \"\",\n"
						+ "         \"txtLatitude2\": \"\",\n" + "         \"DatPresentVal2\": \"\"\n" + "     },\n"
						+ "     \"amtCurrentRealizableVal\": \"" + REALISABLEVALUEOFPLOT + "\",\n" // REALISABLEVALUEOFPLOT
																								// Of Property/ Plot
						+ "     \"assetMortCrt\": \"" + assetMortCrt + "\",\n" + "     \"amtRealizableVal\": \"\",\n"
						+ "     \"emtDetails\": {\n" + "         \"codEmtDocAvl\": \"2\",\n"// always 2
						+ "         \"flgTitleDeedsReceived\": \"" + TITLE_DEED_RECEIVED + "\",\n" // Title Deed
																									// Received :
						+ "         \"codSaleDeedRegNo\": \"" + SALE_DEED_REG_NO + "\",\n"
						+ "         \"CodMortgageTyp\": \"" + CodMortgage + "\",\n" + "         \"datSaleDeedReg\": \""
						+ SALE_DEED_DATE_formattedDate + "\",\n" + "         \"codEmtModtdRegNo\": \""
						+ EMT_MODTD_REG_NO + "\",\n" + "         \"datEmtModtdReg\": \""
						+ EMT_MODTD_DATE_LOCALDATE_formattedDate + "\",\n" + "         \"codReason\": \"" + REASON
						+ "\",\n" + "         \"datProbableReceipt\": \""
						+ PORTABLE_DATE_RECEIPT_TITLEDEED_formattedDate + "\",\n" + "         \"datDueCreationEmt\": \""
						+ DUE_DATE_EMT_CREATION_formattedDate + "\",\n" + "         \"datDueCreationSubseqCharg\": \""
						+ DUE_DATE_SUBSEQUENT_CHARGES_formattedDate + "\",\n" + "         \"datActualCreationEmt\": \""
						+ ACTUAL_DATE_EMT_CREATION_formattedDate + "\",\n" + "         \"datActualCreationSubseq\": \""
						+ ACTUAL_DATE_SUBSEQUENT_CHARGES_formattedDate + "\"\n" + "     }\n" + " }\n" + "}";

				SALE_DEED_REG_NO = "";
				EMT_MODTD_REG_NO = "";
				SALE_DEED_DATE_formattedDate = "";
				EMT_MODTD_DATE_LOCALDATE_formattedDate = "";
				Log.consoleLog(ifr, "request sent to API" + request);
				response = cm.getWebServiceResponse(ifr, apiName, request);
				Log.consoleLog(ifr, "Response===>" + response);

				if (!response.equalsIgnoreCase("{}")) {

					JSONParser parser = new JSONParser();
					JSONObject resultObj = (JSONObject) parser.parse(response);

					String body = resultObj.get("body").toString();
					JSONObject bodyObj = (JSONObject) parser.parse(body);
					String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
					if (CheckError.equalsIgnoreCase("true")) {
						String CollateralCreationResponse = bodyObj.get("CollateralCreationResponse").toString();
						JSONObject CollateralCreationResponseObj = (JSONObject) parser
								.parse(CollateralCreationResponse);
						String collateralID = CollateralCreationResponseObj.get("collaterID").toString();
						Log.consoleLog(ifr, "collateralID==>" + collateralID);

						String collateralInsertQuery = "update LOS_CAM_COLLATERAL_DETAILS set COLLATERAL_ID='"
								+ collateralID + "' where PID='" + processInstanceId + "' and insertionorderid='"
								+ INSERTIONORDERID + "'";
						Log.consoleLog(ifr, "BranchDisbursement:updateBranchDisbursement:collateralInsertQuery->"
								+ collateralInsertQuery);
						ifr.saveDataInDB(collateralInsertQuery);

					} else {
						String[] ErrorData = CheckError.split("#");
						apiErrorCode = ErrorData[0];
						apiErrorMessage = ErrorData[1];
					}
				} else {
					response = "No response from the server.";
					apiErrorMessage = "FAIL";
				}

				if (apiErrorMessage.equalsIgnoreCase("")) {
					apiStatus = RLOS_Constants.SUCCESS;
				} else {
					apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;
				}
				
				if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
					return apiStatus;
				}
			}
			//return apiStatus;
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/COLLATERAL===>" + e);
			Log.errorLog(ifr, "Exception/COLLATERAL===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR+":"+apiErrorMessage;

	}

	public static String parseDate(String dateStr) {
		if (dateStr.isEmpty()) {
			return "";
		}
		if (dateStr.contains(" ")) {
			dateStr = dateStr.substring(0, dateStr.indexOf(" ")); // Keep only date part
		}

		String availablePatterns = "dd-MM-yyyy,yyyyMMdd,yyyy/MM/dd,dd/MM/yyyy,yyyy-MM-dd";
		DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
		String[] patterns = availablePatterns.split(",");
		for (String pattern : patterns) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				sdf.setLenient(false); // Enforce strict parsing
				Date date = sdf.parse(dateStr);
				String format = targetFormat.format(date);
				return format;
			} catch (Exception e) {
			}
		}
		return "";
	}

	public static String checkIfOLdDate(IFormReference ifr, String enteredDate) {
		if (enteredDate.isEmpty()) {
			return "";
		}
		Log.consoleLog(ifr, "Inside Check if Old date: " + enteredDate);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		LocalDate date = LocalDate.parse(enteredDate, formatter);
		LocalDate today = LocalDate.now();

		// Compare only the date part
		if (date.isBefore(today)) {
			LocalDateTime todayMidnight = today.atStartOfDay();
			enteredDate = todayMidnight.format(formatter);
		}
		return enteredDate;
	}
}
