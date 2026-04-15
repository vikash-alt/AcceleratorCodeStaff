/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.AcceleratorBaseCode;
import com.newgen.iforms.budget.BudgetDisbursementScreen;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.dlp.integration.common.APICommonMethods;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author ranshaw
 */
public class LoanAccountCreation {

	CommonFunctionality cf = new CommonFunctionality();
	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();

	public String getLoanAccountDetails(IFormReference ifr, String ProcessInstanceId, String ProductCode,
			String CustomerId, String loanAmount, String Tenure, String SanctionExpiryDate, String journeyType)
			throws ParseException {

		// JSONObject message = new JSONObject();
		Log.consoleLog(ifr, "#Execute_CBSLoanAccountCreation=================");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String apiName = "LoanAccountCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String TBranch = "";
		String productCode = "";
		String CodMisTxn1 = "";
		String CodMisTxn2 = "";
		String CodMisTxn3 = "";
		String CodMisTxn4 = "";
		String CodMisTxn5 = "";
		String CodMisTxn6 = "";
		String CodMisTxn7 = "";
		String CodMisTxn8 = "";
		String CodMisTxn9 = "";
		String CodMisTxn10 = "";
		String CodMisTxn11 = "";
		String CodMisComp1 = "";
		String BankCode = "";
		String Channel = "";
		String UserId = "";
		String Sc_UserId = "";
		String scheduleCode = "";
		String purposeofLoan = "";
		String sanctionDate = null;
		String appNumber = "";
		String collId = "";
		String margin = "";
		String marginCost = "";
		String totalCost = "";
		String salaryAccNum = "";
		String fltMargin = "";
		String lendableAmt = "";
		String baselCode = "";

		String LoanAccNumber = "";
		try {

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formattedDate = dateFormat.format(currentDate);

			String applicationNumber = pcm.getApplicationRefNumber(ifr);// Added by Ahmed on 24/06/2024 for getting
																		// RefNo from CommonMethod

			String SancDate = AcceleratorBaseCode.SanctionDate;

			String query = "select RBI_PURPOSE_CODE,SECTOR,RETAIL_BASEL_II_CUSTOMER_TYPE,SCHEMES,GUARANTEE_COVER,BSR_CODE,SSISUBSEC,STATUSIB,PRI_SECTOR_N_PRI_SECTOR,SPECIAL_BENEFICIARIES,SUB_SCHEME,RAH from LOS_L_BAM83 "
					+ "where pid ='" + processInstanceId + "'";
			List<List<String>> Result = cf.mExecuteQuery(ifr, query, " BAM 83 Update ");

			if (!Result.isEmpty()) {
				CodMisTxn1 = Result.get(0).get(0);
				CodMisTxn2 = Result.get(0).get(1);
				CodMisTxn3 = Result.get(0).get(2);
				CodMisTxn4 = Result.get(0).get(3);
				CodMisTxn5 = Result.get(0).get(4);
				CodMisTxn6 = Result.get(0).get(5);
				CodMisTxn7 = Result.get(0).get(6);
				CodMisTxn8 = Result.get(0).get(7);
				CodMisTxn9 = Result.get(0).get(8);
				CodMisTxn10 = Result.get(0).get(9);
				CodMisTxn11 = Result.get(0).get(10);
				CodMisComp1 = Result.get(0).get(11);
			} else {
				return "BAM83 ERROR";
			}

			String purpoLoan = "";
			BankCode = pcm.getConstantValue(ifr, "LOANACCCREATE", "BANKCODE"); // 15
			Channel = pcm.getConstantValue(ifr, "LOANACCCREATE", "CHANNEL"); // BRN
			UserId = pcm.getConstantValue(ifr, "LOANACCCREATE", "USERID"); // DLPMAKER
			Sc_UserId = pcm.getConstantValue(ifr, "LOANACCCREATE", "SC_USERID");
			String vehicleCat = "";

			String queryBranchCodeAndVehicleCat = "select BRANCH_CODE,PROD_SCHEME_DESC,SCHEDULE_CODE,PURPOSE_LOAN_VL,SALARY_ACC_NUMBER,VEHICLE_CATEGORY from SLOS_STAFF_TRN "
					+ "where WINAME ='" + processInstanceId + "'";
			List<List<String>> list = ifr.getDataFromDB(queryBranchCodeAndVehicleCat);
			Log.consoleLog(ifr, "queryBranchCodeAndVehicleCat==> " + queryBranchCodeAndVehicleCat);
			if (!list.isEmpty()) {
				TBranch = list.get(0).get(0);
				String prodSchemeDesc = list.get(0).get(1);
				scheduleCode = list.get(0).get(2);
				purpoLoan = list.get(0).get(3);
				salaryAccNum = list.get(0).get(4);
				vehicleCat = list.get(0).get(5);
				String[] splitprodSchemeDesc = prodSchemeDesc.split("-");
				if (splitprodSchemeDesc.length > 1) {
					productCode = splitprodSchemeDesc[0];
				}
			}
			if (purpoLoan.equalsIgnoreCase("two")) {
				purposeofLoan = "purchase of two wheeler";
			}
			if (purpoLoan.equalsIgnoreCase("four")) {
				purposeofLoan = "purchase of four wheeler";
			}

//				String formaDate="";
//				String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loandetails " + "WHERE PID='"
//						+ processInstanceId + "' AND ROWNUM=1";
//				Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);
//
//				List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");
//				if (!ResultSanctionDate.isEmpty()) {
//					sanctionDate = ResultSanctionDate.get(0).get(0);
//					Log.consoleLog(ifr, "sanctionDate==>" + sanctionDate);
//				}
//				
//				
////
//                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
////
//				Date date = originalFormat.parse(sanctionDate);
//				
////				String formaDate = getCurrentAPIDate(ifr);
//				
//				
//				formaDate = targetFormat.format(date);
//
//				Date date1 = targetFormat.parse(formaDate);
//
//				Calendar cal = Calendar.getInstance();
//				cal.setTime(date1);
//				cal.add(Calendar.DAY_OF_MONTH, 90);
//
//				String newDateStr = targetFormat.format(cal.getTime());

			String formaDate = "";
			String newDateStr = "";
			String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loansummary " + "WHERE WINAME='"
					+ processInstanceId + "' AND ROWNUM=1";
			Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);

			List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");

			if (!ResultSanctionDate.isEmpty()) {
				sanctionDate = ResultSanctionDate.get(0).get(0);
				Log.consoleLog(ifr, "sanctionDate==>" + sanctionDate);

				try {
					SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					Date parsedDate = originalFormat.parse(sanctionDate);

					// Format to yyyyMMdd
					SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					formaDate = targetFormat.format(parsedDate); // Original date in yyyyMMdd

					// Add 90 days
					Calendar cal = Calendar.getInstance();
					cal.setTime(parsedDate);
					cal.add(Calendar.DAY_OF_MONTH, 90);

					// Format +90 days to yyyyMMdd
					newDateStr = targetFormat.format(cal.getTime());

					Log.consoleLog(ifr, "formaDate (original): " + formaDate);
					Log.consoleLog(ifr, "newDateStr (+90 days): " + newDateStr);
				} catch (Exception e) {
					Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
				}

			} else {
				return "error,No sanction date found";
			}

			String queryForRefrenceNumber = "select APPLICATION_NO from LOS_WIREFERENCE_TABLE " + "where WINAME ='"
					+ processInstanceId + "'";
			List<List<String>> listqueryForRefrenceNumber = ifr.getDataFromDB(queryForRefrenceNumber);
			Log.consoleLog(ifr, "queryForRefrenceNumber==> " + queryForRefrenceNumber);
			if (!listqueryForRefrenceNumber.isEmpty()) {
				appNumber = listqueryForRefrenceNumber.get(0).get(0);

			}
			String queryLendableMargin = "select COLLATERALID from SLOS_COLLATERAL_DETAILS " + "where WINAME ='"
					+ processInstanceId + "'";
			List<List<String>> listqueryLendableMargin = ifr.getDataFromDB(queryLendableMargin);
			Log.consoleLog(ifr, "listqueryLendableMargin==> " + listqueryLendableMargin);
			if (!listqueryLendableMargin.isEmpty()) {
				collId = listqueryLendableMargin.get(0).get(0);

			}

			if (vehicleCat.equalsIgnoreCase("NEW")) {
				String queryUsedNew = "select TOTAL_COST from SLOS_STAFF_COLLATERAL " + "where WINAME ='"
						+ processInstanceId + "'";
				List<List<String>> listqueryUsedNew = ifr.getDataFromDB(queryUsedNew);
				Log.consoleLog(ifr, "listqueryUsedNew==> " + listqueryUsedNew);
				if (!listqueryUsedNew.isEmpty()) {
					totalCost = listqueryUsedNew.get(0).get(0);
				}
			} else {
				String queryUsedNew = "select TOTAL_COST_USED from SLOS_STAFF_COLLATERAL " + "where WINAME ='"
						+ processInstanceId + "'";
				List<List<String>> listqueryUsedNew = ifr.getDataFromDB(queryUsedNew);
				Log.consoleLog(ifr, "listqueryUsedNew==> " + listqueryUsedNew);
				if (!listqueryUsedNew.isEmpty()) {
					totalCost = listqueryUsedNew.get(0).get(0);
				}
			}
			String SancAuthority = pcm.getSancAuhority(ifr, purpoLoan);
			String queryLendableMar = "select FLT_MARGIN from BA_PROD_COLL_XREF " + "where COD_PROD ='" + productCode
					+ "'";
			List<List<String>> listquerycollateralCode = ifr.getDataFromDB(queryLendableMar);
			Log.consoleLog(ifr, "listquerycollateralCode==> " + listquerycollateralCode);
			if (!listquerycollateralCode.isEmpty()) {
				fltMargin = listquerycollateralCode.get(0).get(0);
				Double marginCostDouble = (Double.parseDouble(totalCost) * Double.parseDouble(fltMargin)) / 100;
				marginCost = String.format("%.2f", marginCostDouble);
			}

			String queryBaselCode = "select COD_BASEL_CODE from BA_PROD_BASEL_CUST_TYPE where cod_prod='" + productCode
					+ "'";
			List<List<String>> listqueryBaselCode = ifr.getDataFromDB(queryBaselCode);
			Log.consoleLog(ifr, "queryBaselCode==> " + queryBaselCode);
			if (!listqueryBaselCode.isEmpty()) {
				baselCode = listqueryBaselCode.get(0).get(0);
			}

			request = "{\n" + "  \"input\": {\n" + "    \"SessionContext\": {\n" + "      \"SupervisorContext\": {\n"
					+ "        \"PrimaryPassword\": \"\",\n" + "        \"UserId\": \"" + Sc_UserId + "\"\n"
					+ "      },\n" + "      \"BankCode\": \"" + BankCode + "\",\n" + "      \"Channel\": \"" + Channel
					+ "\",\n" + "      \"ExternalBatchNumber\": \"\",\n" + "      \"ExternalReferenceNo\": \"\",\n"
					+ "      \"ExternalSystemAuditTrailNumber\": \"\",\n" + "      \"LocalDateTimeText\": \"\",\n"
					+ "      \"OriginalReferenceNo\": \"\",\n" + "      \"OverridenWarnings\": \"\",\n"
					+ "      \"PostingDateText\": \"\",\n" + "      \"ServiceCode\": \"\",\n"
					+ "      \"SessionTicket\": \"\",\n" + "      \"TransactionBranch\": \"" + TBranch + "\",\n"
					+ "      \"UserId\": \"" + UserId + "\",\n" + "      \"ValueDateText\": \"\"\n" + "    },\n"
					+ "    \"ExtUniqueRefId\": \"" + formattedDate + "\",\n" + "    \"BranchCode\": \"" + TBranch
					+ "\",\n" + "    \"ProductCode\": \"" + productCode + "\",\n" + "    \"CustomerID\": \""
					+ CustomerId + "\",\n" + "    \"CustRel\": \"SOW\",\n" + "    \"CustomerIDJoint\": \"\",\n"
					+ "    \"CustJointRel\": \"\",\n" + "    \"LoanAmount\": \"" + loanAmount + "\",\n"
					+ "    \"LoanTerm\": \"" + Tenure + "\",\n" + "    \"ScheduleCode\": \"" + scheduleCode + "\",\n"
					+ "    \"InterestVariance\": \"0\",\n" + "    \"Purpose\": \"" + purposeofLoan + "\",\n"
					+ "    \"TakeOverLoan\": \"N\",\n" + "    \"BankFIName\": \"\",\n" + "    \"DatOfSanction\": \""
					+ formaDate + "\",\n" + "    \"SancAuthority\": \"" + SancAuthority + "\",\n" // To be provided
					+ "    \"SancReference\": \"" + appNumber + "\",\n" + "    \"DatOfLoanPapers\": \"" + formaDate
					+ "\",\n" + "    \"PropertyLocation\": \"\",\n" + "    \"PlaceOfInstitute\": \"\",\n"
					+ "    \"GCCLoanPurpose\": \"\",\n" + "    \"AgriLoanPurpose\": \"\",\n"
					+ "    \"ConcessionRate\": \"\",\n" + "    \"DatOfConcessionRate\": \"\",\n"
					+ "    \"ConcessionRatePermitBy\": \"\",\n" + "    \"StatusOfProcessingCharges\": \"N\",\n"
					+ "    \"AmtOfProcessingCharges\": \"\",\n" + "    \"DatProcessingCharges\": \"\",\n"
					+ "    \"DatSanctionExpiry\": \"" + newDateStr + "\",\n" + "    \"UCC\": \"\",\n"
					+ "    \"UCCRemarks\": \"\",\n" + "    \"EduLnPurpose\": \"\",\n" + "    \"EduCourseTyp\": \"\",\n"
					+ "    \"EduQuota\": \"\",\n" + "    \"EduInstitutionCat\": \"\",\n"
					+ "    \"EduCourseStream\": \"\",\n" + "    \"EduEligSubsidy\": \"\",\n"
					+ "    \"EduCoursePrdMonth\": \"\",\n" + "    \"EduDatCompletion\": \"\",\n"
					+ "    \"EduRepayHoliday\": \"\",\n" + "    \"EduCertificateUpto\": \"\",\n"
					+ "    \"EduNamCourse\": \"\",\n" + "    \"EduNamInstitute\": \"\",\n"
					+ "    \"EduPlaceInstitute\": \"\",\n" + "    \"ParentIncomeYear\": \"\",\n"
					+ "    \"ParentIncomeAmt\": \"\",\n" + "    \"IncomeCertIssuedBy\": \"\",\n"
					+ "    \"IncomeCertNumber\": \"\",\n" + "    \"DocSubmitted\": \"\",\n"
					+ "    \"DatMortgage\": \"\",\n" + "    \"BranchBehalf\": \"" + TBranch + "\",\n"
					+ "    \"LSRRefNo\": \"\",\n" + "    \"DatLEDTD\": \"\",\n" + "    \"NotifiedBranches\": \""
					+ TBranch + "\",\n" + "    \"Observations\": \"\",\n" + "    \"HousingFinanceAgency\": \"\",\n"
					+ "    \"POR21blockname\": \"\",\n" + "    \"SectorSensitive\": \"\",\n"
					+ "    \"CollateralValuer\": \"\",\n" + "    \"CollValuationDat\": \"\",\n"
					+ "    \"CollvaluedDetails\": \"\",\n" + "    \"SecutiyValueShortfall\": \"\",\n"
					+ "    \"CollValuationNextDat\": \"\",\n" + "    \"RemarksOval\": \"\",\n"
					+ "    \"CRMBSRDistrictCode\": \"\",\n" + "    \"CRMBSR3CreditCode\": \"\",\n"
					+ "    \"CRMBSR3CreditValue\": \"\",\n" + "    \"CRMSubsidyCode\": \"\",\n"
					+ "    \"BorrowerID\": \"\",\n" + "    \"BorrowerOffice\": \"\",\n"
					+ "    \"BorrowerDUNSNum\": \"\",\n" + "    \"BorrowersLegalConstitution\": \"\",\n"
					+ "    \"Relationship\": \"\",\n" + "    \"LoanCategory\": \"\",\n" + "    \"Handicapped\": \"\",\n"
					+ "    \"BSRAct1\": \"\",\n" + "    \"BSRAct2\": \"\",\n" + "    \"BSRAct3\": \"\",\n"
					+ "    \"AuditDate\": \"\",\n" + "    \"RemarksStockAudit\": \"\",\n"
					+ "    \"AuditNextDate\": \"\",\n" + "    \"AuditConductedBy\": \"\",\n"
					+ "    \"StockValue\": \"\",\n" + "    \"MTRDueDate\": \"\",\n"
					+ "    \"MTRConductedDate\": \"\",\n" + "    \"MTRNextReviewDate\": \"\",\n"
					+ "    \"MTRRemarks\": \"\",\n" + "    \"AdvanceMode\": \"\",\n" + "    \"AdvanceNature\": \"\",\n"
					+ "    \"SSIFlash\": \"\",\n" + "    \"SSISubSector\": \"\",\n" + "    \"GuaranteeType\": \"\",\n"
					+ "    \"SchemeType\": \"\",\n" + "    \"GroupType\": \"\",\n" + "    \"MemberCount\": \"\",\n"
					+ "    \"WomenCount\": \"\",\n" + "    \"MenCount\": \"\",\n" + "    \"MemberName1\": \"\",\n"
					+ "    \"MemberName2\": \"\",\n" + "    \"MemberName3\": \"\",\n" + "    \"MemberName4\": \"\",\n"
					+ "    \"MemberName5\": \"\",\n" + "    \"MemberName6\": \"\",\n" + "    \"MemberName7\": \"\",\n"
					+ "    \"MemberName8\": \"\",\n" + "    \"MemberName9\": \"\",\n" + "    \"MemberName10\": \"\",\n"
					+ "    \"MemberName11\": \"\",\n" + "    \"MemberName12\": \"\",\n"
					+ "    \"MemberName13\": \"\",\n" + "    \"MemberName14\": \"\",\n"
					+ "    \"MemberName15\": \"\",\n" + "    \"MemberName16\": \"\",\n"
					+ "    \"MemberName17\": \"\",\n" + "    \"MemberName18\": \"\",\n"
					+ "    \"MemberName19\": \"\",\n" + "    \"MemberName20\": \"\",\n" + "    \"FinanceBy\": \"\",\n"
					+ "    \"NGOName\": \"\",\n" + "    \"AnchorNGO\": \"\",\n" + "    \"GovSponsoredLoan\": \"\",\n"
					+ "    \"NameGovernment\": \"\",\n" + "    \"SchemeName\": \"\",\n" + "    \"CodMisTxn1\": \""
					+ CodMisTxn1 + "\",\n" + "    \"CodMisTxn2\": \"" + CodMisTxn2 + "\",\n" + "    \"CodMisTxn3\": \""
					+ CodMisTxn3 + "\",\n" + "    \"CodMisTxn4\": \"" + CodMisTxn4 + "\",\n" + "    \"CodMisTxn5\": \""
					+ CodMisTxn5 + "\",\n" + "    \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n" + "    \"CodMisTxn7\": \""
					+ CodMisTxn7 + "\",\n" + "    \"CodMisTxn8\": \"" + CodMisTxn8 + "\",\n" + "    \"CodMisTxn9\": \""
					+ CodMisTxn9 + "\",\n" + "    \"CodMisTxn10\": \"" + CodMisTxn10 + "\",\n"
					+ "    \"CodMisTxn11\": \"" + CodMisTxn11 + "\",\n" + "    \"CodMisComp1\": \"NA\",\n"
					+ "    \"CodMisComp2\": \"\",\n" + "    \"CodMisComp3\": \"\",\n" + "    \"CodMisComp4\": \"\",\n"
					+ "    \"CodMisComp5\": \"\",\n" + "    \"CodMisComp6\": \"\",\n" + "    \"CodMisComp7\": \"\",\n"
					+ "    \"CodMisComp8\": \"\",\n" + "    \"CodMisComp9\": \"\",\n" + "    \"CodMisComp10\": \"\",\n"
					+ "    \"InvInPlantMachinery\": \"\",\n" + "    \"InvInEquipment\": \"\",\n"
					+ "    \"MSMECategory\": \"\",\n" + "    \"TypeOfIndustry\": \"\",\n"
					+ "    \"TypeOfFinance\": \"\",\n" + "    \"Num2\": \"\",\n" + "    \"Num8\": \"\",\n"
					+ "    \"IntSubvFlag\": \"N\",\n" + "    \"CropCultAmt\": \"\",\n" + "    \"AlliedActAmt\": \"\",\n"
					+ "    \"FarmMachAmt\": \"\",\n" + "    \"NonFarmSectAmt\": \"\",\n"
					+ "    \"ConsumPurpAmt\": \"\",\n" + "    \"Security_id1\": \"" + collId + "\",\n"
					+ "    \"AmtMargin\": \"\",\n" + "    \"Lendable_margin1\": \"" + fltMargin + "\",\n"
					+ "    \"Lendable_amount1\": \"" + marginCost + "\",\n" + "    \"CollateralCode1\": \"2\",\n"
					+ "    \"SwitchDueDate\": \"18000101\",\n" + "    \"CropLoan\": \"\",\n"
					+ "    \"CropType\": \"\",\n" + "    \"Season\": \"\",\n" + "    \"ACPlanCode\": \"\",\n"
					+ "    \"ReviewPeriod\": \"0\",\n" + "    \"ReviewDate\": \"\",\n" + "    \"CasaAcctNo\": \""
					+ salaryAccNum + "\",\n" + "    \"AreaOfFarm\": \"\",\n" + "    \"FarmerCategory\": \"\",\n"
					+ "    \"FarmerSubCategory\": \"\",\n" + "    \"HousingLoanPurpose\": \"\",\n"
					+ "    \"ProjectCost\": \"" + totalCost + "\",\n" + "    \"AgriInfraUnit\": \"\",\n"
					+ "    \"Activities\": \"\",\n" + "    \"OtherBankLimit\": \"\",\n" + "    \"Subcategory\": \"\",\n"
					+ "    \"CenterPopulation\": \"\",\n" + "    \"NoOfDependents\": \"\",\n"
					+ "    \"PMFBYApplicable\": \"N\",\n" + "    \"SubsidyAvailable\": \"N\",\n"
					+ "    \"DrawdownRequired\": \"Y\",\n" + "    \"GroupFormationDate\": \"\",\n"
					+ "    \"CustomerID1\": \"\",\n" + "    \"Designation1\": \"\",\n"
					+ "    \"MemberStatus1\": \"\",\n" + "    \"CustomerID2\": \"\",\n"
					+ "    \"Designation2\": \"\",\n" + "    \"MemberStatus2\": \"\",\n"
					+ "    \"CustomerID3\": \"\",\n" + "    \"Designation3\": \"\",\n"
					+ "    \"MemberStatus3\": \"\",\n" + "    \"CustomerID4\": \"\",\n"
					+ "    \"Designation4\": \"\",\n" + "    \"MemberStatus4\": \"\",\n"
					+ "    \"CustomerID5\": \"\",\n" + "    \"Designation5\": \"\",\n"
					+ "    \"MemberStatus5\": \"\",\n" + "    \"CustomerID6\": \"\",\n"
					+ "    \"Designation6\": \"\",\n" + "    \"MemberStatus6\": \"\",\n"
					+ "    \"CustomerID7\": \"\",\n" + "    \"Designation7\": \"\",\n"
					+ "    \"MemberStatus7\": \"\",\n" + "    \"CustomerID8\": \"\",\n"
					+ "    \"Designation8\": \"\",\n" + "    \"MemberStatus8\": \"\",\n"
					+ "    \"CustomerID9\": \"\",\n" + "    \"Designation9\": \"\",\n"
					+ "    \"MemberStatus9\": \"\",\n" + "    \"CustomerID10\": \"\",\n"
					+ "    \"Designation10\": \"\",\n" + "    \"MemberStatus10\": \"\",\n"
					+ "    \"CustomerID11\": \"\",\n" + "    \"Designation11\": \"\",\n"
					+ "    \"MemberStatus11\": \"\",\n" + "    \"CustomerID12\": \"\",\n"
					+ "    \"Designation12\": \"\",\n" + "    \"MemberStatus12\": \"\",\n"
					+ "    \"CustomerID13\": \"\",\n" + "    \"Designation13\": \"\",\n"
					+ "    \"MemberStatus13\": \"\",\n" + "    \"CustomerID14\": \"\",\n"
					+ "    \"Designation14\": \"\",\n" + "    \"MemberStatus14\": \"\",\n"
					+ "    \"CustomerID15\": \"\",\n" + "    \"Designation15\": \"\",\n"
					+ "    \"MemberStatus15\": \"\",\n" + "    \"CustomerID16\": \"\",\n"
					+ "    \"Designation16\": \"\",\n" + "    \"MemberStatus16\": \"\",\n"
					+ "    \"CustomerID17\": \"\",\n" + "    \"Designation17\": \"\",\n"
					+ "    \"MemberStatus17\": \"\",\n" + "    \"CustomerID18\": \"\",\n"
					+ "    \"Designation18\": \"\",\n" + "    \"MemberStatus18\": \"\",\n"
					+ "    \"CustomerID19\": \"\",\n" + "    \"Designation19\": \"\",\n"
					+ "    \"MemberStatus19\": \"\",\n" + "    \"CustomerID20\": \"\",\n"
					+ "    \"Designation20\": \"\",\n" + "    \"MemberStatus20\": \"\",\n"
					+ "    \"ReviewAccount\": \"N\",\n" + "    \"RemarksProcessingCharges\": \"\",\n"
					+ "    \"CentreLeaderIndicator\": \"\",\n" + "    \"ShpiNgoName\": \"\",\n"
					+ "    \"ShpiNgoIdentifier\": \"\",\n" + "    \"ShpiNgoOfficerName\": \"\",\n"
					+ "    \"ShpiNgoAddress\": \"\",\n" + "    \"NoOfMeetingsHeld\": \"\",\n"
					+ "    \"NoOfMeetingsMissed\": \"\",\n" + "    \"AgreedMeetingDayOfTheWeek\": \"\",\n"
					+ "    \"AgreedMeetingTimeOfTheDay\": \"\",\n" + "    \"InsuranceIndicator\": \"\",\n"
					+ "    \"TypeOfInsurance\": \"\",\n" + "    \"SumAssuredCoverage\": \"\",\n"
					+ "    \"LoanCategoryGrp\": \"\",\n" + "    \"CropCultivationAmountKhrif\": \"\",\n"
					+ "    \"CropCultivationAmountRabi\": \"\",\n" + "    \"CropCultivationAmountSumer\": \"\",\n"
					+ "    \"CropInsurancePremium\": \"\",\n" + "    \"LinkTDRDForInterest\": \"N\",\n"
					+ "    \"FlgNomAppl\": \"\",\n" + "    \"concessionPermitted\": \"N\",\n"
					+ "    \"BankArr\": \"1\",\n" + "    \"BankFiTyp\": \"\",\n" + "    \"PrimarySecondary1\": \"P\",\n"
					+ "    \"ExposureRefNum\": \"\",\n" + "    \"AppriaserReg\": \"\",\n"
					+ "    \"RatCrRiskPrem\": \"\",\n" + "    \"RatLiquidityPrem\": \"\",\n"
					+ "    \"RatEcaiUnratedPrem\": \"\",\n" + "    \"RatRiskGradeConcess\": \"\",\n"
					+ "    \"ConcessEndDate\": \"\",\n" + "    \"BaselCustType\": \"" + baselCode + "\"\n" + "  }\n"
					+ "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String LoanAccountCreationResponseResponse = bodyObj.get("LoanAccountCreationResponse").toString();
					// Log.consoleLog(ifr, "Response==>" + Response);
					System.out.println("LoanAccountCreationResponseResponse=>" + LoanAccountCreationResponseResponse);

					JSONObject LoanAccountCreationResponseResponseJSON = (JSONObject) parser
							.parse(LoanAccountCreationResponseResponse);
					JSONObject LoanAccountCreationResponseResponseObj = new JSONObject(
							LoanAccountCreationResponseResponseJSON);

					LoanAccNumber = LoanAccountCreationResponseResponseObj.get("accountNo").toString();
					String sanctionAmount = LoanAccountCreationResponseResponseObj.get("sanctionAmount").toString();

					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					Log.consoleLog(ifr, "sanctionAmount==>" + sanctionAmount);

					Log.consoleLog(ifr, "AccountNumber==>" + LoanAccNumber);

					java.util.Date dateTimeFormat = new java.util.Date();
					SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
					String strCurDateTime = sDateTime.format(dateTimeFormat);

					String selectedBranchCode = "";

					String querySelectedBranchCode = "select BRANCH_CODE from SLOS_STAFF_TRN  where WINAME='"
							+ processInstanceId + "' ";
					Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
					List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
					if (!branchCodeResult.isEmpty()) {
						selectedBranchCode = branchCodeResult.get(0).get(0);
					}

					String Query1 = "";
					Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET " + "LOAN_ACCOUNTNO='" + LoanAccNumber.trim() + "', "
							+ "ACCOUNT_CREATEDDATE='" + strCurDateTime.trim() + "', " + "SANCTION_AMOUNT='" + sanctionAmount
							+ "', " + "DISB_AMOUNT='" + loanAmount + "', " + "DISB_BRANCH='" + selectedBranchCode + "' "
							+ "WHERE PID='" + processInstanceId + "' "
							+ "AND (LOAN_ACCOUNTNO IS NULL OR LOAN_ACCOUNTNO = '')";

					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
					ifr.saveDataInDB(Query1);
					Log.consoleLog(ifr, "Query1=>" + Query1);

//					String Query1 = "INSERT INTO SLOS_TRN_LOANDETAILS (PID, APPLICATION_REFNO, LOAN_ACCOUNTNO, ACCOUNT_CREATEDDATE, SANCTION_AMOUNT, DISB_BRANCH) "
//							+ "SELECT '" + processInstanceId + "', '', '"+LoanAccNumber+"', '"+strCurDateTime+"', '"+sanctionAmount+"', '' " + "FROM dual " + "WHERE NOT EXISTS ("
//							+ "    SELECT 1 FROM SLOS_TRN_LOANDETAILS WHERE PID = '"
//							+ processInstanceId + "'" + ")";
//					Log.consoleLog(ifr,
//							"LoanAccountCreation:getLoanAccountDetails -> Else Query1===>"
//									+ Query1);
//					cf.mExecuteQuery(ifr, Query1, "INSERT LOANACCOUNT disbur details ");

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
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			}

//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String getCurrentAPIDate(IFormReference ifr) {
		Date d = new Date();
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		String APIDate = sd.format(d);
		Log.consoleLog(ifr, "APIDate:" + APIDate);
		return APIDate;
	}

	public String createLADODLoanAccount(IFormReference ifr, String ProcessInstanceId, String ProductCode,
			String CustomerId, String LoanAmount, String TDAccountNo, String SanctionExpiryDate,
			String linkedCasaAccountNo, String depositNo, String linkedCasaAccountNoBranch) {
		Log.consoleLog(ifr, "#getLADODLoanAccountDetails=================");
//        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String LoanAccNumber = "";
//        String APIName = "CBS_ODADLimitCreation";
//        String ErrorCode = "";
//        String ErrorMessage = "";
//
//        String APIStatus = "";
//        String strProductCode = "";

		String apiName = "ODADLoanAccCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String strProductCode = "";
		String loanAccNumber = "";

		try {
			String laonDetails = ConfProperty.getQueryScript("LADOPENLOANACCDETAILS").replaceAll("#winame#",
					processInstanceId);

			Log.consoleLog(ifr, "Data laonDetails for table  :" + laonDetails);

			List<List<String>> list1 = ifr.getDataFromDB(laonDetails);

			String loanselected = "", loan_purpose = "";

			if (!list1.isEmpty()) {

				loanselected = list1.get(0).get(0);

				loan_purpose = list1.get(0).get(1);

				Log.consoleLog(ifr, "loanselected :" + loanselected);

				Log.consoleLog(ifr, "loan_purpose :" + loan_purpose);

			}

			if (loanselected.equalsIgnoreCase("OD") && (loan_purpose.equalsIgnoreCase("RET"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADODP", "PRODUCTCODE");
			}
			if (loanselected.equalsIgnoreCase("OD") && (loan_purpose.equalsIgnoreCase("AGRI"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADODA", "PRODUCTCODE");
			}
			if (loanselected.equalsIgnoreCase("OD") && (loan_purpose.equalsIgnoreCase("BUS"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADODB", "PRODUCTCODE");
			}

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
			// ODAD
			// String strProductCode = pcm.getConstantValue(ifr, "CBSODADLOANACC",
			// "PRODUCTCODE");
			String BankCode = pcm.getConstantValue(ifr, "CBSODADLOANACC", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSODADLOANACC", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSODADLOANACC", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSODADLOANACC", "USERID");
			// String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "ODAD");
			String limitRenewalMode = "N";
			String query = ConfProperty.getQueryScript("PORTALLADRENEWALSELECT").replaceAll("#WI_NAME#",
					ProcessInstanceId);
			List<List<String>> result = cf.mExecuteQuery(ifr, query, "PORTALLADRENEWALSELECT");
			if (result.size() > 0) {
				if (!(result.get(0).get(0).equalsIgnoreCase(""))) {
					limitRenewalMode = result.get(0).get(0);
				}
			}
			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\":\"" + S_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + linkedCasaAccountNoBranch + "\",\n"
					+ "            \"UserId\": \"" + UserId + "\",\n" + "            \"UserReferenceNumber\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "            \"ValueDateText\": \"\"\n"
					+ "        },\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"ProductCode\": \"" + strProductCode + "\",\n" + "        \"AmtLimit\": \"" + LoanAmount
					+ "\",\n" + "        \"LimitDateEnd\": \"" + SanctionExpiryDate + "\",\n"
					+ "        \"TdAccountNo\": \"" + TDAccountNo + "\",\n" + "        \"TdDepNo\": \"" + depositNo
					+ "\",\n" + "        \"oDLimitRenewalMode\": \"" + limitRenewalMode + "\",\n"
					+ "        \"linkedCasaAccountNo\": \"" + linkedCasaAccountNo.replaceAll("^|\\s+", "") + "\"\n"
					+ "    }\n" + "}\n" + "";
//            HashMap requestHeader = new HashMap<>();
//            String Response = "";
//            if (ConfProperty.getIntegrationValue("CBSMOCKODADLIMITFLAG").equalsIgnoreCase("Y")) {
//                Response = "{  \"body\": {    \"CreateOdAgainstTD\": {      \"Status\": {        \"ExtendedReply\": {          \"MessagesArray\": {}        },        \"ErrorCode\": \"0\",        \"ExternalReferenceNo\": \"8221521209565\",        \"IsOverriden\": \"false\",        \"IsServiceChargeApplied\": \"false\",        \"Memo\": {},        \"ReplyCode\": \"0\",        \"ReplyText\": {},        \"SpReturnValue\": \"0\"      },      \"AccountNo\": \"127001084645\",      \"LimitNo\": \"1\",      \"AmtDrawingPower\": \"50000\",      \"ExtUniqueRefId\": {}    }  },  \"responseCode\": 200}";
//                //Response = "{\"body\":{\"Response\":{\"CreateOdAgainstTD\":{\"Status\":{\"ExtendedReply\":{\"MessagesArray\":\"\"},\"ErrorCode\":0,\"ExternalReferenceNo\":\"005c6603-35bf-4ca3-9241-5d8123f9b33f\",\"IsOverriden\":false,\"IsServiceChargeApplied\":false,\"Memo\":\"\",\"ReplyCode\":0,\"ReplyText\":\"\",\"SpReturnValue\":0},\"AccountNo\":127000781425,\"LimitNo\":1,\"AmtDrawingPower\":13333,\"ExtUniqueRefId\":\"\"}}},\"responseCode\":200}";
//            } else {
//                Response = cf.CallWebService(ifr, "ODADLoanAccCreation", Request, "", requestHeader);
//            }

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			Log.consoleLog(ifr, "Response==>" + response);
			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyJSON);
				if (CheckError.equalsIgnoreCase("true")) {
					JSONObject CreateOdAgainstTD = (JSONObject) bodyJSON.get("CreateOdAgainstTD");
					loanAccNumber = CreateOdAgainstTD.get("AccountNo").toString();
					Log.consoleLog(ifr, "LoanAccNumber:" + loanAccNumber);
					updateLoanDetails(ifr, ProcessInstanceId, loanAccNumber);
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
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return loanAccNumber;
			}

			// Commented by Ahmed on 12-07-2023
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
//            if (APIStatus.equalsIgnoreCase("SUCCESS")) {
//                return LoanAccNumber;
//            } else {
//                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, APIName, ErrorCode);
//            }
//            
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
			Log.errorLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public String createLADTLLoanAccount(IFormReference ifr, String ProcessInstanceId, String ProductCode,
			String CustomerId, String LoanAmount, String Tenure, String CASAAccountNumber, String TDaccount,
			String depositNo, String linkedCasaAccountNoBranch) {
		Log.consoleLog(ifr, "#Execute_Open Loan account =================");
		String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

//        String APIName = "CBS_OPENLoanAccountCreation";
//        String ErrorCode = "";
//        String ErrorMessage = "";
//        String APIStatus = "";
		String apiName = "TDLoanAccCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		String LoanAccNumber = "";
		String strProductCode = "";
		String ScheduleCode = "";

		try {

			String laonDetails = ConfProperty.getQueryScript("LADOPENLOANACCDETAILS").replaceAll("#winame#",
					ProcessInsanceId);

			Log.consoleLog(ifr, "Data laonDetails for table  :" + laonDetails);

			List<List<String>> list1 = ifr.getDataFromDB(laonDetails);

			String loanselected = "", loan_purpose = "";

			if (!list1.isEmpty()) {

				loanselected = list1.get(0).get(0);

				loan_purpose = list1.get(0).get(1);

				Log.consoleLog(ifr, "loanselected :" + loanselected);

				Log.consoleLog(ifr, "loan_purpose :" + loan_purpose);

			}

			if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("RET"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLP", "PRODUCTCODE");

				ScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLP", "SCODE");

			}
			if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("AGRI"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLA", "PRODUCTCODE");

				ScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLA", "SCODE");

			}
			if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("BUS"))) {

				strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLB", "PRODUCTCODE");

				ScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLB", "SCODE");

			}

//            Date currentDate = new Date();
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//
//            String formattedDate = dateFormat.format(currentDate);

			// String strProductCode = pcm.getConstantValue(ifr, "CBSLADOANACC",
			// "PRODUCTCODE");
			String BankCode = pcm.getConstantValue(ifr, "CBSLADOANACC", "BANKCODE");

			String Channel = pcm.getConstantValue(ifr, "CBSLADOANACC", "CHANNEL");

			String S_UserId = pcm.getConstantValue(ifr, "CBSLADOANACC", "SC_USERID");

			String UserId = pcm.getConstantValue(ifr, "CBSLADOANACC", "USERID");

			// String ScheduleCode = pcm.getConstantValue(ifr, "CBSLADOANACC", "SCODE");
			// String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "LAD");
			String lendableMarginPercentage = pcm.getConstantValue(ifr, "CBSLADOANACC", "LENDABLEMARGINPERCENTAGE");

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + S_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "         \"ExternalReferenceNo\": \"\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "             \"TransactionBranch\": \"" + linkedCasaAccountNoBranch + "\",\n"
					+ "             \"UserId\": \"" + UserId + "\",\n" + "         \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "          \"CustomerID\": \""
					+ CustomerId + "\",\n" + "       \"LoanAmount\": \"" + LoanAmount + "\",\n"
					+ "    \"TDAccountNumber\": \"" + TDaccount + "\",\n" + "     \"DepositNumber\": \"" + depositNo
					+ "\",\n" + "      \"CASAAccountNumber\": \"" + CASAAccountNumber.replaceAll("^|\\s+", "") + "\",\n"
					+ "    \"LendableMarginPercentage\": \"" + lendableMarginPercentage + "\",\n"
					+ "     \"LoanProductCode\": \"" + strProductCode + "\",\n" + "       \"LoanScheduleCode\": \""
					+ ScheduleCode + "\",\n" + "       \"LoanTenureMonths\": \"" + Tenure + "\",\n"
					+ "        \"FCDBReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\"\n" + "    }\n"
					+ "}\n" + "";

			Log.consoleLog(ifr, "Request for TL ==>" + request);
//
//Commented by Ahmed on 12-07-2023 for making a generic structure for APi`s
//            HashMap requestHeader = new HashMap<>();//
//            String Response = cf.CallWebService(ifr, "TDLoanAccCreation", Request, "", requestHeader);
////            Log.consoleLog(ifr, "Response==>" + Response);

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);
				String body = resultObj.get("body").toString();

				Log.consoleLog(ifr, "body :  " + body);
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);

				if (CheckError.equalsIgnoreCase("true")) {
					JSONObject CreateOdAgainstTD = (JSONObject) bodyJSON.get("LoanAcctCreationResponse");
					LoanAccNumber = CreateOdAgainstTD.get("LoanAccountNumber").toString();
					Log.consoleLog(ifr, "LoanAccNumber:" + LoanAccNumber);
					updateLoanDetails(ifr, ProcessInstanceId, LoanAccNumber);
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
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return LoanAccNumber;
			}

//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//            } else {
//                APIStatus = "FAIL";
//            }
//
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//
//            if (APIStatus.equalsIgnoreCase("SUCCESS")) {
//                return LoanAccNumber;
//            }
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  OpensLoanAccCreateAPI" + e.getMessage());
			Log.errorLog(ifr, "Exception in  OpensLoanAccCreateAPI" + e.getMessage());

		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		// Modified by Ahmed on 11-07-2024 for displaying the actual error message
		// without data massaging
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, apiErrorCode);

	}

	public void updateLoanDetails(IFormReference ifr, String PID, String loanAccountno) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedDate = currentDate.format(formatter);
		String SancReference_Qry = "SELECT APPLICATIONNUMBER FROM " + "LOS_EXT_TABLE WHERE ITEMINDEX IN "
				+ "(SELECT VAR_REC_1 FROM WFINSTRUMENTTABLE WHERE PROCESSINSTANCEID='" + PID + "')";
		Log.consoleLog(ifr, "SancReference_Qry==>" + SancReference_Qry);
		List SancResult = ifr.getDataFromDB(SancReference_Qry);
		String applicationRef = SancResult.toString().replace("[", "").replace("]", "");
		String loanAmout = null;
		String emi = null;
		String sbAccount = null;
		String rpAccount = null;
		String finalTrnData = ConfProperty.getQueryScript("POATALLADLOANDETAILS").replaceAll("#WINAME#", PID);
		List<List<String>> finalTrn = cf.mExecuteQuery(ifr, finalTrnData, "Query for Disbur insert ");
		if (!finalTrn.isEmpty()) {
			loanAmout = finalTrn.get(0).get(0);
			emi = finalTrn.get(0).get(1);
			sbAccount = finalTrn.get(0).get(2);
			rpAccount = finalTrn.get(0).get(3);
			Log.consoleLog(ifr, "finalTrnData==>" + finalTrn);

			String insertLoanDetails = ConfProperty.getQueryScript("POATALLADLOANDETAILSUPDATE")
					.replaceAll("#WINAME#", PID).replaceAll("#APPLICATION_REFNO#", applicationRef)
					.replaceAll("#LOAN_ACCOUNTNO#", loanAccountno).replaceAll("#ACCOUNT_CREATEDDATE#", formattedDate)
					.replaceAll("#SANCTION_AMOUNT#", loanAmout).replaceAll("#DISB_AMOUNT#", loanAmout)
					.replaceAll("#DISB_DATE#", formattedDate).replaceAll("#EMI_AMOUNT#", emi)
					.replaceAll("#SB_ACCOUNTNO#", sbAccount).replaceAll("#RP_ACCOUNTNO#", rpAccount);
			Log.consoleLog(ifr, "UPdateQuery : " + insertLoanDetails);
			int count = ifr.saveDataInDB(insertLoanDetails);
			Log.consoleLog(ifr, "Inserted successfully  : " + count);
		}
	}

	public String getLoanAccountDetailsForHRMS(IFormReference ifr, String processInstanceId, String productCode,
			String customerId, String loanAmount, String tenure, String sanctionExpiryDate, String journeyType) {

		Log.consoleLog(ifr, "#Execute_CBSLoanAccountCreation=================");

		String apiName = "LoanAccountCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String CodMisTxn1 = "";
		String CodMisTxn2 = "";
		String CodMisTxn3 = "";
		String CodMisTxn4 = "";
		String CodMisTxn5 = "";
		String CodMisTxn6 = "";
		String CodMisTxn7 = "";
		String CodMisTxn8 = "";
		String CodMisTxn9 = "";
		String CodMisTxn10 = "";
		String CodMisTxn11 = "";

		String LoanAccNumber = "";
		try {
			String salAccNum = "";
			String codBaselCode = "";
			String flgMntStatus = "";
			String queryForAccNo = "SELECT SALARY_ACC_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
					+ "'";
			Log.consoleLog(ifr, "Salary Acc Number query===>" + queryForAccNo);
			List<List<String>> res = ifr.getDataFromDB(queryForAccNo);
			Log.consoleLog(ifr, "res===>" + res);
			if (!res.isEmpty()) {
				salAccNum = res.get(0).get(0);
			}

			String queryForBaselCode = "select cod_basel_code, flg_mnt_status from ba_prod_basel_cust_type where cod_prod = '"
					+ productCode + "'";
			Log.consoleLog(ifr, "Salary CODE BASEL CODE query===>" + queryForBaselCode);
			List<List<String>> resultForBaselCode = ifr.getDataFromDB(queryForBaselCode);
			Log.consoleLog(ifr, "resultForBaselCode===>" + resultForBaselCode);
			if (!resultForBaselCode.isEmpty()) {
				flgMntStatus = resultForBaselCode.get(0).get(1);
				if (!flgMntStatus.isEmpty() && flgMntStatus != null && flgMntStatus.equalsIgnoreCase("A")) {
					codBaselCode = resultForBaselCode.get(0).get(0);
				}
			}

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formattedDate = dateFormat.format(currentDate);

			String applicationNumber = pcm.getApplicationRefNumber(ifr);
			Log.consoleLog(ifr, "applicationNumber==>" + applicationNumber);

			Log.consoleLog(ifr, "productCode=======>" + productCode);

			String ScheduleCode = AcceleratorConstants.SCHEDULECODE;
			String SancAuthority = AcceleratorConstants.SANCAUTHORITY;

			Log.consoleLog(ifr, "Loan account creation ScheduleCode====>" + ScheduleCode);
			String BankCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "ACCOUNTACTIVATION", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSLOANDED", "USERID");

			String CustRel = AcceleratorConstants.CUSTREL;
			String InterestVariance = AcceleratorConstants.INTERESTVARIANCE;
			String Purpose = AcceleratorConstants.PURPOSE;
			String StatusOfProcessingCharges = AcceleratorConstants.STATUSOFPROCESSINGCHARGES;
			String SwitchDueDate = AcceleratorConstants.SWITCHDUEDATE;

//			SimpleDateFormat dateFor = new SimpleDateFormat("yyyyMMdd");
//			String SancDate = dateFor.format(currentDate);
			String sanctionDate = "";
			String SancDate = "";

			String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loansummary " + "WHERE WINAME='"
					+ processInstanceId + "' AND ROWNUM=1";
			Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);

			List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");

			if (!ResultSanctionDate.isEmpty()) {
				sanctionDate = ResultSanctionDate.get(0).get(0);
				Log.consoleLog(ifr, "sanctionDate==>" + sanctionDate);

				try {
					SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					Date parsedDate = originalFormat.parse(sanctionDate);

					// Format to yyyyMMdd
					SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					SancDate = targetFormat.format(parsedDate); // Original date in yyyyMMdd
				} catch (Exception e) {
					Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
				}

			} else {
				return "error,No sanction date found";
			}

			String codMixquery = "select * from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where PROD_CODE='" + productCode
					+ "'";
			List<List<String>> codMixqueryList = ifr.getDataFromDB(codMixquery);

			if (codMixqueryList.size() > 0) {
				CodMisTxn1 = codMixqueryList.get(0).get(1);
				CodMisTxn2 = codMixqueryList.get(0).get(2);
				CodMisTxn3 = codMixqueryList.get(0).get(3);
				CodMisTxn4 = codMixqueryList.get(0).get(4);
				CodMisTxn5 = codMixqueryList.get(0).get(5);
				CodMisTxn6 = codMixqueryList.get(0).get(6);
				CodMisTxn7 = codMixqueryList.get(0).get(7);
				CodMisTxn8 = codMixqueryList.get(0).get(8);
				CodMisTxn9 = codMixqueryList.get(0).get(9);
				CodMisTxn10 = codMixqueryList.get(0).get(10);
				CodMisTxn11 = codMixqueryList.get(0).get(11);
			} else {
				return "BAM83 ERROR";
			}

			String tBranch = "";
			String selectedBranchCode = "";
			String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
					+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
			Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
			List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
			if (!result.isEmpty()) {
				tBranch = result.get(0).get(0);
			}

			String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='"
					+ processInstanceId + "' ";
			Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
			List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
			if (!branchCodeResult.isEmpty()) {
				selectedBranchCode = branchCodeResult.get(0).get(0);
			}
			if (branchCodeResult.isEmpty()) {
				selectedBranchCode = tBranch;
			}

			String projectCost = loanAmount;

			Log.consoleLog(ifr, "projectCost==========>" + projectCost);
			// ==========================================================================================//

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
					+ "            \"UserId\": \"" + UserId + "\",\n" + "            \"ValueDateText\": \"\"\n"
					+ "        },\n" + "        \"ExtUniqueRefId\": \"" + formattedDate + "\",\n"
					+ "        \"BranchCode\": \"" + selectedBranchCode + "\",\n" + "        \"ProductCode\": \""
					+ productCode + "\",\n" + "        \"CustomerID\": \"" + customerId + "\",\n"
					+ "        \"CustRel\": \"" + CustRel + "\",\n" + "        \"CustomerIDJoint\": \"\",\n"
					+ "        \"CustJointRel\": \"\",\n" + "        \"LoanAmount\": \"" + loanAmount + "\",\n"
					+ "        \"LoanTerm\": \"" + tenure + "\",\n" + "        \"ScheduleCode\": \"" + ScheduleCode
					+ "\",\n" + "        \"InterestVariance\": \"" + InterestVariance + "\",\n"
					+ "        \"Purpose\": \"" + Purpose + "\",\n" + "        \"TakeOverLoan\": \"N\",\n"
					+ "        \"BankFIName\": \"\",\n" + "        \"DatOfSanction\": \"" + SancDate + "\",\n"
					+ "        \"SancAuthority\": \"" + SancAuthority + "\",\n" + "        \"SancReference\": \""
					+ applicationNumber + "\",\n"// Need to
													// discusse
					+ "        \"DatOfLoanPapers\": \"" + SancDate + "\",\n" + "        \"PropertyLocation\": \"\",\n"
					+ "        \"PlaceOfInstitute\": \"\",\n" + "        \"GCCLoanPurpose\": \"\",\n"
					+ "        \"AgriLoanPurpose\": \"\",\n" + "        \"ConcessionRate\": \"\",\n"
					+ "        \"DatOfConcessionRate\": \"\",\n" + "        \"ConcessionRatePermitBy\": \"\",\n"
					+ "        \"StatusOfProcessingCharges\": \"" + StatusOfProcessingCharges + "\",\n"
					+ "        \"AmtOfProcessingCharges\": \"\",\n" + "        \"DatProcessingCharges\": \"\",\n"
					+ "        \"DatSanctionExpiry\": \"" + sanctionExpiryDate + "\",\n" + "        \"UCC\": \"\",\n"
					+ "        \"UCCRemarks\": \"\",\n" + "        \"EduLnPurpose\": \"\",\n"
					+ "        \"EduCourseTyp\": \"\",\n" + "        \"EduQuota\": \"\",\n"
					+ "        \"EduInstitutionCat\": \"\",\n" + "        \"EduCourseStream\": \"\",\n"
					+ "        \"EduEligSubsidy\": \"\",\n" + "        \"EduCoursePrdMonth\": \"\",\n"
					+ "        \"EduDatCompletion\": \"\",\n" + "        \"EduRepayHoliday\": \"\",\n"
					+ "        \"EduCertificateUpto\": \"\",\n" + "        \"EduNamCourse\": \"\",\n"
					+ "        \"EduNamInstitute\": \"\",\n" + "        \"EduPlaceInstitute\": \"\",\n"
					+ "        \"ParentIncomeYear\": \"\",\n" + "        \"ParentIncomeAmt\": \"\",\n"
					+ "        \"IncomeCertIssuedBy\": \"\",\n" + "        \"IncomeCertNumber\": \"\",\n"
					+ "        \"DocSubmitted\": \"\",\n" + "        \"DatMortgage\": \"\",\n"
					+ "        \"BranchBehalf\": \"" + selectedBranchCode + "\",\n" + "        \"LSRRefNo\": \"\",\n"
					+ "        \"DatLEDTD\": \"\",\n" + "        \"NotifiedBranches\": \"" + selectedBranchCode
					+ "\",\n" + "        \"Observations\": \"\",\n" + "        \"HousingFinanceAgency\": \"\",\n"
					+ "        \"POR21blockname\": \"\",\n" + "        \"SectorSensitive\": \"\",\n"
					+ "        \"CollateralValuer\": \"\",\n" + "        \"CollValuationDat\": \"\",\n"
					+ "        \"CollvaluedDetails\": \"\",\n" + "        \"SecutiyValueShortfall\": \"\",\n"
					+ "        \"CollValuationNextDat\": \"\",\n" + "        \"RemarksOval\": \"\",\n"
					+ "        \"CRMBSRDistrictCode\": \"\",\n" + "        \"CRMBSR3CreditCode\": \"\",\n"
					+ "        \"CRMBSR3CreditValue\": \"\",\n" + "        \"CRMSubsidyCode\": \"\",\n"
					+ "        \"BorrowerID\": \"\",\n" + "        \"BorrowerOffice\": \"\",\n"
					+ "        \"BorrowerDUNSNum\": \"\",\n" + "        \"BorrowersLegalConstitution\": \"\",\n"
					+ "        \"Relationship\": \"\",\n" + "        \"LoanCategory\": \"\",\n"
					+ "        \"Handicapped\": \"\",\n" + "        \"BSRAct1\": \"\",\n"
					+ "        \"BSRAct2\": \"\",\n" + "        \"BSRAct3\": \"\",\n" + "        \"AuditDate\": \"\",\n"
					+ "        \"RemarksStockAudit\": \"\",\n" + "        \"AuditNextDate\": \"\",\n"
					+ "        \"AuditConductedBy\": \"\",\n" + "        \"StockValue\": \"\",\n"
					+ "        \"MTRDueDate\": \"\",\n" + "        \"MTRConductedDate\": \"\",\n"
					+ "        \"MTRNextReviewDate\": \"\",\n" + "        \"MTRRemarks\": \"\",\n"
					+ "        \"AdvanceMode\": \"\",\n" + "        \"AdvanceNature\": \"\",\n"
					+ "        \"SSIFlash\": \"\",\n" + "        \"SSISubSector\": \"\",\n"
					+ "        \"GuaranteeType\": \"\",\n" + "        \"SchemeType\": \"\",\n"
					+ "        \"GroupType\": \"\",\n" + "        \"MemberCount\": \"\",\n"
					+ "        \"WomenCount\": \"\",\n" + "        \"MenCount\": \"\",\n"
					+ "        \"MemberName1\": \"\",\n" + "        \"MemberName2\": \"\",\n"
					+ "        \"MemberName3\": \"\",\n" + "        \"MemberName4\": \"\",\n"
					+ "        \"MemberName5\": \"\",\n" + "        \"MemberName6\": \"\",\n"
					+ "        \"MemberName7\": \"\",\n" + "        \"MemberName8\": \"\",\n"
					+ "        \"MemberName9\": \"\",\n" + "        \"MemberName10\": \"\",\n"
					+ "        \"MemberName11\": \"\",\n" + "        \"MemberName12\": \"\",\n"
					+ "        \"MemberName13\": \"\",\n" + "        \"MemberName14\": \"\",\n"
					+ "        \"MemberName15\": \"\",\n" + "        \"MemberName16\": \"\",\n"
					+ "        \"MemberName17\": \"\",\n" + "        \"MemberName18\": \"\",\n"
					+ "        \"MemberName19\": \"\",\n" + "        \"MemberName20\": \"\",\n"
					+ "        \"FinanceBy\": \"\",\n" + "        \"NGOName\": \"\",\n"
					+ "        \"AnchorNGO\": \"\",\n" + "        \"GovSponsoredLoan\": \"\",\n"
					+ "        \"NameGovernment\": \"\",\n" + "        \"SchemeName\": \"\",\n"
					+ "        \"CodMisTxn1\": \"" + CodMisTxn1 + "\",\n" + "        \"CodMisTxn2\": \"" + CodMisTxn2
					+ "\",\n" + "        \"CodMisTxn3\": \"" + CodMisTxn3 + "\",\n" + "        \"CodMisTxn4\": \""
					+ CodMisTxn4 + "\",\n" + "        \"CodMisTxn5\": \"" + CodMisTxn5 + "\",\n"
					+ "        \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n" + "        \"CodMisTxn7\": \"" + CodMisTxn7
					+ "\",\n" + "        \"CodMisTxn8\": \"" + CodMisTxn8 + "\",\n" + "        \"CodMisTxn9\": \""
					+ CodMisTxn9 + "\",\n" + "        \"CodMisTxn10\": \"" + CodMisTxn10 + "\",\n"
					+ "        \"CodMisTxn11\": \"" + CodMisTxn11 + "\",\n" + "        \"CodMisComp1\": \"NA\",\n"
					+ "        \"CodMisComp2\": \"\",\n" + "        \"CodMisComp3\": \"\",\n"
					+ "        \"CodMisComp4\": \"\",\n" + "        \"CodMisComp5\": \"\",\n"
					+ "        \"CodMisComp6\": \"\",\n" + "        \"CodMisComp7\": \"\",\n"
					+ "        \"CodMisComp8\": \"\",\n" + "        \"CodMisComp9\": \"\",\n"
					+ "        \"CodMisComp10\": \"\",\n" + "        \"InvInPlantMachinery\": \"\",\n"
					+ "        \"InvInEquipment\": \"\",\n" + "        \"MSMECategory\": \"\",\n"
					+ "        \"TypeOfIndustry\": \"\",\n" + "        \"TypeOfFinance\": \"\",\n"
					+ "        \"Num2\": \"\",\n" + "        \"Num8\": \"\",\n" + "        \"IntSubvFlag\": \"N\",\n"
					+ "        \"CropCultAmt\": \"\",\n" + "        \"AlliedActAmt\": \"\",\n"
					+ "        \"FarmMachAmt\": \"\",\n" + "        \"NonFarmSectAmt\": \"\",\n"
					+ "        \"ConsumPurpAmt\": \"\",\n" + "        \"Security_id1\": \"\",\n"
					+ "        \"Lendable_margin1\":  \"0.0\",\n" + "        \"Lendable_amount1\": \"\",\n"
					+ "        \"CollateralCode1\": \"\",\n" + "        \"Security_id2\": \"\",\n"
					+ "        \"Lendable_margin2\": \"\",\n" + "        \"Lendable_amount2\": \"\",\n"
					+ "        \"CollateralCode2\": \"\",\n" + "        \"Security_id3\": \"\",\n"
					+ "        \"Lendable_margin3\": \"\",\n" + "        \"Lendable_amount3\": \"\",\n"
					+ "        \"CollateralCode3\": \"\",\n" + "        \"Security_id4\": \"\",\n"
					+ "        \"Lendable_margin4\": \"\",\n" + "        \"Lendable_amount4\": \"\",\n"
					+ "        \"CollateralCode4\": \"\",\n" + "        \"Security_id5\": \"\",\n"
					+ "        \"Lendable_margin5\": \"\",\n" + "        \"Lendable_amount5\": \"\",\n"
					+ "        \"CollateralCode5\": \"\",\n" + "        \"Security_id6\": \"\",\n"
					+ "        \"Lendable_margin6\": \"\",\n" + "        \"Lendable_amount6\": \"\",\n"
					+ "        \"CollateralCode6\": \"\",\n" + "        \"Security_id7\": \"\",\n"
					+ "        \"Lendable_margin7\": \"\",\n" + "        \"Lendable_amount7\": \"\",\n"
					+ "        \"CollateralCode7\": \"\",\n" + "        \"Security_id8\": \"\",\n"
					+ "        \"Lendable_margin8\": \"\",\n" + "        \"Lendable_amount8\": \"\",\n"
					+ "        \"CollateralCode8\": \"\",\n" + "        \"Security_id9\": \"\",\n"
					+ "        \"Lendable_margin9\": \"\",\n" + "        \"Lendable_amount9\": \"\",\n"
					+ "        \"CollateralCode9\": \"\",\n" + "        \"Security_id10\": \"\",\n"
					+ "        \"Lendable_margin10\": \"\",\n" + "        \"Lendable_amount10\": \"\",\n"
					+ "        \"CollateralCode10\": \"\",\n" + "        \"SwitchDueDate\": \"" + SwitchDueDate
					+ "\",\n" + "        \"CropLoan\": \"N\",\n" + "        \"CropType\": \"\",\n"
					+ "        \"Season\": \"\",\n" + "        \"ACPlanCode\": \"\",\n"
					+ "        \"ReviewPeriod\": \"0\",\n" + "        \"ReviewDate\": \"\",\n"
					+ "        \"CasaAcctNo\": \"" + salAccNum + "\",\n" + "        \"AreaOfFarm\": \"\",\n"
					+ "        \"FarmerCategory\": \"\",\n" + "        \"FarmerSubCategory\": \"\",\n"
					+ "        \"HousingLoanPurpose\": \"\",\n" + "        \"ProjectCost\": \"" + projectCost + "\",\n"
					+ "        \"AgriInfraUnit\": \"\",\n" + "        \"Activities\": \"\",\n"
					+ "        \"OtherBankLimit\": \"\",\n" + "        \"Subcategory\": \"\",\n"
					+ "        \"CenterPopulation\": \"\",\n" + "        \"NoOfDependents\": \"\",\n"
					+ "        \"PMFBYApplicable\": \"N\",\n" + "        \"SubsidyAvailable\": \"N\",\n"
					+ "        \"DrawdownRequired\": \"Y\",\n" + "        \"GroupFormationDate\": \"\",\n"
					+ "        \"CustomerID1\": \"\",\n" + "        \"Designation1\": \"\",\n"
					+ "        \"MemberStatus1\": \"\",\n" + "        \"CustomerID2\": \"\",\n"
					+ "        \"Designation2\": \"\",\n" + "        \"MemberStatus2\": \"\",\n"
					+ "        \"CustomerID3\": \"\",\n" + "        \"Designation3\": \"\",\n"
					+ "        \"MemberStatus3\": \"\",\n" + "        \"CustomerID4\": \"\",\n"
					+ "        \"Designation4\": \"\",\n" + "        \"MemberStatus4\": \"\",\n"
					+ "        \"CustomerID5\": \"\",\n" + "        \"Designation5\": \"\",\n"
					+ "        \"MemberStatus5\": \"\",\n" + "        \"CustomerID6\": \"\",\n"
					+ "        \"Designation6\": \"\",\n" + "        \"MemberStatus6\": \"\",\n"
					+ "        \"CustomerID7\": \"\",\n" + "        \"Designation7\": \"\",\n"
					+ "        \"MemberStatus7\": \"\",\n" + "        \"CustomerID8\": \"\",\n"
					+ "        \"Designation8\": \"\",\n" + "        \"MemberStatus8\": \"\",\n"
					+ "        \"CustomerID9\": \"\",\n" + "        \"Designation9\": \"\",\n"
					+ "        \"MemberStatus9\": \"\",\n" + "        \"CustomerID10\": \"\",\n"
					+ "        \"Designation10\": \"\",\n" + "        \"MemberStatus10\": \"\",\n"
					+ "        \"CustomerID11\": \"\",\n" + "        \"Designation11\": \"\",\n"
					+ "        \"MemberStatus11\": \"\",\n" + "        \"CustomerID12\": \"\",\n"
					+ "        \"Designation12\": \"\",\n" + "        \"MemberStatus12\": \"\",\n"
					+ "        \"CustomerID13\": \"\",\n" + "        \"Designation13\": \"\",\n"
					+ "        \"MemberStatus13\": \"\",\n" + "        \"CustomerID14\": \"\",\n"
					+ "        \"Designation14\": \"\",\n" + "        \"MemberStatus14\": \"\",\n"
					+ "        \"CustomerID15\": \"\",\n" + "        \"Designation15\": \"\",\n"
					+ "        \"MemberStatus15\": \"\",\n" + "        \"CustomerID16\": \"\",\n"
					+ "        \"Designation16\": \"\",\n" + "        \"MemberStatus16\": \"\",\n"
					+ "        \"CustomerID17\": \"\",\n" + "        \"Designation17\": \"\",\n"
					+ "        \"MemberStatus17\": \"\",\n" + "        \"CustomerID18\": \"\",\n"
					+ "        \"Designation18\": \"\",\n" + "        \"MemberStatus18\": \"\",\n"
					+ "        \"CustomerID19\": \"\",\n" + "        \"Designation19\": \"\",\n"
					+ "        \"MemberStatus19\": \"\",\n" + "        \"CustomerID20\": \"\",\n"
					+ "        \"Designation20\": \"\",\n" + "        \"MemberStatus20\": \"\",\n"
					+ "        \"ReviewAccount\": \"N\",\n" + "        \"RemarksProcessingCharges\": \"\",\n"
					+ "        \"CentreLeaderIndicator\": \"\",\n" + "        \"ShpiNgoName\": \"\",\n"
					+ "        \"ShpiNgoIdentifier\": \"\",\n" + "        \"ShpiNgoOfficerName\": \"\",\n"
					+ "        \"ShpiNgoAddress\": \"\",\n" + "        \"NoOfMeetingsHeld\": \"\",\n"
					+ "        \"NoOfMeetingsMissed\": \"\",\n" + "        \"AgreedMeetingDayOfTheWeek\": \"\",\n"
					+ "        \"AgreedMeetingTimeOfTheDay\": \"\",\n" + "        \"InsuranceIndicator\": \"\",\n"
					+ "        \"TypeOfInsurance\": \"\",\n" + "        \"SumAssuredCoverage\": \"\",\n"
					+ "        \"LoanCategoryGrp\": \"\",\n" + "        \"CropCultivationAmountKhrif\": \"\",\n"
					+ "        \"CropCultivationAmountRabi\": \"\",\n"
					+ "        \"CropCultivationAmountSumer\": \"\",\n" + "        \"CropInsurancePremium\": \"\",\n"
					+ "        \"LinkTDRDForInterest\": \"\",\n" + "        \"CasaAcctNoSC\": \"\",\n"
					+ "        \"FlgNomAppl\": \"\",\n" + "        \"concessionPermitted\": \"N\",\n"
					+ "        \"BankArr\": \"1\",\n" + "        \"BankFiTyp\": \"\",\n" + " \"BaselCustType\" : \""
					+ codBaselCode + "\",\n" + "        \"PrimarySecondary1\": \"P\",\n" + "  \"LoanPaperDpCode\": \""
					+ selectedBranchCode + "\",\n" + "       \"CBRGuaranteeAmount\": \"\"\n" + "    }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String LoanAccountCreationResponseResponse = bodyObj.get("LoanAccountCreationResponse").toString();
					// Log.consoleLog(ifr, "Response==>" + Response);
					System.out.println("LoanAccountCreationResponseResponse=>" + LoanAccountCreationResponseResponse);

					JSONObject LoanAccountCreationResponseResponseJSON = (JSONObject) parser
							.parse(LoanAccountCreationResponseResponse);
					JSONObject LoanAccountCreationResponseResponseObj = new JSONObject(
							LoanAccountCreationResponseResponseJSON);

					LoanAccNumber = LoanAccountCreationResponseResponseObj.get("accountNo").toString();
					String sanctionAmount = LoanAccountCreationResponseResponseObj.get("sanctionAmount").toString();

					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					Log.consoleLog(ifr, "sanctionAmount==>" + sanctionAmount);

					Log.consoleLog(ifr, "AccountNumber==>" + LoanAccNumber);

					java.util.Date dateTimeFormat = new java.util.Date();
					SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
					String strCurDateTime = sDateTime.format(dateTimeFormat);

					String Query1 = "";
					Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET " + "LOAN_ACCOUNTNO='" + LoanAccNumber.trim() + "', "
							+ "ACCOUNT_CREATEDDATE='" + strCurDateTime.trim()+ "', " + "SANCTION_AMOUNT='" + sanctionAmount
							+ "', " + "DISB_AMOUNT='" + loanAmount + "', " + "DISB_BRANCH='" + selectedBranchCode + "' "
							+ "WHERE PID='" + processInstanceId + "' "
							+ "AND (LOAN_ACCOUNTNO IS NULL OR LOAN_ACCOUNTNO = '')";

					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
					ifr.saveDataInDB(Query1);
					Log.consoleLog(ifr, "Query1=>" + Query1);

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
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}

		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String execLoanAccountBlocking(IFormReference ifr, String processInstanceId, String customerID,
			String productCode, String shortName) throws ParseException {
		String apiName = "AccountBlocking";
		String serviceName = "CBS_" + apiName;
		String request = "";
		String APIStatus = "";
		String status = "";

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String formattedDate = dateFormat.format(currentDate);

		String BankCode = pcm.getConstantValue(ifr, "CBDLP", "BANKCODE");

		String userId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");

		String Sc_UserId = pcm.getConstantValue(ifr, "CBSACCBLOCK", "USERID");
		String Channel = pcm.getConstantValue(ifr, "ACCOUNTBLOCK", "CHANNEL");

		java.util.Date dateTimeFormat = new java.util.Date();
		SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
		String strCurDateTime = sDateTime.format(dateTimeFormat);

		Date d = new Date();
		SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
		String curDate = sd1.format(d);

		String tBranch = "";
		String selectedBranchCode = "";
		String loanPaperDpCode = "507";
		String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
				+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
		List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
		if (!result.isEmpty()) {
			tBranch = result.get(0).get(0);
		}

		String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='" + processInstanceId
				+ "' ";
		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		Log.consoleLog(ifr, "branchCodeResult query==>" + branchCodeResult.size());
		if (!branchCodeResult.isEmpty()) {
			selectedBranchCode = branchCodeResult.get(0).get(0);
			Log.consoleLog(ifr, "selectedBranchCode==>" + selectedBranchCode);
		}
		if (branchCodeResult.isEmpty()) {
			selectedBranchCode = tBranch;
			Log.consoleLog(ifr, "selectedBranchCode==>" + selectedBranchCode);
		}

		Log.consoleLog(ifr, "tBranch" + tBranch);
		String loanAmt = "";
		String queryLoanAmt = "SELECT LOAN_AMOUNT FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId + "'";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryLoanAmt);
		List<List<String>> resqueryLoanAmt = ifr.getDataFromDB(queryLoanAmt);
		if (!resqueryLoanAmt.isEmpty()) {
			loanAmt = resqueryLoanAmt.get(0).get(0);
		}

		String shortenedName = shortName.length() > 17 ? shortName.substring(0, 17) : shortName;
		String extUniqueRefId = generateUniqueID(17);
		request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
				+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
				+ "                \"UserId\": \"" + Sc_UserId + "\"\n" + "            },\n"
				+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
				+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
				+ "            \"ExternalReferenceNo\": \"\",\n"
				+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
				+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
				+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
				+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
				+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "            \"UserId\": \""
				+ Sc_UserId + "\",\n" + "            \"ValueDateText\": \"\"\n" + "        },\n"
				+ "        \"Branchcode\": \"" + selectedBranchCode + "\",\n" + "        \"ExistingCustId\": \""
				+ customerID + "\",\n" + "        \"ProductCode\": \"" + productCode + "\",\n"
				+ "  \"LoanPaperDpCode\": \"" + selectedBranchCode + "\",\n" + "       \"Shortname\": \""
				+ shortenedName + "\"\n" + "    }\n" + "}";

		Log.consoleLog(ifr, "Request====>" + request);
		String response = cm.getWebServiceResponse(ifr, apiName, request);
		Log.consoleLog(ifr, "Response===>" + response);
		String ErrorCode = "";
		String ErrorMessage = "";
		String accountNumber = "";

		if (!response.equalsIgnoreCase("{}")) {
			JSONParser parser = new JSONParser();
			JSONObject OutputJSON = (JSONObject) parser.parse(response);
			JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyJSON = (JSONObject) parser.parse(body);
			if (Optional.ofNullable(bodyJSON.get("AccountNumber")).isPresent()) {
				accountNumber = bodyJSON.get("AccountNumber").toString();
			}

			JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {

//				String Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET LOAN_ACCOUNTNO= '" + accountNumber.trim()
//				+ "'," + "ACCOUNT_CREATEDDATE= '" + strCurDateTime + "',SANCTION_AMOUNT=" + "DISB_DATE= '"
//				+ strCurDateTime + "'  WHERE PID= '" + processInstanceId + "'";
//		       Log.consoleLog(ifr, "UPFATE LOANACCOUNT disbur details-> " + Query1);
//		       ifr.saveDataInDB(Query1);

				String Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET LOAN_ACCOUNTNO= '" + accountNumber.trim()
						+ "', ACCOUNT_CREATEDDATE='" + strCurDateTime + "', SANCTION_AMOUNT='" + loanAmt
						+ "',DISB_AMOUNT='" + loanAmt + "',DISB_DATE='" + strCurDateTime + "',DISB_BRANCH='"
						+ selectedBranchCode + "' WHERE PID= '" + processInstanceId + "'"
						+ "AND (LOAN_ACCOUNTNO IS NULL OR LOAN_ACCOUNTNO = '')";
				Log.consoleLog(ifr, "UPFATE LOANACCOUNT disbur details-> " + Query1);
//				String Query1 = "UPDATE INTO SLOS_TRN_LOANDETAILS "
//						+ "(PID,APPLICATION_REFNO,LOAN_ACCOUNTNO,ACCOUNT_CREATEDDATE,SANCTION_AMOUNT,DISB_AMOUNT,DISB_DATE)\n"
//						+ "VALUES ('" + processInstanceId + "','','" + accountNumber + "','" + strCurDateTime + "','"
//						+ loanAmt + "','" + loanAmt + "','" + curDate + "')";
//				Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
				ifr.saveDataInDB(Query1);

			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
		} else {
			response = "No response from the server.";
			ErrorMessage = "FAIL";
		}

		try {
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = RLOS_Constants.SUCCESS;
				status = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				status = RLOS_Constants.ERROR;
			}
			if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return APIStatus;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			accountNumber = "";
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		}

		return status + ":" + ErrorMessage;
	}

	public static String generateUniqueID(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(10)); // Appending digits from 0 to 9
		}

		return sb.toString();
	}

	public String execLoanAccountActivate(IFormReference ifr, String processInstanceId, String customerID,
			String productCode, String loanAccountNumber, HashMap<String, String> map, String shortName)
			throws ParseException {
		String apiName = "AccountActivate";
		String serviceName = "CBS_" + apiName;
		String status = "";
		String APIStatus = "";

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		String BankCode = pcm.getConstantValue(ifr, "CBSDISBENQ", "BANKCODE");

		String Channel = pcm.getConstantValue(ifr, "ACCOUNTACTIVATION", "CHANNEL");
		String userId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");

		String Sc_UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");

		String Relation = pcm.getConstantValue(ifr, "CBDLP", "CUSTREL");

		String LoanAmount = "";

		String trnQuery = "Select Loan_Amount from slos_staff_trn where winame='" + processInstanceId + "'";
		List<List<String>> trnResponse = ifr.getDataFromDB(trnQuery);
		Log.consoleLog(ifr, "trnQuery==> " + trnQuery);
		Log.consoleLog(ifr, "res==" + trnResponse);
		if (!trnResponse.isEmpty()) {
			LoanAmount = trnResponse.get(0).get(0);
			// String updateQuery="UPDATE SLOS_TRN_LOANDETAILS set
			// sanction_amount='"+LoanAmount+"' where winame='"+processInstanceId+"'";
			// ifr.saveDataInDB(updateQuery);
			Log.consoleLog(ifr, "");
		}

//		String Query = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#", processInstanceId);
//		List<List<String>> Output3 = cf.mExecuteQuery(ifr, Query, Query);
//		if (!Output3.isEmpty()) {
//			LoanAmount = Output3.get(0).get(0);
//
//		}
		String extUniqueRefId = generateUniqueID(17);
		String tBranch = "";
		String selectedBranchCode = "";
		String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
				+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
		List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
		if (!result.isEmpty()) {
			tBranch = result.get(0).get(0);
		}

		String querySelectedBranchCode = "select DISB_BRANCH,LOAN_ACCOUNTNO from SLOS_TRN_LOANDETAILS  where PID='"
				+ processInstanceId + "' ";
		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		Log.consoleLog(ifr, "querySelectedBranchCode==>" + branchCodeResult);
		if (!branchCodeResult.isEmpty()) {
			selectedBranchCode = branchCodeResult.get(0).get(0);
			loanAccountNumber = branchCodeResult.get(0).get(1);
		}
		if (branchCodeResult.isEmpty()) {
			selectedBranchCode = tBranch;
		}
		String request = "{\n" + "    \"input\": {\n" + "        \"AcctDescription\": \"\",\n"
				+ "        \"CustomerID\": \"" + customerID + "\",\n" + "        \"ExtUniqueRefId\": \""
				+ extUniqueRefId + "\",\n" + "        \"InterestWaiver\": \"\",\n"
				+ "        \"MinorAccountStatus\": \"" + "0" + "\",\n" + "        \"NewAccountNo\": \""
				+ loanAccountNumber.trim() + "\",\n" + "        \"OfficerID\": \"" + userId + "\",\n"
				+ "        \"Relation\": \"" + Relation + "\",\n" + "        \"AppriaserReg\": \"\",\n"
				+ "        \"BankArr\": \"\",\n" + "        \"Relation1\": \"\",\n" + "        \"Relation2\": \"\",\n"
				+ "        \"RestrictedAccount\": \"\",\n" + "        \"SecondaryCustomerID1\": \"\",\n"
				+ "        \"SecondaryCustomerID2\": \"\",\n" + "        \"SessionContext\": {\n"
				+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
				+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
				+ "            \"ExternalReferenceNo\": \"\",\n"
				+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
				+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
				+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
				+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
				+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
				+ "                \"UserId\": \"" + Sc_UserId + "\"\n" + "            },\n"
				+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "            \"UserId\": \""
				+ userId + "\",\n" + "            \"ValueDateText\": \"\"\n" + "        },\n"
				+ "        \"ThresholdLimit\": \"" + LoanAmount + "\"\n" + "    }\n" + "}";
		Log.consoleLog(ifr, "Request====>" + request);
		String response = cm.getWebServiceResponse(ifr, apiName, request);
		Log.consoleLog(ifr, "Response===>" + response);
		String ErrorCode = "";
		String ErrorMessage = "";

		if (!response.equalsIgnoreCase("{}")) {
			JSONParser parser = new JSONParser();
			JSONObject OutputJSON = (JSONObject) parser.parse(response);
			JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyJSON = (JSONObject) parser.parse(body);
			JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {

			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
		} else {
			response = "No response from the server.";
			ErrorMessage = "FAIL";
		}

		try {
			// String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				status = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				status = RLOS_Constants.ERROR;
			}

			if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return status;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			status = RLOS_Constants.ERROR;
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		}

		return status + ":" + ErrorMessage;
	}

	public String createLADODLoanAccount(IFormReference ifr, String ProcessInstanceId, String CustomerId,
			String productCode, HashMap<String, String> map) throws ParseException {
		Log.consoleLog(ifr, "#getLADODLoanAccountDetails=================");
		Log.consoleLog(ifr, "productCode===============" + productCode);
		String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String LoanAccNumber = "";
		String APIStatus = "";
		String apiName = "ODLIMITCREATION";
		String serviceName = "CBS_" + apiName;
		String status = "";
		String codBaselCode = "";
		String flgMntStatus = "";

		String extUniqueRefId = generateUniqueID(17);

		String BankCode = pcm.getConstantValue(ifr, "CBSODADLOANACC", "BANKCODE");
		String Channel = pcm.getConstantValue(ifr, "LIMITCREATE", "CHANNEL");
		String S_UserId = pcm.getConstantValue(ifr, "CBSODADLOANACC", "SC_USERID");
		String UserId = pcm.getConstantValue(ifr, "CBSODADLOANACC", "USERID");
		String sancAuthority = "";

		String queryForBaselCode = "select cod_basel_code, flg_mnt_status from ba_prod_basel_cust_type where cod_prod = '"
				+ productCode + "'";
		Log.consoleLog(ifr, "Salary CODE BASEL CODE query===>" + queryForBaselCode);
		List<List<String>> resultForBaselCode = ifr.getDataFromDB(queryForBaselCode);
		Log.consoleLog(ifr, "resultForBaselCode===>" + resultForBaselCode);
		if (!resultForBaselCode.isEmpty()) {
			flgMntStatus = resultForBaselCode.get(0).get(1);
			if (!flgMntStatus.isEmpty() && flgMntStatus != null && flgMntStatus.equalsIgnoreCase("A")) {
				codBaselCode = resultForBaselCode.get(0).get(0);
			}
		}

		String tBranch = "";
		String selectedBranchCode = "";
		String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
				+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
		List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
		if (!result.isEmpty()) {
			tBranch = result.get(0).get(0);
		}

		String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='" + ProcessInsanceId
				+ "' ";
		Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
		List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
		if (!branchCodeResult.isEmpty()) {
			selectedBranchCode = branchCodeResult.get(0).get(0);
		}
		if (branchCodeResult.isEmpty()) {
			selectedBranchCode = tBranch;
		}

		Date currDate = new Date();
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMdd");
		String SanctionDate = dateForm.format(currDate);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate date = LocalDate.parse(SanctionDate, formatter);
		LocalDate newDate = date.plusMonths(24).minusDays(1);
		String LimitDateEnd = newDate.format(formatter);

		String InterestIndx = map.get("InterestIndx");
		String CodMisComp1 = map.get("CodMisComp1");
		String takeOverOD = map.get("takeOverOD");

		String bankArr = map.get("bankArr");

		String LimitPurpose = map.get("LimitPurpose");

		String InternalFD = map.get("InternalFD");
		String SanctionAuthority = map.get("SanctionAuthority");

		String statename = map.get("StateName");
		String distictName = map.get("DistrictName");

		String codMixquery = "select * from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where PROD_CODE='" + productCode + "'";
		List<List<String>> codMixqueryList = ifr.getDataFromDB(codMixquery);
		Log.consoleLog(ifr, "codMixquery===============" + codMixquery);
		String CodMisTxn1 = "";
		String CodMisTxn2 = "";
		String CodMisTxn3 = "";
		String CodMisTxn4 = "";
		String CodMisTxn5 = "";
		String CodMisTxn6 = "";
		String CodMisTxn7 = "";
		String CodMisTxn8 = "";
		String CodMisTxn9 = "";
		String CodMisTxn10 = "";
		String CodMisTxn11 = "";

		if (codMixqueryList.size() > 0) {
			CodMisTxn1 = codMixqueryList.get(0).get(1);
			// CodMisTxn2 = codMixqueryList.get(0).get(2);
			CodMisTxn2 = codMixqueryList.get(0).get(2);
			CodMisTxn3 = codMixqueryList.get(0).get(3);
			CodMisTxn4 = codMixqueryList.get(0).get(4);
			CodMisTxn5 = codMixqueryList.get(0).get(5);
			CodMisTxn6 = codMixqueryList.get(0).get(6);
			CodMisTxn7 = codMixqueryList.get(0).get(7);
			CodMisTxn8 = codMixqueryList.get(0).get(8);
			CodMisTxn9 = codMixqueryList.get(0).get(9);
			CodMisTxn10 = codMixqueryList.get(0).get(10);
			CodMisTxn11 = codMixqueryList.get(0).get(11);
		}

		String accNo = "";
		String Query1 = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS WHERE PID= '" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "Accoint number-> " + Query1);
		List<List<String>> queryresult = ifr.getDataFromDB(Query1);

		if (!queryresult.isEmpty()) {
			accNo = queryresult.get(0).get(0);
		}

		String appRefNo = "";
		String appRefNoQuery1 = "SELECT APPLICATIONNUMBER FROM LOS_EXT_TABLE WHERE PID= '" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "appRefNoQuery1-> " + appRefNoQuery1);
		List<List<String>> queryappRefNoQuery1 = ifr.getDataFromDB(appRefNoQuery1);

		if (!queryappRefNoQuery1.isEmpty()) {
			appRefNo = queryappRefNoQuery1.get(0).get(0);
		}

		String loanAmount = "";
		String queryLoanAmt = "SELECT LOAN_AMOUNT FROM SLOS_STAFF_TRN WHERE WINAME='" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryLoanAmt);
		List<List<String>> resqueryLoanAmt = ifr.getDataFromDB(queryLoanAmt);
		if (!resqueryLoanAmt.isEmpty()) {
			loanAmount = resqueryLoanAmt.get(0).get(0);
		}
		double number = Double.parseDouble(loanAmount);
		long loanAmt = (long) number;

		String renewalMode = "NONE";
		String request = "{\n" + "    \"input\": {\n" + "        \"CBRGuaranteeAmount\": \"\",\n"
				+ "        \"Designation11\": \"\",\n" + "        \"DatCharge\": \"\",\n"
				+ "        \"Designation10\": \"\",\n" + "        \"Designation15\": \"\",\n"
				+ "        \"CustomerID9\": \"\",\n" + "        \"Designation14\": \"\",\n"
				+ "        \"Designation13\": \"\",\n" + "        \"OtherBankLimit\": \"\",\n"
				+ "        \"Designation12\": \"\",\n" + "        \"Designation19\": \"\",\n"
				+ "        \"Designation18\": \"\",\n" + "        \"NameFI\": \"\",\n"
				+ "        \"Designation17\": \"\",\n" + "        \"Designation16\": \"\",\n"
				+ "        \"CustomerID17\": \"\",\n" + "        \"CustomerID16\": \"\",\n"
				+ "        \"CustomerID15\": \"\",\n" + "        \"CustomerID14\": \"\",\n"
				+ "        \"CustomerID13\": \"\",\n" + "        \"CustomerID12\": \"\",\n"
				+ "        \"CustomerID11\": \"\",\n" + "        \"CustomerID10\": \"\",\n"
				+ "        \"ExtUniqueRefId\": \"" + extUniqueRefId + "\",\n"
				+ "        \"CollateralDegree_3\": \"\",\n" + "        \"Designation1\": \"\",\n"
				+ "        \"CollateralDegree_2\": \"\",\n" + "        \"Designation2\": \"\",\n"
				+ "        \"WomenCount\": \"\",\n" + "        \"CollateralDegree_1\": \"\",\n"
				+ "        \"TDDepNo\": \"\",\n" + "        \"Designation5\": \"\",\n" + "        \"AccountNo\": \""
				+ accNo + "\",\n" + "        \"Designation6\": \"\",\n" + "        \"Designation3\": \"\",\n"
				+ "        \"Designation4\": \"\",\n" + "        \"MemberCount\": \"\",\n"
				+ "        \"CollateralDegree_9\": \"\",\n" + "        \"CollateralDegree_8\": \"\",\n"
				+ "        \"CollateralDegree_7\": \"\",\n" + "        \"CollateralDegree_6\": \"\",\n"
				+ "        \"CollateralDegree_5\": \"\",\n" + "        \"CollateralDegree_4\": \"\",\n"
				+ "        \"AnchorNGO\": \"\",\n" + "        \"InternalRD\": \"\",\n" + "        \"CodMisTxn2\": \""
				+ CodMisTxn2 + "\",\n" + "        \"RatLimLevel\": \"\",\n" + "        \"CodMisTxn1\": \"" + CodMisTxn1
				+ "\",\n" + "        \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n" + "        \"CodMisTxn5\": \""
				+ CodMisTxn5 + "\",\n" + "        \"BankFiTyp\": \"\",\n" + "        \"CodMisTxn4\": \"" + CodMisTxn4
				+ "\",\n" + "        \"CodMisTxn3\": \"" + CodMisTxn3 + "\",\n" + "        \"TDDepCert\": \"\",\n"
				+ "        \"CodMisTxn9\": \"" + CodMisTxn9 + "\",\n" + "        \"CodMisTxn8\": \"" + CodMisTxn8
				+ "\",\n" + "        \"CodMisTxn7\": \"" + CodMisTxn7 + "\",\n"
				+ "        \"RatIntConcession\": \"\",\n" + "        \"Designation9\": \"\",\n"
				+ "        \"concessionPermitted\": \"N\",\n" + "        \"DatODPapers\": \"" + SanctionDate + "\",\n"
				+ "        \"Designation7\": \"\",\n" + "        \"Designation8\": \"\",\n"
				+ "        \"Designation20\": \"\",\n" + "        \"RdCollDesc\": \"\",\n"
				+ "        \"CustomerID2\": \"\",\n" + "        \"CustomerID1\": \"\",\n"
				+ "        \"DistrictName\":\"\",\n" + "        \"CustomerID4\": \"\",\n"
				+ "        \"CustomerID3\": \"\",\n" + "        \"CustomerID6\": \"\",\n" + "        \"AmtLimit\": \""
				+ loanAmt + "\",\n" + "        \"CustomerID5\": \"\",\n" + "        \"CustomerID8\": \"\",\n"
				+ "        \"CustomerID7\": \"\",\n" + "        \"AgreedMeetingTimeOfTheDay\": \"\",\n"
				+ "        \"GroupFormationDate\": \"\",\n" + "        \"Num13\": \"\",\n"
				+ "        \"concessionRatePermittedBy\": \"\",\n" + "        \"CollateralType_1\": \"\",\n"
				+ "        \"CollateralType_5\": \"\",\n" + "        \"CollateralType_4\": \"\",\n"
				+ "        \"CollateralType_3\": \"\",\n" + "        \"CollateralType_2\": \"\",\n"
				+ "        \"CollateralType_9\": \"\",\n" + "        \"CollateralType_8\": \"\",\n"
				+ "        \"CollateralType_7\": \"\",\n" + "        \"CollateralType_6\": \"\",\n"
				+ "        \"FITakeover\": \"\",\n" + "        \"AmtCharge\": \"\",\n"
				+ "        \"TxnCharge\": \"N\",\n" + "        \"RefSancNum\": \"" + appRefNo + "\",\n"
				+ "        \"SHPINGOIdentifier\": \"\",\n" + "        \"LimitDateStart\": \"" + SanctionDate + "\",\n"
				+ "        \"LinkRDForInterest\": \"\",\n" + "        \"CollateralDegree\": \"\",\n"
				+ "        \"RdLendMmarg\": \"\",\n" + "        \"TypeOfInsurance\": \"\",\n"
				+ "        \"Activities\": \"\",\n" + "        \"RdAccountNo\": \"\",\n"
				+ "        \"CollateralDesc_10\": \"\",\n" + "        \"CustomerID19\": \"\",\n"
				+ "        \"CustomerID18\": \"\",\n" + "        \"CollateralID_3\": \"\",\n"
				+ "        \"CollateralID_10\": \"\",\n" + "        \"CollateralID_4\": \"\",\n"
				+ "        \"InterestIndx\": \"" + InterestIndx + "\",\n" + "        \"CollateralID_1\": \"\",\n"
				+ "        \"CollateralID_2\": \"\",\n" + "        \"CustomerID20\": \"\",\n"
				+ "        \"SessionContext\": {\n" + "            \"OriginalReferenceNo\": \"\",\n"
				+ "            \"UserReferenceNumber\": \"\",\n" + "            \"SupervisorContext\": {\n"
				+ "                \"PrimaryPassword\": \"\",\n" + "                \"UserId\": \"" + S_UserId + "\"\n"
				+ "            },\n" + "            \"Channel\": \"" + Channel + "\",\n"
				+ "            \"BankCode\": \"" + BankCode + "\",\n"
				+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
				+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"LocalDateTimeText\": \"\",\n"
				+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
				+ "            \"ServiceCode\": \"\",\n" + "            \"UserId\": \"" + UserId + "\",\n"
				+ "            \"SessionTicket\": \"\",\n" + "            \"ValueDateText\": \"\",\n"
				+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n"
				+ "            \"ExternalReferenceNo\": \"" + extUniqueRefId + "\"\n" + "        },\n"
				+ "        \"CollateralID_9\": \"\",\n" + "        \"CollateralID_7\": \"\",\n"
				+ "        \"CollateralID_8\": \"\",\n" + "        \"LimitPurpose\": \"6\",\n"
				+ "        \"CollateralID_5\": \"\",\n" + "        \"CollateralID_6\": \"\",\n"
				+ "        \"MemberStatus3\": \"\",\n" + "        \"MemberStatus2\": \"\",\n"
				+ "        \"CollateralMargin_1\": \"\",\n" + "        \"MemberStatus1\": \"\",\n"
				+ "        \"CollCode\": \"\",\n" + "        \"CollateralMargin_7\": \"\",\n"
				+ "        \"CollateralMargin_6\": \"\",\n" + "        \"CollateralMargin_9\": \"\",\n"
				+ "        \"MemberStatus19\": \"\",\n" + "        \"MemberStatus9\": \"\",\n"
				+ "        \"CollateralMargin_8\": \"\",\n" + "        \"CollateralMargin_3\": \"\",\n"
				+ "        \"MemberStatus17\": \"\",\n" + "        \"MemberStatus7\": \"\",\n"
				+ "        \"CollateralMargin_2\": \"\",\n" + "        \"MemberStatus18\": \"\",\n"
				+ "        \"MemberStatus6\": \"\",\n" + "        \"CollateralMargin_5\": \"\",\n"
				+ "        \"MemberStatus15\": \"\",\n" + "        \"MemberStatus5\": \"\",\n"
				+ "        \"CollateralMargin_4\": \"\",\n" + "        \"MemberStatus16\": \"\",\n"
				+ "        \"MemberStatus4\": \"\",\n" + "        \"MemberStatus13\": \"\",\n"
				+ "        \"MemberStatus14\": \"\",\n" + "        \"MemberStatus11\": \"\",\n"
				+ "        \"MemberStatus12\": \"\",\n" + "        \"MemberStatus10\": \"\",\n"
				+ "        \"LimitDateEnd\": \"" + LimitDateEnd + "\",\n" + "        \"dateOfConcession\": \"\",\n"
				+ "        \"CentreLeadIndicator\": \"\",\n" + "        \"LimitSecured\": \"N\",\n"
				+ "        \"CollValue\": \"\",\n" + "        \"TDAccountNo\": \"\",\n"
				+ "        \"CollateralAmt_1\": \"\",\n" + "        \"CollateralAmt_10\": \"\",\n"
				+ "        \"AgreedMeetingDayOfWeek\": \"\",\n" + "        \"CollateralAmt_5\": \"\",\n"
				+ "        \"GroupType\": \"\",\n" + "        \"CollateralAmt_4\": \"\",\n"
				+ "        \"CollateralAmt_3\": \"\",\n" + "        \"SHPINGOofficerName\": \"\",\n"
				+ "        \"CollateralAmt_2\": \"\",\n" + "        \"CollateralAmt_9\": \"\",\n"
				+ "        \"CollateralAmt_8\": \"\",\n" + "        \"CollateralAmt_7\": \"\",\n"
				+ "        \"CollateralAmt_6\": \"\",\n" + "        \"AgriInfraUnit\": \"\",\n"
				+ "        \"FDCollType\": \"\",\n" + "        \"GovSponsoredLoan\": \"\",\n"
				+ "        \"bankArr\": \"1\",\n" + "        \"FinanceBy\": \"0\",\n" + "        \"RdValue\": \"\",\n"
				+ "        \"SanctionAuthority\": \"75\",\n" + "        \"CodMisComp10\": \"\",\n"
				+ "        \"NameGovernment\": \"\",\n" + "        \"StateName\": \"" + statename + "\",\n"
				+ "        \"SumAssuredCoverage\": \"\",\n" + "        \"CollateralDesc_8\": \"\",\n"
				+ "        \"CollateralDesc_7\": \"\",\n" + "        \"CollateralDesc_9\": \"\",\n"
				+ "        \"LimitCategory\": \"0\",\n" + "        \"NoOfDependents\": \"\",\n"
				+ "        \"NoOfMeetingsMissed\": \"\",\n" + "        \"CollDesc\": \"\",\n"
				+ "        \"InternalFD\": \"N\",\n" + "        \"LinkTD\": \"\",\n"
				+ "        \"SHPINGOName\": \"\",\n" + "        \"Num3\": \"\",\n" + "        \"takeOverOD\": \"N\",\n"
				+ "        \"SchemeName\": \"\",\n" + "        \"BranchCode\": \"" + selectedBranchCode + "\",\n"
				+ "        \"CodMisTxn10\": \"" + CodMisTxn10 + "\",\n" + "        \"CodMisTxn11\": \"" + CodMisTxn11
				+ "\",\n" + "        \"NoOfMeetingsHeld\": \"\",\n" + "        \"SHPINGOAddress\": \"\",\n"
				+ "        \"CodMisComp6\": \"\",\n" + "        \"MenCount\": \"\",\n"
				+ "        \"CodMisComp7\": \"\",\n" + "        \"LoanCategory\": \"\",\n"
				+ "        \"SanctionDate\": \"" + SanctionDate + "\",\n" + "        \"CodMisComp8\": \"\",\n"
				+ "        \"CodMisComp9\": \"\",\n" + "        \"CollateralDegree_10\": \"\",\n"
				+ "        \"CollateralType_10\": \"\",\n" + "        \"CodMisComp1\": \"NA\",\n"
				+ "        \"CodMisComp2\": \"\",\n" + "        \"CodMisComp3\": \"\",\n"
				+ "        \"CodMisComp4\": \"\",\n" + "        \"CodMisComp5\": \"\",\n"
				+ "        \"InsuranceIndicator\": \"\",\n" + "        \"CollateralDesc_4\": \"\",\n"
				+ "        \"CollateralDesc_3\": \"\",\n" + "        \"CollateralDesc_6\": \"\",\n"
				+ "        \"CollateralMargin_10\": \"\",\n" + "        \"CollateralDesc_5\": \"\",\n"
				+ "        \"MemberStatus20\": \"\",\n" + "        \"CollateralDesc_2\": \"\",\n"
				+ "        \"CollateralDesc_1\": \"\",\n" + "        \"LendableMargin\": \"\",\n"
				+ " \"BaselCustType\" : \"" + codBaselCode + "\",\n" + "        \"renewalMode\": \"" + renewalMode
				+ "\"\n" + "    }\n" + "}";

		Log.consoleLog(ifr, "Request====>" + request);
		String response = cm.getWebServiceResponse(ifr, apiName, request);
		Log.consoleLog(ifr, "Response===>" + response);
		String ErrorCode = "";
		String ErrorMessage = "";

		if (!response.equalsIgnoreCase("{}")) {
			JSONParser parser = new JSONParser();
			JSONObject OutputJSON = (JSONObject) parser.parse(response);
			JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyJSON = (JSONObject) parser.parse(body);
			JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInsanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				JSONObject ODLimitMntResponseResponseJSON = (JSONObject) bodyObj.get("ODLimitMntResponse");
				// Log.consoleLog(ifr, "Response==>" + Response);
				Log.consoleLog(ifr, "LoanAccountCreationResponseResponse=>" + ODLimitMntResponseResponseJSON);

//				java.util.Date dateTimeFormat = new java.util.Date();
//				SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
//				String strCurDateTime = sDateTime.format(dateTimeFormat);

//				String queryForPID = "SELECT PID FROM SLOS_TRN_LOANDETAILS where rownum=1";
//				Log.consoleLog(ifr, "#Inside query..." + queryForPID);

				// List<List<String>> listForqueryPID = ifr.getDataFromDB(queryForPID);
//				if (!listForqueryPID.isEmpty()) {
//					String Query2 = "UPDATE SLOS_TRN_LOANDETAILS SET LOAN_ACCOUNTNO= '" + LoanAccNumber.trim()
//							+ "'," + "ACCOUNT_CREATEDDATE= '" + strCurDateTime + "'," + "DISB_DATE= '"
//							+ strCurDateTime + "'  WHERE PID= '" + ProcessInsanceId + "'";
//					Log.consoleLog(ifr, "UPFATE LOANACCOUNT disbur details-> " + Query1);
//					ifr.saveDataInDB(Query2);
//				}
//
//				else {
//
//					Query1 = "INSERT INTO SLOS_TRN_LOANDETAILS "
//							+ "(PID,APPLICATION_REFNO,LOAN_ACCOUNTNO,ACCOUNT_CREATEDDATE,DISB_DATE)\n"
//							+ "VALUES ('" + ProcessInsanceId + "','','" + LoanAccNumber + "','" + strCurDateTime
//							+ "','"+strCurDateTime+"')";
//					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
//
//					cf.mExecuteQuery(ifr, Query1, "INSERT LOANACCOUNT disbur details  ");
//				}

			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
		} else {
			response = "No response from the server.";
			ErrorMessage = "FAIL";
		}

		try {
			// String APIName = "CBS_DisbursementEnquiry";

			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				status = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				status = RLOS_Constants.ERROR;
			}

			if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return APIStatus;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, ProcessInsanceId, serviceName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		}

		return status + ":" + ErrorMessage;

	}

	public String fundTransfer(IFormReference ifr, String processInstanceId, String sBACCNUMBER, String journeyType,
			String loanAmount, String stateCode, String tenure, String schemeId, String loanType) {
		String apiName = "FundTransfer";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String status = "";
		Double loanAmt = 0.0;
		Double odUtilized = 0.0;
		JSONArray getTotalCharges = null;
		try {

			Log.consoleLog(ifr, "Entered into Execute_CBSFundTransfer...");

			String BankCode = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "CHANNEL");
			String S_UserId = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "SC_USERID");
			String UserId = pcm.getConstantValue(ifr, "CBSFUNDTRANS", "USERID");
			// String TBranch = "";

			// String tBranch = "";
			String selectedBranchCode = "";
//				String queryBranchCode = "select  SUBSTR(homebranch, 1, Instr(homebranch, '-', -1, 1) -1) branchcode\n"
//						+ " from LOS_T_CUSTOMER_ACCOUNT_SUMMARY where winame='" + processInstanceId + "'";
//				Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryBranchCode);
//				List<List<String>> result = ifr.getDataFromDB(queryBranchCode);
//				if (!result.isEmpty()) {
//					tBranch = result.get(0).get(0);
//				}
			
			String calculatedStampCharges = "";
			String stmChargesQuery = "SELECT STAMPCHARGES FROM SLOS_TRN_LOANSUMMARY WHERE WINAME='" + processInstanceId
					+ "'";
			List<List<String>> liststmChargesQuery = ifr.getDataFromDB(stmChargesQuery);
			if(!liststmChargesQuery.isEmpty())
			{
				calculatedStampCharges=liststmChargesQuery.get(0).get(0);
			}
			
			String loanAccountNumber = "";
			if (journeyType.equalsIgnoreCase("HRMS")) {
				String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS  where PID='"
						+ processInstanceId + "' ";
				Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
				List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
				if (!branchCodeResult.isEmpty()) {
					selectedBranchCode = branchCodeResult.get(0).get(0);
				}

				// Double loanAmt=0.0;

				String hrmsQuery = "SELECT SALARY_ACC_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> listhrmsQuery = ifr.getDataFromDB(hrmsQuery);
				Log.consoleLog(ifr, "Log of hrmsQuery====>" + listhrmsQuery);
				if (!listhrmsQuery.isEmpty()) {
					loanAccountNumber = listhrmsQuery.get(0).get(0);
					Log.consoleLog(ifr, "loanAccountNumber====>" + loanAccountNumber);
				}
			} else if (journeyType.equalsIgnoreCase("STAFFVL")) {
				String querySelectedBranchCode = "select BRANCH_CODE from SLOS_STAFF_TRN  where WINAME='"
						+ processInstanceId + "' ";
				Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
				List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
				if (!branchCodeResult.isEmpty()) {
					selectedBranchCode = branchCodeResult.get(0).get(0);
				}

				// Double loanAmt=0.0;

				String hrmsQuery = "SELECT SALARY_ACC_NUMBER FROM SLOS_STAFF_TRN WHERE WINAME='" + processInstanceId
						+ "'";
				List<List<String>> listhrmsQuery = ifr.getDataFromDB(hrmsQuery);
				Log.consoleLog(ifr, "Log of hrmsQuery====>" + listhrmsQuery);
				if (!listhrmsQuery.isEmpty()) {
					loanAccountNumber = listhrmsQuery.get(0).get(0);
					Log.consoleLog(ifr, "loanAccountNumber====>" + loanAccountNumber);
				}
                //String calculatedStampCharges = calculateStampCharges(ifr,tenure,loanAmount);
			
				
				getTotalCharges = pcm.getTotalCharges(ifr, schemeId, loanAmount, stateCode, tenure, loanAccountNumber,
						selectedBranchCode,calculatedStampCharges);
			}
			if (journeyType.equalsIgnoreCase("HRMS")) {

			   // String calculatedStampCharges = calculateStampCharges(ifr,tenure,loanAmount);
				getTotalCharges = pcm.getTotalCharges(ifr, schemeId, loanAmount, stateCode, tenure, sBACCNUMBER,
						selectedBranchCode,calculatedStampCharges);

				String queryForOdUtilized = "SELECT LOAN_AMOUNT, OD_UTILIZED FROM SLOS_STAFF_TRN WHERE WINAME='"
						+ processInstanceId + "'";
				Log.consoleLog(ifr, "queryForOdUtilized" + queryForOdUtilized);
				List<List<String>> resForOdUtilized = ifr.getDataFromDB(queryForOdUtilized);
				Log.consoleLog(ifr, "resForOdUtilized===>" + resForOdUtilized);
				if (!resForOdUtilized.isEmpty()) {
					loanAmt = Double.parseDouble(resForOdUtilized.get(0).get(0));
					odUtilized = Double.parseDouble(resForOdUtilized.get(0).get(1));
				}

				if (loanType.contains("Renewal")) {
					Log.consoleLog(ifr, "Inside condition to remove object for stamp amt====>");
					Log.consoleLog(ifr, "Inside condition to remove object====>" + getTotalCharges);
					getTotalCharges.remove(3);
					getTotalCharges.remove(1);
					JSONObject jsonObject = (JSONObject) getTotalCharges.get(0);
					Log.consoleLog(ifr, "Log of jsonObject====>" + jsonObject);
					String transactionAMt = jsonObject.get("TransactionAmount").toString();
					Log.consoleLog(ifr, "Log of transactionAMt====>" + transactionAMt);
					JSONObject jsonObject1 = (JSONObject) getTotalCharges.get(1);
					Log.consoleLog(ifr, "Log of jsonObject1====>" + jsonObject1);
					String transactionAMt1 = jsonObject1.get("TransactionAmount").toString();
					Log.consoleLog(ifr, "Log of transactionAMt1====>" + transactionAMt1);
					jsonObject.put("TransactionAmount", transactionAMt1);
					Log.consoleLog(ifr, "Log of jsonObject2====>" + jsonObject);
					getTotalCharges.add(0, jsonObject);
					getTotalCharges.remove(1);
				}
			}

			JSONObject requestMain = new JSONObject();
			JSONObject input = new JSONObject();
			JSONObject SessionContext = new JSONObject();

			JSONObject SupervisorContext = new JSONObject();
			SupervisorContext.put("PrimaryPassword", "");
			SupervisorContext.put("UserId", S_UserId);
			SessionContext.put("SupervisorContext", SupervisorContext);

			SessionContext.put("BankCode", BankCode);
			SessionContext.put("Channel", Channel);
			SessionContext.put("ExternalBatchNumber", "");
			SessionContext.put("ExternalReferenceNo", cm.getCBSExternalReferenceNo());
			SessionContext.put("ExternalSystemAuditTrailNumber", "");
			SessionContext.put("LocalDateTimeText", "");
			SessionContext.put("OriginalReferenceNo", "");
			SessionContext.put("OverridenWarnings", "129");
			SessionContext.put("PostingDateText", "");
			SessionContext.put("ServiceCode", "");
			SessionContext.put("SessionTicket", "");
			SessionContext.put("TransactionBranch", selectedBranchCode);
			SessionContext.put("UserId", UserId);
			SessionContext.put("UserReferenceNumber", cm.getCBSExternalReferenceNo());
			SessionContext.put("ValueDateText", "");

			input.put("SessionContext", SessionContext);
			input.put("MultipleFundsTransferRequestDTO", getTotalCharges);
			input.put("DebitTransactionCount", "1");
			input.put("CreditTransactionCount", getTotalCharges.size() - 1);
			input.put("makerId", UserId);
			input.put("checkerId", S_UserId);
			requestMain.put("input", input);

			request = requestMain.toString();
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject TextJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(TextJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				// try {
				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {

				} else {
					String[] ErrorData = CheckError.split("#");
					apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}
//	                } catch (Exception e) {
//	                    Log.consoleLog(ifr, "Exception/GetAPIErrorResponse2" + e);
//	                }

			} else {
				response = "No response from the server.";
				apiErrorMessage = "FAIL";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
				status = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = "FAIL";
				status = RLOS_Constants.ERROR;

			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
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

	public String modifyLADODLoanAccount(IFormReference ifr, String processInstanceId, String productCode)
			throws ParseException, java.text.ParseException {
		Log.consoleLog(ifr, "#getLADODLoanAccountDetails=================");
		Log.consoleLog(ifr, "productCode===============" + productCode);
		String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String APIStatus = "";
		String apiName = "ODLIMITMODIFICATION";
		String serviceName = "CBS_" + apiName;
		String status = "";
		String codBaselCode = "";
		String flgMntStatus = "";

		String extUniqueRefId = generateUniqueID(17);

		String BankCode = pcm.getConstantValue(ifr, "CBSODRENWLOANACC", "BANKCODE");
		String Channel = pcm.getConstantValue(ifr, "CBSODRENWLOANACC", "CHANNEL");
		String UserId = pcm.getConstantValue(ifr, "CBSODRENWLOANACC", "USERID");
		String sancAuthority = "";

		String queryForBaselCode = "select cod_basel_code, flg_mnt_status from ba_prod_basel_cust_type where cod_prod = '"
				+ productCode + "'";
		Log.consoleLog(ifr, "Salary CODE BASEL CODE query===>" + queryForBaselCode);
		List<List<String>> resultForBaselCode = ifr.getDataFromDB(queryForBaselCode);
		Log.consoleLog(ifr, "resultForBaselCode===>" + resultForBaselCode);
		if (!resultForBaselCode.isEmpty()) {
			flgMntStatus = resultForBaselCode.get(0).get(1);
			if (!flgMntStatus.isEmpty() && flgMntStatus != null && flgMntStatus.equalsIgnoreCase("A")) {
				codBaselCode = resultForBaselCode.get(0).get(0);
			}
		}

		Date currDate = new Date();
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMdd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String SanctionDate = dateForm.format(currDate);
		LocalDate date = LocalDate.parse(SanctionDate, formatter);
		LocalDate newDate = date.plusYears(2).minusDays(1);
		String tenure = newDate.format(formatter);

		String codMixquery = "select * from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where PROD_CODE='" + productCode + "'";
		List<List<String>> codMixqueryList = ifr.getDataFromDB(codMixquery);
		Log.consoleLog(ifr, "codMixquery===============" + codMixquery);
		String CodMisTxn1 = "";
		String CodMisTxn2 = "";
		String CodMisTxn3 = "";
		String CodMisTxn4 = "";
		String CodMisTxn5 = "";
		String CodMisTxn6 = "";
		String CodMisTxn7 = "";
		String CodMisTxn8 = "";
		String CodMisTxn9 = "";
		String CodMisTxn10 = "";
		String CodMisTxn11 = "";
		String CodMisComp1 = "";
		String IntrestIndx = "";
		String RatLimLevel = "";
		String LimitSanctionBy = "";
		String ProcessChgsCollected = "";
		String ConcessionPermitted = "";

		if (codMixqueryList.size() > 0) {
			CodMisTxn1 = codMixqueryList.get(0).get(1);
			// CodMisTxn2 = codMixqueryList.get(0).get(2);
			CodMisTxn2 = codMixqueryList.get(0).get(2);
			CodMisTxn3 = codMixqueryList.get(0).get(3);
			CodMisTxn4 = codMixqueryList.get(0).get(4);
			CodMisTxn5 = codMixqueryList.get(0).get(5);
			CodMisTxn6 = codMixqueryList.get(0).get(6);
			CodMisTxn7 = codMixqueryList.get(0).get(7);
			CodMisTxn8 = codMixqueryList.get(0).get(8);
			CodMisTxn9 = codMixqueryList.get(0).get(9);
			CodMisTxn10 = codMixqueryList.get(0).get(10);
			CodMisTxn11 = codMixqueryList.get(0).get(11);
			CodMisComp1 = codMixqueryList.get(0).get(12);
			IntrestIndx = codMixqueryList.get(0).get(13);
			RatLimLevel = codMixqueryList.get(0).get(14);
			LimitSanctionBy = codMixqueryList.get(0).get(15);
			ProcessChgsCollected = codMixqueryList.get(0).get(16);
			ConcessionPermitted = codMixqueryList.get(0).get(17);
		}
		if (!Optional.ofNullable(RatLimLevel).isPresent()) {
			RatLimLevel = "";
		}

		String selectedBranchCode = "";
		String Query2 = "SELECT DISB_BRANCH FROM SLOS_TRN_LOANDETAILS WHERE PID= '" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "branch-> " + Query2);
		List<List<String>> brqueryresult = ifr.getDataFromDB(Query2);

		if (!brqueryresult.isEmpty()) {
			selectedBranchCode = brqueryresult.get(0).get(0);
		}

		String appRefNo = "";
		String appRefNoQuery1 = "SELECT APPLICATIONNUMBER FROM LOS_EXT_TABLE WHERE PID= '" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "appRefNoQuery1-> " + appRefNoQuery1);
		List<List<String>> queryappRefNoQuery1 = ifr.getDataFromDB(appRefNoQuery1);

		if (!queryappRefNoQuery1.isEmpty()) {
			appRefNo = queryappRefNoQuery1.get(0).get(0);
		}
		String renewalMode = "NONE";
		String limit = "";
		String Query3 = "SELECT LOAN_AMOUNT FROM SLOS_STAFF_TRN WHERE WINAME= '" + ProcessInsanceId + "'";
		Log.consoleLog(ifr, "limit amount-> " + Query3);
		List<List<String>> limitQuery1 = ifr.getDataFromDB(Query3);

		if (!limitQuery1.isEmpty()) {
			limit = limitQuery1.get(0).get(0);
		}

		String loanAccNum = "";
		String limitNumber = "";
		String queryLoanAmt = "SELECT LOAN_ACC_NUMBER, LIMITNUMBER FROM SLOS_ALL_ACTIVE_PRODUCT WHERE WINAME='"
				+ ProcessInsanceId + "' AND PRODUCTCODE IN ('253','254','1129')";
		Log.consoleLog(ifr, "getHomeBranchCode query==>" + queryLoanAmt);
		List<List<String>> resqueryLoanAmt = ifr.getDataFromDB(queryLoanAmt);
		if (!resqueryLoanAmt.isEmpty()) {
			loanAccNum = resqueryLoanAmt.get(0).get(0).trim();
			limitNumber = resqueryLoanAmt.get(0).get(1);
		}

		java.util.Date dateTimeFormat = new java.util.Date();
		SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
		String strCurDateTime = sDateTime.format(dateTimeFormat);

		String extUniqueRefIdmod = generateUniqueID(17);
		String request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
				+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
				+ "                 \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \"" + BankCode
				+ "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
				+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \"\",\n"
				+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
				+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
				+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
				+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
				+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "            \"UserId\": \""
				+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
				+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
				+ extUniqueRefIdmod + "\",\n" + "        \"DatProcessingCharges\": \"\",\n"
				+ "        \"CodMisTxn1\": \"" + CodMisTxn1 + "\",\n" + "        \"CodMisTxn2\": \"" + CodMisTxn2
				+ "\",\n" + "        \"CodMisTxn3\": \"" + CodMisTxn3 + "\",\n" + "        \"CodMisTxn4\": \""
				+ CodMisTxn4 + "\",\n" + "        \"CodMisTxn5\": \"" + CodMisTxn5 + "\",\n"
				+ "        \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n" + "        \"CodMisTxn7\": \"" + CodMisTxn7
				+ "\",\n" + "        \"CodMisTxn8\": \"" + CodMisTxn8 + "\",\n" + "        \"CodMisTxn9\": \""
				+ CodMisTxn9 + "\",\n" + "        \"CodMisTxn10\": \"" + CodMisTxn10 + "\",\n"
				+ "        \"CodMisTxn11\": \"" + CodMisTxn11 + "\",\n" + "        \"CodMisComp1\": \"" + CodMisComp1
				+ "\",\n" + "        \"CodMisComp2\": \"\",\n" + "        \"CodMisComp3\": \"\",\n"
				+ "        \"CodMisComp4\": \"\",\n" + "        \"CodMisComp5\": \"\",\n"
				+ "        \"CodMisComp6\": \"\",\n" + "        \"CodMisComp7\": \"\",\n"
				+ "        \"CodMisComp8\": \"\",\n" + "        \"CodMisComp9\": \"\",\n"
				+ "        \"CodMisComp10\": \"\",\n" + "        \"InterestIndx\": \"" + IntrestIndx + "\",\n"
				+ "        \"RatLimLevel\": \"" + RatLimLevel + "\",\n" + "        \"DatODPapers\": \"\",\n"
				+ "        \"RatIntConcession\": \"\",\n" + "        \"CollateralID_1\": \"\",\n"
				+ "        \"CollateralType_1\": \"\",\n" + "        \"CollateralAmt_1\": \"\",\n"
				+ "        \"CollateralDegree_1\": \"\",\n" + "        \"CollateralDesc_1\": \"\",\n"
				+ "        \"CollateralID_2\": \"\",\n" + "        \"CollateralType_2\": \"\",\n"
				+ "        \"CollateralAmt_2\": \"\",\n" + "        \"CollateralDegree_2\": \"\",\n"
				+ "        \"CollateralMargin_2\": \"\",\n" + "        \"CollateralDesc_2\": \"\",\n"
				+ "        \"CollateralID_3\": \"\",\n" + "        \"CollateralType_3\": \"\",\n"
				+ "        \"CollateralAmt_3\": \"\",\n" + "        \"CollateralDegree_3\": \"\",\n"
				+ "        \"CollateralMargin_3\": \"\",\n" + "        \"CollateralDesc_3\": \"\",\n"
				+ "        \"CollateralID_4\": \"\",\n" + "        \"CollateralType_4\": \"\",\n"
				+ "        \"CollateralAmt_4\": \"\",\n" + "        \"CollateralDegree_4\": \"\",\n"
				+ "        \"CollateralMargin_4\": \"\",\n" + "        \"CollateralDesc_4\": \"\",\n"
				+ "        \"CollateralID_5\": \"\",\n" + "        \"CollateralType_5\": \"\",\n"
				+ "        \"CollateralAmt_5\": \"\",\n" + "        \"CollateralDegree_5\": \"\",\n"
				+ "        \"CollateralMargin_5\": \"\",\n" + "        \"CollateralDesc_5\": \"\",\n"
				+ "        \"CollateralID_6\": \"\",\n" + "        \"CollateralType_6\": \"\",\n"
				+ "        \"CollateralAmt_6\": \"\",\n" + "        \"CollateralMargin_6\": \"\",\n"
				+ "        \"CollateralDesc_6\": \"\",\n" + "        \"CollateralID_7\": \"\",\n"
				+ "        \"CollateralType_7\": \"\",\n" + "        \"CollateralAmt_7\": \"\",\n"
				+ "        \"CollateralDegree_7\": \"\",\n" + "        \"CollateralMargin_7\": \"\",\n"
				+ "        \"CollateralDesc_7\": \"\",\n" + "        \"CollateralID_8\": \"\",\n"
				+ "        \"CollateralType_8\": \"\",\n" + "        \"CollateralAmt_8\": \"\",\n"
				+ "        \"CollateralDegree_8\": \"\",\n" + "        \"CollateralMargin_8\": \"\",\n"
				+ "        \"CollateralDesc_8\": \"\",\n" + "        \"CollateralID_9\": \"\",\n"
				+ "        \"CollateralType_9\": \"\",\n" + "        \"CollateralAmt_9\": \"\",\n"
				+ "        \"CollateralDegree_9\": \"\",\n" + "        \"CollateralMargin_9\": \"\",\n"
				+ "        \"CollateralDesc_9\": \"\",\n" + "        \"CollateralID_10\": \"\",\n"
				+ "        \"CollateralType_10\": \"\",\n" + "        \"CollateralAmt_10\": \"\",\n"
				+ "        \"CollateralDegree_10\": \"\",\n" + "        \"CollateralMargin_10\": \"\",\n"
				+ "        \"CollateralDesc_10\": \"\",\n" + "        \"AccountNo\": \"" + loanAccNum + "\",\n"
				+ "        \"CollateralMargin_1\": \"\",\n" + "        \"CollateralDegree_6\": \"\",\n"
				+ "        \"LimitNumber\": \"" + limitNumber + "\",\n" + "        \"LimitAmount\": \"" + limit
				+ "\",\n" + "        \"DatLimitEnd\": \"" + tenure + "\",\n" + "        \"LimitSanctionBy\": \"75\",\n"
				+ "        \"SanctionRefNo\": \"" + appRefNo + "\",\n" + "        \"AmtProcessChg\": \"\",\n"
				+ "        \"DatProcessChg\": \"\",\n" + "        \"LimitType\": \"1\",\n"
				+ "        \"CollAddModDel_1\": \"\",\n" + "        \"CollAddModDel_2\": \"\",\n"
				+ "        \"CollAddModDel_3\": \"\",\n" + "        \"CollAddModDel_4\": \"\",\n"
				+ "        \"CollAddModDel_5\": \"\",\n" + "        \"CollAddModDel_6\": \"\",\n"
				+ "        \"CollAddModDel_7\": \"\",\n" + "        \"CollAddModDel_8\": \"\",\n"
				+ "        \"CollAddModDel_9\": \"\",\n" + "        \"CollAddModDel_10\": \"\",\n"
				+ "        \"LinkCollateral_1\": \"\",\n" + "        \"LinkCollateral_2\": \"\",\n"
				+ "        \"LinkCollateral_3\": \"\",\n" + "        \"LinkCollateral_4\": \"\",\n"
				+ "        \"LinkCollateral_5\": \"\",\n" + "        \"LinkCollateral_6\": \"\",\n"
				+ "        \"LinkCollateral_7\": \"\",\n" + "        \"LinkCollateral_8\": \"\",\n"
				+ "        \"LinkCollateral_9\": \"\",\n" + "        \"LinkCollateral_10\": \"\",\n"
				+ "        \"ProcessChgCollected\": \"" + ProcessChgsCollected + "\",\n"
				+ "        \"concessionPermitted\": \"" + ConcessionPermitted + "\",\n"
				+ "        \"dateOfConcession\": \"\",\n" + "        \"concessionRatePermittedBy\": \"\",\n"
				+ "        \"SanctionDate\": \"" + SanctionDate + "\",\n" + "        \"BaselCustType\": \""
				+ codBaselCode + "\",\n" + "        \"renewalMode\": \"" + renewalMode + "\"\n" + "    }\n" + "}";

		Log.consoleLog(ifr, "Request====>" + request);
		String response = cm.getWebServiceResponse(ifr, apiName, request);
		Log.consoleLog(ifr, "Response===>" + response);
		String ErrorCode = "";
		String ErrorMessage = "";

		if (!response.equalsIgnoreCase("{}")) {
			JSONParser parser = new JSONParser();
			JSONObject OutputJSON = (JSONObject) parser.parse(response);
			JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyJSON = (JSONObject) parser.parse(body);
			JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInsanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				String Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET LOAN_ACCOUNTNO= '" + loanAccNum.trim()
						+ "', ACCOUNT_CREATEDDATE='" + strCurDateTime + "', SANCTION_AMOUNT='" + limit
						+ "',DISB_AMOUNT='" + limit + "',DISB_DATE='" + strCurDateTime + "',DISB_BRANCH='"
						+ selectedBranchCode + "' WHERE PID= '" + processInstanceId + "'"
						+ "AND (LOAN_ACCOUNTNO IS NULL OR LOAN_ACCOUNTNO = '')";
				Log.consoleLog(ifr, "UPFATE LOANACCOUNT disbur details-> " + Query1);
				ifr.saveDataInDB(Query1);

			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
		} else {
			response = "No response from the server.";
			ErrorMessage = "FAIL";
		}

		try {
			// String APIName = "CBS_DisbursementEnquiry";

			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				status = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				status = RLOS_Constants.ERROR;
			}

			if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return APIStatus;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, ProcessInsanceId, serviceName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		}

		return status + ":" + ErrorMessage;
	}

	public String getLoanAccountDetailsHL(IFormReference ifr, String processInstanceId, String productCode,
			String customerId, String loanAmount, String tenure, String sanctionExpiryDate, String journeyType) {

		// JSONObject message = new JSONObject();
		Log.consoleLog(ifr, "#Execute_CBSLoanAccountCreation=================");

		String apiName = "LoanAccountCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		String LoanAccNumber = "";
		try {

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formattedDate = dateFormat.format(currentDate);

			String applicationNumber = pcm.getApplicationRefNumber(ifr);// Added by Ahmed on 24/06/2024 for getting
																		// RefNo from CommonMethods
			Log.consoleLog(ifr, "applicationNumber==>" + applicationNumber);

//            productCode = pcm.mGetProductCode(ifr);
//            String subProductCode = pcm.mGetSubProductCode(ifr);
//            Log.consoleLog(ifr, "productCode=======>" + productCode);
//            Log.consoleLog(ifr, "subProductCode====>" + subProductCode);

			// String ScheduleCode = pcm.getConstantValue(ifr, "CBSLOANSCH", "SCODE");
			String ScheduleCode = "";
			String SancAuthority = "";
			Double InterestVariance = 0.0;
			String takeOverLoan = "";
			String strProductCode = "";
			String purposeCode = "";
			String purposeName = "";
			String baselCustType = "";
			Double variance = 0.0;
			Double concession = 0.0;
			// Modfied by Ahme don 09-09-2024
			// String SancDate = AcceleratorBaseCode.SanctionDate;
			String SancDate = "";
			String sanctionDate = "";

			String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loansummary " + "WHERE WINAME='"
					+ processInstanceId + "' AND ROWNUM=1";
			Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);

			List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");

			if (!ResultSanctionDate.isEmpty()) {
				sanctionDate = ResultSanctionDate.get(0).get(0);
				Log.consoleLog(ifr, "sanctionDate==>" + sanctionDate);

				try {
					SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					Date parsedDate = originalFormat.parse(sanctionDate);

					// Format to yyyyMMdd
					SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					SancDate = targetFormat.format(parsedDate); // Original date in yyyyMMdd
				} catch (Exception e) {
					Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
				}

			} else {
				return "error,No sanction date found";
			}

			SancAuthority = pcm.getSancAuhority(ifr, "HL");
//                InterestVariance = pcm.getInterestVariance(ifr);

			String flgMntStatus = "";
			String queryForBaselCode = "select cod_basel_code, flg_mnt_status from ba_prod_basel_cust_type where cod_prod = '"
					+ productCode + "'";
			Log.consoleLog(ifr, "Salary CODE BASEL CODE query===>" + queryForBaselCode);
			List<List<String>> resultForBaselCode = ifr.getDataFromDB(queryForBaselCode);
			Log.consoleLog(ifr, "resultForBaselCode===>" + resultForBaselCode);
			if (!resultForBaselCode.isEmpty()) {
				flgMntStatus = resultForBaselCode.get(0).get(1);
				if (!flgMntStatus.isEmpty() && flgMntStatus != null && flgMntStatus.equalsIgnoreCase("A")) {
					baselCustType = resultForBaselCode.get(0).get(0);
				}
			}

			String queryproductandScheduleCode = "SELECT a.SUB_PRODUCT_CODE, a.SCHEDULE_CODE, c.PURPOSENAME, c.PURPOSECODE, b.WHETHER_PROP_TAKEOVER FROM SLOS_HOME_PRODUCT_SHEET a JOIN SLOS_STAFF_HOME_TRN b ON TRIM(a.sub_product) = TRIM(b.hl_product) JOIN SLOS_HOME_PURPOSE c ON TRIM(c.purposename) = TRIM(b.hl_purpose) WHERE b.winame = '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryproductandScheduleCode===>" + queryproductandScheduleCode);
			List<List<String>> listqueryproductandScheduleCode = ifr.getDataFromDB(queryproductandScheduleCode);
			Log.consoleLog(ifr, "listqueryproductandScheduleCode===>" + listqueryproductandScheduleCode);
			if (!listqueryproductandScheduleCode.isEmpty()) {
				strProductCode = listqueryproductandScheduleCode.get(0).get(0);
				ScheduleCode = listqueryproductandScheduleCode.get(0).get(1);
				purposeName = listqueryproductandScheduleCode.get(0).get(2);
				purposeCode = listqueryproductandScheduleCode.get(0).get(3);
				takeOverLoan = listqueryproductandScheduleCode.get(0).get(4).equalsIgnoreCase("Yes") ? "Y" : "N";
			}
//           implement intrest level variance, concession,takeoverloan

//            String praposedFaciltyQuery = ConfProperty.getQueryScript("getLoanDetailsQueryHL").replaceAll("#PID#", processInstanceId);
//            //"SELECT * FROM LOS_INTEGRATION_CBS_STATUS WHERE TRANSACTION_ID = '#PID#' AND API_NAME ='CIM09'  AND API_STATUS ='SUCCESS'";
//            Log.consoleLog(ifr, " praposedFaciltyQuery  query: " + praposedFaciltyQuery);
//            List<List<String>> praposedFaciltyResult = cf.mExecuteQuery(ifr, praposedFaciltyQuery, "praposedFaciltyQuery");
//            if (!praposedFaciltyQuery.isEmpty()) {
			// takeOverLoan = praposedFaciltyResult.get(0).get(0).equalsIgnoreCase("F") ?
			// "N" : "Y";
			takeOverLoan = "N";
			// ScheduleCode = praposedFaciltyResult.get(0).get(1);
			// variance = Double.parseDouble(praposedFaciltyResult.get(0).get(2));
			// strProductCode = praposedFaciltyResult.get(0).get(3);
			// purposeCode = praposedFaciltyResult.get(0).get(4);
			// purposeName = praposedFaciltyResult.get(0).get(5);
			// baselCustType = praposedFaciltyResult.get(0).get(6);
			// concession=Double.parseDouble(praposedFaciltyResult.get(0).get(7));

//            }
//            Log.consoleLog(ifr, " takeOverLoan : " + takeOverLoan +" ScheduleCode : " + ScheduleCode+
//                    " variance : " + variance+" strProductCode : " + strProductCode+" baselCustType : " + baselCustType+
//                    " concession : " + concession);
//            InterestVariance = variance - concession;

			String CustRel = "";
			String custJointRel = "";
			String checkData = "Select  pid,Applicanttype,customerid from los_nl_basic_info  WHERE PID= '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, " checkCountData : " + checkData);
			List<List<String>> listCheck = ifr.getDataFromDB(checkData);
			Log.consoleLog(ifr, "listCheck : " + listCheck);
			StringBuilder multiCoborowwer = new StringBuilder();
			if (!listCheck.isEmpty()) {
				if (listCheck.size() == 1 && listCheck.get(0).get(1).contains("B")) {
					CustRel = "SOW";
					Log.consoleLog(ifr,
							"Only Borrower=======>" + listCheck.get(0).get(1) + "---->" + listCheck.get(0).get(2));
					multiCoborowwer.append("         \"CustomerID").append("\": \"").append(listCheck.get(0).get(2))
							.append("\",\n");
					multiCoborowwer.append("        \"CustRel").append("\": \"").append(CustRel).append("\",\n");

				} else {
					// Always handle "B" (Borrower) first
					for (int i = 0; i < listCheck.size(); i++) {
						String tag = listCheck.get(i).get(1);
						customerId = listCheck.get(i).get(2);

//						if (tag.contains("B")) {
//							CustRel = "JAF";
//							Log.consoleLog(ifr, " Borrower=======>" + tag + "---->" + customerId);
//							multiCoborowwer.append("         \"CustomerID\": \"").append(customerId).append("\",\n");
//							multiCoborowwer.append("        \"CustRel\": \"").append(CustRel).append("\",\n");
//							break; // Only one borrower assumed
//						}
						if ("B".equalsIgnoreCase(tag)) {
						    CustRel = "JAF";
						    Log.consoleLog(ifr, " Borrower=======>" + tag + "---->" + customerId);
						    multiCoborowwer.append(" \"CustomerID\": \"").append(customerId).append("\",\n");
						    multiCoborowwer.append(" \"CustRel\": \"").append(CustRel).append("\",\n");
						    break;
						}
					}
					int jointCount = 0;
					// Then handle Joint and Guarantors
					for (int i = 0; i < listCheck.size(); i++) {
						String tag = listCheck.get(i).get(1);
						customerId = listCheck.get(i).get(2);

						if (tag.contains("CB") || tag.contains("G")) {
							String custJointRelationship = tag.contains("CB") ? "JAO" : "GUA";

							if (jointCount == 0) {
								multiCoborowwer.append("         \"CustomerIDJoint\": \"").append(customerId)
										.append("\",\n");
								multiCoborowwer.append("        \"CustJointRel\": \"").append(custJointRelationship)
										.append("\",\n");
							} else {
								multiCoborowwer.append("         \"CustomerIDJoint").append(jointCount + 2)
										.append("\": \"").append(customerId).append("\",\n");
								multiCoborowwer.append("        \"CustJointRel").append(jointCount + 2).append("\": \"")
										.append(custJointRelationship).append("\",\n");
							}
							jointCount++;
						}
					}
				}

			}

			Log.consoleLog(ifr, "Loan account creation ScheduleCode====>" + ScheduleCode);
//            String strProductCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PRODUCTCODE");
			String S_UserId = pcm.getConstantValue(ifr, "CBSINQCR", "S_USERID");
			String BankCode = pcm.getConstantValue(ifr, "COLLATERAL", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "COLLATERAL", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "COLLATERAL", "USERID");
			// New as of 02-01-2024
//            String Purpose = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PURPOSE");

//            String StatusOfProcessingCharges = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PCSTATUS");
//            String SwitchDueDate = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SWITCHDUEDATE");
			String StatusOfProcessingCharges = AcceleratorConstants.STATUSOFPROCESSINGCHARGES;
			String SwitchDueDate = AcceleratorConstants.SWITCHDUEDATE;

			// if product type = budget
			// take from masters. otherwise below configs
//            String CodMisTxn1 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn1");
//            String CodMisTxn2 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn2");
//            String CodMisTxn3 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn3");
//            String CodMisTxn4 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn4");
//            String CodMisTxn5 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn5");
//            String CodMisTxn6 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn6");
//            String CodMisTxn7 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn7");
//            String CodMisTxn8 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn8");
//            String CodMisTxn9 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn9");
//            String CodMisTxn10 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn10");
//            String CodMisTxn11 = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "CodMisTxn11");

			String CodMisTxn1 = "";
			String CodMisTxn2 = "";
			String CodMisTxn3 = "";
			String CodMisTxn4 = "";
			String CodMisTxn5 = "";
			String CodMisTxn6 = "";
			String CodMisTxn7 = "";
			String CodMisTxn8 = "";
			String CodMisTxn9 = "";
			String CodMisTxn10 = "";
			String CodMisTxn11 = "";
			String CodMisComp1 = "";

			String query = "select RBI_PURPOSE_CODE,SECTOR,RETAIL_BASEL_II_CUSTOMER_TYPE,SCHEMES,GUARANTEE_COVER,BSR_CODE,SSISUBSEC,STATUSIB,PRI_SECTOR_N_PRI_SECTOR,SPECIAL_BENEFICIARIES,SUB_SCHEME,RAH from LOS_L_BAM83 "
					+ "where pid ='" + processInstanceId + "'";
			List<List<String>> Result = cf.mExecuteQuery(ifr, query, " BAM 83 Update ");

			if (!Result.isEmpty()) {
				CodMisTxn1 = Result.get(0).get(0);
				CodMisTxn2 = Result.get(0).get(1);
				CodMisTxn3 = Result.get(0).get(2);
				CodMisTxn4 = Result.get(0).get(3);
				CodMisTxn5 = Result.get(0).get(4);
				CodMisTxn6 = Result.get(0).get(5);
				CodMisTxn7 = Result.get(0).get(6);
				CodMisTxn8 = Result.get(0).get(7);
				CodMisTxn9 = Result.get(0).get(8);
				CodMisTxn10 = Result.get(0).get(9);
				CodMisTxn11 = Result.get(0).get(10);
				CodMisComp1 = Result.get(0).get(11);
			} else {
				return "BAM83 ERROR";
			}

			// String CodMisComp1 = pcm.getParamConfig(ifr, productCode, subProductCode,
			// "LOANACCCREATE", "CodMisComp1");

			String propertyLocation = pcm.getParamConfigSHL(ifr, "LOANACCCREATE", "PROPERTYLOCATION");
			Log.consoleLog(ifr, "propertyLoacation=======>" + propertyLocation);

			// added by monesh Kumar on 16-06-2024 for updating the CIM09 and BAM 83
			// Modified by Ahmed on 24-06-2024 for getting MIS Data status from Demography
			// if (!ifr.getActivityName().equalsIgnoreCase("Portal")) {

			// String custMISupdateStatus = pcm.getMISStatusFromDemography(ifr);
			// if (custMISupdateStatus.equalsIgnoreCase("Incomplete")) {
//                String CIM09CountQuery = ConfProperty.getQueryScript("CIM09SuccessCount").replaceAll("#PID#", processInstanceId);
//                //"SELECT * FROM LOS_INTEGRATION_CBS_STATUS WHERE TRANSACTION_ID = '#PID#' AND API_NAME ='CIM09'  AND API_STATUS ='SUCCESS'";
//                Log.consoleLog(ifr, " CIM09SuccessCount  query: " + CIM09CountQuery);
//                List<List<String>> CIM09CountQueryResult = cf.mExecuteQuery(ifr, CIM09CountQuery, "CIM09SuccessCount");
//                if (CIM09CountQueryResult.isEmpty()) {
//                    CIM09 objCIM09 = new CIM09();
//                    String cimupdate = objCIM09.updateMIS(ifr);
//
//                    if (cimupdate.contains(RLOS_Constants.ERROR)) {
//                        return RLOS_//Constants.ERROR + ": " + cimupdate;
//                    }
//                }

//                String query = ConfProperty.getQueryScript("CODMISDATAQUERY").replaceAll("#WINAME#", processInstanceId);
//                List<List<String>> Result = cf.mExecuteQuery(ifr, query, " BAM 83 Update ");
//                BudgetDisbursementScreen bds = new BudgetDisbursementScreen();
//                if (!Result.isEmpty() && !(bds.checkbamdata(ifr, "CODMISDATAQUERY", "WINAME").contains(RLOS_Constants.ERROR))) {
//
//                    CodMisTxn1 = Result.get(0).get(0);
//                    CodMisTxn2 = Result.get(0).get(1);
//                    CodMisTxn3 = Result.get(0).get(2);
//                    CodMisTxn4 = Result.get(0).get(3);
//                    CodMisTxn5 = Result.get(0).get(4);
//                    CodMisTxn6 = Result.get(0).get(5);
//                    CodMisTxn7 = Result.get(0).get(6);
//                    CodMisTxn8 = Result.get(0).get(7);
//                    CodMisTxn9 = Result.get(0).get(8);
//                    CodMisTxn10 = Result.get(0).get(9);
//                    CodMisTxn11 = Result.get(0).get(10);
//                    CodMisComp1 = !Result.get(0).get(11).isEmpty()?Result.get(0).get(11).replaceFirst("^0+(?!$)", ""):"";
//
//                } else {
//                    return "BAM83 ERROR";
//                }
//
			// }

			// String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId,
			// journeyType);//Commented by Ahmed on 24/06/2024 for getting
			// BranchCodefromSalaryAccount
//            String salaryAccountNumber = pcm.getPrimarySalaryAccountNo(ifr, journeyType);
//            if (salaryAccountNumber.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR + ":" + apiErrorMessage;
//            }
//            Log.consoleLog(ifr, "salaryAccountNumber=======>" + salaryAccountNumber);

			String salaryAccountNumber = "";
			String queryForsalaryAccountNumber = "select SALARY_ACC_NUMBER from slos_staff_home_trn where winame='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryForsalaryAccountNumber==>" + queryForsalaryAccountNumber);
			List<List<String>> resForsalaryAccountNumber = ifr.getDataFromDB(queryForsalaryAccountNumber);
			if (!resForsalaryAccountNumber.isEmpty()) {
				salaryAccountNumber = resForsalaryAccountNumber.get(0).get(0);
			}

			String tBranch = "";
			String queryForTBranch = " SELECT branchcode FROM LOS_M_BRANCH WHERE  BRANCHNAME IN  (SELECT branch_NAME FROM SLOS_STAFF_home_trn WHERE WINAME = '"
					+ processInstanceId + "')";
			Log.consoleLog(ifr, "queryForTBranch==>" + queryForTBranch);
			List<List<String>> resqueryForTBranch = ifr.getDataFromDB(queryForTBranch);
			if (!resqueryForTBranch.isEmpty()) {
				tBranch = resqueryForTBranch.get(0).get(0);
			}

			// Added by Ahmed on 24-06-2024 for Budget Parameters
			String coBorrowerCustID = "";

			String multipleCollateralId = pcm.getCollateralId(ifr);// Added by Ahmed on 01-07-2024 for getting
																	// CollateralId

			int count = 0;
			String applicantType = "";
			String applicantTypeQuery = "select count(*) from los_nl_basic_info where pid ='" + processInstanceId
					+ "' and APPLICANTTYPE in ('CB','G')";
			List<List<String>> applicantTypeData = cf.mExecuteQuery(ifr, applicantTypeQuery,
					"FETCHING applicantTypeQuery:");
			if (applicantTypeData != null && !applicantTypeData.isEmpty()) {
				applicantType = applicantTypeData.get(0).get(0);
				count = Integer.parseInt(applicantType);
			}
			Log.consoleLog(ifr, "applicantType=======>" + applicantType);
			Log.consoleLog(ifr, "count=======>" + count);
			if (count > 0) {
				coBorrowerCustID = pcm.getCustomerIDCB(ifr, "CB");
//                    custJointRel = "COB";
			}
			Log.consoleLog(ifr, "coBorrowerCustID=======>" + coBorrowerCustID);
			Log.consoleLog(ifr, "custJointRel=======>" + custJointRel);
			Log.consoleLog(ifr, "custRel=======>" + CustRel);

			// =========Added by Ahmed on 15-07-2024 for VL Loan Acc Creation
			// TagMapping=================//
			// String collateralCode = pcm.getParamConfig(ifr, productCode, subProductCode,
			// "LOANACCCREATE", "COLLCODE");
			String lendableMargin = "";
			String lendableAmount = "";
			String ltvRat = "";

//			String getCollateralDetailsQuery = "select COLLATERAL_ID,ASSET_SUBCATEGORY,MARKETVALUEOFPLOT,MARGINPERCENTAGE,LOCATION_OF_PROPERTY,MARKETVALUEOFPLOT,EMTDEBITACCOUNTNO from LOS_CAM_COLLATERAL_DETAILS WHERE PID='"
//					+ processInstanceId + "'";
			
			String getCollateralDetailsQuery = "select COLLATERAL_ID,ASSET_SUBCATEGORY,PROJECT_COST,MARGINPERCENTAGE,LOCATION_OF_PROPERTY,MARKETVALUEOFPLOT,EMTDEBITACCOUNTNO,eligible_amount from LOS_CAM_COLLATERAL_DETAILS WHERE PID='"
					+ processInstanceId + "'";
			List<List<String>> getCollateralDetailsQueryResult = cf.mExecuteQuery(ifr, getCollateralDetailsQuery,
					"LoanAccountCreation:getLoanAccountDetails -> assetTypesQuery: ");

//			String ltvRatioQuery = "SELECT overallltv FROM los_l_collat_summary WHERE PID='" + processInstanceId + "'";
//			List<List<String>> ltvRatioResult = cf.mExecuteQuery(ifr, ltvRatioQuery, "LTV ratio");
//			BigDecimal dlendMargin = BigDecimal.ZERO;
//			if (ltvRatioResult != null && !ltvRatioResult.isEmpty() && ltvRatioResult.get(0) != null
//				&& !ltvRatioResult.get(0).isEmpty()) {
//				ltvRat = ltvRatioResult.get(0).get(0);
//				if (ltvRat != null && !ltvRat.trim().isEmpty()) {
//					dlendMargin = new BigDecimal(ltvRat.trim());
//				}
//			}
			BigDecimal dlendltv = BigDecimal.ZERO;
			BigDecimal eligible_amt = new BigDecimal("0.0");
			BigDecimal project_cost = new BigDecimal("0.0");
			//BigDecimal TprimaryCollateral = new BigDecimal("0.0");
			// BigDecimal dlendMargin = new BigDecimal("0.0");
			// BigDecimal dlendMargin = new BigDecimal("90");
			BigDecimal dlendAmount = new BigDecimal("0.0");
			StringBuilder multiCollaterals = new StringBuilder();

			String queryForTprimaryCollateral = "select TOTCOLLAVAILBL,overallltv from LOS_L_COLLAT_SUMMARY where pid = '"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "queryForTprimaryCollateral==>" + queryForTprimaryCollateral);
			List<List<String>> resqueryForTprimaryCollateral = ifr.getDataFromDB(queryForTprimaryCollateral);
			if (!resqueryForTprimaryCollateral.isEmpty()) {
				//TprimaryCollateral = new BigDecimal(resqueryForTprimaryCollateral.get(0).get(0));
				dlendltv = new BigDecimal(resqueryForTprimaryCollateral.get(0).get(1));
			}
			dlendAmount = eligible_amt.add(new BigDecimal(getCollateralDetailsQueryResult.get(0).get(7)));

			for (int i = 0; i < getCollateralDetailsQueryResult.size(); i++) {
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(0));
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(1));
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(2));
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(3));
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(4));
				Log.consoleLog(ifr, "i=" + i + getCollateralDetailsQueryResult.get(i).get(5));
				project_cost = project_cost.add(new BigDecimal(getCollateralDetailsQueryResult.get(i).get(2)));
				// dlendMargin =new BigDecimal(getCollateralDetailsQueryResult.get(i).get(3));
				// dlendAmount = dlendMargin.multiply(project_cost).divide(new
				// BigDecimal("100"));

				Log.consoleLog(ifr, "project_cost=======>" + project_cost + "---->"
						+ getCollateralDetailsQueryResult.get(i).get(2));
				multiCollaterals.append("        \"Security_id").append(i + 1).append("\": \"")
						.append(getCollateralDetailsQueryResult.get(i).get(0)).append("\",\n");
				multiCollaterals.append("        \"CollateralCode").append(i + 1).append("\": \"")
						.append(getCollateralDetailsQueryResult.get(i).get(1)).append("\",\n");
				multiCollaterals.append("        \"Lendable_margin").append(i + 1).append("\": \"").append(dlendltv)
						.append("\",\n");
				multiCollaterals.append("        \"Lendable_amount").append(i + 1).append("\": \"").append(dlendAmount)
						.append("\",\n");

				Log.consoleLog(ifr, "lendableMargin=======>" + lendableMargin);
				Log.consoleLog(ifr, "lendableAmount=======>" + lendableAmount);
				// Log.consoleLog(ifr, "collateralCode=======>" + collateralCode);
				Log.consoleLog(ifr, "project_Cost==========>" + project_cost);
			}

			for (int i = getCollateralDetailsQueryResult.size() + 1; i <= 10; i++) {
				Log.consoleLog(ifr, " Empty i=" + i);
				multiCollaterals.append("        \"Security_id").append(i).append("\": \"").append("").append("\",\n");
				multiCollaterals.append("        \"CollateralCode").append(i).append("\": \"").append("")
						.append("\",\n");
				multiCollaterals.append("        \"Lendable_margin").append(i).append("\": \"").append("")
						.append("\",\n");
				multiCollaterals.append("        \"Lendable_amount").append(i).append("\": \"").append("")
						.append("\",\n");
			}
			Log.consoleLog(ifr, "multiCollaterals=" + multiCollaterals);

			// ==========================================================================================//

			String propertyOfLocation = "0";
			Log.consoleLog(ifr, "propertyOfLocation=" + propertyOfLocation);

			request = "{\n" + "	\"input\": {\n" + "		\"SessionContext\": {\n"
					+ "			\"SupervisorContext\": {\n" + "				\"PrimaryPassword\": \"\",\n"
					+ "				\"UserId\": \"" + UserId + "\"\n" + "			},\n"
					+ "			\"BankCode\": \"" + BankCode + "\",\n" + "			\"Channel\": \"" + Channel + "\",\n"
					+ "			\"ExternalBatchNumber\": \"\",\n" + "			\"ExternalReferenceNo\": \"\",\n"
					+ "			\"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "			\"LocalDateTimeText\": \"\",\n" + "			\"OriginalReferenceNo\": \"\",\n"
					+ "			\"OverridenWarnings\": \"\",\n" + "			\"PostingDateText\": \"\",\n"
					+ "			\"ServiceCode\": \"\",\n" + "			\"SessionTicket\": \"\",\n"
					+ "                 \"TransactionBranch\": \"" + tBranch + "\",\n"
					+ "                 \"UserId\": \"" + UserId + "\",\n" + "			\"ValueDateText\": \"\"\n"
					+ "		},\n" + "		\"ExtUniqueRefId\": \"" + formattedDate + "\",\n"
					+ "		\"BranchCode\": \"" + tBranch + "\",\n" + "		\"ProductCode\": \"" + strProductCode
					+ "\",\n" + multiCoborowwer.toString() + "         \"LoanAmount\": \"" + loanAmount + "\",\n"
					+ "         \"LoanTerm\": \"" + tenure + "\",\n" + "         \"ScheduleCode\": \"" + ScheduleCode
					+ "\",\n" + "         \"InterestVariance\": \"" + InterestVariance + "\",\n"
					+ "         \"Purpose\": \""
					+ purposeName.replaceAll("[^a-zA-Z ]", "").substring(0,
							Math.min(100, purposeName.replaceAll("[^a-zA-Z ]", "").length()))
					+ "\",\n" + "		\"TakeOverLoan\": \"" + takeOverLoan + "\",\n" // N, Y
					+ "		\"BankFIName\": \"\",\n" + "          \"DatOfSanction\": \"" + SancDate + "\",\n"
					+ "         \"SancAuthority\": \"" + SancAuthority + "\",\n" + "         \"SancReference\": \""
					+ applicationNumber + "\",\n"// Need to discusse
					+ "         \"DatOfLoanPapers\": \"" + SancDate + "\",\n" + "		\"PropertyLocation\":\"\",\n"
					+ "		\"PlaceOfInstitute\": \"\",\n" + "		\"GCCLoanPurpose\": \"\",\n"
					+ "		\"AgriLoanPurpose\": \"\",\n" + "		\"ConcessionRate\": \"\",\n"
					+ "		\"DatOfConcessionRate\": \"\",\n" + "		\"ConcessionRatePermitBy\": \"\",\n"
					+ "         \"StatusOfProcessingCharges\":\"N\",\n" + "         \"AmtOfProcessingCharges\": \"\",\n"
					+ "         \"DatProcessingCharges\": \"\",\n" + "         \"DatSanctionExpiry\": \""
					+ sanctionExpiryDate + "\",\n" + "		\"UCC\": \"\",\n" + "		\"UCCRemarks\": \"\",\n"
					+ "		\"EduLnPurpose\": \"\",\n" + "		\"EduCourseTyp\": \"\",\n"
					+ "		\"EduQuota\": \"\",\n" + "		\"EduInstitutionCat\": \"\",\n"
					+ "		\"EduCourseStream\": \"\",\n" + "		\"EduEligSubsidy\": \"\",\n"
					+ "		\"EduCoursePrdMonth\": \"\",\n" + "		\"EduDatCompletion\": \"\",\n"
					+ "		\"EduRepayHoliday\": \"\",\n" + "		\"EduCertificateUpto\": \"\",\n"
					+ "		\"EduNamCourse\": \"\",\n" + "		\"EduNamInstitute\": \"\",\n"
					+ "		\"EduPlaceInstitute\": \"\",\n" + "		\"ParentIncomeYear\": \"\",\n"
					+ "		\"ParentIncomeAmt\": \"\",\n" + "		\"IncomeCertIssuedBy\": \"\",\n"
					+ "		\"IncomeCertNumber\": \"\",\n" + "		\"DocSubmitted\": \"\",\n"
					+ "		\"DatMortgage\": \"\",\n" + "		\"BranchBehalf\": \"" + tBranch + "\",\n"
					+ "		\"LSRRefNo\": \"\",\n" + "		\"DatLEDTD\": \"\",\n" + "		\"NotifiedBranches\": \""
					+ tBranch + "\",\n" + "		\"Observations\": \"\",\n" + "		\"HousingFinanceAgency\": \"\",\n"
					+ "		\"POR21blockname\": \"\",\n" + "		\"SectorSensitive\": \"\",\n"
					+ "		\"CollateralValuer\": \"\",\n" + "		\"CollValuationDat\": \"\",\n"
					+ "		\"CollvaluedDetails\": \"\",\n" + "		\"SecutiyValueShortfall\": \"\",\n"
					+ "		\"CollValuationNextDat\": \"\",\n" + "		\"RemarksOval\": \"\",\n"
					+ "		\"CRMBSRDistrictCode\": \"\",\n" + "		\"CRMBSR3CreditCode\": \"\",\n"
					+ "		\"CRMBSR3CreditValue\": \"\",\n" + "		\"CRMSubsidyCode\": \"\",\n"
					+ "		\"BorrowerID\": \"\",\n" + "		\"BorrowerOffice\": \"\",\n"
					+ "		\"BorrowerDUNSNum\": \"\",\n" + "		\"BorrowersLegalConstitution\": \"\",\n"
					+ "		\"Relationship\": \"\",\n" + "		\"LoanCategory\": \"\",\n"
					+ "		\"Handicapped\": \"\",\n" + "		\"BSRAct1\": \"\",\n" + "		\"BSRAct2\": \"\",\n"
					+ "		\"BSRAct3\": \"\",\n" + "		\"AuditDate\": \"\",\n"
					+ "		\"RemarksStockAudit\": \"\",\n" + "		\"AuditNextDate\": \"\",\n"
					+ "		\"AuditConductedBy\": \"\",\n" + "		\"StockValue\": \"\",\n"
					+ "		\"MTRDueDate\": \"\",\n" + "		\"MTRConductedDate\": \"\",\n"
					+ "		\"MTRNextReviewDate\": \"\",\n" + "		\"MTRRemarks\": \"\",\n"
					+ "		\"AdvanceMode\": \"\",\n" + "		\"AdvanceNature\": \"\",\n"
					+ "		\"SSIFlash\": \"\",\n" + "		\"SSISubSector\": \"\",\n"
					+ "		\"GuaranteeType\": \"\",\n" + "		\"SchemeType\": \"\",\n"
					+ "		\"GroupType\": \"\",\n" + "		\"MemberCount\": \"\",\n" + "		\"WomenCount\": \"\",\n"
					+ "		\"MenCount\": \"\",\n" + "		\"MemberName1\": \"\",\n"
					+ "		\"MemberName2\": \"\",\n" + "		\"MemberName3\": \"\",\n"
					+ "		\"MemberName4\": \"\",\n" + "		\"MemberName5\": \"\",\n"
					+ "		\"MemberName6\": \"\",\n" + "		\"MemberName7\": \"\",\n"
					+ "		\"MemberName8\": \"\",\n" + "		\"MemberName9\": \"\",\n"
					+ "		\"MemberName10\": \"\",\n" + "		\"MemberName11\": \"\",\n"
					+ "		\"MemberName12\": \"\",\n" + "		\"MemberName13\": \"\",\n"
					+ "		\"MemberName14\": \"\",\n" + "		\"MemberName15\": \"\",\n"
					+ "		\"MemberName16\": \"\",\n" + "		\"MemberName17\": \"\",\n"
					+ "		\"MemberName18\": \"\",\n" + "		\"MemberName19\": \"\",\n"
					+ "		\"MemberName20\": \"\",\n" + "		\"FinanceBy\": \"\",\n" + "		\"NGOName\": \"\",\n"
					+ "		\"AnchorNGO\": \"\",\n" + "		\"GovSponsoredLoan\": \"\",\n"
					+ "		\"NameGovernment\": \"\",\n" + "		\"SchemeName\": \"\",\n"
					+ "         \"CodMisTxn1\": \"" + CodMisTxn1 + "\",\n" + "         \"CodMisTxn2\": \"" + CodMisTxn2
					+ "\",\n" + "         \"CodMisTxn3\": \"" + CodMisTxn3 + "\",\n" + "         \"CodMisTxn4\": \""
					+ CodMisTxn4 + "\",\n" + "         \"CodMisTxn5\": \"" + CodMisTxn5 + "\",\n"
					+ "         \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n" + "         \"CodMisTxn7\": \"" + CodMisTxn7
					+ "\",\n" + "         \"CodMisTxn8\": \"" + CodMisTxn8 + "\",\n" + "         \"CodMisTxn9\": \""
					+ CodMisTxn9 + "\",\n" + "         \"CodMisTxn10\": \"" + CodMisTxn10 + "\",\n"
					+ "         \"CodMisTxn11\": \"" + CodMisTxn11 + "\",\n" + "         \"CodMisComp1\": \""
					+ CodMisComp1 + "\",\n" + "		\"CodMisComp2\": \"\",\n" + "		\"CodMisComp3\": \"\",\n"
					+ "		\"CodMisComp4\": \"\",\n" + "		\"CodMisComp5\": \"\",\n"
					+ "		\"CodMisComp6\": \"\",\n" + "		\"CodMisComp7\": \"\",\n"
					+ "		\"CodMisComp8\": \"\",\n" + "		\"CodMisComp9\": \"\",\n"
					+ "		\"CodMisComp10\": \"\",\n" + "		\"InvInPlantMachinery\": \"\",\n"
					+ "		\"InvInEquipment\": \"\",\n" + "		\"MSMECategory\": \"\",\n"
					+ "		\"TypeOfIndustry\": \"\",\n" + "		\"TypeOfFinance\": \"\",\n"
					+ "		\"Num2\": \"\",\n" + "		\"Num8\": \"\",\n" + "		\"IntSubvFlag\": \"N\",\n"
					+ "		\"CropCultAmt\": \"\",\n" + "		\"AlliedActAmt\": \"\",\n"
					+ "		\"FarmMachAmt\": \"\",\n" + "		\"NonFarmSectAmt\": \"\",\n"
					+ "		\"ConsumPurpAmt\": \"\",\n" + multiCollaterals.toString() + "         \"SwitchDueDate\": \""
					+ SwitchDueDate + "\",\n" + "		\"CropLoan\": \"N\",\n" + "		\"CropType\": \"\",\n"
					+ "		\"Season\": \"\",\n" + "		\"ACPlanCode\": \"\",\n"
					+ "		\"ReviewPeriod\": \"0\",\n" + "		\"ReviewDate\": \"\",\n" + "		\"CasaAcctNo\": \""
					+ salaryAccountNumber + "\",\n" + "		\"AreaOfFarm\": \"\",\n"
					+ "		\"FarmerCategory\": \"\",\n" + "		\"FarmerSubCategory\": \"\",\n"
					+ "		\"HousingLoanPurpose\": \"" + purposeCode + "\",\n" + "		\"ProjectCost\": \""
					+ project_cost + "\",\n" + "		\"AgriInfraUnit\": \"\",\n" + "		\"Activities\": \"\",\n"
					+ "		\"OtherBankLimit\": \"\",\n" + "		\"Subcategory\": \"\",\n"
					+ "		\"CenterPopulation\": \"\",\n" + "		\"NoOfDependents\": \"\",\n"
					+ "		\"PMFBYApplicable\": \"N\",\n" + "		\"SubsidyAvailable\": \"N\",\n"
					+ "		\"DrawdownRequired\": \"Y\",\n" + "		\"GroupFormationDate\": \"\",\n"
					+ "		\"CustomerID1\": \"\",\n" + "		\"Designation1\": \"\",\n"
					+ "		\"MemberStatus1\": \"\",\n" + "		\"CustomerID2\": \"\",\n"
					+ "		\"Designation2\": \"\",\n" + "		\"MemberStatus2\": \"\",\n"
					+ "		\"CustomerID3\": \"\",\n" + "		\"Designation3\": \"\",\n"
					+ "		\"MemberStatus3\": \"\",\n" + "		\"CustomerID4\": \"\",\n"
					+ "		\"Designation4\": \"\",\n" + "		\"MemberStatus4\": \"\",\n"
					+ "		\"CustomerID5\": \"\",\n" + "		\"Designation5\": \"\",\n"
					+ "		\"MemberStatus5\": \"\",\n" + "		\"CustomerID6\": \"\",\n"
					+ "		\"Designation6\": \"\",\n" + "		\"MemberStatus6\": \"\",\n"
					+ "		\"CustomerID7\": \"\",\n" + "		\"Designation7\": \"\",\n"
					+ "		\"MemberStatus7\": \"\",\n" + "		\"CustomerID8\": \"\",\n"
					+ "		\"Designation8\": \"\",\n" + "		\"MemberStatus8\": \"\",\n"
					+ "		\"CustomerID9\": \"\",\n" + "		\"Designation9\": \"\",\n"
					+ "		\"MemberStatus9\": \"\",\n" + "		\"CustomerID10\": \"\",\n"
					+ "		\"Designation10\": \"\",\n" + "		\"MemberStatus10\": \"\",\n"
					+ "		\"CustomerID11\": \"\",\n" + "		\"Designation11\": \"\",\n"
					+ "		\"MemberStatus11\": \"\",\n" + "		\"CustomerID12\": \"\",\n"
					+ "		\"Designation12\": \"\",\n" + "		\"MemberStatus12\": \"\",\n"
					+ "		\"CustomerID13\": \"\",\n" + "		\"Designation13\": \"\",\n"
					+ "		\"MemberStatus13\": \"\",\n" + "		\"CustomerID14\": \"\",\n"
					+ "		\"Designation14\": \"\",\n" + "		\"MemberStatus14\": \"\",\n"
					+ "		\"CustomerID15\": \"\",\n" + "		\"Designation15\": \"\",\n"
					+ "		\"MemberStatus15\": \"\",\n" + "		\"CustomerID16\": \"\",\n"
					+ "		\"Designation16\": \"\",\n" + "		\"MemberStatus16\": \"\",\n"
					+ "		\"CustomerID17\": \"\",\n" + "		\"Designation17\": \"\",\n"
					+ "		\"MemberStatus17\": \"\",\n" + "		\"CustomerID18\": \"\",\n"
					+ "		\"Designation18\": \"\",\n" + "		\"MemberStatus18\": \"\",\n"
					+ "		\"CustomerID19\": \"\",\n" + "		\"Designation19\": \"\",\n"
					+ "		\"MemberStatus19\": \"\",\n" + "		\"CustomerID20\": \"\",\n"
					+ "		\"Designation20\": \"\",\n" + "		\"MemberStatus20\": \"\",\n"
					+ "		\"ReviewAccount\": \"N\",\n" + "		\"RemarksProcessingCharges\": \"\",\n"
					+ "		\"CentreLeaderIndicator\": \"\",\n" + "		\"ShpiNgoName\": \"\",\n"
					+ "		\"ShpiNgoIdentifier\": \"\",\n" + "		\"ShpiNgoOfficerName\": \"\",\n"
					+ "		\"ShpiNgoAddress\": \"\",\n" + "		\"NoOfMeetingsHeld\": \"\",\n"
					+ "		\"NoOfMeetingsMissed\": \"\",\n" + "		\"AgreedMeetingDayOfTheWeek\": \"\",\n"
					+ "		\"AgreedMeetingTimeOfTheDay\": \"\",\n" + "		\"InsuranceIndicator\": \"\",\n"
					+ "		\"TypeOfInsurance\": \"\",\n" + "		\"SumAssuredCoverage\": \"\",\n"
					+ "		\"LoanCategoryGrp\": \"\",\n" + "		\"CropCultivationAmountKhrif\": \"\",\n"
					+ "		\"CropCultivationAmountRabi\": \"\",\n" + "		\"CropCultivationAmountSumer\": \"\",\n"
					+ "		\"CropInsurancePremium\": \"\",\n" + "		\"LinkTDRDForInterest\": \"\",\n"
					+ "		\"concessionPermitted\": \"N\",\n" + "		\"BankArr\": \"1\",\n"
					+ "		\"BankFiTyp\": \"\",\n" + "		\"cropDuration\": \"\",\n"
					+ "		\"PrimarySecondary1\": \"P\",\n" + "		\"CBRGuaranteeAmount\": \"\",\n"
					+ "		\"BaselCustType\": \"" + baselCustType + "\",\n" + "		\"ExposureRefNum\": \"\"\n"
					+ "	}\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String LoanAccountCreationResponseResponse = bodyObj.get("LoanAccountCreationResponse").toString();
					// Log.consoleLog(ifr, "Response==>" + Response);
					System.out.println("LoanAccountCreationResponseResponse=>" + LoanAccountCreationResponseResponse);

					JSONObject LoanAccountCreationResponseResponseJSON = (JSONObject) parser
							.parse(LoanAccountCreationResponseResponse);
					JSONObject LoanAccountCreationResponseResponseObj = new JSONObject(
							LoanAccountCreationResponseResponseJSON);

					LoanAccNumber = LoanAccountCreationResponseResponseObj.get("accountNo").toString();
					String sanctionAmount = LoanAccountCreationResponseResponseObj.get("sanctionAmount").toString();

					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					Log.consoleLog(ifr, "sanctionAmount==>" + sanctionAmount);

					Log.consoleLog(ifr, "AccountNumber==>" + LoanAccNumber);

//					java.util.Date dateTimeFormat = new java.util.Date();
//					SimpleDateFormat sDateTime = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
//					String strCurDateTime = sDateTime.format(dateTimeFormat);
					
					java.util.Date dateTimeFormat = new java.util.Date();
					SimpleDateFormat sDateTime = new SimpleDateFormat("dd/MM/yyyy");
					String strCurDateTime = sDateTime.format(dateTimeFormat);

					String Query1 = "";
					Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET " + "LOAN_ACCOUNTNO='" + LoanAccNumber.trim() + "', "
							+ "ACCOUNT_CREATEDDATE='" + strCurDateTime.trim() + "', " + "SANCTION_AMOUNT='" + sanctionAmount
							+ "', " + "DISB_AMOUNT='" + loanAmount + "', " + "DISB_BRANCH='" + tBranch + "' "
							+ "WHERE PID='" + processInstanceId + "' "
							+ "AND (LOAN_ACCOUNTNO IS NULL OR LOAN_ACCOUNTNO = '')";

					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> Else Query1===>" + Query1);
					ifr.saveDataInDB(Query1);

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
				apiStatus = RLOS_Constants.ERROR;
			}

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			}

//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response,
//                    ErrorCode, ErrorMessage, APIStatus);
		}

		catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String getLoanAccountDetailsForGold(IFormReference ifr, String ProcessInstanceId,
			HashMap<String, String> map, String productCode, String sanctionExpiryDate, String productType)
			throws ParseException {
		
		String APIName = "";
		String APIStatus = "";
		String response = "";
		String request = "";

		Log.consoleLog(ifr, "#Execute_CBSLoanAccountCreation=================");

		String apiName = "LoanAccountCreation";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		String LoanAccNumber = "";
		String Tenure = map.get("Tenure");
                String ErrorCode = "";
                String ErrorMessage = "";

		try {

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formattedDate = dateFormat.format(currentDate);


			Log.consoleLog(ifr, "productCode=======>" + productCode);

			String BankCode = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "BANKCODE");
			String Channel = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "CHANNEL");
			String S_UserId = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "SC_USERID");
			String UserId = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "USERID");
			String CustRel = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "CUSTREL");
			String InterestVariance = "";
			String StatusOfProcessingCharges = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "PCSTATUS");
			String SwitchDueDate = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "SWITCHDUEDATE");
			String CodMisComp1 = cm.getConfigValue(ifr, "GOLD", "GOLD", "LOANACCCREATE", "CodMisComp1");

			String scheduleCodequery = "select distinct(COD_SCHED_TYPE)  from ALOS_GOLD_SCHED_TYPES where cod_prod='"
					+ productCode + "'";
			List<List<String>> queryRes = ifr.getDataFromDB(scheduleCodequery);

			String ScheduleCode = "";

			if (queryRes.size() > 0) {
				ScheduleCode = queryRes.get(0).get(0);
			} else {
				return "ScheduleCode is not found";
			}

			String sancDate = "";
			String collaterId = "";
			
			String queryCollateralId = "SELECT COLLATERAL_ID FROM SLOS_STAFF_JEWELLERY_DETAILS WHERE WINAME = '"
					+ ProcessInstanceId + "'";
			Log.consoleLog(ifr, "queryCollateralId " + queryCollateralId);
			List<List<String>> rsqueryCollateralId = ifr.getDataFromDB(queryCollateralId);
			Log.consoleLog(ifr, "rsqueryCollateralId " + rsqueryCollateralId);
			
			if(!rsqueryCollateralId.isEmpty())
			{
				collaterId=rsqueryCollateralId.get(0).get(0);
			}
			
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
					SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					sancDate = targetFormat.format(parsedDate); // Original date in yyyyMMdd
				} catch (Exception e) {
					Log.consoleLog(ifr, "Error parsing date: " + e.getMessage());
				}

			} else {
				return "error,No sanction date found";
			}

			
			String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, productCode);
			String selectedBranchCode = ifr.getValue("BRANCHCODEVL").toString();

//			String querySelectedBranchCode = "select disbursement_brancode, TO_CHAR(SANCTION_DATE, 'YYYYMMDD') as SANCTIONDATE, collateral_id from glos_l_loansummary where winame='"
//					+ ProcessInstanceId + "' ";
//			Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
//			List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
//			if (branchCodeResult.size() > 0) {
//				selectedBranchCode = branchCodeResult.get(0).get(0);
//				sancDate = branchCodeResult.get(0).get(1);
//				collaterId = branchCodeResult.get(0).get(2);
//			}
//			if (!Optional.ofNullable(selectedBranchCode).isPresent()) {
//				selectedBranchCode = TBranch;
//			}
                        
                        String sancAuthority = "";
                         String randomnumber=pcm.generateRandomNumber(ifr);
                       // String sancAuthorityquery = "select u.GRP_ID from los_m_branch b inner join los_m_user u on u.EMPLOYEE_ID = b.BRHEAD_ID where b.BRANCHCODE ='"+String.format("%05d", Integer.parseInt(selectedBranchCode))+"'";
                        String processingUser = ifr.getUserName();
                        Log.consoleLog(ifr, "Loan Account Creation Checker processingUser==>" + processingUser);
//                        String sancAuthorityquery = "select GRP_ID from los_m_user where EMPLOYEE_ID ='"+processingUser+"'";
//                        
//                        Log.consoleLog(ifr, "sancAuthorityquery==>" + sancAuthorityquery);
//                        List<List<String>> sancAuthorityList = ifr.getDataFromDB(sancAuthorityquery);
//                        if (sancAuthorityList.size() > 0) {
//                          sancAuthority = sancAuthorityList.get(0).get(0);
//                        }
                       /* else
                        {
                        	if (sancAuthorityList.isEmpty()) {
                        		sancAuthority="73";  //need to pick from config table
                        	}
                        }*/
            sancAuthority = pcm.getSancAuhority(ifr, "GOLD");
			String applicationRefNo = "";
			String queryAppNo = "SELECT APP_FORM_NO FROM  ALOS_EXT_TABLE WHERE PID='" + ProcessInstanceId + "' ";
			Log.consoleLog(ifr, "SancReference_Qry==>" + queryAppNo);
			List<List<String>> SancResult = ifr.getDataFromDB(queryAppNo);
			if (SancResult.size() > 0) {
				applicationRefNo = SancResult.get(0).get(0);
			}
			if (applicationRefNo.equalsIgnoreCase("")) {
				applicationRefNo = ProcessInstanceId.replace("-", "");
			}
			
			String RatLimLevelQUERY = "select VARIANCE from alos_m_gold_product where cbs_product_code='"+productCode+"'";
	            List<List<String>> RatLimLevelQUERYList = ifr.getDataFromDB(RatLimLevelQUERY);
	            Log.consoleLog(ifr, "result: " + RatLimLevelQUERYList);
	            if (RatLimLevelQUERYList.size() > 0) {
	            	InterestVariance = RatLimLevelQUERYList.get(0).get(0);
	            	Log.consoleLog(ifr, "InterestVariance: " + InterestVariance);
	            }  
			//InterestVariance="0";

			Log.consoleLog(ifr, "applicationRefNo==>" + applicationRefNo);

			String Purpose = map.get("Purpose");
			String takeOveLoan = map.get("TakeOveLoan");
			String agriLoanPurpose = map.get("AgriLoanPurpose");
			String intSubvFlag = map.get("IntSubvFlag");

			String collateralCode = map.get("CollateralCode");
			String cropLoan = map.get("CropLoan");
			String cropType = map.get("CropType");
			String season = map.get("Season");
			String ACPlanCode = map.get("AcPlanCode");
			String cropDuration = map.get("CropDuration");
			String reviewPeriod = map.get("ReviewPeriod");
			String PMFBYApplicable = map.get("PMFBYApplicable");
			String subsidyAvailable = map.get("SubsidyAvailable");
			String drawdownRequired = map.get("DrawdownRequired");
                        String casaAccountNumber = map.get("CasaAccountNumber");
			String linkTDRDForInterest = map.get("LinkTDRDForInterest");
			String concessionPermitted = map.get("ConcessionPermitted");
			String primarySecondary = map.get("PrimarySecondary");

			String bankArr = map.get("BankArr");
			String AppriaserReg = map.get("AppriaserReg");
			String CustomerId = map.get("CustomerId");

			String codMixquery = "select * from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS where PROD_CODE='" + productCode + "'";
			List<List<String>> codMixqueryList = ifr.getDataFromDB(codMixquery);
			String CodMisTxn1 = "";
			String CodMisTxn2 = "";
			String CodMisTxn3 = "";
			String CodMisTxn4 = "";
			String CodMisTxn5 = "";
			String CodMisTxn6 = "";
			String CodMisTxn7 = "";
			String CodMisTxn8 = "";
			String CodMisTxn9 = "";
			String CodMisTxn10 = "";
			String CodMisTxn11 = "";

			if (codMixqueryList.size() > 0) {
				CodMisTxn1 = codMixqueryList.get(0).get(1);
				CodMisTxn2 = codMixqueryList.get(0).get(2);
				CodMisTxn3 = codMixqueryList.get(0).get(3);
				CodMisTxn4 = codMixqueryList.get(0).get(4);
				CodMisTxn5 = codMixqueryList.get(0).get(5);
				CodMisTxn6 = codMixqueryList.get(0).get(6);
				CodMisTxn7 = codMixqueryList.get(0).get(7);
				CodMisTxn8 = codMixqueryList.get(0).get(8);
				CodMisTxn9 = codMixqueryList.get(0).get(9);
				CodMisTxn10 = codMixqueryList.get(0).get(10);
				CodMisTxn11 = codMixqueryList.get(0).get(11);
			}

			String LoanAmount = map.get("LoanAmount");
			String lendableMargin = map.get("LendableMargin");
			String amtMargin = map.get("AmtMargin");
			String projectCost = map.get("ProjectCost");
            String lendableAmount = map.get("Lendable_Amount");
                        

			String reviewDate = getReviewDate(Integer.parseInt(reviewPeriod));
			String basel_cust_type1=ifr.getValue("BASEL_CUST_TYPE").toString();
			Log.consoleLog(ifr, "basel_cust_type1"+basel_cust_type1);
			String basel_cust_type="";
			
			//BASEL_CUST_TYPE picking from table start here
			
			String queryBaselCode = "select COD_BASEL_CODE from BA_PROD_BASEL_CUST_TYPE where cod_prod='" + productCode
					+ "'";
			List<List<String>> listqueryBaselCode = ifr.getDataFromDB(queryBaselCode);
			Log.consoleLog(ifr, "queryBaselCode==> " + queryBaselCode);
			if (!listqueryBaselCode.isEmpty()) {
				basel_cust_type = listqueryBaselCode.get(0).get(0);
			}
			
			//BASEL_CUST_TYPE picking from table end here
			

			String FlgNomAppl = ifr.getValue("nominee_taken_gold").toString();
			if (FlgNomAppl.toLowerCase().contains("yes")) {
				FlgNomAppl = "Y";
			} else {
				FlgNomAppl = "N";
			}
			
			

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"" + S_UserId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + BankCode + "\",\n" + "            \"Channel\": \"" + Channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + selectedBranchCode + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"ValueDateText\": \"\"\n" + "        },\n"
					+ "        \"ExtUniqueRefId\": \"" + formattedDate +randomnumber+ "\",\n" + "        \"BranchCode\": \""
					+ selectedBranchCode + "\",\n" + "        \"ProductCode\": \"" + productCode + "\",\n"
					+ "        \"CustomerID\": \"" + CustomerId + "\",\n" + "        \"CustRel\": \"" + CustRel
					+ "\",\n" + "        \"CustomerIDJoint\": \"\",\n" + "        \"CustJointRel\": \"\",\n"
					+ "        \"LoanAmount\": \"" + LoanAmount + "\",\n" + "        \"LoanTerm\": \"" + Tenure
					+ "\",\n" + "        \"ScheduleCode\": \"" + ScheduleCode + "\",\n"
					+ "        \"InterestVariance\": \"" + InterestVariance + "\",\n" + "        \"Purpose\": \""
					+ Purpose + "\",\n" + "        \"TakeOverLoan\":\"" + takeOveLoan + "\",\n"
					+ "        \"BankFIName\": \"\",\n" + "        \"DatOfSanction\": \"" + sancDate + "\",\n"
					+ "        \"SancAuthority\": \"" + sancAuthority + "\",\n" + "        \"SancReference\": \""
					+ applicationRefNo + "\",\n" + "        \"DatOfLoanPapers\": \"" + sancDate + "\",\n"
					+ "        \"PropertyLocation\": \"\",\n" + "        \"PlaceOfInstitute\": \"\",\n"
					+ "        \"GCCLoanPurpose\": \"\",\n" + "        \"AgriLoanPurpose\": \"" + agriLoanPurpose
					+ "\",\n" + "        \"ConcessionRate\": \"\",\n" + "        \"DatOfConcessionRate\": \"\",\n"
					+ "        \"ConcessionRatePermitBy\": \"\",\n" + "        \"StatusOfProcessingCharges\": \""
					+ StatusOfProcessingCharges + "\",\n" + "        \"AmtOfProcessingCharges\": \"\",\n"
					+ "        \"DatProcessingCharges\": \"\",\n" + "        \"DatSanctionExpiry\": \""
					+ sanctionExpiryDate + "\",\n" + "        \"UCC\": \"\",\n" + "        \"UCCRemarks\": \"\",\n"
					+ "        \"EduLnPurpose\": \"\",\n" + "        \"EduCourseTyp\": \"\",\n"
					+ "        \"EduQuota\": \"\",\n" + "        \"EduInstitutionCat\": \"\",\n"
					+ "        \"EduCourseStream\": \"\",\n" + "        \"EduEligSubsidy\": \"\",\n"
					+ "        \"EduCoursePrdMonth\": \"\",\n" + "        \"EduDatCompletion\": \"\",\n"
					+ "        \"EduRepayHoliday\": \"\",\n" + "        \"EduCertificateUpto\": \"\",\n"
					+ "        \"EduNamCourse\": \"\",\n" + "        \"EduNamInstitute\": \"\",\n"
					+ "        \"EduPlaceInstitute\": \"\",\n" + "        \"ParentIncomeYear\": \"\",\n"
					+ "        \"ParentIncomeAmt\": \"\",\n" + "        \"IncomeCertIssuedBy\": \"\",\n"
					+ "        \"IncomeCertNumber\": \"\",\n" + "        \"DocSubmitted\": \"\",\n"
					+ "        \"DatMortgage\": \"\",\n" + "        \"BranchBehalf\": \"" + selectedBranchCode + "\",\n"
					+ "        \"LSRRefNo\": \"\",\n" + "        \"DatLEDTD\": \"\",\n"
					+ "        \"NotifiedBranches\": \"" + selectedBranchCode + "\",\n" + "        \"Observations\": \"\",\n"
					+ "        \"HousingFinanceAgency\": \"\",\n" + "        \"POR21blockname\": \"\",\n"
					+ "        \"SectorSensitive\": \"\",\n" + "        \"CollateralValuer\": \"\",\n"
					+ "        \"CollValuationDat\": \"\",\n" + "        \"CollvaluedDetails\": \"\",\n"
					+ "        \"SecutiyValueShortfall\": \"\",\n" + "        \"CollValuationNextDat\": \"\",\n"
					+ "        \"RemarksOval\": \"\",\n" + "        \"CRMBSRDistrictCode\": \"\",\n"
					+ "        \"CRMBSR3CreditCode\": \"\",\n" + "        \"CRMBSR3CreditValue\": \"\",\n"
					+ "        \"CRMSubsidyCode\": \"\",\n" + "        \"BorrowerID\": \"\",\n"
					+ "        \"BorrowerOffice\": \"\",\n" + "        \"BorrowerDUNSNum\": \"\",\n"
					+ "        \"BorrowersLegalConstitution\": \"\",\n" + "        \"Relationship\": \"\",\n"
					+ "        \"LoanCategory\": \"\",\n" + "        \"Handicapped\": \"\",\n"
					+ "        \"BSRAct1\": \"\",\n" + "        \"BSRAct2\": \"\",\n" + "        \"BSRAct3\": \"\",\n"
					+ "        \"AuditDate\": \"\",\n" + "        \"RemarksStockAudit\": \"\",\n"
					+ "        \"AuditNextDate\": \"\",\n" + "        \"AuditConductedBy\": \"\",\n"
					+ "        \"StockValue\": \"\",\n" + "        \"MTRDueDate\": \"\",\n"
					+ "        \"MTRConductedDate\": \"\",\n" + "        \"MTRNextReviewDate\": \"\",\n"
					+ "        \"MTRRemarks\": \"\",\n" + "        \"AdvanceMode\": \"\",\n"
					+ "        \"AdvanceNature\": \"\",\n" + "        \"SSIFlash\": \"\",\n"
					+ "        \"SSISubSector\": \"\",\n" + "        \"GuaranteeType\": \"\",\n"
					+ "        \"SchemeType\": \"\",\n" + "        \"GroupType\": \"\",\n"
					+ "        \"MemberCount\": \"\",\n" + "        \"WomenCount\": \"\",\n"
					+ "        \"MenCount\": \"\",\n" + "        \"MemberName1\": \"\",\n"
					+ "        \"MemberName2\": \"\",\n" + "        \"MemberName3\": \"\",\n"
					+ "        \"MemberName4\": \"\",\n" + "        \"MemberName5\": \"\",\n"
					+ "        \"MemberName6\": \"\",\n" + "        \"MemberName7\": \"\",\n"
					+ "        \"MemberName8\": \"\",\n" + "        \"MemberName9\": \"\",\n"
					+ "        \"MemberName10\": \"\",\n" + "        \"MemberName11\": \"\",\n"
					+ "        \"MemberName12\": \"\",\n" + "        \"MemberName13\": \"\",\n"
					+ "        \"MemberName14\": \"\",\n" + "        \"MemberName15\": \"\",\n"
					+ "        \"MemberName16\": \"\",\n" + "        \"MemberName17\": \"\",\n"
					+ "        \"MemberName18\": \"\",\n" + "        \"MemberName19\": \"\",\n"
					+ "        \"MemberName20\": \"\",\n" + "        \"FinanceBy\": \"\",\n"
					+ "        \"NGOName\": \"\",\n" + "        \"AnchorNGO\": \"\",\n"
					+ "        \"GovSponsoredLoan\": \"\",\n" + "        \"NameGovernment\": \"\",\n"
					+ "        \"SchemeName\": \"\",\n" + "        \"CodMisTxn1\": \"" + CodMisTxn1 + "\",\n"
					+ "        \"CodMisTxn2\": \"" + CodMisTxn2 + "\",\n" + "        \"CodMisTxn3\": \"" + CodMisTxn3
					+ "\",\n" + "        \"CodMisTxn4\": \"" + CodMisTxn4 + "\",\n" + "        \"CodMisTxn5\": \""
					+ CodMisTxn5 + "\",\n" + "        \"CodMisTxn6\": \"" + CodMisTxn6 + "\",\n"
					+ "        \"CodMisTxn7\": \"" + CodMisTxn7 + "\",\n" + "        \"CodMisTxn8\": \"" + CodMisTxn8
					+ "\",\n" + "        \"CodMisTxn9\": \"" + CodMisTxn9 + "\",\n" + "        \"CodMisTxn10\": \""
					+ CodMisTxn10 + "\",\n" + "  \"CodMisTxn11\": \"" + CodMisTxn11 + "\",\n"
					+ "      \"CodMisComp1\": \"" + CodMisComp1 + "\",\n" + "        \"CodMisComp2\": \"\",\n"
					+ "        \"CodMisComp3\": \"\",\n" + "        \"CodMisComp4\": \"\",\n"
					+ "        \"CodMisComp5\": \"\",\n" + "        \"CodMisComp6\": \"\",\n"
					+ "        \"CodMisComp7\": \"\",\n" + "        \"CodMisComp8\": \"\",\n"
					+ "        \"CodMisComp9\": \"\",\n" + "        \"CodMisComp10\": \"\",\n"
					+ "        \"InvInPlantMachinery\": \"\",\n" + "        \"InvInEquipment\": \"\",\n"
					+ "        \"MSMECategory\": \"\",\n" + "        \"TypeOfIndustry\": \"\",\n"
					+ "        \"TypeOfFinance\": \"\",\n" + "        \"Num2\": \"\",\n" + "        \"Num8\": \"\",\n"
					+ "        \"IntSubvFlag\": \"" + intSubvFlag + "\",\n" + "        \"CropCultAmt\": \"\",\n"
					+ "        \"AlliedActAmt\": \"\",\n" + "        \"FarmMachAmt\": \"\",\n"
					+ "        \"NonFarmSectAmt\": \"\",\n" + "        \"ConsumPurpAmt\": \"\",\n"
					+ "        \"Security_id1\": \"" + collaterId + "\",\n" + "        \"AmtMargin\": \"" + amtMargin
					+ "\",\n" + "        \"Lendable_margin1\":  \"" + lendableMargin + "\",\n"
					+ "        \"Lendable_amount1\":\"" + lendableAmount + "\",\n" + "        \"CollateralCode1\": \""
					+ collateralCode + "\",\n" + "        \"SwitchDueDate\": \"" + SwitchDueDate + "\",\n"
					+ "        \"CropLoan\": \"\",\n" + "        \"CropType\":\"\",\n"
					+ "        \"Season\": \"\",\n" + "        \"ACPlanCode\": \"\",\n"
					+ "        \"cropDuration\":\"" + cropDuration + "\",\n" + "        \"ReviewPeriod\": \""
					+ reviewPeriod + "\",\n" + "        \"ReviewDate\": \"" + reviewDate + "\",\n"
					+ "        \"AreaOfFarm\": \"\",\n" + "        \"FarmerCategory\": \"\",\n"
					+ "        \"FarmerSubCategory\": \"\",\n" + "        \"HousingLoanPurpose\": \"\",\n"
					+ "        \"ProjectCost\": \"" + projectCost + "\",\n" + "        \"AgriInfraUnit\": \"\",\n"
					+ "        \"Activities\": \"\",\n" + "        \"OtherBankLimit\": \"\",\n"
					+ "        \"Subcategory\": \"\",\n" + "        \"CenterPopulation\": \"\",\n"
					+ "        \"NoOfDependents\": \"\",\n" + "        \"PMFBYApplicable\":\"" + PMFBYApplicable
					+ "\",\n" + "        \"SubsidyAvailable\": \"" + subsidyAvailable + "\",\n"
					+ "        \"DrawdownRequired\":  \"" + drawdownRequired + "\",\n" + "        \"CasaAcctNo\": \""
					+ casaAccountNumber + "\",\n" + "        \"CasaAcctNoSC\": \"\",\n"
					+ "        \"GroupFormationDate\": \"\",\n" + "        \"CustomerID1\": \"\",\n"
					+ "        \"Designation1\": \"\",\n" + "        \"MemberStatus1\": \"\",\n"
					+ "        \"CustomerID2\": \"\",\n" + "        \"Designation2\": \"\",\n"
					+ "        \"MemberStatus2\": \"\",\n" + "        \"CustomerID3\": \"\",\n"
					+ "        \"Designation3\": \"\",\n" + "        \"MemberStatus3\": \"\",\n"
					+ "        \"CustomerID4\": \"\",\n" + "        \"Designation4\": \"\",\n"
					+ "        \"MemberStatus4\": \"\",\n" + "        \"CustomerID5\": \"\",\n"
					+ "        \"Designation5\": \"\",\n" + "        \"MemberStatus5\": \"\",\n"
					+ "        \"CustomerID6\": \"\",\n" + "        \"Designation6\": \"\",\n"
					+ "        \"MemberStatus6\": \"\",\n" + "        \"CustomerID7\": \"\",\n"
					+ "        \"Designation7\": \"\",\n" + "        \"MemberStatus7\": \"\",\n"
					+ "        \"CustomerID8\": \"\",\n" + "        \"Designation8\": \"\",\n"
					+ "        \"MemberStatus8\": \"\",\n" + "        \"CustomerID9\": \"\",\n"
					+ "        \"Designation9\": \"\",\n" + "        \"MemberStatus9\": \"\",\n"
					+ "        \"CustomerID10\": \"\",\n" + "        \"Designation10\": \"\",\n"
					+ "        \"MemberStatus10\": \"\",\n" + "        \"CustomerID11\": \"\",\n"
					+ "        \"Designation11\": \"\",\n" + "        \"MemberStatus11\": \"\",\n"
					+ "        \"CustomerID12\": \"\",\n" + "        \"Designation12\": \"\",\n"
					+ "        \"MemberStatus12\": \"\",\n" + "        \"CustomerID13\": \"\",\n"
					+ "        \"Designation13\": \"\",\n" + "        \"MemberStatus13\": \"\",\n"
					+ "        \"CustomerID14\": \"\",\n" + "        \"Designation14\": \"\",\n"
					+ "        \"MemberStatus14\": \"\",\n" + "        \"CustomerID15\": \"\",\n"
					+ "        \"Designation15\": \"\",\n" + "        \"MemberStatus15\": \"\",\n"
					+ "        \"CustomerID16\": \"\",\n" + "        \"Designation16\": \"\",\n"
					+ "        \"MemberStatus16\": \"\",\n" + "        \"CustomerID17\": \"\",\n"
					+ "        \"Designation17\": \"\",\n" + "        \"MemberStatus17\": \"\",\n"
					+ "        \"CustomerID18\": \"\",\n" + "        \"Designation18\": \"\",\n"
					+ "        \"MemberStatus18\": \"\",\n" + "        \"CustomerID19\": \"\",\n"
					+ "        \"Designation19\": \"\",\n" + "        \"MemberStatus19\": \"\",\n"
					+ "        \"CustomerID20\": \"\",\n" + "        \"Designation20\": \"\",\n"
					+ "        \"MemberStatus20\": \"\",\n" + "        \"ReviewAccount\": \"\",\n"
					+ "        \"RemarksProcessingCharges\": \"\",\n" + "        \"CentreLeaderIndicator\": \"\",\n"
					+ "        \"ShpiNgoName\": \"\",\n" + "        \"ShpiNgoIdentifier\": \"\",\n"
					+ "        \"ShpiNgoOfficerName\": \"\",\n" + "        \"ShpiNgoAddress\": \"\",\n"
					+ "        \"NoOfMeetingsHeld\": \"\",\n" + "        \"NoOfMeetingsMissed\": \"\",\n"
					+ "        \"AgreedMeetingDayOfTheWeek\": \"\",\n"
					+ "        \"AgreedMeetingTimeOfTheDay\": \"\",\n" + "        \"InsuranceIndicator\": \"\",\n"
					+ "        \"TypeOfInsurance\": \"\",\n" + "        \"SumAssuredCoverage\": \"\",\n"
					+ "        \"LoanCategoryGrp\": \"\",\n" + "  \"CropCultivationAmountKhrif\": \"\",\n"
					+ "        \"CropCultivationAmountRabi\": \"\",\n"
					+ "        \"CropCultivationAmountSumer\": \"\",\n" + "        \"CropInsurancePremium\": \"\",\n"
					+ "        \"LinkTDRDForInterest\":  \"" + linkTDRDForInterest + "\",\n"
					+ "        \"FlgNomAppl\":\"" + FlgNomAppl + "\",\n" + "        \"concessionPermitted\":\""
					+ concessionPermitted + "\",\n" + "        \"BankArr\":\"" + bankArr + "\",\n"
					+ "        \"BankFiTyp\": \"\",\n" + "        \"PrimarySecondary1\": \"" + primarySecondary
					+ "\",\n" + "        \"ExposureRefNum\": \"\",\n" + "        \"AppriaserReg\": \"" + AppriaserReg
					+ "\",\n" + "        \"RatCrRiskPrem\": \"\",\n" + "        \"RatLiquidityPrem\": \"\",\n"
					+ "        \"RatEcaiUnratedPrem\": \"\",\n" + "        \"RatRiskGradeConcess\": \"\",\n"
					+ "        \"BaselCustType\":\"" + basel_cust_type + "\",\n"
					+ "        \"ConcessEndDate\": \"\"\n  } }";

			HashMap requestHeader = new HashMap<>();
			Log.consoleLog(ifr, "Request====>" + request);
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			
			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body :  " + body);

				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				JSONObject bodyJSONObj = new JSONObject(bodyJSON);
				JSONObject bodyObj = new JSONObject(bodyJSONObj);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String LoanAccountCreationResponseResponse = bodyObj.get("LoanAccountCreationResponse").toString();
					// Log.consoleLog(ifr, "Response==>" + Response);
					Log.consoleLog(ifr, "LoanAccountCreationResponseResponse=>" + LoanAccountCreationResponseResponse);

					JSONObject LoanAccountCreationResponseResponseJSON = (JSONObject) parser
							.parse(LoanAccountCreationResponseResponse);
					JSONObject LoanAccountCreationResponseResponseObj = new JSONObject(
							LoanAccountCreationResponseResponseJSON);

					LoanAccNumber = LoanAccountCreationResponseResponseObj.get("accountNo").toString();
					String sanctionAmount = LoanAccountCreationResponseResponseObj.get("sanctionAmount").toString();

					Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);
					Log.consoleLog(ifr, "sanctionAmount==>" + sanctionAmount);

					Log.consoleLog(ifr, "AccountNumber==>" + LoanAccNumber);

					java.util.Date dateTimeFormat = new java.util.Date();
					SimpleDateFormat sDateTime = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
					String strCurDateTime = sDateTime.format(dateTimeFormat);
                     
			           
//					String SEQ_NO = LoanAccountCreationResponseResponseObj.get("SequenceNo").toString();
//					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails SEQ_NO -> " + SEQ_NO);	
//					ifr.setValue("Gold_Seq_No", SEQ_NO);
//					ifr.setValue("Q_DataOnDemand", SEQ_NO);   
					
					String Query1 = "UPDATE SLOS_TRN_LOANDETAILS SET LOAN_ACCOUNTNO= '" + LoanAccNumber.trim() + "',"+ "ACCOUNT_CREATEDDATE= '" + strCurDateTime + "'," + "SANCTION_AMOUNT= '" + sanctionAmount + "' WHERE PID= '" + ProcessInstanceId + "'";
					Log.consoleLog(ifr, "LoanAccountCreation:getLoanAccountDetails -> " + Query1);
					ifr.saveDataInDB(Query1);

				} else {
					String[] ErrorData = CheckError.split("#");
					ErrorCode = ErrorData[0];
					ErrorMessage = ErrorData[1];
				}
			} else {
				response = "No response from the server.";
				ErrorMessage = "No response from the CBS server.";
			}

			try {
				APIName = "CBS_LoanAccountCreation";
			
				if (ErrorMessage.equalsIgnoreCase("")) {
					APIStatus = "SUCCESS";
				} else {
					APIStatus = "FAIL";
                   LoanAccNumber = "";
				}
				if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
					return LoanAccNumber;
				}
				
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
                ErrorMessage = e.getMessage();
               LoanAccNumber = "";
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  LoanAccCreateAPI" + e.getMessage());
            LoanAccNumber = "";
           ErrorMessage = e.getMessage();
		}
		finally {
			Log.consoleLog(ifr, "Calling /CaptureRequestResponse");
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, request, response, ErrorCode, ErrorMessage,
					APIStatus);
		}
		return  RLOS_Constants.ERROR+"#"+ErrorMessage;
	}

	private String getReviewDate(int reviewPeriodInMonths) {
		Calendar calendar = Calendar.getInstance();
		// Add the review period in months to the current date
		calendar.add(Calendar.MONTH, reviewPeriodInMonths);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(calendar.getTime());
	}
}
