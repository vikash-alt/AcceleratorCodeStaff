package com.newgen.dlp.integration.cbs;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.fasterxml.jackson.databind.DatabindException;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Advanced360EnquiryHRMSData {

	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();

	public String executeCBSAdvanced360Inquiryv2(IFormReference ifr, String ProcessInstanceId, String CustomerID,
			String salaryacc, String designation, String probation) {
		Log.consoleLog(ifr, "Entered into ExecuteCBSAdvanced360Inquiryv2...");

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Advanced360Inquiryv2";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		double totalBalance = 0.0;
		double totalBal = 0.0;
		String totalBalEGLoan = "";
		double totalBalEG = 0.0;
		String productCode = "";
		String odAccountDetailsDTO = "";
		double amtOdLimit = 0.0;
		double amtODInt = 0;
		String productName = "";
		double amtInstal = 0.0;
		String productNameOD = "";
		String accountId = "";
		String branchCode = "";
		String Query1 = "";
		String odExpiryDate = "";
		String odAccountId = "";
		String odRenewalprodCode = "";
		double amtInstalOthers = 0.0;
		double odRenewalAmount = 0.0;
		double limitNumber = 0.0;
		String dateAccOpen = "";
		String loanInstallmentAmt = "";
		boolean flag = false;
		JSONArray odValidProductCode = new JSONArray();
		if (probation.equalsIgnoreCase("No")) {
			probation = "N";
		}
		if (probation.equalsIgnoreCase("Yes")) {
			probation = "Y";
		}
		try {
			String queryForProductCode = "SELECT PRODUCTCODE FROM LOS_STAFF_SCHEME_LIMIT where designation='"
					+ designation + "' and PROBATION_TAG='" + probation + "'";

			Log.consoleLog(ifr, "queryForProductCode==>" + queryForProductCode);
			List<List<String>> queryForProductCodeResult = ifr.getDataFromDB(queryForProductCode);
			if (!queryForProductCodeResult.isEmpty()) {
				odRenewalprodCode = queryForProductCodeResult.get(0).get(0);
			}
			String validProduct = "SELECT productCode, productType from staff_valid_products";
			Log.consoleLog(ifr, "validProduct qquery==>" + validProduct);
			List<List<String>> validProductResult = ifr.getDataFromDB(validProduct);
			Log.consoleLog(ifr, "validProductResult qquery==>" + validProductResult);
			List<String> dpnProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("DPN"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> canaraGoldLoan = validProductResult.stream().filter(a1 -> a1.get(1).equals("CGL"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> odProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("OD"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> otherProductCode = validProductResult.stream()
					.filter(a1 -> a1.get(1).equals("GHL") || a1.get(1).equals("VL") || a1.get(1).equals("CM")
							|| a1.get(1).equals("CM") || a1.get(1).equals("CS") || a1.get(1).equals("OTHERS"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			List<String> allProductCodes = validProductResult.stream()
				    .map(a1 -> a1.get(0))
				    .collect(Collectors.toList());
			
			List<String> productCodesExceptOD = allProductCodes.stream()
				    .filter(code -> !odProductCode.contains(code))
				    .collect(Collectors.toList());
			
			Log.consoleLog(ifr, "dpnProductCode qquery==>" + dpnProductCode);
			Log.consoleLog(ifr, "odProductCode qquery==>" + odProductCode);
			String bankCode = pcm.getConstantValue(ifr, "CBS360V2STAFF", "BANKCODE");
			String channel = pcm.getConstantValue(ifr, "CBS360V2STAFF", "CHANNEL");
			String userId = pcm.getConstantValue(ifr, "CBS360V2STAFF", "USERID");
			String tBranch = pcm.getConstantValue(ifr, "CBS360V2STAFF", "TransactionBranch");
			String deleteQuery = "delete from SLOS_ALL_ACTIVE_PRODUCT where winame='" + processInstanceId + "'";
			ifr.clearCombo("chargesToDebitedFor");
			ifr.saveDataInDB(deleteQuery);
			request = "{\n"
					+ "            \"Operation\": \"advancedCustomer360viewEnquiry(SessionContext,long,String,String)\",\n"
					+ "            \"Service\": \"LOSManagerService\",\n" + "            \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + userId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + bankCode + "\",\n" + "            \"Channel\": \"" + channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + tBranch + "\",\n" + "            \"UserId\": \""
					+ userId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"customerId\": \""
					+ CustomerID + "\",\n" + "        \"accountStatus\": \"NONCLOSED\",\n"
					+ "        \"accountModule\": \"ALL\"\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);
			List<String> excludedDPNcode = new ArrayList<>();
			excludedDPNcode.add("743");
			excludedDPNcode.add("793");
			List<String> customerRelationShipIncluded = new ArrayList<>();
			customerRelationShipIncluded.add("SOW");
			customerRelationShipIncluded.add("JOF");
			customerRelationShipIncluded.add("JAF");

			List<String> odModuleCodeInclude = new ArrayList<>();
			odModuleCodeInclude.add("254");
			odModuleCodeInclude.add("253");
			odModuleCodeInclude.add("1129");
			odModuleCodeInclude.add("1127");
			if (!response.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);
				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body==>" + body);

				JSONObject bodyObj = (JSONObject) parser.parse(body);
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				Log.consoleLog(ifr, "CheckError===>" + checkError);

				if (checkError.equalsIgnoreCase("true")) {

					String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
					Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
					JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser
							.parse(XfaceCustomerAccountDetailsDTO);
					String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
					Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
					JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

					if (!AccountDetailsObj.isEmpty()) {
						for (int i = 0; i < AccountDetailsObj.size(); i++) {

							Log.consoleLog(ifr, "AccountDetailsObj==>" + AccountDetailsObj.get(i) + "salaryacc==>"
									+ salaryacc + "salaryacc");
							String inputJSON = AccountDetailsObj.get(i).toString();
							JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
							if (Optional.ofNullable(inputJSONObj.get("AccountId")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AccountId")).isEmpty()) {
								accountId = inputJSONObj.get("AccountId").toString();
								Log.consoleLog(ifr, "AccountId=============>" + accountId);
								if (accountId.trim().equalsIgnoreCase(salaryacc.trim())) {
									Log.consoleLog(ifr, "BranchCode==>" + inputJSONObj.get("BranchCode"));
									if (Optional.ofNullable(inputJSONObj.get("BranchCode")).isPresent()
											&& !Optional.ofNullable(inputJSONObj.get("BranchCode")).isEmpty()) {
										branchCode = inputJSONObj.get("BranchCode").toString();
										Log.consoleLog(ifr, "BranchCode==============>" + branchCode);
										String updateQuery = "UPDATE SLOS_TRN_LOANDETAILS SET DISB_BRANCH = LPAD('"+branchCode+"', 5, '0') WHERE PID = '" + processInstanceId + "'";
									    Log.consoleLog(ifr, "updateQuery==>" + updateQuery);
									    ifr.saveDataInDB(updateQuery);
					                    Log.consoleLog(ifr, "BranchCode==============>" + branchCode);
						                ifr.saveDataInDB(Query1);

									}
								}
							}
							
							if (Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty() && Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()&& Optional.ofNullable(inputJSONObj.get("CurrentStatus")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("CurrentStatus")).isEmpty()) {
								 String products = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCPRODCODE");
								 String relations = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCRELATION");
								 String accStatus = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCSTATUS");
								 
								String product = inputJSONObj.get("ProductCode").toString();
								String relation = inputJSONObj.get("CustomerRelationship").toString();
								String accStat = inputJSONObj.get("CurrentStatus").toString();
								
								Set<String> setS = new HashSet<>(Arrays.asList(products.split(",")));
								Set<String> setY = new HashSet<>(Arrays.asList(relations.split(",")));
								Set<String> setZ = new HashSet<>(Arrays.asList(accStatus.split(",")));
								if (setS.contains(product) && setY.contains(relation) && setZ.contains(accStat)) {
									String accId = inputJSONObj.get("AccountId").toString();
									ifr.addItemInCombo("chargesToDebitedFor", accId, accId);
								}
							}
							
							if (Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()) {
								productCode = inputJSONObj.get("ProductCode").toString();
								Log.consoleLog(ifr, "ProductCode=============>" + productCode);
							}
							if (dpnProductCode.contains(productCode)) {
								String originalBalanceStr = "";
								if (Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isEmpty()) {
									originalBalanceStr = inputJSONObj.get("OriginalBalance").toString();
									totalBalance += Double.parseDouble(originalBalanceStr);
								}
								if (Optional.ofNullable(inputJSONObj.get("ProductName")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ProductName")).isEmpty()) {

									productName = inputJSONObj.get("ProductName").toString();
									Log.consoleLog(ifr, "ProductName=============>" + productName);

								}

//								if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
//										&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()) {
//									amtInstal = Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
//									Log.consoleLog(ifr, "amtInstal=============>" + amtInstal);
//								}
							}
							if (canaraGoldLoan.contains(productCode)) {
								String originalBalanceStr = "";
								double notionalIntrest = 0.0;
								double ratInt = 0.0;
								if (Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatMaturity")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatMaturity")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isEmpty()) {
									ratInt = Double.parseDouble(inputJSONObj.get("RatInt").toString());
									originalBalanceStr = inputJSONObj.get("OriginalBalance").toString();
									notionalIntrest = (Double.parseDouble(originalBalanceStr) * ratInt) / 100;
									Log.consoleLog(ifr, "notionalIntrest===>" + notionalIntrest);
									String dataccOpen = inputJSONObj.get("DatAcctOpen").toString();
									String datMaturity = inputJSONObj.get("DatMaturity").toString();
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
									LocalDate date1 = LocalDate.parse(datMaturity, formatter);
									LocalDate date2 = LocalDate.parse(dataccOpen, formatter);
									double monthsBetween = ChronoUnit.MONTHS.between(date2, date1);
									Log.consoleLog(ifr, "monthsBetween===>" + monthsBetween);
									totalBalEG = (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									totalBalEGLoan = String.format("%.2f", totalBalEG);
									Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
									totalBal += (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									Log.consoleLog(ifr, "totalBal===>" + totalBal);
								}
							}
							if (odProductCode.contains(productCode.trim())) // productCode.equalsIgnoreCase("1129") ||
							// productCode.equalsIgnoreCase("253") ||
							// productCode.equalsIgnoreCase("254"))
							{
								Log.consoleLog(ifr, "enter into OD =============>");
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									odAccountDetailsDTO = inputJSONObj.get("ODAccountDetailsDTO").toString();
									Log.consoleLog(ifr, "odAccountDetailsDTO=============>" + odAccountDetailsDTO);
									JSONArray result = (JSONArray) parser.parse(odAccountDetailsDTO);
									Log.consoleLog(ifr, "result=============>" + result);

									if (result.size() > 0) {
										for (int k = 0; k < result.size(); k++) {
											JSONObject loanDetail = (JSONObject) result.get(k);

											if (Optional.ofNullable(loanDetail.get("ODExpiryDate")).isPresent()
													&& !Optional.ofNullable(loanDetail.get("ODExpiryDate")).isEmpty()) {
												SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss");
												SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
												String date1 = null;
												try {
													String plusdays = "";
													String minusdays = "";
													String Query = "SELECT DISTINCT PLUSDAYS,MINUSDAYS from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS WHERE NAM_PRODUCT LIKE '%"
															+ productCode
															+ "%' AND ( NAM_PRODUCT LIKE '%253%' OR NAM_PRODUCT LIKE '%254%'OR NAM_PRODUCT LIKE '%1129%')";
													Log.consoleLog(ifr, "plusdays and minusdays query===>" + Query);
													List<List<String>> res = ifr.getDataFromDB(Query);
													Log.consoleLog(ifr, "res===>" + res);
													if (!res.isEmpty()) {
														plusdays = res.get(0).get(0);
														Log.consoleLog(ifr, "plusdays:" + plusdays);
														minusdays = res.get(0).get(1);
														Log.consoleLog(ifr, "minusdays:" + minusdays);
													}
													date1 = loanDetail.get("ODExpiryDate").toString();
													Log.consoleLog(ifr, "date1===>" + date1);
													DateTimeFormatter dtf = DateTimeFormatter
															.ofPattern("yyyy-MM-dd HH:mm:ss");
													LocalDateTime givenDate = LocalDateTime.parse(date1, dtf);
													LocalDateTime currDate = LocalDateTime.now();
													LocalDateTime givenDatePlus90Days = givenDate
															.plusDays(Integer.parseInt(plusdays));
													LocalDateTime givenDateminus30Days = givenDate
															.minusDays(Integer.parseInt(minusdays));
													Log.consoleLog(ifr, "currDate==>" + currDate);
													Log.consoleLog(ifr, "givenDate==>" + givenDate);
													Log.consoleLog(ifr,
															"givenDateminus30Days==>" + givenDateminus30Days);
													Log.consoleLog(ifr, "givenDatePlus90Days==>" + givenDatePlus90Days);
													if (Optional.ofNullable(loanDetail.get("AmtOdLimit")).isPresent()
															&& !Optional.ofNullable(loanDetail.get("AmtOdLimit"))
																	.isEmpty()
															&& givenDate.compareTo(currDate) >= 0
															|| currDate.compareTo(givenDatePlus90Days) <= 0) {
														Log.consoleLog(ifr, "Inside AmtOdLimit limit block");

														amtOdLimit += Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														odValidProductCode.add(productCode);
														Log.consoleLog(ifr, "prdCode: " + productCode);
														Log.consoleLog(ifr, "AmtOdLimit==>" + amtOdLimit);
//                                                        Log.consoleLog(ifr, "Entering into odRenewalprodCode code==>");
//                                                        Log.consoleLog(ifr, "odRenewalprodCode==>" + odRenewalprodCode);
														odAccountId = accountId;
														odRenewalAmount = Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														limitNumber = Double
																.parseDouble(loanDetail.get("LimitNumber").toString());
														Log.consoleLog(ifr, "odAccountId=>" + odAccountId);
														SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
																"yyyy-MM-dd HH:mm:ss");
														SimpleDateFormat outputFormat1 = new SimpleDateFormat(
																"dd/MM/yyyy");
														String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
														Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
														Date date = simpleDateFormat1.parse(dateOdExpiry);
														odExpiryDate = outputFormat1.format(date);
														Log.consoleLog(ifr, "odExpiryDate=>" + odExpiryDate);
														dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
														if (currDate.compareTo(givenDateminus30Days) >= 0
																&& currDate.compareTo(givenDatePlus90Days) <= 0) {
															Log.consoleLog(ifr, "Inside renewal enabling block");
															flag = true;
														}
													}

												} catch (Exception e) {
													Log.consoleLog(ifr, "Exception in date formate===>" + e);
												}
//                                                if (isOdExpiredLessThanMonth(loanDetail, ifr)
//                                                        || isOdExpiredMoreThanThreeMonth(loanDetail, ifr)) {
//                                                    Log.consoleLog(ifr, "Entering into odRenewalprodCode code==>");
//                                                    Log.consoleLog(ifr, "odRenewalprodCode==>" + odRenewalprodCode);
//                                                    odAccountId = accountId;
//                                                    odRenewalAmount = Double
//                                                            .parseDouble(loanDetail.get("AmtOdLimit").toString());
//                                                    limitNumber = Double
//                                                            .parseDouble(loanDetail.get("LimitNumber").toString());
//                                                    Log.consoleLog(ifr, "odAccountId=>" + odAccountId);
//                                                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
//                                                            "yyyy-MM-dd HH:mm:ss");
//                                                    SimpleDateFormat outputFormat1 = new SimpleDateFormat("dd/MM/yyyy");
//                                                    String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
//                                                    Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
//                                                    Date date = simpleDateFormat1.parse(dateOdExpiry);
//                                                    odExpiryDate = outputFormat1.format(date);
//                                                    Log.consoleLog(ifr, "odExpiryDate=>" + odExpiryDate);
//                                                    dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
//
//                                                }
											}
										}
									}
								}
								if (Optional.ofNullable(inputJSONObj.get("ProductName")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ProductName")).isEmpty()) {
									productNameOD = inputJSONObj.get("ProductName").toString();
									Log.consoleLog(ifr, "productNameOD=============>" + productNameOD);
								}
//								if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
//										&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()) {
//									amtInstalOD = Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
//									 Log.consoleLog(ifr, "amtInstalOD=============>" + amtInstalOD);
//								}

							}
							//odProductCode.contains(productCode.trim())
							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& otherProductCode.contains(inputJSONObj.get("ProductCode").toString())
									&& !canaraGoldLoan.contains(productCode)
									&& productCodesExceptOD.contains(productCode.trim())) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstalOthers += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstalOthers=============>" + amtInstal);

								}
							}
							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !canaraGoldLoan.contains(productCode)
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& !excludedDPNcode.contains(inputJSONObj.get("ProductCode").toString())
									&& productCodesExceptOD.contains(productCode.trim())) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstal += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstal=============>" + amtInstal);

								}
							}
							// odModuleCodeInclude.contains(inputJSONObj.get("ProductCode").toString())
							if (Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("C")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& odProductCode.contains(inputJSONObj.get("ProductCode").toString())) {
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									amtODInt += odLoanDeduction(ifr, inputJSONObj, customerRelationShipIncluded,
											accountId, limitNumber, dateAccOpen);
								}

							} else if (inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& customerRelationShipIncluded
											.contains(inputJSONObj.get("CustomerRelationship").toString())
									&& accountId != null && !accountId.isEmpty() 
									&& productCodesExceptOD.contains(productCode.trim())) {
								try {
									SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
									SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
									String dateOdMarurity = inputJSONObj.get("DatMaturity").toString();
									Log.consoleLog(ifr, "dateOdMarurity==>" + dateOdMarurity);
									Date date = simpleDateFormat1.parse(dateOdMarurity);
									String dateOFMat = outputFormat.format(date);
									
									String dateAccountOpen = inputJSONObj.get("DatAcctOpen").toString();
									Log.consoleLog(ifr, "dateAccountOpen==>" + dateAccountOpen);
									Date dateAccopen = simpleDateFormat1.parse(dateAccountOpen);
									String dateOFAccOpen = outputFormat.format(dateAccopen);
									
									if (canaraGoldLoan.contains(productCode)) {
										loanInstallmentAmt = totalBalEGLoan;
										Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
										Log.consoleLog(ifr, "loanInstallmentAmt===>" + loanInstallmentAmt);
									} else {
										loanInstallmentAmt = inputJSONObj.get("LoanInstallmentAmt").toString();
									}

									String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT (winame,productcode,productname,limit,tenure,DateAccOpen,OUTSTANDING_BALANCE,EMI,LOAN_ACC_NUMBER,roi,isOd,DATEOFACCOPEN)"
											+ " values('" + processInstanceId + "','" + productCode + "','"
											+ inputJSONObj.get("ProductName").toString() + "','"
											+ inputJSONObj.get("OriginalBalance").toString() + "','" + dateOFMat + "','"
											+ inputJSONObj.get("AvailableBalanace").toString() + "','"
											+ inputJSONObj.get("LcyAmount").toString() + "','" + loanInstallmentAmt
											+ "','" + accountId.trim() + "','" + inputJSONObj.get("RatInt") + "','N','" +dateOFAccOpen+"')";

									ifr.saveDataInDB(insertQuery);
									Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);

									// return "Success";
								} catch (Exception e) {
									Log.consoleLog(ifr, "Exception in SLOS_ALL_ACTIVE_PRODUCT==>" + e);
								}
							}

						}
					}

				} else {
					String[] ErrorData = checkError.split("#");
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR + ":" + apiErrorMessage;
			} else {
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

				ifr.addDataToGrid("first_staffdetails_table2", array);

				JSONObject obj = new JSONObject();
				obj.put("ProductCode", productCode);
				obj.put("OriginalBalance", totalBalance);
				obj.put("AmtOdLimit", amtOdLimit);
				obj.put("ProductName", productName);
				double finalAmt = amtInstal + totalBal;
				String finalAmtInstal = String.format("%.2f", finalAmt);
				obj.put("AmtInstal", Double.parseDouble(finalAmtInstal));
				Log.consoleLog(ifr, "amtInstal==>" + amtInstal);
				Log.consoleLog(ifr, "totalBal==>" + totalBal);
				obj.put("AmtOdInt", amtODInt + amtInstalOthers + totalBal);
				Log.consoleLog(ifr, "amtODInt==>" + amtODInt);
				Log.consoleLog(ifr, "amtInstalOthers==>" + amtInstalOthers);
				obj.put("ProductNameOD", productNameOD);
				obj.put("AccountID", accountId);
				ifr.setValue("Existing_OD_account", odAccountId);
				obj.put("odProductCode", odValidProductCode);
				Log.consoleLog(ifr, "obj==>" + obj.toString());
				Log.consoleLog(ifr, "flag=====================================>" + flag);
				Log.consoleLog(ifr, "odExpiryDate=====================================>" + odExpiryDate);
				if (!(odExpiryDate == null && odExpiryDate.isEmpty()) && flag) {
					Log.consoleLog(ifr, "Inside OdExpiry Diabling block==>");
					obj.put("IsRenewableButton", "disableNo");

				} else {
					obj.put("IsRenewableButton", "disableYes");
				}
				ifr.setValue("Existing_OD_Expiry_Date", odExpiryDate);
				ifr.setValue("first_staffdetails_textbox75", String.valueOf(amtOdLimit));
				// ifr.setValue("renewal_amount", String.valueOf(odRenewalAmount));

				Log.consoleLog(ifr, "obj==>" + obj.toString());
				return obj.toJSONString();
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
			Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR+ ":" + apiErrorMessage;
	}

	private boolean isOdExpiredMoreThanThreeMonth(JSONObject jsonObject, IFormReference ifr) {
		try {
			String odDate = jsonObject.get("ODExpiryDate").toString();
			Log.consoleLog(ifr, "odDate=>" + odDate);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime givenDate = LocalDateTime.parse(odDate, dtf);
			LocalDateTime currDate = LocalDateTime.now();
			LocalDateTime givenDatePlus90Days = givenDate.plusDays(90);
			Log.consoleLog(ifr, "currDate=>" + currDate);
			Log.consoleLog(ifr, "givenDatePlus90Days=>" + givenDatePlus90Days);
			long daysBetween = Duration.between(givenDate, currDate).toDays();
			if (daysBetween <= 0 && daysBetween >= -90) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, e.getMessage());
			return false;
		}
	}

	public double odLoanDeduction(IFormReference ifr, JSONObject inputJSONObj,
			List<String> customerRelationShipIncluded, String accountId, double limitNumber, String dateAccOpen)
			throws java.text.ParseException {
		double odResult = 0.0;
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
				&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
			// String odAccountDetailsDTO =
			// inputJSONObj.get("ODAccountDetailsDTO").toString();
			Log.consoleLog(ifr, "inputJSONObj=============>" + inputJSONObj.get("ODAccountDetailsDTO"));

			JSONArray result = (JSONArray) inputJSONObj.get("ODAccountDetailsDTO");
			Log.consoleLog(ifr, "result=============>" + result);

			if (result.size() > 0) {
				for (int k = 0; k < result.size(); k++) {
					JSONObject loanDetail = (JSONObject) result.get(k);
					Log.consoleLog(ifr, "In for loop");
					if (Optional.ofNullable(loanDetail.get("ODExpiryDate")).isPresent()
							&& !Optional.ofNullable(loanDetail.get("ODExpiryDate")).isEmpty()) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date1 = null;
						try {
							date1 = simpleDateFormat.parse(loanDetail.get("ODExpiryDate").toString());
							Log.consoleLog(ifr, "date1===>" + date1);
						} catch (Exception e) {
							Log.consoleLog(ifr, "Exception in date formate===>" + e);
						}
						LocalDate localDate = LocalDate.now();
						// Convert LocalDate to Date
						Date date2 = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
						Log.consoleLog(ifr, "date2===>" + date2);
						if (Optional.ofNullable(loanDetail.get("AmtOdLimit")).isPresent()
								&& !Optional.ofNullable(loanDetail.get("AmtOdLimit")).isEmpty()) {
							double od = ((Double.parseDouble(loanDetail.get("AmtOdLimit").toString())
									* Double.parseDouble(loanDetail.get("InterestRate").toString())) / 100) / 12;
							odResult += od;

							if (customerRelationShipIncluded
									.contains(inputJSONObj.get("CustomerRelationship").toString()) && accountId != null
									&& !accountId.isEmpty()) {
//								String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT(winame,productcode,productname,limit,tenure,DateAccOpen,roi,isOd) "
//										+ "values('" + processInstanceId + "','"
//										+ inputJSONObj.get("ProductCode").toString() + "','"
//										+ inputJSONObj.get("ProductName").toString() + "','"
//										+ loanDetail.get("AmtOdLimit").toString() + "','"
//										+ inputJSONObj.get("Tenure").toString() + "','"
//										+ inputJSONObj.get("DatAcctOpen").toString() + "','"
//										+ inputJSONObj.get("RatInt") + "','Y'')";
//								ifr.saveDataInDB(insertQuery);
//								Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);
								SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
								String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
								String limitStartdate = loanDetail.get("LimitStartDat").toString();
								Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
								Log.consoleLog(ifr, "limitStartdate==>" + limitStartdate);
								Date date = simpleDateFormat1.parse(dateOdExpiry);
								Date dateLimStartDate = simpleDateFormat1.parse(limitStartdate);
								String expiryODDate = outputFormat.format(date);
								String limitStartDate = outputFormat.format(dateLimStartDate);
								Log.consoleLog(ifr, "expiryODDate==>" + expiryODDate);
								Log.consoleLog(ifr, "limitStartDate==>" + limitStartDate);
								String accId = inputJSONObj.get("AccountId").toString();

								String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT (winame, productcode, productname, limit, tenure, DateAccOpen, OUTSTANDING_BALANCE, EMI, LIMITNUMBER, DATACCOP, LOAN_ACC_NUMBER, roi, isOd, LIMITSTARTDATE) "
										+ "VALUES ('" + processInstanceId + "', '"
										+ inputJSONObj.get("ProductCode").toString() + "', '"
										+ inputJSONObj.get("ProductName").toString() + "', '"
										+ loanDetail.get("AmtOdLimit").toString() + "', '" + expiryODDate + "', '"
										+ inputJSONObj.get("AvailableBalanace").toString() + "', '"
										+ inputJSONObj.get("LcyAmount").toString() + "', '"
										+ inputJSONObj.get("LoanInstallmentAmt").toString() + "', '" + limitNumber
										+ "', '" + dateAccOpen + "', '" + accId.trim() + "', '"
										+ inputJSONObj.get("RatInt") + "', 'Y','" + limitStartDate + "')";

								Log.consoleLog(ifr, "All_ACTIVE_PRODUCT_INSERT_OD_QUERY==>" + insertQuery);
								ifr.saveDataInDB(insertQuery);

								String delQuery = "DELETE FROM SLOS_ALL_ACTIVE_PRODUCT "
										+ "WHERE TO_DATE(tenure, 'DD/MM/YYYY') < ( "
										+ "    SELECT MAX(TO_DATE(tenure, 'DD/MM/YYYY')) "
										+ "    FROM SLOS_ALL_ACTIVE_PRODUCT " + "    WHERE winame = '"
										+ processInstanceId + "' " + "      AND loan_acc_number = '" + accId.trim()
										+ "' " + "      AND isod = 'Y' " + ") " + "AND winame = '" + processInstanceId
										+ "' " + "AND loan_acc_number = '" + accId.trim() + "' " + "AND isod = 'Y'";

								Log.consoleLog(ifr, "All_ACTIVE_PRODUCT_DELETE_OD_QUERY==>" + delQuery);
								ifr.saveDataInDB(delQuery);

							}

						}
					}
				}
			}
		}
		return odResult;
	}

	private boolean isOdExpiredLessThanMonth(JSONObject jsonObject, IFormReference ifr) {
		try {
			String odDate = jsonObject.get("ODExpiryDate").toString();
			Log.consoleLog(ifr, "odDate=>" + odDate);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime givenDate = LocalDateTime.parse(odDate, dtf);
			LocalDateTime currDate = LocalDateTime.now();
			Log.consoleLog(ifr, "currDate=>" + currDate);
			long daysBetween = Duration.between(givenDate, currDate).toDays();
			if (daysBetween >= 0 && daysBetween <= 30) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, e.getMessage());
			return false;
		}

		// return true;
	}

	public String advance360KnockOff(IFormReference ifr) {
		Log.consoleLog(ifr, "Entered into ExecuteCBSAdvanced360Inquiryv2...");

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Advanced360Inquiryv2";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String classification = "";

		String CustomerID = "";
		try {
			String customerQuery = "select CUSTOMERID from los_trn_customersummary where winame='" + processInstanceId
					+ "'";
			List<List<String>> customerResp = ifr.getDataFromDB(customerQuery);
			for (List<String> res : customerResp) {
				CustomerID = res.get(0);
			}
			String bankCode = pcm.getConstantValue(ifr, "CBS360V2STAFF", "BANKCODE");
			String channel = pcm.getConstantValue(ifr, "CBS360V2STAFF", "CHANNEL");
			String userId = pcm.getConstantValue(ifr, "CBS360V2STAFF", "USERID");
			String tBranch = pcm.getConstantValue(ifr, "CBS360V2STAFF", "TransactionBranch");
			request = "{\n"
					+ "            \"Operation\": \"advancedCustomer360viewEnquiry(SessionContext,long,String,String)\",\n"
					+ "            \"Service\": \"LOSManagerService\",\n" + "            \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + userId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + bankCode + "\",\n" + "            \"Channel\": \"" + channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + tBranch + "\",\n" + "            \"UserId\": \""
					+ userId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"customerId\": \""
					+ CustomerID + "\",\n" + "        \"accountStatus\": \"NONCLOSED\",\n"
					+ "        \"accountModule\": \"ALL\"\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);
				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body==>" + body);

				JSONObject bodyObj = (JSONObject) parser.parse(body);
				String checkError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				Log.consoleLog(ifr, "CheckError===>" + checkError);

				if (checkError.equalsIgnoreCase("true")) {
					JSONObject customerRes = (JSONObject) bodyObj.get("CustomerResponse");
					JSONObject XfaceCustomerBasicInquiryDTOCurr = (JSONObject) customerRes
							.get("XfaceCustomerBasicInquiryDTO");

					String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
					Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
					JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser
							.parse(XfaceCustomerAccountDetailsDTO);
					Log.consoleLog(ifr, "obj parsed=>");
					String custCrrDesc = XfaceCustomerBasicInquiryDTOCurr.get("CustCrrDesc").toString();
					if (!custCrrDesc.isEmpty() && (custCrrDesc.equals("SMA 1") || custCrrDesc.equals("SMA 2"))) {

						return AccelatorStaffConstant.SMA_ERROR_MESSAGE;
					}
					Log.consoleLog(ifr, "AccountDetails=>");
					String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
					Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
					JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

					if (!AccountDetailsObj.isEmpty()) {
						for (int i = 0; i < AccountDetailsObj.size(); i++) {

							String inputJSON = AccountDetailsObj.get(i).toString();
							JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);

							if (Optional.ofNullable(inputJSONObj.get("Classification")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("Classification")).isEmpty()) {
								classification = inputJSONObj.get("Classification").toString();
								Log.consoleLog(ifr, "Classification =============>" + classification);

								if (classification.trim().equalsIgnoreCase("SUSPENDED")) {
									Log.consoleLog(ifr, "classification =============>" + classification);
									return AccelatorStaffConstant.NPA_ERROR_MESSAGE;
								}

							}
							String totalOverDue = (String) inputJSONObj.get("TotalOverdue");
							if (!totalOverDue.trim().isEmpty()) {
								Log.consoleLog(ifr, "totalOverDue =============>" + totalOverDue);
								double totalOverDueValue = Double.parseDouble(totalOverDue.trim());
								Log.consoleLog(ifr, "totalOverDue =============>" + totalOverDue);
								String updateQuery = "UPDATE SLOS_TRN_LOANSUMMARY SET TOTALOVERDUE='" + totalOverDueValue + "' where winame='" + processInstanceId + "'";
								ifr.saveDataInDB(updateQuery);
								Log.consoleLog(ifr, "total overdue==>" + updateQuery);
								String overdue = pcm.getConstantValue(ifr, "STAFF LOAN", "TOTALOVERDUE");
								if (totalOverDueValue > Double.parseDouble(overdue) && !totalOverDue.trim().isEmpty()) {
									Log.consoleLog(ifr, "totalOverDue =============>" + totalOverDue);
									return AccelatorStaffConstant.TOTALOVERDUE_ERROR_MESSAGE + totalOverDue;
								}
							}
							// odModuleCodeInclude.contains(inputJSONObj.get("ProductCode").toString())

						}
					}

				} else {
					String[] ErrorData = checkError.split("#");
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR + ":" + apiErrorMessage;
			} else {

				return RLOS_Constants.SUCCESS;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
			Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

	public String getDemographic(IFormReference ifr) {

		String apiName = "Demographic";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";

		try {
			// String APIName = "CBS_Demographic";
//        String ErrorCode = "";
//        String ErrorMessage = "";

			Log.consoleLog(ifr, "Entered into staff ExecuteCBS_Demographic...");
			// String Dataset = "";
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formattedDate = dateFormat.format(currentDate);
			int Age = 0;
			int Years = 0;
			String WriteOffDate = "NA";
			String writeOffPresent = "No";
			String CustomerCategory = "";
			String VoterID = "";
			String PassPortNo = "";
			String DrivingLicense = "";
			String strFlgCustType = "";
			String DateOfCustOpen = "";
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String BankCode = pcm.getConstantValue(ifr, "CBSDGRAPH", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSDGRAPH", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSDGRAPH", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSDGRAPH", "TBRANCH");
			String CustomerID = "";
			String customerQuery = "select CUSTOMERID from los_trn_customersummary where winame='" + processInstanceId
					+ "'";
			List<List<String>> customerResp = ifr.getDataFromDB(customerQuery);
			for (List<String> res : customerResp) {
				CustomerID = res.get(0);
			}
			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \"\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ formattedDate + "\",\n" + "        \"CustomerID\": \"" + CustomerID + "\"\n" + "    }\n" + "}";

			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(response);
				JSONObject resultObj = new JSONObject(OutputJSON);

				String body = resultObj.get("body").toString();
				JSONObject bodyJSON = (JSONObject) parser.parse(body);
				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyJSON);
				// String CheckError = cm.GetAPIErrorResponse(ifr, bodyJSON);
				if (CheckError.equalsIgnoreCase("true")) {
					// String CustDemographInqResponse =
					// bodyJSON.get("CustDemographInqResponse").toString();
					// JSONObject CustDemographInqResponseJSON = (JSONObject)
					// parser.parse(CustDemographInqResponse);
					JSONObject CustDemographInqResponseJSON = (JSONObject) bodyJSON.get("CustDemographInqResponse");

					CustomerCategory = cf.getJsonValue(CustDemographInqResponseJSON, "CustomerCategory");
					Log.consoleLog(ifr, "CustomerCategory : " + CustomerCategory);
					String DateOfBirth = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfBirth");
					Log.consoleLog(ifr, "DateOfBirth===>" + DateOfBirth);

					DateOfCustOpen = cf.getJsonValue(CustDemographInqResponseJSON, "DateOfCustOpen");
					Log.consoleLog(ifr, "DateOfCustOpen===>" + DateOfCustOpen);
					WriteOffDate = cf.getJsonValue(CustDemographInqResponseJSON, "WriteOffDate");
					if (!WriteOffDate.trim().isEmpty() && !WriteOffDate.equals("1800-01-01")) {
						return AccelatorStaffConstant.WRITE_OFF_ERROR_MESSAGE;
					}

					Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);

//                    Log.consoleLog(ifr, "WriteOffDate===>" + WriteOffDate);
//                    if (!(WriteOffDate.equalsIgnoreCase(""))) {
//                        JSONObject WriteOffDateJSON = (JSONObject) parser.parse(WriteOffDate);
//                        Log.consoleLog(ifr, "WriteOffDateJSON.size()===>" + WriteOffDateJSON.size());
//
//                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
//                        String formattedDate1 = dateFormat1.format(currentDate);
//                        if (!WriteOffDateJSON.isEmpty()) {
//                            writeOffPresent = "Yes";
//                            WriteOffDate = formattedDate1;
//                        } else {
//                            WriteOffDate = formattedDate1;
//                        }
//                    }
					Log.consoleLog(ifr, "Years===>" + Years);

					try {
						Log.consoleLog(ifr, "DemoGraphic API");
						String ApplicantCategory = cf.getJsonValue(CustDemographInqResponseJSON, "ApplicantCategory");
						VoterID = cf.getJsonValue(CustDemographInqResponseJSON, "VoterID");
						PassPortNo = cf.getJsonValue(CustDemographInqResponseJSON, "PassPortNo");
						DrivingLicense = cf.getJsonValue(CustDemographInqResponseJSON, "DrivingLicense");
						String Landline = cf.getJsonValue(CustDemographInqResponseJSON, "Landline");
						// String strDATEOFCUSTOPEN=cf.getJsonValue(CustDemographInqResponseJSON,
						// "DateOfCustOpen");
						strFlgCustType = cf.getJsonValue(CustDemographInqResponseJSON, "FlgCustType");

					} catch (Exception e) {
						Log.consoleLog(ifr, "Exception in Demographic API new column adding " + e);
					}

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
			Log.consoleLog(ifr, "CBS_DemographicCBS_Demographic ");

//                String APIStatus = "";
//                if (ErrorMessage.equalsIgnoreCase("")) {
//                    APIStatus = "SUCCESS";
//                } else {
//                    APIStatus = "FAIL";
//                }
//                Log.consoleLog(ifr, "CaptureRequestResponse Calling ");
//                //      cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//                if (!(ErrorMessage.equalsIgnoreCase(""))) {
//                    return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//                }
		} catch (ParseException e) {
			Log.consoleLog(ifr, "Exception/CBS_Demographic===>" + e);
			Log.errorLog(ifr, "Exception/CBS_Demographic===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, "", serviceName, request, response, apiErrorCode, apiErrorMessage,
					apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
		// return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr,
		// serviceName, ErrorCode);
	}

	public String executeCBSAdvanced360Inquiryv2VL(IFormReference ifr, String ProcessInstanceId, String CustomerID,
			String salaryacc, String designation, String probation, List<String> validProductCode,
			boolean isEligibilityCal, String value) {
		Log.consoleLog(ifr, "Entered into ExecuteCBSAdvanced360Inquiryv2...");
		Validator valid = new KnockOffValidator("");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Advanced360Inquiryv2";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		double totalBalance = 0.0;
		double totalBal = 0.0;
		String totalBalEGLoan = "";
		double totalBalEG = 0.0;
		String productCode = "";
		String odAccountDetailsDTO = "";
		double amtOdLimit = 0.0;
		double amtODInt = 0;
		String productName = "";
		double amtInstal = 0.0;
		String productNameOD = "";
		String accountId = "";
		String warning = "NoError";
		// String branchCode = "";
		String Query1 = "";
		String odExpiryDate = "";
		String odAccountId = "";
		String odRenewalprodCode = "";
		double amtInstalOthers = 0.0;
		double odRenewalAmount = 0.0;
		double limitNumber = 0.0;
		String dateAccOpen = "";
		String loanInstallmentAmt = "";
		String salaryBranchDPCode = "";
		boolean flag = false;
		boolean isElgibleForNth =false;
		Set<String> warningSet = new HashSet<>();
		JSONObject vlProductCode = new JSONObject();
		for (String pc : validProductCode) {
			vlProductCode.put(pc, 0);
		}
		JSONArray odValidProductCode = new JSONArray();
		if (probation.equalsIgnoreCase("No")) {
			probation = "N";
		}
		if (probation.equalsIgnoreCase("Yes")) {
			probation = "Y";
		}
		try {
			String queryForProductCode = "SELECT PRODUCTCODE FROM LOS_STAFF_SCHEME_LIMIT where designation='"
					+ designation + "' and PROBATION_TAG='" + probation + "'";

			Log.consoleLog(ifr, "queryForProductCode==>" + queryForProductCode);
			List<List<String>> queryForProductCodeResult = ifr.getDataFromDB(queryForProductCode);
			if (!queryForProductCodeResult.isEmpty()) {
				odRenewalprodCode = queryForProductCodeResult.get(0).get(0);
			}
			String validProduct = "SELECT productCode, productType from staff_valid_products";
			Log.consoleLog(ifr, "validProduct qquery==>" + validProduct);
			List<List<String>> validProductResult = ifr.getDataFromDB(validProduct);
			Log.consoleLog(ifr, "validProductResult qquery==>" + validProductResult);

			List<String> canaraGoldLoan = validProductResult.stream().filter(a1 -> a1.get(1).equals("CGL"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> odProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("OD"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> otherProductCode = validProductResult.stream()
					.filter(a1 -> a1.get(1).equals("GHL") || a1.get(1).equals("VL") || a1.get(1).equals("CM")
							|| a1.get(1).equals("CM") || a1.get(1).equals("CS") || a1.get(1).equals("OTHERS"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			
			List<String> kavachProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("Kavach"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			List<String> allProductCodes = validProductResult.stream()
				    .map(a1 -> a1.get(0))
				    .collect(Collectors.toList());
			
			List<String> productCodesExceptOD = allProductCodes.stream()
				    .filter(code -> !odProductCode.contains(code))
				    .collect(Collectors.toList());
			
			// Log.consoleLog(ifr, "dpnProductCode qquery==>" + dpnProductCode);
			Log.consoleLog(ifr, "odProductCode qquery==>" + odProductCode);
			String bankCode = pcm.getConstantValue(ifr, "CBS360V2STAFF", "BANKCODE");
			String channel = pcm.getConstantValue(ifr, "CBS360V2STAFF", "CHANNEL");
			String userId = pcm.getConstantValue(ifr, "CBS360V2STAFF", "USERID");
			String tBranch = pcm.getConstantValue(ifr, "CBS360V2STAFF", "TransactionBranch");

			String queryForLoanAccNum = "select LOAN_ACC_NUMBER from SLOS_ALL_ACTIVE_PRODUCT where winame='"
					+ processInstanceId + "'";
			List<List<String>> resForLoanAccNum = ifr.getDataFromDB(queryForLoanAccNum);
			Log.consoleLog(ifr, "queryForLoanAccNum : " + queryForLoanAccNum);
			Log.consoleLog(ifr, "resForLoanAccNum==>" + resForLoanAccNum);
//				if (!resForLoanAccNum.isEmpty()) {
//					for (int i = 0; i < resForLoanAccNum.size(); i++) {
//						String accountIds = resForLoanAccNum.get(i).get(0);
//						Log.consoleLog(ifr, "accountIds==>" + accountIds);
//						try {
//							String deleteQuery = "DELETE FROM SLOS_ALL_ACTIVE_PRODUCT WHERE ROWID IN (SELECT rid FROM ( SELECT ROWID AS rid, ROW_NUMBER() OVER (PARTITION BY winame, LOAN_ACC_NUMBER ORDER BY ROWID) AS rn FROM SLOS_ALL_ACTIVE_PRODUCT WHERE winame = '"
//									+ processInstanceId + "' AND LOAN_ACC_NUMBER = '" + accountIds.trim()
//									+ "') WHERE rn > 1)";
//							Log.consoleLog(ifr, "deleteQuery qquery==>" + deleteQuery);
//							ifr.saveDataInDB(deleteQuery);
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}
//
//				}
			if(!value.equalsIgnoreCase("CoBorrower")) {
			String deleteQuery = "delete from SLOS_ALL_ACTIVE_PRODUCT where winame='" + processInstanceId + "'";
			ifr.saveDataInDB(deleteQuery);
			}

			request = "{\n"
					+ "            \"Operation\": \"advancedCustomer360viewEnquiry(SessionContext,long,String,String)\",\n"
					+ "            \"Service\": \"LOSManagerService\",\n" + "            \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + userId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + bankCode + "\",\n" + "            \"Channel\": \"" + channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + tBranch + "\",\n" + "            \"UserId\": \""
					+ userId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"customerId\": \""
					+ CustomerID + "\",\n" + "        \"accountStatus\": \"NONCLOSED\",\n"
					+ "        \"accountModule\": \"ALL\"\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);
			List<String> excludedDPNcode = new ArrayList<>();
			excludedDPNcode.add("743");
			excludedDPNcode.add("793");
			List<String> customerRelationShipIncluded = new ArrayList<>();
			customerRelationShipIncluded.add("SOW");
			customerRelationShipIncluded.add("JOF");
			customerRelationShipIncluded.add("JAF");
			List<String> odModuleCodeInclude = new ArrayList<>();
			odModuleCodeInclude.add("254");
			odModuleCodeInclude.add("253");
			odModuleCodeInclude.add("1129");
			odModuleCodeInclude.add("1127");
			if (!response.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);
				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body==>" + body);

				JSONObject bodyObj = (JSONObject) parser.parse(body);
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				Log.consoleLog(ifr, "CheckError===>" + checkError);

				if (checkError.equalsIgnoreCase("true")) {

					String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
					Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
					JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser
							.parse(XfaceCustomerAccountDetailsDTO);
					String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
					Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
					JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

					if (!AccountDetailsObj.isEmpty()) {
						for (int i = 0; i < AccountDetailsObj.size(); i++) {

							Log.consoleLog(ifr, "AccountDetailsObj==>" + AccountDetailsObj.get(i) + "salaryacc==>"
									+ salaryacc + "salaryacc");
							String inputJSON = AccountDetailsObj.get(i).toString();
							JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
							if (Optional.ofNullable(inputJSONObj.get("AccountId")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AccountId")).isEmpty()
									&& !isEligibilityCal) {
								accountId = inputJSONObj.get("AccountId").toString();
								Log.consoleLog(ifr, "AccountId=============>" + accountId);
								if (accountId.trim().equalsIgnoreCase(salaryacc.trim())) {
									Log.consoleLog(ifr, "BranchCode==>" + inputJSONObj.get("BranchCode"));
									if (Optional.ofNullable(inputJSONObj.get("BranchCode")).isPresent()
											&& !Optional.ofNullable(inputJSONObj.get("BranchCode")).isEmpty()) {
										salaryBranchDPCode = inputJSONObj.get("BranchCode").toString();
										Log.consoleLog(ifr, "BranchCode==============>" + salaryBranchDPCode);					                        
										String updateQuery = "UPDATE SLOS_TRN_LOANDETAILS SET DISB_BRANCH = LPAD('"+salaryBranchDPCode+"', 5, '0') WHERE PID = '" + processInstanceId + "'";
									    Log.consoleLog(ifr, "updateQuery==>" + updateQuery);
									    ifr.saveDataInDB(updateQuery);
					                    Log.consoleLog(ifr, "BranchCode==============>" + salaryBranchDPCode);
						                ifr.saveDataInDB(Query1);

									}
								}
							}
							productCode = valid.getValue(ifr, inputJSONObj, "ProductCode", "");
							Log.consoleLog(ifr, "ProductCode=============>" + productCode);
							if (kavachProductCode.contains(productCode) && value.contains("kavach")) {
								
								isElgibleForNth= true;
							}
							if (validProductCode.contains(productCode)) {
								if(value.equalsIgnoreCase("Barrower")) {
								double val = Double
										.parseDouble(valid.getValue(ifr, inputJSONObj, "OriginalBalance", "0.0"));
								Log.consoleLog(ifr, "productCode val ==>" + val);
								vlProductCode.put(productCode,
										Double.parseDouble(vlProductCode.get(productCode).toString()) + val);
								Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
								String vlProductCodeString = vlProductCode.toString();
								Log.consoleLog(ifr, "vlProductCodeString ==>" + vlProductCodeString);
								productName = valid.getValue(ifr, inputJSONObj, "ProductName", "");
								Log.consoleLog(ifr, "productName ==>" + productName);
								String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set PRODUCTCODEUSEDBORROWER='"
										+ vlProductCodeString + "' " + "where WINAME='" + processInstanceId + "'";
                                
								Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
								ifr.saveDataInDB(ageQuery);
							}
								else if(value.equalsIgnoreCase("CoBorrower")) {
									double val = Double
											.parseDouble(valid.getValue(ifr, inputJSONObj, "OriginalBalance", "0.0"));
									Log.consoleLog(ifr, "productCode val ==>" + val);
									vlProductCode.put(productCode,
											Double.parseDouble(vlProductCode.get(productCode).toString()) + val);
									Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
									String vlProductCodeString = vlProductCode.toString();
									Log.consoleLog(ifr, "vlProductCodeString ==>" + vlProductCodeString);
									productName = valid.getValue(ifr, inputJSONObj, "ProductName", "");
									Log.consoleLog(ifr, "productName ==>" + productName);
									String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set PRODUCTCODEUSEDCOBORROWER='"
											+ vlProductCodeString + "' " + "where WINAME='" + processInstanceId + "'";
	                                
									Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
									ifr.saveDataInDB(ageQuery);
								}
								else
								{
									double val = Double
											.parseDouble(valid.getValue(ifr, inputJSONObj, "OriginalBalance", "0.0"));
									Log.consoleLog(ifr, "productCode val ==>" + val);
									vlProductCode.put(productCode,
											Double.parseDouble(vlProductCode.get(productCode).toString()) + val);
									Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
									String vlProductCodeString = vlProductCode.toString();
									Log.consoleLog(ifr, "vlProductCodeString ==>" + vlProductCodeString);
									productName = valid.getValue(ifr, inputJSONObj, "ProductName", "");
									Log.consoleLog(ifr, "productName ==>" + productName);
									String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set PRODUCTCODEUSED='"
											+ vlProductCodeString + "' " + "where WINAME='" + processInstanceId + "'";
	                                
									Log.consoleLog(ifr, "ageQuery====>" + ageQuery);
									ifr.saveDataInDB(ageQuery);
								}
							}
							warningSet.add(valid.validate(bodyObj, "TotalOverdue", 0).getMessage());
							warningSet.add(valid.validate(bodyObj, "Sma2Count12Months", 0).getMessage());
							warningSet.add(valid.validate(bodyObj, "Sma1Count12months", 0).getMessage());
							if (canaraGoldLoan.contains(productCode) && !isEligibilityCal) {
								String originalBalanceStr = "";
								double notionalIntrest = 0.0;
								double ratInt = 0.0;
								if (Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatMaturity")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatMaturity")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isEmpty()) {
									ratInt = Double.parseDouble(inputJSONObj.get("RatInt").toString());
									originalBalanceStr = inputJSONObj.get("OriginalBalance").toString();
									notionalIntrest = (Double.parseDouble(originalBalanceStr) * ratInt) / 100;
									Log.consoleLog(ifr, "notionalIntrest===>" + notionalIntrest);
									String dataccOpen = inputJSONObj.get("DatAcctOpen").toString();
									String datMaturity = inputJSONObj.get("DatMaturity").toString();
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
									LocalDate date1 = LocalDate.parse(datMaturity, formatter);
									LocalDate date2 = LocalDate.parse(dataccOpen, formatter);
									double monthsBetween = ChronoUnit.MONTHS.between(date2, date1);
									Log.consoleLog(ifr, "monthsBetween===>" + monthsBetween);
									totalBalEG = (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									totalBalEGLoan = String.format("%.2f", totalBalEG);
									Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
									totalBal += (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									Log.consoleLog(ifr, "totalBal===>" + totalBal);
								}
							}

							if (odProductCode.contains(productCode.trim()) && !isEligibilityCal) // productCode.equalsIgnoreCase("1129")
																									// ||
							// productCode.equalsIgnoreCase("253") ||
							// productCode.equalsIgnoreCase("254"))
							{
								Log.consoleLog(ifr, "enter into OD =============>");
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									odAccountDetailsDTO = inputJSONObj.get("ODAccountDetailsDTO").toString();
									Log.consoleLog(ifr, "odAccountDetailsDTO=============>" + odAccountDetailsDTO);
									JSONArray result = (JSONArray) parser.parse(odAccountDetailsDTO);
									Log.consoleLog(ifr, "result=============>" + result);

									if (result.size() > 0) {
										for (int k = 0; k < result.size(); k++) {
											JSONObject loanDetail = (JSONObject) result.get(k);

											if (Optional.ofNullable(loanDetail.get("ODExpiryDate")).isPresent()
													&& !Optional.ofNullable(loanDetail.get("ODExpiryDate")).isEmpty()) {
												SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss");
												SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
												String date1 = null;
												try {
													String plusdays = "";
													String minusdays = "";
													String Query = "SELECT DISTINCT PLUSDAYS,MINUSDAYS from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS WHERE NAM_PRODUCT LIKE '%"
															+ productCode
															+ "%' AND ( NAM_PRODUCT LIKE '%253%' OR NAM_PRODUCT LIKE '%254%'OR NAM_PRODUCT LIKE '%1129%')";
													Log.consoleLog(ifr, "plusdays and minusdays query===>" + Query);
													List<List<String>> res = ifr.getDataFromDB(Query);
													Log.consoleLog(ifr, "res===>" + res);
													if (!res.isEmpty()) {
														plusdays = res.get(0).get(0);
														Log.consoleLog(ifr, "plusdays:" + plusdays);
														minusdays = res.get(0).get(1);
														Log.consoleLog(ifr, "minusdays:" + minusdays);
													}
													date1 = loanDetail.get("ODExpiryDate").toString();
													Log.consoleLog(ifr, "date1===>" + date1);
													DateTimeFormatter dtf = DateTimeFormatter
															.ofPattern("yyyy-MM-dd HH:mm:ss");
													LocalDateTime givenDate = LocalDateTime.parse(date1, dtf);
													LocalDateTime currDate = LocalDateTime.now();
													LocalDateTime givenDatePlus90Days = givenDate
															.plusDays(Integer.parseInt(plusdays));
													LocalDateTime givenDateminus30Days = givenDate
															.minusDays(Integer.parseInt(minusdays));
													Log.consoleLog(ifr, "currDate==>" + currDate);
													Log.consoleLog(ifr, "givenDate==>" + givenDate);
													Log.consoleLog(ifr,
															"givenDateminus30Days==>" + givenDateminus30Days);
													Log.consoleLog(ifr, "givenDatePlus90Days==>" + givenDatePlus90Days);
													if (Optional.ofNullable(loanDetail.get("AmtOdLimit")).isPresent()
															&& !Optional.ofNullable(loanDetail.get("AmtOdLimit"))
																	.isEmpty()
															&& givenDate.compareTo(currDate) >= 0
															|| currDate.compareTo(givenDatePlus90Days) <= 0) {
														Log.consoleLog(ifr, "Inside AmtOdLimit limit block");

														amtOdLimit += Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														odValidProductCode.add(productCode);
														Log.consoleLog(ifr, "prdCode: " + productCode);
														Log.consoleLog(ifr, "AmtOdLimit==>" + amtOdLimit);
//	                                                        Log.consoleLog(ifr, "Entering into odRenewalprodCode code==>");
//	                                                        Log.consoleLog(ifr, "odRenewalprodCode==>" + odRenewalprodCode);
														odAccountId = accountId;
														odRenewalAmount = Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														limitNumber = Double
																.parseDouble(loanDetail.get("LimitNumber").toString());
														Log.consoleLog(ifr, "odAccountId=>" + odAccountId);
														SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
																"yyyy-MM-dd HH:mm:ss");
														SimpleDateFormat outputFormat1 = new SimpleDateFormat(
																"dd/MM/yyyy");
														String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
														Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
														Date date = simpleDateFormat1.parse(dateOdExpiry);
														odExpiryDate = outputFormat1.format(date);
														Log.consoleLog(ifr, "odExpiryDate=>" + odExpiryDate);
														dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
														if (currDate.compareTo(givenDateminus30Days) >= 0
																&& currDate.compareTo(givenDatePlus90Days) <= 0) {
															Log.consoleLog(ifr, "Inside renewal enabling block");
															flag = true;
														}
													}

												} catch (Exception e) {
													Log.consoleLog(ifr, "Exception in date formate===>" + e);
												}
//	                                                if (isOdExpiredLessThanMonth(loanDetail, ifr)
//	                                                        || isOdExpiredMoreThanThreeMonth(loanDetail, ifr)) {
//	                                                    Log.consoleLog(ifr, "Entering into odRenewalprodCode code==>");
//	                                                    Log.consoleLog(ifr, "odRenewalprodCode==>" + odRenewalprodCode);
//	                                                    odAccountId = accountId;
//	                                                    odRenewalAmount = Double
//	                                                            .parseDouble(loanDetail.get("AmtOdLimit").toString());
//	                                                    limitNumber = Double
//	                                                            .parseDouble(loanDetail.get("LimitNumber").toString());
//	                                                    Log.consoleLog(ifr, "odAccountId=>" + odAccountId);
//	                                                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
//	                                                            "yyyy-MM-dd HH:mm:ss");
//	                                                    SimpleDateFormat outputFormat1 = new SimpleDateFormat("dd/MM/yyyy");
//	                                                    String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
//	                                                    Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
//	                                                    Date date = simpleDateFormat1.parse(dateOdExpiry);
//	                                                    odExpiryDate = outputFormat1.format(date);
//	                                                    Log.consoleLog(ifr, "odExpiryDate=>" + odExpiryDate);
//	                                                    dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
												//
//	                                                }
											}
										}
									}
								}
								if (Optional.ofNullable(inputJSONObj.get("ProductName")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ProductName")).isEmpty()) {
									productNameOD = inputJSONObj.get("ProductName").toString();
									Log.consoleLog(ifr, "productNameOD=============>" + productNameOD);
								}
//									if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
//											&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()) {
//										amtInstalOD = Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
//										 Log.consoleLog(ifr, "amtInstalOD=============>" + amtInstalOD);
//									}

							}

							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& otherProductCode.contains(inputJSONObj.get("ProductCode").toString())
									&& !canaraGoldLoan.contains(productCode)
									&& productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstalOthers += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstalOthers=============>" + amtInstal);

								}
							}
							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !canaraGoldLoan.contains(productCode)
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& !excludedDPNcode.contains(inputJSONObj.get("ProductCode").toString())
									&& productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstal += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstal=============>" + amtInstal);

								}
							}
							// odModuleCodeInclude.contains(inputJSONObj.get("ProductCode").toString())
							if (Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("C")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& odProductCode.contains(inputJSONObj.get("ProductCode").toString())
									&& !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									// dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
									Log.consoleLog(ifr, "dateACCOpen====>" + dateAccOpen);

									// limitNumber=Double.parseDouble(loanDetail.get("LimitNumber").toString());
									amtODInt += odLoanDeduction(ifr, inputJSONObj, customerRelationShipIncluded,
											accountId, limitNumber, dateAccOpen);
								}

							} else if (inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& customerRelationShipIncluded
											.contains(inputJSONObj.get("CustomerRelationship").toString())
									&& accountId != null && !accountId.isEmpty() && productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								try {
									SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
									SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
									String dateOdMarurity = inputJSONObj.get("DatMaturity").toString();
									Log.consoleLog(ifr, "dateOdMarurity==>" + dateOdMarurity);
									Date date = simpleDateFormat1.parse(dateOdMarurity);
									String dateOFMat = outputFormat.format(date);
									if (canaraGoldLoan.contains(productCode)) {
										loanInstallmentAmt = totalBalEGLoan;
										Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
										Log.consoleLog(ifr, "loanInstallmentAmt===>" + loanInstallmentAmt);
									} else {
										loanInstallmentAmt = inputJSONObj.get("LoanInstallmentAmt").toString();
									}
									
								      if(value.equalsIgnoreCase("Barrower")) {
											String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT (winame, productcode, productname, limit, tenure, DateAccOpen, OUTSTANDING_BALANCE, EMI, LOAN_ACC_NUMBER, roi, isOd, CONSIDER_ELIGIBILITY,IsBorrower) "
													+ "SELECT '" + processInstanceId + "', '" + productCode + "', '"
													+ inputJSONObj.get("ProductName").toString() + "', '"
													+ inputJSONObj.get("OriginalBalance").toString() + "', '" + dateOFMat
													+ "', '" + inputJSONObj.get("AvailableBalanace").toString() + "', '"
													+ inputJSONObj.get("LcyAmount").toString() + "', '" + loanInstallmentAmt
													+ "', '" + accountId.trim() + "', '" + inputJSONObj.get("RatInt")
													+ "', 'N', 'YES','Barrower' "
													+ "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT WHERE LOAN_ACC_NUMBER = '"
													+ accountId.trim() + "' AND WINAME ='" + processInstanceId + "')";
											ifr.saveDataInDB(insertQuery);
											Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);
		                                    }
//		                                     
									else  if(value.equalsIgnoreCase("CoBorrower")) 
		                                    {
		                                    	String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT_COBORROWER (winame, productcode, productname, limit, tenure, DateAccOpen, OUTSTANDING_BALANCE, EMI, LOAN_ACC_NUMBER, roi, isOd, CONSIDER_ELIGIBILITY,IsCoBorrower) "
		    											+ "SELECT '" + processInstanceId + "', '" + productCode + "', '"
		    											+ inputJSONObj.get("ProductName").toString() + "', '"
		    											+ inputJSONObj.get("OriginalBalance").toString() + "', '" + dateOFMat
		    											+ "', '" + inputJSONObj.get("AvailableBalanace").toString() + "', '"
		    											+ inputJSONObj.get("LcyAmount").toString() + "', '" + loanInstallmentAmt
		    											+ "', '" + accountId.trim() + "', '" + inputJSONObj.get("RatInt")
		    											+ "', 'N', 'YES', 'CoBorrower' "
		    											+ "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT_COBORROWER WHERE LOAN_ACC_NUMBER = '"
		    											+ accountId.trim() + "' AND WINAME ='" + processInstanceId + "')";
		                                    	ifr.saveDataInDB(insertQuery);
		    									Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);
		                                    }
		                            else {
									String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT (winame, productcode, productname, limit, tenure, DateAccOpen, OUTSTANDING_BALANCE, EMI, LOAN_ACC_NUMBER, roi, isOd, CONSIDER_ELIGIBILITY) "
											+ "SELECT '" + processInstanceId + "', '" + productCode + "', '"
											+ inputJSONObj.get("ProductName").toString() + "', '"
											+ inputJSONObj.get("OriginalBalance").toString() + "', '" + dateOFMat
											+ "', '" + inputJSONObj.get("AvailableBalanace").toString() + "', '"
											+ inputJSONObj.get("LcyAmount").toString() + "', '" + loanInstallmentAmt
											+ "', '" + accountId.trim() + "', '" + inputJSONObj.get("RatInt")
											+ "', 'N', 'YES' "
											+ "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT WHERE LOAN_ACC_NUMBER = '"
											+ accountId.trim() + "' AND WINAME ='" + processInstanceId + "')";

									ifr.saveDataInDB(insertQuery);
									Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);
		                            }

									String queryUpdate = "UPDATE SLOS_ALL_ACTIVE_PRODUCT SET CONSIDER_ELIGIBILITY='YES' WHERE WINAME='"
											+ processInstanceId + "'";

									Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
									ifr.saveDataInDB(queryUpdate);

									String limit = "";
									String loanAccNum = "";
									Double notionalEMI = 0.0;
									String queryForOD = "SELECT LIMIT, LOAN_ACC_NUMBER from SLOS_ALL_ACTIVE_PRODUCT where winame='"
											+ processInstanceId + "' AND ISOD='Y' ";
									List<List<String>> resqueryForOD = ifr.getDataFromDB(queryForOD);
									Log.consoleLog(ifr, "resqueryForOD : " + resqueryForOD);
									if (!resqueryForOD.isEmpty()) {
										for (int j = 0; j < resqueryForOD.size(); j++) {
											limit = resqueryForOD.get(j).get(0);
											loanAccNum = resqueryForOD.get(j).get(1);
											if (!limit.isEmpty()) {
												notionalEMI = Double.parseDouble(limit)
														* AcceleratorConstants.ODRATE.doubleValue()
														/ AcceleratorConstants.TWELVE.intValue();
												String queryUpdateR = "UPDATE SLOS_ALL_ACTIVE_PRODUCT SET EMI='"
														+ String.format("%.2f", notionalEMI) + "' WHERE WINAME='"
														+ processInstanceId + "' AND LOAN_ACC_NUMBER='" + loanAccNum
														+ "' ";

												Log.consoleLog(ifr, "queryUpdateR : " + queryUpdateR);
												ifr.saveDataInDB(queryUpdateR);
											}
										}
									}

								} catch (Exception e) {
									Log.consoleLog(ifr, "Exception in SLOS_ALL_ACTIVE_PRODUCT==>" + e);
								}
							}

						}
					}

				} else {
					String[] ErrorData = checkError.split("#");
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR;
			} else {

				if (!isEligibilityCal) {
					ifr.setValue("Salary_Credit_Branch", salaryBranchDPCode);
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
				}
				JSONObject obj = new JSONObject();
				obj.put("ProductCode", productCode);
				obj.put("OriginalBalance", totalBalance);
				obj.put("AmtOdLimit", amtOdLimit);
				obj.put("ProductName", productName);
				double finalAmt = amtInstal + totalBal;
				String finalAmtInstal = String.format("%.2f", finalAmt);
				obj.put("AmtInstal", Double.parseDouble(finalAmtInstal));
				Log.consoleLog(ifr, "amtInstal==>" + amtInstal);
				Log.consoleLog(ifr, "totalBal==>" + totalBal);
				obj.put("AmtOdInt", amtODInt + amtInstalOthers + totalBal);
				Log.consoleLog(ifr, "amtODInt==>" + amtODInt);
				Log.consoleLog(ifr, "amtInstalOthers==>" + amtInstalOthers);
				obj.put("VLUsedAmount", vlProductCode);
				obj.put("AccountID", accountId);
				obj.put("SalaryBranchDPCode", salaryBranchDPCode);
				obj.put("isElgibleForNth",isElgibleForNth);
				// ifr.setValue("Existing_OD_account", odAccountId);
				for (String error : warningSet) {
					if (!error.equalsIgnoreCase("noerror"))
						warning = error;
				}
				obj.put("Warning", warning);
				Log.consoleLog(ifr, "adv360 obj==>" + obj.toString());

				if (salaryBranchDPCode.isEmpty()) {
					Log.consoleLog(ifr, "inside Salary credit branch not found");
					return RLOS_Constants.ERROR + ", Salary credit branch DP CODE NOT FOUNT";
				}

				// ifr.setValue("Existing_OD_Expiry_Date", odExpiryDate);
				// ifr.setValue("first_staffdetails_textbox75", String.valueOf(amtOdLimit));
				// ifr.setValue("renewal_amount", String.valueOf(odRenewalAmount));

				return obj.toJSONString();
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
			Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR;
	}
	
	
	public String executeCBSAdvanced360Inquiryv2Kavach(IFormReference ifr, String ProcessInstanceId, String CustomerID,
			String salaryacc, String designation, String probation, List<String> validProductCode,
			boolean isEligibilityCal, String value) {
		Log.consoleLog(ifr, "Entered into ExecuteCBSAdvanced360Inquiryv2...");
		Validator valid = new KnockOffValidator("");
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiName = "Advanced360Inquiryv2";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		double totalBalance = 0.0;
		double totalBal = 0.0;
		String totalBalEGLoan = "";
		double totalBalEG = 0.0;
		String productCode = "";
		String odAccountDetailsDTO = "";
		double amtOdLimit = 0.0;
		double amtODInt = 0;
		String productName = "";
		double amtInstal = 0.0;
		String productNameOD = "";
		String accountId = "";
		String warning = "NoError";
		// String branchCode = "";
		String Query1 = "";
		String odExpiryDate = "";
		String odAccountId = "";
		String odRenewalprodCode = "";
		double amtInstalOthers = 0.0;
		double odRenewalAmount = 0.0;
		double limitNumber = 0.0;
		String dateAccOpen = "";
		String loanInstallmentAmt = "";
		String salaryBranchDPCode = "";
		String currentStatusDescription  = "";
		boolean flag = false;
		String isElgibleForNth ="";
		Set<String> warningSet = new HashSet<>();
		JSONObject vlProductCode = new JSONObject();
		for (String pc : validProductCode) {
			vlProductCode.put(pc, 0);
		}
		JSONArray odValidProductCode = new JSONArray();
		if (probation.equalsIgnoreCase("No")) {
			probation = "N";
		}
		if (probation.equalsIgnoreCase("Yes")) {
			probation = "Y";
		}
		try {
			String queryForProductCode = "SELECT PRODUCTCODE FROM LOS_STAFF_SCHEME_LIMIT where designation='"
					+ designation + "' and PROBATION_TAG='" + probation + "'";

			Log.consoleLog(ifr, "queryForProductCode==>" + queryForProductCode);
			List<List<String>> queryForProductCodeResult = ifr.getDataFromDB(queryForProductCode);
			if (!queryForProductCodeResult.isEmpty()) {
				odRenewalprodCode = queryForProductCodeResult.get(0).get(0);
			}
			String validProduct = "SELECT productCode, productType from staff_valid_products";
			Log.consoleLog(ifr, "validProduct qquery==>" + validProduct);
			List<List<String>> validProductResult = ifr.getDataFromDB(validProduct);
			Log.consoleLog(ifr, "validProductResult qquery==>" + validProductResult);

			List<String> canaraGoldLoan = validProductResult.stream().filter(a1 -> a1.get(1).equals("CGL"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> odProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("OD"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			List<String> otherProductCode = validProductResult.stream()
					.filter(a1 -> a1.get(1).equals("GHL") || a1.get(1).equals("VL") || a1.get(1).equals("CM")
							|| a1.get(1).equals("CM") || a1.get(1).equals("CS") || a1.get(1).equals("OTHERS"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			
			List<String> kavachProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("Kavach"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			List<String> StaffGoldProductCode = validProductResult.stream().filter(a1 -> a1.get(1).equals("Staff Gold"))
					.flatMap(a2 -> a2.stream()).collect(Collectors.toList());
			
			List<String> allProductCodes = validProductResult.stream()
				    .map(a1 -> a1.get(0))
				    .collect(Collectors.toList());
			
			List<String> productCodesExceptOD = allProductCodes.stream()
				    .filter(code -> !odProductCode.contains(code))
				    .collect(Collectors.toList());
			
			// Log.consoleLog(ifr, "dpnProductCode qquery==>" + dpnProductCode);
			Log.consoleLog(ifr, "odProductCode qquery==>" + odProductCode);
			String bankCode = pcm.getConstantValue(ifr, "CBS360V2STAFF", "BANKCODE");
			String channel = pcm.getConstantValue(ifr, "CBS360V2STAFF", "CHANNEL");
			String userId = pcm.getConstantValue(ifr, "CBS360V2STAFF", "USERID");
			String tBranch = pcm.getConstantValue(ifr, "CBS360V2STAFF", "TransactionBranch");

			String queryForLoanAccNum = "select LOAN_ACC_NUMBER from SLOS_ALL_ACTIVE_PRODUCT where winame='"
					+ processInstanceId + "'";
			List<List<String>> resForLoanAccNum = ifr.getDataFromDB(queryForLoanAccNum);
			Log.consoleLog(ifr, "queryForLoanAccNum : " + queryForLoanAccNum);
			Log.consoleLog(ifr, "resForLoanAccNum==>" + resForLoanAccNum);
			if(!value.equalsIgnoreCase("CoBorrower")) {
			String deleteQuery = "delete from SLOS_ALL_ACTIVE_PRODUCT where winame='" + processInstanceId + "'";
			ifr.saveDataInDB(deleteQuery);
			}
			String all = "";
			String accountModule = "";
			if (value.contains("kavach")) {
				all = "ALL";
				accountModule = "ALL";
			} else {
				all = "NONCLOSED";
				accountModule = "ALL";
			}

			request = "{\n"
					+ "            \"Operation\": \"advancedCustomer360viewEnquiry(SessionContext,long,String,String)\",\n"
					+ "            \"Service\": \"LOSManagerService\",\n" + "            \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "            \"PrimaryPassword\": \"\",\n"
					+ "            \"UserId\": \"" + userId + "\"\n" + "            },\n"
					+ "            \"BankCode\": \"" + bankCode + "\",\n" + "            \"Channel\": \"" + channel
					+ "\",\n" + "            \"ExternalBatchNumber\": \"\",\n"
					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + tBranch + "\",\n" + "            \"UserId\": \""
					+ userId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"customerId\": \""
					+ CustomerID + "\",\n" + "        \"accountStatus\": \"" +all+ "\",\n"
					+ "        \"accountModule\": \""+accountModule+"\"\n" + "}";

			Log.consoleLog(ifr, "Request====>" + request);
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);
			List<String> excludedDPNcode = new ArrayList<>();
			excludedDPNcode.add("743");
			excludedDPNcode.add("793");
			List<String> customerRelationShipIncluded = new ArrayList<>();
			customerRelationShipIncluded.add("SOW");
			customerRelationShipIncluded.add("JOF");
			customerRelationShipIncluded.add("JAF");
			List<String> odModuleCodeInclude = new ArrayList<>();
			odModuleCodeInclude.add("254");
			odModuleCodeInclude.add("253");
			odModuleCodeInclude.add("1129");
			odModuleCodeInclude.add("1127");
			if (!response.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);
				String body = resultObj.get("body").toString();
				Log.consoleLog(ifr, "body==>" + body);

				JSONObject bodyObj = (JSONObject) parser.parse(body);
				String checkError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
				Log.consoleLog(ifr, "CheckError===>" + checkError);

				if (checkError.equalsIgnoreCase("true")) {

					String XfaceCustomerAccountDetailsDTO = bodyObj.get("XfaceCustomerAccountDetailsDTO").toString();
					Log.consoleLog(ifr, "XfaceCustomerAccountDetailsDTO==>" + XfaceCustomerAccountDetailsDTO);
					JSONObject XfaceCustomerAccountDetailsDTOObj = (JSONObject) parser
							.parse(XfaceCustomerAccountDetailsDTO);
					String AccountDetails = XfaceCustomerAccountDetailsDTOObj.get("AccountDetails").toString();
					Log.consoleLog(ifr, "AccountDetails=>" + AccountDetails);
					JSONArray AccountDetailsObj = (JSONArray) parser.parse(AccountDetails);

					if (!AccountDetailsObj.isEmpty()) {
						for (int i = 0; i < AccountDetailsObj.size(); i++) {

							Log.consoleLog(ifr, "AccountDetailsObj==>" + AccountDetailsObj.get(i) + "salaryacc==>"
									+ salaryacc + "salaryacc");
							String inputJSON = AccountDetailsObj.get(i).toString();
							JSONObject inputJSONObj = (JSONObject) parser.parse(inputJSON);
							currentStatusDescription = inputJSONObj.get("CurrentStatusDescription").toString();
							if (Optional.ofNullable(inputJSONObj.get("AccountId")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AccountId")).isEmpty()
									&& !isEligibilityCal && currentStatusDescription.contains("OPEN")) {
								accountId = inputJSONObj.get("AccountId").toString();
								Log.consoleLog(ifr, "AccountId=============>" + accountId);
								if (accountId.trim().equalsIgnoreCase(salaryacc.trim())) {
									Log.consoleLog(ifr, "BranchCode==>" + inputJSONObj.get("BranchCode"));
									if (Optional.ofNullable(inputJSONObj.get("BranchCode")).isPresent()
											&& !Optional.ofNullable(inputJSONObj.get("BranchCode")).isEmpty()) {
										salaryBranchDPCode = inputJSONObj.get("BranchCode").toString();
										Log.consoleLog(ifr, "BranchCode==============>" + salaryBranchDPCode);

										String updateQuery = "UPDATE SLOS_TRN_LOANDETAILS SET DISB_BRANCH = LPAD('"+salaryBranchDPCode+"', 5, '0') WHERE PID = '" + processInstanceId + "'";
									    Log.consoleLog(ifr, "updateQuery==>" + updateQuery);
									    ifr.saveDataInDB(updateQuery);
					                    Log.consoleLog(ifr, "BranchCode==============>" + salaryBranchDPCode);
						                ifr.saveDataInDB(Query1);

									}
								}
							}
							productCode = valid.getValue(ifr, inputJSONObj, "ProductCode", "");
							Log.consoleLog(ifr, "ProductCode=============>" + productCode);
							if (kavachProductCode.contains(productCode) && value.contains("kavach")) {
								
								isElgibleForNth= "yes";
								String Querytenure = "UPDATE SLOS_STAFF_TRN SET IS_ELG_FOR_NTH= '" + isElgibleForNth + "' WHERE WINAME= '"
										+ processInstanceId + "'";

								ifr.saveDataInDB(Querytenure);
								Log.consoleLog(ifr, "Querytenure : " + Querytenure);
							}
							if(currentStatusDescription.contains("OPEN")) {
							
								if (kavachProductCode.contains(productCode)) {
									if (value.contains("kavach")) {
										double val = Double.parseDouble(
												valid.getValue(ifr, inputJSONObj, "OriginalBalance", "0.0"));
										Log.consoleLog(ifr, "productCode val ==>" + val);
//										vlProductCode.put(productCode,
//												Double.parseDouble(vlProductCode.get(productCode).toString()) + val);
										double existingValue = 0.0;

										if (vlProductCode.get(productCode) != null) {
											existingValue = Double
													.parseDouble(vlProductCode.get(productCode).toString());
										}

										vlProductCode.put(productCode, existingValue + val);

										Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
										Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
										String vlProductCodeString = vlProductCode.toString();
										Log.consoleLog(ifr, "vlProductCodeString ==>" + vlProductCodeString);
										productName = valid.getValue(ifr, inputJSONObj, "ProductName", "");
										Log.consoleLog(ifr, "productName ==>" + productName);
										String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set PRODUCTCODEUSEDBORROWER='"
												+ vlProductCodeString + "' " + "where WINAME='" + processInstanceId
												+ "'";

										Log.consoleLog(ifr, "PRODUCTCODEUSEDBORROWER====>" + ageQuery);
										ifr.saveDataInDB(ageQuery);
									}
								}
								
								if (StaffGoldProductCode.contains(productCode)) {
									if (value.contains("StaffGold")) {
										double val = Double.parseDouble(
												valid.getValue(ifr, inputJSONObj, "OriginalBalance", "0.0"));
										Log.consoleLog(ifr, "productCode val ==>" + val);
//										vlProductCode.put(productCode,
//												Double.parseDouble(vlProductCode.get(productCode).toString()) + val);
										double existingValue = 0.0;

										if (vlProductCode.get(productCode) != null) {
											existingValue = Double
													.parseDouble(vlProductCode.get(productCode).toString());
										}

										vlProductCode.put(productCode, existingValue + val);

										Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
										Log.consoleLog(ifr, "vlProductCode ==>" + vlProductCode);
										String vlProductCodeString = vlProductCode.toString();
										Log.consoleLog(ifr, "vlProductCodeString ==>" + vlProductCodeString);
										productName = valid.getValue(ifr, inputJSONObj, "ProductName", "");
										Log.consoleLog(ifr, "productName ==>" + productName);
										String ageQuery = "UPDATE SLOS_TRN_LOANSUMMARY set PRODUCTCODEUSEDBORROWER='"
												+ vlProductCodeString + "' " + "where WINAME='" + processInstanceId
												+ "'";

										Log.consoleLog(ifr, "PRODUCTCODEUSEDBORROWER====>" + ageQuery);
										ifr.saveDataInDB(ageQuery);
									}
								}
								
								if (Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty() && Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()&& Optional.ofNullable(inputJSONObj.get("CurrentStatus")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CurrentStatus")).isEmpty()) {
									 String products = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCPRODCODE");
									 String relations = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCRELATION");
									 String accStatus = pcm.getParamValue(ifr, "STAFFCASACHECK", "SBACCSTATUS");
									 
									String product = inputJSONObj.get("ProductCode").toString();
									String relation = inputJSONObj.get("CustomerRelationship").toString();
									String accStat = inputJSONObj.get("CurrentStatus").toString();
									
									Set<String> setS = new HashSet<>(Arrays.asList(products.split(",")));
									Set<String> setY = new HashSet<>(Arrays.asList(relations.split(",")));
									Set<String> setZ = new HashSet<>(Arrays.asList(accStatus.split(",")));
									if (setS.contains(product) && setY.contains(relation) && setZ.contains(accStat)) {
										String accId = inputJSONObj.get("AccountId").toString();
										ifr.addItemInCombo("chargesToDebitedFor", accId, accId);
									}
								}
							warningSet.add(valid.validate(bodyObj, "TotalOverdue", 0).getMessage());
							warningSet.add(valid.validate(bodyObj, "Sma2Count12Months", 0).getMessage());
							warningSet.add(valid.validate(bodyObj, "Sma1Count12months", 0).getMessage());
							if (canaraGoldLoan.contains(productCode) && !isEligibilityCal) {
								String originalBalanceStr = "";
								double notionalIntrest = 0.0;
								double ratInt = 0.0;
								if (Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("OriginalBalance")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatMaturity")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatMaturity")).isEmpty()
										&& Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("DatAcctOpen")).isEmpty()) {
									ratInt = Double.parseDouble(inputJSONObj.get("RatInt").toString());
									originalBalanceStr = inputJSONObj.get("OriginalBalance").toString();
									notionalIntrest = (Double.parseDouble(originalBalanceStr) * ratInt) / 100;
									Log.consoleLog(ifr, "notionalIntrest===>" + notionalIntrest);
									String dataccOpen = inputJSONObj.get("DatAcctOpen").toString();
									String datMaturity = inputJSONObj.get("DatMaturity").toString();
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
									LocalDate date1 = LocalDate.parse(datMaturity, formatter);
									LocalDate date2 = LocalDate.parse(dataccOpen, formatter);
									double monthsBetween = ChronoUnit.MONTHS.between(date2, date1);
									Log.consoleLog(ifr, "monthsBetween===>" + monthsBetween);
									totalBalEG = (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									totalBalEGLoan = String.format("%.2f", totalBalEG);
									Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
									totalBal += (Double.parseDouble(originalBalanceStr) + notionalIntrest)
											/ monthsBetween;
									Log.consoleLog(ifr, "totalBal===>" + totalBal);
								}
							}

							if (odProductCode.contains(productCode.trim()) && !isEligibilityCal) // productCode.equalsIgnoreCase("1129")
							{
								Log.consoleLog(ifr, "enter into OD =============>");
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									odAccountDetailsDTO = inputJSONObj.get("ODAccountDetailsDTO").toString();
									Log.consoleLog(ifr, "odAccountDetailsDTO=============>" + odAccountDetailsDTO);
									JSONArray result = (JSONArray) parser.parse(odAccountDetailsDTO);
									Log.consoleLog(ifr, "result=============>" + result);

									if (result.size() > 0) {
										for (int k = 0; k < result.size(); k++) {
											JSONObject loanDetail = (JSONObject) result.get(k);

											if (Optional.ofNullable(loanDetail.get("ODExpiryDate")).isPresent()
													&& !Optional.ofNullable(loanDetail.get("ODExpiryDate")).isEmpty()) {
												SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss");
												SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
												String date1 = null;
												try {
													String plusdays = "";
													String minusdays = "";
													String Query = "SELECT DISTINCT PLUSDAYS,MINUSDAYS from LOS_ACCOUNT_CODE_MIX_CONSTANT_HRMS WHERE NAM_PRODUCT LIKE '%"
															+ productCode
															+ "%' AND ( NAM_PRODUCT LIKE '%253%' OR NAM_PRODUCT LIKE '%254%'OR NAM_PRODUCT LIKE '%1129%')";
													Log.consoleLog(ifr, "plusdays and minusdays query===>" + Query);
													List<List<String>> res = ifr.getDataFromDB(Query);
													Log.consoleLog(ifr, "res===>" + res);
													if (!res.isEmpty()) {
														plusdays = res.get(0).get(0);
														Log.consoleLog(ifr, "plusdays:" + plusdays);
														minusdays = res.get(0).get(1);
														Log.consoleLog(ifr, "minusdays:" + minusdays);
													}
													date1 = loanDetail.get("ODExpiryDate").toString();
													Log.consoleLog(ifr, "date1===>" + date1);
													DateTimeFormatter dtf = DateTimeFormatter
															.ofPattern("yyyy-MM-dd HH:mm:ss");
													LocalDateTime givenDate = LocalDateTime.parse(date1, dtf);
													LocalDateTime currDate = LocalDateTime.now();
													LocalDateTime givenDatePlus90Days = givenDate
															.plusDays(Integer.parseInt(plusdays));
													LocalDateTime givenDateminus30Days = givenDate
															.minusDays(Integer.parseInt(minusdays));
													Log.consoleLog(ifr, "currDate==>" + currDate);
													Log.consoleLog(ifr, "givenDate==>" + givenDate);
													Log.consoleLog(ifr,
															"givenDateminus30Days==>" + givenDateminus30Days);
													Log.consoleLog(ifr, "givenDatePlus90Days==>" + givenDatePlus90Days);
													if (Optional.ofNullable(loanDetail.get("AmtOdLimit")).isPresent()
															&& !Optional.ofNullable(loanDetail.get("AmtOdLimit"))
																	.isEmpty()
															&& givenDate.compareTo(currDate) >= 0
															|| currDate.compareTo(givenDatePlus90Days) <= 0) {
														Log.consoleLog(ifr, "Inside AmtOdLimit limit block");

														amtOdLimit += Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														odValidProductCode.add(productCode);
														Log.consoleLog(ifr, "prdCode: " + productCode);
														Log.consoleLog(ifr, "AmtOdLimit==>" + amtOdLimit);
														odAccountId = accountId;
														odRenewalAmount = Double
																.parseDouble(loanDetail.get("AmtOdLimit").toString());
														limitNumber = Double
																.parseDouble(loanDetail.get("LimitNumber").toString());
														Log.consoleLog(ifr, "odAccountId=>" + odAccountId);
														SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(
																"yyyy-MM-dd HH:mm:ss");
														SimpleDateFormat outputFormat1 = new SimpleDateFormat(
																"dd/MM/yyyy");
														String dateOdExpiry = loanDetail.get("ODExpiryDate").toString();
														Log.consoleLog(ifr, "dateOdExpiry==>" + dateOdExpiry);
														Date date = simpleDateFormat1.parse(dateOdExpiry);
														odExpiryDate = outputFormat1.format(date);
														Log.consoleLog(ifr, "odExpiryDate=>" + odExpiryDate);
														dateAccOpen = inputJSONObj.get("DatAcctOpen").toString();
														if (currDate.compareTo(givenDateminus30Days) >= 0
																&& currDate.compareTo(givenDatePlus90Days) <= 0) {
															Log.consoleLog(ifr, "Inside renewal enabling block");
															flag = true;
														}
													}

												} catch (Exception e) {
													Log.consoleLog(ifr, "Exception in date formate===>" + e);
												}
											}
										}
									}
								}
								if (Optional.ofNullable(inputJSONObj.get("ProductName")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ProductName")).isEmpty()) {
									productNameOD = inputJSONObj.get("ProductName").toString();
									Log.consoleLog(ifr, "productNameOD=============>" + productNameOD);
								}


							}

							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& otherProductCode.contains(inputJSONObj.get("ProductCode").toString())
									&& !canaraGoldLoan.contains(productCode)
									&& productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstalOthers += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstalOthers=============>" + amtInstal);

								}
							}
							if (Optional.ofNullable(inputJSONObj.get("AmtInstal")).isPresent()
									&& !canaraGoldLoan.contains(productCode)
									&& !Optional.ofNullable(inputJSONObj.get("AmtInstal")).isEmpty()
									&& Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& !excludedDPNcode.contains(inputJSONObj.get("ProductCode").toString())
									&& productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("CustomerRelationship")).isEmpty()
										&& customerRelationShipIncluded
												.contains(inputJSONObj.get("CustomerRelationship").toString())) {
									amtInstal += Double.parseDouble(inputJSONObj.get("AmtInstal").toString());
									Log.consoleLog(ifr, "amtInstal=============>" + amtInstal);

								}
							}

							if (Optional.ofNullable(inputJSONObj.get("ModuleCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ModuleCode")).isEmpty()
									&& inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("C")
									&& Optional.ofNullable(inputJSONObj.get("ProductCode")).isPresent()
									&& !Optional.ofNullable(inputJSONObj.get("ProductCode")).isEmpty()
									&& odProductCode.contains(inputJSONObj.get("ProductCode").toString())
									&& !isEligibilityCal) {
								if (Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isPresent()
										&& !Optional.ofNullable(inputJSONObj.get("ODAccountDetailsDTO")).isEmpty()) {
									Log.consoleLog(ifr, "dateACCOpen====>" + dateAccOpen);

									amtODInt += odLoanDeduction(ifr, inputJSONObj, customerRelationShipIncluded,
											accountId, limitNumber, dateAccOpen);
								}

							} else if (inputJSONObj.get("ModuleCode").toString().equalsIgnoreCase("L")
									&& customerRelationShipIncluded
											.contains(inputJSONObj.get("CustomerRelationship").toString())
									&& accountId != null && !accountId.isEmpty() && productCodesExceptOD.contains(productCode.trim()) && !isEligibilityCal) {
								try {
									SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
									SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
									String dateOdMarurity = inputJSONObj.get("DatMaturity").toString();
									Log.consoleLog(ifr, "dateOdMarurity==>" + dateOdMarurity);
									Date date = simpleDateFormat1.parse(dateOdMarurity);
									String dateOFMat = outputFormat.format(date);
									if (canaraGoldLoan.contains(productCode)) {
										loanInstallmentAmt = totalBalEGLoan;
										Log.consoleLog(ifr, "totalBalEGLoan===>" + totalBalEGLoan);
										Log.consoleLog(ifr, "loanInstallmentAmt===>" + loanInstallmentAmt);
									} else {
										loanInstallmentAmt = inputJSONObj.get("LoanInstallmentAmt").toString();
									}
									
							
           	                         String insertQuery = "INSERT INTO SLOS_ALL_ACTIVE_PRODUCT (winame, productcode, productname, limit, tenure, DateAccOpen, OUTSTANDING_BALANCE, EMI, LOAN_ACC_NUMBER, roi, isOd, CONSIDER_ELIGIBILITY) "
											+ "SELECT '" + processInstanceId + "', '" + productCode + "', '"
											+ inputJSONObj.get("ProductName").toString() + "', '"
											+ inputJSONObj.get("OriginalBalance").toString() + "', '" + dateOFMat
											+ "', '" + inputJSONObj.get("AvailableBalanace").toString() + "', '"
											+ inputJSONObj.get("LcyAmount").toString() + "', '" + loanInstallmentAmt
											+ "', '" + accountId.trim() + "', '" + inputJSONObj.get("RatInt")
											+ "', 'N', 'YES' "
											+ "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM SLOS_ALL_ACTIVE_PRODUCT WHERE LOAN_ACC_NUMBER = '"
											+ accountId.trim() + "' AND WINAME ='" + processInstanceId + "')";

									ifr.saveDataInDB(insertQuery);
									Log.consoleLog(ifr, "All_ACTIVE_PRODUCT==>" + insertQuery);
		                    

									String queryUpdate = "UPDATE SLOS_ALL_ACTIVE_PRODUCT SET CONSIDER_ELIGIBILITY='YES' WHERE WINAME='"
											+ processInstanceId + "'";

									Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
									ifr.saveDataInDB(queryUpdate);

									String limit = "";
									String loanAccNum = "";
									Double notionalEMI = 0.0;
									String queryForOD = "SELECT LIMIT, LOAN_ACC_NUMBER from SLOS_ALL_ACTIVE_PRODUCT where winame='"
											+ processInstanceId + "' AND ISOD='Y' ";
									List<List<String>> resqueryForOD = ifr.getDataFromDB(queryForOD);
									Log.consoleLog(ifr, "resqueryForOD : " + resqueryForOD);
									if (!resqueryForOD.isEmpty()) {
										for (int j = 0; j < resqueryForOD.size(); j++) {
											limit = resqueryForOD.get(j).get(0);
											loanAccNum = resqueryForOD.get(j).get(1);
											if (!limit.isEmpty()) {
												notionalEMI = Double.parseDouble(limit)
														* AcceleratorConstants.ODRATE.doubleValue()
														/ AcceleratorConstants.TWELVE.intValue();
												String queryUpdateR = "UPDATE SLOS_ALL_ACTIVE_PRODUCT SET EMI='"
														+ String.format("%.2f", notionalEMI) + "' WHERE WINAME='"
														+ processInstanceId + "' AND LOAN_ACC_NUMBER='" + loanAccNum
														+ "' ";

												Log.consoleLog(ifr, "queryUpdateR : " + queryUpdateR);
												ifr.saveDataInDB(queryUpdateR);
											}
										}
									}

								} catch (Exception e) {
									Log.consoleLog(ifr, "Exception in SLOS_ALL_ACTIVE_PRODUCT==>" + e);
								}
							}

						}
					}

				} else {
					String[] ErrorData = checkError.split("#");
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

			if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR;
			} else {

				if (!isEligibilityCal) {
					ifr.setValue("Salary_Credit_Branch", salaryBranchDPCode);
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
						ifr.addItemInCombo("covered_loans", accnumber, accnumber);
						array.add(obj);
					}
					Log.consoleLog(ifr, "Json Array " + array);

					ifr.addDataToGrid("Existing_Loan_Details", array);
				}
				JSONObject obj = new JSONObject();
				obj.put("ProductCode", productCode);
				obj.put("OriginalBalance", totalBalance);
				obj.put("AmtOdLimit", amtOdLimit);
				obj.put("ProductName", productName);
				double finalAmt = amtInstal + totalBal;
				String finalAmtInstal = String.format("%.2f", finalAmt);
				obj.put("AmtInstal", Double.parseDouble(finalAmtInstal));
				Log.consoleLog(ifr, "amtInstal==>" + amtInstal);
				Log.consoleLog(ifr, "totalBal==>" + totalBal);
				obj.put("AmtOdInt", amtODInt + amtInstalOthers + totalBal);
				Log.consoleLog(ifr, "amtODInt==>" + amtODInt);
				Log.consoleLog(ifr, "amtInstalOthers==>" + amtInstalOthers);
				obj.put("VLUsedAmount", vlProductCode);
				obj.put("AccountID", accountId);
				obj.put("SalaryBranchDPCode", salaryBranchDPCode);
				//obj.put("isElgibleForNth",isElgibleForNth);
				for (String error : warningSet) {
					if (!error.equalsIgnoreCase("noerror"))
						warning = error;
				}
				obj.put("Warning", warning);
				Log.consoleLog(ifr, "adv360 obj==>" + obj.toString());

				if (salaryBranchDPCode.isEmpty()) {
					Log.consoleLog(ifr, "inside Salary credit branch not found");
					return RLOS_Constants.ERROR + ", Salary credit branch DP CODE NOT FOUNT";
				}

				return obj.toJSONString();
			}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
			Log.errorLog(ifr, "Exception/CBS_Advanced360Inquiryv2===>" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		
		return RLOS_Constants.ERROR;
	}

}
