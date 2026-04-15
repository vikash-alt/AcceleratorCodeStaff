/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import static com.newgen.dlp.integration.cbs.Demographic.calculateAge;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.staffHL.StaffHLPortalCustomCode;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rajaganapathy.t
 */
public class CustomerAccountSummary {

	APICommonMethods cm = new APICommonMethods();
	PortalCommonMethods pcm = new PortalCommonMethods();
	CCMCommonMethods apic = new CCMCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();

	public String updateCustomerAccountSummary(IFormReference ifr, HashMap<String, String> map) {

		String MobNum = "";
		String title = "";
		Log.consoleLog(ifr, "Entered into ExecuteCBS_CustomerAccountSummaryCB...");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		MobNum = map.get("mobileNumber");
		String CustomerId = "";
		if (map.containsKey("CustomerId")) {
			CustomerId = map.get("CustomerId");
		}
		// Added by Vandana on 12/02/2024 to handel mobile with country code.
		if (!MobNum.equalsIgnoreCase("") && MobNum.trim().length() == 10) {
			MobNum = "91" + MobNum;
		} else {
			MobNum = MobNum;
		}
		Date currentDate = new Date();
		String PANNumber = "";
		if (map.containsKey("PANNUMBER")) {
			PANNumber = map.get("PANNUMBER");
		}
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//        String formattedDate = dateFormat.format(currentDate);
		try {
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");

			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobNum + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"" + PANNumber + "\",\n"
					+ "        \"AadhaarNo\" : \"\",\n" + "        \"PassportNo\": \"\"\n" + "    }\n" + "}";
			String strApplicantType = map.get("ApplicantType");
			Log.consoleLog(ifr, "Applicant type::" + strApplicantType);
			String checkCountData = "Select  pid,Applicanttype from los_nl_basic_info  WHERE PID= '" + ProcessInstanceId
					+ "' and Applicanttype='" + strApplicantType + "'";
			Log.consoleLog(ifr, " checkCountData : " + checkCountData);
			List<List<String>> listCheck = ifr.getDataFromDB(checkCountData);
			Log.consoleLog(ifr, "listCheck : " + listCheck);
			if (!listCheck.isEmpty()) {
				for (int i = 0; i < listCheck.size(); i++) {
					if (listCheck.get(i).get(1).contains("CB")) {
						try {
							String query = "delete LOS_L_BASIC_INFO_I where f_key in (select f_key from LOS_NL_BASIC_INFO where PID='"
									+ ProcessInstanceId + "' and applicanttype='CB')";
							cf.mExecuteQuery(ifr, query, "Delete query");
							String query1 = " delete LOS_NL_Address where f_key in (select f_key from LOS_NL_BASIC_INFO where PID='"
									+ ProcessInstanceId + "' and applicanttype='CB')";
							cf.mExecuteQuery(ifr, query1, "Delete query1");
							String query2 = " delete LOS_NL_BASIC_INFO where PID='" + ProcessInstanceId
									+ "' and applicanttype='CB'";
							cf.mExecuteQuery(ifr, query2, "Delete query2");
							// ifr.saveDataInDB(query);
							// ifr.saveDataInDB(query1);
							// ifr.saveDataInDB(query2);
							Log.consoleLog(ifr, "second loop");
							updateCustomerDetails(ifr, Request, MobNum, map);
						} catch (Exception e) {
							Log.consoleLog(ifr, "Exception in updateCustomerAccountSummary ::" + e);
							Log.errorLog(ifr, "Exception in updateCustomerAccountSummary::" + e);
						}

					}
				}

			} else {
				Log.consoleLog(ifr, "first loop ");
				updateCustomerDetails(ifr, Request, MobNum, map);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		return "";
	}

	public String getCustomerAccountParams_CB(IFormReference ifr, String MobileNo) {

		Log.consoleLog(ifr, "#getCustomerAccountParams_CB");

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		try {
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");

			// Added by Vandana on 12/02/2024 to handel mobile with country code.
			if (!MobileNo.equalsIgnoreCase("")) {
				MobileNo = "91" + MobileNo;
			}

			String CustomerId = pcm.getCustomerIDCB(ifr, "B");

			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobileNo + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"\",\n" + "        \"AadhaarNo\" : \"\",\n"
					+ "        \"PassportNo\": \"\"\n" + "    }\n" + "}";

			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);
			// JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			try {
				if (CheckError.equalsIgnoreCase("true")) {
					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
					JSONObject CustomerAccountSummaryResponseJSON = (JSONObject) parser
							.parse(CustomerAccountSummaryResponse);
					JSONObject CustomerAccountSummaryResponseJSONObj = new JSONObject(
							CustomerAccountSummaryResponseJSON);
					String CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
					String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
					String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
					String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
					String Staff = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
					String DateofBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();

					Log.consoleLog(ifr, "aadharNo====>" + aadharNo);
					Log.consoleLog(ifr, "panNumber===>" + panNumber);
					Log.consoleLog(ifr, "NRI===>" + NRI);
					Log.consoleLog(ifr, "Staff===>" + Staff);
					Log.consoleLog(ifr, "DateofBirth===>" + DateofBirth);

					DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					Date date = originalFormat.parse(DateofBirth);
					String DOB = targetFormat.format(date);
					Log.consoleLog(ifr, "DOB=======>" + DOB);

					String CASADetailsDTO = CustomerAccountSummaryResponseJSONObj.get("CASADetailsDTO").toString();
					Log.consoleLog(ifr, "CASADetailsDTO==>" + CASADetailsDTO);

					JSONArray CASADetailsDTOJSONJSON = (JSONArray) parser.parse(CASADetailsDTO);
					Log.consoleLog(ifr, "CASADetailsDTOJSONJSON.size()==>" + CASADetailsDTOJSONJSON.size());
					String productCode = "";
					for (int i = 0; i < CASADetailsDTOJSONJSON.size(); i++) {
						if (!productCode.equalsIgnoreCase("")) {
							productCode = productCode + ",";
						}
						Log.consoleLog(ifr, "CASADetailsDTOResponseObj==>" + CASADetailsDTOJSONJSON.get(i));
						String InputString = CASADetailsDTOJSONJSON.get(i).toString();
						JSONObject InputStringResponseJSONJSONObj = (JSONObject) parser.parse(InputString);
						// JSONObject InputStringResponseJSONJSONObj = new
						// JSONObject(InputStringResponseJSON);
						JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
						productCode = productCode + AtrresultObj.get("productCode").toString();
						Log.consoleLog(ifr, "productCode===>" + productCode);
					}

					JSONObject jsonValues = new JSONObject();

					jsonValues.put("AadharNo", aadharNo);
					jsonValues.put("PanNumber", panNumber);
					jsonValues.put("NRI", NRI);
					jsonValues.put("Staff", Staff);
					jsonValues.put("productCode", productCode);
					jsonValues.put("DateofBirth", DOB);

					Log.consoleLog(ifr, "Entered into Return Balue..." + jsonValues.toString());
					return jsonValues.toString();
				} else {
					return RLOS_Constants.ERROR;
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e.getMessage());
				return RLOS_Constants.ERROR;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
		}
		return RLOS_Constants.ERROR;
	}

	public String getCustomerAccountSummary(IFormReference ifr, HashMap<String, String> customerdetails) {

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		String title = "";
		Log.consoleLog(ifr, "Entered into CBSCustomerAccountSummaryAPI...");
		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		String MobNum = customerdetails.get("MobileNumber");
		// Added by Vandana on 12/02/2024 to handel mobile with country code.
		if (!MobNum.equalsIgnoreCase("")) {
			MobNum = "91" + MobNum;
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-MobileNumber:" + MobNum);
		String CustomerId = "";
		String Accountnumber = "";
		String PANNumber = "";
		String strAdhar = "";
		if (customerdetails.containsKey("CustomerId")) {
			CustomerId = customerdetails.get("CustomerId");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-CustomerId:" + CustomerId);
		if (customerdetails.containsKey("Accountnumber")) {
			Accountnumber = customerdetails.get("Accountnumber");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-Accountnumber:" + Accountnumber);
		if (customerdetails.containsKey("PANNumber")) {
			PANNumber = customerdetails.get("PANNumber");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-PANNumber:" + PANNumber);
		if (customerdetails.containsKey("AdharNumber")) {
			strAdhar = customerdetails.get("AdharNumber");
		}
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//        String formattedDate = dateFormat.format(currentDate);

		String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
		String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
		String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
		String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
		String APIStatusSend = "";
		try {
			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobNum + "\",\n" + "        \"AccountNumber\":  \"" + Accountnumber
					+ "\",\n" + "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"" + PANNumber + "\",\n"
					+ "        \"AadhaarNo\" : \"" + strAdhar + "\",\n" + "        \"PassportNo\": \"\"\n" + "    }\n"
					+ "}";

//         
			// HashMap<String, String> requestHeader = new HashMap<>();
			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			String CustomerID = "";
			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);
			// JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);

			String ErrorCode = "";
			String ErrorMessage = "";
			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
				JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
						.parse(CustomerAccountSummaryResponse);
				// JSONObject CustomerAccountSummaryResponseJSONObj = new
				// JSONObject(CustomerAccountSummaryResponseJSON);
				CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
				String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber").toString();
				Log.consoleLog(ifr, "accountIdString : " + accountIdString);
				Log.consoleLog(ifr, "CustomerID==>" + CustomerID);
				String currentStatus = "";
				String branchDtls = "";
				String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				Log.consoleLog(ifr, "emailID==>" + emailID);
				String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
				Log.consoleLog(ifr, "homeBranch==>" + homeBranch);
				String branchCode = "";
				try {
					if (homeBranch.contains("-")) {
						String[] BRSet = homeBranch.split("-");
						branchCode = BRSet[0];
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
				}
				String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
				Log.consoleLog(ifr, "permAddress1==>" + permAddress1);
				String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
				Log.consoleLog(ifr, "permAddress2==>" + permAddress2);
				String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
				Log.consoleLog(ifr, "permAddress3==>" + permAddress3);
				String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
				Log.consoleLog(ifr, "dateOfBirth==>" + dateOfBirth);
				String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
				Log.consoleLog(ifr, "servicingBranch==>" + servicingBranch);
				String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
				Log.consoleLog(ifr, "mailCity==>" + mailCity);
				String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice").toString();
				Log.consoleLog(ifr, "phoneNumberOffice==>" + phoneNumberOffice);
				String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
				Log.consoleLog(ifr, "customerFlag==>" + customerFlag);
				String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
				Log.consoleLog(ifr, "aadharNo==>" + aadharNo);

				String aadharVaultRefNo = "";
//                AadharVault av = new AadharVault();
//                String aadharVaultRefNo = av.pushData(ifr, aadharNo);
//                Log.consoleLog(ifr, "aadharVaultRefNo==>" + aadharVaultRefNo);
//                if (aadharVaultRefNo.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                    return RLOS_Constants.ERROR;
//                }

				String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName").toString();
				Log.consoleLog(ifr, "CustomerFirstName==>" + CustomerFirstName);
				String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName").toString();
				Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
				String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
				Log.consoleLog(ifr, "customerLastName==>" + customerLastName);
				String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
				Log.consoleLog(ifr, "customerSex==>" + customerSex);
				String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
				Log.consoleLog(ifr, "maritalStatus==>" + maritalStatus);
				String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				Log.consoleLog(ifr, "landlineNumber==>" + landlineNumber);
				String emailAddress = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				Log.consoleLog(ifr, "emailAddress==>" + emailAddress);
				String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
				Log.consoleLog(ifr, "permenanetState==>" + permenanetState);
				String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
				Log.consoleLog(ifr, "permanentCity==>" + permanentCity);
				String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
				Log.consoleLog(ifr, "permanentCountry==>" + permanentCountry);
				String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
				Log.consoleLog(ifr, "permanentZip==>" + permanentZip);
				String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
				Log.consoleLog(ifr, "NRI==>" + NRI);
				String staff = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
				Log.consoleLog(ifr, "staff==>" + staff);
//                String DateOfCustOpen = CustomerAccountSummaryResponseJSONObj.get("DateOfCustOpen").toString();
//                Log.consoleLog(ifr, "DateOfCustOpen==>" + DateOfCustOpen);

				String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
				Log.consoleLog(ifr, "panNumber==>" + panNumber);
				if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
						&& (landlineNumber.length() > 10)) {
					landlineNumber = landlineNumber.substring(0, 9);
				}
				// String phoneNumber =
				// CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				String mobile_Number = MobNum;
				// String mobile_Number =
				// String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
				String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
				Log.consoleLog(ifr, "mailAddress1==>" + mailAddress1);
				String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
				Log.consoleLog(ifr, "mailAddress2==>" + mailAddress2);
				String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
				Log.consoleLog(ifr, "mailAddress3==>" + mailAddress3);
				String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
				Log.consoleLog(ifr, "fatherName==>" + fatherName);

				if (customerSex.equalsIgnoreCase("MALE")) {
					title = "MR";

				} else if (customerSex.equalsIgnoreCase("FEMALE")) {
					title = "MRS";
				}
				// String AccountId = "";

				// Modifed by Ahmed on 18-07-2024
				String AccountId = "";
				String productCode = "";
				if (!(cf.getJsonValue(CustomerAccountSummaryResponseJSONObj, "CASADetailsDTO").equalsIgnoreCase(""))) {
					String CASADetailsDTO = CustomerAccountSummaryResponseJSONObj.get("CASADetailsDTO").toString();
					Log.consoleLog(ifr, "CASADetailsDTO==>" + CASADetailsDTO);

					// String productCode = "";
					if (Accountnumber.equalsIgnoreCase("") || Accountnumber.equalsIgnoreCase(null)) {
						JSONArray CASADetailsDTOJSONJSON = (JSONArray) parser.parse(CASADetailsDTO);

						for (int i = 0; i < CASADetailsDTOJSONJSON.size(); i++) {

							if (!productCode.equalsIgnoreCase("")) {
								productCode = productCode + ",";
							}

							Log.consoleLog(ifr, "CASADetailsDTOResponseObj==>" + CASADetailsDTOJSONJSON.get(i));
							String InputString = CASADetailsDTOJSONJSON.get(i).toString();
							JSONObject InputStringResponseJSONJSONObj = (JSONObject) parser.parse(InputString);
							// JSONObject InputStringResponseJSONJSONObj = new
							// JSONObject(InputStringResponseJSON);
							JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
							productCode = productCode + AtrresultObj.get("productCode").toString();
							Log.consoleLog(ifr, "productCode===>" + productCode);

							if (AtrresultObj.get("AccountId").toString().equalsIgnoreCase(Accountnumber)) {
								Log.consoleLog(ifr, "AccountId===>" + AccountId);
								AccountId = AtrresultObj.get("AccountId").toString();
							}
						}

					}
				}

				JSONObject jsonValues = new JSONObject();

				jsonValues.put("PermAddress1", permAddress1);
				jsonValues.put("PermAddress2", permAddress2);
				jsonValues.put("PermAddress3", permAddress3);
				jsonValues.put("DateOfBirth", dateOfBirth);
				// jsonValues.put("DateOfCustOpen", DateOfCustOpen);
				jsonValues.put("ServicingBranch", servicingBranch);
				jsonValues.put("MailCity", mailCity);
				jsonValues.put("PhoneNumberOffice", phoneNumberOffice);
				jsonValues.put("CustomerFlag", customerFlag);
				jsonValues.put("AadharNo", aadharNo);
				jsonValues.put("CustomerFirstName", CustomerFirstName);
				jsonValues.put("CustomerMiddleName", customerMiddleName);
				jsonValues.put("CustomerLastName", customerLastName);
				jsonValues.put("CustomerSex", customerSex);
				jsonValues.put("MaritalStatus", maritalStatus);
				jsonValues.put("LandlineNumber", landlineNumber);
				jsonValues.put("EmailAddress", emailAddress);
				jsonValues.put("PermenanetState", permenanetState);
				jsonValues.put("PermanentCity", permanentCity);
				jsonValues.put("PermanentCountry", permanentCountry);
				jsonValues.put("PermanentZip", permanentZip);
				jsonValues.put("NRI", NRI);
				jsonValues.put("mobile_Number", mobile_Number);
				jsonValues.put("mailAddress1", mailAddress1);
				jsonValues.put("mailAddress2", mailAddress2);
				jsonValues.put("mailAddress3", mailAddress3);
				jsonValues.put("fatherName", fatherName);
				jsonValues.put("homeBranch", homeBranch);
				jsonValues.put("emailID", emailID);
				jsonValues.put("CustomerID", CustomerID);
				jsonValues.put("AccountId", AccountId);
				jsonValues.put("PanNumber", panNumber);

				jsonValues.put("AadharNo", aadharNo);// Modified by Ahmed on 12-06-2024 for Aadhar Vault Implemenation
				jsonValues.put("Staff", staff);
				jsonValues.put("productCode", productCode);
				Log.consoleLog(ifr, "Entered into Return Balue..." + jsonValues.toString());
				return jsonValues.toString();
			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
			String APIName = "CBS_CustomerAccountSummary";
			String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				APIStatusSend = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				APIStatusSend = RLOS_Constants.ERROR;
			}
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage,
					APIStatus);

			if (APIStatusSend.contains(RLOS_Constants.ERROR) || APIStatusSend.equalsIgnoreCase("")) {
				return RLOS_Constants.ERROR + "" + ErrorMessage;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getCustomerAccountSummary" + e);
			Log.errorLog(ifr, "Exception/getCustomerAccountSummary" + e);
		}

		return RLOS_Constants.ERROR;
	}
	
	public String getCustomerAccountSummarySHL(IFormReference ifr, HashMap<String, String> customerdetails) {

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		String title = "";
		Log.consoleLog(ifr, "Entered into CBSCustomerAccountSummaryAPI...");
		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		String MobNum = customerdetails.get("MobileNumber");
		// Added by Vandana on 12/02/2024 to handel mobile with country code.
		if (!MobNum.equalsIgnoreCase("")) {
			MobNum = "91" + MobNum;
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-MobileNumber:" + MobNum);
		String CustomerId = "";
		String Accountnumber = "";
		String PANNumber = "";
		String strAdhar = "";
		if (customerdetails.containsKey("CustomerId")) {
			CustomerId = customerdetails.get("CustomerId");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-CustomerId:" + CustomerId);
		if (customerdetails.containsKey("Accountnumber")) {
			Accountnumber = customerdetails.get("Accountnumber");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-Accountnumber:" + Accountnumber);
		if (customerdetails.containsKey("PANNumber")) {
			PANNumber = customerdetails.get("PANNumber");
		}
		Log.consoleLog(ifr, "CustomerAccountSummary:getCustomerAccountSummary-PANNumber:" + PANNumber);
		if (customerdetails.containsKey("AdharNumber")) {
			strAdhar = customerdetails.get("AdharNumber");
		}
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//        String formattedDate = dateFormat.format(currentDate);

		String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
		String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
		String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
		String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
		String APIStatusSend = "";
		try {
			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobNum + "\",\n" + "        \"AccountNumber\":  \"" + Accountnumber
					+ "\",\n" + "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"" + PANNumber + "\",\n"
					+ "        \"AadhaarNo\" : \"" + strAdhar + "\",\n" + "        \"PassportNo\": \"\"\n" + "    }\n"
					+ "}";

//         
			// HashMap<String, String> requestHeader = new HashMap<>();
			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			String CustomerID = "";
			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);
			// JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);

			String ErrorCode = "";
			String ErrorMessage = "";
			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
				JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
						.parse(CustomerAccountSummaryResponse);
				// JSONObject CustomerAccountSummaryResponseJSONObj = new
				// JSONObject(CustomerAccountSummaryResponseJSON);
				CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
				String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber").toString();
				Log.consoleLog(ifr, "accountIdString : " + accountIdString);
				Log.consoleLog(ifr, "CustomerID==>" + CustomerID);
				String currentStatus = "";
				String branchDtls = "";
				String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				Log.consoleLog(ifr, "emailID==>" + emailID);
				String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
				Log.consoleLog(ifr, "homeBranch==>" + homeBranch);
				String branchCode = "";
				try {
					if (homeBranch.contains("-")) {
						String[] BRSet = homeBranch.split("-");
						branchCode = BRSet[0];
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
				}
				String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
				Log.consoleLog(ifr, "permAddress1==>" + permAddress1);
				String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
				Log.consoleLog(ifr, "permAddress2==>" + permAddress2);
				String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
				Log.consoleLog(ifr, "permAddress3==>" + permAddress3);
				String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
				Log.consoleLog(ifr, "dateOfBirth==>" + dateOfBirth);
				String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
				Log.consoleLog(ifr, "servicingBranch==>" + servicingBranch);
				String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
				Log.consoleLog(ifr, "mailCity==>" + mailCity);
				String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice").toString();
				Log.consoleLog(ifr, "phoneNumberOffice==>" + phoneNumberOffice);
				String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
				Log.consoleLog(ifr, "customerFlag==>" + customerFlag);
				String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
				Log.consoleLog(ifr, "aadharNo==>" + aadharNo);

				String aadharVaultRefNo = "";
//                AadharVault av = new AadharVault();
//                String aadharVaultRefNo = av.pushData(ifr, aadharNo);
//                Log.consoleLog(ifr, "aadharVaultRefNo==>" + aadharVaultRefNo);
//                if (aadharVaultRefNo.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//                    return RLOS_Constants.ERROR;
//                }

				String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName").toString();
				Log.consoleLog(ifr, "CustomerFirstName==>" + CustomerFirstName);
				String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName").toString();
				Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
				String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
				Log.consoleLog(ifr, "customerLastName==>" + customerLastName);
				String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
				Log.consoleLog(ifr, "customerSex==>" + customerSex);
				String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
				Log.consoleLog(ifr, "maritalStatus==>" + maritalStatus);
				String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				Log.consoleLog(ifr, "landlineNumber==>" + landlineNumber);
				String emailAddress = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				Log.consoleLog(ifr, "emailAddress==>" + emailAddress);
				String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
				Log.consoleLog(ifr, "permenanetState==>" + permenanetState);
				String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
				Log.consoleLog(ifr, "permanentCity==>" + permanentCity);
				String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
				Log.consoleLog(ifr, "permanentCountry==>" + permanentCountry);
				String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
				Log.consoleLog(ifr, "permanentZip==>" + permanentZip);
				String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
				Log.consoleLog(ifr, "NRI==>" + NRI);
				String staff = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
				Log.consoleLog(ifr, "staff==>" + staff);
//                String DateOfCustOpen = CustomerAccountSummaryResponseJSONObj.get("DateOfCustOpen").toString();
//                Log.consoleLog(ifr, "DateOfCustOpen==>" + DateOfCustOpen);

				String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
				Log.consoleLog(ifr, "panNumber==>" + panNumber);
				if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
						&& (landlineNumber.length() > 10)) {
					landlineNumber = landlineNumber.substring(0, 9);
				}
				// String phoneNumber =
				// CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				String mobile_Number = MobNum;
				// String mobile_Number =
				// String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
				String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
				Log.consoleLog(ifr, "mailAddress1==>" + mailAddress1);
				String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
				Log.consoleLog(ifr, "mailAddress2==>" + mailAddress2);
				String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
				Log.consoleLog(ifr, "mailAddress3==>" + mailAddress3);
				String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
				Log.consoleLog(ifr, "fatherName==>" + fatherName);

				if (customerSex.equalsIgnoreCase("MALE")) {
					title = "MR";

				} else if (customerSex.equalsIgnoreCase("FEMALE")) {
					title = "MRS";
				}
				// String AccountId = "";

				// Modifed by Ahmed on 18-07-2024
				String AccountId = "";
				String productCode = "";
				if (!(cf.getJsonValue(CustomerAccountSummaryResponseJSONObj, "CASADetailsDTO").equalsIgnoreCase(""))) {
					String CASADetailsDTO = CustomerAccountSummaryResponseJSONObj.get("CASADetailsDTO").toString();
					Log.consoleLog(ifr, "CASADetailsDTO==>" + CASADetailsDTO);

					// String productCode = "";
					if (Accountnumber.equalsIgnoreCase("") || Accountnumber.equalsIgnoreCase(null)) {
						JSONArray CASADetailsDTOJSONJSON = (JSONArray) parser.parse(CASADetailsDTO);

						for (int i = 0; i < CASADetailsDTOJSONJSON.size(); i++) {

							if (!productCode.equalsIgnoreCase("")) {
								productCode = productCode + ",";
							}

							Log.consoleLog(ifr, "CASADetailsDTOResponseObj==>" + CASADetailsDTOJSONJSON.get(i));
							String InputString = CASADetailsDTOJSONJSON.get(i).toString();
							JSONObject InputStringResponseJSONJSONObj = (JSONObject) parser.parse(InputString);
							// JSONObject InputStringResponseJSONJSONObj = new
							// JSONObject(InputStringResponseJSON);
							JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
							productCode = productCode + AtrresultObj.get("productCode").toString();
							Log.consoleLog(ifr, "productCode===>" + productCode);

							if (AtrresultObj.get("AccountId").toString().equalsIgnoreCase(Accountnumber)) {
								Log.consoleLog(ifr, "AccountId===>" + AccountId);
								AccountId = AtrresultObj.get("AccountId").toString();
							}
						}

					}
				}

				JSONObject jsonValues = new JSONObject();

				jsonValues.put("PermAddress1", permAddress1);
				jsonValues.put("PermAddress2", permAddress2);
				jsonValues.put("PermAddress3", permAddress3);
				jsonValues.put("DateOfBirth", dateOfBirth);
				// jsonValues.put("DateOfCustOpen", DateOfCustOpen);
				jsonValues.put("ServicingBranch", servicingBranch);
				jsonValues.put("MailCity", mailCity);
				jsonValues.put("PhoneNumberOffice", phoneNumberOffice);
				jsonValues.put("CustomerFlag", customerFlag);
				jsonValues.put("AadharNo", aadharNo);
				jsonValues.put("CustomerFirstName", CustomerFirstName);
				jsonValues.put("CustomerMiddleName", customerMiddleName);
				jsonValues.put("CustomerLastName", customerLastName);
				jsonValues.put("CustomerSex", customerSex);
				jsonValues.put("MaritalStatus", maritalStatus);
				jsonValues.put("LandlineNumber", landlineNumber);
				jsonValues.put("EmailAddress", emailAddress);
				jsonValues.put("PermenanetState", permenanetState);
				jsonValues.put("PermanentCity", permanentCity);
				jsonValues.put("PermanentCountry", permanentCountry);
				jsonValues.put("PermanentZip", permanentZip);
				jsonValues.put("NRI", NRI);
				jsonValues.put("mobile_Number", mobile_Number);
				jsonValues.put("mailAddress1", mailAddress1);
				jsonValues.put("mailAddress2", mailAddress2);
				jsonValues.put("mailAddress3", mailAddress3);
				jsonValues.put("fatherName", fatherName);
				jsonValues.put("homeBranch", homeBranch);
				jsonValues.put("emailID", emailID);
				jsonValues.put("CustomerID", CustomerID);
				jsonValues.put("AccountId", AccountId);
				jsonValues.put("PanNumber", panNumber);

				jsonValues.put("AadharNo", aadharNo);// Modified by Ahmed on 12-06-2024 for Aadhar Vault Implemenation
				jsonValues.put("Staff", staff);
				jsonValues.put("productCode", productCode);
				Log.consoleLog(ifr, "Entered into Return Balue..." + jsonValues.toString());
				String returnString = jsonValues.toString() + "###" + Response;
				Log.consoleLog(ifr, "Final CAS return response::" + returnString);
				return returnString;
			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
			String APIName = "CBS_CustomerAccountSummary";
			String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				APIStatusSend = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				APIStatusSend = RLOS_Constants.ERROR;
			}
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage,
					APIStatus);

			if (APIStatusSend.contains(RLOS_Constants.ERROR) || APIStatusSend.equalsIgnoreCase("")) {
				return RLOS_Constants.ERROR + "" + ErrorMessage;
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/getCustomerAccountSummary" + e);
			Log.errorLog(ifr, "Exception/getCustomerAccountSummary" + e);
		}

		return RLOS_Constants.ERROR;
	}

	// Function modified by Ahmed on 31-07-2024 for CAS API Issue
//    public String getAadharCustomerAccountSummary(IFormReference ifr, HashMap<String, String> customerdetails) {
//
//        String apiName = "customerAccountSummary";
//        String serviceName = "CBS_" + apiName;
//        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
//
//        Log.consoleLog(ifr, "getAadharCustomerAccountSummary...");
//        WDGeneralData Data = ifr.getObjGeneralData();
//        String ProcessInstanceId = Data.getM_strProcessInstanceId();
//        Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
//
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//        String formattedDate = dateFormat.format(currentDate);
//
//        String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
//        String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
//        String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
//        String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
//
//        //Added by Ahmed on 18-07-2024
//        String customerId = "";
//        String mobNumber = "";
//
//        if (customerdetails.containsKey("MobileNumber")) {
//            mobNumber = customerdetails.get("MobileNumber");
//        }
//        if (customerdetails.containsKey("customerId")) {
//            customerId = customerdetails.get("customerId");
//        }
//
//        try {
//
//            String Request = "{\n"
//                    + "    \"input\": {\n"
//                    + "        \"SessionContext\": {\n"
//                    + "            \"SupervisorContext\": {\n"
//                    + "                \"PrimaryPassword\": \"\",\n"
//                    + "                \"UserId\": \"\"\n"
//                    + "            },\n"
//                    + "            \"BankCode\": \"" + BankCode + "\",\n"
//                    + "            \"Channel\": \"" + Channel + "\",\n"
//                    + "            \"ExternalBatchNumber\": \"\",\n"
//                    + "            \"ExternalReferenceNo\": \"" + formattedDate + "\",\n"
//                    + "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
//                    + "            \"LocalDateTimeText\": \"\",\n"
//                    + "            \"OriginalReferenceNo\": \"\",\n"
//                    + "            \"OverridenWarnings\": \"\",\n"
//                    + "            \"PostingDateText\": \"\",\n"
//                    + "            \"ServiceCode\": \"\",\n"
//                    + "            \"SessionTicket\": \"\",\n"
//                    + "            \"TransactionBranch\": \"" + TBranch + "\",\n"
//                    + "            \"UserId\": \"" + UserId + "\",\n"
//                    + "            \"UserReferenceNumber\": \"\",\n"
//                    + "            \"ValueDateText\": \"\"\n"
//                    + "        },\n"
//                    + "        \"ExtUniqueRefId\": \"" + formattedDate + "\",\n"
//                    + "        \"CustomerId\": \"" + customerId + "\",\n"
//                    + "        \"MobileNo\":\"91" + mobNumber + "\",\n"
//                    + "        \"AccountNumber\": \"\",\n"
//                    + "        \"DebitCard\":\"\",\n"
//                    + "        \"PanNo\": \"\",\n"
//                    + "        \"AadhaarNo\" : \"\",\n"
//                    + "        \"PassportNo\": \"\"\n"
//                    + "    }\n"
//                    + "}";
//            //HashMap<String, String> requestHeader = new HashMap<>();
//            String Response = cm.getWebServiceResponse(ifr, apiName, Request);
//            Log.consoleLog(ifr, "Response===>" + Response);
//
//            JSONParser parser = new JSONParser();
//            JSONObject resultObj = (JSONObject) parser.parse(Response);
//            //  JSONObject resultObj = new JSONObject(OutputJSON);
//
//            String body = resultObj.get("body").toString();
//            JSONObject bodyObj = (JSONObject) parser.parse(body);
//            // JSONObject bodyObj = new JSONObject(bodyJSON);
//
//            String ErrorCode = "";
//            String ErrorMessage = "";
//            String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
//            try {
//                if (CheckError.equalsIgnoreCase("true")) {
//                    String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
//                    JSONObject CustomerAccountSummaryResponseJSON = (JSONObject) parser.parse(CustomerAccountSummaryResponse);
//                    JSONObject CustomerAccountSummaryResponseJSONObj = new JSONObject(CustomerAccountSummaryResponseJSON);
//
//                    String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
//
//                    //Added by Ahmed on 12-07-2024 for Aadhar Vault
//                    String aadharVaultReq = pcm.getConstantValue(ifr, "CBSVAULT", "REQUIRED");
//
//                    Log.consoleLog(ifr, "aadharVaultReq===>" + aadharVaultReq);
//                    if (aadharVaultReq.equalsIgnoreCase("YES")) {
//                        String aadharRefNo = aadharNo;
//                        AadharVault objAV = new AadharVault();
//                        aadharNo = objAV.getDataByReferenceKey(ifr, aadharRefNo);
//                        return aadharNo;
//                    } else {
//                        return aadharNo;
//                    }
//
//                } else {
//                    String[] ErrorData = CheckError.split("#");
//                    ErrorCode = ErrorData[0];
//                    ErrorMessage = ErrorData[1];
//                }
//            } catch (Exception e) {
//                Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e.getMessage());
//                e.printStackTrace();
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception:" + e);
//        }
//        return "";
//    }
	public String getAadharCustomerAccountSummary(IFormReference ifr, HashMap<String, String> customerdetails) {

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName + "_GetAadhar";
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String request = "";
		String response = "";
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		try {
			Log.consoleLog(ifr, "getAadharCustomerAccountSummary...");
//            WDGeneralData Data = ifr.getObjGeneralData();
//            processInstanceId = Data.getM_strProcessInstanceId();
//            Log.consoleLog(ifr, "ProcessInstanceId==>" + processInstanceId);

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");

			String customerId = "";
			String mobNumber = "";

			if (customerdetails.containsKey("MobileNumber")) {
				mobNumber = customerdetails.get("MobileNumber");
				mobNumber = mobNumber.length() == 10 ? "91" + mobNumber : mobNumber;
			}
			if (customerdetails.containsKey("customerId")) {
				customerId = customerdetails.get("customerId");
			}

			request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + customerId + "\",\n"
					+ "        \"MobileNo\":\"" + mobNumber + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"\",\n" + "        \"AadhaarNo\" : \"\",\n"
					+ "        \"PassportNo\": \"\"\n" + "    }\n" + "}";
			// HashMap<String, String> requestHeader = new HashMap<>();
			response = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "Response===>" + response);

			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(response);
			// JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);

			if (CheckError.equalsIgnoreCase("true")) {
				String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
				JSONObject CustomerAccountSummaryResponseJSON = (JSONObject) parser
						.parse(CustomerAccountSummaryResponse);
				JSONObject CustomerAccountSummaryResponseJSONObj = new JSONObject(CustomerAccountSummaryResponseJSON);

				String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();

				String aadharVaultReq = pcm.getConstantValue(ifr, "CBSVAULT", "REQUIRED");

				Log.consoleLog(ifr, "aadharVaultReq===>" + aadharVaultReq);
				if (aadharVaultReq.equalsIgnoreCase("YES")) {
					String aadharRefNo = aadharNo;
					AadharVault objAV = new AadharVault();
					aadharNo = objAV.getDataByReferenceKey(ifr, aadharRefNo);
					return aadharNo;
				} else {
					return aadharNo;
				}

			} else {
				String[] ErrorData = CheckError.split("#");
				apiErrorCode = ErrorData[0];
				apiErrorMessage = ErrorData[1];
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
		} finally {

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR;
			}

			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return "";
	}

	public String updateCustomerDetails(IFormReference ifr, String Request, String MobNum,
			HashMap<String, String> map) {
		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		String APIStatusSend = "";

		String ApplicantType = map.get("ApplicantType");
		Log.consoleLog(ifr, "ApplicantType===>" + ApplicantType);

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		try {
			String title = "";
			String ErrorCode = "";
			String ErrorMessage = "";

//            HashMap<String, String> requestHeader = new HashMap<>();
			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

//            if (ConfProperty.getIntegrationValue("CBSMOCKFLAG").equalsIgnoreCase("Y")) {
//                Response = "{\"body\":{\"CustomerAccountSummaryResponse\":{\"EmailID\":\"swapnildeep@yahoo.in\",\"IsSignaturePresent\":\"Y\",\"HNI\":\"N\",\"NRI\":\"N\",\"Staff\":\"Y\",\"MailZip\":\"834002\",\"CustomerID\":\"251711230\",\"AadharAuthFlag\":\"Y\",\"AadharMandateDetails\":\"Y\",\"MailState\":\"JHARKHAND\",\"SeniorCitizen\":\"N\",\"CustomerFirstName\":\"SWAPNIL\",\"TDSAmount\":\"0\",\"HomeBranch\":\"4324-SAHIBGANJ\",\"CASADetailsDTO\":[{\"CurrencyShortName\":\"INR\",\"AccountId\":\"74671290000039  \",\"ProductName\":\"OD - CLEAN - OFFICERS\",\"balAvailable\":\"-198960.42\",\"CurrentStatus\":\"ACCOUNT OPEN REGULAR\",\"AcyAmount\":\"-198960.42\",\"BranchDtls\":\"4324-SAHIBGANJ\",\"LcyAmount\":\"-198960.42\",\"amtLien\":\"0.00\",\"amtInstal\":\"0.00\",\"CurrencyCode\":\"104\",\"branchCode\":\"4324\",\"productCode\":\"254\",\"datMaturity\":{},\"ModuleCode\":\"C\",\"TotalAcyAmount\":\"-198960.42\",\"TotalLcyAmount\":\"-198960.42\",\"CustomerRelationship\":\"SOLE OWNER\",\"sweepinbalance\":\"0.00\",\"InternetBankingFacility\":\"N\",\"ratInt\":\"0.00\",\"MobileBankingFacility\":\"N\"},{\"CurrencyShortName\":\"INR\",\"AccountId\":\"75282020000059  \",\"ProductName\":\"CANARA SB STAFF\",\"balAvailable\":\"2695.06\",\"CurrentStatus\":\"ACCOUNT OPEN REGULAR\",\"AcyAmount\":\"2695.06\",\"BranchDtls\":\"4324-SAHIBGANJ\",\"LcyAmount\":\"2695.06\",\"amtLien\":\"0.00\",\"amtInstal\":\"0.00\",\"CurrencyCode\":\"104\",\"branchCode\":\"4324\",\"productCode\":\"111\",\"datMaturity\":{},\"ModuleCode\":\"C\",\"TotalAcyAmount\":\"2695.06\",\"TotalLcyAmount\":\"2695.06\",\"CustomerRelationship\":\"SOLE OWNER\",\"sweepinbalance\":\"0.00\",\"InternetBankingFacility\":{},\"ratInt\":\"0.00\",\"MobileBankingFacility\":{}}],\"Status\":{\"IsServiceChargeApplied\":\"false\",\"ReplyCode\":\"0\",\"ExtendedReply\":{\"MessagesArray\":{}},\"ErrorCode\":\"0\",\"IsOverriden\":\"false\",\"SpReturnValue\":\"0\",\"Memo\":{},\"ExternalReferenceNo\":\"210124145302308\",\"ReplyText\":{}},\"DateOfBirth\":\"05-09-1990\",\"CreditSMSAlert\":{},\"PermAddress1\":\"SO UPENDRA KUMAR\",\"PermAddress2\":\"NEAR KARTIK ORAON CHOWK\",\"PermAddress3\":\"HARMU HOUSING COLONY\",\"RiskCategory\":\"3 High Risk\",\"City\":\"RANCHI\",\"CustomerCategory\":\"STAFF\",\"MaritalStatus\":\"MARRIED\",\"PermZip\":\"834002\",\"State\":\"JHARKHAND\",\"Country\":\"IN\",\"PassportNumber\":{},\"LoanDetailsDTO\":[{\"OSAAmount\":{},\"CurrencyShortName\":\"INR\",\"AccountId\":\"171000022283    \",\"ProductName\":\"CLEAN DPN LOANS-STAFF\",\"balAvailable\":{},\"CurrentStatus\":{},\"OSAmountLCY\":{},\"AcyAmount\":\"889798.00\",\"BranchDtls\":\"4324-null\",\"LcyAmount\":\"1000000.00\",\"amtLien\":{},\"amtInstal\":\"9242.00\",\"CurrencyCode\":\"104\",\"branchCode\":\"4324\",\"productCode\":\"701\",\"datMaturity\":{},\"balPrincipal\":\"889798.00\",\"ModuleCode\":\"L\",\"TotalAcyAmount\":\"1000000.00\",\"TotalLcyAmount\":\"1000000.00\",\"CustomerRelationship\":\"SOLE OWNER\",\"sweepinbalance\":{},\"ratInt\":\"7.45\"},{\"OSAAmount\":{},\"CurrencyShortName\":\"INR\",\"AccountId\":\"171000936176    \",\"ProductName\":\"LHV TWO WHEELER(OFFICERS)\",\"balAvailable\":{},\"CurrentStatus\":{},\"OSAmountLCY\":{},\"AcyAmount\":\"96400.00\",\"BranchDtls\":\"410-null\",\"LcyAmount\":\"96400.00\",\"amtLien\":{},\"amtInstal\":\"1721.00\",\"CurrencyCode\":\"104\",\"branchCode\":\"410\",\"productCode\":\"694\",\"datMaturity\":{},\"balPrincipal\":\"96400.00\",\"ModuleCode\":\"L\",\"TotalAcyAmount\":\"96400.00\",\"TotalLcyAmount\":\"96400.00\",\"CustomerRelationship\":\"SOLE OWNER\",\"sweepinbalance\":{},\"ratInt\":\"6.00\"},{\"OSAAmount\":{},\"CurrencyShortName\":\"INR\",\"AccountId\":\"74679900000020  \",\"ProductName\":\"OSL-STAFF-VEH-FIXED\",\"balAvailable\":{},\"CurrentStatus\":{},\"OSAmountLCY\":{},\"AcyAmount\":\"433333.20\",\"BranchDtls\":\"4324-null\",\"LcyAmount\":\"800000.00\",\"amtLien\":{},\"amtInstal\":\"6666.67\",\"CurrencyCode\":\"104\",\"branchCode\":\"4324\",\"productCode\":\"1990\",\"datMaturity\":{},\"balPrincipal\":\"433333.20\",\"ModuleCode\":\"L\",\"TotalAcyAmount\":\"800000.00\",\"TotalLcyAmount\":\"800000.00\",\"CustomerRelationship\":\"SOLE OWNER\",\"sweepinbalance\":{},\"ratInt\":\"6.00\"}],\"DebitEmailAlert\":{},\"PermCity\":\"RANCHI\",\"MailCountry\":\"IN\",\"CustomerSex\":\"MALE\",\"ProfessionCategory\":\"1 Accountant (Cost/Chartered)\",\"CreditEmailAlert\":{},\"ServicingBranch\":\"4324-SAHIBGANJ\",\"DebitCardList\":\"6522600001058794\",\"DebitSMSAlert\":{},\"MailCity\":\"RANCHI\",\"PhoneNumberOffice\":{},\"AadharNo\":\"666408403543\",\"CustomerFlag\":\"Y\",\"PermState\":\"JHARKHAND\",\"CustomerMiddleName\":\"null\",\"IncomeCategory\":\"05 300001\\t500000\",\"DateofIncorporation\":{},\"MobileNumber\":\"918709901955\",\"DefaultAccountNumber\":\"75282020000059  \",\"CustomerLastName\":\"DEEP\",\"PanNumber\":\"BDQPD6559P\",\"PhoneNumber\":\"918709901955\",\"PermCountry\":\"IN\",\"MailAddress3\":\"HARMU HOUSING COLONY\",\"MailAddress1\":\"SO UPENDRA KUMAR\",\"MailAddress2\":\"NEAR KARTIK ORAON CHOWK\",\"FatherName\":\"MRD\"}},\"responseCode\":200}";
//            } else {
//                Response = cf.CallWebService(ifr, "customerAccountSummary", Request, "", requestHeader);
//            }
//            Log.consoleLog(ifr, "Response===>" + Response);
			String CustomerID = "";
			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);
			// JSONObject resultObj = new JSONObject(OutputJSON);
			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);
			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
				JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
						.parse(CustomerAccountSummaryResponse);
				// JSONObject CustomerAccountSummaryResponseJSONObj = new
				// JSONObject(CustomerAccountSummaryResponseJSON);
				CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
				Log.consoleLog(ifr, "CustomerID==>" + CustomerID);
				String currentStatus = "";
				String branchDtls = "";
				String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber").toString();
				;
				String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
				String branchCode = "";
				try {
					if (homeBranch.contains("-")) {
						String[] BRSet = homeBranch.split("-");
						branchCode = BRSet[0];
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
				}

				String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
				String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
				String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
				String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
				String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
				String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
				String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice").toString();
				String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
				String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
				String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName").toString();
				String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName").toString();
				String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
				String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
				String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
				String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();

				if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
						&& (landlineNumber.length() > 10)) {
					landlineNumber = landlineNumber.substring(0, 9);
				}

				String emailAddress = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
				String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
				String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
				String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
				String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
				String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
				// String phoneNumber =
				// CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				String mobile_Number = MobNum;
				// String mobile_Number =
				// String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
				String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
				String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
				String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
				String State = CustomerAccountSummaryResponseJSONObj.get("State").toString();
				String Country = CustomerAccountSummaryResponseJSONObj.get("Country").toString();
				String MailZip = CustomerAccountSummaryResponseJSONObj.get("MailZip").toString();
				String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
				// Occupation Type
				String strOccuptaion = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();
				Log.consoleLog(ifr, "updateCustomerAccountSummary:strOccuptaion::" + strOccuptaion);
				if (customerSex.equalsIgnoreCase("MALE")) {
					title = "MR";
				} else if (customerSex.equalsIgnoreCase("FEMALE")) {
					title = "MRS";
				}
				// Data saving in backoffice
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				JSONArray childEmpJsonArray = new JSONArray();
				JSONArray childEmpJsonArray1 = new JSONArray();
				JSONObject EMPJSONObject = new JSONObject();
				Log.consoleLog(ifr, "Party Details Grid Info :");
				jsonObject.put("QNL_BASIC_INFO-CustomerID", CustomerID);
				jsonObject.put("QNL_BASIC_INFO-ExistingCustomer", "Yes");
				jsonObject.put("QNL_BASIC_INFO-customerFlag", customerFlag);
				jsonObject.put("QNL_BASIC_INFO-NRI", NRI);
				jsonObject.put("QNL_BASIC_INFO-accountIdString", accountIdString);
				jsonObject.put("QNL_BASIC_INFO-currentStatus", currentStatus);
				jsonObject.put("QNL_BASIC_INFO-branchDtls", branchDtls);
				jsonObject.put("QNL_BASIC_INFO-ApplicantType", ApplicantType);
				Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
				if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null"))
						|| (customerMiddleName == null)) {
					jsonObject.put("QNL_BASIC_INFO-FullName", CustomerFirstName + " " + customerLastName);
				} else {
					jsonObject.put("QNL_BASIC_INFO-FullName",
							CustomerFirstName + " " + customerMiddleName + " " + customerLastName);
				}
				jsonObject.put("QNL_BASIC_INFO-EntityType", "I");
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-EmailID", emailID);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-OffTelNo", phoneNumberOffice);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-FirstName", CustomerFirstName);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-MiddleName", customerMiddleName);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-LastName", customerLastName);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-Gender", customerSex);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-FatherName", fatherName);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-LandlineNo", landlineNumber);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-MaritalStatus", maritalStatus);
				if (Country.equalsIgnoreCase("IN")) {
					jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-Nationality", "Indian");
				}
				try {
					DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date date = originalFormat.parse(dateOfBirth);
					String DOB = targetFormat.format(date);
					Log.consoleLog(ifr, "DOB==>" + DOB);
					jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-DOB", DOB);
				} catch (Exception e) {
					Log.consoleLog(ifr, "DateParsing==>" + e);
				}

				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-MobileNo", mobile_Number);
				jsonObject.put("QNL_BASIC_INFO-CL_BASIC_INFO_I-Title", title);
				// P
				JSONObject ADDJSONObject1 = new JSONObject();
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-AddressType", "P");
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line1", permAddress1);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line2", permAddress2);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line3", permAddress3);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-City_Town_Village", permanentCity);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-State", permenanetState);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Country", permanentCountry);
				ADDJSONObject1.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-PinCode", permanentZip);
				childEmpJsonArray.add(ADDJSONObject1);
				// CA
				JSONObject ADDJSONObject2 = new JSONObject();
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-AddressType", "CA");
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line1", mailAddress1);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line2", mailAddress2);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Line3", mailAddress3);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-City_Town_Village", mailCity);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-State", State);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-Country", Country);
				ADDJSONObject2.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS-PinCode", MailZip);

				childEmpJsonArray.add(ADDJSONObject2);
				EMPJSONObject.put("QNL_BASIC_INFO-CNL_KYC2-KYC_ID", "TAXID");
				EMPJSONObject.put("QNL_BASIC_INFO-CNL_KYC2-KYC_No", panNumber);
				childEmpJsonArray1.add(EMPJSONObject);
				jsonObject.put("QNL_BASIC_INFO-CNL_CUST_ADDRESS", childEmpJsonArray);
				jsonObject.put("QNL_BASIC_INFO-CNL_KYC2", childEmpJsonArray1);
				// added by vandana to store occupation info
				JSONObject ADDJSONObject3 = new JSONObject();
				JSONArray childEmpJsonArray3 = new JSONArray();
				ADDJSONObject3.put("QNL_BASIC_INFO-CNL_OCCUPATION_INFO-OccupationType", "");
				childEmpJsonArray3.add(ADDJSONObject3);
				jsonObject.put("QNL_BASIC_INFO-CNL_OCCUPATION_INFO", childEmpJsonArray3);

				jsonArray.add(jsonObject);
				Log.consoleLog(ifr, "jsonArray1234EMP" + jsonArray);
				((IFormAPIHandler) ifr).addDataToGrid("QNL_BASIC_INFO", jsonArray, true);
				ifr.setValue("QL_SOURCINGINFO.BranchCode", branchCode);
			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
			String APIName = "CBS_CustomerAccountSummary";
			String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				APIStatusSend = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				APIStatusSend = RLOS_Constants.ERROR + " " + apic.getErrorCodeDescription(ifr, APIName, ErrorCode);
			}
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage,
					APIStatus);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		}
		if (APIStatusSend.contains(RLOS_Constants.ERROR) || APIStatusSend.equalsIgnoreCase("")) {
			return pcm.returnError(ifr);
		}
		return "";
	}

	public String getCustomerAccountParams_VL(IFormReference ifr, String MobileNo, String applicantType) {
		Log.consoleLog(ifr, "#getCustomerAccountParams_VL");

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		try {
			WDGeneralData Data = ifr.getObjGeneralData();
			String ProcessInstanceId = Data.getM_strProcessInstanceId();
			Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

//            Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
//            String formattedDate = dateFormat.format(currentDate);
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
			// String applicanttype ="CB";
			// Added by Vandana on 12/02/2024 to handel mobile with country code.
			// commented by ishwarya on 29-06-2024
//            if (!MobileNo.equalsIgnoreCase("")) {
//                MobileNo = "91" + MobileNo;
//            }
			String CustomerId = pcm.getCustomerIDCB(ifr, applicantType);

			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobileNo + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"\",\n" + "        \"AadhaarNo\" : \"\",\n"
					+ "        \"PassportNo\": \"\"\n" + "    }\n" + "}";

			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);
			// JSONObject resultObj = new JSONObject(OutputJSON);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			// JSONObject bodyObj = new JSONObject(bodyJSON);

			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			try {
				if (CheckError.equalsIgnoreCase("true")) {
					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
					JSONObject CustomerAccountSummaryResponseJSON = (JSONObject) parser
							.parse(CustomerAccountSummaryResponse);
					JSONObject CustomerAccountSummaryResponseJSONObj = new JSONObject(
							CustomerAccountSummaryResponseJSON);
					String CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
					String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
					String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
					String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
					String Staff = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
					String DateofBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
					Log.consoleLog(ifr, "aadharNo====>" + CustomerID);
					Log.consoleLog(ifr, "aadharNo====>" + aadharNo);
					Log.consoleLog(ifr, "panNumber===>" + panNumber);
					Log.consoleLog(ifr, "NRI===>" + NRI);
					Log.consoleLog(ifr, "Staff===>" + Staff);
					Log.consoleLog(ifr, "DateofBirth===>" + DateofBirth);

					DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
					Date date = originalFormat.parse(DateofBirth);
					String DOB = targetFormat.format(date);
					Log.consoleLog(ifr, "DOB=======>" + DOB);

					String CASADetailsDTO = CustomerAccountSummaryResponseJSONObj.get("CASADetailsDTO").toString();
					Log.consoleLog(ifr, "CASADetailsDTO==>" + CASADetailsDTO);

					JSONArray CASADetailsDTOJSONJSON = (JSONArray) parser.parse(CASADetailsDTO);
					Log.consoleLog(ifr, "CASADetailsDTOJSONJSON.size()==>" + CASADetailsDTOJSONJSON.size());
					String productCode = "";
					for (int i = 0; i < CASADetailsDTOJSONJSON.size(); i++) {
						if (!productCode.equalsIgnoreCase("")) {
							productCode = productCode + ",";
						}
						Log.consoleLog(ifr, "CASADetailsDTOResponseObj==>" + CASADetailsDTOJSONJSON.get(i));
						String InputString = CASADetailsDTOJSONJSON.get(i).toString();
						JSONObject InputStringResponseJSONJSONObj = (JSONObject) parser.parse(InputString);
						// JSONObject InputStringResponseJSONJSONObj = new
						// JSONObject(InputStringResponseJSON);
						JSONObject AtrresultObj = new JSONObject(InputStringResponseJSONJSONObj);
						productCode = productCode + AtrresultObj.get("productCode").toString();
						Log.consoleLog(ifr, "productCode===>" + productCode);
					}

					JSONObject jsonValues = new JSONObject();

					jsonValues.put("AadharNo", aadharNo);
					jsonValues.put("PanNumber", panNumber);
					jsonValues.put("NRI", NRI);
					jsonValues.put("Staff", Staff);
					jsonValues.put("productCode", productCode);
					jsonValues.put("DateofBirth", DOB);

					Log.consoleLog(ifr, "Entered into Return Value..." + jsonValues.toString());
					return jsonValues.toString();
				} else {
					return RLOS_Constants.ERROR;
				}
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e.getMessage());
				return RLOS_Constants.ERROR;
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
		}
		return RLOS_Constants.ERROR;

	}

	public String updateCustomerDetailsBorrower(IFormReference ifr, String Request, String MobNum,
			HashMap<String, String> map) {
		WDGeneralData Data = ifr.getObjGeneralData();
		String ProcessInstanceId = Data.getM_strProcessInstanceId();
		String APIStatusSend = "";

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String branchCode = "";
		try {
			String title = "";
			String ErrorCode = "";
			String ErrorMessage = "";
			String Response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + Response);

			String CustomerID = "";
			JSONParser parser = new JSONParser();
			JSONObject resultObj = (JSONObject) parser.parse(Response);

			String body = resultObj.get("body").toString();
			JSONObject bodyObj = (JSONObject) parser.parse(body);

			String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
			if (CheckError.equalsIgnoreCase("true")) {
				String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
				JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
						.parse(CustomerAccountSummaryResponse);
				// JSONObject CustomerAccountSummaryResponseJSONObj = new
				// JSONObject(CustomerAccountSummaryResponseJSON);
				CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
				Log.consoleLog(ifr, "CustomerID==>" + CustomerID);
				String currentStatus = "";
				String branchDtls = "";
				String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber").toString();
				;
				String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();

				try {
					if (homeBranch.contains("-")) {
						String[] BRSet = homeBranch.split("-");
						branchCode = BRSet[0];
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
				}

				String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
				String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
				String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
				String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
				String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
				String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
				String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice").toString();
				String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
				String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
				String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName").toString();
				String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName").toString();
				String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
				String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
				String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
				String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();

				if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
						&& (landlineNumber.length() > 10)) {
					landlineNumber = landlineNumber.substring(0, 9);
				}

				String emailAddress = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
				String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
				String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
				String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
				String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
				String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
				String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
				// String phoneNumber =
				// CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
				String mobile_Number = MobNum;
				// String mobile_Number =
				// String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
				String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
				String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
				String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
				String State = CustomerAccountSummaryResponseJSONObj.get("State").toString();
				String Country = CustomerAccountSummaryResponseJSONObj.get("Country").toString();
				String MailZip = CustomerAccountSummaryResponseJSONObj.get("MailZip").toString();
				String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
				// Occupation Type
				String strOccuptaion = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();
				Log.consoleLog(ifr, "updateCustomerDetailsBorrower:strOccuptaion::" + strOccuptaion);
				if (customerSex.equalsIgnoreCase("MALE")) {
					title = "MR";
				} else if (customerSex.equalsIgnoreCase("FEMALE")) {
					title = "MRS";
				}
				// Data saving in backoffice
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				JSONArray childEmpJsonArray = new JSONArray();
				JSONArray childEmpJsonArray1 = new JSONArray();
				JSONObject EMPJSONObject = new JSONObject();
				Log.consoleLog(ifr, "Party Details Grid Info :");
				jsonObject.put("QNL_BASIC_INFO_CustomerID", CustomerID);
				jsonObject.put("QNL_BASIC_INFO_ExistingCustomer", "Yes");
				jsonObject.put("QNL_BASIC_INFO_customerFlag", customerFlag);
				jsonObject.put("QNL_BASIC_INFO_NRI", NRI);
				jsonObject.put("QNL_BASIC_INFO_accountIdString", accountIdString);
				jsonObject.put("QNL_BASIC_INFO_currentStatus", currentStatus);
				jsonObject.put("QNL_BASIC_INFO_branchDtls", branchCode);

				jsonObject.put("QNL_BASIC_INFO_ApplicantType", map.get("ApplicantType"));
				Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
				if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null"))
						|| (customerMiddleName == null)) {
					jsonObject.put("QNL_BASIC_INFO_FullName", CustomerFirstName + " " + customerLastName);
				} else {
					jsonObject.put("QNL_BASIC_INFO_FullName",
							CustomerFirstName + " " + customerMiddleName + " " + customerLastName);
				}
				jsonObject.put("QNL_BASIC_INFO_EntityType", "I");
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", emailID);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_OffTelNo", phoneNumberOffice);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName", CustomerFirstName);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", customerMiddleName);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName", customerLastName);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender", customerSex);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName", fatherName);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo", landlineNumber);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus", maritalStatus);

				try {
					DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
					DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
					Date date = originalFormat.parse(dateOfBirth);
					String DOB = targetFormat.format(date);
					Log.consoleLog(ifr, "DOB==>" + DOB);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_DOB", DOB);
					SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
					SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
					// modified by keerthana for saving date value on 07/08/2024
					Date dateq = inputAge.parse(DOB);
					String formattedDate = outputAge.format(dateq);
					LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					int age = calculateAge(calAge);
					Log.consoleLog(ifr, "age" + age);
					String customerAge = String.valueOf(age);
					Log.consoleLog(ifr, "customerAge" + customerAge);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age", customerAge);
				} catch (java.text.ParseException e) {
					Log.consoleLog(ifr, "DateParsing==>" + e);
				}

				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", mobile_Number);
				jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Title", title);
				// P
				JSONObject ADDJSONObject1 = new JSONObject();
				ADDJSONObject1.put("Address Type", "P");
				ADDJSONObject1.put("Line 1", permAddress1);
				ADDJSONObject1.put("Line 2", permAddress2);
				ADDJSONObject1.put("Line 3", permAddress3);
				ADDJSONObject1.put("City", permanentCity);
				ADDJSONObject1.put("State", permenanetState);
				ADDJSONObject1.put("Country", permanentCountry);
				ADDJSONObject1.put("PIN Code", permanentZip);
				childEmpJsonArray.add(ADDJSONObject1);
				// CA
				JSONObject ADDJSONObject2 = new JSONObject();
				ADDJSONObject2.put("Address Type", "CA");
				ADDJSONObject2.put("Line 1", mailAddress1);
				ADDJSONObject2.put("Line 2", mailAddress2);
				ADDJSONObject2.put("Line 3", mailAddress3);
				ADDJSONObject2.put("City", mailCity);
				ADDJSONObject2.put("State", State);
				ADDJSONObject2.put("Country", Country);
				ADDJSONObject2.put("PIN Code", MailZip);

				childEmpJsonArray.add(ADDJSONObject2);
				EMPJSONObject.put("ID Type", "TAXID");
				EMPJSONObject.put("ID Number", panNumber);
				childEmpJsonArray1.add(EMPJSONObject);
				jsonObject.put("LV_ADDRESS", childEmpJsonArray);
				jsonObject.put("LV_KYC", childEmpJsonArray1);
				// added by vandana to store occupation info
				JSONObject ADDJSONObject3 = new JSONObject();
				JSONArray childEmpJsonArray3 = new JSONArray();
				ADDJSONObject3.put("Type of Occupation", strOccuptaion);
				childEmpJsonArray3.add(ADDJSONObject3);
				jsonObject.put("LV_OCCUPATION_INFO", childEmpJsonArray3);
// modified by Kathir for pension LC changes on 06/08/2024
				if (ifr.getActivityName().equalsIgnoreCase("Lead Capture")) {
					jsonObject.put("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", (NRI.equalsIgnoreCase("N") ? "No" : "Yes"));
					Log.consoleLog(ifr,
							"CustomerAccountSummary:updateCustomerDetailsBorrower -> Lead Capture NRI: " + NRI);
				}
				jsonArray.add(jsonObject);
				Log.consoleLog(ifr, "jsonArray1234EMP:::" + jsonArray);
				ifr.addDataToGrid("ALV_BASIC_INFO", jsonArray, true);
				Log.consoleLog(ifr, "Applicant type :::" + map.get("ApplicantType"));
				if (map.get("ApplicantType").equalsIgnoreCase("B")) {
					ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
					Log.consoleLog(ifr, "jsonArray1234EMP:::" + branchCode);
				}
			} else {
				String[] ErrorData = CheckError.split("#");
				ErrorCode = ErrorData[0];
				ErrorMessage = ErrorData[1];
			}
			String APIName = "CBS_CustomerAccountSummary";
			String APIStatus = "";
			if (ErrorMessage.equalsIgnoreCase("")) {
				APIStatus = "SUCCESS";
				APIStatusSend = RLOS_Constants.SUCCESS;
			} else {
				APIStatus = "FAIL";
				APIStatusSend = RLOS_Constants.ERROR + " " + apic.getErrorCodeDescription(ifr, APIName, ErrorCode);
			}
			cm.CaptureRequestResponse(ifr, ProcessInstanceId, APIName, Request, Response, ErrorCode, ErrorMessage,
					APIStatus);
		} catch (ParseException e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		}
		if (APIStatusSend.contains(RLOS_Constants.ERROR) || APIStatusSend.equalsIgnoreCase("")) {
			return pcm.returnError(ifr);
		} else {
			if (branchCode != null) {
				return branchCode;
			}
			return RLOS_Constants.SUCCESS;

		}
		// return "";
	}

	public String fetchCustomerAccountSummaryBorrower(IFormReference ifr, HashMap<String, String> map) {

		String MobNum = "";
		String title = "";
		Log.consoleLog(ifr, "Entered into ExecuteCBS_CustomerAccountSummaryCB...");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		MobNum = map.get("mobileNumber");
		String CustomerId = "";
		if (map.containsKey("CustomerId")) {
			CustomerId = map.get("CustomerId");
		}
		// Added by Vandana on 12/02/2024 to handel mobile with country code.
		if (!MobNum.equalsIgnoreCase("")) {
			MobNum = "91" + MobNum;
		}
		Date currentDate = new Date();
		String PANNumber = "";
		if (map.containsKey("PANNUMBER")) {
			PANNumber = map.get("PANNUMBER");
		}

//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
		// String formattedDate = dateFormat.format(currentDate);
		String status = "";
		try {
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobNum + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"" + PANNumber + "\",\n"
					+ "        \"AadhaarNo\" : \"\",\n" + "        \"PassportNo\": \"\"\n" + "    }\n" + "}";
			String strApplicantType = map.get("ApplicantType");
			Log.consoleLog(ifr, "Applicant type::" + strApplicantType);
			String checkCountData = "Select  pid,Applicanttype from los_nl_basic_info  WHERE PID= '" + ProcessInstanceId
					+ "' and Applicanttype='" + strApplicantType + "'";
			Log.consoleLog(ifr, " checkCountData : " + checkCountData);
			List<List<String>> listCheck = ifr.getDataFromDB(checkCountData);
			Log.consoleLog(ifr, "listCheck : " + listCheck);
			if (!listCheck.isEmpty()) {
				for (int i = 0; i < listCheck.size(); i++) {
					if (listCheck.get(i).get(1).contains("CB")) {
						try {

							status = updateCustomerDetailsBorrower(ifr, Request, MobNum, map);
							Log.consoleLog(ifr, "status ::" + status);
						} catch (Exception e) {
							Log.consoleLog(ifr, "Exception in updateCustomerAccountSummary ::" + e);
							Log.errorLog(ifr, "Exception in updateCustomerAccountSummary::" + e);
						}

					}
				}

			} else {
				Log.consoleLog(ifr, "first loop ");
				status = updateCustomerDetailsBorrower(ifr, Request, MobNum, map);
				Log.consoleLog(ifr, "status  2::" + status);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		Log.consoleLog(ifr, "final return status::" + status);
		return status;
	}

	public String updateCustomerAccountSummaryStaffHL(IFormReference ifr, HashMap<String, String> map) {

		String MobNum = "";

		String Response = "";

		Log.consoleLog(ifr, "Entered into ExecuteCBS_CustomerAccountSummaryCB...");

		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

		MobNum = map.get("mobileNumber");

		Log.consoleLog(ifr, "MobNum type::" + MobNum);

		String CustomerId = "";

		String panNumber = "";

		if (map.containsKey("CustomerId")) {

			CustomerId = map.get("CustomerId");

		}

		if (map.containsKey("panNumber")) {

			panNumber = map.get("panNumber");

		}

		Log.consoleLog(ifr, "Pan Details::" + panNumber);

		Log.consoleLog(ifr, "CustomerId type::" + CustomerId);

		if (!MobNum.equalsIgnoreCase("")) {

			MobNum = "91" + MobNum;

		}

		try {

			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");

			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");

			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");

			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");

			String Request = "{\n"

					+ "    \"input\": {\n"

					+ "        \"SessionContext\": {\n"

					+ "            \"SupervisorContext\": {\n"

					+ "                \"PrimaryPassword\": \"\",\n"

					+ "                \"UserId\": \"\"\n"

					+ "            },\n"

					+ "            \"BankCode\": \"" + BankCode + "\",\n"

					+ "            \"Channel\": \"" + Channel + "\",\n"

					+ "            \"ExternalBatchNumber\": \"\",\n"

					+ "            \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"

					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"

					+ "            \"LocalDateTimeText\": \"\",\n"

					+ "            \"OriginalReferenceNo\": \"\",\n"

					+ "            \"OverridenWarnings\": \"\",\n"

					+ "            \"PostingDateText\": \"\",\n"

					+ "            \"ServiceCode\": \"\",\n"

					+ "            \"SessionTicket\": \"\",\n"

					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n"

					+ "            \"UserId\": \"" + UserId + "\",\n"

					+ "            \"UserReferenceNumber\": \"\",\n"

					+ "            \"ValueDateText\": \"\"\n"

					+ "        },\n"

					+ "        \"ExtUniqueRefId\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"

					+ "        \"CustomerId\": \"" + CustomerId + "\",\n"

					+ "        \"MobileNo\":\"" + MobNum + "\",\n"

					+ "        \"AccountNumber\": \"\",\n"

					+ "        \"DebitCard\":\"\",\n"

					+ "        \"PanNo\": \"" + panNumber + "\",\n"

					+ "        \"AadhaarNo\" : \"\",\n"

					+ "        \"PassportNo\": \"\"\n"

					+ "    }\n"

					+ "}";

			Log.consoleLog(ifr, "request::" + Request);

			String strApplicantType = map.get("ApplicantType");

			Log.consoleLog(ifr, "Applicant type::" + strApplicantType);

			Log.consoleLog(ifr, "first loop ");

			Response = updateCustomerDetailsStaffHL(ifr, Request, MobNum, map);

			if (Response.contains(RLOS_Constants.ERROR)) {

				return pcm.returnCustomErrorMessage(ifr, Response);

			}

			return Response;

		} catch (Exception e) {

			Log.consoleLog(ifr, "Exception:" + e);

			Log.errorLog(ifr, "Exception:" + e);

		}

		return "";

	}

	public String updateCustomerDetailsStaffHL(IFormReference ifr, String request, String MobNum,
			HashMap<String, String> map) {

		WDGeneralData Data = ifr.getObjGeneralData();

		String ProcessInstanceId = Data.getM_strProcessInstanceId();

		String APIStatusSend = "";

		JSONObject finalResponse = new JSONObject();

		String ApplicantType = map.get("ApplicantType");

		Log.consoleLog(ifr, "ApplicantType===>" + ApplicantType);

		String apiName = "customerAccountSummary";

		String serviceName = "CBS_" + apiName;

		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);

		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

		String apiStatus = "";

		String apiErrorCode = "";

		String apiErrorMessage = "";

		String response = "";

		try {

			String title = "";

			HashMap<String, Object> requestHeader = new HashMap<>();

			response = cm.getWebServiceResponse(ifr, apiName, request);

			Log.consoleLog(ifr, "response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {

				String CustomerID = "";

				JSONParser parser = new JSONParser();

				JSONObject resultObj = (JSONObject) parser.parse(response);

				String body = resultObj.get("body").toString();

				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);

				if (CheckError.equalsIgnoreCase("true")) {

					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();

					finalResponse = (JSONObject) parser.parse(CustomerAccountSummaryResponse);

					CustomerID = finalResponse.get("CustomerID").toString();

					Demographic objDemographic = new Demographic();

					String GetDemoGraphicData = objDemographic.getDemographicSHL(ifr, ProcessInstanceId, CustomerID);

					Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);

					if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {

						Log.consoleLog(ifr, "inside error condition Demographic Budget");

						return pcm.returnErrorAPIThroughExecute2(ifr, GetDemoGraphicData);

					} else {

						Log.consoleLog(ifr, "inside non-error condition Demographic Budget");

						JSONParser jsonparser = new JSONParser();

						JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);

						finalResponse.putAll(obj);

					}

					Advanced360EnquiryDatav2 objAdv2 = new Advanced360EnquiryDatav2();

					String productCode = pcm.getProductCodeByProductName(ifr);

					String advance360Res = objAdv2.executeCBSAdvanced360Inquiryv2(ifr, ProcessInstanceId, CustomerID,
							productCode, "", "");

					if (advance360Res.contains(RLOS_Constants.ERROR)) {

						return pcm.returnErrorAPIThroughExecute2(ifr, advance360Res);

					}

					JSONObject parsedAdvance360Res = (JSONObject) parser.parse(advance360Res);

					Log.consoleLog(ifr,
							"Values from  Adding 360  into Return Balue..." + parsedAdvance360Res.toString());

					finalResponse.putAll(parsedAdvance360Res);

					Log.consoleLog(ifr, "After Adding 360  into Return Balue..." + finalResponse.toString());

				} else {

					String[] ErrorData = CheckError.split("#");

					apiErrorCode = ErrorData[0];

					apiErrorMessage = ErrorData[1];

				}

			} else {

				response = "No response from the server.";

				apiErrorMessage = response;

			}

			if (apiErrorMessage.equalsIgnoreCase("")) {

				apiStatus = finalResponse.toString();

			} else {

				apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;

			}

			Log.consoleLog(ifr, "Final response " + apiStatus);

			return apiStatus;

		} catch (Exception e) {

			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);

			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);

		} finally {

			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,

					apiErrorCode, apiErrorMessage, apiStatus);

		}

		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String fetchCustomerAccountSummaryCoBorrower(IFormReference ifr, HashMap<String, String> map) {
		Log.consoleLog(ifr, "inside fetchCustomerAccountSummaryBorrower");
		String MobNum = "";
		String title = "";
		Log.consoleLog(ifr, "Entered into ExecuteCBS_CustomerAccountSummaryCB...");
		String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
		MobNum = map.get("mobileNumber");
		String CustomerId = "";
		if (map.containsKey("CustomerId")) {
			CustomerId = map.get("CustomerId");
		}
		// Added by Vandana on 12/02/2024 to handel mobile with country code.
		if (!MobNum.equalsIgnoreCase("")) {
			MobNum = "91" + MobNum;
		}
		String PANNumber = "";
		if (map.containsKey("PANNUMBER")) {
			PANNumber = map.get("PANNUMBER");
		}

		String status = "";
		try {
			String BankCode = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");
			String Channel = pcm.getConstantValue(ifr, "CBSACCSUM", "CHANNEL");
			String UserId = pcm.getConstantValue(ifr, "CBSACCSUM", "USERID");
			String TBranch = pcm.getConstantValue(ifr, "CBSACCSUM", "TBRANCH");
			String Request = "{\n" + "    \"input\": {\n" + "        \"SessionContext\": {\n"
					+ "            \"SupervisorContext\": {\n" + "                \"PrimaryPassword\": \"\",\n"
					+ "                \"UserId\": \"\"\n" + "            },\n" + "            \"BankCode\": \""
					+ BankCode + "\",\n" + "            \"Channel\": \"" + Channel + "\",\n"
					+ "            \"ExternalBatchNumber\": \"\",\n" + "            \"ExternalReferenceNo\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n"
					+ "            \"ExternalSystemAuditTrailNumber\": \"\",\n"
					+ "            \"LocalDateTimeText\": \"\",\n" + "            \"OriginalReferenceNo\": \"\",\n"
					+ "            \"OverridenWarnings\": \"\",\n" + "            \"PostingDateText\": \"\",\n"
					+ "            \"ServiceCode\": \"\",\n" + "            \"SessionTicket\": \"\",\n"
					+ "            \"TransactionBranch\": \"" + TBranch + "\",\n" + "            \"UserId\": \""
					+ UserId + "\",\n" + "            \"UserReferenceNumber\": \"\",\n"
					+ "            \"ValueDateText\": \"\"\n" + "        },\n" + "        \"ExtUniqueRefId\": \""
					+ cm.getCBSExternalReferenceNo() + "\",\n" + "        \"CustomerId\": \"" + CustomerId + "\",\n"
					+ "        \"MobileNo\":\"" + MobNum + "\",\n" + "        \"AccountNumber\": \"\",\n"
					+ "        \"DebitCard\":\"\",\n" + "        \"PanNo\": \"" + PANNumber + "\",\n"
					+ "        \"AadhaarNo\" : \"\",\n" + "        \"PassportNo\": \"\"\n" + "    }\n" + "}";
			Log.consoleLog(ifr, "first loop ");

			Log.consoleLog(ifr, "Inside updateCustomerDetailsCoBorrower");
			status = updateCustomerDetailsCoBorrower(ifr, Request, MobNum, map);

			Log.consoleLog(ifr, "status  2::" + status);
			if (status.contains(RLOS_Constants.ERROR)) {
				return status;
			}
			JSONParser jp = new JSONParser();
			JSONObject objk = (JSONObject) jp.parse(status);
			Log.consoleLog(ifr, "Final Outout " + objk);
			if (cf.getJsonValue(objk, "showMessage").contains("error")) {
				Log.consoleLog(ifr, "Inside if COmmon Methods: error in customer details");
				JSONArray showMessageArray = (JSONArray) objk.get("showMessage");
				JSONObject firstMessageObject = (JSONObject) showMessageArray.get(0);
				String extractedMessage = (String) firstMessageObject.get("message");
				JSONObject errorMsg = new JSONObject();
				errorMsg.put("showMessage", cf.showMessage(ifr, "Portal_B_SubmitOTP", "error", extractedMessage));
				return errorMsg.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
		}
		Log.consoleLog(ifr, "final return status::" + status);
		return status;
	}

	public String updateCustomerDetailsCoBorrower(IFormReference ifr, String Request, String MobNum,
			HashMap<String, String> map) {
		Log.consoleLog(ifr, "inside updateCustomerDetailsCoBorrower ");
		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		String response = "";

		String branchCode = "";
		String returnJson = "";
		try {
			String title = "";
			response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				String CustomerID = "";
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);

				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
					JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
							.parse(CustomerAccountSummaryResponse);

					CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
					Log.consoleLog(ifr, "CustomerID==>" + CustomerID);

					String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber")
							.toString();
					Log.consoleLog(ifr, "DefaultAccountNumber==>" + accountIdString);

					String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
					Log.consoleLog(ifr, "EmailID==>" + emailID);

					String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
					Log.consoleLog(ifr, "HomeBranch==>" + homeBranch);

					try {
						if (homeBranch.contains("-")) {
							String[] BRSet = homeBranch.split("-");
							branchCode = BRSet[0];
						}
					} catch (Exception e) {
						Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
					}

					String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
					Log.consoleLog(ifr, "PermAddress1==>" + permAddress1);

					String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
					Log.consoleLog(ifr, "PermAddress2==>" + permAddress2);

					String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
					Log.consoleLog(ifr, "PermAddress3==>" + permAddress3);

					String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
					Log.consoleLog(ifr, "DateOfBirth==>" + dateOfBirth);

					String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
					Log.consoleLog(ifr, "ServicingBranch==>" + servicingBranch);

					String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
					Log.consoleLog(ifr, "MailCity==>" + mailCity);

					String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice")
							.toString();
					Log.consoleLog(ifr, "PhoneNumberOffice==>" + phoneNumberOffice);

					String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
					Log.consoleLog(ifr, "customerFlag=" + customerFlag);

					String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
					Log.consoleLog(ifr, "AadharNo==>" + aadharNo);

					String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName")
							.toString();
					Log.consoleLog(ifr, "CustomerFirstName==>" + CustomerFirstName);

					String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName")
							.toString();
					Log.consoleLog(ifr, "CustomerMiddleName==>" + customerMiddleName);

					String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
					Log.consoleLog(ifr, "CustomerLastName==>" + customerLastName);

					String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
					Log.consoleLog(ifr, "CustomerSex==>" + customerSex);

					String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
					Log.consoleLog(ifr, "MaritalStatus==>" + maritalStatus);

					String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
					Log.consoleLog(ifr, "PhoneNumber==>" + landlineNumber);

					if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
							&& (landlineNumber.length() > 10)) {
						landlineNumber = landlineNumber.substring(0, 9);
					}

					String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
					Log.consoleLog(ifr, "permenanetState=" + permenanetState);

					String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
					Log.consoleLog(ifr, "PermCity==>" + permanentCity);

					String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
					Log.consoleLog(ifr, "PermCountry==>" + permanentCountry);

					String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
					Log.consoleLog(ifr, "PermZip==>" + permanentZip);

					String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
					Log.consoleLog(ifr, "NRI==>" + NRI);

					String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
					Log.consoleLog(ifr, "PanNumber==>" + panNumber);

					String mobile_Number = MobNum;
					Log.consoleLog(ifr, "mobile_Number==>" + mobile_Number);

					String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
					Log.consoleLog(ifr, "MailAddress1==>" + mailAddress1);

					String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
					Log.consoleLog(ifr, "MailAddress2==>" + mailAddress2);

					String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
					Log.consoleLog(ifr, "MailAddress3==>" + mailAddress3);

					String State = CustomerAccountSummaryResponseJSONObj.get("State").toString();
					Log.consoleLog(ifr, "State==>" + State);

					String Country = CustomerAccountSummaryResponseJSONObj.get("Country").toString();
					Log.consoleLog(ifr, "Country==>" + Country);

					String MailZip = CustomerAccountSummaryResponseJSONObj.get("MailZip").toString();
					Log.consoleLog(ifr, "MailZip==>" + MailZip);

					String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
					Log.consoleLog(ifr, "fatherName==>" + fatherName);

					String strOccuptaion = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();
					Log.consoleLog(ifr, "updateCustomerDetailsBorrower:strOccuptaion::" + strOccuptaion);

					if (customerSex.equalsIgnoreCase("MALE")) {
						title = "MR";
					} else if (customerSex.equalsIgnoreCase("FEMALE")) {
						title = "MRS";
					}

					if (!customerFlag.equalsIgnoreCase("Y")) {
						return "Customer does not exist";
					}

					JSONObject message = new JSONObject();
					String namePattern = ConfProperty.getCommonPropertyValue("NamePattern");
					String add1Pattern = ConfProperty.getCommonPropertyValue("ADD1PATTERN");
					String add2Pattern = ConfProperty.getCommonPropertyValue("ADD2PATTERN");
					String add3Pattern = ConfProperty.getCommonPropertyValue("ADD3PATTERN");

					Log.consoleLog(ifr, "#Result pattern===>" + namePattern);
					Log.consoleLog(ifr, "#Result add1Pattern===>" + add1Pattern);
					Log.consoleLog(ifr, "#Result add2Pattern===>" + add2Pattern);
					Log.consoleLog(ifr, "#Result add3Pattern===>" + add3Pattern);
					Log.consoleLog(ifr, "CustomerFirstName.length() =" + CustomerFirstName.length());

					if (CustomerFirstName.length() < 2) {
						Log.consoleLog(ifr, "#firstName===>lenghth" + CustomerFirstName);
						return (CustomerFirstName + " : Name is less than 1 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(namePattern, CustomerFirstName) ="
							+ !Pattern.matches(namePattern, CustomerFirstName));

					if (!Pattern.matches(namePattern, CustomerFirstName)) {
						Log.consoleLog(ifr, "#firstName===>" + CustomerFirstName);
						return (CustomerFirstName
								+ " : Name contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "fatherName.length() =" + fatherName.length());

					if (fatherName.length() < 2) {
						Log.consoleLog(ifr, "#fatherName===>lenghth" + fatherName);
						return (fatherName + " : Father Name is less than 1 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "dateOfBirth =" + dateOfBirth);

					if (dateOfBirth.equalsIgnoreCase("") || dateOfBirth.equalsIgnoreCase(null)) {
						Log.consoleLog(ifr, "#dateOfBirth===>lenghth" + dateOfBirth);
						return (dateOfBirth + " : DOB is Mandatory. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress1.length() =" + permAddress1.length());

					if (permAddress1.length() < 4) {
						Log.consoleLog(ifr, "#permAddress1===>lenghth" + permAddress1);
						return (permAddress1
								+ " : Permanent address line 1 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress2.length() =" + permAddress2.length());

					if (permAddress2.length() < 4) {
						Log.consoleLog(ifr, "#permAddress2===>lenghth" + permAddress2);
						return (permAddress2
								+ " : Permanent address line 2 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress3.length() =" + permAddress3.length());

					if (permAddress3.length() < 4) {
						Log.consoleLog(ifr, "#permAddress3===>lenghth" + permAddress3);
						return (permAddress3
								+ " : Permanent address line 3 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)" + permAddress1 + add1Pattern);
					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)"
							+ !Pattern.matches(add1Pattern, permAddress1));

					if (!Pattern.matches(add1Pattern, permAddress1)) {
						Log.consoleLog(ifr, "#permAddress1===>" + permAddress1);
						return (permAddress1
								+ " : Address line 1 contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr,
							"!Pattern.matches(add1Pattern, permAddress1)" + !Pattern.matches(add2Pattern, permAddress2)
									+ !Pattern.matches(add3Pattern, permAddress3));

					if (!Pattern.matches(add2Pattern, permAddress2)) {
						Log.consoleLog(ifr, "#permAddress2===>" + permAddress2);
						return (permAddress2
								+ " : Address line 2 contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)"
							+ !Pattern.matches(add3Pattern, permAddress3));

					if (!Pattern.matches(add3Pattern, permAddress3)) {
						Log.consoleLog(ifr, "#permAddress3===>" + permAddress3);
						return (permAddress3
								+ " : Address line 3 contains invalid special characters. Kindly update the CBS");
					}

					String StateCode = "";
					Log.consoleLog(ifr, "permenanetState=" + permenanetState);

					String stateCodeQ = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
							+ "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + permenanetState + "')) AND ROWNUM=1";
					Log.consoleLog(ifr, "stateCodeQ==>" + stateCodeQ);

					List<List<String>> stateCodeR = cf.mExecuteQuery(ifr, stateCodeQ, "stateCodeQ==> ");
					Log.consoleLog(ifr, "#stateCodeR===>" + stateCodeR.toString());

					if (stateCodeR.size() > 0) {
						StateCode = stateCodeR.get(0).get(0);
					}

					if (StateCode.equalsIgnoreCase("")) {
						Log.consoleLog(ifr, "#StateCode is null ===>");
						return "Applicants Perm State code is not available in state master. Kindly update the CBS.";
					} else {
						String ZIP_FRST_TWO_MIN = "", ZIP_FRST_TWO_MAX = "";
						String Query = "SELECT ZIP_FRST_TWO_MIN,ZIP_FRST_TWO_MAX FROM LOS_MST_STATE WHERE "
								+ "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + permenanetState + "')) AND ROWNUM=1";
						Log.consoleLog(ifr, "Query3==>" + Query);

						List<List<String>> Result = ifr.getDataFromDB(Query);
						Log.consoleLog(ifr, "#Result===>" + Result.toString());

						if (Result.size() > 0) {
							ZIP_FRST_TWO_MIN = Result.get(0).get(0);
							ZIP_FRST_TWO_MAX = Result.get(0).get(1);
						}

						String firstTwoChars = permanentZip.length() >= 2 ? permanentZip.substring(0, 2) : permanentZip;
						Log.consoleLog(ifr, "#firstTwoChars===>" + firstTwoChars);

						if (Integer.parseInt(firstTwoChars) >= Integer.parseInt(ZIP_FRST_TWO_MIN)
								&& Integer.parseInt(firstTwoChars) <= Integer.parseInt(ZIP_FRST_TWO_MAX)) {
							Log.consoleLog(ifr, "#firstTwoChars is within range===>");
						} else {
							Log.consoleLog(ifr, "#firstTwoChars is not in  range ===>");
							return "State Code and PIN code Validation failed. Kindly update the CBS.";
						}
					}

					if (!permanentCountry.equalsIgnoreCase("IN")) {
						Log.consoleLog(ifr, "#permanentCountry===>" + permanentCountry);
						return (permanentCountry
								+ " : Invalid Country name in either present address or permanent address. Kindly update the CBS.");
					}

					if (!Pattern.matches(namePattern, permenanetState)) {
						Log.consoleLog(ifr, "#permenanetState===>" + permenanetState);
						return (permenanetState + ": State contains invalid special characters. Kindly update the CBS");
					}

					// Data saving in backoffice
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					JSONArray childEmpJsonArray = new JSONArray();
					JSONArray childEmpJsonArray1 = new JSONArray();
					JSONObject EMPJSONObject = new JSONObject();
					Log.consoleLog(ifr, "Party Details Grid Info :");
					jsonObject.put("SHL_BKF_CUST_ID", CustomerID);
					jsonObject.put("SHL_BKF_EXIST_CUST", "Yes");
					jsonObject.put("Customer Flag", customerFlag);
					jsonObject.put("Whether customer is NRI/not", NRI);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_OffPhoneNo",
							mobile_Number.substring(mobile_Number.length() - 10));
					jsonObject.put("QNL_BASIC_INFO_accountIdString", accountIdString);
//    	                    jsonObject.put("QNL_BASIC_INFO_branchDtls", branchCode);
					if (permanentCountry.equalsIgnoreCase("IN")) {
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Nationality", "Indian");
					}

					jsonObject.put("SHL_BKF_PARTY_TYPE", map.get("ApplicantType"));
					Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
					String customerFullName = "";
					if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null"))
							|| (customerMiddleName == null)) {
						customerFullName = CustomerFirstName + " " + customerLastName;
						jsonObject.put("SHL_BKF_FULLNAME", customerFullName);
					} else {
						customerFullName = CustomerFirstName + " " + customerMiddleName + " " + customerLastName;
						jsonObject.put("SHL_BKF_FULLNAME", customerFullName);
					}
					String staffStatus = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
					Log.consoleLog(ifr, "staffStatus=" + staffStatus);
					String finalStaffStatus="";
					if (staffStatus.trim().equalsIgnoreCase("N")){
					    finalStaffStatus = "No";
					} else {
					    finalStaffStatus = "Yes";
					}
					Log.consoleLog(ifr, "finalStaffStatus=" + finalStaffStatus);
                    String relationWithAppl = ifr.getValue("P_CB_OD_RELATIONSHIP_BORROWER").toString();
                    Log.consoleLog(ifr, "relationWithAppl=" + relationWithAppl);
                    String considerEligibility = ifr.getValue("HL_CB_ConsiderForEligibility").toString();
                    Log.consoleLog(ifr, "considerEligibility=" + considerEligibility);
					jsonObject.put("SHL_BKF_STAFFSTATUS", finalStaffStatus);
					jsonObject.put("QNL_BASIC_INFO_EntityType", "I");
					jsonObject.put("SHL_BKF_EMAIL_ID", emailID);
                    jsonObject.put("QNL_BASIC_INFO_CONSIDER_FOR_ELIGIBILITY", considerEligibility);
					jsonObject.put("QNL_BASIC_INFO_Relationshipwithapplicant", relationWithAppl);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_OffTelNo", phoneNumberOffice);
					jsonObject.put("First Name", CustomerFirstName);
					jsonObject.put("Middle Name", customerMiddleName);
					jsonObject.put("Last Name", customerLastName);
					jsonObject.put("Gender", customerSex);
					jsonObject.put("SHL_BKF_FATHER_NAME", fatherName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo", landlineNumber);
					jsonObject.put("Marital Status", maritalStatus);

					try {
						DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
						DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
						Date date = originalFormat.parse(dateOfBirth);
						String DOB = targetFormat.format(date);
						Log.consoleLog(ifr, "DOB==>" + DOB);
						jsonObject.put("Date of Birth", DOB);
						SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
						SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
						Date dateq = inputAge.parse(DOB);
						String formattedDate = outputAge.format(dateq);
						LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
						int age = calculateAge(calAge);
						Log.consoleLog(ifr, "age" + age);
						String customerAge = String.valueOf(age);
						Log.consoleLog(ifr, "customerAge" + customerAge);
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age", customerAge);
					} catch (java.text.ParseException e) {
						Log.consoleLog(ifr, "DateParsing==>" + e);
					}

					jsonObject.put("SHL_BKF_MOBILE_NO", mobile_Number);

					jsonObject.put("Title", title);
					// P
					JSONObject ADDJSONObject1 = new JSONObject();
					ADDJSONObject1.put("Address Type", "P");
					ADDJSONObject1.put("Line 1", permAddress1);
					ADDJSONObject1.put("Line 2", permAddress2);
					ADDJSONObject1.put("Line 3", permAddress3);
					ADDJSONObject1.put("City", permanentCity);
					ADDJSONObject1.put("State", permenanetState);
					ADDJSONObject1.put("Country", permanentCountry);
					ADDJSONObject1.put("PIN Code", permanentZip);
					childEmpJsonArray.add(ADDJSONObject1);
					// CA
					JSONObject ADDJSONObject2 = new JSONObject();
					ADDJSONObject2.put("Address Type", "CA");
					ADDJSONObject2.put("Line 1", mailAddress1);
					ADDJSONObject2.put("Line 2", mailAddress2);
					ADDJSONObject2.put("Line 3", mailAddress3);
					ADDJSONObject2.put("City", mailCity);
					ADDJSONObject2.put("State", State);
					ADDJSONObject2.put("Country", Country);
					ADDJSONObject2.put("PIN Code", MailZip);

					childEmpJsonArray.add(ADDJSONObject2);
					EMPJSONObject.put("ID Type", "TAXID");
					EMPJSONObject.put("ID Number", panNumber);
					childEmpJsonArray1.add(EMPJSONObject);
					jsonObject.put("LV_ADDRESS", childEmpJsonArray);
					jsonObject.put("LV_KYC", childEmpJsonArray1);
					// to store occupation info
					JSONObject ADDJSONObject3 = new JSONObject();
					JSONArray childEmpJsonArray3 = new JSONArray();
					ADDJSONObject3.put("Type of Occupation", strOccuptaion);

					jsonObject.put("LV_OCCUPATION_INFO", childEmpJsonArray3);

					Demographic objDemographic = new Demographic();
					String GetDemoGraphicData = objDemographic.getDemographicSHL(ifr, processInstanceId, CustomerID);
					Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
					if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
						Log.consoleLog(ifr, "inside error condition Demographic Budget");
						return pcm.returnErrorAPIThroughExecute2(ifr, GetDemoGraphicData);
					} else {
						Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
						JSONParser jsonparser = new JSONParser();
						JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
						Log.consoleLog(ifr, obj.toString());
						String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
						Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
						LocalDate curDate = LocalDate.now();
						Log.consoleLog(ifr, "curDate  :" + curDate);
						LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
						Log.consoleLog(ifr, "PastDate  :" + PastDate);
						long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
						Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
						int YearsWithCanara = (int) (monthsBetween / 12);
						int MonthsWithCanara = (int) (monthsBetween % 12);
						Log.consoleLog(ifr, "relationshipcanaraY_hl: " + YearsWithCanara);
						Log.consoleLog(ifr, "relationshipcanaraM_hl :" + MonthsWithCanara);
						String CanaraYrs = String.valueOf(YearsWithCanara);
						String CanaraMnths = String.valueOf(MonthsWithCanara);
                        String staffStatusByUser = ifr.getValue("Co_borrower_staff_HL_status").toString();
                        Log.consoleLog(ifr, "staffStatusByUser=" + staffStatusByUser);
                        String staffIDByUser = ifr.getValue("Co_borrower_staff_HL_ID").toString();
                        Log.consoleLog(ifr, "staffIDByUser=" + staffIDByUser);
                        String staffDesignation = ifr.getValue("COBORR_DESIGNATION").toString();
                        Log.consoleLog(ifr, "staffDesignation=" + staffDesignation);
                        ADDJSONObject3.put("Co-Borrower Designation", staffDesignation);
                        ADDJSONObject3.put("Co-Borrower Staff Status", staffStatusByUser);
                        ADDJSONObject3.put("Staff ID", staffIDByUser);
						ADDJSONObject3.put("Relationship with Canara in Years", CanaraYrs);
						childEmpJsonArray3.add(ADDJSONObject3);

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						String formattedDate = PastDate.format(formatter);
						jsonObject.put("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);

//    	                        jsonObject.put("QNL_BASIC_INFO_RelationshipWithBank", CanaraYrs);
						jsonObject.put("QNL_BASIC_INFO_RelationshipwithBankMonths", CanaraMnths);
						// MIS DATA
						Log.consoleLog(ifr, "MIS DATA ADDED FOR AND BRANCHMAKER:");

						String CustMisCode1 = cf.getJsonValue(obj, "CustMisCode1");
						Log.consoleLog(ifr, "CustMisCode1==>" + CustMisCode1);

						String CustMisCode2 = cf.getJsonValue(obj, "CustMisCode2");
						Log.consoleLog(ifr, "CustMisCode2==>" + CustMisCode2);

						String CustMisCode3 = cf.getJsonValue(obj, "CustMisCode3");
						Log.consoleLog(ifr, "CustMisCode3==>" + CustMisCode3);

						String CustMisCode4 = cf.getJsonValue(obj, "CustMisCode4");
						Log.consoleLog(ifr, "CustMisCode4==>" + CustMisCode4);

						String CustMisCode5 = cf.getJsonValue(obj, "CustMisCode5");
						Log.consoleLog(ifr, "CustMisCode5==>" + CustMisCode5);

						String CustMisCode6 = cf.getJsonValue(obj, "CustMisCode6");
						Log.consoleLog(ifr, "CustMisCode6==>" + CustMisCode6);

						String CustMisCode7 = cf.getJsonValue(obj, "CustMisCode7");
						Log.consoleLog(ifr, "CustMisCode7==>" + CustMisCode7);

						String CustMisCode8 = cf.getJsonValue(obj, "CustMisCode8");
						Log.consoleLog(ifr, "CustMisCode8==>" + CustMisCode8);

						String CustMisCode9 = cf.getJsonValue(obj, "CustMisCode9");
						Log.consoleLog(ifr, "CustMisCode9==>" + CustMisCode9);

						String CustMisCode10 = cf.getJsonValue(obj, "CustMisCode10");
						Log.consoleLog(ifr, "CustMisCode10==>" + CustMisCode10);

						JSONArray childEmpJsonArray4 = new JSONArray();
						Log.consoleLog(ifr, "childEmpJsonArray4 initialized");

						JSONObject ADDJSONObject4 = new JSONObject();

						Log.consoleLog(ifr, "ADDJSONObject4 initialized");

						ADDJSONObject4.put("Minoriteis", CustMisCode1);
						ADDJSONObject4.put("Caste", CustMisCode2);
						ADDJSONObject4.put("CUST TYPE", CustMisCode3);
						ADDJSONObject4.put("CUST STAT", CustMisCode4);
						ADDJSONObject4.put("CUST CATEGORY", CustMisCode5);
						ADDJSONObject4.put("CUST SPECIAL CATEGORY", CustMisCode6);
						ADDJSONObject4.put("WEAKER SECTOR", CustMisCode7);
						ADDJSONObject4.put("CUSTOMER PREFERRED LANGUAGE", CustMisCode8);
						ADDJSONObject4.put("INTERNAL RATING DETAILS", CustMisCode9);
						ADDJSONObject4.put("BSR CODE", CustMisCode10);
						ADDJSONObject4.put("RETAIL ASSET HUB", "NA");

						Log.consoleLog(ifr, "ADDJSONObject4==>" + ADDJSONObject4.toJSONString());
						childEmpJsonArray4.add(ADDJSONObject4);

						jsonObject.put("LV_MIS_Data", childEmpJsonArray4);

					}

					// modified by Kathir for pension LC changes on 06/08/2024
					jsonObject.put("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", (NRI.equalsIgnoreCase("N") ? "No" : "Yes"));
					Log.consoleLog(ifr,
							"CustomerAccountSummary:updateCustomerDetailsBorrower -> Lead Capture NRI: " + NRI);
					ifr.setValue("CustomerName", customerFullName);
					jsonArray.add(jsonObject);
					returnJson = jsonArray.toString();
					Log.consoleLog(ifr, "returnJson:::" + returnJson);
					Log.consoleLog(ifr, "jsonArray1234EMP:::" + jsonArray);
					StaffHLPortalCustomCode staffHLPortalCustomCode=new StaffHLPortalCustomCode();
					String staffId=ifr.getValue("Co_borrower_staff_HL_ID").toString();
					Log.consoleLog(ifr, "staffId" + staffId);
					staffHLPortalCustomCode.calculateEligiblityCheck(ifr, processInstanceId, staffId, parser);
					ifr.addDataToGrid("ALV_BASIC_INFO", jsonArray, true);
					Log.consoleLog(ifr, "Applicant type :::" + map.get("ApplicantType"));
					if (map.get("ApplicantType").equalsIgnoreCase("B")) {
						ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
						Log.consoleLog(ifr, "jsonArray1234EMP:::" + branchCode);
					}
				} else {
					String[] ErrorData = CheckError.split("#");
					apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}

			} else {
				response = "No response from the server.";
				apiErrorMessage = "No response from the server.";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;
			}

			if (!apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			} else {
				if (branchCode != null) {
					Log.consoleLog(ifr, "Before returning Json:: " + returnJson);
					return returnJson;
				}
			}

		} catch (ParseException e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, Request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

	private String updateCustomerDetailsCoBorrowerNI(IFormReference ifr, String Request, String MobNum,
			HashMap<String, String> map) {

		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorCode = "";
		String apiErrorMessage = "";
		// String request = "";
		String response = "";

		String branchCode = "";
		String returnJson = "";
		try {
			String title = "";
			response = cm.getWebServiceResponse(ifr, apiName, Request);
			Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI:Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				String CustomerID = "";
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);

				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
					JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
							.parse(CustomerAccountSummaryResponse);
					// JSONObject CustomerAccountSummaryResponseJSONObj = new
					// JSONObject(CustomerAccountSummaryResponseJSON);
					CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI:CustomerID==>" + CustomerID);
					String currentStatus = "";
					String branchDtls = "";
					String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber")
							.toString();
					;
					String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
					String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();

					try {
						if (homeBranch.contains("-")) {
							String[] BRSet = homeBranch.split("-");
							branchCode = BRSet[0];
						}
					} catch (Exception e) {
						Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::Exception/Parsing BranchCode==>" + e);
					}

					String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
					String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
					String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
					// String dateOfBirth =
					// CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
					String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
					String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
					String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice")
							.toString();
					String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
					String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
					String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName")
							.toString();
					String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName")
							.toString();
					String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
					String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
					String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
					String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();

					if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
							&& (landlineNumber.length() > 10)) {
						landlineNumber = landlineNumber.substring(0, 9);
					}

					// String emailAddress =
					// CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
					String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
					String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
					String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
					String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
					String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
					String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
					String EstablishmentDate = CustomerAccountSummaryResponseJSONObj.get("DateofIncorporation")
							.toString();
					String MobileNumber = CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString();
					// String phoneNumber =
					// CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
					String mobile_Number = MobNum;
					// String mobile_Number =
					// String.valueOf(CustomerAccountSummaryResponseJSONObj.get("MobileNumber").toString());
					String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
					String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
					String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
					String State = CustomerAccountSummaryResponseJSONObj.get("State").toString();
					String Country = CustomerAccountSummaryResponseJSONObj.get("Country").toString();
					String MailZip = CustomerAccountSummaryResponseJSONObj.get("MailZip").toString();
					String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
					// Occupation Type
					String strOccuptaion = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI:strOccuptaion::" + strOccuptaion);
					if (customerSex.equalsIgnoreCase("MALE")) {
						title = "MR";
					} else if (customerSex.equalsIgnoreCase("FEMALE")) {
						title = "MRS";
					}
					if (!customerFlag.equalsIgnoreCase("Y")) {
						return "Customer does not exist";
					}

					JSONObject message = new JSONObject();

					String Entitytype = ifr.getValue("CTRID_PD_ENTYTYPE").toString();

					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI:Entitytype==>" + Entitytype);

					// Data saving in backoffice
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					JSONArray childEmpJsonArray = new JSONArray();
					JSONArray childEmpJsonArray1 = new JSONArray();
					JSONObject EMPJSONObject = new JSONObject();
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::Party Details Grid Info :");
					jsonObject.put("QNL_BASIC_INFO_CustomerID", CustomerID);
					jsonObject.put("QNL_BASIC_INFO_ExistingCustomer", "Yes");
					jsonObject.put("QNL_BASIC_INFO_customerFlag", customerFlag);
					jsonObject.put("QNL_BASIC_INFO_NRI", NRI);
					jsonObject.put("QNL_BASIC_INFO_accountIdString", accountIdString);
					jsonObject.put("QNL_BASIC_INFO_branchDtls", branchCode);

					jsonObject.put("QNL_BASIC_INFO_ApplicantType", map.get("ApplicantType"));
					Log.consoleLog(ifr,
							"updateCustomerDetailsCoBorrowerNI::customerMiddleName==>" + customerMiddleName);
					String customerFullName = "";
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::Entitytype Individual==>" + Entitytype);
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::CustomerFirstName" + CustomerFirstName);
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::customerMiddleName" + customerMiddleName);
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::customerLastName" + customerLastName);
					if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null"))
							|| (customerMiddleName == null)) {
						customerFullName = CustomerFirstName + " " + customerLastName;
						jsonObject.put("QNL_BASIC_INFO_FullName", customerFullName);
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName", customerFullName);
					} else {
						customerFullName = CustomerFirstName + " " + customerMiddleName + " " + customerLastName;
						jsonObject.put("QNL_BASIC_INFO_FullName", customerFullName);
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_BorrowerName", customerFullName);
					}

					jsonObject.put("QNL_BASIC_INFO_EntityType", Entitytype);
					// jsonObject.put("QNL_BASIC_INFO_EntityType", "I");
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_EmailID", emailID);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_OffTelNo", phoneNumberOffice);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_FirstName", CustomerFirstName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MiddleName", customerMiddleName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LastName", customerLastName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Gender", customerSex);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_FatherName", fatherName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo", landlineNumber);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MaritalStatus", maritalStatus);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_MobileNo", mobile_Number);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_Title", title);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_EmailID", emailID);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_MOBILENUMBER", mobile_Number);

					try {
						DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
						DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
						Date date = originalFormat.parse(EstablishmentDate);
						String Establishment = targetFormat.format(date);
						Log.consoleLog(ifr, "Establishment==>" + Establishment);
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_ESTABLISHMENDATE", Establishment);
					} catch (java.text.ParseException e) {
						Log.consoleLog(ifr, "DateParsing==>" + e);
					}
					// P
					JSONObject ADDJSONObject1 = new JSONObject();
					ADDJSONObject1.put("Address Type", "P");
					ADDJSONObject1.put("Line 1", permAddress1);
					ADDJSONObject1.put("Line 2", permAddress2);
					ADDJSONObject1.put("Line 3", permAddress3);
					ADDJSONObject1.put("City", permanentCity);
					ADDJSONObject1.put("State", permenanetState);
					ADDJSONObject1.put("Country", permanentCountry);
					ADDJSONObject1.put("PIN Code", permanentZip);
					childEmpJsonArray.add(ADDJSONObject1);
					// CA
					JSONObject ADDJSONObject2 = new JSONObject();
					ADDJSONObject2.put("Address Type", "CA");
					ADDJSONObject2.put("Line 1", mailAddress1);
					ADDJSONObject2.put("Line 2", mailAddress2);
					ADDJSONObject2.put("Line 3", mailAddress3);
					ADDJSONObject2.put("City", mailCity);
					ADDJSONObject2.put("State", State);
					ADDJSONObject2.put("Country", Country);
					ADDJSONObject2.put("PIN Code", MailZip);

					childEmpJsonArray.add(ADDJSONObject2);
					EMPJSONObject.put("ID Type", "TAXID");
					EMPJSONObject.put("ID Number", panNumber);
					childEmpJsonArray1.add(EMPJSONObject);
					jsonObject.put("LV_ADDRESS", childEmpJsonArray);
					jsonObject.put("LV_KYC", childEmpJsonArray1);

					// modified by Kathir for pension LC changes on 06/08/2024
					jsonObject.put("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", (NRI.equalsIgnoreCase("N") ? "No" : "Yes"));
					Log.consoleLog(ifr,
							"CustomerAccountSummary:updateCustomerDetailsCoBorrowerNI:: -> Lead Capture NRI: " + NRI);
					ifr.setValue("CustomerName", customerFullName);
					jsonArray.add(jsonObject);
					returnJson = jsonArray.toString();
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::returnJson:::" + returnJson);
					Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::jsonArray1234EMP:::" + jsonArray);
					ifr.addDataToGrid("ALV_BASIC_INFO", jsonArray, true);
					Log.consoleLog(ifr,
							"updateCustomerDetailsCoBorrowerNI::Applicant type :::" + map.get("ApplicantType"));
					if (map.get("ApplicantType").equalsIgnoreCase("B")) {
						// ifr.setValue("QL_SOURCINGINFO_BranchCode", branchCode);
						Log.consoleLog(ifr, "jsonArray1234EMP:::" + branchCode);
					}
				} else {
					String[] ErrorData = CheckError.split("#");
					apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}

			} else {
				response = "No response from the server.";
				apiErrorMessage = "No response from the server.";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;
			}

			if (!apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			} else {
				if (branchCode != null) {
					return returnJson;
				}
			}

		} catch (ParseException e) {
			Log.consoleLog(ifr, "updateCustomerDetailsCoBorrowerNI::Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		} finally {
			cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, Request, response, apiErrorCode,
					apiErrorMessage, apiStatus);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;

	}

	public String updateCustomerDetailsCoBorrowerPortal(IFormReference ifr, String response, String MobNum,
			String demoGraphicData) {
		Log.consoleLog(ifr, "inside updateCustomerDetailsCoBorrowerPortal ");
		String apiName = "customerAccountSummary";
		String serviceName = "CBS_" + apiName;
		Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String apiStatus = "";
		String apiErrorMessage = "";

		String branchCode = "";
		String returnJson = "";
		try {
			String title = "";
			Log.consoleLog(ifr, "Response===>" + response);

			if (!response.equalsIgnoreCase("{}")) {
				String CustomerID = "";
				JSONParser parser = new JSONParser();
				JSONObject resultObj = (JSONObject) parser.parse(response);

				String body = resultObj.get("body").toString();
				JSONObject bodyObj = (JSONObject) parser.parse(body);

				String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
				if (CheckError.equalsIgnoreCase("true")) {
					String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
					JSONObject CustomerAccountSummaryResponseJSONObj = (JSONObject) parser
							.parse(CustomerAccountSummaryResponse);

					CustomerID = CustomerAccountSummaryResponseJSONObj.get("CustomerID").toString();
					Log.consoleLog(ifr, "CustomerID==>" + CustomerID);

					String accountIdString = CustomerAccountSummaryResponseJSONObj.get("DefaultAccountNumber")
							.toString();
					Log.consoleLog(ifr, "DefaultAccountNumber==>" + accountIdString);

					String emailID = CustomerAccountSummaryResponseJSONObj.get("EmailID").toString();
					Log.consoleLog(ifr, "EmailID==>" + emailID);

					String homeBranch = CustomerAccountSummaryResponseJSONObj.get("HomeBranch").toString();
					Log.consoleLog(ifr, "HomeBranch==>" + homeBranch);

					try {
						if (homeBranch.contains("-")) {
							String[] BRSet = homeBranch.split("-");
							branchCode = BRSet[0];
						}
					} catch (Exception e) {
						Log.consoleLog(ifr, "Exception/Parsing BranchCode==>" + e);
					}

					String permAddress1 = CustomerAccountSummaryResponseJSONObj.get("PermAddress1").toString();
					Log.consoleLog(ifr, "PermAddress1==>" + permAddress1);

					String permAddress2 = CustomerAccountSummaryResponseJSONObj.get("PermAddress2").toString();
					Log.consoleLog(ifr, "PermAddress2==>" + permAddress2);

					String permAddress3 = CustomerAccountSummaryResponseJSONObj.get("PermAddress3").toString();
					Log.consoleLog(ifr, "PermAddress3==>" + permAddress3);

					String dateOfBirth = CustomerAccountSummaryResponseJSONObj.get("DateOfBirth").toString();
					Log.consoleLog(ifr, "DateOfBirth==>" + dateOfBirth);

					String servicingBranch = CustomerAccountSummaryResponseJSONObj.get("ServicingBranch").toString();
					Log.consoleLog(ifr, "ServicingBranch==>" + servicingBranch);

					String mailCity = CustomerAccountSummaryResponseJSONObj.get("MailCity").toString();
					Log.consoleLog(ifr, "MailCity==>" + mailCity);

					String phoneNumberOffice = CustomerAccountSummaryResponseJSONObj.get("PhoneNumberOffice")
							.toString();
					Log.consoleLog(ifr, "PhoneNumberOffice==>" + phoneNumberOffice);

					String customerFlag = CustomerAccountSummaryResponseJSONObj.get("CustomerFlag").toString();
					Log.consoleLog(ifr, "customerFlag=" + customerFlag);

					String aadharNo = CustomerAccountSummaryResponseJSONObj.get("AadharNo").toString();
					Log.consoleLog(ifr, "AadharNo==>" + aadharNo);

					String CustomerFirstName = CustomerAccountSummaryResponseJSONObj.get("CustomerFirstName")
							.toString();
					Log.consoleLog(ifr, "CustomerFirstName==>" + CustomerFirstName);

					String customerMiddleName = CustomerAccountSummaryResponseJSONObj.get("CustomerMiddleName")
							.toString();
					Log.consoleLog(ifr, "CustomerMiddleName==>" + customerMiddleName);

					String customerLastName = CustomerAccountSummaryResponseJSONObj.get("CustomerLastName").toString();
					Log.consoleLog(ifr, "CustomerLastName==>" + customerLastName);

					String customerSex = CustomerAccountSummaryResponseJSONObj.get("CustomerSex").toString();
					Log.consoleLog(ifr, "CustomerSex==>" + customerSex);

					String maritalStatus = CustomerAccountSummaryResponseJSONObj.get("MaritalStatus").toString();
					Log.consoleLog(ifr, "MaritalStatus==>" + maritalStatus);

					String landlineNumber = CustomerAccountSummaryResponseJSONObj.get("PhoneNumber").toString();
					Log.consoleLog(ifr, "PhoneNumber==>" + landlineNumber);

					if ((!landlineNumber.equalsIgnoreCase("")) && (!landlineNumber.equalsIgnoreCase("null"))
							&& (landlineNumber.length() > 10)) {
						landlineNumber = landlineNumber.substring(0, 9);
					}

					String permenanetState = CustomerAccountSummaryResponseJSONObj.get("PermState").toString();
					Log.consoleLog(ifr, "permenanetState=" + permenanetState);

					String permanentCity = CustomerAccountSummaryResponseJSONObj.get("PermCity").toString();
					Log.consoleLog(ifr, "PermCity==>" + permanentCity);

					String permanentCountry = CustomerAccountSummaryResponseJSONObj.get("PermCountry").toString();
					Log.consoleLog(ifr, "PermCountry==>" + permanentCountry);

					String permanentZip = CustomerAccountSummaryResponseJSONObj.get("PermZip").toString();
					Log.consoleLog(ifr, "PermZip==>" + permanentZip);

					String NRI = CustomerAccountSummaryResponseJSONObj.get("NRI").toString();
					Log.consoleLog(ifr, "NRI==>" + NRI);

					String panNumber = CustomerAccountSummaryResponseJSONObj.get("PanNumber").toString();
					Log.consoleLog(ifr, "PanNumber==>" + panNumber);

					String mobile_Number = MobNum;
					Log.consoleLog(ifr, "mobile_Number==>" + mobile_Number);

					String mailAddress1 = CustomerAccountSummaryResponseJSONObj.get("MailAddress1").toString();
					Log.consoleLog(ifr, "MailAddress1==>" + mailAddress1);

					String mailAddress2 = CustomerAccountSummaryResponseJSONObj.get("MailAddress2").toString();
					Log.consoleLog(ifr, "MailAddress2==>" + mailAddress2);

					String mailAddress3 = CustomerAccountSummaryResponseJSONObj.get("MailAddress3").toString();
					Log.consoleLog(ifr, "MailAddress3==>" + mailAddress3);

					String State = CustomerAccountSummaryResponseJSONObj.get("State").toString();
					Log.consoleLog(ifr, "State==>" + State);

					String Country = CustomerAccountSummaryResponseJSONObj.get("Country").toString();
					Log.consoleLog(ifr, "Country==>" + Country);

					String MailZip = CustomerAccountSummaryResponseJSONObj.get("MailZip").toString();
					Log.consoleLog(ifr, "MailZip==>" + MailZip);

					String fatherName = CustomerAccountSummaryResponseJSONObj.get("FatherName").toString();
					Log.consoleLog(ifr, "fatherName==>" + fatherName);

					String strOccuptaion = CustomerAccountSummaryResponseJSONObj.get("ProfessionCategory").toString();
					Log.consoleLog(ifr, "updateCustomerDetailsBorrower:strOccuptaion::" + strOccuptaion);

					if (customerSex.equalsIgnoreCase("MALE")) {
						title = "MR";
					} else if (customerSex.equalsIgnoreCase("FEMALE")) {
						title = "MRS";
					}

					if (!customerFlag.equalsIgnoreCase("Y")) {
						return "Customer does not exist";
					}

					JSONObject message = new JSONObject();
					String namePattern = ConfProperty.getCommonPropertyValue("NamePattern");
					String add1Pattern = ConfProperty.getCommonPropertyValue("ADD1PATTERN");
					String add2Pattern = ConfProperty.getCommonPropertyValue("ADD2PATTERN");
					String add3Pattern = ConfProperty.getCommonPropertyValue("ADD3PATTERN");

					Log.consoleLog(ifr, "#Result pattern===>" + namePattern);
					Log.consoleLog(ifr, "#Result add1Pattern===>" + add1Pattern);
					Log.consoleLog(ifr, "#Result add2Pattern===>" + add2Pattern);
					Log.consoleLog(ifr, "#Result add3Pattern===>" + add3Pattern);
					Log.consoleLog(ifr, "CustomerFirstName.length() =" + CustomerFirstName.length());

					if (CustomerFirstName.length() < 2) {
						Log.consoleLog(ifr, "#firstName===>lenghth" + CustomerFirstName);
						return (CustomerFirstName + " : Name is less than 1 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(namePattern, CustomerFirstName) ="
							+ !Pattern.matches(namePattern, CustomerFirstName));

					if (!Pattern.matches(namePattern, CustomerFirstName)) {
						Log.consoleLog(ifr, "#firstName===>" + CustomerFirstName);
						return (CustomerFirstName
								+ " : Name contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "fatherName.length() =" + fatherName.length());

					if (fatherName.length() < 2) {
						Log.consoleLog(ifr, "#fatherName===>lenghth" + fatherName);
						return (fatherName + " : Father Name is less than 1 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "dateOfBirth =" + dateOfBirth);

					if (dateOfBirth.equalsIgnoreCase("") || dateOfBirth.equalsIgnoreCase(null)) {
						Log.consoleLog(ifr, "#dateOfBirth===>lenghth" + dateOfBirth);
						return (dateOfBirth + " : DOB is Mandatory. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress1.length() =" + permAddress1.length());

					if (permAddress1.length() < 4) {
						Log.consoleLog(ifr, "#permAddress1===>lenghth" + permAddress1);
						return (permAddress1
								+ " : Permanent address line 1 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress2.length() =" + permAddress2.length());

					if (permAddress2.length() < 4) {
						Log.consoleLog(ifr, "#permAddress2===>lenghth" + permAddress2);
						return (permAddress2
								+ " : Permanent address line 2 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "permAddress3.length() =" + permAddress3.length());

					if (permAddress3.length() < 4) {
						Log.consoleLog(ifr, "#permAddress3===>lenghth" + permAddress3);
						return (permAddress3
								+ " : Permanent address line 3 is less than 3 character. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)" + permAddress1 + add1Pattern);
					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)"
							+ !Pattern.matches(add1Pattern, permAddress1));

					if (!Pattern.matches(add1Pattern, permAddress1)) {
						Log.consoleLog(ifr, "#permAddress1===>" + permAddress1);
						return (permAddress1
								+ " : Address line 1 contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr,
							"!Pattern.matches(add1Pattern, permAddress1)" + !Pattern.matches(add2Pattern, permAddress2)
									+ !Pattern.matches(add3Pattern, permAddress3));

					if (!Pattern.matches(add2Pattern, permAddress2)) {
						Log.consoleLog(ifr, "#permAddress2===>" + permAddress2);
						return (permAddress2
								+ " : Address line 2 contains invalid special characters. Kindly update the CBS");
					}

					Log.consoleLog(ifr, "!Pattern.matches(add1Pattern, permAddress1)"
							+ !Pattern.matches(add3Pattern, permAddress3));

					if (!Pattern.matches(add3Pattern, permAddress3)) {
						Log.consoleLog(ifr, "#permAddress3===>" + permAddress3);
						return (permAddress3
								+ " : Address line 3 contains invalid special characters. Kindly update the CBS");
					}

					String StateCode = "";
					Log.consoleLog(ifr, "permenanetState=" + permenanetState);

					String stateCodeQ = "SELECT STATE_CODE_NO FROM LOS_MST_STATE WHERE "
							+ "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + permenanetState + "')) AND ROWNUM=1";
					Log.consoleLog(ifr, "stateCodeQ==>" + stateCodeQ);

					List<List<String>> stateCodeR = cf.mExecuteQuery(ifr, stateCodeQ, "stateCodeQ==> ");
					Log.consoleLog(ifr, "#stateCodeR===>" + stateCodeR.toString());

					if (stateCodeR.size() > 0) {
						StateCode = stateCodeR.get(0).get(0);
					}

					if (StateCode.equalsIgnoreCase("")) {
						Log.consoleLog(ifr, "#StateCode is null ===>");
						return "Applicants Perm State code is not available in state master. Kindly update the CBS.";
					} else {
						String ZIP_FRST_TWO_MIN = "", ZIP_FRST_TWO_MAX = "";
						String Query = "SELECT ZIP_FRST_TWO_MIN,ZIP_FRST_TWO_MAX FROM LOS_MST_STATE WHERE "
								+ "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + permenanetState + "')) AND ROWNUM=1";
						Log.consoleLog(ifr, "Query3==>" + Query);

						List<List<String>> Result = ifr.getDataFromDB(Query);
						Log.consoleLog(ifr, "#Result===>" + Result.toString());

						if (Result.size() > 0) {
							ZIP_FRST_TWO_MIN = Result.get(0).get(0);
							ZIP_FRST_TWO_MAX = Result.get(0).get(1);
						}

						String firstTwoChars = permanentZip.length() >= 2 ? permanentZip.substring(0, 2) : permanentZip;
						Log.consoleLog(ifr, "#firstTwoChars===>" + firstTwoChars);

						if (Integer.parseInt(firstTwoChars) >= Integer.parseInt(ZIP_FRST_TWO_MIN)
								&& Integer.parseInt(firstTwoChars) <= Integer.parseInt(ZIP_FRST_TWO_MAX)) {
							Log.consoleLog(ifr, "#firstTwoChars is within range===>");
						} else {
							Log.consoleLog(ifr, "#firstTwoChars is not in  range ===>");
							return "State Code and PIN code Validation failed. Kindly update the CBS.";
						}
					}

					if (!permanentCountry.equalsIgnoreCase("IN")) {
						Log.consoleLog(ifr, "#permanentCountry===>" + permanentCountry);
						return (permanentCountry
								+ " : Invalid Country name in either present address or permanent address. Kindly update the CBS.");
					}

					if (!Pattern.matches(namePattern, permenanetState)) {
						Log.consoleLog(ifr, "#permenanetState===>" + permenanetState);
						return (permenanetState + ": State contains invalid special characters. Kindly update the CBS");
					}

					// Data saving in backoffice
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					JSONArray childEmpJsonArray = new JSONArray();
					JSONArray childEmpJsonArray1 = new JSONArray();
					JSONObject EMPJSONObject = new JSONObject();
					Log.consoleLog(ifr, "Party Details Grid Info :");
					jsonObject.put("SHL_BKF_CUST_ID", CustomerID);
					jsonObject.put("SHL_BKF_EXIST_CUST", "Yes");
					jsonObject.put("QNL_BASIC_INFO_customerFlag", customerFlag);
					jsonObject.put("QNL_BASIC_INFO_NRI", NRI);
					jsonObject.put("QNL_BASIC_INFO_accountIdString", accountIdString);
					jsonObject.put("QNL_BASIC_INFO_branchDtls", branchCode);
					if (permanentCountry.equalsIgnoreCase("IN")) {
						jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_NI_Nationality", "Indian");
					}

					Log.consoleLog(ifr, "customerMiddleName==>" + customerMiddleName);
					String customerFullName = "";
					if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null"))
							|| (customerMiddleName == null)) {
						customerFullName = CustomerFirstName + " " + customerLastName;
						jsonObject.put("SHL_BKF_FULLNAME", customerFullName);
					} else {
						customerFullName = CustomerFirstName + " " + customerMiddleName + " " + customerLastName;
						jsonObject.put("SHL_BKF_FULLNAME", customerFullName);
					}
					String staffStatus = CustomerAccountSummaryResponseJSONObj.get("Staff").toString();
					Log.consoleLog(ifr, "staffStatus=" + staffStatus);
					String finalStaffStatus="";
					if (staffStatus.trim().equalsIgnoreCase("N")){
					    finalStaffStatus = "No";
					} else {
					    finalStaffStatus = "Yes";
					}
					Log.consoleLog(ifr, "finalStaffStatus=" + finalStaffStatus);
					ifr.setValue("SHL_BKF_STAFFSTATUS", finalStaffStatus);
					ifr.setValue("SHL_BKF_EMAIL_ID", emailID);
					ifr.setValue("SHL_CB_ENTITY", "I");
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_OffTelNo", phoneNumberOffice);
					jsonObject.put("SHL_BKF_FIRST_NAME", CustomerFirstName);
					jsonObject.put("SHL_BKF_MID_NAME", customerMiddleName);
					jsonObject.put("SHL_BKF_LAST_NAME", customerLastName);
					jsonObject.put("SHL_BKF_GENDER", customerSex);
					ifr.setValue("SHL_BKF_FATHER_NAME", fatherName);
					jsonObject.put("QNL_BASIC_INFO_CL_BASIC_INFO_I_LandlineNo", landlineNumber);
					jsonObject.put("SHL_BKF_MARITAL_STATUS", maritalStatus);

					try {
						DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
						DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
						Date date = originalFormat.parse(dateOfBirth);
						String DOB = targetFormat.format(date);
						Log.consoleLog(ifr, "DOB==>" + DOB);
						jsonObject.put("SHL_BKF_DOB", DOB);
						SimpleDateFormat inputAge = new SimpleDateFormat("dd/MM/yyyy");
						SimpleDateFormat outputAge = new SimpleDateFormat("dd/MM/yyyy");
						Date dateq = inputAge.parse(DOB);
						String formattedDate = outputAge.format(dateq);
						LocalDate calAge = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
						int age = calculateAge(calAge);
						Log.consoleLog(ifr, "age" + age);
						String customerAge = String.valueOf(age);
						Log.consoleLog(ifr, "customerAge" + customerAge);
						ifr.setValue("Coborrower_Age_Portal", customerAge);
					} catch (java.text.ParseException e) {
						Log.consoleLog(ifr, "DateParsing==>" + e);
					}

					ifr.setValue("SHL_BKF_MOBILE_NO", mobile_Number);

					jsonObject.put("SHL_BKF_TITLE", title);
					// P
					JSONObject ADDJSONObject1 = new JSONObject();
					ADDJSONObject1.put("Address Type", "P");
					ADDJSONObject1.put("Line 1", permAddress1);
					ADDJSONObject1.put("Line 2", permAddress2);
					ADDJSONObject1.put("Line 3", permAddress3);
					ADDJSONObject1.put("City", permanentCity);
					ADDJSONObject1.put("State", permenanetState);
					ADDJSONObject1.put("Country", permanentCountry);
					ADDJSONObject1.put("PIN Code", permanentZip);
					childEmpJsonArray.add(ADDJSONObject1);
					// CA
					JSONObject ADDJSONObject2 = new JSONObject();
					ADDJSONObject2.put("Address Type", "CA");
					ADDJSONObject2.put("Line 1", mailAddress1);
					ADDJSONObject2.put("Line 2", mailAddress2);
					ADDJSONObject2.put("Line 3", mailAddress3);
					ADDJSONObject2.put("City", mailCity);
					ADDJSONObject2.put("State", State);
					ADDJSONObject2.put("Country", Country);
					ADDJSONObject2.put("PIN Code", MailZip);

					childEmpJsonArray.add(ADDJSONObject2);
					ifr.addDataToGrid("LV_ADDRESS", childEmpJsonArray, true);

					EMPJSONObject.put("ID Type", "TAXID");
					EMPJSONObject.put("ID Number", panNumber);
					childEmpJsonArray1.add(EMPJSONObject);
					ifr.addDataToGrid("LV_KYC", childEmpJsonArray1, true);
					// to store occupation info
					JSONObject ADDJSONObject3 = new JSONObject();
					JSONArray childEmpJsonArray3 = new JSONArray();
					ADDJSONObject3.put("Type of Occupation", strOccuptaion);
					childEmpJsonArray3.add(ADDJSONObject3);
					jsonObject.put("LV_OCCUPATION_INFO", childEmpJsonArray3);

					String GetDemoGraphicData = demoGraphicData;
					Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
					if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
						Log.consoleLog(ifr, "inside error condition Demographic Budget");
						return pcm.returnErrorAPIThroughExecute2(ifr, GetDemoGraphicData);
					} else {
						Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
						JSONParser jsonparser = new JSONParser();
						JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
						Log.consoleLog(ifr, obj.toString());
						String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
						Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
						LocalDate curDate = LocalDate.now();
						Log.consoleLog(ifr, "curDate  :" + curDate);
						LocalDate PastDate = LocalDate.parse(DateOfCustOpen);
						Log.consoleLog(ifr, "PastDate  :" + PastDate);
						long monthsBetween = ChronoUnit.MONTHS.between(PastDate, curDate);
						Log.consoleLog(ifr, " MonthsBetween  :" + monthsBetween);
						int YearsWithCanara = (int) (monthsBetween / 12);
						int MonthsWithCanara = (int) (monthsBetween % 12);
						Log.consoleLog(ifr, "relationshipcanaraY_hl: " + YearsWithCanara);
						Log.consoleLog(ifr, "relationshipcanaraM_hl :" + MonthsWithCanara);
						String CanaraYrs = String.valueOf(YearsWithCanara);
						String CanaraMnths = String.valueOf(MonthsWithCanara);

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						String formattedDate = PastDate.format(formatter);
						jsonObject.put("QNL_BASIC_INFO_CustomerSinceDate", formattedDate);

						ifr.setValue("relationshipcanaraY_hl", CanaraYrs);
//                        ifr.setValue("QNL_HL_PORTAL_CNL_BASIC_INFO_RelationshipwithBankMonths", CanaraMnths);
						// MIS DATA
						Log.consoleLog(ifr, "MIS DATA ADDED FOR AND BRANCHMAKER:");

						String CustMisCode1 = cf.getJsonValue(obj, "CustMisCode1");
						Log.consoleLog(ifr, "CustMisCode1==>" + CustMisCode1);

						String CustMisCode2 = cf.getJsonValue(obj, "CustMisCode2");
						Log.consoleLog(ifr, "CustMisCode2==>" + CustMisCode2);

						String CustMisCode3 = cf.getJsonValue(obj, "CustMisCode3");
						Log.consoleLog(ifr, "CustMisCode3==>" + CustMisCode3);

						String CustMisCode4 = cf.getJsonValue(obj, "CustMisCode4");
						Log.consoleLog(ifr, "CustMisCode4==>" + CustMisCode4);

						String CustMisCode5 = cf.getJsonValue(obj, "CustMisCode5");
						Log.consoleLog(ifr, "CustMisCode5==>" + CustMisCode5);

						String CustMisCode6 = cf.getJsonValue(obj, "CustMisCode6");
						Log.consoleLog(ifr, "CustMisCode6==>" + CustMisCode6);

						String CustMisCode7 = cf.getJsonValue(obj, "CustMisCode7");
						Log.consoleLog(ifr, "CustMisCode7==>" + CustMisCode7);

						String CustMisCode8 = cf.getJsonValue(obj, "CustMisCode8");
						Log.consoleLog(ifr, "CustMisCode8==>" + CustMisCode8);

						String CustMisCode9 = cf.getJsonValue(obj, "CustMisCode9");
						Log.consoleLog(ifr, "CustMisCode9==>" + CustMisCode9);

						String CustMisCode10 = cf.getJsonValue(obj, "CustMisCode10");
						Log.consoleLog(ifr, "CustMisCode10==>" + CustMisCode10);

						JSONArray childEmpJsonArray4 = new JSONArray();
						Log.consoleLog(ifr, "childEmpJsonArray4 initialized");

						JSONObject ADDJSONObject4 = new JSONObject();

						Log.consoleLog(ifr, "ADDJSONObject4 initialized");

						ADDJSONObject4.put("Minoriteis", CustMisCode1);
						ADDJSONObject4.put("Caste", CustMisCode2);
						ADDJSONObject4.put("CUST TYPE", CustMisCode3);
						ADDJSONObject4.put("CUST STAT", CustMisCode4);
						ADDJSONObject4.put("CUST CATEGORY", CustMisCode5);
						ADDJSONObject4.put("CUST SPECIAL CATEGORY", CustMisCode6);
						ADDJSONObject4.put("WEAKER SECTOR", CustMisCode7);
						ADDJSONObject4.put("CUSTOMER PREFERRED LANGUAGE", CustMisCode8);
						ADDJSONObject4.put("INTERNAL RATING DETAILS", CustMisCode9);
						ADDJSONObject4.put("BSR CODE", CustMisCode10);
						ADDJSONObject4.put("RETAIL ASSET HUB", "NA");

						Log.consoleLog(ifr, "ADDJSONObject4==>" + ADDJSONObject4.toJSONString());
						childEmpJsonArray4.add(ADDJSONObject4);
						ifr.addDataToGrid("LV_MIS_Data", childEmpJsonArray4, true);
					}

					// modified by Kathir for pension LC changes on 06/08/2024
					jsonObject.put("QNL_BASIC_INFO_CUSTOMERISNRIORNOT", (NRI.equalsIgnoreCase("N") ? "No" : "Yes"));
					Log.consoleLog(ifr,
							"CustomerAccountSummary:updateCustomerDetailsBorrower -> Lead Capture NRI: " + NRI);
					ifr.setValue("CustomerName", customerFullName);
					jsonArray.add(jsonObject);
					returnJson = jsonArray.toString();
					Log.consoleLog(ifr, "returnJson:::" + returnJson);
					Log.consoleLog(ifr, "jsonArray1234EMP:::" + jsonArray);
				} else {
					String[] ErrorData = CheckError.split("#");
					String apiErrorCode = ErrorData[0];
					apiErrorMessage = ErrorData[1];
				}

			} else {
				apiErrorMessage = "No response from the server.";
			}

			if (apiErrorMessage.equalsIgnoreCase("")) {
				apiStatus = RLOS_Constants.SUCCESS;
			} else {
				apiStatus = RLOS_Constants.ERROR + ":" + apiErrorMessage;
			}

			if (!apiStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return apiStatus;
			} else {
				if (branchCode != null) {
					Log.consoleLog(ifr, "Before returning Json:: " + returnJson);
					return returnJson;
				}
			}

		} catch (ParseException e) {
			Log.consoleLog(ifr, "Exception/CaptureRequestResponse" + e);
			Log.errorLog(ifr, "Exception/CaptureRequestResponse" + e);
		}
		return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	}

}
